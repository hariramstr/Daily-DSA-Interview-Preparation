"""
Title: Minimum Delay to Stream K Live Feeds

Problem Description:
A media platform must deliver a live event to viewers through a network of relay servers.
The network is an undirected weighted graph with n servers labeled 0 to n - 1.
Each edge [u, v, w] means a stream can be forwarded between servers u and v with
transmission delay w milliseconds. Server 0 is the origin server. Some servers are
marked as viewer gateways, and at least k of those gateways must receive the stream.

You may choose any subset of the viewer gateways as long as at least k of them are reached.
The cost of a delivery plan is the maximum shortest-path delay from server 0 to any chosen
gateway in that plan. Your task is to return the minimum possible cost.

In other words, compute the shortest delay from server 0 to every gateway, then choose k
gateways so that the largest chosen delay is as small as possible. If fewer than k gateways
are reachable from server 0, return -1.

This problem is designed for large sparse graphs, so an efficient heap-based shortest path
approach is expected.
"""

from typing import List, Tuple
import heapq


class Solution:
    def minimum_delay_to_stream_k_feeds(
        self,
        n: int,
        edges: List[List[int]],
        gateways: List[int],
        k: int,
    ) -> int:
        """
        Compute the minimum possible maximum shortest-path delay needed to reach
        at least k gateway servers from server 0.

        The algorithm uses Dijkstra's shortest path algorithm because:
        - The graph is weighted
        - All edge weights are positive
        - The graph can be very large and sparse
        - A min-heap gives an efficient implementation

        After shortest distances from server 0 are known, the answer is simply
        the k-th smallest reachable gateway distance. That is because:
        - We may choose any subset of gateways
        - To minimize the maximum chosen delay among at least k gateways,
          we should choose the k gateways with the smallest distances
        - The largest distance among those chosen k gateways is the optimal answer

        Args:
            n: Number of servers in the graph.
            edges: Undirected weighted edges, where each edge is [u, v, w].
            gateways: List of gateway server indices.
            k: Minimum number of gateways that must be reached.

        Returns:
            The minimum possible cost, defined as the smallest achievable maximum
            shortest-path delay among at least k chosen gateways. Returns -1 if
            fewer than k gateways are reachable from server 0.

        Time complexity:
            O((n + m) log n), where m is the number of edges.

        Space complexity:
            O(n + m) for the adjacency list, distance array, heap, and gateway set.
        """
        # Build the adjacency list representation of the graph.
        #
        # Why adjacency list?
        # - The graph is sparse according to the problem statement.
        # - Adjacency lists are memory-efficient for sparse graphs.
        # - Dijkstra's algorithm naturally iterates over neighbors of a node.
        #
        # Each entry graph[u] will contain pairs (v, w), meaning there is an edge
        # from u to v with weight w.
        graph: List[List[Tuple[int, int]]] = [[] for _ in range(n)]
        for u, v, w in edges:
            graph[u].append((v, w))
            graph[v].append((u, w))

        # Convert gateways to a set for O(1) membership checks.
        #
        # This is useful because during Dijkstra's traversal, we want to quickly
        # know whether the node whose shortest distance has just been finalized
        # is one of the gateway nodes.
        gateway_set = set(gateways)

        # Distance array:
        # dist[i] will store the currently known shortest distance from node 0 to i.
        #
        # We initialize all distances to infinity because initially we do not know
        # any path to those nodes. The source node 0 has distance 0 to itself.
        inf = float("inf")
        dist: List[float] = [inf] * n
        dist[0] = 0

        # Min-heap priority queue for Dijkstra's algorithm.
        #
        # Each heap entry is (current_distance, node).
        # The heap always pops the node with the smallest tentative distance.
        heap: List[Tuple[int, int]] = [(0, 0)]

        # This list will store shortest distances to reachable gateway nodes.
        #
        # Important idea:
        # In Dijkstra's algorithm, when a node is popped from the heap and the
        # popped distance matches dist[node], that distance is finalized and is
        # guaranteed to be the true shortest distance.
        #
        # Therefore, if that node is a gateway, we can safely record its shortest
        # distance immediately.
        reachable_gateway_distances: List[int] = []

        # Run Dijkstra's algorithm from source node 0.
        while heap:
            current_distance, node = heapq.heappop(heap)

            # Skip stale heap entries.
            #
            # Why can stale entries exist?
            # - A node may be pushed into the heap multiple times if we discover
            #   better paths later.
            # - Only the entry matching dist[node] is the valid current best one.
            if current_distance != dist[node]:
                continue

            # At this moment, current_distance is the finalized shortest distance
            # from node 0 to 'node'.
            #
            # If this node is a gateway, record its shortest distance.
            if node in gateway_set:
                reachable_gateway_distances.append(current_distance)

                # Early stopping optimization:
                #
                # Dijkstra finalizes nodes in non-decreasing order of shortest distance.
                # That means gateway distances are also collected in sorted order.
                #
                # As soon as we have found k reachable gateways, the k-th collected
                # distance is the answer, because:
                # - These are the k smallest reachable gateway distances seen so far
                # - No future finalized node can have a smaller distance
                # - Therefore the current gateway distance is exactly the k-th smallest
                if len(reachable_gateway_distances) == k:
                    return current_distance

            # Relax all outgoing edges from the current node.
            #
            # Relaxation means:
            # - Try going from source -> node -> neighbor
            # - If that path is better than the best known path to neighbor,
            #   update dist[neighbor] and push the new state into the heap
            for neighbor, weight in graph[node]:
                new_distance = current_distance + weight
                if new_distance < dist[neighbor]:
                    dist[neighbor] = new_distance
                    heapq.heappush(heap, (new_distance, neighbor))

        # If we finish Dijkstra and found fewer than k reachable gateways,
        # then it is impossible to stream to at least k gateways.
        return -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 6
    edges1 = [
        [0, 1, 4],
        [1, 2, 3],
        [0, 3, 2],
        [3, 4, 5],
        [4, 5, 1],
        [2, 5, 2],
    ]
    gateways1 = [2, 4, 5]
    k1 = 2
    result1 = solution.minimum_delay_to_stream_k_feeds(n1, edges1, gateways1, k1)
    print(result1)  # Expected: 7

    # Example 2
    n2 = 5
    edges2 = [
        [0, 1, 2],
        [1, 2, 2],
        [3, 4, 1],
    ]
    gateways2 = [2, 3, 4]
    k2 = 2
    result2 = solution.minimum_delay_to_stream_k_feeds(n2, edges2, gateways2, k2)
    print(result2)  # Expected: -1