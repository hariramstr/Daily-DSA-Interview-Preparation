"""
Title: Cheapest Shared Shuttle Pickup

Problem Description:
A company campus runs one-way shuttle lanes between buildings. The campus wants to
pick a single shuttle pickup building where two employees can meet before riding
together to the main office.

You are given:
- An integer n representing buildings labeled from 0 to n - 1
- A list of directed edges lanes where lanes[i] = [u, v, cost] means there is a
  one-way shuttle lane from building u to building v with travel cost cost
- Three distinct buildings: aliceStart, bobStart, and office

Alice starts at aliceStart and Bob starts at bobStart. They may travel independently
through the directed graph and choose any building m as their meeting point. After
both reach m, they continue together from m to office, paying that final segment
cost only once because they share the shuttle from that point onward.

We must minimize:
    dist(aliceStart, m) + dist(bobStart, m) + dist(m, office)

for any building m that is reachable from both starting buildings and can also
reach office.

Return the minimum total travel cost. If no valid meeting point exists, return -1.

Constraints:
- 2 <= n <= 100000
- 1 <= lanes.length <= 200000
- 0 <= u, v < n
- 1 <= cost <= 1000000000
- aliceStart, bobStart, and office are distinct
- There may be multiple edges between the same pair of buildings
- The graph is not guaranteed to be strongly connected
"""

from __future__ import annotations

import heapq
from typing import List, Tuple


