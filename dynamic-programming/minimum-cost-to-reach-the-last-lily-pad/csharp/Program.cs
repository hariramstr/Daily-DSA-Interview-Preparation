/*
Title: Minimum Cost to Reach the Last Lily Pad
Difficulty: Easy
Topic: Dynamic Programming

Problem Description:
A frog is crossing a narrow pond by jumping across a line of lily pads. You are given an integer array cost where cost[i] is the energy cost of landing on lily pad i. The frog starts before the first lily pad, and its goal is to move beyond the last pad. On each move, the frog may jump forward by either 1 pad or 2 pads. If the frog lands on a lily pad, it must pay that pad's cost. The frog does not pay any cost for the starting position before index 0, and it also does not pay any cost after it has moved beyond the last index. Return the minimum total energy needed to reach beyond the last lily pad.

This is a classic one-dimensional dynamic programming problem with a simple state transition. For each position, you can arrive from one step before or two steps before, so the best answer for a pad depends on the minimum cost of those earlier positions. An efficient solution should run in O(n) time, and it can be implemented with either a full DP array or constant extra space.

Constraints:
- 1 <= cost.length <= 1000
- 0 <= cost[i] <= 999

Example 1:
Input: cost = [4, 2, 7, 3]
Output: 5
Explanation: One optimal path is to land on pad 1 (cost 2), then pad 3 (cost 3), then jump beyond the last pad. Total cost = 2 + 3 = 5.

Example 2:
Input: cost = [1, 100, 1, 1, 100, 1]
Output: 3
Explanation: One optimal path is to land on pads 0, 2, 3, and 5, paying 1 + 1 + 1 + 1 = 4, but that is not optimal. A better path is to land on pads 0, 2, and 5, paying 1 + 1 + 1 = 3.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    We use dynamic programming with constant extra space.

    Core idea:
    - To reach any pad i, the frog must come from either:
      1) pad i - 1
      2) pad i - 2
    - So the minimum cost to land on pad i is:
      cost[i] + min(minCostToReach(i - 1), minCostToReach(i - 2))

    Important detail:
    - The frog starts before index 0 and may first jump to pad 0 or pad 1.
    - The frog wants to go beyond the last pad, and that final move costs nothing.
    - Therefore, the final answer is:
      min(minCostToReach(last pad), minCostToReach(second last pad))
      because from either of those positions the frog can jump out of the array.
    */
    public int MinCostClimbingStairs(int[] cost)
    {
        // If there is only one pad, the frog can jump directly beyond it using a 2-step jump.
        // That means it does not need to land on the only pad at all, so the cost is 0.
        // Example:
        // cost = [5]
        // Start before index 0, jump 2 steps beyond the array, pay nothing.
        if (cost.Length == 1)
        {
            return 0;
        }

        // prev2 will store the minimum total cost required to land on pad i - 2.
        // At the beginning, when we are conceptually preparing to process later pads,
        // the minimum cost to land on pad 0 is simply cost[0],
        // because the frog can jump directly from the start to pad 0.
        int prev2 = cost[0];

        // prev1 will store the minimum total cost required to land on pad i - 1.
        // The minimum cost to land on pad 1 is simply cost[1],
        // because the frog can also jump directly from the start to pad 1.
        int prev1 = cost[1];

        // Now we compute the minimum cost to land on every pad from index 2 onward.
        // We do this from left to right because each state depends only on earlier states.
        for (int i = 2; i < cost.Length; i++)
        {
            // To land on pad i, the frog has exactly two valid previous positions:
            // - pad i - 1
            // - pad i - 2
            //
            // If it comes from pad i - 1, the total cost would be:
            // prev1 + cost[i]
            //
            // If it comes from pad i - 2, the total cost would be:
            // prev2 + cost[i]
            //
            // We choose the smaller of those two because we want the minimum total energy.
            int current = cost[i] + Math.Min(prev1, prev2);

            // Move the sliding window forward:
            // - The old prev1 becomes the new prev2
            // - The newly computed current becomes the new prev1
            //
            // This works because for the next iteration:
            // - current pad i will become "i - 1"
            // - previous pad i - 1 will become "i - 2"
            prev2 = prev1;
            prev1 = current;
        }

        // After processing all pads:
        // - prev1 = minimum cost to land on the last pad
        // - prev2 = minimum cost to land on the second last pad
        //
        // To go beyond the last pad, the frog can jump:
        // - from the last pad by 1 step
        // - or from the second last pad by 2 steps
        //
        // Since jumping beyond the array costs nothing,
        // the answer is the cheaper of those two possibilities.
        return Math.Min(prev1, prev2);
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] cost1 = { 4, 2, 7, 3 };
int result1 = solution.MinCostClimbingStairs(cost1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 5

// Example 2
int[] cost2 = { 1, 100, 1, 1, 100, 1 };
int result2 = solution.MinCostClimbingStairs(cost2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 3

// Additional small test
int[] cost3 = { 10, 15, 20 };
int result3 = solution.MinCostClimbingStairs(cost3);
Console.WriteLine("Additional Test Result: " + result3); // Expected: 15