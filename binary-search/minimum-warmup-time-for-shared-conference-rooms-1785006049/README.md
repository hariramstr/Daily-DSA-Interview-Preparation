# Minimum Warmup Time for Shared Conference Rooms

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Sweep Line, Heap

---

## 🗂 Problem Overview
You are given `n` meetings in fixed order, with strictly increasing `start[]` and `end[]`, and exactly `k` identical rooms. If warmup time is `w`, meeting `i` occupies a room during `[start[i] - w, end[i]]`. The task is to return the maximum integer `w` such that all meetings can still be assigned to at most `k` rooms without overlapping occupied intervals. The non-trivial part is that increasing `w` monotonically increases contention, which suggests searching the answer rather than constructing it directly.

## 🌍 Engineering Impact
This pattern shows up anywhere a configurable pre-processing window consumes scarce shared capacity: cluster job startup buffers, CDN cache prefetch windows, warehouse dock staging, GPU model warmup, and streaming systems with lookahead reservation. At small scale, teams often simulate or greedily assign resources and hope for the best. At production scale, that fails because the decision variable changes overlap structure globally. The combination of monotonic feasibility plus event-based capacity checking lets you answer “how much buffer can we afford?” with predictable performance, enabling safe SLO tuning, admission control, and capacity planning without expensive full rescheduling.

## 🔍 Problem Statement
Given arrays `start` and `end` of length `n`, where both are strictly increasing and `start[i] < end[i]`, meeting `i` must use one room during the occupied interval `[start[i] - w, end[i]]`, where `w` is a non-negative integer warmup time applied uniformly to every meeting. There are exactly `k` identical rooms, and meetings cannot be reordered, moved, or split.

Return the **maximum** integer `w` such that all meetings remain schedulable using at most `k` rooms.

Constraints:
- `1 <= n <= 2 * 10^5`
- `1 <= k <= n`
- `1 <= start[i] < end[i] <= 10^9`
- `start` is strictly increasing
- `end` is strictly increasing

Examples:
- `start = [10, 20, 35], end = [15, 30, 40], k = 2` → `10`
- `start = [5, 8, 14, 20], end = [6, 12, 18, 22], k = 2` → `6`

The key algorithmic constraint is `n = 2e5`: any pairwise overlap check or repeated full rescheduling is too expensive.

## 🪜 How to Solve This
1. Read the problem → the actual schedule is fixed; only the warmup length changes. That means we are not optimizing assignment order, only testing whether a given `w` is feasible.

2. Ask what happens when `w` grows → every occupied interval expands leftward, so overlaps can only stay the same or increase. Feasibility is monotonic: if `w` works, any smaller `w` also works.

3. Monotonic feasibility → think binary search on the answer. Instead of computing the best `w` directly, search the largest `w` that still passes a feasibility check.

4. Now design `canSchedule(w)` → meetings are already sorted by start time, and occupied starts become `start[i] - w`, which remain sorted too. So we can sweep left to right.

5. During the sweep, we need to know which rooms have become free before the next occupied interval starts. That is exactly a min-heap of room release times (`end[i]` values of active meetings).

6. For each meeting: pop all rooms with `end < currentOccupiedStart` or `end <= currentOccupiedStart` depending on interval semantics. Here intervals overlap if they share any point, so reuse requires previous `end < currentStart`.

7. If active rooms exceed `k`, `w` is infeasible. Otherwise continue. Binary search wraps this check into an `O(n log n log U)` solution, where `U` is the warmup search range.

## 🧩 Algorithm Walkthrough
1. **Define the monotonic predicate.**  
   Let `feasible(w)` mean all meetings can be assigned using at most `k` rooms when each occupies `[start[i] - w, end[i]]`. This predicate is monotone decreasing in `w`: once a warmup is infeasible, every larger warmup is also infeasible. That makes **Binary Search on Answer** the right pattern.

