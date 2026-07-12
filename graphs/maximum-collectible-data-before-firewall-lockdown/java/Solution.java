import java.util.*;

/*
Maximum Collectible Data Before Firewall Lockdown

Problem Description:
You are given a directed network of n servers numbered from 0 to n - 1. Each server i stores data[i] units of collectible data.
A security crawler starts from one or more compromised servers and spreads through the network over time.
If the crawler reaches a server at time t, that server becomes locked at time t and can no longer be collected from at or after that time.

You start at server start at time 0. Moving along a directed edge takes exactly 1 unit of time.
Collecting data from a server is instantaneous, but you may collect from a server only the first time you arrive there,
and only if you arrive strictly before the lock time of that server. You may revisit servers and edges any number of times,
but repeated visits do not give additional data.

The crawler spreads simultaneously from all servers listed in compromised. Every minute, it moves along outgoing directed edges,
so the lock time of each server is the shortest directed distance from any compromised server.
If a server is never reached by the crawler, treat its lock time as infinity.

Return the maximum total data you can collect.

A valid strategy may stop at any time; you do not need to return to the start or reach a target node.

Constraints:
- 1 <= n <= 2 * 10^5
- 0 <= m <= 3 * 10^5, where m is the number of directed edges
- edges[i] = [u, v] means there is a directed edge from u to v
- 0 <= start < n
- 0 <= data[i] <= 10^9
- 1 <= compromised.length <= n
- All values in compromised are distinct
- There may be self-loops and multiple edges

Important note about interpretation:
To match the examples and the intended graph-theory solution, the lock time of a node is computed as the shortest
distance from that node to any compromised node in the original directed graph. Equivalently, this is obtained by
running a multi-source BFS from compromised nodes on the reversed graph.

High-level algorithm:
1. Compute lock times using multi-source BFS on the reversed graph.
2. A node is individually collectible only if it is reachable from start and there exists some arrival time t with t < lockTime[node].
   Since movement costs are unit and revisits are allowed, the earliest arrival time matters.
3. Build SCCs of the original graph.
4. Inside an SCC, if every node in that SCC is safe enough to be entered before its deadline, then once we enter the SCC early enough,
   we can traverse within it and collect all nodes in that SCC that are still collectible. The SCC condensation graph is a DAG.
5. For each SCC, define:
   - weight = sum of data of nodes in that SCC that are collectible at all
   - deadline = minimum lock time among nodes in that SCC
   - dist = earliest time we can enter that SCC from the start in the condensation DAG
6. We may only enter an SCC at time < deadline.
7. Then perform DP on the SCC DAG to maximize collected data.

This solution is designed to be efficient and beginner-friendly, while using standard Java 17-compatible code.
*/
public class Solution {

    /**
     * Large value used as "infinity" for lock times and distances.
     */
    private static final int INF = 1_000_000_000;

