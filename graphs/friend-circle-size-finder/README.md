# Friend Circle Size Finder

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Union-Find, BFS, Connected Components

---

## 🗂 Problem Overview

Given `n` users and a list of direct friendship pairs, find the sizes of all **transitively closed** friend groups — connected components in an undirected graph — and return them sorted descending. The non-trivial constraint is transitivity: two users with no direct edge can still belong to the same circle through intermediaries, so naive pairwise comparison fails and you need a structure that tracks group membership across merges.

---

## 🌍 Engineering Impact

This pattern is the backbone of any system that must track **dynamic equivalence classes** at scale. Fraud detection systems merge accounts sharing devices or payment methods into clusters to catch coordinated abuse. Distributed databases use connected-component logic to determine replica group membership after network partitions. Social platforms (LinkedIn's "People You May Know", Meta's friend suggestions) compute these clusters offline at billion-node scale using distributed Union-Find variants. Without efficient component tracking, these pipelines degrade from near-linear to quadratic, making real-time or near-real-time decisions infeasible.

---

## 🔍 Problem Statement

**Input:** An integer `n` (users `0` to `n-1`) and a list `connections` where `connections[i] = [a, b]` denotes a bidirectional friendship.

**Output:** A list of component sizes sorted in descending order.

**Constraints:**
- `1 <= n <= 1000`, `0 <= connections.length <= 5000`
- No self-loops, no duplicate edges
- Isolated nodes (no edges) still count as circles of size 1

**Examples:**

| `n` | `connections` | Output |
|-----|--------------|--------|
| 6 | `[[0,1],[1,2],[3,4]]` | `[3, 2, 1]` |
| 4 | `[[0,1],[2,3],[1,3]]` | `[4]` |

The key driver: transitivity forces you to propagate group membership across an arbitrary chain of edges — the algorithmic choice follows directly from that requirement.

---

## 🪜 How to Solve This

1. **Read the problem** → you need to return group *sizes*, not the groups themselves. The output is aggregate, not structural.
2. **Transitivity means equivalence relation** → if A~B and B~C then A~C. Any data structure for equivalence classes is a candidate: Union-Find, BFS/DFS flood-fill, or adjacency matrix closure.
3. **Union-Find vs. BFS** → BFS works but requires building an explicit adjacency list first. Union-Find processes edges directly in one pass and tracks component sizes natively — no adjacency list needed, simpler bookkeeping.
4. **Union-Find with size tracking** → maintain a `size[]` array alongside `parent[]`. Every `union` merges the smaller tree into the larger and updates the root's size. After processing all edges, collect sizes at root nodes.
5. **Collect and sort** → iterate all `n` nodes, emit `size[find(i)]` for each root (where `find(i) == i`), then sort descending. Isolated nodes are roots with `size = 1` automatically — no special-casing needed.

The chain of reasoning makes Union-Find the obvious choice: the problem is literally the definition of what Union-Find was designed to solve.

---

## 🧩 Algorithm Walkthrough

**Pattern: Union-Find (Disjoint Set Union) with Union-by-Size and Path Compression**

This is the canonical abstraction for dynamic connectivity. It maintains a forest where each tree represents one component, and two optimizations keep operations nearly O(1) amortized.

**Steps:**

1. **Initialize:** Create `parent[i] = i` and `size[i] = 1` for all `i` in `[0, n)`. Every node starts as its own root.

2. **`find(x)` with path compression:** Recursively find the root of `x`. On the way back up, point every visited node directly to the root. This flattens the tree, making future queries faster. *Invariant: `find(x)` always returns the canonical root of x's component.*

3. **`union(a, b)`:** Call `find` on both. If roots differ, merge the smaller-size tree into the larger. Update the surviving root's `size`. Decrement a component counter if you're tracking it. *Invariant: after each union, exactly one root represents the merged component.*

4. **Process all edges:** For each `[a, b]` in `connections`, call `union(a, b)`. Order of processing doesn't affect correctness.

