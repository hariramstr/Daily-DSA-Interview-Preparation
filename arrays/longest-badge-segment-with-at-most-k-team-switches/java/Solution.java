import java.util.*;

/*
 * Title: Longest Badge Segment With At Most K Team Switches
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an array teams where teams[i] is the team ID shown on the i-th badge scan
 * in chronological order. A security analyst wants to find the longest contiguous time
 * segment that is operationally stable, where the number of times the team ID changes
 * between adjacent scans is at most k.
 *
 * Formally, for a subarray teams[l...r], count how many indices i satisfy l < i <= r
 * and teams[i] != teams[i - 1]. This count is called the number of team switches inside
 * the segment. Your task is to return the maximum possible length of a contiguous segment
 * whose number of team switches is at most k.
 *
 * This is not the same as counting distinct team IDs. For example, the segment
 * [4, 4, 7, 7, 4] has 2 team switches, even though it contains only 2 distinct values.
 *
 * Return the length of the longest valid segment.
 *
 * Constraints:
 * - 1 <= teams.length <= 200000
 * - 0 <= k < teams.length
 * - 1 <= teams[i] <= 1000000000
 *
 * Example 1:
 * Input: teams = [5,5,2,2,2,9,9,5], k = 2
 * Output: 7
 * Explanation:
 * The subarray [5,5,2,2,2,9,9] has exactly 2 team switches: 5->2 and 2->9.
 * Its length is 7. Any segment of length 8 has 3 switches, so 7 is the maximum.
 *
 * Example 2:
 * Input: teams = [1,3,1,3,1,3], k = 3
 * Output: 4
 * Explanation:
 * Every adjacent pair is a switch. Any valid segment can contain at most 3 switches,
 * so its maximum length is 4, such as [1,3,1,3].
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray whose number of team switches
     * between adjacent elements is at most k.
     *
     * Core idea:
     * We use a sliding window [left, right].
     * For the current window, we maintain how many adjacent changes exist inside it.
     *
     * Important observation:
     * A switch is associated with an index i where teams[i] != teams[i - 1].
     * If both i - 1 and i are inside the current window, then that switch counts.
     *
     * As we expand the window to the right:
     * - When right moves from right - 1 to right, we add one new adjacent pair:
     *   (right - 1, right)
     * - If teams[right] != teams[right - 1], then the switch count increases by 1.
     *
     * As we shrink the window from the left:
     * - When left moves forward from left to left + 1, we remove the adjacent pair
     *   (left, left + 1) from the window.
     * - If teams[left] != teams[left + 1], then the switch count decreases by 1.
     *
     * This guarantees linear time because each pointer moves at most n times.
     *
     * @param teams the array of team IDs in chronological badge scan order
     * @param k the maximum allowed number of team switches inside a valid segment
     * @return the maximum length of a contiguous segment with at most k switches
     * Time complexity: O(n), where n is teams.length
     * Space complexity: O(1)
     */
    public int longestStableSegment(int[] teams, int k) {
        int n = teams.length;

        // Left boundary of the sliding window.
        int left = 0;

        // Number of team switches currently inside window [left, right].
        int switches = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Expand the window one element at a time using 'right'.
        for (int right = 0; right < n; right++) {

            // If right > 0, then adding teams[right] introduces exactly one new adjacent pair:
            // (right - 1, right).
            //
            // That pair contributes a switch if the values differ.
            if (right > 0 && teams[right] != teams[right - 1]) {
                switches++;
            }

            // If the window has too many switches, move 'left' forward until it becomes valid.
            //
            // When left moves from L to L + 1, the adjacent pair (L, L + 1) is no longer
            // fully inside the window, so if it was a switch, we must subtract it.
            while (switches > k) {
                if (left + 1 <= right && teams[left] != teams[left + 1]) {
                    switches--;
                }
                left++;
            }

            // Now window [left, right] is valid, so update the best length.
            int currentLength = right - left + 1;
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        return maxLength;
    }

    /**
     * Helper method to print an example run in a beginner-friendly format.
     *
     * @param teams the input team array
     * @param k the maximum allowed number of switches
     * @return the computed longest valid segment length
     * Time complexity: O(n), where n is teams.length
     * Space complexity: O(1)
     */
    public int demonstrateExample(int[] teams, int k) {
        int result = longestStableSegment(teams, k);
        System.out.println("teams = " + Arrays.toString(teams));
        System.out.println("k = " + k);
        System.out.println("Longest valid segment length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method demonstrating the solution on the sample inputs from the problem statement.
     *
     * Verified examples:
     * 1) [5,5,2,2,2,9,9,5], k = 2 -> 7
     * 2) [1,3,1,3,1,3], k = 3 -> 4
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of demonstrated examples)
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] teams1 = {5, 5, 2, 2, 2, 9, 9, 5};
        int k1 = 2;
        int result1 = solution.demonstrateExample(teams1, k1);
        System.out.println("Expected: 7");
        System.out.println("Actual:   " + result1);
        System.out.println();

        int[] teams2 = {1, 3, 1, 3, 1, 3};
        int k2 = 3;
        int result2 = solution.demonstrateExample(teams2, k2);
        System.out.println("Expected: 4");
        System.out.println("Actual:   " + result2);
        System.out.println();

        int[] extra1 = {4, 4, 7, 7, 4};
        int extraK1 = 2;
        int extraResult1 = solution.demonstrateExample(extra1, extraK1);
        System.out.println("Expected: 5");
        System.out.println("Actual:   " + extraResult1);
        System.out.println();

        int[] extra2 = {8};
        int extraK2 = 0;
        int extraResult2 = solution.demonstrateExample(extra2, extraK2);
        System.out.println("Expected: 1");
        System.out.println("Actual:   " + extraResult2);
    }
}