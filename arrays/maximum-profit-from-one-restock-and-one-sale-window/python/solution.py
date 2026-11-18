"""
Title: Maximum Profit from One Restock and One Sale Window

Problem Description:
You are given an integer array prices where prices[i] is the market price of a product on day i.
A retailer wants to choose exactly one contiguous restock window and one contiguous sale window
such that the sale window starts strictly after the restock window ends.

The retailer buys one unit of product on every day in the restock window and sells one unit on
every day in the sale window.

The profit of this plan is defined as:
    (sum of prices in the sale window) - (sum of prices in the restock window)

Your task is to return the maximum possible profit.

Both windows must be:
- non-empty
- contiguous
- non-overlapping
- and the sale window must start strictly after the restock window ends

You are not required to use all days.

If every valid plan produces a negative profit, return the least negative value among all valid plans.

Constraints:
- 2 <= prices.length <= 200000
- -100000 <= prices[i] <= 100000
- The answer fits in a 64-bit signed integer
"""

from typing import List


class Solution:
    def max_profit(self, prices: List[int]) -> int:
        """
        Compute the maximum profit from one contiguous restock window followed by
        one contiguous sale window.

        The profit is:
            sum(sale_window) - sum(restock_window)

        The restock window must end before the sale window begins.

        Args:
            prices: List of daily market prices.

        Returns:
            The maximum possible profit as an integer.

        Time complexity:
            O(n), where n is the number of days.

        Space complexity:
            O(n), due to the suffix array storing the best sale-window sum
            starting at or after each index.
        """
        n: int = len(prices)

        # ------------------------------------------------------------
        # Step 1: Build a suffix helper array for the "sale window".
        #
        # We want, for every index i, the best possible sum of any
        # non-empty contiguous subarray that starts at or after i.
        #
        # Why do we need this?
        # Because if the restock window ends at day e, then the sale
        # window must start at day e + 1 or later.
        #
        # So while scanning possible restock windows, we need to quickly
        # know:
        #   "What is the maximum subarray sum available in the suffix
        #    starting from the next day?"
        #
        # To answer that in O(1) per split point, we precompute:
        #   best_sale_from[i] = maximum sum of any non-empty contiguous
        #                       subarray fully contained in prices[i:].
        #
        # We compute this using a right-to-left Kadane-style process:
        #
        #   max_starting_here = best sum of a subarray that MUST start at i
        #                     = prices[i] + max(0, max_starting_here at i+1)
        #
        # Then:
        #   best_sale_from[i] = max(best_sale_from[i+1], max_starting_here)
        #
        # This means:
        # - either the best sale window starts exactly at i
        # - or it starts later than i
        # ------------------------------------------------------------
        best_sale_from: List[int] = [0] * n

        max_starting_here: int = prices[-1]
        best_sale_from[-1] = prices[-1]

        for i in range(n - 2, -1, -1):
            # Best subarray sum that is forced to start at i.
            # If extending to the right helps, include it.
            # Otherwise, start fresh at i.
            max_starting_here = prices[i] + max(0, max_starting_here)

            # Best subarray sum anywhere in suffix i..n-1.
            best_sale_from[i] = max(best_sale_from[i + 1], max_starting_here)

        # ------------------------------------------------------------
        # Step 2: Scan left-to-right to evaluate all possible restock
        # windows ending before a valid sale window.
        #
        # We need the minimum sum of any non-empty contiguous subarray
        # ending at the current index, because the retailer wants the
        # restock cost to be as small as possible.
        #
        # Let:
        #   min_ending_here = minimum sum of a non-empty contiguous
        #                     subarray that MUST end at current index
        #
        # This is the "minimum subarray" version of Kadane's algorithm:
        #
        #   min_ending_here = prices[i] + min(0, previous min_ending_here)
        #
        # Why?
        # - If the previous minimum-ending subarray is negative, extending
        #   it makes the current sum even smaller, which is good for a
        #   restock cost we want to minimize.
        # - If it is positive, we should start a new restock window at i.
        #
        # For each possible restock end index i, the sale window must start
        # at i+1 or later, so the best profit using a restock window ending
        # at i is:
        #
        #   best_sale_from[i+1] - min_subarray_ending_at_i
        #
        # We maximize this over all i from 0 to n-2.
        # ------------------------------------------------------------
        min_ending_here: int = prices[0]
        answer: int = best_sale_from[1] - min_ending_here

        for i in range(1, n - 1):
            # Update the minimum-sum subarray that must end at day i.
            # Either:
            # - extend the previous minimum-ending subarray, or
            # - start a new subarray at i
            min_ending_here = prices[i] + min(0, min_ending_here)

            # The sale window must begin strictly after the restock window ends,
            # so it must lie in the suffix starting at i + 1.
            current_profit: int = best_sale_from[i + 1] - min_ending_here

            # Keep the best profit seen so far.
            if current_profit > answer:
                answer = current_profit

        return answer

    def maximumProfit(self, prices: List[int]) -> int:
        """
        Wrapper method using camelCase naming.

        Args:
            prices: List of daily market prices.

        Returns:
            The maximum possible profit.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        return self.max_profit(prices)


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [4, -2, 3, -1, 5],
        [7, 6, 5, 4],
    ]

    for prices in sample_inputs:
        result = solution.maximumProfit(prices)
        print(f"prices = {prices}")
        print(f"maximum profit = {result}")
        print()