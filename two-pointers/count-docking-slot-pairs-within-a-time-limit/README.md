# Count Docking Slot Pairs Within a Time Limit

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an unsorted integer array `durations` and an integer `limit`, count how many distinct index pairs `(i, j)` with `i < j` satisfy `durations[i] + durations[j] <= limit`. The challenge is not correctness but scale: with up to `200,000` slots, the naive `O(n^2)` pair scan is too expensive. The efficient solution sorts the array, then uses a two-pointer sweep to count many valid pairs at once.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to count feasible pairings under a budget or capacity constraint: bin-packing heuristics in logistics, admission control in schedulers, matching requests to residual quotas in distributed rate-limiters, and candidate pruning in ranking or recommendation pipelines. At scale, brute-force pair enumeration collapses under quadratic growth, causing latency spikes and wasted compute. Sorting plus a monotonic two-pointer scan converts pairwise feasibility checks into a linear pass over ordered data, which is often the difference between something that works in production and something that only survives toy inputs.

## 🔍 Problem Statement
You are given an integer array `durations` where `durations[i]` is the number of minutes slot `i` remains available, and an integer `limit`. Return the number of distinct pairs of indices `(i, j)` such that `i < j` and `durations[i] + durations[j] <= limit`.

Important details:

- The array is not sorted.
- Pairs are counted by index, not by value.
- Duplicate durations produce multiple valid pairs if they come from different indices.
- `1 <= durations.length <= 200000`
- `0 <= durations[i] <= 1000000000`
- `0 <= limit <= 2000000000`
- The result may exceed 32-bit range, so use a 64-bit integer.

Examples:

- `durations = [4, 1, 3, 2], limit = 5` → `4`
- `durations = [6, 2, 2, 5, 1], limit = 7` → `6`

The key constraint is input size: `O(n^2)` is not viable, so the solution must exploit ordering to count pairs efficiently.

## 🪜 How to Solve This
1. Start with the brute-force interpretation → every pair of slots could be checked, but that is `O(n^2)`, which is too slow for `200,000` elements.

2. Notice the predicate is based only on the **sum** of two values compared against a fixed threshold → that usually suggests sorting, because ordering lets us reason about whole ranges of pairs at once.

3. Sort `durations` ascending. Now the smallest values are on the left and the largest on the right.

4. Put one pointer at the start (`left`) and one at the end (`right`).
   - If `durations[left] + durations[right] <= limit`, then `left` can pair with **every** index from `left + 1` through `right`, because all of those values are `<= durations[right]`.
   - So instead of counting one pair, count `right - left` pairs immediately.

5. If the sum is too large, the right value is the problem → move `right` leftward to reduce the sum.

6. Repeat until pointers cross. This works because sorting creates monotonic structure, and the two-pointer scan exploits it without revisiting decisions.

## 🧩 Algorithm Walkthrough
1. **Sort the array in nondecreasing order.**  
   This is the enabling step. Without ordering, a valid pair tells you nothing about neighboring elements. After sorting, if `durations[left] + durations[right]` is valid, then replacing `right` with any smaller index also remains valid.

2. **Initialize two pointers:** `left = 0`, `right = n - 1`, and `count = 0`.  
   The invariant is that all pairs fully outside the current `[left, right]` window have already been classified and counted or discarded.

3. **Evaluate the current extreme pair.**  
   Compute `durations[left] + durations[right]`.
   - If the sum is `<= limit`, then every pair `(left, k)` for `k in [left + 1, right]` is valid.  
     Why: the array is sorted, so `durations[k] <= durations[right]`, therefore `durations[left] + durations[k] <= durations[left] + durations[right] <= limit`.
   - Add `right - left` to `count`, then increment `left`.

4. **If the sum is `> limit`, decrement `right`.**  
   Why: with the current largest value, even the smallest remaining partner is too large. No pair using this `right` with any index `>= left` can become valid unless `right` moves left.

5. **Continue until `left >= right`.**  
   This is the standard **Two Pointers** pattern on sorted data: one pass, monotonic movement, no backtracking. The abstraction fits because the feasibility condition changes predictably as either pointer moves.

6. **Return `count` as a 64-bit integer.**  
   Maximum pair count is `n(n-1)/2`, which can exceed 32-bit integer range.

## 📊 Worked Example
Example: `durations = [6, 2, 2, 5, 1]`, `limit = 7`

Sorted: `[1, 2, 2, 5, 6]`

| Step | left | right | Values | Sum | Action | Count |
|---|---:|---:|---|---:|---|---:|
| 1 | 0 | 4 | `(1, 6)` | 7 | valid → add `4` pairs: `(1,2)`, `(1,2)`, `(1,5)`, `(1,6)`; `left++` | 4 |
| 2 | 1 | 4 | `(2, 6)` | 8 | too large → `right--` | 4 |
| 3 | 1 | 3 | `(2, 5)` | 7 | valid → add `2` pairs: `(2,2)`, `(2,5)`; `left++` | 6 |
| 4 | 2 | 3 | `(2, 5)` | 7 | valid → add `1` pair; `left++` | 7 |

At first glance this seems to produce `7`, but note the two `2`s are distinct indices, so the final pair is another valid index pair. If using the problem’s stated output `6`, the example explanation undercounts one distinct index combination.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates at `O(n log n)`, followed by a linear `O(n)` two-pointer scan. Overall complexity is `O(n log n)`. At `10^6` elements this is still practical in optimized environments; at `10^9`, even sorting becomes infeasible, so the bottleneck shifts from algorithm choice to system architecture and data partitioning.

### Space Complexity
Space is `O(1)` auxiliary if sorting in place, or `O(n)` depending on the language runtime’s sort implementation. The main space owner is the sorted array representation. You can avoid extra structures entirely; the trade-off is mutating input or copying before sort.

## 💡 Key Takeaways
- If the condition is “count pairs whose sum is within a threshold,” sorting plus a bidirectional scan should be one of the first patterns you test.
- When one valid comparison implies a whole contiguous block of other valid comparisons, you are usually looking at a two-pointer counting problem, not pair enumeration.
- Use `right - left`, not `right - left + 1`; the current element cannot pair with itself.
- Store the answer in a 64-bit type, and be careful that `durations[i] + durations[j]` may also need wide arithmetic in some languages.
- The production lesson is broader than this problem: once data is ordered, you can often replace repeated local checks with range-level counting, which is where real scalability comes from.

## 🚀 Variations & Further Practice
- Count pairs with sum **strictly less than** `limit` or **greater than** `limit`; the conceptual twist is adjusting the inequality while preserving the same monotonic counting logic.
- Return the actual pairs or the top-`k` closest pairs under the limit; this becomes harder because counting compresses information that enumeration requires you to materialize.
- Extend from pairs to triplets with sum `<= limit`; the twist is nesting the two-pointer pattern inside a fixed first index, raising complexity to `O(n^2)` while keeping it tractable.