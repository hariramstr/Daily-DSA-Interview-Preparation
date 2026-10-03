"""
Title: Minimum Energy to Cross a Toll Bridge Path

Problem Description:
A courier robot needs to cross a sequence of bridge sections to deliver a package.
The path is represented by an array `cost`, where `cost[i]` is the energy required
to land on section `i`. The robot may start before the first section, and on each
move it can jump either 1 section or 2 sections forward. The robot reaches the
destination when it moves past the last section.

If the robot lands on a section, it must pay that section's energy cost exactly once.
Since it can jump over sections, skipped sections do not add any cost.

Return the minimum energy required to cross the full path.

Constraints:
- 2 <= cost.length <= 1000
- 0 <= cost[i] <= 999

Examples:
1) cost = [4, 2, 7, 3]
   Output: 5
   Explanation: Start before the path, land on section 1 (cost 2), then section 3
   (cost 3), then move past the end. Total = 5.

2) cost = [1, 100, 1, 1, 100, 1]
   Output: 3
   Explanation: A best route is to land on sections 0, 2, and 5 for total cost 3.
"""

from typing import List


class Solution:
    def min_cost_climbing_stairs(self, cost: List[int]) -> int:
        """
        Compute the minimum total energy needed to move past the last section.

        The robot may begin before index 0 and can first move to index 0 or index 1.
        From any landed section, it may jump forward by 1 or 2 sections. The goal is
        to reach the position just beyond the last index with minimum total landing cost.

        Args:
            cost: A list where cost[i] is the energy required to land on section i.

        Returns:
            The minimum total energy required to cross the full path.

        Time Complexity:
            O(n), where n is the number of sections.

        Space Complexity:
            O(1), because we only keep the last two dynamic programming states.
        """
        # We use dynamic programming with constant extra space.
        #
        # Core idea:
        # Let dp[i] represent the minimum cost required to reach "step i",
        # where step i means a position in the path progression, and the final
        # destination is step n (one position beyond the last index).
        #
        # To reach step i, the robot must come from:
        # - step i - 1, paying cost[i - 1] when stepping off that previous landing path
        # - step i - 2, paying cost[i - 2]
        #
        # Therefore:
        # dp[i] = min(
        #     dp[i - 1] + cost[i - 1],
        #     dp[i - 2] + cost[i - 2]
        # )
        #
        # Base cases:
        # dp[0] = 0  -> starting before the first section costs nothing
        # dp[1] = 0  -> starting before the path and choosing section 1 as the first
        #               possible landing setup also means no cost has been paid yet
        #               until we actually land according to the recurrence
        #
        # This is the classic "min cost climbing stairs" recurrence.

        n: int = len(cost)

        # prev_two will store dp[i - 2]
        # Initially, for i = 2:
        # dp[0] = 0
        prev_two: int = 0

        # prev_one will store dp[i - 1]
        # Initially, for i = 2:
        # dp[1] = 0
        prev_one: int = 0

        # We compute dp[2], dp[3], ..., dp[n].
        # Each dp[i] means the minimum cost to reach position i,
        # where position n is the destination beyond the last section.
        for i in range(2, n + 1):
            # Option 1:
            # Reach current position i from position i - 1.
            # If we do that, then the last section we landed on is index i - 1,
            # so we must pay cost[i - 1].
            take_one_step: int = prev_one + cost[i - 1]

            # Option 2:
            # Reach current position i from position i - 2.
            # If we do that, then the last section we landed on is index i - 2,
            # so we must pay cost[i - 2].
            take_two_steps: int = prev_two + cost[i - 2]

            # Choose the cheaper of the two possible ways to reach position i.
            current: int = min(take_one_step, take_two_steps)

            # Move the rolling window forward:
            # - old dp[i - 1] becomes new dp[i - 2]
            # - current dp[i] becomes new dp[i - 1]
            prev_two = prev_one
            prev_one = current

        # After the loop, prev_one holds dp[n], the minimum cost to move past the end.
        return prev_one

    def minCostClimbingStairs(self, cost: List[int]) -> int:
        """
        Wrapper method using the common interview/platform naming convention.

        Args:
            cost: A list where cost[i] is the energy required to land on section i.

        Returns:
            The minimum total energy required to cross the full path.

        Time Complexity:
            O(n), where n is the number of sections.

        Space Complexity:
            O(1), because only two previous DP states are stored.
        """
        return self.min_cost_climbing_stairs(cost)


if __name__ == "__main__":
    # Create an instance of the solution class.
    solver = Solution()

    # Sample input 1 from the problem statement.
    cost1: List[int] = [4, 2, 7, 3]
    result1: int = solver.minCostClimbingStairs(cost1)
    print(f"Input: {cost1}")
    print(f"Minimum energy required: {result1}")
    print("Expected: 5")
    print()

    # Sample input 2 from the problem statement.
    cost2: List[int] = [1, 100, 1, 1, 100, 1]
    result2: int = solver.minCostClimbingStairs(cost2)
    print(f"Input: {cost2}")
    print(f"Minimum energy required: {result2}")
    print("Expected: 3")
    print()

    # Additional beginner-friendly sanity check.
    cost3: List[int] = [10, 15, 20]
    result3: int = solver.minCostClimbingStairs(cost3)
    print(f"Input: {cost3}")
    print(f"Minimum energy required: {result3}")
    print("Expected: 15")