"""
Title: Find the Second Largest Daily Sale

Problem Description:
You are given an integer array sales where sales[i] represents the total sales made on day i.
Your task is to return the second largest distinct value in the array. A value is considered
distinct only if it is different from the largest value. If there is no second largest distinct
value, return -1.

This problem is about scanning an array efficiently while keeping track of the two best distinct
values seen so far. Duplicate values should not be counted multiple times. For example, if the
largest value appears several times, you still need the next smaller distinct value.

Write a function that returns the second largest distinct daily sale.

Constraints:
- 1 <= sales.length <= 10^5
- -10^9 <= sales[i] <= 10^9
- The solution should run in O(n) time.
- Extra space should be O(1), not counting the input array.

Example 1:
Input: sales = [12, 7, 19, 19, 5, 14]
Output: 14
Explanation: The largest distinct sale is 19, and the second largest distinct sale is 14.

Example 2:
Input: sales = [8, 8, 8]
Output: -1
Explanation: There is only one distinct value in the array, so a second largest distinct value
does not exist.
"""

from typing import List, Optional


class Solution:
    def second_largest_distinct(self, sales: List[int]) -> int:
        """
        Return the second largest distinct value in the sales array.

        Args:
            sales: A list of integers where each value represents daily sales.

        Returns:
            The second largest distinct integer in the list, or -1 if it does not exist.

        Time Complexity:
            O(n), because we scan through the list exactly once.

        Space Complexity:
            O(1), because we use only a fixed number of extra variables.
        """
        # We keep track of the largest distinct value seen so far.
        # At the beginning, we have not seen any values yet, so we use None.
        largest: Optional[int] = None

        # We also keep track of the second largest distinct value seen so far.
        # This is also initialized to None because we have not found one yet.
        second_largest: Optional[int] = None

        # We scan through every sale value exactly once.
        # This satisfies the O(n) time requirement.
        for value in sales:
            # Case 1:
            # If largest is None, this is the first value we are processing.
            # That means this value automatically becomes the largest seen so far.
            if largest is None:
                largest = value
                continue

            # Case 2:
            # If the current value is exactly equal to the current largest,
            # it is NOT a new distinct value. Since the problem requires
            # distinct values, we ignore duplicates of the largest.
            if value == largest:
                continue

            # Case 3:
            # If the current value is greater than the current largest,
            # then we have found a new largest distinct value.
            #
            # The old largest does not disappear; it becomes the second largest,
            # because it is now the next biggest distinct value below the new largest.
            if value > largest:
                second_largest = largest
                largest = value
                continue

            # At this point, we know:
            # - value is not equal to largest
            # - value is less than largest
            #
            # So value is a candidate for second_largest.

            # Case 4:
            # If second_largest is None, we have not found any valid second distinct
            # value yet. Since value is smaller than largest and distinct from it,
            # it becomes the second largest.
            if second_largest is None:
                second_largest = value
                continue

            # Case 5:
            # If the current value equals second_largest, it is just a duplicate
            # of the second best distinct value we already know. Ignore it.
            if value == second_largest:
                continue

            # Case 6:
            # If the current value is greater than second_largest, but still less than largest
            # (which we already know from earlier checks), then this value is a better
            # candidate for second largest.
            if value > second_largest:
                second_largest = value

            # If value < second_largest, we do nothing because it is not good enough
            # to affect the top two distinct values.

        # After scanning the entire array:
        # - If second_largest is still None, then no second distinct value exists.
        # - Otherwise, return the second largest distinct value.
        return -1 if second_largest is None else second_largest


if __name__ == "__main__":
    solution = Solution()

    sample_sales_1 = [12, 7, 19, 19, 5, 14]
    sample_sales_2 = [8, 8, 8]
    sample_sales_3 = [5]
    sample_sales_4 = [-10, -3, -3, -20, -7]
    sample_sales_5 = [2, 1]
    sample_sales_6 = [1, 2, 3, 4, 5]
    sample_sales_7 = [5, 5, 4, 4, 3]

    print(solution.second_largest_distinct(sample_sales_1))  # Expected: 14
    print(solution.second_largest_distinct(sample_sales_2))  # Expected: -1
    print(solution.second_largest_distinct(sample_sales_3))  # Expected: -1
    print(solution.second_largest_distinct(sample_sales_4))  # Expected: -7
    print(solution.second_largest_distinct(sample_sales_5))  # Expected: 1
    print(solution.second_largest_distinct(sample_sales_6))  # Expected: 4
    print(solution.second_largest_distinct(sample_sales_7))  # Expected: 4