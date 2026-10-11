import java.util.*;

/*
Title: Minimum Cost to Build a Meal Plan with Reuse Discounts
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
You are planning meals for the next several days. For each day i, you must choose exactly one dish from a list of available dishes for that day. Choosing dish j on day i has a base cost costs[i][j]. However, if you choose the same dish on consecutive days, you receive a reuse discount because ingredients can be reused. Specifically, if dish j is chosen on both day i - 1 and day i, then the cost paid for day i becomes costs[i][j] - discount[j]. The discounted cost will never be negative.

Your task is to compute the minimum total cost needed to build a valid meal plan across all days.

A meal plan is valid if exactly one dish is selected for every day. The number of available dishes is the same for every day.

Return the minimum possible total cost.

Constraints:
- 1 <= days == costs.length <= 200
- 1 <= dishes == costs[i].length <= 200
- 0 <= costs[i][j] <= 10^4
- 0 <= discount[j] <= 10^4
- For every i, costs[i].length == discount.length
- It is guaranteed that costs[i][j] - discount[j] >= 0 whenever the discount is applied

Examples:
1)
Input: costs = [[5,8,6],[4,7,3],[6,2,5]], discount = [2,3,1]
A correct minimum is:
- Day 0: dish 0 => 5
- Day 1: dish 2 => 3
- Day 2: dish 1 => 2
Total = 10

2)
Input: costs = [[3,9],[8,1],[2,7],[6,4]], discount = [5,0]
One optimal plan is:
- Day 0: dish 0 => 3
- Day 1: dish 1 => 1
- Day 2: dish 0 => 2
- Day 3: dish 1 => 4
Total = 10
*/

public class Solution {

    /**
     * Computes the minimum total cost to build a valid meal plan.
     *
     * Dynamic programming idea:
     * Let dp[j] represent the minimum total cost up to the previous day
     * if dish j was chosen on that previous day.
     *
     * For the current day and current dish j, there are exactly two meaningful ways
     * to arrive at this state:
     * 1. Continue using the same dish j as yesterday:
     *    previous cost = dp[j]
     *    today's added cost = costs[day][j] - discount[j]
     *
     * 2. Switch from some different dish k != j:
     *    previous cost = min(dp[k]) over all k != j
     *    today's added cost = costs[day][j]
     *
     * To make switching efficient, for each day we track:
     * - the smallest value in dp
     * - the index where that smallest value occurs
     * - the second smallest value in dp
     *
     * Then for dish j:
     * - if j is not the index of the smallest previous value, the best switch cost uses the smallest
     * - otherwise, the best switch cost uses the second smallest
     *
     * This reduces the transition from O(dishes^2) per day to O(dishes) per day.
     *
     * @param costs a matrix where costs[i][j] is the base cost of choosing dish j on day i
     * @param discount an array where discount[j] is the reuse discount for dish j on consecutive days
     * @return the minimum possible total cost across all days
     * Time complexity: O(days * dishes)
     * Space complexity: O(dishes)
     */
    public long minimumMealPlanCost(int[][] costs, int[] discount) {
        validateInput(costs, discount);

        int days = costs.length;
        int dishes = costs[0].length;

        // dp[j] = minimum total cost up to the previous day,
        //         with dish j chosen on that previous day.
        long[] dp = new long[dishes];

        // Base case:
        // On day 0 there is no previous day, so no discount can be applied.
        // Therefore, the cost of ending day 0 with dish j is simply costs[0][j].
        for (int j = 0; j < dishes; j++) {
            dp[j] = costs[0][j];
        }

        // Process each subsequent day.
        for (int day = 1; day < days; day++) {
            // We will build the DP values for the current day here.
            long[] next = new long[dishes];

            // Find the smallest and second smallest values in dp.
            // This is the key optimization that lets us efficiently compute
            // "best previous cost among all dishes except j".
            long min1 = Long.MAX_VALUE;
            long min2 = Long.MAX_VALUE;
            int min1Index = -1;

            for (int j = 0; j < dishes; j++) {
                long value = dp[j];

                if (value < min1) {
                    // Current value becomes the new smallest.
                    // The old smallest becomes the second smallest.
                    min2 = min1;
                    min1 = value;
                    min1Index = j;
                } else if (value < min2) {
                    // Current value is not the smallest, but it is the second smallest.
                    min2 = value;
                }
            }

            // Compute the best cost for each dish on this day.
            for (int j = 0; j < dishes; j++) {
                // Option 1: continue with the same dish as yesterday.
                // Because the dish is reused on consecutive days, we apply the discount.
                long continueSameDish = dp[j] + (long) costs[day][j] - discount[j];

                // Option 2: switch from a different dish.
                // If the globally smallest previous cost came from a different dish,
                // we can use it directly.
                // Otherwise, we must use the second smallest previous cost,
                // because switching requires the previous dish to be different.
                long bestPreviousDifferentDish;
                if (j != min1Index) {
                    bestPreviousDifferentDish = min1;
                } else {
                    bestPreviousDifferentDish = min2;
                }

                long switchDish = Long.MAX_VALUE;
                if (bestPreviousDifferentDish != Long.MAX_VALUE) {
                    switchDish = bestPreviousDifferentDish + costs[day][j];
                }

                // The best way to end today with dish j is the cheaper of:
                // - continuing the same dish with discount
                // - switching from a different dish without discount
                next[j] = Math.min(continueSameDish, switchDish);
            }

            // Move to the next day.
            dp = next;
        }

        // The answer is the minimum total cost among all possible dishes chosen on the last day.
        long answer = Long.MAX_VALUE;
        for (long value : dp) {
            answer = Math.min(answer, value);
        }

        return answer;
    }

