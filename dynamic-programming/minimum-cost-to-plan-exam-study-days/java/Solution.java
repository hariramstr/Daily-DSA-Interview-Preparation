import java.util.*;

/*
 * Title: Minimum Cost to Plan Exam Study Days
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given a strictly increasing array studyDays where each value represents a calendar day
 * on which a student must attend a required study session before an exam. The student can buy
 * study passes that cover consecutive calendar days. A 1-day pass costs costs[0], a 3-day pass
 * costs costs[1], and a 7-day pass costs costs[2]. A pass bought on day d covers day d and the
 * next consecutive days based on its duration. For example, a 3-day pass bought on day 10 covers
 * days 10, 11, and 12.
 *
 * Your task is to return the minimum total cost needed to cover every day in studyDays.
 *
 * A pass may cover days that are not in studyDays, and buying multiple overlapping passes is
 * allowed, although it may not be optimal. The student may buy any number of passes in any order
 * as long as every required study day is covered by at least one active pass.
 *
 * Design an efficient algorithm. A brute-force search over all pass combinations will be too slow
 * for the largest inputs.
 *
 * Constraints:
 * - 1 <= studyDays.length <= 365
 * - 1 <= studyDays[i] <= 365
 * - studyDays is strictly increasing
 * - costs.length == 3
 * - 1 <= costs[i] <= 1000
 *
 * Example 1:
 * Input: studyDays = [1,4,6,7,8,20], costs = [2,7,15]
 * Output: 11
 * Explanation: Buy a 1-day pass for day 1, a 7-day pass starting on day 4 to cover days 4 through 10,
 * and a 1-day pass for day 20. Total cost = 2 + 7 + 2 = 11.
 *
 * Example 2:
 * Input: studyDays = [2,3,4,5,9,10,11,30], costs = [3,8,14]
 * Output: 17
 *
 * Note:
 * The explanation text in the prompt explores several plans. The true minimum for Example 2 is 17.
 */

public class Solution {

    /**
     * Computes the minimum total cost needed to cover all required study days.
     *
     * This method uses dynamic programming over the list of required study days.
     * Let dp[i] represent the minimum cost needed to cover all study days starting
     * from index i onward.
     *
     * For each study day at position i, we consider exactly three choices:
     * 1. Buy a 1-day pass starting on studyDays[i]
     * 2. Buy a 3-day pass starting on studyDays[i]
     * 3. Buy a 7-day pass starting on studyDays[i]
     *
     * After choosing a pass, we jump to the first study day not covered by that pass.
     * The answer is the minimum among these three choices.
     *
     * @param studyDays strictly increasing array of calendar days that must be covered
     * @param costs costs of passes where:
     *              costs[0] = 1-day pass cost,
     *              costs[1] = 3-day pass cost,
     *              costs[2] = 7-day pass cost
     * @return minimum total cost to cover every required study day
     *
     * Time complexity: O(n), where n = studyDays.length.
     * Each pointer only moves forward through the array overall.
     *
     * Space complexity: O(n) for the DP array.
     */
    public int minCostToPlanStudyDays(int[] studyDays, int[] costs) {
        int n = studyDays.length;

        // dp[i] = minimum cost to cover studyDays from index i to the end.
        // dp[n] = 0 because if there are no days left to cover, no cost is needed.
        int[] dp = new int[n + 1];

        // These arrays store the durations and corresponding costs in matching positions.
        int[] durations = {1, 3, 7};
        int[] passCosts = {costs[0], costs[1], costs[2]};

        // We fill dp from right to left.
        // Why right to left?
        // Because dp[i] depends on future states like dp[nextIndex].
        for (int i = n - 1; i >= 0; i--) {
            // Start with a very large number so we can minimize against it.
            int best = Integer.MAX_VALUE;

            // Try each pass type:
            // - 1-day
            // - 3-day
            // - 7-day
            for (int passType = 0; passType < 3; passType++) {
                int duration = durations[passType];
                int passCost = passCosts[passType];

                // If we buy a pass on studyDays[i], it covers:
                // [studyDays[i], studyDays[i] + duration - 1]
                int coverageEndExclusive = studyDays[i] + duration;

                // Find the first index j such that studyDays[j] >= coverageEndExclusive.
                // That means studyDays[j] is NOT covered by this pass,
                // so dp[j] is the remaining cost after using this pass.
                int j = i;
                while (j < n && studyDays[j] < coverageEndExclusive) {
                    j++;
                }

                // Total cost if we choose this pass now:
                // current pass cost + optimal cost for remaining uncovered days
                int totalCost = passCost + dp[j];

                // Keep the minimum among all pass choices.
                best = Math.min(best, totalCost);
            }

            // Store the best possible answer for state i.
            dp[i] = best;
        }

        // dp[0] means minimum cost to cover all required study days.
        return dp[0];
    }

