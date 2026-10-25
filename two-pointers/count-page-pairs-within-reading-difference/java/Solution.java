import java.util.*;

/*
 * Title: Count Page Pairs Within Reading Difference
 * Difficulty: Easy
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given an array pages where pages[i] is the number of pages in the ith booklet.
 * You are also given an integer k. Two booklets form a compatible reading pair if the
 * absolute difference between their page counts is less than or equal to k.
 *
 * Your task is to return the total number of index pairs (i, j) such that:
 * 0 <= i < j < pages.length
 * and
 * |pages[i] - pages[j]| <= k
 *
 * A straightforward solution checks every pair, but that is too slow for large inputs.
 * Since this is an interview-style problem, a more efficient approach uses sorting and
 * two pointers.
 *
 * Note:
 * The original order of the booklets does not matter when counting valid pairs, because
 * we only need the number of valid pairs, not the actual original indices.
 *
 * Constraints:
 * - 1 <= pages.length <= 100000
 * - 0 <= pages[i] <= 1000000000
 * - 0 <= k <= 1000000000
 * - The answer may be large, so a 64-bit integer type is required.
 *
 * Example 1:
 * Input: pages = [12, 15, 10, 18], k = 3
 * Output: 4
 *
 * Example 2:
 * Input: pages = [5, 5, 5, 9], k = 0
 * Output: 3
 */

public class Solution {

    /**
     * Counts how many index pairs (i, j) satisfy i < j and |pages[i] - pages[j]| <= k.
     *
     * The algorithm works as follows:
     * 1. Sort the array so values are in non-decreasing order.
     * 2. Use a sliding window / two-pointer technique.
     * 3. For each right pointer, move the left pointer forward until the difference
     *    between pages[right] and pages[left] is at most k.
     * 4. Once the window is valid, every index from left to right - 1 forms a valid
     *    pair with right, because the array is sorted.
     *
     * @param pages the array of booklet page counts
     * @param k the maximum allowed absolute difference between two page counts
     * @return the total number of valid pairs as a long
     * Time complexity: O(n log n) due to sorting, where n is pages.length
     * Space complexity: O(1) extra space excluding the sorting implementation details
     */
    public long countCompatiblePairs(int[] pages, int k) {
        // Defensive handling:
        // If the array has fewer than 2 elements, no pair can exist.
        if (pages == null || pages.length < 2) {
            return 0L;
        }

        // Sort the array.
        // Why sorting helps:
        // After sorting, for any i < j, pages[j] >= pages[i].
        // Therefore, |pages[j] - pages[i]| becomes simply pages[j] - pages[i].
        // This removes the need to consider both directions of the absolute value.
        Arrays.sort(pages);

        // This variable stores the final answer.
        // We use long because the number of pairs can be as large as n * (n - 1) / 2,
        // which may exceed the range of int when n is large.
        long pairCount = 0L;

        // left marks the beginning of the current valid window.
        int left = 0;

        // Expand the window using right.
        // For each right, we want the smallest left such that:
        // pages[right] - pages[left] <= k
        for (int right = 0; right < pages.length; right++) {

            // If the difference is too large, move left forward.
            // Because the array is sorted, increasing left can only reduce the difference.
            while (left < right && (long) pages[right] - pages[left] > k) {
                left++;
            }

            // At this point, the window [left, right] is valid:
            // pages[right] - pages[left] <= k
            //
            // Since the array is sorted, every index between left and right - 1 also satisfies:
            // pages[right] - pages[index] <= k
            //
            // Therefore, the number of valid pairs ending at 'right' is:
            // right - left
            //
            // Example:
            // If left = 2 and right = 5, then valid partners for index 5 are:
            // 2, 3, 4  -> total = 3 = 5 - 2
            pairCount += (right - left);
        }

        return pairCount;
    }

    /**
     * A helper method that creates a copy of the input array before counting pairs.
     * This is useful for demonstrations when we do not want to modify the original array,
     * because the main algorithm sorts the array in-place.
     *
     * @param pages the original array of booklet page counts
     * @param k the maximum allowed absolute difference
     * @return the total number of valid pairs as a long
     * Time complexity: O(n log n)
     * Space complexity: O(n) because of the copied array
     */
    public long countCompatiblePairsWithoutModifyingInput(int[] pages, int k) {
        if (pages == null) {
            return 0L;
        }

        int[] copy = Arrays.copyOf(pages, pages.length);
        return countCompatiblePairs(copy, k);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) per demonstration call
     * Space complexity: O(n) for copied arrays used in the demo
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] pages1 = {12, 15, 10, 18};
        int k1 = 3;
        long result1 = solution.countCompatiblePairsWithoutModifyingInput(pages1, k1);
        System.out.println("Sample 1:");
        System.out.println("pages = " + Arrays.toString(pages1) + ", k = " + k1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 4");
        System.out.println();

        // Sample 2
        int[] pages2 = {5, 5, 5, 9};
        int k2 = 0;
        long result2 = solution.countCompatiblePairsWithoutModifyingInput(pages2, k2);
        System.out.println("Sample 2:");
        System.out.println("pages = " + Arrays.toString(pages2) + ", k = " + k2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        // Additional quick checks for beginner-friendly understanding

        // All values close enough, so every pair is valid.
        int[] pages3 = {1, 2, 3, 4};
        int k3 = 10;
        long result3 = solution.countCompatiblePairsWithoutModifyingInput(pages3, k3);
        System.out.println("Additional Example 1:");
        System.out.println("pages = " + Arrays.toString(pages3) + ", k = " + k3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: 6");
        System.out.println();

        // No two different values are close enough.
        int[] pages4 = {1, 10, 20, 30};
        int k4 = 0;
        long result4 = solution.countCompatiblePairsWithoutModifyingInput(pages4, k4);
        System.out.println("Additional Example 2:");
        System.out.println("pages = " + Arrays.toString(pages4) + ", k = " + k4);
        System.out.println("Output: " + result4);
        System.out.println("Expected: 0");
    }
}