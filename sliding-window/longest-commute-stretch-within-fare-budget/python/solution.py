"""
Title: Longest Commute Stretch Within Fare Budget

Problem Description:
You are given an array fares where fares[i] is the transit fare paid on the i-th ride
of a commuter's travel history, and an integer budget. A commute stretch is any
contiguous group of rides. Your task is to find the maximum number of consecutive rides
whose total fare is less than or equal to budget.

In other words, among all subarrays of fares, return the length of the longest one
whose sum does not exceed budget.

This models a common analytics problem: given a daily or weekly ride log, determine
the longest uninterrupted sequence of rides that could have been covered by a fixed
reimbursement limit.

Return 0 if no single ride can fit within the budget.

Constraints:
- 1 <= fares.length <= 100000
- 1 <= fares[i] <= 10000
- 1 <= budget <= 1000000000
- All fares are positive integers

Because all fare values are positive, a sliding window solution can efficiently expand
and shrink a window while tracking the running sum.
"""

from typing import List


class Solution:
    def longest_commute_stretch(self, fares: List[int], budget: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is at most budget.

        This method uses the sliding window technique. Since all fare values are
        positive, expanding the window always increases or keeps the sum larger,
        and shrinking the window always decreases the sum. That property makes
        sliding window the ideal approach.

        Args:
            fares: A list of positive integers representing fare paid on each ride.
            budget: The maximum allowed total fare for a valid contiguous stretch.

        Returns:
            The length of the longest contiguous stretch whose total fare is
            less than or equal to budget.

        Time complexity:
            O(n), where n is the number of rides. Each element is added to the
            window once and removed from the window at most once.

        Space complexity:
            O(1), because only a few extra variables are used.
        """
        # This pointer marks the beginning of the current window.
        # The window will always represent a contiguous range:
        # fares[left:right + 1]
        left: int = 0

        # This variable stores the sum of all values currently inside the window.
        # Keeping a running sum avoids recalculating sums repeatedly, which would
        # be too slow for large input sizes.
        current_sum: int = 0

        # This variable stores the best (maximum) valid window length found so far.
        max_length: int = 0

        # We move the right pointer from left to right across the array.
        # At each step, we include fares[right] in the current window.
        for right in range(len(fares)):
            # Expand the window by adding the new fare at index "right".
            current_sum += fares[right]

            # If the current window sum is too large, it is invalid.
            # Because all fares are positive, the only way to make the sum smaller
            # is to move the left pointer forward and remove elements from the left.
            #
            # We keep shrinking until the window becomes valid again
            # (that is, until current_sum <= budget).
            while current_sum > budget and left <= right:
                # Remove the fare at the left edge from the running sum,
                # because that ride is no longer part of the window.
                current_sum -= fares[left]

                # Move the left edge one step to the right.
                left += 1

            # At this point, the window is valid:
            # sum(fares[left:right + 1]) <= budget
            #
            # So we compute its length and compare it with the best answer found so far.
            current_length: int = right - left + 1

            # Update the maximum length if this valid window is longer.
            if current_length > max_length:
                max_length = current_length

        # If no single fare fits within the budget, max_length will remain 0.
        # Otherwise, it will contain the length of the longest valid stretch.
        return max_length

    def solve(self, fares: List[int], budget: int) -> int:
        """
        Wrapper method to solve the problem.

        Args:
            fares: A list of positive integer fares.
            budget: The fare budget limit.

        Returns:
            The maximum number of consecutive rides whose total fare is
            less than or equal to budget.

        Time complexity:
            O(n), where n is the length of fares.

        Space complexity:
            O(1).
        """
        return self.longest_commute_stretch(fares, budget)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    fares1: List[int] = [2, 1, 3, 2, 1]
    budget1: int = 5
    result1: int = solution.solve(fares1, budget1)
    print("Example 1:")
    print(f"fares = {fares1}, budget = {budget1}")
    print(f"Output: {result1}")
    print("Expected: 2")
    print()

    # Example 2
    fares2: List[int] = [4, 2, 1, 1, 3]
    budget2: int = 6
    result2: int = solution.solve(fares2, budget2)
    print("Example 2:")
    print(f"fares = {fares2}, budget = {budget2}")
    print(f"Output: {result2}")
    print("Expected: 3")
    print()

    # Additional example: no single ride fits within budget
    fares3: List[int] = [7, 8, 9]
    budget3: int = 5
    result3: int = solution.solve(fares3, budget3)
    print("Additional Example:")
    print(f"fares = {fares3}, budget = {budget3}")
    print(f"Output: {result3}")
    print("Expected: 0")