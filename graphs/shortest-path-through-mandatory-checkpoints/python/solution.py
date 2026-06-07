"""
Shortest Path Through Mandatory Checkpoints
===========================================

Problem Description:
You are given a weighted undirected graph with `n` nodes (labeled `0` to `n-1`) and a list
of edges where `edges[i] = [u, v, w]` represents an edge between node `u` and node `v` with
weight `w`. You are also given a source node `src`, a destination node `dst`, and a list of
`k` mandatory checkpoint nodes `checkpoints`.

Your task is to find the shortest path from `src` to `dst` that passes through ALL of the
mandatory checkpoints (in any order). If no such path exists, return `-1`.

Note: You may visit nodes multiple times, and the path does not need to be simple.

Constraints:
- 2 <= n <= 100
- 1 <= edges.length <= 500
- 0 <= u, v < n, u != v
- 1 <= w <= 1000
- 0 <= k <= 10
- 0 <= checkpoints[i] < n
- src != dst
- All checkpoint nodes are distinct and differ from src and dst
"""

import heapq
from itertools import permutations
from typing import List, Dict, Tuple


class Solution:
    def dijkstra(self, graph: Dict[int, List[Tuple[int, int]]], start: int, n: int) -> List[float]:
        """
        Run Dijkstra's algorithm from a single source node.

        Args:
            graph: Adjacency list representation {node: [(neighbor, weight), ...]}
            start: The source node to compute shortest paths from
            n: Total number of nodes in the graph

        Returns:
            A list of shortest distances from `start` to every other node.
            dist[i] = shortest distance from start to node i (float('inf') if unreachable)

        Time Complexity: O((V + E) log V) where V = number of nodes, E = number of edges
        Space Complexity: O(V + E) for the distance array and priority queue
        """
        # Initialize all distances to infinity (unreachable by default)
        dist = [float('inf')] * n
        dist[start] = 0  # Distance from start to itself is 0

        # Min-heap priority queue: (distance, node)
        # We always process the node with the smallest known distance first
        min_heap = [(0, start)]

        while min_heap:
            # Pop the node with the smallest current distance
            current_dist, current_node = heapq.heappop(min_heap)

            # If we've already found a shorter path to this node, skip it
            # (This handles "stale" entries in the heap)
            if current_dist > dist[current_node]:
                continue

            # Explore all neighbors of the current node
            for neighbor, weight in graph.get(current_node, []):
                new_dist = current_dist + weight

                # If we found a shorter path to the neighbor, update it
                if new_dist < dist[neighbor]:
                    dist[neighbor] = new_dist
                    heapq.heappush(min_heap, (new_dist, neighbor))

        return dist

    def shortestPathThroughCheckpoints(
        self,
        n: int,
        edges: List[List[int]],
        src: int,
        dst: int,
        checkpoints: List[int]
    ) -> int:
        """
        Find the shortest path from src to dst passing through all mandatory checkpoints.

        Strategy:
        ---------
        1. Build the graph as an adjacency list.
        2. Run Dijkstra from each "key node" (src, dst, and all checkpoints) to get
           pairwise shortest distances between all key nodes.
        3. Use bitmask DP (Traveling Salesman Problem style) to find the optimal
           ORDER in which to visit all checkpoints between src and dst.
           - State: (current_node_index, bitmask_of_visited_checkpoints)
           - We try all orderings of checkpoints efficiently using DP.

        Why Bitmask DP?
        ---------------
        With up to k=10 checkpoints, brute-force permutation would be 10! = 3,628,800
        which is feasible but bitmask DP is cleaner and more efficient: O(k^2 * 2^k).

        Args:
            n: Number of nodes in the graph (labeled 0 to n-1)
            edges: List of [u, v, w] representing undirected weighted edges
            src: Source node
            dst: Destination node
            checkpoints: List of mandatory checkpoint nodes to visit

        Returns:
            The minimum total weight of a path from src to dst through all checkpoints,
            or -1 if no such path exists.

        Time Complexity: O((k+2) * (V+E) log V + k^2 * 2^k)
            - (k+2) Dijkstra runs, each O((V+E) log V)
            - Bitmask DP over k checkpoints: O(k^2 * 2^k)
        Space Complexity: O((k+2) * V + k * 2^k)
            - Storing shortest distances from each key node
            - DP table of size k * 2^k
        """

        # -----------------------------------------------------------------------
        # STEP 1: Build the adjacency list (undirected graph)
        # -----------------------------------------------------------------------
        # We use a dictionary where graph[u] = list of (v, weight) tuples
        # Since the graph is undirected, each edge [u, v, w] adds entries for both u and v
        graph: Dict[int, List[Tuple[int, int]]] = {}
        for u, v, w in edges:
            if u not in graph:
                graph[u] = []
            if v not in graph:
                graph[v] = []
            graph[u].append((v, w))
            graph[v].append((u, w))  # Undirected: add both directions

        # -----------------------------------------------------------------------
        # STEP 2: Identify all "key nodes" and run Dijkstra from each
        # -----------------------------------------------------------------------
        # Key nodes are: src, dst, and all checkpoints
        # We need pairwise shortest distances between all key nodes.
        #
        # Why run Dijkstra from each key node?
        # Because we need to know the cost of traveling between any two key nodes
        # (e.g., from src to checkpoint[0], from checkpoint[0] to checkpoint[1], etc.)
        # Since the graph is undirected, dist(a->b) == dist(b->a), but we run from
        # each key node for simplicity and correctness.

        k = len(checkpoints)

        # key_nodes[0] = src, key_nodes[1..k] = checkpoints, key_nodes[k+1] = dst
        # This indexing makes the DP easier to reason about
        key_nodes = [src] + checkpoints + [dst]
        num_keys = len(key_nodes)  # = k + 2

        # Run Dijkstra from each key node and store the full distance array
        # dist_from[i] = shortest distances from key_nodes[i] to all other nodes
        dist_from: List[List[float]] = []
        for key in key_nodes:
            dist_from.append(self.dijkstra(graph, key, n))

        # -----------------------------------------------------------------------
        # STEP 3: Precompute pairwise distances between key nodes
        # -----------------------------------------------------------------------
        # key_dist[i][j] = shortest distance from key_nodes[i] to key_nodes[j]
        # This avoids repeated lookups during DP
        key_dist: List[List[float]] = []
        for i in range(num_keys):
            row = []
            for j in range(num_keys):
                # dist_from[i] gives distances from key_nodes[i] to all nodes
                # key_nodes[j] is the target node
                row.append(dist_from[i][key_nodes[j]])
            key_dist.append(row)

        # -----------------------------------------------------------------------
        # STEP 4: Handle edge case — no checkpoints
        # -----------------------------------------------------------------------
        # If there are no checkpoints, the answer is simply the shortest path
        # from src to dst (direct Dijkstra result)
        if k == 0:
            result = dist_from[0][dst]  # dist_from[0] = distances from src
            return int(result) if result != float('inf') else -1

        # -----------------------------------------------------------------------
        # STEP 5: Bitmask DP to find optimal checkpoint ordering
        # -----------------------------------------------------------------------
        # This is essentially the Traveling Salesman Problem (TSP) variant:
        # Start at src, visit ALL checkpoints (in any order), end at dst.
        #
        # State definition:
        #   dp[mask][i] = minimum cost to reach checkpoint i (0-indexed among checkpoints)
        #                 having visited exactly the checkpoints indicated by `mask`
        #
        # Bitmask encoding:
        #   mask is a bitmask of k bits. Bit j is set if checkpoint j has been visited.
        #   e.g., mask = 0b101 means checkpoints 0 and 2 have been visited.
        #
        # Indexing note:
        #   - key_nodes[0] = src
        #   - key_nodes[1..k] = checkpoints (checkpoint i corresponds to key_nodes[i+1])
        #   - key_nodes[k+1] = dst
        #   - key_dist[0][i+1] = distance from src to checkpoint i
        #   - key_dist[i+1][j+1] = distance from checkpoint i to checkpoint j
        #   - key_dist[i+1][k+1] = distance from checkpoint i to dst

        INF = float('inf')
        total_masks = 1 << k  # 2^k possible subsets of checkpoints

        # Initialize DP table with infinity
        # dp[mask][i] = min cost to be at checkpoint i with visited set = mask
        dp: List[List[float]] = [[INF] * k for _ in range(total_masks)]

        # Base case: Start at src, move to each checkpoint as the first stop
        # mask with only bit i set = (1 << i)
        for i in range(k):
            # Cost = distance from src (key index 0) to checkpoint i (key index i+1)
            dp[1 << i][i] = key_dist[0][i + 1]

        # Fill the DP table
        # We iterate over all possible subsets of visited checkpoints
        for mask in range(1, total_masks):
            for i in range(k):
                # If checkpoint i is not in the current visited set, skip
                # (We can only be AT checkpoint i if we've visited it)
                if not (mask & (1 << i)):
                    continue

                # If the current state is unreachable, skip
                if dp[mask][i] == INF:
                    continue

                # Try moving from checkpoint i to each unvisited checkpoint j
                for j in range(k):
                    if mask & (1 << j):
                        continue  # Checkpoint j already visited, skip

                    # New mask after visiting checkpoint j
                    new_mask = mask | (1 << j)

                    # Cost to go from checkpoint i to checkpoint j
                    # key_dist[i+1][j+1]: i+1 because key_nodes[0]=src, checkpoints start at index 1
                    travel_cost = key_dist[i + 1][j + 1]
                    new_cost = dp[mask][i] + travel_cost

                    # Update DP if we found a cheaper way to reach checkpoint j
                    if new_cost < dp[new_mask][j]:
                        dp[new_mask][j] = new_cost

        # -----------------------------------------------------------------------
        # STEP 6: Find the minimum cost to go from any last checkpoint to dst
        # -----------------------------------------------------------------------
        # The full mask means all checkpoints have been visited
        full_mask = total_masks - 1  # All k bits set = 2^k - 1

        min_total = INF
        for i in range(k):
            # dp[full_mask][i] = min cost to visit all checkpoints, ending at checkpoint i
            # key_dist[i+1][k+1] = distance from checkpoint i to dst
            if dp[full_mask][i] != INF:
                total_cost = dp[full_mask][i] + key_dist[i + 1][k + 1]
                if total_cost < min_total:
                    min_total = total_cost

        # -----------------------------------------------------------------------
        # STEP 7: Return result
        # -----------------------------------------------------------------------
        return int(min_total) if min_total != INF else -1


