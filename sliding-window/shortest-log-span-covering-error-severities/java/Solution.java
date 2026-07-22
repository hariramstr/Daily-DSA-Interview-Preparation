import java.util.*;

/*
 * Title: Shortest Log Span Covering Error Severities
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * A monitoring system records a stream of application logs as an array `logs`, where each element
 * is an integer severity level. You are also given another array `required`, where each integer
 * represents a severity level that must appear in a chosen contiguous log span. Duplicates in
 * `required` matter: if `required = [2, 2, 5]`, then the chosen span must contain severity `2`
 * at least twice and severity `5` at least once.
 *
 * Your task is to return the length of the shortest contiguous subarray of `logs` that satisfies
 * all required severity counts. If no such subarray exists, return `-1`.
 *
 * This problem models alert triage, where investigators need the smallest time window containing
 * all critical error patterns, including repeated occurrences of the same severity. The arrays may
 * be large, so solutions that check every possible subarray will be too slow.
 *
 * Constraints:
 * - 1 <= logs.length <= 2 * 10^5
 * - 1 <= required.length <= 2 * 10^5
 * - 1 <= logs[i], required[i] <= 10^9
 * - The answer should be computed in better than O(n^2) time.
 *
 * Example 1:
 * Input: logs = [4, 2, 7, 2, 5, 1, 2, 5], required = [2, 5, 2]
 * Output: 4
 * Explanation: The shortest valid span is [2, 7, 2, 5], which contains severity 2 twice and severity 5 once.
 *
 * Example 2:
 * Input: logs = [3, 1, 4, 1, 5, 9], required = [1, 1, 2]
 * Output: -1
 * Explanation: No contiguous span can satisfy the requirement because severity 2 never appears in logs.
 */

public class Solution {

    /**
     * Computes the length of the shortest contiguous subarray of {@code logs} that contains
     * all values from {@code required} with at least the same multiplicities.
     *
     * The method uses the classic sliding window technique:
     * 1. Count how many times each severity is required.
     * 2. Expand the right boundary of the window.
     * 3. Track counts inside the current window.
     * 4. Once the window satisfies all required counts, shrink from the left
     *    as much as possible while still remaining valid.
     * 5. Record the minimum valid window length found.
     *
     * @param logs the stream of log severities
     * @param required the multiset of required severities; duplicates matter
     * @return the length of the shortest valid contiguous subarray, or -1 if impossible
     *
     * Time complexity: O(n + m), where n = logs.length and m = required.length.
     * Each index moves through the array at most once in the sliding window.
     *
     * Space complexity: O(k), where k is the number of distinct severity values
     * appearing in {@code required} and in the tracked window.
     */
    public int shortestLogSpan(int[] logs, int[] required) {
        // Defensive handling for completeness.
        // Based on the constraints, arrays are non-empty, but this keeps the method robust.
        if (logs == null || required == null || logs.length == 0 || required.length == 0) {
            return -1;
        }

        // If the required multiset is larger than the entire logs array,
        // it is impossible for any subarray to satisfy the requirement.
        if (required.length > logs.length) {
            return -1;
        }

        // Step 1:
        // Build a frequency map for the required severities.
        //
        // Example:
        // required = [2, 5, 2]
        // need = {2 -> 2, 5 -> 1}
        //
        // This means a valid window must contain:
        // - severity 2 at least 2 times
        // - severity 5 at least 1 time
        Map<Integer, Integer> need = buildFrequencyMap(required);

        // This value tells us how many distinct severity values must be satisfied.
        // In the example above, requiredDistinct = 2 because only severities 2 and 5 matter.
        int requiredDistinct = need.size();

        // Step 2:
        // This map stores counts of relevant severities inside the current window [left, right].
        Map<Integer, Integer> window = new HashMap<>();

        // formed tells us how many distinct required severities are currently satisfied.
        //
        // A severity is considered "satisfied" when:
        // windowCount(severity) >= needCount(severity)
        //
        // More precisely, we increment formed exactly when a severity reaches the required count.
        int formed = 0;

        // Sliding window left boundary.
        int left = 0;

        // Best answer found so far.
        int minLength = Integer.MAX_VALUE;

        // Step 3:
        // Expand the window by moving right from 0 to logs.length - 1.
        for (int right = 0; right < logs.length; right++) {
            int currentSeverity = logs[right];

            // We only care about severities that appear in the required map.
            // If a severity is irrelevant, it can still be inside the window,
            // but we do not need to track its count.
            if (need.containsKey(currentSeverity)) {
                int newCount = window.getOrDefault(currentSeverity, 0) + 1;
                window.put(currentSeverity, newCount);

                // If this severity has just reached exactly the required count,
                // then one more distinct requirement is now satisfied.
                if (newCount == need.get(currentSeverity)) {
                    formed++;
                }
            }

            // Step 4:
            // If all distinct required severities are satisfied,
            // then the current window is valid.
            //
            // Now try to shrink it from the left to make it as short as possible
            // while keeping it valid.
            while (formed == requiredDistinct && left <= right) {
                // Update the best answer using the current valid window length.
                int currentLength = right - left + 1;
                if (currentLength < minLength) {
                    minLength = currentLength;
                }

                int leftSeverity = logs[left];

                // We are about to remove logs[left] from the window by moving left forward.
                // If that severity is relevant, update the tracked count.
                if (need.containsKey(leftSeverity)) {
                    int updatedCount = window.get(leftSeverity) - 1;
                    window.put(leftSeverity, updatedCount);

                    // If the count drops below what is required,
                    // the window is no longer valid after this removal.
                    if (updatedCount < need.get(leftSeverity)) {
                        formed--;
                    }
                }

                // Actually shrink the window.
                left++;
            }
        }

        // If minLength was never updated, no valid window exists.
        return minLength == Integer.MAX_VALUE ? -1 : minLength;
    }

