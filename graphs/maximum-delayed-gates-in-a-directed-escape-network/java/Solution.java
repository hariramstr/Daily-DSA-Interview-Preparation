import java.util.*;

/*
Problem Title: Maximum Delayed Gates in a Directed Escape Network

Problem Description:
You are given a directed graph with n junctions numbered from 0 to n - 1 and m one-way corridors.
Junction 0 is the control center, and junction n - 1 is the only exit. Every corridor [u, v]
means a traveler standing at u may move to v.

Before an evacuation starts, you may permanently lock some junctions (except 0 and n - 1).
A locked junction cannot be entered or used. However, the network must remain valid after locking:
for every junction that is still unlocked, if it is reachable from junction 0 in the remaining graph,
then it must also be able to reach junction n - 1 in the remaining graph. In other words, after your
locks are applied, there cannot exist any "dead-end reachable region" from the control center.

Your task is to return the maximum number of junctions you can lock while preserving this property.

This is not simply asking whether 0 can still reach n - 1. Some reachable nodes may become invalid
because they can no longer reach the exit, and such a configuration is not allowed unless those nodes
are also locked or become unreachable from 0.

Constraints:
- 2 <= n <= 200000
- 1 <= m <= 300000
- 0 <= u, v < n
- Self-loops and parallel edges may appear
- You may not lock junction 0 or junction n - 1

Example 1:
Input: n = 6, edges = [[0,1],[1,2],[2,5],[0,3],[3,4]]
Output: 2

Example 2:
Input: n = 8, edges = [[0,1],[1,2],[2,7],[0,3],[3,2],[1,4],[4,5],[5,4],[5,6],[6,7]]
Output: 3
*/

public class Solution {

    /**
     * Solves the problem and returns the maximum number of junctions that can be locked.
     *
     * Core idea:
     * We want to keep some unlocked set U containing 0 and n-1 such that:
     * 1) Every node in U reachable from 0 inside U can still reach n-1 inside U.
     * 2) We maximize locked nodes, equivalently minimize the number of unlocked nodes.
     *
     * A crucial observation:
     * In any valid remaining graph, every unlocked node that is reachable from 0 must lie on
     * some directed path from 0 to n-1 in the remaining graph.
     *
     * Therefore, among all valid choices, the minimum possible number of reachable unlocked nodes
     * is exactly the minimum number of original vertices needed to preserve at least one path
     * from 0 to n-1. Any other unlocked nodes can simply be locked.
     *
     * Since strongly connected components (SCCs) are all-or-nothing on a path in the condensation DAG,
     * we compress the graph into SCCs. Then the problem becomes:
     * find a path in the SCC DAG from component(0) to component(n-1) minimizing the total number
     * of original vertices on that path.
     *
     * Why this is correct:
     * - If we keep exactly the vertices of one such SCC-path unlocked, then every reachable unlocked
     *   vertex lies on that path and can continue to the exit.
     * - Any valid solution must contain at least one 0->(n-1) path in the SCC DAG, and if a component
     *   on that path is used, all vertices inside that SCC must remain unlocked because from 0 one can
     *   reach that SCC and inside an SCC vertices mutually reach each other; locking only part of an SCC
     *   can create reachable dead ends. The minimum valid reachable set is therefore a minimum-weight
     *   path in the condensation DAG, where each SCC weight is its size.
     *
     * @param n the number of junctions
     * @param edges directed corridors, where each element is [u, v]
     * @return the maximum number of junctions that can be locked
     * Time complexity: O(n + m)
     * Space complexity: O(n + m)
     */
    public int maximumDelayedGates(int n, int[][] edges) {
        GraphData graph = buildGraph(n, edges);

        SCCResult scc = computeSCCsIterativeKosaraju(n, graph.head, graph.to, graph.next, graph.rHead, graph.rTo, graph.rNext);

        int compCount = scc.componentCount;
        int startComp = scc.comp[0];
        int endComp = scc.comp[n - 1];

        if (startComp == endComp) {
            // If 0 and n-1 are in the same SCC, we can keep only that SCC unlocked.
            // Every vertex in that SCC is reachable from 0 and can reach n-1.
            // All other vertices can be locked.
            return n - scc.compSize[startComp];
        }

        DAGData dag = buildCondensationDAG(compCount, scc.comp, edges, scc.compSize);

        long[] dp = minWeightPathInDAG(compCount, dag.head, dag.to, dag.next, dag.indegree, dag.weight, startComp);

        long minUnlocked = dp[endComp];

        // The problem guarantees a graph, but not necessarily that exit is reachable in the original graph.
        // If no path exists from 0 to n-1 at all, then any reachable unlocked node from 0 must still reach n-1.
        // Since 0 itself is always unlocked and reachable from itself, this would be impossible.
        // Under standard interpretation, such an instance has no valid locking configuration.
        // To keep the method total and safe, if unreachable, return 0.
        if (minUnlocked >= INF / 2) {
            return 0;
        }

        return (int) (n - minUnlocked);
    }

    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Builds forward and reverse adjacency lists using compact array-based linked lists.
     *
     * This representation is memory-efficient and fast for large sparse graphs.
     *
     * @param n number of vertices
     * @param edges directed edges
     * @return graph data containing forward and reverse adjacency structures
     * Time complexity: O(n + m)
     * Space complexity: O(n + m)
     */
    public GraphData buildGraph(int n, int[][] edges) {
        int m = edges.length;

        int[] head = new int[n];
        int[] to = new int[m];
        int[] next = new int[m];

        int[] rHead = new int[n];
        int[] rTo = new int[m];
        int[] rNext = new int[m];

        Arrays.fill(head, -1);
        Arrays.fill(rHead, -1);

        for (int i = 0; i < m; i++) {
            int u = edges[i][0];
            int v = edges[i][1];

            to[i] = v;
            next[i] = head[u];
            head[u] = i;

            rTo[i] = u;
            rNext[i] = rHead[v];
            rHead[v] = i;
        }

        return new GraphData(head, to, next, rHead, rTo, rNext);
    }

