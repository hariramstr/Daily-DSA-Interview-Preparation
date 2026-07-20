import java.util.*;

/*
 * Title: Count Price Changes Between Consecutive Days
 * Difficulty: Easy
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array prices where prices[i] represents the price of the same product on day i.
 * Your task is to count how many times the price changed compared with the previous day.
 * A change is recorded whenever two consecutive values are different.
 * If the price stays the same from one day to the next, it does not count as a change.
 *
 * Return the total number of price changes across the entire array.
 *
 * This problem models a simple analytics task often used in dashboards: instead of caring about the size
 * of a change, you only need to know how many transitions happened.
 * For example, if the prices are [5, 5, 7, 7, 6], then the changes happen from 5 to 7 and from 7 to 6,
 * so the answer is 2.
 *
 * If the array has length 0 or 1, the answer is 0 because there are no consecutive pairs to compare.
 *
 * Constraints:
 * - 0 <= prices.length <= 100000
 * - -1000000000 <= prices[i] <= 1000000000
 *
 * Example 1:
 * Input: prices = [10, 10, 12, 12, 9, 9, 11]
 * Output: 3
 * Explanation: Changes occur at 10 -> 12, 12 -> 9, and 9 -> 11.
 *
 * Example 2:
 * Input: prices = [4, 4, 4, 4]
 * Output: 0
 * Explanation: Every consecutive pair is equal, so there are no price changes.
 */

public class Solution {

    /**
     * Counts how many times the price changes between consecutive days.
     *
     * The method scans the array from left to right and compares each value
     * with the value immediately before it. Whenever the two values are different,
     * that means a price change occurred, so we increase the counter.
     *
     * @param prices the array of daily prices; each element represents the price on one day
     * @return the total number of times the price changed between consecutive days
     *
     * Time complexity: O(n), where n is the length of the prices array,
     * because we examine each consecutive pair exactly once.
     *
     * Space complexity: O(1), because we use only a small fixed amount of extra memory.
     */
    public int countPriceChanges(int[] prices) {
        // If the array is null, empty, or has only one element,
        // there are no consecutive pairs to compare.
        // Therefore, the number of changes must be 0.
        if (prices == null || prices.length <= 1) {
            return 0;
        }

        // This variable will store the total number of detected changes.
        int changes = 0;

        // Start from index 1 because each day is compared with the previous day.
        // For each i:
        // - prices[i - 1] is the previous day's price
        // - prices[i] is the current day's price
        for (int i = 1; i < prices.length; i++) {
            // Compare the current day's price with the previous day's price.
            // If they are different, that means the price changed.
            if (prices[i] != prices[i - 1]) {
                // Increase the count by 1 for this transition.
                changes++;
            }
            // If they are equal, we do nothing because no change occurred.
        }

        // After checking all consecutive pairs, return the total count.
        return changes;
    }

    /**
     * Converts an integer array into a readable string representation.
     * This helper method is used only for demonstration output in main.
     *
     * @param array the integer array to convert into a string
     * @return a string representation of the array, such as [1, 2, 3]
     *
     * Time complexity: O(n), where n is the length of the array,
     * because each element is processed once.
     *
     * Space complexity: O(n), due to the string content being built.
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments; not used in this program
     * @return nothing
     *
     * Time complexity: O(k + totalElementsPrinted), where k is the number of test cases
     * and each test case calls the linear-time counting method.
     *
     * Space complexity: O(1) extra space excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1 from the problem statement.
        int[] prices1 = {10, 10, 12, 12, 9, 9, 11};
        int result1 = solution.countPriceChanges(prices1);
        System.out.println("Input: prices = " + solution.arrayToString(prices1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 3");
        System.out.println();

        // Sample input 2 from the problem statement.
        int[] prices2 = {4, 4, 4, 4};
        int result2 = solution.countPriceChanges(prices2);
        System.out.println("Input: prices = " + solution.arrayToString(prices2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 0");
        System.out.println();

        // Example mentioned in the description.
        int[] prices3 = {5, 5, 7, 7, 6};
        int result3 = solution.countPriceChanges(prices3);
        System.out.println("Input: prices = " + solution.arrayToString(prices3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 2");
        System.out.println();

        // Edge case: empty array.
        int[] prices4 = {};
        int result4 = solution.countPriceChanges(prices4);
        System.out.println("Input: prices = " + solution.arrayToString(prices4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 0");
        System.out.println();

        // Edge case: single element.
        int[] prices5 = {42};
        int result5 = solution.countPriceChanges(prices5);
        System.out.println("Input: prices = " + solution.arrayToString(prices5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 0");
        System.out.println();

        // Additional test: every day changes.
        int[] prices6 = {1, 2, 3, 4, 5};
        int result6 = solution.countPriceChanges(prices6);
        System.out.println("Input: prices = " + solution.arrayToString(prices6));
        System.out.println("Output: " + result6);
        System.out.println("Expected: 4");
    }
}