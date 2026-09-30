import java.util.*;

/*
 * Title: Longest Cooking Streak Within Spice Budget
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array heat of length n, where heat[i] represents the spice level
 * added by the i-th dish cooked in order during a live kitchen session.
 * A chef wants to select one contiguous streak of dishes to present as a tasting sequence.
 * The total spice used in that streak must not exceed a given integer budget.
 * Your task is to return the maximum number of consecutive dishes the chef can include.
 *
 * Formally, find the length of the longest contiguous subarray whose sum is less than
 * or equal to budget.
 *
 * This problem is designed so that an efficient sliding window solution is expected.
 * Since all spice values are non-negative, expanding the right end of the window can
 * only increase or keep the current sum the same, and shrinking from the left reduces
 * it when the budget is exceeded.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 0 <= heat[i] <= 100000
 * - 0 <= budget <= 10^15
 * - The answer always fits in a 32-bit signed integer.
 *
 * Example 1:
 * Input: heat = [2, 1, 3, 2, 1, 1], budget = 5
 * Output: 3
 *
 * Example 2:
 * Input: heat = [0, 4, 0, 2, 1, 0, 1], budget = 3
 * Output: 4
 *
 * Return only the maximum length of such a contiguous streak.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray whose sum is less than or equal to budget.
     *
     * This method uses the classic sliding window / two-pointer technique.
     * Because all values in the array are non-negative:
     * - expanding the window to the right never decreases the sum
     * - shrinking the window from the left never increases the sum
     *
     * That property allows us to process the array in linear time.
     *
     * @param heat the array of non-negative spice values for dishes cooked in order
     * @param budget the maximum allowed total spice for the chosen contiguous streak
     * @return the maximum number of consecutive dishes whose total spice does not exceed budget
     *
     * Time complexity: O(n), because each index is added to and removed from the window at most once.
     * Space complexity: O(1), because only a few variables are used regardless of input size.
     */
    public int longestCookingStreak(int[] heat, long budget) {
        // Left boundary of the current sliding window.
        int left = 0;

        // Running sum of the current window [left..right].
        // We use long because:
        // - n can be large
        // - values can be large
        // - budget itself can be up to 10^15
        long currentSum = 0L;

        // Best valid window length found so far.
        int maxLength = 0;

        // Move the right boundary one step at a time.
        for (int right = 0; right < heat.length; right++) {
            // Step 1:
            // Include heat[right] in the current window.
            currentSum += heat[right];

            // Step 2:
            // If the window sum is too large, shrink from the left
            // until the window becomes valid again.
            //
            // This works because all numbers are non-negative.
            // Removing elements from the left can only decrease or keep the sum the same.
            while (currentSum > budget && left <= right) {
                currentSum -= heat[left];
                left++;
            }

            // Step 3:
            // At this point, the window [left..right] is guaranteed valid:
            // currentSum <= budget
            //
            // So we compute its length and update the answer if this window is longer.
            int currentLength = right - left + 1;
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        return maxLength;
    }

    /**
     * Helper method to print an array in a readable format.
     *
     * @param arr the integer array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n), due to the produced string content.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It prints:
     * - the input array
     * - the budget
     * - the computed answer
     * - the expected answer for verification
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration case.
     * Space complexity: O(1) extra, excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] heat1 = {2, 1, 3, 2, 1, 1};
        long budget1 = 5L;
        int result1 = solution.longestCookingStreak(heat1, budget1);

        System.out.println("Sample 1");
        System.out.println("heat = " + solution.arrayToString(heat1));
        System.out.println("budget = " + budget1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        // Sample 2
        int[] heat2 = {0, 4, 0, 2, 1, 0, 1};
        long budget2 = 3L;
        int result2 = solution.longestCookingStreak(heat2, budget2);

        System.out.println("Sample 2");
        System.out.println("heat = " + solution.arrayToString(heat2));
        System.out.println("budget = " + budget2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 4");
        System.out.println();

        // Additional quick checks for beginner-friendly understanding.

        // Entire array fits.
        int[] heat3 = {1, 1, 1, 1};
        long budget3 = 10L;
        System.out.println("Additional Test 1");
        System.out.println("heat = " + solution.arrayToString(heat3));
        System.out.println("budget = " + budget3);
        System.out.println("Output = " + solution.longestCookingStreak(heat3, budget3));
        System.out.println("Expected = 4");
        System.out.println();

        // Budget is zero, only zero-valued dishes can be included.
        int[] heat4 = {0, 0, 1, 0, 0};
        long budget4 = 0L;
        System.out.println("Additional Test 2");
        System.out.println("heat = " + solution.arrayToString(heat4));
        System.out.println("budget = " + budget4);
        System.out.println("Output = " + solution.longestCookingStreak(heat4, budget4));
        System.out.println("Expected = 2");
    }
}