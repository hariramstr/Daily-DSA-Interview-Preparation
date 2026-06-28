# Minimum Toll to Synchronize Two Rescue Drones

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, Dijkstra, Shortest Path

---

## 🗂 Problem Overview
Given a directed, weighted graph, two drones start at `s1` and `s2`, must meet at some station `r`, and then share a cheapest route from `r` to `t`. The goal is to minimize `dist(s1,r) + dist(s2,r) + dist(r,t)` over all valid rendezvous stations. Return that minimum total cost, or `-1` if no such station exists. The challenge is scale: up to `10^5` nodes and `2 * 10^5` edges makes per-candidate shortest-path recomputation infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere multiple independent flows merge before a shared downstream path: logistics route consolidation, multi-source data replication, packet fan-in before backbone transit, and workflow schedulers that co-locate intermediate artifacts before a final publish step. At scale, the naive design recomputes path cost per merge point and collapses under sparse-but-large graphs. The right abstraction is not “try every meeting point,” but “precompute reusable distance fields.” That shift enables predictable latency, bounded infrastructure cost, and cleaner separation between path computation and decision logic.

## 🔍 Problem Statement
You are given `n` stations and a directed graph `corridors`, where each edge `[u, v, w]` means travel from `u` to `v` costs `w`. Two drones start at `s1` and `s2`. They may travel independently until they meet at some station `r`, after which one drone continues from `r` to destination `t` carrying the combined payload. The total cost is:

`dist(s1, r) + dist(s2, r) + dist(r, t)`

Return the minimum such cost across all stations `r`, or `-1` if no rendezvous station is reachable from both starts and can also reach `t`.

Constraints:
- `1 <= n <= 100000`
- `0 <= corridors.length <= 200000`
- non-negative edge weights up to `10^9`

Examples:
- `n=6, corridors=[[0,2,2],[1,2,3],[2,3,4],[2,4,1],[4,3,1],[3,5,2],[4,5,5]], s1=0, s2=1, t=5` → `9`
- `n=4, corridors=[[0,1,5],[1,3,2],[2,0,1]], s1=0, s2=2, t=3` → `8`

The decisive constraint is graph size: shortest paths must be reused, not recomputed per candidate `r`.

## 🪜 How to Solve This
1. Read the objective carefully → the rendezvous station `r` is the only unknown.
2. Rewrite the total cost as three independent shortest-path terms: from `s1` to `r`, from `s2` to `r`, and from `r` to `t`.
3. That immediately suggests precomputing distance arrays rather than searching per `r`.
4. Distances from `s1` to every node? One Dijkstra.
5. Distances from `s2` to every node? Another Dijkstra.
6. Distances from every node to `t` look awkward on the original graph → reverse all edges, then run Dijkstra from `t`. Now the distance in the reversed graph equals original `dist(node, t)`.
7. Once you have three arrays, every node becomes a constant-time candidate rendezvous check:
   `d1[r] + d2[r] + dt[r]`.
8. Ignore nodes where any component is unreachable.
9. Take the minimum over all nodes.

Why this works: the graph has non-negative weights, so Dijkstra is valid; and the shared suffix after rendezvous is exactly the shortest path from `r` to `t`, independent of how each drone reached `r`.

## 🧩 Algorithm Walkthrough
1. **Build two adjacency lists**: the original graph and its reverse.  
   This is the core **Dijkstra + reverse-graph shortest path** pattern. The original graph supports forward distances from `s1` and `s2`; the reversed graph converts “distance to `t`” into a standard single-source shortest-path query.

2. **Run Dijkstra from `s1` on the original graph** to compute `d1[i] = dist(s1, i)`.  
   Invariant: when a node is popped with minimal tentative distance, `d1[node]` is finalized because all edge weights are non-negative.

3. **Run Dijkstra from `s2` on the original graph** to compute `d2[i] = dist(s2, i)`.  
   Same invariant. This independently captures the cheapest pre-meeting cost for the second drone.

4. **Run Dijkstra from `t` on the reversed graph** to compute `dt[i] = dist(i, t)` in the original graph.  
   Why correct: a path `i -> ... -> t` in the original graph becomes `t -> ... -> i` in the reversed graph with identical total weight.

5. **Scan all nodes `r` from `0` to `n-1`**.  
   If any of `d1[r]`, `d2[r]`, or `dt[r]` is infinite, `r` is not a valid rendezvous point. Otherwise compute `d1[r] + d2[r] + dt[r]` and minimize.

6. **Return the minimum found, else `-1`**.  
   The correctness argument is complete because every feasible solution must choose some rendezvous node `r`, and for that fixed `r`, the optimal cost decomposes exactly into those three shortest-path terms.

## 📊 Worked Example
Use Example 2:

`n=4, corridors=[[0,1,5],[1,3,2],[2,0,1]], s1=0, s2=2, t=3`

| Node `r` | `dist(0,r)` | `dist(2,r)` | `dist(r,3)` | Total |
|---|---:|---:|---:|---:|
| 0 | 0 | 1 | 7 | 8 |
| 1 | 5 | 6 | 2 | 13 |
| 2 | ∞ | 0 | 8 | invalid |
| 3 | 7 | 8 | 0 | 15 |

Trace:
1. Dijkstra from `0` gives `[0,5,∞,7]`.
2. Dijkstra from `2` gives `[1,6,0,8]`.
3. Reverse edges: `1->0(5), 3->1(2), 0->2(1)`.
4. Dijkstra from `3` on reversed graph gives `[7,2,8,0]`, which means original distances to `3`.
5. Scan all nodes. The minimum valid total is at `r=0`: `0 + 1 + 7 = 8`.

## ⏱ Complexity Analysis
### Time Complexity
Building adjacency lists is `O(n + m)`. Each Dijkstra run is `O((n + m) log n)` with a binary heap, and we run it three times, so total time is `O((n + m) log n)`. On sparse million-edge graphs this is practical; recomputing per rendezvous node would explode toward quadratic-scale work.

### Space Complexity
`O(n + m)` space. The adjacency lists dominate edge storage, and the three distance arrays plus heap overhead add linear node cost. You could reduce constants by reusing buffers between runs, but not the asymptotic footprint without sacrificing clarity or recomputation efficiency.

## 💡 Key Takeaways
- If the objective is “choose a merge point `r`” and the cost decomposes into source→`r` plus `r`→target terms, think precomputed distance fields, not per-candidate search.
- If you need shortest distance from every node *to* one target in a directed graph, reverse the edges and run one single-source shortest-path from the target.
- Use 64-bit integers for distances and totals; edge weights up to `10^9` across long paths will overflow 32-bit types.
- Treat unreachable nodes explicitly with an `INF` sentinel and skip any rendezvous candidate where one of the three distances is missing.
- The production lesson is to transform repeated online path queries into a small number of reusable graph passes, then make the final decision in a linear scan.

## 🚀 Variations & Further Practice
- **Allow negative edge weights but no negative cycles**: Dijkstra no longer applies; the conceptual twist is replacing heap-based SSSP with Bellman-Ford or Johnson’s algorithm.
- **Require the two pre-meeting paths to be edge- or node-disjoint**: the problem stops being a simple distance decomposition and moves toward min-cost flow / disjoint paths.
- **Support many `(s1, s2, t)` queries on a mostly static graph**: the twist is preprocessing versus query-time trade-offs, pushing toward indexing, contraction hierarchies, or multi-level routing structures.