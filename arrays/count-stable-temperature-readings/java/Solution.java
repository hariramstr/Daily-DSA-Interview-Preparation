import java.util.*;

/*
 * Title: Count Stable Temperature Readings
 * Difficulty: Easy
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array readings where readings[i] represents the temperature
 * recorded on day i. A reading is called stable if its value is equal to the average
 * of its immediate neighbors. In other words, for an index i to be stable, it must
 * satisfy 1 <= i <= n - 2 and readings[i] * 2 == readings[i - 1] + readings[i + 1].
 *
 * Your task is to return the number of stable readings in the array.
 *
 * This problem models a simple data-quality check used in monitoring systems.
 * A stable reading is not necessarily the same as its neighbors, but it must lie
 * exactly halfway between the previous and next values. Since only immediate neighbors
 * matter, each valid index can be checked independently.
 *
 * Write a function that takes the array and returns how many indices are stable.
 * If the array has fewer than 3 elements, the answer is 0 because no element has
 * two neighbors.
 *
 * Constraints:
 * - 1 <= readings.length <= 100000
 * - -100000 <= readings[i] <= 100000
 * - The expected solution should run in O(n) time
 * - Use O(1) extra space aside from the input
 *
 * Example 1:
 * Input: readings = [4, 6, 8, 7, 6]
 * Output: 2
 * Explanation:
 * - Index 1 is stable because 6 is the average of 4 and 8.
 * - Index 2 is not stable because 8 is not the average of 6 and 7.
 * - Index 3 is stable because 7 is the average of 8 and 6.
 * So the total number of stable readings is 2.
 *
 * Example 2:
 * Input: readings = [5, 5, 5, 5]
 * Output: 2
 * Explanation:
 * - Index 1 is stable because 5 is the average of 5 and 5.
 * - Index 2 is also stable for the same reason.
 * There are 2 stable readings in total.
 */

public class Solution {

    /**
     * Counts how many indices in the given readings array are stable.
     *
     * A reading at index i is stable if:
     * 1 <= i <= readings.length - 2
     * and
     * readings[i] * 2 == readings[i - 1] + readings[i + 1]
     *
     * @param readings the array of temperature readings, where readings[i] is the temperature on day i
     * @return the number of stable readings in the array
     *
     * Time Complexity: O(n), because we scan the array once.
     * Space Complexity: O(1), because we use only a few extra variables.
     */
    public int countStableReadings(int[] readings) {
        // If the array is null, there is no valid data to process.
        // Returning 0 is a safe and beginner-friendly choice.
        if (readings == null) {
            return 0;
        }

        // A stable reading must have both a left neighbor and a right neighbor.
        // Therefore, arrays of length 0, 1, or 2 cannot contain any stable index.
        if (readings.length < 3) {
            return 0;
        }

        // This variable will store the total number of stable readings found.
        int stableCount = 0;

        // We start from index 1 because index 0 has no left neighbor.
        // We stop at index readings.length - 2 because the last index has no right neighbor.
        for (int i = 1; i < readings.length - 1; i++) {
            // Read the three relevant values:
            // - left neighbor
            // - current value
            // - right neighbor
            int left = readings[i - 1];
            int current = readings[i];
            int right = readings[i + 1];

            // A reading is stable if the current value is exactly the average
            // of its immediate neighbors.
            //
            // Instead of writing:
            // current == (left + right) / 2
            //
            // we write:
            // current * 2 == left + right
            //
            // This is better because it avoids issues with integer division.
            // For example, if left + right is odd, dividing by 2 would lose precision.
            if (current * 2 == left + right) {
                // We found one stable reading, so increase the count.
                stableCount++;
            }
        }

        // After checking every valid middle index, return the total count.
        return stableCount;
    }

    /**
     * Prints an integer array in a readable format.
     *
     * This helper method is used only for demonstration in main.
     *
     * @param readings the array to print
     * @return a string representation of the array
     *
     * Time Complexity: O(n), because every element may be visited once while building the string.
     * Space Complexity: O(n), because the produced string stores the array contents.
     */
    public String arrayToString(int[] readings) {
        return Arrays.toString(readings);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time Complexity: O(n) per demonstrated test case due to counting and printing.
     * Space Complexity: O(1) extra for the algorithm itself, excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Input 1 from the problem statement
        int[] readings1 = {4, 6, 8, 7, 6};
        int result1 = solution.countStableReadings(readings1);
        System.out.println("Input: " + solution.arrayToString(readings1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 2");
        System.out.println();

        // Manual trace for Example 1:
        // Index 1: 6 * 2 = 12, and 4 + 8 = 12 -> stable
        // Index 2: 8 * 2 = 16, and 6 + 7 = 13 -> not stable
        // Index 3: 7 * 2 = 14, and 8 + 6 = 14 -> stable
        // Total = 2
        //
        // Therefore, the algorithm correctly returns 2.

        // Sample Input 2 from the problem statement
        int[] readings2 = {5, 5, 5, 5};
        int result2 = solution.countStableReadings(readings2);
        System.out.println("Input: " + solution.arrayToString(readings2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 2");
        System.out.println();

        // Manual trace for Example 2:
        // Index 1: 5 * 2 = 10, and 5 + 5 = 10 -> stable
        // Index 2: 5 * 2 = 10, and 5 + 5 = 10 -> stable
        // Total = 2
        //
        // Therefore, the algorithm correctly returns 2.

        // Additional test case: too short to have a stable reading
        int[] readings3 = {10, 20};
        int result3 = solution.countStableReadings(readings3);
        System.out.println("Input: " + solution.arrayToString(readings3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        // Additional test case: negative values
        int[] readings4 = {-4, -2, 0, 2, 4};
        int result4 = solution.countStableReadings(readings4);
        System.out.println("Input: " + solution.arrayToString(readings4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 3");
        System.out.println();

        // Trace for readings4:
        // Index 1: -2 * 2 = -4, and -4 + 0 = -4 -> stable
        // Index 2:  0 * 2 =  0, and -2 + 2 =  0 -> stable
        // Index 3:  2 * 2 =  4, and  0 + 4 =  4 -> stable
        // Total = 3

        // Additional test case: no stable readings
        int[] readings5 = {1, 3, 2, 5, 9};
        int result5 = solution.countStableReadings(readings5);
        System.out.println("Input: " + solution.arrayToString(readings5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 0");
    }
}