import java.util.*;

/*
Problem Title: Maximum Insight from Scheduling Research Experiments

Problem Description:
A research lab has planned n experiments over the next several days. Experiment i must be started on or before day deadline[i],
requires exactly duration[i] consecutive days to complete, and yields insight[i] points if it is fully completed by its deadline.
Only one experiment can run on any given day, and once an experiment starts, it cannot be interrupted.

You may choose any subset of experiments and schedule them in any order, as long as every chosen experiment finishes no later than
its own deadline. Your task is to compute the maximum total insight that can be obtained.

Unlike simple interval scheduling, each experiment can be reordered relative to the others, and feasibility depends on the total
occupied time before each chosen deadline. This makes greedy choices insufficient in many cases.

Return the maximum possible sum of insight points.

Constraints:
- 1 <= n <= 200
- 1 <= duration[i] <= 200
- 1 <= deadline[i] <= 2000
- 1 <= insight[i] <= 10^6
- The answer fits in a 64-bit signed integer.

Key idea:
Sort experiments by deadline, then use dynamic programming over total used time.
Let dp[t] = maximum total insight achievable by selecting some subset of the processed experiments
such that the total scheduled time is exactly t and every selected experiment can be completed by its own deadline.

When processing an experiment (duration d, deadline ddl, insight val), we may place it as the last experiment
among the selected processed experiments. This is valid only if the new finishing time t is <= ddl.
Thus:
dp[t] = max(dp[t], dp[t - d] + val) for all t from ddl down to d.

This is a classic "schedule jobs before deadlines with profits" dynamic programming formulation.
*/
public class Solution {

    /**
     * Simple container for one experiment.
     */
    private static class Experiment {
        int duration;
        int deadline;
        long insight;

        Experiment(int duration, int deadline, long insight) {
            this.duration = duration;
            this.deadline = deadline;
            this.insight = insight;
        }
    }

    /**
     * Computes the maximum total insight obtainable by selecting and scheduling a subset of experiments.
     *
     * Algorithm:
     * 1. Build experiment objects from the three input arrays.
     * 2. Sort experiments by nondecreasing deadline.
     * 3. Use 1D dynamic programming where dp[t] means:
     *    "maximum insight achievable using exactly t total days after considering some prefix of experiments,
     *     while keeping the schedule feasible."
     * 4. For each experiment, iterate time backwards so each experiment is used at most once.
     * 5. Only allow transitions where the finishing time does not exceed the current experiment's deadline.
     *
     * Why sorting by deadline works:
     * If we consider chosen experiments in nondecreasing deadline order, then checking that each newly added
     * experiment finishes by its own deadline is sufficient to maintain global feasibility.
     *
     * @param duration array where duration[i] is the number of consecutive days required by experiment i
     * @param deadline array where deadline[i] is the latest day by which experiment i must be finished
     * @param insight array where insight[i] is the insight gained if experiment i is completed on time
     * @return the maximum total insight achievable as a long
     *
     * Time complexity: O(n * D), where D is the maximum deadline (at most 2000)
     * Space complexity: O(D)
     */
    public long maximumInsight(int[] duration, int[] deadline, int[] insight) {
        int n = duration.length;

        // Build a list of experiments so we can sort them by deadline.
        Experiment[] experiments = new Experiment[n];
        int maxDeadline = 0;
        for (int i = 0; i < n; i++) {
            experiments[i] = new Experiment(duration[i], deadline[i], insight[i]);
            maxDeadline = Math.max(maxDeadline, deadline[i]);
        }

        // Sort by deadline ascending.
        // This is essential because the DP relies on considering experiments in deadline order.
        Arrays.sort(experiments, Comparator.comparingInt(e -> e.deadline));

        // We use a very negative value to represent "unreachable state".
        // Since insight values are positive, any valid state will be >= 0.
        long NEG = Long.MIN_VALUE / 4;

        // dp[t] = maximum insight achievable using exactly t days.
        long[] dp = new long[maxDeadline + 1];
        Arrays.fill(dp, NEG);
        dp[0] = 0L; // Using 0 days yields 0 insight.

        // Process each experiment one by one.
        for (Experiment exp : experiments) {
            int d = exp.duration;
            int ddl = exp.deadline;
            long val = exp.insight;

            // Iterate backwards to ensure each experiment is chosen at most once.
            // We only consider finishing times t up to this experiment's deadline.
            for (int t = ddl; t >= d; t--) {
                // If state t - d is reachable, then we can append this experiment last
                // and finish at time t.
                if (dp[t - d] != NEG) {
                    dp[t] = Math.max(dp[t], dp[t - d] + val);
                }
            }
        }

        // The answer is the best reachable value over all total times.
        long answer = 0L;
        for (long value : dp) {
            answer = Math.max(answer, value);
        }
        return answer;
    }

