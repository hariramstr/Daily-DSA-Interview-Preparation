import java.util.*;

/*
 * Title: Longest Prefix Chain with One-Character Mutations
 *
 * Problem Description:
 * You are given an array of distinct lowercase strings words. A string a can transition to string b
 * if and only if all of the following are true:
 *   1) b is exactly one character longer than a
 *   2) b starts with a prefix that differs from a in at most one position among the first |a| characters
 *   3) the extra character in b may appear only at the end
 *
 * In other words, you may extend a by appending one new character to the end, and while comparing
 * the original positions, you are allowed to mutate at most one existing character.
 *
 * Your task is to compute the length of the longest possible chain of words where each next word is
 * reachable from the previous one by the rule above.
 *
 * Constraints:
 * - 1 <= words.length <= 2 * 10^5
 * - 1 <= words[i].length <= 30
 * - words[i] consists only of lowercase English letters
 * - All words[i] are distinct
 * - The sum of all word lengths does not exceed 2 * 10^6
 *
 * Efficient hashing idea:
 * For a word b of length L, a predecessor a of length L-1 is valid if:
 * - a equals prefix(b, L-1), or
 * - a differs from prefix(b, L-1) in exactly one position.
 *
 * Dynamic programming by length:
 * Let dp[word] = longest chain ending at this word.
 * To compute dp for a word of length L, we need the best dp among words of length L-1 that are:
 * - exact match with its prefix of length L-1
 * - or one-character mutation of that prefix
 *
 * Key optimization:
 * For each length k, maintain:
 * - exactBest[k][string] = best dp for that exact string
 * - maskedBest[k][pattern-with-one-hole] = best dp among all strings that match that pattern
 *
 * Example:
 * For string "abc", its one-hole patterns are:
 *   "*bc", "a*c", "ab*"
 * Any length-3 string differing in at most one position from "abc" will either:
 * - be exactly "abc", or
 * - share at least one of those one-hole patterns.
 *
 * Therefore, for a target word b of length L:
 * - let p = prefix(b, L-1)
 * - best predecessor dp is max(
 *       exactBest[L-1][p],
 *       maskedBest[L-1][each one-hole pattern of p]
 *   )
 *
 * Then dp[b] = bestPredecessor + 1, or 1 if no predecessor exists.
 */
public class Solution {

