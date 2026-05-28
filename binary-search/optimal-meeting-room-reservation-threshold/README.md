# Optimal Meeting Room Reservation Threshold

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Interval Scheduling, Sorting

---

## 🗂 Problem Overview

Given `n` meeting rooms and a list of meetings with start, end, and priority values, find the minimum priority threshold `T` such that scheduling only meetings with `priority >= T` never requires more than `n` simultaneous rooms. The non-trivial constraint is the interaction between the continuous integer priority space and the interval overlap check — neither dimension is independently tractable without combining binary search over thresholds with an efficient overlap feasibility test.

---

## 🌍 Engineering Impact

This pattern appears directly in admission-control systems: API gateways that must shed low-priority traffic under load, Kubernetes schedulers deciding which pods to evict, and cloud resource brokers managing reserved vs. on-demand capacity. The threshold-search-over-feasibility-check structure also appears in distributed rate limiters (find the minimum rate cap that keeps downstream SLAs intact) and search ranking pipelines (find the minimum relevance score that keeps result-set size within latency budgets). Without a principled binary search over the threshold space, naive linear scans degrade to O(P · M log M) where P is the priority range — unacceptable at 10⁶ priorities and 10⁵ meetings.

---

## 🔍 Problem Statement

**Input:** An integer `n` (room count) and an array `meetings` where `meetings[i] = [start_i, end_i, priority_i]`. Intervals are half-open: `[start_i, end_i)`.

**Output:** The minimum integer priority threshold `T` such that the subset of meetings with `priority >= T` can be scheduled across `n` rooms with no time conflict. Return `-1` if even all meetings (threshold = 1) exceed capacity. Return `1` if no meetings exist or all meetings trivially fit.

**Constraints:**
- `1 <= n <= 100`
- `1 <= meetings.length <= 10^5`
- `0 <= start_i < end_i <= 10^9`
- `1 <= priority_i <= 10^6`

**Key driver:** Priority values span up to 10⁶ distinct integers. A linear scan over all possible thresholds is O(10⁶ · M log M) — the monotonic feasibility property of the threshold space is what makes binary search applicable and necessary.

**Example 1:**
```
n = 2, meetings = [[1,5,3],[2,6,5],[4,8,2],[7,10,1]]
Output: 3
```

**Example 2:**
```
n = 3, meetings = [[1,4,2],[2,5,2],[3,6,2],[4,7,2]]
Output: 1  (all meetings fit; max simultaneous overlap = 3 = n)
```

---

## 🪜 How to Solve This

1. **Observe monotonicity** → As threshold `T` increases, fewer meetings qualify. Fewer meetings means overlap can only decrease or stay the same. This is a monotone predicate: if threshold `T` is feasible, then `T+1` is also feasible. Monotone predicate over an ordered domain = binary search.

2. **Define the search space** → The only thresholds that matter are the distinct priority values present in the input. Binary search over the sorted unique priority array, not over `[1, 10^6]` — this bounds the search to at most `log(M)` iterations.

3. **Define the feasibility check** → For a given `T`, filter meetings with `priority >= T`, then determine whether they can fit in `n` rooms. This is the classic "minimum rooms required" problem: use a sweep-line or sort events by time and track concurrent meeting count.

4. **Combine** → Binary search calls the feasibility check at most `O(log M)` times. Each check is `O(M log M)` for sorting. Total: `O(M log² M)` — well within budget.

5. **Handle edge cases first** → Check feasibility at `T=1` (all meetings). If it fails, return `-1`. If the meeting list is empty, return `1`.

---

## 🧩 Algorithm Walkthrough

**Pattern:** Binary Search on Answer + Sweep-Line Feasibility Check

1. **Extract and deduplicate priorities.** Collect all unique `priority_i` values, sort them ascending. This is your binary search domain. Searching only distinct priorities avoids redundant feasibility checks.

2. **Binary search over sorted priorities.** Maintain `lo = 0`, `hi = len(unique_priorities) - 1`. At each midpoint `mid`, evaluate `feasible(unique_priorities[mid])`. If feasible, record this as a candidate answer and search lower (`hi = mid - 1`) to find the minimum. If not feasible, search higher (`lo = mid + 1`).

3. **Feasibility check via sweep line.** For threshold `T`: filter meetings where `priority >= T`. For each meeting, emit two events: `(start, +1)` and `(end, -1)`. Sort all events by time, breaking ties by putting end events before start events (since intervals are exclusive — a room freed at time `t` is available for a meeting starting at `t`). Sweep through events, maintaining a running count. If the count ever exceeds `n`, return `false`.

