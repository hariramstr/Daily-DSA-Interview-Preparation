import java.util.*;

/*
 * Title: Minimum Cost to Stabilize a Multi-Stage Assembly Line
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A factory produces a product through n sequential assembly stages, numbered from 0 to n - 1.
 * At each stage i, the machine can be configured in one of m modes. Running stage i in mode j
 * incurs a cost costs[i][j]. However, switching modes too aggressively can destabilize the line.
 * You are also given an m x m matrix switchPenalty where switchPenalty[a][b] is the penalty paid
 * when two consecutive stages use mode a followed by mode b.
 *
 * Your goal is to choose exactly one mode for every stage so that the total cost is minimized.
 * The total cost is the sum of all chosen stage costs plus all penalties between adjacent stages.
 * In addition, the final mode sequence must contain exactly k mode-blocks, where a mode-block is
 * a maximal contiguous group of stages using the same mode. For example, modes [2,2,1,1,3] has 3
 * mode-blocks, while [0,1,0] has 3 mode-blocks.
 *
 * Return the minimum possible total cost. If it is impossible to form exactly k mode-blocks,
 * return -1.
 *
 * Constraints:
 * - 1 <= n <= 100
 * - 1 <= m <= 50
 * - 1 <= k <= n
 * - costs.length == n
 * - costs[i].length == m
 * - switchPenalty.length == m
 * - switchPenalty[a].length == m
 * - 0 <= costs[i][j] <= 10^6
 * - 0 <= switchPenalty[a][b] <= 10^6
 * - switchPenalty[a][a] may be 0 or nonzero, but a new block is created only when the mode changes
 *
 * Important correctness note:
 * We solve exactly the problem statement as written:
 * total cost = sum of chosen stage costs + sum of penalties for every adjacent pair.
 * A new block is created only when the mode changes, regardless of penalty values.
 *
 * Dynamic Programming Idea:
 * Let dp[b][c] represent the minimum total cost after processing stages up to the current stage,
 * where:
 *   - b = number of blocks formed so far
 *   - c = mode chosen at the current stage
 *
 * Transition:
 * For the next stage, choose mode next.
 * If next == current mode c:
 *   new block count stays b
 * Else:
 *   new block count becomes b + 1
 *
 * In both cases, we add:
 *   costs[nextStage][next] + switchPenalty[c][next]
 *
 * Since n <= 100, m <= 50, k <= 100, the O(n * k * m * m) dynamic programming solution is
 * efficient enough.
 */
public class Solution {

    /**
     * A large sentinel value used to represent an unreachable DP state.
     * We use a long because total cost can exceed int range:
     * up to roughly n * (10^6 + 10^6) = 2 * 10^8, which still fits in int,
     * but long is safer and standard for DP minimization problems.
     */
    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Computes the minimum total cost to choose one mode for each stage such that
     * the final sequence contains exactly k mode-blocks.
     *
     * A mode-block is a maximal contiguous segment of equal modes.
     *
     * DP state definition:
     * dp[blocks][mode] = minimum cost after processing the current stage,
     *                    ending in "mode" with exactly "blocks" blocks formed.
     *
     * Transition:
     * From previous mode prevMode to current mode currMode:
     * - newBlocks = oldBlocks + (prevMode == currMode ? 0 : 1)
     * - cost added = costs[currentStage][currMode] + switchPenalty[prevMode][currMode]
     *
     * Initialization:
     * At stage 0, choosing any mode starts exactly 1 block:
     * dp[1][mode] = costs[0][mode]
     *
     * Final answer:
     * min over all ending modes of dp[k][mode] after processing all stages.
     *
     * @param n the number of stages
     * @param m the number of available modes at each stage
     * @param k the required exact number of mode-blocks
     * @param costs costs[i][j] is the cost of running stage i in mode j
     * @param switchPenalty switchPenalty[a][b] is the penalty for consecutive stages using mode a then mode b
     * @return the minimum possible total cost, or -1 if it is impossible to form exactly k blocks
     * @implNote Time complexity: O(n * k * m * m)
     * @implNote Space complexity: O(k * m), using rolling DP arrays
     */
    public long minimumCost(int n, int m, int k, int[][] costs, int[][] switchPenalty) {
        // Basic impossibility check:
        // The minimum possible number of blocks is 1 (all stages same mode),
        // and the maximum possible number of blocks is n (change at every stage).
        if (k < 1 || k > n) {
            return -1;
        }

        // dpPrev[blocks][mode] stores the best cost after processing the previous stage.
        long[][] dpPrev = new long[k + 1][m];
        long[][] dpCurr = new long[k + 1][m];

        // Initialize all states as unreachable.
        fillWithInf(dpPrev);
        fillWithInf(dpCurr);

        // Stage 0 initialization:
        // Choosing any mode at the first stage creates exactly 1 block.
        for (int mode = 0; mode < m; mode++) {
            dpPrev[1][mode] = costs[0][mode];
        }

        // Process stages from 1 to n - 1.
        for (int stage = 1; stage < n; stage++) {
            // Reset current layer to INF before filling transitions.
            fillWithInf(dpCurr);

            // At stage index "stage", the number of blocks cannot exceed stage + 1.
            // Also, it cannot exceed k.
            int maxBlocksSoFar = Math.min(k, stage + 1);

            // The previous stage could have formed at least 1 block,
            // and at most min(k, stage) blocks.
            int maxPrevBlocks = Math.min(k, stage);

            // Try every reachable previous state.
            for (int blocks = 1; blocks <= maxPrevBlocks; blocks++) {
                for (int prevMode = 0; prevMode < m; prevMode++) {
                    long prevCost = dpPrev[blocks][prevMode];

                    // If this state is unreachable, skip it.
                    if (prevCost >= INF) {
                        continue;
                    }

                    // Try choosing every possible mode for the current stage.
                    for (int currMode = 0; currMode < m; currMode++) {
                        // If the mode stays the same, block count does not increase.
                        // If the mode changes, we start a new block.
                        int newBlocks = blocks + (prevMode == currMode ? 0 : 1);

                        // If we exceed the required maximum k, skip.
                        if (newBlocks > maxBlocksSoFar) {
                            continue;
                        }

                        // Total cost added for choosing currMode at this stage:
                        // 1) stage operating cost
                        // 2) adjacent penalty from prevMode to currMode
                        long candidate = prevCost + costs[stage][currMode] + switchPenalty[prevMode][currMode];

                        // Standard DP relaxation.
                        if (candidate < dpCurr[newBlocks][currMode]) {
                            dpCurr[newBlocks][currMode] = candidate;
                        }
                    }
                }
            }

            // Move current layer into previous layer for the next iteration.
            long[][] temp = dpPrev;
            dpPrev = dpCurr;
            dpCurr = temp;
        }

        // Extract the best answer among all ending modes with exactly k blocks.
        long answer = INF;
        for (int mode = 0; mode < m; mode++) {
            answer = Math.min(answer, dpPrev[k][mode]);
        }

        return answer >= INF ? -1 : answer;
    }

