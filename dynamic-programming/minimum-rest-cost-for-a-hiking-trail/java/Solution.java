import java.util.*;

/*
 * Title: Minimum Rest Cost for a Hiking Trail
 * Difficulty: Easy
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are planning a hike along a straight trail that is divided into numbered checkpoints
 * from 0 to n - 1. You start at checkpoint 0 and want to reach checkpoint n - 1.
 * From any checkpoint i, you may move either 1 step forward to i + 1 or 2 steps forward
 * to i + 2, as long as that checkpoint exists.
 *
 * Each checkpoint has a rest cost given by an integer array cost, where cost[i] is the
 * energy cost of stopping at checkpoint i. If you land on a checkpoint during your hike,
 * you must pay its rest cost. Your goal is to reach the final checkpoint with the minimum
 * total cost.
 *
 * Return the minimum total rest cost needed to reach checkpoint n - 1.
 *
 * This is a classic dynamic programming problem because the cheapest way to reach a checkpoint
 * depends only on the cheapest ways to reach the previous one or two checkpoints.
 * A simple linear-time solution is expected.
 *
 * Constraints:
 * - 1 <= n == cost.length <= 1000
 * - 0 <= cost[i] <= 1000
 * - If n == 1, you are already at the final checkpoint, so the answer is cost[0]
 *
 * Example 1:
 * Input: cost = [4, 2, 7, 3]
 * Output: 9
 * Explanation:
 * - reach 0: 4
 * - reach 1: 4 + 2 = 6
 * - reach 2: min(4, 6) + 7 = 11
 * - reach 3: min(6, 11) + 3 = 9
 *
 * Example 2:
 * Input: cost = [1, 100, 1, 1, 100, 1]
 * Output: 4
 * Explanation:
 * An optimal path is 0 -> 2 -> 3 -> 5.
 * Total cost = 1 + 1 + 1 + 1 = 4
 */

public class Solution {

    /**
     * Computes the minimum total rest cost needed to reach the final checkpoint.
     *
     * Dynamic Programming Idea:
     * Let dp[i] represent the minimum total cost required to land on checkpoint i.
     * Since you can arrive at checkpoint i only from:
     * - checkpoint i - 1
     * - checkpoint i - 2
     *
     * the recurrence is:
     * dp[i] = min(dp[i - 1], dp[i - 2]) + cost[i]
     *
     * Base cases:
     * - dp[0] = cost[0], because you start at checkpoint 0 and must pay its cost
     * - dp[1] = dp[0] + cost[1], because from checkpoint 0 you can move to checkpoint 1
     *
     * @param cost an array where cost[i] is the rest cost of checkpoint i
     * @return the minimum total rest cost to reach checkpoint cost.length - 1
     * Time complexity: O(n), where n is the number of checkpoints
     * Space complexity: O(n), for the DP array
     */
    public int minRestCost(int[] cost) {
        // Defensive handling for null or empty input.
        // The problem constraints guarantee at least one element,
        // but this makes the method safer and more beginner-friendly.
        if (cost == null || cost.length == 0) {
            return 0;
        }

        int n = cost.length;

        // If there is only one checkpoint, we start there and it is also the destination.
        // Therefore, the total cost is simply the cost of checkpoint 0.
        if (n == 1) {
            return cost[0];
        }

        // Create a DP array where:
        // dp[i] = minimum total cost required to reach checkpoint i.
        int[] dp = new int[n];

        // Base case 1:
        // We start at checkpoint 0, so we must pay cost[0].
        dp[0] = cost[0];

        // Base case 2:
        // To reach checkpoint 1, we come from checkpoint 0.
        // So total cost is the cost to reach 0 plus cost[1].
        dp[1] = dp[0] + cost[1];

        // Fill the DP table from left to right.
        // For every checkpoint i >= 2:
        // - We could have come from i - 1
        // - Or we could have come from i - 2
        // We choose the cheaper of those two ways, then add the current checkpoint's cost.
        for (int i = 2; i < n; i++) {
            dp[i] = Math.min(dp[i - 1], dp[i - 2]) + cost[i];
        }

        // The answer is the minimum total cost to reach the final checkpoint.
        return dp[n - 1];
    }

    /**
     * Computes the minimum total rest cost needed to reach the final checkpoint
     * using an optimized dynamic programming approach with constant extra space.
     *
     * Instead of storing the entire DP array, we only keep track of:
     * - the minimum cost to reach the previous checkpoint
     * - the minimum cost to reach the checkpoint before that
     *
     * This works because each state depends only on the previous two states.
     *
     * @param cost an array where cost[i] is the rest cost of checkpoint i
     * @return the minimum total rest cost to reach checkpoint cost.length - 1
     * Time complexity: O(n), where n is the number of checkpoints
     * Space complexity: O(1), ignoring the input array
     */
    public int minRestCostOptimized(int[] cost) {
        // Again, handle unexpected input safely.
        if (cost == null || cost.length == 0) {
            return 0;
        }

        int n = cost.length;

        // If there is only one checkpoint, that checkpoint is both start and finish.
        if (n == 1) {
            return cost[0];
        }

        // prev2 represents dp[i - 2]
        // Initially, for i = 2, dp[0] = cost[0]
        int prev2 = cost[0];

        // prev1 represents dp[i - 1]
        // Initially, for i = 2, dp[1] = cost[0] + cost[1]
        int prev1 = cost[0] + cost[1];

        // Process checkpoints from index 2 onward.
        for (int i = 2; i < n; i++) {
            // Current minimum cost to reach checkpoint i:
            // choose the cheaper path between coming from i - 1 or i - 2,
            // then add the cost of landing on checkpoint i.
            int current = Math.min(prev1, prev2) + cost[i];

            // Shift the window forward:
            // old prev1 becomes new prev2,
            // current becomes new prev1.
            prev2 = prev1;
            prev1 = current;
        }

        // prev1 now stores the minimum cost to reach the last checkpoint.
        return prev1;
    }

    /**
     * Helper method to print an integer array in a readable format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution with sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo cases shown here
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1
        int[] cost1 = {4, 2, 7, 3};
        int result1 = solution.minRestCost(cost1);
        System.out.println("Input: " + solution.arrayToString(cost1));
        System.out.println("Minimum rest cost: " + result1);
        System.out.println("Expected: 9");
        System.out.println();

        // Sample input 2
        int[] cost2 = {1, 100, 1, 1, 100, 1};
        int result2 = solution.minRestCost(cost2);
        System.out.println("Input: " + solution.arrayToString(cost2));
        System.out.println("Minimum rest cost: " + result2);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional small edge case: single checkpoint
        int[] cost3 = {5};
        int result3 = solution.minRestCost(cost3);
        System.out.println("Input: " + solution.arrayToString(cost3));
        System.out.println("Minimum rest cost: " + result3);
        System.out.println("Expected: 5");
        System.out.println();

        // Show that the optimized version produces the same results.
        System.out.println("Optimized version checks:");
        System.out.println("For " + solution.arrayToString(cost1) + " -> " + solution.minRestCostOptimized(cost1));
        System.out.println("For " + solution.arrayToString(cost2) + " -> " + solution.minRestCostOptimized(cost2));
        System.out.println("For " + solution.arrayToString(cost3) + " -> " + solution.minRestCostOptimized(cost3));
    }
}