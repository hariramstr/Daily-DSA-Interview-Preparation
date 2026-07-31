import java.util.*;

/*
 * Title: Maximum Product Score from a Fixed-Length Window
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array nums and an integer k. A contiguous window of length k
 * is called valid if it contains no zero. The product score of a valid window is the
 * product of all elements inside that window. Your task is to return the maximum product
 * score among all valid windows of length exactly k. If no valid window exists, return 0.
 *
 * This problem is designed for large inputs, so recomputing the product from scratch for
 * every window will be too slow. You need to process the array efficiently while handling
 * positive numbers, negative numbers, and zeros. Because negative values can flip the sign
 * of the product, the maximum answer is not always produced by the window with the largest
 * absolute values. Windows containing even one zero are invalid and must be skipped entirely.
 *
 * Return the maximum product as a 64-bit integer. You may assume the final answer fits in
 * a signed 64-bit range.
 *
 * Constraints:
 * - 1 <= nums.length <= 100000
 * - -10 <= nums[i] <= 10
 * - 1 <= k <= nums.length
 * - The maximum valid product fits in a signed 64-bit integer
 *
 * Example 1:
 * Input: nums = [2, -3, 4, -1, 5], k = 3
 * Output: 12
 * Explanation:
 * The length-3 windows are:
 * [2, -3, 4] -> product = -24
 * [-3, 4, -1] -> product = 12
 * [4, -1, 5] -> product = -20
 * Maximum = 12
 *
 * Example 2:
 * Input: nums = [0, -2, -3, 4, 0, 5], k = 2
 * Output: 6
 * Explanation:
 * Invalid windows containing zero:
 * [0, -2], [4, 0], [0, 5]
 * Valid windows:
 * [-2, -3] -> 6
 * [-3, 4] -> -12
 * Maximum = 6
 */

public class Solution {

