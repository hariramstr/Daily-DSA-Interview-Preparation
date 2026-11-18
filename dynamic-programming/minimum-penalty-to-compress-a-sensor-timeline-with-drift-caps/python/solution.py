"""
Title: Minimum Penalty to Compress a Sensor Timeline with Drift Caps

Problem Description:
You are given a time-ordered array `readings` of length `n`, where `readings[i]` is the
value reported by a sensor at second `i`. To reduce storage, you want to compress the
timeline into contiguous blocks. Each block [l, r] is stored using a single representative
value, and every reading in that block is reconstructed as that representative value.

For any block [l, r], the representative value may be chosen arbitrarily, but the block is
valid only if the difference between the maximum and minimum reading inside the block is at
most `driftCap`. If a block is valid, its compression penalty is the sum of absolute
differences between each reading in the block and the chosen representative value. Since the
representative may be chosen freely, the minimum penalty for a valid block is achieved by
choosing a median of the readings in that block.

You must partition the entire array into exactly `k` non-empty contiguous valid blocks and
return the minimum possible total compression penalty. If it is impossible to partition the
array into exactly `k` valid blocks, return -1.

Constraints:
- 1 <= n <= 300
- 1 <= k <= n
- 0 <= readings[i] <= 10^9
- 0 <= driftCap <= 10^9

Key idea:
1. Precompute the minimum penalty for every contiguous subarray [l, r], but only if that
   subarray is valid under the drift cap.
2. Then run dynamic programming:
      dp[p][i] = minimum penalty to partition the first i readings into exactly p blocks.
"""

from bisect import bisect_left, insort
from typing import List


