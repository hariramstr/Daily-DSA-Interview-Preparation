# Count Reachable Nodes from Each Capital

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** BFS, DFS, Tree, Graph Traversal

---

## 🗂 Problem Overview

Given an undirected tree of `n` cities where city `0` is the national capital, and a subset of cities are marked as regional capitals, determine how many cities each regional capital can reach without crossing another capital boundary. The tree structure guarantees no cycles, but the blocking constraint — traversal halts at any capital node — transforms a trivial reachability problem into a partitioned-component problem where boundaries are defined by node labels, not edge weights.

---

## 🌍 Engineering Impact

This pattern appears directly in **hierarchical network segmentation**: determining blast radius within a fault domain without crossing zone boundaries, computing ownership regions in org-tree-based access control systems, or partitioning a service mesh topology for traffic isolation. In distributed systems, the same traversal-with-boundary logic governs **region-scoped service discovery** — a node queries its local registry without propagating past a regional gateway. Without correct boundary enforcement, you either over-count (leaking cross-region state) or under-count (missing valid local nodes), both of which produce incorrect routing tables or misconfigured rate-limit scopes at scale.

---

## 🔍 Problem Statement

**Input:**
- `n` — number of cities (`1 <= n <= 1000`)
- `edges` — `n - 1` bidirectional edges forming a valid tree
- `capitals` — binary array of length `n`; `capitals[i] = 1` marks city `i` as a regional capital; `capitals[0]` is always `0`

**Output:** Array `result` where `result[i]` is the count of cities reachable from the `i`-th regional capital without traversing through any other regional capital or city `0`.

**Examples:**

| Input | Output |
|---|---|
| `n=6`, `edges=[[0,1],[1,2],[1,3],[3,4],[3,5]]`, `capitals=[0,1,0,1,0,0]` | `[2, 3]` |
| `n=4`, `edges=[[0,1],[1,2],[2,3]]`, `capitals=[0,1,0,0]` | `[3]` |

**Critical constraint:** A regional capital itself is included in its own count, but it acts as a hard boundary for all *other* capitals' traversals. City `0` is always a boundary and never a regional capital.

---

## 🪜 How to Solve This

1. **Read the problem → notice the word "reachable without passing through."** This is not a shortest-path or weight problem — it's a constrained connectivity problem. The tree structure means there is exactly one path between any two nodes, so "blocked" paths are unambiguous.

2. **Blocked traversal on a tree → think BFS/DFS with a visited/blocked set.** For each regional capital, start a traversal and refuse to expand into any node that is itself a capital (national or regional). The capital node you *start from* is included; capitals you *encounter* are not expanded.

3. **Identify the boundary condition precisely:** city `0` and all `capitals[i] = 1` nodes are barriers — but only when encountered as *neighbors*, not as the source. This asymmetry is the main implementation trap.

4. **Scale check:** `n <= 1000` means an O(n) BFS per capital (up to n capitals) gives O(n²) worst case — roughly 10⁶ operations, well within budget. No need for more complex partitioning algorithms.

5. **Output ordering:** results must follow the order regional capitals appear in the `capitals` array, so collect them during a single pass over `capitals`.

---

## 🧩 Algorithm Walkthrough

**Pattern: BFS/DFS with boundary-aware expansion on a tree.**

1. **Build the adjacency list.** Iterate over `edges`, adding both directions. This is standard undirected graph construction — O(n).

2. **Identify all boundary nodes.** A node is a boundary if `capitals[i] = 1` OR `i == 0`. Store this in a set or boolean array for O(1) lookup during traversal.

3. **For each regional capital `c` (where `capitals[c] = 1`):**
   - Initialize a BFS queue with `c`. Mark `c` as visited. Set `count = 0`.
   - While the queue is non-empty: dequeue node `u`, increment `count`.
   - For each neighbor `v` of `u`: if `v` is not visited AND `v` is not a boundary node, enqueue `v` and mark visited.
   - Append `count` to `result`.

