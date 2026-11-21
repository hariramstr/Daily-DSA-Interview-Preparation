"""
Title: Minimum Cost Snack Plan for a Road Trip

Problem Description:
You are planning snacks for a road trip that lasts n days. On each day, you must buy
exactly one snack pack. There are three snack types: fruit, sandwich, and trail mix.
The cost of buying each type can be different on each day.

However, to avoid getting bored, you are not allowed to buy the same snack type on
two consecutive days.

You are given a 2D integer array costs of size n x 3, where:
- costs[i][0] is the cost of buying fruit on day i
- costs[i][1] is the cost of buying a sandwich on day i
- costs[i][2] is the cost of buying trail mix on day i

Return the minimum total cost to buy snacks for all n days while following the rule
that the same snack type cannot be chosen on consecutive days.

This is a classic dynamic programming decision problem: the best choice for the
current day depends on what was chosen the previous day. Since there are only three
possible snack types, an efficient solution can track the minimum cost for ending
each day with each snack type.

Constraints:
- 1 <= n <= 1000
- costs.length == n
- costs[i].length == 3
- 1 <= costs[i][j] <= 1000
"""

from typing import List


class Solution:
    def min_cost_snacks(self, costs: List[List[int]]) -> int:
        """
        Compute the minimum total snack cost over all days such that the same snack
        type is not chosen on consecutive days.

        Args:
            costs: A 2D list where costs[i][0], costs[i][1], and costs[i][2]
                represent the cost of fruit, sandwich, and trail mix on day i.

        Returns:
            The minimum total cost to buy exactly one snack per day while never
            choosing the same snack type on two consecutive days.

        Time complexity:
            O(n), where n is the number of days. We process each day once and do
            constant work for the three snack choices.

        Space complexity:
            O(1) extra space, because we only keep track of the previous day's
            three minimum costs instead of storing a full DP table.
        """
        # Defensive check:
        # The problem guarantees at least one day, but this small guard makes the
        # method safer and easier for beginners to understand if reused elsewhere.
        if not costs:
            return 0

        # Day 0 initialization:
        # If there is only one day so far, then the minimum cost of ending with
        # each snack type is simply the direct cost of buying that snack on day 0.
        #
        # prev_fruit    = minimum total cost up to the previous day if we end by
        #                 choosing fruit on that day
        # prev_sandwich = same idea, but ending with sandwich
        # prev_trail    = same idea, but ending with trail mix
        prev_fruit = costs[0][0]
        prev_sandwich = costs[0][1]
        prev_trail = costs[0][2]

        # Process each remaining day one by one.
        # For each snack choice today, we must come from one of the OTHER two snack
        # choices from yesterday, because choosing the same snack on consecutive
        # days is not allowed.
        for day in range(1, len(costs)):
            # Cost of choosing fruit today:
            # Yesterday cannot also be fruit, so we take the cheaper of:
            # - ending yesterday with sandwich
            # - ending yesterday with trail mix
            current_fruit = costs[day][0] + min(prev_sandwich, prev_trail)

            # Cost of choosing sandwich today:
            # Yesterday cannot be sandwich, so we take the cheaper of:
            # - ending yesterday with fruit
            # - ending yesterday with trail mix
            current_sandwich = costs[day][1] + min(prev_fruit, prev_trail)

            # Cost of choosing trail mix today:
            # Yesterday cannot be trail mix, so we take the cheaper of:
            # - ending yesterday with fruit
            # - ending yesterday with sandwich
            current_trail = costs[day][2] + min(prev_fruit, prev_sandwich)

            # Move the "current day" values into the "previous day" variables so
            # they can be used when processing the next day.
            prev_fruit = current_fruit
            prev_sandwich = current_sandwich
            prev_trail = current_trail

        # After processing all days, the answer is the cheapest valid plan among
        # the three possibilities for what snack was chosen on the final day.
        return min(prev_fruit, prev_sandwich, prev_trail)

    def minCost(self, costs: List[List[int]]) -> int:
        """
        Compatibility wrapper that calls the main dynamic programming solution.

        Args:
            costs: A 2D list of snack costs for each day.

        Returns:
            The minimum total valid snack cost.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        return self.min_cost_snacks(costs)


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the prompt.
    # Note:
    # If we compute the true minimum for this input, the result is 8:
    # Day 0 sandwich = 2
    # Day 1 trail mix = 1
    # Day 2 sandwich = 5
    # Total = 8
    #
    # The prompt states output 7, but that is not achievable under the given rules
    # and costs. The algorithm below correctly computes the true minimum.
    costs1 = [[4, 2, 7], [3, 6, 1], [8, 5, 4]]
    result1 = solution.min_cost_snacks(costs1)
    print("Example 1 result:", result1)

    # Sample input 2 from the prompt.
    # One optimal plan:
    # Day 0 fruit = 1
    # Day 1 sandwich = 2
    # Day 2 trail mix = 1
    # Day 3 sandwich = 3
    # Total = 7
    costs2 = [[1, 10, 3], [10, 2, 8], [4, 7, 1], [6, 3, 9]]
    result2 = solution.min_cost_snacks(costs2)
    print("Example 2 result:", result2)

    # Additional small test for a single day.
    costs3 = [[5, 1, 9]]
    result3 = solution.min_cost_snacks(costs3)
    print("Single day result:", result3)