"""
Title: Maximum Emergency Detour Sum in a Hazard Tree

Problem Description:
You are given an undirected tree with n facilities numbered from 0 to n - 1.
Each facility i has an integer risk value risk[i], which may be positive, zero,
or negative.

A response team starts at facility 0, the command center. During an emergency,
the team may choose exactly one simple detour route that begins at any node on
the path from the root to some node u, then continues downward to some descendant
v without revisiting a node.

Equivalently, after rooting the tree at node 0, we need the maximum-sum contiguous
downward path (ancestor -> descendant).

The score of a detour is the sum of risk values on all nodes in that route.
A route is valid if:
- no risk value appears more than twice on the route
- at most one distinct risk value is allowed to appear exactly twice
- all other values must appear once

Return the maximum possible score among all valid downward paths.

Constraints:
- 1 <= n <= 200000
- edges.length == n - 1
- edges forms a valid tree
- -10^9 <= risk[i] <= 10^9
"""

from typing import Dict, List, Tuple


class SegmentTreeMinPrefix:
    """
    Segment tree that stores the minimum prefix sum value over a range of indices.

    We use it over the current root-to-node path indices:
    - prefix[k] = sum of risks on path positions [0 .. k-1]
    - for a fixed end position r, the sum of subpath [l .. r] is:
          prefix[r + 1] - prefix[l]
    So to maximize the subpath sum ending at r under valid starting positions l,
    we want the minimum prefix[l] among all allowed l.

    The tree supports:
    - point update: set prefix value at one index
    - range minimum query on [left, right]
    """

    def __init__(self, size: int) -> None:
        """
        Initialize the segment tree.

        Args:
            size: Number of positions to support.

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

    def update(self, index: int, value: int) -> None:
        """
        Set one position to a new value.

        Args:
            index: Position to update.
            value: New value.

        Returns:
            None

        Time complexity:
            O(log size)

        Space complexity:
            O(1) extra
        """
        pos: int = index + self.n
        self.data[pos] = value
        pos >>= 1
        while pos:
            self.data[pos] = min(self.data[pos << 1], self.data[(pos << 1) | 1])
            pos >>= 1

    def query(self, left: int, right: int) -> int:
        """
        Query the minimum value in the inclusive range [left, right].

        Args:
            left: Left boundary.
            right: Right boundary.

        Returns:
            Minimum value in the range.

        Time complexity:
            O(log size)

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


