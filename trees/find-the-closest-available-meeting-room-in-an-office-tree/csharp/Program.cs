/*
Title: Find the Closest Available Meeting Room in an Office Tree

Problem Description:
You are given the layout of an office campus as a rooted tree with nodes numbered from 0 to n - 1.
Each node represents either a hallway junction or a room, and edges represent direct walking connections.
Node 0 is the main entrance. Every node has a boolean value available[i] indicating whether that
location is currently an available meeting room.

A query asks: starting from employee position u, which available meeting room can be reached with the
fewest number of edges?

If multiple available meeting rooms are tied for the same minimum distance, return the one with the
smallest node index. If no available meeting room exists anywhere in the tree, return -1 for that query.

You must answer many independent queries on the same tree efficiently.

Implement a function that receives:
- n: number of nodes
- edges: n - 1 undirected edges of the tree
- available: array of length n
- queries: array of starting nodes

Return an array where the ith value is the answer for queries[i].

Constraints:
- 1 <= n <= 2 * 10^5
- edges.length == n - 1
- 0 <= u, v < n
- available.length == n
- 1 <= queries.length <= 2 * 10^5
- The input graph is guaranteed to be a tree

Intended idea:
Precompute the nearest available room for every node in linear time using tree DP / rerooting,
then answer each query in O(1).
*/

using System;
using System.Collections.Generic;

class Solution
{
    // We use a large number as "infinity" for distances.
    // It must be safely larger than any possible tree distance (which is at most n - 1).
    private const int INF = 1_000_000_000;

    // This small helper structure stores:
    // - Dist: the distance to the nearest available room
    // - Node: which available room gives that distance
    //
    // Why store both?
    // Because the problem has a tie-break rule:
    // if two available rooms are at the same minimum distance,
    // we must return the smaller node index.
    //
    // So every DP state must remember not only the best distance,
    // but also the exact room index that achieves that distance.
    private struct Best
    {
        public int Dist;
        public int Node;

        public Best(int dist, int node)
        {
            Dist = dist;
            Node = node;
        }
    }

    // Time Complexity:
    // O(n + q)
    // - O(n) to build the tree
    // - O(n) for the first DFS-style pass (postorder DP)
    // - O(n) for the second DFS-style pass (rerooting / propagate parent-side answers)
    // - O(q) to answer all queries after preprocessing
    //
    // Space Complexity:
    // O(n)
    // - adjacency list
    // - parent array
    // - traversal order
    // - DP arrays
    public int[] ClosestAvailableMeetingRoom(int n, int[][] edges, bool[] available, int[] queries)
    {
        // ------------------------------------------------------------
        // Step 1: Build the adjacency list for the tree.
        // ------------------------------------------------------------
        //
        // A tree is an undirected graph with n nodes and n - 1 edges.
        // We store it as adjacency lists because:
        // - it is memory efficient for sparse graphs like trees
        // - it allows easy traversal of neighbors
        //
        // graph[u] will contain all nodes directly connected to u.
        var graph = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<int>();
        }

        foreach (var e in edges)
        {
            int u = e[0];
            int v = e[1];
            graph[u].Add(v);
            graph[v].Add(u);
        }

        // ------------------------------------------------------------
        // Step 2: Root the tree at node 0 and build:
        // - parent[node]
        // - traversal order
        // ------------------------------------------------------------
        //
        // Even though the original tree is undirected, rerooting DP becomes much easier
        // if we choose a root and think in terms of parent/children.
        //
        // We use an iterative DFS to avoid recursion depth issues.
        // With n up to 200,000, recursive DFS could overflow the call stack in C#.
        //
        // order[] will store nodes in the order they are visited.
        // Later, processing order in reverse gives us a postorder-like traversal,
        // which is exactly what we need for bottom-up DP.
        int[] parent = new int[n];
        Array.Fill(parent, -2); // -2 means "unvisited"
        parent[0] = -1;         // root has no parent

        int[] order = new int[n];
        int orderCount = 0;

        var stack = new Stack<int>();
        stack.Push(0);

        while (stack.Count > 0)
        {
            int u = stack.Pop();
            order[orderCount++] = u;

            foreach (int v in graph[u])
            {
                if (parent[v] != -2) continue; // already visited
                parent[v] = u;
                stack.Push(v);
            }
        }

