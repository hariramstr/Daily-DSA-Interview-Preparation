"""
Title: Maximum Safe Gap for Drone Corridor Placement

Problem Description:
A city wants to place exactly k drone recharge beacons along a straight aerial corridor.
The corridor already has n approved mounting points, given as a sorted array positions,
where positions[i] is the distance in meters from the start of the corridor. You may
place at most one beacon at each mounting point.

For safety reasons, the city wants the minimum distance between any two placed beacons
to be as large as possible. Your task is to return the largest possible value d such
that it is possible to place exactly k beacons and every pair of consecutive placed
beacons is at least d meters apart.

This is an optimization problem: you are not asked to output the placement itself,
only the maximum achievable minimum gap.

Constraints:
- 2 <= n <= 100000
- 2 <= k <= n
- 0 <= positions[i] <= 1000000000
- positions is sorted in strictly increasing order

Examples:
1) positions = [1, 2, 8, 12, 17], k = 3
   Output: 7

2) positions = [3, 6, 14, 20, 25, 31], k = 4
   Correct Output: 6

Intended approach:
Use binary search on the answer combined with a greedy feasibility check.
"""

from typing import List


class Solution:
    def can_place_with_gap(self, positions: List[int], k: int, min_gap: int) -> bool:
        """
        Check whether it is possible to place at least k beacons so that the distance
        between every pair of consecutive placed beacons is at least min_gap.

        The method uses a greedy strategy:
        - Always place the first beacon at the earliest available position.
        - Then continue scanning from left to right.
        - Whenever the current position is far enough from the last placed beacon,
          place another beacon there.

        Why greedy works:
        - Placing a beacon as early as possible leaves the most room for the remaining
          beacons to be placed later.
        - If this earliest-placement strategy cannot place k beacons, then no other
          strategy can do better for the same minimum gap.

        Args:
            positions: Sorted list of approved mounting point positions.
            k: Exact number of beacons we want to place.
            min_gap: Candidate minimum required distance between consecutive beacons.

        Returns:
            True if placing k beacons is feasible with at least min_gap distance,
            otherwise False.

        Time complexity:
            O(n), where n is the number of positions.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We always place the first beacon at the first available mounting point.
        # This is the earliest possible placement and is optimal for feasibility.
        placed_count: int = 1
        last_placed_position: int = positions[0]

        # Scan through the remaining positions from left to right.
        for current_position in positions[1:]:
            # If the current position is far enough from the last placed beacon,
            # we can safely place another beacon here while maintaining min_gap.
            if current_position - last_placed_position >= min_gap:
                placed_count += 1
                last_placed_position = current_position

                # As soon as we have placed k beacons, we know this gap is feasible.
                if placed_count >= k:
                    return True

        # If we finish scanning and still have fewer than k beacons, the gap is too large.
        return False

    def maximum_safe_gap(self, positions: List[int], k: int) -> int:
        """
        Compute the largest possible minimum distance between any two consecutive
        placed beacons.

        This method uses binary search on the answer:
        - The smallest possible minimum gap is 0.
        - The largest possible minimum gap is positions[-1] - positions[0].
        - For each candidate gap, use a greedy feasibility check.
        - If a gap is feasible, try a larger one.
        - If a gap is not feasible, try a smaller one.

        This works because feasibility is monotonic:
        - If a gap d is feasible, then every smaller gap is also feasible.
        - If a gap d is not feasible, then every larger gap is also not feasible.

        Args:
            positions: Sorted list of approved mounting point positions.
            k: Exact number of beacons to place.

        Returns:
            The maximum achievable minimum gap.

        Time complexity:
            O(n log R), where:
            - n is the number of positions
            - R is positions[-1] - positions[0]

        Space complexity:
            O(1), ignoring input storage.
        """
        # Binary search boundaries:
        # low  = definitely feasible lower bound candidate
        # high = definitely possible upper search limit
        #
        # Since positions are sorted and strictly increasing, the maximum possible
        # distance between the first and last point is a safe upper bound.
        low: int = 0
        high: int = positions[-1] - positions[0]

        # This variable stores the best feasible answer found so far.
        best_gap: int = 0

        # Standard binary search over the answer space.
        while low <= high:
            # Choose the middle candidate gap.
            mid: int = (low + high) // 2

            # Check whether we can place k beacons with at least 'mid' distance apart.
            if self.can_place_with_gap(positions, k, mid):
                # If feasible, record it as a valid answer.
                best_gap = mid

                # Then try to do even better by searching larger gaps.
                low = mid + 1
            else:
                # If not feasible, this gap is too large.
                # We must search smaller gaps.
                high = mid - 1

        # After binary search finishes, best_gap is the largest feasible minimum gap.
        return best_gap


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt
    positions1: List[int] = [1, 2, 8, 12, 17]
    k1: int = 3
    result1: int = solution.maximum_safe_gap(positions1, k1)
    print("Example 1 Result:", result1)  # Expected: 7

    # Example 2 from the prompt
    # Careful verification shows the correct answer is 6, not 8.
    positions2: List[int] = [3, 6, 14, 20, 25, 31]
    k2: int = 4
    result2: int = solution.maximum_safe_gap(positions2, k2)
    print("Example 2 Result:", result2)  # Expected: 6

    # Additional small sanity check
    positions3: List[int] = [0, 4, 7, 10, 13]
    k3: int = 3
    result3: int = solution.maximum_safe_gap(positions3, k3)
    print("Additional Example Result:", result3)  # One optimal answer: 6 using 0, 7, 13