/*
Title: Longest Camera Feed With Limited Motion Zones
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A security team monitors a hallway using a camera that reports one motion zone ID for each second.
The array `zones` represents the detected zone at every second, where `zones[i]` is the zone that
had motion during second `i`.

To reduce operator fatigue, the team wants to review the longest continuous time interval that
contains motion from at most `k` distinct zones. If more than `k` different zone IDs appear in the
interval, the segment is considered too noisy to review efficiently.

Your task is to return the length of the longest contiguous subarray of `zones` that contains at
most `k` distinct values.

This is a continuous interval problem, so we must use a contiguous subarray, not a subsequence.
An efficient solution is expected because the feed may be very large.

Constraints:
- 1 <= zones.length <= 2 * 10^5
- 1 <= zones[i] <= 10^9
- 1 <= k <= zones.length
- The solution should run in linear time or close to it.

Example 1:
Input: zones = [4, 2, 4, 3, 2, 2, 4], k = 2
Output: 3

Reasoning:
- [4, 2, 4] has distinct zones {4, 2} => valid, length 3
- [2, 2, 4] has distinct zones {2, 4} => valid, length 3
- Any length 4 window here contains at least 3 distinct zones, so answer is 3

Example 2:
Input: zones = [7, 7, 8, 9, 8, 8, 7, 7], k = 2
Output: 4

Reasoning:
- [8, 9, 8, 8] has distinct zones {8, 9} => valid, length 4
- [8, 8, 7, 7] has distinct zones {8, 7} => valid, length 4
- Any length 5 candidate shown in the prompt contains 3 distinct zones, so invalid
- Therefore answer is 4

Approach:
Use the classic sliding window technique with a frequency dictionary.
- Expand the right side of the window one element at a time
- Count how many times each zone appears in the current window
- If the number of distinct zones becomes greater than k, shrink from the left
  until the window becomes valid again
- Track the maximum valid window length seen so far
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is the length of the zones array.

    Why O(n)?
    - Each element is added to the window once when the right pointer moves forward.
    - Each element is removed from the window at most once when the left pointer moves forward.
    - Therefore, both pointers together move at most 2n times.

    Space Complexity:
    O(k) on average for the active window's distinct values, and in the worst case O(n)
    if k can be as large as n and the window contains many distinct values.

    We use a Dictionary<int, int> to store:
    - key   = zone ID
    - value = how many times that zone appears in the current window
    */
    public int LengthOfLongestSubarrayWithAtMostKDistinct(int[] zones, int k)
    {
        // This dictionary stores the frequency of each zone ID inside the current sliding window.
        // Example:
        // If the current window is [4, 2, 4], then the dictionary is:
        // 4 -> 2
        // 2 -> 1
        //
        // Why do we need frequencies instead of just a set?
        // Because when we move the left side of the window forward, we need to know whether
        // removing one occurrence of a zone should completely remove that zone from the window
        // or whether it still exists elsewhere in the window.
        var frequency = new Dictionary<int, int>();

        // `left` marks the start index of the current window.
        int left = 0;

        // `maxLength` stores the best answer found so far.
        int maxLength = 0;

        // We expand the window by moving `right` from left to right across the array.
        for (int right = 0; right < zones.Length; right++)
        {
            // Step 1: Include zones[right] in the current window.
            //
            // We are expanding the window to the right by one element.
            // That means the zone at index `right` is now part of the current interval.
            int currentZone = zones[right];

            // If this zone is already in the dictionary, increase its count.
            // Otherwise, add it with count 1.
            if (frequency.ContainsKey(currentZone))
            {
                frequency[currentZone]++;
            }
            else
            {
                frequency[currentZone] = 1;
            }

            // Step 2: If the window has become invalid, shrink it from the left.
            //
            // The window is invalid when it contains more than `k` distinct zone IDs.
            // `frequency.Count` tells us how many distinct keys currently exist in the window.
            //
            // We keep shrinking until the window becomes valid again.
            while (frequency.Count > k)
            {
                // The zone at the left edge is about to be removed from the window.
                int leftZone = zones[left];

                // Decrease its frequency because it will no longer be included.
                frequency[leftZone]--;

                // If its frequency becomes zero, that means this zone no longer exists
                // anywhere in the current window, so we must remove the key entirely.
                //
                // This is very important because the number of distinct zones is determined
                // by how many keys are present in the dictionary.
                if (frequency[leftZone] == 0)
                {
                    frequency.Remove(leftZone);
                }

                // Move the left boundary to the right, effectively shrinking the window.
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed to be valid.
            //
            // That means it contains at most `k` distinct zone IDs.
            // So we can safely compute its length and compare it with the best answer so far.
            int currentLength = right - left + 1;

            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After processing all possible right endpoints, `maxLength` is the answer.
        return maxLength;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] zones1 = { 4, 2, 4, 3, 2, 2, 4 };
int k1 = 2;
int result1 = solution.LengthOfLongestSubarrayWithAtMostKDistinct(zones1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 3

// Example 2
int[] zones2 = { 7, 7, 8, 9, 8, 8, 7, 7 };
int k2 = 2;
int result2 = solution.LengthOfLongestSubarrayWithAtMostKDistinct(zones2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional quick checks
int[] zones3 = { 1 };
int k3 = 1;
int result3 = solution.LengthOfLongestSubarrayWithAtMostKDistinct(zones3, k3);
Console.WriteLine("Additional Check 1 Result: " + result3); // Expected: 1

int[] zones4 = { 1, 2, 1, 2, 3 };
int k4 = 2;
int result4 = solution.LengthOfLongestSubarrayWithAtMostKDistinct(zones4, k4);
Console.WriteLine("Additional Check 2 Result: " + result4); // Expected: 4