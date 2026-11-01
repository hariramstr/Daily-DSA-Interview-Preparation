# Minimum Delay to Merge Timed Subtitle Tracks

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, strings, sequence-interleaving

---

## 🗂 Problem Overview
Given two ordered subtitle tracks `A` and `B`, build a merged string by repeatedly taking the next unused character from either track while preserving per-track order. Each adjacent pair in the merge adds cost for a track switch and also for equal neighboring characters. The goal is to return the minimum total cost over all valid interleavings. The challenge is combinatorial: there are exponentially many merges, so brute-force enumeration is infeasible at lengths up to 2000.

## 🌍 Engineering Impact
This pattern shows up anywhere two ordered event streams must be merged under transition penalties: subtitle/audio alignment, log stream stitching, CDC/event replay, packet scheduling, and compiler or search pipelines that merge partially ordered outputs. At small scale, greedy choices look plausible; at production scale, they fail because local minimization ignores future transition cost. The value of the dynamic programming formulation is that it converts an exponential scheduling space into a predictable `O(|A|*|B|)` plan, which is the difference between a system that degrades catastrophically under long sequences and one that remains operational with bounded latency and memory.

## 🔍 Problem Statement
You are given two non-empty lowercase strings `A` and `B`, with `1 <= |A|, |B| <= 2000`. Construct a final string by interleaving them: at each step, take the next unused character from either `A` or `B`, and never reorder characters within the same source string.

For every adjacent pair in the merged result:

- add `1` if the two characters came from different source tracks,
- add `1` if the two characters are equal.

Return the minimum possible total cost across all valid interleavings.

Examples:

- `A = "ab", B = "ac"` → `2`
- `A = "aaa", B = "aa"` → `4`

The key constraint is the input size. Since both lengths can reach 2000, the solution must avoid enumerating interleavings and instead use an `O(|A| * |B|)` dynamic programming strategy.

## 🪜 How to Solve This
1. Read the scoring rule carefully → every cost is created by an **adjacent pair** in the final merge, not by a character in isolation.

2. That means the future only cares about two things:
   - how many characters have already been consumed from `A` and `B`,
   - which track supplied the **last** chosen character.

3. Once you notice that, brute force collapses into stateful optimization: if we know we have used `i` chars from `A`, `j` from `B`, and the last pick came from one track, then the next move cost is fully determined.

4. So define DP states for prefixes:
   - best cost after consuming `A[0..i)` and `B[0..j)`,
   - with the last character taken from `A` or from `B`.

5. Transition by taking the next character from either track. The incremental cost is:
   - `+1` if the source track changes,
   - `+1` if the previous character equals the new one.

6. This is classic **dynamic programming on interleavings**: optimal substructure exists because once a prefix is fixed, only the last source/character matters for the next edge cost.

## 🧩 Algorithm Walkthrough
1. **Define the DP pattern explicitly: 2D prefix DP with last-source state.**  
   Let:
   - `dpA[i][j]` = minimum cost after consuming `i` chars from `A` and `j` from `B`, where the last chosen character is `A[i-1]`.
   - `dpB[i][j]` = same, but the last chosen character is `B[j-1]`.  
   This is the right abstraction because the cost of appending one more character depends only on the previous character and whether the source changes.

2. **Initialize base cases along the borders.**  
   If we take only from `A`, then there are no source switches, only repeated-adjacent penalties inside `A`. So:
   - `dpA[i][0] = dpA[i-1][0] + [A[i-2] == A[i-1]]` for `i >= 2`
   and similarly for `dpB[0][j]`.  
   The first picked character has no previous neighbor, so its cost is `0`.

3. **Transition into `dpA[i][j]`.**  
   The last character is `A[i-1]`. It could come from:
   - `dpA[i-1][j]` by staying on track `A`, adding only noise cost if `A[i-2] == A[i-1]`,
   - `dpB[i-1][j]` by switching from `B`, adding `1` for the switch and `1` more if `B[j-1] == A[i-1]`.  
   Take the minimum of these valid predecessors.

4. **Transition into `dpB[i][j]` symmetrically.**  
   From `dpB[i][j-1]`, add noise if `B[j-2] == B[j-1]`.  
   From `dpA[i][j-1]`, add switch cost plus noise if `A[i-1] == B[j-1]`.

5. **Maintain the invariant.**  
   After filling cell `(i, j)`, `dpA[i][j]` and `dpB[i][j]` are the minimum costs over all interleavings of the two consumed prefixes ending on the specified source. Because every valid merge must end in either `A` or `B`, the final answer is `min(dpA[n][m], dpB[n][m])`.

6. **Why this is correct.**  
   Every interleaving of prefixes has a unique last move. The recurrence enumerates exactly those last moves and charges the exact local edge cost introduced by that move. Since the remainder of the prefix cost is already optimal by definition of the DP state, the recurrence preserves optimality.

## 📊 Worked Example
Take `A = "ab"`, `B = "ac"`.

Let `dpA[i][j]` / `dpB[i][j]` be as defined above.

| State | Value | Reason |
|---|---:|---|
| `dpA[1][0]` | 0 | first char `a`, no previous pair |
| `dpA[2][0]` | 0 | `a -> b`, no repeat |
| `dpB[0][1]` | 0 | first char `a` |
| `dpB[0][2]` | 0 | `a -> c`, no repeat |
| `dpA[1][1]` | 2 | from `B`: switch `+1`, equal `a==a` `+1` |
| `dpB[1][1]` | 2 | symmetric |
| `dpA[2][1]` | 1 | from `B[1][1]`: append `b`, no switch if from `A` path is worse |
| `dpB[1][2]` | 1 | symmetric append `c` |
| `dpA[2][2]` | 2 | best ending at `b` |
| `dpB[2][2]` | 2 | best ending at `c` |

Final answer: `min(2, 2) = 2`, achieved by merge `"abac"`.

## ⏱ Complexity Analysis

### Time Complexity
`O(|A| * |B|)` because each DP cell `(i, j)` computes two states, and each state is updated from a constant number of predecessors. At `10^6` cells this is routine; at `10^9` cells it becomes operationally expensive, which is why the 2000x2000 bound is important.

### Space Complexity
`O(|A| * |B|)` if both DP tables are stored in full. The space is owned by `dpA` and `dpB`. It can be reduced to `O(|B|)` with rolling rows because each state depends only on the current row and previous row, at the cost of more careful update ordering.

## 💡 Key Takeaways
- If a merge/interleaving problem scores only adjacent transitions, the state usually needs prefix positions plus the identity of the last contributor.
- When brute force explores exponentially many interleavings but each decision depends on a tiny suffix of history, that is a strong DP signal.
- The first chosen character contributes no cost; many incorrect solutions accidentally charge a switch or equality penalty at sequence start.
- Border initialization matters: `dpA[i][0]` and `dpB[0][j]` still accumulate repeated-character penalties within a single track.
- The production lesson is broader than this problem: once transition cost is path-dependent but locally composable, model it as stateful sequence optimization instead of relying on greedy scheduling.

## 🚀 Variations & Further Practice
- Add a third subtitle track `C`: same interleaving idea, but the state space grows and forces a decision on whether `O(n*m*k)` is acceptable or whether additional structure is needed.
- Replace unit penalties with arbitrary switch costs between tracks and arbitrary character-pair penalties: same DP skeleton, but now the transition function becomes a weighted automaton.
- Ask for the number of minimum-cost merges, not just the cost: this adds tie-counting on top of the DP and introduces modular arithmetic plus duplicate-optimum handling.