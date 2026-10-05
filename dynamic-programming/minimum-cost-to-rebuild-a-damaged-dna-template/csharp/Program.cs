/*
Title: Minimum Cost to Rebuild a Damaged DNA Template
Difficulty: Hard
Topic: Dynamic Programming

Problem Description:
A genetics lab stores a reference DNA template as a string target consisting only of the characters 'A', 'C', 'G', and 'T'.
After a storage failure, the lab can no longer access the original template directly, but it still has a collection of DNA fragments.
Each fragment fragments[i] can be used any number of times, and using it once adds a fixed assembly cost cost[i].
A fragment may be placed only if it exactly matches the corresponding substring of target at the chosen position.
The final reconstructed sequence must equal target exactly, with no extra characters and no mismatches.

Your task is to compute the minimum total cost required to reconstruct the entire target.
If it is impossible, return -1.

Fragments may overlap in the input set, and different fragments may have the same string but different costs.
Because fragments can be reused, choosing a locally cheapest fragment does not always lead to a globally optimal answer.
An efficient dynamic programming solution is required.

Constraints:
- 1 <= target.length <= 10^5
- 1 <= fragments.length <= 10^4
- 1 <= fragments[i].length <= 200
- sum(fragments[i].length) <= 2 * 10^5
- 1 <= cost[i] <= 10^9
- target and every fragment contain only 'A', 'C', 'G', and 'T'

Important clarification:
To reconstruct the target exactly, we must partition the target into consecutive fragments.
That means if we are currently at position i, we may place only a fragment that matches target starting at i,
and then we continue from i + fragment.Length.
This is the standard "minimum cost string construction" dynamic programming interpretation.

Examples:
1) target = "ACGTAC", fragments = ["AC", "CGT", "GT", "AC"], cost = [4, 5, 3, 2]
   Minimum cost = 2 + 3 + 2 = 7 using:
   - "AC" at position 0 with cost 2
   - "GT" at position 2 with cost 3
   - "AC" at position 4 with cost 2

2) target = "AAGT", fragments = ["AA", "AG", "GT"], cost = [3, 4, 2]
   Minimum cost = 3 + 2 = 5 using:
   - "AA" at position 0 with cost 3
   - "GT" at position 2 with cost 2
*/

using System;
using System.Collections.Generic;

public class Solution
{
    // We use a Trie to store all fragments.
    // Each Trie node represents a prefix of some fragment.
    // If a node marks the end of one or more fragments, we store the minimum cost
    // among all identical fragment strings, because using a more expensive duplicate
    // is never beneficial.
    private sealed class TrieNode
    {
        public int[] Next = { -1, -1, -1, -1 };
        public long EndCost = long.MaxValue;
    }

    /*
    Time Complexity:
    - Building the trie: O(sum of fragment lengths)
    - Dynamic programming transitions:
      For each target position, we walk forward in the trie for at most maxFragmentLength characters.
      Since maxFragmentLength <= 200, this is O(target.Length * 200) in the worst case.
    - Total: O(sum(fragments[i].Length) + target.Length * maxFragmentLength)

    Space Complexity:
    - Trie storage: O(sum of fragment lengths)
    - DP array: O(target.Length)
    - Total: O(sum(fragments[i].Length) + target.Length)
    */
    public long MinimumCost(string target, string[] fragments, int[] cost)
    {
        // -----------------------------
        // Step 1: Build a trie of fragments.
        // -----------------------------
        // Why a trie?
        // If we try every fragment at every position directly, that can be too slow.
        // A trie lets us start from a target position and walk character by character,
        // discovering all fragments that match that position in one pass.
        //
        // Because the alphabet is only {A, C, G, T}, each node has exactly 4 possible edges.
        // This makes the trie compact and fast.
        var trie = new List<TrieNode> { new TrieNode() };
        int maxLen = 0;

        for (int i = 0; i < fragments.Length; i++)
        {
            string fragment = fragments[i];
            maxLen = Math.Max(maxLen, fragment.Length);

            int node = 0;
            for (int j = 0; j < fragment.Length; j++)
            {
                int idx = Map(fragment[j]);

                if (trie[node].Next[idx] == -1)
                {
                    trie[node].Next[idx] = trie.Count;
                    trie.Add(new TrieNode());
                }

                node = trie[node].Next[idx];
            }

            // If the same fragment string appears multiple times with different costs,
            // we only need the cheapest one.
            trie[node].EndCost = Math.Min(trie[node].EndCost, cost[i]);
        }

        int n = target.Length;
        long inf = long.MaxValue / 4;

        // -----------------------------
        // Step 2: Dynamic programming array.
        // -----------------------------
        // dp[i] = minimum cost to build the prefix target[0..i-1]
        //
        // In other words:
        // - dp[0] = 0 because building an empty prefix costs nothing.
        // - We want dp[n] at the end.
        //
        // If dp[i] is reachable, we try to place every fragment that matches target starting at i.
        // Suppose a fragment of length L matches there with cost C.
        // Then we can update:
        // dp[i + L] = min(dp[i + L], dp[i] + C)
        long[] dp = new long[n + 1];
        Array.Fill(dp, inf);
        dp[0] = 0;

        // -----------------------------
        // Step 3: Process each starting position in the target.
        // -----------------------------
        for (int i = 0; i < n; i++)
        {
            // If this prefix cannot be formed, there is no point exploring transitions from it.
            if (dp[i] == inf)
            {
                continue;
            }

            // Start walking the trie from the root, matching target[i], target[i+1], ...
            int node = 0;

            // We never need to walk more than maxLen characters,
            // because no fragment is longer than that.
            int limit = Math.Min(n, i + maxLen);

            for (int j = i; j < limit; j++)
            {
                int idx = Map(target[j]);
                int nextNode = trie[node].Next[idx];

                // If the trie has no edge for this character,
                // then no longer fragment can match either, so we stop immediately.
                if (nextNode == -1)
                {
                    break;
                }

                node = nextNode;

                // If this trie node marks the end of a fragment,
                // then target[i..j] is exactly one usable fragment.
                if (trie[node].EndCost != long.MaxValue)
                {
                    int nextPos = j + 1;
                    long candidate = dp[i] + trie[node].EndCost;

                    if (candidate < dp[nextPos])
                    {
                        dp[nextPos] = candidate;
                    }
                }
            }
        }

        // -----------------------------
        // Step 4: Final answer.
        // -----------------------------
        // If dp[n] is still infinity, the target cannot be reconstructed exactly.
        return dp[n] == inf ? -1 : dp[n];
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

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
string target1 = "ACGTAC";
string[] fragments1 = { "AC", "CGT", "GT", "AC" };
int[] cost1 = { 4, 5, 3, 2 };
long result1 = solution.MinimumCost(target1, fragments1, cost1);
Console.WriteLine(result1); // Expected: 7

// Example 2
string target2 = "AAGT";
string[] fragments2 = { "AA", "AG", "GT" };
int[] cost2 = { 3, 4, 2 };
long result2 = solution.MinimumCost(target2, fragments2, cost2);
Console.WriteLine(result2); // Expected: 5

// Additional impossible case
string target3 = "ACGA";
string[] fragments3 = { "AC", "G" };
int[] cost3 = { 2, 1 };
long result3 = solution.MinimumCost(target3, fragments3, cost3);
Console.WriteLine(result3); // Expected: -1