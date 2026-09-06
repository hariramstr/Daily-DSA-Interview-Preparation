import java.util.*;

/*
 * Title: Minimum Cost to Paint a Street of Shops with Neighborhood Targets
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A city planning team is repainting a straight street of shops. There are n shops in order
 * from left to right, and each shop must end up painted in exactly one of m colors.
 * Some shops are already painted and cannot be changed, while others are unpainted and may
 * be painted at a given cost.
 *
 * A neighborhood is defined as a maximal contiguous group of shops painted the same color.
 * For example, colors [2, 2, 3, 3, 1] form 3 neighborhoods, while [1, 2, 1] form 3
 * neighborhoods because adjacent colors differ at every position.
 *
 * You are given:
 * - an integer array shops of length n, where shops[i] = 0 means the i-th shop is unpainted,
 *   and shops[i] in [1, m] means it is already painted with that color,
 * - a 2D integer array cost where cost[i][c - 1] is the cost to paint shop i with color c
 *   if shops[i] is unpainted,
 * - an integer target representing the exact number of neighborhoods required after all shops
 *   are painted.
 *
 * Return the minimum total painting cost to achieve exactly target neighborhoods.
 * If it is impossible, return -1.
 *
 * Constraints:
 * - 1 <= n <= 100
 * - 1 <= m <= 20
 * - 1 <= target <= n
 * - shops[i] is 0 or an integer in the range [1, m]
 * - 1 <= cost[i][j] <= 10^4
 * - If shops[i] != 0, then cost values for that shop should be ignored
 *
 * This is a dynamic programming problem because the best choice for each shop depends on
 * the color chosen for the previous shop and how many neighborhoods have already been formed.
 */
public class Solution {

    /**
     * A large value used to represent an impossible state in dynamic programming.
     * We keep it safely below Integer.MAX_VALUE to avoid overflow when adding costs.
     */
    private static final int INF = 1_000_000_000;

    /**
     * Computes the minimum total cost to paint all shops so that exactly {@code target}
     * neighborhoods are formed.
     *
     * Dynamic Programming State:
     * dp[i][t][c] = minimum cost after processing the first i shops,
     *               forming exactly t neighborhoods,
     *               with the i-th shop painted color c.
     *
     * To save memory, we only keep the previous row and current row:
     * prev[t][c] and curr[t][c].
     *
     * Transition:
     * - If the current shop ends with color c:
     *   1) If previous shop also had color c, neighborhood count does not increase.
     *   2) If previous shop had a different color pc, neighborhood count increases by 1.
     *
     * Already painted shops:
     * - If shops[i] != 0, then only that fixed color is allowed and painting cost added is 0.
     *
     * Unpainted shops:
     * - We may choose any color c in [1..m], and add cost[i][c - 1].
     *
     * @param shops the array of shop colors, where 0 means unpainted and 1..m means already painted
     * @param cost the painting cost matrix; cost[i][c - 1] is the cost to paint shop i with color c
     * @param m the number of available colors
     * @param target the exact number of neighborhoods required
     * @return the minimum total painting cost, or -1 if it is impossible
     * Time complexity: O(n * target * m * m)
     * Space complexity: O(target * m)
     */
    public int minCost(int[] shops, int[][] cost, int m, int target) {
        int n = shops.length;

        // prev[t][c] means:
        // after processing shops up to the previous index,
        // minimum cost to have exactly t neighborhoods
        // and the previous shop painted color c.
        //
        // We use color indices 1..m for clarity.
        int[][] prev = new int[target + 1][m + 1];
        for (int t = 0; t <= target; t++) {
            Arrays.fill(prev[t], INF);
        }

        // Process shops one by one from left to right.
        for (int i = 0; i < n; i++) {
            // curr will store DP values for shop i.
            int[][] curr = new int[target + 1][m + 1];
            for (int t = 0; t <= target; t++) {
                Arrays.fill(curr[t], INF);
            }

            // Determine which colors are allowed for the current shop.
            // If already painted, only one color is possible.
            // If unpainted, all colors 1..m are possible.
            for (int color = 1; color <= m; color++) {
                // If the shop is already painted and its color is not 'color',
                // then this color choice is invalid.
                if (shops[i] != 0 && shops[i] != color) {
                    continue;
                }

                // Painting cost for current shop:
                // - 0 if already painted
                // - cost[i][color - 1] if unpainted
                int paintCost = (shops[i] == 0) ? cost[i][color - 1] : 0;

                // Base case for the very first shop:
                // If i == 0, choosing any valid color creates exactly 1 neighborhood.
                if (i == 0) {
                    curr[1][color] = Math.min(curr[1][color], paintCost);
                    continue;
                }

                // For all possible neighborhood counts up to target,
                // try to transition from every previous color.
                for (int neighborhoods = 1; neighborhoods <= target; neighborhoods++) {
                    // We want current shop to end with 'color'.
                    // There are two possibilities:
                    //
                    // 1) Previous shop had the same color:
                    //    neighborhood count stays the same.
                    //
                    // 2) Previous shop had a different color:
                    //    neighborhood count increases by 1.
                    for (int prevColor = 1; prevColor <= m; prevColor++) {
                        if (prevColor == color) {
                            // Same color as previous shop:
                            // neighborhood count does not change.
                            if (prev[neighborhoods][prevColor] != INF) {
                                curr[neighborhoods][color] = Math.min(
                                        curr[neighborhoods][color],
                                        prev[neighborhoods][prevColor] + paintCost
                                );
                            }
                        } else {
                            // Different color from previous shop:
                            // neighborhood count increases by 1.
                            if (neighborhoods > 1 && prev[neighborhoods - 1][prevColor] != INF) {
                                curr[neighborhoods][color] = Math.min(
                                        curr[neighborhoods][color],
                                        prev[neighborhoods - 1][prevColor] + paintCost
                                );
                            }
                        }
                    }
                }
            }

            // Move current row into prev for the next iteration.
            prev = curr;
        }

        // After processing all shops, answer is the minimum cost among all ending colors
        // that achieve exactly 'target' neighborhoods.
        int answer = INF;
        for (int color = 1; color <= m; color++) {
            answer = Math.min(answer, prev[target][color]);
        }

        return answer == INF ? -1 : answer;
    }

