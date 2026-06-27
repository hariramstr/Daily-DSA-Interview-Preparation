import java.util.*;

/*
 * Latest Safe Departure in a Flooded Transit Graph
 *
 * Problem Description:
 * A city transit authority models its rail system as an undirected graph with n stations labeled
 * 0 to n - 1 and m bidirectional tunnels. Station 0 is your starting point and station n - 1 is
 * the central shelter. A flood spreads through the network over time. You are given an array
 * floodTime where floodTime[i] is the earliest integer minute when station i becomes unusable.
 * You may stand at or enter station i only at times strictly less than floodTime[i].
 * Traversing any tunnel takes exactly 1 minute, and you may wait at a station for any nonnegative
 * number of whole minutes as long as the station is still usable during the entire wait.
 *
 * Your task is to compute the latest integer minute t at which you can start from station 0 and
 * still reach station n - 1 without ever being in a flooded station. If it is impossible to reach
 * the shelter even when starting at time 0, return -1. If you can delay your departure arbitrarily
 * long and still eventually reach the shelter, return 1000000000.
 *
 * A path is valid only if for every station visited, including the start and destination, your
 * arrival time is strictly smaller than that station's flood time. For waiting, if you stay at
 * station u from time x to x + k, then all times before departure must remain strictly less than
 * floodTime[u].
 *
 * Constraints:
 * - 2 <= n <= 200000
 * - 1 <= m <= 300000
 * - edges.length == m
 * - each edge is [u, v], where 0 <= u, v < n and u != v
 * - there are no self-loops, but multiple edges may exist
 * - 1 <= floodTime[i] <= 1000000000
 *
 * Key Insight:
 * Waiting never helps after departure.
 * If you are already at some station u at time cur, then leaving later only makes every future
 * arrival time later as well, which can never improve strict deadlines of the form arrival < floodTime[v].
 * Therefore, for a fixed start time t, feasibility is equivalent to asking whether there exists a
 * path from 0 to n - 1 such that for every vertex reached at path distance d from 0 along that path:
 *
 *     t + d < floodTime[vertex]
 *
 * So for each vertex v, if dist[v] is the minimum number of edges from 0 to v, then the earliest
 * possible arrival at v after starting at time t is t + dist[v]. Any later arrival is worse.
 * Hence we only need shortest distances from 0, but only through vertices that are still usable
 * when reached.
 *
 * This yields a monotone decision problem:
 * - If starting at time t is feasible, then any earlier start time is also feasible.
 * - Therefore we can binary search the latest feasible t.
 *
 * We also need to detect the "infinite delay" case:
 * If there exists a path from 0 to n - 1 using only vertices with floodTime = 1_000_000_000,
 * then for any start time t <= 1_000_000_000 we can simply wait at station 0 and traverse that path.
 * Under the problem's required output convention, we return 1_000_000_000.
 *
 * Important note about Example 1:
 * The textual explanation in the prompt concludes the latest safe departure is 1, while the stated
 * output says 2. Under the strict inequality rule, starting at time 2 is NOT feasible. This
 * implementation follows the actual rules and therefore returns 1 for that example.
 */

public class Solution {

    private static final int INF_ANSWER = 1_000_000_000;

