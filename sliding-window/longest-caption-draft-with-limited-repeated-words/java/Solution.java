import java.util.*;

/*
 * Title: Longest Caption Draft With Limited Repeated Words
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A social media team is drafting a caption represented as an array of lowercase words `words`,
 * where `words[i]` is the ith word in order. To keep the caption varied, the team wants to select
 * one contiguous block of words such that no single distinct word appears more than `k` times
 * inside that block.
 *
 * Return the length of the longest contiguous subarray of `words` that satisfies this rule.
 *
 * In other words, among all windows `words[l...r]`, find the maximum size of a window where the
 * frequency of every word in that window is at most `k`.
 *
 * This problem should be solved efficiently for large inputs, so an approach that checks every
 * possible subarray will not pass. A sliding window with frequency tracking is expected.
 *
 * Constraints:
 * - 1 <= words.length <= 200000
 * - 1 <= words[i].length <= 20
 * - words[i] contains only lowercase English letters
 * - 1 <= k <= words.length
 *
 * Example 1:
 * Input: words = ["sale","new","sale","trend","sale","new"], k = 2
 * Output: 4
 * Explanation: One valid longest window is ["new","sale","trend","sale"].
 * In this window, "sale" appears 2 times, and every other word appears at most 1 time.
 * Any longer window would contain "sale" 3 times.
 *
 * Example 2:
 * Input: words = ["a","b","a","c","b","b","d"], k = 1
 * Output: 3
 * Explanation: Since each word may appear at most once, the answer is the longest contiguous
 * block of distinct words. Valid windows of length 3 include ["a","c","b"] and ["c","b","d"].
 * No valid window of length 4 exists.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray in which every distinct word
     * appears at most {@code k} times.
     *
     * The method uses the classic sliding window technique:
     * - Expand the right boundary one word at a time.
     * - Track frequencies of words inside the current window.
     * - If adding a word makes its frequency exceed {@code k}, move the left boundary
     *   forward until the window becomes valid again.
     * - Record the maximum valid window length seen so far.
     *
     * Why this works:
     * - At every step, the window is adjusted so that it always satisfies the rule.
     * - Because each index moves from left to right at most once, the algorithm is efficient.
     *
     * @param words the array of lowercase words representing the caption draft
     * @param k the maximum allowed frequency for any single distinct word inside a valid window
     * @return the length of the longest valid contiguous subarray
     *
     * Time complexity: O(n), where n is the number of words, because each word is added to
     * and removed from the sliding window at most once.
     * Space complexity: O(m), where m is the number of distinct words currently tracked
     * in the frequency map, up to O(n) in the worst case.
     */
    public int longestCaptionDraft(String[] words, int k) {
        // Frequency map:
        // key   -> a word currently inside the sliding window
        // value -> how many times that word appears in the current window
        Map<String, Integer> frequency = new HashMap<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < words.length; right++) {
            String currentWord = words[right];

            // Step 1: include the new word at position 'right' into the window.
            frequency.put(currentWord, frequency.getOrDefault(currentWord, 0) + 1);

            // Step 2: if the newly added word now appears more than k times,
            // the window is invalid.
            //
            // Important observation:
            // Before adding words[right], the window was already valid.
            // Therefore, the only possible violation after adding one word is that
            // this specific word's count became k + 1.
            //
            // So we only need to shrink while this word exceeds the allowed limit.
            while (frequency.get(currentWord) > k) {
                String leftWord = words[left];

                // Remove the word at the left boundary from the window.
                frequency.put(leftWord, frequency.get(leftWord) - 1);

                // Optional cleanup:
                // If a word's count becomes zero, remove it from the map.
                // This is not required for correctness, but keeps the map cleaner.
                if (frequency.get(leftWord) == 0) {
                    frequency.remove(leftWord);
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // Step 3: now the window [left...right] is valid again.
            // Compute its length and update the best answer if needed.
            int currentLength = right - left + 1;
            maxLength = Math.max(maxLength, currentLength);
        }

        return maxLength;
    }

    /**
     * A small helper method that prints an input case and the computed result.
     * This is used by {@link #main(String[])} to demonstrate the algorithm clearly.
     *
     * @param words the input array of words
     * @param k the maximum allowed frequency for any word in the chosen window
     * @return the computed longest valid window length
     *
     * Time complexity: O(n), delegated to the main algorithm.
     * Space complexity: O(m), delegated to the main algorithm.
     */
    public int demonstrateCase(String[] words, int k) {
        int result = longestCaptionDraft(words, k);
        System.out.println("Words: " + Arrays.toString(words));
        System.out.println("k = " + k);
        System.out.println("Longest valid contiguous subarray length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * - Example 1 -> 4
     * - Example 2 -> 3
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated test case.
     * Space complexity: O(m) per demonstrated test case.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // ["sale","new","sale","trend","sale","new"], k = 2
        // A longest valid window has length 4.
        String[] words1 = {"sale", "new", "sale", "trend", "sale", "new"};
        int k1 = 2;
        int result1 = solution.demonstrateCase(words1, k1);
        System.out.println("Expected: 4, Actual: " + result1);
        System.out.println();

        // Example 2:
        // ["a","b","a","c","b","b","d"], k = 1
        // A longest valid window has length 3.
        String[] words2 = {"a", "b", "a", "c", "b", "b", "d"};
        int k2 = 1;
        int result2 = solution.demonstrateCase(words2, k2);
        System.out.println("Expected: 3, Actual: " + result2);
        System.out.println();

        // Additional quick sanity checks for beginners:
        String[] words3 = {"x"};
        int k3 = 1;
        int result3 = solution.demonstrateCase(words3, k3);
        System.out.println("Expected: 1, Actual: " + result3);
        System.out.println();

        String[] words4 = {"a", "a", "a", "a"};
        int k4 = 2;
        int result4 = solution.demonstrateCase(words4, k4);
        System.out.println("Expected: 2, Actual: " + result4);
    }
}