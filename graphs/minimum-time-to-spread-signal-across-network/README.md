# Minimum Time to Spread Signal Across Network

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Dijkstra, Shortest Path, Priority Queue, BFS

---

## 🗂 Problem Overview

Given a directed, weighted graph of `n` nodes and a set of source nodes that simultaneously emit a signal at time `0`, find the minimum time for every node to receive the signal. A node receives the signal at the earliest arrival time from any source. The answer is the maximum of those earliest arrival times across all nodes. The non-trivial constraint: multiple sources and directed edges mean naive single-source shortest path is insufficient without a clean multi-source reduction.

---

## 🌍 Engineering Impact

This pattern is the backbone of **network propagation latency analysis** — think CDN cache invalidation (how long until every edge node is stale-free?), distributed database replication lag (when has a write reached all replicas?), and epidemic/failure-blast-radius modeling in service meshes. Without multi-source shortest path, you either over-provision replication timeouts or under-detect propagation failures. The reduction to a virtual source node is the same trick used in systems like Kafka's ISR tracking and BGP convergence time estimation.

---

## 🔍 Problem Statement

**Input:**
- `n` — number of nodes, labeled `1` to `n`
- `edges` — list of directed weighted edges `[u, v, w]`
- `sources` — list of nodes that emit the signal at time `0`

**Output:** Minimum time for all `n` nodes to receive the signal, or `-1` if any node is unreachable.

**Examples:**

| n | edges | sources | Output |
|---|-------|---------|--------|
| 4 | `[[1,2,1],[1,3,4],[2,3,2],[3,4,1]]` | `[1]` | `4` |
| 4 | `[[1,2,1],[3,4,1]]` | `[1,3]` | `1` |

**Key constraints driving algorithmic choice:** Up to `6000` edges with positive weights and multiple sources. Positive weights → Dijkstra is optimal. Multiple sources → virtual-source reduction or simultaneous multi-source initialization. Directed edges mean reachability is not symmetric; isolated nodes must be detected explicitly.

---

## 🪜 How to Solve This

1. **Multiple sources emitting simultaneously** → this is multi-source shortest path, not single-source. The signal arrival at any node is `min` over all sources of the shortest path from that source.

2. **Positive edge weights** → Dijkstra is the right tool. BFS would work only on unweighted graphs; Bellman-Ford works but is slower than necessary here.

3. **Multi-source Dijkstra reduction** → instead of running Dijkstra `|sources|` times and merging, introduce a **virtual source node `0`** connected to every source node with edge weight `0`. Now run single-source Dijkstra from node `0`. The zero-weight edges model "signal is already at all sources at time 0" exactly.

4. **Answer extraction** → after Dijkstra, `dist[v]` holds the earliest signal arrival at node `v`. The answer is `max(dist[1..n])`. If any `dist[v]` remains `∞`, return `-1` — the graph is disconnected from all sources.

5. **Why this is obviously correct:** the virtual source trick preserves all shortest-path invariants because zero-weight edges don't violate Dijkstra's non-negative weight requirement.

---

## 🧩 Algorithm Walkthrough

**Pattern: Multi-Source Dijkstra via Virtual Node**

1. **Build adjacency list** from `edges`. Index nodes `1..n`; reserve node `0` as the virtual source.

2. **Add virtual edges:** for each node `s` in `sources`, add edge `(0 → s, weight=0)` to the adjacency list.

3. **Initialize dist array:** `dist[0] = 0`, `dist[1..n] = ∞`. Push `(0, 0)` — `(cost, node)` — onto a min-heap.

4. **Dijkstra main loop:** pop the minimum-cost entry `(d, u)` from the heap. If `d > dist[u]`, skip (stale entry). For each neighbor `v` of `u` with edge weight `w`: if `dist[u] + w < dist[v]`, update `dist[v] = dist[u] + w` and push `(dist[v], v)` onto the heap.

   **Invariant maintained:** when a node is popped from the heap, its `dist` value is finalized — guaranteed by non-negative weights.

5. **Compute answer:** iterate `dist[1..n]`. If any value is `∞`, return `-1`. Otherwise return `max(dist[1..n])`.

