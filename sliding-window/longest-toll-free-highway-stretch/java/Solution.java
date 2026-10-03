import java.util.*;

/*
 * Title: Longest Toll-Free Highway Stretch
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array costs where costs[i] is the toll fee charged at the i-th
 * highway checkpoint on a road trip. A traveler wants to drive through one contiguous
 * stretch of checkpoints while spending at most budget total toll money.
 *
 * Return the length of the longest contiguous subarray of costs whose sum is less than
 * or equal to budget.
 *
 * This models a common interview scenario where you must find the longest valid window
 * under a running sum constraint. Because all toll costs are non-negative, you can
 * expand the right end of the window and shrink the left end whenever the total exceeds
 * the allowed budget.
 *
 * Constraints:
 * - 1 <= costs.length <= 100000
 * - 0 <= costs[i] <= 10000
 * - 0 <= budget <= 1000000000
 * - The answer is the number of checkpoints in the longest valid contiguous stretch.
 *
 * Example 1:
 * Input: costs = [4, 2, 1, 3, 2], budget = 6
 * Output: 3
 * Explanation: The longest valid stretch is [2, 1, 3] with total cost 6.
 * No contiguous stretch of length 4 stays within budget.
 *
 * Example 2:
 * Input: costs = [1, 1, 1, 1, 1], budget = 3
 * Output: 3
 * Explanation: Any 3 consecutive checkpoints cost 3, which fits the budget.
 * Any 4 consecutive checkpoints cost 4, which is too expensive.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray whose sum is less than
     * or equal to the given budget.
     *
     * This method uses the classic sliding window technique:
     * - Expand the window by moving the right pointer.
     * - Keep track of the running sum of the current window.
     * - If the sum becomes too large, shrink the window from the left until the
     *   sum is valid again.
     * - Record the maximum valid window length seen so far.
     *
     * Because all values are non-negative, once the sum exceeds the budget,
     * moving the left pointer forward is the correct way to reduce the sum.
     *
     * @param costs the array of non-negative toll costs at each checkpoint
     * @param budget the maximum total toll allowed for a valid contiguous stretch
     * @return the maximum number of consecutive checkpoints whose total cost is at most budget
     *
     * Time complexity: O(n), where n is the length of the costs array,
     * because each element is added to the window once and removed at most once.
     * Space complexity: O(1), because only a few extra variables are used.
     */
    public int longestTollFreeStretch(int[] costs, int budget) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Running sum of the current window costs[left...right].
        // We use long for extra safety, even though int would also fit under constraints.
        long currentSum = 0;

        // Move the right boundary one step at a time across the array.
        for (int right = 0; right < costs.length; right++) {
            // Step 1: Include the new element at index 'right' into the window.
            currentSum += costs[right];

            // Step 2: If the window sum is too large, it is invalid.
            // We must shrink from the left until the sum becomes <= budget again.
            //
            // This works because all costs are non-negative:
            // removing elements from the left can only keep the sum the same or reduce it.
            while (currentSum > budget) {
                currentSum -= costs[left];
                left++;
            }

            // Step 3: At this point, the window [left...right] is valid.
            // Compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the answer if this valid window is the longest so far.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // After scanning the whole array, maxLength is the answer.
        return maxLength;
    }

    /**
     * A helper method that prints the input and the computed result in a friendly format.
     *
     * @param costs the array of toll costs to test
     * @param budget the maximum allowed total toll
     * @return the computed longest valid stretch length
     *
     * Time complexity: O(n), because it calls the sliding window method once.
     * Space complexity: O(1), excluding the space used by array-to-string conversion for printing.
     */
    public int demonstrateCase(int[] costs, int budget) {
        int result = longestTollFreeStretch(costs, budget);
        System.out.println("Costs  : " + Arrays.toString(costs));
        System.out.println("Budget : " + budget);
        System.out.println("Result : " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution on sample inputs from the problem statement.
     *
     * It verifies the examples:
     * - Example 1 should produce 3
     * - Example 2 should produce 3
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
        // costs = [4, 2, 1, 3, 2], budget = 6
        // Valid longest window is [2, 1, 3] with sum 6 and length 3.
        int[] costs1 = {4, 2, 1, 3, 2};
        int budget1 = 6;
        int result1 = solution.demonstrateCase(costs1, budget1);
        System.out.println("Expected: 3, Actual: " + result1);
        System.out.println();

        // Example 2:
        // costs = [1, 1, 1, 1, 1], budget = 3
        // Any 3 consecutive elements sum to 3, so the answer is 3.
        int[] costs2 = {1, 1, 1, 1, 1};
        int budget2 = 3;
        int result2 = solution.demonstrateCase(costs2, budget2);
        System.out.println("Expected: 3, Actual: " + result2);
        System.out.println();

        // Additional beginner-friendly checks.

        // Entire array fits within budget.
        int[] costs3 = {2, 2, 2};
        int budget3 = 10;
        int result3 = solution.demonstrateCase(costs3, budget3);
        System.out.println("Expected: 3, Actual: " + result3);
        System.out.println();

        // Budget is zero, only zero-cost checkpoints can be included.
        int[] costs4 = {0, 0, 1, 0, 0};
        int budget4 = 0;
        int result4 = solution.demonstrateCase(costs4, budget4);
        System.out.println("Expected: 2, Actual: " + result4);
        System.out.println();

        // No positive-cost checkpoint fits if budget is too small.
        int[] costs5 = {5, 6, 7};
        int budget5 = 4;
        int result5 = solution.demonstrateCase(costs5, budget5);
        System.out.println("Expected: 0, Actual: " + result5);
    }
}