"""
Title: Minimum Delay to Assemble a Multi-Stage Rocket

Problem Description:
A space manufacturing team must assemble a rocket from stage 1 to stage n in order.
For each stage i, you are given two values:
- buildTime[i]: the number of hours required to physically assemble that stage
- stability[i]: an integer rating of how stable that stage is once completed

The team may group consecutive stages into a single assembly batch.
If stages l..r are assembled in one batch, the batch takes:

    sum(buildTime[l..r]) + (max(stability[l..r]) - min(stability[l..r]))

You must partition the n stages into exactly k non-empty consecutive batches.
Return the minimum possible total cost.

Constraints:
- 1 <= k <= n <= 300
- 1 <= buildTime[i] <= 10^6
- 1 <= stability[i] <= 10^9
- The answer fits in a 64-bit signed integer.
"""

from typing import List


class Solution:
    def _precompute_interval_costs(
        self, build_time: List[int], stability: List[int]
    ) -> List[List[int]]:
        """
        Precompute the cost of every interval [l..r].

        For each possible starting index l, we expand the interval to the right,
        maintaining:
        - running sum of build times
        - current maximum stability
        - current minimum stability

        Then:
            cost[l][r] = sum(build_time[l..r]) + max(stability[l..r]) - min(stability[l..r])

        Args:
            build_time: List of assembly times for each stage.
            stability: List of stability values for each stage.

        Returns:
            A 2D matrix where cost[l][r] is the batch cost for stages l..r inclusive.

        Time complexity:
            O(n^2)

        Space complexity:
            O(n^2)
        """
        n: int = len(build_time)

        # Create an n x n table initialized with 0.
        # We will only fill entries where l <= r.
        cost: List[List[int]] = [[0] * n for _ in range(n)]

        # We compute interval costs by fixing the left boundary l
        # and extending the right boundary r one step at a time.
        for l in range(n):
            running_sum: int = 0
            current_max: int = stability[l]
            current_min: int = stability[l]

            for r in range(l, n):
                # Add the new stage's build time into the running interval sum.
                running_sum += build_time[r]

                # Update the interval maximum stability.
                if stability[r] > current_max:
                    current_max = stability[r]

                # Update the interval minimum stability.
                if stability[r] < current_min:
                    current_min = stability[r]

                # Store the full interval cost.
                cost[l][r] = running_sum + (current_max - current_min)

        return cost

    def minimum_total_cost(
        self, buildTime: List[int], stability: List[int], k: int
    ) -> int:
        """
        Compute the minimum total cost to partition the stages into exactly k
        non-empty consecutive batches.

        Dynamic programming idea:
        - Let dp[b][i] be the minimum cost to partition the first i stages
          into exactly b batches.
        - Transition:
              dp[b][i] = min(dp[b - 1][j] + cost[j][i - 1])
          where:
              - j is the starting index of the last batch in 0-based indexing
              - first j stages are partitioned into b - 1 batches
              - stages j..i-1 form the last batch
        - We require each batch to be non-empty, so:
              b - 1 <= j <= i - 1

        Args:
            buildTime: List of build times.
            stability: List of stability values.
            k: Exact number of consecutive non-empty batches.

        Returns:
            The minimum possible total cost.

        Time complexity:
            O(n^2 + k * n^2), which is O(k * n^2)

        Space complexity:
            O(n^2 + k * n), dominated by O(n^2) for interval costs
        """
        n: int = len(buildTime)

        # Precompute every possible batch cost.
        # This is extremely helpful because the DP transition repeatedly asks:
        # "What is the cost of making stages j..i-1 one batch?"
        # Without precomputation, we would recompute max/min/sum many times.
        cost: List[List[int]] = self._precompute_interval_costs(buildTime, stability)

        # Use a very large number as "infinity" for minimization DP.
        inf: int = 10**30

        # dp[b][i] means:
        # minimum cost to partition the first i stages (stages 0..i-1)
        # into exactly b non-empty consecutive batches.
        #
        # Dimensions:
        # - b ranges from 0..k
        # - i ranges from 0..n
        dp: List[List[int]] = [[inf] * (n + 1) for _ in range(k + 1)]

        # Base case:
        # Partitioning 0 stages into 0 batches costs 0.
        dp[0][0] = 0

        # Fill the DP table batch by batch.
        for batches in range(1, k + 1):
            # To split i stages into 'batches' non-empty groups,
            # we need at least 'batches' stages.
            #
            # Also, if we have i stages and want 'batches' groups,
            # the last group must start at some j where:
            # - the first j stages use batches - 1 groups
            # - j must be at least batches - 1
            # - j must be at most i - 1 so the last group is non-empty
            for i in range(batches, n + 1):
                best: int = inf

                # Try every valid starting point j for the last batch.
                # The last batch is stages j..i-1.
                for j in range(batches - 1, i):
                    previous_cost: int = dp[batches - 1][j]

                    # If the previous state is impossible, skip it.
                    if previous_cost == inf:
                        continue

                    current_cost: int = previous_cost + cost[j][i - 1]

                    if current_cost < best:
                        best = current_cost

                dp[batches][i] = best

        return dp[k][n]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    build_time_1: List[int] = [4, 2, 7, 3]
    stability_1: List[int] = [8, 5, 6, 10]
    k_1: int = 2
    result_1: int = solution.minimum_total_cost(build_time_1, stability_1, k_1)
    print("Example 1 Result:", result_1)

    # Note:
    # The problem statement claims the answer is 18, but if we enumerate all
    # valid 2-batch partitions:
    #
    # 1) [4] | [2,7,3]
    #    cost = 4 + (2+7+3 + (10-5)) = 4 + 17 = 21
    #
    # 2) [4,2] | [7,3]
    #    cost = (6 + (8-5)) + (10 + (10-6)) = 9 + 14 = 23
    #
    # 3) [4,2,7] | [3]
    #    cost = (13 + (8-5)) + 3 = 16 + 3 = 19
    #
    # Therefore the true minimum is 19, not 18.
    # The algorithm correctly returns 19.

    # Example 2
    build_time_2: List[int] = [5, 1, 4, 2, 6]
    stability_2: List[int] = [7, 7, 3, 9, 4]
    k_2: int = 3
    result_2: int = solution.minimum_total_cost(build_time_2, stability_2, k_2)
    print("Example 2 Result:", result_2)

    # Additional quick sanity check
    build_time_3: List[int] = [1, 2, 3]
    stability_3: List[int] = [5, 5, 5]
    k_3: int = 2
    result_3: int = solution.minimum_total_cost(build_time_3, stability_3, k_3)
    print("Additional Test Result:", result_3)