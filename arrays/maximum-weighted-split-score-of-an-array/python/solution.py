"""
Title: Maximum Weighted Split Score of an Array

Problem Description:
You are given an integer array nums of length n and an integer array weights of length n.
You must choose two indices i and j such that 0 <= i < j < n - 1, splitting the array
into three non-empty contiguous parts:

- left = nums[0..i]
- middle = nums[i+1..j]
- right = nums[j+1..n-1]

The score of a split is defined as:

(sum(left) * min(weights in left))
+ (sum(middle) * min(weights in middle))
+ (sum(right) * min(weights in right))

Return the maximum possible score over all valid splits.

Constraints:
- 3 <= n <= 2 * 10^5
- 1 <= nums[i] <= 10^9
- 1 <= weights[i] <= 10^9
- The answer may exceed 32-bit integer range, so use 64-bit arithmetic.

Important note about the requested examples:
The example statements contain inconsistent "Output" values versus the values shown in
their own explanations. The explanations are internally correct:
- Example 1 explanation computes 43, not 31.
- Example 2 explanation computes 79, not 69.
This implementation follows the mathematical definition of the problem and therefore
returns the true maximum score.
"""

from typing import List, Optional, Tuple


class LiChaoNode:
    """Node used by the Li Chao segment tree."""

    def __init__(self, left: int, right: int) -> None:
        """
        Initialize a Li Chao tree node for a coordinate interval.

        Args:
            left: Left index in the compressed x-coordinate array.
            right: Right index in the compressed x-coordinate array.

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.left: int = left
        self.right: int = right
        self.line: Optional[Tuple[int, int]] = None  # (slope, intercept)
        self.left_child: Optional["LiChaoNode"] = None
        self.right_child: Optional["LiChaoNode"] = None


class LiChaoTree:
    """
    Li Chao segment tree for maximum queries over a fixed set of x-coordinates.

    We store lines of the form y = m * x + b and query the maximum y at a given x.
    """

    def __init__(self, xs: List[int]) -> None:
        """
        Build a Li Chao tree over compressed x-coordinates.

        Args:
            xs: Sorted unique x-values that will ever be queried.

        Returns:
            None

        Time complexity:
            O(k), where k is the number of unique x-values

        Space complexity:
            O(k) for the coordinate list, plus dynamic node allocation during inserts
        """
        self.xs: List[int] = xs
        self.root: LiChaoNode = LiChaoNode(0, len(xs) - 1)

    @staticmethod
    def _value(line: Tuple[int, int], x: int) -> int:
        """
        Evaluate a line at x.

        Args:
            line: A tuple (m, b) representing y = m * x + b.
            x: X-coordinate.

        Returns:
            The y-value.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        m, b = line
        return m * x + b

    def add_line(self, m: int, b: int) -> None:
        """
        Insert a line y = m * x + b into the structure.

        Args:
            m: Slope.
            b: Intercept.

        Returns:
            None

        Time complexity:
            O(log k) average / typical for fixed-coordinate Li Chao insertion

        Space complexity:
            O(log k) recursion depth, plus any newly created nodes
        """
        self._add_line(self.root, (m, b))

    def _add_line(self, node: LiChaoNode, new_line: Tuple[int, int]) -> None:
        """
        Recursive helper for line insertion.

        Args:
            node: Current Li Chao node.
            new_line: The line to insert.

        Returns:
            None

        Time complexity:
            O(log k) typical

        Space complexity:
            O(log k) recursion depth
        """
        l_idx = node.left
        r_idx = node.right
        mid_idx = (l_idx + r_idx) // 2

        x_left = self.xs[l_idx]
        x_mid = self.xs[mid_idx]
        x_right = self.xs[r_idx]

        # If this node has no line yet, simply store the new line here.
        if node.line is None:
            node.line = new_line
            return

        cur_line = node.line

        # We maintain the invariant that node.line is the better line at the midpoint.
        if self._value(new_line, x_mid) > self._value(cur_line, x_mid):
            node.line, new_line = new_line, node.line
            cur_line = node.line

        # If this interval is a single point, we are done.
        if l_idx == r_idx:
            return

        # Decide on which side the "losing" line might still beat the stored line.
        if self._value(new_line, x_left) > self._value(cur_line, x_left):
            if node.left_child is None:
                node.left_child = LiChaoNode(l_idx, mid_idx)
            self._add_line(node.left_child, new_line)
        elif self._value(new_line, x_right) > self._value(cur_line, x_right):
            if node.right_child is None:
                node.right_child = LiChaoNode(mid_idx + 1, r_idx)
            self._add_line(node.right_child, new_line)

    def query(self, x: int) -> int:
        """
        Query the maximum y-value among all inserted lines at coordinate x.

        Args:
            x: Query x-coordinate. Must be one of the coordinates used to build the tree.

        Returns:
            Maximum y-value at x.

        Time complexity:
            O(log k)

        Space complexity:
            O(1) excluding recursion stack if implemented recursively
        """
        idx = self._index_of_x(x)
        return self._query(self.root, x, idx)

    def _index_of_x(self, x: int) -> int:
        """
        Find the index of x in the compressed coordinate list.

        Args:
            x: X-coordinate known to exist in self.xs.

        Returns:
            Index of x.

        Time complexity:
            O(log k)

        Space complexity:
            O(1)
        """
        left = 0
        right = len(self.xs) - 1
        while left <= right:
            mid = (left + right) // 2
            if self.xs[mid] == x:
                return mid
            if self.xs[mid] < x:
                left = mid + 1
            else:
                right = mid - 1
        raise ValueError("Query x-coordinate not found in compressed coordinates.")

    def _query(self, node: Optional[LiChaoNode], x: int, idx: int) -> int:
        """
        Recursive helper for querying.

        Args:
            node: Current node.
            x: Query x-coordinate.
            idx: Compressed index of x.

        Returns:
            Maximum y-value from lines stored along the path.

        Time complexity:
            O(log k)

        Space complexity:
            O(log k) recursion depth
        """
        if node is None:
            return -(10**40)

        best = self._value(node.line, x) if node.line is not None else -(10**40)

        if node.left == node.right:
            return best

        mid = (node.left + node.right) // 2
        if idx <= mid:
            child_best = self._query(node.left_child, x, idx)
        else:
            child_best = self._query(node.right_child, x, idx)

        return max(best, child_best)


