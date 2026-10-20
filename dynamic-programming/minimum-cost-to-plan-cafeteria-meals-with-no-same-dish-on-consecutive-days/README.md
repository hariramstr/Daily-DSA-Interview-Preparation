# Minimum Cost to Plan Cafeteria Meals with No Same Dish on Consecutive Days

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given `costs[i][j]`, where each day `i` has exactly three meal choices, compute the minimum total cost of selecting one meal per day such that the same meal is never chosen on consecutive days. The input is an `n x 3` array; the output is a single integer: the minimum achievable total cost. The non-trivial part is that each day’s optimal choice depends on which meal was chosen the previous day, so greedy local decisions are insufficient.

## 🌍 Engineering Impact
This pattern shows up anywhere a system makes sequential decisions under local exclusion constraints: ad-serving rotation to avoid repeated creatives, job schedulers preventing hot-spot assignment to the same worker, search ranking diversification, and streaming pipelines choosing fallback paths without repeating degraded routes. At scale, brute-force enumeration collapses immediately because the state space grows exponentially with sequence length. Dynamic programming turns a path-dependent optimization problem into a linear scan with bounded state. That shift matters operationally: predictable latency, constant working memory, and a formulation that can be embedded inside larger planning, pricing, or scheduling systems without destabilizing throughput.

## 🔍 Problem Statement
You are given a 2D integer array `costs` with `n = costs.length` days and exactly 3 meal options per day:

- `costs[i][0]` = cost of Pasta on day `i`
- `costs[i][1]` = cost of Salad on day `i`
- `costs[i][2]` = cost of Soup on day `i`

You must choose exactly one meal for every day, and you cannot choose the same meal on two consecutive days. Return the minimum total cost over all valid meal plans.

Constraints:

- `1 <= costs.length <= 100000`
- `costs[i].length == 3`
- `1 <= costs[i][j] <= 10000`

The large upper bound on `n` rules out exponential search and strongly favors an `O(n)` solution.

Examples:

- `costs = [[5,9,4],[8,7,6],[3,5,10]]` → `14`
- `costs = [[7,2,8],[6,4,3],[5,9,1],[2,7,6]]` → `11`

Edge case: if there is only one day, the answer is simply the minimum of that day’s three costs.

## 🪜 How to Solve This
1. Read the constraint carefully → each day depends only on the previous day, not the full history. That is the first signal for dynamic programming.

2. Define the right state → instead of asking “what is the cheapest plan up to day `i`?”, ask “what is the cheapest plan up to day `i` if day `i` ends with Pasta / Salad / Soup?” That captures exactly the information needed for the next decision.

3. Write the transition → if today is Pasta, yesterday must have been Salad or Soup. So today’s Pasta cost is `costs[i][0] + min(prevSalad, prevSoup)`. The same logic applies symmetrically to the other two meals.

4. Initialize from day 0 → the cheapest way to end day 0 with a given meal is just that meal’s direct cost.

5. Iterate once → each new row depends only on the previous row, so there is no need to store the full DP table.

6. Finish by taking the minimum of the three states on the last day. That is the global optimum because every valid plan must end in exactly one of those states.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Dynamic Programming over a linear sequence.**  
   The problem has optimal substructure: the best valid plan ending with a specific meal today depends only on the best valid plans ending with the other two meals yesterday. This is the right abstraction because the constraint is local and sequential.

2. **Define DP state.**  
   Let `p`, `s`, and `u` represent the minimum total cost up to the current day if the current day ends with Pasta, Salad, and Soup respectively. The invariant is: after processing day `i`, each variable stores the optimal valid cost for that ending meal.

3. **Initialize base case.**  
   For day `0`, set:
   - `p = costs[0][0]`
   - `s = costs[0][1]`
   - `u = costs[0][2]`  
   This is correct because there is no previous day, so no adjacency restriction applies yet.

4. **Apply transitions for each subsequent day.**  
   For day `i`, compute:
   - `newP = costs[i][0] + min(s, u)`
   - `newS = costs[i][1] + min(p, u)`
   - `newU = costs[i][2] + min(p, s)`  
   Each transition excludes the same meal from the previous day, preserving the no-repeat invariant.

5. **Roll state forward.**  
   Replace `(p, s, u)` with `(newP, newS, newU)`. Only the previous day matters, so constant-space state is sufficient.

6. **Return the final answer.**  
   After the last day, return `min(p, s, u)`. This is correct because every valid complete plan must terminate in one of the three meal states, and each state already represents the cheapest such plan.

## 📊 Worked Example
Use `costs = [[5,9,4],[8,7,6],[3,5,10]]`.

| Day | Pasta cost | Salad cost | Soup cost | `p` | `s` | `u` |
|---|---:|---:|---:|---:|---:|---:|
| 0 | 5 | 9 | 4 | 5 | 9 | 4 |
| 1 | 8 | 7 | 6 | `8 + min(9,4)=12` | `7 + min(5,4)=11` | `6 + min(5,9)=11` |
| 2 | 3 | 5 | 10 | `3 + min(11,11)=14` | `5 + min(12,11)=16` | `10 + min(12,11)=21` |

Trace:

1. Day 0 initializes the three ending states directly.
2. Day 1 computes the cheapest valid way to end with each meal while avoiding repetition.
3. Day 2 repeats the same transition logic.
4. Final answer is `min(14, 16, 21) = 14`.

An optimal plan is Soup → Salad → Pasta with total cost `4 + 7 + 3 = 14`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is the number of days. Each day performs a constant amount of work: three transitions and a few `min` operations. At `10^6` elements this is still a straightforward linear pass; at `10^9`, runtime becomes dominated by raw scan cost rather than algorithmic overhead.

### Space Complexity
`O(1)` extra space. The algorithm keeps only three DP values for the previous day and three temporary values for the current day. A full `O(n)` DP table is unnecessary unless you also need to reconstruct the exact meal sequence.

## 💡 Key Takeaways
- If a sequence problem says “make one choice per position” and the legality of today’s choice depends only on yesterday, think rolling-state dynamic programming.
- If the objective is a global minimum but local greedy picks can block future choices, model “best cost ending in state X” rather than “best cost so far.”
- Do not overwrite previous-day state before computing all three new values; otherwise transitions will accidentally mix old and new rows.
- Handle the single-day case implicitly via initialization; off-by-one bugs usually come from starting transitions at day `0` instead of day `1`.
- In production systems, bounded-state DP is the standard move when path-dependent optimization can be reduced to a small interface between past and future.

## 🚀 Variations & Further Practice
- Generalize from 3 meals to `k` meals. The twist is reducing the naive `O(nk^2)` transition cost, often by tracking the smallest and second-smallest previous values.
- Add a penalty matrix where switching from meal `a` to meal `b` has its own cost. The state remains sequential, but transitions become weighted edges between states.
- Require reconstructing the actual optimal meal plan, not just the minimum cost. This introduces predecessor tracking and changes the space trade-off from `O(1)` to `O(n)`.