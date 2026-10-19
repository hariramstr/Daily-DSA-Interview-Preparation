# Count Lifeboat Crews Within Capacity

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an unsorted array of worker weights and a boat capacity limit, count how many distinct pairs of indices form a valid two-person crew whose combined weight does not exceed the limit. The result must include duplicate weights as separate workers when their indices differ. The challenge is scale: with up to 200,000 workers, checking all `O(n²)` pairs is too slow, so the solution must count valid pairs more efficiently after reordering the data.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must count feasible pairings under a threshold without enumerating all combinations: ad auction candidate matching, search ranking feature joins, fraud detection on transaction pairs, capacity-aware scheduling, and warehouse packing heuristics. At production scale, brute-force pair scans collapse under quadratic growth, causing latency spikes and memory pressure. Sorting plus a monotonic two-pointer sweep converts pairwise feasibility counting into a linear pass over ordered data. That shift is architectural, not cosmetic: it enables predictable performance envelopes, simpler backpressure planning, and tractable operation on batch or near-real-time pipelines.

## 🔍 Problem Statement
You are given:

- `weights`: an integer array where `weights[i]` is the weight of the `i`-th rescue worker
- `limit`: the maximum total weight a lifeboat can carry

A valid crew consists of exactly two **different** workers such that:

- `weights[i] + weights[j] <= limit`
- `i != j`

Return the total number of distinct valid crews. Distinctness is by index pair, not by weight value, so repeated weights can generate multiple valid crews.

Constraints:

- `2 <= weights.length <= 200000`
- `1 <= weights[i] <= 10^9`
- `1 <= limit <= 2 * 10^9`
- Use a 64-bit integer for the answer if needed

Examples:

- `weights = [70, 50, 80, 40], limit = 120` → `4`
- `weights = [30, 30, 60, 90, 50], limit = 90` → `5`

The deciding constraint is input size: `n = 200000` rules out any all-pairs scan.

## 🪜 How to Solve This
1. Start with the brute-force thought: every pair of workers could form a crew. That immediately implies `O(n²)` comparisons, which is unacceptable at 200,000 elements.

2. Notice the condition depends only on the **sum** of two weights relative to a fixed threshold. That is a strong signal to sort the array so pair feasibility becomes ordered and predictable.

3. After sorting, compare the lightest remaining worker with the heaviest remaining worker.  
   → If their sum is within `limit`, then that lightest worker can pair with **every worker between them** as well, because all of those are no heavier than the current heaviest.

4. That means one comparison can count many pairs at once: if `weights[left] + weights[right] <= limit`, add `right - left` to the answer and move `left` forward.

5. Otherwise the heaviest worker is too heavy even with the lightest candidate, so it cannot pair with anyone in the current window. Move `right` leftward.

6. Keep shrinking the window until the pointers cross. This gives a sorted `O(n log n)` solution with a linear counting pass.

## 🧩 Algorithm Walkthrough
1. **Sort the array in non-decreasing order.**  
   This is what makes the feasibility relation monotonic. Once sorted, if `weights[left] + weights[right]` is valid, then `weights[left]` also pairs validly with every index in `[left + 1, right - 1]`.

2. **Initialize two pointers:** `left = 0`, `right = n - 1`, and `count = 0`.  
   The active search space is the closed interval `[left, right]`. Every valid pair counted will use indices from this window exactly once.

3. **Check the current extreme pair.**  
   This is the core **Two Pointers** pattern: use ordering to reason about many pair candidates without enumerating them.

4. **If `weights[left] + weights[right] <= limit`:**  
   Add `right - left` to `count`.  
   Why correct: with sorted order, for any `k` where `left < k <= right`, `weights[k] <= weights[right]`, so  
   `weights[left] + weights[k] <= weights[left] + weights[right] <= limit`.  
   Invariant maintained: all pairs involving the current `left` and any index up to `right` are now fully accounted for. Then increment `left`.

5. **Else (`weights[left] + weights[right] > limit`):**  
   Decrement `right`.  
   Why correct: if the lightest worker cannot pair with `weights[right]`, no one in `[left, right - 1]` can, because they are all at least as heavy as `weights[left]`. So `right` cannot participate in any valid pair within the current window.

6. **Stop when `left >= right`.**  
   At that point every possible pair has either been counted or proven impossible exactly once.

## 📊 Worked Example
Example: `weights = [70, 50, 80, 40]`, `limit = 120`  
Sorted: `[40, 50, 70, 80]`

| Step | left | right | Pair     | Sum | Action             | Count |
|------|------|-------|----------|-----|--------------------|-------|
| 1    | 0    | 3     | (40, 80) | 120 | valid, add `3`     | 3     |
| 2    | 1    | 3     | (50, 80) | 130 | invalid, `right--` | 3     |
| 3    | 1    | 2     | (50, 70) | 120 | valid, add `1`     | 4     |

Why step 1 adds `3`: once `(40, 80)` is valid, `(40, 50)` and `(40, 70)` must also be valid because those partners are lighter than `80`. After counting all pairs involving `40`, move `left` forward. The process ends when `left == right`. Final answer: `4`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates at `O(n log n)`, followed by a single `O(n)` two-pointer sweep. Overall complexity is `O(n log n)`. At `10^6` elements this is still practical in optimized runtimes; at `10^9`, even linear scanning is already infeasible, so the problem shifts from algorithm choice to system-level partitioning and external-memory strategy.

### Space Complexity
Space is `O(1)` auxiliary if sorting can be done in place, excluding the sort implementation’s internal stack or buffer costs. In languages with non-in-place library sorts, effective space may be `O(n)`. Reducing space usually means accepting a different sort implementation or mutating input.

## 💡 Key Takeaways
- If the condition is “count pairs with sum under a threshold,” and the input is unsorted, sorting plus a two-pointer sweep should be your first instinct.
- When one successful comparison lets you count an entire range of pairs at once, you are looking for a monotonic structure, not pair enumeration.
- Use a 64-bit accumulator: the number of valid pairs can be as large as `n * (n - 1) / 2`, which overflows 32-bit integers.
- The key off-by-one detail is adding `right - left`, not `right - left + 1`, because a worker cannot pair with itself.
- In production systems, ordered data often turns combinatorial counting into linear scans; the real win is exploiting monotonicity, not just “using two pointers.”

## 🚀 Variations & Further Practice
- Count pairs whose sum lies in a range `[L, R]`: same sorting foundation, but now you need two boundary counts or binary-search/two-pointer hybrids.
- Count triplets with total weight within capacity: extends the pattern by fixing one index and running two pointers on the suffix, increasing complexity and duplicate-handling risk.
- Return the actual pairs or top-k feasible pairs instead of just the count: shifts the problem from aggregate counting to output-sensitive enumeration and memory-aware traversal.