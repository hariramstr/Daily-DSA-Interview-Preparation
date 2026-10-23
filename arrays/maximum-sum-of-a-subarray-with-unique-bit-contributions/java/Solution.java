import java.util.*;

/*
 * Title: Maximum Sum of a Subarray With Unique Bit Contributions
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array nums of length n, where each value represents a feature mask
 * stored as a non-negative integer. A contiguous subarray is called clean if no bit position
 * is set in more than one element inside that subarray. In other words, for every pair of
 * different indices i and j in the chosen subarray, nums[i] & nums[j] must be 0.
 *
 * Your task is to return the maximum possible sum of values over all clean contiguous subarrays.
 *
 * This is not the same as requiring all numbers to be distinct. Two equal numbers may both appear
 * only if their bitwise AND is 0, which is impossible unless both are 0. Likewise, different
 * numbers may still conflict if they share any set bit. You must find the best contiguous range
 * under this bit-overlap rule.
 *
 * Return the maximum sum of any clean subarray.
 *
 * Constraints:
 * - 1 <= nums.length <= 2 * 10^5
 * - 0 <= nums[i] <= 10^9
 * - The answer fits in a 64-bit signed integer.
 *
 * Example 1:
 * Input: nums = [5,2,8,1]
 * Output: 11
 * Explanation: Binary forms are 0101, 0010, 1000, 0001. The subarray [2,8,1] is clean because
 * no bit is shared between any two elements, and its sum is 11. The full array is not clean
 * because 5 and 1 both use the lowest bit.
 *
 * Example 2:
 * Input: nums = [3,8,48,10]
 * Output: 59
 * Explanation: The entire array is not clean because 8 and 10 share bit 3.
 * The best clean subarray is [3,8,48] with sum 59. Here 3 uses bits 0 and 1, 8 uses bit 3,
 * and 48 uses bits 4 and 5, so there is no overlap.
 */

public class Solution {

    /**
     * Computes the maximum sum of any contiguous clean subarray.
     *
     * A subarray is clean when no bit position is used by more than one element in that subarray.
     * Equivalently, for every pair of different elements inside the window, their bitwise AND is 0.
     *
     * Core idea:
     * - Use a sliding window with two pointers.
     * - Maintain a bitmask named "usedBits" representing all bits currently present in the window.
     * - When trying to add nums[right]:
     *   - If it conflicts with usedBits, repeatedly remove elements from the left side
     *     until the conflict disappears.
     *   - Then add nums[right] safely.
     * - Track the running sum of the current clean window and update the answer.
     *
     * Why XOR removal works:
     * - The window is always kept clean.
     * - Therefore, each bit can appear in at most one element in the window.
     * - So when removing nums[left], toggling its bits out with XOR is correct.
     *
     * @param nums the input array of non-negative integers representing feature masks
     * @return the maximum possible sum among all clean contiguous subarrays
     * Time complexity: O(n), because each element enters and leaves the sliding window at most once
     * Space complexity: O(1), because only a few variables are used regardless of input size
     */
    public long maximumCleanSubarraySum(int[] nums) {
        int left = 0;

        // This bitmask stores the union of all bits currently present in the window [left, right].
        // Because the window is always clean, no bit is duplicated across elements in the window.
        int usedBits = 0;

        // Running sum of the current window.
        long currentSum = 0L;

        // Best answer seen so far.
        long best = 0L;

        // Expand the window one element at a time using "right".
        for (int right = 0; right < nums.length; right++) {
            int value = nums[right];

            // If value shares any bit with the current window, the window would become invalid.
            // So we keep shrinking from the left until there is no overlap.
            //
            // Conflict test:
            // (usedBits & value) != 0 means at least one bit is already used by another element.
            while ((usedBits & value) != 0) {
                int remove = nums[left];

                // Remove nums[left] from the running sum because it leaves the window.
                currentSum -= remove;

                // Remove its bits from the used bitmask.
                // XOR is safe here because in a clean window each bit appears in exactly one element.
                usedBits ^= remove;

                // Move the left boundary rightward.
                left++;
            }

            // Now there is no conflict, so we can safely include nums[right].
            usedBits |= value;
            currentSum += value;

            // Update the best answer with the current valid window sum.
            best = Math.max(best, currentSum);
        }

        return best;
    }

    /**
     * Helper method to print an array in a readable format.
     *
     * @param nums the array to print
     * @return a string representation of the array
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] nums) {
        return Arrays.toString(nums);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and a few additional sanity checks.
     *
     * Expected sample outputs:
     * - [5,2,8,1] -> 11
     * - [3,8,48,10] -> 59
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of demonstrated examples)
     * Space complexity: O(1) excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {5, 2, 8, 1};
        long result1 = solution.maximumCleanSubarraySum(nums1);
        System.out.println("Input: " + solution.arrayToString(nums1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 11");
        System.out.println();

        int[] nums2 = {3, 8, 48, 10};
        long result2 = solution.maximumCleanSubarraySum(nums2);
        System.out.println("Input: " + solution.arrayToString(nums2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 59");
        System.out.println();

        int[] nums3 = {0, 0, 0};
        long result3 = solution.maximumCleanSubarraySum(nums3);
        System.out.println("Input: " + solution.arrayToString(nums3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        int[] nums4 = {1, 2, 4, 8};
        long result4 = solution.maximumCleanSubarraySum(nums4);
        System.out.println("Input: " + solution.arrayToString(nums4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 15");
        System.out.println();

        int[] nums5 = {7, 7};
        long result5 = solution.maximumCleanSubarraySum(nums5);
        System.out.println("Input: " + solution.arrayToString(nums5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 7");
    }
}