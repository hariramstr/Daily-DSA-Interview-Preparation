# Longest Lecture Clip With Limited Topic Drift

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** sliding window, two pointers, array

---

## 🗂 Problem Overview
Given an integer array `topics`, find the maximum length of a contiguous subarray whose number of adjacent topic changes is at most `k`. A transition is counted only when consecutive elements differ, so this is about boundary changes inside a window, not the number of distinct values. The challenge is scale: with up to `2 * 10^5` elements, any approach that recomputes transitions for many subarrays is too slow, so the solution must maintain window state incrementally.

## 🌍 Engineering Impact
This pattern shows up in stream segmentation, media clipping, observability pipelines, and ranking systems where you need the longest interval satisfying a local instability budget. Examples include extracting coherent video/audio segments, detecting low-churn periods in event streams, and finding stable spans in telemetry labels or model outputs. At scale, brute-force interval evaluation collapses under quadratic behavior and repeated recomputation. A sliding-window design turns the problem into online state maintenance: constant-time updates per event, bounded memory, and predictable latency. That matters when the same logic runs continuously over large logs, Kafka topics, or user-session traces.

## 🔍 Problem Statement
You are given an array `topics` where `topics[i]` is the topic label for minute `i` of a lecture. You must return the length of the longest contiguous subarray containing at most `k` topic transitions. A transition occurs between adjacent indices `i - 1` and `i` when `topics[i] != topics[i - 1]`.

This is not a distinct-elements constraint. For example, `[2, 2, 3, 3, 2]` uses only two labels but contains two transitions: `2 -> 3` and `3 -> 2`.

Constraints:

- `1 <= topics.length <= 2 * 10^5`
- `1 <= topics[i] <= 10^9`
- `0 <= k < topics.length`

Examples:

- `topics = [4, 4, 1, 1, 1, 3, 3, 4], k = 2` → `7`
- `topics = [5, 6, 5, 6, 5], k = 1` → `2`

The key constraint is input size: we need a linear or near-linear scan, not nested subarray checks.

## 🪜 How to Solve This
1. Read the condition carefully → validity depends on **adjacent differences inside a contiguous window**, not on frequencies or distinct counts.
2. That immediately suggests a window over the array → when you extend the right edge by one element, only **one new adjacency** can enter the window: the pair `(right - 1, right)`.
3. So maintain a running count `transitions` for the current window `[left, right]`.
4. When moving `right` forward:
   - if `topics[right] != topics[right - 1]`, increment `transitions`.
5. If `transitions > k`, the window is invalid → shrink from the left until it becomes valid again.
6. When moving `left` forward, remove the adjacency that leaves the window:
   - if `topics[left] != topics[left + 1]`, decrement `transitions`.
7. At every valid state, update the best length.

Why this works: each boundary between adjacent elements belongs to the window iff both endpoints are inside it. Expanding or shrinking the window changes membership of only one boundary at a time, so the transition count can be updated in O(1). That is the signature of a sliding-window problem.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` and a counter `transitions` representing how many adjacent unequal pairs exist fully inside the window. This pattern fits because validity is monotonic with respect to shrinking: if a window has too many transitions, moving `left` rightward can only keep or reduce that count.

2. **Initialize state.**  
   Start with `left = 0`, `transitions = 0`, `best = 1`. A single element has zero transitions, so every non-empty array has at least one valid window.

3. **Expand the right pointer one step at a time.**  
   For each `right` from `1` to `n - 1`, compare `topics[right]` with `topics[right - 1]`. If they differ, increment `transitions`, because that boundary is now inside the window.

4. **Restore validity when the budget is exceeded.**  
   While `transitions > k`, advance `left`. Before incrementing `left`, inspect the boundary `(left, left + 1)`. If `topics[left] != topics[left + 1]`, decrement `transitions`, because that boundary leaves the window once `left` moves.

5. **Maintain the invariant.**  
   After the shrink loop, the window `[left, right]` is the longest valid window ending at `right` with at most `k` transitions. Update `best = max(best, right - left + 1)`.

6. **Why correctness holds.**  
   Every index is visited at most twice: once by `right`, once by `left`. The transition count is exact because each adjacent pair enters and leaves the window exactly once. No valid candidate is skipped, since for every `right` we keep the maximal valid suffix ending there.

## 📊 Worked Example
Example: `topics = [4, 4, 1, 1, 1, 3, 3, 4]`, `k = 2`

| right | topics[right] | New transition? | left after shrink | transitions | window                | length | best |
|------:|---------------:|----------------:|------------------:|------------:|-----------------------|-------:|-----:|
| 0     | 4              | —               | 0                 | 0           | `[4]`                 | 1      | 1    |
| 1     | 4              | No              | 0                 | 0           | `[4,4]`               | 2      | 2    |
| 2     | 1              | Yes             | 0                 | 1           | `[4,4,1]`             | 3      | 3    |
| 3     | 1              | No              | 0                 | 1           | `[4,4,1,1]`           | 4      | 4    |
| 4     | 1              | No              | 0                 | 1           | `[4,4,1,1,1]`         | 5      | 5    |
| 5     | 3              | Yes             | 0                 | 2           | `[4,4,1,1,1,3]`       | 6      | 6    |
| 6     | 3              | No              | 0                 | 2           | `[4,4,1,1,1,3,3]`     | 7      | 7    |
| 7     | 4              | Yes             | 2                 | 2           | `[1,1,1,3,3,4]`       | 6      | 7    |

At `right = 7`, adding the final `4` creates a third transition, so the left side is advanced until one transition boundary is removed.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each step adds `right` once, and `left` only moves forward, never backward. Transition updates are constant-time comparisons on adjacent elements. At `10^6` elements this remains practical in a single pass; at `10^9`, the algorithm is still asymptotically optimal but would require streaming/distributed execution due to raw input volume.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, the current transition count, and the best length. No auxiliary map or prefix array is required. You could precompute transition flags, but that would increase space to `O(n)` without improving asymptotic runtime.

## 💡 Key Takeaways
- If the constraint is defined over a **contiguous range** and can be updated by adding/removing one element at a boundary, think sliding window immediately.
- When the metric depends on **adjacent pairs** rather than element counts, model the window over boundaries, not just over values.
- The transition added on expansion is only between `right - 1` and `right`; do not rescan the whole window.
- On shrink, remove the boundary `(left, left + 1)` before incrementing `left`, or the transition count will drift out of sync.
- In production stream processing, local-state window maintenance is often the difference between constant-latency online evaluation and unscalable recomputation.

## 🚀 Variations & Further Practice
- Return the longest clip with at most `k` transitions **and** at most `m` distinct topics; the twist is maintaining two independent window constraints simultaneously.
- Support many offline queries with different `k` values on the same array; the twist is moving from one-pass online logic to preprocessing or query-indexed optimization.
- Maximize weighted clip score where each transition has a custom penalty instead of unit cost; the twist is replacing a simple count with an additive budget over boundary weights.