import java.util.*;

/*
 * Title: Minimum Recharge Cost for a Courier Drone Route
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A courier drone must travel through a fixed sequence of delivery checkpoints from left to right.
 * The route is represented by an integer array cost, where cost[i] is the energy cost to land on
 * checkpoint i and recharge there. The drone starts before checkpoint 0 and wants to reach exactly
 * the last checkpoint n - 1.
 *
 * From checkpoint i, the drone may fly forward by either 1 or 2 checkpoints. However, some
 * checkpoints are marked as windy and cannot be used for landing. You are given a binary array
 * blocked of the same length, where blocked[i] = 1 means checkpoint i is unavailable and the drone
 * may not land there. The first and last checkpoints are guaranteed to be unblocked.
 *
 * The drone pays the recharge cost only for checkpoints it lands on, including the final checkpoint.
 * It does not pay anything before the route begins. Return the minimum total recharge cost needed to
 * reach the last checkpoint. If the last checkpoint cannot be reached, return -1.
 *
 * This is a path optimization problem where local greedy choices may fail because taking a slightly
 * more expensive checkpoint now may avoid a blocked or costly future path.
 *
 * Constraints:
 * - 2 <= n <= 100000
 * - cost.length == blocked.length == n
 * - 0 <= cost[i] <= 1000000000
 * - blocked[i] is either 0 or 1
 * - blocked[0] = 0
 * - blocked[n - 1] = 0
 *
 * Example 1:
 * Input: cost = [4, 2, 7, 3, 5], blocked = [0, 0, 1, 0, 0]
 * Output: 10
 * Explanation:
 * The drone starts before index 0, so its first landing can be checkpoint 0 or checkpoint 1.
 * Checkpoint 2 is blocked, so it cannot be used.
 * A valid optimal route is 1 -> 3 -> 4 with total cost 2 + 3 + 5 = 10.
 *
 * Example 2:
 * Input: cost = [6, 1, 9, 2], blocked = [0, 1, 1, 0]
 * Output: -1
 * Explanation:
 * The drone may initially land on checkpoint 0 or 1, but checkpoint 1 is blocked.
 * From checkpoint 0, both next possible landings (1 and 2) are blocked.
 * Therefore, the last checkpoint cannot be reached.
 */

public class Solution {

