# Maximum Delayed Gates in a Directed Escape Network

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, Strongly Connected Components, DAG

---

## 🗂 Problem Overview
Given a directed graph, lock as many junctions as possible while keeping junctions `0` and `n-1` unlocked. After locking, every unlocked node that remains reachable from `0` must still be able to reach `n-1`. Return the maximum number of lockable junctions. The challenge is global, not local: removing one node can invalidate entire reachable regions, so the problem is about preserving a valid subgraph, not just preserving one source-to-sink path.

## 🌍 Engineering Impact
This pattern shows up in workflow engines, build systems, streaming DAGs, network policy graphs, and service dependency maps. You often need to disable as much infrastructure as possible while preserving a “safe live core” where every still-reachable component can complete successfully. Without SCC condensation and reachability reasoning, teams overfit to path existence and miss latent dead regions that remain activated but cannot terminate correctly. At scale, that creates stranded jobs, partial rollouts, dangling dependencies, and misleading health signals. The graph reduction here is exactly the kind of structural simplification that turns an intractable operational question into a linear-time decision.

## 🔍 Problem Statement
You are given `n` junctions (`2 <= n <= 200000`) and `m` directed corridors (`1 <= m <= 300000`). Each edge `[u, v]` allows movement from `u` to `v`. Junction `0` is the control center; junction `n-1` is the only exit. You may lock any junction except `0` and `n-1`.

A locking is valid iff, in the remaining graph, every unlocked node reachable from `0` can also reach `n-1`. Reachability from `0` alone is insufficient: any reachable node that becomes trapped away from the exit makes the configuration invalid unless that node is also locked or made unreachable.

Return the maximum number of locked junctions.

Examples:

- `n = 6`, `edges = [[0,1],[1,2],[2,5],[0,3],[3,4]]` → `2`
- `n = 8`, `edges = [[0,1],[1,2],[2,7],[0,3],[3,2],[1,4],[4,5],[5,4],[5,6],[6,7]]` → `3`

The constraint that drives the algorithmic choice is graph size: `O(nm)` or repeated recomputation is dead on arrival.

## 🪜 How to Solve This
1. Start from the validity rule → every reachable unlocked node must still have a route to the exit. That means the final unlocked reachable region must be a source-to-sink “closed” subgraph.

2. Cycles immediately suggest SCCs → inside a strongly connected component, nodes are mutually dependent. If one node in an SCC is reachable, the whole SCC behaves as one unit for reachability reasoning.

3. Compress SCCs into the condensation DAG → now the graph is acyclic, which makes “can reach exit” and “must remain” much easier to reason about.

4. In that DAG, any component reachable from the source component but unable to reach the sink component is invalid and must disappear from the reachable region.

5. The maximal locking strategy is therefore the minimal valid surviving region: keep only components that are both reachable from `S = SCC(0)` and can reach `T = SCC(n-1)`.

6. Everything else can be locked, except nodes inside `S` and `T` that are mandatory because `0` and `n-1` themselves cannot be locked.

Once you see “reachable nodes must also reach sink,” the problem becomes “find the intersection of forward reachability from source and reverse reachability from sink on the SCC DAG.”

## 🧩 Algorithm Walkthrough
1. **Compute SCCs** using Tarjan or Kosaraju.  
   Pattern: **Strongly Connected Components + Condensation DAG**.  
   Why: SCCs collapse cyclic mutual reachability into atomic units. Any valid solution reasons about components, not individual nodes trapped inside cycles.

2. **Build the condensation DAG** of SCCs.  
   For every original edge `u -> v`, if `comp[u] != comp[v]`, add `comp[u] -> comp[v]`.  
   Invariant: the condensed graph is a DAG, preserving all inter-component reachability.

3. **Identify source and sink components**:  
   `S = comp[0]`, `T = comp[n-1]`.  
   These components are special because nodes `0` and `n-1` cannot be removed, so any valid remaining graph must keep both SCCs.

4. **Forward reachability from `S`** on the DAG.  
   Mark every component reachable from the control center.  
   Invariant: these are exactly the components that could remain operational from the source side.

5. **Reverse reachability from `T`** on the reversed DAG.  
   Mark every component that can reach the exit.  
   Invariant: these are exactly the components that are exit-safe.

6. **Intersect the two sets**.  
   A component is valid to keep iff it is both reachable from `S` and can reach `T`.  
   Why correct: any reachable kept component outside this intersection would violate the rule; any component inside it can coexist without creating a dead-end reachable region.

7. **Count lockable nodes**.  
   Let `keep` be the total number of original nodes in valid components. Answer is `n - keep`.  
   This is maximal because the intersection is the smallest possible valid reachable region containing both mandatory endpoints.

## 📊 Worked Example
Use Example 1: `n=6`, `edges=[[0,1],[1,2],[2,5],[0,3],[3,4]]`

All nodes are singleton SCCs, so the condensation graph is the same.

| Step | Result |
|---|---|
| `S = SCC(0)` | `{0}` |
| `T = SCC(5)` | `{5}` |
| Forward reachable from `0` | `{0,1,2,3,4,5?}` actually `{0,1,2,3,4,5}` via `0→1→2→5` and `0→3→4` |
| Can reach `5` (reverse from `5`) | `{0,1,2,5}`? More precisely components `{1,2,5,0}` since `0→1→2→5` |
| Intersection | `{0,1,2,5}` |
| Nodes removed | `{3,4}` |

Trace:
1. Reachability from `0` includes both branches.
2. Reverse reachability from exit excludes the dead branch `3→4`.
3. Keeping `3` or `4` would leave a reachable node unable to reach `5`.
4. Minimal valid surviving region is `0→1→2→5`.
5. Locked count = `6 - 4 = 2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`. SCC decomposition is linear, building the condensation DAG is linear in original edges, and the two DAG traversals are linear in condensed nodes plus edges. At `10^6`-scale this is still practical; at `10^9`, even linear scans become infrastructure problems rather than algorithm problems.

### Space Complexity
`O(n + m)`. The adjacency lists, reverse graph, SCC metadata, component sizes, and condensed graph dominate memory. You can reduce constants by deduplicating condensed edges lazily, but that trades memory for more branchy traversal and implementation complexity.

## 💡 Key Takeaways
- If a graph problem says “every reachable node must also satisfy downstream reachability,” think source-reachable ∩ sink-co-reachable, usually after SCC compression.
- If cycles make node-level reasoning messy, collapse them first; DAG reasoning is often the real problem hiding underneath.
- Do not count only nodes that can reach the exit in the original graph; locking can change reachability, so correctness depends on the surviving reachable region.
- Be careful with `0` and `n-1` inside larger SCCs: you cannot partially lock an SCC if doing so changes internal reachability assumptions your proof relies on.
- In production graph systems, the winning move is often to compute the maximal safe core, then disable everything outside it; that yields simpler invariants and safer operations.

## 🚀 Variations & Further Practice
- **Weighted locking**: each junction has a lock cost or value, and you want the maximum total value removed while preserving validity. The twist is optimization over the SCC DAG rather than pure counting.
- **Multiple exits**: every reachable node must reach at least one approved sink. The harder part is reasoning about sink sets and preserving correctness under shared downstream structure.
- **Online edge removals**: corridors are deleted over time and you must maintain the maximum lockable set incrementally. The twist is dynamic SCC and dynamic reachability, which is substantially harder than the static case.