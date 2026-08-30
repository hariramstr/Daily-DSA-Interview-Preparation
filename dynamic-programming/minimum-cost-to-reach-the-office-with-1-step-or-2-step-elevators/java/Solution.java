/*
 * Title: Minimum Cost to Reach the Office With 1-Step or 2-Step Elevators
 * Difficulty: Easy
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are in a building lobby and want to reach the office on floor n.
 * The building has a special elevator system: from floor i, you may move either
 * 1 floor up or 2 floors up. However, each floor has an entry fee charged when
 * you land on it.
 *
 * You are given an integer array cost where cost[i] is the fee to land on floor i + 1.
 * Your goal is to reach exactly floor n while paying the minimum total fee.
 *
 * You start before floor 1, so no fee is paid at the beginning. If you jump directly
 * to a floor, you pay only for the floor where you land. For example, from the lobby
 * you may go to floor 1 and pay cost[0], or go directly to floor 2 and pay cost[1].
 *
 * Return the minimum total fee needed to reach floor n.
 *
 * Dynamic Programming Idea:
 * Let dp[i] represent the minimum cost to reach floor i (1-based floor number).
 * Then:
 *   dp[1] = cost[0]
 *   dp[2] = cost[1]
 *   dp[i] = min(dp[i - 1], dp[i - 2]) + cost[i - 1]   for i >= 3
 *
 * This works because to land on floor i, the last move must come from either:
 *   - floor i - 1 using a 1-floor move, or
 *   - floor i - 2 using a 2-floor move.
 *
 * We choose the cheaper of those two ways, then add the fee of landing on floor i.
 *
 * Constraints:
 * - 1 <= cost.length <= 1000
 * - 1 <= cost[i] <= 1000
 * - n = cost.length
 *
 * Example 1:
 * Input: cost = [4, 2, 7, 3]
 * Output: 5
 * Explanation:
 * One optimal path is lobby -> floor 2 -> floor 4.
 * Total cost = 2 + 3 = 5.
 *
 * Example 2:
 * Input: cost = [1, 100, 1, 1, 100, 1]
 * Correct Output: 4
 * Explanation:
 * A valid optimal path is lobby -> floor 1 -> floor 3 -> floor 4 -> floor 6
 * Total cost = 1 + 1 + 1 + 1 = 4.
 *
 * Note:
 * The original text around Example 2 contains contradictory reasoning, but the
 * correct minimum cost under the stated rules is 4, not 3.
 */

import java.util.*;

public class Solution {

