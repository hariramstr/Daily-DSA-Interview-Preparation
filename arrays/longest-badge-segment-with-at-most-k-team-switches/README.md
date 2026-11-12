# Longest Badge Segment With At Most K Team Switches

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Two Pointers

---

## 🗂 Problem Overview
Given an array `teams`, find the maximum length of a contiguous subarray whose number of adjacent value changes is at most `k`. A switch occurs at index `i` when `teams[i] != teams[i - 1]` inside the chosen segment. Return only the best segment length. The challenge is scale: with up to `200000` scans, any approach that recomputes switch counts for many subarrays degenerates into quadratic work and is not acceptable.

## 🌍 Engineering Impact
This pattern shows up in event-stream analysis, observability pipelines, clickstream segmentation, manufacturing telemetry, and fraud/security monitoring. You are not counting unique values globally; you are enforcing a bounded transition budget inside a contiguous window. That distinction matters in systems that reason about churn, instability, or mode changes over time. At scale, brute-force segment evaluation collapses under high-volume streams and long retention windows. A linear sliding-window formulation enables online processing, predictable memory use, and easy integration into stream processors or low-latency analytics services where recomputation is operationally expensive.

## 🔍 Problem Statement
You are given an integer array `teams` where `teams[i]` is the team ID seen at the `i`-th badge scan in chronological order, and an integer `k`. For any contiguous segment `teams[l...r]`, define its team-switch count as the number of indices `i` such that `l < i <= r` and `teams[i] != teams[i - 1]`. Return the maximum segment length whose switch count is at most `k`.

This is about adjacent transitions, not distinct values. For example, `[4,4,7,7,4]` has `2` switches.

Constraints:
- `1 <= teams.length <= 200000`
- `0 <= k < teams.length`
- `1 <= teams[i] <= 1000000000`

Examples:
- `teams = [5,5,2,2,2,9,9,5], k = 2` → `7`
- `teams = [1,3,1,3,1,3], k = 3` → `4`

The `200000` upper bound rules out nested-window enumeration and pushes toward an `O(n)` solution.

## 🪜 How to Solve This
1. Read the condition carefully → the constraint is on **adjacent changes inside a window**, not on frequency or distinct count. That immediately suggests we should track boundary transitions, not a hashmap of values.

2. Reframe the window property → for a segment `[l...r]`, the only relevant events are positions where `teams[i] != teams[i-1]`. If we know how many such events lie inside the current window, we know whether it is valid.

3. Think contiguous + maximize length + monotonic validity → classic Sliding Window / Two Pointers. Expanding the right end can only keep or increase the switch count by at most one. Shrinking the left end can only keep or decrease it by at most one.

4. Maintain a running switch count for the current window. When moving `right`, add one if `teams[right] != teams[right-1]`. If the count exceeds `k`, advance `left` until the window is valid again.

5. The subtle part is shrinking correctly: when `left` moves from `x` to `x+1`, you remove the boundary between `x` and `x+1`. If those values differ, the window loses one switch.

6. Because each pointer moves forward once, the full scan is linear.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Let `left = 0`, `switches = 0`, and iterate `right` from `0` to `n - 1`. This pattern fits because validity is defined on a contiguous range and can be repaired incrementally when violated.

2. **Expand the window by moving `right`.**  
   For each new `right > 0`, compare `teams[right]` with `teams[right - 1]`. If they differ, increment `switches`. This is correct because that adjacent boundary has just entered the current window.

3. **Restore validity when `switches > k`.**  
   While the window is invalid, move `left` rightward. Before incrementing `left`, inspect the boundary being removed: if `teams[left] != teams[left + 1]`, decrement `switches`. This maintains the invariant that `switches` always equals the number of adjacent changes fully contained in `[left...right]`.

4. **Record the best valid length.**  
   After the shrink loop, the window satisfies `switches <= k`. Update `best = max(best, right - left + 1)`. This is safe because every candidate considered here is valid by construction.

5. **Why this is optimal.**  
   For each `right`, the algorithm keeps the leftmost valid `left` after repair, which yields the maximum valid window ending at `right`. Taking the maximum over all `right` gives the global optimum.

6. **Why no extra structures are needed.**  
   Unlike “at most K distinct values,” this problem depends only on local transitions. A scalar counter is enough; no frequency map is required.

## 📊 Worked Example
Example: `teams = [5,5,2,2,2,9,9,5]`, `k = 2`

| right | teams[right] | new switch? | switches | left after shrink | window | length |
|---|---:|---:|---:|---:|---|---:|
| 0 | 5 | no  | 0 | 0 | `[5]` | 1 |
| 1 | 5 | no  | 0 | 0 | `[5,5]` | 2 |
| 2 | 2 | yes | 1 | 0 | `[5,5,2]` | 3 |
| 3 | 2 | no  | 1 | 0 | `[5,5,2,2]` | 4 |
| 4 | 2 | no  | 1 | 0 | `[5,5,2,2,2]` | 5 |
| 5 | 9 | yes | 2 | 0 | `[5,5,2,2,2,9]` | 6 |
| 6 | 9 | no  | 2 | 0 | `[5,5,2,2,2,9,9]` | 7 |
| 7 | 5 | yes | 3 | 2 | `[2,2,2,9,9,5]` | 6 |

At `right = 7`, the third switch makes the window invalid. Advancing `left` from `0` to `2` removes the `5 -> 2` boundary, reducing `switches` back to `2`. Best length remains `7`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is processed once by `right`, and `left` also advances at most `n` times total. The dominant work is constant-time boundary checks during pointer movement. This remains practical at `10^6` elements, while quadratic approaches are already unusable; at `10^9`, even linear time becomes a systems problem rather than an algorithm problem.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, the current switch count, and the best length. No auxiliary map or prefix structure is required. Space cannot be meaningfully reduced below constant, short of destructive in-place streaming assumptions that do not change asymptotic behavior.

## 💡 Key Takeaways
- If the constraint is “contiguous segment” plus “at most K violations,” first test whether a sliding window can maintain that violation count incrementally.
- When the property depends on adjacent relationships rather than element frequencies, track boundaries, not distinct values or counts per value.
- The off-by-one trap is that switches live on edges between indices, so adding `right` checks `(right - 1, right)` and removing `left` checks `(left, left + 1)`.
- Do not decrement the switch count when moving `left` unless `teams[left] != teams[left + 1]`; removing a repeated pair changes nothing.
- In production stream processing, the winning design often comes from modeling the minimal state needed for validity, not from richer but unnecessary summaries.

## 🚀 Variations & Further Practice
- Return the actual segment bounds, not just the length; same core window, but now tie-breaking rules and reproducibility matter.
- Allow up to `k` switches **between groups of equivalent IDs** under a normalization function; the twist is preprocessing or on-the-fly canonicalization before transition checks.
- Support many queries with different `k` values over the same array; the harder part is moving from a single online pass to offline preprocessing or query-aware indexing.