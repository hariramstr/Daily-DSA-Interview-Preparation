"""
Title: Maximum Coins from Non-Adjacent Arcade Machines

Problem Description:
You are managing a row of arcade machines in a game center. Each machine contains a
certain number of collectible coins, given by the array `coins`, where `coins[i]`
is the number of coins inside the `i`th machine. If you collect coins from one
machine, the security system prevents you from collecting from its immediate
neighboring machines on the same night.

Your task is to determine the maximum number of coins you can collect in one night
without collecting from two adjacent machines.

This is a classic decision-making problem where, for each machine, you can either
skip it or collect from it and then skip its neighbor. Return the largest total
number of coins possible.

Constraints:
- 1 <= coins.length <= 100
- 0 <= coins[i] <= 1000

Example 1:
Input: coins = [4, 2, 7, 9, 3]
Output: 14

Explanation:
We cannot collect from adjacent machines.
Possible strong choices include:
- 4 + 7 + 3 = 14
- 4 + 9 = 13
- 7 + 3 = 10
So the maximum valid total is 14.

Example 2:
Input: coins = [10, 1, 1, 10]
Output: 20

Explanation:
Collect from the first and last machines.
They are not adjacent, so the total is 10 + 10 = 20.
"""

from typing import List


class Solution:
    def max_coins(self, coins: List[int]) -> int:
        """
        Compute the maximum number of coins that can be collected from non-adjacent machines.

        This uses dynamic programming. For each machine, we decide between:
        1. Skipping the current machine
        2. Taking the current machine, which means we must skip the previous one

        Args:
            coins: A list where coins[i] is the number of coins in the i-th machine.

        Returns:
            The maximum number of coins that can be collected without taking
            coins from two adjacent machines.

        Time complexity:
            O(n), where n is the number of machines.

        Space complexity:
            O(1), because we only store the last two dynamic programming states.
        """
        # If there are no machines, the maximum collectible coins is 0.
        # The problem guarantees at least one machine, but this makes the method safer.
        if not coins:
            return 0

        # `prev_two` represents the best answer for the subproblem ending two positions back.
        # In DP terms, this is similar to dp[i - 2].
        prev_two: int = 0

        # `prev_one` represents the best answer for the subproblem ending one position back.
        # In DP terms, this is similar to dp[i - 1].
        prev_one: int = 0

        # We process each machine from left to right.
        # At each machine, we make a local decision that contributes to the global optimum.
        for index, value in enumerate(coins):
            # Option 1: Skip the current machine.
            # If we skip it, the best total remains whatever we had up to the previous machine.
            skip_current: int = prev_one

            # Option 2: Take the current machine.
            # If we take it, we cannot take the previous machine,
            # so we add the current machine's coins to the best total from two machines back.
            take_current: int = prev_two + value

            # The best answer at this position is the better of:
            # - skipping the current machine
            # - taking the current machine
            current_best: int = max(skip_current, take_current)

            # Move the DP window forward:
            # - the old `prev_one` becomes the new `prev_two`
            # - the new best becomes `prev_one`
            prev_two = prev_one
            prev_one = current_best

        # After processing all machines, `prev_one` holds the best possible answer.
        return prev_one

    def max_coins_with_trace(self, coins: List[int]) -> int:
        """
        Compute the maximum number of coins while printing a beginner-friendly trace.

        This helper method is useful for understanding how the dynamic programming
        solution evolves step by step.

        Args:
            coins: A list where coins[i] is the number of coins in the i-th machine.

        Returns:
            The maximum number of coins that can be collected without taking
            coins from two adjacent machines.

        Time complexity:
            O(n), where n is the number of machines.

        Space complexity:
            O(1), excluding printed output.
        """
        if not coins:
            print("No machines available. Maximum coins = 0")
            return 0

        prev_two: int = 0
        prev_one: int = 0

        print(f"Tracing coins array: {coins}")
        print("We maintain two values:")
        print("- prev_two = best answer up to index i-2")
        print("- prev_one = best answer up to index i-1")
        print()

        for index, value in enumerate(coins):
            skip_current: int = prev_one
            take_current: int = prev_two + value
            current_best: int = max(skip_current, take_current)

            print(f"Index {index}, machine coins = {value}")
            print(f"  Skip current -> total stays {skip_current}")
            print(f"  Take current -> prev_two ({prev_two}) + {value} = {take_current}")
            print(f"  Best at this step = max({skip_current}, {take_current}) = {current_best}")
            print()

            prev_two = prev_one
            prev_one = current_best

        print(f"Final maximum coins = {prev_one}")
        print()
        return prev_one


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the problem statement.
    # Correct answer:
    # [4, 2, 7, 9, 3]
    # Best valid choice is 4 + 7 + 3 = 14
    coins1: List[int] = [4, 2, 7, 9, 3]
    result1: int = solution.max_coins(coins1)
    print(f"Input: {coins1}")
    print(f"Output: {result1}")
    print("Expected: 14")
    print()

    # Sample input 2 from the problem statement.
    # Correct answer:
    # [10, 1, 1, 10]
    # Best valid choice is 10 + 10 = 20
    coins2: List[int] = [10, 1, 1, 10]
    result2: int = solution.max_coins(coins2)
    print(f"Input: {coins2}")
    print(f"Output: {result2}")
    print("Expected: 20")
    print()

    # Additional beginner-friendly trace for the provided examples.
    print("Detailed trace for Example 1:")
    solution.max_coins_with_trace(coins1)

    print("Detailed trace for Example 2:")
    solution.max_coins_with_trace(coins2)