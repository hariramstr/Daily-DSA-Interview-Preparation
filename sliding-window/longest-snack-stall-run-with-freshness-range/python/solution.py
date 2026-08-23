"""
Title: Longest Snack Stall Run With Freshness Range
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array freshness where freshness[i] is the freshness score of the i-th
snack stall along a street. A tourist wants to visit a contiguous run of stalls, but
only if the stalls in that run are reasonably consistent in quality. A run is considered
valid if the difference between the maximum freshness score and the minimum freshness
score inside the run is at most limit.

Return the length of the longest valid contiguous run of stalls.

In other words, find the maximum size of a subarray freshness[l..r] such that
max(freshness[l..r]) - min(freshness[l..r]) <= limit.

This problem is intended to be solved efficiently for large inputs. A brute-force
solution that checks every subarray will be too slow. Think about how to maintain the
current window's minimum and maximum values while expanding and shrinking a sliding
window.

Constraints:
- 1 <= freshness.length <= 200000
- 0 <= freshness[i] <= 1000000000
- 0 <= limit <= 1000000000

Example 1:
Input: freshness = [4, 7, 6, 8, 5, 9], limit = 3
Output: 4
Explanation: One longest valid run is [7, 6, 8, 5]. Its maximum is 8 and minimum is 5,
so the difference is 3, which is allowed. No valid contiguous run has length greater
than 4.

Example 2:
Input: freshness = [10, 1, 2, 4, 7, 2], limit = 5
Output: 4
Explanation: The longest valid run is [2, 4, 7, 2]. Its maximum is 7 and minimum is 2,
so the difference is 5. Any longer window would exceed the allowed freshness range.
"""

from collections import deque
from typing import Deque, List


class Solution:
    def longest_subarray(self, freshness: List[int], limit: int) -> int:
        """
        Find the length of the longest contiguous subarray such that the difference
        between the maximum and minimum values in that subarray is at most limit.

        Args:
            freshness: List of freshness scores for snack stalls.
            limit: Maximum allowed difference between the largest and smallest score
                inside a valid window.

        Returns:
            The maximum length of a valid contiguous run.

        Time complexity:
            O(n), where n is the length of freshness.
            Each element is added to and removed from each deque at most once.

        Space complexity:
            O(n) in the worst case for the deques.
        """
        # This deque will store indices of elements in decreasing order of values.
        # Why decreasing?
        # - The front of the deque should always hold the index of the maximum value
        #   in the current window.
        # - If we keep values decreasing from front to back, then the first element
        #   is always the largest.
        max_deque: Deque[int] = deque()

        # This deque will store indices of elements in increasing order of values.
        # Why increasing?
        # - The front of the deque should always hold the index of the minimum value
        #   in the current window.
        # - If we keep values increasing from front to back, then the first element
        #   is always the smallest.
        min_deque: Deque[int] = deque()

        # left marks the start of the current sliding window.
        left: int = 0

        # best stores the maximum valid window length found so far.
        best: int = 0

        # We expand the window one element at a time using right.
        for right, value in enumerate(freshness):
            # ---------------------------------------------------------------
            # STEP 1: Insert the new element into max_deque.
            # ---------------------------------------------------------------
            # We want max_deque to remain decreasing by value.
            # If the new value is greater than values at the back, those smaller
            # values can never become the maximum while this new value remains in
            # the window, so we remove them.
            while max_deque and freshness[max_deque[-1]] < value:
                max_deque.pop()
            max_deque.append(right)

            # ---------------------------------------------------------------
            # STEP 2: Insert the new element into min_deque.
            # ---------------------------------------------------------------
            # We want min_deque to remain increasing by value.
            # If the new value is smaller than values at the back, those larger
            # values can never become the minimum while this new value remains in
            # the window, so we remove them.
            while min_deque and freshness[min_deque[-1]] > value:
                min_deque.pop()
            min_deque.append(right)

            # ---------------------------------------------------------------
            # STEP 3: Shrink the window while it is invalid.
            # ---------------------------------------------------------------
            # The current maximum is at freshness[max_deque[0]].
            # The current minimum is at freshness[min_deque[0]].
            # If their difference is greater than limit, the window is invalid.
            # We must move left forward until the window becomes valid again.
            while freshness[max_deque[0]] - freshness[min_deque[0]] > limit:
                # If the element leaving the window is currently the maximum,
                # remove it from the front of max_deque.
                if max_deque[0] == left:
                    max_deque.popleft()

                # If the element leaving the window is currently the minimum,
                # remove it from the front of min_deque.
                if min_deque[0] == left:
                    min_deque.popleft()

                # Move the left boundary rightward to shrink the window.
                left += 1

            # ---------------------------------------------------------------
            # STEP 4: Update the best answer.
            # ---------------------------------------------------------------
            # At this point, the window [left, right] is guaranteed to be valid.
            # Its length is right - left + 1.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    freshness_1: List[int] = [4, 7, 6, 8, 5, 9]
    limit_1: int = 3
    result_1: int = solution.longest_subarray(freshness_1, limit_1)
    print(f"Example 1 result: {result_1}")  # Expected: 4

    freshness_2: List[int] = [10, 1, 2, 4, 7, 2]
    limit_2: int = 5
    result_2: int = solution.longest_subarray(freshness_2, limit_2)
    print(f"Example 2 result: {result_2}")  # Expected: 4

    freshness_3: List[int] = [8]
    limit_3: int = 0
    result_3: int = solution.longest_subarray(freshness_3, limit_3)
    print(f"Additional example result: {result_3}")  # Expected: 1