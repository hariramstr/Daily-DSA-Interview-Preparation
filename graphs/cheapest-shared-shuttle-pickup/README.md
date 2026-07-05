# Cheapest Shared Shuttle Pickup

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, Dijkstra, Shortest Path

---

## 🗂 Problem Overview
Given a directed weighted graph, choose a meeting building `m` where Alice and Bob can arrive independently, then travel together from `m` to the office. The goal is to minimize `dist(aliceStart, m) + dist(bobStart, m) + dist(m, office)`. Return that minimum total cost, or `-1` if no such meeting point exists. The challenge is scale: up to `10^5` nodes and `2 * 10^5` edges rules out per-node path recomputation.

## 🌍 Engineering Impact
This pattern shows up anywhere multiple independent flows converge before a shared downstream path: ride-pooling dispatch, network traffic aggregation, distributed job staging, warehouse routing, and multi-source replication pipelines. At scale, the wrong approach recomputes shortest paths from every candidate merge point and collapses under graph size or latency budgets. The right abstraction turns a combinatorial search over meeting points into a small number of graph passes. That matters in production because it enables predictable latency, supports sparse but large topologies, and cleanly separates upstream reachability from downstream shared-cost evaluation.

## 🔍 Problem Statement
You are given `n` buildings labeled `0..n-1` and directed weighted edges `lanes[i] = [u, v, cost]`, meaning travel is allowed only from `u` to `v` with positive cost `cost`. Alice starts at `aliceStart`, Bob at `bobStart`, and both want to end at `office`. They may meet at any building `m`, then share the route from `m` to `office`, paying that suffix cost once.

Compute:

`dist(aliceStart, m) + dist(bobStart, m) + dist(m, office)`

over all valid meeting points `m`. A valid `m` must be reachable from both starts and must itself be able to reach `office`. Return the minimum total, or `-1` if none exists.

Constraints: `n <= 100000`, `lanes.length <= 200000`, edge costs up to `10^9`, graph not strongly connected, and multiple edges may exist.

Examples:
- `n=6`, `lanes=[[0,2,2],[1,2,4],[2,3,3],[2,4,1],[4,3,1],[3,5,2],[4,5,5]]`, `aliceStart=0`, `bobStart=1`, `office=5` → `11`
- `n=5`, `lanes=[[0,1,3],[1,4,4],[2,3,2]]`, `aliceStart=0`, `bobStart=2`, `office=4` → `-1`

## 🪜 How to Solve This
1. Read the objective carefully → the meeting point is unknown, but the formula splits cleanly into three shortest-path terms.
2. Notice what varies with `m`:
   - cost from Alice to `m`
   - cost from Bob to `m`
   - cost from `m` to office
3. That suggests precomputing distances instead of evaluating each meeting point independently.
4. Run shortest path from `aliceStart` on the original graph → now you know `distA[m]` for every building.
5. Run shortest path from `bobStart` on the original graph → now you know `distB[m]`.
6. The tricky part is `dist(m, office)` for all `m`. Running Dijkstra from every `m` is too expensive.
7. Reverse every edge and run Dijkstra once from `office` on the reversed graph. That gives `distToOffice[m]`, which equals the original cost from `m` to `office`.
8. Scan all buildings and minimize `distA[m] + distB[m] + distToOffice[m]` where all three distances are finite.
9. Positive edge weights point directly to Dijkstra, and the “all nodes to one destination” requirement is the signal to use graph reversal.

## 🧩 Algorithm Walkthrough
1. **Build two adjacency lists**: one for the original graph and one for the reversed graph.  
   This supports forward shortest paths from Alice and Bob, and reverse shortest paths into the office. The invariant is that every original edge `(u -> v, w)` also appears as `(v -> u, w)` in the reversed graph.

2. **Run Dijkstra from `aliceStart` on the original graph**.  
   This computes the minimum independent cost for Alice to reach every node. Because all edge weights are positive, once a node is popped with minimum tentative distance, that distance is final.

