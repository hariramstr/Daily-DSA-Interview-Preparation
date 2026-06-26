import java.util.*;

/*
 * Title: Earliest Shared Dependency Between Services
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Problem Description:
 * You are given a directed graph representing service dependencies in a microservice platform.
 * An edge [a, b] means service a directly depends on service b. If service X depends on Y,
 * and Y depends on Z, then X also indirectly depends on Z.
 *
 * For two given services s1 and s2, find the shared dependency that can be reached from both
 * services with the smallest combined number of dependency hops. If there are multiple such
 * services with the same minimum total distance, return the one with the smallest service id.
 * If no shared dependency exists, return -1.
 *
 * The graph may contain cycles due to misconfigured dependency metadata, so your solution must
 * handle cycles safely.
 *
 * More formally, for every node x that is reachable from both s1 and s2, define:
 * score(x) = dist(s1, x) + dist(s2, x),
 * where dist(u, v) is the minimum number of directed edges from u to v.
 *
 * You must return the reachable node with the minimum score, breaking ties by smaller node id.
 *
 * Constraints:
 * - 1 <= n <= 100000
 * - 0 <= edges.length <= 200000
 * - edges[i] = [from, to]
 * - 0 <= from, to < n
 * - 0 <= s1, s2 < n
 * - Multiple outgoing edges are allowed
 * - Self-loops and cycles may exist
 *
 * Efficient approach:
 * Because all edges have equal weight 1, the shortest distance from a start node to every
 * reachable node can be computed using Breadth-First Search (BFS).
 *
 * Steps:
 * 1. Build an adjacency list for the directed graph.
 * 2. Run BFS from s1 to compute dist1[].
 * 3. Run BFS from s2 to compute dist2[].
 * 4. Scan all nodes:
 *    - If a node is reachable from both starts, compute dist1[node] + dist2[node].
 *    - Keep the node with the smallest sum.
 *    - If sums tie, keep the smaller node id.
 * 5. If no node is reachable from both, return -1.
 *
 * This works correctly even with cycles because BFS marks a node the first time it is reached,
 * which is guaranteed to be the shortest path length in an unweighted graph.
 */

public class Solution {

    /**
     * Finds the best shared dependency reachable from both services.
     *
     * @param n     the number of services/nodes in the graph, labeled from 0 to n - 1
     * @param edges the directed dependency edges, where edges[i] = [from, to]
     * @param s1    the first starting service
     * @param s2    the second starting service
     * @return the id of the shared reachable node with minimum combined distance;
     *         if multiple nodes have the same minimum score, returns the smallest id;
     *         returns -1 if no shared reachable node exists
     *
     * Time complexity: O(n + m), where m = edges.length
     * Space complexity: O(n + m)
     */
    public int earliestSharedDependency(int n, int[][] edges, int s1, int s2) {
        // Build adjacency list representation of the directed graph.
        // graph[u] will contain all nodes v such that there is an edge u -> v.
        List<List<Integer>> graph = buildGraph(n, edges);

        // Compute shortest distances from s1 to every node.
        int[] dist1 = bfsDistances(graph, s1);

        // Compute shortest distances from s2 to every node.
        int[] dist2 = bfsDistances(graph, s2);

        // We will scan every node and choose the best candidate.
        int bestNode = -1;
        long bestScore = Long.MAX_VALUE;

        for (int node = 0; node < n; node++) {
            // A node is a valid shared dependency only if both starts can reach it.
            if (dist1[node] != -1 && dist2[node] != -1) {
                long score = (long) dist1[node] + dist2[node];

                // Update answer if:
                // 1. This node has a strictly smaller score, or
                // 2. Same score but smaller node id.
                if (score < bestScore || (score == bestScore && node < bestNode)) {
                    bestScore = score;
                    bestNode = node;
                }
            }
        }

        return bestNode;
    }

