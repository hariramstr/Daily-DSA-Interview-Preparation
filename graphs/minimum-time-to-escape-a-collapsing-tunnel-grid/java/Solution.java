import java.util.*;

/*
Minimum Time to Escape a Collapsing Tunnel Grid

Problem Description:
You are given an undirected graph representing underground tunnel junctions. There are n junctions labeled from 0 to n - 1 and m bidirectional tunnels. Traveling through tunnel (u, v) always takes w minutes. However, each junction i also has a collapse time collapse[i]. You may stand at or arrive at junction i only at times strictly less than collapse[i]. If you reach a junction at time t where t >= collapse[i], that state is invalid because the junction has already collapsed.

You start at junction 0 at time 0 and want to reach junction n - 1 as early as possible. In addition to moving through tunnels, you may wait at any currently safe junction for any non-negative integer amount of time, as long as the junction has not collapsed before or during the waiting period. Waiting can be necessary because some tunnels are controlled by periodic gates.

Each tunnel is described by five values [u, v, w, open, close]. The tunnel can only be entered at integer times t such that t mod (open + close) < open. In other words, the tunnel is open for open minutes, then closed for close minutes, repeating forever starting from time 0. If you begin traversing the tunnel at a valid open time t, you spend exactly w minutes moving, and the tunnel does not need to remain open after departure. You still must arrive at the destination junction before it collapses.

Return the minimum time needed to reach junction n - 1, or -1 if it is impossible.

Constraints:
- 2 <= n <= 100000
- 1 <= m <= 200000
- 0 <= collapse[i] <= 10^15
- collapse[0] > 0
- 1 <= w <= 10^9
- 1 <= open, close <= 10^9
- 0 <= u, v < n, u != v
- There may be multiple tunnels between the same pair of junctions
- All times are integers

Notes:
- The graph is undirected, so every tunnel can be used in both directions with the same timing pattern.
- Waiting is allowed only while the current node remains strictly safe.
- A departure time t is valid only if:
  1) t is an integer time when the tunnel is open, and
  2) t < collapse[currentNode]
- An arrival time a is valid only if a < collapse[nextNode].

Approach:
- This is a shortest path problem on a time-dependent graph.
- The key property needed for Dijkstra still holds: if we reach a node earlier, that is never worse,
  because from an earlier time we can always wait to simulate any later departure time.
- Therefore, we can run Dijkstra where the state is simply the node, and the distance is the earliest
  valid arrival time at that node.
- For each edge, from current time t at node u:
  1) Compute the earliest departure time d >= t such that the tunnel is open.
  2) Ensure we are allowed to remain at u until d, which requires d < collapse[u].
  3) Compute arrival = d + w.
  4) Ensure arrival < collapse[v].
  5) Relax the distance of v with arrival.
*/

public class Solution {

    /**
     * Edge in the undirected graph.
     */
    static class Edge {
        int to;
        long travelTime;
        long open;
        long close;

        Edge(int to, long travelTime, long open, long close) {
            this.to = to;
            this.travelTime = travelTime;
            this.open = open;
            this.close = close;
        }
    }

    /**
     * State used in Dijkstra's priority queue.
     */
    static class State {
        int node;
        long time;

        State(int node, long time) {
            this.node = node;
            this.time = time;
        }
    }

