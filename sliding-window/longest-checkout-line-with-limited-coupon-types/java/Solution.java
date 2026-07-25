import java.util.*;

/*
 * Title: Longest Checkout Line With Limited Coupon Types
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A supermarket records the coupon type used by each customer in the order they join a checkout line.
 * You are given an integer array coupons where coupons[i] is the coupon type used by the i-th customer,
 * and an integer k. Your task is to find the length of the longest contiguous block of customers such that
 * the block contains at most k distinct coupon types.
 *
 * This problem models a cashier lane that can efficiently process only a limited variety of coupon rules
 * at once. A valid block may contain repeated coupon types any number of times, but the total number of
 * different coupon types appearing inside the block must not exceed k.
 *
 * Return the maximum possible length of such a contiguous block. If k is 0, no customer can be included,
 * so the answer is 0.
 *
 * Constraints:
 * - 1 <= coupons.length <= 200000
 * - 1 <= coupons[i] <= 1000000000
 * - 0 <= k <= coupons.length
 *
 * Example 1:
 * Input: coupons = [4, 2, 2, 5, 5, 2, 4, 4], k = 2
 * Output: 5
 * Explanation: The longest valid block is [2, 2, 5, 5, 2], which contains only coupon types 2 and 5.
 *
 * Example 2:
 * Input: coupons = [1, 3, 1, 3, 2, 2, 2, 4], k = 3
 * Output: 7
 * Explanation: The block [1, 3, 1, 3, 2, 2, 2] contains exactly 3 distinct coupon types: 1, 3, and 2.
 * No longer contiguous block satisfies the limit.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray that contains at most k distinct coupon types.
     *
     * This method uses the classic sliding window technique:
     * - Expand the right side of the window one element at a time.
     * - Track how many times each coupon type appears inside the current window.
     * - If the number of distinct coupon types becomes greater than k, shrink the window from the left
     *   until it becomes valid again.
     * - At every valid step, update the best window length found so far.
     *
     * @param coupons the array where coupons[i] represents the coupon type used by the i-th customer
     * @param k the maximum number of distinct coupon types allowed in a valid contiguous block
     * @return the maximum length of a contiguous block containing at most k distinct coupon types
     *
     * Time complexity: O(n), where n is coupons.length, because each element is added to and removed from
     * the sliding window at most once.
     * Space complexity: O(min(n, k)) on average for the frequency map, and O(n) in the worst case if many
     * distinct values are encountered before shrinking.
     */
    public int longestCheckoutLineWithLimitedCouponTypes(int[] coupons, int k) {
        // If no coupon types are allowed, then no customer can be included.
        // The problem statement explicitly says the answer must be 0 in this case.
        if (k == 0) {
            return 0;
        }

        // Frequency map:
        // key   -> coupon type
        // value -> how many times that coupon type appears in the current window
        Map<Integer, Integer> frequency = new HashMap<>();

        // left marks the start of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window by moving right from 0 to coupons.length - 1.
        for (int right = 0; right < coupons.length; right++) {
            int currentCoupon = coupons[right];

            // Include coupons[right] in the window.
            // If it is not already present, start its count at 0, then add 1.
            frequency.put(currentCoupon, frequency.getOrDefault(currentCoupon, 0) + 1);

            // If the window now contains too many distinct coupon types,
            // we must shrink it from the left until it becomes valid again.
            while (frequency.size() > k) {
                int leftCoupon = coupons[left];

                // Decrease the count of the coupon type that is leaving the window.
                frequency.put(leftCoupon, frequency.get(leftCoupon) - 1);

                // If its count becomes 0, it is no longer present in the window,
                // so we remove it from the map entirely.
                if (frequency.get(leftCoupon) == 0) {
                    frequency.remove(leftCoupon);
                }

                // Move the left boundary to the right, effectively shrinking the window.
                left++;
            }

            // At this point, the window [left, right] is guaranteed to be valid:
            // it contains at most k distinct coupon types.
            int currentWindowLength = right - left + 1;

            // Update the best answer if this valid window is larger than any seen before.
            if (currentWindowLength > best) {
                best = currentWindowLength;
            }
        }

        return best;
    }

    /**
     * A convenience wrapper method with a shorter name.
     *
     * @param coupons the array of coupon types in customer order
     * @param k the maximum number of distinct coupon types allowed
     * @return the length of the longest valid contiguous block
     *
     * Time complexity: O(n), where n is coupons.length.
     * Space complexity: O(min(n, k)) on average, O(n) worst case.
     */
    public int lengthOfLongestBlock(int[] coupons, int k) {
        return longestCheckoutLineWithLimitedCouponTypes(coupons, k);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total number of elements across demonstrated test cases).
     * Space complexity: Depends on the largest test case frequency map used during execution.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Example 1
        int[] coupons1 = {4, 2, 2, 5, 5, 2, 4, 4};
        int k1 = 2;
        int result1 = solution.longestCheckoutLineWithLimitedCouponTypes(coupons1, k1);
        System.out.println("Example 1 Result: " + result1);
        // Expected: 5
        // One longest valid block is [2, 2, 5, 5, 2]

        // Sample Example 2
        int[] coupons2 = {1, 3, 1, 3, 2, 2, 2, 4};
        int k2 = 3;
        int result2 = solution.longestCheckoutLineWithLimitedCouponTypes(coupons2, k2);
        System.out.println("Example 2 Result: " + result2);
        // Expected: 7
        // One longest valid block is [1, 3, 1, 3, 2, 2, 2]

        // Edge case: k = 0 means no customer can be included
        int[] coupons3 = {7, 7, 8};
        int k3 = 0;
        int result3 = solution.longestCheckoutLineWithLimitedCouponTypes(coupons3, k3);
        System.out.println("Edge Case k=0 Result: " + result3);
        // Expected: 0

        // Additional test: all same coupon type
        int[] coupons4 = {5, 5, 5, 5};
        int k4 = 1;
        int result4 = solution.longestCheckoutLineWithLimitedCouponTypes(coupons4, k4);
        System.out.println("All Same Coupon Type Result: " + result4);
        // Expected: 4

        // Additional test: each coupon different, small k
        int[] coupons5 = {1, 2, 3, 4, 5};
        int k5 = 2;
        int result5 = solution.longestCheckoutLineWithLimitedCouponTypes(coupons5, k5);
        System.out.println("All Different, k=2 Result: " + result5);
        // Expected: 2
    }
}