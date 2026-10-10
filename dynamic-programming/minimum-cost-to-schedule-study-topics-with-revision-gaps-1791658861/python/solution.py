"""
Title: Minimum Cost to Schedule Study Topics With Revision Gaps

Problem Description:
You are preparing for an exam over the next n days. On day i, you may either study
exactly one topic or skip the day. There are m topics, numbered from 0 to m - 1.
Studying topic j on day i gives you a learning cost cost[i][j]. Lower cost means that
topic is easier to study on that day because of your energy, available notes, or class
schedule.

However, to retain information properly, each topic j has a required revision gap
gap[j]. If you study topic j on some day d, then the next time you study the same topic
must be at least gap[j] + 1 days later. In other words, if you last studied topic j on
day p, then you may study it again on day d only if d - p > gap[j].

You are also given an array need where need[j] is the exact number of times topic j
must be studied by the end of day n - 1. You may skip any number of days, but all
required study sessions must be completed. Return the minimum total learning cost, or
-1 if it is impossible.

Constraints:
- 1 <= n <= 30
- 1 <= m <= 5
- 0 <= need[j] <= n
- 0 <= gap[j] <= n
- 1 <= cost[i][j] <= 10^4
- Sum of need[j] over all topics is at most n
"""

from typing import Dict, List, Tuple