4. **Invariant maintained:** At every BFS step, the frontier never crosses a capital boundary. The source capital is always counted (it seeds the queue). Neighbors that are capitals are recognized as walls — they stop expansion but are not counted in the current capital's region.

5. **Why BFS over DFS here?** Both work correctly on a tree. BFS is slightly more predictable for level-order reasoning and avoids stack overflow risk on deep linear trees (e.g., a path graph with n=1000). Either is valid; the choice is stylistic at this scale.

---

## 📊 Worked Example

**Input:** `n=6`, `edges=[[0,1],[1,2],[1,3],[3,4],[3,5]]`, `capitals=[0,1,0,1,0,0]`

**Boundary nodes:** `{0, 1, 3}`

**BFS from city 1:**

| Step | Queue | Visited | Count |
|------|-------|---------|-------|
| Init | [1] | {1} | 0 |
| Dequeue 1 | [] | {1} | 1 |
| Expand neighbors of 1: 0→boundary, 2→enqueue, 3→boundary | [2] | {1,2} | 1 |
| Dequeue 2 | [] | {1,2} | 2 |
| Expand neighbors of 2: 1→visited | [] | {1,2} | 2 |

**Result for city 1:** `2`

**BFS from city 3:** Neighbors 1 (boundary), 4, 5 → count = `3`

**Final output:** `[2, 3]` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n²)** worst case — each of the up to `n` regional capitals triggers a BFS that visits up to `n` nodes. At `n = 1000`, this is ~10⁶ operations: trivial. At `n = 10⁶`, this becomes 10¹² — you'd need a single-pass partition algorithm instead. For the given constraints, O(n²) is the correct and sufficient choice.

### Space Complexity

**O(n)** — the adjacency list owns O(n) space (n−1 edges, two directions), and each BFS uses an O(n) visited array. The visited array is re-initialized per BFS; no persistent auxiliary structure accumulates. Reducible only by using a bitset for visited, trading code clarity for a constant-factor memory win.

---

## 💡 Key Takeaways

- **Pattern signal #1:** "Reachable without passing through" on a tree is always a boundary-constrained BFS/DFS — the moment you see traversal with stopping conditions defined by node properties, reach for this template.
- **Pattern signal #2:** When the graph is guaranteed to be a tree (n−1 edges, connected, no cycles), you don't need cycle detection or complex visited logic — a simple visited set per traversal is sufficient and correct.
- **Implementation gotcha:** The source capital is *included* in its own count but acts as a *wall* for other capitals' traversals. These are two different roles for the same node type — conflating them produces off-by-one errors in both directions.
- **Implementation gotcha:** City `0` is a boundary but never a regional capital — it must be in your boundary set but must never seed a BFS. Forgetting to include it means traversals from city `1` (adjacent to `0`) will bleed into the national capital's subtree.
- **Architectural insight:** This boundary-aware traversal is the graph-theoretic equivalent of a firewall rule: define a set of nodes as zone boundaries, then compute reachability within zones. The same abstraction applies to network topology analysis, IAM policy propagation trees, and hierarchical config inheritance — wherever "local scope" is defined by blocking nodes rather than blocking edges.

---

## 🚀 Variations & Further Practice

- **Weighted reachability:** Assign a cost to each edge and ask for the total reachable *cost* (or population) within each capital's zone, subject to a budget cap. The twist: BFS becomes Dijkstra, and the stopping condition is now a distance threshold rather than a node-type boundary.
- **Dynamic capital assignment:** Support online queries where a city can be promoted to or demoted from regional capital status, requiring efficient re-computation of affected zones. The twist: naive re-BFS from scratch is O(n²) per update — the interesting problem is maintaining a partition data structure (e.g., link-cut trees) that supports O(log n) zone splits and merges.
- **LeetCode 2467 – Most Profitable Path in a Tree** and **LeetCode 1519 – Number of Nodes in the Sub-Tree With the Same Label** extend the same tree-traversal-with-accumulation pattern into subtree aggregation problems where the "boundary" is implicit in the tree structure rather than explicitly labeled.