5. **Collect results:** Iterate `0` to `n-1`. For each `i` where `parent[i] == i` (i.e., `i` is a root), append `size[i]` to results.

6. **Sort descending** and return.

Path compression + union-by-size together achieve **O(α(n))** per operation — effectively constant for all practical `n`.

---

## 📊 Worked Example

**Input:** `n = 6`, `connections = [[0,1],[1,2],[3,4]]`

| Step | Operation | `parent` | `size` | Notes |
|------|-----------|----------|--------|-------|
| Init | — | `[0,1,2,3,4,5]` | `[1,1,1,1,1,1]` | Each node is its own root |
| Edge `[0,1]` | `union(0,1)` | `[0,0,2,3,4,5]` | `[2,1,1,1,1,1]` | 1 merges into 0; `size[0]=2` |
| Edge `[1,2]` | `union(1,2)` | `[0,0,0,3,4,5]` | `[3,1,1,1,1,1]` | `find(1)=0`; 2 merges into 0; `size[0]=3` |
| Edge `[3,4]` | `union(3,4)` | `[0,0,0,3,3,5]` | `[3,1,1,2,1,1]` | 4 merges into 3; `size[3]=2` |
| Collect | roots: 0,3,5 | — | — | Sizes: 3, 2, 1 |

**Output:** `[3, 2, 1]` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n + E · α(n))** where E is the number of edges. The dominant cost is processing each connection through `union`/`find`. α(n) is the inverse Ackermann function — effectively constant (≤ 5) for any `n` up to 10^80. At 10^6 edges this is comfortably linear; at 10^9 it remains tractable where a naive O(n²) approach would not.

### Space Complexity

**O(n)** for the `parent` and `size` arrays — both owned entirely by the Union-Find structure. There is no adjacency list. Space cannot be meaningfully reduced further since you need at minimum one entry per node to track component membership.

---

## 💡 Key Takeaways

- **Pattern signal — equivalence under transitivity:** Any time the problem says "A connects to B, B connects to C, therefore A and C are in the same group," you're looking at a Union-Find or connected-components problem, regardless of domain vocabulary.
- **Pattern signal — dynamic merging with aggregate queries:** If you need to repeatedly merge groups and query group properties (size, min, max) after each merge, Union-Find with augmented metadata on the root is the right abstraction — not repeated BFS.
- **Implementation gotcha — always call `find` before comparing roots:** Directly comparing `parent[a] == parent[b]` is wrong; parent pointers are not always roots. Always resolve to canonical roots via `find` before any union or membership check.
- **Implementation gotcha — isolated nodes are silent:** The problem guarantees all `n` users exist. If you only iterate over `connections` to collect sizes, you'll silently drop isolated nodes. Always iterate all `n` indices when collecting root sizes.
- **Architectural insight — augment the root, not every node:** Storing aggregate metadata (size, count, min latency, etc.) only at the root node is a general pattern for efficient group-level queries. It transfers directly to production systems like shard-group metadata stores or cluster membership registries, where updating every member on a merge would be prohibitively expensive.

---

## 🚀 Variations & Further Practice

- **Number of Provinces (LeetCode 547):** The input is an adjacency *matrix* instead of an edge list, requiring you to extract edges before applying Union-Find or to drive BFS/DFS from the matrix directly — tests whether you can adapt the pattern to different input representations.
- **Accounts Merge (LeetCode 721):** Users are identified by email strings rather than integer IDs, and a user can have multiple emails. The twist is that you must first map arbitrary string keys to integer IDs (coordinate compression), then union on shared emails, then reconstruct the grouped string sets — adding a full layer of bookkeeping around the core Union-Find logic.
- **Redundant Connection (LeetCode 684):** Instead of computing component sizes, you must identify the *first edge* whose addition creates a cycle — i.e., the first `union` call where both nodes already share a root. This inverts the query from "what are the components?" to "when does connectivity change stop happening?", requiring you to instrument the union operation itself.