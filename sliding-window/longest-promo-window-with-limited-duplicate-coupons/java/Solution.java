import java.util.*;

/*
 * Title: Longest Promo Window With Limited Duplicate Coupons
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * An e-commerce platform records the coupon code used in each order during a marketing campaign.
 * The analytics team wants to find the longest contiguous block of orders that is still considered
 * "diverse enough" for reporting. A block is valid if no coupon code appears more than k times
 * inside that block.
 *
 * You are given an array coupons where coupons[i] is the coupon code used in the i-th order,
 * and an integer k. Return the length of the longest contiguous subarray such that every distinct
 * coupon code appears at most k times within that subarray.
 *
 * This is a realistic streaming-style problem: orders arrive in sequence, and you need to maintain
 * a valid window efficiently as you scan from left to right. A brute-force check of every subarray
 * will be too slow for large inputs.
 *
 * Constraints:
 * - 1 <= coupons.length <= 200000
 * - 1 <= coupons[i] <= 1000000000
 * - 1 <= k <= coupons.length
 * - coupons may contain many repeated values
 *
 * Example 1:
 * Input: coupons = [4, 1, 4, 2, 4, 1, 2, 2], k = 2
 * Output: 5
 * Explanation: One longest valid window is [1, 4, 2, 4, 1], where coupon 1 appears 2 times,
 * coupon 4 appears 2 times, and coupon 2 appears 1 time. Any longer window would cause either
 * coupon 4 or coupon 2 to appear more than 2 times.
 *
 * Example 2:
 * Input: coupons = [7, 7, 7, 8, 8, 9, 7], k = 1
 * Output: 3
 * Explanation: A valid longest window is [7, 8, 9] or [8, 9, 7]. In any valid window, each
 * coupon code must be unique because k = 1.
 *
 * Your task is to compute only the maximum length, not the subarray itself.
 */

public class Solution {

    /**
     * Computes the length of the longest contiguous subarray such that
     * every distinct coupon code appears at most k times.
     *
     * The algorithm uses the classic sliding window technique:
     * - Expand the window by moving the right pointer one step at a time.
     * - Track frequencies of coupon codes inside the current window.
     * - If adding the new coupon makes its count exceed k, shrink the window
     *   from the left until the window becomes valid again.
     * - After each step, update the best (maximum) valid window length seen so far.
     *
     * @param coupons the array of coupon codes in order of appearance
     * @param k the maximum allowed frequency for any coupon code inside a valid window
     * @return the maximum length of a contiguous valid subarray
     * Time complexity: O(n), where n is coupons.length, because each element is added to
     * the window once and removed from the window at most once.
     * Space complexity: O(m), where m is the number of distinct coupon codes currently tracked
     * in the frequency map; in the worst case this can be O(n).
     */
    public int longestPromoWindow(int[] coupons, int k) {
        // Frequency map:
        // key   -> coupon code
        // value -> how many times that coupon currently appears in the sliding window
        Map<Integer, Integer> frequency = new HashMap<>();

        // left marks the start of the current window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window by moving right from 0 to coupons.length - 1.
        for (int right = 0; right < coupons.length; right++) {
            int currentCoupon = coupons[right];

            // Step 1: include coupons[right] in the window.
            // Increase its frequency in the map.
            frequency.put(currentCoupon, frequency.getOrDefault(currentCoupon, 0) + 1);

            // Step 2: if the newly added coupon now appears more than k times,
            // the window is invalid.
            //
            // Important observation:
            // Before adding coupons[right], the window was valid.
            // After adding one element, only that specific element's frequency
            // can become too large. So it is enough to shrink while
            // frequency.get(currentCoupon) > k.
            while (frequency.get(currentCoupon) > k) {
                int leftCoupon = coupons[left];

                // Remove the leftmost coupon from the current window
                // by decreasing its frequency.
                frequency.put(leftCoupon, frequency.get(leftCoupon) - 1);

                // Optional cleanup:
                // If a coupon's count becomes zero, remove it from the map.
                // This is not required for correctness, but keeps the map cleaner.
                if (frequency.get(leftCoupon) == 0) {
                    frequency.remove(leftCoupon);
                }

                // Move the left boundary rightward, effectively shrinking the window.
                left++;
            }

            // Step 3: now the window [left..right] is valid again.
            // Compute its length and update the best answer if needed.
            int currentLength = right - left + 1;
            best = Math.max(best, currentLength);
        }

        return best;
    }

    /**
     * A helper method that runs the algorithm on one test case and prints
     * the input and output in a beginner-friendly format.
     *
     * @param coupons the coupon array to test
     * @param k the maximum allowed duplicate count per coupon inside a valid window
     * @return the computed maximum valid window length
     * Time complexity: O(n), where n is coupons.length.
     * Space complexity: O(m), where m is the number of distinct coupon codes tracked.
     */
    public int demonstrateCase(int[] coupons, int k) {
        int result = longestPromoWindow(coupons, k);
        System.out.println("Coupons: " + Arrays.toString(coupons));
        System.out.println("k = " + k);
        System.out.println("Longest valid window length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     *
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * Verified expected outputs:
     * - Example 1 should print 5
     * - Example 2 should print 3
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case.
     * Space complexity: O(m) per demonstrated test case.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] coupons1 = {4, 1, 4, 2, 4, 1, 2, 2};
        int k1 = 2;
        int result1 = solution.demonstrateCase(coupons1, k1);
        System.out.println("Expected: 5");
        System.out.println("Actual:   " + result1);
        System.out.println();

        // Example 2
        int[] coupons2 = {7, 7, 7, 8, 8, 9, 7};
        int k2 = 1;
        int result2 = solution.demonstrateCase(coupons2, k2);
        System.out.println("Expected: 3");
        System.out.println("Actual:   " + result2);
        System.out.println();

        // A couple of extra quick sanity checks for beginners:
        int[] coupons3 = {1, 2, 3, 4};
        int k3 = 1;
        int result3 = solution.demonstrateCase(coupons3, k3);
        System.out.println("Expected: 4");
        System.out.println("Actual:   " + result3);
        System.out.println();

        int[] coupons4 = {5, 5, 5, 5};
        int k4 = 2;
        int result4 = solution.demonstrateCase(coupons4, k4);
        System.out.println("Expected: 2");
        System.out.println("Actual:   " + result4);
    }
}