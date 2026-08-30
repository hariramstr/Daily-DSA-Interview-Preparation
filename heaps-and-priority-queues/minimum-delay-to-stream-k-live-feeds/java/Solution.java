import java.util.*;

/*
Problem Title: Minimum Delay to Stream K Live Feeds

Problem Description:
A media platform must deliver a live event to viewers through a network of relay servers.
The network is an undirected weighted graph with n servers labeled 0 to n - 1.
Each edge [u, v, w] means a stream can be forwarded between servers u and v with
transmission delay w milliseconds. Server 0 is the origin server. Some servers are
marked as viewer gateways, and at least k of those gateways must receive the stream.

You may choose any subset of the viewer gateways as long as at least k of them are reached.
The cost of a delivery plan is the maximum shortest-path delay from server 0 to any chosen
gateway in that plan. Your task is to return the minimum possible cost.

In other words, compute the shortest delay from server 0 to every gateway, then choose k
gateways so that the largest chosen delay is as small as possible. If fewer than k gateways
are reachable from server 0, return -1.

This problem is designed for large sparse graphs, so an efficient heap-based shortest path
approach is expected.

Constraints:
- 1 <= n <= 200000
- 0 <= m == edges.length <= 300000
- edges[i] = [u, v, w]
- 0 <= u, v < n, u != v
- 1 <= w <= 10^9
- gateways.length contains distinct server indices and may include 0
- 1 <= k <= gateways.length

Example 1:
Input:
n = 6
edges = [[0,1,4],[1,2,3],[0,3,2],[3,4,5],[4,5,1],[2,5,2]]
gateways = [2,4,5]
k = 2
Output:
7

Explanation:
Shortest delays from 0 are:
- to 2 = 7
- to 4 = 7
- to 5 = 8

Choosing gateways 2 and 4 gives maximum delay 7, which is optimal.

Example 2:
Input:
n = 5
edges = [[0,1,2],[1,2,2],[3,4,1]]
gateways = [2,3,4]
k = 2
Output:
-1

Explanation:
Only gateway 2 is reachable from server 0. Since fewer than 2 gateways can receive the
stream, the answer is -1.
*/

public class Solution {

    /**
     * Small helper class representing one edge in the adjacency list.
     * Each graph edge stores:
     * - the neighboring node
     * - the weight (delay) to that neighbor
     */
    private static class Edge {
        int to;
        long weight;

        Edge(int to, long weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    /**
     * Helper class used inside Dijkstra's priority queue.
     * It stores:
     * - the current node
     * - the best-known distance used for queue ordering
     */
    private static class State {
        int node;
        long dist;

        State(int node, long dist) {
            this.node = node;
            this.dist = dist;
        }
    }

    /**
     * Computes the minimum possible delivery cost so that at least k gateway servers
     * receive the stream from server 0.
     *
     * Core idea:
     * 1. Build the weighted undirected graph.
     * 2. Run Dijkstra's algorithm from node 0 to compute shortest distances to all nodes.
     * 3. Extract distances for all gateway nodes that are reachable.
     * 4. If fewer than k gateways are reachable, return -1.
     * 5. Otherwise, the optimal answer is the k-th smallest reachable gateway distance.
     *
     * Why the k-th smallest distance?
     * If you want to choose at least k gateways while minimizing the maximum chosen delay,
     * the best strategy is to choose the k reachable gateways with the smallest shortest-path
     * delays. Then the largest among those chosen delays is exactly the k-th smallest delay.
     *
     * @param n the number of servers, labeled from 0 to n - 1
     * @param edges the undirected weighted edges, where each edge is [u, v, w]
     * @param gateways the list of gateway server indices
     * @param k the minimum number of gateways that must receive the stream
     * @return the minimum possible maximum shortest-path delay among a chosen set of at least k gateways;
     *         returns -1 if fewer than k gateways are reachable from server 0
     * Time complexity: O((n + m) log n + g log g), where m is the number of edges and g is gateways.length
     * Space complexity: O(n + m)
     */
    public long minimumDelayToStreamKFeeds(int n, int[][] edges, int[] gateways, int k) {
        // Step 1: Build the graph as an adjacency list.
        // Because the graph is undirected, every input edge [u, v, w]
        // is added in both directions:
        // - u -> v with weight w
        // - v -> u with weight w
        List<List<Edge>> graph = buildGraph(n, edges);

        // Step 2: Compute shortest distances from source node 0 to every node.
        // Dijkstra is the correct choice because:
        // - all edge weights are positive
        // - the graph can be very large and sparse
        long[] dist = dijkstra(n, graph, 0);

        // Step 3: Collect distances for gateway nodes that are reachable.
        // Unreachable nodes will still have "infinite" distance.
        List<Long> reachableGatewayDistances = new ArrayList<>();
        long inf = Long.MAX_VALUE / 4;

        for (int gateway : gateways) {
            if (dist[gateway] < inf) {
                reachableGatewayDistances.add(dist[gateway]);
            }
        }

        // Step 4: If fewer than k gateways are reachable, the task is impossible.
        if (reachableGatewayDistances.size() < k) {
            return -1L;
        }

        // Step 5: Sort the reachable gateway distances.
        // The answer is the k-th smallest distance (1-indexed),
        // which is index k - 1 in 0-indexed Java lists.
        Collections.sort(reachableGatewayDistances);
        return reachableGatewayDistances.get(k - 1);
    }

    /**
     * Builds an undirected weighted graph using adjacency lists.
     *
     * @param n the number of nodes in the graph
     * @param edges the edge list, where each edge is [u, v, w]
     * @return adjacency list representation of the graph
     * Time complexity: O(n + m)
     * Space complexity: O(n + m)
     */
    public List<List<Edge>> buildGraph(int n, int[][] edges) {
        // Create an empty adjacency list for each node.
        List<List<Edge>> graph = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }

        // Insert each undirected edge in both directions.
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            long w = edge[2];

            graph.get(u).add(new Edge(v, w));
            graph.get(v).add(new Edge(u, w));
        }

