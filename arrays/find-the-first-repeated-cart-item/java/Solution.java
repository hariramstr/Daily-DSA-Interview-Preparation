import java.util.*;

/*
 * Title: Find the First Repeated Cart Item
 * Difficulty: Easy
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array items where each value represents the product ID of an item
 * scanned into an online shopping cart, in the exact order the scans happened.
 *
 * Your task is to return the first product ID that appears more than once while scanning
 * from left to right. In other words, as you read the array from the beginning, return
 * the first item whose current scan is a repeat of an item seen earlier.
 *
 * If no product ID is repeated, return -1.
 *
 * This problem models a common event-processing task: detecting the earliest duplicate
 * action in a stream. The answer is not necessarily the smallest repeated value, and it is
 * not the value with the highest frequency. It is specifically the value whose second
 * appearance happens earliest in the array.
 *
 * Constraints:
 * - 1 <= items.length <= 100000
 * - 1 <= items[i] <= 1000000000
 *
 * Example 1:
 * Input: items = [42, 17, 9, 17, 42]
 * Output: 17
 * Explanation:
 * While scanning left to right, 42 appears first, then 17, then 9.
 * The next value is 17, which is the first repeated item encountered.
 *
 * Example 2:
 * Input: items = [5, 8, 3, 1]
 * Output: -1
 * Explanation:
 * Every item appears exactly once, so there is no repeated cart item.
 */

public class Solution {

    /**
     * Finds the first product ID whose second appearance is encountered earliest
     * while scanning the array from left to right.
     *
     * The idea is simple:
     * 1. Keep a set of product IDs we have already seen.
     * 2. Read each item in order.
     * 3. If the current item is already in the set, then this is the first repeated
     *    item encountered during the scan, so return it immediately.
     * 4. Otherwise, add it to the set and continue.
     * 5. If the scan finishes without finding any repeat, return -1.
     *
     * @param items the array of scanned product IDs in the exact order they were scanned
     * @return the first repeated product ID encountered during the left-to-right scan,
     *         or -1 if no product ID repeats
     *
     * Time complexity: O(n), where n is the length of the array, because each item is
     * processed once and HashSet operations are O(1) on average.
     * Space complexity: O(n), in the worst case when all product IDs are distinct and
     * all of them are stored in the set.
     */
    public int findFirstRepeatedCartItem(int[] items) {
        // Create a HashSet to store all product IDs we have already seen.
        // Why a HashSet?
        // Because it allows us to check "have we seen this before?" very quickly
        // on average in constant time.
        Set<Integer> seen = new HashSet<>();

        // Go through the array from left to right exactly as the problem requires.
        for (int item : items) {
            // Step 1:
            // Check whether the current product ID has already been scanned earlier.
            if (seen.contains(item)) {
                // If yes, then this current scan is a repeat.
                // Because we are scanning from left to right, this is the FIRST repeated
                // item encountered in the stream, so we return it immediately.
                return item;
            }

            // Step 2:
            // If this product ID has not been seen before, record it in the set
            // so future occurrences can be recognized as repeats.
            seen.add(item);
        }

        // If we finish scanning the entire array and never find a repeated item,
        // then there is no duplicate scan event.
        return -1;
    }

    /**
     * Converts an integer array into a readable string representation.
     * This helper method is used only for clean demonstration output in main.
     *
     * @param items the integer array to convert into a string
     * @return a string representation of the array, such as "[1, 2, 3]"
     *
     * Time complexity: O(n), where n is the length of the array.
     * Space complexity: O(n), due to the string being built.
     */
    public String arrayToString(int[] items) {
        return Arrays.toString(items);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional test cases.
     *
     * @param args command-line arguments; not used in this program
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demonstration cases shown here,
     * excluding the cost of the called algorithm on each sample.
     * Space complexity: O(1), excluding the space used by the sample arrays.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1 from the problem statement.
        int[] items1 = {42, 17, 9, 17, 42};
        int result1 = solution.findFirstRepeatedCartItem(items1);
        System.out.println("Input:  " + solution.arrayToString(items1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 17");
        System.out.println();

        // Sample input 2 from the problem statement.
        int[] items2 = {5, 8, 3, 1};
        int result2 = solution.findFirstRepeatedCartItem(items2);
        System.out.println("Input:  " + solution.arrayToString(items2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: -1");
        System.out.println();

        // Additional example:
        // 2 repeats before 1 repeats again, so answer is 2.
        int[] items3 = {1, 2, 3, 2, 1};
        int result3 = solution.findFirstRepeatedCartItem(items3);
        System.out.println("Input:  " + solution.arrayToString(items3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 2");
        System.out.println();

        // Additional example:
        // The first repeated scan happens immediately on the second element.
        int[] items4 = {7, 7, 8, 9};
        int result4 = solution.findFirstRepeatedCartItem(items4);
        System.out.println("Input:  " + solution.arrayToString(items4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 7");
        System.out.println();

        // Additional example:
        // Only one item, so no repetition is possible.
        int[] items5 = {99};
        int result5 = solution.findFirstRepeatedCartItem(items5);
        System.out.println("Input:  " + solution.arrayToString(items5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: -1");
    }
}