3. **Run Dijkstra from `bobStart` on the original graph**.  
   Same reasoning, now for Bob. After this step, for every node `m`, `distA[m]` and `distB[m]` represent the cheapest ways for each employee to arrive at `m` independently.

4. **Run Dijkstra from `office` on the reversed graph**.  
   This is the key graph pattern: **multi-query shortest path to a fixed destination via edge reversal**. The result `distRev[m]` equals the shortest path from `m` to `office` in the original graph.

5. **Enumerate every building as a candidate meeting point**.  
   For each `m`, check whether all three distances are finite. If so, compute `distA[m] + distB[m] + distRev[m]` and keep the minimum. The invariant is simple: after processing node `i`, the current answer is the best valid meeting point among `0..i`.

6. **Return the minimum or `-1`**.  
   If no node satisfies all reachability conditions, there is no feasible shared pickup building.

This is the right abstraction because the problem is not “find one shortest path,” but “score every node using three shortest-path projections.”

## 📊 Worked Example
Use Example 1:

`n=6`  
`lanes=[[0,2,2],[1,2,4],[2,3,3],[2,4,1],[4,3,1],[3,5,2],[4,5,5]]`  
`aliceStart=0, bobStart=1, office=5`

| Node `m` | `distA[m]` | `distB[m]` | `distToOffice[m]` | Total |
|---|---:|---:|---:|---:|
| 0 | 0 | ∞ | 7 | invalid |
| 1 | ∞ | 0 | 9 | invalid |
| 2 | 2 | 4 | 5 | 11 |
| 3 | 5 | 7 | 2 | 14 |
| 4 | 3 | 5 | 3 | 11 |
| 5 | 7 | 9 | 0 | 16 |

Trace:
1. Dijkstra from `0` gives shortest costs from Alice.
2. Dijkstra from `1` gives shortest costs from Bob.
3. Reverse graph, run from `5`, giving shortest cost from every node to office.
4. Scan all nodes. Valid meeting points are `2, 3, 4, 5`.
5. Minimum total is `11`, achieved at `m=2` or `m=4`.

## ⏱ Complexity Analysis
### Time Complexity
Building adjacency lists is `O(E)`. Each Dijkstra run is `O((V + E) log V)` with a binary heap, and we run it three times, so total complexity is `O((V + E) log V)`. At `10^6` scale this is practical; at `10^9`, even linear graph scans are already beyond single-machine memory and latency budgets.

### Space Complexity
`O(V + E)` for the original graph, reversed graph, distance arrays, and heap state. The adjacency lists dominate. You cannot meaningfully reduce this without trading away the reversed graph and paying with much worse recomputation cost.

## 💡 Key Takeaways
- If the objective is `dist(source1, m) + dist(source2, m) + dist(m, target)`, that is a strong signal to precompute distance arrays and scan all `m`.
- If many candidate nodes need distance **to one fixed destination** in a directed graph, reverse the edges and run one shortest-path pass from the destination.
- Use 64-bit integers for distances and the final answer; edge costs can reach `10^9`, so path sums easily overflow 32-bit types.
- Do not treat unreachable nodes as large finite sentinels during addition unless you guard first; combining “infinite” values can silently corrupt the minimum.
- The production lesson is to convert repeated per-candidate path queries into a small number of reusable graph projections; that is the difference between scalable planning and accidental quadratic behavior.

## 🚀 Variations & Further Practice
- Allow negative edge weights but no negative cycles. The conceptual twist is replacing Dijkstra with Bellman-Ford or Johnson’s algorithm, and re-evaluating feasibility at scale.
- Generalize from two employees to `k` employees meeting before a shared suffix. The harder part is combining `k` independent source distances efficiently without exploding memory or query cost.
- Add a constraint that the meeting point must satisfy capacity, time-window, or forbidden-node rules. The shortest-path core remains, but feasibility filtering becomes a first-class part of the optimization.