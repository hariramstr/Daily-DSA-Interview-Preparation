/*
 * Title: Group Anagram Chains by Frequency
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given a list of words. Two words are considered **anagram siblings** if one can be
 * rearranged to form the other (i.e., they contain the same characters with the same frequencies).
 * Your task is to group all words into anagram families and then return the groups **sorted by
 * their size in descending order**. If two groups have the same size, sort them lexicographically
 * by their smallest word in ascending order.
 *
 * Additionally, for each group, return the words sorted in **lexicographic order**.
 *
 * Constraints:
 * - 1 <= words.length <= 10^4
 * - 1 <= words[i].length <= 20
 * - All characters in words[i] are lowercase English letters.
 * - Words in the input may contain duplicates; duplicate words belong to the same group.
 *
 * Example 1:
 * Input: words = ["eat", "tea", "tan", "ate", "nat", "bat"]
 * Output: [["ate", "eat", "tea"], ["nat", "tan"], ["bat"]]
 *
 * Example 2:
 * Input: words = ["abc", "bca", "xyz", "zyx", "cab", "yzx"]
 * Output: [["abc", "bca", "cab"], ["xyz", "yzx", "zyx"]]
 */

import java.util.*;

/**
 * Solution class for the "Group Anagram Chains by Frequency" problem.
 *
 * <p>Core Idea:
 * Two words are anagrams if and only if their sorted character sequences are identical.
 * For example, "eat", "tea", and "ate" all sort to "aet".
 * We use this sorted form as a HashMap key to bucket words into anagram groups.
 * After grouping, we sort each group lexicographically, then sort the list of groups
 * by descending size (tie-broken by the lexicographically smallest word in each group).
 */
public class Solution {

    /**
     * Groups the given words into anagram families, sorts each group lexicographically,
     * and returns the list of groups sorted by descending size (ties broken by the
     * lexicographically smallest word in each group, ascending).
     *
     * @param words an array of lowercase English words (may contain duplicates)
     * @return a list of groups, where each group is a list of anagram siblings sorted
     *         lexicographically; the outer list is sorted by descending group size,
     *         then ascending lexicographic order of the smallest word in each group
     *
     * Time Complexity:  O(N * K * log K) where N = number of words, K = max word length.
     *                   Sorting each word costs O(K log K); we do this for all N words.
     *                   Sorting the final groups costs O(G log G) where G <= N.
     * Space Complexity: O(N * K) for storing all words in the HashMap and result list.
     */
    public List<List<String>> groupAnagrams(String[] words) {

        // -----------------------------------------------------------------------
        // STEP 1: Create a HashMap that maps a "canonical key" to a list of words.
        //         The canonical key for a word is its characters sorted alphabetically.
        //         All anagrams share the same canonical key.
        //         Example: "eat" -> "aet", "tea" -> "aet", "ate" -> "aet"
        // -----------------------------------------------------------------------
        Map<String, List<String>> anagramMap = new HashMap<>();

        // -----------------------------------------------------------------------
        // STEP 2: Iterate over every word in the input array.
        // -----------------------------------------------------------------------
        for (String word : words) {

            // -- STEP 2a: Compute the canonical key for this word.
            //    Convert the word to a char array so we can sort it.
            char[] chars = word.toCharArray();

            // Sort the characters alphabetically (e.g., "eat" -> ['a','e','t']).
            Arrays.sort(chars);

            // Convert the sorted char array back to a String to use as the map key.
            String key = new String(chars);

            // -- STEP 2b: If this key doesn't exist in the map yet, create a new list.
            //    computeIfAbsent is a convenient way to do this in one line:
            //    it inserts a new ArrayList only when the key is absent, then returns
            //    the (possibly newly created) list.
            anagramMap.computeIfAbsent(key, k -> new ArrayList<>());

            // -- STEP 2c: Add the original word (not the sorted key) to its group.
            anagramMap.get(key).add(word);
        }

        // -----------------------------------------------------------------------
        // STEP 3: Sort each individual group lexicographically.
        //         We want the words within each group to appear in alphabetical order.
        //         Example: ["eat", "tea", "ate"] -> ["ate", "eat", "tea"]
        // -----------------------------------------------------------------------
        for (List<String> group : anagramMap.values()) {
            Collections.sort(group); // natural String ordering = lexicographic
        }

        // -----------------------------------------------------------------------
        // STEP 4: Collect all groups into a result list so we can sort the groups
        //         themselves.
        // -----------------------------------------------------------------------
        List<List<String>> result = new ArrayList<>(anagramMap.values());

        // -----------------------------------------------------------------------
        // STEP 5: Sort the list of groups using a custom comparator:
        //
        //   Primary criterion:   Descending group size
        //                        (larger groups come first)
        //   Secondary criterion: Ascending lexicographic order of the smallest word
        //                        in each group (the first element after step 3).
        //
        //   Comparator logic:
        //     - Compare b.size() vs a.size() for descending size order.
        //     - If sizes are equal, compare a.get(0) vs b.get(0) for ascending lex order.
        // -----------------------------------------------------------------------
        result.sort((groupA, groupB) -> {
            // Primary: larger group first (descending size)
            int sizeComparison = Integer.compare(groupB.size(), groupA.size());
            if (sizeComparison != 0) {
                return sizeComparison; // different sizes: put bigger group first
            }
            // Secondary: if same size, compare the smallest word in each group
            // (groups are already sorted, so index 0 holds the smallest word)
            return groupA.get(0).compareTo(groupB.get(0)); // ascending lex order
        });

        // -----------------------------------------------------------------------
        // STEP 6: Return the fully sorted result.
        // -----------------------------------------------------------------------
        return result;
    }

