/*
Title: Maximum Consecutive Shelves Within Height Budget
Difficulty: Medium
Topic: Arrays

Problem Description:
A warehouse manager wants to display a consecutive block of products on one long shelf.
The products are already arranged in a fixed order, and product i has height heights[i].
To make the display look neat, the manager may choose any contiguous subarray of products,
but the difference between the tallest and shortest product in that chosen block must be at most limit.

Your task is to return the length of the longest contiguous block that can be selected
while satisfying this height budget.

Formally, find the maximum length of a subarray heights[l..r] such that:
max(heights[l..r]) - min(heights[l..r]) <= limit.

The order of products cannot be changed, and you must choose a contiguous segment.

Constraints:
- 1 <= heights.length <= 100000
- 1 <= heights[i] <= 1000000000
- 0 <= limit <= 1000000000

Example 1:
Input: heights = [4, 7, 6, 8, 5, 9], limit = 3
Output: 4
Explanation: One valid longest block is [7, 6, 8, 5]. Its maximum is 8 and minimum is 5, so the difference is 3.
No contiguous block of length 5 satisfies the condition.

Example 2:
Input: heights = [10, 1, 2, 4, 7, 2], limit = 5
Output: 4
Explanation: The longest valid block is [2, 4, 7, 2]. Its maximum is 7 and minimum is 2, so the difference is 5.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is the number of products/heights.
    Each index is added to and removed from each deque at most one time.

    Space Complexity:
    O(n) in the worst case for the deques.
    */

    public int LongestSubarray(int[] heights, int limit)
    {
        // We will use the "sliding window" technique.
        //
        // The idea:
        // - We keep a window [left..right] that represents the current contiguous block.
        // - We expand the window by moving "right" one step at a time.
        // - If the window becomes invalid (max - min > limit), we shrink it from the left.
        //
        // The challenge:
        // We need to know the current minimum and maximum of the window efficiently.
        // Doing that by scanning the whole window every time would be too slow (O(n^2)).
        //
        // To solve this efficiently, we use two deques:
        // 1) minDeque: stores indices of elements in increasing order of values.
        //    - The front always points to the minimum value in the current window.
        // 2) maxDeque: stores indices of elements in decreasing order of values.
        //    - The front always points to the maximum value in the current window.
        //
        // Why store indices instead of values?
        // Because when the left side of the window moves forward, we need to know whether
        // the element leaving the window is currently sitting at the front of a deque.
        // Indices let us check that directly.

        int n = heights.Length;

        // These linked lists will act as deques.
        // We use:
        // - AddLast(...) to push to the back
        // - RemoveLast() to pop from the back
        // - First.Value to inspect the front
        // - RemoveFirst() to pop from the front
        LinkedList<int> minDeque = new LinkedList<int>();
        LinkedList<int> maxDeque = new LinkedList<int>();

        int left = 0;
        int bestLength = 0;

        // Move the right boundary of the window from left to right across the array.
        for (int right = 0; right < n; right++)
        {
            // ------------------------------------------------------------
            // STEP 1: Insert heights[right] into the min deque.
            // ------------------------------------------------------------
            //
            // Goal:
            // Keep minDeque values in increasing order.
            //
            // Why?
            // The smallest value should always be at the front.
            //
            // How?
            // While the new value is smaller than the values at the back,
            // those larger values can never become the minimum for any future window
            // that includes the new value, so we remove them.
            while (minDeque.Count > 0 && heights[minDeque.Last!.Value] > heights[right])
            {
                minDeque.RemoveLast();
            }

            // Now the deque remains increasing after we add this index.
            minDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert heights[right] into the max deque.
            // ------------------------------------------------------------
            //
            // Goal:
            // Keep maxDeque values in decreasing order.
            //
            // Why?
            // The largest value should always be at the front.
            //
            // How?
            // While the new value is larger than the values at the back,
            // those smaller values can never become the maximum for any future window
            // that includes the new value, so we remove them.
            while (maxDeque.Count > 0 && heights[maxDeque.Last!.Value] < heights[right])
            {
                maxDeque.RemoveLast();
            }

            // Now the deque remains decreasing after we add this index.
            maxDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 3: Check whether the current window is valid.
            // ------------------------------------------------------------
            //
            // Current minimum = heights[minDeque.First]
            // Current maximum = heights[maxDeque.First]
            //
            // If max - min > limit, the window is invalid and must be shrunk.
            while (heights[maxDeque.First!.Value] - heights[minDeque.First!.Value] > limit)
            {
                // Before moving left forward, we must remove outdated indices
                // from the fronts of the deques if they are exactly the index
                // that is leaving the window.
                //
                // Why only the front?
                // Because only the front can represent the current min or max.
                // Any outdated index deeper inside the deque would have already
                // been dominated by a better candidate and would not matter.
                if (minDeque.First!.Value == left)
                {
                    minDeque.RemoveFirst();
                }

                if (maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                // Shrink the window from the left by one position.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: The window [left..right] is now valid.
            // ------------------------------------------------------------
            //
            // So we can compute its length and update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] heights1 = { 4, 7, 6, 8, 5, 9 };
int limit1 = 3;
int result1 = solution.LongestSubarray(heights1, limit1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2
int[] heights2 = { 10, 1, 2, 4, 7, 2 };
int limit2 = 5;
int result2 = solution.LongestSubarray(heights2, limit2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional quick checks
int[] heights3 = { 8 };
int limit3 = 0;
int result3 = solution.LongestSubarray(heights3, limit3);
Console.WriteLine("Single Element Result: " + result3); // Expected: 1

int[] heights4 = { 1, 1, 1, 1 };
int limit4 = 0;
int result4 = solution.LongestSubarray(heights4, limit4);
Console.WriteLine("All Equal Result: " + result4); // Expected: 4