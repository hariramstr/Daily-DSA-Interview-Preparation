# Maximum Distinct Prime Factors in a Subarray Window

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Number Theory, Hash Map

---

## 🗂 Problem Overview

Given an integer array `nums` and integers `k` and `maxUnique`, find the maximum sum of any contiguous subarray of length exactly `k` where the union of all distinct prime factors across every element in the window contains at most `maxUnique` distinct primes. The non-trivial constraint is the union condition: elements share prime factors, so adding or removing a single element can collapse or expand the prime set in non-obvious ways, making a naive recount per window prohibitively expensive.

---

## 🌍 Engineering Impact

This pattern — maintaining a frequency-counted set over a sliding window with a cardinality constraint — appears directly in streaming analytics pipelines (e.g., counting distinct event types in a tumbling window in Apache Flink or Kafka Streams), network intrusion detection (distinct protocol fingerprints per time slice), and search ranking (diversity constraints on result sets). Without the sliding-window + frequency-map approach, a naive O(n·k) recomputation per window blows up at 10⁵ scale and becomes completely untenable in real-time systems with sub-millisecond SLA requirements.

---

## 🔍 Problem Statement

**Input:** Integer array `nums` (length up to 10⁵), integer `k` (window size), integer `maxUnique` (prime cardinality cap, 1–15).

**Output:** Maximum sum of any length-`k` subarray whose elements collectively have at most `maxUnique` distinct prime factors. Return `-1` if no valid window exists.

**Key constraint driving the algorithm:** Prime factors are shared across elements. Removing an element from the window only eliminates its contributed primes if no other element in the window shares them — requiring a reference-count map, not a plain set.

**Examples:**

```
Input:  nums = [2, 3, 5, 7, 11, 13], k = 2, maxUnique = 2
Output: 24   // window [11, 13], each prime contributes exactly one distinct factor

Input:  nums = [6, 10, 15, 4, 9, 14], k = 3, maxUnique = 3
Output: 31   // window [6, 10, 15], union = {2, 3, 5}
```

**Edge cases:** `k == nums.length` (single window), elements equal to 1 (no prime factors), and elements that are large primes (single-factor contribution).

---

## 🪜 How to Solve This

1. **Fixed window size → fixed-size sliding window.** When `k` is fixed, you don't need two pointers with variable bounds. Slide a window of exactly `k` elements left to right.

2. **Membership with shared factors → frequency map, not a set.** A plain set breaks on removal: if both `6 = {2,3}` and `10 = {2,5}` are in the window and you remove `6`, the prime `2` is still present via `10`. You need a count of *how many elements contribute each prime*, so removal only evicts a prime when its count drops to zero.

3. **Prime factorization is the expensive sub-problem.** Factorize each element once, up front. With `nums[i] ≤ 10⁵`, trial division up to √n is fast enough, but a precomputed smallest-prime-factor (SPF) sieve makes each factorization O(log n) — a worthwhile investment at 10⁵ elements.

4. **Validity check is O(1).** Track `distinctCount` as an integer alongside the map. When a prime's count goes from 0→1, increment it; when it goes 1→0, decrement it. The window is valid iff `distinctCount ≤ maxUnique`.

5. **Track running sum.** Maintain a rolling sum — add the incoming element, subtract the outgoing element — and record the maximum sum over all valid windows.

---

## 🧩 Algorithm Walkthrough

**Pattern: Fixed-Size Sliding Window + Prime-Factor Frequency Map**

This is the right abstraction because the window size is fixed (no shrinking needed), and the validity predicate is a cardinality constraint on a multiset — exactly what a frequency map with a live counter handles in O(1) per update.

**Steps:**

1. **Precompute SPF sieve** up to `max(nums[i])`. For each integer `n`, the SPF sieve stores its smallest prime factor, enabling O(log n) factorization via repeated division.

2. **Factorize all elements** once, storing `factors[i]` as a set of distinct primes for `nums[i]`. Element `1` gets an empty set.

3. **Initialize the first window** `[0, k-1]`: for each element, iterate its prime factors, increment their count in `primeFreq`, and increment `distinctCount` when a prime's count transitions 0→1. Compute the initial window sum.

4. **Slide the window** from index `k` to `n-1`:
   - **Add** `nums[i]`: for each prime in `factors[i]`, increment `primeFreq[p]`; if it was 0, increment `distinctCount`. Add `nums[i]` to `windowSum`.
   - **Remove** `nums[i-k]`: for each prime in `factors[i-k]`, decrement `primeFreq[p]`; if it reaches 0, decrement `distinctCount`. Subtract `nums[i-k]` from `windowSum`.

