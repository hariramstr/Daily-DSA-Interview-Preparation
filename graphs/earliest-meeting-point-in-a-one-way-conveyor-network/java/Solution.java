/*
Problem Title: Earliest Meeting Point in a One-Way Conveyor Network

Problem Description:
In a large warehouse, stations are connected by one-way conveyor belts. The warehouse is modeled as a directed graph with n stations labeled from 0 to n - 1. Each conveyor belt is represented by a directed edge [u, v], meaning a package can move from station u to station v in exactly 1 minute.

Two packages start at stations startA and startB at the same time. A station x is considered a valid meeting point if both packages can reach x by following the direction of the conveyor belts. The arrival time for a meeting point is defined as the later of the two arrival times, because both packages must have arrived.

Return the station index of the valid meeting point that minimizes this arrival time. If there are multiple such stations, return the smallest station index among them. If no valid meeting point exists, return -1.

You must consider the graph as unweighted, but it may contain cycles and disconnected components. A package is allowed to stay at its starting station, so if both packages start at the same station, that station is automatically a valid meeting point with arrival time 0.

Constraints:
- 1 <= n <= 100000
- 0 <= edges.length <= 200000
- edges[i] = [u, v]
- 0 <= u, v < n
- 0 <= startA, startB < n
- There may be duplicate edges, though they do not change the answer.

Example 1:
Input: n = 6, edges = [[0,2],[1,2],[2,3],[1,4],[4,3],[3,5]], startA = 0, startB = 1
Output: 2

Example 2:
Input: n = 5, edges = [[0,1],[1,2],[3,4]], startA = 0, startB = 3
Output: -1
*/

import java.util.*;

public class Solution {

    /**
     * Finds the station index that both packages can reach such that the meeting time
     * (the later of the two arrival times) is minimized. If multiple stations have the
     * same best meeting time, the smallest station index is returned.
     *
     * The graph is directed and unweighted, so the shortest travel time from a start node
     * to every reachable node can be computed using Breadth-First Search (BFS).
     *
     * Strategy:
     * 1. Build an adjacency list for the directed graph.
     * 2. Run BFS from startA to compute shortest distances from A to all nodes.
     * 3. Run BFS from startB to compute shortest distances from B to all nodes.
     * 4. For every node:
     *    - If both distances are known, it is a valid meeting point.
     *    - Its meeting time is max(distA[node], distB[node]).
     *    - Keep the node with the smallest meeting time.
     *    - If tied, keep the smaller node index.
     *
     * @param n the number of stations (nodes) in the graph
     * @param edges the directed edges where each edge [u, v] means travel from u to v in 1 minute
     * @param startA the starting station of package A
     * @param startB the starting station of package B
     * @return the index of the best meeting station, or -1 if no common reachable station exists
     * Time complexity: O(n + m), where m = edges.length
     * Space complexity: O(n + m)
     */
    public int earliestMeetingPoint(int n, int[][] edges, int startA, int startB) {
        // Step 1:
        // Build the adjacency list representation of the directed graph.
        //
        // Why adjacency list?
        // - It is memory-efficient for sparse graphs.
        // - It allows us to quickly iterate over all outgoing neighbors of a node.
        //
        // graph[u] will contain every node v such that there is a directed edge u -> v.
        List<List<Integer>> graph = buildGraph(n, edges);

        // Step 2:
        // Compute shortest distances from startA to every node using BFS.
        //
        // Since every edge has equal weight 1, BFS guarantees shortest path lengths
        // in an unweighted graph.
        int[] distA = bfsDistances(graph, startA);

        // Step 3:
        // Compute shortest distances from startB to every node using BFS.
        int[] distB = bfsDistances(graph, startB);

        // Step 4:
        // Scan every node and evaluate whether it is a valid meeting point.
        //
        // A node is valid if:
        // - distA[node] != -1  => reachable from A
        // - distB[node] != -1  => reachable from B
        //
        // For each valid node, meeting time = max(distA[node], distB[node]).
        //
        // We want:
        // 1. Minimum meeting time
        // 2. If tied, minimum node index
        int bestNode = -1;
        int bestTime = Integer.MAX_VALUE;

        for (int node = 0; node < n; node++) {
            // If either package cannot reach this node, it cannot be a meeting point.
            if (distA[node] == -1 || distB[node] == -1) {
                continue;
            }

            // The meeting is only complete when both packages have arrived,
            // so the effective arrival time is the later of the two.
            int meetingTime = Math.max(distA[node], distB[node]);

            // Update the answer if:
            // - we found a strictly smaller meeting time, or
            // - the meeting time is tied and this node index is smaller
            if (meetingTime < bestTime || (meetingTime == bestTime && node < bestNode)) {
                bestTime = meetingTime;
                bestNode = node;
            }
        }

        return bestNode;
    }

