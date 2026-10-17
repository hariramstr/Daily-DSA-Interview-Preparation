import java.util.*;

/*
 * Title: Longest Sleep Tracker Streak With Limited Wake Spikes
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A wearable device records a user's sleep state once per minute. The data is stored in an integer array states
 * where each value is either 0 or 1. A value of 1 means the user was asleep during that minute, and 0 means
 * they were awake. Because short wake-ups are common and can be ignored by the app, the product team wants
 * to measure the longest continuous sleep streak if you are allowed to treat at most k awake minutes as sleep.
 *
 * Your task is to return the length of the longest contiguous subarray that can be considered a sleep streak
 * after converting at most k values of 0 into 1. In other words, find the maximum number of consecutive minutes
 * that contain no more than k awake samples.
 *
 * This is a realistic streaming-analysis problem: the chosen interval must remain contiguous in the original
 * timeline, and you may only ignore up to k wake spikes inside that interval.
 *
 * Constraints:
 * - 1 <= states.length <= 200000
 * - states[i] is either 0 or 1
 * - 0 <= k <= states.length
 *
 * Example 1:
 * Input: states = [1,1,0,1,0,1,1,1], k = 1
 * Output: 5
 * Explanation: Treat either awake minute at index 2 or index 4 as sleep. The longest valid streak is length 5,
 * such as [1,0,1,0,1] is not valid because it contains two awake minutes, but [1,0,1,1,1] after one conversion
 * is valid.
 *
 * Example 2:
 * Input: states = [0,0,1,1,1,0,1,1,0], k = 2
 * Output: 7
 * Explanation: Convert the awake minutes at indices 5 and 8, giving a longest contiguous valid streak from
 * index 2 to index 8 with length 7.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray that contains at most {@code k} zeros.
     * Each zero represents an awake minute that we are allowed to treat as sleep.
     *
     * The core idea is a sliding window:
     * - Expand the right boundary one step at a time.
     * - Count how many zeros are currently inside the window.
     * - If the window becomes invalid (more than k zeros), move the left boundary rightward
     *   until the window becomes valid again.
     * - Track the maximum valid window length seen during the scan.
     *
     * @param states the sleep-state array where 1 means asleep and 0 means awake
     * @param k the maximum number of awake minutes allowed inside the chosen contiguous interval
     * @return the maximum length of a contiguous interval containing at most k zeros
     *
     * Time complexity: O(n), where n is states.length, because each index is visited at most twice
     * (once by the right pointer and once by the left pointer).
     * Space complexity: O(1), because only a few integer variables are used.
     */
    public int longestSleepStreak(int[] states, int k) {
        // Defensive handling for null or empty input.
        // The problem constraints guarantee at least one element, but this makes the method safer.
        if (states == null || states.length == 0) {
            return 0;
        }

        // 'left' marks the start of the current sliding window.
        int left = 0;

        // 'zeroCount' stores how many awake minutes (0s) are currently inside the window [left, right].
        int zeroCount = 0;

        // 'bestLength' stores the maximum valid window length found so far.
        int bestLength = 0;

        // Move 'right' from the beginning to the end of the array.
        for (int right = 0; right < states.length; right++) {
            // Step 1: Include states[right] into the current window.
            // If it is a zero, that means we are using one of our allowed wake-spike ignores.
            if (states[right] == 0) {
                zeroCount++;
            }

            // Step 2: If the window now contains too many zeros, it is invalid.
            // We must shrink it from the left until it becomes valid again.
            while (zeroCount > k) {
                // Before moving 'left' forward, check whether the outgoing element is a zero.
                // If yes, removing it decreases the zero count.
                if (states[left] == 0) {
                    zeroCount--;
                }

                // Move the left boundary rightward by one position.
                left++;
            }

            // Step 3: At this point, the window [left, right] is guaranteed valid:
            // it contains at most k zeros.
            // Compute its length and update the best answer if needed.
            int currentLength = right - left + 1;
            if (currentLength > bestLength) {
                bestLength = currentLength;
            }
        }

        // After scanning the full array, bestLength is the answer.
        return bestLength;
    }

    /**
     * A helper method that runs the algorithm and prints the input and output in a beginner-friendly format.
     *
     * @param states the sleep-state array to test
     * @param k the maximum number of zeros allowed in the valid window
     * @return the computed longest valid streak length
     *
     * Time complexity: O(n), where n is states.length, due to the call to longestSleepStreak.
     * Space complexity: O(1), excluding the temporary string created by Arrays.toString for printing.
     */
    public int demonstrateCase(int[] states, int k) {
        int result = longestSleepStreak(states, k);
        System.out.println("states = " + Arrays.toString(states));
        System.out.println("k = " + k);
        System.out.println("Longest sleep streak = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     * Demonstrates the solution on the sample inputs from the problem statement
     * and a few additional quick checks.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(total input size across demonstrated test cases).
     * Space complexity: O(1), excluding printing-related temporary objects.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the problem statement.
        // states = [1,1,0,1,0,1,1,1], k = 1
        // Correct answer: 5
        int[] sample1 = {1, 1, 0, 1, 0, 1, 1, 1};
        int result1 = solution.demonstrateCase(sample1, 1);
        System.out.println("Expected: 5, Actual: " + result1);
        System.out.println();

        // Sample 2 from the problem statement.
        // states = [0,0,1,1,1,0,1,1,0], k = 2
        // Correct answer: 7
        int[] sample2 = {0, 0, 1, 1, 1, 0, 1, 1, 0};
        int result2 = solution.demonstrateCase(sample2, 2);
        System.out.println("Expected: 7, Actual: " + result2);
        System.out.println();

        // Additional quick checks.

        // All asleep already.
        int[] extra1 = {1, 1, 1, 1};
        int result3 = solution.demonstrateCase(extra1, 0);
        System.out.println("Expected: 4, Actual: " + result3);
        System.out.println();

        // No conversions allowed, so answer is longest run of 1s.
        int[] extra2 = {1, 0, 1, 1, 0, 1};
        int result4 = solution.demonstrateCase(extra2, 0);
        System.out.println("Expected: 2, Actual: " + result4);
        System.out.println();

        // Enough conversions to cover all zeros.
        int[] extra3 = {0, 1, 0, 1, 0};
        int result5 = solution.demonstrateCase(extra3, 3);
        System.out.println("Expected: 5, Actual: " + result5);
    }
}