/*
Title: Longest Shelf Segment With One Height Drop Repair
Difficulty: Medium
Topic: Arrays

Problem Description:
A warehouse stores boxes on a long shelf, and the height of the box at position i is given by heights[i].
A shelf segment is considered orderly if the heights in that contiguous segment are nondecreasing from left to right.
The operations team is allowed to repair at most one box inside a chosen segment by changing its height to any value they want.
After this optional single repair, the entire segment must become nondecreasing.

Your task is to return the length of the longest contiguous segment that can be made nondecreasing after changing at most one element in that segment.

A repair may also be skipped if the segment is already nondecreasing. The changed value does not need to appear elsewhere in the array, and it may be any integer.

This problem asks for the best segment length, not the repaired array itself.

Constraints:
- 1 <= heights.length <= 200000
- -1000000000 <= heights[i] <= 1000000000
- An O(n log n) solution or better is expected.

Example 1:
Input: heights = [3, 4, 2, 5, 6]
Output: 5
Explanation: Change the value 2 to 4 (or any value between 4 and 5). The full array becomes nondecreasing, so the answer is 5.

Example 2:
Input: heights = [7, 3, 5, 2, 6]
Output: 3
Explanation: One optimal segment is [3, 5, 2]. Change 2 to 5, giving [3, 5, 5], which is nondecreasing.
No segment of length 4 or 5 can be fixed with only one change.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    // Time Complexity: O(n)
    // Space Complexity: O(n)
    //
    // Idea in simple words:
    // We want the longest contiguous subarray that can become nondecreasing after changing at most one element.
    //
    // A subarray can be fixed with one change if:
    // 1) It is already nondecreasing, or
    // 2) There is exactly one "bad drop" inside it, and that drop can be repaired by changing one of the two elements
    //    involved in that drop.
    //
    // A "bad drop" is an index i such that heights[i] > heights[i + 1].
    //
    // Key observation:
    // If a subarray contains two or more bad drops, then one change cannot fix all of them in general,
    // because changing one element can only affect comparisons next to that element.
    //
    // So for a valid subarray, the number of bad drops inside it must be:
    // - 0, or
    // - 1 and that one drop must be repairable.
    //
    // We will:
    // - Precompute all drop positions.
    // - Use a sliding window [left..right].
    // - Maintain how many drops are inside the current window.
    // - If there are no drops, the window is already nondecreasing.
    // - If there is exactly one drop at position p, then the window is fixable iff:
    //      we can change heights[p]     so that heights[p-1] <= newValue <= heights[p+1]
    //   or we can change heights[p + 1] so that heights[p]   <= newValue <= heights[p+2]
    //   where only neighbors inside the window matter.
    //
    // Boundary handling:
    // - If p == left, then changing heights[p] has no left constraint from inside the window.
    // - If p + 1 == right, then changing heights[p + 1] has no right constraint from inside the window.
    // - That means edge drops are always easier to repair.
    //
    // This gives a clean O(n) solution.
    public int LongestSegmentWithOneRepair(int[] heights)
    {
        int n = heights.Length;

        // A single element is always nondecreasing.
        if (n <= 1)
            return n;

        // drop[i] means there is a decrease between i and i+1:
        // heights[i] > heights[i+1]
        //
        // Example:
        // heights = [3, 4, 2, 5]
        // drop[0] = false because 3 <= 4
        // drop[1] = true  because 4 > 2
        // drop[2] = false because 2 <= 5
        //
        // There are n-1 adjacent comparisons.
        bool[] drop = new bool[n - 1];

        for (int i = 0; i < n - 1; i++)
        {
            drop[i] = heights[i] > heights[i + 1];
        }

        // We will use a sliding window [left..right].
        //
        // dropsInWindow counts how many drop positions p satisfy:
        // left <= p < right
        // because p compares heights[p] and heights[p+1], both must lie inside the window.
        int left = 0;
        int dropsInWindow = 0;
        int answer = 1;

        // To quickly know where the single drop is when dropsInWindow == 1,
        // we maintain a deque-like structure of drop indices currently inside the window.
        //
        // Since left and right only move forward, a Queue<int> works perfectly.
        Queue<int> dropIndices = new Queue<int>();

        for (int right = 0; right < n; right++)
        {
            // Step 1:
            // Expand the window to include index "right".
            //
            // When we add a new element at position right, the only new adjacent comparison introduced is:
            // (right - 1, right)
            //
            // So if heights[right - 1] > heights[right], then a new drop at position right - 1
            // enters the window.
            if (right > 0 && drop[right - 1])
            {
                dropsInWindow++;
                dropIndices.Enqueue(right - 1);
            }

            // Step 2:
            // While the current window is NOT fixable with at most one change, move left forward.
            //
            // A window is invalid if:
            // - it has 2 or more drops, or
            // - it has exactly 1 drop but that drop is not repairable.
            while (!IsWindowFixable(heights, left, right, dropsInWindow, dropIndices))
            {
                // Before increasing left, we may be removing the adjacent comparison at position left.
                //
                // Why?
                // The drop at position left compares heights[left] and heights[left+1].
                // After moving left to left+1, that comparison is no longer fully inside the window.
                if (left < right && drop[left])
                {
                    dropsInWindow--;

                    // Because indices are enqueued in increasing order and left only moves forward,
                    // if drop[left] is leaving the window, it must be at the front of the queue.
                    if (dropIndices.Count > 0 && dropIndices.Peek() == left)
                    {
                        dropIndices.Dequeue();
                    }
                }

                left++;
            }

            // Step 3:
            // Now [left..right] is valid, so update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > answer)
            {
                answer = currentLength;
            }
        }

        return answer;
    }

    private bool IsWindowFixable(int[] heights, int left, int right, int dropsInWindow, Queue<int> dropIndices)
    {
        // Case 1:
        // No drops at all.
        // Then the window is already nondecreasing, so it is definitely valid.
        if (dropsInWindow == 0)
            return true;

        // Case 2:
        // Two or more drops.
        // One change cannot generally repair multiple separated decreases,
        // because changing one element only affects at most two adjacent comparisons.
        if (dropsInWindow >= 2)
            return false;

        // Case 3:
        // Exactly one drop.
        // Let that drop be at position p, meaning:
        // heights[p] > heights[p + 1]
        //
        // We need to check whether changing ONE element can make the whole window nondecreasing.
        int p = dropIndices.Peek();

        // There are only two meaningful choices:
        // - change heights[p]
        // - change heights[p + 1]
        //
        // Why only these two?
        // Because the only violated adjacent comparison is between p and p+1.
        // Changing any element far away does not fix that broken order.
        //
        // Option A: change heights[p]
        // Then we need a value x such that:
        // - if p > left, heights[p - 1] <= x
        // - x <= heights[p + 1]
        //
        // So this is possible iff:
        // - p == left, or
        // - heights[p - 1] <= heights[p + 1]
        bool canChangeLeftElement =
            (p == left) || (heights[p - 1] <= heights[p + 1]);

        // Option B: change heights[p + 1]
        // Then we need a value y such that:
        // - heights[p] <= y
        // - if p + 1 < right, y <= heights[p + 2]
        //
        // So this is possible iff:
        // - p + 1 == right, or
        // - heights[p] <= heights[p + 2]
        bool canChangeRightElement =
            (p + 1 == right) || (heights[p] <= heights[p + 2]);

        // If either option works, the window is fixable.
        return canChangeLeftElement || canChangeRightElement;
    }
}

// Demo code
var solution = new Solution();

int[] heights1 = { 3, 4, 2, 5, 6 };
int result1 = solution.LongestSegmentWithOneRepair(heights1);
Console.WriteLine(result1); // Expected: 5

int[] heights2 = { 7, 3, 5, 2, 6 };
int result2 = solution.LongestSegmentWithOneRepair(heights2);
Console.WriteLine(result2); // Expected: 3

int[] heights3 = { 1, 2, 3, 4 };
int result3 = solution.LongestSegmentWithOneRepair(heights3);
Console.WriteLine(result3); // Expected: 4

int[] heights4 = { 5, 4, 3, 2, 1 };
int result4 = solution.LongestSegmentWithOneRepair(heights4);
Console.WriteLine(result4); // Expected: 2

int[] heights5 = { 10 };
int result5 = solution.LongestSegmentWithOneRepair(heights5);
Console.WriteLine(result5); // Expected: 1