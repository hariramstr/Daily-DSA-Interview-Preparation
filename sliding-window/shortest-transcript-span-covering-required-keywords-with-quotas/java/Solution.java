import java.util.*;

/*
 * Title: Shortest Transcript Span Covering Required Keywords with Quotas
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a transcript represented by an array of lowercase words `words`,
 * where `words[i]` is the `i`-th token spoken in order. You are also given a list
 * of required keywords with minimum occurrence quotas, represented by two arrays:
 * `required` and `need`, where `required[j]` must appear at least `need[j]` times
 * inside the chosen contiguous span.
 *
 * Your task is to return the length of the shortest contiguous subarray of `words`
 * that satisfies all keyword quotas. If no such span exists, return `-1`.
 *
 * Unlike the classic minimum-cover problems where each target appears once, the same
 * keyword may need to appear multiple times, and the transcript can be very large.
 * An efficient sliding window solution is expected. The challenge is to maintain
 * counts correctly while shrinking the window as aggressively as possible without
 * violating any quota.
 *
 * Formally, find the minimum value of `r - l + 1` such that for every index `j`,
 * the subarray `words[l...r]` contains at least `need[j]` occurrences of `required[j]`.
 *
 * Constraints:
 * - 1 <= words.length <= 200000
 * - 1 <= required.length == need.length <= 100000
 * - 1 <= sum(need) <= 200000
 * - 1 <= words[i].length, required[j].length <= 20
 * - All strings contain only lowercase English letters.
 * - All values in `required` are distinct.
 *
 * Example 1:
 * Input: words = ["api","error","db","api","timeout","error","api"],
 *        required = ["api","error"], need = [2,1]
 * Output: 4
 * Explanation: The shortest valid span is ["api","error","db","api"] with length 4.
 * It contains "api" twice and "error" once.
 *
 * Example 2:
 * Input: words = ["login","cache","login","queue","cache","queue"],
 *        required = ["login","queue","cache"], need = [2,1,2]
 * Output: 5
 * Explanation: The span ["login","cache","login","queue","cache"] has two "login",
 * one "queue", and two "cache". No shorter valid span exists.
 *
 * Return only the minimum length, not the span itself.
 */

public class Solution {

    /**
     * Finds the length of the shortest contiguous subarray of {@code words} that satisfies
     * all required keyword quotas.
     *
     * The algorithm uses a classic sliding window:
     * 1. Expand the right boundary one word at a time.
     * 2. Track counts only for words that matter (those present in {@code required}).
     * 3. Maintain how many distinct required keywords currently meet their quota.
     * 4. Once all quotas are satisfied, shrink the left boundary as much as possible
     *    while keeping the window valid.
     * 5. Record the minimum valid window length seen.
     *
     * @param words the transcript tokens in spoken order
     * @param required the distinct required keywords
     * @param need the minimum required count for each corresponding keyword
     * @return the minimum length of a valid contiguous span, or {@code -1} if impossible
     *
     * Time complexity: O(n + m), where n = words.length and m = required.length,
     * because each pointer moves at most n times and map operations are O(1) average.
     * Space complexity: O(m), for the required-count maps.
     */
    public int shortestSpan(String[] words, String[] required, int[] need) {
        // Defensive validation for completeness.
        // The problem guarantees valid input, but this keeps the method robust.
        if (words == null || required == null || need == null) {
            return -1;
        }
        if (required.length != need.length || required.length == 0) {
            return -1;
        }

        // Map each required keyword to its quota.
        // Example:
        // required = ["api", "error"], need = [2, 1]
        // targetCount = {"api" -> 2, "error" -> 1}
        Map<String, Integer> targetCount = new HashMap<>(required.length * 2);
        for (int i = 0; i < required.length; i++) {
            targetCount.put(required[i], need[i]);
        }

        // Before running the sliding window, we can do a quick feasibility check:
        // count total occurrences of required words in the entire transcript.
        // If any required word appears fewer times than needed globally, answer is -1.
        Map<String, Integer> totalCount = new HashMap<>(required.length * 2);
        for (String word : words) {
            if (targetCount.containsKey(word)) {
                totalCount.put(word, totalCount.getOrDefault(word, 0) + 1);
            }
        }
        for (int i = 0; i < required.length; i++) {
            String key = required[i];
            if (totalCount.getOrDefault(key, 0) < need[i]) {
                return -1;
            }
        }

        // This map stores counts of required keywords inside the CURRENT sliding window.
        Map<String, Integer> windowCount = new HashMap<>(required.length * 2);

        // Number of distinct required keywords whose quota is currently satisfied.
        // For example, if we need:
        // api -> 2, error -> 1, cache -> 3
        // and current window satisfies api and error but not cache,
        // then formed = 2.
        int formed = 0;

        // Total number of distinct required keywords we must satisfy.
        int requiredKinds = required.length;

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far. Start with a very large value.
        int best = Integer.MAX_VALUE;

        // Expand the window by moving the right boundary from left to right.
        for (int right = 0; right < words.length; right++) {
            String currentWord = words[right];

            // Only update counts if this word is one of the required keywords.
            if (targetCount.containsKey(currentWord)) {
                int newCount = windowCount.getOrDefault(currentWord, 0) + 1;
                windowCount.put(currentWord, newCount);

                // If after adding this word, its count exactly reaches the needed quota,
                // then one more required keyword is now satisfied.
                //
                // Important:
                // We increment formed ONLY when count becomes exactly equal to target.
                // If it goes above target, formed should not increase again.
                if (newCount == targetCount.get(currentWord)) {
                    formed++;
                }
            }

            // If all required keywords currently satisfy their quotas,
            // then the window [left..right] is valid.
            // Now we try to shrink it from the left as aggressively as possible
            // without making it invalid.
            while (formed == requiredKinds && left <= right) {
                // Since the current window is valid, update the best answer.
                int currentLength = right - left + 1;
                if (currentLength < best) {
                    best = currentLength;
                }

                String leftWord = words[left];

                // We are about to remove words[left] from the window by moving left forward.
                // If this word is required, its count in the window must be decreased.
                if (targetCount.containsKey(leftWord)) {
                    int oldCount = windowCount.get(leftWord);

                    // If the current count is EXACTLY equal to the target quota,
                    // then removing one occurrence will make this keyword no longer satisfied.
                    // Therefore formed must decrease.
                    if (oldCount == targetCount.get(leftWord)) {
                        formed--;
                    }

                    // Decrease the count in the window.
                    windowCount.put(leftWord, oldCount - 1);
                }

                // Actually shrink the window.
                left++;
            }
        }

        return best == Integer.MAX_VALUE ? -1 : best;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the demonstration itself, excluding the called method.
     * Space complexity: O(1), excluding the called method.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] words1 = {"api", "error", "db", "api", "timeout", "error", "api"};
        String[] required1 = {"api", "error"};
        int[] need1 = {2, 1};
        int result1 = solution.shortestSpan(words1, required1, need1);
        System.out.println(result1); // Expected: 4

        String[] words2 = {"login", "cache", "login", "queue", "cache", "queue"};
        String[] required2 = {"login", "queue", "cache"};
        int[] need2 = {2, 1, 2};
        int result2 = solution.shortestSpan(words2, required2, need2);
        System.out.println(result2); // Expected: 5

        // Additional quick sanity check: impossible case
        String[] words3 = {"a", "b", "c"};
        String[] required3 = {"a", "d"};
        int[] need3 = {1, 1};
        int result3 = solution.shortestSpan(words3, required3, need3);
        System.out.println(result3); // Expected: -1
    }
}