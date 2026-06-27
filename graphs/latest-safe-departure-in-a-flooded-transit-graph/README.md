# Latest Safe Departure in a Flooded Transit Graph

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, Binary Search, Shortest Path

---

## 🗂 Problem Overview
Given an undirected graph, a start node `0`, a destination `n - 1`, and a deadline `floodTime[i]` for every node, compute the latest integer time you can depart and still reach the destination without ever occupying a flooded node. Moving across an edge costs exactly one minute; waiting is allowed only while the current node remains usable. The challenge is that feasibility depends on both path length and per-node time windows, so plain shortest path is not enough by itself.

## 🌍 Engineering Impact
This pattern shows up anywhere reachability is constrained by time-varying node validity: packet routing through degrading network segments, workflow execution over expiring compute slots, evacuation planning on failing infrastructure, and distributed schedulers placing work on nodes with shrinking maintenance windows. At scale, brute-force simulation over start times or paths collapses immediately. The useful abstraction is monotone feasibility: if departure at time `t` works, every earlier time works. That enables binary search over the answer, with a linear-time graph check per probe. The result is predictable performance under large graphs and large time domains.

## 🔍 Problem Statement
You are given `n` stations, `m` undirected tunnels, and an array `floodTime` where `floodTime[i]` is the earliest integer minute when station `i` becomes unusable. You start at station `0` and must reach station `n - 1`. You may be at or enter station `i` only at times strictly less than `floodTime[i]`. Each tunnel traversal takes exactly `1` minute. Waiting is allowed, but only while the current station remains usable for the entire wait.

Return the latest integer departure time `t` such that a valid route exists. Return `-1` if even `t = 0` is impossible. Return `1000000000` if you can delay departure arbitrarily long.

Examples:

- `n = 5, edges = [[0,1],[1,2],[2,4],[0,3],[3,4]], floodTime = [4,3,5,2,6]` → `1`
- `n = 4, edges = [[0,1],[1,2],[2,3],[0,2]], floodTime = [1000000000,1000000000,1000000000,1000000000]` → `1000000000`

The key constraint is scale: up to `200000` nodes and `300000` edges, which rules out per-time simulation or stateful time-expanded graphs.

## 🪜 How to Solve This
1. Read the condition carefully → validity is checked on **arrival times at nodes**, with a strict `< floodTime[i]` inequality.
2. Notice waiting never helps once a departure time is fixed → waiting only makes future arrivals later, and later is never better when every constraint is an upper bound.
3. So for a chosen start time `t`, the question becomes: is there **any path** from `0` to `n - 1` such that `t + distance_along_path_to_node < floodTime[node]` for every visited node?
4. That turns the feasibility check into a shortest-path style reachability problem. Since every edge weight is `1`, BFS is enough.
5. Next observation: feasibility is monotone. If starting at time `t` works, then starting earlier than `t` also works because all arrival times shift earlier.
6. Monotone predicate over a huge answer range (`0..1e9`) → binary search the latest feasible `t`.
7. Handle the special “infinite delay” case first: if departure at `1e9` is feasible, return `1e9` directly.
8. Otherwise, binary search the maximum feasible start time; if even `0` fails, return `-1`.

That is the whole shape: **BFS as decision procedure + binary search on answer**.

## 🧩 Algorithm Walkthrough
1. **Build the adjacency list.**  
   Use a standard graph representation for `n` nodes and `m` undirected edges. This gives `O(n + m)` traversal cost per feasibility check.

2. **Define a feasibility function `canStart(t)`.**  
   This is the core pattern: **Binary Search on Answer** with a **BFS reachability check**. First reject immediately if `t >= floodTime[0]`, because the start node is already unusable at departure.

3. **Run BFS from node `0` with time-aware pruning.**  
   Store the earliest arrival time for each node, or equivalently BFS depth from the start plus `t`. When exploring edge `u -> v`, the candidate arrival is `arrival[u] + 1`. Push `v` only if:
   - this arrival is earlier than any previously known arrival at `v`, and
   - `candidateArrival < floodTime[v]`.

