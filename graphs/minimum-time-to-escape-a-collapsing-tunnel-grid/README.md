# Minimum Time to Escape a Collapsing Tunnel Grid

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** graphs, dijkstra, shortest-path, time-dependent-graph, priority-queue

---

## 🗂 Problem Overview
Given a large undirected graph, compute the earliest time to travel from junction `0` to junction `n - 1` when both nodes and edges are time-constrained. Each node expires at `collapse[i]`, and each edge can only be entered during periodic open windows. You may wait at safe nodes, but only while they remain uncollapsed. Return the minimum valid arrival time, or `-1` if no feasible route exists. The challenge is that shortest-path feasibility depends on arrival time.

## 🌍 Engineering Impact
This pattern shows up in systems where reachability depends on time, not just topology: packet routing across maintenance windows, job scheduling through rate-limited services, transit planning with timed departures, and workflow orchestration across expiring leases or credentials. At scale, static shortest-path assumptions fail because the cheapest edge now may be unusable when reached. Without a time-aware algorithm, systems either over-admit invalid paths or miss feasible ones hidden behind waiting. Modeling the graph as time-dependent enables correct routing, SLA-aware planning, and predictable failure handling under dynamic availability constraints.

## 🔍 Problem Statement
You are given `n` junctions and `m` undirected tunnels. Each tunnel is described as `[u, v, w, open, close]`: traversal takes `w` minutes, and the tunnel may only be entered at integer time `t` when `t mod (open + close) < open`. Each junction `i` has collapse time `collapse[i]`, and you may be at that junction only when `time < collapse[i]`. This applies both to arrival and waiting.

Start at junction `0` at time `0` and reach junction `n - 1` as early as possible. Return the minimum valid arrival time, or `-1` if impossible.

Constraints force an `O((n + m) log n)`-style solution: `n <= 1e5`, `m <= 2e5`, and times up to `1e15`.

Examples:
- `n=4`, corrected representative edge set including `[2,3,1,2,2]` → answer `5`
- `n=3`, corrected collapse set `[20,5,9]` → answer `-1`

## 🪜 How to Solve This
1. Read the problem → this is not plain shortest path, because edge usability depends on **when** you reach a node.
2. Notice waiting is allowed → that usually means the state can still be represented by just the **earliest arrival time** at each node, if later arrivals are never better.
3. Check that property → periodic gates plus nonnegative waiting preserve FIFO behavior: arriving earlier can simulate arriving later by waiting.
4. That points directly to **Dijkstra on a time-dependent graph**.
5. For each popped state `(time, node)`, evaluate every adjacent tunnel:
   - If the node is already collapsed at `time`, discard it.
   - Compute the earliest departure time `>= time` when the tunnel is open.
   - Ensure you can wait at the current node until that departure.
   - Compute arrival `departure + w` and ensure destination is still uncollapsed.
6. Relax the neighbor with that arrival time.
7. Because Dijkstra always expands the globally earliest unresolved arrival, the first valid pop of `n - 1` is optimal.

The key mental move is replacing “edge weight” with “edge travel function from current time.”

## 🧩 Algorithm Walkthrough
1. **Build an adjacency list** for the undirected graph, storing `(neighbor, w, open, close)` for each tunnel.  
   This keeps edge scans linear in total degree and is the standard representation for sparse graphs.

2. **Initialize Dijkstra state** with `dist[i] = INF`, set `dist[0] = 0`, and push `(0, 0)` into a min-heap.  
   Invariant: `dist[i]` is the best known valid arrival time at node `i`.

3. **Pop the smallest `(t, u)` from the heap**.  
   If `t != dist[u]`, skip stale state. If `t >= collapse[u]`, skip invalid state.  
   Invariant: any processed state corresponds to the earliest currently known valid arrival at `u`.