4. **Invariant maintained:** At every binary search step, all thresholds above `hi` are known feasible, all below `lo` are known infeasible. The answer is the minimum feasible threshold seen.

5. **Return the minimum feasible threshold found,** or `-1` if even threshold = `min(all priorities)` is infeasible (equivalently, all meetings together exceed capacity).

---

## 📊 Worked Example

**Input:** `n = 2`, `meetings = [[1,5,3],[2,6,5],[4,8,2],[7,10,1]]`

Unique sorted priorities: `[1, 2, 3, 5]`

| Iteration | lo | hi | mid | T  | Qualifying meetings       | Max overlap | Feasible? | Candidate |
|-----------|----|----|-----|----|---------------------------|-------------|-----------|-----------|
| 1         | 0  | 3  | 1   | 2  | [1,5,3],[2,6,5],[4,8,2]   | 3 at [4,5)  | ❌        | —         |
| 2         | 2  | 3  | 2   | 3  | [1,5,3],[2,6,5]           | 2 at [2,5)  | ✅        | 3         |
| 3         | 2  | 1  | —   | —  | search ends               | —           | —         | —         |

**Output:** `3`

Sweep-line detail for `T=3`: Events sorted: `(1,+1),(2,+1),(5,-1),(5,-1),(6,-1)`. Running count peaks at 2, which equals `n=2`. Feasible.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(M log² M)** where `M = meetings.length`. Binary search runs `O(log M)` iterations over distinct priorities (at most `M` unique values). Each feasibility check sorts up to `M` events: `O(M log M)`. At `M = 10^5`, this is roughly `10^5 × 17 × 17 ≈ 2.9 × 10^7` operations — comfortably within a 1-second budget.

### Space Complexity

**O(M)** for the event list generated during each feasibility check and the sorted priority array. The event list is rebuilt each iteration but not retained, so peak auxiliary space is `O(M)`. No meaningful reduction is possible without sacrificing the sort-based sweep, which would require a more complex data structure at equal or higher constant cost.

---

## 💡 Key Takeaways

- **Pattern signal — monotone predicate:** Whenever a threshold or cutoff parameter has the property that feasibility is monotone (raising it can only help, never hurt), binary search on that parameter is the right instinct — even if the feasibility check itself is expensive.
- **Pattern signal — "minimum X such that condition holds":** This phrasing is the canonical indicator for binary-search-on-answer. The answer space doesn't need to be integers; it needs to be ordered with a monotone predicate.
- **Gotcha — tie-breaking in sweep events:** End events must be processed before start events at the same timestamp because intervals are half-open `[start, end)`. Getting this wrong inflates the overlap count by 1 and produces incorrect feasibility results for tight cases where `max_overlap == n`.
- **Gotcha — search over distinct priorities, not `[1, 10^6]`:** Binary searching the full priority range adds no correctness benefit and forces the feasibility check to handle thresholds with identical qualifying sets repeatedly. Always compress the search space to distinct input values.
- **Architectural insight:** The separation of *search* (binary search over threshold) and *validation* (sweep-line feasibility) is a reusable design pattern for admission-control and capacity-planning systems — encode the policy as a monotone predicate, then binary search over the policy parameter space rather than simulating every possible configuration.

---

## 🚀 Variations & Further Practice

- **Weighted room capacity:** Each room has a maximum concurrent load (not just count), and each meeting has a resource demand. The feasibility check must now solve a bin-packing variant per time slice — NP-hard in general, but tractable with the constraint that `n <= 100` via greedy first-fit. The binary search shell remains identical; only the inner check changes.
- **Dynamic meeting arrivals (online variant):** Meetings arrive in a stream and the threshold must be maintained incrementally. The challenge is that re-running a full sweep-line on each arrival is O(M log M) per event. This pushes toward a segment tree or interval tree that supports online max-overlap queries, turning the feasibility check from a batch operation into an incremental one.
- **Multi-resource scheduling (LeetCode 2402 — Meeting Rooms III):** Rooms have identity and meetings must be assigned to specific rooms with overflow rules. The overlap feasibility check is no longer sufficient; you need a priority queue tracking room release times, which changes the inner algorithm's structure while the binary-search-on-threshold framing still applies if you add a priority filter layer.