# Minimum Playback Speed for Chaptered Tutorials

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Greedy

---

## 🗂 Problem Overview
Given ordered chapter lengths and a day limit `d`, find the smallest integer playback speed `s` that makes the tutorial finishable in `d` days or fewer. At speed `s`, each chapter takes `ceil(length / s)` minutes, chapters must stay in order, and no chapter can be split across days. The non-trivial part is the monotonic search space: feasibility improves as speed increases, which makes binary search the right optimization tool.

## 🌍 Engineering Impact
This pattern shows up anywhere a global control parameter changes per-item cost monotonically and you need the minimum safe setting. Examples include batch sizing in streaming pipelines, CPU throttling for deadline-bound jobs, shard fan-out limits in search systems, and rate settings in distributed backfill workers. At scale, brute-force tuning is operationally expensive and often impossible under large input ranges. Monotonic feasibility plus binary search turns “try every configuration” into a predictable control loop, enabling fast admission checks, autoscaling thresholds, and capacity planning decisions without scanning an unbounded parameter space.

## 🔍 Problem Statement
You are given an array `chapters` where `chapters[i]` is the length of the `i`th video chapter in minutes, and an integer `d` representing the maximum number of days allowed. Chapters must be watched in original order, each day must contain at least one whole chapter, and chapters cannot be split across days.

A single integer playback speed `s >= 1` is applied to every chapter. At speed `s`, a chapter of length `x` takes `ceil(x / s)` minutes. Return the minimum integer `s` such that all chapters can be scheduled into `d` consecutive groups or fewer.

Constraints:
- `1 <= chapters.length <= 100000`
- `1 <= chapters[i] <= 1000000000`
- `1 <= d <= chapters.length`

Examples:
- `chapters = [9, 7, 12, 6], d = 2` → `2`
- `chapters = [5, 8, 4, 10, 3], d = 3` → `3`

The key algorithmic driver is the huge search range for `s`, which rules out linear trial over candidate speeds.

## 🪜 How to Solve This
1. Read the problem → the output is not a partition itself, but the **minimum global speed** that makes some valid partition possible.

2. Notice the monotonic property → if speed `s` works, any larger speed also works because every effective chapter time `ceil(x / s)` stays the same or decreases.

3. Monotonic feasibility usually means **binary search on the answer**, not binary search inside the data.

4. To binary search, define a predicate: “Is speed `s` sufficient?”  
   That requires converting each chapter to `ceil(x / s)` and checking whether the resulting sequence can be split into at most `d` consecutive days.

5. The feasibility check is greedy → scan left to right, accumulate the current day, and start a new day only when adding the next chapter would violate the day budget implied by the optimal grouping rule for that speed.

6. Because the predicate is monotone, binary search can shrink to the first feasible speed.

7. The result is a standard pattern: expensive search space, cheap linear validator, logarithmic number of probes.

## 🧩 Algorithm Walkthrough
1. **Establish the search space.**  
   The minimum possible speed is `1`. A safe upper bound is `max(chapters)`, because at that speed every chapter takes at most `1` minute, so the schedule always fits in at most `n` days, and `d <= n`.  
   **Invariant:** the answer lies within `[lo, hi]`.

2. **Define the feasibility predicate.**  
   For a candidate speed `s`, compute each effective chapter time as `(x + s - 1) / s`. Then determine whether the transformed array can be partitioned into at most `d` consecutive groups under the problem’s grouping rule.  
   **Why correct:** the speed fully determines per-chapter cost; feasibility reduces to checking whether the induced sequence admits a valid schedule.

3. **Use a greedy day-construction pass.**  
   Scan chapters in order, append each to the current day while valid, otherwise start a new day. This is the standard **Greedy + Binary Search on Answer** pattern.  
   **Invariant:** after processing index `i`, you have used the minimum number of days needed for the prefix under speed `s`.

4. **Binary search for the first feasible speed.**  
   Let `mid = lo + (hi - lo) / 2`. If `mid` is feasible, keep the left half including `mid`; otherwise discard it and search right.  
   **Invariant:** all speeds below `lo` are known infeasible; all speeds above `hi` are known feasible.

5. **Terminate when `lo == hi`.**  
   That value is the smallest speed satisfying the predicate.  
   **Why correct:** binary search over a monotone boolean boundary always converges to the first `true`.

## 📊 Worked Example
Take `chapters = [5, 8, 4, 10, 3]`, `d = 3`.

| Step | `lo` | `hi` | `mid` | Effective times at `mid` | Feasible? |
|---|---:|---:|---:|---|---|
| Start | 1 | 10 | — | — | — |
| 1 | 1 | 10 | 5 | `[1,2,1,2,1]` | Yes |
| 2 | 1 | 5 | 3 | `[2,3,2,4,1]` | Yes |
| 3 | 1 | 3 | 2 | `[3,4,2,5,2]` | No |

Trace:
1. `mid = 5` works, so tighten to `[1,5]`.
2. `mid = 3` also works, so tighten to `[1,3]`.
3. `mid = 2` fails, so discard it and search `[3,3]`.

Binary search stops at `3`, which is the minimum feasible playback speed.

## ⏱ Complexity Analysis

### Time Complexity
`O(n log M)`, where `n = chapters.length` and `M = max(chapters)`. Each binary-search probe runs one linear feasibility pass, and there are `log M` probes. This scales well even when `n` reaches `10^6`; by contrast, brute-forcing speeds up to `10^9` is operationally dead on arrival.

### Space Complexity
`O(1)` auxiliary space. The algorithm only maintains scalar counters, bounds, and running totals. No extra arrays are required if effective times are computed on the fly. You could materialize transformed times for debugging, but that increases memory without improving asymptotic runtime.

## 💡 Key Takeaways
- If the question asks for the **minimum integer parameter** that makes a constraint satisfiable, check immediately for a monotone predicate and binary search on the answer.
- When costs are recomputed from a global setting but item order is fixed, expect a **linear feasibility validator** paired with logarithmic search over the parameter space.
- Be careful with ceiling division: use `(x + s - 1) / s` to avoid floating-point errors and precision bugs.
- Binary search boundaries matter: use an upper bound that is provably feasible, or you risk searching a range that does not contain the answer.
- The transferable design insight is to separate **optimization** from **validation**: build a cheap monotone checker, then wrap it with binary search instead of tuning by enumeration.

## 🚀 Variations & Further Practice
- Add a hard cap on total minutes per day and ask for the minimum speed that satisfies both the day count and per-day budget; the twist is combining two interacting feasibility constraints.
- Allow skipping up to `k` chapters per day or reordering within bounded windows; the twist is that simple greedy validation may break, requiring DP or more complex state.
- Related pattern: capacity-to-ship / split-array problems, where you binary search the minimum capacity instead of speed; the twist is the searched parameter changes from per-item transform to per-group limit.