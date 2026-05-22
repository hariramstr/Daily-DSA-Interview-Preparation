/*
 * Title: Longest Subarray With Equal Symbol Frequencies
 *
 * Problem Description:
 * You are given a string s consisting of lowercase English letters.
 * Find the length of the longest contiguous substring such that every
 * distinct character appearing in that substring occurs the same number of times.
 *
 * Example 1: s = "aabbcc"   -> 6  (a:2, b:2, c:2)
 * Example 2: s = "aaabbbcc" -> 6  ("aaabbb": a:3, b:3)
 * Example 3: s = "abcde"    -> 5  (each appears once)
 */

using System;
using System.Collections.Generic;
using System.Text;

// ─────────────────────────────────────────────────────────────────────────────
// Core idea (Canonical Difference Hash approach)
// ─────────────────────────────────────────────────────────────────────────────
// A substring s[i..j] is "balanced" when every character that appears in it
// has the same frequency.  We want the longest such substring.
//
// Key insight:
//   Build prefix-frequency arrays: freq[i][c] = how many times character c
//   appears in s[0..i-1].
//
//   For the substring s[i..j] the frequency of character c is:
//       freq[j+1][c] - freq[i][c]
//
//   The substring is balanced iff all non-zero frequencies are equal.
//   Equivalently, if we subtract the minimum non-zero frequency from every
//   non-zero frequency, all results should be 0.
//
//   We encode this "shape" as a canonical string key:
//       For each character c (a-z), store  freq[pos][c] - freq[pos][minChar]
//       where minChar is the character with the smallest non-zero prefix count
//       at position pos (or 0 if c hasn't appeared yet).
//
//   Two positions i and j share the same canonical key  ⟺
//   the substring s[i..j-1] is balanced.
//
//   We store the FIRST time each key is seen in a dictionary.
//   Whenever we see the same key again at position j, the substring
//   s[firstSeen..j-1] is balanced, and its length is j - firstSeen.
//
// Time  Complexity: O(n * 26)  =  O(n)  — one pass, O(26) work per position
// Space Complexity: O(n * 26)  for the dictionary keys (at most n+1 entries)
// ─────────────────────────────────────────────────────────────────────────────

