# Count Router Pairs With Coverage Overlap Limit

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Array, Sorting

---

## 🗂 Problem Overview
Given a sorted array `positions`, count how many index pairs `(i, j)` with `i < j` satisfy `positions[j] - positions[i] <= maxGap`. The input is already ordered, so the problem is not about sorting but about exploiting monotonic structure to avoid checking all pairs. The key challenge is scale: with up to `2 * 10^5` routers, the naive `O(n^2)` pair scan is too slow, and the result may require 64-bit storage.

## 🌍 Engineering Impact
This pattern shows up anywhere you need to count bounded relationships in ordered data: matching events within time windows in streaming pipelines, identifying nearby geospatial assets, correlating logs by timestamp, or finding candidate joins in search and ranking systems. At scale, brute-force pair enumeration explodes quadratically and becomes both latency- and cost-prohibitive. The two-pointers approach turns an all-pairs comparison into a single linear pass over already ordered data. That shift matters operationally: it enables predictable throughput, lower memory pressure, and simpler capacity planning for high-volume analytics and online query paths.

## 🔍 Problem Statement
You are given a sorted array `positions` of router locations along a line and an integer `maxGap`. Count the number of index pairs `(i, j)` such that:

- `0 <= i < j < positions.length`
- `positions[j] - positions[i] <= maxGap`

Return the total number of such pairs.

Constraints:

- `1 <= positions.length <= 2 * 10^5`
- `0 <= positions[i] <= 10^9`
- `positions` is sorted in non-decreasing order
- `0 <= maxGap <= 10^9`
- The answer may exceed 32-bit integer range

Examples:

- `positions = [1, 3, 4, 9, 10], maxGap = 3` → `4`
- `positions = [2, 2, 5, 6, 8], maxGap = 2` → `3`

The decisive constraint is the input size. Any nested-loop solution is too slow; the sorted order must be used to count valid ranges efficiently.

## 🪜 How to Solve This
1. Read the condition carefully → we are not asked to list pairs, only count them. That usually means we should aggregate ranges rather than inspect every pair individually.

2. Notice the array is already sorted → for a fixed left index `i`, if some `j` is valid, then every index between `i+1` and `j` is also valid. Valid partners form a contiguous block.

3. Contiguous validity in sorted data suggests a sliding window / two-pointers approach → maintain a right pointer that expands as long as the gap stays within `maxGap`.

4. For each left pointer `i`, once the maximum valid `right` is known, the number of valid pairs starting at `i` is simply `right - i - 1`.

5. Move `i` forward by one → do not reset `right`. Because the array is sorted, the valid window cannot move backward. That monotonicity is the whole optimization.

6. Sum contributions for every `i` using a 64-bit counter → linear scan, no redundant comparisons, no extra data structures.

This is the standard mental model: sorted input + threshold condition + contiguous valid region = two pointers.

## 🧩 Algorithm Walkthrough
1. **Initialize two pointers and an accumulator.**  
   Set `left = 0`, `right = 0`, and `count = 0` using a 64-bit integer. The pattern here is **Two Pointers / Sliding Window** over a sorted array.

2. **For each `left`, expand `right` as far as possible.**  
   While `right < n` and `positions[right] - positions[left] <= maxGap`, increment `right`.  
   Why this is correct: because the array is sorted, once an index fails the condition, every later index will also fail for the same `left`.

3. **Count all valid partners for `left` in one step.**  
   After expansion stops, valid indices are in `[left + 1, right - 1]`. Their count is `right - left - 1`. Add that to `count`.  
   Invariant: before moving `left`, the half-open window `[left, right)` contains exactly the positions within `maxGap` of `positions[left]`.

4. **Advance `left` and reuse `right`.**  
   Increment `left` and repeat. Do not move `right` backward.  
   Why this is valid: increasing `left` can only maintain or relax the distance constraint for the current `right`, so the maximal feasible `right` is monotonic.

5. **Terminate after all left endpoints are processed.**  
   Each index is visited at most once by `left` and once by `right`, so the scan is linear.

This abstraction is the right fit because the problem asks for counts over ordered, threshold-bounded pairs. Two pointers converts repeated range discovery into amortized constant work per element.

## 📊 Worked Example
Use `positions = [1, 3, 4, 9, 10]`, `maxGap = 3`.

| Step | left | right after expansion | Valid window for left | Pairs added | Total |
|---|---:|---:|---|---:|---:|
| 1 | 0 (`1`) | 3 | indices `[1, 2]` → values `[3, 4]` | `3 - 0 - 1 = 2` | 2 |
| 2 | 1 (`3`) | 3 | index `[2]` → value `[4]` | `3 - 1 - 1 = 1` | 3 |
| 3 | 2 (`4`) | 3 | empty | `0` | 3 |
| 4 | 3 (`9`) | 5 | index `[4]` → value `[10]` | `5 - 3 - 1 = 1` | 4 |
| 5 | 4 (`10`) | 5 | empty | `0` | 4 |

Trace intuition: `right` never resets. It moves from `0` to `3`, then later from `3` to `5`. That monotonic movement is what eliminates quadratic behavior.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, because both pointers only move forward and each index is advanced at most once by `left` and once by `right`. That is practical for `10^6` elements in memory-resident workloads, while `O(n^2)` becomes infeasible long before that. At `10^9` scale, the algorithm is still linear but input size and I/O dominate system design.

### Space Complexity
`O(1)` auxiliary space. The algorithm uses only a few scalar variables; the input array owns the storage. This cannot be meaningfully reduced further unless the data is streamed, in which case the sorted-order requirement becomes the real trade-off.

## 💡 Key Takeaways
- If the input is sorted and the validity condition is monotonic with distance, expect a two-pointers window rather than nested loops.
- When valid matches for each index form a contiguous suffix or prefix range, count the whole block at once instead of enumerating pairs.
- Use `right - left - 1`, not `right - left`; `right` is the first invalid index in a half-open window.
- Keep the answer in a 64-bit type; pair counts can exceed 32-bit range even when individual values fit in `int`.
- In production systems, ordered data plus monotonic constraints often lets you replace quadratic candidate generation with linear window scans, which is usually the difference between batch-only and online-feasible.

## 🚀 Variations & Further Practice
- Count pairs with distance in a range `[L, R]` instead of only `<= maxGap`; the twist is combining two window counts or using inclusion-exclusion.
- Given unsorted positions, count valid pairs efficiently; the added challenge is deciding whether the sort cost `O(n log n)` is acceptable and how it affects stability or downstream indexing.
- Count triplets or `k`-tuples within a maximum span; the harder part is extending pairwise window logic into higher-order combinatorial counting without reverting to brute force.