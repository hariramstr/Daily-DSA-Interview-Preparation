/*
 * ============================================================
 * Title: Anagram Group Reunion
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given a list of n strings called "words" and a separate
 * list of q query strings called "queries". For each query string,
 * find all strings in "words" that are anagrams of the query string
 * and return them in the order they appear in "words".
 *
 * TWIST: After answering each query, the matched words are REMOVED
 * from "words" permanently, so they cannot be matched again by
 * future queries.
 *
 * Return a list of lists where the i-th list contains all words
 * from "words" (at the time of the i-th query, after previous
 * removals) that are anagrams of queries[i], in their original
 * relative order.
 *
 * Constraints:
 *   1 <= n <= 10^4
 *   1 <= q <= 10^3
 *   1 <= words[i].length, queries[i].length <= 20
 *   All strings consist of lowercase English letters only.
 *
 * Example 1:
 *   Input:  words = ["eat","tea","tan","ate","nat","bat"]
 *           queries = ["ate","tan"]
 *   Output: [["eat","tea","ate"],["tan","nat"]]
 *
 * Example 2:
 *   Input:  words = ["abc","bca","cab","xyz","zyx"]
 *           queries = ["abc","xyz","abc"]
 *   Output: [["abc","bca","cab"],["xyz","zyx"],[]]
 * ============================================================
 */

using System;
using System.Collections.Generic;
using System.Linq;

// ─────────────────────────────────────────────────────────────
// Solution class — contains the core algorithm
// ─────────────────────────────────────────────────────────────
class Solution
{
    // ──────────────────────────────────────────────────────────
    // Method: FindAnagramGroups
    //
    // Time Complexity:
    //   Let n = number of words, q = number of queries, L = max word length.
    //   - Building the initial dictionary: O(n * L log L)  [sort each word]
    //   - For each query (q queries), we scan remaining words: O(n * L log L)
    //     in the worst case per query → O(q * n * L log L) overall.
    //   With n=10^4, q=10^3, L=20 this is comfortably within limits.
    //
    // Space Complexity:
    //   O(n * L) for storing the sorted-key representations of all words,
    //   plus O(n) for the "active" index set.
    // ──────────────────────────────────────────────────────────
    public List<List<string>> FindAnagramGroups(string[] words, string[] queries)
    {
        // ── STEP 1: Pre-compute a "canonical key" for every word ──────────
        // Two strings are anagrams if and only if they produce the same
        // string when their characters are sorted alphabetically.
        // Example: "eat" → "aet", "tea" → "aet", "ate" → "aet"  (all equal)
        //
        // We store these keys in a parallel array so we can look them up
        // in O(1) later without re-sorting on every query.
        string[] wordKeys = new string[words.Length];
        for (int i = 0; i < words.Length; i++)
        {
            // Sort the characters of words[i] and join them back into a string.
            // This is the canonical "anagram fingerprint" for that word.
            char[] chars = words[i].ToCharArray();
            Array.Sort(chars);
            wordKeys[i] = new string(chars);
        }

        // ── STEP 2: Maintain a set of "active" word indices ───────────────
        // Instead of physically removing elements from an array (which is
        // expensive — O(n) per removal), we keep a HashSet of indices that
        // are still available.  Removing a word is then O(1).
        //
        // We initialise it with every index from 0 to n-1.
        HashSet<int> activeIndices = new HashSet<int>(Enumerable.Range(0, words.Length));

        // ── STEP 3: Prepare the result container ─────────────────────────
        // One inner list per query; we'll fill them in order.
        List<List<string>> results = new List<List<string>>();

        // ── STEP 4: Process each query in order ───────────────────────────
        foreach (string query in queries)
        {
            // 4a. Compute the canonical key for this query string.
            //     We need to compare it against the pre-computed word keys.
            char[] qChars = query.ToCharArray();
            Array.Sort(qChars);
            string queryKey = new string(qChars);

            // 4b. Collect all active words whose key matches the query key.
            //     We iterate over active indices in ascending order so that
            //     the results preserve the original relative order of words.
            //
            //     Why ascending order?  HashSet does not guarantee order, so
            //     we sort the active indices before iterating.
            List<string> matched = new List<string>();
            List<int> toRemove = new List<int>(); // indices to deactivate

            // Sort active indices to maintain original word order in output.
            // (Sorting a set of up to n=10^4 integers is very fast.)
            List<int> sortedActive = new List<int>(activeIndices);
            sortedActive.Sort(); // ascending → preserves original word order

            foreach (int idx in sortedActive)
            {
                // 4c. Compare the pre-computed key of this word to the query key.
                if (wordKeys[idx] == queryKey)
                {
                    // This word is an anagram of the query — record it.
                    matched.Add(words[idx]);

                    // Mark this index for removal so future queries cannot
                    // see this word again.
                    toRemove.Add(idx);
                }
            }

            // 4d. Remove matched indices from the active set.
            //     We do this AFTER the loop to avoid modifying the collection
            //     while iterating over it.
            foreach (int idx in toRemove)
            {
                activeIndices.Remove(idx);
            }

            // 4e. Add the matched words for this query to the final result.
            //     If no anagrams were found, matched is an empty list — that
            //     is the correct answer (see Example 2, third query).
            results.Add(matched);
        }

        // ── STEP 5: Return the completed result list ──────────────────────
        return results;
    }
}

