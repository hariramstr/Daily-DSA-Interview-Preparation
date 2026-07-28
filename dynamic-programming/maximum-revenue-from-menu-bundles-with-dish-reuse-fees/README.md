# Maximum Revenue from Menu Bundles with Dish Reuse Fees

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, state-transition

---

## 🗂 Problem Overview
Choose exactly one dish type each evening: `standard[i]` or `premium[i]`. If the same type is chosen on consecutive evenings, revenue on evening `i` is reduced by `fee[i]`. The goal is to maximize total revenue across `N` evenings. The challenge is scale: `N` can reach 200,000, so enumerating all `2^N` menus is infeasible. The key observation is that only the previous evening’s dish type affects the current decision.

## 🌍 Engineering Impact
This pattern shows up anywhere local transition cost matters more than full history. Examples include search ranking with diversity penalties between adjacent results, streaming pipelines that charge warm-cache or cold-start costs when operator types repeat, ad serving with fatigue penalties on repeated creative classes, and job schedulers where reusing the same execution lane incurs contention cost. At scale, brute-force exploration explodes combinatorially and blocks real-time decisions. A compact state-transition model turns an exponential planning problem into a linear scan, enabling online optimization, predictable latency, and memory-bounded implementations that fit high-throughput systems.

## 🔍 Problem Statement
You are given three arrays of length `N`:

- `standard[i]`: revenue if the standard dish is served on evening `i`
- `premium[i]`: revenue if the premium dish is served on evening `i`
- `fee[i]`: penalty applied on evening `i` only if the dish type matches evening `i - 1`

On evening `0`, no fee is applied. For each later evening, choosing the same type as the previous evening reduces that evening’s revenue by `fee[i]`. Return the maximum total revenue over all `N` evenings.

Constraints:

- `1 <= N <= 200000`
- `0 <= standard[i], premium[i], fee[i] <= 10^9`
- Result fits in signed 64-bit integer

Examples:

- `standard = [5, 6, 4, 7]`
- `premium = [8, 3, 9, 2]`
- `fee = [0, 4, 5, 3]`
- Output: `30`

- `standard = [10, 10, 1, 10]`
- `premium = [1, 1, 20, 1]`
- `fee = [0, 2, 8, 2]`
- Output: `48`

The driving constraint is `N = 200000`: the solution must be near-linear.

## 🪜 How to Solve This
1. Read the transition rule → today’s payoff depends on today’s dish and whether yesterday used the same type.  
2. That immediately suggests we do **not** need full history. We only need the best total revenue so far under two states: “yesterday ended with standard” and “yesterday ended with premium.”  
3. Define two running values:
   - best revenue up to evening `i` if evening `i` uses standard
   - best revenue up to evening `i` if evening `i` uses premium  
4. For standard today, there are only two ways to arrive:
   - yesterday was standard → pay `fee[i]`
   - yesterday was premium → no fee  
   Take the better of those two totals, then add today’s standard revenue.
5. Do the symmetric transition for premium.
6. Initialize from evening `0`, where no fee applies.
7. Scan once through the arrays, updating the two states.

This is classic dynamic programming with a tiny state space. The reason it works is that the future only cares about the current ending type, not the exact sequence that produced it.

## 🧩 Algorithm Walkthrough
1. **Model the problem as a 2-state DP.**  
   Pattern: **Dynamic Programming / state transition**.  
   Let:
   - `dpS` = max revenue up to current evening if current choice is standard
   - `dpP` = max revenue up to current evening if current choice is premium  
   This is the right abstraction because the penalty depends only on whether the previous type matches the current one.

2. **Initialize the base case at evening 0.**  
   Set:
   - `dpS = standard[0]`
   - `dpP = premium[0]`  
   This is correct because no reuse fee applies on the first evening.  
   Invariant: after processing evening `i`, `dpS` and `dpP` are optimal totals ending in each type.

3. **Transition to evening `i > 0` for standard.**  
   Compute:
   - continue standard: `dpS + standard[i] - fee[i]`
   - switch from premium: `dpP + standard[i]`  
   Take the maximum.  
   This is exhaustive: the previous evening must have been either standard or premium.

4. **Transition symmetrically for premium.**  
   Compute:
   - continue premium: `dpP + premium[i] - fee[i]`
   - switch from standard: `dpS + premium[i]`  
   Again, take the maximum.

5. **Update both states simultaneously.**  
   Store next values in temporaries before overwriting. Otherwise one transition may incorrectly use already-updated state from the same evening.

6. **Return the better ending state.**  
   Final answer is `max(dpS, dpP)`.  
   The invariant guarantees these are the best totals among all valid menus ending in each type, so their maximum is globally optimal.

## 📊 Worked Example
Use:

- `standard = [10, 10, 1, 10]`
- `premium = [1, 1, 20, 1]`
- `fee = [0, 2, 8, 2]`

| Evening `i` | `nextS` calculation | `nextP` calculation | `dpS` | `dpP` |
|---|---|---|---:|---:|
| 0 | base = 10 | base = 1 | 10 | 1 |
| 1 | `max(10+10-2, 1+10)=18` | `max(1+1-2, 10+1)=11` | 18 | 11 |
| 2 | `max(18+1-8, 11+1)=12` | `max(11+20-8, 18+20)=38` | 12 | 38 |
| 3 | `max(12+10-2, 38+10)=48` | `max(38+1-2, 12+1)=37` | 48 | 37 |

Answer: `max(48, 37) = 48`.

Optimal sequence: standard, standard, premium, standard.  
Only evening 2 reuses standard, so that evening pays fee `2`. The premium choice on evening 3 is strong enough to justify switching, and switching back on evening 4 avoids another penalty.

## ⏱ Complexity Analysis

### Time Complexity
`O(N)`. Each evening performs a constant amount of work: two max computations and a few additions/subtractions. At `10^6` elements this is routine in a single pass. At `10^9`, even linear time becomes operationally expensive, so batching or distributed execution would matter, but asymptotically this is optimal.

### Space Complexity
`O(1)`. The algorithm stores only two DP states plus temporary next-state values. No auxiliary arrays are required. You could materialize a full DP table for debugging or path reconstruction, but that raises space to `O(N)` without improving the optimal revenue computation itself.

## 💡 Key Takeaways
- If the reward at position `i` depends only on the choice at `i` and whether it matches `i-1`, expect a small-state DP rather than backtracking or greedy selection.
- When the future only cares about the current “ending mode,” compress history into one state per mode and scan left to right.
- Do not apply `fee[0]`; the first evening has no predecessor, so any penalty there is a bug.
- Update `nextS` and `nextP` from the previous pair of states, not from partially overwritten values in the same iteration.
- The transferable design insight: many exponential planning problems collapse once you identify the minimal boundary state that fully summarizes the past.

## 🚀 Variations & Further Practice
- Add a third dish type or `K` dish types. The state space becomes `K`, and each transition must consider staying vs switching across multiple prior types.
- Make the reuse fee depend on run length, not just whether the previous evening matched. Now the state must track consecutive streak length or a compressed equivalent.
- Require reconstruction of the actual optimal menu, not just the revenue. This adds predecessor tracking and changes the memory trade-off from `O(1)` to `O(N)`.