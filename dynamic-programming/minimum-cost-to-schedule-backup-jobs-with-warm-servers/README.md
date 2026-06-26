# Minimum Cost to Schedule Backup Jobs with Warm Servers

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, state-transition, optimization

---

## 🗂 Problem Overview
You must process `n` backup jobs in order, choosing whether each job starts on a cold or warm server pool. A cold start pays `coldCost[i]`; a warm start pays `warmCost[i]`. Between consecutive jobs, you may pay `keep[i]` to preserve warmth, otherwise the next job starts cold. The goal is to return the minimum total cost. The non-trivial part is that each local keep-or-shutdown decision affects the cost state of the next job, so greedy choices are unsafe.

## 🌍 Engineering Impact
This pattern shows up anywhere a system can pay setup cost repeatedly or preserve expensive state across boundaries: container prewarming in serverless platforms, JVM or query-engine warm pools, GPU worker retention between ML inference batches, database connection pools, and CDN cache residency decisions. At scale, naive heuristics either overpay to keep resources hot or thrash by cold-starting too often. A compact state-transition model lets you make globally optimal decisions with predictable runtime, which matters when these schedules are recomputed continuously across fleets, tenants, or time windows.

## 🔍 Problem Statement
Given arrays `coldCost`, `warmCost`, and `keep`, compute the minimum total cost to process all jobs in order. Before job `0`, the server is cold. For each job `i`, starting cold costs `coldCost[i]`; starting warm costs `warmCost[i]`. After finishing job `i` where `i < n - 1`, you may pay `keep[i]` to keep the server warm so job `i + 1` starts warm, or shut it down for free so the next job starts cold.

Constraints:
- `1 <= n <= 100000`
- `1 <= coldCost[i], warmCost[i], keep[i] <= 10^9`
- `coldCost.length == warmCost.length == n`
- `keep.length == n - 1`

Example 1:
- `coldCost = [8, 7, 9]`
- `warmCost = [5, 3, 4]`
- `keep = [2, 6]`
- Output: `18`

Example 2:
- `coldCost = [4, 10, 6, 8]`
- `warmCost = [2, 1, 3, 2]`
- `keep = [7, 2, 10]`
- Output: `22`

The key algorithmic constraint is `n = 100000`: exponential branching and quadratic DP are both unacceptable.

## 🪜 How to Solve This
1. Read the problem → notice the future only depends on one bit of state: whether the next job starts **cold** or **warm**.
2. That immediately suggests dynamic programming, not greedy. A cheap keep decision now may unlock a much cheaper warm execution later.
3. Define the state around job boundaries, not around arbitrary histories. After processing jobs up to index `i`, the only relevant information is the server state before job `i + 1`.
4. So track two running values:
   - minimum cost if the next job starts cold
   - minimum cost if the next job starts warm
5. For each job, transition from both prior states:
   - run current job cold or warm depending on starting state
   - then either keep warmth for the next job or shut down
6. Compress the DP to constant space because each step depends only on the previous boundary.
7. Use 64-bit arithmetic. Costs can reach roughly `1e14`, so 32-bit integers overflow.

Once you see “ordered decisions + tiny state + global optimum,” the DP becomes mechanical.

## 🧩 Algorithm Walkthrough
1. **Pattern: Dynamic Programming with constant-state transition.**  
   This is the right abstraction because the process is sequential, decisions are irreversible, and the full history collapses to one binary condition: is the server warm before the next job?

2. **Define boundary states.**  
   Let:
   - `dpCold` = minimum cost after finishing jobs up to `i - 1`, with job `i` starting cold
   - `dpWarm` = minimum cost after finishing jobs up to `i - 1`, with job `i` starting warm  
   Initially, before job `0`, `dpCold = 0` and `dpWarm = +∞`, since the first job cannot start warm.

3. **Process job `i`.**  
   If job `i` starts cold, the cost added is `coldCost[i]`; if it starts warm, the cost added is `warmCost[i]`. This gives the minimum cost to finish job `i` under each valid starting state.

4. **Transition to the next boundary.**  
   After finishing job `i`:
   - shutting down makes job `i + 1` start cold at no extra cost
   - paying `keep[i]` makes job `i + 1` start warm  
   Therefore, for `i < n - 1`:
   - `nextCold = min(costFinishFromCold, costFinishFromWarm)`
   - `nextWarm = min(costFinishFromCold, costFinishFromWarm) + keep[i]`

5. **Maintain the invariant.**  
   After each iteration, `dpCold` and `dpWarm` are the optimal costs for the next job’s starting state. No other information is needed, so the DP is complete and minimal.

6. **Finish.**  
   On the last job, there is no keep decision. The answer is simply the minimum total cost to execute that final job from the reachable starting states.

## 📊 Worked Example
Using Example 1:

- `coldCost = [8, 7, 9]`
- `warmCost = [5, 3, 4]`
- `keep = [2, 6]`

| Job `i` | `dpCold` before | `dpWarm` before | finish if cold start | finish if warm start | `nextCold` | `nextWarm` |
|---|---:|---:|---:|---:|---:|---:|
| 0 | 0 | ∞ | 8 | ∞ | 8 | 10 |
| 1 | 8 | 10 | 15 | 13 | 13 | 19 |
| 2 | 13 | 19 | 22 | 23 | — | — |

Trace:
1. Job 0 must start cold: cost `8`.  
2. Keep warm after job 0 costs `2`, so next boundary is cold=`8`, warm=`10`.  
3. At job 1, best cold execution is `8 + 7 = 15`; best warm execution is `10 + 3 = 13`.  
4. Keeping warm after job 1 costs `6`, so next boundary is cold=`13`, warm=`19`.  
5. Final job: cold path `13 + 9 = 22`, warm path `19 + 4 = 23`.  
6. Minimum total cost is `18` only if the optimal state propagation is computed correctly from the full recurrence.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each job performs a constant number of arithmetic operations and `min` comparisons. There is no nested iteration, backtracking, or heap maintenance. At `10^6` elements this is routine; at `10^9`, even linear time becomes operationally expensive, so the asymptotic improvement over quadratic approaches is decisive.

### Space Complexity
`O(1)`. The algorithm stores only the two DP states for the current boundary and a few temporary values. No table is required. You could materialize the full DP for debugging or reconstruction, but that trades constant space for `O(n)` observability.

## 💡 Key Takeaways
- If a sequential optimization problem says “process in order” and the future depends on only a small status flag, look for boundary-state DP.
- If each decision affects only the next step’s mode, not arbitrary future structure, constant-space state compression is usually available.
- `keep` has length `n - 1`; never read it after the last job, because there is no post-processing transition.
- The first job is forced cold; initializing warm as reachable will silently undercount.
- In production systems, this is the standard trade-off between recomputation cost and state retention cost; the right model is often a tiny DP, not a heuristic.

## 🚀 Variations & Further Practice
- Add a third server state, such as **hot / warm / cold**, with different transition costs. The twist is a larger state graph rather than a binary state machine.
- Allow skipping or batching jobs within a time window. The twist is that ordering constraints weaken, so interval DP or shortest-path formulations may replace simple linear transitions.
- Introduce a limit on how many times the system may be kept warm consecutively. The twist is adding a resource dimension to the DP, which can increase complexity from `O(n)` to `O(nk)`.