class Solution:
    def max_emergency_detour_sum(
        self, n: int, edges: List[List[int]], risk: List[int]
    ) -> int:
        """
        Compute the maximum sum of any valid downward path in the tree.

        Core idea:
        We root the tree at 0 and perform DFS while maintaining the current
        root-to-current-node path.

        For every current node at path index r, every valid downward path ending
        at this node corresponds to choosing a start index l on the current path.
        The path values are path[l..r].

        Validity rule on path[l..r]:
        - no value frequency > 2
        - at most one value has frequency exactly 2

        This can be maintained with a sliding-window-like left boundary over the
        current DFS path:
        - Let left_bound be the smallest l such that path[l..r] is valid.
        Then every l >= left_bound is also valid, because removing nodes from the
        front cannot increase any frequency.

        So for each end r, the best valid path ending at r is:
            prefix[r + 1] - min(prefix[l]) for l in [left_bound .. r]

        We answer that minimum-prefix query using a segment tree over the current
        path indices.

        Because DFS branches, all state changes are rolled back when we return
        from recursion.

        Args:
            n: Number of nodes.
            edges: Undirected tree edges.
            risk: Risk value for each node.

        Returns:
            Maximum possible valid downward path sum.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        # -----------------------------
        # Build adjacency list for the tree.
        # -----------------------------
        graph: List[List[int]] = [[] for _ in range(n)]
        for u, v in edges:
            graph[u].append(v)
            graph[v].append(u)

        # -----------------------------
        # We will maintain the current root-to-node path explicitly.
        #
        # path_values[i] = risk value of the node at depth/index i on the current path
        # prefix[i]      = sum of first i values on the current path
        #                  so prefix[0] = 0
        #                  and prefix[k+1] = prefix[k] + path_values[k]
        #
        # If current node is at path index r, then sum(path[l..r]) = prefix[r+1] - prefix[l]
        # -----------------------------
        path_values: List[int] = []
        prefix: List[int] = [0]

        # -----------------------------
        # Frequency map for values inside the current valid window [left_bound .. current_index].
        #
        # Important:
        # We do NOT store frequencies for the entire root-to-current path.
        # We store frequencies only for the current sliding window that represents
        # the smallest valid suffix ending at the current node.
        #
        # This is exactly what allows us to update left_bound efficiently.
        # -----------------------------
        freq: Dict[int, int] = {}

        # Number of distinct values that currently appear exactly twice in the window.
        doubles_count: int = 0

        # left_bound = smallest valid start index for the current end index.
        left_bound: int = 0

        # Best answer found so far.
        answer: int = -10**30

        # Segment tree over prefix indices [0 .. n].
        # At DFS depth d (current path length = d), only prefix indices [0 .. d] are active.
        seg_tree: SegmentTreeMinPrefix = SegmentTreeMinPrefix(n + 1)

        # Initially, before visiting any node, prefix[0] = 0 is active.
        seg_tree.update(0, 0)

        def add_value(value: int) -> None:
            """
            Add one value to the current window frequency structure.

            Args:
                value: Risk value being added.

            Returns:
                None

            Time complexity:
                O(1)

            Space complexity:
                O(1) extra
            """
            nonlocal doubles_count

            old_count: int = freq.get(value, 0)
            new_count: int = old_count + 1
            freq[value] = new_count

            # If a value goes from frequency 1 -> 2, it becomes one of the duplicated values.
            if old_count == 1:
                doubles_count += 1

        def remove_value(value: int) -> None:
            """
            Remove one value from the current window frequency structure.

            Args:
                value: Risk value being removed.

            Returns:
                None

            Time complexity:
                O(1)

            Space complexity:
                O(1) extra
            """
            nonlocal doubles_count

            old_count: int = freq[value]
            new_count: int = old_count - 1

            # If a value goes from frequency 2 -> 1, it is no longer duplicated.
            if old_count == 2:
                doubles_count -= 1

            if new_count == 0:
                del freq[value]
            else:
                freq[value] = new_count

        def window_is_invalid() -> bool:
            """
            Check whether the current window violates the path validity rules.

            Args:
                None

            Returns:
                True if invalid, False otherwise.

            Time complexity:
                O(1)

            Space complexity:
                O(1)
            """
            # Invalid if:
            # - more than one value appears exactly twice
            # - or some value appears 3 or more times
            #
            # We do not track "count >= 3" separately, but we can detect it when
            # adding/removing because the current end changes by one node at a time.
            #
            # To keep the implementation simple and still O(n), we will only ever
            # need to inspect the newly added value for count > 2.
            #
            # Therefore this helper is not used directly for count > 2 checks.
            return doubles_count > 1

        def dfs(node: int, parent: int) -> None:
            """
            Depth-first traversal with rollback of sliding-window state.

            Args:
                node: Current node.
                parent: Parent node in rooted DFS.

            Returns:
                None

            Time complexity:
                O(subtree_size * log n) amortized over the whole tree

            Space complexity:
                O(height) recursion stack plus shared O(n) structures
            """
            nonlocal left_bound, answer, doubles_count

            # -----------------------------------------
            # Save all mutable state that must be restored after exploring this node.
            #
            # Because DFS branches, each child subtree must see the exact same state
            # that existed before entering that child.
            # -----------------------------------------
            saved_left_bound: int = left_bound
            saved_prefix_len: int = len(prefix)
            saved_path_len: int = len(path_values)

            # We also need to remember which values are removed from the left side
            # while shrinking the window, so we can add them back during rollback.
            removed_from_left: List[int] = []

            # -----------------------------------------
            # Extend the current root-to-node path with this node.
            # -----------------------------------------
            current_value: int = risk[node]
            path_values.append(current_value)
            prefix.append(prefix[-1] + current_value)

            # Activate prefix index corresponding to the new path length.
            # If current node is at index r, then prefix index r+1 becomes active.
            current_path_index: int = len(path_values) - 1
            seg_tree.update(current_path_index + 1, prefix[-1])

            # -----------------------------------------
            # Add this node's value to the sliding window ending at current_path_index.
            # -----------------------------------------
            add_value(current_value)

            # -----------------------------------------
            # Shrink left_bound until the window becomes valid.
            #
            # The window is path_values[left_bound .. current_path_index].
            #
            # It is invalid if:
            # 1) some value appears 3 times
            # 2) more than one distinct value appears twice
            #
            # Since only current_value changed when we extended the path by one node,
            # the only possible "count > 2" offender is current_value itself.
            # -----------------------------------------
            while freq[current_value] > 2 or window_is_invalid():
                removed_value: int = path_values[left_bound]
                remove_value(removed_value)
                removed_from_left.append(removed_value)
                left_bound += 1

            # -----------------------------------------
            # Now [left_bound .. current_path_index] is the smallest valid suffix
            # ending at current_path_index.
            #
            # Any start index l >= left_bound is also valid.
            #
            # Best valid path ending here:
            #   prefix[current_path_index + 1] - min(prefix[l]) for l in [left_bound .. current_path_index]
            #
            # Note:
            # - l is a node index on the path
            # - prefix index is the same l
            # - end uses prefix[current_path_index + 1]
            # -----------------------------------------
            min_prefix_in_valid_range: int = seg_tree.query(left_bound, current_path_index)
            best_ending_here: int = prefix[current_path_index + 1] - min_prefix_in_valid_range
            answer = max(answer, best_ending_here)

            # -----------------------------------------
            # Continue DFS into children.
            # -----------------------------------------
            for neighbor in graph[node]:
                if neighbor == parent:
                    continue
                dfs(neighbor, node)

            # -----------------------------------------
            # Rollback begins here.
            #
            # We must restore:
            # - segment tree active prefix position
            # - path_values and prefix
            # - sliding window frequencies
            # - left_bound
            #
            # Current window before rollback includes this node and excludes any
            # nodes removed from the left during shrinking.
            # -----------------------------------------

            # Remove the current node's value from the window.
            remove_value(current_value)

            # Add back the values that were removed from the left, in reverse order.
            # This restores the exact previous window state.
            for value in reversed(removed_from_left):
                add_value(value)

            # Restore left boundary.
            left_bound = saved_left_bound

            # Deactivate the prefix index for this node's path length.
            seg_tree.update(current_path_index + 1, seg_tree.inf)

            # Restore path containers.
            while len(prefix) > saved_prefix_len:
                prefix.pop()
            while len(path_values) > saved_path_len:
                path_values.pop()

        dfs(0, -1)
        return answer


if __name__ == "__main__":
    solution = Solution()

    n1 = 7
    edges1 = [[0, 1], [0, 2], [1, 3], [1, 4], [2, 5], [2, 6]]
    risk1 = [5, 2, 5, 1, 2, 3, 4]
    result1 = solution.max_emergency_detour_sum(n1, edges1, risk1)
    print(result1)  # Expected: 14

    n2 = 6
    edges2 = [[0, 1], [1, 2], [2, 3], [3, 4], [4, 5]]
    risk2 = [4, -2, 4, 7, 7, 1]
    result2 = solution.max_emergency_detour_sum(n2, edges2, risk2)
    print(result2)  # Expected: 19