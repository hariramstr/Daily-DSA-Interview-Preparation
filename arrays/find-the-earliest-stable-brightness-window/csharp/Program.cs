/*
Title: Find the Earliest Stable Brightness Window
Difficulty: Easy
Topic: Arrays

Problem Description:
You are given an array brightness where brightness[i] is the measured screen brightness on minute i,
and an integer k. A monitoring system wants to find the earliest contiguous window of exactly k minutes
where the brightness never changes too sharply.

A window of length k is called stable if the difference between the maximum and minimum values inside
that window is less than or equal to limit.

Return the starting index of the earliest stable window of length k. If no such window exists, return -1.

This problem models a simple quality check over time-series array data. Since the difficulty is easy,
you may assume the constraints are small enough for a straightforward solution, although cleaner
solutions are encouraged.

Constraints:
- 1 <= brightness.length <= 200
- 1 <= brightness[i] <= 10^4
- 1 <= k <= brightness.length
- 0 <= limit <= 10^4

Example 1:
Input: brightness = [7, 9, 8, 8, 10, 13], k = 3, limit = 2
Output: 0
Explanation: The window [7, 9, 8] has max = 9 and min = 7, so the difference is 2, which is allowed.
Since this is the earliest valid window, return 0.

Example 2:
Input: brightness = [4, 10, 3, 12, 8], k = 2, limit = 1
Output: -1
Explanation: Every window of length 2 has a max-min difference greater than 1, so there is no stable window.

Goal:
Scan the array and determine the first starting position that satisfies the stability rule for a window
of exactly k consecutive elements.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n * k)
    - There are (n - k + 1) possible windows.
    - For each window, we scan up to k elements to find the minimum and maximum.
    - Since constraints are small (n <= 200), this simple approach is fully acceptable and easy to understand.

    Space Complexity: O(1)
    - We only use a few extra variables for min, max, and loop counters.
    - No additional data structures are required.
    */
    public int FindEarliestStableWindow(int[] brightness, int k, int limit)
    {
        // Step 1:
        // We will examine every possible contiguous window of length exactly k.
        //
        // Why this is necessary:
        // The problem asks for the earliest valid window, so we must check windows
        // in left-to-right order. The first one that satisfies the condition should be returned immediately.
        //
        // If the array length is n, then the last valid starting index for a window of size k is n - k.
        // Example:
        // n = 6, k = 3
        // valid starts are 0, 1, 2, 3
        for (int start = 0; start <= brightness.Length - k; start++)
        {
            // Step 2:
            // For the current window, we need to know:
            // - the minimum brightness value inside the window
            // - the maximum brightness value inside the window
            //
            // Why this is necessary:
            // A window is stable if:
            //     max - min <= limit
            //
            // So we must compute min and max for each candidate window.
            //
            // Data structure choice:
            // We do not need any special data structure here because the constraints are small.
            // A direct scan of the k elements is simple, readable, and correct.
            int currentMin = int.MaxValue;
            int currentMax = int.MinValue;

            // Step 3:
            // Scan all elements inside the current window:
            // indices from start to start + k - 1
            //
            // During this scan:
            // - update currentMin whenever we find a smaller value
            // - update currentMax whenever we find a larger value
            for (int i = start; i < start + k; i++)
            {
                // Step 3a:
                // Update the minimum value seen so far in this window.
                //
                // Why this is necessary:
                // We need the smallest value in the window to later compute max - min.
                if (brightness[i] < currentMin)
                {
                    currentMin = brightness[i];
                }

                // Step 3b:
                // Update the maximum value seen so far in this window.
                //
                // Why this is necessary:
                // We need the largest value in the window to later compute max - min.
                if (brightness[i] > currentMax)
                {
                    currentMax = brightness[i];
                }
            }

            // Step 4:
            // After scanning the full window, compute the spread:
            //     currentMax - currentMin
            //
            // If this spread is within the allowed limit, then the window is stable.
            //
            // Why this is necessary:
            // This is exactly the rule given in the problem statement.
            if (currentMax - currentMin <= limit)
            {
                // Step 5:
                // Because we are checking windows from left to right,
                // the first valid window we find is automatically the earliest one.
                //
                // Therefore, we can return immediately.
                return start;
            }
        }

        // Step 6:
        // If we finish checking every possible window and none are stable,
        // then the correct answer is -1.
        return -1;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// brightness = [7, 9, 8, 8, 10, 13], k = 3, limit = 2
// Windows:
// start 0 -> [7, 9, 8], max = 9, min = 7, diff = 2 -> valid
// Earliest valid start is 0
int[] brightness1 = { 7, 9, 8, 8, 10, 13 };
int k1 = 3;
int limit1 = 2;
int result1 = solution.FindEarliestStableWindow(brightness1, k1, limit1);
Console.WriteLine(result1); // Expected: 0

// Example 2:
// brightness = [4, 10, 3, 12, 8], k = 2, limit = 1
// Windows:
// [4,10] diff = 6
// [10,3] diff = 7
// [3,12] diff = 9
// [12,8] diff = 4
// None are valid, so answer is -1
int[] brightness2 = { 4, 10, 3, 12, 8 };
int k2 = 2;
int limit2 = 1;
int result2 = solution.FindEarliestStableWindow(brightness2, k2, limit2);
Console.WriteLine(result2); // Expected: -1

// Additional demo:
// Entire array as the window
int[] brightness3 = { 5, 5, 6, 5 };
int k3 = 4;
int limit3 = 1;
int result3 = solution.FindEarliestStableWindow(brightness3, k3, limit3);
Console.WriteLine(result3); // Expected: 0

// Additional demo:
// Single-element windows are always stable because max == min, so diff = 0
int[] brightness4 = { 100, 200, 300 };
int k4 = 1;
int limit4 = 0;
int result4 = solution.FindEarliestStableWindow(brightness4, k4, limit4);
Console.WriteLine(result4); // Expected: 0