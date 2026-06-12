"""
Title: Longest Focus Session Under Noise Budget

Problem Description:
You are given an array noise where noise[i] represents the noise level recorded
during the i-th minute of a student's study session. The student wants to choose
one contiguous block of minutes to study without taking a break. However, the
total noise during that chosen block must not exceed a given integer budget.

Your task is to return the maximum number of consecutive minutes the student can
study such that the sum of the noise levels in that contiguous block is less than
or equal to budget.

Return 0 if no single minute can be included without exceeding the budget.

Constraints:
- 1 <= noise.length <= 100000
- 0 <= noise[i] <= 10000
- 0 <= budget <= 1000000000
- The solution should run efficiently for large inputs.

Examples:
1.
Input: noise = [2, 1, 3, 2, 1], budget = 5
Output: 2

2.
Input: noise = [0, 2, 0, 1, 1, 0], budget = 3
Output: 4

Key Idea:
Because all noise values are non-negative, we can use a sliding window.
We expand the right side of the window to include more minutes, and whenever
the total noise becomes too large, we move the left side forward until the
window becomes valid again.
"""

from typing import List


class Solution:
    def longest_focus_session(self, noise: List[int], budget: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is at most budget.

        Args:
            noise: A list of non-negative integers where noise[i] is the noise level
                during the i-th minute.
            budget: The maximum allowed total noise for the chosen contiguous block.

        Returns:
            The maximum number of consecutive minutes whose total noise is less than
            or equal to budget.

        Time Complexity:
            O(n), where n is the length of noise.
            Each element is added to the window once and removed at most once.

        Space Complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # This pointer marks the beginning of our current sliding window.
        # The window will always represent a contiguous block:
        # noise[left:right + 1]
        left: int = 0

        # This variable stores the sum of all values currently inside the window.
        # We update it incrementally instead of recomputing sums repeatedly,
        # which keeps the algorithm efficient.
        current_sum: int = 0

        # This variable stores the best (maximum) valid window length found so far.
        max_length: int = 0

        # We move the right pointer from left to right across the array.
        # At each step, we try to include noise[right] in the current window.
        for right in range(len(noise)):
            # Expand the window by adding the new rightmost value.
            current_sum += noise[right]

            # If the window sum is now too large, we must shrink the window
            # from the left until the sum becomes valid again.
            #
            # Why is this correct?
            # Because all numbers are non-negative. That means adding more elements
            # can only keep the sum the same or increase it, never decrease it.
            # So if the current window is invalid, the only way to fix it is to
            # remove elements from the left.
            while current_sum > budget and left <= right:
                # Remove the leftmost element from the window sum.
                current_sum -= noise[left]

                # Move the left boundary one step to the right.
                left += 1

            # At this point, the window sum is guaranteed to be <= budget,
            # so the current window is valid.
            #
            # Its length is:
            # right - left + 1
            current_length: int = right - left + 1

            # Update the best answer if this valid window is longer than
            # any valid window we have seen before.
            if current_length > max_length:
                max_length = current_length

        # If no valid minute exists, max_length will remain 0.
        return max_length

    def maxConsecutiveMinutes(self, noise: List[int], budget: int) -> int:
        """
        Wrapper method matching an alternative interview-style naming convention.

        Args:
            noise: A list of non-negative noise levels.
            budget: Maximum allowed sum for a contiguous block.

        Returns:
            The maximum valid contiguous block length.

        Time Complexity:
            O(n), where n is the length of noise.

        Space Complexity:
            O(1).
        """
        return self.longest_focus_session(noise, budget)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # Valid windows with sum <= 5 include:
    # [2, 1] -> sum 3, length 2
    # [1, 3] -> sum 4, length 2
    # [3, 2] -> sum 5, length 2
    # [2, 1] -> sum 3, length 2
    # Any length 3 window exceeds 5.
    noise1: List[int] = [2, 1, 3, 2, 1]
    budget1: int = 5
    result1: int = solution.longest_focus_session(noise1, budget1)
    print(f"Example 1: noise = {noise1}, budget = {budget1}")
    print(f"Output: {result1}")
    print("Expected: 2")
    print()

    # Example 2
    # Check contiguous windows only:
    # [0, 2, 0, 1] -> sum 3, length 4
    # [0, 1, 1, 0] -> sum 2, length 4
    # Length 5 windows:
    # [0, 2, 0, 1, 1] -> sum 4 (invalid)
    # [2, 0, 1, 1, 0] -> sum 4 (invalid)
    # So the correct answer is 4.
    noise2: List[int] = [0, 2, 0, 1, 1, 0]
    budget2: int = 3
    result2: int = solution.longest_focus_session(noise2, budget2)
    print(f"Example 2: noise = {noise2}, budget = {budget2}")
    print(f"Output: {result2}")
    print("Expected: 4")
    print()

    # Additional edge case:
    # No single minute can be included because every value exceeds the budget.
    noise3: List[int] = [5, 6, 7]
    budget3: int = 4
    result3: int = solution.longest_focus_session(noise3, budget3)
    print(f"Edge Case: noise = {noise3}, budget = {budget3}")
    print(f"Output: {result3}")
    print("Expected: 0")