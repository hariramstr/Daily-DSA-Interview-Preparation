/*
Title: Count Visible Skyline Pairs in a Queue

Problem Description:
You are given an array heights of length n representing the heights of buildings standing in a straight line from left to right.
For every pair of indices (i, j) with i < j, building i and building j are said to be mutually visible if every building
strictly between them has height less than or equal to min(heights[i], heights[j]).

In other words, the shorter of the two endpoint buildings determines the maximum allowed height of all buildings between them.

Your task is to return the total number of mutually visible pairs.

A brute-force solution that checks every pair and scans the buildings in between is far too slow. The intended solution should
exploit the structure of the array using a hard two-pointers-style strategy or an equivalent linear-time boundary compression idea.

Note that adjacent buildings are always visible, because there are no buildings between them. Equal heights are allowed, and they
can still see each other as long as all intermediate heights satisfy the visibility rule.

Constraints:
- 1 <= n <= 200000
- 1 <= heights[i] <= 1000000000
- Return the answer as a 64-bit integer, since the number of visible pairs may exceed 32-bit range.

Example 1:
Input: heights = [5, 3, 4, 2, 6]
Output: 7
Explanation:
Visible pairs are (0,1), (0,2), (0,4), (1,2), (2,3), (2,4), and (3,4).

Example 2:
Input: heights = [4, 4, 4]
Output: 3
Explanation:
All pairs are visible.
- (0,1) and (1,2) are adjacent.
- (0,2) is also visible because the only building between them has height 4, which is <= min(4,4)=4.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Why this works:
    We process buildings from left to right and maintain a monotonic decreasing stack.
    Each stack entry stores:
    - Height of a group
    - How many consecutive buildings of that same height are currently compressed together

    Key idea:
    When a new building arrives, it can see:
    1. Every shorter height group on top of the stack, because those shorter buildings are not blocked anymore.
       We pop them and add their counts.
    2. If the next remaining stack top has the same height, it can see all buildings in that equal-height group.
       We add that group's count and merge with it.
       Also, if there is a taller building behind that equal group, the current building can see exactly one such taller building too.
    3. Otherwise, if the next remaining stack top is taller, it can see exactly that one taller building.

    This is the classic linear-time "visible pairs" / "oasis reunion" monotonic stack pattern,
    adapted to count all mutually visible pairs under the given rule.
    */
    public long CountVisiblePairs(int[] heights)
    {
        // This will store the final answer.
        // We use long because the number of pairs can be as large as n * (n - 1) / 2,
        // which does not fit in a 32-bit integer for n up to 200000.
        long visiblePairs = 0;

        // We use a stack of pairs: (height, countOfSameHeightTogether).
        //
        // Why store counts?
        // Suppose we have many equal heights in a row on the stack, like 4, 4, 4.
        // Instead of storing three separate entries, we compress them into one entry:
        // (4, 3)
        //
        // This is important because:
        // - It keeps the algorithm efficient.
        // - It lets us count all visible equal-height pairs in O(1) for that group.
        var stack = new Stack<(int Height, int Count)>();

        // Process each building from left to right.
        foreach (int currentHeight in heights)
        {
            // This variable starts as 1 because the current building itself forms a group
            // of one building with its own height.
            int currentCount = 1;

            // STEP 1:
            // Pop all strictly shorter buildings from the top of the stack.
            //
            // Why can the current building see all of them?
            // Because the current building is taller than those popped buildings,
            // and since they are on the top of the stack, there is no taller blocker
            // between them and the current building that would prevent visibility.
            //
            // Every building in a popped group contributes one visible pair with the current building.
            while (stack.Count > 0 && stack.Peek().Height < currentHeight)
            {
                var shorterGroup = stack.Pop();

                // The current building can see every building in this shorter group.
                visiblePairs += shorterGroup.Count;
            }

            // STEP 2:
            // After removing all shorter heights, there are three possibilities:
            //
            // A) Stack is empty
            //    -> No building remains to the left that can directly interact now.
            //
            // B) Top has the same height as currentHeight
            //    -> Current building can see all buildings in that equal-height group.
            //       Then we merge the current building into that group.
            //
            // C) Top has greater height
            //    -> Current building can see exactly one taller building: the nearest one on the left.
            //       It cannot see beyond that taller building because that taller building blocks further view.

            if (stack.Count > 0)
            {
                var top = stack.Peek();

                if (top.Height == currentHeight)
                {
                    // CASE B: Equal height group found.

                    // Remove the equal-height group so we can merge with it.
                    stack.Pop();

                    // The current building can see every building in that equal-height group.
                    // Example: heights [4,4,4]
                    // - second 4 sees first 4
                    // - third 4 sees both previous 4s
                    visiblePairs += top.Count;

                    // Merge current building into that equal-height group.
                    currentCount = top.Count + 1;

                    // Important subtlety:
                    // If there is still a building left on the stack after removing the equal group,
                    // then that remaining building must be taller (because all shorter ones were already popped,
                    // and equal one was just removed).
                    //
                    // The current building can also see exactly that one taller building behind the equal group.
                    //
                    // Why exactly one?
                    // Because the nearest taller building is visible, but it blocks any buildings further behind it.
                    //
                    // Example:
                    // Stack before current = [7, 4x3] and current = 4
                    // Current sees:
                    // - all 3 buildings of height 4
                    // - also the 7 behind them
                    if (stack.Count > 0)
                    {
                        visiblePairs += 1;
                    }
                }
                else
                {
                    // CASE C: Top is taller than current.
                    //
                    // The current building can see the nearest taller building.
                    // It cannot see beyond it, because that taller building blocks the view.
                    visiblePairs += 1;
                }
            }

            // STEP 3:
            // Push the current building (or merged equal-height group) onto the stack.
            //
            // The stack remains monotonic decreasing by height, with equal heights compressed.
            stack.Push((currentHeight, currentCount));
        }

        return visiblePairs;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] heights1 = { 5, 3, 4, 2, 6 };
long result1 = solution.CountVisiblePairs(heights1);
Console.WriteLine("Example 1:");
Console.WriteLine($"Input: [{string.Join(", ", heights1)}]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 7");
Console.WriteLine();

// Example 2
int[] heights2 = { 4, 4, 4 };
long result2 = solution.CountVisiblePairs(heights2);
Console.WriteLine("Example 2:");
Console.WriteLine($"Input: [{string.Join(", ", heights2)}]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Additional quick checks
int[] heights3 = { 1 };
long result3 = solution.CountVisiblePairs(heights3);
Console.WriteLine("Additional Test 1:");
Console.WriteLine($"Input: [{string.Join(", ", heights3)}]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 0");
Console.WriteLine();

int[] heights4 = { 1, 2, 3, 4 };
long result4 = solution.CountVisiblePairs(heights4);
Console.WriteLine("Additional Test 2:");
Console.WriteLine($"Input: [{string.Join(", ", heights4)}]");
Console.WriteLine($"Output: {result4}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

int[] heights5 = { 4, 3, 2, 1 };
long result5 = solution.CountVisiblePairs(heights5);
Console.WriteLine("Additional Test 3:");
Console.WriteLine($"Input: [{string.Join(", ", heights5)}]");
Console.WriteLine($"Output: {result5}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

int[] heights6 = { 2, 2, 1, 2 };
long result6 = solution.CountVisiblePairs(heights6);
Console.WriteLine("Additional Test 4:");
Console.WriteLine($"Input: [{string.Join(", ", heights6)}]");
Console.WriteLine($"Output: {result6}");
Console.WriteLine("Expected: 5");