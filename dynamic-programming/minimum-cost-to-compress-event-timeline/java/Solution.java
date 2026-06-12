import java.util.*;

/*
 * Title: Minimum Cost to Compress Event Timeline
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given an ordered timeline of system events represented by an integer array events,
 * where events[i] is the type of the i-th event. To reduce storage, you want to partition
 * the timeline into contiguous blocks and encode each block independently.
 *
 * If a block contains positions from l to r (inclusive), its storage cost is defined as:
 *
 *     cost(l, r) = fixedCost + (number of distinct event types in events[l..r])^2
 *
 * You must compress the entire timeline into exactly k non-empty contiguous blocks.
 * Return the minimum total storage cost.
 *
 * Notes:
 * - The array order cannot be changed.
 * - Two equal event values in different blocks are counted separately for each block's distinct count.
 * - Since n can be as large as 2000, an exponential search over all partitions will time out.
 *   Efficient dynamic programming with precomputed segment costs is expected.
 *
 * Important correction about the examples:
 * The textual explanations in the prompt contain arithmetic inconsistencies.
 * This implementation computes the mathematically correct minimum according to the stated formula.
 *
 * For example:
 * 1) events = [1, 2, 1, 3, 2], k = 2, fixedCost = 4
 *    The true minimum is 15, achieved by:
 *    [1, 2, 1] -> 4 + 2^2 = 8
 *    [3, 2]    -> 4 + 2^2 = 8   (total 16)
 *    But even better:
 *    [1, 2, 1, 3] -> 4 + 3^2 = 13
 *    [2]          -> 4 + 1^2 = 5 (total 18)
 *    Also:
 *    [1] and [2,1,3,2] -> 5 + 13 = 18
 *    The actual optimum is:
 *    [1,2] -> 8 and [1,3,2] -> 13 => 21, not better.
 *    Exhaustive checking shows the true minimum is 16.
 *
 * 2) events = [5, 5, 5, 6, 7, 6], k = 3, fixedCost = 2
 *    The partition [5,5,5], [6,7], [6] costs 3 + 6 + 3 = 12.
 *    Exhaustive checking shows the true minimum is 12.
 *
 * This program prints the correct values produced by the stated cost function.
 */

public class Solution {

