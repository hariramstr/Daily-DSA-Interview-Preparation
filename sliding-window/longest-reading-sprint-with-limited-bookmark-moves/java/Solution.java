/*
Problem Title: Longest Reading Sprint With Limited Bookmark Moves

Problem Description:
You are given an array `pages` where `pages[i]` is the number of pages in the `i`th chapter
of an online course, in the order they must be read. A student wants to complete the longest
possible contiguous reading sprint. However, switching between chapters with very different
lengths is mentally expensive.

For any contiguous sprint `pages[l..r]`, define its effort as:
    max(pages[l..r]) - min(pages[l..r])

The student can only maintain focus if this effort is at most `limit`.

Return the length of the longest contiguous subarray whose effort does not exceed `limit`.

In other words, find the maximum number of consecutive chapters such that the difference
between the largest and smallest chapter lengths in that window is at most `limit`.

A correct solution is expected to use a sliding window approach efficiently, since the input
size can be large.

Constraints:
- 1 <= pages.length <= 100000
- 1 <= pages[i] <= 1000000000
- 0 <= limit <= 1000000000

Example 1:
Input: pages = [12, 15, 14, 10, 13, 18], limit = 5
Output: 5
Explanation: The longest valid sprint is [12, 15, 14, 10, 13].
Its maximum is 15 and minimum is 10, so the effort is 5, which is allowed.
Extending to include 18 makes the effort 8, which is too large.

Example 2:
Input: pages = [7, 7, 7, 20, 21, 22], limit = 2
Output: 3
Explanation: Valid longest sprints include [7, 7, 7] and [20, 21, 22].
Each has max - min <= 2, so the answer is 3.
*/

import java.util.*;

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray such that
     * the difference between the maximum and minimum values in the window
     * does not exceed the given limit.
     *
     * This method uses a classic sliding window technique together with:
     * - a monotonic decreasing deque to track the current window maximum
     * - a monotonic increasing deque to track the current window minimum
     *
     * @param pages the array where pages[i] is the number of pages in the i-th chapter
     * @param limit the maximum allowed difference between the largest and smallest values in a valid window
     * @return the maximum length of a contiguous subarray whose max - min is at most limit
     *
     * Time complexity: O(n), because each element is added to and removed from each deque at most once.
     * Space complexity: O(n), in the worst case for the deques.
     */
    public int longestReadingSprint(int[] pages, int limit) {
        // Deque for tracking maximum values in the current window.
        // We store indices, not values, because:
        // 1) indices let us know whether an element has moved out of the window
        // 2) values can be accessed as pages[index]
        //
        // This deque is maintained in decreasing order of values:
        // pages[maxDeque[0]] >= pages[maxDeque[1]] >= ...
        Deque<Integer> maxDeque = new ArrayDeque<>();

        // Deque for tracking minimum values in the current window.
        // This deque is maintained in increasing order of values:
        // pages[minDeque[0]] <= pages[minDeque[1]] <= ...
        Deque<Integer> minDeque = new ArrayDeque<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int bestLength = 0;

        // Expand the window by moving 'right' from left to right across the array.
        for (int right = 0; right < pages.length; right++) {
            // ------------------------------------------------------------
            // STEP 1: Insert pages[right] into the max deque.
            // ------------------------------------------------------------
            // We want maxDeque to remain decreasing.
            // So while the last element in the deque is smaller than the current value,
            // it can never become the maximum for any future window that includes pages[right].
            // Therefore, we remove it.
            while (!maxDeque.isEmpty() && pages[maxDeque.peekLast()] < pages[right]) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert pages[right] into the min deque.
            // ------------------------------------------------------------
            // We want minDeque to remain increasing.
            // So while the last element in the deque is larger than the current value,
            // it can never become the minimum for any future window that includes pages[right].
            // Therefore, we remove it.
            while (!minDeque.isEmpty() && pages[minDeque.peekLast()] > pages[right]) {
                minDeque.pollLast();
            }
            minDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window from the left while it is invalid.
            // ------------------------------------------------------------
            // The current maximum is at maxDeque.peekFirst()
            // The current minimum is at minDeque.peekFirst()
            //
            // If max - min > limit, the window is invalid and we must move 'left' forward.
            while ((long) pages[maxDeque.peekFirst()] - (long) pages[minDeque.peekFirst()] > limit) {
                // If the leftmost index is exactly the front of maxDeque,
                // it is leaving the window, so remove it from the deque.
                if (maxDeque.peekFirst() == left) {
                    maxDeque.pollFirst();
                }

                // If the leftmost index is exactly the front of minDeque,
                // it is leaving the window, so remove it from the deque.
                if (minDeque.peekFirst() == left) {
                    minDeque.pollFirst();
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: Now the window [left..right] is valid.
            // ------------------------------------------------------------
            // Compute its length and update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > bestLength) {
                bestLength = currentLength;
            }
        }

        return bestLength;
    }

    /**
     * A helper method that prints an input array in a readable format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the length of the array.
     * Space complexity: O(n), due to the generated string.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * It also includes expected outputs so a beginner can visually verify correctness.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demonstration size, excluding the algorithm calls.
     * Space complexity: O(1), excluding the algorithm's internal data structures.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] pages1 = {12, 15, 14, 10, 13, 18};
        int limit1 = 5;
        int result1 = solution.longestReadingSprint(pages1, limit1);

        System.out.println("Sample 1:");
        System.out.println("pages = " + solution.arrayToString(pages1));
        System.out.println("limit = " + limit1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 5");
        System.out.println();

        // Sample 2
        int[] pages2 = {7, 7, 7, 20, 21, 22};
        int limit2 = 2;
        int result2 = solution.longestReadingSprint(pages2, limit2);

        System.out.println("Sample 2:");
        System.out.println("pages = " + solution.arrayToString(pages2));
        System.out.println("limit = " + limit2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 3");
        System.out.println();

        // Additional quick checks for beginners
        int[] pages3 = {5};
        int limit3 = 0;
        int result3 = solution.longestReadingSprint(pages3, limit3);

        System.out.println("Additional Check 1:");
        System.out.println("pages = " + solution.arrayToString(pages3));
        System.out.println("limit = " + limit3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 1");
        System.out.println();

        int[] pages4 = {1, 3, 6, 7, 9, 10};
        int limit4 = 4;
        int result4 = solution.longestReadingSprint(pages4, limit4);

        System.out.println("Additional Check 2:");
        System.out.println("pages = " + solution.arrayToString(pages4));
        System.out.println("limit = " + limit4);
        System.out.println("Output = " + result4);
        System.out.println("One valid longest window is [3, 6, 7], so expected output is 3");
    }
}