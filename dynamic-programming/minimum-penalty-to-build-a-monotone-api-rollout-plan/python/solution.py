"""
Title: Minimum Penalty to Build a Monotone API Rollout Plan

Problem Description:
A backend team is rolling out a new API across N consecutive regions, numbered from 0 to N - 1.
For each region i, the team must choose exactly one rollout level level[i] from 1 to M.
Higher levels enable more features but may cause instability in some regions.
The instability penalty of assigning level j to region i is given by cost[i][j - 1].

To simplify support and documentation, the final rollout plan must be monotone:
if region a comes before region b, then the chosen level for region a cannot be greater
than the chosen level for region b. In other words, the sequence
level[0], level[1], ..., level[N - 1] must be nondecreasing.

Your task is to compute the minimum possible total instability penalty over all valid
monotone rollout plans.

Return the minimum total penalty.

Constraints:
- 1 <= N <= 2000
- 1 <= M <= 2000
- 1 <= N * M <= 4 * 10^6
- 0 <= cost[i][j] <= 10^9
- The answer may exceed 32-bit integer range, so use 64-bit integers.

Key Dynamic Programming Idea:
Let dp[i][j] be the minimum total penalty for regions 0..i if region i is assigned
level j (using 1-based level numbering conceptually, but 0-based indexing in code).

Because the rollout plan must be nondecreasing, if region i uses level j, then region i-1
must use some level k where k <= j.

So:
    dp[i][j] = cost[i][j] + min(dp[i - 1][0], dp[i - 1][1], ..., dp[i - 1][j])

A direct implementation would be too slow if we recompute that minimum for every j.
The optimization is to build prefix minima while scanning left to right:
    prefix_min[j] = min(previous_dp[0..j])

Then:
    current_dp[j] = cost[i][j] + prefix_min[j]

This reduces the transition from O(M) per state to O(1) per state after one linear pass,
for total O(N * M) time.
"""

from typing import List


class Solution:
    def minimum_penalty(self, cost: List[List[int]]) -> int:
        """
        Compute the minimum total instability penalty for a nondecreasing rollout plan.

        Args:
            cost: A 2D list where cost[i][j] is the penalty of assigning level (j + 1)
                  to region i.

        Returns:
            The minimum possible total penalty among all valid nondecreasing assignments.

        Time Complexity:
            O(N * M), where N is the number of regions and M is the number of levels.

        Space Complexity:
            O(M), because we only keep the previous and current DP rows.
        """
        # Defensive handling for empty input.
        # The problem constraints guarantee at least one region and one level,
        # but this makes the method safer and easier to understand in isolation.
        if not cost or not cost[0]:
            return 0

        # Number of regions.
        n: int = len(cost)

        # Number of rollout levels available for each region.
        m: int = len(cost[0])

        # ---------------------------------------------------------------------
        # Base case:
        # For the first region (region 0), there are no previous regions,
        # so choosing level j simply costs cost[0][j].
        #
        # We store this in a 1D DP array:
        # prev_dp[j] = minimum penalty for regions processed so far,
        #              ending with current region assigned level j.
        # ---------------------------------------------------------------------
        prev_dp: List[int] = cost[0][:]

        # ---------------------------------------------------------------------
        # Process each remaining region one by one.
        #
        # Transition:
        # If current region i uses level j, then previous region must have used
        # some level k where k <= j to preserve nondecreasing order.
        #
        # Therefore:
        # current_dp[j] = cost[i][j] + min(prev_dp[0], prev_dp[1], ..., prev_dp[j])
        #
        # To compute this efficiently, we maintain a running prefix minimum while
        # scanning j from left to right.
        # ---------------------------------------------------------------------
        for i in range(1, n):
            # This array will hold DP values for region i.
            current_dp: List[int] = [0] * m

            # running_min will always store:
            # min(prev_dp[0..j]) at the current position j.
            #
            # We initialize it with positive infinity so that the first comparison
            # correctly picks prev_dp[0].
            running_min: int = float("inf")

            # Scan levels from smallest to largest.
            # This order is important because prefix minima depend on all earlier levels.
            for j in range(m):
                # Update the prefix minimum using the previous row's value at level j.
                # After this line:
                # running_min = min(prev_dp[0], prev_dp[1], ..., prev_dp[j])
                if prev_dp[j] < running_min:
                    running_min = prev_dp[j]

                # Now we can compute the best cost if region i ends at level j:
                # - pay the local penalty cost[i][j]
                # - add the best valid previous total ending at any level <= j
                current_dp[j] = cost[i][j] + running_min

            # Move current row into prev_dp for the next iteration.
            prev_dp = current_dp

        # ---------------------------------------------------------------------
        # After processing all regions:
        # prev_dp[j] = minimum total penalty for all regions, ending with final
        # level j.
        #
        # The final answer is the minimum over all possible ending levels.
        # ---------------------------------------------------------------------
        return min(prev_dp)

    def minPenalty(self, cost: List[List[int]]) -> int:
        """
        Wrapper method using a camelCase name for compatibility with common
        interview/platform naming styles.

        Args:
            cost: A 2D list where cost[i][j] is the penalty of assigning level (j + 1)
                  to region i.

        Returns:
            The minimum possible total penalty among all valid nondecreasing assignments.

        Time Complexity:
            O(N * M)

        Space Complexity:
            O(M)
        """
        return self.minimum_penalty(cost)


if __name__ == "__main__":
    # Create a Solution instance.
    solution = Solution()

    # Example 1:
    # cost = [[5,1,4],[6,2,3],[7,3,1]]
    # One optimal monotone plan is levels [2,2,3], which corresponds to
    # 0-based indices [1,1,2].
    # Total penalty = 1 + 2 + 1 = 4
    cost1: List[List[int]] = [
        [5, 1, 4],
        [6, 2, 3],
        [7, 3, 1],
    ]
    result1: int = solution.minimum_penalty(cost1)
    print("Example 1 Output:", result1)  # Expected: 4

    # Example 2:
    # cost = [[1,10,100],[100,1,10],[100,10,1],[1000,1,1]]
    # One optimal monotone plan is [1,2,3,3].
    # Total penalty = 1 + 1 + 1 + 1 = 4
    cost2: List[List[int]] = [
        [1, 10, 100],
        [100, 1, 10],
        [100, 10, 1],
        [1000, 1, 1],
    ]
    result2: int = solution.minimum_penalty(cost2)
    print("Example 2 Output:", result2)  # Expected: 4

    # Additional small sanity check:
    # Only one region, choose the cheapest level directly.
    cost3: List[List[int]] = [
        [9, 2, 7, 3]
    ]
    result3: int = solution.minimum_penalty(cost3)
    print("Additional Test Output:", result3)  # Expected: 2