import java.util.*;

/*
Minimum Cost to Stabilize a Conveyor Belt with Optional Buffer Pads

A warehouse conveyor belt is divided into n consecutive segments. Segment i has an
instability value instability[i], which may be positive, zero, or negative.

To reduce vibration, the belt must be fully covered from left to right using repair
operations. You may apply two kinds of operations:

1. Standard repair on a single segment i, costing singleCost[i].
2. Buffered repair on exactly two adjacent segments i and i+1, costing pairCost[i].
   A buffered repair may only be used if the absolute difference between
   instability[i] and instability[i+1] is at most limit.

In addition, the engineering team allows up to k reinforcement coupons.
Each coupon can be spent on any one repair operation (single or buffered) and reduces
that operation's cost by discount, but the final cost of that operation cannot go below 0.

Your task is to compute the minimum total cost needed to cover every segment exactly once.

Two repairs cannot overlap, and every segment must belong to exactly one chosen operation.

Constraints:
- 1 <= n <= 2000
- 0 <= k <= 2000
- instability.length == singleCost.length == n
- pairCost.length == n - 1
- -10^9 <= instability[i] <= 10^9
- 0 <= singleCost[i], pairCost[i], discount <= 10^9
- 0 <= limit <= 10^9

A dynamic programming solution is used where:
- We process the belt from left to right.
- dp[i][c] conceptually means:
  minimum cost to cover the first i segments using exactly c coupons.
- From position i, we may:
  - cover segment i alone
  - cover segments i and i+1 together if the pair is valid
- For each operation, we may either:
  - not use a coupon
  - use one coupon, reducing that operation's cost by discount but not below 0

To save memory, only two rolling rows are kept:
- current row for prefix length i
- next rows for prefix lengths i+1 and i+2
*/
public class Solution {

    /**
     * Computes the minimum total cost to cover all conveyor segments exactly once.
     *
     * Dynamic programming idea:
     * We scan the segments from left to right. At each position i, we know the best cost
     * for covering the first i segments with a certain number of coupons already used.
     * Then we try all legal next operations:
     * 1) repair segment i alone
     * 2) repair segments i and i+1 together if allowed
     * For each operation, we also try:
     * - without coupon
     * - with one coupon (if we still have one available)
     *
     * Because each transition only goes from i to i+1 or i+2, we can use rolling arrays
     * instead of a full 2D table, which reduces memory usage significantly.
     *
     * @param instability instability values of each segment
     * @param singleCost cost of repairing one segment individually
     * @param pairCost cost of repairing adjacent segments i and i+1 together
     * @param limit maximum allowed absolute instability difference for a buffered repair
     * @param k maximum number of coupons available
     * @param discount amount subtracted from one operation when a coupon is used
     * @return the minimum possible total repair cost
     * Time complexity: O(n * k)
     * Space complexity: O(k)
     */
    public long minimumCost(int[] instability, int[] singleCost, int[] pairCost, int limit, int k, int discount) {
        int n = instability.length;

        // A very large value used as "infinity" for minimization DP.
        long INF = Long.MAX_VALUE / 4;

        // dp0 represents the current DP row:
        // dp0[c] = minimum cost to cover the first i segments using exactly c coupons.
        long[] dp0 = new long[k + 1];

        // dp1 will accumulate transitions that end at prefix length i + 1.
        long[] dp1 = new long[k + 1];

        // dp2 will accumulate transitions that end at prefix length i + 2.
        long[] dp2 = new long[k + 1];

        Arrays.fill(dp0, INF);
        Arrays.fill(dp1, INF);
        Arrays.fill(dp2, INF);

        // Base case:
        // Covering zero segments with zero coupons costs zero.
        dp0[0] = 0;

        // We process positions i = 0 to n - 1.
        // At the start of each iteration:
        // - dp0 corresponds to prefix length i
        // - dp1 corresponds to prefix length i+1, but only from earlier transitions
        // - dp2 corresponds to prefix length i+2, but only from earlier transitions
        for (int i = 0; i < n; i++) {
            // Before generating new transitions from state "prefix i",
            // dp1 already contains all ways to reach prefix i+1 from previous positions.
            // dp2 already contains all ways to reach prefix i+2 from previous positions.
            //
            // We now use dp0 to push transitions forward.

            long singleNoCoupon = singleCost[i];
            long singleWithCoupon = Math.max(0L, (long) singleCost[i] - discount);

            boolean canPair = false;
            long pairNoCoupon = 0L;
            long pairWithCoupon = 0L;

            if (i + 1 < n) {
                long diff = Math.abs((long) instability[i] - instability[i + 1]);
                if (diff <= limit) {
                    canPair = true;
                    pairNoCoupon = pairCost[i];
                    pairWithCoupon = Math.max(0L, (long) pairCost[i] - discount);
                }
            }

            // Try every possible coupon count used so far.
            for (int used = 0; used <= k; used++) {
                long current = dp0[used];
                if (current >= INF) {
                    continue;
                }

                // ------------------------------------------------------------
                // Option 1: Repair segment i alone, without using a coupon.
                // This covers one more segment, so we transition to prefix i+1.
                // Coupon count stays the same.
                // ------------------------------------------------------------
                long candidateSingleNoCoupon = current + singleNoCoupon;
                if (candidateSingleNoCoupon < dp1[used]) {
                    dp1[used] = candidateSingleNoCoupon;
                }

                // ------------------------------------------------------------
                // Option 2: Repair segment i alone, using one coupon.
                // This also covers one more segment, so we transition to prefix i+1.
                // Coupon count increases by 1.
                // ------------------------------------------------------------
                if (used < k) {
                    long candidateSingleWithCoupon = current + singleWithCoupon;
                    if (candidateSingleWithCoupon < dp1[used + 1]) {
                        dp1[used + 1] = candidateSingleWithCoupon;
                    }
                }

                // ------------------------------------------------------------
                // Option 3 / 4: Buffered repair on segments i and i+1.
                // This is only legal if:
                // - i+1 exists
                // - the instability difference is within the limit
                //
                // It covers two segments, so we transition to prefix i+2.
                // ------------------------------------------------------------
                if (canPair) {
                    long candidatePairNoCoupon = current + pairNoCoupon;
                    if (candidatePairNoCoupon < dp2[used]) {
                        dp2[used] = candidatePairNoCoupon;
                    }

                    if (used < k) {
                        long candidatePairWithCoupon = current + pairWithCoupon;
                        if (candidatePairWithCoupon < dp2[used + 1]) {
                            dp2[used + 1] = candidatePairWithCoupon;
                        }
                    }
                }
            }

            // ------------------------------------------------------------
            // Move the rolling window forward by one position.
            //
            // After finishing transitions from prefix i:
            // - the old dp1 becomes the new dp0 (states for prefix i+1)
            // - the old dp2 becomes the new dp1 (states for prefix i+2, now one step closer)
            // - dp2 is reset to INF because it will represent future prefix i+3
            // ------------------------------------------------------------
            long[] oldDp0 = dp0;
            dp0 = dp1;
            dp1 = dp2;
            dp2 = oldDp0;
            Arrays.fill(dp2, INF);
        }

        // After processing all positions, dp0 corresponds to prefix length n,
        // meaning all segments are covered.
        // We may use fewer than k coupons, so take the minimum over all coupon counts.
        long answer = INF;
        for (int used = 0; used <= k; used++) {
            answer = Math.min(answer, dp0[used]);
        }
        return answer;
    }

