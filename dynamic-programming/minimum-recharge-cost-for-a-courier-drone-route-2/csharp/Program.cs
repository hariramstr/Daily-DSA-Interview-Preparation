/*
Title: Minimum Recharge Cost for a Courier Drone Route
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
A courier drone must travel through a fixed sequence of delivery checkpoints from left to right.
The route is represented by an integer array cost, where cost[i] is the energy cost to land on
checkpoint i and recharge there. The drone starts before checkpoint 0 and wants to reach exactly
the last checkpoint n - 1.

From checkpoint i, the drone may fly forward by either 1 or 2 checkpoints. However, some
checkpoints are marked as windy and cannot be used for landing. You are given a binary array
blocked of the same length, where blocked[i] = 1 means checkpoint i is unavailable and the drone
may not land there. The first and last checkpoints are guaranteed to be unblocked.

The drone pays the recharge cost only for checkpoints it lands on, including the final checkpoint.
It does not pay anything before the route begins. Return the minimum total recharge cost needed to
reach the last checkpoint. If the last checkpoint cannot be reached, return -1.

Important interpretation from the examples:
- The drone starts before the array, so its first landing may be checkpoint 0 or checkpoint 1.
- That means checkpoint 0 is NOT mandatory.
- After landing on a checkpoint, the drone may move forward by 1 or 2 checkpoints.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    We use dynamic programming:
    - dp[i] = minimum total recharge cost needed to land on checkpoint i
    - If checkpoint i is blocked, dp[i] is treated as unreachable
    - Since the drone starts before the route, it may first land on:
        * checkpoint 0
        * checkpoint 1
      as long as those checkpoints are not blocked
    - For every later checkpoint i, the drone can only come from:
        * i - 1
        * i - 2
      because those are the only allowed jump lengths
    */
    public long MinRechargeCost(int[] cost, int[] blocked)
    {
        int n = cost.Length;

        // We use a very large number as our "infinity" value.
        // This represents a checkpoint that is currently unreachable.
        // We choose a long value because:
        // - cost[i] can be as large as 1,000,000,000
        // - n can be as large as 100,000
        // - total path cost can therefore exceed int range
        long inf = long.MaxValue / 4;

        // dp[i] will store the minimum total cost to land exactly on checkpoint i.
        // If dp[i] stays equal to inf, that means checkpoint i cannot be reached.
        long[] dp = new long[n];
        Array.Fill(dp, inf);

        // -----------------------------
        // Step 1: Initialize starting landings
        // -----------------------------
        // The drone starts before checkpoint 0 and pays nothing before landing.
        // According to the examples, the first landing can be:
        // - checkpoint 0
        // - checkpoint 1
        //
        // So we initialize both possibilities if they are not blocked.

        if (blocked[0] == 0)
        {
            // If we choose to first land on checkpoint 0,
            // the total cost is simply cost[0].
            dp[0] = cost[0];
        }

        if (n > 1 && blocked[1] == 0)
        {
            // If we choose to first land on checkpoint 1 directly from the start,
            // the total cost is simply cost[1].
            //
            // This is essential for Example 1:
            // cost = [4,2,7,3,5], blocked = [0,0,1,0,0]
            // Best route is 1 -> 3 -> 4 with total 2 + 3 + 5 = 10.
            dp[1] = cost[1];
        }

        // -----------------------------
        // Step 2: Fill the DP table from left to right
        // -----------------------------
        // For each checkpoint i >= 2:
        // - If it is blocked, we cannot land there, so skip it.
        // - Otherwise, we try to arrive from i-1 or i-2.
        // - We take the cheaper reachable option.
        for (int i = 2; i < n; i++)
        {
            // If this checkpoint is blocked, landing here is forbidden.
            // Therefore dp[i] must remain unreachable.
            if (blocked[i] == 1)
            {
                continue;
            }

            // We will compute the best previous reachable checkpoint.
            long bestPrevious = inf;

            // Option 1: come from checkpoint i - 1
            // This is valid only if i - 1 itself was reachable.
            if (dp[i - 1] != inf)
            {
                bestPrevious = Math.Min(bestPrevious, dp[i - 1]);
            }

            // Option 2: come from checkpoint i - 2
            // This is valid only if i - 2 itself was reachable.
            if (dp[i - 2] != inf)
            {
                bestPrevious = Math.Min(bestPrevious, dp[i - 2]);
            }

            // If neither previous checkpoint was reachable,
            // then checkpoint i is also unreachable.
            if (bestPrevious == inf)
            {
                continue;
            }

            // Otherwise, the minimum cost to land on checkpoint i is:
            // best cost to reach a valid previous checkpoint
            // + cost to land on checkpoint i
            dp[i] = bestPrevious + cost[i];
        }

        // -----------------------------
        // Step 3: Return the answer for the last checkpoint
        // -----------------------------
        // The problem asks for the minimum total cost to reach exactly checkpoint n - 1.
        // If dp[n - 1] is still inf, then no valid route exists.
        if (dp[n - 1] == inf)
        {
            return -1;
        }

        return dp[n - 1];
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] cost1 = { 4, 2, 7, 3, 5 };
int[] blocked1 = { 0, 0, 1, 0, 0 };
long result1 = solution.MinRechargeCost(cost1, blocked1);
Console.WriteLine(result1); // Expected: 10

// Example 2
int[] cost2 = { 6, 1, 9, 2 };
int[] blocked2 = { 0, 1, 1, 0 };
long result2 = solution.MinRechargeCost(cost2, blocked2);
Console.WriteLine(result2); // Expected: -1

// Additional quick checks
int[] cost3 = { 5, 1 };
int[] blocked3 = { 0, 0 };
long result3 = solution.MinRechargeCost(cost3, blocked3);
Console.WriteLine(result3); // Expected: 1 (start -> 1)

int[] cost4 = { 3, 8, 2, 4 };
int[] blocked4 = { 0, 0, 0, 0 };
long result4 = solution.MinRechargeCost(cost4, blocked4);
Console.WriteLine(result4); // One optimal route: 1 -> 3 = 8 + 4 = 12, or 0 -> 2 -> 3 = 3 + 2 + 4 = 9, expected: 9