"""
Title: Longest Cooking Streak Within Spice Budget

Problem Description:
You are given an array heat of length n, where heat[i] represents the spice level
added by the i-th dish cooked in order during a live kitchen session. A chef wants
to select one contiguous streak of dishes to present as a tasting sequence. The
total spice used in that streak must not exceed a given integer budget. Your task
is to return the maximum number of consecutive dishes the chef can include.

Formally, find the length of the longest contiguous subarray whose sum is less than
or equal to budget.

This problem is designed so that an efficient sliding window solution is expected.
Since all spice values are non-negative, expanding the right end of the window can
only increase or keep the current sum the same, and shrinking from the left reduces
it when the budget is exceeded.

Constraints:
- 1 <= n <= 200000
- 0 <= heat[i] <= 100000
- 0 <= budget <= 10^15
- The answer always fits in a 32-bit signed integer.

Example 1:
Input: heat = [2, 1, 3, 2, 1, 1], budget = 5
Output: 3

Example 2:
Input: heat = [0, 4, 0, 2, 1, 0, 1], budget = 3
Output: 4

Return only the maximum length of such a contiguous streak.
"""

from typing import List


class Solution:
    def longest_cooking_streak(self, heat: List[int], budget: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is <= budget.

        This method uses the sliding window / two-pointer technique. Because all
        values in `heat` are non-negative, when we move the right pointer forward,
        the window sum never decreases. If the sum becomes too large, we can safely
        move the left pointer forward until the window becomes valid again.

        Args:
            heat: A list of non-negative integers representing spice levels.
            budget: The maximum allowed sum for the chosen contiguous streak.

        Returns:
            The length of the longest contiguous subarray with sum <= budget.

        Time complexity:
            O(n), where n is the length of `heat`, because each index is visited
            at most twice: once by the right pointer and once by the left pointer.

        Space complexity:
            O(1), because only a few extra variables are used.
        """
        # `left` marks the start of the current window.
        # The current window will always be heat[left:right+1].
        left: int = 0

        # `current_sum` stores the total spice level inside the current window.
        current_sum: int = 0

        # `best_length` stores the maximum valid window length seen so far.
        best_length: int = 0

        # We expand the window one element at a time by moving `right`.
        for right in range(len(heat)):
            # Step 1: include the new dish at index `right` into the window.
            # This grows the window from the right side.
            current_sum += heat[right]

            # Step 2: if the window is invalid (sum > budget), shrink it from the left.
            # We keep removing elements from the left until the sum is valid again.
            #
            # Why this works:
            # - All values are non-negative.
            # - So adding more elements can only increase or keep the sum the same.
            # - If the sum is too large, the only way to fix it is to remove elements.
            while current_sum > budget and left <= right:
                current_sum -= heat[left]
                left += 1

            # Step 3: now the window is valid again, so we can measure its length.
            # The current valid window is from `left` to `right`, inclusive.
            current_length: int = right - left + 1

            # Step 4: update the best answer if this valid window is longer.
            if current_length > best_length:
                best_length = current_length

        # After checking all possible right endpoints, `best_length` is the answer.
        return best_length

    def max_consecutive_dishes(self, heat: List[int], budget: int) -> int:
        """
        Wrapper method that returns the maximum valid contiguous streak length.

        This method exists to provide an alternative descriptive name while using
        the same sliding window implementation.

        Args:
            heat: A list of non-negative integers representing spice levels.
            budget: The maximum allowed total spice for the streak.

        Returns:
            The maximum number of consecutive dishes that fit within the budget.

        Time complexity:
            O(n), where n is the length of `heat`.

        Space complexity:
            O(1).
        """
        return self.longest_cooking_streak(heat, budget)


if __name__ == "__main__":
    solution = Solution()

    # Sample Input 1
    # Valid windows with sum <= 5 include:
    # - [2, 1] sum 3 length 2
    # - [3, 2] sum 5 length 2
    # - [2, 1, 1] sum 4 length 3
    # The best length is 3.
    heat1: List[int] = [2, 1, 3, 2, 1, 1]
    budget1: int = 5
    result1: int = solution.longest_cooking_streak(heat1, budget1)
    print(result1)  # Expected: 3

    # Sample Input 2
    # One best valid window is [0, 2, 1, 0] with sum 3 and length 4.
    # So the answer is 4.
    heat2: List[int] = [0, 4, 0, 2, 1, 0, 1]
    budget2: int = 3
    result2: int = solution.longest_cooking_streak(heat2, budget2)
    print(result2)  # Expected: 4

    # Additional quick sanity checks
    heat3: List[int] = [1, 1, 1, 1]
    budget3: int = 2
    print(solution.max_consecutive_dishes(heat3, budget3))  # Expected: 2

    heat4: List[int] = [0, 0, 0]
    budget4: int = 0
    print(solution.max_consecutive_dishes(heat4, budget4))  # Expected: 3