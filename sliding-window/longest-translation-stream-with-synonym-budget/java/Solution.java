import java.util.*;

/*
 * Title: Longest Translation Stream With Synonym Budget
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are building a real-time translation quality monitor. A transcript has already been tokenized
 * into an array words, where each words[i] is the normalized meaning ID of the spoken word at position i.
 * Because multiple surface words may map to the same meaning, repeated IDs are allowed and expected.
 * A translation team considers a contiguous segment valid if no meaning ID appears more than limit[id]
 * times inside that segment. However, the monitor is also allowed to spend from a global synonym budget B:
 * for each extra occurrence of a meaning ID beyond its allowed limit inside the current segment, the segment
 * consumes 1 unit of budget. For example, if meaning 7 appears 5 times and limit[7] = 3, then meaning 7
 * contributes 2 to the budget usage of that segment.
 *
 * Your task is to return the length of the longest contiguous segment whose total budget usage is at most B.
 *
 * Formally, for a window [l, r], let freq[x] be the number of times meaning ID x appears in that window.
 * Its violation cost is sum(max(0, freq[x] - limit[x])) over all meaning IDs that appear in the window.
 * Find the maximum window length with violation cost <= B.
 *
 * If a meaning ID appears in words but is not present in limit, its allowed limit is 0.
 *
 * Constraints:
 * - 1 <= words.length <= 2 * 10^5
 * - 0 <= words[i] <= 10^9
 * - 0 <= B <= 2 * 10^5
 * - 0 <= limit.size() <= 2 * 10^5
 * - 0 <= limit[id] <= words.length
 * - The answer must be computed in better than O(n^2) time.
 *
 * Example 1:
 * Input:
 * words = [4, 1, 4, 2, 4, 1, 3]
 * limit = {4: 2, 1: 1, 2: 1, 3: 1}
 * B = 1
 * Output:
 * 5
 *
 * Example 2:
 * Input:
 * words = [8, 8, 5, 8, 5, 6, 5, 5]
 * limit = {8: 1, 5: 2, 6: 1}
 * B = 2
 * Output:
 * 6
 */

public class Solution {

