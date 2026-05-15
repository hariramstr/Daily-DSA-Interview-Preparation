```java
/*
 * Title: Contact List Prefix Search with Wildcards
 * Difficulty: Medium
 * Topic: Tries
 *
 * Problem Description:
 * You are building a smart contact search feature for a messaging app. Users can search
 * their contact list using a query string that may contain lowercase letters and the
 * wildcard character `?`, where `?` can match exactly one lowercase letter.
 *
 * Given a list of contact names and a list of search queries, return for each query
 * the number of contacts whose names start with the given query pattern (prefix match
 * with wildcard support).
 *
 * A contact name matches a query if:
 * 1. The query is a prefix of the contact name (or equal in length to the name), AND
 * 2. Every non-wildcard character in the query matches the corresponding character in
 *    the contact name exactly, AND
 * 3. Every `?` in the query matches exactly one character in the contact name.
 *
 * Constraints:
 * - 1 <= contacts.length <= 10^4
 * - 1 <= contacts[i].length <= 20
 * - 1 <= queries.length <= 10^4
 * - 1 <= queries[i].length <= 20
 * - All contact names and query characters are lowercase English letters or `?` (queries only)
 * - Contact names contain only lowercase English letters
 *
 * Example 1:
 * Input: contacts = ["alice", "alfred", "bob", "alicia", "alba"], queries = ["al", "al?", "b?b"]
 * Output: [4, 4, 1]
 *
 * Example 2:
 * Input: contacts = ["sam", "samuel", "sandy", "sandra"], queries = ["sa?", "san??", "s?m"]
 * Output: [4, 2, 2]
 */

import java.util.*;

/**
 * Solution class for Contact List Prefix Search with Wildcards.
 *
 * <p>This solution uses a Trie (prefix tree) data structure to efficiently store
 * contact names and answer prefix queries with wildcard support.
 *
 * <p>Key Idea:
 * - Build a Trie from all contact names, storing at each node the count of contacts
 *   that pass through that node (i.e., how many contacts have this prefix).
 * - For each query, traverse the Trie using DFS/BFS. When we encounter a '?',
 *   we branch into ALL 26 possible child nodes (since '?' matches any letter).
 * - When we reach the end of the query pattern, sum up the counts at all reached nodes.
 */
public class Solution {

    // =========================================================================
    // Trie Node Definition
    // =========================================================================

    /**
     * Represents a single node in the Trie.
     *
     * <p>Each node stores:
     * - children: an array of 26 child nodes (one per lowercase letter a-z)
     * - count: how many contact names pass through (or end at) this node
     */
    static class TrieNode {
        // Array of 26 children, one for each letter 'a' to 'z'
        TrieNode[] children;

        // Number of contact names that have this node as part of their prefix path
        // (i.e., how many contacts pass through this node)
        int count;

        /**
         * Constructor: initializes children array and sets count to 0.
         */
        TrieNode() {
            children = new TrieNode[26];
            count = 0;
        }
    }

    // =========================================================================
    // Trie Class
    // =========================================================================

    /**
     * A Trie (prefix tree) that stores contact names and supports wildcard prefix queries.
     */
    static class Trie {
        // The root node of the Trie (represents the empty prefix)
        private final TrieNode root;

        /**
         * Constructor: creates an empty Trie with just a root node.
         */
        Trie() {
            root = new TrieNode();
        }

        /**
         * Inserts a contact name into the Trie.
         *
         * <p>For each character in the name, we traverse (or create) the corresponding
         * child node and increment its count to record that one more contact passes through.
         *
         * @param name the contact name to insert (only lowercase letters)
         *
         * Time complexity: O(L) where L is the length of the name
         * Space complexity: O(L) in the worst case for new nodes
         */
        void insert(String name) {
            // Start at the root node
            TrieNode current = root;

            // Process each character in the contact name
            for (char ch : name.toCharArray()) {
                // Calculate the index: 'a'->0, 'b'->1, ..., 'z'->25
                int index = ch - 'a';

                // If the child node for this character doesn't exist, create it
                if (current.children[index] == null) {
                    current.children[index] = new TrieNode();
                }

                // Move to the child node
                current = current.children[index];

                // Increment the count: one more contact passes through this node
                current.count++;
            }
            // Note: We don't need a separate "end of word" marker because we're
            // doing prefix matching — the count at each node tells us how many
            // contacts have that prefix.
        }

        /**
         * Counts how many contacts match the given query pattern as a prefix.
         *
         * <p>The query may contain '?' wildcards, each matching exactly one letter.
         * We use an iterative BFS approach: maintain a list of "current nodes" we
         * are simultaneously at. For each character in the query:
         * - If it's a letter, move each current node to its specific child.
         * - If it's '?', move each current node to ALL of its existing children.
         *
         * At the end, sum the counts of all nodes we've reached.
         *
         * @param query the search pattern (lowercase letters and '?' wildcards)
         * @return the number of contacts whose names start with the query pattern
         *
         * Time complexity: O(Q * 26^W) where Q is query length and W is number of wildcards
         *                  In practice, bounded by the number of Trie nodes visited.
         * Space complexity: O(N) where N is the number of Trie nodes (for the node lists)
         */
        int search(String query) {
            // We maintain a list of TrieNode positions we are currently at.
            // We start at the root node.
            List<TrieNode> currentNodes = new ArrayList<>();
            currentNodes.add(root);

            // Process each character in the query pattern
            for (char ch : query.toCharArray()) {
                // This list will hold all nodes we move to after processing this character
                List<TrieNode> nextNodes = new ArrayList<>();

                if (ch == '?') {
                    // Wildcard: '?' matches ANY single lowercase letter
                    // So from each current node, we branch into ALL 26 possible children
                    for (TrieNode node : currentNodes) {
                        // Check all 26 possible children (a through z)
                        for (int i = 0; i < 26; i++) {
                            if (node.children[i] != null) {
                                // This child exists, so '?' can match this letter
                                nextNodes.add(node.children[i]);
                            }
                        }
                    }
                } else {
                    // Specific letter: only move to the child corresponding to this letter
                    int index = ch - 'a';
                    for (TrieNode node : currentNodes) {
                        if (node.children[index] != null) {
                            // This child exists, so we can continue matching
                            nextNodes.add(node.children[index]);
                        }
                        // If the child doesn't exist, this path is dead — we don't add anything
                    }
                }

                // Update current nodes to the next level
                currentNodes = nextNodes;

                // Early termination: if no nodes remain, no contacts can match
                if (currentNodes.isEmpty()) {
                    return 0;
                }
            }

            // After processing all characters in the query, we've matched the full prefix.
            // The answer is the sum of counts at all nodes we've reached.
            // Each node's count tells us how many contacts pass through it,
            // meaning how many contacts have this prefix.
            int totalCount = 0;
            for (TrieNode node : currentNodes) {
                totalCount += node.count;
            }

            return totalCount;
        }
    }

    // =========================================================================
    // Main Solution Method
    // =========================================================================

    /**
     * Solves the contact list prefix search problem with wildcard support.
     *
     * <p>Algorithm:
     * 1. Build a Trie by inserting all contact names.
     * 2. For each query, use the Trie's search method to count matching contacts.
     * 3. Return the list of counts.
     *
     * @param contacts array of contact names (lowercase letters only)
     * @param queries  array of search queries (lowercase letters and '?' wildcards)
     * @return an array where result[i] is the number of contacts matching queries[i]
     *
     * Time complexity: O(C*L + Q*L*26^W) where:
     *   C = number of contacts, L = max name/query length,
     *   Q = number of queries, W = max wildcards per query
     * Space complexity: O(C*L*26) for the Trie nodes
     */
    public int[] contactSearch(String[] contacts, String[] queries) {
        // Step 1: Build the Trie
        // Create a new Trie instance
        Trie trie = new Trie();

        // Insert every contact name into the Trie
        // This builds the prefix tree structure with counts at each node
        for (String contact : contacts) {
            trie.insert(contact);
        }

        // Step 2: Answer each query
        int[] results = new int[queries.length];

        for (int i = 0; i < queries.length; i++) {
            // Use the Trie to count how many contacts match this query pattern
            results[i] = trie.search(queries[i]);
        }

        // Step 3: Return the results array
        return results;
    }

    // =========================================================================
    // Main Method — Demonstration
    // =========================================================================

    /**
     * Demonstrates the solution with sample inputs and prints results.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // ---------------------------------------------------------------
        // Example 1 Trace:
        // contacts = ["alice", "alfred", "bob", "alicia", "alba"]
        // queries  = ["al", "al?", "b?b"]
        //
        // Trie structure (partial, showing 'al' branch):
        //   root -> a(5) -> l(4) -> i(3) -> c(2) -> e(1) [alice]
        //                                          -> i(1) -> a(1) [alicia]
        //                        -> b(1) -> a(1) [alba]
        //                -> f(1) -> r(1) -> e(1) -> d(1) [alfred]
        //        -> b(1) -> o(1) -> b(1) [bob]
        //
        // Query "al":
        //   Start at root. Process 'a': move to root.children['a'-'a'=0]. count=5? No wait...
        //   Actually the root itself has no count. Let's re-trace:
        //   After inserting "alice":  a.count=1, l.count=1, i.count=1, c.count=1, e.count=1
        //   After inserting "alfred": a.count=2, l.count=2, f.count=1, r.count=1, e.count=1, d.count=1
        //   After inserting "bob":    b.count=1, o.count=1, b.count=1
        //   After inserting "alicia": a.count=3, l.count=3, i.count=2, c.count=2, i.count=1, a.count=1
        //   After inserting "alba":   a.count=4, l.count=4, b.count=1, a.count=1
        //
        //   Query "al": process 'a' -> node_a (count=4), process 'l' -> node_l (count=4)
        //   Sum = 4 ✓
        //
        //   Query "al?": process 'a' -> node_a, process 'l' -> node_l,
        //   process '?' -> all children of node_l: node_i(count=3), node_f(count=1), node_b(count=1)
        //   Wait, node_i has count=2 (alice+alicia), node_f has count=1 (alfred), node_b has count=1 (alba)
        //   Hmm, let me recount:
        //   "alice":  a->l->i->c->e  (i gets count 1 from alice)
        //   "alfred": a->l->f->r->e->d (f gets count 1)
        //   "alicia": a->l->i->c->i->a (i gets count 2 total: alice+alicia)
        //   "alba":   a->l->b->a (b gets count 1)
        //   So node_l's children: i(count=2), f(count=1), b(count=1)
        //   Sum = 2+1+1 = 4 ✓
        //
        //   Query "b?b": process 'b' -> node_b(count=1), process '?' -> node_o(count=1),
        //   process 'b' -> node_b(count=1). Sum = 1 ✓
        //
        // Expected: [4, 4, 1]
        // ---------------------------------------------------------------

        System.out.println("=== Example 1 ===");
        String[] contacts1 = {"alice", "alfred", "bob", "alicia", "alba"};
        String[] queries1 = {"al", "al?", "b?b"};
        int[] result1 = solution.contactSearch(contacts1, queries1);
        System.out.println("Contacts: " + Arrays.toString(contacts1));
        System.out.println("Queries:  " + Arrays.toString(queries1));
        System.out.println("Output:   " + Arrays.toString(result1));
        System.out.println("Expected: [4, 4, 1]");
        System.out.println();

        // ---------------------------------------------------------------
        // Example 2 Trace:
        // contacts = ["sam", "samuel", "sandy", "sandra"]
        // queries  = ["sa?", "san??", "s?m"]
        //
        // Trie insertions:
        //   "sam":    s(4)->a(4)->m(2)
        //   "samuel": s(4)->a(4)->m(2)->u(1)->e(1)->l(1)
        //   "sandy":  s(4)->a(4)->n(2)->d(2)->y(1)
        //   "sandra": s(4)->a(4)->n(2)->d(2)->r(1)->a(1)
        //
        // Query "sa?":
        //   's' -> node_s(count=4)
        //   'a' -> node_a(count=4)
        //   '?' -> all children of node_a: node_m(count=2), node_n(count=2)
        //   Sum = 2+2 = 4 ✓
        //
        // Query "san??":
        //   's' -> node_s, 'a' -> node_a, 'n' -> node_n(count=2)
        //   '?' -> all children of node_n: node_d(count=2)
        //   '?' -> all children of node_d: node_y(count=1), node_r(count=