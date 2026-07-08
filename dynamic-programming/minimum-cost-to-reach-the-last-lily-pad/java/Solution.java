import java.util.*;

/*
 * Title: Minimum Cost to Reach the Last Lily Pad
 * Difficulty: Easy
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A frog is crossing a narrow pond by jumping across a line of lily pads.
 * You are given an integer array cost where cost[i] is the energy cost of
 * landing on lily pad i. The frog starts before the first lily pad, and its
 * goal is to move beyond the last pad. On each move, the frog may jump forward
 * by either 1 pad or 2 pads. If the frog lands on a lily pad, it must pay that
 * pad's cost. The frog does not pay any cost for the starting position before
 * index 0, and it also does not pay any cost after it has moved beyond the
 * last index. Return the minimum total energy needed to reach beyond the last
 * lily pad.
 *
 * This is a classic one-dimensional dynamic programming problem with a simple
 * state transition. For each position, you can arrive from one step before or
 * two steps before, so the best answer for a pad depends on the minimum cost
 * of those earlier positions. An efficient solution should run in O(n) time,
 * and it can be implemented with either a full DP array or constant extra space.
 *
 * Constraints:
 * - 1 <= cost.length <= 1000
 * - 0 <= cost[i] <= 999
 *
 * Example 1:
 * Input: cost = [4, 2, 7, 3]
 * Output: 5
 * Explanation: One optimal path is to land on pad 1 (cost 2), then pad 3
 * (cost 3), then jump beyond the last pad. Total cost = 2 + 3 = 5.
 *
 * Example 2:
 * Input: cost = [1, 100, 1, 1, 100, 1]
 * Output: 3
 * Explanation: One optimal path is to land on pads 0, 2, 3, and 5, paying
 * 1 + 1 + 1 + 1 = 4, but that is not optimal. A better path is to land on
 * pads 0, 2, and 5, paying 1 + 1 + 1 = 3.
 */

public class Solution {