4. **Why BFS is correct here.**  
   All edges cost `1`, so BFS discovers nodes in nondecreasing arrival time. Since every node constraint is “arrive before deadline,” the earliest arrival dominates all later arrivals. Once BFS has found the earliest reachable time for a node, no later path can improve feasibility.

5. **Why waiting can be ignored.**  
   Waiting only increases arrival times and cannot turn an invalid arrival into a valid one under strict upper bounds. Therefore, the earliest-arrival search is sufficient; there is no hidden benefit from adding idle time.

6. **Use monotonicity to binary search.**  
   If `canStart(t)` is true, then `canStart(t - 1)` is also true. Search the maximum feasible `t` in `[0, 1e9]`. Before the search, check `canStart(0)` for the impossible case and `canStart(1e9)` for the “arbitrarily long” case.

7. **Return the boundary value.**  
   The binary search invariant is: low is feasible, high is infeasible, or vice versa depending on implementation. Maintain it carefully and return the largest feasible start time.

## 📊 Worked Example
Use `n = 5`, `edges = [[0,1],[1,2],[2,4],[0,3],[3,4]]`, `floodTime = [4,3,5,2,6]`.

Check `t = 1`:

| Step | Node popped | Arrival time | Next move | Valid? |
|---|---:|---:|---|---|
| 1 | 0 | 1 | `0 -> 1` arrives at 2 | `2 < 3`, yes |
| 2 | 0 | 1 | `0 -> 3` arrives at 2 | `2 < 2`, no |
| 3 | 1 | 2 | `1 -> 2` arrives at 3 | `3 < 5`, yes |
| 4 | 2 | 3 | `2 -> 4` arrives at 4 | `4 < 6`, yes |

Destination is reachable, so `t = 1` works.

Check `t = 2`:

- Start at node `0` at time `2`, still valid since `2 < 4`.
- `0 -> 1` arrives at `3`, but `3 < 3` is false.
- `0 -> 3` arrives at `3`, but `3 < 2` is false.

No expansion is possible, so `t = 2` fails. Therefore the latest safe departure is `1`.

## ⏱ Complexity Analysis
### Time Complexity
Each feasibility check is a BFS over the graph: `O(n + m)`. Binary search over the answer range `0..1e9` takes `O(log 1e9) ≈ 30` checks, so total time is `O((n + m) log 1e9)`. At million-scale graphs this remains practical; anything quadratic would not.

### Space Complexity
The adjacency list, visited/arrival array, and BFS queue use `O(n + m)` space. That is already asymptotically tight for sparse graph traversal. You can store only visited state instead of full arrival times in pure BFS, but the asymptotic footprint does not change.

## 💡 Key Takeaways
- If the problem asks for the **latest** feasible time and feasibility only gets easier when moving earlier, that is a strong signal for binary search on the answer.
- If all edges have equal weight and constraints are deadline-based on nodes, think BFS with arrival-time pruning before reaching for Dijkstra.
- The inequality is strict: arriving at exactly `floodTime[i]` is invalid, including for the start and destination nodes.
- Waiting is a trap here; it feels relevant because the statement allows it, but under upper-bound deadlines it never improves feasibility.
- In production systems, this is the standard move when a large optimization space can be reduced to a monotone yes/no predicate plus a fast decision procedure.

## 🚀 Variations & Further Practice
- **Weighted tunnels instead of unit travel time:** replace BFS with Dijkstra; the conceptual twist is that earliest-arrival dominance still holds, but shortest path is no longer level-ordered.
- **Flood on edges as well as nodes:** each edge gets its own expiration time, so transitions must satisfy both node and edge deadlines; this turns pruning logic into time-windowed graph traversal.
- **Periodic safe/unsafe intervals per station:** feasibility is no longer a simple upper-bound deadline, so waiting may become useful and the monotonic structure can partially break.