/*
Minimum Cost to Stabilize a Conveyor Belt with Optional Buffer Pads

Problem Description:
A warehouse conveyor belt is divided into n consecutive segments. Segment i has an instability value instability[i], which may be positive, zero, or negative. To reduce vibration, the belt must be fully covered from left to right using repair operations. You may apply two kinds of operations:

1. Standard repair on a single segment i, costing singleCost[i].
2. Buffered repair on exactly two adjacent segments i and i+1, costing pairCost[i]. A buffered repair may only be used if the absolute difference between instability[i] and instability[i+1] is at most limit.

In addition, the engineering team allows up to k reinforcement coupons. Each coupon can be spent on any one repair operation (single or buffered) and reduces that operation's cost by discount, but the final cost of that operation cannot go below 0.

Your task is to compute the minimum total cost needed to cover every segment exactly once.

Two repairs cannot overlap, and every segment must belong to exactly one chosen operation. Coupons are optional and may be used on fewer than k operations.

Return the minimum possible total cost.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n * k)

    Why:
    - We process the conveyor from left to right.
    - For each position i, we consider every possible number of coupons already used, from 0 to k.
    - From each state, we try:
      1) a single repair on segment i
      2) a buffered repair on segments i and i+1 if valid
    - Each transition is O(1), so total work is O(n * k).

    Space Complexity:
    O(n * k)

    Why:
    - dp[i, c] stores the minimum cost to cover the first i segments using exactly c coupons.
    - There are (n + 1) * (k + 1) states.
    */
    public long MinimumCost(int[] instability, int[] singleCost, int[] pairCost, int limit, int k, int discount)
    {
        int n = instability.Length;

        // We use a very large number to represent "unreachable" states.
        // It must be safely larger than any possible valid answer.
        long inf = long.MaxValue / 4;

        // dp[i, c] means:
        // "Minimum total cost to cover exactly the first i segments
        //  (that is, segments 0 through i-1),
        //  while using exactly c coupons."
        //
        // Example:
        // dp[0, 0] = 0 because covering zero segments costs nothing.
        // dp[3, 1] = minimum cost to cover segments 0,1,2 using exactly one coupon.
        long[,] dp = new long[n + 1, k + 1];

        // Initialize every state as unreachable.
        for (int i = 0; i <= n; i++)
        {
            for (int c = 0; c <= k; c++)
            {
                dp[i, c] = inf;
            }
        }

        // Base case:
        // Before covering any segment, cost is 0 and no coupon has been used.
        dp[0, 0] = 0;

        // We now build the answer from left to right.
        // At each prefix length i, we decide how to cover the next segment(s).
        for (int i = 0; i < n; i++)
        {
            for (int usedCoupons = 0; usedCoupons <= k; usedCoupons++)
            {
                long currentCost = dp[i, usedCoupons];

                // If this state is unreachable, there is nothing to extend.
                if (currentCost == inf)
                {
                    continue;
                }

                // ------------------------------------------------------------
                // Option 1: Cover segment i with a standard single repair.
                // ------------------------------------------------------------
                //
                // Why this transition exists:
                // Every segment can always be repaired individually.
                //
                // This moves us from:
                //   covered first i segments
                // to:
                //   covered first i+1 segments
                //
                // Case 1A: do NOT use a coupon on this single repair.
                {
                    long newCost = currentCost + singleCost[i];

                    if (newCost < dp[i + 1, usedCoupons])
                    {
                        dp[i + 1, usedCoupons] = newCost;
                    }
                }

                // Case 1B: use one coupon on this single repair, if we still have one available.
                //
                // The discounted cost of one operation is:
                // max(0, originalCost - discount)
                //
                // We use Math.Max to ensure the operation cost never becomes negative.
                if (usedCoupons < k)
                {
                    long discountedSingle = Math.Max(0L, (long)singleCost[i] - discount);
                    long newCost = currentCost + discountedSingle;

                    if (newCost < dp[i + 1, usedCoupons + 1])
                    {
                        dp[i + 1, usedCoupons + 1] = newCost;
                    }
                }

                // ------------------------------------------------------------
                // Option 2: Cover segments i and i+1 with one buffered repair.
                // ------------------------------------------------------------
                //
                // This is only possible if:
                // 1) i + 1 is inside the array
                // 2) the instability difference is within the allowed limit
                //
                // Why we check this:
                // Buffered repair is only valid for adjacent segments whose
                // instability values are close enough.
                if (i + 1 < n)
                {
                    long diff = Math.Abs((long)instability[i] - instability[i + 1]);

                    if (diff <= limit)
                    {
                        // Case 2A: use the buffered repair without a coupon.
                        //
                        // This covers two segments at once, so the prefix length increases by 2.
                        {
                            long newCost = currentCost + pairCost[i];

                            if (newCost < dp[i + 2, usedCoupons])
                            {
                                dp[i + 2, usedCoupons] = newCost;
                            }
                        }

                        // Case 2B: use one coupon on the buffered repair.
                        if (usedCoupons < k)
                        {
                            long discountedPair = Math.Max(0L, (long)pairCost[i] - discount);
                            long newCost = currentCost + discountedPair;

                            if (newCost < dp[i + 2, usedCoupons + 1])
                            {
                                dp[i + 2, usedCoupons + 1] = newCost;
                            }
                        }
                    }
                }
            }
        }

        // We are allowed to use up to k coupons, not necessarily exactly k.
        // Therefore, among all states that covered all n segments,
        // we take the minimum over coupon counts 0..k.
        long answer = inf;

        for (int usedCoupons = 0; usedCoupons <= k; usedCoupons++)
        {
            if (dp[n, usedCoupons] < answer)
            {
                answer = dp[n, usedCoupons];
            }
        }

        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] instability1 = { 4, 7, 6, 6 };
int[] singleCost1 = { 8, 5, 7, 4 };
int[] pairCost1 = { 9, 20, 6 };
int limit1 = 2;
int k1 = 1;
int discount1 = 3;

long result1 = solution.MinimumCost(instability1, singleCost1, pairCost1, limit1, k1, discount1);
Console.WriteLine(result1); // Expected: 16

// Example 2
int[] instability2 = { 3, 3, 10, 11, 12 };
int[] singleCost2 = { 6, 6, 8, 5, 5 };
int[] pairCost2 = { 7, 50, 6, 9 };
int limit2 = 1;
int k2 = 2;
int discount2 = 4;

long result2 = solution.MinimumCost(instability2, singleCost2, pairCost2, limit2, k2, discount2);
Console.WriteLine(result2); // Expected: 10

// A small extra sanity test
int[] instability3 = { 1 };
int[] singleCost3 = { 5 };
int[] pairCost3 = Array.Empty<int>();
int limit3 = 0;
int k3 = 1;
int discount3 = 10;

long result3 = solution.MinimumCost(instability3, singleCost3, pairCost3, limit3, k3, discount3);
Console.WriteLine(result3); // Expected: 0