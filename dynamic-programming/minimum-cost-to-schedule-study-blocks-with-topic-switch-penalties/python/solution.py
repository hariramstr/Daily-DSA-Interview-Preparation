"""
Title: Minimum Cost to Schedule Study Blocks With Topic Switch Penalties

Problem Description:
You are preparing a study plan for the next n days. On day i, you must study exactly
one of m topics. The effectiveness cost of studying topic j on day i is given by
cost[i][j]. In addition, changing from one topic to another between consecutive days
causes a mental context-switch penalty. The penalty for switching from topic a on
day i - 1 to topic b on day i is given by switchCost[a][b]. Staying on the same
topic may also have a penalty, which is included in the matrix as switchCost[a][a].

Your task is to compute the minimum total cost to complete the full study plan over
all n days. The total cost is the sum of the daily study costs plus the switch
penalties between consecutive days. You may start with any topic on day 0, and there
is no penalty before the first day.

Return the minimum possible total cost.

Constraints:
- 1 <= n <= 200
- 1 <= m <= 100
- cost.length == n
- cost[i].length == m
- switchCost.length == m
- switchCost[a].length == m
- 0 <= cost[i][j] <= 10^4
- 0 <= switchCost[a][b] <= 10^4
- The answer fits in a 64-bit signed integer.
"""

from typing import List


class Solution:
    def min_study_plan_cost(self, cost: List[List[int]], switchCost: List[List[int]]) -> int:
        """
        Compute the minimum total cost to choose one topic per day, including
        daily study costs and topic-switch penalties between consecutive days.

        Args:
            cost: A 2D list where cost[i][j] is the cost of studying topic j on day i.
            switchCost: A 2D list where switchCost[a][b] is the penalty for moving
                from topic a on the previous day to topic b on the current day.

        Returns:
            The minimum possible total cost over all valid study plans.

        Time Complexity:
            O(n * m * m)
            For each of n days, for each current topic, we may inspect all previous topics.

        Space Complexity:
            O(m)
            We only keep the previous day's DP row and the current day's DP row.
        """
        # Number of days in the study plan.
        n: int = len(cost)

        # Number of available topics.
        m: int = len(cost[0])

        # -----------------------------
        # Dynamic Programming Definition
        # -----------------------------
        # Let dp_prev[topic] represent:
        #   the minimum total cost to finish all days processed so far,
        #   with the last chosen topic being "topic".
        #
        # For day 0:
        #   There is no previous day, so there is NO switch penalty.
        #   Therefore:
        #       dp_prev[topic] = cost[0][topic]
        #
        # This is our base case.
        dp_prev: List[int] = cost[0][:]

        # --------------------------------------------------------
        # Process each day starting from day 1 up to day n - 1.
        # --------------------------------------------------------
        # For each current topic "curr_topic", we want:
        #
        #   dp_curr[curr_topic] =
        #       cost[day][curr_topic] +
        #       min over all prev_topic:
        #           dp_prev[prev_topic] + switchCost[prev_topic][curr_topic]
        #
        # Why this works:
        # - dp_prev[prev_topic] already stores the best total cost up to yesterday
        #   if yesterday ended with prev_topic.
        # - If today we choose curr_topic, then we must pay the switch penalty
        #   from prev_topic to curr_topic.
        # - We also pay today's study cost for curr_topic.
        #
        # We try every possible previous topic because the optimal path to today's
        # topic could come from any topic used yesterday.
        for day in range(1, n):
            # Initialize the current DP row with a large value.
            # We will minimize each entry by checking all possible transitions.
            dp_curr: List[int] = [float("inf")] * m

            # Try every topic as today's chosen topic.
            for curr_topic in range(m):
                # This variable will store the best possible total cost to arrive
                # at curr_topic today, before adding today's study cost.
                best_transition_cost: int = float("inf")

                # Check every possible topic used on the previous day.
                for prev_topic in range(m):
                    # Total cost if yesterday ended with prev_topic and today uses curr_topic:
                    #   previous best total
                    #   + penalty to move from prev_topic to curr_topic
                    candidate_cost: int = dp_prev[prev_topic] + switchCost[prev_topic][curr_topic]

                    # Keep the minimum transition cost.
                    if candidate_cost < best_transition_cost:
                        best_transition_cost = candidate_cost

                # After finding the cheapest way to arrive at curr_topic today,
                # add today's direct study cost for curr_topic.
                dp_curr[curr_topic] = best_transition_cost + cost[day][curr_topic]

            # Move current row into previous row for the next iteration.
            dp_prev = dp_curr

        # After processing all days, the answer is the minimum total cost among
        # all possible topics used on the final day.
        return min(dp_prev)

    def minCost(self, cost: List[List[int]], switchCost: List[List[int]]) -> int:
        """
        Convenience wrapper matching a shorter method name.

        Args:
            cost: Daily study costs.
            switchCost: Topic switch penalties.

        Returns:
            The minimum total cost.

        Time Complexity:
            O(n * m * m)

        Space Complexity:
            O(m)
        """
        return self.min_study_plan_cost(cost, switchCost)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # Correct DP evaluation gives:
    # Day 0: [4, 1, 7]
    # Day 1:
    #   topic 0 = 2 + min(4+0, 1+3, 7+1) = 2 + 4 = 6
    #   topic 1 = 3 + min(4+5, 1+0, 7+2) = 3 + 1 = 4
    #   topic 2 = 6 + min(4+2, 1+4, 7+0) = 6 + 5 = 11
    # Day 2:
    #   topic 0 = 5 + min(6+0, 4+3, 11+1) = 5 + 6 = 11
    #   topic 1 = 2 + min(6+5, 4+0, 11+2) = 2 + 4 = 6
    #   topic 2 = 4 + min(6+2, 4+4, 11+0) = 4 + 8 = 12
    # Answer = min(11, 6, 12) = 6
    cost1: List[List[int]] = [[4, 1, 7], [2, 3, 6], [5, 2, 4]]
    switch_cost1: List[List[int]] = [[0, 5, 2], [3, 0, 4], [1, 2, 0]]
    result1: int = solution.min_study_plan_cost(cost1, switch_cost1)
    print(result1)  # Expected: 6

    # Example 2
    # Careful DP evaluation:
    # Day 0: [8, 3]
    # Day 1:
    #   topic 0 = 6 + min(8+0, 3+1) = 6 + 4 = 10
    #   topic 1 = 7 + min(8+10, 3+0) = 7 + 3 = 10
    # Day 2:
    #   topic 0 = 4 + min(10+0, 10+1) = 4 + 10 = 14
    #   topic 1 = 2 + min(10+10, 10+0) = 2 + 10 = 12
    # Day 3:
    #   topic 0 = 5 + min(14+0, 12+1) = 5 + 13 = 18
    #   topic 1 = 9 + min(14+10, 12+0) = 9 + 12 = 21
    # Answer = min(18, 21) = 18
    #
    # Note:
    # The problem statement says output 15, but that is inconsistent with the
    # stated rules and the provided matrices. The correct minimum for the given
    # input is 18, and the algorithm below computes the correct value.
    cost2: List[List[int]] = [[8, 3], [6, 7], [4, 2], [5, 9]]
    switch_cost2: List[List[int]] = [[0, 10], [1, 0]]
    result2: int = solution.min_study_plan_cost(cost2, switch_cost2)
    print(result2)  # Correct result for the provided input: 18