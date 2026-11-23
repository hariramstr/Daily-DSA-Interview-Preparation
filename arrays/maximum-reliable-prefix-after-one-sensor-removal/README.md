# Maximum Reliable Prefix After One Sensor Removal

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Prefix/Suffix, GCD

---

## 🗂 Problem Overview
Given an integer array `readings`, find the maximum length of a prefix that becomes reliable after removing at most one element. A prefix is reliable when every adjacent pair inside it has `gcd(a[i], a[i+1]) > 1`. The difficulty is that deletion changes adjacency, so removing one value can both eliminate one bad edge and create a new one. With `n` up to `2 * 10^5`, brute-force deletion checks are too expensive.

## 🌍 Engineering Impact
This pattern shows up in streaming validation, telemetry ingestion, and ordered event-processing systems where local compatibility constraints determine whether a pipeline can continue. Think sensor fusion chains, packet normalization stages, or ranking feature pipelines where adjacent stages must satisfy a contract. At scale, naive “try every removal” logic turns a linear health check into quadratic tail latency. The useful abstraction is not “fix one bad pair,” but “reason about local constraints under one structural edit.” That shift enables predictable throughput, bounded memory, and implementation paths that survive production-sized arrays rather than toy inputs.

## 🔍 Problem Statement
You are given an array `readings` of length `n` (`1 <= n <= 2 * 10^5`), where `1 <= readings[i] <= 10^9`. A prefix of length `L` is reliable if for every `0 <= j < L - 1`, `gcd(readings[j], readings[j + 1]) > 1`.

You may remove at most one element from the array before evaluating the prefix. After deletion, the remaining elements keep their original order, and formerly separated elements may become adjacent. Return the maximum possible reliable prefix length in the resulting array.

Examples:

- `readings = [6, 10, 15, 7, 14]` → `3`
- `readings = [8, 12, 18, 25, 35, 49]` → `3`

Edge cases matter:
- `n = 1` always returns `1`
- already fully reliable arrays may return `n`
- deleting an element near the front shifts the entire prefix
- deleting index `i` requires checking the new bridge between `i-1` and `i+1`

The `2 * 10^5` bound rules out per-deletion rescans.

## 🪜 How to Solve This
1. Read the condition carefully → reliability is defined by **adjacent GCDs**, so the real object is the edge array `good[i] = (gcd(readings[i], readings[i+1]) > 1)`.

2. Without deletion, the answer is just the first index where `good[i]` becomes false. That suggests prefix reasoning, not global optimization.

3. With one deletion, only a tiny neighborhood changes:
   - edge `(i-1, i)` disappears
   - edge `(i, i+1)` disappears
   - a new bridge `(i-1, i+1)` may appear

4. That means a deletion does **not** require recomputing the whole prefix. We only need to know:
   - how far the prefix was already good before the deletion point
   - whether the new bridge is valid
   - how long the suffix of original good edges continues after the bridge

5. This naturally leads to prefix/suffix preprocessing on the `good` array:
   - longest all-true prefix ending before each position
   - longest all-true run starting after each position

6. Then evaluate each possible deletion in O(1), plus the no-deletion case. Total cost becomes O(n log V) from GCD calls, which fits comfortably.

## 🧩 Algorithm Walkthrough
1. **Build the adjacency-validity array**  
   For each `i` in `[0, n-2]`, compute `good[i] = (gcd(readings[i], readings[i+1]) > 1)`.  
   This converts the problem from values to local edge constraints. The invariant: a prefix of length `L` is reliable iff `good[0..L-2]` are all true.

2. **Precompute prefix validity lengths**  
   Let `pref[i]` be the length of the longest all-true prefix in `good` ending at `i`, but because we only care about prefixes starting at `0`, it is enough to know whether `good[0..i]` are all true. Practically, store `prefixAllTrue[i]`.  
   Invariant: `prefixAllTrue[i]` is true iff the original array prefix of length `i+2` is reliable.

