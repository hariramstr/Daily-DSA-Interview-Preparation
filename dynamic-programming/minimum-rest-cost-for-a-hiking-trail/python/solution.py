"""
Title: Minimum Rest Cost for a Hiking Trail

Problem Description:
You are planning a hike along a straight trail that is divided into numbered checkpoints
from 0 to n - 1. You start at checkpoint 0 and want to reach checkpoint n - 1.
From any checkpoint i, you may move either 1 step forward to i + 1 or 2 steps forward
to i + 2, as long as that checkpoint exists.

Each checkpoint has a rest cost given by an integer array cost, where cost[i] is the
energy cost of stopping at checkpoint i. If you land on a checkpoint during your hike,
you must pay its rest cost. Your goal is to reach the final checkpoint with the minimum
total cost.

Return the minimum total rest cost needed to reach checkpoint n - 1.

This is a classic dynamic programming problem because the cheapest way to reach a
checkpoint depends only on the cheapest ways to reach the previous one or two checkpoints.
A simple linear-time solution is expected.

Constraints:
- 1 <= n == cost.length <= 1000
- 0 <= cost[i] <= 1000
- If n == 1, you are already at the final checkpoint, so the answer is cost[0]

Examples:
1) cost = [4, 2, 7, 3]
   Output: 9

2) cost = [1, 100, 1, 1, 100, 1]
   Output: 4
"""

from typing import List


class Solution:
    def min_rest_cost(self, cost: List[int]) -> int:
        """
        Compute the minimum total rest cost needed to reach the final checkpoint.

        The hiker starts at checkpoint 0, so the cost of checkpoint 0 must always be paid.
        From each checkpoint, the hiker may move forward by 1 or 2 positions.
        Dynamic programming is used because the cheapest way to reach checkpoint i depends
        only on the cheapest ways to reach checkpoints i - 1 and i - 2.

        Args:
            cost: A list of non-negative integers where cost[i] is the rest cost of
                  checkpoint i.

        Returns:
            The minimum total rest cost required to reach checkpoint n - 1.

        Time Complexity:
            O(n), where n is the number of checkpoints.

        Space Complexity:
            O(n), due to the dynamic programming array.
        """
        # First, store the number of checkpoints in a variable so we do not need
        # to repeatedly call len(cost). This makes the code a little cleaner and
        # easier to read.
        n: int = len(cost)

        # Special case:
        # If there is only one checkpoint, then checkpoint 0 is both the start
        # and the destination. Since we begin there and must pay the cost of any
        # checkpoint we land on, the answer is simply cost[0].
        if n == 1:
            return cost[0]

        # Create a DP array where:
        # dp[i] = minimum total cost required to reach checkpoint i
        #
        # Why use a list?
        # - We need to build answers from left to right.
        # - Each position depends only on earlier positions.
        # - A list gives O(1) access to dp[i - 1] and dp[i - 2].
        dp: List[int] = [0] * n

        # Base case 1:
        # To reach checkpoint 0, we start there immediately, so we must pay cost[0].
        dp[0] = cost[0]

        # Base case 2:
        # To reach checkpoint 1, the only possible path is 0 -> 1,
        # because we start at 0 and can move forward.
        # Therefore, the total cost is cost[0] + cost[1].
        dp[1] = cost[0] + cost[1]

        # Fill the DP table from checkpoint 2 up to checkpoint n - 1.
        #
        # For each checkpoint i:
        # - We could arrive from checkpoint i - 1
        # - Or we could arrive from checkpoint i - 2
        #
        # Since we want the minimum total cost, we choose the cheaper of those
        # two previous ways, then add the cost of landing on checkpoint i.
        for i in range(2, n):
            # Compute the cheaper cost to arrive at the previous reachable checkpoint.
            best_previous_cost: int = min(dp[i - 1], dp[i - 2])

            # Add the current checkpoint's cost because landing on checkpoint i
            # requires paying cost[i].
            dp[i] = best_previous_cost + cost[i]

        # The final answer is the minimum cost to reach the last checkpoint.
        return dp[n - 1]


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [4, 2, 7, 3],
        [1, 100, 1, 1, 100, 1],
        [5],
        [3, 8],
        [2, 1, 2, 1, 2],
    ]

    for costs in sample_inputs:
        result: int = solution.min_rest_cost(costs)
        print(f"cost = {costs}")
        print(f"Minimum rest cost = {result}")
        print("-" * 40)