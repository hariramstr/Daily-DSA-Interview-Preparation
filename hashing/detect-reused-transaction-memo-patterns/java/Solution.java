import java.util.*;

/*
Problem Title: Detect Reused Transaction Memo Patterns

Problem Description:
A payments platform stores the free-text memo attached to each transaction as an array of lowercase words.
Two memos are considered to have the same pattern if the sequence of word repetitions is identical,
even if the actual words are different.

For example, the memo ["rent", "paid", "rent", "late"] has the same pattern as
["coffee", "today", "coffee", "again"] because in both memos the 1st and 3rd words are equal,
while the 2nd and 4th words are different from the others.

Given a list of memos, return all indices of memos that belong to a pattern group of size at least 2.
The returned indices must be sorted in increasing order. Two memos can only be grouped together if they
have the same length and the same repetition structure.

Your task is to design an efficient solution using hashing or hash maps to normalize each memo into
a canonical pattern signature.

Constraints:
- 1 <= memos.length <= 100000
- 1 <= memos[i].length <= 100
- 1 <= memos[i][j].length <= 20
- memos[i][j] contains only lowercase English letters
- The sum of all words across all memos does not exceed 300000

Example 1:
Input:
memos = [
  ["rent","paid","rent","late"],
  ["coffee","today","coffee","again"],
  ["taxi","home","taxi","home"],
  ["x","y","x","z"]
]
Output: [0,1,3]

Explanation:
Memo 0, 1, and 3 all map to the same normalized pattern [0,1,0,2].
Memo 2 maps to [0,1,0,1], so it does not belong to that group.

Example 2:
Input:
memos = [["a","b"],["c","c"],["dog","cat"],["hi"],["m","n","m"]]
Output: [0,2]

Explanation:
Memo 0 and memo 2 both have pattern [0,1].
Memo 1 has pattern [0,0], memo 3 has pattern [0], and memo 4 has pattern [0,1,0].
Only indices from groups with at least two memos are returned.
*/

public class Solution {

    /**
     * Finds all memo indices that belong to a pattern group of size at least 2.
     *
     * The core idea:
     * 1. Convert each memo into a canonical pattern signature.
     *    Example:
     *    ["rent","paid","rent","late"] -> "0#1#0#2#"
     *    ["coffee","today","coffee","again"] -> "0#1#0#2#"
     *
     * 2. Group memo indices by that signature using a hash map.
     *
     * 3. Collect indices from groups whose size is at least 2.
     *
     * 4. Sort the final list of indices in increasing order.
     *
     * @param memos the list of memos, where each memo is an array of lowercase words
     * @return a sorted list of indices of memos that share their pattern with at least one other memo
     * Time complexity: O(T + K log K), where T is the total number of words across all memos,
     * and K is the number of returned indices. Building signatures is linear in total words.
     * Space complexity: O(T), due to hash maps, signatures, and grouped indices.
     */
    public List<Integer> findReusedMemoPatternIndices(String[][] memos) {
        // This map groups memo indices by their normalized pattern signature.
        // Key   -> canonical signature such as "0#1#0#2#"
        // Value -> list of memo indices that produce that signature
        Map<String, List<Integer>> groups = new HashMap<>();

        // Process every memo exactly once.
        for (int i = 0; i < memos.length; i++) {
            // Convert the current memo into its canonical repetition pattern.
            String signature = buildPatternSignature(memos[i]);

            // Put the index into the corresponding group.
            groups.computeIfAbsent(signature, k -> new ArrayList<>()).add(i);
        }

        // This list will store all indices that belong to groups of size >= 2.
        List<Integer> result = new ArrayList<>();

        // Examine each group.
        for (List<Integer> indices : groups.values()) {
            // Only groups with at least two memos are valid answer groups.
            if (indices.size() >= 2) {
                result.addAll(indices);
            }
        }

        // The problem requires indices in increasing order.
        Collections.sort(result);

        return result;
    }

    /**
     * Builds a canonical pattern signature for one memo.
     *
     * Example:
     * ["rent","paid","rent","late"]
     *
     * Step-by-step:
     * - "rent" first appears -> assign id 0
     * - "paid" first appears -> assign id 1
     * - "rent" seen before   -> reuse id 0
     * - "late" first appears -> assign id 2
     *
     * Signature becomes: "0#1#0#2#"
     *
     * Why this works:
     * Two memos have the same repetition structure if and only if they produce the same sequence
     * of first-occurrence ids.
     *
     * @param memo one memo represented as an array of words
     * @return a canonical string signature representing the memo's repetition pattern
     * Time complexity: O(L), where L is the number of words in the memo
     * Space complexity: O(U), where U is the number of distinct words in the memo
     */
    public String buildPatternSignature(String[] memo) {
        // This map remembers the first assigned pattern id for each distinct word in this memo.
        Map<String, Integer> wordToId = new HashMap<>();

        // nextId is the next unused integer id for a new word.
        int nextId = 0;

        // StringBuilder is used to efficiently build a hashable signature string.
        StringBuilder signature = new StringBuilder();

        // Walk through the memo from left to right.
        for (String word : memo) {
            // If the word has never been seen in this memo, assign a new id.
            if (!wordToId.containsKey(word)) {
                wordToId.put(word, nextId);
                nextId++;
            }

            // Append the id for this word to the signature.
            // We add a separator '#' so that patterns remain unambiguous.
            // For example, [1, 11] should not look like [11, 1].
            signature.append(wordToId.get(word)).append('#');
        }

        return signature.toString();
    }

    /**
     * Utility method to print a 2D string array in a readable format.
     *
     * @param memos the memo array to print
     * @return a readable string representation of the memo list
     * Time complexity: O(T), where T is the total number of words/characters printed
     * Space complexity: O(T), for the produced string
     */
    public String memosToString(String[][] memos) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');

        for (int i = 0; i < memos.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(Arrays.toString(memos[i]));
        }

        sb.append(']');
        return sb.toString();
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(T + K log K) across the demonstrated examples
     * Space complexity: O(T)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        String[][] memos1 = {
            {"rent", "paid", "rent", "late"},
            {"coffee", "today", "coffee", "again"},
            {"taxi", "home", "taxi", "home"},
            {"x", "y", "x", "z"}
        };

        List<Integer> result1 = solution.findReusedMemoPatternIndices(memos1);
        System.out.println("Example 1 Input: " + solution.memosToString(memos1));
        System.out.println("Example 1 Output: " + result1);
        System.out.println("Expected: [0, 1, 3]");
        System.out.println();

        // Example 2
        String[][] memos2 = {
            {"a", "b"},
            {"c", "c"},
            {"dog", "cat"},
            {"hi"},
            {"m", "n", "m"}
        };

        List<Integer> result2 = solution.findReusedMemoPatternIndices(memos2);
        System.out.println("Example 2 Input: " + solution.memosToString(memos2));
        System.out.println("Example 2 Output: " + result2);
        System.out.println("Expected: [0, 2]");
        System.out.println();

        // Additional quick sanity check
        String[][] memos3 = {
            {"one"},
            {"two"},
            {"a", "a"},
            {"b", "c"},
            {"x", "x"}
        };

        List<Integer> result3 = solution.findReusedMemoPatternIndices(memos3);
        System.out.println("Additional Input: " + solution.memosToString(memos3));
        System.out.println("Additional Output: " + result3);
        System.out.println("Explanation: indices 0 and 1 share pattern [0], indices 2 and 4 share pattern [0,0]");
    }
}