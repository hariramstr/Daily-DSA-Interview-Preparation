"""
Title: Minimum Risk to Merge Security Zones

Problem Description:
A company is consolidating a row of security zones in a data center. The zones are
numbered from left to right, and zone i has a risk value risk[i]. To simplify
monitoring, the company wants to repeatedly merge adjacent groups of zones until
only one group remains.

If you merge two already-formed adjacent groups, the cost of that merge is equal
to the sum of all risk values in the final combined group. However, not every
merge order is allowed: a merge is valid only if at least one of the two groups
being merged has total risk less than or equal to T. This rule models the
requirement that at least one side of a merge must still be small enough to audit
safely.

Return the minimum total cost to merge all zones into one group. If it is
impossible to merge all zones while respecting the rule, return -1.

You may assume every zone starts as its own group, and each merge combines exactly
two adjacent groups. The total cost is the sum of the costs of all performed merges.

Constraints:
- 1 <= n == risk.length <= 300
- 1 <= risk[i] <= 10^6
- 1 <= T <= 10^12

Examples:
1) risk = [4, 2, 7, 3], T = 6
   Output: 32

2) risk = [8, 9, 5], T = 6
   Output: -1
"""

from typing import List


class Solution:
    def min_merge_risk(self, risk: List[int], t: int) -> int:
        """
        Compute the minimum total cost to merge all adjacent security zones into one group
        while respecting the threshold rule.

        The key dynamic programming idea:
        - Let dp[i][j] be the minimum cost to fully merge the subarray risk[i..j] into
          exactly one group.
        - To compute dp[i][j], try every possible final split point k where:
              risk[i..k]   and   risk[k+1..j]
          are the two groups merged in the last step.
        - That final merge is allowed only if at least one of the two group sums is <= t.
        - If both left and right intervals can themselves be fully merged, then:
              candidate_cost = dp[i][k] + dp[k+1][j] + sum(i..j)
          and we take the minimum valid candidate.

        Args:
            risk: List of zone risk values.
            t: Threshold that allows a merge if at least one side's total sum is <= t.

        Returns:
            The minimum total merge cost, or -1 if it is impossible.

        Time complexity:
            O(n^3), because for every interval (i, j) we try every split k.

        Space complexity:
            O(n^2) for the DP table, plus O(n) for prefix sums.
        """
        n: int = len(risk)

        # If there is only one zone, it is already a single group.
        # No merge operations are needed, so the total cost is 0.
        if n == 1:
            return 0

        # Build prefix sums so we can query any subarray sum in O(1) time.
        #
        # prefix[x] stores the sum of risk[0:x], meaning:
        # - prefix[0] = 0
        # - prefix[1] = risk[0]
        # - prefix[2] = risk[0] + risk[1]
        # and so on.
        #
        # Then sum of risk[i..j] is:
        # prefix[j + 1] - prefix[i]
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + risk[i]

        def range_sum(left: int, right: int) -> int:
            """
            Return the sum of risk[left..right], inclusive.

            Args:
                left: Left index of the interval.
                right: Right index of the interval.

            Returns:
                Sum of the interval.

            Time complexity:
                O(1)

            Space complexity:
                O(1)
            """
            return prefix[right + 1] - prefix[left]

        # Use a very large number to represent "impossible" states.
        inf: int = 10**30

        # dp[i][j] = minimum cost to merge risk[i..j] into one group.
        #
        # Base case:
        # - dp[i][i] = 0 because a single zone is already one group.
        #
        # Transition:
        # - For each interval [i..j], try every split k between i and j-1.
        # - Left interval is [i..k], right interval is [k+1..j].
        # - Both sides must be individually mergeable.
        # - The final merge between those two groups is allowed only if:
        #       sum(i..k) <= t  OR  sum(k+1..j) <= t
        # - If allowed, total cost is:
        #       dp[i][k] + dp[k+1][j] + sum(i..j)
        dp: List[List[int]] = [[inf] * n for _ in range(n)]

        # Initialize base cases for intervals of length 1.
        for i in range(n):
            dp[i][i] = 0

        # Process intervals by increasing length.
        #
        # This is essential in range DP:
        # when computing dp[i][j], we need smaller intervals dp[i][k] and dp[k+1][j]
        # to already be known.
        for length in range(2, n + 1):
            # For a fixed length, slide the interval across the array.
            for left in range(0, n - length + 1):
                right: int = left + length - 1

                # Precompute the total sum of the current interval because every valid
                # final merge on this interval adds exactly this amount.
                total_sum: int = range_sum(left, right)

                # Try every possible final split point.
                for mid in range(left, right):
                    # If either side cannot be fully merged into one group,
                    # this split cannot be used.
                    if dp[left][mid] == inf or dp[mid + 1][right] == inf:
                        continue

                    # Compute the sums of the two groups that would be merged last.
                    left_sum: int = range_sum(left, mid)
                    right_sum: int = range_sum(mid + 1, right)

                    # Check the problem's validity rule for the final merge:
                    # at least one side must have total risk <= t.
                    if left_sum <= t or right_sum <= t:
                        candidate: int = dp[left][mid] + dp[mid + 1][right] + total_sum
                        if candidate < dp[left][right]:
                            dp[left][right] = candidate

        # If the whole array cannot be merged, return -1.
        return -1 if dp[0][n - 1] == inf else dp[0][n - 1]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    risk1: List[int] = [4, 2, 7, 3]
    t1: int = 6
    result1: int = solution.min_merge_risk(risk1, t1)
    print(f"risk = {risk1}, T = {t1} -> {result1}")  # Expected: 32

    # Example 2
    risk2: List[int] = [8, 9, 5]
    t2: int = 6
    result2: int = solution.min_merge_risk(risk2, t2)
    print(f"risk = {risk2}, T = {t2} -> {result2}")  # Expected: -1

    # Additional simple check
    risk3: List[int] = [5]
    t3: int = 10
    result3: int = solution.min_merge_risk(risk3, t3)
    print(f"risk = {risk3}, T = {t3} -> {result3}")  # Expected: 0