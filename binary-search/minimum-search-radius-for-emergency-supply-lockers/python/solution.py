"""
Title: Minimum Search Radius for Emergency Supply Lockers

Problem Description:
A city is planning emergency supply coverage along a very long straight highway.
There are n neighborhoods located at integer positions in the array `homes`,
and m supply lockers located at integer positions in the array `lockers`.

A neighborhood is considered covered if there exists at least one locker whose
distance from that neighborhood is at most R.

Your task is to find the minimum integer radius R such that every neighborhood
is covered by at least one locker.

The arrays are not guaranteed to be sorted. Positions may be large, and multiple
homes or lockers may share the same position. You should design an algorithm
efficient enough for large inputs. A brute-force comparison of every home with
every locker will be too slow.

Return the smallest possible integer R.

Constraints:
- 1 <= homes.length, lockers.length <= 2 * 10^5
- 0 <= homes[i], lockers[i] <= 10^9
- The answer fits in a 32-bit signed integer.

Example 1:
Input: homes = [2, 10, 14], lockers = [4, 12]
Output: 2

Example 2:
Input: homes = [1, 5, 9, 15], lockers = [6]
Output: 9

Key idea:
- Sort both arrays.
- Binary search the minimum radius R.
- For each candidate R, check coverage efficiently using a two-pointer scan.
"""

from typing import List


class Solution:
    def find_min_radius(self, homes: List[int], lockers: List[int]) -> int:
        """
        Find the minimum integer radius needed so every home is covered
        by at least one locker.

        Args:
            homes: Positions of neighborhoods along the highway.
            lockers: Positions of supply lockers along the highway.

        Returns:
            The smallest integer radius R such that every home is covered.

        Time complexity:
            O((n + m) * log C + n log n + m log m)
            where:
            - n = len(homes)
            - m = len(lockers)
            - C = search range of radius (at most 10^9)

        Space complexity:
            O(1) extra space beyond the sorted arrays if sorting in place.
            In Python, sorting may use implementation-dependent auxiliary space.
        """
        # We sort both arrays because sorted order allows us to:
        # 1. Binary search on the answer.
        # 2. Check whether a radius works using a single left-to-right scan.
        #
        # Without sorting, we would not be able to efficiently match homes to
        # nearby lockers in linear time during each feasibility check.
        homes.sort()
        lockers.sort()

        # The minimum possible radius is 0:
        # this would only work if every home sits exactly on some locker position.
        left: int = 0

        # A safe upper bound is the farthest possible distance between any home
        # and any locker among the extremes of the sorted arrays.
        #
        # Why this works:
        # - If we choose a radius large enough to cover the farthest home from
        #   the nearest extreme locker, then certainly all homes can be covered.
        # - Using max(abs(home - locker)) across extremes gives a valid upper bound.
        right: int = max(
            abs(homes[0] - lockers[0]),
            abs(homes[0] - lockers[-1]),
            abs(homes[-1] - lockers[0]),
            abs(homes[-1] - lockers[-1]),
        )

        # Standard binary search on the answer:
        # - If a radius works, try smaller.
        # - If it does not work, try larger.
        while left < right:
            mid: int = (left + right) // 2

            # Check whether every home can be covered with radius = mid.
            if self._can_cover_all(homes, lockers, mid):
                # mid is sufficient, so the answer is <= mid.
                right = mid
            else:
                # mid is insufficient, so the answer must be > mid.
                left = mid + 1

        # At the end, left == right and points to the smallest working radius.
        return left

    def _can_cover_all(self, homes: List[int], lockers: List[int], radius: int) -> bool:
        """
        Check whether all homes are covered by at least one locker
        using the given radius.

        This method assumes both `homes` and `lockers` are already sorted.

        Args:
            homes: Sorted home positions.
            lockers: Sorted locker positions.
            radius: Candidate coverage radius.

        Returns:
            True if every home is covered, otherwise False.

        Time complexity:
            O(n + m)

        Space complexity:
            O(1)
        """
        # We use a two-pointer technique.
        #
        # Pointer j tracks the current locker we are considering.
        # For each home, we move j forward while the current locker is too far
        # to the left to cover that home.
        #
        # Because both arrays are sorted:
        # - Once a locker is too far left for the current home,
        #   it will also be too far left for all later homes.
        # - Therefore, we never need to move j backward.
        #
        # This gives a linear-time scan instead of checking every home against
        # every locker.
        j: int = 0
        locker_count: int = len(lockers)

        # Process homes from left to right.
        for home in homes:
            # Move the locker pointer forward until:
            # - either we run out of lockers, or
            # - the current locker is no longer too far left to cover `home`.
            #
            # A locker at position lockers[j] can cover `home` if:
            # abs(lockers[j] - home) <= radius
            #
            # If lockers[j] < home - radius, then this locker is strictly left
            # of the left edge of the coverage interval [home - radius, home + radius].
            # So it cannot cover this home or any later home.
            while j < locker_count and lockers[j] < home - radius:
                j += 1

            # If we exhausted all lockers, there is no locker left that could
            # possibly cover this home.
            if j == locker_count:
                return False

            # Now lockers[j] is the first locker that is not too far left.
            # To cover `home`, it must also not be too far right.
            #
            # Since lockers[j] >= home - radius already holds after the while loop,
            # we only need to verify lockers[j] <= home + radius.
            if lockers[j] > home + radius:
                return False

            # If we reach here, this home is covered by lockers[j].
            # We do not advance j automatically, because the same locker may
            # also cover the next home(s), which is efficient and correct.

        # If every home passed the coverage test, the radius works.
        return True


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    homes1: List[int] = [2, 10, 14]
    lockers1: List[int] = [4, 12]
    result1: int = solution.find_min_radius(homes1, lockers1)
    print("Example 1 Result:", result1)  # Expected: 2

    # Example 2
    homes2: List[int] = [1, 5, 9, 15]
    lockers2: List[int] = [6]
    result2: int = solution.find_min_radius(homes2, lockers2)
    print("Example 2 Result:", result2)  # Expected: 9

    # Additional quick sanity checks
    homes3: List[int] = [1, 2, 3]
    lockers3: List[int] = [2]
    result3: int = solution.find_min_radius(homes3, lockers3)
    print("Additional Test 1 Result:", result3)  # Expected: 1

    homes4: List[int] = [1, 2, 3, 4]
    lockers4: List[int] = [1, 4]
    result4: int = solution.find_min_radius(homes4, lockers4)
    print("Additional Test 2 Result:", result4)  # Expected: 1