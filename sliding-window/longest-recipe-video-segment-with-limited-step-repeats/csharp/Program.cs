/*
Title: Longest Recipe Video Segment With Limited Step Repeats
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array steps where steps[i] is the step ID shown at second i of a cooking tutorial video.
The same step may appear multiple times in a row or reappear later because the editor repeats important
actions such as chopping, mixing, or plating. You are also given an integer k.

A video segment is considered watchable if, within that contiguous segment, no single step ID appears
more than k times.

Your task is to return the length of the longest watchable contiguous segment.

In other words, find the maximum size of a subarray of steps such that the frequency of every distinct
value inside that subarray is at most k.

This problem models a realistic streaming feature: the app wants to recommend the longest clip that does
not over-repeat any one instruction beyond a user-defined tolerance.

Constraints:
- 1 <= steps.length <= 200000
- 1 <= steps[i] <= 1000000000
- 1 <= k <= steps.length
- The answer must be computed in O(n) or O(n log n) time for full credit.

Example 1:
Input: steps = [4, 2, 4, 3, 2, 4, 1], k = 2
Output: 5
Explanation: One longest valid segment is [4, 2, 4, 3, 2]. In this segment, step 4 appears 2 times,
step 2 appears 2 times, and step 3 appears 1 time. Extending the segment to include the next 4 would
make step 4 appear 3 times, which is not allowed.

Example 2:
Input: steps = [7, 7, 7, 8, 8, 9, 7], k = 1
Output: 3
Explanation: A longest valid segment is [7, 8, 9] or [8, 9, 7]. Any longer segment would contain some
step ID more than once.

We return only the maximum length, not the segment itself.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the sliding window once by moving the right pointer.
    - Each element is removed from the sliding window at most once by moving the left pointer.
    - Therefore, the total work done by both pointers together is linear.

    Space Complexity: O(n)
    - In the worst case, the dictionary may store counts for many distinct step IDs currently inside the window.
    - In the worst case, that can be up to the number of elements in the array.
    */
    public int LongestWatchableSegment(int[] steps, int k)
    {
        // This dictionary stores:
        // key   = a step ID
        // value = how many times that step ID currently appears inside our sliding window
        //
        // Why use a Dictionary?
        // - Step IDs can be as large as 1,000,000,000, so we should not use an array indexed by step ID.
        // - A dictionary lets us track only the values that actually appear.
        var frequency = new Dictionary<int, int>();

        // "left" is the starting index of our current window.
        // The window will always be from left to right, inclusive.
        int left = 0;

        // This will store the best (maximum) valid window length we have seen so far.
        int maxLength = 0;

        // We expand the window one element at a time using "right".
        for (int right = 0; right < steps.Length; right++)
        {
            // Step 1: Include steps[right] into the current window.
            //
            // We are extending the window to the right by one position.
            // That means the step ID at index "right" is now part of the window,
            // so we must update its frequency count.
            int currentStep = steps[right];

            if (!frequency.ContainsKey(currentStep))
            {
                frequency[currentStep] = 0;
            }

            frequency[currentStep]++;

            // Step 2: If adding this new step makes the window invalid,
            // shrink the window from the left until it becomes valid again.
            //
            // Why do we only need to check frequency[currentStep] > k?
            // Because before adding steps[right], the window was already valid.
            // The only count that changed is the count of currentStep.
            // So only currentStep could possibly violate the rule now.
            while (frequency[currentStep] > k)
            {
                // The element at "left" is about to leave the window,
                // so we decrease its count.
                int leftStep = steps[left];
                frequency[leftStep]--;

                // Move the left boundary one step to the right.
                // This makes the window smaller and helps restore validity.
                left++;
            }

            // Step 3: At this point, the window [left..right] is valid.
            //
            // That means no step ID appears more than k times in this window.
            // So we can safely compute its length and compare it with our best answer.
            int currentLength = right - left + 1;

            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After processing all possible right endpoints,
        // maxLength contains the longest valid contiguous segment.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] steps1 = { 4, 2, 4, 3, 2, 4, 1 };
int k1 = 2;
int result1 = solution.LongestWatchableSegment(steps1, k1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] steps2 = { 7, 7, 7, 8, 8, 9, 7 };
int k2 = 1;
int result2 = solution.LongestWatchableSegment(steps2, k2);
Console.WriteLine(result2); // Expected: 3

// Additional quick sanity checks
int[] steps3 = { 1, 2, 3, 4 };
int k3 = 1;
Console.WriteLine(solution.LongestWatchableSegment(steps3, k3)); // Expected: 4

int[] steps4 = { 5, 5, 5, 5 };
int k4 = 2;
Console.WriteLine(solution.LongestWatchableSegment(steps4, k4)); // Expected: 2

int[] steps5 = { 1 };
int k5 = 1;
Console.WriteLine(solution.LongestWatchableSegment(steps5, k5)); // Expected: 1