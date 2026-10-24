"""
Title: Maximum Safe Gap for Festival Stage Placement

Problem Description:
A city is preparing a long riverside walkway for a music festival. There are n approved
installation spots along the walkway, and the ith spot is located at position positions[i]
meters from the western entrance. You must place exactly k outdoor stages at these approved
spots.

For safety and crowd flow, the organizers want the minimum distance between any two placed
stages to be as large as possible. In other words, if you place stages at some k positions,
define the arrangement score as the smallest absolute difference between every pair of
consecutive chosen stage positions after sorting them. Your task is to return the maximum
possible arrangement score.

You may assume stages can only be placed at the given approved spots, and each spot can hold
at most one stage. The input positions are not guaranteed to be sorted.

This problem is designed to test whether you can recognize a monotonic decision condition:
if it is possible to place k stages with minimum gap d, then it is also possible for any
smaller gap. That makes binary search on the answer possible.

Constraints:
- 2 <= n <= 10^5
- 2 <= k <= n
- 0 <= positions[i] <= 10^9
- All values in positions are distinct.

Example 1:
Input: positions = [2, 11, 6, 17, 25], k = 3
Output: 9

Example 2:
Input: positions = [1, 3, 4, 8, 13, 17], k = 4
Output: 4
"""

from typing import List


class Solution:
    def can_place_with_gap(self, positions: List[int], k: int, gap: int) -> bool:
        """
        Check whether it is possible to place exactly k stages so that the minimum
        distance between any two chosen stage positions is at least `gap`.

        Args:
            positions: Sorted list of approved installation positions.
            k: Number of stages that must be placed.
            gap: Candidate minimum allowed distance between consecutive chosen stages.

        Returns:
            True if we can place at least k stages while keeping every chosen pair
            of neighboring stages at distance >= gap, otherwise False.

        Time complexity:
            O(n), where n is the number of positions.

        Space complexity:
            O(1), ignoring the input storage.
        """
        # We always place the first stage at the earliest available position.
        # Why is this greedy choice correct?
        # Because placing a stage earlier leaves as much room as possible for the
        # remaining stages to the right. If a valid arrangement exists for this gap,
        # the greedy strategy will also find one.
        count: int = 1
        last_placed: int = positions[0]

        # Scan through the sorted positions from left to right.
        # Whenever the current position is far enough from the last placed stage,
        # we place another stage here.
        for current in positions[1:]:
            # If the distance from the last placed stage is at least `gap`,
            # this position is safe to use.
            if current - last_placed >= gap:
                count += 1
                last_placed = current

                # As soon as we have placed k stages, we know this gap is feasible.
                if count >= k:
                    return True

        # If we finish scanning and still placed fewer than k stages,
        # then this gap is too large to be achievable.
        return False

    def max_safe_gap(self, positions: List[int], k: int) -> int:
        """
        Compute the largest possible minimum distance between any two placed stages.

        This uses:
        1. Sorting, so distances can be checked from left to right.
        2. Binary search on the answer, because feasibility is monotonic:
           - If a gap `d` works, then every smaller gap also works.
           - If a gap `d` does not work, then every larger gap also does not work.

        Args:
            positions: List of distinct approved installation positions.
            k: Number of stages to place.

        Returns:
            The maximum possible arrangement score (largest minimum gap).

        Time complexity:
            O(n log n + n log R), where:
            - n is the number of positions
            - R is the search range of possible gaps, up to positions[-1] - positions[0]

        Space complexity:
            O(1) extra space beyond sorting requirements.
        """
        # First, sort the positions.
        # This is essential because:
        # - The minimum gap is naturally defined between consecutive chosen positions
        #   after sorting.
        # - Our greedy feasibility check depends on moving from left to right.
        positions.sort()

        # The smallest possible answer is 0 in a very general sense,
        # but because all positions are distinct and k >= 2, the practical answer
        # will be at least 1 if positions are integers with distinct values.
        # Still, using 0 as the lower bound is safe and simple.
        left: int = 0

        # The largest possible minimum gap cannot exceed the distance between the
        # leftmost and rightmost approved spots.
        right: int = positions[-1] - positions[0]

        # We will store the best feasible answer found so far.
        best: int = 0

        # Standard binary search on the answer space.
        # At each step:
        # - Try a candidate gap `mid`
        # - If feasible, record it and search for a larger one
        # - If not feasible, search for a smaller one
        while left <= right:
            mid: int = (left + right) // 2

            # Check whether we can place k stages with at least `mid` distance apart.
            if self.can_place_with_gap(positions, k, mid):
                # This gap works, so it is a valid candidate answer.
                best = mid

                # Since we want the maximum possible gap, try larger values.
                left = mid + 1
            else:
                # This gap is too large, so we must try smaller values.
                right = mid - 1

        # After binary search finishes, `best` holds the largest feasible gap.
        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    positions1: List[int] = [2, 11, 6, 17, 25]
    k1: int = 3
    result1: int = solution.max_safe_gap(positions1, k1)
    print("Example 1 Result:", result1)  # Expected: 9

    # Example 2
    positions2: List[int] = [1, 3, 4, 8, 13, 17]
    k2: int = 4
    result2: int = solution.max_safe_gap(positions2, k2)
    print("Example 2 Result:", result2)  # Expected: 4

    # Additional quick sanity checks
    positions3: List[int] = [1, 2, 3, 4, 7]
    k3: int = 3
    result3: int = solution.max_safe_gap(positions3, k3)
    print("Additional Test Result:", result3)  # One optimal answer is 3 (positions 1, 4, 7)