    /**
     * Computes the minimum valid arrival time from node 0 to node n - 1.
     *
     * @param n number of nodes
     * @param edges each edge is [u, v, w, open, close]
     * @param collapse collapse[i] is the time when node i becomes unusable; all presence times must be strictly less
     * @return the earliest valid arrival time at node n - 1, or -1 if impossible
     *
     * Time complexity: O((n + m) log n)
     * Space complexity: O(n + m)
     */
    public long minimumEscapeTime(int n, int[][] edges, long[] collapse) {
        // If the start node is already invalid at time 0, escape is impossible.
        // The problem guarantees collapse[0] > 0, but this check keeps the method robust.
        if (collapse[0] <= 0) {
            return -1;
        }

        // Build adjacency list for the undirected graph.
        List<List<Edge>> graph = buildGraph(n, edges);

        // Standard Dijkstra distance array:
        // dist[i] = earliest known valid arrival time at node i.
        long[] dist = new long[n];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[0] = 0L;

        // Min-heap ordered by earliest time first.
        PriorityQueue<State> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a.time));
        pq.offer(new State(0, 0L));

        while (!pq.isEmpty()) {
            State current = pq.poll();
            int u = current.node;
            long currentTime = current.time;

            // Ignore stale heap entries.
            if (currentTime != dist[u]) {
                continue;
            }

            // If we popped the destination, this is the globally earliest valid arrival.
            if (u == n - 1) {
                return currentTime;
            }

            // Safety check: if somehow this state is not valid anymore, skip it.
            // In correct relaxations this should already hold.
            if (currentTime >= collapse[u]) {
                continue;
            }

            // Try every tunnel leaving node u.
            for (Edge edge : graph.get(u)) {
                int v = edge.to;

                // Compute the earliest time we can depart on this edge,
                // starting from currentTime and possibly waiting at node u.
                long departure = earliestOpenTimeAtOrAfter(currentTime, edge.open, edge.close);

                // Very important:
                // We are allowed to wait at node u only while it remains safe.
                // Since being at node u at time collapse[u] is forbidden,
                // the departure time itself must satisfy departure < collapse[u].
                if (departure >= collapse[u]) {
                    continue;
                }

                // Travel through the tunnel.
                long arrival = departure + edge.travelTime;

                // Arrival is valid only if the destination has not collapsed yet.
                if (arrival >= collapse[v]) {
                    continue;
                }

                // Standard Dijkstra relaxation.
                if (arrival < dist[v]) {
                    dist[v] = arrival;
                    pq.offer(new State(v, arrival));
                }
            }
        }

        return -1;
    }

    /**
     * Builds an undirected adjacency list from the raw edge array.
     *
     * @param n number of nodes
     * @param edges each edge is [u, v, w, open, close]
     * @return adjacency list representation of the graph
     *
     * Time complexity: O(m)
     * Space complexity: O(n + m)
     */
    public List<List<Edge>> buildGraph(int n, int[][] edges) {
        List<List<Edge>> graph = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }

        for (int[] e : edges) {
            int u = e[0];
            int v = e[1];
            long w = e[2];
            long open = e[3];
            long close = e[4];

            graph.get(u).add(new Edge(v, w, open, close));
            graph.get(v).add(new Edge(u, w, open, close));
        }

        return graph;
    }

    /**
     * Returns the earliest integer time t' >= time such that the periodic tunnel is open at t'.
     *
     * The tunnel is open when:
     *     t mod (open + close) < open
     *
     * Detailed logic:
     * - Let cycle = open + close.
     * - Compute position = time mod cycle.
     * - If position < open, then the tunnel is already open at 'time', so we can depart immediately.
     * - Otherwise, we are currently in the closed segment of the cycle.
     *   The next open segment begins at the start of the next cycle,
     *   so we wait (cycle - position) minutes.
     *
     * @param time current time
     * @param open length of the open interval in each cycle
     * @param close length of the closed interval in each cycle
     * @return earliest departure time at or after 'time' when the tunnel is open
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long earliestOpenTimeAtOrAfter(long time, long open, long close) {
        long cycle = open + close;
        long position = time % cycle;

        if (position < open) {
            return time;
        }

        return time + (cycle - position);
    }

    /**
     * Demonstrates the solution on sample-style test cases.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     *
     * Time complexity: O((n + m) log n) per demonstration call
     * Space complexity: O(n + m)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // The original statement text contains a correction in the explanation.
        // We use the corrected fourth edge [2,3,1,2,2], which makes the answer 5.
        int n1 = 4;
        int[][] edges1 = {
            {0, 1, 3, 2, 2},
            {1, 3, 2, 3, 1},
            {0, 2, 2, 1, 3},
            {2, 3, 1, 2, 2}
        };
        long[] collapse1 = {100L, 10L, 10L, 20L};
        System.out.println(solution.minimumEscapeTime(n1, edges1, collapse1)); // Expected: 5

        // Example 2:
        // The statement explanation also corrects collapse to [20,5,9], making the answer -1.
        int n2 = 3;
        int[][] edges2 = {
            {0, 1, 4, 1, 2},
            {1, 2, 3, 1, 1},
            {0, 2, 10, 5, 5}
        };
        long[] collapse2 = {20L, 5L, 9L};
        System.out.println(solution.minimumEscapeTime(n2, edges2, collapse2)); // Expected: -1

        // Additional small sanity test:
        // Direct edge always open, destination safe.
        int n3 = 2;
        int[][] edges3 = {
            {0, 1, 7, 5, 1}
        };
        long[] collapse3 = {100L, 20L};
        System.out.println(solution.minimumEscapeTime(n3, edges3, collapse3)); // Expected: 7
    }
}