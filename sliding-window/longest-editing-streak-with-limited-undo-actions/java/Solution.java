import java.util.*;

/*
 * Title: Longest Editing Streak With Limited Undo Actions
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array `events` representing a user's editing timeline in a document editor.
 * Each element is either `1` or `0`:
 *
 * - `1` means the user made a productive edit during that minute.
 * - `0` means the minute was an undo, rollback, or other non-productive action.
 *
 * The product team wants to measure the longest continuous editing streak that can be considered
 * "mostly productive." A streak is valid if it contains at most `k` non-productive minutes.
 * In other words, you may include up to `k` zeros inside the chosen contiguous subarray.
 *
 * Return the length of the longest valid contiguous streak.
 *
 * This problem models a common analytics task where a noisy activity stream must be summarized
 * while tolerating a limited number of interruptions. A correct solution should run efficiently
 * on large inputs, so approaches that check every subarray will be too slow.
 *
 * Constraints:
 * - 1 <= events.length <= 200000
 * - 0 <= k <= events.length
 * - events[i] is either 0 or 1
 *
 * Example 1:
 * Input: events = [1,1,0,1,0,1,1,1], k = 1
 * Output: 4
 * Explanation:
 * The longest valid streak has length 4.
 * Any valid chosen window must contain at most one 0.
 * For example:
 * - indices 0..3 => [1,1,0,1] has one 0, valid, length 4
 * - indices 3..6 => [1,0,1,1] has one 0, valid, length 4
 * No length-5 window contains at most one 0.
 *
 * Example 2:
 * Input: events = [0,1,1,0,1,1,0,1], k = 2
 * Output: 7
 * Explanation:
 * The subarray [1,1,0,1,1,0,1] contains exactly two non-productive minutes, so its length is 7.
 * The full array has three zeros, which exceeds the limit.
 */

public class Solution {

    /**
     * Computes the maximum length of a contiguous subarray that contains at most k zeros.
     *
     * This method uses the classic sliding window technique:
     * - Expand the right side of the window one element at a time.
     * - Count how many zeros are currently inside the window.
     * - If the number of zeros becomes greater than k, move the left side forward
     *   until the window becomes valid again.
     * - Track the largest valid window length seen so far.
     *
     * Why this works:
     * - At every step, the window [left..right] is adjusted to be valid
     *   (contains at most k zeros).
     * - Since each index moves forward at most once, the algorithm is linear.
     *
     * @param events the binary array representing productive (1) and non-productive (0) minutes
     * @param k the maximum number of zeros allowed in a valid contiguous streak
     * @return the length of the longest contiguous subarray containing at most k zeros
     *
     * Time complexity: O(n), where n is events.length
     * Space complexity: O(1), because only a few variables are used
     */
    public int longestEditingStreak(int[] events, int k) {
        // 'left' marks the beginning of the current sliding window.
        int left = 0;

        // 'zeroCount' stores how many zeros are currently inside the window [left..right].
        int zeroCount = 0;

        // 'maxLength' stores the best answer found so far.
        int maxLength = 0;

        // Move 'right' from the start of the array to the end.
        // Each step expands the window by including events[right].
        for (int right = 0; right < events.length; right++) {

            // If the newly included element is 0, increase the zero count.
            if (events[right] == 0) {
                zeroCount++;
            }

            // If the window now contains too many zeros, it is invalid.
            // We must shrink it from the left until it becomes valid again.
            while (zeroCount > k) {

                // If the element leaving the window is 0, reduce zeroCount
                // because that zero is no longer inside the window.
                if (events[left] == 0) {
                    zeroCount--;
                }

                // Move the left boundary one step to the right.
                left++;
            }

            // At this point, the window [left..right] is guaranteed valid:
            // it contains at most k zeros.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is larger.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // Return the largest valid window length found.
        return maxLength;
    }

    /**
     * A helper method that prints an array in a readable format.
     *
     * @param arr the integer array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is arr.length
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * Also includes expected outputs so a beginner can compare the computed result
     * with the intended answer.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call to longestEditingStreak
     * Space complexity: O(1) extra space for the algorithm itself
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] events1 = {1, 1, 0, 1, 0, 1, 1, 1};
        int k1 = 1;
        int result1 = solution.longestEditingStreak(events1, k1);

        System.out.println("Sample 1:");
        System.out.println("events = " + solution.arrayToString(events1));
        System.out.println("k = " + k1);
        System.out.println("Longest valid streak = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        // Sample 2
        int[] events2 = {0, 1, 1, 0, 1, 1, 0, 1};
        int k2 = 2;
        int result2 = solution.longestEditingStreak(events2, k2);

        System.out.println("Sample 2:");
        System.out.println("events = " + solution.arrayToString(events2));
        System.out.println("k = " + k2);
        System.out.println("Longest valid streak = " + result2);
        System.out.println("Expected = 7");
        System.out.println();

        // Additional quick checks for beginners
        int[] events3 = {1, 1, 1, 1};
        int k3 = 0;
        System.out.println("Additional Check 1:");
        System.out.println("events = " + solution.arrayToString(events3));
        System.out.println("k = " + k3);
        System.out.println("Longest valid streak = " + solution.longestEditingStreak(events3, k3));
        System.out.println("Expected = 4");
        System.out.println();

        int[] events4 = {0, 0, 0, 0};
        int k4 = 2;
        System.out.println("Additional Check 2:");
        System.out.println("events = " + solution.arrayToString(events4));
        System.out.println("k = " + k4);
        System.out.println("Longest valid streak = " + solution.longestEditingStreak(events4, k4));
        System.out.println("Expected = 2");
        System.out.println();

        int[] events5 = {1, 0, 1, 0, 1, 0, 1};
        int k5 = 3;
        System.out.println("Additional Check 3:");
        System.out.println("events = " + solution.arrayToString(events5));
        System.out.println("k = " + k5);
        System.out.println("Longest valid streak = " + solution.longestEditingStreak(events5, k5));
        System.out.println("Expected = 7");
    }
}