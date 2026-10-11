"""
Title: Minimum Cost to Build a Meal Plan with Reuse Discounts

Problem Description:
You are planning meals for the next several days. For each day i, you must choose
exactly one dish from a list of available dishes for that day. Choosing dish j on
day i has a base cost costs[i][j]. However, if you choose the same dish on
consecutive days, you receive a reuse discount because ingredients can be reused.
Specifically, if dish j is chosen on both day i - 1 and day i, then the cost paid
for day i becomes costs[i][j] - discount[j]. The discounted cost will never be
negative.

Your task is to compute the minimum total cost needed to build a valid meal plan
across all days.

A meal plan is valid if exactly one dish is selected for every day. The number of
available dishes is the same for every day.

Return the minimum possible total cost.

Constraints:
- 1 <= days == costs.length <= 200
- 1 <= dishes == costs[i].length <= 200
- 0 <= costs[i][j] <= 10^4
- 0 <= discount[j] <= 10^4
- For every i, costs[i].length == discount.length
- It is guaranteed that costs[i][j] - discount[j] >= 0 whenever the discount is applied

Important note about the provided examples:
The written explanation in Example 1 contains an arithmetic inconsistency:
5 + (4 - 2) + 2 = 9, not 10.
So the true minimum for Example 1 is 9.
The algorithm below computes the mathematically correct minimum total cost.
"""

from typing import List


class Solution:
    def min_meal_plan_cost(self, costs: List[List[int]], discount: List[int]) -> int:
        """
        Compute the minimum total cost to choose exactly one dish per day,
        applying a discount when the same dish is reused on consecutive days.

        Args:
            costs: A 2D list where costs[i][j] is the base cost of choosing
                dish j on day i.
            discount: A list where discount[j] is the amount subtracted from
                the cost of dish j when it is chosen on two consecutive days.

        Returns:
            The minimum possible total cost across all days.

        Time complexity:
            O(days * dishes)

        Space complexity:
            O(dishes)

        Explanation of the optimization:
            A direct dynamic programming transition would be:

                dp[i][j] = min(
                    dp[i - 1][j] + costs[i][j] - discount[j],   # reuse same dish
                    min(dp[i - 1][k] for k != j) + costs[i][j]  # switch dishes
                )

            The expensive part is repeatedly computing min over all k != j,
            which would make the solution O(days * dishes^2).

            To optimize this, for each previous day we track:
            - the smallest value in the previous DP row
            - the second smallest value in the previous DP row
            - the index of the dish that achieved the smallest value

            Then for each current dish j:
            - if the global minimum came from a different dish, we can switch
              from that minimum
            - otherwise, we must use the second minimum to ensure k != j

            This reduces the transition to O(1) per dish, giving O(days * dishes).
        """
        # Number of days in the meal plan.
        days: int = len(costs)

        # Number of dishes available each day.
        dishes: int = len(costs[0])

        # Base case:
        # On day 0, there is no previous day, so no reuse discount can apply.
        # Therefore, the minimum total cost ending with dish j on day 0 is
        # simply the base cost of choosing dish j on day 0.
        prev_dp: List[int] = costs[0][:]

        # Process each later day one by one.
        for day in range(1, days):
            # We need the smallest and second smallest values from prev_dp.
            # Why?
            # For the current dish j:
            #   - If we continue using the same dish j, we use prev_dp[j].
            #   - If we switch from a different dish, we need the minimum
            #     prev_dp[k] where k != j.
            #
            # If the overall minimum in prev_dp comes from some dish index m:
            #   - For any j != m, the best "switch" source is that overall minimum.
            #   - For j == m, we cannot switch from the same dish, so we must use
            #     the second smallest value instead.
            min1_value: int = float("inf")
            min2_value: int = float("inf")
            min1_index: int = -1

            # Find the smallest and second smallest values in prev_dp.
            for dish in range(dishes):
                value = prev_dp[dish]

                if value < min1_value:
                    # Current value becomes the new smallest.
                    # The old smallest is pushed down to second smallest.
                    min2_value = min1_value
                    min1_value = value
                    min1_index = dish
                elif value < min2_value:
                    # Current value is not the smallest, but it improves
                    # the second smallest.
                    min2_value = value

            # Build the DP values for the current day.
            curr_dp: List[int] = [0] * dishes

            for dish in range(dishes):
                # Option 1: Reuse the same dish as yesterday.
                # If yesterday also ended with this dish, today's cost gets
                # the reuse discount.
                reuse_cost: int = prev_dp[dish] + costs[day][dish] - discount[dish]

                # Option 2: Switch from a different dish.
                # We must use the best previous total cost among all dishes
                # except the current one.
                if dish != min1_index:
                    # The globally smallest previous total came from a different dish,
                    # so it is valid to switch from it.
                    best_previous_different: int = min1_value
                else:
                    # The globally smallest previous total came from the same dish,
                    # so to truly switch, we must use the second smallest total.
                    best_previous_different = min2_value

                switch_cost: int = best_previous_different + costs[day][dish]

                # The best way to end today with this dish is the cheaper of:
                # - reusing the same dish and taking the discount
                # - switching from a different dish and paying full base cost
                curr_dp[dish] = min(reuse_cost, switch_cost)

            # Move to the next day.
            prev_dp = curr_dp

        # After processing all days, the answer is the minimum total cost among
        # all possible dishes chosen on the final day.
        return min(prev_dp)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    # The prompt states output 10, but the arithmetic in its own explanation
    # shows a valid plan with total cost 9:
    # Day 0: dish 0 -> 5
    # Day 1: dish 0 again -> 4 - 2 = 2
    # Day 2: dish 1 -> 2
    # Total = 5 + 2 + 2 = 9
    costs_1: List[List[int]] = [[5, 8, 6], [4, 7, 3], [6, 2, 5]]
    discount_1: List[int] = [2, 3, 1]
    result_1: int = solution.min_meal_plan_cost(costs_1, discount_1)
    print("Example 1 result:", result_1)  # Correct mathematical result: 9

    # Example 2 from the prompt.
    costs_2: List[List[int]] = [[3, 9], [8, 1], [2, 7], [6, 4]]
    discount_2: List[int] = [5, 0]
    result_2: int = solution.min_meal_plan_cost(costs_2, discount_2)
    print("Example 2 result:", result_2)  # Expected: 10