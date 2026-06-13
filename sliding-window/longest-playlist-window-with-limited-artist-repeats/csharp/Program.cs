/*
Title: Longest Playlist Window With Limited Artist Repeats
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A music streaming service stores a listening session as an array artists, where artists[i]
is the artist ID of the i-th song played in order. To keep a generated playlist feeling varied,
the service wants to find the longest contiguous block of songs such that no single artist
appears more than k times inside that block.

Your task is to return the length of the longest contiguous subarray of artists satisfying
this rule. In other words, among all windows [l..r], find the maximum window size where every
distinct artist appears at most k times.

This is a realistic streaming constraint problem: duplicates are allowed, the same artist may
appear many times overall, and only contiguous segments count. If k = 0, then no song can be
included, so the answer is 0.

Constraints:
- 1 <= artists.length <= 200000
- 1 <= artists[i] <= 1000000000
- 0 <= k <= artists.length

Example 1:
Input: artists = [4, 1, 4, 2, 4, 1, 3], k = 2
Output: 5
Explanation: The longest valid window is [1, 4, 2, 4, 1]. In this window, artist 4 appears
2 times, artist 1 appears 2 times, and artist 2 appears 1 time.

Example 2:
Input: artists = [7, 7, 7, 2, 2, 3, 7], k = 1
Output: 3
Explanation: One longest valid window is [7, 2, 3]. Any longer window would cause some artist
to appear more than once.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(m), where m is the number of distinct artists currently tracked
                      (in the worst case, up to O(n))

    Why O(n)?
    - The right pointer moves from left to right exactly once.
    - The left pointer also only moves from left to right, never backward.
    - Each song enters the window once and leaves the window once.
    - Dictionary operations are expected O(1) on average.

    Core idea:
    We use a sliding window [left..right].
    - Expand the window by moving right.
    - Track how many times each artist appears in the current window.
    - If adding the new song causes its artist count to exceed k, the window becomes invalid.
    - Then shrink from the left until the window becomes valid again.
    - At every valid step, update the best window length found so far.
    */
    public int LongestPlaylistWindow(int[] artists, int k)
    {
        // Special case:
        // If k == 0, the rule says no artist may appear even once.
        // That means no song can be included in any valid window.
        // Therefore, the answer must be 0 immediately.
        if (k == 0)
        {
            return 0;
        }

        // This dictionary stores:
        // key   = artist ID
        // value = how many times that artist appears in the current window
        //
        // We choose Dictionary<int, int> because:
        // - artist IDs can be as large as 1,000,000,000
        // - so using an array indexed by artist ID would be wasteful or impossible
        // - dictionary gives us efficient average O(1) insert/update/lookup
        var counts = new Dictionary<int, int>();

        // left marks the start of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length seen so far.
        int best = 0;

        // We extend the window one song at a time by moving right.
        for (int right = 0; right < artists.Length; right++)
        {
            int currentArtist = artists[right];

            // Step 1: Include the new song at position right into the window.
            //
            // We increase the count for this artist because the current window now includes it.
            if (!counts.ContainsKey(currentArtist))
            {
                counts[currentArtist] = 0;
            }
            counts[currentArtist]++;

            // Step 2: Check whether the window is still valid.
            //
            // Important observation:
            // Before adding artists[right], the window was valid.
            // After adding one song, only the count of currentArtist changed.
            // Therefore, if the window becomes invalid, it can only be because
            // currentArtist now appears more than k times.
            //
            // So we only need to shrink while counts[currentArtist] > k.
            while (counts[currentArtist] > k)
            {
                int leftArtist = artists[left];

                // Remove the song at the left edge from the window,
                // because we are about to move left forward.
                counts[leftArtist]--;

                // Optional cleanup:
                // If an artist count becomes 0, we can remove it from the dictionary.
                // This is not required for correctness, but it keeps the dictionary smaller
                // and makes the current state easier to reason about.
                if (counts[leftArtist] == 0)
                {
                    counts.Remove(leftArtist);
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // Step 3: At this point, the window [left..right] is valid again.
            //
            // Why is it valid?
            // - We kept shrinking until the only possible violating artist
            //   (currentArtist) no longer exceeds k.
            // - All other artists were already valid before this iteration
            //   and only decreased or stayed the same while shrinking.
            //
            // So now every artist appears at most k times.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is larger.
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After scanning all possible right endpoints, best is the answer.
        return best;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1:
// artists = [4, 1, 4, 2, 4, 1, 3], k = 2
// Expected output: 5
int[] artists1 = { 4, 1, 4, 2, 4, 1, 3 };
int k1 = 2;
int result1 = solution.LongestPlaylistWindow(artists1, k1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// artists = [7, 7, 7, 2, 2, 3, 7], k = 1
// Expected output: 3
int[] artists2 = { 7, 7, 7, 2, 2, 3, 7 };
int k2 = 1;
int result2 = solution.LongestPlaylistWindow(artists2, k2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo for the edge case k = 0:
// No song can be included, so expected output: 0
int[] artists3 = { 5, 1, 5, 2 };
int k3 = 0;
int result3 = solution.LongestPlaylistWindow(artists3, k3);
Console.WriteLine($"Edge Case (k=0) Result: {result3}");