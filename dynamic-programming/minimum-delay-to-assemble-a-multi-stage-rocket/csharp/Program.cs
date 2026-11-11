/*
Title: Minimum Delay to Assemble a Multi-Stage Rocket

Problem Description:
A space manufacturing team must assemble a rocket from stage 1 to stage n in order.
For each stage i, we are given:
- buildTime[i]: the number of hours required to physically assemble that stage
- stability[i]: an integer rating of how stable that stage is once completed

The team may group consecutive stages into a single assembly batch.
If stages l..r are assembled in one batch, the batch cost is:

sum(buildTime[l..r]) + (max(stability[l..r]) - min(stability[l..r]))

We must partition the n stages into exactly k non-empty consecutive batches.
Return the minimum possible total cost.

Constraints:
- 1 <= k <= n <= 300
- 1 <= buildTime[i] <= 10^6
- 1 <= stability[i] <= 10^9
- The answer fits in a 64-bit signed integer.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Precomputing every interval cost: O(n^2)
    - Dynamic programming transitions: O(k * n^2)
    - Total: O(k * n^2 + n^2) = O(k * n^2)

    Space Complexity:
    - Interval cost table: O(n^2)
    - DP table: O(k * n)
    - Total: O(n^2 + k * n)

    Beginner-friendly idea:
    1. First, compute the cost of every possible batch [l..r].
    2. Then use dynamic programming:
       dp[b][i] = minimum cost to partition the first i stages into exactly b batches.
    3. To compute dp[b][i], try every possible last batch start position j:
       - first b-1 batches cover stages 1..j
       - last batch covers stages j+1..i
       So:
       dp[b][i] = min over j from b-1 to i-1 of dp[b-1][j] + cost[j+1][i]
    */
    public long MinimumTotalCost(int[] buildTime, int[] stability, int k)
    {
        int n = buildTime.Length;

        // We will use 1-based indexing inside the algorithm because interval DP
        // is often easier to read when stages are numbered from 1 to n.
        // This avoids many off-by-one mistakes when talking about ranges [l..r].
        long[] build = new long[n + 1];
        long[] stab = new long[n + 1];

        for (int i = 1; i <= n; i++)
        {
            build[i] = buildTime[i - 1];
            stab[i] = stability[i - 1];
        }

        // ------------------------------------------------------------
        // STEP 1: Precompute the cost of every interval [l..r].
        // ------------------------------------------------------------
        //
        // Why do we do this?
        // During dynamic programming, we will repeatedly ask:
        // "What is the cost of making stages l..r into one batch?"
        //
        // If we recompute sum/max/min every time, the DP would become too slow.
        // Since n <= 300, we can afford an O(n^2) table of interval costs.
        //
        // cost[l, r] will store:
        // sum(build[l..r]) + (max(stab[l..r]) - min(stab[l..r]))
        //
        // We compute this by fixing l, then extending r one step at a time.
        // While extending, we maintain:
        // - runningSum
        // - currentMax
        // - currentMin
        //
        // This makes each fixed starting point l cost O(n), so total O(n^2).
        long[,] cost = new long[n + 1, n + 1];

        for (int l = 1; l <= n; l++)
        {
            long runningSum = 0;
            long currentMax = long.MinValue;
            long currentMin = long.MaxValue;

            for (int r = l; r <= n; r++)
            {
                // Add the new stage r into the current interval [l..r].
                runningSum += build[r];

                // Update the maximum and minimum stability seen in this interval.
                if (stab[r] > currentMax) currentMax = stab[r];
                if (stab[r] < currentMin) currentMin = stab[r];

                // Store the full batch cost for interval [l..r].
                cost[l, r] = runningSum + (currentMax - currentMin);
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Dynamic Programming over prefixes and batch count.
        // ------------------------------------------------------------
        //
        // dp[b, i] means:
        // Minimum total cost to partition the first i stages (1..i)
        // into exactly b non-empty consecutive batches.
        //
        // Important:
        // - i ranges from 0..n
        // - b ranges from 0..k
        //
        // Base case:
        // dp[0, 0] = 0
        // Meaning: zero stages split into zero batches costs zero.
        //
        // Impossible states:
        // - dp[0, i] for i > 0 is impossible
        // - dp[b, 0] for b > 0 is impossible
        //
        // We represent impossible states with a very large number INF.
        long INF = long.MaxValue / 4;
        long[,] dp = new long[k + 1, n + 1];

        for (int b = 0; b <= k; b++)
        {
            for (int i = 0; i <= n; i++)
            {
                dp[b, i] = INF;
            }
        }

        dp[0, 0] = 0;

        // ------------------------------------------------------------
        // STEP 3: Fill the DP table.
        // ------------------------------------------------------------
        //
        // For each number of batches b from 1 to k,
        // and for each prefix length i from 1 to n,
        // we try all possible places where the last batch begins.
        //
        // Suppose the last batch is [j+1 .. i].
        // Then the first j stages must already be partitioned into b-1 batches.
        //
        // Transition:
        // dp[b, i] = min(
        //     dp[b-1, j] + cost[j+1, i]
        // ) for all valid j
        //
        // Valid j values:
        // - At least b-1 stages are needed to form b-1 non-empty batches
        //   so j >= b-1
        // - The last batch must be non-empty, so j < i
        //
        // Therefore:
        // j ranges from b-1 to i-1
        for (int b = 1; b <= k; b++)
        {
            // If we want exactly b non-empty batches, we need at least b stages.
            for (int i = b; i <= n; i++)
            {
                long best = INF;

                for (int j = b - 1; j <= i - 1; j++)
                {
                    // If the previous state is impossible, skip it.
                    if (dp[b - 1, j] == INF)
                    {
                        continue;
                    }

                    // Candidate:
                    // cost for first j stages in b-1 batches
                    // + cost of making stages j+1..i the final batch
                    long candidate = dp[b - 1, j] + cost[j + 1, i];

                    if (candidate < best)
                    {
                        best = candidate;
                    }
                }

                dp[b, i] = best;
            }
        }

        // The answer is the minimum cost to partition all n stages into exactly k batches.
        return dp[k, n];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] buildTime1 = { 4, 2, 7, 3 };
int[] stability1 = { 8, 5, 6, 10 };
int k1 = 2;
long result1 = solution.MinimumTotalCost(buildTime1, stability1, k1);
Console.WriteLine(result1);

// Example 2
int[] buildTime2 = { 5, 1, 4, 2, 6 };
int[] stability2 = { 7, 7, 3, 9, 4 };
int k2 = 3;
long result2 = solution.MinimumTotalCost(buildTime2, stability2, k2);
Console.WriteLine(result2);

// Additional small sanity check:
// If k = n, every stage is its own batch, so each batch cost is just buildTime[i].
int[] buildTime3 = { 3, 8, 2 };
int[] stability3 = { 100, 1, 50 };
int k3 = 3;
long result3 = solution.MinimumTotalCost(buildTime3, stability3, k3);
Console.WriteLine(result3);