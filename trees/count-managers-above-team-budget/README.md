# Count Managers Above Team Budget

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, Binary Tree

---

## 🗂 Problem Overview
Given the root of a binary tree, count how many non-leaf nodes have a value strictly greater than the sum of their existing left and right children. Missing children contribute `0`, and leaf nodes are excluded entirely. The input is a binary tree with up to `1000` nodes; the output is a single integer count. The only real requirement is to inspect each node once while handling null children and leaf detection correctly.

## 🌍 Engineering Impact
This pattern shows up anywhere a parent entity is validated against immediate descendants: org-budget rollups, quota enforcement trees, hierarchical rate-limiters, filesystem quota checks, and service dependency budgets. In production, the key decision is whether the rule depends on direct children only or full subtree aggregates. That distinction changes both implementation and cost. A single-pass traversal is the right shape when local invariants must be checked across a hierarchy without materializing extra state. At scale, this enables streaming validation, cheap integrity checks, and predictable `O(n)` behavior instead of repeated subtree recomputation.

## 🔍 Problem Statement
You are given the root of a binary tree where each node contains an integer `val`. For every non-leaf node, compute its immediate team budget as:

- `left.val` if only the left child exists
- `right.val` if only the right child exists
- `left.val + right.val` if both exist
- leaf nodes are not evaluated

A node is counted if `node.val > immediate_child_sum`.

Return the total number of such nodes.

Constraints:

- Number of nodes: `[1, 1000]`
- `-1000 <= Node.val <= 1000`
- Tree structure: binary tree

Examples:

- `root = [10,4,3,1,2]` → `2`
- `root = [5,7,1,null,2]` → `1`

The constraint that drives the algorithmic choice is simple but important: every node may need inspection, so the optimal solution is a full tree traversal with constant work per node.

## 🪜 How to Solve This
1. Read the condition carefully → the comparison is **local**, not subtree-wide. We only compare a node against the values of its direct children.
2. Local checks on every node in a tree usually imply a traversal problem → DFS is the natural first choice.
3. Notice that leaf nodes must be skipped entirely → before counting, determine whether the current node has at least one child.
4. For each non-leaf node, compute `childSum` using existing children only → null children contribute `0`.
5. Compare `node.val > childSum` → if true, increment the answer.
6. Continue traversing left and right so every node is evaluated exactly once.
7. Because the tree has at most `1000` nodes, either recursive DFS or iterative DFS with an explicit stack is safe and straightforward.
8. The key simplification is that no information from grandchildren or deeper descendants is needed, so there is no need for postorder aggregation, memoization, or dynamic programming.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Depth-First Search (DFS).**  
   This is a tree-wide validation where each node is checked independently using only its immediate children. DFS is the right abstraction because it guarantees complete coverage with minimal control logic.

2. **Visit a node once.**  
   At each node, first determine whether it is a leaf: both `left` and `right` are `null`. If it is a leaf, do not count it. This preserves the problem invariant that only managers with at least one direct report are evaluated.

3. **Compute the immediate child sum.**  
   Initialize `childSum = 0`. If `left` exists, add `left.val`; if `right` exists, add `right.val`. This is correct because the problem explicitly defines missing children as contributing nothing.

4. **Apply the over-budget rule.**  
   If the node is non-leaf and `node.val > childSum`, increment the global count or return `1` from this frame. The invariant after this step is: every visited non-leaf node has been evaluated exactly once against the correct local rule.

5. **Recurse or push children.**  
   Traverse into `left` and `right`. Their results are independent, so the total answer is the sum of local matches across the tree.

6. **Return the accumulated count.**  
   Because each node contributes at most one count and is processed once, the final result is exact and runs in linear time.

## 📊 Worked Example
Use `root = [10,4,3,1,2]`.

| Node | Left | Right | Leaf? | Child Sum | `node.val > childSum` | Count |
|---|---:|---:|---|---:|---|---:|
| 10 | 4 | 3 | No | 7 | Yes | 1 |
| 4 | 1 | 2 | No | 3 | Yes | 2 |
| 1 | null | null | Yes | — | Not checked | 2 |
| 2 | null | null | Yes | — | Not checked | 2 |
| 3 | null | null | Yes | — | Not checked | 2 |

Trace:

1. Start at `10` → child sum is `4 + 3 = 7` → `10 > 7`, count becomes `1`.
2. Move to `4` → child sum is `1 + 2 = 3` → `4 > 3`, count becomes `2`.
3. Nodes `1`, `2`, and `3` are leaves, so they are skipped.
4. Traversal ends with total `2`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`, where `n` is the number of nodes. Each node is visited once, and the work per visit is constant: leaf check, child-value sum, and one comparison. At `10^6` nodes this is still linear and practical; at `10^9`, traversal cost dominates regardless of implementation details.

### Space Complexity
`O(h)` for DFS stack usage, where `h` is tree height. In a balanced tree this is `O(log n)`; in a skewed tree it becomes `O(n)`. The space is owned by the recursion stack or explicit iterative stack. It cannot be reduced below traversal state without changing execution model.

## 💡 Key Takeaways
- If a tree problem asks you to evaluate every node against its immediate children, think DFS with constant work per node.
- If the rule is local rather than subtree-aggregate, you do not need postorder sums or dynamic programming.
- Leaf nodes are explicitly excluded; do not accidentally evaluate them as `val > 0`.
- A missing child contributes `0`, so single-child nodes must still be checked as non-leaf nodes.
- In production hierarchies, distinguishing local invariants from recursive aggregates is what keeps validation logic simple, cheap, and composable.

## 🚀 Variations & Further Practice
- Count nodes where `node.val` is greater than the **sum of the entire subtree below it**. The twist is that you now need postorder aggregation, not a purely local check.
- Return the **list of violating nodes or paths** instead of just the count. The harder part is preserving identity and traversal context cleanly.
- Generalize from binary tree to **N-ary hierarchy**. The conceptual shift is replacing fixed child handling with iteration over arbitrary fan-out while preserving linear traversal cost.