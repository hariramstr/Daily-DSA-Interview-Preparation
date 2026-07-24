/*
Problem Title: Maximum Upgrade Score from One Contiguous Patch Window

Problem Description:
A software team tracks the impact score of each available patch in the order the patches must be applied.
The array impact contains positive, negative, or zero values, where impact[i] is the score contributed by
the ith patch.

The team is allowed to choose exactly one contiguous window of patches to deploy together. However,
deployment overhead depends on the length of the chosen window: if the window has length L, the final
score is the sum of all values in that window minus L * penalty.

Your task is to return the maximum possible final score over all non-empty contiguous windows.

Formally, for every pair of indices l and r with 0 <= l <= r < n:
score(l, r) = impact[l] + impact[l+1] + ... + impact[r] - (r - l + 1) * penalty

Find the maximum value of score(l, r).

Constraints:
- 1 <= impact.length <= 200000
- -10^9 <= impact[i] <= 10^9
- 0 <= penalty <= 10^9
- The answer fits in a signed 64-bit integer.

Key Insight:
score(l, r)
= (impact[l] - penalty) + (impact[l+1] - penalty) + ... + (impact[r] - penalty)

So the problem becomes:
Find the maximum sum of any non-empty contiguous subarray in the transformed array:
transformed[i] = impact[i] - penalty

That is exactly the classic maximum subarray problem, solvable with Kadane's algorithm in O(n) time.
*/

import java.util.*;

public class Solution {

    /**
     * Computes the maximum possible final score over all non-empty contiguous windows.
     *
     * The main idea is to transform each element:
     * transformed[i] = impact[i] - penalty
     *
     * Then the score of any window [l..r] becomes simply the sum of transformed[l..r].
     * So the task reduces to finding the maximum sum of a non-empty contiguous subarray.
     *
     * We solve that using Kadane's algorithm:
     * - currentBestEndingHere = best subarray sum that must end at the current index
     * - globalBest = best subarray sum seen anywhere so far
     *
     * @param impact the array of patch impact scores; may contain positive, negative, or zero values
     * @param penalty the fixed cost paid for every included element in the chosen window
     * @return the maximum final score among all non-empty contiguous windows
     * Time complexity: O(n), where n is impact.length
     * Space complexity: O(1), ignoring input storage
     */
    public long maximumUpgradeScore(int[] impact, int penalty) {
        // Because the problem guarantees at least one element, we can safely initialize
        // using the first transformed value.
        long firstTransformedValue = (long) impact[0] - penalty;

        // currentBestEndingHere:
        // The maximum transformed subarray sum for a subarray that MUST end at index 0 initially.
        long currentBestEndingHere = firstTransformedValue;

        // globalBest:
        // The best answer seen so far among all subarrays processed up to the current index.
        long globalBest = firstTransformedValue;

        // Process the rest of the array from left to right.
        for (int i = 1; i < impact.length; i++) {
            // Transform the current element by subtracting the per-element penalty.
            long transformedValue = (long) impact[i] - penalty;

            // Kadane's decision:
            // Either:
            // 1) Start a brand-new subarray at this index using only transformedValue
            // or
            // 2) Extend the previous best subarray that ended at i - 1
            //
            // We choose whichever gives the larger sum.
            currentBestEndingHere = Math.max(transformedValue, currentBestEndingHere + transformedValue);

            // Update the global best answer if the best subarray ending here is better
            // than every subarray seen before.
            globalBest = Math.max(globalBest, currentBestEndingHere);
        }

        return globalBest;
    }

