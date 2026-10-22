/*
Title: Longest Stable Altitude Stretch

Problem Description:
You are given an integer array altitudes where altitudes[i] represents the altitude reading of a drone at the i-th second.
A flight segment is considered stable if the difference between the highest and lowest altitude inside that contiguous
segment is at most limit.

Your task is to return the length of the longest stable contiguous segment.

In other words, find the maximum length of a subarray altitudes[l..r] such that:
max(altitudes[l..r]) - min(altitudes[l..r]) <= limit

This models a real monitoring system where a drone is considered to be flying smoothly only when its altitude
fluctuations stay within a safe tolerance.

Constraints:
- 1 <= altitudes.length <= 100000
- -1000000000 <= altitudes[i] <= 1000000000
- 0 <= limit <= 1000000000

Examples:
1)
Input: altitudes = [30, 32, 31, 35, 34, 33], limit = 3
Correct Output: 3

Reason:
- [30, 32, 31] => max = 32, min = 30, diff = 2 => valid, length = 3
- [32, 31, 35, 34] => max = 35, min = 31, diff = 4 => invalid
- [31, 35, 34, 33] => max = 35, min = 31, diff = 4 => invalid
- [35, 34, 33] => max = 35, min = 33, diff = 2 => valid, length = 3
So the longest valid length is 3.

2)
Input: altitudes = [100, 100, 100, 100], limit = 0
Output: 4

Notes:
- A segment must be contiguous.
- Negative altitude values are allowed.
- An O(n log n) or O(n) sliding window approach is expected.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n), where n is the number of altitude readings.
    - Each index is added to and removed from each deque at most once.

    Space Complexity:
    - O(n) in the worst case for the two deques.

    Idea:
    We use a sliding window [left..right].
    For every new right position, we expand the window.
    We must quickly know:
    - the maximum value in the current window
    - the minimum value in the current window

    To do that efficiently, we maintain:
    1) a decreasing deque of indices for maximum values
       - front always points to the current maximum
    2) an increasing deque of indices for minimum values
       - front always points to the current minimum

    If max - min becomes greater than limit, the window is invalid.
    Then we move left forward until the window becomes valid again.
    */
    public int LongestStableAltitudeStretch(int[] altitudes, int limit)
    {
        // This deque will store indices of elements in decreasing value order.
        // That means altitudes[maxDeque[0]] is always the maximum in the current window.
        LinkedList<int> maxDeque = new LinkedList<int>();

        // This deque will store indices of elements in increasing value order.
        // That means altitudes[minDeque[0]] is always the minimum in the current window.
        LinkedList<int> minDeque = new LinkedList<int>();

        // left marks the start of the current sliding window.
        int left = 0;

        // best stores the longest valid window length found so far.
        int best = 0;

        // We expand the window one element at a time by moving right.
        for (int right = 0; right < altitudes.Length; right++)
        {
            // ------------------------------------------------------------
            // STEP 1: Insert altitudes[right] into the max deque.
            // ------------------------------------------------------------
            // Why?
            // We want maxDeque to remain in decreasing order of values.
            // If the new value is greater than values at the back,
            // those smaller values can never become the maximum while
            // the new value is still inside the window.
            while (maxDeque.Count > 0 && altitudes[maxDeque.Last!.Value] < altitudes[right])
            {
                maxDeque.RemoveLast();
            }

            // Add the current index to the back after removing weaker candidates.
            maxDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert altitudes[right] into the min deque.
            // ------------------------------------------------------------
            // Why?
            // We want minDeque to remain in increasing order of values.
            // If the new value is smaller than values at the back,
            // those larger values can never become the minimum while
            // the new value is still inside the window.
            while (minDeque.Count > 0 && altitudes[minDeque.Last!.Value] > altitudes[right])
            {
                minDeque.RemoveLast();
            }

            // Add the current index to the back after removing worse candidates.
            minDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window from the left while it is invalid.
            // ------------------------------------------------------------
            // The current maximum is at maxDeque.First.
            // The current minimum is at minDeque.First.
            //
            // If max - min > limit, then the current window is not stable,
            // so we must move left forward until it becomes stable again.
            while ((long)altitudes[maxDeque.First!.Value] - altitudes[minDeque.First!.Value] > limit)
            {
                // If the leftmost index is exactly the one stored at the front
                // of maxDeque, it is leaving the window, so remove it.
                if (maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                // If the leftmost index is exactly the one stored at the front
                // of minDeque, it is leaving the window, so remove it.
                if (minDeque.First!.Value == left)
                {
                    minDeque.RemoveFirst();
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: Update the best answer.
            // ------------------------------------------------------------
            // At this point, the window [left..right] is guaranteed valid.
            // So its length is a candidate answer.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        return best;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] altitudes1 = { 30, 32, 31, 35, 34, 33 };
int limit1 = 3;
int result1 = solution.LongestStableAltitudeStretch(altitudes1, limit1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 3

// Example 2
int[] altitudes2 = { 100, 100, 100, 100 };
int limit2 = 0;
int result2 = solution.LongestStableAltitudeStretch(altitudes2, limit2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional demo with negative values
int[] altitudes3 = { -5, -3, -4, -2, -1 };
int limit3 = 2;
int result3 = solution.LongestStableAltitudeStretch(altitudes3, limit3);
Console.WriteLine("Example 3 Result: " + result3);

// Additional demo with a single element
int[] altitudes4 = { 42 };
int limit4 = 10;
int result4 = solution.LongestStableAltitudeStretch(altitudes4, limit4);
Console.WriteLine("Example 4 Result: " + result4);