        // ------------------------------------------------------------
        // Step 3: Bottom-up DP.
        // Compute down[u]:
        // the best available room reachable from u using only u's subtree.
        // ------------------------------------------------------------
        //
        // Meaning of "subtree" here:
        // Since the tree is rooted at 0, the subtree of u contains u and all descendants of u.
        //
        // Why do we need this?
        // Because in rerooting DP, we usually first compute answers coming from below (children),
        // then in a second pass we add answers coming from above (parent side).
        //
        // Base idea:
        // - If u itself is available, then one candidate is (distance 0, room u).
        // - If a child v has a best room in its subtree at distance d,
        //   then from u that same room is at distance d + 1.
        // - We take the best among all such candidates.
        Best[] down = new Best[n];
        for (int i = 0; i < n; i++)
        {
            down[i] = new Best(INF, -1);
        }

        // Process nodes in reverse order so children are handled before parents.
        for (int idx = n - 1; idx >= 0; idx--)
        {
            int u = order[idx];

            // Candidate 1: u itself, if it is an available room.
            if (available[u])
            {
                down[u] = Better(down[u], new Best(0, u));
            }

            // Candidate 2: any best room coming from a child subtree.
            foreach (int v in graph[u])
            {
                if (parent[v] != u) continue; // only true children

                if (down[v].Node != -1)
                {
                    // If child v can reach some available room at distance down[v].Dist,
                    // then from u that room is one edge farther away.
                    var candidate = new Best(down[v].Dist + 1, down[v].Node);
                    down[u] = Better(down[u], candidate);
                }
            }
        }

        // ------------------------------------------------------------
        // Step 4: Top-down rerooting pass.
        // Compute up[u]:
        // the best available room reachable from u that comes from outside u's subtree.
        // ------------------------------------------------------------
        //
        // Why is this needed?
        // down[u] only knows about rooms below u.
        // But the nearest room to u might be:
        // - in its own subtree, OR
        // - somewhere above u, OR
        // - in a sibling subtree
        //
        // The second pass propagates "outside-subtree" information from parent to children.
        //
        // For each child v of u, the best answer from outside v's subtree can come from:
        // 1) u itself, if u is available
        // 2) up[u], meaning something already known from above u
        // 3) another child subtree of u (a sibling of v)
        //
        // To do this efficiently in O(total degree) overall, for each node u we precompute:
        // - prefix bests over child contributions
        // - suffix bests over child contributions
        //
        // Then for a specific child v, we can combine:
        // - prefix before v
        // - suffix after v
        // to get the best sibling contribution in O(1).
        Best[] up = new Best[n];
        for (int i = 0; i < n; i++)
        {
            up[i] = new Best(INF, -1);
        }

