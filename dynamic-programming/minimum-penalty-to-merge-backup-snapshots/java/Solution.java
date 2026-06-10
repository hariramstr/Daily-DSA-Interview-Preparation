import java.util.*;

/*
Title: Minimum Penalty to Merge Backup Snapshots
Difficulty: Hard
Topic: Dynamic Programming

Problem Description:
A storage system keeps a sequence of backup snapshots in chronological order. Each snapshot has a positive integer size. To reduce metadata overhead, the system wants to repeatedly merge adjacent groups of snapshots until only one group remains.

When you merge a contiguous group of snapshots from index i to j into one archive, the merge incurs a penalty equal to the total size of that group plus a fragmentation surcharge. The fragmentation surcharge is defined as the difference between the maximum snapshot size and the minimum snapshot size within that same group. After the merge, that entire interval becomes a single archive whose size is the sum of all snapshot sizes in the interval, and future merges must still respect the original order of snapshots (that is, only adjacent groups can be merged).

Your task is to compute the minimum total penalty required to merge all snapshots into a single archive.

Formally, for any interval [i, j], merging it into one group costs:
(sum of sizes[i..j]) + (max(sizes[i..j]) - min(sizes[i..j]))

You may choose the order of merges, but every merge must combine two adjacent already-formed groups.

Return the minimum possible total penalty.

Constraints:
- 1 <= n <= 300
- 1 <= sizes[i] <= 10^6
- The answer can be large, so use 64-bit integers.

Important note about the examples:
The textual examples in the prompt are internally inconsistent with the stated merge rule.
Under the formal rule, the standard interval DP recurrence is:

dp[i][j] = min over k in [i, j-1] of
           dp[i][k] + dp[k+1][j] + cost(i, j)

where cost(i, j) = sum(i..j) + max(i..j) - min(i..j)

This is the correct interpretation because whenever two adjacent already-merged groups covering
[i..k] and [k+1..j] are merged, the newly formed interval is exactly [i..j], and that final merge
cost depends only on the full interval [i..j].

For example:
- sizes = [4, 2, 7]
  cost(0,1)=8, cost(1,2)=14, cost(0,2)=18
  dp(0,2)=min(0+14+18, 8+0+18)=26
  So the correct answer under the formal definition is 26.

- sizes = [5, 5, 5, 5]
  Every interval surcharge is 0, so this becomes the classic adjacent merge / file merge problem.
  The minimum total cost is 40, not 30.

This implementation follows the formal problem statement exactly and therefore produces correct
results for the mathematically defined problem.
*/

public class Solution {

    /**
     * Computes the minimum total penalty required to merge all snapshots into one archive.
     *
     * The dynamic programming idea is:
     * 1. Let dp[i][j] be the minimum cost to fully merge the subarray sizes[i..j] into one group.
     * 2. If i == j, no merge is needed, so dp[i][i] = 0.
     * 3. For any interval [i, j], the last operation must merge two already-merged adjacent groups:
     *      [i..k] and [k+1..j]
     *    Therefore:
     *      dp[i][j] = min(dp[i][k] + dp[k+1][j] + mergeCost(i, j))
     *    for all k from i to j - 1.
     * 4. We precompute:
     *      - prefix sums for interval sums
     *      - interval minimums
     *      - interval maximums
     *    so mergeCost(i, j) can be obtained in O(1).
     *
     * @param sizes the array of positive snapshot sizes
     * @return the minimum total penalty to merge the entire array into one archive
     * Time complexity: O(n^3)
     * Space complexity: O(n^2)
     */
    public long minimumPenalty(int[] sizes) {
        int n = sizes.length;

        // If there is only one snapshot, it is already a single group.
        // No merge is needed, so the total penalty is zero.
        if (n <= 1) {
            return 0L;
        }

        // ------------------------------------------------------------
        // Step 1: Prefix sums
        // ------------------------------------------------------------
        // prefix[i] will store the sum of the first i elements:
        // prefix[0] = 0
        // prefix[1] = sizes[0]
        // prefix[2] = sizes[0] + sizes[1]
        // ...
        //
        // Then sum of interval [l..r] is:
        // prefix[r + 1] - prefix[l]
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + sizes[i];
        }

