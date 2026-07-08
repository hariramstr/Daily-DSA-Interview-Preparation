"""
Minimum Time to Escape a Collapsing Tunnel Grid

Problem Description:
You are given an undirected graph representing underground tunnel junctions.
There are n junctions labeled from 0 to n - 1 and m bidirectional tunnels.
Traveling through tunnel (u, v) always takes w minutes.

Each junction i has a collapse time collapse[i]. You may stand at or arrive at
junction i only at times strictly less than collapse[i]. If you reach a junction
at time t where t >= collapse[i], that state is invalid because the junction has
already collapsed.

You start at junction 0 at time 0 and want to reach junction n - 1 as early as
possible.

In addition to moving through tunnels, you may wait at any currently safe
junction for any non-negative integer amount of time, as long as the junction
has not collapsed before or during the waiting period.

Each tunnel is described by five values [u, v, w, open, close]. The tunnel can
only be entered at integer times t such that:

    t mod (open + close) < open

So the tunnel is open for "open" minutes, then closed for "close" minutes,
repeating forever starting from time 0.

If you begin traversing the tunnel at a valid open time t, you spend exactly
w minutes moving, and the tunnel does not need to remain open after departure.
You still must arrive at the destination junction before it collapses.

Return the minimum time needed to reach junction n - 1, or -1 if it is impossible.
"""

from __future__ import annotations

import heapq
from typing import List, Tuple


class Solution:
    def _next_open_departure(self, current_time: int, open_time: int, close_time: int) -> int:
        """
        Compute the earliest integer time >= current_time when a tunnel can be entered.

        The tunnel follows a repeating cycle:
        - open for `open_time` minutes
        - closed for `close_time` minutes

        A departure at time t is allowed iff:
            t % (open_time + close_time) < open_time

        Args:
            current_time: The earliest time we would like to depart.
            open_time: Number of open minutes in each cycle.
            close_time: Number of closed minutes in each cycle.

        Returns:
            The earliest valid departure time >= current_time.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        cycle: int = open_time + close_time
        position_in_cycle: int = current_time % cycle

        # If we are already inside the open segment of the cycle,
        # we can depart immediately.
        if position_in_cycle < open_time:
            return current_time

        # Otherwise, we are in the closed segment.
        # The next open time is the start of the next cycle.
        return current_time + (cycle - position_in_cycle)

    def minimum_time(self, n: int, edges: List[List[int]], collapse: List[int]) -> int:
        """
        Compute the earliest valid arrival time from node 0 to node n - 1.

        This uses a Dijkstra-style shortest path algorithm on a time-dependent graph.
        The graph is time-dependent because each edge may only be entered at certain
        periodic times, and each node becomes unusable at its collapse time.

        Key idea:
        - The earliest time we can reach a node matters.
        - From that time, for each adjacent edge, we compute the earliest valid
          departure time considering the edge's periodic gate.
        - We also verify that:
            1) we can remain at the current node until that departure time
            2) arrival at the neighbor happens strictly before the neighbor collapses

        Because waiting is allowed and edge availability is periodic, the travel time
        from a node depends on the current time. However, this system satisfies the
        FIFO property: arriving earlier at a node can never make you worse off than
        arriving later, because you can always wait. Therefore, Dijkstra remains valid.

        Args:
            n: Number of nodes.
            edges: List of tunnels, each as [u, v, w, open, close].
            collapse: collapse[i] is the time when node i collapses.

        Returns:
            The minimum time to reach node n - 1, or -1 if impossible.

        Time complexity:
            O((n + m) log n)

        Space complexity:
            O(n + m)
        """
        # If the starting node is already collapsed at time 0, we cannot even begin.
        # The problem statement guarantees collapse[0] > 0, but this check makes the
        # implementation robust and self-contained.
        if collapse[0] <= 0:
            return -1

        # Build an adjacency list for the undirected graph.
        #
        # For each node, we store a list of tuples:
        #   (neighbor, travel_time, open_time, close_time)
        #
        # Why adjacency list?
        # - Efficient for sparse graphs
        # - The constraints are large (up to 200000 edges), so adjacency matrix
        #   would be far too expensive.
        graph: List[List[Tuple[int, int, int, int]]] = [[] for _ in range(n)]
        for u, v, w, open_time, close_time in edges:
            graph[u].append((v, w, open_time, close_time))
            graph[v].append((u, w, open_time, close_time))

        # Standard Dijkstra distance array:
        # dist[i] = earliest known valid arrival time at node i
        #
        # We initialize all nodes as unreachable.
        inf: int = 10**30
        dist: List[int] = [inf] * n
        dist[0] = 0

        # Priority queue entries are (time, node).
        # The smallest time is processed first.
        heap: List[Tuple[int, int]] = [(0, 0)]

        while heap:
            current_time, node = heapq.heappop(heap)

            # If this heap entry is stale, skip it.
            # This is a standard Dijkstra optimization:
            # a node may have been pushed multiple times, but only the smallest
            # time should be processed.
            if current_time != dist[node]:
                continue

            # If we popped the destination, this is the earliest possible arrival
            # because Dijkstra processes states in nondecreasing time order.
            if node == n - 1:
                return current_time

            # Explore all tunnels leaving the current node.
            for neighbor, travel_time, open_time, close_time in graph[node]:
                # Step 1:
                # Find the earliest time >= current_time when this tunnel can be entered.
                departure_time: int = self._next_open_departure(
                    current_time, open_time, close_time
                )

                # Step 2:
                # Verify that we are allowed to remain at the current node until
                # departure_time.
                #
                # Important rule:
                # We may stand at a node only at times strictly less than collapse[node].
                #
                # If departure_time >= collapse[node], then we would need to still be
                # at the current node at an invalid time, so this move is impossible.
                if departure_time >= collapse[node]:
                    continue

                # Step 3:
                # Compute arrival time at the neighbor.
                arrival_time: int = departure_time + travel_time

                # Step 4:
                # Verify that arrival at the neighbor is valid.
                # We may arrive only if arrival_time < collapse[neighbor].
                if arrival_time >= collapse[neighbor]:
                    continue

                # Step 5:
                # Standard Dijkstra relaxation.
                if arrival_time < dist[neighbor]:
                    dist[neighbor] = arrival_time
                    heapq.heappush(heap, (arrival_time, neighbor))

        # If destination was never reached, return -1.
        return -1


if __name__ == "__main__":
    solver = Solution()

    # Example 1:
    # The original statement contains a correction in the explanation.
    # We use the corrected fourth edge:
    # [2, 3, 1, 2, 2]
    n1 = 4
    edges1 = [
        [0, 1, 3, 2, 2],
        [1, 3, 2, 3, 1],
        [0, 2, 2, 1, 3],
        [2, 3, 1, 2, 2],
    ]
    collapse1 = [100, 10, 10, 20]
    print(solver.minimum_time(n1, edges1, collapse1))  # Expected: 5

    # Example 2:
    # The statement also contains a correction in the explanation.
    # We use the corrected collapse array:
    # [20, 5, 9]
    n2 = 3
    edges2 = [
        [0, 1, 4, 1, 2],
        [1, 2, 3, 1, 1],
        [0, 2, 10, 5, 5],
    ]
    collapse2 = [20, 5, 9]
    print(solver.minimum_time(n2, edges2, collapse2))  # Expected: -1