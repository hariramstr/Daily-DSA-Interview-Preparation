"""
Title: Maximum Scenic Pairing Distance Under Elevation Budget

Problem Description:
You are given an array elevations of length n, where elevations[i] is the height of the
i-th viewpoint along a mountain road. A tourism agency wants to choose two viewpoints
i and j with i < j to place synchronized photo beacons. The pair is considered valid if
the elevation difference between the two viewpoints is at most budget, that is:

    |elevations[i] - elevations[j]| <= budget

The scenic value of a valid pair is defined as the distance between the viewpoints
multiplied by the lower of the two elevations:

    scenicValue(i, j) = (j - i) * min(elevations[i], elevations[j])

Return the maximum scenic value among all valid pairs. If no valid pair exists, return 0.

Constraints:
- 2 <= n <= 200000
- 1 <= elevations[i] <= 1000000000
- 0 <= budget <= 1000000000

Goal:
Design an efficient solution. A brute-force O(n^2) approach is too slow.
"""

from __future__ import annotations

from bisect import bisect_left, bisect_right
from typing import Dict, List, Optional, Tuple


class SegmentTreeMin:
    """Segment tree supporting point updates and range minimum queries."""

    def __init__(self, size: int) -> None:
        """
        Initialize a segment tree for minimum queries.

        Args:
            size: Number of leaves.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.n: int = 1
        while self.n < size:
            self.n <<= 1
        self.inf: int = 10**30
        self.data: List[int] = [self.inf] * (2 * self.n)

    def update_min(self, index: int, value: int) -> None:
        """
        Set tree[index] = min(tree[index], value).

        Args:
            index: Position to update.
            value: Candidate minimum value.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        pos: int = index + self.n
        if value >= self.data[pos]:
            return
        self.data[pos] = value
        pos >>= 1
        while pos:
            new_value: int = min(self.data[pos << 1], self.data[(pos << 1) | 1])
            if new_value == self.data[pos]:
                break
            self.data[pos] = new_value
            pos >>= 1

    def query_min(self, left: int, right: int) -> int:
        """
        Query the minimum value in inclusive range [left, right].

        Args:
            left: Left boundary.
            right: Right boundary.

        Returns:
            Minimum value in the range, or +inf if the range is empty.

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        if left > right:
            return self.inf

        left += self.n
        right += self.n
        result: int = self.inf

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


class SegmentTreeMax:
    """Segment tree supporting point updates and range maximum queries."""

    def __init__(self, size: int) -> None:
        """
        Initialize a segment tree for maximum queries.

        Args:
            size: Number of leaves.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.n: int = 1
        while self.n < size:
            self.n <<= 1
        self.neg_inf: int = -1
        self.data: List[int] = [self.neg_inf] * (2 * self.n)

    def update_max(self, index: int, value: int) -> None:
        """
        Set tree[index] = max(tree[index], value).

        Args:
            index: Position to update.
            value: Candidate maximum value.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        pos: int = index + self.n
        if value <= self.data[pos]:
            return
        self.data[pos] = value
        pos >>= 1
        while pos:
            new_value: int = max(self.data[pos << 1], self.data[(pos << 1) | 1])
            if new_value == self.data[pos]:
                break
            self.data[pos] = new_value
            pos >>= 1

    def query_max(self, left: int, right: int) -> int:
        """
        Query the maximum value in inclusive range [left, right].

        Args:
            left: Left boundary.
            right: Right boundary.

        Returns:
            Maximum value in the range, or -1 if the range is empty.

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        if left > right:
            return self.neg_inf

        left += self.n
        right += self.n
        result: int = self.neg_inf

        while left <= right:
            if left & 1:
                result = max(result, self.data[left])
                left += 1
            if not (right & 1):
                result = max(result, self.data[right])
                right -= 1
            left >>= 1
            right >>= 1

        return result


