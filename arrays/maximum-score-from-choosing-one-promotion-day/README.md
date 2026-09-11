# Maximum Score from Choosing One Promotion Day

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Kadane's Algorithm

---

## 🗂 Problem Overview
Given an integer array `sales`, choose exactly one index `p` as the promotion day. For that day, compute the best non-empty subarray sum ending at `p` and the best non-empty subarray sum starting at `p`, then combine them while counting `sales[p]` once. Return the maximum such score across all indices. The challenge is scale: with up to `200000` elements, any per-index expansion or nested scan is too slow, so the solution must be linear.

## 🌍 Engineering Impact
This pattern shows up anywhere a system needs the best score around a mandatory anchor point: campaign attribution windows, anomaly scoring around an incident timestamp, P&L analysis around a market event, or search/ranking boosts around a pivot document. At production scale, brute-force local expansion around every anchor collapses under high-cardinality streams and long retention windows. The right approach precomputes directional state once, then answers each anchor in O(1). That trade-off—single-pass dynamic programming over repeated local recomputation—is foundational in streaming analytics, observability pipelines, and cost-sensitive ranking systems.

## 🔍 Problem Statement
You are given an integer array `sales` where `sales[i]` is the net revenue on day `i`. Choose exactly one index `p`. Define:

- `leftScore`: maximum sum of a non-empty contiguous subarray ending at `p`
- `rightScore`: maximum sum of a non-empty contiguous subarray starting at `p`
- `promotionScore = leftScore + rightScore - sales[p]`

Return the maximum `promotionScore` over all valid `p`.

Constraints:

- `1 <= sales.length <= 200000`
- `-1000000000 <= sales[i] <= 1000000000`
- Result fits in signed 64-bit integer

Examples:

- `sales = [4, -2, 3, -1, 5]` → `9`
- `sales = [-5, -2, -7]` → `-2`

Edge case: if all values are negative, both optimal streaks collapse to the single chosen day. The key constraint is array length: O(n²) is not viable, so the algorithm must run in O(n).

## 🪜 How to Solve This
1. Read the definition carefully → each candidate day `p` needs two directional answers: best streak ending at `p`, and best streak starting at `p`.

2. If you try to compute those by expanding left and right from every `p`, you immediately get O(n²). With `200000` elements, that is dead on arrival.

3. The phrase “best subarray ending at index `i`” should trigger Kadane-style dynamic programming. There is a standard recurrence:
   - best ending at `i` = `max(sales[i], best ending at i-1 + sales[i])`

4. Symmetrically, “best subarray starting at index `i`” is the same idea from right to left:
   - best starting at `i` = `max(sales[i], sales[i] + best starting at i+1)`

5. Once both arrays exist, every promotion day is easy:
   - `score(i) = left[i] + right[i] - sales[i]`

6. Scan once more for the maximum. The whole solution is just two directional DP passes plus one aggregation pass.

The key insight is to decompose a “peak centered at `p`” into two one-sided maximum-subarray problems.

## 🧩 Algorithm Walkthrough
1. **Compute `left[i]`: best non-empty subarray sum ending at `i`**  
   This is classic **Dynamic Programming / Kadane’s Algorithm**.  
   Recurrence: `left[i] = max(sales[i], left[i-1] + sales[i])`.  
   Correctness: any optimal subarray ending at `i` either starts at `i` itself or extends the optimal subarray ending at `i-1`.  
   Invariant: after processing index `i`, `left[i]` is exact for all positions `0..i`.

2. **Compute `right[i]`: best non-empty subarray sum starting at `i`**  
   Run the same logic in reverse.  
   Recurrence: `right[i] = max(sales[i], sales[i] + right[i+1])`.  
   Correctness: any optimal subarray starting at `i` either ends immediately at `i` or extends into the optimal subarray starting at `i+1`.  
   Invariant: after processing index `i` from right to left, `right[i]` is exact for all positions `i..n-1`.

3. **Evaluate each promotion day `i`**  
   Combine the two directional optima:  
   `score = left[i] + right[i] - sales[i]`  
   We subtract `sales[i]` because the pivot day belongs to both subarrays and must be counted once.

4. **Track the global maximum**  
   Scan all indices and keep the largest score.  
   This is correct because every valid solution is uniquely defined by some pivot `i`, and for that pivot the best left/right contributions are already precomputed.

5. **Use 64-bit arithmetic**  
   Even though each element fits in 32 bits, cumulative sums may not. The recurrence and final aggregation must use signed 64-bit integers.

## 📊 Worked Example
Example: `sales = [4, -2, 3, -1, 5]`

| i | sales[i] | left[i] = best ending at i | right[i] = best starting at i | score = left + right - sales[i] |
|---|----------|-----------------------------|--------------------------------|----------------------------------|
| 0 | 4        | 4                           | 9                              | 9                                |
| 1 | -2       | 2                           | 5                              | 9                                |
| 2 | 3        | 5                           | 7                              | 9                                |
| 3 | -1       | 4                           | 4                              | 9                                |
| 4 | 5        | 9                           | 5                              | 9                                |

Trace:

1. Left pass builds `[4, 2, 5, 4, 9]`.
2. Right pass builds `[9, 5, 7, 4, 5]`.
3. Each pivot score is computed independently from those arrays.
4. Maximum score is `9`; one valid choice is `p = 2`, using left streak `[4, -2, 3]` and right streak `[3, -1, 5]`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in **O(n)** time: one left-to-right DP pass, one right-to-left DP pass, and one final scan for the maximum. That is linear in input size and remains practical at `10^6` elements. At `10^9`, even linear work becomes infrastructure-bound, so the limiting factor shifts from algorithmic complexity to memory bandwidth and execution environment.

### Space Complexity
The straightforward implementation uses **O(n)** extra space for the `left` and `right` arrays. Those arrays own the memory cost. Space can be reduced to **O(n)** for one side plus O(1) rolling state for the other during aggregation, but only by tightening evaluation order and reducing readability.

## 💡 Key Takeaways
- If a problem asks for the best subarray sum **ending at** each index or **starting at** each index, think directional Kadane DP immediately.
- If every answer is “best value around a mandatory pivot,” decompose it into independent left-state and right-state precomputations.
- Do not forget to subtract `sales[p]` once; both directional streaks include the pivot, so naive addition double-counts it.
- Handle all-negative arrays correctly by enforcing non-empty subarrays; initializing with zero would silently produce invalid answers.
- The production-grade lesson is to precompute reusable directional state once when many local anchor evaluations share the same underlying sequence.

## 🚀 Variations & Further Practice
- Allow up to `k` promotion days instead of exactly one. The twist is combining multiple anchored segments without overlap, which pushes the problem toward higher-dimensional DP.
- Turn the array into a circular timeline. The twist is that “starting” and “ending” streaks can wrap, so standard linear Kadane is no longer sufficient without careful case splitting.
- Support online updates to `sales[i]` and repeated max-score queries. The twist is moving from static DP to segment-tree-style state composition for dynamic range aggregation.