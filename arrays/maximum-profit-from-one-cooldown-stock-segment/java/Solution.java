import java.util.*;

/*
 * Title: Maximum Profit from One Cooldown Stock Segment
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array prices where prices[i] is the price of a commodity on day i.
 * You may perform as many buy/sell transactions as you want, but you can hold at most one unit
 * at a time. After you sell, you must wait exactly one full day before buying again (a 1-day cooldown).
 * Normally, this cooldown applies after every sale.
 *
 * However, you are allowed to choose exactly one contiguous segment of days [l, r] and declare it
 * a special promotion window. Inside that chosen window only, the cooldown rule is disabled,
 * meaning you may buy again on the very next day after selling, as long as both actions occur
 * within the promotion window. Outside the chosen window, the normal 1-day cooldown still applies.
 * You may also choose a segment of length 1, which is effectively useless, but still counts as your
 * one chosen segment.
 *
 * Return the maximum total profit achievable.
 *
 * A transaction consists of buying on one day and selling on a later day. You may not buy and sell
 * on the same day, and you may not hold more than one unit at a time.
 *
 * Constraints:
 * - 1 <= prices.length <= 200000
 * - 1 <= prices[i] <= 1000000000
 * - Your solution should run in O(n) or O(n log n) time.
 *
 * Key idea of this solution:
 * We split the timeline into three parts:
 *
 *   1) Prefix before the promotion window: normal cooldown rules.
 *   2) The promotion window itself: no cooldown inside the window.
 *   3) Suffix after the promotion window: normal cooldown rules again.
 *
 * The difficult part is "gluing" these three parts together correctly.
 *
 * We solve it with dynamic programming from both directions:
 *
 * Forward DP (normal cooldown):
 *   - free[i]: best profit after processing days [0..i], ending day i with no stock and allowed to buy.
 *   - cool[i]: best profit after processing days [0..i], ending day i with no stock but in cooldown.
 *   - hold[i]: best profit after processing days [0..i], ending day i while holding one stock.
 *
 * Reverse DP (normal cooldown on suffix):
 *   For a suffix starting at day s, we need the best future profit depending on the state at the
 *   start of day s:
 *   - startFree[s]: best profit from days [s..n-1] if at start of day s we are free to buy.
 *   - startHold[s]: best profit from days [s..n-1] if at start of day s we already hold one stock.
 *   - startCool[s]: best profit from days [s..n-1] if day s is a forced cooldown day.
 *
 * Inside the promotion window [l..r], cooldown is disabled. That means the window behaves like the
 * classic unlimited-transactions-without-cooldown problem, but we must also track whether we enter
 * the window holding or not, and whether we leave the window holding or not.
 *
 * For a fixed right boundary r, as we move the left boundary l from r down to 0, we can maintain
 * four values for the window [l..r]:
 *
 *   A = best profit inside [l..r] if we enter day l with no stock, and leave after day r with no stock
 *   B = best profit inside [l..r] if we enter day l with no stock, and leave after day r holding stock
 *   C = best profit inside [l..r] if we enter day l holding stock, and leave after day r with no stock
 *   D = best profit inside [l..r] if we enter day l holding stock, and leave after day r holding stock
 *
 * These four values can be updated in O(1) when extending the window one day to the left.
 *
 * Then for each pair (l, r), the total answer is:
 *
 *   best prefix state before l
 *   + best window transition for [l..r]
 *   + best suffix continuation starting at r+1
 *
 * But enumerating all (l, r) would be O(n^2), too slow.
 *
 * The crucial optimization:
 * For each fixed r, the formula can be rearranged so that only two quantities depending on l are needed:
 *
 *   M0(l) = max(prefixFreeBeforeL + A(l,r), prefixHoldBeforeL + C(l,r))
 *   M1(l) = max(prefixFreeBeforeL + B(l,r), prefixHoldBeforeL + D(l,r))
 *
 * and the final combination with the suffix at s = r + 1 is:
 *
 *   max(
 *       M0(l) + startFree[s],   // leave window with no stock, suffix starts free
 *       M1(l) + startHold[s],   // leave window holding stock, suffix starts holding
 *       M0(l) + startCool[s]    // only possible if we sold on day r; handled by a refined split
 *   )
 *
 * To correctly distinguish whether leaving with no stock on day r came from "rest" or "sell today",
 * we maintain two no-stock exit values for the window:
 *
 *   no0 = leave with no stock and day r is NOT a sell day
 *   no1 = leave with no stock and day r IS a sell day
 *
 * This lets us connect to suffix state:
 *   - no0 -> suffix starts free on day r+1
 *   - no1 -> suffix starts in cooldown on day r+1
 *
 * We again maintain all needed transitions in O(1) per extension.
 *
 * Overall complexity is O(n):
 *   - O(n) forward DP
 *   - O(n) reverse DP
 *   - O(n) scan that incrementally updates the promotion-window transitions
 *
 * The implementation below is heavily commented and designed to be beginner-friendly.
 */
public class Solution {

