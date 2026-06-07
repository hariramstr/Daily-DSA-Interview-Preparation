/*
 * Title: Shortest Path Through Mandatory Checkpoints
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Problem Description:
 * You are given a weighted undirected graph with `n` nodes (labeled `0` to `n-1`)
 * and a list of edges where edges[i] = [u, v, w] represents an edge between node u
 * and node v with weight w. You are also given a source node `src`, a destination
 * node `dst`, and a list of `k` mandatory checkpoint nodes `checkpoints`.
 *
 * Your task is to find the shortest path from `src` to `dst` that passes through
 * ALL of the mandatory checkpoints (in any order). If no such path exists, return -1.
 *
 * Note: You may visit nodes multiple times, and the path does not need to be simple.
 *
 * Approach:
 * We use a combination of:
 *   1. Dijkstra's algorithm to precompute shortest distances between all "key nodes"
 *      (src, dst, and all checkpoints).
 *   2. Bitmask DP (Dynamic Programming) over subsets of checkpoints visited.
 *
 * Why bitmask DP?
 *   - We need to visit ALL checkpoints in ANY order.
 *   - With up to k=10 checkpoints, there are 2^10 = 1024 possible subsets.
 *   - dp[mask][i] = minimum cost to have visited exactly the checkpoints in `mask`,
 *     currently at key-node index i.
 *   - This is essentially the Traveling Salesman Problem (TSP) variant.
 */

using System;
using System.Collections.Generic;

