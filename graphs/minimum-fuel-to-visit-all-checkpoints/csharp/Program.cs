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

using System;
using System.Collections.Generic;

/*
 * APPROACH: Dijkstra + Bitmask DP (Traveling Salesman Problem variant)
 *
 * Key Insight:
 * We need to visit all checkpoints with minimum fuel. Since checkpoints.length <= 12,
 * we can use bitmask DP (similar to TSP) where each bit represents whether a checkpoint
 * has been visited.
 *
 * Step 1: Run Dijkstra from city 0 AND from each checkpoint city to get shortest
 *         distances between all "key" nodes (city 0 + all checkpoints).
 *
 * Step 2: Use bitmask DP where:
 *         dp[mask][i] = minimum fuel to have visited exactly the checkpoints
 *                       indicated by 'mask', currently at checkpoint i.
 *
 * Step 3: The answer is min over all i of dp[(1<<m)-1][i], where m = checkpoints.length.
 *
 * Why Dijkstra first?
 * The graph can have up to 1000 nodes and 5000 edges. We need shortest paths between
 * key nodes. Running Dijkstra from each key node (at most 13 nodes) gives us all
 * pairwise shortest distances efficiently.
 *
 * Why Bitmask DP?
 * With up to 12 checkpoints, there are 2^12 = 4096 possible subsets. Combined with
 * 12 possible "current" checkpoints, the DP table is only 4096 * 12 = ~49K states.
 * This is the classic TSP approach adapted for this problem.
 */

public class Solution
{
    // Time Complexity:  O(K * (N + E) * log N + K^2 * 2^K)
    //   where K = number of checkpoints (<=12), N = cities, E = roads
    //   - K+1 Dijkstra runs: O(K * (E log N))
    //   - Bitmask DP: O(K^2 * 2^K)
    //
    // Space Complexity: O(N + E + K * 2^K)
    //   - Adjacency list: O(N + E)
    //   - Dijkstra distance arrays: O(N) per run
    //   - DP table: O(K * 2^K)

