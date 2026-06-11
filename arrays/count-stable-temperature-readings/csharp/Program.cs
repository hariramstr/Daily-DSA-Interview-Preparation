/*
Title: Count Stable Temperature Readings
Difficulty: Easy
Topic: Arrays

Problem Description:
You are given an integer array readings where readings[i] represents the temperature recorded on day i.
A reading is called stable if its value is equal to the average of its immediate neighbors.

For an index i to be stable, it must satisfy:
1 <= i <= n - 2
and
readings[i] * 2 == readings[i - 1] + readings[i + 1]

Your task is to return the number of stable readings in the array.

A stable reading is not necessarily the same as its neighbors, but it must lie exactly halfway
between the previous and next values. Since only immediate neighbors matter, each valid index
can be checked independently.

If the array has fewer than 3 elements, the answer is 0 because no element has two neighbors.

Constraints:
- 1 <= readings.length <= 100000
- -100000 <= readings[i] <= 100000
- The expected solution should run in O(n) time
- Use O(1) extra space aside from the input

Example 1:
Input: readings = [4, 6, 8, 7, 6]
Output: 2

Explanation:
- Index 1 is stable because 6 is the average of 4 and 8.
- Index 2 is not stable because 8 is not the average of 6 and 7.
- Index 3 is stable because 7 is the average of 8 and 6.
So the total number of stable readings is 2.

Example 2:
Input: readings = [5, 5, 5, 5]
Output: 2

Explanation:
- Index 1 is stable because 5 is the average of 5 and 5.
- Index 2 is also stable for the same reason.
There are 2 stable readings in total.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the array exactly once, checking each valid middle index one time.

    Space Complexity: O(1)
    - We only use a few integer variables for counting and indexing.
    - No extra array, list, or other data structure is created.
    */
    public int CountStableReadings(int[] readings)
    {
        // Step 1:
        // First, handle the smallest possible arrays.
        //
        // Why this is necessary:
        // A reading can only be "stable" if it has both:
        // - a left neighbor
        // - a right neighbor
        //
        // That means only indices from 1 to readings.Length - 2 can possibly qualify.
        // If the array has fewer than 3 elements, there is no index with two neighbors,
        // so the answer must be 0.
        if (readings == null || readings.Length < 3)
        {
            return 0;
        }

        // Step 2:
        // Create a counter to store how many stable readings we find.
        //
        // Why this is necessary:
        // We need to return the total number of valid indices after checking the array.
        int stableCount = 0;

        // Step 3:
        // Loop through every index that has both neighbors.
        //
        // Valid middle positions are:
        // - start at 1, because index 0 has no left neighbor
        // - end at readings.Length - 2, because the last index has no right neighbor
        //
        // Why this range is necessary:
        // The condition for stability depends on readings[i - 1] and readings[i + 1],
        // so we must avoid going out of bounds.
        for (int i = 1; i < readings.Length - 1; i++)
        {
            // Step 4:
            // Read the three relevant values:
            // - left neighbor
            // - current reading
            // - right neighbor
            //
            // Why this is helpful:
            // Storing them in named variables makes the logic easier to read,
            // especially for beginners learning array problems.
            int left = readings[i - 1];
            int current = readings[i];
            int right = readings[i + 1];

            // Step 5:
            // Check whether the current reading is exactly the average of its neighbors.
            //
            // Mathematical definition:
            // current == (left + right) / 2
            //
            // To avoid any issues with integer division, we rewrite it as:
            // current * 2 == left + right
            //
            // Why this form is better:
            // - It avoids rounding problems.
            // - It directly checks exact equality.
            // - It is the condition given in the problem statement.
            if (current * 2 == left + right)
            {
                // Step 6:
                // If the condition is true, this index is stable.
                // Increase the count by 1.
                stableCount++;
            }

            // If the condition is false, we do nothing and continue to the next index.
        }

        // Step 7:
        // After checking all valid middle indices, return the total count.
        return stableCount;
    }
}

// Demo code:
// Create a solution object, run the provided examples, and print the results.

// Example 1:
// readings = [4, 6, 8, 7, 6]
// Check manually:
// i = 1 -> 6 * 2 = 12, 4 + 8 = 12 -> stable
// i = 2 -> 8 * 2 = 16, 6 + 7 = 13 -> not stable
// i = 3 -> 7 * 2 = 14, 8 + 6 = 14 -> stable
// Total = 2
var solution = new Solution();

int[] readings1 = { 4, 6, 8, 7, 6 };
int result1 = solution.CountStableReadings(readings1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// readings = [5, 5, 5, 5]
// Check manually:
// i = 1 -> 5 * 2 = 10, 5 + 5 = 10 -> stable
// i = 2 -> 5 * 2 = 10, 5 + 5 = 10 -> stable
// Total = 2
int[] readings2 = { 5, 5, 5, 5 };
int result2 = solution.CountStableReadings(readings2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo:
// Fewer than 3 elements means no stable reading is possible.
int[] readings3 = { 10, 20 };
int result3 = solution.CountStableReadings(readings3);
Console.WriteLine($"Additional Example Result: {result3}");