# Minimum Relabels to Make Root Paths Increasing

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Dynamic Programming, DFS

---

## 🗂 Problem Overview
Given a rooted tree and an integer label on every node, change the fewest nodes so that every root-to-node path becomes strictly increasing. A relabel can assign any integer, so the optimization target is equivalent to keeping as many original labels unchanged as possible. The challenge is global consistency: a label kept at one node constrains every descendant and ancestor on its path, so local edge fixes are not sufficient. With `n` up to `2 * 10^5`, quadratic ancestor checks are not viable.

## 🌍 Engineering Impact
This pattern shows up whenever local metadata must remain globally monotone along a hierarchy or dependency chain. Examples include compiler symbol/version assignment across nested scopes, search-ranking feature thresholds propagated through category trees, streaming pipeline stage watermarks, and distributed policy inheritance where child constraints must dominate parent constraints. At small scale, ad hoc recursive fixes seem fine; at production scale, they fail because each local decision affects an entire subtree. The useful abstraction is not “repair invalid edges” but “maximize a compatible subsequence under ancestor constraints,” which leads to scalable DFS-order plus indexed DP rather than repeated tree rewrites.

## 🔍 Problem Statement
You are given a connected tree with `n` nodes, numbered `0..n-1`, rooted at `0`. Each node `i` has label `value[i]`. You may relabel any node to any integer; each changed node costs exactly one operation regardless of the replacement value.

The final tree must satisfy: for every node, labels on the path from root to that node are strictly increasing. Equivalently, for every parent-child edge, `label[child] > label[parent]`, and this must hold consistently across all root paths.

Return the minimum number of relabel operations.

Constraints:
- `1 <= n <= 2 * 10^5`
- `edges.length == n - 1`
- `-10^9 <= value[i] <= 10^9`

Examples:

- `n = 5, edges = [[0,1],[0,2],[1,3],[1,4]], value = [3,4,2,5,6]` → `1`
- `n = 6, edges = [[0,1],[1,2],[2,3],[1,4],[4,5]], value = [8,3,4,10,9,11]` → `2`

The decisive constraint is tree size: any solution that scans ancestors per node degenerates to `O(n^2)` on a chain.

## 🪜 How to Solve This
1. Start from the real objective → minimizing relabels means maximizing nodes whose original values can stay.

2. Ask when a set of kept nodes is feasible → along every ancestor-descendant chain, kept values must already be strictly increasing. Nodes on different branches do not constrain each other.

3. That immediately sounds like a longest-increasing-subsequence variant, but on a tree rather than an array. The relevant sequence for each node is its root path.

4. Reframe the problem as DP on the tree: for each node, compute the best number of kept labels in its subtree assuming this node is kept as the last fixed value on the current path.

5. To make that efficient, process nodes in DFS order and reduce value comparisons with coordinate compression. Then maintain a data structure keyed by label rank that can answer: “among ancestors on the current DFS path with smaller value, what is the best keep-count so far?”

6. This becomes a path-scoped longest-chain DP: query best predecessor, extend by one if keeping the node, recurse, then roll back path state when leaving the subtree.

7. Final answer = `n - max_kept`.

## 🧩 Algorithm Walkthrough
1. **Root the tree and build adjacency.**  
   Run an iterative DFS/BFS from node `0` to establish parent/children relationships. This removes ambiguity from undirected edges and gives a traversal order safe for `2 * 10^5` nodes.

2. **Coordinate-compress label values.**  
   Sort unique `value[i]` and map each label to rank `1..m`. Only relative order matters for strict increase, so compression preserves correctness while enabling indexed queries.

3. **Use a path-scoped DP with a Fenwick tree / segment tree.**  
   Pattern: **DFS + Longest Increasing Chain DP over ancestor constraints**.  
   For the current root-to-node path, maintain at each compressed rank the maximum number of kept nodes achievable by ending at that rank.

