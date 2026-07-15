"""
Title: Verify Single Route Through All Warehouses

Problem Description:
A logistics company stores package transfers as an undirected graph. There are n warehouses
labeled from 0 to n - 1, and each road in roads[i] = [a, b] means warehouse a is directly
connected to warehouse b.

The company wants to know whether its road map forms exactly one simple connected route
through all warehouses. In other words, starting from one end, you should be able to visit
every warehouse exactly once by following the roads, without encountering any branch and
without leaving any warehouse disconnected.

Return true if the graph forms a single path that uses all n warehouses. Otherwise, return false.

A graph is considered a valid single route if:
1. Every warehouse belongs to the same connected component.
2. Exactly two warehouses have degree 1 when n > 1 (the two ends of the route).
3. Every other warehouse has degree 2.
4. If n == 1, the graph with no roads is also valid.

Constraints:
- 1 <= n <= 1000
- 0 <= roads.length <= 1000
- roads[i].length == 2
- 0 <= a, b < n
- a != b
- There are no duplicate roads.

Example 1:
Input: n = 4, roads = [[0,1],[1,2],[2,3]]
Output: true

Example 2:
Input: n = 4, roads = [[0,1],[1,2],[1,3]]
Output: false
"""

from collections import deque
from typing import Deque, List


class Solution:
    def is_single_route(self, n: int, roads: List[List[int]]) -> bool:
        """
        Determine whether the undirected graph forms exactly one simple path
        that includes all warehouses.

        Args:
            n: Number of warehouses labeled from 0 to n - 1.
            roads: List of undirected roads, where each road connects two warehouses.

        Returns:
            True if the graph is a single connected path through all warehouses,
            otherwise False.

        Time complexity:
            O(n + m), where m is the number of roads.

        Space complexity:
            O(n + m), for the adjacency list, degree array, and BFS queue.
        """
        # Special case:
        # If there is only one warehouse, the only valid path is a graph with no roads.
        # This matches the problem statement exactly.
        if n == 1:
            return len(roads) == 0

        # A path graph with n nodes must have exactly n - 1 edges.
        # Why this matters:
        # - If there are fewer than n - 1 roads, the graph cannot be fully connected.
        # - If there are more than n - 1 roads, then there must be either a cycle
        #   or some extra branching structure.
        # So this is a very strong and efficient early check.
        if len(roads) != n - 1:
            return False

        # Build an adjacency list to represent the graph.
        # adjacency[u] will store all neighbors directly connected to warehouse u.
        #
        # We use a list of lists because:
        # - warehouse labels are integers from 0 to n - 1
        # - this makes indexing direct and efficient
        # - it is beginner-friendly and easy to understand
        adjacency: List[List[int]] = [[] for _ in range(n)]

        # degree[i] will count how many roads touch warehouse i.
        # This is important because a valid path graph has a very specific degree pattern:
        # - exactly two nodes with degree 1 (the two ends)
        # - all other nodes with degree 2
        degree: List[int] = [0] * n

        # Fill the adjacency list and degree counts.
        for a, b in roads:
            adjacency[a].append(b)
            adjacency[b].append(a)
            degree[a] += 1
            degree[b] += 1

        # Count how many warehouses have degree 1 and verify all degrees are valid.
        #
        # For a path with n > 1:
        # - exactly two nodes must have degree 1
        # - every other node must have degree 2
        #
        # Any node with degree 0 means disconnected.
        # Any node with degree >= 3 means branching.
        degree_one_count = 0

        for d in degree:
            if d == 1:
                degree_one_count += 1
            elif d == 2:
                # Degree 2 is valid for internal nodes of the path.
                pass
            else:
                # Any other degree makes it impossible to be a single simple path.
                return False

        # There must be exactly two endpoints.
        if degree_one_count != 2:
            return False

        # Even though the edge count and degree pattern are already very restrictive,
        # we still explicitly verify connectivity because the problem requires that
        # every warehouse belongs to the same connected component.
        #
        # We start BFS from one endpoint (a node with degree 1).
        # In a valid path, traversing from one endpoint should eventually visit all nodes.
        start: int = -1
        for i in range(n):
            if degree[i] == 1:
                start = i
                break

        # This should never remain -1 because for n > 1 we already checked that
        # there are exactly two degree-1 nodes. Still, keeping the code safe and clear.
        if start == -1:
            return False

        # visited[i] tells us whether warehouse i has been reached during BFS.
        visited: List[bool] = [False] * n

        # Standard BFS queue.
        queue: Deque[int] = deque([start])
        visited[start] = True

        # Count how many warehouses we can reach.
        visited_count = 0

        while queue:
            current = queue.popleft()
            visited_count += 1

            # Explore all directly connected neighbors.
            for neighbor in adjacency[current]:
                if not visited[neighbor]:
                    visited[neighbor] = True
                    queue.append(neighbor)

        # The graph is connected if and only if BFS reached every warehouse.
        return visited_count == n


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # n = 4, roads = [[0,1],[1,2],[2,3]]
    # This is a straight chain: 0 - 1 - 2 - 3
    # Degrees are [1, 2, 2, 1], connected, so expected result is True.
    n1 = 4
    roads1 = [[0, 1], [1, 2], [2, 3]]
    print(solution.is_single_route(n1, roads1))  # Expected: True

    # Example 2:
    # n = 4, roads = [[0,1],[1,2],[1,3]]
    # Node 1 has degree 3, which creates a branch.
    # Therefore this is not a single simple path.
    n2 = 4
    roads2 = [[0, 1], [1, 2], [1, 3]]
    print(solution.is_single_route(n2, roads2))  # Expected: False

    # Additional sample:
    # Single warehouse with no roads is valid.
    n3 = 1
    roads3: List[List[int]] = []
    print(solution.is_single_route(n3, roads3))  # Expected: True

    # Additional sample:
    # Disconnected graph, not a single route.
    n4 = 4
    roads4 = [[0, 1], [2, 3]]
    print(solution.is_single_route(n4, roads4))  # Expected: False