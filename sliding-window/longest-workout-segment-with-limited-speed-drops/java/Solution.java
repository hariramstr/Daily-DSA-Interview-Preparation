import java.util.*;

/*
 * Title: Longest Workout Segment With Limited Speed Drops
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array speed where speed[i] is the runner's speed during the i-th minute
 * of a workout, and an integer k. A minute i (for i > 0) is called a speed drop if
 * speed[i] < speed[i - 1]. Your task is to find the length of the longest contiguous
 * segment of the workout that contains at most k speed drops.
 *
 * In other words, choose a subarray speed[l...r] such that within that subarray, the number
 * of indices i with l < i <= r and speed[i] < speed[i - 1] is at most k. Return the maximum
 * possible length of such a segment.
 *
 * This models a real fitness analytics scenario where a coach wants to identify the longest
 * sustained stretch of a workout with only a limited number of slowdowns.
 *
 * Constraints:
 * - 1 <= speed.length <= 2 * 10^5
 * - 0 <= k < speed.length
 * - 1 <= speed[i] <= 10^9
 *
 * Example 1:
 * Input: speed = [5, 6, 4, 4, 7, 3, 8], k = 1
 * Output: 5
 * Explanation:
 * One optimal segment is [6, 4, 4, 7, 3], which contains exactly 1 speed drop inside the
 * segment at 4 < 6 and no other drop until the end comparison 3 < 7, so actually that would
 * be 2 drops and is invalid.
 * Valid optimal examples of length 5 are:
 * - [5, 6, 4, 4, 7] with exactly 1 drop (4 < 6)
 * - [6, 4, 4, 7, 3] is invalid because it has 2 drops
 * Therefore the correct answer for this input is 5.
 *
 * Example 2:
 * Input: speed = [9, 8, 7, 10, 11, 6, 12], k = 2
 * Output: 6
 * Explanation:
 * The segment [8, 7, 10, 11, 6, 12] has exactly 2 speed drops:
 * - 7 < 8
 * - 6 < 11
 * Therefore length 6 is achievable, and it is optimal.
 *
 * Note:
 * The originally stated sample outputs in the prompt are inconsistent with the formal problem
 * definition. This implementation follows the formal definition exactly and returns the correct
 * values for the described condition "at most k speed drops within the chosen subarray".
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray that contains at most k speed drops.
     *
     * A "speed drop" inside a window [left, right] happens at an index i where:
     * left < i <= right and speed[i] < speed[i - 1].
     *
     * Core idea:
     * We use a sliding window with two pointers.
     * - Expand the right pointer one step at a time.
     * - Whenever adding the new element creates a new speed drop, increase the drop counter.
     * - If the number of drops becomes greater than k, move the left pointer rightward until
     *   the window becomes valid again.
     *
     * Why this works:
     * - Every drop is determined only by a neighboring pair (i - 1, i).
     * - So when the window changes, we only need to update the count for the boundary pairs
     *   that enter or leave the window.
     *
     * @param speed the array of runner speeds for each minute
     * @param k the maximum number of allowed speed drops inside the chosen segment
     * @return the length of the longest contiguous segment with at most k speed drops
     * Time complexity: O(n), because each pointer moves at most n times
     * Space complexity: O(1), ignoring input storage
     */
    public int longestWorkoutSegment(int[] speed, int k) {
        // Defensive handling for completeness.
        // Based on constraints, speed.length >= 1, but this keeps the method robust.
        if (speed == null || speed.length == 0) {
            return 0;
        }

        int n = speed.length;

        // left marks the beginning of the current sliding window.
        int left = 0;

        // drops stores how many speed drops currently exist inside window [left, right].
        int drops = 0;

        // best stores the maximum valid window length seen so far.
        int best = 1;

        // We expand the window by moving right from 1 to n - 1.
        // We can start from 1 because a drop is defined by comparing speed[i] with speed[i - 1].
        for (int right = 1; right < n; right++) {

            // Step 1:
            // Check whether the newly added adjacent pair (right - 1, right)
            // creates a speed drop inside the current window.
            //
            // If speed[right] < speed[right - 1], then index "right" is a drop point.
            if (speed[right] < speed[right - 1]) {
                drops++;
            }

            // Step 2:
            // If the window now has too many drops, shrink it from the left
            // until it becomes valid again.
            //
            // Important detail:
            // When left moves from x to x + 1, the only adjacent comparison that leaves
            // the window is the pair (x, x + 1). If that pair was a drop, we must subtract it.
            while (drops > k) {
                if (left + 1 <= right && speed[left + 1] < speed[left]) {
                    drops--;
                }
                left++;
            }

            // Step 3:
            // Now the window [left, right] is valid, so update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * A helper method that prints a detailed demonstration for one test case.
     *
     * @param speed the input speed array
     * @param k the maximum allowed number of speed drops
     * @return the computed longest valid segment length
     * Time complexity: O(n), because it calls the main algorithm once
     * Space complexity: O(1), ignoring input storage
     */
    public int demonstrateCase(int[] speed, int k) {
        int result = longestWorkoutSegment(speed, k);
        System.out.println("speed = " + Arrays.toString(speed));
        System.out.println("k = " + k);
        System.out.println("Longest valid segment length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution on sample and additional test cases.
     *
     * Note:
     * The prompt's sample outputs are inconsistent with the formal definition.
     * This main method prints the correct outputs according to the actual problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of demonstrated cases)
     * Space complexity: O(1), ignoring input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the prompt.
        // Formal-definition-correct answer is 5:
        // [5, 6, 4, 4, 7] has exactly one drop: 4 < 6.
        int[] speed1 = {5, 6, 4, 4, 7, 3, 8};
        int k1 = 1;
        solution.demonstrateCase(speed1, k1);

        // Sample 2 from the prompt.
        // Formal-definition-correct answer is 6:
        // [8, 7, 10, 11, 6, 12] has exactly two drops: 7 < 8 and 6 < 11.
        int[] speed2 = {9, 8, 7, 10, 11, 6, 12};
        int k2 = 2;
        solution.demonstrateCase(speed2, k2);

        // Additional beginner-friendly checks.

        // No drops allowed, fully non-decreasing segment should be found.
        int[] speed3 = {1, 2, 2, 3, 4};
        int k3 = 0;
        solution.demonstrateCase(speed3, k3);

        // All decreasing, with only one allowed drop.
        int[] speed4 = {10, 9, 8, 7, 6};
        int k4 = 1;
        solution.demonstrateCase(speed4, k4);

        // Single element array.
        int[] speed5 = {42};
        int k5 = 0;
        solution.demonstrateCase(speed5, k5);
    }
}