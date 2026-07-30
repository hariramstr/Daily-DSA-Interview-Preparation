import java.util.*;

/*
 * Title: Minimum Fee to Cover Streaming Event Days
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A media platform plans to broadcast a set of live events on specific calendar days.
 * To handle traffic, the platform can purchase server reservation passes of different durations.
 * A 1-day pass costs cost1, a 7-day pass costs cost7, and a 30-day pass costs cost30.
 * A pass purchased for day d covers day d and the next consecutive days within its duration.
 * For example, a 7-day pass bought on day 10 covers days 10 through 16 inclusive.
 *
 * You are given a strictly increasing integer array days, where days[i] is a day on which
 * at least one live event must be supported, and an array costs of length 3 where
 * costs = [cost1, cost7, cost30].
 *
 * Return the minimum total fee required to cover every event day in days.
 *
 * You may buy any number of passes, and passes may overlap, but overlapping coverage does not
 * provide any extra benefit beyond covering the required days. The goal is to choose passes so
 * that every day in days is covered at minimum total cost.
 *
 * Constraints:
 * - 1 <= days.length <= 365
 * - 1 <= days[i] <= 365
 * - days is strictly increasing
 * - 1 <= costs[i] <= 1000
 *
 * Example 1:
 * Input: days = [1,4,6,7,8,20], costs = [2,7,15]
 * Output: 11
 *
 * Example 2:
 * Input: days = [2,3,4,5,6,7,8,9,15,16,17,40], costs = [3,8,20]
 * Output: 19
 *
 * Note:
 * If a provided explanation elsewhere appears inconsistent, the algorithm below computes
 * the true minimum cost by dynamic programming.
 */

public class Solution {

    /**
     * Computes the minimum total fee required to cover all event days.
     *
     * This method uses dynamic programming over the list of required event days.
     * For each event index i, we compute the minimum cost needed to cover all event days
     * starting from index i to the end.
     *
     * At each position, we try three choices:
     * 1. Buy a 1-day pass starting on days[i]
     * 2. Buy a 7-day pass starting on days[i]
     * 3. Buy a 30-day pass starting on days[i]
     *
     * Then we jump to the first event day not covered by that pass and continue optimally.
     *
     * @param days strictly increasing array of event days that must be covered
     * @param costs array of length 3 where:
     *              costs[0] = cost of 1-day pass,
     *              costs[1] = cost of 7-day pass,
     *              costs[2] = cost of 30-day pass
     * @return the minimum total fee required to cover every event day
     * Time complexity: O(n), where n = days.length, because each pointer only moves forward overall
     * Space complexity: O(n) for the DP array
     */
    public int mincostTickets(int[] days, int[] costs) {
        int n = days.length;

        // dp[i] will store the minimum cost needed to cover all required event days
        // starting from index i.
        //
        // Meaning:
        // - dp[0] = answer for the whole problem
        // - dp[n] = 0 because if there are no days left to cover, cost is zero
        int[] dp = new int[n + 1];

        // These pointers help us efficiently find the next uncovered event day
        // after buying a 7-day or 30-day pass starting at days[i].
        //
        // We will maintain them while iterating from right to left.
        // However, because right-to-left pointer maintenance is awkward for forward coverage,
        // we compute the next indices directly with while loops from each i.
        //
        // Since n <= 365, even a simple scan is fully acceptable and efficient.
        for (int i = n - 1; i >= 0; i--) {
            // Option 1: Buy a 1-day pass on days[i].
            //
            // A 1-day pass covers only days[i].
            // So the next event day we still need to cover is at index i + 1.
            int costWith1DayPass = costs[0] + dp[i + 1];

            // Option 2: Buy a 7-day pass on days[i].
            //
            // This pass covers all event days with value < days[i] + 7,
            // because it covers days[i] through days[i] + 6 inclusive.
            int j = i;
            while (j < n && days[j] < days[i] + 7) {
                j++;
            }
            int costWith7DayPass = costs[1] + dp[j];

            // Option 3: Buy a 30-day pass on days[i].
            //
            // This pass covers all event days with value < days[i] + 30,
            // because it covers days[i] through days[i] + 29 inclusive.
            int k = i;
            while (k < n && days[k] < days[i] + 30) {
                k++;
            }
            int costWith30DayPass = costs[2] + dp[k];

            // The optimal answer from index i is the cheapest among the three choices.
            dp[i] = Math.min(costWith1DayPass, Math.min(costWith7DayPass, costWith30DayPass));
        }

        return dp[0];
    }

    /**
     * Alternative beginner-friendly dynamic programming solution using calendar days.
     *
     * This version builds a DP table from day 1 up to the last required event day.
     * If a day is not an event day, the cost stays the same as the previous day.
     * If it is an event day, we consider buying:
     * - a 1-day pass ending on this day,
     * - a 7-day pass covering this day,
     * - a 30-day pass covering this day.
     *
     * This method is also correct and very intuitive.
     *
     * @param days strictly increasing array of event days that must be covered
     * @param costs array of pass costs: 1-day, 7-day, and 30-day
     * @return the minimum total fee required to cover every event day
     * Time complexity: O(lastDay), where lastDay <= 365
     * Space complexity: O(lastDay)
     */
    public int mincostTicketsByCalendarDay(int[] days, int[] costs) {
        int lastDay = days[days.length - 1];

        // travelDay[d] tells us whether day d is a required event day.
        boolean[] eventDay = new boolean[lastDay + 1];
        for (int day : days) {
            eventDay[day] = true;
        }

        // dp[d] = minimum cost to cover all required event days from day 1 through day d.
        int[] dp = new int[lastDay + 1];

        for (int day = 1; day <= lastDay; day++) {
            // If this is not an event day, we do not need to buy anything new.
            // So the cost is exactly the same as the previous day.
            if (!eventDay[day]) {
                dp[day] = dp[day - 1];
                continue;
            }

            // If this IS an event day, then we must ensure it is covered.
            //
            // Choice 1: Buy a 1-day pass that covers this day.
            int oneDayCost = dp[Math.max(0, day - 1)] + costs[0];

            // Choice 2: Buy a 7-day pass that covers this day.
            int sevenDayCost = dp[Math.max(0, day - 7)] + costs[1];

            // Choice 3: Buy a 30-day pass that covers this day.
            int thirtyDayCost = dp[Math.max(0, day - 30)] + costs[2];

            // Take the cheapest valid option.
            dp[day] = Math.min(oneDayCost, Math.min(sevenDayCost, thirtyDayCost));
        }

        return dp[lastDay];
    }

    /**
     * Runs sample demonstrations from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstrations shown here
     * Space complexity: O(1) excluding the arrays created for examples
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] days1 = {1, 4, 6, 7, 8, 20};
        int[] costs1 = {2, 7, 15};
        int result1 = solution.mincostTickets(days1, costs1);
        System.out.println("Example 1 Result: " + result1);
        System.out.println("Expected: 11");

        int[] days2 = {2, 3, 4, 5, 6, 7, 8, 9, 15, 16, 17, 40};
        int[] costs2 = {3, 8, 20};
        int result2 = solution.mincostTickets(days2, costs2);
        System.out.println("Example 2 Result: " + result2);
        System.out.println("Expected: 19");

        // Also demonstrate the alternative calendar-day DP method to show both methods agree.
        int result1Alt = solution.mincostTicketsByCalendarDay(days1, costs1);
        int result2Alt = solution.mincostTicketsByCalendarDay(days2, costs2);

        System.out.println("Example 1 Result (Calendar DP): " + result1Alt);
        System.out.println("Example 2 Result (Calendar DP): " + result2Alt);
    }
}