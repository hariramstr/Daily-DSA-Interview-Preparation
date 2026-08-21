import java.util.*;

/*
 * Title: Minimum Energy to Cross Paid Stepping Stones
 * Difficulty: Easy
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given an array cost where cost[i] is the energy required to land on stepping stone i.
 * A hiker wants to cross a small river by moving from left to right. From any stone, the hiker
 * may jump either 1 stone ahead or 2 stones ahead. The hiker may start on stone 0 or stone 1,
 * and the goal is to move beyond the last stone with the minimum total energy spent.
 *
 * A stone's energy cost is paid only when the hiker lands on that stone. Reaching the far bank
 * just past the last index does not cost anything. Your task is to return the minimum total
 * energy needed to cross the river.
 *
 * This is a dynamic programming problem because the cheapest way to reach a stone depends on
 * the cheapest ways to reach the previous one or two stones. An efficient solution should
 * compute the answer in linear time.
 *
 * Constraints:
 * - 2 <= cost.length <= 1000
 * - 0 <= cost[i] <= 999
 *
 * Example 1:
 * Input: cost = [4, 7, 2, 9]
 * Output: 6
 * Explanation: Start on stone 0 (pay 4), jump to stone 2 (pay 2), then jump beyond the last stone.
 * Total energy = 4 + 2 = 6.
 *
 * Example 2:
 * Input: cost = [1, 100, 1, 1, 100, 1]
 * Output: 3
 * Explanation: Start on stone 0, then land on stones 2, 3, and 5. The total is 1 + 1 + 1 = 3.
 * Other paths require more energy.
 *
 * Return the minimum energy required to reach the far bank.
 */

public class Solution {

    /**
     * Computes the minimum total energy required to move beyond the last stone.
     *
     * Dynamic programming idea:
     * - Let dp[i] represent the minimum energy needed to land on stone i.
     * - To land on stone i, the hiker must come from either:
     *   1) stone i - 1
     *   2) stone i - 2
     * - Since landing on stone i costs cost[i], we have:
     *   dp[i] = cost[i] + min(dp[i - 1], dp[i - 2])
     * - The hiker can start on stone 0 or stone 1, so:
     *   dp[0] = cost[0]
     *   dp[1] = cost[1]
     * - To reach the far bank (just beyond the last stone), the final move can come from:
     *   1) the last stone
     *   2) the second-to-last stone
     * - Therefore, the answer is:
     *   min(dp[n - 1], dp[n - 2])
     *
     * @param cost an array where cost[i] is the energy required to land on stone i
     * @return the minimum total energy needed to cross beyond the last stone
     * Time complexity: O(n), where n is the number of stones
     * Space complexity: O(1), because only the previous two DP states are stored
     */
    public int minCostClimbingStones(int[] cost) {
        // The problem guarantees at least 2 stones, but this defensive check
        // makes the method safer and easier for beginners to understand.
        if (cost == null || cost.length == 0) {
            return 0;
        }

        if (cost.length == 1) {
            return cost[0];
        }

        // prev2 will represent the minimum energy needed to land on stone i - 2.
        // Initially, when we are conceptually preparing to compute later stones,
        // the minimum energy to land on stone 0 is simply cost[0],
        // because starting on stone 0 means we pay its landing cost.
        int prev2 = cost[0];

        // prev1 will represent the minimum energy needed to land on stone i - 1.
        // Similarly, the minimum energy to land on stone 1 is cost[1],
        // because starting on stone 1 is also allowed.
        int prev1 = cost[1];

        // Now we compute the minimum energy for every stone from index 2 onward.
        for (int i = 2; i < cost.length; i++) {
            // To land on stone i:
            // - If we come from stone i - 1, total energy would be prev1 + cost[i]
            // - If we come from stone i - 2, total energy would be prev2 + cost[i]
            //
            // We choose the cheaper of those two possibilities.
            int current = cost[i] + Math.min(prev1, prev2);

            // Shift the window forward:
            // - The old prev1 becomes the new prev2
            // - The newly computed current becomes the new prev1
            prev2 = prev1;
            prev1 = current;
        }

        // To move beyond the last stone, the hiker does NOT pay any extra cost.
        // The final jump to the far bank can be made from:
        // - the last stone
        // - or the second-to-last stone
        //
        // So the answer is the cheaper of:
        // - minimum energy to land on the last stone
        // - minimum energy to land on the second-to-last stone
        return Math.min(prev1, prev2);
    }

    /**
     * A beginner-friendly alternative implementation using an explicit DP array.
     * This version is slightly more verbose but can be easier to visualize.
     *
     * @param cost an array where cost[i] is the energy required to land on stone i
     * @return the minimum total energy needed to cross beyond the last stone
     * Time complexity: O(n), where n is the number of stones
     * Space complexity: O(n), due to the DP array
     */
    public int minCostClimbingStonesWithArray(int[] cost) {
        if (cost == null || cost.length == 0) {
            return 0;
        }

        if (cost.length == 1) {
            return cost[0];
        }

        // dp[i] will store the minimum energy needed to land on stone i.
        int[] dp = new int[cost.length];

        // Base case 1:
        // If we start on stone 0, we pay cost[0].
        dp[0] = cost[0];

        // Base case 2:
        // If we start on stone 1, we pay cost[1].
        dp[1] = cost[1];

        // Fill the DP table from left to right.
        for (int i = 2; i < cost.length; i++) {
            // To land on stone i, we must come from i - 1 or i - 2.
            // We choose the cheaper path and then add the cost of landing on i.
            dp[i] = cost[i] + Math.min(dp[i - 1], dp[i - 2]);
        }

        // The far bank can be reached from either of the last two stones.
        return Math.min(dp[cost.length - 1], dp[cost.length - 2]);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo calls shown here
     * Space complexity: O(1), excluding the input arrays used for demonstration
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] cost1 = {4, 7, 2, 9};
        int result1 = solution.minCostClimbingStones(cost1);
        System.out.println("Input: " + Arrays.toString(cost1));
        System.out.println("Minimum energy: " + result1);
        System.out.println("Expected: 6");
        System.out.println();

        int[] cost2 = {1, 100, 1, 1, 100, 1};
        int result2 = solution.minCostClimbingStones(cost2);
        System.out.println("Input: " + Arrays.toString(cost2));
        System.out.println("Minimum energy: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        // Extra demonstration using the array-based DP version.
        int[] cost3 = {10, 15, 20};
        int result3 = solution.minCostClimbingStonesWithArray(cost3);
        System.out.println("Input: " + Arrays.toString(cost3));
        System.out.println("Minimum energy (array DP version): " + result3);
        System.out.println("Expected: 15");
    }
}