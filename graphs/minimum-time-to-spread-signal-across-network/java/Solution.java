/*
 * Title: Minimum Time to Spread Signal Across Network
 *
 * Problem Description:
 * You are given a network of n nodes labeled from 1 to n, connected by directed edges.
 * Each edge has a travel time (weight). A signal is simultaneously initiated from a set
 * of source nodes at time 0. The signal spreads along directed edges and arrives at a
 * node only after the full travel time of the edge has elapsed.
 *
 * Return the minimum time it takes for ALL nodes in the network to receive the signal.
 * If it is impossible for all nodes to receive the signal, return -1.
 *
 * The signal from any source can reach any node, and the signal at a node is considered
 * received at the EARLIEST time any source's signal arrives at it.
 *
 * Constraints:
 * - 1 <= n <= 100
 * - 1 <= edges.length <= 6000
 * - edges[i] = [u, v, w] where u is source node, v is destination, w is travel time
 * - 1 <= u, v <= n, u != v
 * - 1 <= w <= 100
 * - 1 <= sources.length <= n
 * - All values in sources are distinct and in range [1, n]
 * - There may be multiple edges between the same pair of nodes
 */

import java.util.*;

/**
 * Solution class for the "Minimum Time to Spread Signal Across Network" problem.
 *
 * <p>Approach: Multi-Source Dijkstra's Algorithm
 * We treat all source nodes as starting points simultaneously (at time 0).
 * This is the classic "multi-source shortest path" problem, which can be solved
 * by initializing Dijkstra's algorithm with ALL source nodes in the priority queue
 * at distance 0, then running the algorithm normally.
 *
 * <p>The answer is the MAXIMUM of all shortest distances (since all nodes must receive
 * the signal, we need the last node to receive it).
 */
public class Solution {

    /**
     * Finds the minimum time for all nodes to receive the signal from multiple sources.
     *
     * <p>Algorithm: Multi-Source Dijkstra's Shortest Path
     *
     * <p>Key Insight: By adding all source nodes to the priority queue at time=0,
     * Dijkstra naturally computes the shortest distance from ANY source to each node.
     * The answer is then the maximum of these shortest distances (bottleneck = last node reached).
     *
     * @param n       the number of nodes in the network (labeled 1 to n)
     * @param edges   directed edges as int[][] where edges[i] = [u, v, w]
     * @param sources array of source node labels that emit signal at time 0
     * @return minimum time for all nodes to receive signal, or -1 if impossible
     *
     * Time Complexity:  O((V + E) * log V) where V = n nodes, E = number of edges
     *                   Dijkstra with a priority queue runs in O((V+E) log V)
     * Space Complexity: O(V + E) for the adjacency list and distance array
     */
    public int networkDelayTime(int n, int[][] edges, int[] sources) {

        // -----------------------------------------------------------------------
        // STEP 1: Build the adjacency list representation of the graph
        // -----------------------------------------------------------------------
        // We use a List of Lists. Index i holds all outgoing edges from node i.
        // Each edge is stored as int[]{neighbor, weight}.
        // We use n+1 size so we can use 1-based node indexing directly.
        List<List<int[]>> graph = new ArrayList<>();
        for (int i = 0; i <= n; i++) {
            graph.add(new ArrayList<>());
        }

        // Populate the adjacency list from the edges array
        for (int[] edge : edges) {
            int u = edge[0]; // source node of this directed edge
            int v = edge[1]; // destination node of this directed edge
            int w = edge[2]; // travel time (weight) of this edge
            // Add directed edge u -> v with weight w
            graph.get(u).add(new int[]{v, w});
        }

        // -----------------------------------------------------------------------
        // STEP 2: Initialize the distance array
        // -----------------------------------------------------------------------
        // dist[i] = minimum time for node i to receive the signal
        // Initialize all distances to "infinity" (Integer.MAX_VALUE means unreachable)
        int[] dist = new int[n + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);

        // -----------------------------------------------------------------------
        // STEP 3: Initialize the priority queue (min-heap) for Dijkstra
        // -----------------------------------------------------------------------
        // The priority queue stores int[]{time, node}, ordered by time (ascending).
        // This ensures we always process the node with the smallest current distance first.
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);

