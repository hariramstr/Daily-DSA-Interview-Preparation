import java.util.*;

/*
 * Title: Longest Study Window With Limited Difficult Problems
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array problems where problems[i] is the difficulty rating of the i-th practice problem
 * in the order a student solved them. You are also given two integers threshold and k.
 * A problem is considered difficult if its difficulty rating is greater than or equal to threshold.
 *
 * Find the length of the longest contiguous window of solved problems that contains at most k difficult problems.
 *
 * In other words, you may choose any consecutive segment of the array, but inside that segment the number of
 * values greater than or equal to threshold must not exceed k. Return the maximum possible length of such a segment.
 *
 * This models a realistic interview-style scenario where you want to identify the longest sustained practice streak
 * that does not contain too many high-difficulty interruptions.
 *
 * Constraints:
 * - 1 <= problems.length <= 200000
 * - 0 <= problems[i] <= 1000000000
 * - 0 <= k <= problems.length
 * - 0 <= threshold <= 1000000000
 *
 * Example 1:
 * Input: problems = [2, 7, 3, 9, 4, 8, 1], threshold = 7, k = 2
 * Output: 5
 * Explanation: Difficult problems are those with value >= 7, so the difficult entries are 7, 9, and 8.
 * The longest valid contiguous window with at most 2 difficult problems is [7, 3, 9, 4, 8] or [2, 7, 3, 9, 4],
 * both of length 5.
 *
 * Example 2:
 * Input: problems = [10, 1, 1, 10, 1, 10, 1, 1], threshold = 10, k = 1
 * Output: 4
 * Explanation: Any valid window may contain at most one value >= 10.
 * One optimal window is [1, 1, 10, 1], which has length 4.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray that contains at most k difficult problems.
     *
     * A problem is considered difficult if its value is greater than or equal to the given threshold.
     * This method uses the classic sliding window / two-pointer technique:
     *
     * 1. Expand the right boundary one step at a time.
     * 2. Count how many difficult problems are currently inside the window.
     * 3. If the count becomes larger than k, move the left boundary rightward until the window becomes valid again.
     * 4. Track the maximum valid window length seen so far.
     *
     * @param problems the array of problem difficulty ratings in solved order
     * @param threshold the minimum value that makes a problem difficult
     * @param k the maximum number of difficult problems allowed inside a valid window
     * @return the maximum length of a contiguous window containing at most k difficult problems
     *
     * Time complexity: O(n), where n is problems.length, because each index is visited at most twice
     * (once by the right pointer and once by the left pointer).
     * Space complexity: O(1), because only a few integer variables are used.
     */
    public int longestStudyWindow(int[] problems, int threshold, int k) {
        // Left boundary of the sliding window.
        int left = 0;

        // Number of difficult problems currently inside the window [left, right].
        int difficultCount = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < problems.length; right++) {

            // Step 1:
            // Include problems[right] into the current window.
            // If it is difficult, increase the difficult problem count.
            if (isDifficult(problems[right], threshold)) {
                difficultCount++;
            }

            // Step 2:
            // If the window now contains too many difficult problems,
            // shrink it from the left until it becomes valid again.
            //
            // Why a while loop instead of if?
            // Because removing just one element may still leave the window invalid.
            while (difficultCount > k) {

                // Before moving left forward, check whether the element leaving the window
                // is difficult. If yes, reduce the difficult count.
                if (isDifficult(problems[left], threshold)) {
                    difficultCount--;
                }

                // Actually remove the leftmost element from the window by advancing left.
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is guaranteed valid:
            // it contains at most k difficult problems.
            //
            // Compute its length.
            int currentLength = right - left + 1;

            // Step 4:
            // Update the best answer if this valid window is larger than any previous one.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // After scanning the entire array, maxLength holds the answer.
        return maxLength;
    }

    /**
     * Determines whether a single problem is difficult.
     *
     * @param value the difficulty rating of one problem
     * @param threshold the minimum value that classifies a problem as difficult
     * @return true if value >= threshold, otherwise false
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public boolean isDifficult(int value, int threshold) {
        return value >= threshold;
    }

    /**
     * Runs the sample demonstrations from the problem statement and prints the results.
     *
     * This main method is intentionally beginner-friendly:
     * - It creates the sample inputs
     * - Calls the solution method
     * - Prints both the expected and actual outputs
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call, where n is the input array length
     * Space complexity: O(1), excluding the input arrays created for testing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] problems1 = {2, 7, 3, 9, 4, 8, 1};
        int threshold1 = 7;
        int k1 = 2;
        int result1 = solution.longestStudyWindow(problems1, threshold1, k1);

        System.out.println("Example 1:");
        System.out.println("Problems: " + Arrays.toString(problems1));
        System.out.println("Threshold: " + threshold1);
        System.out.println("k: " + k1);
        System.out.println("Expected Output: 5");
        System.out.println("Actual Output: " + result1);
        System.out.println();

        // Example 2
        int[] problems2 = {10, 1, 1, 10, 1, 10, 1, 1};
        int threshold2 = 10;
        int k2 = 1;
        int result2 = solution.longestStudyWindow(problems2, threshold2, k2);

        System.out.println("Example 2:");
        System.out.println("Problems: " + Arrays.toString(problems2));
        System.out.println("Threshold: " + threshold2);
        System.out.println("k: " + k2);
        System.out.println("Expected Output: 4");
        System.out.println("Actual Output: " + result2);
        System.out.println();

        // Additional quick sanity checks

        // No difficult problems allowed, threshold catches some values.
        int[] problems3 = {1, 2, 3, 4, 5};
        int threshold3 = 4;
        int k3 = 0;
        int result3 = solution.longestStudyWindow(problems3, threshold3, k3);

        System.out.println("Additional Test 1:");
        System.out.println("Problems: " + Arrays.toString(problems3));
        System.out.println("Threshold: " + threshold3);
        System.out.println("k: " + k3);
        System.out.println("Actual Output: " + result3);
        System.out.println();

        // Every problem is difficult, but k allows all of them.
        int[] problems4 = {8, 9, 10};
        int threshold4 = 1;
        int k4 = 3;
        int result4 = solution.longestStudyWindow(problems4, threshold4, k4);

        System.out.println("Additional Test 2:");
        System.out.println("Problems: " + Arrays.toString(problems4));
        System.out.println("Threshold: " + threshold4);
        System.out.println("k: " + k4);
        System.out.println("Actual Output: " + result4);
    }
}