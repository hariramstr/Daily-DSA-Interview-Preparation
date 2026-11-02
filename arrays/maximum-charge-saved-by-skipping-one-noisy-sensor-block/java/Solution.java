import java.util.*;

/*
Problem Title: Maximum Charge Saved by Skipping One Noisy Sensor Block

Problem Description:
You are given an integer array readings where readings[i] represents the net battery impact of processing the i-th sensor reading in order.
A positive value means that processing that reading recharges the system by that amount, while a negative value means it consumes battery.

The device must process the stream from left to right, but it is allowed to skip exactly one contiguous block of readings.
Skipping a block means none of the values inside that block contribute to the final total.
The skipped block may be empty only if you choose not to skip anything.

Your task is to return the maximum possible total battery impact after processing the array and optionally skipping one contiguous block.

In other words, choose indices l and r with 0 <= l <= r < n and remove readings[l...r], or choose to remove nothing,
so that the sum of the remaining elements is as large as possible.

A correct solution must handle very large arrays efficiently.

Constraints:
- 1 <= readings.length <= 200000
- -1000000000 <= readings[i] <= 1000000000
- The answer fits in a signed 64-bit integer.

Example 1:
Input: readings = [4, -7, 3, 6, -2, 5]
Output: 16
Explanation: Skip the block [-7]. The remaining sum is 4 + 3 + 6 - 2 + 5 = 16.

Example 2:
Input: readings = [-5, -2, -8]
Output: 0
Explanation: Skip the entire array. The remaining sum is 0, which is better than keeping any negative values.

Key Insight:
If we remove a contiguous block, then:

remaining sum = total sum - sum(removed block)

So to maximize the remaining sum, we want to minimize the sum of the removed block.

We are also allowed to remove nothing, which means removed sum = 0.
Therefore, the best removable block sum is the minimum between:
- 0 (remove nothing)
- the minimum-sum contiguous subarray

Final answer:
answer = total sum - min(0, minimum subarray sum)

This can be computed in O(n) time using a Kadane-style scan for minimum subarray sum.
*/

public class Solution {

    /**
     * Computes the maximum possible total battery impact after optionally skipping
     * one contiguous block of readings.
     *
     * The method works by:
     * 1. Computing the total sum of the array.
     * 2. Finding the minimum-sum contiguous subarray.
     * 3. Deciding whether removing that subarray helps more than removing nothing.
     *
     * Why this works:
     * - If we remove a block with sum X, the remaining total becomes totalSum - X.
     * - To maximize the remaining total, we want X to be as small as possible.
     * - That means we want the minimum-sum contiguous subarray.
     * - However, if every subarray sum is positive, removing any block would hurt us,
     *   so we instead remove nothing, which is equivalent to removing a block of sum 0.
     *
     * @param readings the array where each value represents the battery impact of processing one reading
     * @return the maximum possible total battery impact after removing at most one contiguous block
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(1), because only a constant amount of extra memory is used
     */
    public long maximumChargeSaved(int[] readings) {
        // We use long for all running sums because:
        // - readings[i] can be as large as 1e9 in magnitude
        // - there can be up to 200000 elements
        // - the total can therefore exceed int range
        long totalSum = 0L;

        // This will track the minimum-sum subarray seen so far.
        // We initialize it to a very large value so the first real subarray sum replaces it.
        long minSubarraySum = Long.MAX_VALUE;

        // This variable represents:
        // "the minimum subarray sum that MUST end at the current index"
        //
        // This is the minimum-version of Kadane's algorithm.
        // At each position, we decide:
        // - start a new subarray at this element
        // - or extend the previous minimum-ending subarray
        long currentMinEndingHere = 0L;

        for (int value : readings) {
            totalSum += value;

            // Step-by-step logic:
            //
            // For minimum subarray ending at current position:
            // Either:
            //   1) start fresh with just this value
            //   2) extend the previous minimum-ending subarray by adding this value
            //
            // We choose whichever is smaller.
            currentMinEndingHere = Math.min((long) value, currentMinEndingHere + value);

            // Update the global minimum subarray sum if the current one is even smaller.
            minSubarraySum = Math.min(minSubarraySum, currentMinEndingHere);
        }

        // We are allowed to remove nothing.
        // Removing nothing means "removed sum = 0".
        //
        // So the best removable sum is:
        // - the minimum subarray sum, if it is negative (helpful to remove)
        // - 0, if all removable subarrays are non-negative (better to remove nothing)
        long bestRemovedSum = Math.min(0L, minSubarraySum);

        // Remaining sum after removing the best block.
        return totalSum - bestRemovedSum;
    }

    /**
     * A second public method that performs the same computation, included for clarity
     * and interview-style readability. It simply delegates to the main implementation.
     *
     * @param readings the battery impact readings array
     * @return the maximum possible total after optionally skipping one contiguous block
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(1)
     */
    public long solve(int[] readings) {
        return maximumChargeSaved(readings);
    }

    /**
     * Utility method to convert an int array to a readable string.
     *
     * @param arr the input array
     * @return a human-readable string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * Expected outputs:
     * - For [4, -7, 3, 6, -2, 5], output should be 16
     * - For [-5, -2, -8], output should be 0
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo size, excluding the internal solve calls
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] readings1 = {4, -7, 3, 6, -2, 5};
        long result1 = solution.maximumChargeSaved(readings1);
        System.out.println("Input:  " + solution.arrayToString(readings1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 16");
        System.out.println();

        int[] readings2 = {-5, -2, -8};
        long result2 = solution.maximumChargeSaved(readings2);
        System.out.println("Input:  " + solution.arrayToString(readings2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 0");
        System.out.println();

        // Additional quick sanity checks for beginner-friendly understanding:

        // If all values are positive, best choice is to remove nothing.
        int[] readings3 = {2, 4, 6};
        long result3 = solution.maximumChargeSaved(readings3);
        System.out.println("Input:  " + solution.arrayToString(readings3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 12");
        System.out.println();

        // If there is a negative block in the middle, removing it helps.
        int[] readings4 = {10, -3, -4, 8};
        long result4 = solution.maximumChargeSaved(readings4);
        System.out.println("Input:  " + solution.arrayToString(readings4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 18");
    }
}