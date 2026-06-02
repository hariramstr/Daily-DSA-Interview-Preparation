# Flip Bits to Match Target Pattern

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, XOR, Hamming Distance

---

## 🗂 Problem Overview

Given two non-negative integers `source` and `target`, return the minimum number of single-bit flips required to transform `source` into `target`. Each flip toggles exactly one bit position. The constraint that makes this non-trivial isn't the algorithm — it's recognizing that "minimum flips" maps directly to a well-defined metric (Hamming distance), and that XOR exposes differing bits in a single operation, collapsing what could be an iterative comparison into a counting problem.

---

## 🌍 Engineering Impact

This pattern is foundational in several production domains. Error-correcting codes (Hamming codes, Reed-Solomon) use bit-difference counting to detect and correct transmission errors at scale in storage systems and network protocols. Locality-sensitive hashing in approximate nearest-neighbor search (used in recommendation engines and vector databases) relies on Hamming distance to bucket similar items. Feature flag systems and permission bitmasks in authorization layers use XOR diffing to audit state transitions — knowing exactly which permissions changed between two role snapshots without iterating individual flags. Without this O(1)-per-pair primitive, these systems degrade to linear scans.

---

## 🔍 Problem Statement

**Input:** Two non-negative integers, `source` and `target`, where `0 <= source, target <= 10^9`.  
**Output:** An integer representing the minimum number of bit flips to convert `source` to `target`.

A bit flip toggles a single bit at any position from `0→1` or `1→0`. Since each differing bit requires exactly one flip and flips are independent, the answer equals the count of bit positions where `source` and `target` differ — the Hamming distance.

**Examples:**

| source | target | Binary source | Binary target | XOR    | Output |
|--------|--------|---------------|---------------|--------|--------|
| 10     | 15     | `1010`        | `1111`        | `0101` | 2      |
| 0      | 8      | `0000`        | `1000`        | `1000` | 1      |

**Edge cases:** `source == target` → output `0`. Either operand equals `0` → XOR equals the other operand; count its set bits.

The constraint `source, target <= 10^9` fits within 30 bits, well inside a 32-bit integer — no overflow or big-integer handling required.

---

## 🪜 How to Solve This

1. **Read the goal** → "minimum flips to convert" means we want to touch only the bits that *need* to change. Flipping a bit that already matches would require flipping it back — strictly suboptimal. So the minimum is exactly the count of mismatched bit positions.

2. **Identify the mismatch detector** → XOR is the canonical "difference" operator for bits. `a XOR b` produces `1` at every position where `a` and `b` differ and `0` where they agree. This directly encodes the set of bits that need flipping.

3. **Reduce to a known primitive** → After XOR, the problem becomes "count the number of `1` bits in an integer" — popcount. This is a solved problem with a hardware instruction (`POPCNT`) and well-known software implementations.

