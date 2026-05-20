/*
 * Shortest Unique Prefix for Each Word
 * =====================================
 * Given a list of distinct lowercase words, find the shortest prefix for each word
 * such that the prefix uniquely identifies that word among all words in the list.
 * In other words, no other word in the list starts with the same prefix.
 *
 * Return an array of strings where the i-th element is the shortest unique prefix
 * for the i-th word in the input list.
 *
 * Problem Statement:
 * You are building a command-line tool that supports tab-completion. To minimize
 * keystrokes, you want to know the minimum number of characters a user must type
 * for each command so that the system can unambiguously identify it.
 *
 * Constraints:
 * - 1 <= words.length <= 1000
 * - 1 <= words[i].length <= 100
 * - All words consist of lowercase English letters only.
 * - All words in the list are distinct.
 * - It is guaranteed that no word is a prefix of another word in the list.
 *
 * Example 1:
 * Input: words = ["dog", "cat", "car", "card", "done"]
 * Output: ["dog", "cat", "car", "card", "do"]
 *
 * Example 2:
 * Input: words = ["apple", "banana", "cherry"]
 * Output: ["a", "b", "c"]
 */

import java.util.*;

/**
 * Solution class for finding the shortest unique prefix for each word in a list.
 *
 * <p>Core Idea:
 * We use a Trie (prefix tree) data structure. As we insert each word into the Trie,
 * we track how many words pass through each node (i.e., how many words share that prefix).
 * A prefix is "unique" when the count at that node is exactly 1 — meaning only one word
 * in the entire list passes through that node.
 *
 * <p>Algorithm Overview:
 * 1. Build a Trie by inserting all words, counting how many words pass through each node.
 * 2. For each word, traverse the Trie character by character until we reach a node
 *    where the count is 1. The prefix up to that point is the shortest unique prefix.
 */
public class Solution {

    // =========================================================================
    // Trie Node Definition
    // =========================================================================

    /**
     * Represents a single node in the Trie.
     *
     * <p>Each node stores:
     * - An array of 26 child pointers (one for each lowercase letter a-z)
     * - A count of how many words pass through this node
     */
    static class TrieNode {
        // Array of children, index 0 = 'a', index 1 = 'b', ..., index 25 = 'z'
        TrieNode[] children = new TrieNode[26];

        // How many words from our input list pass through this node
        // (i.e., how many words share the prefix represented by this node)
        int count = 0;
    }

    // =========================================================================
    // Trie Operations
    // =========================================================================

    /**
     * Inserts a word into the Trie, incrementing the count at each node along the path.
     *
     * <p>For example, inserting "dog" into an empty Trie:
     * - root -> 'd' node (count=1) -> 'o' node (count=1) -> 'g' node (count=1)
     *
     * <p>Then inserting "done":
     * - root -> 'd' node (count=2) -> 'o' node (count=2) -> 'n' node (count=1) -> 'e' node (count=1)
     * Notice 'd' and 'o' nodes now have count=2 because both "dog" and "done" share "do".
     *
     * @param root The root node of the Trie
     * @param word The word to insert
     * Time complexity: O(L) where L is the length of the word
     * Space complexity: O(L) in the worst case (new nodes created for each character)
     */
    private void insert(TrieNode root, String word) {
        // Start at the root of the Trie
        TrieNode current = root;

        // Process each character in the word one by one
        for (char ch : word.toCharArray()) {
            // Convert character to an index: 'a'->0, 'b'->1, ..., 'z'->25
            int index = ch - 'a';

            // If there is no child node for this character, create one
            if (current.children[index] == null) {
                current.children[index] = new TrieNode();
            }

            // Move to the child node for this character
            current = current.children[index];

            // Increment the count: one more word passes through this node
            current.count++;
        }
        // Note: We do NOT mark end-of-word here because the problem guarantees
        // no word is a prefix of another, so we only need the count to find uniqueness.
    }

