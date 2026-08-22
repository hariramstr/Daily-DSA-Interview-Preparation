import java.util.*;

/*
 * Title: Minimum Penalty to Compress a Version History
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A software team stores the sizes of consecutive document revisions in an array sizes,
 * where sizes[i] is the size of the i-th saved version. To reduce storage, the team wants
 * to compress the full version history into exactly k archive blocks. Each archive block
 * must contain a contiguous range of versions.
 *
 * If versions from index l to r are placed into one archive block, then all versions in
 * that block are represented using the largest version size inside the block. The storage
 * penalty of that block is defined as:
 *
 * penalty(l, r) = (max(sizes[l..r]) * (r - l + 1)) - sum(sizes[l..r])
 *
 * In other words, every version in the block is padded up to the maximum size in that block,
 * and the penalty is the total extra space introduced by that padding.
 *
 * Your task is to return the minimum total penalty needed to partition the entire array into
 * exactly k contiguous archive blocks.
 *
 * If k > n, where n = sizes.length, then it is impossible to create exactly k non-empty blocks.
 *
 * Return the minimum total penalty, or -1 if the partition is impossible.
 *
 * Constraints:
 * - 1 <= n <= 400
 * - 1 <= sizes[i] <= 10^9
 * - 1 <= k <= 400
 * - Each archive block must be non-empty
 */

public class Solution {

    /**
     * Computes the minimum total penalty to partition the entire version history into exactly k
     * contiguous non-empty archive blocks.
     *
     * The algorithm works in two major phases:
     * 1. Precompute the penalty cost for every interval [l..r].
     * 2. Use dynamic programming where dp[parts][i] means:
     *    minimum penalty to partition the first i elements into exactly parts blocks.
     *
     * If k > n, partitioning into exactly k non-empty blocks is impossible, so return -1.
     *
     * @param sizes the array of version sizes
     * @param k the exact number of contiguous non-empty archive blocks required
     * @return the minimum total penalty, or -1 if such a partition is impossible
     * Time complexity: O(n^2 + k * n^2)
     * Space complexity: O(n^2 + k * n)
     */
    public long minimumPenalty(int[] sizes, int k) {
        int n = sizes.length;

        // If we need more non-empty blocks than elements, it is impossible.
        if (k > n) {
            return -1L;
        }

        // Precompute interval penalties so that later DP transitions can query
        // the cost of any block [l..r] in O(1) time.
        long[][] cost = precomputeCosts(sizes);

        // We use a large sentinel value to represent "unreachable".
        long inf = Long.MAX_VALUE / 4;

        // dp[parts][i] = minimum penalty to split the first i elements
        // (that is, indices 0..i-1) into exactly 'parts' blocks.
        long[][] dp = new long[k + 1][n + 1];

        // Initialize all states as unreachable.
        for (int parts = 0; parts <= k; parts++) {
            Arrays.fill(dp[parts], inf);
        }

        // Base case:
        // Zero elements split into zero blocks has zero penalty.
        dp[0][0] = 0L;

        // Build the answer block count by block count.
        for (int parts = 1; parts <= k; parts++) {

            // To split first i elements into 'parts' non-empty blocks,
            // we must have at least 'parts' elements.
            for (int i = parts; i <= n; i++) {

                // We try every possible starting point of the last block.
                // Suppose the last block is [j..i-1].
                // Then the first j elements must be split into parts-1 blocks.
                //
                // j must be at least parts-1, because we need enough elements
                // to form parts-1 non-empty blocks before the last block.
                for (int j = parts - 1; j < i; j++) {
                    if (dp[parts - 1][j] == inf) {
                        continue;
                    }

                    long candidate = dp[parts - 1][j] + cost[j][i - 1];
                    if (candidate < dp[parts][i]) {
                        dp[parts][i] = candidate;
                    }
                }
            }
        }

        return dp[k][n] >= inf ? -1L : dp[k][n];
    }

    /**
     * Precomputes the penalty for every interval [l..r].
     *
     * For each fixed left boundary l, we extend the right boundary r step by step,
     * maintaining:
     * - the maximum value in sizes[l..r]
     * - the sum of values in sizes[l..r]
     *
     * Then:
     * cost[l][r] = max(sizes[l..r]) * length - sum(sizes[l..r])
     *
     * This allows later DP transitions to access interval penalties in O(1).
     *
     * @param sizes the array of version sizes
     * @return a 2D array where result[l][r] is the penalty of compressing sizes[l..r] into one block
     * Time complexity: O(n^2)
     * Space complexity: O(n^2)
     */
    public long[][] precomputeCosts(int[] sizes) {
        int n = sizes.length;
        long[][] cost = new long[n][n];

        // For every possible left boundary...
        for (int l = 0; l < n; l++) {
            long max = 0L;
            long sum = 0L;

            // ...expand the interval to the right one element at a time.
            for (int r = l; r < n; r++) {
                max = Math.max(max, sizes[r]);
                sum += sizes[r];

                long length = r - l + 1L;
                cost[l][r] = max * length - sum;
            }
        }

        return cost;
    }

    /**
     * A small helper method to print an input array in a readable format.
     *
     * @param arr the array to convert to string
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on sample-style inputs and prints the results.
     *
     * Note:
     * The problem statement's first example contains inconsistent intermediate explanations,
     * but the correct minimum for sizes = [5, 2, 4, 6, 3], k = 2 is 5.
     * One optimal partition is [5, 2, 4] and [6, 3]:
     * - penalty = (5*3 - 11) + (6*2 - 9) = 4 + 3 = 7
     * Another partition [5, 2, 4, 6] and [3]:
     * - penalty = 24 - 17 + 0 = 7
     * The true optimal partition is [5] and [2, 4, 6, 3]:
     * - penalty = 0 + (6*4 - 15) = 9
     * Also not optimal.
     * The actual best split is [5, 2, 4, 6, 3] into 2 blocks at index 1:
     * [5, 2] => 5*2 - 7 = 3
     * [4, 6, 3] => 6*3 - 13 = 5
     * total = 8
     * However, checking all splits:
     * split after 0: 0 + 9 = 9
     * split after 1: 3 + 5 = 8
     * split after 2: 4 + 3 = 7
     * split after 3: 7 + 0 = 7
     * So the true minimum for that example is 7, not 5.
     *
     * Because correctness is mandatory, this program prints the mathematically correct result.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding called methods
     * Space complexity: O(1) for the demonstration itself, excluding called methods
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] sizes1 = {5, 2, 4, 6, 3};
        int k1 = 2;
        long result1 = solution.minimumPenalty(sizes1, k1);
        System.out.println("sizes = " + solution.arrayToString(sizes1) + ", k = " + k1);
        System.out.println("Minimum penalty = " + result1);

        int[] sizes2 = {8, 8, 8, 8};
        int k2 = 3;
        long result2 = solution.minimumPenalty(sizes2, k2);
        System.out.println("sizes = " + solution.arrayToString(sizes2) + ", k = " + k2);
        System.out.println("Minimum penalty = " + result2);

        int[] sizes3 = {10, 1, 10, 1};
        int k3 = 2;
        long result3 = solution.minimumPenalty(sizes3, k3);
        System.out.println("sizes = " + solution.arrayToString(sizes3) + ", k = " + k3);
        System.out.println("Minimum penalty = " + result3);

        int[] sizes4 = {7, 3, 9};
        int k4 = 5;
        long result4 = solution.minimumPenalty(sizes4, k4);
        System.out.println("sizes = " + solution.arrayToString(sizes4) + ", k = " + k4);
        System.out.println("Minimum penalty = " + result4);
    }
}