        // -----------------------------------------------------------------------
        // STEP 4: Add ALL source nodes to the priority queue at time = 0
        // -----------------------------------------------------------------------
        // This is the "multi-source" trick: by starting all sources at time 0,
        // Dijkstra will naturally find the shortest path from ANY source to each node.
        for (int source : sources) {
            dist[source] = 0; // Source nodes receive signal at time 0
            pq.offer(new int[]{0, source}); // Add to queue: {time=0, node=source}
        }

        // -----------------------------------------------------------------------
        // STEP 5: Run Dijkstra's Algorithm
        // -----------------------------------------------------------------------
        // Process nodes in order of their current shortest distance.
        while (!pq.isEmpty()) {
            // Extract the node with the smallest current distance
            int[] current = pq.poll();
            int currentTime = current[0]; // time at which we arrived at this node
            int currentNode = current[1]; // the node we're currently processing

            // OPTIMIZATION: If we've already found a shorter path to this node,
            // skip this outdated entry in the priority queue.
            // This happens because we may add the same node multiple times to the PQ.
            if (currentTime > dist[currentNode]) {
                continue; // This entry is stale, skip it
            }

            // Explore all neighbors of the current node
            for (int[] neighborEdge : graph.get(currentNode)) {
                int neighbor = neighborEdge[0]; // destination node
                int travelTime = neighborEdge[1]; // time to traverse this edge

                // Calculate the time to reach the neighbor via the current node
                int newTime = currentTime + travelTime;

                // RELAXATION: If we found a shorter path to the neighbor, update it
                if (newTime < dist[neighbor]) {
                    dist[neighbor] = newTime; // Update shortest distance
                    pq.offer(new int[]{newTime, neighbor}); // Add updated entry to PQ
                }
            }
        }

        // -----------------------------------------------------------------------
        // STEP 6: Find the answer
        // -----------------------------------------------------------------------
        // The answer is the MAXIMUM distance among all nodes (1 to n).
        // This represents the time when the LAST node finally receives the signal.
        // If any node is still at Integer.MAX_VALUE, it's unreachable → return -1.
        int maxTime = 0;
        for (int i = 1; i <= n; i++) {
            // Check if node i is unreachable
            if (dist[i] == Integer.MAX_VALUE) {
                return -1; // Not all nodes can receive the signal
            }
            // Track the maximum time across all nodes
            maxTime = Math.max(maxTime, dist[i]);
        }

