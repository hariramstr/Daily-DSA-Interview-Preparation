/*
Title: Minimum Cost to Cover a Workweek with Flexible Passes

Problem Description:
A company cafeteria offers prepaid meal passes to employees. On some days of the month, an employee plans to eat at the cafeteria, and they want to spend as little money as possible.

You are given a strictly increasing array days, where days[i] is a calendar day on which the employee will eat at the cafeteria. You are also given three pass types:

- a 1-day pass costing cost1
- a 5-day pass costing cost5
- a 20-day pass costing cost20

A pass bought on day d covers that day and the following consecutive days in its duration.
For example, a 5-day pass bought on day 7 covers days 7 through 11 inclusive.

Return the minimum total cost required to cover every day in days.

This is a planning problem: buying a longer pass earlier may cover several future meal days and reduce the total cost.
The employee may buy multiple passes of any type, and passes may overlap, although overlapping coverage is usually not helpful.

Constraints:
- 1 <= days.length <= 365
- 1 <= days[i] <= 365
-  days is strictly increasing
- 1 <= cost1, cost5, cost20 <= 10^5
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Explanation of complexity:
    - Let n be the number of planned cafeteria days.
    - We compute the answer using dynamic programming from left to right.
    - For each index, we advance two helper pointers at most n times total across the whole algorithm.
    - Because each pointer only moves forward and never backward, the total work is linear.
    */
    public int MinCostToCoverDays(int[] days, int cost1, int cost5, int cost20)
    {
        int n = days.Length;

        // dp[i] will store the minimum cost needed to cover the first i meal days.
        //
        // Important meaning:
        // - dp[0] = 0 means covering zero days costs nothing.
        // - dp[1] means minimum cost to cover days[0]
        // - dp[2] means minimum cost to cover days[0..1]
        // - ...
        // - dp[n] means minimum cost to cover all required days
        //
        // We use size n + 1 so that dp[0] can represent the empty prefix cleanly.
        int[] dp = new int[n + 1];

        // These two pointers help us quickly find:
        // - the first day NOT covered by a 5-day pass ending at the current day
        // - the first day NOT covered by a 20-day pass ending at the current day
        //
        // More precisely:
        // When we are processing days[i - 1], we imagine buying a pass that starts on that day.
        // That pass will cover several future required meal days.
        //
        // However, an equivalent and very common DP view is:
        // "To cover the current required day, what if the last pass we buy is a 1-day, 5-day, or 20-day pass?"
        //
        // Then we need to know how many earlier required days are already covered by that pass.
        int j5 = 0;
        int j20 = 0;

        // We fill dp one required day at a time.
        for (int i = 1; i <= n; i++)
        {
            int currentDay = days[i - 1];

            // ------------------------------------------------------------
            // Option 1: Buy a 1-day pass for the current required day.
            // ------------------------------------------------------------
            //
            // A 1-day pass covers only currentDay.
            // So if we use that as the last purchase, then everything before this day
            // must already be optimally covered by dp[i - 1].
            //
            // Total cost for this option:
            // dp[i - 1] + cost1
            int option1 = dp[i - 1] + cost1;

            // ------------------------------------------------------------
            // Option 2: Buy a 5-day pass that covers the current required day.
            // ------------------------------------------------------------
            //
            // A 5-day pass covering currentDay would cover the calendar interval:
            // [currentDay - 4, currentDay]
            //
            // We need to find the first required meal day that is inside this interval.
            // Every required day before that index is NOT covered by this 5-day pass,
            // so those earlier days must be paid for separately.
            //
            // j5 will become the smallest index such that:
            // days[j5] >= currentDay - 4
            //
            // Then:
            // - required days at indices j5, j5+1, ..., i-1 are covered by this 5-day pass
            // - required days at indices 0, 1, ..., j5-1 must be covered by dp[j5]
            //
            // Why a while loop?
            // Because days is strictly increasing, so once a day is too early to be covered,
            // it will remain too early for all later currentDay values.
            // This makes the pointer move only forward, which is efficient.
            while (j5 < n && days[j5] < currentDay - 4)
            {
                j5++;
            }

            int option5 = dp[j5] + cost5;

            // ------------------------------------------------------------
            // Option 3: Buy a 20-day pass that covers the current required day.
            // ------------------------------------------------------------
            //
            // A 20-day pass covering currentDay would cover:
            // [currentDay - 19, currentDay]
            //
            // We find the first required day inside that interval.
            // All earlier required days are outside the pass and must be covered by dp[j20].
            while (j20 < n && days[j20] < currentDay - 19)
            {
                j20++;
            }

            int option20 = dp[j20] + cost20;

            // ------------------------------------------------------------
            // Choose the cheapest of the three possibilities.
            // ------------------------------------------------------------
            //
            // This is the heart of dynamic programming:
            // We assume smaller subproblems are already solved optimally in dp,
            // then build the optimal answer for the current prefix.
            dp[i] = Math.Min(option1, Math.Min(option5, option20));
        }

        // The final answer is the minimum cost to cover all required meal days.
        return dp[n];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] days1 = { 1, 2, 3, 6, 7, 8, 21 };
int cost1A = 3;
int cost5A = 7;
int cost20A = 18;
int result1 = solution.MinCostToCoverDays(days1, cost1A, cost5A, cost20A);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
int[] days2 = { 4, 5, 9, 10, 11, 30, 31 };
int cost1B = 4;
int cost5B = 9;
int cost20B = 25;
int result2 = solution.MinCostToCoverDays(days2, cost1B, cost5B, cost20B);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional small demo
int[] days3 = { 1, 10, 20, 21, 22 };
int cost1C = 5;
int cost5C = 11;
int cost20C = 30;
int result3 = solution.MinCostToCoverDays(days3, cost1C, cost5C, cost20C);
Console.WriteLine($"Additional Demo Result: {result3}");