# Maximum Ribbon Cut Gain from One Interior Slice

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Kadane's Algorithm

---

## 🗂 Problem Overview
Given an integer array `values`, choose a contiguous range `[l..r]` and remove exactly one element `k` strictly inside that range, maximizing the sum of everything except `values[k]`. Both sides of the removed element must remain non-empty, so `l < k < r`. Return the maximum achievable score. The challenge is that this is not “delete one element anywhere”; the deletion must be interior to the chosen block, which changes the valid substructure and rules out simpler formulations.

## 🌍 Engineering Impact
This pattern shows up in scoring and optimization systems where one bad interior event can be excised while preserving surrounding continuity: ad-ranking sessions with one corrupted impression, streaming pipelines with one malformed record inside an otherwise valuable window, P&L attribution where one anomalous trade is removed from a contiguous strategy interval, or compiler/profile traces where one noisy sample is ignored without breaking temporal locality. At scale, brute-force window enumeration collapses under quadratic cost. The right linear-time decomposition enables online analytics, bounded latency, and predictable memory behavior for arrays with hundreds of thousands or millions of elements.

## 🔍 Problem Statement
You are given an integer array `values` of length `n`, where `3 <= n <= 200000` and each `values[i]` is in `[-10^9, 10^9]`. Choose indices `l < k < r` and maximize:

`sum(values[l..r]) - values[k]`

Equivalently, keep a contiguous block, remove exactly one interior element, and stitch the left and right parts together. The removed element cannot be the first or last element of the chosen block, so both remaining sides must be non-empty.

Return the maximum possible score. The result fits in signed 64-bit integer range.

Examples:

- `values = [4, -7, 5, 6]` → `15`  
  Choose `[4, -7, 5, 6]`, remove `-7`, keep `4 + 5 + 6`.

- `values = [3, -2, 4, -1, 2]` → `8`  
  Choose `[3, -2, 4, -1, 2]`, remove `-2`, keep `3 + 4 + (-1) + 2`.

The key constraint is `n = 2e5`: any `O(n^2)` scan over all `(l, k, r)` triples is infeasible.

## 🪜 How to Solve This
1. Start from the formula: if `k` is the removed interior element, the kept score is “best non-empty subarray ending at `k-1`” + “best non-empty subarray starting at `k+1`”.
2. That observation matters because once `k` is fixed, the left and right choices become independent. You do **not** need to enumerate all `l` and `r`.
3. So the problem reduces to two directional DP passes:
   - for every index `i`, compute the best non-empty subarray sum ending at `i`
   - for every index `i`, compute the best non-empty subarray sum starting at `i`
4. Those are exactly Kadane-style states:
   - `left[i] = max(values[i], left[i-1] + values[i])`
   - `right[i] = max(values[i], right[i+1] + values[i])`
5. Then try every valid interior deletion `k` from `1` to `n-2`:
   - score = `left[k-1] + right[k+1]`
6. Take the maximum over all `k`.

The reason this works is structural: deleting one interior element splits the chosen block into two non-empty contiguous pieces. Kadane gives the best possible piece on each side in linear time.

## 🧩 Algorithm Walkthrough
1. **Model the deletion correctly.**  
   The kept result after removing `values[k]` is not an arbitrary subsequence. It is the concatenation of a non-empty suffix-ending segment on the left and a non-empty prefix-starting segment on the right. This immediately suggests a split-point formulation.

2. **Compute best left contributions with dynamic programming.**  
   Use the Kadane pattern to build `left[i]`, the maximum sum of any non-empty contiguous subarray that ends exactly at `i`.  
   Invariant: after processing `i`, `left[i]` is optimal for all subarrays constrained to end at `i`.

3. **Compute best right contributions symmetrically.**  
   Scan from right to left to build `right[i]`, the maximum sum of any non-empty contiguous subarray that starts exactly at `i`.  
   Invariant: after processing `i`, `right[i]` is optimal for all subarrays constrained to start at `i`.

4. **Evaluate each possible removed interior index.**  
   For every `k` in `[1, n-2]`, the best valid score removing `k` is `left[k-1] + right[k+1]`.  
   Why correct: `left[k-1]` chooses the best non-empty left piece ending right before the gap, and `right[k+1]` chooses the best non-empty right piece starting right after the gap. Together they define some valid `[l..r]` with `l < k < r`.

5. **Take the global maximum.**  
   This is a classic **Dynamic Programming + Kadane decomposition**: precompute optimal local states, then combine them at each split. It is the right abstraction because the constraint is local contiguity on both sides, not global arbitrary deletion.

## 📊 Worked Example
Take `values = [3, -2, 4, -1, 2]`.

| i | values[i] | left[i] (best ending at i) | right[i] (best starting at i) |
|---|-----------|-----------------------------|--------------------------------|
| 0 | 3         | 3                           | 6                              |
| 1 | -2        | 1                           | 3                              |
| 2 | 4         | 5                           | 5                              |
| 3 | -1        | 4                           | 1                              |
| 4 | 2         | 6                           | 2                              |

Now test each valid removed interior index:

1. `k = 1` → remove `-2`  
   Score = `left[0] + right[2] = 3 + 5 = 8`

2. `k = 2` → remove `4`  
   Score = `left[1] + right[3] = 1 + 1 = 2`

3. `k = 3` → remove `-1`  
   Score = `left[2] + right[4] = 5 + 2 = 7`

Maximum is `8`, achieved by removing `-2` from the full block `[3, -2, 4, -1, 2]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One left-to-right Kadane pass, one right-to-left Kadane pass, and one final scan over valid deletion positions. The dominant operation is constant work per element. At `10^6` elements this is routine; at `10^9`, linear time is still too large in practice, but anything superlinear is immediately non-viable.

### Space Complexity
`O(n)` for the `left` and `right` DP arrays. They own essentially all extra memory. This can be reduced to `O(n)` with one side materialized and the other streamed, but not to `O(1)` without losing the ability to combine arbitrary interior split points cleanly.

## 💡 Key Takeaways
• If a “remove one item” problem still requires contiguity on both sides, think split-point DP rather than generic deletion logic.  
• When fixing an index makes the left and right decisions independent, precompute directional best states and combine them.  
• The removed element must be strictly interior: valid `k` is only `1..n-2`, never the endpoints.  
• `left[i]` must mean “best non-empty subarray ending exactly at `i`”, not “best anywhere up to `i`” — that distinction is critical.  
• At scale, the transferable design move is decomposing a global optimization into reusable directional summaries that can be merged in one pass.

## 🚀 Variations & Further Practice
- Allow deleting up to one element from a subarray, including endpoints. The twist is that the state space changes: now the deletion can collapse one side entirely, so the interior-only split formula no longer applies directly.
- Allow removing exactly `k` interior elements while preserving at least one kept segment between removals. This becomes a multi-state DP over deletion count and boundary validity.
- Extend from 1D arrays to 2D grids: choose a submatrix and remove one interior cell or strip while maximizing retained sum. The harder part is preserving contiguity constraints across two dimensions.