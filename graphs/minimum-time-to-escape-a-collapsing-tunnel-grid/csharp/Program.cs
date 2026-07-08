/*
Title: Minimum Time to Escape a Collapsing Tunnel Grid

Problem Description:
You are given an undirected graph representing underground tunnel junctions. There are n junctions labeled from 0 to n - 1 and m bidirectional tunnels. Traveling through tunnel (u, v) always takes w minutes. However, each junction i also has a collapse time collapse[i]. You may stand at or arrive at junction i only at times strictly less than collapse[i]. If you reach a junction at time t where t >= collapse[i], that state is invalid because the junction has already collapsed.

You start at junction 0 at time 0 and want to reach junction n - 1 as early as possible. In addition to moving through tunnels, you may wait at any currently safe junction for any non-negative integer amount of time, as long as the junction has not collapsed before or during the waiting period. Waiting can be necessary because some tunnels are controlled by periodic gates.

Each tunnel is described by five values [u, v, w, open, close]. The tunnel can only be entered at integer times t such that t mod (open + close) < open. In other words, the tunnel is open for open minutes, then closed for close minutes, repeating forever starting from time 0. If you begin traversing the tunnel at a valid open time t, you spend exactly w minutes moving, and the tunnel does not need to remain open after departure. You still must arrive at the destination junction before it collapses.

Return the minimum time needed to reach junction n - 1, or -1 if it is impossible.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    private sealed class Edge
    {
        public int To;
        public long TravelTime;
        public long Open;
        public long Close;

        public Edge(int to, long travelTime, long open, long close)
        {
            To = to;
            TravelTime = travelTime;
            Open = open;
            Close = close;
        }
    }

    /*
    Time Complexity:
    - Building the graph takes O(m)
    - Dijkstra's algorithm processes each node/edge with a priority queue:
      O((n + m) log n) in practice, more precisely O(m log n)
    - Each edge relaxation is O(1)

    Space Complexity:
    - Graph storage: O(n + m)
    - Distance array: O(n)
    - Priority queue: O(n) to O(m) depending on pushes
    */
    public long MinimumEscapeTime(int n, int[][] edges, long[] collapse)
    {
        // If the starting node is already collapsed at time 0, we cannot even begin.
        // The problem states collapse[0] > 0, but this check makes the method robust.
        if (collapse[0] <= 0)
        {
            return -1;
        }

        // -----------------------------
        // Step 1: Build an adjacency list
        // -----------------------------
        // We use an adjacency list because:
        // - The graph can be very large (up to 100000 nodes and 200000 edges).
        // - Adjacency lists are memory-efficient for sparse graphs.
        // - Dijkstra's algorithm naturally iterates over outgoing edges of a node.
        var graph = new List<Edge>[n];
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<Edge>();
        }

        foreach (var e in edges)
        {
            int u = e[0];
            int v = e[1];
            long w = e[2];
            long open = e[3];
            long close = e[4];

            graph[u].Add(new Edge(v, w, open, close));
            graph[v].Add(new Edge(u, w, open, close));
        }

        // -----------------------------
        // Step 2: Prepare Dijkstra state
        // -----------------------------
        // dist[i] = earliest valid time at which we can arrive at node i.
        // We initialize all nodes as unreachable using a very large number.
        long inf = long.MaxValue / 4;
        var dist = new long[n];
        Array.Fill(dist, inf);

        // We start at node 0 at time 0, which is valid because collapse[0] > 0.
        dist[0] = 0;

        // Priority queue stores states ordered by earliest time first.
        // Each item is (node), and priority is the current best-known arrival time.
        var pq = new PriorityQueue<int, long>();
        pq.Enqueue(0, 0);

        // -----------------------------
        // Step 3: Standard Dijkstra loop
        // -----------------------------
        // This works because the edge transition function is FIFO:
        // leaving later can never make you arrive earlier on the same edge,
        // since the next valid departure time is monotonic with respect to current time.
        while (pq.Count > 0)
        {
            pq.TryDequeue(out int node, out long currentTime);

            // This is a stale queue entry.
            // Because we may push the same node multiple times, we only process
            // the entry if it matches the current best known distance.
            if (currentTime != dist[node])
            {
                continue;
            }

            // If we popped the destination, this is the earliest possible arrival time.
            // Dijkstra guarantees optimality here.
            if (node == n - 1)
            {
                return currentTime;
            }

            // If somehow this node is no longer valid at currentTime, skip it.
            // In a correct run this should not happen because we only store valid arrivals,
            // but keeping the check improves clarity and safety.
            if (currentTime >= collapse[node])
            {
                continue;
            }

            // Explore every tunnel leaving the current node.
            foreach (var edge in graph[node])
            {
                // ---------------------------------------------
                // Step 3a: Compute the earliest time we can ENTER this tunnel
                // ---------------------------------------------
                // We are currently at 'node' at time 'currentTime'.
                // We may wait at this node, but only while the node remains safe.
                //
                // The tunnel has a repeating cycle:
                // - open for 'edge.Open' minutes
                // - closed for 'edge.Close' minutes
                // Total cycle length = open + close
                //
                // A departure time t is valid if:
                // t % cycle < open
                //
                // We need the earliest departure time depart >= currentTime satisfying that rule.
                long depart = GetEarliestOpenTime(currentTime, edge.Open, edge.Close);

                // ---------------------------------------------
                // Step 3b: Verify that waiting until 'depart' is allowed
                // ---------------------------------------------
                // We may stand at the current node only at times strictly less than collapse[node].
                // Entering the tunnel at time 'depart' means we must still be safe at that exact time.
                // Therefore depart must be strictly less than collapse[node].
                if (depart >= collapse[node])
                {
                    continue;
                }

                // ---------------------------------------------
                // Step 3c: Compute arrival time at the neighbor
                // ---------------------------------------------
                long arrival = depart + edge.TravelTime;

                // We must arrive strictly before the destination node collapses.
                if (arrival >= collapse[edge.To])
                {
                    continue;
                }

                // ---------------------------------------------
                // Step 3d: Relax the edge
                // ---------------------------------------------
                // If this route gives a better (earlier) arrival time for the neighbor,
                // update it and push the new state into the priority queue.
                if (arrival < dist[edge.To])
                {
                    dist[edge.To] = arrival;
                    pq.Enqueue(edge.To, arrival);
                }
            }
        }

        // If Dijkstra finishes without reaching node n - 1, then it is impossible.
        return -1;
    }

    private static long GetEarliestOpenTime(long currentTime, long open, long close)
    {
        // This helper finds the earliest integer time t >= currentTime such that
        // the tunnel is open at time t.
        //
        // The tunnel repeats every cycle = open + close.
        // Within each cycle, times with remainder [0, open - 1] are open.
        long cycle = open + close;
        long remainder = currentTime % cycle;

        // If we are already inside the open segment of the cycle,
        // we can depart immediately.
        if (remainder < open)
        {
            return currentTime;
        }

        // Otherwise, we are in the closed segment.
        // We must wait until the next cycle begins.
        return currentTime + (cycle - remainder);
    }
}

// --------------------------------------------------
// Demo code
// --------------------------------------------------

// Example 1:
// The original statement text contains a contradiction and then corrects it.
// We use the corrected fourth edge [2,3,1,2,2], which makes the answer 5.
var solution = new Solution();

int n1 = 4;
int[][] edges1 =
{
    new[] { 0, 1, 3, 2, 2 },
    new[] { 1, 3, 2, 3, 1 },
    new[] { 0, 2, 2, 1, 3 },
    new[] { 2, 3, 1, 2, 2 }
};
long[] collapse1 = { 100, 10, 10, 20 };

long result1 = solution.MinimumEscapeTime(n1, edges1, collapse1);
Console.WriteLine(result1); // Expected: 5

// Example 2:
// The statement also corrects this example by changing collapse[1] from 6 to 5.
// We use the corrected version so the expected answer is -1.
int n2 = 3;
int[][] edges2 =
{
    new[] { 0, 1, 4, 1, 2 },
    new[] { 1, 2, 3, 1, 1 },
    new[] { 0, 2, 10, 5, 5 }
};
long[] collapse2 = { 20, 5, 9 };

long result2 = solution.MinimumEscapeTime(n2, edges2, collapse2);
Console.WriteLine(result2); // Expected: -1