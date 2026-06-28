import java.util.*;

/*
 * Title: Longest Delivery Route Within Fuel Budget
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * A courier company records the fuel cost of each stop along a planned route in an array costs,
 * where costs[i] is the amount of fuel needed to travel through stop i. The driver wants to
 * complete the longest possible consecutive portion of the route without exceeding a total fuel
 * budget.
 *
 * Your task is to return the length of the longest contiguous subarray whose sum is less than or
 * equal to budget.
 *
 * This is a practical sliding window problem because all fuel costs are non-negative. As you
 * expand a window to the right, the total fuel used only increases or stays the same. If the total
 * exceeds the budget, you can shrink the window from the left until the route becomes affordable
 * again.
 *
 * Return 0 if no single stop can be included within the budget.
 *
 * Constraints:
 * - 1 <= costs.length <= 100000
 * - 0 <= costs[i] <= 10000
 * - 0 <= budget <= 1000000000
 *
 * Example 1:
 * Input: costs = [4, 2, 1, 7, 3, 2], budget = 8
 * Output: 3
 * Explanation: The longest valid consecutive route is [4, 2, 1] with total fuel 7.
 *
 * Example 2:
 * Input: costs = [9, 1, 2, 1, 1], budget = 4
 * Output: 4
 * Explanation: The longest valid route is [1, 2, 1, 1], which uses exactly 4 units of fuel.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray whose sum is less than or equal to
     * the given budget.
     *
     * This method uses the classic sliding window technique:
     * - Expand the window by moving the right pointer.
     * - Add the new value into the running sum.
     * - If the sum becomes too large, shrink the window from the left until the sum is valid again.
     * - Track the maximum valid window length seen so far.
     *
     * Because all values are non-negative, once the sum exceeds the budget, moving the left pointer
     * forward is the correct way to reduce the sum. This property is what makes sliding window work.
     *
     * @param costs the array of non-negative fuel costs for each stop
     * @param budget the maximum total fuel allowed for a contiguous route segment
     * @return the maximum length of a contiguous subarray with sum less than or equal to budget
     * Time complexity: O(n), where n is the length of the costs array
     * Space complexity: O(1), ignoring input storage
     */
    public int longestRouteWithinBudget(int[] costs, int budget) {
        // Left boundary of the current sliding window.
        int left = 0;

        // Running sum of the current window [left..right].
        long currentSum = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Move the right boundary one step at a time across the array.
        for (int right = 0; right < costs.length; right++) {
            // Step 1: include costs[right] in the current window.
            currentSum += costs[right];

            // Step 2: if the window is too expensive, shrink it from the left.
            // We keep removing elements from the left until the total cost is affordable again.
            while (currentSum > budget && left <= right) {
                currentSum -= costs[left];
                left++;
            }

            // Step 3: after shrinking, the window [left..right] is valid
            // (or empty in edge cases, though here it will be valid for measurement).
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
     * Helper method that prints an input array in a readable format and displays the computed result.
     *
     * @param costs the array of fuel costs
     * @param budget the maximum allowed total fuel
     * @return the computed longest valid route length
     * Time complexity: O(n), due to the call to the main algorithm
     * Space complexity: O(1), ignoring the input array
     */
    public int demonstrateCase(int[] costs, int budget) {
        int result = longestRouteWithinBudget(costs, budget);
        System.out.println("costs = " + Arrays.toString(costs));
        System.out.println("budget = " + budget);
        System.out.println("Longest valid route length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     *
     * Demonstrates the algorithm on the sample inputs from the problem statement and a few
     * additional edge cases for clarity.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case
     * Space complexity: O(1), ignoring input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1:
        // costs = [4, 2, 1, 7, 3, 2], budget = 8
        // Expected output: 3
        solution.demonstrateCase(new int[]{4, 2, 1, 7, 3, 2}, 8);

        // Sample 2:
        // costs = [9, 1, 2, 1, 1], budget = 4
        // Expected output: 4
        solution.demonstrateCase(new int[]{9, 1, 2, 1, 1}, 4);

        // Additional edge case:
        // No single stop fits within the budget.
        // Expected output: 0
        solution.demonstrateCase(new int[]{5, 6, 7}, 4);

        // Additional edge case:
        // Budget is 0, but zero-cost stops can still be included.
        // Expected output: 3
        solution.demonstrateCase(new int[]{0, 0, 0, 5}, 0);
    }
}