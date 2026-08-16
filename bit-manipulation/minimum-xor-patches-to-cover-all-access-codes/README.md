# Minimum XOR Patches to Cover All Access Codes

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Linear Basis, Greedy

---

## 🗂 Problem Overview
Given `codes` and an upper bound `m`, determine the minimum number of additional non-negative integers needed so that every value in `[0, m]` can be produced as the XOR of some subset of the final array. The challenge is that XOR reachability is governed by linear independence over bits, not by magnitude or duplicates. With `n` up to `2e5` and values up to `1e18`, brute-force subset enumeration is completely infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere capabilities are composed via bitwise state spaces rather than additive totals: feature-flag encodings, network capability negotiation, error-correcting code construction, cryptographic protocol masks, and low-level compiler/dataflow analysis. At scale, the wrong mental model leads teams to optimize for counts or sorted ranges when the real constraint is vector-space coverage. Without a basis-driven approach, systems either over-provision state, miss reachable configurations, or ship expensive validation paths. The linear-basis view enables compact representation, fast reachability checks, and principled patching when extending supported capability sets.

## 🔍 Problem Statement
You are given an array `codes` of `n` non-negative integers and an integer `m`. You may perform patch operations; each patch adds any non-negative integer to the array. After all patches, consider every XOR obtainable from any subset of the final array, including the empty subset, so `0` is always reachable.

Return the minimum number of patches required so that **every** integer in the inclusive range `[0, m]` is representable.

Constraints:
- `1 <= n <= 200000`
- `0 <= codes[i] <= 10^18`
- `0 <= m <= 10^18`

Examples:

- `codes = [1, 2], m = 7` → `1`  
  Existing subset XORs are `{0,1,2,3}`. Adding `4` yields basis `{1,2,4}`, which spans all values in `[0,7]`.

- `codes = [5, 10], m = 6` → `2`  
  Values like `1` are unreachable. Adding `1` and `2` creates enough low-bit coverage to generate every value from `0` to `6`.

The key constraint is that exhaustive subset reasoning is impossible; the solution must exploit XOR linear structure.

## 🪜 How to Solve This
1. Read the requirement carefully → we do **not** need to generate all large numbers, only every value in `[0, m]`.
2. For XOR problems, “what values are reachable?” usually means **linear algebra over GF(2)** → think **linear basis**, not subset DP.
3. Ask what it means to cover `[0, m]` completely. If we can generate every value from `0` to `2^k - 1`, then we cover exactly the first `k` bits.
4. That suggests a greedy coverage invariant: maintain the largest prefix `[0, cover]` already representable.
5. If the current basis can produce `cover + 1`, coverage extends. If not, the cheapest patch is exactly `cover + 1`, because it introduces the missing lowest independent bit pattern and doubles the covered range.
6. Existing codes should first be reduced into a basis so we know which low-bit pivots already exist.
7. Then sweep bits from low to high, greedily consuming available independent vectors or patching missing ones until `cover >= m`.

Once you see “XOR subset reachability” plus “minimum additions to cover a contiguous prefix,” the basis + greedy combination is the natural fit.

## 🧩 Algorithm Walkthrough
1. **Build a XOR linear basis from `codes`** using Gaussian-elimination-style insertion over bits.  
   Pattern: **Linear Basis over GF(2)**.  
   Why: duplicates and dependent values do not expand reachability, so only independent vectors matter.

2. **Normalize the basis by pivot bit** so for each bit position you know whether an independent vector exists whose highest set bit is that bit.  
   Invariant: each stored basis vector contributes one unique pivot bit.

3. **Track contiguous XOR coverage with `cover`**, initialized to `0`, meaning all values in `[0, cover]` are representable.  
   Since the empty subset gives `0`, this invariant is valid from the start.

4. **Iterate candidate bits from low to high while `cover < m`**. Let `need = cover + 1`.  
   The highest set bit of `need` determines the next missing scale of coverage.

5. **If the basis contains a pivot at that bit**, we can combine it with already covered lower values to extend coverage from `[0, 2^k - 1]` to `[0, 2^{k+1} - 1]` in effect.  
   Why correct: an independent pivot at bit `k` lets us produce all numbers with that bit off and on, assuming all lower-bit combinations are already reachable.

6. **If no such pivot exists, patch with `need`** and increment the answer.  
   Pattern: **Greedy prefix-extension**, analogous to additive patching, but in XOR space.  
   Why correct: any solution must introduce independence at this missing lowest uncovered bit; choosing `need` is the minimal patch that extends coverage maximally without wasting higher bits.

7. **Update `cover` to `2 * cover + 1`** whenever a new independent low-bit pivot is consumed or patched in.  
   Invariant: after acquiring pivots for bits `0..k`, every value in `[0, 2^{k+1}-1]` is reachable.

8. Stop once `cover >= m`; the patch count is minimal because every step either uses an existing necessary pivot or adds the uniquely optimal missing one.

## 📊 Worked Example
Example: `codes = [5, 10]`, `m = 6`

First reduce to a basis:
- `5 = 101`
- `10 = 1010`
They are independent, but neither gives pivot bit `0` or `1`.

| Step | `cover` | `need = cover + 1` | Missing pivot bit | Action | Patches |
|---|---:|---:|---:|---|---:|
| Start | 0 | 1 | 0 | No bit-0 pivot in basis | 0 |
| 1 | 0 | 1 | 0 | Patch `1` | 1 |
| After update | 1 | 2 | 1 | No bit-1 pivot in basis | 1 |
| 2 | 1 | 2 | 1 | Patch `2` | 2 |
| After update | 3 | 4 | 2 | Existing basis has bit-2 pivot (`5`) | 2 |
| Final | 7 | — | — | `cover >= 6`, stop | 2 |

After adding `1` and `2`, low bits are fully spanned, and the existing `5` extends coverage past `6`.

## ⏱ Complexity Analysis
### Time Complexity
Building the linear basis takes `O(n * B)`, where `B` is the number of bits, at most about `60` for `1e18`. The greedy coverage phase is also `O(B)`. So total time is `O(n * B)`, effectively linear in input size. This is practical at `10^6` elements and still fundamentally impossible to replace with subset enumeration at `2^n` scale.

### Space Complexity
Space is `O(B)` for the basis, plus input storage if retained externally. The algorithm itself only needs a fixed-size array of pivot vectors across bit positions. This is already minimal; reducing further would trade away clarity without changing asymptotic behavior.

## 💡 Key Takeaways
- If a problem asks which values are reachable via subset XOR, the right abstraction is usually a **linear basis over GF(2)**, not sorting, prefix sums, or subset DP.
- If the target is to cover a **contiguous range from zero**, look for a greedy invariant on the largest covered prefix, even in non-additive domains.
- Do not count duplicate or merely large values as useful; only **independent pivot bits** expand the reachable XOR space.
- The off-by-one trap is in the coverage invariant: `cover` means all values in `[0, cover]` are reachable, so the next missing target is always `cover + 1`.
- In production systems, compact basis representations often outperform raw state accumulation because they preserve exactly the information that changes capability coverage.

## 🚀 Variations & Further Practice
- Require covering **all values in `[L, R]`** instead of `[0, m]`; the twist is that contiguous coverage no longer aligns cleanly with low-bit prefix structure.
- Minimize the **sum or cost of patched values** rather than the number of patches; the greedy “patch `cover + 1`” argument must be reworked under weighted objectives.
- Support **online updates and queries** where codes are added over time and you must repeatedly answer whether `[0, m]` is fully covered; this pushes toward dynamic basis maintenance and incremental coverage tracking.