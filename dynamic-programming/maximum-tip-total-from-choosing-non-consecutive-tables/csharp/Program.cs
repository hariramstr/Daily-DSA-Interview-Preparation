/*
Title: Maximum Tip Total from Choosing Non-Consecutive Tables
Difficulty: Easy
Topic: Dynamic Programming

Problem Description:
A restaurant manager is planning which tables to assign to a single premium server during a busy evening.
The dining room has tables arranged in a straight line, and table i would generate tips[i] dollars if the
server handles it. However, to avoid delays, the server cannot be assigned two adjacent tables, because
neighboring tables tend to place orders at nearly the same time.

Your task is to return the maximum total tip amount the server can earn by choosing a subset of tables
such that no two chosen tables are adjacent.

You must decide for each table whether to skip it or assign it, while respecting the non-adjacent rule.
This is an optimization problem where a simple greedy choice does not always work, so a dynamic programming
approach is expected.

Constraints:
- 1 <= tips.length <= 100
- 0 <= tips[i] <= 1000
- The answer fits in a 32-bit signed integer

Example 1:
Input: tips = [5, 1, 8, 4, 7]
Output: 20
Explanation: Choose tables with tips 5, 8, and 7. These are at indices 0, 2, and 4, so no two chosen
tables are adjacent. The total is 5 + 8 + 7 = 20.

Example 2:
Input: tips = [10, 3, 2, 9]
Output: 19
Explanation: The best choice is tables with tips 10 and 9. Choosing 3 and 9 gives only 12, and choosing
10 and 2 gives only 12. So the maximum total tip is 19.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    We process the array once from left to right, so the running time is linear in the number of tables.
    We only store two rolling values instead of a full DP array, so extra space stays constant.

    Beginner-friendly idea:
    At every table, we have exactly two choices:
    1. Skip this table -> total stays whatever was best up to the previous table.
    2. Take this table -> then we must skip the previous table, so we add this tip to the best total
       from two tables back.

    The best answer at each step is the larger of those two choices.
    */
    public int MaxTip(int[] tips)
    {
        // This variable represents the best total tip we can earn considering tables up to index i - 2.
        // In other words, it stores the "two steps back" dynamic programming value.
        // We need this because if we choose the current table, we are not allowed to choose the previous one.
        int prevTwo = 0;

        // This variable represents the best total tip we can earn considering tables up to index i - 1.
        // It is the "one step back" dynamic programming value.
        // This is needed because if we skip the current table, the best answer is simply the best answer
        // we already had from the previous table.
        int prevOne = 0;

        // We now scan through each table from left to right.
        // This order is important because the decision for the current table depends only on earlier tables,
        // not on future ones.
        foreach (int tip in tips)
        {
            // Option 1: Skip the current table.
            // If we do not assign this table to the server, then the best total remains exactly the same
            // as the best total we had after processing the previous table.
            int skipCurrent = prevOne;

            // Option 2: Take the current table.
            // If we assign this table, we are forbidden from taking the immediately previous table.
            // Therefore, the best valid total becomes:
            // best total from two tables ago + current table's tip.
            int takeCurrent = prevTwo + tip;

            // The best total after considering this table is whichever option gives more money:
            // skipping it or taking it.
            int currentBest = Math.Max(skipCurrent, takeCurrent);

            // Before moving to the next table, we shift our rolling DP window forward.
            //
            // The old prevOne becomes the new prevTwo because on the next iteration,
            // today's "previous table result" will become "two tables back".
            prevTwo = prevOne;

            // The newly computed best result for the current table becomes prevOne,
            // because on the next iteration it will represent the best answer up to the previous table.
            prevOne = currentBest;
        }

        // After processing all tables, prevOne stores the best total tip for the entire array.
        return prevOne;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the problem statement:
// tips = [5, 1, 8, 4, 7]
// Best choice: 5 + 8 + 7 = 20
int[] tips1 = { 5, 1, 8, 4, 7 };
int result1 = solution.MaxTip(tips1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2 from the problem statement:
// tips = [10, 3, 2, 9]
// Best choice: 10 + 9 = 19
int[] tips2 = { 10, 3, 2, 9 };
int result2 = solution.MaxTip(tips2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo cases for learning and confidence:

// Single table: only one possible choice
int[] tips3 = { 12 };
int result3 = solution.MaxTip(tips3);
Console.WriteLine($"Single Table Result: {result3}");

// Includes zeros
int[] tips4 = { 0, 0, 10, 0, 20 };
int result4 = solution.MaxTip(tips4);
Console.WriteLine($"With Zeros Result: {result4}");

// Another common pattern
int[] tips5 = { 2, 7, 9, 3, 1 };
int result5 = solution.MaxTip(tips5);
Console.WriteLine($"Additional Example Result: {result5}");