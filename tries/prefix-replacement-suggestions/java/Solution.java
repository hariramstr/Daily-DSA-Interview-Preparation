import java.util.*;

/*
 * Prefix Replacement Suggestions
 *
 * Problem Description:
 * You are building a text normalization tool for a search system. A dictionary of approved
 * root words is given, along with a list of query words typed by users. For each query word,
 * you must find the shortest dictionary root that is a prefix of that query. If such a root
 * exists, replace the query word with that root; otherwise keep the original word unchanged.
 * In addition to returning the transformed list, you must also report how many query words
 * were actually replaced.
 *
 * Your task is to implement a function that takes two arrays: roots and queries. Each string
 * contains only lowercase English letters. For every word in queries, search the dictionary
 * for a prefix match. If multiple roots match, always choose the shortest one. This requirement
 * makes a Trie-based solution especially suitable, because it allows efficient prefix traversal
 * and early stopping when a terminal root is found.
 *
 * Return both the transformed array and the number of replacements performed.
 *
 * Constraints:
 * - 1 <= roots.length, queries.length <= 2 * 10^4
 * - 1 <= roots[i].length, queries[i].length <= 100
 * - All strings consist only of lowercase English letters
 * - The total number of characters across all strings does not exceed 2 * 10^5
 * - Roots may contain duplicates; duplicates should not affect the result
 *
 * Example 1:
 * Input:
 * roots = ["cat", "bat", "rat"]
 * queries = ["cattle", "battery", "rattle", "dog"]
 * Output:
 * transformed = ["cat", "bat", "rat", "dog"]
 * replacedCount = 3
 *
 * Example 2:
 * Input:
 * roots = ["a", "ab", "abc", "bcd"]
 * queries = ["abcde", "abacus", "bcdx", "zzz"]
 * Output:
 * transformed = ["a", "a", "bcd", "zzz"]
 * replacedCount = 3
 */

public class Solution {

    /**
     * Trie node used to store lowercase English root words.
     * Each node has up to 26 children and a flag indicating whether
     * a complete root word ends at this node.
     */
    private static class TrieNode {
        TrieNode[] children = new TrieNode[26];
        boolean isWord;
    }

    /**
     * Result object that contains both the transformed queries and
     * the number of words that were actually replaced.
     */
    public static class ReplacementResult {
        public final String[] transformed;
        public final int replacedCount;

        /**
         * Creates a result container.
         *
         * @param transformed the final transformed query array
         * @param replacedCount the number of queries that were replaced by a root
         */
        public ReplacementResult(String[] transformed, int replacedCount) {
            this.transformed = transformed;
            this.replacedCount = replacedCount;
        }
    }

    /**
     * Builds a Trie from the given root dictionary and then transforms each query
     * by replacing it with the shortest root that is a prefix of that query.
     *
     * Time complexity:
     * O(total characters in roots + total characters in queries)
     *
     * Space complexity:
     * O(total characters in roots) for the Trie, plus O(queries.length) for output storage
     *
     * @param roots the dictionary of approved root words; duplicates are allowed
     * @param queries the list of query words to normalize
     * @return a ReplacementResult containing the transformed array and replacement count
     */
    public ReplacementResult replaceWithShortestRoots(String[] roots, String[] queries) {
        // Step 1:
        // Create the Trie root node. This is the starting point for all insertions
        // and all prefix searches.
        TrieNode trieRoot = new TrieNode();

        // Step 2:
        // Insert every root word into the Trie.
        // Duplicates do not cause any issue because inserting the same word again
        // simply walks the same path and marks the same terminal node as a word.
        for (String root : roots) {
            insertRoot(trieRoot, root);
        }

        // Step 3:
        // Prepare the output array. It will have the same length as the queries array,
        // because each input query produces exactly one output string.
        String[] transformed = new String[queries.length];

        // Step 4:
        // Count how many queries were actually replaced.
        int replacedCount = 0;

        // Step 5:
        // Process each query independently.
        for (int i = 0; i < queries.length; i++) {
            String original = queries[i];

            // Find the shortest root that is a prefix of the current query.
            // If no such root exists, this method returns the original word unchanged.
            String replacement = findShortestPrefixOrOriginal(trieRoot, original);

            // Store the result.
            transformed[i] = replacement;

            // If the returned string is different from the original query,
            // then a replacement happened.
            if (!replacement.equals(original)) {
                replacedCount++;
            }
        }

        // Step 6:
        // Return both the transformed array and the number of replacements.
        return new ReplacementResult(transformed, replacedCount);
    }

