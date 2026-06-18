import java.util.*;

/*
 * Title: Longest Reading Session With Limited Genre Switches
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array genres where genres[i] is the genre ID of the i-th article a user reads
 * in chronological order. A reading session is defined as any contiguous segment of this array.
 * Product analysts want to find the longest session that feels focused, so they allow at most k
 * genre switches inside the session.
 *
 * A genre switch occurs between two adjacent articles when their genre IDs are different.
 * For example, in the session [2, 2, 5, 5, 3], there are 2 genre switches:
 * one from 2 to 5 and one from 5 to 3.
 *
 * Return the length of the longest contiguous reading session that contains at most k genre switches.
 *
 * This is not the same as limiting the number of distinct genres. A session may contain many articles
 * of the same genre grouped together, and only changes between neighboring articles count toward the
 * switch total.
 *
 * Your task is to design an efficient algorithm that works for large inputs.
 *
 * Constraints:
 * - 1 <= genres.length <= 200000
 * - 1 <= genres[i] <= 1000000000
 * - 0 <= k < genres.length
 *
 * Example 1:
 * Input: genres = [1, 1, 2, 2, 2, 3, 3], k = 1
 * Output: 5
 * Explanation: The longest valid session is [1, 1, 2, 2, 2] or [2, 2, 2, 3, 3].
 * Each has exactly 1 genre switch.
 *
 * Example 2:
 * Input: genres = [4, 7, 7, 4, 4, 9, 9, 9, 4], k = 2
 * Output: 7
 * Explanation: One optimal session is [7, 7, 4, 4, 9, 9, 9].
 * The genre switches are 7 -> 4 and 4 -> 9, so the session is valid.
 * No longer contiguous segment has at most 2 switches.
 *
 * Notes:
 * - A session of length 1 always has 0 genre switches.
 * - If k = 0, the answer is the longest contiguous block of equal genre IDs.
 * - An O(n) sliding window solution is expected.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous reading session that contains at most k genre switches.
     *
     * Core idea:
     * A switch is created only at a boundary between adjacent elements:
     * genres[i - 1] != genres[i]
     *
     * For any window [left, right], the number of switches inside that window equals the number of
     * indices j such that left < j <= right and genres[j - 1] != genres[j].
     *
     * We maintain a sliding window:
     * - Expand the right boundary one step at a time.
     * - If adding the new element creates a new switch with its left neighbor, increment switch count.
     * - If switch count becomes too large, move the left boundary rightward until the window is valid again.
     * - While moving left, if we remove a boundary that used to be a switch, decrement switch count.
     *
     * @param genres the array of genre IDs in chronological reading order
     * @param k the maximum allowed number of genre switches inside a session
     * @return the maximum length of a contiguous session with at most k genre switches
     *
     * Time complexity: O(n), where n is genres.length, because each pointer moves at most n times.
     * Space complexity: O(1), ignoring input storage, because only a few variables are used.
     */
    public int longestReadingSession(int[] genres, int k) {
        int n = genres.length;

        // Left boundary of the current sliding window.
        int left = 0;

        // Number of genre switches currently inside the window [left, right].
        int switches = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < n; right++) {

            // When we extend the window to include genres[right], we only create at most one new switch:
            // the boundary between genres[right - 1] and genres[right].
            //
            // This boundary belongs to the window only if right > left, but it is safe to count it here
            // whenever right > 0 and the values differ. If later the window becomes invalid, the shrinking
            // logic will remove boundaries that leave the window.
            if (right > 0 && genres[right] != genres[right - 1]) {
                switches++;
            }

            // If the window has too many switches, shrink it from the left until it becomes valid.
            while (switches > k) {

                // We are about to move left from 'left' to 'left + 1'.
                // That means the boundary between genres[left] and genres[left + 1] will no longer be
                // inside the window after left is incremented.
                //
                // If that boundary was a switch, we must subtract it from the switch count.
                if (left + 1 <= right && genres[left] != genres[left + 1]) {
                    switches--;
                }

                left++;
            }

            // Now the window [left, right] is valid: it has at most k switches.
            // Update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        return maxLength;
    }

    /**
     * A helper method that prints an input array in a readable format and shows the computed answer.
     *
     * @param genres the array of genre IDs
     * @param k the maximum allowed number of genre switches
     * @return the computed longest valid session length
     *
     * Time complexity: O(n), because it calls the main O(n) algorithm.
     * Space complexity: O(1), excluding the string created for printing the array.
     */
    public int demonstrateCase(int[] genres, int k) {
        int result = longestReadingSession(genres, k);
        System.out.println("genres = " + Arrays.toString(genres));
        System.out.println("k = " + k);
        System.out.println("Longest valid session length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     * Demonstrates the algorithm on the sample inputs from the problem statement
     * and a few additional beginner-friendly checks.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(total input size of demonstrated test cases).
     * Space complexity: O(1), excluding printing-related overhead.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1:
        // genres = [1, 1, 2, 2, 2, 3, 3], k = 1
        // Expected output: 5
        int[] genres1 = {1, 1, 2, 2, 2, 3, 3};
        int result1 = solution.demonstrateCase(genres1, 1);
        System.out.println("Expected: 5, Actual: " + result1);
        System.out.println();

        // Sample 2:
        // genres = [4, 7, 7, 4, 4, 9, 9, 9, 4], k = 2
        // Expected output: 7
        int[] genres2 = {4, 7, 7, 4, 4, 9, 9, 9, 4};
        int result2 = solution.demonstrateCase(genres2, 2);
        System.out.println("Expected: 7, Actual: " + result2);
        System.out.println();

        // Additional check: k = 0 means we want the longest block of equal values.
        int[] genres3 = {5, 5, 5, 2, 2, 8, 8, 8, 8, 1};
        int result3 = solution.demonstrateCase(genres3, 0);
        System.out.println("Expected: 4, Actual: " + result3);
        System.out.println();

        // Additional check: single element array.
        int[] genres4 = {42};
        int result4 = solution.demonstrateCase(genres4, 0);
        System.out.println("Expected: 1, Actual: " + result4);
        System.out.println();

        // Additional check: all same genre, any k works.
        int[] genres5 = {9, 9, 9, 9, 9};
        int result5 = solution.demonstrateCase(genres5, 3);
        System.out.println("Expected: 5, Actual: " + result5);
    }
}