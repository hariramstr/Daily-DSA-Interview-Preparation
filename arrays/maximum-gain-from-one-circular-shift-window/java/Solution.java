import java.util.*;

/*
Problem Title: Maximum Gain from One Circular Shift Window

Problem Description:
A factory runs in repeating shifts, so its hourly performance data is considered circular:
after the last hour, the next hour is the first hour again. You are given an integer array
gain where gain[i] is the net productivity change during hour i. Positive values increase
total output, and negative values reduce it.

Choose exactly one non-empty contiguous block of hours to analyze, where the block may wrap
from the end of the array back to the beginning because the schedule is circular. Return the
maximum possible sum of the chosen block.

In other words, you must find the largest sum among all non-empty circular subarrays of gain.

This problem is common in systems that monitor repeating schedules, rotating buffers, or cyclic
sensor logs. A valid solution should handle both standard subarrays and wrap-around subarrays
efficiently.

Constraints:
- 1 <= gain.length <= 100000
- -100000 <= gain[i] <= 100000
- The answer fits in a 32-bit signed integer.

Example 1:
Input: gain = [5, -3, 5]
Output: 10
Explanation: The best circular block is [5] from the end together with [5] from the beginning,
for a total of 10.

Example 2:
Input: gain = [-2, -3, -1]
Output: -1
Explanation: All values are negative, so the best non-empty block is the single hour with value -1.
*/

/**
 * A beginner-friendly solution for finding the maximum sum of a non-empty circular subarray.
 *
 * <p>The key idea is:
 * <ul>
 *     <li>Either the best subarray is a normal subarray that does not wrap around.</li>
 *     <li>Or the best subarray wraps around the end to the beginning.</li>
 * </ul>
 *
 * <p>For the normal case, we use Kadane's algorithm to find the maximum subarray sum.
 * For the wrap-around case, we observe:
 *
 * <pre>
 * maximum circular sum = total sum - minimum subarray sum
 * </pre>
 *
 * <p>Why? Because removing the minimum-sum middle segment leaves the maximum-sum wrapped segment.
 *
 * <p>Important edge case:
 * If all numbers are negative, then totalSum - minSubarraySum becomes 0, which would represent
 * choosing an empty subarray, but the problem requires a non-empty subarray. In that case,
 * the answer is simply the maximum element (which Kadane's maximum subarray already gives us).
 */
public class Solution {

    /**
     * Returns the maximum possible sum of a non-empty circular subarray.
     *
     * <p>Step-by-step idea:
     * <ol>
     *     <li>Compute the maximum subarray sum using Kadane's algorithm.</li>
     *     <li>Compute the minimum subarray sum using a mirrored Kadane-style process.</li>
     *     <li>Compute the total sum of the array.</li>
     *     <li>If all values are negative, return the normal maximum subarray sum.</li>
     *     <li>Otherwise, return the larger of:
     *         <ul>
     *             <li>normal maximum subarray sum</li>
     *             <li>total sum - minimum subarray sum</li>
     *         </ul>
     *     </li>
     * </ol>
     *
     * @param gain the circular array of hourly productivity changes
     * @return the maximum sum among all non-empty circular subarrays
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(1), using only a constant amount of extra space
     */
    public int maxSubarraySumCircular(int[] gain) {
        // We will track:
        // 1) totalSum: sum of all elements
        // 2) currentMax / maxSum: for standard Kadane maximum subarray
        // 3) currentMin / minSum: for minimum subarray
        //
        // We initialize everything with the first element so that:
        // - the subarray is guaranteed to be non-empty
        // - negative-only arrays are handled correctly
        int totalSum = gain[0];

        int currentMax = gain[0];
        int maxSum = gain[0];

        int currentMin = gain[0];
        int minSum = gain[0];

        // Process the rest of the array one element at a time.
        for (int i = 1; i < gain.length; i++) {
            int value = gain[i];

            // Add current value to total sum.
            totalSum += value;

            // -------------------------------
            // Standard Kadane for maximum sum
            // -------------------------------
            // At position i, the best maximum-sum subarray ending at i is either:
            // 1) start fresh from gain[i]
            // 2) extend the previous best ending subarray
            currentMax = Math.max(value, currentMax + value);

            // Update the best maximum seen so far anywhere in the array.
            maxSum = Math.max(maxSum, currentMax);

            // -------------------------------
            // Kadane variant for minimum sum
            // -------------------------------
            // At position i, the best minimum-sum subarray ending at i is either:
            // 1) start fresh from gain[i]
            // 2) extend the previous minimum ending subarray
            currentMin = Math.min(value, currentMin + value);

            // Update the best minimum seen so far anywhere in the array.
            minSum = Math.min(minSum, currentMin);
        }

        // If maxSum is negative, then every element is negative.
        // In that case:
        // - the best non-empty subarray is simply the largest single value (or least negative)
        // - totalSum - minSum would incorrectly become 0, representing an empty subarray
        if (maxSum < 0) {
            return maxSum;
        }

        // Wrap-around case:
        // Remove the minimum-sum subarray from the total array,
        // and the remaining elements form the best wrapped subarray.
        int circularSum = totalSum - minSum;

        // The answer is the better of:
        // - non-wrapping best subarray
        // - wrapping best subarray
        return Math.max(maxSum, circularSum);
    }

    /**
     * Demonstrates the algorithm on sample inputs and a few extra cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm runs
     * Space complexity: O(1)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1 from the problem statement:
        // gain = [5, -3, 5]
        // Normal max subarray = 7 ([5, -3, 5])
        // Minimum subarray = -3
        // Total sum = 7
        // Circular max = 7 - (-3) = 10
        // Expected output: 10
        int[] gain1 = {5, -3, 5};
        System.out.println("Input: " + Arrays.toString(gain1));
        System.out.println("Output: " + solution.maxSubarraySumCircular(gain1));
        System.out.println("Expected: 10");
        System.out.println();

        // Sample input 2 from the problem statement:
        // gain = [-2, -3, -1]
        // All values are negative
        // Best non-empty subarray is [-1]
        // Expected output: -1
        int[] gain2 = {-2, -3, -1};
        System.out.println("Input: " + Arrays.toString(gain2));
        System.out.println("Output: " + solution.maxSubarraySumCircular(gain2));
        System.out.println("Expected: -1");
        System.out.println();

        // Extra example:
        // gain = [1, -2, 3, -2]
        // Normal max = 3
        // Circular max = 2
        // Expected answer = 3
        int[] gain3 = {1, -2, 3, -2};
        System.out.println("Input: " + Arrays.toString(gain3));
        System.out.println("Output: " + solution.maxSubarraySumCircular(gain3));
        System.out.println("Expected: 3");
        System.out.println();

        // Extra example:
        // gain = [3, -1, 2, -1]
        // Normal max = 4
        // Circular max = 4
        // Expected answer = 4
        int[] gain4 = {3, -1, 2, -1};
        System.out.println("Input: " + Arrays.toString(gain4));
        System.out.println("Output: " + solution.maxSubarraySumCircular(gain4));
        System.out.println("Expected: 4");
        System.out.println();

        // Extra example:
        // gain = [3, -2, 2, -3]
        // Normal max = 3
        // Circular max = 3
        // Expected answer = 3
        int[] gain5 = {3, -2, 2, -3};
        System.out.println("Input: " + Arrays.toString(gain5));
        System.out.println("Output: " + solution.maxSubarraySumCircular(gain5));
        System.out.println("Expected: 3");
    }
}