    /**
     * Computes the latest integer minute at which one can start from station 0 and still reach
     * station n - 1 before any visited station floods.
     *
     * The algorithm works in three stages:
     * 1) Build an adjacency list for the undirected graph.
     * 2) Check whether starting at time 0 is impossible. If so, return -1 immediately.
     * 3) Check whether departure can be delayed arbitrarily long under the problem's output
     *    convention. If so, return 1_000_000_000.
     * 4) Otherwise, binary search the largest start time t for which reachability is still possible.
     *
     * @param n number of stations
     * @param edges undirected tunnels, where each element is [u, v]
     * @param floodTime earliest minute when each station becomes unusable; station i is usable only at times < floodTime[i]
     * @return the latest safe departure time, or -1 if impossible even at time 0, or 1_000_000_000 if delay can be arbitrarily large per the problem convention
     *
     * Time complexity: O((n + m) log 1_000_000_000)
     * Space complexity: O(n + m)
     */
    public int latestSafeDeparture(int n, int[][] edges, int[] floodTime) {
        int[][] graph = buildGraph(n, edges);

        // If station 0 is already unusable at time 0, we cannot even begin.
        // Since floodTime[i] >= 1 by constraints, this specific case does not occur,
        // but keeping the check makes the logic complete and beginner-friendly.
        if (floodTime[0] <= 0) {
            return -1;
        }

        // First, verify whether any valid route exists when starting immediately at time 0.
        // If not, the answer is definitely -1.
        if (!canReachWithStartTime(graph, floodTime, 0)) {
            return -1;
        }

        // Next, detect the special "arbitrarily long delay" case required by the statement.
        // Because all flood times are at most 1e9, the only way to keep delaying up to the
        // required sentinel answer is that every station on some valid path remains usable
        // for all start times in the searched range, i.e. has floodTime == 1e9.
        if (canReachOnlyThroughInfiniteStations(graph, floodTime)) {
            return INF_ANSWER;
        }

        // Binary search on the answer.
        // Feasibility is monotone:
        // - If we can start at time t, then we can also start at any earlier time.
        // Therefore the set of feasible start times is a prefix [0..answer].
        int low = 0;
        int high = INF_ANSWER - 1; // If the answer were INF_ANSWER, we would have returned above.
        int ans = 0;

        while (low <= high) {
            int mid = low + (high - low) / 2;

            if (canReachWithStartTime(graph, floodTime, mid)) {
                ans = mid;
                low = mid + 1; // Try to start even later.
            } else {
                high = mid - 1; // Need to start earlier.
            }
        }

        return ans;
    }

    /**
     * Decides whether the shelter can be reached when departure happens exactly at time startTime.
     *
     * Core reasoning:
     * - Every edge traversal costs exactly 1 minute.
     * - Waiting after departure is never beneficial, because it only delays all future arrivals.
     * - Therefore, for a fixed start time, the best possible arrival time at each station is found
     *   by a standard BFS measured in number of edges from station 0.
     * - We may enter station v at time arrival only if arrival < floodTime[v].
     *
     * This method performs a BFS while carrying the current arrival time implicitly as:
     *     arrivalTime = startTime + distanceFromStart
     *
     * A neighbor is enqueued only if it is still usable at that arrival time.
     *
     * @param graph adjacency list in compact int[][] form
     * @param floodTime flood deadlines for stations
     * @param startTime chosen departure time from station 0
     * @return true if there exists a valid route to station n - 1, otherwise false
     *
     * Time complexity: O(n + m)
     * Space complexity: O(n)
     */
    public boolean canReachWithStartTime(int[][] graph, int[] floodTime, int startTime) {
        int n = graph.length;

        // You must be able to stand at station 0 at the departure time itself.
        if (startTime >= floodTime[0]) {
            return false;
        }

        // Standard BFS setup.
        boolean[] visited = new boolean[n];
        int[] queue = new int[n];
        int head = 0;
        int tail = 0;

        visited[0] = true;
        queue[tail++] = 0;

        // distanceLayer represents the number of edges used from station 0 to all nodes
        // currently in this BFS layer.
        int distanceLayer = 0;

        while (head < tail) {
            int layerSize = tail - head;

            // All nodes currently in the queue segment [head, tail) are reached in exactly
            // 'distanceLayer' edge traversals, so their arrival time is:
            //     startTime + distanceLayer
            int currentArrival = startTime + distanceLayer;

            for (int i = 0; i < layerSize; i++) {
                int u = queue[head++];

                // If we reached the shelter, then by construction the arrival time at u
                // already satisfied the strict flood constraint when it was enqueued.
                if (u == n - 1) {
                    return true;
                }

                // Explore all neighbors. Reaching a neighbor takes one more minute.
                int nextArrival = currentArrival + 1;

                for (int v : graph[u]) {
                    if (visited[v]) {
                        continue;
                    }

                    // Strict inequality is essential:
                    // we may enter station v only if nextArrival < floodTime[v].
                    if (nextArrival < floodTime[v]) {
                        visited[v] = true;
                        queue[tail++] = v;
                    }
                }
            }

            distanceLayer++;
        }

        return false;
    }

