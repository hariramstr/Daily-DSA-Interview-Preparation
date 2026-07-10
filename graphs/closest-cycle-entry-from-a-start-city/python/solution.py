"""
Title: Closest Cycle Entry from a Start City

Problem Description:
You are given a directed graph representing one-way roads between cities. The graph has
n cities labeled from 0 to n - 1 and m directed roads, where each road [u, v] means
you can travel from city u to city v. You are also given a starting city s.

A city is called a cycle-entry city if it belongs to at least one directed cycle that
is reachable from s. Your task is to return the minimum number of roads needed to travel
from s to any such cycle-entry city. If multiple cycle-entry cities are reachable at the
same minimum distance, return the smallest city index among them. If no directed cycle is
reachable from s, return [-1, -1].

Return the answer as an array [distance, city]. The distance is the length of the shortest
directed path from s to the chosen city.

A city belongs to a directed cycle if there exists a path that starts at that city,
follows one or more directed edges, and returns to the same city.

Constraints:
- 1 <= n <= 100000
- 0 <= m <= 200000
- 0 <= s < n
- 0 <= u, v < n
- There may be self-loops and multiple edges.

Examples:
1)
Input: n = 7, edges = [[0,1],[1,2],[2,3],[3,1],[2,4],[4,5]], s = 0
Output: [1,1]

2)
Input: n = 6, edges = [[0,1],[1,2],[2,4],[4,5],[3,3]], s = 0
Output: [-1,-1]
"""

from collections import deque
from typing import List, Tuple


