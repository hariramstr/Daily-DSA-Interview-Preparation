import java.util.*;

/*
 * Title: Longest Podcast Queue Within Download Cap
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are building an offline listening feature for a podcast app. The app shows episodes in a fixed order,
 * and a user wants to download a contiguous block of episodes to listen to later. Each episode has a
 * download size in megabytes. Because the user has limited storage, the total size of the chosen block
 * cannot exceed a given capacity cap.
 *
 * Given an integer array sizes where sizes[i] is the size of the i-th episode, return the length of the
 * longest contiguous subarray whose total sum is less than or equal to cap. If multiple valid subarrays
 * have the same maximum length, returning the length alone is sufficient.
 *
 * This problem models a common streaming or caching constraint and is intended to be solved efficiently.
 * A brute-force solution that checks every possible subarray will be too slow for large inputs. Think about
 * how to expand and shrink a window while maintaining the current total size.
 *
 * Constraints:
 * - 1 <= sizes.length <= 200000
 * - 1 <= sizes[i] <= 100000
 * - 1 <= cap <= 10^15
 * - The answer is always at least 1 if some single episode fits within cap; otherwise return 0.
 *
 * Example 1:
 * Input: sizes = [40, 20, 30, 50, 10, 10], cap = 70
 * Output: 3
 * Explanation: The longest valid contiguous block is [50, 10, 10], whose total size is 70.
 *
 * Example 2:
 * Input: sizes = [90, 30, 20, 40], cap = 60
 * Output: 2
 * Explanation: The best block is [20, 40] with total size 60. Episode 90 alone does not fit,
 * so it cannot be part of any valid block.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray whose sum is less than or equal to cap.
     *
     * This method uses the classic sliding window / two-pointer technique.
     * Because all episode sizes are positive, once the current window sum becomes too large,
     * moving the left pointer to the right is guaranteed to reduce the sum.
     *
     * @param sizes the array of episode download sizes
     * @param cap the maximum allowed total download size for a contiguous block
     * @return the maximum length of a contiguous subarray with sum <= cap; returns 0 if no episode fits
     *
     * Time complexity: O(n), where n is the length of sizes, because each index is visited at most twice
     * (once by the right pointer and once by the left pointer).
     * Space complexity: O(1), because only a few extra variables are used.
     */
    public int longestPodcastQueue(int[] sizes, long cap) {
        // Left boundary of the current sliding window.
        int left = 0;

        // Running sum of the current window.
        // We use long because:
        // - sizes.length can be as large as 200000
        // - sizes[i] can be as large as 100000
        // So the sum can exceed the range of int.
        long currentSum = 0L;

        // Best answer found so far.
        int maxLength = 0;

        // Expand the window one episode at a time using the right pointer.
        for (int right = 0; right < sizes.length; right++) {
            // Step 1:
            // Include the new episode at index 'right' into the current window.
            currentSum += sizes[right];

            // Step 2:
            // If the window sum is now too large, we must shrink the window from the left
            // until the sum becomes valid again (<= cap).
            //
            // This works because all values are positive:
            // removing elements from the left always decreases the sum.
            while (left <= right && currentSum > cap) {
                currentSum -= sizes[left];
                left++;
            }

            // Step 3:
            // After shrinking, if the current window is valid, compute its length.
            //
            // The window is from index 'left' to index 'right', inclusive.
            // So its length is: right - left + 1
            //
            // If no valid window exists ending at 'right', then left may have moved past right,
            // and the computed length would be 0. That is fine and harmless.
            int currentLength = right - left + 1;

            // Step 4:
            // Update the best answer if this valid window is longer.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // Return the longest valid contiguous block length found.
        return maxLength;
    }

    /**
     * A small helper method that prints an input array in a readable format and shows the computed answer.
     *
     * @param sizes the array of episode sizes to test
     * @param cap the maximum allowed total size
     * @return the computed longest valid contiguous subarray length
     *
     * Time complexity: O(n), due to the call to longestPodcastQueue and array-to-string conversion.
     * Space complexity: O(1) auxiliary space for the algorithm itself.
     */
    public int demonstrateCase(int[] sizes, long cap) {
        int result = longestPodcastQueue(sizes, cap);
        System.out.println("sizes = " + Arrays.toString(sizes));
        System.out.println("cap = " + cap);
        System.out.println("Longest valid contiguous block length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution on the sample inputs and a few extra cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated test case.
     * Space complexity: O(1) auxiliary space for the algorithm itself.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Example 1
        // sizes = [40, 20, 30, 50, 10, 10], cap = 70
        // Expected output: 3
        //
        // Quick correctness trace:
        // - [40,20] = 60 valid length 2
        // - [20,30] = 50 valid length 2
        // - [50,10,10] = 70 valid length 3
        // No longer valid block exists, so answer is 3.
        int[] sizes1 = {40, 20, 30, 50, 10, 10};
        long cap1 = 70L;
        System.out.println("Example 1:");
        solution.demonstrateCase(sizes1, cap1);

        // Sample Example 2
        // sizes = [90, 30, 20, 40], cap = 60
        // Expected output: 2
        //
        // Quick correctness trace:
        // - [90] invalid
        // - [30,20] = 50 valid length 2
        // - [20,40] = 60 valid length 2
        // No valid length 3 block exists, so answer is 2.
        int[] sizes2 = {90, 30, 20, 40};
        long cap2 = 60L;
        System.out.println("Example 2:");
        solution.demonstrateCase(sizes2, cap2);

        // Extra test case: no single episode fits
        // Expected output: 0
        int[] sizes3 = {100, 200, 300};
        long cap3 = 50L;
        System.out.println("Extra Example 3:");
        solution.demonstrateCase(sizes3, cap3);

        // Extra test case: entire array fits
        // Expected output: 5
        int[] sizes4 = {5, 10, 15, 20, 25};
        long cap4 = 100L;
        System.out.println("Extra Example 4:");
        solution.demonstrateCase(sizes4, cap4);
    }
}