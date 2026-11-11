import java.util.*;

/*
 * Title: Minimum Delay to Assemble a Multi-Stage Rocket
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A space manufacturing team must assemble a rocket from stage 1 to stage n in order.
 * For each stage i, you are given two values:
 *   - buildTime[i]: the number of hours required to physically assemble that stage
 *   - stability[i]: an integer rating of how stable that stage is once completed
 *
 * The team may group consecutive stages into a single assembly batch.
 * If stages l..r are assembled in one batch, the batch takes:
 *
 *   sum(buildTime[l..r]) + (max(stability[l..r]) - min(stability[l..r]))
 *
 * You must partition the n stages into exactly k non-empty consecutive batches.
 * Return the minimum possible total cost.
 *
 * Constraints:
 *   - 1 <= k <= n <= 300
 *   - 1 <= buildTime[i] <= 10^6
 *   - 1 <= stability[i] <= 10^9
 *   - The answer fits in a 64-bit signed integer.
 *
 * Key idea:
 * 1. Precompute the cost of every interval [l..r].
 * 2. Use dynamic programming:
 *      dp[p][i] = minimum cost to partition the first i stages into exactly p batches
 *
 * Transition:
 *      dp[p][i] = min over j from p-1 to i-1 of:
 *                 dp[p-1][j] + cost[j+1][i]
 *
 * This is a classic ordered partition DP.
 */
public class Solution {

    /**
     * Computes the minimum total cost to partition the rocket stages into exactly k consecutive batches.
     *
     * The algorithm works in two major phases:
     *
     * 1. Precompute interval costs:
     *    For every possible interval [l..r], compute:
     *      sum(buildTime[l..r]) + (max(stability[l..r]) - min(stability[l..r]))
     *
     * 2. Dynamic programming over prefixes:
     *    Let dp[p][i] be the minimum cost to partition the first i stages into exactly p batches.
     *    Then:
     *      dp[0][0] = 0
     *      dp[p][i] = min(dp[p-1][j] + cost[j+1][i]) for all valid j
     *
     * We use 1-based indexing internally for cleaner interval handling.
     *
     * @param buildTime the build time for each stage
     * @param stability the stability rating for each stage
     * @param k the exact number of non-empty consecutive batches
     * @return the minimum possible total cost
     * Time complexity: O(n^2 + k * n^2) = O(k * n^2)
     * Space complexity: O(n^2 + k * n)
     */
    public long minimumTotalCost(int[] buildTime, int[] stability, int k) {
        int n = buildTime.length;

        // Defensive validation for beginner-friendliness.
        // The problem guarantees valid input, but checking helps make the method robust.
        if (stability == null || buildTime == null || stability.length != n || k < 1 || k > n) {
            throw new IllegalArgumentException("Invalid input.");
        }

        // Convert arrays to 1-based indexing.
        // This makes interval formulas much easier to read:
        // stage 1 is at index 1, stage n is at index n.
        long[] time = new long[n + 1];
        long[] stab = new long[n + 1];
        for (int i = 1; i <= n; i++) {
            time[i] = buildTime[i - 1];
            stab[i] = stability[i - 1];
        }

        // Prefix sums for build times:
        // prefixTime[i] = sum of time[1..i]
        // Then sum of time[l..r] = prefixTime[r] - prefixTime[l - 1]
        long[] prefixTime = new long[n + 1];
        for (int i = 1; i <= n; i++) {
            prefixTime[i] = prefixTime[i - 1] + time[i];
        }

        // cost[l][r] will store the cost of assembling stages l..r in one batch.
        // We only use entries where 1 <= l <= r <= n.
        long[][] cost = precomputeIntervalCosts(prefixTime, stab);

        // Use a large value as "infinity" for DP initialization.
        // We avoid Long.MAX_VALUE directly to prevent overflow when adding costs.
        long INF = Long.MAX_VALUE / 4;

        // dp[p][i] = minimum cost to partition first i stages into exactly p batches.
        long[][] dp = new long[k + 1][n + 1];

        // Initialize all states to INF.
        for (int p = 0; p <= k; p++) {
            Arrays.fill(dp[p], INF);
        }

        // Base case:
        // 0 stages split into 0 batches costs 0.
        dp[0][0] = 0L;

        // Build the DP table batch by batch.
        for (int batches = 1; batches <= k; batches++) {
            // To split first i stages into exactly "batches" non-empty groups,
            // we must have at least i >= batches.
            for (int i = batches; i <= n; i++) {

                // We try every possible place where the last batch starts.
                // Suppose the previous batches cover stages 1..j,
                // then the last batch is stages (j+1)..i.
                //
                // Since we need exactly (batches - 1) batches for first j stages,
                // j must be at least (batches - 1).
                for (int j = batches - 1; j < i; j++) {
                    if (dp[batches - 1][j] == INF) {
                        continue;
                    }

                    long candidate = dp[batches - 1][j] + cost[j + 1][i];
                    if (candidate < dp[batches][i]) {
                        dp[batches][i] = candidate;
                    }
                }
            }
        }

        return dp[k][n];
    }

