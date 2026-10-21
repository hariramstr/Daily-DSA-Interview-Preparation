/*
Title: Minimum Cost to Encode a Chord Progression with Reusable Motifs
Difficulty: Hard
Topic: Dynamic Programming

Problem Description:
A music production tool stores a long chord progression as a string `progression` of uppercase letters, where each character represents one chord symbol after normalization. To reduce storage, the tool may encode the progression using a library of reusable motifs. Each motif `motifs[i]` is a non-empty string and has an associated non-negative cost `cost[i]`. You may use any motif any number of times. When a motif is used, it must match the next characters of the progression exactly and advances the encoding position by its length. You may also encode a single chord directly without using a motif, paying a fixed cost `singleCost` for that one character.

Your task is to compute the minimum total cost required to encode the entire progression from left to right. If multiple motifs overlap in possible usage, you may choose whichever sequence yields the smallest total cost. Motifs may have repeated strings with different costs; you should treat them as separate options, though an optimal solution will naturally prefer cheaper uses. Return the minimum possible cost.

This is not a compression ratio problem: every character of `progression` must be covered exactly once, either by a matching motif or by paying `singleCost` for that character.

Constraints:
- `1 <= progression.length <= 10^5`
- `1 <= motifs.length <= 10^4`
- `1 <= sum(motifs[i].length) <= 2 * 10^5`
- `1 <= motifs[i].length <= 10^4`
- `0 <= cost[i], singleCost <= 10^9`
- `progression` and all `motifs[i]` consist only of uppercase English letters.

Example 1:
Input: progression = "ABABAC", motifs = ["ABA", "BA", "AC"], cost = [4, 2, 3], singleCost = 2
Output: 7
Explanation: One optimal encoding is "ABA" + "BA" + direct('C') is invalid because coverage would overlap incorrectly. The best valid left-to-right encoding is "ABA" (cost 4) + "C" directly after using "AC" is impossible at position 3. Instead, encode as direct('A') + "BA" + "BA" + direct('C') = 2 + 2 + 2 + 2 = 8, or "ABA" + "AC" = 4 + 3 = 7, which is optimal.

Example 2:
Input: progression = "AAAAAA", motifs = ["A", "AA", "AAA"], cost = [5, 3, 4], singleCost = 2
Output: 8
Explanation: Directly encoding all six characters costs 12. Using "AAA" twice costs 4 + 4 = 8, which is better than any combination involving the more expensive single-character motif.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    private sealed class Node
    {
        public int[] Next = new int[26];
        public int Fail;
        public List<int> OutputLengths = new();
    }

    public long MinimumEncodingCost(string progression, string[] motifs, int[] cost, int singleCost)
    {
        /*
        Time Complexity:
        - Building the trie: O(sum of motif lengths)
        - Building failure links: O(number of trie nodes * alphabet size) in this implementation
        - Scanning the progression with Aho-Corasick: O(progression length + total number of matches reported)
        - Dynamic programming updates: O(progression length + total number of matches reported)
        Overall: O(sum(motifs[i].Length) + numberOfTrieNodes * 26 + progression.Length + totalMatches)

        Space Complexity:
        - Trie / automaton storage: O(sum of motif lengths)
        - DP array: O(progression length)
        - Match buckets: O(total number of matches reported)
        Overall: O(sum(motifs[i].Length) + progression.Length + totalMatches)

        Beginner-friendly high-level idea:
        1. We want the cheapest way to cover the string from left to right.
        2. This naturally suggests dynamic programming:
           dp[i] = minimum cost to encode the first i characters.
        3. From position i, we can:
           - encode one character directly, going to i + 1 with +singleCost
           - use any motif that matches starting at i, going to i + motifLength with +motifCost
        4. The hard part is finding all motif matches efficiently for a very long progression.
        5. To solve that efficiently, we use the Aho-Corasick automaton, which can find all motif occurrences in one left-to-right scan.
        6. Once we know every motif occurrence, we run DP to compute the minimum total cost.
        */

        int n = progression.Length;

        // ------------------------------------------------------------
        // Step 1: Preprocess motifs so that for each distinct motif string,
        // we only keep the cheapest cost.
        //
        // Why this is safe:
        // If the same motif text appears multiple times with different costs,
        // using the more expensive version is never better than using the cheaper one.
        // The problem statement says duplicates should be treated as separate options,
        // but an optimal solution will naturally prefer the cheaper one.
        // So compressing duplicates keeps correctness and reduces work.
        // ------------------------------------------------------------
        var cheapestByMotif = new Dictionary<string, long>(StringComparer.Ordinal);
        for (int i = 0; i < motifs.Length; i++)
        {
            string motif = motifs[i];
            long c = cost[i];

            if (cheapestByMotif.TryGetValue(motif, out long existing))
            {
                if (c < existing)
                {
                    cheapestByMotif[motif] = c;
                }
            }
            else
            {
                cheapestByMotif[motif] = c;
            }
        }

        // ------------------------------------------------------------
        // Step 2: Group motif costs by motif length.
        //
        // Why we do this:
        // During Aho-Corasick scanning, when a motif ends at position 'end',
        // we need to know:
        //   - its length
        //   - the cheapest cost for that exact motif
        //
        // Since we already compressed duplicates by exact string,
        // each inserted motif contributes exactly one (length, cost) pair.
        //
        // We store:
        //   motifCostByLength[length] = list of costs of motifs with that length
        //
        // But because the automaton identifies exact terminal nodes, we actually
        // attach the length to the terminal node and keep a separate map from
        // (terminal node occurrence) to the motif's cheapest cost.
        //
        // To keep the implementation simple and efficient, we will:
        // - insert each distinct motif into the trie
        // - at the terminal node, store the motif length
        // - maintain a dictionary from (node, length) logically by storing
        //   the cheapest cost per length at that terminal node
        //
        // Since one terminal node corresponds to one exact string in a trie,
        // storing only the length is enough, and we can store the cost in a
        // separate terminal-cost list aligned with output lengths.
        // ------------------------------------------------------------
        var nodes = new List<Node> { new Node() };
        var terminalCosts = new List<List<long>> { new List<long>() };

        foreach (var kvp in cheapestByMotif)
        {
            string motif = kvp.Key;
            long motifCost = kvp.Value;

            int current = 0;
            foreach (char ch in motif)
            {
                int idx = ch - 'A';
                if (nodes[current].Next[idx] == 0)
                {
                    nodes[current].Next[idx] = nodes.Count;
                    nodes.Add(new Node());
                    terminalCosts.Add(new List<long>());
                }
                current = nodes[current].Next[idx];
            }

            nodes[current].OutputLengths.Add(motif.Length);
            terminalCosts[current].Add(motifCost);
        }

        // ------------------------------------------------------------
        // Step 3: Build Aho-Corasick failure links.
        //
        // What failure links do:
        // If we are matching characters and cannot continue from the current trie node,
        // the failure link tells us the next best node representing the longest suffix
        // that is also a trie prefix.
        //
        // Why this is necessary:
        // It lets us scan the progression in linear time while finding all motif matches.
        //
        // Important implementation detail:
        // We also "merge" outputs from failure-linked nodes into each node.
        // That means if a shorter motif is a suffix of a longer matched motif,
        // we still report it automatically.
        // ------------------------------------------------------------
        var queue = new Queue<int>();

        for (int c = 0; c < 26; c++)
        {
            int next = nodes[0].Next[c];
            if (next != 0)
            {
                nodes[next].Fail = 0;
                queue.Enqueue(next);
            }
        }

        while (queue.Count > 0)
        {
            int v = queue.Dequeue();

            for (int c = 0; c < 26; c++)
            {
                int u = nodes[v].Next[c];
                if (u != 0)
                {
                    int fail = nodes[v].Fail;
                    while (fail != 0 && nodes[fail].Next[c] == 0)
                    {
                        fail = nodes[fail].Fail;
                    }

                    if (nodes[fail].Next[c] != 0)
                    {
                        fail = nodes[fail].Next[c];
                    }

                    nodes[u].Fail = fail;

                    // Merge outputs from the failure node into this node.
                    // This is crucial because if a motif ends here, and another motif
                    // also ends at a suffix represented by the failure link, both should
                    // be reported when we reach this node.
                    if (nodes[fail].OutputLengths.Count > 0)
                    {
                        nodes[u].OutputLengths.AddRange(nodes[fail].OutputLengths);
                        terminalCosts[u].AddRange(terminalCosts[fail]);
                    }

                    queue.Enqueue(u);
                }
            }
        }

        // ------------------------------------------------------------
        // Step 4: Scan the progression and collect all motif matches by start position.
        //
        // We want to later run DP from left to right.
        // For DP, it is convenient to know:
        //   at position start, which motifs can begin here?
        //
        // Aho-Corasick naturally reports matches by ending position.
        // So when a motif of length L ends at index end (0-based),
        // its start is:
        //   start = end - L + 1
        //
        // We store for each start position a list of transitions:
        //   (nextPosition, motifCost)
        //
        // This lets DP do:
        //   dp[nextPosition] = min(dp[nextPosition], dp[start] + motifCost)
        // ------------------------------------------------------------
        var matchesStartingAt = new List<(int nextPos, long motifCost)>[n];
        for (int i = 0; i < n; i++)
        {
            matchesStartingAt[i] = new List<(int nextPos, long motifCost)>();
        }

        int state = 0;
        for (int end = 0; end < n; end++)
        {
            int c = progression[end] - 'A';

            while (state != 0 && nodes[state].Next[c] == 0)
            {
                state = nodes[state].Fail;
            }

            if (nodes[state].Next[c] != 0)
            {
                state = nodes[state].Next[c];
            }

            // Every output at this state corresponds to a motif that ends at 'end'.
            for (int k = 0; k < nodes[state].OutputLengths.Count; k++)
            {
                int len = nodes[state].OutputLengths[k];
                long motifCost = terminalCosts[state][k];
                int start = end - len + 1;

                // start is guaranteed valid because a reported match must fit in the scanned prefix,
                // but checking it makes the code safer and easier to understand.
                if (start >= 0)
                {
                    matchesStartingAt[start].Add((end + 1, motifCost));
                }
            }
        }

        // ------------------------------------------------------------
        // Step 5: Dynamic Programming.
        //
        // Definition:
        //   dp[i] = minimum cost to encode progression[0..i-1]
        //         = minimum cost to encode the first i characters
        //
        // Base case:
        //   dp[0] = 0
        //   Encoding an empty prefix costs nothing.
        //
        // Transition from position i:
        // 1. Encode one character directly:
        //      dp[i + 1] = min(dp[i + 1], dp[i] + singleCost)
        //
        // 2. Use any motif that starts at i:
        //      dp[nextPos] = min(dp[nextPos], dp[i] + motifCost)
        //
        // Why this is correct:
        // Every valid encoding of the prefix ending at some position must end with
        // either a direct single-character encoding or one motif use.
        // We try all legal choices, so DP explores all valid encodings.
        // Taking minimum ensures we keep the cheapest one.
        // ------------------------------------------------------------
        long inf = long.MaxValue / 4;
        long[] dp = new long[n + 1];
        Array.Fill(dp, inf);
        dp[0] = 0;

        for (int i = 0; i < n; i++)
        {
            // If this state is unreachable, skip it.
            if (dp[i] == inf)
            {
                continue;
            }

            // Option 1: encode the current single character directly.
            // This is always allowed.
            long directCost = dp[i] + singleCost;
            if (directCost < dp[i + 1])
            {
                dp[i + 1] = directCost;
            }

            // Option 2: use any motif that matches starting exactly at position i.
            foreach (var match in matchesStartingAt[i])
            {
                long candidate = dp[i] + match.motifCost;
                if (candidate < dp[match.nextPos])
                {
                    dp[match.nextPos] = candidate;
                }
            }
        }

        return dp[n];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
string progression1 = "ABABAC";
string[] motifs1 = { "ABA", "BA", "AC" };
int[] cost1 = { 4, 2, 3 };
int singleCost1 = 2;
long result1 = solution.MinimumEncodingCost(progression1, motifs1, cost1, singleCost1);
Console.WriteLine(result1); // Expected: 7

// Example 2
string progression2 = "AAAAAA";
string[] motifs2 = { "A", "AA", "AAA" };
int[] cost2 = { 5, 3, 4 };
int singleCost2 = 2;
long result2 = solution.MinimumEncodingCost(progression2, motifs2, cost2, singleCost2);
Console.WriteLine(result2); // Expected: 8

// Additional quick sanity check
string progression3 = "ABCDEF";
string[] motifs3 = { "AB", "CDE", "F" };
int[] cost3 = { 3, 4, 10 };
int singleCost3 = 2;
// Best: "AB"(3) + "CDE"(4) + direct('F')(2) = 9
long result3 = solution.MinimumEncodingCost(progression3, motifs3, cost3, singleCost3);
Console.WriteLine(result3); // Expected: 9