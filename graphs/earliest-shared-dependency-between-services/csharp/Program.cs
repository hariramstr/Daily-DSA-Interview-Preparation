/*
Title: Earliest Shared Dependency Between Services

Problem Description:
You are given a directed graph representing service dependencies in a microservice platform.
An edge [a, b] means service a directly depends on service b.
If service X depends on Y, and Y depends on Z, then X also indirectly depends on Z.

For two given services s1 and s2, find the shared dependency that can be reached from both
services with the smallest combined number of dependency hops.
If there are multiple such services with the same minimum total distance, return the one with
the smallest service id.
If no shared dependency exists, return -1.

More formally:
For every node x that is reachable from both s1 and s2, define:
score(x) = dist(s1, x) + dist(s2, x)
where dist(u, v) is the minimum number of directed edges from u to v.

Return the reachable node with the minimum score, breaking ties by smaller node id.

Important notes:
- The graph may contain cycles.
- Self-loops may exist.
- The solution must safely handle cycles and large sparse graphs.

Examples:
1)
n = 6
edges = [[0,2],[1,2],[2,3],[1,4],[4,3],[0,5]]
s1 = 0, s2 = 1
Reachable from 0: 0,2,3,5
Reachable from 1: 1,2,3,4
Shared: 2,3
score(2) = 1 + 1 = 2
score(3) = 2 + 2 = 4
Answer = 2

2)
n = 5
edges = [[0,1],[1,2],[2,0],[3,2],[3,4]]
s1 = 0, s2 = 3
From 0: reachable nodes include 0,1,2 because of the cycle.
From 3: reachable nodes include 3,2,4,0,1
Shared: 0,1,2
score(0) = 0 + 2 = 2
score(1) = 1 + 3 = 4
score(2) = 2 + 1 = 3
Answer = 0
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the adjacency list takes O(n + m), where:
      n = number of nodes
      m = number of edges
    - Each BFS traversal takes O(n + m) in the worst case, because every node and edge
      is processed at most once.
    - We run BFS twice, so total is still O(n + m).
    - Final scan through all nodes takes O(n).
    Overall: O(n + m)

    Space Complexity:
    - Adjacency list uses O(n + m)
    - Distance arrays use O(n)
    - BFS queue uses O(n) in the worst case
    Overall: O(n + m)
    */
    public int EarliestSharedDependency(int n, int[][] edges, int s1, int s2)
    {
        // Step 1:
        // Build an adjacency list for the directed graph.
        //
        // Why we do this:
        // We need an efficient way to find all direct dependencies of a service.
        // For each node, we want quick access to all outgoing neighbors.
        //
        // Data structure choice:
        // We use List<int>[] where graph[u] contains all nodes v such that u -> v.
        // This is memory-efficient for sparse graphs and supports fast iteration.
        var graph = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<int>();
        }

        foreach (var edge in edges)
        {
            int from = edge[0];
            int to = edge[1];
            graph[from].Add(to);
        }

        // Step 2:
        // Compute the shortest distance from s1 to every node using BFS.
        //
        // Why BFS:
        // All edges have equal weight (1 hop), so BFS gives the minimum number of edges
        // from the source to every reachable node.
        //
        // Why this is safe with cycles:
        // We store distances and only visit a node the first time we reach it.
        // Since BFS explores in increasing distance order, the first time we assign
        // a distance is guaranteed to be the shortest.
        int[] distFromS1 = BfsShortestDistances(graph, s1);

        // Step 3:
        // Compute the shortest distance from s2 to every node using the same BFS logic.
        int[] distFromS2 = BfsShortestDistances(graph, s2);

        // Step 4:
        // Scan every node and find the shared reachable node with the smallest combined score.
        //
        // A node is a valid shared dependency if:
        // - it is reachable from s1
        // - it is reachable from s2
        //
        // In our distance arrays, unreachable nodes are marked as -1.
        //
        // Tie-breaking rule:
        // If two nodes have the same score, choose the smaller node id.
        int bestNode = -1;
        long bestScore = long.MaxValue;

        for (int node = 0; node < n; node++)
        {
            // If either source cannot reach this node, it is not a shared dependency.
            if (distFromS1[node] == -1 || distFromS2[node] == -1)
            {
                continue;
            }

            long score = (long)distFromS1[node] + distFromS2[node];

            // Update answer if:
            // 1) this score is better than the best score seen so far, or
            // 2) the score ties and this node id is smaller
            if (score < bestScore || (score == bestScore && node < bestNode))
            {
                bestScore = score;
                bestNode = node;
            }
        }

        // If no shared reachable node was found, bestNode remains -1.
        return bestNode;
    }

    private int[] BfsShortestDistances(List<int>[] graph, int start)
    {
        int n = graph.Length;

        // Step A:
        // Create and initialize the distance array.
        //
        // Meaning of dist[i]:
        // - dist[i] = shortest number of edges from 'start' to i
        // - dist[i] = -1 means i has not been reached
        //
        // Why initialize to -1:
        // It gives us a simple way to represent "unvisited / unreachable".
        int[] dist = new int[n];
        Array.Fill(dist, -1);

        // Step B:
        // Set the starting node distance to 0 because it takes zero hops to reach itself.
        dist[start] = 0;

        // Step C:
        // Standard BFS queue.
        //
        // Why a queue:
        // BFS explores nodes level by level:
        // first all nodes at distance 0,
        // then all nodes at distance 1,
        // then distance 2, and so on.
        //
        // This guarantees shortest path lengths in an unweighted graph.
        var queue = new Queue<int>();
        queue.Enqueue(start);

        // Step D:
        // Process nodes in BFS order until there are no more reachable nodes to explore.
        while (queue.Count > 0)
        {
            int current = queue.Dequeue();

            // Explore every direct dependency of the current node.
            foreach (int next in graph[current])
            {
                // If next has already been assigned a distance, we have already found
                // the shortest path to it, so we skip it.
                //
                // This is especially important for graphs with cycles:
                // it prevents infinite loops and repeated work.
                if (dist[next] != -1)
                {
                    continue;
                }

                // The shortest distance to 'next' is one more than the distance to 'current'.
                dist[next] = dist[current] + 1;

                // Add it to the queue so we can later explore its outgoing edges.
                queue.Enqueue(next);
            }
        }

        return dist;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int n1 = 6;
int[][] edges1 =
{
    new[] { 0, 2 },
    new[] { 1, 2 },
    new[] { 2, 3 },
    new[] { 1, 4 },
    new[] { 4, 3 },
    new[] { 0, 5 }
};
int s1a = 0;
int s2a = 1;
int result1 = solution.EarliestSharedDependency(n1, edges1, s1a, s2a);
Console.WriteLine(result1); // Expected: 2

// Example 2
int n2 = 5;
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 2, 0 },
    new[] { 3, 2 },
    new[] { 3, 4 }
};
int s1b = 0;
int s2b = 3;
int result2 = solution.EarliestSharedDependency(n2, edges2, s1b, s2b);
Console.WriteLine(result2); // Expected: 0

// Additional example with no shared dependency
int n3 = 4;
int[][] edges3 =
{
    new[] { 0, 1 },
    new[] { 2, 3 }
};
int s1c = 0;
int s2c = 2;
int result3 = solution.EarliestSharedDependency(n3, edges3, s1c, s2c);
Console.WriteLine(result3); // Expected: -1