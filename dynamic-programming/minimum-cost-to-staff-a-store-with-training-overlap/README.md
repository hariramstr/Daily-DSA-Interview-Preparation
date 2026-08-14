# Minimum Cost to Staff a Store With Training Overlap

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given daily staffing requirements, choose how many one-day workers and two-day workers to hire so every day’s demand is met at minimum total cost. A temporary worker covers only day `i`; a cross-trained worker started on day `i` covers both `i` and `i+1`. The output is the minimum achievable cost. The problem is non-trivial because a decision made today changes tomorrow’s remaining demand, so greedy local pricing is insufficient.

## 🌍 Engineering Impact
This pattern shows up anywhere a purchase or allocation decision has overlap across adjacent time windows: cloud reserved capacity vs on-demand instances, CDN prewarming across intervals, workforce scheduling, battery dispatch, and ad-budget pacing with carryover effects. At scale, greedy heuristics systematically misprice future coverage and create either shortages or expensive overprovisioning. Dynamic programming gives a compact state model for “what has already been prepaid into the next slot,” which is exactly the abstraction needed when local actions have bounded downstream effects. That enables predictable optimization without exploding into full search or integer programming.

## 🔍 Problem Statement
You are given:

- `required[i]`: minimum workers needed on day `i`
- `tempCost[i]`: cost of one worker covering only day `i`
- `pairCost[i]`: cost of one worker started on day `i` covering days `i` and `i+1`

You may hire any non-negative number of either worker type, and each hired worker contributes exactly `1` unit of staffing on every covered day. A two-day hire is valid only for `0 <= i < n - 1`. Return the minimum total cost to satisfy all days.

Constraints:

- `1 <= n <= 200`
- `0 <= required[i] <= 200`
- `1 <= tempCost[i] <= 10^4`
- `1 <= pairCost[i] <= 10^4`
- `pairCost.length == n - 1`

Examples:

- `required = [2,1,2], tempCost = [5,4,5], pairCost = [7,6]`
- `required = [1,3,1,2], tempCost = [6,3,8,4], pairCost = [5,10,7]`

The key algorithmic constraint is that a two-day hire affects both the current and next day, so the optimal choice depends on carried coverage.

## 🪜 How to Solve This
1. Read the problem → notice the only thing that survives from day `i-1` into day `i` is how many two-day workers were started yesterday.

2. That means the full hiring history does **not** matter. The state can be compressed to:
   - current day index
   - how much staffing is already carried into this day

3. On day `i`, if `carry` workers already cover the day, then the remaining demand is `max(0, required[i] - carry)`.

4. We now choose how to satisfy that remaining demand:
   - hire some temporary workers for today only
   - hire some two-day workers that help today and create carry for tomorrow

5. If we hire `x` two-day workers today, they contribute to today immediately, so temps needed become `max(0, required[i] - carry - x)`.

6. That naturally leads to DP:
   - try every feasible `x`
   - pay `x * pairCost[i] + temps * tempCost[i]`
   - recurse to day `i+1` with carry `x`

7. Why this works: the interaction horizon is only one day ahead. Once you know tomorrow’s carry, earlier choices are irrelevant.

## 🧩 Algorithm Walkthrough
1. **Define the DP pattern: finite-horizon dynamic programming with carry state.**  
   Let `dp[i][carry]` be the minimum cost to satisfy days `i..n-1`, given that `carry` workers from day `i-1` already cover day `i`. This is the right abstraction because two-day hires are the only cross-day dependency.

2. **Bound the state space.**  
   `carry` never needs to exceed `required[i]`. Extra coverage on day `i` beyond its demand has no additional value, and the only future effect is exactly the number of pair hires started today. Since `required[i] <= 200`, the state space stays small.

3. **Handle the last day separately.**  
   On day `n-1`, no pair hire is allowed. So  
   `dp[n-1][carry] = max(0, required[n-1] - carry) * tempCost[n-1]`.  
   This is correct because temporary workers are the only valid way to fill the remaining gap.

4. **Transition for earlier days.**  
   For each day `i < n-1` and each `carry`, try all `x` from `0` to `required[i]`. Here `x` is the number of two-day workers started on day `i`.  
   Current-day unmet demand after using `carry` and `x` is  
   `need = max(0, required[i] - carry - x)`.  
   Candidate cost is  
   `x * pairCost[i] + need * tempCost[i] + dp[i+1][x]`.

5. **Take the minimum candidate.**  
   This is correct because every feasible plan for day `i` can be uniquely decomposed into:
   - number of pair hires started today
   - number of temp hires used to close today’s remaining gap
   - optimal continuation from tomorrow with carry `x`

6. **Return `dp[0][0]`.**  
   Initially, no previous day exists, so there is no carried coverage.

## 📊 Worked Example
Use `required = [1,3,1,2]`, `tempCost = [6,3,8,4]`, `pairCost = [5,10,7]`.

| Day `i` | Carry in | Try pair hires `x` | Temp needed today | Immediate cost | Next state |
|---|---:|---:|---:|---:|---:|
| 3 | any | invalid | `max(0, 2-carry)` | `4 * need` | end |
| 2 | 0 | 0 | 1 | 8 + `dp[3][0]` | `dp[3][0]` |
| 2 | 0 | 1 | 0 | 7 + `dp[3][1]` | `dp[3][1]` |
| 1 | 1 | 0 | 2 | 6 + `dp[2][0]` | `dp[2][0]` |
| 1 | 1 | 1 | 1 | 13 + `dp[2][1]` | `dp[2][1]` |
| 0 | 0 | 0 | 1 | 6 + `dp[1][0]` | `dp[1][0]` |
| 0 | 0 | 1 | 0 | 5 + `dp[1][1]` | `dp[1][1]` |

The DP evaluates all such transitions bottom-up. The important observation is that the only information passed forward is the number of pair hires started today.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * R^2)`, where `R = max(required)`. For each day and each possible carry value, we try all possible counts of two-day hires. With `n <= 200` and `R <= 200`, this is comfortably bounded. At `10^6` or `10^9` scale, quadratic-in-demand state expansion would be unacceptable without stronger structure.

### Space Complexity
`O(n * R)` for the DP table, or `O(R)` with rolling arrays because each row depends only on the next day. The space is owned entirely by the carry-state DP. Rolling compression reduces memory but makes debugging and traceability slightly worse.

## 💡 Key Takeaways
- If a decision affects the current position and exactly one future position, look for DP with a small “carry” state instead of full-history tracking.
- When greedy pricing per day feels plausible but wrong, that is a strong signal that local decisions are creating deferred value or deferred cost.
- The last day is a special case: starting a two-day worker there is invalid, so only temporary hires can close the gap.
- Do not let the carry dimension grow unbounded; coverage beyond the day’s requirement is never useful to represent explicitly.
- In production optimization systems, the winning move is often identifying the minimal sufficient state, not the cleverest search strategy.

## 🚀 Variations & Further Practice
- Allow 3-day or variable-length training plans. The twist is that the state must track multiple future carry buckets, not just one scalar.
- Add upper bounds on how many workers can be hired under each plan per day. The twist is feasibility becomes constrained, not just cost-minimizing.
- Charge penalties for overstaffing or require exact staffing. The twist is extra coverage is no longer harmless, so transition logic and state pruning both change.