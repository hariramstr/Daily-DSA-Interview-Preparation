import java.util.*;

/*
 * Title: Longest Code Review Window With Per-Language Bug Caps
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a stream of code review findings in chronological order. Each finding belongs
 * to a programming language and has a bug severity score. The i-th finding is represented by
 * two arrays: languages[i] is a string such as "cpp", "java", or "py", and severities[i] is
 * a positive integer. You are also given an integer limit and an integer budget.
 *
 * A contiguous review window is considered valid if it satisfies both rules:
 * 1. For every language, the number of findings from that language inside the window is at most limit.
 * 2. Let maxSeverity be the maximum severity in the window and minSeverity be the minimum severity
 *    in the window. Then maxSeverity - minSeverity must be at most budget.
 *
 * Return the length of the longest valid contiguous window.
 *
 * This problem models a review dashboard that should not become dominated by too many findings
 * from the same language, while also keeping the severity spread manageable enough for a single
 * triage session.
 *
 * Constraints:
 * - 1 <= languages.length == severities.length <= 200000
 * - 1 <= severities[i] <= 10^9
 * - 1 <= limit <= languages.length
 * - 0 <= budget <= 10^9
 * - languages[i] consists of lowercase English letters and has length between 1 and 10
 *
 * Efficient approach:
 * We use a classic sliding window with two pointers:
 * - Expand the right pointer one element at a time.
 * - Track language frequencies in the current window.
 * - Track the minimum and maximum severity in the current window using two monotonic deques:
 *   one increasing deque for minimums and one decreasing deque for maximums.
 * - If either rule is violated, move the left pointer forward until the window becomes valid again.
 *
 * This works in O(n) time because every element is added and removed from each data structure
 * at most once.
 */
public class Solution {

    /**
     * Computes the length of the longest contiguous valid window.
     *
     * A window is valid when:
     * 1) No language appears more than {@code limit} times.
     * 2) The difference between maximum and minimum severity in the window is at most {@code budget}.
     *
     * Time complexity: O(n), where n is the number of findings.
     * Each index enters and leaves the sliding window once, and each index is pushed/popped
     * from each deque at most once.
     *
     * Space complexity: O(n) in the worst case.
     * The language frequency map can hold up to O(n) distinct languages in the worst case,
     * and the deques can also hold up to O(n) indices.
     *
     * @param languages the language of each finding, in chronological order
     * @param severities the severity score of each finding, aligned with {@code languages}
     * @param limit the maximum allowed count for any single language inside a valid window
     * @param budget the maximum allowed difference between max and min severity in a valid window
     * @return the length of the longest valid contiguous window
     */
    public int longestValidWindow(String[] languages, int[] severities, int limit, int budget) {
        int n = languages.length;

        // Frequency map for languages currently inside the sliding window [left, right].
        Map<String, Integer> languageCount = new HashMap<>();

        // Monotonic deque for minimum severity:
        // indices are stored so that severities are increasing from front to back.
        // Therefore, the front always contains the index of the minimum severity in the window.
        Deque<Integer> minDeque = new ArrayDeque<>();

        // Monotonic deque for maximum severity:
        // indices are stored so that severities are decreasing from front to back.
        // Therefore, the front always contains the index of the maximum severity in the window.
        Deque<Integer> maxDeque = new ArrayDeque<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int best = 0;

        // We expand the window by moving 'right' from 0 to n - 1.
        for (int right = 0; right < n; right++) {
            String currentLanguage = languages[right];
            int currentSeverity = severities[right];

            // Step 1: Add the new language occurrence into the frequency map.
            languageCount.put(currentLanguage, languageCount.getOrDefault(currentLanguage, 0) + 1);

            // Step 2: Insert the new severity index into the min deque.
            // We remove from the back while the new severity is smaller or equal,
            // because those larger/equal values can never become the minimum
            // while the new index remains in the window.
            while (!minDeque.isEmpty() && severities[minDeque.peekLast()] >= currentSeverity) {
                minDeque.pollLast();
            }
            minDeque.offerLast(right);

            // Step 3: Insert the new severity index into the max deque.
            // We remove from the back while the new severity is larger or equal,
            // because those smaller/equal values can never become the maximum
            // while the new index remains in the window.
            while (!maxDeque.isEmpty() && severities[maxDeque.peekLast()] <= currentSeverity) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(right);

            // Step 4: Shrink the window from the left while it is invalid.
            //
            // The window can be invalid for two reasons:
            // A) The current language count exceeds 'limit'.
            //    Important observation:
            //    Before adding languages[right], the previous window was already valid.
            //    Therefore, after adding one element, only the count of languages[right]
            //    could possibly exceed the limit.
            //
            // B) The severity spread exceeds 'budget':
            //    maxSeverity - minSeverity > budget
            //
            // We keep moving 'left' until both conditions are satisfied again.
            while (languageCount.get(currentLanguage) > limit
                    || (long) severities[maxDeque.peekFirst()] - severities[minDeque.peekFirst()] > budget) {

                String leftLanguage = languages[left];

                // Decrease the count of the language that is leaving the window.
                int updatedCount = languageCount.get(leftLanguage) - 1;
                if (updatedCount == 0) {
                    languageCount.remove(leftLanguage);
                } else {
                    languageCount.put(leftLanguage, updatedCount);
                }

                // If the outgoing index is currently the minimum, remove it from minDeque.
                if (!minDeque.isEmpty() && minDeque.peekFirst() == left) {
                    minDeque.pollFirst();
                }

                // If the outgoing index is currently the maximum, remove it from maxDeque.
                if (!maxDeque.isEmpty() && maxDeque.peekFirst() == left) {
                    maxDeque.pollFirst();
                }

                // Finally move the left boundary forward.
                left++;
            }

            // Step 5: At this point the window [left, right] is valid.
            // Update the best length.
            best = Math.max(best, right - left + 1);
        }

        return best;
    }

