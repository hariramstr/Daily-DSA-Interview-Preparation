/*
 * Title: Maximum Points from Skipping Adjacent Study Modules
 * Difficulty: Easy
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are preparing for a certification exam. The course platform shows a list of study modules,
 * and each module gives a certain number of points if you complete it. However, completing two
 * adjacent modules on the same day causes too much fatigue, so you are not allowed to complete
 * both module i and module i + 1.
 *
 * Given an integer array points where points[i] is the score earned by completing the i-th module,
 * return the maximum total points you can earn under this rule.
 *
 * You may choose to skip any module. If the array is empty, the answer is 0.
 *
 * This is a classic dynamic programming decision process: for each module, you either skip it and
 * keep the best score so far, or complete it and add its points to the best score from two modules
 * earlier. Your task is to compute the best possible total.
 *
 * Constraints:
 * - 0 <= points.length <= 100
 * - 0 <= points[i] <= 1000
 *
 * Example 1:
 * Input: points = [4, 2, 7, 3, 9]
 * Output: 20
 * Explanation: Complete modules with scores 4, 7, and 9. These are not adjacent, and the total is 20.
 *
 * Example 2:
 * Input: points = [5, 1, 1, 5]
 * Output: 10
 * Explanation: Complete the first and last modules. The maximum total is 10.
 */

import java.util.*;

public class Solution {

    /**
     * Computes the maximum total points that can be earned without taking two adjacent modules.
     *
     * This method uses dynamic programming with an array:
     * - dp[i] represents the maximum points obtainable from the first i modules.
     * - For each module, we decide:
     *   1) Skip it -> keep the previous best
     *   2) Take it -> add its points to the best result from two modules earlier
     *
     * Transition:
     * dp[i] = max(dp[i - 1], dp[i - 2] + points[i - 1])
     *
     * @param points an array where points[i] is the score of the i-th study module
     * @return the maximum total points that can be earned without completing adjacent modules
     * Time complexity: O(n), where n is the number of modules
     * Space complexity: O(n), due to the dynamic programming array
     */
    public int maxPoints(int[] points) {
        // If the input array is null or empty, there are no modules to complete,
        // so the maximum score is simply 0.
        if (points == null || points.length == 0) {
            return 0;
        }

        // Let n be the number of modules.
        int n = points.length;

        // dp[i] will store the maximum points we can earn considering only
        // the first i modules.
        //
        // Important indexing detail:
        // - points uses 0-based indexing: points[0], points[1], ...
        // - dp uses a shifted meaning:
        //   dp[0] = best score using 0 modules
        //   dp[1] = best score using first 1 module
        //   ...
        //   dp[n] = best score using first n modules
        int[] dp = new int[n + 1];

        // Base case:
        // If we consider 0 modules, the best score is 0.
        dp[0] = 0;

        // Base case for the first module:
        // If there is only one module available, the best we can do is either
        // take it or skip it. Since points are non-negative, taking it is optimal.
        dp[1] = points[0];

        // Process modules from the second one onward.
        for (int i = 2; i <= n; i++) {
            // Option 1: Skip the current module.
            // Then the best score remains whatever we had from the first i - 1 modules.
            int skipCurrent = dp[i - 1];

            // Option 2: Take the current module.
            // If we take module (i - 1) in the points array, we cannot take module (i - 2),
            // so we add its points to the best score from the first i - 2 modules.
            int takeCurrent = dp[i - 2] + points[i - 1];

            // Choose the better of the two options.
            dp[i] = Math.max(skipCurrent, takeCurrent);
        }

        // The answer for all n modules is stored in dp[n].
        return dp[n];
    }

    /**
     * Computes the maximum total points that can be earned without taking two adjacent modules.
     *
     * This is a space-optimized version of the dynamic programming solution.
     * Instead of storing the entire dp array, it keeps only the last two states:
     * - prevTwo = dp[i - 2]
     * - prevOne = dp[i - 1]
     *
     * Transition:
     * current = max(prevOne, prevTwo + points[i])
     *
     * @param points an array where points[i] is the score of the i-th study module
     * @return the maximum total points that can be earned without completing adjacent modules
     * Time complexity: O(n), where n is the number of modules
     * Space complexity: O(1), because only a constant amount of extra memory is used
     */
    public int maxPointsOptimized(int[] points) {
        // If there are no modules, there are no points to earn.
        if (points == null || points.length == 0) {
            return 0;
        }

        // prevTwo represents the best score up to two modules before the current one.
        int prevTwo = 0;

        // prevOne represents the best score up to the previous module.
        int prevOne = 0;

        // Iterate through each module score one by one.
        for (int point : points) {
            // If we skip the current module, our total remains prevOne.
            int skipCurrent = prevOne;

            // If we take the current module, we add its score to prevTwo.
            int takeCurrent = prevTwo + point;

            // The best result at this step is the better of skipping or taking.
            int current = Math.max(skipCurrent, takeCurrent);

            // Move the window forward:
            // - old prevOne becomes new prevTwo
            // - current becomes new prevOne
            prevTwo = prevOne;
            prevOne = current;
        }

        // After processing all modules, prevOne holds the final answer.
        return prevOne;
    }

    /**
     * Converts an integer array to a readable string format for printing.
     *
     * @param arr the input integer array
     * @return a string representation of the array
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), for the generated string content
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstration calls, excluding the cost of each algorithm run
     * Space complexity: O(1), excluding the space used inside called methods
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1 from the problem statement.
        int[] points1 = {4, 2, 7, 3, 9};
        int result1 = solution.maxPoints(points1);
        System.out.println("Input: points = " + solution.arrayToString(points1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 20");
        System.out.println();

        // Sample input 2 from the problem statement.
        int[] points2 = {5, 1, 1, 5};
        int result2 = solution.maxPoints(points2);
        System.out.println("Input: points = " + solution.arrayToString(points2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 10");
        System.out.println();

        // Edge case: empty array.
        int[] points3 = {};
        int result3 = solution.maxPoints(points3);
        System.out.println("Input: points = " + solution.arrayToString(points3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        // Additional test: single module.
        int[] points4 = {8};
        int result4 = solution.maxPoints(points4);
        System.out.println("Input: points = " + solution.arrayToString(points4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 8");
        System.out.println();

        // Additional test using the optimized version to show it matches.
        int optimized1 = solution.maxPointsOptimized(points1);
        int optimized2 = solution.maxPointsOptimized(points2);
        System.out.println("Optimized Output for " + solution.arrayToString(points1) + ": " + optimized1);
        System.out.println("Optimized Output for " + solution.arrayToString(points2) + ": " + optimized2);
    }
}