// ============================================================
// SOLUTION CLASS
// ============================================================
public class Solution
{
    /*
     * Time Complexity:  O((K+2)^2 * (N + E) * log N + 2^K * (K+2)^2)
     *   - We run Dijkstra (K+2) times (once per key node): O((N + E) log N) each.
     *   - Bitmask DP has 2^K states * (K+2) current positions * (K+2) transitions.
     *   With K<=10, N<=100, E<=500, this is very fast.
     *
     * Space Complexity: O((K+2)^2 + 2^K * (K+2) + N + E)
     *   - Distance matrix between key nodes: O((K+2)^2)
     *   - DP table: O(2^K * (K+2))
     *   - Adjacency list: O(N + E)
     */
    public int ShortestPathThroughCheckpoints(
        int n,
        int[][] edges,
        int src,
        int dst,
        int[] checkpoints)
    {
        // ----------------------------------------------------------------
        // STEP 1: Build the adjacency list for the undirected weighted graph.
        // ----------------------------------------------------------------
        // We use a list of (neighbor, weight) pairs for each node.
        // An adjacency list is efficient for sparse graphs and works well
        // with Dijkstra's algorithm.
        var adj = new List<(int neighbor, int weight)>[n];
        for (int i = 0; i < n; i++)
            adj[i] = new List<(int, int)>();

        foreach (var edge in edges)
        {
            int u = edge[0], v = edge[1], w = edge[2];
            // Undirected graph: add edge in both directions
            adj[u].Add((v, w));
            adj[v].Add((u, w));
        }

        // ----------------------------------------------------------------
        // STEP 2: Define the "key nodes" — nodes we care about for DP.
        // ----------------------------------------------------------------
        // Key nodes are: src (index 0), dst (index 1), checkpoints (indices 2..K+1)
        // We need shortest distances between every pair of key nodes.
        // This lets us reduce the problem: instead of tracking full paths,
        // we just track which checkpoints have been visited and where we are.
        int k = checkpoints.Length;

        // keyNodes[0] = src, keyNodes[1] = dst, keyNodes[2..] = checkpoints
        int[] keyNodes = new int[k + 2];
        keyNodes[0] = src;
        keyNodes[1] = dst;
        for (int i = 0; i < k; i++)
            keyNodes[i + 2] = checkpoints[i];

        int totalKeys = k + 2; // total number of key nodes

        // ----------------------------------------------------------------
        // STEP 3: Run Dijkstra from each key node to find shortest distances
        //         from that key node to all other nodes in the graph.
        // ----------------------------------------------------------------
        // dist[i][j] = shortest distance from keyNodes[i] to keyNodes[j]
        // We store the full Dijkstra result for each key node, then extract
        // distances to other key nodes.
        int[,] distBetweenKeys = new int[totalKeys, totalKeys];

        // Initialize all distances to "infinity"
        const int INF = int.MaxValue / 2;
        for (int i = 0; i < totalKeys; i++)
            for (int j = 0; j < totalKeys; j++)
                distBetweenKeys[i, j] = (i == j) ? 0 : INF;

        // Run Dijkstra from each key node
        for (int ki = 0; ki < totalKeys; ki++)
        {
            // Dijkstra returns shortest distance from keyNodes[ki] to every node
            int[] shortestFromKi = Dijkstra(keyNodes[ki], n, adj);

            // Extract distances to all other key nodes
            for (int kj = 0; kj < totalKeys; kj++)
            {
                distBetweenKeys[ki, kj] = shortestFromKi[keyNodes[kj]];
            }
        }

        // ----------------------------------------------------------------
        // STEP 4: Bitmask DP to find the shortest path visiting all checkpoints.
        // ----------------------------------------------------------------
        // State: dp[mask][i]
        //   - mask: a bitmask of which checkpoints have been visited
        //           bit j is set if checkpoint j (keyNodes[j+2]) has been visited
        //   - i: index into keyNodes array (0=src, 1=dst, 2..k+1=checkpoints)
        //         representing our current position among key nodes
        //
        // dp[mask][i] = minimum total distance traveled, having visited exactly
        //               the checkpoints indicated by `mask`, currently at keyNodes[i]
        //
        // We want to find: min over all full masks of dp[fullMask][1]
        //   where fullMask = (1 << k) - 1 (all checkpoints visited)
        //   and index 1 = dst

        int fullMask = (1 << k) - 1; // all k checkpoints visited
        int totalMasks = 1 << k;

        // dp[mask][i] initialized to INF
        int[,] dp = new int[totalMasks, totalKeys];
        for (int mask = 0; mask < totalMasks; mask++)
            for (int i = 0; i < totalKeys; i++)
                dp[mask, i] = INF;

        // Base case: we start at src (keyNode index 0) with no checkpoints visited
        dp[0, 0] = 0;

        // ----------------------------------------------------------------
        // STEP 5: Fill the DP table.
        // ----------------------------------------------------------------
        // We iterate over all masks in increasing order (smaller subsets first).
        // For each state (mask, currentPos), we try moving to each other key node.
        //
        // Transitions:
        //   From state (mask, i) with cost dp[mask][i]:
        //   - Move to checkpoint j (keyNode index j+2, not yet visited):
        //       new mask = mask | (1 << j)
        //       new cost = dp[mask][i] + distBetweenKeys[i][j+2]
        //   - Move to dst (keyNode index 1) only if all checkpoints visited:
        //       This is handled at the end when we read dp[fullMask][1]
        //       But we can also move to dst at any time and it's captured naturally.

        for (int mask = 0; mask < totalMasks; mask++)
        {
            for (int i = 0; i < totalKeys; i++)
            {
                // Skip states that are unreachable
                if (dp[mask, i] == INF) continue;

                int currentCost = dp[mask, i];

                // Try moving to each checkpoint that hasn't been visited yet
                for (int j = 0; j < k; j++)
                {
                    // Check if checkpoint j is already in the mask
                    if ((mask & (1 << j)) != 0) continue; // already visited, skip

                    // Checkpoint j corresponds to keyNode index j+2
                    int nextKeyIndex = j + 2;
                    int travelCost = distBetweenKeys[i, nextKeyIndex];

                    // Only proceed if there's a valid path to this checkpoint
                    if (travelCost == INF) continue;

                    int newMask = mask | (1 << j);
                    int newCost = currentCost + travelCost;

                    // Relax the DP state: update if we found a cheaper way
                    if (newCost < dp[newMask, nextKeyIndex])
                        dp[newMask, nextKeyIndex] = newCost;
                }

                // Also try moving to dst (keyNode index 1) from current position.
                // We record this as moving to dst with the current mask.
                // This allows us to reach dst after visiting all checkpoints.
                // (We only care about dp[fullMask][1] at the end, so partial
                //  moves to dst with incomplete masks won't be used in the answer,
                //  but we still update them for completeness — they won't hurt.)
                {
                    int travelCost = distBetweenKeys[i, 1]; // cost to reach dst
                    if (travelCost != INF)
                    {
                        int newCost = currentCost + travelCost;
                        if (newCost < dp[mask, 1])
                            dp[mask, 1] = newCost;
                    }
                }
            }
        }

        // ----------------------------------------------------------------
        // STEP 6: Extract the answer.
        // ----------------------------------------------------------------
        // The answer is the minimum cost to be at dst (keyNode index 1)
        // with all checkpoints visited (mask = fullMask).
        int answer = dp[fullMask, 1];

        // If answer is still INF, no valid path exists
        return answer >= INF ? -1 : answer;
    }

