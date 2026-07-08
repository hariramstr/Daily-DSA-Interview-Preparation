"""
Title: Minimum Cost to Reach the Last Lily Pad

Problem Description:
A frog is crossing a narrow pond by jumping across a line of lily pads. You are given
an integer array cost where cost[i] is the energy cost of landing on lily pad i.
The frog starts before the first lily pad, and its goal is to move beyond the last pad.

On each move, the frog may jump forward by either 1 pad or 2 pads.
If the frog lands on a lily pad, it must pay that pad's cost.
The frog does not pay any cost for the starting position before index 0,
and it also does not pay any cost after it has moved beyond the last index.

Return the minimum total energy needed to reach beyond the last lily pad.

This is a classic one-dimensional dynamic programming problem with a simple state
transition. For each position, you can arrive from one step before or two steps before,
so the best answer for a pad depends on the minimum cost of those earlier positions.
An efficient solution should run in O(n) time, and it can be implemented with either
a full DP array or constant extra space.

Constraints:
- 1 <= cost.length <= 1000
- 0 <= cost[i] <= 999

Example 1:
Input: cost = [4, 2, 7, 3]
Output: 5
Explanation: One optimal path is to land on pad 1 (cost 2), then pad 3 (cost 3),
then jump beyond the last pad. Total cost = 2 + 3 = 5.

Example 2:
Input: cost = [1, 100, 1, 1, 100, 1]
Output: 3
Explanation: One optimal path is to land on pads 0, 2, 3, and 5, paying
1 + 1 + 1 + 1 = 4, but that is not optimal. A better path is to land on
pads 0, 2, and 5, paying 1 + 1 + 1 = 3.
"""

from typing import List


class Solution:
    def min_cost_climbing_stairs(self, cost: List[int]) -> int:
        """
        Compute the minimum total energy required to move beyond the last lily pad.

        The frog may start before index 0 and can jump 1 or 2 pads at a time.
        It pays only when it lands on a pad. The goal is to reach the position
        just beyond the last index with minimum total cost.

        Args:
            cost: A list where cost[i] is the energy cost of landing on pad i.

        Returns:
            The minimum total energy needed to move beyond the last lily pad.

        Time complexity:
            O(n), where n is the number of lily pads.

        Space complexity:
            O(1), because only a constant amount of extra memory is used.
        """
        # The number of lily pads in the pond.
        n: int = len(cost)

        # Edge case:
        # If there is only one lily pad, the frog has two conceptual options:
        # 1. Land on pad 0, pay cost[0], then jump beyond.
        # 2. Since the frog can jump 1 or 2 pads from the start position before index 0,
        #    it can jump directly beyond the only pad using a 2-pad jump and pay nothing.
        # Therefore, the minimum cost is 0.
        if n == 1:
            return 0

        # Dynamic programming with constant space:
        #
        # We define:
        # dp[i] = minimum cost required to reach position i
        #
        # Important detail:
        # Position i is not always a lily pad.
        # - Positions 0 through n - 1 correspond to actual lily pads.
        # - Position n means "just beyond the last lily pad" (the goal).
        #
        # Since the frog starts before the first pad, it can begin by moving to:
        # - position 0 with cost 0 before landing logic is applied in transitions
        # - position 1 also with cost 0
        #
        # This matches the classic recurrence:
        # dp[0] = 0
        # dp[1] = 0
        # For i from 2 to n:
        # dp[i] = min(
        #     dp[i - 1] + cost[i - 1],   # jump 1 step from previous pad
        #     dp[i - 2] + cost[i - 2]    # jump 2 steps from two pads back
        # )
        #
        # Why this works:
        # To reach position i, the frog must come from either:
        # - position i - 1, landing on pad i - 1 before jumping to i
        # - position i - 2, landing on pad i - 2 before jumping to i
        #
        # In both cases, the cost paid is for the pad landed on before arriving at i.
        #
        # We only need the previous two DP values at any time, so we store:
        # prev2 = dp[i - 2]
        # prev1 = dp[i - 1]
        prev2: int = 0
        prev1: int = 0

        # Iterate through all target positions from 2 up to n inclusive.
        # Remember:
        # - i represents the position we want to reach
        # - when i == n, that means we are reaching beyond the last lily pad
        for i in range(2, n + 1):
            # Option 1:
            # Reach position i by coming from position i - 1.
            # To do that, the frog must have already reached position i - 1
            # with minimum cost prev1, and then it must pay cost[i - 1]
            # because it lands on pad i - 1 before moving to i.
            one_step: int = prev1 + cost[i - 1]

            # Option 2:
            # Reach position i by coming from position i - 2.
            # Similarly, the frog must have already reached position i - 2
            # with minimum cost prev2, and then it pays cost[i - 2]
            # for landing on that pad before jumping to i.
            two_steps: int = prev2 + cost[i - 2]

            # Choose the cheaper of the two possible ways to reach position i.
            current: int = min(one_step, two_steps)

            # Shift the DP window forward:
            # - old prev1 becomes new prev2
            # - current becomes new prev1
            #
            # After this update:
            # prev2 will represent dp[i - 1]
            # prev1 will represent dp[i]
            prev2 = prev1
            prev1 = current

        # After the loop finishes, prev1 stores dp[n],
        # which is the minimum cost to move beyond the last lily pad.
        return prev1

    def minCostClimbingStairs(self, cost: List[int]) -> int:
        """
        Wrapper method using the common interview/platform naming convention.

        Args:
            cost: A list where cost[i] is the energy cost of landing on pad i.

        Returns:
            The minimum total energy needed to move beyond the last lily pad.

        Time complexity:
            O(n), where n is the number of lily pads.

        Space complexity:
            O(1), because only a constant amount of extra memory is used.
        """
        return self.min_cost_climbing_stairs(cost)


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Sample input 1 from the problem statement.
    cost1: List[int] = [4, 2, 7, 3]
    result1: int = solution.minCostClimbingStairs(cost1)
    print("Input:", cost1)
    print("Minimum cost to reach beyond the last lily pad:", result1)
    print()

    # Expected reasoning for example 1:
    # Best path is to land on pad 1 (cost 2), then pad 3 (cost 3), then go beyond.
    # Total = 2 + 3 = 5

    # Sample input 2 from the problem statement.
    cost2: List[int] = [1, 100, 1, 1, 100, 1]
    result2: int = solution.minCostClimbingStairs(cost2)
    print("Input:", cost2)
    print("Minimum cost to reach beyond the last lily pad:", result2)
    print()

    # Expected reasoning for example 2:
    # A better path is to land on pads 0, 2, and 5.
    # Total = 1 + 1 + 1 = 3

    # Additional small test to help beginners see edge behavior.
    cost3: List[int] = [10]
    result3: int = solution.minCostClimbingStairs(cost3)
    print("Input:", cost3)
    print("Minimum cost to reach beyond the last lily pad:", result3)