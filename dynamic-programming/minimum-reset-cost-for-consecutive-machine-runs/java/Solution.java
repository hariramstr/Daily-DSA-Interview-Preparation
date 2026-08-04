import java.util.*;

/*
 * Title: Minimum Reset Cost for Consecutive Machine Runs
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A factory must process a sequence of n jobs in the given order. Each job i requires the machine
 * to run in one of several supported modes, represented by an integer modes[i].
 *
 * The machine may continue running in the same mode for the next job at no extra cost.
 * However, if the next job uses a different mode, the factory must pay a reset cost.
 *
 * You are also given an array resetCost where resetCost[x] is the cost to switch the machine
 * into mode x. Switching from any mode a to a different mode b always costs resetCost[b].
 * The initial machine state is undefined, so starting the first job in mode x also costs resetCost[x].
 *
 * Before processing begins, the factory may upgrade at most k jobs. Upgrading job i allows it
 * to be processed in any mode you choose, not just modes[i]. Each upgraded job still occupies
 * its position in the sequence, but you may assign it any mode in order to reduce the total reset cost.
 *
 * Return the minimum possible total cost to process all jobs.
 *
 * In other words, you may change the required mode of up to k positions to arbitrary modes,
 * and you want to minimize the sum of start/switch costs across the full sequence.
 *
 * Constraints:
 * - 1 <= n <= 2000
 * - 1 <= k <= n
 * - 1 <= modes[i] <= m
 * - 1 <= m <= 100
 * - resetCost.length == m + 1, where index 0 is unused
 * - 1 <= resetCost[x] <= 10^4
 *
 * Key Insight:
 * The total cost is paid exactly when a new contiguous segment starts.
 * If the final assigned mode sequence is:
 *   a1, a2, a3, ..., an
 * then the total cost is:
 *   resetCost[a1] + sum over i>1 of (ai != a(i-1) ? resetCost[ai] : 0)
 *
 * So we want to assign modes to at most k positions differently from their original values
 * in order to minimize the total "segment start" costs.
 *
 * Dynamic Programming State:
 * Let dp[u][x] be the minimum total cost after processing some prefix,
 * using exactly u upgrades, and ending with current machine mode x.
 *
 * Transition for next job with original mode orig:
 * 1) Do not upgrade:
 *      nextMode = orig
 *      extra cost = 0 if x == orig, else resetCost[orig]
 * 2) Upgrade:
 *      We may assign this job to any mode y.
 *      - If y == x, extra cost = 0
 *      - If y != x, extra cost = resetCost[y]
 *
 * A naive upgrade transition over all y for every x would be O(m^2) per state.
 * We optimize it:
 * - Keeping same mode x after upgrade costs nothing extra, so:
 *      next[u+1][x] = min(next[u+1][x], dp[u][x])
 * - Switching to some y costs resetCost[y], independent of previous mode except y != x.
 *   For each y, the best previous state that switches into y is:
 *      min over x != y of dp[u][x] + resetCost[y]
 *   This can be computed in O(1) per y if we know the smallest and second smallest values
 *   among dp[u][1..m].
 *
 * Complexity:
 * - Time: O(n * k * m)
 * - Space: O(k * m) if storing all layers, but here we use rolling arrays: O(k * m) per step
 *   simplified to O((k+1) * m) for current/next.
 *
 * This is efficient for:
 * - n <= 2000
 * - k <= 2000
 * - m <= 100
 */

