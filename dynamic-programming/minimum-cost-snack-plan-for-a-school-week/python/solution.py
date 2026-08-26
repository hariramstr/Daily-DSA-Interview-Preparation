"""
Title: Minimum Cost Snack Plan for a School Week

Problem Description:
A school cafeteria sells snack passes for the next n days. On day i, a student may or may not want to buy a snack.
You are given an integer array days where each value is a day number on which the student wants a snack, in strictly
increasing order. The cafeteria offers exactly three pass types: a 1-day pass, a 3-day pass, and a 7-day pass.
A pass covers the day it is bought and the following consecutive days in its duration. For example, if a 3-day pass
is bought on day 5, it covers days 5, 6, and 7. You are also given an integer array costs of length 3, where
costs[0], costs[1], and costs[2] are the prices of the 1-day, 3-day, and 7-day passes.

Return the minimum total cost needed to cover every day in days.

This is a dynamic programming problem because the cheapest way to cover later snack days depends on the cheapest way
to cover earlier ones. A correct solution should consider whether buying a longer pass now can reduce the total cost
compared with buying several short passes.

Constraints:
- 1 <= days.length <= 365
- 1 <= days[i] <= 365
- days is strictly increasing
- costs.length == 3
- 1 <= costs[i] <= 1000
"""

from typing import List


class Solution:
    def mincostTickets(self, days: List[int], costs: List[int]) -> int:
        """
        Compute the minimum total cost to cover all required snack days.

        This method uses dynamic programming over the calendar days from day 1 up to
        the last required day. For each day:
        - If the student does not need a snack that day, the cost stays the same as the previous day.
        - If the student does need a snack that day, we try all three pass options:
          1-day, 3-day, and 7-day, and choose the cheapest total.

        Args:
            days: Strictly increasing list of day numbers on which a snack is needed.
            costs: List of three integers representing the costs of 1-day, 3-day, and 7-day passes.

        Returns:
            The minimum total cost needed to cover every day in days.

        Time complexity:
            O(last_day), where last_day is the final day in the input and last_day <= 365.

        Space complexity:
            O(last_day), for the dynamic programming array.
        """
        # The last required snack day tells us how far we need to compute.
        # There is no reason to process days beyond the final day in the input,
        # because no future day needs coverage.
        last_day: int = days[-1]

        # We convert the list of required days into a set so that checking
        # "Is this a snack day?" becomes very fast.
        #
        # Why use a set?
        # - Membership testing in a list is O(n)
        # - Membership testing in a set is O(1) on average
        #
        # Since we check every day from 1 to last_day, using a set is a clean
        # and efficient choice.
        travel_days = set(days)

        # dp[d] will store the minimum cost needed to cover all required snack days
        # from day 1 through day d.
        #
        # Example meaning:
        # - dp[0] = 0 means before any days happen, cost is 0.
        # - dp[5] = minimum cost to cover all required days up to day 5.
        #
        # We create an array of size last_day + 1 so that indices match actual day numbers.
        dp: List[int] = [0] * (last_day + 1)

        # Process each calendar day in order.
        # This left-to-right order is important because each day's answer depends
        # on earlier days that have already been solved.
        for day in range(1, last_day + 1):
            # If today is NOT a required snack day, then we do not need to buy anything today.
            # Therefore, the minimum cost up to today is exactly the same as the minimum cost
            # up to yesterday.
            if day not in travel_days:
                dp[day] = dp[day - 1]
                continue

            # If today IS a required snack day, then we must ensure today is covered.
            # We consider the three possible pass purchases that could cover today:
            #
            # 1) Buy a 1-day pass ending coverage for today only.
            #    Then total cost = best cost before today + cost of 1-day pass.
            #
            # 2) Buy a 3-day pass that covers today and the previous 2 days in its range.
            #    If bought on day (day-2), it covers day-2, day-1, day.
            #    In DP terms, we can think of it as:
            #    total cost = best cost before the 3-day coverage starts + cost of 3-day pass.
            #
            # 3) Buy a 7-day pass similarly.
            #
            # We use max(0, day - duration) so that we never go below index 0.
            # This handles early days like day 1 or day 2 cleanly.
            cost_1_day: int = dp[max(0, day - 1)] + costs[0]
            cost_3_day: int = dp[max(0, day - 3)] + costs[1]
            cost_7_day: int = dp[max(0, day - 7)] + costs[2]

            # The best choice for today is whichever of the three options gives
            # the smallest total cost.
            dp[day] = min(cost_1_day, cost_3_day, cost_7_day)

        # The answer is the minimum cost to cover all required days up to the final required day.
        return dp[last_day]

    def solve(self, days: List[int], costs: List[int]) -> int:
        """
        Wrapper method that calls the main dynamic programming solution.

        Args:
            days: Strictly increasing list of day numbers on which a snack is needed.
            costs: List of three integers representing pass costs.

        Returns:
            The minimum total cost needed to cover every required day.

        Time complexity:
            O(last_day), where last_day <= 365.

        Space complexity:
            O(last_day).
        """
        return self.mincostTickets(days, costs)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    days1: List[int] = [1, 2, 4, 5, 6]
    costs1: List[int] = [3, 7, 12]
    result1: int = solution.solve(days1, costs1)
    print("Example 1 Result:", result1)  # Expected: 12

    # Example 2
    days2: List[int] = [2, 3, 8, 9, 10, 14]
    costs2: List[int] = [2, 5, 9]
    result2: int = solution.solve(days2, costs2)
    print("Example 2 Result:", result2)  # Based on correct DP evaluation: 11

    # Additional simple test
    days3: List[int] = [1]
    costs3: List[int] = [2, 5, 10]
    result3: int = solution.solve(days3, costs3)
    print("Additional Test Result:", result3)  # Expected: 2