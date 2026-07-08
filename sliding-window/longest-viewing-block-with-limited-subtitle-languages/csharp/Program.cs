/*
Title: Longest Viewing Block With Limited Subtitle Languages
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A streaming platform stores the subtitle language used for each minute of a live broadcast in an array `languages`,
where `languages[i]` is a string such as "en", "es", or "fr".

A user wants to watch one continuous block of the broadcast, but they are only comfortable switching between at most
`k` distinct subtitle languages during that block.

Your task is to return the length of the longest contiguous segment of `languages` that contains at most `k`
distinct language codes.

This models a realistic product analytics problem: find the longest uninterrupted viewing interval that stays within
a user's subtitle tolerance. The segment must be contiguous, and repeated occurrences of the same language do not
increase the distinct count.

Write a function that computes the maximum possible length.

Constraints:
- 1 <= languages.length <= 200000
- 1 <= languages[i].length <= 10
- languages[i] consists of lowercase English letters
- 1 <= k <= languages.length

Example 1:
Input: languages = ["en","en","es","es","fr","es","es"], k = 2
Output: 4
Explanation: The longest valid block is ["en","en","es","es"] or ["es","fr","es","es"], each with length 4 and at most 2 distinct languages.

Example 2:
Input: languages = ["jp","kr","jp","cn","cn","jp","jp"], k = 1
Output: 2
Explanation: With only 1 distinct language allowed, the best contiguous block is ["cn","cn"] or ["jp","jp"], so the answer is 2.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each language enters the sliding window once when the right pointer moves forward.
    - Each language leaves the sliding window at most once when the left pointer moves forward.
    - Therefore, both pointers together move at most 2 * n times.

    Space Complexity: O(k) in the typical valid-window sense, and O(min(n, number of distinct languages)))
    - We store counts of languages currently inside the window in a dictionary.
    - In the worst case, the dictionary can temporarily hold as many distinct languages as appear in the array.
    */
    public int LengthOfLongestViewingBlock(string[] languages, int k)
    {
        // This dictionary stores how many times each language appears in the CURRENT window.
        //
        // Why do we need counts instead of just a set?
        // Because when we move the left side of the window forward, we need to know whether
        // removing one occurrence of a language means that language is still present in the window
        // or has disappeared completely.
        //
        // Example:
        // Window = ["en", "en", "es"]
        // If we remove one "en", the window still contains "en".
        // So we must track frequencies, not just presence/absence.
        var frequency = new Dictionary<string, int>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window by moving right from 0 to the end of the array.
        for (int right = 0; right < languages.Length; right++)
        {
            // Step 1: Include the new language at index 'right' into the window.
            //
            // Current window before adding:
            // languages[left..right-1]
            //
            // Current window after adding:
            // languages[left..right]
            string currentLanguage = languages[right];

            if (!frequency.ContainsKey(currentLanguage))
            {
                frequency[currentLanguage] = 0;
            }

            frequency[currentLanguage]++;

            // Step 2: If the window now contains more than k distinct languages,
            // it is invalid and must be shrunk from the left.
            //
            // Why do we shrink in a while loop?
            // Because adding one new language can make the window invalid,
            // and we may need to remove multiple elements from the left
            // before the number of distinct languages becomes <= k again.
            while (frequency.Count > k)
            {
                // Identify the language that is leaving the window from the left side.
                string leftLanguage = languages[left];

                // Decrease its count because it is no longer part of the window.
                frequency[leftLanguage]--;

                // If its count becomes zero, that language is no longer present in the window at all.
                // We remove it from the dictionary so that frequency.Count correctly reflects
                // the number of distinct languages currently inside the window.
                if (frequency[leftLanguage] == 0)
                {
                    frequency.Remove(leftLanguage);
                }

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3: At this point, the window is guaranteed to be valid:
            // it contains at most k distinct languages.
            //
            // So we can safely compute its length and compare it with the best answer seen so far.
            int currentWindowLength = right - left + 1;

            if (currentWindowLength > best)
            {
                best = currentWindowLength;
            }
        }

        // After scanning the entire array, best contains the length of the longest valid contiguous segment.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] languages1 = { "en", "en", "es", "es", "fr", "es", "es" };
int k1 = 2;
int result1 = solution.LengthOfLongestViewingBlock(languages1, k1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 4

// Example 2
string[] languages2 = { "jp", "kr", "jp", "cn", "cn", "jp", "jp" };
int k2 = 1;
int result2 = solution.LengthOfLongestViewingBlock(languages2, k2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 2

// Additional demo
string[] languages3 = { "en", "fr", "en", "fr", "en", "de", "de", "fr" };
int k3 = 2;
int result3 = solution.LengthOfLongestViewingBlock(languages3, k3);
Console.WriteLine($"Additional Demo Result: {result3}");