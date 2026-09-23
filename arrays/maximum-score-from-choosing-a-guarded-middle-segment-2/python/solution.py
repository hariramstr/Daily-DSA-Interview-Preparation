"""
Title: Maximum Score from Choosing a Guarded Middle Segment

Problem Description:
You are given an integer array nums of length n and two non-negative integers L and R,
where 0 <= L, R < n. You must choose exactly one non-empty contiguous subarray nums[i..j]
as your active segment. The segment is considered valid only if there are at least L
elements strictly to its left and at least R elements strictly to its right. In other
words, the chosen segment must satisfy i >= L and j <= n - 1 - R.

The score of a valid segment is defined as:

    (minimum value inside the segment) * (sum of all values inside the segment)

Your task is to return the maximum possible score among all valid segments.

This is not simply a maximum-subarray problem, because the score depends on both the
segment sum and the segment minimum. Negative numbers are allowed, so extending a segment
may increase or decrease the score in non-obvious ways. An O(n^2) solution will not pass.

Constraints:
- 1 <= n <= 2 * 10^5
- -10^9 <= nums[i] <= 10^9
- 0 <= L, R < n
- There is guaranteed to be at least one valid segment

Notes:
- The answer may exceed 32-bit integer range, so use 64-bit arithmetic.
- A strong solution typically combines monotonic structure ideas with prefix sums and
  range optimization.
"""

from typing import List, Optional


class LiChaoNode:
    """
    Node for a Li Chao segment tree over a discrete set of x-coordinates.

    Each stored line has form:
        y = m * x + b

    The tree supports:
    - insert line
    - query maximum y at a given x

    This implementation is specialized for integer x-values taken from a fixed sorted list.
    """

    __slots__ = ("left", "right", "line")

    def __init__(self) -> None:
        """
        Initialize an empty Li Chao node.

        Args:
            None

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.left: Optional["LiChaoNode"] = None
        self.right: Optional["LiChaoNode"] = None
        self.line: Optional[tuple[int, int]] = None


class LiChaoTree:
    """
    Li Chao tree for maximum queries on a fixed discrete x-domain.

    We use this to optimize expressions of the form:
        max over inserted lines (m * x + b)

    In this problem:
    - x will be a candidate minimum value nums[k]
    - lines are built from prefix sums
    """

    def __init__(self, xs: List[int]) -> None:
        """
        Create a Li Chao tree over the sorted unique x-values.

        Args:
            xs: Sorted unique x-coordinates where queries will happen.

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1) excluding the input list reference
        """
        self.xs: List[int] = xs
        self.root: Optional[LiChaoNode] = None

    @staticmethod
    def _value(line: tuple[int, int], x: int) -> int:
        """
        Evaluate a line at x.

        Args:
            line: Tuple (m, b) representing y = m * x + b
            x: Query x-coordinate

        Returns:
            The value m * x + b

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        m, b = line
        return m * x + b

    def insert_line(self, m: int, b: int) -> None:
        """
        Insert a line y = m * x + b into the structure.

        Args:
            m: Slope
            b: Intercept

        Returns:
            None

        Time complexity:
            O(log M), where M is number of distinct x-values

        Space complexity:
            O(log M) recursion depth in worst case
        """
        line = (m, b)
        if self.root is None:
            self.root = LiChaoNode()
        self._insert(self.root, 0, len(self.xs) - 1, line)

    def _insert(self, node: LiChaoNode, left: int, right: int, line: tuple[int, int]) -> None:
        """
        Recursive helper for line insertion.

        Args:
            node: Current tree node
            left: Left index in xs
            right: Right index in xs
            line: Line to insert

        Returns:
            None

        Time complexity:
            O(log M)

        Space complexity:
            O(log M) recursion depth
        """
        if node.line is None:
            node.line = line
            return

        mid = (left + right) // 2
        x_left = self.xs[left]
        x_mid = self.xs[mid]
        x_right = self.xs[right]

        current = node.line

        # We maintain the line that is better at the midpoint in the current node.
        # If the new line is better at the midpoint, swap them.
        if self._value(line, x_mid) > self._value(current, x_mid):
            node.line, line = line, current
            current = node.line

        if left == right:
            return

        # Now decide where the remaining worse-at-mid line could still beat the stored line.
        # If it is better on the left endpoint, it may matter in the left child.
        if self._value(line, x_left) > self._value(node.line, x_left):
            if node.left is None:
                node.left = LiChaoNode()
            self._insert(node.left, left, mid, line)
        # Else if it is better on the right endpoint, it may matter in the right child.
        elif self._value(line, x_right) > self._value(node.line, x_right):
            if node.right is None:
                node.right = LiChaoNode()
            self._insert(node.right, mid + 1, right, line)

    def query(self, x: int) -> int:
        """
        Query the maximum y-value at x among all inserted lines.

        Args:
            x: Query x-coordinate

        Returns:
            Maximum value at x

        Time complexity:
            O(log M)

        Space complexity:
            O(1) iterative / O(log M) if counting recursion in helper
        """
        if self.root is None:
            raise ValueError("Query on empty Li Chao tree is not allowed.")

        # Binary search to find the discrete index of x.
        lo = 0
        hi = len(self.xs) - 1
        idx = -1
        while lo <= hi:
            mid = (lo + hi) // 2
            if self.xs[mid] == x:
                idx = mid
                break
            if self.xs[mid] < x:
                lo = mid + 1
            else:
                hi = mid - 1

        if idx == -1:
            raise ValueError("Query x must belong to the predefined coordinate set.")

        return self._query(self.root, 0, len(self.xs) - 1, idx, x)

    def _query(self, node: Optional[LiChaoNode], left: int, right: int, idx: int, x: int) -> int:
        """
        Recursive helper for maximum query.

        Args:
            node: Current tree node
            left: Left index in xs
            right: Right index in xs
            idx: Index of x in xs
            x: Actual x-coordinate

        Returns:
            Maximum line value at x from this subtree

        Time complexity:
            O(log M)

        Space complexity:
            O(log M) recursion depth
        """
        if node is None:
            return -(10 ** 40)

        best = -(10 ** 40)
        if node.line is not None:
            best = self._value(node.line, x)

        if left == right:
            return best

        mid = (left + right) // 2
        if idx <= mid:
            child_best = self._query(node.left, left, mid, idx, x)
        else:
            child_best = self._query(node.right, mid + 1, right, idx, x)

        return max(best, child_best)


