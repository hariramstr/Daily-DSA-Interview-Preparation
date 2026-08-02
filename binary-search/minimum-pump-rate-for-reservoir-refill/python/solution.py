"""
Title: Minimum Pump Rate for Reservoir Refill

Problem Description:
A city utility team must refill several reservoirs over a fixed number of nights.
You are given an integer array volumes where volumes[i] is the amount of water
needed for the i-th reservoir, and an integer h representing the total number
of nights available.

In one night, the team chooses exactly one reservoir and pumps water into it at
a constant rate of k units per night. If a reservoir needs less than k units,
the remaining pumping capacity for that night is wasted and cannot be used on
another reservoir.

A reservoir may require multiple nights to finish, and the number of nights
needed for a reservoir with volume v is ceil(v / k).

Return the minimum integer pump rate k such that all reservoirs can be
completely refilled within h nights.

This is guaranteed to have a valid answer.

Constraints:
- 1 <= volumes.length <= 100000
- 1 <= volumes[i] <= 1000000000
- volumes.length <= h <= 1000000000

Example 1:
Input: volumes = [8, 5, 10, 7], h = 8
Output: 5

Example 2:
Input: volumes = [30, 11, 23, 4, 20], h = 6
Output: 23
"""

from typing import List


class Solution:
    def min_pump_rate(self, volumes: List[int], h: int) -> int:
        """
        Find the minimum integer pump rate needed to refill all reservoirs
        within h nights using binary search on the answer.

        Args:
            volumes: A list where volumes[i] is the water needed for reservoir i.
            h: The maximum number of nights available.

        Returns:
            The minimum integer pump rate k such that all reservoirs can be
            completed within h nights.

        Time Complexity:
            O(n * log m)
            where n is the number of reservoirs and m is the maximum volume.

        Space Complexity:
            O(1)
            excluding the input storage.
        """
        # The slowest possible useful rate is 1 unit per night.
        # If we pump slower than 1, that would not make sense because k must be
        # a positive integer.
        left: int = 1

        # The fastest rate we would ever need to consider is the largest single
        # reservoir volume.
        #
        # Why is this enough?
        # Because if k is equal to max(volumes), then every reservoir can be
        # finished in at most 1 night, so the total nights needed is at most
        # len(volumes). The problem guarantees len(volumes) <= h, so this rate
        # is always sufficient.
        right: int = max(volumes)

        # We will binary search for the smallest rate that works.
        #
        # Key monotonic property:
        # - If a rate k is sufficient, then any rate larger than k is also sufficient.
        # - If a rate k is not sufficient, then any rate smaller than k is also not sufficient.
        #
        # This "false, false, false, ..., true, true, true" pattern is exactly
        # what makes binary search valid here.
        while left < right:
            # Compute the middle candidate rate.
            #
            # We use this standard form to avoid overflow in languages where
            # overflow matters. Python integers do not overflow here, but this
            # is still a best practice and very common in interviews.
            mid: int = left + (right - left) // 2

            # Check whether this candidate rate is fast enough.
            if self._can_finish(volumes, h, mid):
                # If mid works, it might be the answer, but there could still be
                # a smaller valid rate on the left side.
                #
                # So we keep mid in the search range by moving right to mid.
                right = mid
            else:
                # If mid does NOT work, then every rate <= mid also does not work.
                #
                # Therefore, we must search strictly to the right of mid.
                left = mid + 1

        # When the loop ends, left == right, and that value is the smallest
        # sufficient pump rate.
        return left

    def _can_finish(self, volumes: List[int], h: int, rate: int) -> bool:
        """
        Determine whether all reservoirs can be refilled within h nights
        using the given pump rate.

        Args:
            volumes: A list of reservoir volumes.
            h: The maximum allowed number of nights.
            rate: The candidate pump rate to test.

        Returns:
            True if all reservoirs can be completed within h nights, otherwise False.

        Time Complexity:
            O(n)
            where n is the number of reservoirs.

        Space Complexity:
            O(1)
        """
        # This variable accumulates the total number of nights needed if we use
        # the given pump rate for all reservoirs.
        total_nights: int = 0

        # Process each reservoir independently.
        for volume in volumes:
            # Nights needed for one reservoir is ceil(volume / rate).
            #
            # Instead of importing math.ceil and using division, we use the
            # integer arithmetic formula:
            #
            #     ceil(a / b) = (a + b - 1) // b
            #
            # This is efficient, exact for integers, and commonly used in
            # interview solutions.
            total_nights += (volume + rate - 1) // rate

            # Small optimization:
            # If we already exceed h, we can stop early because this rate is
            # definitely too slow.
            if total_nights > h:
                return False

        # If after processing all reservoirs the total nights is within the
        # allowed limit, then this rate is sufficient.
        return total_nights <= h


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    volumes1: List[int] = [8, 5, 10, 7]
    h1: int = 8
    result1: int = solution.min_pump_rate(volumes1, h1)
    print("Example 1:")
    print(f"volumes = {volumes1}, h = {h1}")
    print(f"Minimum pump rate = {result1}")
    print("Expected = 5")
    print()

    # Example 2
    volumes2: List[int] = [30, 11, 23, 4, 20]
    h2: int = 6
    result2: int = solution.min_pump_rate(volumes2, h2)
    print("Example 2:")
    print(f"volumes = {volumes2}, h = {h2}")
    print(f"Minimum pump rate = {result2}")
    print("Expected = 23")
    print()

    # Additional quick sanity check
    volumes3: List[int] = [1, 1, 1, 1]
    h3: int = 4
    result3: int = solution.min_pump_rate(volumes3, h3)
    print("Additional Test:")
    print(f"volumes = {volumes3}, h = {h3}")
    print(f"Minimum pump rate = {result3}")
    print("Expected = 1")