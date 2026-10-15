# Count Compatible Ski Lift Pairs

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an unsorted array of guest heights and a threshold `maxGap`, count how many distinct index pairs `(i, j)` with `i < j` satisfy `|heights[i] - heights[j]| <= maxGap`. The result is a single integer count, not the pairs themselves. The challenge is scale: with up to `2 * 10^5` guests, checking every pair is too slow, so the solution must count many valid pairs at once after imposing order on the data.

## 🌍 Engineering Impact
This pattern shows up anywhere you need to count relationships inside a bounded numeric window: matching riders by weight or height in transportation systems, correlating events within latency thresholds in streaming pipelines, grouping near-duplicate embeddings in search and recommendation systems, or identifying transactions within tolerance bands in fraud detection. At small scale, brute force survives; at production scale, it collapses under quadratic growth. Sorting plus a sliding window turns pairwise comparison into range counting, which is the difference between a batch job that finishes and one that starves CPU, memory bandwidth, and downstream capacity.

## 🔍 Problem Statement
You are given:

- `heights`: an integer array where `1 <= heights.length <= 2 * 10^5`
- `maxGap`: an integer where `0 <= maxGap <= 10^9`

Count the number of distinct pairs `(i, j)` such that:

- `i < j`
- `|heights[i] - heights[j]| <= maxGap`

Return the total number of valid pairs. Since the number of pairs can be large, use a 64-bit integer type where required.

Examples:

- `heights = [150, 160, 153, 168]`, `maxGap = 5` → `1`
- `heights = [172, 175, 171, 169, 174]`, `maxGap = 3` → `8`

Edge cases matter:

- Single-element arrays return `0`
- `maxGap = 0` means only equal heights can pair
- Large duplicate groups can produce very large counts

The deciding constraint is `n = 2 * 10^5`: `O(n^2)` pair enumeration is not viable.

## 🪜 How to Solve This
1. Read the condition carefully → it depends only on the difference between two values, not on original positions except for avoiding double-counting with `i < j`.

2. Difference-based pair constraints usually become easier after sorting → once heights are ordered, absolute difference simplifies to `heights[right] - heights[left]`.

3. After sorting, think in terms of a valid window → for a fixed `right`, every index between `left` and `right - 1` is valid if the window satisfies the gap constraint.

4. That suggests Two Pointers → move `right` forward one step at a time, and only move `left` when the gap becomes too large.

5. Why this works → in sorted order, if `heights[right] - heights[left] <= maxGap`, then every element between them is also within `maxGap` of `heights[right]`.

6. So for each `right`, once `left` is adjusted, you can add `right - left` valid pairs in one shot instead of checking each pair individually.

7. The key optimization insight is monotonicity → both pointers move only forward, so the scan after sorting is linear.

## 🧩 Algorithm Walkthrough
1. **Sort the array**  
   This is the enabling transformation. In sorted order, for any `left < right`, the absolute difference becomes `heights[right] - heights[left]`. That removes symmetry and makes the constraint directional and easy to maintain.

2. **Initialize two pointers and an accumulator**  
   Set `left = 0`, and iterate `right` from `0` to `n - 1`. Maintain a 64-bit `count`. The invariant is: before counting for a given `right`, `left` is the smallest index such that all elements in `[left, right]` fit within `maxGap` from `heights[right]`.

3. **Shrink the window when invalid**  
   While `heights[right] - heights[left] > maxGap`, increment `left`. This restores validity. Because the array is sorted, any index before `left` is guaranteed to be invalid for this `right`, so discarding it is correct.

4. **Count pairs in bulk**  
   Once the window is valid, every index `k` in `[left, right - 1]` forms a valid pair `(k, right)`. There are exactly `right - left` such indices, so add that to `count`.

5. **Advance `right` and repeat**  
   This is the classic **Two Pointers / Sliding Window on sorted data** pattern. It is the right abstraction because the valid region is contiguous after sorting, and the window boundaries move monotonically. No pointer ever moves backward, which keeps the post-sort scan linear.

## 📊 Worked Example
Example: `heights = [172, 175, 171, 169, 174]`, `maxGap = 3`  
Sorted: `[169, 171, 172, 174, 175]`

| right | height[right] | left after shrink | valid window | pairs added | total |
|---|---:|---:|---|---:|---:|
| 0 | 169 | 0 | [169] | 0 | 0 |
| 1 | 171 | 0 | [169, 171] | 1 | 1 |
| 2 | 172 | 0 | [169, 171, 172] | 2 | 3 |
| 3 | 174 | 1 | [171, 172, 174] | 2 | 5 |
| 4 | 175 | 1 | [171, 172, 174, 175] | 3 | 8 |

Trace notes:

1. At `right = 3`, `174 - 169 = 5`, which exceeds `3`, so `left` moves from `0` to `1`.
2. After that adjustment, all indices from `1` to `2` pair with `174`.
3. At `right = 4`, the window remains valid, so `175` pairs with `171`, `172`, and `174`.

Final answer: `8`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` due to sorting, followed by an `O(n)` two-pointer scan. The sort dominates asymptotically. At `10^6` elements this is still practical in most environments; at `10^9`, even linear scanning is already beyond typical single-node memory and runtime budgets, so the problem becomes architectural, not algorithmic.

### Space Complexity
`O(1)` auxiliary space beyond the sort if the language/runtime supports in-place sorting; otherwise `O(n)` depending on the sorting implementation. The counting logic itself uses constant extra space. Reducing sort space usually means accepting implementation-specific trade-offs rather than changing the core algorithm.

## 💡 Key Takeaways
- If the condition is based on pairwise numeric distance and asks for a count, sorting plus a contiguous validity window should be your first mental model.
- When a valid set for each endpoint becomes a continuous range after sorting, Two Pointers usually lets you count in bulk instead of enumerating pairs.
- Use `while`, not `if`, when shrinking `left`; a single increment may not restore the window to validity.
- Add `right - left`, not `right - left + 1`; the current element does not pair with itself.
- The production-grade insight is to convert expensive all-to-all comparison into ordered range maintenance, which is a recurring strategy in scalable data systems.

## 🚀 Variations & Further Practice
- Count pairs whose sum is less than or equal to a threshold: same sorted two-pointer foundation, but the counting rule depends on sums rather than differences.
- Count triplets with bounded max-min distance: extends the window idea, but combinatorics inside each valid window become `C(windowSize, 3)`.
- Online version with inserts and queries: sorting is no longer enough; you need balanced trees, Fenwick trees, or order-statistics structures to maintain counts incrementally.