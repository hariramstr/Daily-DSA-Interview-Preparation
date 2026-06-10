import java.util.*;

/*
 * Title: Longest Quiet Study Stretch
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * A university library records the noise level of each minute during the day as an integer array
 * noise, where noise[i] is the noise level at minute i. A student wants to find the longest
 * continuous time period they can study such that the total noise during that period does not
 * exceed a given limit maxNoise.
 *
 * Your task is to return the length of the longest contiguous subarray whose sum is less than or
 * equal to maxNoise.
 *
 * This is an interview-style sliding window problem because all noise values are non-negative.
 * That means when the current window becomes too noisy, you can safely move the left side of the
 * window forward until the total noise is within the allowed limit again.
 *
 * Return 0 if no single minute can be included without exceeding the limit.
 *
 * Constraints:
 * - 1 <= noise.length <= 100000
 * - 0 <= noise[i] <= 10000
 * - 0 <= maxNoise <= 1000000000
 *
 * Example 1:
 * Input: noise = [2, 1, 3, 2, 1, 1], maxNoise = 5
 * Output: 3
 * Explanation: The longest valid stretch is [2, 1, 1] or [3, 2] is length 2, but [2, 1, 1]
 * from later minutes has total noise 4 and length 3. No valid stretch of length 4 stays within
 * a total noise of 5.
 *
 * Example 2:
 * Input: noise = [6, 2, 1], maxNoise = 5
 * Output: 2
 * Explanation: Minute 0 alone is too noisy, but the subarray [2, 1] has total noise 3,
 * so the answer is 2.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray whose sum is less than or equal to
     * the given maximum allowed noise.
     *
     * This method uses the classic sliding window technique:
     * - Expand the window by moving the right pointer.
     * - Keep track of the current window sum.
     * - If the sum becomes too large, shrink the window from the left until it becomes valid again.
     * - Track the maximum valid window length seen so far.
     *
     * Because all values are non-negative, once the sum exceeds maxNoise, moving the left pointer
     * forward is always safe and necessary to reduce the sum.
     *
     * @param noise the array of non-negative noise levels for each minute
     * @param maxNoise the maximum allowed total noise for a valid study stretch
     * @return the maximum length of a contiguous subarray with sum less than or equal to maxNoise;
     *         returns 0 if no valid minute can be included
     *
     * Time complexity: O(n), where n is the length of the noise array, because each element is
     * visited at most twice: once by the right pointer and once by the left pointer.
     * Space complexity: O(1), because only a few extra variables are used.
     */
    public int longestQuietStudyStretch(int[] noise, int maxNoise) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This will store the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We use long for safety, even though int would also work under the given constraints.
        // Using long avoids any accidental overflow if constraints change in the future.
        long currentSum = 0;

        // Move the right boundary one step at a time across the array.
        for (int right = 0; right < noise.length; right++) {
            // Step 1: Include the new element at index 'right' into the current window.
            currentSum += noise[right];

            // Step 2: If the window sum is now too large, we must shrink the window
            // from the left until the sum becomes valid again.
            //
            // This works because all numbers are non-negative:
            // removing elements from the left can only decrease or keep the sum the same.
            while (currentSum > maxNoise && left <= right) {
                currentSum -= noise[left];
                left++;
            }

            // Step 3: At this point, the window [left, right] is valid
            // (its sum is <= maxNoise), so we can measure its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // If no valid window was ever found, maxLength remains 0, which is exactly what we want.
        return maxLength;
    }

    /**
     * A small helper method that prints an input array in a readable format and displays
     * the computed answer for demonstration purposes.
     *
     * @param noise the array of noise levels to test
     * @param maxNoise the maximum allowed total noise
     * @return the computed longest valid stretch length
     *
     * Time complexity: O(n), due to the call to longestQuietStudyStretch.
     * Space complexity: O(1), excluding the internal memory used by array-to-string conversion.
     */
    public int demonstrateCase(int[] noise, int maxNoise) {
        int result = longestQuietStudyStretch(noise, maxNoise);
        System.out.println("noise = " + Arrays.toString(noise) + ", maxNoise = " + maxNoise);
        System.out.println("Longest quiet study stretch length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Runs sample demonstrations of the algorithm using the examples from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) across the demonstrated inputs.
     * Space complexity: O(1), excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // noise = [2, 1, 3, 2, 1, 1], maxNoise = 5
        // Valid longest answer is 3.
        int[] noise1 = {2, 1, 3, 2, 1, 1};
        int maxNoise1 = 5;
        solution.demonstrateCase(noise1, maxNoise1);

        // Example 2:
        // noise = [6, 2, 1], maxNoise = 5
        // The first element alone is invalid, but [2, 1] is valid with length 2.
        int[] noise2 = {6, 2, 1};
        int maxNoise2 = 5;
        solution.demonstrateCase(noise2, maxNoise2);

        // Additional beginner-friendly checks:

        // Single element that fits.
        int[] noise3 = {4};
        int maxNoise3 = 4;
        solution.demonstrateCase(noise3, maxNoise3);

        // Single element that does not fit.
        int[] noise4 = {7};
        int maxNoise4 = 5;
        solution.demonstrateCase(noise4, maxNoise4);

        // Includes zeros, which are perfectly fine in a sliding window.
        int[] noise5 = {0, 0, 0, 3, 0};
        int maxNoise5 = 3;
        solution.demonstrateCase(noise5, maxNoise5);
    }
}