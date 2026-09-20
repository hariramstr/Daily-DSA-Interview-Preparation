# Minimum Cameras to Monitor a Facility Tree

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, Greedy

---

## 🗂 Problem Overview
Given the root of a binary tree, place the fewest cameras so every node is monitored. A camera covers exactly its parent, itself, and its immediate children. The output is the minimum number of cameras required to cover the entire tree.

The challenge is not coverage itself but optimal placement under tree structure constraints. With up to `10^5` nodes, any brute-force search over camera subsets is infeasible, so the solution must make local decisions that compose into a global optimum.

## 🌍 Engineering Impact
This pattern shows up anywhere coverage must be enforced over hierarchical topology with minimal control points: facility security layouts, network probe placement in tree-shaped topologies, filesystem backup agents, AST or DOM instrumentation, and org-policy enforcement in management trees. At scale, brute-force placement explodes combinatorially and centralized optimization becomes impractical. A bottom-up greedy state machine works because it converts a global minimization problem into local coverage decisions with bounded state. That enables linear-time planning, predictable memory use, and implementations that remain stable under very deep or very large hierarchies.

## 🔍 Problem Statement
You are given a valid binary tree with `1 <= n <= 10^5` nodes. Each node represents a room. A camera installed at a node monitors three categories of rooms: the node itself, its parent, and its immediate children. Every node in the tree must be monitored by at least one camera.

Return the minimum number of cameras needed to monitor the full tree. Node values are only identifiers and do not affect placement.

Examples:

- `root = [0,0,null,0,0]` → `1`
- `root = [0,0,null,0,null,0,null,null,0]` → `2`

Important edge cases include a single-node tree, a skewed chain, and trees where the root is uncovered after processing all descendants. The key constraint is `10^5` nodes: exhaustive search over placements is impossible, so the algorithm must run in linear time or close to it.

## 🪜 How to Solve This
1. Start from the coverage rule → a camera affects only distance-1 neighbors, so whether a node needs a camera depends heavily on its children.
2. That suggests **postorder traversal** → decide a node only after both children are fully evaluated.
3. Now define minimal states, not full configurations:
   - this node has a camera,
   - this node is covered,
   - this node is not covered yet.
4. Why these states work → the parent only needs to know whether a child forces action, already solved itself, or can be ignored.
5. Bottom-up reasoning:
   - if any child is uncovered, current node must place a camera;
   - if any child has a camera, current node is covered;
   - otherwise current node is uncovered and asks its parent for help.
6. This is greedy, but not arbitrary → cameras are placed only when a child proves they are necessary.
7. After traversal, check the root separately. If it is still uncovered, add one final camera.

That gives a linear-time DFS with constant state per node.

## 🧩 Algorithm Walkthrough
1. **Use Postorder DFS + Greedy State Compression.**  
   Traverse left subtree, then right subtree, then the current node. This is the right abstraction because a node’s optimal action depends only on already-resolved child states.

2. **Assign three states to each node.**  
   Let:
   - `0 = uncovered`
   - `1 = has camera`
   - `2 = covered, no camera`  
   This is the minimal information the parent needs. The invariant is: after processing a node, its subtree is optimally handled except possibly the node itself being left uncovered for its parent to cover.

3. **Treat null children as covered.**  
   A missing child does not require monitoring and must not force camera placement. Returning `covered` for null nodes prevents leaves from incorrectly placing cameras on themselves.

4. **Apply the local decision rule.**  
   After getting left and right states:
   - if either child is `uncovered`, place a camera here, increment answer, return `has camera`;
   - else if either child `has camera`, return `covered`;
   - else return `uncovered`.  
   This is correct because uncovered children can only be fixed by their parent, while a child camera already covers the current node.

5. **Finalize at the root.**  
   If the root returns `uncovered`, add one more camera. The root has no parent, so deferred coverage cannot be pushed further upward.

6. **Why the greedy choice is optimal.**  
   Cameras are installed only at nodes that are the highest necessary fix for uncovered children. Any lower placement would fail to cover the parent; any higher placement delays a required action and cannot reduce count. The traversal maintains subtree optimality at every step.

## 📊 Worked Example
Example: `root = [0,0,null,0,0]`

Tree shape:
- root
  - left
    - left
    - right

Trace:

| Node | Left State | Right State | Action | Return State | Cameras |
|---|---:|---:|---|---:|---:|
| left.left | covered | covered | no camera needed | uncovered | 0 |
| left.right | covered | covered | no camera needed | uncovered | 0 |
| left | uncovered | uncovered | place camera | has camera | 1 |
| root.right (null) | — | — | base case | covered | 1 |
| root | has camera | covered | already covered by child | covered | 1 |

Result: `1`

The key moment is at node `left`: both children are uncovered leaves, so placing one camera there covers itself, both leaves, and the root. That dominates any strategy that places cameras on the leaves individually.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is the number of nodes. Each node is visited exactly once, and each visit performs constant-time state checks and updates. At `10^6` nodes this is still practical in linear scan terms; at `10^9`, traversal cost alone becomes operationally prohibitive regardless of algorithmic optimality.

### Space Complexity
`O(h)` for recursion stack depth, where `h` is tree height. In a balanced tree this is `O(log n)`; in a skewed tree it becomes `O(n)`. It can be reduced to explicit iterative traversal, but implementation complexity increases substantially.

## 💡 Key Takeaways
- If a tree problem asks for a minimum number of local controllers covering parent/child neighborhoods, think **postorder DFS with compressed node states**.
- If a parent’s decision depends only on summarized child outcomes, that is a strong signal for a **bottom-up greedy tree DP** rather than global search.
- Treat `null` nodes as already covered; otherwise leaves will trigger unnecessary camera placement.
- Do not forget the root post-processing step: an uncovered root cannot be fixed by any parent and needs its own camera.
- The transferable design insight is to replace combinatorial placement search with a minimal state protocol between adjacent nodes, turning global optimization into linear-time local coordination.

## 🚀 Variations & Further Practice
- **Generalize from binary trees to N-ary trees**: same coverage model, but the decision rule must aggregate over an arbitrary number of children without losing linear performance.
- **Weighted camera placement**: each node has a placement cost, and the goal is minimum total cost rather than minimum count; this shifts the problem from greedy state transitions to tree DP with costed states.
- **Radius-k monitoring**: a camera covers nodes within distance `k`; the harder twist is that local three-state compression no longer suffices, so the DP state must encode remaining coverage distance.