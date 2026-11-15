# Count Courier Pairs Within Delivery Radius

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an unsorted array of courier positions on a 1D road and a non-negative delivery radius, count how many distinct index pairs `(i, j)` with `i < j` satisfy `|positions[i] - positions[j]| <= radius`. The challenge is scale: with up to `200000` couriers, checking every pair is too slow. The key is to sort first, then count valid pairs in linear time using a sliding two-pointer window.

## 🌍 Engineering Impact
This pattern shows up anywhere you need to count or join “nearby” entities under a distance or threshold constraint: geospatial batching, matching riders to pickup zones, log correlation within time windows, fraud detection on transaction timestamps, and search/index systems that cluster adjacent keys. At production scale, naive pairwise comparison explodes quadratically and becomes both latency- and cost-prohibitive. Sorting plus a monotonic window converts an all-pairs problem into a scan, which is often the difference between something that fits in memory and SLA budgets versus something that stalls under peak load.

## 🔍 Problem Statement
You are given an integer array `positions`, where `positions[i]` is the location of the `i`th courier on a straight road, and an integer `radius`. Return the number of distinct pairs `(i, j)` such that `i < j` and `|positions[i] - positions[j]| <= radius`.

Constraints:

- `1 <= positions.length <= 200000`
- `-10^9 <= positions[i] <= 10^9`
- `0 <= radius <= 10^9`

The array is not sorted. The output may exceed 32-bit integer range, so use a 64-bit type where required.

Examples:

- `positions = [4, 1, 7, 3, 9], radius = 3` → `4`
- `positions = [10, 10, 10, 15], radius = 0` → `3`

Edge cases matter: duplicate positions, negative coordinates, `radius = 0`, and large inputs. The decisive constraint is input size: `O(n^2)` pair enumeration is not viable, so the solution must exploit ordering and monotonicity.

## 🪜 How to Solve This
1. Read the condition carefully → it depends only on distance between values, not original indices. That is a strong signal that sorting may simplify the problem.

2. After sorting, absolute difference becomes directional: for `i < j`, `positions[j] - positions[i]` is enough. That removes the need to check both sides.

3. Now think in terms of a moving valid range. For each right endpoint `r`, how many earlier couriers can pair with it? Exactly those from some left boundary `l` through `r - 1`.

4. Because the array is sorted, if `positions[r] - positions[l]` is too large, every index before `l` also fails. So `l` only moves forward, never backward.

5. That monotonic behavior is the reason to use the Two Pointers pattern: one pointer expands the window, the other shrinks it until the constraint is restored.

6. Once the window `[l, r]` is valid, every courier inside it before `r` forms a valid pair with `r`. Add `r - l` to the answer.

7. Repeat once across the array. Sorting costs `O(n log n)`; the scan is `O(n)`.

## 🧩 Algorithm Walkthrough
1. **Sort the array.**  
   This is the enabling transformation. In sorted order, for any `l < r`, `|positions[r] - positions[l]|` becomes `positions[r] - positions[l]`. That gives a monotonic condition over a contiguous range.

2. **Initialize two pointers and an accumulator.**  
   Set `l = 0` and `answer = 0` using a 64-bit integer. Iterate `r` from `0` to `n - 1`. The active window is `[l, r]`.

3. **Restore the window invariant.**  
   While `positions[r] - positions[l] > radius`, increment `l`.  
   Invariant: after this loop, every index in `[l, r]` is within `radius` of `positions[r]` on the left boundary, and no index before `l` can be valid with `r`.

4. **Count new pairs ending at `r`.**  
   Once the invariant holds, all indices `k` with `l <= k < r` satisfy `positions[r] - positions[k] <= radius`. There are exactly `r - l` such indices, so add that to `answer`.

5. **Why this is correct.**  
   Each valid pair is counted exactly once, when its right endpoint is processed. No invalid pair is counted because the left pointer excludes all values too far away. No valid pair is missed because the sorted window contains all and only the eligible left endpoints.

6. **Why Two Pointers is the right abstraction.**  
   This is a classic sorted-window counting problem: a threshold condition over pair distance, contiguous validity after sorting, and a left boundary that only moves forward. That is precisely the shape Two Pointers handles efficiently.

## 📊 Worked Example
Example: `positions = [4, 1, 7, 3, 9]`, `radius = 3`

Sorted: `[1, 3, 4, 7, 9]`

| `r` | `positions[r]` | Adjust `l` while invalid | Final `l` | New pairs `r - l` | Total |
|---|---:|---|---:|---:|---:|
| 0 | 1 | none | 0 | 0 | 0 |
| 1 | 3 | `3 - 1 <= 3` | 0 | 1 | 1 |
| 2 | 4 | `4 - 1 <= 3` | 0 | 2 | 3 |
| 3 | 7 | `7 - 1 > 3` → `l=1`, `7 - 3 > 3` → `l=2`, `7 - 4 <= 3` | 2 | 1 | 4 |
| 4 | 9 | `9 - 4 > 3` → `l=3`, `9 - 7 <= 3` | 3 | 1 | 5 |

Using the stated example pairs, the valid sorted-value pairs are `(1,3)`, `(1,4)`, `(3,4)`, `(4,7)`, and `(7,9)`, so the count is `5`. The two-pointer scan finds them by counting all valid left endpoints for each `r`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates at `O(n log n)`, followed by a single `O(n)` two-pointer scan because each pointer advances at most `n` times. At `10^6` elements this is still practical in optimized environments; at `10^9`, even sorting alone is beyond typical single-node memory and latency budgets.

### Space Complexity
`O(1)` auxiliary space beyond the sort if the language/runtime supports in-place sorting; otherwise `O(n)` depending on the sorting implementation. The main storage cost is the sorted array itself. You can avoid extra structures entirely, trading only for the sort’s implementation-specific memory behavior.

## 💡 Key Takeaways
- If the pair condition depends only on value difference and not original order, sorting is usually the first simplification to test.
- When validity forms a contiguous window after sorting and the left boundary only moves forward, Two Pointers is the right counting pattern.
- Use a 64-bit accumulator: the number of valid pairs can be about `n * (n - 1) / 2`, which overflows 32-bit integers.
- The count added at each step is `r - l`, not `r - l + 1`; `r` does not pair with itself.
- In production systems, converting threshold-based all-pairs checks into sorted window scans is a general strategy for making quadratic joins operationally tractable.

## 🚀 Variations & Further Practice
- Count pairs with distance in a range `[L, R]` instead of just `<= radius`; the twist is combining two window counts or using inclusion-exclusion.
- Count triplets where `max - min <= radius`; the harder part is turning a valid window size into combinatorial counts like `C(windowSize, 3)`.
- Process online updates and queries over positions; the conceptual jump is from offline sorting to balanced trees, Fenwick trees, or order-statistics structures.