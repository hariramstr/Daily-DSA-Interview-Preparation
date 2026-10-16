import java.util.*;

/*
Title: Maximum Notes from Skipping Adjacent Lectures
Difficulty: Easy
Topic: Dynamic Programming

Problem Description:
You are planning your day at a university conference. There are n lectures scheduled in a single hallway,
and each lecture has a note value representing how useful your notes would be if you attend that lecture.
However, attending two adjacent lectures is not allowed because there is no transition time between neighboring
rooms, so you must skip at least one lecture between any two lectures you choose.

Given an integer array notes where notes[i] is the note value of the i-th lecture, return the maximum total
note value you can collect.

You may choose to attend no lectures at all, so the answer can be 0 if every value is negative. This makes
the problem slightly more practical than simply forcing a choice.

A dynamic programming solution works well here. For each position, decide whether it is better to skip the
current lecture or attend it and add its value to the best answer from two positions earlier.

Constraints:
- 1 <= notes.length <= 100000
- -1000 <= notes[i] <= 1000

Example 1:
Input: notes = [5, 1, 6, 2, 4]
Output: 15
Explanation: Attend lectures with values 5, 6, and 4. They are not adjacent, and the total is 15.

Example 2:
Input: notes = [4, -2, 3, 7]
Output: 11
Explanation: The best choice is to attend lectures with values 4 and 7 for a total of 11. Attending 3 and 7
is not allowed because they are adjacent.

Your task is to compute the maximum possible sum efficiently, ideally in linear time. Be careful with small
arrays and with negative values, where skipping can be better than attending.
*/

public class Solution {

