# Maximum Net Gain from One Interior Coupon Burst

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Kadane Variant

---

## 🗂 Problem Overview
Given an integer array `purchases`, choose exactly one non-empty contiguous subarray strictly inside the array, so it cannot touch index `0` or `n - 1`. The score of choosing `purchases[l..r]` is `sum(l..r) - purchases[l - 1] - purchases[r + 1]`. Return the maximum possible score. The challenge is that naive enumeration of all interior subarrays is too slow for `n` up to `200000`, so the solution must exploit linear-time structure.

## 🌍 Engineering Impact
This pattern shows up in systems that optimize a contiguous intervention while charging for boundary disruption: ad-budget bursts in event streams, localized cache warming with transition penalties, search-ranking boosts over adjacent result windows, and streaming anomaly suppression where entering and exiting a segment has cost. At scale, brute-force window evaluation collapses under high-cardinality streams or long daily batches. A linear scan with maintained state enables real-time decisioning, predictable latency, and simpler operational envelopes. The broader lesson is important: once a score decomposes into “running interior contribution + boundary adjustments,” it often admits a Kadane-style dynamic program instead of pairwise enumeration.

## 🔍 Problem Statement
You are given an integer array `purchases` where `purchases[i]` may be positive, zero, or negative. Choose exactly one non-empty contiguous block `purchases[l..r]` such that `1 <= l <= r <= n - 2`; in other words, the chosen block must be strictly interior and cannot include the first or last element.

Its campaign score is:

`sum(purchases[l..r]) - purchases[l - 1] - purchases[r + 1]`

Return the maximum score over all valid choices.

Constraints:

- `3 <= purchases.length <= 200000`
- `-1000000000 <= purchases[i] <= 1000000000`
- Result fits in signed 64-bit integer

Examples:

- `purchases = [4, 2, 7, -3, 5]` → `8`  
  Choose `[7]`: `7 - 2 - (-3) = 8`

- `purchases = [3, -5, 6, 1, -2, 4]` → `10`  
  Choose `[6, 1]`: `(6 + 1) - (-5) - (-2) = 10`

The key constraint is `n = 200000`, which rules out quadratic subarray enumeration.

## 🪜 How to Solve This
1. Start from the score formula:  
   `sum(l..r) - purchases[l-1] - purchases[r+1]`.

2. Rearrange it mentally as:  
   `(sum(l..r) - purchases[l-1]) - purchases[r+1]`.  
   For a fixed right endpoint `r`, the only part depending on `l` is `sum(l..r) - purchases[l-1]`.

3. That suggests a running best value ending at `r`: among all valid starts `l <= r`, what is the maximum of `sum(l..r) - purchases[l-1]`?

4. Expand one step:  
   `sum(l..r) = sum(l..r-1) + purchases[r]`.  
   So the best value ending at `r` is either:
   - start fresh at `l = r`, giving `purchases[r] - purchases[r-1]`
   - or extend the best segment ending at `r-1` by adding `purchases[r]`

5. That is exactly Kadane’s recurrence, but with a modified “start fresh” term.

6. Once you know the best interior segment ending at `r`, subtract `purchases[r+1]` to close the score and update the global maximum.

7. Scan `r` from `1` to `n-2`. One pass, constant extra state, no prefix array required.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Kadane variant over interior endpoints.**  
   Standard Kadane tracks the best subarray sum ending at each index. Here, the score adds a left-boundary penalty and a right-boundary penalty, so we adapt the state rather than enumerate all windows.

2. **Define the DP state.**  
   Let `bestEnd[r]` mean the maximum value of `sum(purchases[l..r]) - purchases[l-1]` over all valid `l` with `1 <= l <= r`.  
   This state intentionally excludes the right penalty so it can be updated incrementally.

