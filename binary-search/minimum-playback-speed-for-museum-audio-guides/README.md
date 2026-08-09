# Minimum Playback Speed for Museum Audio Guides

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Math

---

## 🗂 Problem Overview
Given section lengths `guides[]` and a time budget `limit`, find the smallest positive integer playback speed `s` such that the total listening time `sum(ceil(guides[i] / s))` is at most `limit`. Sections must be consumed in order, all at the same speed. The non-trivial part is scale: `guides.length` can reach `100000`, section lengths can reach `1e9`, and brute-forcing candidate speeds is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere a tunable capacity parameter must satisfy a hard latency or throughput budget. Examples include batch sizing in streaming pipelines, shard fan-out limits in search systems, request pacing in distributed rate-limiters, and compiler or build systems choosing parallelism under wall-clock constraints. The common shape is a monotonic feasibility function: increasing the control variable never makes the outcome worse. Without exploiting that monotonicity, systems fall back to linear probing or exhaustive simulation, which collapses under large search spaces. Binary search turns “find the minimum safe operating point” into a predictable logarithmic control loop.

## 🔍 Problem Statement
You are given an integer array `guides`, where `guides[i]` is the duration in minutes of the `i`-th audio section at normal speed, and an integer `limit`, the maximum total number of whole minutes allowed.

Choose a single positive integer playback speed `s` applied to every section. Each section takes `ceil(guides[i] / s)` minutes because playback only advances on minute boundaries. Return the minimum such `s` with total time at most `limit`. If no speed can satisfy the limit, return `-1`.

Constraints:
- `1 <= guides.length <= 100000`
- `1 <= guides[i] <= 1000000000`
- `1 <= limit <= 1000000000`

Critical edge case: if `limit < guides.length`, the answer is always `-1`, since every section costs at least `1` minute.

Examples:
- `guides = [7, 11, 5], limit = 8` → `4`
- `guides = [12, 3, 9, 6], limit = 4` → `12`

The key algorithmic driver is the huge speed search space: candidate speeds range up to `max(guides)`.

## 🪜 How to Solve This
1. Read the objective → we need the **minimum** integer speed that satisfies a time budget.
2. Write the cost function: for speed `s`, total time is `sum(ceil(guides[i] / s))`.
3. Observe the monotonic property → if speed increases, each `ceil(guides[i] / s)` stays the same or decreases, so total time never increases.
4. Monotonic feasibility usually means binary search over the answer, not over the array.
5. Define the predicate: “Is speed `s` fast enough?” That is true when total time `<= limit`.
6. Bound the search space:
   - Lower bound: `1`
   - Upper bound: `max(guides)` because at that speed every section takes exactly `1` minute.
7. Handle impossibility early: if `limit < guides.length`, even infinite speed cannot reduce below one minute per section.
8. Binary search the first feasible speed:
   - If `mid` works, keep searching left for a smaller valid speed.
   - If `mid` fails, search right.
9. Use integer ceiling safely as `(x + s - 1) / s` to avoid floating-point issues.

This is the standard “minimum feasible parameter under a monotonic constraint” playbook.

## 🧩 Algorithm Walkthrough
1. **Pre-check impossibility.**  
   If `limit < guides.length`, return `-1`. This is correct because each section contributes at least `1` minute regardless of speed. Invariant: any remaining instance has at least one feasible speed candidate in principle.

2. **Choose search bounds.**  
   Set `left = 1` and `right = max(guides)`. Speed `1` is the slowest legal option. Speed `max(guides)` guarantees every section finishes in `1` minute, so total time becomes exactly `guides.length`, which is feasible whenever the pre-check passed. Invariant: the answer lies in `[left, right]`.

3. **Apply Binary Search on Answer.**  
   This is not searching sorted data; it is searching a monotonic predicate over integers. For `mid = left + (right - left) / 2`, compute total time at speed `mid`.

4. **Evaluate feasibility.**  
   For each section length `x`, add `(x + mid - 1) / mid` to the running total. This is integer ceiling division. If the total exceeds `limit`, `mid` is infeasible; otherwise it is feasible. Invariant: feasibility transitions only once, from false to true, as speed increases.

5. **Shrink the range correctly.**  
   - If `mid` is feasible, record it implicitly by setting `right = mid`; a smaller valid speed may exist.
   - If `mid` is infeasible, set `left = mid + 1`; all smaller speeds are also infeasible by monotonicity.

6. **Terminate at the first feasible speed.**  
   When `left == right`, that value is the minimum valid speed. Correctness follows from maintaining the invariant that all speeds below `left` are infeasible and at least one speed in `[left, right]` is feasible.

Pattern: **Binary Search on Monotonic Answer Space**. It fits because the optimization target is an integer parameter and the feasibility function is monotone.

## 📊 Worked Example
Example: `guides = [7, 11, 5]`, `limit = 8`

| Step | left | right | mid | Time at `mid` | Feasible? | Action |
|---|---:|---:|---:|---|---|---|
| 1 | 1 | 11 | 6 | `ceil(7/6)+ceil(11/6)+ceil(5/6)=2+2+1=5` | Yes | `right = 6` |
| 2 | 1 | 6 | 3 | `3+4+2=9` | No | `left = 4` |
| 3 | 4 | 6 | 5 | `2+3+1=6` | Yes | `right = 5` |
| 4 | 4 | 5 | 4 | `2+3+2=7` | Yes | `right = 4` |

Now `left == right == 4`, so the minimum valid speed is `4`.

The useful trace insight is that we never enumerate all speeds. We only test enough points to isolate the first feasible one, and each test is a linear pass over `guides`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n = guides.length` and `M = max(guides)`. Each binary-search iteration scans the array once to compute total time, and there are `log M` iterations. At `n = 10^6`, this remains practical; at `M = 10^9`, the search depth is only about 30.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only bounds, a midpoint, and a running total. No extra data structures scale with input size. You could not meaningfully reduce this further without changing the execution model.

## 💡 Key Takeaways
- If the problem asks for the **minimum integer parameter** that satisfies a constraint, check whether feasibility is monotonic and search the answer space directly.
- When increasing a control value can only improve or preserve the outcome, that is a strong binary-search signal even if the input array itself is unsorted.
- The impossibility check is `limit < guides.length`, not `<=`; equality can still be feasible if every section can be forced to exactly one minute.
- Use integer ceiling as `(x + s - 1) / s`; floating-point division introduces unnecessary precision risk and review noise.
- At scale, the transferable design move is to separate **feasibility evaluation** from **parameter search**, then exploit monotonicity to replace exhaustive tuning with logarithmic convergence.

## 🚀 Variations & Further Practice
- Allow playback speed to be a real number instead of an integer. The twist is switching from discrete binary search to precision-bounded search over continuous values.
- Add per-section setup overhead or non-uniform transition costs. The harder part is proving whether the feasibility function remains monotonic.
- Related pattern: Koko Eating Bananas / smallest divisor problems. Same binary-search-on-answer structure, but different domain semantics and edge-case surfaces.