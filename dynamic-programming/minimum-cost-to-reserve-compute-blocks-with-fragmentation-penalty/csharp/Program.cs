/*
Title: Minimum Cost to Reserve Compute Blocks with Fragmentation Penalty
Difficulty: Hard
Topic: Dynamic Programming

Problem Description:
A cloud platform sells compute capacity for the next n hours. For hour i, you must reserve exactly demand[i] units of compute. Instead of buying capacity hour by hour, you may purchase reservation blocks. A block is defined by a contiguous interval of hours [l, r] and a fixed capacity c, meaning that for every hour from l to r you receive exactly c units from that block. Multiple blocks may overlap, and the total reserved capacity at each hour must equal the required demand exactly.

The cost of one block [l, r] with capacity c is:
setupCost + c * (r - l + 1) + fragmentationPenalty * max(0, c - min(demand[l..r]))

The first term is a fixed fee for creating a block, the second term is the usage cost, and the third term penalizes over-provisioning relative to the minimum demand inside the covered interval. Intuitively, a block that spans a low-demand hour cannot cheaply carry a high capacity.

Your task is to compute the minimum total cost needed to satisfy the full demand array exactly.

Constraints:
- 1 <= n <= 200
- 0 <= demand[i] <= 10^6
- 1 <= setupCost <= 10^6
- 0 <= fragmentationPenalty <= 10^6
- The answer fits in a 64-bit signed integer.

Example 1:
Input: demand = [3,1,3], setupCost = 2, fragmentationPenalty = 4
Output: 13

Example 2:
Input: demand = [2,2,2,2], setupCost = 5, fragmentationPenalty = 3
Output: 13

Notes:
- Hours are 0-indexed in the explanation only.
- A valid solution may use any number of blocks.
- Exact equality is required at every hour; reserving more or less than demand at any hour is invalid.
- Designing an efficient solution typically requires interval dynamic programming, because a naive search over all block decompositions is exponential.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n^3)
    Space Complexity: O(n^2)

    Core idea:
    ----------
    We solve this with interval dynamic programming.

    For any interval [l, r], let dp[l, r] be the minimum cost to satisfy exactly
    the demand on that interval using any collection of valid blocks fully contained
    in [l, r].

    The key observation is this:
    - In any optimal solution for [l, r], consider the "bottom-most" capacity layer
      that spans the entire interval. Its height can be at most min(demand[l..r]).
    - If we choose to place x units of capacity across the whole interval, where
      1 <= x <= min(demand[l..r]), then:
          * each such unit can be bundled into one block [l, r] of capacity x
          * because x <= min(demand[l..r]), the fragmentation penalty is zero
          * cost becomes setupCost + x * (r - l + 1)
      and then we still need to satisfy the remaining demand:
          demand'[i] = demand[i] - x for i in [l, r]

    However, doing this directly on modified arrays would be expensive.

    Better structural observation:
    - Let m = min(demand[l..r]).
    - In an optimal construction, it is enough to consider either:
        1) Split the interval into two parts: [l, k] and [k+1, r]
        2) Build one block [l, r] of capacity m, then recursively solve only the
           sub-intervals where demand is above m.
    - Why only capacity m for the full interval?
      Because any full-interval block with capacity c > m would pay an extra
      fragmentation penalty of penalty * (c - m), which is equivalent to paying
      extra for carrying capacity above the minimum through low-demand hours.
      Structurally, that extra capacity is always better handled by recursive
      subproblems on the higher-demand segments above the minimum level.
      Therefore the optimal "base layer" spanning the whole interval is exactly m.

    This transforms the problem into a classic "paint fence / strange printer style"
    interval DP over heights:
    - Pay for a full-width base block of height m:
          setupCost + m * length
    - Then remove that base from the interval.
    - The remaining positive parts form disjoint sub-intervals, each solved recursively.
    - Also compare against all binary splits.

    Important note about fragmentation penalty:
    ------------------------------------------
    Under the above decomposition, every full-interval base block uses capacity equal
    to the minimum demand of that interval, so its fragmentation penalty is zero.
    Any attempt to use a larger full-interval capacity is dominated by handling the
    excess in sub-intervals. Therefore the penalty is implicitly respected by the DP,
    and the optimal solution never needs a spanning block with capacity above the
    interval minimum.

    This matches the examples:
    - [3,1,3], setup=2, penalty=4
      Base on [0,2]: m=1 => cost 2 + 1*3 = 5
      Remaining positive segments after subtracting 1: [2,0,2]
      Solve [0,0]: 2 + 2 = 4
      Solve [2,2]: 2 + 2 = 4
      Total = 13
    - [2,2,2,2], setup=5, penalty=3
      Base on [0,3]: m=2 => 5 + 2*4 = 13
      No remainder => total 13
    */
    public long MinimumCost(int[] demand, long setupCost, long fragmentationPenalty)
    {
        int n = demand.Length;

        // We precompute the minimum demand for every interval [l, r].
        // This allows us to answer "what is min(demand[l..r])?" in O(1) during DP.
        // Since n <= 200, an O(n^2) precomputation is perfectly fine.
        int[,] minInRange = new int[n, n];
        for (int l = 0; l < n; l++)
        {
            int currentMin = int.MaxValue;
            for (int r = l; r < n; r++)
            {
                currentMin = Math.Min(currentMin, demand[r]);
                minInRange[l, r] = currentMin;
            }
        }

        // dp[l, r] = minimum cost to satisfy demand exactly on interval [l, r].
        long[,] dp = new long[n, n];

        // We process intervals by increasing length so that when we compute dp[l, r],
        // all smaller sub-intervals have already been computed.
        for (int len = 1; len <= n; len++)
        {
            for (int l = 0; l + len - 1 < n; l++)
            {
                int r = l + len - 1;

                // Start with a very large value because we are minimizing.
                long best = long.MaxValue / 4;

                // ------------------------------------------------------------
                // Option 1: Split the interval into two non-empty parts.
                // ------------------------------------------------------------
                // Why this is necessary:
                // Some optimal solutions do not use any block spanning the entire
                // interval [l, r]. Instead, they may be composed of independent
                // solutions on left and right parts.
                //
                // We must consider every possible split point k:
                //   [l..k] + [k+1..r]
                //
                // This is the standard interval DP transition.
                for (int k = l; k < r; k++)
                {
                    best = Math.Min(best, dp[l, k] + dp[k + 1, r]);
                }

                // ------------------------------------------------------------
                // Option 2: Build one "base layer" block across the whole interval.
                // ------------------------------------------------------------
                // Let m be the minimum demand in [l, r].
                // Then we can place one block [l, r] with capacity m.
                //
                // Cost of that block:
                //   setupCost + m * length + fragmentationPenalty * max(0, m - m)
                // = setupCost + m * length
                //
                // The penalty is zero because the block capacity equals the interval minimum.
                int m = minInRange[l, r];
                long costUsingWholeIntervalBase = setupCost + (long)m * len;

                // After placing this base block, every hour in [l, r] has m units covered.
                // The remaining demand is:
                //   demand[i] - m
                //
                // Any hour where demand[i] == m is now fully satisfied.
                // Any maximal contiguous region where demand[i] > m still needs extra capacity.
                //
                // Those regions are independent subproblems because no additional block
                // needs to cross an hour whose remaining demand is zero.
                int i = l;
                while (i <= r)
                {
                    // Skip hours that are already fully satisfied after removing the base layer.
                    if (demand[i] == m)
                    {
                        i++;
                        continue;
                    }

                    // We found the start of a positive remainder segment.
                    int start = i;

                    // Extend until the remainder drops back to zero.
                    while (i <= r && demand[i] > m)
                    {
                        i++;
                    }

                    int end = i - 1;

                    // Add the optimal cost for this sub-interval.
                    costUsingWholeIntervalBase += dp[start, end];
                }

                best = Math.Min(best, costUsingWholeIntervalBase);

                // Store the best answer for interval [l, r].
                dp[l, r] = best;
            }
        }

        return dp[0, n - 1];
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] demand1 = { 3, 1, 3 };
long setupCost1 = 2;
long fragmentationPenalty1 = 4;
long result1 = solution.MinimumCost(demand1, setupCost1, fragmentationPenalty1);
Console.WriteLine(result1); // Expected: 13

// Example 2
int[] demand2 = { 2, 2, 2, 2 };
long setupCost2 = 5;
long fragmentationPenalty2 = 3;
long result2 = solution.MinimumCost(demand2, setupCost2, fragmentationPenalty2);
Console.WriteLine(result2); // Expected: 13