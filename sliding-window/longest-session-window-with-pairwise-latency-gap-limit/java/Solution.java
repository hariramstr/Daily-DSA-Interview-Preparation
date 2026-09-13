import java.util.*;

/*
 * Title: Longest Session Window With Pairwise Latency Gap Limit
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array latency where latency[i] is the measured response time
 * of the i-th request in a production session, and an integer limit.
 * A contiguous block of requests is called stable if for every pair of requests
 * inside that block, the absolute difference between their latencies is at most limit.
 * Equivalently, if max(latency[l..r]) - min(latency[l..r]) <= limit,
 * then the window [l, r] is stable.
 *
 * Your task is to return the length of the longest stable contiguous window.
 *
 * This problem must be solved efficiently for very large input sizes.
 * A brute-force approach that checks every subarray or recomputes the minimum
 * and maximum for each candidate window will be too slow.
 * The intended solution uses a sliding window together with data structures
 * that can maintain the current window minimum and maximum as the window expands
 * and shrinks.
 *
 * Constraints:
 * - 1 <= latency.length <= 200000
 * - 0 <= latency[i] <= 10^9
 * - 0 <= limit <= 10^9
 *
 * Example 1:
 * Input: latency = [8, 2, 4, 7], limit = 4
 * Output: 2
 * Explanation:
 * The longest stable windows are [2, 4] and [4, 7].
 * Any window of length 3 has max - min greater than 4.
 *
 * Example 2:
 * Input: latency = [10, 1, 2, 4, 7, 2], limit = 5
 * Output: 4
 * Explanation:
 * The window [2, 4, 7, 2] has maximum 7 and minimum 2,
 * so the difference is 5, which is allowed.
 * No longer contiguous window satisfies the condition.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous window such that
     * the difference between the maximum and minimum value inside the window
     * is at most {@code limit}.
     *
     * This method uses a classic sliding window technique together with
     * two monotonic deques:
     * - one deque keeps values in decreasing order so its front is the maximum
     * - one deque keeps values in increasing order so its front is the minimum
     *
     * As we expand the right boundary of the window, we insert the new element
     * into both deques while preserving monotonic order.
     * If the window becomes invalid (max - min > limit), we move the left boundary
     * to the right until the window becomes valid again.
     *
     * @param latency the array of request latencies
     * @param limit the maximum allowed difference between any two values in a valid window
     * @return the maximum length of any valid contiguous window
     *
     * Time complexity: O(n), because each element is added to and removed from each deque at most once.
     * Space complexity: O(n), in the worst case for the deques.
     */
    public int longestStableWindow(int[] latency, int limit) {
        // Deque for indices of elements in decreasing order of values.
        // The front always stores the index of the current maximum element in the window.
        Deque<Integer> maxDeque = new ArrayDeque<>();

        // Deque for indices of elements in increasing order of values.
        // The front always stores the index of the current minimum element in the window.
        Deque<Integer> minDeque = new ArrayDeque<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int best = 0;

        // Expand the window by moving 'right' from left to right across the array.
        for (int right = 0; right < latency.length; right++) {
            int currentValue = latency[right];

            // ------------------------------------------------------------
            // STEP 1: Insert the new element into the max deque.
            //
            // We want maxDeque to remain decreasing by value.
            // So while the last element is smaller than the current value,
            // it can never become the maximum for any future window that includes
            // the current element, so we remove it.
            // ------------------------------------------------------------
            while (!maxDeque.isEmpty() && latency[maxDeque.peekLast()] < currentValue) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert the new element into the min deque.
            //
            // We want minDeque to remain increasing by value.
            // So while the last element is larger than the current value,
            // it can never become the minimum for any future window that includes
            // the current element, so we remove it.
            // ------------------------------------------------------------
            while (!minDeque.isEmpty() && latency[minDeque.peekLast()] > currentValue) {
                minDeque.pollLast();
            }
            minDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 3: If the current window is invalid, shrink it from the left.
            //
            // The current maximum is at latency[maxDeque.peekFirst()]
            // The current minimum is at latency[minDeque.peekFirst()]
            //
            // If max - min > limit, the window is not stable.
            // We must move 'left' forward until the condition becomes valid again.
            // ------------------------------------------------------------
            while ((long) latency[maxDeque.peekFirst()] - (long) latency[minDeque.peekFirst()] > limit) {
                // If the element leaving the window is currently the maximum,
                // remove it from the front of maxDeque.
                if (maxDeque.peekFirst() == left) {
                    maxDeque.pollFirst();
                }

                // If the element leaving the window is currently the minimum,
                // remove it from the front of minDeque.
                if (minDeque.peekFirst() == left) {
                    minDeque.pollFirst();
                }

                // Actually shrink the window.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: At this point, the window [left, right] is valid.
            // Update the best answer.
            // ------------------------------------------------------------
            int windowLength = right - left + 1;
            if (windowLength > best) {
                best = windowLength;
            }
        }

        return best;
    }

    /**
     * A second public method with the same logic, named to closely match
     * common interview/platform naming conventions.
     *
     * @param latency the array of request latencies
     * @param limit the maximum allowed difference between the maximum and minimum in a window
     * @return the length of the longest stable contiguous window
     *
     * Time complexity: O(n), because each index is processed a constant number of times.
     * Space complexity: O(n), due to the deques.
     */
    public int longestSubarray(int[] latency, int limit) {
        return longestStableWindow(latency, limit);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It also prints the arrays and the computed answers so the behavior is easy to verify.
     *
     * Expected outputs:
     * - Example 1: 2
     * - Example 2: 4
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(n) total for each demonstration call.
     * Space complexity: O(n) in the worst case for each call.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] latency1 = {8, 2, 4, 7};
        int limit1 = 4;
        int result1 = solution.longestStableWindow(latency1, limit1);

        int[] latency2 = {10, 1, 2, 4, 7, 2};
        int limit2 = 5;
        int result2 = solution.longestStableWindow(latency2, limit2);

        System.out.println("Example 1:");
        System.out.println("latency = " + Arrays.toString(latency1));
        System.out.println("limit = " + limit1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 2");
        System.out.println();

        System.out.println("Example 2:");
        System.out.println("latency = " + Arrays.toString(latency2));
        System.out.println("limit = " + limit2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 4");
        System.out.println();

        // Additional quick sanity checks for beginner-friendly demonstration.
        int[] latency3 = {5};
        int limit3 = 0;
        System.out.println("Additional Test 1:");
        System.out.println("latency = " + Arrays.toString(latency3));
        System.out.println("limit = " + limit3);
        System.out.println("Output = " + solution.longestStableWindow(latency3, limit3));
        System.out.println("Expected = 1");
        System.out.println();

        int[] latency4 = {1, 1, 1, 1};
        int limit4 = 0;
        System.out.println("Additional Test 2:");
        System.out.println("latency = " + Arrays.toString(latency4));
        System.out.println("limit = " + limit4);
        System.out.println("Output = " + solution.longestStableWindow(latency4, limit4));
        System.out.println("Expected = 4");
    }
}