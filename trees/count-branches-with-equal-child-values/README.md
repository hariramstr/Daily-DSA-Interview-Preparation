# Count Branches With Equal Child Values

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Depth-First Search, Binary Tree

---

## 🗂 Problem Overview
Given the root of a binary tree, count how many nodes have both a left and right child whose values are equal. The current node’s own value is irrelevant; only its immediate children matter. Return that count as an integer. The non-trivial part is structural: you must inspect the entire tree because any node can qualify, while nodes with fewer than two children must be excluded from the count.

## 🌍 Engineering Impact
This pattern shows up anywhere hierarchical state must be validated locally while traversing a larger structure: compiler AST passes, policy trees in authorization engines, search-ranking decision trees, and config inheritance graphs. The important property is single-pass structural inspection with constant work per node. At scale, that matters because repeated subtree scans or ad hoc recursion logic create avoidable latency and make correctness brittle under change. A disciplined traversal gives predictable runtime, clean invariants, and a reusable shape for richer checks such as subtree consistency, rule symmetry, or local anomaly detection.

## 🔍 Problem Statement
You are given the root of a binary tree with `0` to `1000` nodes. Each node contains an integer value in the range `[-1000, 1000]`. A node is considered a balanced branch if:

- it has both a left child and a right child, and
- `left.val == right.val`

The node’s own value does not affect the decision.

Return the total number of balanced branch nodes in the tree.

Examples:

- `root = [8,4,4,3,3,null,3]` → `2`
- `root = [5,2,7,2,null,7,7]` → `1`

Edge cases matter:
- empty tree → `0`
- leaf nodes never count
- nodes with only one child never count

The key constraint is that the tree can contain up to `1000` nodes, which strongly suggests a full traversal with `O(n)` work rather than any selective or repeated scanning strategy.

## 🪜 How to Solve This
1. Read the condition carefully → this is a **local property** of each node, not a subtree property. We only care about the node’s immediate children.
2. If every node might qualify, we cannot shortcut much → we need to visit every node exactly once.
3. Visiting every node in a tree naturally suggests **DFS or BFS**. Either works because the check at each node is constant-time.
4. At each visited node, ask one question: does it have both children, and if so, are their values equal?
5. If yes, increment a counter. Regardless of the answer, continue traversing both children because qualifying nodes can appear anywhere below.
6. Stop when traversal finishes → the counter is the answer.

Why this approach is obvious in hindsight: the problem does not require ordering, aggregation by value, or subtree comparison. It is just a full tree walk plus a constant-time predicate per node. That is exactly what DFS/BFS is for.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Tree Traversal (DFS or BFS).**  
   This is the right abstraction because the problem asks for a property that must be checked at potentially every node in a binary tree. There is no benefit to sorting, memoization, or subtree hashing.

2. **Initialize a counter to zero.**  
   This counter represents the number of nodes seen so far that satisfy the balanced-branch condition. The invariant is simple: after processing any set of visited nodes, the counter equals the number of qualifying nodes among them.

3. **Traverse from the root.**  
   If using DFS, recursively or iteratively visit the current node, then its children. If the root is `null`, return `0` immediately. This handles the empty-tree edge case cleanly.

4. **At each node, evaluate the local predicate.**  
   Check whether `node.left != null` and `node.right != null`. Only then compare `node.left.val` and `node.right.val`. If they match, increment the counter. This is correct because the definition depends only on immediate children.

5. **Continue into both subtrees.**  
   Even if a node qualifies, its descendants may also qualify. Traversal must not stop early. The invariant remains: every visited node has been checked exactly once.

6. **Return the counter after traversal completes.**  
   Since every node was visited once and each qualifying node increments exactly once, the final count is correct.

This yields linear time with minimal auxiliary state and maps directly to production-grade tree inspection passes.

## 📊 Worked Example
Example: `root = [8,4,4,3,3,null,3]`

| Step | Current Node | Left Child | Right Child | Equal? | Count |
|---|---:|---:|---:|---|---:|
| 1 | 8 | 4 | 4 | Yes | 1 |
| 2 | 4 (left of 8) | 3 | 3 | Yes | 2 |
| 3 | 3 (left of left 4) | null | null | No | 2 |
| 4 | 3 (right of left 4) | null | null | No | 2 |
| 5 | 4 (right of 8) | null | 3 | No | 2 |
| 6 | 3 (right child of right 4) | null | null | No | 2 |

Trace summary:
- Start at the root: both children exist and both are `4`, so count becomes `1`.
- Move into the left subtree: that `4` has children `3` and `3`, so count becomes `2`.
- All remaining nodes are leaves or have only one child, so none qualify.
- Final answer: `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is the number of nodes in the tree. Each node is visited exactly once, and the work per visit is constant: null checks, one value comparison, and possible counter increment. At `10^6` nodes this remains practical as a linear scan; at `10^9`, linear time is still the lower bound but operationally expensive.

### Space Complexity
`O(h)` for recursive DFS, where `h` is the tree height, due to the call stack. In the worst case of a skewed tree, this becomes `O(n)`. Iterative DFS or BFS also uses auxiliary space proportional to tree height or breadth, trading recursion simplicity for explicit memory control.

## 💡 Key Takeaways
- If a tree problem asks you to evaluate a condition at every node and the condition depends only on immediate children, think single-pass DFS/BFS.
- Signals for this pattern: binary tree input, count/collect result, and no requirement to compare distant nodes or whole subtrees.
- Do not count leaves or single-child nodes; equality is only valid when both children exist.
- The parent node’s value is irrelevant here; comparing it to child values is a common implementation mistake.
- In production code, local predicates over hierarchical data should usually be implemented as one traversal with explicit invariants, not repeated ad hoc scans.

## 🚀 Variations & Further Practice
- Count nodes where the **entire left and right subtrees** are identical, not just child values. The twist is subtree equivalence, which requires structural comparison or hashing.
- Return the **list of qualifying nodes or paths** instead of just the count. The twist is preserving traversal context and output ordering.
- Generalize from binary trees to **N-ary trees**, counting nodes whose children all share the same value. The twist is handling variable arity and redefining the local predicate.