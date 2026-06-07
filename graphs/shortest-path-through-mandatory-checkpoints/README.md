# Shortest Path Through Mandatory Checkpoints

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, Dijkstra, Bitmask DP, Shortest Path

---

## 🗂 Problem Overview

Find the minimum-weight path in a weighted undirected graph from `src` to `dst` that visits every node in a mandatory checkpoint set (in any order). Nodes and edges may be revisited. The input/output contract is: given a graph, a source, a destination, and up to 10 checkpoints, return the total weight of the optimal path or `-1` if none exists. The non-trivial constraint is the "all checkpoints in any order" requirement — it breaks standard single-run Dijkstra and demands tracking visited-checkpoint subsets as first-class state.

---

## 🌍 Engineering Impact

This pattern appears directly in logistics and routing systems (last-mile delivery with mandatory waypoints), network topology planning (ensuring traffic passes through specific inspection or policy-enforcement nodes), and game AI pathfinding with required objective pickups. In distributed systems, it models workflows where certain processing stages are mandatory regardless of execution order — think ETL pipelines with required transformation checkpoints. Without bitmask state tracking, naive repeated shortest-path calls miss optimal interleaving routes and produce incorrect results at scale, causing SLA violations or silent data-correctness bugs.

---

## 🔍 Problem Statement

Given a weighted undirected graph of `n` nodes (`0` to `n-1`), `edges[i] = [u, v, w]`, a source `src`, destination `dst`, and a list of `k` mandatory checkpoint nodes, return the minimum total edge weight of any path from `src` to `dst` that passes through all checkpoints. Return `-1` if no such path exists. Nodes may be revisited; the path need not be simple.

**Constraints:** `2 ≤ n ≤ 100`, `1 ≤ edges.length ≤ 500`, `1 ≤ w ≤ 1000`, `0 ≤ k ≤ 10`. The checkpoint count `k ≤ 10` is the critical constraint — it bounds the bitmask state space at `2^10 = 1024`, making exponential-in-k complexity tractable.

**Example 1:** `n=5`, `edges=[[0,1,2],[1,2,3],[2,3,1],[3,4,4],[0,3,10]]`, `src=0`, `dst=4`, `checkpoints=[2]` → Output: `10` (path `0→1→2→3→4`).

**Example 2:** `src=0`, `dst=3`, `checkpoints=[1,2]` → Output: `3` (path `0→1→2→3`).

---

## 🪜 How to Solve This

1. **Observe the "all checkpoints" requirement** → a path's validity depends not just on current position but on *which checkpoints have been visited so far*. Standard Dijkstra's state `(cost, node)` is insufficient.

2. **Checkpoint count is small (k ≤ 10)** → immediately signals bitmask. Each subset of visited checkpoints fits in an integer. This is the classic signal for Bitmask DP.

3. **Reformulate the state space** → instead of nodes, the graph's vertices become `(node, visited_mask)` pairs. There are `n × 2^k` such states — at most `100 × 1024 = 102,400`. Totally manageable.

4. **Apply Dijkstra on the augmented state graph** → transitions are identical to standard Dijkstra (relax neighbors), except when moving to a checkpoint node, OR its index bit into the mask.

5. **Define the goal state** → reach `dst` with `visited_mask == (1 << k) - 1` (all bits set). The first time Dijkstra pops this state, it's the optimal answer — Dijkstra's greedy correctness guarantee holds because all edge weights are positive.

6. **Map checkpoints to bit indices** → precompute a lookup from node ID to its bit position (or `-1` if not a checkpoint) to keep the transition logic O(1).

---

## 🧩 Algorithm Walkthrough

**Pattern: Dijkstra on an Augmented State Graph (Bitmask DP + Shortest Path)**

1. **Preprocessing** — Build an adjacency list from `edges`. Create a `checkpoint_index` map: for each checkpoint node, assign a bit index `0` to `k-1`. This makes mask updates O(1) during traversal.

2. **State definition** — Each state is `(cost, node, mask)` where `mask` is a bitmask of visited checkpoints. Initialize the priority queue with `(0, src, initial_mask)` where `initial_mask` accounts for whether `src` itself is a checkpoint.

3. **Distance table** — Maintain `dist[node][mask]` = minimum cost to reach `node` with exactly `mask` checkpoints visited. Initialize all to infinity. Set `dist[src][initial_mask] = 0`.

