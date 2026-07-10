"""
Title: Maximum Revenue from Ads with Cooling Gap

Problem Description:
A video platform has a list of ad opportunities along a timeline. You are given an
integer array revenue where revenue[i] is the amount earned if you place an ad in
slot i. However, to avoid showing ads too close together, after choosing any slot i,
you must skip the next gap slots. In other words, if you place an ad at slot i, the
next ad can only be placed at slot j where j > i + gap.

Your task is to compute the maximum total revenue that can be earned by selecting a
subset of slots that satisfies this cooling-gap rule.

Return the maximum possible revenue.

This is a one-dimensional optimization problem. A slot may be skipped even if it has
positive revenue, and some revenues may be 0. You should design an algorithm
efficient enough for large inputs.

Constraints:
- 1 <= revenue.length <= 200000
- 0 <= revenue[i] <= 1000000000
- 0 <= gap < revenue.length

Examples:
1) revenue = [5, 1, 2, 10, 6, 2], gap = 1
   Output: 17
   Explanation: Choose slots 0, 3, and 5 for total revenue 17.

2) revenue = [4, 7, 3, 9, 2, 8], gap = 2
   Correct Output: 15
   Explanation: Choose slots 1 and 5 for total revenue 15.
   Note: The prompt text contains a contradictory intermediate discussion, but the
   mathematically correct answer is 15.
"""

from typing import List


class Solution:
    def max_revenue(self, revenue: List[int], gap: int) -> int:
        """
        Compute the maximum total revenue under the cooling-gap rule.

        We use dynamic programming where:
        - dp[i] represents the maximum revenue obtainable using slots from index 0
          through index i inclusive.
        - For each slot i, we have two choices:
          1. Skip slot i, so the answer remains dp[i - 1].
          2. Take slot i, so we add revenue[i] plus the best answer from the last
             compatible slot, which is index i - gap - 1.

        Args:
            revenue: List of non-negative revenues for each ad slot.
            gap: Number of slots that must be skipped after choosing a slot.

        Returns:
            The maximum possible total revenue.

        Time complexity:
            O(n), where n is the number of slots.

        Space complexity:
            O(n), for the dynamic programming array.
        """
        n: int = len(revenue)

        # Edge case:
        # If there is only one slot, the best we can do is either take it or skip it.
        # Since revenues are non-negative, taking it is always optimal.
        if n == 1:
            return revenue[0]

        # Create a DP array where:
        # dp[i] = best possible revenue considering slots [0..i].
        #
        # We store one value per index because each state depends on:
        # - the previous state dp[i - 1]
        # - a potentially earlier compatible state dp[i - gap - 1]
        #
        # This makes the transition very direct and easy to understand.
        dp: List[int] = [0] * n

        # Base case for the first slot:
        # With only slot 0 available, the best revenue is simply revenue[0].
        dp[0] = revenue[0]

        # Process each slot from left to right.
        # At each position, decide whether taking this slot improves the best answer.
        for i in range(1, n):
            # Option 1: Skip the current slot i.
            # Then the best answer is exactly the same as for slots [0..i-1].
            skip_current: int = dp[i - 1]

            # Option 2: Take the current slot i.
            # If we take slot i, the next previously chosen slot must be at or before
            # index i - gap - 1.
            #
            # So we add:
            # - revenue[i] for taking this slot
            # - dp[i - gap - 1] if that index exists
            #
            # If i - gap - 1 < 0, then there is no earlier compatible slot, so the
            # total for taking slot i is just revenue[i].
            previous_compatible_index: int = i - gap - 1
            take_current: int = revenue[i]

            if previous_compatible_index >= 0:
                take_current += dp[previous_compatible_index]

            # The best answer for dp[i] is the better of:
            # - skipping slot i
            # - taking slot i
            dp[i] = max(skip_current, take_current)

        # The final answer is the best revenue considering all slots [0..n-1].
        return dp[-1]

    def maxRevenue(self, revenue: List[int], gap: int) -> int:
        """
        Wrapper method using camelCase naming for compatibility with common interview
        platform conventions.

        Args:
            revenue: List of non-negative revenues for each ad slot.
            gap: Number of slots that must be skipped after choosing a slot.

        Returns:
            The maximum possible total revenue.

        Time complexity:
            O(n), where n is the number of slots.

        Space complexity:
            O(n), for the dynamic programming array.
        """
        return self.max_revenue(revenue, gap)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    revenue1: List[int] = [5, 1, 2, 10, 6, 2]
    gap1: int = 1
    result1: int = solution.max_revenue(revenue1, gap1)
    print("Example 1:")
    print(f"Revenue: {revenue1}")
    print(f"Gap: {gap1}")
    print(f"Maximum Revenue: {result1}")
    print("Expected: 17")
    print()

    # Example 2
    # Important note:
    # The problem statement contains contradictory reasoning, but the correct optimal
    # answer for this input is 15 by choosing slots 1 and 5.
    revenue2: List[int] = [4, 7, 3, 9, 2, 8]
    gap2: int = 2
    result2: int = solution.max_revenue(revenue2, gap2)
    print("Example 2:")
    print(f"Revenue: {revenue2}")
    print(f"Gap: {gap2}")
    print(f"Maximum Revenue: {result2}")
    print("Expected: 15")
    print()

    # Additional quick sanity checks
    revenue3: List[int] = [10]
    gap3: int = 0
    result3: int = solution.max_revenue(revenue3, gap3)
    print("Additional Test 1:")
    print(f"Revenue: {revenue3}")
    print(f"Gap: {gap3}")
    print(f"Maximum Revenue: {result3}")
    print("Expected: 10")
    print()

    revenue4: List[int] = [1, 2, 3, 4, 5]
    gap4: int = 0
    result4: int = solution.max_revenue(revenue4, gap4)
    print("Additional Test 2:")
    print(f"Revenue: {revenue4}")
    print(f"Gap: {gap4}")
    print(f"Maximum Revenue: {result4}")
    print("Expected: 15")