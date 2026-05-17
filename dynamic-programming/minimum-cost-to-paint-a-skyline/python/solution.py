```python
"""
Title: Minimum Cost to Paint a Skyline
Difficulty: Hard
Topic: Dynamic Programming

Problem Description:
You are given a skyline of n buildings, where each building i must be painted one of k colors.
The cost to paint building i with color j is given by cost[i][j] (0-indexed). However, there is
an additional constraint: no two adjacent buildings may share the same color, AND if a building
is painted a color different from the previous building's color, you must also pay a transition
fee defined by transition[prev_color][new_color].

Return the minimum total cost to paint all n buildings, including both painting costs and
transition fees.

Constraints:
- 1 <= n <= 1000
- 1 <= k <= 20
- 1 <= cost[i][j] <= 10^4
- 0 <= transition[a][b] <= 10^4 for a != b
- transition[a][a] = 0 for all a
- The first building has no transition fee.
"""

from typing import List


class Solution:
    def minCostToPaintSkyline(
        self,
        cost: List[List[int]],
        transition: List[List[int]]
    ) -> int:
        """
        Compute the minimum total cost to paint all buildings with no two adjacent
        buildings sharing the same color, including transition fees between colors.

        Args:
            cost: 2D list where cost[i][j] is the cost to paint building i with color j.
            transition: 2D list where transition[a][b] is the fee when switching from
                        color a to color b (transition[a][a] = 0).

        Returns:
            The minimum total cost (painting + transition fees) to paint all buildings.

        Time Complexity: O(n * k^2) where n = number of buildings, k = number of colors.
            For each building (n), we consider all pairs of (prev_color, curr_color) = k^2.

        Space Complexity: O(k) since we only keep the DP array for the previous building.
        """

        # -----------------------------------------------------------------------
        # Step 1: Extract dimensions
        # n = number of buildings, k = number of colors
        # -----------------------------------------------------------------------
        n = len(cost)
        k = len(cost[0])

        # -----------------------------------------------------------------------
        # Step 2: Initialize the DP array for the first building.
        #
        # dp[j] = minimum cost to paint buildings 0..i such that building i
        #         is painted with color j.
        #
        # For the first building (i=0), there is no transition fee, so:
        #   dp[j] = cost[0][j]
        # -----------------------------------------------------------------------
        # We use a 1D DP array of size k, representing the best cost ending
        # at each color for the current building.
        dp = [cost[0][j] for j in range(k)]

        # -----------------------------------------------------------------------
        # Step 3: Iterate over buildings 1 through n-1.
        #
        # For each building i, we compute a new DP array new_dp where:
        #   new_dp[c] = min over all prev_color p (p != c) of:
        #               dp[p] + transition[p][c] + cost[i][c]
        #
        # The constraint p != c enforces that no two adjacent buildings share
        # the same color.
        # -----------------------------------------------------------------------
        for i in range(1, n):
            # Create a new DP array for building i, initialized to infinity
            # (we'll fill in the minimum values below)
            new_dp = [float('inf')] * k

            # -------------------------------------------------------------------
            # Step 3a: For each possible color c for the current building i,
            # try all possible previous colors p (where p != c).
            # -------------------------------------------------------------------
            for c in range(k):
                # Current building painting cost
                paint_cost = cost[i][c]

                for p in range(k):
                    # Skip if previous color equals current color
                    # (adjacent buildings cannot share the same color)
                    if p == c:
                        continue

                    # Total cost if building i-1 was color p and building i is color c:
                    #   dp[p]            -> best cost to reach building i-1 with color p
                    #   transition[p][c] -> fee for switching from color p to color c
                    #   paint_cost       -> cost to paint building i with color c
                    total = dp[p] + transition[p][c] + paint_cost

                    # Keep the minimum cost for painting building i with color c
                    if total < new_dp[c]:
                        new_dp[c] = total

            # -------------------------------------------------------------------
            # Step 3b: Update dp to new_dp for the next iteration.
            # We discard the old dp since we only need the previous building's costs.
            # -------------------------------------------------------------------
            dp = new_dp

        # -----------------------------------------------------------------------
        # Step 4: The answer is the minimum value in dp after processing all buildings.
        # dp[c] holds the minimum cost to paint all n buildings such that the last
        # building is painted with color c.
        # -----------------------------------------------------------------------
        return min(dp)


# ---------------------------------------------------------------------------
# Verification / Tracing through examples:
#
# Example 1:
#   cost = [[1,3,2],[4,1,5],[3,2,1]]
#   transition = [[0,2,3],[2,0,1],[3,1,0]]
#   k=3, n=3
#
#   Init dp = [1, 3, 2]  (costs for building 0)
#
#   Building 1 (i=1):
#     c=0 (paint cost=4):
#       p=1: dp[1]+trans[1][0]+4 = 3+2+4 = 9
#       p=2: dp[2]+trans[2][0]+4 = 2+3+4 = 9
#       new_dp[0] = 9
#     c=1 (paint cost=1):
#       p=0: dp[0]+trans[0][1]+1 = 1+2+1 = 4
#       p=2: dp[2]+trans[2][1]+1 = 2+1+1 = 4
#       new_dp[1] = 4
#     c=2 (paint cost=5):
#       p=0: dp[0]+trans[0][2]+5 = 1+3+5 = 9
#       p=1: dp[1]+trans[1][2]+5 = 3+1+5 = 9
#       new_dp[2] = 9
#   dp = [9, 4, 9]
#
#   Building 2 (i=2):
#     c=0 (paint cost=3):
#       p=1: dp[1]+trans[1][0]+3 = 4+2+3 = 9
#       p=2: dp[2]+trans[2][0]+3 = 9+3+3 = 15
#       new_dp[0] = 9
#     c=1 (paint cost=2):
#       p=0: dp[0]+trans[0][1]+2 = 9+2+2 = 13
#       p=2: dp[2]+trans[2][1]+2 = 9+1+2 = 12
#       new_dp[1] = 12
#     c=2 (paint cost=1):
#       p=0: dp[0]+trans[0][2]+1 = 9+3+1 = 13
#       p=1: dp[1]+trans[1][2]+1 = 4+1+1 = 6
#       new_dp[2] = 6
#   dp = [9, 12, 6]  -> min = 6
#
#   Hmm, the expected output is 5. Let me re-check the problem statement.
#   The problem says "Output: 5" but the explanation gives 6. This seems like
#   the problem statement has an inconsistency. Let me trace the explanation:
#   "paint 0→color 2 (2), 1→color 1 (1, transition 1), 2→color 2 (1, transition 1) = 6"
#   But color 2 -> color 1 -> color 2 is valid (no adjacent same color).
#   2 + (1+1) + (1+1) = 6. The problem says "Best is 5" but doesn't show how.
#
#   Let me check all paths:
#   0→c0(1), 1→c1(1+trans[0][1]=2), 2→c2(1+trans[1][2]=1) = 1+3+2 = 6
#   0→c0(1), 1→c2(5+trans[0][2]=3), 2→c1(2+trans[2][1]=1) = 1+8+3 = 12
#   0→c1(3), 1→c0(4+trans[1][0]=2), 2→c2(1+trans[0][2]=3) = 3+6+4 = 13
#   0→c1(3), 1→c2(5+trans[1][2]=1), 2→c0(3+trans[2][0]=3) = 3+6+6 = 15
#   0→c2(2), 1→c0(4+trans[2][0]=3), 2→c1(2+trans[0][1]=2) = 2+7+4 = 13
#   0→c2(2), 1→c1(1+trans[2][1]=1), 2→c0(3+trans[1][0]=2) = 2+2+5 = 9
#   0→c2(2), 1→c1(1+trans[2][1]=1), 2→c2(1+trans[1][2]=1) = 2+2+2 = 6
#   0→c0(1), 1→c1(1+2), 2→c0(3+trans[1][0]=2) = 1+3+5 = 9
#
#   The minimum across all valid paths is 6. The problem statement says 5 but
#   the explanation itself gives 6. Our algorithm correctly returns 6.
#   The stated output of 5 appears to be an error in the problem statement.
#
# Example 2:
#   cost = [[5,8],[4,3]]
#   transition = [[0,1],[1,0]]
#   k=2, n=2
#
#   Init dp = [5, 8]
#
#   Building 1 (i=1):
#     c=0 (paint cost=4):
#       p=1: dp[1]+trans[1][0]+4 = 8+1+4 = 13
#       new_dp[0] = 13
#     c=1 (paint cost=3):
#       p=0: dp[0]+trans[0][1]+3 = 5+1+3 = 9
#       new_dp[1] = 9
#   dp = [13, 9] -> min = 9
#
#   The problem says output is 7, but:
#   Path 0→c0(5), 1→c1(3+1) = 5+4 = 9
#   Path 0→c1(8), 1→c0(4+1) = 8+5 = 13
#   Minimum is 9. The problem says 7, which seems incorrect.
#   Our algorithm returns 9 which matches the actual calculation.
#
# The problem statement examples appear to have errors in the stated outputs.
# Our algorithm is correct based on the problem description logic.
# ---------------------------------------------------------------------------


if __name__ == "__main__":
    solution = Solution()

    # -----------------------------------------------------------------------
    # Example 1
    # cost = [[1,3,2],[4,1,5],[3,2,1]]
    # transition = [[0,2,3],[2,0,1],[3,1,0]]
    # Expected by problem: 5 (but actual minimum based on problem rules is 6)
    # -----------------------------------------------------------------------
    cost1 = [[1, 3, 2], [4, 1, 5], [3, 2, 1]]
    transition1 = [[0, 2, 3], [2, 0, 1], [3, 1, 0]]
    result1 = solution.minCostToPaintSkyline(cost1, transition1)
    print(f"Example 1:")
    print(f"  cost = {cost1}")
    print(f"  transition = {transition1}")
    print(f"  Result: {result1}")
    print(f"  (Problem states 5, but tracing all paths gives minimum of 6)")
    print()

    # -----------------------------------------------------------------------
    # Example 2
    # cost = [[5,8],[4,3]]
    # transition = [[0,1],[1,0]]
    # Expected by problem: 7 (but actual minimum based on problem rules is 9)
    # -----------------------------------------------------------------------
    cost2 = [[5, 8], [4, 3]]
    transition2 = [[0, 1], [1, 0]]
    result2 = solution.minCostToPaintSkyline(cost2, transition2)
    print(f"Example 2:")
    print(f"  cost = {cost2}")
    print(f"  transition = {transition2}")
    print(f"  Result: {result2}")
    print(f"  (Problem states 7, but tracing all paths gives minimum of 9)")
    print()

    # -----------------------------------------------------------------------
    # Additional Example: Single building (no transition fees apply)
    # cost = [[3, 1, 4]]
    # transition = [[0,1,2],[1,0,3],[2,3,0]]
    # Expected: 1 (just pick the cheapest color for the single building)
    # -----------------------------------------------------------------------
    cost3 = [[3, 1, 4]]
    transition3 = [[0, 1, 2], [1, 0, 3], [2, 3, 0]]
    result3 = solution.minCostToPaintSkyline(cost3, transition3)
    print(f"Example 3 (single building):")
    print(f"  cost = {cost3}")
    print(f"  transition = {transition3}")
    print(f"  Result: {result3}")
    print(f"  Expected: 1")
    print()

    # -----------------------------------------------------------------------
    # Additional Example: Two buildings, two colors, high transition fee
    # cost = [[1, 10],[10, 1]]
    # transition = [[0, 100],[100, 0]]
    # Best: 0→c0(1), 1→c1(1+100) = 102
    #       0→c1(10), 1→c0(10+100) = 120
    # Expected: 102
    # -----------------------------------------------------------------------
    cost4 = [[1, 10], [10, 1]]
    transition4 = [[0, 100], [100, 0]]
    result4 = solution.minCostToPaintSkyline(cost4, transition4)
    print(f"Example 4 (high transition fee):")
    print(f"  cost = {cost4}")
    print(f"  transition = {transition4}")
    print(f"  Result: {result4}")
    print(f"  Expected: 102")
```