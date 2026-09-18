# Minimum Relays to Seal a Firebreak Tree

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Tree DP, Dynamic Programming

---

## 🗂 Problem Overview
Given a tree rooted at `r`, fire may start at any leaf and move upward one edge per minute. A beacon placed on node `u` protects `u`, its parent, and its immediate children; fire stops as soon as it enters any protected node. The goal is to place the fewest beacons so every leaf-to-root path contains at least one protected node. With `n` up to `2 * 10^5`, brute-force path coverage or per-leaf simulation is not viable.

## 🌍 Engineering Impact
This pattern shows up in hierarchical containment and interception systems: isolating blast radius in service dependency trees, placing enforcement points in network topologies, inserting cache or validation barriers in compiler IR trees, and choosing minimal checkpoint nodes in workflow DAGs reduced to trees. At scale, local placement decisions have non-local effects because one action protects both upward and downward neighbors. Without a stateful tree DP, teams either overprovision controls or ship heuristics that fail on skewed topologies. The right formulation gives deterministic minimality, linear scaling, and a reusable decision model for hierarchical risk containment.

## 🔍 Problem Statement
You are given an undirected tree with `n` nodes labeled `0..n-1` and a designated root `r`. Rooting is only used to define parent/child relationships. A beacon on node `u` protects `u`, `parent(u)` if it exists, and every direct child of `u`. Fire can start from any leaf in the rooted tree and spreads only upward toward the root. If it reaches a protected node, propagation stops immediately.

Compute the minimum number of beacons required so that no fire starting from any leaf can ever reach root `r`.

Constraints:
- `1 <= n <= 2 * 10^5`
- `edges.length == n - 1`
- input is a valid tree
- expected solution: `O(n)` or `O(n log n)`

Examples:
- `n=7, edges=[[0,1],[0,2],[1,3],[1,4],[2,5],[2,6]], r=0` → `2`
- `n=8, edges=[[3,0],[3,1],[3,4],[4,2],[4,5],[5,6],[5,7]], r=3` → `2`

The key difficulty is that a beacon placed in a subtree may also protect its parent, changing what ancestors still need to do.

## 🪜 How to Solve This
1. Start from the requirement, not the mechanics: every leaf-to-root path must hit at least one protected node. That is a path-hitting problem on a rooted tree.

2. Notice the awkward part: a beacon does not just cover the node where it is placed. It reaches one level up and one level down. That means a decision inside a child subtree can satisfy the parent.

3. That immediately rules out simple greedy strategies like “place beacons above leaves” or “place them as high as possible.” A locally good placement can block a parent obligation and change sibling interactions.

4. This is the signal for **tree DP**: process children first, summarize each subtree into a small state, then combine those summaries at the parent.

5. The right state is not “how many leaves are covered,” but “what does this subtree still require from its parent?” In practice, each node can be classified into a small number of statuses:
   - it has a beacon,
   - it is already protected by a child beacon,
   - it is still unprotected and would allow fire through unless its parent handles it.

6. Run a postorder traversal, compute the cheapest valid state per node, and resolve the root explicitly because it has no parent to rescue it.

## 🧩 Algorithm Walkthrough
1. **Root the tree at `r`** using DFS or BFS, building a parent array and child lists.  
   Why: beacon coverage is asymmetric once the tree is rooted.  
   Invariant: every edge is oriented parent → child exactly once.

2. **Define a 3-state tree DP** for each node `u`:
   - `dp0[u]`: minimum beacons if `u` has a beacon.
   - `dp1[u]`: minimum beacons if `u` has no beacon but is protected by at least one child beacon.
   - `dp2[u]`: minimum beacons if `u` has no beacon and is not protected from below, so it must rely on its parent.  
   This is the standard **Tree DP with local state compression** pattern.

3. **Initialize leaves**:
   - `dp0[leaf] = 1`
   - `dp1[leaf] = INF` because no child can protect it
   - `dp2[leaf] = 0` because it may defer protection to its parent  
   Invariant: each state exactly captures what obligation leaves upward.

4. **Combine child states for an internal node `u`**:
   - If `u` has a beacon (`dp0`), every child is automatically protected by its parent, so each child may be in either `dp0` or `dp2`; choose `min(dp0[v], dp2[v])`.
   - If `u` is protected by a child (`dp1`), at least one child must be in `dp0`; all other children must independently block fire within their subtrees, so they may be in `dp0` or `dp1`.
   - If `u` relies on its parent (`dp2`), no child beacon may protect `u`, so every child must already block fire internally without depending on `u`; choose `dp1[v]` for every child.

5. **Efficiently compute `dp1[u]`** by taking the sum of `min(dp0[v], dp1[v])` over children, then forcing at least one child into `dp0` via the minimum upgrade cost `dp0[v] - min(dp0[v], dp1[v])`.

6. **Answer at the root** is `min(dp0[r], dp1[r])`.  
   Why: the root has no parent, so `dp2[r]` is invalid.  
   Invariant: every leaf-to-root path is blocked before reaching `r`.

## 📊 Worked Example
Example: `n=7`, root `0`, edges form a full binary tree with children `1,2` and leaves `3,4,5,6`.

| Node | Children | `dp0` | `dp1` | `dp2` |
|---|---|---:|---:|---:|
| 3 | - | 1 | INF | 0 |
| 4 | - | 1 | INF | 0 |
| 5 | - | 1 | INF | 0 |
| 6 | - | 1 | INF | 0 |

Now process internal nodes:

1. Node `1` with children `3,4`  
   - `dp0[1] = 1 + min(1,0) + min(1,0) = 1`  
   - `dp1[1] = 1 + 1 = 2` because one child must carry a beacon  
   - `dp2[1] = INF` since children cannot be in `dp1`

2. Node `2` is symmetric: `(1,2,INF)`

3. Root `0` with children `1,2`  
   - `dp0[0] = 1 + min(1,INF) + min(1,INF) = 3`  
   - `dp1[0] = (1+1) + force one child beacon = 2`  
   - `dp2[0]` invalid for final answer

Result: `min(dp0[0], dp1[0]) = 2`, achieved by placing beacons at `1` and `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Rooting the tree touches each edge once, and the postorder DP also processes each node and edge a constant number of times. This remains practical at `10^6` operations scale; at `10^9`, even linear scans become expensive, which is why avoiding superlinear subtree recomputation is essential.

### Space Complexity
`O(n)`. The adjacency list, parent/children representation, traversal order, and three DP arrays dominate memory. You can reduce constants by reusing arrays or storing only adjacency plus parent/order, but asymptotically it stays linear unless you trade clarity for in-place traversal tricks.

## 💡 Key Takeaways
- If a tree problem says a node’s decision affects both its parent and children, expect a small-state tree DP rather than greedy placement.
- If the requirement is “every leaf-to-root path must intersect something,” think path hitting / cut placement, not full-node coverage.
- The root is a special case: any state that depends on parent protection is invalid there.
- Leaves can legally be in the “defer to parent” state; forbidding that too early overcounts beacons.
- The transferable design insight is to summarize each subtree by the obligation it exports upward, which is the same compression move used in scalable hierarchical control systems.

## 🚀 Variations & Further Practice
- Weighted version: each node has a beacon installation cost; minimize total cost instead of count. Same DP shape, but every transition becomes cost-aware.
- Radius-`k` protection: a beacon protects nodes within distance `k`. The state space expands from constant-size local statuses to distance-sensitive DP.
- General graph variant on a DAG or arbitrary graph: leaf-to-root interception becomes a cut / feedback-style problem, and tree DP no longer applies directly.