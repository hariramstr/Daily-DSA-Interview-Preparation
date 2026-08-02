import java.util.*;

/*
Problem Title: Minimum Cost to Schedule Workshops with Recovery Days

Problem Description:
You are organizing a training program over N calendar days. On day i, you may choose to run a workshop and earn value[i] participants, but running a workshop also increases fatigue. After holding a workshop on day i, you must leave the next cooldown[i] days empty as recovery days before scheduling another workshop. In other words, if you run a workshop on day i, the next workshop can be scheduled no earlier than day i + cooldown[i] + 1.

Each day also has a fixed operating cost cost[i] if you choose to run the workshop that day. Your goal is to reach at least target total participants while minimizing the total operating cost. You may skip any days, and you are not required to use all days. If it is impossible to reach at least target participants, return -1.

Design an algorithm to compute the minimum total cost.

Constraints:
- 1 <= N <= 200
- 1 <= target <= 5000
- 1 <= value[i] <= 100
- 1 <= cost[i] <= 1000
- 0 <= cooldown[i] < N

Example 1:
Input: value = [6,4,7,3], cost = [5,2,6,2], cooldown = [1,0,2,0], target = 10
Output: 8

Explanation:
- Day 0 and day 3 gives 6 + 3 = 9, not enough.
- Day 1 and day 2 is valid because cooldown[1] = 0, so day 2 is allowed. That gives 4 + 7 = 11 for cost 2 + 6 = 8.
- Day 0 and day 2 is also valid because cooldown[0] = 1 blocks only day 1, so day 2 is allowed. That gives 13 for cost 11.
- The minimum valid cost to reach at least 10 is 8.

Example 2:
Input: value = [5,8,4], cost = [4,9,3], cooldown = [2,1,0], target = 13
Output: -1

Explanation:
- If you choose day 0, then days 1 and 2 must be skipped, so total participants is only 5.
- If you choose day 1, then day 2 must be skipped because cooldown[1] = 1.
- Day 1 and day 2 together would reach 12, not 13 anyway, and that pair is invalid.
- Therefore, it is impossible to reach 13, so the answer is -1.
*/

public class Solution {

    /**
     * Computes the minimum total operating cost needed to reach at least the target
     * number of participants while respecting cooldown constraints between chosen days.
     *
     * Dynamic programming idea:
     * dp[i][p] = minimum cost to achieve exactly p participants (capped at target)
     *            considering only days from index i to the end.
     *
     * At each day i, we have two choices:
     * 1. Skip day i:
     *    move to day i + 1 with the same participant count requirement.
     * 2. Take day i:
     *    gain value[i] participants, pay cost[i], and jump to
     *    next day = i + cooldown[i] + 1.
     *
     * We build the solution bottom-up from the end of the calendar toward the front.
     *
     * @param value the participants gained if a workshop is run on each day
     * @param cost the operating cost if a workshop is run on each day
     * @param cooldown the number of recovery days that must be skipped after each chosen day
     * @param target the minimum total participants required
     * @return the minimum total cost to reach at least target participants, or -1 if impossible
     * Time complexity: O(N * target)
     * Space complexity: O(N * target)
     */
    public int minCostToReachTarget(int[] value, int[] cost, int[] cooldown, int target) {
        validateInput(value, cost, cooldown, target);

        int n = value.length;

        // We use a large number to represent an unreachable state.
        // It must be safely larger than any possible valid answer.
        final int INF = 1_000_000_000;

        // dp[i][p] means:
        // minimum cost to reach at least "p" participants starting from day i.
        //
        // However, to keep transitions simple and beginner-friendly, we instead define:
        // dp[i][p] = minimum cost to collect exactly p participants from days i..n-1,
        // where p is capped at target.
        //
        // Since any amount above target is equivalent to target for our goal,
        // whenever we add participants we clamp the result using Math.min(target, ...).
        int[][] dp = new int[n + 1][target + 1];

        // Initialize all states as unreachable first.
        for (int i = 0; i <= n; i++) {
            Arrays.fill(dp[i], INF);
        }

        // Base case:
        // When we are past the last day (i == n):
        // - Achieving 0 participants costs 0 (choose nothing).
        // - Achieving any positive participants is impossible.
        dp[n][0] = 0;

        // Fill the DP table from the last day back to the first day.
        for (int i = n - 1; i >= 0; i--) {
            // For every currently accumulated participant amount p
            // that we want to achieve from day i onward:
            for (int p = 0; p <= target; p++) {

                // Option 1: Skip this day.
                // If we skip day i, then the best cost is simply whatever it costs
                // to achieve p participants starting from day i + 1.
                int best = dp[i + 1][p];

                // Option 2: Take this day.
                // If we run a workshop on day i:
                // - we gain value[i] participants
                // - we pay cost[i]
                // - we must jump to the first allowed next day after cooldown
                int nextDay = i + cooldown[i] + 1;

                // Clamp nextDay to n so that we do not go out of bounds.
                if (nextDay > n) {
                    nextDay = n;
                }

                // We want total participants to become:
                // current p participants from future days + value[i] from today.
                // Since states are capped at target, anything above target becomes target.
                int newParticipants = Math.min(target, p + value[i]);

                // If dp[nextDay][p] is reachable, then taking day i can produce
                // newParticipants with additional cost[i].
                if (dp[nextDay][p] != INF) {
                    best = Math.min(best, dp[nextDay][p] + cost[i]);
                }

                // Store the best of skipping or taking.
                dp[i][newParticipants] = Math.min(dp[i][newParticipants], best);
            }

            // The above loop directly updates dp[i][newParticipants], but to ensure
            // all states are correctly propagated, we also perform a more explicit
            // transition pass below. This makes the logic easier to follow and robust.

            // Reset the row and rebuild it clearly from transitions.
            Arrays.fill(dp[i], INF);

            // Transition 1: skip day i
            for (int participants = 0; participants <= target; participants++) {
                dp[i][participants] = Math.min(dp[i][participants], dp[i + 1][participants]);
            }

            // Transition 2: take day i
            int nextDayForTake = i + cooldown[i] + 1;
            if (nextDayForTake > n) {
                nextDayForTake = n;
            }

            for (int participantsFromFuture = 0; participantsFromFuture <= target; participantsFromFuture++) {
                if (dp[nextDayForTake][participantsFromFuture] == INF) {
                    continue;
                }

                int totalParticipants = Math.min(target, participantsFromFuture + value[i]);
                int totalCost = dp[nextDayForTake][participantsFromFuture] + cost[i];

                dp[i][totalParticipants] = Math.min(dp[i][totalParticipants], totalCost);
            }
        }

        // We need at least target participants.
        // Because we cap all larger values to target, dp[0][target] is the answer.
        return dp[0][target] == INF ? -1 : dp[0][target];
    }

