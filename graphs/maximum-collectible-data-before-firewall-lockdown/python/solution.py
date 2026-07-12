# Maximum Collectible Data Before Firewall Lockdown
#
# Problem Summary:
# We have a directed graph of servers. Each server stores some amount of data.
# We start at a given server at time 0. Moving along one directed edge costs 1 time unit.
# We may collect data from a server only once, on the first visit, and only if we arrive
# strictly before that server's lock time.
#
# The lock times are determined by a security crawler that starts simultaneously from
# all compromised servers and spreads along directed edges, one edge per minute.
# Therefore, the lock time of a server is the shortest directed distance from any
# compromised server to that server. If unreachable from all compromised servers,
# its lock time is infinity.
#
# We may revisit nodes and edges arbitrarily, but repeated visits do not give more data.
# We may stop anywhere at any time.
#
# Goal:
# Return the maximum total data collectible.
#
# Key Insight:
# A server can only be collected if there exists some arrival time t such that:
#   shortest_time_from_start_to_server <= t < lock_time[server]
#
# Because waiting is never helpful and revisits only increase time, the earliest possible
# arrival time to a server is its shortest-path distance from the start. If that earliest
# arrival is already too late, the server can never be collected.
#
# Furthermore, even though revisits are allowed, every collected server must lie on some
# directed walk starting from the start. The set of collectible servers is exactly the set
# of servers reachable from the start whose shortest-path distance from the start is
# strictly smaller than their lock time. Any server outside this set can never be collected.
#
# Once we restrict to collectible servers, we still must respect directed movement.
# Strongly connected components (SCCs) matter because inside one SCC, after entering it
# early enough, we can traverse among its collectible servers as needed. After condensing
# SCCs into a DAG, the problem becomes finding a maximum-weight path in that DAG, where
# each SCC contributes the sum of collectible data inside it.
#
# This yields an efficient and correct solution:
# 1) Compute lock times by multi-source BFS from compromised servers.
# 2) Compute shortest distances from start by BFS.
# 3) Mark servers collectible iff dist_start[u] < lock_time[u].
# 4) Build SCCs on the subgraph induced by collectible servers.
# 5) Condense SCCs into a DAG.
# 6) Run DP for maximum path sum starting from the SCC containing start (if start collectible).
#
# This solution is linear in the graph size.

from collections import deque
from typing import List, Tuple


