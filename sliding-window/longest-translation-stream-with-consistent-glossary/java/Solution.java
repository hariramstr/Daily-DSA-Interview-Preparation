import java.util.*;

/*
 * Title: Longest Translation Stream With Consistent Glossary
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A localization platform stores a stream of translated terms as an array of strings `words`,
 * where each element is the translated form chosen for the next occurrence of a concept in a
 * document. Reviewers want to find the longest contiguous segment that is "glossary-consistent":
 * within that segment, no translated term appears more than `k` times. This helps detect the
 * largest section that can be approved without any single wording being overused beyond the
 * allowed glossary repetition limit.
 *
 * Given an array `words` and an integer `k`, return the length of the longest contiguous subarray
 * such that every distinct string inside the subarray appears at most `k` times.
 *
 * A subarray is a contiguous part of the array. If `k = 0`, the answer is always `0` because no
 * word may appear even once.
 *
 * Constraints:
 * - 1 <= words.length <= 100000
 * - 0 <= k <= words.length
 * - 1 <= words[i].length <= 20
 * - words[i] consists of lowercase English letters
 *
 * Example 1:
 * Input: words = ["uno","dos","uno","tres","dos","uno","cuatro"], k = 2
 * Output: 5
 * Explanation: The longest valid segment is ["uno","dos","uno","tres","dos"].
 * In this segment, "uno" appears 2 times, "dos" appears 2 times, and "tres" appears 1 time.
 *
 * Example 2:
 * Input: words = ["bleu","bleu","bleu","vert","vert","jaune"], k = 1
 * Output: 3
 * Explanation: One longest valid segment is ["bleu","vert","jaune"].
 * Every distinct word appears at most once in the window.
 */

public class Solution {

    /**
     * Returns the length of the longest contiguous subarray such that every distinct word
     * appears at most {@code k} times.
     *
     * The algorithm uses the classic sliding window technique:
     * - Expand the right boundary one word at a time.
     * - Track frequencies of words inside the current window.
     * - If adding a word makes its frequency exceed {@code k}, move the left boundary rightward
     *   until the window becomes valid again.
     * - Record the maximum valid window length seen so far.
     *
     * @param words the array of translated terms
     * @param k the maximum allowed frequency for any distinct word inside a valid subarray
     * @return the length of the longest valid contiguous subarray
     *
     * Time complexity: O(n), where n is words.length, because each index moves at most once
     * through the array as part of the sliding window.
     * Space complexity: O(m), where m is the number of distinct words currently tracked in the map
     * (at most n in the worst case).
     */
    public int longestConsistentGlossarySegment(String[] words, int k) {
        // Special case:
        // If k is 0, then no word may appear even once.
        // That means no non-empty subarray can ever be valid, so the answer is 0 immediately.
        if (k == 0) {
            return 0;
        }

        // This map stores the frequency of each word inside the current sliding window.
        Map<String, Integer> frequencyMap = new HashMap<>();

        // left = left boundary of the current window
        int left = 0;

        // maxLength = best valid window length found so far
        int maxLength = 0;

        // We expand the window by moving 'right' from left to right across the array.
        for (int right = 0; right < words.length; right++) {
            String currentWord = words[right];

            // Add the current word to the window by increasing its count.
            frequencyMap.put(currentWord, frequencyMap.getOrDefault(currentWord, 0) + 1);

            // If the current word now appears more than k times,
            // the window is invalid.
            //
            // Important observation:
            // Before adding words[right], the window was valid.
            // After adding it, only the count of currentWord changed.
            // Therefore, if the window becomes invalid, it is specifically because
            // currentWord exceeded k.
            //
            // So we shrink the window from the left until currentWord's count is back to <= k.
            while (frequencyMap.get(currentWord) > k) {
                String leftWord = words[left];

                // Remove one occurrence of the word at the left boundary.
                frequencyMap.put(leftWord, frequencyMap.get(leftWord) - 1);

                // If its count becomes 0, remove it from the map to keep the map clean.
                if (frequencyMap.get(leftWord) == 0) {
                    frequencyMap.remove(leftWord);
                }

                // Move the left boundary rightward, effectively shrinking the window.
                left++;
            }

            // At this point, the window [left, right] is valid:
            // every word appears at most k times.
            int currentWindowLength = right - left + 1;

            // Update the best answer if this valid window is longer.
            if (currentWindowLength > maxLength) {
                maxLength = currentWindowLength;
            }
        }

        return maxLength;
    }

    /**
     * A helper method that runs the algorithm on one test case and prints the result.
     * This is useful for demonstration in the main method.
     *
     * @param words the array of translated terms
     * @param k the maximum allowed frequency for any distinct word
     * @return the computed longest valid segment length
     *
     * Time complexity: O(n), where n is words.length.
     * Space complexity: O(m), where m is the number of distinct words tracked.
     */
    public int demonstrateAndPrint(String[] words, int k) {
        int result = longestConsistentGlossarySegment(words, k);
        System.out.println("Words: " + Arrays.toString(words));
        System.out.println("k = " + k);
        System.out.println("Longest consistent glossary segment length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution using the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: Depends on the total size of the demonstrated test cases.
     * Space complexity: Depends on the largest test case demonstrated.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1:
        // ["uno","dos","uno","tres","dos","uno","cuatro"], k = 2
        // Expected output: 5
        String[] words1 = {"uno", "dos", "uno", "tres", "dos", "uno", "cuatro"};
        int result1 = solution.demonstrateAndPrint(words1, 2);
        System.out.println("Expected: 5, Actual: " + result1);
        System.out.println("Match: " + (result1 == 5));
        System.out.println();

        // Sample 2:
        // ["bleu","bleu","bleu","vert","vert","jaune"], k = 1
        // Expected output: 3
        String[] words2 = {"bleu", "bleu", "bleu", "vert", "vert", "jaune"};
        int result2 = solution.demonstrateAndPrint(words2, 1);
        System.out.println("Expected: 3, Actual: " + result2);
        System.out.println("Match: " + (result2 == 3));
        System.out.println();

        // Edge case: k = 0, answer must be 0
        String[] words3 = {"a", "b", "c"};
        int result3 = solution.demonstrateAndPrint(words3, 0);
        System.out.println("Expected: 0, Actual: " + result3);
        System.out.println("Match: " + (result3 == 0));
        System.out.println();

        // Additional test: all unique words, large enough k
        String[] words4 = {"red", "green", "blue", "yellow"};
        int result4 = solution.demonstrateAndPrint(words4, 2);
        System.out.println("Expected: 4, Actual: " + result4);
        System.out.println("Match: " + (result4 == 4));
        System.out.println();

        // Additional test: repeated single word
        String[] words5 = {"x", "x", "x", "x", "x"};
        int result5 = solution.demonstrateAndPrint(words5, 2);
        System.out.println("Expected: 2, Actual: " + result5);
        System.out.println("Match: " + (result5 == 2));
    }
}