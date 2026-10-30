"""
Title: Minimum Fatigue to Plan Alternating Workout Intensities

Problem Description:
You are given a list of daily workout sessions. For day i, you may perform the session
in one of three intensity modes: low, medium, or high. The fatigue cost of choosing
each mode on that day is given by costs[i][0], costs[i][1], and costs[i][2] respectively.

To avoid overtraining, your coach imposes a rule: you may not choose the same intensity
mode on two consecutive days. Your task is to compute the minimum total fatigue needed
to complete all sessions while following this rule.

Return the minimum possible sum of fatigue costs over all days.

This is a dynamic programming problem because the best choice for a day depends on the
mode chosen on the previous day. A greedy choice for the current day may lead to a worse
overall result later.

Constraints:
- 1 <= n == costs.length <= 100000
- costs[i].length == 3
- 1 <= costs[i][j] <= 10^4
- The answer fits in a 32-bit signed integer.
"""

from typing import List


class Solution:
    def min_fatigue(self, costs: List[List[int]]) -> int:
        """
        Compute the minimum total fatigue while never choosing the same intensity
        mode on two consecutive days.

        Args:
            costs: A list where costs[i][0], costs[i][1], and costs[i][2] are the
                fatigue costs for low, medium, and high intensity on day i.

        Returns:
            The minimum possible total fatigue cost.

        Time complexity:
            O(n), where n is the number of days. We process each day once.

        Space complexity:
            O(1) extra space, because we only keep the previous day's three DP values.
        """
        # Defensive handling:
        # The constraints guarantee at least one day, but this small check makes the
        # method more robust and beginner-friendly if reused elsewhere.
        if not costs:
            return 0

        # Dynamic Programming idea:
        #
        # Let:
        #   low_cost    = minimum total fatigue up to the current day if today is LOW
        #   medium_cost = minimum total fatigue up to the current day if today is MEDIUM
        #   high_cost   = minimum total fatigue up to the current day if today is HIGH
        #
        # For day 0, these values are simply the direct costs of choosing each mode,
        # because there is no previous day and therefore no adjacency restriction yet.
        low_cost = costs[0][0]
        medium_cost = costs[0][1]
        high_cost = costs[0][2]

        # We now process each following day one by one.
        # For each mode today, we must come from one of the OTHER two modes yesterday.
        #
        # Example:
        # If we choose LOW today, then yesterday could only have been MEDIUM or HIGH.
        # So:
        #   new_low = costs[i][0] + min(previous_medium, previous_high)
        #
        # The same logic applies symmetrically for MEDIUM and HIGH.
        for day in range(1, len(costs)):
            # Read today's three direct fatigue costs for clarity.
            today_low = costs[day][0]
            today_medium = costs[day][1]
            today_high = costs[day][2]

            # Compute the best total fatigue if we end today with LOW intensity.
            # We are not allowed to come from LOW yesterday, so we choose the cheaper
            # of yesterday's MEDIUM and HIGH totals.
            new_low_cost = today_low + min(medium_cost, high_cost)

            # Compute the best total fatigue if we end today with MEDIUM intensity.
            # We cannot come from MEDIUM yesterday, so we choose the cheaper of
            # yesterday's LOW and HIGH totals.
            new_medium_cost = today_medium + min(low_cost, high_cost)

            # Compute the best total fatigue if we end today with HIGH intensity.
            # We cannot come from HIGH yesterday, so we choose the cheaper of
            # yesterday's LOW and MEDIUM totals.
            new_high_cost = today_high + min(low_cost, medium_cost)

            # Move the DP window forward:
            # The "new" values become the current best totals for the next iteration.
            low_cost = new_low_cost
            medium_cost = new_medium_cost
            high_cost = new_high_cost

        # After processing all days, the answer is the cheapest valid plan among the
        # three possibilities for the final day.
        return min(low_cost, medium_cost, high_cost)

    def minFatigue(self, costs: List[List[int]]) -> int:
        """
        Compatibility wrapper using camelCase naming.

        Args:
            costs: A list of daily fatigue costs for low, medium, and high intensity.

        Returns:
            The minimum possible total fatigue cost.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        return self.min_fatigue(costs)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # The prompt's written explanation is inconsistent, but the correct minimum is 8.
    # Let's verify by DP:
    # Day 0: [5, 3, 4]
    # Day 1:
    #   low    = 6 + min(3, 4) = 9
    #   medium = 2 + min(5, 4) = 6
    #   high   = 7 + min(5, 3) = 10
    # Day 2:
    #   low    = 3 + min(6, 10) = 9
    #   medium = 8 + min(9, 10) = 17
    #   high   = 1 + min(9, 6) = 7
    # Answer = min(9, 17, 7) = 7
    #
    # So the mathematically correct answer for Example 1 is 7, not 6 or 8.
    # One optimal plan is:
    #   Day 0: high   = 4
    #   Day 1: medium = 2
    #   Day 2: high   = 1
    # This is valid because only consecutive days must differ, and day 1 != day 2.
    costs1 = [[5, 3, 4], [6, 2, 7], [3, 8, 1]]
    result1 = solution.min_fatigue(costs1)
    print("Example 1 result:", result1)

    # Example 2:
    # low, medium, high, low => 1 + 1 + 1 + 1 = 4
    costs2 = [[1, 10, 100], [10, 1, 100], [100, 10, 1], [1, 100, 10]]
    result2 = solution.min_fatigue(costs2)
    print("Example 2 result:", result2)

    # Additional small sanity check:
    # Only one day: choose the cheapest mode directly.
    costs3 = [[9, 2, 5]]
    result3 = solution.min_fatigue(costs3)
    print("Single day result:", result3)