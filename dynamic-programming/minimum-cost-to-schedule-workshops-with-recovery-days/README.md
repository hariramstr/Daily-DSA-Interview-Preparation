# Minimum Cost to Schedule Workshops with Recovery Days

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, knapsack, scheduling

---

## 🗂 Problem Overview
Given `value[i]`, `cost[i]`, and `cooldown[i]` for each day, choose a subset of workshop days that respects recovery gaps and achieves at least `target` total participants. Among all valid schedules, minimize total operating cost. Return `-1` if no schedule can reach the target. The non-trivial part is that each decision affects both accumulated value and the next eligible day, so greedy local choices fail and the state must encode future scheduling constraints.

## 🌍 Engineering Impact
This pattern shows up anywhere work creates deferred unavailability: ad pacing with campaign cooldowns, maintenance scheduling in fleets, batch jobs that trigger cluster recovery windows, and notification systems with user-contact suppression periods. At scale, naive search explodes because each action changes both reward and future eligibility. Dynamic programming gives a bounded state model that converts a combinatorial schedule search into deterministic optimization. That matters operationally: it enables predictable latency, supports what-if planning, and provides a clean foundation for extensions like budget caps, fairness constraints, or multi-resource scheduling without rebuilding the decision model from scratch.

## 🔍 Problem Statement
You are given `N` calendar days, with `1 <= N <= 200`. On day `i`, running a workshop yields `value[i]` participants, costs `cost[i]`, and forces the next `cooldown[i]` days to remain empty. Therefore, if you schedule day `i`, the next possible workshop day is `i + cooldown[i] + 1`.

You may skip any day. The objective is to reach at least `target` participants, where `1 <= target <= 5000`, while minimizing total cost. Values satisfy `1 <= value[i] <= 100`, costs satisfy `1 <= cost[i] <= 1000`, and `0 <= cooldown[i] < N`. If no valid schedule reaches the target, return `-1`.

Examples:

- `value = [6,4,7,3], cost = [5,2,6,2], cooldown = [1,0,2,0], target = 10` → `8`
- `value = [5,8,4], cost = [4,9,3], cooldown = [2,1,0], target = 13` → `-1`

The key constraint is that `target` is moderate, so participant totals can be capped at `target` and used as a DP dimension.

## 🪜 How to Solve This
1. Read the problem → this is not plain interval scheduling and not plain knapsack. Each chosen day contributes value and cost, but also jumps the timeline forward.

2. Notice the optimization target → minimize cost subject to reaching at least `target` value. That is classic knapsack structure.

3. Notice the dependency between choices → picking day `i` changes the next valid index to `nextDay = i + cooldown[i] + 1`. That means the state must know where we are in time.

4. Combine both observations → define DP over:
   - current day index
   - accumulated participants so far

5. Cap accumulated participants at `target` → once you reach or exceed target, extra participants are equivalent. This keeps the state space bounded.

6. At each day, there are only two meaningful actions:
   - skip the day and move to `i + 1`
   - take the day, pay `cost[i]`, add `value[i]`, and jump to `nextDay`

7. This immediately suggests top-down memoization or bottom-up tabulation. Both are valid; top-down maps cleanly to the decision tree, while bottom-up is often easier to reason about for complexity and implementation discipline.

## 🧩 Algorithm Walkthrough
1. **Define the DP pattern: 2D Dynamic Programming with state compression on reward.**  
   Let `dp[i][p]` be the minimum cost needed to reach at least `target` participants starting from day `i`, given that `p` participants have already been accumulated. We cap `p` at `target`.

2. **Establish terminal conditions.**  
   If `p == target`, the remaining cost is `0`: the objective is already satisfied.  
   If `i >= N` and `p < target`, return infinity: no days remain, so this path is infeasible.  
   This preserves the invariant that every state evaluates to either a valid minimum cost or an infeasible sentinel.

3. **Compute the skip transition.**  
   Skipping day `i` leads to state `(i + 1, p)` with no added cost.  
   This is always legal and ensures the DP explores schedules that leave slack in the calendar.

4. **Compute the take transition.**  
   Taking day `i` adds `cost[i]`, increases participants to `min(target, p + value[i])`, and jumps to `nextDay = i + cooldown[i] + 1`.  
   This enforces the cooldown exactly once at transition time, which is cleaner than carrying a separate “blocked until” state.

5. **Take the minimum of both transitions.**  
   `dp[i][p] = min(skip, take)`  
   Correctness follows from optimal substructure: once the action at day `i` is fixed, the remaining problem is an identical subproblem on a later day with a new participant total.

6. **Return the answer from `dp[0][0]`.**  
   If it is infinity, return `-1`; otherwise return that minimum cost.  
   The abstraction is right because the problem is a knapsack-style objective constrained by scheduling jumps, and the DP state captures exactly the information needed for future decisions—no less, no more.

## 📊 Worked Example
Use `value = [6,4,7,3]`, `cost = [5,2,6,2]`, `cooldown = [1,0,2,0]`, `target = 10`.

Let `f(i, p)` be min cost from day `i` with `p` participants already collected.

| State | Skip | Take | Result |
|---|---:|---:|---:|
| `f(3,0)` | `∞` | `2 + f(4,3)=∞` | `∞` |
| `f(2,0)` | `∞` | `6 + f(5,7)=∞` | `∞` |
| `f(1,0)` | `∞` | `2 + f(2,4)` | depends |
| `f(2,4)` | `f(3,4)` | `6 + f(5,10)=6` | `6` |
| `f(1,0)` | `∞` | `2 + 6 = 8` | `8` |
| `f(0,0)` | `f(1,0)=8` | `5 + f(2,6)` | min |
| `f(2,6)` | `f(3,6)` | `6 + f(5,10)=6` | `6` |
| `f(0,0)` | `8` | `5 + 6 = 11` | `8` |

Optimal answer: `8`, achieved by taking days `1` and `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(N * target)` states, with `O(1)` work per state, so total time is `O(N * target)`. Under the given constraints, that is at most about `200 * 5000 = 10^6` state evaluations, which is trivial. At `10^9` states, this approach would no longer be operationally acceptable without stronger pruning or a different formulation.

### Space Complexity
`O(N * target)` for the memo table or bottom-up DP table. The dominant structure is the 2D state cache over day index and capped participant count. Space can be reduced in some DP problems, but not cleanly here because transitions jump to arbitrary future days rather than only the next row.

## 💡 Key Takeaways
- If a problem says “maximize/minimize under a target threshold” and values beyond the threshold are equivalent, that is a strong signal for capped knapsack-style DP.
- If choosing an item makes some future positions unavailable, think “DP over index with jump transitions,” not greedy interval picking.
- The cooldown jump is `i + cooldown[i] + 1`; forgetting the `+1` is the most common off-by-one bug.
- Cap participant totals with `min(target, current + value[i])`; otherwise you waste states and can blow up the DP range.
- In production scheduling systems, the winning pattern is often to encode downstream unavailability directly in the state transition, rather than carrying implicit side effects through ad hoc control flow.

## 🚀 Variations & Further Practice
- Add a hard budget limit and maximize participants instead of minimizing cost; this flips the optimization axis and creates a dual knapsack formulation.
- Allow at most `K` workshops in total; the extra cardinality dimension turns a 2D DP into 3D and forces explicit trade-offs between value density and timing.
- Generalize cooldown into resource-specific recovery windows across multiple workshop types; this moves the problem toward weighted interval scheduling with multidimensional state and much harder pruning decisions.