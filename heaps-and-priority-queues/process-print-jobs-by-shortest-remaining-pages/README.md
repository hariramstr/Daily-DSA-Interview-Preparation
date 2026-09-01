# Process Print Jobs by Shortest Remaining Pages

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, sorting

---

## 🗂 Problem Overview
Given `jobs[i] = [arrivalTime_i, pages_i]`, return the order of original indices in which a single printer processes jobs. At any moment, the printer may choose only from jobs that have already arrived, and it always selects the job with the fewest pages; ties break by smaller original index. The challenge is that arrival order and execution order differ, so a single sort is insufficient. With up to `100000` jobs, repeated rescanning is too slow.

## 🌍 Engineering Impact
This pattern shows up anywhere work arrives over time and must be dispatched by a policy other than FIFO: kernel schedulers, batch executors, print spooling, warehouse pick queues, CI runners, and stream-processing backpressure control. At small scale, teams often get away with “scan all ready work and pick the best.” At production scale, that collapses under queue growth and bursty arrivals. The sort-plus-heap design separates admission order from execution priority, which is exactly what real schedulers need: predictable dispatch cost, explicit tie-breaking, and efficient handling of idle gaps without simulating every unit of time.

## 🔍 Problem Statement
You are given a 0-indexed array `jobs` where `jobs[i] = [arrivalTime_i, pages_i]`. A single printer processes at most one job at a time, and once started, a job runs to completion. When the printer becomes free, it may choose only among jobs with `arrivalTime <= currentTime`. Among available jobs, it selects the one with the fewest pages; if multiple jobs have the same page count, choose the smaller original index. Return the processing order of job indices.

Constraints:
- `1 <= jobs.length <= 100000`
- `0 <= arrivalTime_i <= 10^9`
- `1 <= pages_i <= 10^9`
- Time accumulation fits in signed 64-bit arithmetic

Examples:
- `[[1,4],[2,3],[3,1],[10,2]] -> [0,2,1,3]`
- `[[0,5],[0,2],[0,2],[1,1]] -> [1,2,3,0]`

The key constraint is input size: any approach that repeatedly scans waiting jobs becomes quadratic and will not scale.

## 🪜 How to Solve This
1. Read the problem → notice there are **two different orderings**:
   - jobs become eligible by `arrivalTime`
   - eligible jobs are chosen by `(pages, index)`

2. A single sorted array cannot represent both orderings at once. That usually means:
   - sort once for admission
   - maintain a dynamic priority structure for selection

3. Sort all jobs by `arrivalTime`, but keep the original index attached. Now you can stream jobs into the system in chronological order.

4. As time advances, push every newly arrived job into a min-heap keyed by `(pages, index)`. That heap always exposes the next correct job to run.

5. If the heap is empty but unprocessed jobs remain, the printer is idle. Don’t increment time one unit at a time; jump directly to the next job’s `arrivalTime`.

6. Pop the best available job, append its index to the answer, and advance `currentTime` by its page count.

That gives the right simulation model with logarithmic dispatch cost instead of repeated linear scans.

## 🧩 Algorithm Walkthrough
1. **Augment and sort the input**  
   Transform each job into `(arrivalTime, pages, index)` and sort by `arrivalTime`.  
   Why: eligibility is time-based, so we need a monotonic stream of arrivals.  
   Invariant: all jobs before pointer `i` have either been enqueued into the heap or already processed.

2. **Maintain a min-heap of available jobs**  
   Use a heap keyed by `(pages, index)`.  
   Why: the scheduling rule is “shortest job first,” with deterministic tie-breaking on original index.  
   Invariant: the heap contains exactly the jobs with `arrivalTime <= currentTime` that have not yet been processed.

3. **Advance time by admission, not by ticks**  
   While sorted jobs have `arrivalTime <= currentTime`, push them into the heap.  
   Why: this batches all newly eligible work before making the next scheduling decision.  
   Invariant: before each pop, the heap represents the full ready set.

4. **Handle idle periods explicitly**  
   If the heap is empty and jobs remain, set `currentTime = next arrivalTime`.  
   Why: no work can run before that time, so simulating intermediate timestamps is wasted effort.  
   Invariant: `currentTime` always points to either the next dispatch moment or the next arrival.

5. **Dispatch the next job**  
   Pop the heap minimum, append its `index` to the result, and do `currentTime += pages`.  
   Why: once started, a job is non-preemptive and runs to completion.  
   Invariant: result order matches the exact scheduling policy.

6. **Repeat until all jobs are processed**  
   Continue until both the arrival stream is exhausted and the heap is empty.  
   Pattern: **Sort + Min-Heap event simulation**. This is the right abstraction because arrivals are offline-known, but eligibility changes over time and selection is priority-based.

## 📊 Worked Example
Example: `jobs = [[1,4],[2,3],[3,1],[10,2]]`

Sorted with indices: `(1,4,0), (2,3,1), (3,1,2), (10,2,3)`

| Step | currentTime | Newly added to heap | Heap top after add | Chosen job | Result |
|---|---:|---|---|---|---|
| 1 | 0 | none | — | idle → jump to 1 | `[]` |
| 2 | 1 | `(4,0)` | `(4,0)` | job `0` runs, time → 5 | `[0]` |
| 3 | 5 | `(3,1), (1,2)` | `(1,2)` | job `2` runs, time → 6 | `[0,2]` |
| 4 | 6 | none | `(3,1)` | job `1` runs, time → 9 | `[0,2,1]` |
| 5 | 9 | none | — | idle → jump to 10 | `[0,2,1]` |
| 6 | 10 | `(2,3)` | `(2,3)` | job `3` runs, time → 12 | `[0,2,1,3]` |

The heap always contains exactly the arrived-but-unprocessed jobs, ordered by shortest pages then smallest index.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)`. Sorting all jobs by arrival time costs `O(n log n)`, and each job is pushed to and popped from the heap once, adding another `O(n log n)`. At `10^6` items this is still practical; at `10^9`, even input traversal is infeasible, so asymptotics stop being the limiting factor.

### Space Complexity
`O(n)` in the worst case. The sorted job list and the heap together can hold all jobs, especially when many arrive before the printer catches up. You can sort in place if mutation is allowed, but the ready-queue heap still requires linear worst-case space.

## 💡 Key Takeaways
• If items become available over time but must be selected by a different priority, think **sorted event stream + heap** immediately.  
• If the problem asks for “next best among currently eligible items,” it is usually not a pure sorting problem; eligibility is dynamic.  
• Use 64-bit time for `currentTime`; cumulative page counts can exceed 32-bit integer range.  
• When the heap is empty, jump directly to the next arrival time; stepping time forward incrementally is both slower and conceptually wrong.  
• This is the same architectural split used in production schedulers: one structure for admission order, another for dispatch policy.

## 🚀 Variations & Further Practice
- **Preemptive shortest-remaining-time scheduling**: a newly arrived shorter job can interrupt the current one. Harder because execution is no longer run-to-completion and remaining work must be updated dynamically.
- **Multiple printers / parallel workers**: dispatch to `k` identical printers. Harder because you now coordinate worker availability and job selection simultaneously.
- **Weighted priorities with aging**: combine pages, priority class, and wait time. Harder because the heap key may evolve over time, forcing reheapification strategies or alternative queue designs.