/*
Title: Longest Lecture Segment With Limited Slide Repeats
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array slides where slides[i] is the ID of the slide shown at minute i during a recorded lecture.
Because instructors sometimes revisit the same slide multiple times, the video platform wants to extract the
longest contiguous segment that is still easy for students to review.

A segment is considered review-friendly if no slide ID appears more than k times inside that segment.

Return the length of the longest contiguous subarray of slides that is review-friendly.

In other words, find the maximum window [l, r] such that for every distinct slide ID in slides[l...r],
its frequency within that window is at most k.

This is a realistic stream-processing problem: the answer must be based on a contiguous time interval,
not on reordering or deleting arbitrary elements outside the chosen interval.

Constraints:
- 1 <= slides.length <= 200000
- 1 <= slides[i] <= 1000000000
- 1 <= k <= slides.length
- The solution should run efficiently for large inputs.

Example 1:
Input: slides = [4, 2, 4, 3, 2, 4, 2, 5], k = 2
Output: 5
Explanation: One optimal segment is [4, 3, 2, 4, 2], which has slide 4 appearing 2 times and slide 2 appearing 2 times.
No slide appears more than 2 times, so the segment length is 5.

Example 2:
Input: slides = [7, 7, 7, 1, 2, 1, 2, 3], k = 1
Output: 4
Explanation: With k = 1, all slide IDs in the chosen segment must be unique.
One optimal segment is [7, 1, 2, 3], whose length is 4.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Why:
    - Each element is added to the sliding window once by moving the right pointer.
    - Each element is removed from the sliding window at most once by moving the left pointer.
    - Therefore, the total amount of pointer movement is linear.
    - We use a Dictionary<int, int> to store frequencies of slide IDs currently inside the window.
    */
    public int LongestReviewFriendlySegment(int[] slides, int k)
    {
        // This dictionary keeps track of how many times each slide ID appears
        // inside the CURRENT window [left, right].
        //
        // Key   = slide ID
        // Value = frequency of that slide ID in the current window
        //
        // We choose Dictionary because:
        // - slide IDs can be as large as 1,000,000,000
        // - they are not guaranteed to be small or continuous
        // - dictionary gives average O(1) insert, lookup, and update
        var frequency = new Dictionary<int, int>();

        // "left" is the start index of our sliding window.
        int left = 0;

        // "best" stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < slides.Length; right++)
        {
            int currentSlide = slides[right];

            // Step 1: Include slides[right] into the window.
            //
            // We are extending the current window from [left, right - 1] to [left, right].
            // So we must update the frequency count for this newly included slide.
            if (!frequency.ContainsKey(currentSlide))
            {
                frequency[currentSlide] = 0;
            }

            frequency[currentSlide]++;

            // Step 2: If adding this slide caused its frequency to exceed k,
            // then the current window is no longer valid.
            //
            // Important observation:
            // Before adding slides[right], the window was valid.
            // After adding one element, only that specific element's frequency
            // can become invalid. No other frequency changed.
            //
            // Therefore, we only need to repair the window while
            // frequency[currentSlide] > k.
            while (frequency[currentSlide] > k)
            {
                int leftSlide = slides[left];

                // We are shrinking the window from the left side.
                // That means slides[left] is leaving the window,
                // so we must decrease its frequency.
                frequency[leftSlide]--;

                // Move left forward to represent the smaller window.
                left++;

                // We do not need to check every key in the dictionary.
                // The only possible violation came from currentSlide,
                // because that was the only count we increased.
                // Once frequency[currentSlide] <= k, the whole window is valid again.
            }

            // Step 3: At this point, the window [left, right] is valid.
            //
            // That means every slide ID appears at most k times in this window.
            // So we can safely compute its length and compare it with the best answer.
            int currentLength = right - left + 1;

            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After processing all possible right endpoints,
        // "best" contains the maximum valid window length.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] slides1 = { 4, 2, 4, 3, 2, 4, 2, 5 };
int k1 = 2;
int result1 = solution.LongestReviewFriendlySegment(slides1, k1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] slides2 = { 7, 7, 7, 1, 2, 1, 2, 3 };
int k2 = 1;
int result2 = solution.LongestReviewFriendlySegment(slides2, k2);
Console.WriteLine(result2); // Expected: 4

// Additional quick checks
int[] slides3 = { 1, 1, 1, 1 };
int k3 = 2;
Console.WriteLine(solution.LongestReviewFriendlySegment(slides3, k3)); // Expected: 2

int[] slides4 = { 1, 2, 3, 4, 5 };
int k4 = 1;
Console.WriteLine(solution.LongestReviewFriendlySegment(slides4, k4)); // Expected: 5