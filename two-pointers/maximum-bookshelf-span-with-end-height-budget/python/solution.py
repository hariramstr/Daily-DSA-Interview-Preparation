"""
Title: Maximum Bookshelf Span With End-Height Budget

Problem Description:
You are given an array heights where heights[i] is the height of the i-th bookshelf
in a long archive hallway. A moving ladder can be placed between any two different
bookshelves i and j with i < j. The ladder is considered stable only if the absolute
difference between the heights of the two endpoint bookshelves is at most budget.

For every stable pair (i, j), define its span score as:

    (j - i) * min(heights[i], heights[j])

Your task is to return the maximum possible span score among all stable pairs.
If no stable pair exists, return 0.

This problem resembles classic endpoint-selection tasks, but the added endpoint-height
budget changes which pairs are valid. A brute-force O(n^2) scan will time out for
large inputs. We need an efficient strategy.

Constraints:
- 2 <= heights.length <= 200000
- 1 <= heights[i] <= 1000000000
- 0 <= budget <= 1000000000
- Return the maximum score as a 64-bit integer
"""

from bisect import bisect_left, bisect_right
from typing import Dict, List, Optional, Tuple


class SegmentTreeMax:
    """Segment tree supporting point updates and range maximum queries."""

    def __init__(self, size: int) -> None:
        """
        Initialize a max segment tree.

        Args:
            size: Number of leaves / coordinate-compressed values.

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
        self.tree: List[int] = [-1] * (2 * self.n)

    def update(self, index: int, value: int) -> None:
        """
        Set tree[index] = max(tree[index], value).

        Args:
            index: Leaf index to update.
            value: Candidate value.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1) auxiliary
        """
        pos: int = index + self.n
        if value <= self.tree[pos]:
            return
        self.tree[pos] = value
        pos //= 2
        while pos >= 1:
            left_val: int = self.tree[2 * pos]
            right_val: int = self.tree[2 * pos + 1]
            new_val: int = left_val if left_val >= right_val else right_val
            if self.tree[pos] == new_val:
                break
            self.tree[pos] = new_val
            pos //= 2

    def query(self, left: int, right: int) -> int:
        """
        Return maximum value in inclusive range [left, right].

        Args:
            left: Left boundary.
            right: Right boundary.

        Returns:
            Maximum stored value in the range, or -1 if empty / no value.

        Time complexity:
            O(log n)

        Space complexity:
            O(1) auxiliary
        """
        if left > right:
            return -1

        left += self.n
        right += self.n
        result: int = -1

        while left <= right:
            if left % 2 == 1:
                if self.tree[left] > result:
                    result = self.tree[left]
                left += 1
            if right % 2 == 0:
                if self.tree[right] > result:
                    result = self.tree[right]
                right -= 1
            left //= 2
            right //= 2

        return result


class SegmentTreeMin:
    """Segment tree supporting point updates and range minimum queries."""

    def __init__(self, size: int) -> None:
        """
        Initialize a min segment tree.

        Args:
            size: Number of leaves / coordinate-compressed values.

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
        self.tree: List[int] = [self.inf] * (2 * self.n)

    def update(self, index: int, value: int) -> None:
        """
        Set tree[index] = min(tree[index], value).

        Args:
            index: Leaf index to update.
            value: Candidate value.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1) auxiliary
        """
        pos: int = index + self.n
        if value >= self.tree[pos]:
            return
        self.tree[pos] = value
        pos //= 2
        while pos >= 1:
            left_val: int = self.tree[2 * pos]
            right_val: int = self.tree[2 * pos + 1]
            new_val: int = left_val if left_val <= right_val else right_val
            if self.tree[pos] == new_val:
                break
            self.tree[pos] = new_val
            pos //= 2

    def query(self, left: int, right: int) -> int:
        """
        Return minimum value in inclusive range [left, right].

        Args:
            left: Left boundary.
            right: Right boundary.

        Returns:
            Minimum stored value in the range, or +inf if empty / no value.

        Time complexity:
            O(log n)

        Space complexity:
            O(1) auxiliary
        """
        if left > right:
            return self.inf

        left += self.n
        right += self.n
        result: int = self.inf

        while left <= right:
            if left % 2 == 1:
                if self.tree[left] < result:
                    result = self.tree[left]
                left += 1
            if right % 2 == 0:
                if self.tree[right] < result:
                    result = self.tree[right]
                right -= 1
            left //= 2
            right //= 2

        return result


