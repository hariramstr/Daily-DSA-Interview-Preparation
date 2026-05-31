# Minimum Days to Distribute K Types of Packages

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Sorting

---

## 🗂 Problem Overview

Given `n` workers, a capacity `C`, and `m` package types each with a weight and count, find the minimum number of days to fully distribute at least `k` distinct package types. Each worker makes one trip per day, carries only one type per trip, and is capped at `C` total weight. The non-trivial constraint: days, workers, and per-type throughput interact multiplicatively, making a direct greedy on days intractable without inverting the search space.

---

## 🌍 Engineering Impact

This pattern — binary search on the answer, validate via a greedy feasibility check — appears directly in distributed job scheduling (can `k` tasks complete within `d` time slots given `n` workers?), cloud resource provisioning (minimum billing periods to drain `k` queues), and batch ETL pipelines where you need to guarantee SLA coverage for a subset of data streams. Without this inversion, naive enumeration over days is O(max\_days × m), which collapses under production-scale cardinalities. The binary search reduces that to O(m log(max\_days)), keeping scheduler hot-paths within latency budgets.

---

## 🔍 Problem Statement

**Inputs:**
- `n` — number of workers (1 ≤ n ≤ 10⁴)
- `C` — per-worker weight capacity per trip (1 ≤ C ≤ 10⁶)
- `weights[i]`, `counts[i]` — weight and total package count for type `i` (1 ≤ m ≤ 10⁵, 1 ≤ counts[i] ≤ 10⁹)
- `k` — minimum distinct types that must be fully distributed (1 ≤ k ≤ m)

**Output:** Minimum integer `d` such that at least `k` types can be fully distributed.

**Key derived quantity:** For type `i` given `d` days, throughput = `n × d × floor(C / weights[i])`. Type `i` is completable in `d` days iff `n × d × floor(C / weights[i]) >= counts[i]`.

**Edge cases:** A type with `weights[i] > C` can never be distributed (zero throughput — exclude it). When `k` exceeds the number of distributable types, the problem has no solution.

**Example 1:** `n=3, C=10, weights=[4,6,3], counts=[12,6,9], k=2` → **Output: 2**

**Example 2:** `n=2, C=5, weights=[5,3,4], counts=[10,15,8], k=3` → **Output: 5**

---

## 🪜 How to Solve This

1. **Observe the monotonicity:** If you can distribute `k` types in `d` days, you can certainly do it in `d+1` days. The feasibility function is monotone in `d` — this is the binary search trigger.

2. **Invert the problem:** Instead of "how many days does type `i` need?", ask "given `d` days, how many types can I complete?" This transforms a minimization over days into a counting problem over types.

3. **Per-type days formula:** For type `i`, minimum days required = `ceil(counts[i] / (n × floor(C / weights[i])))`. This is O(1) per type.

4. **Greedy selection:** To maximize the number of types completable within `d` days, greedily pick types with the smallest required days first — sort ascending by required days, take the first `k`, check if the `k`-th value ≤ `d`.

5. **Binary search bounds:** Lower bound is 1; upper bound is `ceil(max(counts[i]) / n)` (worst case: one package per worker per day). Run standard binary search, calling the feasibility check at each midpoint.

6. **Result:** The leftmost `d` where at least `k` types are completable is the answer.

---

## 🧩 Algorithm Walkthrough

**Pattern: Binary Search on Answer + Greedy Feasibility Check**

This is the right abstraction because the answer space (days) is ordered and the feasibility predicate is monotone — two conditions sufficient for binary search on the answer.

**Steps:**

1. **Precompute per-type minimum days.** For each type `i`, compute `pkgs_per_day = n × floor(C / weights[i])`. If this is zero (weight exceeds capacity), mark the type as infeasible and exclude it. Otherwise, `min_days[i] = ceil(counts[i] / pkgs_per_day)`. This step is O(m).

2. **Sort `min_days`.** Ascending sort gives us the greedy ordering — cheapest types first. O(m log m).

3. **Define feasibility check `can_complete(d)`.** Binary search within the sorted `min_days` array for the largest index where `min_days[idx] <= d`. If that index ≥ k−1 (0-indexed), at least `k` types are completable. This is O(log m) via `upper_bound`.

