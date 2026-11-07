"""
Title: Maximum Secure Relay Score in a Permission Tree

Problem Description:
You are given a rooted tree with nodes numbered from 0 to n - 1, where node 0 is the root.
Each node represents a service in a distributed system and has an integer permission value.

For any two nodes u and v such that u is an ancestor of v (possibly u = v), define the secure
relay score of the downward path from u to v as:

    (maximum permission value on the path)
    - (minimum permission value on the path)
    - (path length in edges)

Your task is to compute the maximum secure relay score over all ancestor-to-descendant paths
in the tree.

A path must always go downward in the rooted tree. The path length of a single node is 0.
The maximum and minimum permission values are taken over all nodes on that path.

Constraints:
- 1 <= n <= 200000
- -10^9 <= permission[i] <= 10^9
- parent[0] = -1
- for i > 0, 0 <= parent[i] < n
- The input parent array always forms a valid tree

Important note about the examples:
The first example text in the prompt is internally inconsistent. If we compute the score exactly
as defined, the result for:
    parent = [-1,0,0,1,1,2]
    permission = [5,1,9,7,3,10]
is 7, achieved by path 1 -> 3 with values [1, 7]:
    max - min - length = 7 - 1 - 1 = 5
and path 0 -> 2 -> 5 with values [5, 9, 10]:
    10 - 5 - 2 = 3
The true optimum is path 0 -> 1 -> 3:
    values [5, 1, 7], score = 7 - 1 - 2 = 4
Actually the best path is 1 -> 3 with score 5, and no path reaches 8.
So the prompt's Example 1 output is incorrect.

The second example is consistent:
    parent = [-1,0,1,2,3]
    permission = [8,2,-4,6,12]
The best path is 1 -> 4:
    values [2, -4, 6, 12], score = 12 - (-4) - 3 = 13

This solution implements the mathematically correct interpretation of the problem statement.
"""

from __future__ import annotations

from typing import List, Tuple