    /**
     * Convenience overload that accepts insight as long[].
     * This can be useful if a caller already stores values in 64-bit form.
     *
     * @param duration array where duration[i] is the number of consecutive days required by experiment i
     * @param deadline array where deadline[i] is the latest day by which experiment i must be finished
     * @param insight array where insight[i] is the insight gained if experiment i is completed on time
     * @return the maximum total insight achievable as a long
     *
     * Time complexity: O(n * D), where D is the maximum deadline
     * Space complexity: O(D)
     */
    public long maximumInsight(int[] duration, int[] deadline, long[] insight) {
        int n = duration.length;

        Experiment[] experiments = new Experiment[n];
        int maxDeadline = 0;
        for (int i = 0; i < n; i++) {
            experiments[i] = new Experiment(duration[i], deadline[i], insight[i]);
            maxDeadline = Math.max(maxDeadline, deadline[i]);
        }

        Arrays.sort(experiments, Comparator.comparingInt(e -> e.deadline));

        long NEG = Long.MIN_VALUE / 4;
        long[] dp = new long[maxDeadline + 1];
        Arrays.fill(dp, NEG);
        dp[0] = 0L;

        for (Experiment exp : experiments) {
            int d = exp.duration;
            int ddl = exp.deadline;
            long val = exp.insight;

            for (int t = ddl; t >= d; t--) {
                if (dp[t - d] != NEG) {
                    dp[t] = Math.max(dp[t], dp[t - d] + val);
                }
            }
        }

        long answer = 0L;
        for (long value : dp) {
            answer = Math.max(answer, value);
        }
        return answer;
    }

    /**
     * Demonstrates the solution on sample-style inputs and prints the results.
     *
     * Note:
     * The textual examples in the prompt contain inconsistencies in their explanations.
     * The dynamic programming algorithm implemented here computes the mathematically correct optimum.
     *
     * For Example 1:
     * duration = [2,1,2], deadline = [2,2,3], insight = [8,4,7]
     * Feasible choices:
     * - experiment 1 alone => 8
     * - experiment 2 alone => 4
     * - experiment 3 alone => 7
     * - experiments 2 then 3 => total time 3, both meet deadlines => 11
     * No pair involving experiment 1 with another experiment is feasible.
     * Therefore the correct optimum is 11.
     *
     * For Example 2:
     * duration = [3,1,2,2], deadline = [3,4,5,6], insight = [10,3,9,8]
     * One optimal schedule is experiments 2, 3, 4 with total insight 20.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     *
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm
     * Space complexity: O(1) auxiliary, excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] duration1 = {2, 1, 2};
        int[] deadline1 = {2, 2, 3};
        int[] insight1 = {8, 4, 7};
        long result1 = solution.maximumInsight(duration1, deadline1, insight1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected mathematically correct result: 11");

        int[] duration2 = {3, 1, 2, 2};
        int[] deadline2 = {3, 4, 5, 6};
        int[] insight2 = {10, 3, 9, 8};
        long result2 = solution.maximumInsight(duration2, deadline2, insight2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected result: 20");

        // Additional quick sanity check:
        // If all experiments fit in deadline order, the DP should choose all of them.
        int[] duration3 = {1, 2, 1};
        int[] deadline3 = {2, 4, 5};
        int[] insight3 = {5, 6, 7};
        long result3 = solution.maximumInsight(duration3, deadline3, insight3);
        System.out.println("Additional test result: " + result3);
        System.out.println("Expected result: 18");
    }
}