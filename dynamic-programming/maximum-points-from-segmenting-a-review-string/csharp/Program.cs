/*
Title: Maximum Points from Segmenting a Review String

Problem Description:
You are building a text scoring system for customer reviews. A review is represented as a lowercase string s.
You are also given a dictionary of approved phrases, where each phrase has an integer score.

Your task is to split the entire string into a sequence of non-overlapping dictionary phrases so that every
character in s belongs to exactly one chosen phrase. Among all valid segmentations, return the maximum total score.

If it is impossible to segment the full string using only the given phrases, return -1.

Each phrase may be used any number of times as long as it matches a substring exactly. Scores may be positive,
zero, or negative, so a valid solution is not always the one with the fewest segments. This means a greedy choice
is not sufficient; you must consider future decisions when choosing a phrase at a position.

Return only the maximum score, not the actual segmentation.

Constraints:
- 1 <= s.length <= 5000
- 1 <= phrases.length <= 2000
- Sum of lengths of all phrases <= 20000
- 1 <= phrase.length <= 50
- -10^4 <= score <= 10^4
- s and all phrases consist only of lowercase English letters
- Phrase strings in the input are unique
*/

using System;
using System.Collections.Generic;

public class Solution
{
    private class TrieNode
    {
        public TrieNode[] Next = new TrieNode[26];
        public bool IsWord;
        public int Score;
    }

    /*
    Time Complexity:
    - Building the trie takes O(total length of all phrases).
    - The dynamic programming loop checks, from each index i in s, at most 50 characters forward
      because each phrase length is at most 50.
    - Therefore the DP work is O(n * 50), where n = s.Length.
    - Total: O(totalPhraseLength + n * 50), which is efficient for the given constraints.

    Space Complexity:
    - Trie storage: O(total length of all phrases)
    - DP array: O(n)
    - Total: O(totalPhraseLength + n)
    */
    public int MaxScore(string s, IList<(string phrase, int score)> phrases)
    {
        // Step 1:
        // Build a trie (prefix tree) from all phrases.
        //
        // Why use a trie?
        // If we tried every phrase at every position, we would repeatedly compare many strings.
        // A trie lets us walk through the review string character by character and discover
        // all phrases that start at a given index in one pass.
        //
        // This is especially useful because:
        // - phrase lengths are small (<= 50)
        // - there can be many phrases
        // - we need to test many starting positions in the string
        var root = new TrieNode();

        foreach (var (phrase, score) in phrases)
        {
            var node = root;

            // Insert the phrase into the trie one character at a time.
            foreach (char ch in phrase)
            {
                int idx = ch - 'a';

                if (node.Next[idx] == null)
                {
                    node.Next[idx] = new TrieNode();
                }

                node = node.Next[idx];
            }

            // Mark the final node as a complete phrase and store its score.
            // The problem states phrase strings are unique, so we do not need to handle duplicates.
            node.IsWord = true;
            node.Score = score;
        }

        int n = s.Length;

        // Step 2:
        // Create a DP array where:
        // dp[i] = maximum score obtainable by segmenting the suffix s[i..n-1]
        //
        // Example:
        // If s = "applepieapple", then:
        // dp[0] is the answer for the whole string,
        // dp[5] is the best score for "pieapple",
        // dp[n] means "empty suffix".
        //
        // We use a very negative sentinel value to mean "impossible".
        // We cannot use -1 as the internal impossible marker because valid scores may be negative.
        const int NEG_INF = int.MinValue / 4;
        int[] dp = new int[n + 1];

        for (int i = 0; i <= n; i++)
        {
            dp[i] = NEG_INF;
        }

        // Base case:
        // The empty suffix can always be segmented with score 0:
        // choose nothing.
        dp[n] = 0;

        // Step 3:
        // Fill the DP table from right to left.
        //
        // Why right to left?
        // Because dp[i] depends on dp[j + 1] for later positions.
        // So when computing dp[i], we want all future answers already known.
        for (int i = n - 1; i >= 0; i--)
        {
            TrieNode? node = root;

            // Starting from position i, walk forward in the string and simultaneously
            // walk down the trie.
            //
            // Every time we reach a trie node that marks a complete phrase,
            // we have found a valid phrase s[i..j].
            //
            // Then we can transition:
            // dp[i] = max(dp[i], score_of_phrase + dp[j + 1])
            //
            // But only if dp[j + 1] is possible.
            for (int j = i; j < n && j < i + 50; j++)
            {
                int idx = s[j] - 'a';

                // If the trie has no child for this character,
                // then no phrase can continue from here.
                // That means we can stop early.
                node = node?.Next[idx];
                if (node == null)
                {
                    break;
                }

                // If this trie node marks the end of a phrase,
                // then s[i..j] is a dictionary phrase.
                if (node.IsWord)
                {
                    // We can only use this phrase if the remaining suffix s[j+1..]
                    // can also be fully segmented.
                    if (dp[j + 1] != NEG_INF)
                    {
                        int candidate = node.Score + dp[j + 1];

                        // Keep the best score among all valid phrase choices starting at i.
                        if (candidate > dp[i])
                        {
                            dp[i] = candidate;
                        }
                    }
                }
            }
        }

        // Step 4:
        // If dp[0] is still impossible, then the whole string cannot be segmented.
        // Return -1 as required by the problem.
        return dp[0] == NEG_INF ? -1 : dp[0];
    }
}

// Demo code

var solution = new Solution();

// Example 1
string s1 = "applepieapple";
var phrases1 = new List<(string phrase, int score)>
{
    ("apple", 5),
    ("pie", 3),
    ("app", 2),
    ("lepie", 4)
};

int result1 = solution.MaxScore(s1, phrases1);
Console.WriteLine(result1); // Expected: 13

// Example 2
string s2 = "catsandog";
var phrases2 = new List<(string phrase, int score)>
{
    ("cat", 4),
    ("cats", 7),
    ("and", 3),
    ("sand", 5),
    ("dog", 6)
};

int result2 = solution.MaxScore(s2, phrases2);
Console.WriteLine(result2); // Expected: -1

// Additional demo showing that negative scores are handled correctly.
// The string must be fully segmented, even if some chosen phrases have negative values.
string s3 = "aaaa";
var phrases3 = new List<(string phrase, int score)>
{
    ("a", -2),
    ("aa", 3),
    ("aaa", 1)
};

int result3 = solution.MaxScore(s3, phrases3);
Console.WriteLine(result3); // One best segmentation is "aa" + "aa" => 6