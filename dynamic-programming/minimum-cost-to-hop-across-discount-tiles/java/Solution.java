/*
 * Title: Minimum Cost to Hop Across Discount Tiles
 * Difficulty: Easy
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given an array cost where cost[i] is the fee to land on tile i in a hallway.
 * A player starts before the first tile and wants to move past the last tile.
 * On each move, the player may hop forward by either 1 tile or 2 tiles.
 * Whenever the player lands on a tile, they must pay that tile's fee.
 * The goal is to reach beyond the last tile with the minimum total cost.
 *
 * There is one small twist: because of a promotional rule, the player is allowed
 * to start by landing on either tile 0 or tile 1 without paying any cost before that move.
 * After that, every landed tile must be paid normally. Return the minimum total fee
 * needed to move beyond the last tile.
 *
 * This is a dynamic programming problem because the cheapest way to reach a position
 * depends on the cheapest ways to reach the previous one or two positions.
 *
 * Constraints:
 * - 2 <= cost.length <= 1000
 * - 0 <= cost[i] <= 999
 *
 * Example 1:
 * Input: cost = [4, 2, 7, 3]
 * Output: 5
 * Explanation:
 * Start on tile 1 (pay 2), hop to tile 3 (pay 3), then move beyond the last tile.
 * Total cost = 2 + 3 = 5.
 *
 * Example 2:
 * Input: cost = [1, 100, 1, 1, 100, 1]
 * Output: 3
 * Explanation:
 * One optimal route is:
 * start on tile 0 (pay 1), hop to tile 2 (pay 1), hop to tile 3 (pay 1),
 * hop to tile 5 (pay 1), then move beyond the last tile, for total 4.
 * But an even better route is:
 * start on tile 0 (pay 1), hop to tile 2 (pay 1), hop to tile 5 (pay 1),
 * then move beyond the last tile, for total 3.
 */

import java.util.*;

public class Solution {

    /**
     * Computes the minimum total cost required to move beyond the last tile.
     *
     * Dynamic programming idea:
     * Let dp[i] represent the minimum cost needed to reach "position i",
     * where position i means:
     * - i = 0: before tile 0
     * - i = 1: before tile 1 / allowed starting point
     * - ...
     * - i = n: beyond the last tile
     *
     * To reach position i, the last move must have come from:
     * - position i - 1, landing on tile i - 1 and paying cost[i - 1]
     * - position i - 2, landing on tile i - 2 and paying cost[i - 2]
     *
     * Therefore:
     * dp[i] = min(
     *     dp[i - 1] + cost[i - 1],
     *     dp[i - 2] + cost[i - 2]
     * )
     *
     * Base cases:
     * dp[0] = 0
     * dp[1] = 0
     *
     * These base cases correctly model the promotional rule:
     * the player may start by landing on tile 0 or tile 1.
     *
     * @param cost an array where cost[i] is the fee paid when landing on tile i
     * @return the minimum total fee needed to move beyond the last tile
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int minCostClimbingStairs(int[] cost) {
        int n = cost.length;

        // dp[i] will store the minimum cost needed to reach position i.
        // Position n means we have moved beyond the last tile.
        int[] dp = new int[n + 1];

        // Base case:
        // Reaching position 0 costs nothing because we start before the hallway.
        dp[0] = 0;

        // Base case:
        // Reaching position 1 also costs nothing because the rules allow us
        // to start by landing on tile 0 or tile 1.
        dp[1] = 0;

        // We now fill the DP table from left to right.
        // Each position depends only on the previous two positions.
        for (int i = 2; i <= n; i++) {
            // Option 1:
            // Come from position i - 1, then land on tile i - 1 and pay its cost.
            int oneStep = dp[i - 1] + cost[i - 1];

            // Option 2:
            // Come from position i - 2, then land on tile i - 2 and pay its cost.
            int twoSteps = dp[i - 2] + cost[i - 2];

            // Choose the cheaper of the two possible ways.
            dp[i] = Math.min(oneStep, twoSteps);
        }

        // dp[n] is the minimum cost to move beyond the last tile.
        return dp[n];
    }

    /**
     * Computes the minimum total cost required to move beyond the last tile
     * using an optimized dynamic programming approach with constant extra space.
     *
     * This method is logically identical to the full DP solution, but instead of
     * storing the entire dp array, it keeps only the last two DP values because
     * each new state depends only on the previous two states.
     *
     * @param cost an array where cost[i] is the fee paid when landing on tile i
     * @return the minimum total fee needed to move beyond the last tile
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int minCostClimbingStairsOptimized(int[] cost) {
        int n = cost.length;

        // prev2 represents dp[i - 2]
        // Initially, for i = 2:
        // dp[0] = 0
        int prev2 = 0;

        // prev1 represents dp[i - 1]
        // Initially, for i = 2:
        // dp[1] = 0
        int prev1 = 0;

        // Build the answer iteratively from position 2 up to position n.
        for (int i = 2; i <= n; i++) {
            // Cost if we arrive from one position back.
            int oneStep = prev1 + cost[i - 1];

            // Cost if we arrive from two positions back.
            int twoSteps = prev2 + cost[i - 2];

            // Current DP value.
            int current = Math.min(oneStep, twoSteps);

            // Shift the window forward:
            // old prev1 becomes new prev2
            // current becomes new prev1
            prev2 = prev1;
            prev1 = current;
        }

        // prev1 now holds dp[n].
        return prev1;
    }

    /**
     * Helper method to print an integer array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstration case
     * Space complexity: O(1) extra for the optimized method call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] cost1 = {4, 2, 7, 3};
        int result1 = solution.minCostClimbingStairs(cost1);
        int result1Optimized = solution.minCostClimbingStairsOptimized(cost1);

        System.out.println("Example 1:");
        System.out.println("Input: cost = " + solution.arrayToString(cost1));
        System.out.println("Output (DP array): " + result1);
        System.out.println("Output (Optimized): " + result1Optimized);
        System.out.println("Expected: 5");
        System.out.println();

        int[] cost2 = {1, 100, 1, 1, 100, 1};
        int result2 = solution.minCostClimbingStairs(cost2);
        int result2Optimized = solution.minCostClimbingStairsOptimized(cost2);

        System.out.println("Example 2:");
        System.out.println("Input: cost = " + solution.arrayToString(cost2));
        System.out.println("Output (DP array): " + result2);
        System.out.println("Output (Optimized): " + result2Optimized);
        System.out.println("Expected: 3");
        System.out.println();

        int[] extra = {10, 15, 20};
        int extraResult = solution.minCostClimbingStairs(extra);
        int extraResultOptimized = solution.minCostClimbingStairsOptimized(extra);

        System.out.println("Extra Example:");
        System.out.println("Input: cost = " + solution.arrayToString(extra));
        System.out.println("Output (DP array): " + extraResult);
        System.out.println("Output (Optimized): " + extraResultOptimized);
        System.out.println("Expected: 15");
    }
}