# Minimum Refill Rate for a Timed Irrigation Plan

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, greedy, array

---

## 🗂 Problem Overview
Given an array `water` and an integer `days`, determine the smallest fixed tank capacity that allows watering all fields in order within at most `days` days. Each day starts with a full tank, fields must be watered left to right, and a single field cannot be split across days. The non-trivial constraint is that capacity is not chosen directly from a formula; it must be inferred under ordering and packing constraints, which makes this a binary-search-on-answer problem.

## 🌍 Engineering Impact
This pattern shows up in systems that must choose the minimum feasible resource budget under ordered workload constraints: batch sizing in streaming pipelines, shard compaction windows in storage engines, CI job packing, API gateway rate buckets, and network transfer chunk sizing. At scale, brute-force exploration of capacities or schedules collapses under large input ranges and strict latency budgets. The monotonic feasibility property enables a clean architecture: a cheap validator plus binary search. That separation matters in production because it turns a combinatorial planning problem into a predictable, testable decision loop with bounded runtime and straightforward operational reasoning.

## 🔍 Problem Statement
You are given:

- `water[i]`: liters required by the `i`-th field
- `days`: maximum number of days allowed

The manager waters fields in the given order only. For a chosen tank capacity, each day begins full, and the manager waters as many consecutive fields as possible from left to right. If the next field would exceed the remaining water, watering stops for that day and resumes the next day with a refilled tank. A field must fit entirely within one day.

Return the minimum capacity that completes all fields in at most `days` days.

Constraints:

- `1 <= water.length <= 100000`
- `1 <= water[i] <= 1000000000`
- `1 <= days <= water.length`

Examples:

- `water = [7,2,5,10,8], days = 2` → `18`
- `water = [3,1,4,1,5,9], days = 3` → `9`

The key algorithmic driver is the large search space for capacity values, potentially up to the sum of all entries, combined with a monotonic feasibility condition.

## 🪜 How to Solve This
1. Read the problem → notice we are not free to reorder fields. That immediately rules out many packing optimizations and turns this into partitioning a fixed sequence into at most `days` contiguous groups.

2. Ask what we are optimizing → not the schedule itself, but the minimum capacity that makes some valid schedule possible.

3. Look for monotonicity → if capacity `C` works, then any capacity larger than `C` also works. More water per day cannot make the plan infeasible.

4. Once feasibility is monotonic, think binary search on the answer rather than binary search on indices.

5. Define the search bounds:
   - Lower bound = `max(water)`, because every individual field must fit in one day.
   - Upper bound = `sum(water)`, because one day could water everything.

6. Build a greedy feasibility check for a candidate capacity:
   - Scan left to right.
   - Keep adding fields to the current day until the next one would overflow.
   - Start a new day only when required.

7. Why greedy works → for a fixed capacity, delaying a split as long as possible minimizes the number of days used. If even that needs more than `days`, the capacity is too small.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Binary Search on Answer + Greedy Feasibility Check.**  
   The answer is a numeric capacity, not an index. Feasibility is monotonic: once a capacity is sufficient, all larger capacities are sufficient. That is the exact signal for binary search over a value range.

2. **Set the lower bound to the largest single field requirement.**  
   Any capacity smaller than `max(water)` is impossible because fields cannot be split. This invariant guarantees every candidate in the search range is at least individually valid.

3. **Set the upper bound to the total water required.**  
   `sum(water)` always works by watering everything in one day. This ensures the search interval contains at least one feasible answer.

4. **For a candidate capacity `mid`, run a greedy scan.**  
   Maintain:
   - `usedDays`, initially `1`
   - `currentLoad`, initially `0`  
   For each field:
   - If `currentLoad + water[i] <= mid`, keep it in the current day.
   - Otherwise, start a new day and set `currentLoad = water[i]`.

5. **Why the greedy validator is correct.**  
   For a fixed capacity, packing each day as full as possible minimizes day count. Starting a new day earlier never helps reduce the number of days, because order is fixed and unused capacity cannot be reclaimed later.

6. **Use the validator to shrink the search space.**  
   - If `usedDays <= days`, `mid` is feasible, so search left for a smaller valid capacity.
   - If `usedDays > days`, `mid` is infeasible, so search right.

7. **Terminate when bounds converge.**  
   The converged value is the minimum feasible capacity. The invariant throughout is: all values below the left boundary are known infeasible, and at least one value in the remaining interval is feasible.

## 📊 Worked Example
Example: `water = [7,2,5,10,8]`, `days = 2`

Initial bounds:

- `left = max(water) = 10`
- `right = sum(water) = 32`

| mid | Day grouping under capacity `mid` | usedDays | Feasible? |
|---|---|---:|---|
| 21 | `[7,2,5]`, `[10,8]` | 2 | Yes |
| 15 | `[7,2,5]`, `[10]`, `[8]` | 3 | No |
| 18 | `[7,2,5]`, `[10,8]` | 2 | Yes |
| 16 | `[7,2,5]`, `[10]`, `[8]` | 3 | No |
| 17 | `[7,2,5]`, `[10]`, `[8]` | 3 | No |

Binary search progression:

1. `mid = 21` works → move `right` to `21`
2. `mid = 15` fails → move `left` to `16`
3. `mid = 18` works → move `right` to `18`
4. `mid = 17` fails → move `left` to `18`

Now `left == right == 18`, so the minimum required capacity is `18`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n log S)`, where `n` is `water.length` and `S = sum(water) - max(water) + 1` is the capacity search range. Each binary search step performs one linear feasibility scan. In practice, even with large value ranges, `log S` stays small. This scales cleanly to million-element arrays; at billion-element scale, the linear scan dominates and data movement becomes the real bottleneck.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only running totals, bounds, and counters; no extra arrays or heap structures are required. Space cannot meaningfully be reduced further without changing the execution model. The main trade-off is not memory but repeated passes over the input during binary search.

## 💡 Key Takeaways
- If the problem asks for the **minimum numeric value that satisfies a constraint**, check whether feasibility is monotonic; that is a strong binary-search-on-answer signal.
- If items must remain in original order and be split into contiguous groups, think greedy validation before reaching for DP.
- The lower bound must be `max(water)`, not `0` or `min(water)`; otherwise the validator will consider impossible capacities.
- Be careful with the day counter: start at `1`, not `0`, because the first group already consumes a day.
- The transferable design insight is to separate **optimization** from **feasibility**: a simple monotonic validator often turns expensive planning problems into predictable search loops.

## 🚀 Variations & Further Practice
- **Split Array Largest Sum** — same core structure, but framed as minimizing the largest subarray sum over exactly or at most `k` partitions; the conceptual twist is recognizing it as the same monotonic partitioning problem under different language.
- **Capacity To Ship Packages Within D Days** — identical binary-search-on-capacity pattern, but operationally framed around shipping loads; useful for reinforcing the reusable feasibility-check template.
- **Allocate Minimum Number of Pages** — similar contiguous allocation constraint, but harder in interviews because off-by-one handling around student/page assignment often exposes weak reasoning about feasibility invariants.