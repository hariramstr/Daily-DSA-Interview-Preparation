# Minimum Printer Rate for Deadline Reports

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given two arrays, `pages` and `deadline`, determine the smallest integer printer rate `r` such that printing reports sequentially in the given order finishes each report by its own deadline. Report `i` completes after the cumulative pages of reports `0..i` are printed at rate `r`. The challenge is that deadlines apply to prefixes, not individual jobs in isolation, and the answer space is large enough that linear search over rates is not viable.

## 🌍 Engineering Impact
This pattern shows up anywhere a fixed-capacity system must satisfy ordered latency or completion SLOs: batch schedulers, CI/CD worker pools, media transcoding queues, streaming backfill jobs, and shared print/render services. The key architectural question is not just “can the system keep up on average?” but “what minimum capacity guarantees every prefix meets its deadline?” Without exploiting monotonic feasibility, capacity planning degenerates into expensive simulation or overprovisioning. Binary search over the answer turns a potentially unbounded tuning problem into a predictable control loop, which matters when rates, workloads, and deadlines are large and frequently recomputed.

## 🔍 Problem Statement
You are given `pages[i]` and `deadline[i]` for `n` reports, where `1 <= n <= 100000`, `1 <= pages[i] <= 10^9`, and `1 <= deadline[i] <= 10^9` in general, though deadlines may effectively make the answer impossible. Reports must be printed strictly in the given order at a constant integer rate `r` pages per minute. The completion time of report `i` is:

`(pages[0] + pages[1] + ... + pages[i]) / r`

A rate is feasible if every report finishes no later than `deadline[i]`. Return the minimum feasible integer `r`, or `-1` if no finite rate can satisfy all deadlines.

Examples:

- `pages = [6, 8, 3], deadline = [2, 5, 6]` → `3`
- `pages = [5, 5], deadline = [0, 10]` → `-1`

The decisive constraint is the combination of `n = 1e5` and values up to `1e9`, which rules out brute-force search over possible rates.

## 🪜 How to Solve This
1. Start from the definition of completion time: report `i` finishes after the cumulative pages up to `i` are printed. So this is really a **prefix constraint** problem.

2. Rewrite feasibility for a fixed rate `r`:
   - For every `i`, we need `prefixPages[i] / r <= deadline[i]`.
   - Rearranged: `prefixPages[i] <= deadline[i] * r`.

3. That immediately suggests a cheap feasibility check:
   - Scan left to right.
   - Maintain cumulative pages.
   - If any prefix violates its deadline at rate `r`, reject `r`.

4. Next observation → feasibility is monotonic:
   - If rate `r` works, any larger rate also works because all completion times shrink.
   - That is the binary-search signal.

5. Search the smallest feasible `r` instead of testing every rate.
   - Lower bound can be `1`.
   - A safe upper bound is `max(prefixPages[i] / max(1, deadline[i]))` rounded up, or more simply `max(pages)` is not enough; use a large bound such as `1e18`-safe derived bound or doubling.

6. Before searching, detect impossible cases:
   - If any `deadline[i] == 0` while cumulative pages through `i` are positive, no finite rate can help.

That yields an `O(n log answer)` solution with a linear feasibility pass.

## 🧩 Algorithm Walkthrough
1. **Compute feasibility as a monotone predicate.**  
   This is the core pattern: **Binary Search on Answer**. For a candidate rate `r`, scan reports in order, accumulate `prefix += pages[i]`, and verify `prefix <= deadline[i] * r`. If this holds for all `i`, `r` is feasible. The invariant is that `prefix` equals total work required to complete report `i`.

2. **Handle impossible zero-deadline prefixes.**  
   If `deadline[i] == 0`, then feasibility requires `prefix == 0`. Since all `pages[i] >= 1`, any non-empty prefix already violates this. In practice, the same inequality check catches it, but calling it out prevents reasoning mistakes about “arbitrarily large” rates.

3. **Establish search bounds.**  
   Lower bound is `1`, since rate is a positive integer. For the upper bound, use either:
   - a derived bound `max(ceil(prefix / deadline[i]))` over all positive deadlines, or
   - exponential search (`1, 2, 4, ...`) until feasible.  
   The derived bound is tighter and still linear.

4. **Binary search the first feasible rate.**  
   Standard lower-bound search:
   - If `mid` is feasible, keep it and search left.
   - Otherwise search right.  
   Invariant: the answer, if it exists, remains inside `[lo, hi]`.

5. **Return the minimum feasible rate.**  
   Because the predicate is monotone and the search maintains the first-true invariant, the final `lo` is the smallest valid integer rate.

This abstraction is correct because deadlines constrain prefixes, and a larger rate can only improve every prefix completion time.

## 📊 Worked Example
Use `pages = [6, 8, 3]`, `deadline = [2, 5, 6]`.

| i | pages[i] | deadline[i] | prefixPages | Check at r = 2 | Check at r = 3 |
|---|---:|---:|---:|---:|---:|
| 0 | 6 | 2 | 6  | `6 <= 4` ❌ | `6 <= 6` ✅ |
| 1 | 8 | 5 | 14 | `14 <= 10` ❌ | `14 <= 15` ✅ |
| 2 | 3 | 6 | 17 | `17 <= 12` ❌ | `17 <= 18` ✅ |

Trace:

1. Try `r = 2` → first prefix already fails, so `2` is infeasible.
2. Try `r = 3` → all prefix checks pass, so `3` is feasible.
3. Since feasibility is monotonic, no rate below `3` can work once `2` fails and `3` passes.
4. Minimum feasible rate is `3`.

Equivalent completion times at `r = 3` are `2`, `14/3`, and `17/3`, each within its deadline.

## ⏱ Complexity Analysis

### Time Complexity
`O(n log U)`, where `U` is the search range for the rate. Each feasibility check is a single linear pass over `n` reports, and binary search performs `log U` such passes. At million-scale inputs this remains practical; at billion-scale inputs, the array scan, not the logarithmic factor, dominates.

### Space Complexity
`O(1)` auxiliary space beyond the input arrays. The algorithm stores only running prefix sums and binary-search bounds. Space cannot be meaningfully reduced further without changing the input model; precomputing prefix arrays is optional and trades memory for no asymptotic gain.

## 💡 Key Takeaways
• If the question asks for the **minimum capacity/rate/threshold** that makes all constraints pass, check whether feasibility becomes easier as the value increases; that is the binary-search-on-answer signal.  
• Prefix-based deadlines usually mean you should reason about **cumulative work**, not per-item work in isolation.  
• Use 64-bit arithmetic for `prefix` and `deadline[i] * r`; `1e9 * 1e9` already exceeds 32-bit range.  
• Be careful with upper bounds: `max(pages)` is not sufficient because deadlines constrain cumulative prefixes, not single reports.  
• In production capacity planning, monotone feasibility lets you replace guess-and-check provisioning with a deterministic search over the smallest safe operating point.

## 🚀 Variations & Further Practice
- Allow reports to be reordered before printing. The twist is that feasibility now depends on scheduling policy, pushing the problem toward deadline scheduling and greedy ordering arguments.
- Make the printer rate vary by minute with a fixed total energy budget. The monotone single-parameter search disappears, and the problem becomes resource allocation over time.
- Add multiple identical printers processing reports in parallel while preserving per-report deadlines. The conceptual jump is from scalar capacity search to parallel-machine scheduling with significantly harder feasibility checks.