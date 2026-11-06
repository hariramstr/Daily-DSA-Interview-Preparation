# Maximum Rollback Gain in a Version Tree

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Tree DP, DFS

---

## 🗂 Problem Overview
Given a rooted tree of versions, each node has a signed score contribution. You may remove at most one non-root subtree, then evaluate the remaining tree by `total_remaining_score - minimum_root_to_leaf_path_sum`. The goal is to maximize that value.

The challenge is that one rollback changes two coupled quantities at once: the global total score and the set of surviving leaves. With up to `2e5` nodes, recomputing path minima for every candidate subtree is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere a hierarchical dependency graph supports a single cut or quarantine action: rollback in version-control DAG projections, disabling a service subtree in config inheritance, pruning a compiler IR region, dropping a bad branch in feature-flag trees, or isolating a corrupt shard in storage metadata. At scale, the hard part is not the cut itself; it is recomputing the best remaining global objective without rescanning the whole structure per candidate. The right tree-DP framing turns an operational “what if we remove this branch?” question from quadratic simulation into a linear-time decision primitive.

## 🔍 Problem Statement
You are given a rooted tree with `n` nodes, root `0`, described by `parent[]`, where every non-root node has exactly one parent. Each node `i` has an integer `score[i]` in `[-1e9, 1e9]`. You may perform **at most one** rollback on a **non-root** node `x`, removing the entire subtree rooted at `x`.

For the remaining tree, define:

- `remaining_sum` = sum of scores of all remaining nodes
- `min_path` = minimum sum over all root-to-leaf paths in the remaining tree

The objective is to maximize `remaining_sum - min_path`.

Important edge case: removing a subtree can make an internal node become a leaf, so the set of root-to-leaf paths must be recomputed under that modified structure.

Constraints:
- `1 <= n <= 200000`
- valid rooted tree
- answer may exceed 32-bit range

Examples:
- `parent = [-1,0,0,1,1,2]`, `score = [5,-2,4,-3,6,-1]` → `9`
- `parent = [-1,0,0,1,2,2,4]`, `score = [7,-5,3,10,-4,6,-8]` → `22`

The `2e5` bound rules out per-node rollback simulation.

## 🪜 How to Solve This
1. Start from the formula after removing subtree `x`:  
   `gain(x) = (total_sum - subtree_sum[x]) - min_path_after_removal(x)`.

2. The first term is easy if we precompute subtree sums with one DFS.

3. The hard part is `min_path_after_removal(x)`. A rollback does **not** just delete some leaf paths; it can create a new leaf at `parent[x]` if `x` was that parent’s last surviving child.

4. That suggests a bottom-up DP on “best root-to-leaf path inside this subtree,” but with enough information to answer:  
   “What is the minimum path from this node to a leaf if one specific child subtree is removed?”

5. For each node, the only local effect of deleting child `c` is:
   - choose the minimum among the other children, or
   - if no other children remain, the node itself becomes a leaf.

6. So for every node, keep the smallest and second-smallest child-derived downward path values. Then excluding one child becomes O(1).

7. Finally, propagate from the root downward a reroot-style DP that computes the minimum root-to-leaf path after deleting each node’s subtree.

That gives an O(n) solution: one pass for subtree sums, one pass for downward minima, one pass for exclusion-based propagation.

## 🧩 Algorithm Walkthrough
1. **Build the rooted adjacency list.**  
   Store children for each node from `parent[]`. This is a standard **tree DP / DFS** setup. The invariant is that every transition is parent → children only.

2. **Compute `subtree_sum[u]` with a postorder DFS.**  
   `subtree_sum[u] = score[u] + sum(subtree_sum[v])` over children `v`.  
   This gives the score removed by rolling back `u`, so `remaining_sum = total_sum - subtree_sum[u]`.

3. **Compute `down[u]`: minimum sum from `u` to any leaf in its current subtree.**  
   If `u` is a leaf, `down[u] = score[u]`. Otherwise  
   `down[u] = score[u] + min(down[child])`.  
   At the same time, for each node record the smallest and second-smallest child `down` values, plus which child achieved the minimum. This is the key local exclusion structure.

