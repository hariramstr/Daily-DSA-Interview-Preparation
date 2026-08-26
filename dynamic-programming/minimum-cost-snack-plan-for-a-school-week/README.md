# Minimum Cost Snack Plan for a School Week

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given a sorted list of calendar days when a student wants a snack, compute the minimum total price needed to cover all those days using only 1-day, 3-day, and 7-day passes. Each pass covers a contiguous range starting on its purchase day. The output is a single minimum cost. The problem is non-trivial because a locally cheap choice can be globally suboptimal: buying a longer pass earlier may reduce total cost across multiple future snack days.

## 🌍 Engineering Impact
This pattern shows up anywhere you must cover sparse demand points with overlapping pricing windows: cloud reserved-capacity planning, CDN burst protection, API quota bundles, warehouse labor scheduling, and mobile data/package optimization. At scale, greedy local decisions overpay because they ignore future coverage overlap. Dynamic programming gives a bounded, deterministic way to evaluate trade-offs between short-term and long-term commitments. That matters when pricing models are tiered, demand is irregular, and you need predictable optimization behavior without exponential search or brittle heuristics.

## 🔍 Problem Statement
You are given:

- `days`: a strictly increasing integer array of snack days
- `costs`: an integer array of length 3 where:
  - `costs[0]` = price of a 1-day pass
  - `costs[1]` = price of a 3-day pass
  - `costs[2]` = price of a 7-day pass

A pass bought on day `d` covers day `d` and the next consecutive days in its duration. Return the minimum total cost required to cover every day in `days`.

Constraints:

- `1 <= days.length <= 365`
- `1 <= days[i] <= 365`
- `days` is strictly increasing
- `costs.length == 3`
- `1 <= costs[i] <= 1000`

Examples:

- `days = [1,2,4,5,6], costs = [3,7,12]` → `12`
- `days = [2,3,8,9,10,14], costs = [2,5,9]` → `11`

The key constraint is that coverage decisions overlap across future days, which rules out simple greedy selection.

## 🪜 How to Solve This
1. Read the problem → notice we do **not** need to cover every calendar day, only the days listed in `days`.
2. Each purchase decision affects a range of future required days → that is a strong signal for **dynamic programming**.
3. Define the subproblem: “What is the minimum cost to cover snack days starting from index `i`?”
4. From any index `i`, there are only three legal decisions:
   - buy a 1-day pass covering `days[i]`
   - buy a 3-day pass covering all required days `< days[i] + 3`
   - buy a 7-day pass covering all required days `< days[i] + 7`
5. For each choice, jump to the first uncovered day and add the corresponding pass cost.
6. Take the minimum of those three outcomes.
7. Because later answers are reused by earlier states, memoization or bottom-up DP avoids recomputing the same suffix repeatedly.

The key insight is that the problem is about choosing the cheapest next coverage window, not simulating every calendar day.

## 🧩 Algorithm Walkthrough
1. **Model the state with Dynamic Programming.**  
   Let `dp[i]` be the minimum cost to cover all snack days from index `i` onward. This is the right abstraction because once earlier days are covered, the remaining problem depends only on the next uncovered snack day.

2. **Define the base case.**  
   If `i == days.length`, there are no remaining snack days, so the cost is `0`. This anchors the recurrence and guarantees termination.

3. **Evaluate the 1-day option.**  
   Buy a 1-day pass on `days[i]`. It covers only that day, so the next state is `i + 1`. Candidate cost: `costs[0] + dp[i + 1]`.  
   Invariant: all days before the next index are fully covered.

4. **Evaluate the 3-day option.**  
   Find the first index `j` where `days[j] >= days[i] + 3`. All days before `j` are covered by this pass. Candidate cost: `costs[1] + dp[j]`.

5. **Evaluate the 7-day option.**  
   Find the first index `k` where `days[k] >= days[i] + 7`. Candidate cost: `costs[2] + dp[k]`.

6. **Take the minimum of the three candidates.**  
   This is correct because every valid plan must start with exactly one of these three pass types, and each recursively reduces to an optimal subproblem.

7. **Compute bottom-up or top-down with memoization.**  
   Given the small constraints, either works. The invariant throughout is: `dp[i]` always stores the optimal cost for suffix `i...end`.

## 📊 Worked Example
Use `days = [2,3,8,9,10,14]`, `costs = [2,5,9]`.

Let `dp[i]` be min cost from `days[i]` onward.

| i | day | 1-day | 3-day jump | 7-day jump | dp[i] |
|---|-----|------:|-----------:|-----------:|------:|
| 6 | —   | —     | —          | —          | 0     |
| 5 | 14  | 2+dp6=2 | 5+dp6=5  | 9+dp6=9    | 2     |
| 4 | 10  | 2+dp5=4 | 5+dp5=7  | 9+dp6=9    | 4     |
| 3 | 9   | 2+dp4=6 | 5+dp5=7  | 9+dp6=9    | 6     |
| 2 | 8   | 2+dp3=8 | 5+dp5=7  | 9+dp6=9    | 7     |
| 1 | 3   | 2+dp2=9 | 5+dp2=12 | 9+dp4=13   | 9     |
| 0 | 2   | 2+dp1=11| 5+dp2=12 | 9+dp4=13   | 11    |

Answer: `dp[0] = 11`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` with the common day-based DP over at most 365 calendar days, or `O(n)` amortized / `O(n^2)` worst-case with index-jump scanning depending on implementation. In practice, with `n <= 365`, both are trivial. At `10^6` scale, only the linear formulation remains operationally safe; at `10^9`, you need compressed states and streaming assumptions.

### Space Complexity
`O(n)` for the DP array if you model states by snack-day index, or `O(365)` with calendar-day DP. The space is owned by memoization/state storage. It can be reduced only slightly, but usually not to `O(1)` without complicating jump logic and hurting clarity.

## 💡 Key Takeaways
- If a choice covers a variable-length future range and you need a minimum total cost, suspect dynamic programming over the next uncovered position.
- Sparse required days are a signal to model states around demand points, not every possible action sequence.
- Coverage boundaries are exclusive on the next uncovered day: for a 3-day pass bought on day `d`, advance while `day < d + 3`.
- Do not assume the cheapest pass per day is optimal; overlap value is the entire problem.
- The production-grade lesson is to optimize over reusable subproblems when pricing or capacity decisions have forward coverage effects.

## 🚀 Variations & Further Practice
- Add arbitrary pass durations and prices instead of fixed `{1,3,7}`. The harder part is generalizing the transition efficiently without hardcoded logic.
- Charge different pass prices by season or weekday. The twist is that state may need to include calendar position, not just demand index.
- Limit the number of long-duration passes allowed. This turns a simple 1D DP into a constrained DP with an additional resource dimension.