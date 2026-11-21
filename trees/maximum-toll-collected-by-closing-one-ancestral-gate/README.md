# Maximum Toll Collected by Closing One Ancestral Gate

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Tree DP, DFS

---

## 🗂 Problem Overview
Given a rooted tree, each root-to-leaf path contributes a revenue equal to the sum of tolls on that path. You must close exactly one non-root node, which deletes its entire subtree and removes every route passing through it. Return the maximum total revenue of the surviving root-to-leaf routes. The challenge is that `n` is up to `2 * 10^5`, so recomputing affected path sums independently for each candidate closure is far too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere a hierarchical structure aggregates downstream value and one cut disables an entire branch: shutting off a shard in a distributed serving tree, disabling a compiler module and all dependents, draining a subtree in a workflow DAG constrained to a tree, or removing a category branch in search/navigation systems. At scale, naive recomputation per candidate becomes quadratic and collapses under large fan-out. Tree DP matters because it turns “impact of removing this branch” into a reusable local metric, enabling fast what-if analysis, failure simulation, and policy optimization over large hierarchies.

## 🔍 Problem Statement
You are given `parent` and `toll` for a rooted tree of `n` nodes, with root `0`. A tourist route is any simple path from node `0` to a leaf. The revenue of one route is the sum of tolls along that path. Let `S` be the sum of revenues of all root-to-leaf routes.

You must close exactly one checkpoint `x != 0`. Closing `x` removes the entire subtree rooted at `x`, so every root-to-leaf route passing through `x` disappears completely. The remaining revenue is therefore `S - removed(x)`, where `removed(x)` is the total revenue of all routes that include `x`.

Return the maximum possible remaining revenue using 64-bit arithmetic.

Constraints:
- `2 <= n <= 2 * 10^5`
- `0 <= toll[i] <= 10^9`
- `parent[0] = -1`, and for `i > 0`, `0 <= parent[i] < n`

Examples:
- `parent = [-1,0,0,1,1,2]`, `toll = [5,2,4,3,1,6]` → `25`
- `parent = [-1,0,0,1,3,3,2]`, `toll = [1,10,2,5,4,6,8]` → `42`

The key constraint is `n = 2e5`: any per-node recomputation over subtrees or paths is too slow.

## 🪜 How to Solve This
1. Read the objective carefully → we are not maximizing what gets removed; we are maximizing what remains, which is equivalent to minimizing the revenue carried by routes through the closed node.

2. Ask what “closing node `x`” really deletes → every root-to-leaf path that enters `x`. So for each node, we need the total contribution of all such paths.

3. Break that deleted value into two parts:
   - tolls from ancestors to `x`, repeated once per leaf under `x`
   - tolls inside `x`’s subtree, counted across leaf routes below it

4. That suggests two reusable quantities:
   - `leafCount[x]`: number of leaves in `x`’s subtree
   - `subtreeRouteSum[x]`: total revenue of all `x`-to-leaf suffixes, measured within the subtree

5. Also track `prefixSum[x]`: root-to-`x` path sum. Then:
   - ancestor contribution = `(prefixSum[x] - toll[x]) * leafCount[x]`
   - subtree contribution = `subtreeRouteSum[x]`

6. So `removed(x) = (prefixSum[x] - toll[x]) * leafCount[x] + subtreeRouteSum[x]`

7. Compute everything with DFS/tree DP in linear time, then scan all non-root nodes for the minimum removed value.

Once you see “remove one subtree, many paths disappear,” this becomes a classic tree-aggregation problem.

## 🧩 Algorithm Walkthrough
1. **Build the rooted adjacency list** from `parent`.  
   This gives children for each node, which is the natural representation for subtree DP. The invariant is simple: every node’s subtree is exactly the node plus all descendants reachable through its child list.

2. **Run a DFS to compute `prefixSum[u]`** = sum of tolls on the path from root to `u`.  
   This is a forward accumulation. It is correct because every node has exactly one parent, so each prefix sum is uniquely determined. Invariant: when visiting `u`, `prefixSum[u]` is final.

