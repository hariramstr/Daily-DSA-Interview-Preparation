# Maximum Weighted Median Segment

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Binary Search, Prefix Sum

---

## 🗂 Problem Overview
Given two arrays, `values` and `weights`, choose any contiguous subarray and compute its weighted median. The goal is to return the largest weighted median achievable over all subarrays. The challenge is not the definition itself but the scale: `n` can reach `2 * 10^5`, so enumerating subarrays and recomputing weighted medians is infeasible. The key is to turn “does some subarray have median at least `x`?” into a linear-time feasibility test, then binary search the answer.

## 🌍 Engineering Impact
This pattern shows up anywhere a local segment must satisfy a weighted dominance condition under large-scale scans: search ranking windows, anomaly detection over weighted telemetry, ad auction quality slices, and streaming observability pipelines. The production issue is not computing one median; it is answering whether any interval crosses a threshold without quadratic explosion. Without the threshold-transform-plus-prefix approach, systems fall back to per-window selection or heap maintenance that collapses under high-cardinality streams. This technique enables predictable `O(n log U)` behavior, where `U` is the value domain, and cleanly separates business semantics from feasibility mechanics.

## 🔍 Problem Statement
You are given integer arrays `values` and `weights` of equal length `n`, where `1 <= n <= 200000`, `1 <= values[i] <= 1e9`, and `1 <= weights[i] <= 1e9`. For any contiguous subarray `values[l..r]`, its score is the weighted median of that subarray using `weights[l..r]`.

A value `x` is a weighted median if:
- total weight of elements strictly smaller than `x` is less than half of the subarray’s total weight, and
- total weight of elements strictly greater than `x` is also less than half.

If multiple values satisfy this, choose the smallest such value.

Return the maximum weighted median over all contiguous subarrays.

Examples:
- `values = [4,1,7,3]`, `weights = [2,5,3,1]` → `7`
- `values = [5,2,6,2,4]`, `weights = [1,4,2,3,5]` → `6`

The decisive constraint is `n = 2e5`: any `O(n^2)` subarray strategy is dead on arrival.

## 🪜 How to Solve This
1. Start from the target value, not from subarrays → ask: “Can some subarray have weighted median at least `x`?”
2. Translate the weighted-median condition into a sign test. For a fixed `x`, assign each position:
   - `+weights[i]` if `values[i] >= x`
   - `-weights[i]` if `values[i] < x`
3. Why this works → in a subarray, if the transformed sum is positive, then total weight of elements `>= x` exceeds total weight of elements `< x`. That implies the weighted median of that subarray is at least `x`.
4. Now the problem becomes: does there exist a contiguous subarray with positive sum in this transformed array?
5. That is a classic prefix-sum feasibility check: a positive-sum subarray exists iff some prefix is greater than an earlier minimum prefix.
6. Since feasibility is monotone in `x` — if `x` works, any smaller value also works — binary search over candidate values.
7. To avoid searching the full `1..1e9` domain unnecessarily, sort and deduplicate `values`, then binary search on those actual candidates.

That gives `O(n log n)` overall.

## 🧩 Algorithm Walkthrough
1. **Binary search over candidate medians**  
   Use the sorted unique elements of `values` as the search space. This is the right abstraction because the answer must be one of the observed values; no other integer can become the smallest valid weighted median.

2. **Feasibility transform for a candidate `x`**  
   Build an implicit transformed array `a[i]`:
   - `a[i] = +weights[i]` if `values[i] >= x`
   - `a[i] = -weights[i]` otherwise  
   This is a **prefix sum + threshold reduction** pattern: convert a median-style condition into a dominance sum.

3. **Why the transform is correct**  
   For any subarray, let `G` be total weight with `value >= x`, and `L` be total weight with `value < x`. The transformed subarray sum is `G - L`. If `G - L > 0`, then `G > L`, so weight below `x` is less than half the total. Therefore some weighted median is at least `x`, and because the weighted median is defined as the smallest valid value, the subarray’s weighted median is still `>= x`.

4. **Check whether any positive-sum subarray exists**  
   Scan once with running prefix sum `pref`. Maintain the minimum prefix seen so far, `minPref`. If at any point `pref > minPref`, then the subarray between those positions has positive sum. This invariant is standard: `pref[j] - minPrefBeforeJ > 0` means some subarray ending at `j` is feasible.

5. **Exploit monotonicity**  
   If `x` is feasible, every smaller candidate is feasible too, because more positions flip from negative to positive or stay positive. That monotonic predicate justifies binary search.

6. **Return the largest feasible candidate**  
   The final binary-search result is the maximum weighted median over all contiguous subarrays.

## 📊 Worked Example
Take `values = [4,1,7,3]`, `weights = [2,5,3,1]`, and test candidate `x = 5`.

Transform by `values[i] >= 5`:

| i | value | weight | transformed | prefix | minPrefixBefore | feasible? |
|---|------:|-------:|------------:|-------:|----------------:|----------:|
| 0 | 4 | 2 | -2 | -2 | 0 | no |
| 1 | 1 | 5 | -5 | -7 | -2 | no |
| 2 | 7 | 3 | +3 | -4 | -7 | yes |
| 3 | 3 | 1 | -1 | -5 | -7 | yes |

At `i = 2`, `prefix = -4` and the minimum earlier prefix is `-7`, so `-4 > -7`. That means subarray `[7]` has positive transformed sum, hence some subarray has weighted median at least `5`.

Now test `x = 7`:
transformed array is `[-2, -5, +3, -1]`. Subarray `[7]` still gives positive sum `+3`, so `7` is feasible.

No candidate larger than `7` exists, so the answer is `7`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting and deduplicating candidate values costs `O(n log n)`. Each binary-search step runs a linear feasibility scan in `O(n)`, and there are `O(log n)` candidate steps. Total complexity is `O(n log n)`. At million-scale input this remains practical; quadratic enumeration does not survive even moderate production workloads.

### Space Complexity
`O(n)` if you materialize the sorted unique candidate list; the feasibility check itself is `O(1)` auxiliary space beyond input storage. You can avoid extra transformed-array storage by computing signed contributions on the fly during the prefix scan.

## 💡 Key Takeaways
- If a problem asks for the maximum threshold such that *some subarray* satisfies a weighted majority-style condition, look for binary search on the answer plus a prefix-sum feasibility check.
- Median constraints often become tractable after converting elements into `+w / -w` contributions relative to a candidate threshold.
- The predicate must be **strictly positive**, not non-negative; using `>= 0` breaks the “strictly less than half” requirement in the weighted-median definition.
- Binary search should run over sorted unique `values`, not the raw integer range, to avoid unnecessary iterations and edge-case mistakes around absent values.
- The transferable design insight is to separate semantic thresholding from interval detection: once reduced to a monotone predicate over prefixes, the same architecture scales across ranking, telemetry, and stream analytics systems.

## 🚀 Variations & Further Practice
- Require the subarray length to be at least `k`. Same transform, but the prefix check must respect a lagged minimum prefix, which adds a constrained-window twist.
- Maximize the weighted median over all **non-contiguous** subsequences of bounded total weight. The monotone threshold remains, but the feasibility layer becomes a knapsack-style or greedy selection problem.
- Return the actual subarray achieving the maximum weighted median, with tie-breaking rules. The decision problem stays the same, but implementation must recover interval boundaries from prefix minima without corrupting correctness.