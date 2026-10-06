"""
Title: Count WiFi Router Pairs Covering a Hallway

Problem Description:
A company is installing WiFi routers along a long hallway. The hallway is represented
by a line segment from position 0 to position L. You are given an integer array
positions of length n, where positions[i] is the location of the i-th router candidate
along the hallway, and an integer array radius of the same length, where radius[i] is
the coverage radius of that candidate. If you install router i and router j together
(i < j), they cover intervals
[positions[i] - radius[i], positions[i] + radius[i]] and
[positions[j] - radius[j], positions[j] + radius[j]] on the hallway.

A pair of routers is called valid if the union of their coverage intervals covers the
entire hallway [0, L]. Count the number of distinct valid pairs.

Two routers may overlap, one interval may fully contain the other, and coverage outside
[0, L] is allowed. Router candidates are not guaranteed to be sorted by position.
Return the total number of pairs (i, j) with i < j that fully cover the hallway.

Constraints:
- 2 <= n <= 200000
- 1 <= L <= 1000000000
- 0 <= positions[i] <= L
- 0 <= radius[i] <= 1000000000
- positions.length == radius.length
"""

from bisect import bisect_left
from typing import List, Tuple


class FenwickTree:
    """Fenwick Tree / Binary Indexed Tree for prefix sums of counts."""

    def __init__(self, size: int) -> None:
        """
        Initialize an empty Fenwick tree.

        Args:
            size: Number of indices managed by the tree.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.size: int = size
        self.tree: List[int] = [0] * (size + 1)

    def add(self, index: int, delta: int) -> None:
        """
        Add a value to one position.

        Args:
            index: Zero-based index to update.
            delta: Value to add.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        i: int = index + 1
        while i <= self.size:
            self.tree[i] += delta
            i += i & -i

    def prefix_sum(self, index: int) -> int:
        """
        Compute sum of values in range [0, index].

        Args:
            index: Zero-based inclusive end index.

        Returns:
            Prefix sum up to index.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        if index < 0:
            return 0

        result: int = 0
        i: int = index + 1
        while i > 0:
            result += self.tree[i]
            i -= i & -i
        return result

    def range_sum(self, left: int, right: int) -> int:
        """
        Compute sum of values in range [left, right].

        Args:
            left: Left boundary, inclusive.
            right: Right boundary, inclusive.

        Returns:
            Sum in the requested range.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        if left > right:
            return 0
        return self.prefix_sum(right) - self.prefix_sum(left - 1)


class Solution:
    def count_valid_pairs(self, positions: List[int], radius: List[int], L: int) -> int:
        """
        Count pairs of routers whose union covers the entire hallway [0, L].

        Core idea:
        For an interval [a, b] and another interval [c, d], their union covers [0, L]
        if and only if:
        1) At least one interval reaches the left end: min(a, c) <= 0
        2) At least one interval reaches the right end: max(b, d) >= L
        3) There is no uncovered gap between them inside [0, L], which is equivalent to
           the two intervals overlapping or touching: max(a, c) <= min(b, d)

        We can count valid pairs efficiently by sorting intervals by left endpoint.
        For each interval j treated as the "later" interval in sorted-by-left order,
        we count earlier intervals i such that:
        - left_i <= 0                      (earlier interval reaches hallway start)
        - right_i >= L                    (earlier interval alone reaches hallway end), OR
          right_i >= left_j               (earlier interval touches/overlaps j)
          AND right_j >= L                (current interval reaches hallway end)

        More compactly for each j:
        - If right_j >= L:
              count earlier intervals with left_i <= 0 and right_i >= left_j
        - If right_j < L:
              count earlier intervals with left_i <= 0 and right_i >= L

        This works because after sorting by left endpoint, every earlier interval has
        left_i <= left_j. Therefore, if an earlier interval reaches 0 and its right end
        reaches at least left_j, then the two intervals connect continuously from 0 to
        at least right_j. If additionally right_j >= L, the whole hallway is covered.

        Args:
            positions: Router candidate positions.
            radius: Coverage radius for each router.
            L: Hallway length.

        Returns:
            Number of valid pairs.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(positions)

        # Step 1:
        # Convert each router into its coverage interval [left, right].
        # We do not clamp to [0, L] because coverage outside the hallway is allowed,
        # and using the original endpoints keeps the logic simple and correct.
        intervals: List[Tuple[int, int]] = []
        for i in range(n):
            left: int = positions[i] - radius[i]
            right: int = positions[i] + radius[i]
            intervals.append((left, right))

        # Step 2:
        # Sort intervals by left endpoint.
        #
        # Why this helps:
        # If we process intervals from smaller left to larger left, then for a current
        # interval j, every previously processed interval i satisfies left_i <= left_j.
        # This removes one degree of freedom and lets us count only based on right ends.
        intervals.sort()

        # Step 3:
        # We only care about earlier intervals that can start the hallway coverage,
        # meaning left_i <= 0.
        #
        # Among those intervals, we need to answer queries of the form:
        # "How many have right_i >= threshold?"
        #
        # This is a classic counting query that can be handled with:
        # - coordinate compression of right endpoints
        # - a Fenwick tree storing counts of eligible earlier intervals
        all_rights: List[int] = sorted({right for _, right in intervals})
        fenwick: FenwickTree = FenwickTree(len(all_rights))

        # This variable stores how many earlier intervals with left <= 0 have already
        # been inserted into the Fenwick tree.
        eligible_count: int = 0

        # Final answer.
        answer: int = 0

        # Step 4:
        # Process intervals in sorted order.
        for left_j, right_j in intervals:
            # Before counting pairs ending at the current interval j, the Fenwick tree
            # already contains exactly the earlier intervals i with left_i <= 0.
            #
            # We now determine what right endpoint threshold an earlier interval must meet.
            #
            # Case A: current interval reaches the hallway end (right_j >= L)
            # Then an earlier interval only needs to:
            #   - reach the hallway start (already enforced by being in the tree)
            #   - connect to current interval, i.e. right_i >= left_j
            #
            # Case B: current interval does NOT reach the hallway end (right_j < L)
            # Then the earlier interval must itself reach the hallway end:
            #   - right_i >= L
            #
            # In both cases, we count earlier eligible intervals with right_i >= threshold.
            if right_j >= L:
                threshold: int = left_j
            else:
                threshold = L

            # Find the first compressed right endpoint >= threshold.
            idx: int = bisect_left(all_rights, threshold)

            # If idx == len(all_rights), then no stored right endpoint can satisfy
            # right_i >= threshold, so contribution is zero.
            if idx < len(all_rights):
                # Count all eligible earlier intervals whose compressed right index is
                # in [idx, end].
                answer += fenwick.range_sum(idx, len(all_rights) - 1)

            # After counting pairs where current interval is the second one in sorted
            # order, we may insert the current interval into the data structure for
            # future intervals.
            #
            # We insert it only if it can start hallway coverage, i.e. left_j <= 0.
            if left_j <= 0:
                right_index: int = bisect_left(all_rights, right_j)
                fenwick.add(right_index, 1)
                eligible_count += 1

        return answer


if __name__ == "__main__":
    solution = Solution()

    positions1: List[int] = [2, 8, 5, 11]
    radius1: List[int] = [3, 4, 1, 2]
    L1: int = 10
    result1: int = solution.count_valid_pairs(positions1, radius1, L1)
    print(result1)  # Expected: 4

    positions2: List[int] = [1, 4, 7, 9]
    radius2: List[int] = [1, 1, 1, 1]
    L2: int = 10
    result2: int = solution.count_valid_pairs(positions2, radius2, L2)
    print(result2)  # Expected: 0