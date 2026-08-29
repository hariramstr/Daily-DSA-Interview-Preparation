import java.util.*;

/*
 * Title: Maximum Consecutive Shelves Within Height Budget
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A warehouse manager wants to display a consecutive block of products on one long shelf.
 * The products are already arranged in a fixed order, and product i has height heights[i].
 * To make the display look neat, the manager may choose any contiguous subarray of products,
 * but the difference between the tallest and shortest product in that chosen block must be
 * at most limit.
 *
 * Your task is to return the length of the longest contiguous block that can be selected
 * while satisfying this height budget.
 *
 * Formally, find the maximum length of a subarray heights[l..r] such that:
 * max(heights[l..r]) - min(heights[l..r]) <= limit.
 *
 * The order of products cannot be changed, and you must choose a contiguous segment.
 *
 * Constraints:
 * - 1 <= heights.length <= 100000
 * - 1 <= heights[i] <= 1000000000
 * - 0 <= limit <= 1000000000
 *
 * Example 1:
 * Input: heights = [4, 7, 6, 8, 5, 9], limit = 3
 * Output: 4
 * Explanation: One valid longest block is [7, 6, 8, 5]. Its maximum is 8 and minimum is 5,
 * so the difference is 3. No contiguous block of length 5 satisfies the condition.
 *
 * Example 2:
 * Input: heights = [10, 1, 2, 4, 7, 2], limit = 5
 * Output: 4
 * Explanation: The longest valid block is [2, 4, 7, 2]. Its maximum is 7 and minimum is 2,
 * so the difference is 5.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray such that
     * the difference between the maximum and minimum values in that subarray
     * is less than or equal to the given limit.
     *
     * This method uses a sliding window plus two monotonic deques:
     * - one deque keeps values in decreasing order so its front is the current maximum
     * - one deque keeps values in increasing order so its front is the current minimum
     *
     * @param heights the array of product heights
     * @param limit the maximum allowed difference between tallest and shortest product
     * @return the maximum valid contiguous block length
     * Time complexity: O(n), because each index is added and removed from each deque at most once
     * Space complexity: O(n), in the worst case for the deques
     */
    public int longestSubarray(int[] heights, int limit) {
        // Deque for tracking candidates for the maximum value in the current window.
        // We store indices, not values, so we can easily remove elements that fall out of the window.
        // The heights at these indices will be in decreasing order from front to back.
        Deque<Integer> maxDeque = new ArrayDeque<>();

        // Deque for tracking candidates for the minimum value in the current window.
        // Again, we store indices.
        // The heights at these indices will be in increasing order from front to back.
        Deque<Integer> minDeque = new ArrayDeque<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int best = 0;

        // Expand the window by moving the right boundary one step at a time.
        for (int right = 0; right < heights.length; right++) {
            // ------------------------------------------------------------
            // STEP 1: Add heights[right] into the max deque.
            //
            // We want maxDeque to remain decreasing by value.
            // So while the new value is greater than values at the back,
            // those smaller values can never become the maximum in any future
            // window that includes the new value, so we remove them.
            // ------------------------------------------------------------
            while (!maxDeque.isEmpty() && heights[maxDeque.peekLast()] < heights[right]) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 2: Add heights[right] into the min deque.
            //
            // We want minDeque to remain increasing by value.
            // So while the new value is smaller than values at the back,
            // those larger values can never become the minimum in any future
            // window that includes the new value, so we remove them.
            // ------------------------------------------------------------
            while (!minDeque.isEmpty() && heights[minDeque.peekLast()] > heights[right]) {
                minDeque.pollLast();
            }
            minDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 3: If the current window is invalid, shrink it from the left.
            //
            // Current maximum is at heights[maxDeque.peekFirst()]
            // Current minimum is at heights[minDeque.peekFirst()]
            //
            // If max - min > limit, the window is invalid.
            // We keep moving left forward until the window becomes valid again.
            // ------------------------------------------------------------
            while ((long) heights[maxDeque.peekFirst()] - (long) heights[minDeque.peekFirst()] > limit) {
                // If the element leaving the window is currently the maximum candidate,
                // remove it from the front of maxDeque.
                if (maxDeque.peekFirst() == left) {
                    maxDeque.pollFirst();
                }

                // If the element leaving the window is currently the minimum candidate,
                // remove it from the front of minDeque.
                if (minDeque.peekFirst() == left) {
                    minDeque.pollFirst();
                }

                // Actually shrink the window.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: At this point, the window [left..right] is valid.
            // Update the best length seen so far.
            // ------------------------------------------------------------
            int currentLength = right - left + 1;
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * Helper method to print an array in a beginner-friendly format.
     *
     * @param arr the array to convert to string form
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It also prints the expected outputs so the user can visually verify correctness.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) overall for the demonstrated test cases
     * Space complexity: O(n) due to the algorithm's deques
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] heights1 = {4, 7, 6, 8, 5, 9};
        int limit1 = 3;
        int result1 = solution.longestSubarray(heights1, limit1);

        System.out.println("Example 1:");
        System.out.println("heights = " + solution.arrayToString(heights1));
        System.out.println("limit = " + limit1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        int[] heights2 = {10, 1, 2, 4, 7, 2};
        int limit2 = 5;
        int result2 = solution.longestSubarray(heights2, limit2);

        System.out.println("Example 2:");
        System.out.println("heights = " + solution.arrayToString(heights2));
        System.out.println("limit = " + limit2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 4");
        System.out.println();

        int[] heights3 = {8};
        int limit3 = 0;
        int result3 = solution.longestSubarray(heights3, limit3);

        System.out.println("Additional Example:");
        System.out.println("heights = " + solution.arrayToString(heights3));
        System.out.println("limit = " + limit3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 1");
    }
}