class Solution:
    def closest_cycle_entry(self, n: int, edges: List[List[int]], s: int) -> List[int]:
        """
        Find the closest city reachable from s that belongs to at least one directed cycle.

        The algorithm works in three major phases:
        1. Build the graph and compute shortest distances from s using BFS over the directed graph.
           This also tells us which nodes are reachable from s.
        2. Restrict attention only to reachable nodes, then compute strongly connected components
           (SCCs) on that reachable subgraph using an iterative version of Kosaraju's algorithm.
        3. Any SCC of size >= 2 is a directed cycle component. Also, an SCC of size 1 is a cycle
           component if that single node has a self-loop. Among all reachable nodes that belong to
           such SCCs, choose the one with minimum BFS distance from s; break ties by smaller index.

        Args:
            n: Number of cities/nodes.
            edges: Directed edges where [u, v] means u -> v.
            s: Starting city.

        Returns:
            A list [distance, city] representing the minimum distance from s to any reachable
            cycle-entry city and the chosen city index. If no reachable directed cycle exists,
            returns [-1, -1].

        Time complexity:
            O(n + m)

        Space complexity:
            O(n + m)
        """
        # ---------------------------------------------------------------------
        # STEP 1: Build both the forward graph and the reverse graph.
        #
        # Why do we need both?
        # - The forward graph is needed for BFS from s to compute shortest distances
        #   and reachability.
        # - The reverse graph is needed by Kosaraju's SCC algorithm in its second pass.
        #
        # We also track self-loops because a single-node SCC is only a cycle if that
        # node has an edge to itself.
        # ---------------------------------------------------------------------
        graph: List[List[int]] = [[] for _ in range(n)]
        reverse_graph: List[List[int]] = [[] for _ in range(n)]
        has_self_loop: List[bool] = [False] * n

        for u, v in edges:
            graph[u].append(v)
            reverse_graph[v].append(u)
            if u == v:
                has_self_loop[u] = True

        # ---------------------------------------------------------------------
        # STEP 2: BFS from the starting city s.
        #
        # Why BFS?
        # In an unweighted graph, BFS gives the shortest number of edges from the
        # start node to every reachable node.
        #
        # What do we store?
        # - dist[node] = shortest path length from s to node, or -1 if unreachable.
        #
        # This phase also tells us exactly which nodes are reachable from s, which is
        # important because the problem only cares about cycles that are reachable.
        # ---------------------------------------------------------------------
        dist: List[int] = [-1] * n
        dist[s] = 0
        queue: deque[int] = deque([s])

        while queue:
            node = queue.popleft()

            # Explore all outgoing neighbors.
            for nei in graph[node]:
                if dist[nei] == -1:
                    dist[nei] = dist[node] + 1
                    queue.append(nei)

        # Build a compact list of reachable nodes for later SCC processing.
        reachable_nodes: List[int] = [node for node in range(n) if dist[node] != -1]

        # Quick exit:
        # If only no nodes are reachable except maybe s and no cycle exists, SCC phase
        # will handle it, but this list is always non-empty because s is reachable from itself.
        # We keep going.

        # ---------------------------------------------------------------------
        # STEP 3: First pass of Kosaraju's algorithm on the reachable subgraph.
        #
        # Goal:
        # Compute nodes in order of finishing times.
        #
        # Important implementation detail:
        # We use an iterative DFS instead of recursive DFS because n can be as large
        # as 100000, and Python recursion depth would be unsafe.
        #
        # We only traverse reachable nodes because unreachable cycles do not matter.
        # ---------------------------------------------------------------------
        visited: List[bool] = [False] * n
        finish_order: List[int] = []

        for start in reachable_nodes:
            if visited[start]:
                continue

            # Stack entries are tuples: (current_node, phase)
            # phase = 0 means "entering node"
            # phase = 1 means "all children processed, now record finish time"
            stack: List[Tuple[int, int]] = [(start, 0)]

            while stack:
                node, phase = stack.pop()

                if phase == 0:
                    if visited[node]:
                        continue

                    visited[node] = True

                    # We will add the node to finish_order only after all reachable
                    # descendants have been processed, so we push a "post-processing"
                    # marker first.
                    stack.append((node, 1))

                    # Then push all unvisited reachable neighbors.
                    # We only care about the reachable subgraph, so we skip neighbors
                    # with dist[nei] == -1.
                    for nei in graph[node]:
                        if dist[nei] != -1 and not visited[nei]:
                            stack.append((nei, 0))
                else:
                    finish_order.append(node)

        # ---------------------------------------------------------------------
        # STEP 4: Second pass of Kosaraju's algorithm on the reverse graph.
        #
        # We process nodes in reverse finishing order. Each DFS in the reverse graph
        # identifies one SCC.
        #
        # For each SCC, we determine whether it represents a directed cycle:
        # - If SCC size >= 2, then every node in that SCC belongs to a directed cycle.
        # - If SCC size == 1, then the single node belongs to a directed cycle only if
        #   it has a self-loop.
        #
        # While processing SCCs, we can directly evaluate candidate answers.
        # ---------------------------------------------------------------------
        visited = [False] * n
        best_distance: int = -1
        best_city: int = -1

        for start in reversed(finish_order):
            if visited[start]:
                continue

            # Collect all nodes in the current SCC.
            component: List[int] = []
            stack = [start]
            visited[start] = True

            while stack:
                node = stack.pop()
                component.append(node)

                for nei in reverse_graph[node]:
                    if dist[nei] != -1 and not visited[nei]:
                        visited[nei] = True
                        stack.append(nei)

            # Determine whether this SCC corresponds to a cycle.
            is_cycle_component: bool = False

            if len(component) >= 2:
                # In a directed graph, any SCC with at least 2 nodes guarantees that
                # every node in it lies on some directed cycle.
                is_cycle_component = True
            else:
                # Single-node SCC: only a cycle if there is a self-loop.
                only_node = component[0]
                if has_self_loop[only_node]:
                    is_cycle_component = True

            if not is_cycle_component:
                continue

            # This SCC contains cycle-entry cities reachable from s.
            # We now compare all nodes in this SCC against the current best answer.
            #
            # Selection rule:
            # 1. Smaller distance from s is better.
            # 2. If distance ties, smaller city index is better.
            for node in component:
                current_distance = dist[node]

                if best_distance == -1 or current_distance < best_distance:
                    best_distance = current_distance
                    best_city = node
                elif current_distance == best_distance and node < best_city:
                    best_city = node

        # ---------------------------------------------------------------------
        # STEP 5: Return the final answer.
        #
        # If no reachable cycle component was found, return [-1, -1].
        # Otherwise return [best_distance, best_city].
        # ---------------------------------------------------------------------
        if best_distance == -1:
            return [-1, -1]

        return [best_distance, best_city]


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # Reachable cycle is 1 -> 2 -> 3 -> 1.
    # Distances from 0:
    # 1 is at distance 1
    # 2 is at distance 2
    # 3 is at distance 3
    # So the answer should be [1, 1].
    n1 = 7
    edges1 = [[0, 1], [1, 2], [2, 3], [3, 1], [2, 4], [4, 5]]
    s1 = 0
    print(solution.closest_cycle_entry(n1, edges1, s1))  # Expected: [1, 1]

    # Example 2:
    # Node 3 has a self-loop, but it is not reachable from 0.
    # Therefore no reachable cycle exists from the start node.
    # The answer should be [-1, -1].
    n2 = 6
    edges2 = [[0, 1], [1, 2], [2, 4], [4, 5], [3, 3]]
    s2 = 0
    print(solution.closest_cycle_entry(n2, edges2, s2))  # Expected: [-1, -1]

    # Additional quick sanity check:
    # Start node itself has a self-loop, so it is a reachable cycle-entry city
    # at distance 0.
    n3 = 3
    edges3 = [[0, 0], [0, 1], [1, 2]]
    s3 = 0
    print(solution.closest_cycle_entry(n3, edges3, s3))  # Expected: [0, 0]