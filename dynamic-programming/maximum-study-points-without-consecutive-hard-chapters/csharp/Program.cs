/*
Title: Maximum Study Points Without Consecutive Hard Chapters
Difficulty: Easy
Topic: Dynamic Programming

Problem Description:
You are preparing for an exam and have a list of chapters to study in order. Each chapter gives you
a certain number of study points if you choose to review it. However, reviewing two adjacent chapters
in the same session is too tiring because both require full concentration. To keep your session manageable,
you may not choose two consecutive chapters.

Given an integer array points where points[i] is the number of study points earned by reviewing chapter i,
return the maximum total study points you can earn.

You may choose to skip any chapter, and you do not need to review the last chapter. This is an optimization
problem where each decision depends on previous choices, making it a good fit for dynamic programming.

Constraints:
- 1 <= points.length <= 100
- 0 <= points[i] <= 1000

Example 1:
Input: points = [3, 2, 5, 10, 7]
Output: 15
Explanation: Review chapters with points 3, 5, and 7. Their indices are not adjacent, so the total is 15.

Example 2:
Input: points = [2, 1, 4, 9]
Output: 11
Explanation: The best choice is to review chapters with points 2 and 9 for a total of 11. Choosing 1 and 9 gives 10,
and choosing 2 and 4 gives 6.

Goal:
Compute the maximum achievable total efficiently using dynamic programming.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We process each chapter exactly once.

    Space Complexity: O(1)
    - We only keep track of two previous dynamic programming states instead of storing a full DP array.

    Idea:
    This is the classic "maximum sum with no adjacent elements" problem.

    For each chapter, we have two choices:
    1. Skip the current chapter:
       Then our total stays the same as the best answer up to the previous chapter.
    2. Take the current chapter:
       Then we must skip the previous chapter, so we add the current points to the best answer up to i - 2.

    Recurrence:
    dp[i] = max(dp[i - 1], dp[i - 2] + points[i])

    Instead of storing the full dp array, we optimize space by keeping:
    - prevTwo = dp[i - 2]
    - prevOne = dp[i - 1]
    */
    public int MaxStudyPoints(int[] points)
    {
        // This handles the smallest valid input safely.
        // If there is only one chapter, the best we can do is either take it or skip it.
        // Since points are non-negative, taking it is always at least as good as skipping.
        if (points.Length == 1)
        {
            return points[0];
        }

        // prevTwo represents the best answer considering chapters up to index i - 2.
        // At the beginning, for index 0, dp[0] = points[0].
        int prevTwo = points[0];

        // prevOne represents the best answer considering chapters up to index i - 1.
        // For the first two chapters:
        // - If we take chapter 0, total = points[0]
        // - If we take chapter 1, total = points[1]
        // - We cannot take both because they are adjacent
        // So the best answer for the first two chapters is the larger of the two.
        int prevOne = Math.Max(points[0], points[1]);

        // Now we process chapters starting from index 2, because indices 0 and 1
        // have already been used to initialize our dynamic programming state.
        for (int i = 2; i < points.Length; i++)
        {
            // Option 1: Skip the current chapter.
            // If we skip chapter i, then the best total remains whatever was best up to chapter i - 1.
            int skipCurrent = prevOne;

            // Option 2: Take the current chapter.
            // If we take chapter i, we are not allowed to take chapter i - 1.
            // Therefore, we add points[i] to the best total up to chapter i - 2.
            int takeCurrent = prevTwo + points[i];

            // Choose the better of the two options.
            // This is the core dynamic programming transition:
            // best up to i = max(skip current, take current)
            int currentBest = Math.Max(skipCurrent, takeCurrent);

            // Move the window forward for the next iteration:
            // - The old prevOne becomes prevTwo
            // - The newly computed currentBest becomes prevOne
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // After processing all chapters, prevOne stores the best possible answer
        // for the entire array.
        return prevOne;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] points1 = { 3, 2, 5, 10, 7 };
int result1 = solution.MaxStudyPoints(points1);
Console.WriteLine($"Input: [{string.Join(", ", points1)}]");
Console.WriteLine($"Maximum study points: {result1}");
Console.WriteLine("Expected: 15");
Console.WriteLine();

// Example 2
int[] points2 = { 2, 1, 4, 9 };
int result2 = solution.MaxStudyPoints(points2);
Console.WriteLine($"Input: [{string.Join(", ", points2)}]");
Console.WriteLine($"Maximum study points: {result2}");
Console.WriteLine("Expected: 11");
Console.WriteLine();

// Additional sample
int[] points3 = { 5 };
int result3 = solution.MaxStudyPoints(points3);
Console.WriteLine($"Input: [{string.Join(", ", points3)}]");
Console.WriteLine($"Maximum study points: {result3}");
Console.WriteLine("Expected: 5");