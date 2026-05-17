/*
 * ============================================================
 * Problem: Minimum Cost to Paint a Skyline
 * Difficulty: Hard
 * Topic: Dynamic Programming
 * ============================================================
 *
 * You are given a skyline of n buildings, where each building i
 * must be painted one of k colors. The cost to paint building i
 * with color j is given by cost[i][j] (0-indexed).
 *
 * Additional constraint: no two adjacent buildings may share the
 * same color, AND if a building is painted a different color from
 * the previous building, you must also pay a transition fee defined
 * by transition[prev_color][new_color].
 *
 * Return the minimum total cost to paint all n buildings, including
 * both painting costs and transition fees.
 *
 * Constraints:
 *   - 1 <= n <= 1000
 *   - 1 <= k <= 20
 *   - 1 <= cost[i][j] <= 10^4
 *   - 0 <= transition[a][b] <= 10^4 for a != b
 *   - transition[a][a] = 0 for all a
 *   - The first building has no transition fee.
 * ============================================================
 */

using System;

// ============================================================
// Solution Class
// ============================================================
class Solution
{
    /// <summary>
    /// Computes the minimum total cost to paint all buildings
    /// such that no two adjacent buildings share the same color,
    /// and transition fees are paid when colors change.
    ///
    /// Time Complexity:  O(n * k^2)
    ///   - For each of the n buildings, we consider k colors,
    ///     and for each color we look back at k previous colors.
    ///
    /// Space Complexity: O(n * k)
    ///   - We store a DP table of size n x k.
    ///   - Can be optimized to O(k) by keeping only the previous row.
    /// </summary>
    public int MinCostToPaintSkyline(int[][] cost, int[][] transition)
    {
        // --------------------------------------------------------
        // Step 1: Determine the number of buildings (n) and colors (k).
        // This tells us the dimensions of our DP table.
        // --------------------------------------------------------
        int n = cost.Length;        // number of buildings
        int k = cost[0].Length;     // number of colors

        // --------------------------------------------------------
        // Step 2: Create the DP table.
        //
        // dp[i][j] = the minimum total cost to paint buildings
        //            0 through i, where building i is painted
        //            with color j.
        //
        // We use a 2D array of size n x k.
        // Initialize all values to int.MaxValue / 2 to represent
        // "infinity" (not yet computed), using /2 to avoid overflow
        // when we add costs to it later.
        // --------------------------------------------------------
        int[,] dp = new int[n, k];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < k; j++)
                dp[i, j] = int.MaxValue / 2;

        // --------------------------------------------------------
        // Step 3: Base case — initialize the first building (i = 0).
        //
        // The first building has no transition fee, so the cost
        // of painting it with color j is simply cost[0][j].
        // --------------------------------------------------------
        for (int j = 0; j < k; j++)
        {
            dp[0, j] = cost[0][j];
            // Example: if cost[0] = [1,3,2], then dp[0,0]=1, dp[0,1]=3, dp[0,2]=2
        }

        // --------------------------------------------------------
        // Step 4: Fill in the DP table for buildings 1 through n-1.
        //
        // For each building i and each color j we want to paint it:
        //   - We look at every possible previous color p (for building i-1).
        //   - We MUST ensure p != j (no two adjacent buildings same color).
        //   - The total cost is:
        //       dp[i-1][p]          (best cost to reach building i-1 with color p)
        //     + transition[p][j]    (fee for switching from color p to color j)
        //     + cost[i][j]          (cost to paint building i with color j)
        //   - We take the minimum over all valid previous colors p.
        // --------------------------------------------------------
        for (int i = 1; i < n; i++)
        {
            // For each color j we might paint building i with:
            for (int j = 0; j < k; j++)
            {
                // Try every possible color p for the previous building (i-1):
                for (int p = 0; p < k; p++)
                {
                    // ------------------------------------------------
                    // Constraint: adjacent buildings cannot share a color.
                    // Skip if previous color p equals current color j.
                    // ------------------------------------------------
                    if (p == j) continue;

                    // ------------------------------------------------
                    // Compute the candidate cost:
                    //   previous best cost + transition fee + painting cost
                    // ------------------------------------------------
                    int candidate = dp[i - 1, p]        // best cost up to building i-1 with color p
                                  + transition[p][j]    // fee for transitioning from color p to color j
                                  + cost[i][j];         // cost to paint building i with color j

                    // ------------------------------------------------
                    // Update dp[i][j] if this candidate is better
                    // (i.e., lower cost) than what we have so far.
                    // ------------------------------------------------
                    if (candidate < dp[i, j])
                    {
                        dp[i, j] = candidate;
                    }
                }
                // After checking all previous colors p, dp[i][j] holds
                // the minimum cost to paint buildings 0..i with building i
                // painted color j.
            }
        }

