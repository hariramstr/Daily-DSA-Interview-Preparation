# Maximum Distinct Flavors in a Circular Tasting Menu

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Circular Array

---

## 🗂 Problem Overview

Given a circular array `flavors[]` of length `n`, find the maximum number of distinct values in any contiguous window of exactly `k` elements, subject to the constraint that no single value appears more than `m` times within that window. The circularity means windows can wrap around the array boundary. If no window satisfies the frequency constraint, return `-1`. The non-trivial tension is managing both a fixed window size and a per-element frequency cap simultaneously across a circular domain.

---

## 🌍 Engineering Impact

This pattern surfaces directly in streaming analytics and rate-limiting infrastructure. Consider a sliding-window rate limiter that must enforce per-client request caps within a fixed time bucket while simultaneously maximizing throughput diversity — the same dual constraint (window size + per-key frequency ceiling) applies. Recommendation engines face this when enforcing content-type diversity caps over a fixed-length feed. Without the circular-aware sliding window, naïve implementations either miss wrap-around violations in ring buffers or re-scan the full window on every step, collapsing throughput at scale in Kafka consumers, time-series databases, and real-time bidding pipelines.

---

## 🔍 Problem Statement

**Input:** Integer array `flavors[0..n-1]`, window size `k`, max frequency `m`.  
**Output:** Maximum distinct flavor count across all valid length-`k` circular windows, or `-1` if none exist.

**Constraints:**
- `1 ≤ n ≤ 100,000`
- `1 ≤ k ≤ n`
- `1 ≤ m ≤ k`
- `1 ≤ flavors[i] ≤ 100,000`

**Examples:**

| Input | k | m | Output |
|---|---|---|---|
| `[1, 2, 1, 3, 2, 4, 1]` | 4 | 1 | `4` |
| `[5, 5, 5, 5]` | 3 | 1 | `-1` |

**Key constraint driving the algorithm:** The window is fixed-size (not variable), which rules out the classic shrink-on-violation two-pointer pattern. Circularity adds a second axis of complexity — you must evaluate `n` distinct windows over a logically doubled index space.

**Edge cases:** `k == n` (single window, full array), `m == k` (frequency constraint is vacuous), all elements identical.

---

## 🪜 How to Solve This

1. **Fixed window size** → this is not a variable-width sliding window. You slide a rigid frame of size `k`, so there are exactly `n` windows to evaluate. No shrinking needed — just slide and update.

2. **Circularity** → rather than special-casing wrap-around logic, double the array: construct `extended = flavors + flavors`. Now every circular window of size `k` starting at index `i` (for `0 ≤ i < n`) maps to `extended[i..i+k-1]`. This is a standard linearization trick for circular problems.

3. **Two things to track per window:** distinct count and per-element frequency. A HashMap handles both — keys are flavor values, values are their counts within the current window.

4. **Validity check:** A window is valid if no frequency exceeds `m`. Tracking violations explicitly (a `violationCount` integer incremented/decremented as elements enter/leave) avoids scanning the entire map on each step.

5. **Slide:** Add the incoming element, remove the outgoing element, update `violationCount` and distinct count accordingly. Record the distinct count if `violationCount == 0`.

The result is a single O(n) pass over the doubled array — the "of course" moment is realizing that doubling the array collapses a two-dimensional circular problem into a one-dimensional linear one.

---

## 🧩 Algorithm Walkthrough

**Pattern:** Fixed-size Sliding Window with Hash Map frequency tracking over a linearized circular array.

**Steps:**

1. **Linearize:** Construct `extended[0..2n-1]` where `extended[i] = flavors[i % n]`. This gives access to all `n` circular windows without modulo arithmetic inside the hot loop.

2. **Initialize the first window (`i = 0` to `k-1`):** Populate a frequency map. Count distinct elements (map size) and count violations (flavors whose frequency exceeds `m`). This establishes the invariant: `freq[x]` always reflects the exact count of `x` in the current window.

3. **Record if valid:** If `violationCount == 0`, update `maxDistinct`.

4. **Slide (for `i` from `1` to `n-1`):**
   - **Add** `extended[i + k - 1]`: increment `freq[incoming]`. If it just hit `m+1`, increment `violationCount`. If it just hit `1`, increment `distinctCount`.
   - **Remove** `extended[i - 1]`: decrement `freq[outgoing]`. If it just dropped from `m+1` to `m`, decrement `violationCount`. If it hits `0`, decrement `distinctCount` and erase from map.
   - **Invariant maintained:** `freq` always reflects exactly the `k` elements in `extended[i..i+k-1]`.

5. **Record if valid:** If `violationCount == 0`, `maxDistinct = max(maxDistinct, distinctCount)`.

