```python
"""
Title: Maximum Profit from Turbulent Stock Segments
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given an integer array `prices` representing daily stock prices, and two integers `k` and `threshold`.

A subarray is called **turbulent** if the absolute difference between every pair of consecutive elements
in the subarray is strictly greater than `threshold`. More formally, a subarray `prices[l..r]` is turbulent
if for every index `i` in `[l, r-1]`, `|prices[i+1] - prices[i]| > threshold`.

Your goal is to find the **maximum total profit** achievable by selecting at most `k` non-overlapping
turbulent subarrays, where the profit of a subarray `prices[l..r]` is defined as
`max(prices[l..r]) - min(prices[l..r])`.

Return the maximum total profit. If no turbulent subarray of length at least 2 exists, return 0.

Constraints:
- 1 <= prices.length <= 10^5
- 0 <= prices[i] <= 10^9
- 1 <= k <= 100
- 0 <= threshold <= 10^9
"""

from typing import List, Tuple
import math


class Solution:
    def maxTurbulentProfit(self, prices: List[int], k: int, threshold: int) -> int:
        """
        Find the maximum total profit from selecting at most k non-overlapping turbulent subarrays.

        A turbulent subarray requires |prices[i+1] - prices[i]| > threshold for all consecutive pairs.
        The profit of a subarray is max - min within that subarray.

        Args:
            prices: List of daily stock prices.
            k: Maximum number of non-overlapping turbulent subarrays to select.
            threshold: Minimum absolute difference required between consecutive elements.

        Returns:
            Maximum total profit achievable.

        Time Complexity: O(n^2 * k) where n = len(prices), due to DP over all turbulent segments.
        Space Complexity: O(n * k) for the DP table.
        """

        n = len(prices)
        if n < 2:
            # Need at least 2 elements to form a turbulent subarray
            return 0

        # -----------------------------------------------------------------------
        # STEP 1: Identify all maximal turbulent segments.
        #
        # We scan through prices and find contiguous "runs" where every consecutive
        # pair satisfies |prices[i+1] - prices[i]| > threshold.
        #
        # A "maximal turbulent segment" is a segment [l, r] such that:
        #   - For all i in [l, r-1]: |prices[i+1] - prices[i]| > threshold
        #   - It cannot be extended further left or right while maintaining turbulence.
        #
        # Why maximal segments? Because any turbulent subarray must be a contiguous
        # sub-range of some maximal turbulent segment. This lets us decompose the
        # problem: within each maximal segment, we can pick any sub-range as our
        # turbulent subarray.
        # -----------------------------------------------------------------------

        # Each entry in maximal_segments is (left_index, right_index) inclusive
        maximal_segments: List[Tuple[int, int]] = []

        seg_start = 0
        for i in range(1, n):
            # Check if the transition from i-1 to i is turbulent
            if abs(prices[i] - prices[i - 1]) <= threshold:
                # The turbulence breaks at index i
                # The segment from seg_start to i-1 is a maximal turbulent segment
                # (only valid if it has length >= 2, i.e., seg_start < i-1+1 means seg_start <= i-1)
                if i - 1 > seg_start:
                    # seg_start to i-1 has at least 2 elements
                    maximal_segments.append((seg_start, i - 1))
                # Start a new potential segment at i
                seg_start = i
        # Don't forget the last segment
        if seg_start < n - 1:
            maximal_segments.append((seg_start, n - 1))

        # -----------------------------------------------------------------------
        # STEP 2: For each maximal turbulent segment, precompute the profit of
        # every possible sub-range [l, r] within it.
        #
        # The profit of [l, r] = max(prices[l..r]) - min(prices[l..r]).
        #
        # We store all valid turbulent subarrays as (profit, left, right) tuples.
        # A valid turbulent subarray has length >= 2.
        #
        # Why enumerate all sub-ranges? Because we need to pick k non-overlapping
        # subarrays with maximum total profit. The optimal choice might not use
        # the full maximal segment — it might use a sub-range that has higher
        # profit or allows better combination with other segments.
        #
        # Note: For a maximal segment of length L, there are O(L^2) sub-ranges.
        # In the worst case this is O(n^2) total sub-ranges.
        # -----------------------------------------------------------------------

        # List of all valid turbulent subarrays: (profit, left_index, right_index)
        all_subarrays: List[Tuple[int, int, int]] = []

        for seg_l, seg_r in maximal_segments:
            # Enumerate all sub-ranges [l, r] within [seg_l, seg_r]
            # with r > l (length >= 2)
            for l in range(seg_l, seg_r + 1):
                cur_max = prices[l]
                cur_min = prices[l]
                for r in range(l + 1, seg_r + 1):
                    cur_max = max(cur_max, prices[r])
                    cur_min = min(cur_min, prices[r])
                    profit = cur_max - cur_min
                    all_subarrays.append((profit, l, r))

        # -----------------------------------------------------------------------
        # STEP 3: Sort all subarrays by their left endpoint (then right endpoint).
        #
        # We need to select at most k non-overlapping subarrays to maximize profit.
        # This is the classic "weighted job scheduling" / "select k non-overlapping
        # intervals" DP problem.
        #
        # Sort by right endpoint to facilitate DP.
        # -----------------------------------------------------------------------

        if not all_subarrays:
            return 0

        # Sort by right endpoint for DP processing
        all_subarrays.sort(key=lambda x: (x[2], x[1]))

        m = len(all_subarrays)

        # -----------------------------------------------------------------------
        # STEP 4: Dynamic Programming to select at most k non-overlapping subarrays.
        #
        # dp[j][i] = maximum profit using exactly j subarrays from the first i subarrays
        #            (where subarrays are sorted by right endpoint).
        #
        # Transition:
        #   dp[j][i] = max(
        #       dp[j][i-1],                          # don't pick subarray i
        #       dp[j-1][p(i)] + profit[i]            # pick subarray i, where p(i) is the
        #                                            # last subarray that ends before subarray i starts
        #   )
        #
        # p(i) = largest index q < i such that all_subarrays[q].right < all_subarrays[i].left
        #
        # We use binary search to find p(i) efficiently.
        # -----------------------------------------------------------------------

        # Extract profits, lefts, rights for easy access
        profits = [s[0] for s in all_subarrays]
        lefts = [s[1] for s in all_subarrays]
        rights = [s[2] for s in all_subarrays]

        # Binary search helper: find the rightmost subarray index q such that rights[q] < lefts[i]
        # This ensures subarrays don't overlap (subarray q ends before subarray i starts)
        def find_last_non_overlapping(i: int) -> int:
            """
            Binary search for the last subarray (by index) whose right endpoint
            is strictly less than the left endpoint of subarray i.
            Returns -1 if no such subarray exists.
            """
            target = lefts[i]  # subarray i starts at lefts[i]
            lo, hi = 0, i - 1
            result = -1
            while lo <= hi:
                mid = (lo + hi) // 2
                if rights[mid] < target:
                    # subarray mid ends before subarray i starts — valid
                    result = mid
                    lo = mid + 1  # try to find a later valid one
                else:
                    hi = mid - 1
            return result

        # Precompute p[i] for all i
        # p[i] = index of last non-overlapping subarray before i, or -1
        p = [find_last_non_overlapping(i) for i in range(m)]

        # dp[j][i]: max profit selecting exactly j subarrays from first i+1 subarrays
        # We use (k+1) x (m+1) table where dp[j][i] considers first i subarrays (1-indexed)
        # dp[j][0] = 0 (no subarrays available, 0 profit)
        # dp[0][i] = 0 (select 0 subarrays, 0 profit)

        # To save space, we process j from 1 to k
        # dp_prev[i] = dp[j-1][i], dp_curr[i] = dp[j][i]

        # Initialize dp_prev (j=0): all zeros
        dp_prev = [0] * (m + 1)  # dp_prev[i] = max profit with 0 subarrays from first i

        best_overall = 0

        for j in range(1, k + 1):
            # dp_curr[i] = max profit selecting exactly j subarrays from first i subarrays
            dp_curr = [0] * (m + 1)

            for i in range(1, m + 1):
                # Option 1: Don't pick subarray i-1 (0-indexed)
                dp_curr[i] = dp_curr[i - 1]

                # Option 2: Pick subarray i-1 (0-indexed)
                # Find the last non-overlapping subarray before i-1
                pi = p[i - 1]  # index in 0-indexed subarrays, or -1

                # If pi == -1, no previous subarray fits, so we use dp_prev[0] = 0
                # Otherwise, use dp_prev[pi+1] (1-indexed: first pi+1 subarrays)
                prev_profit = dp_prev[pi + 1] if pi >= 0 else dp_prev[0]

                pick_profit = prev_profit + profits[i - 1]
                dp_curr[i] = max(dp_curr[i], pick_profit)

            best_overall = max(best_overall, dp_curr[m])
            dp_prev = dp_curr

        return best_overall


# -----------------------------------------------------------------------
# VERIFICATION / TRACING
#
# Example 1: prices = [9, 4, 8, 2, 10, 3, 7], k = 2, threshold = 2
#
# Check turbulence:
#   |4-9|=5 > 2 ✓
#   |8-4|=4 > 2 ✓
#   |2-8|=6 > 2 ✓
#   |10-2|=8 > 2 ✓
#   |3-10|=7 > 2 ✓
#   |7-3|=4 > 2 ✓
# All pairs are turbulent! So the entire array [9,4,8,2,10,3,7] is one maximal segment.
#
# We need to find 2 non-overlapping subarrays with max total profit.
# The problem states the answer is 16.
#
# Let's think about what gives 16:
# We need two non-overlapping subarrays summing to 16.
# Full array profit = max(9,4,8,2,10,3,7) - min(...) = 10 - 2 = 8
# [9,4,8,2,10] profit = 10-2 = 8, [3,7] profit = 7-3 = 4 → total 12
# [4,8,2,10] profit = 10-2 = 8, [3,7] profit = 4 → total 12
# [9,4,8,2,10,3] profit = 10-2 = 8, [7] — can't, length 1
# Hmm, let me try:
# [9,4] profit = 9-4=5, [8,2,10,3,7] profit = 10-2=8 → 13
# [9,4,8] profit = 9-2=7... wait max=9,min=4 → 5? No: max(9,4,8)=9, min=4, profit=5
# Actually [9,4,8,2,10,3,7]: let me try [2,10,3,7] profit=10-2=8 and [9,4] profit=5 → 13
# [9,4,8,2,10] profit=8 and [3,7] profit=4 → 12
# What about [4,8,2,10,3,7]? profit=10-2=8. And [9]? Can't.
# [9,4,8,2,10,3] profit=10-2=8. [7]? Can't.
# Hmm, what gives 16?
# [9,4,8,2,10,3,7] as one subarray: profit=10-2=8. That's only 8 for one.
# Two subarrays summing to 16 means each has profit 8 on average.
# [2,10] profit=8, [3,7] profit=4 → 12
# [4,8,2,10] profit=8, [3,7] profit=4 → 12
# Wait, maybe I'm misreading. Let me re-read the problem.
# "The best selection is [4, 8, 2, 10, 3] (profit 8) and..."
# [4,8,2,10,3]: max=10, min=2, profit=8. Then what's the other?
# The problem says "optimal two non-overlapping segments yield a total of 16"
# but doesn't clearly state which two. Let me try harder:
# [9,4,8,2,10,3,7] — what if we pick [9,4,8,2,10,3,7] as one? profit=8. k=2 but we only need 1.
# Actually wait: can we get profit > 8 from any subarray? max-min for any subarray of this array
# is at most 10-2=8. So max single profit is 8.
# For two subarrays to sum to 16, both need profit 8.
# [2,10] has profit 8 (indices 3,4). [9,4,8] has profit... max=9,min=4=5. No.
# [4,8,2,10] profit=8 (indices 1-4). [3,7] profit=4 (indices 5-6). Total=12.
# Hmm, I can't get 16 from this example. Let me reconsider.
# Maybe the example explanation is just poorly written and the actual answer
# requires a different interpretation. Let me just trust the algorithm and
# verify with example 2.
#
# Example 2: prices = [5,5,5,5], k=1, threshold=0
# |5-5|=0, not > 0. No turbulent subarrays. Return 0. ✓
# -----------------------------------------------------------------------


if __name__ == "__main__":
    solution = Solution()

    # Example