"""
Title: Maximum Profit from Non-Overlapping Contract Chains

Problem Description:
A consulting company is evaluating a sequence of project contracts over the next N days.
On day i, you may choose to start at most one contract that lasts for duration[i] days
and yields profit[i] if completed. If you start a contract on day i, it occupies days
i through i + duration[i] - 1, and you cannot start another contract during that time.
You may also skip any day.

Additional business rule:
Contracts belong to client groups, represented by group[i]. Whenever you accept two
consecutive chosen contracts in your schedule, their client groups must be different.
Skipped days do not reset this rule.

Return the maximum total profit that can be earned.

You are given three arrays of length N:
- duration[i]
- profit[i]
- group[i]

Contract i is available only if started exactly on day i.
A contract that ends after day N cannot be taken.

Constraints:
- 1 <= N <= 2 * 10^5
- 1 <= duration[i] <= N
- 1 <= profit[i] <= 10^9
- 1 <= group[i] <= 2 * 10^5
- Sum of all values fits in 64-bit signed integer
"""

from typing import Dict, List, Tuple


class Solution:
    def max_profit(
        self,
        duration: List[int],
        profit: List[int],
        group: List[int],
    ) -> int:
        """
        Compute the maximum total profit from a valid chain of non-overlapping contracts
        where consecutive chosen contracts must belong to different groups.

        Args:
            duration: duration[i] is the number of days contract i occupies.
            profit: profit[i] is the profit earned by completing contract i.
            group: group[i] is the client group of contract i.

        Returns:
            The maximum achievable total profit.

        Time complexity:
            O(N), because each day is processed once and each contract contributes
            a constant amount of work to the event list and DP updates.

        Space complexity:
            O(N + G) in the worst case, where N is the number of days/contracts and
            G is the number of distinct groups that appear in valid states.
        """
        n: int = len(duration)

        # ---------------------------------------------------------------------
        # We will process the timeline day by day.
        #
        # Core DP idea:
        # Let "ready" mean schedules whose last chosen contract has already finished,
        # so we are free to start a new contract today.
        #
        # Among all ready schedules, we need to know:
        #   1) the best total profit overall
        #   2) for each group g, the best total profit among schedules whose last
        #      chosen contract belongs to group g
        #
        # Why do we need both?
        # If we want to start a new contract of group g today, then the previous
        # chosen contract (if any) must have a different group.
        #
        # So the best predecessor value is:
        #   best ready schedule whose last group != g
        #
        # We can answer that quickly if we maintain the top two ready states:
        #   - best overall ready state: (value, group)
        #   - second best ready state: (value, group)
        #
        # Then for a new contract of group g:
        #   - if best overall uses a different group, use it
        #   - otherwise use second best
        #
        # We also need to allow starting the very first chosen contract with no
        # previous contract. That corresponds to predecessor value 0.
        #
        # Because contracts finish in the future, when we choose a contract on day i,
        # its resulting DP value becomes "ready" only on day end_day = i + duration[i].
        # We store such future activations in an event bucket.
        # ---------------------------------------------------------------------

        # events[day] contains a list of pairs:
        #   (group_of_last_contract, total_profit_of_schedule)
        #
        # Interpretation:
        #   On this day, these schedules become ready for extension because their
        #   last chosen contract finished exactly at the end of the previous day.
        #
        # If a contract starts at day i and lasts d days, it occupies days
        # [i, i + d - 1], so the next contract can start at day i + d.
        events: List[List[Tuple[int, int]]] = [[] for _ in range(n + 1)]

        # best_by_group[g] = best ready total profit among schedules whose last
        # chosen contract belongs to group g.
        best_by_group: Dict[int, int] = {}

        # We maintain the top two ready states across all groups.
        #
        # Each is represented as:
        #   (profit_value, group_id)
        #
        # Initially, there is no chosen contract yet, so there is no "last group".
        # We model the empty schedule separately via predecessor value 0.
        best1_value: int = 0
        best1_group: int = -1

        # second best ready state among non-empty schedules / tracked groups
        best2_value: int = 0
        best2_group: int = -1

        # ---------------------------------------------------------------------
        # Helper function:
        # Update the global top-two structure after best_by_group[g] improves.
        #
        # This function is carefully written so that:
        # - each group's best value is tracked
        # - best1 and best2 always represent the top two values from distinct groups
        #
        # We only call this when a group's value has increased.
        # ---------------------------------------------------------------------
        def push_group_value(g: int, value: int) -> None:
            """
            Update the top-two ready states after group g obtains a new best value.

            Args:
                g: Group identifier.
                value: New best ready profit for this group.

            Returns:
                None

            Time complexity:
                O(1)

            Space complexity:
                O(1)
            """
            nonlocal best1_value, best1_group, best2_value, best2_group

            # If this group is already the current best1 group, we only need to
            # refresh best1_value. Since values only increase, best1 remains valid.
            if g == best1_group:
                if value > best1_value:
                    best1_value = value
                return

            # If this group is already the current best2 group, update it and
            # possibly swap with best1 if it becomes larger.
            if g == best2_group:
                if value > best2_value:
                    best2_value = value
                    if best2_value > best1_value:
                        best1_value, best2_value = best2_value, best1_value
                        best1_group, best2_group = best2_group, best1_group
                return

            # Otherwise, this group is not currently in the top two.
            # Insert it if its value is large enough.
            if value > best1_value:
                best2_value, best2_group = best1_value, best1_group
                best1_value, best1_group = value, g
            elif value > best2_value:
                best2_value, best2_group = value, g

        # ---------------------------------------------------------------------
        # Process each day from left to right.
        #
        # At the start of day i:
        #   1) activate all schedules that became ready today
        #   2) decide whether to start contract i
        #
        # This order is important:
        # If a previous contract ends exactly before day i, then starting a new
        # contract on day i is allowed, so those finished schedules must already
        # be available as predecessors.
        # ---------------------------------------------------------------------
        for day in range(n):
            # -------------------------------------------------------------
            # Step 1: activate all schedules that become ready today.
            #
            # Each activated state says:
            #   "There exists a valid schedule ending with group g and total
            #    profit val, and that schedule is now free to be extended."
            #
            # We merge it into best_by_group and update the top-two summary.
            # -------------------------------------------------------------
            for g, val in events[day]:
                previous_best: int = best_by_group.get(g, -1)
                if val > previous_best:
                    best_by_group[g] = val
                    push_group_value(g, val)

            # -------------------------------------------------------------
            # Step 2: consider starting the contract available exactly today.
            #
            # If it would end after day N, it is invalid and must be skipped.
            # -------------------------------------------------------------
            end_day: int = day + duration[day]
            if end_day > n:
                continue

            current_group: int = group[day]
            current_profit: int = profit[day]

            # -------------------------------------------------------------
            # Find the best predecessor schedule whose last group is different
            # from current_group.
            #
            # There are two possibilities:
            #   A) start fresh with no previous contract -> predecessor profit 0
            #   B) extend the best ready schedule with last group != current_group
            #
            # We use the top-two structure for O(1) retrieval.
            # -------------------------------------------------------------
            predecessor_best: int = 0

            # If the best ready schedule ends with a different group, it is valid.
            if best1_group != current_group:
                predecessor_best = max(predecessor_best, best1_value)
            else:
                # Otherwise, the best ready schedule has the same group and cannot
                # be followed by this contract, so we try the second-best one.
                predecessor_best = max(predecessor_best, best2_value)

            # Total profit if we choose today's contract.
            new_total: int = predecessor_best + current_profit

            # -------------------------------------------------------------
            # This newly formed schedule will become ready on end_day.
            #
            # We do not activate it immediately because the contract occupies
            # its days and cannot be followed until it finishes.
            # -------------------------------------------------------------
            events[end_day].append((current_group, new_total))

        # ---------------------------------------------------------------------
        # After processing all start days, some schedules may become ready exactly
        # on day n. They still count as completed schedules and must be merged
        # before we compute the final answer.
        # ---------------------------------------------------------------------
        for g, val in events[n]:
            previous_best = best_by_group.get(g, -1)
            if val > previous_best:
                best_by_group[g] = val
                push_group_value(g, val)

        # The answer is the best ready schedule profit.
        # We also compare with 0 to support the conceptual possibility of taking
        # no contracts, although profits are positive so best1_value will already
        # dominate whenever any valid contract exists.
        return max(0, best1_value)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt text contains contradictory explanation.
    # The correct optimal answer for these arrays is 120:
    # - Take contract 0: duration 2, profit 50, group 1, occupies days 0-1
    # - Then take contract 2: duration 2, profit 40, group 2, occupies days 2-3
    # Total = 90
    #
    # But an even better valid choice is:
    # - Take contract 1: duration 1, profit 10, group 1, occupies day 1
    # - Then take contract 2: duration 2, profit 40, group 2, occupies days 2-3
    # Total = 50
    #
    # Also:
    # - Take contract 0 and then contract 3 is invalid because groups 1 -> 1
    #
    # The true best is actually:
    # - Take contract 0 (50, group 1), then contract 2 (40, group 2) = 90
    # There is no valid way to also take contract 3 after contract 2 because
    # contract 2 occupies days 2-3.
    #
    # Therefore the mathematically correct answer for this input is 90.
    duration1 = [2, 1, 2, 1]
    profit1 = [50, 10, 40, 70]
    group1 = [1, 1, 2, 1]
    result1 = solution.max_profit(duration1, profit1, group1)
    print(result1)  # Expected correct result: 90

    # Example 2 from the prompt text also contains contradictory explanation.
    # For the given arrays:
    # - contract 4 starts on day 4 and lasts 2 days, so it ends after N=5 and is invalid
    #   because it would occupy days 4 and 5, but valid days are only 0..4.
    #
    # Valid best schedule:
    # - contract 0: group 1, profit 8, day 0
    # - contract 1: group 2, profit 20, days 1-2
    # - contract 3: group 3, profit 15, day 3
    # Total = 43
    #
    # Therefore the mathematically correct answer for this input is 43.
    duration2 = [1, 2, 1, 1, 2]
    profit2 = [8, 20, 7, 15, 30]
    group2 = [1, 2, 1, 3, 2]
    result2 = solution.max_profit(duration2, profit2, group2)
    print(result2)  # Expected correct result: 43