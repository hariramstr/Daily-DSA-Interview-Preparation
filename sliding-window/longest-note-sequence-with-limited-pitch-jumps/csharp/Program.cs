/*
Title: Longest Note Sequence With Limited Pitch Jumps
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A music learning app records a student's practice session as an array of integers called notes,
where notes[i] is the pitch played at time i.

The app wants to find the longest contiguous segment that is still considered smooth enough
to review as a single phrase.

A segment is smooth if the difference between the highest pitch and the lowest pitch inside
that segment is at most limit.

Your task is to return the length of the longest contiguous subarray of notes such that:

    max(notes[l..r]) - min(notes[l..r]) <= limit

This is a streaming-style problem:
- We extend the right side of the window one element at a time.
- If the window becomes invalid, we move the left side forward until it becomes valid again.
- We need an efficient solution because the array can be very large.

Constraints:
- 1 <= notes.length <= 200000
- 0 <= notes[i] <= 1000000000
- 0 <= limit <= 1000000000

Example 1:
Input: notes = [12, 14, 13, 18, 15, 16], limit = 3
Output: 3

Explanation:
One valid longest segment is [12, 14, 13], where max = 14 and min = 12, so the range is 2.
Any longer segment exceeds the allowed range of 3.

Example 2:
Input: notes = [7, 7, 8, 9, 6, 7, 8], limit = 2
Output: 4

Explanation:
The segment [7, 7, 8, 9] has max = 9 and min = 7, so the range is 2.
Another candidate [9, 6, 7, 8] has range 9 - 6 = 3, so it is invalid.
Therefore the maximum length is 4.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n), where n is the number of notes.
    Why?
    - Each index is added to each deque once.
    - Each index is removed from each deque at most once.
    - The left and right pointers each move only forward.

    Space Complexity:
    - O(n) in the worst case for the deques.
    */
    public int LongestSubarray(int[] notes, int limit)
    {
        // We will maintain a sliding window [left..right].
        // For every position "right", we try to include notes[right] in the current window.
        //
        // To quickly know whether the window is valid, we need:
        // - the maximum value in the window
        // - the minimum value in the window
        //
        // A naive approach would scan the whole window each time, which would be too slow.
        //
        // Instead, we use TWO MONOTONIC DEQUES:
        //
        // 1) maxDeque:
        //    - Stores indices
        //    - Values are kept in decreasing order
        //    - The front always points to the maximum value in the current window
        //
        // 2) minDeque:
        //    - Stores indices
        //    - Values are kept in increasing order
        //    - The front always points to the minimum value in the current window
        //
        // Why store indices instead of values?
        // - Because when the left side of the window moves forward,
        //   we need to know whether an element has gone out of the window.
        // - Indices let us remove expired elements efficiently.

        var maxDeque = new LinkedList<int>();
        var minDeque = new LinkedList<int>();

        int left = 0;
        int bestLength = 0;

        // Expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < notes.Length; right++)
        {
            // ------------------------------------------------------------
            // STEP 1: Insert notes[right] into maxDeque
            // ------------------------------------------------------------
            //
            // maxDeque must remain decreasing by value.
            // That means:
            // - While the last element in maxDeque has a value smaller than notes[right],
            //   it can never become the maximum for any future window that also includes notes[right].
            //
            // Why can we remove it?
            // - Because notes[right] is newer (farther to the right)
            // - And notes[right] is greater than or equal to that old value
            // - So the old smaller value is dominated and no longer useful
            while (maxDeque.Count > 0 && notes[maxDeque.Last!.Value] < notes[right])
            {
                maxDeque.RemoveLast();
            }

            // Now append the current index.
            maxDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert notes[right] into minDeque
            // ------------------------------------------------------------
            //
            // minDeque must remain increasing by value.
            // That means:
            // - While the last element in minDeque has a value larger than notes[right],
            //   it can never become the minimum for any future window that also includes notes[right].
            //
            // Why can we remove it?
            // - Because notes[right] is newer
            // - And notes[right] is smaller than or equal to that old value
            // - So the old larger value is dominated and no longer useful
            while (minDeque.Count > 0 && notes[minDeque.Last!.Value] > notes[right])
            {
                minDeque.RemoveLast();
            }

            // Now append the current index.
            minDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window from the left while it is invalid
            // ------------------------------------------------------------
            //
            // The current maximum is at maxDeque.First
            // The current minimum is at minDeque.First
            //
            // If max - min > limit, the window is not allowed.
            // So we must move "left" forward until the window becomes valid again.
            //
            // This is the heart of the sliding window technique:
            // - right only moves forward
            // - left only moves forward
            // - therefore total work stays linear
            while (notes[maxDeque.First!.Value] - notes[minDeque.First!.Value] > limit)
            {
                // If the element leaving the window is exactly the current maximum,
                // remove it from the front of maxDeque.
                if (maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                // If the element leaving the window is exactly the current minimum,
                // remove it from the front of minDeque.
                if (minDeque.First!.Value == left)
                {
                    minDeque.RemoveFirst();
                }

                // Move the left boundary forward by one.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: Update the best answer
            // ------------------------------------------------------------
            //
            // At this point, the window [left..right] is guaranteed to be valid.
            // So we compute its length and compare it with the best length seen so far.
            int currentLength = right - left + 1;
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // After processing all positions, bestLength is the answer.
        return bestLength;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] notes1 = { 12, 14, 13, 18, 15, 16 };
int limit1 = 3;
int result1 = solution.LongestSubarray(notes1, limit1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] notes2 = { 7, 7, 8, 9, 6, 7, 8 };
int limit2 = 2;
int result2 = solution.LongestSubarray(notes2, limit2);
Console.WriteLine(result2); // Expected: 4

// Additional quick checks
int[] notes3 = { 5 };
int limit3 = 0;
Console.WriteLine(solution.LongestSubarray(notes3, limit3)); // Expected: 1

int[] notes4 = { 1, 1, 1, 1 };
int limit4 = 0;
Console.WriteLine(solution.LongestSubarray(notes4, limit4)); // Expected: 4

int[] notes5 = { 10, 1, 2, 4, 7, 2 };
int limit5 = 5;
Console.WriteLine(solution.LongestSubarray(notes5, limit5)); // Expected: 4