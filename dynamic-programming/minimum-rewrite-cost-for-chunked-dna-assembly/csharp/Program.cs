/*
Title: Minimum Rewrite Cost for Chunked DNA Assembly
Difficulty: Hard
Topic: Dynamic Programming

Problem Description:
A bioinformatics pipeline is assembling a target DNA string from a catalog of reusable fragments.
You are given a target string target of length n and a list of fragments parts, where each fragment
parts[i] has an associated non-negative rewrite cost cost[i].

You may build the target from left to right by repeatedly choosing any fragment and placing it over
the next uncovered portion of the target. A fragment can only be placed if its characters exactly
match the corresponding substring of target. Fragments may be reused any number of times.

However, each time you use a fragment whose length is different from the length of the fragment used
immediately before it, the assembly machine must be recalibrated. This adds an extra penalty
switchCost to that placement, except for the very first fragment, which never pays a switch penalty.

Return the minimum total cost needed to assemble the entire target exactly. If it is impossible,
return -1.

Formally, if you place fragments with indices f1, f2, ..., fk, such that their concatenation equals
target, then the total cost is:
cost[f1] + cost[f2] + ... + cost[fk] + switchCost * (# of i from 2..k where len(parts[fi]) != len(parts[f(i-1)]))

Constraints:
- 1 <= n <= 10^4
- 1 <= parts.length <= 2 * 10^4
- 1 <= parts[i].length <= 50
- sum(parts[i].length) <= 2 * 10^5
- 0 <= cost[i] <= 10^9
- 0 <= switchCost <= 10^9
- target and all parts[i] consist only of uppercase letters A, C, G, T
*/

using System;
using System.Collections.Generic;

class Solution
{
    private const long INF = long.MaxValue / 4;

