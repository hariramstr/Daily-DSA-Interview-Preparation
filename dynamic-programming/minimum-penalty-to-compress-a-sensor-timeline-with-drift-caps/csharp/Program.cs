/*
Title: Minimum Penalty to Compress a Sensor Timeline with Drift Caps

Problem Description:
You are given a time-ordered array readings of length n, where readings[i] is the value reported by a sensor at second i.
To reduce storage, you want to compress the timeline into contiguous blocks. Each block [l, r] is stored using a single
representative value, and every reading in that block is reconstructed as that representative value.

For any block [l, r], the representative value may be chosen arbitrarily, but the block is valid only if the difference
between the maximum and minimum reading inside the block is at most driftCap. If a block is valid, its compression penalty
is the sum of absolute differences between each reading in the block and the chosen representative value. Since the
representative may be chosen freely, the minimum penalty for a valid block is achieved by choosing a median of the readings
in that block.

You must partition the entire array into exactly k non-empty contiguous valid blocks and return the minimum possible total
compression penalty. If it is impossible to partition the array into exactly k valid blocks, return -1.

Constraints:
- 1 <= n <= 300
- 1 <= k <= n
- 0 <= readings[i] <= 10^9
- 0 <= driftCap <= 10^9
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    - Precomputing every subarray cost:
      There are O(n^2) subarrays.
      For each fixed left endpoint, we extend the right endpoint and maintain a sorted list.
      Insertion into a List with binary search + shifting is O(n), and computing the median-based
      absolute deviation by scanning the current sorted list is also O(n).
      So this phase is O(n^3) in the worst case.
    - Dynamic programming:
      O(k * n^2)

    Overall:
    - O(n^3 + k * n^2), which is acceptable for n <= 300.

    Space Complexity:
    - cost table: O(n^2)
    - dp table: O(k * n)
    - temporary sorted list during preprocessing: O(n)

    Overall:
    - O(n^2 + k * n)
    */
    public long MinimumPenalty(int[] readings, int k, int driftCap)
    {
        int n = readings.Length;
        const long INF = (long)4e18;

        // cost[l, r] will store the minimum penalty for compressing the contiguous block readings[l..r]
        // using one representative value, but ONLY if that block is valid under the drift cap.
        // If the block is invalid, we store INF to mean "cannot use this block".
        long[,] cost = new long[n, n];

        // First initialize every entry to INF.
        // This is important because many subarrays will be invalid, and we want DP transitions
        // to naturally ignore them.
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                cost[i, j] = INF;
            }
        }

        // ------------------------------------------------------------
        // STEP 1: Precompute the cost of every valid subarray [l..r].
        // ------------------------------------------------------------
        //
        // Why do we do this?
        // The DP later will repeatedly ask:
        // "If the last block is readings[p..i-1], what is its best compression penalty?"
        // If we compute that on the fly every time, the solution becomes too slow and messy.
        //
        // So we precompute all subarray costs once.
        //
        // For each left endpoint l:
        //   - We extend the right endpoint r from l to n-1.
        //   - We maintain:
        //       1) the minimum value in the current subarray
        //       2) the maximum value in the current subarray
        //       3) a sorted list of the current subarray values
        //
        // The sorted list lets us find a median easily.
        // For minimizing sum of absolute deviations, ANY median is optimal.
        // We choose the lower median at index count/2 after sorting.
        //
        // Since n is only 300, a simple sorted List<int> is perfectly fine and keeps the code beginner-friendly.
        for (int l = 0; l < n; l++)
        {
            var sorted = new List<int>();
            int currentMin = int.MaxValue;
            int currentMax = int.MinValue;

            for (int r = l; r < n; r++)
            {
                int value = readings[r];

                // Update min and max for the current subarray [l..r].
                // This is necessary to test the drift cap validity condition:
                // max(readings[l..r]) - min(readings[l..r]) <= driftCap
                if (value < currentMin) currentMin = value;
                if (value > currentMax) currentMax = value;

                // Insert the new value into the sorted list while keeping it sorted.
                // We use BinarySearch to find the insertion position.
                int pos = sorted.BinarySearch(value);
                if (pos < 0) pos = ~pos;
                sorted.Insert(pos, value);

                // Check whether this block is valid under the drift cap.
                if ((long)currentMax - currentMin > driftCap)
                {
                    // Invalid block: leave cost[l, r] as INF.
                    continue;
                }

                // The block is valid.
                // The minimum sum of absolute differences to a chosen representative
                // is achieved by any median.
                int median = sorted[sorted.Count / 2];

                long penalty = 0;

                // Compute sum |x - median| over the sorted values.
                // This is the exact minimum penalty for this valid block.
                for (int i = 0; i < sorted.Count; i++)
                {
                    penalty += Math.Abs((long)sorted[i] - median);
                }

                cost[l, r] = penalty;
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Dynamic Programming over prefixes and block counts.
        // ------------------------------------------------------------
        //
        // dp[parts, i] = minimum penalty to partition the first i readings
        //                (that is, readings[0..i-1]) into exactly 'parts' valid blocks.
        //
        // Important indexing detail:
        // - i is a prefix length, so i ranges from 0 to n.
        // - dp[0, 0] = 0 because zero readings split into zero blocks costs nothing.
        // - dp[0, i>0] is impossible.
        //
        // Transition:
        // To compute dp[parts, i], let the last block start at position p and end at i-1.
        // Then:
        //   dp[parts, i] = min over p from parts-1 to i-1 of
        //                  dp[parts-1, p] + cost[p, i-1]
        //
        // Why p starts from parts-1:
        // If we need 'parts-1' non-empty blocks before p, then the first p elements must contain
        // at least parts-1 elements.
        long[,] dp = new long[k + 1, n + 1];

        // Initialize all DP states to INF.
        for (int parts = 0; parts <= k; parts++)
        {
            for (int i = 0; i <= n; i++)
            {
                dp[parts, i] = INF;
            }
        }

        dp[0, 0] = 0;

        for (int parts = 1; parts <= k; parts++)
        {
            for (int i = 1; i <= n; i++)
            {
                long best = INF;

                // Try every possible starting point p for the last block [p..i-1].
                for (int p = parts - 1; p <= i - 1; p++)
                {
                    // If the prefix before p cannot be partitioned into parts-1 blocks,
                    // skip this transition.
                    if (dp[parts - 1, p] == INF)
                    {
                        continue;
                    }

                    // If the last block [p..i-1] is invalid, skip it.
                    if (cost[p, i - 1] == INF)
                    {
                        continue;
                    }

                    long candidate = dp[parts - 1, p] + cost[p, i - 1];
                    if (candidate < best)
                    {
                        best = candidate;
                    }
                }

                dp[parts, i] = best;
            }
        }

        // If dp[k, n] is still INF, then it is impossible to split the entire array
        // into exactly k valid contiguous blocks.
        return dp[k, n] == INF ? -1 : dp[k, n];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] readings1 = { 8, 10, 9, 3, 4 };
int k1 = 2;
int driftCap1 = 2;
long result1 = solution.MinimumPenalty(readings1, k1, driftCap1);
Console.WriteLine(result1);

// Example 2
int[] readings2 = { 1, 7, 2, 8 };
int k2 = 2;
int driftCap2 = 3;
long result2 = solution.MinimumPenalty(readings2, k2, driftCap2);
Console.WriteLine(result2);

// Additional quick sanity checks
int[] readings3 = { 5 };
int k3 = 1;
int driftCap3 = 0;
Console.WriteLine(solution.MinimumPenalty(readings3, k3, driftCap3));

int[] readings4 = { 1, 2, 3, 4 };
int k4 = 4;
int driftCap4 = 0;
Console.WriteLine(solution.MinimumPenalty(readings4, k4, driftCap4));