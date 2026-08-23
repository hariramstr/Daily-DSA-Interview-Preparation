import java.util.*;

/*
 * Title: Longest Snack Stall Run With Freshness Range
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array freshness where freshness[i] is the freshness score of the i-th snack stall
 * along a street. A tourist wants to visit a contiguous run of stalls, but only if the stalls in that
 * run are reasonably consistent in quality. A run is considered valid if the difference between the
 * maximum freshness score and the minimum freshness score inside the run is at most limit.
 *
 * Return the length of the longest valid contiguous run of stalls.
 *
 * In other words, find the maximum size of a subarray freshness[l..r] such that
 * max(freshness[l..r]) - min(freshness[l..r]) <= limit.
 *
 * This problem is intended to be solved efficiently for large inputs. A brute-force solution that checks
 * every subarray will be too slow. Think about how to maintain the current window's minimum and maximum
 * values while expanding and shrinking a sliding window.
 *
 * Constraints:
 * - 1 <= freshness.length <= 200000
 * - 0 <= freshness[i] <= 1000000000
 * - 0 <= limit <= 1000000000
 *
 * Example 1:
 * Input: freshness = [4, 7, 6, 8, 5, 9], limit = 3
 * Output: 4
 * Explanation: One longest valid run is [7, 6, 8, 5]. Its maximum is 8 and minimum is 5,
 * so the difference is 3, which is allowed. No valid contiguous run has length greater than 4.
 *
 * Example 2:
 * Input: freshness = [10, 1, 2, 4, 7, 2], limit = 5
 * Output: 4
 * Explanation: The longest valid run is [2, 4, 7, 2]. Its maximum is 7 and minimum is 2,
 * so the difference is 5. Any longer window would exceed the allowed freshness range.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray such that the difference
     * between the maximum and minimum values inside the subarray is at most {@code limit}.
     *
     * The algorithm uses a sliding window plus two monotonic deques:
     * 1. A decreasing deque to track the current window maximum.
     * 2. An increasing deque to track the current window minimum.
     *
     * As we expand the right boundary, we insert the new value into both deques while
     * preserving their monotonic order. If the window becomes invalid
     * (currentMax - currentMin > limit), we move the left boundary forward until the
     * window becomes valid again.
     *
     * @param freshness the array of freshness scores for snack stalls
     * @param limit the maximum allowed difference between the maximum and minimum freshness
     *              values in a valid contiguous run
     * @return the length of the longest valid contiguous run
     *
     * Time complexity: O(n), where n is the length of the array, because each element is
     * added to and removed from each deque at most once.
     * Space complexity: O(n) in the worst case for the deques.
     */
    public int longestSubarray(int[] freshness, int limit) {
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

        // Expand the window by moving 'right' from left to right across the array.
        for (int right = 0; right < freshness.length; right++) {
            int currentValue = freshness[right];

            // ------------------------------------------------------------
            // STEP 1: Insert current element into maxDeque.
            // ------------------------------------------------------------
            // We want maxDeque to remain decreasing by value.
            // That means:
            // - If the new value is greater than elements at the back,
            //   those smaller elements can never become the maximum for
            //   this window or any future window that includes the new value.
            // - So we remove them from the back.
            while (!maxDeque.isEmpty() && freshness[maxDeque.peekLast()] < currentValue) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert current element into minDeque.
            // ------------------------------------------------------------
            // We want minDeque to remain increasing by value.
            // That means:
            // - If the new value is smaller than elements at the back,
            //   those larger elements can never become the minimum for
            //   this window or any future window that includes the new value.
            // - So we remove them from the back.
            while (!minDeque.isEmpty() && freshness[minDeque.peekLast()] > currentValue) {
                minDeque.pollLast();
            }
            minDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window while it is invalid.
            // ------------------------------------------------------------
            // The current maximum is at maxDeque.peekFirst().
            // The current minimum is at minDeque.peekFirst().
            //
            // If max - min > limit, the window is invalid and we must move
            // the left boundary to the right until it becomes valid again.
            while ((long) freshness[maxDeque.peekFirst()] - (long) freshness[minDeque.peekFirst()] > limit) {
                // If the outgoing left index is currently the maximum,
                // remove it from the front of maxDeque.
                if (maxDeque.peekFirst() == left) {
                    maxDeque.pollFirst();
                }

                // If the outgoing left index is currently the minimum,
                // remove it from the front of minDeque.
                if (minDeque.peekFirst() == left) {
                    minDeque.pollFirst();
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: Update the best valid window length.
            // ------------------------------------------------------------
            // At this point, the window [left..right] is guaranteed valid.
            int currentLength = right - left + 1;
            if (currentLength > bestLength) {
                bestLength = currentLength;
            }
        }

        return bestLength;
    }

    /**
     * Helper method to print an example run in a beginner-friendly format.
     *
     * @param freshness the input freshness array
     * @param limit the allowed maximum difference between max and min in a valid subarray
     * @return the computed longest valid contiguous run length
     *
     * Time complexity: O(n), delegated to {@link #longestSubarray(int[], int)}.
     * Space complexity: O(n), delegated to {@link #longestSubarray(int[], int)}.
     */
    public int demonstrate(int[] freshness, int limit) {
        int result = longestSubarray(freshness, limit);
        System.out.println("freshness = " + Arrays.toString(freshness));
        System.out.println("limit = " + limit);
        System.out.println("Longest valid contiguous run length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call.
     * Space complexity: O(n) per demonstration call.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1:
        // freshness = [4, 7, 6, 8, 5, 9], limit = 3
        // Expected output: 4
        int[] freshness1 = {4, 7, 6, 8, 5, 9};
        int limit1 = 3;
        int result1 = solution.demonstrate(freshness1, limit1);
        System.out.println("Expected: 4, Actual: " + result1);
        System.out.println();

        // Sample 2:
        // freshness = [10, 1, 2, 4, 7, 2], limit = 5
        // Expected output: 4
        int[] freshness2 = {10, 1, 2, 4, 7, 2};
        int limit2 = 5;
        int result2 = solution.demonstrate(freshness2, limit2);
        System.out.println("Expected: 4, Actual: " + result2);
        System.out.println();

        // Additional quick sanity checks.
        int[] freshness3 = {5};
        int limit3 = 0;
        int result3 = solution.demonstrate(freshness3, limit3);
        System.out.println("Expected: 1, Actual: " + result3);
        System.out.println();

        int[] freshness4 = {1, 1, 1, 1};
        int limit4 = 0;
        int result4 = solution.demonstrate(freshness4, limit4);
        System.out.println("Expected: 4, Actual: " + result4);
    }
}