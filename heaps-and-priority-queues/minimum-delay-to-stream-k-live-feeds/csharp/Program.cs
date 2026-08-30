/*
Title: Minimum Delay to Stream K Live Feeds

Problem Description:
A media platform must deliver a live event to viewers through a network of relay servers.
The network is an undirected weighted graph with n servers labeled 0 to n - 1.
Each edge [u, v, w] means a stream can be forwarded between servers u and v with transmission delay w milliseconds.
Server 0 is the origin server. Some servers are marked as viewer gateways, and at least k of those gateways must receive the stream.

You may choose any subset of the viewer gateways as long as at least k of them are reached.
The cost of a delivery plan is the maximum shortest-path delay from server 0 to any chosen gateway in that plan.
Your task is to return the minimum possible cost.

In other words, compute the shortest delay from server 0 to every gateway, then choose k gateways so that the largest chosen delay is as small as possible.
If fewer than k gateways are reachable from server 0, return -1.

This problem is designed for large sparse graphs, so an efficient heap-based shortest path approach is expected.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the adjacency list: O(m)
    - Dijkstra's algorithm with a priority queue: O((n + m) log n)
    - Collecting and sorting reachable gateway distances: O(g log g), where g = gateways.Length
    Overall: O((n + m) log n + g log g)

    Space Complexity:
    - Adjacency list: O(n + m)
    - Distance array: O(n)
    - Priority queue: O(n) in the worst case
    - Gateway distance list: O(g)
    Overall: O(n + m + g)
    */
    public long MinimumDelayToStreamKFeeds(int n, int[][] edges, int[] gateways, int k)
    {
        // Step 1:
        // Build the graph as an adjacency list.
        //
        // Why we do this:
        // The input graph can be very large and sparse.
        // An adjacency list is the standard efficient representation for sparse graphs.
        // For each node, we store only its actual neighbors instead of an n x n matrix.
        //
        // Since the graph is undirected, every edge [u, v, w] must be added in both directions:
        // - u -> v with weight w
        // - v -> u with weight w
        var graph = new List<(int to, int weight)>[n];
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<(int to, int weight)>();
        }

        foreach (var edge in edges)
        {
            int u = edge[0];
            int v = edge[1];
            int w = edge[2];

            graph[u].Add((v, w));
            graph[v].Add((u, w));
        }

        // Step 2:
        // Prepare the distance array for Dijkstra's algorithm.
        //
        // dist[x] will store the shortest known distance from server 0 to server x.
        // We start by assuming every node is unreachable, so we initialize with "infinity".
        //
        // We use long instead of int because:
        // - edge weights can be as large as 1e9
        // - paths can contain many edges
        // - the total shortest path value may exceed the int range
        long[] dist = new long[n];
        Array.Fill(dist, long.MaxValue);

        // The source server is 0, so its distance to itself is 0.
        dist[0] = 0;

        // Step 3:
        // Create a min-priority queue for Dijkstra's algorithm.
        //
        // Why a priority queue?
        // Dijkstra's algorithm repeatedly needs the currently closest unprocessed node.
        // A min-heap / priority queue gives us that efficiently.
        //
        // We store:
        // - element: node index
        // - priority: current shortest known distance
        var pq = new PriorityQueue<int, long>();
        pq.Enqueue(0, 0);

        // Step 4:
        // Run Dijkstra's algorithm from source node 0.
        //
        // Core idea:
        // Always expand the node with the smallest known distance first.
        // Because all edge weights are positive, once we process a node at its best distance,
        // that distance is final.
        while (pq.Count > 0)
        {
            // Extract the node with the smallest currently known distance.
            int current = pq.Dequeue();

            // Important detail:
            // PriorityQueue in .NET does not automatically remove older worse entries
            // when we later discover a better distance for the same node.
            //
            // So we need to know what priority was associated with the dequeued node.
            // Since Dequeue() alone does not give the priority, we use TryPeek before dequeueing
            // in a different pattern in many implementations.
            //
            // However, here we can safely use a small workaround:
            // after dequeueing "current", we rely on checking neighbors using dist[current].
            // This is still correct because even if "current" was inserted multiple times,
            // relaxing from dist[current] is always based on the best known distance.
            //
            // To make the stale-entry check explicit and fully correct, we instead use
            // Dequeue with priority by storing pairs in the queue as node and using the
            // current dist as priority when enqueued, then compare against a separately
            // tracked popped priority. Since PriorityQueue<int,long> does not return both
            // directly from Dequeue(), we use TryDequeue below in a more explicit form.
            //
            // Because we already used Dequeue() above, we will restructure the loop below.
            // This comment remains educational, but the actual loop implementation should
            // use TryDequeue. So we break here only in explanation; actual code continues
            // with a corrected implementation.
            break;
        }

        // Re-run Dijkstra correctly using TryDequeue so we can detect stale entries.
        Array.Fill(dist, long.MaxValue);
        dist[0] = 0;
        pq = new PriorityQueue<int, long>();
        pq.Enqueue(0, 0);

        while (pq.TryDequeue(out int node, out long currentDistance))
        {
            // Step 4a:
            // Ignore stale queue entries.
            //
            // Why stale entries happen:
            // Suppose we first discover a path to node X with cost 15 and enqueue it.
            // Later we discover a better path with cost 10 and enqueue X again.
            // The old "15" entry is now outdated.
            //
            // If the popped priority is not equal to the best known distance in dist[node],
            // then this queue entry is stale and should be skipped.
            if (currentDistance != dist[node])
            {
                continue;
            }

            // Step 4b:
            // Explore all neighbors of the current node.
            //
            // For each edge node -> neighbor with weight w,
            // we check whether going through "node" gives a shorter path to "neighbor".
            foreach (var (neighbor, weight) in graph[node])
            {
                long newDistance = currentDistance + weight;

                // Step 4c:
                // Relaxation step.
                //
                // If we found a shorter path to the neighbor, update it and push it into the heap.
                //
                // This is the heart of Dijkstra's algorithm:
                // repeatedly improve shortest known distances until no better path exists.
                if (newDistance < dist[neighbor])
                {
                    dist[neighbor] = newDistance;
                    pq.Enqueue(neighbor, newDistance);
                }
            }
        }

        // Step 5:
        // Collect the shortest distances for all reachable gateway servers.
        //
        // We only care about gateways because the problem asks us to choose at least k gateways.
        // If a gateway has distance long.MaxValue, it means it is unreachable from server 0.
        var reachableGatewayDistances = new List<long>();

        foreach (int gateway in gateways)
        {
            if (dist[gateway] != long.MaxValue)
            {
                reachableGatewayDistances.Add(dist[gateway]);
            }
        }

        // Step 6:
        // If fewer than k gateways are reachable, the task is impossible.
        if (reachableGatewayDistances.Count < k)
        {
            return -1;
        }

        // Step 7:
        // Sort the reachable gateway distances in ascending order.
        //
        // Why sorting solves the second half of the problem:
        // We want to choose at least k gateways so that the maximum chosen distance is minimized.
        //
        // The best strategy is always:
        // choose the k gateways with the smallest shortest-path distances.
        //
        // After sorting:
        // d0 <= d1 <= d2 <= ...
        // If we choose the first k gateways, the maximum among them is d[k - 1].
        // No other set of k gateways can have a smaller maximum, because any set of k gateways
        // must include some gateway whose distance is at least the k-th smallest distance.
        reachableGatewayDistances.Sort();

        // Step 8:
        // Return the k-th smallest reachable gateway distance (0-based index k - 1).
        //
        // This value is exactly the minimum possible cost.
        return reachableGatewayDistances[k - 1];
    }
}

// Demo code

var solution = new Solution();

// Example 1
int n1 = 6;
int[][] edges1 =
{
    new[] { 0, 1, 4 },
    new[] { 1, 2, 3 },
    new[] { 0, 3, 2 },
    new[] { 3, 4, 5 },
    new[] { 4, 5, 1 },
    new[] { 2, 5, 2 }
};
int[] gateways1 = { 2, 4, 5 };
int k1 = 2;

long result1 = solution.MinimumDelayToStreamKFeeds(n1, edges1, gateways1, k1);
Console.WriteLine(result1); // Expected: 7

// Example 2
int n2 = 5;
int[][] edges2 =
{
    new[] { 0, 1, 2 },
    new[] { 1, 2, 2 },
    new[] { 3, 4, 1 }
};
int[] gateways2 = { 2, 3, 4 };
int k2 = 2;

long result2 = solution.MinimumDelayToStreamKFeeds(n2, edges2, gateways2, k2);
Console.WriteLine(result2); // Expected: -1