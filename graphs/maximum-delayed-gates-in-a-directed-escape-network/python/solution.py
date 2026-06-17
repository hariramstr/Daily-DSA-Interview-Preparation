"""
Title: Maximum Delayed Gates in a Directed Escape Network

Problem Description:
You are given a directed graph with n junctions numbered from 0 to n - 1 and m one-way
corridors. Junction 0 is the control center, and junction n - 1 is the only exit.
Every corridor [u, v] means a traveler standing at u may move to v.

Before an evacuation starts, you may permanently lock some junctions (except 0 and n - 1).
A locked junction cannot be entered or used. However, the network must remain valid after
locking: for every junction that is still unlocked, if it is reachable from junction 0 in
the remaining graph, then it must also be able to reach junction n - 1 in the remaining
graph. In other words, after your locks are applied, there cannot exist any "dead-end
reachable region" from the control center.

Your task is to return the maximum number of junctions you can lock while preserving this
property.

This is not simply asking whether 0 can still reach n - 1. Some reachable nodes may become
invalid because they can no longer reach the exit, and such a configuration is not allowed
unless those nodes are also locked or become unreachable from 0.

Constraints:
- 2 <= n <= 200000
- 1 <= m <= 300000
- 0 <= u, v < n
- Self-loops and parallel edges may appear
- You may not lock junction 0 or junction n - 1

Key idea used in this solution:
- Compress the graph into strongly connected components (SCCs).
- The SCC condensation graph is a DAG.
- We want to keep as few vertices unlocked as possible, while still ensuring:
  1) 0 can reach n - 1 in the remaining graph,
  2) every reachable unlocked vertex can still reach n - 1.
- In the SCC DAG, this means we want the smallest possible vertex set that:
  - contains the SCC of 0 and the SCC of n - 1,
  - is closed under "reachable nodes must still reach the sink SCC",
  - and preserves a path from source SCC to sink SCC.
- In a DAG, the smallest such reachable-valid subgraph is exactly one directed path from
  source SCC to sink SCC.
- Therefore, the answer is:
      n - (minimum number of original vertices on any SCC-DAG path from SCC(0) to SCC(n-1)))
- The path cost is the sum of SCC sizes along the path.
"""

from collections import deque
from typing import List, Tuple


