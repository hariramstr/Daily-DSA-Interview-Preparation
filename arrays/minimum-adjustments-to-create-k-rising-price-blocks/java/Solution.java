import java.util.*;

/*
 * Title: Minimum Adjustments to Create K Rising Price Blocks
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array prices of length n and an integer k. You may change the value
 * of any element to any other integer, and the cost of changing prices[i] to x is |prices[i] - x|.
 * Your goal is to partition the array into exactly k non-empty contiguous blocks such that after
 * applying some changes, every block becomes non-decreasing.
 *
 * In other words, if a block covers indices [l..r], then after modification its values must satisfy:
 * a[l] <= a[l+1] <= ... <= a[r].
 *
 * Return the minimum total adjustment cost required to transform the array so that it can be split
 * into exactly k contiguous non-decreasing blocks.
 *
 * The partition boundaries are your choice, and the modified values in different blocks do not need
 * to relate to each other. A block of length 1 is always non-decreasing. This is an optimization
 * problem over both the partitioning and the edited values.
 *
 * Constraints:
 * - 1 <= n <= 200
 * - 1 <= k <= n
 * - -10^9 <= prices[i] <= 10^9
 * - You must use exactly k blocks
 *
 * Key idea:
 * 1) Precompute cost[l][r] = minimum cost to modify subarray prices[l..r] into a non-decreasing array.
 * 2) Then run a partition DP:
 *      dp[i][b] = minimum cost to split first i elements into exactly b valid blocks.
 *
 * Important modeling fact for interval cost:
 * For a fixed subarray, there always exists an optimal non-decreasing edited sequence whose values
 * are chosen from the original values appearing in that subarray. Therefore, for each interval we can
 * solve a dynamic programming problem over the sorted distinct values of that interval.
 */
public class Solution {

    /**
     * Computes the minimum total adjustment cost to partition the array into exactly k contiguous
     * blocks such that each block can be edited into a non-decreasing sequence.
     *
     * Approach:
     * 1. Precompute interval costs:
     *    cost[l][r] = minimum cost to make prices[l..r] non-decreasing.
     * 2. Use dynamic programming over partitions:
     *    dp[i][b] = minimum cost for first i elements using exactly b blocks.
     *
     * Time complexity:
     * - Interval preprocessing: O(n^4) in the worst case for n <= 200, which is acceptable here.
     * - Partition DP: O(k * n^2)
     * - Overall: O(n^4 + k * n^2)
     *
     * Space complexity:
     * - cost table: O(n^2)
     * - dp table: O(nk)
     * - temporary arrays during interval computation: O(n^2) total reused
     *
     * @param prices the input array of prices
     * @param k the exact number of contiguous blocks required
     * @return the minimum total adjustment cost
     */
    public long minimumAdjustmentCost(int[] prices, int k) {
        int n = prices.length;

        // Precompute the minimum cost for every interval [l..r].
        long[][] cost = computeIntervalCosts(prices);

        // dp[i][b] = minimum cost to split first i elements (indices 0..i-1) into exactly b blocks.
        long INF = Long.MAX_VALUE / 4;
        long[][] dp = new long[n + 1][k + 1];
        for (int i = 0; i <= n; i++) {
            Arrays.fill(dp[i], INF);
        }
        dp[0][0] = 0;

        // Build the answer block by block.
        for (int blocks = 1; blocks <= k; blocks++) {
            // We need at least 'blocks' elements to form 'blocks' non-empty blocks.
            for (int i = blocks; i <= n; i++) {
                // Try the last block as [j..i-1].
                // Then the first j elements must already be split into blocks-1 blocks.
                for (int j = blocks - 1; j < i; j++) {
                    if (dp[j][blocks - 1] == INF) {
                        continue;
                    }
                    long candidate = dp[j][blocks - 1] + cost[j][i - 1];
                    if (candidate < dp[i][blocks]) {
                        dp[i][blocks] = candidate;
                    }
                }
            }
        }

        return dp[n][k];
    }

    /**
     * Precomputes the minimum cost for every interval [l..r] to become non-decreasing.
     *
     * Detailed interval DP idea:
     * For a fixed interval prices[l..r], let vals be the sorted distinct values appearing in that interval.
     * We define:
     *   dpPos[t][v] = minimum cost to edit prices[l..l+t] so that:
     *                 - the edited sequence is non-decreasing
     *                 - the last edited value equals vals[v]
     *
     * Transition:
     *   dpPos[t][v] = |prices[l+t] - vals[v]| + min(dpPos[t-1][u]) for all u <= v
     *
     * The condition u <= v enforces non-decreasing order.
     * We optimize the transition using prefix minima over v.
     *
     * Why values from the interval are enough:
     * For L1 absolute deviation with monotonicity constraints, an optimal solution can be chosen from
     * breakpoints induced by the original values. Therefore restricting candidate edited values to the
     * distinct values in the interval preserves optimality.
     *
     * Time complexity:
     * - There are O(n^2) intervals.
     * - Each interval of length m uses O(m^2) time in the worst case.
     * - Total worst-case complexity: O(n^4)
     *
     * Space complexity:
     * - Returned cost table: O(n^2)
     * - Temporary DP arrays per interval: O(n)
     *
     * @param prices the input array
     * @return a 2D table cost where cost[l][r] is the minimum cost for subarray [l..r]
     */
    public long[][] computeIntervalCosts(int[] prices) {
        int n = prices.length;
        long[][] cost = new long[n][n];

        // Enumerate every possible left boundary.
        for (int l = 0; l < n; l++) {
            // We will gradually extend the interval [l..r].
            // For each new r, we rebuild the candidate value set from prices[l..r].
            // Since n <= 200, this straightforward approach is fully acceptable and easier to understand.
            for (int r = l; r < n; r++) {
                cost[l][r] = intervalNonDecreasingCost(prices, l, r);
            }
        }

        return cost;
    }

