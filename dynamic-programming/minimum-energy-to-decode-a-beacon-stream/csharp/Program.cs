/*
Title: Minimum Energy to Decode a Beacon Stream
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
A remote beacon sends a message as a string s consisting only of lowercase English letters.
Your decoder reads the message from left to right and must split it into valid signal blocks.
You are given a dictionary patterns, where each pattern is a valid block that may be used any
number of times. Decoding a block has an energy cost equal to block.length * block.length.
However, if two consecutive chosen blocks start with the same letter, the second block receives
a discount of d energy units. The energy cost of a block can never go below 0 after applying
the discount.

Your task is to compute the minimum total energy required to decode the entire string exactly.
If it is impossible to split the whole string into valid patterns, return -1.

Formally, if the chosen sequence of blocks is b1, b2, ..., bk, then they must concatenate to
exactly s. The cost of b1 is len(b1)^2. For each i > 1, the cost of bi is max(0, len(bi)^2 - d)
if bi and b(i-1) start with the same character; otherwise it is len(bi)^2.

Constraints:
- 1 <= s.length <= 5000
- 1 <= patterns.length <= 2000
- 1 <= patterns[i].length <= 50
- 0 <= d <= 2500
- s and all patterns[i] contain only lowercase English letters
- The sum of lengths of all patterns does not exceed 20000

Important note about the examples:
The written explanation in the prompt contains arithmetic inconsistencies.
For example, for "ab" the cost is 2^2 = 4, so "ab" followed by "ba" should cost 4 + 4 + ...
unless a discount applies. The algorithm below follows the formal problem statement exactly:
- base cost = length^2
- discount applies only when consecutive blocks start with the same letter
- discounted cost = max(0, length^2 - d)

The implementation therefore computes the mathematically correct answer for the formal rules.
*/

using System;
using System.Collections.Generic;

class Solution
{
    private sealed class TrieNode
    {
        public TrieNode[] Next = new TrieNode[26];
        public List<int> EndLengths = new List<int>();
    }

