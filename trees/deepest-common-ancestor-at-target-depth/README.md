# Deepest Common Ancestor at Target Depth

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Binary Tree, Depth-First Search, Lowest Common Ancestor

---

## 🗂 Problem Overview

Given a binary tree and a target depth, find the lowest common ancestor of **all** nodes that exist at exactly that depth. The challenge isn't finding a single LCA between two known nodes — it's that the set of target nodes is implicit and variable-sized. You must simultaneously discover which nodes exist at the target depth and propagate ancestry information back up the tree in a single coherent traversal.

---

## 🌍 Engineering Impact

This pattern surfaces in hierarchical data systems where you need to aggregate or coordinate across all entities at a specific tier. Examples: distributed tracing systems that must identify the deepest common service boundary for a set of leaf-level spans; compiler symbol table resolution finding the tightest enclosing scope for a set of references; organizational hierarchy tools computing the lowest common reporting chain for a team subset; filesystem quota enforcement finding the deepest shared directory ancestor for a group of files. Getting this wrong means either over-broad scope (performance, permission leakage) or under-broad scope (missed aggregation).

---

## 🔍 Problem Statement

Given the `root` of a binary tree and an integer `depth` (1-indexed, root at depth 1), return the deepest node that is an ancestor of **every** node at exactly `depth`. If only one node exists at that depth, return it directly. If no nodes exist at that depth, return `null`.

**Constraints:** 1–1000 nodes; node values in `[-10^5, 10^5]`, all unique; `1 <= depth <= 1000`.

| Input | Depth | Output | Reason |
|---|---|---|---|
| `[3,5,1,6,2,0,8,null,null,7,4]` | 4 | Node `2` | Nodes `{7,4}` at depth 4; LCA is node 2 |
| `[1,2,3,4,5]` | 3 | Node `2` | Nodes `{4,5}` at depth 3; node 3 has no depth-3 children |
| `[1,2,3]` | 1 | Node `1` | Only root at depth 1 |

The key constraint: `depth` can exceed the tree's actual height, and the target node set is non-uniform across subtrees — some subtrees contribute nodes at the target depth, others don't.

---

## 🪜 How to Solve This

1. **Observe the dependency** → To find the LCA, you need to know which nodes are at the target depth. But those nodes are spread across the tree. Collecting them first and then running a separate LCA pass means two traversals and extra bookkeeping.

2. **Flip the framing** → Instead of "find nodes then find LCA," ask each subtree: "do you contain any nodes at the target depth, and if so, what is their LCA within you?" This is a single post-order question each node can answer about itself.

3. **Define the recursive return value** → Each call returns `(lca_candidate, deepest_depth_reached)`. If both children reach the target depth, the current node is the LCA. If only one child reaches it, bubble that child's LCA up. If neither reaches it, return `null`.

4. **Handle the single-node case naturally** → When a node *is* at the target depth, it returns itself with its own depth — no special casing needed.

5. **Recognize the invariant** → At every node, the returned candidate is always the correct LCA for the target-depth nodes in that subtree. This composes cleanly up the tree.

---

## 🧩 Algorithm Walkthrough

**Pattern: Post-Order DFS with Upward Propagation**

Post-order is the right abstraction because the LCA decision at any node depends on information from both subtrees — you must process children before parents.

1. **Base case — null node:** Return `(null, 0)`. Depth 0 signals "no contribution."

2. **Base case — leaf at target depth:** If `current_depth == depth`, return `(node, depth)`. This node is both a target and its own LCA within its single-node subtree.

3. **Base case — leaf above target depth:** If `current_depth == depth` is false and node is a leaf, return `(null, current_depth)`. This subtree has no target-depth nodes.

4. **Recurse into children:** Call DFS on left and right children, passing `current_depth + 1`.

5. **Merge results:** Compare the deepest depths returned by left and right subtrees:
   - Both equal `depth` → both subtrees have target nodes; current node is their LCA. Return `(current_node, depth)`.
   - Only left equals `depth` → return left's `(lca, depth)` unchanged.
   - Only right equals `depth` → return right's `(lca, depth)` unchanged.
   - Neither equals `depth` → return `(null, max(left_depth, right_depth))`.

