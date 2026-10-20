# Maximum Recovered Signal by Cutting One Corrupted Branch

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Dynamic Programming, DFS

---

## 🗂 Problem Overview
Given a rooted tree with signed node values, remove at most one non-root subtree to maximize the resulting root-to-leaf path sum. The output is a single 64-bit integer: the best recovered signal after either making one cut or leaving the tree unchanged. The difficulty is that one cut changes which nodes are leaves, so the optimal answer cannot be derived from a single local decision or a naive recomputation per candidate cut.

## 🌍 Engineering Impact
This pattern shows up anywhere a hierarchical dependency graph contains a potentially toxic branch and the system must preserve the best surviving end-to-end path. Examples include streaming pipelines disabling one bad operator subtree, search/ranking cascades dropping a degraded feature family, compiler optimization passes pruning one invalid transformation region, and distributed policy trees isolating one faulty rule branch. At scale, brute-force “remove each subtree and recompute” collapses under latency and cost. Tree DP with sibling-aware propagation enables single-pass evaluation, predictable performance, and makes failure-isolation decisions explainable rather than heuristic.

## 🔍 Problem Statement
You are given a connected acyclic rooted tree with `n` nodes, numbered `0..n-1`, rooted at node `0`. Each node `i` has an integer `value[i]`, possibly negative. You may cut **at most one** edge between a non-root node `x` and its parent, removing the entire subtree rooted at `x`. The root cannot be removed.

After the optional cut, the recovered signal is the maximum sum over all remaining root-to-leaf paths. Because removing a subtree can eliminate all children of a node, an internal node may become a leaf in the remaining tree.

Return the maximum possible recovered signal.

Constraints:
- `2 <= n <= 2 * 10^5`
- `edges.length == n - 1`
- `-10^9 <= value[i] <= 10^9`
- Answer fits in signed 64-bit integer

Examples:
- `value = [5,4,-10,3,2,7]`, `edges = [[0,1],[0,2],[1,3],[1,4],[2,5]]` → `12`
- `value = [8,-5,6,-2,4,-1,3]`, `edges = [[0,1],[0,2],[1,3],[1,4],[2,5],[5,6]]` → `16`

The `n = 2e5` bound rules out recomputing the best path for every possible cut.

## 🪜 How to Solve This
1. Read the objective carefully → after one cut, we still want the best **root-to-leaf** path, not the best arbitrary connected component.
2. Notice what a cut really does → it removes exactly one child option from some parent. That means the answer after cutting subtree `x` depends on what the parent can do **without** child `x`.
3. Compute the normal best downward path first → for every node, store the maximum root-to-leaf sum starting at that node.
4. Then ask the reverse question → for each node `x`, what is the best recovered signal if `x` is the subtree being removed?
5. That value is determined at `parent(x)`:
   - either the parent becomes a leaf after removing `x`,
   - or it uses one of its other children,
   - and everything above the parent is unchanged.
6. To evaluate that in O(1) per child, each node needs the best and second-best child contributions.
7. Propagate from root downward a value meaning: “best root-to-leaf sum available in the current tree if this node’s entire subtree is excluded.”
8. That turns a seemingly global optimization into two DFS passes of local transitions.

## 🧩 Algorithm Walkthrough
1. **Build the rooted adjacency list**  
   Store children for each node from the edge list. The tree is already rooted at `0`, so one DFS/BFS can establish parent/children orientation if needed. This gives the traversal structure for both passes.

2. **Bottom-up DP: compute `down[u]`**  
   Pattern: **Tree DP via postorder DFS**.  
   `down[u]` = maximum sum of a path that starts at `u` and ends at any leaf in `u`’s subtree.  
   If `u` is a leaf, `down[u] = value[u]`. Otherwise,  
   `down[u] = value[u] + max(down[child])`.  
   Invariant: after processing `u`, `down[u]` is exact for its subtree.

3. **Track top two child contributions per node**  
   For each node, record:
   - `best1[u]`: largest `down[child]`
   - `best2[u]`: second largest `down[child]`
   - optionally which child achieved `best1[u]`  
   This is the sibling-exclusion mechanism. If child `x` is removed, parent `p` can still use `best2[p]` when `x` was the best child.

