/*
Title: Longest Citation Window With Per-Author Cap

Problem Description:
You are given a list of citations appearing in a research draft, in the exact order they occur in the document.
Each citation is represented by an author ID string in the array authors, where authors[i] is the author cited
at position i.

To reduce over-reliance on a small set of sources, the editor requires that in any accepted contiguous passage,
no author may appear more than limit times.

Your task is to return the length of the longest contiguous subarray of authors such that every distinct author
appears at most limit times inside that window.

Formally, find the maximum value of r - l + 1 over all indices 0 <= l <= r < authors.length such that for every
author ID x, the number of occurrences of x in authors[l..r] is at most limit.

Constraints:
- 1 <= authors.length <= 2 * 10^5
- 1 <= authors[i].length <= 20
- authors[i] consists of lowercase English letters, digits, or underscores
- 1 <= limit <= authors.length

Examples:
1)
Input: authors = ["lee","kim","lee","patel","kim","lee","ng"], limit = 2
Output: 5

2)
Input: authors = ["a","b","a","c","a","b","b","d"], limit = 1
Output: 3
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n), where n is the number of citations/authors.
    - Each element is added to the sliding window once and removed from the sliding window at most once.

    Space Complexity:
    - O(k), where k is the number of distinct author IDs currently tracked in the frequency dictionary.
    - In the worst case, this can be O(n) if all author IDs are distinct.

    Core idea:
    - Use a sliding window [left..right].
    - Expand right one step at a time.
    - Track how many times each author appears in the current window.
    - If adding the new author causes its count to exceed limit, shrink the window from the left
      until the window becomes valid again.
    - Record the maximum valid window length seen.
    */
    public int LongestCitationWindow(string[] authors, int limit)
    {
        // This dictionary stores:
        // key   -> author ID
        // value -> how many times that author appears in the current window
        //
        // Why a Dictionary<string, int>?
        // - Author IDs are arbitrary strings, not small integers.
        // - We need fast average O(1) insert/update/lookup.
        var frequency = new Dictionary<string, int>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We move right from 0 to authors.Length - 1, expanding the window one citation at a time.
        for (int right = 0; right < authors.Length; right++)
        {
            // Step 1:
            // Include authors[right] in the current window.
            string currentAuthor = authors[right];

            // If this author has not been seen in the current window before, initialize its count to 0.
            if (!frequency.ContainsKey(currentAuthor))
            {
                frequency[currentAuthor] = 0;
            }

            // Increase the count because currentAuthor is now part of the window [left..right].
            frequency[currentAuthor]++;

            // Step 2:
            // If the count of the newly added author exceeds the allowed limit,
            // then the current window is invalid.
            //
            // Important observation:
            // Before we added currentAuthor, the window was valid.
            // Therefore, the only possible violation after adding one element is that
            // currentAuthor itself now appears too many times.
            //
            // So we only need to shrink while frequency[currentAuthor] > limit.
            while (frequency[currentAuthor] > limit)
            {
                // The author at the left edge is about to be removed from the window.
                string leftAuthor = authors[left];

                // Decrease its frequency because we are shrinking the window from the left.
                frequency[leftAuthor]--;

                // Move left forward by one position.
                left++;
            }

            // Step 3:
            // At this point, the window [left..right] is valid:
            // every author appears at most limit times.
            //
            // Compute its length.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is larger than any previously seen.
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After processing all positions, best is the length of the longest valid window.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] authors1 = { "lee", "kim", "lee", "patel", "kim", "lee", "ng" };
int limit1 = 2;
int result1 = solution.LongestCitationWindow(authors1, limit1);
Console.WriteLine(result1); // Expected: 5

// Example 2
string[] authors2 = { "a", "b", "a", "c", "a", "b", "b", "d" };
int limit2 = 1;
int result2 = solution.LongestCitationWindow(authors2, limit2);
Console.WriteLine(result2); // Expected: 3

// Additional quick sanity check
string[] authors3 = { "x", "x", "x", "x" };
int limit3 = 2;
int result3 = solution.LongestCitationWindow(authors3, limit3);
Console.WriteLine(result3); // Expected: 2