# Count Leaves at Each Level

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Binary Tree, BFS, Level Order Traversal

---

## 🗂 Problem Overview

Given the root of a binary tree, return a list where each element is the count of leaf nodes at that depth level (0-indexed). A leaf is any node with no children. The core contract: input is a tree, output is an ordered array of per-level leaf counts. The non-trivial constraint is that you must preserve level identity — a simple DFS accumulator loses the grouping unless you explicitly track depth, making BFS the more natural fit.

---

## 🌍 Engineering Impact

Level-order tree traversal is the backbone of several production patterns. DOM rendering engines process the widget tree level-by-level to compute layout passes. Distributed job schedulers (e.g., Airflow, Argo Workflows) traverse DAG dependency levels to identify which tasks are unblocked (leaf nodes in the remaining graph). Compiler IR passes walk the AST breadth-first to apply constant folding at terminal nodes. In each case, conflating levels corrupts the output — a scheduler that misidentifies a leaf node promotes a job before its dependencies resolve, causing cascading failures at scale.

---

## 🔍 Problem Statement

Given the root of a binary tree with 1–1000 nodes (node values in `[-1000, 1000]`), return a list where index `i` holds the count of leaf nodes at level `i`.

**Example 1:**
```
Input:  [3, 9, 20, null, null, 15, 7]
Output: [0, 1, 2]
```
Level 0: node 3 (internal) → 0 leaves. Level 1: node 9 (leaf), node 20 (internal) → 1 leaf. Level 2: nodes 15, 7 (both leaves) → 2 leaves.

**Example 2:**
```
Input:  [1, 2, 3, 4, null, null, null]
Output: [0, 1, 1]
```

**Edge cases:** A single-node tree returns `[1]`. A fully skewed tree (all nodes on one side) produces a result list as long as the tree's height, with exactly one `1` at the final level. The key constraint driving algorithmic choice: leaf counts must be bucketed by level, not just accumulated globally.

---

## 🪜 How to Solve This

1. **Read the output shape** → we need a list indexed by level. That immediately signals we need to process nodes grouped by depth, not just visit them in any order.

2. **Grouping by level** → BFS is the canonical tool. A queue processes nodes in waves; each wave corresponds to exactly one level. DFS can work but requires passing depth as a parameter and indexing into a result array — more bookkeeping, same complexity.

3. **Within each level** → count how many nodes in the current wave have no children. That's the leaf count for that level. Append it to the result.

4. **Termination** → when the queue empties, every level has been processed. The result list length equals the tree height.

5. **Verify edge cases mentally** → single node: enqueued at start, no children, count = 1, result = `[1]`. Skewed tree: each level has exactly one node; it's a leaf only at the deepest level, so all earlier entries are `0` and the last is `1`.

The reasoning chain makes the solution feel inevitable: level grouping → BFS → count leaves per wave.

---

## 🧩 Algorithm Walkthrough

**Pattern: BFS / Level Order Traversal**

BFS is the right abstraction because it naturally partitions nodes into levels via queue-wave processing. Each iteration of the outer loop corresponds to exactly one level, maintaining the invariant that all nodes in the queue at the start of an iteration belong to the same depth.

**Steps:**

1. **Initialize** — enqueue the root. Create an empty result list.

2. **Outer loop** — while the queue is non-empty, capture the current queue size `n`. This size is the exact node count for the current level — a critical invariant. Initialize `leaf_count = 0`.

3. **Inner loop** — dequeue exactly `n` nodes (one full level). For each dequeued node:
   - If it has no left and no right child, increment `leaf_count`.
   - Otherwise, enqueue its non-null children for the next level.

4. **Record** — after the inner loop, append `leaf_count` to the result. This ensures one result entry per level, in order.

5. **Return** — the result list after the outer loop terminates.

The snapshot of queue size before the inner loop is the key implementation detail. Without it, newly enqueued children would bleed into the current level's count, corrupting level boundaries. This pattern generalizes to any problem requiring per-level aggregation on a tree or graph.

---

## 📊 Worked Example

Input: `[3, 9, 20, null, null, 15, 7]`

| Iteration | Queue at start | n | Nodes processed | Leaves found | Children enqueued | Result so far |
|-----------|---------------|---|-----------------|--------------|-------------------|---------------|
| 1 (L0)    | [3]           | 1 | 3 (has children)| 0            | [9, 20]           | [0]           |
| 2 (L1)    | [9, 20]       | 2 | 9 (no children → leaf), 20 (has children) | 1 | [15, 7] | [0, 1] |
| 3 (L2)    | [15, 7]       | 2 | 15 (leaf), 7 (leaf) | 2         | []                | [0, 1, 2]     |

Queue empty → return `[0, 1, 2]`. The snapshot of `n=2` at iteration 2 prevents nodes 15 and 7 from being counted in level 1.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** — every node is enqueued and dequeued exactly once, and the leaf check is O(1) per node. At 10⁶ nodes this is comfortably sub-second. At 10⁹ nodes, memory becomes the bottleneck before CPU does; the algorithm itself scales linearly.

### Space Complexity

**O(w)** where `w` is the maximum tree width (maximum nodes at any single level). The queue owns this space. In the worst case (a perfect binary tree), `w = n/2`, making it O(n). For skewed trees, `w = 1`, so O(1) auxiliary space. This cannot be reduced without abandoning BFS.

---

## 💡 Key Takeaways

- **Pattern signal — level grouping required:** Any problem asking for per-depth aggregation (count, sum, average, max) on a tree is a BFS/level-order problem. The output being a list indexed by depth is the clearest indicator.
- **Pattern signal — "leaf" as a terminal condition:** Whenever a problem distinguishes terminal nodes (leaves, sinks, unblocked tasks) from internal nodes, you need explicit child-existence checks — not just value comparisons.
- **Gotcha — snapshot queue size before the inner loop:** Failing to capture `n = len(queue)` before processing causes children enqueued during the inner loop to be counted in the current level, silently corrupting all level boundaries.
- **Gotcha — single-node edge case:** A root with no children is simultaneously level 0 and a leaf. Ensure your leaf check (`not node.left and not node.right`) fires correctly here; a check that only runs "if the node has a parent" would miss it.
- **Architectural insight:** The queue-size snapshot pattern is a general synchronization primitive for wave-based processing — it appears in multi-source BFS (e.g., 0-1 BFS, multi-source shortest path) and distributed systems where you need to process all items from "round N" before advancing to "round N+1."

---

## 🚀 Variations & Further Practice

- **Level Averages / Level Sums (LeetCode 637):** Replace the leaf count with a sum or average across all nodes at each level. The twist: every node contributes, not just leaves, so the aggregation logic changes but the BFS skeleton is identical — tests whether you've truly internalized the pattern vs. memorized the leaf check.
- **Binary Tree Right Side View (LeetCode 199):** Return only the last node visible at each level. The twist: you need to select a specific node per level (the rightmost), not aggregate all of them. Forces you to track position within the inner loop, adding index-awareness to the wave pattern.
- **Minimum Depth of Binary Tree (LeetCode 111):** Find the depth of the shallowest leaf. The twist: early termination — you can return as soon as BFS encounters the first leaf, converting an O(n) full traversal into a best-case O(w) search. This tests whether you recognize that BFS finds the shortest path first, a property DFS does not share.