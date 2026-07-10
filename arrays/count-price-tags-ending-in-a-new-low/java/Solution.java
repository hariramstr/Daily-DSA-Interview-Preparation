/*
 * Title: Count Price Tags Ending in a New Low
 * Difficulty: Easy
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array prices where prices[i] is the price tag printed
 * for the i-th item in the order items were labeled during a day.
 *
 * A price tag is considered to end in a new low if its value is strictly smaller
 * than every price that appeared before it in the array.
 *
 * The first item always counts, because there are no earlier prices to compare against.
 *
 * Return the number of items whose price tag ends in a new low.
 *
 * This problem is a simple array scan: as you move from left to right, keep track
 * of the smallest price seen so far. Whenever the current price is smaller than
 * that running minimum, it creates a new low and should be counted.
 *
 * Constraints:
 * - 1 <= prices.length <= 100000
 * - -1000000000 <= prices[i] <= 1000000000
 * - prices may contain duplicates
 *
 * Important notes:
 * - A value equal to the current minimum does not count as a new low.
 * - The first element always contributes 1 to the answer.
 *
 * Example 1:
 * Input: prices = [12, 10, 11, 9, 9, 7]
 * Output: 4
 * Explanation:
 * New lows occur at 12, 10, 9, and 7.
 * The second 9 does not count because it is not strictly smaller than the previous minimum.
 *
 * Example 2:
 * Input: prices = [5, 5, 5, 4, 6, 3]
 * Output: 3
 * Explanation:
 * New lows occur at 5, 4, and 3.
 */

import java.util.*;

public class Solution {

    /**
     * Counts how many price tags are "new lows" while scanning from left to right.
     *
     * A price is counted if:
     * 1) It is the first element, or
     * 2) It is strictly smaller than every price that appeared before it.
     *
     * The method maintains a running minimum seen so far and increments the answer
     * whenever a strictly smaller value is found.
     *
     * @param prices the array of printed price tags in the order they appeared
     * @return the number of positions whose value is a new strict minimum
     *
     * Time complexity: O(n), where n is the length of the array, because we scan the array once.
     * Space complexity: O(1), because we use only a few extra variables.
     */
    public int countNewLows(int[] prices) {
        // According to the constraints, the array length is at least 1.
        // Still, handling null or empty input makes the method safer and beginner-friendly.
        if (prices == null || prices.length == 0) {
            return 0;
        }

        // The first price always counts as a new low because there are no earlier prices.
        int count = 1;

        // Initialize the running minimum with the first element.
        // This variable always stores the smallest value seen so far.
        int minSoFar = prices[0];

        // Start scanning from index 1 because index 0 has already been counted.
        for (int i = 1; i < prices.length; i++) {
            // Read the current price for clarity.
            int currentPrice = prices[i];

            // We only count the current price if it is STRICTLY smaller than the
            // smallest price seen before this point.
            //
            // Why strictly smaller?
            // Because equal values do NOT create a new low.
            // Example: if minSoFar is 9 and currentPrice is also 9, it should not count.
            if (currentPrice < minSoFar) {
                // We found a new low, so increase the answer.
                count++;

                // Update the running minimum because this current price is now
                // the smallest value seen in the scan so far.
                minSoFar = currentPrice;
            }

            // If currentPrice is greater than or equal to minSoFar:
            // - It does not count as a new low.
            // - We leave count unchanged.
            // - We leave minSoFar unchanged.
        }

        // After scanning the whole array, count contains the total number of new lows.
        return count;
    }

    /**
     * Helper method to convert an int array into a readable string.
     * This is used in the demo output inside main.
     *
     * @param arr the integer array to display
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the length of the array.
     * Space complexity: O(n), due to the created string content.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) overall for the demonstrated arrays.
     * Space complexity: O(1) extra, excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1 from the problem statement.
        int[] prices1 = {12, 10, 11, 9, 9, 7};
        int result1 = solution.countNewLows(prices1);

        System.out.println("Example 1:");
        System.out.println("Input: prices = " + solution.arrayToString(prices1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 4");
        System.out.println();

        // Sample input 2 from the problem statement.
        int[] prices2 = {5, 5, 5, 4, 6, 3};
        int result2 = solution.countNewLows(prices2);

        System.out.println("Example 2:");
        System.out.println("Input: prices = " + solution.arrayToString(prices2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        // Additional small demonstrations for clarity.

        // Single element: the first item always counts.
        int[] prices3 = {42};
        int result3 = solution.countNewLows(prices3);

        System.out.println("Additional Example 3:");
        System.out.println("Input: prices = " + solution.arrayToString(prices3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 1");
        System.out.println();

        // Strictly decreasing: every element is a new low.
        int[] prices4 = {9, 8, 7, 6, 5};
        int result4 = solution.countNewLows(prices4);

        System.out.println("Additional Example 4:");
        System.out.println("Input: prices = " + solution.arrayToString(prices4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 5");
        System.out.println();

        // Includes duplicates of the minimum; duplicates should not count.
        int[] prices5 = {3, 3, 2, 2, 1, 1};
        int result5 = solution.countNewLows(prices5);

        System.out.println("Additional Example 5:");
        System.out.println("Input: prices = " + solution.arrayToString(prices5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 3");
    }
}