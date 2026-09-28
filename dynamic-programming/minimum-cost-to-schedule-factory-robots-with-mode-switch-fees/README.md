# Minimum Cost to Schedule Factory Robots with Mode Switch Fees

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, state-transition

---

## 🗂 Problem Overview
Given two per-hour operating cost arrays, choose exactly one robot mode per hour—A or B—so total cost is minimized. The total includes the chosen hourly operating cost plus a fixed `switchFee` whenever the mode changes between adjacent hours. Return the minimum achievable total over all `n` hours. The non-trivial part is path dependence: the best choice at hour `i` depends on which mode was active at hour `i - 1`, so greedy local picks fail.

## 🌍 Engineering Impact
This pattern shows up anywhere a system chooses between operating modes with transition penalties: cloud instance families with warm-up costs, streaming pipelines switching execution strategies, storage tiers with migration overhead, radio/network power states, or manufacturing lines changing machine configuration. At scale, ignoring transition cost produces thrashing: locally cheap decisions become globally expensive because state changes dominate runtime or spend. Dynamic programming gives the right abstraction when the current decision depends only on a small prior state. That enables linear-time optimization, predictable latency, and straightforward extension to richer state machines without exploding into brute-force search.

## 🔍 Problem Statement
You are given `n` hours, `aCost[i]`, `bCost[i]`, and a non-negative `switchFee`. For each hour `i`, exactly one mode must be chosen: A or B. If the chosen mode at hour `i` differs from hour `i - 1`, add `switchFee`; otherwise add nothing extra. The goal is to minimize total cost across all hours.

Constraints:
- `1 <= n <= 100000`
- `aCost.length == bCost.length == n`
- `0 <= aCost[i], bCost[i], switchFee <= 1e9`
- Answer fits in signed 64-bit integer

Examples:
- `aCost = [3,8,2,5], bCost = [4,1,6,1], switchFee = 3` → `12`
- `aCost = [10,2,10,2], bCost = [1,9,1,9], switchFee = 2` → `12`

The key constraint is `n = 1e5`: exponential enumeration of all `2^n` schedules is impossible, so the solution must exploit the fact that only the previous mode matters.

## 🪜 How to Solve This
1. Read the problem → each hour has two choices, so brute force is a binary decision tree of size `2^n`. That is immediately dead at `n = 100000`.

2. Notice what actually influences the future → not the full schedule, only the mode used in the previous hour, because switch cost depends only on adjacent hours.

3. That observation suggests a compact state:
   - minimum cost up to hour `i` if hour `i` ends in A
   - minimum cost up to hour `i` if hour `i` ends in B

4. For hour `i`, ending in A can come from:
   - previous A with no switch
   - previous B plus `switchFee`

   Same logic for ending in B.

5. So each state is the minimum of two transitions. That is classic dynamic programming over a tiny state machine.

6. Initialize from hour `0`: no prior hour means no switch fee.

7. Iterate once, update the two states, and return the smaller final value.

The reason this approach is obvious in hindsight is that the problem has temporal dependency, but only one-step memory. That is exactly where DP with rolling state is the right tool.

## 🧩 Algorithm Walkthrough
1. **Model it as Dynamic Programming with two end states.**  
   Let `dpA[i]` be the minimum total cost for hours `0..i` if hour `i` uses mode A. Let `dpB[i]` be the analogous value for mode B. This is the right abstraction because the only relevant historical fact is the last mode.

2. **Initialize the base case at hour 0.**  
   `dpA[0] = aCost[0]` and `dpB[0] = bCost[0]`.  
   This is correct because there is no previous hour, so no switch fee can apply. The invariant starts as: each state stores the optimal cost among all schedules ending in that mode.

3. **Transition to hour `i > 0` for mode A.**  
   `nextA = min(dpA + aCost[i], dpB + switchFee + aCost[i])`  
   Either we stayed in A, or we switched from B to A. Taking the minimum is correct because any valid schedule ending in A must come from exactly one of those two prior states.

4. **Transition symmetrically for mode B.**  
   `nextB = min(dpB + bCost[i], dpA + switchFee + bCost[i])`  
   This preserves the same invariant for schedules ending in B.

5. **Roll the state forward.**  
   Replace `dpA, dpB` with `nextA, nextB`. No full table is needed because each row depends only on the previous row. This reduces space from `O(n)` to `O(1)`.

6. **Return the best terminal state.**  
   The answer is `min(dpA, dpB)` after processing all hours. Any optimal full schedule must end in either A or B, so one of these two states is globally optimal.

Use 64-bit arithmetic throughout; with costs up to `1e9` over `1e5` hours, 32-bit integers overflow.

## 📊 Worked Example
Take `aCost = [3,8,2,5]`, `bCost = [4,1,6,1]`, `switchFee = 3`.

| Hour `i` | `aCost[i]` | `bCost[i]` | `dpA` calculation | `dpB` calculation | Result `(dpA, dpB)` |
|---|---:|---:|---|---|---|
| 0 | 3 | 4 | base = 3 | base = 4 | `(3, 4)` |
| 1 | 8 | 1 | `min(3+8, 4+3+8)=11` | `min(4+1, 3+3+1)=5` | `(11, 5)` |
| 2 | 2 | 6 | `min(11+2, 5+3+2)=10` | `min(5+6, 11+3+6)=11` | `(10, 11)` |
| 3 | 5 | 1 | `min(10+5, 11+3+5)=15` | `min(11+1, 10+3+1)=12` | `(15, 12)` |

Final answer: `min(15, 12) = 12`.

Interpretation: the cheapest complete schedule ends in B with total cost `12`. The trace shows why local per-hour minima are insufficient: hour 2 prefers A only because the prior state made switching worthwhile.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each hour performs a constant number of arithmetic operations and `min` comparisons over two prior states. At `10^6` elements this is routine in a single pass; at `10^9`, even linear time becomes operationally expensive, so the asymptotic win is necessary but not sufficient without partitioning or streaming constraints.

### Space Complexity
`O(1)`. Only two rolling DP values are required: best cost ending in A and best cost ending in B. A full `O(n)` table is unnecessary unless you also need to reconstruct the actual schedule, which this problem explicitly does not require.

## 💡 Key Takeaways
- If each decision’s cost depends on the immediately previous choice, look for DP over “last state” rather than brute-force enumeration.
- When there are only a small fixed number of terminal states per position, rolling-state DP is usually the right compression.
- Do not charge `switchFee` on hour `0`; there is no prior mode.
- Update both next states from the previous pair before overwriting, or you will mix old and new rows and corrupt the recurrence.
- In production systems, transition penalties often dominate steady-state cost; modeling mode changes explicitly prevents oscillation and produces globally stable plans.

## 🚀 Variations & Further Practice
- **Recover the actual schedule, not just the minimum cost.** Add parent pointers or a predecessor table; same recurrence, but space increases to support path reconstruction.
- **Generalize from 2 modes to `k` modes.** State becomes “minimum cost ending in mode `j`”; the twist is handling `k^2` transitions efficiently when switch costs vary by mode pair.
- **Add cooldown or minimum-run constraints.** Now the state must include how long the current mode has been active, turning a two-state DP into a richer finite-state machine.