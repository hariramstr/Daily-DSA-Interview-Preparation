import java.util.*;

/*
 * Title: Maximize Scenic Stops Within Fuel Budget
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are planning a road trip along a straight highway. There are n scenic stops
 * placed in increasing order of distance from the start, given by the array miles,
 * where miles[i] is the mile marker of the i-th stop. Visiting a group of consecutive
 * scenic stops is only practical if the distance between the farthest and nearest stop
 * in that group does not exceed your extra sightseeing fuel budget budget.
 *
 * Your task is to return the maximum number of consecutive scenic stops you can include
 * in a single sightseeing segment such that:
 *
 * miles[right] - miles[left] <= budget
 *
 * for the chosen segment [left, right].
 *
 * This models the idea that once you commit to a detour region, you only want stops
 * clustered closely enough together to avoid wasting fuel driving back and forth.
 * You may choose any contiguous block of stops from the sorted list, including a block
 * of length 1.
 *
 * Constraints:
 * - 1 <= n <= 100000
 * - 0 <= miles[i] <= 10^9
 * - miles is sorted in non-decreasing order
 * - 0 <= budget <= 10^9
 *
 * Example 1:
 * Input: miles = [2, 5, 6, 7, 11, 15], budget = 4
 * Output: 3
 * Explanation: The segment [5, 6, 7] fits because 7 - 5 = 2 <= 4.
 * The segment [2, 5, 6, 7] does not fit because 7 - 2 = 5 > 4.
 *
 * Example 2:
 * Input: miles = [1, 1, 3, 4, 8, 9, 10], budget = 2
 * Output: 3
 * Explanation: One optimal segment is [8, 9, 10], where 10 - 8 = 2.
 * Another valid segment of length 3 is [1, 1, 3], where 3 - 1 = 2.
 */

public class Solution {

    /**
     * Returns the maximum number of consecutive scenic stops that can fit inside
     * a segment whose farthest and nearest mile markers differ by at most budget.
     *
     * This method uses the classic sliding window / two pointers technique.
     * Because the array is already sorted in non-decreasing order, for any window
     * [left, right], the minimum value is miles[left] and the maximum value is
     * miles[right]. That means we can check validity using only:
     *
     * miles[right] - miles[left] <= budget
     *
     * If the window becomes invalid, we move left forward until it becomes valid again.
     *
     * @param miles the sorted array of mile markers for scenic stops
     * @param budget the maximum allowed difference between the farthest and nearest stop
     * @return the largest size of a valid consecutive segment
     * Time complexity: O(n), because each pointer moves at most n times.
     * Space complexity: O(1), ignoring input storage.
     */
    public int maxScenicStops(int[] miles, int budget) {
        // Defensive handling:
        // The constraints guarantee at least one element, but this makes the method
        // safer and more beginner-friendly if reused elsewhere.
        if (miles == null || miles.length == 0) {
            return 0;
        }

        // left marks the beginning of the current window.
        int left = 0;

        // best stores the maximum valid window length seen so far.
        int best = 1;

        // Expand the window by moving right from left to right across the array.
        for (int right = 0; right < miles.length; right++) {

            // At this moment, we are trying to include miles[right] in the current window.
            // Since the array is sorted, the only possible reason the window becomes invalid
            // is that the new farthest element miles[right] is too far from miles[left].
            //
            // While the current window violates the budget condition,
            // move left forward to shrink the window.
            while (miles[right] - miles[left] > budget) {
                left++;
            }

            // Now the window [left, right] is guaranteed to be valid.
            // Its length is:
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is larger.
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * A second public method that performs the same task, included for clarity and
     * interview-style readability. It simply delegates to the main implementation.
     *
     * @param miles the sorted array of mile markers for scenic stops
     * @param budget the maximum allowed difference between the farthest and nearest stop
     * @return the maximum number of consecutive scenic stops in a valid segment
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int solve(int[] miles, int budget) {
        return maxScenicStops(miles, budget);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It also prints the arrays and budgets so the output is easy to understand.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) overall for the demonstrated test cases.
     * Space complexity: O(1), excluding the sample input arrays.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] miles1 = {2, 5, 6, 7, 11, 15};
        int budget1 = 4;
        int result1 = solution.maxScenicStops(miles1, budget1);

        System.out.println("Sample 1:");
        System.out.println("miles = " + Arrays.toString(miles1));
        System.out.println("budget = " + budget1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        // Sample 2
        int[] miles2 = {1, 1, 3, 4, 8, 9, 10};
        int budget2 = 2;
        int result2 = solution.maxScenicStops(miles2, budget2);

        System.out.println("Sample 2:");
        System.out.println("miles = " + Arrays.toString(miles2));
        System.out.println("budget = " + budget2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 3");
        System.out.println();

        // Additional quick checks for beginners:
        // Single element array -> answer must be 1
        int[] miles3 = {42};
        int budget3 = 0;
        int result3 = solution.maxScenicStops(miles3, budget3);

        System.out.println("Additional Test 1:");
        System.out.println("miles = " + Arrays.toString(miles3));
        System.out.println("budget = " + budget3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 1");
        System.out.println();

        // All stops fit in one segment
        int[] miles4 = {3, 4, 5, 6};
        int budget4 = 3;
        int result4 = solution.maxScenicStops(miles4, budget4);

        System.out.println("Additional Test 2:");
        System.out.println("miles = " + Arrays.toString(miles4));
        System.out.println("budget = " + budget4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 4");
    }
}