```java
/*
 * Title: Minimum Cost to Paint a Skyline
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given a skyline of n buildings, where each building i must be painted one of k colors.
 * The cost to paint building i with color j is given by cost[i][j] (0-indexed).
 * However, there is an additional constraint: no two adjacent buildings may share the same color,
 * AND if a building is painted a color different from the previous building's color, you must also
 * pay a transition fee defined by transition[prev_color][new_color].
 *
 * Return the minimum total cost to paint all n buildings, including both painting costs and
 * transition fees.
 *
 * Constraints:
 * - 1 <= n <= 1000
 * - 1 <= k <= 20
 * - 1 <= cost[i][j] <= 10^4
 * - 0 <= transition[a][b] <= 10^4 for a != b
 * - transition[a][a] = 0 for all a
 * - The first building has no transition fee.
 */

import java.util.*;

/**
 * Solution class for the Minimum Cost to Paint a Skyline problem.
 * Uses dynamic programming to find the minimum cost to paint all buildings
 * while respecting the no-adjacent-same-color constraint and paying transition fees.
 */
public class Solution {

    /**
     * Computes the minimum total cost to paint all buildings in the skyline.
     *
     * <p>Approach: Classic DP where dp[i][j] = minimum cost to paint buildings 0..i
     * such that building i is painted with color j.
     *
     * <p>Transition:
     * dp[i][j] = cost[i][j] + min over all prev_color != j of (dp[i-1][prev_color] + transition[prev_color][j])
     *
     * <p>Base case:
     * dp[0][j] = cost[0][j] for all j (no transition fee for the first building)
     *
     * @param cost       2D array where cost[i][j] is the cost to paint building i with color j
     * @param transition 2D array where transition[a][b] is the fee to switch from color a to color b
     * @return the minimum total cost to paint all buildings
     *
     * Time Complexity:  O(n * k^2) where n = number of buildings, k = number of colors
     *                   For each building (n), for each color (k), we check all previous colors (k)
     * Space Complexity: O(n * k) for the DP table, or O(k) if we optimize to only keep previous row
     */
    public int minCostToPaintSkyline(int[][] cost, int[][] transition) {
        // Determine the number of buildings and colors
        int n = cost.length;
        int k = cost[0].length;

        // -----------------------------------------------------------------------
        // Step 1: Initialize the DP table
        // dp[i][j] = minimum cost to paint buildings 0 through i,
        //            where building i is painted with color j
        // -----------------------------------------------------------------------
        // We use a 2D array of size n x k
        // Initialize all values to a large number (infinity) to represent "not yet computed"
        int[][] dp = new int[n][k];
        for (int[] row : dp) {
            Arrays.fill(row, Integer.MAX_VALUE / 2); // Use MAX_VALUE/2 to avoid overflow on addition
        }

        // -----------------------------------------------------------------------
        // Step 2: Base case — paint the first building (index 0)
        // No transition fee for the first building, so dp[0][j] = cost[0][j]
        // -----------------------------------------------------------------------
        for (int j = 0; j < k; j++) {
            dp[0][j] = cost[0][j];
        }

        // -----------------------------------------------------------------------
        // Step 3: Fill in the DP table for buildings 1 through n-1
        // For each building i and each color j (the color we want to paint building i):
        //   - We look at all possible previous colors (prev) for building i-1
        //   - Since adjacent buildings CANNOT share the same color, prev != j
        //   - The cost is: dp[i-1][prev] + transition[prev][j] + cost[i][j]
        //   - We take the minimum over all valid prev colors
        // -----------------------------------------------------------------------
        for (int i = 1; i < n; i++) {
            // For each color j that we might paint building i with
            for (int j = 0; j < k; j++) {
                // Try all possible colors for the previous building (building i-1)
                for (int prev = 0; prev < k; prev++) {
                    // Adjacent buildings cannot share the same color
                    if (prev == j) {
                        continue; // Skip — same color not allowed for adjacent buildings
                    }

                    // Calculate the total cost if building i-1 was color 'prev'
                    // and building i is color 'j':
                    //   dp[i-1][prev]        = best cost to reach building i-1 with color prev
                    //   transition[prev][j]  = fee for switching from color prev to color j
                    //   cost[i][j]           = cost to paint building i with color j
                    int totalCost = dp[i - 1][prev] + transition[prev][j] + cost[i][j];

                    // Update dp[i][j] if this path is cheaper
                    dp[i][j] = Math.min(dp[i][j], totalCost);
                }
            }
        }

        // -----------------------------------------------------------------------
        // Step 4: Find the answer
        // The answer is the minimum value in the last row of the DP table,
        // i.e., the minimum cost to paint all buildings where the last building
        // can be any color.
        // -----------------------------------------------------------------------
        int minCost = Integer.MAX_VALUE;
        for (int j = 0; j < k; j++) {
            minCost = Math.min(minCost, dp[n - 1][j]);
        }

        return minCost;
    }

    /**
     * Space-optimized version that uses only O(k) space instead of O(n*k).
     * Since we only need the previous row to compute the current row, we can
     * use two 1D arrays instead of a full 2D table.
     *
     * @param cost       2D array where cost[i][j] is the cost to paint building i with color j
     * @param transition 2D array where transition[a][b] is the fee to switch from color a to color b
     * @return the minimum total cost to paint all buildings
     *
     * Time Complexity:  O(n * k^2) — same as the standard version
     * Space Complexity: O(k) — only two 1D arrays of size k are used
     */
    public int minCostToPaintSkylineOptimized(int[][] cost, int[][] transition) {
        int n = cost.length;
        int k = cost[0].length;

        // 'prev' holds the DP values for the previous building
        // 'curr' holds the DP values for the current building being processed
        int[] prev = new int[k];
        int[] curr = new int[k];

        // Base case: first building, no transition fee
        for (int j = 0; j < k; j++) {
            prev[j] = cost[0][j];
        }

        // Process each subsequent building
        for (int i = 1; i < n; i++) {
            // Reset current row to "infinity"
            Arrays.fill(curr, Integer.MAX_VALUE / 2);

            for (int j = 0; j < k; j++) {
                for (int p = 0; p < k; p++) {
                    if (p == j) continue; // Adjacent buildings must differ in color

                    int totalCost = prev[p] + transition[p][j] + cost[i][j];
                    curr[j] = Math.min(curr[j], totalCost);
                }
            }

            // Move current row to previous for the next iteration
            // Swap references for efficiency
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        // The answer is the minimum value in the 'prev' array (which holds the last row)
        int minCost = Integer.MAX_VALUE;
        for (int j = 0; j < k; j++) {
            minCost = Math.min(minCost, prev[j]);
        }

        return minCost;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     * Traces through the examples from the problem description.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // cost = [[1,3,2],[4,1,5],[3,2,1]]
        // transition = [[0,2,3],[2,0,1],[3,1,0]]
        // Expected Output: 5
        //
        // Let's trace through manually to verify:
        // Buildings: 0, 1, 2  |  Colors: 0, 1, 2
        //
        // Base case (building 0):
        //   dp[0][0] = 1, dp[0][1] = 3, dp[0][2] = 2
        //
        // Building 1:
        //   dp[1][0]: prev=1 -> 3 + transition[1][0]=2 + cost[1][0]=4 = 9
        //             prev=2 -> 2 + transition[2][0]=3 + cost[1][0]=4 = 9
        //             min = 9
        //   dp[1][1]: prev=0 -> 1 + transition[0][1]=2 + cost[1][1]=1 = 4
        //             prev=2 -> 2 + transition[2][1]=1 + cost[1][1]=1 = 4
        //             min = 4
        //   dp[1][2]: prev=0 -> 1 + transition[0][2]=3 + cost[1][2]=5 = 9
        //             prev=1 -> 3 + transition[1][2]=1 + cost[1][2]=5 = 9
        //             min = 9
        //
        // Building 2:
        //   dp[2][0]: prev=1 -> 4 + transition[1][0]=2 + cost[2][0]=3 = 9
        //             prev=2 -> 9 + transition[2][0]=3 + cost[2][0]=3 = 15
        //             min = 9
        //   dp[2][1]: prev=0 -> 9 + transition[0][1]=2 + cost[2][1]=2 = 13
        //             prev=2 -> 9 + transition[2][1]=1 + cost[2][1]=2 = 12
        //             min = 12
        //   dp[2][2]: prev=0 -> 9 + transition[0][2]=3 + cost[2][2]=1 = 13
        //             prev=1 -> 4 + transition[1][2]=1 + cost[2][2]=1 = 6
        //             min = 6  <-- Wait, let me recheck...
        //             Actually prev=1: dp[1][1]=4, transition[1][2]=1, cost[2][2]=1 => 4+1+1=6
        //             prev=0: dp[1][0]=9, transition[0][2]=3, cost[2][2]=1 => 9+3+1=13
        //             min = 6
        //
        // Hmm, but we also need to check dp[2][2] with prev=1 more carefully.
        // Wait, let me re-examine dp[1][1]:
        //   prev=0: dp[0][0]=1, transition[0][1]=2, cost[1][1]=1 => 1+2+1=4 ✓
        //   prev=2: dp[0][2]=2, transition[2][1]=1, cost[1][1]=1 => 2+1+1=4 ✓
        //   dp[1][1] = 4 ✓
        //
        // dp[2][2] with prev=1: 4 + 1 + 1 = 6
        // But wait, the path is: building0=color2(cost2), building1=color1(cost1+transition[2][1]=1),
        //                        building2=color2(cost1+transition[1][2]=1)
        // Total = 2 + (1+1) + (1+1) = 6
        // Hmm, but expected is 5. Let me re-examine...
        //
        // Actually wait - let me re-read the problem. The explanation says "Best is 5"
        // but doesn't clearly show which path gives 5. Let me try all paths more carefully.
        //
        // Actually, looking at dp[2][2] again:
        //   prev=1: dp[1][1] + transition[1][2] + cost[2][2] = 4 + 1 + 1 = 6
        //   prev=0: dp[1][0] + transition[0][2] + cost[2][2] = 9 + 3 + 1 = 13
        //   dp[2][2] = 6
        //
        // dp[2][0] = 9, dp[2][1] = 12, dp[2][2] = 6
        // Minimum = 6
        //
        // Hmm, that gives 6, not 5. Let me re-examine the problem statement...
        // The problem says output is 5 but the explanation itself says 6 for the optimal shown.
        // It says "Best is 5" without showing the path. Let me check if there's a path giving 5.
        //
        // All possible paths (3 buildings, 3 colors, no adjacent same):
        // 0->1->0: 1 + (4+2) + (3+2) = 12
        // 0->1->2: 1 + (4+2) + (1+1) = 9
        // 0->2->0: 1 + (5+3) + (3+3) = 15
        // 0->2->1: 1 + (5+3) + (2+1) = 12
        // 1->0->1: 3 + (4+2) + (2+2) = 13
        // 1->0->2: 3 + (4+2) + (1+3) = 13
        // 1->2->0: 3 + (5+1) + (3+3) = 15
        // 1->2->1: 3 + (5+1) + (2+1) = 12  -- wait transition[2][1]=1
        //          Actually: 3 + (5+1) + (2+1) = 12
        // 2->0->1: 2 + (4+3) + (2+2) = 13
        // 2->0->2: 2 + (4+3) + (1+3) = 13
        // 2->1->0: 2 + (1+1) + (3+2) = 9
        // 2->1->2: 2 + (1+1) + (1+1) = 6  <-- This is the minimum I found = 6
        //
        // So the minimum is 6, not 5. The problem statement's example 1 output seems to be 6.
        // I'll trust my calculation and output 6 for example 1.
        // The problem statement may have an error in the stated output.
        // -----------------------------------------------------------------------

        System.out.println("=== Example 1 ===");
        int[][] cost1 = {{1, 3, 2}, {4, 1, 5}, {3, 2, 1}