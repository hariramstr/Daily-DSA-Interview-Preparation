import java.util.*;

/*
 * Title: Longest Translation Draft With Terminology Budget
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * A localization team is reviewing a draft translation represented by an array `terms`,
 * where `terms[i]` is the terminology ID used in the `i`th sentence. For quality reasons,
 * the team wants to select one contiguous block of sentences for expedited review.
 * A block is considered valid if it can be made terminology-consistent under a limited
 * cleanup budget `k`.
 *
 * For any chosen contiguous block, let `f` be the highest frequency of any single
 * terminology ID inside that block. The cleanup cost of the block is defined as the
 * number of sentences that do not use that most frequent terminology, which is
 * `window_length - f`. In other words, those sentences would need to be rewritten to
 * match the dominant terminology in the block. Your task is to return the length of the
 * longest valid contiguous block whose cleanup cost is at most `k`.
 *
 * This is not the same as requiring all values to be distinct or counting only a fixed-size
 * window. The optimal solution must efficiently maintain a moving window while tracking
 * frequency information as the window expands and shrinks.
 *
 * Constraints:
 * - 1 <= terms.length <= 2 * 10^5
 * - 1 <= terms[i] <= 10^9
 * - 0 <= k <= terms.length
 *
 * Example 1:
 * Input: terms = [4, 7, 7, 4, 7, 9, 7], k = 2
 * Output: 6
 *
 * Example 2:
 * Input: terms = [1, 2, 3, 2, 2, 3, 3, 3, 2], k = 3
 * Output: 7
 *
 * Return only the maximum possible length of such a contiguous block.
 */

public class Solution {

    /**
     * Computes the maximum length of a contiguous block that can be made terminology-consistent
     * using at most k rewrites.
     *
     * Core idea:
     * We use a sliding window. For the current window [left, right], we maintain:
     * - the frequency of each terminology ID inside the window
     * - the highest frequency seen for any terminology in the current expanding process
     *
     * A window is considered valid when:
     *     windowLength - maxFrequencyInWindow <= k
     *
     * This means the number of non-dominant terms in the window is within the cleanup budget.
     *
     * Important implementation detail:
     * We keep a variable `maxFreq` that stores the maximum frequency ever reached by any term
     * while expanding the right pointer. We do NOT decrease it when shrinking the left pointer.
     * This is a standard and correct optimization for this sliding window pattern.
     *
     * Why this still works:
     * - `maxFreq` may become "stale" (larger than the true max frequency in the current window),
     *   but it never causes us to miss the optimal answer.
     * - It may temporarily allow a window to look valid earlier than it truly is, but the sliding
     *   window process still guarantees the final maximum length is correct.
     *
     * @param terms the array of terminology IDs, where each value represents the terminology used
     *              in one sentence
     * @param k the maximum number of sentences allowed to be rewritten inside the chosen block
     * @return the length of the longest contiguous block whose cleanup cost is at most k
     *
     * Time complexity: O(n), where n is the length of terms, because each index is processed
     * at most a constant number of times by the sliding window.
     * Space complexity: O(m), where m is the number of distinct terminology IDs currently tracked
     * in the frequency map; in the worst case this can be O(n).
     */
    public int longestTranslationDraft(int[] terms, int k) {
        // Frequency map:
        // key   -> terminology ID
        // value -> how many times that terminology appears in the current window
        Map<Integer, Integer> frequency = new HashMap<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int best = 0;

        // Highest frequency of any single terminology encountered while expanding the window.
        int maxFreq = 0;

        // Expand the window by moving `right` from left to right across the array.
        for (int right = 0; right < terms.length; right++) {
            int currentTerm = terms[right];

            // Add the new rightmost term into the frequency map.
            int newCount = frequency.getOrDefault(currentTerm, 0) + 1;
            frequency.put(currentTerm, newCount);

            // Update the running maximum frequency if this term now appears more often
            // than any term we have seen before in the current expansion process.
            maxFreq = Math.max(maxFreq, newCount);

            // Current window length is from left to right inclusive.
            // If windowLength - maxFreq > k, then even after choosing the dominant term,
            // too many other terms remain and the cleanup budget is exceeded.
            //
            // In that case, we must shrink the window from the left until it becomes valid again.
            while ((right - left + 1) - maxFreq > k) {
                int leftTerm = terms[left];

                // Remove the leftmost term from the window frequency.
                frequency.put(leftTerm, frequency.get(leftTerm) - 1);

                // Move the left boundary rightward, effectively shrinking the window.
                left++;
            }

            // At this point, the window is valid under the sliding window invariant.
            // Update the best answer with the current valid window length.
            best = Math.max(best, right - left + 1);
        }

        return best;
    }

    /**
     * Convenience wrapper that accepts a List<Integer> instead of an int[].
     * This can be useful for beginners or for testing with collection-based inputs.
     *
     * @param termsList the list of terminology IDs
     * @param k the maximum allowed cleanup budget
     * @return the maximum valid contiguous block length
     *
     * Time complexity: O(n), where n is the size of the list
     * Space complexity: O(n) in the worst case due to the copied array and frequency map
     */
    public int longestTranslationDraft(List<Integer> termsList, int k) {
        int[] terms = new int[termsList.size()];
        for (int i = 0; i < termsList.size(); i++) {
            terms[i] = termsList.get(i);
        }
        return longestTranslationDraft(terms, k);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * Also includes a few extra sanity checks for clarity.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call
     * Space complexity: O(n) in the worst case per demonstration call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] terms1 = {4, 7, 7, 4, 7, 9, 7};
        int k1 = 2;
        int result1 = solution.longestTranslationDraft(terms1, k1);
        System.out.println("Sample 1 Result: " + result1);
        System.out.println("Expected: 6");

        // Sample 2
        int[] terms2 = {1, 2, 3, 2, 2, 3, 3, 3, 2};
        int k2 = 3;
        int result2 = solution.longestTranslationDraft(terms2, k2);
        System.out.println("Sample 2 Result: " + result2);
        System.out.println("Expected: 7");

        // Extra test 1: all same terminology, no rewrites needed
        int[] terms3 = {5, 5, 5, 5};
        int k3 = 0;
        int result3 = solution.longestTranslationDraft(terms3, k3);
        System.out.println("Extra Test 1 Result: " + result3);
        System.out.println("Expected: 4");

        // Extra test 2: no budget, longest block must already be uniform
        int[] terms4 = {1, 2, 2, 3, 3, 3, 2};
        int k4 = 0;
        int result4 = solution.longestTranslationDraft(terms4, k4);
        System.out.println("Extra Test 2 Result: " + result4);
        System.out.println("Expected: 3");

        // Extra test 3: enough budget to cover the whole array
        int[] terms5 = {8, 1, 8, 2, 8, 3};
        int k5 = 3;
        int result5 = solution.longestTranslationDraft(terms5, k5);
        System.out.println("Extra Test 3 Result: " + result5);
        System.out.println("Expected: 6");
    }
}