class Solution:
    def maximumScore(self, nums: List[int], L: int, R: int) -> int:
        """
        Compute the maximum score of a valid guarded segment.

        The key idea is:
        1. For each index k, treat nums[k] as the minimum of the chosen segment.
        2. Using monotonic stack, find the maximal interval [left_bound, right_bound]
           where nums[k] is the minimum (with a careful tie-breaking rule so every
           segment is assigned to exactly one pivot index).
        3. Intersect that interval with the guard constraints:
               start >= L
               end   <= n - 1 - R
        4. Among all segments [i..j] inside that allowed interval and containing k,
           maximize:
               nums[k] * (prefix[j+1] - prefix[i])
        5. This becomes:
               if nums[k] >= 0:
                   nums[k] * (max prefix on right - min prefix on left)
               else:
                   nums[k] * (min prefix on right - max prefix on left)
        6. We answer these interval prefix min/max queries offline using Li Chao trees.

        Args:
            nums: Input integer array
            L: Minimum number of elements strictly to the left of chosen segment
            R: Minimum number of elements strictly to the right of chosen segment

        Returns:
            Maximum possible score as an integer

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n = len(nums)

        # ------------------------------------------------------------
        # Step 1: Prefix sums
        #
        # prefix[t] = sum(nums[0:t])
        #
        # Then sum of subarray nums[i..j] is:
        #   prefix[j + 1] - prefix[i]
        #
        # We will repeatedly need subarray sums, so prefix sums are essential.
        # ------------------------------------------------------------
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + nums[i]

        # ------------------------------------------------------------
        # Step 2: Compute ownership interval for each index as the chosen minimum.
        #
        # We need a standard "subarray minimum ownership" partition:
        # - prev strictly smaller element
        # - next smaller-or-equal element
        #
        # This tie-breaking ensures every subarray is assigned to exactly one index.
        #
        # For index k:
        #   left_bound  = prev_less[k] + 1
        #   right_bound = next_less_equal[k] - 1
        #
        # Any subarray [i..j] with:
        #   left_bound <= i <= k <= j <= right_bound
        # has nums[k] as the designated minimum owner.
        # ------------------------------------------------------------
        prev_less: List[int] = [-1] * n
        stack: List[int] = []
        for i in range(n):
            while stack and nums[stack[-1]] >= nums[i]:
                stack.pop()
            prev_less[i] = stack[-1] if stack else -1
            stack.append(i)

        next_less_equal: List[int] = [n] * n
        stack.clear()
        for i in range(n - 1, -1, -1):
            while stack and nums[stack[-1]] > nums[i]:
                stack.pop()
            next_less_equal[i] = stack[-1] if stack else n
            stack.append(i)

        # ------------------------------------------------------------
        # Step 3: Translate guard constraints.
        #
        # Valid segment [i..j] must satisfy:
        #   i >= L
        #   j <= n - 1 - R
        #
        # Let:
        #   max_end = n - 1 - R
        #
        # There is guaranteed to be at least one valid segment.
        # ------------------------------------------------------------
        max_end = n - 1 - R

        # ------------------------------------------------------------
        # Step 4: For each k, determine the allowed ranges of prefix indices.
        #
        # If a segment [i..j] is owned by k and valid, then:
        #   i in [max(left_bound, L), k]
        #   j in [k, min(right_bound, max_end)]
        #
        # In prefix-sum terms:
        #   left prefix index  = i
        #   right prefix index = j + 1
        #
        # So:
        #   i_prefix in [left_start, left_end] where
        #       left_start = max(left_bound, L)
        #       left_end   = k
        #
        #   r_prefix in [right_start, right_end] where
        #       right_start = k + 1
        #       right_end   = min(right_bound, max_end) + 1
        #
        # If either range is empty, index k cannot produce a valid segment.
        # ------------------------------------------------------------
        left_ranges: List[Optional[tuple[int, int]]] = [None] * n
        right_ranges: List[Optional[tuple[int, int]]] = [None] * n

        for k in range(n):
            left_bound = prev_less[k] + 1
            right_bound = next_less_equal[k] - 1

            left_start = max(left_bound, L)
            left_end = k

            right_start = k + 1
            right_end = min(right_bound, max_end) + 1

            if left_start <= left_end and right_start <= right_end:
                left_ranges[k] = (left_start, left_end)
                right_ranges[k] = (right_start, right_end)

        # ------------------------------------------------------------
        # Step 5: We need interval min/max queries on prefix sums, but the interval
        # depends on k and the final score multiplies by nums[k].
        #
        # For a fixed k:
        #   best_sum = best_right_prefix - best_left_prefix
        #
        # If nums[k] >= 0:
        #   maximize sum => maximize right prefix and minimize left prefix
        #
        # If nums[k] < 0:
        #   maximize nums[k] * sum => minimize sum
        #   so minimize (right prefix - left prefix)
        #   => minimize right prefix and maximize left prefix
        #
        # Therefore we need, for each k:
        #   max prefix on right interval
        #   min prefix on right interval
        #   max prefix on left interval
        #   min prefix on left interval
        #
        # We compute these offline using Li Chao trees.
        #
        # Why Li Chao?
        # Because each prefix value p at position t contributes:
        #   for max of nums[k] * p over active t, line y = p * x
        #   for min of nums[k] * p, we can maximize (-p) * x and negate later
        #
        # We process interval queries by sorting events by left endpoint and sweeping
        # the right endpoint, inserting active prefix positions into the tree.
        # ------------------------------------------------------------

        # All x-values where we will query are nums[k].
        xs = sorted(set(nums))

        # Prepare answer arrays for the four needed interval statistics.
        right_max_val: List[int] = [0] * n
        right_min_val: List[int] = [0] * n
        left_max_val: List[int] = [0] * n
        left_min_val: List[int] = [0] * n

        # ------------------------------------------------------------
        # Helper function:
        # Solve interval queries [ql, qr] on the prefix array values.
        #
        # For each query associated with index k and x = nums[k]:
        # - max_prefix_in_interval can be obtained by inserting lines y = prefix[t] * x
        #   and querying maximum at x = nums[k], then dividing conceptually by x is not
        #   needed because query directly returns prefix[t] * nums[k].
        #
        # But we actually need the raw prefix value, not multiplied.
        #
        # To avoid division/sign issues, we instead use:
        #   query max of p * x at x = nums[k]
        # and since x varies, this gives the correct "best weighted prefix" directly.
        #
        # For the final score formulas:
        #   if nums[k] >= 0:
        #       score = max_right_weighted - min_left_weighted
        #   if nums[k] < 0:
        #       score = min_right_weighted - max_left_weighted
        #
        # where:
        #   max_right_weighted = nums[k] * max(prefix on right)
        #   min_right_weighted = nums[k] * min(prefix on right)
        # etc.
        #
        # This is exactly what Li Chao gives us efficiently.
        # ------------------------------------------------------------
        def solve_weighted_interval_queries(
            ranges: List[Optional[tuple[int, int]]],
            want_max_weighted: bool,
        ) -> List[int]:
            """
            Offline interval query solver for weighted prefix optimization.

            For each index k with interval [l, r], compute:
                if want_max_weighted:
                    max over t in [l, r] of nums[k] * prefix[t]
                else:
                    min over t in [l, r] of nums[k] * prefix[t]

            Args:
                ranges: List of optional intervals on prefix indices
                want_max_weighted: Whether to compute weighted maximum or minimum

            Returns:
                List ans where ans[k] is the requested weighted optimum

            Time complexity:
                O(n log n)

            Space complexity:
                O(n)
            """
            queries_by_left: List[List[tuple[int, int]]] = [[] for _ in range(n + 1)]
            for k, interval in enumerate(ranges):
                if interval is not None:
                    l, r = interval
                    queries_by_left[l].append((r, k))

            # We sweep left boundary from n down to 0.
            #
            # At sweep position current_left, the active set contains all prefix indices t
            # such that t >= current_left and already inserted.
            #
            # To answer interval [l, r], we need only t in [l, r].
            # So we group queries by l and use a Fenwick-like idea? Not enough for weighted
            # optimization with varying x.
            #
            # Instead, we use a divide-and-conquer style sweep with right endpoint ordering:
            # Here we choose a simpler and still O(n log n) method:
            # process queries sorted by r, while inserting positions in increasing order,
            # but to enforce lower bound l we run a second pass on reversed indices.
            #
            # However