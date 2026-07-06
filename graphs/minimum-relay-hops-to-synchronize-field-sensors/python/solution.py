"""
Title: Minimum Relay Hops to Synchronize Field Sensors

Problem Description:
A company deploys wireless sensors across a large farm. Each sensor can directly relay
data to some other sensors, forming a directed graph of communication links. Sensor 0
is the central base station. A sensor is considered synchronized if it can send a
message to sensor 0 by following one or more directed relay links.

You are given an integer n and a list edges where edges[i] = [u, v] means sensor u
can directly send data to sensor v. You may install additional one-way relay boosters.
Installing a booster from sensor a to sensor b creates a new directed edge a -> b.
Your goal is to make every sensor synchronized using the minimum number of new boosters.

Return the minimum number of boosters required.

Important details:
- You may choose any pair of sensors when adding a booster.
- Existing edges remain unchanged.
- A sensor already synchronized does not need any extra work.
- If several unsynchronized regions exist, it may be optimal to connect one region to
  another before reaching sensor 0.

Constraints:
- 1 <= n <= 200000
- 0 <= edges.length <= 300000
- 0 <= u, v < n
- u != v
- Multiple edges between the same pair may appear
"""

from typing import List


class Solution:
    def min_boosters(self, n: int, edges: List[List[int]]) -> int:
        """
        Compute the minimum number of new directed edges needed so every node can reach node 0.

        The key idea is:
        1. Compress the graph into Strongly Connected Components (SCCs).
           Inside one SCC, every node can already reach every other node.
        2. Build the SCC condensation graph, which is a DAG.
        3. We only care about SCCs that currently cannot reach the SCC containing node 0.
        4. In the reversed condensation graph, count how many "source" SCCs exist among
           those bad SCCs. Each such source requires one new booster, and that count is minimal.

        Args:
            n: Number of sensors/nodes.
            edges: Directed edges where [u, v] means u -> v.

        Returns:
            Minimum number of boosters required.

        Time complexity:
            O(n + m), where m = len(edges)

        Space complexity:
            O(n + m)
        """
        # -----------------------------
        # Step 1: Build adjacency lists
        # -----------------------------
        # We need:
        # - graph: original directed graph
        # - rev_graph: reversed graph
        #
        # Why both?
        # Kosaraju's SCC algorithm performs:
        # 1) a DFS on the original graph to compute finishing order
        # 2) a DFS on the reversed graph in reverse finishing order to assign SCCs
        graph: List[List[int]] = [[] for _ in range(n)]
        rev_graph: List[List[int]] = [[] for _ in range(n)]

        for u, v in edges:
            graph[u].append(v)
            rev_graph[v].append(u)

        # ---------------------------------------------------------
        # Step 2: First pass of Kosaraju - compute finishing order
        # ---------------------------------------------------------
        # We use an iterative DFS instead of recursion because n can be as large as 200000.
        # Python recursion would be unsafe here due to recursion depth limits.
        visited: List[bool] = [False] * n
        order: List[int] = []

        def dfs_finish(start: int) -> None:
            """
            Iterative DFS that records nodes in postorder (finishing order).

            Args:
                start: Starting node for DFS.

            Returns:
                None

            Time complexity:
                O(size of explored subgraph)

            Space complexity:
                O(size of DFS stack)
            """
            # Stack entries are (node, state)
            # state = 0 means "enter node"
            # state = 1 means "exit node" after processing children
            stack: List[tuple[int, int]] = [(start, 0)]

            while stack:
                node, state = stack.pop()

                if state == 0:
                    if visited[node]:
                        continue

                    visited[node] = True

                    # Push exit action first, so it happens after all descendants.
                    stack.append((node, 1))

                    # Visit outgoing neighbors.
                    for nei in graph[node]:
                        if not visited[nei]:
                            stack.append((nei, 0))
                else:
                    # When we exit a node, all reachable descendants from this DFS path
                    # have already been handled, so we append it to finishing order.
                    order.append(node)

        for node in range(n):
            if not visited[node]:
                dfs_finish(node)

        # ---------------------------------------------------------
        # Step 3: Second pass of Kosaraju - assign SCC identifiers
        # ---------------------------------------------------------
        # We process nodes in reverse finishing order on the reversed graph.
        # Each DFS in this pass discovers exactly one SCC.
        scc_id: List[int] = [-1] * n
        scc_count = 0

        def dfs_assign(start: int, comp_id: int) -> None:
            """
            Iterative DFS on the reversed graph to assign all nodes in one SCC.

            Args:
                start: Starting node.
                comp_id: SCC id to assign.

            Returns:
                None

            Time complexity:
                O(size of explored SCC region)

            Space complexity:
                O(size of DFS stack)
            """
            stack: List[int] = [start]
            scc_id[start] = comp_id

            while stack:
                node = stack.pop()
                for nei in rev_graph[node]:
                    if scc_id[nei] == -1:
                        scc_id[nei] = comp_id
                        stack.append(nei)

        for node in reversed(order):
            if scc_id[node] == -1:
                dfs_assign(node, scc_count)
                scc_count += 1

        # If all nodes are in one SCC, then every node can reach every other node,
        # including node 0, so no boosters are needed.
        if scc_count == 1:
            return 0

        # ---------------------------------------------------------
        # Step 4: Build SCC condensation graph information
        # ---------------------------------------------------------
        # The condensation graph has one node per SCC.
        # There is an edge A -> B if some original edge goes from a node in SCC A
        # to a node in SCC B, with A != B.
        #
        # We do NOT need to store the full condensation graph explicitly in both directions.
        # For the final counting logic, we need:
        # - reverse adjacency between SCCs
        # - indegree within the "bad SCC subgraph" later
        #
        # But first, we need to know which SCC can reach SCC(0).
        # A component can reach SCC(0) in the original condensation graph iff SCC(0)
        # can reach it in the reversed condensation graph.
        rev_scc_graph: List[List[int]] = [[] for _ in range(scc_count)]
        zero_scc = scc_id[0]

        for u, v in edges:
            cu = scc_id[u]
            cv = scc_id[v]
            if cu != cv:
                # Original condensation edge: cu -> cv
                # Reversed condensation edge: cv -> cu
                rev_scc_graph[cv].append(cu)

        # ----------------------------------------------------------------
        # Step 5: Mark all SCCs that CAN reach the SCC containing node 0
        # ----------------------------------------------------------------
        # Why reversed SCC graph?
        # If X can reach zero_scc in the original condensation graph,
        # then zero_scc can reach X in the reversed condensation graph.
        #
        # So a graph traversal from zero_scc on rev_scc_graph marks all "good" SCCs.
        can_reach_zero: List[bool] = [False] * scc_count
        stack_scc: List[int] = [zero_scc]
        can_reach_zero[zero_scc] = True

        while stack_scc:
            comp = stack_scc.pop()
            for prev_comp in rev_scc_graph[comp]:
                if not can_reach_zero[prev_comp]:
                    can_reach_zero[prev_comp] = True
                    stack_scc.append(prev_comp)

        # If every SCC can already reach zero_scc, then every original node can reach 0.
        if all(can_reach_zero):
            return 0

        # -------------------------------------------------------------------------
        # Step 6: Count source SCCs among the "bad" SCCs in the reversed SCC subgraph
        # -------------------------------------------------------------------------
        # Let BAD = SCCs that cannot reach zero_scc.
        #
        # We need the minimum number of new edges to make every BAD SCC eventually reach zero.
        #
        # Important insight:
        # In the original condensation DAG restricted to BAD SCCs, the answer is the number
        # of sink SCCs (outdegree 0 within BAD).
        #
        # Equivalently, in the REVERSED condensation DAG restricted to BAD SCCs, the answer
        # is the number of source SCCs (indegree 0 within BAD).
        #
        # We already built reversed SCC edges, so we count indegrees within BAD there.
        #
        # Why is this correct?
        # - Each source in the reversed BAD graph corresponds to a sink in the original BAD graph.
        # - A single new edge from such a sink to any good SCC (for example directly to SCC(0))
        #   makes that sink, and all BAD SCCs that can reach it, become good.
        # - Different source components in the reversed BAD graph are disconnected from each other
        #   by incoming BAD edges, so each needs its own booster.
        bad_indegree_in_reversed: List[int] = [0] * scc_count

        for to_comp in range(scc_count):
            if can_reach_zero[to_comp]:
                continue
            for from_comp in rev_scc_graph[to_comp]:
                if not can_reach_zero[from_comp]:
                    bad_indegree_in_reversed[from_comp] += 1

        answer = 0
        for comp in range(scc_count):
            if not can_reach_zero[comp] and bad_indegree_in_reversed[comp] == 0:
                answer += 1

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 6
    edges1 = [[1, 0], [2, 1], [3, 4], [4, 5]]
    result1 = solution.min_boosters(n1, edges1)
    print(result1)  # Expected: 1

    # Example 2
    n2 = 7
    edges2 = [[1, 2], [2, 3], [3, 1], [4, 5], [5, 6]]
    result2 = solution.min_boosters(n2, edges2)
    print(result2)  # Expected: 2

    # Additional quick checks
    n3 = 1
    edges3: List[List[int]] = []
    result3 = solution.min_boosters(n3, edges3)
    print(result3)  # Expected: 0

    n4 = 4
    edges4 = [[1, 0], [2, 0], [3, 0]]
    result4 = solution.min_boosters(n4, edges4)
    print(result4)  # Expected: 0