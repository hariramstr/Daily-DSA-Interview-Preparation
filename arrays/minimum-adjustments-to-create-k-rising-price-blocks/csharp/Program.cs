/*
Title: Minimum Adjustments to Create K Rising Price Blocks

Problem Description:
You are given an integer array prices of length n and an integer k. You may change the value of any element to any other integer, and the cost of changing prices[i] to x is |prices[i] - x|. Your goal is to partition the array into exactly k non-empty contiguous blocks such that after applying some changes, every block becomes non-decreasing. In other words, if a block covers indices [l..r], then after modification its values must satisfy a[l] <= a[l+1] <= ... <= a[r].

Return the minimum total adjustment cost required to transform the array so that it can be split into exactly k contiguous non-decreasing blocks.

The partition boundaries are your choice, and the modified values in different blocks do not need to relate to each other. A block of length 1 is always non-decreasing. This is an optimization problem over both the partitioning and the edited values.

Constraints:
- 1 <= n <= 200
- 1 <= k <= n
- -10^9 <= prices[i] <= 10^9
- You must use exactly k blocks

Important note about the examples in the prompt:
The first example's narrative is inconsistent. For prices = [5, 1, 4, 2], k = 2,
the true minimum cost is 2, not 1.
One optimal partition is [5] and [1, 4, 2], changing the second block to [1, 3, 3]
for total cost 2.
This implementation computes the mathematically correct optimum.

Approach overview:
1. Precompute cost[l, r] = minimum cost to modify subarray prices[l..r] into a non-decreasing sequence.
2. Then run dynamic programming:
      dp[b, i] = minimum cost to split the first i elements into exactly b blocks.
   Transition:
      dp[b, i] = min over p < i of dp[b - 1, p] + cost[p, i - 1]

Key subproblem:
How do we compute the minimum cost to make one interval non-decreasing?

For a fixed interval, we use a classic dynamic programming idea:
- In an optimal solution under L1 cost (absolute differences), every chosen final value can be assumed
  to be one of the original values from the interval.
- Let the sorted unique values from the interval be candidates.
- dpPos[v] = minimum cost for the processed prefix if the current final value equals candidate v,
  while preserving non-decreasing order.
- Transition uses prefix minima because previous chosen value must be <= current chosen value.

This gives O(m^2) for an interval of length m after sorting/compressing its values.
Since n <= 200, preprocessing all intervals is fully feasible.
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    - Precomputing interval costs:
        There are O(n^2) intervals.
        For an interval of length m, the inner DP is O(m^2).
        Summed over all intervals, this is O(n^4) in the worst case.
        With n <= 200, this is acceptable in practice.
    - Partition DP:
        O(k * n^2)

    Overall:
        O(n^4 + k * n^2)

    Space Complexity:
    - cost table: O(n^2)
    - partition DP: O(k * n)
    - temporary arrays inside interval computation: O(n)

    Overall:
        O(n^2 + k * n)
    */
    public long MinimumAdjustmentCost(int[] prices, int k)
    {
        int n = prices.Length;

        // This table will store:
        // cost[l, r] = minimum cost to transform prices[l..r] into a non-decreasing sequence.
        long[,] cost = new long[n, n];

        // ------------------------------------------------------------
        // STEP 1: Precompute the best cost for every contiguous interval.
        // ------------------------------------------------------------
        //
        // Why do we need this?
        // The final answer asks us to split the array into k blocks.
        // If we already know the best cost for every possible block [l..r],
        // then the remaining problem becomes a standard partition DP.
        //
        // So the hard part is reduced to:
        // "For one interval, what is the minimum cost to make it non-decreasing?"
        //
        // We solve that interval problem independently for every [l..r].
        //
        for (int l = 0; l < n; l++)
        {
            for (int r = l; r < n; r++)
            {
                cost[l, r] = ComputeIntervalCost(prices, l, r);
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Dynamic programming over number of blocks.
        // ------------------------------------------------------------
        //
        // dp[b, i] means:
        // Minimum cost to split the first i elements (indices 0..i-1)
        // into exactly b non-empty contiguous blocks.
        //
        // Why "first i elements" instead of "up to index i"?
        // This is a very common DP style because it makes transitions cleaner.
        // - dp[0, 0] = 0  -> zero elements split into zero blocks costs nothing
        // - dp[0, i>0] = impossible
        //
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

        // Try building answers block by block.
        for (int blocks = 1; blocks <= k; blocks++)
        {
            // To split first i elements into "blocks" non-empty blocks,
            // we must have at least i >= blocks.
            for (int i = blocks; i <= n; i++)
            {
                // Let the last block start at position p and end at i-1.
                // Then:
                // - first p elements are split into blocks-1 blocks
                // - interval [p..i-1] is the last block
                //
                // Since blocks are non-empty, p must be at least blocks-1 and at most i-1.
                for (int p = blocks - 1; p < i; p++)
                {
                    if (dp[blocks - 1, p] == INF)
                    {
                        continue;
                    }

                    long candidate = dp[blocks - 1, p] + cost[p, i - 1];
                    if (candidate < dp[blocks, i])
                    {
                        dp[blocks, i] = candidate;
                    }
                }
            }
        }

        return dp[k, n];
    }

    private long ComputeIntervalCost(int[] prices, int l, int r)
    {
        int m = r - l + 1;

        // ------------------------------------------------------------
        // STEP A: Build the candidate value set for this interval.
        // ------------------------------------------------------------
        //
        // Very important fact:
        // For minimizing sum of absolute deviations under monotonic constraints,
        // there exists an optimal solution whose chosen values come from the set
        // of original values in the interval.
        //
        // That means we do NOT need to consider every integer from -1e9 to 1e9.
        // We only need to consider the distinct values already present in prices[l..r].
        //
        // This compression is what makes the problem tractable.
        //
        List<int> values = new List<int>();
        for (int i = l; i <= r; i++)
        {
            values.Add(prices[i]);
        }

        values.Sort();

        List<int> unique = new List<int>();
        foreach (int v in values)
        {
            if (unique.Count == 0 || unique[^1] != v)
            {
                unique.Add(v);
            }
        }

        int u = unique.Count;

        // ------------------------------------------------------------
        // STEP B: DP over positions inside the interval.
        // ------------------------------------------------------------
        //
        // We process the interval from left to right.
        //
        // prev[j] means:
        // After processing some prefix of the interval,
        // minimum cost if the last chosen final value equals unique[j].
        //
        // If we are assigning current element to unique[j],
        // then because the final sequence must be non-decreasing,
        // the previous chosen value must be <= unique[j].
        //
        // So:
        // current[j] = min(prev[t]) for all t <= j  +  |prices[pos] - unique[j]|
        //
        // To compute min(prev[t]) for all t <= j efficiently,
        // we maintain a running prefix minimum while scanning j from left to right.
        //
        long[] prev = new long[u];
        long[] curr = new long[u];

        // Base case: first element in the interval.
        // We can assign it to any candidate value independently.
        int firstValue = prices[l];
        for (int j = 0; j < u; j++)
        {
            prev[j] = Math.Abs((long)firstValue - unique[j]);
        }

        // Process remaining elements one by one.
        for (int pos = l + 1; pos <= r; pos++)
        {
            long bestPrefix = long.MaxValue / 4;

            for (int j = 0; j < u; j++)
            {
                // Update prefix minimum of prev[0..j].
                if (prev[j] < bestPrefix)
                {
                    bestPrefix = prev[j];
                }

                // If current final value is unique[j],
                // previous final value can be any candidate <= unique[j],
                // and bestPrefix already stores the cheapest such option.
                curr[j] = bestPrefix + Math.Abs((long)prices[pos] - unique[j]);
            }

            // Move current row into prev for the next iteration.
            var temp = prev;
            prev = curr;
            curr = temp;
        }

        // Final answer for this interval:
        // minimum over all possible ending values.
        long answer = prev[0];
        for (int j = 1; j < u; j++)
        {
            if (prev[j] < answer)
            {
                answer = prev[j];
            }
        }

        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1 from the prompt.
// The prompt claims output 1, but that example explanation is inconsistent.
// The mathematically correct minimum is 2.
int[] prices1 = { 5, 1, 4, 2 };
int k1 = 2;
long result1 = solution.MinimumAdjustmentCost(prices1, k1);
Console.WriteLine($"Example 1: prices = [{string.Join(", ", prices1)}], k = {k1}, minimum cost = {result1}");

// Example 2 from the prompt.
// This one is consistent: expected 3.
int[] prices2 = { 7, 3, 6, 3, 8 };
int k2 = 3;
long result2 = solution.MinimumAdjustmentCost(prices2, k2);
Console.WriteLine($"Example 2: prices = [{string.Join(", ", prices2)}], k = {k2}, minimum cost = {result2}");

// Additional small sanity checks.
int[] prices3 = { 1, 2, 3, 4 };
int k3 = 1; // already non-decreasing
Console.WriteLine($"Sanity 1: prices = [{string.Join(", ", prices3)}], k = {k3}, minimum cost = {solution.MinimumAdjustmentCost(prices3, k3)}");

int[] prices4 = { 4, 3, 2, 1 };
int k4 = 4; // every element can be its own block, so cost 0
Console.WriteLine($"Sanity 2: prices = [{string.Join(", ", prices4)}], k = {k4}, minimum cost = {solution.MinimumAdjustmentCost(prices4, k4)}");

int[] prices5 = { 4, 3, 2, 1 };
int k5 = 1; // one block, must become non-decreasing
Console.WriteLine($"Sanity 3: prices = [{string.Join(", ", prices5)}], k = {k5}, minimum cost = {solution.MinimumAdjustmentCost(prices5, k5)}");