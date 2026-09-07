/*
Problem Title: Find the First Local Peak Day

Problem Description:
You are given an integer array visitors where visitors[i] represents the number of visitors
to a store on day i. A day is called a local peak if its visitor count is strictly greater
than the visitor count of the previous day and strictly greater than the visitor count of
the next day. In other words, for some index i, day i is a local peak when:

    visitors[i] > visitors[i - 1] and visitors[i] > visitors[i + 1]

Your task is to return the index of the first local peak day in the array. If no such day
exists, return -1.

Only days with both a previous and next day can be local peaks, so the first and last
elements can never be considered peaks.

This problem tests careful array traversal and boundary handling. A simple linear scan is expected.

Constraints:
- 3 <= visitors.length <= 10^5
- 0 <= visitors[i] <= 10^6

Example 1:
Input: visitors = [12, 18, 15, 20, 19]
Output: 1
Explanation: Day 1 has 18 visitors, which is greater than 12 and 15.
Although day 3 is also a local peak, the first one appears at index 1.

Example 2:
Input: visitors = [5, 7, 7, 6, 4]
Output: -1
Explanation: Day 1 is not a peak because 7 is not strictly greater than the next value 7.
No index satisfies the local peak condition.
*/

import java.util.*;

public class Solution {

    /**
     * Finds the index of the first local peak in the given visitors array.
     *
     * A local peak is an index i such that:
     * - visitors[i] > visitors[i - 1]
     * - visitors[i] > visitors[i + 1]
     *
     * Since the first and last positions do not have two neighbors, they are never checked.
     *
     * @param visitors the array where visitors[i] is the number of visitors on day i
     * @return the index of the first local peak day; returns -1 if no local peak exists
     * Time complexity: O(n), because we scan the array once from left to right
     * Space complexity: O(1), because we use only a constant amount of extra space
     */
    public int findFirstLocalPeak(int[] visitors) {
        // We start from index 1 because index 0 cannot be a local peak:
        // it does not have a previous element.
        //
        // We stop at visitors.length - 2 because the last index also cannot be a local peak:
        // it does not have a next element.
        for (int i = 1; i < visitors.length - 1; i++) {

            // Store the neighboring values in clearly named variables.
            // This makes the logic easier to read and understand.
            int previousDayVisitors = visitors[i - 1];
            int currentDayVisitors = visitors[i];
            int nextDayVisitors = visitors[i + 1];

            // A local peak must be STRICTLY greater than both neighbors.
            // That means:
            // current > previous
            // current > next
            //
            // If both conditions are true, then we have found the first local peak,
            // because we are scanning from left to right.
            if (currentDayVisitors > previousDayVisitors && currentDayVisitors > nextDayVisitors) {
                return i;
            }
        }

        // If we finish the loop without returning, then no local peak exists.
        return -1;
    }

    /**
     * Converts an integer array to a readable string representation.
     * This helper method is used only for demonstration output in main.
     *
     * @param array the integer array to convert to a string
     * @return a string representation of the array, such as [1, 2, 3]
     * Time complexity: O(n), because each element is processed once
     * Space complexity: O(n), because the resulting string stores all elements
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Runs a single demonstration test case:
     * prints the input array and the computed result.
     *
     * @param visitors the input visitors array to test
     * @return the computed index of the first local peak
     * Time complexity: O(n), dominated by the local peak search
     * Space complexity: O(1), excluding output formatting
     */
    public int runDemo(int[] visitors) {
        int result = findFirstLocalPeak(visitors);
        System.out.println("visitors = " + arrayToString(visitors));
        System.out.println("First local peak index = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution using sample inputs from the problem statement
     * and a few additional beginner-friendly test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per test case
     * Space complexity: O(1), excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Input 1:
        // visitors = [12, 18, 15, 20, 19]
        //
        // Step-by-step verification:
        // i = 1 -> 18 > 12 and 18 > 15 -> true
        // So the first local peak is at index 1.
        int[] sample1 = {12, 18, 15, 20, 19};
        solution.runDemo(sample1);

        // Sample Input 2:
        // visitors = [5, 7, 7, 6, 4]
        //
        // Step-by-step verification:
        // i = 1 -> 7 > 5 is true, but 7 > 7 is false
        // i = 2 -> 7 > 7 is false
        // i = 3 -> 6 > 7 is false
        // No local peak exists, so answer is -1.
        int[] sample2 = {5, 7, 7, 6, 4};
        solution.runDemo(sample2);

        // Additional test:
        // Peak in the middle.
        int[] test3 = {3, 9, 4};
        solution.runDemo(test3);

        // Additional test:
        // No peak because values keep increasing.
        int[] test4 = {1, 2, 3, 4, 5};
        solution.runDemo(test4);

        // Additional test:
        // Multiple peaks exist, but we must return the first one.
        // Peaks are at index 1 (8) and index 3 (7), so answer should be 1.
        int[] test5 = {2, 8, 3, 7, 1};
        solution.runDemo(test5);
    }
}