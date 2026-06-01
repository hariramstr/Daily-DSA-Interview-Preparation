/*
 * Title: Minimum Cost to Connect K Closest Server Clusters
 * 
 * Problem Description:
 * You are given n servers on a 2D grid, where servers[i] = [x, y] represents the
 * coordinates of the i-th server. You also have a list of queries, where each query
 * queries[j] = [index, k] asks: what is the minimum total connection cost to connect
 * the k closest servers to server index (including itself) into a single network?
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
 *
 * Example 2:
 * Input: servers = [[0,0],[2,0],[0,2],[2,2]], queries = [[0,4]]
 * Output: [12]
 */

using System;
using System.Collections.Generic;
using System.Linq;

// ============================================================
// Solution Class
// ============================================================
public class Solution
{
    // -------------------------------------------------------
    // MinCostToConnectClusters
    //
    // Time Complexity:  O(Q * (N log N + K^2 log K))
    //   - Q = number of queries
    //   - N = number of servers (for sorting distances)
    //   - K = number of closest servers per query
    //   - For each query we sort N distances (N log N) and then
    //     run Prim's MST on K nodes with up to K^2 edges (K^2 log K)
    //
    // Space Complexity: O(N + K^2)
    //   - O(N) for the distance array
    //   - O(K^2) for the edge list used in MST
    // -------------------------------------------------------
    public long[] MinCostToConnectClusters(int[][] servers, int[][] queries)
    {
        // ---- Step 1: Prepare the result array ----
        // We need one answer per query, so allocate an array of the same length.
        long[] results = new long[queries.Length];

        // ---- Step 2: Process each query independently ----
        // Each query is self-contained: find k closest servers to a given index,
        // then compute the MST of those k servers.
        for (int q = 0; q < queries.Length; q++)
        {
            int centerIndex = queries[q][0]; // The reference server index
            int k           = queries[q][1]; // How many closest servers to include

            // ---- Step 3: Compute squared Euclidean distance from centerIndex to every server ----
            // We use squared distance to avoid floating-point arithmetic.
            // dist^2 = (x2-x1)^2 + (y2-y1)^2
            // We store (distanceSquared, serverIndex) pairs so we can sort and retrieve indices.
            var distPairs = new (long dist, int idx)[servers.Length];
            int cx = servers[centerIndex][0];
            int cy = servers[centerIndex][1];

            for (int i = 0; i < servers.Length; i++)
            {
                long dx = servers[i][0] - cx;
                long dy = servers[i][1] - cy;
                distPairs[i] = (dx * dx + dy * dy, i);
            }

            // ---- Step 4: Sort by distance to find the k closest servers ----
            // After sorting, the first k entries are the k nearest servers
            // (including the center server itself, which has distance 0).
            Array.Sort(distPairs, (a, b) => a.dist.CompareTo(b.dist));

            // ---- Step 5: Collect the indices of the k closest servers ----
            // These are the nodes that will form our MST.
            int[] chosen = new int[k];
            for (int i = 0; i < k; i++)
                chosen[i] = distPairs[i].idx;

            // ---- Step 6: If k == 1, no edges needed — MST cost is 0 ----
            if (k == 1)
            {
                results[q] = 0;
                continue;
            }

            // ---- Step 7: Build all edges among the k chosen servers ----
            // For MST we need to consider every pair of chosen servers.
            // There are C(k,2) = k*(k-1)/2 such pairs.
            // We store each edge as (cost, u, v) where cost = squared distance.
            var edges = new List<(long cost, int u, int v)>();

            for (int i = 0; i < k; i++)
            {
                for (int j = i + 1; j < k; j++)
                {
                    int si = chosen[i];
                    int sj = chosen[j];
                    long dx = servers[si][0] - servers[sj][0];
                    long dy = servers[si][1] - servers[sj][1];
                    long cost = dx * dx + dy * dy;
                    edges.Add((cost, i, j)); // use local indices 0..k-1 for Union-Find
                }
            }

            // ---- Step 8: Run Kruskal's MST algorithm ----
            // Kruskal's algorithm:
            //   1. Sort all edges by cost (ascending).
            //   2. Use a Union-Find (Disjoint Set Union) to greedily add the
            //      cheapest edge that connects two previously disconnected components.
            //   3. Stop when we have k-1 edges (the MST is complete).
            //
            // Why Kruskal's? It's straightforward to implement with a sorted edge list
            // and Union-Find, and works well for dense graphs of small k.

            // Sort edges by cost ascending
            edges.Sort((a, b) => a.cost.CompareTo(b.cost));

            // Initialize Union-Find for k nodes (local indices 0..k-1)
            int[] parent = new int[k];
            int[] rank   = new int[k];
            for (int i = 0; i < k; i++) parent[i] = i; // each node is its own root

            long totalCost = 0;
            int  edgesUsed = 0;

            foreach (var (cost, u, v) in edges)
            {
                // If we already have k-1 edges, the MST is complete
                if (edgesUsed == k - 1) break;

                // Find the root of u and v
                int rootU = Find(parent, u);
                int rootV = Find(parent, v);

                // If they are in different components, connect them
                if (rootU != rootV)
                {
                    Union(parent, rank, rootU, rootV);
                    totalCost += cost;
                    edgesUsed++;
                }
                // If same component, skip this edge (would create a cycle)
            }

            results[q] = totalCost;
        }

        return results;
    }

    // -------------------------------------------------------
    // Find — Union-Find helper with path compression
    //
    // Path compression flattens the tree so future Find calls
    // on the same node are O(1) amortized.
    // -------------------------------------------------------
    private int Find(int[] parent, int x)
    {
        // If x is not its own parent, recursively find the root
        // and compress the path by pointing x directly to the root.
        if (parent[x] != x)
            parent[x] = Find(parent, parent[x]);
        return parent[x];
    }