6. **Return** `maxDistinct` if updated at least once, else `-1`.

The violation counter is the critical optimization — it makes validity checking O(1) per step rather than O(n) per window.

---

## 📊 Worked Example

**Input:** `flavors = [1, 2, 1, 3, 2, 4, 1]`, `k = 4`, `m = 1`  
**Extended:** `[1, 2, 1, 3, 2, 4, 1, 1, 2, 1, 3, 2, 4, 1]`

| Window Start | Window | freq map | violations | distinct | valid? | maxDistinct |
|---|---|---|---|---|---|---|
| 0 | [1,2,1,3] | {1:2, 2:1, 3:1} | 1 (flavor 1) | 3 | ✗ | 0 |
| 1 | [2,1,3,2] | {2:2, 1:1, 3:1} | 1 (flavor 2) | 3 | ✗ | 0 |
| 2 | [1,3,2,4] | {1:1, 3:1, 2:1, 4:1} | 0 | 4 | ✓ | 4 |
| 3 | [3,2,4,1] | {3:1, 2:1, 4:1, 1:1} | 0 | 4 | ✓ | 4 |
| 4 | [2,4,1,1] | {2:1, 4:1, 1:2} | 1 (flavor 1) | 3 | ✗ | 4 |
| 5 | [4,1,1,2] | {4:1, 1:2, 2:1} | 1 (flavor 1) | 3 | ✗ | 4 |
| 6 | [1,1,2,1] | {1:3, 2:1} | 1 (flavor 1) | 2 | ✗ | 4 |

**Output: `4`**

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** — the doubled array has `2n` elements but we process exactly `n` windows, each requiring O(1) work (two map operations with amortized O(1) for integer keys). At 10⁶ elements this is comfortably sub-second; at 10⁹ it remains linear but memory pressure on the hash map becomes the practical bottleneck, not CPU cycles.

### Space Complexity

**O(min(k, d))** where `d` is the number of distinct flavor values — the frequency map holds at most `k` entries at any point (the window size), bounded above by the flavor domain size `d = 100,000`. The doubled array costs O(n) and can be eliminated by using index modulo arithmetic, trading code clarity for a 2× memory reduction.

---

## 💡 Key Takeaways

- **Pattern signal — fixed window size:** When the problem specifies an exact segment length rather than a maximum or minimum, reach for fixed-size sliding window, not the shrink-on-violation variant. The distinction is immediately visible in the constraint `k` being rigid.
- **Pattern signal — dual constraints:** Any problem combining a structural constraint (window size) with a content constraint (per-element frequency cap) maps to sliding window + frequency map. The frequency map is the "inner state" that makes the content constraint O(1) to evaluate.
- **Implementation gotcha — violation counter:** Do not recompute validity by scanning the map each step. Maintain an explicit `violationCount` integer, updated only at the precise moment a frequency crosses the `m` threshold in either direction. Forgetting the threshold-crossing condition (checking `== m+1` not `>= m+1`) is the most common off-by-one error here.
- **Implementation gotcha — circular indexing:** When using the doubled-array approach, the outer loop runs `i` from `0` to `n-1` (not `2n-1`). Running to `2n-1` double-counts windows and produces incorrect results. If using modulo arithmetic instead, ensure the outgoing index is `(i - 1 + n) % n`, not `i - 1`.
- **Architectural insight:** The violation counter pattern — maintaining a scalar summary of constraint satisfaction that updates in O(1) at boundary crossings — is directly applicable to production sliding-window monitors (SLO breach counters, rate-limit headroom trackers). It decouples the "is the window valid?" query from the "what's in the window?" state, enabling lock-free reads of the validity signal in concurrent systems.

---

## 🚀 Variations & Further Practice

- **Variable-length window with dual frequency constraints:** Find the *longest* subarray where no element exceeds frequency `m` AND at least `p` distinct values are present. The twist: the window is no longer fixed-size, so you need a two-pointer shrink strategy, but the shrink condition now involves two simultaneous invariants that can conflict — shrinking to fix one can break the other.
- **Circular window with weighted distinctness:** Each flavor has a value weight; maximize the *total weight* of distinct flavors (counting each flavor's weight once regardless of repetition) in a valid window. The twist: distinct count becomes a weighted sum, requiring a secondary accumulator that must be correctly adjusted when a flavor transitions from "present once" to "present twice" (it stops contributing weight) and back.
- **Streaming variant with eviction:** The array arrives as a stream with no known `n`; maintain the best valid window seen so far using a deque-backed circular buffer of size `k`. The twist: you cannot double the array ahead of time, so circularity must be handled with explicit modulo indexing under memory constraints, and the answer must be emittable at any point in the stream.