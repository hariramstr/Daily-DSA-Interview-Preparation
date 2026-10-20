import java.util.*;

/*
 * Title: Minimum Cost to Plan Cafeteria Meals with No Same Dish on Consecutive Days
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A company cafeteria offers exactly three meal options each workday: Pasta, Salad, and Soup.
 * You are given a 2D array costs where costs[i][j] is the cost of choosing meal j on day i,
 * with j = 0 for Pasta, j = 1 for Salad, and j = 2 for Soup.
 *
 * You must choose exactly one meal for each day. However, to keep the menu varied, you are not
 * allowed to choose the same meal on two consecutive days.
 *
 * Your task is to compute the minimum total cost needed to plan meals for all days while
 * satisfying the no-consecutive-repeat rule.
 *
 * Return the minimum possible total cost.
 *
 * This problem is intended to test dynamic programming fundamentals: making an optimal choice
 * for the current day based on the best valid choices from previous days.
 *
 * Constraints:
 * - 1 <= costs.length <= 100000
 * - costs[i].length == 3
 * - 1 <= costs[i][j] <= 10000
 * - You must choose one meal every day
 *
 * Example 1:
 * Input: costs = [[5,9,4],[8,7,6],[3,5,10]]
 * Output: 14
 * Explanation: Choose Soup on day 0 (4), Salad on day 1 (7), and Pasta on day 2 (3). Total = 14.
 *
 * Example 2:
 * Input: costs = [[7,2,8],[6,4,3],[5,9,1],[2,7,6]]
 * Correct Output: 11
 * Explanation: One optimal plan is Salad on day 0 (2), Pasta on day 1 (6), Soup on day 2 (1),
 * Pasta on day 3 (2), for a total cost of 11.
 */

public class Solution {

    /**
     * Computes the minimum total meal cost using dynamic programming with O(1) extra space.
     *
     * Core idea:
     * For each day, we track the minimum total cost if we end that day by choosing:
     * - Pasta
     * - Salad
     * - Soup
     *
     * Since the same meal cannot be chosen on consecutive days:
     * - If we choose Pasta today, yesterday must have been Salad or Soup
     * - If we choose Salad today, yesterday must have been Pasta or Soup
     * - If we choose Soup today, yesterday must have been Pasta or Salad
     *
     * Therefore:
     * newPasta = costs[i][0] + min(previousSalad, previousSoup)
     * newSalad = costs[i][1] + min(previousPasta, previousSoup)
     * newSoup  = costs[i][2] + min(previousPasta, previousSalad)
     *
     * We only need the previous day's three values, so we do not need a full DP table.
     *
     * @param costs a 2D array where costs[i][0] is Pasta cost on day i,
     *              costs[i][1] is Salad cost on day i,
     *              and costs[i][2] is Soup cost on day i
     * @return the minimum possible total cost to plan meals for all days without choosing
     *         the same meal on consecutive days
     * Time complexity: O(n), where n is the number of days
     * Space complexity: O(1) extra space, excluding input storage
     */
    public int minCost(int[][] costs) {
        // Defensive handling for unexpected null or empty input.
        // The problem guarantees at least one day, but beginner-friendly code often
        // includes a safe guard for robustness.
        if (costs == null || costs.length == 0) {
            return 0;
        }

        // For day 0, the minimum cost of ending with each meal is simply the direct cost
        // of choosing that meal on the first day, because there is no previous day.
        int pasta = costs[0][0];
        int salad = costs[0][1];
        int soup = costs[0][2];

        // Process each remaining day one by one.
        for (int day = 1; day < costs.length; day++) {
            // If we choose Pasta today, yesterday cannot also be Pasta.
            // So we must come from the cheaper of:
            // - ending yesterday with Salad
            // - ending yesterday with Soup
            int newPasta = costs[day][0] + Math.min(salad, soup);

            // If we choose Salad today, yesterday must be Pasta or Soup.
            int newSalad = costs[day][1] + Math.min(pasta, soup);

            // If we choose Soup today, yesterday must be Pasta or Salad.
            int newSoup = costs[day][2] + Math.min(pasta, salad);

            // Move the "current day" values into our rolling variables.
            pasta = newPasta;
            salad = newSalad;
            soup = newSoup;
        }

        // On the final day, the answer is the cheapest valid total among the three
        // possible meals chosen on that last day.
        return Math.min(pasta, Math.min(salad, soup));
    }

