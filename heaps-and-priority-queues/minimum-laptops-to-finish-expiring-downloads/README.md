# Minimum Laptops to Finish Expiring Downloads

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, greedy, scheduling, binary-search

---

## 🗂 Problem Overview
Given `n` download jobs, each with a release time `start[i]`, processing time `duration[i]`, and deadline `deadline[i]`, determine the minimum number of laptops needed to complete every job without preemption. A job may start only after its release time and must finish by its deadline. Return `-1` if any schedule is impossible even with unlimited laptops. The difficulty is that feasibility depends on dynamic ordering decisions, not just interval overlap, so naive sorting or overlap counting is insufficient.

## 🌍 Engineering Impact
This pattern shows up in deadline-constrained resource provisioning: CI runners executing expiring builds, media transcoders with SLA windows, warehouse robots assigned time-bounded tasks, and edge nodes pulling software artifacts before maintenance cutoffs. At scale, overprovisioning is expensive, but underprovisioning silently violates deadlines. The core architectural question is not just “can we schedule this workload?” but “what is the minimum parallel capacity that preserves feasibility?” Heap-driven simulation enables admission control, autoscaling thresholds, and capacity planning under release-time and deadline constraints where simple concurrency metrics fail.

## 🔍 Problem Statement
You are given three arrays `start`, `duration`, and `deadline`, each of length `n` (`1 <= n <= 200000`). Job `i` becomes available at time `start[i]`, requires `duration[i]` continuous minutes on exactly one laptop, and must finish no later than `deadline[i]`. A laptop runs at most one job at a time, jobs cannot be paused, and a job may start at any integer time `t` such that `t >= start[i]` and `t + duration[i] <= deadline[i]`.

Return the minimum number of laptops required to complete all jobs, or `-1` if the full set is infeasible.

Examples:

- `start = [0,1,3], duration = [3,2,2], deadline = [4,5,7]` → `2`
- `start = [0,2,2], duration = [5,1,1], deadline = [3,4,5]` → `-1`

The key constraint is `n = 200000`, which rules out exponential search, backtracking, or repeated full simulations without a tight `O(n log n)` core.

## 🪜 How to Solve This
1. Read the problem → this is not interval overlap. Jobs are flexible within windows `[start[i], deadline[i] - duration[i]]`, so the order you choose matters.

2. Ask the first hard question → for a fixed number of laptops `k`, can we decide feasibility efficiently? If yes, we can binary-search the minimum `k`.

3. For feasibility, think event-driven scheduling → process time from left to right, adding jobs when they become available.

4. Among available jobs, which one should run next? → the one with the earliest deadline. That is the classic greedy rule for deadline-sensitive scheduling.

5. But we have `k` laptops, not one → track laptop availability with a min-heap of next-free times, and track waiting jobs with another min-heap ordered by deadline.

6. At each step, assign newly free laptops to the most urgent available jobs. If some available job can no longer finish before its deadline, feasibility fails immediately.

7. Since feasibility is monotonic — if `k` laptops work, then `k+1` also works — binary search gives the minimum laptop count.

That chain gets you to `O(n log n log n)` with clean correctness boundaries.

## 🧩 Algorithm Walkthrough
1. **Pre-check impossible jobs.**  
   For each job, if `start[i] + duration[i] > deadline[i]`, return `-1` immediately. Even infinite parallelism cannot save a job whose own window is too small.

2. **Sort jobs by release time.**  
   Represent each job as `(start, deadline, duration)` and sort by `start`. This lets us sweep time forward and add jobs exactly when they become eligible.

3. **Binary search the answer `k`.**  
   Search `k` in `[1, n]`. The predicate is: “can all jobs be scheduled on `k` laptops?” This works because feasibility is monotone in the number of laptops.

4. **Feasibility check with two min-heaps.**  
   Use:
   - a min-heap of laptop free times,
   - a min-heap of available jobs keyed by earliest deadline.  
   This is the right abstraction: **greedy scheduling + priority queues**. The deadline heap enforces urgency; the free-time heap models parallel capacity.

5. **Advance the simulation by the next meaningful event.**  
   Repeatedly:
   - add all jobs with `start <= current_time` to the available-job heap,
   - release all laptops whose free time is `<= current_time`,
   - assign free laptops to available jobs in earliest-deadline order.

6. **Maintain the invariant.**  
   Every scheduled job is started at the earliest time a laptop is available after its release, and among all waiting jobs, the earliest deadline is chosen first. This preserves maximal slack for future jobs.

7. **Detect failure early.**  
   If the top available job would finish after its deadline on the earliest free laptop, the schedule is infeasible for this `k`. If all jobs are assigned, it is feasible.

## 📊 Worked Example
Use `start = [0,1,3]`, `duration = [3,2,2]`, `deadline = [4,5,7]`.

Try `k = 2`.

| Step | Time | Available Jobs (deadline, duration) | Laptop Free Times | Action |
|---|---:|---|---|---|
| 1 | 0 | `(4,3)` | `[0,0]` | Assign job 0 to laptop 1: runs `0→3` |
| 2 | 1 | `(5,2)` | `[0,3]` | Laptop 2 is free, assign job 1: runs `1→3` |
| 3 | 3 | `(7,2)` | `[3,3]` | Job 2 released; assign to either laptop: runs `3→5` |
| 4 | 5 | empty | `[3,5]` | All jobs complete |

All finish by deadline, so `k = 2` works.

Try `k = 1`:
- job 0 must run `0→3`
- then job 1 can run `3→5`
- job 2 would start at `5` and finish at `7`, but it was only available at `3`, so no ordering fits all three on one laptop.

Minimum answer: `2`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting costs `O(n log n)`. Each feasibility check pushes and pops each job from a heap at most once, so it is `O(n log k)` or `O(n log n)` in the worst case. With binary search over `k`, total time is `O(n log n log n)`, which is practical for `2e5` inputs but not for `1e9`.

### Space Complexity
`O(n)` auxiliary space. The job list, available-job heap, and laptop free-time heap dominate memory usage. Space can’t be reduced below linear without sacrificing the ability to sort and maintain pending jobs online.

## 💡 Key Takeaways
- If jobs have both release times and deadlines, and ordering choices affect feasibility, this is a scheduling problem — not a plain overlap-counting problem.
- “Minimum resources such that schedule is feasible” is a strong signal for binary search on capacity plus a greedy feasibility check.
- The immediate impossibility test is `start[i] + duration[i] > deadline[i]`; missing this wastes time on a doomed simulation.
- Be precise about finish-time semantics: feasibility requires `start_time + duration <= deadline`, not `< deadline`.
- In production systems, the transferable idea is separating **capacity search** from **feasibility simulation**, which makes autoscaling and admission-control logic easier to reason about and validate.

## 🚀 Variations & Further Practice
- Allow preemption and migration between laptops. The conceptual twist is that earliest-deadline-first becomes globally stronger, but the state model changes from machine assignment to cumulative processing capacity over time.
- Add per-laptop heterogeneity, where some jobs run faster on specific laptops. This turns a clean greedy schedule into a machine eligibility / unrelated-machines scheduling problem.
- Optimize for weighted value under a fixed laptop budget, where missing some jobs is allowed. The twist is moving from pure feasibility to profit-maximizing selection under deadline constraints.