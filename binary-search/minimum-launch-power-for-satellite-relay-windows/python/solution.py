"""
Title: Minimum Launch Power for Satellite Relay Windows

Problem Description:
A space operations team needs to transmit data to a sequence of orbital relay windows.
The i-th relay window opens at time windows[i], and sending to that window requires
power[i] units of launch energy. A single transmitter can only handle windows in
chronological order. If the transmitter is configured with a launch power limit X,
then it may send to any relay window whose required power is at most X. However,
skipped windows are lost forever, and the team must still successfully transmit to
at least k relay windows in order.

Your task is to find the minimum integer launch power limit X such that it is possible
to transmit to at least k relay windows.

The arrays windows and power are both length n. The windows array is strictly increasing,
but the actual times only determine the order of processing; you may not reorder windows.
Since a higher power limit always allows transmitting to every window that was possible
under a lower limit, the answer is monotonic and can be found efficiently.

Return the minimum integer X that allows at least k successful transmissions.

Constraints:
- 1 <= n <= 200000
- 1 <= k <= n
- 1 <= windows[i] <= 10^9
- windows is strictly increasing
- 1 <= power[i] <= 10^9
"""

from typing import List


class Solution:
    def can_transmit_at_least_k(self, power: List[int], k: int, limit: int) -> bool:
        """
        Check whether a given launch power limit allows at least k transmissions.

        The transmitter processes windows in order. A window is usable if its required
        power is less than or equal to the chosen limit. Because skipping is allowed
        and only the count of successful transmissions matters, the best strategy is
        simply to count how many windows are usable under this limit.

        Args:
            power: List of required power values for each relay window.
            k: Minimum number of successful transmissions needed.
            limit: Candidate launch power limit being tested.

        Returns:
            True if at least k windows can be transmitted to, otherwise False.

        Time complexity:
            O(n), where n is the number of windows.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We count how many relay windows are usable with the current power limit.
        # A window is usable exactly when power[i] <= limit.
        #
        # Why is simple counting enough?
        # - Windows must be processed in chronological order.
        # - We are allowed to skip unusable windows.
        # - There is no extra penalty or dependency between chosen windows.
        # Therefore, every usable window can be taken, and the maximum number of
        # successful transmissions is just the number of usable windows.
        successful_transmissions: int = 0

        # Scan through every window once.
        for required_power in power:
            # If this window's requirement fits within the tested limit,
            # then we can successfully transmit to it.
            if required_power <= limit:
                successful_transmissions += 1

                # Early exit optimization:
                # As soon as we already have at least k successful transmissions,
                # we do not need to continue scanning the rest of the array.
                if successful_transmissions >= k:
                    return True

        # If we finish the scan without reaching k, then this limit is not enough.
        return False

    def minimum_launch_power(self, windows: List[int], power: List[int], k: int) -> int:
        """
        Find the minimum integer launch power limit that allows at least k transmissions.

        This uses binary search on the answer. The key monotonic property is:
        - If a power limit X works, then any larger limit also works.
        - If a power limit X does not work, then any smaller limit also does not work.

        The windows array is included because the problem states the windows are processed
        in chronological order, but for this specific counting logic, only the order matters,
        and the order is already fixed by the input. The actual numeric window times do not
        affect feasibility beyond preserving sequence order.

        Args:
            windows: Strictly increasing relay window times.
            power: Required power for each relay window.
            k: Minimum number of successful transmissions needed.

        Returns:
            The minimum integer launch power limit that makes at least k transmissions possible.

        Time complexity:
            O(n log M), where n is the number of windows and M is the range of power values.

        Space complexity:
            O(1), ignoring input storage.
        """
        # The problem guarantees both arrays have the same length.
        # We do not need the actual values in "windows" for the computation,
        # but we keep the parameter because it is part of the required interface.
        _ = windows

        # Binary search boundaries:
        #
        # The smallest possible useful answer cannot be below the minimum required power,
        # because a limit smaller than every power value would allow zero transmissions.
        #
        # The largest necessary answer never needs to exceed the maximum required power,
        # because with that limit, every window becomes usable.
        left: int = min(power)
        right: int = max(power)

        # We now binary search for the smallest limit that works.
        #
        # Invariant we maintain:
        # - The true answer is always somewhere in [left, right].
        while left < right:
            # Middle candidate limit.
            # Using integer division keeps everything as integers, which matches
            # the problem requirement to return an integer launch power limit.
            mid: int = (left + right) // 2

            # Test whether this candidate limit is sufficient.
            if self.can_transmit_at_least_k(power, k, mid):
                # If mid works, then the answer could be mid or something smaller.
                # So we keep the left half, including mid.
                right = mid
            else:
                # If mid does not work, then every value <= mid also does not work
                # because increasing the limit is the only way to make more windows usable.
                # Therefore, the answer must be strictly larger than mid.
                left = mid + 1

        # When left == right, binary search has converged to the minimum valid limit.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    windows_1: List[int] = [2, 5, 9, 12]
    power_1: List[int] = [7, 3, 6, 4]
    k_1: int = 3
    result_1: int = solution.minimum_launch_power(windows_1, power_1, k_1)
    print(result_1)  # Expected: 6

    # Example 2
    windows_2: List[int] = [1, 4, 8, 10, 15]
    power_2: List[int] = [9, 2, 5, 8, 1]
    k_2: int = 4
    result_2: int = solution.minimum_launch_power(windows_2, power_2, k_2)
    print(result_2)  # Expected: 8