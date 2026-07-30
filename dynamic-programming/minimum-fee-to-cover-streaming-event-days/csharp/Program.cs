/*
Title: Minimum Fee to Cover Streaming Event Days
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
A media platform plans to broadcast a set of live events on specific calendar days. To handle traffic, the platform can purchase server reservation passes of different durations. A 1-day pass costs cost1, a 7-day pass costs cost7, and a 30-day pass costs cost30. A pass purchased for day d covers day d and the next consecutive days within its duration. For example, a 7-day pass bought on day 10 covers days 10 through 16 inclusive.

You are given a strictly increasing integer array days, where days[i] is a day on which at least one live event must be supported, and an array costs of length 3 where costs = [cost1, cost7, cost30].

Return the minimum total fee required to cover every event day in days.

You may buy any number of passes, and passes may overlap, but overlapping coverage does not provide any extra benefit beyond covering the required days. The goal is to choose passes so that every day in days is covered at minimum total cost.

Constraints:
- 1 <= days.length <= 365
- 1 <= days[i] <= 365
- days is strictly increasing
- 1 <= costs[i] <= 1000

Example 1:
Input: days = [1,4,6,7,8,20], costs = [2,7,15]
Output: 11

Example 2:
Input: days = [2,3,4,5,6,7,8,9,15,16,17,40], costs = [3,8,20]
Output: 19
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(lastDay), where lastDay is the final travel/event day and lastDay <= 365.
    Space Complexity: O(lastDay) for the dynamic programming array.

    Beginner-friendly idea:
    We compute the minimum cost needed for every calendar day from day 1 up to the last required event day.

    Let dp[d] mean:
    "the minimum total fee needed to cover all required event days from day 1 through day d"

    For each day:
    - If it is NOT an event day, then we do not need to buy anything new.
      So dp[d] = dp[d - 1]
    - If it IS an event day, then we must make sure this day is covered.
      We have exactly 3 choices:
        1) Buy a 1-day pass ending/used for this day
        2) Buy a 7-day pass that covers this day
        3) Buy a 30-day pass that covers this day

      Then:
      dp[d] = min(
          dp[d - 1] + cost1,
          dp[max(0, d - 7)] + cost7,
          dp[max(0, d - 30)] + cost30
      )

    Why this works:
    - If we buy a 1-day pass for day d, then all previous required days up to d-1 must already be optimally covered.
    - If we buy a 7-day pass covering day d, then it covers days d-6 through d.
      So we only need the optimal cost up to day d-7 before adding the 7-day pass.
    - Similarly for the 30-day pass.
    */
    public int MincostTickets(int[] days, int[] costs)
    {
        // The input array "days" is strictly increasing.
        // The final event day tells us how far our DP needs to go.
        // There is no reason to compute beyond the last required day,
        // because no future day needs coverage.
        int lastDay = days[^1];

        // This boolean array lets us quickly answer:
        // "Is this calendar day an event day that must be covered?"
        //
        // Why use a boolean array?
        // - Days are limited to 365, so this is very small and efficient.
        // - It gives O(1) lookup for each day during the DP loop.
        bool[] isEventDay = new bool[lastDay + 1];

        // Mark every required event day.
        foreach (int day in days)
        {
            isEventDay[day] = true;
        }

        // dp[d] = minimum fee needed to cover all required event days
        // from day 1 through day d.
        //
        // We allocate lastDay + 1 so that dp[0] exists.
        // dp[0] means: covering "no days at all" costs 0.
        int[] dp = new int[lastDay + 1];

        // Process every calendar day in order.
        // This left-to-right order is important because dp[d]
        // depends on smaller day values such as dp[d - 1], dp[d - 7], dp[d - 30].
        for (int day = 1; day <= lastDay; day++)
        {
            // Case 1: This is NOT an event day.
            //
            // Since there is no event to cover today, we do not need to buy any pass.
            // Therefore, the minimum cost up to today is exactly the same as yesterday.
            if (!isEventDay[day])
            {
                dp[day] = dp[day - 1];
                continue;
            }

            // Case 2: This IS an event day.
            //
            // We must ensure this day is covered.
            // We consider all 3 pass types and choose the cheapest total.

            // Option A: Buy a 1-day pass that covers only this day.
            //
            // Then all required days before today must already be covered optimally,
            // which is dp[day - 1].
            int costWith1DayPass = dp[day - 1] + costs[0];

            // Option B: Buy a 7-day pass that covers this day.
            //
            // A 7-day pass covering "day" also covers the previous 6 days:
            // [day - 6, ..., day]
            //
            // So the last day NOT covered by this pass is day - 7.
            // We need the optimal cost up to that point, then add the 7-day pass cost.
            //
            // If day < 7, then the pass reaches all the way back before day 1,
            // so the previous cost should be treated as 0 using Math.Max(0, day - 7).
            int startBefore7DayCoverage = Math.Max(0, day - 7);
            int costWith7DayPass = dp[startBefore7DayCoverage] + costs[1];

            // Option C: Buy a 30-day pass that covers this day.
            //
            // Similarly, a 30-day pass covering "day" covers:
            // [day - 29, ..., day]
            //
            // So the last day not covered is day - 30.
            int startBefore30DayCoverage = Math.Max(0, day - 30);
            int costWith30DayPass = dp[startBefore30DayCoverage] + costs[2];

            // Choose the cheapest of the three valid ways to cover this event day.
            dp[day] = Math.Min(costWith1DayPass, Math.Min(costWith7DayPass, costWith30DayPass));
        }

        // The answer is the minimum cost to cover all required event days
        // up to the final event day.
        return dp[lastDay];
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] days1 = { 1, 4, 6, 7, 8, 20 };
int[] costs1 = { 2, 7, 15 };
int result1 = solution.MincostTickets(days1, costs1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
int[] days2 = { 2, 3, 4, 5, 6, 7, 8, 9, 15, 16, 17, 40 };
int[] costs2 = { 3, 8, 20 };
int result2 = solution.MincostTickets(days2, costs2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick demo
int[] days3 = { 1, 2, 3, 31 };
int[] costs3 = { 2, 7, 15 };
int result3 = solution.MincostTickets(days3, costs3);
Console.WriteLine($"Additional Demo Result: {result3}");