    // -------------------------------------------------------
    // Union — Union-Find helper with union by rank
    //
    // Union by rank keeps the tree shallow by always attaching
    // the smaller-rank tree under the larger-rank tree.
    // -------------------------------------------------------
    private void Union(int[] parent, int[] rank, int a, int b)
    {
        // Attach the tree with lower rank under the tree with higher rank.
        // If ranks are equal, arbitrarily choose one and increment its rank.
        if (rank[a] < rank[b])
            parent[a] = b;
        else if (rank[a] > rank[b])
            parent[b] = a;
        else
        {
            parent[b] = a;
            rank[a]++;
        }
    }
}

// ============================================================
// Demo / Test Code (top-level statements)
// ============================================================

var solution = new Solution();

// ---- Example 1 ----
// servers = [[0,0],[1,1],[3,3],[6,6]]
// queries = [[0,3],[1,2]]
// Expected output: [8, 2]
//
// Trace for query [0,3]:
//   Center = server 0 at (0,0)
//   Distances squared: server0=0, server1=2, server2=18, server3=72
//   3 closest: servers 0,1,2
//   Edges among {0,1,2}:
//     0-1: (1-0)^2+(1-0)^2 = 2
//     0-2: (3-0)^2+(3-0)^2 = 18
//     1-2: (3-1)^2+(3-1)^2 = 8
//   Kruskal: pick edge 0-1 (cost 2), then edge 1-2 (cost 8) => total = 10? 
//   Wait, let me re-check: 2+8=10 but expected is 8.
//   Hmm — re-reading the problem: "connect 0-1 cost 2, connect 1-2 cost 8" => 2+8=10?
//   But the expected output says 8. Let me re-read...
//   The problem says "MST costs 2 + 8 = 8" which seems like a typo in the problem (2+8=10, not 8).
//   Actually wait: (3-1)^2+(3-1)^2 = 4+4 = 8. And (1-0)^2+(1-0)^2 = 1+1 = 2. So 2+8=10.
//   But expected is [8,2]. Let me reconsider: maybe the problem description has a typo
//   and the expected answer is actually [10, 2].
//   Let me verify example 2: servers=[[0,0],[2,0],[0,2],[2,2]], query=[0,4]
//   All 4 servers. MST edges (squared distances):
//     0-1: 4, 0-2: 4, 0-3: 8, 1-2: 8, 1-3: 4, 2-3: 4
//   Kruskal: pick 0-1(4), 0-2(4), 1-3(4) => total=12. Correct!
//   So example 2 checks out. For example 1 the answer should be 10, not 8.
//   The problem statement likely has a typo. Our algorithm is correct.

int[][] servers1 = [[0,0],[1,1],[3,3],[6,6]];
int[][] queries1 = [[0,3],[1,2]];
long[] result1 = solution.MinCostToConnectClusters(servers1, queries1);
Console.WriteLine("Example 1:");
Console.WriteLine($"  Input servers: [[0,0],[1,1],[3,3],[6,6]]");
Console.WriteLine($"  Input queries: [[0,3],[1,2]]");
Console.Write("  Output: [");
Console.Write(string.Join(", ", result1));
Console.WriteLine("]");
// Query [0,3]: 3 closest to server0 are servers 0,1,2. MST = edge(0-1)=2 + edge(1-2)=8 = 10
// Query [1,2]: 2 closest to server1 are servers 0,1. MST = edge(0-1)=2
Console.WriteLine($"  Expected (corrected): [10, 2]  (problem statement has typo: 2+8=10, not 8)");
Console.WriteLine();

// ---- Example 2 ----
// servers = [[0,0],[2,0],[0,2],[2,2]]
// queries = [[0,4]]
// Expected output: [12]
int[][] servers2 = [[0,0],[2,0],[0,2],[2,2]];
int[][] queries2 = [[0,4]];
long[] result2 = solution.MinCostToConnectClusters(servers2, queries2);
Console.WriteLine("Example 2:");
Console.WriteLine($"  Input servers: [[0,0],[2,0],[0,2],[2,2]]");
Console.WriteLine($"  Input queries: [[0,4]]");
Console.Write("  Output: [");
Console.Write(string.Join(", ", result2));
Console.WriteLine("]");
Console.WriteLine($"  Expected: [12]");
Console.WriteLine();

// ---- Additional Example ----
// servers = [[0,0],[1,0],[0,1],[1,1],[5,5]]
// queries = [[0,4],[4,2]]
int[][] servers3 = [[0,0],[1,0],[0,1],[1,1],[5,5]];
int[][] queries3 = [[0,4],[4,2]];
long[] result3 = solution.MinCostToConnectClusters(servers3, queries3);
Console.WriteLine("Example 3 (additional):");
Console.WriteLine($"  Input servers: [[0,0],[1,0],[0,1],[1,1],[5,5]]");
Console.WriteLine($"  Input queries: [[0,4],[4,2]]");
Console.Write("  Output: [");
Console.Write(string.Join(", ", result3));
Console.WriteLine("]");
// Query [0,4]: 4 closest to server0 (0,0) are servers 0,1,2,3
//   Distances: 0=0, 1=1, 2=1, 3=2, 4=50
//   Edges: 0-1=1, 0-2=1, 0-3=2, 1-2=2, 1-3=1, 2-3=1
//   Kruskal: pick 0-1(1), 0-2(1), 1-3(1) => total=3
// Query [4,2]: 2 closest to server4 (5,5) are servers 4 and 3 (dist=2)
//   MST = 2
Console.WriteLine($"  Expected: [3, 2]");