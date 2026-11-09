# Count Concert Duos Within Stage Width

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an unsorted integer array `positions` and an integer `widthLimit`, count how many distinct index pairs `(i, j)` with `i < j` satisfy `|positions[i] - positions[j]| <= widthLimit`. The output is a count, not the pairs themselves. The challenge is scale: with up to `200000` elements, the naive `O(n^2)` pair scan is too slow, so the solution must exploit ordering and count valid ranges in bulk.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to count proximity relationships under a threshold: matching events within latency windows in streaming pipelines, grouping nearly equal timestamps in observability systems, identifying nearby price points in trading analytics, or counting candidate joins in search/ranking prefilters. At scale, brute-force pair enumeration explodes quadratically and becomes both CPU- and cache-hostile. Sorting plus a linear sweep converts pairwise comparison into range counting, which is the difference between a batch job that fits in a latency budget and one that stalls a pipeline or forces premature sharding.

## 🔍 Problem Statement
You are given:

- `positions`: an integer array of length `1` to `200000`
- `widthLimit`: an integer from `0` to `1000000000`

Return the number of index pairs `(i, j)` such that:

- `i < j`
- `|positions[i] - positions[j]| <= widthLimit`

Important details:

- `positions` is not sorted.
- Values may be negative.
- Duplicate values are allowed.
- Equal values at different indices represent different performers and must be counted as distinct pairs.
- The result may exceed 32-bit integer range, so use a 64-bit accumulator.

Examples:

- `positions = [1, 3, 4, 9]`, `widthLimit = 3` → `3`
- `positions = [8, 2, 2, 5, 11]`, `widthLimit = 0` → `1`

The decisive constraint is `n = 200000`: this rules out nested-loop pair checking and pushes toward `O(n log n)` or better.

## 🪜 How to Solve This
1. Read the condition `|positions[i] - positions[j]| <= widthLimit` → this is a distance threshold, not an arbitrary pair predicate. Threshold predicates often become easier after sorting.

2. Sort the array → once values are ordered, absolute difference simplifies. For `i < j` in sorted order, `|a[j] - a[i]|` becomes just `a[j] - a[i]`.

3. Ask: for each left endpoint, how many right endpoints are valid?  
   If the array is sorted and `a[right] - a[left] <= widthLimit`, then every index between them is also valid.

4. That observation means we do not need to count pairs one by one. We can count an entire block at once: if the valid window is `[left, right)`, then `left` contributes `right - left - 1` pairs.

5. Use two pointers because the valid range only moves forward. As `left` increases, the best `right` never needs to move backward.

6. This gives a clean shape: sort once, sweep once, accumulate counts in a 64-bit integer. That is the standard signature of a sortable threshold-counting problem.

## 🧩 Algorithm Walkthrough
1. **Sort the input array.**  
   This is the enabling transformation. After sorting, for any `left < right`, the difference is monotonic: increasing `right` can only increase `positions[right] - positions[left]`. That monotonicity is what makes the Two Pointers pattern applicable.

2. **Initialize `left = 0`, `right = 0`, and `count = 0` as a 64-bit integer.**  
   The invariant will be: before counting for a given `left`, `right` is the first index that is *outside* the valid window or `n` if all remaining elements are valid.

3. **For each `left` from `0` to `n - 1`, advance `right` while the pair remains valid.**  
   Specifically, while `right < n` and `positions[right] - positions[left] <= widthLimit`, increment `right`. Because the array is sorted, once this fails, no larger index can work for the same `left`.

4. **Count all valid partners for `left` in one step.**  
   The valid indices are `(left + 1)` through `(right - 1)`, so add `right - left - 1` to `count`. This is correct because every element in that contiguous range satisfies the threshold, and no element at or beyond `right` does.

5. **Move `left` forward without resetting `right`.**  
   This is the key Two Pointers optimization. Since `left` only increases, the next valid window starts no earlier than the current one. Both pointers move monotonically, so the sweep after sorting is linear.

6. **Return `count`.**  
   The algorithm counts each pair exactly once, anchored at its smaller sorted index, and handles duplicates naturally.

## 📊 Worked Example
Example: `positions = [1, 3, 4, 9]`, `widthLimit = 3`

Sorted array: `[1, 3, 4, 9]`

| left | right after expansion | valid partners for `left` | added | total |
|---|---:|---:|---:|---:|
| 0 (`1`) | 3 | indices 1, 2 → `(1,3)`, `(1,4)` | 2 | 2 |
| 1 (`3`) | 3 | index 2 → `(3,4)` | 1 | 3 |
| 2 (`4`) | 3 | none | 0 | 3 |
| 3 (`9`) | 4 | none | 0 | 3 |

Trace:

1. Start `left=0`, expand `right` through `1, 3, 4`; stop before `9` because `9 - 1 = 8 > 3`.
2. Add `3 - 0 - 1 = 2`.
3. Move `left=1`; `right` stays at `3`. `4 - 3 <= 3`, but `right` already marks the first invalid index, so add `3 - 1 - 1 = 1`.
4. Remaining positions contribute nothing.

Final answer: `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` overall: `O(n log n)` for sorting plus `O(n)` for the two-pointer sweep. Sorting dominates. At `10^6` elements this is still practical in most environments; at `10^9`, even reading and sorting the data becomes a system-level problem requiring external-memory or distributed strategies.

### Space Complexity
`O(1)` auxiliary space if sorting in place, excluding the sort implementation’s internal stack/buffer costs. In practice, the sorted array owns the working set. You can avoid extra structures entirely; reducing space further is not meaningful unless the platform’s sort requires copying.

## 💡 Key Takeaways
- If the pair condition is a numeric threshold like “difference <= k,” sorting is often the move that turns an all-pairs problem into contiguous range counting.
- If a valid set of partners becomes a continuous window after sorting, that is a strong signal for a Two Pointers sweep.
- Use `right - left - 1`, not `right - left`, because `left` cannot pair with itself and `right` is the first invalid index.
- Store the answer in a 64-bit type; with `n = 200000`, the pair count can exceed 32-bit range even when every pair is valid.
- The transferable design insight is to replace explicit pair enumeration with monotonic window boundaries and aggregate counting whenever the predicate becomes ordered.

## 🚀 Variations & Further Practice
- Count pairs with sum `<= K` instead of difference `<= K`; same sorted two-pointer family, but the window condition is based on two ends rather than a fixed left anchor.
- Count pairs where `L <= |a[i] - a[j]| <= R`; harder because you typically compute two threshold counts and subtract, or manage dual windows.
- Return the number of valid triplets instead of pairs; same monotonic reasoning applies, but the counting becomes nested and requires careful fixation of one dimension.