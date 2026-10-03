"""
Title: Minimum Reading Light Radius for Library Tables

Problem Description:
A long library hallway is modeled as a number line. Some positions contain reading lights,
and some positions contain study tables. Each light illuminates every table within distance r
from its position, where r is the same for all lights. You are given two integer arrays:
lights and tables, representing their positions along the hallway. Your task is to find the
minimum integer radius r such that every table is illuminated by at least one light.

A table at position t is illuminated if there exists a light at position l with |l - t| <= r.

Return the smallest possible radius.

This problem is intended to be solved efficiently for large inputs. A brute-force comparison
of every table with every light will be too slow. Think about how sorted positions and binary
search can help determine whether a given radius is sufficient, or how to directly find the
nearest light for each table.

Constraints:
- 1 <= lights.length, tables.length <= 2 * 10^5
- -10^9 <= lights[i], tables[i] <= 10^9
- Positions are not guaranteed to be distinct
- The answer fits in a 32-bit signed integer

Example 1:
Input: lights = [2, 10], tables = [1, 5, 11]
Output: 3
Explanation: With radius 3, the light at 2 covers table 1 and 5, and the light at 10 covers
table 11. Radius 2 is not enough because table 5 would be too far from both lights.

Example 2:
Input: lights = [-4, 0, 8], tables = [-7, -1, 3, 10]
Output: 3
Explanation: Table -7 is 3 units from light -4, table -1 is 1 unit from light 0, table 3 is
3 units from light 0, and table 10 is 2 units from light 8. Therefore, radius 3 is sufficient
and minimal.
"""

from bisect import bisect_left
from typing import List


class Solution:
    def find_min_radius(self, lights: List[int], tables: List[int]) -> int:
        """
        Compute the minimum integer radius needed so every table is illuminated.

        The idea is:
        1. Sort the light positions.
        2. For each table, use binary search to find where that table would be inserted
           among the sorted lights.
        3. The nearest light must be either:
           - the light immediately to the left of that insertion position, or
           - the light at that insertion position (the first light not smaller than the table)
        4. The distance from the table to its nearest light is the minimum of those candidates.
        5. Since one global radius must work for all tables, the answer is the maximum of these
           nearest-light distances across all tables.

        Args:
            lights: Positions of reading lights along the hallway.
            tables: Positions of study tables along the hallway.

        Returns:
            The smallest integer radius that allows every table to be illuminated.

        Time complexity:
            O(L log L + T log L), where L is len(lights) and T is len(tables).

        Space complexity:
            O(L) in Python due to sorting creating a new list if copied, or O(1) extra
            beyond the sorted storage if sorting in place is considered.
        """
        # We sort the light positions because binary search only works on sorted data.
        # Once sorted, we can very quickly locate the closest light to any table.
        sorted_lights: List[int] = sorted(lights)

        # This variable will store the final answer.
        # For each table, we compute the distance to its nearest light.
        # The required radius must be at least that distance.
        # Therefore, the final minimum valid radius is the maximum such distance.
        minimum_required_radius: int = 0

        # We now process each table independently.
        # For every table position, we want the nearest light.
        for table_position in tables:
            # bisect_left returns the index where table_position could be inserted
            # while keeping sorted_lights in sorted order.
            #
            # This gives us a very useful split:
            # - sorted_lights[insertion_index - 1] is the closest candidate on the left
            # - sorted_lights[insertion_index] is the closest candidate on the right
            insertion_index: int = bisect_left(sorted_lights, table_position)

            # Start with "infinity" so we can safely minimize against real distances.
            # We use float("inf") because sometimes only one side exists:
            # - if the table is left of all lights, there is no left candidate
            # - if the table is right of all lights, there is no right candidate
            nearest_distance: int = float("inf")

            # Check the light on the left side, if it exists.
            # This is the greatest light position strictly less than the insertion point.
            if insertion_index > 0:
                left_light_position: int = sorted_lights[insertion_index - 1]
                distance_to_left_light: int = abs(table_position - left_light_position)
                nearest_distance = min(nearest_distance, distance_to_left_light)

            # Check the light on the right side, if it exists.
            # This is the first light position greater than or equal to the table position.
            if insertion_index < len(sorted_lights):
                right_light_position: int = sorted_lights[insertion_index]
                distance_to_right_light: int = abs(right_light_position - table_position)
                nearest_distance = min(nearest_distance, distance_to_right_light)

            # The current table needs at least "nearest_distance" radius to be covered.
            # Since one single radius must work for every table, we keep the maximum.
            minimum_required_radius = max(minimum_required_radius, nearest_distance)

        return minimum_required_radius

    def findRadius(self, lights: List[int], tables: List[int]) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            lights: Positions of reading lights.
            tables: Positions of study tables.

        Returns:
            The minimum radius needed to illuminate all tables.

        Time complexity:
            O(L log L + T log L), where L is len(lights) and T is len(tables).

        Space complexity:
            O(L) due to sorting storage behavior.
        """
        return self.find_min_radius(lights, tables)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    lights_1: List[int] = [2, 10]
    tables_1: List[int] = [1, 5, 11]
    result_1: int = solution.find_min_radius(lights_1, tables_1)
    print("Example 1:")
    print("lights =", lights_1)
    print("tables =", tables_1)
    print("Minimum radius =", result_1)
    print("Expected = 3")
    print()

    # Example 2
    lights_2: List[int] = [-4, 0, 8]
    tables_2: List[int] = [-7, -1, 3, 10]
    result_2: int = solution.find_min_radius(lights_2, tables_2)
    print("Example 2:")
    print("lights =", lights_2)
    print("tables =", tables_2)
    print("Minimum radius =", result_2)
    print("Expected = 3")
    print()

    # Additional quick sanity check
    lights_3: List[int] = [5]
    tables_3: List[int] = [5, 6, 1]
    result_3: int = solution.find_min_radius(lights_3, tables_3)
    print("Additional Test:")
    print("lights =", lights_3)
    print("tables =", tables_3)
    print("Minimum radius =", result_3)
    print("Expected = 4")