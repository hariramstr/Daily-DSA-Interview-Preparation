/*
Title: Count Late Arrivals After the Daily Cutoff
Difficulty: Easy
Topic: Arrays

Problem Description:
You are given an array arrivalTimes where each element represents the arrival time of an employee
in minutes after midnight for a single day. You are also given an integer cutoff, which represents
the latest on-time arrival in minutes after midnight. Your task is to return how many employees
arrived strictly after the cutoff time.

For example, if cutoff = 540, that means employees arriving at 9:00 AM (540 minutes) are still
on time, but anyone arriving at 541 or later is late. The array is not guaranteed to be sorted,
and multiple employees may have the same arrival time.

Write a function that scans the array and counts the number of values greater than cutoff.

Constraints:
- 1 <= arrivalTimes.length <= 100000
- 0 <= arrivalTimes[i] <= 1439
- 0 <= cutoff <= 1439
- The answer fits in a 32-bit integer

Example 1:
Input: arrivalTimes = [525, 541, 540, 600, 539], cutoff = 540
Output: 2
Explanation: The late arrivals are 541 and 600. Arrivals at 525, 540, and 539 are on time.

Example 2:
Input: arrivalTimes = [480, 480, 481, 479], cutoff = 480
Output: 1
Explanation: Only the employee arriving at 481 is late. Employees arriving exactly at 480 are considered on time.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We look at each arrival time exactly once, where n is the number of employees.

    Space Complexity: O(1)
    - We only use a small fixed amount of extra memory: a counter and a loop variable.

    Beginner-friendly idea:
    - We do not need sorting.
    - We do not need any extra data structure like a list, dictionary, or set.
    - We simply scan through the array from left to right.
    - Every time we find a value strictly greater than cutoff, we increase our late counter.
    */
    public int CountLateArrivals(int[] arrivalTimes, int cutoff)
    {
        // This variable will store the total number of employees who are late.
        // We start at 0 because before checking any arrivals, we have not found any late employees yet.
        int lateCount = 0;

        // We use a simple for loop to examine every element in the array one by one.
        // Why this is necessary:
        // - The array is not guaranteed to be sorted.
        // - Because of that, a late arrival could appear anywhere in the array.
        // - The safest and simplest approach is to inspect every value.
        for (int i = 0; i < arrivalTimes.Length; i++)
        {
            // Read the current employee's arrival time from the array.
            // Storing it in a local variable makes the code easier to read and explain.
            int currentArrival = arrivalTimes[i];

            // The problem says an employee is late only if they arrive STRICTLY after the cutoff.
            // That means:
            // - currentArrival > cutoff  => late
            // - currentArrival == cutoff => on time
            // - currentArrival < cutoff  => on time
            //
            // This strict inequality is the most important detail in the problem.
            if (currentArrival > cutoff)
            {
                // We found one late employee, so we increase the counter by 1.
                lateCount++;
            }

            // If the current arrival is not greater than cutoff, we do nothing.
            // That employee is on time, so the count should not change.
        }

        // After checking every employee, lateCount contains the final answer.
        return lateCount;
    }
}

// Demo code:
// We create sample inputs, call the solution method, and print the results.

// Create an instance of the solution class so we can call the algorithm method.
var solution = new Solution();

// Example 1 from the problem description:
// arrivalTimes = [525, 541, 540, 600, 539], cutoff = 540
// Late arrivals are 541 and 600, so the expected answer is 2.
int[] arrivalTimes1 = { 525, 541, 540, 600, 539 };
int cutoff1 = 540;
int result1 = solution.CountLateArrivals(arrivalTimes1, cutoff1);
Console.WriteLine(result1); // Expected: 2

// Example 2 from the problem description:
// arrivalTimes = [480, 480, 481, 479], cutoff = 480
// Only 481 is strictly greater than 480, so the expected answer is 1.
int[] arrivalTimes2 = { 480, 480, 481, 479 };
int cutoff2 = 480;
int result2 = solution.CountLateArrivals(arrivalTimes2, cutoff2);
Console.WriteLine(result2); // Expected: 1

// Additional demo case:
// Everyone is on time because no one arrives after the cutoff.
int[] arrivalTimes3 = { 300, 400, 500 };
int cutoff3 = 500;
int result3 = solution.CountLateArrivals(arrivalTimes3, cutoff3);
Console.WriteLine(result3); // Expected: 0

// Additional demo case:
// Everyone is late because all arrivals are greater than the cutoff.
int[] arrivalTimes4 = { 601, 700, 800 };
int cutoff4 = 600;
int result4 = solution.CountLateArrivals(arrivalTimes4, cutoff4);
Console.WriteLine(result4); // Expected: 3