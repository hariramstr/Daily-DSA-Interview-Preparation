"""
Minimum Time to Spread Signal Across Network
============================================

Problem Description:
You are given a network of n nodes labeled from 1 to n, connected by directed edges.
Each edge has a travel time (weight). A signal is simultaneously initiated from a set
of source nodes at time 0. The signal spreads along directed edges and arrives at a
node only after the full travel time of the edge has elapsed.

Return the minimum time it takes for ALL nodes in the network to receive the signal.
If it is impossible for all nodes to receive the signal, return -1.

The signal from any source can reach any node, and the signal at a node is considered
received at the EARLIEST time any source's signal arrives at it.

Constraints:
- 1 <= n <= 100
- 1 <= edges.length <= 6000
- edges[i] = [u, v, w] where u is source, v is destination, w is travel time
- 1 <= u, v <= n, u != v
- 1 <= w <= 100
- 1 <= sources.length <= n
- All values in sources are distinct and in range [1, n]
- There may be multiple edges between the same pair of nodes
"""

import heapq
from typing import List, Dict, Tuple
from collections import defaultdict


class Solution:
    def network_delay_time(
        self, n: int, edges: List[List[int]], sources: List[int]
    ) -> int:
        """
        Find the minimum time for all nodes to receive the signal from multiple sources.

        This solution uses a modified Dijkstra's algorithm that starts from multiple
        source nodes simultaneously (multi-source Dijkstra). We initialize the priority
        queue with all source nodes at time 0, then expand outward finding the shortest
        path to each node.

        The answer is the maximum of all shortest distances (since we need ALL nodes
        to receive the signal), or -1 if any node is unreachable.

        Args:
            n (int): Number of nodes in the network (labeled 1 to n)
            edges (List[List[int]]): List of [u, v, w] directed edges with travel time w
            sources (List[int]): List of source nodes that emit signal at time 0

        Returns:
            int: Minimum time for all nodes to receive the signal, or -1 if impossible

        Time Complexity: O((V + E) * log V) where V = n nodes, E = number of edges
                        - Each node is processed once from the priority queue: O(V log V)
                        - Each edge is relaxed once: O(E log V)
        Space Complexity: O(V + E) for the adjacency list and distance array
        """

        # =====================================================================
        # STEP 1: Build the Adjacency List (Graph Representation)
        # =====================================================================
        # We use a dictionary mapping each node to a list of (neighbor, weight) tuples.
        # defaultdict(list) automatically creates an empty list for new keys,
        # so we don't need to check if a key exists before appending.
        #
        # Why adjacency list? It's efficient for sparse graphs and allows us to
        # quickly find all neighbors of a given node during Dijkstra's traversal.

        graph: Dict[int, List[Tuple[int, int]]] = defaultdict(list)

        for u, v, w in edges:
            # For each directed edge from u to v with weight w,
            # add (destination, travel_time) to u's neighbor list
            graph[u].append((v, w))

        # =====================================================================
        # STEP 2: Initialize Distance Array
        # =====================================================================
        # dist[node] = minimum time for the signal to reach 'node'
        # We initialize all distances to infinity (unreachable) except source nodes.
        #
        # Using float('inf') as a sentinel value means "not yet reached".
        # Nodes 1..n are used, so we create an array of size n+1 (index 0 unused).

        dist: List[float] = [float('inf')] * (n + 1)

        # =====================================================================
        # STEP 3: Initialize the Priority Queue (Min-Heap) with All Sources
        # =====================================================================
        # This is the KEY insight for multi-source Dijkstra:
        # Instead of starting from a single node, we start from ALL source nodes
        # simultaneously at time 0.
        #
        # The priority queue stores (current_time, node) tuples.
        # heapq in Python is a min-heap, so the node with the smallest current_time
        # is always processed first — this is what makes Dijkstra's algorithm correct.
        #
        # By adding all sources at time 0, we simulate them all emitting signals
        # at the same moment.

        min_heap: List[Tuple[int, int]] = []

        for source in sources:
            # Each source node receives the signal at time 0
            dist[source] = 0
            # Push (time=0, node=source) onto the heap
            heapq.heappush(min_heap, (0, source))

        # =====================================================================
        # STEP 4: Dijkstra's Algorithm - Process Nodes in Order of Distance
        # =====================================================================
        # We repeatedly extract the node with the smallest known distance,
        # then try to "relax" (improve) the distances to its neighbors.
        #
        # Key property of Dijkstra's: When we pop a node from the min-heap,
        # we have found its FINAL shortest distance (assuming non-negative weights).
        # This is why we skip nodes that have already been processed with a
        # shorter distance.

        while min_heap:
            # Extract the node with the current minimum distance
            current_time, current_node = heapq.heappop(min_heap)

            # OPTIMIZATION: If we've already found a shorter path to this node,
            # skip this outdated entry. This happens because we may push the same
            # node multiple times with different distances before finalizing it.
            if current_time > dist[current_node]:
                # This is a stale entry — we already processed this node
                # with a smaller distance, so skip it
                continue

            # Explore all neighbors of the current node
            for neighbor, travel_time in graph[current_node]:
                # Calculate the time to reach 'neighbor' via 'current_node'
                new_time = current_time + travel_time

                # RELAXATION: If this new path is shorter than the known distance,
                # update the distance and add to the heap for further exploration
                if new_time < dist[neighbor]:
                    dist[neighbor] = new_time
                    # Push the updated (time, neighbor) onto the heap
                    # We'll process this when it's the minimum
                    heapq.heappush(min_heap, (new_time, neighbor))

        # =====================================================================
        # STEP 5: Determine the Answer
        # =====================================================================
        # After Dijkstra's completes, dist[i] holds the minimum time for node i
        # to receive the signal (from any source).
        #
        # We need ALL nodes (1 to n) to receive the signal, so the answer is the
        # MAXIMUM of all dist[1..n] — the last node to receive the signal.
        #
        # If any node still has dist = infinity, it's unreachable → return -1.

        # Find the maximum distance among all nodes 1..n
        # (We skip index 0 since nodes are labeled 1..n)
        max_time = max(dist[1:n + 1])

        # If max_time is still infinity, at least one node is unreachable
        if max_time == float('inf'):
            return -1

        return int(max_time)


