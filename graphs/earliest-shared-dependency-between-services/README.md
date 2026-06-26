# Earliest Shared Dependency Between Services

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, BFS, Shortest Path

---

## 🗂 Problem Overview
Given a directed dependency graph and two starting services `s1` and `s2`, return the service reachable from both with the minimum total hop count from the two sources. If multiple shared dependencies have the same combined distance, choose the smallest service id. If none exists, return `-1`. The non-trivial part is scale: up to `10^5` nodes, `2 * 10^5` edges, and possible cycles, which rules out repeated per-node searches or naive transitive-closure approaches.

## 🌍 Engineering Impact
This pattern shows up anywhere two origins need a cheapest common downstream point in a directed system: shared libraries in build graphs, common upstream tables in data lineage, overlapping dependencies in service meshes, compiler dependency resolution, and workflow DAG debugging. At platform scale, brute-force reachability checks become operationally expensive and often incorrect once cycles appear from bad metadata or partial migrations. A shortest-path-from-multiple-sources framing gives deterministic behavior, bounded runtime on sparse graphs, and a clean way to rank shared impact points for remediation, caching, rollout planning, or blast-radius analysis.

## 🔍 Problem Statement
You are given `n` services labeled `0..n-1` and a directed edge list `edges`, where `[a, b]` means service `a` directly depends on service `b`. Dependencies are transitive: if `a -> b` and `b -> c`, then `a` indirectly depends on `c`.

For two services `s1` and `s2`, find a node `x` reachable from both that minimizes:

`score(x) = dist(s1, x) + dist(s2, x)`

where `dist(u, v)` is the minimum number of directed edges from `u` to `v`. Break ties by smaller node id. Return `-1` if no shared reachable node exists.

Constraints:
- `1 <= n <= 100000`
- `0 <= edges.length <= 200000`
- `0 <= from, to < n`
- cycles and self-loops may exist

Examples:
- `n=6, edges=[[0,2],[1,2],[2,3],[1,4],[4,3],[0,5]], s1=0, s2=1` → `2`
- `n=5, edges=[[0,1],[1,2],[2,0],[3,2],[3,4]], s1=0, s2=3` → `0`

The key constraint is large sparse graphs with cycles, which points directly to linear-time graph traversal rather than all-pairs reasoning.

## 🪜 How to Solve This
1. Read the objective carefully → we do **not** need all shared dependencies, only the one with minimum `dist(s1, x) + dist(s2, x)`.
2. Distances in an unweighted directed graph → think **BFS**, because every edge costs exactly one hop.
3. We need distances from **two different sources** to every reachable node → run BFS twice, once from `s1`, once from `s2`.
4. Why not DFS? Because DFS gives reachability, not guaranteed shortest path lengths in unweighted graphs.
5. Why not start BFS from every candidate node? Because `n` is up to `100000`; repeated traversals would blow up.
6. After two BFS passes, every node has either:
   - a shortest distance from each source, or
   - “unreachable” from one or both.
7. Scan all nodes once → keep the node reachable from both with the smallest sum of distances.
8. Tie-breaking by smallest id is easy during that final scan.
9. Cycles are harmless if BFS tracks visited/dist arrays and never re-enqueues an already discovered node.

The core insight is to separate the problem into two shortest-path computations plus one reduction pass.

## 🧩 Algorithm Walkthrough
1. **Build the adjacency list**  
   Convert `edges` into `graph[from] -> list of to`. This is the standard representation for sparse directed graphs and gives `O(n + m)` traversal behavior.

2. **Run BFS from `s1`**  
   Use a queue and a distance array `dist1` initialized to `-1`. Set `dist1[s1] = 0`, then expand level by level.  
   **Why correct:** BFS on an unweighted graph visits nodes in nondecreasing hop count, so the first time a node is discovered is its shortest distance from `s1`.  
   **Invariant:** every dequeued node already has its minimum distance fixed.

3. **Run BFS from `s2`**  
   Repeat the same process into `dist2`.  
   **Why correct:** shortest paths from the second source are independent of the first traversal.  
   **Invariant:** `dist2[v]` is either `-1` or the minimum hops from `s2` to `v`.

4. **Scan all nodes to find the best shared dependency**  
   For each node `v`, if `dist1[v] != -1` and `dist2[v] != -1`, compute `score = dist1[v] + dist2[v]`. Track the minimum score seen. If scores tie, prefer the smaller node id.  
   **Why correct:** every valid candidate is evaluated exactly once against the problem’s ordering: first by total distance, then by id.

5. **Return the selected node or `-1`**  
   If no node is reachable from both sources, no candidate was recorded.

This is the **BFS shortest-path pattern on unweighted directed graphs**, followed by a linear reduction. It is the right abstraction because the problem is fundamentally “multi-source comparison of shortest reachable targets,” not path reconstruction or weighted optimization.

## 📊 Worked Example
Use Example 1:

`n=6, edges=[[0,2],[1,2],[2,3],[1,4],[4,3],[0,5]], s1=0, s2=1`

### BFS distances

| Node | dist from 0 | dist from 1 | Shared? | Score |
|---|---:|---:|---|---:|
| 0 | 0 | -1 | No | - |
| 1 | -1 | 0 | No | - |
| 2 | 1 | 1 | Yes | 2 |
| 3 | 2 | 2 | Yes | 4 |
| 4 | -1 | 1 | No | - |
| 5 | 1 | -1 | No | - |

### Trace
1. From `0`: discover `2` and `5`, then `3` through `2`.
2. From `1`: discover `2` and `4`, then `3` through either `2` or `4`.
3. Shared reachable nodes are `2` and `3`.
4. Compare scores:
   - `2`: `1 + 1 = 2`
   - `3`: `2 + 2 = 4`
5. Minimum score is at node `2`, so return `2`.

## ⏱ Complexity Analysis

### Time Complexity
Building the adjacency list is `O(n + m)`, and each BFS is `O(n + m)` because every node and edge is processed at most once. The final scan is `O(n)`. Total time is `O(n + m)`. At `10^6` scale this remains practical; at `10^9`, even linear scans become infrastructure problems rather than algorithm problems.

### Space Complexity
The adjacency list plus two distance arrays and BFS queue require `O(n + m)` space. The graph representation dominates on sparse inputs. You cannot meaningfully reduce this without trading away traversal speed or recomputing edges on demand, which is usually worse.

## 💡 Key Takeaways
- If the graph is unweighted and the question asks for minimum hop count, shortest reachability should immediately trigger BFS, not DFS.
- If the target is “best common node from two sources,” think “compute distances independently, then reduce across nodes.”
- Treat the source nodes themselves as valid candidates; in cyclic graphs, a source may be reachable from the other source and win.
- Use `-1` or another sentinel for unreachable nodes and never sum distances unless both sides are reachable.
- In production dependency systems, bad metadata creates cycles; robust graph code should assume them and still produce deterministic shortest-path results.

## 🚀 Variations & Further Practice
- Return the actual shared dependency path(s), not just the node id. The twist is storing parent pointers while preserving shortest-path semantics from both sources.
- Extend to weighted edges where dependency hops have costs. The conceptual shift is from BFS to Dijkstra from both sources.
- Find the best shared dependency among `k` starting services instead of two. The harder part is aggregating `k` distance vectors efficiently and defining tie-breaking at scale.