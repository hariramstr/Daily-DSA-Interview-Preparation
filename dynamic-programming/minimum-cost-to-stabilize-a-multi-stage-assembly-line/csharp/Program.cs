/*
Minimum Cost to Stabilize a Multi-Stage Assembly Line

Problem Description:
A factory produces a product through n sequential assembly stages, numbered from 0 to n - 1.
At each stage i, the machine can be configured in one of m modes.
Running stage i in mode j incurs a cost costs[i][j].

You are also given an m x m matrix switchPenalty where switchPenalty[a][b] is the penalty
paid when two consecutive stages use mode a followed by mode b.

Your goal is to choose exactly one mode for every stage so that the total cost is minimized.
The total cost is:
1. The sum of all chosen stage costs
2. Plus all penalties between adjacent stages

In addition, the final mode sequence must contain exactly k mode-blocks, where a mode-block
is a maximal contiguous group of stages using the same mode.

Examples:
- [2,2,1,1,3] has 3 mode-blocks
- [0,1,0] has 3 mode-blocks

Return the minimum possible total cost. If it is impossible to form exactly k mode-blocks, return -1.

Constraints:
- 1 <= n <= 100
- 1 <= m <= 50
- 1 <= k <= n
- costs.length == n
- costs[i].length == m
- switchPenalty.length == m
- switchPenalty[a].length == m
- 0 <= costs[i][j] <= 10^6
- 0 <= switchPenalty[a][b] <= 10^6
- switchPenalty[a][a] may be 0 or nonzero, but a new block is created only when the mode changes
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n * k * m * m)

    Explanation:
    - We process each of the n stages.
    - For each stage, we consider each possible number of blocks from 1 to k.
    - For each current mode (m choices), we may transition from every previous mode (m choices).
    - Therefore the total is O(n * k * m^2), which is efficient enough for:
      n <= 100 and m <= 50.

    Space Complexity:
    O(k * m)

    Explanation:
    - We only keep DP for the previous stage and the current stage.
    - Each DP layer stores values for:
      block count from 1..k and ending mode from 0..m-1.
    */
    public long MinimumCost(int n, int m, int k, int[][] costs, int[][] switchPenalty)
    {
        // A very large number used to represent "impossible" states.
        // We choose a value safely larger than any possible valid answer.
        long INF = long.MaxValue / 4;

        // Quick impossibility check:
        // The minimum possible number of blocks is 1 (all stages same mode),
        // and the maximum possible number of blocks is n (change mode every stage).
        // If k is outside that range, no solution exists.
        if (k < 1 || k > n)
        {
            return -1;
        }

        // prev[b, mode] means:
        // After processing the previous stage,
        // the minimum total cost to have exactly (b + 1) blocks
        // and end in "mode".
        //
        // We use zero-based indexing for the block dimension internally:
        // index 0 means 1 block, index 1 means 2 blocks, ..., index k-1 means k blocks.
        long[,] prev = new long[k, m];
        long[,] curr = new long[k, m];

        // Initialize all states as impossible.
        for (int b = 0; b < k; b++)
        {
            for (int mode = 0; mode < m; mode++)
            {
                prev[b, mode] = INF;
                curr[b, mode] = INF;
            }
        }

        // Base case: stage 0.
        //
        // At the very first stage, choosing any mode creates exactly 1 block.
        // There is no previous stage, so no switch penalty is added yet.
        for (int mode = 0; mode < m; mode++)
        {
            prev[0, mode] = costs[0][mode];
        }

        // Process stages from 1 to n - 1.
        for (int stage = 1; stage < n; stage++)
        {
            // Before computing the current stage, reset all current states to INF.
            for (int b = 0; b < k; b++)
            {
                for (int mode = 0; mode < m; mode++)
                {
                    curr[b, mode] = INF;
                }
            }

            // We now compute transitions into stage "stage".
            //
            // For every possible previous block count and previous ending mode,
            // we try choosing every possible current mode.
            //
            // There are two cases:
            // 1. currentMode == previousMode
            //    - We stay in the same block
            //    - Block count does NOT increase
            //
            // 2. currentMode != previousMode
            //    - We start a new block
            //    - Block count increases by 1
            //
            // In both cases, we always add:
            // - the stage cost for the chosen current mode
            // - the switch penalty from previousMode to currentMode
            //
            // Important detail:
            // The problem states that switchPenalty[a][a] may be nonzero.
            // So even when staying in the same mode, we still add switchPenalty[a][a].
            // The only thing that depends on equality/inequality is whether a new block is created.
            for (int blocksUsedIndex = 0; blocksUsedIndex < k; blocksUsedIndex++)
            {
                for (int previousMode = 0; previousMode < m; previousMode++)
                {
                    long previousCost = prev[blocksUsedIndex, previousMode];

                    // If this previous state is impossible, skip it.
                    if (previousCost >= INF)
                    {
                        continue;
                    }

                    for (int currentMode = 0; currentMode < m; currentMode++)
                    {
                        // Compute the total cost contributed by choosing currentMode at this stage.
                        long transitionCost =
                            previousCost
                            + costs[stage][currentMode]
                            + switchPenalty[previousMode][currentMode];

                        if (currentMode == previousMode)
                        {
                            // Staying in the same mode means:
                            // - same block continues
                            // - number of blocks does not change
                            if (transitionCost < curr[blocksUsedIndex, currentMode])
                            {
                                curr[blocksUsedIndex, currentMode] = transitionCost;
                            }
                        }
                        else
                        {
                            // Changing mode means:
                            // - a new block starts
                            // - number of blocks increases by 1
                            int nextBlocksUsedIndex = blocksUsedIndex + 1;

                            // Only valid if we do not exceed k blocks.
                            if (nextBlocksUsedIndex < k)
                            {
                                if (transitionCost < curr[nextBlocksUsedIndex, currentMode])
                                {
                                    curr[nextBlocksUsedIndex, currentMode] = transitionCost;
                                }
                            }
                        }
                    }
                }
            }

            // Move current layer into previous layer for the next iteration.
            var temp = prev;
            prev = curr;
            curr = temp;
        }

        // After processing all stages, we need exactly k blocks.
        // Internally, that corresponds to index k - 1.
        long answer = INF;
        for (int mode = 0; mode < m; mode++)
        {
            if (prev[k - 1, mode] < answer)
            {
                answer = prev[k - 1, mode];
            }
        }

        return answer >= INF ? -1 : answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int n1 = 4;
int m1 = 3;
int k1 = 2;
int[][] costs1 =
{
    new[] { 3, 1, 4 },
    new[] { 2, 5, 3 },
    new[] { 6, 2, 1 },
    new[] { 4, 3, 2 }
};
int[][] switchPenalty1 =
{
    new[] { 0, 7, 4 },
    new[] { 6, 0, 3 },
    new[] { 5, 2, 0 }
};
Console.WriteLine(solution.MinimumCost(n1, m1, k1, costs1, switchPenalty1));

// Example 2
int n2 = 3;
int m2 = 2;
int k2 = 3;
int[][] costs2 =
{
    new[] { 1, 10 },
    new[] { 10, 1 },
    new[] { 1, 10 }
};
int[][] switchPenalty2 =
{
    new[] { 0, 5 },
    new[] { 5, 0 }
};
Console.WriteLine(solution.MinimumCost(n2, m2, k2, costs2, switchPenalty2));