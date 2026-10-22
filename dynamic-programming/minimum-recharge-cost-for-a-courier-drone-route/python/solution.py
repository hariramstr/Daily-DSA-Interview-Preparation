"""
Title: Minimum Recharge Cost for a Courier Drone Route

Problem Description:
A courier drone must travel through a fixed sequence of delivery checkpoints numbered
from 0 to n - 1. At checkpoint i, the drone may buy an energy pack that allows it
to fly forward up to jump[i] checkpoints, and buying that pack costs cost[i].

The drone starts at checkpoint 0 and must reach checkpoint n - 1. You may choose to
buy a pack at any checkpoint you land on, including checkpoint 0. After buying a
pack at checkpoint i, the drone may move to any checkpoint j such that:
    i < j <= i + jump[i]

The drone does not need to buy packs at every checkpoint it visits. If a checkpoint
is unreachable, the route cannot be completed.

Return the minimum total recharge cost needed to reach checkpoint n - 1.
If it is impossible, return -1.

This is a path optimization problem on a one-dimensional route. A greedy strategy
does not always work, because a cheap local pack may lead to expensive future
decisions, while a slightly more expensive earlier pack may skip multiple costly
checkpoints.

Constraints:
- 1 <= n <= 5000
- cost.length == n
- jump.length == n
- 0 <= cost[i] <= 10^9
- 0 <= jump[i] <= n
- You are guaranteed that the answer fits in a 64-bit signed integer if a solution exists.
"""

from typing import List


class Solution:
    def min_recharge_cost(self, cost: List[int], jump: List[int]) -> int:
        """
        Compute the minimum total recharge cost needed to reach the last checkpoint.

        We use dynamic programming:
        - dp[i] = minimum total cost required to land on checkpoint i
        - From each reachable checkpoint i, if we buy the pack there, we pay cost[i]
          and can move to every checkpoint in the interval:
              i + 1 ... min(n - 1, i + jump[i])

        The transition is:
            dp[j] = min(dp[j], dp[i] + cost[i]) for all valid j reachable from i

        Args:
            cost: List where cost[i] is the price of buying an energy pack at checkpoint i.
            jump: List where jump[i] is the maximum number of checkpoints the drone can move
                  forward after buying at checkpoint i.

        Returns:
            The minimum total recharge cost to reach checkpoint n - 1, or -1 if impossible.

        Time complexity:
            O(n^2) in the worst case, because from each checkpoint we may update many later
            checkpoints.

        Space complexity:
            O(n) for the dynamic programming array.
        """
        n: int = len(cost)

        # Special case:
        # If there is only one checkpoint, the drone already starts at the destination.
        # No purchase is needed because we are already at checkpoint n - 1.
        if n == 1:
            return 0

        # We use a very large number to represent "unreachable".
        # Any real answer will be smaller because the problem guarantees the valid answer
        # fits in a signed 64-bit integer.
        inf: int = 10**30

        # dp[i] will store the minimum cost required to REACH checkpoint i.
        #
        # Important interpretation:
        # - Reaching checkpoint 0 costs 0 because the drone starts there.
        # - Buying a pack at checkpoint i is charged when we decide to move forward from i.
        # - Therefore, transitions out of i add cost[i].
        dp: List[int] = [inf] * n
        dp[0] = 0

        # Process checkpoints from left to right.
        #
        # Why left to right works:
        # Every move only goes forward to a larger index, so the graph is a DAG
        # (Directed Acyclic Graph). Dynamic programming in index order is therefore valid.
        for i in range(n):
            # If checkpoint i is unreachable, there is no valid way to stand here,
            # so we cannot buy a pack here and cannot continue from here.
            if dp[i] == inf:
                continue

            # If jump[i] == 0, buying here cannot move us anywhere.
            # We can skip the update loop naturally because the reachable interval is empty.
            max_reach: int = min(n - 1, i + jump[i])

            # If max_reach <= i, there are no forward checkpoints reachable from i.
            if max_reach <= i:
                continue

            # If we choose to buy the pack at checkpoint i, the total cost after buying is:
            next_cost: int = dp[i] + cost[i]

            # Update every checkpoint j that can be reached directly from i.
            #
            # This is the core DP transition:
            # - We are already at i with cost dp[i]
            # - We buy the pack at i for cost[i]
            # - Then we may land on any j in (i, i + jump[i]]
            #
            # Since we want the minimum total cost, we take the smaller value.
            for j in range(i + 1, max_reach + 1):
                if next_cost < dp[j]:
                    dp[j] = next_cost

        # If the last checkpoint is still marked unreachable, no valid route exists.
        if dp[n - 1] == inf:
            return -1

        # Otherwise, dp[n - 1] is the minimum total recharge cost.
        return dp[n - 1]

    def solve(self, cost: List[int], jump: List[int]) -> int:
        """
        Wrapper method that calls the main algorithm.

        Args:
            cost: Recharge costs at each checkpoint.
            jump: Maximum forward jump length from each checkpoint.

        Returns:
            Minimum total recharge cost to reach the final checkpoint, or -1 if impossible.

        Time complexity:
            O(n^2)

        Space complexity:
            O(n)
        """
        return self.min_recharge_cost(cost, jump)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # cost = [5, 1, 4, 2]
    # jump = [2, 2, 1, 0]
    #
    # Trace:
    # - Start at 0 with cost 0
    # - Buy at 0 for 5, can go to 1 or 2
    #   => dp[1] = 5, dp[2] = 5
    # - At 1, buy for 1 more, total = 6, can go to 2 or 3
    #   => dp[2] = min(5, 6) = 5
    #   => dp[3] = 6
    # - At 2, buy for 4 more, total = 9, can go to 3
    #   => dp[3] = min(6, 9) = 6
    # Answer = 6
    cost1: List[int] = [5, 1, 4, 2]
    jump1: List[int] = [2, 2, 1, 0]
    result1: int = solution.solve(cost1, jump1)
    print("Example 1 Result:", result1)  # Expected: 6

    # Example 2
    # cost = [3, 2, 7, 1, 5]
    # jump = [1, 0, 2, 1, 0]
    #
    # Trace:
    # - Start at 0 with cost 0
    # - Buy at 0 for 3, can only go to 1
    #   => dp[1] = 3
    # - At 1, jump[1] = 0, so cannot move forward
    # - No other checkpoints become reachable
    # Answer = -1
    cost2: List[int] = [3, 2, 7, 1, 5]
    jump2: List[int] = [1, 0, 2, 1, 0]
    result2: int = solution.solve(cost2, jump2)
    print("Example 2 Result:", result2)  # Expected: -1

    # Additional quick checks
    cost3: List[int] = [10]
    jump3: List[int] = [0]
    result3: int = solution.solve(cost3, jump3)
    print("Single Checkpoint Result:", result3)  # Expected: 0

    cost4: List[int] = [4, 2, 3]
    jump4: List[int] = [2, 0, 0]
    result4: int = solution.solve(cost4, jump4)
    print("Direct Reach Result:", result4)  # Expected: 4