6. **Why max?** The signal is useful only when the *last* node receives it. The bottleneck node determines total propagation time.

---

## 📊 Worked Example

**Input:** `n=4`, `edges=[[1,2,1],[1,3,4],[2,3,2],[3,4,1]]`, `sources=[1]`

Virtual edge added: `0→1, w=0`.

| Step | Popped (cost, node) | dist[0] | dist[1] | dist[2] | dist[3] | dist[4] | Heap (cost, node) |
|------|--------------------|---------|---------|---------|---------|---------|--------------------|
| Init | —                  | 0       | ∞       | ∞       | ∞       | ∞       | `(0,0)`            |
| 1    | `(0, 0)`           | 0       | **0**   | ∞       | ∞       | ∞       | `(0,1)`            |
| 2    | `(0, 1)`           | 0       | 0       | **1**   | **4**   | ∞       | `(1,2),(4,3)`      |
| 3    | `(1, 2)`           | 0       | 0       | 1       | **3**   | ∞       | `(3,3),(4,3)`      |
| 4    | `(3, 3)`           | 0       | 0       | 1       | 3       | **4**   | `(4,3),(4,4)`      |
| 5    | `(4, 3)`           | —       | —       | —       | —       | —       | Stale, skip        |
| 6    | `(4, 4)`           | —       | —       | —       | —       | —       | Finalized          |

**Result:** `max(dist[1..4]) = max(0,1,3,4) = 4` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O((V + E) log V)** where `V = n+1` (including virtual node) and `E = |edges| + |sources|`. The dominant cost is heap operations — each edge relaxation is a potential push, each pop is `O(log V)`. At `n=100` and `E=6000` this is trivially fast; the pattern scales to `10^6` nodes in production routing tables without structural change.

### Space Complexity

**O(V + E)** for the adjacency list plus `O(V)` for the dist array and up to `O(E)` heap entries in the worst case. No meaningful reduction is possible without sacrificing the adjacency list; the dist array is irreducible.

---

## 💡 Key Takeaways

- **Pattern signal — multiple simultaneous sources with "earliest arrival" semantics:** whenever the problem says signals/infections/writes start from a set of nodes at time 0, reach for multi-source Dijkstra, not repeated single-source runs.
- **Pattern signal — "minimum of maximums" answer shape:** if the output is the last node to be reached (max over all shortest paths), you're solving a propagation coverage problem, not a point-to-point routing problem.
- **Gotcha — stale heap entries:** Dijkstra with a lazy-deletion heap will push duplicate entries. Always guard with `if d > dist[u]: continue` before processing neighbors, or you'll re-relax already-finalized nodes and corrupt results.
- **Gotcha — 1-indexed nodes:** the virtual source occupies index `0`; allocate `dist` of size `n+1` and answer from `dist[1..n]` only. Off-by-one here produces wrong answers silently, not crashes.
- **Architectural insight:** the virtual-source reduction is a general technique — any "fleet initialization" or "multi-origin propagation" problem (cache warming, config rollout, epidemic modeling) maps cleanly to it. Encoding the initial state as zero-cost edges from a synthetic origin keeps the core algorithm unchanged and the code auditable.

---

## 🚀 Variations & Further Practice

- **Network Delay Time (LeetCode 743):** single-source variant of this exact problem — a useful baseline to verify your Dijkstra implementation before adding the multi-source layer.
- **Path with Maximum Probability (LeetCode 1514):** replaces additive costs with multiplicative probabilities and flips the optimization direction (maximize instead of minimize). Requires switching to a max-heap and changes the relaxation condition — tests whether you understand Dijkstra's invariant deeply enough to adapt it.
- **Minimum Cost to Reach Every Node with Constrained Hops (conceptual variant):** add a maximum hop count `k`, turning this into a resource-constrained shortest path problem. Standard Dijkstra no longer applies; the state space expands to `(node, hops_remaining)`, and the problem becomes NP-hard in the general case — a natural bridge to dynamic programming on DAGs or Bellman-Ford with iteration limits.