# Minimum Rental Cost for Deadline-Limited Machines

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heaps, priority-queue, greedy, scheduling, intervals

---

## 🗂 Problem Overview
Given jobs with release day `start[i]`, deadline `end[i]`, and unit processing time, choose how many machine slots to rent on each priced day so every job is assigned to one feasible day. Renting one machine on day `d` costs that day’s price, and multiple machines may be rented on the same day. Return the minimum total rental cost, or `-1` if no schedule exists. The difficulty is balancing cheap days against deadline pressure under large, sparse day ranges.

## 🌍 Engineering Impact
This pattern shows up anywhere work has release times, deadlines, and time-varying execution cost: cloud batch scheduling on spot markets, ad delivery under pacing constraints, warehouse labor planning, GPU reservation systems, and streaming backfill windows. At scale, the failure mode is not just inefficiency; it is deadline miss amplification caused by consuming cheap capacity too early or too late. The heap-based greedy framing enables online-feeling decisions over large sparse timelines, avoids quadratic reassignment logic, and gives a tractable way to reason about urgency versus cost when capacity is purchased dynamically rather than preallocated.

## 🔍 Problem Statement
You are given `n` jobs, where job `i` may be processed on exactly one integer day `d` satisfying `start[i] <= d <= end[i]`. You are also given `m` rentable days as pairs `(day, price)`. Jobs may only be scheduled on listed rental days. Renting one machine on day `day` costs `price` and provides capacity for exactly one job on that day; renting `k` machines costs `k * price`.

Return the minimum total rental cost needed to complete all jobs, or `-1` if no feasible schedule exists.

Constraints:
- `1 <= n, m <= 200000`
- `1 <= start[i] <= end[i] <= 10^9`
- `1 <= price <= 10^9`
- Inputs may be unsorted

Examples:
- `jobs = [[1,3],[2,2],[2,4]]`, `rentalDays = [[1,5],[2,2],[3,4],[4,7]]` → `11`
- `jobs = [[1,2],[1,2],[2,3],[3,3]]`, `rentalDays = [[1,8],[2,3],[3,1]]` → `8`

The key algorithmic constraint is sparse time: day values go up to `10^9`, so only event days can be processed.

## 🪜 How to Solve This
1. Read the problem → this is not “assign each job greedily to its cheapest day” because jobs compete for the same day’s capacity, and capacity itself is what we buy.

2. Reframe the decision → instead of tracking machine identities, decide how many jobs to place on each rentable day. Since each rented machine handles one job, day `d` can host any number of jobs at linear cost `price[d]`.

3. Notice the structure → jobs are unit-length intervals, so feasibility depends only on how many jobs with deadline `<= x` can be placed into rentable days `<= x`.

4. Sort rentable days by price ascending → if a cheaper day can legally host some jobs, we want to use it before more expensive days.

5. But cheap-first alone is unsafe → when considering a day, we must know which jobs are eligible and which are most urgent. That points directly to a min-heap keyed by deadline.

6. Process days in increasing price, add all jobs whose `start <= day`, and tentatively schedule jobs on that day while respecting deadlines. The heap tells us which available jobs expire soonest.

7. The greedy insight → whenever we consume a slot on a chosen day, assign it to the currently available job with smallest `end`. That preserves future flexibility and is the standard earliest-deadline-first exchange argument.

## 🧩 Algorithm Walkthrough
1. **Sort inputs into event order.**  
   Sort jobs by `start` ascending. Sort rentable days by `price` ascending, breaking ties by `day` ascending. This sets up the greedy choice: consider cheaper capacity before more expensive capacity.

2. **Maintain availability with a min-heap of deadlines.**  
   As you reach a rentable day `d`, push into the heap every job with `start <= d`. The heap contains exactly the jobs that have become available but are not yet scheduled. Key by `end`, because the most urgent job should consume any feasible slot first.