class Solution:
    def _dijkstra(self, n: int, graph: List[List[Tuple[int, int]]], start: int) -> List[int]:
        """
        Compute shortest path distances from one start node to all nodes
        in a directed weighted graph using Dijkstra's algorithm.

        Args:
            n: Number of nodes in the graph.
            graph: Adjacency list where graph[u] contains (v, weight) pairs.
            start: The source node from which distances are computed.

        Returns:
            A list dist where dist[i] is the minimum cost to reach node i
            from start. If node i is unreachable, dist[i] will be a very
            large sentinel value.

        Time complexity:
            O((n + e) log n), where e is the number of edges.

        Space complexity:
            O(n + e) for the graph storage and distance / heap structures.
        """
        # We use a very large integer as "infinity".
        # Python integers do not overflow like fixed-width integers, so this is safe.
        inf: int = 10**30

        # dist[i] will store the currently known shortest distance from `start` to i.
        # Initially, every node is unreachable except the start node itself.
        dist: List[int] = [inf] * n
        dist[start] = 0

        # Priority queue (min-heap) storing pairs:
        #   (current_best_distance_to_node, node)
        #
        # Why a heap?
        # Dijkstra's algorithm repeatedly needs the not-yet-processed node with
        # the smallest known distance. A min-heap gives that efficiently.
        heap: List[Tuple[int, int]] = [(0, start)]

        # Standard Dijkstra loop:
        # Keep extracting the node with the smallest tentative distance.
        while heap:
            current_dist, node = heapq.heappop(heap)

            # Important optimization:
            # If this heap entry is "stale" (meaning we already found a better path
            # to this node after this entry was pushed), we skip it.
            #
            # Example:
            # - We first push distance 10 for node X
            # - Later we discover distance 7 for node X and push that too
            # - When the old (10, X) comes out, it is outdated and should be ignored
            if current_dist != dist[node]:
                continue

            # Explore all outgoing edges from the current node.
            for neighbor, weight in graph[node]:
                new_dist: int = current_dist + weight

                # Relaxation step:
                # If going through `node` gives a cheaper path to `neighbor`,
                # update the distance and push the new state into the heap.
                if new_dist < dist[neighbor]:
                    dist[neighbor] = new_dist
                    heapq.heappush(heap, (new_dist, neighbor))

        return dist

    def minimum_shared_shuttle_cost(
        self,
        n: int,
        lanes: List[List[int]],
        aliceStart: int,
        bobStart: int,
        office: int,
    ) -> int:
        """
        Find the minimum total travel cost for Alice and Bob to meet at some node
        and then travel together to the office.

        The key formula is:
            dist(aliceStart, m) + dist(bobStart, m) + dist(m, office)

        We compute:
        1. Shortest distances from Alice's start to every node
        2. Shortest distances from Bob's start to every node
        3. Shortest distances from every node to the office

        Step 3 is done efficiently by reversing all edges and running Dijkstra
        from the office on the reversed graph. That gives, for each node m,
        the shortest distance from m to office in the original graph.

        Args:
            n: Number of buildings.
            lanes: Directed weighted edges [u, v, cost].
            aliceStart: Alice's starting building.
            bobStart: Bob's starting building.
            office: Destination building.

        Returns:
            The minimum total cost if a valid meeting point exists, otherwise -1.

        Time complexity:
            O((n + e) log n), because we run Dijkstra three times.

        Space complexity:
            O(n + e), for adjacency lists and distance arrays.
        """
        # Build two graphs:
        #
        # 1. graph:
        #    The original directed graph.
        #    Used to compute distances from Alice and Bob to all possible meeting points.
        #
        # 2. reverse_graph:
        #    Every edge u -> v with cost w becomes v -> u with cost w.
        #    Running Dijkstra from `office` on this reversed graph gives us:
        #       distance_from_node_to_office_in_original_graph
        #
        # Why reverse the graph?
        # Because Dijkstra naturally computes distances FROM one source TO all nodes.
        # We need dist(m, office) for every m.
        # Instead of running Dijkstra from every m (far too expensive),
        # we reverse edges and run one Dijkstra from office.
        graph: List[List[Tuple[int, int]]] = [[] for _ in range(n)]
        reverse_graph: List[List[Tuple[int, int]]] = [[] for _ in range(n)]

        for u, v, cost in lanes:
            graph[u].append((v, cost))
            reverse_graph[v].append((u, cost))

        # Compute shortest distances from Alice's start to every node.
        dist_from_alice: List[int] = self._dijkstra(n, graph, aliceStart)

        # Compute shortest distances from Bob's start to every node.
        dist_from_bob: List[int] = self._dijkstra(n, graph, bobStart)

        # Compute shortest distances from every node to office.
        # This is done by running Dijkstra from office on the reversed graph.
        dist_to_office: List[int] = self._dijkstra(n, reverse_graph, office)

        inf: int = 10**30
        answer: int = inf

        # Try every node as the meeting point.
        #
        # A node `m` is valid if:
        # - Alice can reach m
        # - Bob can reach m
        # - m can reach office
        #
        # In terms of our precomputed arrays, that means none of the three
        # distances is infinity.
        for meeting_point in range(n):
            if (
                dist_from_alice[meeting_point] < inf
                and dist_from_bob[meeting_point] < inf
                and dist_to_office[meeting_point] < inf
            ):
                total_cost: int = (
                    dist_from_alice[meeting_point]
                    + dist_from_bob[meeting_point]
                    + dist_to_office[meeting_point]
                )

                if total_cost < answer:
                    answer = total_cost

        # If answer was never updated, no valid meeting point exists.
        return -1 if answer == inf else answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # Graph:
    # 0 -> 2 (2)
    # 1 -> 2 (4)
    # 2 -> 3 (3)
    # 2 -> 4 (1)
    # 4 -> 3 (1)
    # 3 -> 5 (2)
    # 4 -> 5 (5)
    #
    # Best meeting point is 2:
    # Alice: 0 -> 2 = 2
    # Bob:   1 -> 2 = 4
    # Shared: 2 -> 4 -> 3 -> 5 = 1 + 1 + 2 = 4
    # Total = 2 + 4 + 4 = 10
    #
    # Note:
    # The problem statement's written explanation says the shared route cost is 5,
    # but the listed edges imply it is actually 4. Therefore the mathematically
    # correct answer for the provided graph is 10.
    n1: int = 6
    lanes1: List[List[int]] = [
        [0, 2, 2],
        [1, 2, 4],
        [2, 3, 3],
        [2, 4, 1],
        [4, 3, 1],
        [3, 5, 2],
        [4, 5, 5],
    ]
    alice_start_1: int = 0
    bob_start_1: int = 1
    office_1: int = 5

    result1: int = solution.minimum_shared_shuttle_cost(
        n1, lanes1, alice_start_1, bob_start_1, office_1
    )
    print(result1)

    # Example 2
    # Alice can reach office through 0 -> 1 -> 4
    # Bob starts at 2 and can only reach 3
    # There is no node reachable by Bob that can also reach office 4
    # So the answer is -1
    n2: int = 5
    lanes2: List[List[int]] = [
        [0, 1, 3],
        [1, 4, 4],
        [2, 3, 2],
    ]
    alice_start_2: int = 0
    bob_start_2: int = 2
    office_2: int = 4

    result2: int = solution.minimum_shared_shuttle_cost(
        n2, lanes2, alice_start_2, bob_start_2, office_2
    )
    print(result2)