    /**
     * Computes the maximum product among all contiguous windows of length exactly k
     * that contain no zero.
     *
     * Core idea:
     * - Maintain a sliding window of size k.
     * - Track how many zeros are currently inside the window.
     * - Track the product of all non-zero values currently inside the window.
     * - A window is valid exactly when its zero count is 0.
     * - When the window slides:
     *   1) Remove the outgoing element from the window state.
     *   2) Add the incoming element to the window state.
     *   3) If zero count is 0, the maintained product is the exact product of the window.
     *
     * Why this works:
     * - If a zero is inside the window, the window is invalid and we do not consider it.
     * - We never divide by zero:
     *   - If an outgoing element is zero, we only decrease zero count.
     *   - If an outgoing element is non-zero, we divide it out from the maintained product.
     * - The maintained product always equals the product of all non-zero elements currently
     *   in the window. Therefore, when zero count becomes 0, that product is exactly the
     *   full window product.
     *
     * Example trace for nums = [2, -3, 4, -1, 5], k = 3:
     * - Initial window [2, -3, 4]:
     *   zeroCount = 0, product = -24 => answer = -24
     * - Slide to [-3, 4, -1]:
     *   remove 2 => product = -12
     *   add -1 => product = 12
     *   valid => answer = max(-24, 12) = 12
     * - Slide to [4, -1, 5]:
     *   remove -3 => product = -4
     *   add 5 => product = -20
     *   valid => answer = max(12, -20) = 12
     *
     * Example trace for nums = [0, -2, -3, 4, 0, 5], k = 2:
     * - Initial window [0, -2]:
     *   zeroCount = 1 => invalid
     * - Slide to [-2, -3]:
     *   remove 0 => zeroCount = 0
     *   add -3 => product = 6
     *   valid => answer = 6
     * - Slide to [-3, 4]:
     *   remove -2 => product = -3
     *   add 4 => product = -12
     *   valid => answer = max(6, -12) = 6
     * - Slide to [4, 0]:
     *   remove -3 => product = 4
     *   add 0 => zeroCount = 1
     *   invalid
     * - Slide to [0, 5]:
     *   remove 4 => product = 1
     *   add 5 => product = 5, but zeroCount = 1
     *   invalid
     * Final answer = 6
     *
     * @param nums the input integer array
     * @param k the exact required window length
     * @return the maximum product of any valid length-k window, or 0 if no valid window exists
     * Time complexity: O(n), where n is nums.length, because each element is added once and removed once.
     * Space complexity: O(1), ignoring input storage, because only a few variables are used.
     */
    public long maxProductScore(int[] nums, int k) {
        int n = nums.length;

        // This variable stores the product of all NON-ZERO elements currently inside the window.
        // Important detail:
        // - If the window contains zeros, this is NOT the full window product.
        // - But once zeroCount == 0, this becomes exactly the full window product.
        long product = 1L;

        // Counts how many zeros are inside the current window.
        // A window is valid if and only if zeroCount == 0.
        int zeroCount = 0;

        // Build the very first window [0 .. k-1].
        for (int i = 0; i < k; i++) {
            if (nums[i] == 0) {
                // Zero makes the window invalid, so we count it.
                zeroCount++;
            } else {
                // Non-zero values are multiplied into the maintained product.
                product *= nums[i];
            }
        }

        // We need to know whether we have found at least one valid window.
        boolean foundValidWindow = false;

        // This will store the best product among valid windows.
        long best = Long.MIN_VALUE;

        // Check the initial window.
        if (zeroCount == 0) {
            foundValidWindow = true;
            best = product;
        }

        // Now slide the window one position at a time.
        // New window start will be from 1 up to n-k.
        for (int right = k; right < n; right++) {
            // The outgoing element is the one leaving from the left side.
            int outgoing = nums[right - k];

            // Step 1: Remove the outgoing element from the current window state.
            if (outgoing == 0) {
                // If the outgoing element was zero, it was only affecting zeroCount.
                zeroCount--;
            } else {
                // If the outgoing element was non-zero, it was included in product,
                // so divide it out.
                product /= outgoing;
            }

            // The incoming element is the new element entering from the right side.
            int incoming = nums[right];

            // Step 2: Add the incoming element to the current window state.
            if (incoming == 0) {
                // A zero enters the window, so the window becomes invalid
                // unless there are no other zeros and this is later removed.
                zeroCount++;
            } else {
                // Multiply the incoming non-zero value into the maintained product.
                product *= incoming;
            }

            // Step 3: If there are no zeros, this window is valid.
            if (zeroCount == 0) {
                if (!foundValidWindow) {
                    // First valid window found.
                    foundValidWindow = true;
                    best = product;
                } else {
                    // Update the maximum product.
                    best = Math.max(best, product);
                }
            }
        }

        // If no valid window was ever found, return 0 as required.
        return foundValidWindow ? best : 0L;
    }

    /**
     * Utility method to print an array in a beginner-friendly format.
     *
     * @param nums the array to convert to a string
     * @return a string representation of the array
     * Time complexity: O(n), where n is nums.length.
     * Space complexity: O(n), due to string construction.
     */
    public String arrayToString(int[] nums) {
        return Arrays.toString(nums);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo cases shown here.
     * Space complexity: O(1), excluding the sample arrays themselves.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {2, -3, 4, -1, 5};
        int k1 = 3;
        long result1 = solution.maxProductScore(nums1, k1);
        System.out.println("Example 1:");
        System.out.println("nums = " + solution.arrayToString(nums1) + ", k = " + k1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 12");
        System.out.println();

        int[] nums2 = {0, -2, -3, 4, 0, 5};
        int k2 = 2;
        long result2 = solution.maxProductScore(nums2, k2);
        System.out.println("Example 2:");
        System.out.println("nums = " + solution.arrayToString(nums2) + ", k = " + k2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: 6");
        System.out.println();

        int[] nums3 = {0, 0, 0};
        int k3 = 2;
        long result3 = solution.maxProductScore(nums3, k3);
        System.out.println("Additional Example 3:");
        System.out.println("nums = " + solution.arrayToString(nums3) + ", k = " + k3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        int[] nums4 = {-1, -2, -3, -4};
        int k4 = 2;
        long result4 = solution.maxProductScore(nums4, k4);
        System.out.println("Additional Example 4:");
        System.out.println("nums = " + solution.arrayToString(nums4) + ", k = " + k4);
        System.out.println("Output: " + result4);
        System.out.println("Expected: 12");
    }
}