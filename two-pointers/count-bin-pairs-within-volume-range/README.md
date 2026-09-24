# Count Bin Pairs Within Volume Range

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an unsorted integer array `volumes` and bounds `low` and `high`, count how many index pairs `(i, j)` with `i < j` satisfy `low <= volumes[i] + volumes[j] <= high`. Equal values at different indices still form distinct pairs. The challenge is scale: with up to `100000` bins, brute-force pair enumeration is too slow. The intended solution sorts once, then uses a two-pointer counting strategy to aggregate many valid pairs at a time.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to count bounded pair interactions without materializing all combinations: matching requests to capacity buckets in schedulers, estimating feasible resource pairings in warehouse optimization, counting candidate joins in analytics engines, and pruning search spaces in ranking or recommendation pipelines. At production scale, `O(n^2)` pair scans collapse under cardinality growth and create avoidable CPU hotspots. Sorting plus monotonic pointer movement converts an explosive combinatorial check into a predictable `O(n log n)` pipeline, which is often the difference between an online computation and an offline batch fallback.

## 🔍 Problem Statement
You are given an array `volumes` where `volumes[i]` is the capacity of the `i`th bin, plus two integers `low` and `high`. Return the number of distinct index pairs `(i, j)` such that `i < j` and:

`low <= volumes[i] + volumes[j] <= high`

Constraints:

- `1 <= volumes.length <= 100000`
- `0 <= volumes[i] <= 1000000000`
- `0 <= low <= high <= 2000000000`
- The result may exceed 32-bit range, so use a 64-bit integer type.

Examples:

- `volumes = [4, 1, 7, 3, 2], low = 5, high = 8` → `6`
- `volumes = [2, 2, 2, 2], low = 4, high = 4` → `6`

The key constraint is input size. A naive double loop does `O(n^2)` sum checks, which is not viable at `10^5` elements. The algorithmic choice is therefore driven by counting ranges efficiently after sorting.

## 🪜 How to Solve This
1. Start from the obvious formulation: count pairs whose sum lies in `[low, high]`.  
2. Direct enumeration is quadratic, so ask: can we count whole groups of pairs at once instead of checking each pair individually?  
3. Sorting is the first unlock. Once values are ordered, pair sums become monotonic as pointers move inward. That means if a pair is small enough, many neighboring pairs are also small enough.  
4. Reframe the problem:  
   `count(low <= sum <= high) = count(sum <= high) - count(sum < low)`  
   More conveniently: `count(sum <= high) - count(sum <= low - 1)`.  
5. Now solve a simpler subproblem: count pairs with sum `<= limit`.  
6. Use two pointers on the sorted array: left at the start, right at the end.  
   - If `volumes[left] + volumes[right] <= limit`, then every index between `left+1` and `right` also forms a valid pair with `left`. Count them in one shot.  
   - Otherwise the sum is too large, so decrement `right`.  
7. Run that helper twice and subtract. That gives the exact count in range without double-counting.

## 🧩 Algorithm Walkthrough
1. **Sort the array.**  
   This enables monotonic reasoning about pair sums. After sorting, increasing the left pointer increases sums gradually; decreasing the right pointer decreases sums. That structure is what makes the **Two Pointers** pattern applicable.

2. **Define a helper `countAtMost(limit)`.**  
   It returns the number of pairs `(i, j)` with `i < j` and `volumes[i] + volumes[j] <= limit`. This decomposition is correct because the target range count is:  
   `countAtMost(high) - countAtMost(low - 1)`.

3. **Initialize two pointers.**  
   Set `left = 0`, `right = n - 1`, and `count = 0`.  
   Invariant: all pairs already counted satisfy the limit, and no skipped pair can later become valid due to pointer monotonicity.

4. **Evaluate the current sum.**  
   If `volumes[left] + volumes[right] <= limit`, then every pair `(left, k)` for `k in [left+1, right]` is also valid, because `volumes[k] <= volumes[right]`.  
   Add `right - left` to `count`, then increment `left`.

5. **Otherwise shrink the sum.**  
   If the current sum exceeds `limit`, then `(left, right)` is invalid, and so is any pair using the same `right` with a larger left value? No—larger left only increases the sum. So the only productive move is `right--`.

6. **Repeat until pointers cross.**  
   Each step moves exactly one pointer, so the helper runs in linear time after sorting.

7. **Use 64-bit arithmetic for the answer.**  
   With `n = 100000`, the number of pairs can be about `5 * 10^9`, which exceeds signed 32-bit range.

## 📊 Worked Example
Take `volumes = [4, 1, 7, 3, 2]`, `low = 5`, `high = 8`.

Sorted: `[1, 2, 3, 4, 7]`

We compute:

- `countAtMost(8)`
- `countAtMost(4)`
- answer = first - second

### Trace for `countAtMost(8)`

| left | right | pair | sum | action | count |
|---|---:|---|---:|---|---:|
| 0 | 4 | (1,7) | 8 | valid → add `4` pairs `(1,2)(1,3)(1,4)(1,7)` | 4 |
| 1 | 4 | (2,7) | 9 | too large → `right--` | 4 |
| 1 | 3 | (2,4) | 6 | valid → add `2` pairs `(2,3)(2,4)` | 6 |
| 2 | 3 | (3,4) | 7 | valid → add `1` pair `(3,4)` | 7 |

So `countAtMost(8) = 7`.

### Trace for `countAtMost(4)`

Only `(1,2)` works, so `countAtMost(4) = 1`.

Final answer: `7 - 1 = 6`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates at `O(n log n)`, followed by two linear two-pointer passes, each `O(n)`. Total complexity is `O(n log n)`. At `10^6` elements this is still operationally realistic in optimized environments; at `10^9`, even sorting becomes infeasible, so the problem shifts from algorithm choice to system architecture and data partitioning.

### Space Complexity
Space is `O(1)` auxiliary if sorting in place is allowed, excluding the sort implementation's internal stack or buffer costs. In practice, library sort behavior determines the real footprint. Reducing space further is usually not meaningful; the main trade-off is between in-place mutation and preserving input order.

## 💡 Key Takeaways
- If the problem asks for counting pairs in a sum range over a large array, that is a strong signal to sort and convert the range into one or more monotonic counting passes.
- When you can express a bounded condition as `count(<= upper) - count(<= lower - 1)`, you often unlock a simpler helper that two pointers can solve efficiently.
- Use `right - left`, not `right - left + 1`, when bulk-counting pairs after a valid sum; the current element cannot pair with itself.
- Be careful with the lower bound transformation: the second helper must use `low - 1`, not `low`, or you will exclude pairs whose sum is exactly `low`.
- The transferable design insight is to replace explicit combination generation with aggregate counting over ordered data; that shift is fundamental in scalable query planning and resource-matching systems.

## 🚀 Variations & Further Practice
- Count pairs whose sum is **strictly less than** or **strictly greater than** a threshold; the conceptual twist is boundary handling and proving the pointer moves still preserve correctness.
- Count valid **triplets** within a sum range; the harder part is nesting the two-pointer pass inside a fixed first index while keeping complexity at `O(n^2)` instead of `O(n^3)`.
- Process **online updates** to the array with repeated range-pair queries; the twist is that sorting once is no longer enough, pushing you toward Fenwick trees, segment trees, or order-statistics structures.