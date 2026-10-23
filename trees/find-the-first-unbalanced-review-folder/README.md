# Find the First Unbalanced Review Folder

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Binary Tree, Breadth-First Search

---

## 🗂 Problem Overview
Given a binary tree, return the value of the first node whose left-subtree sum differs from its right-subtree sum, where “first” is defined by level-order traversal. Missing children contribute a subtree sum of 0. If no such node exists, return `-1`. The non-trivial part is that level-order determines *which* unbalanced node to return, but subtree sums are naturally computed bottom-up, so the solution must combine two traversal strategies efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere local decisions depend on fully aggregated child state but reporting must follow a different traversal or priority order. Examples include storage quota rollups in hierarchical file systems, cost-accounting trees in cloud billing, compiler AST annotation passes, and health propagation in service dependency graphs. At scale, recomputing subtree aggregates per node turns into avoidable quadratic work and unstable latency. Separating bottom-up aggregation from top-down or breadth-first selection gives deterministic behavior, linear complexity, and a clean architecture for caching derived state before policy evaluation.

## 🔍 Problem Statement
You are given the root of a binary tree with `1..1000` nodes. Each node stores an integer value in the range `0..10^4`, representing pending files in that folder. A node is **balanced** when the sum of all values in its left subtree equals the sum of all values in its right subtree. If a child is missing, that subtree sum is `0`.

Return the stored value of the **first unbalanced node encountered in level-order traversal** (top to bottom, left to right). If every node is balanced, return `-1`.

Examples:

- `root = [8,4,4,2,2,2,2]` → `-1`
- `root = [10,3,5,1,null,2,1]` → `10`

The key constraint is not tree size but traversal mismatch: subtree sums require post-order reasoning, while the answer requires BFS ordering. That drives the two-phase algorithm.

## 🪜 How to Solve This
1. Read the requirement carefully → the answer is **not** the first node found by DFS, and it is **not** any unbalanced node. It must be the first one in **level order**.

2. Ask what makes a node balanced → you need the total sum of its left subtree and right subtree. That immediately suggests a **bottom-up** computation, because a parent’s answer depends on complete child information.

3. Realize BFS alone is insufficient → if you visit nodes level by level and compute subtree sums on demand, you will repeatedly traverse the same descendants. In the worst case that becomes quadratic.

4. So split the problem into two passes:
   - First pass: compute and store each node’s subtree sum using post-order DFS.
   - Second pass: run BFS and, for each node, compare the precomputed left and right subtree sums.

5. Why this is the obvious approach → one traversal builds derived state once; the second traversal applies the selection rule in the exact order the problem asks for.

6. Return the node’s stored value at the first mismatch; if BFS finishes cleanly, return `-1`.

## 🧩 Algorithm Walkthrough
1. **Use a post-order DFS to compute subtree sums**.  
   For each node, recursively compute the sum of the left subtree and the right subtree, then store `node.val + leftSum + rightSum`. This is the standard **tree DP / post-order aggregation** pattern: children are solved before the parent. The invariant is that when DFS returns from a node, its full subtree sum is known and cached.

2. **Store subtree sums in a map keyed by node reference**.  
   This avoids recomputing sums during BFS. Correctness depends on associating each structural node with exactly one aggregate, even when values are duplicated. Using node values as keys would be incorrect because values are not unique.

3. **Run a breadth-first traversal from the root**.  
   BFS is the explicit selection policy because the problem asks for the first unbalanced node in level order. The invariant here is ordering: nodes are examined strictly top-to-bottom and left-to-right.

4. **For each dequeued node, read cached child subtree sums**.  
   If `node.left` is null, `leftSum = 0`; similarly for the right side. Compare `leftSum` and `rightSum`. If they differ, return `node.val` immediately. This is correct because BFS guarantees no earlier node in level order remains unchecked.

5. **Enqueue children and continue**.  
   If the current node is balanced, push its existing children into the queue and proceed.

6. **If traversal completes, return `-1`**.  
   At that point every node has been checked in the required order, so no unbalanced node exists.

## 📊 Worked Example
Example: `root = [10,3,5,1,null,2,1]`

First compute subtree sums bottom-up:

| Node | Left Sum | Right Sum | Subtree Sum |
|---|---:|---:|---:|
| 1 (left leaf) | 0 | 0 | 1 |
| 3 | 1 | 0 | 4 |
| 2 | 0 | 0 | 2 |
| 1 (right leaf) | 0 | 0 | 1 |
| 5 | 2 | 1 | 8 |
| 10 | 4 | 8 | 22 |

Then BFS:

1. Start queue: `[10]`
2. Visit `10` → left subtree sum = `4`, right subtree sum = `8`
3. Since `4 != 8`, `10` is unbalanced
4. Return `10` immediately

Even though deeper nodes may also be unbalanced or balanced, they do not matter because BFS defines the first qualifying node.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)` where `n` is the number of nodes. The DFS computes each subtree sum once, and the BFS visits each node once. No node is reprocessed structurally, so the dominant cost is two linear traversals. At `10^6` nodes this is still practical; at `10^9`, tree materialization and memory, not asymptotic complexity, become the limiting factors.

### Space Complexity
`O(n)` auxiliary space in the general case: the subtree-sum map stores one entry per node, and the BFS queue may hold up to a full level. This can be reduced by annotating nodes in place, but that trades lower memory for mutation of input structure.

## 💡 Key Takeaways
• If a tree problem asks for a node selected in one traversal order but defined by descendant aggregates, expect a multi-pass solution.  
• “Compare left and right subtree totals” is a strong signal for post-order DFS or tree DP, not repeated top-down checks.  
• Do not key cached sums by node value; values are explicitly non-unique, so cache by node identity.  
• Treat missing children as subtree sum `0`; forgetting this usually breaks leaf and single-child cases.  
• In production systems, separating aggregation from selection is a scalable design pattern: compute derived state once, then apply policy in the required evaluation order.

## 🚀 Variations & Further Practice
- Return the **node itself or its level-order index** instead of its value; the twist is handling duplicate values without ambiguity.
- Find **all** unbalanced nodes, grouped by depth; same subtree-sum pass, but the reporting phase becomes full BFS collection instead of early exit.
- Support **dynamic updates** to node values with repeated balance queries; the harder twist is moving from static tree DP to incremental maintenance with parent links, Fenwick-like indexing on Euler tours, or segment trees over flattened trees.