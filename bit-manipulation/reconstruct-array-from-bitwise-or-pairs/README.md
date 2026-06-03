# Reconstruct Array from Bitwise OR Pairs

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Array, Greedy

---

## 🗂 Problem Overview

Given `n` and a list of `(index, value)` pairs, reconstruct an array `arr` where a single hidden base value `B` satisfies `arr[i] | B == value` for every pair referencing index `i`. The non-trivial constraint is that `B` is global — every pair must be consistent with the same `B`, and each `arr[i]` must be independently minimized. Positions with no pairs default to `0`.

---

## 🌍 Engineering Impact

This pattern appears directly in **permission and capability systems** — where a base capability mask `B` is OR'd with per-resource flags to produce effective permissions, and you need to reverse-engineer the minimal per-resource grant from observed effective permissions. It also surfaces in **network packet field reconstruction** (e.g., inferring minimal header flags from observed wire values), **FPGA/hardware synthesis** (recovering minimal signal assignments from observed bus states), and **feature flag systems** where a global rollout mask combines with per-user overrides. Getting this wrong at scale means either over-granting permissions or producing unsatisfiable constraint sets that silently corrupt downstream state.

---

## 🔍 Problem Statement

**Input:** Integer `n` (array length) and a 2D array `pairs` where `pairs[i] = [index_i, value_i]`.

**Contract:** `arr[index_i] | B == value_i` for all pairs, using a single consistent non-negative integer `B`. Minimize each `arr[i]`; set unconstrained positions to `0`.

**Output:** The reconstructed array, or `[]` if no valid `B` exists.

**Constraints:**
- `1 <= n <= 1000`, `0 <= pairs.length <= 1000`
- `0 <= index_i < n`, `0 <= value_i <= 10^6`
- Multiple pairs may reference the same index — all must hold simultaneously.

**Examples:**

```
n=4, pairs=[[0,7],[1,5],[2,6]]  →  [3,1,2,0]   (B=4: 3|4=7, 1|4=5, 2|4=6)
n=3, pairs=[[0,5],[0,3]]        →  []           (5≠3, impossible for same index)
```

**Key driver:** Same index with different values is an immediate contradiction. Same `B` across all pairs is the global consistency requirement that forces a constraint-propagation approach rather than per-index greedy.

---

## 🪜 How to Solve This

1. **Same index, different values → immediate contradiction.** Group pairs by index first. If any index maps to two different values, return `[]` immediately — no `B` can satisfy `x | B == 5` and `x | B == 3` simultaneously.

2. **What does `arr[i] | B == value` tell us about `B`?** OR can only set bits, never clear them. So every bit set in `value` is either from `arr[i]` or from `B`. Every bit set in `value` *could* be in `B`. Every bit *not* set in `value` is definitely *not* in `B` — because if `B` had that bit, the OR result would have it too.

3. **Derive `B` from the values.** `B` must be a subset of every `value_i` (bitwise). So `B` is constrained to bits present in ALL values: `B ⊆ intersection_of_all_values`. To minimize `arr[i]`, maximize `B` — assign `B` exactly the intersection of all values across all pairs.

4. **Validate and assign `arr[i]`.** With `B` fixed, `arr[i] = value_i & ~B` (the bits in `value_i` not covered by `B`). Verify `arr[i] | B == value_i`. If any check fails, return `[]`.

5. **Unconstrained positions get `0`** — already minimal and consistent since `0 | B == B`, which is unconstrained.

---

## 🧩 Algorithm Walkthrough

**Pattern: Constraint Propagation + Greedy Bit Assignment**

OR constraints are one-directional (bits only accumulate), which makes them amenable to a top-down constraint intersection approach rather than backtracking.

1. **Group by index.** Build a map `index → value`. If any index appears with two distinct values, return `[]`. This enforces the per-index consistency invariant in O(pairs.length).

2. **Compute the required bits of `B`.** For each `(index, value)` pair: bits NOT in `value` cannot be in `B`. Therefore `B` must be a subset of every `value`. Compute `B_max = AND of all values across all pairs`. This is the maximal valid `B` — using it minimizes each `arr[i]` because more bits in `B` means fewer bits needed in `arr[i]`.

3. **Assign `arr[i]` for constrained positions.** For each `(index, value)`: set `arr[index] = value & ~B_max`. This strips bits already covered by `B`, leaving only what `arr[i]` must contribute exclusively.

