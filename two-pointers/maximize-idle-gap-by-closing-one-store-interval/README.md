# Maximize Idle Gap by Closing One Store Interval

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Array, Intervals

---

## 🗂 Problem Overview
Given a business day length and a sorted, non-overlapping list of open intervals, compute the largest idle gap obtainable after removing at most one interval. Gaps include the prefix before the first interval, the suffix after the last interval, and spaces between adjacent intervals. The non-trivial part is scale: with up to 200,000 intervals, trying every removal and recomputing gaps is too expensive, so the solution must exploit interval ordering and run in linear time.

## 🌍 Engineering Impact
This pattern shows up anywhere availability windows, reservations, or maintenance slots are modeled as disjoint intervals: ad-serving blackout windows, warehouse dock scheduling, calendar availability engines, CDN maintenance planning, and streaming pipeline pause windows. At scale, the difference between recomputing global state per candidate removal and doing a single linear pass is the difference between interactive planning and batch-only tooling. The underlying architectural lesson is common: when data is already sorted and non-overlapping, preserve that structure. It enables constant-time local reasoning, predictable latency, and simpler correctness arguments under high-volume workloads.

## 🔍 Problem Statement
You are given:

- `dayLength`: total minutes in the day
- `intervals[i] = [start_i, end_i]`: sorted, pairwise non-overlapping open intervals
- Each interval uses half-open semantics: `start` is included, `end` is excluded

You may remove **zero or one** interval. After that, return the maximum continuous idle gap anywhere in `[0, dayLength)`.

If an interval in the middle is removed, its left gap, its own duration, and its right gap merge into one larger idle segment.

Constraints:

- `1 <= intervals.length <= 200000`
- `1 <= dayLength <= 1000000000`
- `0 <= start_i < end_i <= dayLength`

Examples:

- `dayLength = 20, intervals = [[2,5],[7,9],[12,15]]` → `7`
- `dayLength = 30, intervals = [[0,4],[6,10],[13,18],[22,30]]` → `12`

The key constraint is input size: any approach worse than linear or near-linear is unnecessary and risky.

## 🪜 How to Solve This
1. Start from what changes when one interval is removed → only the gaps around that interval matter. Everything else stays untouched.

2. Define the existing gaps explicitly:
   - before the first interval
   - between every adjacent pair
   - after the last interval

3. Observe the merge rule → removing interval `i` creates one idle segment from the previous interval’s end to the next interval’s start. Its length is:
   - left gap
   - plus removed interval length
   - plus right gap

4. That means each candidate removal can be evaluated using only local neighbors, not by rebuilding the whole schedule.

5. Also remember “remove nothing” is allowed → the answer must be at least the largest existing gap.

6. Because intervals are already sorted and non-overlapping, a single left-to-right scan is enough to compute:
   - every existing gap
   - every merged gap created by removing one interval

7. This is why the problem is a linear interval scan / two-neighbor reasoning problem, not a search or DP problem. The sorted structure eliminates the need for nested comparisons.

## 🧩 Algorithm Walkthrough
1. **Compute the baseline maximum gap.**  
   Scan the schedule once and measure:
   - prefix gap: `intervals[0][0]`
   - internal gaps: `intervals[i][0] - intervals[i-1][1]`
   - suffix gap: `dayLength - intervals[n-1][1]`  
   This handles the “remove nothing” case and establishes the minimum correct answer.

2. **Evaluate removing each interval using local boundaries.**  
   For interval `i = [s, e)`, the merged idle segment after removal spans:
   - from `0` if `i == 0`, otherwise `intervals[i-1][1]`
   - to `dayLength` if `i == n-1`, otherwise `intervals[i+1][0]`  
   Its length is `rightBoundary - leftBoundary`.

3. **Why this is correct.**  
   The intervals are sorted and non-overlapping, so removing one interval cannot affect any region except the one bounded by its immediate neighbors. All other gaps remain unchanged. Therefore the only new candidate introduced by removing `i` is exactly that merged segment.

4. **Maintain a running maximum.**  
   Compare each candidate merged gap against the baseline maximum and update the answer.

5. **Pattern fit: Two Pointers / linear interval scan.**  
   This is effectively a two-neighbor pointer pattern: each decision uses the previous and next interval only. The abstraction is right because sorted disjoint intervals collapse a global optimization problem into constant-time local evaluations per index.

## 📊 Worked Example
Example: `dayLength = 30`, `intervals = [[0,4],[6,10],[13,18],[22,30]]`

| i | interval | left boundary | right boundary | merged gap if removed |
|---|----------|---------------|----------------|------------------------|
| - | baseline gaps | `0, 2, 3, 4, 0` | - | max baseline = `4` |
| 0 | `[0,4)`  | `0`  | `6`  | `6`  |
| 1 | `[6,10)` | `4`  | `13` | `9`  |
| 2 | `[13,18)`| `10` | `22` | `12` |
| 3 | `[22,30)`| `18` | `30` | `12` |

Trace:

1. Existing gaps are `[0,0)`, `[4,6)`, `[10,13)`, `[18,22)`, `[30,30)` with lengths `0,2,3,4,0`.
2. Best without removal is `4`.
3. Remove `[13,18)` → idle segment becomes `[10,22)` length `12`.
4. Remove `[22,30)` → idle segment becomes `[18,30)` length `12`.
5. Maximum achievable idle gap is `12`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = intervals.length`. The dominant work is a constant amount of arithmetic per interval while scanning the sorted list. At `10^6` elements this is still practical in a single pass; at `10^9`, even linear work becomes infrastructure-scale and demands partitioning or streaming.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only a few scalar variables for boundaries, current gap, and running maximum. You could materialize all gaps in an array, but that increases memory with no algorithmic benefit unless later queries require reuse.

## 💡 Key Takeaways
- If intervals are already sorted and non-overlapping, suspect a linear scan before considering heavier machinery.
- When removing one element only affects adjacent structure, look for a local-merge formula instead of recomputing the whole state.
- Be precise about half-open intervals: gap lengths come from subtraction of boundaries, with no `+1/-1` adjustments.
- Edge intervals are special: removing the first or last interval merges with the day boundary, not a missing neighbor.
- In production scheduling systems, preserving sorted disjoint representations turns global “what-if” analysis into cheap local reasoning.

## 🚀 Variations & Further Practice
- Remove **up to `k` intervals** to maximize the largest idle gap: the twist is that local independence disappears, pushing the problem toward DP or window-based optimization.
- Intervals are **not sorted and may overlap**: now you must first normalize by sorting and merging, then reason about removals on the canonical interval set.
- Support **many online queries** asking for the best gap after removing interval `i`: the twist is preprocessing for fast per-query answers, typically with prefix/suffix maxima or segment structures.