/*
Title: Longest Sleep Tracker Streak With Limited Wake Spikes
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A wearable device records a user's sleep state once per minute. The data is stored in an integer array states where each value is either 0 or 1. A value of 1 means the user was asleep during that minute, and 0 means they were awake. Because short wake-ups are common and can be ignored by the app, the product team wants to measure the longest continuous sleep streak if you are allowed to treat at most k awake minutes as sleep.

Your task is to return the length of the longest contiguous subarray that can be considered a sleep streak after converting at most k values of 0 into 1. In other words, find the maximum number of consecutive minutes that contain no more than k awake samples.

This is a realistic streaming-analysis problem: the chosen interval must remain contiguous in the original timeline, and you may only ignore up to k wake spikes inside that interval.

Constraints:
- 1 <= states.length <= 200000
- states[i] is either 0 or 1
- 0 <= k <= states.length

Example 1:
Input: states = [1,1,0,1,0,1,1,1], k = 1
Output: 5
Explanation: Treat either awake minute at index 2 or index 4 as sleep. The longest valid streak is length 5, such as [1,0,1,0,1] is not valid because it contains two awake minutes, but [1,0,1,1,1] after one conversion is valid.

Example 2:
Input: states = [0,0,1,1,1,0,1,1,0], k = 2
Output: 7
Explanation: Convert the awake minutes at indices 5 and 8, giving a longest contiguous valid streak from index 2 to index 8 with length 7.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is visited at most twice:
      1) once when the right pointer expands the window
      2) once when the left pointer shrinks the window
    - This makes the total work linear in the size of the array.

    Space Complexity: O(1)
    - We only use a few integer variables.
    - No extra arrays, lists, or hash-based data structures are needed.
    */
    public int LongestSleepStreak(int[] states, int k)
    {
        // We will use the classic "sliding window" technique.
        //
        // Core idea:
        // We want the longest contiguous segment that contains at most k zeros.
        // Why zeros?
        // Because zeros represent awake minutes, and we are allowed to "ignore" or
        // convert at most k of them into sleep.
        //
        // So the problem becomes:
        // "Find the longest subarray with at most k zeros."

        // left marks the beginning of the current window.
        int left = 0;

        // zeroCount stores how many awake minutes (0 values) are currently inside the window.
        int zeroCount = 0;

        // best stores the maximum valid window length we have seen so far.
        int best = 0;

        // right marks the end of the current window.
        // We expand the window one element at a time.
        for (int right = 0; right < states.Length; right++)
        {
            // Step 1: Include states[right] in the current window.
            //
            // If the new element is 0, then our window now contains one more awake minute.
            // We must track this because the window is only valid if zeroCount <= k.
            if (states[right] == 0)
            {
                zeroCount++;
            }

            // Step 2: If the window has too many zeros, it is invalid.
            //
            // "Too many zeros" means we would need more than k conversions,
            // which is not allowed.
            //
            // So we shrink the window from the left until it becomes valid again.
            while (zeroCount > k)
            {
                // Before moving left forward, check whether the element leaving the window
                // is a zero. If it is, then the number of zeros inside the window decreases.
                if (states[left] == 0)
                {
                    zeroCount--;
                }

                // Move the left boundary rightward to make the window smaller.
                left++;
            }

            // Step 3: At this point, the window [left..right] is valid.
            //
            // That means it contains at most k zeros, so it represents a contiguous sleep streak
            // after converting those zeros.
            //
            // Compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is the largest seen so far.
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After scanning the entire array, best contains the maximum valid streak length.
        return best;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] states1 = { 1, 1, 0, 1, 0, 1, 1, 1 };
int k1 = 1;
int result1 = solution.LongestSleepStreak(states1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 5

// Example 2
int[] states2 = { 0, 0, 1, 1, 1, 0, 1, 1, 0 };
int k2 = 2;
int result2 = solution.LongestSleepStreak(states2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 7

// Additional demo cases for learning and verification

// All asleep already
int[] states3 = { 1, 1, 1, 1, 1 };
int k3 = 0;
int result3 = solution.LongestSleepStreak(states3, k3);
Console.WriteLine("All asleep, k=0: " + result3); // Expected: 5

// All awake, but enough conversions to cover all
int[] states4 = { 0, 0, 0, 0 };
int k4 = 4;
int result4 = solution.LongestSleepStreak(states4, k4);
Console.WriteLine("All awake, k=4: " + result4); // Expected: 4

// No conversions allowed
int[] states5 = { 1, 0, 1, 1, 0, 1 };
int k5 = 0;
int result5 = solution.LongestSleepStreak(states5, k5);
Console.WriteLine("No conversions allowed: " + result5); // Expected: 2