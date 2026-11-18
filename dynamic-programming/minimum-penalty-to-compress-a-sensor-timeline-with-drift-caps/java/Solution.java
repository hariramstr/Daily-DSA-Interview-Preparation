import java.util.*;

/*
 * Title: Minimum Penalty to Compress a Sensor Timeline with Drift Caps
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given a time-ordered array readings of length n, where readings[i] is the value
 * reported by a sensor at second i. To reduce storage, you want to compress the timeline
 * into contiguous blocks. Each block [l, r] is stored using a single representative value,
 * and every reading in that block is reconstructed as that representative value.
 *
 * For any block [l, r], the representative value may be chosen arbitrarily, but the block
 * is valid only if the difference between the maximum and minimum reading inside the block
 * is at most driftCap. If a block is valid, its compression penalty is the sum of absolute
 * differences between each reading in the block and the chosen representative value. Since
 * the representative may be chosen freely, the minimum penalty for a valid block is achieved
 * by choosing a median of the readings in that block.
 *
 * You must partition the entire array into exactly k non-empty contiguous valid blocks and
 * return the minimum possible total compression penalty. If it is impossible to partition
 * the array into exactly k valid blocks, return -1.
 *
 * Constraints:
 * - 1 <= n <= 300
 * - 1 <= k <= n
 * - 0 <= readings[i] <= 10^9
 * - 0 <= driftCap <= 10^9
 *
 * Notes:
 * - Blocks must remain contiguous in the original order.
 * - The representative value for a block does not need to appear in the block, though any
 *   median is optimal.
 * - You only need to return the minimum total penalty, not the partition itself.
 * - A correct solution typically precomputes the cost of every valid subarray and then
 *   applies dynamic programming over partition count and prefix length.
 */

public class Solution {

    /**
     * Computes the minimum total compression penalty when partitioning the entire readings array
     * into exactly k non-empty contiguous valid blocks.
     *
     * A block is valid if max(block) - min(block) <= driftCap.
     * For each valid block, its minimum penalty is the sum of distances to a median value.
     *
     * The algorithm works in two major phases:
     * 1) Precompute the cost of every subarray [l..r] if it is valid, otherwise mark it invalid.
     * 2) Run dynamic programming where dp[parts][i] = minimum penalty to partition the first i
     *    elements into exactly parts valid blocks.
     *
     * @param readings the sensor readings in time order
     * @param k the exact number of contiguous non-empty blocks required
     * @param driftCap the maximum allowed range (max - min) inside any block
     * @return the minimum total penalty, or -1 if no valid partition into exactly k blocks exists
     * Time complexity: O(n^3 log n + k * n^2), which is acceptable for n <= 300
     * Space complexity: O(n^2 + k * n)
     */
    public long minimumPenalty(int[] readings, int k, int driftCap) {
        int n = readings.length;

        // Quick impossible case:
        // We cannot split n elements into more than n non-empty blocks.
        if (k > n) {
            return -1;
        }

        // cost[l][r] will store the minimum penalty for subarray readings[l..r]
        // if that subarray is valid under the drift cap.
        // If invalid, we store INF.
        long[][] cost = precomputeBlockCosts(readings, driftCap);

        // A very large number used to represent "impossible".
        long INF = Long.MAX_VALUE / 4;

        // dp[parts][i] = minimum penalty to partition the first i elements
        // (that is, readings[0..i-1]) into exactly 'parts' valid blocks.
        long[][] dp = new long[k + 1][n + 1];

        // Initialize all states as impossible.
        for (int parts = 0; parts <= k; parts++) {
            Arrays.fill(dp[parts], INF);
        }

        // Base case:
        // Zero elements partitioned into zero blocks has zero cost.
        dp[0][0] = 0;

        // Build the DP table one block count at a time.
        for (int parts = 1; parts <= k; parts++) {
            // To form 'parts' non-empty blocks, we need at least 'parts' elements.
            for (int i = parts; i <= n; i++) {
                // We try making the last block start at position j and end at i-1.
                // Then the previous prefix is readings[0..j-1], which must be split into parts-1 blocks.
                //
                // j must be at least parts-1 so that the first j elements can contain parts-1 non-empty blocks.
                for (int j = parts - 1; j < i; j++) {
                    if (dp[parts - 1][j] == INF) {
                        continue;
                    }

                    long blockCost = cost[j][i - 1];
                    if (blockCost == INF) {
                        continue;
                    }

                    dp[parts][i] = Math.min(dp[parts][i], dp[parts - 1][j] + blockCost);
                }
            }
        }

        return dp[k][n] >= INF ? -1 : dp[k][n];
    }

