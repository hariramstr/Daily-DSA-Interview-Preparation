/*
Title: Longest Workout Segment With Limited Speed Drops
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array `speed` where `speed[i]` is the runner's speed during the `i`-th minute of a workout, and an integer `k`. A minute `i` (for `i > 0`) is called a speed drop if `speed[i] < speed[i - 1]`. Your task is to find the length of the longest contiguous segment of the workout that contains at most `k` speed drops.

In other words, choose a subarray `speed[l...r]` such that within that subarray, the number of indices `i` with `l < i <= r` and `speed[i] < speed[i - 1]` is at most `k`. Return the maximum possible length of such a segment.

This models a real fitness analytics scenario where a coach wants to identify the longest sustained stretch of a workout with only a limited number of slowdowns.

Constraints:
- 1 <= speed.length <= 2 * 10^5
- 0 <= k < speed.length
- 1 <= speed[i] <= 10^9

Example 1:
Input: speed = [5, 6, 4, 4, 7, 3, 8], k = 1
Output: 4
Explanation: One optimal segment is [4, 4, 7, 3] or [5, 6, 4, 4]. Each contains exactly 1 speed drop, so the answer is 4.

Example 2:
Input: speed = [9, 8, 7, 10, 11, 6, 12], k = 2
Output: 5
Explanation: The segment [8, 7, 10, 11, 6] has 2 speed drops: 7 < 8 and 6 < 11. No longer valid segment exists.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Why O(n)?
    - We use a classic sliding window with two pointers: left and right.
    - The right pointer moves from left to right exactly once.
    - The left pointer also only moves forward, never backward.
    - Therefore, each element is processed a constant number of times.

    Why O(1) extra space?
    - We only store a few integer variables.
    - We do not use any extra arrays, hash maps, or other data structures that grow with input size.
    */
    public int LongestWorkoutSegment(int[] speed, int k)
    {
        // If the array has only one minute, then the longest valid segment is that single minute.
        // There are no adjacent pairs inside a single-element segment, so there cannot be any speed drops.
        if (speed == null || speed.Length == 0)
        {
            return 0;
        }

        // "left" marks the beginning of our current sliding window.
        int left = 0;

        // "dropsInWindow" stores how many speed drops currently exist inside the window [left..right].
        //
        // Important detail:
        // A speed drop is defined at index i when speed[i] < speed[i - 1].
        // So for a window [left..right], we count drops for indices i where left < i <= right.
        int dropsInWindow = 0;

        // "best" stores the maximum valid window length we have seen so far.
        int best = 1;

        // We expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < speed.Length; right++)
        {
            // STEP 1: Add the new element at position "right" into the window.
            //
            // When right == 0, there is no previous element, so no new adjacent comparison exists.
            // When right > 0, the only NEW adjacent pair introduced by extending the window is:
            // (right - 1, right)
            //
            // If speed[right] < speed[right - 1], then index "right" is a speed drop.
            // That drop belongs to the current window as long as both indices are inside it.
            if (right > 0 && speed[right] < speed[right - 1])
            {
                dropsInWindow++;
            }

            // STEP 2: If the window has too many drops, shrink it from the left
            // until it becomes valid again.
            //
            // We need at most k drops, so while dropsInWindow > k, move "left" forward.
            while (dropsInWindow > k)
            {
                // Before increasing left, we must check whether we are removing a drop
                // that currently contributes to the window.
                //
                // Which drop disappears when left moves from L to L+1?
                // The only adjacent comparison that stops being fully inside the window is the one at index L+1:
                // speed[L+1] < speed[L]
                //
                // Why?
                // Because a drop at index i depends on the pair (i-1, i).
                // When left moves forward, the pair involving the old left boundary is the one that may leave.
                //
                // So if left + 1 is within bounds and speed[left + 1] < speed[left],
                // then that drop was counted before, and now it should be removed.
                if (left + 1 < speed.Length && speed[left + 1] < speed[left])
                {
                    dropsInWindow--;
                }

                // Actually shrink the window by moving the left boundary one step right.
                left++;
            }

            // STEP 3: At this point, the window [left..right] is valid:
            // it contains at most k speed drops.
            //
            // So we compute its length and update the best answer if needed.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After scanning all possible right endpoints, "best" is the answer.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] speed1 = { 5, 6, 4, 4, 7, 3, 8 };
int k1 = 1;
int result1 = solution.LongestWorkoutSegment(speed1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2
int[] speed2 = { 9, 8, 7, 10, 11, 6, 12 };
int k2 = 2;
int result2 = solution.LongestWorkoutSegment(speed2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 5

// Additional quick checks

int[] speed3 = { 1 };
int k3 = 0;
int result3 = solution.LongestWorkoutSegment(speed3, k3);
Console.WriteLine("Single Element Result: " + result3); // Expected: 1

int[] speed4 = { 3, 3, 3, 3 };
int k4 = 0;
int result4 = solution.LongestWorkoutSegment(speed4, k4);
Console.WriteLine("No Drops Result: " + result4); // Expected: 4

int[] speed5 = { 5, 4, 3, 2, 1 };
int k5 = 1;
int result5 = solution.LongestWorkoutSegment(speed5, k5);
Console.WriteLine("Mostly Decreasing Result: " + result5); // Expected: 2