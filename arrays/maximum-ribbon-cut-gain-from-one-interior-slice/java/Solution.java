/*
Problem Title: Maximum Ribbon Cut Gain from One Interior Slice

Problem Description:
You are given an integer array values where values[i] represents the profit contribution of the i-th ribbon segment in a production line.
You must choose exactly one contiguous subarray to keep, but before finalizing it, you are allowed to make one special operation:
remove exactly one interior segment from that chosen subarray. The removed segment cannot be the first or last element of the chosen subarray.
After the removal, the remaining left and right parts are stitched together, and the score of the choice is the sum of all remaining kept segments.

Your task is to return the maximum possible score.

In other words, choose indices l < k < r and maximize the sum of values[l..r] except values[k].
The chosen kept structure must still have at least one segment on both sides of the removed interior segment.

This is not the same as deleting any element from the full array. The removed element must lie strictly inside the selected range,
so the final kept result is formed by a non-empty prefix part and a non-empty suffix part of one larger contiguous block.

Constraints:
- 3 <= values.length <= 200000
- -1000000000 <= values[i] <= 1000000000
- The answer fits in a signed 64-bit integer.

Example 1:
Input: values = [4, -7, 5, 6]
Output: 15
Explanation: Choose the subarray [4, -7, 5, 6] and remove the interior value -7. The remaining score is 4 + 5 + 6 = 15.

Example 2:
Input: values = [3, -2, 4, -1, 2]
Output: 8
Explanation: Choose the subarray [3, -2, 4, -1, 2] and remove the interior value -2. The score becomes 3 + 4 + (-1) + 2 = 8.
Removing -1 instead gives 7, which is smaller.
*/

import java.util.*;

public class Solution {

    /**
     * Computes the maximum possible score after choosing one contiguous subarray
     * and removing exactly one interior element from it.
     *
     * Core idea:
     * If we remove index k, then we must keep:
     * - a non-empty contiguous subarray ending at k - 1
     * - a non-empty contiguous subarray starting at k + 1
     *
     * Therefore, for every possible removed position k:
     * answer candidate = bestLeftEndingAt[k - 1] + bestRightStartingAt[k + 1]
     *
     * Where:
     * - bestLeftEndingAt[i] = maximum sum of any non-empty subarray that ends exactly at i
     * - bestRightStartingAt[i] = maximum sum of any non-empty subarray that starts exactly at i
     *
     * We can compute both arrays in linear time using Kadane-style transitions.
     *
     * @param values the profit contributions of ribbon segments
     * @return the maximum achievable score after removing exactly one interior segment
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long maximumRibbonCutGain(int[] values) {
        int n = values.length;

        // This array will store:
        // leftBestEnd[i] = maximum sum of a non-empty contiguous subarray
        // that MUST end exactly at index i.
        //
        // Example:
        // values = [3, -2, 4]
        // leftBestEnd[0] = 3
        // leftBestEnd[1] = max(-2, 3 + (-2)) = 1
        // leftBestEnd[2] = max(4, 1 + 4) = 5
        //
        // This is the classic "maximum subarray ending here" DP.
        long[] leftBestEnd = new long[n];

        // Base case:
        // The only non-empty subarray ending at index 0 is [values[0]] itself.
        leftBestEnd[0] = values[0];

        // Fill from left to right.
        for (int i = 1; i < n; i++) {
            // Two choices for a subarray ending at i:
            //
            // 1) Start fresh at i:
            //    sum = values[i]
            //
            // 2) Extend the best subarray that ended at i - 1:
            //    sum = leftBestEnd[i - 1] + values[i]
            //
            // We take the better of the two.
            leftBestEnd[i] = Math.max((long) values[i], leftBestEnd[i - 1] + values[i]);
        }

        // This array will store:
        // rightBestStart[i] = maximum sum of a non-empty contiguous subarray
        // that MUST start exactly at index i.
        //
        // This is the symmetric version of Kadane from right to left.
        long[] rightBestStart = new long[n];

        // Base case:
        // The only non-empty subarray starting at index n - 1 is [values[n - 1]] itself.
        rightBestStart[n - 1] = values[n - 1];

        // Fill from right to left.
        for (int i = n - 2; i >= 0; i--) {
            // Two choices for a subarray starting at i:
            //
            // 1) Start and stop at i:
            //    sum = values[i]
            //
            // 2) Extend into the best subarray that starts at i + 1:
            //    sum = values[i] + rightBestStart[i + 1]
            //
            // We take the better of the two.
            rightBestStart[i] = Math.max((long) values[i], values[i] + rightBestStart[i + 1]);
        }

        // Now try every possible removed interior index k.
        //
        // Since the removed element must be strictly inside the chosen subarray,
        // there must be at least one kept element on the left and at least one kept element on the right.
        //
        // Therefore:
        // k can only be from 1 to n - 2.
        long answer = Long.MIN_VALUE;

        for (int k = 1; k <= n - 2; k++) {
            // If we remove values[k], then:
            //
            // - The left kept part must be a non-empty subarray ending at k - 1.
            //   The best possible sum for that is leftBestEnd[k - 1].
            //
            // - The right kept part must be a non-empty subarray starting at k + 1.
            //   The best possible sum for that is rightBestStart[k + 1].
            //
            // Stitching them together after removing k gives total:
            long candidate = leftBestEnd[k - 1] + rightBestStart[k + 1];

            // Update global maximum.
            answer = Math.max(answer, candidate);
        }

        return answer;
    }

    /**
     * A helper method that prints a detailed demonstration for one input array.
     *
     * @param values the input array to test
     * @return the computed maximum score for the given array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long demonstrate(int[] values) {
        long result = maximumRibbonCutGain(values);
        System.out.println("Input:  " + Arrays.toString(values));
        System.out.println("Output: " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     * Demonstrates the algorithm on the sample test cases from the problem statement
     * and a few additional sanity checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of demonstrated examples)
     * Space complexity: O(max input size among demonstrated examples)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1:
        // values = [4, -7, 5, 6]
        // Best choice:
        // choose [4, -7, 5, 6], remove interior -7
        // remaining score = 4 + 5 + 6 = 15
        solution.demonstrate(new int[]{4, -7, 5, 6});

        // Sample 2:
        // values = [3, -2, 4, -1, 2]
        // Best choice:
        // choose [3, -2, 4, -1, 2], remove interior -2
        // remaining score = 3 + 4 + (-1) + 2 = 8
        solution.demonstrate(new int[]{3, -2, 4, -1, 2});

        // Additional checks:

        // Minimum length case.
        // Only possible removed index is 1.
        // Keep values[0] and values[2].
        solution.demonstrate(new int[]{1, 100, 2}); // result = 3

        // All negative values.
        // We still must keep one non-empty left part and one non-empty right part.
        solution.demonstrate(new int[]{-5, -1, -3, -4});

        // Mixed values.
        solution.demonstrate(new int[]{10, -100, 20, 30, -5, 40});
    }
}