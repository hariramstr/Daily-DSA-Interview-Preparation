/*
 * Title: Anagram Group Reunion
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given a list of n strings called words and a separate list of q query strings
 * called queries. For each query string, you must find all strings in words that are
 * anagrams of the query string and return them in the order they appear in words.
 * Two strings are considered anagrams if they contain the same characters with the
 * same frequencies, regardless of order.
 *
 * However, there is an additional twist: after answering each query, the matched words
 * are removed from words permanently, so they cannot be matched again by future queries.
 *
 * Return a list of lists where the i-th list contains all words from words (at the time
 * of the i-th query, after previous removals) that are anagrams of queries[i], in their
 * original relative order.
 *
 * Constraints:
 * - 1 <= n <= 10^4
 * - 1 <= q <= 10^3
 * - 1 <= words[i].length, queries[i].length <= 20
 * - All strings consist of lowercase English letters only.
 *
 * Example 1:
 * Input: words = ["eat", "tea", "tan", "ate", "nat", "bat"], queries = ["ate", "tan"]
 * Output: [["eat", "tea", "ate"], ["tan", "nat"]]
 *
 * Example 2:
 * Input: words = ["abc", "bca", "cab", "xyz", "zyx"], queries = ["abc", "xyz", "abc"]
 * Output: [["abc", "bca", "cab"], ["xyz", "zyx"], []]
 */

import java.util.*;

/**
 * Solution class for the Anagram Group Reunion problem.
 * 
 * Core Idea:
 * - Use a canonical "signature" for each word (sorted characters) to identify anagram groups.
 * - Maintain a LinkedHashMap from signature -> list of words still available.
 * - For each query, compute its signature, retrieve matching words, remove them from the map.
 */
public class Solution {

    /**
     * Computes a canonical signature for a given string by sorting its characters.
     * Two strings that are anagrams will always produce the same signature.
     *
     * For example:
     *   "eat" -> sorted -> "aet"
     *   "tea" -> sorted -> "aet"
     *   "ate" -> sorted -> "aet"
     *
     * @param s the input string (lowercase English letters only)
     * @return a String representing the sorted-character signature of s
     *
     * Time Complexity:  O(L log L) where L is the length of the string (due to sorting)
     * Space Complexity: O(L) for the character array
     */
    private String getSignature(String s) {
        // Step 1: Convert the string to a character array so we can sort it.
        char[] chars = s.toCharArray();

        // Step 2: Sort the character array in ascending order.
        //         This ensures all anagrams map to the same sorted sequence.
        Arrays.sort(chars);

        // Step 3: Convert the sorted char array back to a String and return it.
        //         This is our canonical "key" for the anagram group.
        return new String(chars);
    }

    /**
     * Solves the Anagram Group Reunion problem.
     *
     * Algorithm Overview:
     * 1. Build a LinkedHashMap that maps each anagram signature to the list of words
     *    (in original order) that share that signature.
     * 2. For each query:
     *    a. Compute the query's signature.
     *    b. Look up the signature in the map to get all matching words.
     *    c. If found, add those words to the result for this query, then REMOVE
     *       the entry from the map (so future queries cannot match them again).
     *    d. If not found, add an empty list for this query.
     * 3. Return the collected results.
     *
     * @param words   the list of words to search through (will be conceptually modified)
     * @param queries the list of query strings
     * @return a List of Lists, where the i-th inner list contains all anagrams of
     *         queries[i] found in the remaining words at the time of query i
     *
     * Time Complexity:  O(n * L log L + q * L log L) where n = number of words,
     *                   q = number of queries, L = max string length.
     *                   Building the map: O(n * L log L).
     *                   Processing queries: O(q * L log L) for signature computation.
     * Space Complexity: O(n * L) for storing all words in the map.
     */
    public List<List<String>> anagramGroupReunion(String[] words, String[] queries) {
        // -----------------------------------------------------------------------
        // STEP 1: Build the signature -> words map
        // -----------------------------------------------------------------------
        // We use a LinkedHashMap to preserve insertion order (though for this problem
        // the order of keys doesn't matter; what matters is the order of values within
        // each list, which we maintain by iterating words left-to-right).
        //
        // Key:   sorted-character signature (e.g., "aet" for "eat"/"tea"/"ate")
        // Value: list of original words that have this signature, in original order
        Map<String, List<String>> signatureToWords = new LinkedHashMap<>();

        // Iterate over each word in the original words array
        for (String word : words) {
            // Compute the canonical signature for this word
            String sig = getSignature(word);

            // If this signature hasn't been seen before, create a new list for it
            // computeIfAbsent is a convenient way to do this in one line:
            //   - If sig is already a key, return its existing list
            //   - If sig is NOT a key, create a new ArrayList, put it in the map, return it
            signatureToWords.computeIfAbsent(sig, k -> new ArrayList<>()).add(word);
        }

        // -----------------------------------------------------------------------
        // STEP 2: Process each query
        // -----------------------------------------------------------------------
        // This list will hold one inner list per query
        List<List<String>> results = new ArrayList<>();

        for (String query : queries) {
            // Step 2a: Compute the signature of the current query string
            String querySig = getSignature(query);

            // Step 2b: Look up the signature in our map
            if (signatureToWords.containsKey(querySig)) {
                // Step 2c (found): Retrieve the list of matching words
                List<String> matched = signatureToWords.get(querySig);

                // Add a COPY of the matched list to our results.
                // We copy it because we're about to remove it from the map.
                results.add(new ArrayList<>(matched));

                // IMPORTANT: Remove this entry from the map permanently.
                // This ensures future queries cannot match these words again.
                signatureToWords.remove(querySig);

            } else {
                // Step 2d (not found): No anagrams remain for this query.
                // Add an empty list to represent "no matches".
                results.add(new ArrayList<>());
            }
        }

        // -----------------------------------------------------------------------
        // STEP 3: Return the collected results
        // -----------------------------------------------------------------------
        return results;
    }

