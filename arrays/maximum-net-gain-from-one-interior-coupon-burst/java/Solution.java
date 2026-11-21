/*
 * Title: Maximum Net Gain from One Interior Coupon Burst
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array purchases where purchases[i] represents the net profit
 * contributed by the i-th customer order during a day. Values may be positive, zero, or negative.
 * The marketing team can run exactly one special coupon burst on a contiguous block of orders,
 * but the burst is only allowed on an interior block: it cannot include the first order or the
 * last order of the day.
 *
 * If the team chooses a block purchases[l..r] with 1 <= l <= r <= n - 2, the campaign score is:
 *
 *     sum(purchases[l..r]) - purchases[l - 1] - purchases[r + 1]
 *
 * Your task is to return the maximum possible campaign score over all valid interior blocks.
 * The chosen block must be non-empty, and the array must be long enough to have at least one
 * valid interior block.
 *
 * Constraints:
 * - 3 <= purchases.length <= 200000
 * - -1000000000 <= purchases[i] <= 1000000000
 * - The answer fits in a signed 64-bit integer.
 *
 * Example 1:
 * Input: purchases = [4, 2, 7, -3, 5]
 * Output: 8
 * Explanation: Choose the interior block [7]. Its score is 7 - 2 - (-3) = 8.
 *
 * Example 2:
 * Input: purchases = [3, -5, 6, 1, -2, 4]
 * Output: 10
 * Explanation: Choose the interior block [6, 1]. Its score is (6 + 1) - (-5) - (-2) = 10.
 */

import java.util.*;

public class Solution {

    /**
     * Computes the maximum campaign score over all valid non-empty interior contiguous blocks.
     *
     * Core transformation:
     * For a chosen block [l..r], score is:
     *     sum(purchases[l..r]) - purchases[l - 1] - purchases[r + 1]
     *
     * Rewrite the score as:
     *     (-purchases[l - 1]) + purchases[l] + purchases[l + 1] + ... + purchases[r] + (-purchases[r + 1])
     *
     * This suggests a dynamic programming scan:
     * - When a block starts at index l, its initial value is purchases[l] - purchases[l - 1].
     * - Extending the block by one element to the right adds purchases[r].
     * - When evaluating a block ending at r, we subtract purchases[r + 1].
     *
     * We maintain:
     * - bestEndingHere: the maximum value of (sum of current interior block) - left neighbor penalty,
     *   among all valid blocks that end at the current index.
     * - answer: the best full score seen so far after also subtracting the right neighbor penalty.
     *
     * Important index rules:
     * - l must be at least 1
     * - r must be at most n - 2
     * Therefore, while scanning current end index i, we only consider i in [1, n - 2].
     * To finalize a block ending at i, we subtract purchases[i + 1], which always exists.
     *
     * @param purchases the array of order profits/losses
     * @return the maximum possible campaign score as a long
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long maximumNetGain(int[] purchases) {
        validateInput(purchases);

        int n = purchases.length;

        // bestEndingHere represents the best value of:
        //     sum(purchases[l..i]) - purchases[l - 1]
        // for any valid starting index l where 1 <= l <= i.
        //
        // We initialize it for i = 1, because index 1 is the first possible interior start/end.
        // For block [1..1], the value is:
        //     purchases[1] - purchases[0]
        long bestEndingHere = (long) purchases[1] - purchases[0];

        // To convert a "block ending at i" candidate into the full score,
        // we must also subtract the right outside neighbor purchases[i + 1].
        //
        // For i = 1, full score is:
        //     (purchases[1] - purchases[0]) - purchases[2]
        long answer = bestEndingHere - purchases[2];

        // We now scan possible ending positions i from 2 to n - 2.
        // Each i is a valid interior end because it still has a right neighbor at i + 1.
        for (int i = 2; i <= n - 2; i++) {
            // There are exactly two ways to form the best block ending at i:
            //
            // 1) Start a brand-new block at i:
            //      purchases[i] - purchases[i - 1]
            //    because the left outside neighbor is purchases[i - 1].
            //
            // 2) Extend the best block that ended at i - 1:
            //      bestEndingHere + purchases[i]
            //    because adding purchases[i] extends the block sum by one element.
            //
            // We choose whichever is larger.
            long startNew = (long) purchases[i] - purchases[i - 1];
            long extend = bestEndingHere + purchases[i];
            bestEndingHere = Math.max(startNew, extend);

            // Now bestEndingHere equals:
            //     max over all valid l of (sum(purchases[l..i]) - purchases[l - 1])
            //
            // To get the actual campaign score for a block ending at i,
            // we must subtract the right outside neighbor purchases[i + 1].
            long fullScoreEndingAtI = bestEndingHere - purchases[i + 1];

            // Update the global answer.
            answer = Math.max(answer, fullScoreEndingAtI);
        }

        return answer;
    }

    /**
     * Validates that the input array is non-null and long enough to contain at least one
     * non-empty interior block.
     *
     * @param purchases the input array to validate
     * @return nothing; throws an exception if the input is invalid
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void validateInput(int[] purchases) {
        if (purchases == null) {
            throw new IllegalArgumentException("Input array must not be null.");
        }
        if (purchases.length < 3) {
            throw new IllegalArgumentException("Array length must be at least 3 to have an interior block.");
        }
    }

    /**
     * Helper method to print an array in a beginner-friendly way.
     *
     * @param purchases the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] purchases) {
        return Arrays.toString(purchases);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstration call
     * Space complexity: O(1) extra, excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] purchases1 = {4, 2, 7, -3, 5};
        long result1 = solution.maximumNetGain(purchases1);
        System.out.println("Input: purchases = " + solution.arrayToString(purchases1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 8");
        System.out.println();

        int[] purchases2 = {3, -5, 6, 1, -2, 4};
        long result2 = solution.maximumNetGain(purchases2);
        System.out.println("Input: purchases = " + solution.arrayToString(purchases2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 10");
        System.out.println();

        int[] purchases3 = {1, 2, 3};
        long result3 = solution.maximumNetGain(purchases3);
        System.out.println("Input: purchases = " + solution.arrayToString(purchases3));
        System.out.println("Output: " + result3);
        System.out.println("Explanation: Only interior block is [2], score = 2 - 1 - 3 = -2");
    }
}