# Minimum Relay Hops to Synchronize Field Sensors

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, Strongly Connected Components, Directed Graph

---

## 🗂 Problem Overview
Given a directed graph of `n` sensors and relay links, determine the minimum number of new one-way edges needed so every sensor can reach sensor `0`. The output is a single integer: the fewest boosters required. The non-trivial part is scale: with up to `2e5` nodes and `3e5` edges, brute-force reachability checks or greedy per-node fixes are too expensive. The right abstraction is to collapse cycles and reason over component-level reachability.

## 🌍 Engineering Impact
This pattern shows up anywhere directed dependencies must be made globally drainable to a sink: event pipelines that must eventually flush to durable storage, service dependency graphs that need a path to a control plane, workflow DAGs with cyclic subsystems, and compiler/linker dependency condensation. At scale, node-level reasoning breaks because local fixes ignore strongly connected regions and duplicate work. SCC condensation gives the right control surface: it turns a messy directed graph into a DAG, where you can count exactly which isolated regions need attachment. That enables deterministic remediation planning instead of heuristic patching.

## 🔍 Problem Statement
You are given `n` sensors labeled `0..n-1` and a list `edges`, where `edges[i] = [u, v]` means sensor `u` can directly send data to sensor `v`. A sensor is synchronized if there exists a directed path from that sensor to sensor `0`. You may add any number of new directed edges between arbitrary pairs of sensors. Return the minimum number of added edges required so all sensors become synchronized.

Constraints:

- `1 <= n <= 200000`
- `0 <= edges.length <= 300000`
- `0 <= u, v < n`
- `u != v`
- Duplicate edges may exist

Examples:

- `n = 6`, `edges = [[1,0],[2,1],[3,4],[4,5]]` → `1`
- `n = 7`, `edges = [[1,2],[2,3],[3,1],[4,5],[5,6]]` → `2`

The key constraint is graph size: any approach worse than linear or near-linear in `n + m` will not hold up.

## 🪜 How to Solve This
1. Start from the goal → every node must eventually reach `0`. That is a reachability-to-sink problem on a directed graph.

2. Notice cycles immediately → if `a`, `b`, and `c` can all reach each other, adding a single edge from any one of them can synchronize the whole group. So the unit of work is not a node; it is a strongly connected component.

3. Collapse SCCs → once each SCC becomes a single component, the graph becomes a DAG. In that DAG, either a component can already reach the component containing `0`, or it cannot.

4. Ask what one added edge can accomplish → if a component has no outgoing edge to any other unsynchronized component, then nothing downstream can “carry” it toward `0`. That component needs a direct new connection into the synchronized side.

5. Therefore the answer is: count SCCs in the condensation DAG that do **not** reach `SCC(0)` and have **out-degree 0 within the unsynchronized subgraph**. Each such sink component needs one booster; every other unsynchronized component can route through one of them after connection.

## 🧩 Algorithm Walkthrough
1. **Compute Strongly Connected Components** using Tarjan’s or Kosaraju’s algorithm.  
   This is the core pattern: **SCC condensation of a directed graph**. It is the right abstraction because nodes inside one SCC are mutually reachable, so they rise and fall together with respect to synchronization.

2. **Assign each node a component id** and identify `root = comp[0]`.  
   Correctness invariant: all nodes in the same component have identical reachability behavior in the condensation DAG.

3. **Build the condensation DAG implicitly** by scanning original edges `(u, v)`.  
   If `comp[u] != comp[v]`, then there is a DAG edge `comp[u] -> comp[v]`. Duplicate edges can be ignored logically; they do not change the answer.

4. **Mark components that can already reach `root`** by traversing the reverse condensation graph starting from `root`.  
   Why reverse? In the original DAG, we need components with a path **to** `root`. On reversed edges, that becomes a standard forward reachability walk from `root`.

5. **Restrict attention to unsynchronized components**: those not marked reachable-to-root.  
   These are exactly the regions still needing remediation.

6. **Count sink components inside the unsynchronized induced subgraph**.  
   For each condensation edge `A -> B`, if both `A` and `B` are unsynchronized, then `A` is not a sink. Components with no such outgoing edge are terminal unsynchronized regions.

7. **Return the number of those sinks**.  
   This is minimal because each sink unsynchronized component needs at least one new outgoing path, and sufficient because connecting each sink once to the synchronized side causes all upstream unsynchronized components to flow into it, then into `0`.

## 📊 Worked Example
Use `n = 7`, `edges = [[1,2],[2,3],[3,1],[4,5],[5,6]]`.

1. SCCs:
   - `C0 = {0}`
   - `C1 = {1,2,3}`
   - `C2 = {4}`
   - `C3 = {5}`
   - `C4 = {6}`

2. Condensation edges:

| From | To |
|---|---|
| `C2` | `C3` |
| `C3` | `C4` |

`C1` has no outgoing condensation edge. `root = C0`.

3. Components that can reach `C0`:
   - Only `C0` itself.

4. Unsynchronized components:
   - `C1, C2, C3, C4`

5. Outgoing edges within unsynchronized subgraph:
   - `C2 -> C3`
   - `C3 -> C4`

6. Unsynchronized sinks:
   - `C1` and `C4`

Answer = `2`. One valid plan is adding `1 -> 0` and `6 -> 0`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n + m)`, where `m = edges.length`. SCC construction is linear, condensation analysis is another linear scan, and reverse reachability over components is linear in condensed edges. At `10^6` scale this is still practical in optimized implementations; at `10^9`, even linear graph traversal becomes infrastructure-bound rather than algorithm-bound.

### Space Complexity
`O(n + m)` for adjacency storage, SCC bookkeeping, component ids, and condensed/reverse edge structures. You can reduce some constant factors by avoiding explicit deduplication of component edges, but asymptotically the graph representation dominates memory either way.

## 💡 Key Takeaways
- If the graph is directed and the requirement is “make every node able to reach one target,” look for SCC condensation before considering node-level greedy fixes.
- If one added edge can repair an entire cyclic region, the true unit of optimization is the component DAG, not individual vertices.
- Do not count SCCs with zero in-degree; this problem is about reachability **to `0`**, so the relevant structure is unsynchronized **sinks** in the condensation DAG.
- Be careful with direction when marking synchronized components: use reverse edges from `SCC(0)` to find components that can reach `0`, not components reachable from `0`.
- In production graph remediation, collapsing strongly coupled subsystems first turns noisy local dependencies into a tractable control-plane problem with exact minimal interventions.

## 🚀 Variations & Further Practice
- **Weighted booster installation:** each added edge `(a, b)` has a cost, and you want minimum total cost rather than minimum count. The twist is moving from pure structural counting to optimization over candidate attachments.
- **Dynamic graph updates:** edges are added or removed online, and you must maintain the minimum boosters after each change. The hard part is incremental SCC maintenance, not the final counting logic.
- **Multiple base stations:** a sensor is synchronized if it can reach any node in a target set. The extension changes the root condition from one sink component to a set of acceptable terminal components.