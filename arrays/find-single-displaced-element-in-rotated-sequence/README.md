# Find the Single Displaced Element in a Rotated Sequence

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Math, Prefix Sum

---

## 🗂 Problem Overview

Given an array of `n` integers that represents a rotated sequence of `[1..n]` with exactly one element swapped out for a foreign value, return that foreign element. The input/output contract is simple: one integer in, one integer out. The non-trivial constraint is that the array is *rotated*, meaning you cannot assume sorted order or a predictable starting point — you must reconstruct what the sequence *should* contain before you can identify what doesn't belong.

---

## 🌍 Engineering Impact

This pattern surfaces wherever you need to detect a single anomaly in a structurally predictable sequence without a full scan or sort. Kafka consumer offset tracking uses exactly this logic to detect a single dropped or duplicated message in an ordered partition window. Distributed sequence ID generators (Snowflake, ULIDs) use sum-based integrity checks to flag a corrupted ID in a batch. Database WAL (write-ahead log) replay pipelines use rotated-sequence validation to catch a single corrupted log entry before applying it. Without this approach, the fallback is a full set-difference scan — O(n log n) with allocations — which becomes a bottleneck at millions of events per second.

---

## 🔍 Problem Statement

**Input:** An array `nums` of `n` integers, originally the sequence `[1, 2, ..., n]` rotated at an unknown pivot `k`, producing `[k, k+1, ..., n, 1, 2, ..., k-1]`. Exactly one element has been replaced by a value outside this sequence.

**Output:** The displaced (replaced) element — the integer that does not belong.

**Constraints:**
- `2 <= n <= 10^4`
- `1 <= nums[i] <= 2 * n`
- Exactly one element is displaced; all others are distinct and form a valid rotated consecutive sequence.

**Examples:**

| Input | Output | Reason |
|---|---|---|
| `[4, 5, 6, 7, 99, 1, 2, 3]` | `99` | Expected `8` at index 4; `99` is foreign |
| `[3, 4, 0, 1, 2]` | `0` | Expected `5` at index 2; `0` is out of range |

The key constraint driving the algorithm: the *expected* sum of `[1..n]` is deterministic, and the rotated form preserves that sum — so any deviation is entirely attributable to the single displaced element.

---

## 🪜 How to Solve This

1. **Observe the structure** → A rotated `[1..n]` sequence is still a permutation of `[1..n]`. Rotation changes order, not membership. The sum is invariant under rotation.

2. **Sum is a fingerprint** → The expected sum of any permutation of `[1..n]` is `n * (n + 1) / 2`. This is computable in O(1) with no knowledge of the pivot.

3. **One element is wrong** → The actual sum of `nums` differs from the expected sum by exactly `(displaced_value - missing_value)`. But we need to isolate the displaced value, not just the delta.

4. **Find the missing value independently** → Because the array is a rotated consecutive sequence minus one element plus one foreign element, we can identify the missing value by scanning for the broken step in the sequence. In a valid rotated array, exactly one "step" between adjacent elements will be non-unit (the wrap-around from `n` back to `1`). A second broken step — or a value out of `[1..n]` range — reveals the displaced position and lets us compute what *should* be there.

5. **Combine** → `displaced = nums[broken_index]`, `missing = expected_value_at_that_position`. Return `displaced`.

The insight: treat sum as a checksum and structural adjacency as a positional validator — two independent signals that together fully constrain the answer.

---

## 🧩 Algorithm Walkthrough

**Pattern: Math + Linear Scan (Sum Checksum + Sequence Integrity)**

This is the right abstraction because the problem gives you two invariants — sum and consecutive adjacency — and one unknown. One invariant is sufficient; using both makes the solution robust and easy to verify.

**Steps:**

1. **Compute `expected_sum = n * (n + 1) / 2`** — This is the sum of the intact rotated sequence. O(1), no allocation.

2. **Compute `actual_sum = sum(nums)`** — Single pass, O(n). The difference `delta = actual_sum - expected_sum` equals `displaced - missing`.

