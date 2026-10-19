# Maximum Reward from Alternating Project Types

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, state-machine, array

---

## 🗂 Problem Overview
Given two reward arrays `aReward` and `bReward`, choose at most one action per week: do type A, do type B, or skip. The objective is to maximize total reward across `n` weeks. The non-trivial constraint is that worked weeks must alternate project type based on the last non-skipped week; skipping preserves that prior type rather than resetting it. The output is the maximum achievable total reward, and `n` is large enough that quadratic exploration is not viable.

## 🌍 Engineering Impact
This pattern shows up in schedulers and policy engines where the next valid action depends on the last committed action, not merely the immediately previous time slot. Examples include ad-serving rotation constraints, streaming job placement across heterogeneous worker pools, market-making strategies that must alternate exposure classes, and workflow engines with cooldown-like rules that survive idle periods. At scale, greedy local choice fails because “skip now to unlock a better future state” is often globally optimal. A compact state-machine DP gives deterministic behavior, linear throughput, and predictable memory use under large backlogs.

## 🔍 Problem Statement
You are given two integer arrays, `aReward` and `bReward`, each of length `n` (`1 <= n <= 200000`). On week `i`, exactly one of three actions is allowed:

- complete the type A project and earn `aReward[i]`
- complete the type B project and earn `bReward[i]`
- skip and earn `0`

The constraint: among worked weeks, project types must alternate. If the last non-skipped week used type A, the next worked week must use type B. If it used type B, the next worked week must use type A. Skipping does **not** reset this requirement.

Return the maximum total reward. Rewards satisfy `0 <= aReward[i], bReward[i] <= 10^9`, and the result fits in signed 64-bit.

Examples:

- `aReward = [5,1,7,4]`, `bReward = [2,10,3,8]` → `30`
- `aReward = [9,1,1,9]`, `bReward = [1,8,8,1]` → `34`

The `n = 200000` bound rules out any approach that branches over histories or scans backward per position.

## 🪜 How to Solve This
1. Read the constraint carefully → the legality of week `i` depends only on the **last worked type**, not on the full schedule.
2. That immediately suggests a **state machine**: after processing each week, the system is in one of a few states:
   - no project has ever been taken yet
   - last worked type was A
   - last worked type was B
3. Now ask: what transitions are possible this week?
   - from “none”, you may start with A or B, or skip
   - from “last A”, you may take B or skip
   - from “last B”, you may take A or skip
4. Because reward is additive and future legality depends only on the current state, this is classic **dynamic programming over states**.
5. The arrays are large, so store only the best value for each state after each week. You never need the full DP table; only the previous week’s state values matter.
6. The key insight is that skipping is just a transition that preserves state and reward, which is why a greedy “always take the larger reward” strategy is insufficient.

## 🧩 Algorithm Walkthrough
1. **Model the problem as a 3-state DP.**  
   Maintain:
   - `none`: best reward so far if no project has been taken yet
   - `lastA`: best reward so far if the last worked week used type A
   - `lastB`: best reward so far if the last worked week used type B  
   This is the **state-machine dynamic programming** pattern: a small finite state captures all future-relevant history.

2. **Initialize the states.**  
   Before processing any week:
   - `none = 0`
   - `lastA = -∞`
   - `lastB = -∞`  
   The invariant is that each variable stores the best achievable reward for that exact ending condition after the processed prefix.

3. **Process each week with transition equations.**  
   For week `i`, compute new states from the previous ones:
   - `newNone = none` because skipping before any work still means no work has been done
   - `newLastA = max(lastA, none + aReward[i], lastB + aReward[i])`
   - `newLastB = max(lastB, none + bReward[i], lastA + bReward[i])`  
   The `max(lastA, ...)` and `max(lastB, ...)` terms represent skipping while preserving the last worked type.

4. **Why this is correct.**  
   Every valid schedule over the first `i` weeks must end in exactly one of these states. Each transition enumerates all legal ways to reach that state this week, and no illegal same-type consecutive worked weeks are admitted.

5. **Return the best terminal state.**  
   The answer is `max(none, lastA, lastB)`. In practice `none` matters only when all rewards are zero, but including it keeps the formulation complete and robust.

## 📊 Worked Example
Use `aReward = [5,1,7,4]`, `bReward = [2,10,3,8]`.

| Week | a | b  | none | lastA | lastB |
|------|---|----|------|-------|-------|
| init | - | -  | 0    | -∞    | -∞    |
| 0    | 5 | 2  | 0    | 5     | 2     |
| 1    | 1 | 10 | 0    | 5     | 15    |
| 2    | 7 | 3  | 0    | 22    | 15    |
| 3    | 4 | 8  | 0    | 22    | 30    |

Trace:
1. Week 0: start with A or B → `lastA=5`, `lastB=2`.
2. Week 1: best B is from previous A → `5 + 10 = 15`.
3. Week 2: best A is from previous B → `15 + 7 = 22`.
4. Week 3: best B is from previous A → `22 + 8 = 30`.

Final answer: `max(0, 22, 30) = 30`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each week performs a constant number of arithmetic operations and `max` comparisons over three states. This scales comfortably to `n = 2 * 10^5` and remains practical even at `10^6`. At `10^9`, linear time is still too large in absolute terms, but asymptotically this is optimal because every input value must be examined.

### Space Complexity
`O(1)`. The algorithm stores only three rolling DP states plus temporary next-state values. No auxiliary arrays are required. You could materialize the full DP table for debugging or reconstruction, but that increases space to `O(n)` without improving the reward-only computation.

## 💡 Key Takeaways
- If future legality depends on a tiny summary of prior actions rather than the full history, think state-machine DP immediately.
- When skipping is allowed but does not reset constraints, greedy selection is suspect; preserved state usually implies dynamic programming.
- Do not treat a skip as “clearing” the last project type; the alternation rule is based on the last **non-skipped** week.
- Use 64-bit arithmetic throughout: rewards can be `10^9`, and totals across `2 * 10^5` weeks exceed 32-bit range.
- In production systems, compact finite-state models often turn policy-heavy scheduling rules into linear, testable, and throughput-friendly implementations.

## 🚀 Variations & Further Practice
- Add a switching penalty between A and B. The state machine remains small, but transitions now include costs, forcing more careful recurrence design.
- Require exactly `k` worked weeks instead of any number. This adds a count dimension to the DP, turning `O(n)` state into `O(nk)` or requiring optimization.
- Generalize from two project types to `m` types with “cannot repeat last worked type.” The conceptual twist is scaling the state space and transition cost efficiently.