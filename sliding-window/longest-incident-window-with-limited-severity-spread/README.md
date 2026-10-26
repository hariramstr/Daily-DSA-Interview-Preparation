# Longest Incident Window With Limited Severity Spread

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Monotonic Queue, Deque

---

## 🗂 Problem Overview
Given a time-ordered array `severity` and a threshold `limit`, find the longest contiguous window whose maximum and minimum severities differ by at most `limit`. Return only that maximum length. The challenge is not scanning windows, but maintaining both extrema efficiently while the window grows and shrinks. Since `n` can reach 200,000, recomputing min/max for each candidate window is too expensive; the solution must update window state in near-constant amortized time.

## 🌍 Engineering Impact
This pattern shows up anywhere bounded spread matters over a live stream: anomaly suppression in observability pipelines, market-data smoothing, QoS enforcement, autoscaling signals, fraud scoring, and real-time ranking features. At scale, the wrong approach degenerates into repeated rescans of overlapping windows, burning CPU and introducing latency spikes exactly when streams are hottest. The monotonic-queue formulation enables single-pass processing with predictable memory and throughput, which matters in stream processors, edge agents, and services that must evaluate local stability constraints online rather than batch after the fact.

## 🔍 Problem Statement
You are given an integer array `severity` of length `n` and an integer `limit`. A contiguous window `severity[l..r]` is valid if:

`max(severity[l..r]) - min(severity[l..r]) <= limit`

Return the maximum possible window length `r - l + 1` over all valid windows.

Constraints:

- `1 <= severity.length <= 200000`
- `0 <= severity[i] <= 1000000000`
- `0 <= limit <= 1000000000`

Examples:

- `severity = [4, 7, 5, 6, 8, 3, 4], limit = 3` → `4`
- `severity = [10, 10, 9, 8, 7, 7, 8], limit = 2` → `5`

Edge cases matter: all equal values should return `n`; `limit = 0` means every valid window must contain identical values only; strictly increasing or decreasing streams stress max/min maintenance. The key constraint is `n = 200000`, which rules out any approach that recomputes extrema per window.

## 🪜 How to Solve This
1. Read the condition carefully → validity depends only on the current window’s `max` and `min`, not on every pair inside it.

2. That suggests a variable-size sliding window: expand `r` one step at a time, and shrink `l` only when the window becomes invalid.

3. The blocker is obvious: after each expansion or shrink, we need the current max and min fast. A plain window with rescans becomes `O(n^2)` in the worst case.

4. Ask what data structure supports:
   - append a new value on the right,
   - remove expired values from the left,
   - read current min and max in `O(1)`.

5. The answer is two monotonic deques:
   - one decreasing deque for candidate maxima,
   - one increasing deque for candidate minima.

6. As each new value arrives, discard weaker candidates from the back, because they can never become the max/min while the new value remains in the window.

7. If `maxDeque.front - minDeque.front > limit`, advance `l` and evict fronts that fall out of range.

8. After restoring validity, update the best length. Single pass, no rescans, amortized linear time.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[l, r]` over the array. Increment `r` from left to right exactly once. This is the right abstraction because the validity predicate is monotonic with respect to shrinking: once a window is invalid, moving `l` right is the only way to restore it.

2. **Maintain a decreasing deque for maxima.**  
   Store indices whose values are in non-increasing order. Before pushing `r`, pop from the back while `severity[back] < severity[r]`. This preserves the invariant that the front always holds the index of the current maximum.

3. **Maintain an increasing deque for minima.**  
   Symmetrically, pop from the back while `severity[back] > severity[r]`. The front then always holds the current minimum. Together, the two deque fronts give `max` and `min` in `O(1)`.

4. **Expand the window by adding `r` to both structures.**  
   Each index enters each deque once. Any removed index is dominated by a newer value and can never be useful again while that newer index remains.

5. **Shrink while invalid.**  
   While `severity[maxDeque.front] - severity[minDeque.front] > limit`, increment `l`. If either deque’s front index is now left of `l`, pop it. This restores the invariant that both deque fronts lie inside the active window.

6. **Record the best valid length.**  
   After shrinking, `[l, r]` is valid by construction, so update `best = max(best, r - l + 1)`.

7. **Why correctness holds.**  
   The deques always represent exactly the candidate extrema for the current window, and `l` only advances when required. Therefore every maximal valid window ending at each `r` is considered, and the global maximum is found.

## 📊 Worked Example
Example: `severity = [4, 7, 5, 6, 8, 3, 4]`, `limit = 3`

| r | val | maxDeque values | minDeque values | l after shrink | valid window | best |
|---|-----|------------------|-----------------|----------------|--------------|------|
| 0 | 4   | [4]              | [4]             | 0              | [4]          | 1    |
| 1 | 7   | [7]              | [4,7]           | 0              | [4,7]        | 2    |
| 2 | 5   | [7,5]            | [4,5]           | 0              | [4,7,5]      | 3    |
| 3 | 6   | [7,6]            | [4,5,6]         | 0              | [4,7,5,6]    | 4    |
| 4 | 8   | [8]              | [4,5,6,8]       | 1              | [7,5,6,8]    | 4    |
| 5 | 3   | [8,3]            | [3]             | 5              | [3]          | 4    |
| 6 | 4   | [4]              | [3,4]           | 5              | [3,4]        | 4    |

At `r = 4`, spread becomes `8 - 4 = 4`, so `l` advances until the minimum `4` leaves the window. At `r = 5`, adding `3` breaks the window badly, forcing multiple shrinks. Final answer: `4`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)` amortized. Each index is pushed and popped at most once from the max deque and once from the min deque, so the dominant work is linear deque maintenance plus a single left-to-right pass. At `10^6` elements this is practical; at `10^9`, linear time is still expensive but asymptotically optimal for exact streaming evaluation.

### Space Complexity
`O(n)` in the worst case for the two deques, though typically less. The space is owned entirely by the monotonic queues storing candidate indices. You cannot reduce this to `O(1)` while preserving exact online min/max updates; the trade-off would be slower recomputation or approximate summaries.

## 💡 Key Takeaways
- If a variable-size contiguous window is constrained by current `min` and `max`, that is a strong signal for sliding window plus auxiliary extrema structures.
- If the naive solution repeatedly rescans overlapping windows for extrema, look for monotonic deque optimization immediately.
- Store indices, not values, so you can evict elements precisely when they fall out of the window.
- Be careful to shrink in a `while`, not an `if`; one left shift may not be enough to restore `max - min <= limit`.
- The transferable design insight is to separate window movement from aggregate maintenance: once extrema become incremental state, online constraint evaluation becomes linear and production-friendly.

## 🚀 Variations & Further Practice
- Return the actual window boundaries, not just the length; same core method, but you must preserve the best `[l, r]` when ties or multiple optimal windows exist.
- Support streaming updates with expirations by time rather than index count; the twist is that eviction is driven by timestamps, but monotonic deque invariants still apply.
- Generalize from `max - min <= limit` to windows constrained by multiple rolling statistics; the harder part is identifying which metrics admit exact monotonic maintenance and which require trees or heaps.