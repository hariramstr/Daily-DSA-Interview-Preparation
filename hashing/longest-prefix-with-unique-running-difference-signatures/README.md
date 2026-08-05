# Longest Prefix With Unique Running Difference Signatures

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Rolling Hash, Binary Search

---

## 🗂 Problem Overview
Given an integer array `nums`, compare subarrays by their adjacent-difference sequences rather than by raw values. For a window of length `L`, its signature is the length-`L-1` slice of the difference array. The task is to return the largest `L` such that all windows of length `L` have distinct signatures. With `n` up to `200000`, pairwise subarray comparison is infeasible; the core challenge is detecting duplicate signatures efficiently across many overlapping windows.

## 🌍 Engineering Impact
This pattern shows up anywhere systems compare behavior by deltas instead of absolute values: anomaly detection over telemetry, financial tick-stream normalization, search or ranking feature drift, and deduplication in streaming pipelines. In production, the expensive part is rarely computing one signature; it is proving uniqueness across millions of overlapping windows under tight latency budgets. Without rolling hashes or equivalent indexing, implementations degrade into quadratic scans, blow cache locality, and become impossible to scale in online services. The right approach turns repeated structural comparison into constant-time fingerprint checks plus one global search over window length.

## 🔍 Problem Statement
You are given an integer array `nums` of length `n` (`1 <= n <= 200000`, `-10^9 <= nums[i] <= 10^9`). For any subarray `nums[l..r]`, define its running difference signature as:

`[nums[l+1] - nums[l], nums[l+2] - nums[l+1], ..., nums[r] - nums[r-1]]`

Two subarrays are equivalent iff these signatures have the same length and identical values in order. A length-1 subarray has an empty signature.

Return the maximum integer `L` such that every subarray of length `L` has a unique running difference signature.

Examples:

- `nums = [5, 8, 6, 9, 7]` → differences `[3, -2, 3, -2]` → answer `5`
- `nums = [4, 7, 10, 13, 16]` → differences `[3, 3, 3, 3]` → under the stated definition, `L = 5` is valid because only one window exists

The decisive constraint is `n = 200000`: direct comparison of all window pairs is too slow, so signature equality must be checked via hashing over the difference array.

## 🪜 How to Solve This
1. Read the equivalence rule → notice raw subarray values do not matter, only adjacent differences do. That immediately suggests transforming `nums` into a difference array `diff` of length `n-1`.

2. Reframe the question → a window of length `L` in `nums` corresponds to a contiguous block of length `L-1` in `diff`. So we are really asking: for which largest `k = L-1` are all length-`k` subarrays of `diff` distinct?

3. Comparing all such blocks directly is still too expensive → overlapping windows imply rolling hash. With prefix hashes, each block fingerprint is available in `O(1)`.

4. Observe the monotonic property → if all length-`k` blocks are unique, then all longer blocks are also unique. Two equal longer blocks would force equal prefixes of length `k`. That means validity is monotone with respect to `L`.

5. Monotone predicate + fast checker → binary search on `L`, and for each candidate, hash every corresponding block in `diff` and test whether any fingerprint repeats.

6. Because hashes can collide, use double hashing or 64-bit randomized hashing if you want probabilistic speed with operationally acceptable risk.

## 🧩 Algorithm Walkthrough
1. **Build the difference array**  
   Compute `diff[i] = nums[i+1] - nums[i]` for `0 <= i < n-1`.  
   Why correct: two windows in `nums` have the same running signature exactly when the corresponding slices in `diff` are equal.  
   Invariant: every length-`L` window in `nums` maps to one length-`L-1` window in `diff`.

2. **Precompute rolling-hash prefixes**  
   Use a polynomial rolling hash over `diff`, typically with two moduli or two independent 64-bit hashes.  
   Why correct: prefix hashes let you derive any slice hash in `O(1)` after `O(n)` preprocessing.  
   Invariant: equal slices produce equal hash pairs, modulo collision risk.

