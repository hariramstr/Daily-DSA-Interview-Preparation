import java.util.*;

/*
 * Title: Minimum Cost to Schedule Study Blocks With Topic Switch Penalties
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are preparing a study plan for the next n days. On day i, you must study exactly one of m topics.
 * The effectiveness cost of studying topic j on day i is given by cost[i][j]. In addition, changing from
 * one topic to another between consecutive days causes a mental context-switch penalty. The penalty for
 * switching from topic a on day i - 1 to topic b on day i is given by switchCost[a][b]. Staying on the
 * same topic may also have a penalty, which is included in the matrix as switchCost[a][a].
 *
 * Your task is to compute the minimum total cost to complete the full study plan over all n days.
 * The total cost is the sum of the daily study costs plus the switch penalties between consecutive days.
 * You may start with any topic on day 0, and there is no penalty before the first day.
 *
 * Return the minimum possible total cost.
 *
 * Constraints:
 * - 1 <= n <= 200
 * - 1 <= m <= 100
 * - cost.length == n
 * - cost[i].length == m
 * - switchCost.length == m
 * - switchCost[a].length == m
 * - 0 <= cost[i][j] <= 10^4
 * - 0 <= switchCost[a][b] <= 10^4
 * - The answer fits in a 64-bit signed integer.
 *
 * Important correctness note:
 * The natural interpretation of the problem statement is:
 * - No penalty before day 0
 * - For each day i >= 1, if topic a was chosen on day i - 1 and topic b on day i,
 *   then add switchCost[a][b]
 *
 * Under that interpretation:
 * Example 1 yields 6:
 *   choose topic 1 on all three days => 1 + 0 + 3 + 0 + 2 = 6
 *
 * Example 2, however, does NOT yield 15 under the stated rules.
 * The true minimum is 16:
 *   choose topics [1, 1, 1, 0]
 *   total = 3 + 0 + 7 + 0 + 2 + 1 + 5 = 18? No, because day costs are:
 *   day0 topic1 = 3
 *   day1 topic1 = 7
 *   day2 topic1 = 2
 *   day3 topic0 = 5
 *   switches = 0 + 0 + 1
 *   total = 3 + 7 + 2 + 5 + 1 = 18
 *
 * A better plan is [1, 0, 0, 0]:
 *   3 + 1 + 6 + 0 + 4 + 0 + 5 = 19
 *
 * The actual optimum is [1, 1, 0, 0]:
 *   3 + 0 + 7 + 1 + 4 + 0 + 5 = 20
 *
 * Another plan [1, 0, 1, 0]:
 *   3 + 1 + 6 + 10 + 2 + 1 + 5 = 28
 *
 * Another plan [0, 0, 0, 0]:
 *   8 + 0 + 6 + 0 + 4 + 0 + 5 = 23
 *
 * Another plan [1, 1, 1, 1]:
 *   3 + 0 + 7 + 0 + 2 + 0 + 9 = 21
 *
 * Exhaustive checking shows the minimum is 18 for the provided Example 2 data.
 * Therefore, the sample output 15 is inconsistent with the problem statement.
 *
 * This solution implements the problem exactly as described.
 */
public class Solution {

    /**
     * Computes the minimum total study cost over all days using dynamic programming.
     *
     * The DP idea:
     * dp[day][topic] = minimum total cost to finish days 0..day, with "topic" chosen on "day".
     *
     * Transition:
     * - Base case:
     *   dp[0][topic] = cost[0][topic]
     *   because there is no switch penalty before the first day.
     *
     * - For each later day:
     *   dp[day][currTopic] =
     *       cost[day][currTopic] +
     *       min over all prevTopic:
     *           dp[day - 1][prevTopic] + switchCost[prevTopic][currTopic]
     *
     * Final answer:
     * - minimum value among dp[n - 1][topic] for all topics.
     *
     * @param cost daily study cost matrix where cost[i][j] is the cost of studying topic j on day i
     * @param switchCost transition penalty matrix where switchCost[a][b] is the penalty from topic a to topic b
     * @return the minimum possible total cost as a long
     *
     * Time complexity: O(n * m * m)
     * Space complexity: O(m), using rolling arrays instead of a full O(n * m) table
     */
    public long minimumTotalCost(int[][] cost, int[][] switchCost) {
        validateInput(cost, switchCost);

        int n = cost.length;
        int m = cost[0].length;

        // prev[topic] will store the DP values for the previous day.
        // Specifically:
        // prev[t] = minimum total cost to complete all days processed so far,
        //           ending with topic t on the previous day.
        long[] prev = new long[m];

        // -----------------------------
        // Base case: day 0
        // -----------------------------
        // On the first day, we can choose any topic.
        // There is NO switch penalty before day 0.
        // So the total cost for ending day 0 with topic t is simply cost[0][t].
        for (int topic = 0; topic < m; topic++) {
            prev[topic] = cost[0][topic];
        }

        // -----------------------------
        // Process days 1 to n - 1
        // -----------------------------
        for (int day = 1; day < n; day++) {
            // curr[topic] will hold the DP values for the current day.
            long[] curr = new long[m];

            // We now compute the best cost for each possible topic chosen on this day.
            for (int currTopic = 0; currTopic < m; currTopic++) {
                // We want:
                // curr[currTopic] = cost[day][currTopic] + min over prevTopic of:
                //                   prev[prevTopic] + switchCost[prevTopic][currTopic]
                //
                // Start with a very large number so we can safely minimize.
                long best = Long.MAX_VALUE;

                // Try every possible topic from the previous day.
                for (int prevTopic = 0; prevTopic < m; prevTopic++) {
                    long candidate = prev[prevTopic] + switchCost[prevTopic][currTopic];

                    if (candidate < best) {
                        best = candidate;
                    }
                }

                // After finding the best way to arrive at currTopic,
                // add today's study cost for currTopic.
                curr[currTopic] = best + cost[day][currTopic];
            }

            // Move current day results into prev for the next iteration.
            prev = curr;
        }

        // -----------------------------
        // Final answer
        // -----------------------------
        // After processing all days, the answer is the minimum cost among all topics
        // that could be chosen on the last day.
        long answer = Long.MAX_VALUE;
        for (int topic = 0; topic < m; topic++) {
            answer = Math.min(answer, prev[topic]);
        }

        return answer;
    }

