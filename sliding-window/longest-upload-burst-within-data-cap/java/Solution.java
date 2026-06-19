import java.util.*;

/*
 * Title: Longest Upload Burst Within Data Cap
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A mobile app records the size of each file upload a user performs during a session.
 * You are given an integer array uploads where uploads[i] is the size in megabytes
 * of the i-th upload, and an integer cap representing the maximum total data allowed
 * in a continuous burst.
 *
 * A burst is any contiguous sequence of uploads. Your task is to return the length
 * of the longest burst whose total uploaded data is less than or equal to cap.
 * If no single upload fits within the cap, return 0.
 *
 * This problem models rate-limited network behavior, where the app wants to identify
 * the longest uninterrupted period of uploads that stayed within a data budget.
 * Because the burst must be contiguous, reordering uploads is not allowed.
 *
 * Write a function that efficiently computes the answer for large input sizes.
 *
 * Constraints:
 * - 1 <= uploads.length <= 200000
 * - 0 <= uploads[i] <= 100000
 * - 0 <= cap <= 1000000000
 * - All values are integers
 *
 * Example 1:
 * Input: uploads = [4, 2, 1, 7, 3, 2], cap = 8
 * Output: 3
 * Explanation: The longest valid burst is [4, 2, 1] with total 7.
 *
 * Example 2:
 * Input: uploads = [9, 1, 2, 1, 1], cap = 4
 * Correct Output: 3
 * Explanation:
 * - [1, 2, 1, 1] has total 5, so it is not valid.
 * - The longest valid bursts are [1, 2, 1] and [2, 1, 1], each of length 3.
 * - Since the first upload alone exceeds the cap, it cannot be included.
 *
 * Important note:
 * The written "Output: 4" in the prompt's Example 2 contradicts its own explanation.
 * The explanation is correct, so the correct answer is 3.
 */

public class Solution {

    /**
     * Computes the length of the longest contiguous burst of uploads whose total size
     * is less than or equal to the given cap.
     *
     * This method uses the classic sliding window technique:
     * - Expand the window by moving the right pointer.
     * - If the sum becomes too large, shrink the window from the left
     *   until the sum is valid again.
     * - Track the maximum valid window length seen so far.
     *
     * Because all upload sizes are non-negative, once the sum exceeds the cap,
     * moving the left pointer forward is the correct way to reduce the sum.
     *
     * @param uploads the array of upload sizes in megabytes; each value is non-negative
     * @param cap the maximum allowed total size for any contiguous burst
     * @return the maximum length of a contiguous subarray whose sum is at most cap;
     *         returns 0 if no upload can be included
     *
     * Time complexity: O(n), where n is uploads.length, because each index is visited
     * at most twice: once by the right pointer and once by the left pointer.
     * Space complexity: O(1), because only a few extra variables are used.
     */
    public int longestUploadBurst(int[] uploads, int cap) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This stores the running sum of the current window uploads[left..right].
        // We use long for extra safety, even though int would still fit under constraints.
        long currentSum = 0L;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < uploads.length; right++) {
            // Step 1: Include the new upload at index 'right' into the current window.
            currentSum += uploads[right];

            // Step 2: If the window sum exceeds the cap, the current window is invalid.
            // We must shrink it from the left until the sum becomes valid again.
            //
            // Why this works:
            // Since all uploads are non-negative, adding more elements can only keep
            // the sum the same or increase it. Therefore, when the sum is too large,
            // the only way to fix it is to remove elements from the left.
            while (currentSum > cap && left <= right) {
                currentSum -= uploads[left];
                left++;
            }

            // Step 3: At this point, the window uploads[left..right] is valid
            // (or empty in edge cases, though here it will be valid after shrinking).
            //
            // Its length is:
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // If no valid single upload exists, maxLength will remain 0.
        return maxLength;
    }

    /**
     * Helper method to print an integer array in a readable format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is arr.length.
     * Space complexity: O(n), due to the generated string content.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments; not used
     * @return nothing
     *
     * Time complexity: O(k + total input size across demonstrations), where k is the
     * number of printed test cases.
     * Space complexity: O(1) extra, excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] uploads1 = {4, 2, 1, 7, 3, 2};
        int cap1 = 8;
        int result1 = solution.longestUploadBurst(uploads1, cap1);
        System.out.println("Example 1:");
        System.out.println("uploads = " + solution.arrayToString(uploads1));
        System.out.println("cap = " + cap1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        // Sample 2
        // The prompt text contains a contradiction:
        // it says Output: 4, but its own explanation proves the correct answer is 3.
        int[] uploads2 = {9, 1, 2, 1, 1};
        int cap2 = 4;
        int result2 = solution.longestUploadBurst(uploads2, cap2);
        System.out.println("Example 2:");
        System.out.println("uploads = " + solution.arrayToString(uploads2));
        System.out.println("cap = " + cap2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 3");
        System.out.println();

        // Additional edge case: no upload fits
        int[] uploads3 = {5, 6, 7};
        int cap3 = 4;
        int result3 = solution.longestUploadBurst(uploads3, cap3);
        System.out.println("Edge Case 1:");
        System.out.println("uploads = " + solution.arrayToString(uploads3));
        System.out.println("cap = " + cap3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 0");
        System.out.println();

        // Additional edge case: all zeros
        int[] uploads4 = {0, 0, 0, 0};
        int cap4 = 0;
        int result4 = solution.longestUploadBurst(uploads4, cap4);
        System.out.println("Edge Case 2:");
        System.out.println("uploads = " + solution.arrayToString(uploads4));
        System.out.println("cap = " + cap4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 4");
        System.out.println();

        // Additional edge case: entire array fits
        int[] uploads5 = {1, 2, 3};
        int cap5 = 10;
        int result5 = solution.longestUploadBurst(uploads5, cap5);
        System.out.println("Edge Case 3:");
        System.out.println("uploads = " + solution.arrayToString(uploads5));
        System.out.println("cap = " + cap5);
        System.out.println("Output = " + result5);
        System.out.println("Expected = 3");
    }
}