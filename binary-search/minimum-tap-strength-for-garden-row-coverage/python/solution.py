"""
Title: Minimum Tap Strength for Garden Row Coverage

Problem Description:
A landscaping company manages a straight garden row represented by positions from 0 to L
on a number line. There are n sprinklers installed along the row. The i-th sprinkler is
fixed at position positions[i] and can water every point within distance s from its
location if all sprinklers are configured with the same tap strength s. In other words,
with strength s, sprinkler i covers the interval [positions[i] - s, positions[i] + s].

Your task is to find the minimum integer tap strength s such that the entire garden row
from 0 to L is covered by at least one sprinkler. Coverage is continuous: every point in
the interval [0, L] must lie inside the watering range of some sprinkler. If it is
impossible to cover the full row even with arbitrarily large strength because there are
no sprinklers, return -1.

This problem is designed to reward an efficient solution. A direct simulation over all
possible strengths is too slow when L is large. Instead, determine whether a candidate
strength is feasible, and use binary search on the answer.

Constraints:
- 0 <= L <= 10^9
- 0 <= n == positions.length <= 2 * 10^5
- 0 <= positions[i] <= L
- positions may be unsorted
- Return the minimum integer strength

Example 1:
Input: L = 10, positions = [1, 4, 8]
Output: 2
Explanation: With strength 2, the covered intervals are [-1,3], [2,6], and [6,10],
which together cover the entire row [0,10]. Strength 1 leaves gaps.

Example 2:
Input: L = 12, positions = [3, 9]
Output: 3
Explanation: With strength 3, the intervals are [0,6] and [6,12], so the full row is
covered. With strength 2, coverage is [1,5] and [7,11], leaving uncovered points.
"""

from typing import List


class Solution:
    def can_cover(self, length: int, positions: List[int], strength: int) -> bool:
        """
        Check whether a given tap strength can cover the entire garden row [0, length].

        Args:
            length: The right endpoint L of the garden row [0, L].
            positions: Sorted sprinkler positions on the row.
            strength: Candidate integer tap strength to test.

        Returns:
            True if the full interval [0, length] is covered, otherwise False.

        Time complexity:
            O(n), where n is the number of sprinklers.

        Space complexity:
            O(1), ignoring the input storage.
        """
        # We will greedily scan the sprinklers from left to right.
        #
        # Because positions are sorted, their coverage intervals also appear in sorted
        # order by left endpoint:
        #   interval_i = [positions[i] - strength, positions[i] + strength]
        #
        # To cover the entire garden continuously, two things must hold:
        # 1) The first useful interval must start at or before 0.
        # 2) Every next interval must begin at or before the current covered end,
        #    otherwise there is a gap.
        # 3) Eventually, the covered end must reach at least 'length'.
        #
        # Instead of storing all intervals, we process them one by one and maintain
        # the farthest point covered so far.

        covered_end = 0

        # Go through each sprinkler in sorted order.
        for position in positions:
            # Compute the left and right endpoints of this sprinkler's coverage.
            left = position - strength
            right = position + strength

            # If this interval starts strictly after the currently covered end,
            # then there is an uncovered gap.
            #
            # Example:
            #   covered_end = 5
            #   next interval starts at 6
            # Then point 5.5 is uncovered, so full coverage fails.
            if left > covered_end:
                return False

            # Otherwise, this interval touches or overlaps the already covered region.
            # That means we can safely extend the covered region to the farther right end.
            if right > covered_end:
                covered_end = right

            # If we have already covered up to or beyond 'length', then the whole
            # garden [0, length] is covered and we can stop early.
            if covered_end >= length:
                return True

        # After processing all sprinklers, coverage succeeds only if we reached length.
        return covered_end >= length

    def minimum_tap_strength(self, length: int, positions: List[int]) -> int:
        """
        Find the minimum integer tap strength needed to cover the entire garden row.

        Args:
            length: The right endpoint L of the garden row [0, L].
            positions: Positions of sprinklers, possibly unsorted.

        Returns:
            The minimum integer strength s such that [0, length] is fully covered.
            Returns -1 if there are no sprinklers and coverage is impossible.

        Time complexity:
            O(n log n + n log L), where n is the number of sprinklers.

        Space complexity:
            O(n) due to sorting.
        """
        # Special case:
        # If there are no sprinklers at all, then no positive-length or zero-length
        # garden can be covered by any finite or infinite "shared strength" in this
        # model, because there is no source of water.
        if not positions:
            return -1

        # Sort positions so that we can test coverage in one left-to-right pass.
        sorted_positions = sorted(positions)

        # Binary search setup:
        #
        # We search for the smallest integer strength s such that can_cover(...) is True.
        #
        # Why binary search works:
        # - If a certain strength s can cover the garden, then any larger strength
        #   will also cover it, because every interval only gets wider.
        # - This monotonic True/False behavior is exactly what binary search needs.
        #
        # Lower bound:
        #   0 is always the smallest possible integer strength.
        #
        # Upper bound:
        #   'length' is always sufficient when there is at least one sprinkler inside [0, L].
        #   Why?
        #   For any sprinkler at position p with 0 <= p <= L:
        #       [p - length, p + length]
        #   certainly covers both 0 and L, because:
        #       p - length <= 0
        #       p + length >= L
        #   So one sprinkler alone can cover the whole garden with strength = length.
        #
        # Therefore, when positions is non-empty, an answer always exists in [0, length].
        left = 0
        right = length

        # Standard binary search for the first feasible value.
        while left < right:
            mid = (left + right) // 2

            # Test whether this candidate strength is enough.
            if self.can_cover(length, sorted_positions, mid):
                # mid works, so the minimum answer is in [left, mid].
                right = mid
            else:
                # mid does not work, so we must try larger strengths.
                left = mid + 1

        # At loop end, left == right and points to the minimum feasible strength.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # L = 10, positions = [1, 4, 8]
    # Expected output: 2
    length_1 = 10
    positions_1 = [1, 4, 8]
    result_1 = solution.minimum_tap_strength(length_1, positions_1)
    print(f"Example 1 result: {result_1}")

    # Example 2:
    # L = 12, positions = [3, 9]
    # Expected output: 3
    length_2 = 12
    positions_2 = [3, 9]
    result_2 = solution.minimum_tap_strength(length_2, positions_2)
    print(f"Example 2 result: {result_2}")

    # Additional sample:
    # Single sprinkler at the center of a zero-length garden.
    # Garden [0, 0] is already just one point, and a sprinkler at 0 with strength 0 covers it.
    length_3 = 0
    positions_3 = [0]
    result_3 = solution.minimum_tap_strength(length_3, positions_3)
    print(f"Additional example result: {result_3}")

    # Additional sample:
    # No sprinklers means impossible.
    length_4 = 5
    positions_4: List[int] = []
    result_4 = solution.minimum_tap_strength(length_4, positions_4)
    print(f"No sprinkler example result: {result_4}")