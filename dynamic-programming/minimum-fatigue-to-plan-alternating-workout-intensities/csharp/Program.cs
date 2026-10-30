/*
Title: Minimum Fatigue to Plan Alternating Workout Intensities
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
You are given a list of daily workout sessions. For day i, you may perform the session in one of three intensity modes: low, medium, or high.
The fatigue cost of choosing each mode on that day is given by costs[i][0], costs[i][1], and costs[i][2] respectively.

Rule:
You may not choose the same intensity mode on two consecutive days.

Goal:
Compute the minimum total fatigue needed to complete all sessions while following this rule.

Why Dynamic Programming?
The best choice for the current day depends on what was chosen on the previous day.
So, for each day and each possible intensity, we keep track of the minimum total fatigue up to that point.

Important note about Example 1:
The narrative in the prompt contains contradictions, but the final stated output is 8.
For costs = [[5,3,4],[6,2,7],[3,8,1]]:
- Day 0 low(5), day 1 medium(2), day 2 high(1) => total 8
This is valid and is the true minimum.

Constraints:
- 1 <= n == costs.length <= 100000
- costs[i].length == 3
- 1 <= costs[i][j] <= 10^4
- The answer fits in a 32-bit signed integer.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We process each day exactly once.
    - For each day, we compute 3 values (low, medium, high), each in constant time.

    Space Complexity: O(1)
    - We do not build a full DP table.
    - We only keep the previous day's 3 DP values and compute the current day's 3 values.
    */
    public int MinFatigue(int[][] costs)
    {
        // Defensive check:
        // If the input is null or empty, there are no workout days,
        // so the total fatigue is 0.
        // The problem constraints guarantee at least 1 day, but this makes the method safer.
        if (costs == null || costs.Length == 0)
        {
            return 0;
        }

        // -----------------------------
        // STEP 1: Initialize DP values for day 0
        // -----------------------------
        // We define:
        // prevLow    = minimum total fatigue up to the previous day if that previous day used LOW intensity
        // prevMedium = minimum total fatigue up to the previous day if that previous day used MEDIUM intensity
        // prevHigh   = minimum total fatigue up to the previous day if that previous day used HIGH intensity
        //
        // On day 0, there is no previous day, so the minimum total fatigue for each choice
        // is simply the cost of choosing that intensity on day 0.
        int prevLow = costs[0][0];
        int prevMedium = costs[0][1];
        int prevHigh = costs[0][2];

        // -----------------------------
        // STEP 2: Process each remaining day
        // -----------------------------
        // For each day i, we compute the best total fatigue if day i ends with:
        // - LOW
        // - MEDIUM
        // - HIGH
        //
        // Because we cannot repeat the same intensity on consecutive days:
        // - If today is LOW, yesterday must have been MEDIUM or HIGH
        // - If today is MEDIUM, yesterday must have been LOW or HIGH
        // - If today is HIGH, yesterday must have been LOW or MEDIUM
        for (int i = 1; i < costs.Length; i++)
        {
            // -----------------------------
            // Compute current LOW state
            // -----------------------------
            // If we choose LOW today, yesterday cannot also be LOW.
            // So we take the smaller of:
            // - previous total ending in MEDIUM
            // - previous total ending in HIGH
            //
            // Then we add today's LOW cost.
            int currentLow = costs[i][0] + Math.Min(prevMedium, prevHigh);

            // -----------------------------
            // Compute current MEDIUM state
            // -----------------------------
            // If we choose MEDIUM today, yesterday cannot be MEDIUM.
            // So we take the smaller of:
            // - previous total ending in LOW
            // - previous total ending in HIGH
            //
            // Then we add today's MEDIUM cost.
            int currentMedium = costs[i][1] + Math.Min(prevLow, prevHigh);

            // -----------------------------
            // Compute current HIGH state
            // -----------------------------
            // If we choose HIGH today, yesterday cannot be HIGH.
            // So we take the smaller of:
            // - previous total ending in LOW
            // - previous total ending in MEDIUM
            //
            // Then we add today's HIGH cost.
            int currentHigh = costs[i][2] + Math.Min(prevLow, prevMedium);

            // -----------------------------
            // STEP 3: Move current values into previous values
            // -----------------------------
            // This is the key O(1) space optimization.
            // We do not need the full history of all days.
            // To compute day i+1, we only need the results from day i.
            prevLow = currentLow;
            prevMedium = currentMedium;
            prevHigh = currentHigh;
        }

        // -----------------------------
        // STEP 4: Final answer
        // -----------------------------
        // After processing all days, the answer is the smallest total fatigue among
        // the three possibilities for the last day.
        return Math.Min(prevLow, Math.Min(prevMedium, prevHigh));
    }
}

// --------------------------------------------------
// Demo code
// --------------------------------------------------

var solution = new Solution();

// Example 1
int[][] costs1 =
{
    new[] { 5, 3, 4 },
    new[] { 6, 2, 7 },
    new[] { 3, 8, 1 }
};

int result1 = solution.MinFatigue(costs1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 8

// Example 2
int[][] costs2 =
{
    new[] { 1, 10, 100 },
    new[] { 10, 1, 100 },
    new[] { 100, 10, 1 },
    new[] { 1, 100, 10 }
};

int result2 = solution.MinFatigue(costs2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional small test: single day
int[][] costs3 =
{
    new[] { 7, 2, 9 }
};

int result3 = solution.MinFatigue(costs3);
Console.WriteLine("Single Day Result: " + result3); // Expected: 2

// Additional test: alternating best choices
int[][] costs4 =
{
    new[] { 2, 1, 3 },
    new[] { 3, 2, 1 },
    new[] { 1, 3, 2 },
    new[] { 2, 1, 3 }
};

int result4 = solution.MinFatigue(costs4);
Console.WriteLine("Additional Test Result: " + result4);