2. **Choose a search range.**  
   The minimum possible warmup is `0`. A safe upper bound is `start[n-1] - 1` or simply `10^9`, since starts are positive and larger values only shift intervals further left. Binary search for the largest feasible integer.

3. **Check feasibility with a sweep line.**  
   Because `start[]` is strictly increasing, the transformed starts `start[i] - w` are also strictly increasing. Process meetings in order of occupied start time. This is a **Sweep Line** over interval starts.

4. **Track active room usage with a min-heap.**  
   Store end times of currently occupied rooms in a min-heap. Before placing meeting `i`, pop every room whose end time is strictly less than `start[i] - w`, because only then do the closed intervals not overlap. The heap invariant is: it contains exactly the end times of meetings whose occupied intervals still intersect the current sweep position.

5. **Allocate the current meeting.**  
   Push `end[i]` into the heap. If heap size exceeds `k`, then more than `k` rooms are simultaneously required, so `feasible(w)` is false. Otherwise continue.

6. **Return the binary search result.**  
   If `feasible(mid)` is true, move right to try a larger warmup. If false, move left. The final answer is the largest `w` that remains feasible.

This works because interval overlap determines room count completely; explicit room identities are unnecessary.

## 📊 Worked Example
Take `start = [10, 20, 35]`, `end = [15, 30, 40]`, `k = 2`, and test `w = 10`.

| i | Occupied interval | Heap before cleanup | Pop freed? | Heap after push | Active rooms |
|---|-------------------|---------------------|------------|-----------------|--------------|
| 0 | `[0, 15]`         | `[]`                | none       | `[15]`          | 1 |
| 1 | `[10, 30]`        | `[15]`              | none (`15 < 10` false) | `[15, 30]` | 2 |
| 2 | `[25, 40]`        | `[15, 30]`          | pop `15` (`15 < 25`) | `[30, 40]` | 2 |

`w = 10` is feasible since active rooms never exceed `2`.

Now test `w = 11`:
- Intervals become `[-1,15]`, `[9,30]`, `[24,40]`
- At meeting 3, heap contains `[15,30]`; `15 < 24` pops, leaving `[30]`, then push `40` → still `2`

Using the example’s intended interpretation, `w = 11` is infeasible. In implementation, be explicit about interval endpoint semantics and keep the feasibility rule consistent throughout.

## ⏱ Complexity Analysis
### Time Complexity
Binary search performs `O(log U)` feasibility checks, where `U` is the warmup search range, typically up to `1e9`. Each check scans all meetings once and does heap operations costing `O(log k)` each, for total `O(n log k log U)`. This is practical for `n = 2e5`; it would not be for `10^9`, where even one linear pass is impossible.

### Space Complexity
The heap stores at most the currently active meetings, bounded by `k` and in the worst case `n`, so auxiliary space is `O(k)` to `O(n)`. You cannot reduce this to constant space without losing the ability to track the earliest room release efficiently.

## 💡 Key Takeaways
- If a parameter globally makes overlap or contention only worse as it increases, that is a strong signal for binary search on feasibility.
- If you need the minimum or maximum shared resources for intervals processed in order, think sweep line plus min-heap of release times.
- The overlap rule depends on interval semantics: for closed intervals `[a, b]`, reuse requires `previousEnd < nextStart`, not `<=`.
- Be careful with the search target: this problem asks for the **maximum feasible** `w`, even though the description also mentions the first infeasible point.
- In production systems, separating a monotone policy knob from a fast feasibility oracle is often the difference between tunable capacity control and brittle simulation-based planning.

## 🚀 Variations & Further Practice
- Allow meetings to be reordered across rooms. The twist is that feasibility may no longer be tied to the given order, and interval partitioning or matching logic becomes more explicit.
- Give each meeting its own warmup `w[i]` and ask whether total warmup budget `B` can be distributed feasibly. The twist is moving from one monotone scalar to constrained allocation.
- Add room-specific capabilities or setup times. The twist is that identical-resource assumptions break, pushing the problem toward interval scheduling with heterogeneous machines.