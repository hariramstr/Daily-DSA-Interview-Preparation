/*
Title: Longest Podcast Queue Within Download Cap

Problem Description:
You are building an offline listening feature for a podcast app. The app shows episodes in a fixed order,
and a user wants to download a contiguous block of episodes to listen to later. Each episode has a download
size in megabytes. Because the user has limited storage, the total size of the chosen block cannot exceed
a given capacity cap.

Given an integer array sizes where sizes[i] is the size of the i-th episode, return the length of the
longest contiguous subarray whose total sum is less than or equal to cap.

If multiple valid subarrays have the same maximum length, returning the length alone is sufficient.

This problem should be solved efficiently using a sliding window approach.

Examples:
1) sizes = [40, 20, 30, 50, 10, 10], cap = 70
   Output: 3
   Explanation: The longest valid contiguous block is [50, 10, 10], whose total size is 70.

2) sizes = [90, 30, 20, 40], cap = 60
   Output: 2
   Explanation: The best block is [20, 40] with total size 60. Episode 90 alone does not fit.

Constraints:
- 1 <= sizes.length <= 200000
- 1 <= sizes[i] <= 100000
- 1 <= cap <= 10^15
- Return 0 if no single episode fits within cap.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Why?
    - Each element is added to the sliding window once when the right pointer moves forward.
    - Each element is removed from the sliding window at most once when the left pointer moves forward.
    - Therefore, the total amount of work is linear in the number of episodes.
    */
    public int LongestPodcastQueueWithinCap(int[] sizes, long cap)
    {
        // The left boundary of our current sliding window.
        // The window will always represent a contiguous block: sizes[left..right].
        int left = 0;

        // This stores the sum of all episode sizes currently inside the window.
        // We use long because:
        // - sizes[i] can be up to 100000
        // - there can be up to 200000 elements
        // - the total can exceed int range
        long currentSum = 0;

        // This stores the best (maximum) valid window length found so far.
        int bestLength = 0;

        // We expand the window one episode at a time by moving "right" from left to right.
        for (int right = 0; right < sizes.Length; right++)
        {
            // STEP 1: Include the new episode at index "right" into the current window.
            // This means our window grows by one element on the right side.
            currentSum += sizes[right];

            // STEP 2: If the window is now invalid (sum > cap), we must shrink it from the left.
            // Why shrink from the left?
            // Because the problem requires a contiguous subarray, and the sliding window technique
            // maintains contiguity by only moving the two ends of the window.
            //
            // We keep shrinking until the total size is <= cap again.
            while (left <= right && currentSum > cap)
            {
                // Remove the episode at the current left boundary from the sum,
                // because we are about to move left forward by one.
                currentSum -= sizes[left];

                // Move the left boundary rightward, making the window smaller.
                left++;
            }

            // STEP 3: At this point, the window sizes[left..right] is valid
            // (or possibly empty in edge cases where a single element was too large and got removed).
            //
            // We can now compute its length.
            int currentLength = right - left + 1;

            // STEP 4: Update the best answer if this valid window is longer than anything seen before.
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // After processing all possible right endpoints, bestLength contains the answer.
        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] sizes1 = { 40, 20, 30, 50, 10, 10 };
long cap1 = 70;
int result1 = solution.LongestPodcastQueueWithinCap(sizes1, cap1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 3

// Example 2
int[] sizes2 = { 90, 30, 20, 40 };
long cap2 = 60;
int result2 = solution.LongestPodcastQueueWithinCap(sizes2, cap2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 2

// Additional demo: no episode fits
int[] sizes3 = { 100, 200, 300 };
long cap3 = 50;
int result3 = solution.LongestPodcastQueueWithinCap(sizes3, cap3);
Console.WriteLine($"Example 3 Result: {result3}"); // Expected: 0

// Additional demo: entire array fits
int[] sizes4 = { 10, 15, 20 };
long cap4 = 100;
int result4 = solution.LongestPodcastQueueWithinCap(sizes4, cap4);
Console.WriteLine($"Example 4 Result: {result4}"); // Expected: 3