3. **Locate the rotation pivot** — Scan for the index where `nums[i] > nums[i+1]` and the drop is not exactly `n - 1` (the valid wrap from `n` to `1`). This identifies where the sequence breaks unexpectedly.

4. **Reconstruct the expected value at the broken index** — Using the pivot and the index position, compute what value *should* sit at that position in the intact rotated sequence: `expected_at_i = (pivot_value + offset - 1) % n + 1`.

5. **Return `nums[broken_index]`** — This is the displaced element. Optionally verify: `nums[broken_index] - expected_at_i` should equal `delta` as a consistency check.

**Invariant maintained:** At every step, the expected sequence is fully reconstructible from `n` and the pivot alone — no auxiliary storage needed.

---

## 📊 Worked Example

Input: `nums = [4, 5, 6, 7, 99, 1, 2, 3]`, `n = 8`

| Step | Operation | Value |
|---|---|---|
| Expected sum | `8 * 9 / 2` | `36` |
| Actual sum | `4+5+6+7+99+1+2+3` | `127` |
| Delta | `127 - 36` | `+91` |
| Find pivot | `nums[5]=1 < nums[4]=99`; valid wrap is `8→1` (diff=7); actual diff=98 → broken | index `4` |
| Pivot value | `nums[5] = 1`, pivot start = `4` | sequence starts at `4` |
| Expected at index 4 | `4 + 4 = 8` (4th element of sequence starting at 4) | `8` |
| Displaced element | `nums[4] = 99` | **`99`** |
| Verify | `99 - 8 = 91 == delta` | ✓ |

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** — Two linear passes: one to sum the array, one to locate the broken adjacency. At 10^6 elements this is a trivial microsecond-range operation. At 10^9 elements it remains linear and cache-friendly with no branching overhead, making it suitable for hot-path stream processing.

### Space Complexity

**O(1)** — Only scalar accumulators are used: `expected_sum`, `actual_sum`, `pivot_index`. No auxiliary arrays or hash structures. This cannot be reduced further; the current form is already optimal. The trade-off foregone is a set-based O(n) space approach that would be marginally simpler to implement but wasteful at scale.

---

## 💡 Key Takeaways

- **Pattern signal #1:** When a problem guarantees a known structure (consecutive integers, sorted order, fixed-size window) with exactly one violation, reach for a mathematical invariant — sum, XOR, or product — before reaching for a data structure.
- **Pattern signal #2:** "Rotated sequence" is a disguised permutation problem. Rotation preserves sum and set membership; it only changes positional relationships. Recognizing this collapses the search space immediately.
- **Gotcha #1:** The valid wrap-around step (from `n` back to `1`) is a legitimate adjacency break — do not confuse it with the displaced element's break. Failing to distinguish these two produces a false positive at the rotation pivot.
- **Gotcha #2:** The displaced value can be *less than* the missing value (as in Example 2 where `0` replaces `5`), making `delta` negative. Ensure your delta comparison is signed, not absolute.
- **Architectural insight:** Sum-based checksums are a zero-overhead integrity primitive. Embedding expected-sum validation in batch ingestion pipelines (message queues, ETL loaders, log shippers) catches single-element corruption at the cost of one addition per element — far cheaper than a full set-difference audit triggered after the fact.

---

## 🚀 Variations & Further Practice

- **Multiple displaced elements:** If `k` elements are replaced instead of one, the sum delta alone is insufficient — you need to identify each broken adjacency independently, turning this into a multi-point sequence repair problem. The conceptual twist: you can no longer use a single checksum; you need positional reconstruction across `k` unknown gaps simultaneously.
- **2D rotated matrix with one displaced cell:** Extend the problem to a row-wise and column-wise rotated matrix where one cell is corrupted. The twist: you must validate two independent sequence invariants (row sum and column sum) and find their intersection to localize the single corrupted cell — a constraint-satisfaction problem masquerading as an array problem.
- **LeetCode 268 – Missing Number / LeetCode 287 – Find the Duplicate Number:** These strip away the rotation complexity and isolate the pure sum-checksum and pigeonhole mechanics respectively, making them useful for building the foundational intuition before tackling rotated variants.