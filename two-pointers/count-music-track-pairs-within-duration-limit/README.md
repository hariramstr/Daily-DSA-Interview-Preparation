# Count Music Track Pairs Within Duration Limit

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an array of track durations and a session limit, count how many distinct index pairs `(i, j)` with `i < j` satisfy `durations[i] + durations[j] <= limit`. You only need the count, not the pairs themselves. The challenge is scale: with up to `2 * 10^5` tracks, the obvious nested-loop approach is too slow. The key observation is that sorting creates order, and order lets two pointers count whole ranges of valid pairs in one step.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to count feasible pairings under a threshold without enumerating all combinations: ad-serving budget matching, search ranking candidate pruning, warehouse shipment bundling, media playlist/session packing, and stream-processing joins with bounded sums. At scale, `O(n^2)` pair evaluation is not just slow; it is operationally impossible because it explodes CPU, cache misses, and often downstream memory pressure if intermediate pairs are materialized. Sorting plus two pointers converts exhaustive comparison into monotonic scanning, enabling predictable latency, lower compute cost, and simpler capacity planning for large batch or nearline workloads.

## 🔍 Problem Statement
You are given:

- `durations`: an integer array where `durations[i]` is the length of the `i`-th music track in seconds
- `limit`: an integer session limit in seconds

Return the number of distinct pairs `(i, j)` such that:

- `i < j`
- `durations[i] + durations[j] <= limit`

Constraints:

- `1 <= durations.length <= 2 * 10^5`
- `1 <= durations[i] <= 10^9`
- `1 <= limit <= 2 * 10^9`

The answer can be large, so use a 64-bit integer type.

Examples:

- `durations = [120, 90, 150, 60], limit = 210` → `4`
- `durations = [40, 40, 40, 100], limit = 80` → `3`

The algorithmic pressure comes from input size: `O(n^2)` pair checking is too expensive, so the solution must exploit structure after sorting.

## 🪜 How to Solve This
1. Start with the brute-force mental model → every pair must satisfy a sum constraint. That immediately suggests pairwise comparison, but `n = 2 * 10^5` makes `O(n^2)` unacceptable.

2. Notice the condition depends only on values, not original positions → reordering the array does not change how many value-pairs are valid. That gives permission to sort.

3. After sorting, the smallest and largest remaining values tell you something global:
   - If `durations[left] + durations[right] <= limit`, then pairing `left` with anything between `left+1` and `right` also works.
   - If that sum is too large, the largest value at `right` cannot pair with `left`, and certainly cannot pair with anything larger.

4. That monotonic behavior suggests the **Two Pointers** pattern:
   - one pointer at the smallest value
   - one pointer at the largest value

5. Move inward based on the sum:
   - valid sum → count an entire block of pairs at once, then advance `left`
   - invalid sum → shrink from the right by decrementing `right`

6. This turns repeated local checks into a linear scan after sorting.

## 🧩 Algorithm Walkthrough
1. **Sort the array in nondecreasing order.**  
   This is what makes the problem monotonic. Once sorted, increasing the left pointer only increases the left value, and decreasing the right pointer only decreases the right value. That ordered structure is what makes **Two Pointers** the right abstraction.

2. **Initialize `left = 0`, `right = n - 1`, and `count = 0`.**  
   The active search space is the closed interval `[left, right]`. The invariant is that all pairs fully outside this interval have already been classified and counted or discarded.

3. **Check `durations[left] + durations[right]`.**  
   - If the sum is `<= limit`, then every index `k` in `[left + 1, right]` forms a valid pair with `left`, because `durations[k] <= durations[right]`.  
   - Therefore, add `right - left` to `count`.

4. **Advance `left` after a valid block count.**  
   Why correct: once all pairs involving the current `left` are counted, there is nothing left to do with that element. The invariant remains intact because those pairs are now fully resolved.

5. **Otherwise, decrement `right`.**  
   If `durations[left] + durations[right] > limit`, then the current `right` cannot pair with `left`, and cannot pair with any element to the right of `left` that is larger or equal. So keeping `right` is pointless; move it inward.

6. **Repeat until `left >= right`.**  
   At termination, every possible pair has been considered exactly once through block counting or elimination. No duplicates are counted because each pair is associated with exactly one left endpoint when that endpoint is processed.

## 📊 Worked Example
Example: `durations = [120, 90, 150, 60]`, `limit = 210`

After sorting: `[60, 90, 120, 150]`

| Step | left | right | Values      | Sum | Action            | Count |
|------|------|-------|-------------|-----|-------------------|-------|
| 1    | 0    | 3     | 60, 150     | 210 | valid → add 3     | 3     |
| 2    | 1    | 3     | 90, 150     | 240 | too large → r--   | 3     |
| 3    | 1    | 2     | 90, 120     | 210 | valid → add 1     | 4     |

Stop because `left == right`.

Why step 1 adds `3`: once `60 + 150` fits, then `60 + 120` and `60 + 90` also fit because those partners are smaller than `150`. That is the core optimization: one comparison certifies multiple pairs.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` due to sorting, followed by an `O(n)` two-pointer scan. Sorting dominates. At `10^6` elements this is still practical in optimized environments; at `10^9`, even linear memory movement is already a systems problem, so the algorithm is asymptotically right but operationally constrained by hardware.

### Space Complexity
`O(1)` auxiliary space if sorting is in-place apart from implementation-specific stack/runtime overhead; otherwise `O(n)` depending on the language’s sort. The two-pointer scan itself uses constant extra memory. Reducing space further is not meaningful unless external or streaming sort becomes necessary.

## 💡 Key Takeaways
- If the condition is pairwise and depends only on values, not original positions, sorting is often the first move to unlock monotonic structure.
- When one successful comparison implies a whole contiguous range of other successes, that is a strong signal for a two-pointer counting strategy rather than explicit enumeration.
- Use a 64-bit accumulator for the answer; the number of valid pairs can exceed 32-bit range even when individual durations fit in `int`.
- The increment on a valid step is `right - left`, not `right - left + 1`; counting the current element paired with itself is the classic off-by-one bug here.
- In production code, the real win is not just asymptotic improvement; it is avoiding materialization of combinatorial intermediate results by exploiting order and monotonicity.

## 🚀 Variations & Further Practice
- Count pairs with sum **strictly less than** or **greater than** a threshold; the conceptual twist is handling inequality direction and deciding whether to count from the left or right side.
- Count the number of triplets with sum `<= limit`; this extends the same pattern by fixing one element and running two pointers on the remaining suffix.
- Return the **maximum number of disjoint playable pairs** instead of total pairs; the twist is that this becomes a greedy matching problem rather than a pure counting problem.