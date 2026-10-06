# Minimum Relays to Secure Every Root-to-Leaf Route

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Tree DP, Postorder Traversal, Greedy

---

## 🗂 Problem Overview
Given a binary tree, place the fewest relays so every node is secured. A relay secures its parent, itself, and its immediate children. The output is a single integer: the minimum relay count.

The challenge is global dependency with local coverage: whether a node needs a relay depends on what its children do, and naive placement exploration is exponential. With up to 100,000 nodes, the solution must be linear and avoid brute-force subtree combinations.

## 🌍 Engineering Impact
This pattern shows up in hierarchical observability, policy enforcement, and edge-control systems where a control point covers only adjacent layers. Examples include relay or cache placement in CDN trees, security agent rollout in org hierarchies, and health-check aggregation in service dependency trees.

At scale, brute-force placement or top-down local choices fail because coverage decisions are coupled across parent/child boundaries. The right abstraction — postorder state propagation with a tiny state machine — turns an exponential search space into a linear pass. That enables predictable runtime, bounded per-node work, and implementations that remain viable on deep or highly irregular topologies.

## 🔍 Problem Statement
You are given the root of a binary tree with `n` nodes, where `1 <= n <= 100000`. A relay may be installed on any node. If installed on node `u`, it secures `u`, `parent(u)`, and `u`’s immediate children. Every node in the tree must end up secured, including the root and all leaves.

Return the minimum number of relays required. Node values are unique identifiers only; they do not affect placement. Each node has at most two children.

Examples:

- `root = [0,0,null,0,0]` → `1`
- `root = [0,0,null,0,null,0,null,null,0]` → `2`

The key constraint is scale: trying all relay placements is infeasible. The intended solution is `O(n)` time, and recursive implementations must consider stack overflow on skewed trees.

## 🪜 How to Solve This
1. Read the coverage rule carefully → a relay affects only distance 1: parent, self, children. That strongly suggests each node’s decision should depend only on its immediate children’s outcomes.

2. Ask what information a parent really needs from a child → not the full subtree layout, just whether that child:
   - has a relay,
   - is already secured without needing help,
   - or is currently unsecured and needs its parent to act.

3. That gives a tiny state machine per node. Once children are processed, the parent can decide locally:
   - if any child is unsecured, place a relay here;
   - else if any child has a relay, this node is secured;
   - else this node is unsecured.

4. This is naturally postorder: children must be classified before the parent can be classified.

5. The greedy part is delayed placement. Never place a relay early. Only place one at a node when a child forces it. That is what makes the count minimal.

6. After traversal, handle the root separately: if it is still unsecured, add one final relay.

## 🧩 Algorithm Walkthrough
1. **Use Postorder Traversal + 3-State Tree DP / Greedy classification.**  
   Process left subtree, then right subtree, then the current node. This is the right abstraction because a node’s optimal action depends only on the final states of its children, not on deeper structure once those states are summarized.

2. **Define node states compactly.**  
   Use three states:
   - `HAS_RELAY`
   - `SECURED`
   - `UNSECURED`  
   Null children are treated as `SECURED`, because they require no coverage and should never force relay placement.

3. **Combine child states at the parent.**  
   After visiting both children:
   - If either child is `UNSECURED`, install a relay at the current node. Increment the answer and return `HAS_RELAY`.
   - Else if either child is `HAS_RELAY`, the current node is covered by that child, so return `SECURED`.
   - Else both children are `SECURED` without relays, so the current node is currently uncovered and returns `UNSECURED`.

4. **Why this is correct.**  
   A child marked `UNSECURED` can only be covered by itself or its parent. Since traversal is postorder, the child already chose not to place a relay in its own subtree, so the parent is the last valid place to cover it. Installing the relay there is forced, not speculative.

5. **Finalize at the root.**  
   The root has no parent, so if traversal returns `UNSECURED` for the root, add one more relay. This preserves the invariant that every node is covered exactly when its parent decision is complete.

6. **Operational note.**  
   Recursive DFS is simplest, but for skewed trees with 100,000 nodes, an iterative postorder with an explicit stack is safer in production-grade code.

## 📊 Worked Example
Example: `root = [0,0,null,0,0]`

Trace using states `R = HAS_RELAY`, `S = SECURED`, `U = UNSECURED`.

| Node processed | Left state | Right state | Action | Return state | Relays |
|---|---:|---:|---|---:|---:|
| left.left leaf | S | S | no child forces relay | U | 0 |
| left.right leaf | S | S | no child forces relay | U | 0 |
| left child | U | U | child uncovered → place relay | R | 1 |
| root | R | S(null) | covered by child relay | S | 1 |

Numbered view:
1. Leaves return `UNSECURED` because nothing below them covers them.
2. Their parent sees uncovered children, so it must place one relay.
3. That relay secures itself, both leaf children, and the root.
4. Root is already secured, so no extra relay is needed.

Final answer: `1`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each node is visited once, and each visit performs constant-time state combination from at most two children. At `10^6` nodes this is still practical as a single linear pass; at `10^9`, asymptotically optimal still means the tree itself is too large for ordinary memory and traversal budgets.

### Space Complexity
`O(h)` auxiliary space for traversal, where `h` is tree height. In a balanced tree this is `O(log n)`; in a skewed tree it becomes `O(n)`. The space is owned by the call stack in recursive DFS or an explicit stack in iterative postorder.

## 💡 Key Takeaways
- If a tree problem asks for a minimum count under parent/child coverage rules, look for a small per-node state summary rather than explicit placement enumeration.
- Postorder is the signal when a parent’s optimal action depends on fully resolved child outcomes.
- Treat `null` as already secured; otherwise leaves incorrectly force extra relays.
- The root is a special case: an `UNSECURED` internal node may be covered by its parent, but the root cannot.
- In production systems, local state compression is often what converts globally coupled placement problems into linear, composable traversals.

## 🚀 Variations & Further Practice
- **Generalize from binary trees to N-ary trees.** Same state machine, but child aggregation becomes over an arbitrary fan-out; useful for org charts and service dependency DAG-like trees after tree extraction.
- **Weighted relay placement.** Each node has a placement cost; minimize total cost instead of relay count. This turns the greedy state machine into a fuller tree DP with costed states.
- **Distance-`k` coverage instead of distance-1.** A relay secures nodes within `k` edges. The state must encode distance-to-nearest-relay information, increasing both state space and implementation complexity.