    // ===========================================================================
    // Helper method to print results in a readable format
    // ===========================================================================

    /**
     * Formats a list of string groups as a human-readable string.
     *
     * @param groups the list of groups to format
     * @return a string representation like [["ate", "eat", "tea"], ["bat"]]
     *
     * Time Complexity:  O(N * K) where N = total words, K = average word length
     * Space Complexity: O(N * K) for the StringBuilder
     */
    public static String formatResult(List<List<String>> groups) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < groups.size(); i++) {
            sb.append("[");
            List<String> group = groups.get(i);
            for (int j = 0; j < group.size(); j++) {
                sb.append("\"").append(group.get(j)).append("\"");
                if (j < group.size() - 1) sb.append(", ");
            }
            sb.append("]");
            if (i < groups.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    // ===========================================================================
    // Main method: demonstrates the solution with the provided examples
    // ===========================================================================

    /**
     * Entry point — runs both example test cases and prints the results.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution solution = new Solution();

        // -------------------------------------------------------------------
        // Example 1
        // Input:  ["eat", "tea", "tan", "ate", "nat", "bat"]
        // Expected Output: [["ate", "eat", "tea"], ["nat", "tan"], ["bat"]]
        //
        // Trace:
        //   "eat" -> sorted "aet" -> group {"aet": ["eat"]}
        //   "tea" -> sorted "aet" -> group {"aet": ["eat","tea"]}
        //   "tan" -> sorted "ant" -> group {"ant": ["tan"]}
        //   "ate" -> sorted "aet" -> group {"aet": ["eat","tea","ate"]}
        //   "nat" -> sorted "ant" -> group {"ant": ["tan","nat"]}
        //   "bat" -> sorted "abt" -> group {"abt": ["bat"]}
        //
        //   After sorting each group lexicographically:
        //     "aet" -> ["ate","eat","tea"]
        //     "ant" -> ["nat","tan"]
        //     "abt" -> ["bat"]
        //
        //   After sorting groups by descending size (tie-break by smallest word):
        //     size 3: ["ate","eat","tea"]
        //     size 2: ["nat","tan"]
        //     size 1: ["bat"]
        //
        //   Result: [["ate","eat","tea"],["nat","tan"],["bat"]] ✓
        // -------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        String[] words1 = {"eat", "tea", "tan", "ate", "nat", "bat"};
        System.out.println("Input:    " + Arrays.toString(words1));
        List<List<String>> result1 = solution.groupAnagrams(words1);
        System.out.println("Output:   " + formatResult(result1));
        System.out.println("Expected: [[\"ate\", \"eat\", \"tea\"], [\"nat\", \"tan\"], [\"bat\"]]");
        System.out.println();

        // -------------------------------------------------------------------
        // Example 2
        // Input:  ["abc", "bca", "xyz", "zyx", "cab", "yzx"]
        // Expected Output: [["abc", "bca", "cab"], ["xyz", "yzx", "zyx"]]
        //
        // Trace:
        //   "abc" -> sorted "abc" -> group {"abc": ["abc"]}
        //   "bca" -> sorted "abc" -> group {"abc": ["abc","bca"]}
        //   "xyz" -> sorted "xyz" -> group {"xyz": ["xyz"]}
        //   "zyx" -> sorted "xyz" -> group {"xyz": ["xyz","zyx"]}
        //   "cab" -> sorted "abc" -> group {"abc": ["abc","bca","cab"]}
        //   "yzx" -> sorted "xyz" -> group {"xyz": ["xyz","zyx","yzx"]}
        //
        //   After sorting each group lexicographically:
        //     "abc" -> ["abc","bca","cab"]
        //     "xyz" -> ["xyz","yzx","zyx"]
        //
        //   Both groups have size 3. Tie-break by smallest word:
        //     "abc" < "xyz" lexicographically -> ["abc","bca","cab"] comes first
        //
        //   Result: [["abc","bca","cab"],["xyz","yzx","zyx"]] ✓
        // -------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        String[] words2 = {"abc", "bca", "xyz", "zyx", "cab", "yzx"};
        System.out.println("Input:    " + Arrays.toString(words2));
        List<List<String>> result2 = solution.groupAnagrams(words2);
        System.out.println("Output:   " + formatResult(result2));
        System.out.println("Expected: [[\"abc\", \"bca\", \"cab\"], [\"xyz\", \"yzx\", \"zyx\"]]");
        System.out.println();

        // -------------------------------------------------------------------
        // Example 3: Edge case — single word
        // -------------------------------------------------------------------
        System.out.println("=== Example 3 (single word) ===");
        String[] words3 = {"hello"};
        System.out.println("Input:    " + Arrays.toString(words3));
        List<List<String>> result3 = solution.groupAnagrams(words3);
        System.out.println("Output:   " + formatResult(result3));
        System.out.println("Expected: [[\"hello\"]]");
        System.out.println();

        // -------------------------------------------------------------------
        // Example 4: Duplicate words — duplicates belong to the same group
        // -------------------------------------------------------------------
        System.out.println("=== Example 4 (duplicates) ===");
        String[] words4 = {"abc", "abc", "bca"};
        System.out.println("Input:    " + Arrays.toString(words4));
        List<List<String>> result4 = solution.groupAnagrams(words4);
        System.out.println("Output:   " + formatResult(result4));
        System.out.println("Expected: [[\"abc\", \"abc\", \"bca\"]]");
        System.out.println();

        // -------------------------------------------------------------------
        // Example 5: All words are unique non-anagrams
        // -------------------------------------------------------------------
        System.out.println("=== Example 5 (no anagrams) ===");
        String[] words5 = {"dog", "cat", "bird"};
        System.out.println("Input:    " + Arrays.toString(words5));
        List<List<String>> result5 = solution.groupAnagrams(words5);
        System.out.println("Output:   " + formatResult(result5));
        System.out.println("Expected: [[\"bird\"], [\"cat\"], [\"dog\"]] (all size 1, sorted by smallest word)");
    }
}