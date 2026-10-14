# Minimum Tap Strength for Garden Row Coverage

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Intervals

---

## 🗂 Problem Overview
Given a garden row `[0, L]` and sprinkler positions along that row, find the minimum integer tap strength `s` such that every point in `[0, L]` is covered. With strength `s`, each sprinkler covers `[positions[i] - s, positions[i] + s]`. Return the smallest feasible `s`, or `-1` if no sprinklers exist. The challenge is scale: `L` can be `10^9`, so iterating over strengths or simulating point-by-point coverage is not viable.

## 🌍 Engineering Impact
This pattern shows up anywhere a global threshold must be minimized while satisfying full-range coverage: radio tower power calibration, CDN edge radius planning, warehouse sensor placement, and autoscaling policies that must guarantee SLA coverage across a demand range. The production lesson is not “search faster,” but “search over a monotone decision boundary.” Without that framing, teams brute-force configuration space, burn compute, and still get poor latency. With it, feasibility checks become first-class primitives, enabling predictable optimization loops, capacity planning, and rollout safety checks at large input sizes.

## 🔍 Problem Statement
You are given an integer `L` representing a continuous garden row from position `0` to `L`, and an array `positions` of length `n` containing sprinkler locations. All sprinklers must use the same integer tap strength `s`. A sprinkler at `positions[i]` covers the interval `[positions[i] - s, positions[i] + s]`.

Return the minimum integer `s` such that the union of all sprinkler intervals fully covers `[0, L]`. If `positions` is empty, return `-1`.

Constraints:
- `0 <= L <= 10^9`
- `0 <= n <= 2 * 10^5`
- `0 <= positions[i] <= L`
- `positions` may be unsorted

Examples:
- `L = 10, positions = [1, 4, 8]` → `2`
- `L = 12, positions = [3, 9]` → `3`

The key constraint is `L` up to `10^9`: the answer space is huge, so the solution must test feasibility efficiently and binary search the minimum valid strength.

## 🪜 How to Solve This
1. Read the problem → the output is a **minimum integer parameter** that makes a condition true. That is the first signal for binary search on the answer.

2. Ask whether feasibility is monotone → if strength `s` covers the whole row, then any `s + 1` also covers it. Once coverage becomes possible, it stays possible. That gives a sorted true/false boundary.

3. Define the feasibility check → for a fixed `s`, each sprinkler becomes an interval. Sort sprinklers once, then scan left to right and verify there is no uncovered gap in `[0, L]`.

4. Reduce the coverage test to one running value → maintain the farthest point covered so far. For each interval, if its left edge is beyond current coverage, there is a gap and `s` fails. Otherwise extend coverage with its right edge.

5. Binary search the smallest feasible `s` → low is `0`, high can be `L` because a sprinkler inside `[0, L]` with strength `L` covers the full row. Search for the first `s` whose feasibility check returns true.

That chain gets you from “continuous coverage” to “interval merge” to “monotone predicate” to an efficient solution.

## 🧩 Algorithm Walkthrough
1. **Handle the degenerate case.**  
   If `positions` is empty, return `-1`. No amount of strength helps when there are no intervals to generate. This is the only impossible case under the given constraints.

2. **Sort sprinkler positions.**  
   This is the setup for a **Greedy Interval Scan**. Sorting lets us process candidate coverage in spatial order, which is exactly the right abstraction when the correctness condition is “no gaps.”

3. **Define `canCover(s)`.**  
   For each position `p`, compute interval `[p - s, p + s]`. Maintain `coveredRight`, the farthest point in `[0, L]` known to be covered continuously from `0`.  
   Invariant: before processing the next interval, every point in `[0, coveredRight]` is covered with no gaps.

4. **Greedily merge intervals.**  
   If the current interval starts after `coveredRight`, coverage is broken and `s` is infeasible. Otherwise, extend `coveredRight = max(coveredRight, p + s)`. This is correct because among all overlapping intervals, only the farthest right endpoint matters for future coverage.

5. **Check completion.**  
   If after scanning all sprinklers, `coveredRight >= L`, then `[0, L]` is fully covered. Otherwise some suffix remains uncovered.

6. **Binary search the answer.**  
   Use the monotone predicate `canCover(s)`: false, false, ..., true, true. Search the first true value over `[0, L]`. Each probe costs one linear scan after sorting, so the total is efficient even when `L` is large.

## 📊 Worked Example
Example: `L = 10`, `positions = [1, 4, 8]`

Sorted positions: `[1, 4, 8]`

Try `s = 2`:

| Sprinkler `p` | Interval `[p-s, p+s]` | `coveredRight` before | Action | `coveredRight` after |
|---|---:|---:|---|---:|
| 1 | `[-1, 3]` | `0` | overlaps start of row | `3` |
| 4 | `[2, 6]` | `3` | overlaps existing coverage | `6` |
| 8 | `[6, 10]` | `6` | touches boundary, still continuous | `10` |

Result: `coveredRight = 10`, so `s = 2` is feasible.

Try `s = 1`:

- Intervals are `[0,2]`, `[3,5]`, `[7,9]`
- After first sprinkler, `coveredRight = 2`
- Next interval starts at `3`, which is greater than `2` → gap exists

So `s = 1` fails, and the minimum valid strength is `2`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting costs `O(n log n)`. Each feasibility check is `O(n)`, and binary search performs `O(log L)` checks, giving total complexity `O(n log n + n log L)`. At `n = 2 * 10^5`, this is practical. Brute-forcing strengths up to `10^9` is not; the answer-space search must be logarithmic.

### Space Complexity
Space is `O(1)` beyond the sort if the input array can be sorted in place; otherwise `O(n)` depending on the language’s sorting implementation. The feasibility scan itself uses constant extra state. Reducing space further is usually not worth avoiding the clarity of sorted interval processing.

## 💡 Key Takeaways
- If a problem asks for the **minimum parameter** that makes a condition hold, immediately test whether the condition is monotone and therefore binary-searchable.
- If the condition is “continuous coverage of a line,” translate inputs into intervals and think greedy merge/scan before considering more complex structures.
- Be careful with **touching intervals**: `[2,6]` followed by `[6,10]` is continuous coverage, so the gap check is `start > coveredRight`, not `>=`.
- Clamp your reasoning to the target segment `[0, L]`; intervals may extend below `0` or above `L`, but only uncovered gaps inside the row matter.
- The transferable design insight is to separate optimization from validation: build a fast feasibility predicate, then search the parameter space around it.

## 🚀 Variations & Further Practice
- Allow each sprinkler to have its own maximum strength or activation cost, then minimize total cost while covering `[0, L]`; the twist is moving from monotone feasibility to interval DP or weighted covering.
- Make sprinklers activatable only in subsets of positions over time, and ask for the earliest time full coverage becomes possible; same binary-search pattern, but the predicate depends on temporal state.
- Extend from 1D row coverage to 2D circular coverage over a rectangle; the monotonicity may remain, but the feasibility check becomes computational geometry rather than a linear interval scan.