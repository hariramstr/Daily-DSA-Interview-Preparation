/*
Title: Find the Last Affordable Subscription Day
Difficulty: Easy
Topic: Binary Search

Problem Description:
A streaming platform increases the price of its premium plan over time. You are given a sorted integer array prices where prices[i] is the subscription price on day i, and the values are in non-decreasing order. You are also given an integer budget representing the maximum amount a customer is willing to pay.

Your task is to return the index of the last day on which the subscription price is less than or equal to budget. If every price is greater than budget, return -1.

Because the array is already sorted, an efficient solution should use binary search instead of scanning every day. This is a classic “find the rightmost valid position” problem. Be careful when there are repeated prices: if multiple days have the same affordable price, you must return the largest index among them.

Constraints:
- 1 <= prices.length <= 100000
- 0 <= prices[i] <= 1000000000
- prices is sorted in non-decreasing order
- 0 <= budget <= 1000000000

Example 1:
Input: prices = [5, 7, 7, 10, 14], budget = 7
Output: 2
Explanation: Days 0, 1, and 2 are affordable. The last affordable day is index 2.

Example 2:
Input: prices = [4, 6, 9, 12], budget = 3
Output: -1
Explanation: No day has a price less than or equal to the budget, so the answer is -1.
*/

import java.util.*;

public class Solution {

    /**
     * Finds the index of the last day whose subscription price is less than or equal to the given budget.
     *
     * This method uses binary search on the sorted prices array to efficiently locate the
     * rightmost index i such that prices[i] <= budget.
     *
     * @param prices the sorted array of subscription prices by day; prices[i] is the price on day i
     * @param budget the maximum price the customer is willing to pay
     * @return the largest index whose value is less than or equal to budget, or -1 if no such index exists
     *
     * Time Complexity: O(log n), where n is the length of the prices array
     * Space Complexity: O(1), because only a constant amount of extra space is used
     */
    public int findLastAffordableDay(int[] prices, int budget) {
        // We will search within the full array range:
        // left points to the beginning of the current search space.
        // right points to the end of the current search space.
        int left = 0;
        int right = prices.length - 1;

        // This variable stores the best valid answer found so far.
        // We start with -1 because if no price is affordable, that is the required result.
        int answer = -1;

        // Continue searching while there is still a valid range to inspect.
        while (left <= right) {
            // Compute the middle index safely.
            // Using left + (right - left) / 2 avoids overflow compared to (left + right) / 2.
            int mid = left + (right - left) / 2;

            // If the middle price is affordable, then mid is a valid candidate.
            if (prices[mid] <= budget) {
                // Record mid as the current best answer.
                // Since we want the LAST affordable day, we do not stop here.
                // There may be another affordable day further to the right.
                answer = mid;

                // Move left boundary to mid + 1 to search the right half.
                left = mid + 1;
            } else {
                // If prices[mid] > budget, then mid is too expensive.
                // Because the array is sorted in non-decreasing order,
                // every element to the right of mid is also >= prices[mid],
                // so those are also too expensive.
                // Therefore, we must search only the left half.
                right = mid - 1;
            }
        }

        // When the loop ends, answer contains the rightmost affordable index found,
        // or -1 if no affordable price existed.
        return answer;
    }

    /**
     * A helper method that prints an array in a readable format and shows the computed result.
     *
     * @param prices the sorted array of subscription prices
     * @param budget the maximum affordable price
     * @return the computed last affordable day index for the given input
     *
     * Time Complexity: O(log n) for the search, where n is the length of the prices array
     * Space Complexity: O(1), excluding the space used internally by printing utilities
     */
    public int demonstrateCase(int[] prices, int budget) {
        int result = findLastAffordableDay(prices, budget);
        System.out.println("Prices: " + Arrays.toString(prices));
        System.out.println("Budget: " + budget);
        System.out.println("Last affordable day index: " + result);
        System.out.println();
        return result;
    }

    /**
     * Runs sample demonstrations of the algorithm using the examples from the problem statement
     * and a few additional test cases for clarity.
     *
     * @param args command-line arguments; not used in this program
     * @return nothing
     *
     * Time Complexity: O(k log n) across all demonstrated test cases, where k is the number of test cases
     * and n is the size of each array
     * Space Complexity: O(1), excluding output-related overhead
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // prices = [5, 7, 7, 10, 14], budget = 7
        // Affordable days are indices 0, 1, and 2.
        // The last affordable day is index 2.
        int[] prices1 = {5, 7, 7, 10, 14};
        int budget1 = 7;
        int result1 = solution.demonstrateCase(prices1, budget1);
        System.out.println("Expected: 2, Actual: " + result1);
        System.out.println();

        // Example 2 from the problem statement:
        // prices = [4, 6, 9, 12], budget = 3
        // No price is <= 3, so the answer is -1.
        int[] prices2 = {4, 6, 9, 12};
        int budget2 = 3;
        int result2 = solution.demonstrateCase(prices2, budget2);
        System.out.println("Expected: -1, Actual: " + result2);
        System.out.println();

        // Additional beginner-friendly test:
        // All prices are affordable, so the answer should be the last index.
        int[] prices3 = {2, 4, 4, 4, 9};
        int budget3 = 10;
        int result3 = solution.demonstrateCase(prices3, budget3);
        System.out.println("Expected: 4, Actual: " + result3);
        System.out.println();

        // Additional test with repeated affordable values:
        // The rightmost 6 is at index 3.
        int[] prices4 = {1, 3, 6, 6, 8, 11};
        int budget4 = 6;
        int result4 = solution.demonstrateCase(prices4, budget4);
        System.out.println("Expected: 3, Actual: " + result4);
        System.out.println();

        // Additional test where only the first element is affordable.
        int[] prices5 = {5, 8, 12, 20};
        int budget5 = 5;
        int result5 = solution.demonstrateCase(prices5, budget5);
        System.out.println("Expected: 0, Actual: " + result5);
    }
}