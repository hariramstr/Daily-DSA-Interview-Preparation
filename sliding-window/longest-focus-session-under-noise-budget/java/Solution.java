import java.util.*;

/*
 * Title: Longest Focus Session Under Noise Budget
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array noise where noise[i] represents the noise level recorded during the i-th minute
 * of a student's study session. The student wants to choose one contiguous block of minutes to study
 * without taking a break. However, the total noise during that chosen block must not exceed a given integer budget.
 *
 * Your task is to return the maximum number of consecutive minutes the student can study such that the sum
 * of the noise levels in that contiguous block is less than or equal to budget.
 *
 * This is a practical scheduling problem: the student can tolerate some background noise, but only up to
 * a fixed limit. You need to find the longest valid time window.
 *
 * Return 0 if no single minute can be included without exceeding the budget.
 *
 * Constraints:
 * - 1 <= noise.length <= 100000
 * - 0 <= noise[i] <= 10000
 * - 0 <= budget <= 1000000000
 * - The solution should run efficiently for large inputs.
 *
 * Example 1:
 * Input: noise = [2, 1, 3, 2, 1], budget = 5
 * Output: 2
 * Explanation:
 * Valid windows of length 2 include [2,1] with sum 3, [1,3] with sum 4, [3,2] with sum 5, and [2,1] with sum 3.
 * Any window of length 3 has sum greater than 5, so the answer is 2.
 *
 * Example 2:
 * Input: noise = [0, 2, 0, 1, 1, 0], budget = 3
 * Output: 4
 * Explanation:
 * The window [2,0,1,1,0] has sum 4, so it is invalid.
 * The longest valid contiguous windows include [0,2,0,1] with sum 3 and length 4,
 * and [0,1,1,0] with sum 2 and length 4.
 * Therefore the answer is 4.
 *
 * Note:
 * You are only asked to compute the maximum length of a contiguous subarray whose sum is at most budget.
 * Because all noise values are non-negative, a sliding window approach works well for this problem.
 */

public class Solution {

    /**
     * Computes the maximum length of a contiguous subarray whose sum is less than or equal to the given budget.
     *
     * This method uses the classic sliding window / two-pointer technique.
     * The key reason this works is that all values in the array are non-negative:
     * - Expanding the window to the right can only increase or keep the same sum.
     * - Shrinking the window from the left can only decrease or keep the same sum.
     *
     * @param noise the array where noise[i] is the noise level during the i-th minute
     * @param budget the maximum allowed total noise for the chosen contiguous block
     * @return the maximum number of consecutive minutes whose total noise is at most budget
     *
     * Time complexity: O(n), because each index is visited at most twice
     * (once by the right pointer and once by the left pointer).
     * Space complexity: O(1), because only a few extra variables are used.
     */
    public int longestFocusSession(int[] noise, int budget) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This stores the running sum of the current window noise[left...right].
        long currentSum = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Move the right boundary one step at a time.
        for (int right = 0; right < noise.length; right++) {
            // Step 1:
            // Include the new element at index "right" into the current window.
            currentSum += noise[right];

            // Step 2:
            // If the current window sum exceeds the budget, the window is invalid.
            // Since all numbers are non-negative, the only way to make it valid again
            // is to move the left boundary to the right, removing elements from the window.
            while (currentSum > budget && left <= right) {
                currentSum -= noise[left];
                left++;
            }

            // Step 3:
            // At this point, the window noise[left...right] is guaranteed to be valid
            // (its sum is <= budget), so we can compute its length.
            int currentLength = right - left + 1;

            // Step 4:
            // Update the best answer if this valid window is longer than any previously seen one.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // If no valid single element exists, maxLength will remain 0.
        return maxLength;
    }

    /**
     * Helper method to print an integer array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n), due to the created string content.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demonstration size shown here,
     * excluding the internal calls which each run in O(n).
     * Space complexity: O(1), excluding input arrays.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] noise1 = {2, 1, 3, 2, 1};
        int budget1 = 5;
        int result1 = solution.longestFocusSession(noise1, budget1);
        System.out.println("Sample 1:");
        System.out.println("noise = " + solution.arrayToString(noise1));
        System.out.println("budget = " + budget1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 2");
        System.out.println();

        // Sample 2
        int[] noise2 = {0, 2, 0, 1, 1, 0};
        int budget2 = 3;
        int result2 = solution.longestFocusSession(noise2, budget2);
        System.out.println("Sample 2:");
        System.out.println("noise = " + solution.arrayToString(noise2));
        System.out.println("budget = " + budget2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 4");
        System.out.println();

        // Edge case: no single minute can fit
        int[] noise3 = {6, 7, 8};
        int budget3 = 5;
        int result3 = solution.longestFocusSession(noise3, budget3);
        System.out.println("Edge Case 1:");
        System.out.println("noise = " + solution.arrayToString(noise3));
        System.out.println("budget = " + budget3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 0");
        System.out.println();

        // Edge case: all zeros
        int[] noise4 = {0, 0, 0, 0};
        int budget4 = 0;
        int result4 = solution.longestFocusSession(noise4, budget4);
        System.out.println("Edge Case 2:");
        System.out.println("noise = " + solution.arrayToString(noise4));
        System.out.println("budget = " + budget4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 4");
        System.out.println();

        // Additional test: entire array fits
        int[] noise5 = {1, 1, 1, 1};
        int budget5 = 10;
        int result5 = solution.longestFocusSession(noise5, budget5);
        System.out.println("Additional Test:");
        System.out.println("noise = " + solution.arrayToString(noise5));
        System.out.println("budget = " + budget5);
        System.out.println("Output = " + result5);
        System.out.println("Expected = 4");
    }
}