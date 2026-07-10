/*
Title: Longest Sensor Drift Window Within Calibration Budget
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A factory records a sequence of integer sensor readings over time. Engineers want to analyze the longest contiguous time window that can be considered stable enough to recalibrate together. A window is valid if the difference between its highest reading and lowest reading is at most budget.

Given an integer array readings and an integer budget, return the length of the longest contiguous subarray such that:
max(readings[l..r]) - min(readings[l..r]) <= budget

This models a real monitoring system where readings may fluctuate, but only within a limited tolerance before a recalibration job must be split into smaller batches.

Constraints:
- 1 <= readings.length <= 200000
- -10^9 <= readings[i] <= 10^9
- 0 <= budget <= 10^9

Examples:
1)
Input: readings = [8, 10, 9, 12, 7, 8], budget = 3
Output: 3

2)
Input: readings = [4, 4, 5, 6, 6, 3, 4], budget = 2
Output: 5
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n)
    Each array index is added to and removed from each deque at most once.

    Space Complexity:
    O(n)
    In the worst case, the deques can together store up to n indices.

    Beginner-friendly idea:
    We use a sliding window [left..right].
    For every new right position, we want to know the current window's:
    - maximum value
    - minimum value

    If max - min becomes larger than budget, the window is invalid,
    so we move left forward until the window becomes valid again.

    To get max and min efficiently, we use two monotonic deques:
    - maxDeque: stores indices in decreasing value order
    - minDeque: stores indices in increasing value order

    The front of maxDeque always gives the index of the maximum value in the window.
    The front of minDeque always gives the index of the minimum value in the window.
    */
    public int LongestStableWindow(int[] readings, int budget)
    {
        // This deque will keep indices of elements in decreasing order of values.
        // Why decreasing?
        // Because then the first index always points to the largest value
        // inside the current window.
        LinkedList<int> maxDeque = new LinkedList<int>();

        // This deque will keep indices of elements in increasing order of values.
        // Why increasing?
        // Because then the first index always points to the smallest value
        // inside the current window.
        LinkedList<int> minDeque = new LinkedList<int>();

        // left is the starting index of our current sliding window.
        int left = 0;

        // best will store the maximum valid window length found so far.
        int best = 0;

        // Expand the window one element at a time by moving right forward.
        for (int right = 0; right < readings.Length; right++)
        {
            // ------------------------------------------------------------
            // STEP 1: Insert readings[right] into maxDeque
            // ------------------------------------------------------------
            // We want maxDeque to remain decreasing by value.
            // If the new value is greater than or equal to values at the back,
            // those smaller/equal values can never become the maximum while
            // the new value is still in the window.
            //
            // Example:
            // values at back: [5, 4], new value = 6
            // Then 5 and 4 are useless for future maximum queries,
            // because 6 is newer and larger.
            while (maxDeque.Count > 0 && readings[maxDeque.Last!.Value] <= readings[right])
            {
                maxDeque.RemoveLast();
            }

            // Add the current index to the back after removing weaker candidates.
            maxDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert readings[right] into minDeque
            // ------------------------------------------------------------
            // We want minDeque to remain increasing by value.
            // If the new value is smaller than or equal to values at the back,
            // those larger/equal values can never become the minimum while
            // the new value is still in the window.
            //
            // Example:
            // values at back: [7, 8], new value = 6
            // Then 7 and 8 are useless for future minimum queries,
            // because 6 is newer and smaller.
            while (minDeque.Count > 0 && readings[minDeque.Last!.Value] >= readings[right])
            {
                minDeque.RemoveLast();
            }

            // Add the current index to the back after removing weaker candidates.
            minDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window while it is invalid
            // ------------------------------------------------------------
            // The current maximum is at maxDeque.First
            // The current minimum is at minDeque.First
            //
            // If max - min > budget, then the window [left..right] is invalid.
            // We must move left forward until it becomes valid again.
            while ((long)readings[maxDeque.First!.Value] - readings[minDeque.First!.Value] > budget)
            {
                // If the element leaving the window is currently the maximum,
                // remove it from the front of maxDeque.
                if (maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                // If the element leaving the window is currently the minimum,
                // remove it from the front of minDeque.
                if (minDeque.First!.Value == left)
                {
                    minDeque.RemoveFirst();
                }

                // Actually move the left boundary of the window forward.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: Update the best answer
            // ------------------------------------------------------------
            // At this point, the window [left..right] is guaranteed valid.
            // So we compute its length and compare it with the best seen so far.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        return best;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] readings1 = { 8, 10, 9, 12, 7, 8 };
int budget1 = 3;
int result1 = solution.LongestStableWindow(readings1, budget1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] readings2 = { 4, 4, 5, 6, 6, 3, 4 };
int budget2 = 2;
int result2 = solution.LongestStableWindow(readings2, budget2);
Console.WriteLine(result2); // Expected: 5

// Additional quick checks
int[] readings3 = { 1 };
int budget3 = 0;
Console.WriteLine(solution.LongestStableWindow(readings3, budget3)); // Expected: 1

int[] readings4 = { 1, 2, 3, 4, 5 };
int budget4 = 4;
Console.WriteLine(solution.LongestStableWindow(readings4, budget4)); // Expected: 5

int[] readings5 = { 1, 5, 6, 7, 8, 10, 6, 5, 6 };
int budget5 = 4;
Console.WriteLine(solution.LongestStableWindow(readings5, budget5)); // Expected: 5