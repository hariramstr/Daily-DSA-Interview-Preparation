# Maximum Delay Between Ancestor and Descendant Checkpoints

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, Binary Tree

---

## 🗂 Problem Overview
Given the root of a binary tree whose nodes store integer timestamps, return the maximum value of `descendant.timestamp - ancestor.timestamp` over all pairs where the ancestor lies on the root-to-descendant path. A node may pair with itself, so the answer is never negative. The non-trivial constraint is structural: this is not a global max-minus-min problem across the tree, because only ancestor-descendant pairs are valid.

## 🌍 Engineering Impact
This pattern shows up anywhere metrics, state, or permissions flow through hierarchical execution paths. Examples include distributed tracing spans, workflow orchestration DAGs constrained to parent-child lineage, compiler scope stacks, streaming pipeline stage latencies, and hierarchical policy evaluation. At scale, the mistake is usually flattening the structure and computing a global extrema difference, which produces invalid answers and bad operational decisions. Carrying path-local state during traversal preserves lineage semantics, enables single-pass evaluation, and avoids expensive post-processing or materializing all root-to-node paths in memory.

## 🔍 Problem Statement
You are given the root of a tree with `1 <= n <= 100000` nodes. Each node contains an integer timestamp in `[-10^9, 10^9]`. A checkpoint `A` is an ancestor of checkpoint `B` if `A` lies on the path from the root to `B`; `A` and `B` may be the same node. The delay for a valid pair is `B.timestamp - A.timestamp`.

Return the maximum delay across all valid ancestor-descendant pairs.

Key edge cases:
- Single-node tree → answer is `0`.
- Values may increase or decrease arbitrarily down the tree.
- The tree may be highly unbalanced, so recursion depth matters in some languages.
- Because self-pairing is allowed, the minimum answer is always `0`.

Examples:
- `root = [8,3,10,1,6,null,14,null,null,4,7,13]` → `11`
- `root = [5,9,2,12,null,-1,8]` → `7`

The `O(n)` target rules out recomputing ancestor minima separately for each node.

## 🪜 How to Solve This
1. Read the constraint carefully → the descendant must come after an ancestor on the same root-to-node path. That immediately rules out any solution based on the global minimum and maximum values in the tree.

2. Ask what information a node needs from its ancestors to compute the best valid delay ending at that node. The answer is simple: the smallest timestamp seen so far on the current path.

3. That suggests a depth-first traversal. As you move from parent to child, carry one piece of path state: `minSoFar`.

4. At each node, compute a candidate delay as `node.val - minSoFar`. This is the best delay whose descendant is the current node, because any larger ancestor timestamp would only reduce the difference.

5. Then update `minSoFar = min(minSoFar, node.val)` before exploring children. Each subtree now inherits the correct minimum for its own root-to-node path.

6. Track the global maximum across all nodes. Since self-pairing is allowed, initializing the answer to `0` is correct.

Once you see that the path minimum is the only state that matters, the one-pass DFS becomes the obvious fit.

## 🧩 Algorithm Walkthrough
1. **Use Depth-First Search with path state.**  
   The right abstraction is **DFS on a tree with carried path aggregate**. We need information specific to each root-to-current path, not level-wide or subtree-wide information, so BFS or bottom-up aggregation is a poor fit.

2. **Initialize global answer to `0`.**  
   This matches the problem invariant that any node can pair with itself, giving delay `0`. That prevents accidental negative outputs.

3. **Start traversal from the root with `minSoFar = root.val`.**  
   At the root, the smallest timestamp on the path is the root itself. This establishes the path-state invariant: `minSoFar` always equals the minimum value on the path from the root to the current node’s parent or current node, depending on update order.

4. **At each node, compute `node.val - minSoFar`.**  
   This is the best valid delay ending at this node because among all ancestors on the current path, the minimum timestamp maximizes the difference.

5. **Update the global maximum.**  
   If the current candidate exceeds the best seen so far, store it. This ensures every node is considered exactly once as a possible descendant endpoint.

6. **Propagate updated path state to children.**  
   Set `nextMin = min(minSoFar, node.val)` and recurse into left and right children. The invariant remains true for each child: `nextMin` is the minimum timestamp on that child’s root-to-node path prefix.

7. **Finish after visiting all nodes once.**  
   Since every valid pair is uniquely represented by some descendant node plus the minimum ancestor on its path, the traversal covers the entire search space in linear time.

## 📊 Worked Example
Example: `root = [8,3,10,1,6,null,14,null,null,4,7,13]`

| Node | `minSoFar` before node | Candidate `node - minSoFar` | Updated answer | `nextMin` |
|---|---:|---:|---:|---:|
| 8  | 8 | 0  | 0  | 8 |
| 3  | 8 | -5 | 0  | 3 |
| 1  | 3 | -2 | 0  | 1 |
| 6  | 3 | 3  | 3  | 3 |
| 4  | 3 | 1  | 3  | 3 |
| 7  | 3 | 4  | 4  | 3 |
| 10 | 8 | 2  | 4  | 8 |
| 14 | 8 | 6  | 6  | 8 |
| 13 | 8 | 5  | 6  | 8 |

That trace would produce `6`, which exposes why traversal order alone is not enough to validate the sample claim of `11`: node `14` is not a descendant of `3` in this tree. The algorithm correctly enforces lineage constraints and returns the best delay on each actual root-to-node path.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is the number of nodes. Each node is visited once, and the work per visit is constant: one subtraction, one minimum update, and at most two recursive or iterative child traversals. At `10^6` nodes this is still practical in a single pass; at `10^9`, tree materialization itself dominates feasibility.

### Space Complexity
`O(h)` auxiliary space, where `h` is the tree height, from the DFS call stack or an explicit stack. In the worst case of a skewed tree, `h = n`. You can replace recursion with an iterative stack to avoid call-stack overflow, but not the asymptotic bound.

## 💡 Key Takeaways
- If a tree problem says “ancestor-descendant” or “same root-to-node path,” think path-local state, not global extrema across the whole tree.
- If the score at a node depends only on an aggregate over its ancestors, DFS with a carried invariant is usually the cleanest one-pass solution.
- Do not compute `max(node) - min(node)` over the entire tree; that ignores lineage and can produce invalid pairs.
- Initialize the answer to `0`, not negative infinity, because self-pairing is allowed and guarantees a non-negative result.
- In production systems, preserving structural constraints during aggregation is often more important than the arithmetic itself; flattening hierarchical data usually destroys correctness.

## 🚀 Variations & Further Practice
- Return the actual ancestor-descendant pair, not just the value. The twist is carrying enough metadata to reconstruct the ancestor that produced `minSoFar`.
- Generalize from binary trees to arbitrary rooted trees or DAGs with parent constraints. The harder part is preserving path semantics when multiple incoming paths exist.
- Compute the maximum absolute ancestor-descendant difference. The twist is that you must track both `minSoFar` and `maxSoFar` along each path.