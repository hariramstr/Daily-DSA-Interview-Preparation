# Maximum Consecutive Days After Moving One Holiday

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Prefix Sum, Greedy

---

## 🗂 Problem Overview
Given a planning horizon of `n` days and a sorted list of holiday positions, compute the maximum length of consecutive working days after moving at most one holiday onto a different non-holiday day. The holiday count must stay unchanged, and holidays cannot collide. The challenge is that `n` can be as large as `10^9`, so any day-by-day simulation is infeasible; the algorithm must operate on holiday boundaries and gaps only.

## 🌍 Engineering Impact
This pattern shows up anywhere sparse markers partition a huge logical space: maintenance windows on fleet calendars, blocked intervals in schedulers, tombstones in LSM-backed storage, blackout slots in ad serving, or unavailable shards in distributed query planners. The core issue is optimizing continuity after relocating one disruptive marker. At scale, iterating the full domain is impossible; only event positions matter. Engineers who recognize this shift—from dense timeline processing to sparse boundary analysis—unlock designs that stay fast under billion-scale ranges while keeping memory proportional to actual events, not theoretical capacity.

## 🔍 Problem Statement
You are given:

- An integer `n` representing days `1..n`
- A strictly increasing array `holidays`, where each value is a holiday day

A working day is any day not in `holidays`. You may move **at most one** existing holiday to another day in `1..n` that is currently a working day. After the move:

- the number of holidays must remain unchanged
- no two holidays may share a day

Return the maximum possible length of a consecutive block of working days.

Examples:

- `n = 10, holidays = [3, 8]` → `7`
- `n = 15, holidays = [4, 8, 12]` → `7`

Key constraint: `n` may be `10^9`, while `holidays.length` is at most `2 * 10^5`. That forces an algorithm based on holiday positions and gap lengths, not on scanning every day.

## 🪜 How to Solve This
1. Start from the only structure that matters: holidays split the timeline into working-day gaps. Since `holidays` is already sorted, those gaps are easy to compute.

2. If you remove one holiday at position `holidays[i]`, the gap on its left and the gap on its right become connected, plus the holiday day itself becomes a working day. That gives a merged block of  
   `leftGap + 1 + rightGap`.

3. But removing a holiday is only half the operation. You must place it somewhere else. If there exists **any other** working-day gap outside this merged region, you can place the holiday there without damaging the merged block.

4. If such an external gap exists, the merged block stays `leftGap + 1 + rightGap`. If not, the moved holiday must land inside that merged block, reducing the best achievable length by `1`.

5. So the real question per holiday is not just “what do I merge?” but also “is there another gap available elsewhere?” That naturally suggests precomputing all gap lengths and querying the largest gap excluding the two adjacent to the holiday.

6. Once you see the problem as sparse gaps plus an exclusion query, the solution becomes a linear pass with prefix/suffix maxima.

## 🧩 Algorithm Walkthrough
1. **Model working segments as gaps between holidays**  
   Build an array `gaps` of length `m + 1`, where `m = holidays.length`.  
   - `gaps[0] = holidays[0] - 1`  
   - `gaps[i] = holidays[i] - holidays[i - 1] - 1` for `1 <= i < m`  
   - `gaps[m] = n - holidays[m - 1]`  
   Each `gaps[k]` is the number of consecutive working days between neighboring holidays or boundaries. This is the core sparse representation.

2. **Precompute prefix and suffix maxima over gaps**  
   Build `prefMax[i] = max(gaps[0..i])` and `sufMax[i] = max(gaps[i..m])`.  
   This enables O(1) queries for the largest gap excluding the two gaps adjacent to a chosen holiday.

3. **Evaluate removing each holiday**  
   Holiday `i` sits between `gaps[i]` and `gaps[i + 1]`. Removing it creates a candidate merged block  
   `merged = gaps[i] + 1 + gaps[i + 1]`.  
   This is correct because the holiday day itself becomes a working day, bridging both sides.

