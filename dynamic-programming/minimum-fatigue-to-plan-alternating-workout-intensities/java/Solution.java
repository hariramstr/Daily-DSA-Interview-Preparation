import java.util.*;

/*
 * Title: Minimum Fatigue to Plan Alternating Workout Intensities
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given a list of daily workout sessions. For day i, you may perform the session
 * in one of three intensity modes: low, medium, or high. The fatigue cost of choosing
 * each mode on that day is given by costs[i][0], costs[i][1], and costs[i][2] respectively.
 *
 * To avoid overtraining, your coach imposes a rule: you may not choose the same intensity
 * mode on two consecutive days. Your task is to compute the minimum total fatigue needed
 * to complete all sessions while following this rule.
 *
 * Return the minimum possible sum of fatigue costs over all days.
 *
 * This is a dynamic programming problem because the best choice for a day depends on the
 * mode chosen on the previous day. A greedy choice for the current day may lead to a worse
 * overall result later.
 *
 * Constraints:
 * - 1 <= n == costs.length <= 100000
 * - costs[i].length == 3
 * - 1 <= costs[i][j] <= 10^4
 * - The answer fits in a 32-bit signed integer.
 *
 * Notes about the examples:
 * The first example text contains contradictory arithmetic in the prompt. The correct
 * minimum for costs = [[5,3,4],[6,2,7],[3,8,1]] is 6:
 * - day 0: medium = 3
 * - day 1: high   = 7
 * - day 2: low    = 3
 * total = 13 (not minimum)
 *
 * Better:
 * - day 0: high   = 4
 * - day 1: medium = 2
 * - day 2: high   = 1
 * total = 7
 *
 * Best:
 * - day 0: medium = 3
 * - day 1: low    = 6
 * - day 2: high   = 1
 * total = 10
 *
 * Also:
 * - day 0: low    = 5
 * - day 1: medium = 2
 * - day 2: high   = 1
 * total = 8
 *
 * The actual optimal sequence is:
 * - day 0: high   = 4
 * - day 1: medium = 2
 * - day 2: low    = 3
 * total = 9
 *
 * But even better:
 * - day 0: medium = 3
 * - day 1: high   = 7
 * - day 2: low    = 3
 * total = 13
 *
 * Running the DP carefully gives:
 * day 0 dp = [5, 3, 4]
 * day 1 dp = [min(3,4)+6=9, min(5,4)+2=6, min(5,3)+7=10]
 * day 2 dp = [min(6,10)+3=9, min(9,10)+8=17, min(9,6)+1=7]
 * answer = min(9,17,7) = 7
 *
 * So the correct output for Example 1 is 7.
 *
 * Example 2:
 * Input: costs = [[1,10,100],[10,1,100],[100,10,1],[1,100,10]]
 * Output: 4
 * Explanation: One optimal plan is low, medium, high, low with total fatigue
 * 1 + 1 + 1 + 1 = 4.
 */

public class Solution {

    /**
     * Computes the minimum total fatigue using dynamic programming with O(1) extra space.
     *
     * The key idea:
     * For each day, we track three values:
     * - the minimum total fatigue if we choose low on this day
     * - the minimum total fatigue if we choose medium on this day
     * - the minimum total fatigue if we choose high on this day
     *
     * Since we cannot choose the same intensity on consecutive days:
     * - choosing low today means yesterday must have been medium or high
     * - choosing medium today means yesterday must have been low or high
     * - choosing high today means yesterday must have been low or medium
     *
     * Therefore:
     * newLow    = costs[i][0] + min(prevMedium, prevHigh)
     * newMedium = costs[i][1] + min(prevLow, prevHigh)
     * newHigh   = costs[i][2] + min(prevLow, prevMedium)
     *
     * We only need the previous day's three values, so we do not need a full DP table.
     *
     * @param costs a 2D array where costs[i][0], costs[i][1], and costs[i][2] are the fatigue
     *              costs for low, medium, and high intensity on day i
     * @return the minimum possible total fatigue while never choosing the same intensity
     *         on two consecutive days
     * Time complexity: O(n), where n is the number of days
     * Space complexity: O(1) extra space
     */
    public int minFatigue(int[][] costs) {
        if (costs == null || costs.length == 0) {
            return 0;
        }

        // For the very first day, the minimum total fatigue for each ending intensity
        // is simply the cost of choosing that intensity on day 0.
        int prevLow = costs[0][0];
        int prevMedium = costs[0][1];
        int prevHigh = costs[0][2];

        // Process each remaining day one by one.
        for (int day = 1; day < costs.length; day++) {
            // If we choose LOW today, yesterday cannot also be LOW.
            // So we must come from the cheaper of yesterday's MEDIUM or HIGH.
            int currentLow = costs[day][0] + Math.min(prevMedium, prevHigh);

            // If we choose MEDIUM today, yesterday cannot also be MEDIUM.
            // So we must come from the cheaper of yesterday's LOW or HIGH.
            int currentMedium = costs[day][1] + Math.min(prevLow, prevHigh);

            // If we choose HIGH today, yesterday cannot also be HIGH.
            // So we must come from the cheaper of yesterday's LOW or MEDIUM.
            int currentHigh = costs[day][2] + Math.min(prevLow, prevMedium);

            // Move the "current day" values into the "previous day" variables
            // so they can be used when processing the next day.
            prevLow = currentLow;
            prevMedium = currentMedium;
            prevHigh = currentHigh;
        }

        // After processing all days, the answer is the cheapest valid plan
        // among the three possible intensities chosen on the final day.
        return Math.min(prevLow, Math.min(prevMedium, prevHigh));
    }

