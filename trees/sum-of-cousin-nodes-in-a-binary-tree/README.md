# Sum of Cousin Nodes in a Binary Tree

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Binary Tree, Breadth-First Search

---

## 🗂 Problem Overview
Given the root of a binary tree and a target value `x`, return the sum of all cousin nodes of the node with value `x`. Cousins are nodes at the same depth with different parents, so siblings must be excluded. The target value appears exactly once, which removes ambiguity, but the tree may be sparse, unbalanced, and contain negative values. The non-trivial part is identifying the target’s level and parent relationship efficiently in one traversal.

## 🌍 Engineering Impact
This pattern shows up anywhere hierarchical data must be processed level-by-level while preserving parent context: org-chart analytics, dependency graph expansion, compiler AST passes, search index shard trees, and distributed workflow schedulers. At scale, treating a tree as just a set of nodes loses the structural constraint that siblings and peers mean different things operationally. Breadth-first traversal enables bounded, level-scoped decisions: aggregate metrics for peers, exclude same-parent entities, and stop early once the relevant frontier is found. Without that framing, implementations drift into repeated subtree scans, unnecessary state retention, or incorrect peer-group calculations.

## 🔍 Problem Statement
You are given a binary tree root and an integer `x`, where `x` matches exactly one node value in the tree. Return the sum of all cousin nodes of that target node. A cousin is any node at the same depth whose parent is different from the target node’s parent. If no such nodes exist, return `0`.

Constraints:
- `1 <= number of nodes <= 1000`
- `-10^4 <= Node.val <= 10^4`
- All node values are unique
- `x` exists exactly once in the tree

Examples:

- `root = [5,3,8,1,4,7,9], x = 4` → `16`
  - Target `4` is at depth `2`, parent `3`
  - Same-depth nodes: `1, 4, 7, 9`
  - Exclude target `4` and sibling `1`
  - Cousins: `7 + 9 = 16`

- `root = [10,6,15,3,null,12,18], x = 12` → `3`

The key constraint is structural: cousinhood depends on both depth and parent, which strongly suggests level-order traversal.

## 🪜 How to Solve This
1. Read the definition carefully → cousin means **same depth** and **different parent**. That immediately rules out a plain DFS that only finds the target node; depth alone is not enough.

2. Same depth usually means **process nodes level by level** → think **Breadth-First Search**.

3. While scanning one level, you need parent context for the next level → instead of queueing only nodes, reason in terms of each parent and its children.

4. For every level:
   - compute the sum of all child values that will form the next level
   - check whether the current parent owns the target as a child

5. Once the target is found among a parent’s children, the answer is:
   - total sum of the next level
   - minus the values of that parent’s own children
   This removes the target and any sibling in one step.

6. Stop immediately after finding that parent. No deeper level can contain cousins of the target because cousinhood is depth-specific.

This is the shortest path to a correct solution: BFS gives the right grouping, and parent-local subtraction handles sibling exclusion cleanly.

## 🧩 Algorithm Walkthrough
1. **Use Breadth-First Search (level-order traversal).**  
   The right abstraction is BFS because cousin relationships are defined by depth. BFS processes all nodes at one depth before moving deeper, which preserves the invariant that the queue contains exactly one level at a time.

2. **Initialize a queue with the root.**  
   At the start of each outer loop iteration, every node currently in the queue belongs to the same depth. This invariant is what lets us reason about “the next level” as a single cohort.

3. **Process all parents in the current level.**  
   For each node in the queue:
   - inspect `left` and `right`
   - add existing child values to `nextLevelSum`
   - enqueue those children for the next round  
   This computes the full value sum for the next depth in one pass.

4. **Detect whether the target belongs to a specific parent.**  
   While inspecting a parent’s children, check whether either child has value `x`. If yes, record `siblingGroupSum` as the sum of that parent’s existing children. This is correct because the target and any sibling are exactly the nodes that must be excluded from cousin aggregation.

5. **After finishing the current level, decide whether to return.**  
   If the target was found among the children of any parent at this level, return `nextLevelSum - siblingGroupSum`.  
   Why this works:
   - `nextLevelSum` includes every node at the target’s depth
   - `siblingGroupSum` removes the target and all same-parent nodes
   - what remains are exactly the cousins

6. **If the target is never found until traversal ends, return `0`.**  
   Under the problem constraints this case should not occur, but it keeps the implementation total and safe.

## 📊 Worked Example
Example: `root = [5,3,8,1,4,7,9], x = 4`

| Current Level Queue | Children Seen | `nextLevelSum` | Target Found? | `siblingGroupSum` |
|---|---|---:|---|---:|
| `[5]` | `3, 8` | 11 | No | 0 |
| `[3, 8]` | from `3` → `1, 4`; from `8` → `7, 9` | 21 | Yes, under parent `3` | 5 |

Trace:
1. Start with queue `[5]`. Its children are `3` and `8`; target `4` is not here.
2. Move to queue `[3, 8]`, which is the parent level directly above the target.
3. From parent `3`, children are `1` and `4`. Target found, so `siblingGroupSum = 1 + 4 = 5`.
4. Continue level processing to finish `nextLevelSum`: add children of `8`, which are `7` and `9`. Total becomes `1 + 4 + 7 + 9 = 21`.
5. Return `21 - 5 = 16`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in **O(n)** time because each node is enqueued, dequeued, and inspected at most once. The dominant operation is the BFS scan over all nodes. At `10^6` nodes this remains linear and predictable; at `10^9`, runtime becomes throughput-bound and tree materialization itself is the bigger issue.

### Space Complexity
The algorithm uses **O(w)** auxiliary space, where `w` is the maximum width of the tree, due to the BFS queue. In the worst case this is `O(n)`. You can switch to DFS with depth bookkeeping, but that trades queue width for more global state and usually less clarity.

## 💡 Key Takeaways
- If a tree problem says “same level,” “depth peers,” or “all nodes at distance `d`,” BFS should be your first candidate.
- If the rule also depends on “different parent,” carry parent-local context while traversing levels instead of reconstructing it later.
- The return value is based on the **target’s level**, but you detect it from the **parent level above**; that’s the easiest place to exclude siblings correctly.
- Do not return as soon as you first see the target child; you still need the full sum of the entire next level before subtracting the sibling group.
- In production code, level-scoped aggregation plus local exclusion is a reusable pattern for hierarchical analytics where peer groups matter but same-owner entities must be filtered out.

## 🚀 Variations & Further Practice
- Return the **list of cousin values** instead of their sum; same BFS pattern, but now output construction and ordering semantics matter.
- Generalize from binary trees to **N-ary trees**; the conceptual twist is sibling-group handling across variable-size child lists.
- Compute cousin sums for **every node in one traversal**; harder because you need per-level aggregates and per-parent child sums for all nodes, not just one target.