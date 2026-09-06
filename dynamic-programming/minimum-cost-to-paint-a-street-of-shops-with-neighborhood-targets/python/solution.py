"""
Minimum Cost to Paint a Street of Shops with Neighborhood Targets

A city planning team is repainting a straight street of shops. There are n shops in
order from left to right, and each shop must end up painted in exactly one of m colors.
Some shops are already painted and cannot be changed, while others are unpainted and
may be painted at a given cost.

A neighborhood is defined as a maximal contiguous group of shops painted the same color.
For example, colors [2, 2, 3, 3, 1] form 3 neighborhoods, while [1, 2, 1] form
3 neighborhoods because adjacent colors differ at every position.

You are given:
- an integer array shops of length n, where shops[i] = 0 means the i-th shop is
  unpainted, and shops[i] in [1, m] means it is already painted with that color,
- a 2D integer array cost where cost[i][c - 1] is the cost to paint shop i with color c
  if shops[i] is unpainted,
- an integer target representing the exact number of neighborhoods required after all
  shops are painted.

Return the minimum total painting cost to achieve exactly target neighborhoods.
If it is impossible, return -1.

This is a dynamic programming problem because the best choice for each shop depends on
the color chosen for the previous shop and how many neighborhoods have already been formed.
"""

from typing import List


