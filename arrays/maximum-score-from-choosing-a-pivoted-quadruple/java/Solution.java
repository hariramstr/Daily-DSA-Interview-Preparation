import java.util.*;

/*
Problem Title: Maximum Score from Choosing a Pivoted Quadruple

Problem Description:
You are given an integer array nums of length n. A pivoted quadruple is a choice of four indices
(a, b, c, d) such that 0 <= a < b < c < d < n and b and c act as the two middle anchors of the quadruple.
The score of such a quadruple is defined as:

(nums[a] - nums[b]) * (nums[c] - nums[d])

Your task is to return the maximum possible score over all valid pivoted quadruples. If every possible
quadruple has a negative score, you must still return the largest value among them. It is guaranteed that n >= 4.

A brute-force O(n^4) solution is far too slow. The challenge is to exploit the structure of the expression:
the left pair contributes a difference using indices before and at b, while the right pair contributes a
difference using c and after c. An efficient solution should precompute the best possible left-side and
right-side contributions and combine them carefully while preserving index ordering.

Constraints:
- 4 <= n <= 200000
- -1000000000 <= nums[i] <= 1000000000
- The answer fits in a signed 64-bit integer
*/
public class Solution {

    /**
     * Computes the maximum score over all valid quadruples (a, b, c, d) such that:
     * a < b < c < d
     * and score = (nums[a] - nums[b]) * (nums[c] - nums[d]).
     *
     * Core idea:
     * For each possible middle split between b and c:
     * - The left factor depends only on b and elements before it:
     *     leftValue(b) = max over a < b of (nums[a] - nums[b])
     * - The right factor depends only on c and elements after it:
     *     rightValue(c) can be either:
     *         max over d > c of (nums[c] - nums[d])
     *     or
     *         min over d > c of (nums[c] - nums[d])
     *
     * Why both max and min on the right?
     * Because when multiplying, the best product for a fixed left value depends on the sign:
     * - If left is positive, we want the largest right.
     * - If left is negative, we want the smallest right (most negative), because
     *   negative * negative can become a large positive.
     *
     * Therefore, for every valid c, we precompute:
     * - suffixMaxDiff[c] = max over d > c of (nums[c] - nums[d])
     * - suffixMinDiff[c] = min over d > c of (nums[c] - nums[d])
     *
     * Then we sweep b from left to right while maintaining the maximum value seen so far
     * for nums[a], which allows us to compute:
     * - leftDiff = max over a < b of (nums[a] - nums[b]) = prefixMaxBeforeB - nums[b]
     *
     * Since c must satisfy c > b, the smallest valid c is b + 1.
     * For each b, we pair leftDiff with the precomputed right information at c = b + 1, b + 2, ...
     * But we still need the best possible c among all c > b.
     *
     * So we additionally precompute:
     * - best product obtainable from position c onward for a positive left factor
     * - best product obtainable from position c onward for a negative left factor
     *
     * More concretely:
     * For any leftDiff L and any c > b, the best score using that c is:
     * - L * suffixMaxDiff[c], if L >= 0
     * - L * suffixMinDiff[c], if L < 0
     *
     * Thus we can precompute:
     * - bestRightForPositiveStart[i] = max over c >= i of suffixMaxDiff[c]
     * - bestRightForNegativeStart[i] = min over c >= i of suffixMinDiff[c]
     *
     * Then for each b:
     * - if leftDiff >= 0, best score = leftDiff * bestRightForPositiveStart[b + 1]
     * - else,              best score = leftDiff * bestRightForNegativeStart[b + 1]
     *
     * This yields an O(n) solution.
     *
     * @param nums the input integer array
     * @return the maximum possible score among all valid pivoted quadruples
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long maximumScore(int[] nums) {
        int n = nums.length;

        // suffixMaxDiff[c] = maximum value of (nums[c] - nums[d]) for any d > c
        long[] suffixMaxDiff = new long[n];

        // suffixMinDiff[c] = minimum value of (nums[c] - nums[d]) for any d > c
        long[] suffixMinDiff = new long[n];

        // We build these arrays by scanning from right to left.
        // For a fixed c, the best/worst d > c depends on the minimum/maximum value to the right.
        long minToRight = nums[n - 1];
        long maxToRight = nums[n - 1];

        // The last index cannot serve as c because there is no d > c.
        // We fill it with neutral placeholders; it will never be used directly.
        suffixMaxDiff[n - 1] = Long.MIN_VALUE;
        suffixMinDiff[n - 1] = Long.MAX_VALUE;

        for (int c = n - 2; c >= 0; c--) {
            // Maximum of nums[c] - nums[d] occurs when nums[d] is as small as possible.
            suffixMaxDiff[c] = (long) nums[c] - minToRight;

            // Minimum of nums[c] - nums[d] occurs when nums[d] is as large as possible.
            suffixMinDiff[c] = (long) nums[c] - maxToRight;

            // Update the running min/max for future positions to the left.
            minToRight = Math.min(minToRight, nums[c]);
            maxToRight = Math.max(maxToRight, nums[c]);
        }

        // bestRightForPositiveStart[i]:
        // among all c >= i, what is the largest possible right factor (nums[c] - nums[d])?
        // This is what we want when the left factor is non-negative.
        long[] bestRightForPositiveStart = new long[n];

        // bestRightForNegativeStart[i]:
        // among all c >= i, what is the smallest possible right factor (nums[c] - nums[d])?
        // This is what we want when the left factor is negative.
        long[] bestRightForNegativeStart = new long[n];

        bestRightForPositiveStart[n - 1] = Long.MIN_VALUE;
        bestRightForNegativeStart[n - 1] = Long.MAX_VALUE;

        // Build suffix aggregates over c.
        for (int i = n - 2; i >= 0; i--) {
            bestRightForPositiveStart[i] = Math.max(suffixMaxDiff[i], bestRightForPositiveStart[i + 1]);
            bestRightForNegativeStart[i] = Math.min(suffixMinDiff[i], bestRightForNegativeStart[i + 1]);
        }

        long answer = Long.MIN_VALUE;

        // prefixMax stores the maximum nums[a] seen so far for indices a < current b.
        long prefixMax = nums[0];

        // b must leave room for c and d, so b can range from 1 to n - 3.
        for (int b = 1; b <= n - 3; b++) {
            // Best possible left factor for this b:
            // choose the largest nums[a] among all a < b.
            long leftDiff = prefixMax - nums[b];

            // c must be at least b + 1.
            int cStart = b + 1;

            long candidate;
            if (leftDiff >= 0) {
                // Positive (or zero) left factor:
                // maximize the right factor.
                candidate = leftDiff * bestRightForPositiveStart[cStart];
            } else {
                // Negative left factor:
                // minimize the right factor to make the product as large as possible.
                candidate = leftDiff * bestRightForNegativeStart[cStart];
            }

            answer = Math.max(answer, candidate);

            // Update prefix maximum for the next b.
            prefixMax = Math.max(prefixMax, nums[b]);
        }

        return answer;
    }

    /**
     * A simple brute-force verifier for small arrays.
     * This is not used by the main algorithm, but it is useful for demonstration and sanity checks.
     *
     * @param nums the input integer array
     * @return the exact maximum score computed by checking all valid quadruples
     * Time complexity: O(n^4)
     * Space complexity: O(1)
     */
    public long maximumScoreBruteForce(int[] nums) {
        int n = nums.length;
        long best = Long.MIN_VALUE;

        for (int a = 0; a < n; a++) {
            for (int b = a + 1; b < n; b++) {
                for (int c = b + 1; c < n; c++) {
                    for (int d = c + 1; d < n; d++) {
                        long score = ((long) nums[a] - nums[b]) * ((long) nums[c] - nums[d]);
                        best = Math.max(best, score);
                    }
                }
            }
        }

        return best;
    }

