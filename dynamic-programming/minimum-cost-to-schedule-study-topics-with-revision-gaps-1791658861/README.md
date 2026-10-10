# Minimum Cost to Schedule Study Topics With Revision Gaps

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, state-compression, memoization

---

## 🗂 Problem Overview
Given `n` days and `m` topics, choose for each day whether to skip or study exactly one topic. Studying topic `j` on day `i` incurs `cost[i][j]`, and topic `j` must be studied exactly `need[j]` times overall. Repeating a topic is constrained by a per-topic cooldown: if it was last studied on day `p`, the next valid day is any `d` with `d - p > gap[j]`. Return the minimum total cost, or `-1` if no feasible schedule exists.

## 🌍 Engineering Impact
This pattern shows up in schedulers that must satisfy both quota and cooldown constraints: distributed rate-limiters with per-key recovery windows, ad-delivery pacing with impression spacing, job orchestration with resource-specific backoff, and spaced-repetition or notification systems with retention intervals. The hard part is not local choice but global feasibility under time-indexed constraints. Greedy placement fails because a cheap assignment today can block mandatory work later. State-compressed dynamic programming gives a bounded, exact optimizer for small control dimensions, which is often the right trade-off in policy engines, planners, and admission controllers where correctness matters more than asymptotic elegance.

## 🔍 Problem Statement
You have `n` days (`1 <= n <= 30`) and `m` topics (`1 <= m <= 5`). On each day, you may either skip or study one topic. Studying topic `j` on day `i` costs `cost[i][j]`, where `1 <= cost[i][j] <= 10^4`.

Each topic has:
- `need[j]`: exact number of required study sessions, with `0 <= need[j] <= n`
- `gap[j]`: minimum revision gap, with `0 <= gap[j] <= n`

If topic `j` was last studied on day `p`, it can next be studied only on day `d` such that `d - p > gap[j]`. The total required sessions satisfy `sum(need) <= n`.

Return the minimum total cost to complete all required sessions by day `n - 1`, or `-1` if impossible.

Examples:

- `n=5, m=2, need=[2,1], gap=[1,0]` → `5`
- `n=4, m=2, need=[2,2], gap=[2,1]` → `-1`

The key constraint is small `m` but time-dependent per-topic cooldowns, which rules out simple greedy or one-dimensional DP.

## 🪜 How to Solve This
1. Read the problem → two decisions happen together: **when** to study and **which topic** to assign. That usually means a day-by-day DP, because feasibility depends on prior choices.

2. Notice what matters from the past:
   - how many times each topic has already been studied
   - when each topic was last studied, or at least whether it is currently available again

3. Since `m <= 5`, the topic dimension is tiny. That is the signal to use **state compression**: encode completed counts per topic into a compact key instead of tracking a large schedule history.

4. Process days left to right. On each day, either:
   - skip, or
   - study a topic whose remaining quota is positive and whose cooldown is satisfied

5. The cooldown depends on the last study day, so the DP state must also carry a compact representation of per-topic recency. With only 30 days and 5 topics, memoizing `(day, counts, lastDays)` is tractable.

6. From there the recurrence is straightforward: choose the minimum cost among all valid actions. If all paths fail, mark the state impossible.

This is classic **memoized dynamic programming over compressed state**: small control surface, expensive combinatorics, exact optimization.

## 🧩 Algorithm Walkthrough
1. **Define the DP state.**  
   Use `dp(day, doneCounts, lastDays)` = minimum additional cost from `day` to `n - 1`, given:
   - `doneCounts[j]`: how many times topic `j` has already been studied
   - `lastDays[j]`: the most recent day topic `j` was studied, or a sentinel like `-INF` if never studied  
   This is correct because future legality and remaining work depend only on these values, not on the full schedule history.

2. **Compress the count vector.**  
   Encode `doneCounts` in mixed radix using bases `need[j] + 1`. This makes memoization efficient and bounded. The invariant is that each encoded state uniquely represents one feasible progress vector.

