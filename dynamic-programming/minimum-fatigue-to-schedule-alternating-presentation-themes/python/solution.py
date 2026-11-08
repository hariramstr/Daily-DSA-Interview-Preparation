"""
Title: Minimum Fatigue to Schedule Alternating Presentation Themes

Problem Description:
A conference organizer is preparing a sequence of n presentation slots. For each slot i,
there are exactly three candidate talks available: a Technical talk, a Product talk,
and a Research talk. Choosing candidate type t for slot i adds fatigue cost cost[i][t].

The organizer wants the final schedule to feel varied. A schedule is considered valid
if it satisfies both rules:
1. No two consecutive slots use the same theme.
2. For every three consecutive scheduled slots, the three themes cannot be all distinct.
   In other words, among any window of length 3, at least one theme must repeat.

Your task is to return the minimum total fatigue needed to fill all n slots with a valid
schedule. If no valid schedule exists, return -1.

This is not a greedy problem, because a locally cheap theme choice may force expensive
or impossible choices later. You must reason about the recent history of chosen themes
and compute the optimal total cost.

Input format:
- An integer n
- A 2D integer array cost of size n x 3
- Theme indices are fixed as 0 = Technical, 1 = Product, 2 = Research

Constraints:
- 1 <= n <= 200000
- cost.length == n
- cost[i].length == 3
- 1 <= cost[i][t] <= 10^9
- The answer may exceed 32-bit integer range

Examples:
1) n = 4, cost = [[3,2,7],[5,1,4],[2,6,3],[8,2,5]]
   Output: 8
   One valid optimal schedule is [0,1,0,1] with cost 3 + 1 + 2 + 2 = 8.

2) n = 3, cost = [[1,10,10],[10,1,10],[10,10,1]]
   Output: -1

Notes:
- For n = 1, any single theme is valid.
- For n = 2, any pair of different themes is valid.
- For n >= 3, the second rule heavily constrains transitions and suggests a DP state
  based on the last two chosen themes.
"""

from typing import List


