/*
Title: Longest Sensor Batch With Limited Duplicate Device IDs

Problem Description:
A monitoring system receives a stream of sensor readings, where each reading is labeled with the integer device ID that produced it.
Engineers want to analyze the longest contiguous batch of readings that is still considered "diverse enough."
A batch is valid if no single device ID appears more than k times inside that contiguous segment.

Given an integer array deviceIds and an integer k, return the length of the longest contiguous subarray
such that every distinct device ID appears at most k times within that subarray.

This is a contiguous-window problem: you may only choose a single continuous segment from the input array.
The goal is to maximize its length while respecting the per-device frequency limit.

Constraints:
- 1 <= deviceIds.length <= 200000
- 1 <= deviceIds[i] <= 1000000000
- 1 <= k <= deviceIds.length
- The answer must be computed in O(n) or O(n log n) time.

Example 1:
Input: deviceIds = [4, 1, 4, 2, 4, 1, 2, 2], k = 2
Output: 5

Example 2:
Input: deviceIds = [7, 7, 3, 7, 3, 3, 8], k = 1
Output: 2
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the sliding window once by moving the right pointer.
    - Each element is removed from the sliding window at most once by moving the left pointer.
    - Therefore, the total amount of work across the whole array is linear.

    Space Complexity: O(n)
    - In the worst case, the dictionary may store counts for many distinct device IDs currently in the window.
    - In the worst case, that can be up to O(n) distinct values.
    */
    public int LongestValidBatch(int[] deviceIds, int k)
    {
        // This dictionary stores how many times each device ID appears
        // inside the CURRENT sliding window.
        //
        // Why a dictionary?
        // - Device IDs can be as large as 1,000,000,000, so using an array indexed by ID is not practical.
        // - A Dictionary<int, int> lets us store only the IDs that actually appear.
        var frequency = new Dictionary<int, int>();

        // 'left' is the start index of our current window.
        // The window will always be [left..right].
        int left = 0;

        // 'bestLength' stores the maximum valid window length we have seen so far.
        int bestLength = 0;

        // We expand the window one element at a time by moving 'right'.
        for (int right = 0; right < deviceIds.Length; right++)
        {
            // Step 1: Include the new element at index 'right' into the window.
            int currentDeviceId = deviceIds[right];

            // If this device ID has not been seen in the current window before,
            // initialize its count to 0 first.
            if (!frequency.ContainsKey(currentDeviceId))
            {
                frequency[currentDeviceId] = 0;
            }

            // Now increase the count because deviceIds[right] is entering the window.
            frequency[currentDeviceId]++;

            // Step 2: If adding this element caused its count to exceed k,
            // then the window is no longer valid.
            //
            // Important observation:
            // Before adding deviceIds[right], the window was valid.
            // After adding it, only the count of currentDeviceId changed.
            // So if the window becomes invalid, it is specifically because
            // currentDeviceId now appears too many times.
            //
            // Therefore, we shrink the window from the left until
            // frequency[currentDeviceId] <= k again.
            while (frequency[currentDeviceId] > k)
            {
                // The element at 'left' is about to leave the window.
                int leftDeviceId = deviceIds[left];

                // Decrease its frequency because it is no longer inside the window.
                frequency[leftDeviceId]--;

                // Move the left boundary rightward, making the window smaller.
                left++;
            }

            // Step 3: At this point, the window [left..right] is valid again.
            // Every device ID appears at most k times.
            //
            // So we can compute its length and compare it with the best answer found so far.
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

// Demo code

var solution = new Solution();

// Example 1
int[] deviceIds1 = { 4, 1, 4, 2, 4, 1, 2, 2 };
int k1 = 2;
int result1 = solution.LongestValidBatch(deviceIds1, k1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] deviceIds2 = { 7, 7, 3, 7, 3, 3, 8 };
int k2 = 1;
int result2 = solution.LongestValidBatch(deviceIds2, k2);
Console.WriteLine(result2); // Expected: 2

// Additional quick demo
int[] deviceIds3 = { 1, 2, 3, 1, 2, 3, 1, 2 };
int k3 = 2;
int result3 = solution.LongestValidBatch(deviceIds3, k3);
Console.WriteLine(result3); // One valid longest length is 6