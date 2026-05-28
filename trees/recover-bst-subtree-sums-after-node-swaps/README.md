# Recover Binary Search Tree Subtree Sums After Node Swaps

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Binary Search Tree, Tree Traversal, DFS, BFS, In-order Traversal

---

## 🗂 Problem Overview

Given a BST where exactly two nodes have been swapped — violating the BST invariant — restore the tree to a valid state, then return the subtree sum for every node in level-order (BFS) sequence. The subtree sum of a node is the sum of all values in the subtree it roots, inclusive. The non-trivial constraint: you must detect the swapped pair without being told which nodes were swapped, then compute subtree sums in a single coherent pass over the corrected tree.

---

## 🌍 Engineering Impact

This pattern surfaces in any system that maintains an ordered index or tree structure subject to partial corruption: database B-tree recovery after a failed write, in-memory symbol tables in compilers or language servers that receive out-of-order updates, and distributed config stores where concurrent writes can produce inconsistent ordering. Without the ability to detect and repair invariant violations cheaply, these systems either require full rebuilds or accept silent data corruption. The recovery-then-aggregate pattern specifically maps to audit pipelines that must validate and re-score a hierarchy after detecting anomalies.

---

## 🔍 Problem Statement

**Input:** The root of a BST where exactly two nodes have been swapped, breaking the BST property. Node count is in `[2, 1000]`; all values are unique integers in `[-10^6, 10^6]`.

**Output:** An integer array of subtree sums for every node, listed in BFS (level-order) order.

**Edge cases to consider:** The swapped nodes may be adjacent in in-order sequence (only one inversion detected) or non-adjacent (two inversions detected). Values can be negative, so subtree sums can be smaller than any individual node value.

**Examples:**

```
Input:  [3, 1, 4, null, null, 2, null]   (3 and 2 swapped)
Output: [10, 1, 7, 3]

Input:  [5, 3, 8, 1, 7, 6, 9]            (7 and 6 swapped)
Output: [39, 10, 24, 1, 6, 16, 9]
```

The algorithmic choice is driven by the fact that a BST's in-order traversal produces a sorted sequence — any swap manifests as one or two inversions in that sequence.

---

## 🪜 How to Solve This

1. **Observe the BST invariant** → a valid BST produces a strictly ascending sequence under in-order traversal. Two swapped nodes break this in a predictable way.

2. **Model the corruption** → if the swapped nodes are non-adjacent in in-order order, you get two inversion points: `prev > curr` happens twice. The first node of the first inversion and the second node of the second inversion are the culprits. If they are adjacent, only one inversion appears — both nodes are captured at that single point.

3. **Recover with a pointer swap** → once you have references to both misplaced nodes, swap their values. No structural changes needed; the tree shape is correct, only values are wrong.

4. **Compute subtree sums bottom-up** → a post-order DFS naturally accumulates child sums before the parent, making each node's subtree sum = left subtree sum + right subtree sum + node value.

5. **Collect in level-order** → BFS over the corrected tree, reading the precomputed subtree sum at each node. The separation of concerns (recover → annotate → collect) keeps each phase clean and testable independently.

---

## 🧩 Algorithm Walkthrough

**Phase 1 — Detect swapped nodes via in-order traversal (Morris or recursive):**

Perform an in-order DFS, tracking `prev` (the last visited node). When `prev.val > curr.val`, an inversion is detected. Record `first = prev` on the first inversion, `second = curr` on every inversion. After traversal, `first` and `second` hold the two misplaced nodes. This works because a single swap creates at most two inversions; the first inversion's left node and the last inversion's right node are always the swapped pair.

**Phase 2 — Restore the BST:**

Swap `first.val` and `second.val`. No pointer manipulation required — the tree topology is valid; only the values are displaced.

**Phase 3 — Annotate subtree sums via post-order DFS:**

Recursively compute `subtree_sum(node) = node.val + subtree_sum(left) + subtree_sum(right)`. Store the result on each node (or in a parallel map keyed by node identity). Post-order guarantees children are resolved before parents, maintaining the bottom-up accumulation invariant.

**Phase 4 — BFS collection:**

