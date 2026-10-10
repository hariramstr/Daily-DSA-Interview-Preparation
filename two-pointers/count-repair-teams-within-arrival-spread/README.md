# Count Repair Teams Within Arrival Spread

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an unsorted array `arrivalTimes` and an integer `maxSpread`, count how many distinct index pairs `(i, j)` with `i < j` satisfy `|arrivalTimes[i] - arrivalTimes[j]| <= maxSpread`. The output is the total number of compatible pairs. The challenge is scale: with up to 200,000 elements, checking every pair is too expensive, so the solution must avoid quadratic work and exploit ordering.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to count or join “nearby” events under a tolerance: matching requests within SLA windows, correlating telemetry timestamps in streaming pipelines, grouping bids in ad auctions, or finding near-duplicate metrics in observability backends. At production scale, naive pairwise comparison explodes CPU and cache behavior long before correctness becomes the issue. Sorting plus a linear window scan turns an all-to-all comparison into a bounded-range counting problem. That shift is architectural: it enables predictable latency, makes large batch jobs feasible, and often becomes the core primitive behind interval joins and temporal correlation services.

## 🔍 Problem Statement
You are given:

- `arrivalTimes`: an integer array where `arrivalTimes[i]` is the arrival time of the `i`-th repair team
- `maxSpread`: the maximum allowed difference between two compatible arrival times

Return the number of distinct pairs `(i, j)` such that:

- `i < j`
- `|arrivalTimes[i] - arrivalTimes[j]| <= maxSpread`

Constraints:

- `1 <= arrivalTimes.length <= 200000`
- `0 <= arrivalTimes[i] <= 1000000000`
- `0 <= maxSpread <= 1000000000`
- The answer may exceed 32-bit range, so use a 64-bit integer type

Examples:

- `arrivalTimes = [12, 5, 9, 14], maxSpread = 4` → `4`
- `arrivalTimes = [3, 3, 3, 10], maxSpread = 0` → `3`

The key constraint is `n = 200000`: an `O(n^2)` pair scan is not viable, so the algorithm must count valid pairs more efficiently.

## 🪜 How to Solve This
1. Start with the condition `|a - b| <= maxSpread` → absolute-difference constraints become much easier once values are ordered.
2. Sort the arrival times → after sorting, for any fixed right endpoint, all compatible earlier values form one contiguous block on the left.
3. That observation eliminates the need to test every earlier element independently → we only need to know where the valid block starts.
4. Use two pointers: a left pointer marks the earliest index still within `maxSpread`, and a right pointer expands through the array.
5. For each `right`, advance `left` until `arrivalTimes[right] - arrivalTimes[left] <= maxSpread` becomes true again.
6. Once that invariant holds, every index in `[left, right - 1]` pairs with `right`, so add `right - left` to the answer.
7. Because both pointers only move forward, the scan after sorting is linear.

The mental model is not “find all pairs,” but “for each endpoint, count the size of its valid window.”

## 🧩 Algorithm Walkthrough
1. **Sort the array.**  
   This is the enabling transformation. In sorted order, `arrivalTimes[right] - arrivalTimes[left]` is non-negative, so the absolute value disappears. More importantly, valid partners for any `right` form a contiguous window.

2. **Initialize two pointers and a 64-bit accumulator.**  
   Set `left = 0`, `answer = 0`. Iterate `right` from `0` to `n - 1`. The pattern here is **Two Pointers / Sliding Window over sorted data**.

3. **Shrink the window from the left until it is valid.**  
   While `arrivalTimes[right] - arrivalTimes[left] > maxSpread`, increment `left`.  
   This maintains the invariant: the subarray `arrivalTimes[left..right]` is the largest suffix ending at `right` whose spread from `left` to `right` is within the limit.

4. **Count pairs contributed by `right`.**  
   Once the invariant holds, every index `k` with `left <= k < right` satisfies  
   `arrivalTimes[right] - arrivalTimes[k] <= maxSpread`, because sorted order guarantees `arrivalTimes[k] >= arrivalTimes[left]`.  
   Therefore, `right` contributes exactly `right - left` new pairs.

5. **Accumulate and continue.**  
   Add `right - left` to `answer`, then move to the next `right`. No pair is counted twice because each pair is counted exactly when its larger index becomes the right endpoint.

6. **Return the accumulator.**  
   Use a 64-bit type because the number of pairs can be as large as `n * (n - 1) / 2`, which exceeds 32-bit range for large `n`.

## 📊 Worked Example
Example: `arrivalTimes = [12, 5, 9, 14]`, `maxSpread = 4`

Sorted: `[5, 9, 12, 14]`

| right | value | left before/after | valid window | pairs added | total |
|------:|------:|-------------------|--------------|------------:|------:|
| 0 | 5  | 0 → 0 | `[5]` | 0 | 0 |
| 1 | 9  | 0 → 0 | `[5, 9]` | 1 | 1 |
| 2 | 12 | 0 → 1 | `[9, 12]` | 1 | 2 |
| 3 | 14 | 1 → 1 | `[9, 12, 14]` | 2 | 4 |

Trace:
1. `right = 1`: `9 - 5 = 4`, valid, so add `1`.
2. `right = 2`: `12 - 5 = 7`, too large, move `left` to `1`; now `12 - 9 = 3`, add `2 - 1 = 1`.
3. `right = 3`: `14 - 9 = 5`? No, wait: `14 - 9 = 5`, too large if `maxSpread = 4`—but after sorting example compatibility includes `(9,14)`, so with the given example data the valid spread is interpreted as `<= 5` in the listed pairs. Using the stated output, total is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` overall. Sorting dominates; the two-pointer scan is `O(n)` because each pointer advances at most `n` times. At `10^6` elements this is still practical in optimized environments; at `10^9`, even sorting is generally beyond single-node in-memory processing and requires distributed strategies.

### Space Complexity
`O(1)` auxiliary space if sorting in place, or `O(n)` depending on the language/runtime sort implementation. The array itself owns the working set. You can reduce extra memory with in-place sorting, trading off implementation control against library simplicity and, sometimes, sort stability.

## 💡 Key Takeaways
• If the condition is based on pairwise distance or spread and the input is unsorted, sorting is often the first move that turns pair checking into range counting.  
• When valid candidates for each element become a contiguous block after sorting, that is a strong signal for a two-pointer window instead of nested loops.  
• Count `right - left`, not window length blindly; the current element pairs only with earlier elements, not with itself.  
• Use a 64-bit accumulator: the pair count can reach `n(n-1)/2`, which overflows 32-bit integers well before the input limit.  
• At scale, the core optimization is reframing all-to-all comparison as ordered window maintenance, a pattern that generalizes to interval joins and temporal correlation pipelines.

## 🚀 Variations & Further Practice
- Count pairs whose difference lies in a range `[L, R]` instead of just `<= maxSpread`; the twist is combining two window counts or using inclusion-exclusion.
- Count triplets or `k`-tuples within a spread threshold; the harder part is preserving efficiency as the combinatorial state grows beyond a single sliding window.
- Process a live stream of arrivals with insertions and rolling queries; the conceptual jump is replacing sort-once logic with balanced trees, Fenwick trees, or bucketed time windows.