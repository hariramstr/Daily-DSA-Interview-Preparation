"""
Title: Earliest Shared Dependency Between Services

Problem Description:
You are given a directed graph representing service dependencies in a microservice platform.
An edge [a, b] means service a directly depends on service b. If service X depends on Y,
and Y depends on Z, then X also indirectly depends on Z.

For two given services s1 and s2, find the shared dependency that can be reached from both
services with the smallest combined number of dependency hops. If there are multiple such
services with the same minimum total distance, return the one with the smallest service id.
If no shared dependency exists, return -1.

The graph may contain cycles due to misconfigured dependency metadata, so the solution must
handle cycles safely.

More formally, for every node x that is reachable from both s1 and s2, define:
    score(x) = dist(s1, x) + dist(s2, x)
where dist(u, v) is the minimum number of directed edges from u to v.

Return the id of the reachable node with the minimum score, breaking ties by smaller node id.

Constraints:
- 1 <= n <= 100000
- 0 <= edges.length <= 200000
- edges[i] = [from, to]
- 0 <= from, to < n
- 0 <= s1, s2 < n
- Multiple outgoing edges are allowed
- Self-loops and cycles may exist
"""

from collections import deque
from typing import Deque, List, Tuple


class Solution:
    def _build_graph(self, n: int, edges: List[List[int]]) -> List[List[int]]:
        """
        Build an adjacency list for the directed graph.

        Args:
            n: Number of nodes/services.
            edges: Directed edges where [a, b] means a -> b.

        Returns:
            Adjacency list where graph[u] contains all nodes directly reachable from u.

        Time complexity:
            O(n + m), where m is the number of edges.

        Space complexity:
            O(n + m)
        """
        graph: List[List[int]] = [[] for _ in range(n)]
        for src, dst in edges:
            graph[src].append(dst)
        return graph

    def _bfs_distances(self, graph: List[List[int]], start: int) -> List[int]:
        """
        Compute the shortest distance from a start node to every reachable node
        in an unweighted directed graph using BFS.

        Args:
            graph: Adjacency list of the directed graph.
            start: Starting node.

        Returns:
            A distance list where dist[v] is the minimum number of edges from
            start to v, or -1 if v is unreachable.

        Time complexity:
            O(n + m) in the worst case.

        Space complexity:
            O(n)
        """
        n: int = len(graph)

        # We use -1 to mean "not visited / unreachable".
        # This is a very common BFS pattern because:
        # 1. It lets us quickly test whether a node was already processed.
        # 2. It stores the final shortest distance in the same array.
        dist: List[int] = [-1] * n

        # BFS uses a queue because it explores nodes level by level.
        # In an unweighted graph, the first time we reach a node is guaranteed
        # to be through the shortest path.
        queue: Deque[int] = deque()

        # The start node is always reachable from itself with distance 0.
        dist[start] = 0
        queue.append(start)

        # Standard BFS loop.
        while queue:
            current: int = queue.popleft()

            # Explore every outgoing dependency edge from the current node.
            for neighbor in graph[current]:
                # If neighbor has not been visited yet, we have just found
                # the shortest path to it.
                if dist[neighbor] == -1:
                    dist[neighbor] = dist[current] + 1
                    queue.append(neighbor)

        return dist

    def earliest_shared_dependency(
        self, n: int, edges: List[List[int]], s1: int, s2: int
    ) -> int:
        """
        Find the shared reachable node with the minimum sum of shortest-path distances
        from s1 and s2. Break ties by smaller node id.

        Args:
            n: Number of services/nodes.
            edges: Directed edges [a, b] meaning a depends on b.
            s1: First service.
            s2: Second service.

        Returns:
            The id of the best shared dependency, or -1 if none exists.

        Time complexity:
            O(n + m), where m is the number of edges.
            More precisely:
            - Building graph: O(n + m)
            - BFS from s1: O(n + m)
            - BFS from s2: O(n + m)
            - Final scan: O(n)
            Total remains O(n + m).

        Space complexity:
            O(n + m)
        """
        # Step 1: Build the graph as an adjacency list.
        #
        # Why adjacency list?
        # - The graph is potentially large and sparse.
        # - Adjacency lists are memory-efficient for sparse graphs.
        # - BFS naturally works well with adjacency lists.
        graph: List[List[int]] = self._build_graph(n, edges)

        # Step 2: Compute shortest distances from s1 to all nodes.
        #
        # Because every edge has equal weight (1 hop), BFS gives shortest paths.
        # This also safely handles cycles:
        # - Once a node is visited, we do not revisit it.
        # - Therefore BFS terminates even if the graph contains cycles or self-loops.
        dist_from_s1: List[int] = self._bfs_distances(graph, s1)

        # Step 3: Compute shortest distances from s2 to all nodes.
        dist_from_s2: List[int] = self._bfs_distances(graph, s2)

        # Step 4: Scan every node and evaluate whether it is reachable from both starts.
        #
        # A node x is a valid shared dependency if:
        # - dist_from_s1[x] != -1
        # - dist_from_s2[x] != -1
        #
        # For each valid node, compute:
        #   score = dist_from_s1[x] + dist_from_s2[x]
        #
        # We keep the node with:
        # 1. smallest score
        # 2. if scores tie, smallest node id
        best_node: int = -1
        best_score: int = float("inf")

        for node in range(n):
            # Skip nodes that are not reachable from both services.
            if dist_from_s1[node] == -1 or dist_from_s2[node] == -1:
                continue

            score: int = dist_from_s1[node] + dist_from_s2[node]

            # Update answer if:
            # - this node has a smaller score, or
            # - same score but smaller node id
            if score < best_score or (score == best_score and node < best_node):
                best_score = score
                best_node = node

        return best_node


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1: int = 6
    edges1: List[List[int]] = [[0, 2], [1, 2], [2, 3], [1, 4], [4, 3], [0, 5]]
    s1_1: int = 0
    s2_1: int = 1
    result1: int = solution.earliest_shared_dependency(n1, edges1, s1_1, s2_1)
    print("Example 1 result:", result1)  # Expected: 2

    # Example 2
    # Important correctness check:
    # From 0: dist to 0=0, 1=1, 2=2
    # From 3: dist to 2=1, 0=2, 1=3, 4=1
    # Shared nodes: 0,1,2
    # Scores:
    #   0 -> 0 + 2 = 2
    #   1 -> 1 + 3 = 4
    #   2 -> 2 + 1 = 3
    # Best is 0.
    n2: int = 5
    edges2: List[List[int]] = [[0, 1], [1, 2], [2, 0], [3, 2], [3, 4]]
    s1_2: int = 0
    s2_2: int = 3
    result2: int = solution.earliest_shared_dependency(n2, edges2, s1_2, s2_2)
    print("Example 2 result:", result2)  # Expected: 0

    # Additional sample: no shared dependency
    n3: int = 4
    edges3: List[List[int]] = [[0, 1], [2, 3]]
    s1_3: int = 0
    s2_3: int = 2
    result3: int = solution.earliest_shared_dependency(n3, edges3, s1_3, s2_3)
    print("Example 3 result:", result3)  # Expected: -1