    /**
     * Precomputes the cost of every interval [l..r].
     *
     * For each fixed left boundary l, we extend the right boundary r from l to n,
     * while maintaining:
     *   - current maximum stability in [l..r]
     *   - current minimum stability in [l..r]
     *
     * Then:
     *   cost[l][r] = sum(buildTime[l..r]) + (maxStability - minStability)
     *
     * This avoids recomputing max/min from scratch for every interval.
     *
     * @param prefixTime prefix sums of build times, 1-based
     * @param stability stability values, 1-based
     * @return a 2D table where cost[l][r] is the batch cost for interval [l..r]
     * Time complexity: O(n^2)
     * Space complexity: O(n^2)
     */
    public long[][] precomputeIntervalCosts(long[] prefixTime, long[] stability) {
        int n = stability.length - 1;
        long[][] cost = new long[n + 1][n + 1];

        // For each possible left endpoint...
        for (int l = 1; l <= n; l++) {
            long currentMax = stability[l];
            long currentMin = stability[l];

            // Extend the interval one stage at a time.
            for (int r = l; r <= n; r++) {
                // Update max/min stability for the current interval [l..r].
                if (stability[r] > currentMax) {
                    currentMax = stability[r];
                }
                if (stability[r] < currentMin) {
                    currentMin = stability[r];
                }

                // Sum of build times in [l..r] using prefix sums.
                long sumBuildTime = prefixTime[r] - prefixTime[l - 1];

                // Batch cost definition from the problem statement.
                cost[l][r] = sumBuildTime + (currentMax - currentMin);
            }
        }

        return cost;
    }

    /**
     * A small helper method to print one test case in a readable format and show the result.
     *
     * @param buildTime the build times array
     * @param stability the stability array
     * @param k the number of batches
     * @return the computed minimum total cost
     * Time complexity: O(k * n^2)
     * Space complexity: O(n^2 + k * n)
     */
    public long runAndPrint(int[] buildTime, int[] stability, int k) {
        long result = minimumTotalCost(buildTime, stability, k);
        System.out.println("buildTime = " + Arrays.toString(buildTime));
        System.out.println("stability = " + Arrays.toString(stability));
        System.out.println("k = " + k);
        System.out.println("Minimum total cost = " + result);
        System.out.println();
        return result;
    }

    /**
     * Demonstrates the solution on sample inputs.
     *
     * Important note:
     * The example explanations in the prompt list some partitions and their costs,
     * but the stated outputs do not match the actual minimum when computed from the
     * given formula. This implementation follows the formula exactly:
     *
     *   sum(buildTime[l..r]) + (max(stability[l..r]) - min(stability[l..r]))
     *
     * For Example 1:
     *   buildTime = [4,2,7,3], stability = [8,5,6,10], k = 2
     * Valid 2-batch partitions:
     *   [4] | [2,7,3] => 4 + (12 + (10-5)) = 4 + 17 = 21
     *   [4,2] | [7,3] => (6 + (8-5)) + (10 + (10-6)) = 9 + 14 = 23
     *   [4,2,7] | [3] => (13 + (8-5)) + 3 = 16 + 3 = 19
     * So the true minimum is 19.
     *
     * For Example 2:
     *   buildTime = [5,1,4,2,6], stability = [7,7,3,9,4], k = 3
     * One optimal partition is:
     *   [5,1] | [4] | [2,6]
     *   6 + 4 + 13 = 23
     * So the minimum is 23.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total test cost)
     * Space complexity: O(max test memory)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt.
        // According to the formula, the correct minimum is 19, not 18.
        int[] buildTime1 = {4, 2, 7, 3};
        int[] stability1 = {8, 5, 6, 10};
        long result1 = solution.runAndPrint(buildTime1, stability1, 2);
        System.out.println("Expected by formula for Example 1: 19");
        System.out.println("Computed: " + result1);
        System.out.println();

        // Example 2 from the prompt.
        int[] buildTime2 = {5, 1, 4, 2, 6};
        int[] stability2 = {7, 7, 3, 9, 4};
        long result2 = solution.runAndPrint(buildTime2, stability2, 3);
        System.out.println("Expected for Example 2: 23");
        System.out.println("Computed: " + result2);
        System.out.println();

        // Additional small sanity checks.

        // Single stage, single batch.
        int[] buildTime3 = {10};
        int[] stability3 = {100};
        long result3 = solution.runAndPrint(buildTime3, stability3, 1);
        System.out.println("Expected: 10");
        System.out.println("Computed: " + result3);
        System.out.println();

        // Each stage in its own batch: total is just sum(buildTime), because each batch has max-min = 0.
        int[] buildTime4 = {3, 5, 2};
        int[] stability4 = {9, 1, 7};
        long result4 = solution.runAndPrint(buildTime4, stability4, 3);
        System.out.println("Expected: 10");
        System.out.println("Computed: " + result4);
    }
}