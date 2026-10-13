/*
Title: Closest Badge Pair Across Two Sorted Gates
Difficulty: Medium
Topic: Two Pointers

Problem Description:
A company campus has two security gates, Gate A and Gate B. Each gate logs badge IDs in non-decreasing order as employees pass through. You are given two sorted integer arrays gateA and gateB, where each value represents a badge ID seen at that gate. The same badge ID may appear multiple times in either array.

Your task is to find one pair of badge IDs (a, b) such that a comes from gateA, b comes from gateB, and the absolute difference |a - b| is as small as possible. Return that minimum absolute difference.

If either array is empty, return -1 because no cross-gate pair can be formed.

This problem should be solved efficiently by taking advantage of the fact that both arrays are already sorted. A brute-force solution that compares every pair may be too slow for large inputs.

Constraints:
- 0 <= gateA.length, gateB.length <= 2 * 10^5
- -10^9 <= gateA[i], gateB[i] <= 10^9
- gateA and gateB are each sorted in non-decreasing order
- Duplicate values are allowed

Example 1:
Input: gateA = [3, 8, 15, 21], gateB = [1, 9, 18, 30]
Output: 1
Explanation: The closest cross-gate pair is (8, 9), so the minimum absolute difference is 1.

Example 2:
Input: gateA = [-10, -4, 0, 7], gateB = [-6, -3, 2, 11]
Output: 1
Explanation: There are several close pairs, including (-4, -3). The best valid pair is (-4, -3), giving a difference of 1.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n + m)
    - We move through each array at most once using two pointers.
    - n = gateA.Length, m = gateB.Length

    Space Complexity:
    O(1)
    - We only use a few extra variables.
    - No additional data structures proportional to input size are created.
    */
    public int MinAbsoluteDifference(int[] gateA, int[] gateB)
    {
        // If either array is null or empty, we cannot form any pair where
        // one value comes from gateA and the other comes from gateB.
        // The problem explicitly says to return -1 in that case.
        if (gateA == null || gateB == null || gateA.Length == 0 || gateB.Length == 0)
        {
            return -1;
        }

        // i will point into gateA.
        // j will point into gateB.
        // Because both arrays are already sorted, we can compare current values
        // and intelligently decide which pointer to move.
        int i = 0;
        int j = 0;

        // We store the best (smallest) absolute difference found so far.
        // We use long during calculations to be extra safe when subtracting,
        // even though the final answer fits in int for the given constraints.
        long minDifference = long.MaxValue;

        // Continue while both pointers are still inside their arrays.
        // The moment one pointer reaches the end, no more cross-array pairs
        // can be formed using the two-pointer sweep.
        while (i < gateA.Length && j < gateB.Length)
        {
            // Read the current values from both arrays.
            int a = gateA[i];
            int b = gateB[j];

            // Compute the absolute difference of the current pair.
            // We cast to long before subtraction to avoid any risk of overflow
            // in more general settings.
            long currentDifference = Math.Abs((long)a - b);

            // If this current pair is better than anything we have seen before,
            // update our answer.
            if (currentDifference < minDifference)
            {
                minDifference = currentDifference;
            }

            // If the difference is 0, that means we found equal values in both arrays.
            // It is impossible to do better than 0, because absolute differences
            // can never be negative.
            // So we can return immediately.
            if (minDifference == 0)
            {
                return 0;
            }

            // Now comes the key two-pointer idea:
            //
            // Since both arrays are sorted:
            // - If a < b, then a is the smaller value.
            //   To possibly get a smaller difference, we should move i forward
            //   so that gateA gives us a larger value, potentially closer to b.
            //
            // - If a > b, then b is the smaller value.
            //   To possibly get a smaller difference, we should move j forward
            //   so that gateB gives us a larger value, potentially closer to a.
            //
            // Why not move the larger one?
            // Because moving the larger one forward would only make it stay large
            // or become even larger, which usually cannot help reduce the gap
            // with the smaller current value.
            if (a < b)
            {
                i++;
            }
            else
            {
                j++;
            }
        }

        // At this point, we have checked all relevant candidate pairs reachable
        // by the sorted two-pointer sweep, and minDifference holds the smallest
        // absolute difference found.
        return (int)minDifference;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// gateA = [3, 8, 15, 21], gateB = [1, 9, 18, 30]
// Closest pair is (8, 9), difference = 1
int[] gateA1 = { 3, 8, 15, 21 };
int[] gateB1 = { 1, 9, 18, 30 };
int result1 = solution.MinAbsoluteDifference(gateA1, gateB1);
Console.WriteLine(result1);

// Example 2:
// gateA = [-10, -4, 0, 7], gateB = [-6, -3, 2, 11]
// Closest pair is (-4, -3), difference = 1
int[] gateA2 = { -10, -4, 0, 7 };
int[] gateB2 = { -6, -3, 2, 11 };
int result2 = solution.MinAbsoluteDifference(gateA2, gateB2);
Console.WriteLine(result2);

// Empty array example:
// No pair can be formed, so answer is -1
int[] gateA3 = { };
int[] gateB3 = { 5, 10, 15 };
int result3 = solution.MinAbsoluteDifference(gateA3, gateB3);
Console.WriteLine(result3);