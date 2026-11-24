/*
Title: Longest Workout Segment With Limited Heart Rate Zones
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array `zones` where `zones[i]` represents the heart rate zone recorded during the `i`-th minute of a workout.
A coach wants to analyze the longest continuous workout segment that stayed focused enough to use at most `k` distinct heart
rate zones.

Return the length of the longest contiguous subarray of `zones` that contains no more than `k` distinct values.

This problem models a realistic fitness analytics task: short fluctuations are acceptable, but if too many different zones
appear in one continuous segment, the workout is considered inconsistent. Your goal is to find the maximum duration of a
consistent segment.

A subarray is a contiguous portion of the array.

Constraints:
- 1 <= zones.length <= 2 * 10^5
- 1 <= zones[i] <= 10^9
- 1 <= k <= zones.length

Example 1:
Input: zones = [2, 2, 1, 3, 2, 2, 1], k = 2
Output: 3
Explanation: One valid longest segment is [2, 2, 1], which uses only zones {1, 2}. Any longer contiguous segment contains at least 3 distinct zones.

Example 2:
Input: zones = [5, 5, 5, 4, 4, 6, 6, 4], k = 2
Output: 5
Explanation: The segment [4, 4, 6, 6, 4] contains only zones {4, 6} and has length 5, which is the maximum possible.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n), where n is the length of the zones array.
    - Each element is added to the sliding window once by the right pointer,
      and removed from the sliding window at most once by the left pointer.

    Space Complexity:
    - O(k) in the typical sliding-window sense, or more precisely O(m),
      where m is the number of distinct values currently stored in the dictionary.
    - Because we never allow the valid window to keep more than k distinct values,
      the active window tracking is effectively bounded around that amount.
    */
    public int LongestSegmentWithAtMostKDistinct(int[] zones, int k)
    {
        // This dictionary stores:
        // key   -> a heart rate zone value
        // value -> how many times that zone appears inside the CURRENT window
        //
        // Why do we need counts instead of just a set?
        // Because when we move the left side of the window forward, we need to know
        // whether a zone still exists somewhere else in the window.
        // If its count becomes 0, only then should we remove it from the dictionary.
        var frequency = new Dictionary<int, int>();

        // left marks the beginning of our current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window one element at a time using right.
        for (int right = 0; right < zones.Length; right++)
        {
            int currentZone = zones[right];

            // STEP 1: Add the new rightmost element into the window.
            //
            // We are extending the current subarray from [left..right-1] to [left..right].
            // So we must update the frequency map to include zones[right].
            if (!frequency.ContainsKey(currentZone))
            {
                frequency[currentZone] = 0;
            }

            frequency[currentZone]++;

            // STEP 2: If the window now contains too many distinct zones,
            // shrink it from the left until it becomes valid again.
            //
            // Why is this necessary?
            // The problem requires "at most k distinct values".
            // If we have more than k distinct zones, the current window is invalid.
            // The only way to fix that while keeping the window contiguous is to move
            // the left boundary to the right.
            while (frequency.Count > k)
            {
                int leftZone = zones[left];

                // Remove one occurrence of the leftmost zone because that element
                // is leaving the window.
                frequency[leftZone]--;

                // If the count becomes 0, that zone no longer exists in the window.
                // We must remove it from the dictionary so that frequency.Count
                // correctly reflects the number of distinct zones currently present.
                if (frequency[leftZone] == 0)
                {
                    frequency.Remove(leftZone);
                }

                // Move the left boundary rightward, making the window smaller.
                left++;
            }

            // STEP 3: At this point, the window [left..right] is guaranteed valid.
            // It contains at most k distinct zones.
            //
            // So we can safely compute its length and compare it with the best answer.
            int currentLength = right - left + 1;

            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After processing every possible right boundary, best contains the answer.
        return best;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] zones1 = { 2, 2, 1, 3, 2, 2, 1 };
int k1 = 2;
int result1 = solution.LongestSegmentWithAtMostKDistinct(zones1, k1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 3

// Example 2
int[] zones2 = { 5, 5, 5, 4, 4, 6, 6, 4 };
int k2 = 2;
int result2 = solution.LongestSegmentWithAtMostKDistinct(zones2, k2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 5

// Additional demo
int[] zones3 = { 1, 2, 1, 2, 3 };
int k3 = 2;
int result3 = solution.LongestSegmentWithAtMostKDistinct(zones3, k3);
Console.WriteLine($"Additional Demo Result: {result3}"); // Expected: 4