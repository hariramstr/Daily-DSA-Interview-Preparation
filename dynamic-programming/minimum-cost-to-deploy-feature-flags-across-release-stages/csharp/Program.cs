/*
Minimum Cost to Deploy Feature Flags Across Release Stages

Problem Description:
A product team is preparing a release pipeline with n sequential stages, numbered from 0 to n - 1.
At each stage, exactly one feature flag configuration must be deployed. There are m possible
configurations, and deploying configuration j at stage i has base cost cost[i][j].

However, switching configurations between consecutive stages may introduce migration overhead.
If stage i - 1 uses configuration a and stage i uses configuration b, an additional transition
cost transition[a][b] is paid. Staying on the same configuration may still have a non-zero
transition cost.

The pipeline must also satisfy milestone requirements. You are given an array required of length n
where required[i] is either -1 or a configuration index in [0, m - 1]. If required[i] != -1,
then stage i must use exactly that configuration.

In addition, the release manager wants the final deployment plan to contain exactly k contiguous
blocks of equal configurations. A block is a maximal consecutive segment where all stages use the
same configuration.

Return the minimum total cost to choose configurations for all stages so that all required stages
are respected and the final plan has exactly k blocks. If it is impossible, return -1.

Typical DP state:
dp[i][b][c] = minimum cost after processing stages 0..i,
forming exactly b blocks, and using configuration c at stage i.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n * k * m * m)

    Explanation:
    - There are n stages.
    - For each stage, we consider up to k possible block counts.
    - For each ending configuration currentConfig (m choices),
      we may transition from every previous configuration prevConfig (m choices).

    Space Complexity:
    O(k * m)

    Explanation:
    - We only keep the previous stage DP table and the current stage DP table,
      instead of storing all n layers.
    */
    public long MinimumCost(int n, int m, int k, int[][] cost, int[][] transition, int[] required)
    {
        // We use a very large number to represent "impossible" states.
        // It must be safely larger than any valid answer.
        // Maximum possible cost is roughly:
        // n * 1e9 base + (n-1) * 1e9 transition <= about 2e11
        // So 4e18 is comfortably safe inside long.
        const long INF = 4_000_000_000_000_000_000L;

        // prev[b, c] means:
        // after processing the previous stage,
        // the minimum total cost to have exactly b blocks
        // and end with configuration c.
        long[,] prev = new long[k + 1, m];

        // Initialize all states as impossible.
        for (int b = 0; b <= k; b++)
        {
            for (int c = 0; c < m; c++)
            {
                prev[b, c] = INF;
            }
        }

        // -----------------------------
        // STEP 1: Initialize stage 0
        // -----------------------------
        // Why this is necessary:
        // The first stage is special because there is no previous stage.
        // Therefore:
        // - It always starts the first block.
        // - No transition cost is paid before stage 0.
        //
        // If required[0] is fixed, only that configuration is allowed.
        // Otherwise, all configurations are candidates.
        for (int c = 0; c < m; c++)
        {
            if (required[0] != -1 && required[0] != c)
            {
                continue;
            }

            // Choosing configuration c at stage 0:
            // - base cost = cost[0][c]
            // - number of blocks = 1
            prev[1, c] = cost[0][c];
        }

        // -----------------------------------------
        // STEP 2: Process stages 1 through n - 1
        // -----------------------------------------
        for (int i = 1; i < n; i++)
        {
            // curr[b, c] will store the DP values for stage i.
            long[,] curr = new long[k + 1, m];

            // Initialize current layer as impossible.
            for (int b = 0; b <= k; b++)
            {
                for (int c = 0; c < m; c++)
                {
                    curr[b, c] = INF;
                }
            }

            // Determine which configurations are allowed at this stage.
            // If required[i] is fixed, only that one is legal.
            // Otherwise, all configurations are legal.
            for (int currentConfig = 0; currentConfig < m; currentConfig++)
            {
                if (required[i] != -1 && required[i] != currentConfig)
                {
                    continue;
                }

                // Base deployment cost for using currentConfig at stage i.
                long baseCost = cost[i][currentConfig];

                // We now try to build states with exactly blocksUsed blocks
                // ending in currentConfig at stage i.
                for (int blocksUsed = 1; blocksUsed <= k; blocksUsed++)
                {
                    long best = INF;

                    // We consider every possible configuration used at stage i - 1.
                    // This is the key transition of the DP.
                    for (int prevConfig = 0; prevConfig < m; prevConfig++)
                    {
                        long prevCostSameBlocks = prev[blocksUsed, prevConfig];

                        // Case A: currentConfig == prevConfig
                        // --------------------------------
                        // Then stage i continues the same block as stage i - 1.
                        // So the number of blocks does NOT increase.
                        if (currentConfig == prevConfig)
                        {
                            if (prevCostSameBlocks != INF)
                            {
                                long candidate = prevCostSameBlocks
                                               + transition[prevConfig][currentConfig]
                                               + baseCost;

                                if (candidate < best)
                                {
                                    best = candidate;
                                }
                            }
                        }
                        else
                        {
                            // Case B: currentConfig != prevConfig
                            // -----------------------------------
                            // Then stage i starts a NEW block.
                            // So the number of blocks increases by 1.
                            // Therefore, the previous state must have had blocksUsed - 1 blocks.
                            if (blocksUsed > 1)
                            {
                                long prevCostNewBlock = prev[blocksUsed - 1, prevConfig];

                                if (prevCostNewBlock != INF)
                                {
                                    long candidate = prevCostNewBlock
                                                   + transition[prevConfig][currentConfig]
                                                   + baseCost;

                                    if (candidate < best)
                                    {
                                        best = candidate;
                                    }
                                }
                            }
                        }
                    }

                    curr[blocksUsed, currentConfig] = best;
                }
            }

            // Move current layer into prev for the next iteration.
            prev = curr;
        }

        // -----------------------------------------
        // STEP 3: Extract the answer
        // -----------------------------------------
        // We need exactly k blocks after processing all n stages.
        // The last configuration can be anything valid.
        long answer = INF;

        for (int c = 0; c < m; c++)
        {
            if (prev[k, c] < answer)
            {
                answer = prev[k, c];
            }
        }

        return answer == INF ? -1 : answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int n1 = 4, m1 = 3, k1 = 2;
int[][] cost1 =
{
    new[] { 5, 1, 4 },
    new[] { 2, 3, 6 },
    new[] { 7, 2, 1 },
    new[] { 4, 6, 3 }
};
int[][] transition1 =
{
    new[] { 0, 5, 2 },
    new[] { 4, 0, 3 },
    new[] { 6, 1, 0 }
};
int[] required1 = { -1, -1, -1, -1 };

long result1 = solution.MinimumCost(n1, m1, k1, cost1, transition1, required1);
Console.WriteLine(result1);

// Example 2
int n2 = 5, m2 = 2, k2 = 3;
int[][] cost2 =
{
    new[] { 3, 8 },
    new[] { 5, 1 },
    new[] { 2, 4 },
    new[] { 7, 2 },
    new[] { 6, 3 }
};
int[][] transition2 =
{
    new[] { 0, 10 },
    new[] { 1, 0 }
};
int[] required2 = { 0, -1, -1, 1, -1 };

long result2 = solution.MinimumCost(n2, m2, k2, cost2, transition2, required2);
Console.WriteLine(result2);