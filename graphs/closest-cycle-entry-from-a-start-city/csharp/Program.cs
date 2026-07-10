/*
Title: Closest Cycle Entry from a Start City
Difficulty: Medium
Topic: Graphs

Problem Description:
You are given a directed graph representing one-way roads between cities. The graph has n cities labeled from 0 to n - 1 and m directed roads, where each road [u, v] means you can travel from city u to city v. You are also given a starting city s.

A city is called a cycle-entry city if it belongs to at least one directed cycle that is reachable from s. Your task is to return the minimum number of roads needed to travel from s to any such cycle-entry city. If multiple cycle-entry cities are reachable at the same minimum distance, return the smallest city index among them. If no directed cycle is reachable from s, return [-1, -1].

Return the answer as an array [distance, city]. The distance is the length of the shortest directed path from s to the chosen city.

A city belongs to a directed cycle if there exists a path that starts at that city, follows one or more directed edges, and returns to the same city.

Constraints:
- 1 <= n <= 100000
- 0 <= m <= 200000
- 0 <= s < n
- 0 <= u, v < n
- There may be self-loops and multiple edges.

Example 1:
Input: n = 7, edges = [[0,1],[1,2],[2,3],[3,1],[2,4],[4,5]], s = 0
Output: [1,1]
Explanation: From city 0, the reachable cycle is 1 -> 2 -> 3 -> 1. Cities 1, 2, and 3 are all on a reachable directed cycle. Their distances from 0 are 1, 2, and 3 respectively, so the answer is [1, 1].

Example 2:
Input: n = 6, edges = [[0,1],[1,2],[2,4],[4,5],[3,3]], s = 0
Output: [-1,-1]
Explanation: City 3 has a self-loop, so it forms a cycle, but it is not reachable from city 0. No reachable city from 0 belongs to any directed cycle.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the adjacency list: O(m)
    - Reachability BFS from s: O(n + m)
    - Tarjan's strongly connected components algorithm: O(n + m)
    - Final scan to choose the best reachable cycle node: O(n)
    Overall: O(n + m)

    Space Complexity:
    - Adjacency list: O(n + m)
    - BFS / Tarjan helper arrays and stacks: O(n)
    Overall: O(n + m)
    */
    public int[] ClosestCycleEntry(int n, int[][] edges, int s)
    {
        // Step 1:
        // Build the directed graph as an adjacency list.
        //
        // Why adjacency list?
        // - The graph can be large: up to 100000 nodes and 200000 edges.
        // - An adjacency matrix would be far too large in memory.
        // - An adjacency list lets us efficiently iterate over outgoing edges.
        //
        // graph[u] will contain every city v such that there is a directed road u -> v.
        var graph = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<int>();
        }

        // We also track self-loops explicitly.
        // This is useful because a single-node strongly connected component is only a cycle
        // if that node has an edge to itself.
        var hasSelfLoop = new bool[n];

        foreach (var edge in edges)
        {
            int u = edge[0];
            int v = edge[1];
            graph[u].Add(v);

            if (u == v)
            {
                hasSelfLoop[u] = true;
            }
        }

        // Step 2:
        // Run BFS from the starting city s to determine:
        // 1) Which cities are reachable from s
        // 2) The shortest distance from s to every reachable city
        //
        // Why BFS?
        // - Every road has equal cost (1 step).
        // - BFS guarantees shortest path lengths in an unweighted graph.
        //
        // reachable[i] = true if city i can be reached from s.
        // dist[i] = shortest number of edges from s to i, or -1 if unreachable.
        var reachable = new bool[n];
        var dist = new int[n];
        Array.Fill(dist, -1);

        var queue = new Queue<int>();
        queue.Enqueue(s);
        reachable[s] = true;
        dist[s] = 0;

        while (queue.Count > 0)
        {
            int u = queue.Dequeue();

            // Explore every outgoing road from u.
            foreach (int v in graph[u])
            {
                // If we have not visited v yet, then this is the first time BFS reaches it,
                // which means we have found the shortest path to v.
                if (!reachable[v])
                {
                    reachable[v] = true;
                    dist[v] = dist[u] + 1;
                    queue.Enqueue(v);
                }
            }
        }

        // Step 3:
        // Identify which cities belong to at least one directed cycle.
        //
        // The standard and efficient way to do this in a directed graph is to compute
        // strongly connected components (SCCs).
        //
        // Key fact:
        // - If an SCC has size >= 2, then every node in that SCC lies on a directed cycle.
        // - If an SCC has size == 1, then that single node lies on a directed cycle only if
        //   it has a self-loop.
        //
        // We will use Tarjan's algorithm, which finds all SCCs in O(n + m).
        //
        // Arrays used by Tarjan:
        // - ids[u] = discovery order number of node u, or -1 if unvisited
        // - low[u] = smallest discovery id reachable from u while staying within the current DFS context
        // - onStack[u] = whether u is currently on the Tarjan stack
        //
        // cycleNode[u] will become true if city u belongs to some directed cycle.
        var ids = new int[n];
        Array.Fill(ids, -1);

        var low = new int[n];
        var onStack = new bool[n];
        var cycleNode = new bool[n];
        var stack = new Stack<int>();
        int id = 0;

        // Local DFS function implementing Tarjan's SCC algorithm.
        void Dfs(int at)
        {
            // Assign a unique discovery id to this node.
            ids[at] = id;
            low[at] = id;
            id++;

            // Push onto the stack because this node is part of the current DFS path / SCC search.
            stack.Push(at);
            onStack[at] = true;

            // Explore all outgoing edges from 'at'.
            foreach (int to in graph[at])
            {
                // Case 1: 'to' has not been visited yet.
                // We must DFS into it first, then use its low-link value to update ours.
                if (ids[to] == -1)
                {
                    Dfs(to);
                    low[at] = Math.Min(low[at], low[to]);
                }
                // Case 2: 'to' is still on the stack.
                // That means 'to' is in the current active SCC search, so this edge can
                // contribute to a cycle / back-link within the current SCC.
                else if (onStack[to])
                {
                    low[at] = Math.Min(low[at], ids[to]);
                }
            }

            // If ids[at] == low[at], then 'at' is the root of an SCC.
            // We now pop nodes until we get back to 'at'. Those popped nodes form one SCC.
            if (ids[at] == low[at])
            {
                var component = new List<int>();

                while (true)
                {
                    int node = stack.Pop();
                    onStack[node] = false;
                    component.Add(node);

                    if (node == at)
                    {
                        break;
                    }
                }

                // Determine whether this SCC represents a directed cycle.
                //
                // - If component size > 1, then yes: every node in it is on a cycle.
                // - If size == 1, it is only a cycle if the single node has a self-loop.
                bool isCycleComponent = false;

                if (component.Count > 1)
                {
                    isCycleComponent = true;
                }
                else
                {
                    int only = component[0];
                    if (hasSelfLoop[only])
                    {
                        isCycleComponent = true;
                    }
                }

                // Mark all nodes in this SCC as cycle nodes if this SCC is a cycle component.
                if (isCycleComponent)
                {
                    foreach (int node in component)
                    {
                        cycleNode[node] = true;
                    }
                }
            }
        }

        // Run Tarjan DFS from every unvisited node.
        //
        // Why every node, not only reachable ones?
        // - Simplicity and still linear time.
        // - Later we will only consider nodes that are both reachable and in a cycle.
        for (int i = 0; i < n; i++)
        {
            if (ids[i] == -1)
            {
                Dfs(i);
            }
        }

        // Step 4:
        // Among all cities that:
        // - are reachable from s
        // - belong to at least one directed cycle
        //
        // choose the one with:
        // 1) minimum distance from s
        // 2) if distances tie, smallest city index
        //
        // This directly matches the problem statement.
        int bestDistance = int.MaxValue;
        int bestCity = int.MaxValue;

        for (int city = 0; city < n; city++)
        {
            if (reachable[city] && cycleNode[city])
            {
                if (dist[city] < bestDistance)
                {
                    bestDistance = dist[city];
                    bestCity = city;
                }
                else if (dist[city] == bestDistance && city < bestCity)
                {
                    bestCity = city;
                }
            }
        }

        // Step 5:
        // If we never found a reachable cycle node, return [-1, -1].
        // Otherwise return [bestDistance, bestCity].
        if (bestDistance == int.MaxValue)
        {
            return new[] { -1, -1 };
        }

        return new[] { bestDistance, bestCity };
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// n = 7
// edges = [[0,1],[1,2],[2,3],[3,1],[2,4],[4,5]]
// s = 0
//
// Reachable cycle is 1 -> 2 -> 3 -> 1
// Distances from 0:
// 1 = 1, 2 = 2, 3 = 3
// Expected answer: [1, 1]
int n1 = 7;
int[][] edges1 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 2, 3 },
    new[] { 3, 1 },
    new[] { 2, 4 },
    new[] { 4, 5 }
};
int s1 = 0;

var result1 = solution.ClosestCycleEntry(n1, edges1, s1);
Console.WriteLine($"Example 1: [{result1[0]}, {result1[1]}]");

// Example 2:
// n = 6
// edges = [[0,1],[1,2],[2,4],[4,5],[3,3]]
// s = 0
//
// City 3 has a self-loop, but it is not reachable from 0.
// Therefore no reachable cycle exists.
// Expected answer: [-1, -1]
int n2 = 6;
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 2, 4 },
    new[] { 4, 5 },
    new[] { 3, 3 }
};
int s2 = 0;

var result2 = solution.ClosestCycleEntry(n2, edges2, s2);
Console.WriteLine($"Example 2: [{result2[0]}, {result2[1]}]");

// Additional demo:
// Self-loop reachable directly from start.
// Expected answer: [1, 1]
int n3 = 3;
int[][] edges3 =
{
    new[] { 0, 1 },
    new[] { 1, 1 },
    new[] { 1, 2 }
};
int s3 = 0;

var result3 = solution.ClosestCycleEntry(n3, edges3, s3);
Console.WriteLine($"Example 3: [{result3[0]}, {result3[1]}]");