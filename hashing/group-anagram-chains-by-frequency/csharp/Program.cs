/*
 * Title: Group Anagram Chains by Frequency
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given a list of words. Two words are considered "anagram siblings" if one
 * can be rearranged to form the other (i.e., they contain the same characters with
 * the same frequencies). Your task is to group all words into anagram families and
 * then return the groups sorted by their size in descending order. If two groups
 * have the same size, sort them lexicographically by their smallest word in ascending order.
 *
 * Additionally, for each group, return the words sorted in lexicographic order.
 *
 * Constraints:
 * - 1 <= words.length <= 10^4
 * - 1 <= words[i].length <= 20
 * - All characters in words[i] are lowercase English letters.
 * - Words in the input may contain duplicates; duplicate words belong to the same group.
 *
 * Example 1:
 * Input:  ["eat", "tea", "tan", "ate", "nat", "bat"]
 * Output: [["ate","eat","tea"], ["nat","tan"], ["bat"]]
 *
 * Example 2:
 * Input:  ["abc", "bca", "xyz", "zyx", "cab", "yzx"]
 * Output: [["abc","bca","cab"], ["xyz","yzx","zyx"]]
 */

using System;
using System.Collections.Generic;
using System.Linq;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the core algorithm
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    /// <summary>
    /// Groups the input words into anagram families, then sorts the groups by
    /// descending size (ties broken by the lexicographically smallest word in
    /// each group, ascending). Words within each group are sorted lexicographically.
    ///
    /// Time Complexity : O(N * K * log K)
    ///   where N = number of words, K = maximum word length.
    ///   Sorting each word's characters costs O(K log K); we do this for every word.
    ///
    /// Space Complexity: O(N * K)
    ///   We store every word (and its sorted key) in the dictionary.
    /// </summary>
    public List<List<string>> GroupAnagrams(string[] words)
    {
        // ── Step 1: Create a dictionary that maps a "canonical key" to the list
        //            of words that share that key.
        //
        //   WHY a dictionary?
        //   A dictionary gives O(1) average-time lookup and insertion, which lets
        //   us group all N words in a single pass.
        //
        //   WHAT is the canonical key?
        //   Two words are anagrams if and only if sorting their characters produces
        //   the same string.  For example:
        //       "eat"  → sort → "aet"
        //       "tea"  → sort → "aet"   ← same key  ✓
        //       "tan"  → sort → "ant"   ← different key
        //   So we use the sorted character string as the key.
        var anagramMap = new Dictionary<string, List<string>>();

        // ── Step 2: Iterate over every word in the input array.
        foreach (string word in words)
        {
            // ── Step 2a: Compute the canonical key for this word.
            //
            //   ToCharArray()  – converts the string to a mutable char array so
            //                    we can sort it without modifying the original string.
            //   Array.Sort()   – sorts the characters in-place (ascending by default).
            //   new string()   – reassembles the sorted chars back into a string key.
            char[] chars = word.ToCharArray();
            Array.Sort(chars);
            string key = new string(chars);

            // ── Step 2b: If this key has never been seen before, create a new
            //            empty list for it in the dictionary.
            //
            //   ContainsKey check + Add is equivalent to the more concise
            //   TryGetValue pattern, but written explicitly here for clarity.
            if (!anagramMap.ContainsKey(key))
            {
                anagramMap[key] = new List<string>();
            }

            // ── Step 2c: Append the original (unsorted) word to its group.
            anagramMap[key].Add(word);
        }

        // ── Step 3: Sort the words inside each group lexicographically.
        //
        //   WHY sort inside each group?
        //   The problem requires words within a group to appear in lexicographic order.
        //   We do this before the outer sort so that each group's "smallest word"
        //   (used as a tiebreaker in Step 4) is simply group[0] after sorting.
        foreach (var group in anagramMap.Values)
        {
            group.Sort(StringComparer.Ordinal); // Ordinal = standard lexicographic order
        }

        // ── Step 4: Collect all groups and sort them by the required criteria.
        //
        //   Primary sort   : group size, DESCENDING  (larger groups come first)
        //   Secondary sort : smallest word in the group (group[0] after Step 3),
        //                    ASCENDING  (lexicographically earlier group comes first)
        //
        //   LINQ OrderByDescending / ThenBy gives us a clean, readable expression.
        List<List<string>> result = anagramMap.Values
            .OrderByDescending(group => group.Count)          // primary: size desc
            .ThenBy(group => group[0], StringComparer.Ordinal) // secondary: min word asc
            .ToList();

        // ── Step 5: Return the fully sorted list of groups.
        return result;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / driver code (top-level statements — no explicit Main needed in .NET 6+)
// ─────────────────────────────────────────────────────────────────────────────

// Helper: pretty-print a list of groups
static void PrintResult(List<List<string>> groups)
{
    Console.Write("[");
    for (int i = 0; i < groups.Count; i++)
    {
        Console.Write("[");
        Console.Write(string.Join(", ", groups[i].Select(w => $"\"{w}\"")));
        Console.Write("]");
        if (i < groups.Count - 1) Console.Write(", ");
    }
    Console.WriteLine("]");
}

var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Expected: [["ate","eat","tea"], ["nat","tan"], ["bat"]]
//
// Trace:
//   "eat" → key "aet"  → { "aet": ["eat"] }
//   "tea" → key "aet"  → { "aet": ["eat","tea"] }
//   "tan" → key "ant"  → { "aet": [...], "ant": ["tan"] }
//   "ate" → key "aet"  → { "aet": ["eat","tea","ate"], ... }
//   "nat" → key "ant"  → { ..., "ant": ["tan","nat"] }
//   "bat" → key "abt"  → { ..., "abt": ["bat"] }
//
//   After inner sort:
//     "aet" → ["ate","eat","tea"]   (size 3, min="ate")
//     "ant" → ["nat","tan"]         (size 2, min="nat")
//     "abt" → ["bat"]               (size 1, min="bat")
//
//   Outer sort (desc size, then min word asc):
//     size 3 first → ["ate","eat","tea"]
//     size 2 next  → ["nat","tan"]
//     size 1 last  → ["bat"]
//   ✓ Matches expected output.

Console.WriteLine("Example 1:");
Console.Write("Input:  [\"eat\", \"tea\", \"tan\", \"ate\", \"nat\", \"bat\"]\n");
string[] words1 = { "eat", "tea", "tan", "ate", "nat", "bat" };
var result1 = solution.GroupAnagrams(words1);
Console.Write("Output: ");
PrintResult(result1);
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// Expected: [["abc","bca","cab"], ["xyz","yzx","zyx"]]
//
// Trace:
//   "abc" → key "abc" → { "abc": ["abc"] }
//   "bca" → key "abc" → { "abc": ["abc","bca"] }
//   "xyz" → key "xyz" → { "abc": [...], "xyz": ["xyz"] }
//   "zyx" → key "xyz" → { ..., "xyz": ["xyz","zyx"] }
//   "cab" → key "abc" → { "abc": ["abc","bca","cab"], ... }
//   "yzx" → key "xyz" → { ..., "xyz": ["xyz","zyx","yzx"] }
//
//   After inner sort:
//     "abc" → ["abc","bca","cab"]   (size 3, min="abc")
//     "xyz" → ["xyz","yzx","zyx"]   (size 3, min="xyz")
//
//   Outer sort (desc size, then min word asc):
//     Both size 3 → tiebreak by min word: "abc" < "xyz"
//     → ["abc","bca","cab"] first, ["xyz","yzx","zyx"] second
//   ✓ Matches expected output.

Console.WriteLine("Example 2:");
Console.Write("Input:  [\"abc\", \"bca\", \"xyz\", \"zyx\", \"cab\", \"yzx\"]\n");
string[] words2 = { "abc", "bca", "xyz", "zyx", "cab", "yzx" };
var result2 = solution.GroupAnagrams(words2);
Console.Write("Output: ");
PrintResult(result2);
Console.WriteLine();

// ── Example 3: duplicates ─────────────────────────────────────────────────────
// Duplicates must land in the same group.
Console.WriteLine("Example 3 (duplicates):");
Console.Write("Input:  [\"ab\", \"ba\", \"ab\"]\n");
string[] words3 = { "ab", "ba", "ab" };
var result3 = solution.GroupAnagrams(words3);
Console.Write("Output: ");
PrintResult(result3);
// Expected: [["ab","ab","ba"]]  — all three in one group, sorted lexicographically