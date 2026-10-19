# Count Branches With Missing Siblings

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Binary Tree, DFS

---

## 🗂 Problem Overview
Given the root of a binary tree, count how many nodes have exactly one child. A node qualifies if its left child exists and right is missing, or vice versa. Leaves do not count, and nodes with two children do not count. Return a single integer. The problem is simple structurally, but it forces a full-tree traversal: the answer depends on local parent-child shape, so every node must be inspected once.

## 🌍 Engineering Impact
This pattern shows up anywhere tree-shaped metadata must be validated or summarized: compiler AST sanity checks, filesystem hierarchy audits, org-chart quality checks, search index taxonomy validation, and dependency graph projections constrained into binary form. At scale, the issue is not the predicate itself but traversal discipline: you need a single-pass structural scan with predictable memory behavior. Without that, teams end up with repeated partial traversals, inconsistent counting logic, or expensive materialization of intermediate structure. A clean DFS/BFS pass gives linear cost, composable validation, and a foundation for richer structural metrics.

## 🔍 Problem Statement
You are given the root of a binary tree with `0` to `1000` nodes. Each node contains an integer value in the range `[-10^4, 10^4]`, but the value is irrelevant to the result. Count the number of nodes that have exactly one child.

A node should be counted if:
- `left != null` and `right == null`, or
- `left == null` and `right != null`

Do **not** count:
- leaf nodes, because they have zero children
- full internal nodes, because they have two children

Return the total count.

Examples:

- `root = [5,3,8,1,null,null,9]` → `2`  
  Nodes `3` and `8` each have exactly one child.

- `root = [7,4,10,2,6,null,null]` → `0`  
  Node `4` has two children, `10` has none, and `2`, `6` are leaves.

The key constraint is structural: since any node may qualify, the algorithm must visit every node once.

## 🪜 How to Solve This
1. Read the requirement carefully → the node’s value does not matter at all. Only tree shape matters.

2. The condition is local → for each node, we only need to know whether `left` and `right` exist. That strongly suggests a traversal problem, not a dynamic programming or ordering problem.

3. Since any node in the tree might qualify, skipping subtrees is unsafe → we need a complete traversal.

4. DFS is the most natural fit here → visit a node, check whether it has exactly one child, then recurse into both children.

5. For each node:
   - if it is `null`, contribute `0`
   - if exactly one child exists, contribute `1`
   - add contributions from left and right subtrees

6. This works because the total answer is just the sum of independent local checks across all nodes.

7. BFS would also work, but DFS is simpler to express and keeps the logic close to the recursive tree definition.

Once you see that the predicate is purely local and the result is an aggregate over all nodes, a one-pass traversal becomes the obvious solution.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Depth-First Search (DFS).**  
   This is a standard tree traversal problem. DFS is the right abstraction because the tree is recursively defined, and the result is an aggregate over all nodes.

2. **Define the base case.**  
   If the current node is `null`, return `0`.  
   This is correct because a missing node contributes nothing to the count. The invariant is that each call returns the number of qualifying nodes in that subtree.

3. **Evaluate the current node locally.**  
   Check whether exactly one child exists. A concise test is:
   - `(left == null) XOR (right == null)`  
   while also ensuring the node is not a leaf implicitly, since both-null is false under XOR.  
   If true, contribute `1`; otherwise contribute `0`.

4. **Recurse into both subtrees.**  
   Compute the count from the left subtree and the right subtree.  
   This is necessary because qualifying nodes may appear anywhere below the current node.

5. **Combine results.**  
   Return:  
   `currentContribution + leftCount + rightCount`  
   This is correct because subtree counts are disjoint and additive.

6. **Maintain the invariant through recursion.**  
   After processing a node, the returned value always equals the number of nodes with exactly one child in the subtree rooted at that node.

7. **Alternative formulation: iterative BFS/DFS.**  
   A queue or explicit stack can replace recursion if stack depth is a concern. The counting logic is unchanged: inspect each node once, increment when exactly one child exists.

## 📊 Worked Example
Example: `root = [5,3,8,1,null,null,9]`

| Step | Node | Left | Right | Exactly One Child? | Running Count |
|------|------|------|-------|--------------------|---------------|
| 1 | 5 | 3 | 8 | No | 0 |
| 2 | 3 | 1 | null | Yes | 1 |
| 3 | 1 | null | null | No | 1 |
| 4 | 8 | null | 9 | Yes | 2 |
| 5 | 9 | null | null | No | 2 |

Trace using DFS:
1. Start at `5` → two children, do not count.
2. Go left to `3` → one child (`1`), count it.
3. Visit `1` → leaf, do not count.
4. Return and go right to `8` → one child (`9`), count it.
5. Visit `9` → leaf, do not count.

Final answer: `2`.

## ⏱ Complexity Analysis
### Time Complexity
The time complexity is **O(n)**, where `n` is the number of nodes, because each node is visited exactly once and the work per node is constant. At `10^6` nodes this remains practical as a linear scan; at `10^9`, linear time becomes operationally expensive regardless of implementation details.

### Space Complexity
The space complexity is **O(h)** for recursive DFS, where `h` is the tree height, due to the call stack. In the worst case of a skewed tree, this becomes **O(n)**. An iterative traversal avoids recursion risk but still needs **O(h)** stack or queue space.

## 💡 Key Takeaways
- If the condition depends only on a node and its immediate children, that is a strong signal for a single-pass tree traversal.
- When the answer is a sum of local structural predicates across all nodes, think DFS or BFS before considering more complex machinery.
- Do not count leaves: having zero children is not the same as having exactly one child.
- Be careful with the boolean test: “one child” means exclusive presence, not “at least one child.”
- In production code, structural metrics are often cheapest and most reliable when computed during one canonical traversal rather than through multiple specialized passes.

## 🚀 Variations & Further Practice
- Count nodes with exactly one child **at a specific depth** or within a depth range; the twist is carrying traversal state and filtering by level.
- Return the **list of qualifying node IDs** instead of just the count; same traversal, but now output size affects space complexity.
- Generalize from binary trees to **N-ary trees** and count nodes with exactly one child; the conceptual shift is from fixed-slot child checks to cardinality-based logic over child collections.