import java.util.*;

/*
 * Title: Longest Call Streak Within Roaming Budget
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array costs where costs[i] represents the roaming charge for the i-th phone call
 * made during a trip. A traveler wants to look at one continuous streak of calls and keep the total
 * roaming charge of that streak within a fixed budget. Your task is to return the length of the
 * longest contiguous subarray whose sum is less than or equal to budget.
 *
 * In other words, choose indices l and r such that 0 <= l <= r < costs.length, and the sum of
 * costs[l] through costs[r] does not exceed budget. Among all such valid choices, find the maximum
 * possible number of calls in the streak.
 *
 * This is an interview-style sliding window problem: because all roaming charges are non-negative,
 * you can expand the right end of the window and shrink the left end whenever the total exceeds
 * the budget.
 *
 * Constraints:
 * - 1 <= costs.length <= 100000
 * - 0 <= costs[i] <= 10000
 * - 0 <= budget <= 1000000000
 * - All values are integers
 *
 * Example 1:
 * Input: costs = [4, 2, 1, 3, 2], budget = 6
 * Output: 3
 * Explanation: The longest valid streak is [2, 1, 3], which has total cost 6 and length 3.
 *
 * Example 2:
 * Input: costs = [7, 1, 2, 1, 1], budget = 4
 * Output: 3
 * Explanation: Although [1, 2, 1, 1] has length 4, its total cost is 5, so it is invalid.
 * Valid longest streaks include [1, 2, 1] and [2, 1, 1], both with total cost 4 and length 3.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray whose sum is less than or equal to
     * the given budget.
     *
     * This method uses the classic sliding window technique:
     * 1. Expand the window by moving the right pointer.
     * 2. Add the new value into the running sum.
     * 3. If the sum becomes too large, shrink the window from the left until it becomes valid again.
     * 4. Track the maximum valid window length seen so far.
     *
     * This works because all costs are non-negative. That property guarantees that:
     * - Expanding the window can only increase or keep the sum the same.
     * - Shrinking the window can only decrease or keep the sum the same.
     *
     * @param costs the array of non-negative roaming charges for each call
     * @param budget the maximum allowed total cost for a contiguous streak of calls
     * @return the maximum length of a contiguous subarray whose sum is at most budget
     *
     * Time complexity: O(n), because each element is added to the window once and removed at most once.
     * Space complexity: O(1), because only a few variables are used regardless of input size.
     */
    public int longestCallStreakWithinBudget(int[] costs, int budget) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This will store the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We use long for safety, even though int would still fit under the given constraints.
        // Using long is a good habit when summing many integers.
        long currentSum = 0;

        // Move the right boundary of the window from left to right across the array.
        for (int right = 0; right < costs.length; right++) {
            // Step 1: include the current rightmost element in the window.
            currentSum += costs[right];

            // Step 2: if the window sum is too large, shrink from the left side.
            // We keep removing elements from the left until the window becomes valid again.
            while (currentSum > budget && left <= right) {
                currentSum -= costs[left];
                left++;
            }

            // Step 3: at this point, the window [left..right] is valid
            // because currentSum <= budget.
            int currentLength = right - left + 1;

            // Step 4: update the best answer if this valid window is longer.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // Return the length of the longest valid contiguous subarray.
        return maxLength;
    }

    /**
     * Helper method to print an integer array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n), due to string construction.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * It also includes the expected outputs so that the behavior can be visually verified.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call to the algorithm.
     * Space complexity: O(1) extra space for the algorithm itself.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] costs1 = {4, 2, 1, 3, 2};
        int budget1 = 6;
        int result1 = solution.longestCallStreakWithinBudget(costs1, budget1);

        System.out.println("Example 1");
        System.out.println("Costs: " + solution.arrayToString(costs1));
        System.out.println("Budget: " + budget1);
        System.out.println("Expected Output: 3");
        System.out.println("Actual Output: " + result1);
        System.out.println();

        // Sample 2
        int[] costs2 = {7, 1, 2, 1, 1};
        int budget2 = 4;
        int result2 = solution.longestCallStreakWithinBudget(costs2, budget2);

        System.out.println("Example 2");
        System.out.println("Costs: " + solution.arrayToString(costs2));
        System.out.println("Budget: " + budget2);
        System.out.println("Expected Output: 3");
        System.out.println("Actual Output: " + result2);
        System.out.println();

        // Additional quick checks for clarity
        int[] costs3 = {0, 0, 0, 0};
        int budget3 = 0;
        int result3 = solution.longestCallStreakWithinBudget(costs3, budget3);

        System.out.println("Additional Example 3");
        System.out.println("Costs: " + solution.arrayToString(costs3));
        System.out.println("Budget: " + budget3);
        System.out.println("Expected Output: 4");
        System.out.println("Actual Output: " + result3);
        System.out.println();

        int[] costs4 = {10, 20, 30};
        int budget4 = 5;
        int result4 = solution.longestCallStreakWithinBudget(costs4, budget4);

        System.out.println("Additional Example 4");
        System.out.println("Costs: " + solution.arrayToString(costs4));
        System.out.println("Budget: " + budget4);
        System.out.println("Expected Output: 0");
        System.out.println("Actual Output: " + result4);
    }
}