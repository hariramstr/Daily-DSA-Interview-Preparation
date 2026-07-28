"""
Title: Minimum Refill Rate for a Timed Irrigation Plan

Problem Description:
A farm manager needs to water fields in a fixed order using a mobile tank. The tank is
refilled to the same amount at the start of each day, and the manager waters consecutive
fields from left to right. Each field i requires water[i] liters, and splitting a single
field across multiple days is not allowed. If the next field does not fit in the remaining
water for the current day, the manager must stop for that day and continue from the next
field on the following day with a freshly refilled tank.

Given an array water where water[i] is the amount of water needed for the i-th field,
and an integer days, return the minimum tank refill capacity needed so that all fields
can be watered within at most days days.

The order of fields cannot be changed. Every day starts with a full tank of the chosen
capacity. A capacity is feasible if the full watering plan can be completed in days or fewer.

This problem is designed for an efficient solution using binary search on the answer.
The key observation is that if a certain tank capacity works, then any larger capacity
will also work.

Constraints:
- 1 <= water.length <= 100000
- 1 <= water[i] <= 1000000000
- 1 <= days <= water.length

Example 1:
Input: water = [7,2,5,10,8], days = 2
Output: 18
Explanation: With capacity 18, day 1 can water fields [7,2,5] and day 2 can water [10,8].
Any capacity smaller than 18 would require more than 2 days.

Example 2:
Input: water = [3,1,4,1,5,9], days = 3
Output: 9
Explanation: One optimal schedule is [3,1,4], [1,5], [9]. Capacity 8 is not enough because
the field requiring 9 liters cannot be split across days.
"""

from typing import List


class Solution:
    def _days_needed(self, water: List[int], capacity: int) -> int:
        """
        Compute how many days are required if the tank capacity is fixed.

        Args:
            water: List of water requirements for each field in fixed order.
            capacity: Candidate tank capacity to test.

        Returns:
            The number of days needed to water all fields without splitting any field.

        Time complexity:
            O(n), where n is the number of fields.

        Space complexity:
            O(1), excluding input storage.
        """
        # We always need at least one day if there is at least one field.
        days_used: int = 1

        # This variable tracks how much water has already been used on the current day.
        current_load: int = 0

        # Process fields from left to right because the order cannot be changed.
        for amount in water:
            # If adding the current field would exceed the tank capacity,
            # we must start a new day.
            #
            # Why this is correct:
            # - We are not allowed to split a field across days.
            # - We must preserve the original order.
            # - Therefore, once the current field does not fit, the only valid action
            #   is to stop the current day and place this field at the start of the next day.
            if current_load + amount > capacity:
                days_used += 1
                current_load = amount
            else:
                # Otherwise, the field fits in the current day, so we keep filling
                # the current day's schedule.
                current_load += amount

        return days_used

    def min_refill_capacity(self, water: List[int], days: int) -> int:
        """
        Find the minimum tank refill capacity needed to finish watering within the given days.

        Args:
            water: List of water requirements for each field in fixed order.
            days: Maximum number of days allowed.

        Returns:
            The minimum feasible tank capacity.

        Time complexity:
            O(n log S), where n is the number of fields and
            S = sum(water) - max(water) + 1 is the search range size.

        Space complexity:
            O(1), excluding input storage.
        """
        # -----------------------------
        # Step 1: Establish binary search bounds
        # -----------------------------
        #
        # Lower bound:
        # The capacity must be at least the largest single field requirement,
        # because a field cannot be split across multiple days.
        #
        # Example:
        # If one field needs 10 liters, then any capacity below 10 is impossible.
        left: int = max(water)

        # Upper bound:
        # The capacity can always be the sum of all fields, which means
        # everything can be watered in one day.
        #
        # This is definitely feasible, so it is a safe upper bound.
        right: int = sum(water)

        # -----------------------------
        # Step 2: Binary search for the smallest feasible capacity
        # -----------------------------
        #
        # Why binary search works:
        # - If a capacity C is feasible, then any capacity larger than C is also feasible.
        # - If a capacity C is not feasible, then any capacity smaller than C is also not feasible.
        #
        # This monotonic behavior is exactly what binary search needs.
        while left < right:
            # Choose the middle capacity to test.
            mid: int = (left + right) // 2

            # Determine how many days this capacity would require.
            required_days: int = self._days_needed(water, mid)

            # If this capacity works within the allowed number of days,
            # then it is feasible. But we still want the minimum feasible capacity,
            # so we continue searching on the left half, including mid.
            if required_days <= days:
                right = mid
            else:
                # If it needs too many days, the capacity is too small.
                # Therefore, we must search strictly larger capacities.
                left = mid + 1

        # At the end of binary search, left == right and points to
        # the smallest feasible capacity.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    water_1: List[int] = [7, 2, 5, 10, 8]
    days_1: int = 2
    result_1: int = solution.min_refill_capacity(water_1, days_1)
    print(f"water = {water_1}, days = {days_1}")
    print(f"Minimum refill capacity: {result_1}")
    print("Expected: 18")
    print()

    # Example 2
    water_2: List[int] = [3, 1, 4, 1, 5, 9]
    days_2: int = 3
    result_2: int = solution.min_refill_capacity(water_2, days_2)
    print(f"water = {water_2}, days = {days_2}")
    print(f"Minimum refill capacity: {result_2}")
    print("Expected: 9")
    print()

    # Additional beginner-friendly checks
    extra_water_1: List[int] = [1, 2, 3, 4, 5]
    extra_days_1: int = 5
    extra_result_1: int = solution.min_refill_capacity(extra_water_1, extra_days_1)
    print(f"water = {extra_water_1}, days = {extra_days_1}")
    print(f"Minimum refill capacity: {extra_result_1}")
    print("Expected: 5")
    print()

    extra_water_2: List[int] = [1, 2, 3, 4, 5]
    extra_days_2: int = 1
    extra_result_2: int = solution.min_refill_capacity(extra_water_2, extra_days_2)
    print(f"water = {extra_water_2}, days = {extra_days_2}")
    print(f"Minimum refill capacity: {extra_result_2}")
    print("Expected: 15")