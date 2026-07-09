/*
Title: Earliest Meeting Point in a One-Way Conveyor Network

Problem Description:
In a large warehouse, stations are connected by one-way conveyor belts. The warehouse is modeled as a directed graph with n stations labeled from 0 to n - 1. Each conveyor belt is represented by a directed edge [u, v], meaning a package can move from station u to station v in exactly 1 minute.

Two packages start at stations startA and startB at the same time. A station x is considered a valid meeting point if both packages can reach x by following the direction of the conveyor belts. The arrival time for a meeting point is defined as the later of the two arrival times, because both packages must have arrived.

Return the station index of the valid meeting point that minimizes this arrival time. If there are multiple such stations, return the smallest station index among them. If no valid meeting point exists, return -1.

You must consider the graph as unweighted, but it may contain cycles and disconnected components. A package is allowed to stay at its starting station, so if both packages start at the same station, that station is automatically a valid meeting point with arrival time 0.

Constraints:
- 1 <= n <= 100000
- 0 <= edges.length <= 200000
- edges[i] = [u, v]
- 0 <= u, v < n
- 0 <= startA, startB < n
- There may be duplicate edges, though they do not change the answer.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the adjacency list: O(n + m), where m = number of edges
    - First BFS from startA: O(n + m)
    - Second BFS from startB: O(n + m)
    - Final scan to choose the best meeting point: O(n)
    Overall: O(n + m)

    Space Complexity:
    - Adjacency list: O(n + m)
    - Distance arrays: O(n)
    - BFS queue: O(n)
    Overall: O(n + m)
    */
    public int EarliestMeetingPoint(int n, int[][] edges, int startA, int startB)
    {
        // Step 1:
        // Build an adjacency list for the directed graph.
        //
        // Why do we need this?
        // We need a fast way to find all outgoing neighbors of any station.
        // Since the graph is directed, if we have an edge u -> v, then from u
        // we can go to v, but not necessarily the other way around.
        //
        // Why adjacency list instead of adjacency matrix?
        // Because n can be as large as 100000, and edges can be up to 200000.
        // An adjacency matrix would use O(n^2) memory, which is far too large.
        // An adjacency list stores only the edges that actually exist, which is efficient.
        var graph = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<int>();
        }

        foreach (var edge in edges)
        {
            int u = edge[0];
            int v = edge[1];
            graph[u].Add(v);
        }

        // Step 2:
        // Compute the shortest distance from startA to every station.
        //
        // Why BFS?
        // Every conveyor move takes exactly 1 minute, so this is an unweighted graph.
        // In an unweighted graph, BFS gives shortest path distances in terms of number of edges.
        //
        // The result distA[i] means:
        // - the minimum number of minutes needed for package A to reach station i
        // - or -1 if station i is unreachable from startA
        int[] distA = BfsShortestDistances(graph, startA);

        // Step 3:
        // Compute the shortest distance from startB to every station using the same logic.
        int[] distB = BfsShortestDistances(graph, startB);

        // Step 4:
        // Examine every station and determine whether it is a valid meeting point.
        //
        // A station i is valid if:
        // - distA[i] != -1  (A can reach it)
        // - distB[i] != -1  (B can reach it)
        //
        // For a valid meeting point, the meeting time is:
        // max(distA[i], distB[i])
        //
        // Why max?
        // Because both packages start at the same time, and the meeting can only happen
        // once both have arrived. If one arrives earlier, it can wait there.
        //
        // We want:
        // 1. The smallest possible meeting time
        // 2. If multiple stations have the same best meeting time, choose the smallest index
        int bestNode = -1;
        int bestTime = int.MaxValue;

        for (int i = 0; i < n; i++)
        {
            // Skip stations that are not reachable by both packages.
            if (distA[i] == -1 || distB[i] == -1)
            {
                continue;
            }

            int meetingTime = Math.Max(distA[i], distB[i]);

            // Update answer if:
            // - we found a strictly better (smaller) meeting time
            // - or the meeting time is tied, but this station index is smaller
            if (meetingTime < bestTime || (meetingTime == bestTime && i < bestNode))
            {
                bestTime = meetingTime;
                bestNode = i;
            }
        }

        // If no valid meeting point was found, bestNode remains -1.
        return bestNode;
    }

    private int[] BfsShortestDistances(List<int>[] graph, int start)
    {
        int n = graph.Length;

        // Step 1:
        // Create and initialize the distance array.
        //
        // dist[i] = shortest distance from start to i
        // dist[i] = -1 means "not visited yet" / "unreachable so far"
        //
        // Why initialize to -1?
        // Because 0 is a valid distance for the starting node, so we need a different
        // value to represent "unreachable".
        int[] dist = new int[n];
        Array.Fill(dist, -1);

        // Step 2:
        // Standard BFS queue.
        //
        // Why a queue?
        // BFS explores nodes level by level:
        // - first all nodes at distance 0
        // - then all nodes at distance 1
        // - then all nodes at distance 2
        // and so on.
        //
        // This guarantees that the first time we visit a node, we have found
        // the shortest path to it in an unweighted graph.
        var queue = new Queue<int>();

        // Step 3:
        // Start BFS from the source node itself.
        //
        // Important detail:
        // A package is allowed to stay at its starting station.
        // Therefore the starting station is reachable in 0 minutes.
        dist[start] = 0;
        queue.Enqueue(start);

        // Step 4:
        // Process nodes in BFS order until there are no more reachable nodes to explore.
        while (queue.Count > 0)
        {
            int current = queue.Dequeue();

            // Explore every outgoing edge current -> neighbor.
            foreach (int neighbor in graph[current])
            {
                // If neighbor has already been visited, skip it.
                //
                // Why is this safe?
                // Because in BFS, the first time we assign a distance to a node,
                // that distance is the shortest possible one.
                if (dist[neighbor] != -1)
                {
                    continue;
                }

                // We reached neighbor from current using one more edge,
                // so its distance is current distance + 1.
                dist[neighbor] = dist[current] + 1;

                // Add neighbor to the queue so we can later explore its outgoing edges.
                queue.Enqueue(neighbor);
            }
        }

        // After BFS finishes:
        // - every reachable node has its shortest distance
        // - every unreachable node remains -1
        return dist;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// n = 6
// edges = [[0,2],[1,2],[2,3],[1,4],[4,3],[3,5]]
// startA = 0, startB = 1
// Expected output: 2
int n1 = 6;
int[][] edges1 =
{
    new[] { 0, 2 },
    new[] { 1, 2 },
    new[] { 2, 3 },
    new[] { 1, 4 },
    new[] { 4, 3 },
    new[] { 3, 5 }
};
int startA1 = 0;
int startB1 = 1;
int result1 = solution.EarliestMeetingPoint(n1, edges1, startA1, startB1);
Console.WriteLine(result1);

// Example 2:
// n = 5
// edges = [[0,1],[1,2],[3,4]]
// startA = 0, startB = 3
// Expected output: -1
int n2 = 5;
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 3, 4 }
};
int startA2 = 0;
int startB2 = 3;
int result2 = solution.EarliestMeetingPoint(n2, edges2, startA2, startB2);
Console.WriteLine(result2);