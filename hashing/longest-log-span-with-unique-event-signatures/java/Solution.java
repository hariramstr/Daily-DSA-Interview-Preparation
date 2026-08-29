import java.util.*;

/*
 * Title: Longest Log Span With Unique Event Signatures
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given an array events where events[i] is a string representing the signature
 * of the i-th system log entry in chronological order. A monitoring team wants to extract
 * the longest contiguous span of logs such that no event signature appears more than once
 * inside that span.
 *
 * Return the length of the longest contiguous subarray of events that contains only unique strings.
 *
 * Two log entries are considered the same if their signature strings are exactly equal.
 * The span must be contiguous, meaning you may only choose entries between some left index
 * and right index without skipping any logs.
 *
 * This problem models a common production debugging task: analysts often want the longest
 * time window without repeated event types so that they can study a "clean" sequence of
 * unique failures, warnings, and state changes.
 *
 * Constraints:
 * - 1 <= events.length <= 100000
 * - 1 <= events[i].length <= 50
 * - events[i] consists of lowercase English letters, digits, underscores, and hyphens
 * - The answer must be computed in O(n) or O(n log n) time for full credit
 *
 * Example 1:
 * Input: events = ["auth_ok", "cache_miss", "db_retry", "cache_miss", "email_sent"]
 * Output: 3
 * Explanation: The longest valid span is ["auth_ok", "cache_miss", "db_retry"]
 * or ["db_retry", "cache_miss", "email_sent"], both of length 3.
 *
 * Example 2:
 * Input: events = ["x1", "x2", "x3", "x2", "x4", "x5"]
 * Output: 4
 * Explanation: One longest valid span is ["x3", "x2", "x4", "x5"].
 * No signature repeats within this contiguous segment.
 */

public class Solution {

    /**
     * Computes the length of the longest contiguous subarray containing only unique event signatures.
     *
     * This method uses the classic sliding window technique:
     * - Expand the window by moving the right pointer one step at a time.
     * - Track the most recent index where each event signature appeared.
     * - If the current event was already seen inside the current window, move the left pointer
     *   just past the previous occurrence so the window becomes valid again.
     * - Continuously update the maximum valid window length.
     *
     * @param events the array of event signature strings in chronological order
     * @return the maximum length of a contiguous span with all unique event signatures
     * Time complexity: O(n), where n is the number of events
     * Space complexity: O(n), in the worst case for the hash map of last seen indices
     */
    public int longestUniqueSpan(String[] events) {
        // This map stores:
        // key   -> event signature string
        // value -> the most recent index where that signature was seen
        //
        // Example:
        // if we have processed:
        // ["a", "b", "c", "b"]
        // then the map would contain:
        // "a" -> 0
        // "b" -> 3
        // "c" -> 2
        Map<String, Integer> lastSeenIndex = new HashMap<>();

        // 'left' is the start index of our current sliding window.
        // The window always represents a contiguous segment [left, right]
        // that we try to keep free of duplicates.
        int left = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Move 'right' from left to right across the array.
        // At each step, we include events[right] into the current window.
        for (int right = 0; right < events.length; right++) {
            String currentEvent = events[right];

            // If this event signature has been seen before, it may create a duplicate.
            if (lastSeenIndex.containsKey(currentEvent)) {
                int previousIndex = lastSeenIndex.get(currentEvent);

                // Very important detail:
                // We only move 'left' forward if the previous occurrence is still inside
                // the current window.
                //
                // Why use Math.max(left, previousIndex + 1)?
                // Because:
                // - If previousIndex < left, then that old duplicate is already outside
                //   the current window, so we should NOT move left backward.
                // - If previousIndex >= left, then the duplicate is inside the window,
                //   so we must move left to previousIndex + 1 to remove the duplicate.
                left = Math.max(left, previousIndex + 1);
            }

            // Update the most recent position of the current event signature.
            lastSeenIndex.put(currentEvent, right);

            // Current valid window is from index 'left' to index 'right', inclusive.
            // So its length is:
            int currentWindowLength = right - left + 1;

            // Update the answer if this window is the largest valid one seen so far.
            maxLength = Math.max(maxLength, currentWindowLength);
        }

        return maxLength;
    }

    /**
     * Helper method to print an array of strings in a readable format.
     *
     * @param events the array of event signatures to print
     * @return a formatted string representation of the array
     * Time complexity: O(n), where n is the number of events
     * Space complexity: O(n), due to building the output string
     */
    public String arrayToString(String[] events) {
        return Arrays.toString(events);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional beginner-friendly test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total number of elements across demonstrated test cases)
     * Space complexity: O(total distinct elements in each test case during processing)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Input 1
        String[] events1 = {"auth_ok", "cache_miss", "db_retry", "cache_miss", "email_sent"};
        int result1 = solution.longestUniqueSpan(events1);
        System.out.println("Input:  " + solution.arrayToString(events1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 3");
        System.out.println();

        // Sample Input 2
        String[] events2 = {"x1", "x2", "x3", "x2", "x4", "x5"};
        int result2 = solution.longestUniqueSpan(events2);
        System.out.println("Input:  " + solution.arrayToString(events2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional test: all unique
        String[] events3 = {"login", "fetch_profile", "render_home", "logout"};
        int result3 = solution.longestUniqueSpan(events3);
        System.out.println("Input:  " + solution.arrayToString(events3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional test: all same
        String[] events4 = {"dup", "dup", "dup", "dup"};
        int result4 = solution.longestUniqueSpan(events4);
        System.out.println("Input:  " + solution.arrayToString(events4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 1");
        System.out.println();

        // Additional test: duplicate appears after window has moved
        String[] events5 = {"a", "b", "c", "a", "d", "e"};
        int result5 = solution.longestUniqueSpan(events5);
        System.out.println("Input:  " + solution.arrayToString(events5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 5");
    }
}