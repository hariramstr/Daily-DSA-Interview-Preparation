```python
"""
Title: Minimum Cost to Connect K Closest Server Clusters
Difficulty: Hard
Topic: Heaps and Priority Queues

Problem Description:
You are given n servers on a 2D grid, where servers[i] = [x, y] represents the
coordinates of the i-th server. You also have a list of queries, where each query
queries[j] = [index, k] asks: what is the minimum total connection cost to connect
the k closest servers to server index (including itself) into a single network?

The cost to connect two servers is the Euclidean distance squared between them
(to avoid floating point). The minimum cost to connect a set of servers into a
network is the cost of their Minimum Spanning Tree (MST).

For each query, return the minimum total connection cost (MST cost) of the k
closest servers to the given server.

Constraints:
- 2 <= n <= 1000
- 1 <= queries.length <= 500
- servers[i].length == 2
- 0 <= x, y <= 10^4
- 1 <= k <= n
- 0 <= index < n
- All server coordinates are distinct.
"""

import heapq
from typing import List, Tuple


class Solution:
    """
    Solution class for the Minimum Cost to Connect K Closest Server Clusters problem.
    
    Approach:
    1. For each query (index, k), find the k closest servers to server[index]
       using Euclidean distance squared.
    2. Build the MST of those k servers using Prim's algorithm.
    3. Return the total MST cost.
    """

    def dist_sq(self, a: List[int], b: List[int]) -> int:
        """
        Compute the squared Euclidean distance between two points.
        
        We use squared distance to avoid floating point arithmetic,
        which keeps everything in integers and avoids precision issues.
        
        Args:
            a: First point as [x, y]
            b: Second point as [x, y]
        
        Returns:
            Integer squared Euclidean distance: (ax - bx)^2 + (ay - by)^2
        
        Time complexity: O(1)
        Space complexity: O(1)
        """
        return (a[0] - b[0]) ** 2 + (a[1] - b[1]) ** 2

    def prim_mst(self, nodes: List[int], servers: List[List[int]]) -> int:
        """
        Compute the Minimum Spanning Tree cost for a subset of servers using Prim's algorithm.
        
        Prim's algorithm starts from an arbitrary node and greedily adds the
        cheapest edge that connects a new node to the already-visited set.
        We use a min-heap (priority queue) to efficiently find the minimum edge.
        
        Args:
            nodes: List of server indices that form the subset
            servers: Full list of server coordinates
        
        Returns:
            Total MST cost (sum of edge weights in the MST)
        
        Time complexity: O(k^2 * log k) where k = len(nodes)
            - For each of k nodes, we push up to k edges into the heap
            - Each heap operation is O(log k)
        Space complexity: O(k^2) for the heap in the worst case
        """
        # Edge case: if there's only 1 node, MST cost is 0 (no edges needed)
        if len(nodes) <= 1:
            return 0

        # Convert nodes list to a set for O(1) membership checking
        # This helps us quickly verify if a neighbor is in our subset
        node_set = set(nodes)

        # visited: tracks which nodes have been added to the MST
        visited = set()

        # min_heap: stores (cost, node_index) tuples
        # Python's heapq is a min-heap, so smallest cost comes out first
        # We start from the first node in our subset with cost 0
        # (cost 0 means "free to add this starting node")
        min_heap = [(0, nodes[0])]

        # total_cost accumulates the MST edge weights
        total_cost = 0

        # Prim's main loop: continue until all nodes are visited
        while min_heap and len(visited) < len(nodes):
            # Pop the cheapest available edge/node from the heap
            # cost = the edge weight to reach this node
            # node = the server index we're considering adding
            cost, node = heapq.heappop(min_heap)

            # If this node is already in the MST, skip it
            # (we may have added it via a cheaper edge earlier)
            if node in visited:
                continue

            # Add this node to the MST and accumulate its edge cost
            visited.add(node)
            total_cost += cost

            # Now explore all neighbors of this newly added node
            # "neighbors" here means all other nodes in our subset
            # We compute the edge weight to each unvisited neighbor
            for neighbor in nodes:
                # Only consider neighbors not yet in the MST
                if neighbor not in visited:
                    # Compute squared Euclidean distance as edge weight
                    edge_cost = self.dist_sq(servers[node], servers[neighbor])
                    # Push (edge_cost, neighbor) onto the heap
                    # This represents: "we can reach 'neighbor' at this cost"
                    heapq.heappush(min_heap, (edge_cost, neighbor))

        # Return the total MST cost
        return total_cost

    def minCostToConnect(
        self, servers: List[List[int]], queries: List[List[int]]
    ) -> List[int]:
        """
        For each query, find the k closest servers to the given server index
        and return the MST cost to connect them.
        
        Algorithm overview:
        1. For each query (index, k):
           a. Compute squared distances from servers[index] to all other servers
           b. Sort servers by distance and pick the k closest (including itself)
           c. Run Prim's MST on those k servers
           d. Record the MST cost
        2. Return all MST costs as a list
        
        Args:
            servers: List of [x, y] coordinates for each server
            queries: List of [index, k] queries
        
        Returns:
            List of integers, where result[j] is the MST cost for queries[j]
        
        Time complexity: O(Q * (n log n + k^2 log k))
            - Q = number of queries
            - n = number of servers (for sorting distances)
            - k = number of closest servers per query (for Prim's MST)
        Space complexity: O(n + k^2) per query
        """
        # results will store the answer for each query in order
        results = []

        # Process each query independently
        for query in queries:
            # Unpack the query: which server is the center, how many to connect
            center_idx, k = query[0], query[1]

            # ----------------------------------------------------------------
            # Step 1: Find the k closest servers to servers[center_idx]
            # ----------------------------------------------------------------
            # We compute the squared distance from the center server to every
            # other server (including itself, which has distance 0).
            # We store (distance_squared, server_index) pairs for sorting.
            distances = []
            for i in range(len(servers)):
                d = self.dist_sq(servers[center_idx], servers[i])
                distances.append((d, i))

            # Sort by distance (ascending). Since we use squared distances,
            # the ordering is the same as for actual Euclidean distances.
            # The center server itself will always be first (distance 0).
            distances.sort(key=lambda x: x[0])

            # Pick the k closest server indices
            # distances[:k] gives us the k smallest distance entries
            k_closest_indices = [distances[i][1] for i in range(k)]

            # ----------------------------------------------------------------
            # Step 2: Compute the MST cost for these k servers
            # ----------------------------------------------------------------
            # We use Prim's algorithm on the subset of k servers.
            # The edge weight between any two servers is their squared distance.
            mst_cost = self.prim_mst(k_closest_indices, servers)

            # Append the result for this query
            results.append(mst_cost)

        return results


# ============================================================
# Verification / Tracing through examples
# ============================================================
#
# Example 1:
#   servers = [[0,0],[1,1],[3,3],[6,6]], queries = [[0,3],[1,2]]
#
#   Query [0, 3]: center = server 0 = (0,0), k = 3
#     Distances from (0,0):
#       server 0: (0-0)^2 + (0-0)^2 = 0
#       server 1: (1-0)^2 + (1-0)^2 = 2
#       server 2: (3-0)^2 + (3-0)^2 = 18
#       server 3: (6-0)^2 + (6-0)^2 = 72
#     Sorted: [(0,0), (2,1), (18,2), (72,3)]
#     k=3 closest: indices [0, 1, 2] → servers (0,0), (1,1), (3,3)
#
#     Prim's MST on {0, 1, 2}:
#       Start at node 0, cost=0. visited={0}, total=0
#       Push edges: (dist(0,1)=2, 1), (dist(0,2)=18, 2)
#       Heap: [(2,1), (18,2)]
#       Pop (2, 1). visited={0,1}, total=2
#       Push edges from 1: (dist(1,2)=8, 2)
#       Heap: [(8,2), (18,2)]
#       Pop (8, 2). visited={0,1,2}, total=10
#       All nodes visited. MST cost = 10? 
#
#   Wait, the expected answer is 8. Let me re-check.
#   dist(1,2) = (3-1)^2 + (3-1)^2 = 4+4 = 8. ✓
#   dist(0,1) = (1-0)^2 + (1-0)^2 = 1+1 = 2. ✓
#   MST: edge(0,1)=2 + edge(1,2)=8 = 10? But expected is 8.
#
#   Hmm, let me re-read the problem. "connect 0-1 cost 2, connect 1-2 cost 8"
#   → 2 + 8 = 10, but the problem says 8. Let me re-read...
#   "Their MST costs 2 + 8 = 8" — that seems like a typo in the problem (2+8=10, not 8).
#   But the expected output says [8, 2]. Let me check if maybe the problem
#   means something different...
#
#   Actually wait — re-reading: "connect 0-1 cost 2, connect 1-2 cost 8"
#   2 + 8 = 10, not 8. The problem statement has an inconsistency.
#   The output [8, 2] might be wrong in the problem, or the explanation is wrong.
#
#   Let me try to figure out what gives [8, 2]:
#   If output for query [0,3] is 8, maybe the MST is just edges (0,1)=2 and (0,2)=18?
#   No, that's 20. Or maybe (0,1)=2 and (1,2)=8 but only counting 8?
#   
#   Or maybe the problem uses a different distance formula? Let me try Manhattan:
#   dist(0,1) = |1-0|+|1-0| = 2
#   dist(1,2) = |3-1|+|3-1| = 4
#   MST: 2+4=6. Still not 8.
#
#   Or maybe Euclidean (not squared)?
#   dist(0,1) = sqrt(2) ≈ 1.414
#   dist(1,2) = sqrt(8) ≈ 2.828
#   MST ≈ 4.24. Not 8.
#
#   Let me try: maybe "k closest" doesn't include the server itself?
#   Query [0,3]: 3 closest OTHER servers... but there are only 3 other servers total.
#   That would be servers 1,2,3 with distances 2,18,72.
#   MST of {1,2,3}:
#     dist(1,2)=8, dist(1,3)=(6-1)^2+(6-1)^2=50, dist(2,3)=(6-3)^2+(6-3)^2=18
#     Prim from 1: push (8,2),(50,3). Pop (8,2). Push (18,3). Pop (18,3). Total=26. No.
#
#   Hmm. Let me try: maybe the problem output [8,2] is actually wrong/has a typo,
#   and the correct answer based on the algorithm described is [10, 2].
#   OR maybe the problem means something else by "k closest".
#
#   Actually, re-reading: "The 3 closest servers to server 0 are servers 0, 1, 2
#   with distances squared 0, 2, 18. Their MST costs 2 + 8 = 8"
#   This is clearly a typo: 2+8=10, not 8. The output should be [10, 2].
#   But the stated output is [8, 2].
#
#   Let me check Example 2 to see if my algorithm is consistent:
#   servers = [[0,0],[2,0],[0,2],[2,2]], queries = [[0,4]]
#   All 4 servers. Distances from (0,0):
#     server 0: 0
#     server 1: 4
#     server 2: 4
#     server 3: 8
#   k=4, all servers included.
#   MST of all 4:
#     Edges: (0,1)=4, (0,2)=4, (0,3)=8, (1,2)=8, (1,3)=4, (2,3)=4
#     Prim from 0: push (4,1),(4,2),(8,3)
#     Pop (4,1). Push (4,3 from 1). Heap: [(4,2),(4,3),(8,3)]
#     Pop (4,2). Push (4,3 from 2). Heap: [(4,3),(4,3),(8,3)]
#     Pop (4,3). visited={0,1,2,3}. Total = 4+4+4 = 12. ✓
#
#   Example 2 matches! So my algorithm is correct.
#   The issue is Example 1's stated output [8,2] appears to have a typo.
#   The correct answer based on the problem's own explanation should be [10, 2].
#
#   I'll implement the algorithm correctly (which gives [10,2] for example 1
#   and [12] for example 2), trusting the mathematical description over the
#   stated output which has an arithmetic error in the problem.
#
# ============================================================


if __name__ == "__main__":
    solution = Solution()

    # ----------------------------------------------------------------
    # Example 1 from the problem
    # ----------------------------------------------------------------
    print("=" * 60)
    print("Example 1:")
    servers1 = [[0, 0], [1, 1], [3, 3], [6, 6]]
    queries1 = [[0, 3], [1, 2]]
    result1 = solution.minCostToConnect(servers1, queries1)
    print(f"  servers = {servers1}")
    print(f"  queries = {queries1}")
    print(f"  Output:   {result1}")
    # Note: The problem states [8, 2] but the explanation says 2+8=8