# Minimum Toggles to Match a Device XOR Fingerprint

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, XOR, Array

---

## 🗂 Problem Overview
Given an array of non-negative 32-bit integers, compute the minimum number of single-bit toggles across any elements needed so that the XOR of the entire array equals `target`. You are not matching individual values; only the final aggregate XOR matters. The key constraint is recognizing that each toggle changes exactly one bit of the global XOR, so the problem reduces to comparing the current XOR fingerprint with `target` in O(n) time and O(1) extra space.

## 🌍 Engineering Impact
This pattern shows up anywhere systems reason about aggregate parity or compact fingerprints instead of full state: distributed integrity checks, streaming deduplication, RAID-style parity validation, telemetry rollups, and hardware fault simulation. At scale, trying to “fix” individual records is the wrong abstraction; only the aggregate invariant matters. The XOR view collapses many local choices into a single global state transition. Without this reduction, teams over-engineer search or dynamic programming around per-element edits. With it, the problem becomes a linear pass plus bit counting, which is exactly the kind of simplification that keeps hot paths predictable and operationally cheap.

## 🔍 Problem Statement
You are given an integer array `states` of length `n`, where `1 <= n <= 200000` and each `states[i]` is a non-negative 32-bit integer (`0 <= states[i] <= 10^9`). You are also given `target` (`0 <= target <= 10^9`).

In one operation, you may choose any single device and toggle exactly one bit in its binary representation. Toggling flips that bit from `0` to `1` or from `1` to `0`.

Let:

`current = states[0] XOR states[1] XOR ... XOR states[n - 1]`

Return the minimum number of such operations needed so that the final XOR of the entire array equals `target`.

Examples:

- `states = [5, 1, 2], target = 0` → `2`
- `states = [7, 7, 7], target = 7` → `0`

The algorithmic constraint is explicit: solve it in O(n) time and O(1) extra space.

## 🪜 How to Solve This
1. Read the operation carefully → a single toggle flips exactly one bit in exactly one number.
2. Ask what that does globally → because XOR is bitwise and independent per bit position, flipping bit `k` in any one element flips bit `k` in the overall XOR.
3. That means device choice does not matter for the count → only which global XOR bits must change matters.
4. Compute the current aggregate fingerprint: XOR all values in `states`.
5. Compare `current` with `target` → the bits that differ are exactly the bits we must flip in the global XOR.
6. The mask `current XOR target` marks every mismatched bit with `1`.
7. Each such `1` requires one toggle, and one toggle can fix only one bit position in the global XOR.
8. Therefore, the answer is simply the population count of `current XOR target`.

This is the key mental move: stop thinking about transforming array elements, and start thinking about transforming the aggregate XOR state.

## 🧩 Algorithm Walkthrough
1. **Apply the Bit Manipulation / XOR Invariant pattern.**  
   XOR is associative and commutative, so the order of aggregation does not matter. More importantly, toggling one bit in one element flips that same bit in the total XOR and affects no other bit positions.

2. **Compute the current XOR of the array.**  
   Initialize `current = 0`, then iterate through `states` and do `current ^= value`.  
   **Why correct:** after processing the first `i` elements, `current` equals the XOR of exactly those `i` elements.  
   **Invariant:** prefix XOR is always maintained correctly.

3. **Find which global bits must change.**  
   Compute `diff = current ^ target`.  
   **Why correct:** XOR of two values has `1` exactly where the bits differ. So `diff` is the set of bit positions that must be flipped in the aggregate XOR.

4. **Count the set bits in `diff`.**  
   The minimum operations equal the number of `1`s in `diff`.  
   **Why correct:** each operation flips exactly one global bit position, so every mismatched bit needs at least one operation. Also, each mismatched bit can be fixed with exactly one operation by toggling that bit in any device.  
   **Invariant:** after conceptually fixing any subset of differing bits, the remaining required operations equal the remaining set-bit count.

5. **Return the popcount.**  
   This abstraction is optimal because the problem is defined entirely by the aggregate XOR fingerprint, not by per-element target states.

## 📊 Worked Example
Take `states = [5, 1, 2]`, `target = 0`.

| Step | Value | Running XOR (`current`) | Binary |
|---|---:|---:|---|
| Start | — | 0 | `000` |
| 1 | 5 | 5 | `101` |
| 2 | 1 | 4 | `100` |
| 3 | 2 | 6 | `110` |

Now compare with target:

- `current = 6` → `110`
- `target = 0` → `000`
- `diff = current XOR target = 6 XOR 0 = 6` → `110`

`diff` has two set bits, so two global bit positions are wrong.

That means the minimum number of single-bit toggles is `2`. For example, toggle bit 2 on any one device, then toggle bit 1 on any one device. Which devices you choose is irrelevant to the count; only the two differing XOR bit positions matter.

## ⏱ Complexity Analysis
### Time Complexity
O(n), dominated by one pass to compute the XOR of all array elements. The final popcount is O(1) for fixed-width integers. At `10^6` elements this remains trivial in practice; at `10^9`, the scan cost dominates and becomes bandwidth-bound rather than algorithmically complex.

### Space Complexity
O(1) extra space. The algorithm stores only a few scalar integers: the running XOR, the diff mask, and the bit count. There is no auxiliary structure to reduce further unless input streaming semantics change how values are supplied.

## 💡 Key Takeaways
- If the problem asks to modify elements but only constrains an aggregate XOR, that is a strong signal to reason about the aggregate state, not individual transformations.
- When an operation flips exactly one bit and XOR is involved, check whether each bit position can be treated independently.
- Do not overcomplicate device selection: any device can supply the needed toggle for a bit, so the minimum depends only on differing XOR bits.
- Be careful not to confuse “make every element match something” with “make the final XOR match target”; those are fundamentally different optimization problems.
- In production systems, aggregate invariants often admit much cheaper repair logic than per-record reconciliation if you choose the right state representation.

## 🚀 Variations & Further Practice
- Allow an operation to toggle up to `k` bits in a single device update; the twist is converting bit-count distance into grouped operations under a per-update budget.
- Require the final array XOR to equal `target` while minimizing cost where toggling bit `b` has weight `w[b]`; the twist is weighted bit repair instead of uniform popcount.
- Given many online updates to `states` and many target queries, maintain the current XOR incrementally; the twist is moving from one-shot computation to a streaming/queryable state model.