3. **Normalize recency information.**  
   For cooldown checks, only relative distance from the current day matters. You can store exact `lastDays`, or better, clamp them into “age since last study” buckets up to `gap[j] + 1`. Anything older than that is equivalent because the topic is already available. This reduces state cardinality without changing correctness.

4. **Transition on each day.**  
   From a state, try:
   - **Skip**: move to `day + 1`, update ages/recencies accordingly.
   - **Study topic `j`** if `doneCounts[j] < need[j]` and cooldown is satisfied. Pay `cost[day][j]`, increment `doneCounts[j]`, and reset topic `j`’s recency.

5. **Prune impossible branches early.**  
   If remaining days are fewer than remaining required sessions, return impossible immediately. This invariant prevents exploring states that cannot finish even with perfect availability.

6. **Base case.**  
   At `day == n`, return `0` only if every `doneCounts[j] == need[j]`; otherwise return impossible.

7. **Memoize results.**  
   This is a **Dynamic Programming + Memoization + State Compression** solution. It is the right abstraction because the branching factor is small, the horizon is short, and overlapping subproblems are substantial.

## 📊 Worked Example
Take `n=5`, `m=2`, `need=[2,1]`, `gap=[1,0]`.

Let state be `(day, done0, done1, last0, last1)`.

| Day | Action | Valid? | New State | Cost |
|---|---|---:|---|---:|
| 0 | Study 0 | Yes | `(1,1,0,0,-INF)` | 3 |
| 1 | Study 0 | No | gap for topic 0 requires `1 - 0 > 1` | — |
| 1 | Skip | Yes | `(2,1,0,0,-INF)` | 0 |
| 2 | Study 1 | Yes | `(3,1,1,0,2)` | 1 |
| 3 | Study 0 | Yes | `(4,2,1,3,2)` | 6 |
| 4 | End | Complete | total = 10 | |

Better branch:

1. Day 0 skip  
2. Day 1 study topic 0, cost `2`  
3. Day 2 study topic 1, cost `1`  
4. Day 3 skip  
5. Day 4 study topic 0, cost `2`

Topic 0 is valid on days 1 and 4 because `4 - 1 = 3 > 1`. Total cost is `5`, which is optimal.

## ⏱ Complexity Analysis
### Time Complexity
Let `S = ∏(need[j] + 1)` be the number of compressed count states, and let `R` be the number of recency-state combinations after clamping ages. The memoized search runs in `O(n * S * R * (m + 1))`, since each state tries skip plus up to `m` study actions. This is viable here because `m <= 5` and `n <= 30`; it is not a 10^6- or 10^9-scale data problem.

### Space Complexity
The memo table stores `O(n * S * R)` states. The dominant cost is the cache, not the recursion stack. Space can be reduced with iterative DP over days, but that usually makes recency-state transitions less readable and harder to implement correctly.

## 💡 Key Takeaways
- If the problem asks for an exact minimum under per-item quotas plus cooldown/history constraints, think day-indexed DP rather than greedy placement.
- Small `m` with larger time horizon is a strong signal for state compression over per-topic progress and availability.
- The legality check is `d - p > gap[j]`, not `>= gap[j]`; the next valid day is `p + gap[j] + 1`.
- “Never studied” must be represented with a sentinel that always passes the cooldown check; mishandling this silently drops valid schedules.
- In production planners, bounded exact search over a compressed policy state is often the right tool when local optimizations create downstream infeasibility.

## 🚀 Variations & Further Practice
- Add a per-day capacity `k > 1` topics instead of exactly one: same DP core, but each day becomes a subset-selection problem with combinatorial branching.
- Replace exact `need[j]` with minimum/maximum ranges and add rewards instead of costs: turns feasibility into a richer optimization with more pruning opportunities.
- Introduce topic dependencies or prerequisite ordering: now the state must track both cooldown recency and partial-order progress, which significantly increases the compressed state space.