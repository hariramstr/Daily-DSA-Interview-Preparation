# Minimum Delay to Stream K Live Feeds

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** Heaps, Priority Queue, Dijkstra, Graph Shortest Path

---

## 🗂 Problem Overview
Given a large undirected weighted graph, compute the minimum delay needed to stream from server `0` to at least `k` viewer gateways. The delay of a plan is the largest shortest-path distance among the chosen gateways. So the task is: find shortest distances from `0`, keep only gateway distances, and return the `k`-th smallest reachable one. If fewer than `k` gateways are reachable, return `-1`. The scale rules out repeated searches or all-pairs methods.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must satisfy a quorum under latency constraints: CDN relay selection, multi-region replication, fan-out notification systems, edge inference rollout, and service-mesh failover routing. The operational question is not “reach everyone” but “what latency budget guarantees enough endpoints are online?” Without shortest-path plus top-`k` selection, teams either over-provision, accept unpredictable tail latency, or run expensive per-target searches that collapse under sparse graphs with hundreds of thousands of nodes. The right approach gives deterministic latency envelopes, supports admission decisions, and scales with topology growth.

## 🔍 Problem Statement
You are given `n` servers labeled `0` to `n - 1`, an undirected weighted edge list `edges`, a list of distinct `gateways`, and an integer `k`. Each edge `[u, v, w]` means stream traffic can move between `u` and `v` with delay `w`. Server `0` is the source.

For every gateway, consider its shortest-path delay from `0`. You may choose any subset of gateways of size at least `k`. The cost of that choice is the maximum delay among chosen gateways. Return the minimum possible cost.

Equivalent formulation: among all reachable gateways, sort their shortest distances and return the `k`-th smallest. If fewer than `k` gateways are reachable, return `-1`.

Constraints are large: `n <= 200000`, `m <= 300000`, and weights up to `10^9`, which strongly points to heap-based Dijkstra on a sparse graph.

Examples:
- `n=6, edges=[[0,1,4],[1,2,3],[0,3,2],[3,4,5],[4,5,1],[2,5,2]], gateways=[2,4,5], k=2` → `7`
- `n=5, edges=[[0,1,2],[1,2,2],[3,4,1]], gateways=[2,3,4], k=2` → `-1`

## 🪜 How to Solve This
1. Read the objective carefully → the cost is the **maximum** delay among chosen gateways, but you can choose **any** `k` gateways. That means you want the `k` gateways with the smallest shortest-path distances.

2. So the real problem becomes two subproblems:
   - compute shortest distance from `0` to every node
   - extract the `k`-th smallest distance among gateways that are reachable

3. Weighted graph with positive edge weights and large sparse constraints → this is classic **Dijkstra with a min-heap**, not BFS and not Floyd–Warshall.

4. Build an adjacency list once. Run Dijkstra from node `0` to get `dist[]`.

5. Scan `gateways`:
   - ignore unreachable nodes
   - count reachable ones
   - collect their distances

6. If reachable count `< k`, return `-1`.

7. Otherwise, the answer is the `k`-th smallest gateway distance. You can sort gateway distances or maintain a size-`k` max-heap while scanning. Since gateway count is at most `n`, either works; sorting is simpler, bounded, and usually fine here.

## 🧩 Algorithm Walkthrough
1. **Build the graph using an adjacency list.**  
   Store each undirected edge twice: `u -> (v, w)` and `v -> (u, w)`. This is the right representation for sparse graphs because space is linear in `n + m`, and neighbor traversal is efficient.

2. **Run Dijkstra’s algorithm from source `0` using a min-heap.**  
   Pattern: **Shortest Path on a sparse weighted graph with a Priority Queue**.  
   Initialize `dist[0] = 0`, all others to infinity, and push `(0, 0)` into the heap.

3. **Pop the smallest tentative distance each time.**  
   If the popped distance is stale (`d != dist[node]`), skip it.  
   Invariant: whenever a node is processed with its current best distance, that distance is final because all edge weights are positive.

4. **Relax outgoing edges.**  
   For each neighbor, compute `newDist = d + w`. If `newDist < dist[neighbor]`, update `dist[neighbor]` and push the new pair into the heap.  
   Invariant: `dist[x]` is always the best distance discovered so far.

5. **Extract gateway distances.**  
   Scan the `gateways` list and keep only nodes with finite distance. If fewer than `k` are reachable, return `-1`. This handles disconnected components cleanly.

6. **Return the `k`-th smallest reachable gateway distance.**  
   Why this is correct: choosing more than `k` gateways cannot reduce the maximum chosen delay, so an optimal plan always corresponds to the `k` smallest reachable gateway distances. The answer is therefore exactly the `k`-th order statistic among reachable gateways.

## 📊 Worked Example
Example: `n=6`, `gateways=[2,4,5]`, `k=2`

| Step | Heap Pop | Distance Updates | dist[2] | dist[4] | dist[5] |
|---|---|---|---:|---:|---:|
| Init | — | `dist[0]=0` | inf | inf | inf |
| 1 | `(0,0)` | `1=4`, `3=2` | inf | inf | inf |
| 2 | `(2,3)` | `4=7` | inf | 7 | inf |
| 3 | `(4,1)` | `2=7` | 7 | 7 | inf |
| 4 | `(7,2)` | `5=9` | 7 | 7 | 9 |
| 5 | `(7,4)` | `5=8` improves | 7 | 7 | 8 |
| 6 | `(8,5)` | no improvement | 7 | 7 | 8 |

Reachable gateway delays are `[7, 7, 8]`.  
Sort them → `[7, 7, 8]`.  
The `2`-nd smallest is `7`, so the minimum feasible maximum delay is `7`.

## ⏱ Complexity Analysis
### Time Complexity
Building the adjacency list is `O(m)`. Dijkstra with a binary heap is `O((n + m) log n)`, which dominates. Scanning gateways is `O(g)`, and sorting reachable gateway distances is `O(r log r)` where `r <= g`. At million-edge scale this remains practical; at billion-edge scale it does not fit typical single-node memory or latency budgets.

### Space Complexity
`O(n + m)` for the adjacency list, distance array, and priority queue. The graph representation owns most of the space. Gateway post-processing adds `O(g)` if you collect distances for sorting; that can be reduced to `O(k)` with a size-`k` heap at the cost of slightly more implementation complexity.

## 💡 Key Takeaways
• If you see a sparse weighted graph, positive edge weights, and “minimum latency from one source,” think Dijkstra immediately.  
• If the objective says “choose any `k` targets minimizing the worst chosen value,” translate it to an order statistic: the answer is often the `k`-th smallest feasible metric.  
• Do not confuse graph reachability with gateway feasibility: disconnected gateways must be excluded before selecting the `k`-th value.  
• Use 64-bit integers for distances; path sums can exceed 32-bit range when weights are up to `10^9`.  
• In production routing systems, shortest-path computation and policy selection are often separate phases; keeping that boundary clean makes optimization and observability much easier.

## 🚀 Variations & Further Practice
- Require the stream to reach `k` gateways through a shared delivery tree minimizing the maximum edge-to-gateway delay. Twist: this becomes a network design problem, not just independent shortest paths.
- Add per-gateway demand weights and ask for minimum delay needed to cover total demand at least `D`. Twist: selection changes from `k`-th smallest count to thresholding cumulative capacity.
- Support many online queries with changing `k` or changing gateway sets. Twist: shortest paths may be reusable, but selection needs preprocessing or dynamic data structures.