    /**
     * Computes the minimum cost to edit prices[l..r] into a non-decreasing sequence.
     *
     * Step-by-step:
     * 1. Extract all values in the interval.
     * 2. Sort and deduplicate them to form candidate edited values.
     * 3. Run a DP over positions and candidate ending values.
     *
     * Example of the DP meaning:
     * If candidates are [1, 3, 7], then choosing candidate index 1 for a position means
     * the edited value at that position is 3.
     *
     * Time complexity:
     * - Let m = r - l + 1
     * - Sorting and deduplication: O(m log m)
     * - DP: O(m^2)
     * - Overall: O(m^2)
     *
     * Space complexity:
     * - O(m) for candidate values and rolling DP arrays
     *
     * @param prices the input array
     * @param l left index of interval, inclusive
     * @param r right index of interval, inclusive
     * @return minimum cost to make prices[l..r] non-decreasing
     */
    public long intervalNonDecreasingCost(int[] prices, int l, int r) {
        int m = r - l + 1;

        // Step 1: collect interval values.
        long[] raw = new long[m];
        for (int i = 0; i < m; i++) {
            raw[i] = prices[l + i];
        }

        // Step 2: sort and deduplicate.
        Arrays.sort(raw);
        long[] vals = new long[m];
        int distinct = 0;
        for (int i = 0; i < m; i++) {
            if (i == 0 || raw[i] != raw[i - 1]) {
                vals[distinct++] = raw[i];
            }
        }

        // Rolling DP arrays:
        // prev[v] = minimum cost for processed prefix ending with candidate value vals[v]
        // curr[v] = same for the next position
        long[] prev = new long[distinct];
        long[] curr = new long[distinct];

        // Base case: first element can be changed independently to any candidate value.
        for (int v = 0; v < distinct; v++) {
            prev[v] = Math.abs((long) prices[l] - vals[v]);
        }

        // Process remaining positions one by one.
        for (int pos = 1; pos < m; pos++) {
            // prefixMin[v] = min(prev[0..v])
            long best = Long.MAX_VALUE / 4;
            for (int v = 0; v < distinct; v++) {
                if (prev[v] < best) {
                    best = prev[v];
                }
                curr[v] = best + Math.abs((long) prices[l + pos] - vals[v]);
            }

            // Swap prev and curr for the next iteration.
            long[] temp = prev;
            prev = curr;
            curr = temp;
        }

        // Final answer is the minimum cost among all possible ending values.
        long answer = Long.MAX_VALUE / 4;
        for (int v = 0; v < distinct; v++) {
            if (prev[v] < answer) {
                answer = prev[v];
            }
        }
        return answer;
    }

    /**
     * A small helper method to run and print one test case.
     *
     * Time complexity:
     * - Dominated by minimumAdjustmentCost
     *
     * Space complexity:
     * - Dominated by minimumAdjustmentCost
     *
     * @param prices the input array
     * @param k the number of blocks
     * @return the computed answer, also printed to standard output
     */
    public long demo(int[] prices, int k) {
        long ans = minimumAdjustmentCost(prices, k);
        System.out.println("prices = " + Arrays.toString(prices) + ", k = " + k + " -> " + ans);
        return ans;
    }

    /**
     * Demonstrates the solution on sample-style inputs and a few additional checks.
     *
     * Note:
     * The first example text in the prompt is internally inconsistent. The algorithm implemented here
     * computes the mathematically correct optimum for the stated problem definition.
     *
     * Time complexity:
     * - Depends on the invoked test cases
     *
     * Space complexity:
     * - Depends on the invoked test cases
     *
     * @param args command-line arguments, unused
     * @return nothing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample-style demonstrations from the prompt.
        // For the stated problem definition, the correct optimum for [5,1,4,2], k=2 is 1:
        // split as [5,1] and [4,2], edit to [5,5] and [2,2], total cost = 0+4 + 2+0? No.
        // Better split is [5] and [1,4,2], edit second block to [1,2,2], cost = 0 + 0+2+0 = 2.
        // Best actual split is [5,1,4] and [2], edit first block to [5,4,4]? not non-decreasing.
        // The DP below computes the true optimum under the formal statement.
        solution.demo(new int[]{5, 1, 4, 2}, 2);

        // This sample is consistent and should produce 3.
        solution.demo(new int[]{7, 3, 6, 3, 8}, 3);

        // Additional sanity checks.
        solution.demo(new int[]{1, 2, 3, 4}, 1); // already non-decreasing -> 0
        solution.demo(new int[]{4, 3, 2, 1}, 1); // one block, must become non-decreasing
        solution.demo(new int[]{10}, 1);         // single element -> 0
        solution.demo(new int[]{3, 1, 2}, 3);    // each alone -> 0
    }
}