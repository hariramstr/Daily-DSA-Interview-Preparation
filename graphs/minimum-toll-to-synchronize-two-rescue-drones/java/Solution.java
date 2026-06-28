import java.util.*;

/*
 * Title: Minimum Toll to Synchronize Two Rescue Drones
 *
 * Problem Description:
 * A disaster response team operates on a directed graph of air corridors with n stations labeled
 * from 0 to n - 1. Each corridor is represented as [u, v, w], meaning a drone can fly from
 * station u to station v by paying toll w. Two rescue drones start at different stations s1 and s2,
 * and both must eventually reach the same rendezvous station r. After meeting, exactly one of them
 * needs to continue from r to the final supply station t, carrying the combined payload.
 *
 * The total mission cost is:
 *   cost(s1 -> r) + cost(s2 -> r) + cost(r -> t)
 *
 * We must return the minimum possible total mission cost over all valid rendezvous stations r.
 * If there is no station where both drones can meet and then reach t, return -1.
 *
 * Important observations:
 * - Before meeting, the two drones move independently.
 * - After meeting, they share the same route from r to t, so that suffix cost is counted once.
 * - Edge weights are non-negative, so Dijkstra's algorithm is appropriate.
 * - Trying every rendezvous station and running shortest path each time would be too slow.
 *
 * Efficient idea:
 * 1. Compute shortest distances from s1 to every node.
 * 2. Compute shortest distances from s2 to every node.
 * 3. Compute shortest distances from every node to t.
 *    To do this efficiently, reverse all edges and run Dijkstra from t in the reversed graph.
 * 4. For every node r, if all three distances are finite, evaluate:
 *       distFromS1[r] + distFromS2[r] + distToT[r]
 *    and take the minimum.
 *
 * This runs in:
 * - O((n + m) log n) time for each Dijkstra
 * - 3 Dijkstra runs total
 * - Overall: O((n + m) log n)
 *
 * This is efficient for:
 * - n up to 100000
 * - m up to 200000
 */
public class Solution {

    /**
     * A large sentinel value used to represent "unreachable".
     * We keep it safely below Long.MAX_VALUE so additions do not overflow.
     */
    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Simple directed weighted edge.
     */
    private static class Edge {
        int to;
        int weight;

        Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    /**
     * State stored inside the priority queue for Dijkstra's algorithm.
     * It contains:
     * - the current node
     * - the best known distance to that node at the time it was pushed
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
     * Computes the minimum total mission cost.
     *
     * Strategy:
     * - Build the original graph.
     * - Build the reversed graph.
     * - Run Dijkstra from s1 on the original graph.
     * - Run Dijkstra from s2 on the original graph.
     * - Run Dijkstra from t on the reversed graph.
     *   This gives shortest distances from every node to t in the original graph.
     * - Try every node as the rendezvous station and take the minimum valid sum.
     *
     * Why reversed graph works:
     * - Suppose there is an original edge u -> v with weight w.
     * - In the reversed graph, that becomes v -> u with weight w.
     * - Therefore, a shortest path from x to t in the original graph corresponds exactly to
     *   a shortest path from t to x in the reversed graph.
     *
     * @param n the number of stations (nodes)
     * @param corridors directed weighted edges, where each entry is [u, v, w]
     * @param s1 starting station of drone 1
     * @param s2 starting station of drone 2
     * @param t final supply station
     * @return the minimum total mission cost, or -1 if no valid rendezvous station exists
     * Time complexity: O((n + m) log n), where m = corridors.length
     * Space complexity: O(n + m)
     */
    public long minimumWeight(int n, int[][] corridors, int s1, int s2, int t) {
        // Create adjacency lists for:
        // 1) the original graph
        // 2) the reversed graph
        List<List<Edge>> graph = buildGraph(n, corridors, false);
        List<List<Edge>> reversedGraph = buildGraph(n, corridors, true);

        // Shortest distances from s1 to every node in the original graph.
        long[] distFromS1 = dijkstra(n, graph, s1);

        // Shortest distances from s2 to every node in the original graph.
        long[] distFromS2 = dijkstra(n, graph, s2);

        // Shortest distances from every node to t in the original graph.
        // We obtain them by running Dijkstra from t on the reversed graph.
        long[] distToT = dijkstra(n, reversedGraph, t);

        long answer = INF;

        // Try every node as the rendezvous station r.
        for (int r = 0; r < n; r++) {
            // If any of the three required path segments is unreachable,
            // then this node cannot serve as a valid meeting point.
            if (distFromS1[r] == INF || distFromS2[r] == INF || distToT[r] == INF) {
                continue;
            }

            // Total cost if both drones meet at r and then continue together to t.
            long total = distFromS1[r] + distFromS2[r] + distToT[r];

            if (total < answer) {
                answer = total;
            }
        }

        return answer == INF ? -1 : answer;
    }

