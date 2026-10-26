import java.util.*;

/*
 * Title: Longest Incident Window With Limited Severity Spread
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array severity where severity[i] is the severity score of the i-th incident
 * in a time-ordered monitoring stream, and an integer limit. A contiguous incident window is
 * considered stable if the difference between the maximum severity and the minimum severity
 * inside that window is at most limit.
 *
 * Your task is to return the length of the longest stable contiguous window.
 *
 * Unlike simple fixed-size window problems, the valid window size is not known in advance and
 * the maximum and minimum values must be maintained efficiently as the window expands and shrinks.
 * An O(n^2) solution that checks every subarray will not pass.
 *
 * Formally, find the maximum value of r - l + 1 such that for some indices 0 <= l <= r < n,
 * we have:
 * max(severity[l..r]) - min(severity[l..r]) <= limit
 *
 * Constraints:
 * - 1 <= severity.length <= 200000
 * - 0 <= severity[i] <= 1000000000
 * - 0 <= limit <= 1000000000
 *
 * Example 1:
 * Input: severity = [4, 7, 5, 6, 8, 3, 4], limit = 3
 * Output: 4
 *
 * Explanation:
 * The valid longest windows include [4, 7, 5, 6] and [7, 5, 6, 8].
 * Both have max - min <= 3, so the answer is 4.
 *
 * Example 2:
 * Input: severity = [10, 10, 9, 8, 7, 7, 8], limit = 2
 * Output: 5
 *
 * Explanation:
 * The longest valid window is [9, 8, 7, 7, 8].
 * Its maximum is 9 and minimum is 7, so the spread is exactly 2.
 * Therefore, the answer is 5.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray such that
     * the difference between the maximum and minimum values in that subarray
     * is at most the given limit.
     *
     * The algorithm uses a sliding window with two monotonic deques:
     * 1. A decreasing deque to keep track of the current window's maximum.
     * 2. An increasing deque to keep track of the current window's minimum.
     *
     * This allows us to:
     * - Expand the window to the right in O(1) amortized time.
     * - Shrink the window from the left whenever it becomes invalid.
     * - Always know the current max and min in O(1) time.
     *
     * @param severity the array of incident severity scores
     * @param limit the maximum allowed difference between max and min in a valid window
     * @return the length of the longest stable contiguous window
     *
     * Time complexity: O(n), where n is the length of the severity array.
     * Each index is added to and removed from each deque at most once.
     *
     * Space complexity: O(n) in the worst case for the deques.
     */
    public int longestStableWindow(int[] severity, int limit) {
        // Deque for indices of elements in decreasing order of values.
        // The front always stores the index of the maximum value in the current window.
        Deque<Integer> maxDeque = new ArrayDeque<>();

        // Deque for indices of elements in increasing order of values.
        // The front always stores the index of the minimum value in the current window.
        Deque<Integer> minDeque = new ArrayDeque<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int bestLength = 0;

        // Expand the window by moving the right boundary one step at a time.
        for (int right = 0; right < severity.length; right++) {
            // ------------------------------------------------------------
            // STEP 1: Insert severity[right] into the max deque.
            //
            // We want maxDeque to remain decreasing by value.
            // So while the last element in the deque has a value smaller than
            // the current value, it can never become the maximum for any future
            // window that includes the current element.
            // Therefore, we remove it.
            // ------------------------------------------------------------
            while (!maxDeque.isEmpty() && severity[maxDeque.peekLast()] < severity[right]) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert severity[right] into the min deque.
            //
            // We want minDeque to remain increasing by value.
            // So while the last element in the deque has a value larger than
            // the current value, it can never become the minimum for any future
            // window that includes the current element.
            // Therefore, we remove it.
            // ------------------------------------------------------------
            while (!minDeque.isEmpty() && severity[minDeque.peekLast()] > severity[right]) {
                minDeque.pollLast();
            }
            minDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 3: Check whether the current window [left..right] is valid.
            //
            // The current maximum is severity[maxDeque.peekFirst()].
            // The current minimum is severity[minDeque.peekFirst()].
            //
            // If max - min > limit, the window is invalid and must be shrunk
            // from the left until it becomes valid again.
            // ------------------------------------------------------------
            while ((long) severity[maxDeque.peekFirst()] - (long) severity[minDeque.peekFirst()] > limit) {
                // If the leftmost index is exactly the one stored at the front
                // of maxDeque, it is leaving the window, so remove it.
                if (maxDeque.peekFirst() == left) {
                    maxDeque.pollFirst();
                }

                // If the leftmost index is exactly the one stored at the front
                // of minDeque, it is leaving the window, so remove it.
                if (minDeque.peekFirst() == left) {
                    minDeque.pollFirst();
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: At this point, the window [left..right] is valid.
            // Compute its length and update the best answer if needed.
            // ------------------------------------------------------------
            int currentLength = right - left + 1;
            if (currentLength > bestLength) {
                bestLength = currentLength;
            }
        }

        return bestLength;
    }

    /**
     * A helper method that runs one demonstration test case and prints the result.
     *
     * @param severity the input severity array
     * @param limit the allowed maximum spread
     * @return the computed longest stable window length
     *
     * Time complexity: O(n), where n is the length of the severity array.
     * Space complexity: O(n) in the worst case.
     */
    public int runDemo(int[] severity, int limit) {
        int result = longestStableWindow(severity, limit);
        System.out.println("severity = " + Arrays.toString(severity));
        System.out.println("limit = " + limit);
        System.out.println("Longest stable window length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement
     * and a few additional sanity checks.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: Depends on the total size of all demo arrays.
     * Space complexity: Depends on the largest demo array processed.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the prompt.
        // Correct answer is 4.
        int[] severity1 = {4, 7, 5, 6, 8, 3, 4};
        solution.runDemo(severity1, 3);

        // Sample 2 from the prompt.
        // Correct answer is 5.
        int[] severity2 = {10, 10, 9, 8, 7, 7, 8};
        solution.runDemo(severity2, 2);

        // Additional checks.
        int[] severity3 = {1};
        solution.runDemo(severity3, 0);

        int[] severity4 = {5, 5, 5, 5};
        solution.runDemo(severity4, 0);

        int[] severity5 = {1, 3, 6, 7, 9, 10};
        solution.runDemo(severity5, 4);
    }
}