class Solution:
    def max_scenic_pairing_distance(self, elevations: List[int], budget: int) -> int:
        """
        Compute the maximum scenic value among all valid viewpoint pairs.

        Core idea:
        For a pair (i, j), the score is:
            (j - i) * min(elevations[i], elevations[j])

        If we decide which endpoint is the smaller height, then the score becomes:
        - If elevations[i] <= elevations[j], score = elevations[i] * (j - i)
          and we need elevations[j] in [elevations[i], elevations[i] + budget].
          For a fixed i, we want the farthest possible j to the right in that height range.
        - If elevations[j] <= elevations[i], score = elevations[j] * (j - i)
          and we need elevations[i] in [elevations[j], elevations[j] + budget].
          For a fixed j, we want the farthest possible i to the left in that height range.

        We can solve both directions efficiently using coordinate compression plus
        segment trees over heights:
        1) Left-to-right pass:
           For each position j, treat elevations[j] as the smaller endpoint.
           Query the minimum earlier index i whose height lies in [h, h + budget].
           This gives the farthest-left valid partner where h is the minimum.
        2) Right-to-left pass:
           For each position i, treat elevations[i] as the smaller endpoint.
           Query the maximum later index j whose height lies in [h, h + budget].
           This gives the farthest-right valid partner where h is the minimum.

        Taking the best over both passes covers every valid pair exactly in one of
        those two "smaller endpoint" interpretations.

        Args:
            elevations: Heights of viewpoints.
            budget: Maximum allowed absolute height difference.

        Returns:
            Maximum scenic value, or 0 if no valid pair exists.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(elevations)

        # ------------------------------------------------------------
        # Step 1: Coordinate compression of heights.
        #
        # Why do we compress?
        # Heights can be as large as 1e9, so we cannot build a segment tree
        # directly over raw height values.
        #
        # Compression maps each distinct height to a compact index in [0, m-1].
        # We still preserve ordering, which is exactly what we need for range
        # queries like [h, h + budget].
        # ------------------------------------------------------------
        sorted_unique_heights: List[int] = sorted(set(elevations))
        m: int = len(sorted_unique_heights)

        # ------------------------------------------------------------
        # Step 2: Prepare answer accumulator.
        # ------------------------------------------------------------
        best: int = 0

        # ------------------------------------------------------------
        # Step 3: Left-to-right pass.
        #
        # Interpretation:
        # We are at position j with height h = elevations[j].
        # We want an earlier index i < j such that:
        #   - elevations[i] is in [h, h + budget]
        #   - therefore h is the smaller (or equal) height
        #   - score = h * (j - i)
        #
        # To maximize the distance (j - i), for this fixed j and h we want the
        # SMALLEST possible i among all earlier valid heights.
        #
        # Data structure:
        # A segment tree storing, for each compressed height, the minimum index
        # seen so far with that exact height.
        #
        # Then a range minimum query over compressed heights corresponding to
        # [h, h + budget] gives the earliest valid i.
        # ------------------------------------------------------------
        min_index_tree: SegmentTreeMin = SegmentTreeMin(m)

        for j, h in enumerate(elevations):
            # Find the compressed index range of heights in [h, h + budget].
            left_idx: int = bisect_left(sorted_unique_heights, h)
            right_idx: int = bisect_right(sorted_unique_heights, h + budget) - 1

            # Query the earliest previous index with height in the valid range.
            earliest_i: int = min_index_tree.query_min(left_idx, right_idx)

            # If we found a valid earlier index, compute the scenic value.
            if earliest_i != min_index_tree.inf:
                best = max(best, (j - earliest_i) * h)

            # After processing j as a right endpoint, insert j into the structure
            # so future positions can use it as a left endpoint.
            compressed_h: int = left_idx
            min_index_tree.update_min(compressed_h, j)

        # ------------------------------------------------------------
        # Step 4: Right-to-left pass.
        #
        # Interpretation:
        # We are at position i with height h = elevations[i].
        # We want a later index j > i such that:
        #   - elevations[j] is in [h, h + budget]
        #   - therefore h is the smaller (or equal) height
        #   - score = h * (j - i)
        #
        # To maximize distance, for this fixed i and h we want the LARGEST
        # possible j among all later valid heights.
        #
        # Data structure:
        # A segment tree storing, for each compressed height, the maximum index
        # seen so far while scanning from right to left.
        #
        # Then a range maximum query over [h, h + budget] gives the farthest
        # valid j to the right.
        # ------------------------------------------------------------
        max_index_tree: SegmentTreeMax = SegmentTreeMax(m)

        for i in range(n - 1, -1, -1):
            h = elevations[i]

            # Find the compressed index range of heights in [h, h + budget].
            left_idx = bisect_left(sorted_unique_heights, h)
            right_idx = bisect_right(sorted_unique_heights, h + budget) - 1

            # Query the farthest later index with height in the valid range.
            farthest_j: int = max_index_tree.query_max(left_idx, right_idx)

            # If found, compute the scenic value.
            if farthest_j != max_index_tree.neg_inf:
                best = max(best, (farthest_j - i) * h)

            # Insert current index so positions further left can use it.
            compressed_h = left_idx
            max_index_tree.update_max(compressed_h, i)

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    elevations1: List[int] = [8, 1, 6, 2, 5, 7]
    budget1: int = 2
    result1: int = solution.max_scenic_pairing_distance(elevations1, budget1)
    print(result1)  # Expected: 35

    # Example 2
    elevations2: List[int] = [3, 10, 4, 9, 2]
    budget2: int = 0
    result2: int = solution.max_scenic_pairing_distance(elevations2, budget2)
    print(result2)  # Expected: 0

    # Additional quick checks
    elevations3: List[int] = [5, 5]
    budget3: int = 0
    result3: int = solution.max_scenic_pairing_distance(elevations3, budget3)
    print(result3)  # Expected: 5

    elevations4: List[int] = [1, 100, 1]
    budget4: int = 0
    result4: int = solution.max_scenic_pairing_distance(elevations4, budget4)
    print(result4)  # Expected: 2