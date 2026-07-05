import java.util.*;

/*
Problem Title: Cheapest Shared Shuttle Pickup

Problem Description:
A company campus runs one-way shuttle lanes between buildings. The campus wants to pick a single shuttle pickup building where two employees can meet before riding together to the main office. You are given an integer n representing buildings labeled from 0 to n - 1, a list of directed edges lanes where lanes[i] = [u, v, cost] means there is a one-way shuttle lane from building u to building v with travel cost cost, and three distinct buildings: aliceStart, bobStart, and office.

Alice starts at aliceStart and Bob starts at bobStart. They may travel independently through the directed graph and choose any building m as their meeting point. After both reach m, they continue together from m to office, paying that final segment cost only once because they share the shuttle from that point onward.

Return the minimum total travel cost needed for both employees to reach the office under this rule. If there is no valid meeting point from which both employees can eventually reach the office, return -1.

Formally, you must minimize:

dist(aliceStart, m) + dist(bobStart, m) + dist(m, office)

for any building m that is reachable from both starting buildings and can also reach office.

Constraints:
- 2 <= n <= 100000
- 1 <= lanes.length <= 200000
- 0 <= u, v < n
- 1 <= cost <= 1000000000
- aliceStart, bobStart, and office are distinct
- There may be multiple edges between the same pair of buildings
- The graph is not guaranteed to be strongly connected

Example 1:
Input: n = 6, lanes = [[0,2,2],[1,2,4],[2,3,3],[2,4,1],[4,3,1],[3,5,2],[4,5,5]], aliceStart = 0, bobStart = 1, office = 5
Output: 11

Example 2:
Input: n = 5, lanes = [[0,1,3],[1,4,4],[2,3,2]], aliceStart = 0, bobStart = 2, office = 4
Output: -1
*/

/**
 * A complete runnable solution for finding the minimum total travel cost when
 * two employees may meet at any building and then share the final ride to the office.
 *
 * The key idea:
 * 1. Compute shortest distances from Alice's start to every building.
 * 2. Compute shortest distances from Bob's start to every building.
 * 3. Compute shortest distances from every building to the office.
 *
 * Step 3 is done efficiently by reversing all edges and running Dijkstra once
 * from the office on the reversed graph.
 *
 * Then for every possible meeting building m, evaluate:
 * distAlice[m] + distBob[m] + distToOffice[m]
 *
 * The minimum valid sum is the answer.
 */
public class Solution {

    /**
     * A large value used to represent "unreachable".
     * We use a long because path sums can be much larger than int.
     */
    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Simple directed weighted edge.
     */
    static class Edge {
        int to;
        long cost;

        Edge(int to, long cost) {
            this.to = to;
            this.cost = cost;
        }
    }

    /**
     * State stored in the priority queue for Dijkstra's algorithm.
     * It contains the current node and the best known distance to that node.
     */
    static class State {
        int node;
        long dist;

        State(int node, long dist) {
            this.node = node;
            this.dist = dist;
        }
    }

    /**
     * Computes the minimum total travel cost:
     * dist(aliceStart, m) + dist(bobStart, m) + dist(m, office)
     * over all valid meeting points m.
     *
     * Detailed idea:
     * - Build the original graph for forward travel.
     * - Build the reversed graph so that a shortest path from office in the reversed graph
     *   corresponds exactly to a shortest path to office in the original graph.
     * - Run Dijkstra three times:
     *   1) from aliceStart on original graph
     *   2) from bobStart on original graph
     *   3) from office on reversed graph
     * - For each building m:
     *   if all three distances are finite, then m is a valid meeting point.
     *   Compute total cost and keep the minimum.
     *
     * @param n number of buildings labeled from 0 to n - 1
     * @param lanes directed weighted edges where lanes[i] = [u, v, cost]
     * @param aliceStart Alice's starting building
     * @param bobStart Bob's starting building
     * @param office destination building
     * @return the minimum total travel cost, or -1 if no valid meeting point exists
     * Time complexity: O((n + lanes.length) log n)
     * Space complexity: O(n + lanes.length)
     */
    public long minimumSharedShuttleCost(int n, int[][] lanes, int aliceStart, int bobStart, int office) {
        // Create adjacency lists for:
        // 1) the original graph
        // 2) the reversed graph
        List<List<Edge>> graph = new ArrayList<>(n);
        List<List<Edge>> reversedGraph = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
            reversedGraph.add(new ArrayList<>());
        }

