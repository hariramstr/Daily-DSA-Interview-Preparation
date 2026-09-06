"""
Title: Merge Release Streams by Highest Current Severity

Problem Description:
A company receives incident reports from multiple release streams. Each stream is
already sorted in non-increasing order by severity score, where a larger score
means a more critical issue. You are given a list of integer arrays `streams`,
where `streams[i]` contains the severity scores for stream `i`, already sorted
from highest to lowest. Your task is to merge all streams into one global
processing order.

At every step, you may only take the next unprocessed report from any stream.
Return the merged list of all severity scores in non-increasing order. If two
available reports have the same severity, choose the one from the smaller stream
index first. If there is still a tie, choose the one that appears earlier within
its stream.

Design an algorithm that is efficient when the number of streams is large and
each stream may have different length. A solution that repeatedly scans every
stream for the next best item will be too slow.

Constraints:
- 1 <= streams.length <= 10^5
- 0 <= streams[i].length <= 10^5
- 0 <= severity <= 10^9
- Each `streams[i]` is sorted in non-increasing order
- The total number of reports across all streams does not exceed 2 * 10^5

Examples:
1) streams = [[9,7,3],[10,6],[8,8,1]]
   Output: [10,9,8,8,7,6,3,1]

2) streams = [[5,5,2],[],[5,4],[6]]
   Output: [6,5,5,5,4,2]
"""

from heapq import heappop, heappush
from typing import List, Tuple


class Solution:
    def merge_release_streams(self, streams: List[List[int]]) -> List[int]:
        """
        Merge multiple already-sorted release streams into one non-increasing list.

        The method uses a heap (priority queue) to always select the currently
        highest available severity across all streams without scanning every stream
        each time.

        Args:
            streams: A list of streams, where each stream is sorted in
                non-increasing order.

        Returns:
            A single merged list containing all severity scores in non-increasing
            order, with ties broken by smaller stream index first, and then by
            earlier position within the same stream.

        Time Complexity:
            O(T log K)
            where T is the total number of reports across all streams and
            K is the number of streams.

        Space Complexity:
            O(K) auxiliary heap space, not counting the output list.
        """
        # This heap will store exactly one "current candidate" from each non-empty stream.
        #
        # Python's heapq is a min-heap, but we need to repeatedly extract the
        # largest severity. To simulate max-heap behavior, we store severity as
        # a negative number:
        #   larger severity -> smaller negative value -> popped first
        #
        # Each heap entry is a tuple:
        #   (-severity, stream_index, element_index)
        #
        # Why this tuple order works:
        # 1. -severity:
        #    Ensures highest severity is chosen first.
        # 2. stream_index:
        #    If severities tie, smaller stream index is chosen first exactly as required.
        # 3. element_index:
        #    This is included for completeness and deterministic ordering.
        #    In practice, only one candidate per stream is ever in the heap at once,
        #    so two entries from the same stream cannot compete simultaneously.
        #    Still, including it makes the ordering fully explicit and beginner-friendly.
        heap: List[Tuple[int, int, int]] = []

        # Build the initial heap by taking the first unprocessed item from every
        # non-empty stream.
        #
        # At the beginning, the "next available" report from each stream is simply
        # its first element (index 0).
        for stream_index, stream in enumerate(streams):
            if stream:
                heappush(heap, (-stream[0], stream_index, 0))

        # This list will store the final merged order.
        merged: List[int] = []

        # Continue until there are no more available reports in any stream.
        while heap:
            # Pop the best currently available report:
            # - highest severity
            # - if tied, smaller stream index
            # - if still tied, smaller element index
            neg_severity, stream_index, element_index = heappop(heap)

            # Convert back from negative to the original severity value.
            severity = -neg_severity

            # Add the chosen report to the final answer.
            merged.append(severity)

            # We have just consumed one report from this stream.
            # The only new candidate that can become available from this same stream
            # is the next element immediately after the one we just took.
            next_index = element_index + 1

            # If that next element exists, push it into the heap so it can compete
            # with the current candidates from the other streams.
            if next_index < len(streams[stream_index]):
                next_severity = streams[stream_index][next_index]
                heappush(heap, (-next_severity, stream_index, next_index))

        return merged


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    streams_1 = [[9, 7, 3], [10, 6], [8, 8, 1]]
    result_1 = solution.merge_release_streams(streams_1)
    print("Example 1 Input:", streams_1)
    print("Example 1 Output:", result_1)
    print("Expected:", [10, 9, 8, 8, 7, 6, 3, 1])
    print()

    # Example 2
    streams_2 = [[5, 5, 2], [], [5, 4], [6]]
    result_2 = solution.merge_release_streams(streams_2)
    print("Example 2 Input:", streams_2)
    print("Example 2 Output:", result_2)
    print("Expected:", [6, 5, 5, 5, 4, 2])
    print()

    # Additional quick sanity check
    streams_3 = [[], [7], [7, 7], [9, 1], []]
    result_3 = solution.merge_release_streams(streams_3)
    print("Additional Input:", streams_3)
    print("Additional Output:", result_3)