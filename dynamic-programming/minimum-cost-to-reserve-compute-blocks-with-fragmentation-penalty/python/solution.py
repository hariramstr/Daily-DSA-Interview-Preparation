"""
Title: Minimum Cost to Reserve Compute Blocks with Fragmentation Penalty

Problem Description:
A cloud platform sells compute capacity for the next n hours. For hour i, you must reserve
exactly demand[i] units of compute. Instead of buying capacity hour by hour, you may purchase
reservation blocks. A block is defined by a contiguous interval of hours [l, r] and a fixed
capacity c, meaning that for every hour from l to r you receive exactly c units from that block.
Multiple blocks may overlap, and the total reserved capacity at each hour must equal the required
demand exactly.

The cost of one block [l, r] with capacity c is:
    setupCost + c * (r - l + 1) + fragmentationPenalty * max(0, c - min(demand[l..r]))

The first term is a fixed fee for creating a block, the second term is the usage cost, and the
third term penalizes over-provisioning relative to the minimum demand inside the covered interval.
Intuitively, a block that spans a low-demand hour cannot cheaply carry a high capacity.

Your task is to compute the minimum total cost needed to satisfy the full demand array exactly.

Constraints:
- 1 <= n <= 200
- 0 <= demand[i] <= 10^6
- 1 <= setupCost <= 10^6
- 0 <= fragmentationPenalty <= 10^6
- The answer fits in a 64-bit signed integer.
"""

from typing import List


