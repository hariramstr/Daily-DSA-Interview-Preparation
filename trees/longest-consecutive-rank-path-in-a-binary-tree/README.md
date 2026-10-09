# Longest Consecutive Rank Path in a Binary Tree

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Depth-First Search, Dynamic Programming

---

## 🗂 Problem Overview
Given a binary tree, return the length of the longest path where every adjacent parent-child pair differs by exactly 1. The path may increase, decrease, or pivot once at some node by joining a decreasing chain from one side with an increasing chain from the other. It can start and end anywhere in the tree. The challenge is that a local parent-child check is not enough: the optimal path may be assembled from both subtrees, so the solution must aggregate directional state bottom-up in linear time.

## 🌍 Engineering Impact
This pattern shows up whenever local adjacency rules must be composed into a maximal global chain over hierarchical data: compiler AST rewrites with stepwise version transitions, search-ranking trees with neighboring score bands, workflow dependency graphs constrained by monotonic stage changes, and filesystem or org-chart analytics over leveled metadata. At scale, naive recomputation from every node becomes quadratic and collapses under deep or wide trees. The bottom-up dynamic-programming approach matters because it turns repeated subtree exploration into a single pass, enabling predictable latency, bounded work per node, and a clean separation between local state propagation and global optimum tracking.

## 🔍 Problem Statement
You are given the root of a binary tree with `1 <= n <= 100000` nodes, where each node stores an integer rank in `[-10^9, 10^9]`. A valid rank chain is any path using only parent-child edges such that every adjacent pair differs by exactly `1`. The path may be strictly increasing, strictly decreasing, or combine both around one middle node.

Return the maximum number of nodes in any such path.

Examples:

- `root = [4,3,5,2,null,null,6,1]` → `6`  
  Longest chain: `1 -> 2 -> 3 -> 4 -> 5 -> 6`

- `root = [10,9,11,8,10,null,12]` → `5`  
  One longest chain: `8 -> 9 -> 10 -> 11 -> 12`

Edge cases matter: a single node is a valid answer of `1`, the path does not need to include the root, and the optimal chain may pass through an internal node by combining two directional subtree paths. The `O(n)` requirement rules out restarting DFS from every node.

## 🪜 How to Solve This
1. Read the path definition → notice this is not just “root-to-leaf” and not just “increasing.” A valid answer can bend once at a node, so we need more than one scalar per subtree.

2. Ask what information a parent needs from a child → only two things matter:
   - the longest downward chain starting at that child that is **increasing by 1** toward the parent,
   - the longest downward chain starting at that child that is **decreasing by 1** toward the parent.

3. That immediately suggests bottom-up DFS → each node can compute its own two directional lengths from its children in constant time.

4. Why two lengths? Because the best path through a node may join:
   - decreasing from one side into the node, and
   - increasing from the other side out of the node,
   or the reverse orientation.

5. So at every node:
   - extend an increasing chain if `child.val == node.val + 1`,
   - extend a decreasing chain if `child.val == node.val - 1`,
   - update a global answer with `inc + dec - 1`.

6. This is tree DP: local directional state, global optimum updated once per node, total work linear in the number of nodes.

## 🧩 Algorithm Walkthrough
1. **Use post-order DFS (Tree Dynamic Programming).**  
   Process children before the parent so each node receives already-computed directional chain lengths. This is the right abstraction because the parent’s state depends only on child summaries, not raw subtree structure.

2. **Define the per-node state.**  
   For each node, compute:
   - `inc`: longest downward path starting at this node where values increase by `1` each step,
   - `dec`: longest downward path starting at this node where values decrease by `1` each step.  
   Invariant: both lengths always include the current node, so they start at `1`.

3. **Merge the left child.**  
   If `left.val == node.val + 1`, then the node can extend an increasing chain through the left child: `inc = max(inc, left.inc + 1)`.  
   If `left.val == node.val - 1`, then it can extend a decreasing chain: `dec = max(dec, left.dec + 1)`.  
   This is correct because only exact `±1` transitions preserve validity.

4. **Merge the right child with the same rules.**  
   Take the maximum contribution from either side for `inc` and `dec`.  
   Invariant: after both merges, `inc` and `dec` are the best single-branch directional chains starting at this node.

5. **Update the global answer.**  
   The best path passing through this node is `inc + dec - 1`. The subtraction avoids double-counting the current node.  
   This captures straight increasing, straight decreasing, and one-turn paths.

6. **Return `(inc, dec)` upward.**  
   Each node contributes constant work after visiting its children, so the full traversal is `O(n)` and visits each node exactly once.

## 📊 Worked Example
Example: `root = [4,3,5,2,null,null,6,1]`

Trace bottom-up:

| Node | Child relation used | `inc` | `dec` | Global best |
|---|---|---:|---:|---:|
| 1 | none | 1 | 1 | 1 |
| 2 | `1 = 2 - 1` → extend `dec` | 1 | 2 | 2 |
| 3 | `2 = 3 - 1` → extend `dec` | 1 | 3 | 3 |
| 6 | none | 1 | 1 | 3 |
| 5 | `6 = 5 + 1` → extend `inc` | 2 | 1 | 3 |
| 4 | left `3 = 4 - 1` → `dec = 4`; right `5 = 4 + 1` → `inc = 3` | 3 | 4 | 6 |

At node `4`, we combine the decreasing chain `4 -> 3 -> 2 -> 1` with the increasing chain `4 -> 5 -> 6`.  
Computed length: `inc + dec - 1 = 3 + 4 - 1 = 6`, giving `1 -> 2 -> 3 -> 4 -> 5 -> 6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each node is visited once, and each visit performs constant work: inspect up to two children, update two directional lengths, and refresh the global maximum. At `10^6` nodes this remains operationally feasible in a single pass; at `10^9`, the issue is no longer algorithmic complexity but memory, I/O, and distribution constraints.

### Space Complexity
`O(h)` auxiliary space for the DFS call stack, where `h` is tree height; worst case `O(n)` for a skewed tree. The algorithm itself stores only constant state per active frame plus a global answer. An iterative post-order traversal can remove recursion-risk at the cost of explicit stack management.

## 💡 Key Takeaways
- If a tree path can start and end anywhere, expect a “compute local state, update global answer” DFS rather than root-to-leaf traversal.
- If the path can combine two directions at a node, one scalar per subtree is usually insufficient; track directional states separately.
- The returned state should represent paths **starting at the current node**, not arbitrary subtree maxima, or parent merges become invalid.
- The combined answer is `inc + dec - 1`; forgetting the `-1` double-counts the pivot node and inflates results.
- In production tree analytics, the scalable move is the same: propagate compact summaries upward instead of recomputing expensive subtree properties from every candidate root.

## 🚀 Variations & Further Practice
- **N-ary tree version:** same idea, but merging becomes more subtle because multiple children may compete to extend increasing and decreasing chains.
- **Difference in `{1, k}` or bounded delta range:** harder because each node may need more state than just two directional lengths.
- **Longest consecutive path in a DAG:** extends the pattern from trees to shared substructure, requiring memoization and cycle-aware reasoning instead of simple post-order DFS.