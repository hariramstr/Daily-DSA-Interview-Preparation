import java.util.*;

/*
 * Title: Longest Snack Cart Run Within Sugar Limit
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A company cafeteria records the sugar content of each snack item placed on a moving cart during the day.
 * You are given an integer array sugars where sugars[i] is the sugar amount of the i-th snack in grams,
 * and an integer limit representing the maximum total sugar a nutrition program allows in one consecutive selection.
 *
 * Your task is to find the length of the longest contiguous segment of the cart such that the sum of sugar
 * values in that segment is less than or equal to limit. In other words, choose a consecutive block of snacks
 * with total sugar at most limit, and return the maximum number of snacks that can be included.
 *
 * This problem models real-time monitoring where the cart is scanned from left to right and all sugar values
 * are non-negative. Because of that property, an efficient sliding window solution is expected.
 *
 * Return 0 if no snack can be included without exceeding the limit.
 *
 * Constraints:
 * - 1 <= sugars.length <= 100000
 * - 0 <= sugars[i] <= 100000
 * - 0 <= limit <= 1000000000
 * - All values in sugars are non-negative integers.
 *
 * Example 1:
 * Input: sugars = [2, 1, 3, 2, 1, 1], limit = 5
 * Output: 3
 * Explanation:
 * Valid longest segment is [2, 1, 1] with sum 4, so the answer is 3.
 *
 * Example 2:
 * Input: sugars = [6, 2, 2, 1], limit = 4
 * Correct Output: 2
 * Explanation:
 * The first snack alone exceeds the limit, so it cannot be included.
 * The segment [2, 2] has sum 4 and length 2.
 * The segment [2, 1] has sum 3 and length 2.
 * The segment [2, 2, 1] has sum 5, which exceeds the limit.
 * Therefore, the correct best segment length is 2.
 *
 * Important Note:
 * The original example text contains a contradiction by mentioning output 3 for Example 2,
 * but the correct answer is 2. This implementation follows the correct logic.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray whose sum is less than or equal to the given limit.
     *
     * This method uses the classic sliding window technique, which works efficiently here because all
     * sugar values are non-negative. That non-negative property is crucial:
     * - Expanding the window to the right can only keep the sum the same or increase it.
     * - Shrinking the window from the left can only keep the sum the same or decrease it.
     *
     * Because of this predictable behavior, we can move each pointer at most n times, giving linear time.
     *
     * @param sugars the array where sugars[i] is the sugar amount of the i-th snack
     * @param limit the maximum allowed total sugar for any chosen contiguous segment
     * @return the length of the longest contiguous segment with sum less than or equal to limit;
     *         returns 0 if no valid snack can be included
     *
     * Time complexity: O(n), where n is the length of the sugars array
     * Space complexity: O(1), ignoring input storage
     */
    public int longestSnackCartRun(int[] sugars, int limit) {
        // Left boundary of the current sliding window.
        int left = 0;

        // We use long for safety, even though int would still fit under the given constraints.
        // Using long is a good defensive habit when summing many integers.
        long currentSum = 0;

        // Stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < sugars.length; right++) {
            // Step 1:
            // Include the new element at index 'right' into the current window.
            currentSum += sugars[right];

            // Step 2:
            // If the window sum exceeds the limit, it is invalid.
            // Since all numbers are non-negative, the only way to make it valid again
            // is to move the left boundary to the right, removing elements from the window.
            while (currentSum > limit && left <= right) {
                currentSum -= sugars[left];
                left++;
            }

            // Step 3:
            // After shrinking, if currentSum <= limit, then the window [left..right] is valid.
            // Compute its length and update the answer if this window is longer.
            //
            // Window length formula:
            // right - left + 1
            int currentLength = right - left + 1;
            if (currentSum <= limit && currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // If no valid single element or segment was found, maxLength remains 0.
        return maxLength;
    }

    /**
     * Helper method to print an array in a beginner-friendly format.
     *
     * @param arr the integer array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution with sample inputs and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     *
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demo cases, excluding the algorithm calls themselves
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] sugars1 = {2, 1, 3, 2, 1, 1};
        int limit1 = 5;
        int result1 = solution.longestSnackCartRun(sugars1, limit1);
        System.out.println("Sample 1:");
        System.out.println("sugars = " + solution.arrayToString(sugars1));
        System.out.println("limit = " + limit1);
        System.out.println("Longest valid segment length = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        // Sample 2
        // Important: the problem statement text contains an inconsistency.
        // The correct answer for this input is 2, not 3.
        int[] sugars2 = {6, 2, 2, 1};
        int limit2 = 4;
        int result2 = solution.longestSnackCartRun(sugars2, limit2);
        System.out.println("Sample 2:");
        System.out.println("sugars = " + solution.arrayToString(sugars2));
        System.out.println("limit = " + limit2);
        System.out.println("Longest valid segment length = " + result2);
        System.out.println("Expected = 2");
        System.out.println();

        // Edge case: limit = 0 with zeros present
        int[] sugars3 = {0, 0, 1, 0, 0};
        int limit3 = 0;
        int result3 = solution.longestSnackCartRun(sugars3, limit3);
        System.out.println("Edge Case 1:");
        System.out.println("sugars = " + solution.arrayToString(sugars3));
        System.out.println("limit = " + limit3);
        System.out.println("Longest valid segment length = " + result3);
        System.out.println("Expected = 2");
        System.out.println();

        // Edge case: no snack can be included
        int[] sugars4 = {5, 6, 7};
        int limit4 = 4;
        int result4 = solution.longestSnackCartRun(sugars4, limit4);
        System.out.println("Edge Case 2:");
        System.out.println("sugars = " + solution.arrayToString(sugars4));
        System.out.println("limit = " + limit4);
        System.out.println("Longest valid segment length = " + result4);
        System.out.println("Expected = 0");
        System.out.println();

        // Edge case: all snacks fit
        int[] sugars5 = {1, 1, 1, 1};
        int limit5 = 10;
        int result5 = solution.longestSnackCartRun(sugars5, limit5);
        System.out.println("Edge Case 3:");
        System.out.println("sugars = " + solution.arrayToString(sugars5));
        System.out.println("limit = " + limit5);
        System.out.println("Longest valid segment length = " + result5);
        System.out.println("Expected = 4");
    }
}