/*
Title: Maximum Loyalty Points from Skipping Consecutive Cafes
Difficulty: Easy
Topic: Dynamic Programming

Problem Description:
A commuter passes a row of cafes on the way to work. Each cafe offers a certain number of loyalty points if visited that day.
However, visiting two neighboring cafes on the same trip takes too much time, so the commuter is not allowed to collect
points from two consecutive cafes.

You are given an integer array points where points[i] is the number of loyalty points available at the i-th cafe.
Return the maximum total number of points the commuter can collect while following the rule that no two chosen cafes are adjacent.

This is a classic decision problem with a simple dynamic programming pattern:
at each cafe, either skip it and keep the best total so far, or visit it and add its points
to the best total from two cafes earlier.

Constraints:
- 1 <= points.length <= 100
- 0 <= points[i] <= 1000

Example 1:
Input: points = [5, 1, 2, 10]
Output: 15
Explanation: Visit cafe 0 and cafe 3 for a total of 5 + 10 = 15.

Example 2:
Input: points = [2, 7, 9, 3, 1]
Output: 12
Explanation: The best choice is to visit cafes 0, 2, and 4. The total is 2 + 9 + 1 = 12.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We visit each cafe exactly once, and each step does only constant-time work.

    Space Complexity: O(1)
    - We do not need a full DP array.
    - We only keep track of the best answers for the previous one and previous two positions.
    */
    public int MaxLoyaltyPoints(int[] points)
    {
        // This variable represents the best total points we can collect
        // considering cafes up to index i - 2.
        //
        // Why do we need this?
        // If we decide to visit the current cafe, we are not allowed to visit
        // the immediately previous cafe because adjacent cafes cannot both be chosen.
        // So the best compatible total comes from two positions back.
        int prevTwo = 0;

        // This variable represents the best total points we can collect
        // considering cafes up to index i - 1.
        //
        // Why do we need this?
        // If we decide to skip the current cafe, then the answer simply remains
        // whatever the best total was up to the previous cafe.
        int prevOne = 0;

        // We now process each cafe from left to right.
        //
        // Dynamic programming idea:
        // At every cafe, we have exactly two choices:
        //
        // 1. Skip this cafe
        //    - Then our total remains prevOne
        //
        // 2. Visit this cafe
        //    - Then we add this cafe's points to prevTwo
        //    - We use prevTwo because visiting the current cafe means
        //      we must not have visited the previous adjacent cafe
        //
        // The best answer at this position is the maximum of those two choices.
        foreach (int point in points)
        {
            // Choice 1: skip the current cafe.
            //
            // This means we keep the best total we already had after processing
            // the previous cafe.
            int skipCurrent = prevOne;

            // Choice 2: visit the current cafe.
            //
            // Since we cannot take two adjacent cafes, the best total we can combine
            // with the current cafe is the best total from two cafes ago.
            int takeCurrent = prevTwo + point;

            // The best answer after considering this cafe is whichever choice
            // gives more points.
            int currentBest = Math.Max(skipCurrent, takeCurrent);

            // Now we shift our rolling variables forward for the next iteration.
            //
            // Before moving on:
            // - prevOne was the best answer up to the previous cafe
            // - currentBest is the best answer up to the current cafe
            //
            // For the next loop:
            // - prevTwo should become the old prevOne
            // - prevOne should become currentBest
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // After processing all cafes, prevOne holds the best total possible
        // for the entire array.
        return prevOne;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the problem statement
int[] points1 = { 5, 1, 2, 10 };
int result1 = solution.MaxLoyaltyPoints(points1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 15

// Example 2 from the problem statement
int[] points2 = { 2, 7, 9, 3, 1 };
int result2 = solution.MaxLoyaltyPoints(points2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 12

// Additional sample input
int[] points3 = { 4, 1, 1, 9, 1 };
int result3 = solution.MaxLoyaltyPoints(points3);
Console.WriteLine($"Example 3 Result: {result3}"); // Expected: 13

// Quick correctness trace notes:
//
// Example 1: [5, 1, 2, 10]
// Best choices evolve as:
// after 5  -> 5
// after 1  -> max(5, 1) = 5
// after 2  -> max(5, 5+2? actually prevTwo at that step gives 5? result becomes 7)
// after 10 -> max(7, 5+10) = 15
//
// Final answer: 15
//
// Example 2: [2, 7, 9, 3, 1]
// after 2  -> 2
// after 7  -> 7
// after 9  -> 11
// after 3  -> 11
// after 1  -> 12
//
// Final answer: 12