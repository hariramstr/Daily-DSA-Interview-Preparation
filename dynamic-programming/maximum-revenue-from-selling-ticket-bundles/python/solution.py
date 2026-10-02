"""
Title: Maximum Revenue from Selling Ticket Bundles

Problem Description:
A concert venue is selling tickets for N consecutive seat sections, numbered from 0 to N - 1.
The venue may create promotional bundles, where each bundle must cover a contiguous range
of sections. If a bundle covers sections i through j, inclusive, its revenue is
bundleRevenue[i][j].

You are allowed to choose any number of bundles, but no two chosen bundles may overlap
in covered sections. It is also allowed to leave some sections unbundled.

Your task is to compute the maximum total revenue that can be earned.

This is not a scheduling problem with explicit times; instead, each possible contiguous
section range has a precomputed revenue value. Some bundles may be unattractive, and
skipping them can lead to a better overall answer. You need to decide which non-overlapping
ranges to select so that the sum of their revenues is maximized.

Return the maximum total revenue.

Constraints:
- 1 <= N <= 300
- bundleRevenue is an N x N matrix
- 0 <= bundleRevenue[i][j] <= 10^6 for all 0 <= i <= j < N
- bundleRevenue[i][j] = 0 for i > j, or such entries may be ignored
- Required time complexity: O(N^2) or O(N^2 log N)
"""

from typing import List


class Solution:
    def max_revenue(self, bundle_revenue: List[List[int]]) -> int:
        """
        Compute the maximum total revenue from selecting non-overlapping contiguous bundles.

        The key idea is dynamic programming over prefixes of sections.
        Let dp[i] represent the maximum revenue obtainable using only sections in the
        prefix [0, i - 1]. In other words:
            - dp[0] means no sections are available, so revenue is 0
            - dp[1] means we may use section 0
            - dp[n] means we may use all sections 0 through n - 1

        For each ending position i - 1, we have two categories of choices:
        1. Skip section i - 1 entirely:
           Then the answer remains dp[i - 1].
        2. End a chosen bundle at section i - 1:
           If that bundle starts at section s, then its revenue is bundle_revenue[s][i - 1],
           and everything before s contributes dp[s].
           So this candidate value is:
               dp[s] + bundle_revenue[s][i - 1]

        We take the maximum over all such possibilities.

        Args:
            bundle_revenue: An N x N matrix where bundle_revenue[i][j] is the revenue
                for choosing the contiguous bundle covering sections i through j, inclusive.

        Returns:
            The maximum total revenue obtainable with non-overlapping chosen bundles.

        Time complexity:
            O(N^2), because for each prefix end i we try all possible start positions s.

        Space complexity:
            O(N), for the dynamic programming array.
        """
        # Determine how many seat sections exist.
        # The matrix is N x N, so N is simply the number of rows.
        n: int = len(bundle_revenue)

        # dp[i] will store the best possible revenue using only sections [0, i - 1].
        #
        # Why size n + 1?
        # Because dp[0] represents the empty prefix (no sections at all),
        # and dp[n] will represent the full answer for sections [0, n - 1].
        dp: List[int] = [0] * (n + 1)

        # We build the answer from left to right, one prefix at a time.
        #
        # For each i from 1 to n:
        # - We are computing the best answer for sections [0, i - 1].
        for i in range(1, n + 1):
            # First, consider the option of not using section i - 1 in any bundle.
            #
            # If we skip this section, then the best answer for prefix [0, i - 1]
            # is simply the same as the best answer for prefix [0, i - 2],
            # which is dp[i - 1].
            best_for_prefix: int = dp[i - 1]

            # Next, consider every possible bundle that ends exactly at section i - 1.
            #
            # Let the bundle start at section s, where 0 <= s <= i - 1.
            # Then:
            # - The chosen bundle is [s, i - 1]
            # - Its revenue is bundle_revenue[s][i - 1]
            # - Since bundles cannot overlap, anything chosen before this bundle
            #   must lie entirely in sections [0, s - 1]
            # - The best revenue from those earlier sections is dp[s]
            #
            # Therefore, the total revenue for choosing this ending bundle is:
            #     dp[s] + bundle_revenue[s][i - 1]
            for s in range(i):
                candidate: int = dp[s] + bundle_revenue[s][i - 1]

                # If this candidate is better than our current best, update it.
                if candidate > best_for_prefix:
                    best_for_prefix = candidate

            # Store the best answer for prefix [0, i - 1].
            dp[i] = best_for_prefix

        # The final answer for all sections [0, n - 1] is stored in dp[n].
        return dp[n]


if __name__ == "__main__":
    """
    Run sample test cases from the problem statement and print the results.
    """

    solution = Solution()

    # Example 1
    #
    # Matrix meaning:
    # - bundleRevenue[0][1] = 9 means bundle [0, 1] earns 9
    # - bundleRevenue[2][2] = 6 means bundle [2, 2] earns 6
    #
    # One optimal solution:
    # - choose [0, 1] => 9
    # - choose [2, 2] => 6
    # Total = 15
    #
    # This is better than choosing [0, 3] => 10
    bundle_revenue_1: List[List[int]] = [
        [5, 9, 10, 10],
        [0, 4, 7, 8],
        [0, 0, 6, 9],
        [0, 0, 0, 3],
    ]
    result_1: int = solution.max_revenue(bundle_revenue_1)
    print("Example 1 Output:", result_1)  # Expected: 15

    # Example 2
    #
    # The problem statement's explanation contains a typo:
    # it says output 14, but then correctly identifies a non-overlapping choice:
    # - [0, 1] => 8
    # - [2, 4] => 12
    # These do NOT overlap, and total = 20.
    #
    # Therefore the correct maximum revenue is 20.
    bundle_revenue_2: List[List[int]] = [
        [2, 8, 8, 9, 9],
        [0, 1, 5, 7, 7],
        [0, 0, 4, 6, 12],
        [0, 0, 0, 3, 5],
        [0, 0, 0, 0, 4],
    ]
    result_2: int = solution.max_revenue(bundle_revenue_2)
    print("Example 2 Output:", result_2)  # Expected: 20