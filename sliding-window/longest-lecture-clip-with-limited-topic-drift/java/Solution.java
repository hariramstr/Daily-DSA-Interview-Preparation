import java.util.*;

/*
 * Title: Longest Lecture Clip With Limited Topic Drift
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array `topics` representing the topic label of each consecutive minute
 * in a recorded lecture. The lecture platform wants to extract the longest contiguous clip
 * that still feels focused. A clip is considered focused if it contains at most `k` topic
 * transitions, where a transition happens between two adjacent minutes `i - 1` and `i`
 * when `topics[i] != topics[i - 1]`.
 *
 * Return the length of the longest contiguous subarray of `topics` that contains at most
 * `k` transitions.
 *
 * This is not the same as limiting the number of distinct topic labels. For example,
 * the clip `[2, 2, 3, 3, 2]` has only 2 distinct labels, but it has 2 transitions:
 * `2 -> 3` and `3 -> 2`.
 *
 * Your task is to design an efficient algorithm that scans the lecture once or nearly once,
 * since the input can be large.
 *
 * Constraints:
 * - `1 <= topics.length <= 2 * 10^5`
 * - `1 <= topics[i] <= 10^9`
 * - `0 <= k < topics.length`
 *
 * Example 1:
 * Input: `topics = [4, 4, 1, 1, 1, 3, 3, 4], k = 2`
 * Output: `7`
 * Explanation: The subarray `[4, 4, 1, 1, 1, 3, 3]` has exactly 2 transitions
 * (`4 -> 1`, `1 -> 3`) and length 7. Including the final `4` would create a third transition.
 *
 * Example 2:
 * Input: `topics = [5, 6, 5, 6, 5], k = 1`
 * Output: `2`
 * Explanation: Every adjacent pair changes topic, so any subarray of length 3 has 2 transitions.
 * The longest valid clips are any length-2 subarrays such as `[5, 6]` or `[6, 5]`.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray that contains at most k topic transitions.
     *
     * A transition inside a window [left, right] happens at position i (left < i <= right)
     * when topics[i] != topics[i - 1].
     *
     * Core sliding-window idea:
     * - Expand the right boundary one step at a time.
     * - When adding topics[right], check whether the pair (right - 1, right) creates a new transition.
     * - If the number of transitions becomes too large, move the left boundary rightward
     *   until the window becomes valid again.
     * - While moving left, if we remove the pair (left, left + 1) and that pair was a transition,
     *   we decrement the transition count.
     *
     * @param topics the topic label for each consecutive minute of the lecture
     * @param k the maximum number of allowed topic transitions inside the chosen clip
     * @return the maximum length of a contiguous subarray with at most k transitions
     *
     * Time complexity: O(n), where n is topics.length, because each pointer moves at most n times.
     * Space complexity: O(1), because only a few variables are used.
     */
    public int longestFocusedClip(int[] topics, int k) {
        int n = topics.length;

        // Left boundary of the current sliding window.
        int left = 0;

        // Number of topic transitions currently inside the window [left, right].
        int transitions = 0;

        // Best answer found so far.
        int best = 0;

        // We expand the window by moving "right" from 0 to n - 1.
        for (int right = 0; right < n; right++) {

            // Step 1:
            // If right > 0, then the new element topics[right] forms an adjacent pair
            // with topics[right - 1].
            //
            // That adjacent pair belongs to the current window if both indices are inside it.
            // Since right is newly added and left <= right always holds, the pair is now part
            // of the window whenever right > left or even if left == right - 1.
            //
            // If the values differ, we have introduced one more transition into the window.
            if (right > 0 && topics[right] != topics[right - 1]) {
                transitions++;
            }

            // Step 2:
            // If the window now has too many transitions, shrink it from the left.
            //
            // Important detail:
            // When we move left forward from left to left + 1, the only adjacent pair that
            // leaves the window is (left, left + 1).
            //
            // If topics[left] != topics[left + 1], then that pair contributed exactly one
            // transition to the window, so we must subtract it.
            while (transitions > k) {
                if (left + 1 <= right && topics[left] != topics[left + 1]) {
                    transitions--;
                }
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is valid:
            // it contains at most k transitions.
            //
            // Compute its length and update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * A small helper method to print an array in a beginner-friendly way.
     *
     * @param arr the array to convert to a string
     * @return a readable string representation of the array
     *
     * Time complexity: O(n), where n is arr.length.
     * Space complexity: O(n), due to string construction.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated test case.
     * Space complexity: O(1) extra, excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] topics1 = {4, 4, 1, 1, 1, 3, 3, 4};
        int k1 = 2;
        int result1 = solution.longestFocusedClip(topics1, k1);
        System.out.println("Example 1:");
        System.out.println("topics = " + solution.arrayToString(topics1) + ", k = " + k1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 7");
        System.out.println();

        int[] topics2 = {5, 6, 5, 6, 5};
        int k2 = 1;
        int result2 = solution.longestFocusedClip(topics2, k2);
        System.out.println("Example 2:");
        System.out.println("topics = " + solution.arrayToString(topics2) + ", k = " + k2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 2");
        System.out.println();

        int[] topics3 = {2, 2, 3, 3, 2};
        int k3 = 2;
        int result3 = solution.longestFocusedClip(topics3, k3);
        System.out.println("Additional Check:");
        System.out.println("topics = " + solution.arrayToString(topics3) + ", k = " + k3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 5");
        System.out.println();

        int[] topics4 = {7, 7, 7, 7};
        int k4 = 0;
        int result4 = solution.longestFocusedClip(topics4, k4);
        System.out.println("Additional Check:");
        System.out.println("topics = " + solution.arrayToString(topics4) + ", k = " + k4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 4");
    }
}