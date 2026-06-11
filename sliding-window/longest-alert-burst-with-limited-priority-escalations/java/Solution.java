import java.util.*;

/*
 * Title: Longest Alert Burst With Limited Priority Escalations
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A monitoring system records a stream of alert priorities over time as a string alerts,
 * where each character is either 'L' (low priority) or 'H' (high priority).
 * The operations team wants to identify the longest contiguous burst of alerts that can be
 * treated as a mostly low-priority incident window.
 *
 * You are allowed to escalate at most k high-priority alerts inside a chosen contiguous window,
 * meaning those 'H' alerts can be treated as if they were 'L' for reporting purposes.
 * Return the length of the longest contiguous substring that can be made entirely low priority
 * after at most k such escalations.
 *
 * In other words, find the maximum window length containing at most k occurrences of 'H'.
 *
 * This problem models real incident analysis, where a team may tolerate a small number of severe
 * alerts inside an otherwise routine burst. An efficient solution is expected because the alert
 * stream can be very large.
 *
 * Constraints:
 * - 1 <= alerts.length <= 200000
 * - alerts[i] is either 'L' or 'H'
 * - 0 <= k <= alerts.length
 *
 * Example 1:
 * Input: alerts = "LLHLLHLLL", k = 1
 * Output: 5
 * Explanation: The window "LHLLL" contains exactly one 'H', so it can be fully treated as
 * low priority after one escalation. No longer valid window exists.
 *
 * Example 2:
 * Input: alerts = "HHLLLHLH", k = 2
 * Output: 6
 * Explanation: One optimal window is "HLLLHL", which contains two 'H' characters.
 * After escalating both, the entire window is considered low priority, giving length 6.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous substring that contains at most k occurrences of 'H'.
     *
     * This uses the classic sliding window technique:
     * - Expand the right boundary one character at a time.
     * - Count how many 'H' characters are inside the current window.
     * - If the count becomes greater than k, move the left boundary rightward until the window
     *   becomes valid again.
     * - Track the maximum valid window length seen so far.
     *
     * @param alerts the alert stream consisting only of 'L' and 'H'
     * @param k the maximum number of high-priority alerts that may be escalated inside the window
     * @return the maximum length of a contiguous window containing at most k 'H' characters
     * Time complexity: O(n), where n is alerts.length, because each character is processed at most twice
     * Space complexity: O(1), because only a few variables are used
     */
    public int longestAlertBurst(String alerts, int k) {
        // Left boundary of the current sliding window.
        int left = 0;

        // Number of 'H' characters currently inside the window [left, right].
        int highCount = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Move the right boundary from the start of the string to the end.
        for (int right = 0; right < alerts.length(); right++) {
            // Step 1:
            // Include alerts.charAt(right) into the current window.
            // If it is 'H', then this window now contains one more high-priority alert.
            if (alerts.charAt(right) == 'H') {
                highCount++;
            }

            // Step 2:
            // If the window has too many 'H' characters, it is invalid.
            // We are allowed at most k high-priority alerts in the window.
            //
            // So while the window is invalid, keep shrinking it from the left side.
            while (highCount > k) {
                // Before moving left forward, remove the effect of alerts.charAt(left)
                // from the current window.
                if (alerts.charAt(left) == 'H') {
                    highCount--;
                }

                // Actually shrink the window by moving the left boundary rightward.
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is guaranteed to be valid:
            // it contains at most k 'H' characters.
            //
            // Compute its length.
            int currentLength = right - left + 1;

            // Step 4:
            // Update the best answer if this valid window is larger than any previous one.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // After scanning the whole string, maxLength is the answer.
        return maxLength;
    }

    /**
     * A helper method that prints a demonstration run for a single test case.
     *
     * @param alerts the alert stream consisting of 'L' and 'H'
     * @param k the maximum number of allowed escalations
     * @return the computed longest valid window length for the provided input
     * Time complexity: O(n), where n is alerts.length, because it delegates to the main algorithm
     * Space complexity: O(1), excluding output printing
     */
    public int demonstrateCase(String alerts, int k) {
        int result = longestAlertBurst(alerts, k);
        System.out.println("alerts = " + alerts + ", k = " + k + " -> " + result);
        return result;
    }

    /**
     * Program entry point.
     *
     * Demonstrates the algorithm on the sample inputs from the problem statement
     * and a few additional cases for clarity.
     *
     * Verified sample outputs:
     * - "LLHLLHLLL", k = 1 -> 5
     * - "HHLLLHLH", k = 2 -> 6
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of demonstrated test cases)
     * Space complexity: O(1), excluding JVM/runtime overhead
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample case 1 from the problem statement.
        // Valid longest window length is 5.
        solution.demonstrateCase("LLHLLHLLL", 1);

        // Sample case 2 from the problem statement.
        // Valid longest window length is 6.
        solution.demonstrateCase("HHLLLHLH", 2);

        // Additional beginner-friendly checks.
        solution.demonstrateCase("LLLLL", 0);     // All low priority, answer should be 5
        solution.demonstrateCase("HHHHH", 2);     // Can include at most 2 H's, answer should be 2
        solution.demonstrateCase("LHLHLHLH", 3);  // Mixed pattern
        solution.demonstrateCase("H", 0);         // Single high, cannot escalate, answer should be 0
        solution.demonstrateCase("H", 1);         // Single high, can escalate, answer should be 1
    }
}