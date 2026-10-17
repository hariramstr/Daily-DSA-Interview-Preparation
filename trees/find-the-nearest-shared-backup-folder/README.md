# Find the Nearest Shared Backup Folder

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Lowest Common Ancestor, Binary Lifting

---

## 🗂 Problem Overview
Given a rooted tree encoded as a `parent` array and many queries `[a, b]`, return the deepest folder that is an ancestor of both nodes. This is the Lowest Common Ancestor of `a` and `b`, with each node considered its own ancestor. The challenge is scale: both the tree size and query count can reach `2 * 10^5`, so walking parent chains per query is too slow. The solution must preprocess the tree once and answer repeated ancestor queries efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere hierarchical ownership, containment, or inheritance must be queried repeatedly at scale: filesystem metadata services, IAM policy trees, compiler scope resolution, org-chart authorization checks, taxonomy lookup in search systems, and versioned configuration inheritance. The failure mode is predictable: teams ship a correct but linear-per-query traversal, then query fanout or tail latency explodes under load. Precomputing jump pointers turns a hot-path tree walk into logarithmic lookup, which is exactly the kind of trade-off that enables predictable latency, bounded CPU, and safe multi-tenant scaling in read-heavy systems.

## 🔍 Problem Statement
You are given a valid rooted tree with `n` folders labeled `0..n-1`, where `0` is the root and `parent[0] = -1`. For every `i > 0`, `parent[i]` gives the immediate parent of folder `i`. For each query `[a, b]`, return the deepest folder that is an ancestor of both `a` and `b`. A folder is an ancestor of itself.

Constraints:
- `1 <= n <= 2 * 10^5`
- `1 <= queries.length <= 2 * 10^5`
- `0 <= a, b < n`
- The `parent` array always forms a valid tree rooted at `0`

Examples:

- `parent = [-1,0,0,1,1,2,2]`, `queries = [[3,4],[3,5],[2,6],[0,6]]` → `[1,0,2,0]`
- `parent = [-1,0,1,2,3,1,5]`, `queries = [[4,6],[2,4],[6,6],[3,5]]` → `[1,2,6,1]`

The key constraint is repeated querying on a static tree. That rules out recomputing ancestor paths per query and points directly to LCA preprocessing.

## 🪜 How to Solve This
1. Read the problem → this is not “find one common ancestor,” it is “answer many common-ancestor queries on the same tree.”
2. Many queries on static structure → preprocessing is almost certainly the right move.
3. The target is the *deepest* shared ancestor → that is exactly the Lowest Common Ancestor problem.
4. A naive approach would walk each node upward to the root per query → `O(height)` or worse, which becomes too expensive at `2 * 10^5` queries.
5. To compare two nodes efficiently, first compute each node’s depth in the tree.
6. If depths differ, lift the deeper node upward until both nodes are on the same level.
7. Then lift both nodes upward together, but do it in powers of two instead of one step at a time.
8. That suggests binary lifting: precompute `up[v][j]`, the `2^j`-th ancestor of node `v`.
9. After preprocessing, each query becomes: equalize depth → jump both nodes upward from large powers to small → return their parent once they diverge.
10. This works because the tree is static, queries are numerous, and logarithmic jumps dominate linear climbs.

## 🧩 Algorithm Walkthrough
1. **Build children adjacency from the parent array.**  
   This converts the implicit tree into a traversal-friendly structure. Since the input is guaranteed to be a valid rooted tree, every node except `0` has exactly one incoming edge.

2. **Compute `LOG = ceil(log2(n)) + 1`.**  
   This is the number of jump levels needed so any node can be lifted to the root using powers of two. The invariant is that `up[v][j]` represents the `2^j`-th ancestor if it exists.

3. **Run DFS or BFS from root `0` to fill `depth[v]` and `up[v][0]`.**  
   `depth[v]` is the distance from root. `up[v][0]` is the direct parent. This establishes the base case for binary lifting.

4. **Fill the binary lifting table.**  
   For each node `v` and level `j > 0`, compute  
   `up[v][j] = up[ up[v][j-1] ][j-1]` if the intermediate ancestor exists.  
   This is correct because a `2^j` jump is two consecutive `2^(j-1)` jumps.

5. **Answer each query using the Lowest Common Ancestor pattern with Binary Lifting.**  
   If one node is deeper, lift it upward until depths match. If they become equal, that node is the answer. Otherwise, scan jump levels from high to low and lift both nodes whenever their ancestors differ. This preserves the invariant that the true LCA remains above both nodes. After the loop, their immediate parent is the LCA.

6. **Return answers in query order.**  
   Preprocessing is done once; each query is independent and logarithmic.

## 📊 Worked Example
Take:

- `parent = [-1,0,0,1,1,2,2]`
- Query: `[3,5]`

Depths after preprocessing:

| Node | Parent | Depth |
|------|--------|-------|
| 0    | -1     | 0     |
| 1    | 0      | 1     |
| 2    | 0      | 1     |
| 3    | 1      | 2     |
| 5    | 2      | 2     |

Trace for `LCA(3,5)`:

1. `depth[3] = depth[5] = 2` → no need to lift for depth alignment.
2. Compare highest jump ancestors:
   - At jump `2^1`, both ancestors are `0`, so do not move; jumping would overshoot the divergence point.
   - At jump `2^0`, ancestors are `1` and `2`, which differ → lift both: `3 -> 1`, `5 -> 2`.
3. Now `1` and `2` are distinct but their parents are both `0`.
4. Return `parent[1] = 0`.

Answer: `0`, which matches the shared deepest ancestor on paths `3 -> 1 -> 0` and `5 -> 2 -> 0`.

## ⏱ Complexity Analysis
### Time Complexity
Preprocessing takes `O(n log n)` to build the jump table. Each query takes `O(log n)` for depth alignment and synchronized lifting, so total query cost is `O(q log n)`. At `10^6` scale, logarithmic query work is still practical; at `10^9`, only heavily preprocessed or distributed approaches remain viable.

### Space Complexity
Space is `O(n log n)` for the binary lifting table, plus `O(n)` for depth and adjacency. The jump table dominates memory. You can reduce space with Euler Tour + RMQ variants or Tarjan offline LCA, but that changes implementation complexity and query model.

## 💡 Key Takeaways
- If the input is a rooted tree and the task asks for the deepest shared ancestor across many queries, think LCA immediately.
- “Static structure, many lookups” is the signal to trade preprocessing space for predictable logarithmic query latency.
- Be precise about ancestor semantics: a node is an ancestor of itself, so `[x, x]` must return `x`.
- Compute enough jump levels to cover the root path; underallocating `LOG` causes silent wrong answers on deep trees.
- In production systems, precomputed navigation indexes are often the difference between elegant hierarchy modeling and a latency disaster under read-heavy workloads.

## 🚀 Variations & Further Practice
- **K-th ancestor queries:** preprocess the same binary lifting table, but answer “jump up `k` levels” instead of shared ancestor; the twist is bit-decomposing arbitrary jumps.
- **LCA with edge weights or path aggregates:** return not just the ancestor but also min/max/sum along the path; the harder part is storing aggregate state alongside jump pointers.
- **Dynamic trees:** support parent changes or link/cut operations; binary lifting no longer fits cleanly, pushing you toward Euler Tour Trees, Link-Cut Trees, or heavy dynamic indexing.