    /**
     * Computes strongly connected components using an iterative version of Kosaraju's algorithm.
     *
     * Why iterative?
     * The graph can have up to 200000 vertices, so recursive DFS may overflow the Java stack.
     * This implementation uses explicit stacks instead.
     *
     * Step 1:
     * Run DFS on the original graph and record vertices in finishing order.
     *
     * Step 2:
     * Process vertices in reverse finishing order on the reversed graph.
     * Each traversal identifies one SCC.
     *
     * @param n number of vertices
     * @param head forward adjacency head
     * @param to forward adjacency destination
     * @param next forward adjacency next-edge links
     * @param rHead reverse adjacency head
     * @param rTo reverse adjacency destination
     * @param rNext reverse adjacency next-edge links
     * @return SCC decomposition result
     * Time complexity: O(n + m)
     * Space complexity: O(n + m)
     */
    public SCCResult computeSCCsIterativeKosaraju(
            int n,
            int[] head, int[] to, int[] next,
            int[] rHead, int[] rTo, int[] rNext) {

        boolean[] visited = new boolean[n];
        int[] order = new int[n];
        int orderSize = 0;

        int[] stackNode = new int[n];
        int[] stackEdge = new int[n];

        // First pass: finishing order on original graph.
        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }

            int top = 0;
            stackNode[top] = start;
            stackEdge[top] = head[start];
            visited[start] = true;

