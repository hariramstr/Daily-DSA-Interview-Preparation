# Last Safe Merge Before Road Closures

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** graphs, shortest-path, bfs

---

## 🗂 Problem Overview
Given a directed graph where each edge can only be entered before its closing time, compute the intersection where two drivers starting from `s1` and `s2` can meet as early as possible. Waiting at nodes is allowed, edge traversal always costs 1 minute, and the meeting time at node `x` is `max(t1[x], t2[x])`. The challenge is that reachability depends on arrival time: a path with fewer hops is not automatically feasible once edge deadlines are introduced.

## 🌍 Engineering Impact
This pattern shows up in deadline-constrained routing and coordination systems: package dispatch over expiring network windows, streaming pipelines with watermark cutoffs, distributed schedulers placing work before maintenance windows, and transportation or fleet systems with time-gated links. At scale, naive reachability or plain shortest-path logic fails because temporal feasibility is path-dependent. You need earliest-arrival computation under edge deadlines, then a merge criterion across multiple sources. The payoff is predictable coordination under decaying availability: finding the last safe join point, fallback node, or synchronization boundary before downstream options disappear.

## 🔍 Problem Statement
You are given `n` intersections (`0..n-1`) and `m` directed roads. Each road is `[u, v, closeTime]`, meaning it may be entered only at times strictly less than `closeTime`; traversing it always takes exactly 1 minute. Two drivers start at time `0` from distinct intersections `s1` and `s2`. They may wait at any intersection indefinitely.

For each node `x`, if both drivers can reach it while respecting road closures, they can meet there at time `max(dist1[x], dist2[x])`, since the earlier driver may wait. Return the intersection with minimum such meeting time; break ties by smallest index. Return `-1` if no common reachable node exists.

Constraints: `2 <= n <= 1e5`, `0 <= m <= 2e5`, `closeTime <= 1e9`. Multiple edges and self-loops are allowed.

Examples:
- `n=5, roads=[[0,2,3],[1,2,2],[2,3,5],[1,4,10],[4,3,10]], s1=0, s2=1` → `2`
- `n=4, roads=[[0,2,1],[2,3,2],[1,3,1]], s1=0, s2=1` → `-1`

## 🪜 How to Solve This
1. Read the problem → the meeting node is not about simultaneous arrival; waiting is allowed. So for each node, only the **earliest feasible arrival time** from each source matters.

2. Notice the edge rule: from node `u` reached at time `t`, edge `(u, v, close)` is usable iff `t < close`. Since traversals all cost 1, earlier arrival is always at least as good as later arrival.

3. That monotonic property suggests a shortest-path formulation. We are not optimizing arbitrary weights; every move costs 1, but edge availability depends on current time.

4. Compute earliest feasible arrival times from `s1` to all nodes, then again from `s2`. A BFS-style traversal works because each successful edge relaxation increases time by exactly 1.

5. Once both distance arrays are known, scan all nodes. If both are reachable, the meeting time is `max(d1[i], d2[i])`.

6. Pick the node with minimum meeting time; if several tie, choose the smallest index.

The key insight: solve two constrained earliest-arrival problems first, then reduce the merge decision to a linear scan.

## 🧩 Algorithm Walkthrough
1. **Build the adjacency list**  
   Store outgoing edges for each node as `(neighbor, closeTime)`. This preserves the directed structure and supports linear traversal over all roads.

2. **Run constrained BFS from `s1`**  
   Use a queue and an array `dist1`, initialized to `INF`, with `dist1[s1] = 0`.  
   When processing node `u` at time `t = dist1[u]`, examine each outgoing edge `(u, v, close)`. The edge is feasible only if `t < close`. If feasible and `t + 1 < dist1[v]`, set `dist1[v] = t + 1` and enqueue `v`.  
   **Invariant:** `dist1[x]` is always the smallest feasible arrival time discovered so far.

3. **Run the same constrained BFS from `s2`**  
   Produce `dist2` with the same logic.  
   This is the same graph pattern: **unweighted shortest path / BFS**, with an extra edge-feasibility predicate based on current layer.

4. **Evaluate candidate meeting nodes**  
   For every node `i`, if both `dist1[i]` and `dist2[i]` are finite, compute `meet = max(dist1[i], dist2[i])`. Waiting makes this correct: the earlier driver can remain at `i` until the later one arrives.

5. **Apply tie-breaking**  
   Track the smallest `meet`; on equal meeting time, prefer smaller node index.  
   **Invariant:** after scanning prefix `0..i`, the stored answer is the best valid meeting point among those nodes.

6. **Return result**  
   If no node is reachable from both starts, return `-1`.

Why BFS is the right abstraction: every traversable edge has uniform travel cost `1`, so earliest arrival is layered by time. Deadlines only filter edges; they do not invalidate BFS’s level-order optimality.

## 📊 Worked Example
Use Example 1:

`n=5`  
`roads=[[0,2,3],[1,2,2],[2,3,5],[1,4,10],[4,3,10]]`  
`s1=0, s2=1`

| Step | From `s1` updates | `dist1` | From `s2` updates | `dist2` |
|---|---|---|---|---|
| Init | start at `0` | `[0,∞,∞,∞,∞]` | start at `1` | `[∞,0,∞,∞,∞]` |
| 1 | `0->2` allowed (`0<3`) | `[0,∞,1,∞,∞]` | `1->2` allowed (`0<2`), `1->4` allowed (`0<10`) | `[∞,0,1,∞,1]` |
| 2 | `2->3` allowed (`1<5`) | `[0,∞,1,2,∞]` | `2->3` allowed (`1<5`) | `[∞,0,1,2,1]` |
| 3 | done | — | `4->3` gives time `2`, no improvement | — |

Common reachable nodes: `2` and `3`.  
Meeting times:
- Node `2`: `max(1,1)=1`
- Node `3`: `max(2,2)=2`

Earliest meeting is at node `2`, so return `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)` per BFS, so `O(n + m)` overall up to a constant factor of two, plus `O(n)` for the final scan. The dominant cost is traversing each edge during each source pass. At `10^6` scale this is routine; at `10^9`, graph materialization and traversal are no longer practical in memory or latency budgets.

### Space Complexity
`O(n + m)`. The adjacency list owns `O(m)` space, and the two distance arrays plus BFS queue use `O(n)`. You could reduce one distance array by computing and consuming results in stages, but that complicates tie-handling and usually is not worth the loss in clarity.

## 💡 Key Takeaways
- If edges are unit-cost but only usable under time-dependent predicates, think “BFS with feasibility checks,” not generic Dijkstra by default.
- If two agents may wait after arrival, the merge objective usually becomes minimizing `max(arrivalA[x], arrivalB[x])` over common reachable states.
- The closure rule is strict: an edge with `closeTime = t` is usable only when `currentTime < t`, not `<= t`.
- Do not require equal arrival times at the meeting node; waiting is explicitly allowed, so comparing raw distances directly is wrong.
- In production routing and scheduling systems, separating “compute earliest feasible state per source” from “choose best rendezvous/merge point” keeps the design composable and scalable.

## 🚀 Variations & Further Practice
- Add heterogeneous travel times per edge. The conceptual twist is that BFS no longer works; you need Dijkstra with deadline-aware relaxations.
- Require the drivers to meet **simultaneously without waiting**. The twist is that feasibility becomes an equality constraint on arrival times, often pushing the problem toward product-state search.
- Allow roads to reopen in intervals instead of a single closing deadline. The twist is periodic or interval-constrained shortest path, where edge availability is no longer monotonic.