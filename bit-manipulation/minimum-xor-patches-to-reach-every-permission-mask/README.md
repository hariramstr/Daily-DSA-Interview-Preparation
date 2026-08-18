# Minimum XOR Patches to Reach Every Permission Mask

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Linear Basis, XOR

---

## 🗂 Problem Overview
Given deployed role masks over `b` permission bits, determine the minimum number of additional masks needed so that every value in `[0, 2^b - 1]` can be expressed as the XOR of some subset of deployed masks. The challenge is that mask count is irrelevant by itself: only linearly independent masks increase expressive power. With `n` up to `2 * 10^5` and `b` up to `60`, brute-force subset generation is impossible; the solution depends on computing span dimension in XOR space.

## 🌍 Engineering Impact
This pattern shows up anywhere binary feature combinations define reachable system states: permission systems, network policy encodings, erasure coding, cryptographic key derivation, compiler dataflow lattices, and distributed feature-flag composition. At scale, treating masks as plain integers instead of vectors over `GF(2)` leads to wrong capacity estimates, redundant state rollout, and wasted storage or control-plane churn. The linear-basis view tells you what is actually expressible, what is redundant, and the minimum independent additions required to cover the full state space. That matters when rollout cost, policy count, or control-plane latency is constrained.

## 🔍 Problem Statement
You are given an array `roles` of length `n`, where each `roles[i]` is an integer in `[0, 2^b - 1]`. Each value represents a deployed role mask across `b` permission bits. You may add new masks, called patches.

After patching, the deployed set is fully expressive if every `b`-bit mask can be formed as the XOR of some subset of deployed masks. Each mask can be used at most once, and the empty subset yields `0`.

Return the minimum number of patches required.

Key constraints:
- `1 <= n <= 2 * 10^5`
- `1 <= b <= 60`
- `0 <= roles[i] < 2^b`

Examples:
- `roles = [1, 2], b = 3` → `1`
- `roles = [3, 5, 6], b = 3` → `1`

The critical observation is that reachability depends on the dimension of the XOR span, not on how many masks are present. That rules out subset enumeration and points directly to linear basis construction.

## 🪜 How to Solve This
1. Read the requirement carefully → “every mask must be representable as XOR of a subset.” That is not a subset-sum search problem; it is a span problem over bits.

2. Translate integers into vectors over `GF(2)` → each bit position is a coordinate, XOR is vector addition, and a subset XOR is just a linear combination with coefficients in `{0,1}`.

3. Ask what “fully expressive” means → the deployed masks must span the entire `b`-dimensional space. A full span over `b` bits has dimension exactly `b`.

4. So the real task becomes: what is the current rank of `roles`? Any mask already representable by others is redundant and does not help.

5. Compute that rank with a XOR linear basis → process masks one by one, eliminate highest set bits using existing basis vectors, and keep only vectors that introduce a new pivot bit.

6. Once rank is known, the answer is immediate → if current dimension is `r`, then `b - r` additional independent masks are necessary and sufficient.

That is the whole reduction: from exponential subset reachability to rank deficiency in a 60-dimensional vector space.

## 🧩 Algorithm Walkthrough
1. **Use the Linear Basis / Gaussian Elimination over `GF(2)` pattern.**  
   This is the right abstraction because XOR subset generation forms a vector space. The only thing that matters is the number of independent directions currently available.

2. **Maintain a basis array indexed by bit position.**  
   Let `basis[k]` store a mask whose highest set bit is `k`, or `0` if no such pivot exists yet. This gives a canonical elimination structure for up to `b <= 60` bits.

3. **Insert each role mask into the basis.**  
   For a mask `x`, scan bits from high to low. If bit `k` is set:
   - If `basis[k]` exists, replace `x` with `x XOR basis[k]` to eliminate that pivot.
   - Otherwise, store `x` in `basis[k]` and increase rank by `1`.  
   If `x` becomes `0`, it was dependent on existing vectors and adds no new expressive power.

4. **Invariant:** after processing any prefix of `roles`, the non-zero basis vectors are independent, and every processed role is representable as an XOR of those basis vectors.  
   This is exactly the property needed to compute span dimension correctly.

5. **Derive the answer from rank deficiency.**  
   A `b`-bit space contains all masks iff span dimension is `b`. If current rank is `r`, then at least `b - r` new independent masks are required. This is also sufficient, because each patch can add at most one new dimension, and you can always choose masks outside the current span until the basis reaches full rank.

6. **Return `b - rank`.**  
   No subset construction, enumeration, or DP is needed. The algorithm is bounded by `n * b`, which is practical even at the upper constraint limit.

## 📊 Worked Example
Take `roles = [3, 5, 6]`, `b = 3`.

| Step | Mask | Binary | Action | Basis After | Rank |
|---|---:|---:|---|---|---:|
| 1 | 3 | `011` | No pivot at bit 1 → insert | `{1: 011}` | 1 |
| 2 | 5 | `101` | No pivot at bit 2 → insert | `{2: 101, 1: 011}` | 2 |
| 3 | 6 | `110` | Eliminate bit 2: `110 XOR 101 = 011`; eliminate bit 1: `011 XOR 011 = 000` | unchanged | 2 |

Trace:
1. `3` introduces one independent direction.
2. `5` introduces another.
3. `6` is reducible to zero, so it is redundant: `3 XOR 5 = 6`.

The current span has dimension `2`, so it reaches only `2^2 = 4` masks, not all `2^3 = 8`.  
Minimum patches needed = `b - rank = 3 - 2 = 1`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * b)`, because each of the `n` masks is reduced across at most `b` bit positions. With `b <= 60`, this is effectively linear in input size. At `10^6` elements it remains practical; at `10^9`, input scan alone dominates and the problem becomes I/O-bound rather than algorithmically difficult.

### Space Complexity
`O(b)` auxiliary space for the linear basis, since there is at most one pivot vector per bit position. This is already minimal for explicit rank tracking; reducing below it would require recomputation or lossy state, neither of which is useful here.

## 💡 Key Takeaways
- If a problem asks whether every XOR value is reachable from subsets, treat the input as vectors over `GF(2)`, not as numbers to combine combinatorially.
- When constraints make subset enumeration impossible and values are bounded by bit-width, a XOR linear basis is the strongest signal.
- Do not confuse number of masks with dimension: duplicates and XOR-dependent masks contribute nothing to coverage.
- Scan bits from high to low consistently; incorrect pivot handling breaks independence and silently undercounts or overcounts rank.
- In production systems, rank is the real measure of expressive capacity; counting artifacts without eliminating redundancy leads to bad rollout and storage decisions.

## 🚀 Variations & Further Practice
- **Construct one valid minimal patch set, not just the count.** The twist is choosing masks outside the current span while preserving a clean independent extension to full rank.
- **Support online updates and queries.** Roles are added over time, and you must answer current minimum patches after each insertion; the challenge is incremental basis maintenance.
- **Maximum subset XOR / k-th smallest XOR value.** Same linear-basis machinery, but the objective shifts from span size to ordered or extremal queries over the generated space.