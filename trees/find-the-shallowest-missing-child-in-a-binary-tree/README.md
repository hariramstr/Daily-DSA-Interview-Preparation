# Find the Shallowest Missing Child in a Binary Tree

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Breadth-First Search, Binary Tree

---

## 🗂 Problem Overview
Given the root of a binary tree, return the value of the first node in level-order traversal that is missing at least one direct child. “First” is defined by smallest depth from the root, then left-to-right within that depth. If the tree is empty, return `-1`. The non-trivial part is preserving both ordering rules simultaneously; a depth-first traversal can find an incomplete node, but not necessarily the shallowest or leftmost one.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must detect the earliest structural gap in a hierarchy: incomplete shard-assignment trees, partially provisioned service topologies, scheduler dependency trees, trie-backed routing structures, and heap-like storage layouts. At scale, the difference between “find any defect” and “find the shallowest defect” matters because remediation priority is depth-sensitive: higher-level gaps affect more downstream capacity. Breadth-first traversal gives deterministic, policy-aligned discovery order without post-processing. Without it, systems either over-scan, mis-prioritize repairs, or require extra bookkeeping to reconstruct level semantics after traversal.

## 🔍 Problem Statement
You are given the root of a binary tree with `0` to `1000` nodes. Each node contains a unique integer value in the range `[-10000, 10000]`. A node is considered incomplete if it does not have both a left and a right child.

Return the value of the incomplete node that appears first in level-order traversal: smallest depth first, and within the same depth, left to right. If the tree is empty, return `-1`.

Examples:

- `root = [7,4,9,2,6,null,11]` → `9`  
  Node `9` is the first node encountered level by level that is missing a child.

- `root = [5,3,8,1,null,7,10]` → `3`  
  Node `3` is missing its right child and appears before `8` at the same depth.

The key constraint is the ordering requirement: the answer is not any incomplete node, but the earliest one under level-order semantics.

## 🪜 How to Solve This
1. Read the requirement carefully → the answer depends on **depth order**, not just existence.
2. “Smallest depth” immediately suggests **level-order traversal**, which is Breadth-First Search.
3. “If multiple nodes are at the same depth, choose left to right” reinforces BFS, because a queue naturally preserves insertion order within a level.
4. Start from the root:
   - If the tree is empty, return `-1`.
   - Otherwise, process nodes in queue order.
5. For each node, ask the only question that matters: does it have both children?
   - If not, return its value immediately.
   - If yes, enqueue `left`, then `right`.
6. Why early return works: BFS guarantees that once a node is popped, no unprocessed node can be shallower, and no peer earlier in left-to-right order remains ahead of it.
7. That means the first incomplete node seen is not just a candidate — it is provably the correct answer.

This is a classic “ordering requirement determines traversal strategy” problem.

## 🧩 Algorithm Walkthrough
1. **Handle the empty-tree edge case.**  
   If `root == null`, return `-1`. This satisfies the contract directly and avoids queue setup for a degenerate input.

2. **Initialize a queue with the root.**  
   This is the core **Breadth-First Search (BFS)** pattern: process nodes in FIFO order so traversal follows increasing depth. The invariant is that nodes already dequeued are earlier in level-order than nodes still in the queue.

3. **Pop the next node from the queue.**  
   At this moment, the node is the earliest unprocessed node in level-order. That ordering property is why BFS is the right abstraction here; DFS cannot guarantee shallowness without exploring and comparing many branches.

4. **Check whether the node is incomplete.**  
   If `node.left == null || node.right == null`, return `node.val`. This is correct because BFS ensures:
   - no shallower node remains unseen, and
   - no node to its left on the same level remains unseen.

5. **Otherwise enqueue both children in left-to-right order.**  
   Push `node.left`, then `node.right`. This preserves the left-to-right invariant for the next level. Reversing the order would break tie handling.

6. **Repeat until a result is found.**  
   In practice, every finite non-empty tree has at least one incomplete node unless every internal node has two children and leaves are excluded from consideration. Here leaves count as incomplete, so termination is guaranteed.

7. **Return the first match.**  
   The algorithm is optimal for the required ordering because it stops at the earliest valid answer instead of scanning the whole tree unnecessarily.

## 📊 Worked Example
Example: `root = [7,4,9,2,6,null,11]`

| Step | Queue Before | Node Popped | Has Both Children? | Action |
|---|---|---:|---|---|
| 1 | `[7]` | 7 | Yes (`4`, `9`) | Enqueue `4, 9` |
| 2 | `[4, 9]` | 4 | Yes (`2`, `6`) | Enqueue `2, 6` |
| 3 | `[9, 2, 6]` | 9 | No (`left = null`, `right = 11`) | Return `9` |

Trace summary:
1. Start with root `7`.
2. `7` is complete, so continue.
3. `4` is also complete, so continue.
4. `9` is the next node in level-order and is missing a left child.
5. Because BFS visits nodes by depth and left-to-right within each level, `9` is the shallowest incomplete node and the correct answer.

## ⏱ Complexity Analysis
### Time Complexity
The time complexity is `O(n)` in the worst case, where `n` is the number of nodes. Each node is enqueued and dequeued at most once, and the per-node work is constant. At `10^6` nodes this is still linear and practical; at `10^9`, traversal cost becomes dominated by memory bandwidth and storage layout rather than algorithmic overhead.

### Space Complexity
The space complexity is `O(w)`, where `w` is the maximum width of the tree, bounded by `O(n)`. The queue owns this space. It cannot be reduced asymptotically if level-order semantics must be preserved; trading it away generally means switching to DFS and losing the required ordering guarantee.

## 💡 Key Takeaways
- If a tree problem asks for the **shallowest**, **nearest**, or **first by level**, that is a strong signal for BFS.
- If tie-breaking is **left-to-right within the same depth**, queue insertion order is part of correctness, not an implementation detail.
- Do not return the first incomplete node found by DFS; it may be deeper than a valid answer in another branch.
- Enqueue children in `left` then `right` order, or you will silently violate the tie-breaking rule.
- In production systems, traversal strategy should encode business priority directly; BFS is often the simplest way to align discovery order with remediation order.

## 🚀 Variations & Further Practice
- Return the **depth** of the shallowest incomplete node instead of its value; same BFS pattern, but now you must track level boundaries or store `(node, depth)` pairs.
- Find **all** incomplete nodes at the minimum depth; the twist is that you cannot stop at the first match and must finish the current level once the first incomplete node is found.
- Extend to an **N-ary tree** and return the first node with fewer than `k` children; same level-order idea, but completeness is now parameterized rather than fixed at two children.