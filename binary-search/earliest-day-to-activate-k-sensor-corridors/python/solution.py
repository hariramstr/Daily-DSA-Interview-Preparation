"""
Title: Earliest Day to Activate K Sensor Corridors

Problem Description:
A research facility has deployed sensors along a long hallway. There are n sensors in a fixed
left-to-right order, and sensor i becomes operational on day activationDay[i]. A corridor is
defined as a contiguous block of sensors in this order. A corridor is considered valid on day D
if every sensor inside that block is operational by day D, and the length of the block is at
least minLen and at most maxLen.

The facility wants to activate at least k non-overlapping valid corridors as early as possible.
Two corridors are non-overlapping if they do not share any sensor index. You may choose any
corridor lengths within the allowed range [minLen, maxLen], and you do not need to use all
operational sensors.

Return the earliest day D such that it is possible to select at least k non-overlapping valid
corridors. If it is impossible even after all sensors have become operational, return -1.

This problem is designed to reward a binary search on the answer. For a fixed day D, each sensor
can be treated as either active or inactive, and the challenge is to determine whether at least k
disjoint valid segments can be formed from the active runs. A correct solution must handle large
inputs efficiently.

Constraints:
- 1 <= n <= 200000
- 1 <= activationDay[i] <= 1000000000
- 1 <= k <= n
- 1 <= minLen <= maxLen <= n
- activationDay.length == n
"""

from typing import List


class Solution:
    def _can_make_k_corridors(
        self,
        activation_day: List[int],
        k: int,
        min_len: int,
        day: int,
    ) -> bool:
        """
        Check whether at least k non-overlapping valid corridors can be formed by a given day.

        A sensor is active on 'day' if activation_day[i] <= day.
        Any contiguous active run of length L can contribute floor(L / min_len) corridors,
        because:
        - every chosen corridor must have length at least min_len
        - choosing longer corridors never helps maximize the number of non-overlapping corridors
        - therefore, to maximize count, we greedily split each active run into as many
          min_len-sized corridors as possible

        Note:
        The upper bound maxLen does not reduce feasibility here because every corridor of
        length exactly min_len is allowed whenever min_len <= maxLen.

        Args:
            activation_day: List of activation days for each sensor.
            k: Required number of non-overlapping corridors.
            min_len: Minimum allowed corridor length.
            day: Candidate day being tested.

        Returns:
            True if at least k corridors can be formed by 'day', otherwise False.

        Time complexity:
            O(n), where n is the number of sensors.

        Space complexity:
            O(1), excluding input storage.
        """
        corridors_formed: int = 0
        current_active_run: int = 0

        # We scan the array once and measure lengths of consecutive active sensors.
        # Every time we hit an inactive sensor, the current active run ends.
        # From a run of length L, the maximum number of non-overlapping valid corridors
        # is floor(L / min_len), because using the smallest allowed length gives the
        # greatest number of disjoint corridors.
        for sensor_day in activation_day:
            if sensor_day <= day:
                current_active_run += 1
            else:
                if current_active_run >= min_len:
                    corridors_formed += current_active_run // min_len
                    if corridors_formed >= k:
                        return True
                current_active_run = 0

        # Handle the final run if the array ends while still inside an active block.
        if current_active_run >= min_len:
            corridors_formed += current_active_run // min_len

        return corridors_formed >= k

    def earliestDayToActivateKCorridors(
        self,
        activationDay: List[int],
        k: int,
        minLen: int,
        maxLen: int,
    ) -> int:
        """
        Find the earliest day on which at least k non-overlapping valid corridors can be formed.

        Core idea:
        1. Binary search on the answer (the day).
        2. For a fixed day D, convert sensors into active/inactive.
        3. Check if at least k disjoint valid corridors can be formed.
        4. Because feasibility is monotonic (if day D works, any later day also works),
           binary search is valid.

        Important observation:
        Since any corridor length in [minLen, maxLen] is allowed, and minLen itself is in
        that range, maximizing the number of corridors always uses length exactly minLen.
        Therefore, for each active run of length L, the maximum number of corridors is
        floor(L / minLen).

        Args:
            activationDay: activationDay[i] is the day sensor i becomes operational.
            k: Minimum number of non-overlapping corridors required.
            minLen: Minimum allowed corridor length.
            maxLen: Maximum allowed corridor length.

        Returns:
            The earliest feasible day, or -1 if impossible.

        Time complexity:
            O(n log M), where:
            - n is the number of sensors
            - M is the range of activation days (max(activationDay) - min(activationDay) + 1)

        Space complexity:
            O(1), excluding input storage.
        """
        n: int = len(activationDay)

        # Quick impossibility check:
        # Even if every sensor is active, each corridor needs at least minLen sensors.
        # Therefore, the absolute maximum number of non-overlapping corridors is n // minLen.
        # If that is still less than k, the task is impossible.
        if n // minLen < k:
            return -1

        # The earliest possible answer cannot be before the minimum activation day,
        # and the latest relevant day is the maximum activation day.
        left: int = min(activationDay)
        right: int = max(activationDay)

        # Before binary search, verify that the problem is actually feasible by the final day.
        # This is a safety check and also keeps the logic explicit and beginner-friendly.
        if not self._can_make_k_corridors(activationDay, k, minLen, right):
            return -1

        # Standard binary search for the first feasible day.
        # Invariant:
        # - right is always a feasible day
        # - we search for the smallest feasible day
        while left < right:
            mid: int = left + (right - left) // 2

            # If mid works, try to find an even earlier working day.
            if self._can_make_k_corridors(activationDay, k, minLen, mid):
                right = mid
            else:
                # If mid does not work, all earlier days also do not work
                # because fewer or equal sensors would be active.
                left = mid + 1

        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    activation_day_1: List[int] = [4, 2, 5, 3, 3, 6, 1]
    k_1: int = 2
    min_len_1: int = 2
    max_len_1: int = 3
    result_1: int = solution.earliestDayToActivateKCorridors(
        activation_day_1, k_1, min_len_1, max_len_1
    )
    print(result_1)  # Expected: 4

    # Corrected Example 2
    activation_day_2: List[int] = [7, 7, 7]
    k_2: int = 2
    min_len_2: int = 2
    max_len_2: int = 2
    result_2: int = solution.earliestDayToActivateKCorridors(
        activation_day_2, k_2, min_len_2, max_len_2
    )
    print(result_2)  # Expected: -1

    # Additional check based on the original mistaken example text
    activation_day_3: List[int] = [7, 7, 7, 7, 7]
    k_3: int = 2
    min_len_3: int = 2
    max_len_3: int = 2
    result_3: int = solution.earliestDayToActivateKCorridors(
        activation_day_3, k_3, min_len_3, max_len_3
    )
    print(result_3)  # Expected: 7