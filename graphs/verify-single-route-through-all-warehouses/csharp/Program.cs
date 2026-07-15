/*
Title: Verify Single Route Through All Warehouses
Difficulty: Easy
Topic: Graphs

Problem Description:
A logistics company stores package transfers as an undirected graph. There are n warehouses labeled from 0 to n - 1, and each road in roads[i] = [a, b] means warehouse a is directly connected to warehouse b.

The company wants to know whether its road map forms exactly one simple connected route through all warehouses. In other words, starting from one end, you should be able to visit every warehouse exactly once by following the roads, without encountering any branch and without leaving any warehouse disconnected.

Return true if the graph forms a single path that uses all n warehouses. Otherwise, return false.

A graph is considered a valid single route if:
1. Every warehouse belongs to the same connected component.
2. Exactly two warehouses have degree 1 when n > 1 (the two ends of the route).
3. Every other warehouse has degree 2.
4. If n == 1, the graph with no roads is also valid.

Constraints:
- 1 <= n <= 1000
- 0 <= roads.length <= 1000
- roads[i].length == 2
- 0 <= a, b < n
- a != b
- There are no duplicate roads.

Example 1:
Input: n = 4, roads = [[0,1],[1,2],[2,3]]
Output: true

Example 2:
Input: n = 4, roads = [[0,1],[1,2],[1,3]]
Output: false
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n + m)
    - n is the number of warehouses (nodes)
    - m is the number of roads (edges)
    We build the graph in O(m), inspect degrees in O(n), and perform one graph traversal in O(n + m).

    Space Complexity: O(n + m)
    - Adjacency list stores all nodes and edges
    - Degree array, visited array, and traversal stack/queue also use extra space
    */
    public bool IsSingleRoute(int n, int[][] roads)
    {
        // Special case:
        // If there is only one warehouse, then the only valid "single route"
        // is a graph with no roads at all.
        //
        // Why this is necessary:
        // The general rule "exactly two nodes of degree 1" only makes sense when
        // there are at least two nodes. For n == 1, the single node stands alone
        // and that is considered a valid path of length 0.
        if (n == 1)
        {
            return roads.Length == 0;
        }

        // A path graph with n nodes must have exactly n - 1 edges.
        //
        // Why this is necessary:
        // - If there are fewer than n - 1 edges, the graph cannot be fully connected.
        // - If there are more than n - 1 edges, then some cycle or extra branching
        //   must exist, so it cannot be a simple path.
        //
        // This is a very strong early check that quickly rejects invalid inputs.
        if (roads.Length != n - 1)
        {
            return false;
        }

        // Create an adjacency list to represent the undirected graph.
        //
        // Why adjacency list?
        // - It is a standard and efficient structure for sparse graphs.
        // - It lets us easily:
        //   1. Count neighbors of each node
        //   2. Traverse the graph using DFS or BFS
        //
        // We also maintain a degree array where degree[i] tells us how many roads
        // are connected to warehouse i.
        var graph = new List<int>[n];
        var degree = new int[n];

        // Initialize each adjacency list so every warehouse has a place to store neighbors.
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<int>();
        }

        // Build the undirected graph.
        //
        // For each road [a, b]:
        // - Add b to a's neighbor list
        // - Add a to b's neighbor list
        // - Increase both degrees
        //
        // Why both directions?
        // Because the graph is undirected: if a is connected to b,
        // then b is also connected to a.
        foreach (var road in roads)
        {
            int a = road[0];
            int b = road[1];

            graph[a].Add(b);
            graph[b].Add(a);

            degree[a]++;
            degree[b]++;
        }

        // Count how many warehouses have degree 1 and verify all others have degree 2.
        //
        // For a valid path with n > 1:
        // - Exactly two nodes must be endpoints, so degree 1
        // - Every internal node must connect to exactly two neighbors, so degree 2
        //
        // If any node has degree 0, it is disconnected.
        // If any node has degree 3 or more, there is branching.
        int degreeOneCount = 0;

        for (int i = 0; i < n; i++)
        {
            if (degree[i] == 1)
            {
                degreeOneCount++;
            }
            else if (degree[i] == 2)
            {
                // This is exactly what we want for an internal node of the path.
                // No action needed.
            }
            else
            {
                // Any other degree makes the graph invalid as a single simple path.
                return false;
            }
        }

        // There must be exactly two endpoints.
        //
        // Why this is necessary:
        // A simple path with more than one node starts at one endpoint and ends at another.
        // Therefore, exactly two nodes must have degree 1.
        if (degreeOneCount != 2)
        {
            return false;
        }

        // Even though the edge count and degree pattern are already very strong indicators,
        // we still explicitly verify connectivity.
        //
        // Why this is necessary:
        // The problem definition requires that every warehouse belongs to the same
        // connected component.
        //
        // We will perform an iterative DFS starting from one endpoint.
        // Starting from an endpoint is natural for a path graph.
        int start = -1;
        for (int i = 0; i < n; i++)
        {
            if (degree[i] == 1)
            {
                start = i;
                break;
            }
        }

        var visited = new bool[n];
        var stack = new Stack<int>();
        stack.Push(start);
        int visitedCount = 0;

        // Standard iterative DFS:
        // Repeatedly pop a node, skip if already visited, otherwise mark it visited
        // and push all unvisited neighbors.
        //
        // Why iterative instead of recursive?
        // - Both are fine here.
        // - Iterative DFS avoids recursion depth concerns and is beginner-friendly
        //   once you understand how a stack works.
        while (stack.Count > 0)
        {
            int current = stack.Pop();

            // If we have already processed this warehouse, skip it.
            if (visited[current])
            {
                continue;
            }

            // Mark this warehouse as visited and count it.
            visited[current] = true;
            visitedCount++;

            // Explore all directly connected neighboring warehouses.
            foreach (int neighbor in graph[current])
            {
                if (!visited[neighbor])
                {
                    stack.Push(neighbor);
                }
            }
        }

        // The graph is connected if and only if we visited all n warehouses.
        //
        // If some warehouse was not reached, then the graph has more than one
        // connected component, so it cannot be a single route through all warehouses.
        return visitedCount == n;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print results.

var solution = new Solution();

// Example 1:
// n = 4, roads = [[0,1],[1,2],[2,3]]
// This is a straight chain: 0 - 1 - 2 - 3
// Expected: true
int n1 = 4;
int[][] roads1 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 2, 3 }
};
bool result1 = solution.IsSingleRoute(n1, roads1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// n = 4, roads = [[0,1],[1,2],[1,3]]
// Node 1 has degree 3, which creates a branch.
// Expected: false
int n2 = 4;
int[][] roads2 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 1, 3 }
};
bool result2 = solution.IsSingleRoute(n2, roads2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo 1:
// Single warehouse with no roads.
// Expected: true
int n3 = 1;
int[][] roads3 = Array.Empty<int[]>();
bool result3 = solution.IsSingleRoute(n3, roads3);
Console.WriteLine($"Single Warehouse Result: {result3}");

// Additional demo 2:
// A cycle is not a path because all nodes would have degree 2 and there would be no endpoints.
// Expected: false
int n4 = 4;
int[][] roads4 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 2, 3 },
    new[] { 3, 0 }
};
bool result4 = solution.IsSingleRoute(n4, roads4);
Console.WriteLine($"Cycle Graph Result: {result4}");