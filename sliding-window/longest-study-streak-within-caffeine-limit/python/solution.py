"""
Title: Longest Study Streak Within Caffeine Limit

Problem Description:
You are given an array `cups` where `cups[i]` represents the amount of caffeine
consumed during the `i`-th study session of a day. A student wants to find the
longest consecutive stretch of sessions they can keep without their total caffeine
intake exceeding a safety limit `limit`.

Return the length of the longest contiguous subarray whose sum is less than or equal
to `limit`.

A contiguous subarray means the sessions must be consecutive in the original order.
If no single session can fit within the limit, return `0`.

This problem is designed to be solved efficiently using the sliding window technique.
Since all values in `cups` are non-negative, once the current window exceeds the
limit, moving the left side of the window forward is enough to restore validity.
An `O(n)` solution is expected.

Constraints:
- 1 <= cups.length <= 100000
- 0 <= cups[i] <= 10000
- 0 <= limit <= 1000000000

Example 1:
Input: cups = [2, 1, 3, 2, 1], limit = 5
Output: 2
Explanation: Valid longest windows include [2,1], [3,2], and [2,1].
Any window of length 3 has sum greater than 5.

Example 2:
Input: cups = [0, 1, 1, 0, 2], limit = 2
Output: 4
Explanation: The window [0,1,1,0] has total caffeine 2, so its length is 4.
Extending it by the last value makes the sum 4, which is too large.
"""

from typing import List


class Solution:
    def longest_study_streak(self, cups: List[int], limit: int) -> int:
        """
        Find the length of the longest contiguous subarray whose sum is
        less than or equal to the given limit.

        Args:
            cups: A list of non-negative integers where each value represents
                caffeine consumed in one study session.
            limit: The maximum allowed total caffeine for a valid consecutive stretch.

        Returns:
            The maximum length of a contiguous subarray with sum <= limit.

        Time Complexity:
            O(n), where n is the length of cups. Each element is added to the
            window once and removed from the window at most once.

        Space Complexity:
            O(1), because only a few extra variables are used.
        """
        # `left` marks the beginning of the current sliding window.
        # We will move it to the right only when the current window becomes invalid
        # (that is, when the sum becomes greater than `limit`).
        left: int = 0

        # `current_sum` stores the total caffeine inside the current window
        # from index `left` to index `right`.
        current_sum: int = 0

        # `best_length` stores the longest valid window length found so far.
        best_length: int = 0

        # We expand the window one session at a time by moving `right`.
        # At each step:
        # 1. Add cups[right] into the current window sum.
        # 2. If the sum is too large, shrink the window from the left
        #    until the sum becomes valid again.
        # 3. Once valid, update the best answer using the current window size.
        for right in range(len(cups)):
            # Expand the window to include the new session at index `right`.
            current_sum += cups[right]

            # Because all values are non-negative, if the sum is too large,
            # the only way to make it smaller is to remove values from the left.
            # This is exactly why the sliding window technique works here.
            while current_sum > limit and left <= right:
                # Remove the leftmost session from the current window.
                current_sum -= cups[left]

                # Move the left boundary one step to the right.
                left += 1

            # At this point, the window sum is guaranteed to be <= limit,
            # so the current window is valid.
            #
            # Its length is:
            #   right - left + 1
            #
            # We compare it with the best answer seen so far.
            current_length: int = right - left + 1
            if current_length > best_length:
                best_length = current_length

        # If no single session fits within the limit, then every time we add a value
        # larger than the limit, the while-loop will shrink the window until it becomes
        # empty, producing length 0 for that step. Therefore `best_length` will remain 0,
        # which matches the required behavior.
        return best_length

    def longestSubarrayWithinLimit(self, cups: List[int], limit: int) -> int:
        """
        Wrapper method using an alternative interview-style name.

        Args:
            cups: A list of non-negative integers representing caffeine per session.
            limit: The maximum allowed sum for a valid contiguous subarray.

        Returns:
            The length of the longest valid contiguous subarray.

        Time Complexity:
            O(n), where n is the number of sessions.

        Space Complexity:
            O(1).
        """
        return self.longest_study_streak(cups, limit)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # cups = [2, 1, 3, 2, 1], limit = 5
    # Valid longest windows have length 2.
    cups1: List[int] = [2, 1, 3, 2, 1]
    limit1: int = 5
    result1: int = solution.longest_study_streak(cups1, limit1)
    print(f"Example 1 result: {result1}")  # Expected: 2

    # Example 2:
    # cups = [0, 1, 1, 0, 2], limit = 2
    # The longest valid window is [0, 1, 1, 0] with length 4.
    cups2: List[int] = [0, 1, 1, 0, 2]
    limit2: int = 2
    result2: int = solution.longest_study_streak(cups2, limit2)
    print(f"Example 2 result: {result2}")  # Expected: 4

    # Additional sample:
    # No single session fits within the limit.
    cups3: List[int] = [5, 6, 7]
    limit3: int = 4
    result3: int = solution.longest_study_streak(cups3, limit3)
    print(f"Additional example result: {result3}")  # Expected: 0

    # Additional sample:
    # Entire array fits.
    cups4: List[int] = [1, 1, 1, 1]
    limit4: int = 10
    result4: int = solution.longest_study_streak(cups4, limit4)
    print(f"Additional example result: {result4}")  # Expected: 4