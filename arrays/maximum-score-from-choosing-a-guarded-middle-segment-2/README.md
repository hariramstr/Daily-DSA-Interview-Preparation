# Maximum Score from Choosing a Guarded Middle Segment

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Monotonic Stack, Prefix Sum

---

## 🗂 Problem Overview
Given `nums`, `L`, and `R`, choose one non-empty contiguous segment `nums[i..j]` such that `i >= L` and `j <= n - 1 - R`. Its score is `(minimum element in the segment) * (sum of the segment)`. Return the maximum score over all valid segments. The difficulty is that the objective couples a range minimum with a range sum, and negative values make naive “expand while improving” logic invalid. With `n` up to `2e5`, quadratic enumeration is not viable.

## 🌍 Engineering Impact
This pattern shows up anywhere a window is scored by both a bottleneck and an aggregate: search ranking with weakest-signal penalties, streaming pipelines where throughput is constrained by the slowest stage but benefits from batch volume, portfolio/risk windows where worst exposure scales total notional, and storage compaction where the least compressible block dominates batch efficiency. At scale, brute-force range evaluation collapses under cardinality. The useful abstraction is to convert “all subarrays” into “all dominance regions” around each pivot minimum, then optimize sums inside those regions. That shift is what makes large-array processing operationally feasible.

## 🔍 Problem Statement
You are given an integer array `nums` of length `n` and two non-negative integers `L` and `R`, with `0 <= L, R < n`. You must choose exactly one non-empty contiguous subarray `nums[i..j]` such that there are at least `L` elements to its left and at least `R` elements to its right. Formally:

- `i >= L`
- `j <= n - 1 - R`

The score of a valid segment is:

- `min(nums[i..j]) * sum(nums[i..j])`

Return the maximum possible score. Negative numbers are allowed, so extending a segment can either help or hurt even when the minimum stays fixed. The answer may exceed 32-bit range, so use 64-bit arithmetic.

Examples:

- `nums = [5, 2, 4, 3, 6], L = 1, R = 1`
- `nums = [4, -1, 2, -2, 5], L = 0, R = 0` → `25`

The key constraint is `n <= 2 * 10^5`, which rules out checking all `O(n^2)` subarrays.

## 🪜 How to Solve This
1. Read the score carefully → it is not “best sum” or “best minimum”; it is “pick a segment, then multiply its minimum by its sum.”
2. That usually means: fix the minimum first. If `nums[k]` is the minimum of the chosen segment, then the segment must lie inside the maximal interval where every value is `>= nums[k]`.
3. How do we get that interval for every index efficiently? → monotonic stack for previous/next strictly smaller element.
4. Once `k` is treated as the minimum, the problem becomes: inside its allowed dominance interval, and also respecting the guard bounds, find the subarray containing `k` with maximum sum if `nums[k] > 0`, or minimum sum if `nums[k] < 0`.
5. That is a prefix-sum optimization problem over left and right endpoint choices.
6. Rewrite `sum(i..j)` as `pref[j+1] - pref[i]`. Then for fixed `k`, choose the best `pref[j+1]` and `pref[i]` from constrained ranges.
7. Use range min/max queries over prefix sums so each pivot can be evaluated in `O(log n)` after `O(n)` boundary discovery.

## 🧩 Algorithm Walkthrough
1. **Compute prefix sums.**  
   Build `pref` of length `n + 1`, where `pref[t]` is the sum of the first `t` elements. Then `sum(i..j) = pref[j+1] - pref[i]`. This converts every segment-sum query into a difference of two prefix values.

2. **Find each index’s minimum-dominance interval with a Monotonic Stack.**  
   For every `k`, compute:
   - `leftLess[k]`: nearest index to the left with value `< nums[k]`
   - `rightLessEq[k]` or symmetric tie-breaking on the right  
   The tie rule must be consistent so every subarray is assigned to exactly one pivot minimum. This yields the maximal interval `(leftLess[k], rightLess[k])` where `nums[k]` can serve as the designated minimum.

