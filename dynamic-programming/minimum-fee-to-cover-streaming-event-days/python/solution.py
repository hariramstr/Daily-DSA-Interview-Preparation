"""
Title: Minimum Fee to Cover Streaming Event Days

Problem Description:
A media platform plans to broadcast a set of live events on specific calendar days.
To handle traffic, the platform can purchase server reservation passes of different
durations. A 1-day pass costs cost1, a 7-day pass costs cost7, and a 30-day pass
costs cost30. A pass purchased for day d covers day d and the next consecutive days
within its duration. For example, a 7-day pass bought on day 10 covers days 10
through 16 inclusive.

You are given a strictly increasing integer array days, where days[i] is a day on
which at least one live event must be supported, and an array costs of length 3
where costs = [cost1, cost7, cost30].

Return the minimum total fee required to cover every event day in days.

You may buy any number of passes, and passes may overlap, but overlapping coverage
does not provide any extra benefit beyond covering the required days. The goal is
to choose passes so that every day in days is covered at minimum total cost.

Constraints:
- 1 <= days.length <= 365
- 1 <= days[i] <= 365
- days is strictly increasing
- 1 <= costs[i] <= 1000
"""

from typing import List


class Solution:
    def mincostTickets(self, days: List[int], costs: List[int]) -> int:
        """
        Compute the minimum total fee needed to cover all required event days.

        This uses dynamic programming over calendar days from day 1 up to the last
        required event day. For each day:
        - If it is not an event day, the cost stays the same as the previous day.
        - If it is an event day, we choose the cheapest among:
          1) buying a 1-day pass ending coverage for today,
          2) buying a 7-day pass covering today,
          3) buying a 30-day pass covering today.

        Args:
            days: Strictly increasing list of event days that must be covered.
            costs: List of three integers [cost1, cost7, cost30].

        Returns:
            The minimum total fee required to cover every event day.

        Time complexity:
            O(last_day), where last_day = days[-1] <= 365

        Space complexity:
            O(last_day)
        """
        # The last required event day tells us how far we need to compute.
        # There is no reason to process beyond that day, because no future day
        # needs coverage.
        last_day: int = days[-1]

        # Convert the list of required days into a set for O(1) membership checks.
        # This lets us quickly ask:
        # "Is this calendar day a day that must be covered?"
        travel_days = set(days)

        # dp[day] will store the minimum cost needed to cover all required event
        # days from day 1 through this exact calendar day.
        #
        # We create an array of size last_day + 1 so that index == calendar day.
        # dp[0] = 0 means: before any day starts, the cost is zero.
        dp: List[int] = [0] * (last_day + 1)

        # Process every calendar day in order.
        # This bottom-up order works because the answer for today depends only on
        # earlier days, which have already been computed.
        for day in range(1, last_day + 1):
            # If today is NOT a required event day, then we do not need to buy
            # anything new today.
            #
            # Therefore, the minimum cost up to today is exactly the same as the
            # minimum cost up to yesterday.
            if day not in travel_days:
                dp[day] = dp[day - 1]
                continue

            # If today IS a required event day, then today must be covered by some pass.
            # We consider all three possible pass purchases that could cover today.

            # Option 1: Buy a 1-day pass that covers only today.
            #
            # Then the total cost is:
            #   minimum cost up to yesterday + cost of 1-day pass
            cost_with_1_day: int = dp[day - 1] + costs[0]

            # Option 2: Buy a 7-day pass that covers days [day-6, ..., day].
            #
            # To combine this with previous optimal work, we need the minimum cost
            # up to the day before that 7-day coverage starts.
            #
            # If day is less than 7, then the pass reaches back before day 1.
            # In that case, the previous cost should be treated as dp[0] = 0.
            start_before_7_day_window: int = max(0, day - 7)
            cost_with_7_day: int = dp[start_before_7_day_window] + costs[1]

            # Option 3: Buy a 30-day pass that covers days [day-29, ..., day].
            #
            # Same logic as above: combine the pass cost with the optimal cost
            # before the pass coverage begins.
            start_before_30_day_window: int = max(0, day - 30)
            cost_with_30_day: int = dp[start_before_30_day_window] + costs[2]

            # Choose the cheapest of the three valid ways to ensure today is covered.
            dp[day] = min(cost_with_1_day, cost_with_7_day, cost_with_30_day)

        # The answer is the minimum cost to cover all required event days up to
        # the final required day.
        return dp[last_day]


if __name__ == "__main__":
    solution = Solution()

    days1 = [1, 4, 6, 7, 8, 20]
    costs1 = [2, 7, 15]
    result1 = solution.mincostTickets(days1, costs1)
    print("Example 1 Result:", result1)  # Expected: 11

    days2 = [2, 3, 4, 5, 6, 7, 8, 9, 15, 16, 17, 40]
    costs2 = [3, 8, 20]
    result2 = solution.mincostTickets(days2, costs2)
    print("Example 2 Result:", result2)  # Correct minimum for these inputs

    # Additional quick sanity check
    days3 = [1]
    costs3 = [5, 10, 20]
    result3 = solution.mincostTickets(days3, costs3)
    print("Additional Test Result:", result3)  # Expected: 5