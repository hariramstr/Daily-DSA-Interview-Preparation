import java.util.*;

/*
 * Title: Minimum Energy to Cross a Toll Bridge Path
 * Difficulty: Easy
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A courier robot needs to cross a sequence of bridge sections to deliver a package.
 * The path is represented by an array cost, where cost[i] is the energy required to
 * land on section i. The robot may start before the first section, and on each move
 * it can jump either 1 section or 2 sections forward. The robot reaches the destination
 * when it moves past the last section. Your task is to compute the minimum total energy
 * needed to reach the destination.
 *
 * If the robot lands on a section, it must pay that section's energy cost exactly once.
 * Since it can jump over sections, skipped sections do not add any cost. This makes the
 * problem well suited for dynamic programming: the cheapest way to reach a position depends
 * on the cheapest ways to reach the previous one or two positions.
 *
 * Return the minimum energy required to cross the full path.
 *
 * Constraints:
 * - 2 <= cost.length <= 1000
 * - 0 <= cost[i] <= 999
 *
 * Example 1:
 * Input: cost = [4, 2, 7, 3]
 * Output: 5
 * Explanation:
 * Start before the path, land on section 1 (cost 2), then section 3 (cost 3),
 * then move past the end. Total = 2 + 3 = 5.
 *
 * Example 2:
 * Input: cost = [1, 100, 1, 1, 100, 1]
 * Output: 3
 * Explanation:
 * One optimal route is to land on sections 0, 2, 3, and 5. Total energy = 4,
 * but a better route is to land on sections 0, 2, and 5 for a total of 3.
 */

public class Solution {

    /**
     * Computes the minimum energy required to move past the last section.
     *
     * The robot may begin before index 0 and can first land on either:
     * - section 0
     * - section 1
     *
     * After that, from any section i, it may jump to:
     * - i + 1
     * - i + 2
     *
     * Dynamic programming idea:
     * Let dp[i] represent the minimum total energy needed to land on section i.
     * Then:
     * dp[i] = cost[i] + min(dp[i - 1], dp[i - 2])
     *
     * Why this works:
     * To land on section i, the robot must come from either section i - 1 or i - 2.
     * We choose the cheaper of those two ways, then add the cost of landing on i.
     *
     * Final answer:
     * The destination is beyond the last section, so the robot can finish from either:
     * - the last section
     * - the second-to-last section
     * Therefore, answer = min(dp[n - 1], dp[n - 2])
     *
     * @param cost an array where cost[i] is the energy required to land on section i
     * @return the minimum total energy required to move past the last section
     * Time complexity: O(n), because we process each section exactly once
     * Space complexity: O(1), because we keep only the last two DP states
     */
    public int minCostClimbingStairs(int[] cost) {
        // Defensive handling:
        // The problem guarantees at least 2 elements, but this check makes the method safer
        // and easier for beginners to understand if reused elsewhere.
        if (cost == null || cost.length == 0) {
            return 0;
        }

        if (cost.length == 1) {
            return cost[0];
        }

        // prev2 will represent the minimum cost to land on section i - 2
        // At the beginning, for i = 2:
        // prev2 corresponds to dp[0], which is simply cost[0]
        int prev2 = cost[0];

        // prev1 will represent the minimum cost to land on section i - 1
        // At the beginning, for i = 2:
        // prev1 corresponds to dp[1], which is simply cost[1]
        int prev1 = cost[1];

        // We now compute the minimum cost to land on each section from index 2 onward.
        for (int i = 2; i < cost.length; i++) {
            // To land on section i, the robot must come from:
            // - section i - 1, whose best cost is prev1
            // - section i - 2, whose best cost is prev2
            //
            // We choose the cheaper of those two previous landing states,
            // then add the energy cost of landing on the current section i.
            int current = cost[i] + Math.min(prev1, prev2);

            // Shift the window forward:
            // The old prev1 becomes the new prev2,
            // and current becomes the new prev1.
            prev2 = prev1;
            prev1 = current;
        }

        // The robot does not need to pay any cost for the destination itself,
        // because the destination is just beyond the last section.
        //
        // So the robot can finish by jumping from:
        // - the last section, with total cost prev1
        // - the second-to-last section, with total cost prev2
        //
        // We return the cheaper of these two possibilities.
        return Math.min(prev1, prev2);
    }

    /**
     * Computes the minimum energy required to move past the last section
     * using an explicit DP array for educational clarity.
     *
     * This version is often easier for beginners to understand because it stores
     * the best answer for every section.
     *
     * @param cost an array where cost[i] is the energy required to land on section i
     * @return the minimum total energy required to move past the last section
     * Time complexity: O(n), because we fill the DP array once
     * Space complexity: O(n), because we store one DP value per section
     */
    public int minCostClimbingStairsWithDpArray(int[] cost) {
        // Again, these checks are defensive and beginner-friendly.
        if (cost == null || cost.length == 0) {
            return 0;
        }

        if (cost.length == 1) {
            return cost[0];
        }

        int n = cost.length;

        // dp[i] will store the minimum total energy needed to land on section i.
        int[] dp = new int[n];

        // Base case 1:
        // To land on section 0, the robot can start before the path and jump directly to 0.
        // So the total cost is simply cost[0].
        dp[0] = cost[0];

        // Base case 2:
        // To land on section 1, the robot can also start before the path and jump directly to 1.
        // So the total cost is simply cost[1].
        dp[1] = cost[1];

        // Fill the DP array from left to right.
        for (int i = 2; i < n; i++) {
            // To land on section i, the robot must come from i - 1 or i - 2.
            // We take the cheaper of those two best-known costs and add cost[i].
            dp[i] = cost[i] + Math.min(dp[i - 1], dp[i - 2]);
        }

        // The robot reaches the destination by stepping beyond the last section.
        // It can do that from either the last or second-to-last section.
        return Math.min(dp[n - 1], dp[n - 2]);
    }

    /**
     * Demonstrates the solution with the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments, not used in this program
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called methods
     * Space complexity: O(1), excluding the called methods
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1
        int[] cost1 = {4, 2, 7, 3};
        int result1 = solution.minCostClimbingStairs(cost1);
        System.out.println("Input: " + Arrays.toString(cost1));
        System.out.println("Minimum energy: " + result1);
        System.out.println("Expected: 5");
        System.out.println();

        // Sample input 2
        int[] cost2 = {1, 100, 1, 1, 100, 1};
        int result2 = solution.minCostClimbingStairs(cost2);
        System.out.println("Input: " + Arrays.toString(cost2));
        System.out.println("Minimum energy: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        // Additional demonstration using the DP-array version on the same inputs
        int result1Dp = solution.minCostClimbingStairsWithDpArray(cost1);
        int result2Dp = solution.minCostClimbingStairsWithDpArray(cost2);

        System.out.println("Using DP array version:");
        System.out.println("Input: " + Arrays.toString(cost1) + " -> " + result1Dp);
        System.out.println("Input: " + Arrays.toString(cost2) + " -> " + result2Dp);
    }
}