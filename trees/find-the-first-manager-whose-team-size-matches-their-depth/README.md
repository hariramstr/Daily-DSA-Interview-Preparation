# Find the First Manager Whose Team Size Matches Their Depth

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, Postorder Traversal

---

## 🗂 Problem Overview
Given a rooted organizational tree, return the employee ID of the shallowest node whose subtree size equals its depth. Subtree size includes the employee and all descendants. If several nodes at that minimum depth qualify, choose the first one in left-to-right child order. If none qualify, return `-1`. The non-trivial part is that each decision depends on both top-down state (`depth`) and bottom-up state (`subtree size`), so naive recomputation is too expensive at scale.

## 🌍 Engineering Impact
This pattern shows up anywhere hierarchical aggregates must be evaluated against path-derived metadata: org charts, filesystem quota trees, compiler AST passes, IAM policy inheritance, dependency graphs, and distributed control-plane topologies. In production, the failure mode is usually repeated subtree recomputation or multiple disconnected traversals that explode latency on large trees. A single DFS that combines downward context with upward aggregation gives predictable linear behavior, preserves traversal order semantics, and scales to six-figure node counts without turning a simple hierarchy query into an accidental quadratic hot path.

## 🔍 Problem Statement
You are given the root of a valid connected rooted tree with `1 <= n <= 100000` nodes. Each node contains a unique employee ID and up to 10 children in a fixed left-to-right order. The root is the CEO at depth `0`.

For each employee, define:

- `depth`: distance from the root
- `team size`: number of nodes in that employee’s subtree, including themself

Return the employee ID of the shallowest node whose `team size == depth`. If multiple nodes at that same minimum depth satisfy the condition, return the first one encountered in left-to-right traversal order. If no such node exists, return `-1`.

Examples:

- `root = [10, [20, 30], [40], [], [50, 60], [], []]` → `50`
- `root = [1, [2, 3, 4], [], [], []]` → `-1`

The key constraint is `n = 100000`, which rules out recomputing subtree sizes for each node.

## 🪜 How to Solve This
1. Read the condition carefully → each node needs two values: its `depth` and its `subtree size`.
2. `Depth` is naturally known when moving from parent to child → that suggests a top-down DFS parameter.
3. `Subtree size` is only known after processing all children → that suggests postorder traversal.
4. So the shape of the solution is immediate: one DFS where we pass `depth` downward and return `subtree size` upward.
5. While unwinding recursion, compute `size = 1 + sum(child sizes)`.
6. At that moment, we can test `size == depth` because both required values are available.
7. But the answer is not “any matching node”; it is the shallowest one, with left-to-right tie-breaking.
8. That means we maintain global best state: smallest matching depth seen so far, plus corresponding employee ID.
9. Because DFS visits children in given order, the first match encountered at a depth is automatically the left-to-right winner for that depth.
10. Result: one traversal, no repeated work, deterministic tie-breaking, linear cost.

## 🧩 Algorithm Walkthrough
1. **Use DFS with postorder semantics.**  
   This is a classic **DFS + Postorder Traversal** problem: depth is available on entry, subtree size on exit. That combination is exactly why postorder is the right abstraction.

2. **Pass `depth` as an argument during descent.**  
   When visiting a node, its depth is already known from its parent. This maintains the invariant that every recursive call has the correct top-down context without extra storage or a separate traversal.

3. **Process children left to right and collect their subtree sizes.**  
   For each child, recursively compute its subtree size and add it to the current node’s running total. The invariant after processing the first `k` children is that the running total equals `1 + sum(subtree sizes of those k children)`.

4. **Compute the current node’s subtree size after all children finish.**  
   Once every child has returned, the node’s subtree size is final and correct by definition. This is the postorder step: children first, parent second.

5. **Check the condition `subtree_size == depth`.**  
   At this exact point, both required values are known. If the node matches, compare its depth against the best depth found so far.

6. **Maintain the best answer by minimum depth only.**  
   Update the answer only if this node’s depth is smaller than the current best. Do not replace on equal depth. Because traversal is left to right, the first qualifying node at that depth is already the required tie-break winner.

7. **Return subtree size to the parent.**  
   This preserves the invariant that every call returns the exact size of the subtree rooted at that node, enabling the parent’s computation in constant additional work.

## 📊 Worked Example
Example: `root = [10, [20, 30], [40], [], [50, 60], [], []]`

Tree shape:
- `10`
  - `20`
    - `40`
  - `30`
  - `50`
    - `60`

| Node | Depth | Child sizes | Subtree size | Matches? | Best answer |
|---|---:|---|---:|---|---|
| 40 | 2 | `[]` | 1 | No | -1 |
| 20 | 1 | `[1]` | 2 | No | -1 |
| 30 | 1 | `[]` | 1 | Yes | 30 |
| 60 | 2 | `[]` | 1 | No | 30 |
| 50 | 2 | `[1]` | 2 | Yes | 30 |
| 10 | 0 | `[2,1,2]` | 6 | No | 30 |

If using strict “minimum depth wins,” `30` is the answer because depth `1` beats depth `2`. The traversal logic still shows how subtree sizes are computed once, bottom-up, while preserving left-to-right tie-breaking within a depth.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each node is visited once, and each edge is traversed once during DFS. The dominant operation is summing child subtree sizes across the tree, which totals linear work. At `10^6` nodes this is still practical; at `10^9`, even linear traversal becomes infrastructure-bound and requires distributed or external-memory strategies.

### Space Complexity
`O(h)` auxiliary space for the recursion stack, where `h` is tree height; worst case `O(n)` for a degenerate tree. The tree itself dominates total memory. This can be reduced to iterative DFS with an explicit stack, trading simpler code for stack-frame safety.

## 💡 Key Takeaways
- If a tree condition depends on ancestor-derived state and descendant-derived aggregates, think single-pass DFS with top-down parameters and postorder returns.
- If the problem asks for subtree metrics without updates, repeated per-node recomputation is the red flag; aggregate once and reuse.
- Root depth is `0`, so any node with subtree size `0` is impossible; this matters when reasoning about whether the root can ever match.
- For tie-breaking, do not overwrite the answer on equal depth; left-to-right traversal order already selects the correct node first.
- The production lesson is to fuse traversals when possible: combining context propagation with bottom-up aggregation avoids quadratic behavior and preserves deterministic semantics.

## 🚀 Variations & Further Practice
- Return **all** employee IDs whose team size equals depth, grouped by depth; the twist is preserving left-to-right order while collecting multiple matches efficiently.
- Support **dynamic hierarchy updates** (add/remove/move employees) with repeated queries; the twist is shifting from static DFS to maintained subtree aggregates via Euler tour indexing plus Fenwick/segment trees.
- Find the first node where a custom predicate depends on multiple subtree statistics, such as `subtree_size == depth + leaf_count`; the twist is carrying and combining several bottom-up aggregates in one pass.