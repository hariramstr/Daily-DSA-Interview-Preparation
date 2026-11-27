# Find the Deepest Fully Budgeted Project Node

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, Postorder Traversal

---

## 🗂 Problem Overview
Given a binary tree where each node stores an integer budget, return the value of the deepest node whose left-subtree sum equals its right-subtree sum. If multiple such nodes exist at the same maximum depth, choose the leftmost one. If none exist, return `-1`.

The non-trivial part is scale: with up to `100,000` nodes, recomputing subtree sums independently for each node is too expensive. The solution must aggregate subtree information in one traversal.

## 🌍 Engineering Impact
This pattern shows up anywhere hierarchical aggregates drive decisions: cost rollups in project planning systems, quota balancing in distributed rate-limiters, AST cost analysis in compilers, and health or load propagation in service dependency trees. At small scale, repeated recomputation is tolerable; at `10^5+` nodes, it becomes a latency and CPU sink.

The production lesson is broader than trees: when each parent decision depends on complete child aggregates, bottom-up evaluation is the right architecture. It eliminates duplicate work, preserves locality of computation, and makes tie-breaking rules deterministic without extra passes.

## 🔍 Problem Statement
You are given the root of a binary tree with `1 <= n <= 100000`, and each node value is in `[-100000, 100000]`. A node is **fully budgeted** if the sum of all values in its left subtree is exactly equal to the sum of all values in its right subtree. Empty subtrees contribute `0`, so every leaf is fully budgeted.

Return the **node value** of the deepest fully budgeted node. If several fully budgeted nodes exist at the same deepest depth, return the **leftmost** one. If no such node exists, return `-1`.

Examples:

- `root = [8,4,6,2,2,3,3]` → `2`
- `root = [5,1,7,null,null,4,3]` → `4`

The key constraint is that subtree sums cannot be recomputed from scratch per node. That would degrade to quadratic behavior on skewed trees, which is unacceptable at `100,000` nodes.

## 🪜 How to Solve This
1. Read the condition carefully → whether a node qualifies depends on the **sum of its entire left subtree** and **sum of its entire right subtree**.
2. That immediately implies a local decision needs child aggregates first → this is a **bottom-up tree problem**.
3. Bottom-up on trees usually means **postorder DFS**: process left child, process right child, then evaluate the parent.
4. While returning from recursion, compute each subtree’s total sum once:
   - left subtree sum
   - right subtree sum
   - current subtree sum = `left + right + node.val`
5. At the moment both child sums are known, check whether `left == right`. If true, this node is a candidate.
6. We need the **deepest** candidate, and for ties the **leftmost** one. A left-first DFS naturally helps: if you only replace the answer when depth is strictly greater, the first node seen at that depth remains the winner.
7. This gives a single-pass solution: one traversal computes sums and tracks the best answer simultaneously.

That is the core insight: subtree aggregation and candidate selection happen in the same postorder walk.

## 🧩 Algorithm Walkthrough
1. **Use the Postorder DFS pattern.**  
   Traverse `left → right → node`. This is the correct abstraction because a node’s validity depends on complete information from both children. Before both subtree sums are known, the parent cannot be evaluated.

2. **Pass depth into DFS.**  
   Each recursive call receives the current depth, with root at `0`. This maintains the invariant that when a node is evaluated, its exact depth is already known, so no second traversal is needed.

3. **Define the DFS return value as subtree sum.**  
   For a null child, return `0`. For a real node, recursively compute `leftSum` and `rightSum`, then return `leftSum + rightSum + node.val`.  
   Invariant: every DFS call returns the sum of the entire subtree rooted at that node.

4. **Evaluate “fully budgeted” after both children are processed.**  
   If `leftSum == rightSum`, the current node qualifies. This is correct because subtree sums are final at this point; no future computation can change them.

5. **Track the best candidate globally.**  
   Maintain:
   - `bestDepth`
   - `bestValue`  
   Update only when `depth > bestDepth`. Do **not** update on equal depth. Because DFS visits the left subtree before the right subtree, the first qualifying node seen at any depth is the leftmost one.

6. **Return the recorded value.**  
   Initialize `bestValue = -1`, `bestDepth = -1`. Since leaves always qualify, any non-empty tree will usually produce an answer, but the initialization still keeps the contract explicit and robust.

## 📊 Worked Example
Example: `root = [5,1,7,null,null,4,3]`

| Node | Depth | leftSum | rightSum | Fully Budgeted? | bestDepth | bestValue |
|---|---:|---:|---:|---|---:|---:|
| 1 | 1 | 0 | 0 | Yes | 1 | 1 |
| 4 | 2 | 0 | 0 | Yes | 2 | 4 |
| 3 | 2 | 0 | 0 | Yes, but same depth and to the right | 2 | 4 |
| 7 | 1 | 4 | 3 | No | 2 | 4 |
| 5 | 0 | 1 | 14 | No | 2 | 4 |

Trace:
1. Visit left leaf `1` first; it qualifies at depth `1`.
2. Traverse into node `7`, then its left leaf `4`; it qualifies at depth `2`, replacing `1`.
3. Visit right leaf `3`; it also qualifies at depth `2`, but tie-breaking keeps `4` because it was seen first and is leftmost.
4. Internal nodes `7` and `5` fail the equality check.
5. Final answer: `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is the number of nodes. Each node is visited once, and each visit performs constant work: combining two sums, checking equality, and possibly updating the best answer. At `10^6` nodes this is still linear and practical; at `10^9`, traversal cost itself becomes the limiting factor.

### Space Complexity
`O(h)` auxiliary space for the DFS call stack, where `h` is tree height. In the worst case of a completely skewed tree, this becomes `O(n)`. It can be reduced to iterative postorder traversal with an explicit stack, trading simpler code for stack-safety on deep trees.

## 💡 Key Takeaways
- If a node’s decision depends on complete child aggregates, that is a strong signal for **postorder DFS** rather than preorder or level-order traversal.
- If recomputing a subtree property from every node sounds natural but expensive, look for a **single bottom-up pass** that returns the needed aggregate.
- Tie-breaking by “leftmost at deepest depth” is easy to get wrong; left-first traversal plus updating only on **strictly greater depth** preserves the correct winner.
- Be precise about what counts toward the equality check: compare **left subtree sum vs right subtree sum**, not sums including the current node.
- The production-grade insight is to fuse **aggregation and selection** into one traversal; separate passes often add avoidable cost and state complexity.

## 🚀 Variations & Further Practice
- Return the **node reference** or path instead of the value; same traversal, but the result state must preserve identity and tie-breaking metadata.
- Generalize from binary trees to **N-ary trees**, where “balanced” might mean all child-subtree sums are equal; the conceptual twist is comparing a variable number of aggregates.
- Find the **highest-value** fully budgeted node among the deepest candidates; now depth is the primary key and node value is a secondary key, which changes update logic but not the traversal pattern.