public class Solution
{
    /// <summary>
    /// Returns the length of the longest substring where every distinct
    /// character appears the same number of times.
    /// Time  Complexity: O(n * 26) ≈ O(n)
    /// Space Complexity: O(n * 26) for the hash map
    /// </summary>
    public int LongestEqualFrequencySubarray(string s)
    {
        int n = s.Length;

        // ── Step 1: Build prefix frequency table ──────────────────────────
        // freq[i][c] = number of times character c appears in s[0 .. i-1].
        // We need n+1 rows so that freq[0] is all zeros (empty prefix).
        // This lets us compute any substring frequency as a simple subtraction.
        int[,] freq = new int[n + 1, 26];

        for (int i = 0; i < n; i++)
        {
            // Copy the previous row first (inherit all counts so far)
            for (int c = 0; c < 26; c++)
                freq[i + 1, c] = freq[i, c];

            // Then increment the count for the current character
            freq[i + 1, s[i] - 'a']++;
        }

        // ── Step 2: Prepare the hash map ──────────────────────────────────
        // Maps a canonical key (string) -> the first index at which that key
        // was observed.  We seed it with the empty-prefix key at index 0.
        var firstSeen = new Dictionary<string, int>();

        int answer = 1; // minimum valid answer is 1 (any single character)

        // ── Step 3: Iterate over every prefix position (0 .. n) ──────────
        // For each position pos we compute a canonical key that captures the
        // "relative shape" of the prefix frequencies.
        for (int pos = 0; pos <= n; pos++)
        {
            // ── Step 3a: Find the minimum non-zero prefix frequency ───────
            // We want to normalise so that the smallest active frequency maps
            // to 0.  Characters that have never appeared stay at 0 and are
            // treated as "absent" (they don't affect balance).
            int minFreq = int.MaxValue;
            for (int c = 0; c < 26; c++)
            {
                int f = freq[pos, c];
                if (f > 0 && f < minFreq)
                    minFreq = f;
            }

            // If no character has appeared yet (pos == 0), minFreq stays
            // MaxValue; we'll handle that by treating absent chars as 0.
            if (minFreq == int.MaxValue) minFreq = 0;

            // ── Step 3b: Build the canonical key ─────────────────────────
            // For each of the 26 possible characters we store:
            //   freq[pos][c] - minFreq   if freq[pos][c] > 0
            //   0                        if freq[pos][c] == 0  (absent)
            //
            // Two prefix positions share the same key iff the substring
            // between them is balanced (all active chars have equal counts).
            //
            // We use a StringBuilder for efficiency and separate values with
            // commas so that e.g. "1,0" and "10" don't collide.
            var sb = new StringBuilder(26 * 4);
            for (int c = 0; c < 26; c++)
            {
                int f = freq[pos, c];
                // Normalised difference: 0 if absent, else f - minFreq
                int diff = (f == 0) ? 0 : (f - minFreq);
                sb.Append(diff);
                sb.Append(',');
            }
            string key = sb.ToString();

            // ── Step 3c: Look up / store the key ─────────────────────────
            if (firstSeen.TryGetValue(key, out int firstPos))
            {
                // We have seen this exact canonical shape before at firstPos.
                // The substring s[firstPos .. pos-1] is balanced.
                // Its length is pos - firstPos.
                int length = pos - firstPos;
                if (length > answer)
                    answer = length;
                // Do NOT update firstSeen — we want the earliest occurrence
                // to maximise the length of future matches.
            }
            else
            {
                // First time we see this key; record the position.
                firstSeen[key] = pos;
            }
        }

        return answer;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / verification
// ─────────────────────────────────────────────────────────────────────────────

var sol = new Solution();

// Example 1: "aabbcc"
// Expected: 6  (a:2, b:2, c:2 — entire string)
string s1 = "aabbcc";
int r1 = sol.LongestEqualFrequencySubarray(s1);
Console.WriteLine($"Input: \"{s1}\"");
Console.WriteLine($"Output: {r1}   (Expected: 6)");
Console.WriteLine();

// Example 2: "aaabbbcc"
// Expected: 6  ("aaabbb": a:3, b:3)
string s2 = "aaabbbcc";
int r2 = sol.LongestEqualFrequencySubarray(s2);
Console.WriteLine($"Input: \"{s2}\"");
Console.WriteLine($"Output: {r2}   (Expected: 6)");
Console.WriteLine();

// Example 3: "abcde"
// Expected: 5  (each char appears once — entire string)
string s3 = "abcde";
int r3 = sol.LongestEqualFrequencySubarray(s3);
Console.WriteLine($"Input: \"{s3}\"");
Console.WriteLine($"Output: {r3}   (Expected: 5)");
Console.WriteLine();

// Extra test: "aaabb"
// "aabb" (indices 1-4): a:2, b:2  -> length 4
// "ab"   (indices 2-3 or 3-4): length 2
// Expected: 4
string s4 = "aaabb";
int r4 = sol.LongestEqualFrequencySubarray(s4);
Console.WriteLine($"Input: \"{s4}\"");
Console.WriteLine($"Output: {r4}   (Expected: 4)");
Console.WriteLine();

// Extra test: single character "a"
// Expected: 1
string s5 = "a";
int r5 = sol.LongestEqualFrequencySubarray(s5);
Console.WriteLine($"Input: \"{s5}\"");
Console.WriteLine($"Output: {r5}   (Expected: 1)");
Console.WriteLine();

// Extra test: "aabbccdd"
// Entire string: a:2,b:2,c:2,d:2 -> length 8
string s6 = "aabbccdd";
int r6 = sol.LongestEqualFrequencySubarray(s6);
Console.WriteLine($"Input: \"{s6}\"");
Console.WriteLine($"Output: {r6}   (Expected: 8)");
Console.WriteLine();

// Extra test: "aaabbc"
// "aaabb" -> a:3,b:2 invalid
// "aabb"  -> a:2,b:2 valid, length 4
// "abbc"  -> a:1,b:2,c:1 invalid
// "abc"   -> each 1, length 3
// Expected: 4
string s7 = "aaabbc";
int r7 = sol.LongestEqualFrequencySubarray(s7);
Console.WriteLine($"Input: \"{s7}\"");
Console.WriteLine($"Output: {r7}   (Expected: 4)");