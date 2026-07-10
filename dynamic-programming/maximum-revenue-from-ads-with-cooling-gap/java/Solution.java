import java.util.*;

/*
 * Title: Maximum Revenue from Ads with Cooling Gap
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A video platform has a list of ad opportunities along a timeline. You are given an integer array
 * revenue where revenue[i] is the amount earned if you place an ad in slot i. However, to avoid
 * showing ads too close together, after choosing any slot i, you must skip the next gap slots.
 * In other words, if you place an ad at slot i, the next ad can only be placed at slot j where
 * j > i + gap.
 *
 * Your task is to compute the maximum total revenue that can be earned by selecting a subset of
 * slots that satisfies this cooling-gap rule.
 *
 * Return the maximum possible revenue.
 *
 * This is a one-dimensional optimization problem. A slot may be skipped even if it has positive
 * revenue, and some revenues may be 0. You should design an algorithm efficient enough for large inputs.
 *
 * Constraints:
 * - 1 <= revenue.length <= 200000
 * - 0 <= revenue[i] <= 1000000000
 * - 0 <= gap < revenue.length
 *
 * Example 1:
 * Input: revenue = [5, 1, 2, 10, 6, 2], gap = 1
 * Output: 17
 * Explanation: Choose slots 0, 3, and 5 for total revenue 5 + 10 + 2 = 17.
 *
 * Example 2:
 * Input: revenue = [4, 7, 3, 9, 2, 8], gap = 2
 * Correct Output: 15
 * Explanation: The best valid choice is slots 1 and 5, giving 7 + 8 = 15.
 * The statement text contains a correction note; the mathematically optimal answer is 15.
 */

public class Solution {

    /**
     * Computes the maximum total revenue using dynamic programming.
     *
     * Core idea:
     * For each slot i, we decide between:
     * 1) Skipping slot i, so the best answer remains the same as for slots [0..i-1]
     * 2) Taking slot i, which earns revenue[i], and then the previous slot we are allowed
     *    to combine with must be at index i - gap - 1 or earlier
     *
     * Therefore:
     * dp[i] = max(
     *     dp[i - 1],
     *     revenue[i] + dp[i - gap - 1]
     * )
     *
     * where any dp index below 0 is treated as 0.
     *
     * We use long because:
     * - revenue[i] can be up to 1,000,000,000
     * - length can be up to 200,000
     * - total sum can exceed int range
     *
     * @param revenue the revenue available at each ad slot
     * @param gap the number of slots that must be skipped after choosing one slot
     * @return the maximum total revenue achievable under the cooling-gap rule
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long maxRevenue(int[] revenue, int gap) {
        validateInput(revenue, gap);

        int n = revenue.length;

        // dp[i] will store the maximum revenue we can earn considering slots from 0 to i inclusive.
        long[] dp = new long[n];

        // We process slots from left to right.
        for (int i = 0; i < n; i++) {
            // Option 1: skip the current slot.
            // If we skip slot i, then the best revenue is simply whatever best revenue
            // we had up to slot i - 1.
            long skipCurrent = (i > 0) ? dp[i - 1] : 0L;

            // Option 2: take the current slot.
            // If we take slot i, we earn revenue[i].
            long takeCurrent = revenue[i];

            // Because of the cooling gap, after taking slot i, the previous chosen slot
            // must be at index <= i - gap - 1.
            int previousAllowedIndex = i - gap - 1;

            // If such an index exists, we can add the best revenue achievable up to that index.
            if (previousAllowedIndex >= 0) {
                takeCurrent += dp[previousAllowedIndex];
            }

            // The best answer up to i is the better of:
            // - skipping slot i
            // - taking slot i
            dp[i] = Math.max(skipCurrent, takeCurrent);
        }

        return dp[n - 1];
    }

    /**
     * Computes the maximum total revenue using a space-optimized approach.
     *
     * This method still runs in O(n) time, but instead of storing the entire dp array,
     * it stores values in a circular buffer of size gap + 2.
     *
     * Why gap + 2?
     * At position i, we need:
     * - dp[i - 1]
     * - dp[i - gap - 1]
     * The distance between these indices is gap, so a circular buffer of size gap + 2
     * is enough to safely retain the needed values while overwriting old ones.
     *
     * This is useful when memory optimization is desired.
     *
     * @param revenue the revenue available at each ad slot
     * @param gap the number of slots that must be skipped after choosing one slot
     * @return the maximum total revenue achievable under the cooling-gap rule
     * Time complexity: O(n)
     * Space complexity: O(gap + 2)
     */
    public long maxRevenueOptimized(int[] revenue, int gap) {
        validateInput(revenue, gap);

        int n = revenue.length;
        int bufferSize = gap + 2;
        long[] buffer = new long[bufferSize];

        long previousDp = 0L;

        for (int i = 0; i < n; i++) {
            long skipCurrent = previousDp;

            long takeCurrent = revenue[i];
            int previousAllowedIndex = i - gap - 1;

            if (previousAllowedIndex >= 0) {
                takeCurrent += buffer[previousAllowedIndex % bufferSize];
            }

            long currentDp = Math.max(skipCurrent, takeCurrent);

            buffer[i % bufferSize] = currentDp;
            previousDp = currentDp;
        }

        return previousDp;
    }

