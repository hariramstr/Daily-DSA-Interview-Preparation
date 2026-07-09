/*
Title: Count Days with a New Highest Step Total

Problem Description:
You are given an integer array `steps` where `steps[i]` represents the number of steps a user walked on day `i`.
A day is called a record day if its step count is strictly greater than every previous day's step count.
The first day is always considered a record day because there are no earlier days to compare against.

Your task is to return the total number of record days in the array.

This problem tests your ability to scan an array while maintaining running state.
A correct solution should track the highest step count seen so far and count how many times a new maximum appears.

Constraints:
- 1 <= steps.length <= 100000
- 0 <= steps[i] <= 1000000

Example 1:
Input: steps = [3000, 4500, 4200, 5000, 5000, 6200]
Output: 4
Explanation:
- Day 0: 3000 -> record day (first day)
- Day 1: 4500 -> greater than 3000, so record day
- Day 2: 4200 -> not greater than 4500, so not a record day
- Day 3: 5000 -> greater than 4500, so record day
- Day 4: 5000 -> equal to current maximum 5000, not strictly greater, so not a record day
- Day 5: 6200 -> greater than 5000, so record day
Total = 4

Example 2:
Input: steps = [8000, 7000, 7000, 6500]
Output: 1
Explanation:
- Day 0: 8000 -> record day
- Day 1: 7000 -> not greater than 8000
- Day 2: 7000 -> not greater than 8000
- Day 3: 6500 -> not greater than 8000
Total = 1
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We visit each day exactly once, so the running time grows linearly with the number of days.

    Space Complexity: O(1)
    - We only use a few extra variables (`recordDays` and `highestSoFar`), regardless of input size.
    */
    public int CountRecordDays(int[] steps)
    {
        // This variable will store how many record days we have found so far.
        // We start at 0 because we have not processed any days yet.
        int recordDays = 0;

        // This variable keeps track of the highest step count we have seen while scanning the array.
        // We initialize it to -1 so that the first day's step count will always be greater than it.
        // This works because the problem guarantees step counts are at least 0.
        int highestSoFar = -1;

        // We loop through the array from left to right.
        // This is the natural order because a day can only be compared against previous days.
        for (int i = 0; i < steps.Length; i++)
        {
            // Read the current day's step count into a local variable.
            // This makes the code easier to read and lets us talk clearly about "today's value".
            int todaySteps = steps[i];

            // We now check whether today's step count is a new record.
            // A day is a record day only if it is STRICTLY GREATER than every previous day.
            // Since `highestSoFar` stores the maximum of all earlier days we have processed,
            // it is enough to compare today's value against that one number.
            if (todaySteps > highestSoFar)
            {
                // If we get here, today sets a new personal best.
                // So we increase the count of record days.
                recordDays++;

                // We must also update `highestSoFar` to today's value.
                // This is necessary so future days are compared against the newest maximum.
                highestSoFar = todaySteps;
            }

            // If today's value is not greater than `highestSoFar`,
            // then today is not a record day.
            // In that case, we do nothing:
            // - we do not increase `recordDays`
            // - we do not change `highestSoFar`
            //
            // This correctly handles:
            // - smaller values
            // - equal values (equal is NOT a record because the rule says strictly greater)
        }

        // After scanning all days, `recordDays` contains the final answer.
        return recordDays;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the problem description
int[] steps1 = { 3000, 4500, 4200, 5000, 5000, 6200 };
int result1 = solution.CountRecordDays(steps1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2 from the problem description
int[] steps2 = { 8000, 7000, 7000, 6500 };
int result2 = solution.CountRecordDays(steps2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 1

// Additional quick checks
int[] steps3 = { 0 };
int result3 = solution.CountRecordDays(steps3);
Console.WriteLine("Single Day Result: " + result3); // Expected: 1

int[] steps4 = { 1000, 2000, 3000, 4000 };
int result4 = solution.CountRecordDays(steps4);
Console.WriteLine("Strictly Increasing Result: " + result4); // Expected: 4

int[] steps5 = { 5000, 5000, 5000 };
int result5 = solution.CountRecordDays(steps5);
Console.WriteLine("All Equal Result: " + result5); // Expected: 1