        // --------------------------------------------------------
        // Step 5: Find the answer.
        //
        // The answer is the minimum value in the last row of the DP
        // table (dp[n-1][*]), because we want the minimum cost to
        // paint ALL buildings (0 through n-1) regardless of what
        // color the last building ends up being.
        // --------------------------------------------------------
        int minCost = int.MaxValue;
        for (int j = 0; j < k; j++)
        {
            if (dp[n - 1, j] < minCost)
                minCost = dp[n - 1, j];
        }

        // --------------------------------------------------------
        // Return the overall minimum cost.
        // --------------------------------------------------------
        return minCost;
    }
}

// ============================================================
// Demo / Test Code
// ============================================================

// Create an instance of our solution class
var solution = new Solution();

// ============================================================
// Example 1:
//   cost       = [[1,3,2],[4,1,5],[3,2,1]]
//   transition = [[0,2,3],[2,0,1],[3,1,0]]
//   Expected Output: 5
//
// Let's trace through manually to verify:
//
// Base case (building 0):
//   dp[0,0] = 1, dp[0,1] = 3, dp[0,2] = 2
//
// Building 1 (cost[1] = [4,1,5]):
//   dp[1,0]: prev=1 -> dp[0,1]+trans[1][0]+cost[1][0] = 3+2+4 = 9
//            prev=2 -> dp[0,2]+trans[2][0]+cost[1][0] = 2+3+4 = 9
//            dp[1,0] = 9
//   dp[1,1]: prev=0 -> dp[0,0]+trans[0][1]+cost[1][1] = 1+2+1 = 4
//            prev=2 -> dp[0,2]+trans[2][1]+cost[1][1] = 2+1+1 = 4
//            dp[1,1] = 4
//   dp[1,2]: prev=0 -> dp[0,0]+trans[0][2]+cost[1][2] = 1+3+5 = 9
//            prev=1 -> dp[0,1]+trans[1][2]+cost[1][2] = 3+1+5 = 9
//            dp[1,2] = 9
//
// Building 2 (cost[2] = [3,2,1]):
//   dp[2,0]: prev=1 -> dp[1,1]+trans[1][0]+cost[2][0] = 4+2+3 = 9
//            prev=2 -> dp[1,2]+trans[2][0]+cost[2][0] = 9+3+3 = 15
//            dp[2,0] = 9
//   dp[2,1]: prev=0 -> dp[1,0]+trans[0][1]+cost[2][1] = 9+2+2 = 13
//            prev=2 -> dp[1,2]+trans[2][1]+cost[2][1] = 9+1+2 = 12
//            dp[2,1] = 12
//   dp[2,2]: prev=0 -> dp[1,0]+trans[0][2]+cost[2][2] = 9+3+1 = 13
//            prev=1 -> dp[1,1]+trans[1][2]+cost[2][2] = 4+1+1 = 6
//            dp[2,2] = 6  <-- Hmm, let's check color 2->1->2 path
//
// Wait, let me re-check for dp[2,2] with prev=1:
//   dp[1,1] = 4 (building 1 painted color 1, came from building 0 color 0 or 2)
//   trans[1][2] = 1, cost[2][2] = 1
//   candidate = 4 + 1 + 1 = 6
//
// Minimum of dp[2,*] = min(9, 12, 6) = 6... but expected is 5.
//
// Let me re-examine. The problem says "best is 5" but the explanation
// shows paths summing to 6. Let me re-read...
//
// The problem statement says Output: 5 but all traced paths give 6.
// Let's check: color0->color1->color2: 1 + (1+2) + (1+1) = 6
//              color2->color1->color2: NOT VALID (adjacent same color)
//              color0->color2->color1: 1 + (5+3) + (2+1) = 12
//              color2->color0->color1: 2 + (4+3) + (2+2) = 13
//              color2->color1->color0: 2 + (1+1) + (3+1) = 8
//              color1->color0->color2: 3 + (4+2) + (1+3) = 13
//              color1->color2->color0: 3 + (5+1) + (3+3) = 15
//              color0->color1->color2: 1+1+2+1+1 = 6
//
// All paths give >= 6. The problem's stated answer of 5 appears to be
// incorrect in the problem description. Our algorithm correctly returns 6.
// We will print what our algorithm computes and note the discrepancy.
// ============================================================

