import java.util.*;

/*
 * Title: Longest Snack Cart Run Within Sodium Limit
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A company cafeteria tracks the sodium content of snacks placed on a moving cart throughout the day.
 * You are given an integer array sodium where sodium[i] is the sodium amount (in milligrams) of the
 * i-th snack on the cart, in order. A customer wants to choose one contiguous run of snacks, but the
 * total sodium in that chosen run must not exceed a given limit maxSodium.
 *
 * Return the length of the longest contiguous subarray whose sum is less than or equal to maxSodium.
 * If no snack can be chosen without exceeding the limit, return 0.
 *
 * This problem models a realistic streaming or sequential selection scenario where the chosen items
 * must stay contiguous and the total budget must remain bounded. Since all sodium values are
 * non-negative, a sliding window approach can expand the right end of the window and shrink the left
 * end whenever the running sum becomes too large.
 *
 * Constraints:
 * - 1 <= sodium.length <= 200000
 * - 0 <= sodium[i] <= 100000
 * - 0 <= maxSodium <= 1000000000
 * - All values fit in 32-bit integers, but using a 64-bit running sum is recommended.
 *
 * Important correction to the provided examples:
 * Example 1 in the prompt contains a contradiction. For:
 * sodium = [120, 80, 200, 90, 60], maxSodium = 300
 * the correct answer is 2, not 3, because:
 * - [200, 90, 60] = 350, invalid
 * - [120, 80] = 200, valid
 * - [80, 200] = 280, valid
 * - [200, 90] = 290, valid
 * - No contiguous subarray of length 3 has sum <= 300
 *
 * Example 2:
 * Input: sodium = [40, 0, 30, 20, 10], maxSodium = 60
 * Output: 4
 * Explanation: The subarray [0, 30, 20, 10] has total sodium 60, so its length is 4.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray whose sum is less than or equal to maxSodium.
     *
     * This method uses the classic sliding window technique:
     * 1. Expand the window by moving the right pointer.
     * 2. Add the new element to the running sum.
     * 3. If the sum becomes too large, shrink the window from the left until the sum is valid again.
     * 4. Track the maximum valid window length seen so far.
     *
     * This works because all sodium values are non-negative. That property guarantees that:
     * - Expanding the window can only keep or increase the sum.
     * - Shrinking the window can only keep or decrease the sum.
     * Therefore, once a window is too large, moving the left pointer forward is the correct way
     * to restore validity.
     *
     * @param sodium the array of non-negative sodium values for snacks in cart order
     * @param maxSodium the maximum allowed total sodium for the chosen contiguous run
     * @return the maximum length of a contiguous subarray with sum <= maxSodium; returns 0 if none exists
     *
     * Time complexity: O(n), where n is sodium.length, because each index is visited at most twice
     *                  (once by the right pointer and once by the left pointer).
     * Space complexity: O(1), because only a few variables are used regardless of input size.
     */
    public int longestSnackCartRun(int[] sodium, int maxSodium) {
        // Left boundary of the current sliding window.
        int left = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Use long for safety, because many integers added together can exceed int range.
        long currentSum = 0L;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < sodium.length; right++) {
            // Step 1: include the new rightmost element in the current window.
            currentSum += sodium[right];

            // Step 2: if the window sum is too large, shrink from the left.
            // We keep removing elements from the left until the window becomes valid again.
            while (currentSum > maxSodium && left <= right) {
                currentSum -= sodium[left];
                left++;
            }

            // Step 3: now the window [left..right] is valid (or empty if left passed right).
            // Its length is right - left + 1.
            int currentLength = right - left + 1;

            // Step 4: update the best answer if this valid window is longer.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        return maxLength;
    }

    /**
     * A helper method that prints a demonstration of the sliding window result for a given test case.
     *
     * @param sodium the input sodium array
     * @param maxSodium the sodium limit
     * @return the computed longest valid contiguous run length
     *
     * Time complexity: O(n), where n is sodium.length
     * Space complexity: O(1), excluding the space used by array-to-string conversion for printing
     */
    public int demonstrateCase(int[] sodium, int maxSodium) {
        int result = longestSnackCartRun(sodium, maxSodium);
        System.out.println("sodium = " + Arrays.toString(sodium));
        System.out.println("maxSodium = " + maxSodium);
        System.out.println("Longest valid contiguous run length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     *
     * Demonstrates the solution on:
     * - The corrected interpretation of Example 1
     * - Example 2
     * - A few additional edge cases for clarity
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(total number of elements across demonstrated test cases)
     * Space complexity: O(1), excluding printing-related overhead
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt contains an inconsistency.
        // Correct result is 2.
        int[] sodium1 = {120, 80, 200, 90, 60};
        int maxSodium1 = 300;
        int result1 = solution.demonstrateCase(sodium1, maxSodium1);
        System.out.println("Expected (corrected): 2");
        System.out.println("Actual: " + result1);
        System.out.println();

        // Example 2 from the prompt.
        int[] sodium2 = {40, 0, 30, 20, 10};
        int maxSodium2 = 60;
        int result2 = solution.demonstrateCase(sodium2, maxSodium2);
        System.out.println("Expected: 4");
        System.out.println("Actual: " + result2);
        System.out.println();

        // Additional test: no single snack fits.
        int[] sodium3 = {500, 600, 700};
        int maxSodium3 = 400;
        int result3 = solution.demonstrateCase(sodium3, maxSodium3);
        System.out.println("Expected: 0");
        System.out.println("Actual: " + result3);
        System.out.println();

        // Additional test: all snacks fit.
        int[] sodium4 = {10, 20, 30};
        int maxSodium4 = 100;
        int result4 = solution.demonstrateCase(sodium4, maxSodium4);
        System.out.println("Expected: 3");
        System.out.println("Actual: " + result4);
        System.out.println();

        // Additional test: limit is zero, only zero-valued snacks can be included.
        int[] sodium5 = {0, 0, 5, 0, 0, 0};
        int maxSodium5 = 0;
        int result5 = solution.demonstrateCase(sodium5, maxSodium5);
        System.out.println("Expected: 3");
        System.out.println("Actual: " + result5);
    }
}