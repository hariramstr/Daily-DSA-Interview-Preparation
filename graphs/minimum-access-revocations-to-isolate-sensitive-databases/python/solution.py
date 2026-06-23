"""
Title: Minimum Access Revocations to Isolate Sensitive Databases

Problem Description:
A company models internal service-to-service access as an undirected graph with n systems
labeled from 0 to n - 1. Each edge [u, v] means system u can directly communicate with
system v. Some systems host sensitive databases and are listed in an array sensitive.

For compliance, no two sensitive systems are allowed to remain in the same connected
component after access revocations are applied.

In one operation, you may revoke exactly one existing access link (remove one edge from
the graph). Return the minimum number of access links that must be revoked so that every
connected component contains at most one sensitive system.

If a component already contains zero or one sensitive system, it is valid and requires
no changes. If a component contains multiple sensitive systems, you may remove any edges
inside that component to split it into smaller valid components.

Your task is to compute the minimum number of revoked edges needed over the entire graph.

Important observation:
For any connected component:
- Let E be the number of edges in that component.
- Let V be the number of vertices in that component.
- Let k be the number of sensitive vertices in that component.

A connected component can be transformed into any spanning tree by removing exactly
E - (V - 1) cycle edges. Those removals do not disconnect the component "too much";
they only eliminate cycles.

Once the component is a tree, to split one connected tree into k connected components,
we must remove exactly k - 1 edges.

Therefore, for a connected component with k >= 1 sensitive vertices, the minimum number
of removals needed is:

    (E - (V - 1)) + (k - 1) = E - V + k

If k is 0 or 1, the component is already valid, so the answer for that component is 0.

So the total answer is the sum over all connected components:
- 0, if k <= 1
- E - V + k, if k >= 2
"""

from typing import List


class Solution:
    def min_access_revocations(
        self,
        n: int,
        edges: List[List[int]],
        sensitive: List[int],
    ) -> int:
        """
        Compute the minimum number of edge removals required so that every connected
        component contains at most one sensitive node.

        Args:
            n: Number of nodes in the graph, labeled from 0 to n - 1.
            edges: Undirected edges of the graph.
            sensitive: List of sensitive nodes.

        Returns:
            The minimum number of edges that must be removed.

        Time complexity:
            O(n + m), where m = len(edges), because we build the graph once and
            traverse every node and edge a constant number of times.

        Space complexity:
            O(n + m) for the adjacency list, visited array, and sensitive marker array.
        """
        # ---------------------------------------------------------------------
        # STEP 1: Build an adjacency list for the undirected graph.
        #
        # Why adjacency list?
        # - It is the standard efficient representation for sparse graphs.
        # - The constraints allow up to 200,000 nodes and 200,000 edges, so we need
        #   near-linear memory and traversal time.
        # - Each undirected edge [u, v] is stored twice:
        #   once in graph[u] and once in graph[v].
        # ---------------------------------------------------------------------
        graph: List[List[int]] = [[] for _ in range(n)]
        for u, v in edges:
            graph[u].append(v)
            graph[v].append(u)

        # ---------------------------------------------------------------------
        # STEP 2: Mark which nodes are sensitive.
        #
        # We use a boolean array instead of a set because:
        # - Membership checks become O(1) with very small constant factors.
        # - It is memory-efficient enough for n <= 200,000.
        # ---------------------------------------------------------------------
        is_sensitive: List[bool] = [False] * n
        for node in sensitive:
            is_sensitive[node] = True

        # ---------------------------------------------------------------------
        # STEP 3: Traverse each connected component.
        #
        # For each component, we need:
        # - component_vertices: number of vertices V in this component
        # - degree_sum: sum of degrees of all vertices in this component
        #   Since the graph is undirected, number of edges E = degree_sum // 2
        # - sensitive_count: number of sensitive nodes k in this component
        #
        # Then:
        # - If k <= 1, contribution is 0
        # - If k >= 2, contribution is E - V + k
        #
        # We use iterative DFS to avoid recursion depth issues on large graphs.
        # ---------------------------------------------------------------------
        visited: List[bool] = [False] * n
        answer: int = 0

        for start in range(n):
            # If this node was already visited, it belongs to a component we have
            # already processed, so we skip it.
            if visited[start]:
                continue

            # -------------------------------------------------------------
            # Begin a new connected component traversal from 'start'.
            # -------------------------------------------------------------
            stack: List[int] = [start]
            visited[start] = True

            component_vertices: int = 0
            degree_sum: int = 0
            sensitive_count: int = 0

            while stack:
                node = stack.pop()

                # Count this node as part of the current component.
                component_vertices += 1

                # If this node is sensitive, increase the component's sensitive count.
                if is_sensitive[node]:
                    sensitive_count += 1

                # Add this node's degree to the total degree sum.
                # Later, edges = degree_sum // 2 because each undirected edge is
                # counted once from each endpoint.
                degree_sum += len(graph[node])

                # Explore all neighbors.
                for neighbor in graph[node]:
                    if not visited[neighbor]:
                        visited[neighbor] = True
                        stack.append(neighbor)

            # -------------------------------------------------------------
            # After DFS finishes, we know all properties of this component.
            # -------------------------------------------------------------
            component_edges: int = degree_sum // 2

            # -------------------------------------------------------------
            # If the component has 0 or 1 sensitive node, it is already valid.
            # No removals are needed.
            # -------------------------------------------------------------
            if sensitive_count <= 1:
                continue

            # -------------------------------------------------------------
            # Otherwise, the minimum removals for this component are:
            #
            #   component_edges - component_vertices + sensitive_count
            #
            # Why?
            # 1) Remove cycle edges until the component becomes a tree:
            #      component_edges - (component_vertices - 1)
            # 2) A tree with k sensitive nodes needs k - 1 more cuts to split it
            #    into k components, each containing exactly one sensitive node:
            #      sensitive_count - 1
            #
            # Add them:
            #   component_edges - component_vertices + 1 + sensitive_count - 1
            # = component_edges - component_vertices + sensitive_count
            # -------------------------------------------------------------
            answer += component_edges - component_vertices + sensitive_count

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 7
    edges1 = [[0, 1], [1, 2], [1, 3], [3, 4], [3, 5], [5, 6]]
    sensitive1 = [2, 4, 6]
    result1 = solution.min_access_revocations(n1, edges1, sensitive1)
    print("Example 1 Output:", result1)  # Expected: 2

    # Example 2
    n2 = 8
    edges2 = [[0, 1], [1, 2], [2, 0], [2, 3], [4, 5], [5, 6], [6, 7]]
    sensitive2 = [0, 3, 7]
    result2 = solution.min_access_revocations(n2, edges2, sensitive2)
    print("Example 2 Output:", result2)  # Expected: 1