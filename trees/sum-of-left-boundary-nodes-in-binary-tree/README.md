# Sum of Left Boundary Nodes in Binary Tree

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Tree, Depth-First Search, Binary Tree

---

## 🗂 Problem Overview

Given a binary tree, compute the sum of all non-leaf node values along the left boundary — the path from root to the leftmost leaf, preferring left children but falling back to right when no left child exists. The root is always included; the terminal leaf is always excluded. The non-triviality lies in correctly identifying the fallback-to-right behavior while still excluding the leaf endpoint.

---

## 🌍 Engineering Impact

This traversal pattern — following a priority path through a hierarchical structure with a fallback rule — appears in B-tree range scans (leftmost key lookup), decision-tree inference engines (following the highest-priority branch), and file-system directory traversal (deepest-left path for inode pre-fetching). In distributed systems, leader-election trees and segment trees use analogous boundary traversals to locate minimum-cost nodes. Getting the boundary definition wrong — especially the leaf-exclusion rule — corrupts aggregation results silently, which is exactly the class of bug that survives unit tests and surfaces only under production data distributions.

---

## 🔍 Problem Statement

**Input:** Root of a binary tree with 1–100 nodes; node values in `[-100, 100]`.  
**Output:** Integer sum of all non-leaf nodes on the left boundary.

**Left boundary definition:** Starting at root, at each node take the left child if it exists; otherwise take the right child. Stop when you reach a leaf. Include every node visited *except* the leaf.

**Edge cases to handle:**
- Single-node tree: the root is itself a leaf — sum is 0.
- Root with only a right subtree: root is still included; the boundary then follows right children.
- Negative node values: the sum can be less than any individual node value.

```
Example 1: root = [1,2,3,4,5,null,null,7]  → Output: 7  (1+2+4, leaf 7 excluded)
Example 2: root = [10,20,30,40,null,null,null] → Output: 30 (10+20, leaf 40 excluded)
```

The constraint driving the algorithm: boundary is a **single root-to-leaf path**, not a set — this rules out any BFS or full-tree DFS and points directly to iterative path following.

---

## 🪜 How to Solve This

1. **Read the definition carefully** → "left boundary" is not all left children; it's a single path with a right-child fallback. This is a greedy descent, not a search.

2. **Identify what to accumulate** → We want a running sum along the path, but we must withhold the final node (the leaf). This means we can't add-then-check; we need to check-then-add, or use a lookahead.

3. **Recognize the loop structure** → At each step the rule is deterministic: left child exists? go left. Otherwise, right child exists? go right. Neither? you're at a leaf, stop. This is a simple `while` loop — no recursion needed, no stack.

4. **Handle the single-node edge case first** → If root has no children, it's a leaf; return 0 immediately. This prevents the loop from adding the root when it shouldn't.

5. **Confirm with examples** → Trace Example 1: start at 1 (non-leaf, add), go to 2 (non-leaf, add), go to 4 (non-leaf, add), go to 7 (leaf, stop). Sum = 7. Matches expected output — the approach is correct.

---

## 🧩 Algorithm Walkthrough

**Pattern: Greedy Path Descent (iterative)**

This is the right abstraction because the boundary is fully determined at each node by a local, stateless rule — no backtracking, no memoization, no auxiliary data structure needed.

**Steps:**

1. **Leaf check on root:** If `root` has no left or right child, it is a leaf. Return `0` immediately. This is the only case where the root itself is excluded.

2. **Initialize:** Set `current = root`, `sum = 0`.

3. **Descent loop:** While `current` is not `None`:
   - **Is it a leaf?** If `current.left is None` and `current.right is None` → break. Do not add to sum.
   - **Add to sum:** `sum += current.val` (we only reach this line for non-leaf nodes).
   - **Advance:** If `current.left` exists, move to `current.left`. Otherwise move to `current.right`.

4. **Return `sum`.**

**Invariant maintained:** At every iteration entry, `current` points to a node that has not yet been evaluated. The leaf check before accumulation ensures we never add a leaf value, regardless of tree shape. The left-first, right-fallback advance rule exactly encodes the boundary definition. No node is visited more than once; the path length is bounded by tree height.

---

## 📊 Worked Example

**Input:** `root = [1, 2, 3, 4, 5, null, null, 7]`

| Step | `current` | Is Leaf? | Action | `sum` |
|------|-----------|----------|--------|-------|
| 1 | node(1) | No (has left=2) | Add 1, move left → node(2) | 1 |
| 2 | node(2) | No (has left=4) | Add 2, move left → node(4) | 3 |
| 3 | node(4) | No (has left=7) | Add 4, move left → node(7) | 7 |
| 4 | node(7) | Yes (no children) | Break — do not add | 7 |

**Return:** `7` ✓

The fallback-to-right rule doesn't trigger here, but if node(4) had no left child and only a right child (7), the behavior would be identical — the boundary definition handles it transparently.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(h)** where `h` is the tree height. The loop visits exactly one node per level along the boundary path. For a balanced tree this is O(log n); for a degenerate (skewed) tree it degrades to O(n). At 10⁶ nodes, even worst-case O(n) is trivially fast. At 10⁹ nodes in a balanced tree, O(log n) ≈ 30 operations — negligible.

### Space Complexity

**O(1)** — only two pointers (`current`, `sum`) are maintained regardless of tree size. No recursion stack, no auxiliary collection. This cannot be reduced further; the iterative formulation already achieves the theoretical minimum.

---

## 💡 Key Takeaways

- **Pattern signal — greedy descent:** When a problem defines a path through a tree using a deterministic local rule (prefer X, fallback to Y), the solution is an iterative descent loop, not a full traversal.
- **Pattern signal — boundary vs. set:** "Left boundary" sounds like it might require collecting all left-child nodes, but the fallback rule reveals it's a single path — re-read definitions before reaching for DFS.
- **Gotcha — leaf exclusion timing:** The leaf check must occur *before* accumulation. Checking after leads to an off-by-one where the leaf value is added then subtracted (or simply added incorrectly). Check first, add only if non-leaf.
- **Gotcha — single-node root:** A root with no children is simultaneously the start of the boundary *and* a leaf. Without an explicit upfront check, the loop body adds it before the leaf check can fire, corrupting the result.
- **Architectural insight:** The "check classification before accumulation" pattern — validate the role of a node/event/record before deciding whether to include it in an aggregate — is a foundational principle in stream processing and ETL pipelines, where misclassified records silently corrupt downstream metrics.

---

## 🚀 Variations & Further Practice

- **Full Boundary Sum (LeetCode 545 — Boundary of Binary Tree):** Extend to include left boundary non-leaves + all leaf nodes (left-to-right) + right boundary non-leaves (bottom-to-top). The twist: you must handle three distinct traversal modes and stitch them together without double-counting the root or corners.
- **Deepest Left Leaf Sum:** Find the sum of all nodes on the path to the deepest left leaf, where "left" means the node is a left child of its parent. Requires tracking parent-child relationship during descent, adding state that the simple boundary walk avoids.
- **Kth Left Boundary Node:** Return the value of the kth node along the left boundary (1-indexed from root). Introduces an index counter but more interestingly forces you to handle the case where the boundary path has fewer than k non-leaf nodes — a clean exercise in defensive path traversal.