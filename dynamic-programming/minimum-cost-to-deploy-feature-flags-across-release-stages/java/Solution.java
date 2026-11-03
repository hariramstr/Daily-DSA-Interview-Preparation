import java.util.*;

/*
 * Title: Minimum Cost to Deploy Feature Flags Across Release Stages
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A product team is preparing a release pipeline with n sequential stages, numbered from 0 to n - 1.
 * At each stage, exactly one feature flag configuration must be deployed. There are m possible
 * configurations, and deploying configuration j at stage i has base cost cost[i][j].
 *
 * However, switching configurations between consecutive stages may introduce migration overhead.
 * If stage i - 1 uses configuration a and stage i uses configuration b, an additional transition
 * cost transition[a][b] is paid. Staying on the same configuration may still have a non-zero
 * transition cost.
 *
 * The pipeline must also satisfy milestone requirements. You are given an array required of length n
 * where required[i] is either -1 or a configuration index in [0, m - 1]. If required[i] != -1,
 * then stage i must use exactly that configuration.
 *
 * In addition, the release manager wants the final deployment plan to contain exactly k contiguous
 * blocks of equal configurations. A block is a maximal consecutive segment where all stages use the
 * same configuration. For example, configurations [1,1,3,3,3,2] contain 3 blocks.
 *
 * Return the minimum total cost to choose configurations for all stages so that all required stages
 * are respected and the final plan has exactly k blocks. If it is impossible, return -1.
 *
 * A typical dynamic programming state is:
 * dp[i][b][c] = minimum total cost after processing stages 0..i,
 *               forming exactly b blocks,
 *               and choosing configuration c at stage i.
 *
 * Transition:
 * - If previous configuration p equals current configuration c, then the number of blocks does not change.
 * - Otherwise, a new block starts, so the number of blocks increases by 1.
 * - In both cases, we add:
 *     previous DP cost
 *     + base deployment cost at current stage
 *     + transition cost from previous configuration to current configuration
 *
 * For stage 0:
 * - Choosing any allowed configuration c creates exactly 1 block.
 * - Cost is simply cost[0][c].
 *
 * Final answer:
 * - minimum over dp[n - 1][k][c] for all configurations c
 * - if unreachable, return -1
 */

public class Solution {

