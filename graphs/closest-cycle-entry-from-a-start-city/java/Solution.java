import java.util.*;

/*
 * Title: Closest Cycle Entry from a Start City
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Problem Description:
 * You are given a directed graph representing one-way roads between cities.
 * The graph has n cities labeled from 0 to n - 1 and m directed roads, where each road [u, v]
 * means you can travel from city u to city v. You are also given a starting city s.
 *
 * A city is called a cycle-entry city if it belongs to at least one directed cycle that is reachable from s.
 * Your task is to return the minimum number of roads needed to travel from s to any such cycle-entry city.
 * If multiple cycle-entry cities are reachable at the same minimum distance, return the smallest city index among them.
 * If no directed cycle is reachable from s, return [-1, -1].
 *
 * Return the answer as an array [distance, city]. The distance is the length of the shortest directed path from s
 * to the chosen city.
 *
 * A city belongs to a directed cycle if there exists a path that starts at that city, follows one or more directed edges,
 * and returns to the same city.
 *
 * Constraints:
 * - 1 <= n <= 100000
 * - 0 <= m <= 200000
 * - 0 <= s < n
 * - 0 <= u, v < n
 * - There may be self-loops and multiple edges.
 *
 * Example 1:
 * Input: n = 7, edges = [[0,1],[1,2],[2,3],[3,1],[2,4],[4,5]], s = 0
 * Output: [1,1]
 * Explanation: From city 0, the reachable cycle is 1 -> 2 -> 3 -> 1. Cities 1, 2, and 3 are all on a reachable directed cycle.
 * Their distances from 0 are 1, 2, and 3 respectively, so the answer is [1, 1].
 *
 * Example 2:
 * Input: n = 6, edges = [[0,1],[1,2],[2,4],[4,5],[3,3]], s = 0
 * Output: [-1,-1]
 * Explanation: City 3 has a self-loop, so it forms a cycle, but it is not reachable from city 0.
 * No reachable city from 0 belongs to any directed cycle.
 *
 * Key Idea:
 * A node belongs to a directed cycle if and only if it is inside a strongly connected component (SCC)
 * of size at least 2, or it has a self-loop.
 *
 * Efficient plan:
 * 1. Run BFS from s to find all reachable nodes and their shortest distances from s.
 * 2. Restrict attention to only reachable nodes.
 * 3. Run an SCC algorithm (Tarjan) on the reachable subgraph.
 * 4. Mark every node that belongs to a cycle:
 *    - SCC size >= 2, or
 *    - SCC size == 1 and the node has a self-loop.
 * 5. Among all reachable cycle nodes, choose the one with:
 *    - minimum BFS distance from s
 *    - if tied, minimum node index
 *
 * This gives a linear-time solution in O(n + m).
 */
public class Solution {

    /**
     * Finds the closest reachable city that belongs to at least one directed cycle.
     *
     * The algorithm works in two major phases:
     * 1. Breadth-First Search (BFS) from the start city s:
     *    - This identifies exactly which cities are reachable from s.
     *    - It also computes the shortest distance from s to every reachable city.
     *
     * 2. Strongly Connected Components (SCC) detection on the reachable subgraph:
     *    - In a directed graph, a city belongs to a directed cycle if it is in:
     *      a) an SCC with at least 2 nodes, or
     *      b) a single-node SCC that has a self-loop.
     *    - We use Tarjan's algorithm to find SCCs in linear time.
     *
     * Finally, among all reachable cities that belong to a cycle, we choose:
     * - the one with the smallest BFS distance from s
     * - if multiple have the same distance, the smallest city index
     *
     * @param n the number of cities, labeled from 0 to n - 1
     * @param edges the directed roads, where each element is [u, v] meaning u -> v
     * @param s the starting city
     * @return an array [distance, city], or [-1, -1] if no directed cycle is reachable from s
     * Time complexity: O(n + m)
     * Space complexity: O(n + m)
     */
    public int[] closestCycleEntry(int n, int[][] edges, int s) {
        // Build adjacency list for the directed graph.
        // We use ArrayList<Integer>[] because it is simple, standard, and efficient enough
        // for the given constraints.
        List<Integer>[] graph = buildGraph(n, edges);

        // Step 1: BFS from s.
        // dist[i] = shortest number of edges from s to i, or -1 if i is not reachable.
        int[] dist = bfsDistances(n, graph, s);

        // reachable[i] tells us whether city i can be reached from s.
        boolean[] reachable = new boolean[n];
        for (int i = 0; i < n; i++) {
            reachable[i] = dist[i] != -1;
        }

        // Step 2: Detect which reachable nodes belong to directed cycles.
        boolean[] inCycle = findReachableCycleNodes(n, graph, reachable);

        // Step 3: Scan all cities and choose the best answer according to the rules:
        // - minimum distance
        // - if tie, minimum city index
        int bestDistance = Integer.MAX_VALUE;
        int bestCity = Integer.MAX_VALUE;

        for (int city = 0; city < n; city++) {
            if (reachable[city] && inCycle[city]) {
                if (dist[city] < bestDistance || (dist[city] == bestDistance && city < bestCity)) {
                    bestDistance = dist[city];
                    bestCity = city;
                }
            }
        }

        // If we never found any reachable cycle node, return [-1, -1].
        if (bestDistance == Integer.MAX_VALUE) {
            return new int[] { -1, -1 };
        }

        return new int[] { bestDistance, bestCity };
    }

