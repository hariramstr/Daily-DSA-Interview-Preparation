/*
Title: Minimum Fatigue to Decode a Corrupted Beacon Stream

Problem Description:
A satellite receives a long beacon stream represented by a string s of length n,
where each character is a lowercase English letter. Due to interference, the stream
may contain corruption. You are also given a dictionary of valid beacon codes, where
each code is a lowercase string with an associated non-negative fatigue cost.

You may decode the stream by partitioning s into one or more contiguous pieces.
For each piece, you must choose exactly one dictionary code of the same length and pay
a cost equal to:

    fatigue(code) + mismatch_count(piece, code)

where mismatch_count is the number of positions at which the two strings differ.

Your goal is to decode the entire stream with minimum total fatigue.

Return the minimum possible total fatigue, or -1 if the stream cannot be fully partitioned
into lengths that exist in the dictionary.

Constraints:
- 1 <= n <= 5000
- 1 <= dictionary.length <= 2000
- 1 <= code.length <= 50
- Sum of all dictionary code lengths <= 50000
- 0 <= fatigue(code) <= 10^6
- s and every code consist only of lowercase English letters
*/

using System;
using System.Collections.Generic;

public class Solution
{
    // We store each dictionary entry as a pair:
    // - Word: the beacon code string
    // - Fatigue: the fixed fatigue cost for choosing that code
    public record CodeEntry(string Word, int Fatigue);

    /*
    Time Complexity:
    Let:
    - n = length of s
    - D = number of dictionary entries
    - L = maximum code length (<= 50)

    The dynamic programming has n positions.
    For each reachable position i, we try every distinct length that exists in the dictionary.
    For a chosen length len, we compare the substring s[i..i+len-1] against every dictionary word
    of that same length, and each comparison costs O(len).

    So the worst-case complexity is:
        O(n * sum_over_all_dictionary_words(word_length))
    Since the sum of all dictionary code lengths is at most 50000, this is efficient enough.

    More explicitly, if we group words by length:
        O(n * Σ(count_of_words_with_length_len * len))

    Space Complexity:
    - O(n) for the DP array
    - O(D) for storing dictionary groups
    So total extra space is O(n + D)
    */
    public long MinimumFatigue(string s, IList<CodeEntry> dictionary)
    {
        int n = s.Length;

        // Step 1:
        // Group all dictionary words by their length.
        //
        // Why do we do this?
        // Because when we stand at position i in the stream, we can only choose a dictionary
        // code whose length matches the next piece we want to decode.
        //
        // Example:
        // If we are at index i and want to decode 3 characters, then only dictionary words
        // of length 3 are relevant. Words of length 1, 2, 4, ... cannot be used there.
        //
        // Grouping by length avoids checking impossible candidates.
        var groupsByLength = new Dictionary<int, List<CodeEntry>>();

        foreach (var entry in dictionary)
        {
            int len = entry.Word.Length;
            if (!groupsByLength.ContainsKey(len))
            {
                groupsByLength[len] = new List<CodeEntry>();
            }

            groupsByLength[len].Add(entry);
        }

        // If there are no dictionary lengths at all, decoding is impossible.
        if (groupsByLength.Count == 0)
        {
            return -1;
        }

        // Extract the distinct lengths into a list so we can iterate through them quickly.
        var lengths = new List<int>(groupsByLength.Keys);

        // Step 2:
        // Create the DP array.
        //
        // dp[i] = minimum total fatigue needed to decode the prefix s[0..i-1]
        //
        // That means:
        // - dp[0] = 0 because decoding an empty prefix costs nothing
        // - dp[n] will be our final answer if it is reachable
        //
        // We use a very large number to represent "currently impossible / not reached yet".
        long INF = long.MaxValue / 4;
        long[] dp = new long[n + 1];
        Array.Fill(dp, INF);
        dp[0] = 0;

        // Step 3:
        // Process positions from left to right.
        //
        // Why left to right?
        // Because if we already know the best way to decode the prefix ending at i,
        // then we can try extending that solution by one more piece.
        //
        // This is the classic dynamic programming idea:
        // solve smaller prefixes first, then use them to solve larger prefixes.
        for (int i = 0; i < n; i++)
        {
            // If dp[i] is still INF, then prefix s[0..i-1] cannot be decoded.
            // So there is no valid way to continue from here.
            if (dp[i] == INF)
            {
                continue;
            }

            // Try every dictionary length that exists.
            foreach (int len in lengths)
            {
                // The next piece would be s[i..i+len-1].
                // If it goes past the end of the string, we cannot use this length here.
                if (i + len > n)
                {
                    continue;
                }

                // We now want to find the cheapest dictionary word of this exact length
                // to match against the substring s[i..i+len-1].
                //
                // Cost for choosing a word = word fatigue + mismatch count
                //
                // We will compute the minimum such cost among all words of this length.
                long bestPieceCost = INF;

                var candidates = groupsByLength[len];

                // Compare the current substring against every dictionary word of this length.
                foreach (var candidate in candidates)
                {
                    int mismatches = 0;

                    // Character-by-character comparison.
                    //
                    // This is necessary because mismatch_count(piece, code) is defined as
                    // the number of positions where the characters differ.
                    for (int k = 0; k < len; k++)
                    {
                        if (s[i + k] != candidate.Word[k])
                        {
                            mismatches++;
                        }
                    }

                    long totalPieceCost = (long)candidate.Fatigue + mismatches;

                    if (totalPieceCost < bestPieceCost)
                    {
                        bestPieceCost = totalPieceCost;
                    }
                }

                // If we found at least one candidate (which we always do for existing lengths),
                // update dp[i + len].
                //
                // Interpretation:
                // - dp[i] is the best cost to decode the prefix up to i
                // - bestPieceCost is the cheapest way to decode the next len characters
                // Therefore:
                // - dp[i] + bestPieceCost is a candidate answer for dp[i + len]
                long newCost = dp[i] + bestPieceCost;
                if (newCost < dp[i + len])
                {
                    dp[i + len] = newCost;
                }
            }
        }

        // Step 4:
        // If dp[n] is still INF, then the full string could not be partitioned
        // using available dictionary lengths.
        //
        // Otherwise, dp[n] is the minimum total fatigue.
        return dp[n] == INF ? -1 : dp[n];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
string s1 = "abxdab";
var dictionary1 = new List<Solution.CodeEntry>
{
    new("ab", 1),
    new("ax", 2),
    new("xd", 1),
    new("dab", 3),
    new("zz", 0)
};

long result1 = solution.MinimumFatigue(s1, dictionary1);
Console.WriteLine(result1); // Expected: 3

// Example 2
string s2 = "abcde";
var dictionary2 = new List<Solution.CodeEntry>
{
    new("ab", 4),
    new("c", 2),
    new("de", 1),
    new("xyz", 0)
};

long result2 = solution.MinimumFatigue(s2, dictionary2);
Console.WriteLine(result2); // Expected: 7

// Additional demo: impossible case
string s3 = "abcd";
var dictionary3 = new List<Solution.CodeEntry>
{
    new("a", 1),
    new("bc", 2)
    // No way to cover the final 'd'
};

long result3 = solution.MinimumFatigue(s3, dictionary3);
Console.WriteLine(result3); // Expected: -1