3. **Derive the recurrence.**  
   For each interior index `r`:
   - Start a new block at `r`: score contribution is `purchases[r] - purchases[r-1]`
   - Extend the best block ending at `r-1`: `bestEnd[r-1] + purchases[r]`  
   Therefore:  
   `bestEnd[r] = max(purchases[r] - purchases[r-1], bestEnd[r-1] + purchases[r])`

4. **Close the block on the right.**  
   A complete campaign ending at `r` has score:  
   `bestEnd[r] - purchases[r+1]`  
   This is valid because `r <= n-2`, so `r+1` exists and remains outside the chosen block.

5. **Maintain the global optimum.**  
   As you sweep `r` from `1` to `n-2`, compute the complete score for each possible right endpoint and update `answer`.

6. **Invariant.**  
   After processing index `r`, `bestEnd` stores the best left-adjusted interior block ending exactly at `r`, and `answer` stores the best fully scored interior block among all endpoints `<= r`.

7. **Why this abstraction is right.**  
   The problem is not about arbitrary range queries; it is about maximizing a contiguous segment under local transition costs. That is precisely where Kadane-style DP dominates.

## 📊 Worked Example
Use `purchases = [3, -5, 6, 1, -2, 4]`.

We scan `r` from `1` to `4` (`n - 2`).

| `r` | `purchases[r]` | start fresh = `a[r]-a[r-1]` | extend previous | `bestEnd` | full score = `bestEnd-a[r+1]` | `answer` |
|---|---:|---:|---:|---:|---:|---:|
| 1 | -5 | -8 | — | -8 | -14 | -14 |
| 2 | 6  | 11 | -2 | 11 | 10 | 10 |
| 3 | 1  | -5 | 12 | 12 | 14 | 14 |
| 4 | -2 | -3 | 10 | 10 | 6 | 14 |

Interpretation:

- At `r = 2`, best block ending there is `[6]`, with left-adjusted value `6 - (-5) = 11`.
- At `r = 3`, extending gives `[6, 1]`, left-adjusted value `12`.
- Closing that block subtracts the right neighbor `-2`, so total score is `12 - (-2) = 14`.

For this input, the maximum score is `14`, achieved by `[6, 1]`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. The algorithm performs a single left-to-right scan over valid interior endpoints, doing constant work per index: one recurrence update and one answer update. At `10^6` elements this is comfortably practical; at `10^9`, linear time is still likely too large for a single-machine in-memory pass, but it remains asymptotically optimal.

### Space Complexity
`O(1)`. Only a few scalar variables are needed: the running DP value and the global maximum. No auxiliary arrays are required. You could materialize the DP table for debugging or auditability, but that increases space to `O(n)` without improving asymptotic runtime.

## 💡 Key Takeaways
- If a subarray objective looks like “interior sum plus or minus values adjacent to the boundaries,” try rewriting it so one boundary penalty is absorbed into a Kadane-style state.
- When the score is maximized over all contiguous blocks and constraints forbid quadratic enumeration, ask whether “best subarray ending at index `i`” is the right DP lens.
- The valid range is `1 <= l <= r <= n - 2`; touching either edge invalidates the block even if the arithmetic would still be defined.
- Use 64-bit arithmetic throughout: `n` is large and values can be `1e9`, so intermediate sums easily exceed 32-bit range.
- In production optimization loops, decomposing a global score into incremental state plus local transition costs is often the difference between offline batch search and real-time decisioning.

## 🚀 Variations & Further Practice
- Allow up to `k` interior coupon bursts instead of exactly one. The twist is segment partitioning with boundary penalties, which pushes the solution toward DP over `(index, bursts_used)`.
- Make the boundary penalty distance-weighted, e.g. subtract `wL * purchases[l-1] + wR * purchases[r+1]`. The recurrence still resembles Kadane, but only if the weights are fixed and local.
- Support online updates to `purchases` with repeated max-score queries. The twist is moving from one-pass DP to a segment-tree design that composes left-adjusted and right-adjusted states.