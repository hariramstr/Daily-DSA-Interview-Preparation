/*
 * Title: Find the Longest Equal Neighbor Run
 * Difficulty: Easy
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array nums representing a sequence of sensor states recorded over time.
 * A stable run is a contiguous part of the array where every pair of neighboring elements is equal.
 * In other words, for every valid index i inside that run except the last one, nums[i] == nums[i + 1].
 *
 * Your task is to return the length of the longest stable run in the array.
 *
 * A run may contain just one element, so if all values are different, the answer is 1.
 * If the array is empty, return 0.
 *
 * This problem is useful in systems that need to measure how long a device stayed in the same state
 * without changing. You only need to scan the array once and track the current run length and the
 * best run length seen so far.
 *
 * Constraints:
 * - 0 <= nums.length <= 100000
 * - -1000000000 <= nums[i] <= 1000000000
 *
 * Example 1:
 * Input: nums = [4,4,4,2,2,7,7,7,7,1]
 * Output: 4
 * Explanation: The longest contiguous block of equal values is [7,7,7,7], which has length 4.
 *
 * Example 2:
 * Input: nums = [5,3,8,1]
 * Output: 1
 * Explanation: No two neighboring elements are equal, so every stable run has length 1.
 *
 * Return the maximum length among all contiguous runs of equal values.
 */

import java.util.*;

public class Solution {

    /**
     * Finds the length of the longest contiguous run of equal neighboring values.
     *
     * @param nums the input integer array representing sensor states over time
     * @return the maximum length among all contiguous runs of equal values;
     *         returns 0 if the array is empty
     *
     * Time complexity: O(n), where n is the length of the array, because we scan the array once.
     * Space complexity: O(1), because we use only a few extra variables.
     */
    public int longestEqualNeighborRun(int[] nums) {
        // Step 1:
        // Handle the special case where the array is empty.
        // If there are no elements, there cannot be any run at all.
        // According to the problem statement, the correct answer is 0.
        if (nums == null || nums.length == 0) {
            return 0;
        }

        // Step 2:
        // Initialize two variables:
        //
        // currentRun:
        //   This stores the length of the run we are currently building while scanning the array.
        //   Since a single element by itself counts as a run of length 1,
        //   we start with currentRun = 1.
        //
        // bestRun:
        //   This stores the longest run length we have seen so far.
        //   At the beginning, the first element alone forms a run of length 1,
        //   so bestRun also starts at 1.
        int currentRun = 1;
        int bestRun = 1;

        // Step 3:
        // Start scanning from index 1, because we compare each element with the previous one.
        // For each position i:
        //   - If nums[i] == nums[i - 1], then the current run continues,
        //     so we increase currentRun by 1.
        //   - Otherwise, the run is broken, so we reset currentRun to 1,
        //     because the current element starts a new run by itself.
        //
        // After updating currentRun, we compare it with bestRun.
        // If currentRun is larger, we update bestRun.
        for (int i = 1; i < nums.length; i++) {
            // Check whether the current element matches the previous element.
            if (nums[i] == nums[i - 1]) {
                // The run continues, so extend the current run length.
                currentRun++;
            } else {
                // The run breaks here.
                // Start a new run beginning at nums[i].
                currentRun = 1;
            }

            // Update the best answer seen so far.
            // If the current run is longer than any previous run, store it.
            if (currentRun > bestRun) {
                bestRun = currentRun;
            }
        }

        // Step 4:
        // After scanning the whole array, bestRun contains the length
        // of the longest contiguous block of equal values.
        return bestRun;
    }

    /**
     * Utility method to convert an integer array into a readable string form.
     * This is used only for demonstration in main.
     *
     * @param nums the input integer array
     * @return a string representation of the array, such as [1, 2, 3]
     *
     * Time complexity: O(n), where n is the length of the array.
     * Space complexity: O(n), due to the string construction.
     */
    public String arrayToString(int[] nums) {
        return Arrays.toString(nums);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(k * n) overall for demonstration, where k is the number of test arrays
     * and n is the average array length.
     * Space complexity: O(1) extra space excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1 from the problem statement.
        int[] nums1 = {4, 4, 4, 2, 2, 7, 7, 7, 7, 1};
        int result1 = solution.longestEqualNeighborRun(nums1);
        System.out.println("Input:  " + solution.arrayToString(nums1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 4");
        System.out.println();

        // Sample input 2 from the problem statement.
        int[] nums2 = {5, 3, 8, 1};
        int result2 = solution.longestEqualNeighborRun(nums2);
        System.out.println("Input:  " + solution.arrayToString(nums2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 1");
        System.out.println();

        // Additional edge case: empty array.
        int[] nums3 = {};
        int result3 = solution.longestEqualNeighborRun(nums3);
        System.out.println("Input:  " + solution.arrayToString(nums3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        // Additional edge case: all elements equal.
        int[] nums4 = {9, 9, 9, 9, 9};
        int result4 = solution.longestEqualNeighborRun(nums4);
        System.out.println("Input:  " + solution.arrayToString(nums4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 5");
        System.out.println();

        // Additional edge case: single element.
        int[] nums5 = {42};
        int result5 = solution.longestEqualNeighborRun(nums5);
        System.out.println("Input:  " + solution.arrayToString(nums5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 1");
    }
}