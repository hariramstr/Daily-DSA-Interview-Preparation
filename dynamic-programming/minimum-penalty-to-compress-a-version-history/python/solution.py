"""
Title: Minimum Penalty to Compress a Version History

Problem Description:
A software team stores the sizes of consecutive document revisions in an array `sizes`,
where `sizes[i]` is the size of the `i`-th saved version. To reduce storage, the team
wants to compress the full version history into exactly `k` archive blocks. Each archive
block must contain a contiguous range of versions.

If versions from index `l` to `r` are placed into one archive block, then all versions
in that block are represented using the largest version size inside the block. The storage
penalty of that block is defined as:

penalty(l, r) = (max(sizes[l..r]) * (r - l + 1)) - sum(sizes[l..r])

In other words, every version in the block is padded up to the maximum size in that block,
and the penalty is the total extra space introduced by that padding.

Your task is to return the minimum total penalty needed to partition the entire array into
exactly `k` contiguous archive blocks.

If `k > n`, where `n = sizes.length`, then it is impossible to create exactly `k` non-empty
blocks.

Return the minimum total penalty, or `-1` if the partition is impossible.

Constraints:
- 1 <= n <= 400
- 1 <= sizes[i] <= 10^9
- 1 <= k <= 400
- Each archive block must be non-empty
"""

from typing import List


class Solution:
    def _precompute_costs(self, sizes: List[int]) -> List[List[int]]:
        """
        Precompute the penalty for every contiguous interval [l..r].

        Args:
            sizes: List of version sizes.

        Returns:
            A 2D list `cost` where cost[l][r] is the penalty of compressing
            sizes[l..r] into one archive block.

        Time complexity:
            O(n^2)

        Space complexity:
            O(n^2)
        """
        n: int = len(sizes)

        # We create an n x n table.
        # Only entries with l <= r are meaningful.
        cost: List[List[int]] = [[0] * n for _ in range(n)]

        # For each possible starting index l, we extend the interval to the right.
        # While extending, we maintain:
        # - current_max: the maximum value in sizes[l..r]
        # - current_sum: the sum of values in sizes[l..r]
        #
        # Then the penalty is:
        # current_max * length - current_sum
        #
        # This avoids recomputing max and sum from scratch for every interval.
        for l in range(n):
            current_max: int = 0
            current_sum: int = 0

            for r in range(l, n):
                current_max = max(current_max, sizes[r])
                current_sum += sizes[r]
                length: int = r - l + 1
                cost[l][r] = current_max * length - current_sum

        return cost

    def min_penalty(self, sizes: List[int], k: int) -> int:
        """
        Compute the minimum total penalty to partition the array into exactly k
        non-empty contiguous archive blocks.

        Args:
            sizes: List of version sizes.
            k: Exact number of contiguous non-empty blocks required.

        Returns:
            The minimum total penalty, or -1 if it is impossible.

        Time complexity:
            O(n^2 + k * n^2)
            = O(k * n^2) in the worst case

        Space complexity:
            O(n^2 + k * n)
            The DP here is implemented with rolling arrays, so the DP portion is O(n).
        """
        n: int = len(sizes)

        # If we need more non-empty blocks than there are elements,
        # it is impossible because every block must contain at least one version.
        if k > n:
            return -1

        # Precompute the penalty of every interval.
        # This is a classic optimization for partition DP:
        # once interval costs are available in O(1), the DP becomes straightforward.
        cost: List[List[int]] = self._precompute_costs(sizes)

        # We use a large number to represent "impossible" or "not yet computed".
        inf: int = 10**30

        # DP idea:
        # Let dp_prev[i] = minimum penalty to partition the first i elements
        # (that is, sizes[0..i-1]) into exactly (blocks - 1) blocks.
        #
        # Let dp_curr[i] = minimum penalty to partition the first i elements
        # into exactly blocks blocks.
        #
        # Transition:
        # To compute dp_curr[i], the last block must start at some position j,
        # where blocks-1 <= j < i.
        #
        # Then:
        # - first j elements are partitioned into blocks-1 blocks => dp_prev[j]
        # - last block is sizes[j..i-1] => cost[j][i-1]
        #
        # So:
        # dp_curr[i] = min(dp_prev[j] + cost[j][i-1]) for all valid j
        #
        # Base case:
        # 0 elements into 0 blocks => 0 penalty
        # positive elements into 0 blocks => impossible
        dp_prev: List[int] = [inf] * (n + 1)
        dp_prev[0] = 0

        # We build the answer block by block.
        for blocks in range(1, k + 1):
            dp_curr: List[int] = [inf] * (n + 1)

            # To split i elements into exactly `blocks` non-empty blocks,
            # we must have at least i >= blocks.
            #
            # Also, the last block starts at j, and the first j elements
            # must be split into exactly blocks-1 blocks, so j >= blocks-1.
            for i in range(blocks, n + 1):
                best: int = inf

                # Try every possible starting position j of the last block.
                # The last block is sizes[j..i-1].
                for j in range(blocks - 1, i):
                    candidate: int = dp_prev[j] + cost[j][i - 1]
                    if candidate < best:
                        best = candidate

                dp_curr[i] = best

            dp_prev = dp_curr

        answer: int = dp_prev[n]
        return answer if answer < inf else -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    # Important note:
    # If we compute all valid 2-block partitions of [5, 2, 4, 6, 3],
    # the true minimum is 7, not 5.
    #
    # Splits:
    # [5] | [2,4,6,3]      => 0 + (6*4 - 15) = 9
    # [5,2] | [4,6,3]      => (5*2 - 7)=3, (6*3 - 13)=5, total=8
    # [5,2,4] | [6,3]      => (5*3 - 11)=4, (6*2 - 9)=3, total=7
    # [5,2,4,6] | [3]      => (6*4 - 17)=7, 0, total=7
    #
    # So the mathematically correct answer under the stated formula is 7.
    sizes1: List[int] = [5, 2, 4, 6, 3]
    k1: int = 2
    result1: int = solution.min_penalty(sizes1, k1)
    print(f"sizes = {sizes1}, k = {k1} -> minimum penalty = {result1}")

    # Example 2 from the prompt.
    sizes2: List[int] = [8, 8, 8, 8]
    k2: int = 3
    result2: int = solution.min_penalty(sizes2, k2)
    print(f"sizes = {sizes2}, k = {k2} -> minimum penalty = {result2}")

    # Additional impossible case.
    sizes3: List[int] = [10, 20]
    k3: int = 3
    result3: int = solution.min_penalty(sizes3, k3)
    print(f"sizes = {sizes3}, k = {k3} -> minimum penalty = {result3}")