# Pair Dishes for a Shared Oven Slot

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an array of dish baking times, count how many distinct index pairs `(i, j)` have a combined time within an inclusive range `[minTime, maxTime]`. Equal values still produce different pairs if they come from different indices. The challenge is scale: with up to `2 * 10^5` dishes, checking all `O(n^2)` pairs is infeasible, so the solution must exploit ordering and monotonicity.

## 🌍 Engineering Impact
This pattern shows up anywhere you need to count valid combinations under bounded sums without enumerating all pairs: ad auction budget matching, request coalescing under latency caps, warehouse bin packing heuristics, and streaming analytics over bounded event windows. At production scale, brute-force pair generation explodes both CPU and memory, and often becomes impossible before correctness is even in question. The sort-plus-two-pointer approach turns pair validation into a monotonic counting problem, which is the difference between a batch job that completes in seconds and one that never clears the queue. It also generalizes cleanly into threshold counting APIs and offline analytics pipelines.

## 🔍 Problem Statement
You are given an integer array `times` where `times[i]` is the baking time of the `i`-th dish. Count the number of index pairs `(i, j)` such that:

- `0 <= i < j < n`
- `minTime <= times[i] + times[j] <= maxTime`

Constraints:

- `2 <= times.length <= 2 * 10^5`
- `1 <= times[i] <= 10^9`
- `1 <= minTime <= maxTime <= 2 * 10^9`

The answer can exceed 32-bit range, so use a 64-bit integer.

Examples:

- `times = [2, 5, 3, 7, 4], minTime = 7, maxTime = 9` → `5`
- `times = [1, 1, 2, 2, 3], minTime = 3, maxTime = 4` → `8`

The key constraint is input size. `n = 2 * 10^5` rules out nested-loop enumeration, so the algorithm must count many pairs at once rather than inspect each pair individually.

## 🪜 How to Solve This
1. Start from the obvious formulation → we need the number of pairs whose sum lies in a closed interval.
2. Closed intervals are often easier to count indirectly → count pairs with sum `<= maxTime`, then subtract pairs with sum `< minTime`.
3. That reduces the problem to a reusable primitive: **how many pairs have sum `<= limit`?**
4. For that primitive, sorting is the unlock. Once `times` is sorted, pair sums become monotonic as pointers move.
5. Use two pointers: one at the left, one at the right.
   - If `times[left] + times[right] <= limit`, then every index between `left+1` and `right` also forms a valid pair with `left`.
   - That lets you add `right - left` pairs in one step instead of checking them individually.
6. If the sum is too large, move `right` leftward to reduce it.
7. Compute:
   - `countAtMost(maxTime)`
   - `countAtMost(minTime - 1)`
   - answer = difference

The core insight is not “use two pointers” mechanically. It is recognizing that sorting converts pair validation into a monotone frontier you can sweep once.

## 🧩 Algorithm Walkthrough
1. **Sort the array.**  
   This is the enabling transformation. After sorting, for any fixed `left`, sums with larger `right` indices are larger or equal. That monotonicity is what makes bulk counting possible.

2. **Define `countAtMost(limit)`.**  
   This helper returns the number of index pairs with sum `<= limit`. The final result is:
   `countAtMost(maxTime) - countAtMost(minTime - 1)`.  
   This works because every valid pair belongs to the first set, and pairs below the lower bound are removed by subtraction.

3. **Initialize two pointers.**  
   Set `left = 0`, `right = n - 1`, and `count = 0`.  
   Invariant: all pairs already counted satisfy the threshold, and all discarded regions are known impossible.

4. **Evaluate the current pair sum.**  
   If `times[left] + times[right] <= limit`, then every pair `(left, k)` for `k in [left+1, right]` is also valid.  
   Why: sorted order guarantees `times[k] <= times[right]`, so those sums cannot exceed the current valid sum.

5. **Bulk-add and advance.**  
   Add `right - left` to `count`, then increment `left`.  
   This counts all remaining valid partners for `left` exactly once and never revisits them.

6. **Otherwise shrink from the right.**  
   If the sum is too large, decrement `right`.  
   Why: increasing `left` would only increase or preserve the sum, so the only way to recover feasibility is to reduce the larger endpoint.

7. **Stop when pointers cross.**  
   When `left >= right`, every pair has been either counted or ruled out.  
   This is the canonical **Two Pointers** pattern: exploit sorted monotonic structure to sweep a 2D search space in linear time after sorting.

## 📊 Worked Example
Use `times = [2, 5, 3, 7, 4]`, `minTime = 7`, `maxTime = 9`.

Sorted array: `[2, 3, 4, 5, 7]`

We compute `countAtMost(9)` and `countAtMost(6)`.

| limit | left | right | pair | action | added | total |
|---|---:|---:|---:|---|---:|---:|
| 9 | 0 | 4 | 2+7=9 | valid, count all with left=0 | 4 | 4 |
| 9 | 1 | 4 | 3+7=10 | too large, move right | 0 | 4 |
| 9 | 1 | 3 | 3+5=8 | valid, count all with left=1 | 2 | 6 |
| 9 | 2 | 3 | 4+5=9 | valid | 1 | 7 |

So `countAtMost(9) = 7`.

For `limit = 6`:
- `(2,3)` and `(2,4)` are valid after sorting logic, so `countAtMost(6) = 2`.

Final answer: `7 - 2 = 5`.

## ⏱ Complexity Analysis

### Time Complexity
Sorting costs `O(n log n)`, and each `countAtMost` pass is `O(n)` because each pointer moves monotonically across the array once. Total complexity is `O(n log n)`. At `10^6` scale this is practical; at `10^9`, even linear scans are generally outside single-node in-memory assumptions.

### Space Complexity
Space is `O(1)` auxiliary if sorting in place is allowed, excluding the sort implementation’s internal stack or runtime details. If immutability is required, copying the array makes it `O(n)`. The dominant storage cost is the sorted representation, not the two-pointer scan.

## 💡 Key Takeaways
- If the problem asks for the **count of pairs within a sum threshold or range**, that is a strong signal for sorting plus a threshold-counting helper.
- If validity becomes monotone after sorting — moving one pointer only makes sums larger or smaller — think **Two Pointers** before considering hash-based enumeration.
- The lower bound is inclusive, so subtract `countAtMost(minTime - 1)`, not `countAtMost(minTime)`.
- Use a 64-bit accumulator for the pair count; `n * (n - 1) / 2` can exceed 32-bit range well before the input limit.
- At scale, the win comes from reframing “enumerate valid combinations” into “count a monotone region,” which is a broadly reusable systems design move.

## 🚀 Variations & Further Practice
- Count pairs with sum **strictly less than** a target in a streaming or online setting; the harder twist is losing the ability to sort once and sweep offline.
- Return the **actual pairs** instead of just the count; the twist is output size can become `O(n^2)`, so algorithmic optimality shifts from compute-bound to output-bound.
- Extend from 2-sum range counting to **3-sum smaller / bounded triplets**; the harder part is nesting the two-pointer sweep while preserving deduplication and acceptable complexity.