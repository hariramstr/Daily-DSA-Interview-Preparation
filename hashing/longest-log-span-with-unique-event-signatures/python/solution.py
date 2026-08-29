"""
Title: Longest Log Span With Unique Event Signatures

Problem Description:
You are given an array events where events[i] is a string representing the signature
of the i-th system log entry in chronological order. A monitoring team wants to
extract the longest contiguous span of logs such that no event signature appears
more than once inside that span.

Return the length of the longest contiguous subarray of events that contains only
unique strings.

Two log entries are considered the same if their signature strings are exactly equal.
The span must be contiguous, meaning you may only choose entries between some left
index and right index without skipping any logs.

This problem models a common production debugging task: analysts often want the
longest time window without repeated event types so that they can study a "clean"
sequence of unique failures, warnings, and state changes.

Constraints:
- 1 <= events.length <= 100000
- 1 <= events[i].length <= 50
- events[i] consists of lowercase English letters, digits, underscores, and hyphens
- The answer must be computed in O(n) or O(n log n) time for full credit

Example 1:
Input: events = ["auth_ok", "cache_miss", "db_retry", "cache_miss", "email_sent"]
Output: 3

Example 2:
Input: events = ["x1", "x2", "x3", "x2", "x4", "x5"]
Output: 4
"""

from typing import Dict, List


class Solution:
    def length_of_longest_unique_span(self, events: List[str]) -> int:
        """
        Compute the length of the longest contiguous subarray containing only unique event signatures.

        Args:
            events: A list of log event signature strings in chronological order.

        Returns:
            The maximum length of a contiguous span where every signature appears at most once.

        Time Complexity:
            O(n), where n is the number of events, because each event is processed once.

        Space Complexity:
            O(k), where k is the number of distinct signatures stored in the hash map.
        """
        # This dictionary stores the most recent index where each event signature appeared.
        #
        # Example:
        # if last_seen["cache_miss"] == 3, that means the latest occurrence of
        # "cache_miss" we have processed so far is at index 3.
        #
        # Why use a dictionary?
        # - We need very fast lookup to know whether we have seen a signature before.
        # - A dictionary gives average O(1) lookup and update time.
        last_seen: Dict[str, int] = {}

        # 'left' marks the beginning of the current sliding window.
        #
        # The current window is always events[left:right+1].
        # We maintain the important invariant:
        #   "All signatures inside the current window are unique."
        #
        # We start with an empty window, so left begins at 0.
        left: int = 0

        # This variable stores the best answer found so far.
        # It will be updated whenever we find a longer valid window.
        max_length: int = 0

        # We expand the window one event at a time by moving 'right' from left to right.
        for right, signature in enumerate(events):
            # Step 1: Check whether the current signature has appeared before.
            #
            # If it has appeared, and its last seen position is inside the current window,
            # then adding this signature would create a duplicate in the window.
            #
            # Condition breakdown:
            # - signature in last_seen:
            #     means we have seen this signature somewhere earlier in the array.
            # - last_seen[signature] >= left:
            #     means that earlier occurrence is still inside the current window.
            #
            # If both are true, we must move 'left' to one position after the previous
            # occurrence to remove the duplicate from the window.
            if signature in last_seen and last_seen[signature] >= left:
                # Move the left boundary just past the previous occurrence.
                #
                # Why exactly last_seen[signature] + 1?
                # Because the old duplicate is at last_seen[signature], and we want the
                # new window to exclude it while remaining as large as possible.
                left = last_seen[signature] + 1

            # Step 2: Record/update the most recent index of the current signature.
            #
            # This must happen after handling the duplicate logic above, because now
            # this index becomes the newest known position for this signature.
            last_seen[signature] = right

            # Step 3: Compute the current window length.
            #
            # The current valid window is from index 'left' to index 'right', inclusive.
            # Length formula for an inclusive range:
            #   right - left + 1
            current_length: int = right - left + 1

            # Step 4: Update the best answer if the current window is larger.
            if current_length > max_length:
                max_length = current_length

        # After processing all events, max_length holds the longest valid span.
        return max_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    events1: List[str] = [
        "auth_ok",
        "cache_miss",
        "db_retry",
        "cache_miss",
        "email_sent",
    ]
    result1: int = solution.length_of_longest_unique_span(events1)
    print(result1)  # Expected: 3

    # Example 2
    events2: List[str] = ["x1", "x2", "x3", "x2", "x4", "x5"]
    result2: int = solution.length_of_longest_unique_span(events2)
    print(result2)  # Expected: 4

    # Additional quick sanity check
    events3: List[str] = ["a", "b", "c", "d"]
    result3: int = solution.length_of_longest_unique_span(events3)
    print(result3)  # Expected: 4