"""
Title: Maximum Uniform Delay for Train Departures

Problem Description:
A railway operator has scheduled n trains to depart in nondecreasing order of planned times,
given by an integer array departures where departures[i] is the planned departure minute of
the i-th train. Due to maintenance, each train may be delayed by any integer number of minutes
from 0 up to maxDelay, independently of the others. After delays are chosen, the actual
departure times must still be strictly increasing, and the gap between any two consecutive
actual departures must be at least gap minutes.

Your task is to compute the maximum integer value of gap such that it is possible to assign
delays to all trains while respecting the delay limit maxDelay.

Formally, choose integers actual[i] such that:
1. departures[i] <= actual[i] <= departures[i] + maxDelay
2. actual[i] < actual[i + 1] for all valid i
3. actual[i + 1] - actual[i] >= gap for all valid i

Return the largest feasible gap.

This is an optimization problem where the answer is not constructed directly. Instead, you
must determine whether a candidate gap is feasible and use that to search for the maximum
valid answer efficiently.
"""

from typing import List


class Solution:
    def max_uniform_gap(self, departures: List[int], maxDelay: int) -> int:
        """
        Compute the maximum feasible minimum gap between consecutive actual departures.

        The method uses binary search on the answer. For a candidate gap, it checks
        feasibility greedily by placing each train as early as possible while still
        respecting:
        - its own allowed interval [departures[i], departures[i] + maxDelay]
        - the required minimum gap from the previous chosen actual departure

        Args:
            departures: Sorted list of planned departure times.
            maxDelay: Maximum allowed delay for each train.

        Returns:
            The largest integer gap that can be achieved.

        Time complexity:
            O(n log R), where n is the number of trains and R is the search range
            of possible answers.

        Space complexity:
            O(1), excluding input storage.
        """
        n: int = len(departures)

        # We binary search the answer "gap".
        #
        # Why binary search works:
        # - If a certain gap g is feasible, then every smaller gap is also feasible.
        # - If a certain gap g is not feasible, then every larger gap is also not feasible.
        #
        # This monotonic property is exactly what binary search needs.
        #
        # Lower bound:
        # - 0 is always a safe lower bound for the search space.
        #
        # Upper bound:
        # - A loose but valid upper bound is:
        #       (latest possible last train) - (earliest possible first train)
        #   because the minimum gap between consecutive trains can never exceed the total
        #   span available across the schedule.
        # - latest possible last train = departures[-1] + maxDelay
        # - earliest possible first train = departures[0]
        high: int = departures[-1] + maxDelay - departures[0]
        low: int = 0

        # Standard "find maximum feasible value" binary search.
        while low < high:
            # We bias mid upward so that when low and high are adjacent,
            # the loop still makes progress toward the maximum feasible answer.
            mid: int = (low + high + 1) // 2

            if self._can_achieve_gap(departures, maxDelay, mid):
                # If mid is feasible, try to go larger.
                low = mid
            else:
                # If mid is not feasible, the answer must be smaller.
                high = mid - 1

        return low

    def _can_achieve_gap(self, departures: List[int], maxDelay: int, gap: int) -> bool:
        """
        Check whether a given minimum gap can be achieved.

        The greedy strategy is:
        - Place the first train at the earliest possible time: departures[0].
        - For each next train, place it at the earliest time that satisfies both:
          1. It is not earlier than its planned departure.
          2. It is at least `gap` after the previous actual departure.
        - If this earliest valid placement exceeds departures[i] + maxDelay,
          then the candidate gap is impossible.

        Why this greedy choice is correct:
        - Placing a train earlier never hurts future trains; it only leaves more room.
        - Therefore, if even the earliest valid placement fails, no later placement
          could help.

        Args:
            departures: Sorted list of planned departure times.
            maxDelay: Maximum allowed delay for each train.
            gap: Candidate minimum gap to validate.

        Returns:
            True if the gap is feasible, otherwise False.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        # We store the actual departure time chosen for the previous train.
        #
        # Start with the earliest possible time for the first train.
        # This is the best choice because it maximizes flexibility for all later trains.
        previous_actual: int = departures[0]

        # Process each remaining train in order.
        for i in range(1, len(departures)):
            # The current train cannot leave before its planned departure time.
            earliest_by_schedule: int = departures[i]

            # To maintain the required minimum gap, it also cannot leave before:
            # previous_actual + gap
            earliest_by_gap: int = previous_actual + gap

            # Therefore, the earliest valid actual departure for this train is the
            # maximum of those two lower bounds.
            current_actual: int = max(earliest_by_schedule, earliest_by_gap)

            # The latest allowed actual departure is limited by maxDelay.
            latest_allowed: int = departures[i] + maxDelay

            # If even the earliest valid choice is too late, then this gap is impossible.
            if current_actual > latest_allowed:
                return False

            # Otherwise, commit to the earliest feasible choice.
            # This greedy decision is optimal for preserving room for future trains.
            previous_actual = current_actual

        # If every train was placed successfully, the candidate gap is feasible.
        return True


if __name__ == "__main__":
    solution = Solution()

    departures1: List[int] = [2, 4, 7]
    max_delay1: int = 3
    result1: int = solution.max_uniform_gap(departures1, max_delay1)
    print("Example 1:")
    print("departures =", departures1)
    print("maxDelay =", max_delay1)
    print("Maximum feasible gap =", result1)
    print()

    departures2: List[int] = [1, 1, 1, 1]
    max_delay2: int = 5
    result2: int = solution.max_uniform_gap(departures2, max_delay2)
    print("Example 2:")
    print("departures =", departures2)
    print("maxDelay =", max_delay2)
    print("Maximum feasible gap =", result2)
    print()

    # Additional quick sanity checks.
    departures3: List[int] = [0, 10]
    max_delay3: int = 0
    result3: int = solution.max_uniform_gap(departures3, max_delay3)
    print("Additional Example 3:")
    print("departures =", departures3)
    print("maxDelay =", max_delay3)
    print("Maximum feasible gap =", result3)
    print()

    departures4: List[int] = [5, 5, 5]
    max_delay4: int = 2
    result4: int = solution.max_uniform_gap(departures4, max_delay4)
    print("Additional Example 4:")
    print("departures =", departures4)
    print("maxDelay =", max_delay4)
    print("Maximum feasible gap =", result4)