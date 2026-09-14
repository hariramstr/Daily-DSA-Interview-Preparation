# Maximum Alternating Level Sum in a Binary Tree

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, Dynamic Programming

---

## 🗂 Problem Overview
Given a binary tree, compute for every node the alternating sum of its entire subtree: add nodes at even distance from that node, subtract nodes at odd distance, then continue alternating by depth. Return the maximum such value across all nodes. The challenge is scale: with up to 100000 nodes, recomputing each subtree independently is too expensive. The problem requires a single-pass or linear-time tree DP that reuses subtree results instead of re-traversing descendants.

## 🌍 Engineering Impact
This pattern shows up anywhere hierarchical aggregates depend on parity, phase, or alternating contribution by depth. Examples include compiler IR cost propagation, search-ranking feature rollups over category trees, access-control inheritance with override layers, and streaming pipelines that alternate weighting across stages. At small scale, naive recomputation is tolerable; at 100k+ nodes or under repeated evaluation, it becomes a latency and CPU sink. Tree DP turns repeated subtree scans into local composition, which is the difference between a system that degrades quadratically under skewed hierarchies and one that remains predictably linear.

## 🔍 Problem Statement
You are given the root of a valid binary tree with `1 <= n <= 100000` nodes, and each node value lies in `[-100000, 100000]`. For any node `x`, define its alternating level sum over its full subtree as:

- add `x.val`
- subtract the values of nodes at distance `1`
- add the values of nodes at distance `2`
- continue alternating signs by depth

You must evaluate this quantity for every node and return the maximum value.

Examples:

- `root = [5,2,4,1,3,null,6]` → answer `9`
- `root = [-3,7,2,-5,1]` → answer `11`

Leaves are valid candidates, so the answer is at least the maximum node value. Negative values matter: a parent’s alternating sum can increase when subtracting negative children. The key constraint is `n = 100000`, which rules out `O(n^2)` subtree recomputation and forces an `O(n)` dynamic-programming traversal.

## 🪜 How to Solve This
1. Read the definition carefully → each node’s score depends on its entire subtree, not on a root-to-leaf path and not on a global tree level.

2. Write the expression for a node `x`:
   `alt(x) = x.val - alt-level-1 + alt-level-2 - ...`
   Now look at a child `c`: from `x`’s perspective, everything inside `c`’s subtree flips sign once.

3. That observation suggests a recurrence:
   if `dp(c)` is the alternating sum rooted at `c`, then `c` contributes `-dp(c)` to `x`.

4. So the full relation becomes:
   `dp(x) = x.val - dp(left) - dp(right)`
   with missing children treated as zero.

5. Once that recurrence is visible, the problem collapses into postorder tree DP:
   compute children first, then compute the parent.

6. While computing `dp(x)`, update a global maximum over all nodes. No second pass is needed.

7. The reason this works is structural reuse: every subtree is summarized once by a single scalar, and parents consume only that summary. That is exactly the signature of bottom-up dynamic programming on trees.

## 🧩 Algorithm Walkthrough
1. **Use the Tree DP / Postorder DFS pattern.**  
   This is the right abstraction because each node’s answer depends only on completed answers from its children. Postorder guarantees those child summaries exist before the parent is processed.

2. **Define the DP state.**  
   Let `dp(node)` be the alternating level sum of the subtree rooted at `node`, measured relative to `node` itself. For `null`, define the value as `0`. This makes the recurrence uniform and removes branchy special cases.

3. **Derive the recurrence.**  
   For a node `u`, its own value is added with positive sign. Every node in a child subtree is one level deeper relative to `u` than relative to that child, so every sign flips. Therefore:  
   `dp(u) = u.val - dp(u.left) - dp(u.right)`  
   This is correct because each child DP already encodes the full alternating contribution of that subtree from the child’s frame.

4. **Traverse bottom-up.**  
   Recurse into left and right, compute their `dp`, then compute the current node’s `dp`. The invariant is: after returning from a node, `dp(node)` is final and includes the entire subtree exactly once.

5. **Track the maximum during computation.**  
   Every node is a candidate root for the requested score, so after computing `dp(u)`, update `best = max(best, dp(u))`. This maintains the invariant that `best` is the maximum over all fully processed nodes.

6. **Handle scale concerns.**  
   The algorithm is `O(n)`, but a highly skewed tree can cause recursion depth issues in some languages. In production-grade implementations, use an explicit stack for iterative postorder if stack limits are tight.

## 📊 Worked Example
Example: `root = [-3,7,2,-5,1]`

Postorder trace:

| Node | left dp | right dp | dp(node) = val - left - right | best |
|---|---:|---:|---:|---:|
| -5 | 0 | 0 | -5 | -5 |
| 1 | 0 | 0 | 1 | 1 |
| 7 | -5 | 1 | 7 - (-5) - 1 = 11 | 11 |
| 2 | 0 | 0 | 2 | 11 |
| -3 | 11 | 2 | -3 - 11 - 2 = -16 | 11 |

Interpretation:

1. Leaves evaluate to their own values.
2. Node `7` benefits from subtracting its children’s DP values; subtracting `-5` increases the result.
3. Root `-3` becomes strongly negative because both child subtrees reduce it.
4. The maximum over all node-rooted subtree scores is `11`, achieved at node `7`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each node is visited once, and the work per node is constant: combine two child results, compute one recurrence, update one maximum. At `10^6` nodes this is still linear and practical; at `10^9`, even linear traversal becomes infrastructure-bound, but the algorithm remains asymptotically optimal.

### Space Complexity
`O(h)` for DFS stack depth, where `h` is tree height; worst case `O(n)` for a skewed tree, `O(log n)` for a balanced tree. No auxiliary DP table is required beyond call stack or an explicit traversal stack. Iterative postorder trades recursion risk for explicit stack management.

## 💡 Key Takeaways
- If each node asks for a value over its entire subtree and parents can be expressed from child summaries, think bottom-up tree DP immediately.
- Alternating-by-depth or parity-flip language is a strong signal that child contributions may invert sign in the parent recurrence.
- The recurrence is `node.val - leftDP - rightDP`, not `node.val + leftDP + rightDP`; the sign flip is the whole problem.
- Do not confuse “maximum alternating sum” with a path problem; every candidate uses the full subtree, not a selectable subset.
- The transferable design insight is to compress expensive hierarchical recomputation into a stable local summary that composes upward exactly once.

## 🚀 Variations & Further Practice
- **N-ary tree version:** same recurrence idea, but aggregate over an arbitrary number of children; the harder part is designing iterative traversal cleanly for wide fan-out.
- **Dynamic updates on the tree:** support node-value changes and repeated max queries; this shifts the problem from static tree DP to dynamic tree data structures or recomputation strategies.
- **Weighted sign schedules:** instead of `+/-/+/-`, use a periodic coefficient pattern by depth; the twist is that one scalar DP no longer suffices, and each node may need a vector state per phase.