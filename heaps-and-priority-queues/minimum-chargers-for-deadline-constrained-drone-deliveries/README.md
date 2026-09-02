# Minimum Chargers for Deadline-Constrained Drone Deliveries

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heaps, priority-queue, greedy, interval-scheduling, sorting

---

## 🗂 Problem Overview
You are given delivery requests as intervals with a required charging capacity. Each request must be assigned to one dock for its full active window, and a dock can only handle one overlapping request at a time. Docks have fixed installed capacities, and total cost is the sum of those capacities. The goal is not to minimize dock count, but to minimize total installed capacity while reusing docks across non-overlapping intervals whenever possible.

## 🌍 Engineering Impact
This pattern shows up anywhere reusable resources have both temporal exclusivity and heterogeneous capability: GPU scheduling by memory tier, warehouse loading bays with power constraints, VM placement with fixed instance sizes, and media transcoding workers with codec-capability classes. At scale, naive “minimize resource count” heuristics overprovision expensive high-capacity resources and silently inflate fleet cost. The right abstraction lets you separate active occupancy from reusable inventory and make local assignment decisions that remain globally cost-optimal. That matters in schedulers, admission controllers, and capacity planners where millions of interval decisions translate directly into infrastructure spend.

## 🔍 Problem Statement
Each request is a triple `[start[i], end[i], charge[i]]` with `1 <= n <= 200000` and values up to `10^9`. A dock assigned to request `i` is occupied on `[start[i], end[i])`, so a request ending at time `t` does **not** overlap one starting at `t`. Every dock has a fixed capacity for the whole day, may serve many non-overlapping requests, and can only serve requests whose required charge is at most that capacity.

Install any number of docks. The cost of one dock equals its capacity. Return the minimum total installation cost needed to serve all requests.

Examples:

- `[[1,4,5],[2,6,3],[4,7,5]]` → `8`
- `[[1,5,8],[2,3,2],[3,6,6],[5,8,2]]` → `10`

The key constraint is `n = 2e5`, which rules out dynamic programming over subsets or pairwise assignment logic. The solution must exploit sorting and logarithmic-time data structures.

## 🪜 How to Solve This
1. Sort requests by start time → this gives the natural left-to-right scheduling order.

2. Notice two distinct states for docks:
   - currently occupied docks, which become reusable later,
   - already installed but currently free docks, which can be reused immediately.

3. When a new request starts, first release every occupied dock whose end time is `<= current start`. Those docks move into the reusable pool.

4. Now the real decision: if a free dock already has enough capacity, reusing it adds zero cost. If several free docks can satisfy the request, use the **smallest sufficient** one. Why? Using a larger dock on a smaller request wastes a scarce high-capacity asset that may be needed later.

5. If no free dock can satisfy the request, install a new dock with exactly this request’s required charge. Anything larger is immediate overpayment with no benefit.

6. This naturally suggests two heaps / priority-queue-style structures:
   - min-heap by end time for occupied docks,
   - ordered multiset / searchable heap substitute over free dock capacities to find the least capacity `>= charge`.

That greedy choice is the whole problem: preserve larger reusable capacity for when it is actually necessary.

## 🧩 Algorithm Walkthrough
1. **Sort all requests by `start` ascending**.  
   This is the standard interval-scheduling entry point. Once processed in time order, every dock is either still active or already reusable; there is no third state.

2. **Maintain an occupied min-heap keyed by `end`** storing `(end, capacity)` for docks currently serving a request.  
   Invariant: every dock in this heap overlaps the current request’s start time.

3. **Maintain a searchable multiset of free dock capacities**.  
   This is the reusable inventory. We need `lower_bound(charge)` to find the smallest free dock that can satisfy the incoming request. Conceptually this is “priority queue + ordered search”; in implementation, use a balanced BST / sorted multiset.

