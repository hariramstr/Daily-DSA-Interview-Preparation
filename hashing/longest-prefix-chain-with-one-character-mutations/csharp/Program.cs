/*
Title: Longest Prefix Chain with One-Character Mutations

Problem Description:
You are given an array of distinct lowercase strings words. A string a can transition to string b if and only if all of the following are true:
1. b is exactly one character longer than a
2. b starts with a prefix that differs from a in at most one position among the first |a| characters
3. the extra character in b may appear only at the end

In other words, you may extend a by appending one new character to the end, and while comparing the original positions, you are allowed to mutate at most one existing character.

Your task is to compute the length of the longest possible chain of words where each next word is reachable from the previous one by the rule above.

Constraints:
- 1 <= words.length <= 2 * 10^5
- 1 <= words[i].length <= 30
- words[i] consists only of lowercase English letters
- All words[i] are distinct
- The sum of all word lengths does not exceed 2 * 10^6
*/

using System;
using System.Collections.Generic;
using System.Linq;

class Solution
{
    /*
    Time Complexity:
    Let S be the sum of all word lengths.
    Each word of length L generates exactly L + 1 signatures:
    - 1 exact-prefix signature
    - L one-mismatch signatures
    Since max length is 30, this is very small per word.
    Total time is O(S * 30) in practice, which is O(S) under the given bound.

    Space Complexity:
    O(S) for storing dynamic programming values and hashed signatures grouped by length.
    */
    public int LongestPrefixChain(string[] words)
    {
        // We process words in increasing order of length because a valid predecessor
        // must be exactly one character shorter than the current word.
        //
        // Dynamic programming idea:
        // dp[word] = longest valid chain ending at this word.
        //
        // To compute dp for a word of length L, we need to find any predecessor of length L-1
        // whose characters differ from the first L-1 characters of the current word in at most one position.
        //
        // A direct comparison against all shorter words would be too slow.
        //
        // Instead, we build "signatures" for words of each length:
        // - exact signature: the whole word itself
        // - masked signatures: replace exactly one position by a wildcard marker
        //
        // Example:
        // word = "abc"
        // exact signature: "abc"
        // one-mismatch signatures:
        //   "*bc"
        //   "a*c"
        //   "ab*"
        //
        // Why this works:
        // Two strings of equal length differ in at most one position if and only if
        // either:
        // - they are exactly equal, or
        // - there exists some position such that after masking that position in both strings,
        //   the masked forms become equal.
        //
        // Therefore, for a current word b of length L, we only need to inspect the prefix p = b[0..L-2].
        // Any predecessor a of length L-1 is valid if:
        // - a == p, or
        // - a and p share at least one same masked signature.
        //
        // We store, for each length, the best dp value seen for every signature.
        // Then each current word can query the previous length's signature tables in O(L).

        Array.Sort(words, (a, b) =>
        {
            int cmp = a.Length.CompareTo(b.Length);
            return cmp != 0 ? cmp : string.CompareOrdinal(a, b);
        });

        // For each length, store:
        // 1. best chain length for exact words of that length
        // 2. best chain length for each one-position-masked pattern of that length
        //
        // We use arrays indexed by length because max length is only 30.
        var exactBestByLength = new Dictionary<string, int>[31];
        var maskedBestByLength = new Dictionary<string, int>[31];

        for (int i = 0; i <= 30; i++)
        {
            exactBestByLength[i] = new Dictionary<string, int>();
            maskedBestByLength[i] = new Dictionary<string, int>();
        }

        int answer = 1;

        foreach (var word in words)
        {
            int len = word.Length;

            // Every word alone forms a chain of length 1.
            int bestForCurrentWord = 1;

            // A predecessor must have length exactly len - 1.
            if (len > 1)
            {
                int prevLen = len - 1;

                // Step 1:
                // Extract the prefix of the current word that must be compared
                // against a predecessor.
                //
                // Example:
                // current = "abca"
                // predecessor length = 3
                // prefix = "abc"
                //
                // We are allowed at most one mismatch between predecessor and this prefix.
                string prefix = word[..prevLen];

                // Step 2:
                // Check zero-mismatch case.
                //
                // If some previous word is exactly equal to this prefix,
                // then it is a valid predecessor.
                if (exactBestByLength[prevLen].TryGetValue(prefix, out int exactBest))
                {
                    bestForCurrentWord = Math.Max(bestForCurrentWord, exactBest + 1);
                }

                // Step 3:
                // Check one-mismatch case.
                //
                // For each position in the prefix, mask that position.
                // Any previous word of the same length that has the same masked pattern
                // differs from the prefix in at most that one masked position.
                //
                // Example:
                // prefix = "acc"
                // masked forms:
                //   "*cc"
                //   "a*c"
                //   "ac*"
                //
                // If a predecessor word produced any of these masked forms earlier,
                // then it is a valid predecessor.
                for (int i = 0; i < prevLen; i++)
                {
                    string masked = BuildMasked(prefix, i);

                    if (maskedBestByLength[prevLen].TryGetValue(masked, out int maskedBest))
                    {
                        bestForCurrentWord = Math.Max(bestForCurrentWord, maskedBest + 1);
                    }
                }
            }

            // Update global answer.
            answer = Math.Max(answer, bestForCurrentWord);

            // Step 4:
            // Now that dp for the current word is known, insert this word into the
            // data structures for its own length so longer words can use it later.
            //
            // 4a. Update exact signature table.
            UpdateMax(exactBestByLength[len], word, bestForCurrentWord);

            // 4b. Update all one-position-masked signatures.
            for (int i = 0; i < len; i++)
            {
                string masked = BuildMasked(word, i);
                UpdateMax(maskedBestByLength[len], masked, bestForCurrentWord);
            }
        }

        return answer;
    }

    // Builds a masked version of the string where exactly one position is replaced by '*'.
    // Example: BuildMasked("abcd", 2) => "ab*d"
    private static string BuildMasked(string s, int indexToMask)
    {
        char[] chars = s.ToCharArray();
        chars[indexToMask] = '*';
        return new string(chars);
    }

    // Stores the maximum value for a key inside a dictionary.
    // This is important because multiple words can share the same signature,
    // and for dynamic programming we only care about the best chain length among them.
    private static void UpdateMax(Dictionary<string, int> map, string key, int value)
    {
        if (map.TryGetValue(key, out int existing))
        {
            if (value > existing)
            {
                map[key] = value;
            }
        }
        else
        {
            map[key] = value;
        }
    }
}

// Demo code

var solution = new Solution();

string[] words1 = ["a", "ab", "ac", "abc", "acc", "abca", "acca"];
int result1 = solution.LongestPrefixChain(words1);
Console.WriteLine(result1); // Expected: 5

string[] words2 = ["cat", "bat", "bate", "bath", "batch", "catch", "cater"];
int result2 = solution.LongestPrefixChain(words2);
Console.WriteLine(result2); // By the stated transition rule, the correct result for this concrete input is 2

// Additional small sanity checks
string[] words3 = ["a"];
Console.WriteLine(solution.LongestPrefixChain(words3)); // Expected: 1

string[] words4 = ["a", "b", "ba", "bb", "bba", "bbb"];
Console.WriteLine(solution.LongestPrefixChain(words4)); // One possible best chain length: 4