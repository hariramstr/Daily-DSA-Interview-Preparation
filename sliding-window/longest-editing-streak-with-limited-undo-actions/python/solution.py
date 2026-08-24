"""
Title: Longest Editing Streak With Limited Undo Actions

Problem Description:
You are given an array `events` representing a user's editing timeline in a document editor.
Each element is either `1` or `0`:

- `1` means the user made a productive edit during that minute.
- `0` means the minute was an undo, rollback, or other non-productive action.

The product team wants to measure the longest continuous editing streak that can be
considered "mostly productive." A streak is valid if it contains at most `k`
non-productive minutes. In other words, you may include up to `k` zeros inside the
chosen contiguous subarray.

Return the length of the longest valid contiguous streak.

This problem models a common analytics task where a noisy activity stream must be
summarized while tolerating a limited number of interruptions. A correct solution
should run efficiently on large inputs, so approaches that check every subarray
will be too slow.

Constraints:
- 1 <= events.length <= 200000
- 0 <= k <= events.length
- events[i] is either 0 or 1

Example 1:
Input: events = [1,1,0,1,0,1,1,1], k = 1
Output: 4

Example 2:
Input: events = [0,1,1,0,1,1,0,1], k = 2
Output: 7

Task:
Compute the maximum length of any contiguous subarray containing at most `k` zeros.
"""

from typing import List


class Solution:
    def longest_editing_streak(self, events: List[int], k: int) -> int:
        """
        Find the maximum length of a contiguous subarray containing at most k zeros.

        This uses the sliding window technique:
        - Expand the right side of the window one step at a time.
        - Count how many zeros are currently inside the window.
        - If the window becomes invalid (too many zeros), move the left side forward
          until the window becomes valid again.
        - Track the largest valid window length seen during the scan.

        Args:
            events: A list of integers where each value is either 0 or 1.
            k: The maximum number of zeros allowed in a valid contiguous subarray.

        Returns:
            The length of the longest contiguous subarray with at most k zeros.

        Time complexity:
            O(n), where n is the length of events, because each pointer moves
            from left to right at most once.

        Space complexity:
            O(1), because only a few integer variables are used.
        """
        # `left` marks the beginning of the current sliding window.
        # The window will always represent a contiguous segment: events[left:right+1].
        left: int = 0

        # `zero_count` stores how many zeros are currently inside the window.
        # This is the key piece of information needed to decide whether the
        # current window is valid or invalid.
        zero_count: int = 0

        # `best_length` stores the maximum valid window size found so far.
        best_length: int = 0

        # We move `right` from the start of the array to the end.
        # At each step, we include events[right] into the current window.
        for right in range(len(events)):
            # If the newly added element is 0, then the number of non-productive
            # minutes inside the window increases by one.
            if events[right] == 0:
                zero_count += 1

            # If zero_count is now greater than k, the window is invalid.
            # We must shrink it from the left until it becomes valid again.
            #
            # Why a while loop instead of an if statement?
            # Because moving left by one position may still leave too many zeros
            # in the window. We keep shrinking until the condition is satisfied.
            while zero_count > k:
                # If the element leaving the window is a zero, then the number
                # of zeros inside the window decreases.
                if events[left] == 0:
                    zero_count -= 1

                # Move the left boundary one step to the right, effectively
                # removing events[left] from the window.
                left += 1

            # At this point, the window events[left:right+1] is guaranteed valid:
            # it contains at most k zeros.
            #
            # Compute its length:
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger than any
            # previously seen valid window.
            if current_length > best_length:
                best_length = current_length

        # After scanning the full array, best_length is the answer.
        return best_length

    def longestOnes(self, events: List[int], k: int) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            events: A list of integers where each value is either 0 or 1.
            k: The maximum number of zeros allowed in a valid contiguous subarray.

        Returns:
            The length of the longest contiguous subarray with at most k zeros.

        Time complexity:
            O(n), where n is the length of events.

        Space complexity:
            O(1).
        """
        return self.longest_editing_streak(events, k)


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1 from the problem statement.
    events1: List[int] = [1, 1, 0, 1, 0, 1, 1, 1]
    k1: int = 1
    result1: int = solution.longest_editing_streak(events1, k1)
    print("Example 1:")
    print("events =", events1)
    print("k =", k1)
    print("Longest valid streak length =", result1)
    print("Expected =", 4)
    print()

    # Example 2 from the problem statement.
    events2: List[int] = [0, 1, 1, 0, 1, 1, 0, 1]
    k2: int = 2
    result2: int = solution.longest_editing_streak(events2, k2)
    print("Example 2:")
    print("events =", events2)
    print("k =", k2)
    print("Longest valid streak length =", result2)
    print("Expected =", 7)
    print()

    # Additional beginner-friendly sanity checks.
    extra_tests: List[tuple[List[int], int]] = [
        ([1, 1, 1, 1], 0),          # all productive
        ([0, 0, 0, 0], 2),          # only zeros, limited allowance
        ([1, 0, 1, 0, 1], 0),       # no zeros allowed
        ([1], 0),                   # single element
        ([0], 1),                   # single zero allowed
        ([1, 0, 1, 1, 0, 1], 2),    # mixed values
    ]

    print("Additional tests:")
    for events, k in extra_tests:
        print(
            f"events = {events}, k = {k}, "
            f"longest valid streak = {solution.longest_editing_streak(events, k)}"
        )