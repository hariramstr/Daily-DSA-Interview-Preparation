/*
Title: Minimum Penalty to Build a Monotone API Rollout Plan

Problem Description:
A backend team is rolling out a new API across N consecutive regions, numbered from 0 to N - 1.
For each region i, the team must choose exactly one rollout level level[i] from 1 to M.
Higher levels enable more features but may cause instability in some regions.
The instability penalty of assigning level j to region i is given by cost[i][j - 1].

To simplify support and documentation, the final rollout plan must be monotone:
if region a comes before region b, then the chosen level for region a cannot be greater than
the chosen level for region b. In other words, the sequence
level[0], level[1], ..., level[N - 1] must be nondecreasing.

Your task is to compute the minimum possible total instability penalty over all valid monotone rollout plans.

Return the minimum total penalty.

Constraints:
- 1 <= N <= 2000
- 1 <= M <= 2000
- 1 <= N * M <= 4 * 10^6
- 0 <= cost[i][j] <= 10^9
- The answer may exceed 32-bit integer range, so use 64-bit integers.

Examples:
1) cost = [[5,1,4],[6,2,3],[7,3,1]]
   Output: 4
   Explanation: Choose rollout levels [2,2,3]. Penalties = 1 + 2 + 1 = 4.

2) cost = [[1,10,100],[100,1,10],[100,10,1],[1000,1,1]]
   Output: 4
   Explanation: One optimal plan is [1,2,3,3]. Penalties = 1 + 1 + 1 + 1 = 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(N * M)
      We process each cell of the cost matrix once, and for each row we compute prefix minima in O(M).

    Space Complexity:
    - O(M)
      We only keep the previous DP row and the current DP row, instead of storing all N rows.

    Idea:
    Let dp[i][j] mean:
    "Minimum total penalty for regions 0..i, if region i is assigned level j (0-based level index),
     and the chosen levels are nondecreasing."

    Transition:
    Since the sequence must be nondecreasing, if region i uses level j,
    then region i - 1 must use some level k where k <= j.

    So:
    dp[i][j] = cost[i][j] + min(dp[i - 1][0..j])

    A direct implementation would scan 0..j for every j, which would be O(N * M^2).
    That is too slow.

    Optimization:
    For each row, compute prefixMin[j] = min(dpPrev[0..j]).
    Then:
    dpCurr[j] = cost[i][j] + prefixMin[j]

    This reduces the transition to O(1) per state, giving total O(N * M).
    */
    public long MinimumPenalty(int[][] cost)
    {
        // Number of regions.
        int n = cost.Length;

        // Number of possible rollout levels.
        int m = cost[0].Length;

        // dpPrev[j] will store the DP values for the previous region.
        // Specifically:
        // dpPrev[j] = minimum total penalty for processed regions so far,
        //             ending with level j at the previous region.
        long[] dpPrev = new long[m];

        // Base case:
        // For region 0, there are no previous regions, so if we choose level j,
        // the total penalty is simply cost[0][j].
        for (int j = 0; j < m; j++)
        {
            dpPrev[j] = cost[0][j];
        }

        // Process regions 1 through n - 1.
        for (int i = 1; i < n; i++)
        {
            // prefixMin[j] = minimum value among dpPrev[0], dpPrev[1], ..., dpPrev[j]
            // This is exactly what we need because if current region uses level j,
            // the previous region may use any level <= j.
            long[] prefixMin = new long[m];

            // Build prefix minima from left to right.
            prefixMin[0] = dpPrev[0];
            for (int j = 1; j < m; j++)
            {
                prefixMin[j] = Math.Min(prefixMin[j - 1], dpPrev[j]);
            }

            // dpCurr[j] will store the DP values for the current region i.
            long[] dpCurr = new long[m];

            // Compute the current row using the optimized transition.
            for (int j = 0; j < m; j++)
            {
                // Current step:
                // We choose level j for region i.
                //
                // Why this works:
                // To keep the sequence nondecreasing, the previous region's level
                // must be at most j. Among all such valid previous endings,
                // prefixMin[j] already stores the minimum total penalty.
                //
                // Then we add the penalty of assigning level j to region i.
                dpCurr[j] = prefixMin[j] + cost[i][j];
            }

            // Move current row into previous row for the next iteration.
            dpPrev = dpCurr;
        }

        // After processing all regions, the answer is the minimum value in the last DP row,
        // because the final region may end at any level.
        long answer = dpPrev[0];
        for (int j = 1; j < m; j++)
        {
            answer = Math.Min(answer, dpPrev[j]);
        }

        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[][] cost1 =
{
    new[] { 5, 1, 4 },
    new[] { 6, 2, 3 },
    new[] { 7, 3, 1 }
};

long result1 = solution.MinimumPenalty(cost1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[][] cost2 =
{
    new[] { 1, 10, 100 },
    new[] { 100, 1, 10 },
    new[] { 100, 10, 1 },
    new[] { 1000, 1, 1 }
};

long result2 = solution.MinimumPenalty(cost2);
Console.WriteLine(result2); // Expected: 4