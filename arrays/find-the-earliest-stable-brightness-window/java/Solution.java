import java.util.*;

/*
Problem Title: Find the Earliest Stable Brightness Window

Problem Description:
You are given an array brightness where brightness[i] is the measured screen brightness on minute i,
and an integer k. A monitoring system wants to find the earliest contiguous window of exactly k minutes
where the brightness never changes too sharply.

A window of length k is called stable if the difference between the maximum and minimum values inside
that window is less than or equal to limit.

Return the starting index of the earliest stable window of length k. If no such window exists, return -1.

This problem models a simple quality check over time-series array data. Since the difficulty is easy,
you may assume the constraints are small enough for a straightforward solution, although cleaner
solutions are encouraged.

Constraints:
- 1 <= brightness.length <= 200
- 1 <= brightness[i] <= 10^4
- 1 <= k <= brightness.length
- 0 <= limit <= 10^4

Example 1:
Input: brightness = [7, 9, 8, 8, 10, 13], k = 3, limit = 2
Output: 0
Explanation:
The window [7, 9, 8] has max = 9 and min = 7, so the difference is 2, which is allowed.
Since this is the earliest valid window, return 0.

Example 2:
Input: brightness = [4, 10, 3, 12, 8], k = 2, limit = 1
Output: -1
Explanation:
Every window of length 2 has a max-min difference greater than 1, so there is no stable window.
*/

public class Solution {

    /**
     * Finds the earliest starting index of a contiguous window of exactly k elements
     * such that the difference between the maximum and minimum values in that window
     * is less than or equal to the given limit.
     *
     * This implementation uses a straightforward brute-force scan because the input size
     * is small enough under the given constraints.
     *
     * @param brightness the array of measured brightness values for each minute
     * @param k the exact size of the window to examine
     * @param limit the maximum allowed difference between the largest and smallest values in a window
     * @return the starting index of the earliest stable window; returns -1 if no such window exists
     *
     * Time complexity: O(n * k), where n is brightness.length
     * Space complexity: O(1), ignoring input storage
     */
    public int earliestStableWindow(int[] brightness, int k, int limit) {
        // We will examine every possible window of length k.
        // If the array length is n, then the valid starting indices are:
        // 0, 1, 2, ..., n - k
        //
        // For each starting index:
        //   1. Compute the minimum value in that window.
        //   2. Compute the maximum value in that window.
        //   3. Check whether max - min <= limit.
        //   4. The first window that satisfies the condition is the answer.
        //
        // Because we return immediately when we find the first valid window,
        // this guarantees the result is the earliest stable window.

        int n = brightness.length;

        // Loop over every possible starting index of a window of size k.
        for (int start = 0; start <= n - k; start++) {
            // Initialize min and max using the first element of the current window.
            int minValue = brightness[start];
            int maxValue = brightness[start];

            // Scan all elements in the current window:
            // indices start, start + 1, ..., start + k - 1
            for (int i = start; i < start + k; i++) {
                // Update the running minimum if we find a smaller value.
                if (brightness[i] < minValue) {
                    minValue = brightness[i];
                }

                // Update the running maximum if we find a larger value.
                if (brightness[i] > maxValue) {
                    maxValue = brightness[i];
                }
            }

            // After scanning the full window, check the stability condition.
            // A window is stable if the spread (max - min) is within the allowed limit.
            if (maxValue - minValue <= limit) {
                // This is the earliest valid window because we are scanning from left to right.
                return start;
            }
        }

        // If we finish checking all windows and none are stable, return -1.
        return -1;
    }

    /**
     * Helper method that checks whether a specific window is stable.
     * This method is not required for the main algorithm, but it is useful
     * for demonstration and beginner understanding.
     *
     * @param brightness the array of measured brightness values
     * @param start the starting index of the window
     * @param k the size of the window
     * @param limit the maximum allowed difference between max and min in the window
     * @return true if the window is stable; false otherwise
     *
     * Time complexity: O(k)
     * Space complexity: O(1)
     */
    public boolean isStableWindow(int[] brightness, int start, int k, int limit) {
        int minValue = brightness[start];
        int maxValue = brightness[start];

        // Examine each element in the chosen window and track min/max.
        for (int i = start; i < start + k; i++) {
            minValue = Math.min(minValue, brightness[i]);
            maxValue = Math.max(maxValue, brightness[i]);
        }

        return maxValue - minValue <= limit;
    }

    /**
     * Prints an integer array in a readable format.
     *
     * @param array the array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demo cases, excluding the algorithm calls
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] brightness1 = {7, 9, 8, 8, 10, 13};
        int k1 = 3;
        int limit1 = 2;
        int result1 = solution.earliestStableWindow(brightness1, k1, limit1);

        System.out.println("Example 1:");
        System.out.println("brightness = " + solution.arrayToString(brightness1));
        System.out.println("k = " + k1 + ", limit = " + limit1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 0");
        System.out.println();

        // Sample 2
        int[] brightness2 = {4, 10, 3, 12, 8};
        int k2 = 2;
        int limit2 = 1;
        int result2 = solution.earliestStableWindow(brightness2, k2, limit2);

        System.out.println("Example 2:");
        System.out.println("brightness = " + solution.arrayToString(brightness2));
        System.out.println("k = " + k2 + ", limit = " + limit2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: -1");
        System.out.println();

        // Additional demonstration
        int[] brightness3 = {5, 5, 5, 5};
        int k3 = 2;
        int limit3 = 0;
        int result3 = solution.earliestStableWindow(brightness3, k3, limit3);

        System.out.println("Additional Example:");
        System.out.println("brightness = " + solution.arrayToString(brightness3));
        System.out.println("k = " + k3 + ", limit = " + limit3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
    }
}