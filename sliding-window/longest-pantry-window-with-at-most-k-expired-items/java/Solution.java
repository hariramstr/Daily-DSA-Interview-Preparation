import java.util.*;

/*
 * Title: Longest Pantry Window With At Most K Expired Items
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * A grocery store tracks a row of pantry items in the order they were scanned during an inspection.
 * Each item is marked as either fresh or expired. You are given an array items where items[i] = 0
 * means the ith item is fresh and items[i] = 1 means it is expired. You are also given an integer k,
 * representing the maximum number of expired items the manager is willing to temporarily tolerate
 * inside one continuous inspection segment.
 *
 * Your task is to return the length of the longest contiguous segment of items that contains at most
 * k expired items.
 *
 * In other words, find the largest window of consecutive entries where the number of 1s is less than
 * or equal to k.
 *
 * This is an interview-style sliding window problem. A good solution should scan the array efficiently
 * without checking every possible subarray.
 *
 * Constraints:
 * - 1 <= items.length <= 100000
 * - items[i] is either 0 or 1
 * - 0 <= k <= items.length
 *
 * Example 1:
 * Input: items = [0,1,0,0,1,0,0], k = 1
 * Output: 4
 * Explanation:
 * The longest valid segment has length 4. For example, the window from index 2 to index 5 is
 * [0,0,1,0], which contains exactly 1 expired item.
 *
 * Example 2:
 * Input: items = [1,1,0,0,0,1,0], k = 2
 * Output: 6
 * Explanation:
 * The window from index 1 to index 6 is [1,0,0,0,1,0], which contains exactly 2 expired items
 * and has length 6.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous segment that contains at most k expired items.
     *
     * This method uses the classic sliding window technique:
     * - Expand the right side of the window one step at a time.
     * - Count how many expired items (1s) are currently inside the window.
     * - If the count becomes greater than k, shrink the window from the left
     *   until the window becomes valid again.
     * - Track the largest valid window length seen during the scan.
     *
     * @param items the array of pantry items, where 0 means fresh and 1 means expired
     * @param k the maximum number of expired items allowed in the window
     * @return the length of the longest contiguous segment containing at most k expired items
     *
     * Time complexity: O(n), where n is the length of the array, because each index
     * is visited at most twice (once by the right pointer and once by the left pointer).
     * Space complexity: O(1), because only a few variables are used.
     */
    public int longestPantryWindow(int[] items, int k) {
        // Left boundary of the current sliding window.
        int left = 0;

        // Number of expired items (value 1) currently inside the window.
        int expiredCount = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < items.length; right++) {
            // Step 1:
            // Include items[right] into the current window.
            // If it is expired (1), increase the expired count.
            if (items[right] == 1) {
                expiredCount++;
            }

            // Step 2:
            // If the window now contains too many expired items,
            // it is invalid and must be shrunk from the left side.
            //
            // We keep moving left forward until the number of expired items
            // becomes <= k again.
            while (expiredCount > k) {
                // Before removing items[left] from the window,
                // check whether it is an expired item.
                if (items[left] == 1) {
                    expiredCount--;
                }

                // Move the left boundary rightward, effectively removing
                // the old leftmost element from the window.
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is guaranteed to be valid
            // because expiredCount <= k.
            //
            // Compute its length.
            int currentLength = right - left + 1;

            // Step 4:
            // Update the best answer if this valid window is larger.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // After scanning the entire array, maxLength stores the answer.
        return maxLength;
    }

    /**
     * Helper method to print an integer array in a beginner-friendly format.
     *
     * @param items the array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the length of the array.
     * Space complexity: O(n), due to string construction.
     */
    public String arrayToString(int[] items) {
        return Arrays.toString(items);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) overall for the demonstrated test cases.
     * Space complexity: O(1) extra space excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] items1 = {0, 1, 0, 0, 1, 0, 0};
        int k1 = 1;
        int result1 = solution.longestPantryWindow(items1, k1);

        System.out.println("Sample 1");
        System.out.println("items = " + solution.arrayToString(items1));
        System.out.println("k = " + k1);
        System.out.println("Expected = 4");
        System.out.println("Actual   = " + result1);
        System.out.println();

        // Sample 2
        int[] items2 = {1, 1, 0, 0, 0, 1, 0};
        int k2 = 2;
        int result2 = solution.longestPantryWindow(items2, k2);

        System.out.println("Sample 2");
        System.out.println("items = " + solution.arrayToString(items2));
        System.out.println("k = " + k2);
        System.out.println("Expected = 6");
        System.out.println("Actual   = " + result2);
        System.out.println();

        // Additional quick checks
        int[] items3 = {0, 0, 0, 0};
        int k3 = 0;
        System.out.println("Additional Test 1");
        System.out.println("items = " + solution.arrayToString(items3));
        System.out.println("k = " + k3);
        System.out.println("Result = " + solution.longestPantryWindow(items3, k3));
        System.out.println();

        int[] items4 = {1, 1, 1, 1};
        int k4 = 2;
        System.out.println("Additional Test 2");
        System.out.println("items = " + solution.arrayToString(items4));
        System.out.println("k = " + k4);
        System.out.println("Result = " + solution.longestPantryWindow(items4, k4));
    }
}