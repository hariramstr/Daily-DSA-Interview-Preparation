"""
Title: Maximum Score from Choosing One Value Per Day Range

Problem Description:
You are given an integer array values where values[i] represents the score available
on day i. You must build a schedule by choosing a set of days to collect score,
subject to one rule: if you choose day i, then you cannot choose either adjacent
day i - 1 or i + 1. In other words, any two chosen days must be at least 2 indices apart.

Your task is to return the maximum total score you can collect.

This models a realistic planning problem where collecting score on one day requires
a cooldown on neighboring days. The array may contain positive, zero, or negative values.
You are allowed to skip any day, including all days if that gives a better result.

Write a function that computes the best possible total score.

Constraints:
- 1 <= values.length <= 100000
- -10000 <= values[i] <= 10000
- The answer fits in a 32-bit signed integer.

Example 1:
Input: values = [4, 2, 7, 9, 3]
Corrected Output: 14
Explanation: Choose days 0, 2, and 4 for 4 + 7 + 3 = 14.

Example 2:
Input: values = [-5, -1, -8]
Output: 0
Explanation: Since all scores are negative, the best choice is to skip every day.
"""

from typing import List


class Solution:
    def max_score(self, values: List[int]) -> int:
        """
        Compute the maximum total score by choosing non-adjacent days.

        This uses dynamic programming with constant extra space.
        For each day, we decide between:
        1. Skipping the current day
        2. Taking the current day, which means we must skip the previous day

        Args:
            values: A list of integers where values[i] is the score available on day i.

        Returns:
            The maximum total score obtainable without choosing adjacent days.

        Time complexity:
            O(n), where n is the number of days

        Space complexity:
            O(1), excluding input storage
        """
        # We maintain two rolling dynamic programming states:
        #
        # prev_two:
        #   The best answer considering elements up to index i - 2.
        #   This is needed because if we choose the current day i,
        #   we are not allowed to choose day i - 1, so we can only add
        #   the current value to the best answer from i - 2.
        #
        # prev_one:
        #   The best answer considering elements up to index i - 1.
        #   This is needed because if we skip the current day i,
        #   then the best answer remains whatever was best up to i - 1.
        #
        # We initialize both to 0 because:
        # - Choosing no days is always allowed
        # - This also correctly handles arrays with all negative values,
        #   where the best answer should be 0 by skipping everything
        prev_two: int = 0
        prev_one: int = 0

        # Process each day's score one by one.
        for value in values:
            # Option 1: Skip the current day.
            # If we skip today, the best total is simply the best total
            # we already had up to the previous day.
            skip_current: int = prev_one

            # Option 2: Take the current day.
            # If we take today, we cannot take yesterday, so we add today's
            # value to the best total from two days ago.
            take_current: int = prev_two + value

            # The best answer up to the current day is the better of:
            # - skipping today
            # - taking today
            #
            # Because prev_one and prev_two both started at 0, and because
            # we always compare against skip_current, the running result
            # never drops below 0. That means negative-only arrays correctly
            # return 0.
            current_best: int = max(skip_current, take_current)

            # Move the rolling window forward:
            # - The old prev_one becomes the new prev_two
            # - The current best becomes the new prev_one
            prev_two, prev_one = prev_one, current_best

        # After processing all days, prev_one stores the best answer
        # for the entire array.
        return prev_one


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [4, 2, 7, 9, 3],
        [-5, -1, -8],
        [2],
        [0, 0, 0],
        [5, 1, 1, 5],
    ]

    for values in sample_inputs:
        result = solution.max_score(values)
        print(f"values = {values}")
        print(f"maximum score = {result}")
        print("-" * 40)