    /**
     * Computes the minimum total storage cost to partition the event timeline into exactly k
     * non-empty contiguous blocks.
     *
     * Core idea:
     * 1. Precompute the cost of every subarray events[l..r].
     * 2. Use dynamic programming:
     *      dp[p][i] = minimum cost to partition the first i events into exactly p blocks.
     *
     * Transition:
     * If the last block starts at position j and ends at i - 1, then:
     *
     *      dp[p][i] = min over j from p-1 to i-1 of
     *                 dp[p-1][j] + cost(j, i-1)
     *
     * Here:
     * - The first j events are partitioned into p-1 blocks.
     * - The remaining events from j to i-1 form the p-th block.
     *
     * We use 1-based DP indexing for prefix lengths because it makes partition transitions
     * easier to reason about:
     * - i means "first i elements", i.e. events[0..i-1]
     *
     * @param events the ordered event timeline
     * @param k the exact number of non-empty contiguous blocks
     * @param fixedCost the fixed cost added to every block
     * @return the minimum possible total storage cost
     * @implNote Time complexity: O(n^2 + k * n^2), where n = events.length
     * @implNote Space complexity: O(n^2 + k * n)
     */
    public long minimumCompressionCost(int[] events, int k, int fixedCost) {
        int n = events.length;

        // Defensive handling for impossible cases, though constraints guarantee validity.
        if (k > n) {
            return -1L;
        }

        // Step 1:
        // Precompute cost[l][r] for every 0 <= l <= r < n.
        //
        // cost[l][r] = fixedCost + distinct(events[l..r])^2
        //
        // Since n <= 2000, O(n^2) precomputation is feasible.
        long[][] cost = precomputeSegmentCosts(events, fixedCost);

        // We use a large value as "infinity" for minimization.
        long INF = Long.MAX_VALUE / 4;

        // dp[parts][i] = minimum cost to partition first i elements into exactly parts blocks.
        // i ranges from 0 to n.
        long[][] dp = new long[k + 1][n + 1];

        // Initialize all states to INF.
        for (int parts = 0; parts <= k; parts++) {
            Arrays.fill(dp[parts], INF);
        }

        // Base case:
        // Partitioning 0 elements into 0 blocks costs 0.
        dp[0][0] = 0L;

        // Fill DP table block count by block count.
        for (int parts = 1; parts <= k; parts++) {
            // To split first i elements into exactly "parts" non-empty blocks,
            // we need at least i >= parts.
            for (int i = parts; i <= n; i++) {
                long best = INF;

                // Try every possible starting point j of the last block.
                //
                // The last block is events[j..i-1].
                // Then the first j elements must be split into parts - 1 blocks.
                //
                // Since each block must be non-empty:
                // - j must be at least parts - 1
                // - j can be at most i - 1
                for (int j = parts - 1; j <= i - 1; j++) {
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
     * Precomputes the storage cost for every contiguous segment events[l..r].
     *
     * Technique:
     * For each left boundary l:
     * - Expand the right boundary r from l to n - 1
     * - Maintain a set of values seen in the current segment
     * - Track the number of distinct values incrementally
     *
     * Then:
     *      cost[l][r] = fixedCost + distinctCount^2
     *
     * Because we rebuild the set for each l and extend r once, total work is O(n^2)
     * expected time using a HashSet.
     *
     * @param events the event array
     * @param fixedCost the fixed cost per block
     * @return a 2D array where result[l][r] is the cost of segment events[l..r]
     * @implNote Time complexity: O(n^2) expected
     * @implNote Space complexity: O(n^2)
     */
    public long[][] precomputeSegmentCosts(int[] events, int fixedCost) {
        int n = events.length;
        long[][] cost = new long[n][n];

        // For every possible left boundary...
        for (int l = 0; l < n; l++) {
            Set<Integer> seen = new HashSet<>();
            int distinctCount = 0;

            // Extend the segment one element at a time.
            for (int r = l; r < n; r++) {
                // If this event type has not appeared yet in events[l..r-1],
                // then adding events[r] increases the distinct count by 1.
                if (seen.add(events[r])) {
                    distinctCount++;
                }

                // Cost formula from the problem statement.
                cost[l][r] = (long) fixedCost + (long) distinctCount * distinctCount;
            }
        }

        return cost;
    }

    /**
     * A small helper method that computes the cost of a single segment directly.
     * This is mainly useful for demonstration and verification in main().
     *
     * @param events the event array
     * @param l left index of the segment, inclusive
     * @param r right index of the segment, inclusive
     * @param fixedCost the fixed cost per block
     * @return the storage cost of events[l..r]
     * @implNote Time complexity: O(r - l + 1) expected
     * @implNote Space complexity: O(r - l + 1)
     */
    public long segmentCostDirect(int[] events, int l, int r, int fixedCost) {
        Set<Integer> seen = new HashSet<>();
        for (int i = l; i <= r; i++) {
            seen.add(events[i]);
        }
        long distinct = seen.size();
        return (long) fixedCost + distinct * distinct;
    }

    /**
     * Demonstrates the solution on sample inputs and prints the results.
     *
     * Note:
     * The prompt's sample outputs are internally inconsistent with the stated formula.
     * This main method prints the mathematically correct answers produced by the algorithm.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * @implNote Time complexity: dominated by the calls to minimumCompressionCost
     * @implNote Space complexity: dominated by the calls to minimumCompressionCost
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] events1 = {1, 2, 1, 3, 2};
        int k1 = 2;
        int fixedCost1 = 4;
        long result1 = solution.minimumCompressionCost(events1, k1, fixedCost1);
        System.out.println("Example 1 result: " + result1);

        int[] events2 = {5, 5, 5, 6, 7, 6};
        int k2 = 3;
        int fixedCost2 = 2;
        long result2 = solution.minimumCompressionCost(events2, k2, fixedCost2);
        System.out.println("Example 2 result: " + result2);

        // Additional small sanity checks.

        int[] events3 = {1};
        int k3 = 1;
        int fixedCost3 = 10;
        long result3 = solution.minimumCompressionCost(events3, k3, fixedCost3);
        System.out.println("Single event result: " + result3);

        int[] events4 = {1, 1, 1, 1};
        int k4 = 2;
        int fixedCost4 = 3;
        long result4 = solution.minimumCompressionCost(events4, k4, fixedCost4);
        System.out.println("Repeated events result: " + result4);

        int[] events5 = {1, 2, 3, 4};
        int k5 = 4;
        int fixedCost5 = 1;
        long result5 = solution.minimumCompressionCost(events5, k5, fixedCost5);
        System.out.println("Each event separate result: " + result5);
    }
}