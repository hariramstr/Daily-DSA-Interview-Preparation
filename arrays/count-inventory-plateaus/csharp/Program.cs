/*
Title: Count Inventory Plateaus
Difficulty: Easy
Topic: Arrays

Problem Description:
A warehouse records the number of items in stock at the end of each hour. You are given an integer array `stock`, where `stock[i]` is the inventory count for hour `i`.

A contiguous block of hours is called an inventory plateau if all values in that block are equal, and the block is maximal, meaning it cannot be extended to the left or right without changing the value. For example, in `[5, 5, 3, 3, 3, 7]`, the plateaus are `[5, 5]`, `[3, 3, 3]`, and `[7]`.

Your task is to return the number of plateaus whose length is at least `k`.

In other words, scan the array and group adjacent equal values together. Count how many of those groups have size greater than or equal to `k`.

This problem is meant to test careful array traversal and handling of contiguous runs. An efficient solution should run in linear time.

Constraints:
- `1 <= stock.length <= 100000`
- `0 <= stock[i] <= 1000000000`
- `1 <= k <= stock.length`

Example 1:
Input: stock = [4, 4, 4, 2, 2, 9, 1, 1], k = 2
Output: 3
Explanation: The plateaus are `[4,4,4]`, `[2,2]`, `[9]`, and `[1,1]`. Three of them have length at least 2.

Example 2:
Input: stock = [6, 3, 3, 3, 5, 5, 8], k = 3
Output: 1
Explanation: The plateaus are `[6]`, `[3,3,3]`, `[5,5]`, and `[8]`. Only `[3,3,3]` has length at least 3.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the array exactly once.
    - Each element is visited a constant number of times.

    Space Complexity: O(1)
    - We only use a few integer variables.
    - No extra array, list, dictionary, or other data structure is needed.
    */
    public int CountPlateaus(int[] stock, int k)
    {
        // This variable will store the final answer:
        // how many maximal groups of equal adjacent values
        // have length at least k.
        int plateauCount = 0;

        // This variable tracks the length of the current run (plateau)
        // of equal values as we scan from left to right.
        //
        // We start at 1 because if the array has at least one element,
        // the first element already begins a run of length 1 by itself.
        int currentRunLength = 1;

        // We begin from index 1 because we compare each element with
        // the previous one to decide whether the current plateau continues
        // or a new plateau starts.
        for (int i = 1; i < stock.Length; i++)
        {
            // Step 1: Check whether the current value matches the previous value.
            //
            // Why this matters:
            // - If stock[i] == stock[i - 1], then we are still inside the same plateau.
            // - If stock[i] != stock[i - 1], then the previous plateau has ended at i - 1,
            //   and a new plateau begins at i.
            if (stock[i] == stock[i - 1])
            {
                // The plateau continues, so we increase its length by 1.
                currentRunLength++;
            }
            else
            {
                // Step 2: The current plateau has ended.
                //
                // Before resetting for the new plateau, we must decide whether
                // the plateau we just finished is long enough to count.
                //
                // Why this step is necessary:
                // - A plateau is maximal, so once the value changes, we know for sure
                //   the previous group cannot be extended further.
                // - This is the exact moment to evaluate its final size.
                if (currentRunLength >= k)
                {
                    plateauCount++;
                }

                // Step 3: Start counting the new plateau.
                //
                // Since stock[i] is different from stock[i - 1], the element at i
                // begins a brand-new plateau of length 1.
                currentRunLength = 1;
            }
        }

        // Step 4: Handle the final plateau.
        //
        // Why this is necessary:
        // - Inside the loop, we only count a plateau when we see a change.
        // - The last plateau reaches the end of the array, so there is no later
        //   change to trigger counting it.
        // - Therefore, after the loop finishes, we must manually check the final run.
        if (currentRunLength >= k)
        {
            plateauCount++;
        }

        // Return the total number of qualifying plateaus.
        return plateauCount;
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

// Example 1:
// stock = [4, 4, 4, 2, 2, 9, 1, 1], k = 2
// Plateaus: [4,4,4], [2,2], [9], [1,1]
// Lengths:   3        2      1    2
// Qualifying plateaus (length >= 2): 3
int[] stock1 = { 4, 4, 4, 2, 2, 9, 1, 1 };
int k1 = 2;
int result1 = solution.CountPlateaus(stock1, k1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// stock = [6, 3, 3, 3, 5, 5, 8], k = 3
// Plateaus: [6], [3,3,3], [5,5], [8]
// Lengths:   1      3        2     1
// Qualifying plateaus (length >= 3): 1
int[] stock2 = { 6, 3, 3, 3, 5, 5, 8 };
int k2 = 3;
int result2 = solution.CountPlateaus(stock2, k2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo:
// Single plateau across the whole array.
int[] stock3 = { 7, 7, 7, 7 };
int k3 = 4;
int result3 = solution.CountPlateaus(stock3, k3);
Console.WriteLine($"Additional Demo 1 Result: {result3}");

// Additional demo:
// Every value is different, so each plateau has length 1.
int[] stock4 = { 1, 2, 3, 4, 5 };
int k4 = 1;
int result4 = solution.CountPlateaus(stock4, k4);
Console.WriteLine($"Additional Demo 2 Result: {result4}");