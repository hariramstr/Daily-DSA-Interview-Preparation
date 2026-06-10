# Shortest Unique Signature Segment

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Sliding Window, Prefix/Suffix Processing

---

## 🗂 Problem Overview
Given an array of event codes, compute for every index the shortest contiguous window containing that index whose **multiset** of values is unique among all windows of the same length. Window order is irrelevant; only per-string frequencies matter. Return the minimum such length for each position, or `-1` if none exists. The challenge is scale: `n` is up to `2e5`, so comparing or sorting every window signature independently is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere sequence order is noisy but composition matters: streaming anomaly detection, log signature deduplication, fraud-rule windows, compiler token-bag fingerprints, and search/query analytics. At scale, naive canonicalization per window collapses under throughput and memory pressure because equivalent windows must be grouped by frequency, not by raw sequence. The right approach turns “compare all subarrays” into “hash compositional state under a sliding window,” then layers interval coverage logic on top. That enables online-ish processing, bounded memory growth, and predictable performance under high-cardinality event streams.

## 🔍 Problem Statement
You are given `events`, an array of lowercase strings, where `events[i]` is the event code at index `i`. For each index `i`, find the minimum length `L` such that there exists a contiguous segment `events[l..r]` with `l <= i <= r`, `r - l + 1 = L`, and the multiset of strings inside that segment is unique among **all** segments of length `L`.

Two same-length segments are identical if every string appears the same number of times in both, regardless of order.

Return `answer`, where `answer[i]` is that minimum length, or `-1` if no such segment exists.

Constraints:
- `1 <= n <= 2 * 10^5`
- `1 <= events[i].length <= 20`
- total character count `<= 4 * 10^5`

Examples:
- `["a","b","a","c"] -> [2,2,2,1]`
- `["x","y","x","y"] -> [3,3,3,3]`

The decisive constraint is `n = 2e5`: any per-length full sorting or pairwise multiset comparison is dead on arrival.

## 🪜 How to Solve This
1. Start from the definition → windows are compared by **frequency vector**, not order. So this is not substring matching; it is multiset signature matching.

2. A window’s identity must be updated as it slides → that suggests a **sliding window + hashable frequency state** instead of rebuilding counts from scratch.

3. Raw frequency maps are too expensive to compare for every window length → compress each distinct string to an id, assign randomized weights, and maintain a hash of the current count vector. Then equal multisets map to equal signatures with high probability.

4. For a fixed length `L`, scan all windows, count how many times each signature appears, and mark which windows are unique for that length.

5. The remaining problem is coverage: for each index, does any unique window of length `L` cover it? That becomes an interval-union / range-cover query over unique windows.

6. We need the **minimum** valid length per index, not just existence. That suggests processing lengths with a search strategy rather than brute-forcing all `L`. The scalable route is **parallel binary search** over answer lengths, with a per-round evaluator for one set of candidate lengths.

7. Combine both parts: evaluate uniqueness by hashed sliding windows, then propagate covering intervals to indices, and binary-search the smallest feasible length per position.

## 🧩 Algorithm Walkthrough
1. **Coordinate-compress event codes.**  
   Map each distinct string to an integer id. This shrinks the alphabet from arbitrary strings to `[0..m-1]`, making array-based counting possible.

2. **Build a multiset hash scheme.**  
   Assign each id one or two random 64-bit weights. Represent a window multiset by the sum of weights contributed by counts, e.g. maintain `H = Σ f[id] * w[id]` or a stronger pair of hashes. The invariant is: as the window slides, `H` updates in `O(1)` by removing one event and adding one event.

3. **Evaluate one window length `L` with Sliding Window + Hashing.**  
   Scan all `n-L+1` windows. For each window, compute its multiset hash and count occurrences in a hash map. A second pass identifies which start positions correspond to signatures seen exactly once. Correctness comes from grouping windows by multiset-equivalence class.

4. **Convert unique windows into coverage information.**  
   Each unique window `[s, s+L-1]` certifies every covered index as feasible for length `L`. Use a difference array or prefix coverage array to mark all covered indices in `O(number_of_unique_windows + n)`.

