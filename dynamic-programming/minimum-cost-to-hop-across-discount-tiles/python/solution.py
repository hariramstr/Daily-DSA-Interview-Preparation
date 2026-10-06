"""
Title: Minimum Cost to Hop Across Discount Tiles

Problem Description:
You are given an array `cost` where `cost[i]` is the fee to land on tile `i` in a hallway.
A player starts before the first tile and wants to move past the last tile. On each move,
the player may hop forward by either 1 tile or 2 tiles. Whenever the player lands on a tile,
they must pay that tile's fee. The goal is to reach beyond the last tile with the minimum
total cost.

There is one small twist: because of a promotional rule, the player is allowed to start by
landing on either tile `0` or tile `1` without paying any cost before that move. After that,
every landed tile must be paid normally. Return the minimum total fee needed to move beyond
the last tile.

This is a dynamic programming problem because the cheapest way to reach a position depends on
the cheapest ways to reach the previous one or two positions.

Constraints:
- 2 <= cost.length <= 1000
- 0 <= cost[i] <= 999

Example 1:
Input: cost = [4, 2, 7, 3]
Output: 5
Explanation: Start on tile 1 (pay 2), hop to tile 3 (pay 3), then move beyond the last tile.
Total cost = 2 + 3 = 5.

Example 2:
Input: cost = [1, 100, 1, 1, 100, 1]
Output: 3
Explanation: One optimal path is to land on tiles 0, 2, 5, then move beyond the last tile.
Total cost = 1 + 1 + 1 = 3.
"""

from typing import List


class Solution:
    def min_cost_climbing_stairs(self, cost: List[int]) -> int:
        """
        Compute the minimum total fee needed to move beyond the last tile.

        The player may begin by landing on tile 0 or tile 1, and the total cost
        is the sum of fees for the tiles actually landed on. From any tile, the
        player may hop forward by 1 or 2 tiles. The goal is to reach the position
        just beyond the last tile with minimum total cost.

        Args:
            cost: A list where cost[i] is the fee paid when landing on tile i.

        Returns:
            The minimum total fee required to move beyond the last tile.

        Time complexity:
            O(n), where n is the number of tiles, because we process each tile once.

        Space complexity:
            O(1), because we only store the last two dynamic programming states.
        """
        # We will use a space-optimized dynamic programming approach.
        #
        # Core idea:
        # Let dp[i] represent the minimum cost required to reach position i,
        # where position i means:
        # - positions 0..n-1 are actual tiles
        # - position n means "beyond the last tile" (the goal)
        #
        # A standard and very clean way to model this problem is:
        # dp[i] = minimum cost to stand at step i before paying for step i
        #
        # Then:
        # - To reach step i, we must come from step i-1 or step i-2
        # - If we come from step i-1, we must have paid cost[i-1]
        # - If we come from step i-2, we must have paid cost[i-2]
        #
        # So the recurrence becomes:
        # dp[i] = min(dp[i - 1] + cost[i - 1], dp[i - 2] + cost[i - 2])
        #
        # Base cases:
        # dp[0] = 0
        # dp[1] = 0
        #
        # Why are both zero?
        # Because the promotional rule says we may start by landing on tile 0 or tile 1.
        # In this DP formulation, that means the cost is only paid when moving from a
        # previous DP state onto a tile. Starting positions are represented naturally by
        # dp[0] and dp[1] both being zero.
        #
        # Example for cost = [4, 2, 7, 3]:
        # dp[0] = 0
        # dp[1] = 0
        # dp[2] = min(dp[1] + cost[1], dp[0] + cost[0]) = min(0 + 2, 0 + 4) = 2
        # dp[3] = min(dp[2] + cost[2], dp[1] + cost[1]) = min(2 + 7, 0 + 2) = 2
        # dp[4] = min(dp[3] + cost[3], dp[2] + cost[2]) = min(2 + 3, 2 + 7) = 5
        # Answer = dp[4] = 5
        #
        # This matches the required output.
        #
        # We can optimize space because each dp[i] only depends on the previous two values.
        n: int = len(cost)

        # prev2 will represent dp[i - 2]
        # prev1 will represent dp[i - 1]
        #
        # Initially:
        # prev2 = dp[0] = 0
        # prev1 = dp[1] = 0
        prev2: int = 0
        prev1: int = 0

        # We now compute dp[2], dp[3], ..., dp[n].
        # Each iteration calculates the minimum cost to reach the current position i.
        for i in range(2, n + 1):
            # Option 1:
            # Reach position i by coming from position i - 1.
            # If we were already at position i - 1 with minimum cost prev1,
            # then to move to i we must land on tile i - 1 and pay cost[i - 1].
            one_step: int = prev1 + cost[i - 1]

            # Option 2:
            # Reach position i by coming from position i - 2.
            # If we were already at position i - 2 with minimum cost prev2,
            # then to move to i we must land on tile i - 2 and pay cost[i - 2].
            two_steps: int = prev2 + cost[i - 2]

            # The best way to reach position i is the cheaper of these two options.
            current: int = min(one_step, two_steps)

            # Shift the DP window forward:
            # - old prev1 becomes new prev2
            # - current becomes new prev1
            prev2, prev1 = prev1, current

        # After the loop, prev1 holds dp[n], which is the minimum cost to move
        # beyond the last tile.
        return prev1


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Sample input 1 from the problem statement.
    cost1: List[int] = [4, 2, 7, 3]
    result1: int = solution.min_cost_climbing_stairs(cost1)
    print(f"Input: {cost1}")
    print(f"Minimum cost: {result1}")
    print()

    # Verified manually:
    # Best path is tile 1 -> tile 3 -> beyond
    # Cost = 2 + 3 = 5
    # Expected output: 5

    # Sample input 2 from the problem statement.
    cost2: List[int] = [1, 100, 1, 1, 100, 1]
    result2: int = solution.min_cost_climbing_stairs(cost2)
    print(f"Input: {cost2}")
    print(f"Minimum cost: {result2}")
    print()

    # Verified manually:
    # One optimal path is tile 0 -> tile 2 -> tile 5 -> beyond
    # Cost = 1 + 1 + 1 = 3
    # Expected output: 3

    # Additional beginner-friendly test cases.
    extra_tests: List[List[int]] = [
        [10, 15],
        [0, 0, 0, 0],
        [1, 2, 3, 4, 5],
        [5, 1, 1, 5],
    ]

    for test_cost in extra_tests:
        print(f"Input: {test_cost}")
        print(f"Minimum cost: {solution.min_cost_climbing_stairs(test_cost)}")
        print()