"""
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
- The solution should run in O(n) or O(n log n) time.
"""

from typing import List


class Solution:
    def maxProfitOneCooldownSegment(self, prices: List[int]) -> int:
        """
        Compute the maximum profit when we may choose exactly one contiguous segment
        where the usual 1-day cooldown is disabled.

        The key idea is to split the full trading timeline into three logical parts:
        1) A prefix before the promotion window: normal cooldown applies.
        2) The promotion window itself: no cooldown applies.
        3) A suffix after the promotion window: normal cooldown applies again.

        We precompute:
        - Best prefix profits under normal cooldown.
        - Best suffix profits under normal cooldown.
        - Best possible "gain" contributed by a no-cooldown middle segment, while
          carefully handling whether we enter/leave that segment holding a stock or not.

        Then we combine these pieces in O(n).

        Args:
            prices: List of daily prices.

        Returns:
            Maximum achievable total profit.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(prices)
        if n <= 1:
            return 0

        neg_inf: int = -10**30

        # ---------------------------------------------------------------------
        # PART 1: Prefix DP with NORMAL cooldown
        #
        # We process days from left to right and maintain the classic cooldown DP:
        #
        # free[i] = best profit after processing day i, ending with:
        #           - not holding stock
        #           - and allowed to buy on the next day
        #
        # cool[i] = best profit after processing day i, ending with:
        #           - not holding stock
        #           - but in cooldown on the next day because we sold today
        #
        # hold[i] = best profit after processing day i, ending with:
        #           - currently holding one stock
        #
        # Transitions for day i with price p:
        #   new_free = max(old_free, old_cool)
        #   new_cool = old_hold + p
        #   new_hold = max(old_hold, old_free - p)
        #
        # Important detail:
        # - To buy today under normal cooldown, we must come from old_free only.
        #   We cannot buy from old_cool because cooldown blocks buying today.
        #
        # We store arrays so later we can ask:
        # - What is the best profit before some boundary if we are free / cool / hold?
        # ---------------------------------------------------------------------
        pre_free: List[int] = [0] * n
        pre_cool: List[int] = [neg_inf] * n
        pre_hold: List[int] = [neg_inf] * n

        free: int = 0
        cool: int = neg_inf
        hold: int = -prices[0]

        pre_free[0] = free
        pre_cool[0] = cool
        pre_hold[0] = hold

        for i in range(1, n):
            p: int = prices[i]

            new_free: int = max(free, cool)
            new_cool: int = hold + p
            new_hold: int = max(hold, free - p)

            free, cool, hold = new_free, new_cool, new_hold

            pre_free[i] = free
            pre_cool[i] = cool
            pre_hold[i] = hold

        # ---------------------------------------------------------------------
        # PART 2: Suffix DP with NORMAL cooldown
        #
        # Now we need to know the best profit obtainable on a suffix [i..n-1]
        # under normal cooldown, depending on the state at the START of day i.
        #
        # We define:
        #
        # suf_free[i] = best profit from days i..n-1 if at the start of day i:
        #               - we are not holding
        #               - and we are allowed to buy today
        #
        # suf_cool[i] = best profit from days i..n-1 if at the start of day i:
        #               - we are not holding
        #               - but today is a cooldown day, so we cannot buy today
        #
        # suf_hold[i] = best profit from days i..n-1 if at the start of day i:
        #               - we are already holding one stock
        #
        # Backward transitions:
        #
        # If we start day i in free:
        #   - do nothing today -> tomorrow still free: suf_free[i+1]
        #   - buy today -> tomorrow start holding: suf_hold[i+1] - prices[i]
        #   so:
        #   suf_free[i] = max(suf_free[i+1], suf_hold[i+1] - prices[i])
        #
        # If we start day i in cool:
        #   - cooldown forces us to do nothing today
        #   - tomorrow we become free
        #   so:
        #   suf_cool[i] = suf_free[i+1]
        #
        # If we start day i holding:
        #   - do nothing today -> tomorrow still holding: suf_hold[i+1]
        #   - sell today -> tomorrow is cooldown: prices[i] + suf_cool[i+1]
        #   so:
        #   suf_hold[i] = max(suf_hold[i+1], prices[i] + suf_cool[i+1])
        #
        # Base beyond the last day:
        #   suf_free[n] = 0
        #   suf_cool[n] = 0
        #   suf_hold[n] = -infinity  (ending still holding is invalid / useless)
        # ---------------------------------------------------------------------
        suf_free: List[int] = [0] * (n + 1)
        suf_cool: List[int] = [0] * (n + 1)
        suf_hold: List[int] = [neg_inf] * (n + 1)

        suf_free[n] = 0
        suf_cool[n] = 0
        suf_hold[n] = neg_inf

        for i in range(n - 1, -1, -1):
            p = prices[i]
            suf_free[i] = max(suf_free[i + 1], suf_hold[i + 1] - p)
            suf_cool[i] = suf_free[i + 1]
            suf_hold[i] = max(suf_hold[i + 1], p + suf_cool[i + 1])

        # ---------------------------------------------------------------------
        # PART 3: Build helper arrays for the NO-COOLDOWN middle segment
        #
        # Inside the promotion window, cooldown is disabled.
        #
        # For a no-cooldown market on a segment [l..r], the standard DP is:
        #
        # nc_cash = best profit after current day, not holding
        # nc_hold = best profit after current day, holding
        #
        # Transitions:
        #   new_cash = max(nc_cash, nc_hold + price)
        #   new_hold = max(nc_hold, nc_cash - price)
        #
        # This is equivalent to unlimited transactions without cooldown.
        #
        # But we need more than just "best profit on [l..r]".
        # We need to combine the middle segment with prefix/suffix states.
        #
        # Specifically, for each possible right boundary r, we want the best value of:
        #
        #   prefix_state_value_at_(l-1) + effect_of_no_cooldown_segment_[l..r]
        #
        # where the segment may start in one of two useful ways:
        #
        # A) Enter segment NOT holding.
        #    Then inside the segment we can end:
        #    - not holding  -> contributes some profit delta
        #    - holding      -> contributes some profit delta
        #
        # B) Enter segment ALREADY holding.
        #    This means a stock bought before the segment is carried into it.
        #    Again, inside the segment we can end:
        #    - not holding
        #    - holding
        #
        # To support all l efficiently, we derive four "best up to r" values:
        #
        # mid_nn[r] = best over all l<=r of:
        #             (best prefix profit before l with no stock and buy-allowed)
        #             + (best no-cooldown profit on [l..r], start no-stock, end no-stock)
        #
        # mid_nh[r] = best over all l<=r of:
        #             (best prefix profit before l with no stock and buy-allowed)
        #             + (best no-cooldown profit on [l..r], start no-stock, end holding)
        #
        # mid_hn[r] = best over all l<=r of:
        #             (best prefix profit before l while holding a stock)
        #             + (best no-cooldown profit on [l..r], start holding, end no-stock)
        #
        # mid_hh[r] = best over all l<=r of:
        #             (best prefix profit before l while holding a stock)
        #             + (best no-cooldown profit on [l..r], start holding, end holding)
        #
        # Then after the segment ends at r, we attach the suffix starting at day r+1:
        #
        # - If segment ends not holding:
        #     next day is free, so add suf_free[r+1]
        #
        # - If segment ends holding:
        #     next day starts holding, so add suf_hold[r+1]
        #
        # There is one subtle but very important rule at the LEFT boundary:
        # If day l is inside the promotion window, then buying on day l is allowed
        # even if day l would have been a cooldown day under the normal rule,
        # because cooldown is disabled inside the window.
        #
        # Therefore, when entering the segment with no stock, the usable prefix value
        # before l is:
        #   max(prefix free at l-1, prefix cool at l-1)
        #
        # Why?
        # - If prefix ended in free, obviously we can start segment and buy.
        # - If prefix ended in cool, day l is inside the no-cooldown window, so the
        #   cooldown restriction does not block buying on day l.
        #
        # However, to enter the segment ALREADY holding, the only valid prefix source
        # is prefix hold at l-1.
        #
        # We can update these four values online while sweeping r from left to right.
        # ---------------------------------------------------------------------
        answer: int = 0

        # This baseline corresponds to choosing a useless length-1 segment somewhere
        # and effectively just doing normal cooldown trading everywhere.
        answer = max(pre_free[n - 1], pre_cool[n - 1], 0)

        # Running best values for all possible segment starts l up to current r.
        mid_nn_best: int = neg_inf
        mid_nh_best: int = neg_inf
        mid_hn_best: int = neg_inf
        mid_hh_best: int = neg_inf

        for r in range(n):
            p: int = prices[r]

            # -------------------------------------------------------------
            # Step 3.1: Consider starting a NEW segment at l = r.
            #
            # We need the best prefix value before day r, i.e. after day r-1.
            #
            # If r == 0, there is no prefix:
            #   - no-stock value is 0
            #   - holding value is impossible
            #
            # If r > 0:
            #   - entering segment with no stock can come from either pre_free[r-1]
            #     or pre_cool[r-1], because day r is inside the promotion window
            #     so cooldown does not block buying on day r.
            #   - entering segment already holding can only come from pre_hold[r-1].
            #
            # For a length-1 segment [r, r], the no-cooldown DP states are:
            #
            # Start no-stock:
            #   end no-stock: same profit
            #   end holding : buy today => profit -p
            #
            # Start holding:
            #   end holding : same profit
            #   end no-stock: sell today => profit +p
            #
            # We use these as candidate initial values for the running best states.
            # -------------------------------------------------------------
            if r == 0:
                start_no_stock: int = 0
                start_holding: int = neg_inf
            else:
                start_no_stock = max(pre_free[r - 1], pre_cool[r - 1])
                start_holding = pre_hold[r - 1]

            cand_nn: int = start_no_stock
            cand_nh: int = start_no_stock - p
            cand_hn: int = start_holding + p if start_holding > neg_inf // 2 else neg_inf
            cand_hh: int = start_holding

            # -------------------------------------------------------------
            # Step 3.2: Extend all previously started segments by one more day r
            # using the standard no-cooldown transitions.
            #
            # Previous running states represent best values for segments ending at r-1.
            # After processing day r:
            #
            # new_nn = max(old_nn, old_nh + p)
            # new_nh = max(old_nh, old_nn - p)
            #
            # new_hn = max(old_hn, old_hh + p)
            # new_hh = max(old_hh, old_hn - p)
            #
            # Then we also compare with the "start new segment at r" candidates.
            # -------------------------------------------------------------
            old_nn: int = mid_nn_best
            old_nh: int = mid_nh_best
            old_hn: int = mid_hn_best
            old_hh: int = mid_hh_best

            ext_nn: int = max(old_nn, old_nh + p) if old_nn > neg_inf // 2 or old_nh > neg_inf // 2 else neg_inf
            ext_nh: int = max(old_nh, old_nn - p) if old_nn > neg_inf // 2 or old_nh > neg_inf // 2 else neg_inf
            ext_hn: int = max(old_hn, old_hh + p) if old_hn > neg_inf // 2 or old_hh > neg_inf // 2 else neg_inf
            ext_hh: int = max(old_hh, old_hn - p) if old_hn > neg_inf // 2 or old_hh > neg_inf // 2 else neg_inf

            mid_nn_best = max(cand_nn, ext_nn)
            mid_nh_best = max(cand_nh, ext_nh)
            mid_hn_best = max(cand_hn, ext_hn)
            mid_hh_best = max(cand_hh, ext_hh)

            # -------------------------------------------------------------
            # Step 3.3: Close the promotion window at r and attach the suffix.
            #
            # If the segment ends not holding:
            #   next day starts free -> add suf_free[r+1]
            #
            # If the segment ends holding:
            #   next day starts holding -> add suf_hold[r+1]
            #
            # We test all four possibilities:
            #   start no-stock -> end no-stock
            #   start no-stock -> end holding
            #   start holding  -> end no-stock
            #   start holding  -> end holding
            #
            # This fully covers all ways the chosen promotion window can interact
            # with trades that begin before it or continue after it.
            # -------------------------------------------------------------
            answer = max(answer, mid_nn_best + suf_free[r + 1])
            answer = max(answer, mid_hn_best + suf_free[r + 1])

            if suf_hold[r + 1] > neg_inf // 2:
                answer = max(answer, mid_nh_best + suf_hold[r + 1])
                answer = max(answer, mid_hh_best + suf_hold[r + 1])

        return max(answer, 0)

    def maxProfit(self, prices: List[int]) -> int:
        """
        Public wrapper matching the common interview-style method name.

        Args:
            prices: List of daily prices.

        Returns:
            Maximum achievable total profit with exactly one chosen promotion segment.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        return self.maxProfitOneCooldownSegment(prices)


if __name__ == "__main__":
    solution = Solution()

    sample_1: List[int] = [1, 3, 2, 5, 4, 7]
    sample_2: List[int] = [5, 4, 3, 2, 1]

    result_1: int = solution.maxProfit(sample_1)
    result_2: int = solution.maxProfit(sample_2)

    print("Sample 1:", sample_1)
    print("Maximum profit:", result_1)
    print("Expected:", 8)
    print()

    print("Sample 2:", sample_2)
    print("Maximum profit:", result_2)
    print("Expected:", 0)