    /**
     * Computes the minimum total recharge cost needed for the drone to reach the last checkpoint.
     *
     * The key dynamic programming idea:
     * - Let dp[i] represent the minimum total cost required to land on checkpoint i.
     * - If checkpoint i is blocked, then dp[i] is impossible.
     * - Otherwise, the drone can arrive at i from:
     *   1) checkpoint i - 1
     *   2) checkpoint i - 2
     * - Additionally, because the drone starts before checkpoint 0, it may begin directly at:
     *   1) checkpoint 0
     *   2) checkpoint 1
     *
     * Transition:
     * - dp[i] = cost[i] + min(dp[i - 1], dp[i - 2]) for reachable, unblocked checkpoints
     * - Base cases:
     *   dp[0] = cost[0]
     *   dp[1] = cost[1] if unblocked, because the drone may start directly there,
     *           or cost[1] + dp[0] if coming from 0; we take the minimum of valid choices
     *
     * We use long internally because:
     * - cost[i] can be as large as 1,000,000,000
     * - n can be as large as 100,000
     * - the total can exceed int range
     *
     * @param cost the recharge cost for each checkpoint
     * @param blocked binary array where 1 means the checkpoint cannot be landed on
     * @return the minimum total recharge cost to reach the last checkpoint, or -1 if unreachable
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long minimumRechargeCost(int[] cost, int[] blocked) {
        validateInput(cost, blocked);

        int n = cost.length;

        // We use a very large number to represent "unreachable".
        // Long.MAX_VALUE itself is risky for addition because adding any positive value may overflow.
        // So we choose a safely smaller sentinel.
        final long INF = Long.MAX_VALUE / 4;

        // dp[i] = minimum cost to land on checkpoint i.
        long[] dp = new long[n];
        Arrays.fill(dp, INF);

        // -------------------------
        // Base case for checkpoint 0
        // -------------------------
        // The drone starts before the route and may first land on checkpoint 0.
        // Since checkpoint 0 is guaranteed unblocked, this is always valid.
        dp[0] = cost[0];

        // -------------------------
        // Base case for checkpoint 1
        // -------------------------
        // The drone may either:
        // 1) start directly at checkpoint 1 (if unblocked), paying only cost[1]
        // 2) go from checkpoint 0 to checkpoint 1, paying dp[0] + cost[1]
        //
        // We take the smaller valid option.
        if (n >= 2 && blocked[1] == 0) {
            dp[1] = Math.min((long) cost[1], dp[0] + cost[1]);
        }

        // ----------------------------------------------------------
        // Fill the DP table from left to right for checkpoints 2..n-1
        // ----------------------------------------------------------
        for (int i = 2; i < n; i++) {
            // If this checkpoint is blocked, the drone cannot land here.
            // Therefore, dp[i] remains INF (unreachable).
            if (blocked[i] == 1) {
                continue;
            }

            // The drone can arrive here from i - 1 or i - 2.
            long bestPrevious = Math.min(dp[i - 1], dp[i - 2]);

            // If both previous states are unreachable, then this checkpoint is also unreachable.
            if (bestPrevious == INF) {
                continue;
            }

            // Otherwise, land here and pay this checkpoint's recharge cost.
            dp[i] = bestPrevious + cost[i];
        }

        // If the last checkpoint is still unreachable, return -1.
        if (dp[n - 1] == INF) {
            return -1;
        }

        return dp[n - 1];
    }

    /**
     * A space-optimized version of the same dynamic programming solution.
     *
     * Instead of storing the full dp array, we only keep the last two DP values because
     * each state depends only on the previous two states.
     *
     * This method is included for educational completeness and uses the same logic.
     *
     * @param cost the recharge cost for each checkpoint
     * @param blocked binary array where 1 means the checkpoint cannot be landed on
     * @return the minimum total recharge cost to reach the last checkpoint, or -1 if unreachable
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long minimumRechargeCostOptimized(int[] cost, int[] blocked) {
        validateInput(cost, blocked);

        int n = cost.length;
        final long INF = Long.MAX_VALUE / 4;

        // Base for index 0
        long prev2 = cost[0];

        // Base for index 1
        long prev1 = INF;
        if (blocked[1] == 0) {
            prev1 = Math.min((long) cost[1], prev2 + cost[1]);
        }

        // If there are only 2 checkpoints, answer is simply dp[1].
        if (n == 2) {
            return prev1 == INF ? -1 : prev1;
        }

        // Process checkpoints from index 2 onward.
        for (int i = 2; i < n; i++) {
            long current = INF;

            // We can only land if the checkpoint is not blocked.
            if (blocked[i] == 0) {
                long bestPrevious = Math.min(prev1, prev2);

                // Only compute a valid cost if at least one previous state is reachable.
                if (bestPrevious != INF) {
                    current = bestPrevious + cost[i];
                }
            }

            // Slide the window forward:
            // prev2 becomes old prev1, prev1 becomes current.
            prev2 = prev1;
            prev1 = current;
        }

        return prev1 == INF ? -1 : prev1;
    }

    /**
     * Validates the input arrays according to the problem requirements.
     *
     * This method is not strictly required by many online judges, but it is useful in a
     * complete runnable program to make behavior clearer and safer.
     *
     * @param cost the recharge cost array
     * @param blocked the blocked checkpoint array
     * @return nothing; throws an exception if input is invalid
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void validateInput(int[] cost, int[] blocked) {
        if (cost == null || blocked == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }

        if (cost.length != blocked.length) {
            throw new IllegalArgumentException("cost and blocked must have the same length.");
        }

        if (cost.length < 2) {
            throw new IllegalArgumentException("Array length must be at least 2.");
        }

        if (blocked[0] != 0 || blocked[blocked.length - 1] != 0) {
            throw new IllegalArgumentException("First and last checkpoints must be unblocked.");
        }
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding called methods
     * Space complexity: O(1), excluding called methods
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] cost1 = {4, 2, 7, 3, 5};
        int[] blocked1 = {0, 0, 1, 0, 0};
        long result1 = solution.minimumRechargeCost(cost1, blocked1);
        System.out.println("Example 1 result: " + result1); // Expected: 10

        // Example 2
        int[] cost2 = {6, 1, 9, 2};
        int[] blocked2 = {0, 1, 1, 0};
        long result2 = solution.minimumRechargeCost(cost2, blocked2);
        System.out.println("Example 2 result: " + result2); // Expected: -1

        // Demonstrate the optimized version gives the same answers.
        long result1Optimized = solution.minimumRechargeCostOptimized(cost1, blocked1);
        long result2Optimized = solution.minimumRechargeCostOptimized(cost2, blocked2);
        System.out.println("Example 1 optimized result: " + result1Optimized); // Expected: 10
        System.out.println("Example 2 optimized result: " + result2Optimized); // Expected: -1
    }
}