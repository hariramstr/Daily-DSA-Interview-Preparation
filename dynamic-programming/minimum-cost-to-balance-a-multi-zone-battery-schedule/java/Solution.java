import java.util.*;

/*
 * Title: Minimum Cost to Balance a Multi-Zone Battery Schedule
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A data center campus is powered by a shared battery system across n time slots.
 * During slot i, the campus demand changes by delta[i], where a positive value means
 * the battery must discharge that many units to support the load, and a negative value
 * means that many units can be stored back into the battery from excess solar generation.
 *
 * You may partition the timeline into exactly k contiguous zones. Inside each zone,
 * you are allowed to choose one target battery drift value t, and every delta in that
 * zone must be adjusted to t by buying or spilling energy. The cost of adjusting slot i
 * inside that zone is |delta[i] - t|. The total cost is the sum over all slots in all zones.
 *
 * Your task is to compute the minimum possible total adjustment cost when dividing the
 * array into exactly k contiguous zones.
 *
 * Choosing the best t for a fixed zone is part of the problem. A zone may contain positive,
 * negative, and zero values. Since the cost uses absolute difference, the optimal target
 * for a zone is related to the median of the values in that zone.
 *
 * Return the minimum total cost.
 *
 * Constraints:
 * - 1 <= n <= 300
 * - 1 <= k <= min(n, 30)
 * - -10^6 <= delta[i] <= 10^6
 * - The answer fits in a signed 64-bit integer.
 *
 * Key Idea:
 * 1. For every subarray delta[l..r], precompute the minimum cost to make all values equal.
 *    Because the cost is sum of absolute differences, the optimal target is any median
 *    of the values in that subarray.
 * 2. Then use dynamic programming:
 *      dp[p][i] = minimum cost to partition the first i elements into exactly p zones.
 *    Transition:
 *      dp[p][i] = min over j from p-1 to i-1 of dp[p-1][j] + cost[j][i-1]
 *    where the last zone is delta[j..i-1].
 */
public class Solution {

    /**
     * Computes the minimum total adjustment cost to partition the array into exactly k contiguous zones.
     *
     * The algorithm works in two major phases:
     * 1. Precompute interval costs:
     *    cost[l][r] = minimum cost to make all values in delta[l..r] equal.
     *    Since the optimal target is a median, we sort each subarray and compute the sum of
     *    distances to the median.
     *
     * 2. Dynamic programming over prefixes:
     *    dp[parts][i] = minimum cost to partition the first i elements (indices 0..i-1)
     *    into exactly "parts" contiguous zones.
     *
     * @param delta the battery drift changes for each time slot
     * @param k the exact number of contiguous zones to create
     * @return the minimum possible total adjustment cost
     * Time complexity: O(n^3 log n + k * n^2), which is acceptable for n <= 300
     * Space complexity: O(n^2 + k * n)
     */
    public long minimumCost(int[] delta, int k) {
        int n = delta.length;

        // Precompute the minimum cost for every subarray delta[l..r].
        long[][] cost = precomputeIntervalCosts(delta);

        // A large value used as "infinity" for minimization.
        long INF = Long.MAX_VALUE / 4;

        // dp[parts][i] = minimum cost to partition first i elements into exactly parts zones.
        long[][] dp = new long[k + 1][n + 1];

        // Initialize all states to INF because we are taking minimums.
        for (int parts = 0; parts <= k; parts++) {
            Arrays.fill(dp[parts], INF);
        }

        // Base case:
        // Partitioning 0 elements into 0 zones costs 0.
        dp[0][0] = 0L;

        // Build the DP table zone by zone.
        for (int parts = 1; parts <= k; parts++) {
            // To split first i elements into exactly "parts" zones,
            // we need at least "parts" elements (each zone must be non-empty).
            for (int i = parts; i <= n; i++) {
                long best = INF;

                // Let j be the number of elements covered by the first (parts - 1) zones.
                // Then the last zone is delta[j..i-1].
                //
                // j must be at least parts - 1 so that the first (parts - 1) zones
                // each contain at least one element.
                for (int j = parts - 1; j < i; j++) {
                    if (dp[parts - 1][j] == INF) {
                        continue;
                    }

                    long candidate = dp[parts - 1][j] + cost[j][i - 1];
                    if (candidate < best) {
                        best = candidate;
                    }
                }

                dp[parts][i] = best;
            }
        }

        return dp[k][n];
    }