class LiChaoMax:
    """
    Dynamic Li Chao tree for maximum queries on a fixed set of x-coordinates.

    We use coordinate compression because all query x-values are known in advance:
    they are exactly the permission values of the nodes.

    Each inserted line has form:
        y = m * x + b

    Query returns the maximum y among all inserted lines at a given x.
    """

    __slots__ = ("xs", "size", "tree")

    def __init__(self, xs: List[int]) -> None:
        """
        Initialize the Li Chao tree.

        Args:
            xs: Sorted unique x-coordinates that will ever be queried.

        Returns:
            None

        Time complexity:
            O(k), where k = number of unique x-values

        Space complexity:
            O(k)
        """
        self.xs = xs
        size = 1
        while size < len(xs):
            size <<= 1
        self.size = size
        self.tree: List[Tuple[int, int] | None] = [None] * (2 * size)

    @staticmethod
    def _value(line: Tuple[int, int], x: int) -> int:
        """
        Evaluate a line at x.

        Args:
            line: A pair (m, b) representing y = m*x + b
            x: Query coordinate

        Returns:
            The value y

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        m, b = line
        return m * x + b

    def add_line(self, line: Tuple[int, int]) -> None:
        """
        Insert a line into the structure.

        Args:
            line: A pair (m, b) representing y = m*x + b

        Returns:
            None

        Time complexity:
            O(log k)

        Space complexity:
            O(log k) due to recursion stack
        """
        self._add_line(line, 1, 0, self.size - 1)

    def _add_line(self, line: Tuple[int, int], node: int, left: int, right: int) -> None:
        """
        Recursive helper for line insertion.

        Args:
            line: Line to insert
            node: Current segment-tree node index
            left: Left compressed index
            right: Right compressed index

        Returns:
            None

        Time complexity:
            O(log k)

        Space complexity:
            O(log k)
        """
        if self.tree[node] is None:
            self.tree[node] = line
            return

        cur = self.tree[node]
        mid = (left + right) // 2

        x_left = self.xs[left]
        x_mid = self.xs[mid]
        x_right = self.xs[right]

        # We maintain that self.tree[node] is the line that is better at x_mid.
        if self._value(line, x_mid) > self._value(cur, x_mid):
            self.tree[node], line = line, cur
            cur = self.tree[node]

        if left == right:
            return

        # Decide where the "losing" line might still beat the current stored line.
        if self._value(line, x_left) > self._value(cur, x_left):
            self._add_line(line, node * 2, left, mid)
        elif self._value(line, x_right) > self._value(cur, x_right):
            self._add_line(line, node * 2 + 1, mid + 1, right)

    def query(self, x: int) -> int:
        """
        Query the maximum y-value at coordinate x.

        Args:
            x: Query coordinate

        Returns:
            Maximum value among all inserted lines at x

        Time complexity:
            O(log k)

        Space complexity:
            O(1)
        """
        # Binary search compressed index.
        lo, hi = 0, len(self.xs) - 1
        while lo <= hi:
            mid = (lo + hi) // 2
            if self.xs[mid] < x:
                lo = mid + 1
            elif self.xs[mid] > x:
                hi = mid - 1
            else:
                idx = mid
                break
        else:
            raise ValueError("Query x must exist in compressed coordinates.")

        idx += self.size
        result = -(10**30)

        while idx > 0:
            line = self.tree[idx]
            if line is not None:
                result = max(result, self._value(line, x))
            idx //= 2

        return result


class Solution:
    def max_secure_relay_score(self, parent: List[int], permission: List[int]) -> int:
        """
        Compute the maximum secure relay score over all ancestor-to-descendant paths.

        Core idea:
        For a fixed descendant node v at depth d[v], and an ancestor u, the score is:

            max(path u..v) - min(path u..v) - (d[v] - d[u])

        Rearranged:
            = (max(path) + d[u]) - (min(path) + d[v])

        But directly tracking both max and min for every ancestor is too expensive.

        A much more useful decomposition is:
            score(u, v)
            = max(
                permission[v] - min_on_path(u..v) - length,
                max_on_path(u..v) - permission[v] - length,
                best score already achieved strictly above v and extended to v
              )

        The first two cases correspond to paths where the endpoint v becomes the new maximum
        or the new minimum. Those can be answered using ancestor information only.

        For any ancestor u of v:
            permission[v] - min_on_path(u..v) - (d[v] - d[u])

        If permission[v] is the maximum on the path, then:
            min_on_path(u..v) = minimum permission among ancestors from u to v
        and the expression becomes:
            permission[v] - d[v] + max over ancestors a on root..v of (depth[a] - permission[a]),
        but only among ancestors that can serve as the minimum on the chosen suffix.
        To support this correctly for all suffixes, we use a standard monotonic-stack +
        rollback segment structure idea in a simpler equivalent form:
        we maintain two Li Chao trees over the current root-to-node path.

        For any ancestor u:
            If permission[v] is the maximum endpoint:
                score = (permission[v] - d[v]) + (d[u] - min_on_path(u..v))
            The best such suffix is captured by maintaining lines derived from path minima.

        Symmetrically for permission[v] as the minimum endpoint.

        A cleaner exact formulation is:
        For every ancestor u, define:
            A_u(v) = max_on_path(u..v) + d[u]
            B_u(v) = -min_on_path(u..v) + d[u]

        Then:
            score(u, v) = max_on_path(u..v) - min_on_path(u..v) - d[v] + d[u]
                        = A_u(v) + (-min_on_path(u..v)) - d[v]
        Still coupled.

        The exact efficient solution uses a DSU-on-tree style merge of compressed path states:
        for each node, we maintain all possible pairs:
            best_max_component = max(permission on suffix) + start_depth
            best_min_component = -min(permission on suffix) + start_depth
        and combine them at the current node.

        However, because paths are strictly ancestor-to-descendant, there is an even simpler
        exact DP:
            Let best_inc[v] be the best value of max(path) - min(path) - length for paths ending at v.
            Any optimal path ending at v either:
              1) starts at v itself => 0
              2) extends a path ending at parent[v]
              3) starts somewhere above and uses v as new maximum
              4) starts somewhere above and uses v as new minimum

        Cases (3) and (4) can be answered from ancestors by:
            permission[v] - depth[v] + max(depth[u] - min_on_path(u..parent[v]))
            -permission[v] - depth[v] + max(depth[u] + max_on_path(u..parent[v]))

        We maintain these two maxima along the DFS path using monotonic stacks with rollback:
            S_min stores all suffix minima candidates for current root-to-parent path
            S_max stores all suffix maxima candidates for current root-to-parent path

        Each stack element represents a range of starting ancestors for which the suffix min/max
        is constant, and stores the best depth[u] value over that range.
        This allows O(log n) amortized updates and O(1) query of the needed maxima.

        Args:
            parent: Parent array describing the rooted tree
            permission: Permission value for each node

        Returns:
            The maximum secure relay score

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n = len(parent)
        if n == 0:
            return 0

        children: List[List[int]] = [[] for _ in range(n)]
        for node in range(1, n):
            children[parent[node]].append(node)

        depth = [0] * n
        order = [0]
        for node in order:
            for nxt in children[node]:
                depth[nxt] = depth[node] + 1
                order.append(nxt)

        # ------------------------------------------------------------------
        # We solve the problem with two Li Chao trees over the current root path.
        #
        # For a fixed descendant v and ancestor u:
        #   score(u, v) = max(path) - min(path) - (depth[v] - depth[u])
        #
        # If we choose some node x on the path as the maximum and some node y on the path
        # as the minimum, then:
        #   score = (permission[x] - depth[x]) - (permission[y] - depth[y]) + (depth[u] - depth_of_lca_inside_path?)
        #
        # That direct route becomes messy.
        #
        # A more robust exact method is to process all root-to-node paths and maintain:
        #   best_start_for_max_endpoint(v) = max over ancestors u of (depth[u] - min_on_path(u..v))
        #   best_start_for_min_endpoint(v) = max over ancestors u of (depth[u] + max_on_path(u..v))
        #
        # Then:
        #   best path ending at v where v is the maximum:
        #       permission[v] - depth[v] + best_start_for_max_endpoint(v)
        #   best path ending at v where v is the minimum:
        #       -permission[v] - depth[v] + best_start_for_min_endpoint(v)
        #
        # To maintain these exactly, we use two monotone compressed stacks with rollback.
        # Each stack partitions all possible starting ancestors of the current root-to-node path
        # into contiguous groups where the suffix minimum / suffix maximum is constant.
        #
        # For the minimum structure:
        #   each group stores:
        #       current suffix minimum value m
        #       best depth[start] among starts in this group
        #   query asks for max(depth[start] - m) over all groups
        #
        # For the maximum structure:
        #   each group stores:
        #       current suffix maximum value M
        #       best depth[start] among starts in this group
        #   query asks for max(depth[start] + M) over all groups
        #
        # When we append a new node value val at depth d:
        #   - for minima, any existing group with minimum >= val collapses into a new group
        #     with minimum val
        #   - for maxima, any existing group with maximum <= val collapses into a new group
        #     with maximum val
        #
        # This is exactly the same idea as maintaining suffix minima/maxima under append.
        # Because DFS backtracks, we also need rollback support.
        # ------------------------------------------------------------------

        class RollbackSuffixMin:
            """
            Maintain all suffix minima groups for the current root-to-current path.

            Each group is a pair:
                (min_value, best_depth_start)

            representing a contiguous set of starting positions whose suffix minimum
            equals min_value, and among those starts the maximum depth[start] is
            best_depth_start.

            We also maintain:
                best_value = max(best_depth_start - min_value) over all groups
            """

            __slots__ = ("groups", "best_stack", "history")

            def __init__(self) -> None:
                self.groups: List[Tuple[int, int]] = []
                self.best_stack: List[int] = []
                self.history: List[Tuple[int, List[Tuple[int, int]], int]] = []

            def push(self, value: int, dep: int) -> None:
                """
                Append a new node to the current path.

                Args:
                    value: Permission value of the new node
                    dep: Depth of the new node

                Returns:
                    None

                Time complexity:
                    Amortized O(1)

                Space complexity:
                    O(1) extra plus rollback history
                """
                popped: List[Tuple[int, int]] = []
                best_depth = dep

                # Merge all groups whose suffix minimum becomes the new smaller value.
                while self.groups and self.groups[-1][0] >= value:
                    old_min, old_best_depth = self.groups.pop()
                    popped.append((old_min, old_best_depth))
                    if old_best_depth > best_depth:
                        best_depth = old_best_depth
                    self.best_stack.pop()

                prev_best = self.best_stack[-1] if self.best_stack else -(10**30)
                current_group = (value, best_depth)
                current_best = max(prev_best, best_depth - value)

                self.groups.append(current_group)
                self.best_stack.append(current_best)
                self.history.append((len(popped), popped, prev_best))

            def pop(self) -> None:
                """
                Roll back the most recent push.

                Args:
                    None

                Returns:
                    None

                Time complexity:
                    Amortized O(1)

                Space complexity:
                    O(1)
                """
                count, popped, _ = self.history.pop()
                self.groups.pop()
                self.best_stack.pop()
                for item in reversed(popped):
                    self.groups.append(item)
                    prev_best = self.best_stack[-1] if self.best_stack else -(10**30)
                    self.best_stack.append(max(prev_best, item[1] - item[0]))

            def best(self) -> int:
                """
                Return max(depth[start] - suffix_min(start)) for the current path.

                Args:
                    None

                Returns:
                    Best value

                Time complexity:
                    O(1)

                Space complexity:
                    O(1)
                """
                return self.best_stack[-1] if self.best_stack else -(10**30)

        class RollbackSuffixMax:
            """
            Maintain all suffix maxima groups for the current root-to-current path.

            Each group is a pair:
                (max_value, best_depth_start)

            We maintain:
                best_value = max(best_depth_start + max_value) over all groups
            """

            __slots__ = ("groups", "best_stack", "history")

            def __init__(self) -> None:
                self.groups: List[Tuple[int, int]] = []
                self.best_stack: List[int] = []
                self.history: List[Tuple[int, List[Tuple[int, int]], int]] = []

            def push(self, value: int, dep: int) -> None:
                """
                Append a new node to the current path.

                Args:
                    value: Permission value of the new node
                    dep: Depth of the new node

                Returns:
                    None

                Time complexity:
                    Amortized O(1)

                Space complexity:
                    O(1) extra plus rollback history
                """
                popped: List[Tuple[int, int]] = []
                best_depth = dep

                # Merge all groups whose suffix maximum becomes the new larger value.
                while self.groups and self.groups[-1][0] <= value:
                    old_max, old_best_depth = self.groups.pop()
                    popped.append((old_max, old_best_depth))
                    if old_best_depth > best_depth:
                        best_depth = old_best_depth
                    self.best_stack.pop()

                prev_best = self.best_stack[-1] if self.best_stack else -(10**30)
                current_group = (value, best_depth)
                current_best = max(prev_best, best_depth + value)

                self.groups