    /**
     * Builds a frequency map for the given array.
     *
     * Example:
     * input = [2, 5, 2]
     * output = {2 -> 2, 5 -> 1}
     *
     * @param values the input array whose element frequencies should be counted
     * @return a map from value to occurrence count
     *
     * Time complexity: O(n), where n = values.length.
     * Space complexity: O(k), where k is the number of distinct values.
     */
    public Map<Integer, Integer> buildFrequencyMap(int[] values) {
        Map<Integer, Integer> frequency = new HashMap<>();
        for (int value : values) {
            frequency.put(value, frequency.getOrDefault(value, 0) + 1);
        }
        return frequency;
    }

    /**
     * Runs a demonstration of the algorithm on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     *
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demo setup, excluding the called algorithm.
     * Space complexity: O(1), excluding the called algorithm's internal data structures.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1:
        // logs = [4, 2, 7, 2, 5, 1, 2, 5]
        // required = [2, 5, 2]
        //
        // Required counts:
        // 2 -> 2 times
        // 5 -> 1 time
        //
        // Shortest valid window is [2, 7, 2, 5], length = 4
        int[] logs1 = {4, 2, 7, 2, 5, 1, 2, 5};
        int[] required1 = {2, 5, 2};
        int result1 = solution.shortestLogSpan(logs1, required1);
        System.out.println("Example 1 Output: " + result1); // Expected: 4

        // Sample 2:
        // logs = [3, 1, 4, 1, 5, 9]
        // required = [1, 1, 2]
        //
        // Severity 2 never appears in logs, so no valid window exists.
        int[] logs2 = {3, 1, 4, 1, 5, 9};
        int[] required2 = {1, 1, 2};
        int result2 = solution.shortestLogSpan(logs2, required2);
        System.out.println("Example 2 Output: " + result2); // Expected: -1

        // Additional quick sanity checks for beginners:
        int[] logs3 = {2, 2, 5};
        int[] required3 = {2, 2, 5};
        System.out.println("Additional Test 1 Output: " + solution.shortestLogSpan(logs3, required3)); // Expected: 3

        int[] logs4 = {1, 2, 3, 4, 5};
        int[] required4 = {3};
        System.out.println("Additional Test 2 Output: " + solution.shortestLogSpan(logs4, required4)); // Expected: 1
    }
}