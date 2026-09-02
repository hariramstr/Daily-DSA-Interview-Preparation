# Minimum Processor Count for Deadline-Sorted Builds

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Priority Queue

---

## 🗂 Problem Overview
Given `buildTimes[i]` and `deadlines[i]` for `n` build jobs, determine the minimum number of identical processors needed so every job finishes by its deadline. Jobs must be considered in input order, but each job can be assigned to any processor. Processors run one job at a time with no preemption. The challenge is that feasibility depends on both deadline pressure and processor reuse, so brute-force assignment is intractable at `n = 200000`.

## 🌍 Engineering Impact
This pattern shows up in CI/CD schedulers, distributed build farms, ETL backfill orchestration, GPU inference queues, and manufacturing-style workflow engines where tasks arrive in a fixed priority or dependency-derived order. At scale, overprovisioning wastes money, while underprovisioning causes deadline misses, SLA violations, and cascading queue growth. The important architectural idea is exploiting monotonic feasibility: instead of searching the full scheduling space, search the resource count and use a fast admissibility test. That turns a combinatorial allocation problem into something operationally predictable, explainable, and cheap enough to run continuously in capacity planners.

## 🔍 Problem Statement
You are given two arrays of equal length `n`:

- `buildTimes[i]`: execution time of job `i`
- `deadlines[i]`: latest allowed completion time of job `i`, measured from time `0`

Jobs must be processed in the given order. For each job, you may assign it to any of `k` identical processors. On a processor, jobs execute sequentially, cannot overlap, and cannot be preempted. A schedule is valid if every job completes no later than its own deadline.

Return the minimum `k` in `[1, n]` for which a valid schedule exists. If none exists, return `-1`.

Constraints:

- `1 <= n <= 200000`
- `1 <= buildTimes[i] <= 1e9`
- `1 <= deadlines[i] <= 1e18`

Examples:

- `buildTimes = [3,2,4,1]`, `deadlines = [4,5,8,6]` → `2`
- `buildTimes = [5,5,5]`, `deadlines = [4,10,15]` → `-1`

The key algorithmic driver is monotonicity: if `k` processors work, then any `k+1` processors also work.

## 🪜 How to Solve This
1. Read the problem → the output is not a schedule, but the smallest resource count. That is a strong signal for **binary search on the answer**.

2. Ask whether feasibility is monotonic → yes. If `k` processors can meet all deadlines, giving yourself more processors cannot make things worse.

3. Now reduce the problem to: “Given `k`, can I check feasibility quickly?” A naive simulation over all assignments is exponential, so we need a greedy rule.

4. For each incoming job, the best processor to try is the one that becomes free earliest. Why? Any later-free processor only delays completion further and cannot improve feasibility for the current job.

5. That suggests a **min-heap of processor availability times**. For each job, pop the earliest available processor, schedule the job there, compute completion time, and fail immediately if it exceeds the deadline.

6. If all jobs fit, `k` is feasible. Binary search the smallest feasible `k`.

7. One more edge case: if any single job has `buildTimes[i] > deadlines[i]`, even an isolated processor cannot save it, so return `-1`.

## 🧩 Algorithm Walkthrough
1. **Early impossibility check**  
   Scan all jobs once. If `buildTimes[i] > deadlines[i]` for any `i`, return `-1`. This is necessary because no scheduling strategy can finish that job by its own deadline, even on an idle processor.

2. **Exploit the monotonic predicate**  
   Define `feasible(k)` = whether all jobs can be assigned to `k` processors while meeting deadlines. This predicate is monotone: once true, it stays true for larger `k`. That makes **Binary Search on Answer** the correct top-level pattern.

3. **Model processor state with a min-heap**  
   For a fixed `k`, initialize a priority queue containing `k` zeros, one per processor, representing “next free time.” The heap invariant is: it always contains the current finish time of every processor, and the minimum is the earliest processor available.

4. **Greedy assignment for each job in input order**  
   For job `i`, pop the smallest available time `t`. Schedule the job there, so its completion time is `t + buildTimes[i]`. If this exceeds `deadlines[i]`, `feasible(k)` is false. Otherwise, push `t + buildTimes[i]` back into the heap.  
   This greedy step is correct because assigning the job to any processor with a later availability time would only increase completion time. The earliest-free processor dominates all other choices for preserving feasibility.

5. **Binary search the minimum feasible processor count**  
   Search `k` in `[1, n]`. On success, move left; on failure, move right. The first feasible `k` is the answer.

This combines **Binary Search + Greedy + Priority Queue**, which is the right abstraction because the search space is monotone and the local optimal choice is globally safe for feasibility testing.

## 📊 Worked Example
Use `buildTimes = [3,2,4,1]`, `deadlines = [4,5,8,6]`.

Check `k = 2`:

| Job | Duration | Deadline | Heap before | Picked free time | Completion | Heap after |
|---|---:|---:|---|---:|---:|---|
| 0 | 3 | 4 | `[0,0]` | 0 | 3 | `[0,3]` |
| 1 | 2 | 5 | `[0,3]` | 0 | 2 | `[2,3]` |
| 2 | 4 | 8 | `[2,3]` | 2 | 6 | `[3,6]` |
| 3 | 1 | 6 | `[3,6]` | 3 | 4 | `[4,6]` |

All completions are within deadlines, so `k = 2` is feasible.

Check `k = 1`:

- Job 0 completes at `3` ≤ `4`
- Job 1 completes at `5` ≤ `5`
- Job 2 completes at `9` > `8` → fail

Binary search therefore returns `2`.

## ⏱ Complexity Analysis
### Time Complexity
Feasibility for a fixed `k` processes `n` jobs, each doing one heap pop and push, so it costs `O(n log k)`. Binary search over `k ∈ [1, n]` adds another `log n`, giving `O(n log k log n)` worst-case `O(n log^2 n)`. This remains practical for `2e5`; it does not scale to `1e9` items without a different model.

### Space Complexity
The heap stores one availability timestamp per processor, so auxiliary space is `O(k)`, worst-case `O(n)`. This is already asymptotically tight for explicit simulation. Reducing it would require a more compressed representation of processor states, which is not generally possible here without losing exactness.

## 💡 Key Takeaways
- If the question asks for the **minimum resource count** and feasibility only gets easier as resources increase, think **binary search on the answer**.
- If each step needs the “best currently available machine/server/slot,” a **min-heap of next-available times** is usually the right primitive.
- Initialize the heap with exactly `k` zeros; forgetting idle processors and growing the heap dynamically changes the scheduling model.
- Use 64-bit or wider arithmetic for completion times and deadlines; `buildTimes` can sum far beyond 32-bit range.
- In production schedulers, monotone feasibility checks are a powerful way to convert expensive capacity planning into a deterministic control loop.

## 🚀 Variations & Further Practice
- Allow arbitrary job reordering before assignment. The twist is that ordering becomes part of the optimization, pushing the problem toward deadline-ordering rules and more complex schedulability conditions.
- Add processor heterogeneity, where each processor has a different speed. The heap state must encode effective completion times, and monotonicity may still hold while the greedy proof gets harder.
- Introduce precedence constraints between jobs. Now feasibility depends on both machine availability and DAG readiness, connecting this pattern to parallel task scheduling on dependency graphs.