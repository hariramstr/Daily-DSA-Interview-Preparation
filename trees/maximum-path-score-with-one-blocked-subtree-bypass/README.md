# Maximum Path Score With One Blocked Subtree Bypass

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Tree DP, DFS, Dynamic Programming

---

## 🗂 Problem Overview
Given a rooted tree, each node contributes a score, and some nodes are blocked. A root-to-leaf path is normally invalid if it touches any blocked node, except that one time you may bypass a blocked node by skipping it and jumping directly to one of its children. The goal is to return the maximum achievable root-to-leaf score, or `null` if no such route exists. The challenge is that path validity depends on both tree structure and whether the single bypass has already been consumed.

## 🌍 Engineering Impact
This pattern shows up in systems that traverse hierarchical dependency graphs under constrained failure budgets: workflow engines with one retryable failed stage, query planners that may skip one invalid operator, package/deployment trees with one tolerated unhealthy component, or search/ranking pipelines with one fallback hop across a disabled node. At scale, brute-force path enumeration collapses immediately because the state is not just “where am I?” but also “have I spent my exception budget?”. Tree DP matters because it turns a global policy constraint into local state transitions, enabling linear-time evaluation over large hierarchies.

## 🔍 Problem Statement
You are given a rooted tree with `n` nodes labeled `0..n-1`, rooted at `0`. Each node `i` has score `value[i]`, which may be negative. Some nodes are listed in `blocked`; these nodes cannot normally appear on a valid root-to-leaf route.

You may use **at most one bypass** on a route. If you reach a blocked node `b`, you may skip visiting `b` and jump directly to one of its children `c`. The skipped node contributes no score. This bypass applies only once and only to that blocked node; later blocked nodes remain blocked. If a blocked node is a leaf, bypassing it is impossible.

Return the maximum route score, or `null` if no valid route exists.

- `1 <= n <= 200000`
- `value[i] ∈ [-10^9, 10^9]`
- `edges.length == n - 1`
- `blocked` contains distinct nodes

Examples:

- `value = [5,4,-2,7,3,6]`, `blocked = [1]` → answer `12`
- `value = [2,-5,10,1,4]`, `blocked = [1,2]` → answer `null`

The `n = 200000` bound rules out per-path exploration; the solution must aggregate subtree answers in linear or near-linear time.

## 🪜 How to Solve This
1. Read the problem → this is not just “best root-to-leaf path”. Path legality depends on whether we have already used a one-time exception.
2. One-time exception → introduce state. For each node, we need the best score under two conditions: bypass still available, or bypass already used.
3. Tree + path optimization + small state → think **tree DP**. Each subtree should summarize the best achievable suffix path for each state.
4. Work bottom-up. A parent’s best route depends only on the best child route in each state.
5. Handle blocked nodes carefully:
   - If bypass already used and current node is blocked, this subtree is dead.
   - If bypass still available and current node is blocked, we cannot “visit” it; we may only skip to one child, which consumes the bypass.
6. Leaves define the base cases. An unblocked leaf contributes its own value. A blocked leaf is impossible unless it can be bypassed — but it has no child, so still impossible.
7. Once each node computes two DP values, the answer is the root’s value in the “bypass available” state.

This is the standard mental move: encode the policy as finite state, then let DFS compose local decisions.

## 🧩 Algorithm Walkthrough
1. **Build the rooted tree adjacency.**  
   Convert `edges` into children lists rooted at `0` using one DFS/BFS or by storing undirected adjacency and traversing with parent tracking.  
   **Invariant:** every node is processed exactly once in parent → child order.

2. **Define DP states.**  
   Let:
   - `dp0[u]` = maximum score from node `u` to some leaf if we arrive at `u` with the bypass **already used**.
   - `dp1[u]` = maximum score from node `u` to some leaf if we arrive at `u` with the bypass **still available**.  
   Use `-∞` to represent impossible states.  
   This is classic **Tree DP with finite path state**.