    /**
     * A very large value used as "infinity" for unreachable DP states.
     * We choose a value safely below Long.MAX_VALUE to avoid overflow when adding costs.
     */
    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Computes the minimum total deployment cost such that:
     * 1) every stage chooses exactly one configuration,
     * 2) all required stage constraints are respected,
     * 3) the final sequence contains exactly k contiguous blocks,
     * 4) base deployment costs and transition costs are both included.
     *
     * Dynamic Programming Idea:
     * Let dp[b][c] represent the minimum cost after processing the current prefix of stages,
     * where:
     * - exactly b blocks have been formed,
     * - the current (last chosen) configuration is c.
     *
     * We process stages from left to right.
     * For each new stage, we build a new table next[b][c].
     *
     * Transition details:
     * - If previous configuration prev equals current configuration cur:
     *     block count stays the same
     * - Else:
     *     block count increases by 1
     *
     * Added cost when moving from prev to cur at stage i:
     * - transition[prev][cur]
     * - cost[i][cur]
     *
     * For stage 0:
     * - choosing configuration cur starts the first block
     * - total cost = cost[0][cur]
     *
     * Important:
     * - If required[i] != -1, then only that configuration is allowed at stage i.
     *
     * @param n number of stages
     * @param m number of configurations
     * @param k exact number of contiguous blocks required
     * @param cost base deployment cost matrix, where cost[i][j] is the cost of using configuration j at stage i
     * @param transition transition cost matrix, where transition[a][b] is the cost from previous configuration a to current configuration b
     * @param required required configuration per stage, or -1 if unrestricted
     * @return the minimum total cost satisfying all constraints, or -1 if impossible
     *
     * Time complexity: O(n * k * m * m)
     * Space complexity: O(k * m)
     */
    public long minimumDeploymentCost(int n, int m, int k, int[][] cost, int[][] transition, int[] required) {
        // Quick impossible case:
        // We need at least 1 block if n >= 1, and at most n blocks.
        if (k < 1 || k > n) {
            return -1;
        }

        // dp[b][c]:
        // Minimum cost after processing stages up to the previous stage,
        // with exactly b blocks formed, ending in configuration c.
        long[][] dp = new long[k + 1][m];

        // Initialize all states as unreachable.
        for (int b = 0; b <= k; b++) {
            Arrays.fill(dp[b], INF);
        }

        // -----------------------------
        // Base case: stage 0
        // -----------------------------
        // At the first stage, any chosen configuration starts the first block.
        // So block count becomes exactly 1.
        for (int cur = 0; cur < m; cur++) {
            // Respect required constraint at stage 0.
            if (required[0] != -1 && required[0] != cur) {
                continue;
            }

            // No transition cost before stage 0, only base cost.
            dp[1][cur] = cost[0][cur];
        }

        // -----------------------------
        // Process stages 1 to n - 1
        // -----------------------------
        for (int i = 1; i < n; i++) {
            // next[b][c]:
            // DP table for states after processing stage i.
            long[][] next = new long[k + 1][m];
            for (int b = 0; b <= k; b++) {
                Arrays.fill(next[b], INF);
            }

            // Try every possible number of blocks formed so far.
            for (int blocksSoFar = 1; blocksSoFar <= k; blocksSoFar++) {
                // Try every possible previous ending configuration.
                for (int prev = 0; prev < m; prev++) {
                    long previousCost = dp[blocksSoFar][prev];

                    // If this state was never reached, skip it.
                    if (previousCost >= INF) {
                        continue;
                    }

                    // Try every possible current configuration at stage i.
                    for (int cur = 0; cur < m; cur++) {
                        // Respect required constraint at this stage.
                        if (required[i] != -1 && required[i] != cur) {
                            continue;
                        }

                        // If current configuration equals previous one,
                        // we stay inside the same block.
                        // Otherwise, we start a new block.
                        int newBlocks = blocksSoFar + (cur == prev ? 0 : 1);

                        // If block count exceeds k, this state is invalid.
                        if (newBlocks > k) {
                            continue;
                        }

                        // Total cost for this transition:
                        // previous DP cost
                        // + transition cost from prev to cur
                        // + base cost of choosing cur at stage i
                        long candidate = previousCost + transition[prev][cur] + cost[i][cur];

                        // Relax the DP state.
                        if (candidate < next[newBlocks][cur]) {
                            next[newBlocks][cur] = candidate;
                        }
                    }
                }
            }

            // Move to the next stage.
            dp = next;
        }

        // -----------------------------
        // Final answer
        // -----------------------------
        // We need exactly k blocks after processing all n stages.
        long answer = INF;
        for (int c = 0; c < m; c++) {
            answer = Math.min(answer, dp[k][c]);
        }

        return answer >= INF ? -1 : answer;
    }

    /**
     * Convenience overload that infers n and m directly from the input arrays.
     *
     * @param cost base deployment cost matrix
     * @param transition transition cost matrix
     * @param required required configuration per stage, or -1 if unrestricted
     * @param k exact number of contiguous blocks required
     * @return the minimum total cost satisfying all constraints, or -1 if impossible
     *
     * Time complexity: O(n * k * m * m)
     * Space complexity: O(k * m)
     */
    public long minimumDeploymentCost(int[][] cost, int[][] transition, int[] required, int k) {
        int n = cost.length;
        int m = cost[0].length;
        return minimumDeploymentCost(n, m, k, cost, transition, required);
    }

    /**
     * Demonstrates the solution on the sample-style inputs.
     *
     * Note:
     * The examples in the prompt include narrative explanations with totals that are not
     * internally consistent with the provided matrices. This implementation computes the
     * mathematically correct minimum according to the formal problem statement:
     *
     * total cost =
     *   sum of chosen base costs
     *   + sum of transition costs between every consecutive pair of stages
     *
     * @param args command-line arguments (unused)
     * @return nothing
     *
     * Time complexity: O(1) for the demonstration itself, excluding the solver calls
     * Space complexity: O(1) extra for the demonstration itself
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int n1 = 4, m1 = 3, k1 = 2;
        int[][] cost1 = {
            {5, 1, 4},
            {2, 3, 6},
            {7, 2, 1},
            {4, 6, 3}
        };
        int[][] transition1 = {
            {0, 5, 2},
            {4, 0, 3},
            {6, 1, 0}
        };
        int[] required1 = {-1, -1, -1, -1};

        long result1 = solution.minimumDeploymentCost(n1, m1, k1, cost1, transition1, required1);
        System.out.println("Example 1 result: " + result1);

        int n2 = 5, m2 = 2, k2 = 3;
        int[][] cost2 = {
            {3, 8},
            {5, 1},
            {2, 4},
            {7, 2},
            {6, 3}
        };
        int[][] transition2 = {
            {0, 10},
            {1, 0}
        };
        int[] required2 = {0, -1, -1, 1, -1};

        long result2 = solution.minimumDeploymentCost(n2, m2, k2, cost2, transition2, required2);
        System.out.println("Example 2 result: " + result2);

        // Additional small sanity checks.

        // Single stage: exactly one block is possible.
        int[][] cost3 = {
            {9, 2, 5}
        };
        int[][] transition3 = {
            {0, 1, 1},
            {1, 0, 1},
            {1, 1, 0}
        };
        int[] required3 = {-1};
        System.out.println("Single stage result: " + solution.minimumDeploymentCost(cost3, transition3, required3, 1));

        // Impossible because k > n.
        System.out.println("Impossible result: " + solution.minimumDeploymentCost(cost3, transition3, required3, 2));
    }
}