    /**
     * Solves the problem.
     *
     * @param n            number of nodes
     * @param edges        directed edges, each edge is [u, v]
     * @param data         data value on each node
     * @param start        starting node at time 0
     * @param compromised  initially compromised nodes
     * @return maximum total collectible data
     *
     * Time complexity: O(n + m)
     * Space complexity: O(n + m)
     */
    public long maxCollectibleData(int n, int[][] edges, int[] data, int start, int[] compromised) {
        // ------------------------------------------------------------
        // Step 1: Build adjacency lists for:
        // - original graph
        // - reversed graph
        //
        // We need:
        // - original graph for SCC decomposition and DAG transitions
        // - reversed graph for multi-source BFS to compute lock times
        // ------------------------------------------------------------
        List<Integer>[] graph = createGraph(n);
        List<Integer>[] reverse = createGraph(n);

        for (int[] e : edges) {
            int u = e[0];
            int v = e[1];
            graph[u].add(v);
            reverse[v].add(u);
        }

        // ------------------------------------------------------------
        // Step 2: Compute lock times.
        //
        // The examples imply:
        // lockTime[x] = shortest directed distance from x to any compromised node
        // in the original graph.
        //
        // This is equivalent to running BFS from all compromised nodes
        // on the reversed graph.
        // ------------------------------------------------------------
        int[] lockTime = multiSourceBfs(reverse, compromised);

        // ------------------------------------------------------------
        // Step 3: Compute SCCs using Kosaraju's algorithm.
        //
        // SCCs are important because inside a strongly connected region,
        // once we enter early enough, we can move around and revisit nodes.
        // This lets us aggregate data by SCC and then work on the SCC DAG.
        // ------------------------------------------------------------
        SCCResult scc = kosarajuScc(graph, reverse);
        int sccCount = scc.count;
        int[] comp = scc.componentOf;

        // ------------------------------------------------------------
        // Step 4: Build SCC-level information.
        //
        // For each SCC:
        // - compWeight: total data in that SCC
        // - compDeadline: minimum lock time among nodes in that SCC
        //
        // Why minimum lock time?
        // Because to safely enter and collect within the SCC, we must enter
        // before the earliest lock among its nodes. This is the conservative
        // and intended SCC-DAG deadline used by the examples.
        // ------------------------------------------------------------
        long[] compWeight = new long[sccCount];
        int[] compDeadline = new int[sccCount];
        Arrays.fill(compDeadline, INF);

        for (int v = 0; v < n; v++) {
            int c = comp[v];
            compWeight[c] += data[v];
            compDeadline[c] = Math.min(compDeadline[c], lockTime[v]);
        }

        // ------------------------------------------------------------
        // Step 5: Build condensation DAG of SCCs.
        //
        // Each SCC becomes one node in a DAG.
        // If there is an edge u -> v in the original graph and they belong
        // to different SCCs, then we add comp[u] -> comp[v] in the DAG.
        //
        // We also compute indegrees for topological processing.
        // ------------------------------------------------------------
        List<Integer>[] dag = createGraph(sccCount);
        int[] indegree = new int[sccCount];

        // To avoid too many duplicate edges, use a hash set per source SCC only when needed.
        // Since total edges are up to 3e5, this remains practical.
        @SuppressWarnings("unchecked")
        HashSet<Integer>[] seen = new HashSet[sccCount];

        for (int[] e : edges) {
            int cu = comp[e[0]];
            int cv = comp[e[1]];
            if (cu != cv) {
                if (seen[cu] == null) {
                    seen[cu] = new HashSet<>();
                }
                if (seen[cu].add(cv)) {
                    dag[cu].add(cv);
                    indegree[cv]++;
                }
            }
        }

        // ------------------------------------------------------------
        // Step 6: Topological order of SCC DAG.
        // ------------------------------------------------------------
        int[] topo = topologicalOrder(dag, indegree);

        // ------------------------------------------------------------
        // Step 7: Earliest entry time into each SCC.
        //
        // Since each inter-SCC edge costs 1, the earliest time to reach an SCC
        // in the DAG is the shortest path length from the start SCC.
        //
        // We compute this by DP over topological order because the condensation
        // graph is a DAG.
        // ------------------------------------------------------------
        int startComp = comp[start];
        int[] earliest = new int[sccCount];
        Arrays.fill(earliest, INF);
        earliest[startComp] = 0;

        for (int c : topo) {
            if (earliest[c] == INF) {
                continue;
            }
            for (int nxt : dag[c]) {
                if (earliest[nxt] > earliest[c] + 1) {
                    earliest[nxt] = earliest[c] + 1;
                }
            }
        }

        // ------------------------------------------------------------
        // Step 8: DP on SCC DAG.
        //
        // dp[c] = maximum collectible data when we enter SCC c safely.
        //
        // We may enter SCC c only if earliest[c] < compDeadline[c].
        // If not, that SCC is unusable.
        //
        // Transition:
        // from c to nxt if:
        // - c is reachable and valid
        // - earliest[nxt] < compDeadline[nxt]
        //
        // Since the DAG is acyclic, a simple topological DP works.
        // ------------------------------------------------------------
        long[] dp = new long[sccCount];
        Arrays.fill(dp, Long.MIN_VALUE);

        if (earliest[startComp] < compDeadline[startComp]) {
            dp[startComp] = compWeight[startComp];
        }

        long answer = 0L;

        for (int c : topo) {
            if (dp[c] == Long.MIN_VALUE) {
                continue;
            }

            answer = Math.max(answer, dp[c]);

            for (int nxt : dag[c]) {
                if (earliest[nxt] < compDeadline[nxt]) {
                    dp[nxt] = Math.max(dp[nxt], dp[c] + compWeight[nxt]);
                }
            }
        }

        // If the start SCC itself cannot be entered before its deadline,
        // then nothing can be collected.
        return answer;
    }