# =============================================================================
# TRACE-THROUGH VERIFICATION
# =============================================================================
# Example 1: n=4, edges=[[1,2,1],[1,3,4],[2,3,2],[3,4,1]], sources=[1]
#
# Graph:
#   1 -> 2 (weight 1)
#   1 -> 3 (weight 4)
#   2 -> 3 (weight 2)
#   3 -> 4 (weight 1)
#
# Initial: dist = [inf, 0, inf, inf, inf], heap = [(0, 1)]
#
# Pop (0, 1):
#   Neighbor 2: new_time = 0+1 = 1 < inf → dist[2]=1, push (1,2)
#   Neighbor 3: new_time = 0+4 = 4 < inf → dist[3]=4, push (4,3)
#   heap = [(1,2), (4,3)]
#
# Pop (1, 2):
#   Neighbor 3: new_time = 1+2 = 3 < 4 → dist[3]=3, push (3,3)
#   heap = [(3,3), (4,3)]
#
# Pop (3, 3):
#   Neighbor 4: new_time = 3+1 = 4 < inf → dist[4]=4, push (4,4)
#   heap = [(4,3), (4,4)]
#
# Pop (4, 3): current_time=4 > dist[3]=3 → SKIP (stale entry)
#
# Pop (4, 4): No outgoing edges from node 4
#
# dist = [inf, 0, 1, 3, 4]
# max(dist[1:5]) = max(0, 1, 3, 4) = 4 ✓ Expected: 4
#
# -----------------------------------------------------------------------------
# Example 2: n=4, edges=[[1,2,1],[3,4,1]], sources=[1,3]
#
# Graph:
#   1 -> 2 (weight 1)
#   3 -> 4 (weight 1)
#
# Initial: dist = [inf, 0, inf, 0, inf], heap = [(0,1), (0,3)]
#
# Pop (0, 1):
#   Neighbor 2: new_time = 0+1 = 1 < inf → dist[2]=1, push (1,2)
#   heap = [(0,3), (1,2)]
#
# Pop (0, 3):
#   Neighbor 4: new_time = 0+1 = 1 < inf → dist[4]=1, push (1,4)
#   heap = [(1,2), (1,4)]
#
# Pop (1, 2): No outgoing edges from node 2
#
# Pop (1, 4): No outgoing edges from node 4
#
# dist = [inf, 0, 1, 0, 1]
# max(dist[1:5]) = max(0, 1, 0, 1) = 1 ✓ Expected: 1
# =============================================================================