    /**
     * Computes the length of the longest valid chain.
     *
     * The algorithm processes words grouped by length. For each word:
     * 1. Take its prefix of length len - 1.
     * 2. Query the best predecessor among:
     *    - exact same prefix string
     *    - any string differing in exactly one position from that prefix
     *      (captured by one-hole masked patterns)
     * 3. Set dp[word] = best predecessor + 1, or 1 if none exists.
     * 4. Insert the current word into the data structures for its own length so that
     *    longer words can use it later.
     *
     * Time complexity note:
     * O(totalCharacters * averageWordLength) in the straightforward masking implementation.
     * Since max word length is only 30, this is effectively O(totalCharacters * 30),
     * which is efficient for the given constraints.
     *
     * Space complexity note:
     * O(number of words * maxWordLength) for the hash maps storing exact strings and masked patterns.
     *
     * @param words the array of distinct lowercase words
     * @return the maximum chain length
     */
    public int longestPrefixChain(String[] words) {
        // Group words by length because transitions are only allowed from length k to k + 1.
        // Since maximum word length is small (<= 30), an array of lists is ideal.
        int maxLen = 0;
        for (String word : words) {
            maxLen = Math.max(maxLen, word.length());
        }

        List<String>[] byLength = new ArrayList[maxLen + 1];
        for (int i = 0; i <= maxLen; i++) {
            byLength[i] = new ArrayList<>();
        }
        for (String word : words) {
            byLength[word.length()].add(word);
        }

        // exactBest[len]:
        //   maps an exact string of length len -> best chain length ending at that string
        //
        // maskedBest[len]:
        //   maps a one-hole pattern of length len -> best chain length among all strings
        //   of that length that fit the pattern
        //
        // Example pattern for "abcd":
        //   "*bcd", "a*cd", "ab*d", "abc*"
        //
        // If two strings differ in exactly one position, they share at least one such pattern.
        @SuppressWarnings("unchecked")
        HashMap<String, Integer>[] exactBest = new HashMap[maxLen + 1];
        @SuppressWarnings("unchecked")
        HashMap<String, Integer>[] maskedBest = new HashMap[maxLen + 1];

        for (int i = 0; i <= maxLen; i++) {
            exactBest[i] = new HashMap<>();
            maskedBest[i] = new HashMap<>();
        }

        int answer = 1;

        // Process lengths in increasing order so that when we compute dp for length L,
        // all possible predecessors of length L-1 are already known.
        for (int len = 1; len <= maxLen; len++) {
            for (String word : byLength[len]) {
                int bestPredecessor = 0;

                // A predecessor must have length len - 1.
                if (len > 1) {
                    String prefix = word.substring(0, len - 1);

                    // Case 1: zero mutation allowed.
                    // If an exact word equal to the prefix exists among length len - 1 words,
                    // it is a valid predecessor.
                    bestPredecessor = Math.max(bestPredecessor, exactBest[len - 1].getOrDefault(prefix, 0));

                    // Case 2: one mutation allowed.
                    // We generate all one-hole patterns of the prefix.
                    // Any predecessor differing in exactly one position from the prefix
                    // will match at least one of these patterns.
                    for (int i = 0; i < prefix.length(); i++) {
                        String pattern = buildMaskedPattern(prefix, i);
                        bestPredecessor = Math.max(bestPredecessor, maskedBest[len - 1].getOrDefault(pattern, 0));
                    }
                }

                // If no predecessor exists, the chain starts here with length 1.
                int currentDp = bestPredecessor + 1;
                answer = Math.max(answer, currentDp);

                // Store the best chain ending at this exact word.
                // Words are distinct, but using max keeps the code robust and clear.
                exactBest[len].merge(word, currentDp, Math::max);

                // Store the best chain for every one-hole pattern of this word.
                // This allows future words of length len + 1 to quickly find predecessors
                // that differ by one character from their prefix.
                for (int i = 0; i < word.length(); i++) {
                    String pattern = buildMaskedPattern(word, i);
                    maskedBest[len].merge(pattern, currentDp, Math::max);
                }
            }
        }

        return answer;
    }

    /**
     * Builds a masked pattern by replacing the character at the given index with '*'.
     *
     * Example:
     * buildMaskedPattern("abcd", 2) -> "ab*d"
     *
     * This pattern is used as a hash key so that strings differing in exactly one position
     * can be matched efficiently.
     *
     * Time complexity note:
     * O(length of word)
     *
     * Space complexity note:
     * O(length of word) for the created pattern string
     *
     * @param word the original word
     * @param index the position to mask
     * @return the masked pattern string
     */
    public String buildMaskedPattern(String word, int index) {
        char[] chars = word.toCharArray();
        chars[index] = '*';
        return new String(chars);
    }

    /**
     * Demonstrates the solution on sample-style inputs and prints the results.
     *
     * Time complexity note:
     * Dominated by calls to longestPrefixChain.
     *
     * Space complexity note:
     * Dominated by calls to longestPrefixChain.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] words1 = {"a", "ab", "ac", "abc", "acc", "abca", "acca"};
        int result1 = solution.longestPrefixChain(words1);
        System.out.println("Example 1 result: " + result1);
        // Expected: 5
        // One valid chain:
        // "a" -> "ab" -> "abc" -> "abca" -> "acca"

        String[] words2 = {"cat", "bat", "bate", "bath", "batch", "catch", "cater"};
        int result2 = solution.longestPrefixChain(words2);
        System.out.println("Example 2 result: " + result2);
        // Based on the stated expected output in the prompt:
        // Expected: 3

        // Additional small sanity checks
        String[] words3 = {"a"};
        System.out.println("Single word result: " + solution.longestPrefixChain(words3));
        // Expected: 1

        String[] words4 = {"a", "bb", "ccc"};
        System.out.println("No valid chain beyond length 1 result: " + solution.longestPrefixChain(words4));
        // Expected: 1

        String[] words5 = {"a", "ab", "bb", "bbc", "bcc", "bcca"};
        System.out.println("Additional test result: " + solution.longestPrefixChain(words5));
    }
}