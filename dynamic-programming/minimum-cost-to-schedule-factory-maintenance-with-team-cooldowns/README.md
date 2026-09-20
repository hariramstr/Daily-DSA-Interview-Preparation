# Minimum Cost to Schedule Factory Maintenance With Team Cooldowns

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, state-compression, array

---

## 🗂 Problem Overview
Given `n` days and a `cost[i][t]` matrix for assigning one of three teams to day `i`, compute the minimum total cost schedule. The constraint is temporal: the team used today must differ from the teams used on each of the previous two days. Return the minimum valid total cost, or `-1` if no schedule exists. The challenge is that each decision depends on the last two assignments, so greedy local choices fail.

## 🌍 Engineering Impact
This pattern shows up anywhere a scheduler must optimize cost under short-horizon exclusion rules: job placement with anti-affinity, rotating on-call responders, maintenance windows across shared operators, radio channel assignment, and streaming workers with warmup/cooldown constraints. At scale, brute-force search explodes because each step branches while validity depends on recent history. Dynamic programming with compressed state turns an exponential search tree into a linear scan with constant-state transitions. That difference is architectural, not cosmetic: it determines whether scheduling can run inline in request paths, batch planners, or control loops without becoming the bottleneck.

## 🔍 Problem Statement
You are given `cost`, an `n x 3` array where `cost[i][t]` is the cost of assigning team `t` to machine/day `i`. Exactly one team must be assigned each day.

Valid assignments must satisfy a cooldown rule:

- the team used on day `i` cannot be used on day `i + 1`
- the team used on day `i` also cannot be used on day `i + 2`

Equivalently, the team chosen for day `i` must differ from the teams chosen on the previous one and two days.

Constraints:

- `1 <= n <= 100000`
- `cost.length == n`
- `cost[i].length == 3`
- `1 <= cost[i][t] <= 1000000`

Examples:

- `cost = [[5,1,4],[2,3,6],[7,2,5],[4,6,3]]` → `8`
- `cost = [[3,2,7],[5,1,4]]` → `3`

Return the minimum total cost, or `-1` if no valid schedule exists. The key algorithmic pressure is `n = 100000`: exponential backtracking is not viable.

## 🪜 How to Solve This
1. Start from the constraint, not the costs → today’s choice is only constrained by the previous two days. That immediately suggests the full history is irrelevant.

2. If only the last two teams matter, define the state as: “minimum cost up to day `i`, ending with teams `(prev2, prev1)`.” That is the compression step.

3. On each new day, try assigning one of the three teams. A team is valid only if it differs from both stored teams. If valid, transition to the next state by shifting the window: `(prev2, prev1) -> (prev1, curr)`.

4. Because there are only 3 teams, the number of possible `(prev2, prev1)` states is tiny. In fact, after day 1 there are at most `3 * 3 = 9` states, and valid ones are even fewer.

5. That means we can scan days left to right, maintain only the current DP layer, and update the next one in constant work per state.

6. If the final state set is empty, no valid schedule exists; otherwise take the minimum accumulated cost.

## 🧩 Algorithm Walkthrough
1. **Use dynamic programming with state compression.**  
   The right abstraction is DP because the optimal prefix cost depends on a bounded context: the last two team assignments. We do not need the full schedule, only enough history to validate the next move.

2. **Handle base cases explicitly.**  
   For day `0`, the state is just “last team = `t`” with cost `cost[0][t]`. For day `1`, choose any team different from day `0`; now the state becomes `(team0, team1)` with total cost `cost[0][team0] + cost[1][team1]`.

3. **Define the rolling invariant.**  
   After processing day `i`, `dp[a][b]` stores the minimum total cost for all valid schedules ending with team `a` on day `i-1` and team `b` on day `i`. Every stored state is valid by construction.

4. **Transition to the next day.**  
   For each existing state `(a, b)`, try each team `c` in `{0,1,2}`. The move is valid iff `c != a` and `c != b`. If valid, update `next[b][c] = min(next[b][c], dp[a][b] + cost[i+1][c])`.

5. **Why this is correct.**  
   The cooldown rule depends only on the previous two days, so every legal continuation from a prefix is fully determined by `(a, b)`. Taking the minimum over all ways to reach the same `(b, c)` preserves optimality.

6. **Detect impossibility.**  
   If at any step no next state is reachable, return `-1`. With exactly 3 teams, any schedule longer than 3 days is impossible under this rule, but the DP naturally discovers that without special-casing.

## 📊 Worked Example
Use `cost = [[3,2,7],[5,1,4]]`.

Let teams be `E=0, M=1, S=2`.

| Day | State | Total Cost |
|---|---|---:|
| 0 | `(E)` | 3 |
| 0 | `(M)` | 2 |
| 0 | `(S)` | 7 |

Day 1 must use a different team than day 0:

- From `E`: `(E,M)=4`, `(E,S)=7`
- From `M`: `(M,E)=7`, `(M,S)=6`
- From `S`: `(S,E)=12`, `(S,M)=8`

Now take the minimum over all valid final states:

- min = `4`? Check carefully: `(E,M)=3+1=4`
- `(M,E)=2+5=7`
- `(M,S)=2+4=6`
- `(S,E)=7+5=12`
- `(S,M)=7+1=8`
- `(E,S)=3+4=7`

But the problem’s expected answer is `3`, which is inconsistent with the provided matrix interpretation. Under the stated definition, the correct minimum is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * 9 * 3) = O(n)`. Each day processes at most 9 compressed states, and each state tries 3 team choices. This is effectively linear even for `n = 10^6`. By contrast, brute force is `3^n`, which becomes irrelevant long before `n = 10^9`.

### Space Complexity
`O(1)`. The DP owns constant space: two `3 x 3` layers, or an equivalent fixed-size map over team-pair states. You can reduce bookkeeping further with manual unrolling, but that trades readability for negligible runtime gain.

## 💡 Key Takeaways
- If the validity of the next choice depends only on a fixed-size recent history, think dynamic programming with compressed state instead of full-history search.
- “Choose one option per index with exclusion based on the last `k` choices” is a strong signal for rolling-state DP.
- Be precise about base cases: day `0` has one previous choice, day `1` has two-day state formation, and only from day `2` onward does the full recurrence apply.
- Watch example consistency and feasibility: with 3 teams and a 2-day cooldown, day `i` must differ from both prior teams, which sharply limits schedules.
- The production lesson is to model only the minimal context that affects future legality; that is how you turn exponential planning into predictable linear-time control logic.

## 🚀 Variations & Further Practice
- Generalize from 3 teams to `m` teams with the same 2-day cooldown. The conceptual twist is that the state space becomes `O(m^2)`, so transition optimization starts to matter.
- Extend the cooldown window from 2 days to `k` days. Now the state is the last `k` assignments, pushing you toward bitmask/state-encoding trade-offs.
- Add quotas or per-team usage limits across the full schedule. This combines local-history DP with global resource constraints, often requiring higher-dimensional DP or min-cost flow formulations.