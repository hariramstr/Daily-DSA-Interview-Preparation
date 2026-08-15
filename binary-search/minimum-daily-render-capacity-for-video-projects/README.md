# Minimum Daily Render Capacity for Video Projects

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Greedy

---

## 🗂 Problem Overview
Given an ordered list of project render costs `frames` and a deadline of `d` days, find the smallest daily capacity `C` that lets all projects finish within `d` contiguous day-batches. Each project must stay intact, order cannot change, and each day gets a consecutive slice of the array whose sum is at most `C`. The non-trivial part is that feasibility depends on both capacity and ordering, so brute-forcing all partitions is too expensive at scale.

## 🌍 Engineering Impact
This pattern shows up anywhere work must be processed in order under bounded per-window capacity: render farms, batch ETL pipelines, log compaction, media transcoding queues, CI job packing, and network egress shaping. The key production concern is not just throughput, but minimum safe provisioning under sequencing constraints. Without a monotonic-feasibility approach, teams either overprovision capacity or rely on expensive search over partition plans. Binary search over the answer enables predictable sizing decisions, fast admission checks, and capacity planning that remains viable when job counts reach hundreds of thousands and individual workloads are highly skewed.

## 🔍 Problem Statement
You are given an integer array `frames` where `frames[i]` is the frame-unit cost of the `i`th video project, and an integer `d` representing the maximum number of days available. Projects must be rendered in the given order. A single day may process only a contiguous group of projects, and the total frame-units assigned to that day cannot exceed daily capacity `C`.

Return the minimum `C` such that all projects can be completed in at most `d` days.

Constraints:
- `1 <= frames.length <= 100000`
- `1 <= frames[i] <= 1000000000`
- `1 <= d <= frames.length`
- Answer fits in 64-bit signed integer

Examples:
- `frames = [30, 10, 20, 40, 25], d = 3` → `65`
- `frames = [8, 15, 7, 12, 10], d = 2` → `30`

The algorithmic driver is the input size: `n` can be `100000`, so enumerating partitions is not acceptable.

## 🪜 How to Solve This
1. Read the constraints → partition enumeration is dead on arrival. Even dynamic programming over all split points is too expensive for `n = 100000`.

2. Notice what the question really asks: not “how many ways can we split,” but “what is the smallest capacity that is feasible.”

3. Feasibility for a fixed capacity is easy to test greedily:
   - scan left to right,
   - keep adding projects to the current day,
   - when the next project would exceed capacity, start a new day.

4. That greedy simulation works because order is fixed and splitting earlier never helps reduce day count for the same capacity.

5. Now observe the monotonic property:
   - if capacity `C` works, any larger capacity also works;
   - if capacity `C` fails, any smaller capacity also fails.

6. Monotonic feasibility + “minimum valid value” should immediately suggest **binary search on the answer**.

7. Set bounds carefully:
   - lower bound = largest single project,
   - upper bound = sum of all projects.

8. Binary search that range, using the greedy check as the predicate, until the smallest feasible capacity remains.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Binary Search on Answer + Greedy Feasibility Check.**  
   We are not searching an index in sorted data; we are searching the minimum capacity in a numeric range where feasibility is monotonic.

2. **Establish the search interval.**  
   The minimum possible capacity is `max(frames)` because no project can be split. The maximum possible capacity is `sum(frames)` because one day could process everything. This guarantees the true answer lies within `[lo, hi]`.

3. **Define the feasibility predicate `canFinish(C)`.**  
   Scan `frames` in order, accumulating the current day’s total. If adding the next project exceeds `C`, increment the day count and start a new day with that project.  
   Invariant: after processing each prefix, we have used the fewest possible days for capacity `C`.

4. **Why the greedy check is correct.**  
   For a fixed capacity, packing each day as full as possible never increases the number of days. Starting a new day earlier only leaves unused capacity behind and cannot improve feasibility under fixed ordering.

5. **Use binary search over capacity.**  
   Compute `mid`. If `canFinish(mid)` uses at most `d` days, `mid` is feasible, so try smaller values by setting `hi = mid`. Otherwise, increase capacity with `lo = mid + 1`.

6. **Termination and result.**  
   When `lo == hi`, the interval has collapsed to the smallest feasible capacity. That value is the answer.

This abstraction is right because the expensive combinatorial space of partitions is replaced by repeated linear checks over a monotonic decision boundary.

## 📊 Worked Example
Example: `frames = [8, 15, 7, 12, 10]`, `d = 2`

Bounds:
- `lo = max(frames) = 15`
- `hi = sum(frames) = 52`

| mid | Greedy day split under `mid` | Days used | Feasible? |
|---|---|---:|---|
| 33 | `[8,15,7]`, `[12,10]` | 2 | Yes |
| 24 | `[8,15]`, `[7,12]`, `[10]` | 3 | No |
| 29 | `[8,15]`, `[7,12]`, `[10]` | 3 | No |
| 31 | `[8,15,7]`, `[12,10]` | 2 | Yes |
| 30 | `[8,15,7]`, `[12,10]` | 2 | Yes |

Binary search progression:
1. `mid = 33` works → search left half.
2. `mid = 24` fails → search right half.
3. `mid = 29` fails → search right half.
4. `mid = 31` works → search left half.
5. `mid = 30` works → smallest feasible found.

Answer: `30`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log S)`, where `n = frames.length` and `S = sum(frames) - max(frames) + 1` is the search range width. Each binary-search step runs one linear feasibility scan. At `10^6` elements this remains practical; at `10^9` elements, even linear passes become the bottleneck regardless of the search strategy.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only running totals, bounds, and a day counter. No extra arrays, heaps, or DP tables are needed. Space cannot be meaningfully reduced further without changing the execution model.

## 💡 Key Takeaways
- If the problem asks for the **minimum numeric value** that satisfies a constraint, check whether feasibility is monotonic; that is the strongest signal for binary search on the answer.
- If items must remain **in order** and can only be grouped contiguously, a greedy left-to-right simulation is often the right feasibility test.
- Set the lower bound to `max(frames)`, not `0` or `min(frames)`; otherwise you search impossible capacities and complicate correctness.
- Use 64-bit arithmetic for `sum`, `mid`, and running totals; `frames[i]` can be `10^9`, so 32-bit overflow is easy to trigger.
- The transferable design insight: when exact partition search is combinatorial but feasibility is monotonic, separate the problem into a cheap predicate and a search over the decision boundary.

## 🚀 Variations & Further Practice
- **Split Array Largest Sum** — same core pattern, but framed as minimizing the largest subarray sum across exactly or at most `k` partitions; the twist is recognizing it despite different domain language.
- **Capacity To Ship Packages Within D Days** — nearly identical structure, useful for reinforcing the monotonic-capacity mental model under ordered workloads.
- **Allocate Minimum Number of Pages / Painter’s Partition** — same binary-search predicate, but the harder part is proving why contiguous assignment and greedy packing are sufficient under different business semantics.