import java.util.*;

/*
 * Title: Longest Chat Streak With At Most One Silent Minute
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a binary array messages representing a minute-by-minute chat activity log
 * for a support agent. Each element is either 1 or 0, where 1 means the agent sent at least
 * one message during that minute, and 0 means the minute was silent.
 *
 * A chat streak is defined as a contiguous block of minutes that is considered active if you
 * are allowed to ignore at most one silent minute inside the block.
 *
 * Your task is to return the length of the longest possible active chat streak.
 *
 * In other words, find the maximum length of a contiguous subarray containing at most one 0.
 *
 * This models a real analytics scenario where a single short pause should not necessarily
 * break an otherwise continuous support conversation. The solution should be efficient enough
 * for large logs, so a sliding window approach is expected.
 *
 * Constraints:
 * - 1 <= messages.length <= 100000
 * - messages[i] is either 0 or 1
 *
 * Example 1:
 * Input: messages = [1,1,0,1,1,1,0,1]
 * Output: 6
 * Explanation:
 * The longest valid window is [1,1,0,1,1,1], which has length 6 and contains only one 0.
 *
 * Example 2:
 * Input: messages = [0,1,1,1,0,1,1]
 * Output: 5
 * Explanation:
 * The longest valid streak is [1,1,1,0,1] or [1,1,0,1,1].
 * Each has exactly one silent minute, so the answer is 5.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray that contains at most one 0.
     *
     * This method uses the classic sliding window technique:
     * - Expand the right side of the window one element at a time.
     * - Count how many zeros are currently inside the window.
     * - If the window becomes invalid (more than one zero), move the left side forward
     *   until the window becomes valid again.
     * - Track the maximum valid window length seen so far.
     *
     * @param messages the binary array representing minute-by-minute chat activity,
     *                 where 1 means active and 0 means silent
     * @return the maximum length of a contiguous subarray containing at most one 0
     * Time complexity: O(n), because each index is visited at most twice
     * Space complexity: O(1), because only a few variables are used
     */
    public int longestChatStreak(int[] messages) {
        // The left boundary of our sliding window.
        int left = 0;

        // This will store how many silent minutes (zeros) are currently inside the window.
        int zeroCount = 0;

        // This keeps track of the best / longest valid window length found so far.
        int maxLength = 0;

        // We expand the window by moving 'right' from the start to the end of the array.
        for (int right = 0; right < messages.length; right++) {

            // Step 1:
            // Include messages[right] into the current window.
            // If it is a silent minute (0), increase zeroCount.
            if (messages[right] == 0) {
                zeroCount++;
            }

            // Step 2:
            // If the window now contains more than one zero,
            // it is no longer valid according to the problem.
            //
            // So we must shrink the window from the left side
            // until the window contains at most one zero again.
            while (zeroCount > 1) {

                // Before moving left forward, check whether the element leaving the window
                // is a zero. If yes, we must decrease zeroCount because that zero is no
                // longer inside the window.
                if (messages[left] == 0) {
                    zeroCount--;
                }

                // Move the left boundary one step to the right,
                // effectively removing messages[left] from the window.
                left++;
            }

            // Step 3:
            // At this point, the window [left ... right] is guaranteed to be valid,
            // meaning it contains at most one zero.
            //
            // Its length is:
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is longer than anything seen before.
            maxLength = Math.max(maxLength, currentLength);
        }

        // After checking all possible windows through the sliding process,
        // maxLength contains the answer.
        return maxLength;
    }

    /**
     * Helper method to convert an integer array into a readable string form.
     * This is used only for demonstration in main.
     *
     * @param arr the input integer array
     * @return a string representation of the array
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional beginner-friendly test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per test case for calling the algorithm
     * Space complexity: O(1) extra for the algorithm itself
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample test case 1
        int[] messages1 = {1, 1, 0, 1, 1, 1, 0, 1};
        int result1 = solution.longestChatStreak(messages1);
        System.out.println("Input:  " + solution.arrayToString(messages1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 6");
        System.out.println();

        // Sample test case 2
        int[] messages2 = {0, 1, 1, 1, 0, 1, 1};
        int result2 = solution.longestChatStreak(messages2);
        System.out.println("Input:  " + solution.arrayToString(messages2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 5");
        System.out.println();

        // Additional test case: all active minutes
        int[] messages3 = {1, 1, 1, 1};
        int result3 = solution.longestChatStreak(messages3);
        System.out.println("Input:  " + solution.arrayToString(messages3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional test case: all silent minutes
        int[] messages4 = {0, 0, 0};
        int result4 = solution.longestChatStreak(messages4);
        System.out.println("Input:  " + solution.arrayToString(messages4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 1");
        System.out.println();

        // Additional test case: single element
        int[] messages5 = {0};
        int result5 = solution.longestChatStreak(messages5);
        System.out.println("Input:  " + solution.arrayToString(messages5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 1");
        System.out.println();
    }
}