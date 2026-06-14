# Minimum Toggles to Equalize Binary Counters

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Parity, Greedy

---

## 🗂 Problem Overview
Given `n` non-negative integers, determine the minimum number of operations needed to make every counter identical. One operation selects a bit position and toggles that bit in exactly two distinct counters. The challenge is that changes are constrained both by bit position and by pairing: you cannot flip a single counter independently. The solution must exploit bitwise independence and parity invariants rather than simulate operation sequences.

## 🌍 Engineering Impact
This pattern shows up anywhere state transitions are constrained by paired or balanced updates: replicated hardware registers, distributed quota ledgers, dual-entry accounting systems, ECC-style correction flows, and synchronization protocols where mutations must preserve parity or checksum invariants. At scale, brute-force state search is useless; the system is too large and the transition graph too dense. What matters is identifying conserved quantities early. That enables constant-space feasibility checks, predictable runtime, and designs that fail fast when a target state is unreachable instead of burning cycles on impossible reconciliation plans.

## 🔍 Problem Statement
You are given an array `counters` with `1 <= n <= 10^5` and `0 <= counters[i] <= 10^9`. In one operation, choose a bit position `b` and toggle that bit in exactly two different counters. Toggling flips `0 ↔ 1` at the same bit position for both selected counters.

Return the minimum number of such operations required to make all counters equal, or `-1` if no sequence of operations can do so. The final value does not need to match any original counter.

Examples:

- `counters = [1, 0, 1, 0]` → `1`
- `counters = [3, 3, 1]` → `-1`

The key constraint is that each operation affects exactly two counters at one bit position, so the parity of the count of `1`s at each bit never changes. That invariant drives both feasibility and optimality.

## 🪜 How to Solve This
1. Read the operation carefully → it acts on one bit position at a time, and always on two counters. That strongly suggests decomposing the problem by bit.

2. For a fixed bit, ignore all other bits → now you just have `n` binary values. The goal is to make that column uniform: all `0`s or all `1`s.

3. Ask what never changes → each operation flips two entries in the same column, so the number of `1`s changes by `-2`, `0`, or `+2`. Its parity is invariant.

4. Therefore, a bit column is solvable iff the number of `1`s in that column has the same parity as the target uniform state:
   - all `0`s requires `0` ones, even parity
   - all `1`s requires `n` ones, parity `n % 2`

5. Once feasible, minimize operations for that bit → each operation can fix at most one mismatched `1` and one mismatched `0`, reducing total mismatches by two. So the minimum operations for that bit is `min(ones, n - ones) / 2`.

6. Sum this independently across all bit positions. No cross-bit coupling exists, so local optima compose into the global optimum.

## 🧩 Algorithm Walkthrough
1. **Use a bitwise greedy decomposition.**  
   Treat each bit position independently because an operation never mixes positions. This is the core abstraction: **bitwise decomposition with parity invariants**. It converts one large state-space problem into ~31 tiny counting problems.

2. **Iterate over relevant bit positions.**  
   Since `counters[i] <= 10^9`, checking bits `0..30` is sufficient. For each bit `b`, count `ones`: how many counters have bit `b` set. The invariant maintained is that `ones % 2` cannot change under any allowed operation at that bit.

3. **Check feasibility for the bit.**  
   To end with all counters equal, this bit must become either all `0`s or all `1`s`.
   - Target all `0`s is possible only if `ones` is even.
   - Target all `1`s is possible only if `n - ones` is even, equivalently `ones % 2 == n % 2`.  
   If neither target is feasible, return `-1` immediately. This is correct because bits are independent; one impossible column makes the whole instance impossible.

4. **Compute the minimum operations for the bit.**  
   If targeting all `0`s, each operation removes two `1`s, so cost is `ones / 2`.  
   If targeting all `1`s, each operation removes two `0`s, so cost is `(n - ones) / 2`.  
   Take the smaller feasible cost. The invariant is that every operation reduces the number of mismatches to the chosen target by exactly two, so this bound is both achievable and optimal.

5. **Accumulate across bits.**  
   Sum the per-bit minima. Because operations on different bits do not interfere, this additive composition is exact, not heuristic.

## 📊 Worked Example
Take `counters = [1, 0, 1, 0]`.

Binary by bit position:

| Counter | Bit 0 |
|---|---:|
| 1 | 1 |
| 0 | 0 |
| 1 | 1 |
| 0 | 0 |

Trace:

1. `n = 4`
2. For `bit = 0`, count `ones = 2`, so `zeros = 2`
3. Feasibility:
   - all `0`s: possible because `ones` is even
   - all `1`s: possible because `zeros` is even
4. Cost:
   - to all `0`s: `ones / 2 = 1`
   - to all `1`s: `zeros / 2 = 1`
5. Minimum for this bit = `1`

No higher bits are set in any counter, so they already match and contribute `0`.

One valid operation: toggle bit `0` in one counter currently `1` and one counter currently `0`. The pair becomes `0` and `1`, yielding either `[0,0,0,0]` or `[1,1,1,1]` depending on which pair you choose. Total operations = `1`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * B)`, where `B` is the number of examined bit positions, here at most `31`. That is effectively linear in input size. For `10^6` elements this is still practical; for `10^9`, input scan cost alone is prohibitive regardless of algorithm, so asymptotic improvement beyond linear scan is irrelevant.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only counters for the current bit and the running answer. Space cannot be meaningfully reduced further; the only trade-off is minor constant-factor tuning, such as deriving the max bit dynamically instead of always scanning 31 positions.

## 💡 Key Takeaways
- If an operation flips exactly two items, parity is usually the first invariant to test before designing any constructive algorithm.
- When a bitwise operation never mixes positions, decompose by bit immediately instead of reasoning about whole integers.
- Do not assume the final common value must be present in the input; the optimal target is chosen independently per bit.
- Be careful with feasibility: for odd `n`, “all `1`s” has odd parity per bit, so a column with even `ones` cannot end there.
- In production reconciliation systems, conserved quantities often turn an intractable transition search into a cheap validation-plus-aggregation pass.

## 🚀 Variations & Further Practice
- Allow one operation to toggle the same bit in exactly `k` distinct counters; feasibility shifts from parity to modulo-`k` invariants, and the minimization becomes more subtle.
- Add per-bit or per-device operation costs; the problem becomes a weighted optimization rather than pure counting, often requiring matching or flow formulations.
- Require the final equal value to be one of the original counters; bitwise independence breaks, because the target must be globally consistent with an existing integer.