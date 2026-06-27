/*
Title: Minimum Cost to Balance a Multi-Zone Battery Schedule

Problem Description:
A data center campus is powered by a shared battery system across n time slots. During slot i,
the campus demand changes by delta[i], where a positive value means the battery must discharge
that many units to support the load, and a negative value means that many units can be stored
back into the battery from excess solar generation.

You may partition the timeline into exactly k contiguous zones. Inside each zone, you are allowed
to choose one target battery drift value t, and every delta in that zone must be adjusted to t
by buying or spilling energy. The cost of adjusting slot i inside that zone is |delta[i] - t|.

The total cost is the sum over all slots in all zones.

Your task is to compute the minimum possible total adjustment cost when dividing the array into
exactly k contiguous zones.

Important observation:
For any fixed zone, the value t that minimizes the sum of absolute differences is any median of
the values in that zone. Therefore, the problem becomes:

1. Precompute the minimum cost for every subarray [l..r].
2. Use dynamic programming to split the array into exactly k contiguous zones.

Constraints:
- 1 <= n <= 300
- 1 <= k <= min(n, 30)
- -10^6 <= delta[i] <= 10^6
- The answer fits in a signed 64-bit integer.
*/

using System;

class Solution
{
    /*
    Time Complexity:
    - Precomputing interval costs:
      For each starting index l, we extend the interval to every ending index r.
      For each interval, we copy and sort the values in that interval to find the median
      and compute the absolute-deviation cost.
      This is O(n^3 log n) in the straightforward educational implementation.
      With n <= 300, this is completely acceptable.

    - Dynamic Programming:
      dp[z, i] = minimum cost to split the first i elements into exactly z zones.
      Transition checks all previous cut positions, so this is O(k * n^2).

    Overall:
    O(n^3 log n + k * n^2)

    Space Complexity:
    - cost table: O(n^2)
    - dp table: O(k * n)
    Overall: O(n^2)
    */
    public long MinimumCostToBalanceSchedule(int[] delta, int k)
    {
        int n = delta.Length;

        // ------------------------------------------------------------
        // STEP 1: Precompute the minimum adjustment cost for every
        //         contiguous subarray delta[l..r].
        //
        // Why this is necessary:
        // The DP later will repeatedly ask:
        // "If the last zone is from j to i-1, what is the best possible
        //  cost of making all values in that zone equal to one target?"
        //
        // Instead of recomputing that cost every time during DP,
        // we compute it once for every interval and store it.
        //
        // cost[l, r] means:
        // minimum sum of |delta[x] - t| for x in [l..r], where t is chosen optimally.
        //
        // Since the optimal t for absolute deviations is a median,
        // we:
        //   1. collect the interval values,
        //   2. sort them,
        //   3. choose the median,
        //   4. sum distances to that median.
        // ------------------------------------------------------------
        long[,] cost = new long[n, n];

        for (int l = 0; l < n; l++)
        {
            // We build intervals starting at l and ending at r.
            // For beginner clarity, we keep a growing array of values.
            int[] values = new int[n - l];
            int length = 0;

            for (int r = l; r < n; r++)
            {
                // Add the new element delta[r] into the current interval [l..r].
                values[length++] = delta[r];

                // Copy only the active portion into a temporary array so we can sort it.
                int[] temp = new int[length];
                Array.Copy(values, temp, length);
                Array.Sort(temp);

                // For minimizing sum of absolute differences,
                // any median is optimal.
                // Using the lower/standard middle index works fine.
                int median = temp[length / 2];

                long intervalCost = 0;

                // Compute total absolute deviation from the chosen median.
                for (int i = 0; i < length; i++)
                {
                    intervalCost += Math.Abs((long)temp[i] - median);
                }

                cost[l, r] = intervalCost;
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Dynamic Programming over prefixes and number of zones.
        //
        // dp[z, i] = minimum cost to partition the first i elements
        //            (that is, delta[0..i-1]) into exactly z contiguous zones.
        //
        // Why prefix DP?
        // Because every valid partition of the first i elements must end
        // with some last zone [j..i-1], and before that we must have
        // partitioned the first j elements into z-1 zones.
        //
        // Transition:
        // dp[z, i] = min over j from z-1 to i-1 of
        //            dp[z-1, j] + cost[j, i-1]
        //
        // Explanation:
        // - j is the starting index of the last zone.
        // - The previous part is delta[0..j-1], which uses z-1 zones.
        // - The last zone is delta[j..i-1].
        //
        // Base case:
        // dp[0, 0] = 0
        // Meaning: zero elements split into zero zones costs zero.
        //
        // All other states start as "infinity" because they are not yet known.
        // ------------------------------------------------------------
        long INF = long.MaxValue / 4;
        long[,] dp = new long[k + 1, n + 1];

        for (int z = 0; z <= k; z++)
        {
            for (int i = 0; i <= n; i++)
            {
                dp[z, i] = INF;
            }
        }

        dp[0, 0] = 0;

        for (int zones = 1; zones <= k; zones++)
        {
            // We need at least 'zones' elements to form 'zones' non-empty zones.
            for (int i = zones; i <= n; i++)
            {
                long best = INF;

                // j is where the last zone begins.
                // To have zones-1 non-empty zones before j, we need j >= zones-1.
                for (int j = zones - 1; j <= i - 1; j++)
                {
                    if (dp[zones - 1, j] == INF)
                    {
                        continue;
                    }

                    long candidate = dp[zones - 1, j] + cost[j, i - 1];

                    if (candidate < best)
                    {
                        best = candidate;
                    }
                }

                dp[zones, i] = best;
            }
        }

        return dp[k, n];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] delta1 = { 4, 1, 7, 3, 6 };
int k1 = 2;
long result1 = solution.MinimumCostToBalanceSchedule(delta1, k1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] delta2 = { -5, -2, -4, 8, 9, 7 };
int k2 = 3;
long result2 = solution.MinimumCostToBalanceSchedule(delta2, k2);
Console.WriteLine(result2); // Expected: 4

// Additional quick sanity checks
int[] delta3 = { 1 };
int k3 = 1;
Console.WriteLine(solution.MinimumCostToBalanceSchedule(delta3, k3)); // Expected: 0

int[] delta4 = { 1, 2, 3, 4 };
int k4 = 2;
Console.WriteLine(solution.MinimumCostToBalanceSchedule(delta4, k4)); // One optimal answer: 2