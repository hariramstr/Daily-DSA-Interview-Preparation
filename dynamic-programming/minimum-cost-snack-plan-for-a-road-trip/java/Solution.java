/*
Minimum Cost Snack Plan for a Road Trip

Problem Description:
You are planning snacks for a road trip that lasts n days. On each day, you must buy exactly one snack pack.
There are three snack types: fruit, sandwich, and trail mix. The cost of buying each type can be different on each day.

However, to avoid getting bored, you are not allowed to buy the same snack type on two consecutive days.

You are given a 2D integer array costs of size n x 3, where:
- costs[i][0] is the cost of buying fruit on day i
- costs[i][1] is the cost of buying a sandwich on day i
- costs[i][2] is the cost of buying trail mix on day i

Return the minimum total cost to buy snacks for all n days while following the rule that
the same snack type cannot be chosen on consecutive days.

This is a classic dynamic programming problem:
For each day and each snack type, we compute the minimum total cost to reach that day
if we choose that snack type on the current day.

Since the same snack cannot be chosen on consecutive days:
- If we choose fruit today, yesterday must have been sandwich or trail mix
- If we choose sandwich today, yesterday must have been fruit or trail mix
- If we choose trail mix today, yesterday must have been fruit or sandwich

Constraints:
- 1 <= n <= 1000
- costs.length == n
- costs[i].length == 3
- 1 <= costs[i][j] <= 1000
*/

import java.util.*;

public class Solution {

    /**
     * Computes the minimum total snack cost using dynamic programming with O(1) extra space.
     *
     * The idea:
     * - Track the minimum total cost up to the previous day for each possible snack choice.
     * - For the current day:
     *   - If we buy fruit today, yesterday must have been sandwich or trail mix.
     *   - If we buy sandwich today, yesterday must have been fruit or trail mix.
     *   - If we buy trail mix today, yesterday must have been fruit or sandwich.
     * - Update these three running values day by day.
     *
     * @param costs a 2D array where costs[i][0], costs[i][1], and costs[i][2] represent
     *              the cost of fruit, sandwich, and trail mix on day i
     * @return the minimum total cost to buy exactly one snack per day without choosing
     *         the same snack type on consecutive days
     *
     * Time complexity: O(n), where n is the number of days
     * Space complexity: O(1), because only a constant amount of extra space is used
     */
    public int minCost(int[][] costs) {
        // Defensive check:
        // If the input is null or empty, there are no days to process.
        // Although the constraints guarantee at least 1 day, this makes the method safer.
        if (costs == null || costs.length == 0) {
            return 0;
        }

        // For day 0, the minimum cost of ending with each snack is simply the direct cost on day 0,
        // because there is no previous day and therefore no adjacency restriction yet.
        int fruit = costs[0][0];
        int sandwich = costs[0][1];
        int trailMix = costs[0][2];

        // Process each remaining day one by one.
        for (int day = 1; day < costs.length; day++) {
            // If we choose fruit today:
            // Yesterday cannot also be fruit.
            // So we must come from the cheaper of:
            // - ending yesterday with sandwich
            // - ending yesterday with trail mix
            int newFruit = costs[day][0] + Math.min(sandwich, trailMix);

            // If we choose sandwich today:
            // Yesterday cannot also be sandwich.
            // So we must come from the cheaper of:
            // - ending yesterday with fruit
            // - ending yesterday with trail mix
            int newSandwich = costs[day][1] + Math.min(fruit, trailMix);

            // If we choose trail mix today:
            // Yesterday cannot also be trail mix.
            // So we must come from the cheaper of:
            // - ending yesterday with fruit
            // - ending yesterday with sandwich
            int newTrailMix = costs[day][2] + Math.min(fruit, sandwich);

            // Move the "current day" values into our running state,
            // so they become the "previous day" values for the next iteration.
            fruit = newFruit;
            sandwich = newSandwich;
            trailMix = newTrailMix;
        }

        // After processing all days, the answer is the cheapest way to end on the last day,
        // regardless of which snack type was chosen on that last day.
        return Math.min(fruit, Math.min(sandwich, trailMix));
    }

