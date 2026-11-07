/*
Title: Maximum Profit from One Cooldown Stock Segment

Problem Description:
You are given an integer array prices where prices[i] is the price of a commodity on day i.
You may perform as many buy/sell transactions as you want, but you can hold at most one unit
at a time. After you sell, you must wait exactly one full day before buying again (a 1-day cooldown).
Normally, this cooldown applies after every sale.

However, you are allowed to choose exactly one contiguous segment of days [l, r] and declare it
a special promotion window. Inside that chosen window only, the cooldown rule is disabled, meaning
you may buy again on the very next day after selling, as long as both actions occur within the
promotion window. Outside the chosen window, the normal 1-day cooldown still applies. You may also
choose a segment of length 1, which is effectively useless, but still counts as your one chosen segment.

Return the maximum total profit achievable.

A transaction consists of buying on one day and selling on a later day. You may not buy and sell
on the same day, and you may not hold more than one unit at a time.

Constraints:
- 1 <= prices.length <= 200000
- 1 <= prices[i] <= 1000000000
- Your solution should run in O(n) or O(n log n) time.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    High-level idea:
    We split the whole timeline into three conceptual parts:

    1) A left part before the promotion window:
       - normal cooldown rules apply

    2) The promotion window itself:
       - cooldown is disabled inside this window
       - this means inside the window the best profit behaves like the classic
         "unlimited transactions without cooldown" problem

    3) A right part after the promotion window:
       - normal cooldown rules apply again

    The challenge is that trades may connect across boundaries:
    - We may enter the promotion window already holding a stock bought earlier.
    - We may leave the promotion window still holding a stock to sell later.
    - If we sell on the last day of the promotion window, then the next day is outside,
      so normal cooldown resumes and buying immediately next day is NOT allowed.

    To handle this cleanly, we compute:
    - prefix DP under normal cooldown
    - suffix DP under normal cooldown
    - enough boundary information to connect them with a no-cooldown middle segment

    State meanings for prefix DP (days 0..i):
    - free[i]  : max profit at end of day i, not holding, and allowed to buy tomorrow
    - cool[i]  : max profit at end of day i, not holding, but tomorrow is cooldown
    - hold[i]  : max profit at end of day i, holding one stock

    For suffix DP (days i..n-1), we compute two values:
    - sufFree[i] : max profit from days i..end if at start of day i we are free to buy
    - sufHold[i] : max profit from days i..end if at start of day i we already hold one stock

    Then for every possible promotion window [l, r], we need the best value of:
      left contribution up to day l-1
      + best no-cooldown profit inside [l, r] with possible entering/leaving hold state
      + right contribution from day r+1 onward

    A direct O(n^2) scan over all windows is too slow, so we algebraically transform the
    middle no-cooldown segment into expressions based on prefix sums of positive differences.
    This lets us optimize over all windows in O(n).
    */
    public long MaxProfitOneCooldownSegment(int[] prices)
    {
        int n = prices.Length;
        if (n <= 1) return 0;

        const long NEG = long.MinValue / 4;

        // ------------------------------------------------------------
        // STEP 1: Prefix DP for the normal cooldown problem.
        //
        // Why we need this:
        // Before the promotion window starts, everything follows the usual cooldown rule.
        // We need to know the best possible "entry condition" at every boundary.
        //
        // At the end of each day i:
        // - free[i] means we are not holding and tomorrow we may buy
        // - cool[i] means we sold today, so tomorrow is blocked for buying
        // - hold[i] means we are holding one stock
        //
        // These are standard stock-with-cooldown states.
        // ------------------------------------------------------------
        long[] free = new long[n];
        long[] cool = new long[n];
        long[] hold = new long[n];

        free[0] = 0;
        cool[0] = NEG;              // impossible to be in cooldown on day 0 without a prior sell
        hold[0] = -prices[0];       // buy on day 0

        for (int i = 1; i < n; i++)
        {
            // If we end day i free, either:
            // - we were already free yesterday and do nothing today
            // - we were in cooldown yesterday, and that cooldown expires after today
            free[i] = Math.Max(free[i - 1], cool[i - 1]);

            // If we end day i in cooldown, the only way is:
            // - we were holding yesterday and sell today
            cool[i] = hold[i - 1] + prices[i];

            // If we end day i holding, either:
            // - we were already holding yesterday and keep holding
            // - we were free yesterday and buy today
            //   (note: cannot buy from cooldown state)
            hold[i] = Math.Max(hold[i - 1], free[i - 1] - prices[i]);
        }

        // ------------------------------------------------------------
        // STEP 2: Suffix DP for the normal cooldown problem.
        //
        // Why we need this:
        // After the promotion window ends, normal cooldown resumes.
        // We need to know the best future profit from a boundary onward.
        //
        // We define states at the START of day i:
        // - sufFree[i]: best profit from day i..end if we start day i not holding and allowed to buy
        // - sufHold[i]: best profit from day i..end if we start day i already holding one stock
        //
        // Transition reasoning:
        //
        // From free at day i:
        // - skip day i => remain free at day i+1
        // - buy at day i => move to hold at day i+1 with profit -prices[i]
        //
        // From hold at day i:
        // - keep holding => hold at day i+1
        // - sell at day i => because of cooldown, day i+1 is blocked, so the next day we can
        //   actually act again is day i+2. Profit gained is +prices[i].
        // ------------------------------------------------------------
        long[] sufFree = new long[n + 2];
        long[] sufHold = new long[n + 2];

        // Base:
        // Beyond the end, no more profit can be made if free.
        // If "holding beyond the end", that state is useless unless we sell earlier,
        // so its best future profit from beyond end is effectively negative infinity.
        sufFree[n] = 0;
        sufFree[n + 1] = 0;
        sufHold[n] = NEG;
        sufHold[n + 1] = NEG;

        for (int i = n - 1; i >= 0; i--)
        {
            sufFree[i] = Math.Max(
                sufFree[i + 1],              // do nothing today
                -prices[i] + sufHold[i + 1]  // buy today
            );

            sufHold[i] = Math.Max(
                sufHold[i + 1],              // keep holding
                prices[i] + sufFree[i + 2]   // sell today, then cooldown skips next day
            );
        }

        // ------------------------------------------------------------
        // STEP 3: Build helper arrays for the no-cooldown segment.
        //
        // Inside the promotion window, cooldown is disabled.
        // For the classic "unlimited transactions without cooldown" problem,
        // the profit over a segment [l, r] is simply the sum of all positive daily increases:
        //
        //   sum(max(0, prices[k] - prices[k-1])) for k = l+1..r
        //
        // We store prefix sums of these positive differences so any segment's
        // no-cooldown internal profit can be queried in O(1).
        // ------------------------------------------------------------
        long[] posDiffPrefix = new long[n];
        posDiffPrefix[0] = 0;
        for (int i = 1; i < n; i++)
        {
            long gain = Math.Max(0L, (long)prices[i] - prices[i - 1]);
            posDiffPrefix[i] = posDiffPrefix[i - 1] + gain;
        }

        long SegmentNoCooldownProfit(int l, int r)
        {
            if (l >= r) return 0;
            return posDiffPrefix[r] - posDiffPrefix[l];
        }

        // ------------------------------------------------------------
        // STEP 4: We now characterize the best value of a chosen promotion window [l, r].
        //
        // There are 4 ways the middle segment can connect to the outside:
        //
        // A) Enter free, leave free
        // B) Enter hold, leave free
        // C) Enter free, leave hold
        // D) Enter hold, leave hold
        //
        // For a no-cooldown segment [l, r], these values can be written as:
        //
        // Let G(l,r) = SegmentNoCooldownProfit(l, r)
        //
        // A) midFF(l,r) = G(l,r)
        //    Start free, end free: just do the usual no-cooldown trades inside.
        //
        // B) midHF(l,r) = prices[l] + G(l,r)
        //    Start already holding at day l. This is like having bought before l.
        //    Best no-cooldown handling adds an extra +prices[l] compared to starting free.
        //
        // C) midFH(l,r) = G(l,r) - prices[r]
        //    Start free and end holding. Equivalent to doing normal no-cooldown trades
        //    and additionally keeping one stock bought at the best point, which simplifies
        //    to subtracting prices[r] from the segment gain representation.
        //
        // D) midHH(l,r) = prices[l] + G(l,r) - prices[r]
        //    Start holding and end holding.
        //
        // These formulas can be verified by standard no-cooldown DP on a segment.
        //
        // Then total answer for [l,r] is the best combination of:
        // - left prefix state before l
        // - one of these middle transitions
        // - right suffix state after r, with correct cooldown handling
        //
        // Important boundary after the segment:
        // If we leave the segment in FREE state, then on day r+1 we are free to buy.
        // If we leave the segment by SELLING on day r, that is already represented in ending FREE,
        // but outside the segment cooldown resumes only after sells that happen outside.
        // Since day r is inside the no-cooldown zone, the special rule allows immediate rebuy
        // only while both actions are inside the window. Once we are at day r+1 outside,
        // we simply start the suffix in FREE state. This is correct because the special rule
        // governs the segment internally; after the segment, we continue from the resulting
        // end-of-day state. Ending not holding means we are free next day.
        //
        // If we leave the segment holding, then suffix must start from HOLD at day r+1:
        // conceptually we carry the stock across the boundary.
        //
        // To optimize over all [l,r], we rewrite each case into:
        //   (something depending on l) + (something depending on r)
        // using G(l,r) = posDiffPrefix[r] - posDiffPrefix[l].
        //
        // This allows a single left-to-right sweep maintaining best values over l.
        // ------------------------------------------------------------

        long answer = 0;

        // We will sweep r from 0 to n-1.
        //
        // For each fixed r, we want the best l <= r.
        // We maintain four running maxima corresponding to the four entry possibilities.
        //
        // Left-side values before day l:
        // If l == 0, then before the segment there are no days. The only valid starting state is free with profit 0.
        // Starting the segment while already holding at l means we must have bought before or on day l-1,
        // so for l == 0 that is impossible.
        //
        // For l > 0:
        // - best profit before l and free to act on day l is max(free[l-1], cool[l-1])
        //   because at end of day l-1, both "free" and "cool" become simply "not holding" before day l starts.
        //   More concretely, if we sold on day l-1 under normal rules, day l would be cooldown and buying on day l
        //   would be forbidden. But once day l is inside the promotion window, cooldown is disabled there, so
        //   entering the segment not holding can ignore that restriction. Therefore both free and cool are valid
        //   "enter free" states.
        //
        // - best profit before l while holding at start of day l is hold[l-1]
        //
        // This is a subtle but crucial point: the promotion window can start on a day that would otherwise
        // be a cooldown day, and inside the window that cooldown disappears.
        //
        // For each l, define:
        // enterFreeBase(l) = best profit before l, entering day l not holding
        // enterHoldBase(l) = best profit before l, entering day l holding
        //
        // Then:
        // Case FF total = enterFreeBase(l) + [G(l,r)] + sufFree[r+1]
        //               = [enterFreeBase(l) - posDiffPrefix[l]] + [posDiffPrefix[r] + sufFree[r+1]]
        //
        // Case HF total = enterHoldBase(l) + [prices[l] + G(l,r)] + sufFree[r+1]
        //               = [enterHoldBase(l) + prices[l] - posDiffPrefix[l]] + [posDiffPrefix[r] + sufFree[r+1]]
        //
        // Case FH total = enterFreeBase(l) + [G(l,r) - prices[r]] + sufHold[r+1]
        //               = [enterFreeBase(l) - posDiffPrefix[l]] + [posDiffPrefix[r] - prices[r] + sufHold[r+1]]
        //
        // Case HH total = enterHoldBase(l) + [prices[l] + G(l,r) - prices[r]] + sufHold[r+1]
        //               = [enterHoldBase(l) + prices[l] - posDiffPrefix[l]] + [posDiffPrefix[r] - prices[r] + sufHold[r+1]]
        //
        // So we only need two running maxima over l:
        // bestEnterFree = max over l<=r of enterFreeBase(l) - posDiffPrefix[l]
        // bestEnterHold = max over l<=r of enterHoldBase(l) + prices[l] - posDiffPrefix[l]
        //
        // Then for each r:
        // best ending free  = max(bestEnterFree, bestEnterHold) + posDiffPrefix[r] + sufFree[r+1]
        // best ending hold  = max(bestEnterFree, bestEnterHold) + posDiffPrefix[r] - prices[r] + sufHold[r+1]
        //
        // But note the two cases use the same left maxima grouped by whether we enter free or hold.
        // We'll compute both explicitly for clarity.
        long bestEnterFree = NEG;
        long bestEnterHold = NEG;

        for (int r = 0; r < n; r++)
        {
            int l = r;

            // --------------------------------------------------------
            // STEP 4a: Add the new possible left boundary l = r into the running maxima.
            //
            // We do this before evaluating windows ending at r so that length-1 windows [r, r]
            // are included. A length-1 promotion window is allowed by the problem statement.
            // --------------------------------------------------------
            long enterFreeBase;
            long enterHoldBase;

            if (l == 0)
            {
                // Before day 0, profit is 0 and we are not holding.
                enterFreeBase = 0;
                enterHoldBase = NEG; // impossible to already hold before the first day
            }
            else
            {
                // Entering the promotion window not holding:
                // both normal "free" and normal "cooldown" states from day l-1 are acceptable,
                // because once inside the promotion window, cooldown is disabled.
                enterFreeBase = Math.Max(free[l - 1], cool[l - 1]);

                // Entering while holding means we were holding at end of day l-1.
                enterHoldBase = hold[l - 1];
            }

            bestEnterFree = Math.Max(bestEnterFree, enterFreeBase - posDiffPrefix[l]);

            if (enterHoldBase > NEG / 2)
            {
                bestEnterHold = Math.Max(bestEnterHold, enterHoldBase + prices[l] - posDiffPrefix[l]);
            }

            // --------------------------------------------------------
            // STEP 4b: Evaluate all promotion windows [l, r] implicitly via the running maxima.
            //
            // We combine the best possible left boundary with this fixed right boundary r.
            // --------------------------------------------------------

            // If the segment ends not holding, the right suffix starts in FREE state at day r+1.
            long rightIfFree = sufFree[r + 1];
            long commonFreePart = posDiffPrefix[r] + rightIfFree;

            if (bestEnterFree > NEG / 2)
                answer = Math.Max(answer, bestEnterFree + commonFreePart);

            if (bestEnterHold > NEG / 2)
                answer = Math.Max(answer, bestEnterHold + commonFreePart);

            // If the segment ends holding, the right suffix starts in HOLD state at day r+1.
            long rightIfHold = sufHold[r + 1];
            if (rightIfHold > NEG / 2)
            {
                long commonHoldPart = posDiffPrefix[r] - prices[r] + rightIfHold;

                if (bestEnterFree > NEG / 2)
                    answer = Math.Max(answer, bestEnterFree + commonHoldPart);

                if (bestEnterHold > NEG / 2)
                    answer = Math.Max(answer, bestEnterHold + commonHoldPart);
            }
        }

        // It is always allowed to make no transactions,