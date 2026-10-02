# Count Bookend Pairs Under Shelf Length

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an integer array `lengths` and an integer `shelfLimit`, count how many distinct index pairs `(i, j)` with `i < j` satisfy `lengths[i] + lengths[j] <= shelfLimit`. The output is the total number of valid pairs, not the pairs themselves. The non-trivial part is scale: with up to `100000` elements, checking every pair is too slow, so the solution must exploit ordering to count many pairs at once.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to count feasible combinations under a threshold: bin-packing heuristics in warehouse software, pairing jobs under CPU or memory budgets in schedulers, matching requests under latency or payload caps in gateways, and candidate generation in ranking or recommendation pipelines. At small scale, quadratic enumeration is invisible; at production scale, it becomes a latency spike or an outright capacity failure. Sorting plus two pointers turns pair validation into a monotonic scan, which is the difference between something that degrades gracefully and something that collapses under bursty input sizes.

## 🔍 Problem Statement
You are given:

- `lengths`, where `lengths[i]` is the length of the `i`-th bookend
- `shelfLimit`, the maximum combined length allowed for two bookends on one shelf section

Return the number of distinct pairs `(i, j)` such that:

- `0 <= i < j < lengths.length`
- `lengths[i] + lengths[j] <= shelfLimit`

Constraints:

- `1 <= lengths.length <= 100000`
- `1 <= lengths[i] <= 1000000000`
- `1 <= shelfLimit <= 2000000000`
- The answer may exceed 32-bit integer range, so use a 64-bit type

Examples:

- `lengths = [1, 3, 2, 2]`, `shelfLimit = 4` → `4`
- `lengths = [5, 1, 4, 2]`, `shelfLimit = 5` → `2`

The key constraint is `lengths.length = 100000`: this rules out `O(n^2)` pair enumeration and pushes toward sorting plus a linear scan.

## 🪜 How to Solve This
1. Start with the brute-force interpretation → every pair `(i, j)` must be checked.  
   That is straightforward but costs `O(n^2)`, which is unacceptable at `100000` elements.

2. Notice the condition is based only on the **sum** of two values, not on original positions.  
   That is a strong signal that sorting may help, because sorted order gives structure to which sums can or cannot work.

3. Sort the array.  
   Now the smallest values are on the left and the largest on the right, which lets us reason about whole ranges instead of individual pairs.

4. Put one pointer at the start and one at the end.  
   If `lengths[left] + lengths[right] <= shelfLimit`, then `left` can pair with **every** element from `left + 1` through `right`, because all of them are no larger than `lengths[right]`.

5. Count those pairs in one shot: `right - left`, then move `left` forward.  
   Otherwise, the largest value is too large to pair with the current smallest, so move `right` backward.

6. Repeat until pointers cross.  
   The key insight is monotonicity: sorting converts many pair checks into range counting.

## 🧩 Algorithm Walkthrough
1. **Sort the array in non-decreasing order.**  
   This enables the **Two Pointers** pattern by making pair sums monotonic with respect to pointer movement. Once sorted, increasing `left` makes sums larger, and decreasing `right` makes sums smaller.

2. **Initialize `left = 0`, `right = n - 1`, and `count = 0`.**  
   The invariant is that all valid pairs fully outside the current `[left, right]` window have already been counted exactly once.

3. **Evaluate `lengths[left] + lengths[right]`.**  
   - If the sum is `<= shelfLimit`, then `(left, right)` is valid.  
   - More importantly, because the array is sorted, every index `k` where `left < k <= right` also satisfies `lengths[left] + lengths[k] <= shelfLimit`.  
   So we can add `right - left` pairs immediately.

4. **Move `left++` after counting.**  
   This is correct because every pair that uses the current `left` has now been fully accounted for. The invariant remains intact: no future step needs to revisit that index.

5. **If the sum is too large, move `right--`.**  
   Since `lengths[right]` is the largest remaining value, it cannot pair with `left`, and therefore cannot pair with any index to the right of `left` that is larger than `lengths[left]`. Shrinking `right` is the only move that can restore feasibility.

6. **Stop when `left >= right`.**  
   At that point, every distinct pair has either been counted or ruled out. The algorithm is optimal after sorting because each pointer moves at most `n` times.

## 📊 Worked Example
Example: `lengths = [1, 3, 2, 2]`, `shelfLimit = 4`

After sorting: `[1, 2, 2, 3]`

| Step | left | right | values      | sum | Action                | count |
|------|------|-------|-------------|-----|-----------------------|-------|
| 1    | 0    | 3     | `1, 3`      | 4   | valid → add `3`       | 3     |
| 2    | 1    | 3     | `2, 3`      | 5   | too large → `right--` | 3     |
| 3    | 1    | 2     | `2, 2`      | 4   | valid → add `1`       | 4     |

Stop because `left` becomes `2` and `right` is `2`.

Why step 1 adds `3`: once `1 + 3 <= 4`, the `1` also pairs with both `2`s, since they are no larger than `3`. That counts `(1,2)`, `(1,2)`, and `(1,3)` in one operation. Step 3 counts the pair formed by the two `2`s.

## ⏱ Complexity Analysis

### Time Complexity
`O(n log n)` due to sorting; the two-pointer scan itself is `O(n)`. Sorting dominates. At `10^6` elements, this is still practical in most environments; at `10^9`, even sorting becomes infeasible without distribution or approximation, but it remains dramatically better than `O(n^2)` enumeration.

### Space Complexity
`O(1)` auxiliary space if the sort is in-place, excluding implementation-dependent sort overhead; otherwise typically `O(log n)` to `O(n)` depending on the runtime’s sorting algorithm. The scan itself uses constant space. Reducing space further is not meaningful unless the platform’s sort implementation is the bottleneck.

## 💡 Key Takeaways
- If a pair condition depends only on a thresholded sum and asks for a **count**, look for sorting plus a monotonic scan instead of nested loops.
- When sorted order lets one valid pair imply a whole contiguous block of valid pairs, that is a strong Two Pointers signal.
- Use `right - left`, not `right - left + 1`; the current element cannot pair with itself.
- Store the answer in a 64-bit integer type, because the number of valid pairs can exceed 32-bit range even when inputs fit in `int`.
- The production-level lesson is to exploit monotonic structure to convert per-item validation into range counting; that shift is often what makes a design scale.

## 🚀 Variations & Further Practice
- Count pairs with sum **strictly less than** a target or **greater than** a target; the conceptual twist is adjusting pointer movement and inclusive/exclusive boundary logic without introducing off-by-one errors.
- Return the **actual pairs** or their indices instead of just the count; the twist is that output size can become `O(n^2)`, so the counting optimization no longer solves the full problem.
- Extend from pairs to **triplets** with sum under a limit; the twist is nesting one fixed index outside the two-pointer scan, yielding `O(n^2)` after sorting.