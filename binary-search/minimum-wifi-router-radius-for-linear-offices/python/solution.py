"""
Title: Minimum WiFi Router Radius for Linear Offices

Problem Description:
A company has rented a long hallway with offices placed along a straight line.
The positions of the offices are given in a sorted integer array `offices`,
where `offices[i]` is the coordinate of the i-th office.

The company can install WiFi routers only at locations listed in another sorted
integer array `routers`, where `routers[j]` is the coordinate of a possible
router location.

Every installed router uses the same signal radius `r`, and it covers every
office whose distance from that router is at most `r`.

You may use any number of the available router locations, including all of them.
Your task is to compute the minimum integer radius `r` such that every office is
covered by at least one router.

Return that minimum radius.

A solution is expected to use binary search efficiently rather than checking
every radius one by one. For a candidate radius, you can determine whether all
offices are coverable by checking the nearest router position for each office.

Constraints:
- 1 <= offices.length, routers.length <= 2 * 10^5
- 0 <= offices[i], routers[j] <= 10^9
- offices is sorted in non-decreasing order
- routers is sorted in non-decreasing order
- The answer fits in a 32-bit signed integer

Example 1:
Input: offices = [1, 5, 9], routers = [2, 8]
Output: 3

Example 2:
Input: offices = [2, 4, 6, 14], routers = [1, 7, 15]
Output: 2
"""

from bisect import bisect_left
from typing import List


class Solution:
    def can_cover_all(self, offices: List[int], routers: List[int], radius: int) -> bool:
        """
        Check whether every office can be covered using the given router radius.

        For each office, we find the insertion position of that office in the
        sorted routers array. That gives us the closest router on the right
        (if it exists) and the closest router on the left (if it exists).
        If either of those routers is within `radius`, then that office is covered.

        Args:
            offices: Sorted list of office positions.
            routers: Sorted list of possible router positions.
            radius: Candidate signal radius to test.

        Returns:
            True if all offices are covered by at least one router, otherwise False.

        Time complexity:
            O(n log m), where n = len(offices), m = len(routers)

        Space complexity:
            O(1) extra space
        """
        # We process each office independently.
        # Because routers is sorted, binary search lets us quickly locate where
        # the office would fit among router positions.
        for office in offices:
            # Find the first router position that is >= office.
            # This gives us the nearest router on the right side, if such a router exists.
            index: int = bisect_left(routers, office)

            # We will track whether this office is covered by a nearby router.
            covered: bool = False

            # Check the router on the right side.
            # If index is within bounds, routers[index] exists and is the first router
            # that is not smaller than the office position.
            if index < len(routers):
                if abs(routers[index] - office) <= radius:
                    covered = True

            # Check the router on the left side.
            # If index > 0, then routers[index - 1] exists and is the largest router
            # position that is smaller than the office position.
            if not covered and index > 0:
                if abs(routers[index - 1] - office) <= radius:
                    covered = True

            # If neither nearest candidate router can cover this office,
            # then this radius is not sufficient.
            if not covered:
                return False

        # If we never found an uncovered office, then all offices are coverable.
        return True

    def minimum_radius(self, offices: List[int], routers: List[int]) -> int:
        """
        Compute the minimum integer radius needed so that all offices are covered.

        We use binary search on the answer:
        - If a radius works, then any larger radius also works.
        - If a radius does not work, then any smaller radius also does not work.

        This monotonic property makes binary search the correct and efficient choice.

        Args:
            offices: Sorted list of office positions.
            routers: Sorted list of possible router positions.

        Returns:
            The minimum integer radius that covers every office.

        Time complexity:
            O(n log m log R), where:
            - n = len(offices)
            - m = len(routers)
            - R is the search range of possible radius values

        Space complexity:
            O(1) extra space
        """
        # The smallest possible radius is 0.
        left: int = 0

        # A safe upper bound is the maximum possible coordinate difference.
        # Since coordinates are in [0, 10^9], using 10^9 is valid and simple.
        # This guarantees the true answer lies within [0, 10^9].
        right: int = 10**9

        # Standard binary search for the minimum feasible value.
        while left < right:
            # Midpoint radius to test.
            mid: int = (left + right) // 2

            # If this radius can cover all offices, try to find an even smaller one.
            if self.can_cover_all(offices, routers, mid):
                right = mid
            else:
                # Otherwise, this radius is too small, so search larger radii.
                left = mid + 1

        # At loop end, left == right and points to the minimum feasible radius.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    offices_1: List[int] = [1, 5, 9]
    routers_1: List[int] = [2, 8]
    result_1: int = solution.minimum_radius(offices_1, routers_1)
    print("Example 1 Result:", result_1)  # Expected: 3

    # Example 2
    offices_2: List[int] = [2, 4, 6, 14]
    routers_2: List[int] = [1, 7, 15]
    result_2: int = solution.minimum_radius(offices_2, routers_2)
    print("Example 2 Result:", result_2)  # Expected: 2