"""
Title: Merge Sensor Streams by Freshest Reading

Problem Description:
You are given k sensor streams. Each stream is represented as a list of readings
sorted by increasing timestamp. A reading is a pair [timestamp, value].
Different streams may contain readings at the same timestamp, and some streams
may be empty.

Your task is to produce a single merged timeline of readings using the following rule:
repeatedly choose the unread reading with the smallest timestamp among all streams.
If multiple unread readings share the same timestamp, choose the one with the larger
value first. If there is still a tie, choose the reading from the smaller stream index first.

Return the merged result as a list of triples [timestamp, value, streamIndex]
in the exact order they are selected.

This problem models merging multiple already-sorted event feeds while preserving
a deterministic priority rule. An efficient solution should avoid repeatedly
scanning all streams to find the next reading.

Constraints:
- 1 <= k <= 10^4
- 0 <= total number of readings across all streams <= 2 * 10^5
- 0 <= timestamp <= 10^9
- -10^9 <= value <= 10^9
- Each individual stream is sorted by nondecreasing timestamp
- The output size equals the total number of readings
"""

from heapq import heappop, heappush
from typing import List, Tuple


class Solution:
    def merge_sensor_streams(self, streams: List[List[List[int]]]) -> List[List[int]]:
        """
        Merge multiple sorted sensor streams into one timeline using the required priority rules.

        The selection rule is:
        1. Smaller timestamp first
        2. If timestamps tie, larger value first
        3. If both tie, smaller stream index first

        Args:
            streams: A list of sensor streams. Each stream is a list of [timestamp, value]
                pairs sorted by nondecreasing timestamp.

        Returns:
            A merged list of [timestamp, value, streamIndex] triples in the exact order
            they are selected.

        Time complexity:
            O(N log k), where N is the total number of readings across all streams
            and k is the number of streams.

        Space complexity:
            O(k) auxiliary heap space, not counting the output list.
        """
        # This heap will always store the "current unread reading" from each stream.
        #
        # Why a heap?
        # ----------
        # We repeatedly need the next globally best reading according to the problem's
        # ordering rules. A min-heap is perfect for this because it lets us:
        # - insert a candidate in O(log k)
        # - remove the smallest candidate in O(log k)
        #
        # Since there can be up to 10^4 streams, scanning every stream each time would
        # be too slow. The heap keeps only one active candidate per stream, which gives
        # us the desired O(N log k) performance.
        #
        # Heap item layout:
        # (timestamp, -value, stream_index, reading_index)
        #
        # Why this exact tuple?
        # ---------------------
        # Python's heapq is a min-heap and compares tuples lexicographically.
        #
        # We want:
        # 1. smallest timestamp first        -> use timestamp directly
        # 2. larger value first on tie       -> use -value so larger value becomes smaller
        # 3. smaller stream index first      -> use stream_index directly
        #
        # reading_index is included so that after popping a reading, we know where to
        # find the next unread reading from the same stream.
        heap: List[Tuple[int, int, int, int]] = []

        # This will store the final merged timeline in the required format:
        # [timestamp, value, streamIndex]
        merged: List[List[int]] = []

        # Step 1: Initialize the heap with the first reading from every non-empty stream.
        #
        # We only push the first unread reading from each stream because each stream is
        # already sorted by timestamp. Once we consume one reading from a stream, the
        # next reading from that same stream becomes the only new candidate we need to add.
        for stream_index, stream in enumerate(streams):
            if stream:
                timestamp, value = stream[0]
                heappush(heap, (timestamp, -value, stream_index, 0))

        # Step 2: Repeatedly extract the best available reading from the heap.
        #
        # Each loop iteration chooses exactly one reading for the output.
        # After choosing it, we advance only in that reading's stream and push the next
        # unread reading from that stream if one exists.
        while heap:
            # Pop the reading that is currently best according to:
            # - smallest timestamp
            # - then largest value
            # - then smallest stream index
            timestamp, neg_value, stream_index, reading_index = heappop(heap)

            # Convert the stored negative value back to the original value.
            value = -neg_value

            # Append the chosen reading to the final answer in the required output format.
            merged.append([timestamp, value, stream_index])

            # Move to the next reading in the same stream.
            next_reading_index = reading_index + 1

            # If that stream still has unread readings, push the next one into the heap.
            #
            # This is the key idea behind k-way merge:
            # at any moment, the heap contains the next unread candidate from each stream.
            if next_reading_index < len(streams[stream_index]):
                next_timestamp, next_value = streams[stream_index][next_reading_index]
                heappush(
                    heap,
                    (next_timestamp, -next_value, stream_index, next_reading_index),
                )

        # Once the heap is empty, every reading from every stream has been processed.
        return merged


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    streams1: List[List[List[int]]] = [
        [[1, 5], [4, 2]],
        [[1, 7], [3, 1]],
        [[2, 9]],
    ]
    result1 = solution.merge_sensor_streams(streams1)
    print("Example 1 Result:")
    print(result1)
    # Expected:
    # [[1, 7, 1], [1, 5, 0], [2, 9, 2], [3, 1, 1], [4, 2, 0]]

    # Example 2
    streams2: List[List[List[int]]] = [
        [],
        [[2, 4], [2, 3], [5, 8]],
        [[2, 4]],
        [[1, 10], [6, 0]],
    ]
    result2 = solution.merge_sensor_streams(streams2)
    print("Example 2 Result:")
    print(result2)
    # Expected:
    # [[1, 10, 3], [2, 4, 1], [2, 4, 2], [2, 3, 1], [5, 8, 1], [6, 0, 3]]