    /**
     * Precomputes the minimum adjustment cost for every subarray delta[l..r].
     *
     * For a fixed subarray, the minimum sum of absolute deviations is achieved by choosing
     * any median of the values in that subarray.
     *
     * Since n <= 300, a straightforward and very clear approach is sufficient:
     * - For each pair (l, r), copy the subarray
     * - Sort it
     * - Pick the median
     * - Sum absolute differences to that median
     *
     * This is not the most asymptotically optimized possible method, but it is simple,
     * correct, and fast enough for the given constraints.
     *
     * @param delta the input array
     * @return a 2D array cost where cost[l][r] is the minimum cost for subarray delta[l..r]
     * Time complexity: O(n^3 log n)
     * Space complexity: O(n^2)
     */
    public long[][] precomputeIntervalCosts(int[] delta) {
        int n = delta.length;
        long[][] cost = new long[n][n];

        // We compute every interval independently.
        for (int l = 0; l < n; l++) {
            for (int r = l; r < n; r++) {
                int len = r - l + 1;

                // Copy the subarray delta[l..r] into a temporary array.
                int[] values = new int[len];
                for (int i = 0; i < len; i++) {
                    values[i] = delta[l + i];
                }

                // Sort the values so we can access the median.
                Arrays.sort(values);

                // For absolute deviation, any median minimizes the sum.
                // For even length, either middle value works.
                int median = values[len / 2];

                long sum = 0L;

                // Compute total cost to convert every value in the interval to the median.
                for (int i = 0; i < len; i++) {
                    sum += Math.abs((long) values[i] - median);
                }

                cost[l][r] = sum;
            }
        }

        return cost;
    }

    /**
     * Convenience wrapper matching the problem statement wording.
     *
     * @param delta the battery drift changes for each time slot
     * @param k the exact number of contiguous zones
     * @return the minimum total adjustment cost
     * Time complexity: O(n^3 log n + k * n^2)
     * Space complexity: O(n^2 + k * n)
     */
    public long solve(int[] delta, int k) {
        return minimumCost(delta, k);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Important note about Example 1:
     * If we compute the interval costs exactly and test all valid 2-part partitions,
     * the true minimum is 6, not 5.
     *
     * For delta = [4, 1, 7, 3, 6], k = 2:
     * - Split after index 0: [4] and [1,7,3,6]
     *   costs 0 + 9 = 9
     * - Split after index 1: [4,1] and [7,3,6]
     *   costs 3 + 4 = 7
     * - Split after index 2: [4,1,7] and [3,6]
     *   costs 6 + 3 = 9
     * - Split after index 3: [4,1,7,3] and [6]
     *   costs 7 + 0 = 7
     *
     * Therefore the minimum is 7? Let's verify carefully:
     * [7,3,6] sorted is [3,6,7], median 6, cost 1+3+0 = 4.
     * [4,1] cost = 3, total = 7.
     * [4,1,7] sorted [1,4,7], median 4, cost = 6.
     * [3,6] median 6 or 3, cost = 3.
     * total = 9.
     * [4,1,7,3] sorted [1,3,4,7], median 4 or 3, cost = 7.
     * total = 7.
     *
     * So the correct answer for Example 1 is 7 based on the stated cost definition.
     *
     * Example 2 evaluates correctly to 4.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) outside the invoked solver calls
     * Space complexity: O(1) outside the invoked solver calls
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] delta1 = {4, 1, 7, 3, 6};
        int k1 = 2;
        long result1 = solution.solve(delta1, k1);
        System.out.println("Example 1 result: " + result1);

        int[] delta2 = {-5, -2, -4, 8, 9, 7};
        int k2 = 3;
        long result2 = solution.solve(delta2, k2);
        System.out.println("Example 2 result: " + result2);

        // Additional small sanity checks.
        int[] delta3 = {5};
        int k3 = 1;
        System.out.println("Single element, one zone: " + solution.solve(delta3, k3)); // 0

        int[] delta4 = {1, 2, 3, 4};
        int k4 = 4;
        System.out.println("Each element its own zone: " + solution.solve(delta4, k4)); // 0

        int[] delta5 = {1, 2, 3, 4};
        int k5 = 1;
        System.out.println("All in one zone: " + solution.solve(delta5, k5)); // 4
    }
}