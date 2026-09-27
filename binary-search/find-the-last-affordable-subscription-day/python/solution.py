"""
Title: Find the Last Affordable Subscription Day

Problem Description:
A streaming platform increases the price of its premium plan over time. You are given
a sorted integer array `prices` where `prices[i]` is the subscription price on day `i`,
and the values are in non-decreasing order. You are also given an integer `budget`
representing the maximum amount a customer is willing to pay.

Your task is to return the index of the last day on which the subscription price is
less than or equal to `budget`. If every price is greater than `budget`, return `-1`.

Because the array is already sorted, an efficient solution should use binary search
instead of scanning every day. This is a classic “find the rightmost valid position”
problem. Be careful when there are repeated prices: if multiple days have the same
affordable price, you must return the largest index among them.

Constraints:
- 1 <= prices.length <= 100000
- 0 <= prices[i] <= 1000000000
- prices is sorted in non-decreasing order
- 0 <= budget <= 1000000000

Example 1:
Input: prices = [5, 7, 7, 10, 14], budget = 7
Output: 2
Explanation: Days 0, 1, and 2 are affordable. The last affordable day is index 2.

Example 2:
Input: prices = [4, 6, 9, 12], budget = 3
Output: -1
Explanation: No day has a price less than or equal to the budget, so the answer is -1.
"""

from typing import List


class Solution:
    def last_affordable_day(self, prices: List[int], budget: int) -> int:
        """
        Find the index of the last day whose price is less than or equal to budget.

        This method uses binary search on the sorted `prices` array to efficiently
        locate the rightmost index where prices[index] <= budget.

        Args:
            prices: A sorted list of non-decreasing subscription prices by day.
            budget: The maximum price the customer is willing to pay.

        Returns:
            The largest index i such that prices[i] <= budget, or -1 if no such
            index exists.

        Time complexity:
            O(log n), where n is the number of days in `prices`.

        Space complexity:
            O(1), because only a constant amount of extra space is used.
        """
        # `left` and `right` define the current search range inside the array.
        # We begin by searching the entire array.
        left: int = 0
        right: int = len(prices) - 1

        # `answer` stores the best valid index found so far.
        # We initialize it to -1 because if no price is affordable,
        # the required result is -1.
        answer: int = -1

        # Continue searching while there is still a valid range to inspect.
        while left <= right:
            # Compute the middle index of the current search range.
            # This is the standard binary search split point.
            mid: int = left + (right - left) // 2

            # If the price at `mid` is affordable, then `mid` is a valid candidate.
            if prices[mid] <= budget:
                # Record this index as the current best answer.
                # We do this because we are looking for the LAST affordable day,
                # so any valid index could be part of the answer.
                answer = mid

                # Since `mid` is affordable, there may be another affordable day
                # further to the right. To find the rightmost valid index, we move
                # the search to the right half.
                left = mid + 1
            else:
                # If prices[mid] is greater than budget, then `mid` is not valid,
                # and neither is anything to its right guaranteed to help us,
                # because the array is sorted in non-decreasing order.
                #
                # Therefore, we discard the right half including `mid` and continue
                # searching only in the left half.
                right = mid - 1

        # When the loop ends, `answer` contains the rightmost affordable index found,
        # or -1 if no affordable price existed.
        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # prices = [5, 7, 7, 10, 14], budget = 7
    # Affordable prices are at indices 0, 1, and 2.
    # The last affordable day should be index 2.
    prices1: List[int] = [5, 7, 7, 10, 14]
    budget1: int = 7
    result1: int = solution.last_affordable_day(prices1, budget1)
    print(f"prices = {prices1}, budget = {budget1} -> {result1}")

    # Example 2:
    # prices = [4, 6, 9, 12], budget = 3
    # No price is less than or equal to 3, so the answer should be -1.
    prices2: List[int] = [4, 6, 9, 12]
    budget2: int = 3
    result2: int = solution.last_affordable_day(prices2, budget2)
    print(f"prices = {prices2}, budget = {budget2} -> {result2}")

    # Additional beginner-friendly test:
    # All days are affordable, so the answer should be the last index.
    prices3: List[int] = [2, 2, 3, 3, 5]
    budget3: int = 10
    result3: int = solution.last_affordable_day(prices3, budget3)
    print(f"prices = {prices3}, budget = {budget3} -> {result3}")