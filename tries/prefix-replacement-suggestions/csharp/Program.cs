/*
Title: Prefix Replacement Suggestions
Difficulty: Medium
Topic: Tries

Problem Description:
You are building a text normalization tool for a search system. A dictionary of approved root words is given, along with a list of query words typed by users. For each query word, you must find the shortest dictionary root that is a prefix of that query. If such a root exists, replace the query word with that root; otherwise keep the original word unchanged. In addition to returning the transformed list, you must also report how many query words were actually replaced.

Your task is to implement a function that takes two arrays: roots and queries. Each string contains only lowercase English letters. For every word in queries, search the dictionary for a prefix match. If multiple roots match, always choose the shortest one. This requirement makes a Trie-based solution especially suitable, because it allows efficient prefix traversal and early stopping when a terminal root is found.

Return both the transformed array and the number of replacements performed.

Constraints:
- 1 <= roots.length, queries.length <= 2 * 10^4
- 1 <= roots[i].length, queries[i].length <= 100
- All strings consist only of lowercase English letters
- The total number of characters across all strings does not exceed 2 * 10^5
- Roots may contain duplicates; duplicates should not affect the result

Example 1:
Input:
roots = ["cat", "bat", "rat"]
queries = ["cattle", "battery", "rattle", "dog"]
Output:
transformed = ["cat", "bat", "rat", "dog"]
replacedCount = 3

Example 2:
Input:
roots = ["a", "ab", "abc", "bcd"]
queries = ["abcde", "abacus", "bcdx", "zzz"]
Output:
transformed = ["a", "a", "bcd", "zzz"]
replacedCount = 3
*/

using System;
using System.Collections.Generic;

public class Solution
{
    // A Trie node represents one character position in a prefix tree.
    // Because the problem states that all letters are lowercase English letters,
    // we can store exactly 26 possible children for each node.
    private sealed class TrieNode
    {
        public TrieNode[] Children = new TrieNode[26];

        // IsWord tells us whether a complete root word ends at this node.
        // This is extremely important because the problem asks for the SHORTEST
        // root that matches a prefix of the query. While traversing the query,
        // the first node we encounter with IsWord = true gives us the shortest match.
        public bool IsWord;
    }

    private readonly TrieNode _root = new();

    // Time Complexity:
    // Building the trie: O(total characters in roots)
    // Processing queries: O(total characters in queries)
    // Overall: O(sum of lengths of all roots + sum of lengths of all queries)
    //
    // Space Complexity:
    // O(total characters in roots) for the trie
    // O(total characters in queries) for the output array
    public (string[] transformed, int replacedCount) ReplaceWithShortestRoots(string[] roots, string[] queries)
    {
        // STEP 1: Insert every root into the Trie.
        //
        // Why this is necessary:
        // A Trie is designed for prefix searching. Instead of comparing each query
        // against every root one by one, we organize all roots by shared prefixes.
        // This allows us to walk through each query character-by-character and stop
        // as soon as we know the shortest matching root.
        //
        // Duplicates do not affect correctness:
        // If the same root appears multiple times, inserting it again simply marks
        // the same terminal node as a word again. That changes nothing.
        foreach (var root in roots)
        {
            Insert(root);
        }

        // STEP 2: Prepare the result array and replacement counter.
        //
        // transformed[i] will store either:
        // - the shortest matching root, if one exists
        // - or the original query word, if no root matches
        //
        // replacedCount tracks how many query words were actually changed.
        var transformed = new string[queries.Length];
        int replacedCount = 0;

        // STEP 3: Process each query independently.
        //
        // For each query, we search the Trie from the beginning of the word.
        // The first terminal Trie node we encounter corresponds to the shortest
        // root prefix, which is exactly what the problem asks us to use.
        for (int i = 0; i < queries.Length; i++)
        {
            string query = queries[i];

            // Try to find the shortest root that is a prefix of this query.
            string? replacement = FindShortestPrefixRoot(query);

            // If replacement is not null, we found a valid root prefix.
            // We store that root and increase the replacement count.
            if (replacement is not null)
            {
                transformed[i] = replacement;
                replacedCount++;
            }
            else
            {
                // Otherwise, no root matched this query, so we keep the original word.
                transformed[i] = query;
            }
        }

        // STEP 4: Return both required outputs together.
        return (transformed, replacedCount);
    }

