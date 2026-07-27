import java.util.*;

/*
 * Title: Maximum Loyalty Points from Skipping Consecutive Cafes
 * Difficulty: Easy
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A commuter passes a row of cafes on the way to work. Each cafe offers a certain
 * number of loyalty points if visited that day. However, visiting two neighboring
 * cafes on the same trip takes too much time, so the commuter is not allowed to
 * collect points from two consecutive cafes.
 *
 * You are given an integer array points where points[i] is the number of loyalty
 * points available at the i-th cafe. Return the maximum total number of points the
 * commuter can collect while following the rule that no two chosen cafes are adjacent.
 *
 * This is a classic decision problem with a simple dynamic programming pattern:
 * at each cafe, either skip it and keep the best total so far, or visit it and add
 * its points to the best total from two cafes earlier.
 *
 * Constraints:
 * - 1 <= points.length <= 100
 * - 0 <= points[i] <= 1000
 *
 * Example 1:
 * Input: points = [5, 1, 2, 10]
 * Output: 15
 * Explanation: Visit cafe 0 and cafe 3 for a total of 5 + 10 = 15.
 *
 * Example 2:
 * Input: points = [2, 7, 9, 3, 1]
 * Output: 12
 * Explanation: The best choice is to visit cafes 0, 2, and 4.
 */

public class Solution {

    /**
     * Computes the maximum loyalty points that can be collected without choosing
     * two adjacent cafes.
     *
     * This method uses dynamic programming:
     * - dp[i] represents the maximum points that can be collected from the first
     *   i + 1 cafes (from index 0 to index i).
     * - For each cafe i, we have two choices:
     *   1. Skip the current cafe:
     *      Then the best total remains dp[i - 1].
     *   2. Visit the current cafe:
     *      Then we must skip the previous cafe, so the total becomes
     *      points[i] + dp[i - 2].
     * - We take the maximum of these two choices.
     *
     * @param points an array where points[i] is the loyalty points offered by the i-th cafe
     * @return the maximum total loyalty points that can be collected without visiting adjacent cafes
     * Time complexity: O(n), where n is the number of cafes
     * Space complexity: O(n), due to the dynamic programming array
     */
    public int maxLoyaltyPoints(int[] points) {
        // Defensive check:
        // Although the problem guarantees at least one element,
        // this makes the method safer and easier for beginners to reuse.
        if (points == null || points.length == 0) {
            return 0;
        }

        // If there is only one cafe, the best we can do is simply visit it.
        if (points.length == 1) {
            return points[0];
        }

        // Create a DP array where:
        // dp[i] = maximum points we can collect considering cafes from 0 to i.
        int[] dp = new int[points.length];

        // Base case for the first cafe:
        // With only cafe 0 available, the best choice is to take its points.
        dp[0] = points[0];

        // Base case for the second cafe:
        // We cannot take both cafe 0 and cafe 1 because they are adjacent.
        // So we choose the better of:
        // - visiting cafe 0
        // - visiting cafe 1
        dp[1] = Math.max(points[0], points[1]);

        // Process each remaining cafe one by one.
        for (int i = 2; i < points.length; i++) {
            // Option 1: Skip the current cafe.
            // Then our best total is exactly the same as the best total up to i - 1.
            int skipCurrent = dp[i - 1];

            // Option 2: Visit the current cafe.
            // If we visit cafe i, we cannot visit cafe i - 1.
            // Therefore, we add points[i] to the best total up to i - 2.
            int takeCurrent = dp[i - 2] + points[i];

            // Choose the better of the two options.
            dp[i] = Math.max(skipCurrent, takeCurrent);
        }

        // The last entry contains the answer for the full array.
        return dp[points.length - 1];
    }

    /**
     * Computes the maximum loyalty points that can be collected without choosing
     * two adjacent cafes, using an optimized dynamic programming approach.
     *
     * Instead of storing all DP states, this method keeps only the last two
     * necessary values:
     * - prevTwo = best answer up to i - 2
     * - prevOne = best answer up to i - 1
     *
     * For each cafe, we compute:
     * - skipCurrent = prevOne
     * - takeCurrent = prevTwo + points[i]
     * - current = max(skipCurrent, takeCurrent)
     *
     * @param points an array where points[i] is the loyalty points offered by the i-th cafe
     * @return the maximum total loyalty points that can be collected without visiting adjacent cafes
     * Time complexity: O(n), where n is the number of cafes
     * Space complexity: O(1), because only a constant amount of extra space is used
     */
    public int maxLoyaltyPointsOptimized(int[] points) {
        // Handle edge cases first.
        if (points == null || points.length == 0) {
            return 0;
        }

        if (points.length == 1) {
            return points[0];
        }

        // prevTwo represents dp[i - 2]
        int prevTwo = points[0];

        // prevOne represents dp[i - 1]
        int prevOne = Math.max(points[0], points[1]);

        // Iterate through the rest of the cafes.
        for (int i = 2; i < points.length; i++) {
            // If we skip the current cafe, the best total stays prevOne.
            int skipCurrent = prevOne;

            // If we take the current cafe, we add its points to prevTwo.
            int takeCurrent = prevTwo + points[i];

            // The best answer at this step is the better of the two choices.
            int current = Math.max(skipCurrent, takeCurrent);

            // Move the window forward:
            // old prevOne becomes new prevTwo,
            // current becomes new prevOne.
            prevTwo = prevOne;
            prevOne = current;
        }

        // prevOne now stores the best answer for the full array.
        return prevOne;
    }

    /**
     * Helper method to print an integer array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(n), due to the string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * Verified examples:
     * - [5, 1, 2, 10] -> 15
     * - [2, 7, 9, 3, 1] -> 12
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo cases shown here
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] points1 = {5, 1, 2, 10};
        int[] points2 = {2, 7, 9, 3, 1};

        int result1 = solution.maxLoyaltyPoints(points1);
        int result2 = solution.maxLoyaltyPoints(points2);

        int optimizedResult1 = solution.maxLoyaltyPointsOptimized(points1);
        int optimizedResult2 = solution.maxLoyaltyPointsOptimized(points2);

        System.out.println("Example 1:");
        System.out.println("Input: points = " + solution.arrayToString(points1));
        System.out.println("Output (DP array method): " + result1);
        System.out.println("Output (optimized method): " + optimizedResult1);
        System.out.println("Expected: 15");
        System.out.println();

        System.out.println("Example 2:");
        System.out.println("Input: points = " + solution.arrayToString(points2));
        System.out.println("Output (DP array method): " + result2);
        System.out.println("Output (optimized method): " + optimizedResult2);
        System.out.println("Expected: 12");
        System.out.println();

        int[] extraExample = {4, 10, 3, 1, 5};
        int extraResult = solution.maxLoyaltyPoints(extraExample);
        int extraOptimizedResult = solution.maxLoyaltyPointsOptimized(extraExample);

        System.out.println("Extra Example:");
        System.out.println("Input: points = " + solution.arrayToString(extraExample));
        System.out.println("Output (DP array method): " + extraResult);
        System.out.println("Output (optimized method): " + extraOptimizedResult);
    }
}