class Solution:
    def maximumDelayedGates(self, n: int, edges: List[List[int]]) -> int:
        """
        Compute the maximum number of junctions that can be locked.

        The algorithm:
        1. Build the directed graph and its reverse.
        2. Compute strongly connected components using iterative Kosaraju.
        3. Build the SCC condensation DAG.
        4. Find the minimum total number of original vertices on any path from SCC(0)
           to SCC(n - 1) in that DAG.
        5. Keep exactly those vertices on such a minimum-cost path and lock all others.

        Args:
            n: Number of junctions.
            edges: Directed edges [u, v].

        Returns:
            Maximum number of junctions that can be locked.

        Time complexity:
            O(n + m)

        Space complexity:
            O(n + m)
        """
        graph, reverse_graph = self._build_graphs(n, edges)
        comp_id, comp_sizes, comp_count = self._kosaraju_scc(n, graph, reverse_graph)

        source_comp = comp_id[0]
        sink_comp = comp_id[n - 1]

        # If source and sink are inside the same SCC, then keeping only that SCC is enough.
        # Every vertex inside one SCC can reach every other vertex inside that SCC.
        # So the minimum kept set is simply the size of that SCC.
        if source_comp == sink_comp:
            return n - comp_sizes[source_comp]

        dag, indegree = self._build_condensation_dag(comp_count, edges, comp_id)
        min_kept = self._min_path_cost_in_dag(
            comp_count,
            dag,
            indegree,
            comp_sizes,
            source_comp,
            sink_comp,
        )
        return n - min_kept

    def _build_graphs(self, n: int, edges: List[List[int]]) -> Tuple[List[List[int]], List[List[int]]]:
        """
        Build forward and reverse adjacency lists.

        Args:
            n: Number of vertices.
            edges: Directed edges.

        Returns:
            A tuple (graph, reverse_graph).

        Time complexity:
            O(n + m)

        Space complexity:
            O(n + m)
        """
        graph: List[List[int]] = [[] for _ in range(n)]
        reverse_graph: List[List[int]] = [[] for _ in range(n)]

        for u, v in edges:
            graph[u].append(v)
            reverse_graph[v].append(u)

        return graph, reverse_graph

    def _kosaraju_scc(
        self,
        n: int,
        graph: List[List[int]],
        reverse_graph: List[List[int]],
    ) -> Tuple[List[int], List[int], int]:
        """
        Compute SCCs using an iterative version of Kosaraju's algorithm.

        First pass:
            Run DFS on the original graph and record vertices in finishing order.
        Second pass:
            Process vertices in reverse finishing order on the reversed graph.
            Each DFS tree in this pass is one SCC.

        Args:
            n: Number of vertices.
            graph: Forward adjacency list.
            reverse_graph: Reverse adjacency list.

        Returns:
            A tuple:
            - comp_id: component id for each vertex
            - comp_sizes: size of each component
            - comp_count: total number of components

        Time complexity:
            O(n + m)

        Space complexity:
            O(n + m)
        """
        visited: List[bool] = [False] * n
        order: List[int] = []

        # -----------------------------
        # First pass: finishing order
        # -----------------------------
        # We use an explicit stack instead of recursion because n can be as large as 200000,
        # and Python recursion depth would not be safe here.
        for start in range(n):
            if visited[start]:
                continue

            # Stack entries are (node, next_child_index_to_process).
            stack: List[Tuple[int, int]] = [(start, 0)]
            visited[start] = True

            while stack:
                node, idx = stack[-1]

                if idx < len(graph[node]):
                    nxt = graph[node][idx]
                    stack[-1] = (node, idx + 1)
                    if not visited[nxt]:
                        visited[nxt] = True
                        stack.append((nxt, 0))
                else:
                    # All outgoing neighbors processed, so this node is finished.
                    order.append(node)
                    stack.pop()

        # -----------------------------
        # Second pass: assign SCC ids
        # -----------------------------
        comp_id: List[int] = [-1] * n
        comp_sizes: List[int] = []
        comp_count = 0

        # Process in reverse finishing order.
        for start in reversed(order):
            if comp_id[start] != -1:
                continue

            size = 0
            stack: List[int] = [start]
            comp_id[start] = comp_count

            while stack:
                node = stack.pop()
                size += 1

                for prev in reverse_graph[node]:
                    if comp_id[prev] == -1:
                        comp_id[prev] = comp_count
                        stack.append(prev)

            comp_sizes.append(size)
            comp_count += 1

        return comp_id, comp_sizes, comp_count

    def _build_condensation_dag(
        self,
        comp_count: int,
        edges: List[List[int]],
        comp_id: List[int],
    ) -> Tuple[List[List[int]], List[int]]:
        """
        Build the SCC condensation DAG.

        Each SCC becomes one node.
        For every original edge u -> v with comp_id[u] != comp_id[v],
        add an edge comp_id[u] -> comp_id[v].

        We also compute indegrees for topological processing.

        Args:
            comp_count: Number of SCCs.
            edges: Original directed edges.
            comp_id: SCC id of each original vertex.

        Returns:
            A tuple (dag, indegree).

        Time complexity:
            O(m)

        Space complexity:
            O(comp_count + number_of_condensation_edges)
        """
        # We use sets temporarily to avoid duplicate DAG edges caused by:
        # - parallel edges in the original graph
        # - multiple original edges crossing the same pair of SCCs
        dag_sets: List[set[int]] = [set() for _ in range(comp_count)]
        indegree: List[int] = [0] * comp_count

        for u, v in edges:
            cu = comp_id[u]
            cv = comp_id[v]
            if cu != cv and cv not in dag_sets[cu]:
                dag_sets[cu].add(cv)
                indegree[cv] += 1

        dag: List[List[int]] = [list(neighbors) for neighbors in dag_sets]
        return dag, indegree

    def _min_path_cost_in_dag(
        self,
        comp_count: int,
        dag: List[List[int]],
        indegree: List[int],
        comp_sizes: List[int],
        source_comp: int,
        sink_comp: int,
    ) -> int:
        """
        Find the minimum sum of SCC sizes along any path from source_comp to sink_comp
        in the condensation DAG.

        Since the condensation graph is a DAG, we can do dynamic programming in
        topological order:
            dp[v] = minimum cost to reach component v from source_comp

        Transition:
            dp[to] = min(dp[to], dp[cur] + comp_sizes[to])

        Args:
            comp_count: Number of SCCs.
            dag: Condensation DAG adjacency list.
            indegree: Indegree of each DAG node.
            comp_sizes: Number of original vertices in each SCC.
            source_comp: SCC containing vertex 0.
            sink_comp: SCC containing vertex n - 1.

        Returns:
            Minimum path cost from source_comp to sink_comp.

        Time complexity:
            O(comp_count + number_of_condensation_edges)

        Space complexity:
            O(comp_count)
        """
        # Standard Kahn topological sort.
        queue: deque[int] = deque()
        indeg = indegree[:]  # Work on a copy so the caller's data remains unchanged.

        for node in range(comp_count):
            if indeg[node] == 0:
                queue.append(node)

        topological_order: List[int] = []
        while queue:
            node = queue.popleft()
            topological_order.append(node)
            for nxt in dag[node]:
                indeg[nxt] -= 1
                if indeg[nxt] == 0:
                    queue.append(nxt)

        # Large sentinel value for "unreachable".
        inf = 10**30
        dp: List[int] = [inf] * comp_count
        dp[source_comp] = comp_sizes[source_comp]

        # Process nodes in topological order so every predecessor is handled first.
        for node in topological_order:
            if dp[node] == inf:
                continue

            # Relax outgoing edges.
            for nxt in dag[node]:
                candidate = dp[node] + comp_sizes[nxt]
                if candidate < dp[nxt]:
                    dp[nxt] = candidate

        return dp[sink_comp]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 6
    edges1 = [[0, 1], [1, 2], [2, 5], [0, 3], [3, 4]]
    result1 = solution.maximumDelayedGates(n1, edges1)
    print(result1)  # Expected: 2

    # Example 2
    n2 = 8
    edges2 = [[0, 1], [1, 2], [2, 7], [0, 3], [3, 2], [1, 4], [4, 5], [5, 4], [5, 6], [6, 7]]
    result2 = solution.maximumDelayedGates(n2, edges2)
    print(result2)  # Expected: 3