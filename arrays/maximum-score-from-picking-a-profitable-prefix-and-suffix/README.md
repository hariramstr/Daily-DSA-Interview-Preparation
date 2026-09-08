# Maximum Score from Picking a Profitable Prefix and Suffix

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Prefix Sum, Dynamic Programming

---

## 🗂 Problem Overview
Given an integer array `profits`, choose a prefix from the left edge, a suffix from the right edge, or both, as long as the two chosen segments remain disjoint. Either side may be empty, and choosing nothing is allowed. Return the maximum total sum.

The challenge is not computing prefix or suffix sums individually; it is finding the best non-overlapping combination under `n <= 200000`, which rules out any quadratic scan over split points.

## 🌍 Engineering Impact
This pattern shows up anywhere a system can retain value from both boundaries while discarding a bad middle: search ranking pipelines that preserve strong head and tail candidates, storage tiering that keeps hot early blocks and recent trailing blocks, and streaming ETL windows that trim a toxic middle segment after anomaly detection. At scale, brute-force recomputation across all split points becomes a latency and cost problem immediately. The useful abstraction is boundary-optimized aggregation: precompute best value available from one side, then combine it with the other side in a single pass. That turns an otherwise combinatorial selection problem into a predictable linear-time primitive.

## 🔍 Problem Statement
You are given an array `profits` where `profits[i]` may be positive or negative. You may select:

- a prefix `profits[0..i]`
- a suffix `profits[j..n-1]`
- both, if they are disjoint, meaning `i < j - 1`
- or neither, if every choice is unprofitable

Return the maximum achievable total profit. This is not a maximum subarray problem: the chosen elements must come only from the array edges, as up to two separated edge-aligned segments.

**Constraints**
- `1 <= profits.length <= 200000`
- `-1000000000 <= profits[i] <= 1000000000`
- answer fits in signed 64-bit integer

**Examples**
- `profits = [4, -2, 3, -10, 5, 6]` → `16`
- `profits = [-5, 7, -3, 8, -2]` → `10`

The key algorithmic constraint is the input size: any `O(n^2)` exploration of all prefix/suffix pairs is too slow.

## 🪜 How to Solve This
1. Start from the structure of the choice: every valid answer is “some prefix” + “some suffix,” with a gap between them, or just one side, or empty.

2. If you fix where the suffix starts, the prefix choice becomes independent: you only need the best prefix sum ending strictly before that suffix.

3. That suggests precomputing, for every index, the best prefix sum available up to that point. Not the raw prefix sum only — the maximum prefix sum seen so far, because you may stop earlier.

4. Symmetrically, scan from the right and consider each suffix sum as it grows. For a suffix starting at `j`, combine it with the best prefix ending at or before `j - 2`.

5. Also allow empty selections by treating the best prefix and best suffix contribution as at least `0`.

6. The resulting shape is classic prefix/suffix dynamic programming: summarize everything useful from the left, then consume it while scanning from the right. One linear preprocessing pass and one linear combination pass are enough.

## 🧩 Algorithm Walkthrough
1. **Compute running prefix sums.**  
   Let `prefixSum[i]` be the sum of `profits[0..i]`. This gives the value of choosing the prefix ending exactly at `i`.

2. **Convert raw prefix sums into best-left values.**  
   Build `bestPrefix[i] = max(0, prefixSum[0], ..., prefixSum[i])`.  
   This is the dynamic programming state: the maximum profit obtainable using only a prefix within `0..i`. The `0` matters because taking no prefix is legal.

3. **Initialize the answer with prefix-only choices.**  
   `bestPrefix[n-1]` already covers all prefix-only options and the empty choice. This avoids special-casing later.

4. **Scan suffixes from right to left.**  
   Maintain `suffixSum`, the sum of `profits[j..n-1]` while iterating `j` from `n-1` down to `0`. This represents taking the suffix starting exactly at `j`.

5. **Combine disjoint choices correctly.**  
   For suffix start `j`, the latest valid prefix endpoint is `j-2`. So the best compatible left contribution is:
   - `bestPrefix[j-2]` if `j >= 2`
   - `0` otherwise  
   Candidate score = `suffixSum + compatiblePrefix`.

6. **Track suffix-only choices automatically.**  
   Because `compatiblePrefix` can be `0`, every suffix is also considered as a standalone answer.

7. **Return the maximum seen.**  
   The invariant is: after processing suffix start `j`, the answer equals the best score among all valid configurations using suffixes starting at positions `>= j`, plus all prefix-only and empty configurations.

This is a **Prefix Sum + Dynamic Programming over boundaries** pattern. The right abstraction is not interval enumeration, but precomputing the best left-edge state and pairing it with each right-edge state exactly once.

## 📊 Worked Example
Use `profits = [4, -2, 3, -10, 5, 6]`.

| i | profits[i] | prefixSum | bestPrefix |
|---|------------|-----------|------------|
| 0 | 4          | 4         | 4          |
| 1 | -2         | 2         | 4          |
| 2 | 3          | 5         | 5          |
| 3 | -10        | -5        | 5          |
| 4 | 5          | 0         | 5          |
| 5 | 6          | 6         | 6          |

Now scan suffixes from right to left:

1. `j=5`: `suffixSum=6`, compatible prefix = `bestPrefix[3]=5`, total = `11`
2. `j=4`: `suffixSum=11`, compatible prefix = `bestPrefix[2]=5`, total = `16`
3. `j=3`: `suffixSum=1`, compatible prefix = `bestPrefix[1]=4`, total = `5`
4. `j=2`: `suffixSum=4`, compatible prefix = `bestPrefix[0]=4`, total = `8`
5. `j=1`: `suffixSum=2`, compatible prefix = `0`, total = `2`
6. `j=0`: `suffixSum=6`, compatible prefix = `0`, total = `6`

Maximum is `16`, from prefix `[4, -2, 3]` and suffix `[5, 6]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One left-to-right pass builds prefix aggregates, and one right-to-left pass evaluates every suffix exactly once. There are no nested scans. At `10^6` elements this is routine; at `10^9`, linear work is still expensive, but quadratic alternatives are completely infeasible.

### Space Complexity
`O(n)` for the `bestPrefix` array storing the best left-side value at each position. It can be reduced only if you restructure the computation to materialize equivalent state differently, but for a single forward precompute plus backward combine, `O(n)` is the clean trade-off.

## 💡 Key Takeaways
- If the problem says “choose from the beginning and/or the end” with non-overlap, think boundary DP rather than general subarray algorithms.
- If one side is fixed and the other side becomes “best value seen so far,” that is a strong signal for prefix/suffix precomputation.
- The disjointness condition is `j - 2` for the prefix lookup, not `j - 1`; adjacent segments would overlap at one index boundary if handled incorrectly.
- Empty selection must be modeled explicitly with `0`, otherwise all-negative inputs return the wrong negative answer.
- At scale, precomputing reusable edge summaries is the difference between an optimization primitive and an explosion of repeated interval evaluation.

## 🚀 Variations & Further Practice
- Allow choosing up to `k` disjoint edge-aligned segments instead of two. The twist is extending the boundary DP state to track segment count without introducing quadratic transitions.
- Require selecting at least one element from both prefix and suffix. The twist is removing the empty-side fallback and handling small arrays and invalid split positions carefully.
- Generalize from edge-aligned segments to any two disjoint subarrays. The twist is that simple prefix/suffix sums are no longer enough; you need best subarray DP from both directions.