# Find the Largest Uniform-Value Subtree

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Depth-First Search, Postorder Traversal

---

## 🗂 Problem Overview
Given the root of a binary tree, return the number of nodes in the largest subtree whose nodes all share the same integer value. Every node defines a candidate subtree rooted at itself, and a single node is always uniform. The challenge is to evaluate all rooted subtrees without repeatedly scanning descendants, because the tree can contain up to `10^5` nodes and may be highly unbalanced.

## 🌍 Engineering Impact
This pattern shows up anywhere hierarchical state must be summarized bottom-up: filesystem deduplication, compiler AST simplification, feature-flag inheritance trees, search index segment compaction, and distributed policy evaluation. At scale, naive re-validation of each subtree becomes quadratic and collapses under deep or skewed structures. Bottom-up aggregation turns repeated structural checks into a single pass, which is the difference between a routine background computation and a latency or CPU hotspot. More broadly, it enables local summaries that can be composed upward without revisiting already-processed descendants.

## 🔍 Problem Statement
You are given the root of a binary tree with `1 <= n <= 10^5` nodes. Each node stores an integer value in `[-10^9, 10^9]`. A subtree is **uniform** if every node in that subtree has the same value as its root. A subtree consisting of one node is always uniform.

Return the size of the largest uniform subtree.

The tree is binary and may be unbalanced, so any solution that repeatedly scans descendants for each root will time out. The key constraint is the required `O(n)` runtime.

Examples:

- `root = [5,5,5,5,5,null,5]` → `6`  
  Entire tree is uniform, so the answer is all existing nodes.

- `root = [3,3,4,3,null,4,4]` → `3`  
  The largest uniform subtree is rooted at the right child of the root and contains three `4` nodes.

## 🪜 How to Solve This
1. Read the problem → we are not just checking whether the whole tree is uniform; we need the **largest** rooted subtree that is uniform.
2. For any node, whether its subtree is uniform depends entirely on its left subtree, right subtree, and whether both match the node’s value.
3. That dependency points directly to **postorder traversal**: compute child results first, then decide the parent.
4. For each node, we need a compact summary from each child:
   - is the child subtree uniform?
   - how many nodes does it contain if it is uniform?
   - what value does that subtree carry?
5. If both children are uniform and any existing child has the same value as the current node, then the current subtree is also uniform.
6. When a subtree is uniform, its size is `left_size + right_size + 1`; update the global maximum.
7. This avoids re-checking descendants. Every node contributes one summary upward exactly once, giving linear time.

## 🧩 Algorithm Walkthrough
1. **Use postorder DFS** (`left → right → node`).  
   This is the right abstraction because parent validity depends on completed child summaries. Preorder or inorder would force deferred logic or repeated checks.

2. **Define the DFS return value** for each node as:
   - whether the subtree rooted here is uniform
   - the subtree size if it is uniform  
   The node’s own value is already available from the tree node, so no extra propagation is needed beyond correctness checks.

3. **Base case: null child**.  
   Treat a null subtree as uniform with size `0`. This simplifies parent logic: missing children do not invalidate uniformity and contribute nothing to size.

4. **Recurse into left and right children**.  
   After both calls return, you know whether each child subtree is uniform and, if so, how large it is.

5. **Validate the current node**.  
   The current subtree is uniform iff:
   - left subtree is uniform
   - right subtree is uniform
   - if left child exists, `left.val == node.val`
   - if right child exists, `right.val == node.val`  
   This invariant guarantees that every node below the current root shares the same value.

6. **Compute size and update answer**.  
   If uniform, size is `left_size + right_size + 1`. Update the global maximum with this value. If not uniform, return failure upward; the exact non-uniform size is irrelevant because ancestors cannot build a larger uniform subtree through it.

7. **Return the global maximum** after DFS completes.  
   Each node is processed once, each edge is examined once, and all subtree decisions are made from constant-size summaries.

## 📊 Worked Example
Example: `root = [3,3,4,3,null,4,4]`

Postorder trace:

| Node | Left result | Right result | Uniform? | Size | Max |
|---|---|---|---|---:|---:|
| 3 (left-left leaf) | null | null | yes | 1 | 1 |
| 3 (left child) | yes,1 and child=3 | null | yes | 2 | 2 |
| 4 (right-left leaf) | null | null | yes | 1 | 2 |
| 4 (right-right leaf) | null | null | yes | 1 | 2 |
| 4 (right child) | yes,1 and child=4 | yes,1 and child=4 | yes | 3 | 3 |
| 3 (root) | yes,2 and child=3 | yes,3 but child=4 | no | — | 3 |

The root fails because its right child subtree is uniform but has value `4`, not `3`. The largest valid subtree is therefore the right subtree of size `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each node is visited exactly once, and each visit performs constant-time work: combine two child summaries, compare values, and possibly update the global maximum. At `10^6` nodes this is still a single linear pass; at `10^9`, linear work is usually infeasible regardless of constant factors.

### Space Complexity
`O(h)` auxiliary space from recursion depth, where `h` is tree height. In the worst case of a fully skewed tree, `h = n`. This can be reduced to explicit-stack iterative traversal, trading simpler code for stack management complexity.

## 💡 Key Takeaways
- If a tree problem asks whether a parent property depends entirely on child subtree summaries, think postorder DFS immediately.
- “Largest valid subtree” is a strong signal that each node should return a compact validity summary upward while updating a global best.
- Null children should usually be treated as valid identity cases; otherwise parent checks become cluttered and error-prone.
- Do not propagate subtree size upward for non-uniform subtrees as if it were still useful; only uniform sizes can legally compose into larger answers.
- The production-grade insight is bottom-up summarization: compute once per node, cache in the call stack, and compose upward instead of re-walking structure.

## 🚀 Variations & Further Practice
- Find the largest subtree where all nodes have the same value **or differ by at most `k`** from the root; the twist is propagating range summaries instead of a single equality check.
- Find the largest **BST subtree**; same postorder pattern, but the summary must carry min/max bounds and validity.
- Extend to an **N-ary tree** or DAG-like hierarchy with memoization; the harder part is generalizing child aggregation and handling shared substructure safely.