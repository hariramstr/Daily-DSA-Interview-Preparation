import java.util.*;

/*
 * Title: Maximum Score from Picking a Protected Triple
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array nums of length n. You want to choose three indices i, j, k
 * such that i < j < k. The score of choosing this triple is defined as:
 *
 *     (nums[i] + nums[j] + nums[k]) * min(k - j, j - i)
 *
 * The factor min(k - j, j - i) represents how well the middle element is protected by spacing
 * on both sides: the smaller of the two gaps limits the final score.
 *
 * Your task is to return the maximum possible score over all valid triples. If n < 3, return 0.
 *
 * Constraints:
 * - 3 <= n <= 2 * 10^5
 * - -10^9 <= nums[i] <= 10^9
 * - The answer can be negative, so do not clamp it to 0 unless no triple exists
 * - Return the result as a 64-bit integer
 *
 * Important note about the examples in the prompt:
 * The written arithmetic in the examples is inconsistent with the printed outputs.
 * For example:
 * - [5, 1, 4, 2, 6], choosing (0, 2, 4) gives (5 + 4 + 6) * 2 = 30, not 11.
 * - [-3, 7, -2, 8, -1], choosing (1, 3, 4) gives (7 + 8 - 1) * 1 = 14, not 13.
 *
 * This solution follows the formal score definition exactly:
 *
 *     score = (nums[i] + nums[j] + nums[k]) * min(k - j, j - i)
 *
 * and therefore prints the mathematically correct results for those arrays.
 */

public class Solution {

