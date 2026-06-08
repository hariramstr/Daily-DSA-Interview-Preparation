/*
 * Title: Minimum Cost to Collect All Coupons
 * Difficulty: Medium
 * Topic: Dynamic Programming (Bitmask DP)
 *
 * Problem Description:
 * A store is running a promotion where customers can collect coupons. There are n distinct
 * coupon types numbered from 0 to n-1. You are given an integer array cost where cost[i]
 * represents the price to directly purchase coupon i. Additionally, you are given a 2D array
 * bundles where each bundles[j] = [a, b, discount] means: if you already own coupon a, you
 * can acquire coupon b at a reduced cost of discount (instead of cost[b]). Each bundle can
 * only be used once, and you can only use a bundle if you already own the prerequisite coupon
 * at the time of purchase.
 *
 * Return the minimum total cost to collect all n coupons.
 *
 * Constraints:
 * - 1 <= n <= 20
 * - 1 <= cost[i] <= 1000
 * - 0 <= bundles.length <= n * (n - 1)
 * - bundles[j].length == 3
 * - 0 <= a, b <= n-1, a != b
 * - 1 <= discount <= cost[b]
 * - There are no duplicate (a, b) pairs in bundles.
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /*
     * Time Complexity:  O(2^n * n)
     *   - We have 2^n possible subsets (bitmask states).
     *   - For each state we iterate over all n coupons to decide which one to add next.
     *   - Building the best-discount lookup is O(bundles.length) which is at most O(n^2).
     *
     * Space Complexity: O(2^n + n^2)
     *   - O(2^n) for the DP table.
     *   - O(n^2) for the best-discount adjacency structure.
     *
     * ── Core Idea ────────────────────────────────────────────────────────────
     * Because n ≤ 20 we can represent "which coupons we already own" as a
     * bitmask (an integer whose k-th bit is 1 iff we own coupon k).
     *
     * dp[mask] = minimum total cost to own exactly the set of coupons
     *            described by `mask`.
     *
     * Transition: to move from state `mask` to state `mask | (1 << next)` we
     * pay the cheapest available price for coupon `next`, which is either:
     *   a) cost[next]  (buy it outright), or
     *   b) discount    (use a bundle from any coupon `a` we already own,
     *                   i.e., bit `a` is set in `mask`).
     *
     * We pre-compute bestDiscount[a][b] = the single best (lowest) discount
     * available for buying b given that we own a.  Because there is at most
     * one (a,b) pair in bundles, this is just the discount value itself.
     */
    public int MinCost(int[] cost, int[][] bundles)
    {
        int n = cost.Length;

        // ── Step 1: Build a best-discount lookup table ────────────────────────
        // bestDiscount[a][b] stores the cheapest discount price to buy coupon b
        // when we already own coupon a.
        //
        // We use int.MaxValue as a sentinel meaning "no bundle exists for (a,b)".
        // This makes it easy to skip non-existent bundles during the DP.
        int[,] bestDiscount = new int[n, n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                bestDiscount[i, j] = int.MaxValue; // sentinel: no bundle

        // Fill in the actual bundle discounts.
        // The problem guarantees no duplicate (a, b) pairs, so each cell is
        // written at most once.  If there were duplicates we would take the min.
        foreach (int[] bundle in bundles)
        {
            int a        = bundle[0];
            int b        = bundle[1];
            int discount = bundle[2];
            // Keep the minimum discount in case we ever extend to duplicates.
            bestDiscount[a, b] = Math.Min(bestDiscount[a, b], discount);
        }

        // ── Step 2: Initialise the DP table ──────────────────────────────────
        // There are 2^n possible subsets.  We use int.MaxValue / 2 as "infinity"
        // (dividing by 2 prevents overflow when we add costs to it).
        int totalStates = 1 << n;          // 2^n
        int[] dp = new int[totalStates];
        Array.Fill(dp, int.MaxValue / 2);  // all states start as unreachable

        // The empty set costs nothing — we own zero coupons and have spent $0.
        dp[0] = 0;

        // ── Step 3: Iterate over every possible "owned" subset ───────────────
        // We process states in increasing order of their integer value.
        // Because adding a coupon always sets a new bit (increases the mask),
        // every predecessor state `mask` is numerically smaller than its
        // successors `mask | (1 << next)`.  This guarantees that when we
        // process `mask`, dp[mask] is already finalised.
        for (int mask = 0; mask < totalStates - 1; mask++)
        {
            // If this state is unreachable, skip it — no point extending it.
            if (dp[mask] == int.MaxValue / 2) continue;

            // ── Step 4: Try adding each coupon we don't yet own ───────────────
            // We look at every coupon index `next` and check whether bit `next`
            // is NOT set in `mask` (meaning we don't own it yet).
            for (int next = 0; next < n; next++)
            {
                // Skip coupons we already own.
                if ((mask & (1 << next)) != 0) continue;

                // ── Step 5: Determine the cheapest price for coupon `next` ────
                // Start with the full retail price (no bundle).
                int cheapestPrice = cost[next];

                // Now check every coupon `a` that we currently own (bit set in
                // mask).  If a bundle (a → next) exists, it might be cheaper.
                for (int a = 0; a < n; a++)
                {
                    // We can only use a bundle from `a` if we own `a`.
                    if ((mask & (1 << a)) == 0) continue;

                    // Check whether a bundle from a to `next` exists.
                    if (bestDiscount[a, next] != int.MaxValue)
                    {
                        // Take the minimum: maybe another owned coupon offers
                        // an even better deal for `next`.
                        cheapestPrice = Math.Min(cheapestPrice, bestDiscount[a, next]);
                    }
                }

                // ── Step 6: Relax the successor state ─────────────────────────
                // The new state after acquiring `next` is mask with bit `next` set.
                int newMask = mask | (1 << next);

                // Update dp[newMask] if going through `mask` and buying `next`
                // is cheaper than any previously found way to reach `newMask`.
                dp[newMask] = Math.Min(dp[newMask], dp[mask] + cheapestPrice);
            }
        }

        // ── Step 7: Return the answer ─────────────────────────────────────────
        // The full set of all n coupons is represented by the mask where every
        // bit 0..n-1 is set, i.e., (1 << n) - 1.
        int fullMask = totalStates - 1;
        return dp[fullMask];
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code  (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

Solution sol = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// cost = [5, 3, 4], bundles = [[0,1,1],[0,2,2]]
// Expected output: 8
// Trace:
//   dp[000] = 0
//   Buy coupon 0 outright (no bundles can help yet): dp[001] = 5
//   Buy coupon 1 outright: dp[010] = 3
//   Buy coupon 2 outright: dp[100] = 4
//   From dp[001]=5 (own coupon 0):
//     Buy coupon 1 via bundle(0→1, discount=1): dp[011] = min(inf, 5+1) = 6
//     Buy coupon 2 via bundle(0→2, discount=2): dp[101] = min(inf, 5+2) = 7
//   From dp[010]=3 (own coupon 1):
//     Buy coupon 0 outright: dp[011] = min(6, 3+5) = 6  (no improvement)
//     Buy coupon 2 outright: dp[110] = min(inf, 3+4) = 7
//   From dp[100]=4 (own coupon 2):
//     Buy coupon 0 outright: dp[101] = min(7, 4+5) = 7  (no improvement)
//     Buy coupon 1 outright: dp[110] = min(7, 4+3) = 7  (no improvement)
//   From dp[011]=6 (own coupons 0,1):
//     Buy coupon 2 via bundle(0→2, discount=2): dp[111] = min(inf, 6+2) = 8
//   From dp[101]=7 (own coupons 0,2):
//     Buy coupon 1 via bundle(0→1, discount=1): dp[111] = min(8, 7+1) = 8
//   From dp[110]=7 (own coupons 1,2):
//     Buy coupon 0 outright: dp[111] = min(8, 7+5) = 8  (no improvement)
//   Answer: dp[111] = 8  ✓

int[] cost1    = { 5, 3, 4 };
int[][] bundles1 = { new[] { 0, 1, 1 }, new[] { 0, 2, 2 } };
int result1 = sol.MinCost(cost1, bundles1);
Console.WriteLine($"Example 1 — Expected: 8,  Got: {result1}");

// ── Example 2 ────────────────────────────────────────────────────────────────
// cost = [10, 6, 7, 4], bundles = [[0,1,2],[1,2,3],[2,3,1]]
// Expected output: 16
// Optimal path: buy 0 (10) → bundle 0→1 (2) → bundle 1→2 (3) → bundle 2→3 (1)
// Total = 10 + 2 + 3 + 1 = 16  ✓

int[] cost2    = { 10, 6, 7, 4 };
int[][] bundles2 = { new[] { 0, 1, 2 }, new[] { 1, 2, 3 }, new[] { 2, 3, 1 } };
int result2 = sol.MinCost(cost2, bundles2);
Console.WriteLine($"Example 2 — Expected: 16, Got: {result2}");

// ── Additional edge-case: single coupon, no bundles ───────────────────────────
// cost = [42], bundles = []
// Expected output: 42

int[] cost3    = { 42 };
int[][] bundles3 = Array.Empty<int[]>();
int result3 = sol.MinCost(cost3, bundles3);
Console.WriteLine($"Example 3 — Expected: 42, Got: {result3}");

// ── Additional edge-case: bundle cheaper than direct but prerequisite costs more
// cost = [100, 1], bundles = [[0, 1, 1]]
// Without bundle: 100 + 1 = 101
// With bundle:    100 + 1 = 101  (discount equals direct cost, same result)
// Expected output: 101

int[] cost4    = { 100, 1 };
int[][] bundles4 = { new[] { 0, 1, 1 } };
int result4 = sol.MinCost(cost4, bundles4);
Console.WriteLine($"Example 4 — Expected: 101, Got: {result4}");