if __name__ == "__main__":
    solution = Solution()

    print("=" * 60)
    print("Minimum Time to Spread Signal Across Network")
    print("=" * 60)

    # -----------------------------------------------------------------
    # Example 1: Single source, signal must traverse the whole graph
    # -----------------------------------------------------------------
    print("\nExample 1:")
    n1 = 4
    edges1 = [[1, 2, 1], [1, 3, 4], [2, 3, 2], [3, 4, 1]]
    sources1 = [1]
    result1 = solution.network_delay_time(n1, edges1, sources1)
    print(f"  n = {n1}")
    print(f"  edges = {edges1}")
    print(f"  sources = {sources1}")
    print(f"  Output: {result1}")
    print(f"  Expected: 4")
    print(f"  {'✓ CORRECT' if result1 == 4 else '✗ WRONG'}")

    # -----------------------------------------------------------------
    # Example 2: Multiple sources, each covers part of the graph
    # -----------------------------------------------------------------
    print("\nExample 2:")
    n2 = 4
    edges2 = [[1, 2, 1], [3, 4, 1]]
    sources2 = [1, 3]
    result2 = solution.network_delay_time(n2, edges2, sources2)
    print(f"  n = {n2}")
    print(f"  edges = {edges2}")
    print(f"  sources = {sources2}")
    print(f"  Output: {result2}")
    print(f"  Expected: 1")
    print(f"  {'✓ CORRECT' if result2 == 1 else '✗ WRONG'}")

    # -----------------------------------------------------------------
    # Example 3: Impossible case — node 3 is unreachable from node 1
    # -----------------------------------------------------------------
    print("\nExample 3 (Impossible case):")
    n3 = 3
    edges3 = [[1, 2, 1]]  # Node 3 has no incoming edges
    sources3 = [1]
    result3 = solution.network_delay_time(n3, edges3, sources3)
    print(f"  n = {n3}")
    print(f"  edges = {edges3}")
    print(f"  sources = {sources3}")
    print(f"  Output: {result3}")
    print(f"  Expected: -1")
    print(f"  {'✓ CORRECT' if result3 == -1 else '✗ WRONG'}")

    # -----------------------------------------------------------------
    # Example 4: All nodes are sources — signal already at all nodes
    # -----------------------------------------------------------------
    print("\nExample 4 (All nodes are sources):")
    n4 = 3
    edges4 = [[1, 2, 5], [2, 3, 5]]
    sources4 = [1, 2, 3]
    result4 = solution.network_delay_time(n4, edges4, sources4)
    print(f"  n = {n4}")
    print(f"  edges = {edges4}")
    print(f"  sources = {sources4}")
    print(f"  Output: {result4}")
    print(f"  Expected: 0")
    print(f"  {'✓ CORRECT' if result4 == 0 else '✗ WRONG'}")

    # -----------------------------------------------------------------
    # Example 5: Multiple edges between same pair, pick the shortest
    # -----------------------------------------------------------------
    print("\nExample 5 (Multiple edges between same pair):")
    n5 = 2
    edges5 = [[1, 2, 10], [1, 2, 3], [1, 2, 7]]
    sources5 = [1]
    result5 = solution.network_delay_time(n5, edges5, sources5)
    print(f"  n = {n5}")
    print(f"  edges = {edges5}")
    print(f"  sources = {sources5}")
    print(f"  Output: {result5}")
    print(f"  Expected: 3")
    print(f"  {'✓ CORRECT' if result5 == 3 else '✗ WRONG'}")

    print("\n" + "=" * 60)
    print("All test cases completed!")
    print("=" * 60)