    /**
     * Checks whether there exists a path from station 0 to station n - 1 using only stations whose
     * flood time equals 1_000_000_000.
     *
     * Why this detects the required "infinite delay" answer:
     * - To return 1_000_000_000, the problem asks us to identify cases where we can keep delaying
     *   departure arbitrarily long under its output convention.
     * - Since all flood times are bounded above by 1_000_000_000, any station with a smaller flood
     *   time would eventually become unusable before sufficiently large departures.
     * - Therefore a path that remains valid for all large allowed start times must consist entirely
     *   of stations with floodTime = 1_000_000_000.
     *
     * @param graph adjacency list
     * @param floodTime flood deadlines
     * @return true if such an all-1e9 path exists, otherwise false
     *
     * Time complexity: O(n + m)
     * Space complexity: O(n)
     */
    public boolean canReachOnlyThroughInfiniteStations(int[][] graph, int[] floodTime) {
        int n = graph.length;

        if (floodTime[0] != INF_ANSWER || floodTime[n - 1] != INF_ANSWER) {
            return false;
        }

        boolean[] visited = new boolean[n];
        int[] queue = new int[n];
        int head = 0;
        int tail = 0;

        visited[0] = true;
        queue[tail++] = 0;

        while (head < tail) {
            int u = queue[head++];

            if (u == n - 1) {
                return true;
            }

            for (int v : graph[u]) {
                if (!visited[v] && floodTime[v] == INF_ANSWER) {
                    visited[v] = true;
                    queue[tail++] = v;
                }
            }
        }

        return false;
    }

    /**
     * Builds a compact adjacency list representation for an undirected graph.
     *
     * The returned structure is an int[][] where graph[u] contains all neighbors of u.
     * This is memory-efficient and fast for repeated BFS traversals during binary search.
     *
     * @param n number of vertices
     * @param edges undirected edges
     * @return adjacency list as a compact 2D int array
     *
     * Time complexity: O(n + m)
     * Space complexity: O(n + m)
     */
    public int[][] buildGraph(int n, int[][] edges) {
        int[] degree = new int[n];

        // First pass: count degrees so we know exactly how large each adjacency array must be.
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            degree[u]++;
            degree[v]++;
        }

        int[][] graph = new int[n][];
        for (int i = 0; i < n; i++) {
            graph[i] = new int[degree[i]];
        }

        // We reuse degree[] as write pointers by resetting it to zero.
        Arrays.fill(degree, 0);

        // Second pass: fill adjacency arrays.
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            graph[u][degree[u]++] = v;
            graph[v][degree[v]++] = u;
        }

        return graph;
    }

    /**
     * Demonstrates the solution on sample-style inputs and prints the results.
     *
     * Note:
     * The first sample's written explanation and stated output conflict.
     * Under the strict rule "arrival time must be strictly less than flood time",
     * the correct answer for that sample is 1, not 2.
     *
     * @param args command-line arguments, unused
     * @return nothing
     *
     * Time complexity: O((n + m) log 1_000_000_000) for each demonstration call
     * Space complexity: O(n + m)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        int n1 = 5;
        int[][] edges1 = {
            {0, 1}, {1, 2}, {2, 4}, {0, 3}, {3, 4}
        };
        int[] flood1 = {4, 3, 5, 2, 6};
        System.out.println(sol.latestSafeDeparture(n1, edges1, flood1)); // Correct under strict inequality: 1

        int n2 = 4;
        int[][] edges2 = {
            {0, 1}, {1, 2}, {2, 3}, {0, 2}
        };
        int[] flood2 = {1000000000, 1000000000, 1000000000, 1000000000};
        System.out.println(sol.latestSafeDeparture(n2, edges2, flood2)); // 1000000000

        int n3 = 3;
        int[][] edges3 = {
            {0, 1}, {1, 2}
        };
        int[] flood3 = {1, 1, 10};
        System.out.println(sol.latestSafeDeparture(n3, edges3, flood3)); // -1
    }
}