# Count Warehouse Aisle Pairs Within Walking Distance

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an unsorted array of station positions and a maximum allowed walking distance, count how many distinct pairs of stations are at most `maxDistance` apart. Return the total number of pairs `(i, j)` where `i < j`. The challenge is scale: with up to 200,000 positions, checking every pair is too slow. The intended solution combines sorting with a two-pointers scan to count whole ranges of valid pairs in linear time after sorting.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to count or detect “nearby” entities under a threshold: matching events in streaming pipelines by timestamp skew, grouping geospatial points within radius bands, identifying latency-adjacent requests in observability systems, or counting price/order pairs inside tolerance windows in trading infrastructure. At scale, brute force collapses under quadratic growth and becomes operationally irrelevant. Sorting plus a monotonic scan turns pairwise comparison into a range-counting problem, which is exactly the kind of reframing that keeps batch jobs bounded, dashboards responsive, and online services predictable under high cardinality inputs.

## 🔍 Problem Statement
You are given:

- `positions`: an integer array of station locations along a warehouse aisle
- `maxDistance`: a non-negative integer threshold

Count the number of distinct pairs `(i, j)` such that `i < j` and:

`|positions[i] - positions[j]| <= maxDistance`

Return that count. The array is not sorted. Constraints are large enough that an `O(n^2)` nested-loop solution may time out:

- `1 <= positions.length <= 200000`
- `-10^9 <= positions[i] <= 10^9`
- `0 <= maxDistance <= 10^9`

The result may be as large as `n * (n - 1) / 2`, so use a 64-bit integer type.

Examples:

- `positions = [8, 1, 4, 10, 6], maxDistance = 3` → `4`
- `positions = [5, 5, 5, 9], maxDistance = 0` → `3`

The key constraint is input size: once `n` reaches hundreds of thousands, only near-linear work after sorting is viable.

## 🪜 How to Solve This
1. Start with the condition `|a - b| <= maxDistance` → absolute differences are awkward in arbitrary order, but become simple after sorting.

2. Sort the positions → now for any `i < j`, the distance is just `positions[j] - positions[i]`. No need to reason about both directions.

3. Ask: for each left endpoint, how far right can I extend while staying valid? That naturally suggests a sliding window.

4. Maintain two pointers:
   - `left` marks the start of the current valid window
   - `right` expands monotonically through the array

5. When `positions[right] - positions[left] > maxDistance`, move `left` forward until the window is valid again.

6. Once the window is valid, every index between `left` and `right - 1` forms a valid pair with `right`. That means you can add `right - left` pairs at once instead of checking each individually.

7. The core insight: after sorting, validity is contiguous. For a fixed `right`, valid partners form one continuous block, which is exactly why two pointers works.

## 🧩 Algorithm Walkthrough
1. **Sort the array.**  
   This transforms the absolute-difference condition into an ordered distance check: for `i < j`, `|positions[j] - positions[i]|` becomes `positions[j] - positions[i]`. The invariant after sorting is that values are non-decreasing, so widening the right side never decreases distance.

2. **Initialize `left = 0` and `count = 0`.**  
   We will iterate `right` from `0` to `n - 1`. The pattern here is **Two Pointers / Sliding Window**: maintain the smallest valid left boundary for each right boundary.

3. **For each `right`, restore window validity.**  
   While `positions[right] - positions[left] > maxDistance`, increment `left`. This is correct because any index smaller than the new `left` is even farther from `right` and therefore also invalid. The invariant becomes: all indices in `[left, right]` satisfy the distance constraint relative to `right`.

4. **Count pairs ending at `right`.**  
   Once valid, every index `k` in `[left, right - 1]` forms a valid pair `(k, right)`. There are exactly `right - left` such indices, so add that to `count`.

5. **Exploit monotonicity.**  
   `left` never moves backward, and `right` only moves forward. Each element is touched a constant number of times after sorting, which is why the scan is linear.

6. **Use a 64-bit accumulator.**  
   In the worst case, all pairs are valid, producing about `n^2 / 2` pairs. With `n = 200000`, that exceeds 32-bit integer range.

## 📊 Worked Example
Example: `positions = [8, 1, 4, 10, 6]`, `maxDistance = 3`

Sorted: `[1, 4, 6, 8, 10]`

| right | value | left after shrink | valid window | pairs added | total |
|------:|------:|------------------:|-------------|------------:|------:|
| 0 | 1  | 0 | `[1]` | 0 | 0 |
| 1 | 4  | 0 | `[1, 4]` since `4-1=3` | 1 | 1 |
| 2 | 6  | 1 | `[4, 6]` since `6-1=5` invalid | 1 | 2 |
| 3 | 8  | 1 | `[4, 6, 8]` since `8-4=4` invalid → shift to 2, window `[6,8]`? Actually after shrink left=2 | 1 | 3 |
| 4 | 10 | 3 | `[8, 10]` | 1 | 4 |

Valid pairs are `(1,4)`, `(4,6)`, `(6,8)`, `(8,10)` in sorted-value terms. The important behavior is that `left` only advances when the threshold is violated, and each step counts an entire block of valid partners.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates at `O(n log n)`, followed by an `O(n)` two-pointers scan because each pointer advances monotonically and never resets. At `10^6` elements this is still operationally realistic in optimized environments; at `10^9`, even sorting becomes infeasible, so the bottleneck shifts from algorithm choice to system architecture and data partitioning.

### Space Complexity
`O(1)` extra space beyond the sort if the language/runtime supports in-place sorting; otherwise practical space is often `O(log n)` to `O(n)` depending on the sorting implementation. The scan itself uses constant auxiliary state. Reducing sort space usually means accepting implementation-specific trade-offs rather than changing the core algorithm.

## 💡 Key Takeaways
- If the condition is pairwise distance under a threshold and the array is unsorted, sorting is usually the first move to expose structure.
- If valid partners for each element become a contiguous range after sorting, that is a strong signal for Two Pointers rather than nested loops.
- Count with `right - left`, not `right - left + 1`; the current element cannot pair with itself.
- Use a 64-bit result type; the pair count can exceed 32-bit range even when individual values fit comfortably in `int`.
- The broader design lesson is to convert expensive pairwise comparison into ordered range counting whenever monotonicity can be introduced.

## 🚀 Variations & Further Practice
- Count pairs with distance in a closed interval `[L, R]` instead of only `<= R`; the twist is combining two window counts or using inclusion-exclusion over thresholds.
- Return all valid pairs rather than just the count; the conceptual difficulty is output size, which can become quadratic even when counting is efficient.
- Extend from 1D positions to 2D points within Manhattan or Euclidean distance; sorting alone no longer linearizes the constraint, so spatial indexing or sweep-line structures become necessary.