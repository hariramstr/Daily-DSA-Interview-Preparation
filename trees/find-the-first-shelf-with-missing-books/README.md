# Find the First Shelf With Missing Books

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Binary Tree, Breadth-First Search

---

## 🗂 Problem Overview
Given the root of a binary tree, return the value of the first node that has exactly one child when the tree is traversed in level order. If no such node exists, return `-1`. The non-trivial part is not detecting whether any node is incomplete, but finding the earliest one under strict breadth-first ordering: top to bottom, then left to right within each level.

## 🌍 Engineering Impact
This pattern shows up anywhere hierarchical state must be validated in priority order: filesystem integrity scans, org-chart or entitlement trees, AST validation in compilers, routing tries, and UI layout trees. The requirement is usually not “find all malformed nodes,” but “find the first structurally invalid node closest to the root,” because that node often explains downstream corruption. Breadth-first traversal matters at scale because it surfaces the highest-impact defect first, supports early termination, and avoids wasting work deep in subtrees that are irrelevant once a top-level structural violation is found.

## 🔍 Problem Statement
You are given the root of a binary tree where each node contains a unique integer shelf ID. A shelf is valid if it has either no children or exactly two children. A shelf is considered to have missing books if it has exactly one child.

Return the shelf ID of the first such node encountered in level-order traversal. If the tree is empty or every non-leaf node has either zero or two children, return `-1`.

Constraints:
- Number of nodes: `[0, 1000]`
- Node values: `[1, 100000]`
- All values are unique
- The tree may be empty

Examples:
- `root = [10,5,20,3,null,15,30]` → `5`
- `root = [8,4,12,2,6,10,14]` → `-1`

The key constraint is traversal order: the answer is the earliest incomplete node in BFS order, not just any node with one child.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not a generic tree scan. The phrase “first shelf” plus “level by level, left to right” directly implies breadth-first search.
2. Ask what qualifies as a match → a node with exactly one child. That means:
   - left exists, right missing, or
   - right exists, left missing.
3. Since BFS already visits nodes in the required priority order, the first matching node you encounter is automatically the correct answer. No post-processing, sorting, or tracking of depth is needed.
4. Use a queue → start with the root, pop one node at a time, check whether it has exactly one child, and return immediately if it does.
5. If it does not match, enqueue its existing children in left-to-right order so traversal order stays correct.
6. Finish the queue without finding a match → return `-1`.

The key insight is that traversal order is the problem. Once that is clear, BFS is not just a valid solution; it is the most direct expression of the requirement.

## 🧩 Algorithm Walkthrough
1. **Handle the empty-tree case.**  
   If `root` is `null`, return `-1`. This is correct because there are no shelves to inspect. The invariant is simple: if the queue is never initialized, no candidate exists.

2. **Initialize a queue with the root node.**  
   This is the standard **Breadth-First Search (BFS)** pattern for binary trees. BFS is the right abstraction because it preserves level-order traversal exactly as required by the problem.

3. **Process nodes one by one from the queue front.**  
   At each step, dequeue the current node. BFS guarantees that nodes are visited in non-decreasing depth order, and left children are seen before right siblings when enqueued in that order.

4. **Check whether the current node has exactly one child.**  
   Compute the structural condition:
   - `(left != null && right == null)` or
   - `(left == null && right != null)`  
   If true, return the current node’s value immediately. This is correct because BFS ensures no earlier node in the required order remains unprocessed.

5. **Enqueue existing children from left to right.**  
   If `left` exists, enqueue it. If `right` exists, enqueue it. The invariant maintained is that the queue always contains the next nodes to visit in exact level-order sequence.

6. **Return `-1` if traversal completes.**  
   If the queue empties without finding a node with exactly one child, then no such shelf exists anywhere in the tree.

## 📊 Worked Example
Example: `root = [10,5,20,3,null,15,30]`

| Step | Queue Before | Current | Left | Right | One Child? | Action |
|---|---|---:|---:|---:|---|---|
| 1 | `[10]` | 10 | 5 | 20 | No | Enqueue 5, 20 |
| 2 | `[5,20]` | 5 | 3 | null | Yes | Return 5 |

Trace:
1. Start with root `10`.
2. Node `10` has two children, so it is structurally complete. Continue BFS.
3. Enqueue `5` then `20` to preserve left-to-right order.
4. Dequeue `5`. It has a left child `3` and no right child.
5. This satisfies the “exactly one child” condition.
6. Because BFS visits nodes in the required order, `5` is the first valid answer, so return `5` immediately.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` where `n` is the number of nodes, because each node is dequeued and checked at most once. The dominant operation is the BFS scan. At `10^6` nodes this is still linear and practical; at `10^9`, linear cost becomes infrastructure-bound, so early exit from top-level matches becomes operationally important.

### Space Complexity
`O(w)` where `w` is the maximum width of the tree, due to the BFS queue. In the worst case this is `O(n)`. You could reduce auxiliary space with DFS, but you would lose the natural level-order guarantee and need extra bookkeeping to recover traversal priority.

## 💡 Key Takeaways
- If a tree problem says “first,” “nearest to root,” or “level by level,” that is a strong signal for BFS rather than DFS.
- If correctness depends on left-to-right order within the same depth, queue discipline is part of the algorithm, not an implementation detail.
- Do not enqueue children before checking the current node if your code structure makes early-return ordering ambiguous.
- Be precise about “exactly one child”: XOR-style logic is correct; “has at least one child” is not.
- In production validation pipelines, traversal order often encodes business priority: finding the highest-impact structural defect first is more valuable than exhaustively scanning deep descendants.

## 🚀 Variations & Further Practice
- Return the depth or full node reference of the first incomplete node instead of just its value; same BFS pattern, but the output contract carries more context.
- Find all nodes with exactly one child, grouped by level; this removes early termination and turns the problem into full-level aggregation.
- Validate whether the tree is a complete binary tree; same BFS foundation, but the harder twist is enforcing null-gap rules after the first missing child position.

