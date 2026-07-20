"""
Title: Count Price Changes Between Consecutive Days

Problem Description:
You are given an integer array prices where prices[i] represents the price of the
same product on day i. Your task is to count how many times the price changed
compared with the previous day. A change is recorded whenever two consecutive
values are different. If the price stays the same from one day to the next, it
does not count as a change.

Return the total number of price changes across the entire array.

This problem models a simple analytics task often used in dashboards: instead of
caring about the size of a change, you only need to know how many transitions
happened. For example, if the prices are [5, 5, 7, 7, 6], then the changes happen
from 5 to 7 and from 7 to 6, so the answer is 2.

If the array has length 0 or 1, the answer is 0 because there are no consecutive
pairs to compare.

Constraints:
- 0 <= prices.length <= 100000
- -1000000000 <= prices[i] <= 1000000000

Example 1:
Input: prices = [10, 10, 12, 12, 9, 9, 11]
Output: 3
Explanation: Changes occur at 10 -> 12, 12 -> 9, and 9 -> 11.

Example 2:
Input: prices = [4, 4, 4, 4]
Output: 0
Explanation: Every consecutive pair is equal, so there are no price changes.
"""

from typing import List


class Solution:
    def count_price_changes(self, prices: List[int]) -> int:
        """
        Count how many times the price changes between consecutive days.

        Args:
            prices: A list of integers where prices[i] is the product price on day i.

        Returns:
            The total number of indices i such that prices[i] != prices[i - 1].

        Time complexity:
            O(n), where n is the length of prices, because we scan the list once.

        Space complexity:
            O(1), because we use only a few extra variables regardless of input size.
        """
        # If there are fewer than 2 prices, there are no consecutive pairs to compare.
        # That means no change can possibly be recorded.
        if len(prices) <= 1:
            return 0

        # This variable will store the total number of detected changes.
        # We start at 0 because before checking any pair, we have not found any transitions.
        changes: int = 0

        # We begin from index 1 because each day's price must be compared with the previous day.
        # Index 0 has no previous element, so it cannot form a consecutive pair by itself.
        for i in range(1, len(prices)):
            # Compare the current day's price with the previous day's price.
            # If they are different, that means the price changed from one day to the next.
            if prices[i] != prices[i - 1]:
                # Increase the count by 1 for this detected transition.
                changes += 1

        # After checking every consecutive pair exactly once, return the total count.
        return changes


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [10, 10, 12, 12, 9, 9, 11],
        [4, 4, 4, 4],
        [5, 5, 7, 7, 6],
        [],
        [42],
    ]

    for prices in sample_inputs:
        result = solution.count_price_changes(prices)
        print(f"prices = {prices} -> changes = {result}")