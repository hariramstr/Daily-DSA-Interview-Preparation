```java
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
 * Additionally, after each query, you must also return the lexicographically smallest word
 * that matched the pattern, or an empty string if no word matched.
 *
 * Implement the LogStream class:
 * - LogStream(): Initializes the object.
 * - void insert(String word): Inserts the word into the data structure.
 * - int[] query(String pattern): Returns a pair [count, index] where count is the number of
 *   matching words and index is the 0-based insertion index of the lexicographically smallest
 *   matching word, or -1 if no match exists.
 *
 * Constraints:
 * - 1 <= word.length, pattern.length <= 20
 * - All characters in word are lowercase English letters.
 * - pattern consists of lowercase English letters and '?'.
 * - At most 5 * 10^4 calls will be made to insert and query combined.
 * - Words inserted are not necessarily unique.
 *
 * Example 1:
 * Input: ["LogStream","insert","insert","insert","query","query"]
 *        [[],["apple"],["apply"],["apt"],["ap?le"],["a??"]]
 * Output: [null, null, null, null, [2, 0], [1, 2]]
 *
 * Example 2:
 * Input: ["LogStream","insert","insert","query","insert","query"]
 *        [[],["bat"],["bad"],["b?d"],["bed"],["b?d"]]
 * Output: [null, null, null, [1, 1], null, [2, 1]]
 */

import java.util.*;

/**
 * Solution class containing the LogStream implementation using a Trie data structure
 * with wildcard query support.
 *
 * <p>The core idea:
 * - We use a Trie to store all inserted words.
 * - Each Trie node stores children for each of the 26 lowercase letters.
 * - At each terminal node (end of a word), we store the insertion index and the word itself.
 * - For queries with '?', we perform a DFS/recursive traversal of the Trie,
 *   branching on all 26 children when we encounter a '?'.
 */
public class Solution {

    // =========================================================================
    // Inner class: TrieNode
    // =========================================================================

    /**
     * Represents a single node in the Trie.
     *
     * <p>Each node has:
     * - children[26]: pointers to child nodes for each letter 'a'-'z'
     * - wordEntries: a list of (insertionIndex, word) pairs for words ending at this node
     *   (there can be multiple if the same word is inserted more than once)
     */
    static class TrieNode {
        // Array of 26 children, one per lowercase letter
        TrieNode[] children = new TrieNode[26];

        /**
         * Stores all (insertionIndex, word) pairs for words that end at this node.
         * We keep a list because the same word can be inserted multiple times.
         */
        List<int[]> wordEntries; // each int[] is {insertionIndex}
        List<String> wordStrings; // parallel list storing the actual word strings

        TrieNode() {
            wordEntries = new ArrayList<>();
            wordStrings = new ArrayList<>();
        }
    }

    // =========================================================================
    // Inner class: LogStream
    // =========================================================================

    /**
     * LogStream data structure that supports insert and wildcard query operations.
     *
     * <p>Uses a Trie internally to efficiently store and retrieve words.
     */
    static class LogStream {

        /** Root of the Trie (does not represent any character itself). */
        private final TrieNode root;

        /** Global insertion counter — incremented each time insert() is called. */
        private int insertionCounter;

        /**
         * Initializes the LogStream with an empty Trie.
         */
        LogStream() {
            // Create the root node of the Trie
            root = new TrieNode();
            // Start insertion index at 0
            insertionCounter = 0;
        }

        /**
         * Inserts a word into the Trie and records its insertion index.
         *
         * <p>Algorithm:
         * 1. Start at the root node.
         * 2. For each character in the word, navigate to (or create) the corresponding child node.
         * 3. At the final node, record the current insertionCounter and the word itself.
         * 4. Increment the insertionCounter for the next insertion.
         *
         * @param word The word to insert (all lowercase letters).
         *             Time complexity: O(L) where L is the length of the word.
         *             Space complexity: O(L) for new Trie nodes created.
         */
        void insert(String word) {
            // Step 1: Start traversal from the root
            TrieNode current = root;

            // Step 2: Traverse each character of the word
            for (char c : word.toCharArray()) {
                // Compute the index in the children array (0 for 'a', 1 for 'b', ..., 25 for 'z')
                int idx = c - 'a';

                // If the child node doesn't exist yet, create it
                if (current.children[idx] == null) {
                    current.children[idx] = new TrieNode();
                }

                // Move to the child node
                current = current.children[idx];
            }

            // Step 3: We've reached the end of the word.
            // Record the insertion index and the word at this terminal node.
            current.wordEntries.add(new int[]{insertionCounter});
            current.wordStrings.add(word);

            // Step 4: Increment the global insertion counter for the next word
            insertionCounter++;
        }

        /**
         * Queries the Trie for all words matching the given pattern.
         *
         * <p>The pattern may contain '?' which matches exactly one lowercase letter.
         * Returns [count, index] where:
         * - count = total number of matching words (counting duplicates)
         * - index = 0-based insertion index of the lexicographically smallest matching word,
         *           or -1 if no match exists.
         *
         * <p>Algorithm:
         * 1. Use a recursive DFS helper that traverses the Trie.
         * 2. At each position in the pattern:
         *    - If the character is a letter, follow only that child.
         *    - If the character is '?', follow ALL 26 children (branching).
         * 3. When we reach the end of the pattern, collect all word entries at the current node.
         * 4. After collecting all matches, find the lexicographically smallest word and its index.
         *
         * @param pattern The query pattern (lowercase letters and '?').
         * @return int[] of length 2: [count, insertionIndex of lex-smallest match] or [0, -1].
         *         Time complexity: O(26^Q * L) where Q = number of '?' in pattern, L = pattern length.
         *         In practice much faster due to Trie pruning.
         *         Space complexity: O(L) for the recursion stack depth.
         */
        int[] query(String pattern) {
            // We'll collect all matching (insertionIndex, word) pairs here
            // Each element: int[]{insertionIndex}, String word
            List<int[]> matchedIndices = new ArrayList<>();
            List<String> matchedWords = new ArrayList<>();

            // Step 1: Start the recursive DFS from the root at pattern position 0
            dfsQuery(root, pattern, 0, matchedIndices, matchedWords);

            // Step 2: If no matches found, return [0, -1]
            if (matchedIndices.isEmpty()) {
                return new int[]{0, -1};
            }

            // Step 3: Count total matches
            int count = matchedIndices.size();

            // Step 4: Find the lexicographically smallest word among all matches.
            // If there are ties (same word inserted multiple times), we want the
            // lexicographically smallest word string; among equal strings, the earliest index.
            String smallestWord = null;
            int smallestIndex = -1;

            for (int i = 0; i < matchedWords.size(); i++) {
                String word = matchedWords.get(i);
                int idx = matchedIndices.get(i)[0];

                if (smallestWord == null) {
                    // First match — initialize
                    smallestWord = word;
                    smallestIndex = idx;
                } else {
                    int cmp = word.compareTo(smallestWord);
                    if (cmp < 0) {
                        // Found a lexicographically smaller word
                        smallestWord = word;
                        smallestIndex = idx;
                    } else if (cmp == 0 && idx < smallestIndex) {
                        // Same word but earlier insertion index
                        smallestIndex = idx;
                    }
                }
            }

            // Step 5: Return [count, insertionIndex of lex-smallest match]
            return new int[]{count, smallestIndex};
        }

        /**
         * Recursive DFS helper for wildcard pattern matching in the Trie.
         *
         * <p>At each call, we are at a specific Trie node and a specific position in the pattern.
         * - If we've consumed the entire pattern (pos == pattern.length()), we check if this
         *   node is a terminal node (has word entries) and collect them.
         * - If the current pattern character is a letter, we follow only that child.
         * - If the current pattern character is '?', we follow ALL 26 children.
         *
         * @param node           The current Trie node.
         * @param pattern        The full pattern string.
         * @param pos            The current position in the pattern (0-indexed).
         * @param matchedIndices Accumulator list for matched insertion indices.
         * @param matchedWords   Accumulator list for matched word strings.
         *                       Time complexity: O(26^Q) branching factor where Q = '?' count.
         *                       Space complexity: O(L) recursion depth.
         */
        private void dfsQuery(TrieNode node, String pattern, int pos,
                              List<int[]> matchedIndices, List<String> matchedWords) {

            // Base case: we've matched all characters in the pattern
            if (pos == pattern.length()) {
                // This node is a terminal node if it has word entries
                // Collect all word entries stored at this node
                for (int i = 0; i < node.wordEntries.size(); i++) {
                    matchedIndices.add(node.wordEntries.get(i));
                    matchedWords.add(node.wordStrings.get(i));
                }
                return;
            }

            // Get the current character in the pattern
            char c = pattern.charAt(pos);

            if (c == '?') {
                // Wildcard: try all 26 possible children
                for (int i = 0; i < 26; i++) {
                    if (node.children[i] != null) {
                        // Recurse into this child with the next pattern position
                        dfsQuery(node.children[i], pattern, pos + 1, matchedIndices, matchedWords);
                    }
                }
            } else {
                // Specific letter: follow only the corresponding child
                int idx = c - 'a';
                if (node.children[idx] != null) {
                    dfsQuery(node.children[idx], pattern, pos + 1, matchedIndices, matchedWords);
                }
                // If the child doesn't exist, no match possible — just return
            }
        }
    }

    // =========================================================================
    // Main method: Demonstrates the solution with sample inputs
    // =========================================================================

    /**
     * Main method to demonstrate and verify the LogStream solution.
     *
     * <p>Traces through both examples from the problem description and prints results.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {

        System.out.println("=== Example 1 ===");
        System.out.println("Operations: [\"LogStream\",\"insert\",\"insert\",\"insert\",\"query\",\"query\"]");
        System.out.println("Arguments:  [[],[\"apple\"],[\"apply\"],[\"apt\"],[\"ap?le\"],[\"a??\"]]");
        System.out.println("Expected:   [null, null, null, null, [2, 0], [1, 2]]");
        System.out.println();

        // Create a new LogStream instance
        LogStream ls1 = new LogStream();

        // Insert "apple" at index 0
        ls1.insert("apple");
        System.out.println("insert(\"apple\") -> index 0");

        // Insert "apply" at index 1
        ls1.insert("apply");
        System.out.println("insert(\"apply\") -> index 1");

        // Insert "apt" at index 2
        ls1.insert("apt");
        System.out.println("insert(\"apt\") -> index 2");

        // Query "ap?le":
        // - "apple": a-p-p-l-e vs a-p-?-l-e => '?' matches 'p' => YES (index 0)
        // - "apply": a-p-p-l-y vs a-p-?-l-e => '?' matches 'p', but 'y' != 'e' => NO
        //   Wait, let me re-check: "ap?le" has length 5, "apply" has length 5
        //   a=a, p=p, ?=p (match), l=l, e=y => NO match
        // - "apt": length 3 != 5 => NO
        // So only "apple" matches => count=1? But expected is [2, 0]
        // Let me re-read: "ap?le" matches "apple" AND "apply"
        // "apple": a,p,p,l,e vs a,p,?,l,e => pos0:a=a, pos1:p=p, pos2:?=p(ok), pos3:l=l, pos4:e=e => YES
        // "apply": a,p,p,l,y vs a,p,?,l,e => pos0:a=a, pos1:p=p, pos2:?=p(ok), pos3:l=l, pos4:e!=y => NO
        // Hmm, that gives count=1. But expected is [2,0].
        // Wait, re-reading the problem example:
        // "ap?le" matches "apple" (index 0) and "apply" (index 1). Count=2
        // "apple" = a,p,p,l,e (5 chars)
        // "apply" = a,p,p,l,y (5 chars)
        // "ap?le" = a,p,?,l,e (5 chars)
        // For "apply": a=a, p=p, ?=p(ok), l=l, e vs y => NO
        // This doesn't match. Let me re-read the example more carefully...
        // Oh wait, maybe the example in the problem has a typo or I'm misreading.
        // Let me check: "ap?le" - could '?' be at position 4 (0-indexed)?
        // "ap?le": a(0), p(1), ?(2), l(3), e(4)
        // "apple": a(