    /**
     * Builds a directed adjacency list from the given edge list.
     *
     * @param n the number of nodes in the graph
     * @param edges the directed edges where each edge [u, v] means u -> v
     * @return adjacency list representation of the graph
     * Time complexity: O(n + m), where m = edges.length
     * Space complexity: O(n + m)
     */
    public List<List<Integer>> buildGraph(int n, int[][] edges) {
        // Create an empty adjacency list for each node.
        List<List<Integer>> graph = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }

        // Add every directed edge u -> v into the adjacency list.
        //
        // Duplicate edges are allowed by the problem statement.
        // They do not affect correctness. BFS may inspect the same neighbor more than once,
        // but once a node is visited, it will not be re-enqueued.
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            graph.get(u).add(v);
        }

        return graph;
    }

    /**
     * Computes shortest distances from a given start node to all other nodes in a directed,
     * unweighted graph using Breadth-First Search (BFS).
     *
     * Distances are measured in number of edges, which directly equals minutes in this problem.
     * Unreachable nodes are marked with -1.
     *
     * @param graph the adjacency list of the directed graph
     * @param start the starting node for BFS
     * @return an array dist where dist[i] is the shortest distance from start to i, or -1 if unreachable
     * Time complexity: O(n + m), where n is number of nodes and m is number of edges
     * Space complexity: O(n)
     */
    public int[] bfsDistances(List<List<Integer>> graph, int start) {
        int n = graph.size();

        // Initialize all distances to -1, meaning "unreachable so far".
        int[] dist = new int[n];
        Arrays.fill(dist, -1);

        // Standard BFS queue.
        Queue<Integer> queue = new ArrayDeque<>();

        // The start node is reachable from itself in 0 minutes.
        dist[start] = 0;
        queue.offer(start);

        // Perform BFS level by level.
        while (!queue.isEmpty()) {
            int current = queue.poll();

            // Explore all outgoing edges current -> neighbor.
            for (int neighbor : graph.get(current)) {
                // If neighbor has not been visited yet, we have found its shortest distance.
                //
                // Why is this the shortest?
                // BFS visits nodes in increasing order of distance from the start.
                // Therefore, the first time we reach a node is always via the shortest path.
                if (dist[neighbor] == -1) {
                    dist[neighbor] = dist[current] + 1;
                    queue.offer(neighbor);
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
     * Time complexity: O(n + m) per demonstration call
     * Space complexity: O(n + m)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // n = 6
        // edges = [[0,2],[1,2],[2,3],[1,4],[4,3],[3,5]]
        // startA = 0, startB = 1
        //
        // Reachable from 0:
        // 0 (0), 2 (1), 3 (2), 5 (3)
        //
        // Reachable from 1:
        // 1 (0), 2 (1), 4 (1), 3 (2), 5 (3)
        //
        // Common reachable nodes:
        // 2 => max(1,1) = 1
        // 3 => max(2,2) = 2
        // 5 => max(3,3) = 3
        //
        // Best answer is 2.
        int n1 = 6;
        int[][] edges1 = {
            {0, 2},
            {1, 2},
            {2, 3},
            {1, 4},
            {4, 3},
            {3, 5}
        };
        int startA1 = 0;
        int startB1 = 1;
        int result1 = solution.earliestMeetingPoint(n1, edges1, startA1, startB1);
        System.out.println(result1); // Expected: 2

        // Example 2:
        // n = 5
        // edges = [[0,1],[1,2],[3,4]]
        // startA = 0, startB = 3
        //
        // Reachable from 0: {0,1,2}
        // Reachable from 3: {3,4}
        //
        // No common reachable node exists, so answer is -1.
        int n2 = 5;
        int[][] edges2 = {
            {0, 1},
            {1, 2},
            {3, 4}
        };
        int startA2 = 0;
        int startB2 = 3;
        int result2 = solution.earliestMeetingPoint(n2, edges2, startA2, startB2);
        System.out.println(result2); // Expected: -1

        // Additional quick demonstration:
        // If both packages start at the same node, that node is immediately a valid meeting point.
        int n3 = 3;
        int[][] edges3 = {
            {0, 1},
            {1, 2}
        };
        int startA3 = 1;
        int startB3 = 1;
        int result3 = solution.earliestMeetingPoint(n3, edges3, startA3, startB3);
        System.out.println(result3); // Expected: 1
    }
}