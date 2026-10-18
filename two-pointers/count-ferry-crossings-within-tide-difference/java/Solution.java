import java.util.*;

/*
 * Title: Count Ferry Crossings Within Tide Difference
 * Difficulty: Hard
 * Topic: Two Pointers
 *
 * Problem Description:
 * A coastal logistics company records the tide level at each scheduled ferry departure during a day.
 * You are given an integer array tides of length n, where tides[i] is the tide level at the i-th
 * departure slot, and an integer limit.
 *
 * Two departure slots i < j form a valid crossing pair if the absolute difference between their tide
 * levels is at most limit, that is, |tides[i] - tides[j]| <= limit.
 *
 * Your task is to count how many distinct index pairs (i, j) are valid.
 *
 * Because the number of pairs can be large, return the answer as a 64-bit integer.
 *
 * A brute-force O(n^2) solution will time out for the largest inputs. You are expected to design
 * an efficient solution using sorting and a two-pointers strategy.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - -10^9 <= tides[i] <= 10^9
 * - 0 <= limit <= 10^9
 *
 * Example 1:
 * Input: tides = [4, 1, 7, 3, 5], limit = 2
 * Output: 4
 * Explanation:
 * After sorting, the tide levels are [1, 3, 4, 5, 7].
 * Valid pairs are:
 * - (3, 4)
 * - (3, 5)
 * - (4, 5)
 * - (5, 7)
 * Total = 4
 *
 * Example 2:
 * Input: tides = [8, 8, 8, 8], limit = 0
 * Output: 6
 * Explanation:
 * Every pair of equal tide levels is valid.
 * With 4 departures, the number of pairs is 4 * 3 / 2 = 6.
 *
 * Notes:
 * - Pairs are based on original indices, but only the tide values matter for validity.
 * - Duplicate tide levels are allowed.
 * - An optimal solution should run in O(n log n) time due to sorting, with a linear two-pointer
 *   scan afterward.
 */

public class Solution {

    /**
     * Counts how many distinct index pairs (i, j), with i < j, satisfy:
     * |tides[i] - tides[j]| <= limit.
     *
     * Core idea:
     * 1. Sort the tide levels.
     * 2. Use a sliding window / two-pointer technique.
     * 3. For each right pointer, move the left pointer forward until the window becomes valid.
     * 4. Once the window [left, right] is valid, every index from left to right - 1 can pair
     *    with right, because the array is sorted.
     *
     * Why sorting helps:
     * In a sorted array, for any left <= k < right:
     * sorted[right] - sorted[k] <= sorted[right] - sorted[left]
     * So if the smallest value in the window is within limit of sorted[right], then all values
     * between them are also within limit.
     *
     * @param tides the array of tide levels at departure slots
     * @param limit the maximum allowed absolute difference between two tide levels
     * @return the total number of valid pairs as a long
     *
     * Time complexity: O(n log n), dominated by sorting
     * Space complexity: O(n) if we clone the array before sorting; sorting itself is handled by Java
     */
    public long countValidCrossingPairs(int[] tides, int limit) {
        // Defensive handling for completeness.
        // With fewer than 2 elements, no pair can exist.
        if (tides == null || tides.length < 2) {
            return 0L;
        }

        // We clone the input so the original array remains unchanged.
        int[] sorted = tides.clone();

        // Step 1: Sort the tide levels.
        // After sorting, absolute difference for i < j becomes simply:
        // sorted[j] - sorted[i]
        // because sorted[j] >= sorted[i].
        Arrays.sort(sorted);

        long pairCount = 0L;

        // 'left' marks the smallest index currently allowed in the valid window.
        int left = 0;

        // Step 2: Expand the window using 'right'.
        // For each 'right', we will ensure that:
        // sorted[right] - sorted[left] <= limit
        for (int right = 0; right < sorted.length; right++) {

            // Step 3: If the current window violates the limit,
            // move 'left' forward until it becomes valid again.
            //
            // Because the array is sorted:
            // - The largest value in the window is sorted[right]
            // - The smallest value in the window is sorted[left]
            // So the maximum difference inside this window is:
            // sorted[right] - sorted[left]
            //
            // If that difference is too large, left must move rightward.
            while ((long) sorted[right] - sorted[left] > limit) {
                left++;
            }

            // Step 4: Count how many pairs end at index 'right'.
            //
            // Now the window [left, right] is valid, meaning:
            // sorted[right] - sorted[left] <= limit
            //
            // Since the array is sorted, every index k in [left, right - 1]
            // also satisfies:
            // sorted[right] - sorted[k] <= limit
            //
            // Therefore, the number of new valid pairs with second element 'right' is:
            // right - left
            //
            // Example:
            // if left = 2 and right = 5, then valid partners are indices 2, 3, 4
            // which is exactly 5 - 2 = 3 partners.
            pairCount += (right - left);
        }

        return pairCount;
    }

    /**
     * A helper method that runs the algorithm on one test case and prints the result.
     *
     * @param tides the input tide levels
     * @param limit the maximum allowed absolute difference
     * @return the computed number of valid pairs
     *
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long demonstrateCase(int[] tides, int limit) {
        long result = countValidCrossingPairs(tides, limit);
        System.out.println("tides = " + Arrays.toString(tides));
        System.out.println("limit = " + limit);
        System.out.println("valid pairs = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method demonstrating the solution on the sample inputs from the problem statement.
     *
     * Verified sample outputs:
     * 1. tides = [4, 1, 7, 3, 5], limit = 2 => 4
     * 2. tides = [8, 8, 8, 8], limit = 0 => 6
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: Depends on the demonstrated test cases
     * Space complexity: Depends on the demonstrated test cases
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        // Sorted: [1, 3, 4, 5, 7]
        // Valid pairs by value:
        // (3,4), (3,5), (4,5), (5,7) => total 4
        solution.demonstrateCase(new int[]{4, 1, 7, 3, 5}, 2);

        // Sample 2
        // All values are equal, limit = 0
        // Number of pairs among 4 elements = 4 * 3 / 2 = 6
        solution.demonstrateCase(new int[]{8, 8, 8, 8}, 0);

        // A few extra sanity checks for beginners:

        // No valid pair because differences are too large
        solution.demonstrateCase(new int[]{1, 10, 20}, 3);

        // All pairs valid
        solution.demonstrateCase(new int[]{2, 3, 4, 5}, 10);

        // Mixed negative and positive values
        solution.demonstrateCase(new int[]{-5, -2, 0, 3, 4}, 3);
    }
}