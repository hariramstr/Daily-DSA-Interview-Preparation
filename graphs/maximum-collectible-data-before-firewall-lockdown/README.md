# Maximum Collectible Data Before Firewall Lockdown

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, Strongly Connected Components, Dynamic Programming

---

## 🗂 Problem Overview
Given a directed graph, per-node data values, a start node, and multiple compromised nodes that induce lock deadlines, compute the maximum data collectible before nodes become unavailable. Movement costs 1 per edge, collection is instantaneous, and each node pays at most once. The non-trivial part is that revisits are allowed, so the optimal strategy is not a simple shortest-path tree: cycles can let you sweep entire safe regions, but only before deadline constraints cut them off.

## 🌍 Engineering Impact
This pattern shows up in incident containment, malware spread simulation, deadline-aware job execution, and distributed systems with expiring reachability windows. Examples include collecting diagnostics before hosts are quarantined, draining work from shards before lease expiry, or traversing dependency graphs before invalidation propagates. At scale, naive path search collapses under cyclic topology and repeated-state explosion. The SCC + condensation + DP approach turns an intractable state-space problem into a linear-time graph pipeline: compute deadlines once, compress strongly connected regions, then optimize over a DAG. That shift is exactly what enables predictable behavior on hundred-thousand-node topologies.

## 🔍 Problem Statement
You are given `n` servers, directed `edges`, `data[i]`, a starting server `start`, and a set of compromised servers. The security crawler starts simultaneously from all compromised servers and spreads along outgoing edges, one hop per minute. A server locks at its earliest crawler arrival time; unreachable servers have lock time `∞`.

You move from `start` at time `0`, also one hop per minute. You may collect `data[i]` only on the first visit to server `i`, and only if arrival time is **strictly less** than its lock time. Revisits are allowed but yield no additional data. You may stop anywhere.

Return the maximum collectible total.

Constraints: `n <= 2e5`, `m <= 3e5`, `data[i] <= 1e9`, self-loops and parallel edges allowed.

Examples:
- `n=5, edges=[[0,1],[1,2],[0,3],[3,2],[2,4]], data=[5,7,4,6,3], start=0, compromised=[4]`
- `n=6, edges=[[0,1],[1,2],[2,3],[0,4],[4,5],[5,3]], data=[2,8,5,10,4,7], start=0, compromised=[3]`

The scale rules out any solution that reasons over path histories explicitly.

## 🪜 How to Solve This
1. Read the locking rule → every node gets a deadline equal to the shortest distance from any compromised node. That is a multi-source BFS on the **reversed** graph.

2. Read the movement rule → your earliest arrival to a node from `start` is also a shortest-path distance. But shortest arrival alone is not enough, because cycles let you collect multiple nodes before leaving a region.

3. Notice revisits are free except for time → inside a strongly connected region, if you can enter early enough, you may be able to traverse and collect the whole region before exiting. That suggests **SCC compression**.

4. Collapse the graph into an SCC DAG → now each component is a unit with total data weight, but only if the component is internally collectible under the node deadlines.

5. Compute which SCCs are safe and what the earliest entry time is → then optimize over the condensed DAG with dynamic programming.

6. The key mental move: stop thinking in terms of individual walks; think in terms of **deadline-constrained reachability between strongly connected regions**.

## 🧩 Algorithm Walkthrough
1. **Compute lock times with multi-source BFS on the reversed graph.**  
   Start from all `compromised` nodes at distance `0`. In the reversed graph, BFS distance to `u` equals the shortest directed path from `u` to some compromised node in the original graph. This gives each node its lock deadline. Invariant: after BFS, `lock[u]` is the earliest time the crawler can lock `u`.

2. **Find strongly connected components using Kosaraju or Tarjan.**  
   SCCs are the right abstraction because inside one SCC every node can reach every other node. That is the only place where revisits and cyclic traversal can increase reward without requiring irreversible path choices. Invariant: the condensed graph is a DAG.

