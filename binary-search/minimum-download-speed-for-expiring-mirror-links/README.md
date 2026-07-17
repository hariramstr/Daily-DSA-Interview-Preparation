# Minimum Download Speed for Expiring Mirror Links

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Scheduling

---

## 🗂 Problem Overview
Given ordered file sizes and per-file expiration deadlines, choose the smallest integer download speed `S` such that downloading files sequentially finishes every prefix by its corresponding deadline. File `i` takes `ceil(sizes[i] / S)` minutes, and cumulative time through `i` must be `<= expires[i]`. Return the minimum feasible `S`, or `-1` if impossible. The non-trivial part is that rounded per-file durations change discontinuously as speed changes, so direct optimization is awkward but feasibility is monotonic.

## 🌍 Engineering Impact
This pattern shows up in systems that must provision a single constrained resource against ordered deadlines: CDN backfill jobs, sequential replication catch-up, batch ETL stages with SLA checkpoints, firmware rollout pipelines, and media transcoding queues. At scale, brute-force tuning of throughput is infeasible because each candidate rate requires replaying the schedule. The important architectural insight is monotone feasibility: once a capacity level satisfies all deadline constraints, higher capacity also works. That enables binary search over provisioned capacity instead of simulation over a huge parameter space, which is exactly how schedulers, autoscalers, and admission-control systems stay predictable under large input ranges.

## 🔍 Problem Statement
You are given two integer arrays, `sizes` and `expires`, each of length `n` where `1 <= n <= 2 * 10^5`. File `i` has size `sizes[i]` megabytes and must finish no later than `expires[i]` minutes. A single downloader processes files strictly in index order and cannot overlap work. At constant integer speed `S` megabytes per minute, file `i` requires `ceil(sizes[i] / S)` whole minutes. File `i` is successful only if the cumulative time spent on files `0..i` is at most `expires[i]`.

Return the minimum integer `S` that makes all files succeed, or `-1` if no such speed exists.

Examples:

- `sizes = [8, 5, 10], expires = [3, 5, 9]` → `3`
- `sizes = [4, 4, 4], expires = [1, 2, 2]` → `-1`

The decisive constraint is the search space: `sizes[i]` can reach `10^12` and deadlines `10^18`, so only an `O(n log M)` style solution is realistic.

## 🪜 How to Solve This
1. Read the requirement carefully → the order is fixed, so this is not interval scheduling or sorting by deadline. We only control one thing: the constant speed `S`.

2. Ask what happens if `S` increases → every term `ceil(sizes[i] / S)` stays the same or decreases. Therefore cumulative completion times never get worse.

3. That gives the key monotonic property → if speed `S` is feasible, then any `S' > S` is also feasible. Once you see monotone feasibility over an integer answer space, binary search should be the default move.

4. Now define a feasibility check → simulate the downloads in order, accumulate `ceil(sizes[i] / S)`, and fail immediately if cumulative time exceeds `expires[i]`.

5. Before searching, test global impossibility → even at infinite speed, each file still costs at least 1 minute, so cumulative minimum completion times are `1, 2, ..., n`. If any `expires[i] < i + 1`, no speed can work.

6. Choose a safe search range → `1` to `max(sizes)` is enough, because at speed `max(sizes)` every file takes exactly 1 minute.

That yields a clean `O(n log max(sizes))` solution.

## 🧩 Algorithm Walkthrough
1. **Run an impossibility pre-check.**  
   For each index `i`, the absolute best-case duration of file `i` is 1 minute, regardless of speed. Therefore the earliest possible completion time of file `i` is `i + 1`. If `expires[i] < i + 1` for any `i`, return `-1`.  
   **Invariant:** if the pre-check passes, a sufficiently large speed may still be feasible.

2. **Define the monotone predicate `feasible(S)`.**  
   Simulate files in order, maintaining `time = 0`. For each file, add `(sizes[i] + S - 1) // S`, then compare `time` with `expires[i]`. If `time > expires[i]`, return `false`; otherwise continue.  
   **Why correct:** the problem’s success condition is exactly prefix completion against deadlines.  
   **Invariant:** after processing index `i`, `time` equals the completion time of file `i` at speed `S`.

3. **Binary search the answer space.**  
   Search `S` in `[1, max(sizes)]`. The lower bound is the smallest legal positive speed. The upper bound works because `S >= max(sizes)` makes every file take 1 minute, which is the best possible discrete duration.  
   **Pattern:** binary search on answer over a monotone predicate.

4. **Shrink toward the first feasible speed.**  
   If `feasible(mid)` is true, keep the left half including `mid`; otherwise discard it and search right.  
   **Invariant:** the true answer, if it exists, always remains inside the current interval.

5. **Return the left boundary.**  
   When the interval collapses, it is the minimum feasible speed by construction.

## 📊 Worked Example
Example: `sizes = [8, 5, 10]`, `expires = [3, 5, 9]`

Pre-check: minimum possible completion times are `1, 2, 3`, all within deadlines, so continue.

| Speed `S` | File 0 time | Cum | File 1 time | Cum | File 2 time | Cum | Feasible |
|---|---:|---:|---:|---:|---:|---:|---|
| 5 | `ceil(8/5)=2` | 2 | `ceil(5/5)=1` | 3 | `ceil(10/5)=2` | 5 | Yes |
| 2 | `ceil(8/2)=4` | 4 | — | — | — | — | No (`4 > 3`) |
| 3 | `ceil(8/3)=3` | 3 | `ceil(5/3)=2` | 5 | `ceil(10/3)=4` | 9 | Yes |

Binary search over `[1, 10]`:
1. `mid = 5` → feasible, search left.
2. `mid = 2` → infeasible, search right.
3. `mid = 3` → feasible, search left boundary.
4. Interval converges at `3`.

Minimum valid speed is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `M = max(sizes)`. Each binary-search step runs one linear feasibility scan, and there are `log2(M)` such steps. With `M <= 10^12`, that is about 40 passes. This scales comfortably for `n = 2 * 10^5`; a linear scan over `10^6` items is fine, but `10^9` is not.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only a few scalar variables: bounds, midpoint, and cumulative time. Space cannot meaningfully be reduced further; the only trade-off is readability versus inlining the feasibility check.

## 💡 Key Takeaways
- If the problem asks for the minimum capacity, speed, rate, or threshold that makes a schedule feasible, check whether feasibility is monotone and search the answer directly.
- Fixed processing order plus prefix deadlines is a strong signal that the validation step should be a single left-to-right simulation, not sorting or dynamic programming.
- Use integer ceiling division carefully: `ceil(a / b)` should be implemented as `(a + b - 1) // b`, not floating-point math.
- The impossibility condition is easy to miss: even infinite speed cannot make a file take less than one whole minute, so deadlines must satisfy `expires[i] >= i + 1`.
- In production systems, monotone predicates often turn expensive capacity planning into deterministic logarithmic search over a massive configuration space.

## 🚀 Variations & Further Practice
- Allow reordering of files before download. The twist is that feasibility now depends on schedule selection, pushing the problem toward deadline scheduling and exchange arguments rather than pure binary search.
- Replace one downloader with `k` parallel downloaders. The feasibility check becomes a multi-machine scheduling problem, and monotonicity may remain but validation is substantially harder.
- Make speed vary over time with a total bandwidth budget. The challenge shifts from binary search on a scalar answer to optimizing an allocation policy under cumulative deadline constraints.