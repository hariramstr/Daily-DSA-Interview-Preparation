"""
Title: Maximum Secure Routing Score in a Labeled Tree

Problem Description:
You are given an undirected tree with n nodes numbered from 0 to n - 1. Each node
represents a router, and each router has a security label given by an integer value.
A route is defined as any simple path between two nodes in the tree, including a
path of length 0 consisting of a single node.

The secure routing score of a route is defined as:

    (score of the route) = (sum of labels on the path)
                           - (maximum label on the path)
                           + (minimum label on the path)

Your task is to return the maximum secure routing score among all possible routes
in the tree.

A path may start and end at any two nodes, and the endpoints do not need to include
the root. The tree is not necessarily binary.

This problem is challenging because the best path is not determined only by path sum,
diameter, or subtree information. The contribution of both the minimum and maximum
label on the chosen path must be tracked consistently while combining candidate paths
across branches.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= labels[i] <= 10^9
- edges.length == n - 1
- edges[i] = [ui, vi]
- 0 <= ui, vi < n
- The input edges always form a valid tree.
"""

from __future__ import annotations

from typing import List, Tuple
import sys


class LiChaoMax:
    """
    Dynamic Li Chao tree for maximum queries on a fixed x-domain.

    This structure stores lines of the form:
        y = m * x + b
    and supports:
        - add_line(m, b)
        - query(x): maximum y at x

    The x-domain is known in advance and is integer-based.

    Time complexity:
        - add_line: O(log X)
        - query: O(log X)

    Space complexity:
        - O(number of inserted lines * log X) in the worst case for dynamic nodes
    """

    class Node:
        __slots__ = ("l", "r", "line", "left", "right")

        def __init__(self, l: int, r: int) -> None:
            self.l = l
            self.r = r
            self.line: Tuple[int, int] | None = None
            self.left: LiChaoMax.Node | None = None
            self.right: LiChaoMax.Node | None = None

    def __init__(self, x_left: int, x_right: int) -> None:
        """
        Initialize the Li Chao tree.

        Args:
            x_left: Minimum x value in the domain.
            x_right: Maximum x value in the domain.

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.root = LiChaoMax.Node(x_left, x_right)

    @staticmethod
    def _value(line: Tuple[int, int], x: int) -> int:
        """
        Evaluate a line at x.

        Args:
            line: Tuple (m, b) representing y = m*x + b.
            x: Query x-coordinate.

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
        Insert a line y = m*x + b into the structure.

        Args:
            m: Slope.
            b: Intercept.

        Returns:
            None

        Time complexity:
            O(log X)

        Space complexity:
            O(log X) due to dynamic node creation
        """
        self._add_line(self.root, (m, b))

    def _add_line(self, node: Node, new_line: Tuple[int, int]) -> None:
        """
        Internal recursive insertion for Li Chao tree.

        Args:
            node: Current segment tree node.
            new_line: Line to insert.

        Returns:
            None

        Time complexity:
            O(log X)

        Space complexity:
            O(log X)
        """
        l, r = node.l, node.r
        mid = (l + r) >> 1

        if node.line is None:
            node.line = new_line
            return

        cur = node.line

        # We keep the better line at the midpoint.
        if self._value(new_line, mid) > self._value(cur, mid):
            node.line, new_line = new_line, node.line

        if l == r:
            return

        # Decide where the worse line can still beat the stored line.
        if self._value(new_line, l) > self._value(node.line, l):
            if node.left is None:
                node.left = LiChaoMax.Node(l, mid)
            self._add_line(node.left, new_line)
        elif self._value(new_line, r) > self._value(node.line, r):
            if node.right is None:
                node.right = LiChaoMax.Node(mid + 1, r)
            self._add_line(node.right, new_line)

    def query(self, x: int) -> int:
        """
        Query the maximum y-value at x.

        Args:
            x: Query x-coordinate.

        Returns:
            Maximum y among all inserted lines at x.

        Time complexity:
            O(log X)

        Space complexity:
            O(1) auxiliary, ignoring recursion stack
        """
        return self._query(self.root, x)

    def _query(self, node: Node | None, x: int) -> int:
        """
        Internal recursive query.

        Args:
            node: Current segment tree node.
            x: Query x-coordinate.

        Returns:
            Maximum y-value at x.

        Time complexity:
            O(log X)

        Space complexity:
            O(log X) recursion stack
        """
        if node is None:
            return -10**40

        ans = -10**40
        if node.line is not None:
            ans = self._value(node.line, x)

        if node.l == node.r:
            return ans

        mid = (node.l + node.r) >> 1
        if x <= mid:
            child_val = self._query(node.left, x)
        else:
            child_val = self._query(node.right, x)

        return ans if ans > child_val else child_val


class Solution:
    def max_secure_routing_score(self, n: int, labels: List[int], edges: List[List[int]]) -> int:
        """
        Compute the maximum secure routing score over all simple paths in the tree.

        Core idea:
        The score of a path is:
            sum(path) - max(path) + min(path)

        We solve this using centroid decomposition and a careful algebraic split.

        For any path that passes through a chosen centroid c, suppose one endpoint is in
        subtree A and the other endpoint is in subtree B (possibly one side is just c).
        Let:
            S = sum of labels on the path
            MX = maximum label on the path
            MN = minimum label on the path

        Then score = S - MX + MN.

        During centroid processing, every path through c can be represented as a merge of
        two "arms" from c to endpoints. We enumerate one arm from the current subtree and
        combine it with previously processed arms from other subtrees.

        The difficult part is handling max/min consistently. We split into cases based on
        which side contributes the global maximum and which side contributes the global minimum.
        Each case becomes a linear expression that can be optimized with a Li Chao tree.

        Args:
            n: Number of nodes.
            labels: Security label for each node.
            edges: Undirected tree edges.

        Returns:
            Maximum secure routing score.

        Time complexity:
            O(n log^2 n) in practice with centroid decomposition and Li Chao trees.

        Space complexity:
            O(n log n) in the worst case due to decomposition and temporary structures.
        """
        sys.setrecursionlimit(1_000_000)

        # Build adjacency list for the tree.
        graph: List[List[int]] = [[] for _ in range(n)]
        for u, v in edges:
            graph[u].append(v)
            graph[v].append(u)

        # Coordinate compression for label values.
        # Li Chao tree works on x-domain; we use compressed label values as x.
        sorted_vals = sorted(set(labels))
        value_to_idx = {v: i for i, v in enumerate(sorted_vals)}
        comp = [value_to_idx[v] for v in labels]
        x_left = 0
        x_right = len(sorted_vals) - 1

        # Centroid decomposition bookkeeping.
        removed = [False] * n
        subtree_size = [0] * n

        # Global answer. Single-node path is always valid and has score = label.
        answer = max(labels)

        def calc_size(u: int, p: int) -> int:
            """
            Compute subtree sizes for centroid decomposition.

            Args:
                u: Current node.
                p: Parent in DFS.

            Returns:
                Size of the current active subtree.

            Time complexity:
                O(size of active subtree)

            Space complexity:
                O(height of DFS)
            """
            subtree_size[u] = 1
            for v in graph[u]:
                if v != p and not removed[v]:
                    subtree_size[u] += calc_size(v, u)
            return subtree_size[u]

        def find_centroid(u: int, p: int, total: int) -> int:
            """
            Find centroid of the current active subtree.

            Args:
                u: Current node.
                p: Parent in DFS.
                total: Total size of active subtree.

            Returns:
                The centroid node.

            Time complexity:
                O(size of active subtree)

            Space complexity:
                O(height of DFS)
            """
            for v in graph[u]:
                if v != p and not removed[v]:
                    if subtree_size[v] > total // 2:
                        return find_centroid(v, u, total)
            return u

        def collect_paths(
            u: int,
            p: int,
            sum_from_c: int,
            min_from_c: int,
            max_from_c: int,
            out: List[Tuple[int, int, int]],
        ) -> None:
            """
            Collect all arm-states from centroid c to nodes in one child subtree.

            Each collected tuple is:
                (sum_from_c, min_from_c, max_from_c)

            where the path includes the centroid and the endpoint node.

            Args:
                u: Current node in DFS.
                p: Parent in DFS.
                sum_from_c: Sum of labels on path centroid -> u.
                min_from_c: Minimum label on path centroid -> u.
                max_from_c: Maximum label on path centroid -> u.
                out: Output list to append states.

            Returns:
                None

            Time complexity:
                O(size of traversed subtree)

            Space complexity:
                O(height of DFS)
            """
            out.append((sum_from_c, min_from_c, max_from_c))
            for v in graph[u]:
                if v != p and not removed[v]:
                    nv = labels[v]
                    collect_paths(
                        v,
                        u,
                        sum_from_c + nv,
                        min(min_from_c, nv),
                        max(max_from_c, nv),
                        out,
                    )

        def decompose(entry: int) -> None:
            """
            Perform centroid decomposition recursively and update the global answer.

            Args:
                entry: Any node in the current active subtree.

            Returns:
                None

            Time complexity:
                O(size * log^2 size) over the whole decomposition overall

            Space complexity:
                O(size) temporary plus recursion
            """
            nonlocal answer

            total = calc_size(entry, -1)
            c = find_centroid(entry, -1, total)
            removed[c] = True

            c_val = labels[c]

            # These Li Chao trees store transformed information for previously processed
            # subtrees around centroid c.
            #
            # We need four structures corresponding to the four combinations:
            # 1) previous side supplies both max and min
            # 2) previous side supplies max, current side supplies min
            # 3) current side supplies max, previous side supplies min
            # 4) current side supplies both max and min
            #
            # After algebra, each case can be written as:
            #   query over previous side = max of (m * x + b)
            # where x is either current min or current max.
            #
            # Let a previous arm have:
            #   sumA, minA, maxA
            # and current arm have:
            #   sumB, minB, maxB
            #
            # Full path score through centroid:
            #   (sumA + sumB - c_val) - max(maxA, maxB) + min(minA, minB)
            #
            # Splitting by which side contributes global max/min gives:
            #
            # Case P/P: max=maxA, min=minA, requiring maxA>=maxB and minA<=minB
            #   score = sumB - c_val + (sumA - maxA + minA)
            #   Need previous arms with minA <= minB and maxA >= maxB.
            #
            # Case P/C: max=maxA, min=minB, requiring maxA>=maxB and minA>=minB
            #   score = sumB - c_val + minB + (sumA - maxA)
            #   Need previous arms with maxA >= maxB.
            #
            # Case C/P: max=maxB, min=minA, requiring maxA<=maxB and minA<=minB
            #   score = sumB - c_val - maxB + (sumA + minA)
            #   Need previous arms with minA <= minB.
            #
            # Case C/C: max=maxB, min=minB, requiring maxA<=maxB and minA>=minB
            #   score = sumB - c_val - maxB + minB + sumA
            #   No extra value from previous min/max beyond feasibility.
            #
            # We encode feasibility by grouping previous arms and querying maxima via
            # Li Chao trees over compressed label coordinates.

            # For suffix over maxA >= x:
            #   case P/P needs max over (sumA - maxA + minA) among previous arms with minA <= current_min
            #   case P/C needs max over (sumA - maxA) among all previous arms with maxA >= current_max
            #
            # For prefix over maxA <= x:
            #   case C/P needs max over (sumA + minA) among previous arms with minA <= current_min
            #   case C/C needs max over sumA among previous arms with minA >= current_min
            #
            # To support min constraints too, we use line trick:
            #
            # For condition minA <= current_min:
            #   Insert line at x = minA? Since query is threshold-based, we instead use
            #   slope 0 lines in Li Chao but activate them in Fenwick-like direction is not enough.
            #
            # A simpler and still efficient approach here:
            #   For each centroid, collect all arms, sort by min/max, and process offline.
            #
            # Because each centroid handles only its local nodes, total complexity remains
            # O(n log^2 n).

            # Gather child subtree arm lists.
            child_lists: List[List[Tuple[int, int, int]]] = []
            for v in graph[c]:
                if not removed[v]:
                    arr: List[Tuple[int, int, int]] = []
                    collect_paths(v, c, c_val + labels[v], min(c_val, labels[v]), max(c_val, labels[v]), arr)
                    child_lists.append(arr)

            # Also consider the centroid alone as an arm available for combinations.
            # This allows paths where one endpoint is exactly the centroid.
            centroid_arm = (c_val, c_val, c_val)

            # Helper to process combinations between "current subtree arms" and
            # "previously processed arms + centroid arm".
            def process_against_previous(
                current_arms: List[Tuple[int, int, int]],
                previous_arms: List[Tuple[int, int, int]],
            ) -> None:
                """
                Combine current subtree arms with previously processed arms through centroid.

                Args:
                    current_arms: Arms from one child subtree.
                    previous_arms: Arms from already processed subtrees plus centroid arm.

                Returns:
                    None

                Time complexity:
                    O((a + b) log V) for each offline sweep, several sweeps total

                Space complexity:
                    O(a + b)
                """
                nonlocal answer

                if not current_arms or not previous_arms:
                    return

                # Convert arms into richer records to make formulas easier to read.
                # Each record:
                #   (sum, min, max)
                cur = current_arms
                prev = previous_arms

                # Case P/P:
                # Need previous min <= cur_min and previous max >= cur_max
                # Maximize prev_score = sumA - maxA + minA
                #
                # Offline:
                #   sort current by cur_min ascending
                #   sort previous by minA ascending
                #   insert into Li Chao lines with slope 0? We need threshold on maxA >= cur_max.
                #   We can encode each previous arm as a horizontal line active at x=maxA and query suffix.
                #
                # For suffix threshold on maxA >= x, use line y=const and x=maxA with Li Chao is not enough.
                # Instead, sort by max descending and maintain best constants.
                #
                # Since expression is constant after feasibility, we can solve each case with sorting
                # and prefix/suffix maxima, no Li Chao actually needed. This is much simpler because
                # all formulas are affine only in current min/max with coefficients -1, +1, or 0.
                #
                # We therefore use four offline sweeps with sorting and Fenwick-like max arrays over
                # compressed coordinates.

                size_v = len(sorted_vals)

                # Utility arrays for coordinate-based max queries.
                neg_inf = -10**40

                # -------------------------
                # Case P/P
                # previous min <= cur_min, previous max >= cur_max
                # score = cur_sum - c_val + (prev_sum - prev_max + prev_min)
                # -------------------------
                prev_sorted = sorted(prev, key=lambda t: t[1])  # by min ascending
                cur_sorted = sorted(cur, key=lambda t: t[1])    # by current min ascending

                suffix_best = [neg_inf] * (size_v + 1)
                bucket = [neg_inf] * size_v
                ptr = 0

                def rebuild_suffix_from_bucket() -> None:
                    """
                    Build suffix maxima over max-coordinate buckets.

                   