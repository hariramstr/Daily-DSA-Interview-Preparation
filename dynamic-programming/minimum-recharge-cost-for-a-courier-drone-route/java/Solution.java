import java.util.*;

/*
 * Minimum Recharge Cost for a Courier Drone Route
 *
 * Problem Description:
 * A courier drone must travel through a fixed sequence of delivery checkpoints numbered from 0 to n - 1.
 * At checkpoint i, the drone may buy an energy pack that allows it to fly forward up to jump[i]
 * checkpoints, and buying that pack costs cost[i].
 *
 * The drone starts at checkpoint 0 and must reach checkpoint n - 1.
 * You may choose to buy a pack at any checkpoint you land on, including checkpoint 0.
 * After buying a pack at checkpoint i, the drone may move to any checkpoint j such that:
 *     i < j <= i + jump[i]
 *
 * The drone does not need to buy packs at every checkpoint it visits.
 * If a checkpoint is unreachable, the route cannot be completed.
 *
 * Return the minimum total recharge cost needed to reach checkpoint n - 1.
 * If it is impossible, return -1.
 *
 * Notes:
 * - This is a shortest-path / dynamic-programming style problem on a 1D route.
 * - A greedy strategy does not always work.
 *
 * Constraints:
 * - 1 <= n <= 5000
 * - cost.length == n
 * - jump.length == n
 * - 0 <= cost[i] <= 10^9
 * - 0 <= jump[i] <= n
 * - The answer fits in a 64-bit signed integer if a solution exists.
 */
public class Solution {

    /**
     * Computes the minimum total recharge cost needed to reach the last checkpoint.
     *
     * Core idea:
     * We define dp[i] = minimum total cost required to arrive at checkpoint i.
     *
     * Transition:
     * If we are currently at checkpoint i and dp[i] is known, then by buying the pack at i
     * (which costs cost[i]), we may move to any checkpoint j in the range:
     *     i + 1 ... min(n - 1, i + jump[i])
     *
     * Therefore:
     *     dp[j] = min(dp[j], dp[i] + cost[i])
     *
     * Important interpretation:
     * - You only pay cost[i] when you decide to use checkpoint i as a launching point.
     * - Reaching the final checkpoint n - 1 does not require buying a pack there.
     * - If n == 1, we are already at the destination, so the answer is 0.
     *
     * This implementation uses a straightforward O(n^2) dynamic programming approach,
     * which is acceptable for n <= 5000.
     *
     * @param cost the recharge cost at each checkpoint
     * @param jump the maximum forward distance that can be traveled after buying at each checkpoint
     * @return the minimum total recharge cost to reach checkpoint n - 1, or -1 if impossible
     *
     * Time complexity: O(n^2) in the worst case, because from each checkpoint we may relax up to O(n) next checkpoints.
     * Space complexity: O(n) for the dp array.
     */
    public long minimumRechargeCost(int[] cost, int[] jump) {
        if (cost == null || jump == null || cost.length != jump.length || cost.length == 0) {
            return -1;
        }

        int n = cost.length;

        // If there is only one checkpoint, we start at the destination already.
        // No purchase is needed.
        if (n == 1) {
            return 0L;
        }

        // Use a very large value to represent "unreachable".
        long INF = Long.MAX_VALUE / 4;

        // dp[i] = minimum cost to ARRIVE at checkpoint i.
        long[] dp = new long[n];
        Arrays.fill(dp, INF);

        // We start at checkpoint 0 with zero cost paid so far.
        // Buying at checkpoint 0 is optional, and if useful, it will be considered
        // when we process transitions out of checkpoint 0.
        dp[0] = 0L;

        // Process checkpoints from left to right.
        // Because all moves go strictly forward, once we know dp[i], we can safely
        // use it to update future checkpoints.
        for (int i = 0; i < n; i++) {
            // If checkpoint i itself is unreachable, we cannot launch from it.
            if (dp[i] == INF) {
                continue;
            }

            // If jump[i] == 0, buying here cannot move us anywhere.
            // So there are no outgoing transitions from this checkpoint.
            if (jump[i] == 0) {
                continue;
            }

            // If we choose to buy the pack at checkpoint i, the total cost becomes:
            long nextCost = dp[i] + cost[i];

            // The farthest checkpoint we can reach from i.
            int farthest = Math.min(n - 1, i + jump[i]);

            // Try every reachable next checkpoint j.
            // This directly models the rule:
            // after buying at i, the drone may move to any j such that i < j <= i + jump[i].
            for (int j = i + 1; j <= farthest; j++) {
                if (nextCost < dp[j]) {
                    dp[j] = nextCost;
                }
            }
        }

        return dp[n - 1] == INF ? -1L : dp[n - 1];
    }

    /**
     * Convenience wrapper that accepts lists instead of arrays.
     *
     * @param costList list of recharge costs
     * @param jumpList list of jump capacities
     * @return the minimum total recharge cost to reach the last checkpoint, or -1 if impossible
     *
     * Time complexity: O(n^2), same as the array-based method after conversion.
     * Space complexity: O(n) for the converted arrays and dp array.
     */
    public long minimumRechargeCost(List<Integer> costList, List<Integer> jumpList) {
        if (costList == null || jumpList == null || costList.size() != jumpList.size() || costList.isEmpty()) {
            return -1;
        }

        int n = costList.size();
        int[] cost = new int[n];
        int[] jump = new int[n];

        for (int i = 0; i < n; i++) {
            cost[i] = costList.get(i);
            jump[i] = jumpList.get(i);
        }

        return minimumRechargeCost(cost, jump);
    }

    /**
     * Demonstrates the solution on sample-style inputs and a few extra checks.
     *
     * Note about the problem statement's Example 2:
     * The stated explanation says the route is impossible for:
     *     cost = [3, 2, 7, 1, 5], jump = [1, 0, 2, 1, 0]
     * That is correct because from checkpoint 0 you can only reach checkpoint 1,
     * and checkpoint 1 cannot move further.
     * Therefore the answer is -1.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     *
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm runs.
     * Space complexity: O(1), excluding the called algorithm runs.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] cost1 = {5, 1, 4, 2};
        int[] jump1 = {2, 2, 1, 0};
        long result1 = solution.minimumRechargeCost(cost1, jump1);
        System.out.println("Example 1 result: " + result1);
        // Expected: 6
        // One optimal path:
        // Start at 0 with cost 0
        // Buy at 0 -> pay 5, can go to 1 or 2
        // Go to 1
        // Buy at 1 -> pay 1 more, can go to 2 or 3
        // Go to 3
        // Total = 6

        int[] cost2 = {3, 2, 7, 1, 5};
        int[] jump2 = {1, 0, 2, 1, 0};
        long result2 = solution.minimumRechargeCost(cost2, jump2);
        System.out.println("Example 2 result: " + result2);
        // Expected: -1
        // From 0, only checkpoint 1 is reachable.
        // From 1, jump is 0, so the route gets stuck.

        int[] cost3 = {0};
        int[] jump3 = {0};
        long result3 = solution.minimumRechargeCost(cost3, jump3);
        System.out.println("Single checkpoint result: " + result3);
        // Expected: 0

        int[] cost4 = {4, 100, 1, 1, 1};
        int[] jump4 = {4, 1, 1, 1, 0};
        long result4 = solution.minimumRechargeCost(cost4, jump4);
        System.out.println("Extra test result: " + result4);
        // Expected: 4
        // Buy at 0 and jump directly to 4.
    }
}