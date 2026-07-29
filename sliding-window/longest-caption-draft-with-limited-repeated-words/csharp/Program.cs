/*
Title: Longest Caption Draft With Limited Repeated Words
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A social media team is drafting a caption represented as an array of lowercase words `words`,
where `words[i]` is the ith word in order.

To keep the caption varied, the team wants to select one contiguous block of words such that
no single distinct word appears more than `k` times inside that block.

Return the length of the longest contiguous subarray of `words` that satisfies this rule.

In other words, among all windows `words[l...r]`, find the maximum size of a window where
the frequency of every word in that window is at most `k`.

This problem should be solved efficiently for large inputs, so an approach that checks every
possible subarray will not pass. A sliding window with frequency tracking is expected.

Constraints:
- 1 <= words.length <= 200000
- 1 <= words[i].length <= 20
- words[i] contains only lowercase English letters
- 1 <= k <= words.length

Example 1:
Input: words = ["sale","new","sale","trend","sale","new"], k = 2
Output: 4
Explanation:
One valid longest window is ["new","sale","trend","sale"].
In this window, "sale" appears 2 times, and every other word appears at most 1 time.
Any longer window would contain "sale" 3 times.

Example 2:
Input: words = ["a","b","a","c","b","b","d"], k = 1
Output: 3
Explanation:
Since each word may appear at most once, the answer is the longest contiguous block of distinct words.
Valid windows of length 3 include ["a","c","b"] and ["c","b","d"].
No valid window of length 4 exists.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each word is added to the sliding window once by the right pointer.
    - Each word is removed from the sliding window at most once by the left pointer.
    - Therefore, the total amount of work is linear in the number of words.

    Space Complexity: O(m)
    - We store frequencies of words currently seen in a dictionary.
    - In the worst case, m is the number of distinct words in the array.
    */
    public int LongestValidCaptionBlock(string[] words, int k)
    {
        // This dictionary stores how many times each word appears
        // inside the CURRENT sliding window.
        //
        // Why do we need it?
        // Because the rule says:
        // "No distinct word may appear more than k times in the chosen window."
        //
        // To verify that rule efficiently while the window moves,
        // we must know the frequency of each word currently inside the window.
        var frequency = new Dictionary<string, int>();

        // 'left' is the starting index of the current sliding window.
        // The current window is always words[left..right].
        int left = 0;

        // 'best' stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window one word at a time using 'right'.
        for (int right = 0; right < words.Length; right++)
        {
            string currentWord = words[right];

            // STEP 1: Add the new rightmost word into the window.
            //
            // We are extending the window from the right side,
            // so this word now belongs to the current window.
            if (!frequency.ContainsKey(currentWord))
            {
                frequency[currentWord] = 0;
            }

            frequency[currentWord]++;

            // STEP 2: If adding this word caused its frequency to exceed k,
            // then the window is no longer valid.
            //
            // Important observation:
            // Before adding words[right], the window was valid.
            // After adding it, the ONLY word that could have become invalid
            // is currentWord, because all other counts stayed the same.
            //
            // So we only need to shrink while frequency[currentWord] > k.
            while (frequency[currentWord] > k)
            {
                string leftWord = words[left];

                // Remove the leftmost word from the window,
                // because we are moving the left boundary to the right.
                frequency[leftWord]--;

                // Optional cleanup:
                // If a word count becomes zero, we can remove it from the dictionary.
                // This is not required for correctness, but it keeps the dictionary cleaner.
                if (frequency[leftWord] == 0)
                {
                    frequency.Remove(leftWord);
                }

                left++;
            }

            // STEP 3: At this point, the window is valid again.
            //
            // Why?
            // Because we kept shrinking until the only potentially invalid word
            // (currentWord) has frequency <= k.
            // Since all other words were already valid before, the whole window is valid now.
            int currentLength = right - left + 1;

            // STEP 4: Update the best answer if this valid window is larger.
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After checking all possible right endpoints,
        // 'best' contains the length of the longest valid contiguous block.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] words1 = { "sale", "new", "sale", "trend", "sale", "new" };
int k1 = 2;
int result1 = solution.LongestValidCaptionBlock(words1, k1);
Console.WriteLine(result1); // Expected: 4

// Example 2
string[] words2 = { "a", "b", "a", "c", "b", "b", "d" };
int k2 = 1;
int result2 = solution.LongestValidCaptionBlock(words2, k2);
Console.WriteLine(result2); // Expected: 3

// Additional quick checks
string[] words3 = { "x" };
int k3 = 1;
Console.WriteLine(solution.LongestValidCaptionBlock(words3, k3)); // Expected: 1

string[] words4 = { "one", "one", "one", "one" };
int k4 = 2;
Console.WriteLine(solution.LongestValidCaptionBlock(words4, k4)); // Expected: 2