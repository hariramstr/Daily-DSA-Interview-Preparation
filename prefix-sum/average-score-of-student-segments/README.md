# Average Score of Student Segments

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Prefix Sum &nbsp;|&nbsp; **Tags:** Prefix Sum, Array, Math

---

## 🗂 Problem Overview

Given an array `scores` of length `n` and a list of range queries `[left, right]`, return the average score for each contiguous segment. The core contract: for every query, compute `sum(scores[left..right]) / (right - left + 1)` as a float. The non-trivial constraint is scale — up to 10⁴ queries over an array of 10⁵ elements makes naïve per-query summation a performance liability, demanding an O(1)-per-query lookup structure built in a single O(n) pass.

---

## 🌍 Engineering Impact

Range-sum queries are ubiquitous at scale. Time-series analytics platforms (Prometheus, InfluxDB) answer windowed aggregations over metric arrays using exactly this precomputation. Ad-serving systems compute click-through rates over impression windows. Search ranking pipelines aggregate feature scores across document segments. Database engines use prefix-sum structures inside columnar storage for fast GROUP BY aggregations. Without precomputation, each query degrades to a full segment scan — at 10⁴ queries over 10⁵ elements, that's 10⁹ operations where 10⁵ would suffice.

---

## 🔍 Problem Statement

**Input:**
- `scores`: integer array, `1 <= scores.length <= 10^5`, `0 <= scores[i] <= 100`
- `queries`: 2D array where `queries[i] = [left, right]`, `1 <= queries.length <= 10^4`, indices are 0-based and guaranteed valid (`left <= right < scores.length`)

**Output:** Float array where `result[i] = mean(scores[left..right])` for each query.

**Examples:**

| scores | query | output |
|--------|-------|--------|
| `[80, 90, 70, 60, 85]` | `[0, 2]` | `80.0` |
| `[80, 90, 70, 60, 85]` | `[1, 4]` | `76.25` |
| `[50, 100, 40, 90, 20]` | `[0, 4]` | `60.0` |
| `[50, 100, 40, 90, 20]` | `[2, 3]` | `65.0` |

**Key constraint:** Queries are independent and read-only — no updates — which is precisely the condition under which a static prefix sum is optimal.

---

## 🪜 How to Solve This

1. **Observe the repeated work** → Every naïve query iterates over the same elements multiple times. If two queries overlap, their shared subrange is recomputed from scratch each time. That's the signal: precompute something.

2. **What do we need per query?** → A range sum and a count. The count is trivially `right - left + 1`. The range sum is the expensive part.

3. **Can we express a range sum using endpoints only?** → Yes. If we store cumulative sums, then `sum(left, right) = prefix[right+1] - prefix[left]`. This reduces any range sum to two array lookups and a subtraction.

4. **Build the prefix array once, answer every query in O(1)** → One O(n) pass to build, O(1) per query regardless of segment length. Total: O(n + q) instead of O(n·q).

5. **Divide by segment length for the average** → Integer division would truncate; use float division explicitly. The result is exact floating-point arithmetic on small integers — no precision concerns here.

The insight crystallizes: precomputation trades a one-time O(n) cost for O(1) amortized query cost — a pattern that appears every time you have many reads and no writes.

---

## 🧩 Algorithm Walkthrough

**Pattern: Static Prefix Sum (also called cumulative sum array)**

This is the right abstraction because the data is immutable and queries are purely read operations over contiguous ranges — the exact preconditions for prefix sum optimality.

**Steps:**

1. **Allocate prefix array** of size `n+1`, initialized to zero. The extra slot at index 0 acts as a sentinel, making the range formula uniform — no special-casing for queries starting at index 0.

2. **Build prefix sums** — iterate `i` from `1` to `n` inclusive: `prefix[i] = prefix[i-1] + scores[i-1]`. Invariant maintained: `prefix[i]` always equals the sum of the first `i` elements of `scores`.