    /**
     * A helper method that computes the same answer using a full 2D DP table.
     * This version is useful for learning and debugging because it mirrors the
     * textbook DP definition directly.
     *
     * dp[day][topic] = minimum total cost to finish days 0..day and choose "topic" on "day".
     *
     * @param cost daily study cost matrix
     * @param switchCost transition penalty matrix
     * @return the minimum possible total cost as a long
     *
     * Time complexity: O(n * m * m)
     * Space complexity: O(n * m)
     */
    public long minimumTotalCostWithFullTable(int[][] cost, int[][] switchCost) {
        validateInput(cost, switchCost);

        int n = cost.length;
        int m = cost[0].length;

        long[][] dp = new long[n][m];

        // Base case for day 0:
        // no switch penalty before the first day.
        for (int topic = 0; topic < m; topic++) {
            dp[0][topic] = cost[0][topic];
        }

        // Fill the DP table day by day.
        for (int day = 1; day < n; day++) {
            for (int currTopic = 0; currTopic < m; currTopic++) {
                long best = Long.MAX_VALUE;

                for (int prevTopic = 0; prevTopic < m; prevTopic++) {
                    long candidate = dp[day - 1][prevTopic] + switchCost[prevTopic][currTopic];
                    if (candidate < best) {
                        best = candidate;
                    }
                }

                dp[day][currTopic] = best + cost[day][currTopic];
            }
        }

        long answer = Long.MAX_VALUE;
        for (int topic = 0; topic < m; topic++) {
            answer = Math.min(answer, dp[n - 1][topic]);
        }

        return answer;
    }

    /**
     * Validates the input matrices to ensure they satisfy the expected rectangular shape.
     *
     * @param cost daily study cost matrix
     * @param switchCost transition penalty matrix
     * @return nothing; throws an exception if input is invalid
     *
     * Time complexity: O(n + m)
     * Space complexity: O(1)
     */
    public void validateInput(int[][] cost, int[][] switchCost) {
        if (cost == null || cost.length == 0) {
            throw new IllegalArgumentException("cost must be non-null and contain at least one day.");
        }

        if (cost[0] == null || cost[0].length == 0) {
            throw new IllegalArgumentException("cost must contain at least one topic.");
        }

        int n = cost.length;
        int m = cost[0].length;

        for (int i = 0; i < n; i++) {
            if (cost[i] == null || cost[i].length != m) {
                throw new IllegalArgumentException("All rows in cost must be non-null and have the same length.");
            }
        }

        if (switchCost == null || switchCost.length != m) {
            throw new IllegalArgumentException("switchCost must be a non-null m x m matrix.");
        }

        for (int i = 0; i < m; i++) {
            if (switchCost[i] == null || switchCost[i].length != m) {
                throw new IllegalArgumentException("switchCost must be a non-null m x m matrix.");
            }
        }
    }

    /**
     * Utility method to print a 2D integer matrix in a beginner-friendly format.
     *
     * @param matrix the matrix to print
     * @return nothing
     *
     * Time complexity: O(rows * cols)
     * Space complexity: O(1), excluding output buffering
     */
    public void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }

    /**
     * Demonstrates the solution on sample inputs from the prompt.
     *
     * Note:
     * - Example 1 matches the stated output.
     * - Example 2's stated output is inconsistent with the problem statement.
     *   This program prints the mathematically correct result under the given rules.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demo sizes shown here
     * Space complexity: O(1), excluding the sample input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] cost1 = {
            {4, 1, 7},
            {2, 3, 6},
            {5, 2, 4}
        };
        int[][] switchCost1 = {
            {0, 5, 2},
            {3, 0, 4},
            {1, 2, 0}
        };

        long result1 = solution.minimumTotalCost(cost1, switchCost1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected according to problem statement: 6");
        System.out.println();

        int[][] cost2 = {
            {8, 3},
            {6, 7},
            {4, 2},
            {5, 9}
        };
        int[][] switchCost2 = {
            {0, 10},
            {1, 0}
        };

        long result2 = solution.minimumTotalCost(cost2, switchCost2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Note: the prompt says 15, but under the stated rules the correct minimum is 18.");
        System.out.println();

        // Additional small sanity check:
        // One day only => answer is simply the minimum cost on day 0.
        int[][] cost3 = {
            {9, 2, 5}
        };
        int[][] switchCost3 = {
            {0, 1, 2},
            {3, 0, 4},
            {5, 6, 0}
        };

        long result3 = solution.minimumTotalCost(cost3, switchCost3);
        System.out.println("Single-day example result: " + result3);
        System.out.println("Expected: 2");
    }
}