    /**
     * Validates the input arrays and target value.
     *
     * @param value the participants gained per chosen day
     * @param cost the operating cost per chosen day
     * @param cooldown the cooldown days required after each chosen day
     * @param target the required minimum participants
     * @return nothing; throws an exception if input is invalid
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void validateInput(int[] value, int[] cost, int[] cooldown, int target) {
        if (value == null || cost == null || cooldown == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }
        if (value.length != cost.length || value.length != cooldown.length) {
            throw new IllegalArgumentException("All input arrays must have the same length.");
        }
        if (value.length == 0) {
            throw new IllegalArgumentException("Input arrays must not be empty.");
        }
        if (target <= 0) {
            throw new IllegalArgumentException("Target must be positive.");
        }
    }

    /**
     * Helper method to print an integer array in a readable format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(N)
     * Space complexity: O(N)
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called DP method
     * Space complexity: O(1), excluding the called DP method
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] value1 = {6, 4, 7, 3};
        int[] cost1 = {5, 2, 6, 2};
        int[] cooldown1 = {1, 0, 2, 0};
        int target1 = 10;

        int result1 = solution.minCostToReachTarget(value1, cost1, cooldown1, target1);
        System.out.println("Sample 1:");
        System.out.println("value = " + solution.arrayToString(value1));
        System.out.println("cost = " + solution.arrayToString(cost1));
        System.out.println("cooldown = " + solution.arrayToString(cooldown1));
        System.out.println("target = " + target1);
        System.out.println("Minimum cost = " + result1);
        System.out.println("Expected = 8");
        System.out.println();

        // Sample 2
        int[] value2 = {5, 8, 4};
        int[] cost2 = {4, 9, 3};
        int[] cooldown2 = {2, 1, 0};
        int target2 = 13;

        int result2 = solution.minCostToReachTarget(value2, cost2, cooldown2, target2);
        System.out.println("Sample 2:");
        System.out.println("value = " + solution.arrayToString(value2));
        System.out.println("cost = " + solution.arrayToString(cost2));
        System.out.println("cooldown = " + solution.arrayToString(cooldown2));
        System.out.println("target = " + target2);
        System.out.println("Minimum cost = " + result2);
        System.out.println("Expected = -1");
        System.out.println();

        // Additional demonstration based on the note in Example 2:
        // If target were 8, choosing day 1 alone gives 8 participants for cost 9.
        int target3 = 8;
        int result3 = solution.minCostToReachTarget(value2, cost2, cooldown2, target3);
        System.out.println("Additional Demo:");
        System.out.println("value = " + solution.arrayToString(value2));
        System.out.println("cost = " + solution.arrayToString(cost2));
        System.out.println("cooldown = " + solution.arrayToString(cooldown2));
        System.out.println("target = " + target3);
        System.out.println("Minimum cost = " + result3);
        System.out.println("Expected = 9");
    }
}