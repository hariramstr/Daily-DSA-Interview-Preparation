/*
Problem Title: Maximum Score from Choosing One Value Per Day Range

Problem Description:
You are given an integer array values where values[i] represents the score available on day i.
You must build a schedule by choosing a set of days to collect score, subject to one rule:
if you choose day i, then you cannot choose either adjacent day i - 1 or i + 1.
In other words, any two chosen days must be at least 2 indices apart.

Your task is to return the maximum total score you can collect.

This models a realistic planning problem where collecting score on one day requires a cooldown
on neighboring days. The array may contain positive, zero, or negative values. You are allowed
to skip any day, including all days if that gives a better result.

Write a function that computes the best possible total score.

Constraints:
- 1 <= values.length <= 100000
- -10000 <= values[i] <= 10000
- The answer fits in a 32-bit signed integer.

Example 1:
Input: values = [4, 2, 7, 9, 3]
Corrected Output: 14
Explanation: Choose days 0, 2, and 4 for 4 + 7 + 3 = 14.

Example 2:
Input: values = [-5, -1, -8]
Output: 0
Explanation: Since all scores are negative, the best choice is to skip every day.
*/

import java.util.*;

public class Solution {

    /**
     * Computes the maximum total score that can be collected such that
     * no two chosen days are adjacent.
     *
     * This is the classic dynamic programming pattern:
     * for each day, decide whether to:
     * 1) skip the current day, or
     * 2) take the current day and add its value to the best result from two days back.
     *
     * Important detail:
     * Because values may be negative, we are allowed to skip all days.
     * Therefore, the answer should never be negative; the minimum valid result is 0.
     *
     * @param values an array where values[i] is the score available on day i
     * @return the maximum total score obtainable without choosing adjacent days
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(1), because only a constant amount of extra memory is used
     */
    public int maxScore(int[] values) {
        // Defensive handling:
        // Although the constraints guarantee at least one element,
        // it is still good practice to safely handle null or empty input.
        if (values == null || values.length == 0) {
            return 0;
        }

        // We will optimize the DP to constant space.
        //
        // Let:
        // prevTwo = best answer considering days up to index i - 2
        // prevOne = best answer considering days up to index i - 1
        //
        // When processing day i:
        // - If we skip day i, total remains prevOne
        // - If we take day i, total becomes prevTwo + values[i]
        //
        // So:
        // current = max(prevOne, prevTwo + values[i])
        //
        // Since skipping all days is allowed, we start from 0.
        int prevTwo = 0;
        int prevOne = 0;

        // Process each day from left to right.
        for (int i = 0; i < values.length; i++) {
            // Option 1: do not choose the current day.
            // Then the best total is simply the best total up to the previous day.
            int skipCurrent = prevOne;

            // Option 2: choose the current day.
            // If we choose day i, we cannot choose day i - 1,
            // so we add values[i] to the best total up to day i - 2.
            int takeCurrent = prevTwo + values[i];

            // The best result up to the current day is the better of the two choices.
            int current = Math.max(skipCurrent, takeCurrent);

            // Slide the DP window forward:
            // - old prevOne becomes new prevTwo
            // - current becomes new prevOne
            prevTwo = prevOne;
            prevOne = current;
        }

        // prevOne now stores the best answer for the entire array.
        return prevOne;
    }

    /**
     * Computes the maximum total score using a full DP array.
     *
     * This version is more explicit and beginner-friendly for learning,
     * because it stores the best answer for every prefix of the array.
     *
     * dp[i] = maximum score considering the first i elements (indices 0 to i - 1)
     *
     * Transition:
     * - Skip current element: dp[i - 1]
     * - Take current element: dp[i - 2] + values[i - 1]
     *
     * Therefore:
     * dp[i] = max(dp[i - 1], dp[i - 2] + values[i - 1])
     *
     * @param values an array where values[i] is the score available on day i
     * @return the maximum total score obtainable without choosing adjacent days
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(n), due to the DP array
     */
    public int maxScoreWithDpArray(int[] values) {
        if (values == null || values.length == 0) {
            return 0;
        }

        int n = values.length;

        // dp[0] means: considering zero elements, best score is 0.
        // dp[1] means: considering only values[0], best score is max(0, values[0]).
        int[] dp = new int[n + 1];
        dp[0] = 0;
        dp[1] = Math.max(0, values[0]);

        // Build the DP table one position at a time.
        for (int i = 2; i <= n; i++) {
            // Current actual array index is i - 1.
            int skipCurrent = dp[i - 1];
            int takeCurrent = dp[i - 2] + values[i - 1];
            dp[i] = Math.max(skipCurrent, takeCurrent);
        }

        return dp[n];
    }

    /**
     * Helper method to print an input array and the computed result.
     *
     * @param values the input array of day scores
     * @return the computed maximum score for the given array
     * Time complexity: O(n), due to array-to-string conversion for printing
     * Space complexity: O(n), due to string creation during printing
     */
    public int demonstrateCase(int[] values) {
        int result = maxScore(values);
        System.out.println("Input: " + Arrays.toString(values));
        System.out.println("Maximum score: " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution on sample inputs.
     *
     * It verifies the examples from the problem statement:
     * - [4, 2, 7, 9, 3] should produce 14
     * - [-5, -1, -8] should produce 0
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case
     * Space complexity: O(1) extra space for the optimized solver
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // values = [4, 2, 7, 9, 3]
        //
        // Valid best choice:
        // choose indices 0, 2, and 4 -> 4 + 7 + 3 = 14
        //
        // Let's reason carefully:
        // - 0 and 2 are not adjacent, so allowed
        // - 2 and 4 are not adjacent, so allowed
        // Therefore total 14 is valid and is the correct maximum.
        int[] example1 = {4, 2, 7, 9, 3};
        int result1 = solution.demonstrateCase(example1);
        System.out.println("Expected: 14");
        System.out.println("Matches expected: " + (result1 == 14));
        System.out.println();

        // Example 2:
        // values = [-5, -1, -8]
        //
        // Every value is negative, and skipping all days is allowed.
        // So the best answer is 0.
        int[] example2 = {-5, -1, -8};
        int result2 = solution.demonstrateCase(example2);
        System.out.println("Expected: 0");
        System.out.println("Matches expected: " + (result2 == 0));
        System.out.println();

        // Additional demonstration cases for clarity.

        // Single positive value: best is to take it.
        int[] extra1 = {10};
        solution.demonstrateCase(extra1);

        // Single negative value: best is to skip it.
        int[] extra2 = {-10};
        solution.demonstrateCase(extra2);

        // Mixed values:
        // Best is 5 + 10 + 6 = 21
        int[] extra3 = {5, 1, 10, 1, 6};
        solution.demonstrateCase(extra3);

        // Compare both implementations on one sample to show they agree.
        int[] compareCase = {3, 2, 5, 10, 7};
        int optimized = solution.maxScore(compareCase);
        int dpArray = solution.maxScoreWithDpArray(compareCase);
        System.out.println("Comparison case: " + Arrays.toString(compareCase));
        System.out.println("Optimized DP result: " + optimized);
        System.out.println("Full DP array result: " + dpArray);
        System.out.println("Both methods agree: " + (optimized == dpArray));
    }
}