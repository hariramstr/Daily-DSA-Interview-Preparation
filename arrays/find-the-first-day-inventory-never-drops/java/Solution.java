import java.util.*;

/*
 * Title: Find the First Day Inventory Never Drops
 * Difficulty: Easy
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array stock where stock[i] represents the inventory level
 * of a product at the end of day i. A day is considered stable if its inventory is
 * greater than or equal to the inventory of the previous day. Your task is to return
 * the index of the first day from which the inventory never drops again for the rest
 * of the array.
 *
 * More formally, find the smallest index i such that for every j where i < j < n,
 * stock[j] >= stock[j - 1]. In other words, the subarray stock[i...n-1] must be
 * non-decreasing. If the entire array is already non-decreasing, return 0.
 *
 * This problem models a real inventory dashboard where managers want to know the first
 * day after which stock levels stop declining and only stay the same or increase.
 *
 * Constraints:
 * - 1 <= stock.length <= 100000
 * - -1000000000 <= stock[i] <= 1000000000
 * - The answer is always a valid index from 0 to n - 1
 *
 * Example 1:
 * Input: stock = [9, 7, 8, 8, 10]
 * Output: 1
 * Explanation:
 * Starting from index 1, the values are [7, 8, 8, 10], which is non-decreasing.
 * Index 0 does not work because 7 < 9, so inventory drops between day 0 and day 1.
 *
 * Example 2:
 * Input: stock = [5, 6, 4, 7, 9]
 * Output: 2
 * Explanation:
 * The suffix starting at index 2 is [4, 7, 9], which is non-decreasing.
 * Any earlier starting index fails because there is a drop from 6 to 4.
 */

public class Solution {

    /**
     * Finds the smallest index such that the suffix starting at that index is non-decreasing.
     *
     * The key observation is:
     * - A suffix stock[i...n-1] is non-decreasing if and only if every adjacent pair inside
     *   that suffix satisfies stock[k] <= stock[k + 1].
     * - Therefore, if we scan from right to left, we can keep moving left as long as the
     *   non-decreasing condition continues to hold.
     * - The moment we see a drop when moving left (that is, stock[i] > stock[i + 1]),
     *   then index i cannot be part of the valid suffix, so the earliest valid starting
     *   point must be i + 1.
     *
     * Step-by-step idea:
     * 1. Start from the second-last element and move left.
     * 2. For each index i, compare stock[i] with stock[i + 1].
     * 3. If stock[i] <= stock[i + 1], then the suffix starting at i is still non-decreasing
     *    so far, so continue moving left.
     * 4. If stock[i] > stock[i + 1], then there is a drop between i and i + 1.
     *    That means any suffix starting at or before i is invalid.
     *    So the first valid day is i + 1.
     * 5. If we finish the loop without finding any drop, the whole array is already
     *    non-decreasing, so return 0.
     *
     * @param stock the array where stock[i] is the inventory level at the end of day i
     * @return the index of the first day from which inventory never drops again
     * Time complexity: O(n), because we scan the array once from right to left.
     * Space complexity: O(1), because we use only a constant amount of extra space.
     */
    public int firstStableDay(int[] stock) {
        // If the array has only one element, then that single day is trivially the answer.
        // There are no later days, so inventory can never drop after it.
        if (stock == null || stock.length <= 1) {
            return 0;
        }

        // We scan from right to left because we want to identify the earliest index
        // whose suffix is non-decreasing.
        //
        // Why right to left?
        // Because the last element alone always forms a non-decreasing suffix.
        // Then we try to extend that valid suffix leftward one element at a time.
        for (int i = stock.length - 2; i >= 0; i--) {
            // Compare the current day with the next day.
            //
            // If stock[i] > stock[i + 1], then inventory drops from day i to day i + 1.
            // That means:
            // - starting at i is invalid
            // - starting at any earlier index is also invalid, because that same drop
            //   would still be inside the suffix
            // Therefore, the earliest valid starting point must be i + 1.
            if (stock[i] > stock[i + 1]) {
                return i + 1;
            }

            // If stock[i] <= stock[i + 1], then there is no drop at this boundary.
            // So the suffix can potentially be extended further left.
            // We simply continue the loop.
        }

        // If we never found any drop, then every adjacent pair satisfies:
        // stock[i] <= stock[i + 1]
        // for all valid i.
        //
        // That means the entire array is already non-decreasing,
        // so the answer is 0.
        return 0;
    }

    /**
     * Converts an integer array into a readable string representation.
     * This helper method is used only for demonstration output in main.
     *
     * @param array the input integer array
     * @return a string representation such as [1, 2, 3]
     * Time complexity: O(n), because each element is processed once.
     * Space complexity: O(n), because the resulting string stores all elements.
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional beginner-friendly test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n) overall for k demonstrated test cases of size n,
     * depending on the lengths of the sample arrays used here.
     * Space complexity: O(1) extra space excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample test case 1 from the problem statement.
        int[] stock1 = {9, 7, 8, 8, 10};
        int result1 = solution.firstStableDay(stock1);
        System.out.println("Input:  " + solution.arrayToString(stock1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 1");
        System.out.println();

        // Manual trace for correctness:
        // [9, 7, 8, 8, 10]
        // Compare from right:
        // 8 <= 10 -> okay
        // 8 <= 8  -> okay
        // 7 <= 8  -> okay
        // 9 > 7   -> drop found, answer is 1
        //
        // This matches the expected output.

        // Sample test case 2 from the problem statement.
        int[] stock2 = {5, 6, 4, 7, 9};
        int result2 = solution.firstStableDay(stock2);
        System.out.println("Input:  " + solution.arrayToString(stock2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 2");
        System.out.println();

        // Manual trace for correctness:
        // [5, 6, 4, 7, 9]
        // Compare from right:
        // 7 <= 9  -> okay
        // 4 <= 7  -> okay
        // 6 > 4   -> drop found, answer is 2
        //
        // This matches the expected output.

        // Additional test case: already non-decreasing.
        int[] stock3 = {1, 2, 2, 3, 5};
        int result3 = solution.firstStableDay(stock3);
        System.out.println("Input:  " + solution.arrayToString(stock3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        // Additional test case: strictly decreasing.
        int[] stock4 = {10, 9, 8, 7};
        int result4 = solution.firstStableDay(stock4);
        System.out.println("Input:  " + solution.arrayToString(stock4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 3");
        System.out.println();

        // Additional test case: single element.
        int[] stock5 = {42};
        int result5 = solution.firstStableDay(stock5);
        System.out.println("Input:  " + solution.arrayToString(stock5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 0");
    }
}