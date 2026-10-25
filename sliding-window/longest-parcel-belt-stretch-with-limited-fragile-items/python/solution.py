"""
Title: Longest Parcel Belt Stretch With Limited Fragile Items

Problem Description:
A warehouse conveyor belt carries parcels in a fixed order. Each parcel is labeled
with a non-negative integer type code. A parcel is considered fragile if its type
code appears in a given set `fragileTypes`. The operations team wants to identify
the longest contiguous stretch of the belt that can be processed by a standard
robot arm, which may handle at most `k` fragile parcels before requiring a manual
reset.

Given an integer array `parcels`, an integer array `fragileTypes` containing
distinct type codes, and an integer `k`, return the length of the longest
contiguous subarray of `parcels` that contains at most `k` parcels whose type
belongs to `fragileTypes`.

A contiguous stretch means you must choose a single continuous segment of the
array. The fragile type list is not necessarily sorted, and parcel codes may
repeat many times. If no parcel can be included under the limit, return `0`.

Constraints:
- 1 <= parcels.length <= 200000
- 0 <= parcels[i] <= 10^9
- 0 <= fragileTypes.length <= 200000
- fragileTypes contains distinct values
- 0 <= fragileTypes[i] <= 10^9
- 0 <= k <= parcels.length
"""

from typing import List, Set


class Solution:
    def longest_parcel_belt_stretch(
        self, parcels: List[int], fragile_types: List[int], k: int
    ) -> int:
        """
        Find the length of the longest contiguous subarray containing at most
        k fragile parcels.

        Args:
            parcels: List of parcel type codes in conveyor-belt order.
            fragile_types: Distinct parcel type codes considered fragile.
            k: Maximum number of fragile parcels allowed in the chosen stretch.

        Returns:
            The maximum length of a contiguous subarray with at most k fragile parcels.

        Time complexity:
            O(n + m), where n is len(parcels) and m is len(fragile_types)

        Space complexity:
            O(m) for the set of fragile parcel types
        """
        # Convert the fragile type list into a set.
        #
        # Why:
        # - We need to repeatedly ask: "Is this parcel fragile?"
        # - A Python set gives average O(1) membership checks.
        # - This is much faster than checking inside a list every time,
        #   which would be O(m) per lookup.
        fragile_set: Set[int] = set(fragile_types)

        # This will be the left boundary of our sliding window.
        # The current window is always parcels[left:right+1].
        left: int = 0

        # Count how many fragile parcels are currently inside the window.
        fragile_count: int = 0

        # Store the best (maximum) valid window length seen so far.
        best_length: int = 0

        # Expand the window one parcel at a time by moving `right`.
        for right, parcel in enumerate(parcels):
            # If the new parcel entering the window is fragile,
            # increase the fragile count.
            if parcel in fragile_set:
                fragile_count += 1

            # If the window has too many fragile parcels, it is invalid.
            # We must shrink it from the left until it becomes valid again.
            #
            # This while-loop is the heart of the sliding window technique:
            # - expand right to include more elements
            # - shrink left only when constraints are violated
            #
            # Because each index moves at most once from left to right,
            # the total work remains linear.
            while fragile_count > k:
                # If the parcel leaving the window is fragile,
                # reduce the fragile count because it is no longer included.
                if parcels[left] in fragile_set:
                    fragile_count -= 1

                # Move the left boundary rightward to shrink the window.
                left += 1

            # At this point, the window parcels[left:right+1] is valid:
            # it contains at most k fragile parcels.
            #
            # So we can safely compute its length and compare it with the best answer.
            current_length: int = right - left + 1
            if current_length > best_length:
                best_length = current_length

        # Return the maximum valid window length found.
        return best_length

    def longestSubarray(self, parcels: List[int], fragileTypes: List[int], k: int) -> int:
        """
        Compatibility wrapper using a shorter method name often expected in coding platforms.

        Args:
            parcels: List of parcel type codes.
            fragileTypes: Distinct fragile parcel type codes.
            k: Maximum allowed number of fragile parcels.

        Returns:
            The length of the longest valid contiguous subarray.

        Time complexity:
            O(n + m), where n is len(parcels) and m is len(fragileTypes)

        Space complexity:
            O(m)
        """
        return self.longest_parcel_belt_stretch(parcels, fragileTypes, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    parcels_1: List[int] = [4, 7, 2, 7, 9, 3, 7, 5]
    fragile_types_1: List[int] = [7, 9]
    k_1: int = 2
    result_1: int = solution.longest_parcel_belt_stretch(parcels_1, fragile_types_1, k_1)
    print("Example 1 Result:", result_1)  # Expected: 4

    # Example 2
    parcels_2: List[int] = [1, 8, 6, 8, 2, 4, 6]
    fragile_types_2: List[int] = [8, 4]
    k_2: int = 1
    result_2: int = solution.longest_parcel_belt_stretch(parcels_2, fragile_types_2, k_2)
    print("Example 2 Result:", result_2)  # Expected: 3

    # Additional simple sanity checks
    parcels_3: List[int] = [1, 2, 3, 4]
    fragile_types_3: List[int] = []
    k_3: int = 0
    result_3: int = solution.longest_parcel_belt_stretch(parcels_3, fragile_types_3, k_3)
    print("No fragile types Result:", result_3)  # Expected: 4

    parcels_4: List[int] = [5, 5, 5]
    fragile_types_4: List[int] = [5]
    k_4: int = 0
    result_4: int = solution.longest_parcel_belt_stretch(parcels_4, fragile_types_4, k_4)
    print("All fragile, k=0 Result:", result_4)  # Expected: 0