# Find the Next Right Record in an Incomplete Binary Archive

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Binary Tree, Breadth-First Search

---

## 🗂 Problem Overview
Given the root of a binary tree and a target `recordId` known to exist, return the value of the closest node to its right on the same depth level. If the target is already the rightmost node on that level, return `-1`. The challenge is that the tree is incomplete, so positional assumptions from perfect trees do not apply. You need a level-aware traversal that remains efficient up to `100000` nodes.

## 🌍 Engineering Impact
This pattern shows up anywhere hierarchical data is traversed in breadth order and sibling adjacency matters: search index shard maps, workflow DAG visualizers constrained into tree projections, compiler AST tooling, filesystem metadata scans, and UI layout engines walking render trees. At scale, the issue is not just correctness but traversal discipline: if you lose level boundaries, “next” becomes ambiguous and downstream decisions drift. A level-order strategy preserves locality, enables streaming evaluation, and avoids materializing unnecessary structure. That matters in systems where trees are sparse, deep, and too large for naive per-level reconstruction.

## 🔍 Problem Statement
You are given a binary tree whose nodes contain unique integer `recordId` values and an integer `target` that is guaranteed to match one node in the tree. Return the `recordId` of the nearest node to the right of that target on the same depth level, or `-1` if no such node exists.

The tree is not perfect or complete: any node may have zero, one, or two children. Constraints are large enough to rule out wasteful repeated scans: the tree contains between `1` and `100000` nodes, and all values are unique.

Examples:

- `root = [10,5,15,2,7,null,20], target = 5` → `15`
- `root = [8,3,11,1,6,9,null,null,null,4,7], target = 6` → `9`

The algorithmic pressure comes from needing level awareness in linear time on sparse trees.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not “next in traversal order”; it is “next on the same depth level.”
2. Same depth level immediately suggests **level-order traversal**, i.e. Breadth-First Search.
3. In BFS, nodes are naturally visited left to right within each level if you enqueue children in left-then-right order.
4. The only missing piece is preserving level boundaries. Without that, the next dequeued node might belong to the next level, which would be wrong.
5. So process the queue one level at a time:
   - capture the current queue size,
   - iterate exactly that many nodes,
   - this slice is one depth level.
6. While scanning a level, when you find `target`:
   - if it is not the last node in that level, the next node processed in this same loop is the answer;
   - if it is the last node in that level, return `-1`.
7. This gives a single-pass, linear-time solution with no need to store all levels explicitly.

Once you see “same level” plus “nearest right,” BFS is the obvious fit.

## 🧩 Algorithm Walkthrough
1. **Use Breadth-First Search with a queue.**  
   This is the canonical pattern for level-sensitive tree problems. DFS can track depth, but it does not preserve left-to-right adjacency without extra bookkeeping. BFS gives both naturally.

2. **Initialize the queue with the root.**  
   The queue invariant is: before each outer iteration, it contains exactly the nodes of the next level to process, in left-to-right order.

3. **For each level, record `levelSize = queue.length`.**  
   This snapshots the current depth boundary. Any children enqueued during this pass belong to the next level and must not affect the current answer.

4. **Iterate `i` from `0` to `levelSize - 1`.**  
   Dequeue one node at a time. Because parents were enqueued left-to-right and children are appended left-first, the dequeue order within this loop is exactly the left-to-right order of the current level.

5. **Check whether the current node matches `target`.**  
   - If yes and `i < levelSize - 1`, the next node currently at the front of the queue is the nearest right node on the same level, so return its value.
   - If yes and `i == levelSize - 1`, target is the rightmost node on that level, so return `-1`.

6. **Enqueue children after processing the current node.**  
   Add `left`, then `right` if they exist. This maintains the next-level ordering invariant.

7. **Termination is guaranteed.**  
   Since `target` exists, the algorithm returns during traversal. Worst case, it scans the entire tree once.

This is a textbook **Breadth-First Search (level-order traversal)** problem because the query is defined by horizontal adjacency, not ancestry or subtree structure.

## 📊 Worked Example
Example: `root = [8,3,11,1,6,9,null,null,null,4,7], target = 6`

| Level | Queue at start | Process order | Target found? | Answer |
|---|---|---|---|---|
| 0 | `[8]` | `8` | No | — |
| 1 | `[3, 11]` | `3, 11` | No | — |
| 2 | `[1, 6, 9]` | `1` | No | — |
| 2 | `[6, 9]` | `6` | Yes, not last in level | `9` |

Trace:
1. Start with root `8`.
2. After processing level 0, enqueue `3, 11`.
3. After processing `3`, enqueue `1, 6`; after `11`, enqueue `9`.
4. Level 2 begins with nodes `[1, 6, 9]`.
5. When `6` is dequeued, it is at index 1 of a 3-node level, so the next node on the same level is `9`.
6. Return `9` immediately.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is the number of nodes. Each node is enqueued and dequeued once, and each edge is examined a constant number of times. At `10^6` nodes this is still practical in a single pass; at `10^9`, traversal cost becomes dominated by memory and system-level throughput rather than algorithmic overhead.

### Space Complexity
`O(w)`, where `w` is the maximum width of the tree, because the queue holds one level at a time. In the worst case this is `O(n)`. You can avoid storing all levels explicitly, but not the frontier required for BFS without changing the traversal model.

## 💡 Key Takeaways
• If a tree problem asks for “same level,” “nearest sibling,” or “next to the right,” think level-order traversal before considering DFS.  
• When output depends on left-to-right adjacency within a depth, queue order plus fixed level boundaries is the key signal.  
• The common bug is returning the next dequeued node without checking whether it belongs to the same level.  
• Another trap is enqueueing children correctly but forgetting that the target might be the last node in its level, which must return `-1`.  
• In production traversal code, preserving structural boundaries during streaming often matters more than the raw walk itself.

## 🚀 Variations & Further Practice
- Return the next right node for **every** node in the tree; the twist is building per-level neighbor links efficiently instead of answering one query.
- Solve the same problem with **O(1) extra space** on certain tree classes; the twist is exploiting existing `next` pointers or structural guarantees.
- Generalize from binary trees to **N-ary trees**; the core idea stays BFS, but frontier width and child-order semantics become more operationally significant.