3. **Process children before parent.**  
   Run postorder DFS so every child’s `dp0/dp1` is known before computing the parent.  
   **Why:** parent transitions are max-over-children compositions of child states.

4. **Compute unblocked node transitions.**  
   If `u` is unblocked:
   - If `u` is a leaf, both `dp0[u]` and `dp1[u]` are `value[u]`.
   - Otherwise:
     - `dp0[u] = value[u] + max(dp0[child])`
     - `dp1[u] = value[u] + max(dp1[child])`  
   **Invariant:** visiting an unblocked node preserves bypass state.

5. **Compute blocked node transitions.**  
   If `u` is blocked:
   - `dp0[u] = -∞` because we cannot visit a blocked node after spending the bypass.
   - `dp1[u] = max(dp0[child])` because the only legal move is to skip `u`, choose one child, and consume the bypass.  
   No `value[u]` is added because `u` is not visited.  
   **Invariant:** bypass is consumed exactly once, at the skipped blocked node.

6. **Return the root answer.**  
   If `dp1[0]` is impossible, return `null`; otherwise return `dp1[0]`.  
   Root blocked is naturally handled: `dp1[0]` becomes the best `dp0` among its children.

## 📊 Worked Example
Use Example 1:

`value = [5,4,-2,7,3,6]`  
Edges: `0→{1,2}`, `1→{3,4}`, `2→{5}`  
Blocked: `{1}`

| Node | Blocked | Children | `dp0` | `dp1` |
|---|---:|---|---:|---:|
| 3 | No | [] | 7 | 7 |
| 4 | No | [] | 3 | 3 |
| 5 | No | [] | 6 | 6 |
| 1 | Yes | [3,4] | `-∞` | `max(7,3)=7` |
| 2 | No | [5] | `-2+6=4` | `-2+6=4` |
| 0 | No | [1,2] | `5+max(-∞,4)=9` | `5+max(7,4)=12` |

Trace:
1. Leaves contribute their own values.
2. Node `1` is blocked, so with bypass unused we may skip it and jump to child `3` or `4`; best is `7`.
3. Node `2` is normal, so it contributes `-2` plus child `5`.
4. At root `0`, with bypass available, choose between subtree `1` using the skip (`7`) and subtree `2` normally (`4`).
5. Final answer: `5 + 7 = 12`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each node is visited once, and each edge is examined a constant number of times while building the rooted structure and computing DP transitions. This scales comfortably to `10^6` nodes in optimized environments; at `10^9`, even linear traversal is infeasible, so the asymptotic target is already optimal.

### Space Complexity
`O(n)`. The space is owned by the adjacency list, blocked-set membership structure, and two DP arrays. It can be reduced slightly by storing results in-place or using iterative postorder traversal, but asymptotically the tree representation still dominates.

## 💡 Key Takeaways
- If a tree path problem includes a one-time privilege, coupon, retry, or exception, model it as a small DP state rather than branching over all paths.
- If validity depends on both node content and “history so far”, that is a strong signal for tree DP with per-node state summaries.
- A blocked node does not behave like a very negative score; it changes reachability. Treat impossible states explicitly with `-∞`/sentinel logic.
- The bypass skips only the blocked node itself, so `value[blocked]` must not be added, and the child transition must consume the state exactly once.
- The production-grade lesson is to encode policy budgets as local state machines; once the state is explicit, large hierarchical evaluations become linear and composable.

## 🚀 Variations & Further Practice
- Allow up to `k` bypasses instead of one. The conceptual twist is expanding the DP state from 2 states to `k+1`, forcing careful memory and transition design.
- Allow bypassing a blocked node to any descendant within distance `d`, not just a child. This turns a local transition into a constrained subtree optimization problem.
- Replace root-to-leaf with maximum score between any two nodes under one bypass. The harder part is combining downward and upward DP while preserving the single-use constraint.