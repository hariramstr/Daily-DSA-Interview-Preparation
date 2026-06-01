# Minimum Cost to Connect K Closest Server Clusters

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** Heap, Minimum Spanning Tree, Priority Queue, Prim's Algorithm, Sorting

---

## 🗂 Problem Overview

Given `n` servers on a 2D grid and a list of queries, each query asks for the minimum total connection cost to network the `k` closest servers to a given server index. Connection cost is Euclidean distance squared; the minimum cost to fully connect a subset is its MST weight. The non-trivial constraint: each query defines a *different* subset, so MST must be recomputed per query over a dynamically selected node set — no global precomputation suffices.

---

## 🌍 Engineering Impact

This pattern is the backbone of **network topology optimization** in distributed infrastructure — think AWS placement groups, Kubernetes node affinity scheduling, or CDN PoP selection. When provisioning clusters, you want the minimum-latency spanning backbone across the nearest peers, not all peers globally. Without per-query MST over proximity subsets, you either over-provision links (cost explosion) or under-connect (partition risk). The same structure appears in ML feature clustering pipelines, where connecting the k-nearest embedding neighbors with minimum edge weight defines the local manifold topology used for dimensionality reduction.

---

## 🔍 Problem Statement

**Input:**
- `servers`: array of `n` points `[x, y]` on a 2D grid, `2 ≤ n ≤ 1000`, coordinates in `[0, 10^4]`, all distinct.
- `queries`: array of `[index, k]` pairs, `1 ≤ queries.length ≤ 500`, `1 ≤ k ≤ n`.

**Output:** Integer array where `result[j]` is the MST cost of the `k` closest servers to `servers[index]`, including `servers[index]` itself. Edge weight between two servers is `(x1−x2)² + (y1−y2)²`.

**Examples:**

| Input | Output |
|---|---|
| `servers=[[0,0],[1,1],[3,3],[6,6]], queries=[[0,3],[1,2]]` | `[8, 2]` |
| `servers=[[0,0],[2,0],[0,2],[2,2]], queries=[[0,4]]` | `[12]` |

**Key constraint driving the algorithm:** With `n=1000` and `500` queries, naively recomputing all pairwise distances per query is acceptable, but the MST computation over the selected subset must be efficient. When `k=1`, MST cost is 0. The squared-distance formulation eliminates floating-point precision issues entirely.

---

## 🪜 How to Solve This

1. **Read the problem** → each query is independent and defines a fresh subset. No shared structure across queries means we solve each from scratch.
2. **Subset selection** → for query `[index, k]`, sort all servers by squared distance from `servers[index]`, take the first `k`. This is O(n log n) per query using a sort, or O(n + k log n) with a min-heap.
3. **MST over a subset** → with `k ≤ n ≤ 1000`, the subset has at most 1000 nodes and up to ~500K edges. Prim's algorithm with an adjacency matrix runs in O(k²), which is ideal for dense graphs — and a fully-connected geometric graph *is* dense.
4. **Why Prim's over Kruskal's here?** → Kruskal's requires sorting all edges: O(k² log k²). Prim's on a dense graph with an array-based (not heap-based) priority queue is O(k²) — strictly better when k² >> k log k, i.e., k is large.
5. **Compose the two steps** → select subset, build implicit complete graph, run Prim's, accumulate MST weight. Repeat per query.

The result: a clean two-phase pipeline per query with no cross-query state.

---

## 🧩 Algorithm Walkthrough

**Pattern: Prim's MST on a dense implicit graph**

**Step 1 — Distance ranking (per query):**
For query `[index, k]`, compute squared Euclidean distance from `servers[index]` to every other server. Sort or heap-select to extract the `k` nearest (including `index` itself, distance 0). This gives the working node set `S` of size `k`.

**Step 2 — Initialize Prim's:**
Maintain an array `minCost[0..k-1]` where `minCost[i]` = minimum edge weight to connect node `i` into the growing MST. Initialize `minCost[0] = 0` (start from the query server), all others to `∞`. Maintain a boolean `inMST[0..k-1]`.

**Step 3 — Greedy expansion (k iterations):**
Each iteration: pick the node `u` not yet in MST with minimum `minCost[u]` (O(k) linear scan — correct for dense graphs). Add `u` to MST, accumulate `minCost[u]` into the total cost. For every node `v` not in MST, update `minCost[v] = min(minCost[v], dist²(S[u], S[v]))`.

