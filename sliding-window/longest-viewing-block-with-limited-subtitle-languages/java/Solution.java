import java.util.*;

/*
 * Title: Longest Viewing Block With Limited Subtitle Languages
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A streaming platform stores the subtitle language used for each minute of a live broadcast
 * in an array languages, where languages[i] is a string such as "en", "es", or "fr".
 * A user wants to watch one continuous block of the broadcast, but they are only comfortable
 * switching between at most k distinct subtitle languages during that block.
 *
 * Your task is to return the length of the longest contiguous segment of languages that
 * contains at most k distinct language codes.
 *
 * This models a realistic product analytics problem: find the longest uninterrupted viewing
 * interval that stays within a user's subtitle tolerance. The segment must be contiguous,
 * and repeated occurrences of the same language do not increase the distinct count.
 *
 * Write a function that computes the maximum possible length.
 *
 * Constraints:
 * - 1 <= languages.length <= 200000
 * - 1 <= languages[i].length <= 10
 * - languages[i] consists of lowercase English letters
 * - 1 <= k <= languages.length
 *
 * Example 1:
 * Input: languages = ["en","en","es","es","fr","es","es"], k = 2
 * Output: 4
 * Explanation: The longest valid block is ["en","en","es","es"] or ["es","fr","es","es"],
 * each with length 4 and at most 2 distinct languages.
 *
 * Example 2:
 * Input: languages = ["jp","kr","jp","cn","cn","jp","jp"], k = 1
 * Output: 2
 * Explanation: With only 1 distinct language allowed, the best contiguous block is
 * ["cn","cn"] or ["jp","jp"], so the answer is 2.
 */

public class Solution {

    /**
     * Computes the length of the longest contiguous subarray that contains
     * at most k distinct subtitle languages.
     *
     * The algorithm uses the classic sliding window technique:
     * - Expand the right side of the window one element at a time.
     * - Track how many times each language appears inside the current window.
     * - If the window becomes invalid (more than k distinct languages),
     *   shrink it from the left until it becomes valid again.
     * - At every valid step, update the best window length found so far.
     *
     * @param languages the array of subtitle language codes for each minute
     * @param k the maximum number of distinct languages allowed in the viewing block
     * @return the maximum length of a contiguous segment containing at most k distinct languages
     *
     * Time complexity: O(n), where n is languages.length, because each element is added
     * to the window once and removed from the window at most once.
     * Space complexity: O(k) in the typical sliding-window sense for active distinct entries,
     * and O(min(n, number of unique languages overall)) in the map in the worst case.
     */
    public int longestViewingBlock(String[] languages, int k) {
        // Frequency map:
        // key   -> language code currently present in the window
        // value -> how many times that language appears in the window
        Map<String, Integer> frequency = new HashMap<>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We move right from 0 to languages.length - 1, expanding the window.
        for (int right = 0; right < languages.length; right++) {
            String currentLanguage = languages[right];

            // Step 1: include the new language at index right into the window.
            // If it is already present, increase its count.
            // Otherwise, it becomes a new distinct language in the window.
            frequency.put(currentLanguage, frequency.getOrDefault(currentLanguage, 0) + 1);

            // Step 2: if the window now contains too many distinct languages,
            // shrink it from the left until it becomes valid again.
            //
            // Why a while loop instead of if?
            // Because removing just one element may still leave us with more than k
            // distinct languages, so we keep shrinking until the condition is satisfied.
            while (frequency.size() > k) {
                String leftLanguage = languages[left];

                // Decrease the count of the language that is leaving the window.
                frequency.put(leftLanguage, frequency.get(leftLanguage) - 1);

                // If its count becomes zero, it is no longer inside the window at all,
                // so we remove it from the map. This reduces the distinct language count.
                if (frequency.get(leftLanguage) == 0) {
                    frequency.remove(leftLanguage);
                }

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3: at this point, the window [left, right] is guaranteed valid:
            // it contains at most k distinct languages.
            //
            // So we compute its length and update the best answer if needed.
            int currentWindowLength = right - left + 1;
            best = Math.max(best, currentWindowLength);
        }

        return best;
    }

    /**
     * A small helper method to print an input array in a readable format.
     *
     * @param languages the array of subtitle language codes
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is languages.length.
     * Space complexity: O(n) for the produced string content.
     */
    public String arrayToString(String[] languages) {
        return Arrays.toString(languages);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * Verified examples:
     * 1) ["en","en","es","es","fr","es","es"], k = 2 -> 4
     * 2) ["jp","kr","jp","cn","cn","jp","jp"], k = 1 -> 2
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call to longestViewingBlock.
     * Space complexity: depends on the internal frequency map used by the algorithm.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] languages1 = {"en", "en", "es", "es", "fr", "es", "es"};
        int k1 = 2;
        int result1 = solution.longestViewingBlock(languages1, k1);
        System.out.println("Example 1:");
        System.out.println("languages = " + solution.arrayToString(languages1));
        System.out.println("k = " + k1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        String[] languages2 = {"jp", "kr", "jp", "cn", "cn", "jp", "jp"};
        int k2 = 1;
        int result2 = solution.longestViewingBlock(languages2, k2);
        System.out.println("Example 2:");
        System.out.println("languages = " + solution.arrayToString(languages2));
        System.out.println("k = " + k2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 2");
        System.out.println();

        // Additional quick sanity checks for beginner-friendly demonstration.
        String[] languages3 = {"en"};
        int k3 = 1;
        System.out.println("Additional Test 1:");
        System.out.println("languages = " + solution.arrayToString(languages3));
        System.out.println("k = " + k3);
        System.out.println("Output = " + solution.longestViewingBlock(languages3, k3));
        System.out.println("Expected = 1");
        System.out.println();

        String[] languages4 = {"en", "es", "fr", "de"};
        int k4 = 4;
        System.out.println("Additional Test 2:");
        System.out.println("languages = " + solution.arrayToString(languages4));
        System.out.println("k = " + k4);
        System.out.println("Output = " + solution.longestViewingBlock(languages4, k4));
        System.out.println("Expected = 4");
    }
}