    /**
     * Inserts one root word into the Trie.
     *
     * Time complexity:
     * O(L), where L is the length of the root
     *
     * Space complexity:
     * O(L) in the worst case if all nodes on the path are newly created
     *
     * @param rootNode the Trie root
     * @param word the root word to insert
     * @return nothing
     */
    public void insertRoot(TrieNode rootNode, String word) {
        // Start from the Trie root.
        TrieNode current = rootNode;

        // Walk character by character through the word.
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);

            // Convert the lowercase letter into an index from 0 to 25.
            int index = ch - 'a';

            // If the next node does not exist yet, create it.
            if (current.children[index] == null) {
                current.children[index] = new TrieNode();
            }

            // Move to the child node for the next iteration.
            current = current.children[index];
        }

        // After processing all characters, mark the final node as a complete root word.
        current.isWord = true;
    }

    /**
     * Searches the Trie for the shortest root that is a prefix of the given query.
     * If found, returns that shortest root. Otherwise, returns the original query unchanged.
     *
     * Time complexity:
     * O(L), where L is the length of the query
     *
     * Space complexity:
     * O(L) for the StringBuilder used to build the prefix
     *
     * @param rootNode the Trie root containing all dictionary roots
     * @param query the query word to check
     * @return the shortest matching root if one exists; otherwise the original query
     */
    public String findShortestPrefixOrOriginal(TrieNode rootNode, String query) {
        // Start traversal from the Trie root.
        TrieNode current = rootNode;

        // We build the prefix as we walk through the query.
        // The moment we reach a Trie node marked as a complete word,
        // we can stop immediately because that is the shortest matching root.
        StringBuilder prefix = new StringBuilder();

        // Process the query one character at a time.
        for (int i = 0; i < query.length(); i++) {
            char ch = query.charAt(i);
            int index = ch - 'a';

            // If the required child does not exist, then no root in the dictionary
            // matches this query as a prefix. So we must keep the original query.
            if (current.children[index] == null) {
                return query;
            }

            // Move down one level in the Trie.
            current = current.children[index];

            // Add the current character to the prefix we are building.
            prefix.append(ch);

            // IMPORTANT:
            // As soon as we reach a node that marks the end of a root word,
            // we return immediately. Because we are traversing from left to right,
            // this is guaranteed to be the shortest matching root.
            if (current.isWord) {
                return prefix.toString();
            }
        }

        // If we finished scanning the entire query and never hit a terminal root node,
        // then there is no dictionary root that is a prefix of the query.
        // Return the original query unchanged.
        return query;
    }

    /**
     * Utility method to print a result in a beginner-friendly format.
     *
     * Time complexity:
     * O(n), where n is the number of transformed words
     *
     * Space complexity:
     * O(1) extra space, ignoring output buffering
     *
     * @param result the replacement result to print
     * @return nothing
     */
    public static void printResult(ReplacementResult result) {
        System.out.println("transformed = " + Arrays.toString(result.transformed));
        System.out.println("replacedCount = " + result.replacedCount);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * Time complexity:
     * O(total characters in demo roots + total characters in demo queries)
     *
     * Space complexity:
     * O(total characters in demo roots) for the Trie and O(number of queries) for outputs
     *
     * @param args command-line arguments (not used)
     * @return nothing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        String[] roots1 = {"cat", "bat", "rat"};
        String[] queries1 = {"cattle", "battery", "rattle", "dog"};

        ReplacementResult result1 = solution.replaceWithShortestRoots(roots1, queries1);

        System.out.println("Example 1:");
        printResult(result1);
        System.out.println();

        // Expected:
        // transformed = [cat, bat, rat, dog]
        // replacedCount = 3

        // Example 2
        String[] roots2 = {"a", "ab", "abc", "bcd"};
        String[] queries2 = {"abcde", "abacus", "bcdx", "zzz"};

        ReplacementResult result2 = solution.replaceWithShortestRoots(roots2, queries2);

        System.out.println("Example 2:");
        printResult(result2);
        System.out.println();

        // Expected:
        // transformed = [a, a, bcd, zzz]
        // replacedCount = 3

        // Additional small demonstration showing duplicates do not affect correctness.
        String[] roots3 = {"cat", "cat", "c", "car"};
        String[] queries3 = {"cattle", "carpet", "dog"};

        ReplacementResult result3 = solution.replaceWithShortestRoots(roots3, queries3);

        System.out.println("Additional Example:");
        printResult(result3);
        // Expected:
        // transformed = [c, c, dog]
        // replacedCount = 2
    }
}