    /**
     * Builds an adjacency list for the directed graph.
     *
     * @param n     the number of nodes
     * @param edges the directed edges
     * @return adjacency list representation of the graph
     *
     * Time complexity: O(n + m), where m = edges.length
     * Space complexity: O(n + m)
     */
    public List<List<Integer>> buildGraph(int n, int[][] edges) {
        // Create an empty neighbor list for each node.
        List<List<Integer>> graph = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }

        // Add each directed edge from -> to into the adjacency list.
        for (int[] edge : edges) {
            int from = edge[0];
            int to = edge[1];
            graph.get(from).add(to);
        }

        return graph;
    }

    /**
     * Computes shortest path distances from a start node to all other nodes in an
     * unweighted directed graph using BFS.
     *
     * @param graph the adjacency list of the directed graph
     * @param start the starting node
     * @return an array dist where dist[v] is the minimum number of edges from start to v;
     *         dist[v] is -1 if v is unreachable from start
     *
     * Time complexity: O(n + m), where n is the number of nodes and m is the number of edges
     * Space complexity: O(n)
     */
    public int[] bfsDistances(List<List<Integer>> graph, int start) {
        int n = graph.size();

        // Initialize all distances to -1, meaning "unreachable so far".
        int[] dist = new int[n];
        Arrays.fill(dist, -1);

        // Standard BFS queue.
        Queue<Integer> queue = new ArrayDeque<>();

        // The start node is reachable from itself with distance 0.
        dist[start] = 0;
        queue.offer(start);

        // Perform BFS level by level.
        while (!queue.isEmpty()) {
            int current = queue.poll();

            // Explore all outgoing edges current -> neighbor.
            for (int neighbor : graph.get(current)) {
                // If neighbor has not been visited yet, then this is the shortest
                // possible way to reach it in an unweighted graph.
                if (dist[neighbor] == -1) {
                    dist[neighbor] = dist[current] + 1;
                    queue.offer(neighbor);
                }
            }
        }

        return dist;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n + m) per demonstration case
     * Space complexity: O(n + m)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        // n = 6
        // edges = [[0,2],[1,2],[2,3],[1,4],[4,3],[0,5]]
        // s1 = 0, s2 = 1
        // Reachable from 0: 0,2,3,5
        // Reachable from 1: 1,2,3,4
        // Shared: 2 and 3
        // score(2) = 1 + 1 = 2
        // score(3) = 2 + 2 = 4
        // Answer = 2
        int n1 = 6;
        int[][] edges1 = {
            {0, 2}, {1, 2}, {2, 3}, {1, 4}, {4, 3}, {0, 5}
        };
        int s1a = 0;
        int s2a = 1;
        System.out.println(solution.earliestSharedDependency(n1, edges1, s1a, s2a)); // Expected: 2

        // Example 2
        // n = 5
        // edges = [[0,1],[1,2],[2,0],[3,2],[3,4]]
        // s1 = 0, s2 = 3
        //
        // From 0:
        // dist(0,0)=0, dist(0,1)=1, dist(0,2)=2
        //
        // From 3:
        // dist(3,2)=1, dist(3,0)=2, dist(3,1)=3, dist(3,4)=1
        //
        // Shared nodes: 0,1,2
        // score(0)=0+2=2
        // score(1)=1+3=4
        // score(2)=2+1=3
        // Best is 0
        int n2 = 5;
        int[][] edges2 = {
            {0, 1}, {1, 2}, {2, 0}, {3, 2}, {3, 4}
        };
        int s1b = 0;
        int s2b = 3;
        System.out.println(solution.earliestSharedDependency(n2, edges2, s1b, s2b)); // Expected: 0

        // Additional example: no shared reachable dependency
        int n3 = 4;
        int[][] edges3 = {
            {0, 1}, {2, 3}
        };
        int s1c = 0;
        int s2c = 2;
        System.out.println(solution.earliestSharedDependency(n3, edges3, s1c, s2c)); // Expected: -1
    }
}