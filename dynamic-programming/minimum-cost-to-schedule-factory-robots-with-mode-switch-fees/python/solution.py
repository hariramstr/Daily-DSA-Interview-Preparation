"""
Title: Minimum Cost to Schedule Factory Robots with Mode Switch Fees

Problem Description:
A factory has n production hours to fill. For each hour i, exactly one robot mode
must be active: mode A or mode B. Running mode A during hour i costs aCost[i],
and running mode B during hour i costs bCost[i]. In addition, switching the
active mode between two consecutive hours has an extra fee switchFee. If the
same mode is used in consecutive hours, no switch fee is charged.

Your task is to compute the minimum total cost to schedule all n hours.

More formally, choose a sequence of modes of length n where each entry is either
A or B. The total cost is the sum of the operating cost of the chosen mode at
each hour, plus switchFee for every index i > 0 where the mode at hour i differs
from the mode at hour i - 1.

Return the minimum possible total cost.

This is a dynamic programming problem because the best choice for the current
hour depends on the mode chosen in the previous hour. An efficient solution
should run in O(n) time.

Constraints:
- 1 <= n <= 100000
- aCost.length == n
- bCost.length == n
- 0 <= aCost[i], bCost[i] <= 1000000000
- 0 <= switchFee <= 1000000000
- The answer fits in a 64-bit signed integer
"""

from typing import List


class Solution:
    def min_schedule_cost(self, aCost: List[int], bCost: List[int], switchFee: int) -> int:
        """
        Compute the minimum total cost to schedule all hours using robot mode A or B.

        We use dynamic programming with two running states:
        - dp_a: minimum total cost up to the current hour if the current hour uses mode A
        - dp_b: minimum total cost up to the current hour if the current hour uses mode B

        For each new hour, we decide whether it is cheaper to:
        - stay in the same mode as the previous hour, or
        - switch from the other mode and pay the switch fee

        Args:
            aCost: Cost of using mode A for each hour.
            bCost: Cost of using mode B for each hour.
            switchFee: Extra fee paid whenever the mode changes between consecutive hours.

        Returns:
            The minimum possible total cost.

        Time complexity:
            O(n), where n is the number of hours.

        Space complexity:
            O(1), because we only keep the previous hour's two DP values.
        """
        n: int = len(aCost)

        # At hour 0, there is no previous hour.
        # So there is no switching fee yet.
        # If we end hour 0 in mode A, the total cost is simply aCost[0].
        # If we end hour 0 in mode B, the total cost is simply bCost[0].
        dp_a: int = aCost[0]
        dp_b: int = bCost[0]

        # Process each remaining hour from left to right.
        # This works because the best answer for hour i only depends on hour i - 1.
        for i in range(1, n):
            # To end the current hour i in mode A, there are exactly two possibilities:
            #
            # 1. Previous hour also ended in mode A
            #    -> no switching fee
            #    -> total = previous dp_a + current A operating cost
            #
            # 2. Previous hour ended in mode B
            #    -> we must switch from B to A
            #    -> total = previous dp_b + switchFee + current A operating cost
            #
            # We choose the cheaper of these two possibilities.
            next_a: int = min(
                dp_a + aCost[i],
                dp_b + switchFee + aCost[i],
            )

            # Similarly, to end the current hour i in mode B:
            #
            # 1. Stay in mode B
            #    -> previous dp_b + current B cost
            #
            # 2. Switch from mode A to mode B
            #    -> previous dp_a + switchFee + current B cost
            #
            # Again, choose the cheaper option.
            next_b: int = min(
                dp_b + bCost[i],
                dp_a + switchFee + bCost[i],
            )

            # Move the DP window forward.
            # After this assignment:
            # - dp_a means the best cost up to hour i ending in A
            # - dp_b means the best cost up to hour i ending in B
            dp_a, dp_b = next_a, next_b

        # After processing all hours, the final schedule may end in either mode A or mode B.
        # We return the cheaper of the two.
        return min(dp_a, dp_b)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    # Important note:
    # The prompt's explanation contains inconsistent arithmetic in several places,
    # but it explicitly states that with switchFee = 3 the correct minimum is 12.
    aCost1: List[int] = [3, 8, 2, 5]
    bCost1: List[int] = [4, 1, 6, 1]
    switchFee1: int = 3
    result1: int = solution.min_schedule_cost(aCost1, bCost1, switchFee1)
    print(result1)  # Expected: 12

    # Example 2 from the prompt.
    # The prompt says "Output: 9" but its own detailed calculation shows the true minimum is 12.
    # Our DP correctly computes the minimum based on the problem definition.
    aCost2: List[int] = [10, 2, 10, 2]
    bCost2: List[int] = [1, 9, 1, 9]
    switchFee2: int = 2
    result2: int = solution.min_schedule_cost(aCost2, bCost2, switchFee2)
    print(result2)  # Correct according to the stated rules: 12

    # Additional small sanity check.
    aCost3: List[int] = [5]
    bCost3: List[int] = [2]
    switchFee3: int = 100
    result3: int = solution.min_schedule_cost(aCost3, bCost3, switchFee3)
    print(result3)  # Expected: 2