/*
 * Title: Minimum Time to Spread Signal Across Network
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Problem Description:
 * You are given a network of n nodes labeled from 1 to n, connected by directed edges.
 * Each edge has a travel time (weight). A signal is simultaneously initiated from a set
 * of source nodes at time 0. The signal spreads along directed edges and arrives at a
 * node only after the full travel time of the edge has elapsed.
 *
 * Return the minimum time it takes for ALL nodes in the network to receive the signal.
 * If it is impossible for all nodes to receive the signal, return -1.
 *
 * The signal from any source can reach any node, and the signal at a node is considered
 * received at the EARLIEST time any source's signal arrives at it.
 *
 * Key Insight:
 * This is a multi-source shortest path problem. We want the shortest path from ANY source
 * to EVERY node, then return the maximum of those shortest paths (because all nodes must
 * receive the signal, so we wait for the last one).
 *
 * Approach: Multi-Source Dijkstra's Algorithm
 * - Initialize all source nodes with distance 0 in the priority queue
 * - Run Dijkstra's to find shortest distance from any source to every node
 * - Return the maximum distance (the bottleneck node that receives signal last)
 */

using System;
using System.Collections.Generic;

/// <summary>
/// Solution class containing the algorithm to find minimum time to spread signal.
/// </summary>
public class Solution
{
    /// <summary>
    /// Finds the minimum time for all nodes to receive the signal using Multi-Source Dijkstra.
    ///
    /// Time Complexity:  O((E + N) * log N) where E = number of edges, N = number of nodes
    ///                   - Each edge is processed once, each node is extracted from the heap once
    ///                   - Priority queue operations cost O(log N)
    ///
    /// Space Complexity: O(N + E) for the adjacency list and distance array
    /// </summary>
    /// <param name="n">Number of nodes in the network (labeled 1 to n)</param>
    /// <param name="edges">Directed edges as [u, v, w] meaning u->v with travel time w</param>
    /// <param name="sources">List of source nodes that emit signal at time 0</param>
    /// <returns>Minimum time for all nodes to receive signal, or -1 if impossible</returns>
    public int MinTimeToSpread(int n, int[][] edges, int[] sources)
    {
        // -----------------------------------------------------------------------
        // STEP 1: Build an adjacency list representation of the graph
        // -----------------------------------------------------------------------
        // Why adjacency list? It's efficient for sparse graphs and allows us to
        // quickly find all neighbors of a given node during traversal.
        // We use nodes labeled 1..n, so we create a list of size n+1 (index 0 unused).
        // Each entry stores a list of (neighbor, travelTime) pairs.

        var adjacencyList = new List<(int neighbor, int travelTime)>[n + 1];
        for (int i = 0; i <= n; i++)
        {
            adjacencyList[i] = new List<(int, int)>();
        }

        // Populate the adjacency list from the edges array
        // Each edge [u, v, w] means: from node u, you can go to node v in w time units
        foreach (var edge in edges)
        {
            int u = edge[0]; // source of this directed edge
            int v = edge[1]; // destination of this directed edge
            int w = edge[2]; // travel time (weight) of this edge

            adjacencyList[u].Add((v, w));
            // Note: This is a DIRECTED graph, so we only add u -> v, not v -> u
        }

        // -----------------------------------------------------------------------
        // STEP 2: Initialize the distance array
        // -----------------------------------------------------------------------
        // dist[i] = the minimum time for node i to receive the signal from ANY source
        // We start with "infinity" (int.MaxValue) for all nodes, meaning unreachable.
        // Source nodes will be set to 0 because they emit the signal at time 0.

        int[] dist = new int[n + 1];
        for (int i = 0; i <= n; i++)
        {
            dist[i] = int.MaxValue; // Assume all nodes are unreachable initially
        }

        // -----------------------------------------------------------------------
        // STEP 3: Initialize the priority queue (min-heap) with all source nodes
        // -----------------------------------------------------------------------
        // This is the KEY INSIGHT for multi-source Dijkstra:
        // Instead of starting from a single node, we start from ALL sources simultaneously.
        // We enqueue all sources with distance 0, effectively treating them as if there's
        // a virtual "super-source" node connected to all sources with edge weight 0.
        //
        // The priority queue stores (currentDistance, nodeId) pairs.
        // C#'s PriorityQueue<TElement, TPriority> is a min-heap by default.

        // PriorityQueue<element, priority> - lower priority value = higher priority (min-heap)
        var priorityQueue = new PriorityQueue<int, int>(); // (nodeId, distanceSoFar)

        foreach (int source in sources)
        {
            dist[source] = 0;              // Source nodes receive signal at time 0
            priorityQueue.Enqueue(source, 0); // Add to queue with priority 0
        }

        // -----------------------------------------------------------------------
        // STEP 4: Run Dijkstra's Algorithm
        // -----------------------------------------------------------------------
        // Dijkstra's algorithm greedily processes nodes in order of their current
        // shortest known distance. This works because edge weights are non-negative.
        //
        // The algorithm:
        // 1. Extract the node with the smallest current distance from the priority queue
        // 2. If we've already found a better path to this node, skip it (stale entry)
        // 3. For each neighbor, check if going through the current node gives a shorter path
        // 4. If yes, update the distance and add to the priority queue

        while (priorityQueue.Count > 0)
        {
            // Extract the node with the minimum distance (the most promising node to process)
            priorityQueue.TryDequeue(out int currentNode, out int currentDist);

            // IMPORTANT: Skip stale entries in the priority queue
            // When we find a better path to a node, we add a new entry to the queue
            // but don't remove the old (worse) entry. So we must check if this entry
            // is outdated by comparing with the best known distance.
            if (currentDist > dist[currentNode])
            {
                // This is a stale entry — we already found a shorter path to currentNode
                // Skip it to avoid redundant processing
                continue;
            }

            // Process all outgoing edges from the current node
            // For each neighbor, try to "relax" the edge (update if we found a shorter path)
            foreach (var (neighbor, travelTime) in adjacencyList[currentNode])
            {
                // Calculate the time to reach 'neighbor' by going through 'currentNode'
                // newDist = time to reach currentNode + time to travel from currentNode to neighbor
                int newDist = currentDist + travelTime;

                // RELAXATION: If this new path is shorter than the best known path to 'neighbor'
                if (newDist < dist[neighbor])
                {
                    // Update the best known distance to 'neighbor'
                    dist[neighbor] = newDist;

                    // Add 'neighbor' to the priority queue with its new (better) distance
                    // We'll process it later when it's the minimum in the queue
                    priorityQueue.Enqueue(neighbor, newDist);
                }
            }
        }

        // -----------------------------------------------------------------------
        // STEP 5: Find the answer — maximum of all shortest distances
        // -----------------------------------------------------------------------
        // After Dijkstra's completes, dist[i] holds the minimum time for node i
        // to receive the signal from any source.
        //
        // We need ALL nodes to receive the signal, so we must wait for the LAST node
        // to receive it. This means we take the MAXIMUM of all dist[i] values.
        //
        // If any node has dist[i] == int.MaxValue, it's unreachable → return -1.

        int maxTime = 0; // Track the maximum time across all nodes

        for (int node = 1; node <= n; node++) // Nodes are labeled 1 to n
        {
            if (dist[node] == int.MaxValue)
            {
                // This node is unreachable from all sources — impossible to spread signal
                return -1;
            }

            // Update the maximum time seen so far
            if (dist[node] > maxTime)
            {
                maxTime = dist[node];
            }
        }

        // Return the time when the LAST node receives the signal
        return maxTime;
    }
}

