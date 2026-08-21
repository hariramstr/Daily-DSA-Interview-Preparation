"""
Title: Minimum Energy to Cross Paid Stepping Stones

Problem Description:
You are given an array cost where cost[i] is the energy required to land on
stepping stone i. A hiker wants to cross a small river by moving from left to
right. From any stone, the hiker may jump either 1 stone ahead or 2 stones
ahead. The hiker may start on stone 0 or stone 1, and the goal is to move
beyond the last stone with the minimum total energy spent.

A stone's energy cost is paid only when the hiker lands on that stone.
Reaching the far bank just past the last index does not cost anything.
Your task is to return the minimum total energy needed to cross the river.

This is a dynamic programming problem because the cheapest way to reach a stone
depends on the cheapest ways to reach the previous one or two stones.
An efficient solution should compute the answer in linear time.

Constraints:
- 2 <= cost.length <= 1000
- 0 <= cost[i] <= 999

Example 1:
Input: cost = [4, 7, 2, 9]
Output: 6
Explanation: Start on stone 0 (pay 4), jump to stone 2 (pay 2), then jump
beyond the last stone. Total energy = 4 + 2 = 6.

Example 2:
Input: cost = [1, 100, 1, 1, 100, 1]
Output: 3
Explanation: Start on stone 0, then land on stones 2, 3, and 5.
The total is 1 + 1 + 1 = 3. Other paths require more energy.

Return the minimum energy required to reach the far bank.
"""

from typing import List


class Solution:
    def min_cost_climbing_stairs(self, cost: List[int]) -> int:
        """
        Compute the minimum total energy needed to move beyond the last stone.

        The hiker may begin on stone 0 or stone 1. Landing on a stone costs
        cost[i], and from each stone the hiker may jump 1 or 2 stones forward.
        The far bank beyond the last stone has no cost.

        Args:
            cost: A list where cost[i] is the energy required to land on stone i.

        Returns:
            The minimum total energy required to reach the far bank.

        Time complexity:
            O(n), where n is the number of stones, because we process each stone once.

        Space complexity:
            O(1), because we only store the minimum costs for the previous two states.
        """
        # The key dynamic programming idea:
        #
        # Let dp[i] represent the minimum total energy needed to LAND on stone i.
        #
        # If we want to land on stone i, there are only two possible previous places:
        #   1. stone i - 1
        #   2. stone i - 2
        #
        # Therefore:
        #   dp[i] = cost[i] + min(dp[i - 1], dp[i - 2])
        #
        # Why does this work?
        # Because the cheapest way to reach stone i must come from the cheaper of:
        #   - the cheapest way to reach i - 1, then jump 1 step
        #   - the cheapest way to reach i - 2, then jump 2 steps
        #
        # Important detail:
        # The hiker is allowed to START on stone 0 or stone 1.
        # That means:
        #   dp[0] = cost[0]
        #   dp[1] = cost[1]
        #
        # Finally, to reach the far bank (which is just beyond the last index),
        # the hiker can jump there from either:
        #   - the last stone
        #   - the second-to-last stone
        #
        # Since the far bank itself costs nothing, the answer is:
        #   min(dp[n - 1], dp[n - 2])

        n: int = len(cost)

        # Base cases:
        # If there are exactly 2 stones, the hiker can start on either one and
        # immediately jump beyond the last stone. So the answer is simply the
        # cheaper of the two starting stones.
        if n == 2:
            return min(cost[0], cost[1])

        # prev2 will store dp[i - 2]
        # Initially, for i = 2, dp[0] is just cost[0] because starting on stone 0
        # means we pay its landing cost immediately.
        prev2: int = cost[0]

        # prev1 will store dp[i - 1]
        # Similarly, dp[1] is cost[1] because starting on stone 1 is allowed.
        prev1: int = cost[1]

        # Process stones from index 2 up to the last stone.
        for i in range(2, n):
            # To land on stone i:
            # - either come from stone i - 1 with total cost prev1
            # - or come from stone i - 2 with total cost prev2
            #
            # We choose the cheaper previous route, then add the cost of landing
            # on the current stone.
            current: int = cost[i] + min(prev1, prev2)

            # Move the sliding window forward:
            # - the old prev1 becomes the new prev2
            # - current becomes the new prev1
            #
            # This lets us compute the next state without storing the full dp array.
            prev2, prev1 = prev1, current

        # After the loop:
        # - prev1 holds dp[n - 1], the minimum cost to land on the last stone
        # - prev2 holds dp[n - 2], the minimum cost to land on the second-to-last stone
        #
        # The hiker can reach the far bank from either of those stones at no extra cost.
        return min(prev1, prev2)

    def minCostClimbingStairs(self, cost: List[int]) -> int:
        """
        Wrapper method using the common interview-style method name.

        Args:
            cost: A list where cost[i] is the energy required to land on stone i.

        Returns:
            The minimum total energy required to reach the far bank.

        Time complexity:
            O(n), where n is the number of stones.

        Space complexity:
            O(1), because only constant extra memory is used.
        """
        return self.min_cost_climbing_stairs(cost)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # cost = [4, 7, 2, 9]
    #
    # Trace:
    # dp[0] = 4
    # dp[1] = 7
    # dp[2] = 2 + min(7, 4) = 6
    # dp[3] = 9 + min(6, 7) = 15
    # answer = min(dp[3], dp[2]) = min(15, 6) = 6
    #
    # Expected output: 6
    sample_1: List[int] = [4, 7, 2, 9]
    result_1: int = solution.minCostClimbingStairs(sample_1)
    print(f"Input: {sample_1}")
    print(f"Minimum energy: {result_1}")
    print()

    # Example 2:
    # cost = [1, 100, 1, 1, 100, 1]
    #
    # Trace:
    # dp[0] = 1
    # dp[1] = 100
    # dp[2] = 1 + min(100, 1) = 2
    # dp[3] = 1 + min(2, 100) = 3
    # dp[4] = 100 + min(3, 2) = 102
    # dp[5] = 1 + min(102, 3) = 4
    # answer = min(dp[5], dp[4]) = min(4, 102) = 4
    #
    # Note:
    # The mathematically correct minimum under the stated rules is 4, not 3.
    # A valid cheapest path is:
    #   start at stone 0 -> stone 2 -> stone 3 -> stone 5 -> far bank
    #   total = 1 + 1 + 1 + 1 = 4
    #
    # The problem statement's example says 3, but that omits the cost of landing
    # on stone 5 even though it is included in the described path.
    sample_2: List[int] = [1, 100, 1, 1, 100, 1]
    result_2: int = solution.minCostClimbingStairs(sample_2)
    print(f"Input: {sample_2}")
    print(f"Minimum energy: {result_2}")
    print()

    # Additional quick test
    sample_3: List[int] = [10, 15, 20]
    result_3: int = solution.minCostClimbingStairs(sample_3)
    print(f"Input: {sample_3}")
    print(f"Minimum energy: {result_3}")