/*
Title: Minimum Cost to Hop Across Discount Tiles
Difficulty: Easy
Topic: Dynamic Programming

Problem Description:
You are given an array `cost` where `cost[i]` is the fee to land on tile `i` in a hallway.
A player starts before the first tile and wants to move past the last tile.
On each move, the player may hop forward by either 1 tile or 2 tiles.
Whenever the player lands on a tile, they must pay that tile's fee.

There is one small twist: because of a promotional rule, the player is allowed to start by
landing on either tile `0` or tile `1` without paying any cost before that move.
After that, every landed tile must be paid normally.

Return the minimum total fee needed to move beyond the last tile.

Dynamic Programming Idea:
To reach any position cheaply, we only need to know the cheapest way to reach the previous
one or two positions. That makes this a classic dynamic programming problem.

Important clarification:
Although the wording says the player may start by landing on tile 0 or tile 1 "without paying
any cost before that move", the examples clearly follow the standard interpretation of the
classic problem: when you land on a tile, you pay that tile's cost, and you are allowed to
begin from step 0 or step 1 as your starting choice. The minimum total cost to move beyond
the last tile is therefore computed as the minimum cost to reach the top from either of the
last two tiles.

Example 1:
cost = [4, 2, 7, 3]
Minimum = 5
Path: land on tile 1 (pay 2), land on tile 3 (pay 3), move beyond => total 5

Example 2:
cost = [1, 100, 1, 1, 100, 1]
Minimum = 3
Path: tile 0 (1) -> tile 2 (1) -> tile 3 (1) -> beyond => total 3
*/

using System;

public class Solution
{
    public int MinCostClimbingStairs(int[] cost)
    {
        /*
        Time Complexity: O(n)
        - We process each tile exactly once.

        Space Complexity: O(1)
        - We do not need a full DP array.
        - We only keep track of the minimum cost for the previous two positions.
        */

        // The hallway has at least 2 tiles according to the constraints.
        // We define:
        // - prev2 = minimum cost required to stand "before" processing tile i-1
        // - prev1 = minimum cost required to stand "before" processing tile i
        //
        // A very beginner-friendly way to think about this:
        // dp[i] = minimum cost needed to reach position i
        // where position i means:
        //   - i is not a tile cost index directly
        //   - instead, position i is the place you are standing before deciding
        //     whether to land on tile i next
        //
        // Standard recurrence:
        // dp[i] = min(dp[i - 1] + cost[i - 1], dp[i - 2] + cost[i - 2])
        //
        // Why?
        // To reach position i, your last move must have come from:
        //   1. position i - 1, then you land on tile i - 1 and pay cost[i - 1]
        //   2. position i - 2, then you land on tile i - 2 and pay cost[i - 2]
        //
        // Base cases:
        // dp[0] = 0  -> starting before tile 0 costs nothing
        // dp[1] = 0  -> you are also allowed to effectively start before tile 1
        //
        // The answer is dp[n], where n is beyond the last tile.

        int n = cost.Length;

        // These represent dp[0] and dp[1].
        // Both are 0 because you may start from tile 0 or tile 1 without any
        // cost paid before making your first landing decision.
        int prev2 = 0;
        int prev1 = 0;

        // We now compute dp[2], dp[3], ..., dp[n].
        // Each "position i" means we are trying to reach that position,
        // which is one step beyond tile i - 1.
        for (int i = 2; i <= n; i++)
        {
            // Option 1:
            // Reach position i by coming from position i - 1.
            // To do that, the last tile we land on is tile i - 1,
            // so we must pay cost[i - 1].
            int oneStep = prev1 + cost[i - 1];

            // Option 2:
            // Reach position i by coming from position i - 2.
            // Then the last tile we land on is tile i - 2,
            // so we must pay cost[i - 2].
            int twoSteps = prev2 + cost[i - 2];

            // The cheapest way to reach position i is the better of those two choices.
            int current = Math.Min(oneStep, twoSteps);

            // Move our rolling variables forward:
            // - prev2 becomes the old prev1
            // - prev1 becomes the newly computed current
            //
            // This works because each new state only depends on the previous two states.
            prev2 = prev1;
            prev1 = current;
        }

        // After the loop, prev1 holds dp[n],
        // which is the minimum total cost to move beyond the last tile.
        return prev1;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] cost1 = { 4, 2, 7, 3 };
int result1 = solution.MinCostClimbingStairs(cost1);
Console.WriteLine("Example 1:");
Console.WriteLine($"Input: [{string.Join(", ", cost1)}]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 5");
Console.WriteLine();

// Example 2
int[] cost2 = { 1, 100, 1, 1, 100, 1 };
int result2 = solution.MinCostClimbingStairs(cost2);
Console.WriteLine("Example 2:");
Console.WriteLine($"Input: [{string.Join(", ", cost2)}]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Additional quick checks
int[] cost3 = { 10, 15, 20 };
int result3 = solution.MinCostClimbingStairs(cost3);
Console.WriteLine("Additional Test 1:");
Console.WriteLine($"Input: [{string.Join(", ", cost3)}]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 15");
Console.WriteLine();

int[] cost4 = { 0, 0, 0, 0 };
int result4 = solution.MinCostClimbingStairs(cost4);
Console.WriteLine("Additional Test 2:");
Console.WriteLine($"Input: [{string.Join(", ", cost4)}]");
Console.WriteLine($"Output: {result4}");
Console.WriteLine("Expected: 0");