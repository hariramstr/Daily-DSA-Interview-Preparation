/*
Title: Minimum Energy to Cross a Toll Bridge Path

Problem Description:
A courier robot needs to cross a sequence of bridge sections to deliver a package.
The path is represented by an array cost, where cost[i] is the energy required to land on section i.

The robot may start before the first section, and on each move it can jump either
1 section or 2 sections forward. The robot reaches the destination when it moves
past the last section.

If the robot lands on a section, it must pay that section's energy cost exactly once.
Since it can jump over sections, skipped sections do not add any cost.

Goal:
Compute the minimum total energy needed to reach the destination.

Key Dynamic Programming Idea:
To reach any section i, the robot must have come from either:
- section i - 1
- section i - 2

So the minimum cost to land on section i is:
cost[i] + min(min cost to land on i - 1, min cost to land on i - 2)

Finally, the robot can move past the last section from either:
- the last section
- the second-to-last section

So the answer is:
min(min cost to land on last section, min cost to land on second-to-last section)

Example 1:
cost = [4, 2, 7, 3]
Minimum path:
start -> section 1 (2) -> section 3 (3) -> end
Answer = 5

Example 2:
cost = [1, 100, 1, 1, 100, 1]
A best path:
start -> section 0 (1) -> section 2 (1) -> section 3 (1) -> end
Answer = 3

Constraints:
- 2 <= cost.length <= 1000
- 0 <= cost[i] <= 999
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We process each section exactly once.

    Space Complexity: O(1)
    - We do not build a full DP array.
    - We only keep the last two DP states, because each new state depends only on the previous two.
    */
    public int MinCostClimbingStairs(int[] cost)
    {
        // The problem guarantees at least 2 elements, but this guard makes the method safer
        // and easier for beginners to understand if they test extra cases on their own.
        if (cost == null || cost.Length == 0)
        {
            return 0;
        }

        if (cost.Length == 1)
        {
            return cost[0];
        }

        // dp0 represents the minimum total energy needed to land on section 0.
        // If we land on section 0, we must pay cost[0].
        int dp0 = cost[0];

        // dp1 represents the minimum total energy needed to land on section 1.
        // If we land on section 1 directly from the start, we pay cost[1].
        int dp1 = cost[1];

        // We now compute the minimum energy to land on every later section.
        // Instead of storing all DP values in an array, we only keep the last two:
        // - dp0 = answer for i - 2
        // - dp1 = answer for i - 1
        //
        // This works because to compute the current section i, we only need:
        // min(previous section, section before previous)
        for (int i = 2; i < cost.Length; i++)
        {
            // Current step:
            // Compute the minimum energy needed to land on section i.
            //
            // Why this formula is correct:
            // To land on section i, the robot must come from:
            // - section i - 1 with cost dp1
            // - section i - 2 with cost dp0
            //
            // We choose the cheaper of those two ways, then add cost[i]
            // because landing on section i requires paying its energy cost.
            int current = cost[i] + Math.Min(dp0, dp1);

            // Shift the window forward:
            // After computing section i:
            // - old dp1 becomes the new "two steps back" value
            // - current becomes the new "one step back" value
            //
            // This is the constant-space version of dynamic programming.
            dp0 = dp1;
            dp1 = current;
        }

        // Final step:
        // The destination is just beyond the last section.
        //
        // The robot can reach the destination from either:
        // - the last section
        // - the second-to-last section
        //
        // It does NOT need to pay any extra cost for the destination itself,
        // because the destination is not a real section in the cost array.
        //
        // Therefore, the answer is the cheaper of:
        // - minimum cost to land on last section
        // - minimum cost to land on second-to-last section
        return Math.Min(dp0, dp1);
    }
}

// Demo code:
// Create sample inputs, call the solution, and print results.

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

// Additional beginner-friendly test
int[] cost3 = { 10, 15, 20 };
int result3 = solution.MinCostClimbingStairs(cost3);
Console.WriteLine("Additional Test:");
Console.WriteLine($"Input: [{string.Join(", ", cost3)}]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 15");