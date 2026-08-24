/*
Title: Longest Editing Streak With Limited Undo Actions
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array `events` representing a user's editing timeline in a document editor.
Each element is either `1` or `0`:

- `1` means the user made a productive edit during that minute.
- `0` means the minute was an undo, rollback, or other non-productive action.

The product team wants to measure the longest continuous editing streak that can be considered
"mostly productive." A streak is valid if it contains at most `k` non-productive minutes.
In other words, you may include up to `k` zeros inside the chosen contiguous subarray.

Return the length of the longest valid contiguous streak.

This problem models a common analytics task where a noisy activity stream must be summarized
while tolerating a limited number of interruptions. A correct solution should run efficiently
on large inputs, so approaches that check every subarray will be too slow.

Constraints:
- 1 <= events.length <= 200000
- 0 <= k <= events.length
- events[i] is either 0 or 1

Example 1:
Input: events = [1,1,0,1,0,1,1,1], k = 1
Output: 4

Example 2:
Input: events = [0,1,1,0,1,1,0,1], k = 2
Output: 7

Goal:
Compute the maximum length of any contiguous subarray containing at most k zeros.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is processed by the right pointer once.
    - Each element is removed from the window by the left pointer at most once.
    - Because both pointers only move forward, the total work is linear.

    Space Complexity: O(1)
    - We only store a few integer variables.
    - No extra array, list, queue, or dictionary is needed.
    */
    public int LongestEditingStreak(int[] events, int k)
    {
        // This variable marks the left boundary of our current sliding window.
        // The window always represents a contiguous subarray: events[left..right].
        int left = 0;

        // This counts how many zeros currently exist inside the window.
        // We must keep this value <= k for the window to remain valid.
        int zeroCount = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We expand the window one element at a time by moving the right boundary.
        // For every position 'right', we try to include events[right] in the current window.
        for (int right = 0; right < events.Length; right++)
        {
            // Step 1: Include the new element at index 'right' into the window.
            // If it is a zero, then the number of non-productive minutes in the window increases.
            if (events[right] == 0)
            {
                zeroCount++;
            }

            // Step 2: If the window has become invalid (too many zeros),
            // we must shrink it from the left until it becomes valid again.
            //
            // Why is this necessary?
            // Because the problem only allows windows with at most k zeros.
            // If zeroCount > k, then the current window cannot be considered.
            //
            // Why use a while loop instead of an if statement?
            // Because removing just one element from the left may still leave too many zeros.
            // We keep shrinking until the condition is fixed.
            while (zeroCount > k)
            {
                // Before moving 'left' forward, check whether the element leaving the window is zero.
                // If it is, then the zero count inside the window decreases by one.
                if (events[left] == 0)
                {
                    zeroCount--;
                }

                // Move the left boundary rightward by one position.
                // This effectively removes events[left] from the current window.
                left++;
            }

            // Step 3: At this point, the window is guaranteed to be valid:
            // it contains at most k zeros.
            //
            // So we compute its length.
            // Since both endpoints are inclusive, length = right - left + 1.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer than any previous one.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After checking every possible right boundary, maxLength holds the answer.
        return maxLength;
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

// Example 1
int[] events1 = { 1, 1, 0, 1, 0, 1, 1, 1 };
int k1 = 1;
int result1 = solution.LongestEditingStreak(events1, k1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 4

// Example 2
int[] events2 = { 0, 1, 1, 0, 1, 1, 0, 1 };
int k2 = 2;
int result2 = solution.LongestEditingStreak(events2, k2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 7

// Additional quick checks

int[] events3 = { 1, 1, 1, 1 };
int k3 = 0;
int result3 = solution.LongestEditingStreak(events3, k3);
Console.WriteLine($"All productive, k=0: {result3}"); // Expected: 4

int[] events4 = { 0, 0, 0, 0 };
int k4 = 2;
int result4 = solution.LongestEditingStreak(events4, k4);
Console.WriteLine($"All non-productive, k=2: {result4}"); // Expected: 2

int[] events5 = { 1, 0, 1, 0, 1, 0, 1 };
int k5 = 2;
int result5 = solution.LongestEditingStreak(events5, k5);
Console.WriteLine($"Alternating pattern, k=2: {result5}"); // Expected: 5