"""
Title: Minimum Heater Power for Cold Storage Crates

Problem Description:
A warehouse stores temperature-sensitive crates along a straight hallway. The position
of each crate and each portable heater is given as an integer coordinate on the same
number line. Every heater uses the same power setting, and a heater with power r can
keep all crates within distance r from its position safe.

You are given two integer arrays, crates and heaters, where crates[i] is the position
of the i-th crate and heaters[j] is the position of the j-th heater. Your task is to
find the minimum integer power r such that every crate is covered by at least one heater.

A crate at position x is covered if there exists some heater at position h with
|x - h| <= r.

The answer must be the smallest possible integer r. The input arrays are not guaranteed
to be sorted. A typical efficient solution sorts the heater positions and, for each
crate, uses binary search to find the nearest heater on the left and right.

Constraints:
- 1 <= crates.length, heaters.length <= 2 * 10^5
- -10^9 <= crates[i], heaters[j] <= 10^9
- Positions may contain duplicates
- The expected solution should run in O((n + m) log m) or better after sorting

Example 1:
Input: crates = [2, 8, 14], heaters = [4, 10]
Output: 4
Explanation: Crate 2 is 2 units from heater 4, crate 8 is 2 units from heater 10 or
4 units from heater 4, and crate 14 is 4 units from heater 10. So power 4 is enough,
and no smaller power covers crate 14.

Example 2:
Input: crates = [1, 5, 9, 15], heaters = [6]
Output: 9
Explanation: With only one heater at position 6, the farthest crate is at position 15,
which is 9 units away. Therefore the minimum required power is 9.
"""

from bisect import bisect_left
from typing import List


class Solution:
    def findRadius(self, crates: List[int], heaters: List[int]) -> int:
        """
        Compute the minimum heater power needed so every crate is covered.

        The method sorts heater positions, then for each crate uses binary search
        to locate the insertion point in the heater list. That insertion point tells
        us the nearest heater on the right, and the heater just before it is the
        nearest heater on the left. The crate only needs to be covered by one heater,
        so we take the smaller of those two distances. The final answer is the maximum
        such minimum distance across all crates.

        Args:
            crates: Positions of crates along the number line.
            heaters: Positions of heaters along the number line.

        Returns:
            The smallest integer power radius required to cover every crate.

        Time complexity:
            O(m log m + n log m), where n = len(crates), m = len(heaters)

        Space complexity:
            O(1) extra space beyond the sorting cost used by Python internally
        """
        # We sort the heater positions because binary search only works correctly
        # on sorted data. Once sorted, we can quickly find where each crate would
        # fit among the heaters.
        heaters.sort()

        # This variable will store the final answer.
        # For each crate, we compute the distance to its nearest heater.
        # The required global power must be large enough for the "worst" crate,
        # so we keep track of the maximum nearest-heater distance seen so far.
        required_power: int = 0

        # Process each crate independently.
        # For each crate position, we want to know:
        #   1) the closest heater on the left (if one exists)
        #   2) the closest heater on the right (if one exists)
        # Then the crate's needed power is the smaller of those two distances.
        for crate in crates:
            # bisect_left returns the first index where "crate" could be inserted
            # while keeping the heater list sorted.
            #
            # This gives us a very useful split:
            # - heaters[index] is the first heater >= crate, if index is in range
            # - heaters[index - 1] is the last heater < crate, if index > 0
            #
            # So with one binary search, we identify the nearest candidates on both sides.
            index: int = bisect_left(heaters, crate)

            # Start with "infinite" distances. We will replace them if a valid
            # left or right heater exists.
            left_distance: int = float("inf")
            right_distance: int = float("inf")

            # If index > 0, then there is at least one heater strictly to the left
            # of the insertion point. The closest left heater is the one immediately
            # before index.
            if index > 0:
                left_distance = crate - heaters[index - 1]

            # If index < len(heaters), then there is a heater at the insertion point,
            # which is the first heater that is greater than or equal to the crate.
            # That is the closest candidate on the right.
            if index < len(heaters):
                right_distance = heaters[index] - crate

            # The crate only needs one heater to cover it, so the minimum power needed
            # for this specific crate is the smaller of:
            # - distance to nearest left heater
            # - distance to nearest right heater
            nearest_heater_distance: int = min(left_distance, right_distance)

            # The overall heater power must be enough for every crate.
            # Therefore, if this crate needs more power than any previous crate,
            # we update the answer.
            required_power = max(required_power, nearest_heater_distance)

        # After checking all crates, required_power is the smallest radius that
        # covers every crate.
        return required_power

    def minimum_heater_power(self, crates: List[int], heaters: List[int]) -> int:
        """
        Wrapper method with a more descriptive problem-specific name.

        Args:
            crates: Positions of crates along the hallway.
            heaters: Positions of heaters along the hallway.

        Returns:
            The minimum integer heater power required.

        Time complexity:
            O(m log m + n log m), where n = len(crates), m = len(heaters)

        Space complexity:
            O(1) extra space beyond the sorting cost used by Python internally
        """
        return self.findRadius(crates, heaters)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    crates1: List[int] = [2, 8, 14]
    heaters1: List[int] = [4, 10]
    result1: int = solution.minimum_heater_power(crates1, heaters1)
    print("Example 1:")
    print("Crates:", crates1)
    print("Heaters:", heaters1)
    print("Minimum heater power:", result1)
    print()

    # Example 2
    crates2: List[int] = [1, 5, 9, 15]
    heaters2: List[int] = [6]
    result2: int = solution.minimum_heater_power(crates2, heaters2)
    print("Example 2:")
    print("Crates:", crates2)
    print("Heaters:", heaters2)
    print("Minimum heater power:", result2)
    print()

    # Additional quick sanity checks
    crates3: List[int] = [1, 2, 3]
    heaters3: List[int] = [2]
    result3: int = solution.minimum_heater_power(crates3, heaters3)
    print("Additional Test 1:")
    print("Crates:", crates3)
    print("Heaters:", heaters3)
    print("Minimum heater power:", result3)
    print()

    crates4: List[int] = [-10, 0, 10]
    heaters4: List[int] = [-5, 5]
    result4: int = solution.minimum_heater_power(crates4, heaters4)
    print("Additional Test 2:")
    print("Crates:", crates4)
    print("Heaters:", heaters4)
    print("Minimum heater power:", result4)