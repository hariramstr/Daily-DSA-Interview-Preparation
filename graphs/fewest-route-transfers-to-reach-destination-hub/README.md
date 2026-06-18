# Fewest Route Transfers to Reach Destination Hub

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, BFS, Hash Map

---

## 🗂 Problem Overview
Given bus routes as arrays of stop IDs, compute the minimum number of route boardings needed to travel from `source` to `target`. You may board any route containing `source`, and transfer between routes that share at least one stop. Return `0` if `source == target`, otherwise the fewest boarded routes, or `-1` if unreachable. The challenge is scale: total stop entries can reach `10^5`, so naive route-to-route comparison is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere entities are connected indirectly through shared membership: service dependency graphs, package resolution, identity stitching, streaming pipeline lineage, and multimodal routing systems. The core issue is avoiding quadratic connectivity construction when overlap is sparse but the raw input is large. A stop-to-routes index turns implicit relationships into traversable adjacency on demand. Without that indirection, systems burn time materializing edges they never traverse. With it, you get predictable preprocessing, bounded exploration, and a design that scales to large catalogs, topologies, or transport networks without collapsing under pairwise comparisons.

## 🔍 Problem Statement
You are given `routes`, where `routes[i]` lists the distinct stop IDs served by the `i`-th circular bus route. You may board a route at any stop it contains. If two routes share a stop, you may transfer between them there. Starting from `source`, return the minimum number of boarded routes needed to reach `target`. Boarding the first route counts as `1`.

Edge cases:
- If `source == target`, return `0`.
- If no sequence of route transfers reaches a route containing `target`, return `-1`.

Constraints:
- `1 <= routes.length <= 500`
- `1 <= routes[i].length <= 10^4`
- Total stops across all routes `<= 10^5`
- Stop IDs are up to `10^6`

Examples:
- `routes = [[1,5,7],[3,7,9,10],[10,11],[2,4,8]], source = 1, target = 11` → `3`
- `routes = [[2,6],[1,3,5],[7,8,9]], source = 2, target = 5` → `-1`

The algorithmic driver is that total stop volume is large enough that comparing every pair of routes is the wrong model.

## 🪜 How to Solve This
1. Read the problem → the cost is not “number of stops traveled,” it is “number of routes boarded.” That means routes, not stops, are the real graph nodes.

2. Ask how routes connect → two routes are adjacent if they share a stop. But explicitly checking every route pair would be quadratic in route count times route length.

3. Invert the data → build a `stop -> list of routes` map. Now a stop tells you exactly which route transfers are possible there.

4. Identify start and end states → all routes containing `source` are BFS starting nodes; any route containing `target` is a valid finish.

5. Use BFS on routes → each BFS layer represents boarding one more route. That gives the minimum transfer count automatically because BFS finds the shortest path in an unweighted graph.

6. Avoid repeated expansion → mark visited routes, and ideally mark processed stops too, so shared hubs do not cause the same transfer fanout to be scanned repeatedly.

Once you see “minimum hops over overlap-defined groups,” the route-graph + BFS model is the natural fit.

## 🧩 Algorithm Walkthrough
1. **Handle the trivial case.** If `source == target`, return `0`. This is correct because no boarding is required.

2. **Build a stop-to-routes index using a Hash Map.** For every route `i` and every stop in `routes[i]`, append `i` to `stopToRoutes[stop]`. This avoids constructing the full route graph explicitly. The invariant: after preprocessing, every possible transfer induced by a stop can be discovered in O(number of routes at that stop).

3. **Initialize BFS from all source-containing routes.** Look up `stopToRoutes[source]`. Each such route is enqueued with distance `1` because boarding the first route counts. Mark them visited immediately. The invariant: every queued route has a known minimum boarding count.

4. **Run Breadth-First Search over route nodes.** Pop a route. If it contains `target`, return its boarding count. This is correct because BFS explores routes in nondecreasing number of boardings.

5. **Expand neighbors through shared stops.** For each stop on the current route, fetch all routes serving that stop. Any unvisited route becomes reachable with `count + 1`, so enqueue it and mark visited. This is the implicit graph traversal step.

6. **Optionally mark stops as processed.** Once a stop’s route list has been used, do not scan it again. This preserves correctness and prevents repeated fanout through busy interchange stops.

7. **If BFS exhausts, return `-1`.** At that point, every route reachable from the source-side component has been explored, so no valid transfer chain exists.

This is a classic **graph + BFS shortest-path in an unweighted graph** problem, with a **Hash Map inversion** to expose adjacency efficiently.

## 📊 Worked Example
Example: `routes = [[1,5,7],[3,7,9,10],[10,11],[2,4,8]]`, `source = 1`, `target = 11`

| Step | Queue | Visited Routes | Action |
|---|---|---:|---|
| Init | `[(0,1)]` | `{0}` | Route `0` contains source `1` |
| 1 | `[]` | `{0}` | Pop route `0`, not target-containing |
| 1a | `[(1,2)]` | `{0,1}` | From stop `7`, transfer to route `1` |
| 2 | `[]` | `{0,1}` | Pop route `1`, not target-containing |
| 2a | `[(2,3)]` | `{0,1,2}` | From stop `10`, transfer to route `2` |
| 3 | `[]` | `{0,1,2}` | Pop route `2`; it contains `11` |

Trace:
1. Board route `0` at stop `1` → count `1`.
2. Route `0` shares stop `7` with route `1` → count `2`.
3. Route `1` shares stop `10` with route `2` → count `3`.
4. Route `2` contains target `11`, so answer is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(S + E_implicit)` with `S = sum(routes[i].length)`, typically implemented as `O(S)` preprocessing plus BFS that scans each route once and, with stop-level deduplication, each stop list once. At `10^6` or `10^9` scale, the difference between linear-ish traversal and pairwise route comparison is the difference between feasible and dead on arrival.

### Space Complexity
`O(S + R)` where the dominant structure is the `stop -> routes` index, plus visited sets and BFS queue. You can reduce some overhead by mutating or clearing processed stop lists, but that trades memory savings for less reusable state and more implementation coupling.

## 💡 Key Takeaways
- If the problem asks for the fewest transitions between groups connected by shared members, model the groups as graph nodes and use BFS.
- When adjacency is defined by overlap, look for an inverted index (`member -> groups`) instead of materializing all pairwise edges.
- Count boardings correctly: source routes start at distance `1`, not `0`; only the `source == target` case returns `0`.
- Without guarding visited routes and processed stops, high-degree transfer hubs can cause massive redundant work.
- In production systems, explicit graph construction is often the wrong abstraction; indexed, on-demand adjacency is usually the scalable design.

## 🚀 Variations & Further Practice
- Add weighted transfers: different routes or transfer points have costs, which turns unweighted BFS into Dijkstra over the same implicit graph.
- Optimize for stop count and route count simultaneously: now the state includes both current stop and current route, making the graph model more granular.
- Support dynamic route updates: insertions and removals force you to think about incremental indexing and cache invalidation rather than one-shot preprocessing.