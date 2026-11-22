/*
Title: Minimum Edit Cost to Form K Palindrome Chapters

Problem Description:
You are given a lowercase string s representing a draft manuscript and an integer k.
The editor wants to divide the manuscript into exactly k non-empty contiguous chapters.
After the split, each chapter may be edited independently so that the final text of every
chapter is a palindrome.

In one edit, you may change any single character in a chapter to any other lowercase
English letter. The cost of a chapter is the minimum number of character changes required
to turn that chapter into a palindrome. Your task is to return the minimum total edit cost
over all ways to split s into exactly k chapters.

A palindrome reads the same forward and backward. For example, the chapter "abca" can be
turned into a palindrome with 1 edit, because changing either 'b' to 'c' or 'c' to 'b'
makes it symmetric. The split must preserve order, and every character of s must belong
to exactly one chapter.

Return the minimum possible total number of edits.

Constraints:
- 1 <= s.length <= 200
- 1 <= k <= s.length
- s consists only of lowercase English letters

Example 1:
Input: s = "abcac", k = 2
Output: 1

Example 2:
Input: s = "leetcode", k = 3
Output: 2
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Precomputing palindrome conversion cost for every substring: O(n^2)
    - Partition dynamic programming: O(k * n^2)
    - Total: O(k * n^2)

    Space Complexity:
    - Cost table for all substrings: O(n^2)
    - DP table: O(n * k)
    - Total: O(n^2)
    */
    public int PalindromePartition(string s, int k)
    {
        int n = s.Length;

        // ------------------------------------------------------------
        // STEP 1: Precompute the edit cost for every substring s[i..j].
        // ------------------------------------------------------------
        //
        // Why do we need this?
        // During the partition DP, we will repeatedly ask:
        // "If I make substring s[p..i-1] into one chapter, how many edits
        //  are needed to turn that chapter into a palindrome?"
        //
        // If we compute that answer from scratch every time, the solution
        // becomes too slow.
        //
        // So we build a 2D table:
        // cost[i, j] = minimum edits needed to convert s[i..j] into a palindrome
        //
        // Key observation:
        // A substring is a palindrome if its outer characters match and its inner
        // substring is also a palindrome.
        //
        // Therefore:
        // cost[i, j] = cost[i+1, j-1] + (s[i] == s[j] ? 0 : 1)
        //
        // This works because for each mirrored pair:
        // - if characters are equal, no edit is needed for that pair
        // - if characters differ, exactly one edit is enough to make them equal
        //
        // We fill shorter substrings first so that cost[i+1, j-1] is already known
        // when we need it.
        int[,] cost = new int[n, n];

        for (int len = 2; len <= n; len++)
        {
            for (int i = 0; i + len - 1 < n; i++)
            {
                int j = i + len - 1;

                // Start with the mismatch cost of the two outer characters.
                int mismatchCost = s[i] == s[j] ? 0 : 1;

                if (len == 2)
                {
                    // For a 2-character substring, there is no inner substring.
                    // The cost is simply whether the two characters match.
                    cost[i, j] = mismatchCost;
                }
                else
                {
                    // For longer substrings, combine:
                    // - the cost to fix the inner substring
                    // - the cost to fix the outer pair if needed
                    cost[i, j] = cost[i + 1, j - 1] + mismatchCost;
                }
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Dynamic programming for partitioning into exactly k chapters.
        // ------------------------------------------------------------
        //
        // We define:
        // dp[i, parts] = minimum total edit cost to split the first i characters
        //                of s into exactly 'parts' non-empty palindrome chapters.
        //
        // Important indexing detail:
        // - i is a length, not an index
        // - first i characters means substring s[0..i-1]
        //
        // Example:
        // dp[5, 2] means:
        // "minimum cost to split s[0..4] into exactly 2 chapters"
        //
        // Transition:
        // Suppose the last chapter starts at position p and ends at i-1.
        // Then:
        // - the first p characters must be split into parts-1 chapters
        // - the last chapter is s[p..i-1]
        //
        // So:
        // dp[i, parts] = min over p from parts-1 to i-1 of
        //                dp[p, parts-1] + cost[p, i-1]
        //
        // Why does p start at parts-1?
        // Because to split the first p characters into parts-1 non-empty chapters,
        // we need at least parts-1 characters.
        //
        // We use a large number INF to represent impossible states.
        int INF = 1_000_000_000;
        int[,] dp = new int[n + 1, k + 1];

        for (int i = 0; i <= n; i++)
        {
            for (int parts = 0; parts <= k; parts++)
            {
                dp[i, parts] = INF;
            }
        }

        // Base case:
        // Splitting 0 characters into 0 chapters costs 0.
        dp[0, 0] = 0;

        // Fill the DP table chapter count by chapter count.
        for (int parts = 1; parts <= k; parts++)
        {
            // To create 'parts' non-empty chapters, we need at least 'parts' characters.
            for (int i = parts; i <= n; i++)
            {
                // Try every possible starting position p of the last chapter.
                for (int p = parts - 1; p <= i - 1; p++)
                {
                    // If the previous state is impossible, skip it.
                    if (dp[p, parts - 1] == INF)
                    {
                        continue;
                    }

                    // Cost of making the last chapter s[p..i-1] a palindrome.
                    int lastChapterCost = cost[p, i - 1];

                    // Total cost if we choose this split point p.
                    int candidate = dp[p, parts - 1] + lastChapterCost;

                    // Keep the best (minimum) answer.
                    if (candidate < dp[i, parts])
                    {
                        dp[i, parts] = candidate;
                    }
                }
            }
        }

        // The answer is the minimum cost to split all n characters into exactly k chapters.
        return dp[n, k];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

string s1 = "abcac";
int k1 = 2;
int result1 = solution.PalindromePartition(s1, k1);
Console.WriteLine($"Input: s = \"{s1}\", k = {k1}");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 1");
Console.WriteLine();

string s2 = "leetcode";
int k2 = 3;
int result2 = solution.PalindromePartition(s2, k2);
Console.WriteLine($"Input: s = \"{s2}\", k = {k2}");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 2");
Console.WriteLine();

string s3 = "aabbc";
int k3 = 3;
int result3 = solution.PalindromePartition(s3, k3);
Console.WriteLine($"Input: s = \"{s3}\", k = {k3}");
Console.WriteLine($"Output: {result3}");