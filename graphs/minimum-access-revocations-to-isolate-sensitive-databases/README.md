# Minimum Access Revocations to Isolate Sensitive Databases

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, Connected Components, DFS

---

## 🗂 Problem Overview
Given an undirected graph, remove the fewest edges so that no connected component contains more than one sensitive node. The input is `n`, an edge list, and a distinct list `sensitive`; the output is the minimum number of edge removals required. The non-trivial part is scale: with up to `2 * 10^5` nodes and edges, brute-force cut exploration is impossible, so the solution must exploit component structure and graph invariants.

## 🌍 Engineering Impact
This pattern shows up in network segmentation, zero-trust service meshes, blast-radius reduction, tenant isolation, and compliance-driven data boundary enforcement. In practice, the graph is an access topology: services, databases, IAM relationships, or east-west traffic permissions. The question is not “can these entities reach each other?” but “what is the minimum change set to enforce isolation?” At scale, ad hoc edge-by-edge reasoning fails because cycles and disconnected regions hide the true cut cost. A component-level traversal gives deterministic planning, bounded runtime, and a clean basis for automated remediation or policy simulation.

## 🔍 Problem Statement
You are given an undirected graph of `n` systems labeled `0..n-1` and `edges`, where each `[u, v]` is an existing communication link. A subset of nodes, `sensitive`, host protected databases. You may revoke one existing link per operation by removing one edge.

Return the minimum number of edge removals needed so that every connected component contains at most one sensitive node.

Constraints:
- `1 <= n <= 200000`
- `0 <= edges.length <= 200000`
- no self-loops, no duplicate edges
- graph may already be disconnected
- `sensitive` contains distinct nodes

Examples:

- `n = 7`, `edges = [[0,1],[1,2],[1,3],[3,4],[3,5],[5,6]]`, `sensitive = [2,4,6]` → `2`
- `n = 8`, `edges = [[0,1],[1,2],[2,0],[2,3],[4,5],[5,6],[6,7]]`, `sensitive = [0,3,7]` → `1`

The key constraint is input size: any solution must be near-linear in nodes plus edges.

## 🪜 How to Solve This
1. Read the problem → the condition is defined per **connected component**, so the first instinct should be to process the graph component by component.

2. Inside one component, suppose there are `k` sensitive nodes. The final state must split that component into at least `k` components, because each resulting component may contain at most one sensitive node.

3. In any connected component, every removed edge can increase the number of connected components by **at most one**. So creating `k` components from one original component needs at least `k - 1` removals.

4. Is `k - 1` always achievable? Yes. Take any spanning tree of the component. The sensitive nodes lie in that tree too, and in a tree you can cut edges to separate `k` marked nodes using exactly `k - 1` cuts.

5. That means cycles do not change the minimum. They only add redundant edges; the answer depends solely on how many sensitive nodes each original connected component contains.

6. So the job reduces to: traverse each connected component, count how many sensitive nodes it contains, and add `count - 1` if `count > 0`.

That is a standard **Connected Components + DFS/BFS counting** problem.

## 🧩 Algorithm Walkthrough
1. **Build adjacency lists**  
   Convert the edge list into an adjacency list for `0..n-1`. This supports linear-time traversal. Also build a boolean array or hash set for `sensitive` membership so node classification is `O(1)`.

2. **Traverse each unvisited node**  
   Run DFS or iterative DFS/BFS from every node not yet visited. This identifies exactly one connected component per traversal.  
   **Invariant:** when a traversal finishes, every node in that component has been visited exactly once.

3. **Count sensitive nodes within the component**  
   During traversal, maintain `sensitiveCount`. Increment when the current node is marked sensitive.  
   **Why this is sufficient:** the minimum removals for a component depend only on the number of sensitive nodes in that component, not on the exact topology.

4. **Add the component contribution**  
   If `sensitiveCount = k`, contribute `max(0, k - 1)` to the answer.  
   **Correctness argument:**  
   - Lower bound: each removed edge increases component count by at most one, so splitting one component into at least `k` valid pieces requires at least `k - 1` cuts.  
   - Upper bound: choose any spanning tree of the component and cut `k - 1` edges to separate the `k` sensitive nodes. Therefore the bound is tight.

5. **Sum across components**  
   Components are independent because removing edges in one component cannot affect another.  
   **Pattern:** this is a **Connected Components traversal** problem with a structural graph invariant, not a min-cut problem.

6. **Use iterative traversal in production-grade implementations**  
   With `2 * 10^5` nodes, recursive DFS may overflow the call stack in skewed graphs. Iterative DFS/BFS is safer while preserving `O(n + m)` complexity.

## 📊 Worked Example
Use Example 1:

`n = 7`  
`edges = [[0,1],[1,2],[1,3],[3,4],[3,5],[5,6]]`  
`sensitive = [2,4,6]`

Single connected component:

| Step | Visited Node(s) | Sensitive Found | `sensitiveCount` |
|---|---|---:|---:|
| 1 | 0 | no | 0 |
| 2 | 1 | no | 0 |
| 3 | 2 | yes | 1 |
| 4 | 3 | no | 1 |
| 5 | 4 | yes | 2 |
| 6 | 5 | no | 2 |
| 7 | 6 | yes | 3 |

Traversal finishes: one component contains `3` sensitive nodes.

Contribution to answer:
- Need at least `3` valid components
- Starting from `1` component
- Minimum removals = `3 - 1 = 2`

One valid cut set is removing edges `[1,3]` and `[3,5]`, yielding components containing sensitive nodes `{2}`, `{4}`, and `{6}` separately.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `m = edges.length`. Building adjacency lists is linear in edges, and each node and edge is processed once during component traversal. This scales comfortably to `10^6` total graph elements; at `10^9`, the issue becomes memory bandwidth and graph storage, not algorithmic shape.

### Space Complexity
`O(n + m)` for the adjacency list, visited structure, and sensitive-membership lookup. This is the natural cost of explicit graph traversal. You can reduce constant factors with compact adjacency representations, but not the asymptotic bound without changing the access model.

## 💡 Key Takeaways
- If the requirement is phrased per connected region and edits cannot cross components, think connected-components traversal before considering heavier cut algorithms.
- When each edge removal can increase component count by at most one, “need `k` final pieces” is a strong signal that the answer may collapse to `k - 1`.
- Do not overfit to cycles: they look like they should matter, but here the minimum is determined by sensitive-node count per component, not by edge density.
- Recursive DFS is risky at `2 * 10^5` nodes; use iterative DFS/BFS to avoid stack overflow on long chains.
- In production isolation planning, topology details often matter less than structural invariants; reducing the problem to component-local counts yields simpler, auditable remediation logic.

## 🚀 Variations & Further Practice
- **Weighted revocations:** each edge has a removal cost, and you need minimum total cost rather than minimum count. This turns the problem into a tree/graph cut optimization rather than a pure counting invariant.
- **Directed access graph:** reachability replaces undirected connectivity, so strongly connected components or reachability cuts become relevant and the `k - 1` argument no longer holds directly.
- **Online updates:** edges are added/removed and sensitive nodes change over time. The harder twist is maintaining component-sensitive counts incrementally instead of recomputing from scratch.