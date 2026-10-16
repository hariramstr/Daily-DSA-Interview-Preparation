/*
Title: Maximum Notes from Skipping Adjacent Lectures
Difficulty: Easy
Topic: Dynamic Programming

Problem Description:
You are planning your day at a university conference. There are n lectures scheduled in a single hallway, and each lecture has a note value representing how useful your notes would be if you attend that lecture. However, attending two adjacent lectures is not allowed because there is no transition time between neighboring rooms, so you must skip at least one lecture between any two lectures you choose.

Given an integer array notes where notes[i] is the note value of the i-th lecture, return the maximum total note value you can collect.

You may choose to attend no lectures at all, so the answer can be 0 if every value is negative. This makes the problem slightly more practical than simply forcing a choice.

A dynamic programming solution works well here. For each position, decide whether it is better to skip the current lecture or attend it and add its value to the best answer from two positions earlier.

Constraints:
- 1 <= notes.length <= 100000
- -1000 <= notes[i] <= 1000

Example 1:
Input: notes = [5, 1, 6, 2, 4]
Output: 15
Explanation: Attend lectures with values 5, 6, and 4. They are not adjacent, and the total is 15.

Example 2:
Input: notes = [4, -2, 3, 7]
Output: 11
Explanation: The best choice is to attend lectures with values 4 and 7 for a total of 11. Attending 3 and 7 is not allowed because they are adjacent.

Goal:
Compute the maximum possible sum efficiently, ideally in linear time.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We visit each lecture exactly once, and each step does only constant-time work.

    Space Complexity: O(1)
    - We do not build a full DP array.
    - We only keep the last two dynamic programming states needed to compute the next one.

    Beginner-friendly idea:
    Let dp[i] mean:
    "the maximum total note value we can collect from lectures 0..i, without taking adjacent lectures."

    Transition:
    For each lecture i, we have two choices:
    1. Skip lecture i
       => total stays dp[i - 1]
    2. Attend lecture i
       => then we cannot attend lecture i - 1,
          so total becomes dp[i - 2] + notes[i]

    Therefore:
    dp[i] = max(dp[i - 1], dp[i - 2] + notes[i])

    Important extra rule in this problem:
    We are allowed to attend no lectures at all.
    That means the answer should never be negative.
    So our running best values naturally start from 0.
    */
    public int MaxNotes(int[] notes)
    {
        // This variable represents the best answer up to index i - 2.
        // Think of it as "two steps back" in the DP recurrence.
        //
        // We initialize it to 0 because before processing any lecture,
        // the best total we can have is to attend nothing.
        int prevTwo = 0;

        // This variable represents the best answer up to index i - 1.
        // Think of it as "one step back" in the DP recurrence.
        //
        // It also starts at 0 for the same reason:
        // if we have not chosen any lecture yet, total value is 0.
        int prevOne = 0;

        // We now process each lecture from left to right.
        // This order is important because the decision for the current lecture
        // depends on already-computed best answers from earlier positions.
        foreach (int value in notes)
        {
            // Option 1: Skip the current lecture.
            //
            // If we skip it, then the best total does not change from the previous lecture.
            // That value is stored in prevOne.
            int skipCurrent = prevOne;

            // Option 2: Attend the current lecture.
            //
            // If we attend this lecture, we are not allowed to attend the immediately
            // previous lecture because adjacent selections are forbidden.
            //
            // Therefore, the best compatible total we can add is prevTwo,
            // which is the best answer from two positions back.
            int takeCurrent = prevTwo + value;

            // The best answer including decisions up to the current lecture
            // is the better of:
            // - skipping the current lecture
            // - taking the current lecture
            //
            // This is the core DP transition.
            int current = Math.Max(skipCurrent, takeCurrent);

            // Move the window forward:
            //
            // The old prevOne becomes the new prevTwo,
            // because on the next iteration it will represent "two steps back".
            prevTwo = prevOne;

            // The newly computed current becomes the new prevOne,
            // because on the next iteration it will represent "one step back".
            prevOne = current;
        }

        // prevOne now stores the best answer after processing all lectures.
        //
        // Because we started from 0 and always take maximums,
        // this naturally handles all-negative arrays correctly:
        // the result will remain 0, meaning "attend no lectures".
        return prevOne;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1:
// notes = [5, 1, 6, 2, 4]
// Best choice: 5 + 6 + 4 = 15
int[] notes1 = { 5, 1, 6, 2, 4 };
int result1 = solution.MaxNotes(notes1);
Console.WriteLine($"Input: [{string.Join(", ", notes1)}]");
Console.WriteLine($"Maximum notes: {result1}");
Console.WriteLine("Expected: 15");
Console.WriteLine();

// Example 2:
// notes = [4, -2, 3, 7]
// Best choice: 4 + 7 = 11
int[] notes2 = { 4, -2, 3, 7 };
int result2 = solution.MaxNotes(notes2);
Console.WriteLine($"Input: [{string.Join(", ", notes2)}]");
Console.WriteLine($"Maximum notes: {result2}");
Console.WriteLine("Expected: 11");
Console.WriteLine();

// Additional demo: all negative values
// Since attending no lectures is allowed, answer should be 0.
int[] notes3 = { -5, -1, -8 };
int result3 = solution.MaxNotes(notes3);
Console.WriteLine($"Input: [{string.Join(", ", notes3)}]");
Console.WriteLine($"Maximum notes: {result3}");
Console.WriteLine("Expected: 0");
Console.WriteLine();

// Additional demo: single lecture
int[] notes4 = { 9 };
int result4 = solution.MaxNotes(notes4);
Console.WriteLine($"Input: [{string.Join(", ", notes4)}]");
Console.WriteLine($"Maximum notes: {result4}");
Console.WriteLine("Expected: 9");