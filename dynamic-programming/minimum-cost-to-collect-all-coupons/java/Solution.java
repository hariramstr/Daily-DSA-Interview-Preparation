```java
/*
 * Title: Minimum Cost to Collect All Coupons
 * Difficulty: Medium
 * Topic: Dynamic Programming (Bitmask DP)
 *
 * Problem Description:
 * A store is running a promotion where customers can collect coupons. There are `n` distinct
 * coupon types numbered from `0` to `n-1`. You are given an integer array `cost` where
 * `cost[i]` represents the price to directly purchase coupon `i`. Additionally, you are given
 * a 2D array `bundles` where each `bundles[j] = [a, b, discount]` means: if you already own
 * coupon `a`, you can acquire coupon `b` at a reduced cost of `discount` (instead of `cost[b]`).
 * Each bundle can only be used once, and you can only use a bundle if you already own the
 * prerequisite coupon at the time of purchase.
 *
 * Return the minimum total cost to collect all `n` coupons.
 *
 * Constraints:
 * - 1 <= n <= 20
 * - 1 <= cost[i] <= 1000
 * - 0 <= bundles.length <= n * (n - 1)
 * - bundles[j].length == 3
 * - 0 <= a, b <= n - 1, a != b
 * - 1 <= discount <= cost[b]
 * - There are no duplicate (a, b) pairs in bundles.
 */

import java.util.*;

/**
 * Solution class for the Minimum Cost to Collect All Coupons problem.
 *
 * <p>Key Insight: Since n <= 20, we can use bitmask DP where each bit in the state
 * represents whether we own a particular coupon. We iterate over all 2^n possible
 * subsets and compute the minimum cost to reach each subset.
 *
 * <p>For each state (subset of coupons we own), we try adding one more coupon:
 * - Either buy it at full price (cost[i])
 * - Or use a bundle discount if we already own the prerequisite coupon
 */
public class Solution {

    /**
     * Computes the minimum total cost to collect all n coupons using bitmask DP.
     *
     * <p>Algorithm Overview:
     * 1. Build a discount map: for each coupon b, store all possible (prerequisite, discountCost) pairs.
     * 2. Use bitmask DP: dp[mask] = minimum cost to own exactly the coupons indicated by mask.
     * 3. For each state, try adding each coupon not yet owned, computing the best price
     *    (either full price or best available bundle discount given current owned coupons).
     * 4. Return dp[(1 << n) - 1], the cost to own all coupons.
     *
     * @param cost    array where cost[i] is the full purchase price of coupon i
     * @param bundles 2D array where bundles[j] = [a, b, discount] means owning coupon a
     *                lets you buy coupon b for `discount` instead of cost[b]
     * @return the minimum total cost to collect all n coupons
     *
     * Time Complexity:  O(2^n * n^2) — for each of 2^n states, we try n coupons,
     *                   and for each coupon we check up to n possible bundle prerequisites.
     * Space Complexity: O(2^n + n^2) — dp array of size 2^n plus the discount map.
     */
    public int minCost(int[] cost, int[][] bundles) {
        int n = cost.length;

        // -----------------------------------------------------------------------
        // Step 1: Build a discount lookup structure.
        // discountMap[b] is a list of int[] pairs {prerequisite, discountCost}
        // meaning: if you own `prerequisite`, you can buy coupon `b` for `discountCost`.
        // -----------------------------------------------------------------------
        @SuppressWarnings("unchecked")
        List<int[]>[] discountMap = new List[n];
        for (int i = 0; i < n; i++) {
            discountMap[i] = new ArrayList<>();
        }

        // Populate the discount map from the bundles array
        for (int[] bundle : bundles) {
            int prereq = bundle[0];      // prerequisite coupon (must already own this)
            int target = bundle[1];      // coupon we want to buy at a discount
            int discountCost = bundle[2]; // reduced cost to buy `target` if we own `prereq`
            discountMap[target].add(new int[]{prereq, discountCost});
        }

        // -----------------------------------------------------------------------
        // Step 2: Initialize the DP array.
        // dp[mask] = minimum cost to own exactly the set of coupons represented by `mask`.
        // A mask is an integer where bit i is set if we own coupon i.
        //
        // Total number of states = 2^n (all possible subsets of n coupons).
        // We initialize all states to Integer.MAX_VALUE / 2 (infinity) to indicate
        // they haven't been reached yet, except dp[0] = 0 (owning nothing costs 0).
        // -----------------------------------------------------------------------
        int totalStates = 1 << n;          // 2^n possible subsets
        int[] dp = new int[totalStates];
        Arrays.fill(dp, Integer.MAX_VALUE / 2); // use large value as "infinity"
        dp[0] = 0; // base case: owning no coupons costs 0

        // -----------------------------------------------------------------------
        // Step 3: Iterate over all possible states (subsets) in increasing order.
        // For each reachable state, try adding each coupon not yet in the state.
        // -----------------------------------------------------------------------
        for (int mask = 0; mask < totalStates; mask++) {

            // Skip states that are unreachable (cost is still "infinity")
            if (dp[mask] == Integer.MAX_VALUE / 2) continue;

            // Try adding each coupon `i` that we don't yet own
            for (int i = 0; i < n; i++) {

                // Check if coupon i is already owned in this state
                // (bit i is set in mask)
                if ((mask & (1 << i)) != 0) {
                    continue; // already own coupon i, skip
                }

                // ---------------------------------------------------------------
                // Step 4: Determine the best price to acquire coupon i given
                // the current set of owned coupons (represented by `mask`).
                //
                // Start with the full purchase price as the default.
                // ---------------------------------------------------------------
                int bestPrice = cost[i]; // default: buy at full price

                // Check all bundles that give a discount on coupon i
                for (int[] entry : discountMap[i]) {
                    int prereq = entry[0];       // prerequisite coupon
                    int discountCost = entry[1]; // discounted price for coupon i

                    // We can use this bundle only if we already own the prerequisite
                    // i.e., bit `prereq` is set in the current mask
                    if ((mask & (1 << prereq)) != 0) {
                        // Take the minimum of current best price and this discount
                        bestPrice = Math.min(bestPrice, discountCost);
                    }
                }

                // ---------------------------------------------------------------
                // Step 5: Transition to the new state where we also own coupon i.
                // newMask = mask with bit i set.
                // Update dp[newMask] if we found a cheaper way to reach it.
                // ---------------------------------------------------------------
                int newMask = mask | (1 << i); // add coupon i to our owned set
                dp[newMask] = Math.min(dp[newMask], dp[mask] + bestPrice);
            }
        }

        // -----------------------------------------------------------------------
        // Step 6: The answer is the minimum cost to own ALL n coupons.
        // The mask with all n bits set is (1 << n) - 1.
        // -----------------------------------------------------------------------
        int allOwned = totalStates - 1; // (1 << n) - 1: all bits set
        return dp[allOwned];
    }

    /**
     * Main method to demonstrate the solution with sample inputs from the problem description.
     *
     * <p>Example 1: cost = [5, 3, 4], bundles = [[0,1,1],[0,2,2]] → Expected: 8
     * <p>Example 2: cost = [10, 6, 7, 4], bundles = [[0,1,2],[1,2,3],[2,3,1]] → Expected: 16
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1 Trace:
        // n = 3, cost = [5, 3, 4]
        // Bundles: [0,1,1] means owning coupon 0 lets you buy coupon 1 for 1
        //          [0,2,2] means owning coupon 0 lets you buy coupon 2 for 2
        //
        // Optimal strategy:
        //   - Buy coupon 0 at full price: 5
        //   - Now own {0}, use bundle [0,1,1]: buy coupon 1 for 1
        //   - Now own {0,1}, use bundle [0,2,2]: buy coupon 2 for 2
        //   - Total = 5 + 1 + 2 = 8
        //
        // DP trace (key transitions):
        //   dp[000] = 0
        //   dp[001] = dp[000] + cost[0] = 0 + 5 = 5  (buy coupon 0 at full price)
        //   dp[011] = dp[001] + discount(1 given {0}) = 5 + 1 = 6
        //   dp[101] = dp[001] + discount(2 given {0}) = 5 + 2 = 7
        //   dp[111] = min(dp[011] + discount(2 given {0,1}), dp[101] + discount(1 given {0,2}))
        //           = min(6 + 2, 7 + 1) = min(8, 8) = 8
        // -----------------------------------------------------------------------
        int[] cost1 = {5, 3, 4};
        int[][] bundles1 = {{0, 1, 1}, {0, 2, 2}};
        int result1 = solution.minCost(cost1, bundles1);
        System.out.println("Example 1:");
        System.out.println("  cost = [5, 3, 4]");
        System.out.println("  bundles = [[0,1,1],[0,2,2]]");
        System.out.println("  Expected: 8");
        System.out.println("  Got:      " + result1);
        System.out.println("  Correct:  " + (result1 == 8));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2 Trace:
        // n = 4, cost = [10, 6, 7, 4]
        // Bundles: [0,1,2] means owning 0 lets you buy 1 for 2
        //          [1,2,3] means owning 1 lets you buy 2 for 3
        //          [2,3,1] means owning 2 lets you buy 3 for 1
        //
        // Optimal strategy:
        //   - Buy coupon 0 at full price: 10
        //   - Use bundle [0,1,2]: buy coupon 1 for 2
        //   - Use bundle [1,2,3]: buy coupon 2 for 3
        //   - Use bundle [2,3,1]: buy coupon 3 for 1
        //   - Total = 10 + 2 + 3 + 1 = 16
        // -----------------------------------------------------------------------
        int[] cost2 = {10, 6, 7, 4};
        int[][] bundles2 = {{0, 1, 2}, {1, 2, 3}, {2, 3, 1}};
        int result2 = solution.minCost(cost2, bundles2);
        System.out.println("Example 2:");
        System.out.println("  cost = [10, 6, 7, 4]");
        System.out.println("  bundles = [[0,1,2],[1,2,3],[2,3,1]]");
        System.out.println("  Expected: 16");
        System.out.println("  Got:      " + result2);
        System.out.println("  Correct:  " + (result2 == 16));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 3: No bundles — must buy everything at full price
        // cost = [3, 7, 2], bundles = []
        // Expected: 3 + 7 + 2 = 12
        // -----------------------------------------------------------------------
        int[] cost3 = {3, 7, 2};
        int[][] bundles3 = {};
        int result3 = solution.minCost(cost3, bundles3);
        System.out.println("Example 3 (no bundles):");
        System.out.println("  cost = [3, 7, 2]");
        System.out.println("  bundles = []");
        System.out.println("  Expected: 12");
        System.out.println("  Got:      " + result3);
        System.out.println("  Correct:  " + (result3 == 12));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 4: Single coupon
        // cost = [5], bundles = []
        // Expected: 5
        // -----------------------------------------------------------------------
        int[] cost4 = {5};
        int[][] bundles4 = {};
        int result4 = solution.minCost(cost4, bundles4);
        System.out.println("Example 4 (single coupon):");
        System.out.println("  cost = [5]");
        System.out.println("  bundles = []");
        System.out.println("  Expected: 5");
        System.out.println("  Got:      " + result4);
        System.out.println("  Correct:  " + (result4 == 5));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 5: Multiple discount paths — choose the cheapest
        // cost = [4, 8, 6]
        // bundles = [[0,2,1], [1,2,2]]
        // Owning coupon 0 lets you buy coupon 2 for 1
        // Owning coupon 1 lets you buy coupon 2 for 2
        //
        // Option A: buy 0(4), buy 1(8), buy 2 via bundle[0,2,1] = 1 → total = 13
        // Option B: buy 0(4), buy 2 via bundle[0,2,1] = 1, buy 1(8) → total = 13
        // Option C: buy 1(8), buy 2 via bundle[1,2,2] = 2, buy 0(4) → total = 14
        // Option D: buy 0(4), buy 1(8), buy 2 via best discount = min(1,2) = 1 → 13
        // Expected: 13
        // -----------------------------------------------------------------------
        int[] cost5 = {4, 8, 6};
        int[][] bundles5 = {{0, 2, 1}, {1, 2, 2}};
        int result5 = solution.minCost(cost5, bundles5);
        System.out.println("Example 5 (multiple discount paths):");
        System.out.println("  cost = [4, 8, 6]");
        System.out.println("  bundles = [[