/*
Title: Maximum Reward from Skipping Adjacent Milestones
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
You are planning a roadshow with n milestone events arranged in a fixed order along a route.
Attending milestone i gives you a reward value rewards[i], which may be positive, zero, or negative.
However, due to travel fatigue and scheduling limits, you are not allowed to attend two adjacent milestones.
You may also choose to skip any milestone.

Your task is to return the maximum total reward you can collect.

In addition to the maximum reward, think carefully about edge cases: if all rewards are negative,
it is valid to attend no milestones at all, resulting in a total reward of 0. The order of milestones
cannot be changed.

Formally, choose a subset of indices such that no two chosen indices are adjacent, and the sum of
their reward values is maximized.

Constraints:
- 1 <= n <= 100000
- -10000 <= rewards[i] <= 10000
- The solution should run in O(n) time.
- Aim for O(1) extra space if possible.

Example 1:
Input: rewards = [5, 1, 2, 10, 6]
Output: 15
Explanation: Attend milestones with rewards 5 and 10 for a total of 15. Attending 5, 2, and 6 is only 13.

Example 2:
Input: rewards = [-4, -2, -7]
Output: 0
Explanation: Every milestone reduces the total reward, so the best choice is to skip all of them.

Core Dynamic Programming Idea:
For each milestone, we have two choices:
1. Skip it -> keep the best total we already had from previous milestones.
2. Attend it -> add its reward to the best total from two milestones earlier,
   because we cannot attend adjacent milestones.

We take the better of those two choices at every step.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the rewards array exactly once.

    Space Complexity: O(1)
    - We do not build a full DP array.
    - We only keep two running values:
      1. best total up to the previous milestone
      2. best total up to two milestones before
    */
    public int MaxReward(int[] rewards)
    {
        // This variable stores the best answer for milestones up to index i - 2.
        // In other words:
        // - Before processing the current milestone, "prevTwo" represents the best
        //   total reward we could have collected from the portion of the array that
        //   ends two positions earlier.
        //
        // Why do we need this?
        // Because if we decide to attend the current milestone, we are forbidden from
        // attending the immediately previous one. So the latest safe total we can add
        // to the current reward is the best total from two positions back.
        //
        // We initialize it to 0 because:
        // - Before processing any milestones, the best reward is 0.
        // - This also correctly supports the rule that we may choose to attend nothing.
        int prevTwo = 0;

        // This variable stores the best answer for milestones up to index i - 1.
        // That means:
        // - Before processing the current milestone, "prevOne" is the best total reward
        //   we can achieve using all milestones seen so far up to the previous index.
        //
        // Why is this needed?
        // Because one of our two choices at each step is to skip the current milestone.
        // If we skip it, then the best answer simply remains whatever the best answer
        // was up to the previous milestone.
        //
        // We also initialize this to 0 for the same reason:
        // choosing no milestones is always allowed, especially important when values
        // are negative.
        int prevOne = 0;

        // We now process each milestone from left to right.
        // This order is important because each decision depends only on earlier results.
        for (int i = 0; i < rewards.Length; i++)
        {
            // Option 1: Skip the current milestone.
            //
            // If we skip rewards[i], then nothing changes compared to the best answer
            // we already had for the previous milestone.
            //
            // So the total reward in this case is simply "prevOne".
            int skipCurrent = prevOne;

            // Option 2: Attend the current milestone.
            //
            // If we attend rewards[i], we are not allowed to attend milestone i - 1.
            // Therefore, the best compatible total we can combine with rewards[i]
            // is the best answer from two milestones earlier, stored in "prevTwo".
            //
            // So this choice gives:
            // best total up to i - 2 + current reward
            int takeCurrent = prevTwo + rewards[i];

            // Choose the better of the two valid choices:
            // - skip the current milestone
            // - attend the current milestone
            //
            // This is the heart of the dynamic programming recurrence:
            // dp[i] = max(dp[i - 1], dp[i - 2] + rewards[i])
            //
            // Because "prevOne" and "prevTwo" already represent dp[i - 1] and dp[i - 2],
            // we can compute the current answer without storing the entire dp array.
            //
            // Important note about negative values:
            // Since prevOne starts at 0 and we always take the maximum,
            // the result will never drop below 0.
            // That correctly models the rule that we may skip all milestones.
            int currentBest = Math.Max(skipCurrent, takeCurrent);

            // Now we shift our rolling variables forward for the next iteration.
            //
            // Before moving on:
            // - currentBest is dp[i]
            // - prevOne is currently dp[i - 1]
            // - prevTwo is currently dp[i - 2]
            //
            // For the next milestone:
            // - prevTwo should become the old prevOne
            // - prevOne should become currentBest
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // After processing all milestones, "prevOne" holds the best total reward
        // for the entire array.
        return prevOne;
    }
}

// Demo code
var solution = new Solution();

// Example 1 from the problem statement.
// rewards = [5, 1, 2, 10, 6]
//
// Step-by-step expected DP behavior:
// i=0: max(0, 0+5)  = 5
// i=1: max(5, 0+1)  = 5
// i=2: max(5, 5+2)  = 7
// i=3: max(7, 5+10) = 15
// i=4: max(15, 7+6) = 15
//
// Final answer = 15
int[] rewards1 = { 5, 1, 2, 10, 6 };
int result1 = solution.MaxReward(rewards1);
Console.WriteLine($"Input: [{string.Join(", ", rewards1)}]");
Console.WriteLine($"Maximum reward: {result1}");
Console.WriteLine("Expected: 15");
Console.WriteLine();

// Example 2 from the problem statement.
// rewards = [-4, -2, -7]
//
// Step-by-step expected DP behavior:
// i=0: max(0, 0-4) = 0
// i=1: max(0, 0-2) = 0
// i=2: max(0, 0-7) = 0
//
// Final answer = 0
int[] rewards2 = { -4, -2, -7 };
int result2 = solution.MaxReward(rewards2);
Console.WriteLine($"Input: [{string.Join(", ", rewards2)}]");
Console.WriteLine($"Maximum reward: {result2}");
Console.WriteLine("Expected: 0");
Console.WriteLine();

// Additional demo: mixed positive and negative values.
// One optimal choice is to take 4 + 7 + 3 = 14.
int[] rewards3 = { 4, -1, 2, 7, -3, 3 };
int result3 = solution.MaxReward(rewards3);
Console.WriteLine($"Input: [{string.Join(", ", rewards3)}]");
Console.WriteLine($"Maximum reward: {result3}");
Console.WriteLine();

// Additional demo: single negative milestone.
// Best choice is to skip it, so answer should be 0.
int[] rewards4 = { -5 };
int result4 = solution.MaxReward(rewards4);
Console.WriteLine($"Input: [{string.Join(", ", rewards4)}]");
Console.WriteLine($"Maximum reward: {result4}");
Console.WriteLine();

// Additional demo: single positive milestone.
// Best choice is to take it.
int[] rewards5 = { 9 };
int result5 = solution.MaxReward(rewards5);
Console.WriteLine($"Input: [{string.Join(", ", rewards5)}]");
Console.WriteLine($"Maximum reward: {result5}");