3. **Run a postorder DFS for Tree DP** to compute:
   - `leafCount[u]`: number of leaves in `u`’s subtree
   - `subtreeRouteSum[u]`: total revenue of all `u`-to-leaf paths, counting tolls from `u` downward  
   Base case: if `u` is a leaf, then `leafCount[u] = 1` and `subtreeRouteSum[u] = toll[u]`.  
   Transition for internal node:
   - `leafCount[u] = sum(leafCount[v])`
   - `subtreeRouteSum[u] = sum(subtreeRouteSum[v] + toll[u] * leafCount[v])` over children `v`  
   This works because every `u`-to-leaf path is formed by prepending `u` to exactly one child suffix path.

4. **Compute removed revenue for each non-root node** using the Tree DP identity:  
   `removed[u] = (prefixSum[u] - toll[u]) * leafCount[u] + subtreeRouteSum[u]`  
   The first term counts ancestor tolls repeated across every leaf route below `u`; the second term counts all tolls from `u` downward across those same routes.

5. **Compute original total revenue** as `subtreeRouteSum[0]`.  
   Since root-to-leaf paths are exactly `0`-to-leaf suffixes, this value is the total original revenue `S`.

6. **Answer = `S - min(removed[u])` for `u != 0`**, or equivalently maximize `S - removed[u]`.  
   This is the right abstraction because the problem is a **Tree DP + DFS aggregation** problem: each node needs a subtree summary and one path-prefix summary, both composable in linear time.

## 📊 Worked Example
Use `parent = [-1,0,0,1,1,2]`, `toll = [5,2,4,3,1,6]`.

Tree: `0 -> {1,2}`, `1 -> {3,4}`, `2 -> {5}`

| Node | prefixSum | leafCount | subtreeRouteSum | removed |
|---|---:|---:|---:|---:|
| 3 | 10 | 1 | 3  | 10 |
| 4 | 8  | 1 | 1  | 8  |
| 5 | 15 | 1 | 6  | 15 |
| 1 | 7  | 2 | 8  | 18 |
| 2 | 9  | 1 | 10 | 15 |
| 0 | 5  | 3 | 33 | —  |

Trace:
1. Leaves first: each leaf contributes one route, so `leafCount = 1`, `subtreeRouteSum = toll`.
2. Node `1`: two leaves below it. `leafCount[1] = 2`, `subtreeRouteSum[1] = (3 + 2*1) + (1 + 2*1) = 8`.
3. Node `2`: one leaf below it. `subtreeRouteSum[2] = 6 + 4*1 = 10`.
4. Root total `S = 33`.
5. Best closure minimizes `removed`: node `4` removes only `8`, so remaining revenue is `33 - 8 = 25`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Building the child lists is linear, and each DFS visits every node and edge once. There is no per-candidate recomputation, which is the entire point: at `10^6` nodes, linear remains practical; at `10^9`, even linear is too large, but anything superlinear is already dead far earlier.

### Space Complexity
`O(n)` for the adjacency list plus arrays for `prefixSum`, `leafCount`, and `subtreeRouteSum`. This is already asymptotically optimal for explicit-tree input. You can reduce recursion overhead with an iterative traversal, but not the core storage without recomputing state.

## 💡 Key Takeaways
- If removing one node deletes an entire descendant region and affects many root-to-leaf paths at once, think subtree aggregation, not per-node simulation.
- When a path-based tree problem asks for totals over all leaves, `leafCount` is often the multiplicity term that converts local node values into global contribution.
- The root cannot be closed; forgetting that can produce an invalid minimum and a wrong answer on small tests.
- Use 64-bit integers everywhere: `toll[i]` can be `1e9`, and path contributions are multiplied by leaf counts across large subtrees.
- The transferable design insight is to precompute stable hierarchical summaries once, then answer destructive what-if queries by subtraction instead of rebuilding global state.

## 🚀 Variations & Further Practice
- Close **up to `k` subtrees** instead of exactly one. The twist is overlapping ancestry constraints and selecting cuts without double-counting removed routes, which pushes the problem toward knapsack-style tree DP.
- Support **online updates** to tolls or repeated closure queries. The twist is moving from static DFS aggregation to Euler tour indexing plus Fenwick/segment trees or heavy-light decomposition.
- Generalize from trees to a **DAG of routes**. The twist is that suffix counts and contributions are no longer unique per node, so simple subtree DP breaks and requires topological DP with shared downstream state.