class SegmentTreeMax:
    """
    Segment tree supporting point updates and range maximum queries.

    This is used in the divide-and-conquer optimization step to maintain the best
    "right segment" contribution among suffixes whose minimum weight is at least
    a threshold.
    """

    def __init__(self, size: int) -> None:
        """
        Initialize a max segment tree.

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
        self.data: List[int] = [-(10**40)] * (2 * self.n)

    def update(self, index: int, value: int) -> None:
        """
        Set a position to max(current, value).

        Args:
            index: Leaf index.
            value: Value to merge in.

        Returns:
            None

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        pos = index + self.n
        if value > self.data[pos]:
            self.data[pos] = value
            pos //= 2
            while pos >= 1:
                self.data[pos] = max(self.data[2 * pos], self.data[2 * pos + 1])
                pos //= 2

    def query(self, left: int, right: int) -> int:
        """
        Query maximum on inclusive range [left, right].

        Args:
            left: Left index.
            right: Right index.

        Returns:
            Maximum value in the range.

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        if left > right:
            return -(10**40)

        left += self.n
        right += self.n
        result = -(10**40)

        while left <= right:
            if left % 2 == 1:
                result = max(result, self.data[left])
                left += 1
            if right % 2 == 0:
                result = max(result, self.data[right])
                right -= 1
            left //= 2
            right //= 2

        return result


class Solution:
    def max_weighted_split_score(self, nums: List[int], weights: List[int]) -> int:
        """
        Compute the maximum weighted split score over all valid 3-part splits.

        The algorithm combines:
        1. Prefix sums for O(1) segment sums.
        2. Monotonic-stack preprocessing to find, for every index, the maximal interval
           where that index is the minimum weight.
        3. A Li Chao tree to optimize the left+middle interaction.
        4. A divide-and-conquer routine with a segment tree to optimize the middle+right
           interaction.
        5. A final combination:
              answer = max_j( best_left_middle_ending_at_j + best_right_starting_at_j+1 )

        Args:
            nums: Array of positive integers.
            weights: Array of positive integers.

        Returns:
            Maximum possible score.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n = len(nums)

        # ------------------------------------------------------------
        # Step 1: Prefix sums.
        #
        # prefix[k] = sum(nums[0:k]), so:
        # sum(nums[l:r+1]) = prefix[r+1] - prefix[l]
        #
        # Prefix sums are essential because every candidate split score uses
        # segment sums repeatedly. Without prefix sums, even reading the sum
        # of one segment would cost O(length), which would be far too slow.
        # ------------------------------------------------------------
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + nums[i]

        # ------------------------------------------------------------
        # Step 2: For each position k, compute:
        #   prev_less[k] = nearest index to the left with weight < weights[k]
        #   next_less_eq[k] = nearest index to the right with weight <= weights[k]
        #
        # Why these exact inequalities?
        # We want each subarray's minimum to be "owned" by exactly one position.
        # A standard tie-breaking choice is:
        #   - previous strictly less
        #   - next less-or-equal
        #
        # Then position k is the designated minimum owner for every subarray [l..r]
        # satisfying:
        #   prev_less[k] < l <= k <= r < next_less_eq[k]
        #
        # This avoids double-counting equal minima.
        # ------------------------------------------------------------
        prev_less: List[int] = [-1] * n
        stack: List[int] = []
        for i in range(n):
            while stack and weights[stack[-1]] >= weights[i]:
                stack.pop()
            prev_less[i] = stack[-1] if stack else -1
            stack.append(i)

        next_less_eq: List[int] = [n] * n
        stack.clear()
        for i in range(n - 1, -1, -1):
            while stack and weights[stack[-1]] > weights[i]:
                stack.pop()
            next_less_eq[i] = stack[-1] if stack else n
            stack.append(i)

        # ------------------------------------------------------------
        # Step 3: Compute best_left_middle_ending_at[j].
        #
        # Definition:
        #   best_left_middle_ending_at[j] =
        #       max over i in [0..j-1] of
        #           score(left = [0..i]) + score(middle = [i+1..j])
        #
        # We need this for every j from 1 to n-2, because the right segment
        # must still be non-empty.
        #
        # Let's derive a form suitable for optimization.
        #
        # For a fixed middle segment [l..j] where l = i+1:
        #   left contribution = prefix[l-1] * min(weights[0..l-1])
        #   middle contribution = (prefix[j+1] - prefix[l]) * min(weights[l..j])
        #
        # If position k is the designated minimum owner of the middle segment,
        # then k must satisfy l <= k <= j and:
        #   prev_less[k] < l
        #   j < next_less_eq[k]
        #
        # For fixed k and j, middle minimum is weights[k], so:
        #   total = left_score_at_(l-1) + (prefix[j+1] - prefix[l]) * weights[k]
        #         = (left_score_at_(l-1) - prefix[l] * weights[k]) + prefix[j+1] * weights[k]
        #
        # For fixed k, the only part depending on l is:
        #   left_score_at_(l-1) - prefix[l] * weights[k]
        #
        # This is a line in x = weights[k]:
        #   y = (-prefix[l]) * x + left_score_at_(l-1)
        #
        # So for each k, among all valid l values, we need the maximum of that
        # expression at x = weights[k]. A Li Chao tree handles exactly this.
        #
        # We process l from left to right. When l becomes available, we add the
        # corresponding line to all k for which l is allowed, i.e. all k with
        # prev_less[k] < l <= k. Equivalently, line for l is active on k in
        # [l .. next_greater_or_equal?] -- but because the condition depends on
        # prev_less[k], it is easier to group by the threshold:
        #
        # A position k becomes eligible for line l exactly when l > prev_less[k].
        # During a sweep over l, we can insert the line into a global Li Chao tree
        # and query only those k whose prev_less[k] < current_l and k >= current_l.
        #
        # However, we still need results by ending index j, not by minimum owner k.
        #
        # A cleaner equivalent formulation:
        # For each j, consider all k with k <= j < next_less_eq[k].
        # For each such k, valid l range is [max(prev_less[k] + 1, 1) .. k].
        # We precompute:
        #   best_start_for_middle_owner[k] =
        #       max over l in [max(prev_less[k]+1,1)..k] of
        #           left_score_at_(l-1) - prefix[l] * weights[k]
        #
        # Then for every j in [k .. next_less_eq[k)-1]:
        #   candidate = best_start_for_middle_owner[k] + prefix[j+1] * weights[k]
        #
        # This is again a line in x = prefix[j+1]:
        #   y = weights[k] * x + best_start_for_middle_owner[k]
        #
        # Therefore:
        #   1) compute best_start_for_middle_owner[k] using a Li Chao tree over x=weights[k]
        #   2) add line y = weights[k] * x + best_start_for_middle_owner[k] active on
        #      j in [k .. next_less_eq[k)-1]
        #   3) query at x = prefix[j+1] to get best_left_middle_ending_at[j]
        #
        # We handle active intervals with add/remove events, using a segment tree of
        # vectors plus a DFS-like offline approach would be possible, but there is a
        # simpler route because lines are only added and expire later. We instead use
        # divide-and-conquer over j for the right side and a direct heapless method here:
        # since each k contributes to a contiguous j interval, we can process j left-to-right
        # with a Li Chao tree that only adds lines when they start. To handle expiry, we use
        # a segment-tree-over-time offline insertion.
        # ------------------------------------------------------------

        # left_score_end[i] = contribution of prefix segment [0..i]
        # Since all nums are positive, this is well-defined and easy to compute:
        # min weight on prefix [0..i] can be maintained incrementally.
        left_score_end: List[int] = [0] * n
        current_prefix_min = 10**40
        for i in range(n):
            current_prefix_min = min(current_prefix_min, weights[i])
            left_score_end[i] = prefix[i + 1] * current_prefix_min

        # Build compressed x-coordinates for the first Li Chao tree:
        # we query at x = weights[k].
        unique_weights = sorted(set(weights))
        lichao_for_starts = LiChaoTree(unique_weights)

        # best_start_for_middle_owner[k] as defined above.
        best_start_for_middle_owner: List[int] = [-(10**