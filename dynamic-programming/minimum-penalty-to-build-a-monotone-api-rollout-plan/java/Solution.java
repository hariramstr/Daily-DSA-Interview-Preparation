import java.util.*;

/*
 * Title: Minimum Penalty to Build a Monotone API Rollout Plan
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A backend team is rolling out a new API across N consecutive regions, numbered from 0 to N - 1.
 * For each region i, the team must choose exactly one rollout level level[i] from 1 to M.
 * Higher levels enable more features but may cause instability in some regions.
 * The instability penalty of assigning level j to region i is given by cost[i][j - 1].
 *
 * To simplify support and documentation, the final rollout plan must be monotone:
 * if region a comes before region b, then the chosen level for region a cannot be greater than
 * the chosen level for region b. In other words, the sequence
 * level[0], level[1], ..., level[N - 1] must be nondecreasing.
 *
 * Your task is to compute the minimum possible total instability penalty over all valid monotone
 * rollout plans.
 *
 * Return the minimum total penalty.
 *
 * Constraints:
 * - 1 <= N <= 2000
 * - 1 <= M <= 2000
 * - 1 <= N * M <= 4 * 10^6
 * - 0 <= cost[i][j] <= 10^9
 * - The answer may exceed 32-bit integer range, so use 64-bit integers.
 *
 * Example 1:
 * Input: cost = [[5,1,4],[6,2,3],[7,3,1]]
 * Output: 4
 * Explanation: Choose rollout levels [2,2,3]. The penalties are 1 + 2 + 1 = 4.
 * This sequence is nondecreasing, so it is valid. No other valid monotone assignment has
 * smaller total penalty.
 *
 * Example 2:
 * Input: cost = [[1,10,100],[100,1,10],[100,10,1],[1000,1,1]]
 * Output: 4
 * Explanation: One optimal plan is [1,2,3,3]. The total penalty is 1 + 1 + 1 + 1 = 4.
 * The sequence is nondecreasing, so it is valid. Plans that greedily choose the cheapest level
 * per region may violate the monotonicity rule, so dynamic programming is needed.
 */

public class Solution {

    /**
     * Computes the minimum total instability penalty for a nondecreasing rollout plan.
     *
     * Dynamic Programming idea:
     * Let dp[i][j] mean:
     * the minimum total penalty for regions 0..i, if region i is assigned level (j + 1).
     *
     * Transition:
     * Since the chosen levels must be nondecreasing, if region i uses level j,
     * then region i - 1 must use some level k where k <= j.
     *
     * Therefore:
     * dp[i][j] = cost[i][j] + min(dp[i - 1][0], dp[i - 1][1], ..., dp[i - 1][j])
     *
     * A direct implementation of that transition would be O(N * M * M),
     * which is too slow.
     *
     * Optimization:
     * While processing row i, we maintain a running prefix minimum over the previous DP row:
     * prefixMin[j] = min(previous[0..j])
     *
     * Then:
     * current[j] = cost[i][j] + prefixMin[j]
     *
     * This reduces the total complexity to O(N * M).
     *
     * @param cost a matrix where cost[i][j] is the penalty of assigning level (j + 1) to region i
     * @return the minimum possible total penalty among all valid nondecreasing level assignments
     * Time complexity: O(N * M)
     * Space complexity: O(M)
     */
    public long minimumPenalty(int[][] cost) {
        if (cost == null || cost.length == 0 || cost[0].length == 0) {
            return 0L;
        }

        int n = cost.length;
        int m = cost[0].length;

        // previous[j] will store the DP values for the previous region.
        // Specifically, after processing region i - 1:
        // previous[j] = minimum total penalty for regions 0..i-1,
        //               with region i-1 assigned level (j + 1).
        long[] previous = new long[m];

        // Base case for region 0:
        // If there is only one region considered, and we assign it level (j + 1),
        // then the total penalty is simply cost[0][j].
        for (int j = 0; j < m; j++) {
            previous[j] = cost[0][j];
        }

        // Process each remaining region one by one.
        for (int i = 1; i < n; i++) {
            long[] current = new long[m];

            // runningMin will represent:
            // min(previous[0], previous[1], ..., previous[j])
            // as we sweep j from left to right.
            long runningMin = Long.MAX_VALUE;

            for (int j = 0; j < m; j++) {
                // Update the prefix minimum using the previous DP row.
                // This is the key optimization:
                // instead of scanning all k <= j every time,
                // we incrementally maintain the minimum.
                runningMin = Math.min(runningMin, previous[j]);

                // If region i is assigned level (j + 1),
                // then all earlier regions must end at levels <= (j + 1).
                // The best such previous total is exactly runningMin.
                current[j] = runningMin + cost[i][j];
            }

            // Move to the next row.
            previous = current;
        }

        // The answer is the minimum value in the last DP row,
        // because the final region may end at any level.
        long answer = Long.MAX_VALUE;
        for (long value : previous) {
            answer = Math.min(answer, value);
        }

        return answer;
    }

    /**
     * A second public method that performs the same computation.
     * This is included to provide a clearly named API for interview-style usage.
     *
     * @param cost a matrix where cost[i][j] is the penalty of assigning level (j + 1) to region i
     * @return the minimum possible total penalty for a monotone rollout plan
     * Time complexity: O(N * M)
     * Space complexity: O(M)
     */
    public long minTotalPenalty(int[][] cost) {
        return minimumPenalty(cost);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm
     * Space complexity: O(1) for the demonstration itself, excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] cost1 = {
            {5, 1, 4},
            {6, 2, 3},
            {7, 3, 1}
        };

        int[][] cost2 = {
            {1, 10, 100},
            {100, 1, 10},
            {100, 10, 1},
            {1000, 1, 1}
        };

        long result1 = solution.minimumPenalty(cost1);
        long result2 = solution.minimumPenalty(cost2);

        System.out.println("Example 1 Output: " + result1);
        System.out.println("Expected: 4");
        System.out.println();

        System.out.println("Example 2 Output: " + result2);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional tiny sanity check:
        // Single region, choose the cheapest level directly.
        int[][] cost3 = {
            {9, 2, 7, 3}
        };
        long result3 = solution.minimumPenalty(cost3);
        System.out.println("Single region example Output: " + result3);
        System.out.println("Expected: 2");
    }
}