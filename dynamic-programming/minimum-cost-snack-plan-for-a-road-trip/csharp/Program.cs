/*
Title: Minimum Cost Snack Plan for a Road Trip

Problem Description:
You are planning snacks for a road trip that lasts n days. On each day, you must buy exactly one snack pack.
There are three snack types: fruit, sandwich, and trail mix. The cost of buying each type can be different on each day.

However, to avoid getting bored, you are not allowed to buy the same snack type on two consecutive days.

You are given a 2D integer array costs of size n x 3, where:
- costs[i][0] is the cost of buying fruit on day i
- costs[i][1] is the cost of buying a sandwich on day i
- costs[i][2] is the cost of buying trail mix on day i

Return the minimum total cost to buy snacks for all n days while following the rule that
the same snack type cannot be chosen on consecutive days.

Dynamic Programming Idea:
For each day, we track the minimum total cost if we end that day by choosing:
- fruit
- sandwich
- trail mix

Because we cannot choose the same snack on consecutive days:
- If we choose fruit today, yesterday must have been sandwich or trail mix
- If we choose sandwich today, yesterday must have been fruit or trail mix
- If we choose trail mix today, yesterday must have been fruit or sandwich

This leads to a very small and efficient dynamic programming solution.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We process each day exactly once.
    - For each day, we compute 3 values, and each value uses only constant-time work.

    Space Complexity: O(1)
    - We only keep track of the previous day's 3 minimum costs and the current day's 3 minimum costs.
    - We do not need a full DP table because each day depends only on the previous day.
    */
    public int MinCost(int[][] costs)
    {
        // Defensive check:
        // If the input is null or empty, there are no days to process.
        // The problem constraints guarantee at least 1 day, but this makes the method safer.
        if (costs == null || costs.Length == 0)
        {
            return 0;
        }

        // Day 0 initialization:
        // On the very first day, there is no previous day.
        // So the minimum cost of ending day 0 with each snack is simply the direct cost of that snack on day 0.
        //
        // prevFruit    = minimum total cost up to the previous day if the previous day ended with fruit
        // prevSandwich = minimum total cost up to the previous day if the previous day ended with sandwich
        // prevTrailMix = minimum total cost up to the previous day if the previous day ended with trail mix
        int prevFruit = costs[0][0];
        int prevSandwich = costs[0][1];
        int prevTrailMix = costs[0][2];

        // Process each remaining day starting from day 1.
        // For each day, we compute the best possible total cost for ending that day with each snack type.
        for (int day = 1; day < costs.Length; day++)
        {
            // If we choose fruit today:
            // We are NOT allowed to choose fruit yesterday.
            // Therefore, yesterday must have ended with either sandwich or trail mix.
            //
            // We take the cheaper of those two valid previous states,
            // then add today's fruit cost.
            int currentFruit = costs[day][0] + Math.Min(prevSandwich, prevTrailMix);

            // If we choose sandwich today:
            // Yesterday cannot also be sandwich.
            // So yesterday must have ended with fruit or trail mix.
            //
            // Again, choose the cheaper valid previous state and add today's sandwich cost.
            int currentSandwich = costs[day][1] + Math.Min(prevFruit, prevTrailMix);

            // If we choose trail mix today:
            // Yesterday cannot also be trail mix.
            // So yesterday must have ended with fruit or sandwich.
            //
            // Choose the cheaper valid previous state and add today's trail mix cost.
            int currentTrailMix = costs[day][2] + Math.Min(prevFruit, prevSandwich);

            // Now that we have computed the best totals for the current day,
            // we move these values into the "previous day" variables.
            //
            // This is the key space optimization:
            // We do not store all days in a table because the next day only needs today's results.
            prevFruit = currentFruit;
            prevSandwich = currentSandwich;
            prevTrailMix = currentTrailMix;
        }

        // After processing all days, the answer is the cheapest way to end the final day,
        // regardless of which snack type was chosen on that final day.
        return Math.Min(prevFruit, Math.Min(prevSandwich, prevTrailMix));
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1 from the prompt.
// Note:
// If we compute the true minimum for this input, the result is 8.
// Valid optimal plan:
// Day 0: sandwich = 2
// Day 1: trail mix = 1
// Day 2: sandwich = 5
// Total = 2 + 1 + 5 = 8
//
// The prompt's stated output of 7 is inconsistent with the provided costs.
// The algorithm below correctly computes the minimum valid total cost for the given input.
int[][] costs1 =
{
    new[] { 4, 2, 7 },
    new[] { 3, 6, 1 },
    new[] { 8, 5, 4 }
};

int result1 = solution.MinCost(costs1);
Console.WriteLine("Example 1 Result: " + result1);

// Example 2 from the prompt.
// One optimal plan:
// Day 0: fruit = 1
// Day 1: sandwich = 2
// Day 2: trail mix = 1
// Day 3: sandwich = 3
// Total = 7
int[][] costs2 =
{
    new[] { 1, 10, 3 },
    new[] { 10, 2, 8 },
    new[] { 4, 7, 1 },
    new[] { 6, 3, 9 }
};

int result2 = solution.MinCost(costs2);
Console.WriteLine("Example 2 Result: " + result2);

// Additional small demo to help beginners see a one-day case.
// With only one day, the answer is simply the cheapest snack on that day.
int[][] costs3 =
{
    new[] { 9, 2, 5 }
};

int result3 = solution.MinCost(costs3);
Console.WriteLine("Single Day Demo Result: " + result3);