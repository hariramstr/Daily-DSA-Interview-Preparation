/*
Title: Minimum Fatigue to Tune a Circular Drum Line

Problem Description:
A festival stage has n drums arranged in a circle. Drum i currently has pitch current[i], and it must be retuned to target[i]. You may tune the drums in any order, but because the drums are mounted on a shared circular frame, tuning one drum affects the effort needed to tune its neighbors.

If you tune a contiguous block of drums from index l to index r in clockwise order as one phase, then during that phase you may choose exactly one drum k in that block to tune last. All other drums in the block must be completely tuned before drum k is finalized. The fatigue cost contributed by drum k when it is finalized is:

abs(current[k] - target[k]) + leftPenalty + rightPenalty

where leftPenalty is 0 if there is no untuned drum remaining on the clockwise-left side inside the current block, otherwise it is abs(target[k] - target[left]), where left is the nearest drum in the block still not finalized before k; similarly, rightPenalty is defined using the nearest untuned drum on the clockwise-right side inside the current block.

Your task is to compute the minimum total fatigue required to finalize all drums.

Because the drums are arranged in a circle, drum 0 and drum n - 1 are adjacent. You may break the circle at any position before planning phases, but the resulting total fatigue must respect the original circular adjacency.

Return the minimum possible total fatigue.

Constraints:
- 1 <= n <= 300
- 0 <= current[i], target[i] <= 10^9
- current.length == target.length == n

Key DP insight used in this solution:
1. The unavoidable part is sum(abs(current[i] - target[i])).
2. The optimization only concerns the extra penalties involving target values.
3. For a linear interval [l..r], if drum k is finalized last inside that interval, then:
      dp[l][r] = min over k in [l..r] of
                 dp[l][k-1] + dp[k+1][r]
                 + (k > l ? |target[k] - target[k-1]| : 0)
                 + (k < r ? |target[k] - target[k+1]| : 0)
   because when k is the last remaining drum of the interval, the nearest still-unfinalized
   drum on the left side is exactly k-1 if that side exists, and similarly k+1 on the right.
4. For the circle, choose one drum s to be finalized globally last.
   Then every other drum lies in the linear interval obtained by cutting the circle at s.
   The last drum s pays two circular boundary penalties:
      |target[s] - target[left neighbor of s in the circle]|
      + |target[s] - target[right neighbor of s in the circle]|
   plus the optimal DP cost of the remaining linear chain.

This yields an O(n^3) algorithm, which is acceptable for n <= 300.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n^3)
      We try every possible globally-last drum s (n choices).
      For each choice, we solve an interval DP on a linear chain of length n - 1.
      That interval DP is O(m^3) where m = n - 1, so overall O(n^3).

    Space Complexity:
    - O(n^2)
      The interval DP table for one linear chain has size m x m.

    This method returns the minimum total fatigue for the circular drum arrangement.
    */
    public long MinimumFatigue(int[] current, int[] target)
    {
        int n = current.Length;

        // Step 1:
        // Compute the unavoidable "base" cost.
        //
        // Why this is necessary:
        // Every drum must eventually be retuned from current[i] to target[i].
        // The quantity |current[i] - target[i]| is paid no matter what order we choose.
        // Since it does not depend on the order, we can separate it from the optimization.
        long baseCost = 0;
        for (int i = 0; i < n; i++)
        {
            baseCost += Math.Abs((long)current[i] - target[i]);
        }

        // Special case:
        // If there is only one drum, there are no neighbors at all.
        // So the answer is just the base cost.
        if (n == 1)
        {
            return baseCost;
        }

        long bestExtra = long.MaxValue;

        // Step 2:
        // Try each drum s as the globally last finalized drum in the circle.
        //
        // Why this works:
        // In any complete valid process on the circle, exactly one drum is finalized last overall.
        // Suppose that drum is s.
        // At that final moment, the only still-unfinalized drum is s itself, and the rest of the
        // drums have already been resolved in some way that is equivalent to solving a linear chain
        // formed by removing s from the circle.
        //
        // The remaining drums appear in circular order:
        //   s+1, s+2, ..., n-1, 0, 1, ..., s-1
        // which is a linear chain of length n-1 after cutting at s.
        for (int s = 0; s < n; s++)
        {
            int m = n - 1;

            // Build the linear target array "arr" consisting of all drums except s,
            // in clockwise order starting right after s.
            //
            // Example:
            // If n = 5 and s = 2, then arr corresponds to indices:
            // 3, 4, 0, 1
            //
            // Why this is useful:
            // Once s is fixed as the globally last drum, the rest of the circle becomes a line.
            long[] arr = new long[m];
            for (int i = 0; i < m; i++)
            {
                int originalIndex = (s + 1 + i) % n;
                arr[i] = target[originalIndex];
            }

            // Step 3:
            // Solve the interval DP on this linear chain.
            //
            // dp[l, r] = minimum extra structural fatigue needed to finalize all drums
            //            in the subarray arr[l..r].
            //
            // Important interpretation:
            // This DP only stores the "extra" penalties involving target differences.
            // The base retuning costs were already separated out.
            long[,] dp = new long[m, m];

            // Base case:
            // For a single drum interval [i..i], there are no neighbors inside the interval.
            // Therefore, if that drum is finalized last within this interval, it pays no extra penalty.
            for (int i = 0; i < m; i++)
            {
                dp[i, i] = 0;
            }

            // Fill intervals by increasing length.
            //
            // Why increasing length:
            // When computing dp[l, r], we need smaller intervals such as dp[l, k-1] and dp[k+1, r].
            // Therefore those must already be known.
            for (int len = 2; len <= m; len++)
            {
                for (int l = 0; l + len - 1 < m; l++)
                {
                    int r = l + len - 1;
                    long best = long.MaxValue;

                    // Try every position k in [l..r] as the last drum finalized in this interval.
                    //
                    // Why this transition is correct:
                    // If k is the last remaining drum in arr[l..r], then:
                    // - everything in arr[l..k-1] must already have been finalized optimally
                    // - everything in arr[k+1..r] must already have been finalized optimally
                    // - when k is finally finalized, the nearest still-unfinalized drum on the left
                    //   side inside the interval is exactly k-1 if k > l
                    // - similarly, the nearest still-unfinalized drum on the right side is exactly
                    //   k+1 if k < r
                    //
                    // Therefore the final extra penalty for k is:
                    //   (k > l ? |arr[k] - arr[k-1]| : 0)
                    // + (k < r ? |arr[k] - arr[k+1]| : 0)
                    for (int k = l; k <= r; k++)
                    {
                        long cost = 0;

                        // Add optimal cost of left subinterval if it exists.
                        if (k > l)
                        {
                            cost += dp[l, k - 1];
                            cost += Math.Abs(arr[k] - arr[k - 1]);
                        }

                        // Add optimal cost of right subinterval if it exists.
                        if (k < r)
                        {
                            cost += dp[k + 1, r];
                            cost += Math.Abs(arr[k] - arr[k + 1]);
                        }

                        if (cost < best)
                        {
                            best = cost;
                        }
                    }

                    dp[l, r] = best;
                }
            }

            // Step 4:
            // Add the final cost for drum s itself.
            //
            // Since s is globally last in the original circle, at that moment its two circular
            // neighbors are exactly:
            // - previous drum in the circle: (s - 1 + n) % n
            // - next drum in the circle:     (s + 1) % n
            //
            // Both sides still "exist" as the two exposed ends of the linear chain of other drums,
            // so s pays both penalties.
            int leftNeighbor = (s - 1 + n) % n;
            int rightNeighbor = (s + 1) % n;

            long finalPenaltyForS =
                Math.Abs((long)target[s] - target[leftNeighbor]) +
                Math.Abs((long)target[s] - target[rightNeighbor]);

            long extraForThisChoice = dp[0, m - 1] + finalPenaltyForS;

            if (extraForThisChoice < bestExtra)
            {
                bestExtra = extraForThisChoice;
            }
        }

        return baseCost + bestExtra;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] current1 = { 5, 1, 4 };
int[] target1 = { 3, 2, 6 };
long answer1 = solution.MinimumFatigue(current1, target1);
Console.WriteLine(answer1); // Expected: 7

// Example 2
int[] current2 = { 8, 8, 8, 8 };
int[] target2 = { 1, 10, 1, 10 };
long answer2 = solution.MinimumFatigue(current2, target2);
Console.WriteLine(answer2); // Expected: 23