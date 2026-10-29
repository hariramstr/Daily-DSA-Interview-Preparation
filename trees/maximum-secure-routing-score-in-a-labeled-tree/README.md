# Maximum Secure Routing Score in a Labeled Tree

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Tree DP, DFS

---

## 🗂 Problem Overview
Given a tree with up to `2 * 10^5` nodes, each carrying an integer label, compute the maximum score over all simple paths. For a path, the score is `sum(path) - max(path) + min(path)`. Paths may be single nodes and may start or end anywhere. The challenge is that the objective is not decomposable by standard subtree sums or tree diameter logic: when two partial paths are merged, both the global minimum and maximum on the combined path must remain consistent.

## 🌍 Engineering Impact
This pattern shows up when a path aggregate is almost additive but includes a small number of non-linear corrections: routing with worst-hop penalties, ranking pipelines with top-feature suppression, fraud graphs with path-level risk caps, and distributed query planners where one extreme value dominates cost. At scale, naive path enumeration collapses immediately, and “just keep the best subtree score” is wrong because mergeability depends on retained boundary state. The useful engineering lesson is broader than this problem: if an objective depends on additive state plus a few path extrema, the system needs structured state compression, not brute-force traversal or local greedy decisions.

## 🔍 Problem Statement
You are given:

- `n` routers, numbered `0` to `n - 1`
- `labels[i]`, the security label of router `i`
- `n - 1` undirected edges forming a valid tree

A route is any simple path in the tree, including a single node. Its secure routing score is:

`sum(labels on path) - maximum label on path + minimum label on path`

Return the maximum score over all valid routes.

Constraints:

- `1 <= n <= 2 * 10^5`
- `1 <= labels[i] <= 10^9`
- `edges.length == n - 1`
- the graph is always a tree

Examples:

- `n = 5, labels = [4,2,7,1,3], edges = [[0,1],[1,2],[1,3],[3,4]]`
- `n = 4, labels = [8,5,6,2], edges = [[0,1],[1,2],[1,3]]`

The decisive constraint is `n = 2 * 10^5`: any all-pairs path enumeration or per-node quadratic merge is dead on arrival.

## 🪜 How to Solve This
1. Start from the score formula → `sum - max + min`.  
   The path sum is additive, but `max` and `min` are global path properties. That immediately rules out plain diameter DP.

2. Ask what information a partial path must expose to be mergeable.  
   If a path ends at some node, its future score contribution depends on:
   - total sum so far
   - current minimum
   - current maximum

3. Notice the obstacle → storing all `(sum, min, max)` states per subtree is too large.  
   We need a compressed representation that still supports combining paths across a parent.

4. Rewrite the objective mentally as “sum plus one bonus endpoint extreme and one penalty endpoint extreme.”  
   That suggests tracking best downward paths under fixed extreme roles rather than raw path scores.

5. Root the tree arbitrarily and use DFS-based tree DP.  
   For each node, compute best downward states for paths starting at that node, then combine child contributions through the node to form candidate full paths.

6. While merging children, maintain the best previously seen compatible states.  
   This is the standard “tree DP with cross-child aggregation” pattern: local state per child, global answer updated at the LCA-like merge point.

## 🧩 Algorithm Walkthrough
1. **Root the tree and run DFS**  
   Use any node as root, typically `0`. Every simple path has a highest node in this rooted view, so every answer can be considered exactly once when processed at that node. This avoids double counting and gives a clean merge point.

2. **Define downward DP states**  
   For each node `u`, maintain compressed best values for downward paths that start at `u` and go into exactly one descendant branch. The useful abstraction is **Tree DP with extremal-state compression**: instead of storing every `(sum, min, max)`, store the best transformed values needed to extend or merge paths while accounting for whether `labels[u]` becomes a new min or max.

3. **Process each child independently**  
   DFS returns the child’s downward states. Extend each returned state through `u` by adding `labels[u]` and updating min/max role transitions. This preserves the invariant: every stored state corresponds to some simple downward path starting at `u`.

4. **Update the global answer with one-branch paths**  
   Single-node paths and pure downward paths are valid routes. Evaluate them immediately so paths that never need a cross-child merge are included.

5. **Merge across children at `u`**  
   As child contributions are scanned, combine the current child’s states with the best compatible states from previously processed children. This is the critical correctness step: any path passing through `u` and using two branches is formed exactly here. The invariant is that the accumulator summarizes all earlier branches in a form sufficient for optimal pairing.

6. **Return aggregated states upward**  
   After all children are processed, return the best downward states rooted at `u`. The final answer is the maximum score seen anywhere during DFS.

## 📊 Worked Example
Take `labels = [8,5,6,2]`, edges `[[0,1],[1,2],[1,3]]`, rooted at `1`.

| Step | Node | Candidate path | Sum | Min | Max | Score |
|---|---:|---|---:|---:|---:|---:|
| 1 | 0 | `0` | 8 | 8 | 8 | 8 |
| 2 | 2 | `2` | 6 | 6 | 6 | 6 |
| 3 | 3 | `3` | 2 | 2 | 2 | 2 |
| 4 | 1 | `1` | 5 | 5 | 5 | 5 |
| 5 | 1 + child 0 | `1 -> 0` | 13 | 5 | 8 | 10 |
| 6 | 1 + child 2 | `1 -> 2` | 11 | 5 | 6 | 10 |
| 7 | merge 0 and 2 through 1 | `0 -> 1 -> 2` | 19 | 5 | 8 | 16 |
| 8 | merge 3 and 2 through 1 | `3 -> 1 -> 2` | 13 | 2 | 6 | 9 |

The important trace point is step 7: the optimal path is not contained in one subtree, so the answer only appears when two child-derived downward states are merged through node `1`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` if the per-node state remains constant-sized and each edge is processed once in DFS plus once during merge. That is the only viable regime for `2 * 10^5` nodes. At `10^6`, linear still works in native code; at `10^9`, even linear traversal becomes a system-level throughput problem.

### Space Complexity
`O(n)` for the adjacency list, recursion or explicit stack, and per-node DP state. The dominant owner is the tree representation itself. Recursion can be replaced with an iterative postorder traversal to avoid stack overflow, but asymptotic space does not change.

## 💡 Key Takeaways
- If a tree path objective is “additive term + a few path extrema,” expect tree DP with compressed path state, not diameter or subtree-sum templates.
- When the optimal path can start and end anywhere, look for the node where two downward paths must be merged; that node is the natural DP composition point.
- Single-node paths are valid and often dominate edge-heavy candidates when a large label would otherwise be subtracted as the path maximum.
- Cross-child merges must only pair states from different branches; reusing the same child twice is a subtle but common correctness bug.
- In production graph and routing systems, scalable solutions come from preserving only merge-relevant state, not from retaining full path histories.

## 🚀 Variations & Further Practice
- Replace the score with `sum - max + min - second_max`; now the state must preserve more than one extreme, which makes compression and merge logic significantly harder.
- Support online label updates with repeated max-score queries; this shifts the problem toward heavy-light decomposition or centroid-based dynamic maintenance.
- Generalize from trees to DAGs; path uniqueness disappears, so the merge structure changes from parent-child composition to topological DP with multiple predecessors.