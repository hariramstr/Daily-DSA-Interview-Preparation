"""
Title: Minimum Oven Temperature for Batch Baking

Problem Description:
A bakery needs to finish several trays of pastries before the shop opens.
You are given an array `batches`, where `batches[i]` is the number of pastries
in the `i`th tray, and an integer `hours` representing the total number of whole
hours available.

The bakery uses a programmable oven that can be set to a single integer
temperature level `t` for the entire night.

If the oven is set to temperature `t`, then tray `i` takes `ceil(batches[i] / t)`
hours to finish because higher temperature bakes more pastries per hour.
Trays are baked one after another, not in parallel.

Return the minimum integer temperature `t` such that all trays can be completed
within `hours` hours.

Key observation:
- If a temperature `t` is enough to finish within `hours`, then any larger
  temperature will also be enough.
- This creates a monotonic condition, which makes binary search over the answer
  space the ideal approach.

Constraints:
- 1 <= batches.length <= 100000
- 1 <= batches[i] <= 1000000000
- batches.length <= hours <= 1000000000
- 1 <= t <= max(batches)

Example 1:
Input: batches = [12, 7, 18, 5], hours = 10
Output: 5

Example 2:
Input: batches = [30, 11, 23, 4, 20], hours = 6
Output: 23
"""

from typing import List


class Solution:
    def _hours_needed(self, batches: List[int], temperature: int) -> int:
        """
        Compute the total number of hours needed to bake all trays at a given temperature.

        Args:
            batches: A list where each value is the number of pastries in one tray.
            temperature: The oven temperature level being tested.

        Returns:
            The total whole hours required to finish all trays at this temperature.

        Time complexity:
            O(n), where n is the number of trays.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We accumulate the total time needed across all trays.
        total_hours: int = 0

        # Each tray is processed one after another.
        # For a tray with size `batch`, the required time is:
        #   ceil(batch / temperature)
        #
        # In integer arithmetic, we can compute ceil(a / b) as:
        #   (a + b - 1) // b
        #
        # This avoids floating-point math and is both faster and exact.
        for batch in batches:
            total_hours += (batch + temperature - 1) // temperature

        return total_hours

    def min_oven_temperature(self, batches: List[int], hours: int) -> int:
        """
        Find the minimum integer oven temperature that allows all trays to finish within the given hours.

        Args:
            batches: A list where each value is the number of pastries in one tray.
            hours: The maximum total whole hours available.

        Returns:
            The smallest integer temperature that completes all trays within `hours`.

        Time complexity:
            O(n log m), where:
            - n is the number of trays
            - m is max(batches), the search range for the answer

        Space complexity:
            O(1), ignoring input storage.
        """
        # -----------------------------
        # Why binary search works here:
        # -----------------------------
        # Define a condition:
        #   "Is temperature t sufficient?"
        #
        # If temperature t is sufficient, then any temperature larger than t
        # will also be sufficient, because increasing temperature can only
        # reduce or keep the same the required hours for each tray.
        #
        # That means the answer space looks like:
        #   [False False False ... False True True True ... True]
        #
        # We want the FIRST True value, which is a classic binary search pattern.

        # The smallest possible temperature is 1.
        left: int = 1

        # The largest temperature we ever need to test is max(batches).
        # At that temperature, every tray takes exactly 1 hour, because
        # ceil(batch / max_batch) is at most 1 for each tray.
        #
        # Since the problem guarantees that some answer exists and
        # hours >= len(batches), this upper bound is always sufficient.
        right: int = max(batches)

        # We keep shrinking the search interval until left == right.
        # At the end, that value will be the minimum sufficient temperature.
        while left < right:
            # Midpoint of the current search range.
            # Using this formula avoids overflow in languages with fixed-size ints.
            # Python integers do not overflow here, but this is still a best practice.
            mid: int = left + (right - left) // 2

            # Compute how many hours are needed if we choose temperature = mid.
            needed: int = self._hours_needed(batches, mid)

            # If this temperature is fast enough, it might be the answer,
            # but there could still be a smaller sufficient temperature.
            # So we keep the left half, including mid.
            if needed <= hours:
                right = mid
            else:
                # Otherwise, mid is too slow, so the answer must be larger.
                # We discard mid and everything below it.
                left = mid + 1

        # When the loop ends, left == right and points to the smallest
        # sufficient temperature.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    batches_1: List[int] = [12, 7, 18, 5]
    hours_1: int = 10
    result_1: int = solution.min_oven_temperature(batches_1, hours_1)
    print("Example 1:")
    print(f"batches = {batches_1}, hours = {hours_1}")
    print(f"Minimum oven temperature = {result_1}")
    print()

    # Example 2
    batches_2: List[int] = [30, 11, 23, 4, 20]
    hours_2: int = 6
    result_2: int = solution.min_oven_temperature(batches_2, hours_2)
    print("Example 2:")
    print(f"batches = {batches_2}, hours = {hours_2}")
    print(f"Minimum oven temperature = {result_2}")
    print()

    # Additional quick sanity checks for beginner-friendly demonstration.
    extra_batches: List[int] = [3, 6, 7, 11]
    extra_hours: int = 8
    extra_result: int = solution.min_oven_temperature(extra_batches, extra_hours)
    print("Additional Example:")
    print(f"batches = {extra_batches}, hours = {extra_hours}")
    print(f"Minimum oven temperature = {extra_result}")