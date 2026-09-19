/*
Title: Longest Badge Scan Streak Under Duplicate Limit
Difficulty: Easy
Topic: Sliding Window

Problem Description:
A security team is analyzing a hallway badge scanner that records employee badge IDs in the order they were scanned.
Because people may walk back and forth, the same badge ID can appear multiple times in a row or later in the log.
The team wants to find the longest contiguous portion of the scan log that is still considered "clean" under a simple rule:
within that portion, no badge ID may appear more than k times.

Given an array scans where scans[i] is the badge ID seen at time i, and an integer k, return the length of the
longest contiguous subarray such that every distinct badge ID in that subarray appears at most k times.

This is an interview-style sliding window problem. A good solution should expand the right side of the window and
shrink the left side only when some badge ID appears too many times.

Constraints:
- 1 <= scans.length <= 100000
- 1 <= scans[i] <= 1000000000
- 1 <= k <= scans.length
- The answer fits in a 32-bit integer

Example 1:
Input: scans = [5, 7, 5, 7, 5, 8], k = 2
Output: 5
Explanation: The longest valid window is [7, 5, 7, 5, 8]. In this subarray, badge 7 appears 2 times,
badge 5 appears 2 times, and badge 8 appears 1 time.

Example 2:
Input: scans = [3, 3, 3, 2, 2, 1], k = 1
Output: 2
Explanation: With k = 1, all badge IDs inside the chosen window must be unique. The longest valid windows have
length 2, such as [3, 2] or [2, 1].
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n), where n is the length of the scans array.
    - Each element is added to the sliding window once by moving the right pointer,
      and each element is removed from the sliding window at most once by moving the left pointer.

    Space Complexity:
    - O(m), where m is the number of distinct badge IDs currently tracked in the dictionary.
    - In the worst case, this can be O(n) if all values are different.
    */
    public int MaxSubarrayLength(int[] scans, int k)
    {
        // This dictionary stores how many times each badge ID appears
        // inside the CURRENT sliding window.
        //
        // Key   = badge ID
        // Value = frequency of that badge ID in the current window
        //
        // We choose a Dictionary<int, int> because:
        // 1. Badge IDs can be as large as 1,000,000,000, so using an array indexed by badge ID is not practical.
        // 2. Dictionary gives average O(1) insert, lookup, and update time.
        var frequency = new Dictionary<int, int>();

        // 'left' is the left boundary of our sliding window.
        // The window will always represent scans[left..right].
        int left = 0;

        // 'bestLength' stores the maximum valid window size we have seen so far.
        int bestLength = 0;

        // We expand the window by moving 'right' from left to right across the array.
        for (int right = 0; right < scans.Length; right++)
        {
            // Step 1:
            // Add the new rightmost element into the window.
            //
            // Why?
            // We are trying every possible window that ends at index 'right'.
            // So first, we include scans[right] in the current window.
            int currentBadge = scans[right];

            if (!frequency.ContainsKey(currentBadge))
            {
                frequency[currentBadge] = 0;
            }

            frequency[currentBadge]++;

            // Step 2:
            // If adding scans[right] caused its frequency to become greater than k,
            // then the window is no longer valid.
            //
            // Important observation:
            // Before adding scans[right], the window was valid.
            // After adding it, only the count of scans[right] could have become invalid.
            // No other badge count changed.
            //
            // Therefore, we only need to shrink while frequency[currentBadge] > k.
            while (frequency[currentBadge] > k)
            {
                // The element at the left side is leaving the window.
                int leftBadge = scans[left];

                // Decrease its frequency because we are removing it from the window.
                frequency[leftBadge]--;

                // Move the left boundary one step to the right.
                //
                // Why?
                // We keep shrinking until the over-limit badge count is fixed.
                left++;
            }

            // Step 3:
            // At this point, the window scans[left..right] is valid again.
            // Every badge ID appears at most k times.
            //
            // So we compute the current window length.
            int currentLength = right - left + 1;

            // Step 4:
            // Update the best answer if this valid window is the largest one seen so far.
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // After processing all possible right endpoints, bestLength is the answer.
        return bestLength;
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

// Example 1:
// scans = [5, 7, 5, 7, 5, 8], k = 2
// Expected output: 5
int[] scans1 = { 5, 7, 5, 7, 5, 8 };
int k1 = 2;
int result1 = solution.MaxSubarrayLength(scans1, k1);
Console.WriteLine(result1);

// Example 2:
// scans = [3, 3, 3, 2, 2, 1], k = 1
// Expected output: 2
int[] scans2 = { 3, 3, 3, 2, 2, 1 };
int k2 = 1;
int result2 = solution.MaxSubarrayLength(scans2, k2);
Console.WriteLine(result2);