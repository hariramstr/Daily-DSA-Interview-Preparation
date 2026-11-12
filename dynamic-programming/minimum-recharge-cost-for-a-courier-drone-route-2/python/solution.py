"""
Title: Minimum Recharge Cost for a Courier Drone Route

Problem Description:
A courier drone must travel through a fixed sequence of delivery checkpoints from left to right.
The route is represented by an integer array `cost`, where `cost[i]` is the energy cost to land
on checkpoint `i` and recharge there. The drone starts before checkpoint `0` and wants to reach
exactly the last checkpoint `n - 1`.

From checkpoint `i`, the drone may fly forward by either 1 or 2 checkpoints. However, some
checkpoints are marked as windy and cannot be used for landing. You are given a binary array
`blocked` of the same length, where `blocked[i] = 1` means checkpoint `i` is unavailable and the
drone may not land there. The first and last checkpoints are guaranteed to be unblocked.

The drone pays the recharge cost only for checkpoints it lands on, including the final checkpoint.
It does not pay anything before the route begins. Return the minimum total recharge cost needed to
reach the last checkpoint. If the last checkpoint cannot be reached, return `-1`.

This is a path optimization problem where local greedy choices may fail because taking a slightly
more expensive checkpoint now may avoid a blocked or costly future path.

Constraints:
- 2 <= n <= 100000
- cost.length == blocked.length == n
- 0 <= cost[i] <= 1000000000
- blocked[i] is either 0 or 1
- blocked[0] = 0
- blocked[n - 1] = 0
"""

from typing import List


class Solution:
    def min_recharge_cost(self, cost: List[int], blocked: List[int]) -> int:
        """
        Compute the minimum total recharge cost needed for the drone to reach
        the last checkpoint, or return -1 if it is impossible.

        The drone starts before index 0, so its first landing may be on
        checkpoint 0 or checkpoint 1, as long as that checkpoint is not blocked.
        After landing on checkpoint i, it may move to i + 1 or i + 2.

        Args:
            cost: A list where cost[i] is the recharge cost for landing on checkpoint i.
            blocked: A binary list where blocked[i] == 1 means checkpoint i cannot be used.

        Returns:
            The minimum total recharge cost to reach checkpoint n - 1, or -1 if unreachable.

        Time complexity:
            O(n), because we process each checkpoint exactly once.

        Space complexity:
            O(1), excluding the input arrays, because we only keep the last two DP values.
        """
        n: int = len(cost)

        # We use a very large number to represent an unreachable state.
        # Python integers do not overflow in normal use, so this is safe.
        inf: int = 10**30

        # dp[i] concept:
        #   minimum total cost to land on checkpoint i.
        #
        # Transition:
        #   dp[i] = cost[i] + min(dp[i - 1], dp[i - 2])
        #
        # But there are two important special rules:
        # 1. If checkpoint i is blocked, then dp[i] is unreachable.
        # 2. The drone starts before the array, so it may initially land on:
        #       - checkpoint 0
        #       - checkpoint 1
        #    without paying any "previous" movement cost.
        #
        # Because each state only depends on the previous two states,
        # we do not need a full DP array. We can store only:
        #   prev2 = dp[i - 2]
        #   prev1 = dp[i - 1]

        # Initialize dp[0].
        # Since blocked[0] is guaranteed to be 0, landing on checkpoint 0 is allowed.
        # Starting before the route means the drone can choose to land here first,
        # paying exactly cost[0].
        dp0: int = cost[0]

        # Initialize dp[1].
        # The drone may also choose to start directly on checkpoint 1,
        # as long as it is not blocked.
        #
        # There are two possible ways to land on checkpoint 1:
        #   1. Start directly on 1 -> cost[1]
        #   2. Start on 0, then move to 1 -> dp0 + cost[1]
        #
        # Since costs are non-negative, starting directly on 1 is never worse than
        # going 0 -> 1. So the minimum is simply cost[1], if checkpoint 1 is unblocked.
        if blocked[1] == 1:
            dp1: int = inf
        else:
            dp1 = cost[1]

        # If there are only two checkpoints, the answer is simply dp[1],
        # because the destination is checkpoint n - 1 = 1.
        if n == 2:
            return -1 if dp1 >= inf else dp1

        # Prepare rolling DP variables.
        prev2: int = dp0
        prev1: int = dp1

        # Process checkpoints from index 2 to index n - 1.
        for i in range(2, n):
            # If this checkpoint is blocked, the drone cannot land here at all.
            # Therefore this state is unreachable.
            if blocked[i] == 1:
                current: int = inf
            else:
                # To land on checkpoint i, the drone must come from:
                #   - checkpoint i - 1
                #   - checkpoint i - 2
                #
                # We choose the cheaper reachable option.
                best_previous: int = min(prev1, prev2)

                # If both previous states are unreachable, then this checkpoint
                # is also unreachable.
                if best_previous >= inf:
                    current = inf
                else:
                    # Otherwise, pay the cost of landing on checkpoint i.
                    current = best_previous + cost[i]

            # Shift the rolling window forward:
            #   old prev1 becomes new prev2
            #   current becomes new prev1
            prev2, prev1 = prev1, current

        # After the loop, prev1 holds dp[n - 1], the minimum cost to reach the last checkpoint.
        return -1 if prev1 >= inf else prev1


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # cost = [4, 2, 7, 3, 5]
    # blocked = [0, 0, 1, 0, 0]
    #
    # Valid reasoning:
    # - Start at 0: cost 4
    # - Start at 1: cost 2
    # - Checkpoint 2 is blocked
    # - From 1, can go to 3, then 4
    #   total = 2 + 3 + 5 = 10
    #
    # Expected output: 10
    cost1: List[int] = [4, 2, 7, 3, 5]
    blocked1: List[int] = [0, 0, 1, 0, 0]
    result1: int = solution.min_recharge_cost(cost1, blocked1)
    print(result1)

    # Example 2
    # cost = [6, 1, 9, 2]
    # blocked = [0, 1, 1, 0]
    #
    # - Start at 0 is allowed, but from 0 both 1 and 2 are blocked
    # - Start at 1 is not allowed because 1 is blocked
    # Therefore the last checkpoint cannot be reached.
    #
    # Expected output: -1
    cost2: List[int] = [6, 1, 9, 2]
    blocked2: List[int] = [0, 1, 1, 0]
    result2: int = solution.min_recharge_cost(cost2, blocked2)
    print(result2)