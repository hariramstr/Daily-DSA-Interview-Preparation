# Maximum Audience Gain from One Schedule Insertion

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Kadane's Algorithm

---

## 🗂 Problem Overview
Given an integer array `viewers` and a single value `promo`, insert `promo` exactly once at any position and return the largest possible maximum subarray sum of the resulting array. The challenge is not computing one maximum subarray — Kadane already does that — but optimizing over all `n + 1` insertion points without materializing every candidate array. With `viewers.length` up to `2 * 10^5`, any explicit try-all-insertions approach is too slow.

## 🌍 Engineering Impact
This pattern shows up whenever one controlled intervention must maximize the best contiguous run in a sequence: ad insertion in media scheduling, temporary capacity boosts in traffic shaping, bonus weighting in ranking pipelines, or one-time remediation in anomaly-smoothed telemetry. At scale, brute-forcing every insertion point turns a linear scan into quadratic work and quickly becomes operationally irrelevant. The useful abstraction is boundary-aware optimization: precompute the best contribution ending at each index and starting at each index, then evaluate one modification in O(1) per boundary. That enables low-latency decisioning on long streams and keeps the optimization composable inside larger scheduling or ranking systems.

## 🔍 Problem Statement
You are given an array `viewers` where `viewers[i]` is the expected audience of the `i`-th show, and an integer `promo` representing a promotional show that must be inserted exactly once. The insertion may occur before index `0`, between any adjacent shows, or after the last show.

After insertion, compute the maximum subarray sum of the new array. Your goal is to choose the insertion position that maximizes that value.

Constraints:

- `1 <= viewers.length <= 2 * 10^5`
- `-10^4 <= viewers[i] <= 10^4`
- `-10^4 <= promo <= 10^4`
- Result fits in 32-bit signed integer

Examples:

- `viewers = [4, -2, 3, -1], promo = 5` → `10`
- `viewers = [-3, -2, -4], promo = 6` → `6`

The key constraint is array size: evaluating all `n + 1` insertions and rerunning Kadane each time is O(n²), which is not viable.

## 🪜 How to Solve This
1. Start with the brute-force thought: insert `promo` at every position, run maximum-subarray each time, take the best.  
   → Correct, but O(n²).

2. Notice what changes across insertion points.  
   → The inserted value can connect a profitable suffix on the left with a profitable prefix on the right.

3. That immediately suggests two directional DP arrays:  
   → best subarray sum ending at each index  
   → best subarray sum starting at each index

4. Why these two?  
   → If `promo` is inserted between `i` and `i + 1`, the best subarray using `promo` is:
   `max(0, best suffix ending at i) + promo + max(0, best prefix starting at i + 1)`

5. Also remember a subtlety: the final answer might be an original subarray that ignores `promo` if `promo` is negative and placed outside the optimal block.  
   → So compare against the original Kadane result too.

6. Now every insertion boundary can be evaluated in O(1) after O(n) preprocessing.  
   → Total O(n), which matches the constraint profile cleanly.

## 🧩 Algorithm Walkthrough
1. **Compute `leftEnd[i]`: maximum subarray sum ending at `i`** using Kadane-style DP.  
   Recurrence: `leftEnd[i] = max(viewers[i], leftEnd[i-1] + viewers[i])`.  
   This maintains the invariant that `leftEnd[i]` is the best contiguous block whose right boundary is exactly `i`.

2. **Compute `rightStart[i]`: maximum subarray sum starting at `i`** by scanning from right to left.  
   Recurrence: `rightStart[i] = max(viewers[i], viewers[i] + rightStart[i+1])`.  
   This symmetrically maintains the best contiguous block whose left boundary is exactly `i`.

3. **Track the original maximum subarray sum** as `baseMax`.  
   This matters because insertion is mandatory, but the chosen homepage block does not need to include the inserted show. If `promo` is harmful, place it outside the optimal original block.

4. **Evaluate each insertion boundary** from `0` to `n`.  
   For boundary `b`:
   - left contribution = `max(0, leftEnd[b-1])` if `b > 0`, else `0`
   - right contribution = `max(0, rightStart[b])` if `b < n`, else `0`
   - candidate using promo = `left contribution + promo + right contribution`

   The `max(0, ...)` is the key invariant: only profitable adjacent segments should be attached.

5. **Take the global maximum** over all insertion candidates and `baseMax`.  
   This is a classic **Dynamic Programming / Kadane boundary decomposition** pattern: precompute optimal local states, then compose them around a single modification point in constant time.

## 📊 Worked Example
Example: `viewers = [4, -2, 3, -1]`, `promo = 5`

| Index | viewers | leftEnd (best ending here) | rightStart (best starting here) |
|---|---:|---:|---:|
| 0 | 4  | 4 | 5 |
| 1 | -2 | 2 | 1 |
| 2 | 3  | 5 | 3 |
| 3 | -1 | 4 | -1 |

`baseMax = 5` from subarray `[4, -2, 3]`.

Now evaluate insertion boundaries:

1. Before `0`: `0 + 5 + max(0, 5) = 10`
2. Between `0` and `1`: `max(0, 4) + 5 + max(0, 1) = 10`
3. Between `1` and `2`: `max(0, 2) + 5 + max(0, 3) = 10`
4. Between `2` and `3`: `max(0, 5) + 5 + max(0, -1) = 10`
5. After `3`: `max(0, 4) + 5 + 0 = 9`

Best answer is `10`. Multiple insertion points achieve it.

## ⏱ Complexity Analysis

### Time Complexity
The algorithm is O(n): one left-to-right pass for `leftEnd`, one right-to-left pass for `rightStart`, and one final pass over `n + 1` insertion boundaries. That remains practical at `10^6` elements, while an O(n²) strategy becomes unusable long before that and is completely infeasible at `10^9` scale.

### Space Complexity
The straightforward implementation is O(n) due to the two DP arrays storing left-ending and right-starting maxima. One side can be streamed to reduce auxiliary memory, but keeping both arrays usually improves clarity and keeps boundary evaluation simple and branch-light.

## 💡 Key Takeaways
- If a problem asks for the best result after one insertion, deletion, or modification at any boundary, look for precomputed left/right DP states instead of simulating every edit.
- “Maximum subarray after one structural change” is a strong signal for Kadane-derived decomposition: best suffix on the left + change + best prefix on the right.
- The inserted value does **not** have to belong to the final optimal block; always compare against the original maximum subarray sum.
- Boundary handling is the main off-by-one risk: there are `n + 1` insertion positions, including before index `0` and after index `n - 1`.
- The transferable design insight is to separate expensive global recomputation into reusable directional summaries, then compose around a small mutable decision surface.

## 🚀 Variations & Further Practice
- Allow replacing one existing element instead of inserting one; the twist is that the modified position consumes an index, so the composition logic changes from boundary-based to point-based.
- Allow inserting up to `k` promotional shows; the harder part is moving from single-boundary composition to multi-edit DP with state on how many insertions have been used.
- Maximize the best circular subarray after one insertion; the twist is handling wraparound, which requires combining standard and circular Kadane reasoning without double-counting segments.