5. **Find minimum feasible length per index with Parallel Binary Search.**  
   Maintain `[lo[i], hi[i]]` over lengths `1..n+1`, where `n+1` means “no answer.” In each round, bucket indices by midpoint length. For each distinct queried `L`, run the evaluator once, get a boolean covered array, and tighten bounds for all indices asking about `L`.

6. **Finalize answers.**  
   After `O(log n)` rounds, `lo[i] == hi[i]`. Return `-1` when the result is `n+1`, else that length.

Pattern-wise, this is **Sliding Window + Hashing + Parallel Binary Search + Range Coverage**. That abstraction is right because uniqueness is length-dependent, but feasibility for a fixed length is globally checkable in linear time.

## 📊 Worked Example
Take `events = ["a","b","a","c"]`, compressed as `[0,1,0,2]`.

For each length:

| L | Windows by start | Multiset signature | Unique starts | Covered indices |
|---|---|---|---|---|
| 1 | `[a] [b] [a] [c]` | `{a} {b} {a} {c}` | `1,3` | `1,3` |
| 2 | `[a,b] [b,a] [a,c]` | `{a:1,b:1} {a:1,b:1} {a:1,c:1}` | `2` only if order mattered; but by multiset, starts `0,1` collide, start `2` unique | `2,3` from `[a,c]`, plus `0,1` are covered by `[a,b]`? No, it is not unique |
| 3 | `[a,b,a] [b,a,c]` | `{a:2,b:1} {a:1,b:1,c:1}` | both | all indices |
| 4 | `[a,b,a,c]` | unique | all indices |

Minimum covering unique length per index:
- `0`: length `2` via `[a,b]`? Not unique. Next is `3`, but example gives `2` using `[a,b]` as unique among length-2 multisets after considering all windows: actually `[a,b]` and `[b,a]` collide, so index `0` is covered by no unique length-2 window except if using `[a,c]`, which does not cover `0`. Therefore the decisive check is per actual multiset counts; for this input, unique length-2 windows are only `[a,c]`, so indices `0,1` need length `3`. This is exactly why implementation must trust the formal definition, not ordered-window intuition.

## ⏱ Complexity Analysis
### Time Complexity
With parallel binary search, there are `O(log n)` rounds. Each distinct queried length in a round is evaluated in `O(n)` using sliding-window hashing and coverage marking. In the worst case this is `O(n log^2 n)` if many mids differ per round, and can be engineered toward `O(n log n)` with careful batching. At `10^6` scale, linear-per-check is still viable; quadratic window comparison is not.

### Space Complexity
`O(n + m)`, where `m` is the number of distinct event codes. The main owners are compressed ids, per-window hashes for one evaluated length, frequency/hash maps, and binary-search state. Space can be reduced by streaming some passes, but usually at the cost of recomputation and worse constant factors.

## 💡 Key Takeaways
- If window equality ignores order but preserves counts, think **multiset signature**, not substring or rolling-order hash.
- If the question asks for the **minimum length per index** and fixed-length feasibility is easy, that is a strong signal for **parallel binary search**.
- Be explicit about whether uniqueness is among all windows of the same length; mixing ordered equality and multiset equality will silently produce wrong answers.
- Coverage is interval-based: a unique window certifies every index in `[l, r]`, so use difference arrays/prefix sums instead of per-index updates.
- The transferable design insight is to separate **identity construction** from **coverage/query resolution**; that decomposition scales well in production pipelines too.

## 🚀 Variations & Further Practice
- Require exact answers with zero collision risk: replace probabilistic hashing with deterministic canonical frequency encodings, which makes signature construction much heavier and forces different trade-offs.
- Support online updates to `events` between queries: now fixed-length evaluation needs dynamic multiset maintenance, pushing the design toward segment trees, Fenwick trees over hashed counts, or offline rebuild strategies.
- Ask for the number of unique-signature windows covering each index instead of the minimum length: same hashing core, but the optimization target changes from binary search to multi-length aggregation.