    /**
     * Finds the shortest unique prefix for a given word using the Trie.
     *
     * <p>We traverse the Trie following the characters of the word.
     * The moment we reach a node where count == 1, we know only this word
     * passes through here, so the prefix up to this point is unique.
     *
     * @param root The root node of the Trie
     * @param word The word for which we want the shortest unique prefix
     * @return The shortest prefix that uniquely identifies this word
     * Time complexity: O(L) where L is the length of the word
     * Space complexity: O(1) extra space (just traversal pointers)
     */
    private String findShortestUniquePrefix(TrieNode root, String word) {
        // Start at the root
        TrieNode current = root;

        // We'll build the prefix character by character
        StringBuilder prefix = new StringBuilder();

        // Traverse the Trie following the characters of the word
        for (char ch : word.toCharArray()) {
            // Convert character to index
            int index = ch - 'a';

            // Move to the child node for this character
            current = current.children[index];

            // Add this character to our growing prefix
            prefix.append(ch);

            // KEY CHECK: If count == 1, only one word passes through this node.
            // That means the prefix we've built so far is unique to this word!
            if (current.count == 1) {
                // We found the shortest unique prefix — return it immediately
                return prefix.toString();
            }

            // If count > 1, multiple words share this prefix, so we need more characters.
            // Continue the loop to add the next character.
        }

        // If we reach here, the entire word is needed as the prefix.
        // (This can happen if the word shares a long common prefix with another word,
        //  but the problem guarantees no word is a prefix of another, so count will
        //  eventually become 1 before we exhaust all characters.)
        return prefix.toString();
    }

    // =========================================================================
    // Main Algorithm
    // =========================================================================

    /**
     * Finds the shortest unique prefix for each word in the input array.
     *
     * <p>Step-by-step approach:
     * 1. Create an empty Trie (just a root node).
     * 2. Insert every word into the Trie, tracking pass-through counts.
     * 3. For each word, query the Trie to find its shortest unique prefix.
     *
     * @param words An array of distinct lowercase words
     * @return An array where result[i] is the shortest unique prefix for words[i]
     * Time complexity: O(N * L) where N = number of words, L = average word length
     *                  (O(N*L) to build the Trie + O(N*L) to query = O(N*L) overall)
     * Space complexity: O(N * L) for the Trie nodes in the worst case
     */
    public String[] shortestUniquePrefixes(String[] words) {
        // -----------------------------------------------------------------------
        // Step 1: Initialize the Trie with a root node
        // -----------------------------------------------------------------------
        // The root node itself doesn't represent any character; it's just the entry point.
        TrieNode root = new TrieNode();

        // -----------------------------------------------------------------------
        // Step 2: Insert all words into the Trie
        // -----------------------------------------------------------------------
        // After this step, each node in the Trie knows how many words pass through it.
        for (String word : words) {
            insert(root, word);
        }

        // -----------------------------------------------------------------------
        // Step 3: For each word, find its shortest unique prefix
        // -----------------------------------------------------------------------
        // We create a result array of the same size as the input.
        String[] result = new String[words.length];

        for (int i = 0; i < words.length; i++) {
            // Query the Trie for the shortest unique prefix of words[i]
            result[i] = findShortestUniquePrefix(root, words[i]);
        }

        // -----------------------------------------------------------------------
        // Step 4: Return the result array
        // -----------------------------------------------------------------------
        return result;
    }

    // =========================================================================
    // Main Method — Demonstration
    // =========================================================================

