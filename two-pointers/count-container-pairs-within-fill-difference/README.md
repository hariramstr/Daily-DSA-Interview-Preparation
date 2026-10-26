# Count Container Pairs Within Fill Difference

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an array of container fill percentages and a threshold `k`, count how many index pairs `(i, j)` with `i < j` satisfy `|fills[i] - fills[j]| <= k`. The output is a count, not the pairs themselves. The challenge is scale: with up to `2 * 10^5` elements, checking all `O(n^2)` pairs is too slow, so the solution must aggregate valid pairs efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to count near-matches under a tolerance without materializing every comparison: balancing shards by load, grouping latency samples within SLO bands, pairing bids and asks in trading systems, deduplicating near-equal telemetry values, or identifying documents with close ranking scores. At production scale, naive pairwise comparison explodes quadratically in CPU and memory pressure. Sorting plus a linear scan turns an infeasible all-to-all comparison into a predictable pipeline stage, enabling batch analytics, online admission checks, and precomputation jobs to stay within latency and cost budgets.

## 🔍 Problem Statement
You are given:

- `fills`: an integer array where `fills[i]` is the fill level of the `i`-th container
- `k`: a non-negative integer threshold

Return the number of pairs `(i, j)` such that:

- `i < j`
- `|fills[i] - fills[j]| <= k`

Constraints:

- `1 <= fills.length <= 2 * 10^5`
- `0 <= fills[i] <= 10^9`
- `0 <= k <= 10^9`
- The answer may exceed 32-bit range, so use a 64-bit integer type

Examples:

- `fills = [10, 13, 15, 19], k = 3` → `2`
- `fills = [7, 7, 8, 12, 13], k = 1` → `4`

The key constraint is input size. `O(n^2)` pair enumeration is not viable at `2 * 10^5`; the algorithm must avoid explicit comparison of every pair.

## 🪜 How to Solve This
1. Start with the condition `|fills[i] - fills[j]| <= k` → absolute-difference constraints usually become easier after sorting.
2. Sort the array → once values are ordered, `|a - b|` for `a <= b` becomes just `b - a`.
3. After sorting, valid pairs form contiguous windows → if `fills[right] - fills[left] <= k`, then every element between them is also close enough to `fills[right]`.
4. That suggests a sliding window / Two Pointers approach → maintain the largest valid range ending at each `right`.
5. For each `right`, move `left` forward until the window is valid again.
6. Once valid, all indices from `left` to `right - 1` pair with `right` → add `right - left` in one shot instead of checking each pair.
7. Because both pointers only move forward, the scan after sorting is linear.
8. Use a 64-bit accumulator → in the worst case, the number of pairs is about `n(n-1)/2`, which exceeds 32-bit limits.

The key insight is not “find pairs,” but “count an entire valid range at once.”

## 🧩 Algorithm Walkthrough
1. **Sort `fills` in non-decreasing order.**  
   This transforms the absolute-difference condition into a one-sided range check: for `left < right`, validity is `fills[right] - fills[left] <= k`. Sorting is what makes the window structure exist.

2. **Initialize two pointers and an accumulator.**  
   Set `left = 0`, and iterate `right` from `0` to `n - 1`. Maintain `count` as a 64-bit integer. The pattern here is **Two Pointers / Sliding Window** over a sorted array.

3. **Restore the window invariant.**  
   While `fills[right] - fills[left] > k`, increment `left`.  
   Invariant: after this loop, the window `[left, right]` is the maximal valid window ending at `right`, meaning every value inside differs from `fills[right]` by at most `k`.

4. **Count all new pairs ending at `right`.**  
   Once the invariant holds, every index `i` in `[left, right - 1]` forms a valid pair `(i, right)`. There are exactly `right - left` such indices, so add that to `count`.

5. **Why this is correct.**  
   Sorting guarantees monotonicity: if the smallest value in the window is within `k` of `fills[right]`, then every larger value in the window is also within `k`. Conversely, anything left of `left` is too small and therefore invalid.

6. **Why this is efficient.**  
   `right` advances `n` times; `left` also advances at most `n` times total. So the scan is linear after the sort, with no backtracking and no nested pair enumeration.

## 📊 Worked Example
Example: `fills = [7, 7, 8, 12, 13]`, `k = 1`

Sorted array is the same.

| right | fills[right] | left after shrink | valid window | pairs added | total |
|---|---:|---:|---|---:|---:|
| 0 | 7  | 0 | [7] | 0 | 0 |
| 1 | 7  | 0 | [7, 7] | 1 | 1 |
| 2 | 8  | 0 | [7, 7, 8] | 2 | 3 |
| 3 | 12 | 3 | [12] | 0 | 3 |
| 4 | 13 | 3 | [12, 13] | 1 | 4 |

Trace:

1. At `right = 1`, pair `(7, 7)` is valid.
2. At `right = 2`, both earlier `7`s pair with `8`.
3. At `right = 3`, differences from `7` and `8` exceed `1`, so `left` advances to `3`.
4. At `right = 4`, only `12` pairs with `13`.

Final answer: `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` overall: `O(n log n)` for sorting and `O(n)` for the two-pointer scan. Sorting dominates. At `10^6` elements this is still practical in optimized runtimes; at `10^9`, even reading and sorting the input is operationally unrealistic, so the bottleneck becomes system architecture, not just asymptotics.

### Space Complexity
`O(1)` auxiliary space beyond the sort if the language/runtime supports in-place sorting semantics; otherwise practical space is driven by the sorting implementation, often `O(log n)` to `O(n)`. You can’t remove the need to reorder values without giving up the linear scan that makes counting efficient.

## 💡 Key Takeaways
- If the condition is based on pairwise distance or difference and you only need a count, sorting often converts pair validation into a contiguous-window problem.
- When valid partners for each element form a monotonic range, Two Pointers is usually the right abstraction instead of nested loops or binary search per element.
- The count added at each step is `right - left`, not window size `right - left + 1`; the current element does not pair with itself.
- Use a 64-bit accumulator even if individual values fit in 32 bits; the pair count can grow to `n(n-1)/2`.
- At scale, the design lesson is to replace explicit pair generation with range aggregation whenever the data can be ordered to expose monotonic structure.

## 🚀 Variations & Further Practice
- Count pairs with difference in a range `[L, R]` instead of just `<= k` — harder because you typically compute two cumulative counts and subtract them.
- Count cross-array compatible pairs between two container fleets — same sorted-window idea, but now the pointers move across different arrays.
- Count triplets where `max - min <= k` — same monotonic-window intuition, but combinatorics and window accounting become more subtle.