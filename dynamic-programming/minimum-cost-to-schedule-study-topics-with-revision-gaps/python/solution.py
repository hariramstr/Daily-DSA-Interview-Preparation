"""
Title: Minimum Cost to Schedule Study Topics with Revision Gaps

Problem Description:
You are preparing a study plan for an exam over n days. On day i, you must study exactly
one topic chosen from m possible topics. The cost of studying topic j on day i is given
by costs[i][j]. However, repeatedly studying the same topic too soon is mentally exhausting,
so a topic can only be chosen again if at least gap[j] full days have passed since the last
day that same topic was studied.

More formally, if topic j is studied on day a and again on day b where a < b, then
b - a - 1 must be at least gap[j]. Equivalently, topic j cannot be used again within the
next gap[j] days after it is chosen.

Return the minimum total cost to complete all n days, or -1 if it is impossible to build
a valid schedule.

This is a dynamic programming problem because the best choice for the current day depends
on which topics were used recently and when they become available again. A correct solution
should efficiently explore valid schedules without brute-forcing all possible sequences.

Constraints:
- 1 <= n <= 100
- 1 <= m <= 8
- costs.length == n
- costs[i].length == m
- 1 <= costs[i][j] <= 10^4
- 0 <= gap[j] <= 7
"""

from typing import Dict, List, Tuple