5. **Check validity** after each slide: if `distinctCount ≤ maxUnique`, update `maxSum`.

6. **Return** `maxSum` if any valid window was found, else `-1`.

**Invariant maintained:** `distinctCount` always equals the number of keys in `primeFreq` with a value greater than zero.

---

## 📊 Worked Example

`nums = [6, 10, 15, 4, 9]`, `k = 3`, `maxUnique = 3`

Precomputed factors: `6→{2,3}`, `10→{2,5}`, `15→{3,5}`, `4→{2}`, `9→{3}`

| Step | Window | Added Primes | Removed Primes | `primeFreq` (nonzero) | `distinctCount` | `windowSum` | Valid? | `maxSum` |
|------|--------|--------------|----------------|-----------------------|-----------------|-------------|--------|----------|
| Init | [6,10,15] | {2,3},{2,5},{3,5} | — | {2:2, 3:2, 5:2} | 3 | 31 | ✅ | 31 |
| i=3 | [10,15,4] | {2} | {2,3} | {2:2, 3:1, 5:2} | 3 | 29 | ✅ | 31 |
| i=4 | [15,4,9] | {3} | {2,5} | {2:1, 3:2, 5:1} | 3 | 28 | ✅ | 31 |

**Result:** `31`

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n · log(max\_val))** — the dominant cost is factorizing each element via the SPF sieve in O(log n) time, done once per element. The sieve construction itself is O(M log log M) where M = max(nums[i]) ≤ 10⁵. Sliding-window updates are O(log n) per step due to iterating each element's prime factors (bounded by ~6 for values ≤ 10⁵). At 10⁶ elements this remains well within budget; at 10⁹ elements, the sieve approach would need rethinking.

### Space Complexity

**O(M + n)** — O(M) for the SPF sieve (M ≤ 10⁵, ~400 KB), O(n) for the precomputed factors array, and O(maxUnique) ≤ O(15) for the sliding `primeFreq` map. The sieve is the dominant allocation and cannot be eliminated without reverting to O(√n) per-element trial division, which trades space for a constant-factor time increase.

---

## 💡 Key Takeaways

- **Pattern signal — fixed window + set cardinality constraint:** Any time a problem asks for the best fixed-length subarray subject to a "how many distinct X" bound, reach for a frequency map with a live counter rather than recomputing the set from scratch each step.
- **Pattern signal — "union of properties" across elements:** When validity depends on the union of per-element attributes (not just per-element values), the window's state is a multiset, and removal requires reference counting — a plain set will produce incorrect results on eviction.
- **Implementation gotcha — prime 1:** The number `1` has no prime factors. Failing to handle it (e.g., entering an infinite factorization loop) is a common off-by-one in SPF-based factorization; always special-case `n == 1` before the factorization loop.
- **Implementation gotcha — distinctCount drift:** If you update `distinctCount` inside the prime-factor loop without checking the before/after count transition (0→1 or 1→0), you'll double-count primes shared between elements entering or leaving the window in the same step.
- **Architectural insight:** The frequency-map-with-live-counter pattern is the production-grade primitive for any streaming system that must enforce cardinality constraints over a window — it generalizes directly to distinct-user-count rate limiting, diversity-capped recommendation feeds, and anomaly detection on event-type entropy.

---

## 🚀 Variations & Further Practice

- **Variable-size window with the same prime constraint:** Instead of fixed `k`, find the *longest* subarray with at most `maxUnique` distinct prime factors. This requires a two-pointer shrink step and the added complexity that shrinking the window may not immediately restore validity — you need to decide whether to shrink from the left greedily or track multiple candidate windows.
- **Weighted prime penalty:** Assign a cost to each distinct prime and enforce a total cost budget rather than a count budget. The frequency map structure survives, but the validity check becomes a sum comparison, and the interaction between element removal and cost delta requires careful bookkeeping — particularly when a single element contributes multiple high-cost primes.
- **2D sliding window with prime constraints:** Extend to a matrix where you slide a `k×k` subgrid and enforce the prime-union constraint across all cells. The naive approach is O(n²·k²·log M); the efficient approach decomposes into row-wise prefix structures and a 1D sliding window over column aggregates, combining this pattern with sparse table or segment tree range queries.