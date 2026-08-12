# Minimum Rewrites to Match a Layered Bit Template

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Parity, Greedy

---

## 🗂 Problem Overview
Given `n`, a `target`, and per-bit rewrite costs, build an `n`-bit nonnegative integer `x` starting from all zeros. Setting bit `i` to `1` costs `costs[i]`. For every prefix length `k`, the parity of the lowest `k` bits of `x` must equal the `k`-th lowest bit of `target`. Return the minimum total cost, or `-1` if impossible. The difficulty is that prefix parity constraints are globally coupled across bits, so naive bitmask search is infeasible for `n` up to `200000`.

## 🌍 Engineering Impact
This pattern shows up anywhere local state changes induce cumulative invariants: streaming checksums, parity-protected storage layouts, compiler bitset encodings, and distributed feature-flag rollouts where each toggle affects aggregate policy state. At scale, brute-force exploration of all bit assignments collapses immediately; you need to exploit the algebraic structure of cumulative constraints. The practical value is architectural: once you recognize that prefix conditions can often be inverted into per-position decisions, a seemingly exponential search becomes a linear pass. That shift is what keeps validation, optimization, and reconciliation pipelines predictable under large cardinalities.

## 🔍 Problem Statement
You are given:

- `1 <= n <= 200000`
- `0 <= target < 2^n`
- `costs.length == n`
- `1 <= costs[i] <= 10^9`

Construct an `n`-bit integer `x`, initially all zeros, by optionally setting bits to `1`. Setting bit `i` costs `costs[i]`. For every `k` from `1` to `n`, let `low(k)` be the integer formed by the lowest `k` bits of `x`. The constraint is:

- `popcount(low(k))) % 2 == bit(target, k-1)`

That is, the parity of each least-significant-bit prefix of `x` must match the corresponding bit of `target`.

Return the minimum total rewrite cost required to build such an `x`. If no assignment satisfies all prefix constraints, return `-1`.

Examples:

- `n = 4, target = 11, costs = [5, 2, 7, 1]`
- `n = 5, target = 6, costs = [4, 9, 1, 3, 8]`

The key constraint is `n = 200000`: any `O(2^n)` or even `O(n^2)` approach is dead on arrival.

## 🪜 How to Solve This
1. Read the constraint carefully → every `k` gives a parity condition on the first `k` bits of `x`.
2. Prefix parity usually suggests a cumulative state. Let `p[k]` be the required parity for the lowest `k` bits, taken directly from bit `k-1` of `target`.
3. Now ask the right inversion question: if I know the parity of the first `k-1` bits and the parity of the first `k` bits, what must bit `k-1` of `x` be?
4. The answer is forced:  
   `x[k-1] = p[k] XOR p[k-1]`, with `p[0] = 0`.
5. That means there is no combinatorial search at all. The entire bit pattern of `x` is uniquely determined by adjacent parity differences.
6. Once each bit is forced, cost minimization becomes trivial: sum `costs[i]` exactly where the derived bit is `1`.
7. Impossibility only exists if the problem allowed conflicting external constraints. Here it does not: every parity sequence defines exactly one valid `x`.

The key mental move is converting cumulative constraints into local transitions.

## 🧩 Algorithm Walkthrough
1. **Extract the required prefix parity sequence.**  
   For each `k` in `1..n`, define `p[k] = (target >> (k - 1)) & 1`. Also set `p[0] = 0`.  
   **Why correct:** the problem states that the parity of the lowest `k` bits of `x` must equal the `k`-th lowest bit of `target`.  
   **Invariant:** `p[k]` is the exact required parity after processing bits `0..k-1`.

2. **Recover each bit from parity transitions.**  
   The parity after including bit `i` is the previous parity XOR that bit:  
   `p[i + 1] = p[i] XOR x[i]`, so `x[i] = p[i] XOR p[i + 1]`.  
   **Why correct:** parity flips iff the newly added bit is `1`.  
   **Invariant:** after deriving `x[0..i]`, the computed prefix parities match `p[1..i+1]`.

3. **Accumulate cost greedily.**  
   If `x[i] = 1`, add `costs[i]`; otherwise add nothing.  
   **Why correct:** there is exactly one feasible assignment, so “greedy” here means paying only for forced `1` bits. There is no alternative choice to compare.  
   **Invariant:** running total equals the minimum cost for the uniquely determined prefix.

4. **Return the total.**  
   This is a **prefix-parity inversion** pattern: cumulative XOR/parity constraints are transformed into independent per-position decisions via adjacent differences. It is the right abstraction because the original coupling is only apparent; algebraically, the dependency graph is triangular and fully invertible in one pass.

## 📊 Worked Example
Take `n = 5`, `target = 6`, `costs = [4, 9, 1, 3, 8]`.

`target = 00110₂`, so from least significant bit upward the required prefix parities are:

| k | `p[k]` |
|---|--------|
| 0 | 0 |
| 1 | 0 |
| 2 | 1 |
| 3 | 1 |
| 4 | 0 |
| 5 | 0 |

Now derive each bit of `x` using `x[i] = p[i] XOR p[i+1]`:

| i | `p[i]` | `p[i+1]` | `x[i]` | cost added |
|---|--------|----------|--------|------------|
| 0 | 0 | 0 | 0 | 0 |
| 1 | 0 | 1 | 1 | 9 |
| 2 | 1 | 1 | 0 | 0 |
| 3 | 1 | 0 | 1 | 3 |
| 4 | 0 | 0 | 0 | 0 |

So `x = 01010₂` and total cost is `9 + 3 = 12`.

The important observation is not the numeric result from the prompt examples, but the derivation: once prefix parities are fixed, every bit of `x` is forced.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. You scan the `n` bit positions once, extract each required parity bit from `target`, derive the corresponding bit of `x`, and conditionally add its cost. At `10^6` elements this is routine; at `10^9`, linear work is still too large, but asymptotically this is already optimal because every position can matter.

### Space Complexity
`O(1)` auxiliary space if you stream the computation directly from `target` without materializing the parity array. The only owned state is the running previous parity and total cost. You could store the full derived bit vector for debugging, but that increases space to `O(n)` with no algorithmic benefit.

## 💡 Key Takeaways
- If a problem constrains every prefix by parity or XOR, look for a cumulative-state inversion instead of exploring assignments.
- When each new element only toggles a binary aggregate, adjacent prefix states usually determine the element uniquely.
- The indexing trap is `k` being 1-based while bits are 0-based; define `p[0] = 0` explicitly and use `x[i] = p[i] XOR p[i+1]`.
- Do not over-engineer “minimum cost” here: there is no optimization search once the parity sequence is fixed; cost is just the sum over forced `1` bits.
- In production systems, cumulative constraints often look globally coupled but collapse into local transitions once you model the state boundary correctly.

## 🚀 Variations & Further Practice
- Allow both `0 -> 1` and `1 -> 0` rewrites from an arbitrary initial bitmask, with asymmetric costs. The twist is separating feasibility from edit-distance minimization under the same parity inversion.
- Replace parity with prefix XOR over multi-bit symbols instead of single bits. The harder part is lifting the same inversion idea from binary state to a larger finite algebra.
- Add online updates to `costs` or `target` bits and answer minimum-cost queries after each change. The challenge becomes maintaining adjacent-difference effects incrementally rather than recomputing the full pass.