        // Root has no parent side, so up[0] stays "no answer".
        // Process in forward order so parent is handled before children.
        for (int idx = 0; idx < n; idx++)
        {
            int u = order[idx];

            // Collect the children of u into a compact list.
            // This makes prefix/suffix handling easier.
            var children = new List<int>();
            foreach (int v in graph[u])
            {
                if (parent[v] == u)
                {
                    children.Add(v);
                }
            }

            int m = children.Count;

            // childBest[i] = contribution from child children[i] to node u
            //
            // Specifically:
            // if child c has down[c] = (d, room),
            // then from u that room is at distance d + 1.
            Best[] childBest = new Best[m];
            for (int i = 0; i < m; i++)
            {
                int c = children[i];
                if (down[c].Node == -1)
                {
                    childBest[i] = new Best(INF, -1);
                }
                else
                {
                    childBest[i] = new Best(down[c].Dist + 1, down[c].Node);
                }
            }

            // prefix[i] = best among childBest[0..i-1]
            // suffix[i] = best among childBest[i..m-1]
            //
            // We use arrays of size m + 1 to make boundaries easy:
            // prefix[0] = empty
            // prefix[1] = childBest[0]
            // ...
            //
            // suffix[m] = empty
            // suffix[m-1] = childBest[m-1]
            // ...
            Best[] prefix = new Best[m + 1];
            Best[] suffix = new Best[m + 1];

            prefix[0] = new Best(INF, -1);
            for (int i = 0; i < m; i++)
            {
                prefix[i + 1] = Better(prefix[i], childBest[i]);
            }

            suffix[m] = new Best(INF, -1);
            for (int i = m - 1; i >= 0; i--)
            {
                suffix[i] = Better(childBest[i], suffix[i + 1]);
            }

            // Now compute up[child] for each child.
            for (int i = 0; i < m; i++)
            {
                int v = children[i];

                // We want the best room visible from u that is NOT inside v's subtree.
                Best bestFromUExcludingV = new Best(INF, -1);

                // Option A: u itself is available.
                if (available[u])
                {
                    bestFromUExcludingV = Better(bestFromUExcludingV, new Best(0, u));
                }

                // Option B: something from above u.
                if (up[u].Node != -1)
                {
                    bestFromUExcludingV = Better(bestFromUExcludingV, up[u]);
                }

                // Option C: something from a sibling subtree of v.
                // prefix[i] covers children before i
                // suffix[i + 1] covers children after i
                bestFromUExcludingV = Better(bestFromUExcludingV, prefix[i]);
                bestFromUExcludingV = Better(bestFromUExcludingV, suffix[i + 1]);

                // If u can see some best room outside v's subtree at distance d,
                // then from v that same room is one edge farther away.
                if (bestFromUExcludingV.Node != -1)
                {
                    up[v] = new Best(bestFromUExcludingV.Dist + 1, bestFromUExcludingV.Node);
                }
                else
                {
                    up[v] = new Best(INF, -1);
                }
            }
        }

        // ------------------------------------------------------------
        // Step 5: Combine down[u] and up[u] to get the final answer for every node.
        // ------------------------------------------------------------
        //
        // down[u] = best room in u's subtree
        // up[u]   = best room outside u's subtree
        //
        // The true answer for u is simply the better of these two.
        int[] answerForNode = new int[n];
        for (int u = 0; u < n; u++)
        {
            Best best = Better(down[u], up[u]);
            answerForNode[u] = best.Node;
        }

        // ------------------------------------------------------------
        // Step 6: Answer queries in O(1) each.
        // ------------------------------------------------------------
        //
        // Since we already computed the nearest available room for every node,
        // each query is just a direct array lookup.
        int[] result = new int[queries.Length];
        for (int i = 0; i < queries.Length; i++)
        {
            result[i] = answerForNode[queries[i]];
        }

        return result;
    }

    // Compare two candidates and return the better one.
    //
    // Better means:
    // 1) smaller distance
    // 2) if distances tie, smaller node index
    //
    // This exactly matches the problem statement.
    private Best Better(Best a, Best b)
    {
        if (a.Node == -1) return b;
        if (b.Node == -1) return a;

        if (a.Dist != b.Dist)
        {
            return a.Dist < b.Dist ? a : b;
        }

        return a.Node < b.Node ? a : b;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

// Example 1
int n1 = 7;
int[][] edges1 =
{
    new[] { 0, 1 },
    new[] { 0, 2 },
    new[] { 1, 3 },
    new[] { 1, 4 },
    new[] { 2, 5 },
    new[] { 2, 6 }
};
bool[] available1 = { false, false, true, false, true, false, false };
int[] queries1 = { 3, 5, 0 };

var solution = new Solution();
int[] result1 = solution.ClosestAvailableMeetingRoom(n1, edges1, available1, queries1);
Console.WriteLine("Example 1 Output: [" + string.Join(",", result1) + "]");

// Expected: [4,2,2]
// Quick verification:
// - From 3: nearest available is 4 at distance 2, and 2 is distance 3 => answer 4
// - From 5: nearest available is 2 at distance 1 => answer 2
// - From 0: nearest available is 2 at distance 1, while 4 is distance 2 => answer 2

// Example 2
int n2 = 5;
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 1, 3 },
    new[] { 3, 4 }
};
bool[] available2 = { false, false, false, false, false };
int[] queries2 = { 0, 2, 4 };

int[] result2 = solution.ClosestAvailableMeetingRoom(n2, edges2, available2, queries2);
Console.WriteLine("Example 2 Output: [" + string.Join(",", result2) + "]");

// Expected: [-1,-1,-1]