    /**
     * Validates the input according to the problem requirements.
     *
     * @param costs the day-by-dish cost matrix
     * @param discount the reuse discount per dish
     * @return nothing; throws an exception if the input is invalid
     * Time complexity: O(days)
     * Space complexity: O(1)
     */
    public void validateInput(int[][] costs, int[] discount) {
        if (costs == null || costs.length == 0) {
            throw new IllegalArgumentException("costs must contain at least one day.");
        }
        if (discount == null || discount.length == 0) {
            throw new IllegalArgumentException("discount must contain at least one dish.");
        }

        int dishes = discount.length;

        for (int i = 0; i < costs.length; i++) {
            if (costs[i] == null || costs[i].length != dishes) {
                throw new IllegalArgumentException("Each row in costs must have the same length as discount.");
            }
        }
    }

    /**
     * Utility method to print a 2D integer matrix in a readable form.
     *
     * @param matrix the matrix to print
     * @return a string representation of the matrix
     * Time complexity: O(rows * cols)
     * Space complexity: O(rows * cols) due to string construction
     */
    public String matrixToString(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < matrix.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(Arrays.toString(matrix[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution on sample test cases from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo inputs, excluding the algorithm calls
     * Space complexity: O(1), excluding the algorithm calls
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] costs1 = {
            {5, 8, 6},
            {4, 7, 3},
            {6, 2, 5}
        };
        int[] discount1 = {2, 3, 1};

        long result1 = solution.minimumMealPlanCost(costs1, discount1);
        System.out.println("Example 1");
        System.out.println("costs = " + solution.matrixToString(costs1));
        System.out.println("discount = " + Arrays.toString(discount1));
        System.out.println("Minimum total cost = " + result1);
        System.out.println("Expected = 10");
        System.out.println();

        int[][] costs2 = {
            {3, 9},
            {8, 1},
            {2, 7},
            {6, 4}
        };
        int[] discount2 = {5, 0};

        long result2 = solution.minimumMealPlanCost(costs2, discount2);
        System.out.println("Example 2");
        System.out.println("costs = " + solution.matrixToString(costs2));
        System.out.println("discount = " + Arrays.toString(discount2));
        System.out.println("Minimum total cost = " + result2);
        System.out.println("Expected = 10");
        System.out.println();

        int[][] costs3 = {
            {7}
        };
        int[] discount3 = {4};

        long result3 = solution.minimumMealPlanCost(costs3, discount3);
        System.out.println("Single day / single dish test");
        System.out.println("costs = " + solution.matrixToString(costs3));
        System.out.println("discount = " + Arrays.toString(discount3));
        System.out.println("Minimum total cost = " + result3);
        System.out.println("Expected = 7");
        System.out.println();

        int[][] costs4 = {
            {5, 100},
            {5, 100},
            {5, 100}
        };
        int[] discount4 = {5, 0};

        long result4 = solution.minimumMealPlanCost(costs4, discount4);
        System.out.println("Reuse discount stress test");
        System.out.println("costs = " + solution.matrixToString(costs4));
        System.out.println("discount = " + Arrays.toString(discount4));
        System.out.println("Minimum total cost = " + result4);
        System.out.println("Expected = 5");
    }
}