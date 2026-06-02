# Total Rainfall Between Two Checkpoints

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Prefix Sum &nbsp;|&nbsp; **Tags:** Prefix Sum, Array, Range Query

---

## 🗂 Problem Overview

Given an array of `n` daily rainfall measurements and a batch of `[left, right]` range queries, return the sum of elements within each range, inclusive. The naive approach — iterating over each range per query — is O(n) per query, making it O(n·q) overall. With `n` and `q` both reaching 10^5, that's 10^10 operations: unacceptable. The non-trivial constraint is that both the array size and query count are large simultaneously, forcing a preprocessing strategy.

---

## 🌍 Engineering Impact

Range sum queries are a foundational primitive in production systems. Time-series databases (InfluxDB, Prometheus) answer aggregation queries over sliding windows using prefix-sum-style structures. Ad-serving platforms compute cumulative impression budgets across time slots. Network monitoring tools tally byte counts between router hops. Billing systems aggregate usage across billing periods. Without precomputation, each query degrades to a full scan — at 10^5 queries per second, that collapses throughput. Prefix sums decouple data ingestion from query serving, enabling O(1) reads regardless of range width.

---

## 🔍 Problem Statement

**Input:**
- `rainfall`: integer array of length `n`, where `rainfall[i]` is the rainfall in mm at checkpoint `i`
- `queries`: 2D array where `queries[j] = [left, right]`

**Output:** Integer array `answer` where `answer[j] = sum(rainfall[left..right])`, inclusive.

**Constraints:**
- `1 <= n <= 10^5`
- `0 <= rainfall[i] <= 1000`
- `1 <= queries.length <= 10^5`
- `0 <= left <= right < n`

**Examples:**

| Input | Output |
|---|---|
| `rainfall=[3,1,4,1,5,9,2,6]`, `queries=[[1,4],[0,6],[3,3]]` | `[11, 25, 1]` |
| `rainfall=[0,2,7,3,5]`, `queries=[[0,4],[2,3]]` | `[17, 10]` |

**Edge case to note:** A single-element query (`left == right`) must return exactly `rainfall[left]` — the formula must handle this without special-casing.

---

## 🪜 How to Solve This

1. **Read the problem** → we're answering many independent range-sum queries on a static array. "Many queries, static data" is the trigger.

2. **Naive cost** → iterating each range is O(n) per query. With q queries, that's O(n·q). At n=q=10^5, this is 10^10 operations — a non-starter.

3. **What if we precomputed cumulative sums?** → `prefix[i]` = sum of all elements from index 0 to i-1. Then `sum(left, right) = prefix[right+1] - prefix[left]`. Each query becomes O(1).

4. **Why does subtraction work?** → `prefix[right+1]` includes everything up to `right`. `prefix[left]` includes everything before `left`. Their difference is exactly the range we want — no elements double-counted, none missed.

5. **Total cost** → O(n) to build the prefix array, O(1) per query, O(q) total for all queries. The preprocessing cost is paid once; every query is free.

The pattern clicks immediately once you recognize that range sums are differences of cumulative sums — the same insight behind integral calculus.

---

## 🧩 Algorithm Walkthrough

**Pattern: Prefix Sum (Static Range Query)**

This is the canonical application: static array, repeated range-sum queries, no updates. Prefix sum is the right abstraction because it converts a linear-scan operation into a constant-time lookup by shifting work from query time to build time.

**Steps:**

1. **Allocate prefix array** of size `n+1`, initialized to 0. The extra slot at index 0 acts as a sentinel — `prefix[0] = 0` — eliminating the need to special-case the `left == 0` query.

2. **Build prefix array:** For `i` from 1 to n, set `prefix[i] = prefix[i-1] + rainfall[i-1]`. After this step, `prefix[i]` holds the total rainfall across checkpoints 0 through i-1. **Invariant:** `prefix[i]` always equals the exact sum of the first `i` elements.