    /**
     * Computes the minimum total fee required to reach exactly the top floor.
     *
     * <p>We use dynamic programming where dp[i] stores the minimum cost to land on
     * floor i (using 1-based floor numbering). Since the input array is 0-based,
     * the fee for floor i is cost[i - 1].</p>
     *
     * <p>Transition:
     * To reach floor i, the last move must be:
     * <ul>
     *   <li>from floor i - 1, or</li>
     *   <li>from floor i - 2</li>
     * </ul>
     * Therefore:
     * dp[i] = min(dp[i - 1], dp[i - 2]) + cost[i - 1]
     * </p>
     *
     * @param cost an array where cost[i] is the fee to land on floor i + 1
     * @return the minimum total fee needed to reach exactly floor n
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int minCostToReachOffice(int[] cost) {
        // Defensive handling:
        // The constraints guarantee at least one floor, but this makes the method
        // safer and easier for beginners to understand in general-purpose use.
        if (cost == null || cost.length == 0) {
            return 0;
        }

        // n is the total number of floors we must reach exactly.
        int n = cost.length;

        // Base case:
        // If there is only one floor, the only possible move is:
        // lobby -> floor 1
        // So we must pay the fee of floor 1.
        if (n == 1) {
            return cost[0];
        }

        // Create a DP array of size n + 1 so that we can directly use floor numbers
        // from 1 to n. We will ignore index 0 for simplicity.
        int[] dp = new int[n + 1];

        // Base initialization:
        // Minimum cost to reach floor 1 is simply paying cost[0].
        dp[1] = cost[0];

        // Minimum cost to reach floor 2 is simply paying cost[1], because we are
        // allowed to jump directly from the lobby to floor 2.
        dp[2] = cost[1];

        // Fill the DP table from floor 3 up to floor n.
        for (int floor = 3; floor <= n; floor++) {
            // Option 1:
            // Reach the current floor from the previous floor (floor - 1),
            // then pay the fee of the current floor.
            int fromPreviousFloor = dp[floor - 1];

            // Option 2:
            // Reach the current floor from two floors below (floor - 2),
            // then pay the fee of the current floor.
            int fromTwoFloorsBelow = dp[floor - 2];

            // We choose the cheaper of the two previous ways, because both are valid
            // ways to land on the current floor.
            int bestPreviousCost = Math.min(fromPreviousFloor, fromTwoFloorsBelow);

            // Add the fee for landing on the current floor.
            dp[floor] = bestPreviousCost + cost[floor - 1];
        }

        // The answer is the minimum cost to reach exactly floor n.
        return dp[n];
    }

    /**
     * Computes the minimum total fee required to reach exactly the top floor
     * using an optimized dynamic programming approach.
     *
     * <p>This version does not store the entire DP array. Instead, it only keeps
     * track of the last two DP values, because each state depends only on the
     * previous two states.</p>
     *
     * @param cost an array where cost[i] is the fee to land on floor i + 1
     * @return the minimum total fee needed to reach exactly floor n
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int minCostToReachOfficeOptimized(int[] cost) {
        // Again, handle edge cases safely.
        if (cost == null || cost.length == 0) {
            return 0;
        }

        int n = cost.length;

        // If there is only one floor, we must land there directly.
        if (n == 1) {
            return cost[0];
        }

        // prev2 represents dp[1]
        int prev2 = cost[0];

        // prev1 represents dp[2]
        int prev1 = cost[1];

        // Build the answer floor by floor, but only remember the last two results.
        for (int floor = 3; floor <= n; floor++) {
            // Current minimum cost to reach this floor:
            // choose the cheaper of the previous two reachable floors,
            // then add the fee of the current floor.
            int current = Math.min(prev1, prev2) + cost[floor - 1];

            // Shift the window forward:
            // old prev1 becomes new prev2,
            // current becomes new prev1.
            prev2 = prev1;
            prev1 = current;
        }

        // prev1 now stores dp[n].
        return prev1;
    }

    /**
     * Utility method to print an integer array in a beginner-friendly format.
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
     * Time complexity: O(1) for the fixed demo inputs, excluding algorithm calls
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1
        int[] cost1 = {4, 2, 7, 3};
        int result1 = solution.minCostToReachOffice(cost1);
        int result1Optimized = solution.minCostToReachOfficeOptimized(cost1);

        System.out.println("Example 1:");
        System.out.println("Input: cost = " + solution.arrayToString(cost1));
        System.out.println("Output (DP array): " + result1);
        System.out.println("Output (Optimized): " + result1Optimized);
        System.out.println("Expected: 5");
        System.out.println();

        // Sample input 2
        int[] cost2 = {1, 100, 1, 1, 100, 1};
        int result2 = solution.minCostToReachOffice(cost2);
        int result2Optimized = solution.minCostToReachOfficeOptimized(cost2);

        System.out.println("Example 2:");
        System.out.println("Input: cost = " + solution.arrayToString(cost2));
        System.out.println("Output (DP array): " + result2);
        System.out.println("Output (Optimized): " + result2Optimized);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional small test: single floor
        int[] cost3 = {8};
        int result3 = solution.minCostToReachOffice(cost3);
        int result3Optimized = solution.minCostToReachOfficeOptimized(cost3);

        System.out.println("Additional Test:");
        System.out.println("Input: cost = " + solution.arrayToString(cost3));
        System.out.println("Output (DP array): " + result3);
        System.out.println("Output (Optimized): " + result3Optimized);
        System.out.println("Expected: 8");
    }
}