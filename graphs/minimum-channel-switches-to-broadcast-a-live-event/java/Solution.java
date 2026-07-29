import java.util.*;

/*
 * Title: Minimum Channel Switches to Broadcast a Live Event
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Problem Description:
 * A media company needs to send a live video stream from studio 0 to studio n - 1
 * through a network of relay stations. Each directed connection between stations is
 * labeled with a channel ID, representing the transmission channel used on that link.
 * Moving along an edge is always possible, but every time the stream uses a link whose
 * channel ID is different from the previous link's channel ID, engineers must perform
 * a costly channel switch.
 *
 * Your task is to compute the minimum number of channel switches required to send the
 * stream from node 0 to node n - 1. The first chosen edge does not count as a switch,
 * because no channel is active before transmission begins. If it is impossible to reach
 * node n - 1, return -1.
 *
 * Formally, you are given an integer n and a list edges where each element is [u, v, c],
 * meaning there is a directed edge from u to v using channel c. A path may revisit nodes,
 * but you want the minimum possible number of channel changes along any valid path from
 * 0 to n - 1.
 *
 * This is a graph shortest-path problem where the state must include both the current node
 * and the channel used to arrive there. A solution that only tracks nodes may miss the
 * optimal answer.
 *
 * Constraints:
 * - 2 <= n <= 100000
 * - 1 <= edges.length <= 200000
 * - 0 <= u, v < n
 * - u != v
 * - 1 <= c <= 1000000000
 * - Multiple edges between the same pair of nodes may exist with different channel IDs.
 *
 * Example 1:
 * Input: n = 5, edges = [[0,1,7],[1,2,7],[2,4,7],[0,3,2],[3,4,2],[1,3,5]]
 * Output: 0
 * Explanation: One optimal path is 0 -> 1 -> 2 -> 4, and every edge uses channel 7,
 * so no switch is needed.
 *
 * Example 2:
 * Input: n = 6, edges = [[0,1,1],[1,2,2],[2,5,2],[0,3,3],[3,4,4],[4,5,5],[1,4,1]]
 * Output: 1
 * Explanation: Path 0 -> 1 -> 4 -> 5 uses channels 1, 1, 5, so there is exactly one
 * switch when moving from channel 1 to channel 5. Other valid paths require at least
 * two switches.
 */

public class Solution {

    /**
     * Simple directed edge in the original graph.
     */
    private static class Edge {
        int to;
        int color;

        Edge(int to, int color) {
            this.to = to;
            this.color = color;
        }
    }

    /**
     * State used in 0-1 BFS:
     * we are currently at "node" and the last channel used to arrive here is "color".
     *
     * The total cost stored externally is the minimum number of switches needed
     * to reach this exact (node, color) state.
     */
    private static class State {
        int node;
        int color;

        State(int node, int color) {
            this.node = node;
            this.color = color;
        }
    }

    /**
     * Computes the minimum number of channel switches needed to travel from node 0
     * to node n - 1 in a directed graph whose edges are labeled by channel IDs.
     *
     * Core idea:
     * A path cost depends not only on the current node, but also on the channel used
     * on the previous edge. Therefore, we must treat (node, lastChannel) as the true
     * shortest-path state.
     *
     * Efficient modeling trick:
     * Instead of connecting every state to every other state at the same node directly,
     * we build an implicit bipartite-style graph:
     *
     * 1. For each original node u, create a "station node" U.
     * 2. For each distinct pair (u, channel c) that appears on an incoming or outgoing
     *    edge of u, create a "state node" S(u, c).
     * 3. Add edges:
     *      U -> S(u, c) with cost 1   (starting to use / switching to channel c at node u)
     *      S(u, c) -> U with cost 0   (being at node u while already on channel c)
     * 4. For each original directed edge u -> v with channel c:
     *      S(u, c) -> S(v, c) with cost 0
     *
     * Why this works:
     * - Moving along edges with the same channel costs 0.
     * - Changing channel at a node costs 1.
     * - The first chosen edge should not count as a switch, so we start from station node 0
     *   with distance -1. Then the first transition 0 -> S(0, c) adds +1, bringing the
     *   total to 0 exactly as required.
     *
     * We then run 0-1 BFS because all edge weights are only 0 or 1.
     *
     * @param n the number of nodes in the graph
     * @param edges the directed edges, where each element is [u, v, c]
     * @return the minimum number of channel switches from node 0 to node n - 1, or -1 if unreachable
     * Time complexity: O((n + m + k) + totalConstructedEdges), which is O(n + m + k),
     * where m is the number of original edges and k is the number of distinct (node, channel) pairs.
     * Since each original edge contributes constant work and each distinct state contributes constant work,
     * this is effectively linear in input size.
     * Space complexity: O(n + m + k) for adjacency structures, state mapping, and BFS distance storage.
     */
    public int minChannelSwitches(int n, int[][] edges) {
        if (n == 1) {
            return 0;
        }

        List<List<Edge>> graph = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }

