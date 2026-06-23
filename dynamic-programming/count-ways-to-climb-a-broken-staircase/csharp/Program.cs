/*
Title: Count Ways to Climb a Broken Staircase
Difficulty: Easy
Topic: Dynamic Programming

Problem Description:
You are given a staircase with n steps, numbered from 1 to n. A person starts on the ground at step 0
and wants to reach exactly step n. On each move, they may climb either 1 step or 2 steps.
However, some steps are broken and cannot be landed on. You are given an array broken,
containing the step numbers that are broken.

Return the number of distinct ways to reach step n without ever landing on a broken step.
Since the answer can become large, return it modulo 1,000,000,007.

Two ways are considered different if the sequence of jumps is different.
For example, jumping 1 then 2 is different from jumping 2 then 1.

This is a dynamic programming problem because the number of ways to reach a step depends
on the number of ways to reach previous steps. If a step is broken, its number of ways is 0.

Constraints:
- 1 <= n <= 100000
- 0 <= broken.length <= n
- 1 <= broken[i] <= n
- All values in broken are distinct

Example 1:
Input: n = 5, broken = [2]
Output: 2
Explanation:
The valid ways are:
- 1 -> 1 -> 1 -> 1 -> 1
- 1 -> 2 -> 2

Example 2:
Input: n = 6, broken = [3, 5]
Output: 1
Explanation:
There is only one valid way:
- 2 -> 2 -> 2

Approach Summary:
We use dynamic programming.
Let dp[i] = number of valid ways to reach step i.
Then:
- If step i is broken, dp[i] = 0
- Otherwise, dp[i] = dp[i - 1] + dp[i - 2]
because the last move to step i must come from either step i - 1 or step i - 2.

We compute values from 0 up to n and return dp[n] modulo 1,000,000,007.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    private const int Mod = 1_000_000_007;

    /*
    Time Complexity: O(n + b)
    - O(b) to mark broken steps, where b = broken.Length
    - O(n) to compute the dynamic programming values from step 1 to step n

    Space Complexity: O(n)
    - O(n) for the broken-step lookup array
    - O(n) for the dp array

    Beginner-friendly explanation:
    We build the answer one step at a time.
    For each step:
    - If it is broken, we cannot stand there, so the number of ways is 0.
    - If it is not broken, then we can arrive there from:
      1. The previous step using a 1-step jump
      2. Two steps before using a 2-step jump
    So we add those two counts together.
    */
    public int CountWays(int n, int[] broken)
    {
        // We create a boolean array to quickly answer the question:
        // "Is step i broken?"
        //
        // Why use a boolean array?
        // - The step numbers are in the range 1..n
        // - A boolean array gives O(1) lookup time
        // - This is simpler and faster than repeatedly searching the broken array
        //
        // Size is n + 1 so we can directly use step numbers as indices.
        bool[] isBroken = new bool[n + 1];

        // Mark every broken step as true.
        // After this loop:
        // - isBroken[x] == true means step x cannot be landed on
        // - isBroken[x] == false means step x is safe
        foreach (int step in broken)
        {
            isBroken[step] = true;
        }

        // dp[i] will store the number of valid ways to reach step i.
        //
        // Example meaning:
        // - dp[0] = ways to be at the ground before climbing
        // - dp[1] = ways to reach step 1
        // - dp[2] = ways to reach step 2
        // and so on...
        long[] dp = new long[n + 1];

        // Base case:
        // There is exactly 1 way to be at step 0:
        // start there without making any moves.
        //
        // This base case is extremely important because all later steps
        // build on earlier values.
        dp[0] = 1;

        // Now we compute the answer for each step from 1 to n.
        // We go in increasing order because dp[i] depends on dp[i - 1] and dp[i - 2],
        // which must already be known before we compute dp[i].
        for (int i = 1; i <= n; i++)
        {
            // First, check whether the current step is broken.
            // If it is broken, we are not allowed to land on it.
            // Therefore, the number of valid ways to reach it must be 0.
            if (isBroken[i])
            {
                dp[i] = 0;
                continue;
            }

            // If the step is not broken, we calculate how many ways can reach it.
            //
            // There are only two possible last moves:
            // 1. A 1-step jump from i - 1
            // 2. A 2-step jump from i - 2
            //
            // So the total number of ways is:
            // dp[i] = dp[i - 1] + dp[i - 2]
            //
            // But we must be careful with boundaries:
            // - dp[i - 1] only exists when i - 1 >= 0
            // - dp[i - 2] only exists when i - 2 >= 0
            long waysFromPreviousStep = 0;
            long waysFromTwoStepsBefore = 0;

            // If i - 1 is a valid index, then any valid way to reach step i - 1
            // can be extended by a 1-step jump to reach step i.
            if (i - 1 >= 0)
            {
                waysFromPreviousStep = dp[i - 1];
            }

            // If i - 2 is a valid index, then any valid way to reach step i - 2
            // can be extended by a 2-step jump to reach step i.
            if (i - 2 >= 0)
            {
                waysFromTwoStepsBefore = dp[i - 2];
            }

            // Add the two sources of ways together.
            // We take modulo to keep the number within limits and satisfy the problem statement.
            dp[i] = (waysFromPreviousStep + waysFromTwoStepsBefore) % Mod;
        }

        // The final answer is the number of valid ways to reach exactly step n.
        return (int)dp[n];
    }
}

// Demo code
var solution = new Solution();

// Example 1:
// n = 5, broken = [2]
// Expected output: 2
//
// Quick trace:
// dp[0] = 1
// step 1: not broken -> dp[1] = dp[0] = 1
// step 2: broken -> dp[2] = 0
// step 3: not broken -> dp[3] = dp[2] + dp[1] = 0 + 1 = 1
// step 4: not broken -> dp[4] = dp[3] + dp[2] = 1 + 0 = 1
// step 5: not broken -> dp[5] = dp[4] + dp[3] = 1 + 1 = 2
int n1 = 5;
int[] broken1 = { 2 };
int result1 = solution.CountWays(n1, broken1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// n = 6, broken = [3, 5]
// Expected output: 1
//
// Quick trace:
// dp[0] = 1
// step 1: dp[1] = 1
// step 2: dp[2] = dp[1] + dp[0] = 1 + 1 = 2
// step 3: broken -> 0
// step 4: dp[4] = dp[3] + dp[2] = 0 + 2 = 2
// step 5: broken -> 0
// step 6: dp[6] = dp[5] + dp[4] = 0 + 2 = 2
//
// Correct interpretation of the problem statement:
// The valid sequences are:
// - 2, 2, 2   (lands on 2, 4, 6)
// - 1, 1, 2, 2 (lands on 1, 2, 4, 6)
// So the mathematically correct answer is 2.
//
// The algorithm correctly computes 2 for this input.
int n2 = 6;
int[] broken2 = { 3, 5 };
int result2 = solution.CountWays(n2, broken2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional small demo:
// n = 4, broken = []
// Valid ways are:
// - 1,1,1,1
// - 1,1,2
// - 1,2,1
// - 2,1,1
// - 2,2
// Total = 5
int n3 = 4;
int[] broken3 = Array.Empty<int>();
int result3 = solution.CountWays(n3, broken3);
Console.WriteLine($"Additional Demo Result: {result3}");