# Count Pairs of Photos Within Brightness Budget

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an array `brightness` and an integer `budget`, count how many index pairs `(i, j)` with `i < j` satisfy `|brightness[i] - brightness[j]| <= budget`. The output is a single integer: the number of valid distinct pairs. The challenge is scale: with up to 200,000 photos, the naive all-pairs check is `O(n^2)` and becomes infeasible. The key is to transform the problem so valid pair ranges can be counted in bulk rather than tested individually.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to count near-matches under a threshold: deduplicating similar events in streaming pipelines, grouping telemetry samples within tolerance bands, matching bids and asks within spread constraints, or identifying candidate neighbors in ranking and recommendation systems. At production scale, quadratic pairwise comparison collapses under data volume, cache behavior, and latency budgets. Sorting plus a linear scan turns an intractable combinatorial search into a predictable batch primitive. That shift matters architecturally: it enables offline analytics, bounded-memory processing, and deterministic performance envelopes that are easier to capacity-plan and reason about.

## 🔍 Problem Statement
You are given an integer array `brightness` where `brightness[i]` is the brightness score of the `i`-th photo, and an integer `budget`. Return the number of distinct index pairs `(i, j)` such that:

- `0 <= i < j < n`
- `|brightness[i] - brightness[j]| <= budget`

Constraints:

- `1 <= brightness.length <= 200000`
- `0 <= brightness[i] <= 10^9`
- `0 <= budget <= 10^9`

Duplicates are allowed, and equal values may contribute many pairs. The output counts pairs of indices, not unique values.

Examples:

- `brightness = [4, 1, 7, 5], budget = 3` → `4`
- `brightness = [2, 2, 2, 8, 9], budget = 1` → `4`

The deciding constraint is `n = 200000`: `O(n^2)` pair enumeration is too slow, so the solution must count ranges of valid partners efficiently after reordering the data.

## 🪜 How to Solve This
1. Start from the brute-force view → every photo can pair with every later photo, but that is `n(n-1)/2` checks, which is unacceptable at 200k elements.

2. Notice the predicate depends only on value difference, not original position → that strongly suggests sorting, because sorted order makes “close enough” elements contiguous.

3. After sorting, the absolute value disappears for `i < j` → you only need to check whether `sorted[j] - sorted[i] <= budget`.

4. For each left endpoint `i`, ask: how far right can I extend while staying within budget? If the farthest valid index is `j - 1`, then `i` contributes exactly `(j - i - 1)` pairs.

5. Recomputing that farthest index from scratch for every `i` would still be too expensive. But the valid window only moves forward as `i` increases.

6. That monotonic movement is the signal for a two-pointer solution: maintain a sliding right pointer, expand while valid, count the window width, then advance the left pointer.

7. The result is sorting plus one linear pass: `O(n log n)` overall, with simple correctness reasoning and strong practical performance.

## 🧩 Algorithm Walkthrough
1. **Sort the array.**  
   This is the enabling transformation. Once `brightness` is sorted, for any `i < j`, we know `sorted[j] >= sorted[i]`, so the condition becomes `sorted[j] - sorted[i] <= budget`. Valid partners for a fixed `i` form one contiguous suffix-free interval, not scattered positions.

2. **Initialize two pointers: `left = 0`, `right = 0`.**  
   This is the classic **Two Pointers** pattern: maintain a window `[left, right)` such that every index in that window after `left` is currently a valid partner candidate for `left`.

3. **Expand `right` while the budget holds.**  
   For each `left`, move `right` forward as long as `right < n` and `sorted[right] - sorted[left] <= budget`. Because the array is sorted, once this fails, no larger index can work for the same `left`.

4. **Count pairs contributed by `left`.**  
   All indices from `left + 1` through `right - 1` are valid partners, so add `right - left - 1` to the answer. This is correct because each pair is counted exactly once, anchored at its smaller index.

5. **Advance `left` and preserve the invariant.**  
   Increment `left` and repeat. `right` never moves backward. The invariant is: before counting for a given `left`, `right` is the first index outside the valid range for that `left`.

6. **Use a 64-bit accumulator if needed.**  
   The number of pairs can be as large as `n(n-1)/2`, which exceeds 32-bit integer range for `n = 200000`.

This abstraction is right because the problem asks for counts over a monotonic validity region in sorted order, exactly where two pointers outperform repeated binary searches or nested scans.

## 📊 Worked Example
Example: `brightness = [4, 1, 7, 5]`, `budget = 3`

Sorted array: `[1, 4, 5, 7]`

| left | value | right after expansion | valid partners | pairs added | total |
|------|-------|------------------------|----------------|-------------|-------|
| 0    | 1     | 2                      | indices 1..1   | 1           | 1     |
| 1    | 4     | 4                      | indices 2..3   | 2           | 3     |
| 2    | 5     | 4                      | indices 3..3   | 1           | 4     |
| 3    | 7     | 4                      | none           | 0           | 4     |

Trace:

1. From `1`, only `4` is within budget; `5` is too far.
2. From `4`, both `5` and `7` are within budget.
3. From `5`, `7` is within budget.
4. Final count is `4`, matching the valid original-value pairs: `(4,1)`, `(4,7)`, `(4,5)`, `(7,5)`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates at `O(n log n)`, followed by a linear `O(n)` two-pointer scan because each pointer advances at most `n` times. At `10^6` elements this is still practical in optimized environments; at `10^9`, even linear memory movement becomes the real bottleneck, so the problem shifts from algorithm choice to system design.

### Space Complexity
The scan itself uses `O(1)` auxiliary state beyond the sorted array. If sorting is done in place, extra space is minimal; otherwise, copying the array costs `O(n)`. Reducing that copy trades memory for mutation of the input buffer.

## 💡 Key Takeaways
- If the pair condition depends only on value distance and not original order, sorting is usually the first useful reduction.
- When valid partners for each position form a contiguous range that only moves forward, reach for Two Pointers rather than nested loops.
- Count `right - left - 1`, not `right - left`; the left element cannot pair with itself.
- Use a 64-bit result type: the pair count can exceed 32-bit range even when each input value fits comfortably in `int`.
- At scale, the win is not just asymptotic elegance; it is converting pairwise comparison into a monotonic scan with predictable latency and memory behavior.

## 🚀 Variations & Further Practice
- Count pairs with difference in a closed interval `[L, R]`; the twist is combining two monotonic counts, typically `count(<= R) - count(< L)`.
- Count cross-array pairs between two sorted lists within a threshold; the harder part is preserving linear progress across two independent sequences.
- Return all valid pairs instead of just the count; the conceptual shift is that output size can become `O(n^2)`, so no algorithm can avoid that worst-case cost.