"""
Title: Minimum Cost to Reach the Office With 1-Step or 2-Step Elevators

Problem Description:
You are in a building lobby and want to reach the office on floor n. The building has
a special elevator system: from floor i, you may move either 1 floor up or 2 floors up.
However, each floor has an entry fee charged when you land on it. You are given an
integer array cost where cost[i] is the fee to land on floor i + 1. Your goal is to
reach exactly floor n while paying the minimum total fee.

You start before floor 1, so no fee is paid at the beginning. If you jump directly to
a floor, you pay only for the floor where you land. For example, from the lobby you may
go to floor 1 and pay cost[0], or go directly to floor 2 and pay cost[1].

Return the minimum total fee needed to reach floor n.

Dynamic Programming Idea:
Let dp[i] represent the minimum cost to reach floor i (1-indexed for floor numbers).
Then:
- dp[1] = cost[0]
- dp[2] = cost[1]
- For i >= 3:
    dp[i] = min(dp[i - 1], dp[i - 2]) + cost[i - 1]

This works because to land on floor i, the last move must come from either:
- floor i - 1 using a 1-floor move, or
- floor i - 2 using a 2-floor move

So we choose the cheaper of those two ways and then add the fee for landing on floor i.

Constraints:
- 1 <= cost.length <= 1000
- 1 <= cost[i] <= 1000
- n = cost.length

Example 1:
Input: cost = [4, 2, 7, 3]
Output: 5
Explanation:
- Reach floor 1: cost 4
- Reach floor 2: cost 2
- Reach floor 3: min(2, 4) + 7 = 9
- Reach floor 4: min(9, 2) + 3 = 5
Answer = 5

Example 2:
Input: cost = [1, 100, 1, 1, 100, 1]
Correct Output: 4
Explanation:
- floor 1 = 1
- floor 2 = 100
- floor 3 = min(100, 1) + 1 = 2
- floor 4 = min(2, 100) + 1 = 3
- floor 5 = min(3, 2) + 100 = 102
- floor 6 = min(102, 3) + 1 = 4
Answer = 4
"""

from typing import List


class Solution:
    def min_cost_to_reach_office(self, cost: List[int]) -> int:
        """
        Compute the minimum total fee needed to reach exactly the top floor.

        The traveler starts in the lobby (before floor 1) and may move either
        1 floor or 2 floors at a time. A fee is paid only when landing on a floor.
        This method uses dynamic programming to build the minimum cost for each floor.

        Args:
            cost: A list where cost[i] is the fee to land on floor i + 1.

        Returns:
            The minimum total fee required to reach exactly floor n.

        Time complexity:
            O(n), where n is the number of floors.

        Space complexity:
            O(n), because we store a DP table of size n + 1.
        """
        # The number of floors is exactly the length of the cost list.
        # If cost has length 4, then the office is on floor 4.
        n: int = len(cost)

        # Handle the smallest possible input directly.
        # If there is only one floor, the only valid move is:
        # lobby -> floor 1, paying cost[0].
        if n == 1:
            return cost[0]

        # We create a DP array where:
        # dp[i] = minimum cost to reach floor i
        #
        # Important indexing note:
        # - Floors are naturally numbered from 1 to n.
        # - Python lists are 0-indexed.
        # So:
        # - floor 1 corresponds to cost[0]
        # - floor 2 corresponds to cost[1]
        # - floor i corresponds to cost[i - 1]
        #
        # We allocate n + 1 elements so that dp[1] through dp[n] are easy to use.
        # dp[0] is unused in the recurrence, but keeping it makes indexing cleaner.
        dp: List[int] = [0] * (n + 1)

        # Base case 1:
        # To reach floor 1, we must jump directly from the lobby to floor 1.
        # Therefore the minimum cost is simply the fee of floor 1.
        dp[1] = cost[0]

        # Base case 2:
        # To reach floor 2, we can jump directly from the lobby to floor 2.
        # Since the problem allows starting with either a 1-floor or 2-floor move,
        # the minimum cost to reach floor 2 is just the fee of floor 2 itself.
        dp[2] = cost[1]

        # Now fill the DP table from floor 3 up to floor n.
        # For each floor i, there are only two possible previous floors:
        # - i - 1, if we take a 1-floor move
        # - i - 2, if we take a 2-floor move
        #
        # We choose the cheaper way to reach one of those previous floors,
        # then add the fee for landing on floor i.
        for i in range(3, n + 1):
            # Cost if we come from the previous floor using a 1-floor move.
            one_step_before: int = dp[i - 1]

            # Cost if we come from two floors below using a 2-floor move.
            two_steps_before: int = dp[i - 2]

            # The current floor's landing fee.
            current_floor_fee: int = cost[i - 1]

            # Choose the cheaper previous route, then pay the current floor's fee.
            dp[i] = min(one_step_before, two_steps_before) + current_floor_fee

        # The answer is the minimum cost to reach exactly floor n.
        return dp[n]

    def min_cost_to_reach_office_optimized(self, cost: List[int]) -> int:
        """
        Compute the minimum total fee needed to reach exactly the top floor
        using a space-optimized dynamic programming approach.

        Instead of storing the entire DP table, this method keeps only the
        last two DP values, because each new state depends only on the previous
        two states.

        Args:
            cost: A list where cost[i] is the fee to land on floor i + 1.

        Returns:
            The minimum total fee required to reach exactly floor n.

        Time complexity:
            O(n), where n is the number of floors.

        Space complexity:
            O(1), excluding the input list.
        """
        n: int = len(cost)

        # If there is only one floor, we must land on it directly.
        if n == 1:
            return cost[0]

        # prev2 will represent dp[i - 2]
        # Initially, for i = 3:
        # prev2 = dp[1] = cost[0]
        prev2: int = cost[0]

        # prev1 will represent dp[i - 1]
        # Initially, for i = 3:
        # prev1 = dp[2] = cost[1]
        prev1: int = cost[1]

        # Build the answer floor by floor, but only keep the last two results.
        for i in range(3, n + 1):
            current: int = min(prev1, prev2) + cost[i - 1]

            # Shift the window forward:
            # - old prev1 becomes new prev2
            # - current becomes new prev1
            prev2, prev1 = prev1, current

        return prev1


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1
    cost1: List[int] = [4, 2, 7, 3]
    result1: int = solution.min_cost_to_reach_office(cost1)
    print(f"Input: {cost1}")
    print(f"Minimum cost to reach the office: {result1}")
    print()

    # Sample input 2
    # The correct answer for this example is 4.
    cost2: List[int] = [1, 100, 1, 1, 100, 1]
    result2: int = solution.min_cost_to_reach_office(cost2)
    print(f"Input: {cost2}")
    print(f"Minimum cost to reach the office: {result2}")
    print()

    # Demonstrate the optimized version produces the same results.
    optimized_result1: int = solution.min_cost_to_reach_office_optimized(cost1)
    optimized_result2: int = solution.min_cost_to_reach_office_optimized(cost2)

    print("Using space-optimized DP:")
    print(f"Input: {cost1} -> {optimized_result1}")
    print(f"Input: {cost2} -> {optimized_result2}")