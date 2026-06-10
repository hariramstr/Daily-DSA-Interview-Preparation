"""
Title: Minimum Penalty to Merge Backup Snapshots

Problem Description:
A storage system keeps a sequence of backup snapshots in chronological order. Each snapshot
has a positive integer size. To reduce metadata overhead, the system wants to repeatedly
merge adjacent groups of snapshots until only one group remains.

When you merge a contiguous group of snapshots from index i to j into one archive, the merge
incurs a penalty equal to the total size of that group plus a fragmentation surcharge.
The fragmentation surcharge is defined as the difference between the maximum snapshot size
and the minimum snapshot size within that same group. After the merge, that entire interval
becomes a single archive whose size is the sum of all snapshot sizes in the interval, and
future merges must still respect the original order of snapshots (that is, only adjacent
groups can be merged).

Your task is to compute the minimum total penalty required to merge all snapshots into a
single archive.

Formally, for any interval [i, j], merging it into one group costs:
(sum of sizes[i..j]) + (max(sizes[i..j]) - min(sizes[i..j]))

You may choose the order of merges, but every merge must combine two adjacent already-formed
groups.

Return the minimum possible total penalty.

Constraints:
- 1 <= n <= 300
- 1 <= sizes[i] <= 10^6
- The answer can be large, so use 64-bit integers.

Important note about the examples:
The textual examples in the prompt are internally inconsistent for the classic interval-merge
model they describe. Under the stated rules, the correct interval DP recurrence is:

    dp[i][j] = min(dp[i][k] + dp[k + 1][j]) + cost(i, j)

where:
    cost(i, j) = sum(i..j) + max(i..j) - min(i..j)

This is the natural generalization of the standard "merge adjacent piles" / "optimal merge
of intervals" dynamic programming pattern, with an added interval surcharge.

This implementation follows the formal problem statement exactly.
"""

from typing import List


