"""
Title: Track Top K Hashtags in a Sliding Event Window

Problem Description:
You are building analytics for a live social platform. Events arrive in non-decreasing
timestamp order, and each event contains a single hashtag string. For every event, you
must report the current top k hashtags that appeared within the last windowSize seconds,
inclusive of the current timestamp and excluding events older than
currentTime - windowSize + 1.

The ranking rules are:
1. A hashtag with higher frequency in the active window ranks higher.
2. If two hashtags have the same frequency, the lexicographically smaller hashtag ranks higher.

Return the top k hashtags after processing each event, as a list of lists. If fewer than
k distinct hashtags are present in the active window, return all of them in ranked order
for that step.

Because old events expire as time advances, a hashtag's count can both increase and decrease
over time. A queue can evict expired events, and a heap with lazy deletion can help maintain
rankings efficiently.

Constraints:
- 1 <= n <= 100000
- 1 <= windowSize <= 10^9
- 1 <= k <= 100
- timestamps.length == hashtags.length == n
- timestamps is sorted in non-decreasing order
- hashtags contain only lowercase English letters
"""

from collections import deque
import heapq
from typing import Deque, Dict, List, Tuple


class Solution:
    def top_k_hashtags(
        self,
        timestamps: List[int],
        hashtags: List[str],
        window_size: int,
        k: int,
    ) -> List[List[str]]:
        """
        Process the event stream and return the top k hashtags after each event.

        The algorithm maintains:
        1. A queue of active events currently inside the sliding time window.
        2. A frequency map for hashtags inside the active window.
        3. A heap storing ranking candidates using lazy deletion.

        Because counts can both increase and decrease as events enter and leave the
        window, the heap may contain stale entries. We do not try to update old heap
        entries in place. Instead, whenever a hashtag's count changes, we push a new
        heap entry. Later, when reading from the heap, we verify whether the entry still
        matches the current frequency map. If not, it is stale and discarded.

        Args:
            timestamps: Non-decreasing event timestamps.
            hashtags: Hashtag string for each event.
            window_size: Size of the inclusive sliding time window.
            k: Number of top hashtags to report after each event.

        Returns:
            A list where the i-th element is the ranked list of top hashtags after
            processing the i-th event.

        Time complexity:
            O(n * k * log n) in practice with lazy deletion, where n is the number of events.
            Each event is inserted once, removed once from the queue, and causes heap pushes.
            Extracting top k requires heap operations and temporary buffering.

        Space complexity:
            O(n) in the worst case for the queue and heap due to lazy deletion.
        """
        # This queue stores only events that are still relevant or not yet evicted.
        # Each item is a pair: (timestamp, hashtag).
        #
        # Why a queue?
        # Events arrive in non-decreasing timestamp order, so the oldest event is always
        # at the front. That makes expiration very efficient: repeatedly pop from the
        # left while events are too old for the current window.
        active_events: Deque[Tuple[int, str]] = deque()

        # This dictionary stores the current frequency of each hashtag inside the active window.
        #
        # Example:
        # counts["ai"] == 3 means "ai" appears 3 times among events whose timestamps
        # are currently inside the valid window.
        counts: Dict[str, int] = {}

        # This heap stores ranking candidates.
        #
        # Python's heapq is a min-heap, so to simulate "highest frequency first",
        # we store (-frequency, hashtag).
        #
        # Why does this work?
        # - Smaller negative frequency means larger actual frequency.
        #   Example: -5 comes before -2, so count 5 ranks above count 2.
        # - If frequencies tie, Python compares the second tuple element, the hashtag.
        #   Since lexicographically smaller hashtag should rank higher, storing the
        #   plain hashtag string gives exactly the desired tie-break behavior.
        #
        # Important note:
        # The heap may contain stale entries because a hashtag's count changes over time.
        # We handle that with lazy deletion when reading from the heap.
        heap: List[Tuple[int, str]] = []

        # This will store the answer after each processed event.
        result: List[List[str]] = []

        # Process each event in order.
        for current_time, tag in zip(timestamps, hashtags):
            # ------------------------------------------------------------
            # Step 1: Expire events that are now outside the sliding window.
            # ------------------------------------------------------------
            #
            # The valid window for current_time is:
            # [current_time - window_size + 1, current_time]
            #
            # Therefore, any event with timestamp < current_time - window_size + 1
            # must be removed before we count the current event.
            #
            # We compute the smallest valid timestamp in the current window.
            window_start: int = current_time - window_size + 1

            # Remove all events from the front of the queue that are too old.
            while active_events and active_events[0][0] < window_start:
                expired_time, expired_tag = active_events.popleft()

                # Decrease the frequency of the expired hashtag because it is no longer
                # part of the active window.
                counts[expired_tag] -= 1

                # If the count becomes zero, remove the hashtag entirely from the map.
                # This keeps the dictionary clean and also helps stale-entry validation.
                if counts[expired_tag] == 0:
                    del counts[expired_tag]
                else:
                    # The hashtag still exists with a smaller count.
                    # Push its updated state into the heap.
                    #
                    # We do not remove the old heap entry. That old entry becomes stale
                    # and will be ignored later when encountered.
                    heapq.heappush(heap, (-counts[expired_tag], expired_tag))

            # ------------------------------------------------------------
            # Step 2: Add the current event into the active window.
            # ------------------------------------------------------------
            #
            # First, append the event to the queue so it can later expire in order.
            active_events.append((current_time, tag))

            # Increase the current hashtag's frequency in the active window.
            counts[tag] = counts.get(tag, 0) + 1

            # Push the updated state into the heap.
            # Again, we allow old entries to remain and rely on lazy deletion later.
            heapq.heappush(heap, (-counts[tag], tag))

            # ------------------------------------------------------------
            # Step 3: Read the current top k hashtags.
            # ------------------------------------------------------------
            #
            # We cannot simply pop k items directly and trust them, because some heap
            # entries may be stale:
            # - The hashtag's count may have changed since that entry was pushed.
            # - The hashtag may have disappeared from the active window entirely.
            #
            # So for each candidate popped from the heap:
            # - Convert negative frequency back to positive.
            # - Check whether counts[tag] still equals that frequency.
            # - If yes, it is valid and can be used.
            # - If not, discard it and continue.
            #
            # One more subtle issue:
            # While collecting the top k, we pop valid entries from the heap. But we still
            # need them available for future steps. Therefore, we temporarily store valid
            # popped entries and push them back after finishing this query.
            current_top: List[str] = []
            valid_entries_to_restore: List[Tuple[int, str]] = []

            while heap and len(current_top) < k:
                neg_freq, candidate_tag = heapq.heappop(heap)
                candidate_freq: int = -neg_freq

                # Validate whether this heap entry still matches the current true state.
                #
                # It is valid only if:
                # 1. The hashtag still exists in the active window.
                # 2. Its current count equals the frequency stored in this heap entry.
                #
                # If either condition fails, this entry is stale and must be ignored.
                if counts.get(candidate_tag, 0) != candidate_freq:
                    continue

                # This is a valid current ranking entry.
                current_top.append(candidate_tag)
                valid_entries_to_restore.append((neg_freq, candidate_tag))

            # Restore the valid entries we popped so the heap remains usable for future events.
            for entry in valid_entries_to_restore:
                heapq.heappush(heap, entry)

            # Save the ranked list for this step.
            result.append(current_top)

        return result


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    timestamps_1 = [1, 2, 3, 6, 7]
    hashtags_1 = ["ai", "ml", "ai", "db", "ai"]
    window_size_1 = 5
    k_1 = 2

    output_1 = solution.top_k_hashtags(timestamps_1, hashtags_1, window_size_1, k_1)
    print("Example 1 Output:")
    print(output_1)
    print("Expected:")
    print([["ai"], ["ai", "ml"], ["ai", "ml"], ["ai", "db"], ["ai", "db"]])
    print()

    # Example 2
    timestamps_2 = [4, 4, 5, 8, 8, 9]
    hashtags_2 = ["red", "blue", "red", "green", "blue", "blue"]
    window_size_2 = 3
    k_2 = 3

    output_2 = solution.top_k_hashtags(timestamps_2, hashtags_2, window_size_2, k_2)
    print("Example 2 Output:")
    print(output_2)
    print("Expected:")
    print([["red"], ["blue", "red"], ["red", "blue"], ["green"], ["blue", "green"], ["blue", "green"]]))