class Solution:
    def _compute_subarray_costs(self, readings: List[int], drift_cap: int) -> List[List[int]]:
        """
        Precompute the minimum compression penalty for every valid contiguous subarray.

        For each starting index l, we extend the right end r one step at a time.
        We maintain:
        - a sorted list of values in readings[l:r+1] so we can get a median
        - the current minimum and maximum values to test validity quickly enough
        Once a subarray becomes invalid due to max - min > drift_cap, extending it further
        cannot make it valid again, so we stop for that starting index.

        The penalty of a valid subarray is:
            sum(abs(x - median)) for x in subarray

        Args:
            readings: The original sensor readings.
            drift_cap: Maximum allowed range (max - min) inside a valid block.

        Returns:
            A 2D matrix cost where cost[l][r] is the minimum penalty for subarray [l, r]
            if valid, otherwise a large sentinel value.

        Time complexity:
            O(n^3 log n) in the worst case:
            - O(n^2) subarrays
            - each insertion into a sorted list is O(n)
            - each cost computation scans the current subarray O(n)
            Given n <= 300, this is acceptable and keeps the implementation clear.

        Space complexity:
            O(n^2) for the cost table.
        """
        n: int = len(readings)
        inf: int = 10**30

        # cost[l][r] will store the minimum penalty for the contiguous block readings[l:r+1]
        # if that block is valid. Otherwise it remains INF.
        cost: List[List[int]] = [[inf] * n for _ in range(n)]

        # We process every possible left boundary independently.
        for left in range(n):
            # This sorted list stores the values of the current subarray readings[left:right+1]
            # in sorted order. This makes median lookup easy.
            sorted_values: List[int] = []

            # Track the minimum and maximum values in the current subarray.
            # These are needed to check the drift cap validity condition.
            current_min: int = readings[left]
            current_max: int = readings[left]

            # Extend the right boundary one step at a time.
            for right in range(left, n):
                value: int = readings[right]

                # Insert the new value into the sorted list while preserving sorted order.
                insort(sorted_values, value)

                # Update min and max for the current subarray.
                if value < current_min:
                    current_min = value
                if value > current_max:
                    current_max = value

                # If the subarray violates the drift cap, then any longer subarray starting
                # at the same 'left' will also violate it, because adding more elements
                # cannot reduce the existing max-min range below the current one.
                if current_max - current_min > drift_cap:
                    break

                # The subarray is valid. Its minimum sum of absolute deviations is achieved
                # by any median. For simplicity, we use the lower median when the length is even.
                length: int = right - left + 1
                median: int = sorted_values[(length - 1) // 2]

                # Compute the exact penalty for this valid subarray.
                # Since n is only 300, a direct scan is simple and safe.
                penalty: int = 0
                for x in sorted_values:
                    penalty += abs(x - median)

                cost[left][right] = penalty

        return cost

    def minimum_penalty(self, readings: List[int], k: int, drift_cap: int) -> int:
        """
        Compute the minimum total compression penalty using exactly k contiguous valid blocks.

        The algorithm has two main phases:
        1. Precompute the penalty of every valid subarray.
        2. Use dynamic programming to choose exactly k blocks covering the whole array.

        DP definition:
            dp[parts][i] = minimum penalty to partition the first i readings
                           (that is, readings[0:i]) into exactly 'parts' valid blocks.

        Transition:
            Suppose the last block starts at j and ends at i-1.
            Then:
                dp[parts][i] = min(
                    dp[parts - 1][j] + cost[j][i - 1]
                )
            for all j < i where cost[j][i - 1] is valid.

        Args:
            readings: The sensor readings in time order.
            k: Exact number of contiguous non-empty blocks required.
            drift_cap: Maximum allowed range inside each block.

        Returns:
            The minimum total penalty, or -1 if no valid partition into exactly k blocks exists.

        Time complexity:
            O(n^3 log n + k * n^2)
            The precomputation dominates for n <= 300.

        Space complexity:
            O(n^2 + k * n)
            - O(n^2) for subarray costs
            - O(k * n) for DP table
        """
        n: int = len(readings)

        # Quick impossible case:
        # We cannot split n items into more than n non-empty blocks.
        if k > n:
            return -1

        inf: int = 10**30

        # Phase 1: precompute cost of every valid subarray.
        cost: List[List[int]] = self._compute_subarray_costs(readings, drift_cap)

        # Phase 2: dynamic programming.
        # dp[parts][i] = minimum penalty to partition readings[0:i] into exactly 'parts' blocks.
        # Note:
        # - i ranges from 0 to n
        # - dp[0][0] = 0 because zero readings need zero blocks with zero penalty
        # - dp[0][i > 0] is impossible
        dp: List[List[int]] = [[inf] * (n + 1) for _ in range(k + 1)]
        dp[0][0] = 0

        # Build answers for 1 block, 2 blocks, ..., k blocks.
        for parts in range(1, k + 1):
            # To partition the first i readings into 'parts' non-empty blocks,
            # we must have at least i >= parts.
            for i in range(1, n + 1):
                # Try every possible starting position j of the last block.
                # Then the previous prefix is readings[0:j], which must be split into parts-1 blocks.
                for j in range(parts - 1, i):
                    # If the previous state is impossible, skip it.
                    if dp[parts - 1][j] == inf:
                        continue

                    # The last block is readings[j:i], inclusive indices [j, i-1].
                    block_cost: int = cost[j][i - 1]

                    # If this block is invalid, skip it.
                    if block_cost == inf:
                        continue

                    candidate: int = dp[parts - 1][j] + block_cost
                    if candidate < dp[parts][i]:
                        dp[parts][i] = candidate

        answer: int = dp[k][n]
        return -1 if answer == inf else answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # readings = [8, 10, 9, 3, 4], k = 2, driftCap = 2
    # Valid optimal partition:
    #   [8,10,9] -> median 9, penalty = 1 + 1 + 0 = 2
    #   [3,4]    -> median 3 or 4, penalty = 1
    # Total = 3
    #
    # The problem statement's stated output "2" is inconsistent with the actual definition
    # of sum of absolute differences over all readings in all blocks. The mathematically
    # correct minimum is 3.
    readings1 = [8, 10, 9, 3, 4]
    k1 = 2
    drift_cap1 = 2
    print(solution.minimum_penalty(readings1, k1, drift_cap1))  # Correct result: 3

    # Example 2
    readings2 = [1, 7, 2, 8]
    k2 = 2
    drift_cap2 = 3
    print(solution.minimum_penalty(readings2, k2, drift_cap2))  # Expected: -1

    # Additional small sanity checks
    print(solution.minimum_penalty([5], 1, 0))          # 0
    print(solution.minimum_penalty([1, 2, 3], 3, 0))    # 0
    print(solution.minimum_penalty([1, 2, 3], 1, 2))    # 2