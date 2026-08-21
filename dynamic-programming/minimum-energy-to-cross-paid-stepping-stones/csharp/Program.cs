/*
Title: Minimum Energy to Cross Paid Stepping Stones

Problem Description:
You are given an array cost where cost[i] is the energy required to land on stepping stone i.
A hiker wants to cross a small river by moving from left to right. From any stone, the hiker
may jump either 1 stone ahead or 2 stones ahead. The hiker may start on stone 0 or stone 1,
and the goal is to move beyond the last stone with the minimum total energy spent.

A stone's energy cost is paid only when the hiker lands on that stone. Reaching the far bank
just past the last index does not cost anything. Your task is to return the minimum total energy
needed to cross the river.

This is a dynamic programming problem because the cheapest way to reach a stone depends on the
cheapest ways to reach the previous one or two stones. An efficient solution should compute the
answer in linear time.

Constraints:
- 2 <= cost.length <= 1000
- 0 <= cost[i] <= 999

Example 1:
Input: cost = [4, 7, 2, 9]
Output: 6
Explanation: Start on stone 0 (pay 4), jump to stone 2 (pay 2), then jump beyond the last stone.
Total energy = 4 + 2 = 6.

Example 2:
Input: cost = [1, 100, 1, 1, 100, 1]
Output: 3
Explanation: Start on stone 0, then land on stones 2, 3, and 5. The total is 1 + 1 + 1 = 3.
Other paths require more energy.

Goal:
Return the minimum energy required to reach the far bank.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We process each stone exactly once in a single loop.

    Space Complexity: O(1)
    - We do not use a full DP array.
    - We only keep track of the minimum cost to reach the previous two stones.
    */
    public int MinCostClimbingStairs(int[] cost)
    {
        // The input guarantees at least 2 stones, but this guard makes the method
        // more defensive and beginner-friendly in case it is reused elsewhere.
        if (cost == null || cost.Length == 0)
        {
            return 0;
        }

        if (cost.Length == 1)
        {
            return cost[0];
        }

        // Dynamic Programming Idea:
        //
        // Let dp[i] mean:
        // "the minimum total energy needed to land on stone i"
        //
        // If we want to land on stone i, we could have come from:
        // - stone i - 1
        // - stone i - 2
        //
        // So the recurrence is:
        // dp[i] = cost[i] + min(dp[i - 1], dp[i - 2])
        //
        // Why?
        // Because landing on stone i requires paying cost[i],
        // and before that we want the cheaper of the two valid previous positions.
        //
        // Base cases:
        // dp[0] = cost[0]
        // dp[1] = cost[1]
        //
        // Why is dp[1] just cost[1] instead of cost[0] + cost[1]?
        // Because the hiker is allowed to START on stone 0 OR stone 1.
        // So landing directly on stone 1 costs only cost[1].
        //
        // Final answer:
        // The far bank is just beyond the last stone.
        // To reach it, the hiker can jump from:
        // - the last stone
        // - the second-to-last stone
        //
        // Therefore the answer is:
        // min(dp[n - 1], dp[n - 2])
        //
        // Instead of storing the whole dp array, we only need the previous two values.
        // This is enough because each new state depends only on the two states before it.

        // prev2 represents dp[i - 2].
        // Initially, before the loop starts:
        // prev2 = dp[0] = cost[0]
        int prev2 = cost[0];

        // prev1 represents dp[i - 1].
        // Initially:
        // prev1 = dp[1] = cost[1]
        int prev1 = cost[1];

        // We now compute dp[2], dp[3], ..., dp[n - 1].
        for (int i = 2; i < cost.Length; i++)
        {
            // Current step:
            // Compute the minimum cost to land on stone i.
            //
            // We have exactly two ways to get here:
            // 1. Jump from stone i - 1, which costs prev1 so far
            // 2. Jump from stone i - 2, which costs prev2 so far
            //
            // We choose the cheaper path, then add the cost of landing on stone i.
            int current = cost[i] + Math.Min(prev1, prev2);

            // Now we slide our window forward.
            //
            // Why do this?
            // Because in the next iteration:
            // - the old prev1 should become the new prev2
            // - the newly computed current should become the new prev1
            //
            // This keeps the meaning of the variables consistent:
            // prev2 = dp[i - 1] from the next iteration's perspective
            // prev1 = dp[i]
            prev2 = prev1;
            prev1 = current;
        }

        // After the loop:
        // - prev1 holds dp[n - 1]  -> minimum cost to land on the last stone
        // - prev2 holds dp[n - 2]  -> minimum cost to land on the second-to-last stone
        //
        // To reach the far bank, the hiker can jump from either of those stones.
        // The bank itself has no cost, so we simply take the cheaper of the two.
        return Math.Min(prev1, prev2);
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] cost1 = { 4, 7, 2, 9 };
int result1 = solution.MinCostClimbingStairs(cost1);
Console.WriteLine("Example 1:");
Console.WriteLine($"Input: [{string.Join(", ", cost1)}]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 6");
Console.WriteLine();

// Example 2
int[] cost2 = { 1, 100, 1, 1, 100, 1 };
int result2 = solution.MinCostClimbingStairs(cost2);
Console.WriteLine("Example 2:");
Console.WriteLine($"Input: [{string.Join(", ", cost2)}]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Additional demo
int[] cost3 = { 10, 15, 20 };
int result3 = solution.MinCostClimbingStairs(cost3);
Console.WriteLine("Additional Example:");
Console.WriteLine($"Input: [{string.Join(", ", cost3)}]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 15");