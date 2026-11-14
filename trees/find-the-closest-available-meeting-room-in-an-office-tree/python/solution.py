"""
Title: Find the Closest Available Meeting Room in an Office Tree

Problem Description:
You are given the layout of an office campus as a rooted tree with nodes numbered
from 0 to n - 1. Each node represents either a hallway junction or a room, and
edges represent direct walking connections. Node 0 is the main entrance.
Every node has a boolean value `available[i]` indicating whether that location is
currently an available meeting room.

A query asks: starting from employee position `u`, which available meeting room
can be reached with the fewest number of edges?

If multiple available meeting rooms are tied for the same minimum distance,
return the one with the smallest node index. If no available meeting room exists
anywhere in the tree, return -1 for that query.

You must answer many independent queries on the same tree efficiently.

The intended efficient approach is to preprocess the tree so that for every node
we already know the nearest available room. Then each query can be answered in
O(1) time by direct lookup.

This solution uses:
1. A first tree DP pass from children to parent ("downward DP")
2. A second rerooting-style pass from parent to children ("upward DP")
3. A final merge of both directions to get the best answer for every node

The key idea:
- For each node, compute the best available room inside its own subtree.
- Then compute the best available room coming from outside its subtree.
- Combine both to get the globally nearest available room for that node.
"""

from typing import List, Tuple