4. **Binary search on `d`.** `lo = 1`, `hi = ceil(max(counts) / n)`. At each midpoint, call `can_complete(mid)`. Narrow the window using standard left-boundary binary search. Total: O(log(max_days)) iterations × O(log m) per check.

5. **Invariant maintained:** At every step, `lo` is a candidate answer and `hi` is known feasible or out of range. The loop terminates with `lo` as the minimum feasible `d`.

**Total complexity:** O(m log m + log(max\_days) × log m).

---

## 📊 Worked Example

**Input:** `n=3, C=10, weights=[4,6,3], counts=[12,6,9], k=2`

**Precompute `min_days`:**

| Type | weight | floor(C/w) | pkgs/day (×n) | counts | min\_days |
|------|--------|------------|---------------|--------|-----------|
| 0    | 4      | 2          | 6             | 12     | 2         |
| 1    | 6      | 1          | 3             | 6      | 2         |
| 2    | 3      | 3          | 9             | 9      | 1         |

**Sorted `min_days`:** `[1, 2, 2]`

**Binary search:** `lo=1, hi=4`

| Iteration | mid | Types with min\_days ≤ mid | Count ≥ k=2? | Action       |
|-----------|-----|---------------------------|--------------|--------------|
| 1         | 2   | [1, 2, 2] → 3 types       | ✓            | hi = 2       |
| 2         | 1   | [1] → 1 type              | ✗            | lo = 2       |

**Result:** `lo = 2` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(m log m + log(max\_days) × log m).** Sorting dominates setup at O(m log m). The binary search runs O(log(max\_days)) ≈ O(log(10⁹/1)) ≈ 30 iterations, each doing an O(log m) binary search on the sorted array. At m = 10⁵, the total is well under 10⁷ operations — comfortably real-time.

### Space Complexity

**O(m)** for the `min_days` array. No auxiliary structures beyond the sort (which can be in-place). Irreducible unless you recompute per-type days inline during the feasibility check, trading a constant-factor memory reduction for negligible practical gain.

---

## 💡 Key Takeaways

- **Pattern signal — monotone feasibility:** When a problem asks for "minimum X such that some condition holds," and the condition is monotone in X, binary search on the answer is almost always the right move. The trigger here is recognizing that "days" forms a totally ordered feasibility space.
- **Pattern signal — throughput decomposition:** When items are independent and throughput is multiplicative (workers × days × rate), per-item cost reduces to a closed-form expression, enabling a sort-and-count greedy rather than simulation.
- **Gotcha — integer ceiling division:** `ceil(a / b)` must be computed as `(a + b - 1) / b` in integer arithmetic. Using float division here introduces rounding errors at `counts[i]` up to 10⁹ that silently produce wrong answers.
- **Gotcha — zero throughput exclusion:** Types where `floor(C / weights[i]) == 0` must be filtered before sorting. Including them poisons the `min_days` array with infinities (or division-by-zero), and failing to account for them inflates the apparent feasible type count.
- **Architectural insight:** This "binary search on answer + O(log n) feasibility check" pattern composes cleanly in production schedulers — the feasibility check is a pure function with no side effects, making it trivially parallelizable and safe to call speculatively in prefetch pipelines or admission-control hot paths.

---

## 🚀 Variations & Further Practice

- **Heterogeneous worker capacities:** Each worker has a distinct capacity `C_i`. The per-type throughput is now `sum(floor(C_i / weights[j]))` across workers — the closed-form per-type cost survives, but computing it requires an O(n) pass per type, pushing total complexity to O(mn log(max\_days)). Precomputing a sorted worker-capacity array and using binary search per type recovers O(m log n log(max\_days)).
- **Dependent package types with prerequisites:** Type `j` cannot begin distribution until type `i` is complete (DAG of dependencies). The greedy sort-and-pick no longer applies; the feasibility check becomes a DAG scheduling problem (critical path analysis), and the binary search outer loop remains valid but the inner check jumps from O(log m) to O(m + edges).
- **LeetCode 1011 — Capacity to Ship Packages Within D Days:** The structural twin of this problem — binary search on capacity instead of days, with a linear greedy feasibility scan. Solving both back-to-back cements the "invert the search dimension" mental model.