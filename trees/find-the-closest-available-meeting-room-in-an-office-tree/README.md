# Find the Closest Available Meeting Room in an Office Tree

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, Dynamic Programming

---

## 🗂 Problem Overview
Given a rooted tree representing an office layout, preprocess it so each query from node `u` returns the nearest node marked `available`. Distance is the number of edges, and ties are broken by smaller node index. If no available node exists anywhere, return `-1`. The challenge is scale: both `n` and the number of queries can reach `2 * 10^5`, so per-query traversal is too expensive. The right solution computes the best answer for every node once, then answers queries in O(1).

## 🌍 Engineering Impact
This pattern shows up anywhere a static hierarchy serves many nearest-resource lookups: nearest healthy replica in a topology tree, nearest cache shard with warm data, closest escalation owner in an org graph constrained to a tree, or nearest service endpoint in a region/zone hierarchy. At small scale, repeated BFS/DFS per request is acceptable; at `10^5+` nodes and queries, it becomes latency amplification and wasted CPU. Precomputing nearest candidates via rerooting-style DP turns repeated graph search into indexed lookup. Architecturally, this is the difference between request-path computation and topology-aware preprocessing, which is often what keeps control planes and schedulers predictable under load.

## 🔍 Problem Statement
You are given a tree with `n` nodes labeled `0..n-1`, rooted at `0`. Each node has a boolean `available[i]` indicating whether it is an available meeting room. For each query node `u`, return the available node reachable with minimum edge distance. If multiple available nodes are at the same minimum distance, return the smallest node index. If no available node exists in the tree, return `-1`.

Constraints:
- `1 <= n <= 2 * 10^5`
- `edges.length == n - 1`
- `available.length == n`
- `1 <= queries.length <= 2 * 10^5`
- The graph is guaranteed to be a tree

Examples:

- `n = 7`, `edges = [[0,1],[0,2],[1,3],[1,4],[2,5],[2,6]]`
- `available = [false,false,true,false,true,false,false]`
- `queries = [3,5,0]`
- Output: `[4,2,2]`

- `n = 5`, `edges = [[0,1],[1,2],[1,3],[3,4]]`
- `available = [false,false,false,false,false]`
- `queries = [0,2,4]`
- Output: `[-1,-1,-1]`

The decisive constraint is that many queries run against the same tree, so `O(n * q)` is not viable.

## 🪜 How to Solve This
1. Read the problem → notice queries are independent, but the tree and `available` array are fixed. That strongly suggests preprocessing.
2. A naive BFS from every query node finds the nearest available room, but `q` can be `2 * 10^5`. In the worst case that becomes `O(nq)`, which is dead on arrival.
3. Reframe the question: instead of answering each query separately, compute the nearest available room for **every node** once.
4. In a tree, the nearest answer for a node can come from two directions:
   - inside its own subtree
   - outside its subtree, through its parent or siblings
5. That split is the signature of **tree DP with rerooting**. First pass pushes information upward from children. Second pass pushes information downward from parent/sibling context.
6. For each node, maintain the best candidate as `(distance, nodeIndex)`, compared lexicographically so tie-breaking is automatic.
7. After both passes, every node has its globally best available room. Each query is then just `answer[u]`.

This is the standard “local subtree DP + reroot global context” pattern for all-nodes nearest-resource problems on trees.

## 🧩 Algorithm Walkthrough
1. **Build the adjacency list.**  
   Represent the tree as `graph[u] -> neighbors`. This gives linear traversal cost and is the standard foundation for tree DP.

2. **Define a candidate type.**  
   Store each best answer as `(dist, node)`, where smaller `dist` wins, then smaller `node` breaks ties. Use an invalid sentinel like `(INF, -1)` for “no available room seen.” This invariant makes all merges uniform.

3. **First DFS: compute `down[u]`.**  
   `down[u]` is the best available room reachable from `u` using only nodes in `u`’s subtree.  
   - Start with `(0, u)` if `available[u]` is true.
   - For each child `v`, if `down[v]` is valid, candidate from that child becomes `(down[v].dist + 1, down[v].node)`.
   - Keep the lexicographically smallest candidate.  
   This is correct because every subtree path from `u` must either stop at `u` or enter exactly one child subtree.

