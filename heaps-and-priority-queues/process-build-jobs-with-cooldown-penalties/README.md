# Process Build Jobs with Cooldown Penalties

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heaps, priority-queue, greedy, scheduling, sorting

---

## 🗂 Problem Overview
Given `n` build jobs, each job has a release time, processing duration, and linear waiting penalty rate. A single machine runs at most one job at a time, non-preemptively. If job `i` starts at time `s`, it contributes `(s - availableTime[i]) * penalty[i]` to the objective. Compute the minimum total penalty over all feasible schedules. The challenge is that jobs arrive over time, idling is allowed, and `n` is large enough that any repeated full scan or brute-force ordering is infeasible.

## 🌍 Engineering Impact
This pattern shows up in CI/CD schedulers, batch compilers, warehouse job dispatchers, GPU inference queues, and storage compaction pipelines where work arrives over time and delay has unequal cost. At small scale, FIFO or “highest priority first” looks acceptable; at fleet scale, it creates pathological tail costs and poor business alignment because long low-value work can block short high-penalty work. The right abstraction is not just queueing but release-aware scheduling with dynamic candidate selection. That enables predictable cost control, better SLO adherence, and schedulers that remain efficient under hundreds of thousands of pending tasks.

## 🔍 Problem Statement
You are given three arrays of length `n`:

- `availableTime[i]`: when job `i` becomes eligible to run
- `duration[i]`: how long job `i` occupies the machine
- `penalty[i]`: linear cost per unit of waiting before it starts

Only one job may run at a time, and once started, a job must finish before another begins. If job `i` starts at time `s`, its contribution is:

`(s - availableTime[i]) * penalty[i]`

Return the minimum possible total penalty as a signed 64-bit integer.

Constraints:

- `1 <= n <= 2 * 10^5`
- `0 <= availableTime[i] <= 10^9`
- `1 <= duration[i], penalty[i] <= 10^6`

Examples:

- `availableTime = [0,1,2], duration = [3,1,2], penalty = [4,100,2]`
- `availableTime = [0,0,5,5], duration = [4,2,3,1], penalty = [3,10,2,20]`

The key constraint is the combination of release times and large `n`: we need an `O(n log n)` scheduling strategy, not local rescans or DP over time.

## 🪜 How to Solve This
1. Start from the objective: total penalty is the sum of each job’s waiting time times its penalty rate. Once a set of jobs is already available, delaying all of them by `Δ` increases cost by `Δ * sum(penalty of waiting jobs)`.

2. That immediately suggests a local ordering rule: among currently available jobs, if you swap two adjacent jobs `i` and `j`, the better order is determined by which one causes less weighted waiting for the other. The pairwise comparison gives Smith’s rule: run `i` before `j` when `duration[i] / penalty[i] < duration[j] / penalty[j]`, equivalently `duration[i] * penalty[j] < duration[j] * penalty[i]`.

3. Release times complicate this because not all jobs are available yet. So sort jobs by `availableTime`, sweep time forward, and maintain a priority queue of currently available jobs ordered by that ratio.

4. At each decision point, either:
   - run the best available job, or
   - if nothing is available, jump time to the next release.

5. Why is idling while work is available unnecessary here? Because all waiting penalties are nonnegative and accrue linearly. If some job is available, idling only increases total cost for every waiting available job and never reveals a better decision that could offset that increase.

## 🧩 Algorithm Walkthrough
1. **Sort by release time.**  
   Build tuples `(availableTime, duration, penalty)` and sort ascending by `availableTime`. This gives a chronological stream of arrivals. Invariant: every unswept job has release time at or after the current scan position.

2. **Maintain current time and arrival pointer.**  
   Let `t` be the machine clock and `i` the next job in release order. If the heap is empty, set `t = max(t, jobs[i].availableTime)` and push all jobs released at or before `t`. Invariant: the heap contains exactly the available, unfinished jobs.

3. **Use a priority queue ordered by Smith’s rule.**  
   For two available jobs `a` and `b`, prefer `a` before `b` if  
   `duration[a] * penalty[b] < duration[b] * penalty[a]`.  
   This is the classic **greedy scheduling with a priority queue** pattern: the heap gives the best next job among the currently feasible set.

4. **Pop the best job and account for its penalty.**  
   If job `j` starts at time `t`, add `(t - availableTime[j]) * penalty[j]` to the answer, then advance `t += duration[j]`. This is correct because the job’s waiting time is fully determined at start time.

5. **Repeat arrival ingestion.**  
   After finishing a job, push every newly released job with `availableTime <= t`. The invariant remains: heap = all available candidates, ordered by optimal local exchange rule.

6. **Why the greedy choice is correct.**  
   For any interval with no new arrivals, the available set is fixed. On that interval, minimizing weighted waiting is equivalent to minimizing weighted completion time up to a constant, and Smith’s rule is optimal by pairwise exchange. Since idling with available work is never beneficial, applying that rule whenever the available set is fixed yields the global optimum under release times.

## 📊 Worked Example
Take `availableTime = [0,0,5,5]`, `duration = [4,2,3,1]`, `penalty = [3,10,2,20]`.

| Step | `t` before | Newly available | Heap best order | Run | Added penalty | `t` after |
|---|---:|---|---|---|---:|---:|
| 1 | 0 | jobs 0, 1 | 1 before 0 (`2/10 < 4/3`) | 1 | `(0-0)*10 = 0` | 2 |
| 2 | 2 | none | 0 | 0 | `(2-0)*3 = 6` | 6 |
| 3 | 6 | jobs 2, 3 | 3 before 2 (`1/20 < 3/2`) | 3 | `(6-5)*20 = 20` | 7 |
| 4 | 7 | none | 2 | 2 | `(7-5)*2 = 4` | 10 |

Total = `0 + 6 + 20 + 4 = 30`.

The trace shows the two core mechanics: release-time sweep and heap selection by `duration / penalty`, implemented via cross-multiplication to avoid floating-point error.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)`. Sorting by `availableTime` costs `O(n log n)`, and each job is pushed to and popped from the heap once, each in `O(log n)`. This is the right regime for `n = 2 * 10^5`; anything with repeated scans degrades toward quadratic behavior and fails well before million-scale workloads.

### Space Complexity
`O(n)` for the sorted job list and the priority queue holding available jobs. The heap owns the dynamic working set. You could sort in place to reduce auxiliary storage, but asymptotically the active frontier still requires linear worst-case space.

## 💡 Key Takeaways
- If jobs have release times plus a linear waiting cost, think “event sweep + best-next-job heap,” not “simulate every time unit” or “sort once globally.”
- If the objective is weighted waiting or weighted completion, look for a pairwise exchange argument; Smith’s rule is the usual signal.
- Do not compare `duration / penalty` with floating point; use cross-multiplication to preserve ordering exactly.
- When the heap is empty, jump time to the next release; when the heap is non-empty, idling is a bug here, not an optimization.
- In production schedulers, the transferable insight is to separate **admission over time** from **optimal selection within the feasible frontier**.

## 🚀 Variations & Further Practice
- Allow **preemption**: the problem shifts toward policies like Highest Density First / SRPT-style hybrids, and correctness depends on whether penalty is tied to start time, flow time, or remaining work.
- Add **multiple identical machines**: the local exchange rule no longer composes cleanly, and the problem becomes significantly harder, often requiring approximation or min-cost flow formulations.
- Replace linear waiting penalty with **piecewise or deadline-based cost**: the heap key becomes state-dependent, and event handling must account for cost discontinuities rather than a single static ratio.