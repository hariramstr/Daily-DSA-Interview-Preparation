/*
 * Title: Wildcard Query Frequency in Log Stream
 * Difficulty: Hard
 * Topic: Tries
 *
 * Problem Description:
 * You are given a stream of log entries, where each log entry is a lowercase alphabetic string.
 * You need to build a data structure that supports two operations efficiently:
 *
 * 1. insert(word): Insert a word into the log stream.
 * 2. query(pattern): Given a pattern string consisting of lowercase letters and the wildcard
 *    character '?', return the total number of previously inserted words that match the pattern.
 *    A '?' can match exactly one lowercase letter. The pattern must match the entire word.
 *
 * Additionally, after each query, return the lexicographically smallest word that matched,
 * or empty string if no word matched.
 *
 * Returns: [count, index] where count = number of matching words,
 *          index = 0-based insertion index of lexicographically smallest matching word, or -1.
 *
 * Constraints:
 * - 1 <= word.length, pattern.length <= 20
 * - All characters in word are lowercase English letters.
 * - pattern consists of lowercase English letters and '?'.
 * - At most 5 * 10^4 calls to insert and query combined.
 * - Words inserted are not necessarily unique.
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Trie Node Definition
// ─────────────────────────────────────────────────────────────────────────────

/// <summary>
/// Represents a single node in our Trie.
/// Each node stores:
///   - children[26]: links to child nodes for each letter 'a'-'z'
///   - insertionIndices: list of insertion indices for every word that ends here
///   - minLexWord: the lexicographically smallest word that ends at this node
///   - minLexIndex: the insertion index of that smallest word
/// </summary>
class TrieNode
{
    // 26 children, one per lowercase letter
    public TrieNode[] Children { get; } = new TrieNode[26];

    // Every time a word ends at this node we record its insertion index.
    // This lets us count duplicates correctly (same word inserted twice = two entries).
    public List<int> InsertionIndices { get; } = new List<int>();

    // Track the lexicographically smallest word that terminates here,
    // along with the insertion index of its FIRST occurrence.
    public string MinLexWord { get; set; } = null;
    public int MinLexIndex { get; set; } = -1;
}

// ─────────────────────────────────────────────────────────────────────────────
// LogStream class
// ─────────────────────────────────────────────────────────────────────────────

/// <summary>
/// LogStream uses a Trie to store inserted words.
/// For queries with '?' wildcards we perform a depth-first search (DFS) over
/// the Trie, branching on all 26 children whenever we encounter a '?'.
///
/// Time Complexity:
///   insert  : O(L)          where L = word length (≤ 20)
///   query   : O(26^W * L)   worst case when pattern is all '?', W = pattern length.
///             In practice much faster because the Trie prunes dead branches.
///             With L ≤ 20 and 5×10^4 operations this is acceptable.
///
/// Space Complexity:
///   O(N * L) for the Trie, where N = number of inserted words.
/// </summary>
class LogStream
{
    // ── Fields ────────────────────────────────────────────────────────────────

    // Root of the Trie (does not represent any character itself)
    private readonly TrieNode _root;

    // Global insertion counter — incremented on every insert call
    private int _insertionCounter;

    // ── Constructor ───────────────────────────────────────────────────────────
    public LogStream()
    {
        _root = new TrieNode();
        _insertionCounter = 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INSERT
    // Time: O(L)  Space: O(L) new nodes in worst case
    // ─────────────────────────────────────────────────────────────────────────

    /// <summary>
    /// Inserts a word into the Trie and records its insertion index.
    /// </summary>
    public void Insert(string word)
    {
        // Step 1: Capture the current insertion index for this word,
        //         then advance the counter for the next insert.
        int idx = _insertionCounter++;

        // Step 2: Walk down the Trie, creating nodes as needed.
        TrieNode current = _root;

        for (int i = 0; i < word.Length; i++)
        {
            // Convert character to 0-based index (e.g. 'a'→0, 'b'→1, …)
            int c = word[i] - 'a';

            // Step 3: If the child for this character doesn't exist yet, create it.
            //         This is the standard Trie insert operation.
            if (current.Children[c] == null)
                current.Children[c] = new TrieNode();

            // Step 4: Move to the child node.
            current = current.Children[c];
        }

        // Step 5: We have reached the terminal node for this word.
        //         Record the insertion index so query can count it.
        current.InsertionIndices.Add(idx);

        // Step 6: Update the lexicographically smallest word at this terminal node.
        //         We compare the newly inserted word against whatever was stored before.
        //         If this is the first word ending here, or if the new word is
        //         lexicographically smaller, update both the word and its index.
        //         NOTE: if the new word equals the stored word we keep the earlier
        //         insertion index (the one already stored), because the problem asks
        //         for the index of the first (earliest) occurrence of the lex-smallest word.
        if (current.MinLexWord == null ||
            string.Compare(word, current.MinLexWord, StringComparison.Ordinal) < 0)
        {
            current.MinLexWord = word;
            current.MinLexIndex = idx;
        }
        // If word == MinLexWord the existing MinLexIndex is already the earlier one — no update needed.
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QUERY
    // Time: O(26^W * L) worst case, W = pattern length ≤ 20
    // Space: O(W) recursion stack depth
    // ─────────────────────────────────────────────────────────────────────────

    /// <summary>
    /// Returns [count, index] where:
    ///   count = total number of inserted words (with duplicates) matching the pattern.
    ///   index = insertion index of the lexicographically smallest matching word, or -1.
    /// </summary>
    public int[] Query(string pattern)
    {
        // We'll accumulate results in these two variables during DFS.
        int totalCount = 0;
        string bestWord = null;   // lex-smallest matching word found so far
        int bestIndex = -1;       // insertion index of bestWord

        // Step 1: Launch a depth-first search from the root.
        //         We pass depth=0 (position in the pattern) and the root node.
        DFS(_root, pattern, 0, ref totalCount, ref bestWord, ref bestIndex);

        // Step 2: Return the aggregated results.
        return new int[] { totalCount, bestIndex };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DFS helper
    // ─────────────────────────────────────────────────────────────────────────

    /// <summary>
    /// Recursively traverses the Trie matching the pattern character by character.
    ///
    /// Parameters:
    ///   node    – current Trie node
    ///   pattern – the query pattern
    ///   depth   – current position in the pattern (0-based)
    ///   totalCount – running count of matched words (passed by ref)
    ///   bestWord   – lex-smallest matched word so far (passed by ref)
    ///   bestIndex  – insertion index of bestWord (passed by ref)
    /// </summary>
    private void DFS(TrieNode node, string pattern, int depth,
                     ref int totalCount, ref string bestWord, ref int bestIndex)
    {
        // ── Base case ─────────────────────────────────────────────────────────
        // Step A: We have consumed all characters of the pattern.
        //         Check whether the current node is a terminal node
        //         (i.e., at least one inserted word ends exactly here).
        if (depth == pattern.Length)
        {
            // If InsertionIndices is non-empty, words end here — count them all.
            if (node.InsertionIndices.Count > 0)
            {
                // Add every occurrence (handles duplicate insertions).
                totalCount += node.InsertionIndices.Count;

                // Step B: Update the global best (lex-smallest) word.
                //         node.MinLexWord is the lex-smallest word ending at this node.
                //         Compare it against the best we've found across all terminal nodes.
                if (bestWord == null ||
                    string.Compare(node.MinLexWord, bestWord, StringComparison.Ordinal) < 0)
                {
                    bestWord = node.MinLexWord;
                    bestIndex = node.MinLexIndex;
                }
                else if (node.MinLexWord == bestWord && node.MinLexIndex < bestIndex)
                {
                    // Same lex-smallest word but an earlier insertion — prefer earlier index.
                    bestIndex = node.MinLexIndex;
                }
            }
            // Either way, stop recursing — we've matched the full pattern length.
            return;
        }

        // ── Recursive case ────────────────────────────────────────────────────
        char ch = pattern[depth];

        if (ch == '?')
        {
            // Step C: Wildcard — try all 26 possible children.
            //         Each child represents one possible letter at this position.
            for (int c = 0; c < 26; c++)
            {
                if (node.Children[c] != null)
                {
                    // Recurse into this child, advancing depth by 1.
                    DFS(node.Children[c], pattern, depth + 1,
                        ref totalCount, ref bestWord, ref bestIndex);
                }
            }
        }
        else
        {
            // Step D: Literal character — follow exactly one child.
            int c = ch - 'a';
            if (node.Children[c] != null)
            {
                DFS(node.Children[c], pattern, depth + 1,
                    ref totalCount, ref bestWord, ref bestIndex);
            }
            // If the child doesn't exist, this branch is a dead end — do nothing.
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code  (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

Console.WriteLine("═══════════════════════════════════════════════════════");
Console.WriteLine("  Wildcard Query Frequency in Log Stream — Demo");
Console.WriteLine("═══════════════════════════════════════════════════════\n");

// ── Example 1 ────────────────────────────────────────────────────────────────
// Expected output:
//   insert("apple")  → null
//   insert("apply")  → null
//   insert("apt")    → null
//   query("ap?le")   → [2, 0]   ("apple" idx 0, "apply" idx 1 → count=2, smallest="apple" idx 0)
//   query("a??")     → [1, 2]   ("apt" idx 2 → count=1, smallest="apt" idx 2)

Console.WriteLine("── Example 1 ──────────────────────────────────────────");
LogStream ls1 = new LogStream();

ls1.Insert("apple");   // index 0
ls1.Insert("apply");   // index 1
ls1.Insert("apt");     // index 2

int[] r1 = ls1.Query("ap?le");
Console.WriteLine($"query(\"ap?le\") → [{r1[0]}, {r1[1]}]   (expected [2, 0])");

int[] r2 = ls1.Query("a??");
Console.WriteLine($"query(\"a??\")   → [{r2[0]}, {r2[1]}]   (expected [1, 2])");

Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// Expected output:
//   insert("bat")  → null
//   insert("bad")  → null
//   query("b?d")   → [1, 1]   ("bad" idx 1 → count=1, smallest="bad" idx 1)
//   insert("bed")  → null
//   query("b?d")   → [2, 1]   ("bad" idx 1, "bed" idx 3 → count=2, smallest="bad" idx 1)

Console.WriteLine("── Example 2 ──────────────────────────────────────────");
LogStream ls2 = new LogStream();

ls2.Insert("bat");   // index 0
ls2.Insert("bad");   // index 1

int[] r3 = ls2.Query("b?d");
Console.WriteLine($"query(\"b?d\") → [{r3[0]}, {r3[1]}]   (expected [1, 1])");

ls2.Insert("bed");   // index 2  (note: counter continues from 2, not 3)

int[] r4 = ls2.Query("b?d");
Console.WriteLine($"query(\"b?d\") → [{r4[0]}, {r4[1]}]   (expected [2, 1])");

Console.WriteLine();

// ── Extra edge-case tests ─────────────────────────────────────────────────────
Console.WriteLine("── Extra Tests ────────────────────────────────────────");

LogStream ls3 = new LogStream();

// Duplicate insertions: same word inserted twice
ls3.Insert("hello");  // index 0
ls3.Insert("hello");  // index 1
ls3.Insert("world");  // index 2

int[] r5 = ls3.Query("hello");
Console.WriteLine($"query(\"hello\") → [{r5[0]}, {r5[1]}]   (expected [2, 0])  // two 'hello' entries");

int[] r6 = ls3.Query("?????");
// Matches "hello"(×2) and "world"(×1) → count=3, lex-smallest="hello" at idx 0
Console.WriteLine($"query(\"?????\") → [{r6[0]}, {r6[1]}]   (expected [3, 0])  // all 5-letter words");

int[] r7 = ls3.Query("w????");
// Matches "world" → count=1, idx=2
Console.WriteLine($"query(\"w????\") → [{r7[0]}, {r7[1]}]   (expected [1, 2])");

int[] r8 = ls3.Query("xyz");
// No match → count=0, idx=-1
Console.WriteLine($"query(\"xyz\")   → [{r8[0]}, {r8[1]}]   (expected [0, -1])");

Console.WriteLine();

// Pattern length mismatch: "hel" should not match "hello"
int[] r9 = ls3.Query("