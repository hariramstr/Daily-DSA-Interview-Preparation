"""
Title: Minimum Signal Delay to Sync Stage Lights

Problem Description:
A concert venue controls n stage lights placed along a straight truss. The i-th light is
mounted at position positions[i] and can only start its animation at one of the times
listed in times[i], where times[i] is sorted in non-decreasing order.

To create a synchronized visual effect, the lighting engineer wants to choose exactly one
start time for each light so that the difference between the earliest chosen time and the
latest chosen time is as small as possible.

Return the minimum possible synchronization delay, defined as:
    max(chosenTimes) - min(chosenTimes)

You are given:
- an integer array positions of length n
- a 2D integer array times where times[i] contains the allowed start times for light i

A solution always exists because each times[i] contains at least one value.

This problem is well suited for binary search on the answer. For a candidate delay D,
determine whether there exists a common time window of length D that contains at least
one allowed start time from every light. If yes, try a smaller delay; otherwise, try a
larger one.

Constraints:
- 1 <= n <= 100000
- positions.length == n
- times.length == n
- 1 <= times[i].length <= 100000
- Sum of all times[i].length over all lights <= 200000
- 0 <= positions[i] <= 10^9
- 0 <= times[i][j] <= 10^9
- Each times[i] is sorted in non-decreasing order
"""

from bisect import bisect_left
from typing import List


class Solution:
    def minimum_signal_delay(self, positions: List[int], times: List[List[int]]) -> int:
        """
        Compute the minimum possible synchronization delay.

        The key idea is to binary search the answer D = allowed window length.
        For a fixed D, we check whether there exists some interval [L, L + D]
        that contains at least one allowed time from every light.

        Args:
            positions: Positions of the lights. These are not used in the logic,
                but are part of the input format.
            times: times[i] is the sorted list of allowed start times for light i.

        Returns:
            The minimum possible value of max(chosenTimes) - min(chosenTimes).

        Time complexity:
            O(S * log R * log M) in the worst case if described loosely, where:
            - S is the total number of time values across all lists
            - R is the search range of answers (up to 1e9)
            More precisely, each feasibility check iterates over all candidate left
            endpoints taken from all time values, and for each list performs a binary
            search. Since the total number of values is at most 200000, this is
            acceptable for the given constraints.

        Space complexity:
            O(S) for storing all candidate left endpoints.
        """
        # The positions array is intentionally unused in the computation because the
        # problem statement explicitly says positions do not affect the synchronization
        # delay calculation. Still, we keep it in the method signature to match the
        # required input format.
        _ = positions

        # ------------------------------------------------------------
        # Step 1: Build the set of candidate left boundaries.
        # ------------------------------------------------------------
        # Why can we restrict the left boundary L to one of the existing allowed times?
        #
        # Suppose there exists some valid window [L, L + D] that hits every list.
        # Let x be the smallest chosen time among the selected times inside that window.
        # Then x is an allowed time from some list, and shifting the window's left edge
        # to x gives [x, x + D], which still contains all chosen times because:
        # - x is the minimum chosen time, so every chosen time >= x
        # - every chosen time <= L + D <= x + D if x >= L
        #
        # Therefore, if a valid window exists, there is also a valid window whose left
        # endpoint is exactly one of the allowed times appearing in the input.
        #
        # This is extremely useful because it turns an infinite search over all possible
        # L values into a finite search over at most 200000 candidates.
        candidates: List[int] = []
        for arr in times:
            candidates.extend(arr)

        # Sorting candidates allows deterministic iteration order. Duplicates are fine:
        # they do not affect correctness. We could deduplicate to reduce repeated work,
        # but even without deduplication the total size is bounded by 200000.
        candidates.sort()

        # ------------------------------------------------------------
        # Step 2: Determine the binary search range for the answer.
        # ------------------------------------------------------------
        # The smallest possible delay is 0.
        # The largest possible delay is:
        #     global_max_time - global_min_time
        # because we can always choose one time from each list, and no chosen set can
        # span more than the full range of all available times.
        global_min: int = min(arr[0] for arr in times)
        global_max: int = max(arr[-1] for arr in times)

        left: int = 0
        right: int = global_max - global_min

        # ------------------------------------------------------------
        # Step 3: Binary search for the minimum feasible delay.
        # ------------------------------------------------------------
        # Standard "find first true" pattern:
        # - If delay mid is feasible, try smaller values by moving right = mid
        # - Otherwise, move left = mid + 1
        while left < right:
            mid: int = (left + right) // 2

            if self._can_sync_with_delay(times, candidates, mid):
                right = mid
            else:
                left = mid + 1

        # At loop end, left == right and is the minimum feasible delay.
        return left

    def _can_sync_with_delay(
        self,
        times: List[List[int]],
        candidates: List[int],
        delay: int,
    ) -> bool:
        """
        Check whether there exists a window of length `delay` that contains at least
        one allowed time from every light.

        Args:
            times: Sorted allowed times for each light.
            candidates: Sorted list of candidate left boundaries.
            delay: Candidate synchronization delay.

        Returns:
            True if some interval [L, L + delay] intersects every times[i], else False.

        Time complexity:
            O(C * n * log k) in the worst case, where:
            - C is the number of candidate left boundaries
            - n is the number of lights
            - k is the average size of a time list
            Since the total number of time values is bounded by 200000, this remains
            practical for the problem constraints.

        Space complexity:
            O(1) auxiliary space beyond the input storage.
        """
        # ------------------------------------------------------------
        # Core feasibility logic:
        # ------------------------------------------------------------
        # For each candidate left boundary L, we test whether every list has at least
        # one value inside [L, L + delay].
        #
        # Because each times[i] is sorted, the fastest way to test this for one list is:
        # 1. Use binary search to find the first value >= L
        # 2. Check whether that value exists and is <= L + delay
        #
        # If this succeeds for every list, then the window works.
        # If no candidate L works, then this delay is not feasible.
        for start in candidates:
            end: int = start + delay
            valid_for_all: bool = True

            # Test every light independently.
            for arr in times:
                # Find the first allowed time in this list that is not smaller than start.
                idx: int = bisect_left(arr, start)

                # There are two failure cases:
                # 1. idx == len(arr): every value in arr is < start, so nothing lies in
                #    the window [start, end]
                # 2. arr[idx] > end: the first value >= start is already too large, so
                #    again there is nothing in the window
                if idx == len(arr) or arr[idx] > end:
                    valid_for_all = False
                    break

            # If every list contributed at least one time inside this window,
            # then the candidate delay is feasible.
            if valid_for_all:
                return True

        # After checking all possible left boundaries, none worked.
        return False


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    positions_1: List[int] = [2, 5, 9]
    times_1: List[List[int]] = [[1, 4, 8], [2, 6], [5, 7, 10]]
    result_1: int = solution.minimum_signal_delay(positions_1, times_1)
    print(result_1)  # Expected: 2

    # Example 2
    positions_2: List[int] = [0, 3, 8, 12]
    times_2: List[List[int]] = [[3], [1, 4, 9], [5, 6], [2, 8, 11]]
    result_2: int = solution.minimum_signal_delay(positions_2, times_2)
    print(result_2)  # Expected: 3