    private void Insert(string word)
    {
        // Start at the Trie root before processing any characters.
        TrieNode current = _root;

        // Walk through each character of the root word.
        for (int i = 0; i < word.Length; i++)
        {
            int index = word[i] - 'a';

            // If the path for this character does not exist yet,
            // create a new Trie node.
            if (current.Children[index] is null)
            {
                current.Children[index] = new TrieNode();
            }

            // Move to the child node for the current character.
            current = current.Children[index]!;
        }

        // After processing all characters, mark this node as the end of a root word.
        current.IsWord = true;
    }

    private string? FindShortestPrefixRoot(string query)
    {
        // Start from the Trie root.
        TrieNode current = _root;

        // We use a character buffer to build the prefix as we traverse.
        // This lets us return the matching root immediately when we hit a terminal node.
        var prefixChars = new char[query.Length];

        // Traverse the query one character at a time.
        for (int i = 0; i < query.Length; i++)
        {
            int index = query[i] - 'a';

            // If there is no child for this character, then the query's current prefix
            // does not exist in the Trie. That means no root can be a prefix of this query.
            if (current.Children[index] is null)
            {
                return null;
            }

            // Move to the next Trie node that matches this character.
            current = current.Children[index]!;

            // Record this character in our prefix buffer.
            prefixChars[i] = query[i];

            // This is the key step for correctness:
            // The moment we reach a node marked as a complete root word,
            // we return immediately.
            //
            // Why immediate return is correct:
            // We are scanning the query from left to right.
            // Therefore, the first complete root we encounter is the shortest root
            // that matches the query prefix.
            //
            // Example:
            // roots = ["a", "ab", "abc"], query = "abacus"
            // - after reading 'a', IsWord is true, so we return "a"
            // - we do NOT continue to "ab" or "abc", because the problem wants
            //   the shortest matching root, not the longest.
            if (current.IsWord)
            {
                return new string(prefixChars, 0, i + 1);
            }
        }

        // If we consumed the entire query without hitting a terminal root node,
        // then no root is a prefix of the query.
        return null;
    }
}

// -------------------------
// Demo code
// -------------------------

var solution = new Solution();

// Example 1
string[] roots1 = ["cat", "bat", "rat"];
string[] queries1 = ["cattle", "battery", "rattle", "dog"];

var result1 = solution.ReplaceWithShortestRoots(roots1, queries1);

Console.WriteLine("Example 1");
Console.WriteLine("Transformed: [" + string.Join(", ", result1.transformed) + "]");
Console.WriteLine("Replaced Count: " + result1.replacedCount);
Console.WriteLine();

// Expected:
// ["cat", "bat", "rat", "dog"]
// 3

// Example 2
string[] roots2 = ["a", "ab", "abc", "bcd"];
string[] queries2 = ["abcde", "abacus", "bcdx", "zzz"];

var result2 = solution.ReplaceWithShortestRoots(roots2, queries2);

Console.WriteLine("Example 2");
Console.WriteLine("Transformed: [" + string.Join(", ", result2.transformed) + "]");
Console.WriteLine("Replaced Count: " + result2.replacedCount);
Console.WriteLine();

// Expected:
// ["a", "a", "bcd", "zzz"]
// 3

// Additional small demo showing duplicates do not affect the result.
string[] roots3 = ["cat", "cat", "c", "car"];
string[] queries3 = ["cattle", "carpet", "dog"];

var result3 = solution.ReplaceWithShortestRoots(roots3, queries3);

Console.WriteLine("Example 3");
Console.WriteLine("Transformed: [" + string.Join(", ", result3.transformed) + "]");
Console.WriteLine("Replaced Count: " + result3.replacedCount);