class Solution:
    def _better(self, a: Tuple[int, int], b: Tuple[int, int]) -> Tuple[int, int]:
        """
        Compare two candidate answers and return the better one.

        A candidate is represented as:
        (distance, node_index)

        Smaller distance is better.
        If distances tie, smaller node index is better.

        Args:
            a: First candidate pair (distance, node_index).
            b: Second candidate pair (distance, node_index).

        Returns:
            The better candidate according to the problem rules.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        if a[0] != b[0]:
            return a if a[0] < b[0] else b
        return a if a[1] < b[1] else b

    def closest_available_rooms(
        self,
        n: int,
        edges: List[List[int]],
        available: List[bool],
        queries: List[int],
    ) -> List[int]:
        """
        Precompute the nearest available meeting room for every node in the tree,
        then answer each query in O(1).

        Args:
            n: Number of nodes in the tree.
            edges: Undirected edges of the tree.
            available: Boolean list where available[i] is True if node i is an available room.
            queries: Starting nodes for which we must find the nearest available room.

        Returns:
            A list of answers, where each answer is the nearest available room for the
            corresponding query node, or -1 if no available room exists.

        Time complexity:
            O(n + len(queries))

        Space complexity:
            O(n)
        """
        # A very large distance used as "infinity".
        # Any real tree distance will be much smaller than this.
        inf = 10**18

        # ------------------------------------------------------------
        # Step 1: Build the adjacency list for the tree.
        # ------------------------------------------------------------
        # Because the input is an undirected tree, each edge [u, v]
        # is stored in both directions.
        graph: List[List[int]] = [[] for _ in range(n)]
        for u, v in edges:
            graph[u].append(v)
            graph[v].append(u)

        # ------------------------------------------------------------
        # Step 2: Build parent/depth/order arrays using iterative DFS.
        # ------------------------------------------------------------
        # We avoid recursive DFS because n can be as large as 2 * 10^5,
        # and Python recursion depth would be unsafe.
        #
        # parent[x] = parent of node x in the rooted tree (rooted at 0)
        # order = traversal order; reversing it gives postorder-like processing
        parent: List[int] = [-1] * n
        order: List[int] = []
        stack: List[int] = [0]
        parent[0] = 0

        while stack:
            node = stack.pop()
            order.append(node)
            for nei in graph[node]:
                if parent[nei] == -1:
                    parent[nei] = node
                    stack.append(nei)

        # ------------------------------------------------------------
        # Step 3: Downward DP.
        # ------------------------------------------------------------
        # down[node] will store the best available room reachable by moving
        # only inside the subtree rooted at 'node'.
        #
        # Representation:
        # down[node] = (best_distance, best_room_index)
        #
        # Examples:
        # - If node itself is available, one candidate is (0, node)
        # - If a child has a subtree answer (d, room), then from node that
        #   candidate becomes (d + 1, room)
        #
        # We process nodes in reversed DFS order so that children are processed
        # before their parent.
        down: List[Tuple[int, int]] = [(inf, inf) for _ in range(n)]

        for node in reversed(order):
            # If the current node itself is an available room,
            # then distance 0 to itself is a valid candidate.
            best = (0, node) if available[node] else (inf, inf)

            # Check every child contribution.
            # Since the tree is rooted at 0, a neighbor is a child if its parent is 'node'.
            for nei in graph[node]:
                if parent[nei] == node:
                    child_dist, child_room = down[nei]
                    if child_dist != inf:
                        # From current node to that child's best room,
                        # we need one extra edge.
                        candidate = (child_dist + 1, child_room)
                        best = self._better(best, candidate)

            down[node] = best

        # ------------------------------------------------------------
        # Step 4: Upward DP using rerooting.
        # ------------------------------------------------------------
        # up[node] will store the best available room reachable from 'node'
        # that does NOT rely on going down into node's own subtree.
        #
        # In other words, it captures answers coming from:
        # - the parent side
        # - siblings' subtrees
        # - the parent itself if available
        #
        # For the root, there is no outside-of-subtree area, so up[root] is invalid.
        up: List[Tuple[int, int]] = [(inf, inf) for _ in range(n)]
        up[0] = (inf, inf)

        # Process in normal root-to-leaf order so that parent's up value
        # is known before computing children's up values.
        for node in order:
            # --------------------------------------------------------
            # For rerooting efficiently, each child needs to know:
            # "What is the best candidate available from the parent side
            #  excluding that child's own subtree?"
            #
            # The possible sources for a child are:
            # 1. The current node itself, if available -> distance 1 from child
            # 2. The current node's up answer -> distance +1 from child
            # 3. Any sibling subtree's down answer -> distance +2 from child
            #
            # To compute sibling contributions efficiently for all children,
            # we build prefix/suffix best arrays over the children.
            # --------------------------------------------------------
            children: List[int] = []
            child_contrib: List[Tuple[int, int]] = []

            for nei in graph[node]:
                if parent[nei] == node:
                    children.append(nei)
                    # Contribution from this child subtree as seen from 'node':
                    # if child's best room is at distance d from child,
                    # then from node it is d + 1.
                    d, room = down[nei]
                    if d == inf:
                        child_contrib.append((inf, inf))
                    else:
                        child_contrib.append((d + 1, room))

            m = len(children)

            # prefix[i] = best among child_contrib[0..i-1]
            prefix: List[Tuple[int, int]] = [(inf, inf)] * (m + 1)
            for i in range(m):
                prefix[i + 1] = self._better(prefix[i], child_contrib[i])

            # suffix[i] = best among child_contrib[i..m-1]
            suffix: List[Tuple[int, int]] = [(inf, inf)] * (m + 1)
            for i in range(m - 1, -1, -1):
                suffix[i] = self._better(child_contrib[i], suffix[i + 1])

            # Base candidate available from the current node itself or above it,
            # as seen from the current node.
            #
            # This is what a child can use after adding one edge.
            base_at_node = (0, node) if available[node] else (inf, inf)
            base_at_node = self._better(base_at_node, up[node])

            # Now compute up value for each child.
            for i, child in enumerate(children):
                # Best sibling contribution as seen from current node,
                # excluding this child's own subtree.
                siblings_best = self._better(prefix[i], suffix[i + 1])

                # Best source available at current node excluding child's subtree:
                # - current node itself
                # - anything from above current node
                # - any sibling subtree
                best_excluding_child = self._better(base_at_node, siblings_best)

                if best_excluding_child[0] == inf:
                    up[child] = (inf, inf)
                else:
                    # Child is one edge farther away from any source seen at current node.
                    up[child] = (best_excluding_child[0] + 1, best_excluding_child[1])

        # ------------------------------------------------------------
        # Step 5: Final answer for every node.
        # ------------------------------------------------------------
        # The nearest available room for a node can come from:
        # - inside its own subtree (down[node])
        # - outside its subtree (up[node])
        #
        # We take the better of the two.
        answer_for_node: List[int] = [-1] * n
        for node in range(n):
            best = self._better(down[node], up[node])
            answer_for_node[node] = -1 if best[0] == inf else best[1]

        # ------------------------------------------------------------
        # Step 6: Answer queries by direct lookup.
        # ------------------------------------------------------------
        return [answer_for_node[q] for q in queries]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 7
    edges1 = [[0, 1], [0, 2], [1, 3], [1, 4], [2, 5], [2, 6]]
    available1 = [False, False, True, False, True, False, False]
    queries1 = [3, 5, 0]
    result1 = solution.closest_available_rooms(n1, edges1, available1, queries1)
    print(result1)  # Expected: [4, 2, 2]

    # Example 2
    n2 = 5
    edges2 = [[0, 1], [1, 2], [1, 3], [3, 4]]
    available2 = [False, False, False, False, False]
    queries2 = [0, 2, 4]
    result2 = solution.closest_available_rooms(n2, edges2, available2, queries2)
    print(result2)  # Expected: [-1, -1, -1]