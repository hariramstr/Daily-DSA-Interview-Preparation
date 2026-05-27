# Checkered Flag Bit Counter

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Counting, Math

---

## 🗂 Problem Overview

Given an array of non-negative integers representing race car IDs, count how many are "flagged." A number is flagged when its popcount (number of set bits) strictly exceeds the number of unset bits within the span from bit 0 to the most significant bit, inclusive. The non-trivial constraint: the bit-length window is dynamic per number, not a fixed word size, so you cannot use naive 32-bit or 64-bit population count comparisons directly.

---

## 🌍 Engineering Impact

Popcount-based classification appears across several high-throughput domains. Bloom filters use set-bit density to tune false-positive rates — a filter whose bit array exceeds 50% saturation degrades precision predictably. Network packet classifiers use bit-density thresholds in hardware match rules. Feature selection in ML pipelines flags sparse vs. dense binary feature vectors using exactly this ratio. In distributed systems, consistent-hashing ring health checks use bit-weight thresholds to detect skewed token distributions. Getting the bit-length boundary wrong in any of these contexts silently corrupts results at scale.

---

## 🔍 Problem Statement

**Input:** `carIds` — an integer array, `1 <= carIds.length <= 10^4`, `0 <= carIds[i] <= 10^6`.

**Output:** Count of integers where `popcount(n) > bit_length(n) - popcount(n)`, i.e., set bits strictly outnumber unset bits within the minimal binary representation.

**Edge cases:**
- `0` is explicitly not flagged (no set bits, no window).
- `2` (`'10'`) has 1 set and 1 unset — equality is not flagged.
- `1` (`'1'`) has 1 set and 0 unset — flagged.

**Examples:**

| Input | Output |
|---|---|
| `[7, 5, 4, 6, 1]` | `4` |
| `[0, 2, 8, 15]` | `1` |

The key constraint driving the algorithm: the comparison window is `floor(log2(n)) + 1` bits wide, not 32 or 64, so unset bits must be counted relative to the number's own bit length.

---

## 🪜 How to Solve This

1. **Read the condition** → "set bits > unset bits within the minimal representation." Rewrite algebraically: if `L = bit_length(n)` and `S = popcount(n)`, then flagged means `S > L - S`, which simplifies to `2S > L`.

2. **Recognize the two primitives needed** → `popcount(n)` and `bit_length(n)`. Both are O(log n) naively, but most languages expose them as single intrinsic operations (`bin(n).count('1')`, `n.bit_length()` in Python; `__builtin_popcount` + `32 - __builtin_clz` in C++).

3. **Handle the zero edge case explicitly** → `0.bit_length()` returns 0 in Python, so `2*0 > 0` is false — the constraint is automatically satisfied without a special branch, but verify this in your target language.

4. **Iterate once** → No sorting, no grouping, no auxiliary structure. A single linear scan with a counter suffices. The problem reduces to: for each element, evaluate a constant-time predicate, accumulate.

The rewrite `2S > L` is the insight. Once you see it, the implementation is three lines.

---

## 🧩 Algorithm Walkthrough

**Pattern: Linear Scan with Constant-Time Predicate**

This is the right abstraction because each element is evaluated independently — there is no inter-element dependency, no ordering requirement, and no state that accumulates across elements beyond the running count.

**Steps:**

1. **Initialize counter** `flagged = 0`. This is the only mutable state.

2. **Iterate over each `n` in `carIds`.**

3. **Compute `L = bit_length(n)`** — the position of the most significant bit plus one. This defines the comparison window. For `n = 0`, `L = 0`.

4. **Compute `S = popcount(n)`** — the number of 1-bits. Language intrinsics make this O(1) in practice; a naive loop is O(log n).

5. **Evaluate the predicate `2 * S > L`** — algebraically equivalent to `S > L - S` but avoids computing unset bits as a separate step and eliminates the subtraction-underflow risk if you were working in unsigned arithmetic.

6. **Increment `flagged` if predicate holds.**

7. **Return `flagged`.**