    public int MinFuelToVisitCheckpoints(int n, int[][] roads, int[] checkpoints)
    {
        // -----------------------------------------------------------------------
        // STEP 1: Build the adjacency list for the graph
        // -----------------------------------------------------------------------
        // We use a list of (neighbor, weight) pairs for each city.
        // This allows efficient traversal during Dijkstra's algorithm.
        var adj = new List<(int city, int weight)>[n];
        for (int i = 0; i < n; i++)
            adj[i] = new List<(int, int)>();

        // Add each road as a bidirectional edge (since roads are bidirectional)
        foreach (var road in roads)
        {
            int u = road[0], v = road[1], w = road[2];
            adj[u].Add((v, w));
            adj[v].Add((u, w));
        }

        // -----------------------------------------------------------------------
        // STEP 2: Define the "key nodes" — city 0 plus all checkpoint cities
        // -----------------------------------------------------------------------
        // We only care about shortest distances between these key nodes.
        // City 0 is our starting point; checkpoints are our required destinations.
        int m = checkpoints.Length; // number of checkpoints

        // keyNodes[0] = city 0 (start), keyNodes[1..m] = checkpoint cities
        // This makes indexing consistent: key node index 0 is always city 0
        int[] keyNodes = new int[m + 1];
        keyNodes[0] = 0; // starting city
        for (int i = 0; i < m; i++)
            keyNodes[i + 1] = checkpoints[i];

        // -----------------------------------------------------------------------
        // STEP 3: Run Dijkstra from each key node to get shortest distances
        // -----------------------------------------------------------------------
        // dist[k][v] = shortest distance from keyNodes[k] to city v
        // We run Dijkstra m+1 times (once from city 0, once from each checkpoint)
        long[][] dist = new long[m + 1][];
        for (int k = 0; k <= m; k++)
        {
            dist[k] = Dijkstra(keyNodes[k], n, adj);
        }

        // -----------------------------------------------------------------------
        // STEP 4: Check reachability — if any checkpoint is unreachable from city 0,
        //         return -1 immediately
        // -----------------------------------------------------------------------
        // dist[0][keyNodes[i]] gives the shortest distance from city 0 to checkpoint i
        for (int i = 1; i <= m; i++)
        {
            if (dist[0][keyNodes[i]] == long.MaxValue)
                return -1; // This checkpoint is completely unreachable
        }

        // -----------------------------------------------------------------------
        // STEP 5: Build a compact distance matrix between key nodes
        // -----------------------------------------------------------------------
        // shortDist[i][j] = shortest distance from keyNodes[i] to keyNodes[j]
        // This avoids repeatedly indexing into the full dist arrays during DP.
        // Indices: 0 = city 0 (start), 1..m = checkpoints 0..m-1
        long[,] shortDist = new long[m + 1, m + 1];
        for (int i = 0; i <= m; i++)
        {
            for (int j = 0; j <= m; j++)
            {
                // Distance from key node i to key node j using Dijkstra results from node i
                shortDist[i, j] = dist[i][keyNodes[j]];
            }
        }

        // -----------------------------------------------------------------------
        // STEP 6: Bitmask DP Setup
        // -----------------------------------------------------------------------
        // dp[mask][i] = minimum fuel cost to:
        //   - Have visited exactly the checkpoints indicated by bits in 'mask'
        //   - Currently be at checkpoint index i (1-indexed in keyNodes, so checkpoint i-1)
        //
        // mask is a bitmask of m bits: bit j is set if checkpoint j has been visited
        // i ranges from 1 to m (representing checkpoints[0] to checkpoints[m-1])
        //
        // We use long to avoid integer overflow (costs can be large)
        int totalMasks = 1 << m; // 2^m possible subsets of checkpoints
        long[,] dp = new long[totalMasks, m + 1];

        // Initialize all DP states to "infinity" (unreachable)
        for (int mask = 0; mask < totalMasks; mask++)
            for (int i = 0; i <= m; i++)
                dp[mask, i] = long.MaxValue;

        // -----------------------------------------------------------------------
        // STEP 7: Initialize base cases
        // -----------------------------------------------------------------------
        // We start at city 0 (keyNodes[0]). The first move is to travel from city 0
        // to any single checkpoint. When we arrive at checkpoint i (1-indexed),
        // the mask has only bit (i-1) set, and the cost is shortDist[0, i].
        for (int i = 1; i <= m; i++)
        {
            long costFromStart = shortDist[0, i]; // cost from city 0 to checkpoint i
            if (costFromStart < long.MaxValue)
            {
                int initialMask = 1 << (i - 1); // only checkpoint i-1 is visited
                dp[initialMask, i] = costFromStart;
            }
        }

        // -----------------------------------------------------------------------
        // STEP 8: Fill the DP table
        // -----------------------------------------------------------------------
        // We iterate over all possible masks (subsets of checkpoints visited).
        // For each mask and each current position i, we try to extend by traveling
        // to an unvisited checkpoint j.
        //
        // Why iterate masks in order?
        // A mask with fewer bits set is always processed before masks with more bits,
        // ensuring that when we compute dp[newMask][j], dp[mask][i] is already finalized.
        for (int mask = 1; mask < totalMasks; mask++)
        {
            for (int i = 1; i <= m; i++)
            {
                // Skip if this state is unreachable or checkpoint i isn't in this mask
                if (dp[mask, i] == long.MaxValue) continue;
                if ((mask & (1 << (i - 1))) == 0) continue; // i must be in the current mask

                // Try traveling from current checkpoint i to each unvisited checkpoint j
                for (int j = 1; j <= m; j++)
                {
                    // Skip if checkpoint j is already visited (bit j-1 is set in mask)
                    if ((mask & (1 << (j - 1))) != 0) continue;

                    // Cost to travel from checkpoint i to checkpoint j
                    long travelCost = shortDist[i, j];
                    if (travelCost == long.MaxValue) continue; // j is unreachable from i

                    // New mask: add checkpoint j to the visited set
                    int newMask = mask | (1 << (j - 1));

                    // New total cost: current cost + travel cost to j
                    long newCost = dp[mask, i] + travelCost;

                    // Update DP if this path is cheaper
                    if (newCost < dp[newMask, j])
                        dp[newMask, j] = newCost;
                }
            }
        }

        // -----------------------------------------------------------------------
        // STEP 9: Find the answer
        // -----------------------------------------------------------------------
        // The full mask means all checkpoints are visited: (1 << m) - 1
        // We want the minimum cost over all possible "last checkpoint visited"
        int fullMask = totalMasks - 1;
        long answer = long.MaxValue;

        for (int i = 1; i <= m; i++)
        {
            if (dp[fullMask, i] < answer)
                answer = dp[fullMask, i];
        }

        // If answer is still MaxValue, it's impossible (though we checked reachability above)
        return answer == long.MaxValue ? -1 : (int)answer;
    }

