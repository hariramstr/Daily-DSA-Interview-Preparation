import java.util.*;

/*
Problem Title: Maximum Tip Total from Choosing Non-Consecutive Tables

Problem Description:
A restaurant manager is planning which tables to assign to a single premium server during a busy evening.
The dining room has tables arranged in a straight line, and table i would generate tips[i] dollars if the
server handles it. However, to avoid delays, the server cannot be assigned two adjacent tables, because
neighboring tables tend to place orders at nearly the same time.

Your task is to return the maximum total tip amount the server can earn by choosing a subset of tables
such that no two chosen tables are adjacent.

You must decide for each table whether to skip it or assign it, while respecting the non-adjacent rule.
This is an optimization problem where a simple greedy choice does not always work, so a dynamic programming
approach is expected.

Constraints:
- 1 <= tips.length <= 100
- 0 <= tips[i] <= 1000
- The answer fits in a 32-bit signed integer

Example 1:
Input: tips = [5, 1, 8, 4, 7]
Output: 20
Explanation: Choose tables with tips 5, 8, and 7. These are at indices 0, 2, and 4, so no two chosen
tables are adjacent. The total is 5 + 8 + 7 = 20.

Example 2:
Input: tips = [10, 3, 2, 9]
Output: 19
Explanation: The best choice is tables with tips 10 and 9. Choosing 3 and 9 gives only 12, and choosing
10 and 2 gives only 12. So the maximum total tip is 19.
*/

public class Solution {

    /**
     * Computes the maximum total tip that can be earned by choosing non-adjacent tables.
     *
     * Dynamic Programming Idea:
     * For each table index i, we have two choices:
     * 1. Skip table i:
     *    Then the best total remains whatever was best up to table i - 1.
     * 2. Take table i:
     *    Then we cannot take table i - 1, so the best total becomes tips[i] + best up to table i - 2.
     *
     * Therefore:
     * dp[i] = max(dp[i - 1], dp[i - 2] + tips[i])
     *
     * Base cases:
     * - dp[0] = tips[0]
     * - dp[1] = max(tips[0], tips[1])
     *
     * @param tips an array where tips[i] is the tip amount from table i
     * @return the maximum total tip possible without choosing adjacent tables
     * Time complexity: O(n), where n is the number of tables
     * Space complexity: O(n), due to the DP array
     */
    public int maxTipTotal(int[] tips) {
        // Defensive check:
        // The problem guarantees at least one element, but checking null or empty
        // makes the method safer and more beginner-friendly.
        if (tips == null || tips.length == 0) {
            return 0;
        }

        // If there is only one table, the answer is simply its tip value.
        if (tips.length == 1) {
            return tips[0];
        }

        // Create a DP array where:
        // dp[i] = maximum tip total we can earn considering tables from index 0 to i
        int[] dp = new int[tips.length];

        // Base case for the first table:
        // If we only consider table 0, the best we can do is take it.
        dp[0] = tips[0];

        // Base case for the second table:
        // We cannot take both table 0 and table 1 because they are adjacent.
        // So we choose the better of the two.
        dp[1] = Math.max(tips[0], tips[1]);

        // Fill the DP array from left to right.
        for (int i = 2; i < tips.length; i++) {
            // Option 1: Skip the current table i.
            // Then our best total is exactly the same as the best total up to i - 1.
            int skipCurrent = dp[i - 1];

            // Option 2: Take the current table i.
            // If we take it, we must skip table i - 1.
            // So we add tips[i] to the best total up to i - 2.
            int takeCurrent = dp[i - 2] + tips[i];

            // Choose whichever option gives a larger total.
            dp[i] = Math.max(skipCurrent, takeCurrent);
        }

        // The last entry contains the answer for the entire array.
        return dp[tips.length - 1];
    }

    /**
     * Computes the maximum total tip using an optimized dynamic programming approach
     * that stores only the last two DP states instead of the full array.
     *
     * This works because each state depends only on:
     * - the previous state (i - 1)
     * - the state before that (i - 2)
     *
     * @param tips an array where tips[i] is the tip amount from table i
     * @return the maximum total tip possible without choosing adjacent tables
     * Time complexity: O(n), where n is the number of tables
     * Space complexity: O(1), because only a constant amount of extra space is used
     */
    public int maxTipTotalOptimized(int[] tips) {
        // Handle edge cases first.
        if (tips == null || tips.length == 0) {
            return 0;
        }

        if (tips.length == 1) {
            return tips[0];
        }

        // prevTwo represents dp[i - 2]
        int prevTwo = tips[0];

        // prevOne represents dp[i - 1]
        int prevOne = Math.max(tips[0], tips[1]);

        // Process each remaining table starting from index 2.
        for (int i = 2; i < tips.length; i++) {
            // If we skip the current table, total remains prevOne.
            int skipCurrent = prevOne;

            // If we take the current table, total becomes prevTwo + tips[i].
            int takeCurrent = prevTwo + tips[i];

            // Current best result for index i.
            int current = Math.max(skipCurrent, takeCurrent);

            // Shift the window forward:
            // old prevOne becomes new prevTwo,
            // current becomes new prevOne.
            prevTwo = prevOne;
            prevOne = current;
        }

        return prevOne;
    }

    /**
     * Prints an integer array in a readable format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * Verified examples:
     * - [5, 1, 8, 4, 7] -> 20
     * - [10, 3, 2, 9] -> 19
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo inputs, excluding the called methods
     * Space complexity: O(1), excluding the called methods
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1 from the problem statement
        int[] tips1 = {5, 1, 8, 4, 7};
        int result1 = solution.maxTipTotal(tips1);

        System.out.println("Input: tips = " + solution.arrayToString(tips1));
        System.out.println("Maximum total tip: " + result1);
        System.out.println("Expected: 20");
        System.out.println();

        // Sample input 2 from the problem statement
        int[] tips2 = {10, 3, 2, 9};
        int result2 = solution.maxTipTotal(tips2);

        System.out.println("Input: tips = " + solution.arrayToString(tips2));
        System.out.println("Maximum total tip: " + result2);
        System.out.println("Expected: 19");
        System.out.println();

        // Also demonstrate the optimized version on the same inputs
        int optimizedResult1 = solution.maxTipTotalOptimized(tips1);
        int optimizedResult2 = solution.maxTipTotalOptimized(tips2);

        System.out.println("Optimized DP Result for " + solution.arrayToString(tips1) + ": " + optimizedResult1);
        System.out.println("Optimized DP Result for " + solution.arrayToString(tips2) + ": " + optimizedResult2);
    }
}