    /**
     * Computes the minimum total fatigue using a full DP table.
     *
     * This version is more explicit and often easier for beginners to understand,
     * because it stores the answer for every day and every ending intensity.
     *
     * dp[i][0] = minimum total fatigue for days 0..i if day i uses low
     * dp[i][1] = minimum total fatigue for days 0..i if day i uses medium
     * dp[i][2] = minimum total fatigue for days 0..i if day i uses high
     *
     * Transition:
     * dp[i][0] = costs[i][0] + min(dp[i-1][1], dp[i-1][2])
     * dp[i][1] = costs[i][1] + min(dp[i-1][0], dp[i-1][2])
     * dp[i][2] = costs[i][2] + min(dp[i-1][0], dp[i-1][1])
     *
     * @param costs a 2D array where costs[i][0], costs[i][1], and costs[i][2] are the fatigue
     *              costs for low, medium, and high intensity on day i
     * @return the minimum possible total fatigue while never choosing the same intensity
     *         on two consecutive days
     * Time complexity: O(n), where n is the number of days
     * Space complexity: O(n), due to the DP table
     */
    public int minFatigueWithTable(int[][] costs) {
        if (costs == null || costs.length == 0) {
            return 0;
        }

        int n = costs.length;
        int[][] dp = new int[n][3];

        // Base case:
        // On day 0, the minimum cost for each intensity is just its direct cost.
        dp[0][0] = costs[0][0];
        dp[0][1] = costs[0][1];
        dp[0][2] = costs[0][2];

        // Fill the DP table from day 1 to day n - 1.
        for (int day = 1; day < n; day++) {
            dp[day][0] = costs[day][0] + Math.min(dp[day - 1][1], dp[day - 1][2]);
            dp[day][1] = costs[day][1] + Math.min(dp[day - 1][0], dp[day - 1][2]);
            dp[day][2] = costs[day][2] + Math.min(dp[day - 1][0], dp[day - 1][1]);
        }

        return Math.min(dp[n - 1][0], Math.min(dp[n - 1][1], dp[n - 1][2]));
    }

    /**
     * Utility method to print a 2D array in a readable format.
     *
     * @param costs the 2D array to print
     * @return a string representation of the 2D array
     * Time complexity: O(n)
     * Space complexity: O(n), for building the output string
     */
    public String arrayToString(int[][] costs) {
        return Arrays.deepToString(costs);
    }

    /**
     * Demonstrates the solution on sample inputs and a few extra checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo inputs shown here
     * Space complexity: O(1) extra space excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] example1 = {
            {5, 3, 4},
            {6, 2, 7},
            {3, 8, 1}
        };

        int[][] example2 = {
            {1, 10, 100},
            {10, 1, 100},
            {100, 10, 1},
            {1, 100, 10}
        };

        int[][] singleDay = {
            {9, 4, 7}
        };

        System.out.println("Example 1 costs: " + solution.arrayToString(example1));
        System.out.println("Minimum fatigue (optimized DP): " + solution.minFatigue(example1));
        System.out.println("Minimum fatigue (table DP): " + solution.minFatigueWithTable(example1));
        System.out.println("Expected correct result: 7");
        System.out.println();

        System.out.println("Example 2 costs: " + solution.arrayToString(example2));
        System.out.println("Minimum fatigue (optimized DP): " + solution.minFatigue(example2));
        System.out.println("Minimum fatigue (table DP): " + solution.minFatigueWithTable(example2));
        System.out.println("Expected result: 4");
        System.out.println();

        System.out.println("Single day costs: " + solution.arrayToString(singleDay));
        System.out.println("Minimum fatigue (optimized DP): " + solution.minFatigue(singleDay));
        System.out.println("Minimum fatigue (table DP): " + solution.minFatigueWithTable(singleDay));
        System.out.println("Expected result: 4");
    }
}