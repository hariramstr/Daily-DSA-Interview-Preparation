# Count Single-Child Checkpoints in a Binary Tree

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Binary Tree, DFS

---

## 🗂 Problem Overview
Given the root of a binary tree, count how many nodes have exactly one child. The node values are irrelevant; only tree structure matters. Return `0` for an empty tree. The core challenge is not computation but complete structural inspection: a node qualifies only when one child pointer is present and the other is absent, so every reachable node must be visited exactly once.

## 🌍 Engineering Impact
This pattern shows up anywhere systems reason about topology rather than payload: AST validation in compilers, sparse hierarchy checks in access-control graphs, workflow DAG sanity scans, and storage/index tree health checks. In production, structural predicates like “exactly one downstream edge” often indicate malformed data, partial rollout state, or inefficient layout. The important engineering lesson is that value-centric logic is the wrong abstraction here; the traversal exists to inspect shape. At scale, choosing a single-pass traversal avoids repeated tree walks, keeps latency predictable, and makes instrumentation straightforward when these checks run in hot validation or reconciliation paths.

## 🔍 Problem Statement
You are given the root of a standard binary tree with `0` to `1000` nodes. Each node contains an integer value in `[-10^4, 10^4]`, but those values do not affect the answer. A node is a single-child checkpoint if it has exactly one direct child: left only or right only. Leaves do not count, and nodes with two children do not count.

Return the total number of such nodes. If the tree is empty, return `0`.

Examples:

- `root = [5,3,8,1,null,null,9]` → `2`  
  Node `3` has only left child `1`, and node `8` has only right child `9`.

- `root = [10,4,12,2,6,null,null]` → `0`  
  Nodes `10` and `4` each have two children; `2`, `6`, and `12` are leaves.

The key constraint is structural completeness: because any node may qualify, the algorithm must inspect every node once.

## 🪜 How to Solve This
1. Read the problem → notice the node values are irrelevant. This immediately tells you the task is about tree shape, not search, ordering, or aggregation by value.

2. Ask what makes a node count → exactly one child exists. That condition is local: for any node, inspect `left` and `right` and check whether one is null and the other is not.

3. If the condition is local, why isn’t the problem trivial? → because you still need to evaluate that condition for every node in the tree. There is no pruning rule based on values or subtree properties.

4. “Visit every node once” in a tree usually means traversal → DFS or BFS both work. DFS is the most natural because the result is just a count, and recursion maps directly to the tree structure.

5. During traversal, maintain one invariant: after processing a node and its descendants, the accumulated count equals the number of single-child nodes seen so far.

6. Edge cases fall out naturally → empty tree returns `0`; leaves contribute `0`; nodes with two children contribute `0`; nodes with one child contribute `1`.

This leads to a linear scan of the tree with a constant-time check per node.

## 🧩 Algorithm Walkthrough
1. **Choose the traversal pattern: DFS over a binary tree.**  
   This is a standard **Depth-First Search (DFS)** problem because the tree has no cycles, every node must be visited, and the per-node work is constant. DFS is the right abstraction when the result is an aggregation over all nodes.

2. **Handle the base case.**  
   If the current node is `null`, return `0`. This is correct because an empty subtree contains no qualifying nodes. The invariant is that each recursive call returns the exact count for its subtree.

3. **Evaluate the current node locally.**  
   Check whether exactly one child exists. A precise condition is:  
   `(node.left == null) != (node.right == null)`  
   This XOR-style test is correct because it is true only when one side is null and the other is not.

4. **Recurse into both subtrees.**  
   Compute the count from the left subtree and the right subtree independently. This preserves correctness because the property is local and subtree counts are additive.

5. **Combine results.**  
   Return:  
   `currentNodeContribution + leftCount + rightCount`  
   This works because each node belongs to exactly one subtree rooted at the current call, so no node is missed or double-counted.

6. **Alternative implementation: iterative DFS or BFS.**  
   A stack or queue can replace recursion if you want explicit control over traversal state or to avoid recursion depth concerns. The counting logic remains identical.

## 📊 Worked Example
Use `root = [5,3,8,1,null,null,9]`.

| Step | Node | Left | Right | Single-child? | Running Count |
|------|------|------|-------|---------------|---------------|
| 1 | 5 | 3 | 8 | No, two children | 0 |
| 2 | 3 | 1 | null | Yes | 1 |
| 3 | 1 | null | null | No, leaf | 1 |
| 4 | 8 | null | 9 | Yes | 2 |
| 5 | 9 | null | null | No, leaf | 2 |

Trace using DFS:
1. Start at `5`; it does not qualify.
2. Traverse left to `3`; it has exactly one child, so increment.
3. Visit `1`; leaf, no increment.
4. Return and traverse right to `8`; it has exactly one child, so increment.
5. Visit `9`; leaf, no increment.

Final answer: `2`.

## ⏱ Complexity Analysis
### Time Complexity
The time complexity is `O(n)`, where `n` is the number of nodes in the tree. Each node is visited exactly once, and the work per visit is constant: inspect two child pointers and add subtree counts. At `10^6` nodes this remains a straightforward linear scan; at `10^9`, the algorithm is still optimal asymptotically, but the tree would be impractical to hold and traverse in memory on a single machine.

### Space Complexity
The space complexity is `O(h)` for recursive DFS, where `h` is the tree height, due to the call stack. In the worst case of a skewed tree, `h = n`. An iterative traversal uses `O(h)` stack space for DFS or up to `O(w)` queue space for BFS, trading recursion simplicity for explicit memory control.

## 💡 Key Takeaways
- If a tree problem says node values do not matter and asks about parent/child presence, treat it as a structural traversal problem.
- “Count nodes satisfying a local condition” is a strong signal for single-pass DFS or BFS with constant-time work per node.
- Do not count leaves: zero children is not the same as one child.
- Be precise about the predicate: “exactly one child” means XOR, not OR.
- In production code, separate topology checks from payload logic; structural validation often scales better and composes cleanly with observability and reconciliation workflows.

## 🚀 Variations & Further Practice
- Count nodes with exactly `k` children in an n-ary tree; the twist is generalizing the local predicate beyond binary structure.
- Return all single-child node values or paths instead of just the count; the twist is preserving traversal state and output ordering.
- Count single-child nodes only on root-to-leaf paths satisfying a constraint, such as path sum or depth parity; the twist is combining local structural checks with path-dependent state.