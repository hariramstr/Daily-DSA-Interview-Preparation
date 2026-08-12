"""
Title: Minimum Loudspeaker Volume for Hall Announcements

Problem Description:
A convention center has a long hallway with event booths placed at known integer positions
along a straight line. You need to install loudspeakers at some of these booth positions so
that every booth can hear announcements. If a loudspeaker is set to volume radius R, it
covers every booth whose position is within distance R from that loudspeaker. You may install
at most k loudspeakers, and each loudspeaker must be placed at one of the given booth positions.

Return the minimum integer radius R needed so that all booths are covered.

This problem is designed for an efficient solution using binary search on the answer.
For a fixed radius R, determine whether it is possible to cover all booth positions using
at most k loudspeakers. The booth positions are not guaranteed to be sorted and may contain
duplicates.

Constraints:
- 1 <= n == positions.length <= 2 * 10^5
- 1 <= k <= n
- 0 <= positions[i] <= 10^9
- The answer fits in a 32-bit signed integer.
"""

from bisect import bisect_right
from typing import List


class Solution:
    def minimum_radius(self, positions: List[int], k: int) -> int:
        """
        Compute the minimum integer radius needed to cover all booth positions
        using at most k loudspeakers placed only at given booth positions.

        Args:
            positions: List of booth positions along a line. May be unsorted and contain duplicates.
            k: Maximum number of loudspeakers allowed.

        Returns:
            The smallest integer radius R such that all booths can be covered.

        Time complexity:
            O(n log n + n log M), where:
            - n is the number of booth positions
            - M is the search range of the answer (up to 10^9)

        Space complexity:
            O(n) for storing the sorted unique positions.
        """
        # Step 1:
        # Sort the positions and remove duplicates.
        #
        # Why removing duplicates is safe:
        # If multiple booths are at the exact same coordinate, covering that coordinate once
        # covers all booths there. Therefore, duplicates do not change the geometric coverage
        # problem. They only repeat the same point.
        #
        # This simplification is very helpful because:
        # - It reduces the number of points we need to reason about.
        # - It makes the greedy coverage check cleaner.
        unique_positions: List[int] = sorted(set(positions))

        # If the number of distinct booth positions is already <= k,
        # we can place one loudspeaker at each distinct position with radius 0.
        if len(unique_positions) <= k:
            return 0

        # Step 2:
        # Prepare binary search bounds for the answer.
        #
        # The minimum possible radius is 0.
        # A safe maximum possible radius is the full spread of the positions:
        # max_position - min_position.
        #
        # This upper bound is always enough because with that radius, one loudspeaker
        # placed appropriately among booth positions can cover a very large interval,
        # and certainly k loudspeakers can cover everything.
        left: int = 0
        right: int = unique_positions[-1] - unique_positions[0]

        # Standard binary search on the answer:
        # We search for the smallest radius R such that coverage is possible.
        while left < right:
            mid: int = (left + right) // 2

            # If radius mid is feasible, try smaller radius.
            if self._can_cover(unique_positions, k, mid):
                right = mid
            else:
                # Otherwise, we need a larger radius.
                left = mid + 1

        # At the end, left == right and is the minimum feasible radius.
        return left

    def _can_cover(self, positions: List[int], k: int, radius: int) -> bool:
        """
        Check whether all booth positions can be covered using at most k loudspeakers,
        each placed at one of the given booth positions, with the given radius.

        Args:
            positions: Sorted list of distinct booth positions.
            k: Maximum number of loudspeakers allowed.
            radius: Candidate radius to test.

        Returns:
            True if all positions can be covered with at most k loudspeakers, else False.

        Time complexity:
            O(m log m), where m is the number of distinct positions.
            Each loudspeaker placement uses binary search to jump over covered positions.

        Space complexity:
            O(1) auxiliary space, excluding input storage.
        """
        # Greedy strategy:
        #
        # We always start from the leftmost booth that is not yet covered.
        # Suppose that booth is at position x.
        #
        # To maximize how far one loudspeaker can cover to the right, while still covering x,
        # we should place the loudspeaker at the rightmost booth position <= x + radius.
        #
        # Why?
        # - The loudspeaker must cover x, so if placed at position p, we need p - x <= radius,
        #   i.e. p <= x + radius.
        # - Among all valid booth positions where we may place it, choosing the furthest right
        #   gives the largest right coverage endpoint p + radius.
        #
        # This is the classic optimal greedy choice for covering points on a line.
        used_speakers: int = 0
        n: int = len(positions)
        i: int = 0

        # Continue until all positions are covered or we exceed k loudspeakers.
        while i < n:
            used_speakers += 1

            # If we already used too many loudspeakers, this radius is not feasible.
            if used_speakers > k:
                return False

            # The current leftmost uncovered booth.
            leftmost_uncovered: int = positions[i]

            # Find the rightmost booth position where we can place a loudspeaker
            # while still covering leftmost_uncovered.
            #
            # Valid placement positions must satisfy:
            # placement <= leftmost_uncovered + radius
            #
            # bisect_right returns the insertion index to keep order for the value
            # (leftmost_uncovered + radius), so subtracting 1 gives the index of the
            # rightmost existing booth position <= that value.
            placement_limit: int = leftmost_uncovered + radius
            placement_index: int = bisect_right(positions, placement_limit) - 1

            # This is the actual booth position where we place the loudspeaker.
            speaker_position: int = positions[placement_index]

            # Once placed at speaker_position, this loudspeaker covers up to:
            # speaker_position + radius
            right_covered: int = speaker_position + radius

            # Skip every booth position that lies within the covered interval.
            #
            # All positions <= right_covered are now covered, so we jump directly
            # to the first uncovered position using binary search.
            i = bisect_right(positions, right_covered)

        # If we exited the loop, every booth was covered using <= k loudspeakers.
        return True


if __name__ == "__main__":
    solution = Solution()

    sample_positions_1: List[int] = [1, 2, 8, 12, 17]
    sample_k_1: int = 2
    result_1: int = solution.minimum_radius(sample_positions_1, sample_k_1)
    print(f"positions = {sample_positions_1}, k = {sample_k_1}")
    print(f"Minimum radius = {result_1}")
    print("Expected = 4")
    print()

    sample_positions_2: List[int] = [4, 4, 4, 10, 15, 21]
    sample_k_2: int = 3
    result_2: int = solution.minimum_radius(sample_positions_2, sample_k_2)
    print(f"positions = {sample_positions_2}, k = {sample_k_2}")
    print(f"Minimum radius = {result_2}")
    print("Expected = 3")
    print()

    extra_positions_3: List[int] = [0, 100]
    extra_k_3: int = 1
    result_3: int = solution.minimum_radius(extra_positions_3, extra_k_3)
    print(f"positions = {extra_positions_3}, k = {extra_k_3}")
    print(f"Minimum radius = {result_3}")
    print("Expected = 100")
    print()

    extra_positions_4: List[int] = [5, 5, 5, 5]
    extra_k_4: int = 1
    result_4: int = solution.minimum_radius(extra_positions_4, extra_k_4)
    print(f"positions = {extra_positions_4}, k = {extra_k_4}")
    print(f"Minimum radius = {result_4}")
    print("Expected = 0")