/*
Title: Minimum Cost to Assemble a Target Robot Command Stream
Difficulty: Hard
Topic: Dynamic Programming

Problem Description:
A robotics team stores reusable command macros in a library. Each macro is a non-empty string made of lowercase English letters, and executing that macro contributes a fixed cost. You are given a target command stream target, along with arrays macros and cost, where macros[i] can be used any number of times and appending macros[i] adds cost[i] to the total cost.

Your task is to build target exactly by concatenating chosen macros in order. You may reuse the same macro many times, but every chosen macro must match the next characters of target exactly at the position where it is placed. Return the minimum total cost needed to assemble the entire target string, or -1 if it is impossible.

This is not a shortest-length problem: a more expensive long macro may be worse than several cheaper short macros, and duplicate macro strings may appear with different costs. The answer should use the cheapest possible combination.

Constraints:
- 1 <= target.length <= 10^5
- 1 <= macros.length <= 10^5
- 1 <= macros[i].length <= 10^5
- Sum of all macros[i].length <= 2 * 10^5
- 1 <= cost[i] <= 10^9
- target and every macros[i] consist only of lowercase English letters

Example 1:
Input: target = "ababa", macros = ["ab", "aba", "ba", "a"], cost = [4, 5, 2, 10]
Output: 7
Explanation: One optimal construction is "aba" + "ba" with total cost 5 + 2 = 7. Using "ab" + "a" + "ba" would cost 16, which is worse.

Example 2:
Input: target = "robot", macros = ["ro", "bot", "obo", "t"], cost = [3, 4, 10, 1]
Output: -1
Explanation: You can start with "ro", leaving "bot", but no sequence of allowed concatenations matches the remaining characters exactly from every position. Therefore the target cannot be assembled.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    private sealed class Node
    {
        public int[] Next = new int[26];
        public int Fail;
        public List<(int Length, long Cost)> Outputs = new();
    }

    /*
    Time Complexity:
    - Building a dictionary that keeps only the cheapest cost for duplicate macro strings:
      O(total length of all macros)
    - Building the Aho-Corasick trie + failure links:
      O(total length of unique macros * alphabet size factor hidden as constant 26)
    - Scanning the target once through the automaton:
      O(target length + number of matched outputs reported)
    - Dynamic programming transitions:
      O(number of matched outputs reported)

    Overall:
    O(sum(macros[i].Length) + target.Length + totalMatchesReported)

    Space Complexity:
    O(sum(macros[i].Length)) for the trie/automaton, output lists, and DP array.
    */
    public long MinimumCost(string target, string[] macros, int[] cost)
    {
        // We use a very large number to represent "unreachable".
        // long is required because:
        // - each chosen macro can cost up to 1e9
        // - target length can be up to 1e5
        // - total answer can therefore be much larger than int
        const long INF = long.MaxValue / 4;

        // ------------------------------------------------------------
        // STEP 1: Deduplicate identical macro strings by keeping only
        //         the cheapest cost for each exact string.
        //
        // Why this is necessary:
        // If the same macro appears multiple times with different costs,
        // there is never a reason to keep a more expensive copy of the
        // exact same string, because both create the same transition in
        // the target but one costs more.
        //
        // Example:
        // macros = ["ab", "ab"], cost = [10, 4]
        // We only need "ab" with cost 4.
        // ------------------------------------------------------------
        var cheapest = new Dictionary<string, long>(StringComparer.Ordinal);

        for (int i = 0; i < macros.Length; i++)
        {
            string word = macros[i];
            long c = cost[i];

            if (cheapest.TryGetValue(word, out long existing))
            {
                if (c < existing)
                {
                    cheapest[word] = c;
                }
            }
            else
            {
                cheapest[word] = c;
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Build an Aho-Corasick automaton from the unique macros.
        //
        // Why Aho-Corasick?
        // A naive solution would try every macro at every target position,
        // which is far too slow for large inputs.
        //
        // Aho-Corasick lets us scan the target once and efficiently discover
        // every macro that ends at every position.
        //
        // This is exactly what we need for dynamic programming:
        // whenever a macro of length L ends at target position i,
        // it means that macro matches target substring:
        // target[i-L+1 .. i]
        //
        // Then we can transition:
        // dp[i + 1] = min(dp[i + 1], dp[i + 1 - L] + macroCost)
        //
        // because if we can build the prefix ending before this macro starts,
        // then we can append this macro and build a longer prefix.
        // ------------------------------------------------------------
        var nodes = new List<Node> { new Node() }; // node 0 = root

        foreach (var entry in cheapest)
        {
            string word = entry.Key;
            long c = entry.Value;

            int current = 0;

            // Insert the word character by character into the trie.
            for (int i = 0; i < word.Length; i++)
            {
                int ch = word[i] - 'a';

                if (nodes[current].Next[ch] == 0)
                {
                    nodes[current].Next[ch] = nodes.Count;
                    nodes.Add(new Node());
                }

                current = nodes[current].Next[ch];
            }

            // Mark that a macro ends at this trie node.
            // We store both:
            // - Length: needed to know where the matched macro starts
            // - Cost: needed for the DP transition
            nodes[current].Outputs.Add((word.Length, c));
        }

        // ------------------------------------------------------------
        // STEP 3: Build failure links using BFS.
        //
        // What is a failure link?
        // If we are at some trie node and the next character does not continue
        // the current trie path, the failure link tells us the next best state
        // that represents the longest suffix that is also a trie prefix.
        //
        // Why this matters:
        // It allows us to continue matching in linear time over the target.
        //
        // Important implementation detail:
        // We also merge output lists from failure-linked nodes.
        // That means when we arrive at a node, its Outputs list contains:
        // - macros ending exactly at this node
        // - macros ending at suffix states reachable through failure links
        //
        // This makes target scanning simpler, because every match ending at the
        // current position is directly available in the current state's Outputs.
        // ------------------------------------------------------------
        var queue = new Queue<int>();

        // Initialize root transitions.
        // For missing root edges, we keep them as 0 so they point back to root.
        for (int ch = 0; ch < 26; ch++)
        {
            int next = nodes[0].Next[ch];
            if (next != 0)
            {
                nodes[next].Fail = 0;
                queue.Enqueue(next);
            }
        }

        while (queue.Count > 0)
        {
            int v = queue.Dequeue();

            for (int ch = 0; ch < 26; ch++)
            {
                int u = nodes[v].Next[ch];

                if (u != 0)
                {
                    // Compute failure link for child u.
                    int fail = nodes[v].Fail;

                    while (fail != 0 && nodes[fail].Next[ch] == 0)
                    {
                        fail = nodes[fail].Fail;
                    }

                    if (nodes[fail].Next[ch] != 0)
                    {
                        nodes[u].Fail = nodes[fail].Next[ch];
                    }
                    else
                    {
                        nodes[u].Fail = 0;
                    }

                    // Merge outputs from the failure state into this node.
                    // Why?
                    // Suppose current node corresponds to "...aba"
                    // and its failure state corresponds to "...ba".
                    // If "ba" is also a macro, then when we end at "...aba",
                    // both "aba" and "ba" are valid matches ending here.
                    foreach (var output in nodes[nodes[u].Fail].Outputs)
                    {
                        nodes[u].Outputs.Add(output);
                    }

                    queue.Enqueue(u);
                }
            }
        }

        // ------------------------------------------------------------
        // STEP 4: Dynamic Programming over target prefixes.
        //
        // dp[i] = minimum cost to build the first i characters of target
        //         meaning target[0 .. i-1]
        //
        // Base case:
        // dp[0] = 0 because building an empty prefix costs nothing.
        //
        // Transition:
        // If a macro of length L and cost C ends at target index i (0-based),
        // then it covers target[i-L+1 .. i].
        //
        // So if dp[(i + 1) - L] is reachable, then:
        // dp[i + 1] = min(dp[i + 1], dp[(i + 1) - L] + C)
        //
        // Why this is correct:
        // Any valid construction of the first i+1 characters must end with
        // some final macro. That final macro must match a suffix ending at i.
        // Therefore checking all matched macros ending at i covers all possible
        // last-step choices.
        // ------------------------------------------------------------
        int n = target.Length;
        long[] dp = new long[n + 1];
        Array.Fill(dp, INF);
        dp[0] = 0;

        int state = 0;

        // Scan the target from left to right exactly once.
        for (int i = 0; i < n; i++)
        {
            int ch = target[i] - 'a';

            // Follow failure links until we can consume this character,
            // or until we return to root.
            while (state != 0 && nodes[state].Next[ch] == 0)
            {
                state = nodes[state].Fail;
            }

            // If there is a trie edge for this character, take it.
            // Otherwise remain at root.
            if (nodes[state].Next[ch] != 0)
            {
                state = nodes[state].Next[ch];
            }
            else
            {
                state = 0;
            }

            // Every output in this state is a macro that ends at position i.
            // We try all corresponding DP transitions.
            foreach (var (length, macroCost) in nodes[state].Outputs)
            {
                int startPrefixLength = (i + 1) - length;

                // If the prefix before this macro is reachable,
                // then we can append this macro.
                if (startPrefixLength >= 0 && dp[startPrefixLength] != INF)
                {
                    long candidate = dp[startPrefixLength] + macroCost;
                    if (candidate < dp[i + 1])
                    {
                        dp[i + 1] = candidate;
                    }
                }
            }
        }

        // If the full target prefix is still unreachable, return -1.
        return dp[n] == INF ? -1 : dp[n];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
string target1 = "ababa";
string[] macros1 = { "ab", "aba", "ba", "a" };
int[] cost1 = { 4, 5, 2, 10 };
long result1 = solution.MinimumCost(target1, macros1, cost1);
Console.WriteLine(result1); // Expected: 7

// Example 2
string target2 = "robot";
string[] macros2 = { "ro", "bot", "obo", "t" };
int[] cost2 = { 3, 4, 10, 1 };
long result2 = solution.MinimumCost(target2, macros2, cost2);
Console.WriteLine(result2); // Expected: 8

// Additional demo showing duplicate macros with different costs
string target3 = "aaaa";
string[] macros3 = { "a", "aa", "a" };
int[] cost3 = { 5, 3, 1 };
long result3 = solution.MinimumCost(target3, macros3, cost3);
Console.WriteLine(result3); // Expected: 2 using "a" + "a" + "a" + "a" or 4? Actually cheapest is four "a" = 4, two "aa" = 6, so expected 4