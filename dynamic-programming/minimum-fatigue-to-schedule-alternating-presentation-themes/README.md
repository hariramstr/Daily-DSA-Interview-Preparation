# Minimum Fatigue to Schedule Alternating Presentation Themes

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, state-compression, sequence-dp

---

## 🗂 Problem Overview
Given `n` presentation slots, each slot must be assigned one of three themes with a slot-specific fatigue cost. The schedule is valid only if adjacent themes differ and every length-3 window contains a repeated theme. Return the minimum total fatigue across all valid schedules, or `-1` if none exists. The challenge is that validity depends on recent choices, so local minimum-cost picks can make later states invalid or unnecessarily expensive.

## 🌍 Engineering Impact
This pattern shows up in production scheduling and sequencing systems where local choices create short-horizon constraints: ad-serving diversity controls, search ranking interleaving, streaming playlist generation, job schedulers with anti-affinity rules, and compiler or query planners that must avoid invalid local patterns. At scale, greedy heuristics fail because they optimize the current slot while destroying future feasibility. The right abstraction is a compact state machine over recent history. That enables predictable runtime, bounded memory, and a clean path from business constraints to an implementation that survives `10^5–10^6` decisions per batch.

## 🔍 Problem Statement
You are given an integer `n` and a `cost` matrix of size `n x 3`, where `cost[i][t]` is the fatigue added by assigning theme `t` to slot `i`. Theme indices are fixed: `0 = Technical`, `1 = Product`, `2 = Research`.

A schedule is valid if:

1. No two consecutive slots use the same theme.
2. In every consecutive window of length 3, the three themes are **not** all distinct.

Return the minimum total fatigue of any valid schedule, or `-1` if no valid schedule exists.

Constraints:

- `1 <= n <= 200000`
- `cost[i][t] <= 10^9`
- Answer may exceed 32-bit range

Examples:

- `n = 4`, `cost = [[3,2,7],[5,1,4],[2,6,3],[8,2,5]]` → `8`
- `n = 3`, `cost = [[1,10,10],[10,1,10],[10,10,1]]` → `-1`

The key constraint is that validity depends on the last two chosen themes, which rules out greedy selection and points directly to sequence DP.

## 🪜 How to Solve This
1. Read the rules carefully → the first rule depends on the previous theme, the second depends on the previous two themes. That immediately suggests the future only cares about a short suffix of history.

2. Ask what information is sufficient to decide whether we can place the next theme → the last two chosen themes are enough. Anything older is irrelevant once those two are fixed.

3. Model the process as sequence DP → let each state represent the last two themes used so far, and store the minimum fatigue needed to end in that state.

4. For each new slot, try appending each of the three themes → keep only transitions that satisfy:
   - new theme differs from the last theme
   - the triple `(prev2, prev1, new)` is not all distinct

5. Minimize over all valid incoming states → this gives optimal substructure because every valid prefix ending with the same last-two-themes state is interchangeable except for total cost.

6. Notice the state space is tiny → only `3 x 3 = 9` possible last-two-theme pairs, so the DP is linear in `n` despite large input size.

## 🧩 Algorithm Walkthrough
1. **Handle small prefixes explicitly.**  
   For `n = 1`, any single theme is valid, so return `min(cost[0])`. For `n = 2`, choose any pair of different themes and return the minimum sum. This is correct because the length-3 rule does not apply yet.

2. **Define the DP pattern: sequence DP with state compression.**  
   For `n >= 2`, maintain `dp[a][b]` = minimum fatigue for a valid schedule processed so far whose last two themes are `(a, b)`. Only states with `a != b` are valid because adjacent themes must differ.

3. **Initialize from the first two slots.**  
   For every pair `(a, b)` with `a != b`, set  
   `dp[a][b] = cost[0][a] + cost[1][b]`.  
   This establishes the invariant: every stored state corresponds to a valid prefix.

4. **Process each slot `i` from `2` to `n - 1`.**  
   Build a fresh `next[3][3]` initialized to infinity. For every reachable state `(a, b)`, try each candidate theme `c`.

5. **Validate transitions using both rules.**  
   Transition `(a, b) -> (b, c)` is allowed iff:
   - `b != c` for the adjacency rule
   - `(a, b, c)` is not all distinct  
   Since `a != b` already holds in every reachable state, the second condition forces `c == a`. So after the first two slots, the sequence must alternate.

6. **Update cost and preserve optimality.**  
   If valid, set  
   `next[b][c] = min(next[b][c], dp[a][b] + cost[i][c])`.  
   The invariant remains: `next` stores the minimum cost for every valid suffix state after slot `i`.

7. **Return the best terminal state.**  
   After processing all slots, take the minimum value across all reachable states. If none exist, return `-1`.

This is the right abstraction because the problem is a finite-state constrained sequence optimization problem, not a global search over all `3^n` schedules.

## 📊 Worked Example
Use `n = 4`, `cost = [[3,2,7],[5,1,4],[2,6,3],[8,2,5]]`.

Initialize on first two slots:

| State `(a,b)` | Cost |
|---|---:|
| (0,1) | 3 + 1 = 4 |
| (0,2) | 3 + 4 = 7 |
| (1,0) | 2 + 5 = 7 |
| (1,2) | 2 + 4 = 6 |
| (2,0) | 7 + 5 = 12 |
| (2,1) | 7 + 1 = 8 |

Process slot `2`:

- From `(0,1)`, `c` must differ from `1`, and triple `(0,1,c)` cannot be all distinct, so only `c = 0` works → `(1,0)` with cost `4 + 2 = 6`
- From `(1,0)`, only `c = 1` works → `(0,1)` with cost `7 + 6 = 13`
- Similar updates for other states

Best states after slot `2` include `(1,0)=6`.

Process slot `3`:

- From `(1,0)`, only `c = 1` works → `(0,1)` with cost `6 + 2 = 8`

Minimum final cost is `8`, corresponding to schedule `[0,1,0,1]`.

## ⏱ Complexity Analysis
### Time Complexity
The DP has a constant-size state space: at most 9 `(last2)` states, each trying 3 next themes per slot. Total time is `O(n)`. At `10^6` slots this is still practical; at `10^9`, linear scan cost dominates regardless of constant factors, so the bottleneck becomes raw input throughput rather than algorithm design.

### Space Complexity
Space is `O(1)` beyond the input: two `3 x 3` DP tables and a few scalars. The compressed state owns all auxiliary memory. There is no need to retain the full DP history unless reconstructing the actual schedule, which would require additional predecessor storage.

## 💡 Key Takeaways
- If validity of the next choice depends only on a fixed-size suffix of prior choices, think sequence DP with compressed state rather than backtracking or greedy.
- When constraints mention “every window of length `k`,” the natural DP state is often the last `k-1` decisions.
- Do not forget that `n = 1` and `n = 2` bypass the length-3 rule; folding them into the general transition logic can produce false `-1`s.
- Use 64-bit integers for accumulated cost; `n * cost[i][t]` can easily exceed 32-bit range.
- In production systems, many “global” sequencing constraints collapse into a tiny deterministic state machine, which is what makes linear-time optimization feasible at scale.

## 🚀 Variations & Further Practice
- Increase the number of themes from 3 to `m`: the same idea works, but the state space becomes `O(m^2)`, and transition pruning matters much more.
- Change the rule from “no length-3 window is all distinct” to arbitrary forbidden patterns over the last `k` themes: this becomes DP over automaton states or Aho-Corasick-style pattern matching.
- Add a requirement to reconstruct the optimal schedule, not just its cost: same DP core, but now you must store predecessors or rerun transitions carefully under memory constraints.