3. **Precompute suffix run lengths**  
   Let `suf[i]` be the number of consecutive true edges starting at `good[i]`.  
   This lets us extend a prefix after a deletion-created bridge without rescanning. Pattern: **Prefix/Suffix preprocessing**.

4. **Evaluate no deletion**  
   Find the first false in `good`. If none exists, answer is `n`; otherwise answer starts as that prefix length.

5. **Evaluate deleting each index `k`**  
   - `k = 0`: resulting prefix starts at original `readings[1]`; answer is `1 + suf[1]` in edge terms.
   - `k = n-1`: deletion cannot improve an earlier failure; candidate is the original reliable prefix capped at `n-1`.
   - `0 < k < n-1`: require original prefix before `k-1` to be valid, then check bridge `gcd(readings[k-1], readings[k+1]) > 1`, then append `suf[k+1]`.

   Candidate length formula:
   - left block contributes `k` elements if edges `good[0..k-2]` are all true
   - bridge adds one more element if valid
   - suffix extension adds however many consecutive true edges continue from `good[k+1]`

6. **Take the maximum over all candidates**  
   Correctness follows because any single deletion changes only one local adjacency bridge; all other edge truth values are preserved.

## 📊 Worked Example
Use `readings = [6, 10, 15, 7, 14]`.

`good` over adjacent pairs:

| i | pair      | gcd | good[i] |
|---|-----------|-----|---------|
| 0 | (6, 10)   | 2   | T       |
| 1 | (10, 15)  | 5   | T       |
| 2 | (15, 7)   | 1   | F       |
| 3 | (7, 14)   | 7   | T       |

Suffix true-run lengths on `good`:
- `suf = [2, 1, 0, 1]`

Trace deletions:
1. No deletion → first false at `good[2]` → reliable prefix length `3`
2. Delete `0` → array `[10,15,7,14]` → `good` starts `T,F,...` → prefix `2`
3. Delete `1` → bridge `(6,15)` valid, then `(15,7)` fails → prefix `2`
4. Delete `2` → bridge `(10,7)` invalid → prefix `2`
5. Delete `3` → bridge `(15,14)` invalid → prefix `3`
6. Delete `4` → earlier failure unchanged → prefix `3`

Maximum is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log V)`, where `V` is the maximum reading value. We compute `O(n)` GCDs, and each GCD costs `O(log V)`. Everything else is linear prefix/suffix preprocessing and O(1) candidate evaluation per deletion. This scales cleanly to `10^6` elements; it does not scale to `10^9` elements in memory, but the asymptotic shape is still optimal for in-memory arrays.

### Space Complexity
`O(n)` for the `good` array and suffix/prefix helper structures. You can reduce some of this to `O(1)` extra space with more careful streaming logic, but the implementation becomes less obvious and harder to verify under deletion boundary cases.

## 💡 Key Takeaways
- If a condition is defined on adjacent pairs, convert the array into an edge-validity array first; the problem often becomes a prefix/suffix run problem.
- “Remove one element” is a strong signal that only a constant-size neighborhood changes, so precompute everything outside that neighborhood.
- The main trap is forgetting the new bridge after deletion: removing `readings[k]` creates adjacency between `readings[k-1]` and `readings[k+1]`.
- Another common off-by-one bug is mixing element counts with edge counts; a reliable prefix of `L` elements contains `L-1` validated edges.
- The transferable design insight is to separate immutable global structure from the small local delta introduced by a single edit; that is the difference between quadratic retries and predictable linear systems.

## 🚀 Variations & Further Practice
- Allow removing up to `k` elements. The conceptual jump is from one local bridge to a stateful DP over deletions and prefix validity.
- Ask for the longest reliable **subarray** after one deletion instead of a prefix. That removes the fixed-start simplification and requires run merging from both sides.
- Replace `gcd(a[i], a[i+1]) > 1` with a general binary compatibility predicate. The harder part is preserving linear-time evaluation when the predicate is expensive or non-symmetric.