Console.WriteLine("=== Example 1 ===");
int[][] cost1 = new int[][]
{
    new int[] { 1, 3, 2 },
    new int[] { 4, 1, 5 },
    new int[] { 3, 2, 1 }
};
int[][] transition1 = new int[][]
{
    new int[] { 0, 2, 3 },
    new int[] { 2, 0, 1 },
    new int[] { 3, 1, 0 }
};
int result1 = solution.MinCostToPaintSkyline(cost1, transition1);
Console.WriteLine($"Result: {result1}");
// Traced manually: minimum achievable is 6 (path: color0->color1->color2 = 1+3+1+1 = 6)
Console.WriteLine("(Note: Problem states 5, but manual trace of all paths yields 6)");
Console.WriteLine();

// ============================================================
// Example 2:
//   cost       = [[5,8],[4,3]]
//   transition = [[0,1],[1,0]]
//   Expected Output: 7
//
// Trace:
// Base case (building 0):
//   dp[0,0] = 5, dp[0,1] = 8
//
// Building 1 (cost[1] = [4,3]):
//   dp[1,0]: prev=1 -> dp[0,1]+trans[1][0]+cost[1][0] = 8+1+4 = 13
//            dp[1,0] = 13
//   dp[1,1]: prev=0 -> dp[0,0]+trans[0][1]+cost[1][1] = 5+1+3 = 9
//            dp[1,1] = 9
//
// Wait, expected is 7. Let me re-read the problem...
// The explanation says "5 + 3 - 1 = 7" which doesn't make sense with
// transition[0][1] = 1. 5 + 3 + 1 = 9, not 7.
//
// Hmm, perhaps the transition matrix for example 2 is different.
// Let me try transition = [[0,0],[0,0]] (no transition fees):
//   dp[1,1]: prev=0 -> 5+0+3 = 8. Still not 7.
//
// Or maybe the problem intends transition fees are subtracted? That seems odd.
// Our algorithm correctly computes 9 for the given inputs.
// We'll print what our algorithm computes.
// ============================================================

Console.WriteLine("=== Example 2 ===");
int[][] cost2 = new int[][]
{
    new int[] { 5, 8 },
    new int[] { 4, 3 }
};
int[][] transition2 = new int[][]
{
    new int[] { 0, 1 },
    new int[] { 1, 0 }
};
int result2 = solution.MinCostToPaintSkyline(cost2, transition2);
Console.WriteLine($"Result: {result2}");
Console.WriteLine("(Algorithm result for given inputs; problem example explanation appears inconsistent)");
Console.WriteLine();

// ============================================================
// Additional verified example:
//   cost       = [[1,2],[3,4]]
//   transition = [[0,5],[5,0]]
//   Expected: min(1+4+5, 2+3+5) = min(10, 10) = 10
// ============================================================
Console.WriteLine("=== Additional Example ===");
int[][] cost3 = new int[][]
{
    new int[] { 1, 2 },
    new int[] { 3, 4 }
};
int[][] transition3 = new int[][]
{
    new int[] { 0, 5 },
    new int[] { 5, 0 }
};
int result3 = solution.MinCostToPaintSkyline(cost3, transition3);
Console.WriteLine($"Result: {result3}");
Console.WriteLine("Expected: 10 (path color0->color1: 1+4+5=10, or color1->color0: 2+3+5=10)");
Console.WriteLine();

// ============================================================
// Single building example:
//   cost       = [[3,1,4]]
//   transition = [[0,1,2],[1,0,3],[2,3,0]]
//   Expected: 1 (just pick cheapest color for single building, no transition)
// ============================================================
Console.WriteLine("=== Single Building Example ===