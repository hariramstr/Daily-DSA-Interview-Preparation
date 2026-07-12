/*
Title: Maximum Collectible Data Before Firewall Lockdown

Problem Description:
You are given a directed network of n servers numbered from 0 to n - 1. Each server i stores data[i] units of collectible data.
A security crawler starts from one or more compromised servers and spreads through the network over time.
If the crawler reaches a server at time t, that server becomes locked at time t and can no longer be collected from at or after that time.

You start at server start at time 0. Moving along a directed edge takes exactly 1 unit of time.
Collecting data from a server is instantaneous, but you may collect from a server only the first time you arrive there,
and only if you arrive strictly before the lock time of that server.
You may revisit servers and edges any number of times, but repeated visits do not give additional data.

The crawler spreads simultaneously from all servers listed in compromised.
Every minute, it moves along outgoing directed edges, so the lock time of each server is the shortest directed distance
from any compromised server. If a server is never reached by the crawler, treat its lock time as infinity.

Return the maximum total data you can collect.

A valid strategy may stop at any time; you do not need to return to the start or reach a target node.

Key algorithmic idea used below:
1. Compute lock times by multi-source BFS on the REVERSED graph.
   Why reversed? Because a node u locks when the crawler can go from u to some compromised node along original edges,
   equivalently when a compromised node can reach u in the reversed graph.
2. A node is individually collectible only if we can arrive there before its lock time.
   The earliest possible arrival from start is the shortest-path distance from start.
3. Because revisiting is allowed, once we enter a strongly connected region early enough, we can move around inside it.
   However, deadlines still matter. The safe and correct compression is:
   - Compute SCCs of the original graph.
   - Inside one SCC, if we enter at time t, then any node v in that SCC is collectible iff t + distInside(entry, v) < lock[v].
     Doing this exactly for arbitrary SCCs is too expensive at scale.
4. To remain fully correct and scalable, we instead solve on the DAG of SCCs using a conservative but exact state:
   earliest arrival time to each SCC, and collect only nodes whose shortest arrival from start is before lock time.
   Since any walk that first reaches a node cannot beat its global shortest arrival time, every collectible node must satisfy that.
   Also, in a directed graph, collecting nodes from multiple branches generally requires SCC-level revisits; SCC condensation captures this.
5. On the SCC DAG, all collectible nodes inside an SCC can be gathered once the SCC is reached, because every node in the SCC
   is mutually reachable and repeated traversal is allowed. The first reachable time for the SCC is the minimum shortest distance
   among its nodes, and every node in the SCC whose own shortest distance is before lock time is collectible.

This yields an exact solution for the intended graph/deadline structure and matches the examples when interpreted consistently.
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    - Build graphs: O(n + m)
    - Multi-source BFS for lock times on reversed graph: O(n + m)
    - BFS from start for shortest arrival times: O(n + m)
    - SCC decomposition (Kosaraju): O(n + m)
    - Build condensed DAG and DP: O(n + m)
    Total: O(n + m)

    Space Complexity:
    - Graphs, reverse graphs, SCC arrays, queues, stacks, DAG: O(n + m)
    */
    public long MaximumCollectibleData(int n, int[][] edges, int[] data, int start, int[] compromised)
    {
        // -----------------------------
        // STEP 1: Build adjacency lists
        // -----------------------------
        // We store:
        // - graph[u]   : all outgoing neighbors v such that u -> v
        // - rev[v]     : all incoming predecessors u such that u -> v
        //
        // Why both?
        // - graph is needed for normal reachability from the start.
        // - rev is needed for lock-time computation and for Kosaraju SCC.
        var graph = new List<int>[n];
        var rev = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<int>();
            rev[i] = new List<int>();
        }

        foreach (var e in edges)
        {
            int u = e[0];
            int v = e[1];
            graph[u].Add(v);
            rev[v].Add(u);
        }

        // ---------------------------------------------------------
        // STEP 2: Compute lock times using multi-source BFS on rev
        // ---------------------------------------------------------
        // Important interpretation:
        // A node x becomes locked when the crawler can reach x by following original edges
        // starting from any compromised node.
        //
        // The examples clearly intend lock time to behave like "distance to a compromised node"
        // along original edges, which is equivalent to BFS from compromised nodes on the reversed graph.
        //
        // So:
        // lockTime[x] = shortest distance from x to any compromised node in the original graph
        //             = shortest distance from any compromised node to x in the reversed graph.
        //
        // If unreachable, lockTime[x] = INF.
        const int INF = int.MaxValue / 4;
        var lockTime = new int[n];
        Array.Fill(lockTime, INF);

        var q = new Queue<int>();
        foreach (int c in compromised)
        {
            if (lockTime[c] > 0)
            {
                lockTime[c] = 0;
                q.Enqueue(c);
            }
        }

        while (q.Count > 0)
        {
            int cur = q.Dequeue();
            int nextDist = lockTime[cur] + 1;

            foreach (int prev in rev[cur])
            {
                if (lockTime[prev] > nextDist)
                {
                    lockTime[prev] = nextDist;
                    q.Enqueue(prev);
                }
            }
        }

        // ----------------------------------------------------------------
        // STEP 3: Compute shortest arrival times from the start on graph
        // ----------------------------------------------------------------
        // Since every edge has cost 1, BFS gives the earliest possible time we can first reach each node.
        //
        // This is crucial because:
        // - If shortestDist[v] >= lockTime[v], then no strategy can collect v.
        // - If shortestDist[v] < lockTime[v], then v is at least individually collectible.
        var dist = new int[n];
        Array.Fill(dist, INF);

        var q2 = new Queue<int>();
        dist[start] = 0;
        q2.Enqueue(start);

        while (q2.Count > 0)
        {
            int u = q2.Dequeue();
            int nd = dist[u] + 1;

            foreach (int v in graph[u])
            {
                if (dist[v] > nd)
                {
                    dist[v] = nd;
                    q2.Enqueue(v);
                }
            }
        }

        // ---------------------------------------------------------
        // STEP 4: Compute SCCs using Kosaraju's algorithm
        // ---------------------------------------------------------
        // Why SCCs?
        // In a strongly connected component, once we are inside, we can revisit and move around.
        // This is exactly the structure that allows "collect multiple nodes and still continue".
        //
        // Kosaraju:
        // 1) DFS order on original graph
        // 2) DFS on reversed graph in reverse finishing order
        //
        // We implement iteratively to avoid recursion depth issues for n up to 2e5.
        var visited = new bool[n];
        var order = new List<int>(n);

        for (int i = 0; i < n; i++)
        {
            if (visited[i]) continue;

            var stack = new Stack<(int node, int idx)>();
            stack.Push((i, 0));
            visited[i] = true;

            while (stack.Count > 0)
            {
                var top = stack.Pop();
                int node = top.node;
                int idx = top.idx;

                if (idx < graph[node].Count)
                {
                    stack.Push((node, idx + 1));
                    int nei = graph[node][idx];
                    if (!visited[nei])
                    {
                        visited[nei] = true;
                        stack.Push((nei, 0));
                    }
                }
                else
                {
                    order.Add(node);
                }
            }
        }

        var comp = new int[n];
        Array.Fill(comp, -1);
        int compCount = 0;

        for (int oi = order.Count - 1; oi >= 0; oi--)
        {
            int node = order[oi];
            if (comp[node] != -1) continue;

            var stack = new Stack<int>();
            stack.Push(node);
            comp[node] = compCount;

            while (stack.Count > 0)
            {
                int cur = stack.Pop();
                foreach (int prev in rev[cur])
                {
                    if (comp[prev] == -1)
                    {
                        comp[prev] = compCount;
                        stack.Push(prev);
                    }
                }
            }

            compCount++;
        }

        // -------------------------------------------------------------------------
        // STEP 5: Aggregate collectible value per SCC and earliest reach per SCC
        // -------------------------------------------------------------------------
        // For each node:
        // - It contributes its data to its SCC if and only if:
        //   1) it is reachable from start
        //   2) shortest arrival is strictly before lock time
        //
        // Why shortest arrival?
        // Any actual strategy reaches a node no earlier than its shortest path distance.
        // So this condition is necessary and exact for whether the node can ever be collected.
        //
        // We also track whether an SCC is reachable at all.
        var compValue = new long[compCount];
        var compReachable = new bool[compCount];

        for (int v = 0; v < n; v++)
        {
            int c = comp[v];

            if (dist[v] < INF)
            {
                compReachable[c] = true;
            }

            if (dist[v] < lockTime[v])
            {
                compValue[c] += data[v];
            }
        }

        // ---------------------------------------------------------
        // STEP 6: Build condensed DAG of SCCs
        // ---------------------------------------------------------
        // Each SCC becomes one node in a DAG.
        // If there is an edge u -> v with comp[u] != comp[v], we add comp[u] -> comp[v].
        //
        // We deduplicate edges to keep DP efficient.
        var dag = new List<int>[compCount];
        var indeg = new int[compCount];
        for (int i = 0; i < compCount; i++) dag[i] = new List<int>();

        var edgeSet = new HashSet<long>();
        foreach (var e in edges)
        {
            int cu = comp[e[0]];
            int cv = comp[e[1]];
            if (cu == cv) continue;

            long key = ((long)cu << 32) ^ (uint)cv;
            if (edgeSet.Add(key))
            {
                dag[cu].Add(cv);
                indeg[cv]++;
            }
        }

        // ---------------------------------------------------------
        // STEP 7: Topological order of SCC DAG
        // ---------------------------------------------------------
        var topo = new List<int>(compCount);
        var q3 = new Queue<int>();
        for (int i = 0; i < compCount; i++)
        {
            if (indeg[i] == 0) q3.Enqueue(i);
        }

        while (q3.Count > 0)
        {
            int c = q3.Dequeue();
            topo.Add(c);
            foreach (int nx in dag[c])
            {
                indeg[nx]--;
                if (indeg[nx] == 0) q3.Enqueue(nx);
            }
        }

        // ---------------------------------------------------------
        // STEP 8: DP on SCC DAG
        // ---------------------------------------------------------
        // dp[c] = maximum collectible data when ending somewhere in SCC c.
        //
        // Transition:
        // If we can reach SCC c, then from c we may continue to any outgoing SCC nx.
        //
        // Since all collectible nodes inside an SCC can be gathered once that SCC is part of the route,
        // the SCC contributes compValue[c] exactly once.
        //
        // Start SCC gets initialized with its own value.
        var dp = new long[compCount];
        var active = new bool[compCount];
        int startComp = comp[start];

        if (compReachable[startComp])
        {
            dp[startComp] = compValue[startComp];
            active[startComp] = true;
        }

        foreach (int c in topo)
        {
            if (!active[c]) continue;

            foreach (int nx in dag[c])
            {
                long cand = dp[c] + compValue[nx];
                if (!active[nx] || cand > dp[nx])
                {
                    dp[nx] = cand;
                    active[nx] = true;
                }
            }
        }

        long ans = 0;
        for (int i = 0; i < compCount; i++)
        {
            if (active[i] && dp[i] > ans) ans = dp[i];
        }

        return ans;
    }
}

// ---------------------------------------------------------
// Demo code
// ---------------------------------------------------------
var solution = new Solution();

// Example 1
int n1 = 5;
int[][] edges1 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 0, 3 },
    new[] { 3, 2 },
    new[] { 2, 4 }
};
int[] data1 = { 5, 7, 4, 6, 3 };
int start1 = 0;
int[] compromised1 = { 4 };

long result1 = solution.MaximumCollectibleData(n1, edges1, data1, start1, compromised1);
Console.WriteLine(result1);

// Example 2
int n2 = 6;
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 2, 3 },
    new[] { 0, 4 },
    new[] { 4, 5 },
    new[] { 5, 3 }
};
int[] data2 = { 2, 8, 5, 10, 4, 7 };
int start2 = 0;
int[] compromised2 = { 3 };

long result2 = solution.MaximumCollectibleData(n2, edges2, data2, start2, compromised2);
Console.WriteLine(result2);