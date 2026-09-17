"""
Title: Maximum Tip Total from Choosing Non-Consecutive Tables

Problem Description:
A restaurant manager is planning which tables to assign to a single premium server
during a busy evening. The dining room has tables arranged in a straight line, and
table i would generate tips[i] dollars if the server handles it. However, to avoid
delays, the server cannot be assigned two adjacent tables, because neighboring tables
tend to place orders at nearly the same time.

Your task is to return the maximum total tip amount the server can earn by choosing
a subset of tables such that no two chosen tables are adjacent.

You must decide for each table whether to skip it or assign it, while respecting the
non-adjacent rule. This is an optimization problem where a simple greedy choice does
not always work, so a dynamic programming approach is expected.

Constraints:
- 1 <= tips.length <= 100
- 0 <= tips[i] <= 1000
- The answer fits in a 32-bit signed integer

Example 1:
Input: tips = [5, 1, 8, 4, 7]
Output: 20
Explanation: Choose tables with tips 5, 8, and 7. These are at indices 0, 2, and 4,
so no two chosen tables are adjacent. The total is 5 + 8 + 7 = 20.

Example 2:
Input: tips = [10, 3, 2, 9]
Output: 19
Explanation: The best choice is tables with tips 10 and 9. Choosing 3 and 9 gives
only 12, and choosing 10 and 2 gives only 12. So the maximum total tip is 19.
"""

from typing import List


class Solution:
    def max_tip_total(self, tips: List[int]) -> int:
        """
        Compute the maximum total tip by selecting non-adjacent tables.

        This uses dynamic programming. For each table, we decide between:
        1. Skipping the current table
        2. Taking the current table, which means the previous table cannot be taken

        Args:
            tips: A list where tips[i] is the tip amount from table i.

        Returns:
            The maximum total tip possible without choosing adjacent tables.

        Time complexity:
            O(n), where n is the number of tables

        Space complexity:
            O(1), because we only store results for the previous two states
        """
        # We will solve this using a classic dynamic programming pattern.
        #
        # Main idea:
        # At each position, the best answer depends only on earlier positions.
        # Specifically, for table i:
        #
        # - If we SKIP table i, then our total remains the best total up to table i - 1.
        # - If we TAKE table i, then we must skip table i - 1, so the total becomes:
        #       best total up to table i - 2 + tips[i]
        #
        # Therefore:
        #   dp[i] = max(dp[i - 1], dp[i - 2] + tips[i])
        #
        # Instead of storing the entire dp array, we only need the previous two values:
        # - prev_one = dp[i - 1]
        # - prev_two = dp[i - 2]
        #
        # This reduces space usage from O(n) to O(1).

        # Handle the smallest possible input safely.
        # If there is only one table, the best we can do is take that table.
        if len(tips) == 1:
            return tips[0]

        # prev_two represents the best answer up to index i - 2.
        # At the start, before processing index 1, this corresponds to dp[0].
        prev_two: int = tips[0]

        # prev_one represents the best answer up to index i - 1.
        # For the first two tables, the best choice is the larger of:
        # - taking table 0
        # - taking table 1
        #
        # We cannot take both because they are adjacent.
        prev_one: int = max(tips[0], tips[1])

        # Now process tables starting from index 2.
        for i in range(2, len(tips)):
            # Option 1: skip the current table.
            # If we skip it, the best total stays the same as the best total up to i - 1.
            skip_current: int = prev_one

            # Option 2: take the current table.
            # If we take it, we must add its tip to the best total up to i - 2.
            take_current: int = prev_two + tips[i]

            # Choose the better of the two options.
            current_best: int = max(skip_current, take_current)

            # Move the window forward:
            # - The old prev_one becomes the new prev_two
            # - The current best becomes the new prev_one
            prev_two = prev_one
            prev_one = current_best

        # After processing all tables, prev_one holds the best answer for the full list.
        return prev_one


if __name__ == "__main__":
    solution = Solution()

    sample_1: List[int] = [5, 1, 8, 4, 7]
    result_1: int = solution.max_tip_total(sample_1)
    print(f"Input: {sample_1}")
    print(f"Output: {result_1}")
    print("Expected: 20")
    print()

    sample_2: List[int] = [10, 3, 2, 9]
    result_2: int = solution.max_tip_total(sample_2)
    print(f"Input: {sample_2}")
    print(f"Output: {result_2}")
    print("Expected: 19")
    print()

    # Additional beginner-friendly checks
    sample_3: List[int] = [4]
    result_3: int = solution.max_tip_total(sample_3)
    print(f"Input: {sample_3}")
    print(f"Output: {result_3}")
    print("Expected: 4")
    print()

    sample_4: List[int] = [2, 7, 9, 3, 1]
    result_4: int = solution.max_tip_total(sample_4)
    print(f"Input: {sample_4}")
    print(f"Output: {result_4}")
    print("Expected: 12")