from typing import List

"""
Title: Minimum Drift to Align Beacon Pulses

Problem Description:
A monitoring system receives pulse timestamps from two independent space beacons.
Due to clock drift and packet loss, the two timestamp sequences are not perfectly aligned.
You are given two integer arrays `a` and `b`, where `a[i]` and `b[j]` are pulse times
in milliseconds, sorted in non-decreasing order.

You want to align the sequences by partitioning each array into the same number of
non-empty contiguous groups. The k-th group from `a` must be matched with the k-th
group from `b`.

If a group from `a` spans indices l1..r1 and the matched group from `b` spans indices
l2..r2, the drift cost of that matched pair is:

    abs((sum of a[l1..r1]) - (sum of b[l2..r2])))

Return the minimum possible total drift cost over all valid ways to partition both arrays
into the same number of contiguous non-empty groups.

Constraints:
- 1 <= a.length, b.length <= 200
- 1 <= a[i], b[j] <= 10^6
- Both arrays are sorted in non-decreasing order
- The answer fits in a 64-bit signed integer
"""


class Solution:
    def minimum_drift(self, a: List[int], b: List[int]) -> int:
        """
        Compute the minimum total drift cost by partitioning both arrays into the same
        number of non-empty contiguous groups and matching groups in order.

        The key dynamic programming idea:
        Let dp[i][j] be the minimum cost to align the first i elements of `a`
        with the first j elements of `b`.

        The last matched group must end at a[i-1] and b[j-1]. If that last group starts
        right after positions p in `a` and q in `b`, then:
            previous cost = dp[p][q]
            last group cost = abs(sum(a[p:i]) - sum(b[q:j]))

        So:
            dp[i][j] = min over 0 <= p < i, 0 <= q < j of
                       dp[p][q] + abs((prefix_a[i] - prefix_a[p]) - (prefix_b[j] - prefix_b[q]))

        Args:
            a: Sorted list of pulse timestamps from beacon A.
            b: Sorted list of pulse timestamps from beacon B.

        Returns:
            Minimum possible total drift cost.

        Time complexity:
            O(len(a)^2 * len(b)^2)

        Space complexity:
            O(len(a) * len(b))
        """
        n: int = len(a)
        m: int = len(b)

        # Build prefix sums so that any contiguous subarray sum can be computed in O(1).
        #
        # prefix_a[i] = sum of a[0:i]
        # prefix_b[j] = sum of b[0:j]
        #
        # Then:
        # sum of a[l:r] inclusive in 0-based indexing is prefix_a[r+1] - prefix_a[l]
        #
        # In our DP we use half-open ranges:
        # group a[p:i] means elements p, p+1, ..., i-1
        # group b[q:j] means elements q, q+1, ..., j-1
        prefix_a: List[int] = [0] * (n + 1)
        prefix_b: List[int] = [0] * (m + 1)

        for i in range(n):
            prefix_a[i + 1] = prefix_a[i] + a[i]

        for j in range(m):
            prefix_b[j + 1] = prefix_b[j] + b[j]

        # Use a very large number as "infinity" for initialization.
        inf: int = 10**30

        # dp[i][j] = minimum cost to align first i elements of a with first j elements of b.
        #
        # Dimensions are (n+1) x (m+1) because we include empty prefixes.
        # dp[0][0] = 0 because aligning two empty prefixes costs nothing.
        #
        # Important:
        # dp[i][0] and dp[0][j] for positive i or j are impossible, because every group
        # must be non-empty in both arrays. So they remain infinity.
        dp: List[List[int]] = [[inf] * (m + 1) for _ in range(n + 1)]
        dp[0][0] = 0

        # Fill the DP table for all non-empty prefixes.
        #
        # We compute dp[i][j] by trying every possible place where the final matched group
        # could begin:
        #
        #   previous aligned prefixes: a[0:p], b[0:q]
        #   final matched group:       a[p:i], b[q:j]
        #
        # Since groups must be non-empty:
        #   p must be in [0, i-1]
        #   q must be in [0, j-1]
        #
        # This is a complete search over all valid last-group choices, which guarantees
        # correctness because every valid partitioning has some final group, and that final
        # group corresponds to exactly one pair (p, q).
        for i in range(1, n + 1):
            for j in range(1, m + 1):
                best: int = inf

                # Try every possible starting boundary p for the last group in array a.
                for p in range(i):
                    # Sum of the last group chosen from a: a[p:i]
                    sum_a_group: int = prefix_a[i] - prefix_a[p]

                    # Try every possible starting boundary q for the last group in array b.
                    for q in range(j):
                        # If the previous state is impossible, skip it.
                        if dp[p][q] == inf:
                            continue

                        # Sum of the last group chosen from b: b[q:j]
                        sum_b_group: int = prefix_b[j] - prefix_b[q]

                        # Cost of matching these two final groups.
                        group_cost: int = abs(sum_a_group - sum_b_group)

                        # Total cost = best way to align previous prefixes
                        #            + cost of the final matched group
                        candidate: int = dp[p][q] + group_cost

                        # Keep the minimum over all valid choices.
                        if candidate < best:
                            best = candidate

                dp[i][j] = best

        return dp[n][m]

    def minDrift(self, a: List[int], b: List[int]) -> int:
        """
        Convenience wrapper using an alternative method name.

        Args:
            a: Sorted list of pulse timestamps from beacon A.
            b: Sorted list of pulse timestamps from beacon B.

        Returns:
            Minimum possible total drift cost.

        Time complexity:
            O(len(a)^2 * len(b)^2)

        Space complexity:
            O(len(a) * len(b))
        """
        return self.minimum_drift(a, b)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the statement:
    # Best is one single group on each side:
    # [2,5,9] vs [4,6,7] => |16 - 17| = 1
    a1: List[int] = [2, 5, 9]
    b1: List[int] = [4, 6, 7]
    result1: int = solution.minimum_drift(a1, b1)
    print(result1)  # Expected: 1

    # Example 2 from the statement:
    # One optimal partition:
    # [1,3,8] | [10]
    # [2,4,6] | [12]
    # Costs: |12-12| + |10-12| = 2
    a2: List[int] = [1, 3, 8, 10]
    b2: List[int] = [2, 4, 6, 12]
    result2: int = solution.minimum_drift(a2, b2)
    print(result2)  # Expected: 2