3. **Answer each query** — for `[left, right]`, compute:
   - `range_sum = prefix[right + 1] - prefix[left]`
   - `count = right - left + 1`
   - `result = range_sum / float(count)`

   Correctness: `prefix[right+1]` is the sum of elements `0..right`; subtracting `prefix[left]` removes elements `0..left-1`, leaving exactly `left..right`.

4. **Return results array** — one float per query, preserving input order.

**Why not a segment tree or sparse table?** Both solve range queries but add O(n log n) build time and implementation complexity. Since there are no point updates and queries ask only for sum (not min/max), the prefix sum is strictly simpler and faster.

---

## 📊 Worked Example

`scores = [80, 90, 70, 60, 85]`, queries: `[0,2]`, `[1,4]`

**Prefix array construction:**

| i | scores[i-1] | prefix[i] |
|---|-------------|-----------|
| 0 | —           | 0         |
| 1 | 80          | 80        |
| 2 | 90          | 170       |
| 3 | 70          | 240       |
| 4 | 60          | 300       |
| 5 | 85          | 385       |

**Query resolution:**

| query | prefix[right+1] | prefix[left] | range_sum | count | average |
|-------|-----------------|--------------|-----------|-------|---------|
| [0,2] | prefix[3] = 240 | prefix[0] = 0 | 240      | 3     | **80.0** |
| [1,4] | prefix[5] = 385 | prefix[1] = 80 | 305     | 4     | **76.25** |

Both results match expected output. Note how `prefix[0] = 0` makes the `[0, x]` case require no special handling.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n + q)** — O(n) for the single prefix-sum construction pass, O(1) per query (two array reads, one subtraction, one division), totalling O(q) for all queries. At n = 10⁶ this is trivially fast; at n = 10⁹ the prefix array itself becomes a memory concern before time does.

### Space Complexity

**O(n)** for the prefix array of size `n+1`. The output array is O(q) but typically considered required output space. The prefix array cannot be eliminated without reverting to O(n) per query — there is no sub-linear space solution for arbitrary range queries without preprocessing trade-offs.

---

## 💡 Key Takeaways

- **Pattern signal — repeated range aggregation:** Whenever you see multiple queries over contiguous subarrays of a static array, prefix sum is the first tool to reach for — the "many reads, no writes" contract is the trigger.
- **Pattern signal — O(n·q) naïve cost:** If the brute-force solution multiplies input sizes together, look for a precomputation that decouples build cost from query cost.
- **Gotcha — off-by-one in prefix indexing:** The prefix array is 1-indexed relative to scores. `prefix[right+1] - prefix[left]` is correct; writing `prefix[right] - prefix[left-1]` requires a guard for `left == 0` and is a common source of bugs.
- **Gotcha — integer vs. float division:** In statically typed languages (Java, C++), `range_sum / count` on two integers truncates silently. Cast at least one operand to float/double before dividing, not after.
- **Architectural insight:** This pattern generalizes directly to immutable time-series data in production — precompute cumulative metrics at ingest time, store them alongside raw data, and serve window aggregations in O(1) at query time rather than scanning raw records on every request.

---

## 🚀 Variations & Further Practice

- **Range Sum Query — Mutable (LeetCode 307):** Introduces point updates between queries, breaking the static prefix sum. The conceptual twist: you need a data structure that supports both O(log n) updates and O(log n) range queries — enter Binary Indexed Trees (Fenwick Trees) or Segment Trees.
- **2D Range Sum Query (LeetCode 304):** Extends prefix sums to a matrix, where the range becomes a submatrix. The inclusion-exclusion formula for 2D prefix sums is non-trivial and a common interview stumbling block — understanding the 1D case cold is the prerequisite.
- **Maximum Average Subarray of Fixed Length k (LeetCode 643):** Shifts the problem from arbitrary queries to finding the optimal window, combining prefix sums with a sliding window scan — a useful exercise in recognizing when you need optimization on top of aggregation.