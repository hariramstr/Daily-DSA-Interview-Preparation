"""
Title: Minimum Toll to Synchronize Two Rescue Drones

Problem Description:
A disaster response team operates on a directed graph of air corridors with n stations
labeled from 0 to n - 1. Each corridor is represented as [u, v, w], meaning a drone
can fly from station u to station v by paying toll w. Two rescue drones start at
different stations s1 and s2, and both must eventually reach the same rendezvous
station r. After meeting, exactly one of them needs to continue from r to the final
supply station t, carrying the combined payload. The total mission cost is defined as
the sum of the tolls paid by the first drone from s1 to r, the second drone from s2
to r, and the shared route from r to t.

Your task is to return the minimum possible total mission cost over all valid choices
of rendezvous station r. If there is no station where both drones can meet and then
reach t, return -1.

The drones may revisit stations and edges if needed, but all tolls are non-negative.
The rendezvous station may be equal to s1, s2, or t. Note that the two drones travel
independently before meeting, so their paths do not need to be disjoint.

Constraints:
- 1 <= n <= 100000
- 0 <= m == corridors.length <= 200000
- corridors[i] = [u, v, w]
- 0 <= u, v < n
- u != v
- 0 <= w <= 1000000000
- 0 <= s1, s2, t < n

Key idea:
We need:
1) shortest distance from s1 to every node
2) shortest distance from s2 to every node
3) shortest distance from every node to t

The third requirement is efficiently computed by reversing all edges and running
Dijkstra from t on the reversed graph.

Then for every possible rendezvous node r, the total cost is:
dist_s1[r] + dist_s2[r] + dist_to_t[r]

We return the minimum valid total.
"""

from __future__ import annotations

import heapq
from typing import List, Tuple