public class Solution {

    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Computes the minimum possible total reset cost after upgrading at most k jobs.
     *
     * Dynamic programming idea:
     * We process jobs from left to right.
     * For every possible number of upgrades used and every possible current ending mode,
     * we store the minimum cost to realize the processed prefix.
     *
     * @param modes the original required mode for each job; each value is in [1, m]
     * @param k the maximum number of jobs that may be upgraded
     * @param resetCost resetCost[x] is the cost to start/switch into mode x; index 0 is unused
     * @return the minimum total reset cost achievable with at most k upgrades
     *
     * Time complexity: O(n * k * m)
     * Space complexity: O(k * m)
     */
    public long minimumResetCost(int[] modes, int k, int[] resetCost) {
        int n = modes.length;
        int m = resetCost.length - 1;

        // We never need more than n upgrades.
        k = Math.min(k, n);

        // dp[u][mode] = minimum cost after processing the current prefix,
        // using exactly u upgrades, and ending with machine in "mode".
        long[][] dp = new long[k + 1][m + 1];
        long[][] next = new long[k + 1][m + 1];

        // Initialize all states to INF (unreachable).
        for (int u = 0; u <= k; u++) {
            Arrays.fill(dp[u], INF);
            Arrays.fill(next[u], INF);
        }

        // Before processing any job, there is no current mode yet.
        // We handle the first job naturally through transitions from an "empty prefix".
        //
        // Instead of inventing a fake mode 0, we initialize the first job directly:
        // - Without upgrade: assign its original mode, pay resetCost[orig]
        // - With upgrade: assign any mode y, pay resetCost[y]
        int first = modes[0];

        // Case 1: first job is not upgraded.
        dp[0][first] = Math.min(dp[0][first], resetCost[first]);

        // Case 2: first job is upgraded.
        if (k >= 1) {
            for (int y = 1; y <= m; y++) {
                dp[1][y] = Math.min(dp[1][y], resetCost[y]);
            }
        }

        // Process remaining jobs one by one.
        for (int i = 1; i < n; i++) {
            int orig = modes[i];

            // Reset next layer to INF before filling it.
            for (int u = 0; u <= k; u++) {
                Arrays.fill(next[u], INF);
            }

            // For each possible number of upgrades already used...
            for (int used = 0; used <= k; used++) {

                // ------------------------------------------------------------
                // Step A: transition when we DO NOT upgrade this job.
                // ------------------------------------------------------------
                //
                // Then this job must use its original mode "orig".
                // If previous ending mode is also orig, no extra cost.
                // Otherwise, we pay resetCost[orig].
                //
                // We can compute:
                // next[used][orig] = min over prevMode:
                //    dp[used][prevMode] + (prevMode == orig ? 0 : resetCost[orig])
                //
                // We do this by scanning all previous modes.
                long bestNoUpgrade = INF;
                for (int prevMode = 1; prevMode <= m; prevMode++) {
                    long prevCost = dp[used][prevMode];
                    if (prevCost >= INF) {
                        continue;
                    }

                    long candidate = prevCost + (prevMode == orig ? 0L : resetCost[orig]);
                    if (candidate < bestNoUpgrade) {
                        bestNoUpgrade = candidate;
                    }
                }
                next[used][orig] = Math.min(next[used][orig], bestNoUpgrade);

                // ------------------------------------------------------------
                // Step B: transition when we DO upgrade this job.
                // ------------------------------------------------------------
                //
                // Then we may assign this job to ANY mode y.
                //
                // For a fixed target mode y:
                // - If previous mode is y, extra cost = 0
                // - Otherwise, extra cost = resetCost[y]
                //
                // So:
                // next[used+1][y] = min(
                //      dp[used][y],                         // keep same mode after upgrade
                //      min_{x != y}(dp[used][x]) + resetCost[y]   // switch into y
                // )
                //
                // To avoid O(m^2), we precompute the smallest and second smallest dp[used][x].
                if (used < k) {
                    long min1 = INF;
                    long min2 = INF;
                    int minMode = -1;

                    // Find the smallest and second smallest values in dp[used][1..m].
                    for (int mode = 1; mode <= m; mode++) {
                        long value = dp[used][mode];
                        if (value < min1) {
                            min2 = min1;
                            min1 = value;
                            minMode = mode;
                        } else if (value < min2) {
                            min2 = value;
                        }
                    }

                    // Now compute best upgraded transition for every target mode y.
                    for (int y = 1; y <= m; y++) {
                        long best = dp[used][y]; // assign upgraded job to same mode as previous; no extra cost

                        // Best previous state with mode different from y.
                        long bestDifferent = (minMode == y ? min2 : min1);
                        if (bestDifferent < INF) {
                            best = Math.min(best, bestDifferent + resetCost[y]);
                        }

                        if (best < next[used + 1][y]) {
                            next[used + 1][y] = best;
                        }
                    }
                }
            }

            // Move next layer into dp for the next iteration.
            long[][] temp = dp;
            dp = next;
            next = temp;
        }

        // We are allowed to use AT MOST k upgrades, so answer is the minimum over all used <= k
        // and all ending modes.
        long answer = INF;
        for (int used = 0; used <= k; used++) {
            for (int mode = 1; mode <= m; mode++) {
                answer = Math.min(answer, dp[used][mode]);
            }
        }

        return answer;
    }

    /**
     * Convenience wrapper that returns the answer as an int when it is guaranteed to fit.
     *
     * @param modes the original required mode for each job
     * @param k the maximum number of upgrades allowed
     * @param resetCost resetCost[x] is the cost to start/switch into mode x
     * @return the minimum total reset cost as an int
     *
     * Time complexity: O(n * k * m)
     * Space complexity: O(k * m)
     */
    public int minimumResetCostInt(int[] modes, int k, int[] resetCost) {
        return (int) minimumResetCost(modes, k, resetCost);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Note:
     * The original statement text contains contradictory sample outputs/explanations.
     * We verify the true optimum by the problem rules:
     *
     * Example 1:
     * modes = [1, 2, 2, 3], k = 1, resetCost = [0, 5, 2, 7]
     * Best is upgrading the last job 3 -> 2, giving [1,2,2,2]:
     * cost = 5 + 2 = 7
     *
     * Example 2:
     * modes = [4, 1, 4, 1, 4], k = 2, resetCost = [0, 3, 6, 8, 2]
     * Best is upgrading both 1s -> 4, giving all 4s:
     * cost = 2
     *
     * @param args command-line arguments (unused)
     * @return nothing
     *
     * Time complexity: O(n * k * m) per demonstration call
     * Space complexity: O(k * m)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] modes1 = {1, 2, 2, 3};
        int k1 = 1;
        int[] resetCost1 = {0, 5, 2, 7};
        long result1 = solution.minimumResetCost(modes1, k1, resetCost1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected by correct reasoning: 7");

        int[] modes2 = {4, 1, 4, 1, 4};
        int k2 = 2;
        int[] resetCost2 = {0, 3, 6, 8, 2};
        long result2 = solution.minimumResetCost(modes2, k2, resetCost2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected by correct reasoning: 2");

        // Additional small sanity checks.
        int[] modes3 = {2, 2, 2};
        int k3 = 1;
        int[] resetCost3 = {0, 10, 4};
        long result3 = solution.minimumResetCost(modes3, k3, resetCost3);
        System.out.println("All same mode result: " + result3);
        System.out.println("Expected: 4");

        int[] modes4 = {1, 2, 1};
        int k4 = 1;
        int[] resetCost4 = {0, 5, 1};
        long result4 = solution.minimumResetCost(modes4, k4, resetCost4);
        System.out.println("Mixed small test result: " + result4);
    }
}