4. **At node `u`, query best predecessor.**  
   Compute `bestLess = query(rank(value[u]) - 1)`: the maximum keep-count among ancestors with strictly smaller label. If `u` is kept unchanged, its chain length is `dpKeep[u] = bestLess + 1`.

5. **Temporarily apply this node to the path structure.**  
   Update rank `r` with `dpKeep[u]` before exploring children. Invariant: the data structure always represents exactly the active DFS path, not arbitrary previously visited nodes.

6. **Recurse into children and track subtree optimum.**  
   Children can either keep their own value if it extends some valid ancestor chain, or be relabeled. Since relabeled values are unconstrained except for preserving strictness, only unchanged nodes need explicit DP accounting.

7. **Rollback on DFS exit.**  
   Because siblings must not see each other as ancestors, restore the previous value at rank `r` after finishing `u`’s subtree. This rollback is the critical correctness condition.

8. **Compute the result.**  
   Let `maxKept` be the largest valid chain-compatible count accumulated across the tree structure induced by ancestor relations. The minimum relabels is `n - maxKeptSubtreeCompatible`, where subtree DP aggregates kept counts across branches because incomparable branches can keep labels independently.

## 📊 Worked Example
Example: `edges = [[0,1],[0,2],[1,3],[1,4]]`, `value = [3,4,2,5,6]`

Compressed ranks: `2->1, 3->2, 4->3, 5->4, 6->5`

| Step | Node | Path | Rank | Best `< rank` | Keep count if unchanged |
|---|---:|---|---:|---:|---:|
| 1 | 0 | `0` | 2 | 0 | 1 |
| 2 | 1 | `0→1` | 3 | 1 | 2 |
| 3 | 3 | `0→1→3` | 4 | 2 | 3 |
| 4 | backtrack |  |  |  |  |
| 5 | 4 | `0→1→4` | 5 | 2 | 3 |
| 6 | backtrack to 0 |  |  |  |  |
| 7 | 2 | `0→2` | 1 | 0 | 1 |

Nodes `0,1,3,4` can all remain unchanged simultaneously. Node `2` cannot keep value `2` because its parent path already fixes `3` at the root. So `maxKept = 4`, and answer is `5 - 4 = 1`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` after coordinate compression. Each node participates in one DFS entry, one range-maximum query, one point update, and one rollback update on a Fenwick tree or segment tree. At `10^6` scale this is still practical in optimized languages; `O(n^2)` is not. At `10^9`, no in-memory exact tree algorithm is realistic.

### Space Complexity
`O(n)` for adjacency, parent/DFS state, compressed values, and the indexed tree over unique ranks. This is already near-minimal for exact processing of an arbitrary tree. You can reduce recursion overhead with an iterative DFS, trading simpler code for explicit stack management.

## 💡 Key Takeaways
- If the requirement is “strictly increasing along every root path,” think ancestor-chain compatibility, not local parent-child patching.
- When labels can be changed arbitrarily and cost is per node changed, the problem usually flips into “maximize unchanged items under a partial order.”
- Strict inequality means queries must use ranks `< current`, never `<=`; getting this wrong silently overcounts.
- DFS path state must be rolled back on exit; otherwise siblings incorrectly influence each other as if they were ancestors.
- The transferable design insight is to separate structural constraints from value-domain freedom: once relabeled values are unconstrained, only the preserved assignments need heavy optimization.

## 🚀 Variations & Further Practice
- **Non-decreasing root paths instead of strictly increasing:** same framework, but predecessor queries become `<= current`; the subtlety is handling duplicate ranks without double-counting path state.
- **Weighted relabel cost per node:** maximize total saved weight rather than count, turning the DP from longest chain to maximum-weight compatible subset on tree paths.
- **Dynamic updates to labels with repeated queries:** requires persistent or heavy-light-based data structures, because recomputing path-compatible DP from scratch per update is too expensive.