    /*
    Time Complexity:
    Let n = s.Length, and let L be the maximum pattern length (at most 50).
    Building the trie takes O(total length of all patterns).
    For each position in s, we walk forward in the trie for at most L characters.
    For every matched pattern, we try 26 possible previous starting letters.
    So the DP work is O(n * L * 26) in the worst practical bound, which is efficient here.

    Space Complexity:
    O(n * 26 + total trie nodes)
    - dp uses (n + 1) * 26 states
    - trie size is proportional to the total length of all patterns
    */
    public int MinimumEnergy(string s, string[] patterns, int d)
    {
        int n = s.Length;
        const long INF = long.MaxValue / 4;

        // Step 1:
        // Build a trie from all patterns.
        //
        // Why use a trie?
        // We need to know, for every position i in the string, which dictionary patterns
        // start exactly at i. A trie lets us scan forward character by character from s[i]
        // and discover all matching patterns efficiently, without checking every pattern one by one.
        //
        // Since each pattern length is at most 50, this scan is naturally short.
        TrieNode root = new TrieNode();
        foreach (string pattern in patterns)
        {
            TrieNode node = root;
            foreach (char ch in pattern)
            {
                int idx = ch - 'a';
                node.Next[idx] ??= new TrieNode();
                node = node.Next[idx];
            }

            // Store the pattern length at the terminal node.
            // Multiple patterns could theoretically end at the same node if duplicates exist,
            // and duplicates do not hurt correctness.
            node.EndLengths.Add(pattern.Length);
        }

        // Step 2:
        // Define DP state.
        //
        // dp[pos, prevStart] = minimum energy needed to decode the prefix s[0..pos-1]
        //                     such that the LAST chosen block starts with letter prevStart.
        //
        // Example:
        // If dp[5, 1] = 12, that means:
        // - we have decoded exactly the first 5 characters
        // - the most recently chosen block starts with 'b' (because 'b' - 'a' = 1)
        // - the minimum total energy to do that is 12
        //
        // Why do we track the starting letter of the previous block?
        // Because the cost of the next block depends on whether its starting letter matches
        // the previous block's starting letter. That means the previous starting letter is
        // exactly the information we must remember.
        long[,] dp = new long[n + 1, 26];
        for (int i = 0; i <= n; i++)
        {
            for (int c = 0; c < 26; c++)
            {
                dp[i, c] = INF;
            }
        }

        // Step 3:
        // Handle the first block separately.
        //
        // There is no "previous block" before the first one, so no discount can apply.
        // We will start transitions from position 0 by matching every pattern that begins there.
        //
        // After choosing the first block, the state becomes:
        // dp[endPosition, startLetterOfThatBlock] = costOfThatBlock
        AddTransitionsFromStart(s, root, d, dp, INF);

        // Step 4:
        // Process all positions from left to right.
        //
        // At each position pos, if some dp[pos, prevStart] is reachable, we try to place
        // every valid pattern starting at pos. For each such next block:
        // - its starting letter is s[pos]
        // - its base cost is len^2
        // - if its starting letter equals prevStart, discount applies
        // - then we update dp[nextPos, currentStart]
        //
        // This is a classic "extend partial solutions" dynamic programming pattern.
        for (int pos = 1; pos < n; pos++)
        {
            // Small optimization:
            // If no state at this position is reachable, there is nothing to extend.
            bool reachable = false;
            for (int c = 0; c < 26; c++)
            {
                if (dp[pos, c] < INF)
                {
                    reachable = true;
                    break;
                }
            }

            if (!reachable)
            {
                continue;
            }

            TrieNode node = root;
            int currentStart = s[pos] - 'a';

            // Walk forward in the trie starting from s[pos].
            // This finds every pattern that matches exactly at this position.
            for (int j = pos; j < n && j < pos + 50; j++)
            {
                int idx = s[j] - 'a';
                node = node.Next[idx];
                if (node == null)
                {
                    // No further pattern can match from this position.
                    break;
                }

                if (node.EndLengths.Count == 0)
                {
                    continue;
                }

                // Every stored length here corresponds to a valid pattern ending at j.
                foreach (int len in node.EndLengths)
                {
                    int nextPos = pos + len;
                    long baseCost = (long)len * len;

                    // Try all possible previous starting letters.
                    // This is the exact information our DP state stores.
                    for (int prevStart = 0; prevStart < 26; prevStart++)
                    {
                        long prevCost = dp[pos, prevStart];
                        if (prevCost >= INF)
                        {
                            continue;
                        }

                        long addCost = baseCost;

                        // Apply discount only if the previous block and current block
                        // start with the same letter.
                        if (prevStart == currentStart)
                        {
                            addCost = Math.Max(0L, baseCost - d);
                        }

                        long candidate = prevCost + addCost;
                        if (candidate < dp[nextPos, currentStart])
                        {
                            dp[nextPos, currentStart] = candidate;
                        }
                    }
                }
            }
        }

        // Step 5:
        // The answer is the best reachable state at position n,
        // regardless of what the last block's starting letter was.
        long answer = INF;
        for (int c = 0; c < 26; c++)
        {
            answer = Math.Min(answer, dp[n, c]);
        }

        return answer >= INF ? -1 : (int)answer;
    }

    private static void AddTransitionsFromStart(string s, TrieNode root, int d, long[,] dp, long INF)
    {
        int n = s.Length;
        TrieNode node = root;

        // We match every pattern that starts at position 0.
        // Since this is the first block, its cost is simply len^2 with no discount.
        for (int j = 0; j < n && j < 50; j++)
        {
            int idx = s[j] - 'a';
            node = node.Next[idx];
            if (node == null)
            {
                break;
            }

            if (node.EndLengths.Count == 0)
            {
                continue;
            }

            int startLetter = s[0] - 'a';
            foreach (int len in node.EndLengths)
            {
                long cost = (long)len * len;
                if (cost < dp[len, startLetter])
                {
                    dp[len, startLetter] = cost;
                }
            }
        }
    }
}

// Demo code
var solution = new Solution();

string s1 = "ababa";
string[] patterns1 = { "a", "ab", "ba" };
int d1 = 2;
int result1 = solution.MinimumEnergy(s1, patterns1, d1);
Console.WriteLine($"Input: s = \"{s1}\", patterns = [\"a\", \"ab\", \"ba\"], d = {d1}");
Console.WriteLine($"Minimum energy = {result1}");

string s2 = "cable";
string[] patterns2 = { "ca", "ble", "cab" };
int d2 = 3;
int result2 = solution.MinimumEnergy(s2, patterns2, d2);
Console.WriteLine($"Input: s = \"{s2}\", patterns = [\"ca\", \"ble\", \"cab\"], d = {d2}");
Console.WriteLine($"Minimum energy = {result2}");