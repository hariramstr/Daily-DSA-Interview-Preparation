/*
Title: Longest Playlist Window With Limited Tempo Drift
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A music app stores the tempo (in BPM) of songs in the order they were added to a playlist.
Product designers want to highlight the longest contiguous block of songs that feels rhythmically consistent.
A block is considered consistent if the difference between the fastest and slowest song in that block
is at most `limit` BPM.

Given an integer array `tempo`, where `tempo[i]` is the BPM of the `i`-th song, and an integer `limit`,
return the length of the longest contiguous subarray such that:

    max(tempo[l..r]) - min(tempo[l..r]) <= limit

The solution must be efficient for large inputs, so we use a sliding window plus two monotonic deques
to maintain the current window's maximum and minimum values in O(1) amortized time per element.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is the number of songs.
    Each index is added to and removed from each deque at most once.

    Space Complexity:
    O(n) in the worst case for the deques, though typically less.
    */
    public int LongestSubarray(int[] tempo, int limit)
    {
        // These deques will store indices, not values.
        //
        // Why indices instead of values?
        // 1. We need to know whether an element has moved out of the current window.
        // 2. If we only stored values, we would not know which occurrence to remove when duplicates exist.
        //
        // maxDeque:
        // - Maintains indices of elements in decreasing order of their tempo values.
        // - The front always points to the maximum value in the current window.
        //
        // minDeque:
        // - Maintains indices of elements in increasing order of their tempo values.
        // - The front always points to the minimum value in the current window.
        var maxDeque = new LinkedList<int>();
        var minDeque = new LinkedList<int>();

        // left is the start of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // Expand the window by moving right from left to right across the array.
        for (int right = 0; right < tempo.Length; right++)
        {
            // ------------------------------------------------------------
            // STEP 1: Insert tempo[right] into maxDeque
            // ------------------------------------------------------------
            //
            // Goal:
            // Keep maxDeque in decreasing order of values.
            //
            // Why?
            // If the new value is greater than values at the back,
            // those smaller values can never become the maximum of this window
            // or any future window that includes the new value.
            //
            // Example:
            // If the deque ends with values [108, 103] and new value is 107,
            // then 103 is useless as a future maximum while 107 remains in the window,
            // so we remove 103.
            while (maxDeque.Count > 0 && tempo[maxDeque.Last!.Value] < tempo[right])
            {
                maxDeque.RemoveLast();
            }

            // Add the current index to the back.
            maxDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert tempo[right] into minDeque
            // ------------------------------------------------------------
            //
            // Goal:
            // Keep minDeque in increasing order of values.
            //
            // Why?
            // If the new value is smaller than values at the back,
            // those larger values can never become the minimum of this window
            // or any future window that includes the new value.
            while (minDeque.Count > 0 && tempo[minDeque.Last!.Value] > tempo[right])
            {
                minDeque.RemoveLast();
            }

            // Add the current index to the back.
            minDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window while it is invalid
            // ------------------------------------------------------------
            //
            // The current window is [left..right].
            // It is valid only if:
            //
            //     currentMax - currentMin <= limit
            //
            // Since maxDeque front gives the maximum and minDeque front gives the minimum,
            // we can check validity in O(1).
            //
            // If invalid, we move left forward until the window becomes valid again.
            while (tempo[maxDeque.First!.Value] - tempo[minDeque.First!.Value] > limit)
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

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: Update the best answer
            // ------------------------------------------------------------
            //
            // At this point, the window [left..right] is guaranteed valid.
            // So we compute its length and compare it with the best seen so far.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // best now contains the length of the longest valid contiguous subarray.
        return best;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] tempo1 = { 98, 101, 100, 108, 107, 103 };
int limit1 = 5;
int result1 = solution.LongestSubarray(tempo1, limit1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 3

// Example 2
// Important correctness note:
// The problem statement's explanation contains a correction.
// For this input, the correct answer is 4.
int[] tempo2 = { 120, 124, 122, 121, 119, 130, 118 };
int limit2 = 4;
int result2 = solution.LongestSubarray(tempo2, limit2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 4

// Additional quick sanity checks
int[] tempo3 = { 5 };
int limit3 = 0;
Console.WriteLine($"Single song Result: {solution.LongestSubarray(tempo3, limit3)}"); // Expected: 1

int[] tempo4 = { 10, 10, 10, 10 };
int limit4 = 0;
Console.WriteLine($"All equal Result: {solution.LongestSubarray(tempo4, limit4)}"); // Expected: 4

int[] tempo5 = { 8, 2, 4, 7 };
int limit5 = 4;
Console.WriteLine($"Mixed values Result: {solution.LongestSubarray(tempo5, limit5)}"); // Expected: 2