3. **Intersect with guard constraints.**  
   Valid starts satisfy `i >= L`; valid ends satisfy `j <= n-1-R`. For pivot `k`, also require `i <= k <= j`. So:
   - `i ∈ [max(L, leftLess[k]+1), k]`
   - `j ∈ [k, min(n-1-R, rightLess[k]-1)]`  
   If either range is empty, pivot `k` cannot produce a valid segment.

4. **Reduce to choosing best prefix difference.**  
   Since `sum(i..j) = pref[j+1] - pref[i]`, define:
   - left prefix indices `a = i`
   - right prefix indices `b = j+1`  
   Then:
   - `a ∈ [startLo, k]`
   - `b ∈ [k+1, endHi+1]`

5. **Use range min/max queries over prefix sums.**  
   If `nums[k] >= 0`, maximize the segment sum: choose maximum `pref[b]` minus minimum `pref[a]`.  
   If `nums[k] < 0`, minimize the segment sum: choose minimum `pref[b]` minus maximum `pref[a]`.  
   A segment tree or sparse table over `pref` supports these range min/max queries efficiently.

6. **Evaluate score and keep the global maximum.**  
   For each pivot, compute the best achievable sum under its constraints, multiply by `nums[k]`, and update the answer. Correctness follows because every valid segment has a designated minimum pivot, and for that pivot we optimize exactly over all admissible endpoints.

## 📊 Worked Example
Take `nums = [4, -1, 2, -2, 5]`, `L = 0`, `R = 0`.

Prefix sums: `pref = [0, 4, 3, 5, 3, 8]`

| k | nums[k] | dominance interval | start range `i` | end range `j` | best sum containing k | score |
|---|---:|---|---|---|---:|---:|
| 0 | 4  | `[0..0]` | `[0..0]` | `[0..0]` | `4` | `16` |
| 1 | -1 | `[0..2]` | `[0..1]` | `[1..2]` | minimum sum is `1` from `[1..2]`? no, better `-1` from `[1..1]` | `1` |
| 2 | 2  | `[2..2]` | `[2..2]` | `[2..2]` | `2` | `4` |
| 3 | -2 | `[0..4]` | `[0..3]` | `[3..4]` | minimum sum `3` from `[3..4]`? better `-2` from `[3..3]` | `4` |
| 4 | 5  | `[4..4]` | `[4..4]` | `[4..4]` | `5` | `25` |

Maximum score is `25`, achieved by segment `[5]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` with a monotonic stack plus `O(log n)` range min/max queries per index. The stack work is linear; the dominant cost is evaluating all pivots against prefix-sum query structures. At `10^6` elements this remains practical in optimized native code; at `10^9`, no in-memory exact approach is realistic.

### Space Complexity
`O(n)` for prefix sums, stack boundary arrays, and the range-query structure. You can trade implementation complexity for constants: an iterative segment tree is compact, while sparse tables increase memory but offer `O(1)` queries for static input.

## 💡 Key Takeaways
- If a subarray score is defined by “minimum inside range” combined with another aggregate, treat each index as a candidate pivot minimum and compute its dominance interval.
- When the problem asks for all valid subarrays implicitly but `n` is large, monotonic stack boundaries are often the right way to collapse quadratic search space.
- Guard constraints apply to the chosen segment, not to the pivot interval; always intersect stack-derived bounds with `i >= L` and `j <= n-1-R`.
- Tie-breaking for equal values in the monotonic stack must be asymmetric; otherwise the same segment can be counted under multiple minima or missed entirely.
- The transferable design insight is to decompose composite range objectives into a structural partition step plus a fast optimization step over each partition.

## 🚀 Variations & Further Practice
- Allow online updates to `nums` between queries. The hard part becomes maintaining minimum-dominance structure and prefix-sum extrema under mutation, pushing toward segment trees with richer state or offline batching.
- Replace the score with `min(segment) * max(segment)` or `min(segment) * length(segment)`. The conceptual twist is that prefix sums no longer linearize the second term, so the optimization layer changes completely.
- Add a length constraint `len ∈ [A, B]` on top of guard constraints. Now each pivot’s endpoint search becomes a bounded-range optimization with additional feasibility geometry.