    /**
     * Validates the input according to the problem constraints.
     *
     * @param revenue the revenue array to validate
     * @param gap the cooling gap to validate
     * @return nothing; throws an exception if input is invalid
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void validateInput(int[] revenue, int gap) {
        if (revenue == null) {
            throw new IllegalArgumentException("revenue array must not be null");
        }
        if (revenue.length == 0) {
            throw new IllegalArgumentException("revenue array must contain at least one element");
        }
        if (gap < 0 || gap >= revenue.length) {
            throw new IllegalArgumentException("gap must satisfy 0 <= gap < revenue.length");
        }
    }

    /**
     * Helper method to print a test case and its computed answer.
     *
     * @param revenue the revenue array for the test
     * @param gap the cooling gap for the test
     * @return nothing
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public void runDemo(int[] revenue, int gap) {
        long answer = maxRevenue(revenue, gap);
        long optimizedAnswer = maxRevenueOptimized(revenue, gap);

        System.out.println("Revenue: " + Arrays.toString(revenue));
        System.out.println("Gap: " + gap);
        System.out.println("Maximum Revenue (DP): " + answer);
        System.out.println("Maximum Revenue (Optimized): " + optimizedAnswer);
        System.out.println();
    }

    /**
     * Demonstrates the solution on sample and additional test cases.
     *
     * Important correctness checks:
     * - Example 1 should produce 17
     * - Example 2 should produce 15 (the problem statement includes a correction note)
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(total input size across demos)
     * Space complexity: O(max input size among demos)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // revenue = [5, 1, 2, 10, 6, 2], gap = 1
        // Valid optimal choice: slots 0, 3, 5 => 5 + 10 + 2 = 17
        int[] revenue1 = {5, 1, 2, 10, 6, 2};
        int gap1 = 1;
        solution.runDemo(revenue1, gap1);

        // Example 2 from the problem statement:
        // revenue = [4, 7, 3, 9, 2, 8], gap = 2
        // Correct optimal answer is 15 by choosing slots 1 and 5 => 7 + 8 = 15
        int[] revenue2 = {4, 7, 3, 9, 2, 8};
        int gap2 = 2;
        solution.runDemo(revenue2, gap2);

        // Additional beginner-friendly test:
        // gap = 0 means we can choose any slots, because after choosing a slot,
        // we skip the next 0 slots. So all positive values can be taken.
        int[] revenue3 = {3, 0, 4, 2};
        int gap3 = 0;
        solution.runDemo(revenue3, gap3);

        // Additional test:
        // Large gap relative to array means we can choose at most one slot in many cases.
        int[] revenue4 = {8, 1, 9, 3, 7};
        int gap4 = 4;
        solution.runDemo(revenue4, gap4);
    }
}