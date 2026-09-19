import java.util.*;

/*
 * Title: Longest Badge Scan Streak Under Duplicate Limit
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * A security team is analyzing a hallway badge scanner that records employee badge IDs
 * in the order they were scanned. Because people may walk back and forth, the same badge ID
 * can appear multiple times in a row or later in the log. The team wants to find the longest
 * contiguous portion of the scan log that is still considered "clean" under a simple rule:
 * within that portion, no badge ID may appear more than k times.
 *
 * Given an array scans where scans[i] is the badge ID seen at time i, and an integer k,
 * return the length of the longest contiguous subarray such that every distinct badge ID
 * in that subarray appears at most k times.
 *
 * This is an interview-style sliding window problem. A good solution should expand the right
 * side of the window and shrink the left side only when some badge ID appears too many times.
 *
 * Constraints:
 * - 1 <= scans.length <= 100000
 * - 1 <= scans[i] <= 1000000000
 * - 1 <= k <= scans.length
 * - The answer fits in a 32-bit integer
 *
 * Example 1:
 * Input: scans = [5, 7, 5, 7, 5, 8], k = 2
 * Output: 5
 * Explanation: The longest valid window is [7, 5, 7, 5, 8].
 * In this subarray, badge 7 appears 2 times, badge 5 appears 2 times, and badge 8 appears 1 time.
 *
 * Example 2:
 * Input: scans = [3, 3, 3, 2, 2, 1], k = 1
 * Output: 2
 * Explanation: With k = 1, all badge IDs inside the chosen window must be unique.
 * The longest valid windows have length 2, such as [3, 2] or [2, 1].
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray such that
     * every distinct value appears at most k times.
     *
     * We use the classic sliding window technique:
     * 1. Expand the right boundary one step at a time.
     * 2. Track frequencies of values inside the current window.
     * 3. If the newly added value appears more than k times, shrink the left boundary
     *    until the window becomes valid again.
     * 4. Record the maximum valid window length seen so far.
     *
     * @param scans the array of badge scan IDs in chronological order
     * @param k the maximum allowed frequency for any badge ID inside a valid window
     * @return the length of the longest valid contiguous subarray
     *
     * Time complexity: O(n), where n is scans.length, because each index is moved
     * at most once by the left pointer and once by the right pointer.
     * Space complexity: O(m), where m is the number of distinct badge IDs currently
     * tracked in the frequency map, up to O(n) in the worst case.
     */
    public int longestCleanStreak(int[] scans, int k) {
        // Frequency map:
        // key   -> badge ID
        // value -> how many times that badge ID appears inside the current window
        Map<Integer, Integer> frequency = new HashMap<>();

        // left marks the start of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // Move right from 0 to scans.length - 1, expanding the window one element at a time.
        for (int right = 0; right < scans.length; right++) {
            int currentBadge = scans[right];

            // Include scans[right] in the window by increasing its frequency.
            frequency.put(currentBadge, frequency.getOrDefault(currentBadge, 0) + 1);

            // If the current badge now appears too many times,
            // the window is invalid and must be shrunk from the left.
            //
            // Important observation:
            // Before adding currentBadge, the window was valid.
            // After adding it, only currentBadge can possibly violate the rule.
            // So we only need to check frequency.get(currentBadge) > k.
            while (frequency.get(currentBadge) > k) {
                int leftBadge = scans[left];

                // Remove scans[left] from the window by decreasing its frequency.
                frequency.put(leftBadge, frequency.get(leftBadge) - 1);

                // Optional cleanup:
                // If a frequency becomes zero, remove it from the map.
                // This is not required for correctness, but keeps the map tidy.
                if (frequency.get(leftBadge) == 0) {
                    frequency.remove(leftBadge);
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // At this point, the window [left, right] is valid:
            // every badge ID appears at most k times.
            int windowLength = right - left + 1;

            // Update the best answer if this valid window is larger than any seen before.
            best = Math.max(best, windowLength);
        }

        return best;
    }

    /**
     * A small helper method to print an array in a beginner-friendly format.
     *
     * @param scans the array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is scans.length.
     * Space complexity: O(n), due to the created string content.
     */
    public String arrayToString(int[] scans) {
        return Arrays.toString(scans);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * Expected outputs:
     * - Example 1: 5
     * - Example 2: 2
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) across the demonstrated test cases.
     * Space complexity: O(n) in the worst case due to the frequency map.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] scans1 = {5, 7, 5, 7, 5, 8};
        int k1 = 2;
        int result1 = solution.longestCleanStreak(scans1, k1);

        System.out.println("Example 1");
        System.out.println("scans = " + solution.arrayToString(scans1));
        System.out.println("k = " + k1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 5");
        System.out.println();

        // Example 2
        int[] scans2 = {3, 3, 3, 2, 2, 1};
        int k2 = 1;
        int result2 = solution.longestCleanStreak(scans2, k2);

        System.out.println("Example 2");
        System.out.println("scans = " + solution.arrayToString(scans2));
        System.out.println("k = " + k2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 2");
        System.out.println();

        // Additional quick demonstration
        int[] scans3 = {1, 2, 1, 2, 1, 2, 3};
        int k3 = 2;
        int result3 = solution.longestCleanStreak(scans3, k3);

        System.out.println("Additional Example");
        System.out.println("scans = " + solution.arrayToString(scans3));
        System.out.println("k = " + k3);
        System.out.println("Output = " + result3);
    }
}