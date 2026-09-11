"""
Title: Minimum Router Delay for Sequential Packet Waves

Problem Description:
You are given a network router that must transmit packet waves in the given order.
The i-th wave contains packets[i] packets, and the router can process at most d
packets per second while a wave is active. Because of protocol overhead, each wave
takes ceil(packets[i] / d) whole seconds to finish. The router cannot start the next
wave until the current one is completed.

You are also given an integer maxTime, the maximum total number of seconds allowed
to transmit all waves. Your task is to find the minimum integer router delay capacity
d such that all waves can be transmitted within maxTime seconds.

If d is too small, some waves take too long. If d is larger, every wave finishes no
slower than before. This monotonic behavior makes the problem suitable for binary
search on the answer.

Return the minimum positive integer d that allows the total transmission time to be
at most maxTime.

Constraints:
- 1 <= packets.length <= 100000
- 1 <= packets[i] <= 10^9
- packets.length <= maxTime <= 10^14
- The answer always exists.
"""

from typing import List


class Solution:
    def _can_finish(self, packets: List[int], max_time: int, d: int) -> bool:
        """
        Check whether all packet waves can be transmitted within max_time
        using router capacity d.

        Args:
            packets: List of packet counts for each wave.
            max_time: Maximum allowed total transmission time.
            d: Candidate router capacity in packets per second.

        Returns:
            True if total required time is at most max_time, otherwise False.

        Time complexity:
            O(n), where n is the number of waves.

        Space complexity:
            O(1), excluding input storage.
        """
        # This variable accumulates the total number of whole seconds needed
        # to transmit all waves using the candidate capacity d.
        total_time: int = 0

        # We process each wave independently because the total time is simply
        # the sum of the time needed for every wave in order.
        for wave_packets in packets:
            # We need ceil(wave_packets / d), but instead of using floating-point
            # math, we use a standard integer formula:
            #
            #     ceil(a / b) = (a + b - 1) // b
            #
            # This avoids precision issues and is faster and cleaner in Python.
            total_time += (wave_packets + d - 1) // d

            # Early stopping optimization:
            # If we already exceeded max_time, there is no need to continue.
            # This makes the helper faster in many cases, especially when d is too small.
            if total_time > max_time:
                return False

        # If we finish the loop without exceeding max_time, then this capacity works.
        return total_time <= max_time

    def minimum_router_delay(self, packets: List[int], maxTime: int) -> int:
        """
        Find the minimum positive integer router capacity d such that all packet
        waves can be transmitted within maxTime seconds.

        Args:
            packets: List of packet counts for each wave.
            maxTime: Maximum allowed total transmission time.

        Returns:
            The minimum valid integer router capacity.

        Time complexity:
            O(n log M), where n is the number of waves and M is max(packets).

        Space complexity:
            O(1), excluding input storage.
        """
        # Binary search is appropriate because the condition is monotonic:
        #
        # - If a capacity d is sufficient, then any larger capacity is also sufficient.
        # - If a capacity d is insufficient, then any smaller capacity is also insufficient.
        #
        # Therefore, the valid capacities form a suffix of the positive integers,
        # and we want the first valid one.

        # The smallest possible positive capacity is 1.
        left: int = 1

        # A safe upper bound is max(packets):
        # with d = max(packets), every wave takes at most 1 second,
        # so total time is at most len(packets), and the constraints guarantee
        # len(packets) <= maxTime, so this upper bound always works.
        right: int = max(packets)

        # We now perform standard binary search on the answer space [left, right].
        while left < right:
            # Midpoint candidate capacity.
            mid: int = left + (right - left) // 2

            # Check whether this candidate capacity is enough.
            if self._can_finish(packets, maxTime, mid):
                # If mid works, it might be the answer, but there could be
                # a smaller valid capacity. So we keep searching the left half,
                # including mid itself.
                right = mid
            else:
                # If mid does not work, then all capacities <= mid also do not work.
                # So we must search strictly to the right.
                left = mid + 1

        # When left == right, binary search has converged to the smallest valid capacity.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    packets1: List[int] = [8, 4, 10]
    max_time1: int = 8
    result1: int = solution.minimum_router_delay(packets1, max_time1)
    print("Example 1:")
    print(f"packets = {packets1}, maxTime = {max_time1}")
    print(f"Minimum router delay capacity = {result1}")
    print()

    # Verification for Example 1:
    # d = 4 -> ceil(8/4) + ceil(4/4) + ceil(10/4) = 2 + 1 + 3 = 6 <= 8
    # d = 3 -> ceil(8/3) + ceil(4/3) + ceil(10/3) = 3 + 2 + 4 = 9 > 8
    # So the correct answer is 4.

    # Example 2
    packets2: List[int] = [30, 11, 23, 4, 20]
    max_time2: int = 10
    result2: int = solution.minimum_router_delay(packets2, max_time2)
    print("Example 2:")
    print(f"packets = {packets2}, maxTime = {max_time2}")
    print(f"Minimum router delay capacity = {result2}")
    print()

    # Verification for Example 2:
    # d = 11 -> ceil(30/11) + ceil(11/11) + ceil(23/11) + ceil(4/11) + ceil(20/11)
    #       = 3 + 1 + 3 + 1 + 2 = 10 <= 10
    # d = 10 -> 3 + 2 + 3 + 1 + 2 = 11 > 10
    # So the correct answer is 11.