4. **Check whether relocation can avoid the merged block**  
   Query the largest gap outside indices `i` and `i + 1`:  
   - left side: `prefMax[i - 1]` if `i - 1 >= 0`  
   - right side: `sufMax[i + 2]` if `i + 2 <= m`  
   Let `externalMax` be the maximum of those.  
   If `externalMax > 0`, there exists another working day somewhere else, so the moved holiday can be placed outside the merged block and the result is `merged`. Otherwise, the holiday must be reinserted into that block, so the result is `merged - 1`.

5. **Take the global maximum**  
   Also handle the implicit “do not move” case automatically: every original gap can be achieved or exceeded by considering moves, but taking the maximum over all candidates is sufficient.  
   Pattern: **Prefix/Suffix Maximum + Greedy local merge**. Prefix/suffix maxima give constant-time exclusion queries; the greedy part is that once a holiday is chosen, the optimal effect is determined entirely by adjacent gaps and whether any external placement exists.

## 📊 Worked Example
Example: `n = 15`, `holidays = [4, 8, 12]`

First compute gaps:

| Segment | Days | Length |
|---|---|---:|
| `gaps[0]` | `1..3` | 3 |
| `gaps[1]` | `5..7` | 3 |
| `gaps[2]` | `9..11` | 3 |
| `gaps[3]` | `13..15` | 3 |

So `gaps = [3, 3, 3, 3]`.

Prefix max: `[3, 3, 3, 3]`  
Suffix max: `[3, 3, 3, 3]`

Now test each holiday:

1. Remove day `4`  
   `merged = gaps[0] + 1 + gaps[1] = 3 + 1 + 3 = 7`  
   External gap exists (`gaps[2]` or `gaps[3]`), so result = `7`

2. Remove day `8`  
   `merged = gaps[1] + 1 + gaps[2] = 7`  
   External gap exists (`gaps[0]` or `gaps[3]`), so result = `7`

3. Remove day `12`  
   `merged = gaps[2] + 1 + gaps[3] = 7`  
   External gap exists, so result = `7`

Maximum = `7`.

## ⏱ Complexity Analysis

### Time Complexity
`O(m)`, where `m = holidays.length`. We make a constant number of linear passes: one to build gaps, one for prefix maxima, one for suffix maxima, and one to evaluate each holiday. This scales comfortably to `10^6` holidays and remains viable even when `n = 10^9`, because runtime depends on sparse events, not the full day range.

### Space Complexity
`O(m)` for the `gaps`, `prefMax`, and `sufMax` arrays. The dominant cost is the auxiliary max-tracking structure. Space can be reduced somewhat by computing one side on the fly, but that complicates exclusion logic and usually is not worth the loss in clarity.

## 💡 Key Takeaways
- If `n` is huge but the marked positions are sparse and sorted, the right abstraction is usually gaps or boundaries, not the full domain.
- When each decision excludes a small local neighborhood but needs a global best elsewhere, prefix/suffix maxima are a strong signal.
- The merged block after removing holiday `i` is `gaps[i] + 1 + gaps[i+1]`; forgetting the `+1` is the most common correctness bug.
- “Can I move the holiday away harmlessly?” requires checking for an external gap with at least one working day; `0`-length gaps do not help.
- In production systems, sparse event modeling is often the difference between an algorithm that scales with configured capacity and one that scales with actual activity.

## 🚀 Variations & Further Practice
- Allow moving up to `k` holidays instead of one. The twist is that local merge decisions interact, pushing the problem toward interval DP or sliding-window reasoning over blocked positions.
- Holidays become intervals rather than single days. The harder part is handling variable-length blocked regions and relocation feasibility without overlap.
- Maximize the total length of the top two disjoint working blocks after one move. The twist is that a locally optimal merge may reduce the second-best segment, so single-objective greediness no longer suffices.