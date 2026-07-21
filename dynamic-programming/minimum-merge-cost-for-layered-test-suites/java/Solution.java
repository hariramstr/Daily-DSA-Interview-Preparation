import java.util.*;

/*
Problem Title: Minimum Merge Cost for Layered Test Suites

Problem Description:
A build system stores automated tests as an ordered list of suites. The i-th suite has
execution weight tests[i]. To reduce startup overhead, the system must repeatedly merge
adjacent suites until exactly one suite remains. When you merge a contiguous block of
suites from index l to r into a single suite, the merge operation itself costs the total
execution weight of that block. However, the final merged suite is considered layered only
if every intermediate merge also combined adjacent groups; you may choose the merge order,
but you may never reorder suites.

Your task is to compute the minimum total cost required to merge all suites into one suite.

More formally, given an array tests of length n, each merge picks two already-formed
adjacent groups and combines them. If the left group covers [l..m] and the right group
covers [m+1..r], then the cost of that merge is sum(tests[l..r]). Return the minimum
possible total cost over all valid merge sequences.

This is not a greedy problem: choosing the cheapest local merge first may lead to a worse
overall answer. You must find the globally optimal parenthesization of merges.

Constraints:
- 1 <= n <= 400
- 1 <= tests[i] <= 10^9
- The answer fits in a 64-bit signed integer

Example 1:
Input: tests = [4, 1, 7, 3]
Output: 29
Explanation:
One optimal strategy is to merge [4,1] with cost 5, merge [7,3] with cost 10, then merge
the two resulting groups with cost 15. Total = 30.
But a better order is merge [1,7] with cost 8, then merge [4,(1,7)] with cost 12, then
[(4,1,7),3] with cost 15, for a total of 29.

Example 2:
Input: tests = [6, 2, 4]
Output: 18
Explanation:
Merge [2,4] first for cost 6, then merge [6,6] for cost 12. Total cost = 18.
Merging [6,2] first would cost 8, followed by 12, for a total of 20.
*/

public class Solution {

    /**
     * Computes the minimum total cost required to merge all adjacent test suites into one suite.
     *
     * Dynamic Programming idea:
     * Let dp[l][r] be the minimum cost to merge the subarray tests[l..r] into one group.
     *
     * Transition:
     * To merge tests[l..r], we must choose a final split point m where:
     * - tests[l..m] becomes one group
     * - tests[m+1..r] becomes one group
     * Then we merge those two adjacent groups together.
     *
     * So:
     * dp[l][r] = min over m in [l, r-1] of:
     *            dp[l][m] + dp[m+1][r] + sum(tests[l..r])
     *
     * Base case:
     * dp[i][i] = 0
     * A single suite is already one group, so no merge is needed.
     *
     * We use prefix sums so that sum(tests[l..r]) can be computed in O(1).
     *
     * @param tests the ordered list of suite execution weights
     * @return the minimum total merge cost
     * Time complexity: O(n^3)
     * Space complexity: O(n^2)
     */
    public long minimumMergeCost(int[] tests) {
        int n = tests.length;

        // If there is only one suite, no merge is needed.
        if (n <= 1) {
            return 0L;
        }

        // prefix[i] stores the sum of tests[0..i-1].
        // This allows us to compute any subarray sum quickly:
        // sum(l..r) = prefix[r + 1] - prefix[l]
        long[] prefix = buildPrefixSums(tests);

        // dp[l][r] = minimum cost to merge tests[l..r] into one suite.
        long[][] dp = new long[n][n];

        // We process intervals by increasing length.
        // Length 1 intervals are already solved: dp[i][i] = 0.
        for (int length = 2; length <= n; length++) {
            // Try every interval [left..right] of this length.
            for (int left = 0; left + length - 1 < n; left++) {
                int right = left + length - 1;

                // Start with a very large value because we are taking a minimum.
                dp[left][right] = Long.MAX_VALUE;

                // Total weight of the whole interval [left..right].
                // This is the cost paid for the final merge that combines
                // the left merged part and the right merged part.
                long totalWeight = rangeSum(prefix, left, right);

                // Try every possible final split point.
                // The last merge will combine:
                // - [left..mid]
                // - [mid+1..right]
                for (int mid = left; mid < right; mid++) {
                    long leftCost = dp[left][mid];
                    long rightCost = dp[mid + 1][right];

                    // Total cost if we choose this split:
                    // 1) optimally merge left half
                    // 2) optimally merge right half
                    // 3) merge the two resulting adjacent groups
                    long candidate = leftCost + rightCost + totalWeight;

                    if (candidate < dp[left][right]) {
                        dp[left][right] = candidate;
                    }
                }
            }
        }

        // The answer for the whole array is the cost to merge [0..n-1].
        return dp[0][n - 1];
    }

    /**
     * Builds prefix sums for the given array.
     *
     * prefix[0] = 0
     * prefix[i + 1] = tests[0] + tests[1] + ... + tests[i]
     *
     * @param tests the input array of suite weights
     * @return a prefix sum array of length tests.length + 1
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildPrefixSums(int[] tests) {
        long[] prefix = new long[tests.length + 1];
        for (int i = 0; i < tests.length; i++) {
            prefix[i + 1] = prefix[i] + tests[i];
        }
        return prefix;
    }

    /**
     * Returns the sum of tests[left..right], inclusive, using prefix sums.
     *
     * @param prefix the prefix sum array
     * @param left the left index of the range
     * @param right the right index of the range
     * @return the sum of the subarray from left to right inclusive
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long rangeSum(long[] prefix, int left, int right) {
        return prefix[right + 1] - prefix[left];
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding algorithm calls
     * Space complexity: O(1) for the demonstration itself, excluding algorithm memory
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] tests1 = {4, 1, 7, 3};
        long result1 = solution.minimumMergeCost(tests1);
        System.out.println("Input: " + Arrays.toString(tests1));
        System.out.println("Minimum merge cost: " + result1);
        System.out.println("Expected: 29");
        System.out.println();

        int[] tests2 = {6, 2, 4};
        long result2 = solution.minimumMergeCost(tests2);
        System.out.println("Input: " + Arrays.toString(tests2));
        System.out.println("Minimum merge cost: " + result2);
        System.out.println("Expected: 18");
        System.out.println();

        int[] tests3 = {5};
        long result3 = solution.minimumMergeCost(tests3);
        System.out.println("Input: " + Arrays.toString(tests3));
        System.out.println("Minimum merge cost: " + result3);
        System.out.println("Expected: 0");
    }
}