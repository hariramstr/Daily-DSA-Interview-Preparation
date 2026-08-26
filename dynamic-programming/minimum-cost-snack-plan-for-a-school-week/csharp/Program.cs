/*
Title: Minimum Cost Snack Plan for a School Week

Problem Description:
A school cafeteria sells snack passes for the next n days. On day i, a student may or may not want to buy a snack.
You are given an integer array days where each value is a day number on which the student wants a snack, in strictly
increasing order.

The cafeteria offers exactly three pass types:
- a 1-day pass
- a 3-day pass
- a 7-day pass

A pass covers the day it is bought and the following consecutive days in its duration.
For example, if a 3-day pass is bought on day 5, it covers days 5, 6, and 7.

You are also given an integer array costs of length 3, where:
- costs[0] = price of the 1-day pass
- costs[1] = price of the 3-day pass
- costs[2] = price of the 7-day pass

Return the minimum total cost needed to cover every day in days.

Why Dynamic Programming:
The cheapest way to cover all required snack days depends on earlier decisions.
At each required day, we can choose among three pass types, and each choice affects which future days are already covered.
So we compute the best answer for smaller prefixes of the problem and build up to the full answer.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Explanation of complexity:
    - n is the number of required snack days.
    - We compute one DP value for each required day.
    - We also move two pointers forward across the array only once each in total,
      so the total pointer movement is linear.
    - Therefore, the full algorithm runs in linear time.
    */
    public int MinCostSnackPlan(int[] days, int[] costs)
    {
        // If there are no days, no cost is needed.
        // The problem guarantees at least one day, but this guard makes the method safer.
        if (days == null || days.Length == 0)
        {
            return 0;
        }

        // n is the number of snack days we must cover.
        int n = days.Length;

        // dp[i] will store the minimum cost needed to cover the first i required snack days.
        //
        // Important meaning:
        // - dp[0] = 0 means covering zero snack days costs nothing.
        // - dp[1] means minimum cost to cover days[0]
        // - dp[2] means minimum cost to cover days[0..1]
        // - ...
        // - dp[n] means minimum cost to cover all required snack days
        //
        // We use size n + 1 so that dp[0] can represent the empty prefix.
        int[] dp = new int[n + 1];

        // These two pointers help us efficiently find how far back a 3-day pass
        // or a 7-day pass would cover when purchased for the current required day.
        //
        // Pointer j3:
        //   For current day days[i - 1], we want the first index whose day is still covered
        //   by a 3-day pass ending at/starting from the current required day's purchase logic.
        //   A 3-day pass bought on day X covers X, X+1, X+2.
        //   If we decide to use a 3-day pass to cover current required day D,
        //   then it can cover all required days >= D - 2.
        //
        // Pointer j7:
        //   Similarly, a 7-day pass can cover all required days >= D - 6.
        //
        // These pointers only move forward, never backward, which keeps the algorithm O(n).
        int j3 = 0;
        int j7 = 0;

        // We process required snack days from left to right.
        // At step i, we compute dp[i], meaning the minimum cost to cover the first i required days.
        for (int i = 1; i <= n; i++)
        {
            // currentDay is the actual calendar day number of the i-th required snack day.
            int currentDay = days[i - 1];

            // -----------------------------
            // Option 1: Buy a 1-day pass
            // -----------------------------
            //
            // A 1-day pass bought for currentDay covers only currentDay.
            // So we must already have covered the first i - 1 required days optimally,
            // and then add the cost of one 1-day pass.
            int costWith1DayPass = dp[i - 1] + costs[0];

            // ---------------------------------------------------------
            // Move j3 so it points to the first day covered by a 3-day pass
            // ---------------------------------------------------------
            //
            // A 3-day pass that covers currentDay can cover days:
            // currentDay - 2, currentDay - 1, currentDay
            //
            // So any required snack day strictly less than currentDay - 2
            // is NOT covered by this pass and must have been covered earlier.
            //
            // We advance j3 until days[j3] is within the covered range.
            while (j3 < n && days[j3] < currentDay - 2)
            {
                j3++;
            }

            // Now j3 is the first required day index that WOULD be covered by a 3-day pass
            // used to cover currentDay.
            //
            // Therefore:
            // - dp[j3] is the minimum cost to cover all required days before index j3
            // - then one 3-day pass covers required days from j3 through i - 1 (and maybe more calendar days too)
            int costWith3DayPass = dp[j3] + costs[1];

            // ---------------------------------------------------------
            // Move j7 so it points to the first day covered by a 7-day pass
            // ---------------------------------------------------------
            //
            // A 7-day pass that covers currentDay can cover days:
            // currentDay - 6 through currentDay
            //
            // So any required snack day strictly less than currentDay - 6
            // is NOT covered by this pass and must have been covered earlier.
            while (j7 < n && days[j7] < currentDay - 6)
            {
                j7++;
            }

            // Now j7 is the first required day index that WOULD be covered by a 7-day pass.
            //
            // Therefore:
            // - dp[j7] covers all required days before that
            // - one 7-day pass covers required days from j7 through i - 1
            int costWith7DayPass = dp[j7] + costs[2];

            // ---------------------------------------------------------
            // Choose the cheapest of the three valid choices
            // ---------------------------------------------------------
            //
            // This is the core dynamic programming transition:
            // the best way to cover the first i required days is the minimum among:
            // 1) best way to cover first i-1 days + 1-day pass
            // 2) best way to cover all days before the 3-day coverage window + 3-day pass
            // 3) best way to cover all days before the 7-day coverage window + 7-day pass
            dp[i] = Math.Min(costWith1DayPass, Math.Min(costWith3DayPass, costWith7DayPass));
        }

        // dp[n] is the minimum cost to cover all required snack days.
        return dp[n];
    }
}

// ---------------------------------------------------------
// Demo code
// ---------------------------------------------------------

var solution = new Solution();

// Example 1
int[] days1 = { 1, 2, 4, 5, 6 };
int[] costs1 = { 3, 7, 12 };
int result1 = solution.MinCostSnackPlan(days1, costs1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
int[] days2 = { 2, 3, 8, 9, 10, 14 };
int[] costs2 = { 2, 5, 9 };
int result2 = solution.MinCostSnackPlan(days2, costs2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional small demo
int[] days3 = { 1, 4, 6, 7, 8, 20 };
int[] costs3 = { 2, 7, 15 };
int result3 = solution.MinCostSnackPlan(days3, costs3);
Console.WriteLine($"Additional Demo Result: {result3}");