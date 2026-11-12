# Find the Quietest Ancestor Department

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, Dynamic Programming

---

## 🗂 Problem Overview
Given a rooted tree of `n` departments, each with a noise score, compute for every node the ancestor on its root-to-node path with the minimum noise. The answer must include the node itself, and ties must prefer the earliest occurrence on the path, i.e. the ancestor closest to the root. The non-trivial part is scale: with up to `100000` nodes, scanning all ancestors per node degrades to quadratic time and is not acceptable.

## 🌍 Engineering Impact
This pattern shows up anywhere hierarchical inheritance meets path-local optimization. Examples include compiler symbol resolution with shadowing, policy inheritance in IAM trees, configuration propagation in service meshes, and ranking or throttling decisions in streaming pipelines organized by tenant or namespace. At small scale, recomputing state from scratch per node is tolerable; at organizational or infrastructure scale, it becomes an avoidable `O(n^2)` tax. Carrying forward the best state along a traversal turns a repeated ancestor query into a linear pass, which is exactly the difference between a design that survives deep hierarchies and one that stalls under load.

## 🔍 Problem Statement
You are given:

- `n`: number of departments
- `parent`: length-`n` array describing a rooted tree, where `parent[0] = -1`
- `noise`: length-`n` array where `noise[i]` is the department's noise score

For each department `i`, return the index of the quietest department on the path from root `0` to `i`, inclusive. If multiple departments on that path share the same minimum noise, return the one closest to the root.

Constraints:

- `1 <= n <= 100000`
- `0 <= noise[i] <= 1000000000`
- `parent` forms a valid rooted tree

Examples:

- `n = 7`, `parent = [-1,0,0,1,1,2,2]`, `noise = [5,3,8,4,2,8,1]` → `[0,1,0,1,4,0,6]`
- `n = 5`, `parent = [-1,0,1,2,3]`, `noise = [4,4,6,2,2]` → `[0,0,0,3,3]`

The key constraint is `n = 100000`: per-node ancestor scans are too slow.

## 🪜 How to Solve This
1. Read the requirement carefully → every answer depends only on the path from the root to that node, not on siblings or the full subtree.

2. That immediately suggests a tree traversal from the root downward. When visiting a node, the only information you need from above is: “what is the quietest ancestor seen so far?”

3. If you already know the best ancestor for a node’s parent, then the best ancestor for the node is just a comparison between:
   - the parent’s best candidate, and
   - the node itself.

4. Tie-breaking matters: if the current node has the same noise as the best-so-far, you must keep the earlier ancestor, not replace it. So update only when the current node is strictly quieter.

5. Build children lists from the `parent` array, then run DFS or iterative DFS/BFS from root `0`, propagating the best candidate downward.

6. This is tree DP in its simplest form: each node’s result is derived from its parent’s result in `O(1)`, so the whole tree is solved in one pass.

## 🧩 Algorithm Walkthrough
1. **Build the rooted adjacency list.**  
   Convert `parent` into `children`, where each node stores its direct descendants. This gives efficient top-down traversal from root `0`. The invariant is that every edge is represented once, from parent to child.

2. **Initialize the root.**  
   Set `answer[0] = 0`, because the root’s path contains only itself. This establishes the base case for the dynamic program.

3. **Traverse the tree top-down.**  
   Use **DFS on a tree with propagated state**. For each child `v` of node `u`, start with `best = answer[u]`, meaning the quietest ancestor known on the path to `u`.

4. **Compare the child against the inherited best.**  
   If `noise[v] < noise[best]`, then `v` becomes the new quietest ancestor for its own path, so set `answer[v] = v`. Otherwise set `answer[v] = best`.  
   This is correct because the path to `v` is exactly the path to `u` plus `v`, so no other candidates exist.

5. **Preserve tie-breaking by doing nothing on equality.**  
   If `noise[v] == noise[best]`, keep `best`. That preserves the earlier node on the path, which is closer to the root as required.

6. **Continue until all nodes are processed.**  
   Each node is visited once, each edge is traversed once, and the invariant holds throughout: `answer[x]` is always the earliest minimum-noise node on the root-to-`x` path.

This is the right abstraction because the problem is not “query arbitrary ancestors repeatedly”; it is “propagate optimal path state through a rooted tree.”

## 📊 Worked Example
Use `n = 7`, `parent = [-1,0,0,1,1,2,2]`, `noise = [5,3,8,4,2,8,1]`.

| Node | Parent | Inherited best | Compare with node | answer[node] |
|---|---:|---:|---|---:|
| 0 | -1 | - | root initializes itself | 0 |
| 1 | 0 | 0 (noise 5) | `3 < 5` → replace | 1 |
| 2 | 0 | 0 (noise 5) | `8 < 5` false | 0 |
| 3 | 1 | 1 (noise 3) | `4 < 3` false | 1 |
| 4 | 1 | 1 (noise 3) | `2 < 3` → replace | 4 |
| 5 | 2 | 0 (noise 5) | `8 < 5` false | 0 |
| 6 | 2 | 0 (noise 5) | `1 < 5` → replace | 6 |

Final output: `[0,1,0,1,4,0,6]`.

The important detail is node `5`: although its parent is `2`, the inherited best is still `0`, because the minimum on path `0 -> 2` is at the root.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Building the children list touches each node once, and the traversal processes each edge once. The per-node transition is constant time: compare two noise values and assign one index. At `10^6` nodes this is still practical; at `10^9`, even linear scans become infrastructure problems, but the algorithm remains asymptotically optimal.

### Space Complexity
`O(n)`. The adjacency list stores all `n - 1` edges, and the answer array stores one integer per node. Recursive DFS may also consume `O(h)` stack space, which is unsafe for skewed trees; iterative traversal avoids that without changing asymptotic memory.

## 💡 Key Takeaways
- If each node’s answer depends only on the root-to-node path, look for a top-down tree DP that propagates a compact state.
- When the problem asks for “best ancestor including self,” avoid repeated upward scans; derive each node from its parent in `O(1)`.
- Tie-breaking here is strict: update only on `<`, not `<=`, or you will incorrectly prefer deeper nodes over earlier ancestors.
- Recursive DFS can fail on a degenerate chain of length `100000`; use an explicit stack in production-grade implementations.
- The transferable design insight is to push inherited state through a hierarchy once, instead of recomputing path aggregates independently at every leaf.

## 🚀 Variations & Further Practice
- Support online queries and point updates to `noise`: now the tree is dynamic, and you need heavy-light decomposition or Euler tour + segment tree instead of a single DFS.
- Return the top-`k` quietest ancestors on each root path: the state is no longer a single scalar candidate, forcing a richer path summary structure.
- Generalize from minimum noise to arbitrary associative path aggregates with custom tie-breaking; this shifts the problem toward reusable tree-aggregation frameworks.