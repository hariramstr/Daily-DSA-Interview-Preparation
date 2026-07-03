# Longest Annotation Span With Limited Reviewer Handoffs

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given an array of reviewer IDs in annotation order, find the maximum length of a contiguous subarray whose internal adjacent reviewer changes are at most `k`. A handoff is counted only between neighboring elements inside the chosen span. Return that maximum length. The challenge is scale: with up to `200,000` annotations, any approach that recomputes handoffs for many subarrays is too slow, so the solution must maintain window state incrementally.

## 🌍 Engineering Impact
This pattern shows up in ownership analytics, session segmentation, streaming observability, and workflow stability metrics. Examples include detecting long low-churn operator sessions in incident timelines, measuring stable executor assignment in distributed schedulers, or finding long stretches of consistent model ownership in human-in-the-loop labeling pipelines. At production scale, brute-force subarray scans collapse under high-volume event streams because every boundary recomputation multiplies cost. A sliding-window formulation turns the problem into online state maintenance: constant work per event, predictable memory, and direct applicability to real-time dashboards, anomaly detectors, and backfill jobs over large append-only logs.

## 🔍 Problem Statement
You are given an integer array `reviewers` where `reviewers[i]` is the reviewer responsible for the `i`-th annotation, and an integer `k`. For any contiguous span `reviewers[l..r]`, count the number of indices `j` such that `l < j <= r` and `reviewers[j] != reviewers[j - 1]`. That count is the number of handoffs inside the span.

Return the maximum span length whose handoff count is at most `k`.

**Constraints**
- `1 <= reviewers.length <= 200000`
- `1 <= reviewers[i] <= 1000000000`
- `0 <= k < reviewers.length`

**Examples**
- `reviewers = [5,5,2,2,2,7,7,2], k = 2` → `7`
- `reviewers = [1,3,3,4,4,4,2,2,5], k = 1` → `5`

The key constraint is input size: `O(n^2)` enumeration is not viable, so the algorithm must be linear or near-linear.

## 🪜 How to Solve This
1. Read the condition carefully → the validity of a span depends only on how many adjacent pairs inside it are different.
2. That immediately suggests a contiguous-window problem, not a prefix-sum-over-values problem. We do not care how many distinct reviewers exist; we care how many *transitions* occur.
3. If we extend the right boundary by one element, only one new adjacent pair can affect the handoff count: `(right - 1, right)`.
4. That means we can maintain the current handoff count incrementally in `O(1)` as the window grows.
5. Once the count exceeds `k`, the window is invalid. To restore validity, move the left boundary rightward.
6. When left moves, only one pair leaves the window: `(left, left + 1)`. If that pair was a handoff, decrement the count.
7. Because both pointers move only forward, total work is linear.
8. The mental model is: maintain the longest window satisfying a monotone constraint. More handoffs never become fewer unless the window shrinks, which is exactly what sliding window is built for.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` and an integer `handoffs` representing the number of adjacent reviewer changes currently inside that window. This is the right abstraction because validity is local to contiguous boundaries and updates incrementally when either pointer moves.

2. **Expand the window by advancing `right`.**  
   For each new index `right > 0`, compare `reviewers[right]` with `reviewers[right - 1]`. If they differ, increment `handoffs`, because that boundary is now inside the window.  
   **Invariant:** after expansion, `handoffs` equals the number of transitions in `[left, right]`.

3. **Shrink while invalid.**  
   While `handoffs > k`, move `left` rightward. Before incrementing `left`, inspect the boundary `(left, left + 1)`. If `reviewers[left] != reviewers[left + 1]`, that handoff is leaving the window, so decrement `handoffs`.  
   **Why correct:** the only boundary removed by shifting `left` is exactly `(left, left + 1)`.

4. **Record the best valid window.**  
   After shrinking, the window is valid again, so update `best = max(best, right - left + 1)`.

5. **Rely on monotonic pointer movement.**  
   Neither pointer ever moves backward. Each index is processed at most twice: once when entering the window, once when leaving. That gives `O(n)` time and `O(1)` extra space.

## 📊 Worked Example
Example: `reviewers = [5,5,2,2,2,7,7,2]`, `k = 2`

| right | value | new handoff? | left after shrink | handoffs | window | best |
|---|---:|---:|---:|---:|---|---:|
| 0 | 5 | no  | 0 | 0 | [5] | 1 |
| 1 | 5 | no  | 0 | 0 | [5,5] | 2 |
| 2 | 2 | yes | 0 | 1 | [5,5,2] | 3 |
| 3 | 2 | no  | 0 | 1 | [5,5,2,2] | 4 |
| 4 | 2 | no  | 0 | 1 | [5,5,2,2,2] | 5 |
| 5 | 7 | yes | 0 | 2 | [5,5,2,2,2,7] | 6 |
| 6 | 7 | no  | 0 | 2 | [5,5,2,2,2,7,7] | 7 |
| 7 | 2 | yes | 2 | 2 | [2,2,2,7,7,2] | 7 |

At `right = 7`, the third handoff enters, so the window becomes invalid. Moving `left` from `0` to `2` removes the `5->2` boundary, restoring validity.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each step adds at most one new boundary when `right` advances, and removes at most one boundary per `left` shift. Since both pointers move monotonically from `0` to `n - 1`, total operations are linear. At `10^6` elements this is routine; at `10^9`, the algorithm is still asymptotically right but becomes I/O-bound and memory-layout-sensitive.

### Space Complexity
`O(1)` extra space. The algorithm stores only pointer indices, the current handoff count, and the best length. No auxiliary arrays or maps are required. You could precompute transition flags, but that increases space to `O(n)` without improving asymptotic runtime.

## 💡 Key Takeaways
- If the constraint is “longest contiguous segment with at most X violations,” that is a strong signal for Sliding Window / Two Pointers.
- If validity changes only at window boundaries when pointers move, you can usually maintain the metric incrementally instead of recomputing it.
- Count handoffs only for adjacent pairs fully inside the current window; do not accidentally include the boundary just outside `left`.
- When shrinking, decrement `handoffs` based on the pair `(left, left + 1)` before advancing `left`, or you will introduce an off-by-one bug.
- In production systems, many “stability” metrics reduce to tracking boundary transitions, and that reframing often converts expensive rescans into online linear passes.

## 🚀 Variations & Further Practice
- **Exactly `k` handoffs instead of at most `k`:** requires counting windows under a stricter condition, often solved via `atMost(k) - atMost(k - 1)` rather than a single max-window pass.
- **Weighted handoffs:** each reviewer change has a cost instead of unit weight; the same window pattern works, but the maintained state becomes cumulative transition cost.
- **Longest span with at most `k` distinct reviewers:** similar surface form, different invariant; now you need a frequency map, not just adjacent-boundary tracking.