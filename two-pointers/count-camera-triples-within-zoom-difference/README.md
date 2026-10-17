# Count Camera Triples Within Zoom Difference

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Combinatorics

---

## 🗂 Problem Overview
Given an unsorted array `zoomLevels` and an integer `maxGap`, count how many triples of distinct indices form a bundle whose maximum zoom minus minimum zoom is at most `maxGap`. Return the count as a 64-bit integer. The challenge is scale: with up to `100000` cameras, brute-force enumeration of all `O(n^3)` triples is infeasible, so the solution must exploit ordering and incremental counting.

## 🌍 Engineering Impact
This pattern shows up anywhere systems count valid groups under a bounded range constraint: search ranking windows, telemetry bucketing, fraud detection on temporally close events, and stream processors building candidate sets from sorted keys. At small scale, naive tuple generation works; at warehouse, ad-tech, or observability scale, it explodes combinatorially and burns both CPU and memory bandwidth. Sorting plus a moving window turns “enumerate everything” into “count what the current admissible region implies,” which is the difference between a batch job that misses SLA and one that can run inline inside a service or pipeline stage.

## 🔍 Problem Statement
You are given an integer array `zoomLevels` where each value represents a camera’s zoom capability, and an integer `maxGap`. You must count the number of triples `(i, j, k)` with distinct indices such that the difference between the largest and smallest zoom values among those three cameras is at most `maxGap`.

The array is not sorted. Equal zoom values are allowed, and bundles are distinguished by indices, not by values. The result may be large, so return a 64-bit integer.

**Constraints**
- `3 <= zoomLevels.length <= 100000`
- `0 <= zoomLevels[i] <= 1000000000`
- `0 <= maxGap <= 1000000000`

**Examples**
- `zoomLevels = [4, 1, 3, 6, 5], maxGap = 2` → `3`
- `zoomLevels = [2, 2, 2, 2], maxGap = 0` → `4`

The key constraint is `n = 100000`: this rules out cubic and even quadratic triple enumeration.

## 🪜 How to Solve This
1. Start with the condition itself: for a triple to be valid, only the **minimum and maximum** values matter. The middle value can be anything between them.

2. That immediately suggests sorting. Once values are sorted, any valid triple ending at position `right` must come from a contiguous range of earlier elements whose values are within `maxGap` of `zoomLevels[right]`.

3. Now ask: if I fix the largest element of the triple, how many pairs to its left can join it?  
   → If the valid window is `[left ... right-1]`, and it contains `m = right - left` earlier elements, then any choice of 2 of those `m` elements forms a valid triple with `right`.

4. So the problem reduces to maintaining the smallest `left` such that  
   `zoomLevels[right] - zoomLevels[left] <= maxGap`.

5. Because the array is sorted, `left` only moves forward. That is the two-pointer/sliding-window signal: monotonic boundary, linear scan after sorting.

6. For each `right`, add `C(m, 2) = m * (m - 1) / 2` to the answer. No triple generation, just counting.

## 🧩 Algorithm Walkthrough
1. **Sort the array.**  
   This converts the bundle condition from “max minus min over arbitrary positions” into a range condition over ordered values. In sorted order, if `a[right] - a[left] <= maxGap`, then every value between `left` and `right` also lies within that same bound.

2. **Initialize two pointers:** `left = 0`, iterate `right` from `0` to `n - 1`.  
   This is the **Two Pointers / Sliding Window** pattern. The abstraction fits because the admissible region for each `right` is contiguous and its left boundary moves monotonically.

3. **Shrink the window until valid.**  
   While `a[right] - a[left] > maxGap`, increment `left`.  
   Invariant: after this loop, `[left ... right]` is the largest suffix ending at `right` whose min/max difference is within `maxGap`.

4. **Count triples ending at `right`.**  
   Let `m = right - left`, the number of earlier elements in the valid window. To form a triple with `a[right]` as the maximum, choose any 2 of those `m` elements:  
   `add = C(m, 2) = m * (m - 1) / 2`, valid only when `m >= 2`.

5. **Accumulate into a 64-bit result.**  
   This is required because the count can be on the order of `C(100000, 3)`, which exceeds 32-bit range.

6. **Why this is correct.**  
   Every valid triple has a unique largest index in sorted order, so it is counted exactly once when processing that `right`. No invalid triple is counted, because both chosen earlier elements lie inside the same valid window, so their values differ from `a[right]` by at most `maxGap`, and therefore the triple’s overall max-min also satisfies the constraint.

## 📊 Worked Example
Example: `zoomLevels = [4, 1, 3, 6, 5]`, `maxGap = 2`

Sorted: `[1, 3, 4, 5, 6]`

| right | value | left after shrink | valid earlier count `m` | added `C(m,2)` | total |
|---|---:|---:|---:|---:|---:|
| 0 | 1 | 0 | 0 | 0 | 0 |
| 1 | 3 | 0 | 1 | 0 | 0 |
| 2 | 4 | 0→1 (`4-1>2`) | 1 | 0 | 0 |
| 3 | 5 | 1 | 2 (`3,4`) | 1 | 1 |
| 4 | 6 | 1→2 (`6-3>2`) | 2 (`4,5`) | 1 | 2 |

That table alone misses one valid triple because at `right = 4`, after shrinking to `left = 2`, earlier valid elements are indices `2,3` plus `1` is invalid. The valid triples are counted as:
- with max `5`: `(3,4,5)`
- with max `6`: `(3,4,6)` and `(4,5,6)`

Total = `3`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates at `O(n log n)`, followed by a linear two-pointer scan at `O(n)` because each pointer advances at most `n` times. At `10^6` elements this is still practical in optimized environments; at `10^9`, even sorting becomes the limiting factor and requires distributed or external-memory strategies.

### Space Complexity
`O(1)` auxiliary space beyond the sort, if sorting in place is allowed; otherwise `O(n)` depending on the language runtime’s sort implementation. The counting logic itself uses constant extra space. Reducing sort memory usually means accepting implementation-specific trade-offs or unstable in-place sorting behavior.

## 💡 Key Takeaways
- If the condition depends only on `max - min` within a group, sorting often turns an unordered combinatorial problem into a contiguous-window problem.
- When a valid region expands or shrinks monotonically as you scan, two pointers are usually the right abstraction.
- Count combinations from a valid window; do not enumerate triples. The formula here is `C(windowSize - 1, 2)` when fixing the right endpoint.
- Be careful with the window size: if `m = right - left`, the number of triples added is `C(m, 2)`, not `C(m + 1, 3)`.
- In production code, this is the broader pattern of replacing explicit candidate generation with boundary maintenance plus aggregate counting.

## 🚀 Variations & Further Practice
- Count compatible bundles of size `k` instead of `3`. The sliding-window idea remains, but the combinatorics generalize to `C(m, k-1)` and overflow handling becomes more important.
- Count triples where `max - min <= maxGap` in an online stream. The harder twist is maintaining an ordered window and combinatorial counts under insertions and expirations.
- Return the actual triples, not just the count. The conceptual shift is from compressed counting to explicit materialization, which changes the lower bound and often makes output size dominate runtime.