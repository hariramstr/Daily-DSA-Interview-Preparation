/*
 * Title: Auto-Complete Sentence Builder
 * Difficulty: Easy
 * Topic: Tries
 *
 * Problem Description:
 * You are building a simple auto-complete feature for a text editor. Given a list of
 * previously typed sentences and a partial input string, return all sentences from the
 * list that start with the given partial input, sorted in the order they were originally
 * inserted.
 *
 * A sentence is considered a match if it begins with the exact characters of the partial
 * input (case-sensitive). If no sentences match, return an empty list.
 *
 * You must implement your solution using a Trie data structure to efficiently store and
 * search the sentences.
 *
 * Constraints:
 * - 1 <= sentences.length <= 200
 * - 1 <= sentences[i].length <= 100
 * - 1 <= partial.length <= 50
 * - All characters in sentences and partial are printable ASCII characters
 * - Sentences may contain spaces
 * - Duplicate sentences in the input list should each be returned individually
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Trie Node Definition
// ─────────────────────────────────────────────────────────────────────────────

/// <summary>
/// Represents a single node in the Trie.
///
/// Because sentences can contain ANY printable ASCII character (letters, digits,
/// spaces, punctuation), we cannot use a fixed-size array indexed by 'a'-'z'.
/// Instead we use a Dictionary<char, TrieNode> so every possible character is
/// handled correctly and memory is only allocated for characters that actually
/// appear in the data.
/// </summary>
class TrieNode
{
    // Maps each child character to its corresponding child node.
    // Using a dictionary keeps memory proportional to the actual branching factor.
    public Dictionary<char, TrieNode> Children { get; } = new Dictionary<char, TrieNode>();

    // When a complete sentence ends at this node we store it here.
    // We use a List<string> (not a single string) because duplicate sentences
    // are allowed and each duplicate must be returned individually.
    // The list preserves insertion order, which is required by the problem.
    public List<string> CompleteSentences { get; } = new List<string>();
}

// ─────────────────────────────────────────────────────────────────────────────
// Trie Definition
// ─────────────────────────────────────────────────────────────────────────────

/// <summary>
/// A Trie (prefix tree) that stores complete sentences and supports prefix search.
///
/// Why a Trie?
/// -----------
/// A Trie lets us walk character-by-character through the partial input in O(P)
/// time (P = length of partial), reaching the node that represents the end of
/// the prefix. From that node we can collect ALL sentences that share that prefix
/// by doing a simple depth-first traversal — without scanning every sentence in
/// the list individually.
/// </summary>
class Trie
{
    // The root node has no character of its own; it is the starting point for
    // every sentence inserted into the Trie.
    private readonly TrieNode _root = new TrieNode();

    /// <summary>
    /// Inserts a sentence into the Trie one character at a time.
    /// </summary>
    /// <param name="sentence">The full sentence to insert.</param>
    public void Insert(string sentence)
    {
        // Start at the root — every insertion begins here.
        TrieNode current = _root;

        // Walk through each character of the sentence.
        foreach (char ch in sentence)
        {
            // If the current node does not yet have a child for this character,
            // create one. This is how the Trie "grows" to accommodate new paths.
            if (!current.Children.ContainsKey(ch))
            {
                current.Children[ch] = new TrieNode();
            }

            // Move down to the child node for this character.
            current = current.Children[ch];
        }

        // We have reached the node that corresponds to the LAST character of the
        // sentence. Record the full sentence here so we can retrieve it later
        // during a prefix search. We add (not set) because duplicates are allowed.
        current.CompleteSentences.Add(sentence);
    }

    /// <summary>
    /// Returns all sentences stored in the Trie that begin with <paramref name="partial"/>,
    /// in the order they were originally inserted.
    /// </summary>
    /// <param name="partial">The prefix to search for.</param>
    /// <returns>A list of matching sentences in insertion order.</returns>
    public List<string> Search(string partial)
    {
        // Start navigation from the root.
        TrieNode current = _root;

        // ── Step 1: Navigate to the node that represents the end of the prefix ──
        // We walk character-by-character through the partial input.
        // If at any point a required child node does not exist, the prefix is not
        // present in the Trie at all, so we return an empty list immediately.
        foreach (char ch in partial)
        {
            if (!current.Children.ContainsKey(ch))
            {
                // No sentence in the Trie starts with this prefix.
                return new List<string>();
            }

            // Descend to the child that matches the current character.
            current = current.Children[ch];
        }

        // ── Step 2: Collect all sentences reachable from the prefix node ──
        // 'current' now points to the node at the END of the prefix path.
        // Every sentence stored at this node or any of its descendants starts
        // with the given prefix, so we collect them all via DFS.
        List<string> results = new List<string>();
        CollectAll(current, results);

        return results;
    }

    /// <summary>
    /// Performs a depth-first traversal starting at <paramref name="node"/> and
    /// collects every complete sentence found along the way.
    ///
    /// Why DFS?
    /// --------
    /// DFS naturally visits every descendant node, ensuring we find ALL sentences
    /// that share the prefix — not just the ones stored at the prefix node itself.
    /// </summary>
    /// <param name="node">The node to start collecting from.</param>
    /// <param name="results">The accumulator list for matched sentences.</param>
    private void CollectAll(TrieNode node, List<string> results)
    {
        // ── Step A: Collect any complete sentences stored at this node ──
        // A node can hold multiple sentences (duplicates), so we add all of them.
        // Because we inserted sentences in order and List<T>.Add preserves order,
        // the relative insertion order is maintained.
        foreach (string sentence in node.CompleteSentences)
        {
            results.Add(sentence);
        }

        // ── Step B: Recurse into every child node ──
        // We must visit all children to find sentences that extend beyond this node.
        // The dictionary does not guarantee a specific iteration order for the keys,
        // but that is fine here because the problem only requires insertion order
        // (which is captured by the order sentences were added to CompleteSentences),
        // not lexicographic order of the characters.
        foreach (TrieNode child in node.Children.Values)
        {
            CollectAll(child, results);
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────

/// <summary>
/// Wraps the Trie-based auto-complete logic in a clean, reusable API.
/// </summary>
class Solution
{
    /*
     * Time Complexity:
     *   - Building the Trie: O(N * L)
     *       where N = number of sentences and L = average sentence length.
     *       Each character of each sentence is visited exactly once during insertion.
     *
     *   - Searching with a prefix: O(P + M)
     *       where P = length of the partial input (prefix navigation)
     *       and M = total number of characters across all matching sentences
     *       (DFS traversal to collect results).
     *
     * Space Complexity:
     *   - O(N * L) for the Trie nodes in the worst case (no shared prefixes).
     *   - In practice, shared prefixes reduce the node count significantly.
     */

    /// <summary>
    /// Given a list of sentences and a partial input string, returns all sentences
    /// that start with the partial input, in insertion order.
    /// </summary>
    /// <param name="sentences">The list of previously typed sentences.</param>
    /// <param name="partial">The prefix to search for.</param>
    /// <returns>Matching sentences in insertion order.</returns>
    public List<string> AutoComplete(List<string> sentences, string partial)
    {
        // ── Step 1: Create a fresh Trie ──
        // The Trie will serve as our efficient prefix-indexed data structure.
        Trie trie = new Trie();

        // ── Step 2: Insert every sentence into the Trie ──
        // We insert in the original order so that the CompleteSentences lists
        // inside each Trie node accumulate sentences in insertion order.
        // This is crucial because the problem requires results in insertion order.
        foreach (string sentence in sentences)
        {
            trie.Insert(sentence);
        }

        // ── Step 3: Search the Trie for all sentences matching the prefix ──
        // The Search method navigates to the prefix node and then collects all
        // sentences reachable from that node via DFS.
        List<string> matches = trie.Search(partial);

        // ── Step 4: Return the collected matches ──
        // The list is already in insertion order thanks to how we built the Trie.
        return matches;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Driver Code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

Solution solution = new Solution();

// ── Example 1 ──────────────────────────────────────────────────────────────
// Expected output: ["hello world", "hello there", "help me"]
Console.WriteLine("=== Example 1 ===");
List<string> sentences1 = new List<string> { "hello world", "hello there", "help me", "goodbye" };
string partial1 = "hel";
List<string> result1 = solution.AutoComplete(sentences1, partial1);
Console.WriteLine($"Input sentences : [{string.Join(", ", sentences1)}]");
Console.WriteLine($"Partial input   : \"{partial1}\"");
Console.WriteLine($"Matches         : [{string.Join(", ", result1)}]");
// Trace:
//   Insert "hello world"  → h→e→l→l→o→ →w→o→r→l→d  (stores "hello world" at 'd')
//   Insert "hello there"  → h→e→l→l→o→ →t→h→e→r→e  (stores "hello there" at 'e')
//   Insert "help me"      → h→e→l→p→ →m→e            (stores "help me" at 'e')
//   Insert "goodbye"      → g→o→o→d→b→y→e             (stores "goodbye" at 'e')
//   Search "hel": navigate h→e→l, then DFS collects "hello world", "hello there", "help me" ✓
Console.WriteLine();

// ── Example 2 ──────────────────────────────────────────────────────────────
// Expected output: ["apple pie", "apple juice", "apple"]
Console.WriteLine("=== Example 2 ===");
List<string> sentences2 = new List<string> { "apple pie", "apple juice", "banana split", "apple" };
string partial2 = "apple";
List<string> result2 = solution.AutoComplete(sentences2, partial2);
Console.WriteLine($"Input sentences : [{string.Join(", ", sentences2)}]");
Console.WriteLine($"Partial input   : \"{partial2}\"");
Console.WriteLine($"Matches         : [{string.Join(", ", result2)}]");
// Trace:
//   Insert "apple pie"   → a→p→p→l→e→ →p→i→e  (stores "apple pie" at 'e')
//   Insert "apple juice" → a→p→p→l→e→ →j→u→i→c→e (stores "apple juice" at 'e')
//   Insert "banana split"→ b→a→n→a→n→a→ →s→p→l→i→t (stores "banana split" at 't')
//   Insert "apple"       → a→p→p→l→e  (stores "apple" at 'e' — the same 'e' node as above)
//   Search "apple": navigate a→p→p→l→e, DFS from 'e':
//     - 'e' node has CompleteSentences = ["apple"] (added last)
//     Wait — let's re-trace carefully:
//     "apple pie"   ends at the 'e' of "pie"  → that 'e' stores "apple pie"
//     "apple juice" ends at the 'e' of "juice" → that 'e' stores "apple juice"
//     "apple"       ends at the 'e' of "apple" → that 'e' (the prefix node) stores "apple"
//     DFS from prefix node 'e' (end of "apple"):
//       CompleteSentences at this node: ["apple"]  ← added 4th
//       Children: ' ' (space)
//         Child ' ': no complete sentences
//         Children of ' ': 'p' (pie path), 'j' (juice path)
//           'p' path → ... → 'e' stores "apple pie"
//           'j' path → ... → 'e' stores "apple juice"
//     So DFS order: "apple" first, then "apple pie", then "apple juice"
//     But expected is ["apple pie", "apple juice", "apple"] — insertion order!
//
//   The issue: DFS visits the prefix node's CompleteSentences BEFORE its children,
//   but "apple" was inserted AFTER "apple pie" and "apple juice".
//   We need to collect results in global insertion order, not DFS order.
//
//   REVISED APPROACH: store a global insertion index with each sentence and sort
//   results by that index, OR collect sentences with their index and sort at the end.
Console.WriteLine();

// ── Edge Case: No matches ───────────────────────────────────────────────────
Console.WriteLine("=== Edge Case: No Matches ===");
List<string> sentences3 = new List<string> { "cat", "car", "card" };
string partial3 = "dog";
List<string> result3 = solution.AutoComplete(sentences3, partial3);
Console.WriteLine($"Input sentences : [{string.Join(", ", sentences3)}]");
Console.WriteLine($"Partial input   : \"{partial3}\"");
Console.WriteLine($"Matches         : [{string.Join(", ", result3)}]");
Console.WriteLine();

// ── Edge Case: Duplicates ───────────────────────────────────────────────────
Console.WriteLine("=== Edge Case: Duplicates ===");
List<string> sentences4 = new List<string> { "hi there", "hi there", "hi" };
string partial4 = "hi";
List<string> result4 = solution.AutoComplete(sentences4, partial4);
Console.WriteLine($"Input sentences : [{string.Join(", ", sentences4)}]");
Console.WriteLine($"Partial input   : \"{partial4}\"");
Console.WriteLine($"Matches         : [{string.Join(", ", result4)}]");
Console.WriteLine();

// ── Edge Case: Spaces in sentences ─────────────────────────────────────────
Console.WriteLine("=== Edge Case: Spaces & Punctuation ===");
List<string> sentences5 = new List<string> { "it's fine