4. **Prepare sibling exclusion efficiently.**  
   In the reroot pass, a child needs the best candidate from its parent side excluding itself. For a node’s children, compute prefix/suffix best arrays over child-contributed candidates. This maintains O(n) total work instead of scanning siblings repeatedly.

5. **Second DFS: compute `up[u]`.**  
   `up[u]` is the best available room reachable from `u` without entering `u`’s own subtree. For child `v`, combine:
   - parent itself if `available[u]`
   - `up[u]` shifted by one edge
   - best sibling-subtree candidate shifted through `u`  
   Then shift once more when assigning to `up[v]`. This is the rerooting step: each child receives the best “outside” context.

6. **Finalize `ans[u] = min(down[u], up[u])`.**  
   By construction, every path from `u` either stays in its subtree or leaves through its parent. Those cases are disjoint and exhaustive, so the minimum of the two is globally correct.

7. **Answer queries in O(1).**  
   Return `ans[queries[i]].node`, or `-1` if the candidate is invalid.  
   The pattern here is **Rerooting Dynamic Programming on Trees**, the right abstraction because each node’s global answer decomposes cleanly into subtree-local and outside-subtree contributions.

## 📊 Worked Example
Use Example 1:

- Tree: `0-[1,2]`, `1-[3,4]`, `2-[5,6]`
- Available rooms: `2`, `4`

### Down pass
| Node | `down[node]` | Reason |
|---|---|---|
| 3 | invalid | not available, no children |
| 4 | `(0,4)` | available |
| 1 | `(1,4)` | best child is 4 |
| 5 | invalid | not available |
| 6 | invalid | not available |
| 2 | `(0,2)` | available beats descendants |
| 0 | `(1,2)` | min of child paths via 1 and 2 |

### Up/final reasoning
1. `up[0] = invalid`
2. For node `1`, best outside comes from root through child `2` → room `2` at distance `2`, so `up[1] = (2,2)`
3. For node `3`, best outside comes through `1` to room `4` → `up[3] = (2,4)`
4. Final answers:
   - `ans[3] = min(invalid, (2,4)) = 4`
   - `ans[5] = min(invalid, (1,2)) = 2`
   - `ans[0] = min((1,2), invalid) = 2`

Queries `[3,5,0]` return `[4,2,2]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + q)`. Building the adjacency list is `O(n)`, the first DFS is `O(n)`, and the rerooting pass with prefix/suffix aggregation is also `O(n)`. Each query is then `O(1)`. At `10^6` scale this remains practical; at `10^9`, even linear preprocessing becomes infrastructure-sized and no in-memory tree traversal is realistic.

### Space Complexity
`O(n)`. The adjacency list, parent/depth metadata, and DP arrays (`down`, `up`, `ans`, plus temporary prefix/suffix storage across child lists) dominate memory. You can reduce some temporary allocations with iterative reuse, but only by increasing implementation complexity and reducing clarity.

## 💡 Key Takeaways
- If the tree is static and queries are numerous, compute answers for all nodes once; repeated per-query traversal is the wrong cost model.
- “Best answer may come from subtree or from outside subtree” is the classic signal for rerooting DP on trees.
- Tie-breaking by node index should be encoded directly into candidate comparison; don’t bolt it on after distance computation.
- Be careful when propagating parent/sibling answers: distances must be incremented exactly once per traversed edge, and double-shifting is a common bug.
- In production systems, topology-aware preprocessing often converts variable request-path graph work into deterministic lookup, which is usually the more scalable architecture.

## 🚀 Variations & Further Practice
- Support online updates to `available[i]` between queries. The conceptual twist is moving from static rerooting DP to dynamic tree data structures such as centroid decomposition or heavy-light decomposition with segment trees.
- Return the `k` nearest available rooms instead of one. The harder part is merging and propagating multiple ordered candidates while preserving tie-breaking and linearithmic bounds.
- Generalize from unweighted edges to weighted walking times. The rerooting idea still applies, but distance propagation and candidate comparison now operate on weighted path costs rather than edge counts.