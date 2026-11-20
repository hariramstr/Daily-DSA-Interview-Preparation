# Longest Consecutive Approval Chain in an Org Tree

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, Binary Tree

---

## 🗂 Problem Overview
Given the root of a binary tree, return the length of the longest downward path where each child value is exactly `parent + 1`. The path may start at any node, must follow parent→child edges only, and can end anywhere. An empty tree returns `0`. The non-trivial part is scale: with up to `100000` nodes and an unbalanced tree, the solution must avoid recomputing chain lengths from every possible start node.

## 🌍 Engineering Impact
This pattern shows up anywhere hierarchical state must satisfy a strict local progression rule: approval workflows, IAM inheritance trees, build/dependency pipelines, compiler AST validation, and lineage checks in data platforms. At small scale, brute-force “start from every node” logic is tolerable; at org-sized or graph-shaped hierarchies, it explodes into repeated subtree scans and unpredictable latency. A single-pass tree DP/DFS approach turns local edge validation into global optimization, which is exactly what production systems need: bounded traversal cost, composable invariants, and behavior that remains stable as hierarchies deepen or skew.

## 🔍 Problem Statement
You are given the root of a binary tree where each node stores an integer approval level. A valid approval chain is a downward path along parent-child edges such that each next node has value exactly one greater than the previous node.

The chain:
- may start at any node,
- may end at any node,
- must move only downward,
- must be consecutive by `+1` at every step.

Return the maximum chain length. If the tree is empty, return `0`.

Constraints:
- `0 <= number of nodes <= 100000`
- `-10^9 <= node.val <= 10^9`
- The tree may be highly unbalanced

Examples:

- `root = [3,4,2,5,null,null,3,6]` → `4`  
  Longest chain: `3 -> 4 -> 5 -> 6`

- `root = [10,11,9,12,10,null,null,13]` → `4`  
  Longest chain: `10 -> 11 -> 12 -> 13`

The key constraint is `O(n)` expected time on a potentially skewed tree, which rules out restarting a full search from every node.

## 🪜 How to Solve This
1. Read the problem → the path is constrained to parent→child only, so this is not a general tree path problem. That removes the need for bidirectional path merging.

2. Notice the chain condition is local: a child extends a chain only if `child.val == parent.val + 1`. That suggests carrying just enough state from parent to child.

3. Ask what state matters during traversal → only the current consecutive length ending at this node. If the rule holds, extend by `1`; otherwise reset to `1`.

4. Since the answer may start anywhere, you cannot anchor the search at the root conceptually. Instead, traverse the whole tree and treat every node as a potential chain start when resets happen naturally.

5. This leads directly to DFS with propagated state:
   - pass down the parent value and current chain length,
   - compute this node’s chain length,
   - update a global maximum,
   - recurse into children.

6. Why this works → every valid downward chain is examined exactly once along the traversal path that reaches its endpoint. No subtree needs to be rescanned, so the algorithm stays linear.

## 🧩 Algorithm Walkthrough
1. **Use Depth-First Search with state propagation**.  
   This is a tree DFS / top-down dynamic programming pattern. The state is: previous node value and the length of the current valid chain reaching the current node. This abstraction fits because validity depends only on the immediate parent-child relationship.

2. **Handle the empty-tree case first**.  
   If `root == null`, return `0`. This preserves the contract and avoids special-casing inside traversal.

3. **At each node, decide extend vs reset**.  
   If `node.val == prevVal + 1`, then `currLen = prevLen + 1`; otherwise `currLen = 1`.  
   Why correct: any valid chain ending at this node must either extend the parent’s chain by exactly one step or begin fresh here.

4. **Update the global maximum**.  
   After computing `currLen`, set `best = max(best, currLen)`.  
   Invariant: after visiting any node, `best` equals the longest valid chain seen in the visited portion of the tree.

5. **Recurse into left and right children with the new state**.  
   Pass `(node.val, currLen)` downward. Each child independently determines whether it can extend the chain. This is sufficient because sibling subtrees do not combine under the downward-only rule.

6. **Visit every node exactly once**.  
   The traversal is linear in node count. No memoization is required because each node’s result depends only on the path state arriving from its parent, and that state is unique in a tree.

7. **Operational note for skewed trees**.  
   Recursive DFS is conceptually simplest, but a tree of depth `100000` can overflow the call stack in many runtimes. An explicit stack preserves the same invariant and asymptotic behavior with better production safety.

## 📊 Worked Example
Example: `root = [3,4,2,5,null,null,3,6]`

Trace using DFS state `(node, prevVal, prevLen)`

| Step | Node | Rule Check | currLen | best |
|---|---:|---|---:|---:|
| 1 | 3 | start node | 1 | 1 |
| 2 | 4 | `4 == 3 + 1` | 2 | 2 |
| 3 | 5 | `5 == 4 + 1` | 3 | 3 |
| 4 | 6 | `6 == 5 + 1` | 4 | 4 |
| 5 | 2 | `2 != 3 + 1` | 1 | 4 |
| 6 | 3 | `3 == 2 + 1` | 2 | 4 |

The longest chain is discovered on the left spine: `3 -> 4 -> 5 -> 6`.  
The right subtree does contain a smaller valid chain `2 -> 3`, but it does not exceed the current maximum. The reset at node `2` is essential: chains are local to parent-child value relationships, not inherited automatically from ancestors.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is the number of nodes. Each node is visited once, and the work per visit is constant: one comparison, one length update, one max update, and child pushes/calls. At `10^6` nodes this is still practical as a single pass; at `10^9`, traversal cost itself becomes the bottleneck regardless of algorithmic optimality.

### Space Complexity
`O(h)` auxiliary space, where `h` is tree height, from the recursion stack or explicit DFS stack. In the worst-case skewed tree, `h = n`. This cannot be reduced below traversal frontier size without changing execution model; iterative DFS avoids call-stack risk but not asymptotic space.

## 💡 Key Takeaways
- If a tree problem asks for a path constrained by a parent-child local rule, think top-down DFS with propagated state before considering heavier tree DP.
- If the path may start anywhere but only moves downward, resets during traversal usually replace the need to launch a search from every node.
- Do not require the chain to start at the root; a broken edge means the current node becomes a new chain of length `1`.
- Be careful with the comparison: the rule is exactly `child == parent + 1`, not greater-than, non-decreasing, or absolute difference `1`.
- In production code, local invariants propagated in one pass are often the difference between predictable linear behavior and repeated subtree work that collapses under skewed hierarchies.

## 🚀 Variations & Further Practice
- **Longest consecutive path that can go increasing or decreasing through a node**: harder because the best answer may combine left and right subtree information at a pivot, requiring bottom-up aggregation.
- **N-ary org tree instead of binary**: same core idea, but traversal generalizes to an arbitrary child list and raises practical concerns around iteration order and memory locality.
- **Return the actual chain, not just its length**: adds path reconstruction, which requires predecessor tracking or rebuilding from stored endpoints rather than only maintaining scalar DP state.