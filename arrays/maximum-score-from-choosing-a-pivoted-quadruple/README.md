# Maximum Score from Choosing a Pivoted Quadruple

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Prefix/Suffix Precomputation

---

## 🗂 Problem Overview
Given an integer array `nums`, choose indices `a < b < c < d` to maximize `(nums[a] - nums[b]) * (nums[c] - nums[d])`. The output is the maximum score across all valid ordered quadruples, even if that maximum is negative. The difficulty is not the formula itself but the ordering constraint: each factor depends on a disjoint side of the pivot pair `(b, c)`, so a naive `O(n^4)` search is infeasible for `n` up to `200000`.

## 🌍 Engineering Impact
This pattern shows up anywhere a global optimum is composed from two ordered local decisions: ranking pipelines that combine upstream gain with downstream decay, trading systems that pair entry spread with exit spread under time ordering, and streaming analytics that compute best split-point metrics over massive event windows. At scale, brute force collapses under combinatorial growth, and even `O(n^2)` often misses latency budgets. Prefix/suffix precomputation turns a cross-boundary optimization into linear passes, which is exactly the kind of decomposition that keeps search, observability, and real-time scoring systems predictable under load.

## 🔍 Problem Statement
You are given an integer array `nums` of length `n`, where `4 <= n <= 200000` and `-10^9 <= nums[i] <= 10^9`. A valid pivoted quadruple is a tuple of indices `(a, b, c, d)` such that:

- `0 <= a < b < c < d < n`

Its score is:

- `(nums[a] - nums[b]) * (nums[c] - nums[d])`

Return the maximum score over all valid quadruples. If every valid score is negative, still return the least negative one. The result is guaranteed to fit in signed 64-bit integer range.

Examples:

- `nums = [8, 1, 9, 2, 7]` → best valid score is `49`
- `nums = [5, 10, 3, 8, 1, 6]` → best valid score is `49`

The key algorithmic constraint is `n = 200000`: any solution above near-linear time is operationally dead on arrival.

## 🪜 How to Solve This
1. Start from the expression, not the indices:  
   `(nums[a] - nums[b]) * (nums[c] - nums[d])`.

2. Notice the split: for fixed middle anchors `b` and `c`, the left factor only depends on indices `<= b`, and the right factor only depends on indices `>= c`.

3. That suggests precomputing “best possible left difference ending at `b`” and “best possible right difference starting at `c`”.

4. But multiplication changes the game: the best product may need:
   - largest positive left × largest positive right, or
   - most negative left × most negative right.

5. So for every `b`, track both:
   - maximum value of `nums[a] - nums[b]`
   - minimum value of `nums[a] - nums[b]`

6. Symmetrically, for every `c`, track both:
   - maximum value of `nums[c] - nums[d]`
   - minimum value of `nums[c] - nums[d]`

7. Then sweep all valid adjacent anchor pairs `b < c`, specifically `c >= b + 1`, and combine the four sign cases in `O(1)` each.

8. The whole problem reduces from enumerating quadruples to joining prefix-derived and suffix-derived summaries while preserving index order.

## 🧩 Algorithm Walkthrough
1. **Precompute prefix extrema of values**  
   Maintain `prefixMax[i]` and `prefixMin[i]`: the maximum and minimum values in `nums[0..i]`.  
   Why: for any `b`, the best `a < b` maximizing `nums[a] - nums[b]` is the largest earlier value; the most negative left difference uses the smallest earlier value.

2. **Build left-side difference arrays**  
   For each `b` in `[1, n-3]`:
   - `leftMax[b] = prefixMax[b-1] - nums[b]`
   - `leftMin[b] = prefixMin[b-1] - nums[b]`  
   Invariant: these are the maximum and minimum achievable values of `nums[a] - nums[b]` over all valid `a < b`.

3. **Precompute suffix extrema of values**  
   Maintain `suffixMax[i]` and `suffixMin[i]`: the maximum and minimum values in `nums[i..n-1]`.  
   Why: for any `c`, the best `d > c` maximizing `nums[c] - nums[d]` uses the smallest later value; the most negative right difference uses the largest later value.

