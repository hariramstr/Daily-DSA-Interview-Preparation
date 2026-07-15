/*
Title: Minimum Cost to Decode a Split Keyboard Message

Problem Description:
A mobile device recorded a typed message, but its keyboard firmware was corrupted. Instead of storing the intended characters directly, the device stored a string s of lowercase letters representing raw key scan groups. You are also given a dictionary of valid words. Each dictionary word w has two properties: a positive integer decodeCost, and a positive integer splitPenalty.

A word w can decode a contiguous substring of s if that substring can be partitioned into one or more non-empty pieces whose concatenation is exactly w, and every piece is either kept in order or reversed before being matched. However, each time you split w into more than one piece, you must pay (number of pieces - 1) * splitPenalty in addition to decodeCost.

For example, the word tablet can decode tabelt by splitting as tab | let, then matching tab in order and let reversed as tel if the substring arrangement allows it. A word may be reused any number of times.

Your task is to decode the entire string s into a sequence of dictionary words with minimum total cost. If it is impossible to decode the full string, return -1.

Formally, let dp[i] be the minimum cost to decode the prefix s[0...i-1]. You must compute the minimum possible cost for s[0...n-1].

Constraints:
- 1 <= s.length <= 400
- 1 <= dictionary.length <= 120
- Sum of lengths of all dictionary words <= 2500
- 1 <= word.length <= 40
- 1 <= decodeCost, splitPenalty <= 10^6
- All strings contain only lowercase English letters
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    Let n = s.Length, D = number of dictionary words, and L = maximum word length (<= 40).

    For each dictionary word of length m, we precompute the minimum split cost needed to transform
    the word into every possible target string obtainable by partitioning the word into pieces and
    reversing each piece independently. The number of such target strings is at most 2^(m-1) * 2,
    because there are (m-1) possible cut positions and each final piece can be either normal or reversed.
    Since m <= 40 and the total dictionary length <= 2500, this is manageable only because practical
    branching is heavily reduced by memoization on intervals. Our implementation uses interval DP:
    for every substring of the word, we compute all strings obtainable from that interval with their
    minimum number of pieces. This is exponential in the worst theoretical case, but with the given
    small word length and dictionary size it is suitable for the problem constraints.

    After precomputation, the main DP over s is:
    O(n * totalNumberOfGeneratedFormsAcrossDictionary)

    Space Complexity:
    O(totalNumberOfGeneratedFormsAcrossDictionary + n)

    Important note:
    The core challenge is correctly modeling the rule:
    - The word is partitioned into contiguous pieces.
    - Each piece is either kept or reversed.
    - The resulting concatenation must equal the substring in s.
    - Cost = decodeCost + (pieces - 1) * splitPenalty.

    This implementation computes, for each dictionary word, every reachable transformed string together
    with the minimum number of pieces needed to produce it. Then the outer DP simply checks which transformed
    strings match each position in s.
    */
    public long MinimumDecodeCost(string s, string[][] dictionary)
    {
        int n = s.Length;
        const long INF = long.MaxValue / 4;

        // We will preprocess every dictionary word into a map:
        // transformedString -> minimum total cost to use that word to produce transformedString.
        //
        // Why do we do this?
        // Because the decoding rule for a single word is independent from the global string DP.
        // Once we know all substrings a word can generate and the cheapest way to generate each one,
        // the final problem becomes a standard "minimum cost string segmentation" dynamic programming task.
        //
        // Since each word length is at most 40, generating all reachable forms per word is feasible
        // with memoized interval DP.
        var forms = new List<Dictionary<string, long>>();

        foreach (var entry in dictionary)
        {
            string word = entry[0];
            long decodeCost = long.Parse(entry[1]);
            long splitPenalty = long.Parse(entry[2]);

            // Build all possible transformed strings for this word, and for each transformed string
            // store the minimum number of pieces required.
            //
            // Then convert "minimum pieces" into actual monetary cost:
            // decodeCost + (pieces - 1) * splitPenalty
            var transformedToPieces = BuildAllFormsWithMinPieces(word);

            var transformedToCost = new Dictionary<string, long>();
            foreach (var kvp in transformedToPieces)
            {
                int pieces = kvp.Value;
                long totalCost = decodeCost + (long)(pieces - 1) * splitPenalty;

                if (!transformedToCost.TryGetValue(kvp.Key, out long existing) || totalCost < existing)
                {
                    transformedToCost[kvp.Key] = totalCost;
                }
            }

            forms.Add(transformedToCost);
        }

        // Standard DP:
        // dp[i] = minimum cost to decode prefix s[0..i-1]
        long[] dp = new long[n + 1];
        Array.Fill(dp, INF);
        dp[0] = 0;

        // To make matching efficient, group all transformed strings by their length.
        // For each length len, we keep a map:
        // exactString -> minimum cost among all dictionary words that can produce that exact string.
        //
        // Why is this useful?
        // During DP at position i, we only need to inspect substring lengths that exist in the dictionary forms.
        // Then we can directly look up whether s[i..i+len-1] is a valid transformed word.
        var byLength = new Dictionary<int, Dictionary<string, long>>();

        foreach (var wordForms in forms)
        {
            foreach (var kvp in wordForms)
            {
                string transformed = kvp.Key;
                long cost = kvp.Value;
                int len = transformed.Length;

                if (!byLength.TryGetValue(len, out var map))
                {
                    map = new Dictionary<string, long>();
                    byLength[len] = map;
                }

                if (!map.TryGetValue(transformed, out long existing) || cost < existing)
                {
                    map[transformed] = cost;
                }
            }
        }

        var lengths = new List<int>(byLength.Keys);
        lengths.Sort();

        // Main prefix DP.
        for (int i = 0; i < n; i++)
        {
            // If prefix up to i cannot be decoded, there is no reason to extend from here.
            if (dp[i] == INF) continue;

            // Try every possible transformed-word length.
            foreach (int len in lengths)
            {
                int j = i + len;
                if (j > n) break;

                // Extract the candidate substring from s.
                string sub = s.Substring(i, len);

                // If some dictionary word can generate exactly this substring,
                // update the DP transition.
                if (byLength[len].TryGetValue(sub, out long cost))
                {
                    long candidate = dp[i] + cost;
                    if (candidate < dp[j])
                    {
                        dp[j] = candidate;
                    }
                }
            }
        }

        return dp[n] == INF ? -1 : dp[n];
    }

    private Dictionary<string, int> BuildAllFormsWithMinPieces(string word)
    {
        int m = word.Length;

        // Memoization table for interval DP.
        // Key: (l, r) inclusive interval in the original word.
        // Value: map transformedString -> minimum number of pieces needed to produce it from word[l..r].
        //
        // The interval DP idea:
        // A transformed result for word[l..r] can be produced in two ways:
        //
        // 1) Use the entire interval as ONE piece:
        //    - normal: word[l..r]
        //    - reversed: reverse(word[l..r])
        //
        // 2) Split at some k between l and r:
        //    word[l..k] + word[k+1..r]
        //    Then concatenate any valid transformed left result with any valid transformed right result.
        //    The number of pieces is piecesLeft + piecesRight.
        //
        // This exactly matches the problem statement because every valid partition is a sequence of contiguous pieces,
        // and each piece may independently be kept or reversed.
        var memo = new Dictionary<(int, int), Dictionary<string, int>>();

        Dictionary<string, int> Dfs(int l, int r)
        {
            if (memo.TryGetValue((l, r), out var cached))
            {
                return cached;
            }

            var result = new Dictionary<string, int>();

            // Step 1: treat the whole interval as one single piece.
            //
            // This is necessary because the problem allows "no split" as a valid partition.
            // Also, even if we later split, the one-piece option may be cheaper because fewer pieces
            // means smaller split penalty.
            string normal = word.Substring(l, r - l + 1);
            AddOrMin(result, normal, 1);

            char[] chars = normal.ToCharArray();
            Array.Reverse(chars);
            string reversed = new string(chars);
            AddOrMin(result, reversed, 1);

            // Step 2: try every possible split point.
            //
            // Why every split?
            // Because the partition of the word into pieces can happen at any boundary between characters.
            // To be correct, we must consider all such boundaries.
            for (int k = l; k < r; k++)
            {
                var leftMap = Dfs(l, k);
                var rightMap = Dfs(k + 1, r);

                // Combine every left form with every right form.
                //
                // If left interval can become A using p pieces,
                // and right interval can become B using q pieces,
                // then the whole interval can become A+B using p+q pieces.
                foreach (var left in leftMap)
                {
                    foreach (var right in rightMap)
                    {
                        string combined = left.Key + right.Key;
                        int pieces = left.Value + right.Value;
                        AddOrMin(result, combined, pieces);
                    }
                }
            }

            memo[(l, r)] = result;
            return result;
        }

        return Dfs(0, m - 1);
    }

    private void AddOrMin(Dictionary<string, int> map, string key, int value)
    {
        if (!map.TryGetValue(key, out int existing) || value < existing)
        {
            map[key] = value;
        }
    }
}

// Demo code

var solution = new Solution();

// Example 1
string s1 = "tabeltcode";
string[][] dictionary1 =
{
    new[] { "tablet", "5", "2" },
    new[] { "code", "3", "1" },
    new[] { "tab", "4", "1" },
    new[] { "let", "2", "1" }
};
Console.WriteLine(solution.MinimumDecodeCost(s1, dictionary1));

// Example 2
string s2 = "abdc";
string[][] dictionary2 =
{
    new[] { "abcd", "6", "5" },
    new[] { "ab", "2", "1" },
    new[] { "cd", "2", "1" }
};
Console.WriteLine(solution.MinimumDecodeCost(s2, dictionary2));