class Solution:
    def dijkstra(self, n: int, graph: List[List[Tuple[int, int]]], start: int) -> List[int]:
        """
        Compute shortest path distances from one start node to all nodes in a graph
        with non-negative edge weights using Dijkstra's algorithm.

        Args:
            n: Number of nodes in the graph.
            graph: Adjacency list where graph[u] contains (v, weight) pairs.
            start: Source node.

        Returns:
            A list dist where dist[i] is the minimum cost from start to i.
            If i is unreachable, dist[i] will be a very large number.

        Time complexity:
            O((n + m) log n), where m is the number of edges.

        Space complexity:
            O(n + m)
        """
        # We use a very large integer as "infinity".
        # Python integers do not overflow, so this is safe.
        inf: int = 10**30

        # dist[node] stores the best known distance from the start node to "node".
        # Initially, every node is unreachable except the start node itself.
        dist: List[int] = [inf] * n
        dist[start] = 0

        # Priority queue (min-heap) storing pairs: (current_distance, node).
        # Dijkstra always expands the node with the smallest known distance first.
        heap: List[Tuple[int, int]] = [(0, start)]

        # Standard Dijkstra loop.
        while heap:
            current_dist, node = heapq.heappop(heap)

            # If this heap entry is stale, skip it.
            # This happens because we may have pushed an older, worse distance
            # before discovering a better one later.
            if current_dist != dist[node]:
                continue

            # Explore all outgoing edges from the current node.
            for neighbor, weight in graph[node]:
                new_dist: int = current_dist + weight

                # If going through "node" improves the best known distance to "neighbor",
                # update it and push the new state into the heap.
                if new_dist < dist[neighbor]:
                    dist[neighbor] = new_dist
                    heapq.heappush(heap, (new_dist, neighbor))

        return dist

    def minimumWeight(
        self,
        n: int,
        edges: List[List[int]],
        src1: int,
        src2: int,
        dest: int,
    ) -> int:
        """
        Return the minimum total cost for two paths starting from src1 and src2
        to meet at some node r, then continue from r to dest.

        The total cost for a meeting node r is:
            dist(src1 -> r) + dist(src2 -> r) + dist(r -> dest)

        Args:
            n: Number of nodes.
            edges: Directed weighted edges [u, v, w].
            src1: Start node of the first drone.
            src2: Start node of the second drone.
            dest: Final destination node.

        Returns:
            The minimum possible total cost, or -1 if no valid meeting node exists.

        Time complexity:
            O((n + m) log n)

        Space complexity:
            O(n + m)
        """
        # ---------------------------------------------------------------------
        # STEP 1: Build two adjacency lists:
        #
        # 1) graph:
        #    Normal directed graph. Used to compute:
        #    - shortest distances from src1 to all nodes
        #    - shortest distances from src2 to all nodes
        #
        # 2) reverse_graph:
        #    Every edge u -> v becomes v -> u.
        #    This lets us compute shortest distances from every node to dest
        #    by running one Dijkstra starting from dest on the reversed graph.
        #
        # Why reverse the graph?
        # Because shortest path from x to dest in the original graph is equivalent to
        # shortest path from dest to x in the reversed graph.
        # This is a classic optimization that avoids running Dijkstra from every node.
        # ---------------------------------------------------------------------
        graph: List[List[Tuple[int, int]]] = [[] for _ in range(n)]
        reverse_graph: List[List[Tuple[int, int]]] = [[] for _ in range(n)]

        for u, v, w in edges:
            graph[u].append((v, w))
            reverse_graph[v].append((u, w))

        # ---------------------------------------------------------------------
        # STEP 2: Run Dijkstra three times.
        #
        # dist_from_src1[r] = shortest cost from src1 to r
        # dist_from_src2[r] = shortest cost from src2 to r
        # dist_to_dest[r]   = shortest cost from r to dest
        #
        # The third one is computed by running Dijkstra from dest on reverse_graph.
        # ---------------------------------------------------------------------
        dist_from_src1: List[int] = self.dijkstra(n, graph, src1)
        dist_from_src2: List[int] = self.dijkstra(n, graph, src2)
        dist_to_dest: List[int] = self.dijkstra(n, reverse_graph, dest)

        # ---------------------------------------------------------------------
        # STEP 3: Try every node as the rendezvous point.
        #
        # For a node r to be valid:
        # - src1 must be able to reach r
        # - src2 must be able to reach r
        # - r must be able to reach dest
        #
        # If all three distances are finite, then:
        # total_cost = dist_from_src1[r] + dist_from_src2[r] + dist_to_dest[r]
        #
        # We take the minimum over all nodes.
        # ---------------------------------------------------------------------
        inf: int = 10**30
        answer: int = inf

        for rendezvous in range(n):
            if (
                dist_from_src1[rendezvous] < inf
                and dist_from_src2[rendezvous] < inf
                and dist_to_dest[rendezvous] < inf
            ):
                total_cost: int = (
                    dist_from_src1[rendezvous]
                    + dist_from_src2[rendezvous]
                    + dist_to_dest[rendezvous]
                )
                if total_cost < answer:
                    answer = total_cost

        # If answer was never updated, no valid rendezvous node exists.
        return -1 if answer == inf else answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # Correct computation:
    # Meet at node 2:
    # 0 -> 2 = 2
    # 1 -> 2 = 3
    # 2 -> 5 cheapest is 2 -> 4 -> 3 -> 5 = 1 + 1 + 2 = 4
    # Total = 2 + 3 + 4 = 9
    n1 = 6
    corridors1 = [
        [0, 2, 2],
        [1, 2, 3],
        [2, 3, 4],
        [2, 4, 1],
        [4, 3, 1],
        [3, 5, 2],
        [4, 5, 5],
    ]
    s1_1 = 0
    s2_1 = 1
    t1 = 5
    result1 = solution.minimumWeight(n1, corridors1, s1_1, s2_1, t1)
    print(result1)  # Expected: 9

    # Example 2
    # Best rendezvous is node 0:
    # src1 already at 0 => 0
    # 2 -> 0 = 1
    # 0 -> 1 -> 3 = 5 + 2 = 7
    # Total = 8
    n2 = 4
    corridors2 = [
        [0, 1, 5],
        [1, 3, 2],
        [2, 0, 1],
    ]
    s1_2 = 0
    s2_2 = 2
    t2 = 3
    result2 = solution.minimumWeight(n2, corridors2, s1_2, s2_2, t2)
    print(result2)  # Expected: 8