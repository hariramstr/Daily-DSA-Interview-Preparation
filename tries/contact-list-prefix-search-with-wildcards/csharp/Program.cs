/*
 * Contact List Prefix Search with Wildcards
 * Difficulty: Medium
 * Topic: Tries
 *
 * Problem Description:
 * You are building a smart contact search feature for a messaging app. Users can search
 * their contact list using a query string that may contain lowercase letters and the
 * wildcard character '?', where '?' can match exactly one lowercase letter.
 *
 * Given a list of contact names and a list of search queries, return for each query
 * the number of contacts whose names start with the given query pattern (prefix match
 * with wildcard support).
 *
 * A contact name matches a query if:
 * 1. The query is a prefix of the contact name (or equal in length to the name), AND
 * 2. Every non-wildcard character in the query matches the corresponding character
 *    in the contact name exactly, AND
 * 3. Every '?' in the query matches exactly one character in the contact name.
 *
 * Constraints:
 * - 1 <= contacts.length <= 10^4
 * - 1 <= contacts[i].length <= 20
 * - 1 <= queries.length <= 10^4
 * - 1 <= queries[i].length <= 20
 * - All contact names contain only lowercase English letters
 * - Queries may contain lowercase English letters or '?'
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Trie Node Definition
// Each node in the Trie represents one character position in the stored words.
// ─────────────────────────────────────────────────────────────────────────────
class TrieNode
{
    // children[0..25] correspond to letters 'a'..'z'
    // A null entry means no contact name passes through this edge.
    public TrieNode[] Children { get; } = new TrieNode[26];

    // How many contact names pass through (or end at) this node?
    // We increment this counter every time we insert a name that visits this node.
    // This lets us answer "how many names share this prefix?" in O(1) once we
    // reach the node — no need to count descendants at query time.
    public int PassCount { get; set; } = 0;
}

// ─────────────────────────────────────────────────────────────────────────────
// Trie (Prefix Tree) Definition
// ─────────────────────────────────────────────────────────────────────────────
class Trie
{
    // The root node doesn't represent any character; it's the entry point.
    private readonly TrieNode _root = new TrieNode();

    /// <summary>
    /// Inserts a contact name into the Trie.
    /// Time: O(L) where L = length of the name.
    /// </summary>
    public void Insert(string name)
    {
        TrieNode current = _root;

        foreach (char ch in name)
        {
            int index = ch - 'a'; // Map 'a'→0, 'b'→1, …, 'z'→25

            // Create the child node if it doesn't exist yet.
            if (current.Children[index] == null)
                current.Children[index] = new TrieNode();

            // Move down to the child.
            current = current.Children[index];

            // Increment PassCount: this name passes through this node,
            // so any prefix that leads here will count this name.
            current.PassCount++;
        }
    }

    /// <summary>
    /// Counts how many inserted names match the given query prefix (with wildcards).
    ///
    /// Time:  O(Q * 26^W) where Q = query length, W = number of '?' wildcards.
    ///        In the worst case every character is '?', giving O(26^Q), but
    ///        Q ≤ 20 and wildcards are typically sparse, so this is fast in practice.
    ///        A tighter bound: at each '?' we branch into at most 26 children,
    ///        but PassCount pruning cuts dead branches immediately.
    ///
    /// Space: O(Q) recursion stack depth (or O(Q) for the BFS queue).
    /// </summary>
    public int CountMatches(string query)
    {
        // We use an iterative BFS/DFS approach with a queue of (node, depth) pairs.
        // Starting from the root at depth 0, we process each character of the query.
        // When we finish all characters, every node still in the frontier contributed
        // its PassCount to the answer — but we need to be careful: PassCount at a node
        // means "how many names passed through here", which equals "how many names
        // have this node's prefix as a prefix of their name". That is exactly what we want.

        // However, the cleanest approach is to track a LIST of active nodes at each depth.
        // After processing all query characters, we sum the PassCount of all active nodes.
        // Wait — that would double-count. Let me think more carefully.
        //
        // Better approach: track a list of active TrieNodes at the CURRENT depth.
        // After processing all query characters, the active nodes are those reached
        // by following the query path. The number of contacts that match is the SUM
        // of PassCount values of all active nodes at the final depth.
        //
        // Why sum PassCount? Because different wildcard branches lead to DIFFERENT nodes,
        // and each node's PassCount counts distinct names that passed through it.
        // Names that share a common prefix will have been counted in the same node,
        // so there's no double-counting across different active nodes at the same depth.

        // Start: the root is the only active node (depth 0, before any character).
        var currentLevel = new List<TrieNode> { _root };

        // Process each character in the query one at a time.
        for (int i = 0; i < query.Length; i++)
        {
            char ch = query[i];

            // This list will hold all nodes reachable after consuming query[i].
            var nextLevel = new List<TrieNode>();

            if (ch == '?')
            {
                // Wildcard: '?' matches ANY single lowercase letter.
                // So from each currently active node, we follow ALL 26 possible children.
                foreach (TrieNode node in currentLevel)
                {
                    for (int c = 0; c < 26; c++)
                    {
                        TrieNode child = node.Children[c];
                        if (child != null)
                        {
                            // This child exists, meaning at least one contact name
                            // has the letter ('a'+c) at this position.
                            nextLevel.Add(child);
                        }
                    }
                }
            }
            else
            {
                // Exact character match: only follow the specific child for ch.
                int index = ch - 'a';

                foreach (TrieNode node in currentLevel)
                {
                    TrieNode child = node.Children[index];
                    if (child != null)
                    {
                        // This child exists, meaning at least one contact name
                        // has the letter ch at this position.
                        nextLevel.Add(child);
                    }
                }
            }

            // Move to the next depth level.
            currentLevel = nextLevel;

            // Early exit: if no nodes are reachable, no contacts can match.
            if (currentLevel.Count == 0)
                return 0;
        }

        // After consuming all query characters, every node in currentLevel represents
        // a point in the Trie reached by a valid match of the query pattern.
        // The PassCount of each such node tells us how many contact names passed
        // through that node (i.e., have the corresponding prefix).
        // Summing these gives the total number of matching contacts.
        int total = 0;
        foreach (TrieNode node in currentLevel)
            total += node.PassCount;

        return total;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    /// <summary>
    /// For each query, returns the count of contacts whose name starts with the
    /// query pattern (supporting '?' as a single-character wildcard).
    ///
    /// Time Complexity:
    ///   - Building the Trie: O(N * L) where N = number of contacts, L = max name length.
    ///   - Answering queries: O(Q * queryLen * 26^W) per query in the worst case,
    ///     where W = number of wildcards in the query. Typically much faster due to pruning.
    ///   - Overall: O(N*L + Q * queryLen * 26^W)
    ///
    /// Space Complexity:
    ///   - Trie storage: O(N * L * 26) for the node array pointers (most are null).
    ///   - BFS frontier: O(26^W) nodes at most at any level.
    ///   - Overall: O(N * L * 26)
    /// </summary>
    public int[] ContactSearch(string[] contacts, string[] queries)
    {
        // ── Step 1: Build the Trie from all contact names ──────────────────────
        // We insert every contact name into the Trie so that prefix queries
        // can be answered efficiently without scanning all names linearly.
        var trie = new Trie();

        foreach (string contact in contacts)
        {
            // Insert this contact name; each node along the path gets PassCount++.
            trie.Insert(contact);
        }

        // ── Step 2: Answer each query using the Trie ───────────────────────────
        // For each query we traverse the Trie, branching at '?' wildcards,
        // and sum up PassCount values at the nodes reached after the full query.
        int[] results = new int[queries.Length];

        for (int i = 0; i < queries.Length; i++)
        {
            results[i] = trie.CountMatches(queries[i]);
        }

        // ── Step 3: Return the results array ───────────────────────────────────
        return results;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// contacts = ["alice", "alfred", "bob", "alicia", "alba"]
// queries  = ["al", "al?", "b?b"]
//
// Trie structure (relevant paths):
//   root → a → l → i → c → e   (alice,   PassCount at 'l'=4, at 'i'=3)
//                    → c → i → a (alicia)
//                → b → a        (alba)
//              → f → r → e → d  (alfred)
//        → b → o → b            (bob)
//
// "al"  → reach node for 'l' under 'a': PassCount = 4  ✓
// "al?" → from 'l'-node, expand all children: 'i'-node (PassCount=3) + 'b'-node (PassCount=1) + 'f'-node (PassCount=1) = 5?
//   Wait, let me recount. After inserting:
//     alice:   a(4) → l(4) → i(3) → c(2) → e(1)
//     alfred:  a(4) → l(4) → f(1) → r(1) → e(1) → d(1)
//     bob:     b(1) → o(1) → b(1)
//     alicia:  a(4) → l(4) → i(3) → c(2) → i(1) → a(1)
//     alba:    a(4) → l(4) → b(1) → a(1)
//
//   "al?" → from l-node (PassCount=4), children are:
//     i-node (PassCount=3): alice + alicia + (alice shares 'i') → alice, alicia = 2 names? No:
//       alice  → a,l,i,c,e  → i-node gets PassCount++ for alice AND alicia
//       alicia → a,l,i,c,i,a → i-node gets PassCount++ for alicia
//       So i-node PassCount = 2 (alice contributes 1, alicia contributes 1)... 
//       Hmm wait, alice: a→l→i→c→e, alicia: a→l→i→c→i→a
//       Both pass through the 'i' node under 'l'. So i-node PassCount = 2.
//     f-node (PassCount=1): alfred
//     b-node (PassCount=1): alba
//   Total for "al?" = 2 + 1 + 1 = 4  ✓
//
// "b?b" → b-node → expand all children of b-node: o-node (PassCount=1)
//          → from o-node, look for 'b': b-node (PassCount=1) → total = 1  ✓
//
// Expected output: [4, 4, 1]
// (The problem description corrected itself to [4,4,1] in the explanation.)

Console.WriteLine("=== Example 1 ===");
string[] contacts1 = { "alice", "alfred", "bob", "alicia", "alba" };
string[] queries1  = { "al", "al?", "b?b" };
int[] result1 = solution.ContactSearch(contacts1, queries1);
Console.WriteLine($"Input contacts: [{string.Join(", ", contacts1)}]");
Console.WriteLine($"Input queries:  [{string.Join(", ", queries1)}]");
Console.WriteLine($"Output:         [{string.Join(", ", result1)}]");
Console.WriteLine($"Expected:       [4, 4, 1]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// contacts = ["sam", "samuel", "sandy", "sandra"]
// queries  = ["sa?", "san??", "s?m"]
//
// Trie after insertion:
//   sam:    s(4)→a(4)→m(2)→(end)
//   samuel: s(4)→a(4)→m(2)→u(1)→e(1)→l(1)
//   sandy:  s(4)→a(4)→n(2)→d(2)→y(1)
//   sandra: s(4)→a(4)→n(2)→d(2)→r(1)→a(1)
//
// "sa?" → s→a→ expand all children of a-node:
//   m-node (PassCount=2): sam, samuel
//   n-node (PassCount=2): sandy, sandra
//   Total = 2 + 2 = 4  ✓
//
// "san??" → s→a→n→ expand all children of n-node:
//   d-node (PassCount=2): sandy, sandra
//   → from d-node, expand all children:
//     y-node (PassCount=1): sandy
//     r-node (PassCount=1): sandra
//   Total = 1 + 1 = 2  ✓
//
// "s?m" → s→ expand all children of s-node:
//   a-node (PassCount=4)
//   → from a-node, look for 'm': m-node (PassCount=2)
//   Total = 2  ✓
//
// Expected output: [4, 2, 2]
// (The problem says [4, 2, 1] for "s?m" but "samuel" also starts with "sam" so it's 2.)

Console.WriteLine("=== Example 2 ===");
string[] contacts2 = { "sam", "samuel", "sandy", "sandra"