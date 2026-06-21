"""
Title: Minimum Gap to Place Festival Stages

Problem Description:
You are organizing a large outdoor festival along a straight road. There are n approved
installation points, given as a sorted or unsorted array positions, where positions[i]
is the coordinate of the i-th point. You must place exactly k stages at distinct
installation points.

For safety and crowd control, the festival authority defines the gap of a placement as
the minimum distance between any two chosen stages. A placement is considered valid if
every pair of neighboring chosen stages is at least that gap apart. Your task is to
compute the largest possible gap that can be guaranteed while still placing all k stages.

Return the maximum integer value g such that it is possible to choose exactly k
installation points and the distance between every two consecutive chosen points is at
least g.

This problem is designed to reward an efficient solution. A brute-force search over all
subsets of size k is far too slow. The key observation is that if a gap g is feasible,
then every smaller gap is also feasible, which makes the answer searchable with binary
search.

Constraints:
- 2 <= n <= 2 * 10^5
- 2 <= k <= n
- 0 <= positions[i] <= 10^9
- All installation points are distinct integers.

Example 1:
Input: positions = [1, 2, 8, 12, 17], k = 3
Output: 7

Example 2:
Input: positions = [4, 15, 7, 20, 1, 11], k = 4
Output: 4
"""

from typing import List


class Solution:
    def can_place_with_gap(self, positions: List[int], k: int, gap: int) -> bool:
        """
        Check whether it is possible to place exactly k stages such that the minimum
        distance between consecutive chosen positions is at least `gap`.

        Args:
            positions: Sorted list of distinct installation point coordinates.
            k: Number of stages that must be placed.
            gap: Candidate minimum gap to test.

        Returns:
            True if we can place at least k stages while keeping every neighboring
            chosen pair at distance >= gap, otherwise False.

        Time complexity:
            O(n), where n is the number of positions.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We always place the first stage at the earliest available position.
        # Why is this greedy choice correct?
        # Because placing a stage earlier leaves as much room as possible for the
        # remaining stages to be placed later. If a solution exists for this gap,
        # the greedy strategy will find one.
        count: int = 1
        last_placed: int = positions[0]

        # Scan through the sorted positions from left to right.
        # Whenever we find a point that is at least `gap` away from the last chosen
        # point, we place another stage there.
        for current in positions[1:]:
            # If the current position is far enough from the last placed stage,
            # it is safe to place a new stage here while preserving the required gap.
            if current - last_placed >= gap:
                count += 1
                last_placed = current

                # As soon as we have placed k stages, the candidate gap is feasible.
                if count >= k:
                    return True

        # If we finish scanning and still have fewer than k stages placed,
        # then this gap is too large to be feasible.
        return False

    def maximum_gap(self, positions: List[int], k: int) -> int:
        """
        Compute the largest possible minimum gap between consecutive chosen stage
        positions using sorting, greedy feasibility checking, and binary search.

        Args:
            positions: Unsorted or sorted list of distinct installation point coordinates.
            k: Number of stages that must be placed.

        Returns:
            The maximum integer gap that can be guaranteed while placing exactly k stages.

        Time complexity:
            O(n log n + n log D), where:
            - n is the number of positions
            - D is the search range of distances, at most positions[-1] - positions[0]

        Space complexity:
            O(1) extra space beyond the sort implementation details.
        """
        # Step 1: Sort the positions.
        # The greedy placement strategy only makes sense when points are processed
        # from left to right in increasing order.
        positions.sort()

        # Step 2: Define the binary search range for the answer.
        #
        # The minimum possible gap is 0 in theory, but because all positions are
        # distinct integers and k >= 2, the practical answer will be at least 1.
        # Still, using 0 is perfectly safe and keeps the logic general.
        #
        # The maximum possible gap cannot exceed the distance between the leftmost
        # and rightmost installation points.
        left: int = 0
        right: int = positions[-1] - positions[0]

        # `best` stores the largest feasible gap found so far.
        best: int = 0

        # Step 3: Binary search on the answer.
        #
        # Why binary search works:
        # - If a gap `g` is feasible, then every smaller gap is also feasible.
        # - If a gap `g` is not feasible, then every larger gap is also not feasible.
        #
        # This monotonic property is exactly what binary search needs.
        while left <= right:
            mid: int = (left + right) // 2

            # Test whether `mid` can serve as the minimum required gap.
            if self.can_place_with_gap(positions, k, mid):
                # If feasible, record it as a candidate answer.
                best = mid

                # Try to do even better by searching larger gaps.
                left = mid + 1
            else:
                # If not feasible, we must reduce the gap.
                right = mid - 1

        # After binary search ends, `best` is the largest feasible gap.
        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    positions1: List[int] = [1, 2, 8, 12, 17]
    k1: int = 3
    result1: int = solution.maximum_gap(positions1, k1)
    print("Example 1 Result:", result1)  # Expected: 7

    # Example 2
    positions2: List[int] = [4, 15, 7, 20, 1, 11]
    k2: int = 4
    result2: int = solution.maximum_gap(positions2, k2)
    print("Example 2 Result:", result2)  # Expected: 4