4. **Validate each assignment.** Assert `arr[index] | B_max == value`. A failure here means `B_max` is missing a bit that `value` requires — which contradicts step 2 only if the AND computation was correct, so this catches logical errors or edge cases (e.g., `pairs` is empty, making `B_max` unconstrained).

5. **Fill unconstrained positions with `0`.** Return the array.

**Invariant maintained:** At every step, `B_max` is the largest `B` consistent with all seen pairs, ensuring `arr[i]` values are globally minimal.

---

## 📊 Worked Example

**Input:** `n=4`, `pairs=[[0,7],[1,5],[2,6]]`

Binary: `7=111`, `5=101`, `6=110`

| Step | Operation | Result |
|---|---|---|
| Group by index | `{0:7, 1:5, 2:6}` — no conflicts | Valid |
| Compute `B_max` | `7 & 5 & 6 = 111 & 101 & 110` | `B_max = 100` (4) |
| `arr[0]` | `7 & ~4 = 111 & 011 = 011` | `arr[0] = 3` |
| Validate `arr[0]` | `3 \| 4 = 7` ✓ | Pass |
| `arr[1]` | `5 & ~4 = 101 & 011 = 001` | `arr[1] = 1` |
| Validate `arr[1]` | `1 \| 4 = 5` ✓ | Pass |
| `arr[2]` | `6 & ~4 = 110 & 011 = 010` | `arr[2] = 2` |
| Validate `arr[2]` | `2 \| 4 = 6` ✓ | Pass |
| `arr[3]` | Unconstrained | `arr[3] = 0` |

**Output:** `[3, 1, 2, 0]`

---

## ⏱ Complexity Analysis

### Time Complexity

**O(pairs.length · log(max_value))** — dominated by iterating pairs to compute the AND intersection and per-index assignments. The `log(max_value)` factor (≈20 bits for 10^6) is the implicit bit-width constant. At 10^6 pairs this is comfortably sub-second; at 10^9 pairs you'd need streaming aggregation but the per-element work stays constant.

### Space Complexity

**O(n + pairs.length)** — the output array owns O(n) and the index-to-value map owns O(pairs.length). The map cannot be eliminated without two passes over `pairs`; trading it away would require sorting pairs by index first, costing O(pairs.length · log(pairs.length)) time.

---

## 💡 Key Takeaways

- **Signal: multiple constraints sharing a global hidden variable.** When constraints are of the form `f(local, global) == observed` and `global` is shared, look for constraint intersection — not per-element greedy.
- **Signal: OR/AND/XOR with unknown operands.** Bitwise OR constraints are monotone (bits only accumulate), which means the feasible set for `B` is always an intersection of bit-masks — directly computable without search.
- **Gotcha: empty `pairs` makes `B_max` undefined.** AND over an empty set is typically initialized to all-ones (`~0`), which would make `B_max` unconstrained. Decide your convention explicitly — defaulting to `B=0` is safest and produces `arr[i] = 0` for all positions.
- **Gotcha: same index, multiple pairs must be deduplicated before computing `B_max`.** If you naively AND all values including duplicates for the same index, you may over-constrain `B` — though in this problem same-index values must be identical anyway, so a conflict check must precede the AND computation.
- **Architectural insight: constraint satisfaction over bitfields is closed under AND/OR intersection.** In production permission systems, this means you can compute the minimal grant set for a role hierarchy in a single pass over observed effective permissions — no SAT solver required when the constraint operator is monotone.

---

## 🚀 Variations & Further Practice

- **Multiple hidden base values (per-group `B`).** Extend the problem so pairs are partitioned into groups, each with its own `B`. The twist: you must first infer group membership from the constraints themselves — a clustering problem layered on top of the bit reconstruction, pushing toward union-find or graph connectivity.
- **Reconstruct with AND constraints instead of OR.** `arr[i] & B == value_i` — AND is anti-monotone (bits only clear), so the feasible set for `B` becomes a union of bit-masks rather than an intersection, and the minimization objective flips. The structural reasoning is symmetric but the greedy direction reverses.
- **Reconstruct under both OR and AND constraints simultaneously.** Given `arr[i] | B == v1` and `arr[i] & B == v2` for the same index, derive both `arr[i]` and `B` exactly. This is over-determined for most inputs and requires checking XOR consistency — a useful exercise in recognizing when a system of bitwise equations has a unique solution versus no solution.