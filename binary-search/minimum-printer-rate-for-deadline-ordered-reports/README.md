# Minimum Printer Rate for Deadline-Ordered Reports

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Monotonic Predicate

---

## 🗂 Problem Overview
Given an array `pages`, where `pages[i]` is the size of the `i`-th report, and a deadline `h` in hours, find the smallest integer printer rate `r` such that printing all reports in order finishes within `h` hours. Each report consumes `ceil(pages[i] / r)` whole hours because partial leftover time in an hour cannot be reused. The non-trivial part is scale: page counts are large, the answer space is large, and brute-forcing every possible rate is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must choose the minimum capacity that satisfies a latency or throughput SLO: batch window sizing in data pipelines, minimum worker concurrency for ordered job queues, shard throughput provisioning, or rate selection for media transcoding and document rendering services. The key property is a monotonic feasibility boundary: once a capacity works, any larger capacity also works. Without exploiting that structure, systems either overprovision expensively or rely on linear scans that collapse under large search spaces. Binary search over a monotonic predicate turns sizing from guesswork into a predictable control mechanism.

## 🔍 Problem Statement
You are given:

- `pages`, an array of report sizes
- `h`, the total number of hours available

A printer runs at a constant integer rate `r` pages per hour. It prints reports strictly in the given order, and during any hour it can work on only the current report. If a report finishes before the hour ends, the remaining fraction of that hour is wasted. Therefore, a report of size `x` requires `ceil(x / r)` hours.

Find the minimum integer `r` such that:

- `1 <= pages.length <= 100000`
- `1 <= pages[i] <= 1000000000`
- `pages.length <= h <= 1000000000000`
- the answer is guaranteed to exist

Examples:

- `pages = [300, 200, 400, 100], h = 8` → `150`
- `pages = [30, 11, 23, 4, 20], h = 6` → `23`

The algorithmic driver is the huge rate search space: scanning from `1` to `max(pages)` is not acceptable.

## 🪜 How to Solve This
1. Read the cost model carefully → each report takes `ceil(pages[i] / r)` hours, not fractional time, and leftover time cannot spill into the next report.

2. Translate the question → we are not asked to simulate printing optimally; the order is fixed, so for any chosen rate `r`, total hours are deterministic:
   `sum(ceil(pages[i] / r))`.

3. Ask the key feasibility question → “If the printer runs at rate `r`, can all reports finish within `h` hours?”

4. Notice monotonicity → if rate `r` works, then any rate `r + 1`, `r + 2`, ... also works, because each `ceil(pages[i] / r)` stays the same or decreases.

5. Monotonic feasibility immediately suggests binary search over the answer, not over the array.

6. Set bounds → the minimum possible rate is `1`; the maximum needed is `max(pages)`, because at that rate every report finishes in at most one hour.

7. For each midpoint, compute required hours and shrink the search range toward the first feasible rate.

That chain gets you from raw statement to the right abstraction quickly.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Binary Search on Answer using a Monotonic Predicate.**  
   The predicate is `canFinish(r) := totalHours(r) <= h`. This is monotonic because increasing `r` never increases required hours. That makes the feasible region a suffix of the integer rate domain.

2. **Initialize the search interval.**  
   Set `low = 1` and `high = max(pages)`.  
   Why this is correct: rate `1` is the slowest legal choice, and `max(pages)` is always sufficient since each report then takes exactly one hour, so total time is `pages.length <= h`.

3. **Evaluate a candidate rate `mid`.**  
   Compute  
   `hours = Σ ceil(pages[i] / mid)`  
   using integer arithmetic: `(pages[i] + mid - 1) // mid`.  
   Invariant: `hours` is the exact time required at rate `mid`, with no floating-point error.

4. **Use the predicate to shrink the interval.**  
   - If `hours <= h`, `mid` is feasible, so record it implicitly by moving `high = mid`.  
   - Otherwise, `mid` is too slow, so move `low = mid + 1`.  
   Invariant: the minimum feasible rate always remains inside `[low, high]`.

5. **Terminate when `low == high`.**  
   At convergence, the interval contains exactly one rate, and by the invariant it is the smallest feasible rate.

6. **Guard implementation details.**  
   Use 64-bit accumulation for total hours. With up to `100000` reports and large values, overflow is easy in fixed-width 32-bit types.

This abstraction is right because the expensive part is not searching the array; it is searching a large numeric answer space with a clean monotonic boundary.

## 📊 Worked Example
Use `pages = [30, 11, 23, 4, 20]`, `h = 6`.

| Step | low | high | mid | Hours at mid | Feasible? | Next range |
|---|---:|---:|---:|---:|---|---|
| 1 | 1 | 30 | 15 | 2+1+2+1+2 = 8 | No | `[16, 30]` |
| 2 | 16 | 30 | 23 | 2+1+1+1+1 = 6 | Yes | `[16, 23]` |
| 3 | 16 | 23 | 19 | 2+1+2+1+2 = 8 | No | `[20, 23]` |
| 4 | 20 | 23 | 21 | 2+1+2+1+1 = 7 | No | `[22, 23]` |
| 5 | 22 | 23 | 22 | 2+1+2+1+1 = 7 | No | `[23, 23]` |

Search converges at `23`, so the minimum valid printer rate is `23`. The trace shows the core property: once a candidate is feasible, the search continues left to find the first feasible point.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n = pages.length` and `M = max(pages)`. Each binary-search step scans the array once to compute required hours, and there are `log M` such steps. In practice this scales well: even with `n = 10^6`, the logarithmic factor stays small; a linear scan over a `10^9`-sized answer space would not.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only search bounds, a midpoint, and an accumulated hour count. No extra data structures are required. Space cannot be meaningfully reduced further without changing the execution model; the only trade-off is using wider integer types for safety.

## 💡 Key Takeaways
- If the problem asks for the **minimum integer capacity/rate/threshold** that satisfies a deadline or budget, check whether feasibility is monotonic and binary-search the answer.
- When the input order is fixed and the cost for a candidate answer can be recomputed deterministically, that often signals a monotonic predicate over the answer space rather than a combinatorial search.
- Compute `ceil(x / r)` with integer math as `(x + r - 1) // r`; floating-point division is unnecessary and can introduce avoidable bugs.
- Use `while low < high` with `high = mid` on feasible candidates; using `high = mid - 1` here commonly loses the first valid rate.
- In production sizing problems, the transferable idea is to separate **feasibility evaluation** from **capacity search**; once feasibility is monotonic, provisioning becomes a bounded optimization problem instead of trial-and-error.

## 🚀 Variations & Further Practice
- Allow multiple printers working in parallel, but preserve report order per printer. The twist is that feasibility now depends on scheduling, not just a simple per-item sum.
- Replace the wasted-hour rule with continuous carry-over time. The monotonic predicate still exists, but the cost function changes from `sum(ceil(...))` to a continuous throughput model.
- Add a setup cost or context-switch penalty between reports. This keeps binary search viable if feasibility remains monotonic, but makes the per-rate evaluation more nuanced.