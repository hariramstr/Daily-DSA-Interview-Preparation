# Earliest Meeting Point in a One-Way Conveyor Network

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, BFS, Shortest Path

---

## 🗂 Problem Overview
Given a directed, unweighted graph and two starting nodes, find a node reachable from both starts that minimizes the meeting time, where meeting time is `max(distA[x], distB[x])`. If multiple nodes achieve the same minimum, return the smallest index. If none are reachable from both, return `-1`. The non-trivial part is scale: up to `10^5` nodes and `2 * 10^5` edges rules out per-node recomputation or path enumeration.

## 🌍 Engineering Impact
This pattern shows up anywhere two independent flows must converge under directional constraints: streaming DAGs finding earliest common downstream operators, workflow engines identifying the first shared execution checkpoint, package routing in logistics networks, and dependency analysis in build systems or compilers. At scale, the wrong approach degenerates into repeated reachability checks or all-pairs shortest paths, which is operationally useless on large sparse graphs. The right abstraction—single-source shortest path on an unweighted directed graph, run twice—turns a potentially explosive search into a linear pass over the topology and enables predictable latency, bounded memory, and straightforward reasoning about correctness.

## 🔍 Problem Statement
You are given `n` stations labeled `0..n-1` and a list of directed edges `edges[i] = [u, v]`, where each edge takes exactly 1 minute to traverse. Two packages start simultaneously at `startA` and `startB`. A station `x` is a valid meeting point if both packages can reach it by following edge direction. Its meeting time is `max(dist(startA, x), dist(startB, x))`.

Return the valid meeting point with minimum meeting time. If several stations tie, return the smallest index. If no common reachable station exists, return `-1`.

Constraints:
- `1 <= n <= 100000`
- `0 <= edges.length <= 200000`
- Graph is directed, unweighted, may contain cycles, duplicates, and disconnected components

Examples:
- `n=6, edges=[[0,2],[1,2],[2,3],[1,4],[4,3],[3,5]], startA=0, startB=1` → `2`
- `n=5, edges=[[0,1],[1,2],[3,4]], startA=0, startB=3` → `-1`

The key algorithmic driver is graph size: you need near-linear work, not repeated searches from every candidate node.

## 🪜 How to Solve This
1. Read the objective carefully → we are not looking for any common reachable node; we need the one minimizing the **later** arrival time.
2. “Later arrival time” depends on shortest distances from both starts → this is a shortest-path problem, but edges are all weight `1`, so BFS is the right tool.
3. Run BFS from `startA` → compute `distA[x]`, the earliest time package A can reach every node.
4. Run BFS from `startB` → compute `distB[x]` for package B.
5. Scan all nodes once:
   - if either distance is unreachable, skip it
   - otherwise compute `max(distA[x], distB[x])`
   - keep the node with the smallest such value
   - break ties by smaller index
6. Why this works → for any candidate meeting point, only the shortest arrival times matter. Any longer path is dominated and cannot improve `max(distA[x], distB[x])`.
7. Why this is efficient → two BFS traversals plus one linear scan gives `O(n + m)` on a sparse graph, which fits the constraints comfortably.

## 🧩 Algorithm Walkthrough
1. **Build the adjacency list**  
   Represent the directed graph as `adj[u] -> list of v`. This is the standard graph traversal setup for sparse graphs. Duplicate edges are harmless: BFS may inspect them, but visited-distance checks prevent correctness issues.

2. **Run BFS from `startA`**  
   Use a queue and a distance array initialized to `-1`. Set `distA[startA] = 0`, then expand level by level.  
   **Why correct:** in an unweighted graph, BFS discovers each node at minimum hop count.  
   **Invariant:** when a node is dequeued, its recorded distance is already the shortest possible from `startA`.

3. **Run BFS from `startB`**  
   Repeat the same process to compute `distB`.  
   **Why needed:** the objective depends on both arrival times independently; a single traversal cannot encode both sources unless you change the problem semantics.

4. **Evaluate all possible meeting points**  
   For each node `x`, check whether `distA[x] != -1` and `distB[x] != -1`. If so, compute `t = max(distA[x], distB[x])`. Track the minimum `t`, and on ties prefer smaller `x`.  
   **Why correct:** the meeting completes only when both packages have arrived, so the later arrival defines the cost.

5. **Return the best node or `-1`**  
   If no node is reachable from both starts, no valid meeting point exists.

This is a **double BFS + linear selection** pattern: compute shortest-path metadata from each source, then reduce over nodes using the problem-specific objective.

## 📊 Worked Example
Example: `n=6`, `edges=[[0,2],[1,2],[2,3],[1,4],[4,3],[3,5]]`, `startA=0`, `startB=1`

| Node | distA from 0 | distB from 1 | Reachable by both? | Meeting time `max` |
|------|---------------|--------------|--------------------|--------------------|
| 0    | 0             | -1           | No                 | —                  |
| 1    | -1            | 0            | No                 | —                  |
| 2    | 1             | 1            | Yes                | 1                  |
| 3    | 2             | 2            | Yes                | 2                  |
| 4    | -1            | 1            | No                 | —                  |
| 5    | 3             | 3            | Yes                | 3                  |

Trace:
1. BFS from `0` discovers `2 -> 3 -> 5`, giving distances `[0,-1,1,2,-1,3]`.
2. BFS from `1` discovers `2`, `4`, then `3`, then `5`, giving `[-1,0,1,2,1,3]`.
3. Common reachable nodes are `2, 3, 5`.
4. Their meeting times are `1, 2, 3`.
5. Minimum is `1`, so return node `2`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n + m)`, where `m = edges.length`. Building the adjacency list is linear in edges, each BFS visits every node and edge at most once, and the final selection scan is linear in nodes. At `10^6` elements this is routine; at `10^9`, even linear work becomes infrastructure-scale and memory bandwidth dominates.

### Space Complexity
`O(n + m)`. The adjacency list owns `O(m)` space; two distance arrays and the BFS queue require `O(n)`. You could reduce constants by reusing queue storage, but you cannot avoid storing reachability/distances from both sources if you need the final minimization pass.

## 💡 Key Takeaways
- If the graph is unweighted and the objective depends on shortest hop counts from one or more sources, BFS should be your default first candidate.
- If the answer is “best common reachable node,” think in two phases: compute per-source reachability/distances, then reduce across nodes with the target scoring function.
- Do not confuse “first common node discovered” with the optimal answer; BFS traversal order is not the same as minimizing `max(distA[x], distB[x])`.
- Tie-breaking must be handled explicitly after comparing meeting times; relying on queue order or adjacency ordering is incorrect.
- In production graph systems, separating metric computation from selection logic keeps the traversal reusable and the optimization criterion easy to change.

## 🚀 Variations & Further Practice
- **Weighted conveyor times:** edges carry arbitrary positive costs, so BFS no longer works; replace each traversal with Dijkstra and keep the same final minimization over `max(distA[x], distB[x])`.
- **Meet anywhere along an edge, not only at nodes:** the search space becomes node-plus-edge state, and the objective shifts from discrete hop counts to continuous arrival-time alignment.
- **Many package sources instead of two:** find a node minimizing the maximum arrival time across `k` starts; the conceptual twist is scaling from two distance arrays to repeated traversals or multi-source formulations depending on the exact objective.