4. **For each tunnel from `u` to `v`**, compute the earliest legal departure:
   - Let `cycle = open + close`
   - Let `r = t % cycle`
   - If `r < open`, depart immediately at `t`
   - Otherwise, wait `cycle - r` minutes and depart at `t + (cycle - r)`  
   This is the time-dependent relaxation step.

5. **Validate waiting at `u`** by checking `departure < collapse[u]`.  
   You may stand at `u` only at strictly earlier times than its collapse, so if departure is not strictly less, that transition is impossible.

6. **Compute arrival** as `arrival = departure + w`, then require `arrival < collapse[v]`.  
   This enforces the destination expiration rule at the moment of arrival.

7. **Relax neighbor**: if `arrival < dist[v]`, update `dist[v]` and push `(arrival, v)`.  
   Invariant: heap always contains candidate earliest arrivals not yet finalized.

8. **Stop early when `n - 1` is popped validly**.  
   This is correct because the pattern is **Dijkstra on a FIFO time-dependent graph**: edge travel functions never reward later departure over earlier arrival plus optional waiting.

## 📊 Worked Example
Use the corrected representative example:

`n = 4`  
`edges = [[0,1,3,2,2],[1,3,2,3,1],[0,2,2,1,3],[2,3,1,2,2]]`  
`collapse = [100,10,10,20]`

| Step | Heap Pop | Action | Result |
|---|---:|---|---:|
| 1 | `(0,0)` | `0->1`: open at `0`, arrive `3` | `dist[1]=3` |
| 1 | `(0,0)` | `0->2`: open at `0`, arrive `2` | `dist[2]=2` |
| 2 | `(2,2)` | `2->3`: cycle `4`, `2 mod 4 = 2`, closed | wait to `4` |
| 2 | `(2,2)` | depart `4`, arrive `5` | `dist[3]=5` |
| 3 | `(3,1)` | `1->3`: cycle `4`, `3 mod 4 = 3`, closed | wait to `4`, arrive `6` |
| 4 | `(5,3)` | destination popped | answer `5` |

The important detail is that node `2` remains safe while waiting from time `2` to `4`, since `4 < collapse[2] = 10`.

## ⏱ Complexity Analysis
### Time Complexity
`O((n + m) log n)` in the standard sparse-graph setting. Each successful relaxation pushes into the priority queue, and each heap operation costs `log n`. The dominant cost is scanning all edges plus heap churn. This remains practical at `10^6` graph elements, but not at `10^9`, where partitioning or external-memory strategies would be required.

### Space Complexity
`O(n + m)` for the adjacency list, distance array, and priority queue. The adjacency list owns most of the space. You cannot reduce below `O(m)` without sacrificing efficient neighbor access; compressing edge storage trades memory for decoding overhead and implementation complexity.

## 💡 Key Takeaways
- If edge feasibility depends on arrival time but earlier arrival is never worse than later arrival, think **time-dependent Dijkstra**, not BFS or plain shortest path.
- When the problem allows waiting and uses periodic availability windows, that is a strong signal the graph is **FIFO** and still admits label-setting shortest-path methods.
- The collapse rule is **strict**: both standing at and arriving at node `i` require `time < collapse[i]`; `time == collapse[i]` is invalid.
- Waiting validity must be checked at the current node, not just the destination; a legal edge departure time is useless if the source collapses before that departure.
- In production systems, dynamic routing often stays tractable when time dependence preserves FIFO semantics; once that property breaks, the algorithmic and operational complexity jumps sharply.

## 🚀 Variations & Further Practice
- Add **edge-specific traversal deadlines** or tunnels that must remain open for the full crossing, not just at departure. The twist is that edge validation now depends on the entire interval, not a single departure timestamp.
- Allow **non-FIFO travel functions**, where departing later can arrive earlier. This breaks Dijkstra’s label-setting guarantee and forces more general time-dependent shortest-path techniques.
- Introduce **resource constraints** such as limited waiting budget, fuel, or collapse-delay tokens. The twist is multi-dimensional state, typically requiring expanded-state Dijkstra or dynamic programming over `(node, resource)` pairs.