    /**
     * Builds either the original graph or the reversed graph.
     *
     * If reverse == false:
     * - add edge u -> v with weight w
     *
     * If reverse == true:
     * - add edge v -> u with weight w
     *
     * @param n number of nodes
     * @param corridors edge list where each edge is [u, v, w]
     * @param reverse whether to build the reversed graph
     * @return adjacency list representation of the requested graph
     * Time complexity: O(n + m)
     * Space complexity: O(n + m)
     */
    public List<List<Edge>> buildGraph(int n, int[][] corridors, boolean reverse) {
        List<List<Edge>> graph = new ArrayList<>(n);

        // Initialize an empty adjacency list for every node.
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }

        // Insert each edge in the appropriate direction.
        for (int[] edge : corridors) {
            int u = edge[0];
            int v = edge[1];
            int w = edge[2];

            if (!reverse) {
                graph.get(u).add(new Edge(v, w));
            } else {
                graph.get(v).add(new Edge(u, w));
            }
        }

        return graph;
    }

    /**
     * Runs Dijkstra's algorithm from a single source on a directed weighted graph
     * with non-negative edge weights.
     *
     * Very detailed explanation:
     * 1. Initialize all distances as INF (unreachable).
     * 2. Set distance[source] = 0.
     * 3. Use a min-priority queue ordered by current best known distance.
     * 4. Repeatedly extract the node with the smallest tentative distance.
     * 5. If the extracted distance is stale (not equal to the current best distance array),
     *    skip it. This is a standard optimization that avoids needing a decrease-key operation.
     * 6. Relax all outgoing edges:
     *      if dist[u] + weight < dist[v], update dist[v] and push the new state.
     * 7. When the queue is empty, all shortest distances are finalized.
     *
     * Why this is correct:
     * - Dijkstra's algorithm is correct for graphs with non-negative edge weights.
     * - Once a node is extracted with its smallest valid distance, no shorter path to it exists.
     *
     * @param n number of nodes
     * @param graph adjacency list of the graph
     * @param source starting node
     * @return array dist where dist[i] is the shortest distance from source to i,
     *         or INF if i is unreachable
     * Time complexity: O((n + m) log n)
     * Space complexity: O(n + m)
     */
    public long[] dijkstra(int n, List<List<Edge>> graph, int source) {
        long[] dist = new long[n];
        Arrays.fill(dist, INF);
        dist[source] = 0L;

        // Min-heap ordered by smallest distance first.
        PriorityQueue<State> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a.dist));
        pq.offer(new State(source, 0L));

        while (!pq.isEmpty()) {
            State current = pq.poll();
            int u = current.node;
            long currentDist = current.dist;

            // If this entry is outdated, skip it.
            // This happens because we may have pushed an older, worse distance for the same node.
            if (currentDist != dist[u]) {
                continue;
            }

            // Explore all outgoing edges from u.
            for (Edge edge : graph.get(u)) {
                int v = edge.to;
                long newDist = currentDist + edge.weight;

                // Relaxation step:
                // If going through u improves the best known distance to v, update it.
                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    pq.offer(new State(v, newDist));
                }
            }
        }

        return dist;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Note about Example 1:
     * The textual explanation in the prompt corrects the expected result to 9.
     * We verify that:
     * - Meeting at node 2:
     *   s1 -> 2 = 2
     *   s2 -> 2 = 3
     *   2 -> 5 = 2 -> 4 -> 3 -> 5 = 1 + 1 + 2 = 4
     *   total = 2 + 3 + 4 = 9
     *
     * Example 2:
     * - Meeting at node 0:
     *   s1 -> 0 = 0
     *   s2 -> 0 = 1
     *   0 -> 3 = 0 -> 1 -> 3 = 5 + 2 = 7
     *   total = 8
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O((n + m) log n) for each demonstration call
     * Space complexity: O(n + m)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int n1 = 6;
        int[][] corridors1 = {
            {0, 2, 2},
            {1, 2, 3},
            {2, 3, 4},
            {2, 4, 1},
            {4, 3, 1},
            {3, 5, 2},
            {4, 5, 5}
        };
        int s1_1 = 0;
        int s2_1 = 1;
        int t1 = 5;

        long result1 = solution.minimumWeight(n1, corridors1, s1_1, s2_1, t1);
        System.out.println(result1); // Expected: 9

        int n2 = 4;
        int[][] corridors2 = {
            {0, 1, 5},
            {1, 3, 2},
            {2, 0, 1}
        };
        int s1_2 = 0;
        int s2_2 = 2;
        int t2 = 3;

        long result2 = solution.minimumWeight(n2, corridors2, s1_2, s2_2, t2);
        System.out.println(result2); // Expected: 8
    }
}