4. **Verify the reduction is lossless** → Each `1` in `XOR(source, target)` corresponds to exactly one required flip, and flips are independent (flipping bit *i* doesn't affect bit *j*). The reduction is exact, not approximate.

The solution is two operations: XOR then popcount. Any approach doing more work is solving a harder problem than the one stated.

---

## 🧩 Algorithm Walkthrough

**Pattern: XOR + Popcount (Hamming Distance)**

This is the canonical Hamming distance computation. XOR acts as a bitwise inequality detector; popcount counts the resulting set bits.

**Steps:**

1. **Compute `diff = source XOR target`.**  
   XOR produces `1` at every bit position where the operands differ. This is correct because XOR is the bitwise definition of "not equal." The invariant: every `1` bit in `diff` represents one required flip.

2. **Count set bits in `diff` (popcount).**  
   Iterate using Brian Kernighan's algorithm: while `diff != 0`, execute `diff = diff & (diff - 1)` to clear the lowest set bit, incrementing a counter each iteration. This runs in O(k) where k is the number of set bits — faster than iterating all 32 bit positions when the number is sparse.  
   Alternatively, use the language's built-in: `Integer.bitCount()` (Java), `bin(diff).count('1')` (Python), `__builtin_popcount()` (C++), or `math/bits.OnesCount()` (Go). These typically compile to a single `POPCNT` instruction on modern hardware.

3. **Return the count.**  
   The count is both the minimum and the exact number of flips — there is no way to do fewer, and no reason to do more.

**Why this abstraction fits:** The problem is structurally a Hamming distance query between two fixed-width binary strings. XOR + popcount is the standard O(1) implementation of that metric.

---

## 📊 Worked Example

**Input:** `source = 10`, `target = 15`

| Step | Operation | Value (binary) | Value (decimal) | Counter |
|------|-----------|----------------|-----------------|---------|
| 1 | `diff = 10 XOR 15` | `1010 XOR 1111` | `0101` = 5 | — |
| 2 | `diff & (diff-1)` → clear bit 0 | `0101 & 0100` | `0100` = 4 | 1 |
| 3 | `diff & (diff-1)` → clear bit 2 | `0100 & 0011` | `0000` = 0 | 2 |
| 4 | `diff == 0`, halt | — | — | **2** |

**Output:** `2`

Bits at positions 0 and 2 differ between `source` and `target`. Two flips transform `1010` → `1111`. No ordering of flips matters; they are independent operations.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(1)** — bounded by the fixed word size (32 bits for this constraint). The XOR is a single CPU instruction. Popcount via Kernighan's algorithm runs in O(k) iterations where k ≤ 30 (set bits in a 30-bit number). At any scale — 10^6 or 10^9 queries — each individual call is constant time; throughput scales linearly with query volume.

### Space Complexity

**O(1)** — only two scalar integers (`diff` and a counter) are allocated regardless of input magnitude. No auxiliary data structures. Space cannot be meaningfully reduced further; these two variables are the irreducible minimum.

---

## 💡 Key Takeaways

- **Pattern signal — "minimum changes between two states":** Whenever a problem asks for the minimum number of independent, reversible, single-element changes between two fixed-width representations, suspect Hamming distance and reach for XOR + popcount.
- **Pattern signal — "which bits/flags changed":** If you're auditing a transition between two bitmask states (permissions, feature flags, configuration words), XOR immediately gives you the change set — no iteration required.
- **Gotcha — don't assume 32-bit alignment:** Python integers are arbitrary precision; `bin(n).count('1')` works correctly for any size. In C/C++, use `unsigned` types or `__builtin_popcountll` for 64-bit values to avoid undefined behavior on negative inputs.
- **Gotcha — `source == target` edge case:** XOR produces `0`, popcount of `0` is `0`. Verify your popcount implementation handles this without entering the loop or returning an off-by-one result.
- **Architectural insight:** Hamming distance over bitmasks is an O(1)-per-pair similarity metric. In systems that need to compare millions of state vectors (feature flags, permission sets, cache-line dirty bits), encoding state as integers and using XOR + popcount is orders of magnitude faster than field-by-field comparison — a design decision that compounds at scale.

---

## 🚀 Variations & Further Practice

- **Total Bit Flips Across an Array (LeetCode #477 — Total Hamming Distance):** Given an array of integers, compute the sum of Hamming distances between all pairs. The naive O(n²) per-pair XOR blows up; the insight is to count, per bit position, how many numbers have that bit set vs. unset, then multiply — reducing to O(32·n). The conceptual twist is moving from pairwise comparison to a column-wise contribution model.
- **Minimum Bit Flips to Reach Target with Constraints (LeetCode #2220):** A variant where you must count flips in a sliding window of k bits, requiring you to combine the XOR-popcount primitive with a windowed traversal. The twist is that the "target" is positional, not global — local alignment matters.
- **Hamming Code Error Detection/Correction:** Implement a single-error-correcting Hamming code encoder/decoder. The conceptual leap is using XOR across *multiple* codewords to locate the erroneous bit position, not just detect that an error exists — turning Hamming distance from a metric into a corrective mechanism.