    /**
     * Computes the maximum total note value that can be collected without attending
     * two adjacent lectures.
     *
     * This is the space-optimized dynamic programming version.
     *
     * Core idea:
     * For each lecture, we have exactly two choices:
     * 1. Skip it -> keep the best answer we already had up to the previous lecture.
     * 2. Attend it -> add its value to the best answer from two lectures earlier.
     *
     * Because each state depends only on the previous two states, we do not need a full DP array.
     * We only keep two rolling values.
     *
     * @param notes the array where notes[i] is the note value of the i-th lecture
     * @return the maximum total note value possible without choosing adjacent lectures;
     *         returns 0 if skipping all lectures is best
     * Time complexity: O(n), where n is the number of lectures
     * Space complexity: O(1), excluding input storage
     */
    public int maxNotes(int[] notes) {
        // Defensive handling:
        // Although the constraints guarantee at least one element,
        // handling null or empty input makes the method safer and beginner-friendly.
        if (notes == null || notes.length == 0) {
            return 0;
        }

        // prevTwo represents the best answer for lectures up to index i - 2.
        // At the beginning, before processing any lecture, that value is 0
        // because choosing no lectures is allowed.
        int prevTwo = 0;

        // prevOne represents the best answer for lectures up to index i - 1.
        // Initially, before processing the first lecture, it is also 0.
        int prevOne = 0;

        // Process each lecture from left to right.
        for (int i = 0; i < notes.length; i++) {
            // Option 1: skip the current lecture.
            // If we skip it, the best total remains whatever was best up to the previous lecture.
            int skipCurrent = prevOne;

            // Option 2: attend the current lecture.
            // If we attend lecture i, we cannot attend lecture i - 1,
            // so we add notes[i] to the best answer up to i - 2.
            int takeCurrent = prevTwo + notes[i];

            // We are also allowed to choose no lectures at all.
            // This matters when values are negative.
            // Example: if notes = [-5], the best answer should be 0, not -5.
            //
            // Therefore, the best answer ending at this position is the maximum of:
            // - skipping the current lecture
            // - taking the current lecture
            // - 0 (skip everything)
            int currentBest = Math.max(0, Math.max(skipCurrent, takeCurrent));

            // Shift the rolling window forward:
            // - the old prevOne becomes the new prevTwo
            // - currentBest becomes the new prevOne
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // After processing all lectures, prevOne stores the best answer for the full array.
        return prevOne;
    }

    /**
     * Computes the maximum total note value using a full dynamic programming array.
     *
     * This version is included for educational purposes because it makes the state transition
     * very explicit and easy to trace step by step.
     *
     * Let dp[i] mean:
     * the maximum total note value we can collect from the subarray notes[0...i]
     * without attending adjacent lectures.
     *
     * Transition:
     * dp[i] = max(
     *     dp[i - 1],          // skip lecture i
     *     dp[i - 2] + notes[i], // attend lecture i
     *     0                   // choose nothing if all options are negative
     * )
     *
     * @param notes the array where notes[i] is the note value of the i-th lecture
     * @return the maximum total note value possible without choosing adjacent lectures;
     *         returns 0 if skipping all lectures is best
     * Time complexity: O(n), where n is the number of lectures
     * Space complexity: O(n), for the DP array
     */
    public int maxNotesWithDpArray(int[] notes) {
        if (notes == null || notes.length == 0) {
            return 0;
        }

        int n = notes.length;
        int[] dp = new int[n];

        // Base case for the first lecture:
        // Either attend it or skip everything.
        dp[0] = Math.max(0, notes[0]);

        // If there is only one lecture, we are done.
        if (n == 1) {
            return dp[0];
        }

        // Base case for the second lecture:
        // We cannot attend both lecture 0 and lecture 1 because they are adjacent.
        // So the best answer is the maximum of:
        // - best up to lecture 0
        // - attending lecture 1 alone
        // - skipping both (0)
        dp[1] = Math.max(dp[0], Math.max(0, notes[1]));

        // Fill the DP table from left to right.
        for (int i = 2; i < n; i++) {
            // If we skip lecture i, the answer stays dp[i - 1].
            int skipCurrent = dp[i - 1];

            // If we attend lecture i, we add its value to dp[i - 2].
            int takeCurrent = dp[i - 2] + notes[i];

            // Also compare with 0 in case all values seen so far are negative.
            dp[i] = Math.max(0, Math.max(skipCurrent, takeCurrent));
        }

        return dp[n - 1];
    }

    /**
     * Converts an integer array into a readable string representation.
     *
     * This helper method is used only for clean output in the demonstration.
     *
     * @param arr the integer array to convert
     * @return a string such as [1, 2, 3]
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), for the generated string
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs and a few additional edge cases.
     *
     * The printed outputs for the required examples are:
     * - [5, 1, 6, 2, 4] -> 15
     * - [4, -2, 3, 7] -> 11
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total number of elements across demonstrated arrays)
     * Space complexity: O(1) extra, excluding temporary output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] notes1 = {5, 1, 6, 2, 4};
        int[] notes2 = {4, -2, 3, 7};

        // Additional edge cases to show correctness and beginner-friendly behavior.
        int[] notes3 = {-5, -1, -9};   // Best is 0: skip all lectures
        int[] notes4 = {10};           // Best is 10
        int[] notes5 = {2, 1};         // Best is 2
        int[] notes6 = {2, 7, 9, 3, 1}; // Classic example: best is 12

        System.out.println("Sample Example 1:");
        System.out.println("Input:  notes = " + solution.arrayToString(notes1));
        System.out.println("Output: " + solution.maxNotes(notes1));
        System.out.println("Expected: 15");
        System.out.println();

        System.out.println("Sample Example 2:");
        System.out.println("Input:  notes = " + solution.arrayToString(notes2));
        System.out.println("Output: " + solution.maxNotes(notes2));
        System.out.println("Expected: 11");
        System.out.println();

        System.out.println("Additional Edge Case 1:");
        System.out.println("Input:  notes = " + solution.arrayToString(notes3));
        System.out.println("Output: " + solution.maxNotes(notes3));
        System.out.println("Expected: 0");
        System.out.println();

        System.out.println("Additional Edge Case 2:");
        System.out.println("Input:  notes = " + solution.arrayToString(notes4));
        System.out.println("Output: " + solution.maxNotes(notes4));
        System.out.println("Expected: 10");
        System.out.println();

        System.out.println("Additional Edge Case 3:");
        System.out.println("Input:  notes = " + solution.arrayToString(notes5));
        System.out.println("Output: " + solution.maxNotes(notes5));
        System.out.println("Expected: 2");
        System.out.println();

        System.out.println("Additional Edge Case 4:");
        System.out.println("Input:  notes = " + solution.arrayToString(notes6));
        System.out.println("Output: " + solution.maxNotes(notes6));
        System.out.println("Expected: 12");
        System.out.println();

        // Also demonstrate that both implementations agree.
        System.out.println("Verification using full DP array on Sample Example 1:");
        System.out.println("Output: " + solution.maxNotesWithDpArray(notes1));
        System.out.println("Expected: 15");
        System.out.println();

        System.out.println("Verification using full DP array on Sample Example 2:");
        System.out.println("Output: " + solution.maxNotesWithDpArray(notes2));
        System.out.println("Expected: 11");
    }
}