"""
Title: Find the Nearest Shared Backup Folder

Problem Description:
A company stores project backups in a rooted tree of folders. Each folder has a unique
integer ID from 0 to n - 1, and folder 0 is always the root. You are given the tree as
a parent array where parent[i] is the parent of folder i, and parent[0] = -1.

For several restore operations, two backup snapshots are selected from folders a and b.
The restore system needs to locate the nearest folder that is an ancestor of both a and b.
This folder is the deepest folder that lies on both paths from a and b to the root.

Your task is to answer all queries and return the shared backup folder for each pair.

Implement a function that, given the parent array and a list of queries, returns an array
where the i-th answer is the nearest shared backup folder for queries[i] = [a, b].

A folder is considered an ancestor of itself.

Constraints:
- 1 <= n <= 2 * 10^5
- parent.length == n
- parent[0] == -1
- For every i > 0, 0 <= parent[i] < n
- The input parent array always forms a valid tree rooted at 0
- 1 <= queries.length <= 2 * 10^5
- queries[i].length == 2
- 0 <= a, b < n

This is a classic Lowest Common Ancestor (LCA) problem on a rooted tree.
To answer many queries efficiently, we preprocess binary lifting tables.
"""

from collections import deque
from typing import List


class Solution:
    def _build_depth_and_jump_table(self, parent: List[int]) -> tuple[List[int], List[List[int]]]:
        """
        Build node depths and the binary lifting jump table.

        The jump table stores:
        up[k][v] = the 2^k-th ancestor of node v

        This allows us to:
        1. Move a node upward by any number of levels in O(log n)
        2. Compute the lowest common ancestor of two nodes in O(log n)

        Args:
            parent: Parent array of the rooted tree, where parent[0] == -1.

        Returns:
            A tuple containing:
            - depth: depth[v] is the distance from root to node v
            - up: binary lifting table

        Time complexity:
            O(n log n)

        Space complexity:
            O(n log n)
        """
        n: int = len(parent)

        # We need enough levels so that 2^LOG is at least n.
        # Example:
        # if n = 7, then LOG = 3 is enough because:
        # 2^0 = 1
        # 2^1 = 2
        # 2^2 = 4
        # 2^3 = 8 >= 7
        #
        # Using bit_length is a clean Python way to compute this.
        log: int = n.bit_length()

        # Build adjacency list from the parent array.
        # children[u] will contain all direct children of node u.
        #
        # We do this because the input gives parent pointers, but to compute
        # depths for all nodes efficiently, it is easier to traverse from the root
        # down to the leaves.
        children: List[List[int]] = [[] for _ in range(n)]
        for node in range(1, n):
            children[parent[node]].append(node)

        # depth[v] = number of edges from root to v
        depth: List[int] = [0] * n

        # up[k][v] = 2^k-th ancestor of v
        #
        # We initialize all entries to 0. For the root, we will define all ancestors
        # as 0 itself. This is a common and safe trick in LCA implementations because:
        # - root has no real parent
        # - keeping root's ancestors as root avoids invalid indices
        up: List[List[int]] = [[0] * n for _ in range(log)]

        # Base case for binary lifting:
        # up[0][v] = direct parent of v
        #
        # For root (node 0), we store 0 as its own ancestor.
        up[0][0] = 0
        for node in range(1, n):
            up[0][node] = parent[node]

        # Compute depths using BFS from the root.
        #
        # BFS is iterative and avoids recursion depth issues, which is important
        # because n can be as large as 200,000.
        queue: deque[int] = deque([0])
        while queue:
            current: int = queue.popleft()
            for child in children[current]:
                depth[child] = depth[current] + 1
                queue.append(child)

        # Fill the rest of the jump table.
        #
        # Transition:
        # up[k][v] = up[k - 1][ up[k - 1][v] ]
        #
        # Meaning:
        # - first jump 2^(k-1) steps up from v
        # - then jump another 2^(k-1) steps up
        # - total = 2^k steps up
        for k in range(1, log):
            previous_row: List[int] = up[k - 1]
            current_row: List[int] = up[k]
            for node in range(n):
                current_row[node] = previous_row[previous_row[node]]

        return depth, up

    def _lift_node(self, node: int, distance: int, up: List[List[int]]) -> int:
        """
        Move a node upward by a given number of levels using binary lifting.

        Args:
            node: The starting node.
            distance: Number of levels to move upward.
            up: Binary lifting table.

        Returns:
            The ancestor reached after moving up 'distance' levels.

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra space
        """
        # We inspect each bit of 'distance'.
        # If bit k is set, we jump upward by 2^k using up[k][node].
        bit_index: int = 0
        while distance > 0:
            if distance & 1:
                node = up[bit_index][node]
            distance >>= 1
            bit_index += 1
        return node

    def _lca(self, a: int, b: int, depth: List[int], up: List[List[int]]) -> int:
        """
        Compute the lowest common ancestor (nearest shared ancestor) of two nodes.

        Args:
            a: First node.
            b: Second node.
            depth: Depth array.
            up: Binary lifting table.

        Returns:
            The lowest common ancestor of a and b.

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra space
        """
        # Step 1:
        # Ensure 'a' is the deeper node (or equal depth).
        # This makes the next step simpler because we only need to lift 'a'.
        if depth[a] < depth[b]:
            a, b = b, a

        # Step 2:
        # Lift 'a' so both nodes are at the same depth.
        depth_difference: int = depth[a] - depth[b]
        a = self._lift_node(a, depth_difference, up)

        # If they are now equal, then one node was an ancestor of the other,
        # and that node is the LCA.
        if a == b:
            return a

        # Step 3:
        # Lift both nodes upward together, from the largest jump size down to 0.
        #
        # We only jump when their ancestors at that level are different.
        # Why?
        # Because if up[k][a] == up[k][b], then jumping both would overshoot or
        # move them to the same ancestor too early.
        #
        # After this loop, a and b will be direct children of the LCA.
        for k in range(len(up) - 1, -1, -1):
            if up[k][a] != up[k][b]:
                a = up[k][a]
                b = up[k][b]

        # Step 4:
        # Since a and b are now immediate children of the LCA,
        # their direct parent is the answer.
        return up[0][a]

    def nearest_shared_backup_folder(self, parent: List[int], queries: List[List[int]]) -> List[int]:
        """
        Answer all nearest shared ancestor queries on the rooted tree.

        This method preprocesses the tree once using binary lifting, then answers
        each query efficiently.

        Args:
            parent: Parent array representing the rooted tree.
            queries: List of [a, b] pairs asking for the nearest shared ancestor.

        Returns:
            A list where each element is the answer for the corresponding query.

        Time complexity:
            O(n log n + q log n)
            where n is the number of nodes and q is the number of queries

        Space complexity:
            O(n log n)
        """
        # Precompute:
        # - depth of each node
        # - binary lifting table for ancestors
        depth, up = self._build_depth_and_jump_table(parent)

        # Process each query independently using the LCA helper.
        answers: List[int] = []
        for a, b in queries:
            answers.append(self._lca(a, b, depth, up))

        return answers


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    parent1: List[int] = [-1, 0, 0, 1, 1, 2, 2]
    queries1: List[List[int]] = [[3, 4], [3, 5], [2, 6], [0, 6]]
    result1: List[int] = solution.nearest_shared_backup_folder(parent1, queries1)
    print("Example 1 Output:", result1)
    # Expected: [1, 0, 2, 0]

    # Example 2
    parent2: List[int] = [-1, 0, 1, 2, 3, 1, 5]
    queries2: List[List[int]] = [[4, 6], [2, 4], [6, 6], [3, 5]]
    result2: List[int] = solution.nearest_shared_backup_folder(parent2, queries2)
    print("Example 2 Output:", result2)
    # Expected: [1, 2, 6, 1]