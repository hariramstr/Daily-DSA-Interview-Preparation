# Longest Shelf Segment With One Height Drop Repair

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Greedy

---

## 🗂 Problem Overview
Given an integer array `heights`, find the maximum length of a contiguous segment that can be made nondecreasing after modifying at most one element anywhere inside that segment. The replacement value can be any integer, or you can skip the repair entirely. The output is only the best segment length. The challenge is scale: `n` can reach `200000`, so quadratic window checking is not viable and the solution must reason about local violations efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere a mostly ordered stream must tolerate a single local defect without discarding the whole range: event-time ordering in streaming pipelines, monotonic metric series in observability systems, ranked candidate lists in search/recommendation, and append-only storage segments with one corrupt record. At scale, brute-force validation of every interval collapses under latency and memory pressure. What matters is recognizing when one local repair preserves a global invariant. That enables linear scans, online validation, and predictable performance under high-throughput ingestion or large offline batch analysis.

## 🔍 Problem Statement
You are given `heights`, where `heights[i]` is the box height at shelf position `i`. A contiguous segment is valid if it is already nondecreasing, or can be made nondecreasing by changing at most one element in that segment to any integer value.

Return the maximum possible segment length.

Constraints:

- `1 <= heights.length <= 200000`
- `-1000000000 <= heights[i] <= 1000000000`
- Expected solution: `O(n log n)` or better

Examples:

- `heights = [3, 4, 2, 5, 6]` → `5`
- `heights = [7, 3, 5, 2, 6]` → `3`

Key edge cases:

- length `1` always works
- already nondecreasing arrays return `n`
- a segment may contain multiple drops, but only one element may be changed
- the replacement value is unconstrained except by the need to make the segment nondecreasing

The input size rules out checking every subarray explicitly.

## 🪜 How to Solve This
1. Start with the base problem: longest contiguous nondecreasing segment. That is easy with one scan.
2. Now add “one repair.” A repair only matters where monotonicity breaks, so the problem is really about whether one bad local region can be absorbed.
3. Think in terms of joining two already-valid runs. If a single element can be edited, then a long answer often looks like:
   - a nondecreasing prefix ending at `i-1`
   - one repaired element at `i`
   - a nondecreasing suffix starting at `i+1`
4. That suggests precomputing structure from both directions:
   - `left[i]`: length of the nondecreasing run ending at `i`
   - `right[i]`: length of the nondecreasing run starting at `i`
5. For each index, ask: if this is the edited element, can the left run and right run be stitched together? The stitch is possible when there exists some value between the left boundary and right boundary, i.e. `heights[i-1] <= heights[i+1]`.
6. Also handle edge positions, where editing the first or last element trivially extends one side.

Once you see “repair one point to reconnect monotone runs,” the linear DP scan becomes the obvious tool.

## 🧩 Algorithm Walkthrough
1. **Precompute left-to-right run lengths (`left`)**  
   Use a linear scan. `left[i]` is the length of the longest nondecreasing contiguous segment ending at `i`.  
   If `heights[i-1] <= heights[i]`, then `left[i] = left[i-1] + 1`; otherwise reset to `1`.  
   **Invariant:** `left[i]` always describes a valid monotone suffix ending at `i`.

2. **Precompute right-to-left run lengths (`right`)**  
   Scan backward. `right[i]` is the length of the longest nondecreasing contiguous segment starting at `i`.  
   If `heights[i] <= heights[i+1]`, then `right[i] = right[i+1] + 1`; otherwise reset to `1`.  
   **Invariant:** `right[i]` always describes a valid monotone prefix starting at `i`.

3. **Initialize answer with the no-repair case**  
   The array may already contain the optimal segment without any edit, so start with `max(left)`.

4. **Try each index as the repaired element**  
   This is a **Dynamic Programming + local greedy merge** pattern. The DP arrays summarize maximal valid runs; the greedy part tests whether one edit can bridge them.  
   - If repairing index `i` at an edge, candidate length is `1 + right[i+1]` or `left[i-1] + 1`.
   - If `0 < i < n-1`, then:
     - left-only extension: `left[i-1] + 1`
     - right-only extension: `1 + right[i+1]`
     - full merge if `heights[i-1] <= heights[i+1]`: `left[i-1] + 1 + right[i+1]`

5. **Take the maximum over all candidates**  
   This is correct because any valid repaired segment has at most one edited position, and everything on both sides of that position must already be nondecreasing. The merge condition exactly captures whether one chosen value can satisfy both neighboring constraints simultaneously.

## 📊 Worked Example
Example: `heights = [7, 3, 5, 2, 6]`

| i | heights[i] | left[i] | right[i] | Best if repair at i |
|---|------------|---------|----------|---------------------|
| 0 | 7 | 1 | 1 | `1 + right[1] = 2` |
| 1 | 3 | 1 | 2 | merge? `7 <= 5` no → max(`left[0]+1=2`, `1+right[2]=3`) = 3 |
| 2 | 5 | 2 | 1 | merge? `3 <= 2` no → max(2, 2) = 2 |
| 3 | 2 | 1 | 2 | merge? `5 <= 6` yes → `left[2] + 1 + right[4] = 4`? No: `left[2]=2`, `right[4]=1`, so candidate = 4 over indices `[1..4]`? But `[7,3,5,2,6]` left run before 3 is broken at 0→1, so actual contiguous left run is `[3,5]`, length 2, giving segment `[3,5,2,6]` length 4? Repair `2` to `5` yields `[3,5,5,6]`, valid. |
| 4 | 6 | 2 | 1 | `left[3] + 1 = 2` |

Best answer is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One forward pass builds `left`, one backward pass builds `right`, and one final pass evaluates each index as the possible repaired position. There are no nested scans. At `10^6` elements this is routine; at `10^9`, the bottleneck becomes memory bandwidth and storage, not asymptotic behavior.

### Space Complexity
`O(n)` for the `left` and `right` arrays. Those arrays own essentially all auxiliary space. It can be reduced with more careful streaming or partial state compression, but the implementation becomes less transparent and easier to get wrong around merge boundaries.

## 💡 Key Takeaways
- If a problem says “contiguous,” “ordered after at most one fix,” and asks for a maximum length, look for precomputed run lengths rather than window brute force.
- When one modification can reconnect two valid regions, model the answer as `left-run + edited point + right-run` with an explicit merge condition.
- The merge test is not “one inversion exists”; it is whether a single replacement can satisfy both neighbors: `heights[i-1] <= newValue <= heights[i+1]`, which reduces to `heights[i-1] <= heights[i+1]`.
- Be careful that `left[i-1]` and `right[i+1]` are contiguous-run lengths, not arbitrary prefix/suffix lengths; using global monotone lengths gives invalid joins.
- In production code, this is the broader pattern of converting expensive range validation into local defect accounting plus precomputed structural summaries.

## 🚀 Variations & Further Practice
- Allow up to `k` repairs instead of one. The conceptual twist is that local merge logic no longer suffices; you need a window or DP that tracks defect budget across multiple violations.
- Return the actual segment and one valid repaired value, not just the length. The harder part is reconstructing a witness while preserving linear complexity.
- Change “nondecreasing” to “strictly increasing.” The merge condition becomes stricter (`left neighbor < right neighbor`), and duplicates that were previously harmless now block merges.