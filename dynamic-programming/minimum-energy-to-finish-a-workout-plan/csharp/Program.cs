/*
Title: Minimum Energy to Finish a Workout Plan

Problem Description:
You are given a workout plan represented by an integer array energy, where energy[i] is the energy cost of completing exercise i.
You start before the first exercise and want to finish by reaching just beyond the last exercise.
On each move, you may complete either the next 1 exercise or the next 2 exercises.
If you land on an exercise, you must pay its energy cost.

If you skip directly over an exercise by taking a 2-exercise move, you do not pay the cost of the skipped exercise.
Your goal is to find the minimum total energy needed to finish the workout plan.

This is the same dynamic programming pattern as the classic "min cost climbing stairs" problem:
- You may move 1 or 2 steps at a time
- You pay the cost only for the steps you land on
- Reaching just beyond the last index is the finish

Examples:
1) energy = [4, 1, 6, 2]
   One optimal path:
   start -> exercise 1 (pay 1) -> exercise 3 (pay 2) -> finish
   Total = 3

2) energy = [3, 5, 2, 1, 4]
   One optimal path:
   start -> exercise 0 (pay 3) -> exercise 2 (pay 2) -> exercise 3 (pay 1) -> finish
   Total = 6
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We process each exercise exactly once.

    Space Complexity: O(1)
    - We only keep track of the minimum energy for the previous two positions,
      instead of storing a full DP array.

    Beginner-friendly idea:
    Let dp[i] mean:
    "the minimum total energy needed to land on exercise i"

    To land on exercise i, we must have come from:
    - exercise i - 1, or
    - exercise i - 2

    So:
    dp[i] = energy[i] + min(dp[i - 1], dp[i - 2])

    At the end, we want to reach just beyond the last exercise.
    We can finish from either:
    - the last exercise, or
    - the second-to-last exercise

    Therefore answer = min(dp[n - 1], dp[n - 2])
    */
    public int MinEnergy(int[] energy)
    {
        // The problem guarantees at least 2 exercises,
        // but this guard makes the method safer and easier to understand.
        if (energy == null || energy.Length == 0)
        {
            return 0;
        }

        if (energy.Length == 1)
        {
            // If there were only one exercise, the cheapest way would be
            // to land on it and then finish.
            return energy[0];
        }

        // prev2 will represent dp[i - 2]
        // Initially, for i = 2, dp[0] is simply energy[0],
        // because the cheapest way to land on exercise 0 is to start and step onto it.
        int prev2 = energy[0];

        // prev1 will represent dp[i - 1]
        // Similarly, dp[1] is simply energy[1],
        // because we may start and jump directly onto exercise 1.
        int prev1 = energy[1];

        // We now compute the minimum energy for each later exercise.
        // For every exercise i >= 2:
        // - If we come from i - 1, total cost is dp[i - 1] + energy[i]
        // - If we come from i - 2, total cost is dp[i - 2] + energy[i]
        // We choose the cheaper of those two options.
        for (int i = 2; i < energy.Length; i++)
        {
            // Compute the minimum energy needed to land on the current exercise.
            int current = energy[i] + Math.Min(prev1, prev2);

            // Shift the rolling values forward:
            // - old prev1 becomes the new prev2
            // - current becomes the new prev1
            //
            // This works because for the next iteration:
            // - dp[i - 1] should be available as prev2 or prev1 depending on the shift
            // - dp[i] should become the newest previous value
            prev2 = prev1;
            prev1 = current;
        }

        // We do NOT pay any cost for the finish position beyond the last exercise.
        // We can reach the finish from either:
        // - the last exercise
        // - the second-to-last exercise
        //
        // So the answer is the cheaper of those two accumulated costs.
        return Math.Min(prev1, prev2);
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] energy1 = { 4, 1, 6, 2 };
int result1 = solution.MinEnergy(energy1);
Console.WriteLine("Example 1:");
Console.WriteLine($"Input: [{string.Join(", ", energy1)}]");
Console.WriteLine($"Minimum total energy: {result1}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Example 2
int[] energy2 = { 3, 5, 2, 1, 4 };
int result2 = solution.MinEnergy(energy2);
Console.WriteLine("Example 2:");
Console.WriteLine($"Input: [{string.Join(", ", energy2)}]");
Console.WriteLine($"Minimum total energy: {result2}");
Console.WriteLine("Expected: 6");
Console.WriteLine();

// Additional demo
int[] energy3 = { 10, 15, 20 };
int result3 = solution.MinEnergy(energy3);
Console.WriteLine("Additional Demo:");
Console.WriteLine($"Input: [{string.Join(", ", energy3)}]");
Console.WriteLine($"Minimum total energy: {result3}");
Console.WriteLine("Expected: 15");