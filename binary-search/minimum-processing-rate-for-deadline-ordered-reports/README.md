# Minimum Processing Rate for Deadline Ordered Reports

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Monotonic Predicate, Greedy

---

## 🗂 Problem Overview
Given ordered jobs `reports[i]` and required completion times `deadlines[i]`, find the minimum integer processing rate `r` such that sequential execution finishes every job by its deadline. Job `i` takes `ceil(reports[i] / r)` hours, and completion time is cumulative across all prior jobs. Return the smallest feasible `r`, or `-1` if impossible. The non-trivial part is that feasibility is global: local per-job checks are insufficient because early delays propagate forward.

## 🌍 Engineering Impact
This pattern shows up in capacity planning for ordered pipelines: ETL stages with SLA checkpoints, batch indexing, CI/CD queues, media transcoding backlogs, and single-threaded compaction or replay workers. The operational question is often identical: what minimum throughput guarantees all downstream deadlines under strict ordering? Without exploiting monotonic feasibility, teams end up with brute-force simulation, poor autoscaling heuristics, or overprovisioned systems. The binary-search-on-answer pattern turns a vague sizing problem into a deterministic admission-control or provisioning decision, which matters when rates, workloads, and deadlines are large enough that naive search is operationally useless.

## 🔍 Problem Statement
You are given two arrays of equal length `n`:

- `reports[i]`: pages in the `i`-th job, `1 <= reports[i] <= 10^12`
- `deadlines[i]`: latest allowed completion time for job `i`, `1 <= deadlines[i] <= 10^18`

Jobs must be processed strictly in the given order by one processor running at constant integer rate `r` pages/hour. Job `i` requires `ceil(reports[i] / r)` hours. Since execution is sequential, the completion time of job `i` is the cumulative sum of rounded processing times from job `0` through `i`.

A rate is feasible iff every cumulative completion time is `<= deadlines[i]`. Return the minimum feasible integer `r`, or `-1` if no such rate exists. `n` can be as large as `2 * 10^5`, so scanning all possible rates is impossible.

Examples:

- `reports = [8, 5, 10], deadlines = [2, 4, 7]` → `4`
- `reports = [9, 9, 9], deadlines = [1, 2, 2]` → `-1`

## 🪜 How to Solve This
1. Read the problem → notice the decision variable is not a schedule but a single integer rate `r`.
2. Try a fixed `r` → the schedule becomes deterministic because order is fixed. You can simulate cumulative completion times in one pass.
3. Observe the key monotonicity → if rate `r` is feasible, any larger rate is also feasible, since every `ceil(reports[i] / r)` stays the same or decreases.
4. Monotone yes/no predicate over an integer range → think binary search on the answer.
5. Define `check(r)` → accumulate `time += ceil(reports[i] / r)` and fail immediately if `time > deadlines[i]`.
6. Before searching, detect impossible cases cheaply → even at infinite rate, each job still costs at least `1` hour, so cumulative minimum completion times are `1, 2, ..., n`. If any `i + 1 > deadlines[i]`, answer is `-1`.
7. Search the smallest feasible `r` in `[1, 10^12]` using lower-bound binary search.

That chain gets you from problem statement to an `O(n log M)` solution, where `M = 10^12`.

## 🧩 Algorithm Walkthrough
1. **Apply the monotonic predicate pattern.**  
   Define feasibility as: “all jobs meet deadlines at rate `r`.” This is monotonic because increasing `r` cannot increase any rounded processing time. That makes binary search the right abstraction: we are finding the first `true` in a sorted boolean space.

2. **Run an impossibility pre-check.**  
   Even with arbitrarily large `r`, each job takes at least `1` hour due to ceiling. Therefore the best possible cumulative completion time after processing `i` jobs is `i + 1`. If `deadlines[i] < i + 1` for any index, no rate can work. This invariant rules out impossible instances before any search.

3. **Implement `check(r)` as a greedy simulation.**  
   Iterate jobs in order, compute `jobTime = (reports[i] + r - 1) / r`, and add it to `elapsed`. After each addition, verify `elapsed <= deadlines[i]`. The invariant is: after processing index `i`, `elapsed` equals the earliest possible completion time of job `i` at rate `r`, because order is fixed and there is no idle-time advantage.

4. **Binary search for the minimum feasible rate.**  
   Search over `[1, 10^12]`. If `check(mid)` succeeds, keep the left half including `mid`; otherwise search right. This maintains the invariant that the answer, if it exists, always remains inside the current interval.

5. **Return the lower bound.**  
   After convergence, the search lands on the smallest feasible integer rate. Combined with the impossibility pre-check, correctness is complete.

## 📊 Worked Example
Use `reports = [8, 5, 10]`, `deadlines = [2, 4, 7]`.

Check `r = 3`:

| i | reports[i] | ceil(reports[i]/3) | cumulative time | deadline | feasible so far |
|---|------------|--------------------|-----------------|----------|-----------------|
| 0 | 8          | 3                  | 3               | 2        | No              |

`r = 3` fails immediately.

Check `r = 4`:

| i | reports[i] | ceil(reports[i]/4) | cumulative time | deadline | feasible so far |
|---|------------|--------------------|-----------------|----------|-----------------|
| 0 | 8          | 2                  | 2               | 2        | Yes             |
| 1 | 5          | 2                  | 4               | 4        | Yes             |
| 2 | 10         | 3                  | 7               | 7        | Yes             |

Since `4` works and `3` does not, the minimum feasible rate is `4`. Binary search finds this transition point without testing every rate between `1` and `10^12`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n` is the number of jobs and `M` is the maximum candidate rate (`10^12`). Each feasibility check is a single pass over `n`, and binary search performs about `log2(10^12) ≈ 40` checks. This remains practical at `10^6` scale; linear or quadratic rate search does not.

### Space Complexity
`O(1)` auxiliary space beyond the input arrays. The algorithm stores only the binary search bounds, current elapsed time, and per-iteration arithmetic. Space cannot be meaningfully reduced further; the main trade-off is using wider integer types to avoid overflow.

## 💡 Key Takeaways
• If the problem asks for the minimum numeric parameter that makes a yes/no condition hold, check whether feasibility is monotonic and search the answer space.  
• When order is fixed and there is no branching in execution, a candidate answer often reduces to a deterministic one-pass simulation.  
• Do the impossibility pre-check: because of ceiling, each job costs at least one hour, so `deadlines[i] < i + 1` is an immediate `-1`.  
• Use integer ceiling safely as `(x + r - 1) / r`, and keep cumulative time in 64-bit or wider arithmetic to avoid overflow.  
• In production, this is a capacity-sizing pattern: convert SLA compliance into a monotone predicate, then solve provisioning with logarithmic search instead of iterative guesswork.

## 🚀 Variations & Further Practice
- Allow idle time between jobs and add release times; feasibility is no longer just cumulative sum, and the check must reason about waiting plus deadlines.
- Replace one processor with `k` identical processors while preserving per-job order constraints inside each stream; the monotone search may remain, but the feasibility check becomes a scheduling problem.
- Optimize for minimum rate under a bounded number of deadline violations; the predicate shifts from strict feasibility to constrained slack allocation, requiring more complex DP or greedy reasoning.