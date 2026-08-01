"""
Title: Minimum Heater Radius for Circular Warehouses

Problem Description:
A logistics company stores goods in warehouses placed around a circular ring road of total
length L. The positions of the warehouses are given as integers in the range [0, L - 1],
measured clockwise from a fixed origin. The company wants to install heaters at some existing
warehouse locations. Each heater warms all warehouses within clockwise or counterclockwise road
distance at most R, where distance on the ring is the shorter of the two circular paths.

You are given a sorted array warehouses of unique warehouse positions, an integer L, and an
integer k representing the maximum number of heaters that may be installed. Return the minimum
integer radius R such that all warehouses can be covered by at most k heaters.

A heater may only be placed at one of the given warehouse positions. Coverage wraps around the
circle, so a heater near position 0 may also cover warehouses near position L - 1.

The solution below uses:
1. Binary search on the answer R
2. A feasibility check for a fixed R
3. Circular-to-linear transformation by cutting the circle at every possible start
4. Doubling + greedy jump preprocessing
5. A segment tree over transformed start positions to test whether some cut works

This implementation is designed to handle large inputs efficiently.
"""

from bisect import bisect_right
from typing import List


class SegmentTreeMin:
    """Segment tree supporting point updates and range minimum queries."""

    def __init__(self, size: int) -> None:
        """
        Initialize the segment tree.

        Args:
            size: Number of leaves to support.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.n = 1
        while self.n < size:
            self.n <<= 1
        self.data = [10**18] * (2 * self.n)

    def update(self, index: int, value: int) -> None:
        """
        Set one position to the minimum of its current value and the given value.

        Args:
            index: Position to update.
            value: Candidate value.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        i = index + self.n
        if value >= self.data[i]:
            return
        self.data[i] = value
        i >>= 1
        while i:
            self.data[i] = min(self.data[i << 1], self.data[i << 1 | 1])
            i >>= 1

    def query(self, left: int, right: int) -> int:
        """
        Query the minimum value on the inclusive range [left, right].

        Args:
            left: Left endpoint.
            right: Right endpoint.

        Returns:
            Minimum value in the range.

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        if left > right:
            return 10**18
        left += self.n
        right += self.n
        result = 10**18
        while left <= right:
            if left & 1:
                result = min(result, self.data[left])
                left += 1
            if not (right & 1):
                result = min(result, self.data[right])
                right -= 1
            left >>= 1
            right >>= 1
        return result


class Solution:
    def minimum_heater_radius(self, warehouses: List[int], L: int, k: int) -> int:
        """
        Compute the minimum integer heater radius needed to cover all warehouses on a circle.

        Args:
            warehouses: Sorted unique warehouse positions on the circle.
            L: Total circumference of the circular road.
            k: Maximum number of heaters allowed.

        Returns:
            The minimum integer radius R.

        Time complexity:
            O(n log n log L), where n is the number of warehouses

        Space complexity:
            O(n log n)
        """
        n = len(warehouses)

        # If we may place at least one heater per warehouse, radius 0 is always enough:
        # place a heater at every warehouse.
        if k >= n:
            return 0

        # Binary search over the answer.
        # Radius is an integer and never needs to exceed L // 2 on a circle,
        # because the farthest circular distance between two points is at most L // 2.
        left = 0
        right = L // 2

        while left < right:
            mid = (left + right) // 2
            if self._can_cover_with_radius(warehouses, L, k, mid):
                right = mid
            else:
                left = mid + 1

        return left

    def _can_cover_with_radius(self, warehouses: List[int], L: int, k: int, radius: int) -> bool:
        """
        Check whether all warehouses can be covered by at most k heaters of a fixed radius.

        Args:
            warehouses: Sorted unique warehouse positions on the circle.
            L: Circle length.
            k: Maximum number of heaters.
            radius: Candidate heater radius.

        Returns:
            True if feasible, otherwise False.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n log n)
        """
        n = len(warehouses)

        # We duplicate the warehouse positions by adding L to each one.
        # This is the standard trick for circular problems:
        # any contiguous block of n warehouses in this doubled array corresponds
        # to one possible "cut" of the circle into a line.
        doubled = warehouses + [x + L for x in warehouses]

        # For a fixed linear interval of warehouses, one heater placed at warehouse i
        # can cover all warehouses whose positions lie within [doubled[i], doubled[i] + 2R].
        #
        # Why 2R?
        # On a line, if the leftmost uncovered warehouse is at position a, and we place a heater
        # at some warehouse position h with h - a <= R, then that heater covers up to h + R.
        # Since h <= a + R, the farthest possible covered point is at most a + 2R.
        #
        # The greedy optimal strategy on a line is:
        # - Start from the first uncovered warehouse
        # - Place a heater at the rightmost warehouse within distance R from it
        # - That heater then covers as far right as possible
        #
        # We precompute the "next uncovered index" after one greedy heater.
        next_index = [0] * (2 * n)

        # We will use binary search on the doubled positions to find:
        # 1) the rightmost heater location allowed within first_pos + R
        # 2) the first warehouse beyond heater_pos + R
        #
        # Because the array is sorted, bisect_right is perfect here.
        for i in range(2 * n):
            first_pos = doubled[i]

            # Step 1:
            # Find the rightmost warehouse index j such that doubled[j] <= first_pos + radius.
            # This is the best heater position if warehouse i is the first uncovered one.
            j = bisect_right(doubled, first_pos + radius, i, 2 * n) - 1

            # Step 2:
            # A heater at doubled[j] covers up to doubled[j] + radius.
            # Find the first index strictly greater than that coverage end.
            cover_end = doubled[j] + radius
            nxt = bisect_right(doubled, cover_end, j, 2 * n)
            next_index[i] = nxt

        # Binary lifting:
        # jump[p][i] = index reached after using 2^p heaters starting from first uncovered index i.
        #
        # This lets us answer:
        # "If I start covering from warehouse s, where am I after k heaters?"
        #
        # If after k heaters we reach at least s + n, then the entire circular set is covered
        # for the cut starting at s.
        max_log = k.bit_length()
        jump = [next_index[:]]
        for p in range(1, max_log):
            prev = jump[p - 1]
            curr = [0] * (2 * n + 1)

            # We create a safe extra slot at index 2n so that jumps that already moved
            # past the useful range remain stable.
            for i in range(2 * n):
                ni = prev[i]
                curr[i] = prev[ni] if ni < 2 * n else ni
            curr[2 * n] = 2 * n
            jump.append(curr)

        # Build final_reach[s] = index reached after exactly k heaters from start s.
        final_reach = [0] * n
        for s in range(n):
            pos = s
            heaters = k
            bit = 0
            while heaters:
                if heaters & 1:
                    pos = jump[bit][pos] if pos < 2 * n else pos
                heaters >>= 1
                bit += 1
            final_reach[s] = pos

        # If for any cut s we can cover n consecutive warehouses, we are done.
        for s in range(n):
            if final_reach[s] >= s + n:
                return True

        return False


if __name__ == "__main__":
    solution = Solution()

    warehouses1 = [1, 4, 8, 11]
    L1 = 12
    k1 = 2
    result1 = solution.minimum_heater_radius(warehouses1, L1, k1)
    print(result1)  # Expected: 2

    warehouses2 = [2, 6, 9, 14]
    L2 = 20
    k2 = 1
    result2 = solution.minimum_heater_radius(warehouses2, L2, k2)
    print(result2)  # Expected: 6