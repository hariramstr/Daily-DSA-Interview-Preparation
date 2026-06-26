import java.util.*;

/*
 * Title: Longest Viewing Streak With Limited Ad Categories
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A video platform records the category of the ad shown before each video in a user's session.
 * You are given an array ads where ads[i] is the category ID of the ad shown at minute i,
 * and an integer k. Your task is to find the length of the longest contiguous viewing streak
 * such that the number of distinct ad categories appearing in that streak is at most k.
 *
 * A viewing streak is any contiguous subarray of ads. Distinct categories are counted by
 * category ID, so repeated appearances of the same category only count once toward the limit.
 * Return the maximum possible length of such a streak.
 *
 * This problem models a real analytics scenario where the platform wants to identify the
 * longest period of time during which ad variety stayed within an acceptable threshold.
 *
 * Constraints:
 * - 1 <= ads.length <= 200000
 * - 1 <= ads[i] <= 1000000000
 * - 1 <= k <= ads.length
 *
 * Example 1:
 * Input: ads = [4, 2, 2, 5, 5, 2, 4], k = 2
 * Output: 5
 * Explanation: The longest valid streak is [2, 2, 5, 5, 2], which contains only
 * 2 distinct categories: 2 and 5.
 *
 * Example 2:
 * Input: ads = [1, 3, 1, 3, 2, 2, 4, 2], k = 3
 * Output: 6
 * Explanation:
 * The subarray [1, 3, 1, 3, 2, 2, 4] has 4 distinct categories, so it is invalid.
 * Valid longest streaks include [1, 3, 1, 3, 2, 2] and [3, 1, 3, 2, 2, 4],
 * each of length 6.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray containing at most k distinct ad categories.
     *
     * The algorithm uses the classic sliding window technique:
     * - Expand the right boundary one element at a time.
     * - Track how many times each category appears inside the current window.
     * - If the number of distinct categories becomes greater than k, move the left boundary
     *   forward until the window becomes valid again.
     * - At every step, record the maximum valid window length seen so far.
     *
     * @param ads the array where ads[i] is the ad category shown at minute i
     * @param k the maximum number of distinct ad categories allowed in a valid viewing streak
     * @return the maximum length of a contiguous viewing streak with at most k distinct categories
     *
     * Time complexity: O(n), where n is ads.length, because each element is added to and removed
     * from the sliding window at most once.
     * Space complexity: O(k) on average for the frequency map of categories in the current window,
     * and O(min(n, number of distinct values)) in the worst case.
     */
    public int longestViewingStreak(int[] ads, int k) {
        // Frequency map:
        // key   -> ad category ID
        // value -> how many times that category currently appears inside the window [left, right]
        Map<Integer, Integer> frequency = new HashMap<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < ads.length; right++) {
            int currentCategory = ads[right];

            // Step 1:
            // Include ads[right] in the current window.
            // If the category is new to the window, its count starts at 1.
            // Otherwise, increment its existing count.
            frequency.put(currentCategory, frequency.getOrDefault(currentCategory, 0) + 1);

            // Step 2:
            // If the window now contains more than k distinct categories,
            // it is invalid and must be shrunk from the left.
            //
            // We keep moving 'left' forward until the number of distinct categories
            // becomes at most k again.
            while (frequency.size() > k) {
                int leftCategory = ads[left];

                // Decrease the count of the category that is leaving the window.
                frequency.put(leftCategory, frequency.get(leftCategory) - 1);

                // If its count becomes zero, that category no longer exists in the window,
                // so we remove it from the map entirely.
                if (frequency.get(leftCategory) == 0) {
                    frequency.remove(leftCategory);
                }

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is guaranteed to be valid
            // because it contains at most k distinct categories.
            //
            // Compute its length and update the best answer if needed.
            int currentWindowLength = right - left + 1;
            maxLength = Math.max(maxLength, currentWindowLength);
        }

        // After processing all possible right boundaries, maxLength is the answer.
        return maxLength;
    }

    /**
     * A helper method that prints an input case and the computed result.
     *
     * @param ads the ad category array to test
     * @param k the maximum number of distinct categories allowed
     * @return the computed longest valid streak length for the provided input
     *
     * Time complexity: O(n), delegated to longestViewingStreak.
     * Space complexity: O(k) on average, delegated to longestViewingStreak.
     */
    public int demonstrateCase(int[] ads, int k) {
        int result = longestViewingStreak(ads, k);
        System.out.println("ads = " + Arrays.toString(ads));
        System.out.println("k = " + k);
        System.out.println("Longest viewing streak = " + result);
        System.out.println();
        return result;
    }

    /**
     * Runs sample demonstrations for the problem.
     *
     * This main method verifies the examples from the prompt:
     * - Example 1 should produce 5
     * - Example 2 should produce 6
     *
     * @param args command-line arguments, not used
     * @return nothing
     *
     * Time complexity: O(total input size of demonstrated cases).
     * Space complexity: O(k) on average per demonstrated case.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] ads1 = {4, 2, 2, 5, 5, 2, 4};
        int k1 = 2;
        int result1 = solution.demonstrateCase(ads1, k1);
        System.out.println("Expected: 5");
        System.out.println("Matches expected: " + (result1 == 5));
        System.out.println();

        int[] ads2 = {1, 3, 1, 3, 2, 2, 4, 2};
        int k2 = 3;
        int result2 = solution.demonstrateCase(ads2, k2);
        System.out.println("Expected: 6");
        System.out.println("Matches expected: " + (result2 == 6));
        System.out.println();

        int[] ads3 = {7};
        int k3 = 1;
        solution.demonstrateCase(ads3, k3);

        int[] ads4 = {1, 2, 1, 2, 1, 2};
        int k4 = 2;
        solution.demonstrateCase(ads4, k4);

        int[] ads5 = {1, 2, 3, 4, 5};
        int k5 = 1;
        solution.demonstrateCase(ads5, k5);
    }
}