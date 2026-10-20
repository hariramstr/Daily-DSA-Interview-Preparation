"""
Title: Minimum Cost to Plan Cafeteria Meals with No Same Dish on Consecutive Days

Problem Description:
A company cafeteria offers exactly three meal options each workday: Pasta, Salad, and Soup.
You are given a 2D array costs where costs[i][j] is the cost of choosing meal j on day i,
with j = 0 for Pasta, j = 1 for Salad, and j = 2 for Soup.

You must choose exactly one meal for each day. However, to keep the menu varied, you are not
allowed to choose the same meal on two consecutive days.

Your task is to compute the minimum total cost needed to plan meals for all days while
satisfying the no-consecutive-repeat rule.

Return the minimum possible total cost.

This problem is intended to test dynamic programming fundamentals: making an optimal choice
for the current day based on the best valid choices from previous days.

Constraints:
- 1 <= costs.length <= 100000
- costs[i].length == 3
- 1 <= costs[i][j] <= 10000
- You must choose one meal every day
"""

from typing import List


class Solution:
    def min_cost_meals(self, costs: List[List[int]]) -> int:
        """
        Compute the minimum total meal cost with the rule that the same meal
        cannot be chosen on consecutive days.

        Args:
            costs: A list where costs[i][j] is the cost of choosing meal j on day i.
                   j = 0 -> Pasta, j = 1 -> Salad, j = 2 -> Soup.

        Returns:
            The minimum possible total cost to plan meals for all days.

        Time complexity:
            O(n), where n is the number of days.
            We process each day exactly once and do constant work per day.

        Space complexity:
            O(1) extra space.
            We only keep track of the previous day's three dynamic programming values.
        """
        # Defensive handling:
        # The constraints guarantee at least one day, but checking for empty input
        # makes the method safer and easier to reuse in general contexts.
        if not costs:
            return 0

        # Dynamic Programming Idea:
        #
        # Let:
        #   prev_pasta = minimum total cost up to the previous day
        #                if we chose Pasta on the previous day
        #   prev_salad = minimum total cost up to the previous day
        #                if we chose Salad on the previous day
        #   prev_soup  = minimum total cost up to the previous day
        #                if we chose Soup on the previous day
        #
        # For the first day, there is no previous day, so the best cost for each meal
        # is simply the cost of choosing that meal on day 0.
        prev_pasta = costs[0][0]
        prev_salad = costs[0][1]
        prev_soup = costs[0][2]

        # Process each remaining day one by one.
        # On each day, we compute the best total cost if we end that day with:
        #   - Pasta
        #   - Salad
        #   - Soup
        #
        # Because we cannot repeat the same meal on consecutive days:
        #   current Pasta can only come from previous Salad or previous Soup
        #   current Salad can only come from previous Pasta or previous Soup
        #   current Soup  can only come from previous Pasta or previous Salad
        for day in range(1, len(costs)):
            pasta_cost, salad_cost, soup_cost = costs[day]

            # If we choose Pasta today, yesterday must have been Salad or Soup.
            # So we take the cheaper of those two valid previous states.
            current_pasta = pasta_cost + min(prev_salad, prev_soup)

            # If we choose Salad today, yesterday must have been Pasta or Soup.
            current_salad = salad_cost + min(prev_pasta, prev_soup)

            # If we choose Soup today, yesterday must have been Pasta or Salad.
            current_soup = soup_cost + min(prev_pasta, prev_salad)

            # Move the "current day" results into the "previous day" variables
            # so they become the base for the next iteration.
            prev_pasta = current_pasta
            prev_salad = current_salad
            prev_soup = current_soup

        # After processing all days, the answer is the cheapest valid plan
        # among the three possibilities for the final day.
        return min(prev_pasta, prev_salad, prev_soup)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # costs = [[5,9,4],[8,7,6],[3,5,10]]
    # Day 0:
    #   Pasta = 5, Salad = 9, Soup = 4
    # Day 1:
    #   Pasta = 8 + min(9, 4) = 12
    #   Salad = 7 + min(5, 4) = 11
    #   Soup  = 6 + min(5, 9) = 11
    # Day 2:
    #   Pasta = 3 + min(11, 11) = 14
    #   Salad = 5 + min(12, 11) = 16
    #   Soup  = 10 + min(12, 11) = 21
    # Answer = min(14, 16, 21) = 14
    costs1 = [[5, 9, 4], [8, 7, 6], [3, 5, 10]]
    result1 = solution.min_cost_meals(costs1)
    print("Example 1 Output:", result1)  # Expected: 14

    # Example 2
    # costs = [[7,2,8],[6,4,3],[5,9,1],[2,7,6]]
    # Day 0:
    #   Pasta = 7, Salad = 2, Soup = 8
    # Day 1:
    #   Pasta = 6 + min(2, 8) = 8
    #   Salad = 4 + min(7, 8) = 11
    #   Soup  = 3 + min(7, 2) = 5
    # Day 2:
    #   Pasta = 5 + min(11, 5) = 10
    #   Salad = 9 + min(8, 5) = 14
    #   Soup  = 1 + min(8, 11) = 9
    # Day 3:
    #   Pasta = 2 + min(14, 9) = 11
    #   Salad = 7 + min(10, 9) = 16
    #   Soup  = 6 + min(10, 14) = 16
    # Answer = min(11, 16, 16) = 11
    #
    # Note:
    # The problem statement's listed "Output: 8" is inconsistent with its own explanation.
    # The correct minimum for this input is 11, and the DP trace confirms it.
    costs2 = [[7, 2, 8], [6, 4, 3], [5, 9, 1], [2, 7, 6]]
    result2 = solution.min_cost_meals(costs2)
    print("Example 2 Output:", result2)  # Expected: 11

    # Additional simple test: single day
    costs3 = [[3, 1, 2]]
    result3 = solution.min_cost_meals(costs3)
    print("Single Day Output:", result3)  # Expected: 1