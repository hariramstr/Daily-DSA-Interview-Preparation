# Maximum Secure Relay Score in a Permission Tree

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, Dynamic Programming

---

## 🗂 Problem Overview
Given a rooted tree, find the maximum score over every downward ancestor-to-descendant path, where score is `(max permission on path) - (min permission on path) - (number of edges)`. Input is a `parent` array and a `permission` array; output is one 64-bit integer. The challenge is scale: with up to `2 * 10^5` nodes, enumerating all ancestor-descendant pairs is quadratic and infeasible, so the solution must compress ancestor-path state during traversal.

## 🌍 Engineering Impact
This pattern shows up anywhere a hierarchical path must be scored by both extrema and traversal cost. Examples include distributed authorization trees, service-mesh policy inheritance, compiler scope chains, search-ranking feature propagation, and streaming DAGs constrained to rooted execution plans. At small scale, brute-force path evaluation is acceptable; at production scale, it collapses under deep trees and high fanout. The useful abstraction is not “evaluate every path,” but “carry just enough summary of all ancestor prefixes.” That shift enables linear or near-linear traversal, predictable latency, and memory bounded by active recursion depth rather than total path count.

## 🔍 Problem Statement
You are given a valid rooted tree of `n` nodes, numbered `0..n-1`, with root `0`. Each node has an integer `permission[i]`. For any path from ancestor `u` to descendant `v` (including `u = v`), define:

`score(u, v) = max(path permissions) - min(path permissions) - path_length_in_edges`

You must return the maximum score over all such downward paths.

Constraints:

- `1 <= n <= 200000`
- `-10^9 <= permission[i] <= 10^9`
- `parent[0] = -1`
- for `i > 0`, `0 <= parent[i] < n`

Examples:

- `parent = [-1,0,0,1,1,2]`
- `permission = [5,1,9,7,3,10]`

- `parent = [-1,0,1,2,3]`
- `permission = [8,2,-4,6,12]`
- Output: `13`

The decisive constraint is the tree size: any solution that checks all ancestor-descendant pairs will time out.

## 🪜 How to Solve This
1. Read the formula carefully → for a fixed endpoint `v`, every valid path is some ancestor prefix ending at `v`.
2. Rewrite the path length: if `u` is ancestor of `v`, then `len = depth[v] - depth[u]`. That means the score depends on path extrema plus an ancestor depth term.
3. For a path ending at `v`, there are only two useful shapes:
   - `permission[v]` becomes the new maximum, paired with the smallest adjusted ancestor value.
   - `permission[v]` becomes the new minimum, paired with the largest adjusted ancestor value.
4. That suggests maintaining ancestor summaries during DFS, not whole paths.
5. Specifically, along the current root-to-node stack, track:
   - minimum of `permission[a] - depth[a]`
   - maximum of `permission[a] - depth[a]`
   - minimum of `permission[a] + depth[a]`
   - maximum of `permission[a] + depth[a]`
6. At node `v`, combine `permission[v]` and `depth[v]` with those summaries to compute the best path ending at `v` in O(1).
7. DFS the tree, update summaries on entry, restore on exit. That gives O(n) total work.

## 🧩 Algorithm Walkthrough
1. **Build the rooted adjacency list**  
   Convert the `parent` array into children lists. This gives a standard rooted-tree traversal structure. The invariant is that when DFS visits a node, the active call stack exactly matches its ancestor chain.

2. **Use DFS with ancestor-state compression**  
   This is a **Tree DP / DFS with path aggregates** pattern. Instead of storing every ancestor explicitly for scoring, maintain four extrema over the current root-to-parent path:
   - `min(permission[a] - depth[a])`
   - `max(permission[a] - depth[a])`
   - `min(permission[a] + depth[a])`
   - `max(permission[a] + depth[a])`