4. **Top-down DP: compute `out[u]`**  
   Pattern: **Reroot-style propagation / outside-subtree DP**.  
   `out[u]` = best root-to-leaf sum obtainable in the remaining tree if subtree `u` is cut away.  
   For a child `v` of parent `p`, the remaining tree’s best path is the maximum of:
   - `out[p]`: best path that avoids `p`’s subtree entirely
   - path ending at `p` after removing `v`: prefix-to-`p` sum plus either
     - no remaining child, so `p` becomes leaf
     - best sibling contribution excluding `v`

5. **Maintain root-to-node prefix sums**  
   Let `pref[u]` be sum from root to `u`. Then if child `v` is cut, the best path through parent `p` is:  
   `pref[p] + max(0 if p becomes leaf? no — use value already in pref, then add sibling tail only if exists)`  
   More concretely:
   - if `p` has another child after excluding `v`, candidate = `pref[p] + bestSiblingDown`
   - otherwise candidate = `pref[p]`  
   Invariant: this exactly models the new leaf semantics after deletion.

6. **Answer extraction**  
   The no-cut answer is `down[0]`.  
   Every valid cut corresponds to some non-root node `u`, with resulting signal `out[u]`.  
   Final answer = `max(down[0], max(out[u] for u != 0))`.

This works because every single-cut outcome is uniquely identified by the removed subtree root, and each such outcome depends only on ancestor context plus sibling alternatives.

## 📊 Worked Example
Use `value = [5,4,-10,3,2,7]`, `edges = [[0,1],[0,2],[1,3],[1,4],[2,5]]`.

| Node | `pref` | `down` | Best child downs |
|---|---:|---:|---|
| 3 | 12 | 3 | none |
| 4 | 11 | 2 | none |
| 1 | 9 | 7 | `3, 2` |
| 5 | 2 | 7 | none |
| 2 | -5 | -3 | `7` |
| 0 | 5 | 12 | `7 (via 1), -3 (via 2)` |

Trace:
1. Bottom-up: `down[3]=3`, `down[4]=2`, so `down[1]=4+max(3,2)=7`.
2. `down[5]=7`, so `down[2]=-10+7=-3`.
3. Root: `down[0]=5+max(7,-3)=12`.
4. Cutting node `2`: parent is `0`. Excluding child `2`, root can still use child `1`, so recovered signal is `pref[0] + 7 = 12`.
5. Cutting node `1`: root falls back to child `2`, giving `5 + (-3) = 2`.
6. Cutting `3` or `4` leaves node `1` with the other child, so best remains at most `11`.
7. Maximum over no cut and all cuts is `12`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One pass computes `down`, top-two child contributions, and prefix sums; a second pass computes `out` for every non-root node. Each edge and node is processed a constant number of times. At `10^6` nodes this is still linear and operationally feasible; at `10^9`, even linear traversal is too large for memory and wall-clock constraints.

### Space Complexity
`O(n)` for adjacency, parent/children structure, and DP arrays such as `down`, `pref`, `best1`, `best2`, and `out`. You can reduce some auxiliary arrays by fusing passes or storing less metadata, but not below linear without sacrificing clarity or requiring recomputation.

## 💡 Key Takeaways
- If a tree problem asks for the best result after removing one subtree, think “inside DP + outside DP” rather than recomputing from scratch per cut.
- If excluding one child changes the parent’s optimal choice, that is a strong signal to maintain best and second-best child contributions.
- The cut can create new leaves; forgetting that parent may become a leaf produces systematically wrong transitions.
- Use 64-bit arithmetic throughout: path sums can exceed 32-bit range even when individual node values fit in `int`.
- The production analogue is failure isolation in hierarchical systems: precompute local optima and sibling fallbacks so one bad branch can be removed without global recomputation.

## 🚀 Variations & Further Practice
- Allow cutting **up to `k` subtrees** instead of one. The twist is combinatorial interaction across siblings, which turns the problem into knapsack-style tree DP.
- Maximize the best path after cutting one subtree **and reconnecting one surviving subtree elsewhere**. The harder part is combining deletion DP with rerooting and reattachment state.
- Replace root-to-leaf sum with **maximum path anywhere in the remaining tree**. The twist is that deletion affects both downward and through-node path composition, requiring a richer DP state.