    /**
     * Builds the adjacency list representation of the directed graph.
     *
     * @param n the number of nodes
     * @param edges the directed edges, each as [u, v]
     * @return adjacency list where graph[u] contains all v such that u -> v
     * Time complexity: O(n + m)
     * Space complexity: O(n + m)
     */
    public List<Integer>[] buildGraph(int n, int[][] edges) {
        @SuppressWarnings("unchecked")
        List<Integer>[] graph = new ArrayList[n];

        // Initialize each adjacency list.
        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();
        }

        // Add every directed edge u -> v.
        for (int[] edge : edges) {
            graph[edge[0]].add(edge[1]);
        }

        return graph;
    }

    /**
     * Computes shortest distances from the start node using BFS.
     *
     * Because every road has equal cost (1 edge), BFS gives shortest path lengths
     * in terms of number of roads traveled.
     *
     * dist[i] will be:
     * - 0 for the start city s
     * - shortest number of edges from s to i if reachable
     * - -1 if i is not reachable from s
     *
     * @param n the number of nodes
     * @param graph the adjacency list of the directed graph
     * @param s the start node
     * @return distance array
     * Time complexity: O(n + m)
     * Space complexity: O(n)
     */
    public int[] bfsDistances(int n, List<Integer>[] graph, int s) {
        int[] dist = new int[n];
        Arrays.fill(dist, -1);

        Queue<Integer> queue = new ArrayDeque<>();
        queue.offer(s);
        dist[s] = 0;

        // Standard BFS loop.
        while (!queue.isEmpty()) {
            int u = queue.poll();

            // Explore all outgoing edges from u.
            for (int v : graph[u]) {
                // If v has not been visited yet, we discover it now.
                if (dist[v] == -1) {
                    dist[v] = dist[u] + 1;
                    queue.offer(v);
                }
            }
        }

        return dist;
    }

    /**
     * Finds all reachable nodes that belong to at least one directed cycle.
     *
     * We only care about nodes reachable from s, so we run Tarjan's SCC algorithm
     * only on the reachable subgraph.
     *
     * A node belongs to a directed cycle if:
     * - it is in an SCC of size >= 2, or
     * - it is in an SCC of size 1 and has a self-loop
     *
     * @param n the number of nodes
     * @param graph the adjacency list of the directed graph
     * @param reachable reachable[i] is true if node i is reachable from s
     * @return boolean array where result[i] is true if i is reachable and belongs to a directed cycle
     * Time complexity: O(n + m)
     * Space complexity: O(n)
     */
    public boolean[] findReachableCycleNodes(int n, List<Integer>[] graph, boolean[] reachable) {
        TarjanState state = new TarjanState(n, graph, reachable);

        // Run Tarjan DFS from every reachable node that has not yet been visited.
        for (int i = 0; i < n; i++) {
            if (reachable[i] && state.index[i] == -1) {
                tarjanDfs(i, state);
            }
        }

        return state.inCycle;
    }

    /**
     * Performs Tarjan's DFS from a given node.
     *
     * Tarjan's algorithm assigns:
     * - index[u]: discovery order of u
     * - low[u]: the smallest discovery index reachable from u using:
     *   - zero or more tree edges
     *   - and at most one back edge to a node currently on the stack
     *
     * When low[u] == index[u], u is the root of an SCC.
     * We then pop nodes from the stack until we pop u; those popped nodes form one SCC.
     *
     * After extracting the SCC:
     * - if SCC size >= 2, all its nodes are on a directed cycle
     * - if SCC size == 1, the single node is on a cycle only if it has a self-loop
     *
     * @param u the current node
     * @param state shared Tarjan state
     * @return nothing
     * Time complexity: O(size of explored subgraph overall across all calls)
     * Space complexity: O(n) overall including recursion stack and Tarjan structures
     */
    public void tarjanDfs(int u, TarjanState state) {
        // Assign discovery index and low-link value.
        state.index[u] = state.time;
        state.low[u] = state.time;
        state.time++;

        // Push u onto the Tarjan stack and mark it as currently on the stack.
        state.stack.push(u);
        state.onStack[u] = true;

        // Explore all outgoing edges u -> v.
        for (int v : state.graph[u]) {
            // Ignore nodes that are not reachable from s.
            // They are irrelevant to the answer and should not affect SCCs in the reachable subgraph.
            if (!state.reachable[v]) {
                continue;
            }

            if (state.index[v] == -1) {
                // v has not been visited yet, so recurse into it.
                tarjanDfs(v, state);

                // After returning, update low[u] using low[v].
                state.low[u] = Math.min(state.low[u], state.low[v]);
            } else if (state.onStack[v]) {
                // v is currently in the active DFS stack, so u can reach an ancestor/current SCC member.
                // This is the key Tarjan back-edge update.
                state.low[u] = Math.min(state.low[u], state.index[v]);
            }
        }

        // If u is the root of an SCC, extract that SCC.
        if (state.low[u] == state.index[u]) {
            List<Integer> component = new ArrayList<>();

            while (true) {
                int node = state.stack.pop();
                state.onStack[node] = false;
                component.add(node);

                if (node == u) {
                    break;
                }
            }

            // Determine whether this SCC represents a directed cycle.
            if (component.size() >= 2) {
                // Any SCC with 2 or more nodes always contains a directed cycle,
                // and every node in that SCC belongs to some directed cycle.
                for (int node : component) {
                    state.inCycle[node] = true;
                }
            } else {
                // Single-node SCC: it belongs to a cycle only if it has a self-loop.
                int node = component.get(0);
                for (int neighbor : state.graph[node]) {
                    if (neighbor == node) {
                        state.inCycle[node] = true;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Formats an integer array like [a, b] for easy printing.
     *
     * @param arr the array to format
     * @return string representation of the array
     * Time complexity: O(k), where k is the array length
     * Space complexity: O(1) excluding output string
     */
    public String formatArray(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> [1, 1]
     * Example 2 -> [-1, -1]
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(total graph size of the demo inputs)
     * Space complexity: O(total graph size of the demo inputs)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Graph:
        // 0 -> 1
        // 1 -> 2
        // 2 -> 3
        // 3 -> 1   forms cycle 1 -> 2 -> 3 -> 1
        // 2 -> 4
        // 4 -> 5
        //
        // Reachable cycle nodes from 0 are 1, 2, 3.
        // Distances are:
        // dist(1) = 1
        // dist(2) = 2
        // dist(3) = 3
        // So answer is [1, 1].
        int n1 = 7;
        int[][] edges1 = {
            {0, 1}, {1, 2}, {2, 3}, {3, 1}, {2, 4}, {4, 5}
        };
        int s1 = 0;
        int[] result1 = solution.closestCycleEntry(n1, edges1, s1);
        System.out.println(solution.formatArray(result1));

        // Example 2:
        // Graph:
        // 0 -> 1 -> 2 -> 4 -> 5
        // 3 -> 3   self-loop, but city 3 is not reachable from 0
        //
        // Therefore no reachable cycle exists from 0.
        // Answer is [-1, -1].
        int n2 = 6;
        int[][] edges2 = {
            {0, 1}, {1, 2}, {2, 4}, {4, 5}, {3, 3}
        };
        int s2 = 0;
        int[] result2 = solution.closestCycleEntry(n2, edges2, s2);
        System.out.println(solution.formatArray(result2));
    }

    /**
     * Helper class that stores all mutable state needed by Tarjan's SCC algorithm.
     *
     * Keeping this state in one object makes the recursive DFS method cleaner and easier to understand.
     */
    static class TarjanState {
        List<Integer>[] graph;
        boolean[] reachable;

        int[] index;
        int[] low;
        boolean[] onStack;
        boolean[] inCycle;

        int time;
        Deque<Integer> stack;

        /**
         * Creates and initializes Tarjan state.
         *
         * @param n number of nodes
         * @param graph adjacency list
         * @param reachable reachable marker array
         */
        TarjanState(int n, List<Integer>[] graph, boolean[] reachable) {
            this.graph = graph;
            this.reachable = reachable;
            this.index = new int[n];
            this.low = new int[n];
            this.onStack = new boolean[n];
            this.inCycle = new boolean[n];
            this.time = 0;
            this.stack = new ArrayDeque<>();

            Arrays.fill(this.index, -1);
        }
    }
}