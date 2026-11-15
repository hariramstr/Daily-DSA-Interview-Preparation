"""
Title: Longest Sensor Window With Coverage Gaps Budget

Problem Description:
You are given a strictly increasing array `times` of length `n`, where `times[i]`
is the timestamp (in seconds) of the `i`-th sensor report from a remote device.
A monitoring dashboard wants to display the longest contiguous block of reports
that can be considered a "continuous observation window". However, the device may
occasionally go silent between two consecutive reports.

For any contiguous subarray `times[l..r]`, define its coverage gap cost as the sum
of all missing seconds between adjacent reports inside the window:

cost(l, r) = sum(max(0, times[i] - times[i-1] - 1)) for all i from l+1 to r.

In other words, if two adjacent reports happen at seconds 10 and 13, then seconds
11 and 12 are missing, contributing 2 to the cost. You are given an integer
`budget`, representing the maximum total number of missing seconds that may be
tolerated inside the chosen window.

Return the maximum number of reports in any contiguous window whose coverage gap
cost is at most `budget`.

A window containing only one report has cost 0.

Constraints:
- 1 <= n <= 200000
- 0 <= times[i] <= 10^18
- times is strictly increasing
- 0 <= budget <= 10^18
"""

from typing import List


class Solution:
    def longest_sensor_window(self, times: List[int], budget: int) -> int:
        """
        Find the maximum size of a contiguous window whose total missing-second
        gap cost is at most the given budget.

        Args:
            times: Strictly increasing list of sensor report timestamps.
            budget: Maximum allowed total missing seconds inside the chosen window.

        Returns:
            The maximum number of reports in any valid contiguous window.

        Time complexity:
            O(n), because each pointer in the sliding window moves at most n times.

        Space complexity:
            O(1), excluding the input array.
        """
        n: int = len(times)

        # A single report always forms a valid window with cost 0.
        # Since n >= 1 by constraints, the answer is at least 1.
        best: int = 1

        # `left` is the starting index of the current sliding window.
        left: int = 0

        # `current_cost` stores the total gap cost inside the current window [left..right].
        #
        # Important observation:
        # The cost of a window is the sum of costs between adjacent elements.
        # If we define:
        #   gap_cost(i) = times[i] - times[i - 1] - 1   for i >= 1
        # then:
        #   cost(left, right) = gap_cost(left+1) + gap_cost(left+2) + ... + gap_cost(right)
        #
        # Because `times` is strictly increasing, times[i] - times[i-1] >= 1,
        # so gap_cost(i) is always >= 0. That non-negativity is exactly why
        # the sliding window technique works perfectly here.
        current_cost: int = 0

        # We expand the window one report at a time using `right`.
        for right in range(n):
            # When `right` moves forward and becomes part of the window,
            # we add the gap between times[right - 1] and times[right].
            #
            # For right == 0, there is no previous element, so no gap is added.
            if right > 0:
                current_cost += times[right] - times[right - 1] - 1

            # If the window cost is too large, we must shrink from the left
            # until the window becomes valid again.
            #
            # Why removing from the left works:
            # The window cost is made of adjacent internal gaps only.
            # When we move `left` from L to L+1, the only gap that disappears
            # from the window cost is the gap between times[L] and times[L+1].
            while current_cost > budget and left < right:
                current_cost -= times[left + 1] - times[left] - 1
                left += 1

            # At this point, [left..right] is guaranteed to have cost <= budget.
            # So we can safely update the best answer.
            window_length: int = right - left + 1
            if window_length > best:
                best = window_length

        return best

    def solve(self, times: List[int], budget: int) -> int:
        """
        Wrapper method that computes the answer for the problem.

        Args:
            times: Strictly increasing list of timestamps.
            budget: Maximum allowed total missing seconds.

        Returns:
            Maximum valid contiguous window length.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        return self.longest_sensor_window(times, budget)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    times1: List[int] = [2, 3, 7, 8, 10]
    budget1: int = 3
    result1: int = solution.solve(times1, budget1)
    print(result1)  # Expected: 4

    # Example 2
    times2: List[int] = [1, 5, 6, 9, 15, 16]
    budget2: int = 5
    result2: int = solution.solve(times2, budget2)
    print(result2)  # Expected: 3

    # Additional quick checks
    times3: List[int] = [10]
    budget3: int = 0
    result3: int = solution.solve(times3, budget3)
    print(result3)  # Expected: 1

    times4: List[int] = [1, 2, 3, 4, 5]
    budget4: int = 0
    result4: int = solution.solve(times4, budget4)
    print(result4)  # Expected: 5