    /**
     * Precomputes the minimum penalty for every contiguous subarray [l..r].
     *
     * For each subarray:
     * - If max - min > driftCap, the subarray is invalid and cost[l][r] is set to INF.
     * - Otherwise, the minimum penalty is the sum of absolute deviations from a median.
     *
     * Because n <= 300, we can afford an O(n^3 log n) style preprocessing:
     * for each starting index l, we extend r one step at a time, track min/max,
     * and when valid, compute the median-based cost by sorting the subarray values.
     *
     * @param readings the sensor readings
     * @param driftCap the maximum allowed range inside a valid block
     * @return a 2D array cost where cost[l][r] is the minimum penalty for readings[l..r],
     *         or INF if that block is invalid
     * Time complexity: O(n^3 log n)
     * Space complexity: O(n^2)
     */
    public long[][] precomputeBlockCosts(int[] readings, int driftCap) {
        int n = readings.length;
        long INF = Long.MAX_VALUE / 4;
        long[][] cost = new long[n][n];

        // Initialize everything as invalid/impossible first.
        for (int i = 0; i < n; i++) {
            Arrays.fill(cost[i], INF);
        }

        // We process every possible starting index l.
        for (int l = 0; l < n; l++) {
            int currentMin = Integer.MAX_VALUE;
            int currentMax = Integer.MIN_VALUE;

            // Extend the right boundary r from l to n-1.
            for (int r = l; r < n; r++) {
                // Update min and max for the current subarray readings[l..r].
                currentMin = Math.min(currentMin, readings[r]);
                currentMax = Math.max(currentMax, readings[r]);

                // If the range exceeds driftCap, this block is invalid.
                if ((long) currentMax - currentMin > driftCap) {
                    cost[l][r] = INF;
                    continue;
                }

                // Otherwise, compute the minimum sum of absolute deviations to a median.
                cost[l][r] = computeMedianPenalty(readings, l, r);
            }
        }

        return cost;
    }

    /**
     * Computes the minimum possible sum of absolute differences between all values in
     * readings[l..r] and a chosen representative value.
     *
     * This minimum is achieved by any median of the multiset of values in the subarray.
     * We therefore:
     * 1) Copy the subarray values into a temporary array
     * 2) Sort it
     * 3) Pick one median
     * 4) Sum absolute differences to that median
     *
     * @param readings the full readings array
     * @param l left index of the subarray (inclusive)
     * @param r right index of the subarray (inclusive)
     * @return the minimum penalty for this subarray
     * Time complexity: O(m log m), where m = r - l + 1
     * Space complexity: O(m)
     */
    public long computeMedianPenalty(int[] readings, int l, int r) {
        int len = r - l + 1;
        int[] values = new int[len];

        // Copy the subarray into a temporary array so we can sort it.
        for (int i = 0; i < len; i++) {
            values[i] = readings[l + i];
        }

        Arrays.sort(values);

        // Any median minimizes the sum of absolute deviations.
        // For even length, either middle value works.
        int median = values[len / 2];

        long penalty = 0L;
        for (int value : values) {
            penalty += Math.abs((long) value - median);
        }

        return penalty;
    }

    /**
     * A convenience wrapper matching the problem statement style.
     *
     * @param readings the sensor readings
     * @param k the exact number of contiguous valid blocks
     * @param driftCap the maximum allowed range inside each block
     * @return the minimum total penalty, or -1 if impossible
     * Time complexity: O(n^3 log n + k * n^2)
     * Space complexity: O(n^2 + k * n)
     */
    public long solve(int[] readings, int k, int driftCap) {
        return minimumPenalty(readings, k, driftCap);
    }

    /**
     * Demonstrates the solution on sample-style inputs and a few extra sanity checks.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total work of invoked examples)
     * Space complexity: O(space used by invoked examples)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] readings1 = {8, 10, 9, 3, 4};
        int k1 = 2;
        int driftCap1 = 2;
        long result1 = solution.solve(readings1, k1, driftCap1);
        System.out.println(result1);

        int[] readings2 = {1, 7, 2, 8};
        int k2 = 2;
        int driftCap2 = 3;
        long result2 = solution.solve(readings2, k2, driftCap2);
        System.out.println(result2);

        int[] readings3 = {5, 5, 5, 5};
        int k3 = 2;
        int driftCap3 = 0;
        long result3 = solution.solve(readings3, k3, driftCap3);
        System.out.println(result3);

        int[] readings4 = {1, 2, 3};
        int k4 = 3;
        int driftCap4 = 0;
        long result4 = solution.solve(readings4, k4, driftCap4);
        System.out.println(result4);
    }
}