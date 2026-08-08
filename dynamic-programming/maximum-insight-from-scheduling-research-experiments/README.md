# Maximum Insight from Scheduling Research Experiments

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, knapsack, scheduling

---

## 🗂 Problem Overview
Given `n` experiments, each with a duration, deadline, and insight value, choose a subset and an execution order that maximizes total insight. A chosen experiment must run for exactly its duration on consecutive days and finish by its own deadline. Only one experiment can run at a time. The challenge is that feasibility depends on cumulative occupied time before each deadline, so local greedy choices by value, duration, or deadline alone are not reliable.

## 🌍 Engineering Impact
This pattern shows up anywhere work units have both value and completion deadlines under a single shared capacity budget: batch schedulers, CI/CD execution queues, GPU job packing, edge inference windows, and data pipeline backfills. In production, naive priority rules overfit to one dimension — urgency, cost, or payoff — and leave throughput on the table. Dynamic programming is what you reach for when feasibility depends on aggregate resource consumption across ordered deadlines. It enables predictable optimization under bounded horizons, which is often exactly the right trade-off for offline planning, admission control, and capacity-aware orchestration.

## 🔍 Problem Statement
You are given three arrays of length `n`:

- `duration[i]`: consecutive days required by experiment `i`
- `deadline[i]`: latest day by which experiment `i` must be completed
- `insight[i]`: value earned if experiment `i` finishes by its deadline

You may select any subset and schedule the chosen experiments in any order, but only one experiment may run on a day and experiments are non-preemptive. Return the maximum total insight achievable.

Constraints:

- `1 <= n <= 200`
- `1 <= duration[i] <= 200`
- `1 <= deadline[i] <= 2000`
- `1 <= insight[i] <= 10^6`

Examples:

- `duration = [2,1,2], deadline = [2,2,3], insight = [8,4,7]` → `12`
- `duration = [3,1,2,2], deadline = [3,4,5,6], insight = [10,3,9,8]` → `20`

The key constraint is the bounded deadline horizon (`<= 2000`), which makes pseudo-polynomial DP practical while brute-force subset scheduling is not.

## 🪜 How to Solve This
1. Start with the scheduling constraint → if a set of jobs is feasible, there exists an order by nondecreasing deadline that works. That immediately suggests sorting by deadline.
2. After sorting, the only state that matters is: “how much total time have I used so far?” If I know that, I can test whether the next experiment can finish by its deadline.
3. That turns the problem into a knapsack variant:
   - weight = `duration`
   - value = `insight`
   - capacity is not global; each item has its own latest allowable finish time
4. So define DP over time: `dp[t] = max insight achievable using exactly t days` after processing some prefix of experiments.
5. For each experiment, try taking it only if `t <= deadline[i]`. Update backward so each experiment is used at most once.
6. The reason this works is that deadline sorting converts a hard ordering problem into a prefix-feasibility problem. Once sorted, any DP state that respects time and current deadline corresponds to a valid partial schedule.

This is the standard mental move: reorder by the constraint that defines feasibility, then optimize value under that normalized order.

## 🧩 Algorithm Walkthrough
1. **Sort experiments by increasing deadline**  
   Pattern: **Dynamic Programming on ordered items / knapsack with deadlines**.  
   Why: after sorting, if a subset is feasible, scheduling chosen experiments in this order preserves deadline feasibility.  
   Invariant: when processing experiment `i`, all prior DP states represent valid schedules using only experiments with deadlines `<= deadline[i]`.

2. **Define DP state over elapsed time**  
   Let `dp[t]` be the maximum insight achievable with total occupied time exactly `t`. Initialize all states to negative infinity except `dp[0] = 0`.  
   Why: elapsed time is the resource that determines whether the next experiment can still meet its deadline.  
   Invariant: any finite `dp[t]` corresponds to some valid schedule of processed experiments finishing at day `t`.

3. **Process each experiment once**  
   For experiment `(d, ddl, val)`, iterate `t` backward from `ddl` down to `d`.  
   Transition: `dp[t] = max(dp[t], dp[t - d] + val)`.  
   Why backward: this is 0/1 knapsack; forward iteration would reuse the same experiment multiple times.  
   Invariant: after finishing this loop, each experiment contributes at most once.

4. **Enforce deadline feasibility during transition**  
   Only allow target times `t <= ddl`.  
   Why: if the experiment finishes later than its own deadline, that schedule is invalid regardless of value.  
   Invariant: every updated state remains schedulable in sorted order.

5. **Return the best value across all times**  
   The answer is `max(dp[t])` for `0 <= t <= maxDeadline`.  
   Why: the optimal schedule may leave idle capacity; exact fill is not required.

This abstraction is right because the problem is not about choosing intervals on a fixed timeline; it is about constructing a feasible prefix schedule under cumulative time constraints.

## 📊 Worked Example
Use `duration = [3,1,2,2]`, `deadline = [3,4,5,6]`, `insight = [10,3,9,8]`.

After sorting by deadline, order is unchanged.

| Step | Experiment `(d, ddl, val)` | Key DP updates |
|---|---|---|
| 0 | start | `dp[0]=0` |
| 1 | `(3,3,10)` | `dp[3]=10` |
| 2 | `(1,4,3)` | `dp[1]=3`, `dp[4]=13` from `dp[3]+3` |
| 3 | `(2,5,9)` | `dp[2]=9`, `dp[3]=12` from `dp[1]+9`, `dp[5]=19` from `dp[3]+9` |
| 4 | `(2,6,8)` | `dp[2]=9`, `dp[4]=17`, `dp[5]=20` from `dp[3]+8`, `dp[6]=21` from `dp[4]+8` if feasible state exists |

The best reachable value is `20`, achieved by taking experiments 2, 3, and 4 with total time 5. Their sorted execution finishes on days 1, 3, and 5, all within deadlines 4, 5, and 6.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * D)`, where `D = max(deadline) <= 2000`. We process each experiment once and scan a bounded time horizon for transitions. With current constraints this is at most about `4 * 10^5` updates, trivial in practice. At `10^6` or `10^9` horizon scale, this pseudo-polynomial approach stops being viable.

### Space Complexity
`O(D)` using a 1D DP array over time. The space is owned entirely by the deadline-bounded state table. You could use 2D DP for easier reconstruction, but that raises memory to `O(nD)` with little benefit unless schedule recovery is required.

## 💡 Key Takeaways
- If jobs can be reordered and feasibility depends on cumulative processing time before each deadline, think deadline-sorted DP rather than interval scheduling.
- A bounded deadline horizon is the signal that a pseudo-polynomial knapsack-style state over time may be the intended solution.
- Iterate time backward; forward iteration silently turns the problem into unbounded reuse of the same experiment.
- Be precise about finish time semantics: if an experiment takes `d` days and ends at time `t`, it is valid only when `t <= deadline[i]`.
- In production planning systems, normalizing by the constraint that defines feasibility often converts a combinatorial scheduling problem into a tractable resource-allocation DP.

## 🚀 Variations & Further Practice
- **Recover the actual schedule, not just the value**: store predecessor choices or use 2D DP; the harder part is reconstructing a valid ordered subset without breaking deadline feasibility.
- **Multiple identical machines**: capacity becomes parallel rather than serial, pushing the problem toward much harder scheduling formulations and often NP-hard generalizations.
- **Allow penalties for lateness instead of hard deadlines**: the state no longer has a simple feasibility cutoff, and transitions must model trade-offs between value and tardiness cost.