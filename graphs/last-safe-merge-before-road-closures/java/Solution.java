import java.util.*;

/*
 * Title: Last Safe Merge Before Road Closures
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Problem Description:
 * A delivery company operates in a city with one-way roads. There are n intersections labeled
 * from 0 to n - 1 and m directed roads. Every road is described by three integers [u, v, closeTime],
 * meaning a driver can travel from intersection u to intersection v only if they start using that road
 * at a time strictly less than closeTime. Traveling along any road always takes exactly 1 minute.
 *
 * Two drivers start at the same time 0 from different intersections s1 and s2. They want to meet
 * as early as possible at some intersection x such that both drivers can reach x while respecting
 * all road closing times. If multiple intersections can be used for the earliest possible meeting time,
 * return the smallest intersection index. If no meeting is possible, return -1.
 *
 * Important details:
 * - A driver may wait at any intersection for any amount of time.
 * - A road with closeTime = t can be entered only at times 0, 1, ..., t - 1.
 * - Since each road takes 1 minute, arriving later can make some future roads unusable.
 * - The meeting happens when both drivers have arrived at the same intersection; one driver may arrive earlier and wait.
 *
 * Constraints:
 * - 2 <= n <= 100000
 * - 0 <= m <= 200000
 * - 0 <= s1, s2 < n
 * - s1 != s2
 * - 0 <= closeTime <= 1000000000
 * - There may be multiple roads between the same pair of intersections.
 * - Self-loops may appear.
 *
 * Key observation:
 * Because waiting is allowed, reaching a node earlier is never worse than reaching it later.
 * Therefore, for each start node, we only need the earliest arrival time to every intersection.
 *
 * Transition rule:
 * If we are at node u at time dist[u], then we may use edge (u -> v, closeTime) only when:
 *     dist[u] < closeTime
 * because the road must be entered strictly before it closes.
 * If usable, arrival time at v becomes:
 *     dist[u] + 1
 *
 * Since every road takes exactly 1 minute, this is a shortest-path problem with a feasibility
 * condition on each edge. A standard BFS by number of edges is sufficient because:
 * - all edges have equal travel time 1
 * - waiting never helps produce an earlier arrival to any node
 * - earliest arrival times are exactly the minimum number of traversed edges among feasible paths
 *
 * After computing earliest arrival arrays from s1 and s2:
 * For every node x reachable from both starts, the earliest meeting time at x is:
 *     max(dist1[x], dist2[x])
 * because the earlier driver can wait.
 * We choose the node minimizing that value; if tied, choose the smallest index.
 *
 * Note about the provided Example 2:
 * Under the stated rules, waiting is allowed, so if one driver reaches a node earlier,
 * they may wait for the other. The algorithm below follows the formal problem statement exactly.
 */

public class Solution {

    /**
     * Simple directed edge representation.
     */
    static class Edge {
        int to;
        int closeTime;

        Edge(int to, int closeTime) {
            this.to = to;
            this.closeTime = closeTime;
        }
    }

    /**
     * Computes the meeting intersection that allows the two drivers to meet as early as possible.
     * If multiple intersections yield the same earliest meeting time, the smallest index is returned.
     * If no common reachable intersection exists, returns -1.
     *
     * Time complexity: O(n + m)
     * Space complexity: O(n + m)
     *
     * @param n     number of intersections labeled 0 to n - 1
     * @param roads directed roads, where each road is [u, v, closeTime]
     * @param s1    starting intersection of driver 1
     * @param s2    starting intersection of driver 2
     * @return the best meeting intersection index, or -1 if no meeting is possible
     */
    public int lastSafeMergeBeforeRoadClosures(int n, int[][] roads, int s1, int s2) {
        List<Edge>[] graph = buildGraph(n, roads);

        // Compute earliest arrival times from each starting point.
        int[] dist1 = earliestArrivalTimes(n, graph, s1);
        int[] dist2 = earliestArrivalTimes(n, graph, s2);

        int answerNode = -1;
        long bestMeetingTime = Long.MAX_VALUE;

        // Examine every intersection as a possible meeting point.
        for (int node = 0; node < n; node++) {
            // Both drivers must be able to reach this node.
            if (dist1[node] == -1 || dist2[node] == -1) {
                continue;
            }

            // Since waiting is allowed, the meeting time is the later of the two arrivals.
            long meetingTime = Math.max(dist1[node], dist2[node]);

            // We want the earliest possible meeting time.
            // If tied, choose the smaller node index.
            if (meetingTime < bestMeetingTime || (meetingTime == bestMeetingTime && node < answerNode)) {
                bestMeetingTime = meetingTime;
                answerNode = node;
            }
        }

        return answerNode;
    }

