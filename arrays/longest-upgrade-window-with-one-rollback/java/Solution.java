import java.util.*;

/*
 * Title: Longest Upgrade Window With One Rollback
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A deployment team records the result of each software upgrade attempt in chronological order
 * using an integer array status, where 1 means the upgrade at that minute succeeded and 0 means
 * it failed. The team is allowed to perform at most one rollback operation on a single failed
 * attempt, turning one 0 into a 1. Your task is to find the length of the longest contiguous
 * time window that can consist entirely of successful upgrades after applying at most one rollback.
 *
 * Return the maximum possible length of such a contiguous window.
 *
 * This is an arrays problem focused on finding an optimal continuous segment efficiently.
 * A brute-force check of every subarray will be too slow for large inputs, so you should design
 * an algorithm that runs in linear time.
 *
 * Constraints:
 * - 1 <= status.length <= 100000
 * - status[i] is either 0 or 1
 *
 * Example 1:
 * Input: status = [1,1,0,1,1,1,0,1]
 * Output: 6
 * Explanation: Roll back the failure at index 2. The subarray [1,1,0,1,1,1] becomes six
 * consecutive successes.
 *
 * Example 2:
 * Input: status = [0,1,1,0,1,0,1,1]
 * Output: 4
 * Explanation: Roll back the failure at index 3. Then the window [1,1,0,1] becomes four
 * consecutive successes. No longer contiguous window can be made all 1s using only one rollback.
 *
 * Notes:
 * - You may choose not to use the rollback if the array already contains all successful upgrades.
 * - The rollback can be applied to any single failed attempt, but only once.
 * - The answer must be based on a contiguous subarray, not scattered positions.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray that can be made entirely of 1s
     * by flipping at most one 0 to 1.
     *
     * The key idea is to use a sliding window:
     * - Expand the right side of the window one element at a time.
     * - Count how many zeros are currently inside the window.
     * - If the window contains more than one zero, it is no longer valid because we are allowed
     *   to flip at most one failed attempt.
     * - So we move the left side forward until the window becomes valid again.
     * - At every step, the current valid window length is a candidate answer.
     *
     * @param status the array of upgrade results, where 1 means success and 0 means failure
     * @return the maximum possible length of a contiguous window that can become all 1s
     *         after at most one rollback
     *
     * Time complexity: O(n), because each index is visited at most twice
     *                  (once by the right pointer and once by the left pointer).
     * Space complexity: O(1), because only a few variables are used.
     */
    public int longestUpgradeWindow(int[] status) {
        // Left boundary of the sliding window.
        int left = 0;

        // Number of zeros currently inside the window [left, right].
        int zeroCount = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < status.length; right++) {

            // If the new element entering the window is 0,
            // increase the zero count because this is a failed attempt.
            if (status[right] == 0) {
                zeroCount++;
            }

            // If we now have more than one zero in the window,
            // the window is invalid because we can flip at most one zero.
            //
            // So we must shrink the window from the left side until
            // there is at most one zero again.
            while (zeroCount > 1) {

                // If the element leaving the window is 0,
                // we are removing one failed attempt from the window,
                // so decrease the zero count.
                if (status[left] == 0) {
                    zeroCount--;
                }

                // Move the left boundary to the right to shrink the window.
                left++;
            }

            // At this point, the window [left, right] contains at most one zero,
            // so it is a valid candidate:
            // - If it contains no zeros, it is already all 1s.
            // - If it contains one zero, we can flip that zero to 1.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is larger.
            maxLength = Math.max(maxLength, currentLength);
        }

        return maxLength;
    }

    /**
     * Helper method to print an array in a beginner-friendly format.
     *
     * @param arr the integer array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n), due to building the output string.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demo cases shown here, excluding the cost
     *                  of the algorithm on each sample input.
     * Space complexity: O(1), excluding input storage.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1 from the problem statement.
        int[] status1 = {1, 1, 0, 1, 1, 1, 0, 1};
        int result1 = solution.longestUpgradeWindow(status1);
        System.out.println("Input:  " + solution.arrayToString(status1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 6");
        System.out.println();

        // Sample input 2 from the problem statement.
        int[] status2 = {0, 1, 1, 0, 1, 0, 1, 1};
        int result2 = solution.longestUpgradeWindow(status2);
        System.out.println("Input:  " + solution.arrayToString(status2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional example: all successes, no rollback needed.
        int[] status3 = {1, 1, 1, 1};
        int result3 = solution.longestUpgradeWindow(status3);
        System.out.println("Input:  " + solution.arrayToString(status3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional example: single failure can be rolled back.
        int[] status4 = {0};
        int result4 = solution.longestUpgradeWindow(status4);
        System.out.println("Input:  " + solution.arrayToString(status4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 1");
        System.out.println();

        // Additional example: mixed values.
        int[] status5 = {1, 0, 1, 1, 0, 1};
        int result5 = solution.longestUpgradeWindow(status5);
        System.out.println("Input:  " + solution.arrayToString(status5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 4");
    }
}