# =============================================================================
# MAIN: Test the solution with the provided examples
# =============================================================================
if __name__ == "__main__":
    solution = Solution()

    # -------------------------------------------------------------------------
    # Example 1:
    # n = 5
    # edges = [[0,1,2],[1,2,3],[2,3,1],[3,4,4],[0,3,10]]
    # src = 0, dst = 4, checkpoints = [2]
    # Expected Output: 10
    # Explanation: 0→1→2→3→4 with weights 2+3+1+4 = 10
    # -------------------------------------------------------------------------
    print("=" * 60)
    print("Example 1:")
    n1 = 5
    edges1 = [[0, 1, 2], [1, 2, 3], [2, 3, 1], [3, 4, 4], [0, 3, 10]]
    src1, dst1 = 0, 4
    checkpoints1 = [2]
    result1 = solution.shortestPathThroughCheckpoints(n1, edges1, src1, dst1, checkpoints1)
    print(f"  n={n1}, src={src1}, dst={dst1}, checkpoints={checkpoints1}")
    print(f"  edges={edges1}")
    print(f"  Result: {result1}")
    print(f"  Expected: 10")
    print(f"  {'PASS' if result1 == 10 else 'FAIL'}")

    # -------------------------------------------------------------------------
    # Example 2:
    # n = 4
    # edges = [[0,1,1],[1,2,1],[2,3,1],[0,3,10]]
    # src = 0, dst = 3, checkpoints = [1, 2]
    # Expected Output: 3
    # Explanation: 0→1→2→3 with weights 1+1+1 = 3
    # -------------------------------------------------------------------------
    print("=" * 60)
    print("Example 2:")
    n2 = 4
    edges2 = [[0, 1, 1], [1, 2, 1], [2, 3, 1], [0, 3, 10]]
    src2, dst2 = 0, 3
    checkpoints2 = [1, 2]
    result2 = solution.shortestPathThroughCheckpoints(n2, edges2, src2, dst2, checkpoints2)
    print(f"  n={n2}, src={src2}, dst={dst2}, checkpoints={checkpoints2}")
    print(f"  edges={edges2}")
    print(f"  Result: {result2}")
    print(f"  Expected: 3")
    print(f"  {'PASS' if result2 == 3 else 'FAIL'}")

    # -------------------------------------------------------------------------
    # Example 3: No checkpoints — direct shortest path
    # n = 3, edges = [[0,1,5],[1,2,3],[0,2,100]]
    # src = 0, dst = 2, checkpoints = []
    # Expected Output: 8 (0→1→2)
    # -------------------------------------------------------------------------
    print("=" * 60)
    print("Example 3 (No checkpoints):")
    n3 = 3
    edges3 = [[0, 1, 5], [1, 2, 3], [0, 2, 100]]
    src3, dst3 = 0, 2
    checkpoints3 = []
    result3 = solution.shortestPathThroughCheckpoints(n3, edges3, src3, dst3, checkpoints3)