    /**
     * Builds the adjacency list for the directed graph.
     *
     * Time complexity: O(n + m)
     * Space complexity: O(n + m)
     *
     * @param n     number of intersections
     * @param roads road list, where each road is [u, v, closeTime]
     * @return adjacency list graph
     */
    public List<Edge>[] buildGraph(int n, int[][] roads) {
        @SuppressWarnings("unchecked")
        List<Edge>[] graph = new ArrayList[n];

        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();
        }

        for (int[] road : roads) {
            int u = road[0];
            int v = road[1];
            int closeTime = road[2];
            graph[u].add(new Edge(v, closeTime));
        }

        return graph;
    }

    /**
     * Computes the earliest arrival time from a given start node to every node in the graph,
     * respecting the road closing constraints.
     *
     * Why BFS works:
     * - Every road traversal costs exactly 1 minute.
     * - We only care about earliest arrival times.
     * - Waiting never improves earliest arrival to a node.
     * - Therefore, once a node is first reached in BFS, that time is its minimum feasible arrival time.
     *
     * Detailed transition:
     * Suppose we pop node u from the queue and dist[u] = t.
     * For every outgoing edge (u -> v, closeTime):
     * - We may enter this road only if t < closeTime.
     * - If allowed, we would arrive at v at time t + 1.
     * - If v has not been visited before, then t + 1 is its earliest arrival time.
     *
     * Time complexity: O(n + m)
     * Space complexity: O(n)
     *
     * @param n     number of intersections
     * @param graph adjacency list of the graph
     * @param start starting intersection
     * @return array dist where dist[i] is earliest arrival time to i, or -1 if unreachable
     */
    public int[] earliestArrivalTimes(int n, List<Edge>[] graph, int start) {
        int[] dist = new int[n];
        Arrays.fill(dist, -1);

        ArrayDeque<Integer> queue = new ArrayDeque<>();

        // Starting node is reached at time 0.
        dist[start] = 0;
        queue.offer(start);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            int currentTime = dist[u];

            // Explore every outgoing road from u.
            for (Edge edge : graph[u]) {
                int v = edge.to;
                int closeTime = edge.closeTime;

                // VERY IMPORTANT:
                // The driver must START using the road at a time strictly less than closeTime.
                // Since we are at node u at time currentTime, we can use this road only if:
                //     currentTime < closeTime
                if (currentTime >= closeTime) {
                    continue;
                }

                int nextTime = currentTime + 1;

                // Because BFS processes nodes in nondecreasing time order and every edge cost is 1,
                // the first time we assign dist[v] is guaranteed to be the earliest feasible arrival.
                if (dist[v] == -1) {
                    dist[v] = nextTime;
                    queue.offer(v);
                }
            }
        }

        return dist;
    }

    /**
     * Demonstrates the solution on sample inputs and a few additional checks.
     *
     * Time complexity: O(total n + total m) across the demonstrated test cases
     * Space complexity: O(total n + total m) for the built graphs and BFS structures
     *
     * @param args command-line arguments (unused)
     * @return nothing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int n1 = 5;
        int[][] roads1 = {
                {0, 2, 3},
                {1, 2, 2},
                {2, 3, 5},
                {1, 4, 10},
                {4, 3, 10}
        };
        int s1a = 0;
        int s2a = 1;
        System.out.println(solution.lastSafeMergeBeforeRoadClosures(n1, roads1, s1a, s2a)); // Expected: 2

        int n2 = 4;
        int[][] roads2 = {
                {0, 2, 1},
                {2, 3, 2},
                {1, 3, 1}
        };
        int s1b = 0;
        int s2b = 1;
        // According to the formal rules with waiting allowed:
        // Driver 1: 0 -> 2 at time 0, arrives 1; 2 -> 3 at time 1, arrives 2
        // Driver 2: 1 -> 3 at time 0, arrives 1; waits until time 2
        // So they can meet at node 3, and the correct output under the stated rules is 3.
        System.out.println(solution.lastSafeMergeBeforeRoadClosures(n2, roads2, s1b, s2b)); // Formal-rules result: 3

        int n3 = 3;
        int[][] roads3 = {
                {0, 1, 1},
                {1, 2, 1}
        };
        int s1c = 0;
        int s2c = 2;
        System.out.println(solution.lastSafeMergeBeforeRoadClosures(n3, roads3, s1c, s2c)); // Expected: -1

        int n4 = 4;
        int[][] roads4 = {
                {0, 2, 5},
                {1, 2, 5},
                {0, 3, 5},
                {1, 3, 5}
        };
        int s1d = 0;
        int s2d = 1;
        // Both 2 and 3 are reachable at meeting time 1, choose smaller index 2.
        System.out.println(solution.lastSafeMergeBeforeRoadClosures(n4, roads4, s1d, s2d)); // Expected: 2
    }
}