class Solution:
    def minimum_cost(
        self,
        demand: List[int],
        setupCost: int,
        fragmentationPenalty: int,
    ) -> int:
        """
        Compute the minimum total cost to exactly satisfy the demand array.

        The key dynamic programming idea:
        For any interval [l, r], consider the bottom-most layer of capacity that is present
        across the entire interval. If the minimum demand inside [l, r] is m, then one valid
        way to build the solution is:
            1. Create one block [l, r] with capacity m
            2. Recursively solve the positive "extra demand" segments above that baseline

        More generally, if the demand at the boundaries is lower than the interval minimum after
        recursive splitting, the DP recurrence naturally handles all valid decompositions by
        splitting the interval around positions where the minimum occurs.

        We use interval DP:
            dp[l][r] = minimum cost to satisfy demand[l..r] exactly

        Recurrence:
            Let m = min(demand[l..r]).
            Option A:
                Build one block [l, r] with capacity m.
                Then split the remaining excess demand into maximal subsegments where demand > m.
                For each such subsegment [a, b], recursively solve after conceptually subtracting m.
            This "subtracting m" is implemented by another DP on transformed heights:
                f(l, r, base) = min cost to build demand[l..r] above already-covered base.
            To avoid a 3D DP, we instead solve on compressed distinct levels using a classic
            interval decomposition:
                g(l, r, base_min_covered)

        A simpler and efficient implementation is:
            solve(l, r, base) = min(
                sum of solving each single hour directly from base,
                cost of raising whole interval from base to current minimum + recurse on parts
            )

        This is analogous to the classic "strange printer / painting fence" interval DP, but with
        a custom cost for creating a horizontal layer block.

        Args:
            demand: Required capacity for each hour.
            setupCost: Fixed cost for creating any block.
            fragmentationPenalty: Penalty coefficient for carrying capacity above interval minimum.

        Returns:
            Minimum total cost as an integer.

        Time complexity:
            O(n^4) in the worst case with memoized interval recursion and scanning minima/splits.
            With n <= 200, this is acceptable in Python with careful implementation.

        Space complexity:
            O(n^3) in the worst case for memoization states over (l, r, base-index-like values),
            but this implementation stores only reachable states via memoization dictionaries.
        """
        n: int = len(demand)

        # ---------------------------------------------------------------------
        # We memoize states of the form:
        #   solve(l, r, base)
        #
        # Meaning:
        #   We still need to build exactly demand[i] - base units for every i in [l, r],
        #   assuming that all hours in [l, r] already have "base" units covered by blocks
        #   that span the entire interval [l, r].
        #
        # Why this state is enough:
        #   Any block we add inside [l, r] must contribute on a contiguous subinterval.
        #   The most important structural choice is whether to add a block spanning the whole
        #   current interval up to the current minimum remaining height.
        #
        # Important observation about cost:
        #   If we add one block [l, r] of extra capacity x on top of an already-covered base,
        #   then the block's actual capacity is x, and its cost depends on the original demand
        #   minimum over [l, r]. However, because x will never exceed the remaining minimum
        #   when used as the "whole interval baseline raise", the fragmentation penalty is zero
        #   for that baseline block. The penalty matters for arbitrary blocks, but the recursive
        #   decomposition below still captures optimality because any profitable block can be
        #   represented as a baseline raise on some subinterval before splitting further.
        #
        #   Therefore, when we raise the whole interval from base to m = min(demand[l..r]),
        #   the added block has capacity (m - base), length (r - l + 1), and zero penalty.
        #   Cost = setupCost + (m - base) * length
        #
        #   Then we recursively solve the subsegments where demand > m.
        #
        # Alternative option:
        #   Build each hour separately from the current base. This gives a safe upper bound and
        #   ensures correctness.
        # ---------------------------------------------------------------------

        memo: dict[tuple[int, int, int], int] = {}

        def solve(l: int, r: int, base: int) -> int:
            """
            Recursively compute the minimum cost to satisfy demand[l..r], given that every hour
            in this interval already has `base` units covered.

            Args:
                l: Left index of interval.
                r: Right index of interval.
                base: Already-covered capacity across the whole interval.

            Returns:
                Minimum additional cost for this interval.

            Time complexity:
                In the worst case, each state scans the interval to find the minimum and split
                points, so O(length). Across memoized states this leads to O(n^4) worst-case.

            Space complexity:
                O(number of memoized states).
            """
            if l > r:
                return 0

            key = (l, r, base)
            if key in memo:
                return memo[key]

            # -------------------------------------------------------------
            # Option 1: Build every hour independently from the current base.
            #
            # For a single hour i, the remaining capacity needed is demand[i] - base.
            # If positive, we can satisfy it with one single-hour block:
            #   cost = setupCost + (demand[i] - base)
            # because length = 1 and fragmentation penalty is always zero on a single hour
            # (its minimum demand equals demand[i], so c - min = 0 when c <= demand[i]).
            #
            # This option is always valid and gives a straightforward upper bound.
            # -------------------------------------------------------------
            direct_cost: int = 0
            for i in range(l, r + 1):
                if demand[i] > base:
                    direct_cost += setupCost + (demand[i] - base)

            best: int = direct_cost

            # -------------------------------------------------------------
            # Option 2: Raise the whole interval from `base` up to the minimum
            # demand inside [l, r].
            #
            # Let m = min(demand[l..r]).
            # If m > base, then we can create one block [l, r] with capacity (m - base)
            # on top of the already-covered base.
            #
            # Cost of this baseline block:
            #   setupCost + (m - base) * (r - l + 1)
            #
            # Fragmentation penalty is zero here because this block's capacity does not exceed
            # the interval minimum remaining demand.
            #
            # After adding this baseline, only positions with demand > m still need extra
            # capacity. Those positions form one or more disjoint subsegments, each solved
            # recursively with new base = m.
            # -------------------------------------------------------------
            interval_min: int = min(demand[l:r + 1])

            if interval_min > base:
                whole_block_cost: int = setupCost + (interval_min - base) * (r - l + 1)

                extra_cost: int = 0
                i = l
                while i <= r:
                    if demand[i] == interval_min:
                        i += 1
                        continue

                    start: int = i
                    while i <= r and demand[i] > interval_min:
                        i += 1
                    end: int = i - 1

                    extra_cost += solve(start, end, interval_min)

                best = min(best, whole_block_cost + extra_cost)

            memo[key] = best
            return best

        return solve(0, n - 1, 0)


if __name__ == "__main__":
    solution = Solution()

    demand1 = [3, 1, 3]
    setup1 = 2
    penalty1 = 4
    result1 = solution.minimum_cost(demand1, setup1, penalty1)
    print(result1)  # Expected: 13

    demand2 = [2, 2, 2, 2]
    setup2 = 5
    penalty2 = 3
    result2 = solution.minimum_cost(demand2, setup2, penalty2)
    print(result2)  # Expected: 13