    /**
     * A very negative number used as "minus infinity" for impossible DP states.
     * We choose a value safely far below any achievable profit.
     */
    private static final long NEG_INF = Long.MIN_VALUE / 4;

    /**
     * Computes the maximum total profit when exactly one contiguous segment is chosen
     * as a promotion window where cooldown is disabled.
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     *
     * @param prices the price on each day
     * @return the maximum achievable profit
     */
    public long maxProfitOneCooldownSegment(int[] prices) {
        int n = prices.length;
        if (n == 0) {
            return 0L;
        }

        /*
         * ------------------------------------------------------------
         * Part 1: Forward DP for the normal cooldown model on prefixes.
         * ------------------------------------------------------------
         *
         * State meaning after processing day i:
         *
         * free[i]:
         *   best profit if we end day i with no stock and are allowed to buy tomorrow.
         *
         * cool[i]:
         *   best profit if we end day i with no stock and tomorrow is a forced cooldown day.
         *   This happens exactly when we sell on day i.
         *
         * hold[i]:
         *   best profit if we end day i holding one stock.
         *
         * Transitions for day i:
         *
         *   newFree = max(oldFree, oldCool)
         *       - do nothing if already free
         *       - cooldown day passes and becomes free
         *
         *   newHold = max(oldHold, oldFree - price[i])
         *       - keep holding
         *       - buy today, only allowed from free
         *
         *   newCool = oldHold + price[i]
         *       - sell today, so tomorrow is cooldown
         */
        long[] free = new long[n];
        long[] cool = new long[n];
        long[] hold = new long[n];

        free[0] = 0L;
        cool[0] = NEG_INF;
        hold[0] = -prices[0];

        for (int i = 1; i < n; i++) {
            long newFree = Math.max(free[i - 1], cool[i - 1]);
            long newHold = Math.max(hold[i - 1], free[i - 1] - prices[i]);
            long newCool = hold[i - 1] + prices[i];

            free[i] = newFree;
            hold[i] = newHold;
            cool[i] = newCool;
        }

        /*
         * For each l, we need the best state BEFORE day l starts.
         *
         * prefixFreeBefore[l]:
         *   best profit before day l if we are free to buy on day l.
         *
         * prefixHoldBefore[l]:
         *   best profit before day l if we already hold one stock at the start of day l.
         *
         * These are derived from the DP after day l-1.
         *
         * Special case l = 0:
         *   before day 0 starts, we are free with profit 0 and cannot already be holding.
         */
        long[] prefixFreeBefore = new long[n];
        long[] prefixHoldBefore = new long[n];

        prefixFreeBefore[0] = 0L;
        prefixHoldBefore[0] = NEG_INF;

        for (int l = 1; l < n; l++) {
            prefixFreeBefore[l] = Math.max(free[l - 1], cool[l - 1]);
            prefixHoldBefore[l] = hold[l - 1];
        }

        /*
         * ------------------------------------------------------------
         * Part 2: Reverse DP for the normal cooldown model on suffixes.
         * ------------------------------------------------------------
         *
         * We define states at the START of day s:
         *
         * startFree[s]:
         *   best profit from days [s..n-1] if at the start of day s we are free to buy.
         *
         * startHold[s]:
         *   best profit from days [s..n-1] if at the start of day s we already hold one stock.
         *
         * startCool[s]:
         *   best profit from days [s..n-1] if day s is a forced cooldown day.
         *
         * Reverse transitions:
         *
         * If free at start of day s:
         *   - skip day s -> startFree[s+1]
         *   - buy today   -> startHold[s+1] - price[s]
         *
         * If hold at start of day s:
         *   - keep holding -> startHold[s+1]
         *   - sell today   -> price[s] + startCool[s+1]
         *
         * If cooldown at start of day s:
         *   - cannot buy today, day passes -> startFree[s+1]
         *
         * Base case at s = n:
         *   no days left, profit is 0 regardless of state
         *   (if holding at the very end, best future profit is also 0 because no more actions are possible;
         *    the value of the held stock is already accounted for when it was bought/sold in transitions)
         */
        long[] startFree = new long[n + 1];
        long[] startHold = new long[n + 1];
        long[] startCool = new long[n + 1];

        startFree[n] = 0L;
        startHold[n] = 0L;
        startCool[n] = 0L;

        for (int s = n - 1; s >= 0; s--) {
            startFree[s] = Math.max(startFree[s + 1], startHold[s + 1] - prices[s]);
            startHold[s] = Math.max(startHold[s + 1], prices[s] + startCool[s + 1]);
            startCool[s] = startFree[s + 1];
        }

        /*
         * ------------------------------------------------------------
         * Part 3: Enumerate the promotion window in O(n).
         * ------------------------------------------------------------
         *
         * We process windows ending at each r, but we do NOT try all l explicitly in a nested O(n^2) way.
         *
         * Instead, for each r we maintain DP transitions for the current window [l..r] while moving l left.
         * However, to keep total O(n), we use a rolling formulation where the best answer for windows ending
         * at r can be updated from windows ending at r-1.
         *
         * We maintain four "best combined" values after choosing some l for the current right boundary r:
         *
         * bestNoRest:
         *   best value of (prefix + window) where after day r we have no stock and day r is NOT a sell day.
         *
         * bestNoSell:
         *   best value of (prefix + window) where after day r we have no stock and day r IS a sell day.
         *
         * bestHold:
         *   best value of (prefix + window) where after day r we are holding stock.
         *
         * To update from r-1 to r, we extend every existing window by one day to the right under
         * no-cooldown rules inside the window, and we also allow starting a brand-new window at l = r.
         *
         * Window-internal states for a chosen l:
         *
         *   noRest : leave current right end with no stock, and today was not a sell
         *   noSell : leave current right end with no stock, and today was a sell
         *   hold   : leave current right end holding stock
         *
         * Transitions when appending day price p:
         *
         *   newNoRest = max(oldNoRest, oldNoSell)
         *       - stay without stock and do nothing today
         *
         *   newNoSell = oldHold + p
         *       - sell today
         *
         *   newHold = max(oldHold, max(oldNoRest, oldNoSell) - p)
         *       - keep holding
         *       - buy today (allowed immediately after a sell because inside window cooldown is disabled)
         *
         * For a brand-new window [r..r]:
         *   entering with no stock:
         *       noRest = 0
         *       noSell = impossible
         *       hold   = -price[r]
         *
         *   entering with holding stock:
         *       noRest = impossible
         *       noSell = price[r]   (sell today)
         *       hold   = 0          (do nothing)
         *
         * We combine these with prefix states before l and keep only the best aggregate values.
         */
        long answer = 0L;

        long bestNoRest = NEG_INF;
        long bestNoSell = NEG_INF;
        long bestHoldCombined = NEG_INF;

        for (int r = 0; r < n; r++) {
            long p = prices[r];

            /*
             * Step A: extend all previously tracked windows [l..r-1] to [l..r].
             */
            long extendedNoRest = Math.max(bestNoRest, bestNoSell);
            long extendedNoSell = (bestHoldCombined == NEG_INF) ? NEG_INF : bestHoldCombined + p;
            long extendedHold = Math.max(bestHoldCombined, Math.max(bestNoRest, bestNoSell) == NEG_INF
                    ? NEG_INF
                    : Math.max(bestNoRest, bestNoSell) - p);

            /*
             * Step B: start a brand-new window [r..r].
             *
             * We must consider both possible states before day r:
             *   - free before r
             *   - holding before r
             */
            long prefFree = prefixFreeBefore[r];
            long prefHold = prefixHoldBefore[r];

            /*
             * Enter with no stock:
             *   noRest = 0
             *   noSell = impossible
             *   hold   = -p
             */
            long startFromFree_noRest = prefFree;
            long startFromFree_noSell = NEG_INF;
            long startFromFree_hold = (prefFree == NEG_INF) ? NEG_INF : prefFree - p;

            /*
             * Enter with holding:
             *   noRest = impossible
             *   noSell = +p
             *   hold   = 0
             */
            long startFromHold_noRest = NEG_INF;
            long startFromHold_noSell = (prefHold == NEG_INF) ? NEG_INF : prefHold + p;
            long startFromHold_hold = prefHold;

            long startNoRest = Math.max(startFromFree_noRest, startFromHold_noRest);
            long startNoSell = Math.max(startFromFree_noSell, startFromHold_noSell);
            long startHold = Math.max(startFromFree_hold, startFromHold_hold);

            /*
             * Step C: best windows ending at r are either:
             *   - an extended previous window
             *   - a brand-new window starting at r
             */
            bestNoRest = Math.max(extendedNoRest, startNoRest);
            bestNoSell = Math.max(extendedNoSell, startNoSell);
            bestHoldCombined = Math.max(extendedHold, startHold);

            /*
             * Step D: connect the end of the window at day r to the suffix starting at day r+1.
             *
             * If we leave day r:
             *   - with no stock and today was NOT a sell -> next day starts FREE
             *   - with no stock and today WAS a sell     -> next day starts COOL
             *   - with holding stock                     -> next day starts HOLD
             */
            int s = r + 1;

            long candidate1 = (bestNoRest == NEG_INF) ? NEG_INF : bestNoRest + startFree[s];
            long candidate2 = (bestNoSell == NEG_INF) ? NEG_INF : bestNoSell + startCool[s];
            long candidate3 = (bestHoldCombined == NEG_INF) ? NEG_INF : bestHoldCombined + startHold[s];

            long bestHere = Math.max(candidate1, Math.max(candidate2, candidate3));
            answer = Math.max(answer, bestHere);
        }

        return Math.max(answer, 0L);
    }

    /**
     * Convenience wrapper returning the answer as an int when it fits typical interview examples.
     * Internally, the algorithm uses long because profits can exceed int range.
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     *
     * @param prices the price on each day
     * @return the maximum achievable profit as a long
     */
    public long solve(int[] prices) {
        return maxProfitOneCooldownSegment(prices);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Time complexity: O(n) per test case
     * Space complexity: O(n) per test case
     *
     * @param args command-line arguments, not used
     * @return