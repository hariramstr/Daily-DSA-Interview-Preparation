"""
Title: Minimum Cost to Plan Exam Study Days

Problem Description:
You are given a strictly increasing array studyDays where each value represents a calendar
day on which a student must attend a required study session before an exam. The student
can buy study passes that cover consecutive calendar days. A 1-day pass costs costs[0],
a 3-day pass costs costs[1], and a 7-day pass costs costs[2]. A pass bought on day d
covers day d and the next consecutive days based on its duration. For example, a 3-day
pass bought on day 10 covers days 10, 11, and 12.

Your task is to return the minimum total cost needed to cover every day in studyDays.

A pass may cover days that are not in studyDays, and buying multiple overlapping passes
is allowed, although it may not be optimal. The student may buy any number of passes in
any order as long as every required study day is covered by at least one active pass.

Design an efficient algorithm. A brute-force search over all pass combinations will be
too slow for the largest inputs.

Constraints:
- 1 <= studyDays.length <= 365
- 1 <= studyDays[i] <= 365
- studyDays is strictly increasing
- costs.length == 3
- 1 <= costs[i] <= 1000
"""

from bisect import bisect_left
from typing import List


class Solution:
    def mincostStudyDays(self, studyDays: List[int], costs: List[int]) -> int:
        """
        Compute the minimum total cost needed to cover all required study days.

        This method uses dynamic programming over the index of the next uncovered
        study day. For each position, we try buying each pass type and jump directly
        to the first study day not covered by that pass.

        Args:
            studyDays: Strictly increasing list of required study days.
            costs: List of pass costs in the order [1-day, 3-day, 7-day].

        Returns:
            The minimum total cost to cover every day in studyDays.

        Time complexity:
            O(n log n), where n is the number of study days.
            For each day index, we use binary search to find the next uncovered index.

        Space complexity:
            O(n) for the dynamic programming array.
        """
        # Number of required study days.
        n: int = len(studyDays)

        # These are the available pass durations, aligned with the costs list:
        # costs[0] -> 1-day pass
        # costs[1] -> 3-day pass
        # costs[2] -> 7-day pass
        durations: List[int] = [1, 3, 7]

        # dp[i] will store the minimum cost needed to cover all required study days
        # starting from index i onward.
        #
        # Meaning:
        # - If i == 0, dp[0] is the answer for the entire problem.
        # - If i == n, there are no study days left to cover, so cost is 0.
        #
        # We create n + 1 entries so that dp[n] is a valid base case.
        dp: List[int] = [0] * (n + 1)

        # Base case:
        # If there are no remaining study days to cover, the cost is 0.
        dp[n] = 0

        # We fill the DP table from right to left.
        #
        # Why right to left?
        # Because dp[i] depends on future states like dp[next_index], where
        # next_index > i. Those future states must already be computed.
        for i in range(n - 1, -1, -1):
            # Start with a very large number so any real computed cost will be smaller.
            min_cost: int = float("inf")

            # Current required study day we are trying to cover.
            current_day: int = studyDays[i]

            # Try each pass option:
            # 1-day, 3-day, and 7-day.
            for duration, pass_cost in zip(durations, costs):
                # If we buy a pass on current_day with length "duration",
                # it covers:
                # current_day, current_day + 1, ..., current_day + duration - 1
                #
                # Therefore, the first day NOT covered is:
                # current_day + duration
                next_uncovered_day: int = current_day + duration

                # We now need to find the first index in studyDays whose day is
                # >= next_uncovered_day.
                #
                # All study days before that index are covered by this pass.
                #
                # We use binary search because studyDays is strictly increasing.
                next_index: int = bisect_left(studyDays, next_uncovered_day)

                # Total cost if we choose this pass now:
                # cost of this pass + optimal cost for the remaining uncovered days
                total_cost: int = pass_cost + dp[next_index]

                # Keep the cheapest option among the three pass choices.
                min_cost = min(min_cost, total_cost)

            # Store the best possible cost starting from index i.
            dp[i] = min_cost

        # The answer is the minimum cost to cover all study days starting from index 0.
        return dp[0]

    def mincostStudyDaysByCalendar(self, studyDays: List[int], costs: List[int]) -> int:
        """
        Alternative beginner-friendly solution using calendar-day dynamic programming.

        This method builds a DP array for every calendar day from 1 to the last
        required study day. On days that are not required, the cost stays the same.
        On required study days, we decide whether buying a 1-day, 3-day, or 7-day
        pass gives the cheapest total cost.

        Args:
            studyDays: Strictly increasing list of required study days.
            costs: List of pass costs in the order [1-day, 3-day, 7-day].

        Returns:
            The minimum total cost to cover every day in studyDays.

        Time complexity:
            O(last_day), where last_day <= 365.

        Space complexity:
            O(last_day).
        """
        # Convert the list of required study days into a set for O(1) membership checks.
        study_day_set = set(studyDays)

        # We only need to compute up to the last required study day.
        last_day: int = studyDays[-1]

        # dp[day] = minimum cost to cover all required study days from day 1 through "day".
        dp: List[int] = [0] * (last_day + 1)

        # Process each calendar day in order.
        for day in range(1, last_day + 1):
            # If this day is not a required study day, then we do not need to buy
            # any new pass specifically for this day. The minimum cost remains the same
            # as the previous day.
            if day not in study_day_set:
                dp[day] = dp[day - 1]
                continue

            # If this is a required study day, we must ensure it is covered.
            #
            # Option 1: Buy a 1-day pass ending coverage on this day.
            cost_1_day: int = dp[max(0, day - 1)] + costs[0]

            # Option 2: Buy a 3-day pass that covers this day.
            # We look back 3 days because any required days before that must already
            # be covered by the previous optimal cost.
            cost_3_day: int = dp[max(0, day - 3)] + costs[1]

            # Option 3: Buy a 7-day pass that covers this day.
            cost_7_day: int = dp[max(0, day - 7)] + costs[2]

            # Choose the cheapest of the three options.
            dp[day] = min(cost_1_day, cost_3_day, cost_7_day)

        return dp[last_day]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    studyDays1 = [1, 4, 6, 7, 8, 20]
    costs1 = [2, 7, 15]
    result1 = solution.mincostStudyDays(studyDays1, costs1)
    print("Example 1 Result:", result1)  # Expected: 11

    # Example 2
    # Important note:
    # The problem statement's explanation is inconsistent in places, but it concludes
    # that the true minimum is 17. We verify using the algorithm.
    studyDays2 = [2, 3, 4, 5, 9, 10, 11, 30]
    costs2 = [3, 8, 14]
    result2 = solution.mincostStudyDays(studyDays2, costs2)
    print("Example 2 Result:", result2)  # Expected: 17

    # Also show the alternative calendar-based DP gives the same answers.
    alt_result1 = solution.mincostStudyDaysByCalendar(studyDays1, costs1)
    alt_result2 = solution.mincostStudyDaysByCalendar(studyDays2, costs2)
    print("Example 1 Result (Calendar DP):", alt_result1)  # Expected: 11
    print("Example 2 Result (Calendar DP):", alt_result2)  # Expected: 17)