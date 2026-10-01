# Count Apartment Pairs Within Noise Difference

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an array `noise` and an integer `limit`, count how many distinct index pairs `(i, j)` with `i < j` satisfy `|noise[i] - noise[j]| <= limit`. The output is a 64-bit integer because the number of valid pairs can be large. The non-trivial part is scale: with up to `200000` apartments, checking all `O(n^2)` pairs is too slow, so the solution must aggregate many valid pairs in one pass after ordering the data.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to count “close enough” pairs under a numeric threshold: matching nearby timestamps in streaming pipelines, grouping latency samples in observability backends, identifying near-duplicate scores in ranking systems, or correlating events in fraud detection windows. At production scale, brute-force pairwise comparison collapses under quadratic growth and creates avoidable CPU hotspots. Sorting plus a monotonic scan turns an intractable comparison problem into a predictable batch operation with strong cache behavior. That shift matters architecturally: it enables offline analytics, bounded-latency preprocessing, and simpler reasoning about worst-case performance.

## 🔍 Problem Statement
You are given:

- `noise`, an array where `noise[i]` is the nighttime noise level of apartment `i`
- `limit`, the maximum allowed absolute difference for two apartments to be considered compatible

Return the total number of distinct pairs `(i, j)` such that:

- `0 <= i < j < n`
- `|noise[i] - noise[j]| <= limit`

Constraints:

- `1 <= noise.length <= 200000`
- `0 <= noise[i] <= 1000000000`
- `0 <= limit <= 1000000000`

The answer may exceed 32-bit range, so use a 64-bit integer.

Examples:

- `noise = [12, 7, 10, 15], limit = 3` → `4`
- `noise = [4, 4, 4, 9], limit = 0` → `3`

The key constraint is `n = 200000`: an `O(n^2)` pair scan is not viable, which forces a subquadratic strategy.

## 🪜 How to Solve This
1. Start from the condition `|a - b| <= limit` → absolute differences are awkward on unsorted data because every element can match both smaller and larger values.

2. Sort the array → once values are ordered, the absolute value disappears for `i < j`, because `noise[j] - noise[i]` is non-negative.

3. After sorting, the question becomes: for each left endpoint `i`, how far right can we extend while keeping `noise[r] - noise[i] <= limit`?

4. That immediately suggests a sliding window / Two Pointers approach → maintain a window `[left, right]` where all elements from `left+1` through `right-1` form valid pairs with `left`.

5. The key observation is batching: if the farthest valid index for `left` is `right - 1`, then `left` contributes exactly `right - left - 1` pairs. No need to enumerate them individually.

6. Because the array is sorted, `right` never needs to move backward when `left` advances. That monotonicity is what collapses the scan to linear time after sorting.

7. Use a 64-bit accumulator, because in the worst case the count is roughly `n(n-1)/2`.

## 🧩 Algorithm Walkthrough
1. **Sort the array ascending.**  
   This is the enabling transformation. After sorting, for any `left < right`, we only need to check `noise[right] - noise[left] <= limit`. The ordering removes the need to reason about both directions of the absolute difference.

2. **Initialize two pointers: `left = 0`, `right = 0`, and `count = 0`.**  
   The pattern here is **Two Pointers / Sliding Window on sorted data**. The invariant is that `right` is always the first index that is *outside* the valid range for the current `left`, or `n` if all remaining elements are valid.

3. **For each `left` from `0` to `n - 1`, advance `right` while the pair remains valid.**  
   Specifically, while `right < n` and `noise[right] - noise[left] <= limit`, increment `right`. This finds the maximal contiguous suffix of values compatible with `noise[left]`.

4. **Add the number of valid partners for `left`.**  
   Once the loop stops, valid indices are `[left + 1, right - 1]`, so the contribution is `right - left - 1`. This is correct because every element in that interval differs from `noise[left]` by at most `limit`, and everything at `right` or beyond does not.

5. **Advance `left` and keep `right` monotonic.**  
   Never reset `right` backward. Since the array is sorted, increasing `left` can only relax or preserve the lower bound on valid `right`, never require revisiting earlier positions. This invariant is what makes the scan `O(n)` after sorting.

6. **Return `count` as a 64-bit integer.**  
   In dense valid ranges, the number of pairs grows quadratically even though the algorithm does not.

## 📊 Worked Example
Example: `noise = [12, 7, 10, 15]`, `limit = 3`

Sorted: `[7, 10, 12, 15]`

| left | noise[left] | right after expansion | valid partners | pairs added | total |
|---|---:|---:|---:|---:|---:|
| 0 | 7  | 2 | indices 1..1 | 1 | 1 |
| 1 | 10 | 4 | indices 2..3 | 2 | 3 |
| 2 | 12 | 4 | index 3..3   | 1 | 4 |
| 3 | 15 | 4 | none          | 0 | 4 |

Trace:

1. From `7`, only `10` is within distance `3`; `12` is too far.  
2. From `10`, both `12` and `15` are valid.  
3. From `12`, only `15` is valid.  
4. From `15`, no later elements exist.

Final answer: `4`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting costs `O(n log n)`, and the two-pointer scan is `O(n)` because each pointer advances at most `n` times. Overall complexity is `O(n log n)`. At `10^6` elements this is still practical in native code; at `10^9`, even sorting becomes a system-level problem rather than an algorithmic one.

### Space Complexity
Space is `O(1)` auxiliary if sorting in place is allowed, excluding the sort implementation’s stack/runtime overhead. If the environment requires copying before sort, effective space becomes `O(n)`. Reducing space usually means mutating input, which may not be acceptable in shared-data pipelines.

## 💡 Key Takeaways
- If the condition is based on pairwise numeric distance and asks for a count rather than the pairs themselves, sorting plus a window is a strong first candidate.
- When valid matches form a contiguous range after sorting, think Two Pointers: monotonic structure usually means linear scan after preprocessing.
- Count with `right - left - 1`, not `right - left`; including `left` itself is the classic off-by-one error here.
- Use a 64-bit accumulator even if each `noise[i]` fits in 32 bits; the pair count can overflow long before values do.
- The production lesson is broader than this problem: when a predicate becomes interval-shaped after ordering, you can replace expensive pair enumeration with boundary tracking.

## 🚀 Variations & Further Practice
- Count pairs with difference in a range `[L, R]` instead of just `<= limit`; the twist is combining two window counts or using inclusion-exclusion over sorted boundaries.
- Count triplets where `max - min <= limit`; same sorted-window idea, but now each window contributes combinatorially with `C(window_size, 3)`.
- Process online inserts and queries for “how many prior values are within `limit`”; the harder part is replacing offline sorting with balanced trees, Fenwick trees, or coordinate-compressed order statistics.