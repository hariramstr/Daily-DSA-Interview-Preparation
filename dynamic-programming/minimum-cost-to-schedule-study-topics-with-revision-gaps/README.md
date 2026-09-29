# Minimum Cost to Schedule Study Topics with Revision Gaps

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, state-compression, memoization

---

## 🗂 Problem Overview
You must choose exactly one topic per day across `n` days, minimizing total study cost. Each topic `j` has a per-day cost `costs[i][j]` and a cooldown `gap[j]`: after using it, it cannot be chosen again for the next `gap[j]` days. Return the minimum achievable total cost, or `-1` if no valid schedule exists. The challenge is that each decision depends on recent topic history, not just the current day.

## 🌍 Engineering Impact
This pattern shows up anywhere actions have local cost but also cooldown or reuse constraints. Examples include distributed rate-limiters with per-key backoff windows, job schedulers with resource recovery periods, ad-serving systems that enforce impression spacing, and streaming pipelines that must avoid reusing hot partitions too aggressively. At small scale, brute force can enumerate sequences; at production scale, state explodes because feasibility depends on recent history, not aggregate counts. Dynamic programming with compressed state turns an exponential search into a bounded exploration over the minimal information needed to make the next decision safely and optimally.

## 🔍 Problem Statement
Given `n` days and `m` topics, choose one topic each day. Studying topic `j` on day `i` costs `costs[i][j]`. If topic `j` is used on day `a`, it cannot be used again until at least `gap[j]` full days have passed, meaning a later use on day `b` is valid only if `b - a - 1 >= gap[j]`.

Return the minimum total cost over all valid schedules, or `-1` if no schedule fills all `n` days.

Constraints:
- `1 <= n <= 100`
- `1 <= m <= 8`
- `costs.length == n`
- `costs[i].length == m`
- `1 <= costs[i][j] <= 10^4`
- `0 <= gap[j] <= 7`

Examples:
- `n = 4, costs = [[3,8],[5,2],[6,4],[1,7]], gap = [1,0]` → `10`
- `n = 3, costs = [[4,1],[2,3],[5,6]], gap = [2,2]` → `-1`

The algorithmic driver is that `m` and `gap[j]` are small, but `n` is much larger, which strongly suggests DP over compressed recent-history state.

## 🪜 How to Solve This
1. Read the constraint carefully → the legality of choosing topic `j` today depends only on how many days remain before `j` becomes available again.
2. That means full schedule history is unnecessary → we only need a compact state describing each topic’s remaining cooldown.
3. Since `m <= 8` and each `gap[j] <= 7`, the per-topic cooldown value is tiny → this is a strong signal for state compression.
4. Define a DP state for `day` plus the cooldown vector of all topics. From that state, try every topic whose cooldown is zero.
5. After choosing a topic:
   - all positive cooldowns decrease by one for the next day,
   - the chosen topic is reset to its full cooldown `gap[j]`.
6. Memoize the minimum cost from each `(day, state)` pair. This avoids recomputing the same future scheduling subproblem reached through different prefixes.
7. If no topic is available on some day, that branch is impossible.
8. The answer is the minimum cost from day `0` with all cooldowns initially zero.

The key realization: this is not “pick cheapest each day”; it is “optimize over future availability,” which is classic dynamic programming.

## 🧩 Algorithm Walkthrough
1. **Model the problem as Dynamic Programming with state compression.**  
   Let `state` encode, for every topic, how many more days it remains blocked. This is the minimal sufficient history: if two prefixes produce the same day and same cooldown vector, their future options and optimal remaining cost are identical.

2. **Define the recursive subproblem.**  
   `dp(day, state)` = minimum cost to schedule days `day...n-1` given current cooldowns.  
   Base case: if `day == n`, return `0` because all days are scheduled.

3. **Enumerate valid choices for the current day.**  
   For each topic `j`, it is selectable iff its cooldown in `state` is `0`. If no topic is selectable, return infinity/impossible for this state.

4. **Transition to the next state.**  
   Build `nextState` by decrementing every positive cooldown by one. Then set the chosen topic’s cooldown to `gap[j]`, since it becomes unavailable for the next `gap[j]` days.  
   This preserves the invariant: `state[k]` always means “days remaining before topic `k` can be chosen again.”

5. **Accumulate cost and take the minimum.**  
   Candidate value = `costs[day][j] + dp(day + 1, nextState)`. Minimize across all valid `j`.

6. **Memoize aggressively.**  
   Cache results by `(day, encodedState)`. Since `n` is up to `100`, but the compressed state space is bounded by roughly `∏(gap[j] + 1)`, memoization converts repeated subtree exploration into table lookup.

7. **Return `-1` if the root state is impossible.**  
   If the computed minimum is infinity, no valid schedule exists.

This is the right abstraction because the problem is fundamentally a finite-state decision process over short memory, not a shortest-path over full sequences.

## 📊 Worked Example
Use `n = 4`, `costs = [[3,8],[5,2],[6,4],[1,7]]`, `gap = [1,0]`.

State format: `(cooldown_topic0, cooldown_topic1)`

| Day | State In | Valid Topics | Chosen | Cost | State Out |
|---|---|---|---|---:|---|
| 0 | `(0,0)` | `0,1` | `0` | 3 | `(1,0)` |
| 1 | `(1,0)` | `1` | `1` | 2 | `(0,0)` |
| 2 | `(0,0)` | `0,1` | `1` | 4 | `(0,0)` |
| 3 | `(0,0)` | `0,1` | `0` | 1 | `(1,0)` |

Total = `3 + 2 + 4 + 1 = 10`.

Why is this valid? Topic `0` has gap `1`, so after using it on day `0`, it is blocked on day `1` and becomes available again on day `2`. Topic `1` has gap `0`, so it can be repeated immediately. The DP explores both choices at each day, but memoization collapses repeated visits to the same `(day, state)` pair.

## ⏱ Complexity Analysis
### Time Complexity
Let `S = ∏(gap[j] + 1)` be the number of compressed cooldown states. Each DP state tries up to `m` topics, so time is `O(n * S * m)`. With `m <= 8` and `gap[j] <= 7`, this is tractable. At `10^6` or `10^9` states it would be unacceptable, but the bounded state space keeps it practical here.

### Space Complexity
Space is `O(n * S)` for memoization, or `O(S)` if implemented bottom-up with rolling layers by day. The dominant structure is the cache keyed by encoded cooldown state. Reducing space usually trades off implementation simplicity and sometimes debuggability.

## 💡 Key Takeaways
- If feasibility depends on a short sliding history rather than the full prefix, look for DP with compressed state instead of sequence brute force.
- Small `m` plus tiny bounded per-item state (`gap <= 7`) is a strong signal that exponential-in-`m` state compression may still be efficient.
- The cooldown update is easy to get wrong: decrement existing positive cooldowns for the next day, then set the chosen topic to `gap[j]`.
- The validity rule is off-by-one sensitive: “at least `gap[j]` full days passed” translates to blocking the next `gap[j]` days after use.
- In production systems, this is the same design move as replacing unbounded event history with a compact, sufficient state representation that preserves future correctness.

## 🚀 Variations & Further Practice
- Add a reward or penalty for switching topics between adjacent days; now the state must capture both cooldowns and the previously chosen topic.
- Require studying exactly `k[j]` times per topic across the schedule; this adds count-tracking to the DP and expands the state space significantly.
- Generalize to choosing multiple topics per day with shared resource limits; the twist is combining cooldown constraints with subset selection or knapsack-style transitions.