        /*
         * For each node, we need to know which channel IDs are relevant at that node.
         * A channel is relevant at node u if:
         * - there is an outgoing edge from u with that channel, or
         * - there is an incoming edge to u with that channel.
         *
         * Why both?
         * Because a state (u, c) means "I am at node u and the last used channel is c".
         * Such a state can be reached by arriving on channel c, and from there we may:
         * - continue on outgoing edges of the same channel at zero cost, or
         * - switch to another channel at cost 1.
         */
        List<Set<Integer>> channelsPerNode = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            channelsPerNode.add(new HashSet<>());
        }

        for (int[] e : edges) {
            int u = e[0];
            int v = e[1];
            int c = e[2];
            graph.get(u).add(new Edge(v, c));
            channelsPerNode.get(u).add(c);
            channelsPerNode.get(v).add(c);
        }

        /*
         * We now assign integer IDs to all constructed nodes in the transformed graph.
         *
         * IDs 0..n-1 are reserved for the original station nodes.
         * Then we assign IDs to each distinct state node (u, c).
         */
        int nextId = n;
        List<Map<Integer, Integer>> stateId = new ArrayList<>(n);
        for (int u = 0; u < n; u++) {
            Map<Integer, Integer> map = new HashMap<>();
            for (int c : channelsPerNode.get(u)) {
                map.put(c, nextId++);
            }
            stateId.add(map);
        }

        /*
         * Build adjacency list for the transformed graph.
         * Each edge has weight 0 or 1, so we store destination and weight.
         */
        List<List<int[]>> transformed = new ArrayList<>(nextId);
        for (int i = 0; i < nextId; i++) {
            transformed.add(new ArrayList<>());
        }

        /*
         * Add:
         * station node u -> state node (u, c) with cost 1
         * state node (u, c) -> station node u with cost 0
         *
         * Interpretation:
         * - Going from station node to a channel-state means we decide to use that channel now.
         *   This costs 1, representing a channel selection/switch.
         * - Going back from a channel-state to the station node costs 0, meaning we are physically
         *   at the same original node and can consider switching if needed.
         */
        for (int u = 0; u < n; u++) {
            for (int c : channelsPerNode.get(u)) {
                int sid = stateId.get(u).get(c);
                transformed.get(u).add(new int[]{sid, 1});
                transformed.get(sid).add(new int[]{u, 0});
            }
        }

        /*
         * For each original directed edge u -> v with channel c,
         * add transformed edge:
         *   (u, c) -> (v, c) with cost 0
         *
         * This means if we are already using channel c, traversing an edge with the same
         * channel does not require a switch.
         */
        for (int[] e : edges) {
            int u = e[0];
            int v = e[1];
            int c = e[2];
            int fromState = stateId.get(u).get(c);
            int toState = stateId.get(v).get(c);
            transformed.get(fromState).add(new int[]{toState, 0});
        }

        /*
         * 0-1 BFS setup.
         *
         * Important trick for the first edge:
         * The first chosen edge should NOT count as a switch.
         * But our model charges +1 when moving from station node u to state node (u, c).
         *
         * To cancel that first +1, we start with distance[0] = -1.
         * Then:
         *   station 0 -> state (0, firstChannel) costs +1
         * so total becomes 0, exactly matching the problem statement.
         */
        int[] dist = new int[nextId];
        Arrays.fill(dist, Integer.MAX_VALUE);

        Deque<Integer> deque = new ArrayDeque<>();
        dist[0] = -1;
        deque.addFirst(0);

        while (!deque.isEmpty()) {
            int cur = deque.pollFirst();
            int curDist = dist[cur];

            /*
             * Standard 0-1 BFS relaxation:
             * - weight 0 edges go to the front
             * - weight 1 edges go to the back
             *
             * This guarantees nodes are processed in nondecreasing distance order,
             * similar to Dijkstra but faster for binary weights.
             */
            for (int[] edge : transformed.get(cur)) {
                int next = edge[0];
                int weight = edge[1];
                int newDist = curDist + weight;

                if (newDist < dist[next]) {
                    dist[next] = newDist;
                    if (weight == 0) {
                        deque.addFirst(next);
                    } else {
                        deque.addLast(next);
                    }
                }
            }
        }

        /*
         * The answer for reaching original node n - 1 can appear in two ways:
         *
         * 1. dist[n - 1]:
         *    This means we reached the station node directly in the transformed graph.
         *    That can happen from some state node (n - 1, c) -> station node (n - 1) with cost 0.
         *
         * 2. Some state node dist[(n - 1, c)]:
         *    In practice dist[n - 1] will be at most those values because of the zero-cost edge
         *    back to the station node, but checking both is harmless and beginner-friendly.
         */
        int answer = dist[n - 1];

        for (int c : channelsPerNode.get(n - 1)) {
            int sid = stateId.get(n - 1).get(c);
            answer = Math.min(answer, dist[sid]);
        }

        return answer == Integer.MAX_VALUE ? -1 : answer;
    }

    /**
     * Convenience wrapper that accepts a list of edges.
     *
     * @param n the number of nodes in the graph
     * @param edgesList list of edges where each edge is [u, v, c]
     * @return the minimum number of channel switches from node 0 to node n - 1, or -1 if unreachable
     * Time complexity: O(n + m + k), same as the main method after converting the list to an array.
     * Space complexity: O(n + m + k).
     */
    public int minChannelSwitches(int n, List<int[]> edgesList) {
        int[][] edges = new int[edgesList.size()][3];
        for (int i = 0; i < edgesList.size(); i++) {
            edges[i] = edgesList.get(i);
        }
        return minChannelSwitches(n, edges);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 0
     * Example 2 -> 1
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total size of demonstrated examples).
     * Space complexity: O(total size of demonstrated examples).
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int n1 = 5;
        int[][] edges1 = {
            {0, 1, 7},
            {1, 2, 7},
            {2, 4, 7},
            {0, 3, 2},
            {3, 4, 2},
            {1, 3, 5}
        };
        int result1 = solution.minChannelSwitches(n1, edges1);
        System.out.println(result1); // Expected: 0

        int n2 = 6;
        int[][] edges2 = {
            {0, 1, 1},
            {1, 2, 2},
            {2, 5, 2},
            {0, 3, 3},
            {3, 4, 4},
            {4, 5, 5},
            {1, 4, 1}
        };
        int result2 = solution.minChannelSwitches(n2, edges2);
        System.out.println(result2); // Expected: 1

        /*
         * Additional quick unreachable example:
         * There is no path from 0 to 3.
         */
        int n3 = 4;
        int[][] edges3 = {
            {0, 1, 10},
            {1, 2, 10}
        };
        int result3 = solution.minChannelSwitches(n3, edges3);
        System.out.println(result3); // Expected: -1
    }
}