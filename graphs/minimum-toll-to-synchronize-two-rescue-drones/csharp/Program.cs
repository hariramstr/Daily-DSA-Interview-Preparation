/*
Title: Minimum Toll to Synchronize Two Rescue Drones

Problem Description:
A disaster response team operates on a directed graph of air corridors with n stations labeled from 0 to n - 1.
Each corridor is represented as [u, v, w], meaning a drone can fly from station u to station v by paying toll w.

Two rescue drones start at different stations s1 and s2, and both must eventually reach the same rendezvous station r.
After meeting, exactly one of them needs to continue from r to the final supply station t, carrying the combined payload.

The total mission cost is:
- cost from s1 to r
- plus cost from s2 to r
- plus cost from r to t

We must return the minimum possible total mission cost over all valid rendezvous stations r.
If no such station exists, return -1.

Important observations:
- The graph is directed.
- Edge weights are non-negative.
- The rendezvous station may be equal to s1, s2, or t.
- The two drones travel independently before meeting, so their paths do not need to be disjoint.

Efficient idea:
For every possible meeting node r, we need:
dist(s1 -> r) + dist(s2 -> r) + dist(r -> t)

If we compute:
1) shortest distances from s1 to every node
2) shortest distances from s2 to every node
3) shortest distances from every node to t

then we can test every node r in O(1) time each.

How do we get "distance from every node to t" efficiently?
- Reverse every edge.
- Run Dijkstra from t on the reversed graph.
- That gives the shortest distance from each node to t in the original graph.

So the full solution is:
- Build original graph
- Build reversed graph
- Run Dijkstra 3 times:
  from s1 on original graph
  from s2 on original graph
  from t on reversed graph
- Try every node as rendezvous point and take the minimum valid sum
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
        Time Complexity:
        - Building the graphs: O(m)
        - Each Dijkstra run: O((n + m) log n)
        - We run Dijkstra 3 times, so total: O((n + m) log n)

        Space Complexity:
        - Graph storage: O(n + m)
        - Distance arrays: O(n)
        - Priority queue / heap usage: O(n + m) in the worst case across pushes
        - Total: O(n + m)
    */
    public long MinimumWeight(int n, int[][] corridors, int s1, int s2, int t)
    {
        // We will store the graph as adjacency lists.
        // graph[u] contains all outgoing edges from node u in the original graph.
        // reverseGraph[v] contains all outgoing edges from node v in the reversed graph.
        //
        // Why do we need both?
        // - The original graph is needed to compute shortest paths from s1 and s2 to all nodes.
        // - The reversed graph is needed to compute shortest paths from all nodes to t
        //   by running one Dijkstra starting from t in the reversed graph.
        var graph = new List<(int to, int weight)>[n];
        var reverseGraph = new List<(int to, int weight)>[n];

        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<(int to, int weight)>();
            reverseGraph[i] = new List<(int to, int weight)>();
        }

        // Build both adjacency lists.
        foreach (var edge in corridors)
        {
            int u = edge[0];
            int v = edge[1];
            int w = edge[2];

            graph[u].Add((v, w));
            reverseGraph[v].Add((u, w));
        }

        // Step 1:
        // Compute shortest distances from s1 to every node.
        // distFromS1[r] = cheapest cost for drone 1 to reach rendezvous node r.
        long[] distFromS1 = Dijkstra(n, graph, s1);

        // Step 2:
        // Compute shortest distances from s2 to every node.
        // distFromS2[r] = cheapest cost for drone 2 to reach rendezvous node r.
        long[] distFromS2 = Dijkstra(n, graph, s2);

        // Step 3:
        // Compute shortest distances from every node to t.
        //
        // Instead of running Dijkstra from every possible rendezvous node r to t
        // (which would be far too slow), we reverse all edges and run Dijkstra once from t.
        //
        // In the reversed graph:
        // distance from t to r (reversed) == distance from r to t (original)
        long[] distToT = Dijkstra(n, reverseGraph, t);

        // Step 4:
        // Try every node as the rendezvous station.
        //
        // For a node r to be valid:
        // - s1 must be able to reach r
        // - s2 must be able to reach r
        // - r must be able to reach t
        //
        // If all three distances are finite, then total cost is:
        // distFromS1[r] + distFromS2[r] + distToT[r]
        long answer = long.MaxValue;

        for (int r = 0; r < n; r++)
        {
            // If any part is unreachable, this node cannot be used as a meeting point.
            if (distFromS1[r] == long.MaxValue ||
                distFromS2[r] == long.MaxValue ||
                distToT[r] == long.MaxValue)
            {
                continue;
            }

            long total = distFromS1[r] + distFromS2[r] + distToT[r];

            if (total < answer)
            {
                answer = total;
            }
        }

        // If answer was never updated, there is no valid rendezvous station.
        return answer == long.MaxValue ? -1 : answer;
    }

    private long[] Dijkstra(int n, List<(int to, int weight)>[] graph, int start)
    {
        // We use long for distances because:
        // - each edge weight can be up to 1,000,000,000
        // - paths can contain many edges
        // - int could overflow
        long[] dist = new long[n];
        Array.Fill(dist, long.MaxValue);

        // The priority queue always gives us the node with the currently smallest known distance.
        //
        // In .NET's PriorityQueue<TElement, TPriority>:
        // - TElement is what we store
        // - TPriority is what determines ordering
        //
        // We store the node as the element and the current distance as the priority.
        var pq = new PriorityQueue<int, long>();

        // Distance from start to itself is 0.
        dist[start] = 0;
        pq.Enqueue(start, 0);

        // Standard Dijkstra loop.
        while (pq.Count > 0)
        {
            // Extract the node with the smallest priority (smallest tentative distance).
            pq.TryDequeue(out int current, out long currentDistance);

            // This is a very important optimization and correctness guard:
            // If the distance we popped is not equal to the best known distance anymore,
            // then this queue entry is "stale" and should be ignored.
            //
            // Why can stale entries exist?
            // Because when we find a better path to a node, we push the improved distance
            // into the priority queue, but the old worse entry may still remain inside.
            if (currentDistance != dist[current])
            {
                continue;
            }

            // Explore all outgoing edges from the current node.
            foreach (var (next, weight) in graph[current])
            {
                // Candidate distance if we go from start -> ... -> current -> next
                long newDistance = currentDistance + weight;

                // If this path is better than the best one we knew before,
                // update it and push the improved state into the priority queue.
                if (newDistance < dist[next])
                {
                    dist[next] = newDistance;
                    pq.Enqueue(next, newDistance);
                }
            }
        }

        return dist;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int n1 = 6;
int[][] corridors1 =
{
    new[] { 0, 2, 2 },
    new[] { 1, 2, 3 },
    new[] { 2, 3, 4 },
    new[] { 2, 4, 1 },
    new[] { 4, 3, 1 },
    new[] { 3, 5, 2 },
    new[] { 4, 5, 5 }
};
int s1_1 = 0;
int s2_1 = 1;
int t1 = 5;

// Correct calculation:
// Meet at 2:
// 0 -> 2 = 2
// 1 -> 2 = 3
// 2 -> 5 cheapest is 2 -> 4 -> 3 -> 5 = 1 + 1 + 2 = 4
// Total = 2 + 3 + 4 = 9
Console.WriteLine(solution.MinimumWeight(n1, corridors1, s1_1, s2_1, t1)); // Expected: 9

// Example 2
int n2 = 4;
int[][] corridors2 =
{
    new[] { 0, 1, 5 },
    new[] { 1, 3, 2 },
    new[] { 2, 0, 1 }
};
int s1_2 = 0;
int s2_2 = 2;
int t2 = 3;

// Best rendezvous is 0:
// s1 already at 0 => 0
// s2: 2 -> 0 => 1
// 0 -> 1 -> 3 => 7
// Total = 8
Console.WriteLine(solution.MinimumWeight(n2, corridors2, s1_2, s2_2, t2)); // Expected: 8