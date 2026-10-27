# Maximum Launch Delay Before Missing Any Satellite Window

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, greedy, prefix-sums

---

## 🗂 Problem Overview
Given `duration`, `travel`, and `windowClose`, compute the largest integer launch delay `D` such that every pass starts no later than its corresponding closing time. Passes must occur in fixed order, and once launched, all start times are fully determined by cumulative prior durations and travel times. The challenge is scale: with up to `2 * 10^5` passes and values up to `10^18`, brute-force search over delays is infeasible, so the solution must exploit monotonic feasibility.

## 🌍 Engineering Impact
This pattern shows up anywhere a global offset must satisfy many downstream deadlines: batch job release scheduling, streaming pipeline watermark shifts, satellite contact planning, manufacturing line startup offsets, and distributed maintenance windows. The key property is monotone feasibility: if a later start fails, any even later start also fails. Without exploiting that structure, systems fall back to linear scans over massive search spaces or repeated simulation. Binary search over a deterministic feasibility check turns an operational planning problem into something predictable, scalable, and easy to reason about under large integer time domains.

## 🔍 Problem Statement
You are given `n` communication passes. Arrays `duration` and `windowClose` have length `n`; `travel` has length `n - 1`. If launch is delayed by integer `D`, then pass `0` starts at time `D`. For each subsequent pass, its start time equals the previous start time plus previous pass duration plus travel time to the next station.

A delay is feasible if every pass `i` starts at or before `windowClose[i]`. Return the maximum feasible integer `D`. If even `D = 0` violates some window, return `-1`.

Constraints:
- `1 <= n <= 200000`
- `0 <= duration[i] <= 10^9`
- `0 <= travel[i] <= 10^9`
- `0 <= windowClose[i] <= 10^18`

Example 1:
- `duration = [3, 2, 4]`
- `travel = [5, 1]`
- `windowClose = [4, 12, 20]`
- Output: `4`

Example 2:
- `duration = [5, 3]`
- `travel = [4]`
- `windowClose = [3, 8]`
- Output: `-1`

The large search range over `D` is what forces an `O(n log M)` approach.

## 🪜 How to Solve This
1. Read the timing rule → every pass start time is just `D` plus a fixed cumulative offset from earlier durations and travel.
2. Rewrite the condition → for each pass `i`, we need  
   `D + offset[i] <= windowClose[i]`,  
   so equivalently `D <= windowClose[i] - offset[i]`.
3. That means every pass imposes an upper bound on `D`; the mission is feasible iff `D` is no larger than the minimum of those bounds.
4. Notice the monotonicity: if delay `D` works, any smaller delay also works. If `D` fails, any larger delay fails. That is the binary-search signal.
5. So define a feasibility check: simulate start times or accumulate offsets and verify all windows.
6. Binary search the maximum feasible `D` over an integer range.
7. Before searching, handle the impossible case naturally: if `D = 0` fails, no nonnegative delay can work, so return `-1`.

You can also solve this directly with a running minimum bound, but the intended pattern here is binary search on a monotone predicate.

## 🧩 Algorithm Walkthrough
1. **Model pass start times with a prefix-style accumulation.**  
   Let `offset[0] = 0`. For `i > 0`,  
   `offset[i] = sum(duration[0..i-1]) + sum(travel[0..i-1])`.  
   Then `start[i] = D + offset[i]`. This is the core invariant: once `D` is fixed, all starts are deterministic.

2. **Define the monotone predicate `feasible(D)`.**  
   Scan passes from left to right, maintaining the current offset. For each `i`, check whether `D + offset <= windowClose[i]`. If any pass violates its window, return `false`; otherwise return `true`.  
   Correctness follows because these are exactly the required constraints.

3. **Why Binary Search fits.**  
   This is a classic **Binary Search on Answer** problem. If `feasible(D)` is true, then any smaller delay is also true because all start times shift earlier by the same amount. If `feasible(D)` is false, any larger delay remains false. That monotonic boundary is the right abstraction.

4. **Choose the search range.**  
   Lower bound is `0`. A safe upper bound is `min(windowClose)`, or more generally `max(windowClose)`; the answer is guaranteed to fit in signed 64-bit. Use `0..10^18` if you want a simple universal bound.

5. **Search for the last true value.**  
   Standard upper-mid binary search: while `lo <= hi`, test `mid`. If feasible, record it and move right; otherwise move left. Maintain the invariant that the answer is the greatest feasible delay seen so far.

6. **Return `-1` if nothing is feasible.**  
   If even `D = 0` fails, the binary search never finds a valid candidate.

## 📊 Worked Example
Use Example 1:

- `duration = [3, 2, 4]`
- `travel = [5, 1]`
- `windowClose = [4, 12, 20]`

Offsets before each pass:

| Pass `i` | Offset before pass | Start time = `D + offset` | Constraint |
|---|---:|---:|---|
| 0 | 0 | `D` | `D <= 4` |
| 1 | 3 + 5 = 8 | `D + 8` | `D + 8 <= 12` → `D <= 4` |
| 2 | 3 + 5 + 2 + 1 = 11 | `D + 11` | `D + 11 <= 20` → `D <= 9` |

The tightest bound is `D <= 4`, so the maximum feasible delay is `4`.

Binary-search view:
1. Test `D = 5` → pass 1 starts at `13`, fails.
2. Test `D = 4` → starts are `4, 12, 15`, all valid.
3. Therefore the last feasible delay is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`feasible(D)` scans all `n` passes once, so it is `O(n)`. Binary search over a 64-bit integer range takes at most about 60 iterations, giving `O(n log M)`, where `M` is the delay search space. At `10^6` elements this is still practical; at `10^9`, the scan itself becomes the bottleneck.

### Space Complexity
`O(1)` auxiliary space if offsets are accumulated on the fly during feasibility checks. No extra arrays are required. You could precompute prefix offsets in `O(n)` space, but that only trades memory for slightly simpler code, not better asymptotic runtime.

## 💡 Key Takeaways
- If the question asks for the **maximum value** that satisfies a condition, check whether feasibility is monotone; that is the strongest signal for binary search on answer.
- When each item’s state is a fixed global offset plus cumulative prior work, think prefix sums or running offsets before reaching for simulation-heavy designs.
- The constraint is on **start times**, not finish times; using finish times will silently produce wrong upper bounds.
- Use 64-bit arithmetic everywhere for offsets and comparisons; cumulative durations and travel can exceed 32-bit even when individual values do not.
- At scale, monotone-feasibility search is a design pattern for turning huge scheduling spaces into deterministic, bounded-cost planning logic.

## 🚀 Variations & Further Practice
- Allow each pass to be skipped at a penalty, and maximize launch delay while keeping total penalty under a budget. The twist is that feasibility is no longer a simple linear scan; it becomes DP or shortest-path flavored.
- Replace fixed order with reorderable passes plus travel matrix costs. The conceptual jump is that cumulative offsets are no longer predetermined, so the problem shifts toward sequencing and combinatorial optimization.
- Add both earliest-start and latest-start windows for each pass. The harder part is preserving monotonicity: feasibility may require checking interval consistency rather than only upper bounds.