    /**
     * Main method to demonstrate the solution with sample inputs from the problem.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1 Trace:
        // words   = ["eat", "tea", "tan", "ate", "nat", "bat"]
        // queries = ["ate", "tan"]
        //
        // Building the map:
        //   "eat" -> sig "aet" -> map: {"aet": ["eat"]}
        //   "tea" -> sig "aet" -> map: {"aet": ["eat", "tea"]}
        //   "tan" -> sig "ant" -> map: {"aet": ["eat","tea"], "ant": ["tan"]}
        //   "ate" -> sig "aet" -> map: {"aet": ["eat","tea","ate"], "ant": ["tan"]}
        //   "nat" -> sig "ant" -> map: {"aet": ["eat","tea","ate"], "ant": ["tan","nat"]}
        //   "bat" -> sig "abt" -> map: {"aet": [...], "ant": [...], "abt": ["bat"]}
        //
        // Query 1: "ate" -> sig "aet" -> found ["eat","tea","ate"] -> remove "aet" from map
        // Query 2: "tan" -> sig "ant" -> found ["tan","nat"] -> remove "ant" from map
        //
        // Expected Output: [["eat", "tea", "ate"], ["tan", "nat"]]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        String[] words1 = {"eat", "tea", "tan", "ate", "nat", "bat"};
        String[] queries1 = {"ate", "tan"};
        List<List<String>> result1 = solution.anagramGroupReunion(words1, queries1);
        System.out.println("Input words:   " + Arrays.toString(words1));
        System.out.println("Input queries: " + Arrays.toString(queries1));
        System.out.println("Output:        " + result1);
        System.out.println("Expected:      [[eat, tea, ate], [tan, nat]]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2 Trace:
        // words   = ["abc", "bca", "cab", "xyz", "zyx"]
        // queries = ["abc", "xyz", "abc"]
        //
        // Building the map:
        //   "abc" -> sig "abc" -> map: {"abc": ["abc"]}
        //   "bca" -> sig "abc" -> map: {"abc": ["abc","bca"]}
        //   "cab" -> sig "abc" -> map: {"abc": ["abc","bca","cab"]}
        //   "xyz" -> sig "xyz" -> map: {"abc": [...], "xyz": ["xyz"]}
        //   "zyx" -> sig "xyz" -> map: {"abc": [...], "xyz": ["xyz","zyx"]}
        //
        // Query 1: "abc" -> sig "abc" -> found ["abc","bca","cab"] -> remove "abc" from map
        // Query 2: "xyz" -> sig "xyz" -> found ["xyz","zyx"] -> remove "xyz" from map
        // Query 3: "abc" -> sig "abc" -> NOT found (already removed) -> []
        //
        // Expected Output: [["abc", "bca", "cab"], ["xyz", "zyx"], []]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        String[] words2 = {"abc", "bca", "cab", "xyz", "zyx"};
        String[] queries2 = {"abc", "xyz", "abc"};
        List<List<String>> result2 = solution.anagramGroupReunion(words2, queries2);
        System.out.println("Input words:   " + Arrays.toString(words2));
        System.out.println("Input queries: " + Arrays.toString(queries2));
        System.out.println("Output:        " + result2);
        System.out.println("Expected:      [[abc, bca, cab], [xyz, zyx], []]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Edge Case: query with no matches at all
        // words   = ["hello", "world"]
        // queries = ["xyz"]
        // Expected: [[]]
        // -----------------------------------------------------------------------
        System.out.println("=== Edge Case: No Matches ===");
        String[] words3 = {"hello", "world"};
        String[] queries3 = {"xyz"};
        List<List<String>> result3 = solution.anagramGroupReunion(words3, queries3);
        System.out.println("Input words:   " + Arrays.toString(words3));
        System.out.println("Input queries: " + Arrays.toString(queries3));
        System.out.println("Output:        " + result3);
        System.out.println("Expected:      [[]]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Edge Case: single word, single query that matches
        // words   = ["listen"]
        // queries = ["silent"]
        // Expected: [["listen"]]
        // -----------------------------------------------------------------------
        System.out.println("=== Edge Case: Single Match ===");
        String[] words4 = {"listen"};
        String[] queries4 = {"silent"};
        List<List<String>> result4 = solution.anagramGroupReunion(words4, queries4);
        System.out.println("Input words:   " + Arrays.toString(words4));
        System.out.println("Input queries: " + Arrays.toString(queries4));
        System.out.println("Output:        " + result4);
        System.out.println("Expected:      [[listen]]");
    }
}