/*
Title: Maximum Reward from Adjacent Booth Visits
Difficulty: Easy
Topic: Dynamic Programming

Problem Description:
You are walking through a row of festival booths. Each booth offers a reward value, given by an integer array rewards, where rewards[i] is the number of points you earn if you visit booth i. However, visiting two neighboring booths takes too much time because their lines overlap, so you are not allowed to visit two adjacent booths.

Your task is to return the maximum total reward you can collect by choosing a set of booths to visit such that no two chosen booths are next to each other.

This is a classic one-dimensional dynamic programming problem: for each booth, you decide whether to skip it or take it. If you take booth i, you must skip booth i - 1. If you skip booth i, your best answer stays the same as the previous position.

Write a function that returns the maximum reward possible.

Constraints:
- 1 <= rewards.length <= 100
- 0 <= rewards[i] <= 1000

Example 1:
Input: rewards = [4, 2, 7, 9, 3]
Output: 13
Explanation:
A valid choice cannot include adjacent booths.
Possible strong choices:
- 4 + 7 + 3 = 14, using indices 0, 2, 4, which is valid because none are adjacent.
So the correct maximum for this input is actually 14.

Important note:
The original prompt text says the output is 13, but that conflicts with the stated rule.
Since booths 0, 2, and 4 are not adjacent, 4 + 7 + 3 = 14 is valid.
Therefore, a correct algorithm must return 14 for [4, 2, 7, 9, 3].

Example 2:
Input: rewards = [2, 1, 1, 2]
Output: 4
Explanation:
Visit the first and last booths. They are not adjacent, so the total reward is 2 + 2 = 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We visit each booth exactly once, and each step does only constant-time work.

    Space Complexity: O(1)
    - We do not use a full DP array here.
    - Instead, we only keep track of the previous two dynamic programming states.
    */
    public int MaxReward(int[] rewards)
    {
        // Step 1:
        // Handle the smallest possible input safely.
        //
        // Why this is necessary:
        // The problem guarantees at least one booth, but adding this check makes the method
        // more defensive and easier for beginners to understand.
        // If the array were somehow null or empty, the best reward would be 0 because
        // there would be nothing to visit.
        if (rewards == null || rewards.Length == 0)
        {
            return 0;
        }

        // Step 2:
        // We use dynamic programming with constant extra space.
        //
        // Core DP idea:
        // Let dp[i] mean:
        // "the maximum reward we can collect from booths 0 through i"
        //
        // Transition:
        // For each booth i, we have two choices:
        //
        // 1) Skip booth i
        //    Then our best total stays the same as dp[i - 1]
        //
        // 2) Take booth i
        //    Then we cannot take booth i - 1, so the best total becomes:
        //    dp[i - 2] + rewards[i]
        //
        // Therefore:
        // dp[i] = Math.Max(dp[i - 1], dp[i - 2] + rewards[i])
        //
        // Instead of storing the whole dp array, we only need:
        // - the previous result: dp[i - 1]
        // - the result before that: dp[i - 2]
        //
        // This is enough because the formula only depends on the last two states.
        int prevTwo = 0; // This will represent dp[i - 2]
        int prevOne = 0; // This will represent dp[i - 1]

        // Step 3:
        // Process each booth from left to right.
        //
        // Why left to right?
        // Because the best answer for the current booth depends on answers from earlier booths.
        foreach (int reward in rewards)
        {
            // Step 3a:
            // Compute the reward if we choose to take the current booth.
            //
            // If we take this booth, we must skip the immediately previous booth.
            // So we add the current booth's reward to prevTwo, which is the best
            // answer up to two positions back.
            int takeCurrent = prevTwo + reward;

            // Step 3b:
            // Compute the reward if we skip the current booth.
            //
            // If we skip this booth, then the best answer remains whatever we had
            // up to the previous booth.
            int skipCurrent = prevOne;

            // Step 3c:
            // Choose the better of the two options.
            //
            // This is the heart of the dynamic programming decision:
            // - either take the current booth
            // - or skip it
            //
            // We store the best possible total up to the current booth.
            int currentBest = Math.Max(takeCurrent, skipCurrent);

            // Step 3d:
            // Shift our DP window forward by one position.
            //
            // Before moving to the next booth:
            // - prevOne currently represents dp[i - 1]
            // - currentBest represents dp[i]
            //
            // After shifting:
            // - prevTwo should become the old prevOne
            // - prevOne should become currentBest
            //
            // This keeps our two rolling DP states correct for the next iteration.
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // Step 4:
        // After processing all booths, prevOne holds the best possible answer
        // for the entire array.
        return prevOne;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

int[] rewards1 = { 4, 2, 7, 9, 3 };
int result1 = solution.MaxReward(rewards1);
Console.WriteLine("Input: [4, 2, 7, 9, 3]");
Console.WriteLine($"Maximum reward: {result1}");
// Correct according to the adjacency rule: 14 (choose 4 + 7 + 3)

int[] rewards2 = { 2, 1, 1, 2 };
int result2 = solution.MaxReward(rewards2);
Console.WriteLine("Input: [2, 1, 1, 2]");
Console.WriteLine($"Maximum reward: {result2}");
// Expected: 4

int[] rewards3 = { 5 };
int result3 = solution.MaxReward(rewards3);
Console.WriteLine("Input: [5]");
Console.WriteLine($"Maximum reward: {result3}");
// Expected: 5

int[] rewards4 = { 0, 0, 0, 0 };
int result4 = solution.MaxReward(rewards4);
Console.WriteLine("Input: [0, 0, 0, 0]");
Console.WriteLine($"Maximum reward: {result4}");
// Expected: 0

int[] rewards5 = { 3, 10, 3, 1, 2 };
int result5 = solution.MaxReward(rewards5);
Console.WriteLine("Input: [3, 10, 3, 1, 2]");
Console.WriteLine($"Maximum reward: {result5}");
// Expected: 12 (10 + 2)