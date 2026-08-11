# Count Equivalent Access Windows by Relative Time Gaps

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Rolling Hash, Prefix Differences

---

## 🗂 Problem Overview
Given a sorted array of event timestamps and a window size `k`, count how many unordered pairs of contiguous length-`k` windows have identical internal gap patterns. A window is defined only by differences between adjacent timestamps, not by its absolute start time. The challenge is scale: up to `200,000` timestamps means `O(number_of_windows^2)` pairwise comparison is infeasible, so equivalent windows must be grouped efficiently by a compact signature.

## 🌍 Engineering Impact
This pattern shows up anywhere absolute positions drift but relative structure matters: burst-shape detection in observability pipelines, request cadence matching in distributed rate-limiters, sequence deduplication in telemetry streams, and anomaly detection over event inter-arrival times. At scale, naive pairwise comparison explodes quadratically and becomes both latency- and memory-prohibitive. A hashing-based grouping strategy turns structural equivalence into a linear scan over rolling signatures. That shift matters operationally: it enables online aggregation, bounded per-window work, and predictable behavior under high-cardinality workloads where exact structural matching is still required.

## 🔍 Problem Statement
You are given `timestamps`, a nondecreasing array of length `n` where `1 <= n <= 200000` and each value is in `[0, 10^18]`, plus an integer `k` with `1 <= k <= n`. For every contiguous window of exactly `k` timestamps, compute its gap signature: the `k-1` adjacent differences inside that window. Two windows are equivalent if these signatures match element by element.

Return the number of unordered pairs of distinct equivalent windows.

Examples:

- `timestamps = [2,5,9,12,15,19], k = 3`  
  Windows: `[3,4], [4,3], [3,3], [3,4]` → one matching pair → `1`

- `timestamps = [7,7,10,13,13,16], k = 2`  
  Windows: `[0], [3], [3], [0], [3]` → `C(2,2) + C(3,2) = 1 + 3 = 4`

The key constraint is the number of windows: `n-k+1` can be `O(n)`, so comparing each window against every other is too slow.

## 🪜 How to Solve This
1. Read the definition carefully → windows are equivalent by **relative gaps**, not raw timestamps. That means the real data is the difference array `diff[i] = timestamps[i+1] - timestamps[i]`.

2. A length-`k` timestamp window maps to a length-`k-1` subarray in `diff`. So the problem becomes: count equal subarrays of fixed length `k-1`.

3. Equal fixed-length subarrays are a classic hashing target → use a rolling hash so each signature can be represented in `O(1)` update time after preprocessing.

4. Scan all windows once → compute each window hash, store frequency in a hash map, and accumulate combinations. If a signature has frequency `f`, it contributes `f * (f - 1) / 2`.

5. Handle `k = 1` separately. Every single timestamp window has an empty signature, so all `n` windows are equivalent and the answer is simply `C(n, 2)`.

6. Because values are large and repeated timestamps are allowed, use 64-bit arithmetic for differences and counts. For robustness, prefer double hashing or a strong 64-bit rolling hash to minimize collision risk.

## 🧩 Algorithm Walkthrough
1. **Build the adjacent-difference array**  
   Compute `diff[i] = timestamps[i+1] - timestamps[i]` for `0 <= i < n-1`. This is correct because a window’s equivalence depends only on these adjacent gaps. Invariant: every length-`k` timestamp window corresponds exactly to one length-`k-1` slice of `diff`.

2. **Handle the degenerate case `k = 1`**  
   A single-element window has no internal gaps, so every such window shares the same empty signature. Return `n * (n - 1) / 2`. Invariant: no hashing is needed because there is only one possible signature.

3. **Precompute rolling-hash powers and prefix hashes**  
   Use the **Rolling Hash / Prefix Hash** pattern. Treat each `diff[i]` as a token and build prefix hashes so any subarray hash of length `L = k-1` can be extracted in `O(1)`. This abstraction is right because we need equality checks across many overlapping fixed-length slices.

4. **Enumerate all window signatures**  
   There are `n-k+1` windows. For each start index `s`, compute the hash of `diff[s .. s+L-1]`. Invariant: each computed hash represents exactly one candidate signature.

5. **Group identical signatures with a hash map**  
   Maintain `freq[signature]++`. This groups windows by structural equivalence without materializing full gap arrays. If using double hashing, the map key is the pair of hashes.

6. **Count unordered pairs**  
   Either accumulate online by adding current frequency before increment, or do a second pass over `freq` and sum `f * (f - 1) / 2`. This is correct because every pair of windows in the same bucket is equivalent, and no cross-bucket pair is.

## 📊 Worked Example
Take `timestamps = [7,7,10,13,13,16]`, `k = 2`.

`diff = [0,3,3,0,3]` and each timestamp window maps to a length-`1` slice of `diff`.

| Window index | Timestamp window | Signature slice in `diff` | Signature | Running freq | Pairs added |
|---|---|---:|---:|---:|---:|
| 0 | `[7,7]`   | `diff[0]` | `[0]` | 1 | 0 |
| 1 | `[7,10]`  | `diff[1]` | `[3]` | 1 | 0 |
| 2 | `[10,13]` | `diff[2]` | `[3]` | 2 | 1 |
| 3 | `[13,13]` | `diff[3]` | `[0]` | 2 | 1 |
| 4 | `[13,16]` | `diff[4]` | `[3]` | 3 | 2 |

Total pairs added: `0 + 0 + 1 + 1 + 2 = 4`.

This matches the combinatorial view: signature `[0]` appears twice → `1` pair; signature `[3]` appears three times → `3` pairs.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` with rolling hash preprocessing and a single pass over all `n-k+1` windows. The dominant work is computing prefix hashes and extracting each fixed-length subarray hash in constant time. At million-scale inputs this remains practical; at billion-scale, exact in-memory processing would require partitioning or streaming plus external aggregation.

### Space Complexity
`O(n)` for the difference array, hash prefixes/powers, and the frequency map in the worst case where every signature is distinct. Space can be reduced by computing differences on the fly, but prefix-based rolling hash still needs auxiliary arrays unless a different streaming hash strategy is used.

## 💡 Key Takeaways
• If a problem says two windows are equal up to translation or absolute offset, convert the data to adjacent differences and compare structure there.  
• If you need to count equal fixed-length subarrays across many overlapping windows, rolling hash plus frequency counting is the default pattern.  
• `k = 1` is a real edge case: the signature length is zero, so the answer is `C(n,2)`, not zero.  
• Window count is `n-k+1`, but signature length is `k-1`; most indexing bugs come from mixing those two domains.  
• In production, hashing structural fingerprints is often the difference between exact matching at stream scale and an unbounded quadratic comparison path.

## 🚀 Variations & Further Practice
- Count equivalent windows under reversal as well, where a signature matches either exactly or reversed; the twist is canonicalizing two structural representations per window.
- Support many online queries with different `k`; the harder part is reusing preprocessing while avoiding `O(n)` work per query.
- Allow up to one mismatched gap between windows; exact hashing no longer suffices, so you need locality-sensitive ideas, partitioned hashes, or indexed mismatch-tolerant comparison.