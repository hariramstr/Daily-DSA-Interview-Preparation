import java.util.*;

/*
 * Title: Maximum Score from Picking a Profitable Prefix and Suffix
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array profits where profits[i] represents the net profit
 * (which may be negative) of the i-th product in a catalog. A merchandising team
 * wants to build a promotion by choosing some number of products from the beginning
 * of the catalog and some number of products from the end of the catalog.
 * The chosen prefix and suffix must not overlap, but either side may be empty.
 *
 * Your task is to return the maximum total profit that can be obtained.
 *
 * Formally, choose indices such that you take profits[0..i] as a prefix and
 * profits[j..n-1] as a suffix, where i < j - 1 so the two chosen parts are disjoint.
 * You may also choose only a prefix, only a suffix, or choose nothing at all if every
 * option is unprofitable. The score is the sum of all selected values.
 *
 * This is not the same as choosing one contiguous subarray. You are selecting up to
 * two separated edge segments of the array.
 *
 * Constraints:
 * - 1 <= profits.length <= 200000
 * - -1000000000 <= profits[i] <= 1000000000
 * - The answer fits in a signed 64-bit integer.
 *
 * Example 1:
 * Input: profits = [4, -2, 3, -10, 5, 6]
 * Output: 16
 * Explanation: Take prefix [4, -2, 3] with sum 5 and suffix [5, 6] with sum 11.
 * They do not overlap, so the total is 16.
 *
 * Example 2:
 * Input: profits = [-5, 7, -3, 8, -2]
 * Output: 10
 * Explanation: The best choice is to take only the suffix [7, -3, 8, -2] with sum 10.
 * Taking both sides is worse because the selected parts must stay disjoint and edge-aligned.
 */

public class Solution {