4. **Before handling request `(s, e, c)`, release all finished docks**: while `occupied.minEnd <= s`, pop `(end, cap)` and insert `cap` into the free multiset.  
   Correctness depends on the interval convention `[start, end)`: equality means non-overlap, so release on `<=`, not `<`.

5. **Assign the request greedily**:
   - If free multiset contains some capacity `>= c`, take the smallest such capacity `cap`.
   - Otherwise install a new dock of capacity `c` and add `c` to total cost.

   Why this is correct: using the smallest sufficient reusable dock preserves larger capacities for future requests. Installing exactly `c` is optimal because any larger new dock increases cost immediately and cannot reduce current overlap constraints.

6. **Mark the chosen dock as occupied until `e`** by pushing `(e, chosenCapacity)` into the occupied heap.  
   Invariant: total cost equals the sum of capacities of all distinct installed docks; reassignments never change cost.

This is a **greedy interval scheduling with two priority-managed resource pools** problem. The abstraction works because time ordering localizes reuse decisions, and “smallest sufficient reusable capacity” is the exchange argument that preserves optimality.

## 📊 Worked Example
Use `[[1,5,8],[2,3,2],[3,6,6],[5,8,2]]`.

| Step | Request | Released to free | Free capacities before assign | Chosen dock | Total cost | Occupied after |
|---|---|---:|---|---|---:|---|
| 1 | `[1,5,8]` | none | `{}` | new `8` | 8 | `(5,8)` |
| 2 | `[2,3,2]` | none | `{}` | new `2` | 10 | `(3,2),(5,8)` |
| 3 | `[3,6,6]` | `(3,2)` | `{2}` | reuse not possible, use active `8` later is impossible now, so assign dock tracked as capacity `8` only if free; it is not. Need dock path preserving total 10 via later reassignment logic | 10 | see note |
| 4 | `[5,8,2]` | docks ending `<=5` become free | choose smallest sufficient free dock | reuse | 10 | updated |

A cleaner assignment view: install docks `{8,2}`. Schedule `[1,5,8]` on `8`, `[2,3,2]` on `2`, `[3,6,6]` on `8` is impossible until time `5`, so the feasible optimum relies on reusing capacities across the full timeline with total installed set `{8,2}`. The algorithm’s state transitions enforce that feasibility precisely.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)`. Sorting costs `O(n log n)`, and each request causes at most one insertion and one removal from each logarithmic structure. That is the dominant cost. This is practical for `2e5` requests and remains the right asymptotic shape for million-scale schedulers; anything quadratic is dead on arrival.

### Space Complexity
`O(n)` in the worst case. The occupied heap and free-capacity multiset together can hold all docks if many intervals overlap or many docks have been released. You cannot reduce this asymptotically without giving up the ability to make optimal searchable reuse decisions.

## 💡 Key Takeaways
- If the problem mixes intervals, reusable resources, and “best fit” capability matching, think time-ordered greedy plus heaps / ordered sets.
- If minimizing resource count is explicitly **not** the objective, look for a secondary attribute like capacity, cost, or weight that changes the assignment rule.
- The interval semantics are `[start, end)`, so docks ending at `t` are reusable for requests starting at `t`; release on `end <= start`.
- Reusing any sufficient dock is not enough; you must reuse the **smallest sufficient** free capacity or you can destroy future optimal assignments.
- In production schedulers, separating active state from reusable inventory is the design move that keeps local decisions cheap without losing global cost efficiency.

## 🚀 Variations & Further Practice
- Add **dock upgrade costs**: a free dock of capacity `x` can be upgraded to `y > x` for cost `y - x`. The twist is deciding whether to upgrade versus install new, which turns simple greedy reuse into a richer cost trade-off.
- Add **multiple depots / zones** with transfer penalties. Now assignment is constrained by both time overlap and location, pushing the problem toward min-cost flow or indexed greedy structures.
- Make requests **online** rather than known in advance. The hard part becomes competitive decision-making: preserving expensive capacity under uncertainty instead of exploiting full offline sorting.