    /**
     * Computes the minimum total snack cost using a full DP table.
     *
     * This version is more explicit and beginner-friendly because it stores the answer for every day
     * and every snack type:
     * - dp[i][0] = minimum total cost up to day i if day i uses fruit
     * - dp[i][1] = minimum total cost up to day i if day i uses sandwich
     * - dp[i][2] = minimum total cost up to day i if day i uses trail mix
     *
     * Transition rules:
     * - dp[i][0] = costs[i][0] + min(dp[i-1][1], dp[i-1][2])
     * - dp[i][1] = costs[i][1] + min(dp[i-1][0], dp[i-1][2])
     * - dp[i][2] = costs[i][2] + min(dp[i-1][0], dp[i-1][1])
     *
     * @param costs a 2D array where costs[i][0], costs[i][1], and costs[i][2] represent
     *              the cost of fruit, sandwich, and trail mix on day i
     * @return the minimum total cost to buy exactly one snack per day without choosing
     *         the same snack type on consecutive days
     *
     * Time complexity: O(n), where n is the number of days
     * Space complexity: O(n), due to the DP table
     */
    public int minCostWithTable(int[][] costs) {
        if (costs == null || costs.length == 0) {
            return 0;
        }

        int n = costs.length;

        // Create a DP table with n rows and 3 columns.
        int[][] dp = new int[n][3];

        // Base case:
        // On the first day, the minimum cost of choosing each snack is just its direct cost.
        dp[0][0] = costs[0][0];
        dp[0][1] = costs[0][1];
        dp[0][2] = costs[0][2];

        // Fill the table from day 1 to day n-1.
        for (int day = 1; day < n; day++) {
            // Choose fruit today -> yesterday must be sandwich or trail mix
            dp[day][0] = costs[day][0] + Math.min(dp[day - 1][1], dp[day - 1][2]);

            // Choose sandwich today -> yesterday must be fruit or trail mix
            dp[day][1] = costs[day][1] + Math.min(dp[day - 1][0], dp[day - 1][2]);

            // Choose trail mix today -> yesterday must be fruit or sandwich
            dp[day][2] = costs[day][2] + Math.min(dp[day - 1][0], dp[day - 1][1]);
        }

        // The final answer is the minimum among the three possibilities on the last day.
        return Math.min(dp[n - 1][0], Math.min(dp[n - 1][1], dp[n - 1][2]));
    }

    /**
     * Utility method to print a 2D array in a readable format.
     *
     * @param costs the 2D costs array to print
     * @return a string representation of the 2D array
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[][] costs) {
        return Arrays.deepToString(costs);
    }

    /**
     * Demonstrates the solution on sample inputs and a few additional checks.
     *
     * Important note about Example 1 from the prompt:
     * If we correctly apply the dynamic programming rules to:
     * [[4,2,7],[3,6,1],[8,5,4]]
     * the true minimum is 8, not 7.
     *
     * Valid plans:
     * - Day0 fruit(4), day1 sandwich(6), day2 fruit(8) = 18
     * - Day0 fruit(4), day1 sandwich(6), day2 trail(4) = 14
     * - Day0 fruit(4), day1 trail(1), day2 fruit(8) = 13
     * - Day0 fruit(4), day1 trail(1), day2 sandwich(5) = 10
     * - Day0 sandwich(2), day1 fruit(3), day2 sandwich(5) = 10
     * - Day0 sandwich(2), day1 fruit(3), day2 trail(4) = 9
     * - Day0 sandwich(2), day1 trail(1), day2 fruit(8) = 11
     * - Day0 sandwich(2), day1 trail(1), day2 sandwich(5) is invalid
     * - Day0 trail(7), day1 fruit(3), day2 sandwich(5) = 15
     * - Day0 trail(7), day1 fruit(3), day2 trail(4) = 14
     * - Day0 trail(7), day1 sandwich(6), day2 fruit(8) = 21
     * - Day0 trail(7), day1 sandwich(6), day2 trail(4) = 17
     *
     * The best valid total is 9? Let's verify carefully:
     * - sandwich(2), fruit(3), trail(4) = 9
     * - fruit(4), trail(1), sandwich(5) = 10
     * So the true minimum is actually 9.
     *
     * Therefore, the prompt's Example 1 output is inconsistent with the stated rules.
     * The algorithm below is correct for the problem definition.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(1) for the fixed demonstrations shown here
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] costs1 = {
            {4, 2, 7},
            {3, 6, 1},
            {8, 5, 4}
        };

        int[][] costs2 = {
            {1, 10, 3},
            {10, 2, 8},
            {4, 7, 1},
            {6, 3, 9}
        };

        System.out.println("Example 1 costs: " + solution.arrayToString(costs1));
        System.out.println("Minimum cost (optimized DP): " + solution.minCost(costs1));
        System.out.println("Minimum cost (table DP): " + solution.minCostWithTable(costs1));
        System.out.println();

        System.out.println("Example 2 costs: " + solution.arrayToString(costs2));
        System.out.println("Minimum cost (optimized DP): " + solution.minCost(costs2));
        System.out.println("Minimum cost (table DP): " + solution.minCostWithTable(costs2));
        System.out.println();

        int[][] singleDay = {
            {5, 1, 9}
        };

        System.out.println("Single day costs: " + solution.arrayToString(singleDay));
        System.out.println("Minimum cost (optimized DP): " + solution.minCost(singleDay));
        System.out.println("Minimum cost (table DP): " + solution.minCostWithTable(singleDay));
    }
}