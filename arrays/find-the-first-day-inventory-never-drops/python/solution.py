"""
Title: Find the First Day Inventory Never Drops

Problem Description:
You are given an integer array stock where stock[i] represents the inventory level
of a product at the end of day i. A day is considered stable if its inventory is
greater than or equal to the inventory of the previous day. Your task is to return
the index of the first day from which the inventory never drops again for the rest
of the array.

More formally, find the smallest index i such that for every j where i < j < n,
stock[j] >= stock[j - 1]. In other words, the subarray stock[i...n-1] must be
non-decreasing. If the entire array is already non-decreasing, return 0.

This problem models a real inventory dashboard where managers want to know the first
day after which stock levels stop declining and only stay the same or increase.

Constraints:
- 1 <= stock.length <= 100000
- -1000000000 <= stock[i] <= 1000000000
- The answer is always a valid index from 0 to n - 1

Example 1:
Input: stock = [9, 7, 8, 8, 10]
Output: 1
Explanation: Starting from index 1, the values are [7, 8, 8, 10], which is
non-decreasing. Index 0 does not work because 7 < 9, so inventory drops between
day 0 and day 1.

Example 2:
Input: stock = [5, 6, 4, 7, 9]
Output: 2
Explanation: The suffix starting at index 2 is [4, 7, 9], which is non-decreasing.
Any earlier starting index fails because there is a drop from 6 to 4.

Goal:
Return the smallest index from which the remaining suffix of the array is
non-decreasing.
"""

from typing import List


class Solution:
    def first_stable_day(self, stock: List[int]) -> int:
        """
        Find the earliest index such that the suffix starting there is non-decreasing.

        The key idea is to scan from right to left. While moving leftward, we check
        whether each adjacent pair keeps the suffix non-decreasing:
        stock[i] <= stock[i + 1].

        If this condition holds, then the suffix starting at i is still valid.
        If it fails, then i cannot be part of the final non-decreasing suffix, so
        the earliest valid starting point must be i + 1.

        Args:
            stock: List of inventory values by day.

        Returns:
            The smallest index from which inventory never drops again.

        Time complexity:
            O(n), where n is the length of stock, because we scan the array once.

        Space complexity:
            O(1), because we use only a few extra variables.
        """
        n: int = len(stock)

        # If there is only one day, then that single day trivially forms a
        # non-decreasing suffix by itself, because there are no later days where
        # a drop could happen.
        if n == 1:
            return 0

        # We begin by assuming the last day is the start of a valid suffix.
        # This is always true because a suffix of length 1 cannot violate the
        # non-decreasing rule.
        start_index: int = n - 1

        # We now scan from right to left, examining each pair:
        # stock[i] and stock[i + 1].
        #
        # Why scan from right to left?
        # Because we want to know how far left we can extend a suffix that is
        # already known to be non-decreasing.
        #
        # At every step:
        # - If stock[i] <= stock[i + 1], then adding stock[i] to the front of the
        #   current valid suffix keeps it non-decreasing.
        # - Otherwise, there is a drop from day i to day i + 1, so the suffix
        #   cannot start at i or any earlier index that includes this drop.
        #   Therefore, the earliest valid suffix start must be i + 1.
        for i in range(n - 2, -1, -1):
            # Check whether the sequence does NOT drop at this boundary.
            if stock[i] <= stock[i + 1]:
                # Since stock[i] is less than or equal to the next value,
                # the suffix starting at i is still non-decreasing.
                # So we can safely move the valid start one step left.
                start_index = i
            else:
                # We found a drop: stock[i] > stock[i + 1].
                #
                # This means any suffix starting at i would immediately fail,
                # because the first adjacent pair inside that suffix already
                # decreases.
                #
                # Since we are scanning from right to left, the current
                # start_index already points to the earliest valid start for the
                # suffix to the right. Once we hit a drop, we cannot extend
                # farther left across it.
                #
                # Therefore, we stop immediately and return i + 1, which is the
                # first day after the drop.
                return i + 1

        # If we finish the loop without finding any drop, then the entire array
        # is already non-decreasing, so the answer is 0.
        return start_index


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the problem statement.
    stock1: List[int] = [9, 7, 8, 8, 10]
    result1: int = solution.first_stable_day(stock1)
    print(f"stock = {stock1}")
    print(f"First stable day index: {result1}")
    print("Expected: 1")
    print()

    # Sample input 2 from the problem statement.
    stock2: List[int] = [5, 6, 4, 7, 9]
    result2: int = solution.first_stable_day(stock2)
    print(f"stock = {stock2}")
    print(f"First stable day index: {result2}")
    print("Expected: 2")
    print()

    # Additional beginner-friendly test cases.

    # Entire array is already non-decreasing.
    stock3: List[int] = [1, 2, 2, 3, 5]
    result3: int = solution.first_stable_day(stock3)
    print(f"stock = {stock3}")
    print(f"First stable day index: {result3}")
    print("Expected: 0")
    print()

    # Strictly decreasing array: only the last day works.
    stock4: List[int] = [5, 4, 3, 2, 1]
    result4: int = solution.first_stable_day(stock4)
    print(f"stock = {stock4}")
    print(f"First stable day index: {result4}")
    print("Expected: 4")
    print()

    # Single-element array.
    stock5: List[int] = [42]
    result5: int = solution.first_stable_day(stock5)
    print(f"stock = {stock5}")
    print(f"First stable day index: {result5}")
    print("Expected: 0")