    /**
     * Computes the length of the longest contiguous segment whose total violation cost
     * is at most the given budget B.
     *
     * The key observation is:
     * For each meaning ID x, its contribution to the window cost is:
     * max(0, freq[x] - limit[x])
     *
     * When we add one occurrence of x into the window:
     * - If the previous frequency was already at least limit[x], then this new occurrence
     *   increases the violation cost by 1.
     * - Otherwise, the cost does not change.
     *
     * When we remove one occurrence of x from the window:
     * - If the frequency before removal was greater than limit[x], then removing it
     *   decreases the violation cost by 1.
     * - Otherwise, the cost does not change.
     *
     * This allows a classic sliding window:
     * - Expand the right pointer one step at a time.
     * - Maintain the current total violation cost.
     * - If the cost becomes too large, move the left pointer rightward until the window
     *   becomes valid again.
     * - Track the maximum valid window length.
     *
     * @param words the array of meaning IDs
     * @param limit a map from meaning ID to its allowed count inside a valid segment;
     *              if an ID is missing, its allowed limit is treated as 0
     * @param B the maximum total violation cost allowed
     * @return the maximum length of a contiguous segment with total violation cost <= B
     *
     * Time complexity: O(n), where n = words.length, because each index enters and leaves the window at most once.
     * Space complexity: O(k), where k is the number of distinct IDs currently tracked in the frequency map.
     */
    public int longestTranslationStream(int[] words, Map<Integer, Integer> limit, int B) {
        Map<Integer, Integer> freq = new HashMap<>();

        int left = 0;
        int best = 0;

        // This variable stores the current total violation cost of the window [left, right].
        int currentCost = 0;

        // Expand the window by moving 'right' from left to right across the array.
        for (int right = 0; right < words.length; right++) {
            int id = words[right];

            // Read the allowed limit for this ID.
            // If the ID is not present in the limit map, its allowed limit is 0.
            int allowed = limit.getOrDefault(id, 0);

            // Current frequency before adding this new occurrence.
            int oldFreq = freq.getOrDefault(id, 0);

            // If oldFreq >= allowed, then adding one more occurrence creates one additional
            // "extra" occurrence beyond the allowed limit.
            //
            // Why this condition works:
            // - Suppose allowed = 2
            //   oldFreq = 0 -> newFreq = 1, cost contribution stays 0
            //   oldFreq = 1 -> newFreq = 2, cost contribution stays 0
            //   oldFreq = 2 -> newFreq = 3, cost contribution increases from 0 to 1
            //   oldFreq = 3 -> newFreq = 4, cost contribution increases from 1 to 2
            //
            // - Suppose allowed = 0
            //   oldFreq = 0 -> newFreq = 1, cost increases from 0 to 1
            //   oldFreq = 1 -> newFreq = 2, cost increases from 1 to 2
            if (oldFreq >= allowed) {
                currentCost++;
            }

            // Actually add this occurrence into the frequency map.
            freq.put(id, oldFreq + 1);

            // If the window is invalid (cost too large), shrink it from the left
            // until it becomes valid again.
            while (currentCost > B) {
                int leftId = words[left];
                int leftAllowed = limit.getOrDefault(leftId, 0);
                int leftFreq = freq.get(leftId);

                // We are about to remove one occurrence of leftId.
                //
                // If leftFreq > leftAllowed before removal, then this occurrence is currently
                // counted as one of the "extra" occurrences. Removing it reduces total cost by 1.
                //
                // Example:
                // allowed = 2, leftFreq = 4
                // contribution before = 4 - 2 = 2
                // contribution after  = 3 - 2 = 1
                // so cost decreases by 1
                //
                // If leftFreq <= leftAllowed, then removing it does not reduce any violation.
                if (leftFreq > leftAllowed) {
                    currentCost--;
                }

                // Update the frequency map after removal.
                if (leftFreq == 1) {
                    freq.remove(leftId);
                } else {
                    freq.put(leftId, leftFreq - 1);
                }

                // Move the left boundary rightward.
                left++;
            }

            // At this point, window [left, right] is valid.
            // Update the best answer.
            best = Math.max(best, right - left + 1);
        }

        return best;
    }

    /**
     * Convenience helper that builds the limit map from a 2D array of pairs.
     * Each pair is expected to be [id, allowedCount].
     *
     * @param pairs 2D array where each row contains exactly two integers: [id, limit]
     * @return a map representing the limits
     *
     * Time complexity: O(m), where m is the number of pairs.
     * Space complexity: O(m).
     */
    public Map<Integer, Integer> buildLimitMap(int[][] pairs) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int[] pair : pairs) {
            map.put(pair[0], pair[1]);
        }
        return map;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It prints:
     * - Example 1 result, expected 5
     * - Example 2 result, expected 6
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n1 + n2) for the two demonstrations.
     * Space complexity: O(k1 + k2) for the maps and frequency tracking.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] words1 = {4, 1, 4, 2, 4, 1, 3};
        Map<Integer, Integer> limit1 = solution.buildLimitMap(new int[][]{
                {4, 2},
                {1, 1},
                {2, 1},
                {3, 1}
        });
        int B1 = 1;
        int result1 = solution.longestTranslationStream(words1, limit1, B1);
        System.out.println(result1); // Expected: 5

        // Example 2
        int[] words2 = {8, 8, 5, 8, 5, 6, 5, 5};
        Map<Integer, Integer> limit2 = solution.buildLimitMap(new int[][]{
                {8, 1},
                {5, 2},
                {6, 1}
        });
        int B2 = 2;
        int result2 = solution.longestTranslationStream(words2, limit2, B2);
        System.out.println(result2); // Expected: 6
    }
}