# Minimum Cost to Cover a Workweek with Flexible Passes

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given a strictly increasing list of meal days and the prices of 1-day, 5-day, and 20-day passes, compute the minimum total spend needed to cover every required day. The challenge is that a decision made today affects future coverage: a longer pass may be more expensive upfront but cheaper overall. With up to 365 calendar days, brute-forcing pass combinations is unnecessary but naive greedy choices are still wrong.

## 🌍 Engineering Impact
This pattern shows up anywhere you choose among overlapping coverage windows with different costs: cloud reserved-capacity planning, CDN cache prewarming windows, API quota bundles, subscription packaging, and batch scheduling with fixed-duration leases. At scale, local cheapest-choice heuristics fail because coverage decisions create downstream optionality. Dynamic programming gives a compact way to reason over future cost while preserving deterministic runtime. Without it, systems either overpay due to greedy allocation or become operationally expensive by exploring too many combinations. This approach enables predictable optimization under bounded horizons.

## 🔍 Problem Statement
You are given:

- `days`: a strictly increasing array of integers, where each value is a calendar day in `[1, 365]` on which the employee will eat at the cafeteria
- `cost1`, `cost5`, `cost20`: the costs of 1-day, 5-day, and 20-day passes

A pass bought on day `d` covers day `d` and the next consecutive days in its duration:
- 1-day pass → covers `[d, d]`
- 5-day pass → covers `[d, d + 4]`
- 20-day pass → covers `[d, d + 19]`

Return the minimum total cost to cover all days in `days`.

Constraints:
- `1 <= days.length <= 365`
- `1 <= days[i] <= 365`
- `days` is strictly increasing
- `1 <= cost1, cost5, cost20 <= 10^5`

Examples:
- `days = [1,2,3,6,7,8,21], cost1 = 3, cost5 = 7, cost20 = 18` → `14`
- `days = [4,5,9,10,11,30,31], cost1 = 4, cost5 = 9, cost20 = 25` → `18`

The key constraint is not input size alone; it is overlapping future coverage, which makes greedy selection unreliable.

## 🪜 How to Solve This
1. Read the problem → notice each purchase covers a *range* of future days, so the cost of covering day `i` depends on what future days that pass absorbs.

2. Greedy is suspicious immediately:
   - cheapest pass per day can miss bundle savings
   - longest pass whenever possible can overpay on sparse schedules

3. That suggests dynamic programming: define the minimum cost from some point onward, then try all valid next decisions.

4. Natural state: “what is the minimum cost to cover meal days starting at index `i`?”  
   From `days[i]`, there are only three choices:
   - buy a 1-day pass
   - buy a 5-day pass
   - buy a 20-day pass

5. For each choice, jump to the first uncovered day after that pass expires.  
   That is the key simplification: we do not care about uncovered calendar days, only the next relevant meal day.

6. Recurrence becomes:
   `dp[i] = min(cost1 + dp[next1], cost5 + dp[next5], cost20 + dp[next20])`

7. Because `days.length <= 365`, even a straightforward scan to find each next index is fast enough. The result is compact, deterministic, and easy to reason about.

## 🧩 Algorithm Walkthrough
1. **Model the problem as Dynamic Programming over meal-day indices.**  
   Let `dp[i]` be the minimum cost to cover all required days from `days[i]` onward. This is the right abstraction because decisions matter only at required consumption points, not on every calendar day.

2. **Define the base case.**  
   If `i == n`, there are no remaining meal days, so `dp[n] = 0`.  
   Invariant: `dp[k]` always represents the optimal cost for suffix `k..n-1`.

3. **At index `i`, evaluate the three pass options.**  
   Buying a pass on `days[i]` covers a fixed interval:
   - 1-day: through `days[i]`
   - 5-day: through `days[i] + 4`
   - 20-day: through `days[i] + 19`

4. **Find the next uncovered meal-day index for each pass.**  
   Advance an index until `days[j]` falls outside the covered interval.  
   Why correct: all days before `j` are fully covered by the chosen pass, so the remaining subproblem starts exactly at `j`.

5. **Apply the recurrence.**  
   `dp[i] = min(cost1 + dp[j1], cost5 + dp[j5], cost20 + dp[j20])`  
   This works because the first purchase partitions the problem into “cost of this pass” plus an optimal suffix.

6. **Compute bottom-up from right to left.**  
   By the time you compute `dp[i]`, all later states are already known.  
   Invariant: after processing position `i`, every `dp[k]` for `k >= i` is optimal.

7. **Return `dp[0]`.**  
   That is the minimum cost to cover the full schedule.

This is classic **Dynamic Programming on ordered events**, with small-window forward jumps.

## 📊 Worked Example
Use `days = [1,2,3,6,7,8,21]`, `cost1 = 3`, `cost5 = 7`, `cost20 = 18`.

| i | day | next after 1-day | next after 5-day | next after 20-day | dp[i] |
|---|-----|------------------|------------------|-------------------|------:|
| 7 | —   | —                | —                | —                 | 0 |
| 6 | 21  | 7                | 7                | 7                 | 3 |
| 5 | 8   | 6                | 6                | 7                 | 6 |
| 4 | 7   | 5                | 6                | 7                 | 9 |
| 3 | 6   | 4                | 6                | 7                 | 10 |
| 2 | 3   | 3                | 5                | 7                 | 13 |
| 1 | 2   | 2                | 4                | 7                 | 14 |
| 0 | 1   | 1                | 4                | 7                 | 14 |

Trace:
- At day 21, cheapest is one 1-day pass: `3`.
- At day 6, a 5-day pass covers `6,7,8`, then day 21 remains: `7 + 3 = 10`.
- At day 1, best is `1-day on 1` plus optimal suffix from day 2: `3 + 11 = 14`, or equivalently the computed minimum `14`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^2)` with the straightforward implementation, where `n = days.length`. For each index, you may scan forward to find the next uncovered index for each pass type. With `n <= 365`, this is trivial in practice. At `10^6` elements, this would be too slow; at `10^9`, completely infeasible without stronger indexing or pointer reuse.

### Space Complexity
`O(n)` for the DP array storing one optimal value per meal-day index. This can be reduced only marginally because transitions jump forward by variable distances, so full suffix state is useful. A calendar-day DP also stays small here but is less general.

## 💡 Key Takeaways
- If a choice covers a future interval and changes the next decision point, think dynamic programming over positions, not greedy local minimization.
- When the input is sparse but ordered, model state on relevant events (`days[i]`) rather than the full calendar unless the horizon is tiny.
- Coverage is inclusive: a 5-day pass bought on day `d` covers through `d + 4`, not `d + 5`.
- The “next uncovered day” must be the first `days[j] > covered_end`; using `>=` here creates an off-by-one bug.
- In production optimization systems, compressing state to decision-relevant events is often the difference between a tractable planner and an expensive simulation.

## 🚀 Variations & Further Practice
- Add arbitrary pass durations and costs as arrays; the twist is generalizing transitions efficiently while preserving ordered jumps.
- Introduce blackout days or pass-type restrictions on certain dates; the harder part is integrating feasibility constraints into the DP state.
- Optimize for two objectives, such as minimum cost and minimum number of purchases; this turns a scalar DP into lexicographic or Pareto-state optimization.