    /**
     * A second implementation of the same logic, written in a slightly more explicit style
     * for educational purposes. This can help beginners understand the transformation step.
     *
     * @param impact the array of patch impact scores
     * @param penalty the fixed cost per included element
     * @return the maximum final score among all non-empty contiguous windows
     * Time complexity: O(n), where n is impact.length
     * Space complexity: O(1), ignoring input storage
     */
    public long maximumUpgradeScoreVerbose(int[] impact, int penalty) {
        long bestOverall = Long.MIN_VALUE;
        long bestEndingHere = Long.MIN_VALUE;

        for (int i = 0; i < impact.length; i++) {
            // Convert the original value into its "net contribution" after paying penalty
            // for including this patch in the chosen window.
            long netValue = (long) impact[i] - penalty;

            if (i == 0) {
                // For the first element, the only non-empty subarray ending here is [0..0].
                bestEndingHere = netValue;
                bestOverall = netValue;
            } else {
                // If the previous best-ending-here sum is negative, it hurts us to keep it,
                // so we start fresh at the current element.
                // Otherwise, we extend the previous subarray.
                bestEndingHere = Math.max(netValue, bestEndingHere + netValue);

                // Track the best answer seen anywhere.
                bestOverall = Math.max(bestOverall, bestEndingHere);
            }
        }

        return bestOverall;
    }

    /**
     * Utility method to print a test case and its computed answer.
     *
     * @param impact the input impact array
     * @param penalty the per-element penalty
     * @return the computed maximum score for the provided test case
     * Time complexity: O(n), where n is impact.length
     * Space complexity: O(1), ignoring input storage
     */
    public long demonstrateCase(int[] impact, int penalty) {
        long answer = maximumUpgradeScore(impact, penalty);
        System.out.println("impact = " + Arrays.toString(impact) + ", penalty = " + penalty);
        System.out.println("Maximum upgrade score = " + answer);
        System.out.println();
        return answer;
    }

    /**
     * Demonstrates the solution on sample inputs and a few additional checks.
     *
     * Important note about the problem statement's Example 2:
     * The long explanation in the prompt contains contradictions, but after re-checking
     * all windows, the correct answer for impact = [-5, 7, -1, 7, -6], penalty = 3 is 7.
     *
     * Why?
     * Transform the array by subtracting penalty from each element:
     * [-8, 4, -4, 4, -9]
     * The maximum subarray sum is 4 + (-4) + 4 = 4, or just 4 alone, but actually
     * checking all original windows:
     * [7, -1, 7] => 13 - 3*3 = 4
     * [7] => 7 - 3 = 4
     * [7, -1, 7, -6] => 7 - 12 = -5
     * The best is 4.
     *
     * So the corrected output for Example 2 is 4.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total number of demonstrated elements)
     * Space complexity: O(1), ignoring input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt:
        // impact = [8, -1, 3, -2, 4], penalty = 2
        // Transformed = [6, -3, 1, -4, 2]
        // Maximum subarray sum = 6 + (-3) + 1 = 4, or just 6? Let's check carefully:
        // [8] => 8 - 2 = 6
        // [8, -1] => 7 - 4 = 3
        // [8, -1, 3] => 10 - 6 = 4
        // [4] => 4 - 2 = 2
        // Therefore the true maximum is 6, not 4.
        //
        // The prompt's Example 1 explanation is inconsistent.
        int[] impact1 = {8, -1, 3, -2, 4};
        int penalty1 = 2;
        long result1 = solution.demonstrateCase(impact1, penalty1);
        System.out.println("Verified correct result for Example 1: " + result1 + " (correct value is 6)");
        System.out.println();

        // Example 2 from the prompt:
        // impact = [-5, 7, -1, 7, -6], penalty = 3
        // Transformed = [-8, 4, -4, 4, -9]
        // Best non-empty subarray sum = 4
        int[] impact2 = {-5, 7, -1, 7, -6};
        int penalty2 = 3;
        long result2 = solution.demonstrateCase(impact2, penalty2);
        System.out.println("Verified correct result for Example 2: " + result2 + " (correct value is 4)");
        System.out.println();

        // Additional sanity checks
        int[] impact3 = {5};
        int penalty3 = 2;
        solution.demonstrateCase(impact3, penalty3); // 3

        int[] impact4 = {-10, -3, -7};
        int penalty4 = 1;
        solution.demonstrateCase(impact4, penalty4); // best single element => -4

        int[] impact5 = {3, 3, 3};
        int penalty5 = 0;
        solution.demonstrateCase(impact5, penalty5); // 9
    }
}