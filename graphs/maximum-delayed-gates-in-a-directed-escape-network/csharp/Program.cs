/*
Title: Maximum Delayed Gates in a Directed Escape Network

Problem Description:
You are given a directed graph with n junctions numbered from 0 to n - 1 and m one-way corridors.
Junction 0 is the control center, and junction n - 1 is the only exit. Every corridor [u, v]
means a traveler standing at u may move to v.

Before an evacuation starts, you may permanently lock some junctions (except 0 and n - 1).
A locked junction cannot be entered or used. However, the network must remain valid after locking:
for every junction that is still unlocked, if it is reachable from junction 0 in the remaining graph,
then it must also be able to reach junction n - 1 in the remaining graph. In other words, after your
locks are applied, there cannot exist any "dead-end reachable region" from the control center.

Your task is to return the maximum number of junctions you can lock while preserving this property.

This is not simply asking whether 0 can still reach n - 1. Some reachable nodes may become invalid
because they can no longer reach the exit, and such a configuration is not allowed unless those nodes
are also locked or become unreachable from 0.

Constraints:
- 2 <= n <= 200000
- 1 <= m <= 300000
- 0 <= u, v < n
- Self-loops and parallel edges may appear
- You may not lock junction 0 or junction n - 1

Key idea:
We want to keep as few vertices unlocked as possible, while still ensuring:
1) 0 remains unlocked
2) n-1 remains unlocked
3) Every unlocked vertex reachable from 0 can still reach n-1

A very important observation is:
If a vertex is unlocked and reachable from 0, then it belongs to the "live" part of the graph.
Inside the remaining unlocked graph, the set of reachable live vertices must be closed under
"being needed to continue toward the exit".

This becomes much cleaner after compressing strongly connected components (SCCs):
- Inside one SCC, every vertex can reach every other vertex.
- After SCC compression, we get a DAG.
- In that DAG, we need to keep a smallest possible set of SCCs containing the SCC of 0 and the SCC of n-1,
  such that every kept SCC reachable from the start SCC can still reach the exit SCC using only kept SCCs.

In a DAG, the minimum such kept set is exactly the set of vertices that lie on at least one path
from start SCC to exit SCC:
- reachable from start SCC
- and can reach exit SCC

Why is that minimal?
Because if a kept SCC is reachable from start, validity requires it to reach exit within kept SCCs,
so it must lie on some start-to-exit route. Conversely, keeping exactly all SCCs on some start-to-exit
route is valid: every kept reachable SCC can continue to exit.

Therefore:
maximum locked vertices = n - (# original vertices inside SCCs that are both
reachable from SCC(0) and can reach SCC(n-1) in the SCC DAG)
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building adjacency lists: O(n + m)
    - Tarjan SCC computation: O(n + m)
    - Building SCC DAG reachability helpers: O(number of SCCs + number of SCC edges), which is O(n + m)
    - Final counting: O(n)

    Overall: O(n + m)

    Space Complexity:
    - Graph storage, reverse graph helper arrays, Tarjan stacks/arrays, SCC DAG arrays:
      O(n + m)

    Overall: O(n + m)
    */
    public int MaximumLockedJunctions(int n, int[][] edges)
    {
        // ------------------------------------------------------------
        // STEP 1: Build the original directed graph.
        //
        // We store:
        // - g[u]  : all outgoing neighbors from u
        //
        // Why?
        // We need the original graph to compute strongly connected components (SCCs).
        // Tarjan's algorithm works directly on the original adjacency list.
        // ------------------------------------------------------------
        var g = new List<int>[n];
        for (int i = 0; i < n; i++)
            g[i] = new List<int>();

        foreach (var e in edges)
            g[e[0]].Add(e[1]);

        // ------------------------------------------------------------
        // STEP 2: Compute SCCs using Tarjan's algorithm.
        //
        // Why SCCs?
        // Inside an SCC, every node can reach every other node.
        // So for our validity condition, SCCs behave like indivisible "super-nodes".
        // After compressing SCCs, the graph becomes a DAG, which is much easier to reason about.
        //
        // Tarjan arrays:
        // - disc[u] = DFS discovery time of u (0 means unvisited)
        // - low[u]  = smallest discovery time reachable from u through DFS tree/back edges
        // - onStack[u] = whether u is currently in Tarjan's stack
        // - comp[u] = SCC id assigned to u
        //
        // We also keep:
        // - stack of active nodes
        // - compCount = number of SCCs found
        // - compSize[id] = how many original vertices are inside SCC id
        // ------------------------------------------------------------
        int time = 0;
        int compCount = 0;

        var disc = new int[n];
        var low = new int[n];
        var onStack = new bool[n];
        var comp = new int[n];
        Array.Fill(comp, -1);

        var stack = new Stack<int>();
        var compSizesList = new List<int>();

        void Dfs(int u)
        {
            disc[u] = ++time;
            low[u] = disc[u];
            stack.Push(u);
            onStack[u] = true;

            foreach (int v in g[u])
            {
                if (disc[v] == 0)
                {
                    Dfs(v);
                    low[u] = Math.Min(low[u], low[v]);
                }
                else if (onStack[v])
                {
                    low[u] = Math.Min(low[u], disc[v]);
                }
            }

            if (low[u] == disc[u])
            {
                int size = 0;
                while (true)
                {
                    int x = stack.Pop();
                    onStack[x] = false;
                    comp[x] = compCount;
                    size++;
                    if (x == u) break;
                }

                compSizesList.Add(size);
                compCount++;
            }
        }

        for (int i = 0; i < n; i++)
        {
            if (disc[i] == 0)
                Dfs(i);
        }

        int[] compSize = compSizesList.ToArray();

        // ------------------------------------------------------------
        // STEP 3: Build the SCC condensation DAG.
        //
        // Each SCC becomes one node.
        // For every original edge u -> v:
        //   if comp[u] != comp[v], then we add an SCC edge comp[u] -> comp[v].
        //
        // Why?
        // The SCC graph is a DAG.
        // Our problem becomes:
        //   keep the minimum number of SCCs so that every kept SCC reachable from start SCC
        //   can still reach exit SCC.
        //
        // We also build the reverse SCC DAG because we need to know which SCCs can reach the exit SCC.
        //
        // We use HashSet per SCC to avoid duplicate DAG edges caused by parallel edges or many original edges
        // between the same pair of SCCs.
        // ------------------------------------------------------------
        var dagSet = new HashSet<int>[compCount];
        var revDagSet = new HashSet<int>[compCount];
        for (int i = 0; i < compCount; i++)
        {
            dagSet[i] = new HashSet<int>();
            revDagSet[i] = new HashSet<int>();
        }

        foreach (var e in edges)
        {
            int cu = comp[e[0]];
            int cv = comp[e[1]];
            if (cu != cv && dagSet[cu].Add(cv))
            {
                revDagSet[cv].Add(cu);
            }
        }

        var dag = new List<int>[compCount];
        var revDag = new List<int>[compCount];
        for (int i = 0; i < compCount; i++)
        {
            dag[i] = new List<int>(dagSet[i]);
            revDag[i] = new List<int>(revDagSet[i]);
        }

        int startComp = comp[0];
        int exitComp = comp[n - 1];

        // ------------------------------------------------------------
        // STEP 4: Find all SCCs reachable from the start SCC in the SCC DAG.
        //
        // Why?
        // Any unlocked SCC that matters for the validity condition must be reachable from 0.
        // If an SCC is not reachable from 0, it can safely remain unlocked or locked without affecting
        // the rule, because the rule only talks about unlocked vertices reachable from 0.
        //
        // However, since we want to maximize the number of locked vertices, we will eventually lock
        // every original vertex not in the essential kept set (except 0 and n-1 are automatically included
        // because their SCCs will be in the kept set).
        //
        // We use iterative DFS to avoid recursion depth issues on large inputs.
        // ------------------------------------------------------------
        var fromStart = new bool[compCount];
        var st = new Stack<int>();
        st.Push(startComp);
        fromStart[startComp] = true;

        while (st.Count > 0)
        {
            int u = st.Pop();
            foreach (int v in dag[u])
            {
                if (!fromStart[v])
                {
                    fromStart[v] = true;
                    st.Push(v);
                }
            }
        }

        // ------------------------------------------------------------
        // STEP 5: Find all SCCs that can reach the exit SCC.
        //
        // We do this by traversing the REVERSED SCC DAG starting from exitComp.
        //
        // Why?
        // In the original SCC DAG, "u can reach exitComp" is equivalent to
        // "u is reachable from exitComp in the reversed DAG".
        //
        // These SCCs are exactly the SCCs that have some path to the exit.
        // ------------------------------------------------------------
        var toExit = new bool[compCount];
        st.Clear();
        st.Push(exitComp);
        toExit[exitComp] = true;

        while (st.Count > 0)
        {
            int u = st.Pop();
            foreach (int v in revDag[u])
            {
                if (!toExit[v])
                {
                    toExit[v] = true;
                    st.Push(v);
                }
            }
        }

        // ------------------------------------------------------------
        // STEP 6: Count how many original vertices must remain unlocked.
        //
        // Fundamental theorem for this problem:
        // The minimum valid kept set of SCCs is exactly:
        //   { SCC x | x is reachable from startComp AND x can reach exitComp }
        //
        // Why this is correct:
        //
        // (A) Necessity:
        // Suppose an SCC K is kept and reachable from start in the remaining graph.
        // Validity says K must be able to reach the exit in the remaining graph.
        // Therefore K must be on some route from start to exit, so:
        //   - K is reachable from start
        //   - K can reach exit
        //
        // (B) Sufficiency:
        // If we keep exactly all SCCs satisfying both properties, then for any kept SCC K:
        //   - K is reachable from start
        //   - K can reach exit
        // Since all SCCs on a path from K to exit also satisfy these two properties,
        // that path remains entirely inside the kept set.
        // So every kept reachable SCC can still reach exit.
        //
        // Therefore this set is valid and minimal.
        //
        // Then:
        //   maximum locked = total vertices - kept vertices
        // ------------------------------------------------------------
        long keptVertices = 0;
        for (int c = 0; c < compCount; c++)
        {
            if (fromStart[c] && toExit[c])
                keptVertices += compSize[c];
        }

        return (int)(n - keptVertices);
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int n1 = 6;
int[][] edges1 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 2, 5 },
    new[] { 0, 3 },
    new[] { 3, 4 }
};
int result1 = solution.MaximumLockedJunctions(n1, edges1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int n2 = 8;
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 2, 7 },
    new[] { 0, 3 },
    new[] { 3, 2 },
    new[] { 1, 4 },
    new[] { 4, 5 },
    new[] { 5, 4 },
    new[] { 5, 6 },
    new[] { 6, 7 }
};
int result2 = solution.MaximumLockedJunctions(n2, edges2);
Console.WriteLine(result2); // Expected: 3

// Additional small sanity check:
// Graph: 0 -> 1 -> 2, exit is 2
// All nodes are on the only valid path, so answer should be 0.
int n3 = 3;
int[][] edges3 =
{
    new[] { 0, 1 },
    new[] { 1, 2 }
};
int result3 = solution.MaximumLockedJunctions(n3, edges3);
Console.WriteLine(result3); // Expected: 0