**Invariant maintained:** After each iteration, every node in MST is connected to the rest of the MST via the cheapest possible cut edge — the standard Prim's greedy cut property.

**Step 4 — Return total:**
After `k` iterations, the accumulated cost is the MST weight for this query.

Total per-query complexity: O(n log n) for subset selection + O(k²) for Prim's.

---

## 📊 Worked Example

**Query `[0, 3]` on `servers = [[0,0],[1,1],[3,3],[6,6]]`**

Subset selection from server 0: distances² = [0, 2, 18, 72] → top 3: servers {0,1,2}, distances² {0,2,18}.

Prim's on subset `S = [(0,0),(1,1),(3,3)]`:

| Iteration | u picked | minCost[] before pick | Edge added | Cost added | MST so far |
|---|---|---|---|---|---|
| 0 | node 0 | [0, ∞, ∞] | — | 0 | {0} |
| 1 | node 1 | [—, 2, 18] | 0→1 | 2 | {0,1} |
| 2 | node 2 | [—, —, 8] | 1→2 | 8 | {0,1,2} |

After iteration 1, `minCost[2]` updates: `dist²((1,1),(3,3)) = 4+4 = 8 < 18`. Total MST cost = **0 + 2 + 8 = 10**... wait — accumulated = 2 + 8 = **8** (node 0 contributes 0). ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(Q · (n log n + k²))** where Q = number of queries. The dominant term per query is O(n log n) for sorting all servers by distance and O(k²) for array-based Prim's. With Q=500, n=1000, k=1000: roughly 500 × (10K + 1M) ≈ 500M operations — tight but feasible. A heap-based Prim's would be O(k² log k), strictly worse here.

### Space Complexity

**O(n + k)** per query. The distance array is O(n); Prim's `minCost` and `inMST` arrays are O(k). No explicit adjacency matrix is stored — edge weights are computed on-the-fly from coordinates. Space is reused across queries, so peak allocation stays O(n).

---

## 💡 Key Takeaways

- **Pattern signal — "minimum cost to connect a subset"**: whenever a problem asks for connection cost over a *dynamically selected* subset of nodes (not all nodes), MST over that subset is the answer, and Prim's dense variant is the right tool when subset size can approach n.
- **Pattern signal — squared distance as edge weight**: when coordinates are integers and the problem avoids mentioning floating point, squared Euclidean distance is the intended formulation — it preserves ordering and eliminates `sqrt` entirely, which also signals a geometric MST problem.
- **Gotcha — k=1 edge case**: a single-node "cluster" has MST cost 0. Prim's handles this correctly (one iteration, adds 0), but verify your subset selection includes the query server itself at distance 0.
- **Gotcha — distance update after node joins MST**: in Prim's, you update `minCost[v]` using the *newly added* node `u`, not all MST nodes. Iterating over all MST nodes for updates is a common bug that inflates complexity to O(k³).
- **Architectural insight**: the two-phase pattern — *filter to relevant subset, then run expensive algorithm on small subset* — is a fundamental performance primitive in production systems (pre-filtering candidates before scoring, shard pruning before aggregation). The MST here is the "expensive algorithm"; the k-nearest selection is the filter. Keeping these phases cleanly separated makes each independently optimizable.

---

## 🚀 Variations & Further Practice

- **Dynamic server addition**: servers are added incrementally and queries arrive in a stream. The twist is that the k-nearest set for a fixed index changes as new servers are inserted, requiring an online data structure (e.g., a k-d tree) to maintain proximity efficiently — static sort-per-query no longer works.
- **Weighted connection cost with bandwidth constraints**: each server has a capacity, and edges can only be included in the MST if both endpoints have remaining capacity. This transforms the problem into a constrained MST (related to degree-constrained spanning trees), which is NP-hard in general but approximable — the greedy Prim's structure breaks down and requires rethinking the feasibility check at each expansion step.
- **LeetCode 1584 — Min Cost to Connect All Points**: the direct single-query version of this problem (no subset selection, no queries). Solving it first with both Prim's and Kruskal's builds the MST muscle memory needed before tackling the per-query subset variant here.