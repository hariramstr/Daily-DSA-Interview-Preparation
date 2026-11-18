/*
Title: Maximum Profit from One Restock and One Sale Window

Problem Description:
You are given an integer array prices where prices[i] is the market price of a product on day i.
A retailer wants to choose exactly one contiguous restock window and one contiguous sale window
such that the sale window starts strictly after the restock window ends.

The retailer buys one unit of product on every day in the restock window and sells one unit on
every day in the sale window. The profit of this plan is defined as:

    (sum of prices in the sale window) - (sum of prices in the restock window)

Your task is to return the maximum possible profit.

Both windows must be non-empty, contiguous, and must not overlap. The sale window must begin at
least one day after the restock window ends. You are not required to use all days. If every valid
plan produces a negative profit, return the least negative value among all valid plans.

This problem models choosing a low-cost buying period followed by a high-revenue selling period,
but the optimal answer may involve windows of different lengths. A brute-force solution that checks
all pairs of subarrays is too slow for large inputs, so you should design an efficient array-based
algorithm.

Constraints:
- 2 <= prices.length <= 200000
- -100000 <= prices[i] <= 100000
- The answer fits in a 64-bit signed integer
*/

import java.util.*;

public class Solution {

    /**
     * Computes the maximum possible profit by choosing:
     * 1) one non-empty contiguous restock window
     * 2) one non-empty contiguous sale window
     * such that the sale window starts strictly after the restock window ends.
     *
     * Core idea:
     * - For every split position i, imagine:
     *   - the restock window must lie completely in prices[0..i]
     *   - the sale window must lie completely in prices[i+1..n-1]
     * - Then the best profit for that split is:
     *      (maximum subarray sum on the right side)
     *      -
     *      (minimum subarray sum on the left side)
     * - We precompute:
     *   - leftMin[i]  = minimum subarray sum anywhere in prices[0..i]
     *   - rightMax[i] = maximum subarray sum anywhere in prices[i..n-1]
     * - Then answer is:
     *      max over i from 0 to n-2 of (rightMax[i+1] - leftMin[i])
     *
     * This works because:
     * - the restock window wants the smallest possible sum
     * - the sale window wants the largest possible sum
     * - the split guarantees non-overlap and correct order
     *
     * @param prices the array of daily prices
     * @return the maximum profit as a long
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long maximumProfit(int[] prices) {
        int n = prices.length;

        // leftMin[i] will store the minimum subarray sum found anywhere
        // in the prefix prices[0..i].
        long[] leftMin = buildLeftMinimumSubarraySums(prices);

        // rightMax[i] will store the maximum subarray sum found anywhere
        // in the suffix prices[i..n-1].
        long[] rightMax = buildRightMaximumSubarraySums(prices);

        // We now try every valid split:
        // - restock window is fully on the left side ending at or before i
        // - sale window is fully on the right side starting at or after i+1
        //
        // Since both windows must be non-empty, i can only go from 0 to n-2.
        long answer = Long.MIN_VALUE;

        for (int i = 0; i < n - 1; i++) {
            long candidateProfit = rightMax[i + 1] - leftMin[i];
            answer = Math.max(answer, candidateProfit);
        }

        return answer;
    }

    /**
     * Builds an array where each position i stores the minimum subarray sum
     * that appears anywhere in the prefix prices[0..i].
     *
     * We use a "minimum version" of Kadane's algorithm:
     * - minEndingHere = minimum subarray sum that must end exactly at current index
     * - bestSoFar     = minimum subarray sum seen anywhere so far
     *
     * Transition:
     * - Either start a new subarray at prices[i]
     * - Or extend the previous minimum-ending subarray
     *
     * @param prices the input price array
     * @return an array leftMin where leftMin[i] is the minimum subarray sum in prices[0..i]
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildLeftMinimumSubarraySums(int[] prices) {
        int n = prices.length;
        long[] leftMin = new long[n];

        // At index 0, the only non-empty subarray is [prices[0]] itself.
        long minEndingHere = prices[0];
        long bestSoFar = prices[0];
        leftMin[0] = bestSoFar;

        // Process from left to right.
        for (int i = 1; i < n; i++) {
            // To get the minimum subarray sum ending exactly at i:
            // - either take only prices[i]
            // - or extend the previous minimum-ending subarray
            minEndingHere = Math.min((long) prices[i], minEndingHere + prices[i]);

            // Update the best minimum seen anywhere in the prefix so far.
            bestSoFar = Math.min(bestSoFar, minEndingHere);

            // Store the answer for prefix [0..i].
            leftMin[i] = bestSoFar;
        }

        return leftMin;
    }

    /**
     * Builds an array where each position i stores the maximum subarray sum
     * that appears anywhere in the suffix prices[i..n-1].
     *
     * We process from right to left using a mirrored Kadane idea:
     * - maxStartingHere = maximum subarray sum that must start exactly at current index
     * - bestSoFar       = maximum subarray sum seen anywhere in the suffix so far
     *
     * Transition:
     * - Either start a new subarray at prices[i]
     * - Or extend into the best subarray that starts at i+1
     *
     * @param prices the input price array
     * @return an array rightMax where rightMax[i] is the maximum subarray sum in prices[i..n-1]
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildRightMaximumSubarraySums(int[] prices) {
        int n = prices.length;
        long[] rightMax = new long[n];

        // At the last index, the only non-empty subarray is [prices[n-1]] itself.
        long maxStartingHere = prices[n - 1];
        long bestSoFar = prices[n - 1];
        rightMax[n - 1] = bestSoFar;

        // Process from right to left.
        for (int i = n - 2; i >= 0; i--) {
            // To get the maximum subarray sum starting exactly at i:
            // - either take only prices[i]
            // - or extend the previous maximum-starting subarray
            maxStartingHere = Math.max((long) prices[i], prices[i] + maxStartingHere);

            // Update the best maximum seen anywhere in the suffix so far.
            bestSoFar = Math.max(bestSoFar, maxStartingHere);

            // Store the answer for suffix [i..n-1].
            rightMax[i] = bestSoFar;
        }

        return rightMax;
    }

    /**
     * Helper method to print an integer array in a friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on sample inputs and a few extra checks.
     *
     * Note about Example 1:
     * The problem statement's provided output says 11, but the explanation repeatedly
     * computes values that lead to 9, and the true optimal answer is 9.
     *
     * For prices = [4, -2, 3, -1, 5]:
     * - Best restock window is [-2] with sum = -2
     * - Best sale window after that is [3, -1, 5] with sum = 7
     * - Profit = 7 - (-2) = 9
     *
     * So this implementation correctly returns 9 for that example.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstrations, excluding the called methods
     * Space complexity: O(1) auxiliary, excluding the called methods
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] prices1 = {4, -2, 3, -1, 5};
        long result1 = solution.maximumProfit(prices1);
        System.out.println("Input:  " + solution.arrayToString(prices1));
        System.out.println("Output: " + result1);
        System.out.println("Expected by correct reasoning: 9");
        System.out.println();

        int[] prices2 = {7, 6, 5, 4};
        long result2 = solution.maximumProfit(prices2);
        System.out.println("Input:  " + solution.arrayToString(prices2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: -1");
        System.out.println();

        int[] prices3 = {1, 2};
        long result3 = solution.maximumProfit(prices3);
        System.out.println("Input:  " + solution.arrayToString(prices3));
        System.out.println("Output: " + result3);
        System.out.println("Explanation: restock [1], sale [2], profit = 1");
        System.out.println();

        int[] prices4 = {-5, -1, -3, 10};
        long result4 = solution.maximumProfit(prices4);
        System.out.println("Input:  " + solution.arrayToString(prices4));
        System.out.println("Output: " + result4);
        System.out.println("One optimal plan: restock [-5, -1, -3] sum = -9, sale [10] sum = 10, profit = 19");
    }
}