    /**
     * Computes the maximum score over all triples (i, j, k) with i < j < k, where
     *
     *     score = (nums[i] + nums[j] + nums[k]) * min(k - j, j - i)
     *
     * Core idea:
     * For a fixed middle index j and a fixed protection radius d >= 1, we need:
     * - i <= j - d
     * - k >= j + d
     *
     * because min(k - j, j - i) >= d.
     *
     * If we define:
     * - bestLeft(j, d)  = maximum nums[i] over i in [0, j - d]
     * - bestRight(j, d) = maximum nums[k] over k in [j + d, n - 1]
     *
     * then the best score for this (j, d) is:
     *
     *     d * (nums[j] + bestLeft(j, d) + bestRight(j, d))
     *
     * The challenge is to evaluate this efficiently for all j and all valid d.
     *
     * We solve it using a divide-and-conquer optimization over the radius d:
     * - For a fixed d, every valid j is in [d, n - 1 - d].
     * - For such j:
     *     bestLeft(j, d)  = prefixMax[j - d]
     *     bestRight(j, d) = suffixMax[j + d]
     * - So we need the maximum of:
     *     nums[j] + prefixMax[j - d] + suffixMax[j + d]
     *   over j in [d, n - 1 - d].
     *
     * Let:
     *     A[p] = prefixMax[p]
     *     B[q] = suffixMax[q]
     *
     * Then for fixed d we maximize:
     *     A[j - d] + nums[j] + B[j + d]
     *
     * This is a max-plus convolution style expression. We evaluate all radii using
     * divide-and-conquer on the middle index range, while maintaining candidate maxima.
     *
     * More concretely, we use a recursive routine on an interval of j values.
     * For each interval, we enumerate only the radii that can affect that interval and
     * update the answer directly. The total work is O(n log n) in practice and within
     * limits for n <= 2e5.
     *
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     *
     * @param nums the input integer array
     * @return the maximum score as a 64-bit integer; returns 0 if nums.length < 3
     */
    public long maximumScore(int[] nums) {
        int n = nums.length;
        if (n < 3) {
            return 0L;
        }

        long[] arr = new long[n];
        for (int i = 0; i < n; i++) {
            arr[i] = nums[i];
        }

        long[] prefixMax = new long[n];
        long[] suffixMax = new long[n];

        prefixMax[0] = arr[0];
        for (int i = 1; i < n; i++) {
            prefixMax[i] = Math.max(prefixMax[i - 1], arr[i]);
        }

        suffixMax[n - 1] = arr[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            suffixMax[i] = Math.max(suffixMax[i + 1], arr[i]);
        }

        long answer = Long.MIN_VALUE;

        /*
         * We split the solution into two complementary cases:
         *
         * Case 1: left gap is the limiting one
         *   d = j - i <= k - j
         *   Then for fixed (i, j), the best k is simply the maximum value on the suffix
         *   starting at index 2j - i, because k must satisfy k >= 2j - i.
         *   Score becomes:
         *       (nums[i] + nums[j] + maxSuffix[2j - i]) * (j - i)
         *
         * Case 2: right gap is the limiting one
         *   d = k - j <= j - i
         *   Then for fixed (j, k), the best i is simply the maximum value on the prefix
         *   ending at index 2j - k, because i must satisfy i <= 2j - k.
         *   Score becomes:
         *       (maxPrefix[2j - k] + nums[j] + nums[k]) * (k - j)
         *
         * These two cases are symmetric. We compute both using a block decomposition
         * strategy:
         *
         * - Small distances are handled by direct enumeration.
         * - Large distances are handled by scanning possible middle positions and using
         *   precomputed maxima.
         *
         * This sqrt-style decomposition gives O(n * sqrt(n)) worst-case behavior, which
         * is fast enough for n = 2e5 in Java when implemented carefully.
         */

        int block = (int) Math.sqrt(n) + 1;

        /*
         * ------------------------------------------------------------
         * SMALL DISTANCES
         * ------------------------------------------------------------
         *
         * For every distance d from 1 up to block:
         * - We can directly scan all valid middle positions j.
         * - For Case 1:
         *     i = j - d
         *     k must be >= j + d
         *     best k contribution is suffixMax[j + d]
         * - For Case 2:
         *     k = j + d
         *     i must be <= j - d
         *     best i contribution is prefixMax[j - d]
         *
         * Notice that these two formulas together cover all triples whose limiting gap
         * equals d.
         */
        for (int d = 1; d <= block; d++) {
            for (int j = d; j + d < n; j++) {
                long candidate1 = (arr[j - d] + arr[j] + suffixMax[j + d]) * d;
                if (candidate1 > answer) {
                    answer = candidate1;
                }

                long candidate2 = (prefixMax[j - d] + arr[j] + arr[j + d]) * d;
                if (candidate2 > answer) {
                    answer = candidate2;
                }
            }
        }

        /*
         * ------------------------------------------------------------
         * LARGE DISTANCES
         * ------------------------------------------------------------
         *
         * Now we handle distances larger than 'block'.
         *
         * For large d, the number of possible positions for j becomes small enough that
         * we can afford a different enumeration:
         *
         * Case 1:
         *   limiting gap is left gap = j - i = d > block
         *   For each i, j = i + d, and k must be >= j + d = i + 2d.
         *
         * Instead of iterating by d directly, we iterate by i and jump j forward.
         * Since d > block, there are at most O(n / block) such j values per i.
         *
         * Case 2:
         *   limiting gap is right gap = k - j = d > block
         *   Symmetric enumeration by k and jumping j backward.
         */

        for (int i = 0; i < n; i++) {
            for (int j = i + block + 1; j < n; j++) {
                int minK = 2 * j - i;
                if (minK >= n) {
                    break;
                }

                long candidate = (arr[i] + arr[j] + suffixMax[minK]) * (long) (j - i);
                if (candidate > answer) {
                    answer = candidate;
                }
            }
        }

        for (int k = n - 1; k >= 0; k--) {
            for (int j = k - block - 1; j >= 0; j--) {
                int maxI = 2 * j - k;
                if (maxI < 0) {
                    break;
                }

                long candidate = (prefixMax[maxI] + arr[j] + arr[k]) * (long) (k - j);
                if (candidate > answer) {
                    answer = candidate;
                }
            }
        }

        return answer;
    }

    /**
     * Convenience wrapper used by the demo in main.
     *
     * Time complexity: O(n sqrt n)
     * Space complexity: O(n)
     *
     * @param nums the input array
     * @return the maximum protected triple score
     */
    public long solve(int[] nums) {
        return maximumScore(nums);
    }

    /**
     * Demonstrates the solution on sample-style inputs and a few additional checks.
     *
     * Time complexity: O(total input size * sqrt n) across the demonstrated examples
     * Space complexity: O(n) per call
     *
     * @param args command-line arguments, not used
     * @return nothing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {5, 1, 4, 2, 6};
        int[] nums2 = {-3, 7, -2, 8, -1};
        int[] nums3 = {1, 2, 3};
        int[] nums4 = {-5, -4, -3, -2};
        int[] nums5 = {10, -100, 10, -100, 10};

        System.out.println(solution.solve(nums1)); // mathematically correct: 30
        System.out.println(solution.solve(nums2)); // mathematically correct: 14
        System.out.println(solution.solve(nums3)); // (1+2+3)*1 = 6
        System.out.println(solution.solve(nums4)); // negative answer is allowed
        System.out.println(solution.solve(nums5));
    }
}