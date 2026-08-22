# Longest Reading Sprint With Limited Bookmark Moves

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Monotonic Queue, Array

---

## 🗂 Problem Overview
Given an array `pages` and an integer `limit`, find the maximum length of a contiguous subarray where the difference between the largest and smallest values in that window is at most `limit`. The output is a single integer: the longest valid window size. The challenge is maintaining window `min` and `max` efficiently while expanding and shrinking the window, because recomputing them naively for every candidate range is too slow at `pages.length` up to `100000`.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maintain a bounded spread over a moving range: stream quality monitoring, fraud detection windows, market-data smoothing, observability pipelines, and adaptive rate-control loops. In production, the question is rarely “what is the max window?” and more often “how long can we stay within tolerance before violating an SLO?” Without an incremental min/max structure, teams fall back to repeated scans or balanced trees, which either collapse under throughput or add unnecessary complexity. Monotonic queues enable predictable linear behavior, low latency, and straightforward backpressure-aware implementations on large streams.

## 🔍 Problem Statement
You are given an integer array `pages` where `pages[i]` is the number of pages in the `i`th chapter, and chapters must be read in order. For any contiguous sprint `pages[l..r]`, define:

- `effort = max(pages[l..r]) - min(pages[l..r])`

A sprint is valid if `effort <= limit`. Return the maximum possible length of any valid contiguous sprint.

Constraints:

- `1 <= pages.length <= 100000`
- `1 <= pages[i] <= 1000000000`
- `0 <= limit <= 1000000000`

Examples:

- `pages = [12, 15, 14, 10, 13, 18], limit = 5` → `5`
- `pages = [7, 7, 7, 20, 21, 22], limit = 2` → `3`

Edge cases matter: identical values, `limit = 0`, strictly increasing or decreasing arrays, and windows that become invalid only after a late outlier. The `100000` bound rules out any approach that rescans each window to recompute min and max.

## 🪜 How to Solve This
1. Read the requirement carefully → we need the **longest contiguous** range, so this is a window problem, not sorting or subsequence DP.

2. Ask what makes a window valid → only `max(window) - min(window) <= limit` matters. So every time the right edge grows, we need current min and max immediately.

3. A naive sliding window is not enough → when the left edge moves, the old min or max may leave the window, and recomputing by scanning would make the algorithm quadratic.

4. That suggests a data structure that supports:
   - push a new value on the right,
   - remove expired values from the left,
   - read current min and max in O(1).

5. Monotonic queues fit exactly:
   - one decreasing deque for the maximum,
   - one increasing deque for the minimum.

6. Expand `right` one step at a time. If `max - min` exceeds `limit`, advance `left` until the window is valid again, evicting stale indices from both deques.

7. Because each index enters and leaves each deque once, the whole process is linear. That is the key observation that makes this scale.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` that always represents the current candidate sprint. This is the right abstraction because validity depends on a contiguous range and can be restored by moving `left` forward.

2. **Track maxima with a decreasing deque.**  
   Store indices, not values. Before pushing `right`, pop from the back while `pages[back] < pages[right]`. The deque front always holds the index of the current maximum. The invariant: values in this deque are in non-increasing order.

3. **Track minima with an increasing deque.**  
   Similarly, pop from the back while `pages[back] > pages[right]`. The front is the current minimum. The invariant: values are in non-decreasing order.

4. **Expand the window by moving `right`.**  
   Insert the new index into both deques after enforcing monotonicity. At this point, both fronts represent the max and min for the full window ending at `right`.

5. **Shrink while invalid.**  
   If `pages[maxDeque[0]] - pages[minDeque[0]] > limit`, increment `left` until the condition is restored. Whenever a deque front is left of `left`, pop it. This is correct because those indices are no longer inside the window.

6. **Record the best valid length.**  
   After restoring validity, update `best = max(best, right - left + 1)`. The invariant now is strong: after each iteration, `[left, right]` is valid and as far left as possible for this `right`.

7. **Why linear time holds.**  
   Each index is pushed once and popped at most once from each deque. No index re-enters. That amortized accounting is what turns an apparently dynamic min/max problem into `O(n)`.

## 📊 Worked Example
Example: `pages = [12, 15, 14, 10, 13, 18]`, `limit = 5`

| right | pages[right] | left after shrink | max | min | valid window |
|---|---:|---:|---:|---:|---|
| 0 | 12 | 0 | 12 | 12 | `[12]` |
| 1 | 15 | 0 | 15 | 12 | `[12,15]` |
| 2 | 14 | 0 | 15 | 12 | `[12,15,14]` |
| 3 | 10 | 0 | 15 | 10 | `[12,15,14,10]` |
| 4 | 13 | 0 | 15 | 10 | `[12,15,14,10,13]` |
| 5 | 18 | 2 | 18 | 10 → 10 invalid, then 14 valid? no, then 10 removed, min 13 | `[14,10,13,18]` invalid → `[10,13,18]` invalid → `[13,18]` valid |

Trace summary:
1. Up through index `4`, `max=15`, `min=10`, so effort is `5`; best becomes `5`.
2. Adding `18` makes effort `8`, so the window must shrink.
3. Move `left` forward, removing expired indices from deque fronts.
4. Once the window becomes `[13,18]`, effort is `5` again.
5. Best remains `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is inserted into and removed from each monotonic deque at most once, so the dominant work is amortized constant time per index. At `10^6` elements this remains practical; at `10^9`, linear time is still expensive, but it is the right asymptotic shape for streaming or partitioned processing.

### Space Complexity
`O(n)` in the worst case, owned by the two deques when the window grows large and values remain monotonic. In practice this is bounded by window size, not full input size. You cannot reduce it to `O(1)` without losing efficient dynamic min/max maintenance.

## 💡 Key Takeaways
- If the problem asks for the longest **contiguous** range under a constraint, start with sliding window before considering heavier structures.
- If window validity depends on both current minimum and maximum, monotonic deques are a strong signal; repeated rescans are the usual anti-pattern.
- Store **indices**, not just values, so you can evict elements that fall out of the left side of the window.
- Be careful about the shrink condition: update deques for the new `right`, then shrink while `max - min > limit`, then compute length.
- The transferable design insight: incremental state maintenance beats recomputation when operating on high-throughput ordered data.

## 🚀 Variations & Further Practice
- Replace `max - min <= limit` with a budget on total adjustment cost, such as making all values equal with at most `k` edits; the twist is that min/max alone no longer captures validity.
- Ask for the number of valid subarrays instead of the longest one; same window mechanics, but counting logic changes and off-by-one errors become easier.
- Generalize to time-based streams with expiring events rather than array indices; the conceptual twist is eviction by timestamp, not by pointer position.