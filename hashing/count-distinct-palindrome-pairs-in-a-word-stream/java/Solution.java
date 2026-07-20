import java.util.*;

/*
 * Title: Count Distinct Palindrome Pairs in a Word Stream
 * Difficulty: Hard
 * Topic: Hashing
 *
 * Problem Description:
 * You are given an array of lowercase strings words, where each string represents a token observed
 * in a live text stream. Two different indices i and j form a valid palindrome pair if i != j and
 * the concatenation words[i] + words[j] is a palindrome. Your task is to count how many distinct
 * ordered index pairs (i, j) are valid.
 *
 * This is not a simple duplicate-checking problem. The input may contain repeated words, empty
 * strings, and words of different lengths. If the same string appears multiple times, each index is
 * treated separately. For example, if words[2] = "ab" and words[5] = "ba", then (2, 5) and (5, 2)
 * must both be evaluated independently because order matters. However, (i, i) is never allowed,
 * even if doubling a word would form a palindrome.
 *
 * A brute-force solution that checks all pairs is too slow for large inputs. An efficient solution
 * is expected, typically using hashing to store reversed words, split positions, or
 * palindrome-compatible prefixes and suffixes.
 *
 * Return the total number of valid ordered pairs.
 *
 * Constraints:
 * - 1 <= words.length <= 10^5
 * - 0 <= words[i].length <= 100
 * - words[i] consists only of lowercase English letters
 * - The sum of all word lengths does not exceed 3 * 10^5
 *
 * Example 1:
 * Input: words = ["bat", "tab", "cat"]
 * Output: 2
 * Explanation: (0, 1) gives "battab", which is a palindrome, and (1, 0) gives "tabbat",
 * which is also a palindrome. No other ordered pair works.
 *
 * Example 2:
 * Input: words = ["", "aba", "xy", "yx", "a"]
 * Output: 6
 * Explanation:
 * Valid ordered pairs are:
 * (0,1), (1,0), (0,4), (4,0), (2,3), (3,2)
 * The statement text says 8, but the listed valid pairs total 6, and checking all ordered pairs
 * confirms the correct answer is 6.
 */

public class Solution {

    /**
     * Counts how many distinct ordered index pairs (i, j) satisfy:
     * - i != j
     * - words[i] + words[j] is a palindrome
     *
     * Core idea:
     * For each word, try every split position:
     *   word = prefix + suffix
     *
     * There are two ways to form a palindrome pair:
     *
     * 1) If prefix is a palindrome, then we need some other word equal to reverse(suffix)
     *    placed before the current word:
     *       reverse(suffix) + word
     *
     * 2) If suffix is a palindrome, then we need some other word equal to reverse(prefix)
     *    placed after the current word:
     *       word + reverse(prefix)
     *
     * Because the input may contain duplicates, we store all indices for each exact word.
     * Then every matching index contributes a distinct ordered pair, except the same index itself.
     *
     * @param words the array of lowercase strings; duplicates and empty strings are allowed
     * @return the total number of valid ordered index pairs
     * Time complexity: O(totalCharacters * maxWordLength), which is acceptable here because each
     * word length is at most 100 and total characters are bounded by 3 * 10^5
     * Space complexity: O(totalCharacters + n) for the hash map and helper storage
     */
    public long countPalindromePairs(String[] words) {
        int n = words.length;

        // Map each exact word to the list of indices where it appears.
        // This is essential because duplicates must be counted separately.
        Map<String, List<Integer>> indicesByWord = new HashMap<>();
        for (int i = 0; i < n; i++) {
            indicesByWord.computeIfAbsent(words[i], k -> new ArrayList<>()).add(i);
        }

        long count = 0L;

        // Process each word independently as the "current" word.
        for (int i = 0; i < n; i++) {
            String word = words[i];
            int len = word.length();

            // Try every split position from 0 to len inclusive.
            // Example: word = "abcd"
            // split = 0 -> "" | "abcd"
            // split = 1 -> "a" | "bcd"
            // split = 2 -> "ab" | "cd"
            // split = 3 -> "abc" | "d"
            // split = 4 -> "abcd" | ""
            for (int split = 0; split <= len; split++) {

                // Case 1:
                // If prefix [0, split-1] is palindrome, then any word equal to reverse(suffix)
                // can be placed BEFORE current word to form a palindrome.
                if (isPalindrome(word, 0, split - 1)) {
                    String neededBefore = reverseSubstring(word, split, len - 1);
                    List<Integer> candidates = indicesByWord.get(neededBefore);

                    if (candidates != null) {
                        // Every candidate index j gives pair (j, i), as long as j != i.
                        for (int j : candidates) {
                            if (j != i) {
                                count++;
                            }
                        }
                    }
                }

                // Case 2:
                // If suffix [split, len-1] is palindrome, then any word equal to reverse(prefix)
                // can be placed AFTER current word to form a palindrome.
                //
                // Important duplicate-avoidance rule:
                // When split == len, suffix is empty and palindrome. But this case mirrors a pair
                // already counted by Case 1 at another split arrangement. To avoid double counting,
                // we only do Case 2 when split < len.
                if (split < len && isPalindrome(word, split, len - 1)) {
                    String neededAfter = reverseSubstring(word, 0, split - 1);
                    List<Integer> candidates = indicesByWord.get(neededAfter);

                    if (candidates != null) {
                        // Every candidate index j gives pair (i, j), as long as j != i.
                        for (int j : candidates) {
                            if (j != i) {
                                count++;
                            }
                        }
                    }
                }
            }
        }

        return count;
    }

