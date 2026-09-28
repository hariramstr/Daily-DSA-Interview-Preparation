/*
Title: Find the First Day With Consecutive Stock Refill
Difficulty: Easy
Topic: Arrays

Problem Description:
A store tracks the number of units added to inventory each day in an integer array refills,
where refills[i] is the number of items restocked on day i.

A manager wants to know the earliest day when the store begins a streak of at least k
consecutive days with a positive refill.

In other words, find the smallest index i such that:
refills[i], refills[i + 1], ..., refills[i + k - 1]
are all greater than 0.

If no such streak exists, return -1.

Important notes:
- A refill value greater than 0 means that day contributes to a positive streak.
- A refill value of 0 breaks the streak.
- If k is 1, then any day with a positive refill is already a valid answer.
- If the array length is smaller than k, the answer must be -1.

Constraints:
- 1 <= refills.length <= 100000
- 0 <= refills[i] <= 1000000
- 1 <= k <= 100000

Example 1:
Input: refills = [0, 3, 2, 5, 0, 4], k = 3
Output: 1
Explanation:
The first streak of 3 consecutive positive refill days starts at index 1
because values [3, 2, 5] are all greater than 0.

Example 2:
Input: refills = [1, 0, 2, 3, 0, 1], k = 2
Output: 2
Explanation:
The first valid streak of length 2 is [2, 3], which starts at index 2.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the array exactly once.
    - n is the number of days in the refills array.

    Space Complexity: O(1)
    - We only use a few integer variables.
    - No extra arrays, lists, or other data structures are needed.

    Beginner-friendly idea:
    We keep track of how many consecutive positive refill days we have seen so far.
    - If the current day has a positive refill, we increase the streak length.
    - If the current day has 0, the streak is broken, so we reset the count to 0.
    - As soon as the streak length becomes k, we know the first valid starting index:
      currentIndex - k + 1
    */
    public int FirstDayWithConsecutiveRefill(int[] refills, int k)
    {
        // Step 1:
        // Handle a very important edge case early.
        //
        // If the array is shorter than k, it is impossible to have k consecutive days.
        // Example:
        // refills = [4, 5], k = 3
        // There are only 2 days total, so a streak of length 3 cannot exist.
        if (refills == null || refills.Length < k)
        {
            return -1;
        }

        // Step 2:
        // This variable will store the length of the current streak of consecutive
        // positive refill days.
        //
        // Why do we need it?
        // Because the problem asks for k consecutive days where each value is > 0.
        // So we need to count how many positive days in a row we have seen.
        int consecutivePositiveDays = 0;

        // Step 3:
        // Scan the array from left to right.
        //
        // Why left to right?
        // Because we want the earliest starting index.
        // The first time we find a valid streak of length k, that must be the answer.
        for (int i = 0; i < refills.Length; i++)
        {
            // Step 4:
            // Check whether the current day has a positive refill.
            //
            // If refills[i] > 0:
            // - This day continues a positive streak.
            // - So we increase the streak counter by 1.
            if (refills[i] > 0)
            {
                consecutivePositiveDays++;
            }
            else
            {
                // Step 5:
                // If refills[i] == 0, the streak is broken.
                //
                // Why reset to 0?
                // Because consecutive means there can be no gap and no zero in between.
                // Once we hit a zero, any previous streak cannot continue past this point.
                consecutivePositiveDays = 0;
            }

            // Step 6:
            // After updating the streak count for the current day,
            // check whether we have reached at least k consecutive positive days.
            //
            // The first time this happens, we can immediately return the starting index.
            //
            // Why is the starting index equal to i - k + 1?
            // Suppose:
            // - i is the current ending index of the streak
            // - the streak length is k
            // Then the streak starts k - 1 positions earlier:
            // start = i - (k - 1) = i - k + 1
            //
            // Example:
            // refills = [0, 3, 2, 5, 0, 4], k = 3
            // At i = 3, we have seen [3, 2, 5], so consecutivePositiveDays = 3
            // start = 3 - 3 + 1 = 1
            if (consecutivePositiveDays >= k)
            {
                return i - k + 1;
            }
        }

        // Step 7:
        // If we finish scanning the entire array and never find a streak of length k,
        // then no valid answer exists.
        return -1;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1:
// refills = [0, 3, 2, 5, 0, 4], k = 3
// Positive streaks:
// index 0 -> 0, streak reset
// index 1 -> 3, streak = 1
// index 2 -> 2, streak = 2
// index 3 -> 5, streak = 3 => answer is 3 - 3 + 1 = 1
int[] refills1 = { 0, 3, 2, 5, 0, 4 };
int k1 = 3;
int result1 = solution.FirstDayWithConsecutiveRefill(refills1, k1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// refills = [1, 0, 2, 3, 0, 1], k = 2
// index 0 -> 1, streak = 1
// index 1 -> 0, streak reset
// index 2 -> 2, streak = 1
// index 3 -> 3, streak = 2 => answer is 3 - 2 + 1 = 2
int[] refills2 = { 1, 0, 2, 3, 0, 1 };
int k2 = 2;
int result2 = solution.FirstDayWithConsecutiveRefill(refills2, k2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo 1:
// k = 1 means any positive day is enough.
// The first positive value is at index 1.
int[] refills3 = { 0, 7, 0, 2 };
int k3 = 1;
int result3 = solution.FirstDayWithConsecutiveRefill(refills3, k3);
Console.WriteLine($"Additional Demo 1 Result: {result3}");

// Additional demo 2:
// Array length is smaller than k, so answer must be -1.
int[] refills4 = { 5, 6 };
int k4 = 3;
int result4 = solution.FirstDayWithConsecutiveRefill(refills4, k4);
Console.WriteLine($"Additional Demo 2 Result: {result4}");

// Additional demo 3:
// No streak of 2 consecutive positive days exists.
int[] refills5 = { 1, 0, 1, 0, 1 };
int k5 = 2;
int result5 = solution.FirstDayWithConsecutiveRefill(refills5, k5);
Console.WriteLine($"Additional Demo 3 Result: {result5}");