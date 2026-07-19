/*
Title: Maximum Score from Choosing One Value Per Day Range

Problem Description:
You are given an integer array values where values[i] represents the score available on day i.
You must build a schedule by choosing a set of days to collect score, subject to one rule:
if you choose day i, then you cannot choose either adjacent day i - 1 or i + 1.
In other words, any two chosen days must be at least 2 indices apart.

Your task is to return the maximum total score you can collect.

This models a realistic planning problem where collecting score on one day requires a cooldown
on neighboring days. The array may contain positive, zero, or negative values. You are allowed
to skip any day, including all days if that gives a better result.

Write a function that computes the best possible total score.

Constraints:
- 1 <= values.length <= 100000
- -10000 <= values[i] <= 10000
- The answer fits in a 32-bit signed integer.

Examples:
1) values = [4, 2, 7, 9, 3]
   Best valid choice is days 0, 2, and 4 => 4 + 7 + 3 = 14
   Output: 14

2) values = [-5, -1, -8]
   Best choice is to skip all days
   Output: 0
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Explanation:
    We process the array once from left to right.
    At each position, we decide between:
    1. Skipping the current day
    2. Taking the current day, which means we must add its value to the best answer from two days ago

    Because each decision depends only on the previous two states, we do not need a full DP array.
    */
    public int MaxScore(int[] values)
    {
        // This variable stores the best answer for the subarray ending at index i - 2.
        // In classic dynamic programming terms, this represents dp[i - 2].
        // We need this because if we choose the current day i, we are not allowed to choose day i - 1,
        // so the best compatible total comes from two positions back.
        int prevTwo = 0;

        // This variable stores the best answer for the subarray ending at index i - 1.
        // In classic dynamic programming terms, this represents dp[i - 1].
        // We need this because one option at every step is to skip the current day,
        // in which case the answer simply stays whatever the best answer was up to the previous day.
        int prevOne = 0;

        // We now scan through each day exactly once.
        // For each day, we compute the best possible total score considering all days up to this one.
        for (int i = 0; i < values.Length; i++)
        {
            // Option 1: Skip the current day.
            // If we skip day i, then the best total remains the same as the best total up to day i - 1.
            int skipCurrent = prevOne;

            // Option 2: Take the current day.
            // If we take day i, then we are forbidden from taking day i - 1.
            // Therefore, the best total we can combine with values[i] is the best total up to day i - 2.
            int takeCurrent = prevTwo + values[i];

            // The best answer for the current position is the better of:
            // - skipping the day
            // - taking the day
            //
            // This also naturally handles negative values:
            // if values[i] is negative and hurts the total, skipping will be better.
            // Since prevOne starts at 0, the algorithm also correctly allows skipping all days.
            int currentBest = Math.Max(skipCurrent, takeCurrent);

            // Move the rolling window forward:
            // - what used to be dp[i - 1] becomes dp[i - 2] for the next iteration
            // - what we just computed becomes dp[i - 1] for the next iteration
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // After processing all days, prevOne holds the best total score for the entire array.
        return prevOne;
    }
}

// Demo code
var solution = new Solution();

int[] values1 = { 4, 2, 7, 9, 3 };
int result1 = solution.MaxScore(values1);
Console.WriteLine($"Input: [{string.Join(", ", values1)}]");
Console.WriteLine($"Maximum score: {result1}");
Console.WriteLine("Expected: 14");
Console.WriteLine();

int[] values2 = { -5, -1, -8 };
int result2 = solution.MaxScore(values2);
Console.WriteLine($"Input: [{string.Join(", ", values2)}]");
Console.WriteLine($"Maximum score: {result2}");
Console.WriteLine("Expected: 0");
Console.WriteLine();

int[] values3 = { 2, 1, 1, 2 };
int result3 = solution.MaxScore(values3);
Console.WriteLine($"Input: [{string.Join(", ", values3)}]");
Console.WriteLine($"Maximum score: {result3}");
Console.WriteLine("Expected: 4");
Console.WriteLine();

int[] values4 = { 5 };
int result4 = solution.MaxScore(values4);
Console.WriteLine($"Input: [{string.Join(", ", values4)}]");
Console.WriteLine($"Maximum score: {result4}");
Console.WriteLine("Expected: 5");
Console.WriteLine();

int[] values5 = { 0, 0, 0 };
int result5 = solution.MaxScore(values5);
Console.WriteLine($"Input: [{string.Join(", ", values5)}]");
Console.WriteLine($"Maximum score: {result5}");
Console.WriteLine("Expected: 0");