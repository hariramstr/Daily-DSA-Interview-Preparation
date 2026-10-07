# Longest Zigzag Turn Path in a Binary Tree

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Depth-First Search, Dynamic Programming

---

## 🗂 Problem Overview
Given the root of a binary tree, compute the maximum length of any downward path whose edge directions strictly alternate between left and right. The path can start at any node, not just the root, and its length is measured in edges. The challenge is that the tree can contain up to `10^5` nodes and may be highly unbalanced, so brute-force exploration from every node is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere a hierarchical structure carries state that depends on the previous transition: query planners alternating join strategies, workflow engines validating allowed step transitions, compiler passes over ASTs with context-sensitive traversal rules, and fraud/risk pipelines detecting alternating event sequences in decision trees. At scale, recomputing path properties independently from every node creates quadratic blowups and poor cache behavior. The right approach pushes state through a single traversal, turning a global optimization problem into local transition updates. That enables predictable latency, bounded memory growth, and implementations that remain viable on deep or skewed trees.

## 🔍 Problem Statement
You are given a binary tree with `1` to `10^5` nodes. Each node value is arbitrary and irrelevant to the answer. A valid zigzag turn path moves only downward, from parent to child, and each successive edge must alternate direction: left, right, left, right, or right, left, right, left. The path may begin at any node. Its length is the number of edges, so a single node alone has length `0`.

Return the maximum zigzag path length anywhere in the tree.

Examples:

- `root = [1,null,2,3,4,null,null,5,null]` → `2`  
  One valid longest path: `2 -> 3 -> 5`

- `root = [7,4,9,2,6,null,10,null,3,5,null]` → `3`  
  One valid longest path: `7 -> 4 -> 6 -> 5`

The key constraint is tree size: `10^5` nodes rules out restarting a full search from every node.

## 🪜 How to Solve This
1. Read the problem → the path condition depends on the **previous move direction**, not on node values.
2. That means each node does not have one state, but two:  
   → longest zigzag starting here if the **next move goes left**  
   → longest zigzag starting here if the **next move goes right**
3. Once you see “answer at a node depends on child answers with flipped state,” this becomes tree DP over a DFS traversal.
4. Suppose you move left from a node. The next move must be right. So the left-based answer is `1 + child.rightState`.
5. Symmetrically, if you move right first, the answer is `1 + child.leftState`.
6. A null child cannot extend a path, so treat missing branches as contributing `-1`; then `1 + (-1) = 0`, which correctly gives zero edges for a leaf state.
7. Traverse once in postorder, compute both states per node, and update a global maximum.
8. This avoids exploring every possible start separately because every node already encodes the best path starting there in both directional contexts.

## 🧩 Algorithm Walkthrough
1. **Use Depth-First Search + Tree Dynamic Programming.**  
   The right abstraction is DP on a tree because each node’s optimal zigzag lengths depend only on its children’s optimal zigzag lengths under the opposite direction constraint.

2. **Define the per-node state.**  
   For each node, compute a pair:  
   - `leftLen`: longest zigzag path starting at this node if the first move is to the left  
   - `rightLen`: longest zigzag path starting at this node if the first move is to the right`  
   This state is sufficient because the future validity of the path depends only on the last direction taken.

3. **Handle the base case cleanly.**  
   For a null node, return `(-1, -1)`.  
   Why `-1` instead of `0`? Because for a leaf, `1 + (-1) = 0`, which correctly means no outgoing edge exists, so the longest path starting there has length zero.

4. **Compute transitions bottom-up.**  
   Recurse into `node.left` and `node.right`.  
   Then:
   - `leftLen = leftChild.rightLen + 1`
   - `rightLen = rightChild.leftLen + 1`  
   This maintains the invariant that every computed state already represents a valid alternating downward path.

5. **Track the global optimum.**  
   At each node, update `answer = max(answer, leftLen, rightLen)`.  
   This is necessary because the best path may start anywhere, not necessarily at the root.

6. **Return the final maximum.**  
   A single traversal visits each node once, computes constant work per node, and captures all valid starts without redundant searches.

## 📊 Worked Example
Example: `root = [7,4,9,2,6,null,10,null,3,5,null]`

Postorder trace:

| Node | Left child state | Right child state | `leftLen` | `rightLen` | Global max |
|---|---:|---:|---:|---:|---:|
| 3 | `(-1,-1)` | `(-1,-1)` | 0 | 0 | 0 |
| 2 | `(-1,-1)` | `(0,0)` | 0 | 1 | 1 |
| 5 | `(-1,-1)` | `(-1,-1)` | 0 | 0 | 1 |
| 6 | `(0,0)` | `(-1,-1)` | 1 | 0 | 1 |
| 4 | `(0,1)` | `(1,0)` | 2 | 2 | 2 |
| 10 | `(-1,-1)` | `(-1,-1)` | 0 | 0 | 2 |
| 9 | `(-1,-1)` | `(0,0)` | 0 | 1 | 2 |
| 7 | `(2,2)` | `(0,1)` | 3 | 1 | 3 |

At node `7`, going left gives `1 + rightLen(4) = 1 + 2 = 3`, corresponding to `7 -> 4 -> 6 -> 5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is the number of nodes. Each node is visited exactly once, and each visit performs constant-time state combination from its children. At `10^6` nodes this remains linear and practical; at `10^9`, the algorithmic shape is still optimal, but memory and traversal time become system-level constraints.

### Space Complexity
`O(h)` for the recursion stack, where `h` is tree height; worst case is `O(n)` for a fully skewed tree. No auxiliary map or DP table is required beyond call frames. An iterative postorder traversal can remove recursion-risk at the cost of more explicit stack management.

## 💡 Key Takeaways
- If a tree path constraint depends on the **previous edge direction**, expect a small per-node DP state rather than a single scalar answer.
- If the best path can start at **any node**, compute local optima everywhere and maintain a global maximum during traversal.
- Path length is measured in **edges**, not nodes; a leaf contributes `0`, not `1`.
- Returning `(-1, -1)` for null nodes is a deliberate trick to make leaf transitions compute correctly without special casing.
- The transferable design insight is to encode just enough transition state locally so a global optimization collapses into one linear pass over the structure.

## 🚀 Variations & Further Practice
- **Longest Zigzag Path with path reconstruction:** return both length and the actual node sequence; harder because state must preserve predecessor choices without breaking linear complexity.
- **N-ary tree with alternating edge labels:** generalize from left/right to arbitrary labeled transitions; harder because the state space grows from 2 directions to `k` transition classes.
- **Longest alternating path in a DAG:** same “previous transition matters” idea, but no tree structure; harder because you need topological DP and must handle multiple incoming paths.