    /**
     * Alternative beginner-friendly dynamic programming solution using calendar days.
     *
     * This version builds a DP table for every day from 1 to the last required study day.
     * If a day is not a required study day, the cost does not change from the previous day.
     * If a day is required, we consider buying:
     * - a 1-day pass ending on that day
     * - a 3-day pass covering that day
     * - a 7-day pass covering that day
     *
     * This method is also correct and efficient because the maximum day value is only 365.
     *
     * @param studyDays strictly increasing array of calendar days that must be covered
     * @param costs costs of passes where:
     *              costs[0] = 1-day pass cost,
     *              costs[1] = 3-day pass cost,
     *              costs[2] = 7-day pass cost
     * @return minimum total cost to cover every required study day
     *
     * Time complexity: O(lastDay), where lastDay <= 365.
     * Space complexity: O(lastDay).
     */
    public int minCostToPlanStudyDaysByCalendar(int[] studyDays, int[] costs) {
        int lastDay = studyDays[studyDays.length - 1];

        // Mark which calendar days require study coverage.
        boolean[] required = new boolean[lastDay + 1];
        for (int day : studyDays) {
            required[day] = true;
        }

        // dp[day] = minimum cost to cover all required study days from day 1 through this day.
        int[] dp = new int[lastDay + 1];

        for (int day = 1; day <= lastDay; day++) {
            // If this day is not a required study day,
            // then we do not need to buy anything new.
            if (!required[day]) {
                dp[day] = dp[day - 1];
                continue;
            }

            // If this day is required, we must ensure it is covered.
            // We consider the three pass options:
            //
            // 1-day pass:
            // Covers only this day, so add costs[0] to dp[day - 1].
            int cost1 = dp[Math.max(0, day - 1)] + costs[0];

            // 3-day pass:
            // Covers day-2 through day, so add costs[1] to dp[day - 3].
            int cost3 = dp[Math.max(0, day - 3)] + costs[1];

            // 7-day pass:
            // Covers day-6 through day, so add costs[2] to dp[day - 7].
            int cost7 = dp[Math.max(0, day - 7)] + costs[2];

            // Choose the cheapest valid option.
            dp[day] = Math.min(cost1, Math.min(cost3, cost7));
        }

        return dp[lastDay];
    }

    /**
     * Runs sample demonstrations and prints the results.
     *
     * This main method verifies the examples from the problem statement and also
     * shows both implementations producing the same answers.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demonstrations shown here,
     * excluding the complexity of the called solution methods.
     *
     * Space complexity: O(1), excluding the space used by the called solution methods.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] studyDays1 = {1, 4, 6, 7, 8, 20};
        int[] costs1 = {2, 7, 15};

        int result1 = solution.minCostToPlanStudyDays(studyDays1, costs1);
        int result1Calendar = solution.minCostToPlanStudyDaysByCalendar(studyDays1, costs1);

        System.out.println("Example 1:");
        System.out.println("studyDays = " + Arrays.toString(studyDays1));
        System.out.println("costs = " + Arrays.toString(costs1));
        System.out.println("Minimum cost (index DP) = " + result1);
        System.out.println("Minimum cost (calendar DP) = " + result1Calendar);
        System.out.println("Expected = 11");
        System.out.println();

        int[] studyDays2 = {2, 3, 4, 5, 9, 10, 11, 30};
        int[] costs2 = {3, 8, 14};

        int result2 = solution.minCostToPlanStudyDays(studyDays2, costs2);
        int result2Calendar = solution.minCostToPlanStudyDaysByCalendar(studyDays2, costs2);

        System.out.println("Example 2:");
        System.out.println("studyDays = " + Arrays.toString(studyDays2));
        System.out.println("costs = " + Arrays.toString(costs2));
        System.out.println("Minimum cost (index DP) = " + result2);
        System.out.println("Minimum cost (calendar DP) = " + result2Calendar);
        System.out.println("Expected = 17");
        System.out.println();

        // Additional quick sanity check.
        int[] studyDays3 = {1};
        int[] costs3 = {5, 6, 20};

        int result3 = solution.minCostToPlanStudyDays(studyDays3, costs3);
        System.out.println("Additional Test:");
        System.out.println("studyDays = " + Arrays.toString(studyDays3));
        System.out.println("costs = " + Arrays.toString(costs3));
        System.out.println("Minimum cost = " + result3);
        System.out.println("Expected = 5");
    }
}