        // Build both graphs.
        // Original edge: u -> v with cost c
        // Reversed edge: v -> u with cost c
        for (int[] lane : lanes) {
            int u = lane[0];
            int v = lane[1];
            int cost = lane[2];

            graph.get(u).add(new Edge(v, cost));
            reversedGraph.get(v).add(new Edge(u, cost));
        }

        // Shortest distances from Alice to every building.
        long[] distFromAlice = dijkstra(n, graph, aliceStart);

        // Shortest distances from Bob to every building.
        long[] distFromBob = dijkstra(n, graph, bobStart);

        // Shortest distances from every building to office.
        // We get this by running Dijkstra from office on the reversed graph.
        long[] distToOffice = dijkstra(n, reversedGraph, office);

        long answer = INF;

        // Try every building as the meeting point.
        for (int meeting = 0; meeting < n; meeting++) {
            // A meeting point is valid only if:
            // - Alice can reach it
            // - Bob can reach it
            // - It can reach the office
            if (distFromAlice[meeting] == INF || distFromBob[meeting] == INF || distToOffice[meeting] == INF) {
                continue;
            }

            long total = distFromAlice[meeting] + distFromBob[meeting] + distToOffice[meeting];
            answer = Math.min(answer, total);
        }

        return answer == INF ? -1 : answer;
    }

    /**
     * Runs Dijkstra's shortest path algorithm from a single source on a directed weighted graph.
     *
     * This implementation is appropriate because all edge costs are positive.
     *
     * Step-by-step:
     * 1. Initialize all distances to INF.
     * 2. Set source distance to 0.
     * 3. Use a min-priority queue ordered by current best distance.
     * 4. Repeatedly extract the node with the smallest tentative distance.
     * 5. If the extracted distance is outdated, skip it.
     * 6. Otherwise, relax all outgoing edges:
     *      if dist[current] + edge.cost < dist[next], update dist[next]
     *      and push the new state into the priority queue.
     * 7. When the queue is empty, all shortest distances are finalized.
     *
     * @param n number of nodes in the graph
     * @param graph adjacency list of the graph
     * @param source starting node
     * @return an array where result[i] is the shortest distance from source to i,
     *         or INF if i is unreachable
     * Time complexity: O((n + E) log n), where E is the number of edges
     * Space complexity: O(n + E)
     */
    public long[] dijkstra(int n, List<List<Edge>> graph, int source) {
        long[] dist = new long[n];
        Arrays.fill(dist, INF);
        dist[source] = 0L;

        PriorityQueue<State> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a.dist));
        pq.offer(new State(source, 0L));

        while (!pq.isEmpty()) {
            State current = pq.poll();

            int node = current.node;
            long currentDist = current.dist;

            // If this entry is not the latest best distance for this node,
            // we ignore it. This is a standard Dijkstra optimization.
            if (currentDist != dist[node]) {
                continue;
            }

            // Explore all outgoing edges from the current node.
            for (Edge edge : graph.get(node)) {
                int next = edge.to;
                long newDist = currentDist + edge.cost;

                // If going through the current node gives a better path to 'next',
                // update and push the improved state.
                if (newDist < dist[next]) {
                    dist[next] = newDist;
                    pq.offer(new State(next, newDist));
                }
            }
        }

        return dist;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 11
     * Example 2 -> -1
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O((n + lanes.length) log n) per demonstration call
     * Space complexity: O(n + lanes.length)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int n1 = 6;
        int[][] lanes1 = {
            {0, 2, 2},
            {1, 2, 4},
            {2, 3, 3},
            {2, 4, 1},
            {4, 3, 1},
            {3, 5, 2},
            {4, 5, 5}
        };
        int aliceStart1 = 0;
        int bobStart1 = 1;
        int office1 = 5;

        long result1 = solution.minimumSharedShuttleCost(n1, lanes1, aliceStart1, bobStart1, office1);
        System.out.println(result1); // Expected: 11

        int n2 = 5;
        int[][] lanes2 = {
            {0, 1, 3},
            {1, 4, 4},
            {2, 3, 2}
        };
        int aliceStart2 = 0;
        int bobStart2 = 2;
        int office2 = 4;

        long result2 = solution.minimumSharedShuttleCost(n2, lanes2, aliceStart2, bobStart2, office2);
        System.out.println(result2); // Expected: -1
    }
}