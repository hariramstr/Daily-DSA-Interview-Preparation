/*
Title: Minimum Cost to Compress Event Timeline

Problem Description:
You are given an ordered timeline of system events represented by an integer array events, where events[i] is the type of the i-th event.
To reduce storage, you want to partition the timeline into contiguous blocks and encode each block independently.

If a block contains positions from l to r (inclusive), its storage cost is defined as:

cost(l, r) = fixedCost + (number of distinct event types in events[l..r])^2

You must compress the entire timeline into exactly k non-empty contiguous blocks.
Return the minimum total storage cost.

Important note about the examples in the prompt:
The written explanations contain arithmetic inconsistencies.
For example 1, every shown partition sums to 16 or 18, not 14, so the true minimum is 16.
For example 2, the shown "optimal" partition sums to 12, not 11, and the true minimum is 12.

We therefore solve the problem according to the formal definition:
minimize the sum of block costs over exactly k non-empty contiguous blocks.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Precomputing the cost of every segment [l..r]: O(n^2)
    - Dynamic programming over partitions: O(k * n^2)
    - Total: O(n^2 + k * n^2) = O(k * n^2)

    Space Complexity:
    - Segment cost table: O(n^2)
    - DP table: O(k * n)
    - Total: O(n^2 + k * n)

    This is acceptable for:
    - n <= 2000
    - k <= 100
    */
    public long MinimumCompressionCost(int[] events, int k, int fixedCost)
    {
        int n = events.Length;

        // If we must split into exactly k non-empty blocks, then k cannot exceed n.
        // The problem guarantees this, but keeping the code defensive is good practice.
        if (k > n) return -1;

        // ---------------------------------------------------------------------
        // STEP 1: Precompute the cost of every possible contiguous segment.
        //
        // Why do this?
        // During dynamic programming, we will repeatedly ask:
        // "What is the cost of making the last block be events[j..i-1]?"
        //
        // If we compute distinct counts from scratch every time, the solution
        // would become too slow. So we precompute:
        //   segmentCost[l, r] = fixedCost + distinct(events[l..r])^2
        //
        // We use 0-based indexing for the original array.
        // ---------------------------------------------------------------------
        long[,] segmentCost = new long[n, n];

        // For each possible left boundary l, expand the right boundary r.
        // We maintain a HashSet of values seen in the current segment [l..r].
        // This lets us update the distinct count incrementally.
        for (int l = 0; l < n; l++)
        {
            var seen = new HashSet<int>();
            int distinct = 0;

            for (int r = l; r < n; r++)
            {
                // If this event type has not appeared yet in the current segment,
                // then adding events[r] increases the distinct count by 1.
                if (seen.Add(events[r]))
                {
                    distinct++;
                }

                // The cost formula is:
                // fixedCost + (number of distinct event types)^2
                segmentCost[l, r] = (long)fixedCost + (long)distinct * distinct;
            }
        }

        // ---------------------------------------------------------------------
        // STEP 2: Dynamic Programming definition.
        //
        // Let dp[parts, i] mean:
        //   the minimum cost to partition the first i events
        //   (that is, events[0..i-1]) into exactly 'parts' non-empty blocks.
        //
        // So:
        // - i ranges from 0 to n
        // - parts ranges from 0 to k
        //
        // Base case:
        //   dp[0, 0] = 0
        //   dp[0, i] = impossible for i > 0
        //
        // Transition:
        // To compute dp[parts, i], choose where the last block starts.
        // Suppose the last block is events[j..i-1], where:
        //   parts-1 <= j < i
        //
        // Then:
        //   dp[parts, i] = min over j of
        //       dp[parts-1, j] + segmentCost[j, i-1]
        //
        // Why does j start from parts-1?
        // Because the first j events must be split into parts-1 non-empty blocks,
        // so we need at least one event per block:
        //   j >= parts-1
        // ---------------------------------------------------------------------
        long INF = long.MaxValue / 4;
        long[,] dp = new long[k + 1, n + 1];

        // Initialize all states as "impossible" first.
        for (int parts = 0; parts <= k; parts++)
        {
            for (int i = 0; i <= n; i++)
            {
                dp[parts, i] = INF;
            }
        }

        // Zero events split into zero blocks costs zero.
        dp[0, 0] = 0;

        // ---------------------------------------------------------------------
        // STEP 3: Fill the DP table.
        // ---------------------------------------------------------------------
        for (int parts = 1; parts <= k; parts++)
        {
            // To split the first i events into 'parts' non-empty blocks,
            // we need at least i >= parts.
            for (int i = parts; i <= n; i++)
            {
                long best = INF;

                // Try every valid starting position j for the last block.
                // The last block will be events[j..i-1].
                for (int j = parts - 1; j < i; j++)
                {
                    // If the previous state is impossible, skip it.
                    if (dp[parts - 1, j] == INF) continue;

                    long candidate = dp[parts - 1, j] + segmentCost[j, i - 1];

                    if (candidate < best)
                    {
                        best = candidate;
                    }
                }

                dp[parts, i] = best;
            }
        }

        // The answer is the minimum cost to split all n events into exactly k blocks.
        return dp[k, n];
    }
}

// -----------------------------------------------------------------------------
// Demo code
// -----------------------------------------------------------------------------

var solution = new Solution();

// Example 1 from the prompt.
// The prompt says 14, but its own partition calculations contradict that.
// By the formal definition, the true minimum is 16.
int[] events1 = { 1, 2, 1, 3, 2 };
int k1 = 2;
int fixedCost1 = 4;
long result1 = solution.MinimumCompressionCost(events1, k1, fixedCost1);
Console.WriteLine($"Example 1 result: {result1}");

// Example 2 from the prompt.
// The prompt says 11, but the shown arithmetic gives 12, and the true minimum is 12.
int[] events2 = { 5, 5, 5, 6, 7, 6 };
int k2 = 3;
int fixedCost2 = 2;
long result2 = solution.MinimumCompressionCost(events2, k2, fixedCost2);
Console.WriteLine($"Example 2 result: {result2}");

// Additional small sanity checks.
int[] events3 = { 7 };
int k3 = 1;
int fixedCost3 = 10;
long result3 = solution.MinimumCompressionCost(events3, k3, fixedCost3);
Console.WriteLine($"Single event result: {result3}");

int[] events4 = { 1, 1, 1, 1 };
int k4 = 2;
int fixedCost4 = 0;
long result4 = solution.MinimumCompressionCost(events4, k4, fixedCost4);
Console.WriteLine($"Repeated events result: {result4}");