    /**
     * Convenience wrapper matching a typical interview-style method name.
     *
     * @param instability instability values of each segment
     * @param singleCost cost of repairing one segment individually
     * @param pairCost cost of repairing adjacent segments i and i+1 together
     * @param limit maximum allowed absolute instability difference for a buffered repair
     * @param k maximum number of coupons available
     * @param discount amount subtracted from one operation when a coupon is used
     * @return the minimum possible total repair cost
     * Time complexity: O(n * k)
     * Space complexity: O(k)
     */
    public long solve(int[] instability, int[] singleCost, int[] pairCost, int limit, int k, int discount) {
        return minimumCost(instability, singleCost, pairCost, limit, k, discount);
    }

    /**
     * Demonstrates the solution on the sample-style inputs from the prompt.
     *
     * Note:
     * The prompt's first example text contains a contradiction:
     * it states Output: 15, but its own reasoning concludes the true optimum is 16.
     * The algorithm correctly computes 16 for that input.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the solver calls
     * Space complexity: O(1), excluding the solver's internal DP arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] instability1 = {4, 7, 6, 6};
        int[] singleCost1 = {8, 5, 7, 4};
        int[] pairCost1 = {9, 20, 6};
        int limit1 = 2;
        int k1 = 1;
        int discount1 = 3;

        long result1 = solution.minimumCost(instability1, singleCost1, pairCost1, limit1, k1, discount1);
        System.out.println(result1); // Correct result is 16

        int[] instability2 = {3, 3, 10, 11, 12};
        int[] singleCost2 = {6, 6, 8, 5, 5};
        int[] pairCost2 = {7, 50, 6, 9};
        int limit2 = 1;
        int k2 = 2;
        int discount2 = 4;

        long result2 = solution.minimumCost(instability2, singleCost2, pairCost2, limit2, k2, discount2);
        System.out.println(result2); // Correct result is 10
    }
}