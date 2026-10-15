import java.util.*;

/*
 * Title: Count Compatible Ski Lift Pairs
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A ski resort records the heights of guests waiting in a single line. For safety,
 * the resort wants to place some guests together on a two-person lift seat, but
 * only if the difference between their heights is at most maxGap centimeters.
 *
 * You are given an integer array heights where heights[i] is the height of the
 * ith guest, and an integer maxGap.
 *
 * Your task is to count how many distinct pairs of guests (i, j) with i < j can
 * safely share a lift seat, meaning |heights[i] - heights[j]| <= maxGap.
 *
 * The array is not guaranteed to be sorted. An efficient solution is expected for
 * large inputs, so a quadratic scan over all pairs will be too slow. Sorting and
 * a two-pointers strategy can help count valid pairs in bulk.
 *
 * Return the total number of valid pairs.
 *
 * Constraints:
 * - 1 <= heights.length <= 2 * 10^5
 * - 1 <= heights[i] <= 10^9
 * - 0 <= maxGap <= 10^9
 * - The answer may be large, so use a 64-bit integer type.
 *
 * Important correctness note:
 * The first example text in the prompt contains contradictory wording, but the
 * mathematically correct answer is:
 *
 * Example 1:
 * Input: heights = [150, 160, 153, 168], maxGap = 5
 * Sorted: [150, 153, 160, 168]
 * Valid pairs: (150,153)
 * Output: 1
 *
 * Example 2:
 * Input: heights = [172, 175, 171, 169, 174], maxGap = 3
 * Sorted: [169, 171, 172, 174, 175]
 * Valid pairs:
 * (169,171), (169,172), (171,172), (171,174), (172,174), (172,175), (174,175)
 * Output: 7
 *
 * Note:
 * The prompt states Example 2 output is 8, but pair (171,175) has difference 4,
 * so it is invalid. Therefore, the correct output is 7.
 */

public class Solution {

    /**
     * Counts how many distinct pairs of guests have height difference at most maxGap.
     *
     * Algorithm:
     * 1. Sort the heights array.
     * 2. Use a sliding window / two-pointers approach.
     * 3. Maintain a left pointer such that for each right pointer, the subarray
     *    from left to right contains only values where:
     *    heights[right] - heights[left] <= maxGap
     * 4. Once the window is valid, every index between left and right - 1 can pair
     *    with right, so we add (right - left) to the answer.
     *
     * @param heights the array of guest heights
     * @param maxGap the maximum allowed height difference for a valid pair
     * @return the total number of valid pairs as a long
     * Time complexity: O(n log n) due to sorting, plus O(n) for the two-pointer scan
     * Space complexity: O(1) extra space beyond the sorting implementation details
     */
    public long countCompatiblePairs(int[] heights, int maxGap) {
        // Defensive handling is not strictly necessary because constraints guarantee
        // at least one element, but this makes the method more robust and beginner-friendly.
        if (heights == null || heights.length < 2) {
            return 0L;
        }

        // Sort the heights so that absolute difference becomes easy to reason about.
        // After sorting, for any i < j:
        // |heights[i] - heights[j]| == heights[j] - heights[i]
        Arrays.sort(heights);

        long totalPairs = 0L;

        // "left" marks the smallest index still compatible with the current "right".
        int left = 0;

        // Move "right" from left to right across the sorted array.
        for (int right = 0; right < heights.length; right++) {

            // If the current window is too wide, move "left" forward until
            // the difference between heights[right] and heights[left] is at most maxGap.
            //
            // Because the array is sorted:
            // - heights[right] is the largest value in the current window
            // - heights[left] is the smallest value in the current window
            //
            // So if heights[right] - heights[left] <= maxGap,
            // then every value between left and right is also within maxGap of heights[right].
            while (heights[right] - heights[left] > maxGap) {
                left++;
            }

            // At this point, the window [left, right] is valid.
            //
            // Every index k in [left, right - 1] forms a valid pair (k, right),
            // because:
            // heights[right] - heights[k] <= heights[right] - heights[left] <= maxGap
            //
            // Number of such indices = right - left
            totalPairs += (right - left);
        }

        return totalPairs;
    }

    /**
     * A simple brute-force method used only for demonstration and verification on small inputs.
     * This is not efficient for large arrays, but it is useful to confirm correctness.
     *
     * @param heights the array of guest heights
     * @param maxGap the maximum allowed height difference for a valid pair
     * @return the total number of valid pairs as a long
     * Time complexity: O(n^2)
     * Space complexity: O(1)
     */
    public long countCompatiblePairsBruteForce(int[] heights, int maxGap) {
        if (heights == null || heights.length < 2) {
            return 0L;
        }

        long count = 0L;

        for (int i = 0; i < heights.length; i++) {
            for (int j = i + 1; j < heights.length; j++) {
                if (Math.abs((long) heights[i] - heights[j]) <= maxGap) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Creates a copy of the given array.
     * This helper is used so demonstrations do not mutate the original sample arrays
     * when sorting is performed inside the main algorithm.
     *
     * @param arr the input array
     * @return a copied array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] copyArray(int[] arr) {
        return Arrays.copyOf(arr, arr.length);
    }

    /**
     * Demonstrates the solution on sample inputs and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo inputs, excluding algorithm calls
     * Space complexity: O(1), excluding copied arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] heights1 = {150, 160, 153, 168};
        int maxGap1 = 5;
        long result1 = solution.countCompatiblePairs(solution.copyArray(heights1), maxGap1);
        long brute1 = solution.countCompatiblePairsBruteForce(heights1, maxGap1);

        System.out.println("Example 1:");
        System.out.println("heights = " + Arrays.toString(heights1) + ", maxGap = " + maxGap1);
        System.out.println("Efficient result = " + result1);
        System.out.println("Brute-force check = " + brute1);
        System.out.println("Expected correct output = 1");
        System.out.println();

        int[] heights2 = {172, 175, 171, 169, 174};
        int maxGap2 = 3;
        long result2 = solution.countCompatiblePairs(solution.copyArray(heights2), maxGap2);
        long brute2 = solution.countCompatiblePairsBruteForce(heights2, maxGap2);

        System.out.println("Example 2:");
        System.out.println("heights = " + Arrays.toString(heights2) + ", maxGap = " + maxGap2);
        System.out.println("Efficient result = " + result2);
        System.out.println("Brute-force check = " + brute2);
        System.out.println("Expected correct output = 7");
        System.out.println();

        int[] heights3 = {100, 100, 100, 100};
        int maxGap3 = 0;
        long result3 = solution.countCompatiblePairs(solution.copyArray(heights3), maxGap3);

        System.out.println("Additional Example 3:");
        System.out.println("heights = " + Arrays.toString(heights3) + ", maxGap = " + maxGap3);
        System.out.println("Result = " + result3);
        System.out.println("Expected output = 6");
        System.out.println();

        int[] heights4 = {140, 155, 170, 185};
        int maxGap4 = 10;
        long result4 = solution.countCompatiblePairs(solution.copyArray(heights4), maxGap4);

        System.out.println("Additional Example 4:");
        System.out.println("heights = " + Arrays.toString(heights4) + ", maxGap = " + maxGap4);
        System.out.println("Result = " + result4);
        System.out.println("Expected output = 0");
    }
}