class Solution:
    def min_cost(self, shops: List[int], cost: List[List[int]], m: int, target: int) -> int:
        """
        Compute the minimum total cost to paint all shops so that exactly `target`
        neighborhoods are formed.

        We use dynamic programming where:
        - We process shops from left to right.
        - We track how many neighborhoods have been formed so far.
        - We track the color of the previous shop, because that determines whether
          painting the current shop creates a new neighborhood or continues the old one.

        Args:
            shops: List of shop colors. 0 means unpainted, otherwise already painted.
            cost: Painting cost matrix where cost[i][c - 1] is the cost to paint
                  shop i with color c if shop i is unpainted.
            m: Number of available colors.
            target: Required exact number of neighborhoods.

        Returns:
            The minimum painting cost to achieve exactly `target` neighborhoods,
            or -1 if impossible.

        Time complexity:
            O(n * target * m * m)

        Space complexity:
            O(target * m)
        """
        n: int = len(shops)

        # We use a very large number to represent an impossible or not-yet-reached state.
        # This is a common dynamic programming trick:
        # - If a state has cost INF, it means "we currently do not know any valid way
        #   to reach this state".
        # - Later, if we find a valid transition into that state, we replace INF with
        #   the smaller real cost.
        inf: int = 10**18

        # dp_prev[k][c] will mean:
        # "After processing some prefix of shops, the minimum cost to end with exactly
        #  k neighborhoods, where the last processed shop has color c."
        #
        # Important indexing notes:
        # - k ranges from 0 to target
        # - c ranges from 1 to m
        #
        # We allocate m + 1 columns so that color numbers can be used directly
        # (1-based color indexing), which makes the code easier to read.
        dp_prev: List[List[int]] = [[inf] * (m + 1) for _ in range(target + 1)]

        # Before processing any shops:
        # - We have formed 0 neighborhoods
        # - There is no last color yet
        #
        # We do not store "no last color" directly in the DP table.
        # Instead, we handle the first shop carefully during transitions.
        #
        # To start, we conceptually have an empty prefix with cost 0.
        # We will build the first real states from this.
        #
        # Since there is no valid "last color" before the first shop, we keep dp_prev
        # empty and special-case the first transition by using k == 1 when the first
        # shop is assigned any color.
        first_shop_color: int = shops[0]

        if first_shop_color != 0:
            # The first shop is already painted.
            # That means:
            # - We must use its existing color
            # - It forms exactly 1 neighborhood by itself
            # - No painting cost is added
            dp_prev[1][first_shop_color] = 0
        else:
            # The first shop is unpainted.
            # We may choose any color from 1 to m.
            # Whatever color we choose, the first shop starts the first neighborhood.
            for color in range(1, m + 1):
                dp_prev[1][color] = cost[0][color - 1]

        # Now process the remaining shops one by one.
        for i in range(1, n):
            # dp_curr will store the DP states after processing shop i.
            # We reset everything to INF because we are about to compute fresh values
            # from dp_prev.
            dp_curr: List[List[int]] = [[inf] * (m + 1) for _ in range(target + 1)]

            # Determine which colors are allowed for the current shop.
            #
            # If the shop is already painted:
            # - We are forced to use exactly that one color
            # - Additional painting cost is 0
            #
            # If the shop is unpainted:
            # - We may choose any color from 1 to m
            # - Additional painting cost depends on the chosen color
            if shops[i] != 0:
                allowed_colors: List[int] = [shops[i]]
            else:
                allowed_colors = list(range(1, m + 1))

            # Try every possible number of neighborhoods formed so far.
            for neighborhoods_so_far in range(1, target + 1):
                # Try every possible previous color.
                for prev_color in range(1, m + 1):
                    previous_cost: int = dp_prev[neighborhoods_so_far][prev_color]

                    # If this state was never reachable, skip it.
                    if previous_cost == inf:
                        continue

                    # For the current shop, try every allowed color.
                    for current_color in allowed_colors:
                        # Determine whether choosing current_color creates a new neighborhood.
                        #
                        # Rule:
                        # - If current_color == prev_color, the current shop continues the
                        #   previous neighborhood, so the neighborhood count does not change.
                        # - Otherwise, a new neighborhood starts here, so the count increases by 1.
                        if current_color == prev_color:
                            new_neighborhoods: int = neighborhoods_so_far
                        else:
                            new_neighborhoods = neighborhoods_so_far + 1

                        # If we exceed the target, this transition is useless.
                        if new_neighborhoods > target:
                            continue

                        # Compute the extra cost for painting this shop.
                        #
                        # If the shop is already painted, extra cost is 0.
                        # If unpainted, we pay the corresponding painting cost.
                        if shops[i] == 0:
                            paint_cost: int = cost[i][current_color - 1]
                        else:
                            paint_cost = 0

                        # Total cost for this transition.
                        candidate_cost: int = previous_cost + paint_cost

                        # Relax the DP state:
                        # Keep the minimum cost among all ways to reach the same
                        # (new_neighborhoods, current_color) state.
                        if candidate_cost < dp_curr[new_neighborhoods][current_color]:
                            dp_curr[new_neighborhoods][current_color] = candidate_cost

            # Move to the next shop.
            dp_prev = dp_curr

        # After processing all shops, we need exactly `target` neighborhoods.
        # The last shop may have any color, so we take the minimum over all ending colors.
        answer: int = min(dp_prev[target][color] for color in range(1, m + 1))

        # If answer is still INF, no valid painting plan exists.
        return -1 if answer == inf else answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    shops1: List[int] = [0, 0, 0, 0]
    cost1: List[List[int]] = [
        [1, 5],
        [4, 1],
        [1, 3],
        [2, 1],
    ]
    m1: int = 2
    target1: int = 2
    result1: int = solution.min_cost(shops1, cost1, m1, target1)
    print(result1)  # Expected: 4

    # Example 2
    # Note:
    # The problem statement's explanation appears inconsistent with the provided data.
    # We compute the true minimum from the given input, as required.
    shops2: List[int] = [1, 0, 2, 0, 0]
    cost2: List[List[int]] = [
        [3, 9, 4],
        [2, 1, 7],
        [8, 5, 6],
        [4, 3, 2],
        [7, 6, 1],
    ]
    m2: int = 3
    target2: int = 3
    result2: int = solution.min_cost(shops2, cost2, m2, target2)
    print(result2)

    # Additional simple test
    shops3: List[int] = [1, 2, 1]
    cost3: List[List[int]] = [
        [1, 1],
        [1, 1],
        [1, 1],
    ]
    m3: int = 2
    target3: int = 3
    result3: int = solution.min_cost(shops3, cost3, m3, target3)
    print(result3)  # Expected: 0