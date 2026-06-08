```python
"""
Title: Minimum Cost to Collect All Coupons
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
A store is running a promotion where customers can collect coupons. There are `n` distinct
coupon types numbered from `0` to `n-1`. You are given an integer array `cost` where
`cost[i]` represents the price to directly purchase coupon `i`. Additionally, you are given
a 2D array `bundles` where each `bundles[j] = [a, b, discount]` means: if you already own
coupon `a`, you can acquire coupon `b` at a reduced cost of `discount` (instead of `cost[b]`).
Each bundle can only be used once, and you can only use a bundle if you already own the
prerequisite coupon at the time of purchase.

Return the minimum total cost to collect all `n` coupons.

Constraints:
- 1 <= n <= 20
- 1 <= cost[i] <= 1000
- 0 <= bundles.length <= n * (n - 1)
- bundles[j].length == 3
- 0 <= a, b <= n - 1, a != b
- 1 <= discount <= cost[b]
- There are no duplicate (a, b) pairs in bundles.
"""

from typing import List, Dict
import sys


class Solution:
    def minCostToCollectAllCoupons(
        self, cost: List[int], bundles: List[List[int]]
    ) -> int:
        """
        Find the minimum total cost to collect all n coupons using direct purchases
        and bundle discounts.

        The key insight is that n <= 20, which allows us to use bitmask DP.
        Each state in our DP represents a subset of coupons we currently own.
        We want to find the minimum cost to reach the state where all coupons are owned.

        Args:
            cost: List of integers where cost[i] is the direct purchase price of coupon i.
            bundles: List of [a, b, discount] meaning if you own coupon a, you can buy
                     coupon b for `discount` instead of cost[b].

        Returns:
            The minimum total cost to collect all n coupons.

        Time Complexity: O(2^n * n) - We have 2^n states, and for each state we consider
                         adding each of the n coupons.
        Space Complexity: O(2^n) - We store the minimum cost for each subset of coupons.
        """

        n = len(cost)

        # -----------------------------------------------------------------------
        # Step 1: Build a discount lookup structure.
        # For each coupon b, we want to know: given that we own a certain set of
        # coupons, what is the cheapest way to buy coupon b?
        #
        # bundle_discount[b][a] = discount means: if we own coupon a, we can buy
        # coupon b for `discount`.
        #
        # We use a dictionary of dictionaries for O(1) lookup.
        # -----------------------------------------------------------------------
        # bundle_discount[b] = dict mapping prerequisite coupon a -> discounted price
        bundle_discount: Dict[int, Dict[int, int]] = {}
        for a, b, discount in bundles:
            if b not in bundle_discount:
                bundle_discount[b] = {}
            # Store the discounted price for buying coupon b when we own coupon a
            bundle_discount[b][a] = discount

        # -----------------------------------------------------------------------
        # Step 2: Set up bitmask DP.
        # We represent the set of owned coupons as a bitmask (integer).
        # If bit i is set in the mask, it means we own coupon i.
        #
        # dp[mask] = minimum cost to own exactly the coupons represented by `mask`.
        #
        # We initialize all states to infinity (unreachable), except dp[0] = 0
        # (owning no coupons costs nothing).
        # -----------------------------------------------------------------------
        total_states = 1 << n  # 2^n possible subsets
        dp = [sys.maxsize] * total_states
        dp[0] = 0  # Base case: owning no coupons costs 0

        # -----------------------------------------------------------------------
        # Step 3: Iterate over all possible states (subsets of owned coupons).
        # For each state, we try to add one more coupon that we don't yet own.
        #
        # Why iterate in order from 0 to 2^n - 1?
        # Because when we compute dp[mask | (1 << next_coupon)], we need dp[mask]
        # to already be computed. Since mask | (1 << next_coupon) > mask always,
        # iterating in increasing order ensures correct ordering.
        # -----------------------------------------------------------------------
        for mask in range(total_states):
            # Skip states that are unreachable
            if dp[mask] == sys.maxsize:
                continue

            # -------------------------------------------------------------------
            # Step 4: For the current state `mask`, try acquiring each coupon
            # that we don't yet own.
            # -------------------------------------------------------------------
            for next_coupon in range(n):
                # Check if we already own this coupon (bit is set in mask)
                if mask & (1 << next_coupon):
                    continue  # Already own it, skip

                # ---------------------------------------------------------------
                # Step 5: Determine the cheapest way to acquire `next_coupon`
                # given the coupons we currently own (represented by `mask`).
                #
                # Option 1: Buy at full price (always available)
                # Option 2: Use a bundle discount if we own the prerequisite coupon
                # ---------------------------------------------------------------
                purchase_cost = cost[next_coupon]  # Default: full price

                # Check if there are any bundle discounts available for next_coupon
                if next_coupon in bundle_discount:
                    for prereq_coupon, discounted_price in bundle_discount[next_coupon].items():
                        # Check if we own the prerequisite coupon
                        # (bit prereq_coupon is set in mask)
                        if mask & (1 << prereq_coupon):
                            # We can use this bundle! Take the minimum discount available.
                            purchase_cost = min(purchase_cost, discounted_price)

                # ---------------------------------------------------------------
                # Step 6: Update the DP state for owning next_coupon in addition
                # to all coupons in `mask`.
                #
                # new_mask = mask with bit next_coupon set
                # new_cost = current cost to reach mask + cost to buy next_coupon
                # ---------------------------------------------------------------
                new_mask = mask | (1 << next_coupon)
                new_cost = dp[mask] + purchase_cost

                # Update if we found a cheaper way to reach new_mask
                if new_cost < dp[new_mask]:
                    dp[new_mask] = new_cost

        # -----------------------------------------------------------------------
        # Step 7: The answer is dp[all_owned], where all_owned is the bitmask
        # with all n bits set (meaning we own all n coupons).
        # -----------------------------------------------------------------------
        all_owned = total_states - 1  # (1 << n) - 1 = bitmask with all n bits set
        return dp[all_owned]


# -------------------------------------------------------------------------------
# Main block: Test the solution with the provided examples and verify correctness.
# -------------------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # ---------------------------------------------------------------------------
    # Example 1:
    # cost = [5, 3, 4], bundles = [[0, 1, 1], [0, 2, 2]]
    # Expected Output: 8
    #
    # Trace:
    # - Start with mask=0 (own nothing), dp[0]=0
    # - Buy coupon 0 at full price 5 -> mask=001, dp[1]=5
    # - Buy coupon 1 at full price 3 -> mask=010, dp[2]=3
    # - Buy coupon 2 at full price 4 -> mask=100, dp[4]=4
    # - From mask=001 (own coupon 0):
    #   - Buy coupon 1: bundle [0,1,1] applies (own coupon 0), cost=1 -> dp[3]=5+1=6
    #   - Buy coupon 2: bundle [0,2,2] applies (own coupon 0), cost=2 -> dp[5]=5+2=7
    # - From mask=010 (own coupon 1):
    #   - Buy coupon 0: full price 5 -> dp[3]=min(6, 3+5)=6
    #   - Buy coupon 2: full price 4 -> dp[6]=3+4=7
    # - From mask=100 (own coupon 2):
    #   - Buy coupon 0: full price 5 -> dp[5]=min(7, 4+5)=7
    #   - Buy coupon 1: full price 3 -> dp[6]=min(7, 4+3)=7
    # - From mask=011 (own coupons 0,1), dp[3]=6:
    #   - Buy coupon 2: bundle [0,2,2] applies, cost=2 -> dp[7]=6+2=8
    # - From mask=101 (own coupons 0,2), dp[5]=7:
    #   - Buy coupon 1: bundle [0,1,1] applies, cost=1 -> dp[7]=min(8, 7+1)=8
    # - From mask=110 (own coupons 1,2), dp[6]=7:
    #   - Buy coupon 0: full price 5 -> dp[7]=min(8, 7+5)=8
    # - Final: dp[7]=8 ✓
    # ---------------------------------------------------------------------------
    cost1 = [5, 3, 4]
    bundles1 = [[0, 1, 1], [0, 2, 2]]
    result1 = solution.minCostToCollectAllCoupons(cost1, bundles1)
    print(f"Example 1:")
    print(f"  Input: cost={cost1}, bundles={bundles1}")
    print(f"  Output: {result1}")
    print(f"  Expected: 8")
    print(f"  {'PASS' if result1 == 8 else 'FAIL'}")
    print()

    # ---------------------------------------------------------------------------
    # Example 2:
    # cost = [10, 6, 7, 4], bundles = [[0, 1, 2], [1, 2, 3], [2, 3, 1]]
    # Expected Output: 16
    #
    # Trace (optimal path):
    # - Buy coupon 0 at full price 10 -> own {0}, cost=10
    # - Use bundle [0,1,2]: own coupon 0, buy coupon 1 for 2 -> own {0,1}, cost=12
    # - Use bundle [1,2,3]: own coupon 1, buy coupon 2 for 3 -> own {0,1,2}, cost=15
    # - Use bundle [2,3,1]: own coupon 2, buy coupon 3 for 1 -> own {0,1,2,3}, cost=16
    # Total = 16 ✓
    # ---------------------------------------------------------------------------
    cost2 = [10, 6, 7, 4]
    bundles2 = [[0, 1, 2], [1, 2, 3], [2, 3, 1]]
    result2 = solution.minCostToCollectAllCoupons(cost2, bundles2)
    print(f"Example 2:")
    print(f"  Input: cost={cost2}, bundles={bundles2}")
    print(f"  Output: {result2}")
    print(f"  Expected: 16")
    print(f"  {'PASS' if result2 == 16 else 'FAIL'}")
    print()

    # ---------------------------------------------------------------------------
    # Additional Edge Case: Single coupon, no bundles
    # cost = [7], bundles = []
    # Expected Output: 7 (must buy the only coupon at full price)
    # ---------------------------------------------------------------------------
    cost3 = [7]
    bundles3 = []
    result3 = solution.minCostToCollectAllCoupons(cost3, bundles3)
    print(f"Edge Case (single coupon):")
    print(f"  Input: cost={cost3}, bundles={bundles3}")
    print(f"  Output: {result3}")
    print(f"  Expected: 7")
    print(f"  {'PASS' if result3 == 7 else 'FAIL'}")
    print()

    # ---------------------------------------------------------------------------
    # Additional Edge Case: No bundles, must buy everything at full price
    # cost = [3, 5, 2], bundles = []
    # Expected Output: 10
    # ---------------------------------------------------------------------------
    cost4 = [3, 5, 2]
    bundles4 = []
    result4 = solution.minCostToCollectAllCoupons(cost4, bundles4)
    print(f"Edge Case (no bundles):")
    print(f"  Input: cost={cost4}, bundles={bundles4}")
    print(f"  Output: {result4}")
    print(f"  Expected: 10")
    print(f"  {'PASS' if result4 == 10 else 'FAIL'}")
```