"""
Title: Maximum Score from Picking a Profitable Prefix and Suffix

Problem Description:
You are given an integer array profits where profits[i] represents the net profit
(which may be negative) of the i-th product in a catalog. A merchandising team
wants to build a promotion by choosing some number of products from the beginning
of the catalog and some number of products from the end of the catalog. The chosen
prefix and suffix must not overlap, but either side may be empty.

Your task is to return the maximum total profit that can be obtained.

Formally, choose indices such that you take profits[0..i] as a prefix and
profits[j..n-1] as a suffix, where i < j - 1 so the two chosen parts are disjoint.
You may also choose only a prefix, only a suffix, or choose nothing at all if every
option is unprofitable. The score is the sum of all selected values.

This is not the same as choosing one contiguous subarray. You are selecting up to
two separated edge segments of the array.

Constraints:
- 1 <= profits.length <= 200000
- -1000000000 <= profits[i] <= 1000000000
- The answer fits in a signed 64-bit integer.

Example 1:
Input: profits = [4, -2, 3, -10, 5, 6]
Output: 16
Explanation: Take prefix [4, -2, 3] with sum 5 and suffix [5, 6] with sum 11.
They do not overlap, so the total is 16.

Example 2:
Input: profits = [-5, 7, -3, 8, -2]
Output: 10
Explanation: The best choice is to take only the suffix [7, -3, 8, -2] with sum 10.
Taking both sides is worse because the selected parts must stay disjoint and
edge-aligned.
"""

from typing import List


class Solution:
    def max_total_profit(self, profits: List[int]) -> int:
        """
        Compute the maximum total profit obtainable by selecting:
        - a prefix from the start of the array,
        - a suffix from the end of the array,
        - both if they are disjoint,
        - or neither if all choices are negative.

        Args:
            profits: List of integer profits, where each value may be positive or negative.

        Returns:
            The maximum total profit as an integer.

        Time complexity:
            O(n), where n is the length of profits.

        Space complexity:
            O(n), used for storing best suffix sums starting at each index.
        """
        n: int = len(profits)

        # Edge case note:
        # The problem allows choosing nothing, so the answer is always at least 0.
        # We will build the answer from that safe baseline.
        answer: int = 0

        # ------------------------------------------------------------
        # Step 1: Build suffix sums.
        #
        # suffix_sum[i] will store the sum of profits[i] + profits[i+1] + ... + profits[n-1]
        #
        # Why do we need this?
        # Because a valid suffix must start at some index j and continue to the end.
        # So for every possible starting position j, we want to know the total suffix sum quickly.
        # ------------------------------------------------------------
        suffix_sum: List[int] = [0] * n
        suffix_sum[n - 1] = profits[n - 1]

        # Fill from right to left because each suffix sum depends on the next one.
        for i in range(n - 2, -1, -1):
            suffix_sum[i] = profits[i] + suffix_sum[i + 1]

        # ------------------------------------------------------------
        # Step 2: Build best_suffix_from.
        #
        # best_suffix_from[i] will mean:
        # "Among all suffixes that start at index i or later, what is the maximum suffix sum?"
        #
        # This is extremely useful when we choose a prefix ending at index i.
        # If the prefix ends at i, then the suffix must start at least at i + 2
        # to ensure there is no overlap and at least one index gap between them.
        #
        # Then the best suffix we can pair with that prefix is simply:
        # best_suffix_from[i + 2]
        #
        # We also allow choosing no suffix, so we compare against 0.
        # ------------------------------------------------------------
        best_suffix_from: List[int] = [0] * n
        best_suffix_from[n - 1] = max(0, suffix_sum[n - 1])

        # Move from right to left:
        # At each position i, either:
        # - start the suffix exactly at i, giving suffix_sum[i]
        # - or skip i and use the best suffix starting later, best_suffix_from[i + 1]
        # - or choose no suffix at all, represented by 0
        for i in range(n - 2, -1, -1):
            best_suffix_from[i] = max(0, suffix_sum[i], best_suffix_from[i + 1])

        # ------------------------------------------------------------
        # Step 3: Consider taking only a suffix (or nothing).
        #
        # Since the prefix may be empty, any suffix alone is valid.
        # best_suffix_from[0] already represents the best suffix anywhere in the array,
        # or 0 if all suffixes are negative.
        # ------------------------------------------------------------
        answer = max(answer, best_suffix_from[0])

        # ------------------------------------------------------------
        # Step 4: Scan prefixes from left to right.
        #
        # prefix_sum will store the sum of profits[0..i].
        #
        # For each prefix ending at i, we consider:
        # 1. Taking only this prefix
        # 2. Taking this prefix plus the best valid suffix starting at i + 2 or later
        #
        # Why i + 2?
        # If prefix is profits[0..i], then suffix must start at j where i < j - 1.
        # Rearranging gives j >= i + 2.
        #
        # This guarantees the chosen prefix and suffix do not overlap.
        # ------------------------------------------------------------
        prefix_sum: int = 0

        for i in range(n):
            # Extend the prefix by including profits[i].
            prefix_sum += profits[i]

            # Option A: take only the prefix.
            # We compare with current answer because this may be the best choice.
            answer = max(answer, prefix_sum)

            # Option B: take the prefix and also a valid suffix.
            # A valid suffix must start at index i + 2 or later.
            if i + 2 < n:
                answer = max(answer, prefix_sum + best_suffix_from[i + 2])

        return answer


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [4, -2, 3, -10, 5, 6],
        [-5, 7, -3, 8, -2],
        [-1],
        [10],
        [1, 2, 3],
        [-5, -4, -3],
        [5, -100, 6, 7],
    ]

    for profits in sample_inputs:
        result = solution.max_total_profit(profits)
        print(f"profits = {profits}")
        print(f"maximum total profit = {result}")
        print("-" * 50)