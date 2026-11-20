/*
 * Title: Longest Playlist Window With Limited Tempo Drift
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A music app stores the tempo (in BPM) of songs in the order they were added to a playlist.
 * Product designers want to highlight the longest contiguous block of songs that feels
 * rhythmically consistent. A block is considered consistent if the difference between the
 * fastest and slowest song in that block is at most limit BPM.
 *
 * Given an integer array tempo, where tempo[i] is the BPM of the i-th song, and an integer
 * limit, return the length of the longest contiguous subarray such that:
 *
 *     max(tempo[l..r]) - min(tempo[l..r]) <= limit
 *
 * The solution must be efficient for large inputs, so checking every subarray is too slow.
 * We need a sliding window approach that can maintain the current minimum and maximum values
 * while expanding and shrinking the window.
 *
 * Constraints:
 * - 1 <= tempo.length <= 200000
 * - 1 <= tempo[i] <= 1000000000
 * - 0 <= limit <= 1000000000
 *
 * Example 1:
 * Input: tempo = [98, 101, 100, 108, 107, 103], limit = 5
 * Output: 3
 *
 * Example 2:
 * Input: tempo = [120, 124, 122, 121, 119, 130, 118], limit = 4
 * Output: 4
 *
 * Note:
 * Return only the maximum length, not the subarray itself.
 */

import java.util.*;

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray where the difference between
     * the maximum and minimum values is at most the given limit.
     *
     * This method uses a sliding window plus two monotonic deques:
     * 1. A decreasing deque to keep track of the current window's maximum value.
     * 2. An increasing deque to keep track of the current window's minimum value.
     *
     * The key idea:
     * - Expand the right side of the window one element at a time.
     * - Update both deques so they always represent valid candidates for max and min.
     * - If the window becomes invalid (max - min > limit), move the left side forward
     *   until the window becomes valid again.
     * - Track the largest valid window length seen.
     *
     * @param tempo the array of song tempos in BPM
     * @param limit the maximum allowed difference between the fastest and slowest song
     *              in a valid contiguous block
     * @return the maximum length of a contiguous subarray satisfying
     *         max(window) - min(window) <= limit
     *
     * Time complexity: O(n), because each element is added to and removed from each deque
     * at most once.
     * Space complexity: O(n) in the worst case for the deques.
     */
    public int longestConsistentPlaylist(int[] tempo, int limit) {
        // Deque for maximum values:
        // We will store indices, and the values at those indices will be in decreasing order.
        // The front of this deque always holds the index of the maximum value in the current window.
        Deque<Integer> maxDeque = new ArrayDeque<>();

        // Deque for minimum values:
        // We will store indices, and the values at those indices will be in increasing order.
        // The front of this deque always holds the index of the minimum value in the current window.
        Deque<Integer> minDeque = new ArrayDeque<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int bestLength = 0;

        // Expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < tempo.length; right++) {
            // ------------------------------------------------------------
            // STEP 1: Insert tempo[right] into the maxDeque.
            //
            // We want maxDeque to remain decreasing by value.
            // So while the last element in the deque has a value smaller than the current value,
            // it can never become the maximum for this or any future window that includes
            // the current element. Therefore, we remove it.
            // ------------------------------------------------------------
            while (!maxDeque.isEmpty() && tempo[maxDeque.peekLast()] < tempo[right]) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert tempo[right] into the minDeque.
            //
            // We want minDeque to remain increasing by value.
            // So while the last element in the deque has a value larger than the current value,
            // it can never become the minimum for this or any future window that includes
            // the current element. Therefore, we remove it.
            // ------------------------------------------------------------
            while (!minDeque.isEmpty() && tempo[minDeque.peekLast()] > tempo[right]) {
                minDeque.pollLast();
            }
            minDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window from the left while it is invalid.
            //
            // The current maximum is at tempo[maxDeque.peekFirst()]
            // The current minimum is at tempo[minDeque.peekFirst()]
            //
            // If max - min > limit, the window is invalid and must be shrunk.
            // ------------------------------------------------------------
            while ((long) tempo[maxDeque.peekFirst()] - (long) tempo[minDeque.peekFirst()] > limit) {
                // If the leftmost index is exactly the one stored at the front of maxDeque,
                // it is leaving the window, so remove it from maxDeque.
                if (maxDeque.peekFirst() == left) {
                    maxDeque.pollFirst();
                }

                // If the leftmost index is exactly the one stored at the front of minDeque,
                // it is leaving the window, so remove it from minDeque.
                if (minDeque.peekFirst() == left) {
                    minDeque.pollFirst();
                }

                // Move the left boundary to the right, shrinking the window.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: At this point, the window [left..right] is valid.
            // Update the best answer if this window is longer than any previously seen.
            // ------------------------------------------------------------
            int currentLength = right - left + 1;
            if (currentLength > bestLength) {
                bestLength = currentLength;
            }
        }

        return bestLength;
    }

    /**
     * A helper method that prints a test case and the computed result.
     *
     * @param tempo the input tempo array
     * @param limit the allowed maximum drift
     * @return the computed longest valid window length
     *
     * Time complexity: O(n), where n is the length of the input array.
     * Space complexity: O(n) in the worst case due to the deques used by the main algorithm.
     */
    public int demonstrateCase(int[] tempo, int limit) {
        int result = longestConsistentPlaylist(tempo, limit);
        System.out.println("tempo = " + Arrays.toString(tempo));
        System.out.println("limit = " + limit);
        System.out.println("Longest consistent playlist window length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     *
     * Demonstrates the algorithm on the sample inputs from the problem statement
     * and prints the results.
     *
     * Verified sample outputs:
     * - Example 1 should produce 3
     * - Example 2 should produce 4
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: Depends on the test cases executed; each call is O(n).
     * Space complexity: O(n) in the worst case for each call.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement.
        int[] tempo1 = {98, 101, 100, 108, 107, 103};
        int limit1 = 5;
        int result1 = solution.demonstrateCase(tempo1, limit1);
        System.out.println("Expected: 3");
        System.out.println("Actual:   " + result1);
        System.out.println();

        // Example 2 from the problem statement.
        // Important correction from the statement's explanation:
        // [120, 124, 122, 121, 119] has max-min = 124-119 = 5, which is invalid for limit=4.
        // The correct answer is 4.
        int[] tempo2 = {120, 124, 122, 121, 119, 130, 118};
        int limit2 = 4;
        int result2 = solution.demonstrateCase(tempo2, limit2);
        System.out.println("Expected: 4");
        System.out.println("Actual:   " + result2);
        System.out.println();

        // Additional small sanity checks for beginners.
        int[] tempo3 = {100};
        int limit3 = 0;
        int result3 = solution.demonstrateCase(tempo3, limit3);
        System.out.println("Expected: 1");
        System.out.println("Actual:   " + result3);
        System.out.println();

        int[] tempo4 = {90, 90, 90, 90};
        int limit4 = 0;
        int result4 = solution.demonstrateCase(tempo4, limit4);
        System.out.println("Expected: 4");
        System.out.println("Actual:   " + result4);
        System.out.println();

        int[] tempo5 = {1, 5, 6, 7, 8, 10, 6, 5, 6};
        int limit5 = 4;
        int result5 = solution.demonstrateCase(tempo5, limit5);
        System.out.println("Expected: 5");
        System.out.println("Actual:   " + result5);
    }
}