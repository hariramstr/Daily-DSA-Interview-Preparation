# Maximize Throughput with Expiring Compute Credits

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, greedy, sweep-line, event-processing

---

## 🗂 Problem Overview
You are given two time-ordered resource streams: jobs that become eligible at `releaseTime[i]`, and credit batches that appear at `grantTime[j]` and vanish after `expireTime[j]`. A job can be completed once, at a single integer time, only if enough active credits exist at that moment. The goal is to choose completion times and a subset of jobs to maximize total value. The difficulty is temporal coupling: credits expire, future jobs may be better, and greedy spending can destroy optimality.

## 🌍 Engineering Impact
This pattern shows up in quota schedulers, cloud burst-capacity allocators, ad-budget pacing, market-making inventory windows, and streaming systems with expiring processing tokens. In production, the failure mode is usually the same: teams model supply as a running total and ignore per-batch expiry, which overstates feasible capacity and produces invalid schedules. A sweep-line plus heap-based policy gives a scalable way to reconcile arrivals, deadlines, and value density under high event volume. At scale, this enables online admission control, SLA-aware prioritization, and accurate “use-it-before-it-expires” resource consumption without resorting to expensive time-expanded flow models.

## 🔍 Problem Statement
Each job is `jobs[i] = [releaseTime[i], creditsNeeded[i], value[i]]`. Each credit batch is `credits[j] = [grantTime[j], amount[j], expireTime[j]]`. Starting at `grantTime`, `amount` credits are usable; any unused credits disappear immediately after `expireTime`. Credits from overlapping active batches are interchangeable, but a job must consume all `creditsNeeded[i]` at one integer time `t >= releaseTime[i]`. Jobs are indivisible and can be completed at most once.

Return the maximum total value.

Constraints are large: `1 <= n, m <= 2 * 10^5`, times up to `10^9`, amounts and values up to `10^9`. That rules out DP over time or explicit simulation of every integer timestamp.

Examples:

- `jobs = [[1,2,8],[2,1,4],[3,2,7]]`, `credits = [[1,2,2],[2,1,3],[3,2,3]]` → `19`
- `jobs = [[1,3,10],[2,2,9],[2,1,3],[4,2,8]]`, `credits = [[1,2,2],[2,2,2],[4,2,4]]` → `17`

The core challenge is that resource feasibility depends on both cumulative arrivals and per-batch expiration boundaries.

## 🪜 How to Solve This
1. Read the problem → notice this is not just “pick highest-value jobs under total credits.” Credits are time-scoped, so feasibility changes as batches arrive and expire.

2. Time is sparse → only event times matter: job releases, credit grants, and credit expirations. That suggests a sweep-line over sorted events rather than iterating across `10^9` timestamps.

3. At any sweep position, some jobs are available and some credits are active. The immediate question becomes: if credits must be spent before the next expiration, which jobs should consume them?

4. Expiring credits create urgency. When a batch is about to disappear, any credits not assigned by then are lost forever. So each expiration acts like a capacity deadline.

5. Reframe the problem as deadline-constrained selection: every active credit unit belongs to the earliest expiry among remaining supply. Jobs chosen before that boundary must fit within cumulative credits expiring by then.

6. That points to a greedy admission structure: process credit capacity in increasing expiry order, keep candidate jobs released so far, and maintain the best-value feasible set under current cumulative capacity.

7. Because jobs have variable credit cost, the heap is not just “take largest value.” The right state is a selected set whose total credit usage stays within capacity, evicting the least efficient choices when over budget.

## 🧩 Algorithm Walkthrough
1. **Convert credits into deadline capacity events.**  
   For each batch `(grant, amount, expire)`, add `amount` to cumulative available capacity starting at `grant`, but that capacity must be consumed no later than `expire`. The useful abstraction is a **sweep-line over time with capacity deadlines**: by any time `t`, the total credits that can still have been used before expired batches vanish is the sum of all batches with `expire <= t`, restricted to jobs released by `t`.

