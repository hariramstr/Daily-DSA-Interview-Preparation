```java
/*
 * Title: Shortest Path Through Mandatory Checkpoints
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Problem Description:
 * You are given a weighted undirected graph with `n` nodes (labeled `0` to `n-1`) and a list of
 * edges where `edges[i] = [u, v, w]` represents an edge between node `u` and node `v` with weight `w`.
 * You are also given a source node `src`, a destination node `dst`, and a list of `k` mandatory
 * checkpoint nodes `checkpoints`.
 *
 * Your task is to find the shortest path from `src` to `dst` that passes through **all** of the
 * mandatory checkpoints (in any order). If no such path exists, return `-1`.
 *
 * Note: You may visit nodes multiple times, and the path does not need to be simple.
 *
 * Constraints:
 * - 2 <= n <= 100
 * - 1 <= edges.length <= 500
 * - 0 <= u, v < n, u != v
 * - 1 <= w <= 1000
 * - 0 <= k <= 10
 * - 0 <= checkpoints[i] < n
 * - src != dst
 * - All checkpoint nodes are distinct and differ from `src` and `dst`
 */

import java.util.*;

/**
 * Solution class for finding the shortest path through mandatory checkpoints.
 *
 * <p>Approach Overview:
 * We use a combination of:
 * 1. Dijkstra's algorithm to precompute shortest distances between all "key nodes"
 *    (src, dst, and all checkpoints).
 * 2. Bitmask DP (Dynamic Programming) to find the optimal order to visit all checkpoints.
 *
 * <p>Why Bitmask DP?
 * Since k <= 10, there are at most 2^10 = 1024 subsets of checkpoints. We can use a bitmask
 * to represent which checkpoints have been visited. This is similar to the Traveling Salesman
 * Problem (TSP) approach.
 *
 * <p>Key Insight:
 * - Let "key nodes" = {src} ∪ {all checkpoints} ∪ {dst}
 * - Precompute shortest distances between every pair of key nodes using Dijkstra.
 * - Then use DP where dp[mask][i] = minimum cost to reach checkpoint i, having visited
 *   exactly the checkpoints indicated by `mask`, starting from `src`.
 * - Finally, find the minimum over all dp[(1<<k)-1][i] + dist[checkpoint_i][dst].
 */
public class Solution {

    /**
     * Runs Dijkstra's algorithm from a given source node on the graph.
     *
     * @param source    The starting node for Dijkstra's algorithm.
     * @param n         Total number of nodes in the graph.
     * @param adjList   Adjacency list representation: adjList[u] = list of [v, weight].
     * @return          An array dist[] where dist[v] = shortest distance from source to v.
     *                  If v is unreachable, dist[v] = Integer.MAX_VALUE.
     *
     * Time Complexity:  O((n + E) log n) where E is the number of edges.
     * Space Complexity: O(n + E) for the priority queue and distance array.
     */
    private int[] dijkstra(int source, int n, List<int[]>[] adjList) {
        // dist[v] holds the shortest known distance from `source` to node v
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;

        // Priority queue: stores [distance, node], ordered by distance (min-heap)
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        pq.offer(new int[]{0, source}); // Start with the source node at distance 0

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int currDist = curr[0];
            int currNode = curr[1];

            // If we already found a shorter path to currNode, skip this entry
            if (currDist > dist[currNode]) {
                continue;
            }

            // Explore all neighbors of currNode
            for (int[] neighbor : adjList[currNode]) {
                int nextNode = neighbor[0];
                int edgeWeight = neighbor[1];

                // Relaxation step: check if going through currNode gives a shorter path
                if (dist[currNode] != Integer.MAX_VALUE &&
                        dist[currNode] + edgeWeight < dist[nextNode]) {
                    dist[nextNode] = dist[currNode] + edgeWeight;
                    pq.offer(new int[]{dist[nextNode], nextNode});
                }
            }
        }

        return dist;
    }

    /**
     * Finds the shortest path from src to dst that passes through all mandatory checkpoints
     * in any order.
     *
     * <p>Algorithm Steps:
     * 1. Build the adjacency list from the edges.
     * 2. Run Dijkstra from src, dst, and each checkpoint to get pairwise shortest distances
     *    among all "key nodes".
     * 3. Use bitmask DP to find the minimum cost path visiting all checkpoints.
     * 4. Return the minimum total cost, or -1 if no valid path exists.
     *
     * @param n            Number of nodes in the graph (labeled 0 to n-1).
     * @param edges        List of edges, each as [u, v, w] (undirected, weight w).
     * @param src          Source node.
     * @param dst          Destination node.
     * @param checkpoints  Array of mandatory checkpoint nodes to visit.
     * @return             The minimum total weight of a path from src to dst through all
     *                     checkpoints, or -1 if no such path exists.
     *
     * Time Complexity:  O((k+2) * (n+E) log n + k^2 * 2^k)
     *                   - (k+2) Dijkstra runs: O((k+2)(n+E) log n)
     *                   - Bitmask DP: O(k^2 * 2^k)
     * Space Complexity: O(n + E + k * 2^k) for adjacency list, distances, and DP table.
     */
    public int shortestPathWithCheckpoints(int n, int[][] edges, int src, int dst,
                                           int[] checkpoints) {
        // -----------------------------------------------------------------------
        // STEP 1: Build the adjacency list for the undirected graph
        // -----------------------------------------------------------------------
        @SuppressWarnings("unchecked")
        List<int[]>[] adjList = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            adjList[i] = new ArrayList<>();
        }

        for (int[] edge : edges) {
            int u = edge[0], v = edge[1], w = edge[2];
            // Undirected graph: add edges in both directions
            adjList[u].add(new int[]{v, w});
            adjList[v].add(new int[]{u, w});
        }

        // -----------------------------------------------------------------------
        // STEP 2: Identify all "key nodes" and run Dijkstra from each
        // -----------------------------------------------------------------------
        // Key nodes: src, dst, and all checkpoints
        // We'll index them as:
        //   keyNodes[0]       = src
        //   keyNodes[1..k]    = checkpoints[0..k-1]
        //   keyNodes[k+1]     = dst
        int k = checkpoints.length;

        // Handle the edge case where there are no checkpoints
        if (k == 0) {
            // Just run Dijkstra from src and return dist[dst]
            int[] distFromSrc = dijkstra(src, n, adjList);
            int result = distFromSrc[dst];
            return result == Integer.MAX_VALUE ? -1 : result;
        }

        // Build the key nodes array: [src, checkpoint_0, checkpoint_1, ..., checkpoint_{k-1}, dst]
        // Total size = k + 2
        int[] keyNodes = new int[k + 2];
        keyNodes[0] = src;
        for (int i = 0; i < k; i++) {
            keyNodes[i + 1] = checkpoints[i];
        }
        keyNodes[k + 1] = dst;

        // dist[i][v] = shortest distance from keyNodes[i] to node v (in the full graph)
        int[][] distFromKey = new int[k + 2][n];
        for (int i = 0; i < k + 2; i++) {
            distFromKey[i] = dijkstra(keyNodes[i], n, adjList);
        }

        // For convenience, define a helper to get shortest distance between two key nodes:
        // shortDist[i][j] = shortest distance from keyNodes[i] to keyNodes[j]
        // We'll compute this inline using distFromKey[i][keyNodes[j]]

        // -----------------------------------------------------------------------
        // STEP 3: Bitmask DP
        // -----------------------------------------------------------------------
        // dp[mask][i] = minimum cost to reach checkpoint i (0-indexed among checkpoints),
        //               having visited exactly the checkpoints indicated by `mask`,
        //               starting from `src`.
        //
        // mask is a bitmask of size k:
        //   bit j = 1 means checkpoint j has been visited.
        //
        // Transitions:
        //   - Base case: dp[1 << i][i] = dist(src, checkpoint_i)
        //                (We go directly from src to checkpoint i)
        //   - Transition: dp[mask | (1 << j)][j] = min(dp[mask][i] + dist(checkpoint_i, checkpoint_j))
        //                 for all i in mask, j not in mask
        //
        // Final answer: min over all i of (dp[(1<<k)-1][i] + dist(checkpoint_i, dst))

        int totalMasks = 1 << k; // 2^k possible subsets
        int[][] dp = new int[totalMasks][k];

        // Initialize all DP values to infinity
        for (int[] row : dp) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }

        // Base case: Start from src and go to each checkpoint directly
        for (int i = 0; i < k; i++) {
            // keyNodes[0] = src, keyNodes[i+1] = checkpoints[i]
            int distSrcToCheckpointI = distFromKey[0][keyNodes[i + 1]];
            if (distSrcToCheckpointI != Integer.MAX_VALUE) {
                // Mask with only bit i set = (1 << i)
                dp[1 << i][i] = distSrcToCheckpointI;
            }
        }

        // Fill the DP table: iterate over all masks in increasing order
        for (int mask = 1; mask < totalMasks; mask++) {
            for (int i = 0; i < k; i++) {
                // Check if checkpoint i is in the current mask and dp[mask][i] is reachable
                if ((mask & (1 << i)) == 0) continue; // checkpoint i not in mask, skip
                if (dp[mask][i] == Integer.MAX_VALUE) continue; // unreachable, skip

                // Try extending the path to each checkpoint j not yet in mask
                for (int j = 0; j < k; j++) {
                    if ((mask & (1 << j)) != 0) continue; // checkpoint j already visited, skip

                    // Distance from checkpoint i to checkpoint j
                    // keyNodes[i+1] = checkpoints[i], keyNodes[j+1] = checkpoints[j]
                    int distItoJ = distFromKey[i + 1][keyNodes[j + 1]];

                    if (distItoJ != Integer.MAX_VALUE) {
                        int newMask = mask | (1 << j);
                        int newCost = dp[mask][i] + distItoJ;
                        // Update if we found a shorter path
                        if (newCost < dp[newMask][j]) {
                            dp[newMask][j] = newCost;
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------------
        // STEP 4: Compute the final answer
        // -----------------------------------------------------------------------
        // All checkpoints must be visited: mask = (1 << k) - 1 (all bits set)
        int fullMask = totalMasks - 1;
        int answer = Integer.MAX_VALUE;

        for (int i = 0; i < k; i++) {
            // If we've visited all checkpoints and the last one is checkpoint i
            if (dp[fullMask][i] == Integer.MAX_VALUE) continue;

            // Distance from checkpoint i to dst
            // keyNodes[i+1] = checkpoints[i], keyNodes[k+1] = dst
            int distCheckpointIToDst = distFromKey[i + 1][keyNodes[k + 1]];

            if (distCheckpointIToDst != Integer.MAX_VALUE) {
                int totalCost = dp[fullMask][i] + distCheckpointIToDst;
                answer = Math.min(answer, totalCost);
            }
        }

        // Return -1 if no valid path was found
        return answer == Integer.MAX_VALUE ? -1 : answer;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     *
     * <p>Traces through both examples from the problem description to verify correctness.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // n = 5
        // edges = [[0,1,2],[1,2,3],[2,3,1],[3,4,4],[0,3,10]]
        // src = 0, dst = 4
        // checkpoints = [2]
        // Expected Output: 10
        // Explanation: 0→1→2→3→4 with total weight 2+3+1+4 = 10
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        int n1 = 5;
        int[][] edges1 = {{0, 1, 2}, {1, 2, 3}, {2, 3, 1}, {3, 4, 4}, {0, 3, 10}};
        int src1 = 0, dst1 = 4;
        int[] checkpoints1 = {2};

        int result1 = solution.shortestPathWithCheckpoints(n1, edges1, src1, dst1, checkpoints1);
        System.out.println("Input: n=" + n1 + ", src=" + src1 + ", dst=" + dst1 +
                ", checkpoints=[2]");
        System.out.println("Output: " + result1);
        System.out.println("Expected: 10");
        System.out.println("Correct: " + (result1 == 10));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // n = 4
        // edges = [[0,1,1],[1,2,1],[2,3,1],[0,3,10]]
        // src = 0, dst = 3
        // checkpoints = [1, 2]
        // Expected Output: 3
        // Explanation: 0→1→2→3 with total weight 1+1+1 = 3
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        int n2 = 4;
        int[][] edges2 = {{0, 1, 1}, {1, 2, 1}, {2, 3, 1}, {0, 3, 