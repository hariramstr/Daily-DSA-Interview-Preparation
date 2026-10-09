/*
Problem Title: Maximum Study Points Without Consecutive Hard Chapters

Problem Description:
You are preparing for an exam and have a list of chapters to study in order. Each chapter gives you
a certain number of study points if you choose to review it. However, reviewing two adjacent chapters
in the same session is too tiring because both require full concentration. To keep your session manageable,
you may not choose two consecutive chapters.

Given an integer array points where points[i] is the number of study points earned by reviewing chapter i,
return the maximum total study points you can earn.

You may choose to skip any chapter, and you do not need to review the last chapter. This is an optimization
problem where each decision depends on previous choices, making it a good fit for dynamic programming.

Constraints:
- 1 <= points.length <= 100
- 0 <= points[i] <= 1000

Example 1:
Input: points = [3, 2, 5, 10, 7]
Output: 15
Explanation: Review chapters with points 3, 5, and 7. Their indices are not adjacent, so the total is 15.

Example 2:
Input: points = [2, 1, 4, 9]
Output: 11
Explanation: The best choice is to review chapters with points 2 and 9 for a total of 11. Choosing 1 and 9 gives 10,
and choosing 2 and 4 gives 6.

Task:
Compute the maximum achievable total efficiently.
*/

import java.util.*;

public class Solution {

    /**
     * Computes the maximum total study points that can be earned without choosing
     * two consecutive chapters.
     *
     * This method uses dynamic programming:
     * - For each chapter, we decide whether to:
     *   1) skip it, keeping the best result from previous chapters, or
     *   2) take it, which means we must skip the immediately previous chapter.
     *
     * @param points an array where points[i] is the study points earned by reviewing chapter i
     * @return the maximum total study points achievable without selecting adjacent chapters
     *
     * Time Complexity: O(n), where n is the number of chapters
     * Space Complexity: O(n), due to the DP array
     */
    public int maxStudyPoints(int[] points) {
        // Defensive check:
        // Although the constraints guarantee at least one element,
        // handling empty input makes the method safer and more reusable.
        if (points == null || points.length == 0) {
            return 0;
        }

        int n = points.length;

        // If there is only one chapter, the best we can do is either take it or skip it.
        // Since points are non-negative, taking it is always at least as good as skipping it.
        if (n == 1) {
            return points[0];
        }

        // dp[i] will store the maximum study points we can earn
        // considering chapters from index 0 to index i.
        int[] dp = new int[n];

        // Base case for the first chapter:
        // With only chapter 0 available, the best answer is simply points[0].
        dp[0] = points[0];

        // Base case for the second chapter:
        // We cannot take both chapter 0 and chapter 1 because they are adjacent.
        // So the best answer is the larger of:
        // - taking chapter 0
        // - taking chapter 1
        dp[1] = Math.max(points[0], points[1]);

        // Fill the DP table from left to right.
        for (int i = 2; i < n; i++) {
            // Option 1: Skip the current chapter.
            // Then our total remains the best total up to chapter i - 1.
            int skipCurrent = dp[i - 1];

            // Option 2: Take the current chapter.
            // If we take chapter i, we cannot take chapter i - 1,
            // so we add points[i] to the best total up to chapter i - 2.
            int takeCurrent = dp[i - 2] + points[i];

            // The best result at position i is the better of these two choices.
            dp[i] = Math.max(skipCurrent, takeCurrent);
        }

        // The last DP entry contains the answer for the full array.
        return dp[n - 1];
    }

    /**
     * Computes the maximum total study points that can be earned without choosing
     * two consecutive chapters, using space optimization.
     *
     * Instead of storing the entire DP array, this method keeps only the last two
     * necessary values:
     * - prevTwo = best answer up to i - 2
     * - prevOne = best answer up to i - 1
     *
     * @param points an array where points[i] is the study points earned by reviewing chapter i
     * @return the maximum total study points achievable without selecting adjacent chapters
     *
     * Time Complexity: O(n), where n is the number of chapters
     * Space Complexity: O(1), ignoring input storage
     */
    public int maxStudyPointsOptimized(int[] points) {
        // Handle invalid or empty input safely.
        if (points == null || points.length == 0) {
            return 0;
        }

        int n = points.length;

        // If there is only one chapter, return its value.
        if (n == 1) {
            return points[0];
        }

        // prevTwo represents dp[i - 2]
        int prevTwo = points[0];

        // prevOne represents dp[i - 1]
        int prevOne = Math.max(points[0], points[1]);

        // Process chapters starting from index 2.
        for (int i = 2; i < n; i++) {
            // If we skip the current chapter, total remains prevOne.
            int skipCurrent = prevOne;

            // If we take the current chapter, we add its points to prevTwo.
            int takeCurrent = prevTwo + points[i];

            // Current best answer for this position.
            int current = Math.max(skipCurrent, takeCurrent);

            // Shift the window forward:
            // old prevOne becomes new prevTwo,
            // current becomes new prevOne.
            prevTwo = prevOne;
            prevOne = current;
        }

        return prevOne;
    }

    /**
     * Converts an integer array to a readable string for display.
     *
     * @param arr the integer array to convert
     * @return a string representation of the array
     *
     * Time Complexity: O(n), where n is the array length
     * Space Complexity: O(n), due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * It also verifies that the outputs match the expected answers:
     * - [3, 2, 5, 10, 7] -> 15
     * - [2, 1, 4, 9] -> 11
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time Complexity: O(1) for the fixed demo inputs
     * Space Complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] points1 = {3, 2, 5, 10, 7};
        int[] points2 = {2, 1, 4, 9};

        int result1 = solution.maxStudyPoints(points1);
        int result2 = solution.maxStudyPoints(points2);

        int optimizedResult1 = solution.maxStudyPointsOptimized(points1);
        int optimizedResult2 = solution.maxStudyPointsOptimized(points2);

        System.out.println("Sample Input 1: " + solution.arrayToString(points1));
        System.out.println("Maximum Study Points (DP array): " + result1);
        System.out.println("Maximum Study Points (Optimized): " + optimizedResult1);
        System.out.println("Expected: 15");
        System.out.println();

        System.out.println("Sample Input 2: " + solution.arrayToString(points2));
        System.out.println("Maximum Study Points (DP array): " + result2);
        System.out.println("Maximum Study Points (Optimized): " + optimizedResult2);
        System.out.println("Expected: 11");
        System.out.println();

        // Additional small demonstrations for beginner clarity.
        int[] points3 = {5};
        int[] points4 = {4, 8};
        int[] points5 = {0, 0, 0, 0};

        System.out.println("Additional Input 3: " + solution.arrayToString(points3));
        System.out.println("Maximum Study Points: " + solution.maxStudyPoints(points3));
        System.out.println();

        System.out.println("Additional Input 4: " + solution.arrayToString(points4));
        System.out.println("Maximum Study Points: " + solution.maxStudyPoints(points4));
        System.out.println();

        System.out.println("Additional Input 5: " + solution.arrayToString(points5));
        System.out.println("Maximum Study Points: " + solution.maxStudyPoints(points5));
    }
}