2. **Sort jobs by release time and credit expirations by expire time.**  
   We process deadlines in increasing order. Before handling deadline `d`, insert every job with `releaseTime <= d` into the candidate pool. This maintains the invariant: the pool contains exactly the jobs that could legally be completed by `d`.

3. **Maintain a best feasible subset under cumulative credit budget.**  
   Let `cap(d)` be total credits from batches expiring at or before `d`. Among jobs released by `d`, we want the maximum-value subset with total `creditsNeeded <= cap(d)`. This is a classic **greedy with a priority queue**, but weighted: keep selected jobs in a min-heap keyed by “removal priority” while tracking total selected credits and total selected value.

4. **Use value-aware replacement when capacity is exceeded.**  
   Insert a new job optimistically. If total selected credits now exceeds `cap(d)`, remove jobs that are worst to keep under the current budget. The heap supports fast eviction. The invariant becomes: after processing deadline `d`, the selected set is the best set representable by jobs released so far within cumulative expiring capacity.

5. **Carry the selected set forward.**  
   Later deadlines only increase the candidate set and cumulative capacity. Previously selected jobs remain valid because they were already feasible before earlier expirations. The sweep therefore composes correctly across deadlines.

6. **Answer with total selected value after the final deadline.**  
   This works because every lost credit is accounted for at its expiration boundary, and every chosen job is admitted only when enough pre-expiry capacity exists.

## 📊 Worked Example
Using Example 2:

`jobs = [[1,3,10],[2,2,9],[2,1,3],[4,2,8]]`  
`credits = [[1,2,2],[2,2,2],[4,2,4]]`

| Deadline | New jobs available | Credits expiring by now | Candidate jobs | Best feasible subset | Total value |
|---|---|---:|---|---|---:|
| 2 | `(3,10)`, `(2,9)`, `(1,3)` | 4 | three jobs | choose `(2,9)` + `(1,3)` | 12 |
| 4 | `(2,8)` | 6 | all four jobs | keep prior two, add `(2,8)` | 20? |

Why not 20? Because the 4 credits available by time 2 must cover all jobs completed no later than 2. The 2-credit job at time 4 uses fresh credits from the batch granted at 4, so it is valid. But the 3-credit value-10 job cannot be retrofitted: by time 2, choosing it would crowd out the better combination `(2,9)+(1,3)`. Final answer: `9 + 3 + 8 = 17`.

## ⏱ Complexity Analysis
### Time Complexity
`O((n + m) log (n + m))`. Sorting jobs and credit expiration events dominates setup, and each job enters the heap once and may be removed once. This is the right regime for `2 * 10^5` inputs; at `10^6`, it is still practical in optimized languages, while anything quadratic is dead on arrival.

### Space Complexity
`O(n + m)` for sorted event arrays plus the heap of candidate/selected jobs. The heap owns the live working set. Space can be reduced slightly with in-place sorting and event compression, but not below linear without sacrificing clarity or increasing recomputation.

## 💡 Key Takeaways
• If inputs contain both arrivals and expirations, and feasibility depends on “what is active right now,” think sweep-line before thinking DP.  
• If you must keep the best subset under a changing budget, a heap-backed greedy replacement policy is a strong signal.  
• Expiration is inclusive through `expireTime`; credits disappear immediately after it, so jobs at time `expireTime` may still use them.  
• Do not collapse all active credits into one scalar without preserving expiry order; that silently admits impossible schedules.  
• The transferable design insight: expiring capacity should be modeled as deadline-constrained supply, not as a global counter.

## 🚀 Variations & Further Practice
- Allow jobs to be split across multiple times. The problem shifts toward min-cost flow / time-expanded networks because indivisibility is what makes the heap-based greedy interesting.  
- Add job deadlines in addition to release times. Now both supply and demand expire, requiring dual event processing and tighter feasibility invariants.  
- Make job value depend on completion time (decay or lateness penalty). The static ordering breaks, and the scheduler becomes a dynamic priority problem rather than pure admission control.