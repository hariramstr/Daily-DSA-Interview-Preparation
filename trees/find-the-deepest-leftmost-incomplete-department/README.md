# Find the Deepest Leftmost Incomplete Department

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, Binary Tree

---

## 🗂 Problem Overview
Given a binary tree, return the value of the node that has exactly one child, is located at the greatest depth, and is leftmost among all such deepest candidates. If no node has exactly one child, return `-1`. The challenge is not detecting incompleteness locally — that is trivial — but enforcing the global ranking correctly: deepest depth wins first, and left-to-right position only breaks ties at that depth.

## 🌍 Engineering Impact
This pattern shows up anywhere hierarchical structures must be scanned for the deepest structurally anomalous node: org charts, filesystem trees, compiler AST validation, search index shard hierarchies, workflow DAG projections constrained into binary form, and routing or decision trees. At scale, the issue is rarely “does a node violate a local rule?” but “which violation should drive remediation or alerting?” Without a traversal that preserves depth and left-to-right semantics, systems produce unstable results, non-deterministic diagnostics, or expensive post-processing. A single-pass tree walk gives deterministic prioritization, predictable latency, and a clean basis for operational tooling.

## 🔍 Problem Statement
You are given the root of a binary tree, possibly empty, with up to `100000` nodes. Each node contains a unique integer value in `[-10^9, 10^9]`. A node is **incomplete** if it has exactly one child: left only or right only.

Return the value of the incomplete node that is:
1. at the greatest depth, and
2. among nodes at that depth, the leftmost.

Depth is defined with the root at depth `0`. If the tree contains no incomplete node, return `-1`.

Examples:

- `root = [10,5,20,3,null,15,30,null,4]` → `3`
- `root = [8,4,12,2,6,null,14,1]` → `2`

The key constraint is size: `O(n^2)` comparisons or storing unnecessary horizontal metadata is unjustified. You need a linear traversal that naturally respects both depth and left-to-right tie-breaking.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not “find any node with one child.” It is a ranked selection problem over all such nodes.
2. The ranking is lexicographic: **maximize depth**, then **minimize horizontal position**. In a binary tree, “leftmost” can be handled implicitly if traversal order is chosen correctly.
3. That suggests DFS with **left subtree visited before right subtree**. Why? Because the first incomplete node seen at a given depth will be the leftmost one at that depth.
4. While traversing, check each node locally: exactly one child means candidate.
5. Maintain global state:
   - `bestDepth`
   - `bestValue`
6. Update only when current depth is strictly greater than `bestDepth`. Do **not** replace on equal depth, because the earlier node encountered in left-first DFS is already the leftmost.
7. Handle edge cases explicitly:
   - empty tree → `-1`
   - full tree → `-1`
8. For very deep trees, prefer iterative DFS in production-grade code to avoid recursion-stack failure on skewed inputs near `100000` nodes.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Depth-First Search with ordered traversal.**  
   This is a tree-selection problem where candidate quality depends on depth and left-to-right order. Left-first DFS is the right abstraction because it explores all nodes exactly once and gives deterministic tie-breaking without extra bookkeeping.

2. **Initialize global best state.**  
   Set `bestDepth = -1` and `bestValue = -1`.  
   Invariant: after processing any visited prefix of the traversal, `(bestDepth, bestValue)` represents the deepest incomplete node seen so far, with leftmost tie-breaking already resolved.

3. **Traverse nodes in preorder-style DFS: node → left → right.**  
   The exact DFS flavor is flexible as long as the left subtree is fully processed before the right subtree at the same ancestor. This preserves left-to-right ordering among nodes at equal depth.

4. **At each node, test incompleteness locally.**  
   A node is incomplete iff exactly one child exists:  
   `(left == null) XOR (right == null)`.  
   This is correct because “incomplete” excludes both leaf nodes and nodes with two children.

5. **Update only on strictly deeper candidates.**  
   If current node is incomplete and `depth > bestDepth`, assign `bestDepth = depth` and `bestValue = node.val`.  
   Invariant preserved: equal-depth candidates discovered later are to the right, so they must not replace the current best.

6. **Continue traversal until exhaustion.**  
   Every node is visited once, every candidate is evaluated once, and no sorting or level buffering is required.

7. **Return the result.**  
   If no candidate was ever found, `bestValue` remains `-1`, which matches the required output.

## 📊 Worked Example
Use `root = [10,5,20,3,null,15,30,null,4]`.

| Step | Node | Depth | Incomplete? | Action | Best |
|---|---:|---:|---|---|---|
| 1 | 10 | 0 | No | none | `(-1, -1)` |
| 2 | 5 | 1 | Yes, left only | update | `(1, 5)` |
| 3 | 3 | 2 | Yes, right only | update | `(2, 3)` |
| 4 | 4 | 3 | No | none | `(2, 3)` |
| 5 | 20 | 1 | No | none | `(2, 3)` |
| 6 | 15 | 2 | No | none | `(2, 3)` |
| 7 | 30 | 2 | No | none | `(2, 3)` |

Trace logic:
1. Node `5` becomes the first valid candidate.
2. Node `3` is deeper, so it replaces `5`.
3. No later incomplete node appears at depth `> 2`.
4. Final answer is `3`.

The important detail is that tie-breaking never required explicit horizontal indexing; left-first traversal encoded it.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is the number of nodes. Each node is visited exactly once, and the per-node work is constant: child existence checks plus at most one comparison/update. At `10^6` nodes this is still practical as a single linear scan; at `10^9`, traversal cost dominates and distributed or external-memory concerns become the real bottleneck.

### Space Complexity
`O(h)` for DFS stack usage, where `h` is tree height. In the worst case of a skewed tree, `h = n`. The space is owned by the recursion stack or an explicit stack in iterative DFS. It can’t be reduced below traversal-state requirements without changing execution model.

## 💡 Key Takeaways
- If a tree problem says “deepest” plus “leftmost/rightmost,” think traversal order as part of the solution, not just post-processing.
- When a node property is local but the answer is a global best candidate, maintain a running optimum during traversal instead of collecting all matches.
- The update rule is strict: replace the answer only when `depth > bestDepth`; replacing on equality breaks leftmost tie resolution.
- “Incomplete” means exactly one child, not “missing at least one child”; leaves must be excluded.
- In production code, deterministic traversal order is a design tool: it eliminates secondary sorting and makes anomaly selection stable across runs.

## 🚀 Variations & Further Practice
- Return the **deepest rightmost** incomplete node instead; same core idea, but traversal order or tie-breaking flips.
- Generalize from binary trees to **k-ary trees**, where “incomplete” means child count is in `(1..k-1)`; tie-breaking now depends on sibling order across wider branching.
- Return **all** deepest incomplete nodes rather than one; this changes the state from a single best candidate to a depth bucket collected during traversal.