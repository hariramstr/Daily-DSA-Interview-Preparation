# Longest Alternating Turn Path in a Binary Tree

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Depth-First Search, Dynamic Programming

---

## 🗂 Problem Overview
Given the root of a binary tree, compute the maximum length of any downward path whose edge directions strictly alternate between left and right. The path may start at any node, may stop anywhere, and length is measured in edges, so a single node contributes 0. The challenge is that local choices are not independent: to extend a path from a node, you must know which direction was used last, and brute-forcing every start node and direction is too expensive at up to 100,000 nodes.

## 🌍 Engineering Impact
This pattern shows up anywhere validity of a path depends on prior transition state, not just current position: query planners alternating join strategies, workflow engines enforcing step-type alternation, packet inspection over protocol state transitions, and graph-based ranking pipelines with constrained edge traversal. At scale, naive re-evaluation from every candidate start becomes quadratic and collapses under deep or wide structures. The useful abstraction is stateful tree DP: summarize, per node, the best continuation under each incoming state. That reduces repeated work, keeps traversal linear, and makes the dependency explicit enough to parallelize, test, and reason about in larger recursive planners.

## 🔍 Problem Statement
You are given a binary tree with `1 <= n <= 100000` nodes. Each node value is arbitrary, may be negative, and is irrelevant to the path rule. A valid path:

- starts at any node,
- moves only downward along parent-to-child edges,
- chooses either left or right at each step,
- must alternate directions on every move.

Return the maximum path length in **edges**. A single node is a valid path of length `0`.

Examples:

- `root = [1,2,3,null,4,5,null,null,null,6]` → `2`
- `root = [7,9,8,4,null,null,10,null,5]` → `3`

Important edge cases:

- one-node tree → answer `0`
- skewed tree with only one direction available → answer at most `1`
- longest path may start below the root

The key constraint is `O(n)` expected time. Any approach that tries every node as a fresh starting point and walks downward repeatedly will time out on large trees.

## 🪜 How to Solve This
1. Read the problem → the path is downward only, so this is not a general graph search problem; subtree structure matters.
2. Notice the alternation rule depends on the **previous move direction**. That means one value per node is not enough.
3. Ask: “If I am at node `x`, what is the longest path starting here if my first move is left? What if my first move is right?” Now the state is well-defined.
4. That naturally suggests tree DP with two states per node:
   - `leftLen`: longest alternating path starting at this node by going left first
   - `rightLen`: same, but going right first
5. These states depend only on children:
   - if you go left first, the next move must be right, so use the left child’s `rightLen`
   - if you go right first, use the right child’s `leftLen`
6. Compute these bottom-up with DFS. At each node, update a global maximum using both states.
7. This avoids restarting searches from every node. Each node contributes constant work, giving linear time.

## 🧩 Algorithm Walkthrough
1. **Use depth-first search with tree dynamic programming.**  
   The pattern is **DFS + DP on trees**. It fits because each node’s optimal answer can be derived from optimal answers of its children, and there are no cycles.

2. **Define the return value for each node as a pair `(goLeft, goRight)`.**  
   - `goLeft`: longest alternating path starting at this node if the first edge taken is to the left  
   - `goRight`: longest alternating path starting at this node if the first edge taken is to the right  
   This state is sufficient because the only historical fact that matters is the next required direction.

3. **Handle the null base case.**  
   For a missing node, return `(-1, -1)` or equivalently treat absent children specially. Using `-1` is convenient:  
   `1 + (-1) = 0` would be wrong here, so better to compute only when a child exists; otherwise the corresponding length is `0`.  
   Invariant: every returned value is the best valid edge count from that node under the specified first move.

4. **Recurse into left and right children first.**  
   This is postorder traversal. By the time you compute the current node, both child summaries are already available.

5. **Compute current states from child states.**  
   - If `node.left` exists, `goLeft = 1 + leftChild.goRight`; else `0`
   - If `node.right` exists, `goRight = 1 + rightChild.goLeft`; else `0`  
   Correctness follows directly from alternation: after taking one direction, the next step must flip.

6. **Update the global maximum with both states.**  
   The longest valid path can start anywhere, so every node is a candidate start.  
   Invariant: after processing a node, the global maximum reflects all fully processed subtrees.

7. **Return the global maximum after DFS completes.**  
   Each node is visited once, each edge is considered a constant number of times, so the solution is optimal for the stated constraints.

## 📊 Worked Example
Consider `root = [1,2,3,null,4,5,null,null,null,6]`.

Tree shape:
- `1.left = 2`, `1.right = 3`
- `2.right = 4`
- `3.left = 5`
- `5.left = 6`

Postorder trace:

| Node | left child state | right child state | goLeft | goRight | globalMax |
|---|---:|---:|---:|---:|---:|
| 4 | none | none | 0 | 0 | 0 |
| 2 | none | (0,0) | 0 | 1 | 1 |
| 6 | none | none | 0 | 0 | 1 |
| 5 | (0,0) | none | 1 | 0 | 1 |
| 3 | (1,0) | none | 1 | 0 | 1 |
| 1 | (0,1) | (1,0) | 2 | 2 | 2 |

Interpretation:
- From node `1`, going left gives `1 -> 2 -> 4` with directions `L, R`, length `2`.
- Going right gives `1 -> 3 -> 5` with directions `R, L`, also length `2`.

Answer: `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is the number of nodes. Each node is visited exactly once, and its two DP states are computed from already-known child results in constant time. At `10^6` nodes this is still practical in a single pass; at `10^9`, tree materialization itself dominates and the problem becomes a systems constraint, not an algorithmic one.

### Space Complexity
`O(h)` auxiliary space for the recursion stack, where `h` is tree height. In a balanced tree this is `O(log n)`; in a skewed tree it becomes `O(n)`. You can replace recursion with an explicit stack to avoid call-stack limits, but asymptotic auxiliary space remains height-bounded.

## 💡 Key Takeaways
- If a tree path constraint depends on the **previous move**, expect a DP state keyed by direction or prior action, not just by node.
- If the path may start at any node, that is a strong signal to compute a local optimum at every node and aggregate with a global maximum.
- Length is measured in **edges**, not nodes; leaf-like starts therefore contribute `0`, not `1`.
- Do not force paths to begin at the root or end at a leaf; both assumptions silently undercount valid answers.
- The transferable design insight is to encode exactly the minimal historical state needed for future decisions, which is the same move used in production planners, automata, and constrained traversal engines.

## 🚀 Variations & Further Practice
- **Longest ZigZag Path with path reconstruction**: same DP, but also return parent-choice metadata so you can reconstruct one optimal path instead of only its length.
- **Alternating path in an N-ary tree with labeled edges**: the twist is that alternation is over edge labels or categories, not fixed left/right positions, so the state space becomes label-dependent.
- **Longest alternating path in a general DAG**: subtree independence disappears; you need topological DP and must handle multiple incoming paths with different prior-direction states.