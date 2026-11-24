# Find the Deepest Complete Team Subtree

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, Binary Tree

---

## 🗂 Problem Overview
Given the root of a binary tree, return the node value at the root of the deepest subtree where every node has either exactly 0 children or exactly 2 children. A leaf is therefore valid, but any node with only one child invalidates its entire subtree. If several valid subtree roots occur at the same maximum depth, return the smallest node value. The challenge is doing this in one traversal for trees with up to 100,000 nodes.

## 🌍 Engineering Impact
This pattern shows up anywhere hierarchical structures must satisfy local structural invariants while still supporting global selection. Examples include validating compiler AST fragments, checking balanced execution branches in workflow engines, verifying binary decision diagrams, and identifying structurally safe subplans in query optimizers. At scale, naive “recompute subtree validity from every node” approaches collapse into quadratic behavior and become unusable on deep or skewed trees. A bottom-up traversal turns local validity into a composable property, enabling linear-time validation, ranking, and extraction of the deepest structurally sound region without repeated scans.

## 🔍 Problem Statement
You are given a binary tree with `1 <= n <= 100000` nodes. Each node has a unique integer value in `[-10^9, 10^9]`. A subtree rooted at node `x` is considered complete under this problem’s definition if every node inside that subtree has either zero children or two children; no node may have exactly one child.

Return the value of the root node of the deepest valid complete subtree. Depth is measured in the original tree, with the overall root at depth `0`. If multiple valid subtree roots share the same maximum depth, return the smallest node value.

Examples:

- `root = [8,4,10,2,6,9,null,null,null,5,7]` → `6`
- `root = [12,7,20,3,null,15,25]` → `3`

The key constraint is `n = 100000`: this rules out checking subtree validity independently from every node and forces a linear-time tree DP / DFS approach.

## 🪜 How to Solve This
1. Read the condition carefully → subtree validity depends entirely on descendants, not ancestors. That strongly suggests a bottom-up traversal.

2. Ask what each parent needs from its children → only whether each child subtree is valid complete, plus whether the parent has 0, 1, or 2 children.

3. Notice the local rule:
   - leaf → valid
   - two children + both child subtrees valid → valid
   - exactly one child → invalid

4. That means each node can return a single boolean: “my subtree is complete.” No expensive subtree rescans are needed.

5. While traversing, also track candidate answers globally:
   - current node depth
   - whether this node roots a valid subtree
   - best depth seen so far
   - smallest value among roots at that depth

6. Use post-order DFS because children must be classified before the parent can be classified.

7. The result is a classic tree DP: compute validity once per node, update the best answer once per valid node, and finish in linear time.

## 🧩 Algorithm Walkthrough
1. **Use post-order DFS (Tree DP / bottom-up DFS).**  
   Visit left subtree, then right subtree, then evaluate the current node. This is the correct abstraction because a node’s validity depends only on already-computed child results.

2. **Pass depth as a parameter.**  
   Depth is defined in the original tree, so when descending from parent to child, increment depth by one. This avoids a second traversal just to annotate depths.

3. **For each node, compute child existence and child validity.**  
   Let `hasLeft`, `hasRight`, `leftValid`, and `rightValid` describe the local state. The invariant is: after both recursive calls return, each child’s subtree classification is final.

4. **Classify the current subtree.**  
   - If no children exist, the node is a valid complete subtree root.  
   - If both children exist and both child subtrees are valid, the current subtree is valid.  
   - Otherwise, it is invalid because some node in this subtree has exactly one child or an invalid descendant.

5. **Update the global best answer only for valid roots.**  
   If current depth is greater than the best depth, replace the answer. If depths tie, keep the smaller node value. This maintains the invariant that the global answer is always the deepest valid root seen so far, with deterministic tie-breaking.

6. **Return validity upward.**  
   Parents do not need subtree size, height, or node count—only whether this subtree satisfies the “0 or 2 children everywhere” rule.

7. **Handle recursion depth in production-quality implementations.**  
   On a skewed tree of 100,000 nodes, recursive DFS may overflow the call stack in some languages. Use an explicit stack for iterative post-order if the runtime environment has limited recursion support.

## 📊 Worked Example
Example: `root = [8,4,10,2,6,9,null,null,null,5,7]`

| Node | Depth | Left Valid | Right Valid | Complete? | Best Answer After |
|---|---:|---:|---:|---:|---|
| 2 | 2 | - | - | Yes | `(2, 2)` |
| 5 | 3 | - | - | Yes | `(3, 5)` |
| 7 | 3 | - | - | Yes | `(3, 5)` |
| 6 | 2 | Yes | Yes | Yes | `(3, 5)` |
| 4 | 1 | Yes | Yes | Yes | `(3, 5)` |
| 9 | 2 | - | - | Yes | `(3, 5)` |
| 10 | 1 | Yes | No child only | No | `(3, 5)` |
| 8 | 0 | Yes | No | No | `(3, 5)` |

Deepest valid roots are leaves `5` and `7` at depth `3`, so strict interpretation returns `5`. If a variant prefers deepest non-leaf valid subtree, that would be `6`. The algorithm itself is driven entirely by the stated tie-break rule.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each node is visited once, and each visit performs constant-time work: combine two child validity flags, inspect child presence, and possibly update the best answer. At `10^6` nodes this remains practical; at `10^9`, linear work is still too large, but asymptotically optimal because every node must be examined.

### Space Complexity
`O(h)` auxiliary space for DFS stack, where `h` is tree height. In a balanced tree, that is `O(log n)`; in a skewed tree, `O(n)`. The space is owned by recursion or an explicit traversal stack. You can avoid call-stack risk with iterative post-order, but not the worst-case height bound.

## 💡 Key Takeaways
- If subtree validity depends only on descendant structure, think bottom-up DFS rather than checking each root independently.
- When a parent needs only a compact summary from children, that is a strong signal for tree DP with a small return state.
- The tie-break is on root depth first, then node value; do not accidentally compare subtree height instead of original-tree depth.
- A leaf is valid by definition; forgetting that base case causes valid larger subtrees with two leaf children to be rejected.
- In production tree processing, encode local invariants into composable summaries so global selection can be done in one pass.

## 🚀 Variations & Further Practice
- Return the **largest** complete subtree by node count instead of the deepest root; this adds subtree-size aggregation and changes the ranking metric.
- Generalize from binary trees to **k-ary trees**, where every node must have either `0` or `k` children; the same bottom-up pattern applies but child-state aggregation widens.
- Find the deepest subtree satisfying a richer predicate, such as **height-balanced and full**; this requires returning multiple attributes per node and composing them carefully.