4. **Dijkstra relaxation** — Pop the minimum-cost state `(cost, node, mask)`. If `node == dst` and `mask == full_mask`, return `cost` immediately. If `cost > dist[node][mask]`, skip (stale entry). For each neighbor `(next, w)`, compute `next_mask = mask | (1 << checkpoint_index[next])` if `next` is a checkpoint, else `next_mask = mask`. If `cost + w < dist[next][next_mask]`, update and push.

5. **Termination** — If the queue empties without reaching the goal state, return `-1`. The invariant maintained: when a state is popped, its recorded cost is the true shortest path to that `(node, mask)` combination — identical to standard Dijkstra's correctness argument, valid because all weights are positive.

---

## 📊 Worked Example

Using Example 2: `n=4`, `edges=[[0,1,1],[1,2,1],[2,3,1],[0,3,10]]`, `src=0`, `dst=3`, `checkpoints=[1,2]` → `checkpoint_index: {1→bit0, 2→bit1}`, `full_mask = 0b11 = 3`.

| Step | Popped State (cost, node, mask) | Action | dist updates |
|------|----------------------------------|--------|--------------|
| 1 | (0, 0, 0b00) | Expand node 0 | dist[1][01]=1, dist[3][00]=10 |
| 2 | (1, 1, 0b01) | Node 1 is cp bit0 | dist[2][11]=2, dist[0][01]=2 |
| 3 | (2, 2, 0b11) | Node 2 is cp bit1 | dist[1][11]=3, dist[3][11]=3 |
| 4 | (3, 3, 0b11) | node==dst, mask==full | **Return 3** ✓ |

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n · 2^k · log(n · 2^k) + E · 2^k)** — The augmented graph has `n · 2^k` nodes and `E · 2^k` edges. Dijkstra on this graph dominates. With `n=100`, `k=10`, `E=500`, this is roughly `102,400 · 17 ≈ 1.7M` operations — well within real-time constraints. The exponential in `k` is why `k ≤ 10` is a hard practical ceiling.

### Space Complexity

**O(n · 2^k)** — owned entirely by the `dist` table. At `n=100`, `k=10` this is ~100K entries. Reducible only by trading the table for recomputation, which destroys the Dijkstra correctness guarantee and isn't worth it here.

---

## 💡 Key Takeaways

- **Pattern signal — "visit all of a small set in any order"**: whenever k ≤ ~20 items must all be visited and ordering is free, bitmask DP is the canonical tool. The small-k constraint is the unlock.
- **Pattern signal — revisitable nodes with global visit state**: if the problem says "you may revisit nodes" but cares about *what* you've visited globally, the state space must encode that history — standard shortest-path algorithms are insufficient without augmentation.
- **Gotcha — initialize mask for `src`**: if `src` is itself a checkpoint, the initial mask must have its bit set. Missing this produces wrong answers only on edge-case inputs, making it a silent bug.
- **Gotcha — stale priority queue entries**: Dijkstra with a lazy-deletion heap requires the `cost > dist[node][mask]` skip check. Forgetting it doesn't affect correctness but degrades performance to near-exponential in dense graphs.
- **Architectural insight**: this pattern generalizes to any system where progress is measured by a *set of conditions satisfied* rather than a single scalar metric — workflow engines, feature-flag gating pipelines, and distributed saga orchestration all benefit from modeling state as `(position, completion_bitmask)` rather than position alone.

---

## 🚀 Variations & Further Practice

- **Ordered checkpoints (TSP-variant)**: require checkpoints to be visited in a fixed sequence. The bitmask approach still applies, but transitions must additionally enforce ordering — you can only set bit `i` if bit `i-1` is already set. This adds a precedence constraint that forces rethinking which states are valid.
- **Checkpoint with time windows**: each checkpoint must be visited within a specific cost/time range. The state expands to `(node, mask, current_cost)` and the graph becomes a time-expanded network — Dijkstra still works but the state space grows by an order of magnitude, pushing you toward A* or bounded search.
- **Minimize checkpoints visited (inverse problem)**: given a budget constraint, maximize the number of checkpoints reachable. This flips the optimization direction and transforms the problem into a variant of the Prize-Collecting Steiner Tree, requiring a fundamentally different DP formulation.