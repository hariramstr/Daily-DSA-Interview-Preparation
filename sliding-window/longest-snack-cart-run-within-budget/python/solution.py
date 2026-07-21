"""
Title: Longest Snack Cart Run Within Budget

Problem Description:
A company campus has a row of snack carts, and each cart has a fixed price for buying
one item. You are given an integer array prices where prices[i] is the cost at the
i-th cart, and an integer budget. During a break, an employee wants to visit a
contiguous sequence of carts and buy exactly one item from each cart in that sequence.

Your task is to return the maximum number of consecutive carts the employee can include
without the total cost exceeding budget.

In other words, find the length of the longest contiguous subarray whose sum is less
than or equal to budget.

This problem is designed to be solved efficiently using a sliding window. Since all
prices are positive, once the current window exceeds the budget, moving the left side
forward is guaranteed to reduce the total.

Constraints:
- 1 <= prices.length <= 100000
- 1 <= prices[i] <= 10000
- 1 <= budget <= 1000000000

Example 1:
Input: prices = [2, 1, 3, 2, 1], budget = 6
Output: 3

Example 2:
Input: prices = [5, 2, 2, 1, 4], budget = 5
Output: 2
"""

from typing import List


class Solution:
    def longest_affordable_run(self, prices: List[int], budget: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is <= budget.

        This uses the sliding window technique. Because all values in prices are
        positive, expanding the window increases or keeps the sum larger, and
        shrinking the window decreases the sum. That property allows us to move
        each pointer at most once for an efficient linear-time solution.

        Args:
            prices: A list of positive integers where each value is the cost at a cart.
            budget: The maximum allowed total cost for a contiguous sequence of carts.

        Returns:
            The maximum number of consecutive carts that can be included without
            exceeding the budget.

        Time complexity:
            O(n), where n is the length of prices, because each element is added
            to the window once and removed from the window at most once.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # The left pointer marks the beginning of the current sliding window.
        # The window will always represent a contiguous segment:
        # prices[left:right + 1]
        left: int = 0

        # current_sum stores the total cost of the current window.
        # We update it incrementally instead of recomputing sums repeatedly,
        # which keeps the algorithm efficient.
        current_sum: int = 0

        # best_length stores the longest valid window length found so far.
        best_length: int = 0

        # We move the right pointer from left to right across the array.
        # At each step, we try to include prices[right] in the current window.
        for right in range(len(prices)):
            # Expand the window by adding the new rightmost element.
            current_sum += prices[right]

            # If the total cost is now too large, the current window is invalid.
            # Because all prices are positive, the only way to reduce the sum
            # is to move the left pointer to the right and remove elements.
            #
            # We keep shrinking until the window becomes valid again
            # (that is, until current_sum <= budget).
            while current_sum > budget and left <= right:
                current_sum -= prices[left]
                left += 1

            # At this point, the window from left to right is valid:
            # its sum is <= budget.
            #
            # We compute its length and compare it with the best answer seen so far.
            current_length: int = right - left + 1
            if current_length > best_length:
                best_length = current_length

        # If no single element fits within the budget, best_length will remain 0.
        return best_length

    def max_length_within_budget(self, prices: List[int], budget: int) -> int:
        """
        Wrapper method that returns the longest affordable contiguous run.

        This method exists to provide an alternative descriptive name and to keep
        the public interface beginner-friendly.

        Args:
            prices: A list of positive cart prices.
            budget: The maximum total allowed for a contiguous segment.

        Returns:
            The maximum valid contiguous segment length.

        Time complexity:
            O(n), where n is the number of carts.

        Space complexity:
            O(1).
        """
        return self.longest_affordable_run(prices, budget)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # prices = [2, 1, 3, 2, 1], budget = 6
    # Valid longest segment length is 3, for example [1, 3, 2].
    prices1: List[int] = [2, 1, 3, 2, 1]
    budget1: int = 6
    result1: int = solution.max_length_within_budget(prices1, budget1)
    print(result1)  # Expected: 3

    # Example 2:
    # prices = [5, 2, 2, 1, 4], budget = 5
    # Valid longest segment length is 2, for example [2, 2] or [1, 4].
    prices2: List[int] = [5, 2, 2, 1, 4]
    budget2: int = 5
    result2: int = solution.max_length_within_budget(prices2, budget2)
    print(result2)  # Expected: 2

    # Additional example:
    # Every single cart is too expensive, so answer should be 0.
    prices3: List[int] = [7, 8, 9]
    budget3: int = 5
    result3: int = solution.max_length_within_budget(prices3, budget3)
    print(result3)  # Expected: 0