**Invariant maintained:** At every iteration boundary, `flagged` equals the count of flagged numbers seen so far. The predicate is stateless per element, so correctness is trivially inductive.

**Why not a bitmask lookup table?** With `carIds[i] <= 10^6` (20 bits max), a precomputed boolean array of size `10^6 + 1` would reduce per-element work to a single array lookup — a valid optimization if the same dataset is queried repeatedly.

---

## 📊 Worked Example

Input: `[7, 5, 4, 6, 1]`

| `n` | Binary | `L` (bit_length) | `S` (popcount) | `2S` | `2S > L` | `flagged` |
|---|---|---|---|---|---|---|
| 7 | `111` | 3 | 3 | 6 | 6 > 3 ✅ | 1 |
| 5 | `101` | 3 | 2 | 4 | 4 > 3 ✅ | 2 |
| 4 | `100` | 3 | 1 | 2 | 2 > 3 ❌ | 2 |
| 6 | `110` | 3 | 2 | 4 | 4 > 3 ✅ | 3 |
| 1 | `1` | 1 | 1 | 2 | 2 > 1 ✅ | 4 |

**Output: `4`** — matches the expected result. Note that `6` and `1` are both flagged, confirming the window is minimal-representation width, not a fixed word size.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** where `n = carIds.length`. Each element requires O(1) work: `bit_length` and `popcount` are hardware-assisted or O(log V) at worst (V = max value = 10^6, so log V ≈ 20 — effectively constant). At 10^6 elements this is a single tight loop; at 10^9 elements, memory bandwidth dominates, not computation.

### Space Complexity

**O(1)** auxiliary space. Only the counter and two scalar temporaries per iteration are allocated. The input array is read-only. If a lookup-table optimization is applied, space becomes O(V) = O(10^6) — a 1 MB boolean array, a deliberate time-space trade-off worth making only under repeated-query workloads.

---

## 💡 Key Takeaways

- **Pattern signal — bit-density threshold:** Any problem comparing set bits to total bits within a number's own representation is a popcount-vs-bit_length problem. The algebraic rewrite `2S > L` is the canonical reduction.
- **Pattern signal — fixed-width vs. minimal-width:** When a problem says "considering only bits up to the most significant bit," that is a deliberate signal to use `bit_length(n)`, not `sizeof(int) * 8`. Conflating the two produces wrong answers on sparse numbers like `8` (`1000`).
- **Gotcha — equality is not flagged:** The condition is *strictly* greater. `2` (`10`) has `2S = L = 2`, which fails `2S > L`. An off-by-one here (using `>=`) inflates the count silently.
- **Gotcha — zero handling:** `0` has `bit_length = 0` and `popcount = 0`, so `2*0 > 0` is false — correctly not flagged. Verify this holds in your language before adding a special-case branch that may itself introduce a bug.
- **Architectural insight:** Expressing a multi-step bit condition as a single algebraic inequality (`2S > L`) is the same discipline as reducing a compound database predicate to a single indexed expression — it eliminates intermediate state, reduces branch count, and maps directly to vectorizable operations in hot loops.

---

## 🚀 Variations & Further Practice

- **Variable-width threshold:** Instead of the 50% threshold, count numbers where set bits exceed a fraction `k/m` of total bits (e.g., at least 2/3 set). The twist: `S * m > L * k` is still integer arithmetic, but choosing `k` and `m` to avoid overflow at 64-bit word sizes requires careful bounds analysis.
- **Streaming / sliding window variant:** Given a stream of car IDs, maintain the count of flagged numbers within the last `W` arrivals. The twist: you need an eviction mechanism (circular buffer + per-element predicate cache), turning an O(1)-space problem into an O(W)-space one with amortized O(1) updates.
- **Rank by bit density:** Instead of a binary flagged/not-flagged classification, return IDs sorted by `S / L` ratio descending. The twist: rational comparison without floating point requires cross-multiplication (`S1 * L2 > S2 * L1`), and stable sort behavior on ties becomes a correctness requirement.