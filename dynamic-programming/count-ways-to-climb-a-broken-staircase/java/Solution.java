import java.util.*;

/*
 * Title: Count Ways to Climb a Broken Staircase
 * Difficulty: Easy
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given a staircase with n steps, numbered from 1 to n. A person starts
 * on the ground at step 0 and wants to reach exactly step n. On each move, they
 * may climb either 1 step or 2 steps. However, some steps are broken and cannot
 * be landed on. You are given an array broken, containing the step numbers that
 * are broken.
 *
 * Return the number of distinct ways to reach step n without ever landing on a
 * broken step. Since the answer can become large, return it modulo 1,000,000,007.
 *
 * Two ways are considered different if the sequence of jumps is different.
 * For example, jumping 1 then 2 is different from jumping 2 then 1.
 *
 * This is a dynamic programming problem because the number of ways to reach a
 * step depends on the number of ways to reach previous steps. If a step is broken,
 * its number of ways is 0.
 *
 * Constraints:
 * - 1 <= n <= 100000
 * - 0 <= broken.length <= n
 * - 1 <= broken[i] <= n
 * - All values in broken are distinct
 *
 * Example 1:
 * Input: n = 5, broken = [2]
 * Output: 2
 * Explanation:
 * The valid ways are:
 * - 1 -> 1 -> 1 -> 1 -> 1
 * - 1 -> 2 -> 2
 *
 * Example 2:
 * Input: n = 6, broken = [3, 5]
 * Output: 1
 * Explanation:
 * There is only one valid way:
 * - 2 -> 2 -> 2
 */

public class Solution {

    /**
     * Constant used for modulo arithmetic so the answer stays within integer range.
     */
    private static final int MOD = 1_000_000_007;

    /**
     * Computes the number of distinct valid ways to reach step n while avoiding broken steps.
     *
     * Dynamic Programming Idea:
     * - Let dp[i] represent the number of valid ways to land exactly on step i.
     * - If step i is broken, then dp[i] = 0 because we are not allowed to land there.
     * - Otherwise, we can reach step i from:
     *   - step i - 1 by taking a 1-step jump
     *   - step i - 2 by taking a 2-step jump
     * - Therefore:
     *   dp[i] = dp[i - 1] + dp[i - 2], if step i is not broken
     *
     * Base Case:
     * - dp[0] = 1
     *   There is exactly one way to be at the ground before climbing: do nothing.
     *
     * Example trace for n = 5, broken = [2]:
     * - dp[0] = 1
     * - dp[1] = dp[0] = 1
     * - dp[2] = 0 because step 2 is broken
     * - dp[3] = dp[2] + dp[1] = 0 + 1 = 1
     * - dp[4] = dp[3] + dp[2] = 1 + 0 = 1
     * - dp[5] = dp[4] + dp[3] = 1 + 1 = 2
     * Answer = 2
     *
     * Example trace for n = 6, broken = [3, 5]:
     * - dp[0] = 1
     * - dp[1] = 1
     * - dp[2] = dp[1] + dp[0] = 2
     * - dp[3] = 0 because broken
     * - dp[4] = dp[3] + dp[2] = 0 + 2 = 2
     * - dp[5] = 0 because broken
     * - dp[6] = dp[5] + dp[4] = 0 + 2 = 2
     *
     * Note:
     * According to the exact problem statement, the correct answer for Example 2 is 2,
     * because there are two valid jump sequences:
     * - 2 -> 2 -> 2
     * - 1 -> 1 -> 2 -> 2
     * Both avoid landing on steps 3 and 5.
     *
     * @param n the target step to reach
     * @param broken an array containing the broken step numbers
     * @return the number of distinct valid ways to reach step n modulo 1,000,000,007
     * Time complexity: O(n + b), where b is broken.length
     * Space complexity: O(n)
     */
    public int countWays(int n, int[] broken) {
        // Create a boolean array to quickly check whether a step is broken.
        // brokenSteps[i] will be true if step i is broken, otherwise false.
        boolean[] brokenSteps = new boolean[n + 1];

        // Mark all broken steps.
        for (int step : broken) {
            if (step >= 1 && step <= n) {
                brokenSteps[step] = true;
            }
        }

        // dp[i] = number of valid ways to reach step i.
        long[] dp = new long[n + 1];

        // Base case:
        // There is exactly one way to stand at step 0 before making any move.
        dp[0] = 1;

        // Process each step from 1 to n in increasing order.
        for (int i = 1; i <= n; i++) {

            // If the current step is broken, we cannot land on it.
            // Therefore, the number of ways to reach it is 0.
            if (brokenSteps[i]) {
                dp[i] = 0;
                continue;
            }

            // Start with 0 ways, then add contributions from previous reachable steps.
            long ways = 0;

            // If step i - 1 exists, then every valid way to reach i - 1
            // can be extended by a 1-step jump to reach i.
            if (i - 1 >= 0) {
                ways += dp[i - 1];
            }

            // If step i - 2 exists, then every valid way to reach i - 2
            // can be extended by a 2-step jump to reach i.
            if (i - 2 >= 0) {
                ways += dp[i - 2];
            }

            // Apply modulo to keep the value within bounds.
            dp[i] = ways % MOD;
        }

        // The answer is the number of valid ways to reach exactly step n.
        return (int) dp[n];
    }

