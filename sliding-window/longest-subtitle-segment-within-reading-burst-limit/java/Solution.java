import java.util.*;

/*
 * Title: Longest Subtitle Segment Within Reading Burst Limit
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array `words`, where `words[i]` is the number of words shown in the `i`-th subtitle frame of a video.
 * A viewer can comfortably follow a contiguous segment only if the total number of words in that segment does not exceed
 * `maxWords`, and the segment contains at most `k` dense frames. A frame is considered dense if `words[i] >= denseThreshold`.
 *
 * Return the length of the longest contiguous segment of subtitle frames that satisfies both conditions.
 *
 * This models a real video playback system where subtitle readability depends on both the overall reading load and the
 * number of especially text-heavy frames. The answer should be the maximum number of consecutive frames the viewer can
 * watch without exceeding the reading burst limit.
 *
 * Constraints:
 * - 1 <= words.length <= 200000
 * - 0 <= words[i] <= 100000
 * - 0 <= maxWords <= 10^14
 * - 0 <= k <= words.length
 * - 1 <= denseThreshold <= 100000
 *
 * Example 1:
 * Input: words = [4, 9, 3, 8, 2, 7], maxWords = 20, k = 2, denseThreshold = 7
 * Output: 4
 * Explanation:
 * - Segment [9, 3, 8, 2] has total 22, so it is invalid.
 * - Segment [3, 8, 2, 7] has total 20 and contains two dense frames (8 and 7), so length 4 is valid.
 *
 * Example 2:
 * Input: words = [6, 1, 7, 2, 9, 1, 1], maxWords = 10, k = 1, denseThreshold = 6
 * Output: 3
 * Explanation:
 * - Segment [1, 7, 2] has total 10 and only one dense frame, so it is valid.
 * - Any longer valid segment would either exceed maxWords or contain more than one dense frame.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subtitle segment such that:
     * 1) the sum of words in the segment is at most maxWords
     * 2) the number of dense frames in the segment is at most k
     *
     * This method uses the classic sliding window / two-pointer technique.
     * Because all word counts are non-negative, once a window becomes invalid,
     * moving the left pointer to the right can only decrease the total sum and
     * possibly decrease the dense-frame count, which makes the method correct
     * and efficient.
     *
     * @param words the array where words[i] is the number of words in the i-th subtitle frame
     * @param maxWords the maximum allowed total number of words in a valid contiguous segment
     * @param k the maximum allowed number of dense frames in a valid segment
     * @param denseThreshold a frame is dense if its word count is greater than or equal to this threshold
     * @return the length of the longest contiguous valid segment
     *
     * Time complexity: O(n), where n is words.length, because each index enters and leaves the window at most once.
     * Space complexity: O(1), ignoring the input array, because only a few variables are used.
     */
    public int longestSubtitleSegment(int[] words, long maxWords, int k, int denseThreshold) {
        // Left boundary of the current sliding window.
        int left = 0;

        // Running sum of words inside the current window [left, right].
        long currentSum = 0L;

        // Number of dense frames inside the current window.
        int denseCount = 0;

        // Best valid window length found so far.
        int bestLength = 0;

        // Expand the window by moving 'right' from left to right across the array.
        for (int right = 0; right < words.length; right++) {
            // Step 1:
            // Include the new frame at index 'right' into the window.
            currentSum += words[right];

            // If this frame is dense, update the dense frame count.
            if (words[right] >= denseThreshold) {
                denseCount++;
            }

            // Step 2:
            // If the window violates either rule:
            // - total words > maxWords
            // - dense frames > k
            // then shrink the window from the left until it becomes valid again.
            while (currentSum > maxWords || denseCount > k) {
                // Before moving left forward, remove the contribution of words[left].
                currentSum -= words[left];

                // If the frame leaving the window was dense, decrease denseCount.
                if (words[left] >= denseThreshold) {
                    denseCount--;
                }

                // Move the left boundary rightward.
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is guaranteed to be valid.
            // Compute its length and update the best answer if this window is longer.
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
     * @param words the subtitle word counts per frame
     * @param maxWords the maximum total words allowed in a valid segment
     * @param k the maximum number of dense frames allowed
     * @param denseThreshold the threshold for considering a frame dense
     * @return the computed longest valid segment length
     *
     * Time complexity: O(n), where n is words.length.
     * Space complexity: O(1), excluding input storage.
     */
    public int runDemoCase(int[] words, long maxWords, int k, int denseThreshold) {
        int result = longestSubtitleSegment(words, maxWords, k, denseThreshold);

        System.out.println("words = " + Arrays.toString(words));
        System.out.println("maxWords = " + maxWords + ", k = " + k + ", denseThreshold = " + denseThreshold);
        System.out.println("Longest valid segment length = " + result);
        System.out.println();

        return result;
    }

    /**
     * Main method to demonstrate the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated test case.
     * Space complexity: O(1), excluding input arrays.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        // Expected output: 4
        int[] words1 = {4, 9, 3, 8, 2, 7};
        int result1 = solution.runDemoCase(words1, 20L, 2, 7);
        System.out.println("Expected: 4");
        System.out.println("Actual:   " + result1);
        System.out.println();

        // Sample 2
        // Expected output: 3
        int[] words2 = {6, 1, 7, 2, 9, 1, 1};
        int result2 = solution.runDemoCase(words2, 10L, 1, 6);
        System.out.println("Expected: 3");
        System.out.println("Actual:   " + result2);
        System.out.println();

        // Additional quick sanity checks for beginner-friendly demonstration.

        // Case: all zeros, maxWords = 0, no dense frames allowed, threshold high enough
        int[] words3 = {0, 0, 0, 0};
        int result3 = solution.runDemoCase(words3, 0L, 0, 1);
        System.out.println("Expected: 4");
        System.out.println("Actual:   " + result3);
        System.out.println();

        // Case: k = 0, so no dense frames can appear in the chosen segment
        int[] words4 = {1, 7, 1, 1, 8, 1};
        int result4 = solution.runDemoCase(words4, 10L, 0, 7);
        System.out.println("Expected: 3");
        System.out.println("Actual:   " + result4);
    }
}