# Closest Cycle Entry from a Start City

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, BFS, Strongly Connected Components

---

## 🗂 Problem Overview
Given a directed graph, a start city `s`, and up to `10^5` nodes / `2*10^5` edges, find the reachable city that lies on some directed cycle and is closest to `s` by shortest-path distance. Return `[distance, city]`, breaking ties by smaller city index. If no cycle node is reachable, return `[-1, -1]`. The non-trivial part is that cycle membership in directed graphs is global structure, not something you can test independently per node without blowing up runtime.

## 🌍 Engineering Impact
This pattern shows up anywhere a directed dependency graph mixes reachability with feedback detection: workflow engines finding the nearest cyclic stage from a source job, compiler/module systems detecting reachable recursive dependency clusters, streaming pipelines locating the first feedback loop downstream of an ingress, and service-call graphs identifying the closest reachable retry loop or routing cycle. At scale, naive per-node cycle checks collapse under repeated traversals. The right decomposition—reachability first, structural condensation second—lets you answer “what problematic strongly-connected region do I hit first?” in linear time, which is exactly the difference between usable graph diagnostics and operational noise.

## 🔍 Problem Statement
You are given a directed graph with `n` cities labeled `0..n-1`, a list of directed roads `edges`, and a start city `s`. A city is a valid target if it belongs to at least one directed cycle and is reachable from `s`. Return `[distance, city]`, where `distance` is the shortest directed-path length from `s` to that city. If multiple valid cities have the same minimum distance, choose the smallest city index. If none exist, return `[-1, -1]`.

Constraints: `1 <= n <= 100000`, `0 <= m <= 200000`, self-loops and duplicate edges allowed.

Examples:

- `n = 7, edges = [[0,1],[1,2],[2,3],[3,1],[2,4],[4,5]], s = 0` → `[1,1]`
- `n = 6, edges = [[0,1],[1,2],[2,4],[4,5],[3,3]], s = 0` → `[-1,-1]`

The key algorithmic constraint is graph size: anything worse than linear or near-linear in `n + m` is not viable.

## 🪜 How to Solve This
1. Start with the output requirement: shortest distance from `s` to a city that is on a directed cycle. Shortest distance in an unweighted graph immediately suggests BFS.

2. But BFS alone cannot tell whether a node belongs to a directed cycle. In directed graphs, local degree checks are insufficient; cycle membership is about mutual reachability.

3. Mutual reachability points directly to strongly connected components (SCCs). Every node in an SCC of size greater than `1` lies on a directed cycle, and a size-`1` SCC is cyclic only if it has a self-loop.

4. We do not need SCCs for the whole graph if we think operationally: only nodes reachable from `s` can matter. So first run BFS/DFS from `s` and mark the reachable subgraph.

5. Then run an SCC algorithm on just that reachable portion. Mark every node whose SCC is cyclic.

6. Finally, reuse the BFS distances from `s` and scan all reachable cyclic nodes. Pick the minimum distance; if tied, pick the smallest index.

That chain gives a linear-time solution: reachability narrows scope, SCC identifies cycle membership correctly, BFS supplies the shortest-path metric.

## 🧩 Algorithm Walkthrough
1. **Build adjacency lists**  
   Store outgoing edges for the directed graph. Also keep a reverse graph if using Kosaraju. This gives `O(n + m)` traversal cost and is the standard representation for sparse graphs at this scale.

2. **Run BFS from `s` to compute reachability and shortest distances**  
   Use a queue, initialize `dist[s] = 0`, and visit each reachable node once. Invariant: when a node is dequeued, `dist[node]` is its shortest path length from `s`. This is the **BFS shortest-path pattern** for unweighted directed graphs.

3. **Restrict attention to the reachable subgraph**  
   Ignore every node not marked reachable. Correctness is immediate: unreachable cycle nodes can never be answers, so excluding them cannot remove a valid solution.

4. **Compute SCCs on the reachable subgraph**  
   Use **Kosaraju** or **Tarjan**. The right abstraction is the **Strongly Connected Components pattern** because directed-cycle membership is equivalent to belonging to a cyclic SCC. Invariant: each node is assigned to exactly one SCC, and nodes in the same SCC are mutually reachable.

5. **Mark which SCCs are cyclic**  
   An SCC is cyclic if it has more than one node, or exactly one node with a self-loop. This handles the important edge case where a single node still forms a directed cycle.

6. **Select the answer using BFS distances**  
   Scan all reachable nodes. For each node in a cyclic SCC, compare `(dist[node], node)` lexicographically. The minimum pair is the required answer because BFS already guarantees shortest distances, and the scan enforces the tie-break on city index.

7. **Return `[-1, -1]` if no reachable cyclic node exists**  
   This means the reachable region is acyclic, even if cycles exist elsewhere in the graph.

## 📊 Worked Example
Take `n = 7`, `edges = [[0,1],[1,2],[2,3],[3,1],[2,4],[4,5]]`, `s = 0`.

| Step | State |
|---|---|
| BFS start | `dist[0]=0` |
| Visit `0` | enqueue `1`, `dist[1]=1` |
| Visit `1` | enqueue `2`, `dist[2]=2` |
| Visit `2` | enqueue `3`, `dist[3]=3`; enqueue `4`, `dist[4]=3` |
| Visit `3` | edge to `1` already seen |
| Visit `4` | enqueue `5`, `dist[5]=4` |
| Reachable set | `{0,1,2,3,4,5}` |
| SCCs | `{0}`, `{1,2,3}`, `{4}`, `{5}` |
| Cyclic SCCs | `{1,2,3}` only, because size `3` |
| Candidate nodes | `1 (dist 1)`, `2 (dist 2)`, `3 (dist 3)` |
| Best answer | `[1,1]` |

City `1` wins because it is the closest node on any reachable directed cycle. City `6` is absent and irrelevant; unreachable nodes never participate.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`. BFS over the graph is linear, and SCC computation via Kosaraju or Tarjan is also linear in nodes plus edges. That remains practical at `10^6` total graph elements, while any repeated DFS/BFS per node trends toward quadratic behavior and becomes infeasible well before `10^9` operations.

### Space Complexity
`O(n + m)`, dominated by adjacency storage, reverse adjacency if used, distance/reachability arrays, and SCC metadata. You can avoid the reverse graph with Tarjan instead of Kosaraju, but that trades simpler reasoning for a more stateful implementation.

## 💡 Key Takeaways
- If a directed-graph problem asks for nodes “on a cycle,” think SCCs immediately; directed cycle membership is a component property, not a local one.
- If the answer is “closest from a source” in an unweighted graph, BFS should own distance computation even if another algorithm handles structural classification.
- A size-`1` SCC is not automatically acyclic; a self-loop makes that single node a valid cycle-entry city.
- Do not compute SCCs and then ignore reachability semantics in selection; only nodes reachable from `s` are eligible, even if other cyclic SCCs exist.
- In production graph analysis, separating traversal concerns from structural concerns—reachability first, component condensation second—keeps both runtime and system reasoning tractable.

## 🚀 Variations & Further Practice
- Return the closest reachable **cycle itself** rather than a node: now you need SCC-level selection plus a canonical representative or full component reconstruction.
- Support **many start cities** with repeated queries: precompute SCC condensation and answer on the DAG, or add indexing for nearest cyclic component reachability.
- Minimize **weighted** distance to a reachable cycle-entry city: BFS no longer applies; replace it with Dijkstra while keeping SCC-based cycle detection.