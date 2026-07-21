/*
Title: Maximum Visible Booths Between Taller Buildings
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array heights where heights[i] represents the height of the i-th building in a straight line.
A pop-up booth can be placed on top of any building i.

From building i, the booth is considered enclosed if there exists:
- at least one taller building on its left, and
- at least one taller building on its right.

The visibility score of building i is the number of consecutive buildings strictly between the nearest taller building
on the left and the nearest taller building on the right.

Formally:
- Let L be the closest index less than i such that heights[L] > heights[i]
- Let R be the closest index greater than i such that heights[R] > heights[i]

If both L and R exist, then:
score(i) = R - L - 1

If either side does not have a taller building, then score(i) = 0.

Task:
Return the maximum visibility score among all buildings.

Important note:
Equal-height buildings are NOT considered taller. Only strictly greater heights qualify.

Examples:
1) heights = [5, 2, 4, 3, 6]
   - i = 1, height = 2: left taller = 0, right taller = 2, score = 2 - 0 - 1 = 1
   - i = 2, height = 4: left taller = 0, right taller = 4, score = 4 - 0 - 1 = 3
   - i = 3, height = 3: left taller = 2, right taller = 4, score = 4 - 2 - 1 = 1
   Maximum = 3

2) heights = [7, 1, 5, 2, 4, 8]
   - i = 1, height = 1: left taller = 0, right taller = 2, score = 2 - 0 - 1 = 1
   - i = 2, height = 5: left taller = 0, right taller = 5, score = 5 - 0 - 1 = 4
   - i = 3, height = 2: left taller = 2, right taller = 4, score = 4 - 2 - 1 = 1
   - i = 4, height = 4: left taller = 2, right taller = 5, score = 5 - 2 - 1 = 2
   Maximum = 4

Constraints:
- 1 <= heights.length <= 200000
- 1 <= heights[i] <= 1000000000
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Why?
    - We scan from left to right once to find the nearest taller building on the left.
    - We scan from right to left once to find the nearest taller building on the right.
    - Each index is pushed to and popped from the stack at most one time in each pass.
    - Therefore the total work is linear.

    Core idea:
    We need the nearest strictly greater element on both sides for every position.
    A monotonic stack is the standard linear-time tool for this kind of "nearest greater" problem.
    */
    public int MaxVisibilityScore(int[] heights)
    {
        int n = heights.Length;

        // These arrays will store the nearest taller building index on each side.
        // If no taller building exists on that side, we store -1.
        int[] leftGreater = new int[n];
        int[] rightGreater = new int[n];

        // Initialize with -1 so "not found" is easy to detect later.
        Array.Fill(leftGreater, -1);
        Array.Fill(rightGreater, -1);

        // ------------------------------------------------------------
        // STEP 1: Find nearest taller building on the LEFT for each index
        // ------------------------------------------------------------
        //
        // We use a stack of indices.
        // The stack will be maintained so that heights at those indices are in
        // strictly decreasing order from bottom to top with respect to usefulness.
        //
        // For the current building i:
        // - While the top of the stack is NOT taller than heights[i],
        //   it cannot be the nearest taller building for i.
        //   Specifically, if heights[stackTop] <= heights[i], then it is not strictly greater,
        //   so it does not qualify.
        //
        // After removing all such indices:
        // - If the stack is not empty, the top is the nearest index to the left
        //   with height strictly greater than heights[i].
        //
        // Then we push i onto the stack so it can help future buildings.
        Stack<int> stack = new Stack<int>();

        for (int i = 0; i < n; i++)
        {
            // Remove all buildings that are not strictly taller than the current building.
            // Why is this safe?
            // Because if heights[stack.Peek()] <= heights[i], then that building cannot be
            // the nearest taller building for i, and it also becomes less useful for future
            // positions compared to the current building which is at least as tall and closer.
            while (stack.Count > 0 && heights[stack.Peek()] <= heights[i])
            {
                stack.Pop();
            }

            // If the stack still has something, its top is the closest taller building on the left.
            if (stack.Count > 0)
            {
                leftGreater[i] = stack.Peek();
            }

            // Push current index so it may serve as a candidate for buildings to the right.
            stack.Push(i);
        }

        // ------------------------------------------------------------
        // STEP 2: Find nearest taller building on the RIGHT for each index
        // ------------------------------------------------------------
        //
        // We clear the stack and repeat the same idea, but now scanning from right to left.
        // This gives us the nearest strictly greater element on the right.
        stack.Clear();

        for (int i = n - 1; i >= 0; i--)
        {
            // Remove all buildings that are not strictly taller than the current building.
            // Same reasoning as the left pass.
            while (stack.Count > 0 && heights[stack.Peek()] <= heights[i])
            {
                stack.Pop();
            }

            // If stack is not empty, the top is the nearest taller building on the right.
            if (stack.Count > 0)
            {
                rightGreater[i] = stack.Peek();
            }

            // Push current index for future buildings to the left.
            stack.Push(i);
        }

        // ------------------------------------------------------------
        // STEP 3: Compute the maximum visibility score
        // ------------------------------------------------------------
        //
        // For each building i:
        // - If both leftGreater[i] and rightGreater[i] exist,
        //   score = rightGreater[i] - leftGreater[i] - 1
        // - Otherwise score = 0
        //
        // We keep the maximum over all buildings.
        int maxScore = 0;

        for (int i = 0; i < n; i++)
        {
            // A valid enclosed booth needs a taller building on BOTH sides.
            if (leftGreater[i] != -1 && rightGreater[i] != -1)
            {
                int score = rightGreater[i] - leftGreater[i] - 1;

                if (score > maxScore)
                {
                    maxScore = score;
                }
            }
        }

        return maxScore;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

int[] heights1 = { 5, 2, 4, 3, 6 };
int result1 = solution.MaxVisibilityScore(heights1);
Console.WriteLine("Input: [5, 2, 4, 3, 6]");
Console.WriteLine("Output: " + result1);
Console.WriteLine("Expected: 3");
Console.WriteLine();

int[] heights2 = { 7, 1, 5, 2, 4, 8 };
int result2 = solution.MaxVisibilityScore(heights2);
Console.WriteLine("Input: [7, 1, 5, 2, 4, 8]");
Console.WriteLine("Output: " + result2);
Console.WriteLine("Expected: 4");
Console.WriteLine();

int[] heights3 = { 3 };
int result3 = solution.MaxVisibilityScore(heights3);
Console.WriteLine("Input: [3]");
Console.WriteLine("Output: " + result3);
Console.WriteLine("Expected: 0");
Console.WriteLine();

int[] heights4 = { 4, 1, 4 };
int result4 = solution.MaxVisibilityScore(heights4);
Console.WriteLine("Input: [4, 1, 4]");
Console.WriteLine("Output: " + result4);
Console.WriteLine("Expected: 0");
Console.WriteLine("Reason: equal heights are not taller, so index 1 has no strictly taller building on both sides.");
Console.WriteLine();

int[] heights5 = { 9, 2, 7, 3, 8, 1, 10 };
int result5 = solution.MaxVisibilityScore(heights5);
Console.WriteLine("Input: [9, 2, 7, 3, 8, 1, 10]");
Console.WriteLine("Output: " + result5);