// =============================================================================
// DEMO CODE — Verify with the examples from the problem description
// =============================================================================

Console.WriteLine("=== Minimum Time to Spread Signal Across Network ===\n");

var solution = new Solution();

// -----------------------------------------------------------------------
// Example 1:
// n = 4, edges = [[1,2,1],[1,3,4],[2,3,2],[3,4,1]], sources = [1]
// Expected Output: 4
//
// Trace:
// - Start: dist = [∞, 0, ∞, ∞, ∞], queue = [(1,0)]
// - Process node 1 (dist=0):
//     - Edge 1->2 (w=1): newDist=1 < ∞, update dist[2]=1, enqueue (2,1)
//     - Edge 1->3 (w=4): newDist=4 < ∞, update dist[3]=4, enqueue (3,4)
// - Process node 2 (dist=1):
//     - Edge 2->3 (w=2): newDist=3 < 4, update dist[3]=3, enqueue (3,3)
// - Process node 3 (dist=3):
//     - Edge 3->4 (w=1): newDist=4 < ∞, update dist[4]=4, enqueue (4,4)
// - Process node 3 again (dist=4): STALE (4 > dist[3]=3), skip
// - Process node 4 (dist=4): no outgoing edges
// - dist = [∞, 0, 1, 3, 4]
// - max(0, 1, 3, 4) = 4 ✓
// -----------------------------------------------------------------------