4. **Define `cutMin[u]`: minimum root-to-leaf path sum in the whole remaining tree after removing subtree `u``.**  
   We only need this for non-root nodes. For a child `v` of parent `p`, after deleting `v`, the path minimum in the remaining tree is the minimum of:
   - any unaffected path that avoids subtree `p` entirely
   - a path that goes through `p` and then uses another surviving child
   - a path ending at `p` itself if `v` was its only child

5. **Propagate an “outside minimum” top-down.**  
   Let `upMin[u]` mean the minimum root-to-leaf path sum in the tree after deleting subtree `u`, considering all effects above `u`. For child `v` of `p`, the best path through `p` after excluding `v` is:  
   `prefix_to_p + min(0 as leaf-at-p, best_other_child_down)`  
   where `prefix_to_p` is the root-to-`p` path sum excluding descendants, and `0 as leaf-at-p` means no extra suffix beyond `p`.  
   Using the parent’s first/second minima, `best_other_child_down` is O(1).

6. **Evaluate every rollback candidate.**  
   For each non-root node `u`:  
   `gain(u) = total_sum - subtree_sum[u] - cutMin[u]`.  
   Also compare against the no-rollback case:  
   `gain_none = total_sum - down[0]`.

7. **Return the maximum.**  
   Correctness follows from decomposing every surviving root-to-leaf path after a cut into either an unaffected path outside the parent chain or a path that reaches the cut’s parent and then chooses the best surviving local continuation.

## 📊 Worked Example
Use `parent = [-1,0,0,1,1,2]`, `score = [5,-2,4,-3,6,-1]`.

| Node | score | subtree_sum | down |
|---|---:|---:|---:|
| 3 | -3 | -3 | -3 |
| 4 | 6 | 6 | 6 |
| 5 | -1 | -1 | -1 |
| 1 | -2 | 1 | -5 |
| 2 | 4 | 3 | 3 |
| 0 | 5 | 9 | 0 |

- `total_sum = 9`
- No rollback: `gain = 9 - down[0] = 9 - 0 = 9`

Now test rollback at node `3`:
- `remaining_sum = 9 - (-3) = 12`
- Parent `1` loses child `3` but still has child `4`
- Remaining root-to-leaf paths:
  - `0 -> 1 -> 4 = 5 + (-2) + 6 = 9`
  - `0 -> 2 -> 5 = 5 + 4 + (-1) = 8`
- Minimum path is `8`? No: path through `1` to `4` is `9`, through `2` to `5` is `8`, so min is `8`
- Gain is `12 - 8 = 4`

This example also shows why careful recomputation matters: deleting a subtree changes both total score and the leaf set. The implementation must derive the new minimum path structurally, not by patching old leaf values.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each node is processed a constant number of times across three DFS-style passes: subtree sums, downward minimum-path DP, and top-down exclusion propagation. At `10^6` nodes this is still practical in a systems language; at `10^9`, the bottleneck is input size itself, not algorithmic overhead.

### Space Complexity
`O(n)` for the children adjacency list and DP arrays: subtree sums, downward minima, prefix/root path sums, and exclusion-state values. You can reduce some arrays by fusing passes, but that trades memory for tighter coupling and harder correctness reasoning.

## 💡 Key Takeaways
- If a tree problem asks for the best result after removing one subtree, expect a split into “inside removed part” vs “outside surviving structure,” which is classic tree DP with exclusion.
- If deleting one child can turn its parent into a leaf, plain subtree minima are insufficient; you need per-node first/second best child contributions.
- The main trap is forgetting that the minimum root-to-leaf path after a cut may end at a newly created leaf, not an original leaf.
- Another common bug is using 32-bit integers; both subtree sums and path sums can exceed that range under `2e5` nodes and `1e9` values.
- The transferable design insight is to precompute enough local alternative state that a global “remove one branch” what-if query becomes O(1) per candidate.

## 🚀 Variations & Further Practice
- Allow rollback of **up to `k` subtrees**. The twist is combinatorial interaction between cuts; local first/second minima are no longer enough, and the problem becomes a higher-dimensional tree DP.
- Replace the objective with `remaining_sum - median(root-to-leaf path sum)`. The hard part shifts from min-exclusion DP to maintaining order statistics under structural deletion.
- Generalize from trees to a **version DAG** with shared descendants. The conceptual jump is that subtree removal is no longer well-defined by a single parent chain, so reference counting or dominator-style analysis becomes necessary.