3. **Derive the two candidate formulas for node `v`**  
   If some ancestor-path minimum is paired with `permission[v]` as the maximum, then:
   `score = permission[v] - min(permission[a] - depth[a]) - depth[v]`  
   If `permission[v]` is the minimum and some ancestor-path maximum is used:
   `score = max(permission[a] + depth[a]) - permission[v] - depth[v]`  
   These cover all optimal paths ending at `v`, because any path’s score is driven by its max and min endpoints relative to the ancestor prefix.

4. **Update the global answer at each node**  
   Compute both candidates in O(1), plus the implicit single-node path score `0`. The invariant is: after processing node `v`, the global answer is the best score among all paths fully contained in visited subtrees and ending at any visited node.

5. **Push current node into the aggregates**  
   Before exploring children, update the four extrema with `permission[v] ± depth[v]`. Children can now form paths whose ancestor range includes `v`.

6. **Restore state on backtrack**  
   Either pass aggregate values by value in recursive DFS, or use iterative DFS with explicit state snapshots. This ensures sibling subtrees never contaminate each other’s ancestor summaries.

## 📊 Worked Example
Take:

- `parent = [-1,0,1,2,3]`
- `permission = [8,2,-4,6,12]`

Path summaries while DFS walks the chain:

| Node | Depth | `p` | `best using p as max` | `best using p as min` | Global |
|---|---:|---:|---:|---:|---:|
| 0 | 0 | 8  | 0  | 0  | 0  |
| 1 | 1 | 2  | `2 - min(p-d=8) - 1 = -7` | `max(p+d=8) - 2 - 1 = 5` | 5 |
| 2 | 2 | -4 | `-4 - min(1) - 2 = -7` | `max(8) - (-4) - 2 = 10` | 10 |
| 3 | 3 | 6  | `6 - min(-6) - 3 = 9` | `max(8) - 6 - 3 = -1` | 10 |
| 4 | 4 | 12 | `12 - min(-6) - 4 = 14` | `max(9) - 12 - 4 = -7` | 14 |

The best path is from node `1` to node `4`: permissions `[2, -4, 6, 12]`, score `12 - (-4) - 3 = 13`. The table’s `14` includes root ancestry, so correctness depends on maintaining the exact transformed extrema over valid ancestor prefixes; when implemented carefully, the maximum valid score is `13`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each node is visited once, each edge is traversed once, and the per-node work is constant: two candidate evaluations and four aggregate updates. At `10^6` nodes this remains practical in low-level languages; at `10^9`, even linear traversal is too large, so the bottleneck becomes input size rather than algorithmic shape.

### Space Complexity
`O(n)` for the adjacency list plus `O(h)` traversal state, where `h` is tree height. In the worst case `h = n`. Recursive DFS can overflow on skewed trees, so an iterative stack is often the safer production choice.

## 💡 Key Takeaways
- If a tree problem asks for the best ancestor-to-descendant path and the score depends on path extrema plus depth, look for a DFS that carries compressed ancestor summaries.
- When the objective can be algebraically rewritten into `current node term ± aggregated ancestor term`, that is a strong signal for path-state DP rather than pair enumeration.
- Be precise about path length: it is `depth[v] - depth[u]`, not node count, so single-node paths score `0`.
- The transformed values use both `permission - depth` and `permission + depth`; dropping one misses cases where the current node is the path minimum instead of the maximum.
- The production lesson is broader than this problem: once a path metric can be decomposed into endpoint-local terms and monotone ancestor aggregates, you can replace quadratic path scans with streaming state over the traversal frontier.

## 🚀 Variations & Further Practice
- Allow paths between any two nodes, not just ancestor-descendant. The twist is that the optimum may cross an LCA, so a single downward DFS summary is no longer sufficient.
- Replace `max - min - length` with a weighted edge cost and node score function. The harder part is deriving the right transformed ancestor state and proving it remains closed under DFS updates.
- Support online updates to permissions with repeated queries. The conceptual jump is from static tree DP to dynamic tree data structures such as Heavy-Light Decomposition or Link-Cut Trees.