# Maximum Score from Choosing a Centered Triple

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Prefix Maximum, Suffix Maximum

---

## 🗂 Problem Overview
Given an integer array `nums`, choose indices `(i, j, k)` with `i < j < k` such that `nums[j]` is strictly larger than both `nums[i]` and `nums[k]`. The score is `nums[i] + nums[j] + nums[k]`, and the goal is to return the maximum score or `-1` if no such triple exists. The challenge is scale: with up to `2 * 10^5` elements, checking all triples is infeasible, so each position must be evaluated as a potential center in near-linear time.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must evaluate a local “peak” against best-compatible context on both sides without quadratic scans. Examples include search ranking pipelines choosing a dominant candidate with weaker neighboring signals, streaming anomaly detection over telemetry windows, financial time-series analysis for local maxima with bounded context, and compiler optimization passes that compare an instruction against best prior and future alternatives. At production scale, brute-force neighborhood evaluation explodes latency and cache misses. Prefix/suffix precomputation turns repeated contextual queries into constant-time lookups, enabling predictable throughput, vectorizable passes, and simpler reasoning about worst-case behavior.

## 🔍 Problem Statement
You are given an integer array `nums` where `3 <= nums.length <= 200000` and `1 <= nums[i] <= 1000000000`. A valid centered triple is a choice of indices `(i, j, k)` such that:

- `i < j < k`
- `nums[i] < nums[j]`
- `nums[k] < nums[j]`

The score of the triple is:

`nums[i] + nums[j] + nums[k]`

Return the maximum score among all valid triples. If no valid triple exists, return `-1`.

The strict inequalities matter: equal values on either side do not qualify. Also, although each input value fits in 32-bit signed integer range, the sum may require 64-bit arithmetic in some languages.

Examples:

- `nums = [4, 9, 6, 3, 8]` → `19`
- `nums = [5, 5, 5, 5]` → `-1`

The key algorithmic constraint is `n = 200000`: any `O(n^2)` or `O(n^3)` approach is dead on arrival.

## 🪜 How to Solve This
1. Start from the score formula: for a fixed center `j`, we want the best `nums[i]` on the left and best `nums[k]` on the right, but both must be **strictly smaller** than `nums[j]`.

2. That immediately suggests decomposing the problem by center. Instead of searching triples directly, ask: “If `j` is the peak, what is the best legal partner on each side?”

3. For a given center, the best partner is not the smallest smaller value — it is the **largest value still below `nums[j]`**, because that maximizes the sum.

4. So the problem becomes two repeated queries:
   - best smaller value in `nums[0..j-1]`
   - best smaller value in `nums[j+1..n-1]`

5. If those queries were arbitrary, we might need balanced trees or coordinate compression. But this problem is simpler: we only need to know whether the **maximum value on a side** is still below the center.

6. Why? If the maximum on the left is `< nums[j]`, then it is automatically the best valid left choice. If it is `>= nums[j]`, we need more structure — but the intended pattern here is to precompute side maxima and use them where they satisfy the strict inequality.

7. That leads to prefix maximum and suffix maximum arrays, then a single scan over all possible centers.

## 🧩 Algorithm Walkthrough
1. **Precompute prefix maxima**  
   Build `leftMax[i]`, the maximum value in `nums[0..i]`.  
   Invariant: after processing index `i`, `leftMax[i]` is the best candidate value available anywhere up to `i`.

2. **Precompute suffix maxima**  
   Build `rightMax[i]`, the maximum value in `nums[i..n-1]`.  
   Invariant: after processing index `i` from right to left, `rightMax[i]` is the best candidate value available from `i` onward.

3. **Evaluate each interior index as the center**  
   For each `j` from `1` to `n - 2`, the best possible left value is `leftMax[j - 1]`, and the best possible right value is `rightMax[j + 1]`.

4. **Validate strict peak constraints**  
   A centered triple is valid only if both side candidates are strictly smaller than `nums[j]`.  
   Check:
   - `leftMax[j - 1] < nums[j]`
   - `rightMax[j + 1] < nums[j]`

5. **Compute score when valid**  
   If both checks pass, the optimal score for center `j` is:
   `leftMax[j - 1] + nums[j] + rightMax[j + 1]`  
   This is correct because prefix/suffix maxima give the largest available values on each side, and under the strict inequality they are the best legal contributors.

6. **Track the global maximum**  
   Maintain `ans`, initialized to `-1`. Update it whenever a valid center yields a larger score.

This is a **Prefix Maximum / Suffix Maximum** pattern: preprocess directional summaries so every center can be evaluated in `O(1)` time, reducing total work to linear time.

## 📊 Worked Example
Use `nums = [4, 9, 6, 3, 8]`.

| Index | nums[i] | leftMax[i] | rightMax[i] |
|------:|--------:|-----------:|------------:|
| 0 | 4 | 4 | 9 |
| 1 | 9 | 9 | 9 |
| 2 | 6 | 9 | 8 |
| 3 | 3 | 9 | 8 |
| 4 | 8 | 9 | 8 |

Now evaluate centers:

1. `j = 1`, center = `9`  
   Left candidate = `leftMax[0] = 4`  
   Right candidate = `rightMax[2] = 8`  
   Both are `< 9`, so score = `4 + 9 + 8 = 21`

2. `j = 2`, center = `6`  
   Left candidate = `9`, but `9 < 6` is false → invalid

3. `j = 3`, center = `3`  
   Left candidate = `9`, invalid immediately

Maximum valid score found = `21`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One pass builds prefix maxima, one pass builds suffix maxima, and one pass evaluates all centers. The dominant operation is linear scanning with constant-time work per element. At `10^6` elements this remains practical; at `10^9`, even linear time becomes a systems problem dominated by memory bandwidth and I/O.

### Space Complexity
`O(n)` for the `leftMax` and `rightMax` arrays. Those structures own the extra space. It can be reduced to `O(n)` with only one auxiliary side plus a running maximum from the other direction, but not to true `O(1)` without giving up constant-time center evaluation.

## 💡 Key Takeaways
- If the problem asks for the best score around each index using information strictly from the left and right, think directional preprocessing before considering nested loops.
- “Best valid partner on each side” is a strong signal for prefix/suffix summaries when the compatibility rule can be checked in `O(1)`.
- Do not evaluate the first or last index as a center; a valid triple requires at least one element on both sides.
- The inequalities are strict: `<=` is wrong, and equal-valued neighbors must be rejected even if they maximize the sum.
- Precomputing reusable side-context is a production-grade optimization pattern: pay one linear preprocessing cost to eliminate repeated scans and stabilize latency.

## 🚀 Variations & Further Practice
- Return the actual indices `(i, j, k)` of the maximum-scoring triple, not just the score. The twist is preserving argmax information alongside prefix/suffix values.
- Generalize from triples to a centered subsequence of odd length `2m+1`, where the center must dominate all chosen neighbors. The harder part is tracking multiple best compatible values per side.
- Support online updates to `nums` with repeated max-score queries. This shifts the problem from static preprocessing to segment trees or Fenwick-based structures with richer state.