    /**
     * Convenience helper that accepts lists instead of arrays.
     *
     * Time complexity: O(n), where n is the number of findings.
     * Space complexity: O(n).
     *
     * @param languagesList list of languages in chronological order
     * @param severitiesList list of severities aligned with {@code languagesList}
     * @param limit the maximum allowed count for any single language inside a valid window
     * @param budget the maximum allowed severity spread inside a valid window
     * @return the length of the longest valid contiguous window
     * @throws IllegalArgumentException if the two lists have different sizes
     */
    public int longestValidWindow(List<String> languagesList, List<Integer> severitiesList, int limit, int budget) {
        if (languagesList.size() != severitiesList.size()) {
            throw new IllegalArgumentException("languages and severities must have the same length");
        }

        String[] languages = languagesList.toArray(new String[0]);
        int[] severities = new int[severitiesList.size()];
        for (int i = 0; i < severitiesList.size(); i++) {
            severities[i] = severitiesList.get(i);
        }

        return longestValidWindow(languages, severities, limit, budget);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Time complexity: O(n) per demonstration call.
     * Space complexity: O(n) per demonstration call.
     *
     * @param args command-line arguments, not used
     * @return nothing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        String[] languages1 = {"py", "java", "py", "cpp", "java", "py"};
        int[] severities1 = {4, 7, 5, 6, 8, 5};
        int limit1 = 2;
        int budget1 = 3;
        int result1 = solution.longestValidWindow(languages1, severities1, limit1, budget1);
        System.out.println("Example 1 Output: " + result1); // Expected: 4

        // Example 2
        String[] languages2 = {"go", "go", "rs", "go", "rs", "ts", "rs"};
        int[] severities2 = {10, 3, 6, 4, 7, 5, 8};
        int limit2 = 2;
        int budget2 = 4;
        int result2 = solution.longestValidWindow(languages2, severities2, limit2, budget2);
        System.out.println("Example 2 Output: " + result2); // Expected: 5

        // Additional quick sanity check
        String[] languages3 = {"java"};
        int[] severities3 = {42};
        int limit3 = 1;
        int budget3 = 0;
        int result3 = solution.longestValidWindow(languages3, severities3, limit3, budget3);
        System.out.println("Single Element Output: " + result3); // Expected: 1
    }
}