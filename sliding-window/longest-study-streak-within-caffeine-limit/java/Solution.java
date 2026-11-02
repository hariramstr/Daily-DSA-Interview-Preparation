import java.util.*;

/*
 * Title: Longest Study Streak Within Caffeine Limit
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array cups where cups[i] represents the amount of caffeine
 * consumed during the i-th study session of a day. A student wants to find the
 * longest consecutive stretch of sessions they can keep without their total
 * caffeine intake exceeding a safety limit limit.
 *
 * Return the length of the longest contiguous subarray whose sum is less than
 * or equal to limit.
 *
 * A contiguous subarray means the sessions must be consecutive in the original
 * order. If no single session can fit within the limit, return 0.
 *
 * This problem is designed to be solved efficiently using the sliding window
 * technique. Since all values in cups are non-negative, once the current window
 * exceeds the limit, moving the left side of the window forward is enough to
 * restore validity. An O(n) solution is expected.
 *
 * Constraints:
 * - 1 <= cups.length <= 100000
 * - 0 <= cups[i] <= 10000
 * - 0 <= limit <= 1000000000
 *
 * Example 1:
 * Input: cups = [2, 1, 3, 2, 1], limit = 5
 * Output: 2
 * Explanation: Valid longest windows include [2,1], [3,2], and [2,1].
 * Any window of length 3 has sum greater than 5.
 *
 * Example 2:
 * Input: cups = [0, 1, 1, 0, 2], limit = 2
 * Output: 4
 * Explanation: The window [0,1,1,0] has total caffeine 2, so its length is 4.
 * Extending it by the last value makes the sum 4, which is too large.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray whose sum is less than
     * or equal to the given limit.
     *
     * This method uses the sliding window technique:
     * - Expand the window by moving the right pointer.
     * - Keep track of the current window sum.
     * - If the sum becomes larger than the limit, shrink the window from the left
     *   until the sum becomes valid again.
     * - Because all values are non-negative, shrinking from the left is always
     *   sufficient to restore validity.
     *
     * @param cups the array where cups[i] is the caffeine consumed in the i-th session
     * @param limit the maximum allowed total caffeine for a valid contiguous stretch
     * @return the maximum length of a contiguous subarray with sum less than or equal to limit
     *
     * Time complexity: O(n), because each element is added to the window once and removed at most once.
     * Space complexity: O(1), because only a few variables are used regardless of input size.
     */
    public int longestStudyStreak(int[] cups, int limit) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This stores the running sum of the current window [left..right].
        long currentSum = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Move the right boundary one step at a time across the array.
        for (int right = 0; right < cups.length; right++) {
            // Step 1:
            // Include the current element cups[right] into the window.
            currentSum += cups[right];

            // Step 2:
            // If the current window sum is too large, the window is invalid.
            // Since all numbers are non-negative, the only way to reduce the sum
            // is to move the left boundary to the right.
            //
            // We keep shrinking until the window sum becomes <= limit again.
            while (currentSum > limit && left <= right) {
                currentSum -= cups[left];
                left++;
            }

            // Step 3:
            // At this point, the window [left..right] is valid:
            // currentSum <= limit
            //
            // So we compute its length and update the answer if this window
            // is longer than any valid window we have seen before.
            int currentLength = right - left + 1;
            if (currentSum <= limit) {
                maxLength = Math.max(maxLength, currentLength);
            }
        }

        // If no valid single element exists, maxLength remains 0, which matches the requirement.
        return maxLength;
    }

    /**
     * Runs sample demonstrations from the problem statement and prints the results.
     *
     * @param args command-line arguments (not used)
     *
     * @return nothing
     *
     * Time complexity: O(n) overall for each demonstration call to longestStudyStreak.
     * Space complexity: O(1), excluding the input arrays created in main.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // cups = [2, 1, 3, 2, 1], limit = 5
        // Expected output: 2
        int[] cups1 = {2, 1, 3, 2, 1};
        int limit1 = 5;
        int result1 = solution.longestStudyStreak(cups1, limit1);
        System.out.println("Example 1 Output: " + result1);

        // Example 2:
        // cups = [0, 1, 1, 0, 2], limit = 2
        // Expected output: 4
        int[] cups2 = {0, 1, 1, 0, 2};
        int limit2 = 2;
        int result2 = solution.longestStudyStreak(cups2, limit2);
        System.out.println("Example 2 Output: " + result2);

        // Additional beginner-friendly checks:

        // Case where no session fits within the limit.
        // cups = [5, 6, 7], limit = 4
        // Expected output: 0
        int[] cups3 = {5, 6, 7};
        int limit3 = 4;
        int result3 = solution.longestStudyStreak(cups3, limit3);
        System.out.println("No valid session Output: " + result3);

        // Case with all zeros.
        // Entire array is valid.
        // Expected output: 5
        int[] cups4 = {0, 0, 0, 0, 0};
        int limit4 = 0;
        int result4 = solution.longestStudyStreak(cups4, limit4);
        System.out.println("All zeros Output: " + result4);
    }
}