class Solution:
    def maximum_collectible_data(
        self,
        n: int,
        edges: List[List[int]],
        data: List[int],
        start: int,
        compromised: List[int],
    ) -> int:
        """
        Compute the maximum total collectible data before servers lock.

        Args:
            n: Number of servers.
            edges: Directed edges [u, v].
            data: Data value stored at each server.
            start: Starting server at time 0.
            compromised: Initial crawler source servers.

        Returns:
            Maximum total collectible data.

        Time complexity:
            O(n + m), where m = len(edges)

        Space complexity:
            O(n + m)
        """
        # ---------------------------------------------------------------------
        # STEP 1: Build adjacency list for the directed graph.
        #
        # We need the graph for:
        # - BFS from compromised servers to compute lock times
        # - BFS from start to compute earliest arrival times
        # - SCC decomposition on the collectible subgraph
        #
        # We also build the reverse graph because Kosaraju's SCC algorithm needs it.
        # ---------------------------------------------------------------------
        graph: List[List[int]] = [[] for _ in range(n)]
        reverse_graph: List[List[int]] = [[] for _ in range(n)]

        for u, v in edges:
            graph[u].append(v)
            reverse_graph[v].append(u)

        # ---------------------------------------------------------------------
        # STEP 2: Compute lock times using multi-source BFS.
        #
        # Since every edge has weight 1 and the crawler starts from all compromised
        # nodes simultaneously, the lock time of each node is simply the shortest
        # directed distance from any compromised node.
        #
        # If a node is never reached, its lock time remains INF.
        # ---------------------------------------------------------------------
        INF: int = 10**18
        lock_time: List[int] = [INF] * n
        queue: deque[int] = deque()

        for node in compromised:
            lock_time[node] = 0
            queue.append(node)

        while queue:
            u = queue.popleft()
            next_time = lock_time[u] + 1
            for v in graph[u]:
                if lock_time[v] == INF:
                    lock_time[v] = next_time
                    queue.append(v)

        # ---------------------------------------------------------------------
        # STEP 3: Compute earliest arrival times from the start using BFS.
        #
        # Again, edges all cost 1, so BFS gives shortest path distances.
        #
        # The earliest arrival time is the best possible time at which we can first
        # reach each server. If even this earliest arrival is not strictly before
        # the lock time, then that server can never be collected.
        # ---------------------------------------------------------------------
        dist_start: List[int] = [INF] * n
        dist_start[start] = 0
        queue.clear()
        queue.append(start)

        while queue:
            u = queue.popleft()
            next_dist = dist_start[u] + 1
            for v in graph[u]:
                if dist_start[v] == INF:
                    dist_start[v] = next_dist
                    queue.append(v)

        # ---------------------------------------------------------------------
        # STEP 4: Determine which servers are collectible.
        #
        # A server is collectible iff:
        # - it is reachable from start, and
        # - earliest arrival time < lock time
        #
        # This strict inequality is crucial because the statement says the server
        # becomes locked at time t and can no longer be collected at or after t.
        # So arriving exactly at lock time is too late.
        # ---------------------------------------------------------------------
        collectible: List[bool] = [False] * n
        for u in range(n):
            if dist_start[u] < lock_time[u]:
                collectible[u] = True

        # If the start itself is already locked at time 0, we cannot collect anything.
        if not collectible[start]:
            return 0

        # ---------------------------------------------------------------------
        # STEP 5: Run Kosaraju's algorithm on the subgraph induced by collectible nodes.
        #
        # Why SCCs?
        # Inside a strongly connected component, every node can reach every other node.
        # Once we enter such a component while all its nodes are collectible under the
        # earliest-arrival criterion, we can treat the whole SCC as a single "bundle"
        # of collectible value. Then movement between SCCs forms a DAG.
        #
        # Kosaraju:
        #   1) DFS order on original graph
        #   2) DFS on reversed graph in reverse finishing order
        #
        # We only traverse collectible nodes and edges between collectible nodes.
        # ---------------------------------------------------------------------
        visited: List[bool] = [False] * n
        order: List[int] = []

        def dfs1_iterative(src: int) -> None:
            """
            First pass of Kosaraju: compute finishing order iteratively.

            Args:
                src: Starting node.

            Returns:
                None

            Time complexity:
                O(size of explored subgraph)

            Space complexity:
                O(stack size)
            """
            stack: List[Tuple[int, int]] = [(src, 0)]
            visited[src] = True

            while stack:
                u, idx = stack[-1]
                if idx < len(graph[u]):
                    v = graph[u][idx]
                    stack[-1] = (u, idx + 1)
                    if collectible[v] and not visited[v]:
                        visited[v] = True
                        stack.append((v, 0))
                else:
                    order.append(u)
                    stack.pop()

        for u in range(n):
            if collectible[u] and not visited[u]:
                dfs1_iterative(u)

        comp_id: List[int] = [-1] * n
        comp_weights: List[int] = []

        def dfs2_iterative(src: int, cid: int) -> int:
            """
            Second pass of Kosaraju: assign component id and sum data in the SCC.

            Args:
                src: Starting node in reversed graph.
                cid: Component id to assign.

            Returns:
                Total data weight of this SCC.

            Time complexity:
                O(size of explored SCC)

            Space complexity:
                O(stack size)
            """
            stack: List[int] = [src]
            comp_id[src] = cid
            total = 0

            while stack:
                u = stack.pop()
                total += data[u]
                for v in reverse_graph[u]:
                    if collectible[v] and comp_id[v] == -1:
                        comp_id[v] = cid
                        stack.append(v)

            return total

        component_count = 0
        for u in reversed(order):
            if comp_id[u] == -1:
                weight = dfs2_iterative(u, component_count)
                comp_weights.append(weight)
                component_count += 1

        # ---------------------------------------------------------------------
        # STEP 6: Build the condensed DAG of SCCs.
        #
        # Each SCC becomes one node with weight = sum of data in that SCC.
        # For every original edge u -> v where both endpoints are collectible and
        # belong to different SCCs, we add an edge comp[u] -> comp[v].
        #
        # The condensed graph is always a DAG.
        # ---------------------------------------------------------------------
        dag: List[List[int]] = [[] for _ in range(component_count)]
        indegree: List[int] = [0] * component_count

        # To avoid duplicate edges causing unnecessary work, we sort-and-unique
        # logically by using a temporary list of pairs and then deduplicating.
        # Since total edges are large, we use a set of tuples once here.
        dag_edges = set()

        for u, v in edges:
            if collectible[u] and collectible[v]:
                cu = comp_id[u]
                cv = comp_id[v]
                if cu != cv and (cu, cv) not in dag_edges:
                    dag_edges.add((cu, cv))
                    dag[cu].append(cv)
                    indegree[cv] += 1

        # ---------------------------------------------------------------------
        # STEP 7: Topological DP on the condensed DAG.
        #
        # We want the maximum collectible data along any directed path starting
        # from the SCC containing the start node.
        #
        # Let dp[c] = maximum data collectable upon reaching component c.
        # Transition:
        #   dp[next] = max(dp[next], dp[cur] + comp_weights[next])
        #
        # Since the graph is a DAG, we process nodes in topological order.
        # ---------------------------------------------------------------------
        start_comp = comp_id[start]
        topo_queue: deque[int] = deque()

        for c in range(component_count):
            if indegree[c] == 0:
                topo_queue.append(c)

        topo_order: List[int] = []
        while topo_queue:
            c = topo_queue.popleft()
            topo_order.append(c)
            for nxt in dag[c]:
                indegree[nxt] -= 1
                if indegree[nxt] == 0:
                    topo_queue.append(nxt)

        NEG_INF = -10**30
        dp: List[int] = [NEG_INF] * component_count
        dp[start_comp] = comp_weights[start_comp]

        for c in topo_order:
            if dp[c] == NEG_INF:
                continue
            current_value = dp[c]
            for nxt in dag[c]:
                candidate = current_value + comp_weights[nxt]
                if candidate > dp[nxt]:
                    dp[nxt] = candidate

        # The answer is the best value among all reachable SCCs.
        answer = 0
        for value in dp:
            if value > answer:
                answer = value

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 5
    edges1 = [[0, 1], [1, 2], [0, 3], [3, 2], [2, 4]]
    data1 = [5, 7, 4, 6, 3]
    start1 = 0
    compromised1 = [4]
    result1 = solution.maximum_collectible_data(n1, edges1, data1, start1, compromised1)
    print(result1)

    # Example 2
    n2 = 6
    edges2 = [[0, 1], [1, 2], [2, 3], [0, 4], [4, 5], [5, 3]]
    data2 = [2, 8, 5, 10, 4, 7]
    start2 = 0
    compromised2 = [3]
    result2 = solution.maximum_collectible_data(n2, edges2, data2, start2, compromised2)
    print(result2)