    // -----------------------------------------------------------------------
    // HELPER: Dijkstra's Algorithm
    // -----------------------------------------------------------------------
    // Computes shortest distances from 'source' to all other cities.
    // Uses a min-heap (priority queue) for efficiency.
    //
    // Returns: array where result[v] = shortest distance from source to city v
    //          (long.MaxValue if unreachable)
    private long[] Dijkstra(int source, int n, List<(int city, int weight)>[] adj)
    {
        // Initialize all distances to "infinity"
        long[] dist = new long[n];
        Array.Fill(dist, long.MaxValue);
        dist[source] = 0; // Distance from source to itself is 0

        // Priority queue: (distance, city) — min-heap by distance
        // .NET 8's PriorityQueue<TElement, TPriority> is a min-heap by default
        var pq = new PriorityQueue<int, long>();
        pq.Enqueue(source, 0);

        while (pq.Count > 0)
        {
            // Dequeue the city with the smallest known distance
            pq.TryDequeue(out int city, out long currentDist);

            // If we've already found a shorter path to this city, skip
            // (This handles "stale" entries in the priority queue)
            if (currentDist > dist[city]) continue;

            // Explore all neighbors of the current city
            foreach (var (neighbor, weight) in adj[city])
            {
                long newDist = dist[city] + weight;

                // If we found a shorter path to neighbor, update and enqueue
                if (newDist < dist[neighbor])
                {
                    dist[neighbor] = newDist;
                    pq.Enqueue(neighbor, newDist);
                }
            }
        }

        return dist;
    }
}

// =============================================================================
// DEMO / TEST CODE
// =============================================================================
// Verify against the examples provided in the problem description

var solution = new Solution();

Console.WriteLine("=== Minimum Fuel to Visit All Checkpoints ===\n");

// -----------------------------------------------------------------------
// Example 1:
// n = 5, roads = [[0,1,2],[1,2,3],[2,3,1],[3,4,4],[1,4,10]], checkpoints = [2, 4]
//
// Graph:
//   0 --2-- 1 --3-- 2 --1-- 3 --4-- 4
//           |_________10___________|
//
// Shortest distances from city 0:
//   0->1 = 2, 0->2 = 5, 0->3 = 6, 0->4 = 10 (via 0->1->4) or 10 (via 0->1->2->3->4)
//
// Checkpoints: [2, 4]
// Key nodes: city0=0, cp0=2, cp1=4
//
// Options:
//   Visit 2 first then 4: dist(0,2) + dist(2,4) = 5 + 5 = 10
//   Visit 4 first then 2: dist(0,4) + dist(4,2) = 10 + 5 = 15
//
// Wait, the expected output is 6. Let me re-examine...
// Actually looking at the problem again: the example says output is 6.
// Let me recheck: roads = [[0,1,2],[1,2,3],[2,3,1],[3,4,4],[1,4,10]]
// dist(0,2) = 0->1->2 = 2+3 = 5
// dist(2,4) = 2->3->4 = 1+4 = 5
// Total visiting 2 then 4 = 5+5 = 10
// Hmm, but expected is 6. The problem description seems inconsistent in its explanation.
// Let me trust the algorithm and see what it produces.
// Actually re-reading: "6 via shortest paths summed from a Steiner-tree perspective"
// This might be a Steiner tree problem where we don't need to backtrack.
// The path 0->1->2->3->4 visits both checkpoints 2 and 4 with cost 2+3+1+4=10.
// The minimum spanning path through 0, 2, 4 might be different.
// Given the problem says output=6, perhaps the roads are different from what I'm reading.
// Our algorithm correctly computes TSP-style minimum, let's see what it outputs.
// -----------------------------------------------------------------------

int n1