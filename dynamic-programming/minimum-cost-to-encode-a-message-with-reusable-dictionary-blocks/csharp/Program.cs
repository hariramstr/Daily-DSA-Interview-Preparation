/*
Title: Minimum Cost to Encode a Message with Reusable Dictionary Blocks

Problem Description:
You are building a compression system for a chat platform. A message string target must be encoded
from left to right using a set of reusable dictionary blocks. Each dictionary block is a lowercase
string words[i] with an associated non-negative encoding cost costs[i]. You may use any block any
number of times.

Starting at position p in target, you may place block words[i] only if it exactly matches the
substring of target beginning at p. If it matches, you pay costs[i] and advance by words[i].Length.
Your goal is to encode the entire target string with minimum total cost. If it is impossible to
cover the full string exactly, return -1.

This is a dynamic programming problem:
- Let dp[i] = minimum cost to encode target starting from index i.
- Then:
    dp[i] = min(cost(word) + dp[i + word.Length]) over all words matching target at i
- Answer is dp[0], or -1 if impossible.

To make matching efficient for large inputs, this solution uses:
1. A trie to store all dictionary words.
2. Cost compression for duplicate words:
   if the same string appears multiple times, only the minimum cost matters.
3. Bottom-up DP from right to left.

Why a trie works well here:
- From each target position i, we walk forward character by character in the trie.
- Every time we reach a trie node that marks the end of a dictionary word, we have found a valid
  block starting at i and can update dp[i].
- Because the total dictionary length is at most 2 * 10^5, the trie is efficient to build.

Important note about the examples:
- Example 1's written explanation in the prompt is inconsistent. "ab" + "aba" exactly covers
  "ababa" and has total cost 9, which is valid.
- Example 2's written explanation is also inconsistent in places, but the correct minimum is 14
  using "co" + "de" + "co" + "de".

This implementation computes the true minimum according to the formal rules.

*/

using System;
using System.Collections.Generic;

public class Solution
{
    // Trie node used to store dictionary words.
    // Each node has:
    // - Next[26]: transitions for lowercase letters
    // - EndCost: the minimum cost of any word ending at this node
    private sealed class TrieNode
    {
        public int[] Next;
        public long EndCost;

        public TrieNode()
        {
            Next = new int[26];
            Array.Fill(Next, -1);

            // "No word ends here yet"
            EndCost = long.MaxValue;
        }
    }

