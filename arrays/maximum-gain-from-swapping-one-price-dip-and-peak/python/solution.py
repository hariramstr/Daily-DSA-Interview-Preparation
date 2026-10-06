"""
Title: Maximum Gain from Swapping One Price Dip and Peak

Problem Description:
You are given an integer array prices where prices[i] represents the price of an asset on day i.
You may perform at most one swap of two different elements in the array. After the swap, choose
a single buy day b and a later sell day s such that b < s. Your profit is prices[s] - prices[b].

Return the maximum profit you can achieve.

A swap is optional, and the buy/sell operation must happen after the array has been modified.
The swapped values remain in their new positions when selecting the buy and sell days.
If no profitable transaction is possible, return 0.

This problem is about reasoning over array positions, not sorting the array freely. Because only
one swap is allowed, a good solution must carefully evaluate how moving one low price earlier or
one high price later can improve the best possible transaction.

Constraints:
- 2 <= prices.length <= 2 * 10^5
- 0 <= prices[i] <= 10^9
- You may swap at most one pair of indices i and j where i != j

Examples:
1) prices = [8, 3, 6, 1, 9]
   Output: 8

2) prices = [10, 7, 4, 6, 2]
   Output: 5
"""

from typing import List


class SegmentTreeMax:
    """Segment tree for range maximum queries."""

    def __init__(self, values: List[int]) -> None:
        """
        Build a segment tree storing maximum values.

        Args:
            values: Source array.

        Returns:
            None

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        self.n: int = len(values)
        size: int = 1
        while size < self.n:
            size <<= 1
        self.size: int = size
        self.data: List[int] = [0] * (2 * size)

        for i, value in enumerate(values):
            self.data[size + i] = value
        for i in range(size - 1, 0, -1):
            self.data[i] = max(self.data[i << 1], self.data[i << 1 | 1])

    def query(self, left: int, right: int) -> int:
        """
        Return the maximum value in the inclusive range [left, right].

        Args:
            left: Left index.
            right: Right index.

        Returns:
            Maximum value in the range. Returns 0 if the range is empty.

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        if left > right:
            return 0

        left += self.size
        right += self.size
        result: int = 0

        while left <= right:
            if left & 1:
                result = max(result, self.data[left])
                left += 1
            if not (right & 1):
                result = max(result, self.data[right])
                right -= 1
            left >>= 1
            right >>= 1

        return result


class SegmentTreeMin:
    """Segment tree for range minimum queries."""

    def __init__(self, values: List[int]) -> None:
        """
        Build a segment tree storing minimum values.

        Args:
            values: Source array.

        Returns:
            None

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        self.n: int = len(values)
        size: int = 1
        while size < self.n:
            size <<= 1
        self.size: int = size
        self.inf: int = 10**30
        self.data: List[int] = [self.inf] * (2 * size)

        for i, value in enumerate(values):
            self.data[size + i] = value
        for i in range(size - 1, 0, -1):
            self.data[i] = min(self.data[i << 1], self.data[i << 1 | 1])

    def query(self, left: int, right: int) -> int:
        """
        Return the minimum value in the inclusive range [left, right].

        Args:
            left: Left index.
            right: Right index.

        Returns:
            Minimum value in the range. Returns +infinity-like value if empty.

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        if left > right:
            return self.inf

        left += self.size
        right += self.size
        result: int = self.inf

        while left <= right:
            if left & 1:
                result = min(result, self.data[left])
                left += 1
            if not (right & 1):
                result = min(result, self.data[right])
                right -= 1
            left >>= 1
            right >>= 1

        return result