6. **Final answer:** The `lca` field of the root's return value is the answer.

The invariant maintained at every step: the returned `lca` is the correct lowest common ancestor for all target-depth nodes within the current subtree.

---

## 📊 Worked Example

Tree: `[3,5,1,6,2,0,8,null,null,7,4]`, target `depth = 4`

| Node | Depth | Left Result | Right Result | Returned (lca, max_depth) |
|------|-------|-------------|--------------|---------------------------|
| 7 | 4 | — | — | (7, 4) — target hit |
| 4 | 4 | — | — | (4, 4) — target hit |
| 2 | 3 | (7, 4) | (4, 4) | (2, 4) — both sides hit depth 4 |
| 6 | 3 | (null, 0) | (null, 0) | (null, 0) |
| 5 | 2 | (null, 0) | (2, 4) | (2, 4) — only right hits depth 4 |
| 0 | 3 | — | — | (null, 3) |
| 8 | 3 | — | — | (null, 3) |
| 1 | 2 | (null, 3) | (null, 3) | (null, 3) |
| 3 | 1 | (2, 4) | (null, 3) | **(2, 4)** — final answer: node 2 |

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** — every node is visited exactly once in the DFS. No node is revisited regardless of tree shape. At 10^6 nodes this is comfortably fast; at 10^9 nodes you'd need iterative DFS to avoid stack overflow, but the work per node remains constant.

### Space Complexity

**O(h)** where `h` is tree height — the call stack owns this space. For a balanced tree, `h = O(log n)`; for a degenerate (linked-list) tree, `h = O(n)`. Converting to an explicit stack eliminates system stack risk at the cost of heap allocation.

---

## 💡 Key Takeaways

- **Pattern signal — "LCA of a dynamic set":** Whenever the problem asks for an ancestor of *all* members of an implicitly defined node set (not just two named nodes), post-order DFS with upward propagation is the right frame.
- **Pattern signal — subtree aggregation:** If a problem requires combining information from both children before making a decision at the parent, that's a post-order problem — not pre-order, not BFS.
- **Gotcha — depth is 1-indexed:** The root is at depth 1, not 0. Passing `current_depth + 1` to children and initializing the root call at depth 1 prevents an off-by-one that silently returns wrong nodes.
- **Gotcha — depth exceeds tree height:** If `depth > actual_height`, no node returns `depth` as its max depth, so the root returns `(null, actual_height)`. Ensure the caller handles `null` rather than assuming a valid node is always returned.
- **Architectural insight:** The `(result, metadata)` return tuple pattern — where metadata drives the merge logic but isn't the final answer — is a broadly applicable technique for single-pass tree aggregation. It avoids global state and makes the recursion composable, which matters when embedding this logic inside larger tree-processing pipelines.

---

## 🚀 Variations & Further Practice

- **[LC 1123 — Lowest Common Ancestor of Deepest Leaves](https://leetcode.com/problems/lowest-common-ancestor-of-deepest-leaves/):** The target set is the deepest leaves rather than nodes at a fixed depth. The twist: "deepest" is itself computed during traversal, so the merge condition changes from a fixed threshold to a dynamic maximum — you must track the globally deepest level seen so far.
- **LCA of nodes matching a predicate:** Generalize the target set to any boolean condition on node values (e.g., all nodes with value > k). The structure is identical, but the base case becomes a predicate check rather than a depth check, and you lose the guarantee that target nodes form a clean horizontal band — requiring careful handling of ancestors that are themselves in the target set.
- **K-ary tree variant:** Extend to trees where each node has up to `k` children. The binary merge (`left vs right`) becomes a reduction over `k` child results. The algorithmic structure is unchanged, but the merge step becomes `O(k)` per node, lifting total complexity to `O(n·k)` — relevant for filesystem trees or org-chart hierarchies with high branching factors.