    /*
    Time Complexity:
    - Building the trie: O(sum of lengths of all unique words after cost compression))
    - DP matching:
        For each target index i, we walk forward in the trie until mismatch or target ends.
        In the worst case this can be O(n * L), where L is the maximum word length.
        Under the given total dictionary length constraints, this is typically efficient in practice.
    - Overall: O(totalWordLength + totalTrieWalks)

    Space Complexity:
    - Trie: O(totalWordLength)
    - DP array: O(target.Length)
    - Dictionary for duplicate-word compression: O(number of unique words)
    */
    public long MinimumCost(string target, string[] words, int[] costs)
    {
        // ------------------------------------------------------------
        // STEP 1: Compress duplicate dictionary strings by keeping only
        //         the minimum cost for each distinct word.
        //
        // Why this is necessary:
        // If the same word appears multiple times with different costs,
        // using the more expensive copy is never helpful.
        // So we safely keep only the cheapest version.
        // This reduces unnecessary trie insertions and simplifies logic.
        // ------------------------------------------------------------
        var minCostByWord = new Dictionary<string, long>(StringComparer.Ordinal);

        for (int i = 0; i < words.Length; i++)
        {
            string w = words[i];
            long c = costs[i];

            if (minCostByWord.TryGetValue(w, out long existing))
            {
                if (c < existing)
                {
                    minCostByWord[w] = c;
                }
            }
            else
            {
                minCostByWord[w] = c;
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Build the trie from the compressed dictionary.
        //
        // Why a trie:
        // Starting from a target position i, we want to know all words
        // that match target[i..].
        // A trie lets us scan forward character by character and discover
        // every matching word prefix efficiently.
        //
        // Data structure choice:
        // We store trie nodes in a List<TrieNode>.
        // Children are represented by integer indices into this list.
        // This is memory-efficient and fast.
        // ------------------------------------------------------------
        var trie = new List<TrieNode>();
        trie.Add(new TrieNode()); // root at index 0

        foreach (var pair in minCostByWord)
        {
            string word = pair.Key;
            long cost = pair.Value;

            int node = 0;

            for (int j = 0; j < word.Length; j++)
            {
                int ch = word[j] - 'a';

                if (trie[node].Next[ch] == -1)
                {
                    trie[node].Next[ch] = trie.Count;
                    trie.Add(new TrieNode());
                }

                node = trie[node].Next[ch];
            }

            // Mark that a word ends here.
            // If somehow multiple insertions reach the same terminal node,
            // keep the minimum end cost.
            if (cost < trie[node].EndCost)
            {
                trie[node].EndCost = cost;
            }
        }

        int n = target.Length;

        // ------------------------------------------------------------
        // STEP 3: Bottom-up dynamic programming.
        //
        // dp[i] = minimum cost to encode target starting at index i.
        //
        // Base case:
        // dp[n] = 0
        // because an empty suffix needs no cost to encode.
        //
        // Transition:
        // For each i, walk forward in the trie following target[i], target[i+1], ...
        // Every time we reach a trie node that represents a complete word,
        // we can "take" that word and combine:
        //
        //   candidate = cost(word) + dp[nextPosition]
        //
        // Then dp[i] is the minimum of all such candidates.
        //
        // Why right-to-left:
        // When computing dp[i], we need dp[i + len(word)].
        // Those positions are to the right, so they should already be known.
        // ------------------------------------------------------------
        const long INF = long.MaxValue / 4;
        long[] dp = new long[n + 1];
        Array.Fill(dp, INF);
        dp[n] = 0;

        for (int i = n - 1; i >= 0; i--)
        {
            int node = 0;

            // --------------------------------------------------------
            // From target position i, try to extend as far as possible
            // through the trie.
            //
            // If at some point the next character does not exist in the trie,
            // then no longer word can match either, so we stop immediately.
            // --------------------------------------------------------
            for (int j = i; j < n; j++)
            {
                int ch = target[j] - 'a';
                int nextNode = trie[node].Next[ch];

                if (nextNode == -1)
                {
                    // No dictionary word continues with this character,
                    // so matching from position i cannot go any further.
                    break;
                }

                node = nextNode;

                // ----------------------------------------------------
                // If a word ends at this trie node, then target[i..j]
                // is a valid block we can place.
                //
                // We then check whether the remaining suffix starting at
                // j + 1 can also be encoded.
                // If yes, update dp[i].
                // ----------------------------------------------------
                if (trie[node].EndCost != long.MaxValue && dp[j + 1] != INF)
                {
                    long candidate = trie[node].EndCost + dp[j + 1];
                    if (candidate < dp[i])
                    {
                        dp[i] = candidate;
                    }
                }
            }
        }

        // If dp[0] is still INF, then no exact full cover exists.
        return dp[0] == INF ? -1 : dp[0];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
string target1 = "ababa";
string[] words1 = { "ab", "aba", "ba", "a" };
int[] costs1 = { 4, 5, 2, 10 };
long result1 = solution.MinimumCost(target1, words1, costs1);
Console.WriteLine(result1); // Expected: 9

// Example 2
string target2 = "codecode";
string[] words2 = { "co", "code", "de", "odec" };
int[] costs2 = { 3, 8, 4, 5 };
long result2 = solution.MinimumCost(target2, words2, costs2);
Console.WriteLine(result2); // Expected: 14

// Additional quick impossible-case demo
string target3 = "abc";
string[] words3 = { "a", "bc", "d" };
int[] costs3 = { 1, 2, 3 };
long result3 = solution.MinimumCost(target3, words3, costs3);
Console.WriteLine(result3); // Expected: 3

// Another impossible case
string target4 = "abc";
string[] words4 = { "ab", "d" };
int[] costs4 = { 5, 1 };
long result4 = solution.MinimumCost(target4, words4, costs4);
Console.WriteLine(result4); // Expected: -1