class Solution:
    def min_merge_penalty(self, sizes: List[int]) -> int:
        """
        Compute the minimum total penalty to merge all snapshots into one archive.

        We use interval dynamic programming:
        - dp[i][j] stores the minimum penalty needed to merge the subarray sizes[i..j]
          into a single archive.
        - For a single element, no merge is needed, so dp[i][i] = 0.
        - For a larger interval [i, j], the final merge must combine two adjacent
          already-merged groups:
                [i..k] and [k+1..j]
          Therefore:
                dp[i][j] = min(dp[i][k] + dp[k+1][j]) + interval_cost(i, j)

        The interval cost is:
                sum(sizes[i..j]) + max(sizes[i..j]) - min(sizes[i..j])

        To make interval cost queries O(1), we precompute:
        - prefix sums for interval sums
        - 2D tables for interval minimum and maximum values

        Args:
            sizes: List of positive integers representing snapshot sizes.

        Returns:
            The minimum total penalty as an integer.

        Time complexity:
            O(n^3), where n is the number of snapshots.
            - O(n^2) preprocessing for min/max tables
            - O(n^3) for interval DP transitions

        Space complexity:
            O(n^2) for dp, interval min table, and interval max table.
        """
        n: int = len(sizes)

        # If there are zero or one snapshots, no merge is needed.
        # The constraints say n >= 1, but handling n <= 1 makes the method robust.
        if n <= 1:
            return 0

        # ---------------------------------------------------------------------
        # Step 1: Prefix sums for O(1) interval sum queries.
        #
        # prefix[x] will store the sum of the first x elements:
        #   prefix[0] = 0
        #   prefix[1] = sizes[0]
        #   prefix[2] = sizes[0] + sizes[1]
        #   ...
        #
        # Then the sum of interval [i, j] is:
        #   prefix[j + 1] - prefix[i]
        # ---------------------------------------------------------------------
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + sizes[i]

        # ---------------------------------------------------------------------
        # Step 2: Precompute interval minimums and maximums.
        #
        # min_val[i][j] = minimum value in sizes[i..j]
        # max_val[i][j] = maximum value in sizes[i..j]
        #
        # We fill these tables by fixing a start index i and extending j to the right.
        # This allows each next interval to be updated from the previous one:
        #   min_val[i][j] = min(min_val[i][j - 1], sizes[j])
        #   max_val[i][j] = max(max_val[i][j - 1], sizes[j])
        #
        # This preprocessing costs O(n^2), which is acceptable for n <= 300.
        # ---------------------------------------------------------------------
        min_val: List[List[int]] = [[0] * n for _ in range(n)]
        max_val: List[List[int]] = [[0] * n for _ in range(n)]

        for i in range(n):
            current_min: int = sizes[i]
            current_max: int = sizes[i]
            for j in range(i, n):
                if sizes[j] < current_min:
                    current_min = sizes[j]
                if sizes[j] > current_max:
                    current_max = sizes[j]
                min_val[i][j] = current_min
                max_val[i][j] = current_max

        # ---------------------------------------------------------------------
        # Step 3: Prepare the DP table.
        #
        # dp[i][j] means:
        #   minimum penalty to merge the contiguous interval sizes[i..j]
        #   into exactly one archive.
        #
        # Base case:
        #   dp[i][i] = 0
        # because a single snapshot is already one group and requires no merge.
        #
        # We will fill the table by increasing interval length.
        # ---------------------------------------------------------------------
        dp: List[List[int]] = [[0] * n for _ in range(n)]

        # ---------------------------------------------------------------------
        # Step 4: Fill DP for intervals of length 2 up to n.
        #
        # For each interval [i, j]:
        #   1. Compute the cost of the final merge of the whole interval.
        #   2. Try every possible split point k between i and j - 1.
        #      This means the last operation merges:
        #          [i..k] and [k+1..j]
        #      Both sides must already be fully merged optimally.
        #
        # So:
        #   dp[i][j] = min over k of (dp[i][k] + dp[k+1][j]) + interval_cost(i, j)
        #
        # The interval_cost(i, j) is paid exactly once for the final merge that
        # combines the two already-merged adjacent groups into one archive.
        # ---------------------------------------------------------------------
        for length in range(2, n + 1):
            for i in range(0, n - length + 1):
                j: int = i + length - 1

                # Compute the interval cost for [i, j].
                interval_sum: int = prefix[j + 1] - prefix[i]
                interval_range_penalty: int = max_val[i][j] - min_val[i][j]
                merge_cost: int = interval_sum + interval_range_penalty

                # Start with a very large number so any real candidate is smaller.
                best: int = 10**30

                # Try every possible final split point.
                for k in range(i, j):
                    candidate: int = dp[i][k] + dp[k + 1][j] + merge_cost
                    if candidate < best:
                        best = candidate

                dp[i][j] = best

        # The answer for the whole array is the minimum cost to merge [0..n-1].
        return dp[0][n - 1]

    def minimumPenalty(self, sizes: List[int]) -> int:
        """
        Wrapper method using a camelCase name for compatibility with common
        online judge naming styles.

        Args:
            sizes: List of positive integers representing snapshot sizes.

        Returns:
            The minimum total penalty.

        Time complexity:
            O(n^3)

        Space complexity:
            O(n^2)
        """
        return self.min_merge_penalty(sizes)


if __name__ == "__main__":
    solution = Solution()

    # Sample inputs from the prompt.
    samples: List[List[int]] = [
        [4, 2, 7],
        [5, 5, 5, 5],
    ]

    for arr in samples:
        result = solution.minimumPenalty(arr)
        print(f"sizes = {arr}")
        print(f"minimum penalty = {result}")
        print()

    # Additional small sanity checks.
    extra_samples: List[List[int]] = [
        [10],
        [1, 2],
        [3, 1, 2],
    ]

    for arr in extra_samples:
        result = solution.minimumPenalty(arr)
        print(f"sizes = {arr}")
        print(f"minimum penalty = {result}")
        print()