    /**
     * Computes the maximum total profit obtainable by selecting:
     * 1) a prefix of the array,
     * 2) a suffix of the array,
     * 3) both a prefix and a suffix that do not overlap,
     * 4) or nothing at all.
     *
     * The key observation is:
     * - Any chosen prefix is fully determined by its ending index.
     * - Any chosen suffix is fully determined by its starting index.
     * - We need the best prefix ending at or before some position, and the best suffix
     *   starting at or after some later position, while keeping at least one index gap
     *   between them so they do not overlap.
     *
     * We build:
     * - prefixSum[i] = sum of profits[0..i]
     * - bestPrefixUpTo[i] = maximum value among:
     *     * 0 (choose no prefix)
     *     * prefixSum[0], prefixSum[1], ..., prefixSum[i]
     *
     * Similarly from the right:
     * - suffixSum[i] = sum of profits[i..n-1]
     * - bestSuffixFrom[i] = maximum value among:
     *     * 0 (choose no suffix)
     *     * suffixSum[i], suffixSum[i+1], ..., suffixSum[n-1]
     *
     * Then for every possible split between i and i+1:
     * - left side can use any prefix ending at or before i
     * - right side can use any suffix starting at or after i+1
     * Since those ranges are disjoint, their sums can be added safely.
     *
     * We also naturally cover:
     * - only prefix: because bestSuffixFrom[...] can be 0
     * - only suffix: because bestPrefixUpTo[...] can be 0
     * - choose nothing: because both sides can be 0
     *
     * @param profits the array of product profits, where each value may be positive or negative
     * @return the maximum total profit as a long
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long maximumScore(int[] profits) {
        int n = profits.length;

        // bestPrefixUpTo[i] will store the best profit obtainable by choosing
        // a prefix that ends somewhere in the range [0..i], or choosing no prefix at all.
        long[] bestPrefixUpTo = buildBestPrefixUpTo(profits);

        // bestSuffixFrom[i] will store the best profit obtainable by choosing
        // a suffix that starts somewhere in the range [i..n-1], or choosing no suffix at all.
        long[] bestSuffixFrom = buildBestSuffixFrom(profits);

        // Start with 0 because choosing nothing is always allowed.
        long answer = 0L;

        // Case 1: choose only a prefix (or nothing).
        // bestPrefixUpTo[n - 1] already includes the option 0.
        answer = Math.max(answer, bestPrefixUpTo[n - 1]);

        // Case 2: choose only a suffix (or nothing).
        // bestSuffixFrom[0] already includes the option 0.
        answer = Math.max(answer, bestSuffixFrom[0]);

        // Case 3: choose both a prefix and a suffix.
        //
        // We split the array between positions i and i+1.
        // - Left part available for prefix: indices [0..i]
        // - Right part available for suffix: indices [i+1..n-1]
        //
        // Because the prefix must lie entirely on the left and the suffix entirely on the right,
        // they are guaranteed not to overlap.
        for (int i = 0; i < n - 1; i++) {
            long candidate = bestPrefixUpTo[i] + bestSuffixFrom[i + 1];
            answer = Math.max(answer, candidate);
        }

        return answer;
    }

    /**
     * Builds an array where each position i stores the best prefix sum obtainable
     * using only indices up to i, with the option to choose no prefix at all.
     *
     * Example:
     * profits = [4, -2, 3, -10, 5, 6]
     * prefix sums are:
     * [4, 2, 5, -5, 0, 6]
     * bestPrefixUpTo becomes:
     * [4, 4, 5, 5, 5, 6]
     *
     * Notice that we also compare against 0 so that if all prefix sums are negative,
     * we can choose an empty prefix instead.
     *
     * @param profits the input profit array
     * @return an array bestPrefixUpTo where bestPrefixUpTo[i] is the maximum prefix profit
     *         using only the first i+1 elements, or 0 if taking no prefix is better
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildBestPrefixUpTo(int[] profits) {
        int n = profits.length;
        long[] bestPrefixUpTo = new long[n];

        // Running sum of the prefix from index 0 to current index i.
        long runningPrefixSum = 0L;

        // Track the best prefix sum seen so far, allowing 0 for "take nothing".
        long bestSoFar = 0L;

        for (int i = 0; i < n; i++) {
            // Extend the current prefix by including profits[i].
            runningPrefixSum += profits[i];

            // Update the best prefix sum seen so far.
            // We compare:
            // - previous best
            // - current full prefix sum ending exactly at i
            bestSoFar = Math.max(bestSoFar, runningPrefixSum);

            // Store the best result available up to this point.
            bestPrefixUpTo[i] = bestSoFar;
        }

        return bestPrefixUpTo;
    }

    /**
     * Builds an array where each position i stores the best suffix sum obtainable
     * using only indices from i to n-1, with the option to choose no suffix at all.
     *
     * Example:
     * profits = [4, -2, 3, -10, 5, 6]
     * suffix sums starting at each index are:
     * [6, 2, 4, 1, 11, 6]  // actually:
     * index 5 -> 6
     * index 4 -> 11
     * index 3 -> 1
     * index 2 -> 4
     * index 1 -> 2
     * index 0 -> 6
     *
     * bestSuffixFrom becomes:
     * [11, 11, 11, 11, 11, 6]
     *
     * This means:
     * - from index 0 onward, the best suffix is [5, 6] with sum 11
     * - from index 3 onward, the best suffix is still [5, 6] with sum 11
     * - from index 5 onward, the best suffix is [6] with sum 6
     *
     * We also compare against 0 so that if all suffix sums are negative,
     * we can choose an empty suffix instead.
     *
     * @param profits the input profit array
     * @return an array bestSuffixFrom where bestSuffixFrom[i] is the maximum suffix profit
     *         starting at some index in [i..n-1], or 0 if taking no suffix is better
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildBestSuffixFrom(int[] profits) {
        int n = profits.length;
        long[] bestSuffixFrom = new long[n];

        // Running sum of the suffix from current index i to the end.
        long runningSuffixSum = 0L;

        // Track the best suffix sum seen so far while scanning from right to left.
        // 0 means "choose no suffix".
        long bestSoFar = 0L;

        for (int i = n - 1; i >= 0; i--) {
            // Extend the current suffix by including profits[i].
            runningSuffixSum += profits[i];

            // Update the best suffix sum seen so far.
            bestSoFar = Math.max(bestSoFar, runningSuffixSum);

            // Store the best result available from this index onward.
            bestSuffixFrom[i] = bestSoFar;
        }

        return bestSuffixFrom;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * Expected outputs:
     * - For [4, -2, 3, -10, 5, 6], output should be 16
     * - For [-5, 7, -3, 8, -2], output should be 10
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstrations, excluding the called method costs
     * Space complexity: O(1) auxiliary, excluding the called method costs
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] profits1 = {4, -2, 3, -10, 5, 6};
        long result1 = solution.maximumScore(profits1);
        System.out.println("Input: " + Arrays.toString(profits1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 16");
        System.out.println();

        int[] profits2 = {-5, 7, -3, 8, -2};
        long result2 = solution.maximumScore(profits2);
        System.out.println("Input: " + Arrays.toString(profits2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 10");
        System.out.println();

        // Additional quick sanity checks for beginners:
        int[] profits3 = {-4, -1, -7};
        long result3 = solution.maximumScore(profits3);
        System.out.println("Input: " + Arrays.toString(profits3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        int[] profits4 = {5};
        long result4 = solution.maximumScore(profits4);
        System.out.println("Input: " + Arrays.toString(profits4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 5");
    }
}