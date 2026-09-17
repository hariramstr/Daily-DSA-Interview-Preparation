import java.util.*;

/*
 * Title: Maximum Score from Picking Three Non-Overlapping Price Dips
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array prices where prices[i] represents the profit impact
 * of selecting day i in a promotional trading strategy. Values may be positive, zero,
 * or negative. You must choose exactly three non-empty contiguous subarrays, and the
 * three chosen subarrays must be pairwise non-overlapping. The score of a chosen
 * subarray is the sum of its elements, and the total strategy score is the sum of the
 * scores of the three chosen subarrays.
 *
 * Your task is to return the maximum possible total score.
 *
 * This is not the same as choosing any three individual elements: each choice must be
 * a contiguous block, and blocks may have different lengths. Because you must choose
 * exactly three subarrays, negative values cannot always be ignored. In particular,
 * if the array contains many negative numbers, the optimal answer may still include a
 * negative-sum segment in order to satisfy the requirement of selecting three
 * non-overlapping subarrays.
 *
 * Constraints:
 * - 3 <= prices.length <= 200000
 * - -10^9 <= prices[i] <= 10^9
 * - The answer fits in a signed 64-bit integer.
 *
 * Examples:
 * 1) prices = [4,-1,3,-2,5,-6,2,2]
 *    A valid optimal total is 15 using [4,-1,3], [5], [2,2].
 *
 * 2) prices = [-5,4,-1,4,-10,3]
 *    A valid optimal total is 10 using [4], [-1,4], [3].
 *
 * Efficient idea:
 * Use dynamic programming where:
 * - local[k]: best sum using exactly k subarrays, with the k-th subarray forced to end
 *   at the current index.
 * - global[k]: best sum using exactly k subarrays anywhere within the processed prefix.
 *
 * Transition for each value x:
 * - local[k] = max(local[k] + x, global[k - 1] + x)
 *   Meaning:
 *   1) extend the current k-th subarray with x
 *   2) start a new k-th subarray at current index, after finishing k-1 subarrays earlier
 *
 * - global[k] = max(global[k], local[k])
 *
 * This is the classic "maximum sum of k non-overlapping subarrays" DP optimized to O(n * k),
 * and here k = 3, so the runtime is linear in n.
 */
public class Solution {

    /**
     * Computes the maximum possible total score by selecting exactly three non-empty,
     * pairwise non-overlapping contiguous subarrays.
     *
     * The method uses dynamic programming with two arrays:
     * - local[k]: best score for exactly k subarrays where the last chosen subarray
     *   must end at the current index.
     * - global[k]: best score for exactly k subarrays anywhere in the prefix processed so far.
     *
     * Because we must choose exactly three subarrays, negative values are handled naturally.
     * The DP never "skips" the requirement; it always tracks exact counts.
     *
     * @param prices the input array where each value is the contribution of that day
     * @return the maximum total score obtainable by choosing exactly three non-overlapping contiguous subarrays
     * Time complexity: O(n), because k = 3 is constant and each element performs constant work.
     * Space complexity: O(1), because only fixed-size DP arrays of length 4 are used.
     */
    public long maximumScoreThreeSubarrays(int[] prices) {
        final int k = 3;

        // A very small sentinel is used to represent impossible states.
        // We avoid Long.MIN_VALUE directly because adding a number to it could overflow.
        final long NEG_INF = Long.MIN_VALUE / 4;

        // local[j]:
        // Best sum for exactly j subarrays, with the j-th subarray ending at the current index.
        long[] local = new long[k + 1];

        // global[j]:
        // Best sum for exactly j subarrays anywhere in the processed prefix.
        long[] global = new long[k + 1];

        // Initialization:
        // - 0 subarrays from an empty prefix has score 0.
        // - Any positive number of subarrays is impossible before processing elements.
        Arrays.fill(local, NEG_INF);
        Arrays.fill(global, NEG_INF);
        global[0] = 0;

        // Process each element one by one.
        for (int value : prices) {
            // We must update j from high to low.
            //
            // Why descending order?
            // Because local[j] depends on global[j - 1] from the PREVIOUS prefix state.
            // If we updated j from low to high, global[j - 1] might already include the
            // current element, which would incorrectly allow overlap.
            for (int j = k; j >= 1; j--) {
                // Option 1: extend an already-open j-th subarray that ended at the previous index.
                long extend = local[j] == NEG_INF ? NEG_INF : local[j] + value;

                // Option 2: start a brand-new j-th subarray at the current index.
                // This is only possible if we already had a valid solution with exactly j-1
                // subarrays somewhere before the current index.
                long startNew = global[j - 1] == NEG_INF ? NEG_INF : global[j - 1] + value;

                // The best way to force the j-th subarray to end exactly here.
                local[j] = Math.max(extend, startNew);

                // Now update the best overall answer for exactly j subarrays in the prefix.
                global[j] = Math.max(global[j], local[j]);
            }
        }

        return global[3];
    }

    /**
     * Convenience wrapper matching a common interview-style naming pattern.
     *
     * @param prices the input array of values
     * @return the maximum total score from exactly three non-overlapping non-empty contiguous subarrays
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long maxSumOfThreeNonOverlappingSubarrays(int[] prices) {
        return maximumScoreThreeSubarrays(prices);
    }

    /**
     * Runs a few demonstrations, including the examples from the problem statement.
     *
     * Note:
     * The first example's listed output says 13, but its own explanation shows a valid total of 15:
     * [4,-1,3] = 6, [5] = 5, [2,2] = 4, total = 15.
     * Therefore, the correct answer for Example 1 is 15.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(m * n) across all demo cases, where m is the number of demo arrays.
     * Space complexity: O(1) extra besides the input arrays.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] prices1 = {4, -1, 3, -2, 5, -6, 2, 2};
        long result1 = solution.maximumScoreThreeSubarrays(prices1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected based on the explanation: 15");

        int[] prices2 = {-5, 4, -1, 4, -10, 3};
        long result2 = solution.maximumScoreThreeSubarrays(prices2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 10");

        int[] prices3 = {-1, -2, -3};
        long result3 = solution.maximumScoreThreeSubarrays(prices3);
        System.out.println("All negative, exactly three subarrays required: " + result3);
        System.out.println("Expected: -6");

        int[] prices4 = {1, 2, 3, 4};
        long result4 = solution.maximumScoreThreeSubarrays(prices4);
        System.out.println("Positive array result: " + result4);
        System.out.println("One optimal choice is [1,2], [3], [4] => 10");

        int[] prices5 = {5, -100, 6, 7, -100, 8};
        long result5 = solution.maximumScoreThreeSubarrays(prices5);
        System.out.println("Separated peaks result: " + result5);
        System.out.println("One optimal choice is [5], [6,7], [8] => 26");
    }
}