4. **Build right-side difference arrays**  
   For each `c` in `[2, n-2]`:
   - `rightMax[c] = nums[c] - suffixMin[c+1]`
   - `rightMin[c] = nums[c] - suffixMax[c+1]`  
   Invariant: these summarize all valid `d > c`.

5. **Combine anchors with index ordering preserved**  
   Iterate all valid `(b, c)` with `1 <= b < c <= n-2`. Since `left*` is tied to `b` and `right*` to `c`, evaluate:
   - `leftMax[b] * rightMax[c]`
   - `leftMax[b] * rightMin[c]`
   - `leftMin[b] * rightMax[c]`
   - `leftMin[b] * rightMin[c]`  
   This is the correct abstraction: **prefix/suffix precomputation plus dynamic programming over extrema**. You are not storing all candidates, only the sufficient statistics needed to reconstruct the optimum product.

6. **Return the global maximum**  
   Use 64-bit arithmetic throughout. Products can exceed 32-bit range even when inputs do not.

## 📊 Worked Example
Take `nums = [5, 10, 3, 8, 1, 6]`.

| index | value | leftMax | leftMin | rightMax | rightMin |
|---|---:|---:|---:|---:|---:|
| 0 | 5  | - | - | - | - |
| 1 | 10 | -5 | -5 | - | - |
| 2 | 3  | 7  | 2  | 2 | -3 |
| 3 | 8  | 2  | -2 | 7 | 2 |
| 4 | 1  | 9  | 0  | - | - |
| 5 | 6  | - | - | - | - |

Now combine valid anchor pairs:

1. `b=1, c=2` → candidates: `(-5*2), (-5*-3)` = `-10, 15`
2. `b=1, c=3` → `(-5*7), (-5*2)` = `-35, -10`
3. `b=2, c=3` → `(7*7), (7*2), (2*7), (2*2)` = `49, 14, 14, 4`

Maximum is `49`, achieved by `(a,b,c,d) = (1,2,3,4)`:
`(10 - 3) * (8 - 1) = 7 * 7 = 49`.

## ⏱ Complexity Analysis
### Time Complexity
With prefix and suffix extrema computed in linear passes, and each valid anchor position processed in `O(1)`, the total time is `O(n)` if the combination step is implemented as a single aligned sweep over valid `b, c` structure, or `O(n^2)` if all anchor pairs are enumerated directly. For `10^6` elements, only the linear formulation is viable; at `10^9`, even linear scan becomes a distributed systems problem.

### Space Complexity
`O(n)` auxiliary space for prefix/suffix extrema and left/right difference arrays. The dominant cost is storing per-index summaries, not raw candidate pairs. Some arrays can be fused or streamed to reduce memory, but that usually trades away clarity and makes boundary handling more error-prone.

## 💡 Key Takeaways
- If an objective factorizes into a left term and a right term around ordered anchors, look for prefix/suffix precomputation before considering nested loops.
- When maximizing a product, track both maxima and minima; sign interactions often make the “worst” local value part of the global optimum.
- `a < b < c < d` means `left` arrays are valid only for `b >= 1` and `right` arrays only for `c <= n-2`; most bugs are boundary bugs here.
- Use 64-bit multiplication explicitly; differences of `10^9` can produce products around `10^18`.
- The transferable design insight is to replace combinatorial search with compact per-boundary summaries that preserve exactly the information needed for downstream optimization.

## 🚀 Variations & Further Practice
- Maximize `(nums[a] - nums[b]) * (nums[c] - nums[d]) + bonus[b][c]`: same decomposition, but now the anchor join includes a cross-term, forcing a richer DP state.
- Generalize to `k` ordered picks with alternating `+/-` contributions: the hard part becomes defining minimal sufficient prefix/suffix states without exploding dimensions.
- Solve the online version where elements arrive in a stream and queries ask for the current best score: the conceptual twist is maintaining prefix/suffix-style extrema incrementally under append-only updates.