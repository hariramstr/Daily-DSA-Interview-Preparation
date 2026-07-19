"""
Title: Minimum Fatigue to Merge Spell Scrolls

Problem Description:
A wizard archive stores a row of spell scrolls, where the i-th scroll has an energy
value energy[i]. You must merge all scrolls into exactly one final scroll. In one
operation, you may choose any contiguous block of exactly k scrolls and merge them
into a single new scroll. The fatigue cost of that operation is the sum of the
energy values of those k scrolls. The new scroll's energy becomes that same sum,
and it remains in the row in place of the merged block.

Your task is to return the minimum total fatigue needed to end with one scroll.
If it is impossible to reduce the row to one scroll using only merges of exactly
k consecutive scrolls, return -1.

Because every merge changes future costs, a greedy strategy is not always optimal.
You need to determine the globally minimum fatigue over all valid merge orders.

Constraints:
- 1 <= energy.length <= 30
- 1 <= energy[i] <= 10^4
- 2 <= k <= 30

Notes:
- Only contiguous blocks may be merged.
- Every merge must combine exactly k current scrolls, not fewer and not more.
- The answer fits in a 32-bit signed integer.
"""

from typing import List


class Solution:
    def merge_scrolls(self, energy: List[int], k: int) -> int:
        """
        Compute the minimum total fatigue required to merge all scrolls into one.

        This uses interval dynamic programming. The key idea is:
        - We do not always merge a whole interval directly into one pile immediately.
        - Instead, for every subarray, we first compute the minimum cost to reduce it
          as much as possible.
        - If that subarray can finally become exactly one pile, we add the sum of that
          interval as one final merge cost.

        Args:
            energy: List of scroll energy values.
            k: Exact number of consecutive scrolls that must be merged in one operation.

        Returns:
            The minimum total fatigue to merge all scrolls into one, or -1 if impossible.

        Time complexity:
            O(n^3 / (k - 1)) in practice due to stepping by (k - 1), commonly described
            as O(n^3), where n is the number of scrolls.

        Space complexity:
            O(n^2) for the interval DP table.
        """
        n: int = len(energy)

        # Before doing any dynamic programming, we must check whether it is even
        # mathematically possible to end with exactly one scroll.
        #
        # Why this works:
        # - Each merge takes exactly k piles and turns them into 1 pile.
        # - So each merge reduces the total number of piles by (k - 1).
        # - Starting from n piles, to end at 1 pile, we must be able to subtract
        #   (k - 1) repeatedly until we reach 1.
        # - Therefore, (n - 1) must be divisible by (k - 1).
        #
        # If this condition fails, no sequence of valid merges can ever produce
        # exactly one final scroll.
        if (n - 1) % (k - 1) != 0:
            return -1

        # Prefix sums allow us to compute the total energy of any interval [i..j]
        # in O(1) time after O(n) preprocessing.
        #
        # prefix[x] = sum of energy[0:x]
        # Then sum of energy[i:j+1] = prefix[j+1] - prefix[i]
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + energy[i]

        # dp[i][j] will store the minimum cost to merge the subarray energy[i..j]
        # into the minimum number of piles achievable under the rules.
        #
        # Important subtle point:
        # We do NOT explicitly store the number of piles in this 2D DP.
        # Instead, we rely on a known property of this problem:
        # - For an interval length L, after valid merges, the number of remaining
        #   piles is determined modulo (k - 1).
        # - Specifically, an interval can be fully merged into 1 pile only when
        #   (L - 1) % (k - 1) == 0.
        #
        # So dp[i][j] means:
        # "minimum cost to reduce interval [i..j] as much as possible"
        # and if [i..j] can become 1 pile, we later add the interval sum.
        dp: List[List[int]] = [[0] * n for _ in range(n)]

        # We process intervals by increasing length so that when computing dp[i][j],
        # all smaller subproblems are already solved.
        #
        # length is the size of the current interval.
        for length in range(2, n + 1):
            for i in range(n - length + 1):
                j: int = i + length - 1

                # Start with an effectively infinite value so we can minimize over
                # all possible valid split points.
                best: int = float("inf")

                # Why do we split by step (k - 1) instead of every possible midpoint?
                #
                # This is a very important optimization and correctness detail.
                # When merging piles under this rule, the number of piles changes in
                # increments of (k - 1). Therefore, only certain partition points
                # produce meaningful states that can eventually combine correctly.
                #
                # Splitting at every position is unnecessary; stepping by (k - 1)
                # preserves valid pile-count transitions and is the standard approach
                # for this problem.
                for mid in range(i, j, k - 1):
                    # Cost to optimally reduce left part [i..mid]
                    left_cost: int = dp[i][mid]

                    # Cost to optimally reduce right part [mid+1..j]
                    right_cost: int = dp[mid + 1][j]

                    # Combine the two independently optimized parts.
                    candidate: int = left_cost + right_cost

                    if candidate < best:
                        best = candidate

                dp[i][j] = best

                # After reducing [i..j] as much as possible, we may be able to do
                # one final merge to turn its remaining k piles into 1 pile.
                #
                # This is possible exactly when:
                # (length - 1) % (k - 1) == 0
                #
                # If true, then the final merge cost is the total sum of the interval,
                # because merging k piles costs the sum of their energies.
                if (length - 1) % (k - 1) == 0:
                    interval_sum: int = prefix[j + 1] - prefix[i]
                    dp[i][j] += interval_sum

        # The answer for the whole array is the minimum cost to merge [0..n-1]
        # into one pile. Because we already checked feasibility at the start,
        # dp[0][n-1] is guaranteed to represent that final answer.
        return dp[0][n - 1]

    def min_fatigue(self, energy: List[int], k: int) -> int:
        """
        Wrapper method matching the problem statement terminology.

        Args:
            energy: List of scroll energy values.
            k: Exact number of consecutive scrolls to merge in one operation.

        Returns:
            Minimum total fatigue, or -1 if impossible.

        Time complexity:
            O(n^3) in the standard interval-DP sense.

        Space complexity:
            O(n^2).
        """
        return self.merge_scrolls(energy, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    energy1: List[int] = [3, 2, 4, 1]
    k1: int = 2
    result1: int = solution.min_fatigue(energy1, k1)
    print(f"energy = {energy1}, k = {k1} -> {result1}")  # Expected: 20

    # Example 2
    energy2: List[int] = [3, 2, 4, 1]
    k2: int = 3
    result2: int = solution.min_fatigue(energy2, k2)
    print(f"energy = {energy2}, k = {k2} -> {result2}")  # Expected: -1

    # Example 3
    energy3: List[int] = [3, 5, 1, 2, 6]
    k3: int = 3
    result3: int = solution.min_fatigue(energy3, k3)
    print(f"energy = {energy3}, k = {k3} -> {result3}")  # Expected: 25)