int n1 = 4;
int[][] edges1 = [[1, 2, 1], [1, 3, 4], [2, 3, 2], [3, 4, 1]];
int[] sources1 = [1];
int result1 = solution.MinTimeToSpread(n1, edges1, sources1);
Console.WriteLine($"Example 1:");
Console.WriteLine($"  n = {n1}");
Console.WriteLine($"  edges = [[1,2,1],[1,3,4],[2,3,2],[3,4,1]]");
Console.WriteLine($"  sources = [1]");
Console.WriteLine($"  Output:   {result1}");
Console.WriteLine($"  Expected: 4");
Console.WriteLine($"  Correct:  {result1 == 4}\n");

// -----------------------------------------------------------------------
// Example 2:
// n = 4, edges = [[1,2,1],[3,4,1]], sources = [1,3]
// Expected Output: 1
//
// Trace:
// - Start: dist = [∞, 0, ∞, 0, ∞], queue = [(1,0), (3,0)]
// - Process node 1 (dist=0):
//     - Edge 1->2 (w=1): newDist=1 < ∞, update dist[2]=1, enqueue (2,1)
// - Process node 3 (dist=0):
//     - Edge 3->4 (w=1): newDist=1 < ∞, update dist[4]=1, enqueue (4,1)
// - Process node 2 (dist=1): no outgoing edges
// - Process node 4 (dist=1): no outgoing edges
// - dist = [∞, 0, 1, 0, 1]
// - max(0, 1, 0, 1) = 1 ✓
// -----------------------------------------------------------------------

int n2 = 4;
int[][] edges2 = [[1, 2, 1], [3, 4, 1]];
int[] sources2 = [1, 3];
int result2 = solution.MinTimeToSpread(n2, edges2, sources2);
Console.WriteLine($"Example 2:");
Console.WriteLine($"  n = {n2}");
Console.WriteLine($"  edges = [[1,2,1],[3,4,1]]");
Console.WriteLine($"  sources = [1,3]");
Console.WriteLine($"  Output:   {result2}");
Console.WriteLine($"  Expected: 1");
Console.WriteLine($"  Correct:  {result2 == 1}\n");

// -----------------------------------------------------------------------
// Example 3: Impossible case — node 4 is unreachable
// n = 4, edges = [[1,2,1],[2,3,1]], sources = [1]
// Expected Output: -1
// -----------------------------------------------------------------------

int n3 = 4;
int[][] edges3 = [[1, 2, 1], [2, 3, 1]];
int[] sources3 = [1];
int result3 = solution.MinTimeToSpread(n3, edges3, sources3);
Console.WriteLine($"Example 3 (Impossible):");
Console.WriteLine($"  n = {n3}");
Console.WriteLine($"  edges = [[1,2,1],[2,3,1]]");
Console.WriteLine($"  sources = [1]");
Console.WriteLine($"  Output:   {result3}");
Console.WriteLine($"  Expected: -1");
Console.WriteLine($"  Correct:  {result3 == -1}\n");

// -----------------------------------------------------------------------
// Example 4: All nodes are sources — signal received instantly
// n = 3, edges = [[1,2,5],[2,3,5]], sources = [1,2,3]
// Expected Output: 0
// -----------------------------------------------------------------------

int n4 = 3;
int[][] edges4 = [[1, 2, 5], [2, 3, 5]];
int[] sources4 = [1, 2, 3];
int result4 = solution.MinTimeToSpread(n4, edges4, sources4);
Console.WriteLine($"Example 4 (All sources):");
Console.WriteLine($"  n = {n4}");
Console.WriteLine($"  edges = [[1,2,5],[2,3,5]]");
Console.WriteLine($"  sources = [1,2,3]");
Console.WriteLine($"  Output:   {result4}");
Console.WriteLine($"  Expected: 0");
Console.WriteLine($"  Correct:  {result4 == 0}\n");

// -----------------------------------------------------------------------
// Example 5: Multiple edges between same pair, pick shortest
// n = 2, edges = [[1,2,10],[1,2,3]], sources = [1]
// Expected Output: 3
// -----------------------------------------------------------------------

int n5 = 2;
int[][] edges5 = [[1, 2, 10], [1, 2, 3]];
int[] sources5 = [1];
int result5 = solution.MinTimeToSpread(n5, edges5, sources5);
Console.WriteLine($"Example 5 (Multiple edges, pick shortest):");
Console.WriteLine($"  n = {n5}");
Console.WriteLine($"  edges = [[1,2,10],[1,2,3]]");
Console.WriteLine($"  sources = [1]");
Console.WriteLine($"  Output:   {result5}");
Console.WriteLine($"  Expected: 3");
Console.WriteLine($"  Correct:  {result5