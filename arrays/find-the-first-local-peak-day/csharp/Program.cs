/*
Title: Find the First Local Peak Day

Problem Description:
You are given an integer array `visitors` where `visitors[i]` represents the number of visitors to a store on day `i`.
A day is called a local peak if its visitor count is strictly greater than the visitor count of the previous day
and strictly greater than the visitor count of the next day.

In other words, for some index `i`, day `i` is a local peak when:
    visitors[i] > visitors[i - 1]
and visitors[i] > visitors[i + 1]

Your task is to return the index of the first local peak day in the array.
If no such day exists, return -1.

Only days with both a previous and next day can be local peaks, so the first and last elements
can never be considered peaks.

A simple linear scan is expected.

Constraints:
- 3 <= visitors.length <= 10^5
- 0 <= visitors[i] <= 10^6

Example 1:
Input: visitors = [12, 18, 15, 20, 19]
Output: 1

Explanation:
Day 1 has 18 visitors, which is greater than 12 and 15.
Although day 3 is also a local peak, the first one appears at index 1.

Example 2:
Input: visitors = [5, 7, 7, 6, 4]
Output: -1

Explanation:
Day 1 is not a peak because 7 is not strictly greater than the next value 7.
No index satisfies the local peak condition.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the array once from left to right.
    - Each index is checked at most one time.

    Space Complexity: O(1)
    - We use only a few extra variables.
    - No additional data structures are needed.

    Beginner-friendly idea:
    We only need to inspect the "middle" days, because the first and last day
    cannot be local peaks. For each valid index i, we compare visitors[i]
    with its left neighbor visitors[i - 1] and right neighbor visitors[i + 1].
    The first index that is strictly greater than both neighbors is the answer.
    */
    public int FindFirstLocalPeakDay(int[] visitors)
    {
        // Step 1:
        // We loop only from index 1 to index visitors.Length - 2.
        //
        // Why?
        // - Index 0 has no previous day, so it cannot be a local peak.
        // - The last index has no next day, so it also cannot be a local peak.
        //
        // This boundary handling is essential to avoid invalid array access
        // and to match the exact problem definition.
        for (int i = 1; i < visitors.Length - 1; i++)
        {
            // Step 2:
            // Read the values around the current day:
            // - left: the previous day's visitor count
            // - current: the current day's visitor count
            // - right: the next day's visitor count
            //
            // Why store them in variables?
            // - It makes the code easier to read.
            // - It helps beginners clearly see the three values being compared.
            // - It avoids repeating the same array indexing expression multiple times.
            int left = visitors[i - 1];
            int current = visitors[i];
            int right = visitors[i + 1];

            // Step 3:
            // Check whether the current day is a local peak.
            //
            // The condition must be STRICT:
            // current > left AND current > right
            //
            // Why strict comparison?
            // The problem says the current day must be strictly greater than both neighbors.
            // So equal values do NOT count as a peak.
            //
            // Example:
            // [5, 7, 7, 6, 4]
            // At index 1, current = 7 and right = 7.
            // Since 7 is not strictly greater than 7, index 1 is not a peak.
            if (current > left && current > right)
            {
                // Step 4:
                // As soon as we find the first valid local peak, we return its index immediately.
                //
                // Why return immediately?
                // The problem asks for the FIRST local peak day.
                // Since we are scanning from left to right, the first one we find
                // is guaranteed to be the correct answer.
                return i;
            }

            // Step 5:
            // If the current index is not a peak, the loop simply continues
            // to the next possible day and repeats the same comparison process.
        }

        // Step 6:
        // If we finish the loop without returning, then no local peak exists.
        //
        // Therefore, we return -1 exactly as required by the problem statement.
        return -1;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print results.

var solution = new Solution();

// Example 1:
// visitors = [12, 18, 15, 20, 19]
// Index 1: 18 > 12 and 18 > 15 => peak, so answer should be 1
int[] visitors1 = { 12, 18, 15, 20, 19 };
int result1 = solution.FindFirstLocalPeakDay(visitors1);
Console.WriteLine(result1);

// Example 2:
// visitors = [5, 7, 7, 6, 4]
// Index 1: 7 is not strictly greater than 7
// Index 2: 7 is not strictly greater than left 7
// Index 3: 6 is not greater than left 7
// No peak exists, so answer should be -1
int[] visitors2 = { 5, 7, 7, 6, 4 };
int result2 = solution.FindFirstLocalPeakDay(visitors2);
Console.WriteLine(result2);

// Additional demo:
// visitors = [1, 3, 2]
// Index 1: 3 > 1 and 3 > 2 => peak at index 1
int[] visitors3 = { 1, 3, 2 };
int result3 = solution.FindFirstLocalPeakDay(visitors3);
Console.WriteLine(result3);

// Additional demo:
// visitors = [2, 2, 2, 2]
// No value is strictly greater than both neighbors
int[] visitors4 = { 2, 2, 2, 2 };
int result4 = solution.FindFirstLocalPeakDay(visitors4);
Console.WriteLine(result4);