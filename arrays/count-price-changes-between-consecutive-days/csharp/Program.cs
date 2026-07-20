/*
Title: Count Price Changes Between Consecutive Days
Difficulty: Easy
Topic: Arrays

Problem Description:
You are given an integer array prices where prices[i] represents the price of the same product on day i.
Your task is to count how many times the price changed compared with the previous day.
A change is recorded whenever two consecutive values are different.
If the price stays the same from one day to the next, it does not count as a change.

Return the total number of price changes across the entire array.

This problem models a simple analytics task often used in dashboards: instead of caring about the size of a change,
you only need to know how many transitions happened.

Example:
If the prices are [5, 5, 7, 7, 6], then the changes happen from 5 to 7 and from 7 to 6, so the answer is 2.

If the array has length 0 or 1, the answer is 0 because there are no consecutive pairs to compare.

Constraints:
- 0 <= prices.length <= 100000
- -1000000000 <= prices[i] <= 1000000000

Example 1:
Input: prices = [10, 10, 12, 12, 9, 9, 11]
Output: 3
Explanation: Changes occur at 10 -> 12, 12 -> 9, and 9 -> 11.

Example 2:
Input: prices = [4, 4, 4, 4]
Output: 0
Explanation: Every consecutive pair is equal, so there are no price changes.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the array exactly once.
    - For each position after the first, we compare the current value with the previous value.
    - That means the total work grows linearly with the number of elements.

    Space Complexity: O(1)
    - We only use a small fixed number of variables:
      - one counter for the number of changes
      - one loop index
    - We do not create any extra data structures that grow with input size.
    */
    public int CountPriceChanges(int[] prices)
    {
        // Step 1:
        // Handle the smallest possible inputs first.
        //
        // Why this is necessary:
        // A "change" can only happen between two consecutive days.
        // If the array has:
        // - 0 elements: there are no days at all
        // - 1 element: there is only one day, so there is no previous day to compare against
        //
        // In both cases, there are no consecutive pairs, so the answer must be 0.
        if (prices == null || prices.Length <= 1)
        {
            return 0;
        }

        // Step 2:
        // Create a counter to store how many times the price changes.
        //
        // Why this is necessary:
        // We need to return the total number of transitions where:
        // prices[i] != prices[i - 1]
        //
        // Data structure choice:
        // We use a simple integer variable because we only need a running total.
        // No array, list, dictionary, or other structure is needed.
        int changeCount = 0;

        // Step 3:
        // Start scanning from index 1, not index 0.
        //
        // Why start at index 1?
        // Because each day's price must be compared with the previous day.
        // The first element at index 0 has no previous element, so there is nothing to compare it to.
        //
        // For every i from 1 to the end:
        // - compare prices[i] with prices[i - 1]
        for (int i = 1; i < prices.Length; i++)
        {
            // Step 4:
            // Check whether the current day's price is different from the previous day's price.
            //
            // Why this works:
            // The problem defines a "change" exactly as two consecutive values being different.
            // So this condition directly matches the problem statement.
            if (prices[i] != prices[i - 1])
            {
                // Step 5:
                // If the two consecutive prices are different, record one change.
                //
                // Why increment by 1?
                // Because each pair of consecutive days contributes either:
                // - 1 change, if different
                // - 0 changes, if equal
                changeCount++;
            }

            // If prices[i] == prices[i - 1], we do nothing.
            //
            // Why do nothing?
            // Because equal consecutive prices do not count as a change.
        }

        // Step 6:
        // After checking every consecutive pair, return the total count.
        return changeCount;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print results.

var solution = new Solution();

// Example 1 from the problem description:
// [10, 10, 12, 12, 9, 9, 11]
// Comparisons:
// 10 vs 10 -> same, no change
// 12 vs 10 -> different, change #1
// 12 vs 12 -> same, no change
// 9 vs 12  -> different, change #2
// 9 vs 9   -> same, no change
// 11 vs 9  -> different, change #3
// Expected output: 3
int[] prices1 = { 10, 10, 12, 12, 9, 9, 11 };
Console.WriteLine(solution.CountPriceChanges(prices1)); // 3

// Example 2 from the problem description:
// [4, 4, 4, 4]
// Every consecutive pair is equal.
// Expected output: 0
int[] prices2 = { 4, 4, 4, 4 };
Console.WriteLine(solution.CountPriceChanges(prices2)); // 0

// Additional example mentioned in the description:
// [5, 5, 7, 7, 6]
// Changes:
// 5 -> 7
// 7 -> 6
// Expected output: 2
int[] prices3 = { 5, 5, 7, 7, 6 };
Console.WriteLine(solution.CountPriceChanges(prices3)); // 2

// Edge case: empty array
// No consecutive pairs, so answer is 0.
int[] prices4 = Array.Empty<int>();
Console.WriteLine(solution.CountPriceChanges(prices4)); // 0

// Edge case: single element
// Only one day, so no previous day to compare with.
int[] prices5 = { 42 };
Console.WriteLine(solution.CountPriceChanges(prices5)); // 0