/*
Title: Minimum Rest Cost for a Hiking Trail

Problem Description:
You are planning a hike along a straight trail that is divided into numbered checkpoints from 0 to n - 1.
You start at checkpoint 0 and want to reach checkpoint n - 1.

From any checkpoint i, you may move:
- 1 step forward to i + 1
- 2 steps forward to i + 2
as long as that checkpoint exists.

Each checkpoint has a rest cost given by an integer array cost, where cost[i] is the energy cost
of stopping at checkpoint i. If you land on a checkpoint during your hike, you must pay its rest cost.

Your goal is to reach the final checkpoint with the minimum total cost.

Important detail:
- You start at checkpoint 0, so checkpoint 0 is included in the total cost.
- If n == 1, you are already at the final checkpoint, so the answer is cost[0].

Dynamic Programming Idea:
Let dp[i] represent the minimum total cost required to reach checkpoint i.
To reach checkpoint i, you must come from either:
- checkpoint i - 1
- checkpoint i - 2

So:
dp[i] = min(dp[i - 1], dp[i - 2]) + cost[i]

Base cases:
- dp[0] = cost[0]
- dp[1] = cost[0] + cost[1]

Example 1:
cost = [4, 2, 7, 3]
dp[0] = 4
dp[1] = 4 + 2 = 6
dp[2] = min(6, 4) + 7 = 11
dp[3] = min(11, 6) + 3 = 9
Answer = 9

Example 2:
cost = [1, 100, 1, 1, 100, 1]
dp[0] = 1
dp[1] = 1 + 100 = 101
dp[2] = min(101, 1) + 1 = 2
dp[3] = min(2, 101) + 1 = 3
dp[4] = min(3, 2) + 100 = 102
dp[5] = min(102, 3) + 1 = 4
Answer = 4
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We process each checkpoint exactly once.

    Space Complexity: O(n)
    - We use a DP array of size n to store the minimum cost to reach each checkpoint.

    This version is intentionally beginner-friendly:
    - It uses a full DP array so every state is visible.
    - That makes the logic easier to understand and trace step by step.
    */
    public int MinRestCost(int[] cost)
    {
        // The number of checkpoints on the trail.
        int n = cost.Length;

        // Base case:
        // If there is only one checkpoint, then we start there and it is also the destination.
        // Because we are standing on checkpoint 0, we must pay its cost.
        if (n == 1)
        {
            return cost[0];
        }

        // Create a DP array where:
        // dp[i] = minimum total cost needed to reach checkpoint i.
        //
        // Why use an array?
        // Because dynamic programming works by storing answers to smaller subproblems
        // so we can reuse them later instead of recomputing them.
        int[] dp = new int[n];

        // Base case for checkpoint 0:
        // We start at checkpoint 0, so the minimum cost to reach checkpoint 0
        // is simply the cost of checkpoint 0 itself.
        dp[0] = cost[0];

        // Base case for checkpoint 1:
        // Since we start at checkpoint 0, the only way to reach checkpoint 1
        // is to move from 0 to 1.
        // Therefore, total cost = cost at 0 + cost at 1.
        dp[1] = cost[0] + cost[1];

        // Fill the DP table from left to right.
        // We start at checkpoint 2 because checkpoints 0 and 1 are already initialized.
        for (int i = 2; i < n; i++)
        {
            // To reach checkpoint i, there are only two possible previous checkpoints:
            // 1) i - 1
            // 2) i - 2
            //
            // If we come from i - 1, total cost would be:
            // dp[i - 1] + cost[i]
            //
            // If we come from i - 2, total cost would be:
            // dp[i - 2] + cost[i]
            //
            // We choose the cheaper of those two options.
            dp[i] = Math.Min(dp[i - 1], dp[i - 2]) + cost[i];
        }

        // The answer is the minimum cost to reach the final checkpoint, which is n - 1.
        return dp[n - 1];
    }
}

// Demo code:
// Create a solver instance, run the sample inputs, and print the results.

var solution = new Solution();

int[] cost1 = { 4, 2, 7, 3 };
int result1 = solution.MinRestCost(cost1);
Console.WriteLine(result1); // Expected: 9

int[] cost2 = { 1, 100, 1, 1, 100, 1 };
int result2 = solution.MinRestCost(cost2);
Console.WriteLine(result2); // Expected: 4

int[] cost3 = { 5 };
int result3 = solution.MinRestCost(cost3);
Console.WriteLine(result3); // Expected: 5