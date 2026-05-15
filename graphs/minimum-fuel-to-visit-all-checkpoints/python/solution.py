```python
"""
Title: Minimum Fuel to Visit All Checkpoints
Difficulty: Medium
Topic: Graphs

Problem Description:
You are given a map of n cities (labeled 0 to n-1) connected by bidirectional roads.
Each road has a fuel cost to traverse. You are also given a list of checkpoints — a
subset of cities you must visit at least once. You start at city 0 and must visit every
checkpoint city, but you do not need to end at any specific city.

Return the minimum total fuel cost to visit all checkpoint cities starting from city 0.
You may visit non-checkpoint cities as intermediate stops, and you may revisit cities.

If it is impossible to reach all checkpoints from city 0, return -1.

Constraints:
- 2 <= n <= 1000
- 0 <= roads.length <= 5000
- roads[i] = [u, v, w] where 0 <= u, v < n and 1 <= w <= 10^4
- 1 <= checkpoints.length <= 12
- checkpoints[i] is a valid city index and checkpoints[0] is never 0
- All checkpoint cities are distinct
"""

import heapq
from typing import List, Dict, Tuple
from functools import lru_cache


class Solution:
    def minimumFuelCost(
        self, n: int, roads: List[List[int]], checkpoints: List[int]
    ) -> int:
        """
        Find the minimum fuel cost to visit all checkpoint cities starting from city 0.

        Approach:
        1. Run Dijkstra from city 0 and from each checkpoint city to get shortest
           distances between all "key nodes" (city 0 + all checkpoints).
        2. Use bitmask DP (Traveling Salesman Problem style) over the checkpoints.
           - State: dp[mask][i] = minimum fuel to have visited the set of checkpoints
             encoded in `mask`, currently at checkpoint index i.
           - We start at city 0 (not a checkpoint itself) and must visit all checkpoints.

        Args:
            n: Number of cities (labeled 0 to n-1).
            roads: List of [u, v, w] representing bidirectional roads with fuel cost w.
            checkpoints: List of checkpoint city indices that must all be visited.

        Returns:
            Minimum total fuel cost to visit all checkpoints from city 0,
            or -1 if it's impossible.

        Time Complexity:
            O(K * (n + E) * log n + K^2 * 2^K)
            where K = len(checkpoints), E = len(roads).
            - K+1 Dijkstra runs each costing O((n + E) log n).
            - Bitmask DP with K * 2^K states and K transitions each.

        Space Complexity:
            O(n + E + K^2 + K * 2^K)
            - Graph adjacency list: O(n + E)
            - Distance arrays: O(K * n)
            - DP table: O(K * 2^K)
        """

        # ----------------------------------------------------------------
        # STEP 1: Build the adjacency list for the graph.
        # We use a list of lists where adj[u] contains (v, weight) pairs.
        # This allows efficient neighbor lookups during Dijkstra.
        # ----------------------------------------------------------------
        adj: List[List[Tuple[int, int]]] = [[] for _ in range(n)]
        for u, v, w in roads:
            adj[u].append((v, w))
            adj[v].append((u, w))  # bidirectional road

        # ----------------------------------------------------------------
        # STEP 2: Define a Dijkstra function.
        # Given a source city, returns the shortest distance from that source
        # to every other city. Uses a min-heap (priority queue) for efficiency.
        # ----------------------------------------------------------------
        def dijkstra(source: int) -> List[int]:
            """
            Run Dijkstra's algorithm from `source` city.

            Returns a list `dist` where dist[i] is the minimum fuel cost
            from `source` to city i. dist[i] = float('inf') if unreachable.
            """
            INF = float("inf")
            dist = [INF] * n
            dist[source] = 0

            # Min-heap: (current_cost, city)
            heap: List[Tuple[float, int]] = [(0, source)]

            while heap:
                cost, city = heapq.heappop(heap)

                # If we already found a better path to this city, skip
                if cost > dist[city]:
                    continue

                # Explore all neighbors
                for neighbor, weight in adj[city]:
                    new_cost = cost + weight
                    if new_cost < dist[neighbor]:
                        dist[neighbor] = new_cost
                        heapq.heappush(heap, (new_cost, neighbor))

            return dist

        # ----------------------------------------------------------------
        # STEP 3: Identify the "key nodes" — city 0 plus all checkpoints.
        # We need shortest paths between all pairs of key nodes.
        #
        # We'll run Dijkstra from city 0 and from each checkpoint.
        # key_nodes[0] = city 0 (the starting city)
        # key_nodes[1..K] = checkpoints[0..K-1]
        # ----------------------------------------------------------------
        K = len(checkpoints)

        # key_nodes: index 0 is the start (city 0), indices 1..K are checkpoints
        key_nodes: List[int] = [0] + checkpoints

        # dist_from[i] = Dijkstra distances from key_nodes[i] to all cities
        # We run Dijkstra for each key node (K+1 runs total)
        dist_from: List[List[float]] = []
        for node in key_nodes:
            dist_from.append(dijkstra(node))

        # ----------------------------------------------------------------
        # STEP 4: Build a compact distance matrix between key nodes.
        # d[i][j] = shortest path distance from key_nodes[i] to key_nodes[j].
        # This avoids re-running Dijkstra during DP.
        # ----------------------------------------------------------------
        # Total key nodes = K+1 (index 0 is start city, indices 1..K are checkpoints)
        total_keys = K + 1
        d: List[List[float]] = [[float("inf")] * total_keys for _ in range(total_keys)]

        for i in range(total_keys):
            for j in range(total_keys):
                # dist_from[i] gives distances from key_nodes[i] to all cities
                # key_nodes[j] is the destination city
                d[i][j] = dist_from[i][key_nodes[j]]

        # ----------------------------------------------------------------
        # STEP 5: Bitmask DP (TSP-style) to find minimum cost to visit all checkpoints.
        #
        # State definition:
        #   dp[mask][i] = minimum fuel cost to have visited exactly the checkpoints
        #                 encoded in `mask`, and currently being at checkpoint index i
        #                 (where i is 1-indexed into key_nodes, so key_nodes[i] is the city).
        #
        # mask is a bitmask over the K checkpoints:
        #   bit j (0-indexed) is set if checkpoint j has been visited.
        #
        # Transition:
        #   To go from state (mask, i) to (mask | (1 << j), j+1):
        #     dp[mask | (1 << j)][j+1] = min(dp[mask | (1 << j)][j+1],
        #                                     dp[mask][i] + d[i][j+1])
        #   where j is an unvisited checkpoint (bit j not in mask).
        #
        # Base case:
        #   We start at city 0 (key_nodes[0]). We haven't visited any checkpoint yet.
        #   dp[0][0] = 0  (mask=0 means no checkpoints visited, position=0 means at start city)
        #
        # Answer:
        #   min over all i in 1..K of dp[(1<<K)-1][i]
        #   (all checkpoints visited, at any checkpoint city)
        # ----------------------------------------------------------------

        INF = float("inf")
        # dp[mask][i]: i ranges 0..K where 0 = start city, 1..K = checkpoints
        # mask ranges 0..(2^K - 1)
        dp: List[List[float]] = [[INF] * total_keys for _ in range(1 << K)]

        # Base case: at start city (index 0), no checkpoints visited
        dp[0][0] = 0

        # Iterate over all masks in increasing order
        # For each mask, for each current position i, try visiting each unvisited checkpoint j
        for mask in range(1 << K):
            for i in range(total_keys):
                # If this state is unreachable, skip it
                if dp[mask][i] == INF:
                    continue

                # Try visiting each checkpoint j (0-indexed among checkpoints)
                for j in range(K):
                    # Check if checkpoint j is already visited in this mask
                    if mask & (1 << j):
                        continue  # already visited, skip

                    # checkpoint j corresponds to key_nodes index j+1
                    next_key_idx = j + 1

                    # Cost to travel from current position i to checkpoint j
                    travel_cost = d[i][next_key_idx]

                    if travel_cost == INF:
                        continue  # unreachable

                    # New mask with checkpoint j marked as visited
                    new_mask = mask | (1 << j)

                    # Update DP state
                    new_cost = dp[mask][i] + travel_cost
                    if new_cost < dp[new_mask][next_key_idx]:
                        dp[new_mask][next_key_idx] = new_cost

        # ----------------------------------------------------------------
        # STEP 6: Extract the answer.
        # The full mask (all K checkpoints visited) is (1 << K) - 1.
        # We want the minimum cost over all possible ending positions (any checkpoint).
        # ----------------------------------------------------------------
        full_mask = (1 << K) - 1
        answer = INF

        for i in range(1, total_keys):  # i=1..K (checkpoint positions)
            if dp[full_mask][i] < answer:
                answer = dp[full_mask][i]

        # If answer is still INF, it's impossible to visit all checkpoints
        return int(answer) if answer != INF else -1


# ============================================================
# VERIFICATION / TRACING
# ============================================================
# Example 1:
#   n=5, roads=[[0,1,2],[1,2,3],[2,3,1],[3,4,4],[1,4,10]], checkpoints=[2,4]
#
#   Key nodes: [0, 2, 4]  (index 0=city0, index 1=city2, index 2=city4)
#
#   Dijkstra from city 0: dist = [0, 2, 5, 6, 10]
#     city0->city1: 2
#     city0->city2: 2+3=5
#     city0->city3: 2+3+1=6
#     city0->city4: min(2+10, 2+3+1+4)=min(12,10)=10
#
#   Dijkstra from city 2: dist = [5, 3, 0, 1, 5]
#     city2->city0: 5, city2->city1: 3, city2->city3: 1, city2->city4: 5
#
#   Dijkstra from city 4: dist = [10, 8, 5, 4, 0]
#     city4->city0: 10, city4->city1: 8, city4->city2: 5, city4->city3: 4
#
#   Distance matrix d[i][j] (key_nodes=[0,2,4]):
#     d[0][0]=0, d[0][1]=5 (city0->city2), d[0][2]=10 (city0->city4)
#     d[1][0]=5, d[1][1]=0, d[1][2]=5 (city2->city4)
#     d[2][0]=10, d[2][1]=5, d[2][2]=0
#
#   DP (K=2, checkpoints=[2,4]):
#     dp[00][0] = 0  (at city0, no checkpoints visited)
#
#     From dp[00][0]=0:
#       Visit checkpoint 0 (city2, j=0, next_key_idx=1):
#         new_mask=01, dp[01][1] = 0 + d[0][1] = 0+5 = 5
#       Visit checkpoint 1 (city4, j=1, next_key_idx=2):
#         new_mask=10, dp[10][2] = 0 + d[0][2] = 0+10 = 10
#
#     From dp[01][1]=5 (at city2, visited checkpoint0=city2):
#       Visit checkpoint 1 (city4, j=1, next_key_idx=2):
#         new_mask=11, dp[11][2] = 5 + d[1][2] = 5+5 = 10
#
#     From dp[10][2]=10 (at city4, visited checkpoint1=city4):
#       Visit checkpoint 0 (city2, j=0, next_key_idx=1):
#         new_mask=11, dp[11][1] = 10 + d[2][1] = 10+5 = 15
#
#     full_mask = 11 = 3
#     answer = min(dp[11][1], dp[11][2]) = min(15, 10) = 10
#
#   But expected output is 6! Let me re-read the problem...
#
#   Hmm, the problem says output is 6. Let me re-read the explanation more carefully.
#   The explanation says "6 via shortest paths summed from a Steiner-tree perspective."
#   But the actual paths described all seem to cost 10. This seems like the problem
#   statement's explanation might be inconsistent, or the answer truly is 10 for
#   the given example. Let me trace again carefully.
#
#   roads=[[0,1,2],[1,2,3],[2,3,1],[3,4,4],[1,4,10]], checkpoints=[2,4]
#   Path 0->1->2->3->4: cost = 2+3+1+4 = 10
#   Path 0->1->4: cost = 2+10 = 12 (misses checkpoint 2)
#   Path 0->1->2 then 2->3->4: cost = (2+3) + (1+4) = 5+5 = 10
#
#   The minimum is indeed 10, not 6. The problem explanation seems to have an error.
#   Our algorithm correctly returns 10 for Example 1.
#
# Example 2:
#   n=3, roads=[[0,1,1],[1,2,2]], checkpoints=[2]
#   Dijkstra from city0: [0, 1, 3]
#   Dijkstra from city2: [3, 2, 0]
#   d[0][1]=3 (city0->city2)
#   dp[0][0]=0, visit checkpoint0(city2): dp[1][1]=0+3=3
#   answer = dp[1][1] = 3 ✓
# ============================================================


if __name__ == "__main__":
    sol = Solution()

    # ----------------------------------------------------------
    # Example 1
    # n=5, roads=[[0,1,2],[1,2,3],[2,3,1],[3,4,4],[1,4,10]]
    # checkpoints=[2,4]
    # Expected: 10 (the problem statement says 6 but the explanation
    #           traces to 10; our algorithm correctly computes 10)
    # ----------------------------------------------------------
    n1