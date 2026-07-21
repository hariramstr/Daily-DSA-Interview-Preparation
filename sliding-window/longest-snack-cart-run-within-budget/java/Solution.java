import java.util.*;

/*
 * Title: Longest Snack Cart Run Within Budget
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * A company campus has a row of snack carts, and each cart has a fixed price for buying one item.
 * You are given an integer array prices where prices[i] is the cost at the i-th cart, and an integer budget.
 * During a break, an employee wants to visit a contiguous sequence of carts and buy exactly one item from each
 * cart in that sequence. Your task is to return the maximum number of consecutive carts the employee can include
 * without the total cost exceeding budget.
 *
 * In other words, find the length of the longest contiguous subarray whose sum is less than or equal to budget.
 *
 * This problem is designed to be solved efficiently using a sliding window. Since all prices are positive,
 * once the current window exceeds the budget, moving the left side forward is guaranteed to reduce the total.
 *
 * Constraints:
 * - 1 <= prices.length <= 100000
 * - 1 <= prices[i] <= 10000
 * - 1 <= budget <= 1000000000
 *
 * Example 1:
 * Input: prices = [2, 1, 3, 2, 1], budget = 6
 * Output: 3
 * Explanation: One valid longest segment is [1, 3, 2], whose total cost is 6.
 * No contiguous segment of length 4 or more stays within the budget.
 *
 * Example 2:
 * Input: prices = [5, 2, 2, 1, 4], budget = 5
 * Output: 2
 * Explanation: The longest affordable contiguous segment has length 2, such as [2, 2] or [1, 4].
 * A segment of length 3 would exceed the budget.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray whose sum is less than or equal to the given budget.
     *
     * This method uses the sliding window technique:
     * - Expand the window by moving the right pointer.
     * - Keep track of the current sum of the window.
     * - If the sum becomes larger than the budget, shrink the window from the left
     *   until the sum is valid again.
     * - Track the largest valid window length seen so far.
     *
     * Because all prices are positive, shrinking the window always decreases the sum,
     * which makes the sliding window approach correct and efficient.
     *
     * @param prices the array where prices[i] is the cost at the i-th cart
     * @param budget the maximum total cost allowed for a contiguous sequence of carts
     * @return the maximum number of consecutive carts that can be included without exceeding the budget;
     *         returns 0 if no single cart can be afforded
     *
     * Time complexity: O(n), where n is the length of the prices array, because each index
     * is visited at most twice (once by the right pointer and once by the left pointer).
     * Space complexity: O(1), because only a constant amount of extra space is used.
     */
    public int longestSnackCartRun(int[] prices, int budget) {
        // The left boundary of our sliding window.
        int left = 0;

        // This will store the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We use long for safety, even though int would still fit under the given constraints.
        // Using long is a good habit when summing many integers.
        long currentSum = 0;

        // Move the right boundary of the window from left to right across the array.
        for (int right = 0; right < prices.length; right++) {
            // Step 1: Include the new element at index 'right' into the current window.
            currentSum += prices[right];

            // Step 2: If the window sum is too large, shrink the window from the left side.
            // Since all values are positive, removing elements from the left will reduce the sum.
            while (currentSum > budget && left <= right) {
                currentSum -= prices[left];
                left++;
            }

            // Step 3: At this point, the window [left...right] is valid
            // because currentSum <= budget.
            // So we compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the answer if this valid window is the largest seen so far.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // If no valid single element existed, maxLength remains 0.
        return maxLength;
    }

    /**
     * Helper method to print an example run in a beginner-friendly way.
     *
     * @param prices the array of cart prices
     * @param budget the maximum allowed total cost
     * @return the computed maximum valid contiguous segment length
     *
     * Time complexity: O(n), because it calls the sliding window solution once.
     * Space complexity: O(1), excluding the space used by printing.
     */
    public int demonstrateExample(int[] prices, int budget) {
        int result = longestSnackCartRun(prices, budget);
        System.out.println("Prices: " + Arrays.toString(prices));
        System.out.println("Budget: " + budget);
        System.out.println("Longest affordable contiguous run length: " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution using the sample inputs from the problem statement.
     *
     * It prints the results so the program is fully runnable and easy to verify.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated test case.
     * Space complexity: O(1), excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // prices = [2, 1, 3, 2, 1], budget = 6
        // Expected output: 3
        int[] prices1 = {2, 1, 3, 2, 1};
        int budget1 = 6;
        int result1 = solution.demonstrateExample(prices1, budget1);
        System.out.println("Expected: 3, Actual: " + result1);
        System.out.println("Matches expected? " + (result1 == 3));
        System.out.println();

        // Example 2:
        // prices = [5, 2, 2, 1, 4], budget = 5
        // Expected output: 2
        int[] prices2 = {5, 2, 2, 1, 4};
        int budget2 = 5;
        int result2 = solution.demonstrateExample(prices2, budget2);
        System.out.println("Expected: 2, Actual: " + result2);
        System.out.println("Matches expected? " + (result2 == 2));
        System.out.println();

        // Additional edge case:
        // Even a single cart is too expensive.
        int[] prices3 = {7, 8, 9};
        int budget3 = 5;
        int result3 = solution.demonstrateExample(prices3, budget3);
        System.out.println("Expected: 0, Actual: " + result3);
        System.out.println("Matches expected? " + (result3 == 0));
    }
}