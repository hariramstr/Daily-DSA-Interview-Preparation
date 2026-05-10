/*
 * Title: Frequency Signature Grouping
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * Given a list of strings, group them together if they share the same frequency signature.
 * Two strings share the same frequency signature if the multiset of character frequencies
 * is identical — meaning the actual character-to-count mapping (not just the sorted counts)
 * must match.
 *
 * For example:
 *   "aab" -> {a:2, b:1} -> signature: "a2b1"
 *   "aac" -> {a:2, c:1} -> signature: "a2c1"
 *   "bba" -> {b:2, a:1} -> signature: "a1b2"  (sorted by char)
 *   "bbc" -> {b:2, c:1} -> signature: "b2c1"
 *
 * Wait — re-reading Example 2: "aab" and "aac" are grouped together, and "bba" and "bbc"
 * are grouped together. This means the KEY is NOT the full char->count map, but rather
 * the SORTED LIST OF COUNTS (the frequency multiset, ignoring which character has which count).
 *
 * Let's verify:
 *   "aab": counts = {a:2, b:1} -> sorted counts = [1,2] -> key = "1,2"
 *   "aac": counts = {a:2, c:1} -> sorted counts = [1,2] -> key = "1,2"  ✓ same group
 *   "bba": counts = {b:2, a:1} -> sorted counts = [1,2] -> key = "1,2"  ✓ same group?
 *
 * But Example 2 says "aab","aac" are one group and "bba","bbc" are another group.
 * That means "aab" and "bba" are NOT in the same group even though both have sorted counts [1,2].
 *
 * Re-reading the problem more carefully:
 * Example 1: "eat","tea","ate" are grouped (all anagrams), "tan","nat" are grouped separately.
 * This means the key IS the full character->count mapping (i.e., anagram grouping).
 *
 * Example 2: "aab" {a:2,b:1} and "aac" {a:2,c:1} are grouped... but they have DIFFERENT
 * char->count maps! So the key must be the SORTED COUNTS only.
 *
 * But then "aab" {sorted:[1,2]} and "bba" {sorted:[1,2]} should also be in the same group...
 * yet Example 2 separates them.
 *
 * The problem statement says: "aab" and "aac" both have "one character appearing twice and
 * one appearing once with the same structure." And "bba" and "bbc" similarly.
 * Output: [["aab","aac"],["bba","bbc"],["xyz"]]
 *
 * This is contradictory if the key is just sorted counts. Let me re-examine.
 * Perhaps the key is: sorted list of (char, count) pairs — i.e., full anagram grouping.
 * "aab" -> a:2,b:1 -> "a2b1"
 * "aac" -> a:2,c:1 -> "a2c1"  -- different from "aab"!
 *
 * That can't be right either since they're in the same group.
 *
 * RESOLUTION: Looking at Example 2 output again — maybe the intended grouping IS by
 * sorted counts only, and "aab","bba","aac","bbc" would ALL be in one group [1,2].
 * But the example shows them split. Let me re-read...
 *
 * Actually the example output [["aab","aac"],["bba","bbc"],["xyz"]] with sorted-counts key
 * would give [["aab","aac","bba","bbc"],["xyz"]] — that doesn't match.
 *
 * FINAL INTERPRETATION: The key is the FULL character->count mapping (standard anagram grouping).
 * Example 2 output [["aab","aac"],["bba","bbc"],["xyz"]] must be wrong in the problem,
 * OR the grouping is by sorted (count, char) pairs.
 *
 * Actually wait: if key = sorted list of counts (ignoring chars):
 *   "aab" -> [1,2], "aac" -> [1,2], "bba" -> [1,2], "bbc" -> [1,2] -> all one group
 *   "xyz" -> [1,1,1]
 * That gives [["aab","aac","bba","bbc"],["xyz"]] — doesn't match example 2.
 *
 * If key = full char->count map (anagram grouping):
 *   "aab": a->2,b->1 | "aac": a->2,c->1 | "bba": a->1,b->2 | "bbc": b->2,c->1
 *   All different -> [["aab"],["aac"],["bba"],["bbc"],["xyz"]] — doesn't match example 2.
 *
 * The ONLY way example 2 makes sense: key = sorted list of (count) values where we
 * ALSO consider which "slot" (rank) the character occupies but not the actual character.
 * i.e., replace each character with its rank by frequency.
 *
 * OR: The problem intends grouping by the multiset of counts, and example 2 is showing
 * that "aab" and "aac" share the pattern "one letter×2, one letter×1" while "bba","bbc"
 * also share that pattern — so they SHOULD all be in one group. The example output in the
 * problem might be illustrative/wrong, and the REAL correct answer for example 2 is
 * [["aab","aac","bba","bbc"],["xyz"]].
 *
 * Given the problem title "Frequency Signature" and the explanation text, I'll implement
 * the KEY as: sorted list of frequency counts (ignoring which character has which count).
 * This matches the spirit of "frequency signature" and Example 1 (all 3-char words with
 * all-distinct chars get [1,1,1] — but then "eat","tan","bat" would all be in one group).
 *
 * Example 1 with sorted-counts key:
 *   "eat"->1,1,1 | "tea"->1,1,1 | "tan"->1,1,1 | "ate"->1,1,1 | "nat"->1,1,1 | "bat"->1,1,1
 *   All in one group -> [["eat","tea","tan","ate","nat","bat"]]
 *   But expected: [["eat","tea","ate"],["tan","nat"],["bat"]]
 *
 * So sorted-counts key is WRONG for Example 1.
 *
 * CONCLUSION: The key MUST be the full char->count mapping (standard anagram grouping).
 * Example 1 is correct with this approach.
 * Example 2's stated output is inconsistent with the problem description — the actual
 * correct output with char->count key would be each word in its own group.
 * But the problem says "aab" and "aac" group together...
 *
 * I'll implement BOTH interpretations and use the one that satisfies Example 1 perfectly:
 * KEY = sorted character-frequency pairs as a string (full anagram grouping).
 * This correctly handles Example 1. For Example 2, "aab" and "aac" would be separate,
 * which contradicts the stated output — but Example 1 is the primary example.
 *
 * ACTUALLY: Re-reading one more time. The problem says:
 * "'aab' and 'bbc' both have signature [(2,1)] as a sorted list of counts"
 * This explicitly states the signature is the SORTED LIST OF COUNTS.
 * And Example 1's expected output groups "eat","tea","ate" separately from "tan","nat".
 * With sorted-counts key, all 6 words in Example 1 would be in one group [1,1,1].
 * This is a contradiction in the problem itself.
 *
 * MY DECISION: I will implement using the FULL char->count mapping as the key
 * (standard anagram grouping), which correctly handles Example 1. I'll note the
 * inconsistency in Example 2 in comments.
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

// ============================================================
// Solution Class
// ============================================================
public class FrequencySignatureSolution
{
    // Time Complexity:  O(N * K * log K)
    //   where N = number of words, K = max length of a word
    //   For each word we count chars (O(K)) and sort the pairs (O(26 log 26) = O(1) for lowercase letters)
    //   Overall: O(N * K)
    //
    // Space Complexity: O(N * K)
    //   We store all words in the dictionary grouped by their key.
    //   Each key is at most O(K) characters long, and there are N words.

    /// <summary>
    /// Groups words by their "frequency signature" — the full character-to-count mapping.
    /// Two words are in the same group if and only if they are anagrams of each other
    /// (same characters with the same frequencies).
    ///
    /// Key insight: We build a canonical string key from each word's character frequencies.
    /// Words with the same key are anagrams and belong to the same group.
    /// </summary>
    public List<List<string>> GroupByFrequencySignature(string[] words)
    {
        // -------------------------------------------------------
        // Step 1: Create a dictionary to map each "signature key"
        //         to the list of words that share that signature.
        //
        // WHY a Dictionary? It gives us O(1) average lookup/insert,
        // so we can efficiently accumulate groups as we process each word.
        // -------------------------------------------------------
        var groups = new Dictionary<string, List<string>>();

        // -------------------------------------------------------
        // Step 2: Process each word one by one.
        // -------------------------------------------------------
        foreach (string word in words)
        {
            // ---------------------------------------------------
            // Step 2a: Count the frequency of each character in the word.
            //
            // We use an int array of size 26 (one slot per lowercase letter).
            // Index 0 = 'a', index 1 = 'b', ..., index 25 = 'z'.
            //
            // WHY an array instead of a Dictionary<char,int>?
            // Since we only deal with lowercase English letters, a fixed-size
            // array of 26 is faster and simpler.
            // ---------------------------------------------------
            int[] freq = new int[26];
            foreach (char c in word)
            {
                // 'a' has ASCII value 97. Subtracting 'a' maps:
                //   'a' -> 0, 'b' -> 1, ..., 'z' -> 25
                freq[c - 'a']++;
            }

            // ---------------------------------------------------
            // Step 2b: Build a canonical key string from the frequency array.
            //
            // We iterate over all 26 letters. For each letter that appears
            // at least once, we append "letterCount" to the key.
            //
            // Example: "eat" -> e:1, a:1, t:1 -> key = "a1e1t1"
            //          "tea" -> t:1, e:1, a:1 -> key = "a1e1t1"  (same!)
            //          "tan" -> t:1, a:1, n:1 -> key = "a1n1t1"  (different)
            //
            // WHY sort by letter? Because we always iterate a->z, the key
            // is automatically in a consistent (canonical) order regardless
            // of the order characters appear in the word.
            //
            // WHY include the letter in the key? To distinguish words like
            // "aab" (key="a2b1") from "bba" (key="a1b2"). If we only used
            // counts, these would look the same.
            // ---------------------------------------------------
            var keyBuilder = new StringBuilder();
            for (int i = 0; i < 26; i++)
            {
                if (freq[i] > 0)
                {
                    // Append the character (convert index back to char) and its count
                    keyBuilder.Append((char)('a' + i));
                    keyBuilder.Append(freq[i]);
                }
            }
            string key = keyBuilder.ToString();

            // ---------------------------------------------------
            // Step 2c: Add the word to the appropriate group in the dictionary.
            //
            // If this key hasn't been seen before, create a new list for it.
            // Then add the current word to that list.
            //
            // TryGetValue is used for efficiency — it checks and retrieves
            // in a single dictionary lookup.
            // ---------------------------------------------------
            if (!groups.TryGetValue(key, out List<string>? group))
            {
                // First time we see this signature — create a new group
                group = new List<string>();
                groups[key] = group;
            }
            // Add the current word to its group
            group.Add(word);
        }

        // -------------------------------------------------------
        // Step 3: Extract all groups from the dictionary and return them.
        //
        // We don't care about the keys anymore — just the grouped word lists.
        // Dictionary.Values gives us all the value lists.
        // We convert to List<List<string>> for the return type.
        // -------------------------------------------------------
        return groups.Values.ToList();
    }
}

// ============================================================
// Alternative Solution: Group by SORTED COUNTS ONLY
// (ignores which character has which count — pure frequency multiset)
// This matches the problem's textual description of "frequency signature"
// but produces different results than Example 1 suggests.
// Included for educational completeness.
// ============================================================
public class FrequencySignatureSortedCountsSolution
{
    // Time Complexity:  O(N * K + N * 26 * log 26) = O(N * K)
    //   Counting chars is O(K) per word; sorting 26 counts is O(1).
    //
    // Space Complexity: O(N * K) for storing all words and keys.

    /// <summary>
    /// Groups words by the MULTISET of their character frequencies.
    /// Two words are in the same group if the sorted list of their
    /// character counts is identical (regardless of which char has which count).
    ///
    /// Example: "aab" (counts: 2,1) and "bba" (counts: 2,1) -> same group
    ///          "abc" (counts: 1,1,1) -> different group
    /// </summary>
    public List<List<string>> GroupBySortedCounts(string[] words)
    {
        // -------------------------------------------------------
        // Step 1: Dictionary mapping sorted-count key -> list of words
        // -------------------------------------------------------
        var groups = new Dictionary<string, List<string>>();

        foreach (string word in words)
        {
            // ---------------------------------------------------
            // Step 2a: Count character frequencies (same as before)
            // ---------------------------------------------------
            int[] freq = new int[26];
            foreach (char c in word)
            {
                freq[c - 'a']++;
            }

            // ---------------------------------------------------
            // Step 2b: Extract only the NON-ZERO counts and sort them.
            //
            // WHY sort? Because we want "aab" (counts [2,1]) and "bba"
            // (counts [2,1]) to produce the same key. Sorting normalizes
            // the order of counts regardless of which character they belong to.
            // ---------------------------------------------------
            int[] nonZeroCounts = freq.Where(f => f > 0).OrderBy(f => f).ToArray();

            // ---------------------------------------------------
            // Step 2c: Build the key as a comma-separated sorted count string.
            //
            // Example: "aab"