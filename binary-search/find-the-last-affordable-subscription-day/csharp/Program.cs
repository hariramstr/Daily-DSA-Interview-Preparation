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

using System;

public class Solution
{
    /*
    Time Complexity: O(log n)
    Space Complexity: O(1)

    We use binary search because the prices array is already sorted in non-decreasing order.
    That sorted order allows us to eliminate half of the remaining search space at each step.

    Goal:
    Find the RIGHTMOST index i such that prices[i] <= budget.

    Important idea:
    - If prices[mid] <= budget, then mid is a valid affordable day.
      But there might be another valid day farther to the right, so we continue searching right.
    - If prices[mid] > budget, then mid is too expensive, and everything to the right is also
      too expensive or equal/larger because the array is sorted. So we search left.
    */
    public int FindLastAffordableDay(int[] prices, int budget)
    {
        // "left" and "right" define the current search range.
        // We start by searching the entire array.
        int left = 0;
        int right = prices.Length - 1;

        // This variable stores the best answer found so far.
        // We initialize it to -1 because if no affordable day exists,
        // the required result is -1.
        int answer = -1;

        // Continue searching while there is still a valid range to inspect.
        while (left <= right)
        {
            // Compute the middle index safely.
            // We use this form instead of (left + right) / 2 to avoid overflow in general.
            int mid = left + (right - left) / 2;

            // Step 1: Check whether the current middle price is affordable.
            if (prices[mid] <= budget)
            {
                // This day is affordable, so it is a valid candidate answer.
                // We store it because we are looking for the last (rightmost) affordable day.
                answer = mid;

                // Why move right?
                // Since prices[mid] is affordable, there may be another affordable day
                // later in the array. Because the array is sorted, all values before mid
                // are not more useful than mid for finding the LAST valid index.
                // So we discard the left half including mid and continue on the right side.
                left = mid + 1;
            }
            else
            {
                // prices[mid] > budget, so this day is too expensive.

                // Why move left?
                // Because the array is sorted in non-decreasing order, every element to the right
                // of mid is also >= prices[mid], which means those days are also too expensive.
                // Therefore, the answer cannot be at mid or to the right of mid.
                // We must continue searching only in the left half.
                right = mid - 1;
            }
        }

        // When the loop ends, "answer" contains the largest index found
        // where prices[index] <= budget, or -1 if no such index exists.
        return answer;
    }
}

// Demo code:
// We create sample inputs from the problem statement,
// call the solution method, and print the results.

var solution = new Solution();

// Example 1
int[] prices1 = { 5, 7, 7, 10, 14 };
int budget1 = 7;
int result1 = solution.FindLastAffordableDay(prices1, budget1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[] prices2 = { 4, 6, 9, 12 };
int budget2 = 3;
int result2 = solution.FindLastAffordableDay(prices2, budget2);
Console.WriteLine(result2); // Expected: -1

// Additional quick checks for learning and confidence:

// All days affordable
int[] prices3 = { 2, 3, 3, 5 };
int budget3 = 10;
int result3 = solution.FindLastAffordableDay(prices3, budget3);
Console.WriteLine(result3); // Expected: 3

// Only first day affordable
int[] prices4 = { 1, 4, 6, 8 };
int budget4 = 1;
int result4 = solution.FindLastAffordableDay(prices4, budget4);
Console.WriteLine(result4); // Expected: 0

// Repeated affordable values, must return the largest index among them
int[] prices5 = { 5, 5, 5, 9, 12 };
int budget5 = 5;
int result5 = solution.FindLastAffordableDay(prices5, budget5);
Console.WriteLine(result5); // Expected: 2