"""
Title: Earliest Meeting Point in a One-Way Conveyor Network

Problem Description:
In a large warehouse, stations are connected by one-way conveyor belts. The warehouse
is modeled as a directed graph with n stations labeled from 0 to n - 1. Each conveyor
belt is represented by a directed edge [u, v], meaning a package can move from station
u to station v in exactly 1 minute.

Two packages start at stations startA and startB at the same time. A station x is
considered a valid meeting point if both packages can reach x by following the
direction of the conveyor belts. The arrival time for a meeting point is defined as
the later of the two arrival times, because both packages must have arrived.

Return the station index of the valid meeting point that minimizes this arrival time.
If there are multiple such stations, return the smallest station index among them.
If no valid meeting point exists, return -1.

The graph is unweighted, but it may contain cycles and disconnected components.
A package is allowed to stay at its starting station, so if both packages start at
the same station, that station is automatically a valid meeting point with arrival
time 0.

Constraints:
- 1 <= n <= 100000
- 0 <= edges.length <= 200000
- edges[i] = [u, v]
- 0 <= u, v < n
- 0 <= startA, startB < n
- There may be duplicate edges, though they do not change the answer.
"""

from collections import deque
from typing import Deque, List


class Solution:
    def _build_graph(self, n: int, edges: List[List[int]]) -> List[List[int]]:
        """
        Build an adjacency list for the directed graph.

        Args:
            n: Number of nodes in the graph.
            edges: Directed edges where each edge is [u, v].

        Returns:
            Adjacency list where graph[u] contains all neighbors reachable from u.

        Time complexity:
            O(n + m), where m is the number of edges.

        Space complexity:
            O(n + m)
        """
        # We use an adjacency list because:
        # 1. The graph is sparse enough that storing only existing edges is efficient.
        # 2. BFS needs to quickly iterate over outgoing neighbors of each node.
        # 3. It is the standard representation for large unweighted graphs.
        graph: List[List[int]] = [[] for _ in range(n)]

        # Add each directed edge u -> v into the adjacency list.
        # Duplicate edges are allowed by the problem statement.
        # They do not affect correctness because BFS will still only assign
        # the shortest distance to a node once.
        for u, v in edges:
            graph[u].append(v)

        return graph

    def _bfs_distances(self, graph: List[List[int]], start: int) -> List[int]:
        """
        Compute shortest path distances from a start node in an unweighted directed graph.

        Args:
            graph: Adjacency list of the directed graph.
            start: Starting node.

        Returns:
            A list dist where dist[i] is the minimum number of edges needed to
            reach node i from start, or -1 if node i is unreachable.

        Time complexity:
            O(n + m), where n is number of nodes and m is number of edges.

        Space complexity:
            O(n)
        """
        n: int = len(graph)

        # Distance array initialized to -1 means "unvisited / unreachable so far".
        # Once a node gets a non-negative value, that value is its shortest distance
        # from the start node because BFS explores in increasing distance order.
        dist: List[int] = [-1] * n

        # Standard BFS queue.
        queue: Deque[int] = deque()

        # The start node is reachable from itself in 0 minutes.
        dist[start] = 0
        queue.append(start)

        # BFS explanation:
        # - We repeatedly take the next node from the queue.
        # - For each outgoing neighbor, if it has not been visited yet,
        #   we assign its distance as current distance + 1.
        # - Because every edge has equal weight (1 minute), BFS guarantees
        #   the first time we visit a node is the shortest possible time.
        while queue:
            current: int = queue.popleft()

            # Explore every node directly reachable from "current".
            for neighbor in graph[current]:
                # If neighbor has not been visited yet, we have found the shortest
                # path to it. We record the distance and push it into the queue
                # so its neighbors can be explored later.
                if dist[neighbor] == -1:
                    dist[neighbor] = dist[current] + 1
                    queue.append(neighbor)

        return dist

    def earliest_meeting_point(
        self, n: int, edges: List[List[int]], startA: int, startB: int
    ) -> int:
        """
        Find the valid meeting station that minimizes the later arrival time
        of the two packages. If multiple stations tie, return the smallest index.

        Args:
            n: Number of stations (nodes).
            edges: Directed edges [u, v].
            startA: Starting station of package A.
            startB: Starting station of package B.

        Returns:
            The index of the best meeting station, or -1 if no common reachable
            station exists.

        Time complexity:
            O(n + m), where m is the number of edges.
            We build the graph once and run BFS twice.

        Space complexity:
            O(n + m)
        """
        # Step 1: Build the directed graph.
        # We need outgoing edges from each node because movement follows
        # the direction of the conveyor belts.
        graph: List[List[int]] = self._build_graph(n, edges)

        # Step 2: Compute shortest distances from both starting stations.
        # dist_a[i] = shortest time for package A to reach station i
        # dist_b[i] = shortest time for package B to reach station i
        dist_a: List[int] = self._bfs_distances(graph, startA)
        dist_b: List[int] = self._bfs_distances(graph, startB)

        # Step 3: Scan all stations and evaluate valid meeting points.
        #
        # A station i is valid if:
        # - A can reach it: dist_a[i] != -1
        # - B can reach it: dist_b[i] != -1
        #
        # The meeting time at station i is:
        #   max(dist_a[i], dist_b[i])
        # because both packages must have arrived, so the later arrival determines
        # when the meeting can actually happen.
        #
        # We want:
        # 1. Minimum meeting time
        # 2. If tied, minimum station index
        best_station: int = -1
        best_time: int = float("inf")

        for station in range(n):
            # Skip stations that are not reachable by both packages.
            if dist_a[station] == -1 or dist_b[station] == -1:
                continue

            # Compute the actual meeting time for this station.
            meeting_time: int = max(dist_a[station], dist_b[station])

            # Update the answer if:
            # - this station gives a smaller meeting time, or
            # - it ties on meeting time but has a smaller index
            if meeting_time < best_time:
                best_time = meeting_time
                best_station = station
            elif meeting_time == best_time and station < best_station:
                best_station = station

        return best_station


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1: int = 6
    edges1: List[List[int]] = [[0, 2], [1, 2], [2, 3], [1, 4], [4, 3], [3, 5]]
    startA1: int = 0
    startB1: int = 1
    result1: int = solution.earliest_meeting_point(n1, edges1, startA1, startB1)
    print(result1)  # Expected: 2

    # Example 2
    n2: int = 5
    edges2: List[List[int]] = [[0, 1], [1, 2], [3, 4]]
    startA2: int = 0
    startB2: int = 3
    result2: int = solution.earliest_meeting_point(n2, edges2, startA2, startB2)
    print(result2)  # Expected: -1

    # Additional quick check: same starting station
    n3: int = 3
    edges3: List[List[int]] = [[0, 1], [1, 2]]
    startA3: int = 1
    startB3: int = 1
    result3: int = solution.earliest_meeting_point(n3, edges3, startA3, startB3)
    print(result3)  # Expected: 1