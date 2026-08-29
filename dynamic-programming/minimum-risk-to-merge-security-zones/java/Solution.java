import java.util.*;

/*
Problem Title: Minimum Risk to Merge Security Zones

Problem Description:
A company is consolidating a row of security zones in a data center. The zones are numbered from left to right, and zone i has a risk value risk[i]. To simplify monitoring, the company wants to repeatedly merge adjacent groups of zones until only one group remains.

If you merge two already-formed adjacent groups, the cost of that merge is equal to the sum of all risk values in the final combined group. However, not every merge order is allowed: a merge is valid only if at least one of the two groups being merged has total risk less than or equal to T. This rule models the requirement that at least one side of a merge must still be small enough to audit safely.

Return the minimum total cost to merge all zones into one group. If it is impossible to merge all zones while respecting the rule, return -1.

You may assume every zone starts as its own group, and each merge combines exactly two adjacent groups. The total cost is the sum of the costs of all performed merges.

Constraints:
- 1 <= n == risk.length <= 300
- 1 <= risk[i] <= 10^6
- 1 <= T <= 10^12

Example 1:
Input: risk = [4, 2, 7, 3], T = 6
Output: 32

Example 2:
Input: risk = [8, 9, 5], T = 6
Output: -1
*/

public class Solution {

    /**
     * Computes the minimum total cost to merge all security zones into one group.
     *
     * Core idea:
     * This is a classic interval dynamic programming problem with an extra validity rule
     * on the final merge of each interval.
     *
     * Let dp[l][r] be the minimum cost to fully merge the subarray risk[l..r] into one group,
     * while respecting the threshold rule for every merge performed inside that interval.
     *
     * Transition:
     * To merge interval [l..r], we choose a split point k:
     *   [l..k] | [k+1..r]
     *
     * If both sides can already be fully merged into one group, then we may perform the final
     * merge between those two groups only when:
     *   sum(l..k) <= T OR sum(k+1..r) <= T
     *
     * If valid, then:
     *   dp[l][r] = min(dp[l][r], dp[l][k] + dp[k+1][r] + sum(l..r))
     *
     * Base case:
     * A single zone is already one group, so cost is 0:
     *   dp[i][i] = 0
     *
     * If dp[0][n-1] remains unreachable, return -1.
     *
     * @param risk the risk values of the zones in left-to-right order
     * @param T the threshold such that a merge is allowed if at least one side has total risk <= T
     * @return the minimum total merge cost, or -1 if it is impossible to merge all zones
     * Time complexity: O(n^3), where n is risk.length
     * Space complexity: O(n^2)
     */
    public long minimumRiskToMerge(int[] risk, long T) {
        int n = risk.length;

        // Prefix sums allow us to compute any interval sum in O(1).
        // prefix[i] = sum of risk[0..i-1]
        long[] prefix = buildPrefixSums(risk);

        // Use a very large value to represent "impossible / not yet reachable".
        long INF = Long.MAX_VALUE / 4;

        // dp[l][r] = minimum cost to merge subarray [l..r] into one group.
        long[][] dp = new long[n][n];

        // Initialize all intervals as impossible first.
        for (int i = 0; i < n; i++) {
            Arrays.fill(dp[i], INF);
        }

        // Base case:
        // A single zone is already a single group, so no merge is needed.
        for (int i = 0; i < n; i++) {
            dp[i][i] = 0L;
        }

        // We process intervals by increasing length.
        // This guarantees that when computing dp[l][r], all smaller intervals
        // such as dp[l][k] and dp[k+1][r] are already known.
        for (int len = 2; len <= n; len++) {
            for (int l = 0; l + len - 1 < n; l++) {
                int r = l + len - 1;

                long totalSum = rangeSum(prefix, l, r);

                // Try every possible final split:
                // First fully merge [l..k], fully merge [k+1..r],
                // then perform one final merge between those two groups.
                for (int k = l; k < r; k++) {
                    // If either side cannot be formed into one group, skip this split.
                    if (dp[l][k] == INF || dp[k + 1][r] == INF) {
                        continue;
                    }

                    long leftSum = rangeSum(prefix, l, k);
                    long rightSum = rangeSum(prefix, k + 1, r);

                    // Validity rule for the final merge:
                    // at least one of the two adjacent groups must have total risk <= T.
                    if (leftSum <= T || rightSum <= T) {
                        long candidate = dp[l][k] + dp[k + 1][r] + totalSum;
                        if (candidate < dp[l][r]) {
                            dp[l][r] = candidate;
                        }
                    }
                }
            }
        }

        return dp[0][n - 1] == INF ? -1L : dp[0][n - 1];
    }

    /**
     * Builds prefix sums for the given risk array.
     *
     * Example:
     * risk = [4, 2, 7]
     * prefix = [0, 4, 6, 13]
     *
     * Then sum of interval [l..r] can be computed as:
     * prefix[r + 1] - prefix[l]
     *
     * @param risk the input risk array
     * @return prefix sum array of length risk.length + 1
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildPrefixSums(int[] risk) {
        long[] prefix = new long[risk.length + 1];
        for (int i = 0; i < risk.length; i++) {
            prefix[i + 1] = prefix[i] + risk[i];
        }
        return prefix;
    }

    /**
     * Returns the sum of risk[l..r] inclusive using the prefix sum array.
     *
     * @param prefix prefix sum array where prefix[i] stores sum of first i elements
     * @param l left index of the interval, inclusive
     * @param r right index of the interval, inclusive
     * @return the sum of the interval [l..r]
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long rangeSum(long[] prefix, int l, int r) {
        return prefix[r + 1] - prefix[l];
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for demonstration itself, excluding called algorithm
     * Space complexity: O(1) for demonstration itself, excluding called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] risk1 = {4, 2, 7, 3};
        long T1 = 6;
        long result1 = solution.minimumRiskToMerge(risk1, T1);
        System.out.println(result1); // Expected: 32

        int[] risk2 = {8, 9, 5};
        long T2 = 6;
        long result2 = solution.minimumRiskToMerge(risk2, T2);
        System.out.println(result2); // Expected: -1

        // Additional quick sanity checks
        int[] risk3 = {5};
        long T3 = 10;
        long result3 = solution.minimumRiskToMerge(risk3, T3);
        System.out.println(result3); // Expected: 0

        int[] risk4 = {1, 2, 3};
        long T4 = 100;
        long result4 = solution.minimumRiskToMerge(risk4, T4);
        System.out.println(result4); // One valid minimum result
    }
}