    /*
    Time Complexity:
    - Building the trie: O(sum(parts[i].Length))
    - Dynamic programming transitions:
      For each target position, we walk forward in the trie for at most 50 characters,
      and for each matched fragment length we try all possible previous lengths (1..50).
      Therefore this part is O(n * 50 * 50).
    - Total: O(sum(parts[i].Length) + n * 2500)

    Space Complexity:
    - Trie storage: O(sum(parts[i].Length))
    - DP table: O((n + 1) * 51)
    - Total: O(sum(parts[i].Length) + n * 51)

    Beginner-friendly summary:
    We use:
    1) A trie to quickly find which fragments match the target starting at each position.
    2) Dynamic programming where the state remembers:
       - how much of the target we have already built
       - the length of the last fragment used
    This is necessary because the extra switch penalty depends on the previous fragment length.
    */
    public long MinimumRewriteCost(string target, string[] parts, int[] cost, long switchCost)
    {
        int n = target.Length;

        // ------------------------------------------------------------
        // STEP 1: Preprocess fragments by keeping only the cheapest cost
        // for each distinct fragment string.
        //
        // Why?
        // If the same fragment text appears multiple times with different costs,
        // we would never want to use the more expensive copy because the fragment
        // matches exactly the same positions and has the same length.
        //
        // This reduces redundant work and keeps the trie smaller.
        // ------------------------------------------------------------
        var cheapestByPart = new Dictionary<string, long>(StringComparer.Ordinal);
        for (int i = 0; i < parts.Length; i++)
        {
            string p = parts[i];
            long c = cost[i];

            if (!cheapestByPart.TryGetValue(p, out long existing) || c < existing)
            {
                cheapestByPart[p] = c;
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Build a trie of all unique fragments.
        //
        // Why a trie?
        // We need to know, for every position i in target, which fragments match
        // target starting at i.
        //
        // A naive approach would compare every fragment against every position,
        // which is too slow.
        //
        // With a trie, from each position i we walk character by character through
        // target[i], target[i+1], ... and discover all matching fragments in one pass
        // up to maximum fragment length (50).
        // ------------------------------------------------------------
        var trie = new Trie();
        foreach (var kvp in cheapestByPart)
        {
            trie.Insert(kvp.Key, kvp.Value);
        }

        // ------------------------------------------------------------
        // STEP 3: Dynamic programming table.
        //
        // dp[pos, lastLen] = minimum cost to build exactly target[0..pos-1]
        //                    where the most recently used fragment has length lastLen.
        //
        // Special meaning:
        // lastLen = 0 means "no fragment has been used yet".
        // This is only valid at pos = 0.
        //
        // Why do we need lastLen in the state?
        // Because when we place the next fragment, we may need to pay switchCost
        // if its length differs from the previous fragment length.
        // ------------------------------------------------------------
        long[,] dp = new long[n + 1, 51];
        for (int i = 0; i <= n; i++)
        {
            for (int len = 0; len <= 50; len++)
            {
                dp[i, len] = INF;
            }
        }
        dp[0, 0] = 0;

        // ------------------------------------------------------------
        // STEP 4: Iterate over every starting position in the target.
        //
        // At each position "start", we ask:
        // "Which fragments can match target starting here?"
        //
        // The trie gives us all such matches efficiently.
        // For every matching fragment of length nextLen and fragment cost partCost,
        // we try extending every valid previous state dp[start, prevLen].
        // ------------------------------------------------------------
        for (int start = 0; start < n; start++)
        {
            // Quick check: if no DP state is reachable at this position, skip it.
            bool reachable = false;
            for (int prevLen = 0; prevLen <= 50; prevLen++)
            {
                if (dp[start, prevLen] < INF)
                {
                    reachable = true;
                    break;
                }
            }

            if (!reachable)
            {
                continue;
            }

            // --------------------------------------------------------
            // Find all fragments that match target starting at "start".
            //
            // Each returned pair is:
            // - fragment length
            // - cheapest cost among fragments with exactly that text
            //
            // Note:
            // Different fragment texts can have the same length and both match
            // at different positions, but at a fixed start position and fixed length,
            // target substring is fixed. Therefore only one exact text can match.
            // --------------------------------------------------------
            var matches = trie.GetMatches(target, start);

            // --------------------------------------------------------
            // For every matching fragment, perform DP transitions.
            // --------------------------------------------------------
            foreach (var match in matches)
            {
                int nextLen = match.Length;
                long partCost = match.Cost;
                int end = start + nextLen;

                // Try all possible previous fragment lengths.
                for (int prevLen = 0; prevLen <= 50; prevLen++)
                {
                    long current = dp[start, prevLen];
                    if (current >= INF)
                    {
                        continue;
                    }

                    // ------------------------------------------------
                    // Compute the extra switch penalty.
                    //
                    // Rules:
                    // - If prevLen == 0, this is the first fragment, so no penalty.
                    // - Otherwise, if prevLen != nextLen, pay switchCost.
                    // - If prevLen == nextLen, no penalty.
                    // ------------------------------------------------
                    long extra = partCost;
                    if (prevLen != 0 && prevLen != nextLen)
                    {
                        extra += switchCost;
                    }

                    long candidate = current + extra;
                    if (candidate < dp[end, nextLen])
                    {
                        dp[end, nextLen] = candidate;
                    }
                }
            }
        }

        // ------------------------------------------------------------
        // STEP 5: The answer is the minimum cost among all states that
        // have built the full target, regardless of the last fragment length.
        // ------------------------------------------------------------
        long answer = INF;
        for (int lastLen = 1; lastLen <= 50; lastLen++)
        {
            if (dp[n, lastLen] < answer)
            {
                answer = dp[n, lastLen];
            }
        }

        return answer >= INF ? -1 : answer;
    }

    private sealed class Trie
    {
        private sealed class Node
        {
            // Because the alphabet is only {A, C, G, T}, we can store children
            // in a fixed-size array of length 4 instead of a dictionary.
            // This is faster and more memory-efficient.
            public int[] Next = new int[] { -1, -1, -1, -1 };

            // If a fragment ends at this node, EndCost stores the cheapest cost
            // for that exact fragment string. Otherwise it stays INF.
            public long EndCost = INF;
        }

        private readonly List<Node> _nodes = new();

        public Trie()
        {
            _nodes.Add(new Node()); // root
        }

        public void Insert(string s, long cost)
        {
            int cur = 0;
            foreach (char ch in s)
            {
                int idx = Map(ch);
                if (_nodes[cur].Next[idx] == -1)
                {
                    _nodes[cur].Next[idx] = _nodes.Count;
                    _nodes.Add(new Node());
                }
                cur = _nodes[cur].Next[idx];
            }

            if (cost < _nodes[cur].EndCost)
            {
                _nodes[cur].EndCost = cost;
            }
        }

        public List<Match> GetMatches(string target, int start)
        {
            var result = new List<Match>();
            int cur = 0;

            // We walk forward from target[start] for at most 50 characters,
            // because no fragment can be longer than 50.
            for (int i = start; i < target.Length && i < start + 50; i++)
            {
                int idx = Map(target[i]);
                int next = _nodes[cur].Next[idx];

                // If the trie has no edge for this character, then no longer fragment
                // can match either, so we stop immediately.
                if (next == -1)
                {
                    break;
                }

                cur = next;

                // If a fragment ends here, then target[start..i] matches a valid fragment.
                if (_nodes[cur].EndCost < INF)
                {
                    result.Add(new Match(i - start + 1, _nodes[cur].EndCost));
                }
            }

            return result;
        }

        private static int Map(char c)
        {
            return c switch
            {
                'A' => 0,
                'C' => 1,
                'G' => 2,
                'T' => 3,
                _ => throw new ArgumentException("Invalid DNA character.")
            };
        }
    }

    private readonly record struct Match(int Length, long Cost);
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
string target1 = "ACGTAC";
string[] parts1 = { "AC", "GT", "ACG", "TAC" };
int[] cost1 = { 3, 2, 5, 4 };
long switchCost1 = 6;
Console.WriteLine(solution.MinimumRewriteCost(target1, parts1, cost1, switchCost1)); // Expected: 8

// Example 2
string target2 = "AACGT";
string[] parts2 = { "AA", "A", "CG", "GT" };
int[] cost2 = { 4, 2, 3, 3 };
long switchCost2 = 5;
Console.WriteLine(solution.MinimumRewriteCost(target2, parts2, cost2, switchCost2)); // Expected: -1

// Additional sanity check:
// "AA" + "CG" + "T" with "T" available would be 4 + 3 + 1 + 5 = 13 if lengths are 2,2,1
// only one switch from 2 to 1.
// Let's verify the solver handles such cases correctly.
string target3 = "AACGT";
string[] parts3 = { "AA", "A", "CG", "GT", "T" };
int[] cost3 = { 4, 2, 3, 3, 1 };
long switchCost3 = 5;
Console.WriteLine(solution.MinimumRewriteCost(target3, parts3, cost3, switchCost3)); // Expected: 13