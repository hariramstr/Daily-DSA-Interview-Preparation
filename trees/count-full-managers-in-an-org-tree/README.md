# Count Full Managers in an Org Tree

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, BFS

---

## 🗂 Problem Overview
Given the root of a binary tree representing an org chart, count how many employees have exactly two direct reports. Each node is one employee; `left` and `right` are the only possible report slots. The output is a single integer: the number of full managers. The only meaningful data is tree structure, not node values. The non-trivial part is that every node must be inspected while correctly handling empty trees and partially populated branches.

## 🌍 Engineering Impact
This pattern shows up anywhere hierarchical state must be aggregated from structure rather than payload: org modeling in HR systems, entitlement trees in IAM platforms, AST analysis in compilers, dependency graphs in build systems, and topology inspection in workflow orchestrators. At scale, the core issue is not counting itself but choosing a traversal strategy that is predictable, linear, and easy to reason about under skewed shapes. Without a disciplined traversal, teams introduce duplicated logic, inconsistent edge-case handling, or accidental quadratic scans. A simple tree-walk abstraction enables reusable structural analytics, validation, and reporting over large hierarchies.

## 🔍 Problem Statement
You are given the `root` of a binary tree where each node represents an employee in a company. A node may have up to two children, representing direct reports. A manager is considered **full** only if both child pointers are present. Nodes with zero or one child do not count.

Return the total number of full managers in the tree.

Constraints:
- Number of nodes: `[0, 1000]`
- Node values: `[-10^4, 10^4]`
- Node values are irrelevant; only child presence matters
- The tree may be empty, in which case the answer is `0`

Examples:
- `root = [10,5,20,3,7,null,30]` → `2`
- `root = [1,2,3,4,null,null,null]` → `1`

The constraint that drives the algorithmic choice is structural coverage: to know whether a node is full, you must visit that node, so any correct solution is fundamentally a full-tree traversal.

## 🪜 How to Solve This
1. Read the problem → the value stored in each node does not matter at all. That immediately tells you this is a **structure-only traversal** problem.

2. Ask what makes a node count → not leaf status, not subtree size, just one predicate: `left != null && right != null`.

3. If the answer depends on checking that predicate for every employee, then every node must be visited once. That points directly to **DFS or BFS**.

4. Pick a traversal style:
   - Recursive DFS if you want the shortest implementation.
   - Iterative DFS if you want explicit stack control.
   - BFS if level-order processing is more natural for your codebase.

5. Maintain one running count. For each visited node:
   - If both children exist, increment the count.
   - Continue traversing any existing children.

6. Handle the empty tree first. If `root == null`, return `0`.

The key insight is that there is no optimization trick here. Since fullness is a local property but can occur anywhere, the right solution is the simplest linear traversal that inspects each node exactly once.

## 🧩 Algorithm Walkthrough
1. **Choose the traversal pattern: Tree Traversal (DFS or BFS).**  
   This is the right abstraction because the problem asks for a property evaluated independently at each node. There is no ordering requirement, no path constraint, and no need for subtree aggregation beyond a global count.

2. **Initialize the result.**  
   Start a counter at `0`. This counter represents the number of nodes seen so far that satisfy the invariant: “has exactly two children.”

3. **Handle the base case.**  
   If `root` is `null`, return `0` immediately. This preserves correctness for the empty-tree edge case and avoids unnecessary traversal setup.

4. **Visit each node exactly once.**  
   In DFS, recurse or push children onto a stack. In BFS, enqueue children into a queue. The invariant is that every reachable node from `root` will eventually be processed once.

5. **Evaluate the local predicate.**  
   For the current node, check whether `node.left != null` and `node.right != null`. If true, increment the counter. This is correct because the definition of a full manager depends only on direct children, not descendants.

6. **Continue traversal through existing children.**  
   Push or recurse into `left` if present, and into `right` if present. Nodes with one child still need traversal because their descendants may contain full managers.

7. **Return the counter after traversal completes.**  
   At termination, the count is correct because every node has been examined once and only nodes satisfying the exact predicate were counted.

## 📊 Worked Example
Example: `root = [10,5,20,3,7,null,30]`

Using BFS:

| Step | Node Visited | Left | Right | Full Manager? | Count |
|------|--------------|------|-------|---------------|-------|
| 1 | 10 | 5 | 20 | Yes | 1 |
| 2 | 5 | 3 | 7 | Yes | 2 |
| 3 | 20 | null | 30 | No | 2 |
| 4 | 3 | null | null | No | 2 |
| 5 | 7 | null | null | No | 2 |
| 6 | 30 | null | null | No | 2 |

Trace:
1. Start with queue = `[10]`, count = `0`.
2. Visit `10` → both children exist → count becomes `1`.
3. Visit `5` → both children exist → count becomes `2`.
4. Visit `20` → only one child exists → count unchanged.
5. Remaining nodes are leaves, so none qualify.
6. Traversal ends with final answer `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is the number of nodes in the tree. The dominant operation is visiting each node once and performing a constant-time child check. At `10^6` nodes this remains practical as a linear scan; at `10^9`, the asymptotics are still optimal, but memory layout, recursion safety, and traversal infrastructure become the real bottlenecks.

### Space Complexity
`O(h)` for recursive DFS, where `h` is tree height; `O(n)` in the worst case for BFS queue or iterative DFS stack on broad or skewed trees. The owning structure is the call stack or explicit worklist. You can trade recursion brevity for iterative control to avoid stack overflow on degenerate trees.

## 💡 Key Takeaways
- If the condition is defined per node and may occur anywhere in a tree, default to a full traversal rather than searching for shortcuts.
- When node values are irrelevant and only parent/child presence matters, this is a structural DFS/BFS recognition signal.
- Do not count leaves or one-child nodes; the predicate is exactly two non-null children, not “at least one.”
- Even if a node is not a full manager, you must still traverse its existing child because qualifying nodes may exist deeper in the tree.
- In production code, simple linear traversals are often the right foundation for reusable hierarchy analytics because they separate structural inspection from business-specific predicates.

## 🚀 Variations & Further Practice
- Count nodes with exactly one child instead of two; same traversal, but the predicate becomes an exclusive-or check on child presence.
- Count full managers only at even depths or within a specific subtree; same tree-walk, but now traversal state must carry depth or scope context.
- Generalize from binary trees to arbitrary `N`-ary org structures; the conceptual twist is replacing fixed child checks with iteration over a variable-size reports list.