    /**
     * Convenience overload that infers n and m directly from the input arrays.
     *
     * @param costs costs[i][j] is the cost of running stage i in mode j
     * @param switchPenalty switchPenalty[a][b] is the penalty for consecutive stages using mode a then mode b
     * @param k the required exact number of mode-blocks
     * @return the minimum possible total cost, or -1 if impossible
     * @implNote Time complexity: O(n * k * m * m)
     * @implNote Space complexity: O(k * m)
     */
    public long minimumCost(int[][] costs, int[][] switchPenalty, int k) {
        int n = costs.length;
        int m = costs[0].length;
        return minimumCost(n, m, k, costs, switchPenalty);
    }

    /**
     * Fills a 2D long array with the INF sentinel value.
     *
     * @param array the 2D DP array to reset
     * @return nothing
     * @implNote Time complexity: O(rows * cols)
     * @implNote Space complexity: O(1) extra space
     */
    public void fillWithInf(long[][] array) {
        for (long[] row : array) {
            Arrays.fill(row, INF);
        }
    }

    /**
     * Demonstrates the solution on sample-style inputs and prints the results.
     *
     * Note:
     * The examples in the prompt contain internal arithmetic inconsistencies.
     * This program prints the mathematically correct result for the stated problem:
     * total cost includes all chosen stage costs and all adjacent penalties.
     *
     * @param args command-line arguments, unused
     * @return nothing
     * @implNote Time complexity: dominated by the DP calls shown
     * @implNote Space complexity: dominated by the DP calls shown
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int n1 = 4, m1 = 3, k1 = 2;
        int[][] costs1 = {
            {3, 1, 4},
            {2, 5, 3},
            {6, 2, 1},
            {4, 3, 2}
        };
        int[][] penalty1 = {
            {0, 7, 4},
            {6, 0, 3},
            {5, 2, 0}
        };
        System.out.println(solution.minimumCost(n1, m1, k1, costs1, penalty1));

        int n2 = 3, m2 = 2, k2 = 3;
        int[][] costs2 = {
            {1, 10},
            {10, 1},
            {1, 10}
        };
        int[][] penalty2 = {
            {0, 5},
            {5, 0}
        };
        System.out.println(solution.minimumCost(n2, m2, k2, costs2, penalty2));

        int[][] costs3 = {
            {5, 2},
            {4, 3},
            {6, 1}
        };
        int[][] penalty3 = {
            {1, 7},
            {2, 3}
        };
        System.out.println(solution.minimumCost(costs3, penalty3, 1));
    }
}