3. **Discard expired jobs before using day `d`.**  
   While heap top has `end < d`, that job can no longer be scheduled on the current or any later day. If such a job is still unscheduled, feasibility is already lost, so return `-1`. This invariant matters: every job remaining in the heap can still use day `d`.

4. **Decide whether to rent capacity on day `d`.**  
   If the heap is non-empty, the cheapest currently considered day should be used for one of those jobs. Pop the smallest deadline and assign it to day `d`, adding `price[d]` to the answer. Repeat this for as many copies of day `d` as you conceptually buy; since each listed day appears once, the practical interpretation is one rentable slot per machine and unlimited machines at that price, so keep assigning while profitable and feasible.

5. **Why earliest-deadline-first is correct.**  
   This is a **Greedy + Priority Queue** pattern. On any chosen day, assigning a later-deadline job instead of an earlier-deadline one cannot improve future feasibility; it only risks stranding the urgent job. Swapping them never increases cost because both use the same day price.

6. **Finish with a feasibility check.**  
   After processing all rentable days, if any jobs remain unscheduled, return `-1`; otherwise return the accumulated cost. The invariant throughout is: scheduled jobs occupy the cheapest processed feasible slots, and unscheduled available jobs are represented exactly once in the heap.

## 📊 Worked Example
Use `jobs = [[1,2],[1,2],[2,3],[3,3]]` and `rentalDays = [[1,8],[2,3],[3,1]]`.

Sort rental days by price: `(3,1), (2,3), (1,8)` is tempting, but feasibility depends on time. The correct implementation processes actual days in chronological order while using the heap to force urgent jobs first.

| Day | Price | Jobs becoming available | Heap before assign | Assigned jobs | Cost |
|---|---:|---|---|---|---:|
| 1 | 8 | `[1,2], [1,2]` | `[2,2]` | none yet if we defer | 0 |
| 2 | 3 | `[2,3]` | `[2,2,3]` | two jobs with deadline `2` | 6 |
| 3 | 1 | `[3,3]` | `[3,3]` | two jobs with deadline `3` | 8 |

Trace:
1. By day 2, two jobs expire immediately, so they must consume capacity there.
2. By day 3, the remaining two jobs both fit and day 3 is cheapest.
3. Total cost is `2*3 + 2*1 = 8`.

## ⏱ Complexity Analysis
### Time Complexity
`O((n + m) log n)` after sorting. Sorting jobs and rental days dominates setup, and each job is pushed and popped from the heap at most once. This is the difference between a solution that survives `2 * 10^5` inputs comfortably and one that collapses under quadratic reassignment. At `10^6` scale, heap discipline is still practical; at `10^9` day values, iterating raw time is impossible.

### Space Complexity
`O(n)` for the priority queue holding currently available unscheduled jobs, plus sorted input storage. This is already near-optimal for an exact greedy solution; reducing it would require streaming or external sorting, trading memory for implementation complexity and I/O overhead.

## 💡 Key Takeaways
- If you see unit-time jobs with release times and deadlines, plus “choose among currently available items,” think greedy scheduling with a deadline-ordered heap.
- Sparse day values up to `10^9` are a strong signal to process only event days, never the full timeline.
- The main trap is treating “cheap day” as globally safe; cost ordering without deadline protection will strand urgent jobs.
- Another common bug is forgetting that jobs may only run on listed rental days, so interval feasibility must be checked against rentable-day events, not raw calendar days.
- The production-grade insight is to separate **capacity purchase decisions** from **work-item assignment**, then connect them with a priority queue that preserves future optionality.

## 🚀 Variations & Further Practice
- Add a per-day machine cap `cap[d]`; now each day has bounded capacity, and the heap must be drained up to that limit while preserving feasibility.
- Let jobs have processing time `> 1`; this breaks the unit-interval simplification and pushes the problem toward min-cost flow or more complex deadline scheduling.
- Introduce machine types or job classes with compatibility constraints; the single-heap model becomes multiple interacting queues or a flow/matching formulation.