    /**
     * Computes the number of distinct valid ways to reach step n while avoiding broken steps,
     * using an optimized dynamic programming approach with constant extra DP space.
     *
     * This method is functionally equivalent to countWays(...), but instead of storing the
     * entire dp array, it only keeps track of:
     * - ways to reach step i - 2
     * - ways to reach step i - 1
     * because those are the only values needed to compute step i.
     *
     * @param n the target step to reach
     * @param broken an array containing the broken step numbers
     * @return the number of distinct valid ways to reach step n modulo 1,000,000,007
     * Time complexity: O(n + b), where b is broken.length
     * Space complexity: O(n) due to broken-step tracking, and O(1) extra DP space
     */
    public int countWaysOptimized(int n, int[] broken) {
        // Track which steps are broken for O(1) lookup.
        boolean[] brokenSteps = new boolean[n + 1];
        for (int step : broken) {
            if (step >= 1 && step <= n) {
                brokenSteps[step] = true;
            }
        }

        // prev2 represents dp[i - 2]
        // prev1 represents dp[i - 1]
        long prev2 = 1; // dp[0] = 1

        // Handle n >= 1 carefully by building from step 1 onward.
        long prev1;
        if (n >= 1) {
            prev1 = brokenSteps[1] ? 0 : 1;
        } else {
            prev1 = prev2;
        }

        // Special case: if n == 0 (not needed by constraints, but safe), return dp[0].
        if (n == 0) {
            return 1;
        }

        // If n == 1, we already computed the answer.
        if (n == 1) {
            return (int) prev1;
        }

        // Compute dp[i] for i from 2 to n.
        for (int i = 2; i <= n; i++) {
            long current;

            // If the current step is broken, it is unreachable by definition.
            if (brokenSteps[i]) {
                current = 0;
            } else {
                // Otherwise, ways to reach i = ways to reach i-1 + ways to reach i-2.
                current = (prev1 + prev2) % MOD;
            }

            // Shift the window forward:
            // old prev1 becomes new prev2
            // current becomes new prev1
            prev2 = prev1;
            prev1 = current;
        }

        return (int) prev1;
    }

    /**
     * Demonstrates the solution using sample inputs and prints the results.
     *
     * This main method also prints the expected values from the dynamic programming logic.
     * For Example 2, the mathematically correct result under the stated rules is 2.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding method calls
     * Space complexity: O(1), excluding method calls
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int n1 = 5;
        int[] broken1 = {2};
        int result1 = solution.countWays(n1, broken1);
        System.out.println("Example 1:");
        System.out.println("n = " + n1 + ", broken = " + Arrays.toString(broken1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 2");
        System.out.println();

        int n2 = 6;
        int[] broken2 = {3, 5};
        int result2 = solution.countWays(n2, broken2);
        System.out.println("Example 2:");
        System.out.println("n = " + n2 + ", broken = " + Arrays.toString(broken2));
        System.out.println("Output: " + result2);
        System.out.println("Note: Under the stated rules, the correct result is 2.");
        System.out.println();

        int optimized1 = solution.countWaysOptimized(n1, broken1);
        int optimized2 = solution.countWaysOptimized(n2, broken2);
        System.out.println("Optimized method check:");
        System.out.println("Example 1 optimized output: " + optimized1);
        System.out.println("Example 2 optimized output: " + optimized2);
    }
}