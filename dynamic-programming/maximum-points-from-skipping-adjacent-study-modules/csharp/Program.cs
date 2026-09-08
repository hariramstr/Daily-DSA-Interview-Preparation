/*
Title: Maximum Points from Skipping Adjacent Study Modules

Problem Description:
You are preparing for a certification exam. The course platform shows a list of study modules,
and each module gives a certain number of points if you complete it. However, completing two
adjacent modules on the same day causes too much fatigue, so you are not allowed to complete
both module i and module i + 1.

Given an integer array points where points[i] is the score earned by completing the i-th module,
return the maximum total points you can earn under this rule.

You may choose to skip any module. If the array is empty, the answer is 0.

This is a classic dynamic programming decision process: for each module, you either skip it and
keep the best score so far, or complete it and add its points to the best score from two modules
earlier. Your task is to compute the best possible total.

Constraints:
- 0 <= points.length <= 100
- 0 <= points[i] <= 1000

Example 1:
Input: points = [4, 2, 7, 3, 9]
Output: 20
Explanation: Complete modules with scores 4, 7, and 9. These are not adjacent, and the total is 20.

Example 2:
Input: points = [5, 1, 1, 5]
Output: 10
Explanation: Complete the first and last modules. Completing both middle modules would block better choices.
The maximum total is 10.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We visit each module exactly once, and each step does only constant-time work.

    Space Complexity: O(1)
    - We do not use a full DP array here.
    - Instead, we keep only the last two dynamic programming states, which is enough
      because each decision depends only on:
        1) the best result up to the previous module
        2) the best result up to two modules before
    */
    public int MaxPoints(int[] points)
    {
        // Step 1: Handle the simplest possible input.
        // If there are no modules at all, then there is nothing to complete,
        // so the maximum score must be 0.
        // This check is necessary to avoid unnecessary processing and to correctly
        // satisfy the problem statement for an empty array.
        if (points == null || points.Length == 0)
        {
            return 0;
        }

        // We use dynamic programming, but in a space-optimized form.
        //
        // Core idea:
        // For each module, we have two choices:
        // 1) Skip the current module:
        //    Then our best score stays the same as the best score up to the previous module.
        //
        // 2) Complete the current module:
        //    Then we cannot complete the previous module, so we add the current module's points
        //    to the best score from two modules earlier.
        //
        // Recurrence:
        // dp[i] = max(dp[i - 1], dp[i - 2] + points[i])
        //
        // Instead of storing the whole dp array, we only keep:
        // - prevTwo = dp[i - 2]
        // - prevOne = dp[i - 1]
        //
        // This is enough because the formula only needs the previous two results.

        // Step 2: Initialize the DP states before processing any modules.
        //
        // Think of:
        // prevTwo = best score up to "two positions before the current one"
        // prevOne = best score up to "one position before the current one"
        //
        // At the very beginning, before processing any module:
        // - best score before the array starts is 0
        // - best score up to the previous position is also 0
        //
        // These initial values make the loop logic clean and uniform.
        int prevTwo = 0;
        int prevOne = 0;

        // Step 3: Process each module one by one from left to right.
        // This order is important because each decision depends on already-computed
        // best answers from earlier modules.
        foreach (int currentPoints in points)
        {
            // Step 3a: Compute the score if we SKIP the current module.
            //
            // If we skip it, then nothing changes from the best answer we already had
            // for the previous module.
            int skipCurrent = prevOne;

            // Step 3b: Compute the score if we COMPLETE the current module.
            //
            // If we complete this module, we are forbidden from completing the immediately
            // previous one, so the best compatible earlier result is prevTwo.
            //
            // Therefore:
            // completeCurrent = best up to two modules ago + current module's points
            int completeCurrent = prevTwo + currentPoints;

            // Step 3c: Choose the better of the two options.
            //
            // This is the heart of the dynamic programming decision:
            // - either skipping is better
            // - or completing is better
            //
            // The maximum of these two becomes the best answer up to the current module.
            int currentBest = Math.Max(skipCurrent, completeCurrent);

            // Step 3d: Shift the DP window forward.
            //
            // Before moving to the next module:
            // - the old prevOne becomes the new prevTwo
            // - the currentBest becomes the new prevOne
            //
            // This update is necessary so that on the next iteration,
            // the variables correctly represent:
            // - dp[i - 2]
            // - dp[i - 1]
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // Step 4: After processing all modules, prevOne holds the best possible total score.
        return prevOne;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1:
// points = [4, 2, 7, 3, 9]
// Best choice is 4 + 7 + 9 = 20
int[] points1 = { 4, 2, 7, 3, 9 };
int result1 = solution.MaxPoints(points1);
Console.WriteLine("Example 1 Result: " + result1);

// Example 2:
// points = [5, 1, 1, 5]
// Best choice is 5 + 5 = 10
int[] points2 = { 5, 1, 1, 5 };
int result2 = solution.MaxPoints(points2);
Console.WriteLine("Example 2 Result: " + result2);

// Additional demo: empty array should return 0
int[] points3 = Array.Empty<int>();
int result3 = solution.MaxPoints(points3);
Console.WriteLine("Empty Array Result: " + result3);