// ─────────────────────────────────────────────────────────────
// Demo / driver code (top-level statements)
// ─────────────────────────────────────────────────────────────

// Helper: pretty-print a List<List<string>>
static void PrintResult(List<List<string>> result)
{
    Console.Write("[");
    for (int i = 0; i < result.Count; i++)
    {
        Console.Write("[");
        Console.Write(string.Join(", ", result[i].Select(w => $"\"{w}\"")));
        Console.Write("]");
        if (i < result.Count - 1) Console.Write(", ");
    }
    Console.WriteLine("]");
}

Solution sol = new Solution();

// ── Example 1 ────────────────────────────────────────────────
// words   = ["eat","tea","tan","ate","nat","bat"]
// queries = ["ate","tan"]
// Expected output: [["eat","tea","ate"],["tan","nat"]]
Console.WriteLine("=== Example 1 ===");
string[] words1   = { "eat", "tea", "tan", "ate", "nat", "bat" };
string[] queries1 = { "ate", "tan" };
List<List<string>> result1 = sol.FindAnagramGroups(words1, queries1);
Console.Write("Output:   ");
PrintResult(result1);
Console.WriteLine("Expected: [[\"eat\", \"tea\", \"ate\"], [\"tan\", \"nat\"]]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────
// words   = ["abc","bca","cab","xyz","zyx"]
// queries = ["abc","xyz","abc"]
// Expected output: [["abc","bca","cab"],["xyz","zyx"],[]]
Console.WriteLine("=== Example 2 ===");
string[] words2   = { "abc", "bca", "cab", "xyz", "zyx" };
string[] queries2 = { "abc", "xyz", "abc" };
List<List<string>> result2 = sol.FindAnagramGroups(words2, queries2);
Console.Write("Output:   ");
PrintResult(result2);
Console.WriteLine("Expected: [[\"abc\", \"bca\", \"cab\"], [\"xyz\", \"zyx\"], []]");
Console.WriteLine();

// ── Extra edge-case: single word, single query, no match ─────
Console.WriteLine("=== Edge Case: no match ===");
string[] words3   = { "hello" };
string[] queries3 = { "world" };
List<List<string>> result3 = sol.FindAnagramGroups(words3, queries3);
Console.Write("Output:   ");
PrintResult(result3);
Console.WriteLine("Expected: [[]]");
Console.WriteLine();

// ── Extra edge-case: same query repeated, words exhausted ────
Console.WriteLine("=== Edge Case: repeated query exhausts words ===");
string[] words4   = { "ab", "ba" };
string[] queries4 = { "ab", "ab" };
List<List<string>> result4 = sol.FindAnagramGroups(words4, queries4);
Console.Write("Output:   ");
PrintResult(result4);
Console.WriteLine("Expected: [[\"ab\", \"ba\"], []]");