3. **Define the predicate `valid(L)`**  
   Let `k = L - 1`. If `L == n`, return true immediately because only one window exists. Otherwise, hash every length-`k` slice of `diff` and insert into a hash set. If a hash repeats, `valid(L) = false`; otherwise true.  
   Why correct: repeated signature means two windows of length `L` are equivalent.

4. **Exploit monotonicity with Binary Search**  
   Pattern: **Rolling Hash + Binary Search on Answer**.  
   If `valid(L)` is true, then any larger length is also true. Longer equal signatures would imply equal shorter signatures, so duplicates cannot appear only at larger lengths.  
   Invariant: search range maintains the largest known valid `L`.

5. **Return the maximum valid length**  
   Binary search over `L in [1, n]`. `L = 1` is valid only if all empty signatures are considered across all windows; under the formal statement, multiple length-1 windows all share the empty signature, so `L=1` is invalid unless `n=1`. The search logic should follow the exact definition you implement.  
   Invariant: final answer is the rightmost `L` with `valid(L) = true`.

## 📊 Worked Example
Take `nums = [5, 8, 6, 9, 7]`.

`diff = [3, -2, 3, -2]`

We binary-search `L`:

| Candidate `L` | `k = L-1` | `diff` windows of length `k` | Duplicate? | `valid(L)` |
|---|---:|---|---|---|
| 3 | 2 | `[3,-2]`, `[-2,3]`, `[3,-2]` | Yes | No |
| 4 | 3 | `[3,-2,3]`, `[-2,3,-2]` | No | Yes |
| 5 | 4 | `[3,-2,3,-2]` | No | Yes |

Trace:
1. Precompute rolling-hash prefixes for `diff`.
2. Check `L=3`: hash three length-2 slices; first and third match, so reject.
3. Check `L=4`: two length-3 hashes, distinct.
4. Check `L=5`: only one window, trivially unique.
5. Largest valid `L` is `5`.

This example also shows why the property is monotone in the useful direction: once uniqueness holds for `L=4`, it continues to hold for `L=5`.

## ⏱ Complexity Analysis
### Time Complexity
Building `diff` and hash prefixes is `O(n)`. Each `valid(L)` scan hashes all windows in `O(n)`, and binary search performs `O(log n)` checks, so total time is `O(n log n)`. At `10^6` scale this is practical; at `10^9`, even linear passes become the bottleneck and the model no longer fits memory comfortably.

### Space Complexity
`O(n)` space for the difference array, power tables, prefix hashes, and the temporary hash set used by `valid(L)`. You can reduce some constants by hashing `diff` on the fly, but the asymptotic bound stays linear unless you trade speed for recomputation.

## 💡 Key Takeaways
- If subarray equivalence is defined by transformed local structure rather than raw values, first normalize the input; here the right normalization is the adjacent-difference array.
- When the question asks for the largest length satisfying a global uniqueness property, check whether the predicate is monotone and therefore searchable with binary search.
- The mapping is `length L in nums` ↔ `length L-1 in diff`; most bugs here are off-by-one errors in window count and hash slice boundaries.
- Be explicit about the `L = 1` and `L = n` cases; empty signatures and single-window lengths can change the answer depending on the exact interpretation.
- In production, rolling hashes are not just an optimization; they are the mechanism that converts repeated structural comparisons into indexable fingerprints with predictable latency.

## 🚀 Variations & Further Practice
- Allow up to `k` duplicate signatures instead of requiring full uniqueness; the twist is turning a boolean monotone predicate into a frequency-bounded one while preserving `O(n log n)` behavior.
- Ask for the longest length where signatures are unique across multiple arrays or streams; the harder part is cross-source indexing and collision-safe deduplication at scale.
- Replace exact equality with approximate equality on differences, such as tolerating bounded noise; this breaks simple hashing and pushes the solution toward sketching, suffix structures, or locality-sensitive hashing.