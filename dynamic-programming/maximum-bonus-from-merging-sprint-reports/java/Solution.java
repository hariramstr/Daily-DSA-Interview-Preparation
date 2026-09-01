import java.util.*;

/*
Problem Title: Maximum Bonus from Merging Sprint Reports

Problem Description:
A product team tracks daily engineering output as an array of integers reports,
where reports[i] is the score recorded on day i. To prepare a quarterly review,
the manager wants to compress the timeline into several consecutive sprint summaries.

If you choose a subarray from index l to r as one sprint summary, its bonus is:

(sum of reports[l..r]) * (length of the sprint)

You must partition the entire array into one or more contiguous, non-empty sprint summaries.
Every day must belong to exactly one summary, and summaries cannot overlap or be reordered.

Return the maximum total bonus obtainable.

In other words, split the array into contiguous blocks, compute sum(block) * size(block)
for each block, and maximize the sum of these values.

This is a dynamic programming problem because the best partition ending at a position
depends on the best partitions of all earlier prefixes.

Constraints:
- 1 <= reports.length <= 2000
- -10^4 <= reports[i] <= 10^4
- The answer fits in a signed 64-bit integer.

Examples:
1) reports = [3, -1, 2]
   Whole array bonus = (3 + -1 + 2) * 3 = 4 * 3 = 12
   This is optimal, so answer = 12

2) reports = [4, -5, 6, 1]
   Whole array bonus = (4 + -5 + 6 + 1) * 4 = 6 * 4 = 24
   This is optimal, so answer = 24
*/

public class Solution {

    /**
     * Computes the maximum total bonus obtainable by partitioning the array into
     * contiguous non-empty blocks, where each block contributes:
     * (sum of block) * (length of block).
     *
     * Dynamic Programming idea:
     * Let dp[i] be the maximum bonus we can obtain using the first i elements
     * (that is, reports[0..i-1]).
     *
     * To compute dp[i], we try every possible last block:
     * - Suppose the last block starts at index j and ends at index i - 1.
     * - Then the prefix before that block is reports[0..j-1], whose best value is dp[j].
     * - The last block value is:
     *     (sum of reports[j..i-1]) * (i - j)
     * - Therefore:
     *     dp[i] = max over all j in [0, i-1] of:
     *             dp[j] + sum(reports[j..i-1]) * (i - j)
     *
     * We use prefix sums so that any subarray sum can be computed in O(1).
     *
     * @param reports the daily engineering output scores
     * @return the maximum total bonus over all valid contiguous partitions
     * Time complexity: O(n^2), where n is reports.length
     * Space complexity: O(n)
     */
    public long maximumBonus(int[] reports) {
        int n = reports.length;

        // prefix[i] will store the sum of the first i elements:
        // prefix[0] = 0
        // prefix[1] = reports[0]
        // prefix[2] = reports[0] + reports[1]
        // ...
        // This allows us to compute sum of reports[l..r] as:
        // prefix[r + 1] - prefix[l]
        long[] prefix = buildPrefixSums(reports);

        // dp[i] = maximum bonus for the first i elements (reports[0..i-1]).
        long[] dp = new long[n + 1];

        // We initialize all states except dp[0] to a very small value,
        // because we are taking maximums and values can be negative.
        Arrays.fill(dp, Long.MIN_VALUE / 4);

        // Base case:
        // Using zero elements gives zero bonus.
        dp[0] = 0L;

        // We now build answers for prefixes of length 1, 2, 3, ..., n.
        for (int i = 1; i <= n; i++) {

            // We want to compute dp[i].
            // Try every possible starting point j of the last block.
            // The last block is reports[j..i-1].
            for (int j = 0; j < i; j++) {

                // Compute the sum of the last block reports[j..i-1].
                long blockSum = prefix[i] - prefix[j];

                // Length of the block is number of elements from j to i-1.
                long blockLength = i - j;

                // Bonus contributed by this last block.
                long blockBonus = blockSum * blockLength;

                // Total if we end with this block:
                // best for first j elements + bonus of last block
                long candidate = dp[j] + blockBonus;

                // Keep the best possible value.
                if (candidate > dp[i]) {
                    dp[i] = candidate;
                }
            }
        }

        // dp[n] is the answer for the entire array.
        return dp[n];
    }

    /**
     * Builds prefix sums for the given array.
     *
     * prefix[i] stores the sum of the first i elements.
     * Therefore:
     * - prefix[0] = 0
     * - prefix[1] = reports[0]
     * - prefix[2] = reports[0] + reports[1]
     * and so on.
     *
     * This makes subarray sum queries very easy:
     * sum of reports[l..r] = prefix[r + 1] - prefix[l]
     *
     * @param reports the input array
     * @return a prefix sum array of length reports.length + 1
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildPrefixSums(int[] reports) {
        long[] prefix = new long[reports.length + 1];
        for (int i = 0; i < reports.length; i++) {
            prefix[i + 1] = prefix[i] + reports[i];
        }
        return prefix;
    }

    /**
     * A small helper method to print an array in a beginner-friendly way.
     *
     * @param reports the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] reports) {
        return Arrays.toString(reports);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Important correctness check against the examples:
     * - For [3, -1, 2], the correct answer is 12
     * - For [4, -5, 6, 1], the correct answer is 24
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo calls, excluding the algorithm calls
     * Space complexity: O(1), excluding the algorithm's own memory usage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] reports1 = {3, -1, 2};
        long answer1 = solution.maximumBonus(reports1);
        System.out.println("Input: " + solution.arrayToString(reports1));
        System.out.println("Maximum bonus: " + answer1);
        System.out.println("Expected: 12");
        System.out.println();

        int[] reports2 = {4, -5, 6, 1};
        long answer2 = solution.maximumBonus(reports2);
        System.out.println("Input: " + solution.arrayToString(reports2));
        System.out.println("Maximum bonus: " + answer2);
        System.out.println("Expected: 24");
        System.out.println();

        int[] reports3 = {-5};
        long answer3 = solution.maximumBonus(reports3);
        System.out.println("Input: " + solution.arrayToString(reports3));
        System.out.println("Maximum bonus: " + answer3);
        System.out.println();

        int[] reports4 = {1, 2, 3};
        long answer4 = solution.maximumBonus(reports4);
        System.out.println("Input: " + solution.arrayToString(reports4));
        System.out.println("Maximum bonus: " + answer4);
        System.out.println();

        int[] reports5 = {5, -10, 5};
        long answer5 = solution.maximumBonus(reports5);
        System.out.println("Input: " + solution.arrayToString(reports5));
        System.out.println("Maximum bonus: " + answer5);
    }
}