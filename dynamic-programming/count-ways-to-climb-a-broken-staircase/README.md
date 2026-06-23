# Count Ways to Climb a Broken Staircase

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, counting

---

## 🗂 Problem Overview
Given a staircase of `n` steps, count how many distinct ways reach exactly step `n` from step `0` using jumps of size `1` or `2`, while never landing on any step listed in `broken`. Return the result modulo `1,000,000,007`. The problem is non-trivial because `n` can be as large as `100000`, so brute-force path enumeration is infeasible and overlapping subproblems must be reused efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere valid state transitions must be counted under exclusion rules: workflow engines skipping invalid states, streaming pipelines routing around quarantined partitions, compiler passes propagating counts through blocked control-flow nodes, and reliability models computing reachable system states with failed components. At small scale, naive recursion or exhaustive search works; at production scale, it collapses under combinatorial growth. Dynamic programming turns exponential exploration into linear state propagation, which is the difference between an algorithm that times out under peak load and one that can be embedded safely in latency-sensitive services.

## 🔍 Problem Statement
You are given:
- An integer `n` representing the top step, where `1 <= n <= 100000`
- An array `broken` of distinct step numbers, where each `broken[i]` satisfies `1 <= broken[i] <= n`

A person starts at step `0` and wants to reach exactly step `n`. On each move, they may climb either `1` step or `2` steps. They may not land on a broken step. Return the number of distinct valid jump sequences modulo `1,000,000,007`.

If a step is broken, the number of ways to reach it is `0`.

Examples:
- `n = 5, broken = [2]` → `2`
- `n = 6, broken = [3, 5]` → `1`

The key constraint is `n = 100000`: this rules out recursive enumeration and pushes toward an `O(n)` dynamic programming solution with constant or linear auxiliary space.

## 🪜 How to Solve This
1. Start from the recurrence for the normal staircase problem: ways to reach step `i` come from step `i-1` or step `i-2`.
2. Add the broken-step rule → if step `i` is broken, then `ways[i] = 0` regardless of previous values.
3. That immediately suggests dynamic programming: each state depends only on the previous two states.
4. Define `ways[i]` as the number of valid ways to land on step `i`.
5. Base case matters: `ways[0] = 1`, because there is exactly one way to be at the ground before taking any jumps.
6. For each step from `1` to `n`:
   - if broken → zero it out
   - otherwise → sum the reachable counts from `i-1` and `i-2`
7. Apply modulo at every addition to avoid overflow and satisfy the output contract.
8. Notice the dependency window is only two steps wide → full `O(n)` DP array is fine, but space can be reduced to `O(1)` if needed.

This is the standard “count paths with blocked states” DP pattern.

## 🧩 Algorithm Walkthrough
1. **Model the problem as Dynamic Programming.**  
   Use the pattern explicitly: **1D Dynamic Programming over a linear state space**. Each step is a state, and transitions come only from the previous one or two states. This abstraction fits because the future does not depend on the full path history, only on how many ways reached recent positions.

2. **Create fast broken-step lookup.**  
   Store `broken` in a boolean array or hash set. The invariant is: membership checks for “can I land here?” must be `O(1)`, otherwise the DP loop degrades unnecessarily.

3. **Initialize the base state.**  
   Set `ways[0] = 1`. This means there is one valid way to stand at the starting position before any move. Without this seed, all later counts remain zero.

4. **Iterate from step `1` to step `n`.**  
   For each step `i`:
   - If `i` is broken, set `ways[i] = 0`
   - Otherwise, compute:
     `ways[i] = ways[i - 1] + ways[i - 2]`  
     with missing indices treated as zero

   The invariant after processing step `i` is: `ways[k]` is correct for every `0 <= k <= i`.

5. **Apply modulo during accumulation.**  
   Use `MOD = 1_000_000_007` on every update. This preserves correctness under modular arithmetic and prevents integer growth.

6. **Return `ways[n]`.**  
   By construction, this is the total number of valid sequences that end exactly at the target step while avoiding broken landings.

## 📊 Worked Example
Example: `n = 6`, `broken = [3, 5]`

Let `ways[i]` be the number of valid ways to reach step `i`.

| Step `i` | Broken? | Formula / Reason                  | `ways[i]` |
|---------:|:-------:|-----------------------------------|----------:|
| 0        | No      | Base case                         | 1         |
| 1        | No      | `ways[0]`                         | 1         |
| 2        | No      | `ways[1] + ways[0] = 1 + 1`       | 2         |
| 3        | Yes     | Broken step                       | 0         |
| 4        | No      | `ways[3] + ways[2] = 0 + 2`       | 2         |
| 5        | Yes     | Broken step                       | 0         |
| 6        | No      | `ways[5] + ways[4] = 0 + 2`       | 2         |

Result: `2`

Valid sequences are:
1. `2 -> 2 -> 2`
2. `1 -> 1 -> 2 -> 2`

Both avoid landing on steps `3` and `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + b)`, where `b = broken.length`. Building the broken-step lookup costs `O(b)`, and the DP scan from `1` to `n` costs `O(n)`. At `10^6` states this is routine; at `10^9`, even linear scans become operationally expensive and require a different formulation or stronger constraints.

### Space Complexity
`O(n)` if using a DP array plus a broken-marker array. The dominant space owner is the per-step state. It can be reduced to `O(1)` DP state if broken membership remains `O(1)`, trading away full traceability of intermediate counts.

## 💡 Key Takeaways
- If a counting problem asks for the number of ways to reach position `i` from a small fixed set of previous positions, it is usually a 1D dynamic programming recurrence.
- “Blocked,” “forbidden,” or “broken” states are a strong signal to keep the same recurrence but force those states to contribute zero.
- The most common off-by-one bug is forgetting that the start is step `0`, not step `1`, so `ways[0]` must be initialized to `1`.
- Another easy mistake is mishandling `i - 2` for small `i`; treat out-of-range predecessors as zero rather than reading invalid memory or special-casing incorrectly.
- At system scale, this pattern is really about propagating valid state counts through constrained transitions efficiently instead of exploring paths explicitly.

## 🚀 Variations & Further Practice
- Allow jumps of size `1`, `2`, or `k`: same DP pattern, but the recurrence expands and constant-space optimization becomes less trivial.
- Assign a cost to each step and ask for the minimum cost path while avoiding broken steps: shifts from counting DP to optimization DP.
- Generalize the staircase into a DAG of states with blocked nodes and count paths from source to target: same idea, but now topological order replaces simple left-to-right iteration.