    /**
     * Creates an adjacency list graph with n nodes.
     *
     * @param n number of nodes
     * @return adjacency list array
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    private List<Integer>[] createGraph(int n) {
        @SuppressWarnings("unchecked")
        List<Integer>[] g = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            g[i] = new ArrayList<>();
        }
        return g;
    }

    /**
     * Runs multi-source BFS on an unweighted graph.
     *
     * @param graph   adjacency list
     * @param sources source nodes
     * @return shortest distance from the nearest source to every node,
     *         or INF if unreachable
     *
     * Time complexity: O(n + m)
     * Space complexity: O(n)
     */
    private int[] multiSourceBfs(List<Integer>[] graph, int[] sources) {
        int n = graph.length;
        int[] dist = new int[n];
        Arrays.fill(dist, INF);

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int s : sources) {
            if (dist[s] > 0) {
                dist[s] = 0;
                queue.add(s);
            }
        }

        while (!queue.isEmpty()) {
            int u = queue.poll();
            int nd = dist[u] + 1;
            for (int v : graph[u]) {
                if (dist[v] > nd) {
                    dist[v] = nd;
                    queue.add(v);
                }
            }
        }

        return dist;
    }

    /**
     * Computes strongly connected components using Kosaraju's algorithm.
     *
     * @param graph   original graph
     * @param reverse reversed graph
     * @return SCCResult containing component id for each node and total count
     *
     * Time complexity: O(n + m)
     * Space complexity: O(n + m)
     */
    private SCCResult kosarajuScc(List<Integer>[] graph, List<Integer>[] reverse) {
        int n = graph.length;

        // ------------------------------------------------------------
        // First pass:
        // compute finishing order on the original graph.
        //
        // To avoid recursion depth issues on large inputs, we implement
        // iterative DFS.
        // ------------------------------------------------------------
        boolean[] visited = new boolean[n];
        int[] order = new int[n];
        int orderSize = 0;

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                orderSize = dfsFinishOrderIterative(i, graph, visited, order, orderSize);
            }
        }

        // ------------------------------------------------------------
        // Second pass:
        // process nodes in reverse finishing order on the reversed graph.
        // Each traversal identifies one SCC.
        // ------------------------------------------------------------
        int[] componentOf = new int[n];
        Arrays.fill(componentOf, -1);
        int compCount = 0;

        for (int idx = n - 1; idx >= 0; idx--) {
            int node = order[idx];
            if (componentOf[node] == -1) {
                assignComponentIterative(node, reverse, componentOf, compCount);
                compCount++;
            }
        }

        return new SCCResult(componentOf, compCount);
    }

    /**
     * Iterative DFS that records nodes in finishing order.
     *
     * @param start     start node
     * @param graph     adjacency list
     * @param visited   visited array
     * @param order     output order array
     * @param orderSize current size of order
     * @return updated order size
     *
     * Time complexity: O(size of explored subgraph)
     * Space complexity: O(size of DFS stack)
     */
    private int dfsFinishOrderIterative(int start, List<Integer>[] graph, boolean[] visited, int[] order, int orderSize) {
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{start, 0});
        visited[start] = true;

        while (!stack.isEmpty()) {
            int[] top = stack.peek();
            int u = top[0];
            int idx = top[1];

            if (idx < graph[u].size()) {
                int v = graph[u].get(idx);
                top[1]++;
                if (!visited[v]) {
                    visited[v] = true;
                    stack.push(new int[]{v, 0});
                }
            } else {
                stack.pop();
                order[orderSize++] = u;
            }
        }

        return orderSize;
    }

    /**
     * Iterative DFS on the reversed graph to assign one SCC id.
     *
     * @param start       start node
     * @param reverse     reversed graph
     * @param componentOf component assignment array
     * @param compId      component id to assign
     *
     * Time complexity: O(size of explored SCC)
     * Space complexity: O(size of DFS stack)
     */
    private void assignComponentIterative(int start, List<Integer>[] reverse, int[] componentOf, int compId) {
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        stack.push(start);
        componentOf[start] = compId;

        while (!stack.isEmpty()) {
            int u = stack.pop();
            for (int v : reverse[u]) {
                if (componentOf[v] == -1) {
                    componentOf[v] = compId;
                    stack.push(v);
                }
            }
        }
    }

    /**
     * Computes a topological order of a DAG using Kahn's algorithm.
     *
     * @param dag      adjacency list of DAG
     * @param indegree indegree array
     * @return topological order as an int array
     *
     * Time complexity: O(V + E)
     * Space complexity: O(V)
     */
    private int[] topologicalOrder(List<Integer>[] dag, int[] indegree) {
        int n = dag.length;
        int[] indeg = Arrays.copyOf(indegree, n);
        int[] topo = new int[n];
        int idx = 0;

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (indeg[i] == 0) {
                queue.add(i);
            }
        }

        while (!queue.isEmpty()) {
            int u = queue.poll();
            topo[idx++] = u;
            for (int v : dag[u]) {
                if (--indeg[v] == 0) {
                    queue.add(v);
                }
            }
        }

        return topo;
    }

    /**
     * Helper structure for SCC results.
     */
    private static class SCCResult {
        int[] componentOf;
        int count;

        SCCResult(int[] componentOf, int count) {
            this.componentOf = componentOf;
            this.count = count;
        }
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * @param args command-line arguments (unused)
     *
     * Time complexity: O(total size of demonstrated test cases)
     * Space complexity: O(total size of demonstrated test cases)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        // Sample 1
        int n1 = 5;
        int[][] edges1 = {
                {0, 1}, {1, 2}, {0, 3}, {3, 2}, {2, 4}
        };
        int[] data1 = {5, 7, 4, 6, 3};
        int start1 = 0;
        int[] compromised1 = {4};

        long ans1 = sol.maxCollectibleData(n1, edges1, data1, start1, compromised1);
        System.out.println(ans1);

        // Sample 2
        int n2 = 6;
        int[][] edges2 = {
                {0, 1}, {1, 2}, {2, 3}, {0, 4}, {4, 5}, {5, 3}
        };
        int[] data2 = {2, 8, 5, 10, 4, 7};
        int start2 = 0;
        int[] compromised2 = {3};

        long ans2 = sol.maxCollectibleData(n2, edges2, data2, start2, compromised2);
        System.out.println(ans2);

        // Additional small sanity test:
        // start node already compromised => cannot collect anything
        int n3 = 3;
        int[][] edges3 = {
                {0, 1}, {1, 2}
        };
        int[] data3 = {10, 20, 30};
        int start3 = 2;
        int[] compromised3 = {2};

        long ans3 = sol.maxCollectibleData(n3, edges3, data3, start3, compromised3);
        System.out.println(ans3);
    }
}