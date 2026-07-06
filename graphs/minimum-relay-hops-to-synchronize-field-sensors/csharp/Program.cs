/*
Title: Minimum Relay Hops to Synchronize Field Sensors

Problem Description:
A company deploys wireless sensors across a large farm. Each sensor can directly relay data to some other sensors,
forming a directed graph of communication links. Sensor 0 is the central base station. A sensor is considered
synchronized if it can send a message to sensor 0 by following one or more directed relay links.

You are given an integer n and a list edges where edges[i] = [u, v] means sensor u can directly send data to sensor v.
You may install additional one-way relay boosters. Installing a booster from sensor a to sensor b creates a new
directed edge a -> b. Your goal is to make every sensor synchronized using the minimum number of new boosters.

Return the minimum number of boosters required.

Important details:
- You may choose any pair of sensors when adding a booster.
- Existing edges remain unchanged.
- A sensor already synchronized does not need any extra work.
- If several unsynchronized regions exist, it may be optimal to connect one region to another before reaching sensor 0.

Key idea:
- Inside a strongly connected component (SCC), every node can already reach every other node in that component.
- So we can compress the graph into SCCs, producing a DAG (Directed Acyclic Graph).
- In that DAG, we want every component to have a path to the component containing node 0.
- The minimum number of new edges needed equals the number of SCCs that:
  1) are NOT already able to reach the SCC containing node 0, and
  2) have zero outgoing edges to other SCCs among that "bad" subgraph.

Why?
- Each such sink SCC in the unsynchronized portion must receive at least one new outgoing route eventually.
- One added edge from each such sink SCC is sufficient: connect it directly to the SCC of node 0.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building adjacency lists: O(n + m)
    - Kosaraju's algorithm (two DFS passes): O(n + m)
    - Building SCC DAG information and counting sink components: O(n + m)
    Overall: O(n + m)

    Space Complexity:
    - Graph + reverse graph + stacks/arrays + component data: O(n + m)

    Where:
    - n = number of sensors (nodes)
    - m = number of directed communication links (edges)
    */
    public int MinBoosters(int n, int[][] edges)
    {
        // -----------------------------
        // STEP 1: Build the graph and the reversed graph.
        // -----------------------------
        //
        // We use adjacency lists because:
        // - The graph can be very large.
        // - Adjacency lists are memory-efficient for sparse graphs.
        // - DFS traversals naturally work well with adjacency lists.
        //
        // graph[u] contains all v such that u -> v exists.
        // reverseGraph[v] contains all u such that u -> v exists in the original graph,
        // meaning reverseGraph lets us traverse edges backward.
        //
        var graph = new List<int>[n];
        var reverseGraph = new List<int>[n];

        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<int>();
            reverseGraph[i] = new List<int>();
        }

        foreach (var edge in edges)
        {
            int u = edge[0];
            int v = edge[1];
            graph[u].Add(v);
            reverseGraph[v].Add(u);
        }

        // -----------------------------
        // STEP 2: First pass of Kosaraju's algorithm.
        // -----------------------------
        //
        // Goal:
        // Compute nodes in order of finishing times using DFS on the original graph.
        //
        // Why this matters:
        // Kosaraju's algorithm finds SCCs in two passes.
        // In the first pass, we record the order in which DFS finishes nodes.
        // In the second pass, processing nodes in reverse finishing order on the reversed graph
        // guarantees that each DFS discovers exactly one SCC.
        //
        // Important implementation note:
        // Because n can be as large as 200,000, recursive DFS may cause stack overflow in C#.
        // So we implement DFS iteratively using explicit stacks.
        //
        var visited = new bool[n];
        var finishOrder = new List<int>(n);

        for (int start = 0; start < n; start++)
        {
            if (visited[start]) continue;

            // Iterative DFS that simulates recursion.
            // Each stack item stores:
            // - node: current node
            // - nextIndex: which outgoing edge we should process next
            //
            // This lets us know when all children are done, so we can add the node
            // to finishOrder at the correct "postorder" moment.
            var stack = new Stack<(int node, int nextIndex)>();
            stack.Push((start, 0));
            visited[start] = true;

            while (stack.Count > 0)
            {
                var (node, nextIndex) = stack.Pop();

                if (nextIndex < graph[node].Count)
                {
                    // We are not done exploring this node yet.
                    // Put it back with nextIndex + 1 so we continue later.
                    stack.Push((node, nextIndex + 1));

                    int neighbor = graph[node][nextIndex];
                    if (!visited[neighbor])
                    {
                        visited[neighbor] = true;
                        stack.Push((neighbor, 0));
                    }
                }
                else
                {
                    // All outgoing edges have been processed.
                    // This is the iterative equivalent of the DFS "finish" moment.
                    finishOrder.Add(node);
                }
            }
        }

        // -----------------------------
        // STEP 3: Second pass of Kosaraju's algorithm on the reversed graph.
        // -----------------------------
        //
        // Goal:
        // Assign each node to an SCC id.
        //
        // We process nodes in reverse finishing order from step 2.
        // Every DFS on the reversed graph identifies one SCC.
        //
        // componentId[node] = which SCC this node belongs to.
        //
        Array.Fill(visited, false);
        var componentId = new int[n];
        int componentCount = 0;

        for (int i = finishOrder.Count - 1; i >= 0; i--)
        {
            int start = finishOrder[i];
            if (visited[start]) continue;

            var stack = new Stack<int>();
            stack.Push(start);
            visited[start] = true;
            componentId[start] = componentCount;

            while (stack.Count > 0)
            {
                int node = stack.Pop();

                foreach (int neighbor in reverseGraph[node])
                {
                    if (!visited[neighbor])
                    {
                        visited[neighbor] = true;
                        componentId[neighbor] = componentCount;
                        stack.Push(neighbor);
                    }
                }
            }

            componentCount++;
        }

        // The SCC containing sensor 0 is the "good target" component.
        int zeroComponent = componentId[0];

        // -----------------------------
        // STEP 4: Build information about the SCC DAG.
        // -----------------------------
        //
        // After SCC compression, each SCC becomes one node in a DAG.
        // If there is an original edge u -> v with componentId[u] != componentId[v],
        // then there is a DAG edge componentId[u] -> componentId[v].
        //
        // We need to know which SCCs can already reach zeroComponent.
        //
        // A very efficient way:
        // - In the SCC DAG, a component can reach zeroComponent in the original DAG
        //   iff zeroComponent can reach it in the REVERSED SCC DAG.
        //
        // So we build reverse edges between components and run a traversal starting from zeroComponent.
        //
        var reverseComponentGraph = new List<int>[componentCount];
        for (int i = 0; i < componentCount; i++)
        {
            reverseComponentGraph[i] = new List<int>();
        }

        // We also want to know, among the components that are NOT able to reach zeroComponent,
        // which ones are sinks inside that "bad" subgraph.
        //
        // So later we will count outgoing edges from each bad component to another bad component.
        //
        for (int u = 0; u < n; u++)
        {
            int cu = componentId[u];
            foreach (int v in graph[u])
            {
                int cv = componentId[v];
                if (cu != cv)
                {
                    // Reverse SCC DAG edge: cv -> cu
                    reverseComponentGraph[cv].Add(cu);
                }
            }
        }

        // -----------------------------
        // STEP 5: Mark all SCCs that can reach the SCC containing node 0.
        // -----------------------------
        //
        // We traverse the reversed SCC DAG starting from zeroComponent.
        // If we can go from zeroComponent to some component X in the reversed DAG,
        // then X can reach zeroComponent in the original DAG.
        //
        var canReachZero = new bool[componentCount];
        var componentStack = new Stack<int>();
        componentStack.Push(zeroComponent);
        canReachZero[zeroComponent] = true;

        while (componentStack.Count > 0)
        {
            int comp = componentStack.Pop();

            foreach (int prevComp in reverseComponentGraph[comp])
            {
                if (!canReachZero[prevComp])
                {
                    canReachZero[prevComp] = true;
                    componentStack.Push(prevComp);
                }
            }
        }

        // -----------------------------
        // STEP 6: Count outgoing edges inside the "bad" SCC subgraph.
        // -----------------------------
        //
        // "Bad" components are SCCs that currently cannot reach zeroComponent.
        //
        // Consider only these bad components and the edges between them.
        // In this bad subgraph (which is still a DAG), the minimum number of new edges needed
        // equals the number of sink components (components with zero outgoing edges inside the bad subgraph).
        //
        // Why sinks?
        // - A sink bad component has no way to pass its synchronization responsibility to another bad component.
        // - Therefore, at least one new edge must ultimately be added from each such sink region.
        // - Conversely, adding one edge from each sink directly to zeroComponent is enough.
        //
        var badOutDegree = new int[componentCount];

        for (int u = 0; u < n; u++)
        {
            int cu = componentId[u];

            foreach (int v in graph[u])
            {
                int cv = componentId[v];

                // We only care about edges between DIFFERENT SCCs.
                if (cu == cv) continue;

                // We only count edges that stay inside the bad subgraph.
                if (!canReachZero[cu] && !canReachZero[cv])
                {
                    badOutDegree[cu]++;
                }
            }
        }

        // -----------------------------
        // STEP 7: Count sink SCCs in the bad subgraph.
        // -----------------------------
        //
        // Every bad SCC with badOutDegree == 0 is a sink in the unsynchronized SCC DAG.
        // The answer is exactly the number of such sink SCCs.
        //
        int answer = 0;

        for (int comp = 0; comp < componentCount; comp++)
        {
            if (!canReachZero[comp] && badOutDegree[comp] == 0)
            {
                answer++;
            }
        }

        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1:
// n = 6
// edges = [[1,0],[2,1],[3,4],[4,5]]
// Expected output: 1
int n1 = 6;
int[][] edges1 =
{
    new[] { 1, 0 },
    new[] { 2, 1 },
    new[] { 3, 4 },
    new[] { 4, 5 }
};
int result1 = solution.MinBoosters(n1, edges1);
Console.WriteLine(result1);

// Example 2:
// n = 7
// edges = [[1,2],[2,3],[3,1],[4,5],[5,6]]
// Expected output: 2
int n2 = 7;
int[][] edges2 =
{
    new[] { 1, 2 },
    new[] { 2, 3 },
    new[] { 3, 1 },
    new[] { 4, 5 },
    new[] { 5, 6 }
};
int result2 = solution.MinBoosters(n2, edges2);
Console.WriteLine(result2);

// Additional quick sanity check:
// If all nodes already reach 0, answer should be 0.
int n3 = 4;
int[][] edges3 =
{
    new[] { 1, 0 },
    new[] { 2, 1 },
    new[] { 3, 2 }
};
int result3 = solution.MinBoosters(n3, edges3);
Console.WriteLine(result3);