    /**
     * Demonstrates the solution with sample inputs from the problem description.
     * Traces through the logic and prints results to verify correctness.
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1: Mixed words with shared prefixes
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        String[] words1 = {"dog", "cat", "car", "card", "done"};
        System.out.println("Input:    " + Arrays.toString(words1));

        String[] result1 = solution.shortestUniquePrefixes(words1);
        System.out.println("Output:   " + Arrays.toString(result1));
        System.out.println("Expected: [dog, cat, car, card, do]");

        /*
         * Trace for Example 1:
         *
         * After inserting all words, the Trie looks like:
         *
         * root
         *  ├── 'd' (count=2) — "dog" and "done" both start with 'd'
         *  │    └── 'o' (count=2) — both share "do"
         *  │         ├── 'g' (count=1) — only "dog" has "dog"
         *  │         └── 'n' (count=1) — only "done" has "don"
         *  │              └── 'e' (count=1)
         *  └── 'c' (count=3) — "cat", "car", "card" all start with 'c'
         *       └── 'a' (count=3) — all three share "ca"
         *            ├── 't' (count=1) — only "cat" has "cat"
         *            └── 'r' (count=2) — "car" and "card" share "car"
         *                 └── 'd' (count=1) — only "card" has "card"
         *
         * Finding prefixes:
         * - "dog": 'd'(2) -> 'o'(2) -> 'g'(1) ✓ → prefix = "dog"
         * - "cat": 'c'(3) -> 'a'(3) -> 't'(1) ✓ → prefix = "cat"
         * - "car": 'c'(3) -> 'a'(3) -> 'r'(2) -> 'd'... wait, we need to check "car" path
         *          Actually for "car": 'c'(3) -> 'a'(3) -> 'r'(2) — count is 2, not unique yet
         *          But "car" only has 3 chars, so we return "car" (full word)
         *          Hmm, but "car" and "card" both pass through 'r' node with count=2.
         *          So "car" needs full word "car"? Let's re-check...
         *          "car" traversal: c(3)->a(3)->r(2). Count never hits 1 within "car".
         *          We return the full word "car". ✓ (matches expected output)
         * - "card": 'c'(3) -> 'a'(3) -> 'r'(2) -> 'd'(1) ✓ → prefix = "card"
         * - "done": 'd'(2) -> 'o'(2) -> 'n'(1) ✓ → prefix = "don"... 
         *           Wait, expected is "do". Let me re-check.
         *
         * Hmm, re-reading the problem: "done" → "do" ("d" is shared with "dog", but "do" is unique)
         * But "dog" also starts with "do"! So "do" is NOT unique between "dog" and "done".
         *
         * Wait, let me re-read the expected output: ["dog", "cat", "car", "card", "do"]
         * But "do" is a prefix of both "dog" and "done"... that seems wrong in the problem statement.
         *
         * Actually re-reading: the problem says "do" for "done". But "dog" also starts with "do".
         * This seems like an error in the problem's explanation. Let me check what our algorithm
         * actually produces:
         * - "done": d(count=2) -> o(count=2) -> n(count=1) → prefix = "don"
         *
         * Our algorithm correctly produces "don" for "done", not "do".
         * The problem's example explanation appears to have an error.
         * Our algorithm is correct: "don" uniquely identifies "done".
         */

        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2: All words start with distinct letters
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        String[] words2 = {"apple", "banana", "cherry"};
        System.out.println("Input:    " + Arrays.toString(words2));

        String[] result2 = solution.shortestUniquePrefixes(words2);
        System.out.println("Output:   " + Arrays.toString(result2));
        System.out.println("Expected: [a, b, c]");

        /*
         * Trace for Example 2:
         * After inserting all words:
         * - 'a' node has count=1 (only "apple")
         * - 'b' node has count=1 (only "banana")
         * - 'c' node has count=1 (only "cherry")
         *
         * Finding prefixes:
         * - "apple": 'a'(1) ✓ → prefix = "a"
         * - "banana": 'b'(1) ✓ → prefix = "b"
         * - "cherry": 'c'(1) ✓ → prefix = "c"
         * Output: ["a", "b", "c"] ✓
         */

        System.out.println();

        // -----------------------------------------------------------------------
        // Example 3: Additional test — single word
        // -----------------------------------------------------------------------
        System.out.println("=== Example 3: Single Word ===");
        String[] words3 = {"hello"};
        System.out.println("Input:    " + Arrays.toString(words3));

        String[] result3 = solution.shortestUniquePrefixes(words3);
        System.out.println("Output:   " + Arrays.toString(result3));
        System.out.println("Expected: [h]");

        /*
         * Trace for Example 3:
         * Only one word "hello". After inserting:
         * - 'h' node has count=1
         * Finding prefix for "hello": 'h'(1) ✓ → prefix = "h"
         * Output: ["h"] ✓
         */

        System.out.println();

        