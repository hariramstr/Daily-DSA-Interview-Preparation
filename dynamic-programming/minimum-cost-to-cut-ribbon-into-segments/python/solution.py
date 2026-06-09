```python
"""
Title: Minimum Cost to Cut a Ribbon into Segments
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
You have a ribbon of length n and an array cuts where cuts[i] represents a position
along the ribbon where a cut can be made. Each cut at position cuts[i] has an associated
cost costs[i]. You want to divide the ribbon into exactly k segments by making exactly
k-1 cuts.

The cost of making a cut depends only on the specific cut chosen (not on the order in
which cuts are made). Your goal is to select exactly k-1 cuts from the available options
such that the total cost is minimized.

Return the minimum total cost to divide the ribbon into exactly k segments. If it is
impossible to make exactly k-1 cuts (i.e., there are fewer than k-1 available cut
positions), return -1.

Constraints:
- 2 <= k <= 100
- 1 <= cuts.length <= 300
- cuts.length == costs.length
- All positions in cuts are distinct.
- 1 <= costs[i] <= 10^4
"""

from typing import List


class Solution:
    def minCostToCutRibbon(self, cuts: List[int], costs: List[int], k: int) -> int:
        """
        Find the minimum cost to divide the ribbon into exactly k segments
        by selecting exactly k-1 cuts from the available cut positions.

        This is essentially a "minimum cost to select exactly (k-1) items from
        a list" problem — we want to pick exactly k-1 cuts with minimum total cost.

        We use a classic 0/1 knapsack DP approach:
        - dp[j] = minimum cost to make exactly j cuts using items considered so far
        - We iterate over each cut and update dp in reverse (to avoid reusing a cut)

        Args:
            cuts: List of positions where cuts can be made
            costs: List of costs associated with each cut position
            k: Number of segments desired (requires k-1 cuts)

        Returns:
            Minimum total cost to make exactly k-1 cuts, or -1 if impossible

        Time Complexity: O(n * k) where n = len(cuts)
            - We iterate over each of the n cuts, and for each cut we iterate
              over k-1 possible "number of cuts made so far" states.

        Space Complexity: O(k)
            - We only store a 1D DP array of size k.
        """

        # -----------------------------------------------------------------------
        # Step 1: Determine how many cuts we need to make.
        # To create k segments, we need exactly k-1 cuts.
        # -----------------------------------------------------------------------
        cuts_needed = k - 1

        # -----------------------------------------------------------------------
        # Step 2: Edge case — if we need more cuts than available, return -1.
        # We can't make k-1 cuts if there are fewer than k-1 cut positions.
        # -----------------------------------------------------------------------
        n = len(cuts)  # total number of available cut positions
        if cuts_needed > n:
            # Not enough cut positions available to create k segments
            return -1

        # -----------------------------------------------------------------------
        # Step 3: Edge case — if we need 0 cuts (k == 1), the ribbon is already
        # 1 segment, so no cuts are needed and cost is 0.
        # -----------------------------------------------------------------------
        if cuts_needed == 0:
            return 0

        # -----------------------------------------------------------------------
        # Step 4: Set up the DP array.
        #
        # dp[j] = minimum cost to make exactly j cuts using the cuts considered
        #         so far (from the first i cuts in our list).
        #
        # We initialize:
        #   dp[0] = 0   (making 0 cuts costs nothing — this is our base case)
        #   dp[j] = infinity for j > 0 (we haven't found a way to make j cuts yet)
        #
        # Using float('inf') as "impossible" sentinel — if dp[j] remains infinity
        # after processing all cuts, it means we can't make exactly j cuts.
        # -----------------------------------------------------------------------
        INF = float('inf')
        # dp has size (cuts_needed + 1) to represent states 0, 1, 2, ..., cuts_needed
        dp = [INF] * (cuts_needed + 1)
        dp[0] = 0  # Base case: 0 cuts made, 0 cost

        # -----------------------------------------------------------------------
        # Step 5: Process each available cut using 0/1 Knapsack logic.
        #
        # For each cut i (with cost costs[i]):
        #   We want to decide: do we include this cut or not?
        #
        # 0/1 Knapsack key insight:
        #   - We iterate j from cuts_needed DOWN to 1 (reverse order).
        #   - This ensures each cut is used AT MOST ONCE.
        #   - If we iterated forward, we might "reuse" the same cut multiple times.
        #
        # Transition:
        #   dp[j] = min(dp[j],           <- don't use cut i
        #               dp[j-1] + cost_i) <- use cut i (adds 1 to cut count, adds cost)
        # -----------------------------------------------------------------------
        for i in range(n):
            # cost of the current cut we're considering
            current_cost = costs[i]

            # Iterate in reverse to maintain 0/1 knapsack property (each cut used once)
            for j in range(cuts_needed, 0, -1):
                # dp[j-1] must be reachable (not infinity) before we can extend it
                if dp[j - 1] != INF:
                    # Option: use cut i to go from (j-1) cuts to j cuts
                    # Compare with existing dp[j] (which represents not using cut i)
                    dp[j] = min(dp[j], dp[j - 1] + current_cost)

        # -----------------------------------------------------------------------
        # Step 6: Return the result.
        #
        # dp[cuts_needed] holds the minimum cost to make exactly (k-1) cuts.
        # If it's still INF, it means it's impossible (shouldn't happen if
        # cuts_needed <= n, but we handle it gracefully).
        # -----------------------------------------------------------------------
        result = dp[cuts_needed]
        return result if result != INF else -1


# -------------------------------------------------------------------------------
# Verification / Tracing through examples:
#
# Example 1: cuts = [2, 5, 7, 9], costs = [3, 8, 2, 6], k = 3
#   cuts_needed = 2
#   dp = [0, INF, INF]
#
#   Process cut 0 (pos=2, cost=3):
#     j=2: dp[1]=INF, skip
#     j=1: dp[0]=0, dp[1] = min(INF, 0+3) = 3
#   dp = [0, 3, INF]
#
#   Process cut 1 (pos=5, cost=8):
#     j=2: dp[1]=3, dp[2] = min(INF, 3+8) = 11
#     j=1: dp[0]=0, dp[1] = min(3, 0+8) = 3
#   dp = [0, 3, 11]
#
#   Process cut 2 (pos=7, cost=2):
#     j=2: dp[1]=3, dp[2] = min(11, 3+2) = 5
#     j=1: dp[0]=0, dp[1] = min(3, 0+2) = 2
#   dp = [0, 2, 5]
#
#   Process cut 3 (pos=9, cost=6):
#     j=2: dp[1]=2, dp[2] = min(5, 2+6) = 5
#     j=1: dp[0]=0, dp[1] = min(2, 0+6) = 2
#   dp = [0, 2, 5]
#
#   Answer: dp[2] = 5 ✓
#
# Example 2: cuts = [1, 4, 6], costs = [10, 5, 7], k = 4
#   cuts_needed = 3, n = 3 (exactly enough)
#   dp = [0, INF, INF, INF]
#
#   Process cut 0 (pos=1, cost=10):
#     j=3: dp[2]=INF, skip
#     j=2: dp[1]=INF, skip
#     j=1: dp[0]=0, dp[1] = min(INF, 0+10) = 10
#   dp = [0, 10, INF, INF]
#
#   Process cut 1 (pos=4, cost=5):
#     j=3: dp[2]=INF, skip
#     j=2: dp[1]=10, dp[2] = min(INF, 10+5) = 15
#     j=1: dp[0]=0, dp[1] = min(10, 0+5) = 5
#   dp = [0, 5, 15, INF]
#
#   Process cut 2 (pos=6, cost=7):
#     j=3: dp[2]=15, dp[3] = min(INF, 15+7) = 22
#     j=2: dp[1]=5, dp[2] = min(15, 5+7) = 12
#     j=1: dp[0]=0, dp[1] = min(5, 0+7) = 5
#   dp = [0, 5, 12, 22]
#
#   Answer: dp[3] = 22 ✓
# -------------------------------------------------------------------------------


if __name__ == "__main__":
    # Create a Solution instance
    solution = Solution()

    # ------------------------------------------------------------------
    # Example 1:
    # cuts = [2, 5, 7, 9], costs = [3, 8, 2, 6], k = 3
    # Expected output: 5
    # Explanation: Choose cut at position 2 (cost 3) and position 7 (cost 2)
    #              Total = 3 + 2 = 5
    # ------------------------------------------------------------------
    cuts1 = [2, 5, 7, 9]
    costs1 = [3, 8, 2, 6]
    k1 = 3
    result1 = solution.minCostToCutRibbon(cuts1, costs1, k1)
    print(f"Example 1:")
    print(f"  cuts = {cuts1}, costs = {costs1}, k = {k1}")
    print(f"  Output: {result1}")
    print(f"  Expected: 5")
    print(f"  {'PASS' if result1 == 5 else 'FAIL'}")
    print()

    # ------------------------------------------------------------------
    # Example 2:
    # cuts = [1, 4, 6], costs = [10, 5, 7], k = 4
    # Expected output: 22
    # Explanation: Need k-1 = 3 cuts, exactly 3 available, must use all.
    #              Total = 10 + 5 + 7 = 22
    # ------------------------------------------------------------------
    cuts2 = [1, 4, 6]
    costs2 = [10, 5, 7]
    k2 = 4
    result2 = solution.minCostToCutRibbon(cuts2, costs2, k2)
    print(f"Example 2:")
    print(f"  cuts = {cuts2}, costs = {costs2}, k = {k2}")
    print(f"  Output: {result2}")
    print(f"  Expected: 22")
    print(f"  {'PASS' if result2 == 22 else 'FAIL'}")
    print()

    # ------------------------------------------------------------------
    # Example 3: Impossible case
    # cuts = [1, 4], costs = [10, 5], k = 4
    # Need k-1 = 3 cuts but only 2 available → return -1
    # ------------------------------------------------------------------
    cuts3 = [1, 4]
    costs3 = [10, 5]
    k3 = 4
    result3 = solution.minCostToCutRibbon(cuts3, costs3, k3)
    print(f"Example 3 (impossible):")
    print(f"  cuts = {cuts3}, costs = {costs3}, k = {k3}")
    print(f"  Output: {result3}")
    print(f"  Expected: -1")
    print(f"  {'PASS' if result3 == -1 else 'FAIL'}")
    print()

    # ------------------------------------------------------------------
    # Example 4: k = 1 (no cuts needed)
    # cuts = [3, 7], costs = [5, 9], k = 1
    # Need k-1 = 0 cuts → cost is 0
    # ------------------------------------------------------------------
    cuts4 = [3, 7]
    costs4 = [5, 9]
    k4 = 1
    result4 = solution.minCostToCutRibbon(cuts4, costs4, k4)
    print(f"Example 4 (k=1, no cuts needed):")
    print(f"  cuts = {cuts4}, costs = {costs4}, k = {k4}")
    print(f"  Output: {result4}")
    print(f"  Expected: 0")
    print(f"  {'PASS' if result4 == 0 else 'FAIL'}")
    print()

    # ------------------------------------------------------------------
    # Example 5: Choose cheapest single cut
    # cuts = [1, 2, 3], costs = [100, 1, 50], k = 2
    # Need k-1 = 1 cut → choose cheapest = 1 (at position 2)
    # ------------------------------------------------------------------
    cuts5 = [1, 2, 3]
    costs5 = [100, 1, 50]
    k5 = 2
    result5 = solution.minCostToCutRibbon(cuts5, costs5, k5)
    print(f"Example 5 (choose cheapest single cut):")
    print(f"  cuts = {cuts5}, costs = {costs5}, k = {k5}")
    print(f"  Output: {result5}")
    print(f"  Expected: 1")
    print(f"  {'PASS' if result5 == 1 else 'FAIL'}")
```