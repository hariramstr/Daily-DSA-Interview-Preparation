# Maximum Root-to-Leaf Budget With One Department Freeze

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Tree, Depth-First Search, Dynamic Programming

---

## 🗂 Problem Overview
Given a binary tree where each node contributes a positive or negative budget value, compute the maximum root-to-leaf path sum when you may skip exactly one node’s value on that path by treating it as `0`. Using the skip is optional. The output is a single maximum total. The non-trivial part is that the decision to freeze a node is path-dependent, so a greedy local choice is not sufficient, especially with negative values and up to `100000` nodes.

## 🌍 Engineering Impact
This pattern shows up in hierarchical scoring and fault-tolerant aggregation: search ranking trees with one tolerated bad signal, compiler optimization passes that allow one suppressed penalty, streaming pipelines that can discount one corrupt stage contribution, or org/cost rollups where one exceptional adjustment is permitted. At scale, brute-force path enumeration collapses immediately because root-to-leaf combinations grow with tree size. The useful abstraction is stateful traversal: carry a small, explicit state machine through the hierarchy. That enables linear-time evaluation, predictable memory behavior, and a design that generalizes cleanly to “at most k exceptions” policies.

## 🔍 Problem Statement
You are given a binary tree with `1 <= n <= 100000` nodes. Each node stores an integer budget contribution in `[-100000, 100000]`. A valid reporting chain is any path from the root to a leaf. You may apply at most one department freeze on the chosen path, replacing exactly one node’s contribution with `0`; you may also choose not to freeze any node.

Return the maximum achievable root-to-leaf total.

Important edge cases:
- The tree may contain all negative values.
- The best answer may come from not using the freeze at all.
- The freeze may be applied to the root, an internal node, or a leaf.
- The answer fits in a signed 64-bit integer.

Examples:
- `root = [5,4,8,-3,null,6,2]` → `19`
- `root = [7,-5,3,4,2,-6,9]` → `19`

The key constraint is `100000` nodes: this rules out recomputing path sums or exploring freeze choices separately for each path.

## 🪜 How to Solve This
1. Read the problem → this is a root-to-leaf optimization problem on a tree, so DFS is the natural traversal.
2. Notice the twist → each path has two modes: freeze not used yet, or freeze already used. That immediately suggests dynamic programming with two states.
3. Ask what each state should mean → for any node, we want the best root-to-leaf sum starting here:
   - if no freeze has been used yet
   - if the freeze has already been used somewhere above or at this node
4. Work bottom-up → a node’s best answer depends only on its children’s best answers, so postorder DFS fits.
5. Define transitions:
   - If freeze is already used, we must include this node’s value and continue.
   - If freeze is still available, either include this node normally, or freeze this node and continue with the “used” state.
6. Handle leaves carefully → a leaf is a complete path, so its two states are easy base cases.
7. Final answer → the root’s “freeze still available” state already covers both “use it somewhere” and “never use it.”

Once you see “tree path + one optional exception,” the two-state DFS becomes the obvious compression of all path choices.

## 🧩 Algorithm Walkthrough
1. **Use Tree DP with DFS.**  
   The pattern is **Depth-First Search + Dynamic Programming on Trees**. Each node returns two values:
   - `noFreeze`: best root-to-leaf sum from this node downward when the freeze is still available.
   - `usedFreeze`: best root-to-leaf sum from this node downward when the freeze has already been consumed.  
   This is the right abstraction because the only historical fact that matters is whether the one allowed freeze remains available.

2. **Define leaf base cases.**  
   For a leaf with value `v`:
   - `usedFreeze = v` because we must include it if the freeze was used earlier.
   - `noFreeze = max(v, 0)` because we may either keep the leaf or freeze it now.  
   This preserves the invariant that each state represents a complete root-to-leaf path.

3. **Combine child results for internal nodes.**  
   Let the best child values be the maximum over existing children only:
   - `bestChildNoFreeze`
   - `bestChildUsedFreeze`  
   Then:
   - `usedFreeze = node.val + bestChildUsedFreeze`
   - `noFreeze = max(node.val + bestChildNoFreeze, 0 + bestChildUsedFreeze)`  
   The first term means we do not freeze this node. The second means we freeze this node now and continue with children in the “already used” state.

4. **Ignore missing children safely.**  
   This is root-to-leaf, not arbitrary path sum. You cannot terminate early at an internal node just because one branch is bad. Only leaves end a path, so null children should behave like invalid options, not zero.

5. **Return the root’s `noFreeze`.**  
   That state includes both possibilities: never use the freeze, or use it at the best location.

For very deep trees, prefer an iterative postorder traversal to avoid stack overflow in languages with limited recursion depth.

## 📊 Worked Example
Example: `root = [7,-5,3,4,2,-6,9]`

Process bottom-up.

| Node | Left `(noFreeze, usedFreeze)` | Right `(noFreeze, usedFreeze)` | Result `(noFreeze, usedFreeze)` |
|---|---:|---:|---:|
| 4 | — | — | `(4, 4)` |
| 2 | — | — | `(2, 2)` |
| -6 | — | — | `(0, -6)` |
| 9 | — | — | `(9, 9)` |
| -5 | `(4,4)` | `(2,2)` | `max(-5+4, 0+4)=4`, `-5+4=-1` → `(4, -1)` |
| 3 | `(0,-6)` | `(9,9)` | `max(3+9, 0+9)=12`, `3+9=12` → `(12, 12)` |
| 7 | `(4,-1)` | `(12,12)` | `max(7+12, 0+12)=19`, `7+12=19` → `(19, 19)` |

Answer: `19`.

Interpretation: the best path is `7 -> 3 -> 9`, and the freeze is unnecessary.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is the number of nodes. Each node is visited once, and each visit performs constant-time work: combine up to two child state pairs and compute two values. At `10^6` nodes this is still practical in a compiled language; at `10^9`, tree materialization itself is the real bottleneck, not the recurrence.

### Space Complexity
`O(h)` for recursive DFS call stack, where `h` is tree height; worst case `O(n)` for a skewed tree. An iterative postorder version also uses `O(h)` to `O(n)` depending on implementation. The DP itself is constant per node if computed on the fly.

## 💡 Key Takeaways
- If a tree-path problem says “at most one special action/exception,” that is a strong signal for DFS with a small finite state per node.
- If the result is constrained to root-to-leaf paths, think in terms of path-complete states, not arbitrary subtree aggregates.
- Do not treat null children as contributing `0`; that would incorrectly allow paths to terminate before reaching a leaf.
- For leaves, `noFreeze = max(value, 0)` is the critical base case; forgetting the optional freeze there breaks all-negative edge cases.
- In production systems, this is the scalable pattern for hierarchical optimization under a bounded exception budget: encode the exception count as state, not as branching search.

## 🚀 Variations & Further Practice
- Allow **up to `k` freezes** instead of one. The conceptual twist is expanding the DP state from 2 modes to `k+1`, turning the recurrence into tree knapsack-style state propagation.
- Maximize the best **root-to-any-node** path with one freeze. The harder part is that termination is no longer restricted to leaves, so null-handling and base conditions change.
- Generalize from a binary tree to a **DAG** of dependency rollups. The same state idea works, but you now need topological DP and must handle shared subproblems explicitly.