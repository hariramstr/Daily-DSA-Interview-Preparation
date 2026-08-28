# Maximum Weighted Median Segment

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Binary Search, Prefix Sum

---

## 🗂 Problem Overview
Given two equal-length arrays, `values` and `weights`, consider every non-empty contiguous subarray. Its score is the weighted median: the smallest `x` whose cumulative weight among elements `<= x` reaches at least half of the segment’s total weight, rounded up. Return the maximum score over all segments. The challenge is scale: `n` is up to `2e5`, so enumerating all `O(n^2)` segments or recomputing medians directly is not viable.

## 🌍 Engineering Impact
This pattern shows up anywhere a local window must satisfy a weighted threshold under extreme cardinality: search ranking with confidence-weighted signals, telemetry pipelines computing robust per-interval summaries, fraud systems scoring contiguous event bursts, and storage/query engines evaluating percentile-like predicates over ranges. At scale, the difference between “check every segment” and “convert the predicate into a prefix-sum feasibility test” is the difference between an offline batch and an interactive service. The useful architectural move is reducing an order statistic over many candidate windows into a monotone decision problem, then solving it with binary search plus a linear scan.

## 🔍 Problem Statement
You are given integer arrays `values` and `weights` with:

- `1 <= n <= 200000`
- `1 <= values[i] <= 1e9`
- `1 <= weights[i] <= 1e9`
- `values.length == weights.length`

For any contiguous subarray `values[l..r]`, let `W` be the sum of `weights[l..r]`. Its weighted median is the smallest number `x` such that the total weight of elements in `[l..r]` with value `<= x` is at least `ceil(W / 2)`. The score of a segment is this weighted median.

Return the maximum score among all non-empty contiguous subarrays.

Examples:

- `values = [4,1,7,3]`, `weights = [2,5,4,1]` → `7`
- `values = [5,2,5,1,4]`, `weights = [1,10,1,1,1]` → `5`

The key constraint is `n = 2e5`: any solution that inspects all subarrays is dead on arrival.

## 🪜 How to Solve This
1. Start from the question “can some segment have weighted median at least `T`?”  
   That is easier than directly maximizing the answer.

2. Rephrase the median condition.  
   A segment has weighted median `>= T` iff the total weight of elements with `value >= T` is at least half of the segment’s total weight.

3. Move everything to one side.  
   Give each index contribution `+weights[i]` if `values[i] >= T`, otherwise `-weights[i]`.  
   Then a segment is valid iff its transformed sum is **positive**.

4. Why positive, not non-negative?  
   Weighted median is the **smallest** `x` crossing `ceil(W/2)`. For integer weights, “median at least `T`” is equivalent to weight below `T` being **strictly less** than half, which becomes transformed sum `> 0`.

5. Now the feasibility check is simple: does any subarray have positive sum?  
   That is a prefix-sum question: if current prefix is greater than some earlier minimum prefix, a positive-sum segment exists.

6. The predicate is monotone.  
   If `T` is feasible, any smaller threshold is feasible too. That gives binary search over sorted distinct `values`.

## 🧩 Algorithm Walkthrough
1. **Binary search on the answer space.**  
   Sort and deduplicate `values`. Search the largest candidate `T` such that some segment has weighted median at least `T`.  
   **Why correct:** feasibility is monotone in `T`; raising the threshold can only turn `+weights[i]` into `-weights[i]`, never the reverse.

2. **Transform the array for a fixed threshold `T`.**  
   Build an implicit sequence  
   `a[i] = +weights[i]` if `values[i] >= T`, else `-weights[i]`.  
   **Invariant:** for any segment `[l..r]`, `sum(a[l..r]) = weight(>=T) - weight(<T)`.

3. **Interpret the transformed sum.**  
   Segment `[l..r]` has weighted median at least `T` iff `weight(>=T) > weight(<T)`, equivalently `sum(a[l..r]) > 0`.  
   **Why correct:** total weight is partitioned into `<T` and `>=T`; strict majority on the `>=T` side is exactly the weighted-median threshold condition.

4. **Use the Prefix Sum pattern for feasibility.**  
   Scan once, maintaining running prefix `pref` and the minimum prefix seen so far, `minPref`.  
   If at any point `pref > minPref`, then some subarray ending here has positive sum.  
   **Invariant:** `minPref` is the smallest prefix before the current index, so `pref - minPref` is the best subarray sum ending here.

5. **Return the largest feasible threshold.**  
   Binary search finishes on the maximum `T` whose transformed array contains a positive-sum subarray.  
   This is the right abstraction: **Binary Search on Answer + Prefix Sum feasibility** converts a hard order-statistic-over-all-segments problem into `O(n log n)`.

## 📊 Worked Example
Take `values = [5,2,5,1,4]`, `weights = [1,10,1,1,1]`. Test threshold `T = 5`.

Transform using `>= 5`:

| i | value | weight | transformed | prefix | minPrefix before | positive segment? |
|---|-------|--------|-------------|--------|------------------|-------------------|
| 0 | 5     | 1      | +1          | 1      | 0                | yes (`1 > 0`)     |
| 1 | 2     | 10     | -10         | -9     | 0                | no                |
| 2 | 5     | 1      | +1          | -8     | -9               | yes (`-8 > -9`)   |
| 3 | 1     | 1      | -1          | -9     | -9               | no                |
| 4 | 4     | 1      | -1          | -10    | -9               | no                |

A positive segment exists immediately at index `0`: subarray `[5]` has transformed sum `+1`, so its weighted median is at least `5`. Since `5` is also the maximum array value, the final answer is `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log m)`, where `m` is the number of distinct values, bounded by `n`. Each binary-search step runs a linear feasibility scan using prefix sums, and there are `O(log m)` steps. At `10^6` scale this is still practical; `10^9` elements would require distribution or streaming approximations, not exact in-memory evaluation.

### Space Complexity
`O(m)` for the sorted distinct candidate values, or `O(n)` in the worst case. The feasibility check itself is `O(1)` extra space beyond the input. You can avoid a separate transformed array entirely by computing contributions on the fly during the scan.

## 💡 Key Takeaways
- If a problem asks for the maximum median-like value over all subarrays, look for a monotone feasibility predicate and binary search the answer instead of enumerating segments.
- When a weighted order statistic appears, try converting it into a signed-balance test: “good weight minus bad weight” often collapses the condition into prefix sums.
- The threshold check here is **strictly positive** subarray sum, not non-negative; using `>= 0` accepts segments whose weighted median is actually below the candidate.
- Be precise about the partition: test `values[i] >= T` versus `< T`. Using `<= T` on the positive side inverts the predicate and breaks monotonicity.
- The transferable design move is turning an expensive optimization over many windows into a monotone decision problem with a linear verifier.

## 🚀 Variations & Further Practice
- Require the segment length to be at least `L`: same binary-search reduction, but the prefix-sum verifier must track eligible minimum prefixes with an index lag.
- Ask for the **count** of subarrays whose weighted median is at least `T`: feasibility becomes range counting over prefix sums, typically via Fenwick tree or coordinate compression.
- Extend to online updates of `values` or `weights`: the static prefix-sum scan no longer works directly; you need segment trees or offline divide-and-conquer over thresholds and updates.