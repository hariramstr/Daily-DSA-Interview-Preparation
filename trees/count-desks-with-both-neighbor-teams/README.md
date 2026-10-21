# Count Desks with Both Neighbor Teams

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Depth-First Search, Breadth-First Search

---

## 🗂 Problem Overview
Given the root of a binary tree, count how many nodes have **both** left and right children and whose value differs from each child’s value. A node qualifies only when all three desks exist in that local structure and the parent value is unequal to both child values. Return `0` for an empty tree. The only real constraint is structural: you must inspect tree relationships, not just values, so a full traversal is required.

## 🌍 Engineering Impact
This pattern shows up anywhere systems evaluate local invariants over hierarchical state: AST validation in compilers, policy checks in IAM trees, topology validation in network maps, and rule evaluation over org or catalog hierarchies. At scale, the key decision is whether to compute such properties incrementally or via full traversal. Without a clean traversal model, teams end up with duplicated logic, partial scans, and subtle correctness gaps around missing children. The value here is not the predicate itself; it is recognizing that tree-local conditions are best expressed as a single-pass structural walk with explicit null handling and predictable complexity.

## 🔍 Problem Statement
You are given a binary tree where each node stores an integer team ID. A node should be counted if:

- it has a non-null left child,
- it has a non-null right child,
- its value is different from the left child’s value,
- its value is different from the right child’s value.

Formally, for a node with value `x`, left child `a`, and right child `b`, count it only if `x != a` and `x != b`.

Return the total number of such nodes.

Constraints:

- Number of nodes: `[0, 1000]`
- Node values: `[-10^4, 10^4]`
- Tree is binary, not necessarily balanced or complete
- Empty tree returns `0`

Examples:

- `root = [5,3,8,3,4,8,9]` → `1`
- `root = [7,2,9,1,2,null,10]` → `1`

The constraint that drives the algorithm is simple but decisive: every node may need inspection, so the natural solution is a full DFS or BFS traversal in linear time.

## 🪜 How to Solve This
1. Read the condition carefully → this is a **local property** of each node, not a global tree property.
2. A node qualifies only if it has **two children** → leaves and single-child nodes can be skipped immediately.
3. The predicate is cheap: compare the current value against `left.val` and `right.val`.
4. Since the property depends on parent-child relationships, we must visit every node at least once → think **tree traversal**.
5. At that point, the choice is straightforward:
   - **DFS** if you want minimal scaffolding and natural recursion/stack logic.
   - **BFS** if you prefer explicit queue-based iteration.
6. During traversal, maintain a running count.
7. For each visited node:
   - if both children exist and both values differ from the parent, increment the count;
   - continue traversing children regardless, because qualifying status of descendants is independent.
8. Stop when traversal completes. If the root is `null`, return `0`.

The key insight is that nothing about ordering matters. You are not searching, balancing, or aggregating by level. You are just evaluating one predicate per node, so a linear scan of the tree is the right mental model.

## 🧩 Algorithm Walkthrough
1. **Choose the traversal pattern: DFS or BFS.**  
   This is a standard **tree traversal** problem. The predicate depends only on a node and its immediate children, so any traversal that visits each node exactly once is sufficient. No ordering constraint exists.

2. **Handle the empty-tree case first.**  
   If `root == null`, return `0`. This is both correct and simplifies the main traversal logic by removing special-case branching later.

3. **Initialize a counter.**  
   Maintain `count = 0`. The invariant is: after processing any visited set of nodes, `count` equals the number of qualifying nodes among that set.

4. **Visit each node exactly once.**  
   In DFS, recurse or use an explicit stack. In BFS, use a queue. The correctness argument is straightforward: every node that could qualify must be examined, and the traversal guarantees coverage of the full reachable tree.

5. **Evaluate the node-local predicate.**  
   For the current node, first check `left != null && right != null`. Only then compare values:
   - `node.val != node.left.val`
   - `node.val != node.right.val`  
   If both hold, increment `count`.

6. **Continue traversal into children.**  
   Whether the current node qualifies has no effect on descendants. The invariant remains intact because each node contributes at most one increment based solely on its own local structure.

7. **Return the final count.**  
   After traversal completes, `count` is the exact number of nodes satisfying the definition.

This abstraction is correct because the problem decomposes cleanly into independent per-node checks over a finite tree.

## 📊 Worked Example
Consider `root = [7,2,9,1,2,null,10]`.

| Step | Current Node | Left | Right | Qualifies? | Count |
|---|---:|---:|---:|---|---:|
| 1 | 7 | 2 | 9 | Yes, `7 != 2` and `7 != 9` | 1 |
| 2 | 2 | 1 | 2 | No, right child matches parent | 1 |
| 3 | 9 | null | 10 | No, missing one child | 1 |
| 4 | 1 | null | null | No, leaf | 1 |
| 5 | 2 | null | null | No, leaf | 1 |
| 6 | 10 | null | null | No, leaf | 1 |

Trace summary:

1. Start at root `7`; it has two children and differs from both, so count it.
2. Move to node `2`; it has two children, but one child value equals `2`, so skip.
3. Node `9` has only one child, so it cannot qualify.
4. Remaining nodes are leaves and therefore cannot qualify.

Final answer: `1`.

## ⏱ Complexity Analysis
### Time Complexity
The traversal visits each node once and performs only constant-time work per node, so the total time complexity is **O(n)**. At `10^6` nodes this is still a straightforward linear scan; at `10^9`, the algorithm remains asymptotically optimal, but memory locality, recursion depth, and traversal infrastructure become operational concerns.

### Space Complexity
The auxiliary space is **O(h)** for recursive DFS, where `h` is tree height, or **O(w)** for BFS, where `w` is maximum width. In the worst case both can reach **O(n)**. Space can be controlled with iterative DFS, trading recursion simplicity for explicit stack management.

## 💡 Key Takeaways
- If a tree problem asks whether each node satisfies a condition involving only itself and immediate children, think single-pass DFS/BFS before anything more complex.
- When ordering does not matter and every node may contribute independently, that is a strong signal for linear traversal with a local predicate.
- Count a node only when **both** children exist; a single child is not “close enough” for this problem.
- Do not compare the children to each other; the requirement is only that the parent differs from each child.
- In production code, local structural invariants over hierarchical data are best implemented as explicit traversals with narrow predicates and well-defined null semantics.

## 🚀 Variations & Further Practice
- Count nodes where the parent differs from both children **and** the two children differ from each other; the twist is expanding the local predicate without changing traversal shape.
- Return the list of qualifying node values or references instead of just the count; the twist is preserving output ordering and representation.
- Generalize from binary trees to n-ary trees and count nodes whose value differs from all children; the twist is moving from fixed-arity checks to variable-size child iteration.