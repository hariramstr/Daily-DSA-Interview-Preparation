import java.util.*;

/*
 * Title: Count Late Arrivals After the Daily Cutoff
 * Difficulty: Easy
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an array arrivalTimes where each element represents the arrival time
 * of an employee in minutes after midnight for a single day. You are also given an
 * integer cutoff, which represents the latest on-time arrival in minutes after midnight.
 * Your task is to return how many employees arrived strictly after the cutoff time.
 *
 * For example, if cutoff = 540, that means employees arriving at 9:00 AM (540 minutes)
 * are still on time, but anyone arriving at 541 or later is late. The array is not
 * guaranteed to be sorted, and multiple employees may have the same arrival time.
 *
 * Write a function that scans the array and counts the number of values greater than cutoff.
 *
 * Constraints:
 * - 1 <= arrivalTimes.length <= 100000
 * - 0 <= arrivalTimes[i] <= 1439
 * - 0 <= cutoff <= 1439
 * - The answer fits in a 32-bit integer
 *
 * Example 1:
 * Input: arrivalTimes = [525, 541, 540, 600, 539], cutoff = 540
 * Output: 2
 * Explanation: The late arrivals are 541 and 600. Arrivals at 525, 540, and 539 are on time.
 *
 * Example 2:
 * Input: arrivalTimes = [480, 480, 481, 479], cutoff = 480
 * Output: 1
 * Explanation: Only the employee arriving at 481 is late. Employees arriving exactly at 480
 * are considered on time.
 */

public class Solution {

    /**
     * Counts how many employees arrived strictly after the given cutoff time.
     *
     * The key detail in this problem is the word "strictly":
     * - arrival time > cutoff  -> late
     * - arrival time == cutoff -> on time
     * - arrival time < cutoff  -> on time
     *
     * We solve this by scanning the array once and counting every value that is
     * greater than the cutoff.
     *
     * @param arrivalTimes an array where each value is an employee's arrival time in minutes after midnight
     * @param cutoff the latest on-time arrival in minutes after midnight
     * @return the number of employees whose arrival time is strictly greater than cutoff
     *
     * Time complexity: O(n), where n is the number of employees in arrivalTimes,
     * because we inspect each element exactly once.
     * Space complexity: O(1), because we use only a small fixed amount of extra memory.
     */
    public int countLateArrivals(int[] arrivalTimes, int cutoff) {
        // This variable will store the total number of late employees found so far.
        int lateCount = 0;

        // We go through every arrival time one by one.
        // Since the array is not guaranteed to be sorted, we cannot stop early
        // and we must inspect every element.
        for (int i = 0; i < arrivalTimes.length; i++) {
            // Read the current employee's arrival time.
            int currentArrival = arrivalTimes[i];

            // The problem says an employee is late only if they arrive strictly after cutoff.
            // That means:
            // - If currentArrival > cutoff, increment the answer.
            // - If currentArrival <= cutoff, do nothing.
            if (currentArrival > cutoff) {
                lateCount++;
            }
        }

        // After checking all employees, lateCount contains the final answer.
        return lateCount;
    }

    /**
     * A second beginner-friendly version using the enhanced for-loop.
     *
     * This method performs the exact same logic as countLateArrivals, but uses
     * Java's enhanced for-loop syntax, which can be easier to read for beginners.
     *
     * @param arrivalTimes an array where each value is an employee's arrival time in minutes after midnight
     * @param cutoff the latest on-time arrival in minutes after midnight
     * @return the number of employees whose arrival time is strictly greater than cutoff
     *
     * Time complexity: O(n), where n is the number of employees in arrivalTimes.
     * Space complexity: O(1), because no extra data structures are created.
     */
    public int countLateArrivalsEnhanced(int[] arrivalTimes, int cutoff) {
        // Start with zero late employees counted.
        int lateCount = 0;

        // Visit each arrival time directly.
        for (int arrival : arrivalTimes) {
            // Count only those arrivals that are strictly greater than the cutoff.
            if (arrival > cutoff) {
                lateCount++;
            }
        }

        // Return the total number of late arrivals.
        return lateCount;
    }

    /**
     * Prints an array in a simple readable format.
     *
     * This helper method is only used in main to make the demonstration output cleaner.
     *
     * @param arr the integer array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the length of the array.
     * Space complexity: O(n), due to the string created for display.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - the input array
     * - the cutoff
     * - the computed result
     * - the expected result
     *
     * Verified examples:
     * Example 1:
     * arrivalTimes = [525, 541, 540, 600, 539], cutoff = 540
     * Late values are 541 and 600 -> answer = 2
     *
     * Example 2:
     * arrivalTimes = [480, 480, 481, 479], cutoff = 480
     * Late value is 481 only -> answer = 1
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(n) for each demonstrated test case due to scanning the arrays.
     * Space complexity: O(1) extra algorithmic space, excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement
        int[] arrivalTimes1 = {525, 541, 540, 600, 539};
        int cutoff1 = 540;
        int result1 = solution.countLateArrivals(arrivalTimes1, cutoff1);

        System.out.println("Example 1");
        System.out.println("arrivalTimes = " + solution.arrayToString(arrivalTimes1));
        System.out.println("cutoff = " + cutoff1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 2");
        System.out.println();

        // Example 2 from the problem statement
        int[] arrivalTimes2 = {480, 480, 481, 479};
        int cutoff2 = 480;
        int result2 = solution.countLateArrivals(arrivalTimes2, cutoff2);

        System.out.println("Example 2");
        System.out.println("arrivalTimes = " + solution.arrayToString(arrivalTimes2));
        System.out.println("cutoff = " + cutoff2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 1");
        System.out.println();

        // Additional quick checks for edge behavior

        // Everyone is on time because all arrivals are equal to or below cutoff.
        int[] arrivalTimes3 = {540, 540, 539, 0};
        int cutoff3 = 540;
        int result3 = solution.countLateArrivals(arrivalTimes3, cutoff3);

        System.out.println("Additional Check 1");
        System.out.println("arrivalTimes = " + solution.arrayToString(arrivalTimes3));
        System.out.println("cutoff = " + cutoff3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 0");
        System.out.println();

        // Everyone is late because all arrivals are strictly greater than cutoff.
        int[] arrivalTimes4 = {1, 2, 3, 4};
        int cutoff4 = 0;
        int result4 = solution.countLateArrivalsEnhanced(arrivalTimes4, cutoff4);

        System.out.println("Additional Check 2");
        System.out.println("arrivalTimes = " + solution.arrayToString(arrivalTimes4));
        System.out.println("cutoff = " + cutoff4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 4");
    }
}