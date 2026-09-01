# Minimum Daily Build Quota for Staged Releases

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Scheduling

---

## 🗂 Problem Overview
Given ordered feature build efforts `builds[]`, a day limit `d`, and a cap `k` on how many features may span multiple days, compute the minimum integer daily quota `Q` that allows all work to finish left-to-right within at most `d` days. Features may be partially completed only at the current frontier. The non-trivial part is that feasibility depends on two coupled resources: total days consumed and how many feature boundaries are crossed with partial work.

## 🌍 Engineering Impact
This pattern shows up in staged rollout systems, CI/CD build farms, media transcoding pipelines, and ordered backfill jobs where work must advance monotonically and only the frontier item may be partially processed. At scale, the wrong quota policy either underutilizes capacity or explodes coordination overhead by fragmenting too many units across scheduling windows. Binary-searching the minimum viable quota matters because quota becomes an SLO knob: too high wastes reserved capacity, too low misses release deadlines. The split-budget constraint models real operational limits such as checkpoint cost, cache invalidation churn, or human approval boundaries between partially completed artifacts.

## 🔍 Problem Statement
You are given `n` features in fixed order, where `builds[i]` is the effort for feature `i`. Work proceeds strictly left to right. On any day, the team may finish a suffix of the current unfinished feature and then continue onto later features, but the total effort done that day cannot exceed a daily quota `Q`. A feature may span multiple days, yet at most `k` features may be split across more than one day. All work must finish in at most `d` days.

Return the minimum integer `Q` for which such a schedule exists.

Constraints:

- `1 <= n <= 200000`
- `1 <= builds[i] <= 10^12`
- `1 <= d <= 10^12`
- `0 <= k <= n`
- Answer fits in signed 64-bit

Examples:

- `builds = [7,2,5,10,8], d = 3, k = 1` → `14`
- `builds = [9,9,9], d = 2, k = 0` → `18`

The key algorithmic driver is monotonicity: if quota `Q` is feasible, every larger quota is also feasible.

## 🪜 How to Solve This
1. Read the objective → we are not asked for a schedule directly, but for the minimum quota making some schedule possible. That is classic **binary search on the answer**.
2. Ask whether feasibility is monotone → yes. If quota `Q` works, any larger quota can simulate the same schedule or do better.
3. Reduce the problem to a decision check: for a fixed `Q`, can we finish within `d` days while splitting at most `k` features?
4. Notice the left-to-right frontier rule removes combinatorial branching. For a fixed `Q`, the only meaningful choice is where days end.
5. To minimize days and usually also splits, greedily pack each day with as much work as possible. Any unused capacity cannot help later because order is fixed.
6. During this scan, count:
   - how many days are used,
   - whether a feature is split because it starts on one day and ends on another.
7. The subtle point: a feature larger than `Q` is impossible, since even one day cannot hold its full remaining chunk and only one contiguous frontier can be active.
8. Once feasibility is linear, binary search between a safe lower bound and upper bound gives the optimal quota.

## 🧩 Algorithm Walkthrough
1. **Pattern: Binary Search on Answer + Greedy Feasibility Scan.**  
   Search `Q` over an integer range. Feasibility is monotone, so the first feasible `Q` is the answer.

2. **Bounds.**  
   Lower bound is `max(builds)` when splits are unrestricted across days only if a day may continue the same feature; but because a day can process only up to `Q`, any feature must still fit eventually through repeated frontier work. So the true lower bound is at least `ceil(sum(builds)/d)` and at least `1`; using `max(1, ceil(sum/d))` is valid, but `1` also works. A practical tighter lower bound is `max(1, ceil(sum(builds)/d))`. Upper bound is `sum(builds)`.

3. **Feasibility scan for a fixed `Q`.**  
   Iterate features left to right while maintaining remaining capacity in the current day, total `days`, and `splits`. Greedily consume as much as possible from the current feature. If the feature does not finish before the day ends, the next day must continue it, so that feature counts as split exactly once.

4. **Invariant.**  
   After processing prefix `[0..i)`, the constructed schedule uses the minimum possible days for that prefix under quota `Q`, because every day except possibly the last is saturated. With fixed order, leaving capacity unused cannot reduce future days or splits.

5. **Counting splits correctly.**  
   A feature contributes `+1` to `splits` iff work on it occurs on more than one day. Even if it spans three days, it is still one split feature, not two split events.

6. **Early exits.**  
   If `days > d` or `splits > k`, stop immediately: `Q` is infeasible.

7. **Binary search update.**  
   If feasible, move left to find a smaller quota; otherwise move right. Return the first feasible value.

## 📊 Worked Example
Take `builds = [7,2,5,10,8]`, `d = 3`, `k = 1`, test `Q = 14`.

| Step | Feature effort | Day | Remaining day cap before | Action | Days | Split features |
|---|---:|---:|---:|---|---:|---:|
| 1 | 7 | 1 | 14 | finish feature 1 | 1 | 0 |
| 2 | 2 | 1 | 7 | finish feature 2 | 1 | 0 |
| 3 | 5 | 1 | 5 | finish feature 3 | 1 | 0 |
| 4 | 10 | 2 | 14 | finish feature 4 | 2 | 0 |
| 5 | 8 | 2 | 4 | do 4, continue next day | 2 | 1 |
| 6 | 8 rem 4 | 3 | 14 | finish feature 5 | 3 | 1 |

Result: `days = 3`, `splits = 1`, so `Q = 14` is feasible. Testing smaller quotas eventually shows `13` is infeasible, making `14` minimal.

## ⏱ Complexity Analysis
### Time Complexity
Binary search performs `O(log U)` feasibility checks, where `U` is the search range, typically up to `sum(builds)`. Each check is `O(n)`, so total time is `O(n log U)`. With 64-bit quotas, `log U` is at most about 60, which is effectively linear even for million-element inputs.

### Space Complexity
`O(1)` auxiliary space beyond the input array. The feasibility scan keeps only counters for days, splits, current feature remainder, and binary-search bounds. Space cannot be meaningfully reduced further; the main trade-off is clarity versus in-place arithmetic on 64-bit values.

## 💡 Key Takeaways
• If the question asks for a minimum capacity, quota, rate, or threshold and feasibility improves monotonically as that value increases, think binary search on the answer.  
• If processing order is fixed and only the current frontier may be partial, greedy saturation of each day is usually optimal because idle capacity cannot be recovered later.  
• Count split **features**, not split transitions: one feature spanning three days still consumes only one unit from `k`.  
• Use 64-bit arithmetic everywhere: `sum(builds)`, midpoint computation, and `ceil(sum/d)` can all overflow 32-bit integers.  
• In production schedulers, constraining fragmentation separately from throughput is often the real requirement; quota alone is not enough when partial progress has coordination cost.

## 🚀 Variations & Further Practice
- Add a per-day fixed startup cost before any work can be done. The twist is that effective usable quota becomes stateful, changing the feasibility arithmetic and lower bounds.
- Allow at most `k` split **events** instead of split features. A feature spanning three days now costs two events, which changes the greedy accounting.
- Make daily quotas vary by day (`Q[j]`) and ask for the minimum scaling factor on that quota vector. The monotonicity remains, but feasibility must respect non-uniform capacities.