        // ------------------------------------------------------------
        // Step 2: Precompute interval minimums and maximums
        // ------------------------------------------------------------
        // minVal[i][j] = minimum value in sizes[i..j]
        // maxVal[i][j] = maximum value in sizes[i..j]
        //
        // We fill these tables by expanding intervals from each starting point.
        int[][] minVal = new int[n][n];
        int[][] maxVal = new int[n][n];

        for (int i = 0; i < n; i++) {
            int currentMin = sizes[i];
            int currentMax = sizes[i];

            for (int j = i; j < n; j++) {
                currentMin = Math.min(currentMin, sizes[j]);
                currentMax = Math.max(currentMax, sizes[j]);
                minVal[i][j] = currentMin;
                maxVal[i][j] = currentMax;
            }
        }

        // ------------------------------------------------------------
        // Step 3: Precompute merge cost for every interval
        // ------------------------------------------------------------
        // mergeCost[i][j] = sum(i..j) + max(i..j) - min(i..j)
        //
        // This is the cost paid when the interval [i..j] is merged
        // in the final step that forms it as one archive.
        long[][] mergeCost = new long[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                long sum = prefix[j + 1] - prefix[i];
                mergeCost[i][j] = sum + (long) maxVal[i][j] - minVal[i][j];
            }
        }

        // ------------------------------------------------------------
        // Step 4: Interval DP
        // ------------------------------------------------------------
        // dp[i][j] = minimum cost to merge sizes[i..j] into one group
        long[][] dp = new long[n][n];

        // Base case:
        // dp[i][i] = 0 because a single snapshot is already one group.
        for (int i = 0; i < n; i++) {
            dp[i][i] = 0L;
        }

        // We process intervals by increasing length.
        // length = 2 means intervals of two elements
        // length = 3 means intervals of three elements
        // ...
        // length = n means the full array
        for (int length = 2; length <= n; length++) {
            for (int i = 0; i + length - 1 < n; i++) {
                int j = i + length - 1;

                // Start with a very large number because we want the minimum.
                long best = Long.MAX_VALUE;

                // Try every possible split point k:
                // left interval  = [i..k]
                // right interval = [k+1..j]
                //
                // If we choose this split, then:
                // 1. We must first fully merge the left side:  dp[i][k]
                // 2. We must first fully merge the right side: dp[k+1][j]
                // 3. Then we merge those two adjacent groups into [i..j]:
                //    cost = mergeCost[i][j]
                for (int k = i; k < j; k++) {
                    long candidate = dp[i][k] + dp[k + 1][j] + mergeCost[i][j];
                    if (candidate < best) {
                        best = candidate;
                    }
                }

                dp[i][j] = best;
            }
        }

        // The answer for the whole array is dp[0][n-1].
        return dp[0][n - 1];
    }

    /**
     * Returns the sum of the subarray sizes[left..right] using a prefix sum array.
     *
     * @param prefix the prefix sum array where prefix[i] is the sum of the first i elements
     * @param left the left index of the interval, inclusive
     * @param right the right index of the interval, inclusive
     * @return the sum of sizes[left..right]
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long rangeSum(long[] prefix, int left, int right) {
        return prefix[right + 1] - prefix[left];
    }

    /**
     * A small helper method to run one demonstration case and print the result.
     *
     * @param sizes the input snapshot sizes
     * @return the computed minimum penalty for the given input
     * Time complexity: O(n^3) because it calls the main solver
     * Space complexity: O(n^2) because it calls the main solver
     */
    public long demoCase(int[] sizes) {
        long answer = minimumPenalty(sizes);
        System.out.println("sizes = " + Arrays.toString(sizes));
        System.out.println("minimum penalty = " + answer);
        System.out.println();
        return answer;
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * Note:
     * The prompt's sample outputs conflict with the formal merge rule.
     * This main method prints the mathematically correct outputs under the stated definition.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) outside of the solver calls
     * Space complexity: O(1) outside of the solver calls
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Demonstration 1:
        // Under the formal rule:
        // [4,2,7] -> correct minimum is 26
        solution.demoCase(new int[]{4, 2, 7});

        // Demonstration 2:
        // Under the formal rule:
        // [5,5,5,5] -> correct minimum is 40
        solution.demoCase(new int[]{5, 5, 5, 5});

        // Additional quick checks
        solution.demoCase(new int[]{10});
        solution.demoCase(new int[]{3, 1});
        solution.demoCase(new int[]{1, 2, 3, 4});
    }
}