class Solution:
    def maxProfitAfterOneSwap(self, prices: List[int]) -> int:
        """
        Compute the maximum possible buy/sell profit after performing at most one swap.

        The key idea is to classify every useful swap into one of two powerful effects:

        1) Move a small value earlier:
           If we swap some index i with a later index j, then the value prices[j] can become
           the buy price at day i. After that, we can sell on some day after i. The best sell
           after day i is simply the suffix maximum after i.

        2) Move a large value later:
           If we swap some earlier index i with a later index j, then the value prices[i] can
           become the sell price at day j. Before day j, the best buy is simply the prefix
           minimum before j.

        A very important observation is that any optimal swap can be evaluated by one of these
        two views. We do not need to simulate all O(n^2) swaps.

        We precompute:
        - prefix minima
        - suffix maxima
        - best profit ending at or before each position
        - best profit starting at or after each position

        Then:
        - For "move low earlier", for each buy position i we need the minimum value available
          in the suffix [i+1 .. n-1], and the best sell opportunity after i.
        - For "move high later", for each sell position j we need the maximum value available
          in the prefix [0 .. j-1], and the best buy opportunity before j.

        We use segment trees to answer:
        - max over a suffix of precomputed sell opportunities
        - min over a suffix of prices
        - max over a prefix of prices
        - min over a prefix of precomputed buy opportunities

        Args:
            prices: Array of daily prices.

        Returns:
            Maximum achievable profit, or 0 if no profitable transaction exists.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(prices)

        # ------------------------------------------------------------------
        # Step 1: Compute the ordinary "no swap" best profit.
        #
        # This is the classic stock problem:
        # - keep track of the smallest price seen so far
        # - at each day, see how much profit we would get by selling today
        #
        # We also store prefix_best_profit[i]:
        #   the best profit achievable using only days in [0 .. i]
        #
        # This array is useful later because when we move a high value to some
        # later day j, the buy day must still be before j, so we need to know
        # the best "buy-side" opportunities in prefixes.
        # ------------------------------------------------------------------
        prefix_min_price: List[int] = [0] * n
        prefix_best_profit: List[int] = [0] * n

        current_min: int = prices[0]
        prefix_min_price[0] = prices[0]
        prefix_best_profit[0] = 0

        for i in range(1, n):
            current_min = min(current_min, prices[i])
            prefix_min_price[i] = current_min
            prefix_best_profit[i] = max(prefix_best_profit[i - 1], prices[i] - current_min)

        answer: int = prefix_best_profit[n - 1]

        # ------------------------------------------------------------------
        # Step 2: Compute suffix maximum prices and suffix best profits.
        #
        # suffix_max_price[i] = maximum price in [i .. n-1]
        # suffix_best_profit[i] = best profit achievable using only days in [i .. n-1]
        #
        # This is the mirror image of the classic stock problem.
        #
        # It is useful when we move a low value to an earlier day i:
        # after buying at day i, we want the best possible sell day after i.
        # ------------------------------------------------------------------
        suffix_max_price: List[int] = [0] * n
        suffix_best_profit: List[int] = [0] * n

        current_max: int = prices[n - 1]
        suffix_max_price[n - 1] = prices[n - 1]
        suffix_best_profit[n - 1] = 0

        for i in range(n - 2, -1, -1):
            current_max = max(current_max, prices[i])
            suffix_max_price[i] = current_max
            suffix_best_profit[i] = max(suffix_best_profit[i + 1], current_max - prices[i])

        # ------------------------------------------------------------------
        # Step 3: Prepare helper arrays for the two swap patterns.
        #
        # Pattern A: Move a low value earlier.
        #
        # Suppose we decide the final buy day is i after the swap.
        # We want to bring some later value prices[j] (j > i) to position i.
        # The best such value is simply the minimum price in the suffix [i+1 .. n-1].
        #
        # Once that low value is placed at i, the best sell after i is the maximum
        # price in [i+1 .. n-1], so the profit candidate is:
        #
        #   suffix_max_price[i+1] - min(prices[j] for j > i)
        #
        # But there is a subtle issue:
        # if the chosen sell day is exactly j, then after the swap that day contains
        # prices[i], not the original prices[j].
        #
        # To avoid this pitfall cleanly, we use:
        #   best_sell_after[i] = max over t in [i+1 .. n-1] of suffix_max_price[t]
        #
        # Since suffix_max_price[t] is the best sell value available from t onward,
        # and our moved low can come from any j >= t, this captures a valid sell
        # opportunity after the moved buy.
        #
        # In practice, suffix_max_price itself is already non-increasing as i moves
        # left, but we keep the logic explicit and robust.
        # ------------------------------------------------------------------
        min_suffix_excluding_self: List[int] = [10**30] * n
        for i in range(n - 2, -1, -1):
            if i == n - 2:
                min_suffix_excluding_self[i] = prices[i + 1]
            else:
                min_suffix_excluding_self[i] = min(prices[i + 1], min_suffix_excluding_self[i + 1])

        # For each possible buy position i, the best sell value after i is simply
        # suffix_max_price[i+1]. We store it in an aligned array for segment queries.
        sell_value_after: List[int] = [0] * n
        for i in range(n - 1):
            sell_value_after[i] = suffix_max_price[i + 1]
        sell_value_after[n - 1] = 0

        # ------------------------------------------------------------------
        # Step 4: Prepare helper arrays for the second swap pattern.
        #
        # Pattern B: Move a high value later.
        #
        # Suppose we decide the final sell day is j after the swap.
        # We want to bring some earlier value prices[i] (i < j) to position j.
        # The best such value is simply the maximum price in the prefix [0 .. j-1].
        #
        # Before day j, the best buy is the minimum price in [0 .. j-1], so the
        # profit candidate is:
        #
        #   max(prices[i] for i < j) - prefix_min_price[j-1]
        #
        # Again, we must be careful that if the buy day were exactly i, then after
        # the swap that day contains prices[j], not prices[i].
        #
        # The clean safe quantity is:
        #   best_buy_before[j] = minimum price available in [0 .. j-1]
        #
        # and the moved sell value is the maximum prefix value before j.
        # ------------------------------------------------------------------
        max_prefix_excluding_self: List[int] = [0] * n
        for j in range(1, n):
            if j == 1:
                max_prefix_excluding_self[j] = prices[0]
            else:
                max_prefix_excluding_self[j] = max(max_prefix_excluding_self[j - 1], prices[j - 1])

        buy_value_before: List[int] = [10**30] * n
        buy_value_before[0] = 10**30
        for j in range(1, n):
            buy_value_before[j] = prefix_min_price[j - 1]

        # ------------------------------------------------------------------
        # Step 5: Evaluate "move a low value earlier".
        #
        # For each position i as the final buy day:
        # - choose the smallest value from some later day
        # - sell at the best later value
        #
        # Candidate:
        #   suffix_max_price[i+1] - min_suffix_excluding_self[i]
        #
        # This is valid because:
        # - the buy day is i
        # - the chosen low comes from a later day
        # - the sell day is after i
        #
        # If the best sell day happens to be the same day as the moved low, the
        # profit becomes non-positive and will not beat the answer anyway.
        # ------------------------------------------------------------------
        for i in range(n - 1):
            candidate: int = sell_value_after[i] - min_suffix_excluding_self[i]
            if candidate > answer:
                answer = candidate

        # ------------------------------------------------------------------
        # Step 6: Evaluate "move a high value later".
        #
        # For each position j as the final sell day:
        # - choose the largest value from some earlier day
        # - buy at the best earlier minimum
        #
        # Candidate:
        #   max_prefix_excluding_self[j] - prefix_min_price[j-1]
        #
        # This is valid because:
        # - the sell day is j
        # - the chosen high comes from an earlier day
        # - the buy day is before j
        # ------------------------------------------------------------------
        for j in range(1, n):
            candidate = max_prefix_excluding_self[j] - buy_value_before[j]
            if candidate > answer:
                answer = candidate

        # ------------------------------------------------------------------
        # Step 7: There is one more important mixed scenario:
        #
        # A swap can improve both sides at once:
        # - the earlier swapped position becomes a better buy
        # - the later swapped position becomes a better sell
        #
        # For a chosen pair i < j, the direct profit using those exact swapped
        # endpoints becomes:
        #
        #   prices[i] - prices[j]
        #
        # because after swapping:
        # - day i contains prices[j]
        # - day j contains prices[i]
        #
        # and buying at i, selling at j gives prices[i] - prices[j].
        #
        # This can be larger than the previous two patterns when the same swap
        # simultaneously creates the best buy and best sell.
        #
        # Therefore we must also consider:
        #   max over i < j of prices[i] - prices[j]
        #
        # This is equivalent to:
        #   maximum prefix value before j minus prices[j]
        # ------------------------------------------------------------------
        best_prefix_value: int = prices[0]
        for j in range(1, n):
            candidate = best_prefix_value - prices[j]
            if candidate > answer:
                answer = candidate
            if prices[j] > best_prefix_value:
                best_prefix_value = prices[j]

        # Profit cannot be negative because we are allowed to do nothing.
        return max(0, answer)


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [8, 3, 6, 1, 9],
        [10, 7, 4, 6, 2],
        [5, 4, 3, 2, 1],
        [1, 2, 3, 4, 5],
        [9, 1, 8, 2, 7],
    ]

    for prices in sample_inputs:
        result = solution.maxProfitAfterOneSwap(prices)
        print(f"prices = {prices}")
        print(f"maximum profit after at most one swap = {result}")
        print("-" * 60)