3. **Answer each query:** For `[left, right]`, compute `prefix[right+1] - prefix[left]`. This works because:
   - `prefix[right+1]` = sum of checkpoints 0..right
   - `prefix[left]` = sum of checkpoints 0..left-1
   - Difference = sum of checkpoints left..right ✓

4. **Single-element correctness:** When `left == right`, result is `prefix[right+1] - prefix[right] = rainfall[right]`. No special case needed — the formula is self-consistent.

5. **Store and return** all query results in an output array.

---

## 📊 Worked Example

**Input:** `rainfall = [3, 1, 4, 1, 5, 9, 2, 6]`, query `[1, 4]`

**Build phase:**

| i | rainfall[i-1] | prefix[i] |
|---|---|---|
| 0 | — | 0 |
| 1 | 3 | 3 |
| 2 | 1 | 4 |
| 3 | 4 | 8 |
| 4 | 1 | 9 |
| 5 | 5 | 14 |
| 6 | 9 | 23 |
| 7 | 2 | 25 |
| 8 | 6 | 31 |

**Query `[1, 4]`:** `prefix[5] - prefix[1]` = `14 - 3` = **11** ✓

**Query `[0, 6]`:** `prefix[7] - prefix[0]` = `25 - 0` = **25** ✓

**Query `[3, 3]`:** `prefix[4] - prefix[3]` = `9 - 8` = **1** ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n + q)** — O(n) for a single linear pass to build the prefix array, then O(1) per query for O(q) total. At 10^6 elements and queries this is trivially fast; even at 10^9 it's a memory-bandwidth problem, not a compute problem. The dominant cost shifts to cache performance, not algorithmic complexity.

### Space Complexity

**O(n)** for the prefix array of size `n+1`. This space is non-negotiable for the O(1)-per-query guarantee — there is no way to answer arbitrary range queries in O(1) without O(n) precomputed state. The trade-off is explicit: space buys query speed.

---

## 💡 Key Takeaways

- **Pattern signal #1:** "Many range queries on a static array" is the canonical trigger for prefix sums. If the array doesn't change between queries, precomputation is almost always the right move.
- **Pattern signal #2:** When query cost is O(n) and query count is O(n), total complexity is O(n²) — look for a way to pay O(n) once at build time to reduce each query to O(1).
- **Implementation gotcha #1:** Allocate the prefix array with size `n+1`, not `n`. Using size `n` forces awkward index arithmetic and makes the `left == 0` case a special case rather than a natural result of `prefix[0] = 0`.
- **Implementation gotcha #2:** The query formula is `prefix[right+1] - prefix[left]`, not `prefix[right] - prefix[left-1]`. The off-by-one is subtle — always verify with a single-element query (`left == right`) and a full-array query (`left=0, right=n-1`).
- **Architectural insight:** This pattern generalizes directly to production: precompute on write, serve on read. Any system with high read-to-write ratios (dashboards, reporting, analytics) should evaluate whether a prefix-sum-style materialized view can replace repeated aggregation at query time.

---

## 🚀 Variations & Further Practice

- **Range Sum Query — Mutable (LeetCode 307):** Introduces point updates between queries. A static prefix array breaks immediately on any update — O(n) rebuild per update is untenable. This forces a Binary Indexed Tree (Fenwick Tree) or Segment Tree, both of which achieve O(log n) for updates and queries. The conceptual twist: you're now trading between update cost and query cost.

- **2D Range Sum Query (LeetCode 304):** Extends prefix sums to a matrix, where each query asks for the sum of a rectangular subgrid. The 2D prefix sum formula involves inclusion-exclusion across four corners — a direct generalization, but the off-by-one errors compound in two dimensions and require careful index discipline.

- **Subarray Sum Equals K (LeetCode 560):** Inverts the problem — instead of answering given ranges, find how many subarrays sum to a target `k`. Prefix sums are still the tool, but the query becomes a HashMap lookup: for each `prefix[i]`, check whether `prefix[i] - k` has been seen before. The twist is recognizing that range-sum queries and subarray-counting problems share the same underlying structure.