    /**
     * Computes the minimum total energy needed to move beyond the last lily pad.
     *
     * Dynamic programming idea:
     * - Let dp[i] represent the minimum cost required to stand on position i.
     * - Here, positions 0..n-1 are actual lily pads.
     * - Position n represents "beyond the last lily pad" and has no landing cost.
     *
     * Transition:
     * - To reach pad i, the frog must come from pad i-1 or pad i-2.
     * - Since landing on pad i costs cost[i], we add that cost when computing dp[i].
     * - To reach beyond the last pad (position n), the frog can come from n-1 or n-2,
     *   and no extra cost is paid there.
     *
     * This method uses constant extra space by only keeping the last two DP values.
     *
     * @param cost an array where cost[i] is the energy cost of landing on lily pad i
     * @return the minimum total energy needed to move beyond the last lily pad
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int minCostClimbingStairs(int[] cost) {
        // Defensive handling for null or empty input.
        // The problem guarantees at least one element, but beginner-friendly code
        // often includes safe guards.
        if (cost == null || cost.length == 0) {
            return 0;
        }

        int n = cost.length;

        // If there is only one lily pad:
        // The frog can either:
        // - jump directly beyond it from the start using a 2-step style move conceptually,
        //   paying 0, or
        // - land on it and then move beyond, paying cost[0].
        // The minimum is 0.
        //
        // This matches the classic interpretation where the frog may start from
        // step 0 or step 1 without paying a starting cost.
        if (n == 1) {
            return 0;
        }

        // prev2 will represent the minimum cost to reach the "virtual position" 0 start option.
        // prev1 will represent the minimum cost to reach the "virtual position" 1 start option.
        //
        // Another way to understand this:
        // We define dp[i] = minimum cost to reach position i,
        // where position i means "standing just before paying for step i",
        // and the answer is dp[n].
        //
        // Base cases:
        // dp[0] = 0  -> start before the first pad
        // dp[1] = 0  -> the frog may also begin in a way that allows first move to pad 1
        int prev2 = 0; // dp[0]
        int prev1 = 0; // dp[1]

        // We now compute dp[2], dp[3], ..., dp[n].
        // For each position i:
        // - If we came from i-1, then we must have paid cost[i-1]
        // - If we came from i-2, then we must have paid cost[i-2]
        //
        // Therefore:
        // dp[i] = min(dp[i-1] + cost[i-1], dp[i-2] + cost[i-2])
        for (int i = 2; i <= n; i++) {
            // Cost if the frog reaches current position i by jumping 1 pad from position i-1.
            // Since that move lands on pad i-1 before moving to i, we add cost[i-1].
            int oneStep = prev1 + cost[i - 1];

            // Cost if the frog reaches current position i by jumping 2 pads from position i-2.
            // Since that move lands on pad i-2 before moving to i, we add cost[i-2].
            int twoSteps = prev2 + cost[i - 2];

            // The best way to reach position i is the cheaper of the two possibilities.
            int current = Math.min(oneStep, twoSteps);

            // Shift the window forward:
            // - old prev1 becomes new prev2
            // - current becomes new prev1
            prev2 = prev1;
            prev1 = current;
        }

        // After the loop, prev1 holds dp[n], which is the minimum cost to move beyond the last pad.
        return prev1;
    }

    /**
     * Computes the minimum total energy needed using a full DP array.
     *
     * This version is especially useful for learning because it stores every
     * intermediate result explicitly.
     *
     * @param cost an array where cost[i] is the energy cost of landing on lily pad i
     * @return the minimum total energy needed to move beyond the last lily pad
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int minCostClimbingStairsWithDpArray(int[] cost) {
        if (cost == null || cost.length == 0) {
            return 0;
        }

        int n = cost.length;

        if (n == 1) {
            return 0;
        }

        // dp[i] = minimum cost to reach position i
        // Position n means beyond the last lily pad.
        int[] dp = new int[n + 1];

        // Base cases:
        // Starting before the first pad costs nothing.
        dp[0] = 0;
        dp[1] = 0;

        // Build the answer from smaller subproblems.
        for (int i = 2; i <= n; i++) {
            // Option 1: come from position i-1 and pay cost of pad i-1
            int oneStep = dp[i - 1] + cost[i - 1];

            // Option 2: come from position i-2 and pay cost of pad i-2
            int twoSteps = dp[i - 2] + cost[i - 2];

            // Store the cheaper option.
            dp[i] = Math.min(oneStep, twoSteps);
        }

        return dp[n];
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement.
     *
     * @param args command-line arguments; not used in this program
     * @return nothing
     * Time complexity: O(n) overall for each demonstration call
     * Space complexity: O(1) for the main demonstrated method, excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] cost1 = {4, 2, 7, 3};
        int result1 = solution.minCostClimbingStairs(cost1);
        System.out.println("Input: " + Arrays.toString(cost1));
        System.out.println("Minimum cost: " + result1);
        System.out.println("Expected: 5");
        System.out.println();

        int[] cost2 = {1, 100, 1, 1, 100, 1};
        int result2 = solution.minCostClimbingStairs(cost2);
        System.out.println("Input: " + Arrays.toString(cost2));
        System.out.println("Minimum cost: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        // Additional beginner-friendly demonstrations
        int[] cost3 = {10, 15, 20};
        int result3 = solution.minCostClimbingStairs(cost3);
        System.out.println("Input: " + Arrays.toString(cost3));
        System.out.println("Minimum cost: " + result3);
        System.out.println();

        int[] cost4 = {0, 0, 0, 0};
        int result4 = solution.minCostClimbingStairs(cost4);
        System.out.println("Input: " + Arrays.toString(cost4));
        System.out.println("Minimum cost: " + result4);
        System.out.println();

        // Show that both implementations produce the same answer.
        System.out.println("Using DP array version on Example 1: "
                + solution.minCostClimbingStairsWithDpArray(cost1));
        System.out.println("Using DP array version on Example 2: "
                + solution.minCostClimbingStairsWithDpArray(cost2));
    }
}