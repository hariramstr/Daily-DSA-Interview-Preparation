/*
Title: Longest Caption Feed With Limited Hashtag Overload

Problem Description:
A social media analytics team is reviewing a chronological feed of post captions.
Each caption is represented by an integer in the array `hashtags`, where the value
is the hashtag ID used in that post.

A feed segment is considered valid if no hashtag appears more than `limit` times
inside that contiguous segment.

Task:
Given an array `hashtags` and an integer `limit`, return the length of the longest
valid contiguous segment.

In other words, find the maximum size of a subarray such that for every distinct
hashtag ID inside the subarray, its frequency is at most `limit`.

Examples:
1)
Input: hashtags = [4, 1, 4, 2, 2, 4, 3], limit = 2
Output: 5
Explanation:
A longest valid segment is [1, 4, 2, 2, 4].
Hashtag 4 appears 2 times and hashtag 2 appears 2 times, both allowed.
If we extend further, hashtag 4 would appear 3 times, which is invalid.

2)
Input: hashtags = [7, 7, 7, 8, 8, 9], limit = 1
Output: 3
Explanation:
When limit = 1, every value may appear at most once.
A longest valid segment is [7, 8, 9], so the answer is 3.

Efficient Idea:
Use a sliding window with two pointers:
- Expand the right side of the window
- Track frequencies of values inside the current window
- If adding a value makes its count exceed `limit`, shrink from the left
  until the window becomes valid again
- Track the maximum valid window length seen so far
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element enters the sliding window once when the right pointer moves.
    - Each element leaves the sliding window at most once when the left pointer moves.
    - Therefore, the total amount of work is linear in the size of the array.

    Space Complexity: O(k)
    - We store frequencies in a dictionary.
    - In the worst case, k is the number of distinct hashtag IDs in the current array.
    */
    public int LongestValidSegment(int[] hashtags, int limit)
    {
        // This dictionary stores:
        // key   -> hashtag ID
        // value -> how many times that hashtag currently appears inside our sliding window
        //
        // Why use a Dictionary?
        // Because hashtag IDs can be very large (up to 1,000,000,000), so using an array
        // indexed by hashtag ID would waste huge amounts of memory.
        // A dictionary lets us store only the values that actually appear.
        var frequency = new Dictionary<int, int>();

        // `left` is the start index of our current sliding window.
        int left = 0;

        // `bestLength` stores the maximum valid window size we have found so far.
        int bestLength = 0;

        // We move `right` from left to right across the array.
        // At each step, we try to include hashtags[right] into the current window.
        for (int right = 0; right < hashtags.Length; right++)
        {
            int currentHashtag = hashtags[right];

            // Step 1: Add the new rightmost element into the window.
            //
            // If the hashtag is already in the dictionary, increase its count.
            // Otherwise, start its count at 1.
            if (frequency.ContainsKey(currentHashtag))
            {
                frequency[currentHashtag]++;
            }
            else
            {
                frequency[currentHashtag] = 1;
            }

            // Step 2: Check whether the window is still valid.
            //
            // Important observation:
            // Before adding hashtags[right], the window was valid.
            // After adding it, only the count of `currentHashtag` changed.
            // So if the window becomes invalid, it can only be because
            // `currentHashtag` now appears more than `limit` times.
            //
            // Therefore, we only need to shrink while this specific hashtag
            // exceeds the allowed frequency.
            while (frequency[currentHashtag] > limit)
            {
                // We are going to remove the leftmost element from the window.
                int leftHashtag = hashtags[left];

                // Decrease its frequency because it is no longer inside the window.
                frequency[leftHashtag]--;

                // Move the left boundary one step to the right.
                left++;

                // We do not need to remove keys when count becomes 0 for correctness.
                // Keeping zero-count keys is harmless, but removing them is cleaner.
                if (frequency[leftHashtag] == 0)
                {
                    frequency.Remove(leftHashtag);
                }
            }

            // Step 3: At this point, the window [left..right] is valid.
            //
            // Why?
            // Because we kept shrinking until the only possible violating hashtag
            // (`currentHashtag`) no longer exceeds `limit`.
            // Since the window was valid before adding currentHashtag, and no other
            // counts increased, all frequencies are now within the allowed limit.
            int currentWindowLength = right - left + 1;

            // Step 4: Update the best answer seen so far.
            if (currentWindowLength > bestLength)
            {
                bestLength = currentWindowLength;
            }
        }

        // After scanning the whole array, `bestLength` is the answer.
        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// hashtags = [4, 1, 4, 2, 2, 4, 3], limit = 2
// Expected output: 5
int[] hashtags1 = { 4, 1, 4, 2, 2, 4, 3 };
int limit1 = 2;
int result1 = solution.LongestValidSegment(hashtags1, limit1);
Console.WriteLine(result1);

// Example 2:
// hashtags = [7, 7, 7, 8, 8, 9], limit = 1
// Expected output: 3
int[] hashtags2 = { 7, 7, 7, 8, 8, 9 };
int limit2 = 1;
int result2 = solution.LongestValidSegment(hashtags2, limit2);
Console.WriteLine(result2);