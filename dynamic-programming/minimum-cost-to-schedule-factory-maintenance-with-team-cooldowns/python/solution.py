"""
Title: Minimum Cost to Schedule Factory Maintenance With Team Cooldowns

Problem Description:
A factory must perform maintenance on a sequence of n machines over n consecutive days.
On day i, exactly one maintenance team must be assigned to machine i.

There are 3 available teams:
- Electrical
- Mechanical
- Software

Assigning team t to machine i has a known cost cost[i][t].

Cooldown rule:
If a team is used on day i, that same team cannot be used again on day i + 1
or day i + 2. So the team chosen today must be different from the teams used
on each of the previous two days.

Goal:
Compute the minimum total maintenance cost to complete all n days while
satisfying the cooldown rule. If no valid schedule exists, return -1.

Why dynamic programming:
The cheapest choice for the current day depends on which teams were used on the
previous one or two days, so we must keep track of recent history.

Constraints:
- 1 <= n <= 100000
- cost.length == n
- cost[i].length == 3
- 1 <= cost[i][t] <= 1000000
"""

from typing import List, Dict, Tuple


class Solution:
    def min_maintenance_cost(self, cost: List[List[int]]) -> int:
        """
        Compute the minimum total maintenance cost under the 2-day cooldown rule.

        We use dynamic programming where the state stores the last two teams used.
        For each day, we try assigning one of the 3 teams, but only if it is
        different from both teams used on the previous two days.

        Because there are only 3 teams, the number of possible "last two team"
        states is very small, so we can process all days efficiently.

        Args:
            cost: A list where cost[i][t] is the cost of assigning team t on day i.

        Returns:
            The minimum possible total cost, or -1 if no valid schedule exists.

        Time complexity:
            O(n * S * 3), where S is the number of states.
            Since S is at most 16 here (including sentinel states), this is O(n).

        Space complexity:
            O(S), which is O(1) because the number of states is constant.
        """
        # ------------------------------------------------------------
        # Basic input handling
        # ------------------------------------------------------------
        # If there are no days, the cost would be 0. The problem states n >= 1,
        # but handling this case makes the method more robust and complete.
        if not cost:
            return 0

        n: int = len(cost)

        # ------------------------------------------------------------
        # Important observation about feasibility
        # ------------------------------------------------------------
        # There are exactly 3 teams.
        #
        # Starting from day 2 (the third day), the chosen team must be different
        # from the teams used on day i-1 and day i-2.
        #
        # Since there are exactly 3 teams total:
        # - if the previous two days used two different teams,
        #   then the current day is forced to use the third team.
        #
        # Therefore, once the first two days are chosen (and they must be different),
        # the rest of the schedule is completely determined.
        #
        # Also, because there are 3 teams, a valid schedule always exists for any n:
        # choose any team on day 0, a different team on day 1, and then the unique
        # remaining team on day 2, and continue cyclically.
        #
        # Even though we could solve this with a tiny brute force over the 6 possible
        # ordered choices for the first two days, we will still present it as a clean
        # dynamic programming solution, because that directly matches the problem's
        # intended reasoning and is beginner-friendly.

        # ------------------------------------------------------------
        # DP state definition
        # ------------------------------------------------------------
        # We will represent a state as:
        #   (prev2, prev1)
        #
        # where:
        # - prev2 = team used two days ago
        # - prev1 = team used one day ago
        #
        # Team values:
        # - 0, 1, 2 are real teams
        # - 3 is a sentinel meaning "no team yet"
        #
        # Example:
        # Before processing any day, the last two teams are both "none":
        #   (3, 3)
        #
        # After choosing team 1 on day 0:
        #   (3, 1)
        #
        # After then choosing team 0 on day 1:
        #   (1, 0)
        #
        # The DP dictionary maps:
        #   state -> minimum cost to reach that state after processing current days
        #
        # Because there are only 4 possible values for each part of the state,
        # there are at most 16 states total, which is tiny.
        NONE: int = 3
        dp: Dict[Tuple[int, int], int] = {(NONE, NONE): 0}

        # ------------------------------------------------------------
        # Process each day one by one
        # ------------------------------------------------------------
        for day in range(n):
            # This dictionary will store the best costs for states after assigning
            # a team on the current day.
            next_dp: Dict[Tuple[int, int], int] = {}

            # Examine every state that was reachable after the previous day.
            for (prev2, prev1), current_total_cost in dp.items():
                # Try assigning each of the 3 teams today.
                for team in range(3):
                    # --------------------------------------------------------
                    # Cooldown rule enforcement
                    # --------------------------------------------------------
                    # The team chosen today must be different from:
                    # - the team used yesterday (prev1), and
                    # - the team used two days ago (prev2)
                    #
                    # Sentinel NONE means that day does not exist yet, so it does
                    # not block any team choice.
                    if team == prev1 or team == prev2:
                        continue

                    # --------------------------------------------------------
                    # Compute the new total cost if we choose this team today
                    # --------------------------------------------------------
                    new_cost: int = current_total_cost + cost[day][team]

                    # --------------------------------------------------------
                    # Update the next state
                    # --------------------------------------------------------
                    # After choosing "team" today:
                    # - today's team becomes the new prev1
                    # - old prev1 shifts to become the new prev2
                    #
                    # So the new state is:
                    #   (prev1, team)
                    new_state: Tuple[int, int] = (prev1, team)

                    # If this state has not been seen yet today, store it.
                    # If it has been seen, keep only the cheaper total cost.
                    if new_state not in next_dp or new_cost < next_dp[new_state]:
                        next_dp[new_state] = new_cost

            # Move to the next day.
            dp = next_dp

            # ------------------------------------------------------------
            # Early failure check
            # ------------------------------------------------------------
            # If no states are reachable after some day, then no valid schedule
            # exists. In this specific problem with 3 teams, that should not happen
            # for valid input sizes, but this check is still correct and safe.
            if not dp:
                return -1

        # ------------------------------------------------------------
        # Final answer
        # ------------------------------------------------------------
        # After processing all days, every remaining DP state represents a valid
        # complete schedule. The answer is the minimum total cost among them.
        return min(dp.values()) if dp else -1

    def minCost(self, cost: List[List[int]]) -> int:
        """
        Convenience wrapper matching a shorter common interview-style method name.

        Args:
            cost: A list where cost[i][t] is the cost of assigning team t on day i.

        Returns:
            The minimum possible total cost, or -1 if no valid schedule exists.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        return self.min_maintenance_cost(cost)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    #
    # Important note:
    # The narrative in the prompt is inconsistent and claims the answer is 8,
    # but under the stated cooldown rule with exactly 3 teams, once the first
    # two different teams are chosen, the rest are forced.
    #
    # Let's verify all valid patterns for 4 days:
    # - 0,1,2,0 => 5 + 3 + 5 + 4 = 17
    # - 0,2,1,0 => 5 + 6 + 2 + 4 = 17
    # - 1,0,2,1 => 1 + 2 + 5 + 6 = 14
    # - 1,2,0,1 => 1 + 6 + 7 + 6 = 20
    # - 2,0,1,2 => 4 + 2 + 2 + 3 = 11
    # - 2,1,0,2 => 4 + 3 + 7 + 3 = 17
    #
    # So the true minimum is 11, not 8.
    cost1: List[List[int]] = [[5, 1, 4], [2, 3, 6], [7, 2, 5], [4, 6, 3]]
    result1: int = solution.min_maintenance_cost(cost1)
    print("Example 1 result:", result1)  # Correct result under the stated rules: 11

    # Example 2 from the prompt.
    # Day 0 choose team 1 (cost 2), day 1 choose team 0 (cost 1), total = 3.
    cost2: List[List[int]] = [[3, 2, 7], [5, 1, 4]]
    result2: int = solution.min_maintenance_cost(cost2)
    print("Example 2 result:", result2)  # Expected: 3

    # Additional small sanity checks for beginners.

    # Single day: just choose the cheapest team.
    cost3: List[List[int]] = [[8, 4, 6]]
    result3: int = solution.min_maintenance_cost(cost3)
    print("Single day result:", result3)  # Expected: 4

    # Three days: must use all three teams exactly once in some order.
    cost4: List[List[int]] = [[1, 10, 10], [10, 1, 10], [10, 10, 1]]
    result4: int = solution.min_maintenance_cost(cost4)
    print("Three day result:", result4)  # Expected: 3