class Solution:
    def _can_finish_from_state(
        self,
        day: int,
        counts: Tuple[int, ...],
        last_days: Tuple[int, ...],
        n: int,
        m: int,
        need: List[int],
        gap: List[int],
        memo: Dict[Tuple[int, Tuple[int, ...], Tuple[int, ...]], bool],
    ) -> bool:
        """
        Check whether it is still possible to complete all remaining required study sessions
        from the current state, ignoring costs and considering only feasibility.

        This helper is used as a pruning step inside the main dynamic programming recursion.
        If a state can never lead to a valid full schedule, we stop exploring it early.

        Args:
            day: Current day index we are deciding for.
            counts: Tuple where counts[j] is how many times topic j has already been studied.
            last_days: Tuple where last_days[j] is the most recent day topic j was studied,
                or a large negative sentinel if it has never been studied.
            n: Total number of days.
            m: Number of topics.
            need: Required number of study sessions for each topic.
            gap: Required gap for each topic.

        Returns:
            True if there exists at least one valid completion from this state, otherwise False.

        Time complexity:
            Exponential in the number of reachable states in the worst case, but memoized.
            Since n <= 30 and m <= 5, this is practical.

        Space complexity:
            O(number of memoized states)
        """
        state: Tuple[int, Tuple[int, ...], Tuple[int, ...]] = (day, counts, last_days)
        if state in memo:
            return memo[state]

        # If we have processed all days, the schedule is feasible only if every topic
        # has been studied exactly the required number of times.
        if day == n:
            result: bool = all(counts[j] == need[j] for j in range(m))
            memo[state] = result
            return result

        # Quick necessary condition:
        # If the total number of remaining required sessions is greater than the number
        # of days left, then completion is impossible no matter what we do.
        remaining_sessions: int = sum(need[j] - counts[j] for j in range(m))
        days_left: int = n - day
        if remaining_sessions > days_left:
            memo[state] = False
            return False

        # Stronger per-topic necessary condition:
        # For each topic, compute the earliest possible days we could place its remaining
        # sessions, respecting its own gap and its most recent study day.
        #
        # If even the earliest possible placement would exceed the schedule horizon,
        # then this state is impossible.
        for j in range(m):
            remaining_for_topic: int = need[j] - counts[j]
            if remaining_for_topic <= 0:
                continue

            # Earliest day we can next study topic j:
            # - We cannot schedule before the current day.
            # - We also must respect the gap from the last time topic j was studied.
            earliest_next: int = max(day, last_days[j] + gap[j] + 1)

            # If we need k remaining sessions, the earliest possible sequence is:
            # earliest_next,
            # earliest_next + (gap[j] + 1),
            # earliest_next + 2 * (gap[j] + 1), ...
            #
            # So the last of those k sessions would be at:
            # earliest_next + (k - 1) * (gap[j] + 1)
            last_needed_day: int = earliest_next + (remaining_for_topic - 1) * (gap[j] + 1)

            # If that day is outside the schedule, then impossible.
            if last_needed_day >= n:
                memo[state] = False
                return False

        # Option 1: skip this day.
        # Skipping is always allowed, so if any valid completion exists after skipping,
        # the state is feasible.
        if self._can_finish_from_state(day + 1, counts, last_days, n, m, need, gap, memo):
            memo[state] = True
            return True

        # Option 2: study one topic today, if allowed.
        for j in range(m):
            # We can only study topic j if it still needs more sessions.
            if counts[j] >= need[j]:
                continue

            # Gap validity:
            # If last_days[j] is the previous study day, then today is valid only when
            # day - last_days[j] > gap[j].
            if day - last_days[j] <= gap[j]:
                continue

            new_counts: List[int] = list(counts)
            new_counts[j] += 1

            new_last_days: List[int] = list(last_days)
            new_last_days[j] = day

            if self._can_finish_from_state(
                day + 1,
                tuple(new_counts),
                tuple(new_last_days),
                n,
                m,
                need,
                gap,
                memo,
            ):
                memo[state] = True
                return True

        memo[state] = False
        return False

    def min_cost_schedule(
        self,
        n: int,
        m: int,
        cost: List[List[int]],
        need: List[int],
        gap: List[int],
    ) -> int:
        """
        Compute the minimum total learning cost to schedule all required study sessions
        while respecting per-topic revision gaps.

        The algorithm uses top-down dynamic programming with memoization.
        A state is defined by:
        - current day
        - how many times each topic has already been studied
        - the last day each topic was studied

        From each state, we try:
        1. Skipping the current day
        2. Studying any valid topic on the current day

        We also use a feasibility-pruning helper to avoid exploring states that can no
        longer possibly finish all required sessions.

        Args:
            n: Number of days.
            m: Number of topics.
            cost: cost[i][j] is the cost of studying topic j on day i.
            need: need[j] is the exact number of times topic j must be studied.
            gap: gap[j] is the required number of days between consecutive studies of topic j.

        Returns:
            The minimum total cost, or -1 if no valid schedule exists.

        Time complexity:
            Exponential in the number of reachable states in the worst case, but heavily
            reduced by memoization and pruning. Practical for n <= 30 and m <= 5.

        Space complexity:
            O(number of memoized states)
        """
        # A very negative day value means "this topic has never been studied".
        # We choose a value much smaller than any real day index so that the first study
        # of any topic is always allowed.
        never_studied_day: int = -10**9

        # Initial state:
        # - day 0
        # - no topic studied yet
        # - last study day for every topic is the sentinel value
        initial_counts: Tuple[int, ...] = tuple(0 for _ in range(m))
        initial_last_days: Tuple[int, ...] = tuple(never_studied_day for _ in range(m))

        # Memo table for the main DP:
        # key   -> (day, counts_tuple, last_days_tuple)
        # value -> minimum additional cost from this state to finish all requirements
        dp_memo: Dict[Tuple[int, Tuple[int, ...], Tuple[int, ...]], int] = {}

        # Separate memo table for feasibility pruning.
        feasible_memo: Dict[Tuple[int, Tuple[int, ...], Tuple[int, ...]], bool] = {}

        # Infinity-like large value used to represent impossible states in minimization.
        inf: int = 10**18

        def dp(day: int, counts: Tuple[int, ...], last_days: Tuple[int, ...]) -> int:
            """
            Recursive memoized DP that returns the minimum additional cost needed from
            the current state to complete the schedule.

            Args:
                day: Current day index.
                counts: How many times each topic has been studied so far.
                last_days: Most recent study day for each topic.

            Returns:
                Minimum remaining cost from this state, or a very large number if impossible.

            Time complexity:
                Depends on the number of reachable states; each state considers at most
                m + 1 transitions.

            Space complexity:
                O(number of memoized states)
            """
            state: Tuple[int, Tuple[int, ...], Tuple[int, ...]] = (day, counts, last_days)
            if state in dp_memo:
                return dp_memo[state]

            # Before doing any expensive exploration, check whether this state is even
            # capable of finishing. If not, return impossible immediately.
            if not self._can_finish_from_state(
                day, counts, last_days, n, m, need, gap, feasible_memo
            ):
                dp_memo[state] = inf
                return inf

            # Base case:
            # If all days are processed, the schedule is valid only if all requirements
            # are exactly met. The feasibility helper already guarantees this for reachable
            # states, but we keep the explicit check for clarity and safety.
            if day == n:
                result: int = 0 if all(counts[j] == need[j] for j in range(m)) else inf
                dp_memo[state] = result
                return result

            # Start with the option of skipping the current day.
            # Skipping adds no cost today.
            best: int = dp(day + 1, counts, last_days)

            # Try studying each topic today if:
            # 1. It still needs more sessions
            # 2. The gap constraint allows it
            for j in range(m):
                if counts[j] >= need[j]:
                    continue

                if day - last_days[j] <= gap[j]:
                    continue

                # Build the next state after studying topic j today.
                new_counts: List[int] = list(counts)
                new_counts[j] += 1

                new_last_days: List[int] = list(last_days)
                new_last_days[j] = day

                # Today's cost plus the optimal future cost.
                candidate: int = cost[day][j] + dp(
                    day + 1,
                    tuple(new_counts),
                    tuple(new_last_days),
                )

                if candidate < best:
                    best = candidate

            dp_memo[state] = best
            return best

        answer: int = dp(0, initial_counts, initial_last_days)
        return -1 if answer >= inf else answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1: int = 5
    m1: int = 2
    cost1: List[List[int]] = [
        [3, 8],
        [2, 5],
        [4, 1],
        [6, 3],
        [2, 7],
    ]
    need1: List[int] = [2, 1]
    gap1: List[int] = [1, 0]
    result1: int = solution.min_cost_schedule(n1, m1, cost1, need1, gap1)
    print(result1)  # Expected: 5

    # Example 2
    n2: int = 4
    m2: int = 2
    cost2: List[List[int]] = [
        [5, 2],
        [4, 3],
        [3, 6],
        [2, 1],
    ]
    need2: List[int] = [2, 2]
    gap2: List[int] = [2, 1]
    result2: int = solution.min_cost_schedule(n2, m2, cost2, need2, gap2)
    print(result2)  # Expected: -1