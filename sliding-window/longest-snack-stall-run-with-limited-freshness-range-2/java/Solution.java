import java.util.*;

/*
Problem Title: Longest Snack Stall Run with Limited Freshness Range

Problem Description:
You are given an array freshness where freshness[i] represents the freshness score of the i-th snack stall along a street.
A tourist wants to visit a contiguous run of stalls, but only if the stalls in that run are reasonably consistent in quality.
A run is considered valid if the difference between the maximum freshness score and the minimum freshness score inside the run is at most limit.

Return the length of the longest valid contiguous run of stalls.

In other words, find the maximum window size such that for some subarray freshness[l..r],
max(freshness[l..r]) - min(freshness[l..r]) <= limit.

This problem should be solved efficiently for large inputs. A brute-force solution that checks every subarray
will be too slow. Think about how to maintain the current window's minimum and maximum as you expand and shrink
a sliding window.

Constraints:
- 1 <= freshness.length <= 200000
- 0 <= freshness[i] <= 1000000000
- 0 <= limit <= 1000000000

Example 1:
Input: freshness = [8, 6, 7, 9, 10, 5], limit = 3
Output: 4
Explanation: A longest valid run is [8, 6, 7, 9], where max - min = 9 - 6 = 3.

Example 2:
Input: freshness = [4, 4, 4, 2, 3, 5, 6], limit = 1
Output: 3
Explanation: One longest valid run is [4, 4, 4]. Its maximum and minimum are both 4, so the difference is 0.
*/

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray such that
     * the difference between the maximum and minimum values in that subarray
     * is at most the given limit.
     *
     * This method uses a sliding window together with two monotonic deques:
     * 1. A decreasing deque to track the maximum value in the current window.
     * 2. An increasing deque to track the minimum value in the current window.
     *
     * The key idea:
     * - Expand the window by moving the right pointer.
     * - Keep both deques updated so their front always represents the current
     *   window's maximum and minimum.
     * - If the window becomes invalid (max - min > limit), shrink it from the left
     *   until it becomes valid again.
     * - Track the largest valid window length seen so far.
     *
     * @param freshness the array of freshness scores for snack stalls
     * @param limit the maximum allowed difference between the maximum and minimum freshness in a valid run
     * @return the length of the longest valid contiguous run
     *
     * Time complexity: O(n), where n is the length of the freshness array,
     * because each element is added to and removed from each deque at most once.
     *
     * Space complexity: O(n) in the worst case for the deques.
     */
    public int longestSnackStallRun(int[] freshness, int limit) {
        // Defensive handling, although the problem guarantees at least one element.
        if (freshness == null || freshness.length == 0) {
            return 0;
        }

        // maxDeque will store indices of elements in decreasing order of values.
        // This means:
        // - The front of the deque always holds the index of the maximum value
        //   in the current window.
        Deque<Integer> maxDeque = new ArrayDeque<>();

        // minDeque will store indices of elements in increasing order of values.
        // This means:
        // - The front of the deque always holds the index of the minimum value
        //   in the current window.
        Deque<Integer> minDeque = new ArrayDeque<>();

        // left is the start of the sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // Move right from left to right across the array, expanding the window one element at a time.
        for (int right = 0; right < freshness.length; right++) {
            // ------------------------------------------------------------
            // STEP 1: Insert freshness[right] into maxDeque
            // ------------------------------------------------------------
            // We want maxDeque to remain decreasing by value.
            // So while the last element in maxDeque has a value smaller than the current value,
            // it can never become the maximum for this or any future window that includes current value.
            // Therefore, we remove it.
            while (!maxDeque.isEmpty() && freshness[maxDeque.peekLast()] < freshness[right]) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert freshness[right] into minDeque
            // ------------------------------------------------------------
            // We want minDeque to remain increasing by value.
            // So while the last element in minDeque has a value larger than the current value,
            // it can never become the minimum for this or any future window that includes current value.
            // Therefore, we remove it.
            while (!minDeque.isEmpty() && freshness[minDeque.peekLast()] > freshness[right]) {
                minDeque.pollLast();
            }
            minDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window while it is invalid
            // ------------------------------------------------------------
            // The current window is [left, right].
            // It is invalid if:
            // currentMax - currentMin > limit
            //
            // currentMax is freshness[maxDeque.peekFirst()]
            // currentMin is freshness[minDeque.peekFirst()]
            //
            // We keep moving left forward until the window becomes valid again.
            while ((long) freshness[maxDeque.peekFirst()] - (long) freshness[minDeque.peekFirst()] > limit) {
                // If the index at the front of maxDeque is leaving the window,
                // remove it from the deque.
                if (maxDeque.peekFirst() == left) {
                    maxDeque.pollFirst();
                }

                // If the index at the front of minDeque is leaving the window,
                // remove it from the deque.
                if (minDeque.peekFirst() == left) {
                    minDeque.pollFirst();
                }

                // Actually shrink the window from the left.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: Update the best answer
            // ------------------------------------------------------------
            // At this point, the window [left, right] is guaranteed to be valid.
            // So we compute its length and compare it with the best seen so far.
            int currentLength = right - left + 1;
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * A convenience wrapper method with a shorter name.
     * This simply delegates to longestSnackStallRun.
     *
     * @param freshness the array of freshness scores
     * @param limit the maximum allowed difference between max and min in a valid window
     * @return the length of the longest valid contiguous run
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int longestSubarray(int[] freshness, int limit) {
        return longestSnackStallRun(freshness, limit);
    }

    /**
     * Converts an integer array to a readable string representation.
     * This helper is used only for demonstration output in main.
     *
     * @param array the input integer array
     * @return a string like [1, 2, 3]
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and a few additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total number of elements across demonstrated test cases)
     * Space complexity: O(n) for the largest demonstrated test case due to deques
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] freshness1 = {8, 6, 7, 9, 10, 5};
        int limit1 = 3;
        int result1 = solution.longestSnackStallRun(freshness1, limit1);
        System.out.println("Sample 1:");
        System.out.println("freshness = " + solution.arrayToString(freshness1));
        System.out.println("limit = " + limit1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        // Sample 2
        int[] freshness2 = {4, 4, 4, 2, 3, 5, 6};
        int limit2 = 1;
        int result2 = solution.longestSnackStallRun(freshness2, limit2);
        System.out.println("Sample 2:");
        System.out.println("freshness = " + solution.arrayToString(freshness2));
        System.out.println("limit = " + limit2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 3");
        System.out.println();

        // Additional test 1: all equal values
        int[] freshness3 = {5, 5, 5, 5};
        int limit3 = 0;
        int result3 = solution.longestSnackStallRun(freshness3, limit3);
        System.out.println("Additional Test 1:");
        System.out.println("freshness = " + solution.arrayToString(freshness3));
        System.out.println("limit = " + limit3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 4");
        System.out.println();

        // Additional test 2: strictly increasing with small limit
        int[] freshness4 = {1, 2, 3, 4, 5};
        int limit4 = 2;
        int result4 = solution.longestSnackStallRun(freshness4, limit4);
        System.out.println("Additional Test 2:");
        System.out.println("freshness = " + solution.arrayToString(freshness4));
        System.out.println("limit = " + limit4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 3");
        System.out.println();

        // Additional test 3: single element
        int[] freshness5 = {42};
        int limit5 = 10;
        int result5 = solution.longestSnackStallRun(freshness5, limit5);
        System.out.println("Additional Test 3:");
        System.out.println("freshness = " + solution.arrayToString(freshness5));
        System.out.println("limit = " + limit5);
        System.out.println("Output = " + result5);
        System.out.println("Expected = 1");
    }
}