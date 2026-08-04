"""
Title: Maximum Starting Delay Before Missing Any Checkpoint

Problem Description:
You are given a route with n mandatory checkpoints that must be visited in order.
For each checkpoint i, the travel time from checkpoint i - 1 to checkpoint i is
travel[i], and the latest allowed arrival time at checkpoint i is deadline[i].

You begin before checkpoint 0, and you may choose to wait some whole number of
minutes x before starting the trip. Once you start, you move continuously through
the route and cannot reorder or skip checkpoints.

Your task is to compute the maximum integer starting delay x such that, after
waiting x minutes and then traveling through all checkpoints in order, you still
arrive at every checkpoint no later than its deadline.

Formally, if prefix[i] is the total travel time needed to reach checkpoint i,
then the arrival time at checkpoint i is x + prefix[i]. This must satisfy:
    x + prefix[i] <= deadline[i]
for every i.

If it is impossible to satisfy all deadlines even with x = 0, return -1.

A binary-search-based solution is expected: if a given delay x is feasible, then
any smaller delay is also feasible, which makes the answer monotonic.
"""

from typing import List


class Solution:
    def _can_delay(self, travel: List[int], deadline: List[int], delay: int) -> bool:
        """
        Check whether a given starting delay is feasible.

        We simulate the trip by building cumulative travel time step by step.
        At each checkpoint, we compute:
            arrival_time = delay + cumulative_travel
        and verify that it does not exceed the checkpoint's deadline.

        Args:
            travel: List of travel times between consecutive checkpoints.
            deadline: List of latest allowed arrival times for each checkpoint.
            delay: Candidate integer starting delay to test.

        Returns:
            True if all checkpoints can still be reached on time with this delay,
            otherwise False.

        Time complexity:
            O(n), where n is the number of checkpoints.

        Space complexity:
            O(1), ignoring input storage.
        """
        # This variable stores the total travel time accumulated so far.
        # After processing checkpoint i, it equals the prefix sum up to i.
        cumulative_travel: int = 0

        # We walk through checkpoints in order because the route order is fixed.
        for i in range(len(travel)):
            # Add the time needed to travel from the previous checkpoint
            # to the current checkpoint.
            cumulative_travel += travel[i]

            # The arrival time at this checkpoint is:
            #   waiting before start + total travel done so far
            arrival_time: int = delay + cumulative_travel

            # If we arrive after the allowed deadline, this delay is not feasible.
            # Because every checkpoint must be on time, one failure means False.
            if arrival_time > deadline[i]:
                return False

        # If we never violated any deadline, this delay works.
        return True

    def maximum_starting_delay(self, travel: List[int], deadline: List[int]) -> int:
        """
        Compute the maximum feasible integer starting delay.

        The key observation is monotonicity:
        - If a delay x is feasible, then every smaller delay is also feasible.
        - If a delay x is not feasible, then every larger delay is also not feasible.

        That makes binary search a perfect fit.

        We first check whether delay 0 is feasible at all. If not, the route is
        impossible and we return -1.

        Otherwise, we binary search the largest delay that still satisfies all
        checkpoint deadlines.

        Args:
            travel: List of travel times between consecutive checkpoints.
            deadline: List of latest allowed arrival times for each checkpoint.

        Returns:
            The maximum integer starting delay, or -1 if even delay 0 is impossible.

        Time complexity:
            O(n log M), where:
            - n is the number of checkpoints
            - M is the search range for the answer

        Space complexity:
            O(1), ignoring input storage.
        """
        # Defensive check: if the input lengths differ, the problem statement
        # would be violated. We raise an error to make misuse obvious.
        if len(travel) != len(deadline):
            raise ValueError("travel and deadline must have the same length")

        n: int = len(travel)

        # If there are no checkpoints, the problem constraints say n >= 1,
        # but we still handle it gracefully.
        if n == 0:
            return 0

        # First, test the smallest possible delay: 0.
        # If even starting immediately misses some checkpoint, no solution exists.
        if not self._can_delay(travel, deadline, 0):
            return -1

        # We need an upper bound for binary search.
        #
        # Since for every checkpoint:
        #   delay <= deadline[i] - prefix[i]
        # the answer can never exceed the minimum of those values.
        #
        # We compute that exact safe upper bound in one pass.
        cumulative_travel: int = 0
        upper_bound: int = 2**63 - 1

        for i in range(n):
            cumulative_travel += travel[i]

            # This checkpoint alone implies:
            #   delay <= deadline[i] - cumulative_travel
            # We keep the minimum across all checkpoints.
            checkpoint_limit: int = deadline[i] - cumulative_travel
            if checkpoint_limit < upper_bound:
                upper_bound = checkpoint_limit

        # At this point, upper_bound is guaranteed to be feasible or at least
        # not smaller than the true answer, because it comes directly from the
        # mathematical constraints.
        #
        # Since delay 0 is feasible, upper_bound must be >= 0.
        left: int = 0
        right: int = upper_bound
        answer: int = 0

        # Standard binary search for the maximum feasible value.
        #
        # Invariant:
        # - All feasible answers are in [left, right] or already stored in answer.
        # - We move rightward when mid is feasible, because we want the maximum.
        while left <= right:
            # Use the overflow-safe midpoint formula.
            # In Python overflow is not an issue, but this is still best practice.
            mid: int = left + (right - left) // 2

            # Check whether this candidate delay works for all checkpoints.
            if self._can_delay(travel, deadline, mid):
                # mid is feasible, so it is a valid candidate answer.
                answer = mid

                # Try to find an even larger feasible delay.
                left = mid + 1
            else:
                # mid is too large, so the answer must be smaller.
                right = mid - 1

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # Prefix travel times: [3, 5, 9]
    # Feasibility condition:
    #   x + 3 <= 5   -> x <= 2
    #   x + 5 <= 8   -> x <= 3
    #   x + 9 <= 12  -> x <= 3
    # Therefore the true maximum feasible delay is 2.
    #
    # Note:
    # The textual explanation in the prompt claims the answer is 3, but that
    # contradicts the formal condition x + prefix[i] <= deadline[i] for every i.
    # Under the formal definition, x = 3 would arrive at the first checkpoint
    # at time 6, which is later than deadline 5. So the correct result is 2.
    travel1 = [3, 2, 4]
    deadline1 = [5, 8, 12]
    print(solution.maximum_starting_delay(travel1, deadline1))  # Correct by formula: 2

    # Example 2
    # Prefix travel times: [4, 8, 12]
    # With x = 0, first arrival is 4 > 3, so impossible.
    travel2 = [4, 4, 4]
    deadline2 = [3, 10, 15]
    print(solution.maximum_starting_delay(travel2, deadline2))  # Expected: -1

    # Additional sanity check
    # Prefix travel times: [1, 3, 6]
    # Limits:
    #   x <= 4, x <= 4, x <= 4
    # So answer should be 4.
    travel3 = [1, 2, 3]
    deadline3 = [5, 7, 10]
    print(solution.maximum_starting_delay(travel3, deadline3))  # Expected: 4