class Solution:
    def maximum_bookshelf_span(self, heights: List[int], budget: int) -> int:
        """
        Compute the maximum stable span score.

        Core idea:
        For each position j as the right endpoint, we want the best earlier index i
        such that:
            heights[i] is within [heights[j] - budget, heights[j] + budget]
        and the score is:
            (j - i) * min(heights[i], heights[j])

        We split valid earlier endpoints into two groups:

        1) heights[i] <= heights[j]
           Then min(heights[i], heights[j]) = heights[i]
           Score = (j - i) * heights[i] = j * heights[i] - i * heights[i]
           For fixed j, maximizing this means we want the maximum value of:
               (-i * heights[i])
           plus a j-dependent term.
           We process heights in descending order and maintain a segment tree over
           height values storing the maximum of (-i * height).

        2) heights[i] >= heights[j]
           Then min(heights[i], heights[j]) = heights[j]
           Score = (j - i) * heights[j]
           For fixed j and fixed heights[j], we simply want the smallest valid i.
           We process heights in ascending order and maintain a segment tree over
           height values storing the minimum index seen so far.

        Combining both gives an O(n log n) solution.

        Args:
            heights: List of bookshelf heights.
            budget: Maximum allowed absolute difference between endpoint heights.

        Returns:
            Maximum span score as an integer.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(heights)
        if n < 2:
            return 0

        # ---------------------------------------------------------------------
        # Coordinate compression:
        # Heights can be as large as 1e9, so we cannot build segment trees
        # directly on raw height values. Instead, we sort unique heights and
        # map each height to its compressed index.
        #
        # This lets us query all heights in a numeric interval [L, R] by using
        # binary search to find the compressed index range corresponding to that
        # interval.
        # ---------------------------------------------------------------------
        values: List[int] = sorted(set(heights))
        m: int = len(values)

        # ---------------------------------------------------------------------
        # Pass 1:
        # Handle earlier endpoints i with heights[i] <= heights[j].
        #
        # Valid heights[i] must lie in:
        #   [heights[j] - budget, heights[j]]
        #
        # For such i:
        #   score = (j - i) * heights[i]
        #         = j * heights[i] - i * heights[i]
        #
        # If we process j in descending order of heights[j], then before handling
        # a given j, we can insert all earlier indices i whose height is <= current
        # height. For each exact height h, we store the maximum value of (-i * h).
        #
        # Then for current j and any valid h in the allowed range:
        #   best score using that h = j * h + best(-i * h)
        #
        # A segment tree over compressed heights stores the maximum (-i * h).
        # ---------------------------------------------------------------------
        order_desc: List[int] = sorted(range(n), key=lambda idx: heights[idx], reverse=True)
        seg_max: SegmentTreeMax = SegmentTreeMax(m)
        best_answer: int = 0

        # Group indices by equal height so that when processing a height H, we first
        # query all positions with height H using only earlier positions whose height
        # is <= H, and only after that insert the current positions. This guarantees
        # i < j and avoids using the same index as both endpoints.
        p: int = len(order_desc) - 1
        while p >= 0:
            current_height: int = heights[order_desc[p]]
            group: List[int] = []

            while p >= 0 and heights[order_desc[p]] == current_height:
                group.append(order_desc[p])
                p -= 1

            # Sort the equal-height positions from left to right.
            # This is important because for equal heights, earlier indices in the same
            # group are valid earlier endpoints for later indices in the same group.
            group.sort()

            # Query phase for this height group.
            for j in group:
                low_height: int = current_height - budget
                left_idx: int = bisect_left(values, low_height)
                right_idx: int = bisect_right(values, current_height) - 1

                # Query the best stored value of (-i * heights[i]) among all earlier
                # indices with heights in the valid range and <= current_height.
                best_term: int = seg_max.query(left_idx, right_idx)
                if best_term != -1:
                    candidate: int = j * current_height + best_term
                    if candidate > best_answer:
                        best_answer = candidate

            # Update phase for this height group.
            # After querying, we insert each position so it can serve as an earlier
            # endpoint for future positions to the right.
            for i in group:
                compressed: int = bisect_left(values, current_height)
                seg_max.update(compressed, -i * current_height)

        # ---------------------------------------------------------------------
        # Pass 2:
        # Handle earlier endpoints i with heights[i] >= heights[j].
        #
        # Valid heights[i] must lie in:
        #   [heights[j], heights[j] + budget]
        #
        # For such i:
        #   score = (j - i) * heights[j]
        #
        # Since heights[j] is fixed for current j, maximizing the score means
        # minimizing i among valid earlier endpoints.
        #
        # We process heights in ascending order. Before handling a height H, we have
        # already inserted all earlier indices with height >= H? To achieve that, we
        # iterate groups from high to low while using a min segment tree storing the
        # smallest index seen for each height.
        #
        # For current j with height H, we query the minimum index among heights in
        # [H, H + budget]. That gives the best earlier endpoint in this case.
        # ---------------------------------------------------------------------
        order_asc: List[int] = sorted(range(n), key=lambda idx: heights[idx])
        seg_min: SegmentTreeMin = SegmentTreeMin(m)

        p = len(order_asc) - 1
        while p >= 0:
            current_height = heights[order_asc[p]]
            group = []

            while p >= 0 and heights[order_asc[p]] == current_height:
                group.append(order_asc[p])
                p -= 1

            group.sort()

            # Query phase.
            for j in group:
                high_height: int = current_height + budget
                left_idx = bisect_left(values, current_height)
                right_idx = bisect_right(values, high_height) - 1

                earliest_i: int = seg_min.query(left_idx, right_idx)
                if earliest_i != seg_min.inf:
                    candidate = (j - earliest_i) * current_height
                    if candidate > best_answer:
                        best_answer = candidate

            # Update phase.
            for i in group:
                compressed = bisect_left(values, current_height)
                seg_min.update(compressed, i)

        return best_answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    heights1: List[int] = [5, 1, 4, 6, 3]
    budget1: int = 2
    result1: int = solution.maximum_bookshelf_span(heights1, budget1)
    print(result1)  # Expected: 15

    # Example 2
    heights2: List[int] = [9, 2, 8, 1, 7]
    budget2: int = 0
    result2: int = solution.maximum_bookshelf_span(heights2, budget2)
    print(result2)  # Expected: 0

    # Additional quick checks
    heights3: List[int] = [4, 4]
    budget3: int = 0
    print(solution.maximum_bookshelf_span(heights3, budget3))  # Expected: 4

    heights4: List[int] = [1, 100, 1, 100, 1]
    budget4: int = 0
    print(solution.maximum_bookshelf_span(heights4, budget4))  # Expected: 200