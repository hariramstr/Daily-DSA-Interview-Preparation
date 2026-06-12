"""
Title: Minimum Adjustments to Create K Rising Price Blocks

Problem Description:
You are given an integer array prices of length n and an integer k. You may change the
value of any element to any other integer, and the cost of changing prices[i] to x is
|prices[i] - x|. Your goal is to partition the array into exactly k non-empty contiguous
blocks such that after applying some changes, every block becomes non-decreasing.

In other words, if a block covers indices [l..r], then after modification its values must
satisfy:
    a[l] <= a[l + 1] <= ... <= a[r]

Return the minimum total adjustment cost required to transform the array so that it can be
split into exactly k contiguous non-decreasing blocks.

The partition boundaries are your choice, and the modified values in different blocks do
not need to relate to each other. A block of length 1 is always non-decreasing.

Constraints:
- 1 <= n <= 200
- 1 <= k <= n
- -10^9 <= prices[i] <= 10^9

Notes:
This solution computes:
1. The minimum cost to transform every subarray into a non-decreasing sequence.
2. A dynamic programming solution over the number of blocks.

Important note about the statement examples:
The first example text in the prompt is internally inconsistent. The algorithm below solves
the mathematically defined problem exactly, so the printed result reflects the true optimum
for the formal problem definition.
"""

from typing import List


class Solution:
    def _compute_interval_costs(self, prices: List[int]) -> List[List[int]]:
        """
        Precompute the minimum cost to make every subarray non-decreasing.

        For each interval prices[l..r], we compute the minimum possible value of:
            sum |prices[i] - b[i]|
        subject to:
            b[l] <= b[l + 1] <= ... <= b[r]

        Key fact used:
        In an optimal solution under L1 distance, every chosen block value can be taken
        from the set of original values inside that interval. Therefore we can run a DP
        over the sorted unique values of the interval.

        Args:
            prices: Original array.

        Returns:
            A 2D matrix cost where cost[l][r] is the minimum adjustment cost needed to
            make prices[l..r] non-decreasing.

        Time complexity:
            O(n^4) in the worst case, which is acceptable for n <= 200.

        Space complexity:
            O(n^2)
        """
        n: int = len(prices)

        # cost[l][r] will store the answer for subarray prices[l..r].
        cost: List[List[int]] = [[0] * n for _ in range(n)]

        # We process each starting index independently.
        # For a fixed l, we extend the right boundary r one step at a time.
        # For each interval [l..r], we:
        #   1. Collect the values in that interval.
        #   2. Sort and deduplicate them to get candidate target values.
        #   3. Run a DP that enforces non-decreasing chosen values.
        #
        # DP meaning for a fixed interval [l..r]:
        #   Let vals be the sorted unique values from prices[l..r].
        #   dp_prev[t] = minimum cost to transform prices[l..i-1] so that the last chosen
        #                transformed value is exactly vals[t], and the sequence is
        #                non-decreasing.
        #
        # Transition for next element x = prices[i]:
        #   new_dp[t] = min over s <= t of dp_prev[s] + |x - vals[t]|
        #
        # We optimize this by maintaining a prefix minimum over dp_prev.
        for l in range(n):
            interval_values: List[int] = []

            for r in range(l, n):
                interval_values.append(prices[r])

                # Candidate transformed values for this interval.
                vals: List[int] = sorted(set(interval_values))
                m: int = len(vals)

                # Initialize DP for the first element in the interval.
                dp_prev: List[int] = [abs(prices[l] - v) for v in vals]

                # Extend DP through the rest of the interval.
                for i in range(l + 1, r + 1):
                    x: int = prices[i]
                    dp_curr: List[int] = [0] * m

                    # prefix_best will hold:
                    #   min(dp_prev[0], dp_prev[1], ..., dp_prev[t])
                    # as we sweep t from left to right.
                    prefix_best: int = dp_prev[0]
                    dp_curr[0] = prefix_best + abs(x - vals[0])

                    for t in range(1, m):
                        if dp_prev[t] < prefix_best:
                            prefix_best = dp_prev[t]
                        dp_curr[t] = prefix_best + abs(x - vals[t])

                    dp_prev = dp_curr

                cost[l][r] = min(dp_prev)

        return cost

    def min_adjustment_cost(self, prices: List[int], k: int) -> int:
        """
        Compute the minimum total adjustment cost to split the array into exactly k
        contiguous non-empty blocks, where each block becomes non-decreasing after edits.

        The algorithm has two major stages:
        1. Precompute interval costs:
           cost[l][r] = minimum cost to make prices[l..r] non-decreasing.
        2. Dynamic programming over partitions:
           dp[b][i] = minimum cost to split the first i elements into exactly b blocks.

        Transition:
            dp[b][i] = min(dp[b - 1][j] + cost[j][i - 1]) for b - 1 <= j < i

        Here, the last block is prices[j..i-1], and the first j elements are split into
        b - 1 blocks.

        Args:
            prices: Original array of prices.
            k: Exact number of contiguous blocks required.

        Returns:
            Minimum total adjustment cost.

        Time complexity:
            O(n^4 + k * n^2)

        Space complexity:
            O(n^2 + k * n)
        """
        n: int = len(prices)

        # Precompute the cost of fixing every possible interval.
        cost: List[List[int]] = self._compute_interval_costs(prices)

        # Use a large number as "infinity" for minimization DP.
        inf: int = 10**30

        # dp[b][i] means:
        #   minimum cost to partition the first i elements (prices[0..i-1])
        #   into exactly b non-empty contiguous blocks.
        dp: List[List[int]] = [[inf] * (n + 1) for _ in range(k + 1)]
        dp[0][0] = 0

        # Build the answer block by block.
        for blocks in range(1, k + 1):
            # To form 'blocks' non-empty blocks from the first i elements,
            # we must have at least i >= blocks.
            for i in range(blocks, n + 1):
                best: int = inf

                # j is the starting index of the last block in 0-based array indexing.
                # The first j elements are split into blocks - 1 blocks.
                # The last block is prices[j..i-1].
                #
                # Since each block must be non-empty, j must satisfy:
                #   blocks - 1 <= j <= i - 1
                for j in range(blocks - 1, i):
                    candidate: int = dp[blocks - 1][j] + cost[j][i - 1]
                    if candidate < best:
                        best = candidate

                dp[blocks][i] = best

        return dp[k][n]


if __name__ == "__main__":
    solution = Solution()

    # Sample 1 from the prompt.
    # The prompt's explanation is inconsistent, so this code prints the true optimum
    # for the formal problem definition.
    prices1: List[int] = [5, 1, 4, 2]
    k1: int = 2
    result1: int = solution.min_adjustment_cost(prices1, k1)
    print(f"prices = {prices1}, k = {k1} -> minimum cost = {result1}")

    # Sample 2 from the prompt.
    prices2: List[int] = [7, 3, 6, 3, 8]
    k2: int = 3
    result2: int = solution.min_adjustment_cost(prices2, k2)
    print(f"prices = {prices2}, k = {k2} -> minimum cost = {result2}")

    # Additional quick sanity checks.
    prices3: List[int] = [1, 2, 3, 4]
    k3: int = 1
    result3: int = solution.min_adjustment_cost(prices3, k3)
    print(f"prices = {prices3}, k = {k3} -> minimum cost = {result3}")

    prices4: List[int] = [4, 3, 2, 1]
    k4: int = 4
    result4: int = solution.min_adjustment_cost(prices4, k4)
    print(f"prices = {prices4}, k = {k4} -> minimum cost = {result4}")