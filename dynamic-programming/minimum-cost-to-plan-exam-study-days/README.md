# Minimum Cost to Plan Exam Study Days

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Array, Memoization

---

## 🗂 Problem Overview
Given a strictly increasing list of required study days and three pass costs for 1, 3, and 7 consecutive days, compute the minimum spend needed to cover every required day. The output is a single integer: the cheapest valid plan. The non-trivial part is that a pass covers calendar ranges, not just listed days, so local greedy choices fail; the optimal decision for one day depends on how many future required days the pass absorbs.

## 🌍 Engineering Impact
This pattern shows up anywhere you choose among overlapping coverage windows with different fixed costs: cloud reserved-capacity planning, CDN cache prewarming windows, API quota bundles, ad-budget pacing, and maintenance scheduling. The core issue is not selecting an item, but selecting a time interval whose value depends on future demand density. At scale, brute-force exploration explodes because overlapping options create combinatorial branching. Dynamic programming turns that into a compact state transition model, enabling predictable latency, simpler correctness reasoning, and easy extension when product teams add new pass types, pricing tiers, or blackout constraints.

## 🔍 Problem Statement
You are given:

- `studyDays`: a strictly increasing array of integers in `[1, 365]`
- `costs`: an array of three integers where:
  - `costs[0]` = cost of a 1-day pass
  - `costs[1]` = cost of a 3-day pass
  - `costs[2]` = cost of a 7-day pass

A pass bought on day `d` covers consecutive calendar days starting at `d`. Every day in `studyDays` must be covered by at least one pass. Passes may overlap and may cover non-required days.

Return the minimum total cost.

Constraints:

- `1 <= studyDays.length <= 365`
- `1 <= studyDays[i] <= 365`
- `studyDays` is strictly increasing
- `costs.length == 3`
- `1 <= costs[i] <= 1000`

Examples:

- `studyDays = [1,4,6,7,8,20], costs = [2,7,15]` → `11`
- `studyDays = [2,3,4,5,9,10,11,30], costs = [3,8,14]` → `17`

The key constraint is that brute force over pass combinations is unnecessary but tempting; the bounded day range and overlapping subproblems strongly suggest dynamic programming.

## 🪜 How to Solve This
1. Start from the first uncovered required day → any valid plan must cover it somehow.
2. On that day, there are only three meaningful choices: buy a 1-day, 3-day, or 7-day pass.
3. Each choice does two things at once:
   - adds a fixed cost
   - skips forward to the first required day not covered by that pass
4. That means the problem naturally decomposes into:  
   **minimum cost from index `i` onward**.
5. Once you phrase it that way, the recurrence is obvious:  
   `dp(i) = min(cost(pass) + dp(nextUncoveredIndex))` over the three pass types.
6. Why not greedy? Because the cheapest pass today may force more expensive purchases later, while a longer pass may absorb a dense cluster of future days.
7. Why dynamic programming? Because many decision paths ask the same question: “what is the minimum cost starting from required day `i`?”
8. Memoization or bottom-up DP removes repeated work and gives a small, deterministic state space: at most one state per required day.

This is classic interval-coverage DP: decisions are local, but optimality is global.

## 🧩 Algorithm Walkthrough
1. **Define the state using Dynamic Programming with Memoization.**  
   Let `dp(i)` be the minimum cost to cover all required study days starting from `studyDays[i]`. If `i == n`, no days remain, so the cost is `0`. This state is sufficient because past choices only matter through which index is the first uncovered day.

2. **Enumerate the only three valid actions.**  
   From `studyDays[i]`, try buying:
   - a 1-day pass covering through `day`
   - a 3-day pass covering through `day + 2`
   - a 7-day pass covering through `day + 6`  
   No other action changes the future state in a meaningful way.

3. **Advance to the next uncovered index.**  
   For each pass duration, scan forward until reaching the first `studyDays[j]` outside the covered range. This preserves the invariant that `j` always points to the earliest uncovered required day after buying that pass.

4. **Apply the recurrence.**  
   Compute  
   `dp(i) = min(costs[k] + dp(nextIndexForPassK))` for `k ∈ {0,1,2}`.  
   This is correct by optimal substructure: once the current pass is fixed, the remaining suffix is an identical subproblem.

5. **Memoize results.**  
   Cache `dp(i)` so each suffix is solved once. Without caching, the recursion tree branches exponentially; with caching, each index is evaluated once.

6. **Return `dp(0)`.**  
   That represents the minimum cost to cover the full schedule.

This is the right abstraction because the problem is not about days on the calendar; it is about transitions between uncovered required-day indices.

## 📊 Worked Example
Example: `studyDays = [1,4,6,7,8,20]`, `costs = [2,7,15]`

| `i` | day | 1-day → next / cost | 3-day → next / cost | 7-day → next / cost | `dp(i)` |
|---|---:|---|---|---|---:|
| 5 | 20 | `6 / 2` | `6 / 7` | `6 / 15` | 2 |
| 4 | 8  | `5 / 2 + dp(5)=4` | `5 / 7 + 2=9` | `5 / 15 + 2=17` | 4 |
| 3 | 7  | `4 / 2 + 4=6` | `5 / 7 + 2=9` | `5 / 15 + 2=17` | 6 |
| 2 | 6  | `3 / 2 + 6=8` | `5 / 7 + 2=9` | `5 / 15 + 2=17` | 8 |
| 1 | 4  | `2 / 2 + 8=10` | `4 / 7 + 4=11` | `5 / 7 + 2=9` | 9 |
| 0 | 1  | `1 / 2 + 9=11` | `1 / 7 + 9=16` | `4 / 15 + 4=19` | 11 |

Optimal plan: 1-day on day 1, 7-day on day 4, 1-day on day 20.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)` to `O(n * 3 * avgSkip)` in practice, with `n <= 365`. More precisely, each DP state considers three pass types and advances indices forward over a bounded input, so worst-case work is `O(n^2)` with naive scanning, still trivial here. At `10^6` or `10^9` scale, you would need binary search or pointer reuse; the naive scan would stop being acceptable.

### Space Complexity
`O(n)` for the memoization table or bottom-up DP array, where `n` is the number of required study days. Recursive memoization also uses `O(n)` call stack in the worst case. You can reduce stack usage with iterative DP, but not the state storage unless you switch to a calendar-day DP formulation.

## 💡 Key Takeaways
- If choices cover a variable-length future window and you need the minimum total cost, think interval-coverage dynamic programming.
- If the input is sorted and the next state is “first uncovered index after applying an option,” memoization over indices is usually the cleanest model.
- Coverage is inclusive: a 3-day pass bought on day `d` covers `d, d+1, d+2`, not three arbitrary required days.
- The transition target is the first required day **outside** the covered range; getting that boundary wrong produces subtle off-by-one bugs.
- In production systems, this pattern matters because it converts combinatorial planning over overlapping windows into a compact state machine with predictable cost.

## 🚀 Variations & Further Practice
- Add arbitrary pass durations and prices, not just 1/3/7 days. The twist is generalizing the transition efficiently, often with binary search over the next uncovered index.
- Add per-day penalties or rewards, where skipping coverage is allowed at a cost. The twist is that the state now models trade-offs between coverage and violation budgets.
- Extend to multiple resources or people sharing bundle purchases. The twist is state explosion: the DP must encode joint coverage state, often requiring bitmasking or decomposition.