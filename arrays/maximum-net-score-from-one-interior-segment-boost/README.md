# Maximum Net Score from One Interior Segment Boost

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Kadane's Algorithm, Dynamic Programming

---

## 🗂 Problem Overview
Given an integer array `scores`, choose exactly one contiguous segment strictly inside the array—indices `l..r` where `1 <= l <= r <= n - 2`—and double its contribution to the final sum. Since doubling a segment adds its sum one extra time, the task reduces to maximizing `totalSum + interiorSegmentSum`. The non-trivial part is scale: with up to `200000` elements, enumerating all interior subarrays is too slow, so the solution must optimize segment selection in linear time.

## 🌍 Engineering Impact
This pattern shows up anywhere a system applies one bounded amplification window over a long signal: boosting a ranking interval in search, applying a temporary multiplier to a pricing or bidding stream, prioritizing one span in observability traces, or selecting a remediation window in SLO analysis. At production scale, brute-force interval evaluation collapses under throughput and latency constraints because candidate ranges grow quadratically. The right abstraction—maximum subarray over a constrained region—turns an infeasible scan into a single-pass primitive. That enables online scoring, low-latency decisioning, and predictable resource usage in data-plane services and analytics pipelines.

## 🔍 Problem Statement
You are given an integer array `scores` of length `n`, where `3 <= n <= 200000` and each value is in `[-100000, 100000]`. Positive values help the total score; negative values reduce it.

You must choose exactly one contiguous boosted segment `scores[l..r]` such that:

- `1 <= l <= r <= n - 2`
- the segment does **not** include the first or last element

If the normal total is `sum(scores)`, boosting `scores[l..r]` makes the final score:

`sum(scores) + sum(scores[l..r])`

So the problem is equivalent to finding the maximum-sum contiguous subarray inside `scores[1..n-2]`.

Examples:

- `scores = [4, -2, 3, -1, 5]` → total `= 9`, best interior segment `[3]`, final `= 12`
- `scores = [7, -5, 4, 6, -2, 8]` → total `= 18`, best interior segment `[4, 6]`, final `= 28`

The constraint driving the algorithmic choice is the array size: quadratic subarray enumeration is not viable.

## 🪜 How to Solve This
1. Read the scoring rule → boosting a segment “counts it twice,” which means we are not changing values outside the segment at all. We are simply adding the chosen segment sum one more time.

2. Rewrite the objective → maximize  
   `sum(scores) + sum(scores[l..r])`.  
   Since `sum(scores)` is fixed, the real problem is: find the maximum-sum contiguous subarray.

3. Notice the restriction → not over the whole array, only over the interior slice `scores[1..n-2]`. That immediately rules out using the first or last element in any candidate segment.

4. Once the problem becomes “maximum subarray on a slice,” Kadane’s Algorithm is the natural fit. It gives the best contiguous sum ending at each position while maintaining the global best seen so far.

5. Why Kadane works here → at each interior index, the best segment ending there either:
   - starts fresh at this index, or
   - extends the best segment ending at the previous index.

6. Compute the full array sum once, run Kadane only on the interior range, then return `total + bestInteriorSum`. Linear time, constant extra space.

## 🧩 Algorithm Walkthrough
1. **Compute the baseline total.**  
   Sum every element in `scores` into `total`. This is the score with no extra contribution. It is fixed regardless of which valid segment is chosen.

2. **Restrict the search space to the interior.**  
   The problem forbids using index `0` and index `n - 1`. So the optimization domain is exactly `scores[1..n-2]`. This is the critical invariant: every candidate considered by the algorithm is valid by construction.

3. **Apply Kadane’s Algorithm on the interior slice.**  
   Pattern: **Dynamic Programming / Kadane’s Algorithm**.  
   Maintain:
   - `current`: maximum sum of a valid interior subarray ending at the current index
   - `best`: maximum interior subarray sum seen so far

   Transition at index `i`:
   - `current = max(scores[i], current + scores[i])`
   - `best = max(best, current)`

   This is correct because any optimal subarray ending at `i` has only two forms: start at `i`, or extend the optimal subarray ending at `i-1`.

4. **Initialize correctly.**  
   Start both `current` and `best` with `scores[1]`, not `0`. The segment must be chosen exactly once, so even if all interior values are negative, the answer must use the least bad interior element rather than pretending we can choose nothing.

5. **Return the final score.**  
   The best boost adds `best` one extra time, so the result is `total + best`.

This abstraction is right because the only decision is selecting one contiguous gain window under boundary constraints; Kadane is the minimal linear-time solution to that class.

## 📊 Worked Example
Take `scores = [7, -5, 4, 6, -2, 8]`.

- `total = 18`
- Interior range is indices `1..4` → `[-5, 4, 6, -2]`

| i | scores[i] | current = max(scores[i], current + scores[i]) | best |
|---|-----------|-----------------------------------------------|------|
| 1 | -5        | -5                                            | -5   |
| 2 | 4         | max(4, -1) = 4                                | 4    |
| 3 | 6         | max(6, 10) = 10                               | 10   |
| 4 | -2        | max(-2, 8) = 8                                | 10   |

Trace:

1. Start at index `1`: only segment is `[-5]`.
2. At index `2`, extending `-5` is worse than starting fresh, so reset to `[4]`.
3. At index `3`, extending gives `[4, 6]` with sum `10`, now the best.
4. At index `4`, extending to `[4, 6, -2]` gives `8`; still valid, but not better.

Best interior segment sum is `10`, so final answer is `18 + 10 = 28`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. One pass computes the total sum, and one pass over indices `1..n-2` runs Kadane’s recurrence. The dominant operation is constant work per element. At `10^6` elements this is routine in memory-resident workloads; at `10^9`, linear time is still expensive, but quadratic alternatives are completely non-starters.

### Space Complexity
`O(1)`. The algorithm stores only scalar accumulators: `total`, `current`, and `best`. No auxiliary arrays or prefix tables are required. You could materialize DP state for debugging or reconstruction, but that increases space to `O(n)` without improving the optimal score computation.

## 💡 Key Takeaways
- If the score becomes “base total plus one chosen contiguous gain,” translate it into a maximum-subarray problem immediately.
- When the problem says “exactly one segment” and “contiguous,” Kadane is a strong default candidate before considering heavier DP.
- The interior-only rule means Kadane must run on `scores[1..n-2]`, not the full array.
- Initialize from the first interior element, not `0`; otherwise all-negative interiors produce an invalid “choose nothing” answer.
- In production systems, many “temporary boost window” optimizations collapse to finding the best constrained interval; the win comes from reframing business logic as a standard linear-time primitive.

## 🚀 Variations & Further Practice
- Allow the boosted segment to be any subarray, including endpoints. Same core pattern, but the boundary restriction disappears and full-array Kadane applies directly.
- Allow up to `k` boosted segments. The twist is interval budgeting; this becomes DP over segment count rather than single-window Kadane.
- Boosting multiplies the chosen segment by an arbitrary factor `m` or even applies a per-element transform. The harder part is reducing the transformed objective back to a maximum-subarray formulation, if possible.