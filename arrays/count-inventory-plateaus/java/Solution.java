/*
 * Title: Count Inventory Plateaus
 * Difficulty: Easy
 * Topic: Arrays
 *
 * Problem Description:
 * A warehouse records the number of items in stock at the end of each hour.
 * You are given an integer array stock, where stock[i] is the inventory count
 * for hour i.
 *
 * A contiguous block of hours is called an inventory plateau if all values in
 * that block are equal, and the block is maximal, meaning it cannot be extended
 * to the left or right without changing the value.
 *
 * For example, in [5, 5, 3, 3, 3, 7], the plateaus are:
 * [5, 5], [3, 3, 3], and [7].
 *
 * Your task is to return the number of plateaus whose length is at least k.
 *
 * In other words, scan the array and group adjacent equal values together.
 * Count how many of those groups have size greater than or equal to k.
 *
 * Constraints:
 * - 1 <= stock.length <= 100000
 * - 0 <= stock[i] <= 1000000000
 * - 1 <= k <= stock.length
 *
 * Example 1:
 * Input: stock = [4, 4, 4, 2, 2, 9, 1, 1], k = 2
 * Output: 3
 * Explanation:
 * The plateaus are [4,4,4], [2,2], [9], and [1,1].
 * Three of them have length at least 2.
 *
 * Example 2:
 * Input: stock = [6, 3, 3, 3, 5, 5, 8], k = 3
 * Output: 1
 * Explanation:
 * The plateaus are [6], [3,3,3], [5,5], and [8].
 * Only [3,3,3] has length at least 3.
 */

import java.util.*;

public class Solution {

    /**
     * Counts how many maximal contiguous groups of equal values have length
     * greater than or equal to k.
     *
     * @param stock the inventory counts recorded hour by hour
     * @param k the minimum plateau length required to be counted
     * @return the number of plateaus whose size is at least k
     *
     * Time complexity: O(n), where n is stock.length, because we scan the array once.
     * Space complexity: O(1), because we use only a few extra variables.
     */
    public int countInventoryPlateaus(int[] stock, int k) {
        // This variable will store the final answer:
        // how many plateaus have length >= k.
        int plateauCount = 0;

        // We use index i to walk through the array from left to right.
        int i = 0;

        // Continue until we have processed every element.
        while (i < stock.length) {
            // At position i, we are at the start of a new plateau.
            // Why is it the start?
            // Because every time we finish one plateau, we jump directly
            // to the first index of the next plateau.

            // Store the current value so we know what value this plateau contains.
            int currentValue = stock[i];

            // This will count how many times currentValue repeats contiguously.
            int runLength = 0;

            // Move forward while:
            // 1) we are still inside the array, and
            // 2) the value is still equal to currentValue.
            //
            // Every matching element belongs to the same plateau.
            while (i < stock.length && stock[i] == currentValue) {
                runLength++;
                i++;
            }

            // At this point, one of two things happened:
            // - i reached the end of the array, or
            // - stock[i] is different from currentValue
            //
            // That means the current plateau is complete and maximal.

            // If this plateau is long enough, count it.
            if (runLength >= k) {
                plateauCount++;
            }

            // We do not need to do anything else here.
            // The outer while loop continues from the first index
            // of the next plateau.
        }

        // Return the total number of qualifying plateaus.
        return plateauCount;
    }

    /**
     * Converts an integer array to a readable string for display.
     *
     * @param arr the array to convert
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is arr.length.
     * Space complexity: O(n), due to the created string content.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Runs a single demonstration test case and prints the input and output.
     *
     * @param stock the inventory array for the test
     * @param k the minimum plateau length
     * @return the computed number of valid plateaus
     *
     * Time complexity: O(n), where n is stock.length.
     * Space complexity: O(1), excluding output formatting.
     */
    public int runDemo(int[] stock, int k) {
        int result = countInventoryPlateaus(stock, k);
        System.out.println("stock = " + arrayToString(stock));
        System.out.println("k = " + k);
        System.out.println("Output: " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method demonstrating the solution on sample inputs from the problem.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated test case.
     * Space complexity: O(1), excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // stock = [4, 4, 4, 2, 2, 9, 1, 1], k = 2
        //
        // Plateaus:
        // [4,4,4] -> length 3 -> counted
        // [2,2]   -> length 2 -> counted
        // [9]     -> length 1 -> not counted
        // [1,1]   -> length 2 -> counted
        //
        // Total = 3
        int[] stock1 = {4, 4, 4, 2, 2, 9, 1, 1};
        int k1 = 2;
        int result1 = solution.runDemo(stock1, k1);
        System.out.println("Expected: 3");
        System.out.println("Matches expected: " + (result1 == 3));
        System.out.println();

        // Example 2 from the problem statement:
        // stock = [6, 3, 3, 3, 5, 5, 8], k = 3
        //
        // Plateaus:
        // [6]       -> length 1 -> not counted
        // [3,3,3]   -> length 3 -> counted
        // [5,5]     -> length 2 -> not counted
        // [8]       -> length 1 -> not counted
        //
        // Total = 1
        int[] stock2 = {6, 3, 3, 3, 5, 5, 8};
        int k2 = 3;
        int result2 = solution.runDemo(stock2, k2);
        System.out.println("Expected: 1");
        System.out.println("Matches expected: " + (result2 == 1));
        System.out.println();

        // Additional beginner-friendly example:
        // Every element is different, so every plateau has length 1.
        int[] stock3 = {1, 2, 3, 4};
        int k3 = 1;
        int result3 = solution.runDemo(stock3, k3);
        System.out.println("Expected: 4");
        System.out.println("Matches expected: " + (result3 == 4));
        System.out.println();

        // Additional example:
        // Entire array is one plateau.
        int[] stock4 = {7, 7, 7, 7, 7};
        int k4 = 3;
        int result4 = solution.runDemo(stock4, k4);
        System.out.println("Expected: 1");
        System.out.println("Matches expected: " + (result4 == 1));
    }
}