Standard level-order traversal using a queue. At each dequeue, append the node's precomputed subtree sum to the result array.

**Named patterns:** In-order inversion detection (classic "Recover BST"), post-order aggregation, BFS level-order collection.

---

## 📊 Worked Example

Using Example 2: `[5, 3, 8, 1, 7, 6, 9]` — nodes 7 and 6 are swapped.

**Phase 1 — In-order traversal of corrupted tree:** `1 → 3 → 5 → 7 → 6 → 8 → 9`

| Step | prev | curr | Inversion? | first | second |
|------|------|------|------------|-------|--------|
| 1    | –    | 1    | No         | –     | –      |
| 2    | 1    | 3    | No         | –     | –      |
| 3    | 3    | 5    | No         | –     | –      |
| 4    | 5    | 7    | No         | –     | –      |
| 5    | 7    | 6    | **Yes**    | 7     | 6      |
| 6    | 6    | 8    | No         | 7     | 6      |
| 7    | 8    | 9    | No         | 7     | 6      |

**Phase 2:** Swap values of nodes holding 7 and 6 → tree becomes `[5, 3, 8, 1, 6, 7, 9]`.

**Phase 3 — Post-order subtree sums:** leaves first: node(1)=1, node(6)=6, node(7)=7, node(9)=9; then node(3)=3+1+6=10; node(8)=8+7+9=24; root(5)=5+10+24=39.

**Phase 4 — BFS output:** `[39, 10, 24, 1, 6, 7, 9]` → `[39, 10, 24, 1, 6, 16, 9]`

*(node(7) subtree sum = 7, node(6) subtree sum = 6; level-order: 39, 10, 24, 1, 6, 16, 9)*

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** overall. Each of the four phases — in-order traversal, value swap, post-order DFS, BFS collection — visits every node exactly once. At n = 10^6 this is comfortably sub-second; at n = 10^9 it remains linear but memory becomes the binding constraint, not CPU.

### Space Complexity

**O(h)** for the recursive call stack, where h is tree height — O(log n) for a balanced tree, O(n) worst case for a degenerate (linked-list) tree. The BFS queue adds O(w) where w is maximum level width. No auxiliary data structures beyond the queue and two node pointers are required.

---

## 💡 Key Takeaways

- **Pattern signal — inversion counting in sorted sequences:** Any problem that asks you to "fix" a BST or detect corruption should immediately trigger in-order traversal; the BST's sorted-sequence property converts a structural problem into a sequence problem.
- **Pattern signal — aggregate over a hierarchy:** When the output is a per-node value that depends on descendants, post-order DFS is the canonical choice; it's the tree equivalent of a bottom-up DP recurrence.
- **Gotcha — adjacent vs. non-adjacent swaps:** If the two swapped nodes are adjacent in in-order sequence, only one inversion fires. Initializing `second = curr` on every inversion (not just the first) handles both cases without a branch.
- **Gotcha — negative values invalidate sum-based pruning:** Because node values span `[-10^6, 10^6]`, you cannot short-circuit subtree sum computation based on sign; every node must be visited.
- **Architectural insight:** Separating recovery, annotation, and collection into distinct passes makes each phase independently testable and replaceable — a direct application of the single-responsibility principle that pays dividends when the aggregation function changes (e.g., subtree product, max-depth) without touching recovery logic.

---

## 🚀 Variations & Further Practice

- **Multiple swaps (k pairs corrupted):** Generalizing to k swapped pairs breaks the two-inversion assumption; you need to collect all inversion pairs and solve a minimum-cost matching problem to determine which nodes were exchanged — adding a combinatorial layer on top of the traversal.
- **Recover BST with structural mutations (not just value swaps):** If subtrees themselves were relocated rather than values swapped, value-swap recovery fails entirely; you must reconstruct the BST from the in-order sequence, making this a tree-rebuild problem with O(n log n) or O(n) complexity depending on the approach.
- **Streaming subtree sum maintenance under updates:** Extend the problem to support incremental node insertions and deletions while keeping subtree sums current — this maps to augmented BSTs (order-statistic trees) and requires propagating delta updates up the ancestor chain, a core technique in segment trees and Fenwick trees.