class Solution:
    def _encode_state(self, remaining_cooldowns: List[int], base: int) -> int:
        """
        Convert a list of cooldown values into a single integer key.

        We use a mixed-radix style encoding where every topic's cooldown is one "digit"
        in base (max_gap + 1). This lets us store DP states compactly in dictionaries.

        Args:
            remaining_cooldowns: Current cooldown for each topic.
            base: Encoding base, chosen as max(gap) + 1.

        Returns:
            Encoded integer representing the cooldown vector.

        Time complexity:
            O(m)

        Space complexity:
            O(1) auxiliary
        """
        state_key: int = 0
        for value in remaining_cooldowns:
            state_key = state_key * base + value
        return state_key

    def _decode_state(self, state_key: int, m: int, base: int) -> List[int]:
        """
        Convert an encoded integer state back into the cooldown list.

        Args:
            state_key: Encoded state integer.
            m: Number of topics.
            base: Encoding base, chosen as max(gap) + 1.

        Returns:
            List of cooldown values for all topics.

        Time complexity:
            O(m)

        Space complexity:
            O(m)
        """
        remaining_cooldowns: List[int] = [0] * m
        for index in range(m - 1, -1, -1):
            remaining_cooldowns[index] = state_key % base
            state_key //= base
        return remaining_cooldowns

    def min_cost_schedule(self, costs: List[List[int]], gap: List[int]) -> int:
        """
        Compute the minimum total cost to schedule one topic per day while respecting
        topic-specific revision gaps.

        Dynamic programming idea:
        - A state is defined by the cooldown remaining for every topic at the start of a day.
        - cooldown[j] == 0 means topic j is available today.
        - cooldown[j] > 0 means topic j is blocked for that many more days.
        - When we choose a topic:
            1. Every positive cooldown decreases by 1 because one day passes.
            2. The chosen topic gets reset to gap[topic], meaning it is blocked for the
               next gap[topic] days.

        We process days from left to right and keep only the best cost for each reachable state.

        Args:
            costs: costs[i][j] is the cost of studying topic j on day i.
            gap: gap[j] is the number of full days topic j must wait before reuse.

        Returns:
            Minimum total cost, or -1 if no valid schedule exists.

        Time complexity:
            O(n * S * m^2) in the worst case if decoding every state each day,
            where S is the number of reachable cooldown states.
            Since m <= 8 and gap[j] <= 7, this is practical.

        Space complexity:
            O(S), where S is the number of reachable states stored for one DP layer.
        """
        n: int = len(costs)
        if n == 0:
            return 0

        m: int = len(costs[0])
        if m == 0:
            return -1

        # The largest possible cooldown value is at most max(gap).
        # We use base = max_gap + 1 so every cooldown value fits in one digit.
        max_gap: int = max(gap) if gap else 0
        base: int = max_gap + 1

        # Initial state before day 0:
        # all topics are available, so every cooldown is 0.
        initial_cooldowns: List[int] = [0] * m
        initial_state: int = self._encode_state(initial_cooldowns, base)

        # DP dictionary:
        # key   -> encoded cooldown state at the START of the current day
        # value -> minimum total cost to reach that state
        dp: Dict[int, int] = {initial_state: 0}

        # Process each day one by one.
        for day in range(n):
            # next_dp will store the best costs for states at the start of the next day.
            next_dp: Dict[int, int] = {}

            # Try every currently reachable state.
            for state_key, current_cost in dp.items():
                # Decode the state so we can inspect which topics are available.
                current_cooldowns: List[int] = self._decode_state(state_key, m, base)

                # Try choosing each topic for today's study.
                for topic in range(m):
                    # A topic can be chosen today only if its cooldown is 0.
                    if current_cooldowns[topic] != 0:
                        continue

                    # Build the cooldown state for the next day.
                    # First, one day passes, so every positive cooldown decreases by 1.
                    next_cooldowns: List[int] = [0] * m
                    for other_topic in range(m):
                        if current_cooldowns[other_topic] > 0:
                            next_cooldowns[other_topic] = current_cooldowns[other_topic] - 1
                        else:
                            next_cooldowns[other_topic] = 0

                    # Now apply the effect of choosing 'topic' today:
                    # it becomes unavailable for the next gap[topic] days.
                    next_cooldowns[topic] = gap[topic]

                    # Encode the next-day state so it can be used as a dictionary key.
                    next_state_key: int = self._encode_state(next_cooldowns, base)

                    # Total cost if we choose this topic today.
                    new_cost: int = current_cost + costs[day][topic]

                    # Standard DP relaxation:
                    # keep only the minimum cost for each next state.
                    if next_state_key not in next_dp or new_cost < next_dp[next_state_key]:
                        next_dp[next_state_key] = new_cost

            # Move to the next day.
            dp = next_dp

            # If no states are reachable after this day, scheduling is impossible.
            if not dp:
                return -1

        # After processing all days, any remaining state is acceptable.
        # We simply want the minimum total cost among all reachable end states.
        return min(dp.values()) if dp else -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    # Note:
    # The textual explanation in the prompt contains an inconsistency.
    # For costs = [[3,8],[5,2],[6,4],[1,7]] and gap = [1,0],
    # the schedule topic 0, topic 1, topic 0, topic 0 is valid:
    # - topic 0 appears on days 0, 2, 3
    # - gap[0] = 1 means topic 0 cannot be repeated on consecutive days
    # - day 0 to day 2 has one full day in between, so valid
    # - day 2 to day 3 is consecutive, so that pair would be invalid
    # Therefore we must let the algorithm determine the true minimum.
    #
    # Valid schedules include:
    # - 0,1,0,1 => 3 + 2 + 6 + 7 = 18
    # - 0,1,1,0 => 3 + 2 + 4 + 1 = 10
    # The algorithm correctly computes the minimum valid total cost.
    costs1: List[List[int]] = [[3, 8], [5, 2], [6, 4], [1, 7]]
    gap1: List[int] = [1, 0]
    result1: int = solution.min_cost_schedule(costs1, gap1)
    print("Example 1 Output:", result1)

    # Example 2 from the prompt.
    costs2: List[List[int]] = [[4, 1], [2, 3], [5, 6]]
    gap2: List[int] = [2, 2]
    result2: int = solution.min_cost_schedule(costs2, gap2)
    print("Example 2 Output:", result2)

    # Additional small sanity check.
    costs3: List[List[int]] = [[1, 10], [10, 1], [1, 10]]
    gap3: List[int] = [1, 1]
    result3: int = solution.min_cost_schedule(costs3, gap3)
    print("Additional Test Output:", result3)