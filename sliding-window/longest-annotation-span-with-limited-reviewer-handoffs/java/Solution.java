import java.util.*;

/*
 * Title: Longest Annotation Span With Limited Reviewer Handoffs
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A document review platform records which reviewer handled each consecutive annotation
 * in a long editing session. You are given an array reviewers where reviewers[i] is the
 * reviewer ID responsible for the i-th annotation, in chronological order. A handoff
 * happens between two adjacent annotations when their reviewer IDs are different.
 *
 * Your task is to find the length of the longest contiguous span of annotations that
 * contains at most k handoffs. In other words, within the chosen subarray, count how
 * many indices j satisfy reviewers[j] != reviewers[j - 1] for adjacent elements inside
 * that subarray. That count must be less than or equal to k.
 *
 * Return the maximum possible length of such a contiguous span.
 *
 * This problem models finding the longest stable review segment where ownership changes
 * are limited. A span with repeated reviewer IDs may still contain handoffs if the
 * reviewer changes and later changes back.
 *
 * Constraints:
 * - 1 <= reviewers.length <= 200000
 * - 1 <= reviewers[i] <= 1000000000
 * - 0 <= k < reviewers.length
 *
 * Example 1:
 * Input: reviewers = [5,5,2,2,2,7,7,2], k = 2
 * Output: 7
 * Explanation: The span [5,5,2,2,2,7,7] has exactly 2 handoffs: 5->2 and 2->7.
 * Its length is 7. Any longer span would include the final 7->2 handoff, making 3 handoffs.
 *
 * Example 2:
 * Input: reviewers = [1,3,3,4,4,4,2,2,5], k = 1
 * Output: 5
 * Explanation: One valid longest span is [3,3,4,4,4], which contains a single handoff: 3->4.
 * No contiguous span of length 6 or more has at most 1 handoff.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray containing at most k handoffs.
     *
     * A handoff inside a window [left, right] is counted for every index i where
     * left < i <= right and reviewers[i] != reviewers[i - 1].
     *
     * Core sliding window idea:
     * - Expand the right boundary one step at a time.
     * - Whenever adding reviewers[right] creates a reviewer change from reviewers[right - 1],
     *   increase the handoff count.
     * - If the handoff count becomes greater than k, move the left boundary rightward
     *   until the window becomes valid again.
     *
     * @param reviewers the reviewer IDs in chronological order
     * @param k the maximum allowed number of handoffs inside the chosen span
     * @return the length of the longest contiguous span with at most k handoffs
     *
     * Time complexity: O(n), where n is reviewers.length, because each pointer moves at most n times.
     * Space complexity: O(1), ignoring input storage, because only a few variables are used.
     */
    public int longestAnnotationSpan(int[] reviewers, int k) {
        int n = reviewers.length;

        // Left boundary of the current sliding window.
        int left = 0;

        // Number of handoffs currently inside window [left, right].
        int handoffs = 0;

        // Best valid window length found so far.
        int best = 0;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < n; right++) {

            // When right > 0, the new element reviewers[right] introduces one new adjacent pair:
            // (right - 1, right).
            //
            // That pair belongs to the current window if both indices are inside the window.
            // Since right is always inside after expansion, and left <= right, this pair is relevant
            // unless left has already moved beyond right - 1 later during shrinking.
            //
            // At the moment of expansion, we simply add the effect of this new adjacent pair.
            if (right > 0 && reviewers[right] != reviewers[right - 1]) {
                handoffs++;
            }

            // If the window now has too many handoffs, shrink it from the left.
            //
            // Important detail:
            // When we move left from L to L + 1, the only adjacent pair that leaves the window is
            // (L, L + 1), because all other adjacent pairs remain fully inside.
            //
            // So before incrementing left, we check whether reviewers[left] != reviewers[left + 1].
            // If yes, that departing pair had contributed one handoff, so we subtract it.
            while (handoffs > k) {
                if (left + 1 <= right && reviewers[left] != reviewers[left + 1]) {
                    handoffs--;
                }
                left++;
            }

            // At this point, window [left, right] is valid: it has at most k handoffs.
            // Compute its length and update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * Helper method to format an integer array as a readable string.
     *
     * @param arr the array to convert to string
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is arr.length.
     * Space complexity: O(n), due to string construction.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * It prints:
     * - the input array
     * - the value of k
     * - the computed result
     * - the expected result
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) overall for the demonstrated inputs.
     * Space complexity: O(1) extra, excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] reviewers1 = {5, 5, 2, 2, 2, 7, 7, 2};
        int k1 = 2;
        int result1 = solution.longestAnnotationSpan(reviewers1, k1);
        System.out.println("Example 1");
        System.out.println("reviewers = " + solution.arrayToString(reviewers1));
        System.out.println("k = " + k1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 7");
        System.out.println();

        int[] reviewers2 = {1, 3, 3, 4, 4, 4, 2, 2, 5};
        int k2 = 1;
        int result2 = solution.longestAnnotationSpan(reviewers2, k2);
        System.out.println("Example 2");
        System.out.println("reviewers = " + solution.arrayToString(reviewers2));
        System.out.println("k = " + k2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 5");
        System.out.println();

        int[] reviewers3 = {9};
        int k3 = 0;
        int result3 = solution.longestAnnotationSpan(reviewers3, k3);
        System.out.println("Additional Example 3");
        System.out.println("reviewers = " + solution.arrayToString(reviewers3));
        System.out.println("k = " + k3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 1");
        System.out.println();

        int[] reviewers4 = {4, 4, 4, 4};
        int k4 = 0;
        int result4 = solution.longestAnnotationSpan(reviewers4, k4);
        System.out.println("Additional Example 4");
        System.out.println("reviewers = " + solution.arrayToString(reviewers4));
        System.out.println("k = " + k4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 4");
        System.out.println();

        int[] reviewers5 = {1, 2, 1, 2, 1};
        int k5 = 2;
        int result5 = solution.longestAnnotationSpan(reviewers5, k5);
        System.out.println("Additional Example 5");
        System.out.println("reviewers = " + solution.arrayToString(reviewers5));
        System.out.println("k = " + k5);
        System.out.println("Output = " + result5);
        System.out.println("Expected = 3");
    }
}