    /**
     * Computes the minimum total meal cost using a full DP table.
     *
     * This version is easier for beginners to visualize because dp[i][j] explicitly stores:
     * the minimum total cost for days 0..i if day i uses meal j.
     *
     * Meal mapping:
     * - 0 = Pasta
     * - 1 = Salad
     * - 2 = Soup
     *
     * Transition:
     * dp[i][0] = costs[i][0] + min(dp[i-1][1], dp[i-1][2])
     * dp[i][1] = costs[i][1] + min(dp[i-1][0], dp[i-1][2])
     * dp[i][2] = costs[i][2] + min(dp[i-1][0], dp[i-1][1])
     *
     * @param costs a 2D array of daily meal costs
     * @return the minimum possible total cost to plan meals for all days
     * Time complexity: O(n), where n is the number of days
     * Space complexity: O(n), due to the DP table
     */
    public int minCostWithTable(int[][] costs) {
        if (costs == null || costs.length == 0) {
            return 0;
        }

        int n = costs.length;
        int[][] dp = new int[n][3];

        // Base case: first day costs are copied directly.
        dp[0][0] = costs[0][0];
        dp[0][1] = costs[0][1];
        dp[0][2] = costs[0][2];

        // Fill the DP table day by day.
        for (int day = 1; day < n; day++) {
            dp[day][0] = costs[day][0] + Math.min(dp[day - 1][1], dp[day - 1][2]);
            dp[day][1] = costs[day][1] + Math.min(dp[day - 1][0], dp[day - 1][2]);
            dp[day][2] = costs[day][2] + Math.min(dp[day - 1][0], dp[day - 1][1]);
        }

        return Math.min(dp[n - 1][0], Math.min(dp[n - 1][1], dp[n - 1][2]));
    }

    /**
     * Converts a 2D int array into a readable string for printing.
     *
     * @param array the 2D array to format
     * @return a human-readable string representation of the 2D array
     * Time complexity: O(n), where n is the total number of integers in the array
     * Space complexity: O(n), for the generated string content
     */
    public static String arrayToString(int[][] array) {
        return Arrays.deepToString(array);
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement and prints results.
     *
     * This main method also verifies the outputs against the expected answers:
     * - Example 1 should be 14
     * - Example 2 should be 11
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) overall for the demonstrated examples
     * Space complexity: O(1) extra space excluding sample input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] costs1 = {
            {5, 9, 4},
            {8, 7, 6},
            {3, 5, 10}
        };

        int[][] costs2 = {
            {7, 2, 8},
            {6, 4, 3},
            {5, 9, 1},
            {2, 7, 6}
        };

        int result1 = solution.minCost(costs1);
        int result2 = solution.minCost(costs2);

        System.out.println("Example 1 input: " + arrayToString(costs1));
        System.out.println("Minimum total cost: " + result1);
        System.out.println("Expected: 14");
        System.out.println();

        System.out.println("Example 2 input: " + arrayToString(costs2));
        System.out.println("Minimum total cost: " + result2);
        System.out.println("Expected: 11");
        System.out.println();

        // Additional small sanity check: single day case.
        int[][] costs3 = {
            {10, 3, 7}
        };
        int result3 = solution.minCost(costs3);
        System.out.println("Single day input: " + arrayToString(costs3));
        System.out.println("Minimum total cost: " + result3);
        System.out.println("Expected: 3");
    }
}