    /**
     * Convenience overload that infers the number of colors from the cost matrix.
     * This is useful for demonstrations and simple calls.
     *
     * @param shops the array of shop colors, where 0 means unpainted
     * @param cost the painting cost matrix
     * @param target the exact number of neighborhoods required
     * @return the minimum total painting cost, or -1 if impossible
     * Time complexity: O(n * target * m * m)
     * Space complexity: O(target * m)
     */
    public int minCost(int[] shops, int[][] cost, int target) {
        int m = cost[0].length;
        return minCost(shops, cost, m, target);
    }

    /**
     * Helper method to print a 1D integer array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Helper method to print a 2D integer array in a beginner-friendly format.
     *
     * @param matrix the matrix to print
     * @return a string representation of the matrix
     * Time complexity: O(r * c)
     * Space complexity: O(r * c) due to string construction
     */
    public String matrixToString(int[][] matrix) {
        return Arrays.deepToString(matrix);
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement.
     *
     * Note about Example 2:
     * The narrative in the prompt contains an inconsistency in the explanation and expected value.
     * This program computes the true minimum from the given input arrays.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstration size, excluding the DP calls
     * Space complexity: O(1), excluding the DP calls
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] shops1 = {0, 0, 0, 0};
        int[][] cost1 = {
                {1, 5},
                {4, 1},
                {1, 3},
                {2, 1}
        };
        int target1 = 2;

        System.out.println("Example 1");
        System.out.println("shops  = " + solution.arrayToString(shops1));
        System.out.println("cost   = " + solution.matrixToString(cost1));
        System.out.println("target = " + target1);
        System.out.println("Output = " + solution.minCost(shops1, cost1, target1));
        System.out.println();

        // Example 2
        int[] shops2 = {1, 0, 2, 0, 0};
        int[][] cost2 = {
                {3, 9, 4},
                {2, 1, 7},
                {8, 5, 6},
                {4, 3, 2},
                {7, 6, 1}
        };
        int target2 = 3;

        System.out.println("Example 2");
        System.out.println("shops  = " + solution.arrayToString(shops2));
        System.out.println("cost   = " + solution.matrixToString(cost2));
        System.out.println("target = " + target2);
        System.out.println("Output = " + solution.minCost(shops2, cost2, target2));
        System.out.println();

        // Additional quick sanity check:
        // Already painted shops, impossible target.
        int[] shops3 = {1, 1, 1};
        int[][] cost3 = {
                {5, 6},
                {7, 8},
                {9, 1}
        };
        int target3 = 2;

        System.out.println("Additional Example");
        System.out.println("shops  = " + solution.arrayToString(shops3));
        System.out.println("cost   = " + solution.matrixToString(cost3));
        System.out.println("target = " + target3);
        System.out.println("Output = " + solution.minCost(shops3, cost3, 2, target3));
    }
}