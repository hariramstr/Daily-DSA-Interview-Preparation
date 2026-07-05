/*
Title: Cheapest Shared Shuttle Pickup

Problem Description:
A company campus runs one-way shuttle lanes between buildings. The campus wants to pick a single shuttle pickup building where two employees can meet before riding together to the main office. You are given an integer n representing buildings labeled from 0 to n - 1, a list of directed edges lanes where lanes[i] = [u, v, cost] means there is a one-way shuttle lane from building u to building v with travel cost cost, and three distinct buildings: aliceStart, bobStart, and office.

Alice starts at aliceStart and Bob starts at bobStart. They may travel independently through the directed graph and choose any building m as their meeting point. After both reach m, they continue together from m to office, paying that final segment cost only once because they share the shuttle from that point onward.

Return the minimum total travel cost needed for both employees to reach the office under this rule. If there is no valid meeting point from which both employees can eventually reach the office, return -1.

Formally, minimize:

dist(aliceStart, m) + dist(bobStart, m) + dist(m, office)

for any building m that is reachable from both starting buildings and can also reach office.

Key idea:
- Run Dijkstra from Alice's start on the original graph.
- Run Dijkstra from Bob's start on the original graph.
- Run Dijkstra from the office on the reversed graph.
  This gives, for every node m, the shortest distance from m to office in the original graph.
- Then try every node as the meeting point and take the minimum valid sum.

This works because all edge costs are positive, so Dijkstra's algorithm is correct.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the adjacency lists: O(E)
    - Running Dijkstra 3 times: O((V + E) log V) each
    - Final scan over all nodes: O(V)
    - Total: O((V + E) log V)

    Space Complexity:
    - Adjacency lists for original and reversed graph: O(V + E)
    - Distance arrays: O(V)
    - Priority queue storage: O(V + E) in the worst case across operations
    - Total: O(V + E)
    */
    public long MinimumSharedShuttleCost(int n, int[][] lanes, int aliceStart, int bobStart, int office)
    {
        // We build two graphs:
        //
        // 1) graph:
        //    The original directed graph.
        //    If there is an edge u -> v with cost w, we store (v, w) in graph[u].
        //
        // 2) reverseGraph:
        //    The reversed directed graph.
        //    If there is an edge u -> v with cost w in the original graph,
        //    then we store (u, w) in reverseGraph[v].
        //
        // Why do we need the reversed graph?
        // We need dist(m, office) for every possible meeting point m.
        // Instead of running Dijkstra from every m (which would be far too slow),
        // we run ONE Dijkstra from office on the reversed graph.
        // That gives us the shortest distance from every node m to office in the original graph.
        var graph = new List<(int to, long cost)>[n];
        var reverseGraph = new List<(int to, long cost)>[n];

        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<(int to, long cost)>();
            reverseGraph[i] = new List<(int to, long cost)>();
        }

        // Fill both adjacency lists.
        foreach (var lane in lanes)
        {
            int u = lane[0];
            int v = lane[1];
            long cost = lane[2];

            graph[u].Add((v, cost));
            reverseGraph[v].Add((u, cost));
        }

        // Compute shortest distances from Alice's start to every node.
        // distFromAlice[m] = shortest cost for Alice to reach meeting point m.
        long[] distFromAlice = Dijkstra(n, graph, aliceStart);

        // Compute shortest distances from Bob's start to every node.
        // distFromBob[m] = shortest cost for Bob to reach meeting point m.
        long[] distFromBob = Dijkstra(n, graph, bobStart);

        // Compute shortest distances from every node to office.
        // We do this by running Dijkstra from office on the reversed graph.
        // distToOffice[m] = shortest cost from m to office in the original graph.
        long[] distToOffice = Dijkstra(n, reverseGraph, office);

        // We will now test every building as a possible meeting point.
        //
        // A building m is valid only if:
        // - Alice can reach m
        // - Bob can reach m
        // - m can reach office
        //
        // In terms of our distance arrays, that means none of the three distances
        // should be "infinity" (our unreachable marker).
        long answer = long.MaxValue;

        for (int m = 0; m < n; m++)
        {
            // If any required path does not exist, this meeting point cannot work.
            if (distFromAlice[m] == long.MaxValue ||
                distFromBob[m] == long.MaxValue ||
                distToOffice[m] == long.MaxValue)
            {
                continue;
            }

            // Total cost if both meet at m:
            // Alice travels alone to m
            // Bob travels alone to m
            // Then they share the ride from m to office, paying that segment once
            long totalCost = distFromAlice[m] + distFromBob[m] + distToOffice[m];

            if (totalCost < answer)
            {
                answer = totalCost;
            }
        }

        // If answer was never updated, no valid meeting point exists.
        return answer == long.MaxValue ? -1 : answer;
    }

    private long[] Dijkstra(int n, List<(int to, long cost)>[] graph, int start)
    {
        // We use long because:
        // - Each edge cost can be as large as 1,000,000,000
        // - A shortest path may include many edges
        // - int could overflow, so long is the safe choice
        long[] dist = new long[n];
        Array.Fill(dist, long.MaxValue);

        // PriorityQueue<TElement, TPriority> in .NET stores:
        // - element: the node
        // - priority: the current known shortest distance
        //
        // Dijkstra always expands the node with the smallest current distance first.
        var pq = new PriorityQueue<int, long>();

        // Distance from start to itself is 0.
        dist[start] = 0;
        pq.Enqueue(start, 0);

        // Standard Dijkstra loop.
        while (pq.Count > 0)
        {
            // Extract the node with the smallest known distance.
            pq.TryDequeue(out int currentNode, out long currentDistance);

            // Important optimization:
            // Because we may insert the same node multiple times into the priority queue
            // (whenever we find a better path), some queue entries become outdated.
            //
            // If the popped distance is not equal to the best known distance anymore,
            // we skip it.
            if (currentDistance != dist[currentNode])
            {
                continue;
            }

            // Explore all outgoing edges from currentNode.
            foreach (var (nextNode, edgeCost) in graph[currentNode])
            {
                // If we go from start -> ... -> currentNode -> nextNode,
                // this is the candidate distance to nextNode.
                long newDistance = currentDistance + edgeCost;

                // If this path is better than what we knew before, update it.
                if (newDistance < dist[nextNode])
                {
                    dist[nextNode] = newDistance;
                    pq.Enqueue(nextNode, newDistance);
                }
            }
        }

        return dist;
    }
}

// Demo code:
// We create the sample inputs from the problem statement,
// call the solution, and print the results.

// Example 1
int n1 = 6;
int[][] lanes1 =
{
    new[] { 0, 2, 2 },
    new[] { 1, 2, 4 },
    new[] { 2, 3, 3 },
    new[] { 2, 4, 1 },
    new[] { 4, 3, 1 },
    new[] { 3, 5, 2 },
    new[] { 4, 5, 5 }
};
int aliceStart1 = 0;
int bobStart1 = 1;
int office1 = 5;

var solution = new Solution();
long result1 = solution.MinimumSharedShuttleCost(n1, lanes1, aliceStart1, bobStart1, office1);
Console.WriteLine(result1); // Expected: 11

// Example 2
int n2 = 5;
int[][] lanes2 =
{
    new[] { 0, 1, 3 },
    new[] { 1, 4, 4 },
    new[] { 2, 3, 2 }
};
int aliceStart2 = 0;
int bobStart2 = 2;
int office2 = 4;

long result2 = solution.MinimumSharedShuttleCost(n2, lanes2, aliceStart2, bobStart2, office2);
Console.WriteLine(result2); // Expected: -1