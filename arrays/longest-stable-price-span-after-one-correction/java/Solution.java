import java.util.*;

/*
Problem Title: Longest Stable Price Span After One Correction

Problem Description:
A retail analytics team records the price of the same product once per day in an integer array prices,
where prices[i] is the observed price on day i.

A span of days is considered stable if all values in that contiguous subarray are equal.
However, the team knows that at most one recorded day in a span may be wrong due to a data entry mistake.
You are allowed to correct the value of at most one element inside a chosen contiguous subarray to any integer you want.

Return the length of the longest contiguous subarray that can be made stable after applying at most one correction.

In other words, find the maximum length of a subarray such that by changing zero or one element in that subarray,
every value in the subarray can become identical.

This is an array problem focused on finding the best window efficiently.
A brute-force check over all subarrays will be too slow for large inputs.

Constraints:
- 1 <= prices.length <= 2 * 10^5
- 1 <= prices[i] <= 10^9

Example 1:
Input: prices = [5,5,7,5,5,5]
Output: 6
Explanation: Change the 7 to 5. Then the entire array becomes stable, so the answer is 6.

Example 2:
Input: prices = [3,3,4,4,4,3]
Output: 4
Explanation: The longest valid span is [4,4,4,3]. By changing the last 3 to 4, the span becomes [4,4,4,4] with length 4.
No length-5 or length-6 subarray can be made all equal using only one correction.
*/

public class Solution {

    /**
     * Computes the length of the longest contiguous subarray that can be turned into
     * an array of identical values by changing at most one element.
     *
     * Core idea:
     * A subarray can be made fully equal with at most one correction if and only if
     * inside that subarray, all elements are already the same except possibly one.
     *
     * That means:
     * window length - (maximum frequency of any value inside the window) <= 1
     *
     * We use a sliding window:
     * - Expand the right boundary one step at a time.
     * - Track frequencies of values inside the current window.
     * - Track the highest frequency seen in the current window.
     * - If more than one correction would be needed, move the left boundary rightward.
     *
     * Important note:
     * We keep a "possibly stale" max frequency, which is a standard and correct optimization
     * for this sliding-window pattern. Even if the stored max frequency is slightly larger
     * than the true current-window max after shrinking, the algorithm still returns the
     * correct maximum answer because the window only grows when it is potentially valid,
     * and every recorded answer corresponds to a feasible maximum encountered during the scan.
     *
     * @param prices the array of daily observed prices
     * @return the maximum length of a contiguous subarray that can become all equal after at most one correction
     * Time complexity: O(n), where n is prices.length
     * Space complexity: O(k), where k is the number of distinct values inside the sliding window (up to O(n))
     */
    public int longestStablePriceSpanAfterOneCorrection(int[] prices) {
        // Frequency map for values currently inside the sliding window.
        Map<Integer, Integer> frequency = new HashMap<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int best = 0;

        // Highest frequency of any single value seen in the current window expansion process.
        int maxFrequencyInWindow = 0;

        // Expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < prices.length; right++) {
            int currentValue = prices[right];

            // Add the new rightmost value into the frequency map.
            int newCount = frequency.getOrDefault(currentValue, 0) + 1;
            frequency.put(currentValue, newCount);

            // Update the maximum frequency seen in the current window.
            // This tells us how many elements already match the most common value.
            maxFrequencyInWindow = Math.max(maxFrequencyInWindow, newCount);

            // Current window length.
            int windowLength = right - left + 1;

            // If more than one element would need correction, the window is invalid.
            // We must shrink it from the left until it becomes valid again.
            //
            // Needed corrections = windowLength - maxFrequencyInWindow
            // Valid if needed corrections <= 1
            while (windowLength - maxFrequencyInWindow > 1) {
                int leftValue = prices[left];

                // Remove the leftmost value from the window.
                int updatedCount = frequency.get(leftValue) - 1;
                if (updatedCount == 0) {
                    frequency.remove(leftValue);
                } else {
                    frequency.put(leftValue, updatedCount);
                }

                // Move left boundary rightward.
                left++;

                // Recompute current window length after shrinking.
                windowLength = right - left + 1;
            }

            // At this point, the window is valid:
            // it can be made all equal by changing at most one element.
            best = Math.max(best, windowLength);
        }

        return best;
    }

    /**
     * A helper method that prints an array in a readable format.
     *
     * @param prices the array to print
     * @return a string representation of the array
     * Time complexity: O(n), where n is prices.length
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] prices) {
        return Arrays.toString(prices);
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement
     * and a few additional examples.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of demonstrated examples)
     * Space complexity: O(1) extra beyond the arrays used for demonstration
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] prices1 = {5, 5, 7, 5, 5, 5};
        int result1 = solution.longestStablePriceSpanAfterOneCorrection(prices1);
        System.out.println("Input:  " + solution.arrayToString(prices1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 6");
        System.out.println();

        // Sample 2
        int[] prices2 = {3, 3, 4, 4, 4, 3};
        int result2 = solution.longestStablePriceSpanAfterOneCorrection(prices2);
        System.out.println("Input:  " + solution.arrayToString(prices2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional checks
        int[] prices3 = {1};
        int result3 = solution.longestStablePriceSpanAfterOneCorrection(prices3);
        System.out.println("Input:  " + solution.arrayToString(prices3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 1");
        System.out.println();

        int[] prices4 = {2, 2, 2, 2};
        int result4 = solution.longestStablePriceSpanAfterOneCorrection(prices4);
        System.out.println("Input:  " + solution.arrayToString(prices4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 4");
        System.out.println();

        int[] prices5 = {1, 2, 1, 1, 3, 1};
        int result5 = solution.longestStablePriceSpanAfterOneCorrection(prices5);
        System.out.println("Input:  " + solution.arrayToString(prices5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 4");
    }
}