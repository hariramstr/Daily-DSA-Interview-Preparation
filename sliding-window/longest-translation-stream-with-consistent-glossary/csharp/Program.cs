/*
Title: Longest Translation Stream With Consistent Glossary
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A localization platform stores a stream of translated terms as an array of strings `words`,
where each element is the translated form chosen for the next occurrence of a concept in a document.

Reviewers want to find the longest contiguous segment that is "glossary-consistent":
within that segment, no translated term appears more than `k` times.

Given an array `words` and an integer `k`, return the length of the longest contiguous subarray
such that every distinct string inside the subarray appears at most `k` times.

A subarray is a contiguous part of the array. If `k = 0`, the answer is always `0`
because no word may appear even once.

Constraints:
- 1 <= words.length <= 100000
- 0 <= k <= words.length
- 1 <= words[i].length <= 20
- words[i] consists of lowercase English letters

Example 1:
Input: words = ["uno","dos","uno","tres","dos","uno","cuatro"], k = 2
Output: 5
Explanation: The longest valid segment is ["uno","dos","uno","tres","dos"].
In this segment, "uno" appears 2 times, "dos" appears 2 times, and "tres" appears 1 time.

Example 2:
Input: words = ["bleu","bleu","bleu","vert","vert","jaune"], k = 1
Output: 3
Explanation: One longest valid segment is ["bleu","vert","jaune"].
Every distinct word appears at most once in the window.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each word is added to the sliding window once by moving the right pointer.
    - Each word is removed from the sliding window at most once by moving the left pointer.
    - Therefore, the total amount of work is linear in the number of words.

    Space Complexity: O(m)
    - We store counts for the distinct words currently seen.
    - In the worst case, all words are different, so m can be up to n.
    */
    public int LongestConsistentGlossarySegment(string[] words, int k)
    {
        // If k is 0, no word is allowed to appear even once.
        // That means no non-empty subarray can ever be valid.
        // So the answer must be 0 immediately.
        if (k == 0)
        {
            return 0;
        }

        // This dictionary keeps track of how many times each word appears
        // inside the current sliding window.
        //
        // Why a dictionary?
        // - We need fast updates when a word enters the window.
        // - We need fast updates when a word leaves the window.
        // - We need fast lookup to know whether a word count exceeded k.
        //
        // Dictionary<string, int> gives average O(1) insert/update/lookup.
        var frequency = new Dictionary<string, int>();

        // left marks the beginning of the current window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window by moving right from 0 to words.Length - 1.
        for (int right = 0; right < words.Length; right++)
        {
            // Step 1: Include the new word at position right into the window.
            string currentWord = words[right];

            // If the word has not been seen in the current window before,
            // initialize its count to 0 first.
            if (!frequency.ContainsKey(currentWord))
            {
                frequency[currentWord] = 0;
            }

            // Increase the count because currentWord is now part of the window.
            frequency[currentWord]++;

            // Step 2: If adding this word made the window invalid,
            // shrink the window from the left until it becomes valid again.
            //
            // Important observation:
            // Before adding currentWord, the window was valid.
            // After adding it, the only possible violation is that currentWord
            // may now appear more than k times.
            //
            // So we only need to check frequency[currentWord] > k.
            while (frequency[currentWord] > k)
            {
                // Identify the word that is leaving the window.
                string leftWord = words[left];

                // Decrease its count because it is no longer inside the window.
                frequency[leftWord]--;

                // Move the left boundary one step to the right.
                left++;

                // We do not need to remove keys with count 0 for correctness.
                // Keeping them is fine and keeps the code simple for beginners.
            }

            // Step 3: At this point, the window [left..right] is valid.
            // Every word in the window appears at most k times.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is longer.
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After scanning all possible right endpoints, best contains
        // the length of the longest valid contiguous segment.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] words1 = { "uno", "dos", "uno", "tres", "dos", "uno", "cuatro" };
int k1 = 2;
int result1 = solution.LongestConsistentGlossarySegment(words1, k1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
string[] words2 = { "bleu", "bleu", "bleu", "vert", "vert", "jaune" };
int k2 = 1;
int result2 = solution.LongestConsistentGlossarySegment(words2, k2);
Console.WriteLine($"Example 2 Result: {result2}");

// Extra demo: k = 0
string[] words3 = { "alpha", "beta", "alpha" };
int k3 = 0;
int result3 = solution.LongestConsistentGlossarySegment(words3, k3);
Console.WriteLine($"Extra Example (k=0) Result: {result3}");