3. **Aggregate each SCC.**  
   Sum `data[u]` into `compValue[c]`. Also track whether nodes in the SCC are even collectible at candidate entry times. Practically, you need the earliest arrival to each node from `start`; compute this with BFS from `start` on the original graph.

4. **Determine collectible SCC contribution.**  
   For a node `u`, it is individually collectible iff `distStart[u] < lock[u]`. Nodes failing this are dead weight. For each SCC, only sum nodes satisfying that inequality. This is correct because any first arrival later than the shortest possible arrival cannot improve feasibility.

5. **Build the SCC DAG and run DP from the start component.**  
   Transition along condensed edges in topological order. `dp[c]` is the maximum collectible data upon reaching component `c`. Since the DAG removes cycles, standard longest-path DP applies. Invariant: when processing `c`, all predecessor-optimal values are finalized.

6. **Return the maximum `dp[c]` over reachable components.**  
   Stopping is allowed anywhere, so the answer is the best value seen, not necessarily at a sink.

Pattern-wise, this is **multi-source shortest paths + SCC condensation + DAG dynamic programming**.

## 📊 Worked Example
Use Example 1:

`n=5`  
`edges: 0→1, 1→2, 0→3, 3→2, 2→4`  
`data: [5,7,4,6,3]`, `start=0`, `compromised=[4]`

| Node | lock time | dist from start | collectible? | data |
|---|---:|---:|---:|---:|
| 0 | ∞ | 0 | yes | 5 |
| 1 | ∞ | 1 | yes | 7 |
| 2 | 1 | 2 | no (`2 < 1` false) | 4 |
| 3 | ∞ | 1 | yes | 6 |
| 4 | 0 | 3 | no | 3 |

Trace:
1. Reverse-BFS from `4` gives `lock[4]=0`, `lock[2]=1`, others `∞`.
2. BFS from `0` gives earliest arrivals `0,1,2,1,3`.
3. Every node is its own SCC here, so the condensed graph is the same DAG.
4. Valid rewards are only at `0,1,3`.
5. DP from `0`: collect `5`, then branch to `1` or `3`. Since both are terminal with respect to collectible progress, best path is `0→1` for `12`, or `0→3` for `11`. If additional safe cyclic reachability existed, SCC aggregation would capture it.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`. Reverse multi-source BFS is `O(n + m)`, SCC decomposition is `O(n + m)`, forward BFS from `start` is `O(n + m)`, and DP on the condensed DAG is linear in condensed nodes and edges. At `10^6` edges this is still practical; at `10^9`, no in-memory graph algorithm is.

### Space Complexity
`O(n + m)`. The graph, reverse graph, SCC metadata, distance arrays, and condensed DAG dominate memory. You can reduce constants by streaming edge construction or using CSR-style adjacency storage, but not the asymptotic bound without sacrificing traversal efficiency.

## 💡 Key Takeaways
- If a graph problem allows revisits and asks for maximum one-time reward, look for SCCs before reaching for path-state search.
- If “bad events” spread simultaneously from multiple sources, that is usually a multi-source shortest-path precomputation of deadlines or contamination times.
- The lock condition is **strictly before** deadline, so `arrival < lock`, not `<=`; this is the easiest correctness bug.
- Distinguish original vs reversed graph carefully: crawler deadlines come from BFS on the reversed graph, not the original adjacency.
- The transferable design insight is to separate time-feasibility computation from reward optimization, then compress cyclic structure before doing DP.

## 🚀 Variations & Further Practice
- Add weighted edges instead of unit travel time. The conceptual twist is replacing BFS with multi-source Dijkstra and preserving correctness under non-uniform traversal costs.
- Require returning to a safe sink before lockdown. This turns the problem into deadline-aware collection with terminal feasibility, often needing forward/backward feasibility states on the SCC DAG.
- Let lock times depend on edge-specific propagation delays or periodic reopening. The harder part is that SCC condensation alone no longer captures all temporal behavior; you need time-expanded reasoning or richer state DP.