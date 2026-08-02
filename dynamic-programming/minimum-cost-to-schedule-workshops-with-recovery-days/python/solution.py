"""
Minimum Cost to Schedule Workshops with Recovery Days

Problem Description:
You are organizing a training program over N calendar days. On day i, you may choose
to run a workshop and earn value[i] participants, but running a workshop also
increases fatigue. After holding a workshop on day i, you must leave the next
cooldown[i] days empty as recovery days before scheduling another workshop. In other
words, if you run a workshop on day i, the next workshop can be scheduled no earlier
than day i + cooldown[i] + 1.

Each day also has a fixed operating cost cost[i] if you choose to run the workshop
that day. Your goal is to reach at least target total participants while minimizing
the total operating cost. You may skip any days, and you are not required to use all
days. If it is impossible to reach at least target participants, return -1.

Constraints:
- 1 <= N <= 200
- 1 <= target <= 5000
- 1 <= value[i] <= 100
- 1 <= cost[i] <= 1000
- 0 <= cooldown[i] < N

We solve this with dynamic programming.

Key idea:
Define a DP state by:
- current day index
- accumulated participants so far, capped at target

At each day, we have two choices:
1. Skip the day:
   move to the next day with the same accumulated participants
2. Run a workshop on this day:
   add value[i] participants (capped at target),
   pay cost[i],
   and jump to day i + cooldown[i] + 1

Because reaching more than target is equivalent to reaching target, we cap the
participant count at target to keep the state space manageable.
"""

from typing import List


class Solution:
    def min_cost_to_schedule(
        self,
        value: List[int],
        cost: List[int],
        cooldown: List[int],
        target: int,
    ) -> int:
        """
        Compute the minimum total operating cost needed to reach at least target
        participants while respecting cooldown constraints.

        Args:
            value: Participants gained by running a workshop on each day.
            cost: Operating cost of running a workshop on each day.
            cooldown: Number of recovery days that must be skipped after each workshop.
            target: Required minimum total participants.

        Returns:
            The minimum total cost to reach at least target participants, or -1 if
            it is impossible.

        Time complexity:
            O(N * target), where N is the number of days.

        Space complexity:
            O(N * target) for the DP table.
        """
        n: int = len(value)

        # A very large number used to represent an impossible or not-yet-computed state.
        # We choose a number much larger than any possible valid answer.
        inf: int = 10**15

        # dp[day][participants] means:
        # "the minimum cost needed to achieve exactly 'participants' participants
        #  (capped at target) starting from calendar day 'day' onward"
        #
        # Why "starting from day onward"?
        # Because this makes cooldown jumps very natural:
        # if we run a workshop on day i, we simply jump to the next allowed day.
        #
        # Table dimensions:
        # - day ranges from 0 to n
        # - participants ranges from 0 to target
        #
        # We include day == n as a base row meaning "no days left".
        dp: List[List[int]] = [[inf] * (target + 1) for _ in range(n + 1)]

        # Base case initialization:
        # If we are already at or above target participants, the remaining cost is 0,
        # even if there are no days left.
        #
        # Since we cap participants at target, the only "already enough" state is
        # participants == target.
        dp[n][target] = 0

        # If there are no days left and we have fewer than target participants,
        # it is impossible to reach the target, so those states remain inf.

        # We fill the DP table from the last day backward to day 0.
        # Backward filling works because each state depends on future days only.
        for day in range(n - 1, -1, -1):
            # For every possible accumulated participant count so far...
            for participants in range(target + 1):
                # If we have already reached the target, we need no more cost.
                # This is an important optimization and also logically correct.
                if participants == target:
                    dp[day][participants] = 0
                    continue

                # ------------------------------------------------------------
                # Option 1: Skip this day
                # ------------------------------------------------------------
                # If we do not run a workshop today, we simply move to the next day
                # with the same participant count.
                skip_cost: int = dp[day + 1][participants]

                # ------------------------------------------------------------
                # Option 2: Run a workshop on this day
                # ------------------------------------------------------------
                # Running today gives us value[day] more participants.
                # Since any amount above target is equivalent to target, we cap it.
                new_participants: int = min(target, participants + value[day])

                # After running a workshop today, the next allowed day is:
                # day + cooldown[day] + 1
                next_day: int = day + cooldown[day] + 1

                # If the jump goes beyond the last day, we treat it as day n,
                # which is our "no days left" base row.
                if next_day > n:
                    next_day = n

                # The total cost for choosing today's workshop is:
                # today's operating cost + minimum future cost from next_day onward
                run_cost: int = cost[day] + dp[next_day][new_participants]

                # We choose the cheaper of the two valid options.
                dp[day][participants] = min(skip_cost, run_cost)

        # The answer is the minimum cost starting from day 0 with 0 participants.
        answer: int = dp[0][0]

        # If the answer is still "infinite", then no valid schedule can reach target.
        return -1 if answer >= inf else answer

    def solve(
        self,
        value: List[int],
        cost: List[int],
        cooldown: List[int],
        target: int,
    ) -> int:
        """
        Wrapper method that calls the main DP algorithm.

        Args:
            value: Participants gained by running a workshop on each day.
            cost: Operating cost of running a workshop on each day.
            cooldown: Required recovery days after each workshop.
            target: Required minimum total participants.

        Returns:
            Minimum total cost, or -1 if impossible.

        Time complexity:
            O(N * target)

        Space complexity:
            O(N * target)
        """
        return self.min_cost_to_schedule(value, cost, cooldown, target)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # Important note:
    # The original problem statement's explanation contains contradictions.
    # Let's verify the schedule carefully:
    #
    # value = [6, 4, 7, 3]
    # cost = [5, 2, 6, 2]
    # cooldown = [1, 0, 2, 0]
    # target = 10
    #
    # Valid choices:
    # - Day 1 and Day 2 are valid because cooldown[1] = 0, so day 2 is allowed.
    #   Participants = 4 + 7 = 11, Cost = 2 + 6 = 8
    # - Day 0 and Day 2 are also valid because cooldown[0] = 1, so only day 1 is blocked.
    #   Participants = 6 + 7 = 13, Cost = 5 + 6 = 11
    #
    # Therefore the minimum valid cost is 8.
    value1 = [6, 4, 7, 3]
    cost1 = [5, 2, 6, 2]
    cooldown1 = [1, 0, 2, 0]
    target1 = 10
    result1 = solution.solve(value1, cost1, cooldown1, target1)
    print(result1)  # Expected: 8

    # Example 2
    # Again, the statement's sample output says 13, but its own explanation says impossible.
    # Let's verify:
    #
    # value = [5, 8, 4]
    # cost = [4, 9, 3]
    # cooldown = [2, 1, 0]
    # target = 13
    #
    # - Day 0 blocks days 1 and 2, so only 5 participants total if chosen.
    # - Day 1 blocks day 2, so day 1 + day 2 is invalid.
    # - Day 0 + anything else is impossible due to cooldown and array end.
    # - Day 1 alone gives 8, day 2 alone gives 4.
    #
    # So target 13 is impossible, and the correct answer is -1.
    value2 = [5, 8, 4]
    cost2 = [4, 9, 3]
    cooldown2 = [2, 1, 0]
    target2 = 13
    result2 = solution.solve(value2, cost2, cooldown2, target2)
    print(result2)  # Expected: -1

    # Additional quick sanity check:
    # If target were 8 for example 2, choosing day 1 alone works with cost 9.
    target3 = 8
    result3 = solution.solve(value2, cost2, cooldown2, target3)
    print(result3)  # Expected: 9