        return graph;
    }

    /**
     * Runs Dijkstra's shortest path algorithm from a given source node.
     *
     * This implementation uses a min-heap (priority queue) ordered by current distance.
     * Because Java's PriorityQueue does not support decrease-key directly, we may insert
     * multiple states for the same node. When we pop a state, we ignore it if it is stale
     * (meaning it does not match the current best-known distance).
     *
     * @param n the number of nodes
     * @param graph adjacency list of the weighted graph
     * @param source the starting node
     * @return an array dist where dist[i] is the shortest distance from source to i,
     *         or a very large value if i is unreachable
     * Time complexity: O((n + m) log n)
     * Space complexity: O(n + m)
     */
    public long[] dijkstra(int n, List<List<Edge>> graph, int source) {
        // We use a large finite value instead of Long.MAX_VALUE directly
        // to avoid overflow when adding edge weights.
        long inf = Long.MAX_VALUE / 4;

        // dist[i] will store the best-known shortest distance from source to node i.
        long[] dist = new long[n];
        Arrays.fill(dist, inf);
        dist[source] = 0L;

        // Min-heap ordered by smallest distance first.
        PriorityQueue<State> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a.dist));
        pq.offer(new State(source, 0L));

        // Standard Dijkstra loop:
        // repeatedly extract the node with the smallest tentative distance.
        while (!pq.isEmpty()) {
            State current = pq.poll();
            int node = current.node;
            long currentDist = current.dist;

            // Very important optimization:
            // If this queue entry is stale, skip it.
            //
            // Why can stale entries exist?
            // Suppose we first discovered node X with distance 10 and pushed it.
            // Later we found a better path with distance 7 and pushed that too.
            // When the old distance 10 entry comes out, it should be ignored.
            if (currentDist != dist[node]) {
                continue;
            }

            // Relax all outgoing edges from the current node.
            for (Edge edge : graph.get(node)) {
                int next = edge.to;
                long newDist = currentDist + edge.weight;

                // If going through "node" improves the shortest distance to "next",
                // update it and push the new state into the priority queue.
                if (newDist < dist[next]) {
                    dist[next] = newDist;
                    pq.offer(new State(next, newDist));
                }
            }
        }

        return dist;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm
     * Space complexity: O(1) for the demonstration itself, excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int n1 = 6;
        int[][] edges1 = {
            {0, 1, 4},
            {1, 2, 3},
            {0, 3, 2},
            {3, 4, 5},
            {4, 5, 1},
            {2, 5, 2}
        };
        int[] gateways1 = {2, 4, 5};
        int k1 = 2;

        long result1 = solution.minimumDelayToStreamKFeeds(n1, edges1, gateways1, k1);
        System.out.println(result1); // Expected: 7

        // Example 2
        int n2 = 5;
        int[][] edges2 = {
            {0, 1, 2},
            {1, 2, 2},
            {3, 4, 1}
        };
        int[] gateways2 = {2, 3, 4};
        int k2 = 2;

        long result2 = solution.minimumDelayToStreamKFeeds(n2, edges2, gateways2, k2);
        System.out.println(result2); // Expected: -1
    }
}