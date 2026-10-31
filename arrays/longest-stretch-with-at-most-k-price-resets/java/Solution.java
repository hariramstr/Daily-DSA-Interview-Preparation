import java.util.*;

/*
 * Title: Longest Stretch With At Most K Price Resets
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * An online retailer records the displayed price of a product once per hour in an integer array prices,
 * where prices[i] is the price at hour i. A pricing analyst defines a stretch of hours as stable if,
 * after removing at most k hours from that stretch, the remaining prices are all equal.
 * The removed hours do not need to be consecutive, but they must come from inside the chosen contiguous stretch.
 *
 * Your task is to return the length of the longest contiguous stretch that can be made stable under this rule.
 *
 * In other words, for any subarray prices[l...r], you may delete at most k elements from that subarray.
 * If the remaining elements can all have the same value, then that subarray is valid. Find the maximum possible
 * length of such a valid subarray.
 *
 * This models a real pricing system where a few temporary resets or glitches may be ignored when measuring
 * how long a product effectively stayed at one displayed price.
 *
 * Constraints:
 * - 1 <= prices.length <= 100000
 * - 1 <= prices[i] <= 1000000000
 * - 0 <= k <= prices.length
 * - The answer should be computed in O(n) or O(n log n) time.
 *
 * Example 1:
 * Input: prices = [7,7,8,7,7,9,7], k = 1
 * Output: 5
 * Explanation: The subarray [7,7,8,7,7] has length 5. By removing the single value 8,
 * all remaining values are 7, so this stretch is valid. No longer valid stretch exists with at most 1 removal.
 *
 * Example 2:
 * Input: prices = [4,5,5,5,6,5,5], k = 2
 * Output: 7
 * Explanation: The entire array has length 7. Remove 4 and 6, and all remaining values are 5.
 * Since only 2 removals are needed, the whole stretch is valid.
 *
 * Key Observation:
 * For any window, if the most frequent value appears maxFreq times, then we need to remove
 * windowLength - maxFreq elements to make all remaining values equal.
 * Therefore, a window is valid if:
 *
 *     windowLength - maxFreq <= k
 *
 * This leads naturally to a sliding window solution.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray that can be made stable
     * by deleting at most k elements from inside that subarray.
     *
     * The method uses a sliding window plus a frequency map:
     * - Expand the right boundary one element at a time.
     * - Track how many times each price appears in the current window.
     * - Track the highest frequency seen inside the current window process.
     * - If the number of non-majority elements becomes greater than k,
     *   shrink the window from the left until it becomes valid again.
     *
     * Why this works:
     * For a window to be transformable into all equal values after at most k deletions,
     * we keep the most frequent value and delete everything else.
     * So required deletions = window size - highest frequency in the window.
     *
     * @param prices the array of hourly displayed prices
     * @param k the maximum number of hours that may be removed from a chosen contiguous stretch
     * @return the maximum length of a contiguous stretch that can be made stable
     *
     * Time complexity: O(n), where n is prices.length, because each index moves through the window at most once.
     * Space complexity: O(m), where m is the number of distinct prices currently tracked in the map
     * (in the worst case O(n)).
     */
    public int longestStableStretch(int[] prices, int k) {
        // Frequency map:
        // key   -> price value
        // value -> how many times that price appears in the current sliding window
        Map<Integer, Integer> frequency = new HashMap<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int bestLength = 0;

        // maxFrequency stores the largest count of any single price seen in the current
        // sliding window expansion process.
        //
        // Important note:
        // We do NOT decrease maxFrequency when shrinking the window.
        // This is a standard optimization used in this pattern.
        // Even if maxFrequency becomes "stale", the algorithm remains correct because:
        // - It may delay shrinking slightly,
        // - But it never causes us to miss the true optimal answer.
        int maxFrequency = 0;

        // Expand the window by moving "right" from 0 to n - 1.
        for (int right = 0; right < prices.length; right++) {
            int currentPrice = prices[right];

            // Add the new rightmost element into the frequency map.
            int newCount = frequency.getOrDefault(currentPrice, 0) + 1;
            frequency.put(currentPrice, newCount);

            // Update the maximum frequency seen in the current window process.
            // If this price now appears more times than any previous price in the window,
            // it becomes the new majority candidate.
            maxFrequency = Math.max(maxFrequency, newCount);

            // Current window length is from left to right inclusive.
            // If windowLength - maxFrequency > k, then even after deleting k elements,
            // we still cannot make all remaining values equal.
            //
            // So we must shrink the window from the left until it becomes valid again.
            while ((right - left + 1) - maxFrequency > k) {
                int leftPrice = prices[left];

                // Remove the leftmost element from the frequency map because it is leaving the window.
                frequency.put(leftPrice, frequency.get(leftPrice) - 1);

                // Optional cleanup: remove keys whose count becomes zero.
                if (frequency.get(leftPrice) == 0) {
                    frequency.remove(leftPrice);
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // At this point, the window [left...right] is valid.
            // Update the best answer with its length if it is larger than previous best.
            bestLength = Math.max(bestLength, right - left + 1);
        }

        return bestLength;
    }

    /**
     * A helper method that runs the algorithm on one test case and prints the result.
     * This is useful for demonstration in main.
     *
     * @param prices the input price array
     * @param k the maximum allowed deletions
     * @return the computed longest valid stretch length
     *
     * Time complexity: O(n), where n is prices.length.
     * Space complexity: O(m), where m is the number of distinct values tracked in the window.
     */
    public int demonstrateCase(int[] prices, int k) {
        int result = longestStableStretch(prices, k);
        System.out.println("prices = " + Arrays.toString(prices));
        System.out.println("k = " + k);
        System.out.println("Longest stable stretch = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional sanity checks.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(total input size across demonstrated test cases).
     * Space complexity: O(m) per test case for the frequency map.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1:
        // prices = [7,7,8,7,7,9,7], k = 1
        // Expected output: 5
        int[] prices1 = {7, 7, 8, 7, 7, 9, 7};
        int result1 = solution.demonstrateCase(prices1, 1);
        System.out.println("Expected: 5, Actual: " + result1);
        System.out.println();

        // Sample 2:
        // prices = [4,5,5,5,6,5,5], k = 2
        // Expected output: 7
        int[] prices2 = {4, 5, 5, 5, 6, 5, 5};
        int result2 = solution.demonstrateCase(prices2, 2);
        System.out.println("Expected: 7, Actual: " + result2);
        System.out.println();

        // Additional sanity check 1:
        // All equal already, no deletions needed.
        int[] prices3 = {3, 3, 3, 3};
        int result3 = solution.demonstrateCase(prices3, 0);
        System.out.println("Expected: 4, Actual: " + result3);
        System.out.println();

        // Additional sanity check 2:
        // With enough deletions, the whole array can be made stable.
        int[] prices4 = {1, 2, 3, 4};
        int result4 = solution.demonstrateCase(prices4, 3);
        System.out.println("Expected: 4, Actual: " + result4);
        System.out.println();

        // Additional sanity check 3:
        // No deletions allowed, so answer is the longest contiguous block of equal values.
        int[] prices5 = {2, 2, 1, 1, 1, 2};
        int result5 = solution.demonstrateCase(prices5, 0);
        System.out.println("Expected: 3, Actual: " + result5);
    }
}