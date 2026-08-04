/*
Title: Minimum Reset Cost for Consecutive Machine Runs
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
A factory must process a sequence of n jobs in the given order. Each job i requires the machine to run in one of several supported modes, represented by an integer modes[i]. The machine may continue running in the same mode for the next job at no extra cost. However, if the next job uses a different mode, the factory must pay a reset cost.

You are also given an array resetCost where resetCost[x] is the cost to switch the machine into mode x. Switching from any mode a to a different mode b always costs resetCost[b]. The initial machine state is undefined, so starting the first job in mode x also costs resetCost[x].

Before processing begins, the factory may upgrade at most k jobs. Upgrading job i allows it to be processed in any mode you choose, not just modes[i]. Each upgraded job still occupies its position in the sequence, but you may assign it any mode in order to reduce the total reset cost.

Return the minimum possible total cost to process all jobs.

In other words, you may change the required mode of up to k positions to arbitrary modes, and you want to minimize the sum of start/switch costs across the full sequence.

Constraints:
- 1 <= n <= 2000
- 1 <= k <= n
- 1 <= modes[i] <= m
- 1 <= m <= 100
- resetCost.length == m + 1, where index 0 is unused
- 1 <= resetCost[x] <= 10^4
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * k * m^2)
      where:
      n = number of jobs
      k = maximum number of upgrades allowed
      m = number of possible modes
    - With constraints n <= 2000 and m <= 100, this is acceptable in C# when implemented carefully.

    Space Complexity:
    - O(k * m)
      We only keep the previous DP layer and the current DP layer, instead of storing all n layers.

    Core DP idea:
    ----------------
    We process jobs from left to right.

    DP state:
      dp[usedUpgrades, lastMode] = minimum total cost after processing the current prefix,
                                   using exactly usedUpgrades upgrades,
                                   and ending with the machine currently in lastMode.

    Transition for each job:
      1. Do NOT upgrade this job:
         - Allowed only if chosen mode == original required mode.
         - If lastMode == requiredMode, extra cost = 0
         - Otherwise extra cost = resetCost[requiredMode]

      2. Upgrade this job:
         - We may assign this job to ANY mode.
         - This consumes 1 upgrade.
         - If we assign it to the same mode as lastMode, extra cost = 0
         - If we assign it to a different mode x, extra cost = resetCost[x]

    Important optimization insight:
    -------------------------------
    For an upgraded job, from a previous state ending in mode p:
      - staying in p costs +0
      - switching to any other mode x costs +resetCost[x]

    So for each target mode x, the best previous source is:
      min(
          prev[used-1][x],                 // stay in x, no switch cost
          min over p != x of prev[used-1][p] + resetCost[x]
      )

    We compute this efficiently by precomputing, for each upgrade count:
      - the smallest prev value and its mode
      - the second smallest prev value
    Then for each target mode x:
      best source excluding x is:
         if argmin mode != x => globalMin
         else secondMin

    This avoids an extra factor of m in the upgrade transition.
    */
    public long MinResetCost(int[] modes, int k, int[] resetCost)
    {
        int n = modes.Length;
        int m = resetCost.Length - 1;

        // We use a very large number to represent "impossible / not reached yet".
        long inf = long.MaxValue / 4;

        // prev[u, mode] = best cost after processing previous jobs,
        // using exactly u upgrades, and ending in "mode".
        long[,] prev = new long[k + 1, m + 1];
        long[,] curr = new long[k + 1, m + 1];

        // Initialize all states to INF because initially nothing has been processed.
        for (int u = 0; u <= k; u++)
        {
            for (int mode = 1; mode <= m; mode++)
            {
                prev[u, mode] = inf;
                curr[u, mode] = inf;
            }
        }

        // ------------------------------------------------------------
        // Base case: process the first job separately.
        // Why?
        // Because the machine starts in an undefined state, so choosing the first
        // mode always costs resetCost[firstMode].
        //
        // Two possibilities:
        // 1) Do not upgrade first job:
        //      it must use modes[0], cost = resetCost[modes[0]]
        // 2) Upgrade first job:
        //      it may use any mode x, cost = resetCost[x]
        // ------------------------------------------------------------
        int firstRequired = modes[0];

        // No upgrade on first job.
        prev[0, firstRequired] = resetCost[firstRequired];

        // Upgrade first job, if allowed.
        if (k >= 1)
        {
            for (int mode = 1; mode <= m; mode++)
            {
                prev[1, mode] = Math.Min(prev[1, mode], resetCost[mode]);
            }
        }

        // ------------------------------------------------------------
        // Process jobs from index 1 to n-1.
        // At each step, we build "curr" from "prev".
        // ------------------------------------------------------------
        for (int i = 1; i < n; i++)
        {
            int required = modes[i];

            // Reset current layer to INF before filling it.
            for (int u = 0; u <= k; u++)
            {
                for (int mode = 1; mode <= m; mode++)
                {
                    curr[u, mode] = inf;
                }
            }

            // --------------------------------------------------------
            // Transition 1: do NOT upgrade this job.
            //
            // Then this job must run in its original required mode.
            //
            // From every previous ending mode "last":
            //   - if last == required, extra cost = 0
            //   - else extra cost = resetCost[required]
            //
            // Resulting ending mode becomes "required".
            //
            // We do this for every possible number of used upgrades.
            // --------------------------------------------------------
            for (int u = 0; u <= k; u++)
            {
                for (int last = 1; last <= m; last++)
                {
                    long oldCost = prev[u, last];
                    if (oldCost >= inf) continue;

                    long add = (last == required) ? 0 : resetCost[required];
                    long candidate = oldCost + add;

                    if (candidate < curr[u, required])
                    {
                        curr[u, required] = candidate;
                    }
                }
            }

            // --------------------------------------------------------
            // Transition 2: upgrade this job.
            //
            // If we upgrade the current job, we may assign it ANY mode x.
            // This consumes one additional upgrade.
            //
            // Naively, for each previous mode p and each target mode x:
            //   candidate = prev[u-1, p] + (p == x ? 0 : resetCost[x])
            //
            // That would be O(m^2) per upgrade count.
            //
            // We optimize by observing:
            //   For a fixed target mode x, the best source is either:
            //     A) already in x, cost prev[u-1, x]
            //     B) come from the best different mode p != x, then pay resetCost[x]
            //
            // So for each upgrade count (u-1), we precompute:
            //   - smallest prev value and which mode achieved it
            //   - second smallest prev value
            //
            // Then for target x:
            //   bestDifferent =
            //      globalMin if globalMinMode != x
            //      secondMin otherwise
            //
            // candidate = min(
            //      prev[u-1, x],               // stay in x
            //      bestDifferent + resetCost[x] // switch into x
            //   )
            // --------------------------------------------------------
            for (int u = 1; u <= k; u++)
            {
                // Find the smallest and second smallest values in prev[u - 1, *].
                long min1 = inf;
                long min2 = inf;
                int minMode = -1;

                for (int mode = 1; mode <= m; mode++)
                {
                    long value = prev[u - 1, mode];

                    if (value < min1)
                    {
                        min2 = min1;
                        min1 = value;
                        minMode = mode;
                    }
                    else if (value < min2)
                    {
                        min2 = value;
                    }
                }

                // If even the best previous state is unreachable, skip.
                if (min1 >= inf) continue;

                // Try assigning the upgraded current job to every possible mode.
                for (int target = 1; target <= m; target++)
                {
                    // Option 1: stay in the same mode target, if previous state already ended there.
                    long best = prev[u - 1, target];

                    // Option 2: come from a different mode and pay resetCost[target].
                    long bestDifferent = (minMode == target) ? min2 : min1;
                    if (bestDifferent < inf)
                    {
                        long switchCandidate = bestDifferent + resetCost[target];
                        if (switchCandidate < best)
                        {
                            best = switchCandidate;
                        }
                    }

                    if (best < curr[u, target])
                    {
                        curr[u, target] = best;
                    }
                }
            }

            // Move current layer into previous layer for the next iteration.
            var temp = prev;
            prev = curr;
            curr = temp;
        }

        // ------------------------------------------------------------
        // Final answer:
        // We may use AT MOST k upgrades, not necessarily exactly k.
        // So we take the minimum over all used upgrade counts 0..k
        // and all possible ending modes.
        // ------------------------------------------------------------
        long answer = inf;

        for (int u = 0; u <= k; u++)
        {
            for (int mode = 1; mode <= m; mode++)
            {
                if (prev[u, mode] < answer)
                {
                    answer = prev[u, mode];
                }
            }
        }

        return answer;
    }
}

// Demo code
var solution = new Solution();

int[] modes1 = { 1, 2, 2, 3 };
int k1 = 1;
int[] resetCost1 = { 0, 5, 2, 7 };
long result1 = solution.MinResetCost(modes1, k1, resetCost1);
Console.WriteLine(result1); // Expected: 7

int[] modes2 = { 4, 1, 4, 1, 4 };
int k2 = 2;
int[] resetCost2 = { 0, 3, 6, 8, 2 };
long result2 = solution.MinResetCost(modes2, k2, resetCost2);
Console.WriteLine(result2); // Expected: 2