            while (top >= 0) {
                int u = stackNode[top];
                int e = stackEdge[top];

                if (e == -1) {
                    // All outgoing edges processed, so u is finished.
                    order[orderSize++] = u;
                    top--;
                    continue;
                }

                // Advance the iterator for the current stack frame.
                stackEdge[top] = next[e];
                int v = to[e];

                if (!visited[v]) {
                    visited[v] = true;
                    top++;
                    stackNode[top] = v;
                    stackEdge[top] = head[v];
                }
            }
        }

        // Second pass: reverse graph, in reverse finishing order.
        int[] comp = new int[n];
        Arrays.fill(comp, -1);
        int[] compSize = new int[n];
        int componentCount = 0;

        int[] stack = new int[n];

        for (int i = orderSize - 1; i >= 0; i--) {
            int start = order[i];
            if (comp[start] != -1) {
                continue;
            }

            int size = 0;
            int top = 0;
            stack[top] = start;
            comp[start] = componentCount;

            while (top >= 0) {
                int u = stack[top--];
                size++;

                for (int e = rHead[u]; e != -1; e = rNext[e]) {
                    int v = rTo[e];
                    if (comp[v] == -1) {
                        comp[v] = componentCount;
                        stack[++top] = v;
                    }
                }
            }

            compSize[componentCount] = size;
            componentCount++;
        }

        return new SCCResult(comp, compSize, componentCount);
    }

    /**
     * Builds the condensation DAG of SCCs.
     *
     * Each SCC becomes one node.
     * For every original edge u -> v with comp[u] != comp[v], we add an edge comp[u] -> comp[v].
     *
     * Parallel edges between SCCs do not affect correctness of the DP, but they do affect indegrees.
     * That is still fine for topological processing because Kahn's algorithm works with duplicate edges.
     *
     * @param compCount number of SCCs
     * @param comp component id for each original vertex
     * @param edges original graph edges
     * @param compSize size of each SCC, used as node weight in the DAG
     * @return condensation DAG data
     * Time complexity: O(m + compCount)
     * Space complexity: O(m + compCount)
     */
    public DAGData buildCondensationDAG(int compCount, int[] comp, int[][] edges, int[] compSize) {
        int m = edges.length;

        int[] head = new int[compCount];
        int[] to = new int[m];
        int[] next = new int[m];
        int[] indegree = new int[compCount];
        int[] weight = new int[compCount];

        Arrays.fill(head, -1);

        for (int c = 0; c < compCount; c++) {
            weight[c] = compSize[c];
        }

        int idx = 0;
        for (int[] edge : edges) {
            int cu = comp[edge[0]];
            int cv = comp[edge[1]];
            if (cu == cv) {
                continue;
            }

            to[idx] = cv;
            next[idx] = head[cu];
            head[cu] = idx;
            indegree[cv]++;
            idx++;
        }

        return new DAGData(head, to, next, indegree, weight, idx);
    }

    /**
     * Finds the minimum total vertex-weight path from a start component to all components in a DAG.
     *
     * Since the condensation graph is a DAG, we can process nodes in topological order.
     * Let dp[c] = minimum total number of original vertices needed to reach component c
     * along some path starting from startComp.
     *
     * Transition:
     * dp[next] = min(dp[next], dp[cur] + weight[next])
     *
     * Initialization:
     * dp[startComp] = weight[startComp]
     *
     * @param compCount number of DAG nodes
     * @param head adjacency head of DAG
     * @param to adjacency destination of DAG
     * @param next adjacency next-edge links of DAG
     * @param indegree indegree of each DAG node
     * @param weight weight of each DAG node
     * @param startComp source component
     * @return dp array of minimum path weights from startComp
     * Time complexity: O(compCount + number of DAG edges)
     * Space complexity: O(compCount)
     */
    public long[] minWeightPathInDAG(
            int compCount,
            int[] head, int[] to, int[] next,
            int[] indegree, int[] weight,
            int startComp) {

        long[] dp = new long[compCount];
        Arrays.fill(dp, INF);
        dp[startComp] = weight[startComp];

        int[] indeg = Arrays.copyOf(indegree, indegree.length);
        int[] queue = new int[compCount];
        int ql = 0, qr = 0;

        for (int i = 0; i < compCount; i++) {
            if (indeg[i] == 0) {
                queue[qr++] = i;
            }
        }

        while (ql < qr) {
            int u = queue[ql++];

            for (int e = head[u]; e != -1; e = next[e]) {
                int v = to[e];

                // Relax the path if u is reachable from startComp.
                if (dp[u] < INF / 2) {
                    long candidate = dp[u] + weight[v];
                    if (candidate < dp[v]) {
                        dp[v] = candidate;
                    }
                }

                indeg[v]--;
                if (indeg[v] == 0) {
                    queue[qr++] = v;
                }
            }
        }

        return dp;
    }

    /**
     * Demonstrates the solution on the sample inputs from the statement.
     *
     * @param args command-line arguments, unused
     * @return nothing
     * Time complexity: O(total n + total m) for the shown examples
     * Space complexity: O(total n + total m) for the shown examples
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        int n1 = 6;
        int[][] edges1 = {
                {0, 1}, {1, 2}, {2, 5}, {0, 3}, {3, 4}
        };
        System.out.println(sol.maximumDelayedGates(n1, edges1)); // Expected: 2

        int n2 = 8;
        int[][] edges2 = {
                {0, 1}, {1, 2}, {2, 7}, {0, 3}, {3, 2},
                {1, 4}, {4, 5}, {5, 4}, {5, 6}, {6, 7}
        };
        System.out.println(sol.maximumDelayedGates(n2, edges2)); // Expected: 3
    }

    /**
     * Simple container for the original graph and its reverse graph.
     */
    public static class GraphData {
        int[] head;
        int[] to;
        int[] next;
        int[] rHead;
        int[] rTo;
        int[] rNext;

        GraphData(int[] head, int[] to, int[] next, int[] rHead, int[] rTo, int[] rNext) {
            this.head = head;
            this.to = to;
            this.next = next;
            this.rHead = rHead;
            this.rTo = rTo;
            this.rNext = rNext;
        }
    }

    /**
     * Container for SCC decomposition results.
     */
    public static class SCCResult {
        int[] comp;
        int[] compSize;
        int componentCount;

        SCCResult(int[] comp, int[] compSize, int componentCount) {
            this.comp = comp;
            this.compSize = compSize;
            this.componentCount = componentCount;
        }
    }

    /**
     * Container for condensation DAG data.
     */
    public static class DAGData {
        int[] head;
        int[] to;
        int[] next;
        int[] indegree;
        int[] weight;
        int edgeCount;

        DAGData(int[] head, int[] to, int[] next, int[] indegree, int[] weight, int edgeCount) {
            this.head = head;
            this.to = to;
            this.next = next;
            this.indegree = indegree;
            this.weight = weight;
            this.edgeCount = edgeCount;
        }
    }
}