    /**
     * Utility method to print an array in a readable form.
     *
     * @param nums the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] nums) {
        return Arrays.toString(nums);
    }

    /**
     * Demonstrates the solution on sample-style inputs and a few additional checks.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size of demonstrated arrays)
     * Space complexity: O(n) per call to the main algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {8, 1, 9, 2, 7};
        long result1 = solution.maximumScore(nums1);
        long brute1 = solution.maximumScoreBruteForce(nums1);
        System.out.println("Input:  " + solution.arrayToString(nums1));
        System.out.println("Fast:   " + result1);
        System.out.println("Brute:  " + brute1);
        System.out.println();

        int[] nums2 = {5, 10, 3, 8, 1, 6};
        long result2 = solution.maximumScore(nums2);
        long brute2 = solution.maximumScoreBruteForce(nums2);
        System.out.println("Input:  " + solution.arrayToString(nums2));
        System.out.println("Fast:   " + result2);
        System.out.println("Brute:  " + brute2);
        System.out.println();

        int[] nums3 = {8, 1, 9, 1, 2};
        long result3 = solution.maximumScore(nums3);
        long brute3 = solution.maximumScoreBruteForce(nums3);
        System.out.println("Input:  " + solution.arrayToString(nums3));
        System.out.println("Fast:   " + result3);
        System.out.println("Brute:  " + brute3);
        System.out.println();

        int[] nums4 = {-5, -1, -10, 7, -3, 2};
        long result4 = solution.maximumScore(nums4);
        long brute4 = solution.maximumScoreBruteForce(nums4);
        System.out.println("Input:  " + solution.arrayToString(nums4));
        System.out.println("Fast:   " + result4);
        System.out.println("Brute:  " + brute4);
    }
}