    /**
     * Checks whether the substring s[left..right] is a palindrome.
     * An empty range is considered a palindrome.
     *
     * @param s the source string
     * @param left the starting index of the substring
     * @param right the ending index of the substring
     * @return true if the substring is a palindrome; false otherwise
     * Time complexity: O(right - left + 1)
     * Space complexity: O(1)
     */
    public boolean isPalindrome(String s, int left, int right) {
        while (left < right) {
            if (s.charAt(left) != s.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    /**
     * Returns the reversed form of the substring s[left..right].
     * If left > right, this represents an empty substring, so the result is "".
     *
     * @param s the source string
     * @param left the starting index of the substring
     * @param right the ending index of the substring
     * @return the reversed substring
     * Time complexity: O(right - left + 1)
     * Space complexity: O(right - left + 1)
     */
    public String reverseSubstring(String s, int left, int right) {
        if (left > right) {
            return "";
        }

        StringBuilder builder = new StringBuilder(right - left + 1);
        for (int i = right; i >= left; i--) {
            builder.append(s.charAt(i));
        }
        return builder.toString();
    }

    /**
     * Brute-force validator used only for demonstration and sanity checking on small inputs.
     * It checks every ordered pair (i, j), i != j.
     *
     * @param words the array of words
     * @return the exact number of valid ordered palindrome pairs
     * Time complexity: O(n^2 * L), where L is the average concatenated length
     * Space complexity: O(L) due to temporary concatenation
     */
    public long countPalindromePairsBruteForce(String[] words) {
        long count = 0L;
        for (int i = 0; i < words.length; i++) {
            for (int j = 0; j < words.length; j++) {
                if (i == j) {
                    continue;
                }
                String combined = words[i] + words[j];
                if (isPalindrome(combined, 0, combined.length() - 1)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Demonstrates the solution on the sample inputs and prints the results.
     *
     * @param args command-line arguments; not used
     * @return nothing
     * Time complexity: Depends on the sample sizes used in this demo
     * Space complexity: Depends on the sample sizes used in this demo
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] words1 = {"bat", "tab", "cat"};
        long result1 = solution.countPalindromePairs(words1);
        long brute1 = solution.countPalindromePairsBruteForce(words1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Example 1 brute-force check: " + brute1);
        System.out.println("Expected: 2");
        System.out.println();

        String[] words2 = {"", "aba", "xy", "yx", "a"};
        long result2 = solution.countPalindromePairs(words2);
        long brute2 = solution.countPalindromePairsBruteForce(words2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Example 2 brute-force check: " + brute2);
        System.out.println("Correct expected value after verification: 6");
        System.out.println();

        String[] words3 = {"a", ""};
        System.out.println("Extra demo 1: " + solution.countPalindromePairs(words3)); // 2

        String[] words4 = {"ab", "ba", "ab"};
        System.out.println("Extra demo 2: " + solution.countPalindromePairs(words4)); // 4

        String[] words5 = {"", "", "aa"};
        System.out.println("Extra demo 3: " + solution.countPalindromePairs(words5)); // 6
    }
}