"""
Title: Minimum Processor Speed for Sequential Simulation Batches

Problem Description:
A research platform must run n simulation batches in the given order. The i-th batch
contains workloads[i] compute units and must be fully processed no later than
deadlines[i] minutes from time 0. All batches are executed on a single processor,
one after another, without reordering and without preemption.

If the processor speed is S compute units per minute, then batch i takes
ceil(workloads[i] / S) minutes to finish.

Your task is to find the minimum positive integer processor speed S such that every
batch finishes by its deadline. If no speed can satisfy the deadlines, return -1.

More formally, let finish[i] be the cumulative running time of batches 0 through i.
A speed S is feasible if for every i, finish[i] <= deadlines[i]. You must compute
the smallest feasible S.

This problem is designed for a binary-search-on-answer approach. The challenge is to
test feasibility efficiently while handling very large workloads and deadlines.
Be careful with integer overflow when summing running times and computing ceil division.
"""

from typing import List


class Solution:
    def _is_feasible(self, workloads: List[int], deadlines: List[int], speed: int) -> bool:
        """
        Check whether a given processor speed allows all batches to finish by deadline.

        We simulate the batches in order. For each batch:
        - compute its processing time as ceil(workload / speed)
        - add that time to the cumulative finish time
        - immediately verify that the current prefix finishes by its required deadline

        Early exit is used as soon as any deadline is violated.

        Args:
            workloads: List of batch workloads.
            deadlines: List of required completion times for each prefix ending at index i.
            speed: Candidate processor speed to test.

        Returns:
            True if the speed is feasible for all batches, otherwise False.

        Time complexity:
            O(n), where n is the number of batches.

        Space complexity:
            O(1), ignoring input storage.
        """
        # This variable stores the total elapsed processing time after finishing
        # the batches seen so far.
        #
        # We keep it as an integer because:
        # - all workloads are integers
        # - speed is an integer
        # - ceil division produces an integer number of minutes
        # - Python integers automatically handle very large values safely
        elapsed_time: int = 0

        # Process batches strictly in the given order because reordering is forbidden.
        for i in range(len(workloads)):
            # Compute ceil(workloads[i] / speed) using integer arithmetic:
            # ceil(a / b) == (a + b - 1) // b
            #
            # We avoid floating-point math because:
            # - inputs can be very large
            # - integer arithmetic is exact
            # - it is the standard safe approach for this pattern
            batch_time: int = (workloads[i] + speed - 1) // speed

            # Add the current batch's time to the cumulative finish time.
            elapsed_time += batch_time

            # The i-th deadline applies to the completion time of the prefix [0..i].
            # If we already exceed it, this speed is impossible.
            if elapsed_time > deadlines[i]:
                return False

        # If every prefix met its deadline, the speed is feasible.
        return True

    def min_processor_speed(self, workloads: List[int], deadlines: List[int]) -> int:
        """
        Find the minimum positive integer processor speed that satisfies all deadlines.

        The key observation is monotonicity:
        - If a speed S is feasible, then any speed larger than S is also feasible,
          because every batch time ceil(workload / S) can only stay the same or decrease.
        - This monotonic property allows binary search on the answer.

        Steps:
        1. Handle impossible cases that no finite speed can fix.
        2. Find an upper bound that is guaranteed to be feasible by doubling.
        3. Binary search the smallest feasible speed in that range.

        Args:
            workloads: List of batch workloads.
            deadlines: List of deadlines for each prefix completion.

        Returns:
            The minimum feasible positive integer speed, or -1 if impossible.

        Time complexity:
            O(n log A), where A is the answer magnitude / searched speed range.

        Space complexity:
            O(1), ignoring input storage.
        """
        n: int = len(workloads)

        # Basic sanity check for consistent input lengths.
        # In typical coding platforms this is guaranteed, but keeping the code robust
        # is good practice.
        if n != len(deadlines):
            return -1

        # If there are no batches, one could argue speed 1 is enough.
        # However, constraints say n >= 1, so this is just defensive programming.
        if n == 0:
            return 1

        # Important impossibility check:
        # Every non-empty batch with positive workload needs at least 1 minute
        # for any finite positive integer speed, because ceil(workload / speed) >= 1.
        #
        # Therefore, after finishing the first i+1 batches, the cumulative time is
        # at least i+1 minutes, no matter how large the speed is.
        #
        # So if deadlines[i] < i+1 for any i, then even "infinite-like" speed cannot help.
        # This is the strongest simple impossibility condition for this problem.
        for i in range(n):
            minimum_possible_prefix_time: int = i + 1
            if deadlines[i] < minimum_possible_prefix_time:
                return -1

        # We now need to find the smallest feasible speed.
        #
        # Binary search requires a search interval [left, right] such that:
        # - left is a candidate lower bound
        # - right is known to be feasible
        #
        # We start with the smallest allowed positive speed.
        left: int = 1

        # Start with a small upper bound and repeatedly double it until it becomes feasible.
        # This avoids needing a tricky closed-form upper bound and is very reliable.
        right: int = 1

        # Because we already ruled out the "no finite speed can ever work" case,
        # a feasible speed must exist. Therefore this loop will terminate.
        while not self._is_feasible(workloads, deadlines, right):
            right *= 2

        # Standard binary search for the first feasible value.
        #
        # Invariant:
        # - answer is in [left, right]
        # - right is feasible
        while left < right:
            # Midpoint chosen this way is safe and standard.
            mid: int = left + (right - left) // 2

            # If mid works, then the minimum feasible speed is <= mid,
            # so we keep the left half including mid.
            if self._is_feasible(workloads, deadlines, mid):
                right = mid
            else:
                # Otherwise mid is too slow, so answer must be > mid.
                left = mid + 1

        # At loop end, left == right and points to the smallest feasible speed.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    #
    # workloads = [7, 11, 5]
    # deadlines = [4, 8, 10]
    #
    # Careful manual verification:
    # - speed 3:
    #   times = [ceil(7/3), ceil(11/3), ceil(5/3)] = [3, 4, 2]
    #   cumulative = [3, 7, 9]
    #   compare with deadlines [4, 8, 10] => all satisfied
    # - speed 2:
    #   times = [4, 6, 3]
    #   cumulative = [4, 10, 13]
    #   violates deadline 8 at index 1
    # Therefore the true minimum is 3.
    workloads1: List[int] = [7, 11, 5]
    deadlines1: List[int] = [4, 8, 10]
    print(solution.min_processor_speed(workloads1, deadlines1))  # Expected: 3

    # Example 2 from the prompt.
    #
    # workloads = [9, 9, 9]
    # deadlines = [0, 5, 8]
    #
    # The first batch always needs at least 1 minute for any finite positive speed,
    # but its deadline is 0, so this is impossible.
    workloads2: List[int] = [9, 9, 9]
    deadlines2: List[int] = [0, 5, 8]
    print(solution.min_processor_speed(workloads2, deadlines2))  # Expected: -1

    # Additional small sanity check.
    workloads3: List[int] = [1, 1, 1]
    deadlines3: List[int] = [1, 2, 3]
    print(solution.min_processor_speed(workloads3, deadlines3))  # Expected: 1