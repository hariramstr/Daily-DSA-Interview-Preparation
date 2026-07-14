# Maximum Consecutive Sensor IDs After One Repair

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Two Pointers, Greedy

---

## 🗂 Problem Overview
Given a sorted, strictly increasing array of sensor IDs, change at most one element to any integer so the array contains the longest possible set of distinct consecutive values. Return that maximum run length. The challenge is not finding existing streaks, but reasoning about how one repair can either bridge a single missing value between blocks or extend one block by replacing an outlier.

## 🌍 Engineering Impact
This pattern shows up anywhere ordered identifiers are expected to be dense but may contain one corrupt record: telemetry sequence validation, CDC pipelines, event-log compaction, time-series shard indexes, compiler/debug symbol ranges, and search segment docID maintenance. At scale, brute-force “try every replacement” logic is dead on arrival; it explodes on hot paths and complicates recovery workflows. The useful abstraction is gap analysis over sorted distinct data: identify contiguous blocks, then determine whether one mutation can merge or extend them. That enables linear-time validation, online repair heuristics, and predictable behavior under large cardinalities.

## 🔍 Problem Statement
You are given `nums`, a sorted array of distinct integers where `1 <= nums.length <= 100000` and each value lies in `[-10^9, 10^9]`. You may modify at most one element, changing it to any integer, as long as the final array is still considered a set of distinct values. Your goal is to maximize the length of any consecutive run of integers present after that optional repair.

A run is any set of values of the form `[x, x+1, ..., y]` with no gaps.

Examples:

- `nums = [10, 11, 13, 14]` → `5`  
  Change `14 -> 12`, yielding values `{10,11,12,13,14}`.

- `nums = [3, 4, 7, 8, 9]` → `4`  
  Change `3 -> 6`, yielding longest run `{6,7,8,9}`.

The key constraint is `n <= 100000`: this rules out quadratic exploration and pushes toward a single pass over gap structure.

## 🪜 How to Solve This
1. Read the array as **consecutive blocks separated by gaps**. Since `nums` is already sorted and distinct, every adjacent difference tells you whether you stay in the same block or start a new one.

2. Ask what one repair can actually do. It cannot fix multiple gaps. So the only meaningful moves are:
   - **bridge two blocks** if the gap size is exactly `2` (one missing integer),
   - or **extend one block by 1** by changing some value outside that block to the missing neighbor.

3. That immediately suggests tracking block lengths, not individual replacement values. Once blocks are known, each block contributes a baseline answer, each bridgeable pair contributes a merge candidate, and any non-full-array block can potentially be extended by one because one element elsewhere can be repurposed.

4. Why greedy / two-pointers? Because the sorted order already encodes all structure. A linear scan is enough to segment the array into maximal consecutive runs and evaluate local interactions between adjacent runs.

5. The mental model: **compress the array into runs, then reason locally about one allowed mutation**. One mutation affects at most two neighboring runs.

## 🧩 Algorithm Walkthrough
1. **Partition into maximal consecutive runs** using a linear scan.  
   If `nums[i] == nums[i-1] + 1`, stay in the current run; otherwise, close the run and start a new one.  
   Invariant: each recorded run is maximal and internally gap-free.

2. **Track each run length** in an array `lens`.  
   This converts the problem from raw values to block arithmetic.  
   Invariant: `sum(lens) == n`, and run boundaries correspond exactly to gaps `> 1`.

3. **Initialize the answer from single-run behavior.**  
   For a run of length `L`, you can always keep it. If there exists at least one element outside the run (`L < n`), you can often change that external element to extend the run by one endpoint, so candidate becomes `L + 1`. If the run already uses the whole array, candidate is just `L`.  
   Invariant: this covers the “extend one block” case.

4. **Check adjacent runs for a bridge.**  
   Let run `i` end at value `a`, and run `i+1` start at value `b`. If `b - a == 2`, there is exactly one missing integer between them. One repair can fill that hole by changing some element not needed in the merged run. Candidate length is `lens[i] + lens[i+1] + extra`, where `extra = 1` for the inserted missing value. This is valid only if there is some element available to change; in practice, that means:
   - if the merged runs do not already cover all `n` elements, you can use an outside element and get `lens[i] + lens[i+1] + 1`;
   - otherwise, you must sacrifice one endpoint from within the merged region, so the best remains `lens[i] + lens[i+1]`.

5. **Return the maximum candidate.**  
   This is a greedy local-merge problem over run boundaries. The sorted array makes a full dynamic program unnecessary because one repair can influence only one gap or one run endpoint.

## 📊 Worked Example
Take `nums = [10, 11, 13, 14]`.

First scan into runs:

| Index | Value | Adjacent diff | Action | Runs so far |
|---|---:|---:|---|---|
| 0 | 10 | — | start run | `[10]` |
| 1 | 11 | 1 | extend run | `[10,11]` |
| 2 | 13 | 2 | close/start | `[10,11]`, `[13]` |
| 3 | 14 | 1 | extend run | `[10,11]`, `[13,14]` |

Run lengths are `[2, 2]`.

- Single-run extension: each run can become length `3` because another element exists outside it.
- Adjacent bridge: gap between `11` and `13` is exactly `2`, so the missing value is `12`.
- The two runs together cover all 4 elements, so there is no extra outside element to donate. Bridging yields `2 + 2 + 1 = 5` because one existing element can be changed to `12`, and the remaining values still realize `{10,11,12,13,14}`.

Answer: `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. The dominant work is one left-to-right pass to form consecutive runs and inspect adjacent gaps. No nested search or replacement simulation is needed. At `10^6` elements this is routine in memory-resident systems; at `10^9`, the algorithm is still asymptotically right, but execution becomes constrained by I/O and storage layout rather than CPU.

### Space Complexity
`O(k)` where `k` is the number of consecutive runs, worst-case `O(n)`. That space is owned by the run-length metadata. It can be reduced to `O(1)` by streaming only the previous run and current run, at the cost of slightly more careful state management.

## 💡 Key Takeaways
• If the input is already sorted and distinct, look for **run compression** first; many array problems become simpler when rewritten as lengths of maximal blocks.  
• “One modification” is a strong signal to reason about **local effects only**: one gap bridged, one block extended, not global reshaping.  
• The critical off-by-one is distinguishing a gap of `2` from larger gaps: only `2` is bridgeable with one inserted value.  
• When extending or bridging, check whether an **extra donor element exists outside the target run(s)**; otherwise you may overcount by one.  
• In production code, compressing raw ordered data into structural summaries often turns expensive repair/search logic into linear-time decisions with predictable scaling.

## 🚀 Variations & Further Practice
- Allow **up to `k` repairs** instead of one. The twist is that local reasoning no longer suffices; you need windowing over total missing counts or DP over gaps.
- Remove the guarantee that the array is **sorted and distinct**. The harder part becomes deduplication plus rebuilding run structure efficiently.
- Maximize the longest consecutive run after **one insertion or deletion** rather than replacement. The conceptual twist is that cardinality changes, so donor-element reasoning changes as well.