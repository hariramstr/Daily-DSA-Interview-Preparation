```java
/*
 * Title: Minimum Cost to Connect K Closest Server Clusters
 * Difficulty: Hard
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * You are given n servers on a 2D grid, where servers[i] = [x, y] represents
 * the coordinates of the i-th server. You also have a list of queries, where
 * each query queries[j] = [index, k] asks: what is the minimum total connection
 * cost to connect the k closest servers to server index (including itself) into
 * a single network?
 *
 * The cost to connect two servers is the Euclidean distance squared between them
 * (to avoid floating point). The minimum cost to connect a set of servers into a
 * network is the cost of their Minimum Spanning Tree (MST).
 *
 * For each query, return the minimum total connection cost (MST cost) of the k
 * closest servers to the given server.
 *
 * Constraints:
 * - 2 <= n <= 1000
 * - 1 <= queries.length <= 500
 * - servers[i].length == 2
 * - 0 <= x, y <= 10^4
 * - 1 <= k <= n
 * - 0 <= index < n
 * - All server coordinates are distinct.
 *
 * Example 1:
 * Input: servers = [[0,0],[1,1],[3,3],[6,6]], queries = [[0,3],[1,2]]
 * Output: [8, 2]
 * Explanation: For query [0,3]: The 3 closest servers to server 0 are servers 0, 1, 2
 * with distances squared 0, 2, 18. Their MST costs 2 + 8 = 8.
 * For query [1,2]: The 2 closest servers to server 1 are servers 0 and 1 (distance squared 2).
 * MST cost = 2.
 *
 * Example 2:
 * Input: servers = [[0,0],[2,0],[0,2],[2,2]], queries = [[0,4]]
 * Output: [12]
 * Explanation: All 4 servers form the MST with cost 4+4+4 = 12.
 */

import java.util.*;

/**
 * Solution class for the Minimum Cost to Connect K Closest Server Clusters problem.
 * 
 * <p>Approach:
 * For each query [index, k]:
 * 1. Find the k closest servers to server[index] using squared Euclidean distance.
 * 2. Build a complete graph among those k servers.
 * 3. Compute the MST of that subgraph using Prim's algorithm (efficient with a priority queue).
 * 4. Return the total MST cost.
 */
public class Solution {

    /**
     * Computes the squared Euclidean distance between two servers.
     *
     * @param servers the array of server coordinates
     * @param i       index of the first server
     * @param j       index of the second server
     * @return squared Euclidean distance between server i and server j
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    private long squaredDist(int[][] servers, int i, int j) {
        long dx = servers[i][0] - servers[j][0];
        long dy = servers[i][1] - servers[j][1];
        return dx * dx + dy * dy;
    }

    /**
     * Finds the k closest servers to the server at the given index (including itself),
     * then computes the MST cost of connecting those k servers.
     *
     * <p>Algorithm overview:
     * - For each query, sort all servers by their squared distance to the query server.
     * - Take the k closest (including the query server itself, which has distance 0).
     * - Run Prim's MST algorithm on the complete subgraph of those k servers.
     *
     * @param servers array of server coordinates, servers[i] = [x, y]
     * @param queries array of queries, queries[j] = [index, k]
     * @return array of MST costs for each query
     * Time complexity: O(Q * (N log N + K^2 log K)) where Q = queries.length, N = servers.length, K = k
     * Space complexity: O(N + K^2) for sorting and MST structures
     */
    public long[] minCostToConnectClusters(int[][] servers, int[][] queries) {
        int n = servers.length;
        int q = queries.length;
        long[] results = new long[q];

        // Process each query independently
        for (int qi = 0; qi < q; qi++) {
            int centerIndex = queries[qi][0]; // The reference server index
            int k = queries[qi][1];           // Number of closest servers to include

            // ---------------------------------------------------------------
            // Step 1: Find the k closest servers to server[centerIndex]
            // We create an array of all server indices and sort them by their
            // squared distance to the center server.
            // ---------------------------------------------------------------
            Integer[] indices = new Integer[n];
            for (int i = 0; i < n; i++) {
                indices[i] = i;
            }

            // Sort server indices by squared distance to the center server
            // The center server itself will have distance 0 and sort to the front
            Arrays.sort(indices, (a, b) -> {
                long distA = squaredDist(servers, centerIndex, a);
                long distB = squaredDist(servers, centerIndex, b);
                return Long.compare(distA, distB);
            });

            // Take the first k indices — these are the k closest servers
            int[] cluster = new int[k];
            for (int i = 0; i < k; i++) {
                cluster[i] = indices[i];
            }

            // ---------------------------------------------------------------
            // Step 2: Compute MST of the k-server subgraph using Prim's algorithm
            //
            // Prim's algorithm:
            // - Start from any node (we'll use cluster[0]).
            // - Maintain a "minCost" array: minCost[i] = minimum edge cost to
            //   connect cluster node i to the current MST.
            // - Use a min-heap (priority queue) to always pick the cheapest edge.
            // - Repeat until all k nodes are in the MST.
            // ---------------------------------------------------------------

            // minCost[i] stores the minimum cost to add cluster[i] to the MST
            long[] minCost = new long[k];
            Arrays.fill(minCost, Long.MAX_VALUE);

            // inMST[i] = true if cluster[i] has been added to the MST
            boolean[] inMST = new boolean[k];

            // Start from the first node in the cluster (arbitrary choice)
            minCost[0] = 0;

            // Priority queue: [cost, clusterNodeIndex]
            // Min-heap ordered by cost
            PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(e -> e[0]));
            pq.offer(new long[]{0L, 0L}); // Start with cluster node 0, cost 0

            long totalMSTCost = 0;
            int nodesAdded = 0;

            while (!pq.isEmpty() && nodesAdded < k) {
                // Pick the cluster node with the minimum connection cost
                long[] top = pq.poll();
                long cost = top[0];
                int ci = (int) top[1]; // Index within the cluster array

                // Skip if this node is already in the MST
                // (we may have stale entries in the priority queue)
                if (inMST[ci]) {
                    continue;
                }

                // Add this node to the MST
                inMST[ci] = true;
                totalMSTCost += cost;
                nodesAdded++;

                // Update minCost for all neighbors not yet in the MST
                for (int cj = 0; cj < k; cj++) {
                    if (!inMST[cj]) {
                        // Compute the edge weight between cluster[ci] and cluster[cj]
                        long edgeCost = squaredDist(servers, cluster[ci], cluster[cj]);

                        // If this edge is cheaper than the current best for cj, update
                        if (edgeCost < minCost[cj]) {
                            minCost[cj] = edgeCost;
                            pq.offer(new long[]{edgeCost, (long) cj});
                        }
                    }
                }
            }

            // Store the MST cost for this query
            results[qi] = totalMSTCost;
        }

        return results;
    }

    /**
     * Main method to demonstrate the solution with sample inputs from the problem.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // ---------------------------------------------------------------
        // Example 1:
        // servers = [[0,0],[1,1],[3,3],[6,6]], queries = [[0,3],[1,2]]
        // Expected Output: [8, 2]
        //
        // Trace for query [0, 3]:
        //   Center = server 0 at (0,0)
        //   Distances squared:
        //     server 0: 0
        //     server 1: (1-0)^2 + (1-0)^2 = 2
        //     server 2: (3-0)^2 + (3-0)^2 = 18
        //     server 3: (6-0)^2 + (6-0)^2 = 72
        //   3 closest: servers 0, 1, 2
        //   MST of {(0,0),(1,1),(3,3)}:
        //     Edge 0-1: dist^2 = 2
        //     Edge 1-2: dist^2 = 8
        //     Edge 0-2: dist^2 = 18
        //     MST = edges {0-1: 2, 1-2: 8} = 10? Wait let me recheck.
        //     Actually: (3-1)^2 + (3-1)^2 = 4+4 = 8. Yes.
        //     MST cost = 2 + 8 = 10? But expected is 8.
        //
        //   Wait, let me re-read: "connect 0-1 cost 2, connect 1-2 cost 8" = 10?
        //   But expected output says 8. Let me re-read the problem...
        //   "MST costs 2 + 8 = 8" — that seems like a typo in the problem (2+8=10, not 8).
        //   But the expected output is [8, 2].
        //
        //   Hmm, let me reconsider. Maybe the problem means something different.
        //   Let me check: servers 0,1,2 are (0,0),(1,1),(3,3).
        //   Edge 0-1: 1+1=2, Edge 1-2: 4+4=8, Edge 0-2: 9+9=18.
        //   MST = 2+8 = 10. But expected is 8.
        //
        //   Wait — maybe the problem description has an error in the explanation
        //   but the expected output [8,2] is correct. Let me think differently:
        //   Maybe the 3 closest to server 0 are servers 0,1,2 but the MST is
        //   actually computed differently, or maybe I'm misidentifying the k closest.
        //
        //   Actually wait: re-reading "The 3 closest servers to server 0 are servers 0, 1, 2
        //   with distances squared 0, 2, 18. Their MST costs 2 + 8 = 8"
        //   2+8=10, not 8. This is a typo in the problem. The actual answer should be 10.
        //   But the stated output is [8, 2].
        //
        //   Hmm, let me reconsider. Maybe the problem means k closest EXCLUDING itself?
        //   If k=3 means 3 closest excluding server 0: servers 1,2,3.
        //   Distances from server 0: server1=2, server2=18, server3=72.
        //   3 closest excluding self: servers 1,2,3.
        //   MST of {(1,1),(3,3),(6,6)}:
        //     Edge 1-2: (3-1)^2+(3-1)^2=8
        //     Edge 2-3: (6-3)^2+(6-3)^2=18
        //     Edge 1-3: (6-1)^2+(6-1)^2=50
        //   MST = 8+18 = 26. Not 8 either.
        //
        //   Let me try: maybe k=3 means the server itself + 2 others.
        //   So cluster = {server0, server1, server2} — same as before, MST=10.
        //
        //   OR maybe the output [8,2] is wrong in the problem statement and should be [10,2].
        //   Let me just implement the correct algorithm and output what's mathematically correct.
        //   The explanation says "2+8=8" which is clearly a typo for "2+8=10".
        //   I'll implement the correct algorithm.
        // ---------------------------------------------------------------

        int[][] servers1 = {{0, 0}, {1, 1}, {3, 3}, {6, 6}};
        int[][] queries1 = {{0, 3}, {1, 2}};
        long[] result1 = solution.minCostToConnectClusters(servers1, queries1);
        System.out.println("Example 1:");
        System.out.println("Input: servers = [[0,0],[1,1],[3,3],[6,6]], queries = [[0,3],[1,2]]");
        System.out.print("Output: [");
        for (int i = 0; i < result1.length; i++) {
            System.out.print(result1[i]);
            if (i < result1.length - 1) System.out.print(", ");
        }
        System.out.println("]");
        // For query [0,3]: cluster = {(0,0),(1,1),(3,3)}, MST = 2+8 = 10
        // For query [1,2]: cluster = {(0,0),(1,1)}, MST = 2
        System.out.println("Note: Problem statement has a typo (2+8=8 should be 2+8=10).");
        System.out.println("Mathematically correct answer for query [0,3] is 10, not 8.");
        System.out.println();

        // ---------------------------------------------------------------
        // Example 2:
        // servers = [[0,0],[2,0],[0,2],[2,2]], queries = [[0,4]]
        // Expected Output: [12]
        //
        // Trace:
        //   Center = server 0 at (0,0)
        //   Distances squared from server 0:
        //     server 0: 0
        //     server 1: 4+0=4
        //     server 2: 0+4=4
        //     server 3: 4+4=8
        //   k=4, so all 4 servers are in the cluster.
        //   MST of {(0,0),(2,0),(0,2),(2,2)}:
        //     All edges of length 4 (horizontal/vertical neighbors): 0-1, 0-2, 1-3, 2-3
        //     Diagonal edges of length 8: 0-3, 1-2
        //     MST picks 3 edges of cost 4 each = 12. ✓
        // ---------------------------------------------------------------

        int[][] servers2 =