    // ----------------------------------------------------------------
    // HELPER: Dijkstra's Algorithm
    // ----------------------------------------------------------------
    // Computes the shortest distance from `startNode` to every other node
    // in the graph using a min-heap (priority queue).
    //
    // Why Dijkstra? The graph has non-negative edge weights, making Dijkstra
    // optimal. It runs in O((N + E) log N) time.
    //
    // Returns: int[] dist where dist[v] = shortest distance from startNode to v
    private int[] Dijkstra(int startNode, int n, List<(int neighbor, int weight)>[] adj)
    {
        const int INF = int.MaxValue / 2;

        // dist[v] = best known shortest distance from startNode to v
        int[] dist = new int[n];
        Array.Fill(dist, INF);
        dist[startNode] = 0;

        // Priority queue: (distance, node)
        // .NET 8 has a built-in PriorityQueue<TElement, TPriority>
        var pq = new PriorityQueue<int, int>(); // (node, priority=distance)
        pq.Enqueue(startNode, 0);

        while (pq.Count > 0)
        {
            // Extract the node with the smallest known distance
            pq.TryDequeue(out int current, out int currentDist);

            // If this entry is outdated (we already found a shorter path), skip it
            if (currentDist > dist[current]) continue;

            // Explore all neighbors of `current`
            foreach (var (neighbor, weight) in adj[current])
            {
                int newDist = dist[current] + weight;

                // Relaxation: if we found a shorter path to `neighbor`, update it
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

// ============================================================
// DEMO CODE (Top-Level Statements)
// ============================================================

var solution = new Solution();

// -------------------------------------------------------
// Example 1:
// n=5, edges=[[0,1,2],[1,2,3],[2,3,1],[3,4,4],[0,3,10]]
// src=0, dst=4, checkpoints=[2]
// Expected Output: 10
// Trace: 0→1→2→3→4 = 2+3+1+4 = 10
// -------------------------------------------------------
Console.WriteLine("=== Example 1 ===");
int n1 = 5;
int[][] edges1 = new int[][]
{
    new int[] {0, 1, 2},
    new int[] {1, 2, 3},
    new int[] {2, 3, 1},
    new int[] {3, 4, 4},
    new int[] {0, 3, 10}
};
int src1 = 0, dst1 = 4;
int[] checkpoints1 = new int[] {2};

int result1 = solution.ShortestPathThroughCheckpoints(n1, edges1, src1, dst1, checkpoints1);
Console.WriteLine($"Input: n={n1}, src={src1}, dst={dst1}, checkpoints=[2]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine($"Expected: 10");
Console.WriteLine($"Correct: {result1 == 10}");
Console.WriteLine();

// -------------------------------------------------------
// Example 2:
// n=4, edges=[[0,1,1],[1,2,1],[2,3,1],[0,3,10]]
// src=0, dst=3, checkpoints=[1,2]
// Expected Output: 3
// Trace: 0→1→2→3 = 1+1+1 = 3
// -------------------------------------------------------
Console.WriteLine("=== Example 2 ===");
int n2 = 4;
int[][] edges2 = new int[][]
{
    new int[] {0, 1, 1},
    new int[] {1, 2, 1},
    new int[] {2, 3, 1},
    new int[] {0, 3, 10}
};
int src2 = 0, dst2 = 3;
int[] checkpoints2 = new int[] {1, 2};

int result2 = solution.ShortestPathThroughCheckpoints(n2, edges2, src2, dst2, checkpoints2);
Console.WriteLine($"Input: n={n2}, src={src2}, dst={dst2}, checkpoints=[1,2]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine($"Expected: 3");
Console.WriteLine($"Correct: {result2 == 3}");
Console.WriteLine();

// -------------------------------------------------------
// Example 3: No valid path (disconnected graph)
// Expected Output: -1
// -------------------------------------------------------
Console.WriteLine("=== Example 3: No valid path ===");
int n3 = 4;
int[][] edges3 = new int[][]
{
    new int[] {0, 1, 5},
    new int[] {2, 3, 5}  // disconnected from 0 and 1
};
int src3 = 0, dst3 = 3;
int[] checkpoints3 = new int[] {1};

int result3 