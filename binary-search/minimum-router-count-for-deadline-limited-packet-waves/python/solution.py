"""
Title: Minimum Router Count for Deadline-Limited Packet Waves

Problem Description:
A data center receives packet waves in a fixed order. The i-th wave contains packets[i]
packets and arrives at time i. All packets from wave i must be fully processed no later
than deadline[i], where deadline is a non-decreasing array and deadline[i] >= i.

You may deploy k identical routers. Each router can process exactly 1 packet per unit of
time, can work on at most one wave at a time, and packet processing is preemptive:
a router may stop processing one wave and later continue the same or another wave.
However, packets cannot be processed before their wave arrives.

Your task is to find the minimum number of routers needed so that every wave can be
completed by its deadline.

Formally, at any real time t, at most k packets total can be processed across all routers
per unit time, and only packets from waves with arrival time <= t may be processed.
For every prefix of waves 0..i, the total amount of work finished by time deadline[i]
must be enough to complete those waves.

Return the smallest integer k that makes the schedule feasible.

Key idea:
- Feasibility is monotonic:
  if k routers are enough, then any larger number is also enough.
- So we can binary search the answer.
- The hard part is checking whether a fixed k is feasible.

Correct feasibility condition:
For every interval [a, b] on the time axis, the total work of jobs whose release time
and deadline are both inside that interval must fit into the interval's processing
capacity:
    sum(packets[i] for a <= i and deadline[i] <= b) <= k * (b - a + 1)

Because arrivals are exactly i and deadlines are non-decreasing, it is enough to check
intervals of the form [l, deadline[r]] for every l <= r. This can be transformed into:
    max over r of max over l<=r (
        prefix_packets[r + 1] - prefix_packets[l] - k * (deadline[r] - l + 1)
    ) <= 0

Rearrange:
    prefix_packets[r + 1] - k * (deadline[r] + 1) <= prefix_packets[l] - k * l
for some best l <= r.

So for each r, we maintain:
    min_value = min(prefix_packets[l] - k * l) for l in [0..r]
and require:
    prefix_packets[r + 1] - k * (deadline[r] + 1) <= min_value

This yields an O(n) feasibility check for a fixed k, and O(n log answer) overall.
"""

from typing import List


class Solution:
    def minimum_router_count(self, packets: List[int], deadline: List[int]) -> int:
        """
        Compute the minimum number of routers needed.

        Args:
            packets: packets[i] is the amount of work arriving at time i.
            deadline: deadline[i] is the latest time by which wave i must finish.

        Returns:
            The smallest integer k such that all waves can be processed on time.

        Time complexity:
            O(n log A), where A is the answer range searched by binary search.

        Space complexity:
            O(1) extra space beyond the input arrays.
        """
        n: int = len(packets)

        # ------------------------------------------------------------
        # We first build a simple upper bound for binary search.
        #
        # A very safe upper bound is the total amount of work:
        # if we had sum(packets) routers, then every packet of a wave could
        # be processed immediately at its arrival time, so deadlines are
        # certainly satisfied.
        #
        # The problem guarantees the answer fits in signed 64-bit integer,
        # and Python integers are arbitrary precision anyway.
        # ------------------------------------------------------------
        total_packets: int = sum(packets)

        # Edge case safety: though packets[i] >= 1 by constraints, we still
        # keep the lower bound at 1 because at least one router is needed.
        left: int = 1
        right: int = total_packets

        # ------------------------------------------------------------
        # Standard binary search on the answer.
        #
        # Why binary search works:
        # - If k routers are feasible, then k+1, k+2, ... are also feasible.
        # - So feasibility forms a monotonic boolean sequence:
        #     False False False ... True True True
        # - We want the first True.
        # ------------------------------------------------------------
        while left < right:
            mid: int = (left + right) // 2

            if self._can_finish_with_k(packets, deadline, mid):
                right = mid
            else:
                left = mid + 1

        return left

    def _can_finish_with_k(self, packets: List[int], deadline: List[int], k: int) -> bool:
        """
        Check whether k routers are sufficient.

        This method uses the interval feasibility characterization for
        preemptive scheduling with release times and deadlines on identical
        parallel machines.

        For every interval [l, deadline[r]], the total work of waves
        l..r must fit into capacity k * (deadline[r] - l + 1), but only
        waves whose deadlines are <= deadline[r] matter. Since deadline is
        non-decreasing, that is exactly the prefix up to r.

        We transform the condition into an O(n) scan.

        Args:
            packets: Work amounts per wave.
            deadline: Non-decreasing deadlines.
            k: Number of routers to test.

        Returns:
            True if feasible, False otherwise.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        # ------------------------------------------------------------
        # prefix_sum will represent:
        #   prefix_sum = packets[0] + packets[1] + ... + packets[r]
        # as we scan from left to right.
        #
        # We do not need to store the whole prefix array; one running sum is enough.
        # ------------------------------------------------------------
        prefix_sum: int = 0

        # ------------------------------------------------------------
        # We maintain:
        #   min_value = min(prefix_packets[l] - k * l) for all l in [0..r]
        #
        # Important detail:
        # - prefix_packets[l] means sum of packets[0..l-1]
        # - So for l = 0, prefix_packets[0] = 0
        # - Therefore the initial candidate is:
        #       0 - k * 0 = 0
        #
        # This corresponds to intervals starting at time 0.
        # ------------------------------------------------------------
        min_value: int = 0

        # ------------------------------------------------------------
        # We scan each r from 0 to n-1.
        #
        # For current r:
        #   total work in prefix 0..r is prefix_sum_after_update
        #
        # The transformed condition says:
        #   prefix_packets[r+1] - k * (deadline[r] + 1) <= min_value
        #
        # If this fails for any r, then there exists some interval that
        # demands more work than k routers can provide, so schedule is impossible.
        # ------------------------------------------------------------
        for r, work in enumerate(packets):
            prefix_sum += work

            # --------------------------------------------------------
            # Check the interval feasibility condition for all intervals
            # ending at deadline[r] in one shot.
            #
            # left side:
            #   prefix_packets[r+1] - k * (deadline[r] + 1)
            #
            # right side:
            #   min over l<=r of (prefix_packets[l] - k*l)
            #
            # If left side is greater than right side, then for every l<=r:
            #   prefix_packets[r+1] - prefix_packets[l] > k * (deadline[r] - l + 1)
            # meaning the interval [l, deadline[r]] is overloaded.
            # --------------------------------------------------------
            current_need: int = prefix_sum - k * (deadline[r] + 1)
            if current_need > min_value:
                return False

            # --------------------------------------------------------
            # Now update min_value so future positions can use l = r+1
            # as a possible interval start.
            #
            # prefix_packets[r+1] is exactly current prefix_sum.
            # So the new candidate is:
            #   prefix_sum - k * (r + 1)
            # --------------------------------------------------------
            candidate: int = prefix_sum - k * (r + 1)
            if candidate < min_value:
                min_value = candidate

        # If no interval overload was found, k routers are sufficient.
        return True


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    packets1: List[int] = [3, 2, 4]
    deadline1: List[int] = [2, 3, 5]
    result1: int = solution.minimum_router_count(packets1, deadline1)
    print(result1)  # Expected: 2

    # Example 2
    packets2: List[int] = [5, 6, 4]
    deadline2: List[int] = [1, 2, 2]
    result2: int = solution.minimum_router_count(packets2, deadline2)
    print(result2)  # Expected: 8