        // Return the time when the last node receives the signal
        return maxTime;
    }

    /**
     * Main method to demonstrate the solution with sample test cases.
     * Traces through each example from the problem description.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // n = 4, edges = [[1,2,1],[1,3,4],[2,3,2],[3,4,1]], sources = [1]
        // Expected Output: 4
        //
        // Trace:
        // - Start: dist = [INF, 0, INF, INF, INF], PQ = [{0,1}]
        // - Process node 1 (time=0):
        //     - Edge 1->2 (w=1): dist[2] = 1, PQ = [{1,2}]
        //     - Edge 1->3 (w=4): dist[3] = 4, PQ = [{1,2},{4,3}]
        // - Process node 2 (time=1):
        //     - Edge 2->3 (w=2): newTime=3 < dist[3]=4, dist[3]=3, PQ = [{3,3},{4,3}]
        // - Process node 3 (time=3):
        //     - Edge 3->4 (w=1): dist[4] = 4, PQ = [{4,3},{4,4}]
        // - Process node 3 again (time=4): stale, skip (4 > dist[3]=3)
        // - Process node 4 (time=4): no outgoing edges
        // - dist = [_, 0, 1, 3, 4] → max = 4 ✓
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        int n1 = 4;
        int[][] edges1 = {{1, 2, 1}, {1, 3, 4}, {2, 3, 2}, {3, 4, 1}};
        int[] sources1 = {1};
        int result1 = solution.networkDelayTime(n1, edges1, sources1);
        System.out.println("Input: n=" + n1 + ", edges=[[1,2,1],[1,3,4],[2,3,2],[3,4,1]], sources=[1]");
        System.out.println("Output: " + result1);
        System.out.println("Expected: 4");
        System.out.println("Correct: " + (result1 == 4));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // n = 4, edges = [[1,2,1],[3,4,1]], sources = [1,3]
        // Expected Output: 1
        //
        // Trace:
        // - Start: dist = [INF, 0, INF, 0, INF], PQ = [{0,1},{0,3}]
        // - Process node 1 (time=0):
        //     - Edge 1->2 (w=1): dist[2] = 1, PQ = [{0,3},{1,2}]
        // - Process node 3 (time=0):
        //     - Edge 3->4 (w=1): dist[4] = 1, PQ = [{1,2},{1,4}]
        // - Process node 2 (time=1): no outgoing edges
        // - Process node 4 (time=1): no outgoing edges
        // - dist = [_, 0, 1, 0, 1] → max = 1 ✓
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        int n2 = 4;
        int[][] edges2 = {{1, 2, 1}, {3, 4, 1}};
        int[] sources2 = {1, 3};
        int result2 = solution.networkDelayTime(n2, edges2, sources2);
        System.out.println("Input: n=" + n2 + ", edges=[[1,2,1],[3,4,1]], sources=[1,3]");
        System.out.println("Output: " + result2);
        System.out.println("Expected: 1");
        System.out.println("Correct: " + (result2 == 1));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 3: Impossible case
        // n = 3, edges = [[1,2,1]], sources = [1]
        // Node 3 is unreachable → Expected Output: -1
        // -----------------------------------------------------------------------
        System.out.println("=== Example 3 (Impossible Case) ===");
        int n3 = 3;
        int[][] edges3 = {{1, 2, 1}};
        int[] sources3 = {1};
        int result3 = solution.networkDelayTime(n3, edges3, sources3);
        System.out.println("Input: n=" + n3 + ", edges=[[1,2,1]], sources=[1]");
        System.out.println("Output: " + result3);
        System.out.println("Expected: -1");
        System.out.println("Correct: " + (result3 == -1));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 4: Single node, single source
        // n = 1, edges = [], sources = [1]
        // Only one node which is also the source → Expected Output: 0
        // -----------------------------------------------------------------------
        System.out.println("=== Example 4 (Single Node) ===");
        int n4 = 1;
        int[][] edges4 = {};
        int[] sources4 = {1};
        int result4 = solution.networkDelayTime(n4, edges4, sources4);
        System.out.println("Input: n=" + n4 + ", edges=[], sources=[1]");
        System.out.println("Output: " + result4);
        System.out.println("Expected: 0");
        System.out.println("Correct: " + (result4 == 0));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 5: Multiple edges between same pair, sources cover all nodes
        // n = 3, edges = [[1,2,5],[1,2,1],[2,3,2]], sources = [1,3]
        // Node 1: time=0 (source), Node 3: time=0 (source)
        // Node 2: min(0+5, 0+1) = 1 (via edge 1->2 with w=1)
        // All nodes reached → max = 0 (nodes 1 and 3 are sources, node 2 at time 1)
        // Expected Output: 1
        // -----------------------------------------------------------------------
        System.out.println("=== Example 5 (Multiple Edges + Multiple Sources) ===");
        int n5 = 3;
        int[][] edges5 = {{1, 2, 5}, {1, 2, 1}, {2, 3, 2}};
        int[] sources5 = {1, 3};
        int result5 = solution.networkDelayTime(n5, edges5, sources5);
        System.out.println("Input: n=" + n5 + ", edges=[[1,2,5],[1,2,1],[2,3,2]], sources=[1,3]");
        System.out.println("Output: " + result5);
        System.out.println("Expected: 1");
        System.out.println("Correct: " + (result5 == 1));
    }
}