# Minimum Batch Size for Deadline-Limited Jobs

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, greedy, prefix-sum

---

## 🗂 Problem Overview
Given two arrays, `jobs` and `deadline`, find the smallest integer batch size `B` such that processing jobs in order at rate `B` records per day finishes every job by its own deadline. Job `i` completes after `ceil((jobs[0] + ... + jobs[i]) / B)` days. The challenge is that deadlines apply to cumulative progress, not individual job sizes, so local decisions are insufficient; you need a global feasibility test over all prefixes.

## 🌍 Engineering Impact
This pattern shows up in throughput provisioning for ordered workloads: streaming ETL stages, backfill pipelines, log compaction, database migration batches, CI/CD job queues, and rate-limited ingestion services. The operational question is rarely “can we finish one unit fast enough?” but “what minimum sustained capacity guarantees every milestone on an ordered dependency chain?” Without exploiting monotonic feasibility, teams overprovision, simulate expensively, or miss SLOs under peak backlog. Binary search over capacity turns sizing from trial-and-error into a deterministic planning primitive, which matters when capacity decisions affect cost, autoscaling policy, and admission control.

## 🔍 Problem Statement
You are given `jobs[i]`, the number of records in the `i`-th job, and `deadline[i]`, the latest day by which that job must be fully completed. Jobs must be processed strictly in input order, and work on job `i + 1` cannot begin until job `i` finishes. With batch size `B`, the system processes at most `B` records per day, carrying unfinished work across days.

For each prefix sum `prefix[i] = jobs[0] + ... + jobs[i]`, job `i` finishes on day `ceil(prefix[i] / B)`. We need the smallest integer `B` such that `ceil(prefix[i] / B) <= deadline[i]` for all `i`.

Constraints are large: up to `2 * 10^5` jobs, each value up to `10^9`, so `O(n log M)` is acceptable but `O(n^2)` is not.

Examples:
- `jobs = [5, 8, 6]`, `deadline = [2, 4, 5]` → `4`
- `jobs = [7, 2, 9, 4]`, `deadline = [1, 2, 5, 6]` → `7`

## 🪜 How to Solve This
1. Read the formula carefully → each deadline constrains a **prefix**, not an isolated job. That immediately suggests cumulative sums.
2. Ask what changes when `B` increases → every value `ceil(prefix / B)` stays the same or decreases. So feasibility is monotonic.
3. Monotonic answer space usually means binary search on the answer, not on the input.
4. Define a predicate: for a candidate `B`, scan jobs left to right, maintain running prefix sum, and verify `ceil(prefix / B) <= deadline[i]` at every step.
5. If the predicate fails once, `B` is too small; if it succeeds for all jobs, `B` is sufficient.
6. Search the smallest sufficient `B`. A safe lower bound is `1`; a safe upper bound is `max(jobs)`, since processing at least the largest single job in one day is always enough given the problem guarantee.
7. Use integer arithmetic for ceiling: `ceil(x / B) = (x + B - 1) / B`.
8. Because prefix sums can reach `2e5 * 1e9`, use 64-bit integers throughout.

Once you see “minimum capacity satisfying all ordered milestones,” binary search plus linear feasibility becomes the obvious shape.

## 🧩 Algorithm Walkthrough
1. **Precompute feasibility as a monotonic predicate**  
   Pattern: **Binary Search on Answer** with a **greedy linear check**. For any fixed `B`, there is only one relevant question: do all prefixes finish by their deadlines? Larger `B` can only help, so the predicate is monotonic.

2. **Choose search bounds**  
   Set `lo = 1`. Set `hi = max(jobs)`. This upper bound is sufficient under the problem’s guarantee that a valid answer exists; if every day can process at least the largest job, no single job requires more than one day of fresh capacity, and larger values are unnecessary for search correctness.

3. **Implement the feasibility check**  
   Scan from left to right, maintaining `prefix += jobs[i]`. Compute `finishDay = (prefix + B - 1) / B`. If `finishDay > deadline[i]`, return `false` immediately.  
   Invariant: after processing index `i`, if no violation has occurred, then all jobs `0..i` meet their deadlines under batch size `B`.

4. **Binary search for the first valid `B`**  
   While `lo < hi`, test `mid = lo + (hi - lo) / 2`.  
   - If `feasible(mid)` is true, keep the left half: `hi = mid`.  
   - Otherwise, discard it: `lo = mid + 1`.  
   Invariant: the answer always remains within `[lo, hi]`.

5. **Return `lo`**  
   When the interval collapses, `lo` is the smallest batch size whose feasibility check passes. This is correct because binary search maintained the first-true boundary of a monotonic boolean function.

## 📊 Worked Example
Example: `jobs = [5, 8, 6]`, `deadline = [2, 4, 5]`

Binary search over `B in [1, 8]`:

| B | Prefix sums | Finish days `ceil(prefix/B)` | Valid? |
|---|-------------|------------------------------|--------|
| 4 | 5, 13, 19   | 2, 4, 5                      | Yes    |
| 2 | 5, 13, 19   | 3, 7, 10                     | No     |
| 3 | 5, 13, 19   | 2, 5, 7                      | No     |

Trace:
1. `lo=1, hi=8`, test `mid=4` → all deadlines satisfied, so answer is `<= 4`.
2. `lo=1, hi=4`, test `mid=2` → first job already misses (`ceil(5/2)=3 > 2`), so answer is `> 2`.
3. `lo=3, hi=4`, test `mid=3` → second job misses (`ceil(13/3)=5 > 4`), so answer is `> 3`.
4. `lo=4, hi=4` → stop. Minimum valid batch size is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n = jobs.length` and `M` is the search range for `B` (here bounded by `max(jobs)`). Each binary-search step performs one linear feasibility scan. At `10^6` elements this remains practical; at `10^9`, the scan itself becomes the bottleneck regardless of the logarithmic factor.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only the running prefix sum, bounds, and loop variables. No extra arrays or prefix-sum buffer are required. Space cannot be meaningfully reduced further without changing the execution model; the main trade-off is using 64-bit arithmetic for correctness.

## 💡 Key Takeaways
- If the question asks for the **minimum capacity/rate/threshold** that makes all constraints pass, check whether feasibility becomes monotonic as the answer grows.
- When deadlines apply to **ordered cumulative work**, think in prefixes first; per-item reasoning usually misses the real constraint surface.
- The example hinges on `ceil(prefix / B)`, so use integer-safe ceiling division: `(prefix + B - 1) / B`, not floating point.
- Prefix sums can exceed 32-bit range even when each input value fits in 32 bits; use 64-bit types for `prefix`, `mid`, and derived arithmetic.
- In production sizing problems, binary search over a monotonic SLA predicate is often the cleanest way to convert capacity planning from simulation-heavy guesswork into a bounded, explainable decision procedure.

## 🚀 Variations & Further Practice
- Allow **reordering of jobs** before execution. The twist is that feasibility is no longer determined by fixed prefixes; scheduling strategy becomes part of the problem.
- Add **per-day setup costs or idle gaps** between jobs. The monotonic predicate may still hold, but the finish-time formula is no longer a simple ceiling on prefix sums.
- Let batch size vary by day with a fixed **capacity budget**. Now the problem shifts from scalar answer search to allocation or DP over time, with deadlines interacting across multiple dimensions.