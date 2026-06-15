"""
Title: Minimum Beacon Radius for City Coverage

Problem Description:
A city has several fixed beacon towers placed along a straight highway. Each tower can
broadcast a signal equally in both directions, and all towers must use the same
broadcast radius R. You are given two integer arrays: houses, where houses[i] is the
position of the i-th house on the highway, and beacons, where beacons[j] is the
position of the j-th beacon tower. A house is considered covered if there exists at
least one beacon whose distance to that house is at most R.

Your task is to return the minimum integer radius R such that every house is covered.

The input arrays are not guaranteed to be sorted. Positions may be negative, zero, or
positive. Multiple houses or beacons may share the same position. You should design an
efficient solution that works for large inputs.

A common approach is to sort the beacon positions and, for each house, determine the
nearest beacon using binary search. The answer is the maximum among these nearest
distances. Equivalent binary-search-on-answer solutions are also acceptable if
implemented efficiently.

Constraints:
- 1 <= houses.length <= 200000
- 1 <= beacons.length <= 200000
- -10^9 <= houses[i], beacons[j] <= 10^9
- The result fits in a 32-bit signed integer

Example 1:
Input: houses = [1, 5, 9], beacons = [2, 8]
Output: 3
Explanation: House 1 is covered by beacon 2 with radius 1. House 5 is distance 3 from
beacon 2 and distance 3 from beacon 8. House 9 is distance 1 from beacon 8. The
smallest radius that covers all houses is 3.

Example 2:
Input: houses = [-4, 0, 7, 15], beacons = [-2, 10]
Output: 5
Explanation: Distances to the nearest beacon are 2, 2, 3, and 5 respectively.
Therefore, the minimum radius required is 5.
"""

from bisect import bisect_left
from typing import List


class Solution:
    def find_radius(self, houses: List[int], beacons: List[int]) -> int:
        """
        Compute the minimum integer radius needed so every house is covered
        by at least one beacon.

        Args:
            houses: Positions of houses along the highway.
            beacons: Positions of beacon towers along the highway.

        Returns:
            The minimum integer radius R such that every house is within
            distance R of at least one beacon.

        Time complexity:
            O(m log m + n log m)
            where n = len(houses), m = len(beacons)

        Space complexity:
            O(m) due to sorting the beacon list
        """
        # We sort beacon positions once so that we can use binary search.
        # Why sorting helps:
        # - For any house position, the nearest beacon must be either:
        #   1) the first beacon not smaller than the house, or
        #   2) the beacon immediately before that one.
        # - Binary search lets us find that split point in O(log m) time.
        sorted_beacons: List[int] = sorted(beacons)

        # This variable will store the final answer.
        # For each house, we compute the distance to its nearest beacon.
        # The required common radius must be large enough for the "worst" house,
        # so we take the maximum nearest distance across all houses.
        required_radius: int = 0

        # Process each house independently.
        for house in houses:
            # bisect_left returns the insertion index where `house` could be placed
            # while keeping sorted_beacons sorted.
            #
            # Example:
            # sorted_beacons = [2, 8], house = 5
            # bisect_left(...) returns 1
            # meaning:
            # - beacon at index 1 (value 8) is the first beacon >= 5
            # - beacon at index 0 (value 2) is the previous beacon < 5
            insertion_index: int = bisect_left(sorted_beacons, house)

            # We now compute the nearest distance from this house to a beacon.
            # Start with a very large number so any real distance will be smaller.
            nearest_distance: int = float("inf")

            # Candidate 1: beacon at insertion_index, if it exists.
            # This is the first beacon that is >= house.
            if insertion_index < len(sorted_beacons):
                right_distance: int = abs(sorted_beacons[insertion_index] - house)
                nearest_distance = min(nearest_distance, right_distance)

            # Candidate 2: beacon just before insertion_index, if it exists.
            # This is the largest beacon that is < house.
            if insertion_index > 0:
                left_distance: int = abs(sorted_beacons[insertion_index - 1] - house)
                nearest_distance = min(nearest_distance, left_distance)

            # The global radius must cover this house too, so update the maximum.
            required_radius = max(required_radius, nearest_distance)

        return required_radius

    def findRadius(self, houses: List[int], beacons: List[int]) -> int:
        """
        Compatibility wrapper using camelCase naming.

        Args:
            houses: Positions of houses along the highway.
            beacons: Positions of beacon towers along the highway.

        Returns:
            The minimum integer radius needed to cover all houses.

        Time complexity:
            O(m log m + n log m)
            where n = len(houses), m = len(beacons)

        Space complexity:
            O(m)
        """
        return self.find_radius(houses, beacons)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement:
    # houses = [1, 5, 9], beacons = [2, 8]
    # Nearest distances:
    # house 1 -> min(|1-2|, |1-8|) = 1
    # house 5 -> min(|5-2|, |5-8|) = 3
    # house 9 -> min(|9-2|, |9-8|) = 1
    # answer = max(1, 3, 1) = 3
    houses1: List[int] = [1, 5, 9]
    beacons1: List[int] = [2, 8]
    result1: int = solution.find_radius(houses1, beacons1)
    print("Example 1 Result:", result1)  # Expected: 3

    # Example 2 from the problem statement:
    # houses = [-4, 0, 7, 15], beacons = [-2, 10]
    # Nearest distances:
    # house -4 -> 2
    # house 0  -> 2
    # house 7  -> 3
    # house 15 -> 5
    # answer = 5
    houses2: List[int] = [-4, 0, 7, 15]
    beacons2: List[int] = [-2, 10]
    result2: int = solution.find_radius(houses2, beacons2)
    print("Example 2 Result:", result2)  # Expected: 5

    # Additional quick sanity checks:
    houses3: List[int] = [1, 2, 3]
    beacons3: List[int] = [2]
    print("Additional Test 1 Result:", solution.find_radius(houses3, beacons3))  # Expected: 1

    houses4: List[int] = [10]
    beacons4: List[int] = [10]
    print("Additional Test 2 Result:", solution.find_radius(houses4, beacons4))  # Expected: 0