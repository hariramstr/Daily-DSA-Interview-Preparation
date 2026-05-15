```java
/*
 * Title: Minimum Fuel to Visit All Checkpoints
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Problem Description:
 * You are given a map of n cities (labeled 0 to n-1) connected by bidirectional roads.
 * Each road has a fuel cost to traverse. You are also given a list of checkpoints —
 * a subset of cities you must visit at least once. You start at city 0 and must visit
 * every checkpoint city, but you do not need to end at any specific city.
 *
 * Return the minimum total fuel cost to visit all checkpoint cities starting from city 0.
 * You may visit non-checkpoint cities as intermediate stops, and you may revisit cities.
 *
 * If it is impossible to reach all checkpoints from city 0, return -1.
 *
 * Constraints:
 * - 2 <= n <= 1000
 * - 0 <= roads.length <= 5000
 * - roads[i] = [u, v, w] where 0 <= u, v < n and 1 <= w <= 10^4
 * - 1 <= checkpoints.length <= 12
 * - checkpoints[i] is a valid city index and checkpoints[0] is never 0
 * - All checkpoint cities are distinct
 */

import java.util.*;

/**
 * Solution class for the "Minimum Fuel to Visit All Checkpoints" problem.
 *
 * <p>Approach:
 * This problem is a variant of the Traveling Salesman Problem (TSP) on a graph.
 * Since checkpoints.length <= 12, we can use bitmask DP (dynamic programming with
 * bitmasks to represent subsets of visited checkpoints).
 *
 * <p>Key Steps:
 * 1. Run Dijkstra's algorithm from city 0 and from each checkpoint city to compute
 *    shortest paths between all relevant nodes (city 0 + all checkpoints).
 * 2. Use bitmask DP where dp[mask][i] = minimum fuel to have visited exactly the
 *    checkpoints indicated by 'mask', currently at checkpoint index i.
 * 3. The answer is the minimum over all dp[fullMask][i] for all checkpoint indices i.
 */
public class Solution {

    /**
     * Computes the shortest distances from a source node to all other nodes
     * using Dijkstra's algorithm with a priority queue.
     *
     * @param src   the source node index
     * @param n     total number of cities
     * @param graph adjacency list: graph.get(u) contains int[]{v, w} pairs
     * @return an array dist[] where dist[v] = shortest distance from src to v
     *         (Integer.MAX_VALUE if unreachable)
     *
     * Time complexity: O((V + E) log V) where V = n, E = number of roads
     * Space complexity: O(V + E) for the distance array and priority queue
     */
    private int[] dijkstra(int src, int n, List<List<int[]>> graph) {
        // dist[i] = minimum fuel cost from src to city i
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;

        // Priority queue: [cost, node] — always processes the cheapest node first
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        pq.offer(new int[]{0, src});

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int cost = curr[0];
            int node = curr[1];

            // If we've already found a better path to this node, skip
            if (cost > dist[node]) continue;

            // Explore all neighbors of the current node
            for (int[] edge : graph.get(node)) {
                int neighbor = edge[0];
                int weight = edge[1];

                // Relaxation step: can we reach neighbor cheaper via current node?
                if (dist[node] != Integer.MAX_VALUE && dist[node] + weight < dist[neighbor]) {
                    dist[neighbor] = dist[node] + weight;
                    pq.offer(new int[]{dist[neighbor], neighbor});
                }
            }
        }

        return dist;
    }

    /**
     * Finds the minimum total fuel cost to visit all checkpoint cities starting from city 0.
     *
     * <p>Algorithm Overview:
     * 1. Build an adjacency list from the roads array.
     * 2. Define "key nodes" = [city 0] + all checkpoints. There are at most 13 key nodes.
     * 3. Run Dijkstra from each key node to get shortest distances to all other key nodes.
     * 4. Use bitmask DP:
     *    - State: dp[mask][i] = min fuel to have visited checkpoints in 'mask', currently at checkpoint i
     *    - Transition: from checkpoint i with visited set 'mask', move to checkpoint j (not yet visited)
     *    - Answer: min(dp[fullMask][i]) for all i
     *
     * @param n           total number of cities (labeled 0 to n-1)
     * @param roads       array of [u, v, w] representing bidirectional roads with fuel cost w
     * @param checkpoints array of checkpoint city indices that must all be visited
     * @return minimum total fuel cost to visit all checkpoints from city 0, or -1 if impossible
     *
     * Time complexity: O(K^2 * 2^K + (K+1) * (V+E) log V) where K = checkpoints.length
     * Space complexity: O(K * 2^K) for the DP table, plus O(V+E) for the graph
     */
    public int minFuelToVisitCheckpoints(int n, int[][] roads, int[] checkpoints) {
        // -----------------------------------------------------------------------
        // Step 1: Build the adjacency list for the graph
        // -----------------------------------------------------------------------
        List<List<int[]>> graph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }
        for (int[] road : roads) {
            int u = road[0], v = road[1], w = road[2];
            // Bidirectional road: add both directions
            graph.get(u).add(new int[]{v, w});
            graph.get(v).add(new int[]{u, w});
        }

        // -----------------------------------------------------------------------
        // Step 2: Define key nodes = [city 0, checkpoint[0], checkpoint[1], ...]
        // keyNodes[0] = city 0 (starting point)
        // keyNodes[1..K] = checkpoints[0..K-1]
        // -----------------------------------------------------------------------
        int K = checkpoints.length;
        int[] keyNodes = new int[K + 1];
        keyNodes[0] = 0; // starting city
        for (int i = 0; i < K; i++) {
            keyNodes[i + 1] = checkpoints[i];
        }

        // -----------------------------------------------------------------------
        // Step 3: Run Dijkstra from each key node
        // shortDist[i][j] = shortest distance from keyNodes[i] to keyNodes[j]
        // -----------------------------------------------------------------------
        int[][] shortDist = new int[K + 1][K + 1];
        for (int i = 0; i <= K; i++) {
            // Run Dijkstra from keyNodes[i]
            int[] dist = dijkstra(keyNodes[i], n, graph);
            for (int j = 0; j <= K; j++) {
                shortDist[i][j] = dist[keyNodes[j]];
            }
        }

        // -----------------------------------------------------------------------
        // Step 4: Bitmask DP
        //
        // We have K checkpoints indexed 0..K-1 (corresponding to keyNodes[1..K]).
        // dp[mask][i] = minimum fuel to have visited exactly the checkpoints
        //               indicated by 'mask', and currently being at checkpoint i.
        //
        // 'mask' is a bitmask of K bits. Bit j is set if checkpoint j has been visited.
        // 'i' ranges from 0 to K-1 (index into the checkpoints array).
        //
        // In shortDist, checkpoint i corresponds to keyNodes[i+1].
        // City 0 corresponds to keyNodes[0], which is shortDist index 0.
        // -----------------------------------------------------------------------
        int fullMask = (1 << K) - 1; // all K checkpoints visited
        // Initialize DP table with "infinity"
        int INF = Integer.MAX_VALUE / 2;
        int[][] dp = new int[1 << K][K];
        for (int[] row : dp) Arrays.fill(row, INF);

        // Base case: start at city 0, move to each checkpoint i as the first visit
        // shortDist[0][i+1] = distance from city 0 to checkpoint i (keyNodes[i+1])
        for (int i = 0; i < K; i++) {
            int distFromStart = shortDist[0][i + 1]; // distance from city 0 to checkpoint i
            if (distFromStart < INF) {
                // We've visited only checkpoint i (bit i is set)
                dp[1 << i][i] = distFromStart;
            }
        }

        // Fill the DP table
        // Iterate over all possible masks (subsets of visited checkpoints)
        for (int mask = 1; mask <= fullMask; mask++) {
            // For each checkpoint i that is currently "last visited" (bit i is set in mask)
            for (int i = 0; i < K; i++) {
                // Skip if checkpoint i is not in the current mask
                if ((mask & (1 << i)) == 0) continue;
                // Skip if this state is unreachable
                if (dp[mask][i] == INF) continue;

                // Try moving from checkpoint i to checkpoint j (not yet visited)
                for (int j = 0; j < K; j++) {
                    // Skip if checkpoint j is already visited
                    if ((mask & (1 << j)) != 0) continue;

                    // Distance from checkpoint i to checkpoint j
                    // In shortDist, checkpoint i is at index i+1, checkpoint j at j+1
                    int travelCost = shortDist[i + 1][j + 1];
                    if (travelCost == Integer.MAX_VALUE) continue; // unreachable

                    int newMask = mask | (1 << j);
                    int newCost = dp[mask][i] + travelCost;

                    // Update if we found a cheaper way to reach state (newMask, j)
                    if (newCost < dp[newMask][j]) {
                        dp[newMask][j] = newCost;
                    }
                }
            }
        }

        // -----------------------------------------------------------------------
        // Step 5: Find the answer
        // The answer is the minimum dp[fullMask][i] over all last-visited checkpoints i
        // -----------------------------------------------------------------------
        int answer = INF;
        for (int i = 0; i < K; i++) {
            answer = Math.min(answer, dp[fullMask][i]);
        }

        // If answer is still INF, it's impossible to visit all checkpoints
        return answer >= INF ? -1 : answer;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // n = 5, roads = [[0,1,2],[1,2,3],[2,3,1],[3,4,4],[1,4,10]], checkpoints = [2, 4]
        //
        // Let's trace through:
        // Key nodes: [0, 2, 4] (city 0 + checkpoints)
        //
        // Dijkstra from city 0:
        //   dist[0]=0, dist[1]=2, dist[2]=5, dist[3]=6, dist[4]=10
        //   shortDist[0][1] = dist[2] = 5 (city 0 to checkpoint 2)
        //   shortDist[0][2] = dist[4] = 10 (city 0 to checkpoint 4)
        //
        // Dijkstra from city 2 (checkpoint 0):
        //   dist[2]=0, dist[1]=3, dist[3]=1, dist[0]=5, dist[4]=5
        //   shortDist[1][2] = dist[4] = 5 (checkpoint 2 to checkpoint 4)
        //
        // Dijkstra from city 4 (checkpoint 1):
        //   dist[4]=0, dist[3]=4, dist[1]=10, dist[2]=5, dist[0]=12
        //   shortDist[2][1] = dist[2] = 5 (checkpoint 4 to checkpoint 2)
        //
        // DP:
        // Base: dp[01][0] = shortDist[0][1] = 5 (visited checkpoint 2, at checkpoint 2)
        //       dp[10][1] = shortDist[0][2] = 10 (visited checkpoint 4, at checkpoint 4)
        //
        // From dp[01][0]=5 (at checkpoint 2, visited={2}):
        //   Move to checkpoint 4: dp[11][1] = 5 + shortDist[1][2] = 5 + 5 = 10
        //
        // From dp[10][1]=10 (at checkpoint 4, visited={4}):
        //   Move to checkpoint 2: dp[11][0] = 10 + shortDist[2][1] = 10 + 5 = 15
        //
        // Answer = min(dp[11][0], dp[11][1]) = min(15, 10) = 10
        //
        // Wait, the expected output is 6. Let me re-read the problem...
        // The explanation says "6 via shortest paths summed from a Steiner-tree perspective"
        // but the actual paths described give cost 10. The example explanation seems
        // contradictory. Let me re-examine.
        //
        // Actually looking at the example more carefully:
        // "Best is visit 2 then backtrack optimally: 0→1(2)→2(3), then 2→3(1)→4(4) = total 10"
        // The output says 6 but the explanation gives 10. This seems like an error in the
        // problem statement. Let me verify with example 2 which is clear.
        //
        // For example 2: n=3, roads=[[0,1,1],[1,2,2]], checkpoints=[2]
        // Dijkstra from 0: dist[2] = 3. Answer = 3. Correct!
        //
        // For example 1, the correct answer based on the graph should be 10.
        // The "6" in the output seems to be an error in the problem statement.
        // Our algorithm correctly computes 10.
        // -----------------------------------------------------------------------

        System.out.println("=== Example 1 ===");
        int n1 = 5;
        int[][] roads1 = {{0, 1, 2}, {1, 2, 3}, {2, 3, 1}, {3, 4, 4}, {1, 4, 10}};
        int[] checkpoints1 = {2, 4};
        int result1 = sol.minFuelToVisitCheckpoints(n1, roads1, checkpoints1);
        System.out.println("Input: n=" + n1 + ", roads=[[0,1,2],[1,2,3],[2,3,1],[3,4,4],[1,4,10]], checkpoints=[2,4]");
        System.out.println("Output: " + result1);
        // Shortest path: 0->1->2 (cost 5) then 2->3->4 (cost 5)