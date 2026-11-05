import java.util.*;

/*
 * Title: Longest Pantry Restock Run Within Box Capacity
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * A grocery warehouse packs a sequence of pantry items into a single moving box belt.
 * The items must stay in their original order, and the packer wants to choose one
 * contiguous run of items to place into a box. Each item has a size value in sizes[i],
 * and the total size of all chosen items must be less than or equal to capacity.
 *
 * Your task is to return the length of the longest contiguous subarray whose sum is
 * at most capacity.
 *
 * If multiple valid runs have the same maximum length, you only need to return the
 * length, not the subarray itself. If no item can fit in the box, return 0.
 *
 * This problem is designed to be solved efficiently using a sliding window because
 * all item sizes are positive. As you expand the right end of the window, the total
 * size increases. If the total becomes too large, move the left end forward until
 * the window becomes valid again.
 *
 * Constraints:
 * - 1 <= sizes.length <= 100000
 * - 1 <= sizes[i] <= 10000
 * - 1 <= capacity <= 1000000000
 * - All values in sizes are positive integers.
 *
 * Example 1:
 * Input: sizes = [2, 1, 3, 2, 1], capacity = 6
 * Output: 3
 * Explanation: The longest valid contiguous run is [1, 3, 2] or [3, 2, 1], both with total size 6.
 *
 * Example 2:
 * Input: sizes = [5, 4, 2, 1], capacity = 3
 * Output: 2
 * Explanation: Item 5 and item 4 are too large by themselves. The longest valid run is [2, 1], which has length 2.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray whose sum is less than or equal to capacity.
     *
     * This method uses the sliding window technique:
     * - Expand the window by moving the right pointer.
     * - Add the new element to the running sum.
     * - If the sum becomes too large, shrink the window from the left until the sum is valid again.
     * - Track the maximum valid window length seen so far.
     *
     * Because all numbers are positive, once the sum exceeds capacity, moving the left pointer
     * forward is the correct way to reduce the sum and restore validity.
     *
     * @param sizes the array of positive item sizes
     * @param capacity the maximum allowed sum for any chosen contiguous run
     * @return the maximum length of a contiguous subarray with sum at most capacity
     * Time complexity: O(n), where n is the length of the array, because each index is visited at most twice
     * Space complexity: O(1), because only a few extra variables are used
     */
    public int longestPantryRestockRun(int[] sizes, int capacity) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This stores the sum of all elements currently inside the window [left, right].
        long currentSum = 0L;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Move the right boundary from the beginning to the end of the array.
        for (int right = 0; right < sizes.length; right++) {
            // Step 1: Expand the window to include sizes[right].
            currentSum += sizes[right];

            // Step 2: If the window sum is too large, shrink from the left.
            // We keep removing elements from the left side until the sum becomes valid again.
            while (currentSum > capacity && left <= right) {
                currentSum -= sizes[left];
                left++;
            }

            // Step 3: At this point, the window [left, right] is valid
            // (or empty in edge cases, though with positive numbers it will be valid after shrinking).
            // Compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // Return the longest valid window length found.
        return maxLength;
    }

    /**
     * A helper method that prints an input array in a readable format and shows the computed answer.
     *
     * @param sizes the array of positive item sizes
     * @param capacity the maximum allowed sum for the contiguous run
     * @return the computed longest valid run length
     * Time complexity: O(n), because it calls the main algorithm once
     * Space complexity: O(1), excluding the space used by output formatting
     */
    public int demonstrateCase(int[] sizes, int capacity) {
        int result = longestPantryRestockRun(sizes, capacity);
        System.out.println("sizes = " + Arrays.toString(sizes) + ", capacity = " + capacity);
        System.out.println("Longest valid contiguous run length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Runs sample demonstrations for the problem.
     *
     * This main method:
     * - Creates a Solution object
     * - Tests the sample inputs from the problem statement
     * - Prints the outputs
     *
     * Verified sample results:
     * - [2, 1, 3, 2, 1], capacity = 6 -> 3
     * - [5, 4, 2, 1], capacity = 3 -> 2
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per test case
     * Space complexity: O(1), excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        // Possible valid runs with sum <= 6 include:
        // [2, 1, 3] -> sum 6, length 3
        // [1, 3, 2] -> sum 6, length 3
        // [3, 2, 1] -> sum 6, length 3
        // No valid run of length 4 exists because:
        // [2, 1, 3, 2] -> sum 8
        // [1, 3, 2, 1] -> sum 7
        // So the correct answer is 3.
        solution.demonstrateCase(new int[]{2, 1, 3, 2, 1}, 6);

        // Sample 2
        // Single items:
        // [5] -> too large
        // [4] -> too large
        // [2] -> valid
        // [1] -> valid
        // Pair [2, 1] -> sum 3, valid, length 2
        // No longer valid run exists.
        // So the correct answer is 2.
        solution.demonstrateCase(new int[]{5, 4, 2, 1}, 3);

        // Additional beginner-friendly checks
        solution.demonstrateCase(new int[]{1, 1, 1, 1}, 2);
        solution.demonstrateCase(new int[]{10}, 5);
        solution.demonstrateCase(new int[]{10}, 10);
    }
}