class Solution:
    def minimum_fatigue(self, n: int, cost: List[List[int]]) -> int:
        """
        Compute the minimum total fatigue to build a valid schedule.

        A valid schedule must satisfy:
        1. Adjacent themes are different.
        2. In every length-3 window, the three themes are not all distinct.

        Args:
            n: Number of presentation slots.
            cost: cost[i][t] is the fatigue cost of choosing theme t for slot i.

        Returns:
            The minimum total fatigue if a valid schedule exists, otherwise -1.

        Time complexity:
            O(n), because there are only 3 themes and therefore only a constant
            number of DP states per position.

        Space complexity:
            O(1) auxiliary space, because we only keep DP values for the previous
            position rather than the full DP table.
        """
        # -----------------------------
        # Basic edge-case handling
        # -----------------------------
        # If there are no slots, the problem constraints say n >= 1, so this is
        # not expected. Still, returning 0 is a safe defensive choice.
        if n == 0:
            return 0

        # For a single slot, there are no adjacency or length-3 constraints to worry about.
        # We can simply choose the cheapest of the three themes.
        if n == 1:
            return min(cost[0])

        # We will use a very large number to represent "impossible".
        # Python integers do not overflow, so this is safe.
        inf: int = 10**30

        # ------------------------------------------------------------
        # DP idea:
        #
        # Since the constraints depend on:
        # - the previous theme (for "no equal adjacent")
        # - and the previous two themes (for the length-3 rule),
        # the natural DP state is:
        #
        #   dp[a][b] = minimum cost so far where the last two chosen themes are a, b
        #
        # Here:
        # - a is the theme used at slot i-1
        # - b is the theme used at slot i
        #
        # This state is enough because when we try to append a new theme c:
        # - we must have b != c  (adjacent rule)
        # - and the triple (a, b, c) must NOT be all distinct
        #
        # Since there are only 3 themes, there are only 3 * 3 = 9 possible pairs,
        # and in practice only pairs with a != b can be valid after length >= 2.
        # ------------------------------------------------------------

        # ------------------------------------------------------------
        # Initialize DP for the first two slots.
        #
        # For n >= 2:
        # Any pair of DIFFERENT themes is valid, because the length-3 rule does not
        # apply yet. So we initialize dp[a][b] for all a != b.
        # ------------------------------------------------------------
        dp: List[List[int]] = [[inf] * 3 for _ in range(3)]

        for first_theme in range(3):
            for second_theme in range(3):
                # Adjacent themes must differ.
                if first_theme == second_theme:
                    continue

                # Cost of choosing first_theme at slot 0 and second_theme at slot 1.
                dp[first_theme][second_theme] = (
                    cost[0][first_theme] + cost[1][second_theme]
                )

        # ------------------------------------------------------------
        # Process slots from index 2 to n-1.
        #
        # Transition:
        # Suppose current DP state is (a, b), meaning the last two themes are a, b.
        # We want to append theme c at the next slot.
        #
        # Validity conditions:
        # 1. b != c  (adjacent themes differ)
        # 2. (a, b, c) is NOT all distinct
        #
        # Because we already know a != b in any valid state, condition (2) plus (1)
        # forces c to equal a.
        #
        # Why?
        # - c cannot equal b because adjacent themes must differ.
        # - If c differs from both a and b, then a, b, c are all distinct, forbidden.
        # - Therefore the only possible valid choice is c == a.
        #
        # This means every valid schedule of length >= 3 must alternate:
        # x, y, x, y, x, y, ...
        #
        # We still implement the DP clearly so the reasoning is explicit and easy to follow.
        # ------------------------------------------------------------
        for i in range(2, n):
            next_dp: List[List[int]] = [[inf] * 3 for _ in range(3)]

            for a in range(3):
                for b in range(3):
                    current_cost = dp[a][b]

                    # Skip impossible states.
                    if current_cost >= inf:
                        continue

                    # The only valid next theme is c = a.
                    c = a

                    # We now verify the rules explicitly for clarity and correctness.

                    # Rule 1: adjacent themes must differ.
                    # Since the previous theme is b, we need b != c.
                    # Because valid states already have a != b, and c = a, this will hold.
                    if b == c:
                        continue

                    # Rule 2: among (a, b, c), not all three can be distinct.
                    # Since c = a, the triple definitely has a repetition.
                    # So this is valid.

                    # After appending c, the new last-two-themes state becomes (b, c).
                    new_cost = current_cost + cost[i][c]
                    if new_cost < next_dp[b][c]:
                        next_dp[b][c] = new_cost

            dp = next_dp

        # ------------------------------------------------------------
        # The answer is the minimum value among all valid ending states.
        #
        # For n = 2, this simply returns the cheapest valid pair.
        # For n >= 3, this returns the cheapest valid alternating schedule.
        # If all states are impossible, return -1.
        # ------------------------------------------------------------
        answer = inf
        for a in range(3):
            for b in range(3):
                if dp[a][b] < answer:
                    answer = dp[a][b]

        return -1 if answer >= inf else answer

    def minFatigue(self, n: int, cost: List[List[int]]) -> int:
        """
        Convenience wrapper matching an alternative method name.

        Args:
            n: Number of presentation slots.
            cost: cost[i][t] is the fatigue cost of choosing theme t for slot i.

        Returns:
            The minimum total fatigue if a valid schedule exists, otherwise -1.

        Time complexity:
            O(n)

        Space complexity:
            O(1) auxiliary space
        """
        return self.minimum_fatigue(n, cost)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 4
    cost1 = [
        [3, 2, 7],
        [5, 1, 4],
        [2, 6, 3],
        [8, 2, 5],
    ]
    result1 = solution.minimum_fatigue(n1, cost1)
    print(result1)  # Expected: 8

    # Example 2
    n2 = 3
    cost2 = [
        [1, 10, 10],
        [10, 1, 10],
        [10, 10, 1],
    ]
    result2 = solution.minimum_fatigue(n2, cost2)
    print(result2)  # Expected: -1

    # Additional small sanity checks

    # n = 1: choose the cheapest single theme
    n3 = 1
    cost3 = [[9, 4, 7]]
    result3 = solution.minimum_fatigue(n3, cost3)
    print(result3)  # Expected: 4

    # n = 2: choose the cheapest pair of different themes
    n4 = 2
    cost4 = [
        [1, 100, 100],
        [100, 2, 3],
    ]
    result4 = solution.minimum_fatigue(n4, cost4)
    print(result4)  # Expected: 3 (themes 0,1)

    # n = 5: alternating pattern
    n5 = 5
    cost5 = [
        [1, 5, 9],
        [4, 1, 8],
        [1, 7, 6],
        [3, 1, 5],
        [1, 9, 4],
    ]
    result5 = solution.minimum_fatigue(n5, cost5)
    print(result5)  # One optimal alternating schedule should be cheap